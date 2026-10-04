package pl.edu.pwr.pwrinspace.poliwrocket;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pwr.pwrinspace.poliwrocket.Controller.BaseController;
import pl.edu.pwr.pwrinspace.poliwrocket.Controller.ConnectionController;
import pl.edu.pwr.pwrinspace.poliwrocket.Controller.MainController;
import pl.edu.pwr.pwrinspace.poliwrocket.Controller.RAWDataController;
import pl.edu.pwr.pwrinspace.poliwrocket.Event.Discord.NotificationDiscordEvent;
import pl.edu.pwr.pwrinspace.poliwrocket.Event.NotificationEvent;
import pl.edu.pwr.pwrinspace.poliwrocket.Event.UIUpdateEventEmitter;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration.Configuration;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration.ConfigurationSaveModel;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.MessageParser.*;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Notification.DiscordNotification;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Notification.INotification;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.SerialPort.SerialPortManager;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Speech.TextToSpeechDictionary;
import pl.edu.pwr.pwrinspace.poliwrocket.Service.Notification.NotificationFormatDiscordService;
import pl.edu.pwr.pwrinspace.poliwrocket.Service.Notification.NotificationFormatService;
import pl.edu.pwr.pwrinspace.poliwrocket.Service.Notification.NotificationSendService;
import pl.edu.pwr.pwrinspace.poliwrocket.Service.Rule.RuleValidationService;
import pl.edu.pwr.pwrinspace.poliwrocket.Service.Save.FrameSaveService;
import pl.edu.pwr.pwrinspace.poliwrocket.Service.Save.ModelAsJsonSaveService;
import pl.edu.pwr.pwrinspace.poliwrocket.Service.Speech.TextToSpeechService;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.Logger.AppStateLogger;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.Notification.NotificationThread;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.SchedulerThread;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.UI.UIThreadManager;

