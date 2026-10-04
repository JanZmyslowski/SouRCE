package pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.PullResistance;
import org.slf4j.Logger;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.Command;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.SerialPort.SerialPortManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HardwareConfig {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(HardwareConfig.class);
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final Map<String, DigitalInput> activeInputs = new HashMap<>();
    private static final long DEBOUNCE_TIME_MS = 250;

    public static void assignCommandsToGPIO(Context pi4j, List<Command<?>> commandsList) {
        if (!isRunningOnPi()) {
            LOGGER.warn("Running on non-ARM architecture ({}). Skipping GPIO initialization.", System.getProperty("os.arch"));
            return;
        }

        clearAllHardwareInputs(pi4j);

        for (Command<?> command : commandsList) {
            String gpioPin = command.getGPIOPin();

            if (gpioPin != null && !gpioPin.trim().isEmpty()) {
                try {
                    int bcmPin = Integer.parseInt(gpioPin.trim());
                    String triggerKey = command.getCommandTriggerKey();

                    if (activeInputs.containsKey(triggerKey)) {
                        LOGGER.info("Input for key '{}' already configured. Skipping.", triggerKey);
                        continue;
                    }

                    LOGGER.info("Assigning command '{}' to GPIO pin '{}'", triggerKey, bcmPin);

                    var pi4jConfig = DigitalInput.newConfigBuilder(pi4j)
                            .id(triggerKey)
                            .bcm(bcmPin)
                            .pull(PullResistance.PULL_UP)
                            .debounce(DEBOUNCE_TIME_MS * 1000L);

                    DigitalInput digitalInput = pi4j.create(pi4jConfig);

                    digitalInput.addListener(event -> {
                        if (event.state().isLow()) {
                            LOGGER.info("GPIO pin '{}' triggered command '{}'", bcmPin, triggerKey);
                            executorService.execute(() -> SerialPortManager.getInstance().write(command));
                        }
                    });

                    activeInputs.put(triggerKey, digitalInput);

                } catch (NumberFormatException e) {
                    LOGGER.error("Incorrect GPIO format {} for command {}", gpioPin, command.getCommandTriggerKey());
                } catch (Exception e) {
                    LOGGER.error("Failed to provision GPIO pin {} for command {}: {}", gpioPin, command.getCommandTriggerKey(), e.getMessage());
                }
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

    private static boolean isRunningOnPi() {
        String osArch = System.getProperty("os.arch").toLowerCase();
        return osArch.contains("arm") || osArch.contains("aarch64");
    }
}
