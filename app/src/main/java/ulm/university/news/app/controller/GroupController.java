package ulm.university.news.app.controller;

import android.content.Context;

import java.util.List;
import java.util.Locale;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Ballot;
import ulm.university.news.app.data.Conversation;
import ulm.university.news.app.data.Group;
import ulm.university.news.app.data.Option;
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
        boolean isAdmin = conversation.isAdmin(Util.getInstance(context).getLocalUser().getId());
        if (isAdmin) {
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
            if (isAdmin) {
                conversationIcon = R.drawable.ic_conversation_closed_admin_white;
            } else {
                conversationIcon = R.drawable.ic_conversation_closed_white;
            }
        }
        return conversationIcon;
    }

    /**
     * Chooses the appropriate icon for the given ballot.
     *
     * @param ballot The ballot.
     * @return The resource id of the ballot icon.
     */
    public static int getBallotIcon(Ballot ballot, Context context) {
        int ballotIcon;
        // Get current language.
        String language = Locale.getDefault().getLanguage();

        // Set appropriate channel icon.
        boolean isAdmin = ballot.getAdmin() == Util.getInstance(context).getLocalUser().getId();
        if (isAdmin) {
            if (language.equals("de")) {
                ballotIcon = R.drawable.ic_abstimmung_admin;
            } else {
                ballotIcon = R.drawable.ic_ballot_admin;
            }
        } else {
            if (language.equals("de")) {
                ballotIcon = R.drawable.ic_abstimmung;
            } else {
                ballotIcon = R.drawable.ic_ballot;
            }
        }

        // If group is marked as closed, set closed icon.
        if (ballot.getClosed()) {
            if (isAdmin) {
                ballotIcon = R.drawable.ic_ballot_closed_admin_white;
            } else {
                ballotIcon = R.drawable.ic_ballot_closed_white;
            }
        }
        return ballotIcon;
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

    /**
     * Stores new ballots or updates existing ones in the database.
     *
     * @param context The current context.
     * @param ballots A list of ballots.
     * @param groupId The group id to which the ballots belong.
     * @return true if new ballots were stored.
     */
    public static boolean storeBallots(Context context, List<Ballot> ballots, int groupId) {
        GroupDatabaseManager groupDBM = new GroupDatabaseManager(context);
        boolean newBallots = false;

        if (!ballots.isEmpty()) {
            List<Ballot> ballotsDB = groupDBM.getBallots(groupId);
            boolean alreadyStored;

            // Store new ballots and update existing ones.
            for (Ballot ballot : ballots) {
                alreadyStored = false;
                for (Ballot ballotDB : ballotsDB) {
                    if (ballotDB.getId() == ballot.getId()) {
                        groupDBM.updateBallot(ballot);
                        alreadyStored = true;
                        break;
                    }
                }
                if (!alreadyStored) {
                    groupDBM.storeBallot(groupId, ballot);
                    newBallots = true;
                }
            }
        }
        return newBallots;
    }

    /**
     * Stores new ballot options in the database.
     *
     * @param context The current context.
     * @param options A list of options.
     * @param ballotId The ballot id to which the options belong.
     * @return true if new options were stored.
     */
    public static boolean storeOptions(Context context, List<Option> options, int ballotId) {
        GroupDatabaseManager groupDBM = new GroupDatabaseManager(context);
        boolean newOptions = false;

        if (!options.isEmpty()) {
            List<Option> optionsDB = groupDBM.getOptions(ballotId);
            boolean alreadyStored;
            // Store new options.
            for (Option option : options) {
                alreadyStored = false;
                for (Option optionDB : optionsDB) {
                    if (optionDB.getId() == option.getId()) {
                        alreadyStored = true;
                        break;
                    }
                }
                if (!alreadyStored) {
                    groupDBM.storeOption(ballotId, option);
                    newOptions = true;
                }
            }
        }
        return newOptions;
    }
}
