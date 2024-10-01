package pl.edu.pwr.pwrinspace.poliwrocket.Model.SerialPort;

import com.google.common.primitives.Bytes;
import gnu.io.NRSerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import javafx.beans.InvalidationListener;
import org.slf4j.LoggerFactory;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.ICommand;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration.Configuration;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.MessageParser.Frame;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.MessageParser.IMessageParser;
import pl.edu.pwr.pwrinspace.poliwrocket.Service.Save.FrameSaveService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class SerialPortManager implements SerialPortEventListener, ISerialPortManager {

    private final List<InvalidationListener> observers = new ArrayList<>();
    private final List<InvalidationListener> portStatusObservers = new ArrayList<>();

    private NRSerialPort serialPort;
    private String PORT_NAME = "COM3";
    private int DATA_RATE = 115200;
    private final Logger log = Logger.getLogger(getClass().getName());
    private OutputStream outputStream;
    private InputStream inputStream;
    private SerialWriter serialWriter;
    private boolean isPortOpen = false;
    private FrameSaveService frameSaveService;
    private IMessageParser messageParser;
    private String lastMessage = "";
    protected static String msgPrefix = Configuration.getInstance().MSG_PREFIX;

    private SerialPortManager() {
        if (Holder.INSTANCE != null) {
            throw new IllegalStateException("Singleton already constructed");
        }
    }

    public static SerialPortManager getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        observers.add(invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        observers.remove(invalidationListener);
    }

    @Override
    public void setFrameSaveService(FrameSaveService frameSaveService) {
        this.frameSaveService = frameSaveService;
    }

    @Override
    public void addPortStatusListener(InvalidationListener invalidationListener) {
        this.portStatusObservers.add(invalidationListener);
    }

    private void notifyObserver() {
        for (InvalidationListener obs : observers) {
            obs.invalidated(this);
        }
    }

    private void notifyPortStatusObserver() {
        for (InvalidationListener obs : portStatusObservers) {
            obs.invalidated(this);
        }
    }

    private static class Holder {
        private static final SerialPortManager INSTANCE = new SerialPortManager();
    }

    @Override
    public void setMessageParser(IMessageParser messageParser) {
        this.messageParser = messageParser;
    }

    @Override
    public void initialize(String portName, int dataRate) {
        this.PORT_NAME = portName;
        this.DATA_RATE = dataRate;
        this.initialize();
    }

    @Override
    public boolean isPortOpen() {
        return this.isPortOpen;
    }

    @Override
    public void initialize() {
        if(messageParser != null) {
            try {
                // otwieramy i konfigurujemy port
                serialPort = new NRSerialPort(PORT_NAME, DATA_RATE);
                serialPort.connect();
                if(serialPort.isConnected()) {
                    // strumień wejścia
                    inputStream = serialPort.getInputStream();

                    //strumień wyjścia
                    outputStream = serialPort.getOutputStream();
                    serialWriter = new SerialWriter(outputStream);
                    Thread writerThread = new Thread(serialWriter);
                    writerThread.setDaemon(true);
                    writerThread.start();

                    // dodajemy słuchaczy zdarzeń
                    serialPort.addEventListener(this);
                    serialPort.notifyOnDataAvailable(true);
                } else {
                    try {
                        serialPort.disconnect();
                    } catch (NullPointerException e) {
                        serialPort = new NRSerialPort(PORT_NAME, DATA_RATE);
                    }
                }
                isPortOpen = serialPort.isConnected();
            } catch (Exception e) {
                isPortOpen = serialPort.isConnected();
                log.log(Level.WARNING,e.toString());
            } finally {
                lastMessage = "";
                notifyObserver();
                notifyPortStatusObserver();
                log.log(Level.INFO, "Serialport status: {0}", isPortOpen);
            }
        } else {
            isPortOpen = serialPort.isConnected();
            notifyObserver();
            notifyPortStatusObserver();
            log.log(Level.WARNING,"IMessageParser not set");
            log.log(Level.INFO, "Serialport status: {0}", isPortOpen);
        }
        if(frameSaveService == null) {
            log.log(Level.WARNING,"FrameSaveService not set");
        }
    }

    @Override
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.disconnect();
            log.log(Level.INFO, "Serialport closed.");
            isPortOpen = serialPort.isConnected();
            notifyObserver();
            notifyPortStatusObserver();
        }
    }

    @Override
    public void serialEvent(SerialPortEvent oEvent) {
        synchronized (messageParser) {
            if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                try {
                    Frame frame;
                    byte[] buffer;
                    if(Configuration.getInstance().BUFFER_SIZE != 0) {
                        buffer = this.inputStream.readNBytes(Configuration.getInstance().BUFFER_SIZE);
                    } else {
                        buffer = new byte[256];
                        int length = 0;
                        while(this.inputStream.available() > 0) {
                            buffer[length] = (byte)this.inputStream.read();
                            length++;

                            if(length == 256) {
                                return;
                            }

                            if(this.inputStream.available() == 0) {
                                Thread.sleep(1);
                            }
                        }
                        buffer = Arrays.copyOfRange(buffer, msgPrefix.length(), length);
                    }

                    frame = new Frame(buffer, Instant.now());

                    messageParser.parseMessage(frame);
                    if(frameSaveService != null) {
                        if(frame.getFormattedContent() == null) {
                            frame.setFormattedContent(frame.getStringContent());
                        }
                        frameSaveService.saveFrameToFile(frame);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    public void write(String message) {
        if(serialWriter == null) {
            log.log(Level.WARNING, "Not connected");
            return;
        }
        log.log(Level.INFO, "Written: {0}", message);
        serialWriter.send(message);
        this.lastMessage = message;
        notifyObserver();
    }

    public void write(ICommand command) {
        if(serialWriter == null) {
            log.log(Level.WARNING, "Not connected");
            return;
        }
        var msg = command.getCommandValueAsString() + '\n';
        log.log(Level.INFO, "Written: {0}", msg);
        serialWriter.send(command.getCommandValueAsBytes(Configuration.getInstance().isForceCommandsActive()));
        this.lastMessage = msg;
        notifyObserver();
    }

    @Override
    public String getLastSend() {
        return this.lastMessage;
    }

    public static class SerialWriter implements Runnable {
        OutputStream out;

        private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SerialWriter.class);

        public SerialWriter(OutputStream out) {
            this.out = out;
        }

        @Override
        public void run() {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }

        public void send(String msg) {
            send(msg.getBytes());
        }

        public void send(byte[] msg) {
            try {
                //out.write(msg);
                var finalMsg = getMessageWithPrefixAndCRC(msg);
                out.write(finalMsg);
                logger.info("Written msg: {}",msg);
                logger.info("Written with prefix and crc: {}",finalMsg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private byte[] getMessageCRC(byte[] msg) {
            Integer messageCounter = 0;
            for (byte msgByte : msg) {
                messageCounter += msgByte;
            }

            return new byte[]{ (byte)(messageCounter % 256) };
        }

        private byte[] getMessageWithPrefixAndCRC(byte[] msg) {
            return Bytes.concat(SerialPortManager.msgPrefix.getBytes(), msg, getMessageCRC(msg));
        }
    }
}
