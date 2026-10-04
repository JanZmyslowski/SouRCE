package pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration;

import com.google.gson.annotations.Expose;
import pl.edu.pwr.pwrinspace.poliwrocket.Controller.BaseButtonSensorController;
import pl.edu.pwr.pwrinspace.poliwrocket.Controller.BaseController;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.BaseSaveModel;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.Command;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.ICommand;

import java.util.*;

public class CommandsConfig extends BaseSaveModel {

    @Expose
    public List<Command<?>> commandsList = new LinkedList<>();

    public CommandsConfig() {
        super(Configuration.CONFIG_PATH, "CommandConfig.json");
    }

    public void assignCommandsToControllers(Collection<BaseController> controllersList) {
        if (controllersList == null || commandsList == null) {
            return;
        }

        for (BaseController controller : controllersList) {
            if (controller instanceof BaseButtonSensorController buttonController) {
                List<ICommand> assignedCommands = new ArrayList<>();
                String controllerName = controller.getControllerName();

                for (Command<?> command : commandsList) {
                    if (command.getDestinationControllerNames().contains(controllerName)) {
                        assignedCommands.add(command);
                    }
                }

                if (!assignedCommands.isEmpty()) {
                    buttonController.assignsCommands(assignedCommands);
                }
            }
        }
    }
}