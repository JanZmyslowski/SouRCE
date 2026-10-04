package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import eu.hansolo.tilesfx.addons.Indicator;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.ICommand;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.ISensor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TestCommandsController extends BaseButtonSensorCommandsController{
    @Override
    protected void buildVisualizationMap() {

        mainPanel.getChildren().removeIf(node ->
                labelHashMap.containsValue(node)
                        || inputHashMap.containsValue(node)
                        || buttonHashMap.containsValue(node)
                        || indicatorHashMap.containsValue(node)
        );

        tileHashMap.clear();
        indicatorHashMap.clear();
        labelHashMap.clear();
        inputHashMap.clear();
        buttonHashMap.clear();
        sensorHashMap.clear();
        openHashMap.clear();
        closeHashMap.clear();
        commandHashMap.clear();
        moduleArray.clear();

        for (ISensor sensor : sensors) {
            String sensorName = sensor.getName().toLowerCase().replaceAll(" ", "");
            sensorHashMap.put(sensorName, sensor);
            indicatorHashMap.put(sensorName, new Indicator());
            moduleArray.add(sensorName);
        }
        moduleArray.sort((String sensorName1, String sensorName2) -> {
            if (sensorName1.startsWith("oxi") && !sensorName2.startsWith("oxi")) return -1;
            if (!sensorName1.startsWith("oxi") && sensorName2.startsWith("oxi")) return 1;

            if (sensorName1.startsWith("eth") && !sensorName2.startsWith("eth")) return 1;
            if (!sensorName1.startsWith("eth") && sensorName2.startsWith("eth")) return -1;

            var sensor1 = sensorHashMap.get(sensorName1);
            var sensor2 = sensorHashMap.get(sensorName2);
            return sensor1.getDestination().compareTo(sensor2.getDestination());
        });

        List<ICommand> commandList = this.commands.stream().sorted(Comparator.comparing(ICommand::getCommandDescription)).collect(Collectors.toList());

        for (ICommand command : commandList) {
            String commandTriggerKey = command.getCommandTriggerKey().toLowerCase();
            String commandType = String.valueOf(command.getCommandType());

            Button button = new JFXButton(command.getCommandDescription());

            switch (command.getCommandType()) {
                case OPEN:
                    openHashMap.put(commandTriggerKey, button);
                    buttonHashMap.put(command.getCommandTriggerKey() + commandType, button);
                    break;
                case CLOSE:
                    closeHashMap.put(commandTriggerKey, button);
                    buttonHashMap.put(command.getCommandTriggerKey() + commandType, button);
                    break;
                case INPUT_COMMAND:
                    commandHashMap.put(commandTriggerKey, button);
                    buttonHashMap.put(command.getCommandTriggerKey() + commandType, button);
                    var input = new JFXTextField();
                    if (command.getPayload() == null) input.setVisible(false);
                    else input.setText(command.getPayload());
                    input.setDisable(command.isFinal());
                    inputHashMap.put(commandTriggerKey, input);
                    inputHashMap.put(command.getCommandTriggerKey(), input);
                    break;
            }

            if (!moduleArray.contains(commandTriggerKey)) moduleArray.add(commandTriggerKey);
        }

        int initY = this.initY;
        int initYLabel = this.initYLabel;
        int initYIndicator = this.initYIndicator;
        int offsetY = this.offestY;

        for (String module : moduleArray) {
            var sensor = sensorHashMap.get(module);
            var indicator = indicatorHashMap.get(module);
            var openButton = openHashMap.get(module);
            var closeButton = closeHashMap.get(module);
            var input = inputHashMap.get(module);
            var commandButton = commandHashMap.get(module);

            if (sensor != null) {
                var label = new Label(sensor.getName());
                label.setVisible(showLabel);
                label.setLayoutY(initYLabel);
                label.setLayoutX(labelLayoutX);
                label.setPrefHeight(labelPrefHeight);
                label.setPrefWidth(labelPrefWidth);
                labelHashMap.put(module, label);
                mainPanel.getChildren().add(label);
            }
            else if (commandButton != null) {
                var label = new Label(commandButton.getText());
                label.setVisible(showLabel);
                label.setLayoutY(initYLabel);
                label.setLayoutX(labelLayoutX);
                label.setPrefHeight(labelPrefHeight);
                label.setPrefWidth(labelPrefWidth);
                labelHashMap.put(module, label);
                mainPanel.getChildren().add(label);
                commandButton.setText(commandButtonText);
            }

            if (indicator != null) {
                setupElement(indicator, indicatorLayoutX, initYIndicator, indicatorPrefHeight, indicatorPrefWidth);
            }
            if (openButton != null) {
                setupElement(openButton, openButtonLayoutX, initY, openButtonPrefHeight, openButtonPrefWidth);
            }
            if (closeButton != null) {
                setupElement(closeButton, closeButtonLayoutX, initY, closeButtonPrefHeight, closeButtonPrefWidth);
            }
            if (input != null) {
                input.setPromptText(inputText);
                setupElement(input, inputLayoutX, initY, inputPrefHeight, inputPrefWidth);
            }
            if (commandButton != null) {
                setupElement(commandButton, commandButtonLayoutX, initY, commandButtonPrefHeight, commandButtonPrefWidth);
            }

            initY += offsetY;
            initYLabel += offsetY;
            initYIndicator += offsetY;
        }
    }
}