import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private final ModelAsJsonSaveService modelAsJsonSaveService = new ModelAsJsonSaveService();
    private FrameSaveService frameSaveService;
    private NotificationSendService notificationSendService;
    private NotificationThread notificationThread;
    private NotificationFormatService notificationFormatService;
    private IMessageParser messageParser;
    private NotificationEvent notificationEvent;
    private TextToSpeechService ttsService;
    private TextToSpeechDictionary textToSpeechDictionary;
    private final RuleValidationService ruleValidationService = new RuleValidationService();
    private UIUpdateEventEmitter uiUpdateEventEmitter;
    private SchedulerThread schedulerThread;

    private static final String mainView  = "MainView.fxml";

    @Override
    public void start(Stage primaryStage) {
        System.setProperty("jbr.jfx.media.disabled", "true");
        System.setProperty("jfx.media.disabled", "true");

        logger.warn(System.getProperty("jbr.jfx.media.disabled"));
        try {
            //Read config file
            try {
                Configuration.getInstance().setupConfigInstance(modelAsJsonSaveService.readFromFile(new ConfigurationSaveModel()));
            } catch (UnsupportedOperationException e) {
                logger.error("Wrong mapping in controller");
                e.printStackTrace();
                return;
            } catch (Exception e) {
                logger.error("Bad config file, overwritten by default and loaded");
                logger.error(e.getMessage());
                logger.error(Arrays.toString(e.getStackTrace()));
                logger.error(e.toString());
                modelAsJsonSaveService.persistOldFile(new ConfigurationSaveModel());
                //modelAsJsonSaveService.saveToFile(ConfigurationSaveModel.defaultConfiguration());
                //Configuration.getInstance().setupConfigInstance((ConfigurationSaveModel) modelAsJsonSaveService.readFromFile(new ConfigurationSaveModel()));
                return;
            }
            //--------------

            //Read speech file
            try {
                textToSpeechDictionary = modelAsJsonSaveService.readFromFile(new TextToSpeechDictionary());
            } catch (Exception e) {
                logger.error("Bad speech file, overwritten by default and loaded");
                logger.error(e.getMessage());
                logger.error(Arrays.toString(e.getStackTrace()));
                logger.error(e.toString());
                modelAsJsonSaveService.persistOldFile(new TextToSpeechDictionary());
                modelAsJsonSaveService.saveToFile(TextToSpeechDictionary.defaultModel());
                textToSpeechDictionary = modelAsJsonSaveService.readFromFile(new TextToSpeechDictionary());
            }
            //--------------

            //Discover Views and create FXMLLoaders
            var uri = Main.class.getResource("/Views").toURI();
            Path dirPath;

            try {
                dirPath = Paths.get(uri);
            } catch (FileSystemNotFoundException e) {
                // If this is thrown, then it means that we are running the JAR directly (example: not from an IDE)
                var env = new HashMap<String, String>();
                dirPath = FileSystems.newFileSystem(uri, env).getPath("/Views");
            }
            HashMap<String, FXMLLoader> loaders = new HashMap<>();

            Files.list(dirPath)
                    .forEach(p -> {
                        if (p.getFileName().toString().endsWith("View.fxml")) {
                            try {
                                if(!p.getFileName().toString().equals("DetachedTabView.fxml")){
                                    loaders.put(p.getFileName().toString(), new FXMLLoader(p.toUri().toURL()));
                                }

                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                        }
                    });

            FXMLLoader loaderMain = loaders.get(mainView);
            Scene scene = new Scene(loaderMain.load(), 1550, 750);
            //--------------


            //Controllers
            MainController mainController = loaderMain.getController();
            mainController.initSubScenes(loaders.values().stream().filter(loader -> loader != loaderMain).collect(Collectors.toList()));
            mainController.setPrimaryStage(primaryStage);

            HashMap<String, BaseController> controllerList = new HashMap<>();
            loaders.values().forEach(loader -> {
                BaseController controller = loader.getController();
                controllerList.put(controller.getClass().getSimpleName(), controller);
            });

            ConnectionController connectionController = (ConnectionController) controllerList.get(ConnectionController.class.getSimpleName());
//            try {
//                MapController mapController = (MapController) controllerList.get(MapController.class.getSimpleName());
//                final Projection projection = getParameters().getUnnamed().contains("wgs84")
//                        ? Projection.WGS_84 : Projection.WEB_MERCATOR;
//                mapController.initMapAndControls(projection);
//            }catch (Exception e) {
//                logger.error("Map init error", e);
//            }
            Configuration.getInstance().addListener(mainController);
            //--------------

            //Mapping sensors and commands to controllers
            Configuration.getInstance().setupApplicationConfig(controllerList.values());
            //--------------

            //IMessageParser setup
            if (Configuration.getInstance().PARSER_TYPE == MessageParserEnum.JSON) {
                messageParser = new JsonMessageParser();
            } else if (Configuration.getInstance().PARSER_TYPE == MessageParserEnum.STANDARD) {
                messageParser = new StandardMessageParser();
            }else if (Configuration.getInstance().PARSER_TYPE == MessageParserEnum.PROTOBUF) {
                messageParser = new ProtobufMessageParser();
            } else {
                messageParser = new StandardMessageParser();
            }
            messageParser.addListener(mainController);
            messageParser.addListener(controllerList.get(RAWDataController.class.getSimpleName()));
            messageParser.addListener(UIThreadManager.getInstance());
            //--------------

            //FrameSaveService setup
            frameSaveService = new FrameSaveService(Configuration.getInstance().FRAME_PATTERN.keySet());
            Configuration.getInstance().FRAME_PATTERN.forEach((key, value) -> frameSaveService.writeFileHeader(key, value));
            //--------------

            //UIUpdateEventEmitter setup
            uiUpdateEventEmitter = new UIUpdateEventEmitter();
            Configuration.getInstance().sensorRepository.getAllBasicSensors().values().forEach(s -> uiUpdateEventEmitter.addListener(s));
            //--------------

            //SchedulerThread setup
            //schedulerThread = new SchedulerThread(uiUpdateEventEmitter);
            //--------------

            //SerialPortManager setup
            SerialPortManager.getInstance().setMessageParser(messageParser);
            SerialPortManager.getInstance().setFrameSaveService(frameSaveService);
            SerialPortManager.getInstance().addListener(connectionController);
            SerialPortManager.getInstance().addListener(mainController);
            SerialPortManager.getInstance().addListener(AppStateLogger.getInstance());
            //--------------

            //Notification setup
            if (!Configuration.getInstance().DISCORD_TOKEN.equals("")) {
                notificationFormatService = new NotificationFormatDiscordService();
                notificationEvent = new NotificationDiscordEvent(notificationFormatService);
                INotification discord = new DiscordNotification(notificationEvent);
                discord.addListener(connectionController);
                notificationSendService = new NotificationSendService(discord, notificationFormatService);
                notificationThread = new NotificationThread(notificationSendService);
                notificationThread.setupSchedule(Configuration.getInstance().notificationSchedule);
                connectionController.injectNotification(notificationSendService, Configuration.getInstance().notificationMessageKeys, notificationThread);
                discord.setup();
            }
            //--------------

            //SpeechService setup
            ttsService = new TextToSpeechService(ruleValidationService, textToSpeechDictionary);

            //Add SpeechService as listener
            Configuration.getInstance().sensorRepository.getAllBasicSensors().forEach((s, sensor) -> {
                sensor.addListener(ttsService);
            });
            //--------------

            //Stage settings
            primaryStage.setTitle("SouRCE");
            primaryStage.setScene(scene);
            primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("Poliwrocket.png"))));
            primaryStage.heightProperty().addListener(mainController);
            primaryStage.widthProperty().addListener(mainController);
            primaryStage.setMaximized(true);
            primaryStage.setOnShown(windowEvent -> {
                UIThreadManager.getInstance().start();
                AppStateLogger.getInstance().start();
            });
            primaryStage.setOnCloseRequest(windowEvent -> {
                System.exit(0);
            });
            primaryStage.show();
            //--------------

        } catch (Exception e) {
            e.printStackTrace();
        }
       /* new Thread(() -> {
            while(true) {
                Set<Thread> threads = Thread.getAllStackTraces().keySet();
                System.out.printf("%-15s \t %-15s \t %-15s \t %s\n", "Name", "State", "Priority", "isDaemon");
                for (Thread t : threads) {
                    System.out.printf("%-15s \t %-15s \t %-15d \t %s\n", t.getName(), t.getState(), t.getPriority(), t.isDaemon());
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();*/
    }

    public static void main(String[] args) {
        launch(args);
    }

