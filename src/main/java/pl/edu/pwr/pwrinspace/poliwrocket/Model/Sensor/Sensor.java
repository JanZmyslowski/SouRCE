package pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor;

import com.google.gson.annotations.Expose;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import org.jetbrains.annotations.NotNull;
import pl.edu.pwr.pwrinspace.poliwrocket.Event.IUIUpdateEventListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Sensor implements Observable, ISensor, IUIUpdateEventListener {

    public List<InvalidationListener> observers = new ArrayList<>();

    @Expose
    private String destination = "";
    @Expose
    private String name = "";
    @Expose
    private String unit = "";
    @Expose
    private List<String> destinationControllerNames = new ArrayList<>();
    @Expose
    private double maxRange = 0;
    @Expose
    private double minRange = 0;
    @Expose
    private boolean isBoolean = false;
    @Expose
    private String interpreterKey;
    @Expose
    private boolean hidden = false;
    @Expose
    private int precision = 1;

    protected double value = 0;

    private CodeInterpreter interpreter;

    private List<Double> values = new LinkedList<>();

    private Instant timeStamp;
    private Instant previousTimeStamp;
    private Instant lastAveragingTimeStamp = Instant.now();
    private boolean shouldNotify = false;
    private double previousReportedValue = 0;
    private double maxValue = -10.0;

    public Sensor() {
        this.timeStamp = Instant.now();
    }

    public Sensor(@NotNull Sensor sensor) {
        this.destination = sensor.destination;
        this.unit = sensor.unit;
        this.name = sensor.name;
        this.minRange = sensor.minRange;
        this.maxRange = sensor.maxRange;
        this.value = sensor.value;
        this.timeStamp = sensor.timeStamp;
    }

    public Sensor(String name) {
        this.timeStamp = Instant.now();
        this.name = name;
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        observers.add(invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        observers.remove(invalidationListener);
    }

    @Override
    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public String getDestination() {
        return destination;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    @Override
    public InterpreterValue getCodeMeaning() {
        if (interpreter == null) {
           throw new RuntimeException("Interpreter not set");
        }

        return interpreter.getCodeMeaning((int) value);
    }

    @Override
    public void setInterpreter(CodeInterpreter interpreter) {
        this.interpreter = interpreter;
        isBoolean = true;
    }

    @Override
    public String getInterpreterKey() {
        return interpreterKey;
    }

    @Override
    public boolean hasInterpreter() {
        return interpreter != null;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    protected void notifyObserver() {
        for (InvalidationListener obs : observers) {
            if (shouldNotify || obs instanceof Observable) {
                obs.invalidated(this);
            }
        }
        shouldNotify = false;
    }

    //WARNING: tmp turn off averaging
    @Override
    public void setValue(double newValue) {
        this.values.add(newValue);
        Instant currentTimeStamp = Instant.now();
 //       long currentTime = currentTimeStamp.toEpochMilli();

//        if (isBoolean || currentTime - this.timeStamp.toEpochMilli() >=  1000) {
//            this.previousReportedValue = this.value;
//            this.value = newValue;
//            this.shouldNotify = true;
//        } else if (currentTime - this.lastAveragingTimeStamp.toEpochMilli() >= Configuration.getInstance().AVERAGING_PERIOD) {
//            this.previousReportedValue = this.value;
//            this.value = this.getAverage();
//            this.lastAveragingTimeStamp = currentTimeStamp;
//            this.values.clear();
//            this.shouldNotify = true;
//        } else {
//            this.value = newValue;
//        }

        this.previousReportedValue = this.value;
        this.value = newValue;
        this.shouldNotify = true;

        if(this.value > this.maxValue) {
            this.maxValue = this.value;
        }

        //protect overflow
//        if(this.values.size() > 500) {
//            this.values = new ArrayList<>();
//            this.values.add(this.previousReportedValue);
//            this.values.add(this.value);
//        }
        this.previousTimeStamp = this.timeStamp;
        this.timeStamp = currentTimeStamp;
        notifyObserver();
    }

    private double getAverage() {
        double sum = 0;
        for (Double val : this.values) {
            sum += val;
        }
        return Math.round((sum / this.values.size())*100.0)/100.0;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUnit() {
        return unit;
    }

    @Override
    public double getMaxRange() {
        return maxRange;
    }

    @Override
    public double getMinRange() {
        return minRange;
    }

    @Override
    public double getValue() {
        return value / precision;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public List<String> getDestinationControllerNames() {
        return destinationControllerNames;
    }

    public void setDestinationControllerNames(List<String> destinationControllerNames) {
        this.destinationControllerNames = destinationControllerNames;
    }

    public void setMaxRange(double maxRange) {
        this.maxRange = maxRange;
    }

    public void setMinRange(double minRange) {
        this.minRange = minRange;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public boolean isBoolean() {
        return isBoolean;
    }

    public void setBoolean(boolean aBoolean) {
        isBoolean = aBoolean;
    }

    public double getPreviousValue() {
        return previousReportedValue;
    }

    @Override
    public void onUIUpdateEvent() {
        notifyObserver();
    }
}
