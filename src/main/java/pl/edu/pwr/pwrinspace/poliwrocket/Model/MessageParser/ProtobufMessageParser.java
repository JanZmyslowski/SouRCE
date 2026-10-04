package pl.edu.pwr.pwrinspace.poliwrocket.Model.MessageParser;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class ProtobufMessageParser extends BaseMessageParser {

    private static final Logger logger = LoggerFactory.getLogger(ProtobufMessageParser.class);

    private static final String classPathBase =
            FrameProtos.getDescriptor().getOptions().getJavaPackage() + '.'
            + FrameProtos.getDescriptor().getOptions().getJavaOuterClassname() + "$";

    private void readFields(Message message) {
        message.getAllFields().forEach((fieldDescriptor, o) -> {
            if (fieldDescriptor.getJavaType() != JavaType.MESSAGE) {
                var sensorName = fieldDescriptor.getName();
                double value;
                if (fieldDescriptor.getJavaType() == JavaType.BOOLEAN) {
                    value = (boolean)o ? 1.0 : 0;
                } else {
                    value = Double.parseDouble(o.toString());
                }

                addSensorUpdate(() ->  {
                    try {
                        Configuration.getInstance().sensorRepository.getSensorByName(sensorName).setValue(value);
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                });
            } else {
                readFields((Message) o);
            }
        });
    }

    @Override
    protected void parseInternal(Frame frame) {
        try{
            Message message;
            String frameName;

            logger.info("Frame length: {}", frame.getByteContent().length);

            FrameProtos.LoRaFrame loRaFrame = FrameProtos.LoRaFrame.parseFrom(frame.getByteContent());
            switch (loRaFrame.getFrameCase()) {
                case MCB_FRAME:
                    message = loRaFrame.getMcbFrame();
                    frameName = "MCB_FRAME";
                    break;
                case TANWA_FRAME:
                    message = loRaFrame.getTanwaFrame();
                    frameName = "TANWA_FRAME";
                    break;
                default:
                    logger.error("Unknown frame type: {}", loRaFrame.getFrameCase());
                    setParsingError();
                    return;
            }
            frame.setFormattedContent(frameName + Configuration.getInstance().FRAME_DELIMITER + Arrays.toString(frame.getByteContent()));
            readFields(message);

            if (parsingResultStatus == ParsingResultStatus.PENDING) {
                parsingResultStatus = ParsingResultStatus.OK;
                lastMessage = "RECEIVED: OK " + frameName + " at " + getCurrentDate();
            }

        }catch (Exception e){
            logger.error(e.getMessage());
            logger.error("Protobuf parsing error");
            setParsingError();
        }
    }

    private void setParsingError() {
        lastMessage = "ERROR: Parsing message error at " + getCurrentDate();
        parsingResultStatus = ParsingResultStatus.ERROR;
    }

    private String getCurrentDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}
