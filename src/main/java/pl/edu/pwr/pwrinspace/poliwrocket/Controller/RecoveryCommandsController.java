package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import com.jfoenix.controls.JFXToggleButton;
import javafx.fxml.FXML;

public class RecoveryCommandsController extends BaseButtonSensorCommandsController {
    @FXML
    protected JFXToggleButton dumpValveToggle;

    public RecoveryCommandsController() {
        this.labelPrefWidth = 200;
        this.indicatorLayoutX = 195;
        this.openButtonLayoutX = 247;
        this.closeButtonLayoutX = 317;
        this.inputLayoutX = 195;
        this.commandButtonLayoutX = 275;
    }

    @Override
    protected void buildVisualizationMap() {
        super.buildVisualizationMap();
        toggleDumpValve();
    }

    @FXML
    public void toggleDumpValve() {
        var dumpValveButton = commandHashMap.get("dump_valve_fire");
        if (dumpValveButton != null) dumpValveButton.setDisable(!dumpValveToggle.isSelected());
    }
}
