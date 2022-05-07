package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.ICommand;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.SerialPort.SerialPortManager;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BasicButtonSensorController extends BasicTilesFXSensorController {

    protected HashMap<String,Button> buttonHashMap = new HashMap<>();

    protected HashSet<ICommand> commands = new HashSet<>();

    protected static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @FXML
    protected void initialize() {
        for (Field declaredField : this.getClass().getDeclaredFields()) {
            if(Button.class.isAssignableFrom(declaredField.getType())) {
                try {
                    buttonHashMap.put(declaredField.getName(), (Button)declaredField.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void assignsCommands(Collection<ICommand> commands){
        Platform.runLater(() -> {
            this.commands.clear();
            this.commands.addAll(commands);

            for (ICommand command : commands) {
                var button = buttonHashMap.get(command.getCommandTriggerKey());
                if (button != null){
                    button.setOnAction(handleButtonsClickByCommand(button,command));
                    button.setVisible(true);
                } else {
                    logger.warn("Trigger not found: {} , it`s maybe correct for fire button!", command.getCommandTriggerKey());
                }
            }
        });
    }

    protected EventHandler<ActionEvent> handleButtonsClickByCommand(Button button, ICommand command){
        return actionEvent -> executorService.execute(() -> SerialPortManager.getInstance().write(command.getCommandValue()));
    }

}
