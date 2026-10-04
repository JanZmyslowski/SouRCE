package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.ICommand;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.ISensor;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.SerialPort.SerialPortManager;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public abstract class BaseButtonSensorController extends BaseTilesFXSensorController {

    protected HashSet<ICommand> commands = new HashSet<>();
    protected HashMap<String,Button> buttonHashMap = new HashMap<>();

    @Override
    protected void buildVisualizationMap() {
        super.buildVisualizationMap();

        buttonHashMap.clear();

        for (Field declaredField : this.getClass().getDeclaredFields()) {
            if (Button.class.isAssignableFrom(declaredField.getType())) {
                try {
                    buttonHashMap.put(declaredField.getName(), (Button) declaredField.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void setUIBySensors() {
        super.setUIBySensors();
        for (ISensor sensor : sensors) {
            if (sensor != null) {
                var indicator = indicatorHashMap.get(sensor.getDestination());
                if (indicator != null) {
                    boolean on = sensor.getValue() == 1.0;
                    indicator.setOn(on);
                    indicator.setDotOnColor(sensor.hasInterpreter() ? UIHelper.resolveUIHintColor(sensor.getCodeMeaning().UIHint) : Color.TRANSPARENT);
                }
            }
        }
    }

    public void assignsCommands(Collection<ICommand> commands){
        this.commands.clear();
        this.commands.addAll(commands);
        Platform.runLater(() -> {
            for (ICommand command : commands) {
                var button = buttonHashMap.get(command.getCommandTriggerKey());
                if (button != null){
                    button.setOnAction(handleButtonsClickByCommand(command));
                    button.setVisible(true);
                } else {
                    logger.warn("Trigger not found: {} , it`s maybe correct for fire button!", command.getCommandTriggerKey());
                }
            }
        });
    }

    protected EventHandler<ActionEvent> handleButtonsClickByCommand(ICommand command){
        return actionEvent -> executorService.execute(() -> SerialPortManager.getInstance().write(command));
    }
}