package pl.edu.pwr.pwrinspace.poliwrocket.Model.Command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.Content.BaseProtobufContent;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.MessageParser.FrameProtos;

public abstract class ProtobufBaseCommand<T extends BaseProtobufContent> extends Command<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtobufBaseCommand.class);

    protected byte[] buildLoRaCommand(int loraDevId, int sysDevId) {
        var builder = FrameProtos.AppFrame.newBuilder()
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

        builder.build().toByteArray();

        LOGGER.info("Built LoRa command: loraDevId={}, sysDevId={}, command={}, payload={}", loraDevId, sysDevId, value.getCommand(), payloadAsNumber);
        FrameProtos.LoRaFrame mainFrame = FrameProtos.LoRaFrame.newBuilder()
                .setAppFrame(builder)
                .build();

        LOGGER.info("Lora type, {}", mainFrame.getFrameCase());

        return mainFrame.toByteArray();
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
