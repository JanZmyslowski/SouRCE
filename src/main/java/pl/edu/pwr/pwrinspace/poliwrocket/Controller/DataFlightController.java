package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import com.interactivemesh.jfx.importer.ModelImporter;
import com.interactivemesh.jfx.importer.tds.TdsModelImporter;
import eu.hansolo.tilesfx.Tile;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.*;
import javafx.scene.paint.Color;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.IGyroSensor;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.UI.UIThreadManager;

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


    @FXML
    private SubScene modelScene;
    private final SmartGroup root = new SmartGroup();

    @FXML
    protected void initialize() {

        setup3DModel();
    }


    private void setup3DModel() {
        //Creating camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-125); //-900
        camera.setNearClip(0.01);
        camera.setFarClip(3000.0);
        camera.setFieldOfView(60);
        modelScene.setCamera(camera);


        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(0);
        light.setTranslateY(6000);
        light.setTranslateZ(300);
        root.getChildren().add(light);

        AmbientLight ambiance = new AmbientLight(Color.LIGHTGREY);
        root.getChildren().add(ambiance);

        //importing 3ds model
        ModelImporter tdsImporter = new TdsModelImporter();
        try {
            tdsImporter.read("./assets/rocketModel/rocketModel.3DS");
        } catch (Exception e){
            logger.error(e.getMessage());
            logger.info("Loading default model.");
            tdsImporter.read(getClass().getClassLoader().getResource("rocketModel.3DS"));
        }
        Node[] tdsMesh = (Node[]) tdsImporter.getImport();

        Node rocket3DModel = tdsMesh[0];
        tdsImporter.close();
        root.getChildren().add(rocket3DModel);
        modelScene.setRoot(root);

        modelScene.setOnScroll(scrollEvent -> modelScene.getCamera().setTranslateZ(Double.min(0 , modelScene.getCamera().getTranslateZ() + scrollEvent.getDeltaY())));
    }

    @Override
    public void invalidated(Observable observable) {
        if (observable instanceof IGyroSensor) {
            var gyroSensor = (IGyroSensor) observable;
            UIThreadManager.getInstance().addImmediateOnOK(() -> {
                root.rotateByX((int)gyroSensor.getX());
                root.rotateByY((int)gyroSensor.getY());
                root.rotateByZ((int)gyroSensor.getZ());
            });
        } else {
            super.invalidated(observable);
        }
    }
}
