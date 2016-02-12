package ulm.university.news.app.api;

import java.util.List;

import ulm.university.news.app.data.Moderator;

/**
 * This is a helper class which is used to send a list of moderators through the event bus.
 *
 * @author Matthias Mak
 */
public class BusEventModerators {
    private List<Moderator> moderators;

    public BusEventModerators(List<Moderator> moderators) {
        this.moderators = moderators;
    }

    public List<Moderator> getModerators() {
        return moderators;
    }

    @Override
    public String toString() {
        return "BusEventModerators{" +
                "moderators=" + moderators +
                '}';
    }
}
