package pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import org.javatuples.KeyValue;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pwr.pwrinspace.poliwrocket.Controller.BasicButtonSensorController;
import pl.edu.pwr.pwrinspace.poliwrocket.Controller.BasicController;
import pl.edu.pwr.pwrinspace.poliwrocket.Controller.BasicSensorController;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.Command;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.ICommand;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.MessageParser.MessageParserEnum;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Notification.Schedule;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Protobuf.ProtobufDeviceRepository;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Protobuf.ProtobufSystemRepository;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.*;

import java.time.Instant;
import java.util.*;

public class Configuration implements Observable {

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
    public List<InvalidationListener> observers = new ArrayList<>();

    public int FPS = 10;

    public int AVERAGING_PERIOD = 1000;

    public int BUFFER_SIZE;

    public double START_POSITION_LAT = 49.013517;

    public double START_POSITION_LON = 8.404435;
    private boolean forceCommandsActive = false;

    public MessageParserEnum PARSER_TYPE = MessageParserEnum.STANDARD;

    protected static String CONFIG_PATH = "./config/";

    public static final String CONFIG_FILE_NAME = "config.json";

    public static final String FLIGHT_DATA_PATH = "./flightData/";

    public static final String FLIGHT_DATA_FILE_NAME = "Flight_" + Instant.now().getEpochSecond() + ".txt";

    public String DISCORD_TOKEN = "";

    public String DISCORD_CHANNEL_NAME = "";

    public String FRAME_DELIMITER = ",";

    public Map<String,List<String>> FRAME_PATTERN = new HashMap<>();

    public String MSG_PREFIX = "";

    public List<Command> commandsList = new LinkedList<>();

    public List<Schedule> notificationSchedule = new LinkedList<>();

    public List<String> notificationMessageKeys = new LinkedList<>();

    public SensorRepository sensorRepository = new SensorRepository();

    public InterpreterRepository interpreterRepository = new InterpreterRepository();
    public ProtobufSystemRepository protobufSystemRepository = new ProtobufSystemRepository();
    public ProtobufDeviceRepository protobufDeviceRepository = new ProtobufDeviceRepository();

    public Collection<BasicController> controllersList = new LinkedList<>();

    public final static Instant startUpTime = Instant.now();

    private Configuration() {
        if (Holder.INSTANCE != null) {
            throw new IllegalStateException("Singleton already constructed");
        }
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
        copyModelProperties(config);
        addSensorsToRepository(config);
        validateFrameAndRepository();
        setupSensorsAsListeners(config);
        setupSensorsInterpreters(config);
    }

    private void setupSensorsInterpreters(ConfigurationSaveModel config) {
        config.sensorRepository.getAllBasicSensors().forEach((s, sensor) -> {
            if(sensor.getInterpreterKey() != null && !sensor.getInterpreterKey().isEmpty()) {
                sensor.setInterpreter(interpreterRepository.getInterpreter(sensor.getInterpreterKey()));
            }
        });
    }

    private void validateFrameAndRepository() {
        FRAME_PATTERN.forEach((frameKey,pattern) -> {
            pattern.forEach(key -> {
                try {
                    sensorRepository.getSensorByName(key);
                } catch (NullPointerException e) {
                    logger.info("Sensor {} in frame {} is not configured in repository and will be automatically added", key, frameKey);
                    sensorRepository.addSensor(new Sensor(key));
                }
            });
        });
    }

    private void copyModelProperties(ConfigurationSaveModel config) {
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
        this.protobufDeviceRepository = config.protobufDeviceRepository;
        this.protobufSystemRepository = config.protobufSystemRepository;
        this.notificationMessageKeys = config.notificationMessageKeys;
        this.notificationSchedule = config.notificationSchedule;
    }

