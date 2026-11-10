package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import com.jfoenix.controls.JFXButton;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration.HardwareConfig;

public class FanControlController extends BaseController{
    @FXML private JFXButton fanOnButton;
    @FXML private JFXButton fanOffButton;

    @FXML
    private void handleFanOnButtonClick() {
        if (HardwareConfig.isRunningOnPi()) {
            for (int pin : HardwareConfig.FAN_PINS) {
                var fanOutput = HardwareConfig.getFanOutput(pin);
                if (fanOutput != null) {
                    fanOutput.high();
                }
            }
        }
    }

    @FXML
    private void handleFanOffButtonClick() {
        if (HardwareConfig.isRunningOnPi()) {
            for (int pin : HardwareConfig.FAN_PINS) {
                var fanOutput = HardwareConfig.getFanOutput(pin);
                if (fanOutput != null) {
                    fanOutput.low();
                }
            }
        }
    }

    @Override
    public void invalidated(Observable observable) {

    }
}
