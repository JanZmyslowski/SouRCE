package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.ISensor;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.UI.UIThreadManager;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseInterpretersController extends BaseSensorController {

    @FXML
    protected AnchorPane mainPanel;

    protected final HashMap<String, Label> labelHashMap = new HashMap<>();
    protected final HashMap<String, Label> valueHashMap = new HashMap<>();

    @Override
    protected void buildVisualizationMap() {
        mainPanel.getChildren().removeIf(node -> labelHashMap.containsValue(node) || valueHashMap.containsValue(node));
    }

    @Override
    protected void setUIBySensors() {
        for (ISensor sensor : this.getSortedSensors()){
            labelHashMap.get(sensor.getDestination()).setText(sensor.getName());

            if(sensor.hasInterpreter()) {
                var valueLabel = valueHashMap.get(sensor.getDestination());
                var code = sensor.getCodeMeaning();
                valueLabel.setText(code.text);
                valueLabel.setTextFill(UIHelper.resolveUIHintColor(code.UIHint));

            } else {
                valueHashMap.get(sensor.getDestination()).setText(Double.toString(sensor.getValue()));
            }
        }
    }

    protected List<ISensor> getSortedSensors() {
        return sensors.stream().sorted(Comparator.comparing(ISensor::getDestination, String::compareTo))
                .collect(Collectors.toList());
    }

    @Override
    public void invalidated(Observable observable) {
        if(observable instanceof ISensor) {
            var sensor = (ISensor)observable;
            var valueLabel =  valueHashMap.get(sensor.getDestination());
            var code = sensor.getCodeMeaning();

            UIThreadManager.getInstance().addNormal(() -> {
                if(sensor.hasInterpreter()) {
                    valueLabel.setText(code.text);
                    valueLabel.setTextFill(UIHelper.resolveUIHintColor(code.UIHint));
                } else {
                    valueLabel.setText(Double.toString(sensor.getValue()));
                }
            });
        }
    }
}
