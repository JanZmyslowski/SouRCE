package pl.edu.pwr.pwrinspace.poliwrocket.Model.MessageParser;

import javafx.beans.InvalidationListener;

import java.util.LinkedList;
import java.util.List;

public abstract class BaseMessageParser implements IMessageParser {

    protected String lastMessage;

    protected ParsingResultStatus parsingResultStatus;

    private final List<InvalidationListener> observers = new LinkedList<>();

    private final List<ISensorUpdate> sensorUpdates = new LinkedList<>();

    @Override
    public String getLastMessage() {
        return lastMessage;
    }

    @Override
    public ParsingResultStatus getParsingStatus() {
        return parsingResultStatus;
    }

    @Override
    public void parseMessage(Frame frame) {

        parsingResultStatus = ParsingResultStatus.PENDING;

        parseInternal(frame);
        if(parsingResultStatus == ParsingResultStatus.OK) {
            commitParsing();

        }
        clearUpdatesList();
        notifyObserver();
    }

    protected abstract void parseInternal(Frame frame);

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        this.observers.add(invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        this.observers.remove(invalidationListener);
    }

    private void notifyObserver(){
        this.observers.forEach(obs -> obs.invalidated(this));
    }

    private synchronized void commitParsing() {
        sensorUpdates.forEach(ISensorUpdate::execute);
    }

    private void clearUpdatesList() {
        sensorUpdates.clear();
    }

    protected void addSensorUpdate(ISensorUpdate sensorUpdate) {
        sensorUpdates.add(sensorUpdate);
    }
}
