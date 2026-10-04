package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import javafx.beans.Observable;
import javafx.scene.paint.Color;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.IAlert;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.ISensor;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.UI.UIThreadManager;

public abstract class BaseDataTilesController extends BaseTilesFXSensorController {

    @Override
    public void invalidated(Observable observable) {
        try {
            var sensor = ((ISensor) observable);
            UIThreadManager.getInstance().addNormal(() -> {
                var gauge = tileHashMap.get(sensor.getDestination());
                if(sensor instanceof IAlert) {
                    if (((IAlert) sensor).getAlert()) {
                        gauge.setValueColor(Color.rgb(0, 255, 68));
                    } else {
                        gauge.setValueColor(Color.WHITE);
                    }
                }
                gauge.setValue(sensor.getValue());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void setUIBySensors() {
        super.setUIBySensors();
        tileHashMap.values().forEach(tile -> {
            var tileCharNumber = tile.getId().charAt(tile.getId().length() - 1);
            int tileNum = Character.getNumericValue(tileCharNumber);
            var simpleName = tile.getId().substring(0, tile.getId().length() - 2);
            if(tileNum % 2 == 0 && !tile.visibleProperty().get()) {
                var visible = tileHashMap.get(simpleName + (tileNum - 1));
                if(visible != null && visible.visibleProperty().get()) {
                    visible.setPrefWidth(visible.getPrefWidth() * 2 + 24);
                }
            }
        });
    }
}