    private void addSensorsToRepository(ConfigurationSaveModel config) {
        if(config.PARSER_TYPE == MessageParserEnum.PROTOBUF || config.FRAME_PATTERN.values().stream().anyMatch(l -> l.contains(this.sensorRepository.getGpsSensor().getLatitude().getName()))){
            this.sensorRepository.addSensor(this.sensorRepository.getGpsSensor().getLatitude());
        }
        if(config.PARSER_TYPE == MessageParserEnum.PROTOBUF || config.FRAME_PATTERN.values().stream().anyMatch(l -> l.contains(this.sensorRepository.getGpsSensor().getLongitude().getName()))){
            this.sensorRepository.addSensor(this.sensorRepository.getGpsSensor().getLongitude());
        }
        if(config.PARSER_TYPE == MessageParserEnum.PROTOBUF || config.FRAME_PATTERN.values().stream().anyMatch(l -> l.contains(this.sensorRepository.getGyroSensor().getAxis_x().getName()))){
            this.sensorRepository.addSensor(this.sensorRepository.getGyroSensor().getAxis_x());
        }
        if(config.PARSER_TYPE == MessageParserEnum.PROTOBUF || config.FRAME_PATTERN.values().stream().anyMatch(l -> l.contains(this.sensorRepository.getGyroSensor().getAxis_y().getName()))){
            this.sensorRepository.addSensor(this.sensorRepository.getGyroSensor().getAxis_y());
        }
        if(config.PARSER_TYPE == MessageParserEnum.PROTOBUF || config.FRAME_PATTERN.values().stream().anyMatch(l -> l.contains(this.sensorRepository.getGyroSensor().getAxis_z().getName()))){
            this.sensorRepository.addSensor(this.sensorRepository.getGyroSensor().getAxis_z());
        }

        var basicSensors = this.sensorRepository.getAllBasicSensors().values().toArray();

        Arrays.stream(basicSensors).filter(s -> s instanceof FillingLevelSensor).forEach(s -> {
            var sensor = (FillingLevelSensor)s;
            this.sensorRepository.addSensor(sensor.getHallSensor1());
            this.sensorRepository.addSensor(sensor.getHallSensor2());
            this.sensorRepository.addSensor(sensor.getHallSensor3());
            this.sensorRepository.addSensor(sensor.getHallSensor4());
            this.sensorRepository.addSensor(sensor.getHallSensor5());
        });

        Arrays.stream(basicSensors).filter(ISensorsWrapper.class::isInstance).forEach(s -> {
            for (Sensor innerSensor: ((ISensorsWrapper) s).getSensors()) {
                if(!innerSensor.getName().isEmpty())
                    this.sensorRepository.addSensor(innerSensor);
            }
        });

        Arrays.stream(basicSensors).filter(CompositeBitSensor.class::isInstance).forEach(s -> {
            List<KeyValue<String,Sensor>> sensorList = new LinkedList<>();
            var composite = ((CompositeBitSensor) s);
            this.sensorRepository.getAllBasicSensors().forEach((k,v) -> {
                if(Arrays.asList(composite.getSensorsKeys()).contains(k)) {
                    sensorList.add(new KeyValue<>(k,v));
                }
            });
            try {
                composite.injectSensors(sensorList);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    private void setupSensorsAsListeners(ConfigurationSaveModel config) {
        Arrays.stream(this.sensorRepository.getAllBasicSensors().values().toArray())
                .filter(IFieldsObserver.class::isInstance)
                .forEach(s -> ((IFieldsObserver) s).observeFields());

        this.sensorRepository.getGyroSensor().observeFields();
        this.sensorRepository.getGpsSensor().observeFields();
    }

    public void setupApplicationConfig(Collection<BasicController> controllersList) {
        this.controllersList = controllersList;
        List<Triplet<BasicController, List<ISensor>, List<ICommand>>> controllersConfig = new ArrayList<>();
        controllersList.forEach(basicController -> controllersConfig.add(new Triplet<>(basicController,new ArrayList<>(),new ArrayList<>())));

        for (int i = 0; i < controllersConfig.size(); i++) {

            String controllerName = controllersConfig.get(i).getValue0().getControllerName();
            if (Configuration.getInstance().sensorRepository.getGpsSensor().getDestinationControllerNames().contains(controllerName)) {
                Configuration.getInstance().sensorRepository.getGpsSensor().addListener(controllersConfig.get(i).getValue0());
            }
            if (Configuration.getInstance().sensorRepository.getGyroSensor().getDestinationControllerNames().contains(controllerName)) {
                Configuration.getInstance().sensorRepository.getGyroSensor().addListener(controllersConfig.get(i).getValue0());
            }
            int finalI = i;
            Configuration.getInstance().sensorRepository.getAllBasicSensors().values().forEach(s -> {
                if (s.getDestinationControllerNames().contains(controllerName) && s.getDestination() != null && !s.getDestination().isEmpty()) {
                    s.addListener(controllersConfig.get(finalI).getValue0());
                    controllersConfig.get(finalI).getValue1().add(s);

                    if (s instanceof SettingsSensor) {
                        controllersConfig.get(finalI).getValue2().add((SettingsSensor)s);
                    }
                }
            });

            for (int j = 0; j < Configuration.getInstance().commandsList.size(); j++) {
                if (Configuration.getInstance().commandsList.get(j).getDestinationControllerNames().contains(controllersConfig.get(i).getValue0().getControllerName())) {
                    controllersConfig.get(i).getValue2().add(Configuration.getInstance().commandsList.get(j));
                }
            }
        }

        for (Triplet<BasicController, List<ISensor>, List<ICommand>> objects : controllersConfig) {
/*            for (Method method : objects.getValue0().getClass().getMethods()) {
                try {
                    if(method.getName().equals("injectSensorsModels")) {
                        method.invoke(objects.getValue0(), objects.getValue1());
                    } else if(method.getName().equals("assignsCommands")) {
                        method.invoke(objects.getValue0(), objects.getValue2());
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }*/
            if (!objects.getValue1().isEmpty() && objects.getValue0() instanceof BasicSensorController) {
                ((BasicSensorController) objects.getValue0()).injectSensorsModels(objects.getValue1());
            }

            if (!objects.getValue2().isEmpty() && objects.getValue0() instanceof BasicButtonSensorController) {
                ((BasicButtonSensorController) objects.getValue0()).assignsCommands(objects.getValue2());
            }

        }
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
