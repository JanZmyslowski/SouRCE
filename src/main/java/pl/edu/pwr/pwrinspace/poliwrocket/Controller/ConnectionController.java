package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.tilesfx.addons.Switch;
import gnu.io.NRSerialPort;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.ICommand;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration.Configuration;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Notification.INotification;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.ISensor;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.SerialPort.ISerialPortManager;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.SerialPort.SerialPortManager;
import pl.edu.pwr.pwrinspace.poliwrocket.Service.Notification.NotificationSendService;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.Notification.INotificationThread;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.ThreadName;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.UI.UIThreadManager;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConnectionController extends BaseButtonSensorController {

    @FXML
    private JFXComboBox<String> serialPorts;
    @FXML
    protected JFXButton connectionButton;
    @FXML
    private JFXTextField baudRate;
    @FXML
    protected Label connectionStatus;
    @FXML
    private JFXComboBox<String> notifications;
    @FXML
    protected JFXButton sendNotification;
    @FXML
    protected Label notificationStatus;
    @FXML
    protected JFXButton threadButton;
    @FXML
    protected Label threadStatus;
    @FXML
    private Gauge signal;
    @FXML
    private JFXComboBox<ICommand> serialMessages;
    @FXML
    protected JFXButton sendSerialMessage;
    @FXML
    private Switch sudoToggle;

    private final ObservableList<String> availableSerialPorts = FXCollections.observableArrayList();
    private final ObservableList<String> availableNotifications = FXCollections.observableArrayList();
    private final ObservableList<ICommand> availableMessages = FXCollections.observableArrayList();
    private NotificationSendService notificationSendService;
    private INotificationThread notificationThreadRunnable;
    private Thread notificationThread;

    @Override
    @FXML
    protected void initialize() {
        serialSetup();
        sendNotification.setDisable(true);
        notifications.setDisable(true);
        threadButton.setDisable(true);

        serialPorts.setOnMouseClicked(mouseEvent -> serialSetup());

        connectionButton.setOnMouseClicked(mouseEvent -> {
            executorService.execute(() -> {
                if (availableSerialPorts.isEmpty()) {
                    Platform.runLater(() -> connectionStatus.setText("Error: no ports"));
                } else {
                    if (!SerialPortManager.getInstance().isPortOpen()) {
                        Platform.runLater(() -> {
                            connectionStatus.setText("Connecting");
                            baudRate.setDisable(true);
                            serialPorts.setDisable(true);
                        });
                        SerialPortManager.getInstance().initialize(serialPorts.getValue(), Integer.parseInt(baudRate.getText()));
                    } else {
                        Platform.runLater(() -> {
                            connectionStatus.setText("Disconnecting");
                            sendSerialMessage.setDisable(true);
                        });
                        SerialPortManager.getInstance().close();
                    }
                }
            });
        });

        sendNotification.setOnMouseClicked(mouseEvent -> {
            executorService.execute(() -> {
                if (notificationSendService != null) {
                    notificationSendService.sendNotification(notifications.getValue());
                } else {
                    Platform.runLater(() -> {
                        sendNotification.setDisable(true);
                        notificationStatus.setText("Service error");
                    });
                }
            });
        });

        threadButton.setOnMouseClicked(mouseEvent -> {
            executorService.execute(() -> {
                if (notificationThreadRunnable != null && (notificationThread == null || !notificationThread.isAlive())) {
                    notificationThread = new Thread(notificationThreadRunnable, ThreadName.DISCORD_NOTIFICATION.getName());
                    notificationThread.setDaemon(true);
                    notificationThread.start();
                    Platform.runLater(() -> threadStatus.setText("Running"));
                } else if (notificationThread != null && notificationThread.isAlive()) {
                    notificationThread.interrupt();
                    Platform.runLater(() -> threadStatus.setText("Interrupted"));
                }
            });
        });

        sendSerialMessage.setOnMouseClicked(mouseEvent ->
                executorService.execute(() -> SerialPortManager.getInstance().write(serialMessages.getValue()))
        );

        sudoToggle.setActive(Configuration.getInstance().isForceCommandsActive());
        sudoToggle.setOnMouseClicked(actionEvent -> {
            Configuration.getInstance().setForceCommandsActive(sudoToggle.isActive());
        });
    }

    public void injectNotification(NotificationSendService notificationSendService, List<String> notificationsList, INotificationThread notificationThreadRunnable) {
        this.notificationSendService = notificationSendService;
        this.availableNotifications.clear();
        this.availableNotifications.addAll(notificationsList);
        notifications.setItems(availableNotifications);
        if (!notificationsList.isEmpty()) {
            notifications.setValue(notificationsList.get(0));
        }
        this.notificationThreadRunnable = notificationThreadRunnable;
    }

    @Override
    public void assignsCommands(Collection<ICommand> messagesList) {
        Platform.runLater(() -> {
            this.availableMessages.clear();
            this.availableMessages.addAll(messagesList);
            serialMessages.setItems(availableMessages);
            if (!messagesList.isEmpty()) {
                serialMessages.setValue(availableMessages.get(0));
            }
        });
    }
    private void serialSetup() {
        try {
            final Set<String> ports = executorService.submit(NRSerialPort::getAvailableSerialPorts).get(30, TimeUnit.SECONDS);
            Platform.runLater(() -> {
                availableSerialPorts.clear();
                availableSerialPorts.addAll(ports);
                serialPorts.setItems(availableSerialPorts);
                serialPorts.setValue(!availableSerialPorts.isEmpty() ? availableSerialPorts.get(0) : "No ports available");
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void setUIBySensors() {
        for (ISensor sensor : sensors) {
            if (sensor.getDestination().equals(signal.getId())) {
                signal.setVisible(true);
                signal.setUnit(sensor.getUnit());
            } else {
                logger.error("Wrong UI binding - destination not found: {}",sensor.getDestination());
            }
        }
    }

    @Override
    protected void buildVisualizationMap() {}

    @Override
    public void invalidated(Observable observable) {
        if (observable instanceof ISerialPortManager) {

            Platform.runLater(() -> {
                if (((ISerialPortManager) observable).isPortOpen()) {
                    connectionStatus.setText("Connected");
                    sendSerialMessage.setDisable(false);
                    baudRate.setDisable(true);
                    serialPorts.setDisable(true);
                } else {
                    if (connectionStatus.getText().equals("Connecting")) {
                        connectionStatus.setText("Failed");
                    } else {
                        connectionStatus.setText("Disconnected");
                    }
                    baudRate.setDisable(false);
                    serialPorts.setDisable(false);
                    sendSerialMessage.setDisable(true);
                }
            });
        } else if (observable instanceof INotification) {
            boolean status = ((INotification) observable).isConnected();
            Platform.runLater(() -> {
                sendNotification.setDisable(!status);
                notifications.setDisable(!status);
                threadButton.setDisable(!status);
                notificationStatus.setText(status ? "Connected" : "Not connected");
                threadStatus.setText(status ? "Not running" : "Not enabled");
            });
        } else if (observable instanceof ISensor) {
            var sensor = ((ISensor) observable);
            UIThreadManager.getInstance().addNormal(() -> signal.setValue(Math.round((sensor.getValue() - sensor.getMinRange())/(sensor.getMaxRange()-sensor.getMinRange())*1000)/1000.0));
        }
    }
}
