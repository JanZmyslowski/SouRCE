package pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor;

import com.google.gson.annotations.Expose;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.Command;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.ProtobufCommand;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.SerialPort.SerialPortManager;

import java.util.*;

public class TimerSensor extends Sensor {

    @Expose
    private int abortTime;

    private final Timer timer = new Timer();

    private TimerTask mainTask;

    private TimerTask controlTask;

    @Expose
    private Command<ProtobufCommand> command;

    @Expose
    private int controlTime;

    private void resetTimer(long timeToAbort) {
        if (mainTask != null) {
            mainTask.cancel();
        }

        controlTask = new TimerTask() {
            @Override
            public void run() {
                if (getValue() <= abortTime) {
                    sendVoidCommand();
                    timer.schedule(controlTask, controlTime);
                }
            }
        };

        mainTask = new TimerTask() {
            @Override
            public void run() {
                sendVoidCommand();
                timer.schedule(controlTask, controlTime);
            }
        };

        long delay = Math.max(timeToAbort - abortTime, 100);

        timer.schedule(mainTask, delay);
    }

    private void sendVoidCommand() {
        SerialPortManager.getInstance().write(this.command);
    }

    @Override
    public void notifyObserver() {
        super.notifyObserver();
        resetTimer((long) this.getValue());
    }

    public void setAbortTime(int abortTime) {
        this.abortTime = abortTime;
    }

    public long getAbortTime() {
        return abortTime;
    }

    public void cancelTimer() {
        if (mainTask != null) {
            mainTask.cancel();
        }
    }
}
