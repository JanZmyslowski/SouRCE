package pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration;

import com.google.gson.annotations.Expose;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.BaseSaveModel;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Notification.Schedule;
import java.util.LinkedList;
import java.util.List;

public class NotificationsConfig extends BaseSaveModel {
    @Expose public List<Schedule> notificationSchedule = new LinkedList<>();
    @Expose public List<String> notificationMessageKeys = new LinkedList<>();

    public NotificationsConfig() {
        super(Configuration.CONFIG_PATH, "NotificationConfig.json");
    }
}