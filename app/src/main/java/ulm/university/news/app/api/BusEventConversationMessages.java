package ulm.university.news.app.api;

import java.util.List;

import ulm.university.news.app.data.ConversationMessage;

/**
 * This is a helper class which is used to send a list of conversation messages through the event bus.
 *
 * @author Matthias Mak
 */
public class BusEventConversationMessages {
    private List<ConversationMessage> conversationMessages;

    public BusEventConversationMessages(List<ConversationMessage> conversationMessages) {
        this.conversationMessages = conversationMessages;
    }

    public List<ConversationMessage> getConversationMessages() {
        return conversationMessages;
    }

    @Override
    public String toString() {
        return "BusEventConversationMessages{" +
                "conversationMessages=" + conversationMessages +
                '}';
    }
}
