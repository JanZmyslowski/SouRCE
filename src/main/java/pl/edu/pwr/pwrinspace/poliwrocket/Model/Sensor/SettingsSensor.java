package pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor;

import com.google.gson.annotations.Expose;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.Command;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.CommandType;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.ICommand;

public class SettingsSensor extends Sensor implements ISettingsSensor, ICommand {
    @Expose
    private Double defaultValue = 0.0;
    @Expose
    private Command<?> command;
    private static final String _destinationCommandPrefix = "button";
    private static final String _destinationInoutPrefix = "button";

    @Override
    public Double getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getInputKey() {
        return _destinationInoutPrefix + this.getDestination();
    }

    @Override
    public Command<?> getCommand() {
        return command;
    }

    @Override
    public String getCommandValueAsString() {
        return command.getCommandValueAsString();
    }

    @Override
    public void setPayload(String payload) {
        command.setPayload(payload);
    }

    @Override
    public String getPayload() {
        return command.getPayload();
    }

    @Override
    public boolean isFinal() {
        return command.isFinal();
    }

    @Override
    public String getCommandTriggerKey() {
        return command.getCommandTriggerKey();
    }

    @Override
    public String getCommandDescription() {
        return command.getCommandDescription();
    }

    @Override
    public byte[] getCommandValueAsBytes(boolean force) {
        return command.getCommandValueAsBytes(force);
    }

    @Override
    public byte[] getCommandValueAsBytes() {
        return command.getCommandValueAsBytes();
    }

    @Override
    public CommandType getCommandType() {
        return command.getCommandType();
    }

    @Override
    public String getGPIOPin() {
        return command.getGPIOPin();
    }
}
