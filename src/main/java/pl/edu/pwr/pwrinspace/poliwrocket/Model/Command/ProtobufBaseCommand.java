package pl.edu.pwr.pwrinspace.poliwrocket.Model.Command;

import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.Content.BaseProtobufContent;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.MessageParser.FrameProtos;

import java.util.List;

public abstract class ProtobufBaseCommand<T extends BaseProtobufContent> extends Command<T> {

    protected byte[] buildLoRaCommand(int loraDevId, int sysDevId) {
        var builder = FrameProtos.LoRaCommand.newBuilder()
                .setLoraDevId(loraDevId)
                .setSysDevId(sysDevId)
                .setCommand(Integer.decode(value.getCommand()));

        int payloadAsNumber = 0;
        if(payload != null && !payload.isEmpty()) {
            try {
                if(payload.contains(".") || payload.contains(",")) {
                    payloadAsNumber = (int) Double.parseDouble(payload);
                } else {
                    payloadAsNumber = Integer.parseInt(payload);
                }
            } catch (NumberFormatException ignored) {

            }
        }

        builder.setPayload(payloadAsNumber);

        return builder.build().toByteArray();
    }

    @Override
    public String getCommandValueAsString() {
        var description = getCommandDescription();
        if(description == null || description.isEmpty()) {
            description = getCommandTriggerKey();
        }

        return "[" + description + "]:" + super.getCommandValueAsString();
    }
}
