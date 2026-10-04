package pl.edu.pwr.pwrinspace.poliwrocket.Thred.UI;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration.Configuration;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.MessageParser.IMessageParser;
import pl.edu.pwr.pwrinspace.poliwrocket.Provider.UITaskProvider;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UIThreadManager implements InvalidationListener {

    private static final Logger logger = LoggerFactory.getLogger(UIThreadManager.class);

    private final Queue<Runnable> activeRunnableQueue = new ConcurrentLinkedQueue<>();
    private UIRunnable waitingNormalRunnable = new UIRunnable();
    private UIRunnable waitingImmediateOnOKRunnable = new UIRunnable();
    private UIRunnable waitingImmediateRunnable = new UIRunnable();

    private final Timer timerRunner = new Timer();

    private UIThreadManager() {
        if (UIThreadManager.Holder.INSTANCE != null) {
            throw new IllegalStateException("Singleton already constructed");
        }
    }

    private static class Holder {
        private static final UIThreadManager INSTANCE = new UIThreadManager();
    }

    public static UIThreadManager getInstance() {
        return UIThreadManager.Holder.INSTANCE;
    }

    public void start() {
        timerRunner.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                activeRunnable(waitingNormalRunnable);
                waitingNormalRunnable = new UIRunnable();
            }
        },new Date(new Date().getTime() + 2000),1000 / Configuration.getInstance().FPS);
    }

    private synchronized void run() {
        Platform.runLater(() -> {
            UITaskProvider.getInstance().runTasks();
            while (!activeRunnableQueue.isEmpty()) {
                var task = activeRunnableQueue.poll();
                if(task != null) {
                    task.run();
                }
            }
        });
        Platform.requestNextPulse();
    }

    public void addNormal(InterfaceUpdateTask task) {
        waitingNormalRunnable.addTask(task);
    }

    public void addImmediate(InterfaceUpdateTask task) {
        waitingImmediateRunnable.addTask(task);
    }

    public void addImmediateOnOK(InterfaceUpdateTask task) {
        waitingImmediateOnOKRunnable.addTask(task);
    }

    private void activeRunnable(UIRunnable ...uiRunnable) {
        activeRunnableQueue.addAll(Arrays.asList(uiRunnable));
        run();
    }

    @Override
    public void invalidated(Observable observable) {
        if(observable instanceof  IMessageParser) {
            var parser = (IMessageParser) observable;
            switch (parser.getParsingStatus()) {
                case OK:
                    activeRunnable(waitingImmediateRunnable, waitingImmediateOnOKRunnable);
                    waitingImmediateRunnable = new UIRunnable();
                    waitingImmediateOnOKRunnable = new UIRunnable();
                    break;
                case ERROR:
                    activeRunnable(waitingImmediateRunnable);
                    waitingImmediateRunnable = new UIRunnable();
                    waitingImmediateOnOKRunnable = new UIRunnable();
                    break;
                case PENDING:
                    logger.warn("Invalidated while pending. Check your code.");
                    break;
                default:
                    logger.error("Unrecognized parsing status: {}",parser.getParsingStatus());
                    break;
            }
        }
    }
}
