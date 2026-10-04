package pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor;

import com.google.gson.annotations.Expose;

import java.util.HashMap;

public class CodeInterpreter {
    @Expose private HashMap<Integer, InterpreterValue> interpreters;

    public InterpreterValue getCodeMeaning(int code) {
        return interpreters.getOrDefault(code,  new InterpreterValue(code, CodeInterpreterUIHint.INFO));
    }
}
