package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.addons.Indicator;
import javafx.scene.control.Label;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.ISensor;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;

public abstract class BaseTilesFXSensorController extends BaseSensorController {

    private static final int _duration = 30;

    private static final Duration DURATION = Duration.ofSeconds(_duration);

    protected final HashMap<String, Tile> tileHashMap = new HashMap<>();
    protected final HashMap<String, Indicator> indicatorHashMap = new HashMap<>();
    protected final HashMap<String, Label> labelHashMap = new HashMap<>();

    @Override
    protected void buildVisualizationMap() {
        tileHashMap.clear();
        indicatorHashMap.clear();
        labelHashMap.clear();

        var fields = getAllFields(new LinkedList<>(), this.getClass());

        for (Field declaredField : fields) {
            try {
                if (declaredField.getType().isAssignableFrom(Tile.class)) {
                    ((Tile) declaredField.get(this)).setVisible(false);
                    tileHashMap.put(declaredField.getName(), (Tile) declaredField.get(this));

                } else if (declaredField.getType().isAssignableFrom(Indicator.class)) {
                    ((Indicator) declaredField.get(this)).setVisible(false);
                    indicatorHashMap.put(declaredField.getName(), (Indicator) declaredField.get(this));

                    var label = fields.stream().filter(f -> f.getName().equals("indicatorLabel" + declaredField.getName().charAt(declaredField.getName().length() - 1))).findFirst();

                    if (label.isPresent()) {
                        Label label_ = (Label) label.get().get(this);
                        label_.setVisible(false);
                        labelHashMap.put(declaredField.getName(), label_);
                    } else {
                        logger.error("Indicator without label! in controller {}", this.getClass().getName());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void setUIBySensors() {
        for (ISensor sensor : sensors) {
            var tile = tileHashMap.get(sensor.getDestination());
            var indicator = indicatorHashMap.get(sensor.getDestination());
            var label = labelHashMap.get(sensor.getDestination());
            if (tile != null) {
                tile.setVisible(true);

                if (sensor.getMinRange() != sensor.getMaxRange()) {
                    tile.setMinValue(sensor.getMinRange());
                    tile.setMaxValue(sensor.getMaxRange());
                } else {
                    tile.setAutoScale(true);
                    tile.setMinValue(-50.0);
                    tile.setMaxValue(Double.MAX_VALUE);
                }
                
                tile.setTitle(sensor.getName());
                tile.setUnit(sensor.getUnit());
                tile.setAverageVisible(true);
                tile.setSmoothing(true);
                tile.setTimePeriod(DURATION);
                tile.setAveragingPeriod(_duration);
                tile.setTextVisible(true);
            } else if (indicator != null) {
                indicator.setVisible(true);

                if (label != null) {
                    label.setText(sensor.getName());
                    label.setVisible(true);
                }
            } else {
                logger.error("Wrong UI binding - destination not found: {}", sensor.getDestination());
            }
        }
    }

}
