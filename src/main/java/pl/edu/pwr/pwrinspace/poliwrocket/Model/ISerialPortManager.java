package pl.edu.pwr.pwrinspace.poliwrocket.Model;

public interface ISerialPortManager {
    void initialize();
    void close();
    void initialize(String portName, int dataRate);

}
