package pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration;

import com.google.gson.annotations.Expose;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.BaseSaveModel;
import java.util.HashMap;
import java.util.Map;

public class SpeechConfig extends BaseSaveModel {
    @Expose public Map<String, Object> speechRules = new HashMap<>();

    public SpeechConfig() {
        super(Configuration.CONFIG_PATH, "speechConfig.json");
    }
}