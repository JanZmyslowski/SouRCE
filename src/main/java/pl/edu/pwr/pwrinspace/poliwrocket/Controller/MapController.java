package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import com.jfoenix.controls.JFXTextField;
import com.sothawo.mapjfx.*;
import com.sothawo.mapjfx.offline.OfflineCache;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.IGPSSensor;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.UI.UIThreadManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


// todo dodajmy w końcu obslugę mapy :)))
public class MapController extends BaseController {

    private static final int ZOOM_DEFAULT = 60;
    private static final double MAP_START_CORRECTION = 0.017;

    @FXML private MapView mapView;
    @FXML private JFXTextField currentLocation;
    @FXML protected Label currentDistance;

    private CoordinateLine track;

    @Override
    @FXML
    protected void initialize() {
        initMapAndControls(Projection.WEB_MERCATOR);
    }

    public void initMapAndControls(Projection projection) {

        // init MapView-Cache
        final OfflineCache offlineCache = mapView.getOfflineCache();
        final String cacheDir = System.getProperty("java.io.tmpdir") + "/mapjfx-cache";
        logger.info("using dir for cache: {}", cacheDir);
        try {
            Files.createDirectories(Paths.get(cacheDir));
            offlineCache.setCacheDirectory(cacheDir);
            offlineCache.setActive(true);
        } catch (IOException | IllegalArgumentException e) {
            logger.warn("could not activate offline cache", e);
        }

        // watch the MapView's initialized property to finish initialization
        mapView.initializedProperty().addListener((observable, oldValue, newValue) -> {
            if (Boolean.TRUE.equals(newValue)) {
                afterMapIsInitialized();
            }
        });

        Platform.runLater(() -> {
            try {
                mapView.setMapType(MapType.OSM);

                // finally initialize the map view
                mapView.initialize(Configuration.builder()
                        .projection(projection)
                        .showZoomControls(false)
                        .build());
            } catch (Exception e) {
                logger.error("Map init error", e);
            }
        });

    }

    private void afterMapIsInitialized() {
        mapView.setZoom(ZOOM_DEFAULT);
        Coordinate startPosition = new Coordinate(pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration.Configuration.getInstance().START_POSITION_LAT+MAP_START_CORRECTION, pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration.Configuration.getInstance().START_POSITION_LON-MAP_START_CORRECTION);
        mapView.setCenter(startPosition);
    }

    @Override
    public void invalidated(Observable observable) {
        UIThreadManager.getInstance().addImmediateOnOK(() -> {
            Coordinate nextCoordinate = new Coordinate(((IGPSSensor) observable).getPosition().get(IGPSSensor.LATITUDE_KEY), ((IGPSSensor) observable).getPosition().get(IGPSSensor.LONGITUDE_KEY));
            currentLocation.setText(nextCoordinate.getLatitude() + ";" + nextCoordinate.getLongitude());
            currentDistance.setText("Distance: " + distance(((IGPSSensor) observable).getPosition().get(IGPSSensor.LATITUDE_KEY), ((IGPSSensor) observable).getPosition().get(IGPSSensor.LONGITUDE_KEY)) + "m");
            final List<Coordinate> coordinates = new ArrayList<>();
            if (track != null) {
                track.setVisible(false);
                track.getCoordinateStream().limit(480).forEach(coordinates::add);
                mapView.removeCoordinateLine(track);
                track = null;
            }
            coordinates.add(nextCoordinate);
            track = new CoordinateLine(coordinates)
                    .setColor(Color.DODGERBLUE)
                    .setWidth(7)
                    .setClosed(false);
            mapView.addCoordinateLine(track);
            track.setVisible(true);
        });
    }

    public static int distance(double currentLat, double currentLon) {

        // The math module contains a function
        // named toRadians which converts from
        // degrees to radians.
        currentLon = Math.toRadians(currentLon);
        double lon2 = Math.toRadians(pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration.Configuration.getInstance().START_POSITION_LON);
        currentLat = Math.toRadians(currentLat);
        double lat2 = Math.toRadians(pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration.Configuration.getInstance().START_POSITION_LAT);

        // Haversine formula
        double dlon = lon2 - currentLon;
        double dlat = lat2 - currentLat;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(currentLat) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        // Radius of earth in kilometers. Use 3956
        // for miles
        double r = 6371;

        // calculate the result in meters
        return (int) ((c * r) * 1000);
    }
}
