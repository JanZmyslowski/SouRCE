package pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import pl.edu.pwr.pwrinspace.poliwrocket.Controller.BaseController;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.Command;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.MessageParser.MessageParserEnum;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Notification.Schedule;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Protobuf.ProtobufDeviceRepository;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Protobuf.ProtobufSystemRepository;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.InterpreterRepository;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.SensorRepository;

import java.time.Instant;
import java.util.*;

public class Configuration implements Observable {

    public List<InvalidationListener> observers = new ArrayList<>();

    public int FPS = 10;
    public int AVERAGING_PERIOD = 1000;
    public int BUFFER_SIZE;
    public double START_POSITION_LAT = 49.013517;
    public double START_POSITION_LON = 8.404435;
    private boolean forceCommandsActive = false;
    public MessageParserEnum PARSER_TYPE = MessageParserEnum.STANDARD;
    public String DISCORD_TOKEN = "";
    public String DISCORD_CHANNEL_NAME = "";
    public String FRAME_DELIMITER = ",";
    public Map<String,List<String>> FRAME_PATTERN = new HashMap<>();
    public String MSG_PREFIX = "";

    protected static String CONFIG_PATH = "./config/";
    public static final String CONFIG_FILE_NAME = "config.json";
    public static final String FLIGHT_DATA_PATH = "./flightData/";
    public static final String FLIGHT_DATA_FILE_NAME = "Flight_" + Instant.now().getEpochSecond() + ".txt";

    public List<Command<?>> commandsList = new LinkedList<>();
    public List<Schedule> notificationSchedule = new LinkedList<>();
    public List<String> notificationMessageKeys = new LinkedList<>();
    public SensorRepository sensorRepository = new SensorRepository();
    public InterpreterRepository interpreterRepository = new InterpreterRepository();
    public ProtobufSystemRepository protobufSystemRepository = new ProtobufSystemRepository();
    public ProtobufDeviceRepository protobufDeviceRepository = new ProtobufDeviceRepository();
    public Map<String, Object> speechRules = new HashMap<>();
    public Collection<BaseController> controllersList = new LinkedList<>();
    public final static Instant startUpTime = Instant.now();
    private boolean lightMode = false;

    private final Context pi4j = Pi4J.newAutoContext();

    private Configuration() {}

    public void setLightMode(boolean lightMode) {
        this.lightMode = lightMode;
        for (InvalidationListener listener : observers) {
            listener.invalidated(this);
        }
    }

    public boolean isLightMode() {
        return lightMode;
    }

    public void setConfigPath(String path) {
        CONFIG_PATH = path;
    }

    public static String getConfigFilesPath() {
        return CONFIG_PATH;
    }

    public static String getFlightDataFileName(String key) {
        return "Flight_" + key + "_" + startUpTime.getEpochSecond() + ".txt";
    }

    public void reloadConfigInstance(ConfigurationSaveModel config) {
        setupConfigInstance(config);
        setupApplicationConfig(this.controllersList);
    }

    public void setupConfigInstance(ConfigurationSaveModel config) {
        config.loadRemainingSplitFiles();
        
        this.FPS = config.FPS;
        this.AVERAGING_PERIOD = config.AVERAGING_PERIOD;
        this.BUFFER_SIZE = config.BUFFER_SIZE;
        this.START_POSITION_LAT = config.START_POSITION_LAT;
        this.START_POSITION_LON = config.START_POSITION_LON;
        this.PARSER_TYPE = config.PARSER_TYPE;
        this.FRAME_DELIMITER = config.FRAME_DELIMITER;
        this.FRAME_PATTERN = config.FRAME_PATTERN;
        this.DISCORD_TOKEN = config.DISCORD_TOKEN;
        this.DISCORD_CHANNEL_NAME = config.DISCORD_CHANNEL_NAME;
        this.MSG_PREFIX = config.MSG_PREFIX;
        this.commandsList = config.commandsList;
        this.sensorRepository = config.sensorRepository;
        this.interpreterRepository = config.interpreterRepository;
        this.notificationSchedule = config.notificationSchedule;
        this.notificationMessageKeys = config.notificationMessageKeys;
        this.protobufDeviceRepository = config.protobufDeviceRepository;
        this.protobufSystemRepository = config.protobufSystemRepository;
        this.speechRules = config.speechRules;

        SensorsConfig sensors = new SensorsConfig();
        sensors.sensorRepository = this.sensorRepository;
        this.sensorRepository = sensors.processAndGetRepository(
                this.PARSER_TYPE,
                this.interpreterRepository
        );
    }

    public void setupApplicationConfig(Collection<BaseController> controllersList) {
        this.controllersList = controllersList;

        SensorsConfig sensorsConfig = new SensorsConfig();
        sensorsConfig.sensorRepository = this.sensorRepository;
        sensorsConfig.assignSensorsToControllers(controllersList);

        CommandsConfig commandsConfig = new CommandsConfig();
        commandsConfig.commandsList = this.commandsList;
        commandsConfig.assignCommandsToControllers(controllersList);

        HardwareConfig.assignCommandsToGPIO(pi4j, this.commandsList);
    }

    public static Configuration getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        observers.add(invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        observers.remove(invalidationListener);
    }

    protected void notifyObserver() {
        for (InvalidationListener obs : observers) {
            obs.invalidated(this);
        }
    }

    public boolean isForceCommandsActive() {
        return forceCommandsActive;
    }

    public void setForceCommandsActive(boolean forceCommandsActive) {
        this.forceCommandsActive = forceCommandsActive;
        notifyObserver();
    }

    private static class Holder {
        private static final Configuration INSTANCE = new Configuration();
    }
}