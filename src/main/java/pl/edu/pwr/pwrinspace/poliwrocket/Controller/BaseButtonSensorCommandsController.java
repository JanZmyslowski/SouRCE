package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import eu.hansolo.tilesfx.addons.Indicator;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.ICommand;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.CodeInterpreterUIHint;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.ISensor;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.UI.UIThreadManager;

import java.util.*;
import java.util.stream.Collectors;

// from now this controller will work similar to normal BaseButtonSensorController
// differences
// it's dynamic - you don't have to define its fields
// it supports buttons with payload

public abstract class BaseButtonSensorCommandsController extends BaseCommandsController {

    @FXML
    protected AnchorPane mainPanel;

    protected final HashMap<String, ISensor> sensorHashMap = new HashMap<>();
    protected final HashMap<String, Button> openHashMap = new HashMap<>();
    protected final HashMap<String, Button> closeHashMap = new HashMap<>();

    // indicator
    protected int initYIndicator = 30;
    protected int indicatorLayoutX = 115;
    protected int indicatorPrefHeight = 42;
    protected int indicatorPrefWidth = 42;

    // open button
    protected int openButtonLayoutX = 167;
    protected int openButtonPrefHeight = 26;
    protected int openButtonPrefWidth = 60;

    // close button
    protected int closeButtonLayoutX = 237;
    protected int closeButtonPrefHeight = 26;
    protected int closeButtonPrefWidth = 60;

    public BaseButtonSensorCommandsController() {
        this.initY = 38;
        this.initYLabel = 42;
        this.offestY = 45;
        this.labelPrefWidth = 90;
        this.inputLayoutX = 307;
        this.commandButtonLayoutX = 387;
    }

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

    @Override
    protected void setUIBySensors() {}

    @Override
    public void assignsCommands(Collection<ICommand> commands) {
        Platform.runLater(this::buildVisualizationMap);
        this.commands.clear();
        this.commands.addAll(commands);
        Platform.runLater(() -> {
            for (ICommand command : commands) {
                var button = buttonHashMap.get(command.getCommandTriggerKey() + command.getCommandType());
                if (button != null) {
                    button.setOnAction(handleButtonsClickByCommand(button, command));
                    button.setVisible(true);
                } else {
                    logger.warn("Trigger not found: {} , it`s maybe correct for fire button!", command.getCommandTriggerKey());
                }
            }
        });
    }

    @Override
    public void invalidated(Observable observable) {
        try {
            var sensor = ((ISensor) observable);
            UIThreadManager.getInstance().addImmediateOnOK(() -> {
                var ind = indicatorHashMap.get(sensor.getName().toLowerCase().replaceAll(" ", ""));

                if (ind != null) {
                    ind.setDotOnColor(sensor.hasInterpreter() ? UIHelper.resolveUIHintColor(sensor.getCodeMeaning().UIHint) : Color.DODGERBLUE);
                    ind.setOn(sensor.getValue() == 1.0);
                }

                if (sensor.hasInterpreter()) {
                    boolean isNotClosed = sensor.getCodeMeaning().UIHint != CodeInterpreterUIHint.CLOSE;
                    var closeBtn = closeHashMap.get(sensor.getName().toLowerCase().replace(" ", ""));
                    var openBtn = openHashMap.get(sensor.getDestination().toLowerCase().replace(" ", ""));
                    if (closeBtn != null) closeBtn.setDefaultButton(isNotClosed);
                    if (openBtn != null) openBtn.setDefaultButton(!isNotClosed);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setupElement(Region element, int layoutX, int layoutY, int prefHeight, int prefWidth) {
        element.setLayoutY(layoutY);
        element.setLayoutX(layoutX);
        element.setPrefHeight(prefHeight);
        element.setPrefWidth(prefWidth);
        mainPanel.getChildren().add(element);
    }
}
