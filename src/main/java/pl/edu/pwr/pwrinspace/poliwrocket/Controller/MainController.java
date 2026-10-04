package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import com.jfoenix.controls.JFXTextArea;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.tilesfx.Tile;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
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
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MainController extends BaseController implements InvalidationListener {

    private static final double initWidth = 1550.4;
    private static final double initHeight = 838.4;

    private static final Color white = Color.WHITE ;
    private static final Color bgDark = Color.rgb(11, 66, 116, 0.7);

    private static final Color black = Color.BLACK;
    private static final Color fgDark = Color.rgb(245, 245, 247);

    @FXML
    private SubScene CANIndicatorsScene;
    @FXML
    private SubScene fillingCommandsScene;
    @FXML
    private SubScene pressurizingCommandsScene;
    @FXML
    private SubScene othersIndicatorsScene;
    @FXML
    private SubScene othersIndicators2Scene;
    @FXML
    private SubScene serialPortMonitorScene;
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
    private SubScene valvesScene;
    @FXML
    private TabPane tabPane;
    @FXML
    private JFXTextArea outGoing;
    @FXML
    private SubScene dataFillingScene;
    @FXML
    private SubScene dataPressurizingScene;
    @FXML
    private SubScene valvesPressurizingScene;
    @FXML
    private SubScene indicatorsPressurizingScene;
    @FXML
    private SubScene startCommandsScene;
    @FXML
    private SubScene recoveryCommandsScene;
    @FXML
    private SubScene dataFlightScene;
    @FXML
    private SubScene moreDataScene;
    @FXML
    private SubScene indicatorsScene;
    @FXML
    private SubScene indicators2Scene;
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
    private SubScene othersScene;
    @FXML
    private SubScene others2Scene;
    @FXML
    private SubScene recoveryArmScene;
    @FXML
    private SubScene testCommandsScene;
    @FXML
    private SubScene fuelingCalculatorScene;

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
                    ((SubScene) field.get(this)).setRoot(loaded);
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

        detaching();

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
//        nodes.add(modelScene);
        nodes.add(footer);
        nodes.add(rawDataScene);
        nodes.add(indicatorsScene);
        nodes.add(indicators2Scene);
        nodes.add(outGoing);
        nodes.add(inComing);
        nodes.add(interpretersScene);
        nodes.add(tabPane);
        nodes.forEach(scene -> nodesInitPositions.put(scene,new Pair<>(scene.getLayoutX(),scene.getLayoutY())));
    }

    private void setup3DModel() {
//        modelScene.setVisible(false); //tmp off
//
//        //Creating camera
//        PerspectiveCamera camera = new PerspectiveCamera(true);
//        camera.setTranslateZ(-125); //-900
//        camera.setNearClip(0.01);
//        camera.setFarClip(3000.0);
//        camera.setFieldOfView(60);
//        modelScene.setCamera(camera);
//
//
//        PointLight light = new PointLight(Color.WHITE);
//        light.setTranslateX(0);
//        light.setTranslateY(6000);
//        light.setTranslateZ(300);
//        root.getChildren().add(light);
//
//        AmbientLight ambiance = new AmbientLight(Color.LIGHTGREY);
//        root.getChildren().add(ambiance);
//
//        //importing 3ds model
//        ModelImporter tdsImporter = new TdsModelImporter();
//        try {
//            tdsImporter.read("./assets/rocketModel/rocketModel.3DS");
//        } catch (Exception e){
//            logger.error(e.getMessage());
//            logger.info("Loading default model.");
//            tdsImporter.read(getClass().getClassLoader().getResource("rocketModel.3DS"));
//        }
//        Node[] tdsMesh = (Node[]) tdsImporter.getImport();
//
//        Node rocket3DModel = tdsMesh[0];
//        tdsImporter.close();
//        root.getChildren().add(rocket3DModel);
//        modelScene.setRoot(root);
//
//        modelScene.setOnScroll(scrollEvent -> modelScene.getCamera().setTranslateZ(Double.min(0 , modelScene.getCamera().getTranslateZ() + scrollEvent.getDeltaY())));
    }

    @Override
    public void invalidated(Observable observable) {
        if (observable instanceof IMessageParser) {
            var value = ((IMessageParser) observable).getLastMessage();
            if (!value.contains("\n")) {
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

            boolean isLight = ((Configuration) observable).isLightMode();
            URL cssURL = getClass().getResource(isLight ? "/Views/constantsLight.css" : "/Views/constants.css");

            if (cssURL == null) {
                System.err.println("ERROR: css not found");
                return;
            }

            String cssPath = cssURL.toExternalForm();

            nodes.forEach(node -> {
                applyStyleToNode(node, cssPath, isLight);
            });

            for (Field field : this.getClass().getDeclaredFields()) {
                if (field.getName().endsWith("Scene")) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(this);
                        if (value instanceof SubScene) {
                            SubScene ss = (SubScene) value;
                            applyStyleToNode(ss, cssPath, isLight);
                            findAndStyleTiles(ss.getRoot(), isLight);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
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


    private void detachTab(Tab tab) {
        Node content = tab.getContent();
        if (content == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/DetachedTabView.fxml"));
            Parent wrapper = loader.load();
            DetachedTabController detachedController = loader.getController();

            tab.setContent(null);
            int oldIndex = tabPane.getTabs().indexOf(tab);
            tabPane.getTabs().remove(tab);

            ((AnchorPane)wrapper).getChildren().add(content);
            Stage stage = new Stage();
            stage.setScene(new Scene(wrapper, initWidth, initHeight));

            String tabTitle = tab.getText();
            stage.setTitle("SouRCE - " + tabTitle);

            detachedController.setStageAndContent(stage, content);

            stage.setOnCloseRequest(e -> {
                content.getTransforms().clear();
                tab.setContent(content);
                tabPane.getTabs().add(oldIndex, tab);
                tabPane.getSelectionModel().select(tab);
            });

            stage.show();
        } catch (IOException e) {
            logger.error("Error DetachedView: " + e.getMessage());
        }
    }

    private void detaching(){
        Platform.runLater(() -> {
            if(tabPane != null){
                for(Tab tab: tabPane.getTabs()){
                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem detachItem = new MenuItem("detach");

                    detachItem.setOnAction(event -> detachTab(tab));
                    contextMenu.getItems().add(detachItem);

                    tab.setContextMenu(contextMenu);
                }
            }
        });
    }

    private void applyStyleToNode(Node node, String cssPath, boolean isLight) {
        if (node instanceof Parent) {
            Parent p = (Parent) node;
            p.getStylesheets().clear();
            p.getStylesheets().add(cssPath);
        } else if (node instanceof SubScene) {
            SubScene ss = (SubScene) node;
            if (ss.getRoot() != null) {
                ss.getRoot().getStylesheets().clear();
                ss.getRoot().getStylesheets().add(cssPath);
                findAndStyleTiles(ss.getRoot(), isLight);
            }else{
                System.out.println("Error: Scene root cannot be null when applying styles");
            }
        }
    }

    private void findAndStyleTiles(Parent root, boolean isLight) {
        Color bg = isLight ? white : bgDark;
        Color fg = isLight ? black : fgDark;
        for (Node n : root.getChildrenUnmodifiable()) {
            if (n instanceof Tile) {
                Tile tile = (Tile) n;
                applyTileStyle(tile, bg, fg);
            }else if (n instanceof Gauge){
                Gauge gauge = (Gauge) n;
                applyGaugeStyle(gauge, fg);
            }
        }
    }

    private void applyTileStyle(Tile tile, Color bg, Color fg) {
        tile.setBackgroundColor(bg);
        tile.setForegroundBaseColor(fg);
        tile.setTitleColor(fg);
        tile.setTextColor(fg);
        tile.setValueColor(fg);
        tile.setUnitColor(fg);
        tile.setBarColor(fg);
    }

    private void applyGaugeStyle(Gauge gauge, Color fg) {
        gauge.setBarColor(fg);
        gauge.setValueColor(fg);
        gauge.setTitleColor(fg);
        gauge.setUnitColor(fg);
        gauge.setTickLabelColor(fg);
        gauge.setNeedleColor(fg);
    }
}
