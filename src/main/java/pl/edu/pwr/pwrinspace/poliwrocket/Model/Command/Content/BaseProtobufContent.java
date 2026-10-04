package pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.Content;

import com.google.gson.annotations.Expose;

public class BaseProtobufContent {
    @Expose
    private String command;

    public String getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return "command: " + command;
    }
}
