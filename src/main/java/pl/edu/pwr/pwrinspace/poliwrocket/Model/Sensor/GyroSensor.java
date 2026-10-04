package pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor;

import com.google.gson.annotations.Expose;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import java.util.ArrayList;
import java.util.List;

public class GyroSensor implements Observable, IGyroSensor {
    @Expose private Sensor axis_x;
    @Expose private Sensor axis_y;
    @Expose private Sensor axis_z;
    @Expose private List<String> destinationControllerNames = new ArrayList<>();
    private boolean axis_z_updated = false;
    private boolean axis_x_updated = false;
    private boolean axis_y_updated = false;
    List<InvalidationListener> observers = new ArrayList<>();

    public GyroSensor(){

    }

    public GyroSensor(Sensor gryro1, Sensor gryro2, Sensor gryro3) {
        this.axis_x = gryro1;
        this.axis_y = gryro2;
        this.axis_z = gryro3;
    }

    public Sensor getAxis_x() {
        return axis_x;
    }

    public Sensor getAxis_y() {
        return axis_y;
    }

    public Sensor getAxis_z() {
        return axis_z;
    }

    @Override
    public double getX() {
        return axis_x.getValue();
    }

    @Override
    public double getY() {
        return axis_y.getValue();
    }

    @Override
    public double getZ() {
        return axis_z.getValue();
    }

    public void observeFields(){
        this.axis_x.addListener(this);
        this.axis_y.addListener(this);
        this.axis_z.addListener(this);
    }

    @Override
    public void assignSensors(Sensor axis_x, Sensor axis_y, Sensor axis_z) {
        this.axis_x = axis_x;
        this.axis_y = axis_y;
        this.axis_z = axis_z;
    }

    private void notifyObserver(){
        for (InvalidationListener obs: observers) {
            obs.invalidated(this);
        }
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
    public void invalidated(Observable observable) {
        if (observable == axis_x) {
            axis_x_updated = true;
        }

        if (observable == axis_y) {
            axis_y_updated = true;
        }

        if (observable == axis_z) {
            axis_z_updated = true;
        }

        if (axis_x_updated && axis_y_updated && axis_z_updated) {
            notifyObserver();
            axis_x_updated = false;
            axis_y_updated = false;
            axis_z_updated = false;
        }
    }

    public List<String> getDestinationControllerNames() {
        return destinationControllerNames;
    }
}
