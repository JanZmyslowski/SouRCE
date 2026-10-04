package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import com.jfoenix.controls.JFXButton;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.SerialPort.SerialPortManager;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;


public class SerialPortMonitorController extends BaseDataTilesController {

    @FXML
    private TextField serialPortMonitor;

    @FXML
    private JFXButton sendButton;

    @FXML
    public void initialize() {
        sendButton.setOnAction(event -> {
            String serialMessage = serialPortMonitor.getText();
            SerialPortManager.getInstance().writeWithoutCRC(serialMessage);
        });
    }
}

