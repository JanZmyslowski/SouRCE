package pl.edu.pwr.pwrinspace.poliwrocket.Service.Save;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.BaseSaveModel;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.*;

import java.io.*;

public class ModelAsJsonSaveService {

    private static final Logger logger = LoggerFactory.getLogger(ModelAsJsonSaveService.class);
    private final RuntimeTypeAdapterFactory<Sensor> sensorAdapterFactory = RuntimeTypeAdapterFactory.of(Sensor.class, "type")
            .registerSubtype(Sensor.class, "Sensor")
            .registerSubtype(FillingLevelSensor.class, "FillingLevelSensor")
            .registerSubtype(TareSensor.class, "TareSensor")
            .registerSubtype(AlertSensor.class, "AlertSensor")
            .registerSubtype(TanwiarzSensor.class, "TanwiarzSensor")
            .registerSubtype(PoteznyTanwiarzSensor.class, "PoteznyTanwiarzSensor")
            .registerSubtype(ByteSensor.class, "ByteSensor");

    public void saveToFile(BaseSaveModel configuration) {
        String configContent = new Gson().newBuilder().registerTypeAdapterFactory(sensorAdapterFactory).excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().disableHtmlEscaping().create().toJson(configuration);
        File configFile = new File(configuration.getPath() + configuration.getFileName());

        try (FileWriter configWriter = new FileWriter(configFile)) {
            configWriter.write(configContent);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void persistOldFile(BaseSaveModel config) {
        File configFile = new File(config.getPath() + config.getFileName());
        File copy = new File(config.getPath()  + config.getPersistPrefix() + config.getFileName());
        try (FileInputStream fis = new FileInputStream(configFile);
           FileOutputStream fos = new FileOutputStream(copy)) {
            int len;
            byte[] buffer = new byte[4096];
            while ((len = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public BaseSaveModel readFromFile(BaseSaveModel config) throws Exception {
        File configFile = new File(config.getPath() + config.getFileName());

        try (JsonReader reader = new JsonReader(new FileReader(configFile))) {
            config = new Gson().newBuilder().registerTypeAdapterFactory(sensorAdapterFactory).excludeFieldsWithoutExposeAnnotation().create().fromJson(reader, config.getClass());
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new Exception(e.getMessage());
        }

        return config;
    }
}
