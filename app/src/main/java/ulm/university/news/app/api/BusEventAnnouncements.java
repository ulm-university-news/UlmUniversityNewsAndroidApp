package ulm.university.news.app.api;

import java.util.List;

import ulm.university.news.app.data.Announcement;

/**
 * This is a helper class which is used to send a list of announcements through the event bus.
 *
 * @author Matthias Mak
 */
public class BusEventAnnouncements {
    private List<Announcement> announcements;

    public BusEventAnnouncements(List<Announcement> announcements) {
        this.announcements = announcements;
    }

    public List<Announcement> getAnnouncements() {
        return announcements;
    }

    @Override
    public String toString() {
        return "BusEventAnnouncements{" +
                "announcements=" + announcements +
                '}';
    }
}
