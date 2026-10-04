package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import eu.hansolo.tilesfx.addons.Indicator;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.ISensor;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.UI.UIThreadManager;

public class CANIndicatorsController extends BaseTilesFXSensorController {
    @FXML
    protected Indicator dataIndicator1;
    @FXML
    protected Label indicatorLabel1;
    @FXML
    protected Indicator dataIndicator2;
    @FXML
    protected Label indicatorLabel2;
    @FXML
    protected Indicator dataIndicator3;
    @FXML
    protected Label indicatorLabel3;
    @FXML
    protected Indicator dataIndicator4;
    @FXML
    protected Label indicatorLabel4;
    @FXML
    protected Indicator dataIndicator5;
    @FXML
    protected Label indicatorLabel5;
    @FXML
    protected Indicator dataIndicator6;
    @FXML
    protected Label indicatorLabel6;
    @FXML
    protected Indicator dataIndicator7;
    @FXML
    protected Label indicatorLabel7;
    @FXML
    protected Indicator dataIndicator8;
    @FXML
    protected Label indicatorLabel8;
    @FXML
    protected Indicator dataIndicator9;
    @FXML
    protected Label indicatorLabel9;
    @FXML
    protected Indicator dataIndicator10;
    @FXML
    protected Label indicatorLabel10;

    @Override
    public void invalidated(Observable observable) {
        try {
            var sensor = ((ISensor) observable);
            UIThreadManager.getInstance().addImmediateOnOK(() -> {
                var visualization = indicatorHashMap.get(sensor.getDestination());
                if(visualization != null)
                    visualization.setOn(sensor.getValue() == 1.0);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
