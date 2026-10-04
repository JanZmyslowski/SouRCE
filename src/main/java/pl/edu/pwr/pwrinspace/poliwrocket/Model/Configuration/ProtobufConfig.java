package pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration;

import com.google.gson.annotations.Expose;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.BaseSaveModel;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Protobuf.ProtobufDeviceRepository;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Protobuf.ProtobufSystemRepository;

public class ProtobufConfig extends BaseSaveModel {
    @Expose public ProtobufDeviceRepository protobufDeviceRepository = new ProtobufDeviceRepository();
    @Expose public ProtobufSystemRepository protobufSystemRepository = new ProtobufSystemRepository();

    public ProtobufConfig() {
        super(Configuration.CONFIG_PATH, "ProtobufConfig.json");
    }
}