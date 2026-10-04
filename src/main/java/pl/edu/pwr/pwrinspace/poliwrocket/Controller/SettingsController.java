package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration.Configuration;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration.ConfigurationSaveModel;
import pl.edu.pwr.pwrinspace.poliwrocket.Service.Save.ModelAsJsonSaveService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsController extends BaseController {
    @FXML
    public JFXToggleButton lightModeToggle;

    @FXML
    private AnchorPane mainPanel;
    @FXML
    protected JFXButton applyButton;
    @FXML
    protected JFXButton reloadButton;

    private final ModelAsJsonSaveService modelAsJsonSaveService = new ModelAsJsonSaveService();
    List<IAction> actionList = new ArrayList<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @FXML
    protected void initialize() {
        applyButton.setOnMouseClicked(event -> executorService.execute(this::apply));
        reloadButton.setOnMouseClicked(event -> executorService.execute(this::reloadConfig));
    }

    void apply() {
        actionList.forEach(IAction::execute);
    }

    @FXML
    private void onToggleLightMode() {
        boolean isLight = lightModeToggle.isSelected();;
        Configuration.getInstance().setLightMode(isLight);
    }

    void reloadConfig() {
        try {
            Configuration.getInstance().reloadConfigInstance(modelAsJsonSaveService.readFromFile(new ConfigurationSaveModel()));
        } catch (Exception e) {
            logger.error("Bad config file");
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            logger.error(e.toString());
        }
    }

    @Override
    public void invalidated(Observable observable) {}
}