//    private void testMode() {
//
//            JsonObject raw = new Gson().fromJson("{\n" +
//                    "\t\"accel\": 100,\n" +
//                    "\t\"pressure\": 48,\n" +
//                    "\t\"satelites\": 12,\n" +
//                    "\t\"fix\": 1,\n" +
//                    "\t\"rocketWeight\": 24357,\n" +
//                    "\t\"rssi\": 57,\n" +
//                    "\t\"motherWeight\": 5357,\n" +
//                    "\t\"bat1\": 8.1,\n" +
//                    "\t\"bat2\": 8.2,\n" +
//                    "\t\"bat3\": 8.3,\n" +
//                    "\t\"bat4\": 8.0,\n" +
//                    "\t\"bat5\": 7.9,\n" +
//                    "\t\"bat6\": 7.8,\n" +
//                    "\t\"alt\": 0,\n" +
//                    "\t\"vel\": 12,\n" +
//                    "\t\"igni\": 0,\n" +
//                    "\t\"pilot\": 0,\n" +
//                    "\t\"apoge\": 1,\n" +
//                    "\t\"mainSchute\": 0,\n" +
//                    "\t\"lat\": 51.20395820043864,\n" +
//                    "\t\"lon\": 16.99416435080908\n" +
//                    "}",JsonObject.class);
//            new Thread(() -> {
//                try {
//                    Thread.sleep(4000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                while (true) {
//                    if(SerialPortManager.getInstance().isPortOpen()) {
//                        Frame frame = new Frame(raw.toString(), Instant.now());
//                        messageParser.parseMessage(frame);
//                        frameSaveService.saveFrameToFile(frame);
//
//                        for (String sensorKey: Configuration.getInstance().FRAME_PATTERN) {
//                            if(sensorKey.equals("lat")) {
//                                raw.addProperty(sensorKey,Double.parseDouble(raw.get(sensorKey).toString())+0.000025);
//                            } else if(sensorKey.equals("lon")) {
//                                raw.addProperty(sensorKey,Double.parseDouble(raw.get(sensorKey).toString())+0.000025);
//                            } else if(sensorKey.contains("bat")) {
//                                double r = ThreadLocalRandom.current().nextDouble(7.0,8.4);
//                                raw.addProperty(sensorKey,r);
//                            } else if(sensorKey.equals("accel")){
//                                if(Double.parseDouble(raw.get(sensorKey).toString()) < 380){
//                                    double r = ThreadLocalRandom.current().nextDouble(Double.parseDouble(raw.get(sensorKey).toString()),Double.parseDouble(raw.get(sensorKey).toString())+10);
//                                    raw.addProperty(sensorKey,r);
//
//                                }
//                            } else if(sensorKey.equals("alt")){
//                                if(Double.parseDouble(raw.get(sensorKey).toString()) < 3850){
//                                    double r = ThreadLocalRandom.current().nextDouble(Double.parseDouble(raw.get(sensorKey).toString()),Double.parseDouble(raw.get(sensorKey).toString())+20);
//                                    raw.addProperty(sensorKey,r);
//
//                                }
//                            }else if(sensorKey.equals("vel")){
//                                if(Double.parseDouble(raw.get(sensorKey).toString()) < 380){
//                                    double r = ThreadLocalRandom.current().nextDouble(Double.parseDouble(raw.get(sensorKey).toString()),Double.parseDouble(raw.get(sensorKey).toString())+5);
//                                    raw.addProperty(sensorKey,r);
//
//                                }
//                            } else if(sensorKey.equals("rocketWeight")){
//                                double r = ThreadLocalRandom.current().nextDouble(16000,36000);
//                                raw.addProperty(sensorKey,r);
//                            } else if(sensorKey.equals("motherWeight")){
//                                double r = ThreadLocalRandom.current().nextDouble(4000,11600);
//                                raw.addProperty(sensorKey,r);
//                            }else if(sensorKey.equals("rssi")){
//                                double r = ThreadLocalRandom.current().nextDouble(50,100);
//                                raw.addProperty(sensorKey,r);
//                            }else if(sensorKey.equals("apoge")){
//                                var r = ThreadLocalRandom.current().nextBoolean() ? 1.0 : 0;
//                                raw.addProperty(sensorKey,r);
//                            }else if(sensorKey.equals("igni")){
//                                var r = ThreadLocalRandom.current().nextBoolean() ? 1.0 : 0;
//                                raw.addProperty(sensorKey,r);
//                            }else if(sensorKey.equals("pilot")){
//                                var r = ThreadLocalRandom.current().nextBoolean() ? 1.0 : 0;
//                                raw.addProperty(sensorKey,r);
//                            }else if(sensorKey.equals("mainSchute")){
//                                var r = ThreadLocalRandom.current().nextBoolean() ? 1.0 : 0;
//                                raw.addProperty(sensorKey,r);
//                            }
//                        }
//
//                    }
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//    }

    private void testMode() {

        Random random = new Random();
        var thread = new Thread(() -> {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (true) {
                if (SerialPortManager.getInstance().isPortOpen()) {


                    for (Map.Entry<String, List<String>> frameKey : Configuration.getInstance().FRAME_PATTERN.entrySet()) {
                        StringBuilder raw = new StringBuilder(frameKey.getKey() + Configuration.getInstance().FRAME_DELIMITER);

                        for (String sensorKey : frameKey.getValue()) {
                            switch (sensorKey) {
                                default:
                                    raw.append(random.nextDouble() * 100);
                            }
                            raw.append(Configuration.getInstance().FRAME_DELIMITER);
                        }
//                        Frame frame = new Frame(raw.toString(), new byte[], Instant.now());
//                        messageParser.parseMessage(frame);
//                        frameSaveService.saveFrameToFile(frame);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
