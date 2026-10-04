package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import com.jfoenix.controls.JFXButton;
import eu.hansolo.tilesfx.addons.Indicator;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.CodeInterpreterUIHint;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.ISensor;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.UI.UIThreadManager;

import java.util.HashMap;

public class FlightValvesController extends BaseButtonSensorController {
    @FXML
    protected Indicator dataIndicator1;
    @FXML
    protected Label indicatorLabel1;
    @FXML
    protected JFXButton valveOpenButton1;
    @FXML
    protected JFXButton valveCloseButton1;
    @FXML
    protected Indicator dataIndicator2;
    @FXML
    protected Label indicatorLabel2;
    @FXML
    protected JFXButton valveOpenButton2;
    @FXML
    protected JFXButton valveCloseButton2;
    @FXML
    protected Indicator dataIndicator3;
    @FXML
    protected Label indicatorLabel3;
    @FXML
    protected JFXButton valveOpenButton3;
    @FXML
    protected JFXButton valveCloseButton3;
    @FXML
    protected Indicator dataIndicator4;
    @FXML
    protected Label indicatorLabel4;
    @FXML
    protected JFXButton valveOpenButton4;
    @FXML
    protected JFXButton valveCloseButton4;
    @FXML
    protected Indicator dataIndicator5;
    @FXML
    protected Label indicatorLabel5;
    @FXML
    protected JFXButton valveOpenButton5;
    @FXML
    protected JFXButton valveCloseButton5;
    @FXML
    protected Indicator dataIndicator6;
    @FXML
    protected Label indicatorLabel6;
    @FXML
    protected JFXButton valveOpenButton6;
    @FXML
    protected JFXButton valveCloseButton6;
    @FXML
    protected Indicator dataIndicator7;
    @FXML
    protected Label indicatorLabel7;
    @FXML
    protected JFXButton valveOpenButton7;
    @FXML
    protected JFXButton valveCloseButton7;

    private final HashMap<String, Button> closeHashMap = new HashMap<>();
    private final HashMap<String,Button> openHashMap = new HashMap<>();

    @Override
    protected void buildVisualizationMap() {
        super.buildVisualizationMap();

        closeHashMap.clear();
        openHashMap.clear();

        openHashMap.put(dataIndicator1.getId(),valveOpenButton1);
        openHashMap.put(dataIndicator2.getId(),valveOpenButton2);
        openHashMap.put(dataIndicator3.getId(),valveOpenButton3);
        openHashMap.put(dataIndicator4.getId(),valveOpenButton4);
        openHashMap.put(dataIndicator5.getId(),valveOpenButton5);
        openHashMap.put(dataIndicator6.getId(),valveOpenButton6);

        closeHashMap.put(dataIndicator1.getId(),valveCloseButton1);
        closeHashMap.put(dataIndicator2.getId(),valveCloseButton2);
        closeHashMap.put(dataIndicator3.getId(),valveCloseButton3);
        closeHashMap.put(dataIndicator4.getId(),valveCloseButton4);
        closeHashMap.put(dataIndicator5.getId(),valveCloseButton5);
    }

    @Override
    public void invalidated(Observable observable) {
        try {
            var sensor = ((ISensor) observable);
            UIThreadManager.getInstance().addImmediateOnOK(() -> {
                var ind = indicatorHashMap.get(sensor.getDestination());

                if (ind != null) {
                    ind.setDotOnColor(sensor.hasInterpreter() ? UIHelper.resolveUIHintColor(sensor.getCodeMeaning().UIHint) : Color.DODGERBLUE);
                    ind.setOn(sensor.getValue() == 1.0);
                }

                if (sensor.hasInterpreter()) {
                    boolean isNotClosed = sensor.getCodeMeaning().UIHint != CodeInterpreterUIHint.CLOSE;
                    var closeBtn = closeHashMap.get(sensor.getDestination());
                    var openBtn = openHashMap.get(sensor.getDestination());
                    if (closeBtn != null) closeBtn.setDefaultButton(isNotClosed);
                    if (openBtn != null) openBtn.setDefaultButton(!isNotClosed);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
