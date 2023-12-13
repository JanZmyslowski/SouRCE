package pl.edu.pwr.pwrinspace.poliwrocket.Model.MessageParser;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProtobufMessageParser extends BaseMessageParser {

    private static final Logger logger = LoggerFactory.getLogger(ProtobufMessageParser.class);

    private static final String classPathBase =
            FrameProtos.getDescriptor().getOptions().getJavaPackage() + '.'
            + FrameProtos.getDescriptor().getOptions().getJavaOuterClassname() + "$";

    private void readFields(Message message) {
        message.getAllFields().forEach((fieldDescriptor, o) -> {
            if (fieldDescriptor.getJavaType() != Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                var sensorName = fieldDescriptor.getName();
                double value;
                if (fieldDescriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.BOOLEAN) {
                    value = (boolean)o ? 1.0 : 0;
                } else {
                    value = Double.parseDouble(o.toString());
                }

                addSensorUpdate(() ->  {
                    try {
                        Configuration.getInstance().sensorRepository.getSensorByName(sensorName).setValue(value);
                    } catch (Exception e) {
                        e.printStackTrace();
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
            Message message = null;
            String frameName = "";

            logger.info("Frame length: " + frame.getByteContent().length);
            for (Descriptors.Descriptor descriptor: FrameProtos.getDescriptor().getMessageTypes()) {
                try {
                    frameName = descriptor.getFullName();
                    var parserClass = Class.forName(classPathBase + descriptor.getFullName());
                    var pars = parserClass.getMethod("parseFrom", byte[].class);
                    logger.info("Try with: " + frameName);
                    message = (Message)pars.invoke(parserClass, frame.getByteContent());
                    this.parsingResultStatus = ParsingResultStatus.PENDING;
                    break;
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    setParsingError();
                }
            }
            frame.setFormattedContent(frameName + Configuration.getInstance().FRAME_DELIMITER + frame.getByteContent().toString());

            if(message != null) {
                readFields(message);
            }

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
