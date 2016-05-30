package ulm.university.news.app.api;

import ulm.university.news.app.data.Conversation;

/**
 * This is a helper class which is used to send a updated conversation through the event bus.
 *
 * @author Matthias Mak
 */
public class BusEventConversationChange {
    private Conversation conversation;

    public BusEventConversationChange(Conversation conversation) {
        this.conversation = conversation;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    @Override
    public String toString() {
        return "BusEventConversationChange{" +
                "conversation=" + conversation +
                '}';
    }
}
