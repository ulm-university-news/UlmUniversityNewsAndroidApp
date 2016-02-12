package ulm.university.news.app.api;

import java.util.List;

import ulm.university.news.app.data.Channel;

/**
 * This is a helper class which is used to send a list of channels through the event bus.
 *
 * @author Matthias Mak
 */
public class BusEventChannels {
    private List<Channel> channels;

    public BusEventChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    @Override
    public String toString() {
        return "BusEventChannels{" +
                "channels=" + channels +
                '}';
    }
}
