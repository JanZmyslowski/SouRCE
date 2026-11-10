package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import eu.hansolo.tilesfx.Tile;
import javafx.beans.Observable;
import javafx.fxml.FXML;

public class DataFlightController extends BaseDataTilesController {

    @FXML
    protected Tile dataGauge1;

    @FXML
    protected Tile dataGauge2;

    @FXML
    protected Tile dataGauge3;

    @FXML
    protected Tile dataGauge4;

    @FXML
    protected Tile dataGauge5;

    @FXML
    protected Tile dataGauge6;

    @FXML
    protected Tile dataGauge7;

    @Override
    public void invalidated(Observable observable) {
        super.invalidated(observable);
    }
}
