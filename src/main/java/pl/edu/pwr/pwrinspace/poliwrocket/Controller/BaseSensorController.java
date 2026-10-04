package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import javafx.application.Platform;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.ISensor;

import java.lang.reflect.Field;
import java.util.*;

public abstract class BaseSensorController extends BaseController {

    protected HashSet<ISensor> sensors = new HashSet<>();

    public final void injectSensorsModels(Collection<ISensor> sensors) {
        this.sensors.clear();
        this.sensors.addAll(sensors);
        Platform.runLater(this::buildUI);
    }

    private void buildUI() {
        buildVisualizationMap();
        setUIBySensors();
    }

    protected abstract void buildVisualizationMap();

    protected abstract void setUIBySensors();

    protected static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }
}
