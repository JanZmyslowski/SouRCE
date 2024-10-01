package pl.edu.pwr.pwrinspace.poliwrocket.Model.Command;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class Command<T> implements ICommand {

    @Expose
    protected T value;

    @Expose
    private boolean isFinal = false;

    @Expose
    private String trigger;

    @Expose
    private String description = "";

    @Expose
    protected String payload = "";

    @Expose
    private List<String> destinationControllerNames = new ArrayList<>();

    @Override
    public String toString() {
        return trigger;
    }

    @Override
    public int hashCode() {
        return trigger.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String getPayload() {
        return payload;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public String getCommandTriggerKey() {
        return trigger;
    }

    @Override
    public String getCommandDescription() {
        return description;
    }

    @Override
    public byte[] getCommandValueAsBytes(boolean force) {
        return getCommandValueAsString().getBytes();
    }

    @Override
    public byte[] getCommandValueAsBytes() {
        return getCommandValueAsBytes(false);
    }

    public List<String> getDestinationControllerNames() {
        return destinationControllerNames;
    }

    public void setDestinationControllerNames(List<String> destinationControllerNames) {
        this.destinationControllerNames = destinationControllerNames;
    }
    @Override
    public String getCommandValueAsString() {
        return value.toString() + payload;
    }
}
