package ulm.university.news.app.api;

import java.util.List;

import ulm.university.news.app.data.Conversation;

/**
 * This is a helper class which is used to send a list of conversations through the event bus.
 *
 * @author Matthias Mak
 */
public class BusEventConversations {
    private List<Conversation> conversations;

    public BusEventConversations(List<Conversation> conversations) {
        this.conversations = conversations;
    }

    public List<Conversation> getConversations() {
        return conversations;
    }

    @Override
    public String toString() {
        return "BusEventConversations{" +
                "conversations=" + conversations +
                '}';
    }
}
