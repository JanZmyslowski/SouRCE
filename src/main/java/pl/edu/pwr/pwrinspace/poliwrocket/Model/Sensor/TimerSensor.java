package pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor;

import com.google.gson.annotations.Expose;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.Command;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.ProtobufCommand;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.SerialPort.SerialPortManager;

import java.util.Timer;
import java.util.TimerTask;

public class TimerSensor extends Sensor {
    @Expose private int abortTime;
    @Expose private Command<ProtobufCommand> command;
    @Expose private int controlTime;
    private Timer timer;
    private TimerTask mainTask;
    private TimerTask controlTask;

    private void resetTimer(long timeToAbort) {

        cancelTimer();
        timer = new Timer();

        controlTask = new TimerTask() {
            @Override
            public void run() {
                if (getValue() <= abortTime) {
                    sendVoidCommand();
                }
            }
        };

        mainTask = new TimerTask() {
            @Override
            public void run() {
                if (getValue() <= abortTime) {
                    sendVoidCommand();
                    timer.schedule(controlTask, controlTime);
                }
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

    public void cancelTimer() {
        if (mainTask != null) {
            mainTask.cancel();
        }

        if (controlTask != null) {
            controlTask.cancel();
        }

        if (timer != null) {
            timer.cancel();
        }
    }
}
