package pl.edu.pwr.pwrinspace.poliwrocket.Model.Command;

import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.Content.ProtobufContent;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration.Configuration;

import java.util.List;

public class ProtobufCommand extends ProtobufBaseCommand<ProtobufContent> {

    @Override
    public byte[] getCommandValueAsBytes(boolean force) {
        var protobufDevice = Configuration.getInstance().protobufDeviceRepository.getDevice(value.getDevice());
        if(force) {
            return buildLoRaCommand(
                    protobufDevice.getDeviceId() | protobufDevice.getSudoMask(),
                    Configuration.getInstance().protobufSystemRepository.getSystem(value.getSystem()).getSystemDeviceId()
            );
        }

        return buildLoRaCommand(
                protobufDevice.getDeviceId(),
                Configuration.getInstance().protobufSystemRepository.getSystem(value.getSystem()).getSystemDeviceId()
        );
    }
}
