package pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration;

import com.google.gson.annotations.Expose;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.BaseSaveModel;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.InterpreterRepository;

public class InterpretersConfig extends BaseSaveModel {
    @Expose public InterpreterRepository interpreterRepository = new InterpreterRepository();

    public InterpretersConfig() {
        super(Configuration.CONFIG_PATH, "InterpreterConfig.json");
    }
}