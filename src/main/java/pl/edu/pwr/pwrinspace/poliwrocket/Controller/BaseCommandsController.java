package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.ICommand;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.SerialPort.SerialPortManager;

import java.util.*;
import java.util.stream.Collectors;

// from now this controller will work similar to normal BaseButtonSensorController
// differences
// it's dynamic - you don't have to define its fields
// it supports buttons with payload

public abstract class BaseCommandsController extends BaseButtonSensorController {

    @FXML
    protected AnchorPane mainPanel;

    protected final ArrayList<String> moduleArray = new ArrayList<>();
    protected final HashMap<String, JFXTextField> inputHashMap = new HashMap<>();
    protected final HashMap<String, Button> commandHashMap = new HashMap<>();

    protected int offestY = 40;
    protected int initY = 30;

    // label
    protected int initYLabel = 34;
    protected boolean showLabel = true;
    protected int labelLayoutX = 15;
    protected int labelPrefHeight = 18;
    protected int labelPrefWidth = 180;

    // input
    protected int inputLayoutX = 205;
    protected int inputPrefHeight = 26;
    protected int inputPrefWidth = 70;
    protected String inputText = "X;Y";

    // command button
    protected int commandButtonLayoutX = 285;
    protected int commandButtonPrefHeight = 26;
    protected int commandButtonPrefWidth = 60;
    protected String commandButtonText = "Send";


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
        commandHashMap.clear();
        moduleArray.clear();

        List<ICommand> commandList = this.commands.stream().sorted(Comparator.comparing(ICommand::getCommandDescription)).collect(Collectors.toList());

        for (ICommand command : commandList) {
            String commandTriggerKey = command.getCommandTriggerKey();

            var button = new JFXButton(command.getCommandDescription());
            commandHashMap.put(commandTriggerKey, button);
            buttonHashMap.put(command.getCommandTriggerKey(), button);

            var input = new JFXTextField();
            if (command.getPayload() == null) input.setVisible(false);
            else input.setText(command.getPayload());
            input.setDisable(command.isFinal());
            inputHashMap.put(commandTriggerKey, input);
            inputHashMap.put(command.getCommandTriggerKey(), input);

            if (!moduleArray.contains(commandTriggerKey)) moduleArray.add(commandTriggerKey);
        }

        int initY = this.initY;
        int initYLabel = this.initYLabel;
        int offsetY = this.offestY;

        for (String module : moduleArray) {
            var input = inputHashMap.get(module);
            var commandButton = commandHashMap.get(module);

            if (input != null) {
                input.setLayoutY(initY);
                input.setLayoutX(inputLayoutX);
                input.setPrefHeight(inputPrefHeight);
                input.setPrefWidth(inputPrefWidth);
                input.setPromptText(inputText);
                mainPanel.getChildren().add(input);
            }

            if (commandButton != null) {
                var label = new Label(commandButton.getText());
                commandButton.setText(commandButtonText);
                label.setVisible(showLabel);
                label.setLayoutY(initYLabel);
                label.setLayoutX(labelLayoutX);
                label.setPrefHeight(labelPrefHeight);
                label.setPrefWidth(labelPrefWidth);
                labelHashMap.put(module, label);
                mainPanel.getChildren().add(label);

                commandButton.setLayoutY(initY);
                commandButton.setLayoutX(commandButtonLayoutX);
                commandButton.setPrefHeight(commandButtonPrefHeight);
                commandButton.setPrefWidth(commandButtonPrefWidth);
                mainPanel.getChildren().add(commandButton);
            }

            initY += offsetY;
            initYLabel += offsetY;
        }
    }

    @Override
    public void assignsCommands(Collection<ICommand> commands){
        Platform.runLater(this::buildVisualizationMap);
        super.assignsCommands(commands);
    }

    protected EventHandler<ActionEvent> handleButtonsClickByCommand(Button button, ICommand command){
        if (commandHashMap.containsValue(button)) {
            return actionEvent -> executorService.execute(() -> {
                command.setPayload(inputHashMap.get(command.getCommandTriggerKey()).getText());
                SerialPortManager.getInstance().write(command);
            });
        }
        else {
            return actionEvent -> executorService.execute(() -> {
                SerialPortManager.getInstance().write(command);
            });
        }
    }

    @Override
    public void invalidated(Observable observable) {
        logger.error("Controller has no visualization.");
    }
}
