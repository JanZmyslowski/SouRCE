package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import com.jfoenix.controls.JFXButton;
import eu.hansolo.tilesfx.addons.Switch;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.ICommand;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.SerialPort.SerialPortManager;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.ThreadName;

public class AbortController extends BaseButtonSensorController {

    @FXML
    private Switch safeSwitch1;
    @FXML
    private Switch safeSwitch2;
    @FXML
    protected JFXButton abortButton;

    @FXML
    @Override
    protected void initialize() {
        super.initialize();

        Platform.runLater(this::buildVisualizationMap);

        abortButton.setDisable(true);
        safeSwitch1.setOnMouseClicked(actionEvent -> abortButton.setDisable(!(safeSwitch1.isActive() && safeSwitch2.isActive())));
        safeSwitch2.setOnMouseClicked(actionEvent -> abortButton.setDisable(!(safeSwitch1.isActive() && safeSwitch2.isActive())));
    }

    @Override
    protected EventHandler<ActionEvent> handleButtonsClickByCommand(ICommand command) {
        return actionEvent -> {
            executorService.execute(() -> {
                if (safeSwitch1.isActive() && safeSwitch2.isActive()) {
                    SerialPortManager.getInstance().write(command);
                    for (Thread thread : Thread.getAllStackTraces().keySet()) {
                        if (thread.getName().equals(ThreadName.COUNTDOWN.getName())) {
                            thread.interrupt();
                        }
                    }
                }
            });
        };
    }


    @Override
    public void invalidated(Observable observable) throws UnsupportedOperationException {
        //there is no field to display sensor data
        throw new UnsupportedOperationException();
    }

    @Override
    protected void setUIBySensors() throws UnsupportedOperationException {
        //there is no field to display sensor data
        throw new UnsupportedOperationException();
    }
}