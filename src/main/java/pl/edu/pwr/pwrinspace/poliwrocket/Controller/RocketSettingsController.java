package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.ICommand;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration.Configuration;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.ISensor;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.ISettingsSensor;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.SettingsSensor;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.SerialPort.SerialPortManager;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.UI.UIThreadManager;

import java.util.HashMap;

public class RocketSettingsController extends BaseButtonSensorController {
    @FXML
    private AnchorPane mainPanel;

    protected final HashMap<String, JFXTextField> inputHashMap = new HashMap<>();
    protected final HashMap<String, Label> valueHashMap = new HashMap<>();

    @Override
    protected void buildVisualizationMap() {
        mainPanel.getChildren().removeIf(node ->
                    labelHashMap.containsValue(node)
                   || valueHashMap.containsValue(node)
                   || inputHashMap.containsValue(node)
                   || buttonHashMap.containsValue(node)
        );

        tileHashMap.clear();
        indicatorHashMap.clear();
        labelHashMap.clear();
        inputHashMap.clear();
        valueHashMap.clear();

        int initYLabel = 39;
        int initYInput = 35;
        int offsetY = 40;

        for (ISensor sensor : Configuration.getInstance().sensorRepository.getAllBasicSensors().values()) {
            if (sensor instanceof SettingsSensor) {
                var settingsSensor = (SettingsSensor) sensor;
                Label label = new Label(settingsSensor.getName());
                Label value = new Label();

                Double defaultValue = settingsSensor.getDefaultValue();
                JFXTextField input = new JFXTextField();

                if (defaultValue == null) {
                    input.setVisible(false);
                } else {
                    input.setText(Double.toString(defaultValue));
                    input.setDisable(settingsSensor.isFinal());
                }

                JFXButton button = new JFXButton("Send");

                label.setLayoutX(14);
                label.setLayoutY(initYLabel);
                label.setPrefHeight(18);
                label.setPrefWidth(150);

                value.setId(settingsSensor.getDestination());
                value.setLayoutX(112);
                value.setLayoutY(initYLabel);
                value.setPrefHeight(18);
                value.setPrefWidth(70);

                input.setLayoutX(215);
                input.setLayoutY(initYInput);
                input.setPrefHeight(26);
                input.setPrefWidth(70);

                button.setId(settingsSensor.getCommandTriggerKey());
                button.setLayoutX(305);
                button.setLayoutY(initYInput);
                button.setPrefHeight(26);
                button.setPrefWidth(60);

                mainPanel.getChildren().add(label);
                mainPanel.getChildren().add(value);
                mainPanel.getChildren().add(input);
                mainPanel.getChildren().add(button);

                labelHashMap.put(settingsSensor.getDestination(), label);
                inputHashMap.put(settingsSensor.getInputKey(), input);
                valueHashMap.put(settingsSensor.getDestination(), value);
                buttonHashMap.put(settingsSensor.getCommandTriggerKey(), button);

                initYLabel += offsetY;
                initYInput += offsetY;
            }
        }
    }

    protected void setUIBySensors() {}

    protected EventHandler<ActionEvent> handleButtonsClickByCommand(Button button, ICommand command) {
        return actionEvent -> executorService.execute(() -> {
            var input = inputHashMap.get(((ISettingsSensor)command).getInputKey());
            if(input.isVisible())
                command.setPayload(input.getText());
            SerialPortManager.getInstance().write(command);
            String comStr = command.getCommandValueAsString();

            if (comStr.contains("0x10")) {
                SerialPortManager.getInstance().writeWithoutCRC("GS;FREQ;" + command.getPayload());
            }
        });
    }

    @Override
    public void invalidated(Observable observable) {
        try {
            if(observable instanceof SettingsSensor) {
                var sensor = (SettingsSensor) observable;
                UIThreadManager.getInstance().addImmediateOnOK(() -> valueHashMap.get(sensor.getDestination()).setText(Double.toString(sensor.getValue())));
            } else {
                logger.error("Controller has been notified but there was no action.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
