package ulm.university.news.app.api;

import java.util.List;

import ulm.university.news.app.data.Reminder;

/**
 * This is a helper class which is used to send a list of reminders through the event bus.
 *
 * @author Matthias Mak
 */
public class BusEventReminders {
    private List<Reminder> reminders;

    public BusEventReminders(List<Reminder> reminders) {
        this.reminders = reminders;
    }

    public List<Reminder> getReminders() {
        return reminders;
    }

    @Override
    public String toString() {
        return "BusEventReminders{" +
                "reminders=" + reminders +
                '}';
    }
}
