package ulm.university.news.app.controller;

import android.content.Context;
import android.text.Html;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Ballot;
import ulm.university.news.app.data.Conversation;
import ulm.university.news.app.data.Group;
import ulm.university.news.app.data.Option;
import ulm.university.news.app.data.Settings;
import ulm.university.news.app.data.User;
import ulm.university.news.app.manager.database.GroupDatabaseManager;
import ulm.university.news.app.manager.database.SettingsDatabaseManager;
import ulm.university.news.app.manager.database.UserDatabaseManager;
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
     * Stores new conversations or updates existing ones and removes deleted conversations in the database.
     *
     * @param context The current context.
     * @param conversations A list of conversations.
     * @param groupId The group id to which the conversations belong.
     * @return true if new conversations were stored and/or old conversations were removed.
     */
    public static boolean storeConversations(Context context, List<Conversation> conversations, int groupId) {
        GroupDatabaseManager groupDBM = new GroupDatabaseManager(context);
        boolean newConversations = false;
        boolean oldConversations = false;

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
            // Remove deleted conversations.
            for (Conversation conversationDB: conversationsDB) {
                boolean wasDeleted = true;
                for (Conversation conversation : conversations) {
                    if (conversationDB.getId() == conversation.getId()) {
                        wasDeleted = false;
                        break;
                    }
                }
                if (wasDeleted) {
                    groupDBM.deleteConversation(conversationDB.getId());
                    oldConversations = true;
                }
            }
        }
        return newConversations || oldConversations;
    }

    /**
     * Stores new ballots or updates existing ones and removes deleted ballots in the database.
     *
     * @param context The current context.
     * @param ballots A list of ballots.
     * @param groupId The group id to which the ballots belong.
     * @return true if new ballots were stored and/or deleted ballots were removed.
     */
    public static boolean storeBallots(Context context, List<Ballot> ballots, int groupId) {
        GroupDatabaseManager groupDBM = new GroupDatabaseManager(context);
        boolean newBallots = false;
        boolean oldBallots = false;

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
            // Remove deleted ballots.
            for (Ballot ballotDB: ballotsDB) {
                boolean wasDeleted = true;
                for (Ballot ballot : ballots) {
                    if (ballotDB.getId() == ballot.getId()) {
                        wasDeleted = false;
                        break;
                    }
                }
                if (wasDeleted) {
                    groupDBM.deleteBallot(ballotDB.getId());
                    oldBallots = true;
                }
            }
        }
        return newBallots || oldBallots;
    }

    /**
     * Stores new and removes deleted ballot options in the database.
     *
     * @param context The current context.
     * @param options A list of options.
     * @param ballotId The ballot id to which the options belong.
     * @return true if options were change.
     */
    public static boolean storeOptions(Context context, List<Option> options, int ballotId) {
        GroupDatabaseManager groupDBM = new GroupDatabaseManager(context);
        boolean newOptions = false;
        boolean oldOptions = false;

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
            // Remove deleted votes.
            for (Option optionDB: optionsDB) {
                if (!options.contains(optionDB)) {
                    groupDBM.deleteOption(optionDB.getId());
                    oldOptions = true;
                }
            }
        }
        return newOptions || oldOptions;
    }

    /**
     * Stores new voters and removes deleted voters of an option in the database.
     *
     * @param context The current context.
     * @param options A list of options including user votes.
     * @return true if new votes were changed.
     */
    public static boolean storeVoters(Context context, List<Option> options) {
        GroupDatabaseManager groupDBM = new GroupDatabaseManager(context);
        boolean newVotes = false;
        boolean oldVotes = false;

        if (!options.isEmpty()) {
            for (Option option : options) {
                List<Integer> votesDB = groupDBM.getVoters(option.getId());
                boolean alreadyStored;
                // Store new votes.
                for (Integer vote : option.getVoters()) {
                    alreadyStored = false;
                    for (Integer voteDB : votesDB) {
                        if (voteDB.intValue() == vote.intValue()) {
                            alreadyStored = true;
                            break;
                        }
                    }
                    if (!alreadyStored) {
                        groupDBM.storeVote(option.getId(), vote);
                        newVotes = true;
                    }
                }
                // Remove deleted votes.
                for (Integer voteDB : votesDB) {
                    if (!option.getVoters().contains(voteDB)) {
                        groupDBM.deleteVote(option.getId(), voteDB);
                        oldVotes = true;
                    }
                }
            }
        }
        return newVotes || oldVotes;
    }

    public static List<Option> getMyOptions(Context context, List<Option> options) {
        List<Option> myOptions = new ArrayList<>();
        for (Option option : options) {
            for (Integer voter : option.getVoters()) {
                if (voter.intValue() == Util.getInstance(context).getLocalUser().getId()) {
                    myOptions.add(option);
                    break;
                }
            }
        }
        return myOptions;
    }

    public static Option getMyOption(Context context, List<Option> options) {
        for (Option option : options) {
            for (Integer voter : option.getVoters()) {
                if (voter.intValue() == Util.getInstance(context).getLocalUser().getId()) {
                    return option;
                }
            }
        }
        return null;
    }

    public static String getVoterNames(Context context, List<Integer> voters) {
        String voterNames;
        if (voters != null && !voters.isEmpty()) {
            UserDatabaseManager userDBM = new UserDatabaseManager(context);
            voterNames = "";
            for (int i = 0; i < voters.size(); i++) {
                User user = userDBM.getUser(voters.get(i));
                if (user != null) {
                    voterNames += user.getName();
                } else {
                    voterNames += context.getString(R.string.general_unknown);
                }
                if (i < voters.size() - 1) {
                    voterNames += " " + Html.fromHtml("&#8211; ").toString();
                }
            }
        } else {
            voterNames = context.getString(R.string.general_nobody);
        }
        return voterNames;
    }

    /**
     * Sorts the given options according to the number of votes.
     *
     * @param options The option list.
     */
    public static void sortOptions(List<Option> options) {
        Collections.sort(options, new Comparator<Option>() {
            public int compare(Option c1, Option c2) {
                return c1.getVoters().size() < c2.getVoters().size() ? 1 : -1;
            }
        });
    }

    /**
     * Sorts the given groups alphabetically. Ignores group type.
     *
     * @param groups The group list.
     */
    private static void sortGroupName(List<Group> groups) {
        Collections.sort(groups, new Comparator<Group>() {
            public int compare(Group g1, Group g2) {
                return g1.getName().compareToIgnoreCase(g2.getName());
            }
        });
    }

    /**
     * Sorts the given groups according to their group type. Groups of the same type will be sorted
     * alphabetically.
     *
     * @param groups The group list.
     */
    private static void sortGroupType(List<Group> groups) {
        Collections.sort(groups, new Comparator<Group>() {
            public int compare(Group g1, Group g2) {
                int res = g1.getGroupType().compareTo(g2.getGroupType());
                if (res != 0) {
                    return res;
                }
                return g1.getName().compareToIgnoreCase(g2.getName());
            }
        });
    }

    /**
     * Sorts the given group list according to the group settings.
     *
     * @param groups The group list.
     */
    public static void sortGroups(Context context, List<Group> groups) {
        if (groups != null) {
            // Check settings for preferred group order.
            Settings settings = new SettingsDatabaseManager(context).getSettings();
            switch (settings.getGroupSettings()) {
                case ALPHABETICAL:
                    sortGroupName(groups);
                    break;
                case TYPE:
                    sortGroupType(groups);
                    break;
            }
        }
    }
}
