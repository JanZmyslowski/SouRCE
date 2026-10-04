package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import com.jfoenix.controls.JFXButton;
import eu.hansolo.tilesfx.addons.Switch;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.ICommand;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.SerialPort.SerialPortManager;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.CountdownThread;
import pl.edu.pwr.pwrinspace.poliwrocket.Thred.ThreadName;

public class StartControlController extends BaseButtonSensorController {
    @FXML
    private Switch safeSwitch1;
    @FXML
    private Switch safeSwitch2;
    @FXML
    protected JFXButton voidButton;
    @FXML
    protected JFXButton fireButton;
    @FXML
    protected Label countdownTimer;

    private Thread countdownThread;
    private CountdownThread countdownTime;

    @FXML
    @Override
    protected void initialize() {
        super.initialize();

        buildVisualizationMap();

        this.countdownTime = new CountdownThread();
        this.countdownTime.addListener(this);
        this.fireButton.setDisable(true);
        this.safeSwitch2.setDisable(true);
        this.safeSwitch1.setOnMouseClicked(actionEvent -> executorService.execute(() -> {
            if (this.safeSwitch1.isActive()) {
                this.safeSwitch2.setDisable(false);
            } else {
                this.fireButton.setDisable(true);
                this.safeSwitch2.setActive(false);
                this.safeSwitch2.setDisable(true);
            }
            this.checkReset();
        }));
        this.safeSwitch2.setOnMouseClicked(actionEvent -> executorService.execute(() -> {
            if (this.safeSwitch1.isActive() && this.safeSwitch2.isActive()) {
                this.fireButton.setDisable(false);
            } else {
                this.fireButton.setDisable(true);
                this.safeSwitch1.setActive(false);
                this.safeSwitch2.setDisable(true);
            }
            this.checkReset();
        }));
        this.fireButton.setOnMouseClicked(mouseEvent -> executorService.execute(() -> {
            if (!(this.countdownTime == null || this.countdownThread != null && this.countdownThread.isAlive())) {
                this.countdownThread = new Thread((Runnable)this.countdownTime, ThreadName.COUNTDOWN.getName());
                this.countdownThread.setDaemon(true);
                this.countdownThread.start();
                this.commands.forEach(c -> {
                    if (c.getCommandTriggerKey().equals(this.fireButton.getId())) {
                        SerialPortManager.getInstance().write((ICommand)c);
                    }
                });
            } else if (this.countdownTime != null && this.countdownThread != null && this.countdownThread.isAlive()) {
                this.countdownTime.resetCountdown();
                this.commands.forEach(c -> {
                    if (c.getCommandTriggerKey().equals(this.fireButton.getId())) {
                        SerialPortManager.getInstance().write((ICommand)c);
                    }
                });
                this.countdownTime.makeCanRun();
            }
        }));
    }

    @Override
    public void invalidated(Observable observable) {
        Platform.runLater(() -> this.countdownTimer.setText(((CountdownThread) observable).getFormattedTimeResult()));
    }

    private void checkReset() {
        if (!this.safeSwitch1.isActive() || !this.safeSwitch2.isActive()) {
            this.countdownTime.resetCountdown();
        }
    }

    @Override
    protected void setUIBySensors() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
