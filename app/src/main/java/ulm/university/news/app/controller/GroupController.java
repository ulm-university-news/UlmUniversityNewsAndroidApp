package ulm.university.news.app.controller;

import android.content.Context;

import java.util.List;
import java.util.Locale;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Conversation;
import ulm.university.news.app.data.Group;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.manager.database.GroupDatabaseManager;
import ulm.university.news.app.util.Util;

/**
 * This class provides static util methods regarding the group resource and subresources which are often used across
 * different activities.
 *
 * @author Matthias Mak
 */
public class GroupController {
    /** This classes tag for logging. */
    private static final String TAG = "GroupController";

    /**
     * Chooses the appropriate icon for the given group.
     *
     * @param group The group.
     * @return The resource id of the group icon.
     */
    public static int getGroupIcon(Group group, Context context) {
        int groupIcon;
        // Get current language.
        String language = Locale.getDefault().getLanguage();

        // Set appropriate channel icon.
        switch (group.getGroupType()) {
            case TUTORIAL:
                if (group.isGroupAdmin(Util.getInstance(context).getLocalUser().getId())) {
                    groupIcon = R.drawable.ic_t_admin;
                } else {
                    groupIcon = R.drawable.ic_t;
                }
                break;
            default:
                if (group.isGroupAdmin(Util.getInstance(context).getLocalUser().getId())) {
                    if (language.equals("de")) {
                        groupIcon = R.drawable.ic_a_admin;
                    } else {
                        groupIcon = R.drawable.ic_w_admin;
                    }
                } else {
                    if (language.equals("de")) {
                        groupIcon = R.drawable.ic_a;
                    } else {
                        groupIcon = R.drawable.ic_w;
                    }
                }
        }

        // If group is marked as deleted, set deleted icon.
        if (group.getDeleted() != null && group.getDeleted()) {
            groupIcon = R.drawable.ic_deleted;
        }
        return groupIcon;
    }

    /**
     * Chooses the appropriate icon for the given conversation.
     *
     * @param conversation The conversation.
     * @return The resource id of the conversation icon.
     */
    public static int getConversationIcon(Conversation conversation, Context context) {
        int conversationIcon;
        // Get current language.
        String language = Locale.getDefault().getLanguage();

        // Set appropriate channel icon.
        if (conversation.isAdmin(Util.getInstance(context).getLocalUser().getId())) {
            if (language.equals("de")) {
                conversationIcon = R.drawable.ic_konversation_admin;
            } else {
                conversationIcon = R.drawable.ic_conversation_admin;
            }
        } else {
            if (language.equals("de")) {
                conversationIcon = R.drawable.ic_konversation;
            } else {
                conversationIcon = R.drawable.ic_conversation;
            }
        }

        // If group is marked as closed, set closed icon.
        if (conversation.getClosed()) {
            conversationIcon = R.drawable.ic_conversation_closed;
        }
        return conversationIcon;
    }

    /**
     * Stores new conversations or updates existing ones in the database.
     *
     * @param context The current context.
     * @param conversations A list of conversations.
     * @param groupId The group id to which the conversations belong.
     * @return true if new conversations were stored.
     */
    public static boolean storeConversations(Context context, List<Conversation> conversations, int groupId) {
        ChannelDatabaseManager channelDBM = new ChannelDatabaseManager(context);
        GroupDatabaseManager groupDBM = new GroupDatabaseManager(context);
        boolean newConversations = false;

        if (!conversations.isEmpty()) {
            List<Conversation> conversationsDB = groupDBM.getConversations(groupId);
            boolean alreadyStored;

            // Store new conversations and update existing ones.
            for (Conversation conversation : conversations) {
                alreadyStored = false;
                for (Conversation conversationDB : conversationsDB) {
                    if (conversationDB.getId() == conversation.getId()) {
                        groupDBM.updateConversation(conversation);
                        alreadyStored = true;
                        break;
                    }
                }
                if (!alreadyStored) {
                    groupDBM.storeConversation(groupId, conversation);
                    newConversations = true;
                }
            }
        }
        return newConversations;
    }
}
