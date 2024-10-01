package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import eu.hansolo.tilesfx.addons.Indicator;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.ISensor;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.UI.UIThreadManager;

public class CANIndicatorsController extends BasicTilesFXSensorController{

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
