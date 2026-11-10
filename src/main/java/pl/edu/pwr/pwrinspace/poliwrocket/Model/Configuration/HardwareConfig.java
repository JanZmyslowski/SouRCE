package pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;
import org.slf4j.Logger;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.Command;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.Sensor;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.SensorRepository;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.SerialPort.SerialPortManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HardwareConfig {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(HardwareConfig.class);
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final ScheduledExecutorService gpioMonitor = Executors.newSingleThreadScheduledExecutor();
    private static final Map<String, DigitalInput> activeInputs = new HashMap<>();
    private static final Map<Integer, DigitalInput> activeInputsByPin = new HashMap<>();
    private static final Map<String, DigitalState> pinLastStatesForCommand = new HashMap<>();
    private static final Map<String, DigitalOutput> activeOutputs = new HashMap<>();
    private static final long DEBOUNCE_TIME_MS = 125;
    private static final Map<Integer, DigitalOutput> fanOutputs = new HashMap<>();
    public static final int[] FAN_PINS = {14, 15};
    public static final int[] PROG_PINS = {8, 9, 10, 11};

    public static DigitalOutput getDigitalOutput(String sensorKey) {
        return activeOutputs.get(sensorKey);
    }

    public static DigitalOutput getFanOutput(int pin) {
        return fanOutputs.get(pin);
    }

    public static void turnOnProgLed(Context pi4j) {
        if (!isRunningOnPi()) {
            LOGGER.warn("Running on non-ARM architecture ({}). Skipping GPIO initialization.", System.getProperty("os.arch"));
            return;
        }

        for (int pin : PROG_PINS) {
            var pi4jConfig = DigitalOutput.newConfigBuilder(pi4j)
                    .id("prog_led_" + pin)
                    .bus(14)
                    .bcm(pin)
                    .shutdown(DigitalState.LOW)
                    .initial(DigitalState.LOW);

            DigitalOutput digitalOutput = pi4j.create(pi4jConfig);
            digitalOutput.high();
        }
    }

    public static void setupFanOutputs(Context pi4j) {
        if (!isRunningOnPi()) {
            LOGGER.warn("Running on non-ARM architecture ({}). Skipping GPIO initialization.", System.getProperty("os.arch"));
            return;
        }

        for (int pin : FAN_PINS) {
            var pi4jConfig = DigitalOutput.newConfigBuilder(pi4j)
                    .id("fan_" + pin)
                    .bus(14)
                    .bcm(pin)
                    .shutdown(DigitalState.LOW)
                    .initial(DigitalState.LOW);

            DigitalOutput digitalOutput = pi4j.create(pi4jConfig);
            fanOutputs.put(pin, digitalOutput);
        }
    }

    public static void assignCommandsToGPIO(Context pi4j, List<Command<?>> commandsList) {
        if (!isRunningOnPi()) {
            LOGGER.warn("Running on non-ARM architecture ({}). Skipping GPIO initialization.", System.getProperty("os.arch"));
            return;
        }

        clearAllHardwareInputs(pi4j);

        for (Command<?> command : commandsList) {
            String gpioRising = command.getGPIORising();
            String gpioFalling = command.getGPIOFalling();

            if (gpioRising != null && !gpioRising.trim().isEmpty()) {
                if (gpioFalling != null && !gpioFalling.trim().isEmpty()) {
                    LOGGER.error("Command '{}' has both rising and falling GPIO pins defined. Only one should be set. Skipping.", command.getCommandTriggerKey());
                    continue;
                }
            }

            if (gpioRising == null || gpioRising.trim().isEmpty()) {
                if (gpioFalling == null || gpioFalling.trim().isEmpty()) {
                    continue;
                }
            }

            int gpioPin = (gpioRising != null && !gpioRising.trim().isEmpty()) ? Integer.parseInt(gpioRising.trim()) : Integer.parseInt(gpioFalling.trim());
            try {
                String triggerKey = command.getCommandTriggerKey() + "_" + command.getCommandType().name();

                if (activeInputs.containsKey(triggerKey)) {
                    LOGGER.info("Input for key '{}' already configured. Skipping.", triggerKey);
                    continue;
                }

                LOGGER.info("Assigning command '{}' to GPIO pin '{}'", triggerKey, gpioPin);

                DigitalInput digitalInput;
                if (!activeInputsByPin.containsKey(gpioPin)) {
                    LOGGER.info("Provisioning new GPIO pin {} for command '{}'", gpioPin, triggerKey);
                    var pi4jConfig = DigitalInput.newConfigBuilder(pi4j)
                            .id(triggerKey)
                            .bcm(gpioPin)
                            .pull(PullResistance.PULL_UP)
                            .debounce(DEBOUNCE_TIME_MS * 1000L);

                    digitalInput = pi4j.create(pi4jConfig);
                    pinLastStatesForCommand.put(triggerKey, digitalInput.state());
                    activeInputsByPin.put(gpioPin, digitalInput);
                } else {
                    LOGGER.info("GPIO pin {} already provisioned. Reusing for command '{}'", gpioPin, triggerKey);
                    digitalInput = activeInputsByPin.get(gpioPin);
                }

                gpioMonitor.scheduleAtFixedRate(() -> {
                    try {
                        DigitalState currentState = digitalInput.state();
                        DigitalState lastState = pinLastStatesForCommand.get(triggerKey);

                        if (currentState != lastState) {
                            LOGGER.info("GPIO pin {} state changed from {} to {} owner: {}", gpioPin, lastState, currentState, triggerKey);
                            pinLastStatesForCommand.put(triggerKey, currentState);
                            if (currentState == DigitalState.LOW && gpioFalling != null && !gpioFalling.trim().isEmpty()) {
                                LOGGER.info("GPIO pin {} changed to LOW. Triggering command '{}'", gpioPin, triggerKey);
                                executorService.execute(() -> SerialPortManager.getInstance().write(command));
                            } else if (currentState == DigitalState.HIGH && gpioRising != null && !gpioRising.trim().isEmpty()) {
                                LOGGER.info("GPIO pin {} changed to HIGH. Triggering command '{}'", gpioPin, triggerKey);
                                executorService.execute(() -> SerialPortManager.getInstance().write(command));
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error while monitoring GPIO pin {}: {}", gpioPin, e.getMessage());
                    }
                }, 0 , DEBOUNCE_TIME_MS, TimeUnit.MILLISECONDS);

                activeInputs.put(triggerKey, digitalInput);

            } catch (NumberFormatException e) {
                LOGGER.error("Incorrect GPIO format {} for command {}", gpioPin, command.getCommandTriggerKey());
            } catch (Exception e) {
                LOGGER.error("Failed to provision GPIO pin {} for command {}: {}", gpioPin, command.getCommandTriggerKey(), e.getMessage());
            }
        }
    }

    public static void clearAllHardwareInputs(Context pi4j) {
        if (activeInputs.isEmpty()) {
            return;
        }

        LOGGER.info("Clearing old GPIO configuration... (Active pins: {})", activeInputs.size());

        for (Map.Entry<String, DigitalInput> entry : activeInputs.entrySet()) {
            DigitalInput input = entry.getValue();
            try {
                input.close();
                pi4j.registry().remove(input.id());
            } catch (Exception e) {
                LOGGER.error("Failed to close GPIO pin associated with trigger '{}': {}", entry.getKey(), e.getMessage());
            }
        }

        activeInputs.clear();
        LOGGER.info("GPIO configuration cleared successfully.");
    }

    public static void assignSensorsToGPIO(Context pi4j, SensorRepository sensorRepository) {
        if (!isRunningOnPi()) {
            LOGGER.warn("Running on non-ARM architecture ({}). Skipping GPIO initialization.", System.getProperty("os.arch"));
            return;
        }

        clearAllHardwareOutputs(pi4j);

        for (String sensorKey : sensorRepository.getSensorsKeys()) {
            Sensor sensor = sensorRepository.getSensorByName(sensorKey);
            String gpioPin = sensor.getGPIOPin();
            String sensorName = sensor.getName();

            if (gpioPin != null && !gpioPin.trim().isEmpty()) {
                try {
                    int bcmPin = Integer.parseInt(gpioPin.trim());

                    if (activeOutputs.containsKey(sensorName)) {
                        LOGGER.info("Output for key '{}' already configured. Skipping.", sensorName);
                        continue;
                    }

                    LOGGER.info("Assigning sensor '{}' to GPIO pin '{}'", sensorName, bcmPin);

                    var pi4jConfig = DigitalOutput.newConfigBuilder(pi4j)
                            .id(sensorName)
                            .bus(14)
                            .bcm(bcmPin)
                            .shutdown(DigitalState.LOW)
                            .initial(DigitalState.LOW);

                    DigitalOutput digitalOutput = pi4j.create(pi4jConfig);

                    activeOutputs.put(sensorName, digitalOutput);
                } catch (NumberFormatException e) {
                    LOGGER.error("Incorrect GPIO format {} for sensor '{}'", gpioPin, sensorName);
                } catch (Exception e) {
                    LOGGER.error("Failed to provision GPIO pin {} for sensor {}: {}", gpioPin, sensorName, e.getMessage());
                }
            }
        }
    }

    public static void clearAllHardwareOutputs(Context pi4j) {
        if (activeOutputs.isEmpty()) {
            return;
        }

        LOGGER.info("Clearing old GPIO output configuration... (Active pins: {})", activeOutputs.size());

        for (Map.Entry<String, DigitalOutput> entry : activeOutputs.entrySet()) {
            DigitalOutput output = entry.getValue();
            try {
                output.close();
                pi4j.registry().remove(output.id());
            } catch (Exception e) {
                LOGGER.error("Failed to close GPIO output pin associated with trigger '{}': {}", entry.getKey(), e.getMessage());
            }
        }

        activeOutputs.clear();
        LOGGER.info("GPIO output configuration cleared successfully.");
    }

    public static boolean isRunningOnPi() {
        String osArch = System.getProperty("os.arch").toLowerCase();
        return osArch.contains("arm") || osArch.contains("aarch64");
    }
}
