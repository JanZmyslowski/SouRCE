package pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor;

import com.google.gson.annotations.Expose;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import org.javatuples.KeyValue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CompositeBitSensor extends Sensor implements InvalidationListener {

    @Expose
    private String[] sensorsKeys;
    @Expose
    private int divider = 1;
    @Expose
    private boolean signed = false;
    private Sensor[] sensors;
    private int sensorsUpdates = 0;

    public CompositeBitSensor() {
        super();
        this.setBoolean(true);
    }

    public void injectSensors(List<KeyValue<String,Sensor>> sensors) throws Exception {
        if(sensors.size() != sensorsKeys.length || sensors.stream().anyMatch(s -> !Arrays.asList(sensorsKeys).contains(s.getKey()))) {
            throw new Exception("Wrong sensor key in " + this.getName());
        }
        this.sensors = new Sensor[sensorsKeys.length];
        sensors.stream()
                .sorted(Comparator.comparingInt(s -> Arrays.asList(this.sensorsKeys).indexOf(s.getKey())))
                .map(KeyValue::getValue)
                .collect(Collectors.toList())
                .toArray(this.sensors);

        this.sensorsUpdates = 0;

        for (Sensor sensor : this.sensors) {
            sensor.addListener(this);
        }
    }

    public String[] getSensorsKeys() {
        return sensorsKeys;
    }

    public Sensor[] getSensors() {
        return sensors;
    }

    @Override
    public void invalidated(Observable observable) {
        this.sensorsUpdates++;

        if (sensorsUpdates == sensorsKeys.length) {
            this.sensorsUpdates = 0;
            StringBuilder binaryString = new StringBuilder();
            for (Sensor sensor : sensors) {
                binaryString.append((int) sensor.getValue());
            }

            int val;
            if (signed) {
                int shift = 32 - binaryString.length();
                val = Integer.parseInt(binaryString.toString(), 2) << shift >> shift;
            } else {
                val = Integer.parseInt(binaryString.toString(), 2);
            }

            assert divider != 0;
            this.setValue((double) val / divider);
        }
    }
}
