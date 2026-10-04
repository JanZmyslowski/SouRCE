package pl.edu.pwr.pwrinspace.poliwrocket.Model.SerialPort;

import com.google.common.primitives.Bytes;
import gnu.io.NRSerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import javafx.beans.InvalidationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.ICommand;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.StandardCommand;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SerialPortManager implements SerialPortEventListener, ISerialPortManager {

    private final List<InvalidationListener> observers = new ArrayList<>();
    private final List<InvalidationListener> portStatusObservers = new ArrayList<>();

    private NRSerialPort serialPort;
    private String PORT_NAME = "COM3";
    private int DATA_RATE = 115200;
    private final Logger log = LoggerFactory.getLogger(SerialPortManager.class);
    private OutputStream outputStream;
    private InputStream inputStream;
    private SerialWriter serialWriter;
    private boolean isPortOpen = false;
    private FrameSaveService frameSaveService;
    private IMessageParser messageParser;
    private final Object LOCK = new Object();
    private String lastMessage = "";
    protected static String msgPrefix = Configuration.getInstance().MSG_PREFIX;

    private SerialPortManager() {
        if (getInstance() != null) {
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
        synchronized (LOCK) {
            this.messageParser = messageParser;
        }
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
        if (messageParser != null) {
            try {
                // otwieramy i konfigurujemy port
                serialPort = new NRSerialPort(PORT_NAME, DATA_RATE);
                serialPort.connect();
                if (serialPort.isConnected()) {
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
                log.warn(e.toString());
            } finally {
                lastMessage = "";
                notifyObserver();
                notifyPortStatusObserver();
            }
        } else {
            isPortOpen = serialPort.isConnected();
            notifyObserver();
            notifyPortStatusObserver();
            log.warn("IMessageParser not set");
        }
        if (frameSaveService == null) {
            log.warn("FrameSaveService not set");
        }
    }

    @Override
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.disconnect();
            isPortOpen = serialPort.isConnected();
            notifyObserver();
            notifyPortStatusObserver();
        }
    }

    @Override
    public void serialEvent(SerialPortEvent oEvent) {
        synchronized (LOCK) {
            if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                try {
                    Frame frame;
                    byte[] buffer;
                    if (Configuration.getInstance().BUFFER_SIZE != 0) {
                        log.info("reading with buffer size: {}", Configuration.getInstance().BUFFER_SIZE);
                        buffer = this.inputStream.readNBytes(Configuration.getInstance().BUFFER_SIZE);
                    } else {
                        buffer = new byte[512];
                        int length = 0;
                        while (this.inputStream.available() > 0) {

                            buffer[length] = (byte) this.inputStream.read();
                            length++;

                            if (length == 512) {
                                log.info("LENGTH IS 512");
                                return;
                            }

                            if (this.inputStream.available() == 0) {
                                Thread.sleep(1);
                            }
                        }
                        buffer = Arrays.copyOfRange(buffer, msgPrefix.length(), length);
                    }

                    byte crc = buffer[buffer.length - 1];
                    buffer = Arrays.copyOfRange(buffer, 0, buffer.length - 1);

                    if (serialWriter.getMessageCRC(buffer) != null && serialWriter.getMessageCRC(buffer)[0] == crc) {
                        log.info("CRC MATCH");
                        log.info("DATA LENGTH: {}", buffer.length);
                        frame = new Frame(buffer, Instant.now());

                        messageParser.parseMessage(frame);
                        if (frameSaveService != null) {
                            if (frame.getFormattedContent() == null) {
                                frame.setFormattedContent(frame.getStringContent());
                            }
                            frameSaveService.saveFrameToFile(frame);
                        }
                    } else {
                        log.info("CRC MISMATCH");
                    }
                } catch (IOException e) {
                    log.error(e.getMessage());
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                    throw new RuntimeException(e);
                }

            }
        }
    }

    public void write(String message) {
        System.out.println(message);
        if (serialWriter == null) {
            log.warn("Not connected");
            return;
        }
        log.info("Written: {}", message);
        serialWriter.send(message);
        this.lastMessage = message;
        notifyObserver();
    }

    public void write(ICommand command) {
        System.out.println(command.getCommandValueAsString());
        if (serialWriter == null) {
            log.warn("Not connected");
            return;
        }
        var msg = command.getCommandValueAsString() + '\n';
        log.info("Written command: {}", msg);
        log.info("Force: {}", Configuration.getInstance().isForceCommandsActive());
        if (command.getClass().isAssignableFrom(StandardCommand.class)) {
            writeWithoutCRC(command.getCommandValueAsString());
        } else {
            serialWriter.send(command.getCommandValueAsBytes(Configuration.getInstance().isForceCommandsActive()));
        }
        this.lastMessage = msg;
        notifyObserver();
    }

    public void writeWithoutCRC(String message) {
        if (serialWriter == null) {
            log.warn("Not connected");
            return;
        }
        log.info("Written: {}", message);
        serialWriter.sendWithoutCRC(message);
        this.lastMessage = message;
        notifyObserver();
    }

    @Override
    public String getLastSend() {
        return this.lastMessage;
    }

    public static class SerialWriter implements Runnable {
        private final OutputStream out;
        private final BlockingQueue<byte[]> messageQueue = new LinkedBlockingQueue<>();
        private static final Logger logger = LoggerFactory.getLogger(SerialWriter.class);

        public SerialWriter(OutputStream out) {
            this.out = out;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    byte[] msg = messageQueue.take();
                    out.write(msg);
                    logger.info("Written msg: {}", msg);
                } catch (InterruptedException e) {
                    logger.error("Writer thread interrupted: {}", e.getMessage());
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    logger.error("Error writing to serial port: {}", e.getMessage());
                }
            }
        }

        public void send(String msg) {
            send(msg.getBytes());
        }

        public void send(byte[] msg) {
            var finalMsg = getMessageWithPrefixAndCRC(msg);
            logger.info("added crc and prefix");
            messageQueue.add(finalMsg);
        }

        public void sendWithoutCRC(String msg) {
            sendWithoutCRC(msg.getBytes());
        }

        public void sendWithoutCRC(byte[] msg) {
            messageQueue.add(msg);
        }

        public byte[] getMessageCRC(byte[] msg) {
            int messageCounter = 0;
            for (byte msgByte : msg) {
                messageCounter += msgByte;
            }

            return new byte[]{(byte) (messageCounter % 512)};
        }

        public byte[] getMessageWithPrefixAndCRC(byte[] msg) {
            return Bytes.concat(SerialPortManager.msgPrefix.getBytes(), msg, getMessageCRC(msg));
        }
    }
}
