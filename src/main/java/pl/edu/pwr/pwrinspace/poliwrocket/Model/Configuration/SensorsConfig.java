package pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration;

import com.google.gson.annotations.Expose;
import org.javatuples.KeyValue;
import org.slf4j.Logger;
import pl.edu.pwr.pwrinspace.poliwrocket.Controller.BaseController;
import pl.edu.pwr.pwrinspace.poliwrocket.Controller.BaseSensorController;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.BaseSaveModel;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.MessageParser.MessageParserEnum;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.*;
import java.util.*;

public class SensorsConfig extends BaseSaveModel {

    @Expose
    public SensorRepository sensorRepository = new SensorRepository();

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SensorsConfig.class);

    public SensorsConfig() {
        super(Configuration.CONFIG_PATH, "SensorConfig.json");
    }

    public SensorRepository processAndGetRepository(
            MessageParserEnum parserType,
            InterpreterRepository interpreterRepository
    ) {
        if (sensorRepository == null) sensorRepository = new SensorRepository();

        addSensorsToRepository(parserType);
        setupSensorsAsListeners();
        setupSensorsInterpreters(interpreterRepository);

        return sensorRepository;
    }

    public void assignSensorsToControllers(Collection<BaseController> controllersList) {
        if (controllersList == null || sensorRepository == null) {
            return;
        }

        for (BaseController controller : controllersList) {
            String controllerName = controller.getControllerName();
            List<ISensor> assignedSensors = new ArrayList<>();

            if (sensorRepository.getGpsSensor() != null && sensorRepository.getGpsSensor().getDestinationControllerNames().contains(controllerName)) {
                sensorRepository.getGpsSensor().addListener(controller);
            }

            if (sensorRepository.getGyroSensor() != null && sensorRepository.getGyroSensor().getDestinationControllerNames().contains(controllerName)) {
                sensorRepository.getGyroSensor().addListener(controller);
            }

            sensorRepository.getAllBasicSensors().values().forEach(sensor -> {
                if (sensor.getDestinationControllerNames().contains(controllerName) && sensor.getDestination() != null && !sensor.getDestination().isEmpty()) {
                    sensor.addListener(controller);
                    assignedSensors.add(sensor);
                }
            });

            if (!assignedSensors.isEmpty() && controller instanceof BaseSensorController sensorController) {
                sensorController.injectSensorsModels(assignedSensors);
            }
        }
    }

    private void addSensorsToRepository(MessageParserEnum parserType) {
        if (parserType == MessageParserEnum.PROTOBUF) {
            this.sensorRepository.addSensor(this.sensorRepository.getGpsSensor().getLatitude());
            this.sensorRepository.addSensor(this.sensorRepository.getGpsSensor().getLongitude());
            this.sensorRepository.addSensor(this.sensorRepository.getGyroSensor().getAxis_x());
            this.sensorRepository.addSensor(this.sensorRepository.getGyroSensor().getAxis_y());
            this.sensorRepository.addSensor(this.sensorRepository.getGyroSensor().getAxis_z());
        }

        var basicSensors = this.sensorRepository.getAllBasicSensors().values().toArray();

        Arrays.stream(basicSensors).filter(ISensorsWrapper.class::isInstance).forEach(s -> {
            for (Sensor innerSensor : ((ISensorsWrapper) s).getSensors()) {
                if (!innerSensor.getName().isEmpty())
                    this.sensorRepository.addSensor(innerSensor);
            }
        });

        Arrays.stream(basicSensors).filter(CompositeBitSensor.class::isInstance).forEach(s -> {
            List<KeyValue<String, Sensor>> sensorList = new LinkedList<>();
            var composite = ((CompositeBitSensor) s);
            this.sensorRepository.getAllBasicSensors().forEach((k, v) -> {
                if (Arrays.asList(composite.getSensorsKeys()).contains(k)) {
                    sensorList.add(new KeyValue<>(k, v));
                }
            });
            try {
                composite.injectSensors(sensorList);
            } catch (Exception e) {
                LOGGER.error("Error injecting sensors into composite sensor: {}", e.getMessage());
            }
        });
    }

    private void setupSensorsAsListeners() {
        Arrays.stream(this.sensorRepository.getAllBasicSensors().values().toArray())
                .filter(IFieldsObserver.class::isInstance)
                .forEach(s -> ((IFieldsObserver) s).observeFields());

        if (this.sensorRepository.getGyroSensor() != null) {
            this.sensorRepository.getGyroSensor().observeFields();
        }
        if (this.sensorRepository.getGpsSensor() != null) {
            this.sensorRepository.getGpsSensor().observeFields();
        }
    }

    private void setupSensorsInterpreters(InterpreterRepository interpreterRepository) {
        if (interpreterRepository == null) return;
        this.sensorRepository.getAllBasicSensors().forEach((s, sensor) -> {
            if (sensor.getInterpreterKey() != null && !sensor.getInterpreterKey().isEmpty()) {
                sensor.setInterpreter(interpreterRepository.getInterpreter(sensor.getInterpreterKey()));
            }
        });
    }
}