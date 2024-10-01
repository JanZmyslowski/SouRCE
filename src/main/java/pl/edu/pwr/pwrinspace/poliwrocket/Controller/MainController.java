package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import com.interactivemesh.jfx.importer.ModelImporter;
import com.interactivemesh.jfx.importer.tds.TdsModelImporter;
import com.jfoenix.controls.JFXTextArea;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import org.javatuples.Pair;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration.Configuration;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.MessageParser.IMessageParser;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.SerialPort.ISerialPortManager;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.UI.UIThreadManager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.XMLFormatter;
import java.util.stream.Collectors;

public class MainController extends BasicController implements InvalidationListener {

    private static final double initWidth = 1550.4;
    private static final double initHeight = 838.4;

//    @FXML
//    public SubScene newScene;

    @FXML
    private SubScene errorsScene;

    @FXML
    private JFXTextArea inComing;

    @FXML
    private SubScene dataScene;

    @FXML
    private SubScene mapScene;

    @FXML
    private SubScene powerScene;

    @FXML
    private SubScene abortScene;

    @FXML
    private SubScene commandsScene;

    @FXML
    private SubScene commands2Scene;

    @FXML
    private AnchorPane footer;

    @FXML
    private ImageView poliwrocketLogo;

    @FXML
    private ImageView inSpaceLogo;

    @FXML
    private SubScene rawDataScene;

    @FXML
    private SubScene connectionScene;

    @FXML
    private TabPane tabPane;

    @FXML
    private JFXTextArea outGoing;

    @FXML
    private SubScene dataFillingScene;

    @FXML
    private SubScene valvesScene;

    @FXML
    private SubScene dataFlightScene;

    @FXML
    private SubScene modelScene;

    @FXML
    private SubScene moreDataScene;

    @FXML
    private SubScene indicatorsScene;

    @FXML
    private SubScene indicators2Scene;

    @FXML
    private SubScene CANIndicatorsScene;

    @FXML
    private SubScene indicatorsFlightScene;

    @FXML
    private SubScene startControlScene;

    @FXML
    private SubScene settingsScene;

    @FXML
    private SubScene interpretersScene;

    @FXML
    private SubScene rocketSettingsScene;

    @FXML
    private SubScene interpretersFlightScene;

    @FXML
    private SubScene fillingCommandsScene;

    private final SmartGroup root = new SmartGroup();

    private Stage primaryStage;

    private final List<Node> nodes = new ArrayList<>();
    private final HashMap<Node,Pair<Double,Double>> nodesInitPositions = new HashMap<>();

    public SubScene getMapScene() {
        return mapScene;
    }


    public void initSubScenes(Collection<FXMLLoader> fxmlLoaders) {
        try {
            HashMap<String, Field> fields = new HashMap<>();

            for (Field declaredField : this.getClass().getDeclaredFields()) {
                String fieldName = declaredField.getName();
                if(fieldName.endsWith("Scene"))
                    fields.put(fieldName.replace("Scene","").toLowerCase(),declaredField);
            }

            for (FXMLLoader fxmlLoader : fxmlLoaders) {
                Parent loaded = fxmlLoader.load();
                String className = fxmlLoader.getController().getClass().getSimpleName().replace("Controller", "").toLowerCase();
                var field = fields.get(className);
                if(field != null) {
                    ((SubScene)field.get(this)).setRoot(loaded);
                } else {
                    logger.error(String.format("Scene not found for %s", className));
                }
            }
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void initialize() {
        addNodesForAppScalingPurpose();
        setAppImages();
        setup3DModel();
    }

    private void setAppImages() {
        //set logo
        poliwrocketLogo.setImage(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("Poliwrocket.png"))));
        inSpaceLogo.setImage(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("inSpaceLogo.png"))));
    }

    private void addNodesForAppScalingPurpose() {
        //add nodes to list, ONLY nodes that are directly on main panel, it is necessary for scaling app window
        nodes.add(dataScene);
        nodes.add(mapScene);
        nodes.add(powerScene);
        nodes.add(abortScene);
        nodes.add(modelScene);
        nodes.add(footer);
        nodes.add(rawDataScene);
        nodes.add(indicatorsScene);
        nodes.add(indicators2Scene);
        nodes.add(outGoing);
        nodes.add(inComing);
        nodes.add(interpretersScene);
        nodes.add(tabPane);
        nodes.add(CANIndicatorsScene);
//        nodes.add(newScene);
        nodes.forEach(scene -> nodesInitPositions.put(scene,new Pair<>(scene.getLayoutX(),scene.getLayoutY())));
    }

    private void setup3DModel() {
        modelScene.setVisible(false); //tmp off

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
        if (observable instanceof IMessageParser) {
            var value = ((IMessageParser) observable).getLastMessage();
            if(!value.contains("\n")) {
                value += "\n";
            }
            String currentLog = inComing.getText();
            String[] currentLogs = currentLog.split("\n");
            if(currentLogs.length > 30)
                currentLog = Arrays.stream(currentLogs).skip(5).collect(Collectors.joining("\n"));
            currentLog += value;
            String finalValue = currentLog;
            UIThreadManager.getInstance().addImmediate(() -> {
                double pos = inComing.getScrollTop();
                int anchor = inComing.getAnchor();
                int caret = inComing.getCaretPosition();
                inComing.clear();
                inComing.appendText(finalValue);
                inComing.setScrollTop(pos);
                inComing.selectRange(anchor, caret);
            });
        } else if (observable instanceof ISerialPortManager) {
            var value = ((ISerialPortManager) observable).getLastSend() + "\n";
            Platform.runLater(() -> outGoing.appendText(value));
            Platform.requestNextPulse();
        } else if (observable == Configuration.getInstance()) {
            if(Configuration.getInstance().isForceCommandsActive()) {
                outGoing.setStyle("-fx-border-color: red;");
            } else {
                outGoing.setStyle("");
            }
        } else if(primaryStage.heightProperty().equals(observable) || primaryStage.widthProperty().equals(observable)) {
            scaleSubScenes(primaryStage.widthProperty().doubleValue()/initWidth,primaryStage.heightProperty().doubleValue()/initHeight);
        }
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void scaleSubScenes(double scaleX, double scaleY) {
        nodes.forEach(scene -> {
            if(!scene.getTransforms().isEmpty()) {
                scene.getTransforms().clear();
            }

            scene.getTransforms().add(new Scale(scaleX,scaleY));
            scene.setLayoutX(nodesInitPositions.get(scene).getValue0() * scaleX);
            scene.setLayoutY(nodesInitPositions.get(scene).getValue1() * scaleY);
        });
    }

}
