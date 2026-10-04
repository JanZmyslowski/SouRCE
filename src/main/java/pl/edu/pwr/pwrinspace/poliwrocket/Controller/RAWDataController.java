package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import com.jfoenix.controls.JFXTextArea;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration.Configuration;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.MessageParser.IMessageParser;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.CodeInterpreterUIHint;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.SerialPort.ISerialPortManager;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.SerialPort.SerialPortManager;
import pl.edu.pwr.pwrinspace.poliwrocket.Provider.UITaskProvider;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.UI.UIThreadManager;

import java.time.Instant;

public class RAWDataController extends BaseSensorController {

    @FXML
    private JFXTextArea rawDataPanel;

    @FXML
    private Label lastMessageField;

    private Instant lastMessageTime = Instant.now();
    @FXML
    protected void initialize() {
        SerialPortManager.getInstance().addPortStatusListener(this);

        UITaskProvider.getInstance().registerTask(() -> {
            if (SerialPortManager.getInstance().isPortOpen()) {
                var sinceLastMsg = Instant.now().getEpochSecond() - lastMessageTime.getEpochSecond();
                lastMessageField.setText(String.valueOf(sinceLastMsg));
                lastMessageField.setTextFill(UIHelper.resolveUIHintColor(getColor(sinceLastMsg)));
            }
        });
    }

    private CodeInterpreterUIHint getColor(long timeSinceLastMsg){
        if(timeSinceLastMsg < 15) {
            return CodeInterpreterUIHint.INFO;
        }

        if(timeSinceLastMsg < 30) {
            return CodeInterpreterUIHint.WARNING;
        }

        return CodeInterpreterUIHint.ERROR;
    }

    @Override
    protected void setUIBySensors() {
        //there is now field to display sensor data
        throw new UnsupportedOperationException();
    }

    @Override
    protected void buildVisualizationMap() {
        //there is now field to display sensor data
        throw new UnsupportedOperationException();
    }

    @Override
    public void invalidated(Observable observable) {
        try {
            if(observable instanceof IMessageParser) {
                String rawData = Configuration.getInstance().sensorRepository.getSensorKeysAndValues();
                UIThreadManager.getInstance().addNormal(() -> {
                    lastMessageTime = Instant.now();
                    double pos = rawDataPanel.getScrollTop();
                    int anchor = rawDataPanel.getAnchor();
                    int caret = rawDataPanel.getCaretPosition();
                    rawDataPanel.setText(rawData);
                    rawDataPanel.setScrollTop(pos);
                    rawDataPanel.selectRange(anchor, caret);
                });
            } else if(observable instanceof ISerialPortManager) {
                if(SerialPortManager.getInstance().isPortOpen()) {
                    lastMessageTime = Instant.now();
                }
            } else {
                logger.error("Controller has been notified but there was no action.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
