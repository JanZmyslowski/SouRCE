package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import javafx.scene.paint.Color;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.CodeInterpreterUIHint;

public class UIHelper {

    public static Color resolveUIHintColor(CodeInterpreterUIHint uiHint) {
        switch (uiHint) {
            case WARNING:
                return Color.ORANGERED;
            case ERROR:
                return Color.RED;
            case SUCCESS:
                return Color.GREENYELLOW;
            case OPEN:
                return Color.BLUE;
            case CLOSE:
                return Color.TRANSPARENT;
            case INFO:
            default:
                return Color.WHITE;
        }
    }
}