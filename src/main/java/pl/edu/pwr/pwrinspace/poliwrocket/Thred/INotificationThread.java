package pl.edu.pwr.pwrinspace.poliwrocket.Thred;

import pl.edu.pwr.pwrinspace.poliwrocket.Model.Schedule;

import java.util.List;

public interface INotificationThread extends Runnable {
    void sendNotifications(String message);
    void setupSchedule(List<Schedule> schedule);
    String getFormattedMessage(String messageKey);
}