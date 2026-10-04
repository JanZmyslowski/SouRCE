package pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor;

import com.google.gson.annotations.Expose;

public class InterpreterValue {
    @Expose public String text;
    @Expose public CodeInterpreterUIHint UIHint;

    public InterpreterValue() {
    }

    public InterpreterValue(int text, CodeInterpreterUIHint UIHint) {
        this.text = Integer.toString(text);
        this.UIHint = UIHint;
    }

}
