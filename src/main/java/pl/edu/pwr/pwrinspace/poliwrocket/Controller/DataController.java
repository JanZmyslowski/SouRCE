package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import eu.hansolo.tilesfx.Tile;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.IAlert;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.ISensor;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.UI.UIThreadManager;

public class DataController extends BaseTilesFXSensorController {

    @FXML
    protected Tile dataGauge1;

    @FXML
    protected Tile dataGauge2;

    @FXML
    protected Tile dataGauge3;

    @Override
    public void invalidated(Observable observable) {
        try {
            var sensor = ((ISensor) observable);
            UIThreadManager.getInstance().addNormal(() -> {
                var gauge = tileHashMap.get(sensor.getDestination());
                if(sensor instanceof IAlert) {
                    if(((IAlert)sensor).getAlert()) {
                        gauge.setValueColor(Color.RED);
                    } else {
                        gauge.setValueColor(Color.WHITE);
                    }
                }

                if(sensor.hasInterpreter()) {
                    var code = sensor.getCodeMeaning();
                    gauge.setText(code.text);
                    gauge.setTextColor(UIHelper.resolveUIHintColor(code.UIHint));
                }
                gauge.setValue(sensor.getValue());

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}