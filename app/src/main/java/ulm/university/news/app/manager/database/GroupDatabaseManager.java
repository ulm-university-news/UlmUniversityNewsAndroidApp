package ulm.university.news.app.manager.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import ulm.university.news.app.data.Ballot;
import ulm.university.news.app.data.Conversation;
import ulm.university.news.app.data.ConversationMessage;
import ulm.university.news.app.data.Group;
import ulm.university.news.app.data.User;
import ulm.university.news.app.data.enums.GroupType;
import ulm.university.news.app.data.enums.Priority;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.manager.database.DatabaseManager.BALLOT_ADMIN;
import static ulm.university.news.app.manager.database.DatabaseManager.BALLOT_CLOSED;
import static ulm.university.news.app.manager.database.DatabaseManager.BALLOT_DESCRIPTION;
import static ulm.university.news.app.manager.database.DatabaseManager.BALLOT_ID;
import static ulm.university.news.app.manager.database.DatabaseManager.BALLOT_MULTIPLE_CHOICE;
import static ulm.university.news.app.manager.database.DatabaseManager.BALLOT_PUBLIC;
import static ulm.university.news.app.manager.database.DatabaseManager.BALLOT_TABLE;
import static ulm.university.news.app.manager.database.DatabaseManager.BALLOT_TITLE;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_TABLE;
import static ulm.university.news.app.manager.database.DatabaseManager.CONVERSATION_ADMIN;
import static ulm.university.news.app.manager.database.DatabaseManager.CONVERSATION_CLOSED;
import static ulm.university.news.app.manager.database.DatabaseManager.CONVERSATION_ID;
import static ulm.university.news.app.manager.database.DatabaseManager.CONVERSATION_ID_FOREIGN;
import static ulm.university.news.app.manager.database.DatabaseManager.CONVERSATION_MESSAGE_AUTHOR;
import static ulm.university.news.app.manager.database.DatabaseManager.CONVERSATION_MESSAGE_MESSAGE_NUMBER;
import static ulm.university.news.app.manager.database.DatabaseManager.CONVERSATION_MESSAGE_TABLE;
import static ulm.university.news.app.manager.database.DatabaseManager.CONVERSATION_TABLE;
import static ulm.university.news.app.manager.database.DatabaseManager.CONVERSATION_TITLE;
import static ulm.university.news.app.manager.database.DatabaseManager.GROUP_ADMIN;
import static ulm.university.news.app.manager.database.DatabaseManager.GROUP_CREATION_DATE;
import static ulm.university.news.app.manager.database.DatabaseManager.GROUP_DELETED;
import static ulm.university.news.app.manager.database.DatabaseManager.GROUP_DELETED_READ;
import static ulm.university.news.app.manager.database.DatabaseManager.GROUP_DESCRIPTION;
import static ulm.university.news.app.manager.database.DatabaseManager.GROUP_ID;
import static ulm.university.news.app.manager.database.DatabaseManager.GROUP_ID_FOREIGN;
import static ulm.university.news.app.manager.database.DatabaseManager.GROUP_MODIFICATION_DATE;
import static ulm.university.news.app.manager.database.DatabaseManager.GROUP_NAME;
import static ulm.university.news.app.manager.database.DatabaseManager.GROUP_TABLE;
import static ulm.university.news.app.manager.database.DatabaseManager.GROUP_TERM;
import static ulm.university.news.app.manager.database.DatabaseManager.GROUP_TYPE;
import static ulm.university.news.app.manager.database.DatabaseManager.MESSAGE_CREATION_DATE;
import static ulm.university.news.app.manager.database.DatabaseManager.MESSAGE_ID;
import static ulm.university.news.app.manager.database.DatabaseManager.MESSAGE_ID_FOREIGN;
import static ulm.university.news.app.manager.database.DatabaseManager.MESSAGE_PRIORITY;
import static ulm.university.news.app.manager.database.DatabaseManager.MESSAGE_READ;
import static ulm.university.news.app.manager.database.DatabaseManager.MESSAGE_TABLE;
import static ulm.university.news.app.manager.database.DatabaseManager.MESSAGE_TEXT;
import static ulm.university.news.app.manager.database.DatabaseManager.USER_GROUP_ACTIVE;
import static ulm.university.news.app.manager.database.DatabaseManager.USER_GROUP_TABLE;
import static ulm.university.news.app.manager.database.DatabaseManager.USER_ID;
import static ulm.university.news.app.manager.database.DatabaseManager.USER_ID_FOREIGN;
import static ulm.university.news.app.manager.database.DatabaseManager.USER_NAME;
import static ulm.university.news.app.manager.database.DatabaseManager.USER_OLD_NAME;
import static ulm.university.news.app.manager.database.DatabaseManager.USER_TABLE;
import static ulm.university.news.app.util.Constants.TIME_ZONE;

/**
 * TODO
 * Methods won't throw exceptions if database failure of whatever kind occurs.
 *
 * @author Matthias Mak
 */
public class GroupDatabaseManager {
    /** This classes tag for logging. */
    private static final String TAG = "GroupDatabaseManager";
    /** The instance of DatabaseManager. */
    private DatabaseManager dbm;
    /** The application context. */
    private Context appContext;

    public static final String STORE_GROUP = "storeGroup";
    public static final String JOIN_GROUP = "joinGroup";
    public static final String ADD_USER_TO_GROUP = "addUserToGroup";
    public static final String LEAVE_GROUP = "removeUserFromGroup";
    public static final String UPDATE_GROUP = "updateGroup";
    public static final String STORE_CONVERSATION = "storeConversation";
    public static final String UPDATE_CONVERSATION = "updateConversation";
    public static final String STORE_CONVERSATION_MESSAGE = "storeConversationMessage";
    public static final String CONVERSATION_DELETED = "conversationDeleted";
    public static final String STORE_BALLOT = "storeBallot";
    public static final String UPDATE_BALLOT = "updateBallot";
    public static final String BALLOT_DELETED = "ballotDeleted";
    public static final String STORE_BALLOT_OPTION = "storeBallotOption";

    /** Creates a new instance of GroupDatabaseManager. */
    public GroupDatabaseManager(Context context) {
        dbm = DatabaseManager.getInstance(context);
        appContext = context.getApplicationContext();
    }

    /**
     * Stores the given group in the database.
     *
     * @param group The group which should be stored.
     */
    public void storeGroup(Group group) {
        Log.d(TAG, "Store " + group);
        SQLiteDatabase db = dbm.getWritableDatabase();

        // Group values.
        ContentValues groupValues = new ContentValues();
        groupValues.put(GROUP_ID, group.getId());
        groupValues.put(GROUP_NAME, group.getName());
        groupValues.put(GROUP_DESCRIPTION, group.getDescription());
        groupValues.put(GROUP_TYPE, group.getGroupType().ordinal());
        groupValues.put(GROUP_TERM, group.getTerm());
        groupValues.put(GROUP_DELETED, false);
        groupValues.put(GROUP_DELETED_READ, false);
        groupValues.put(GROUP_CREATION_DATE, group.getCreationDate().getMillis());
        groupValues.put(GROUP_MODIFICATION_DATE, group.getModificationDate().getMillis());
        groupValues.put(GROUP_ADMIN, group.getGroupAdmin());

        try {
            db.insertOrThrow(GROUP_TABLE, null, groupValues);
        } catch (SQLException e) {
            Log.i(TAG, "Group " + group.getId() + " already stored in database.");
        }

        // Notify observers that database content has changed.
        Intent databaseChanged = new Intent(STORE_GROUP);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged);
    }

    /**
     * Retrieves the group with given id from the database.
     *
     * @return The group with given id.
     */
    public Group getGroup(int groupId) {
        Group group = null;
        SQLiteDatabase db = dbm.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + GROUP_TABLE + " WHERE " + GROUP_ID + "=?";
        String[] args = {String.valueOf(groupId)};
        Log.d(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, args);
        if (c != null && c.moveToFirst()) {
            group = new Group();
            group.setId(c.getInt(c.getColumnIndex(GROUP_ID)));
            group.setName((c.getString(c.getColumnIndex(GROUP_NAME))));
            group.setDescription((c.getString(c.getColumnIndex(GROUP_DESCRIPTION))));
            group.setGroupType(GroupType.values[c.getInt(c.getColumnIndex(GROUP_TYPE))]);
            group.setTerm((c.getString(c.getColumnIndex(GROUP_TERM))));
            group.setDeleted(c.getInt(c.getColumnIndex(GROUP_DELETED)) == 1);
            group.setDeletedRead(c.getInt(c.getColumnIndex(GROUP_DELETED_READ)) == 1);
            group.setGroupAdmin((c.getInt(c.getColumnIndex(GROUP_ADMIN))));
            group.setCreationDate(new DateTime(c.getLong(c.getColumnIndex(GROUP_CREATION_DATE)), TIME_ZONE));
            group.setModificationDate(new DateTime(c.getLong(c.getColumnIndex(GROUP_MODIFICATION_DATE)), TIME_ZONE));
            c.close();
        }
        Log.d(TAG, "End with " + group);
        return group;
    }

    /**
     * Retrieves the groups of the local user from the database.
     *
     * @return The groups of the local user.
     */
    public List<Group> getMyGroups() {
        List<Group> groups = new ArrayList<>();
        Group group = null;
        SQLiteDatabase db = dbm.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + GROUP_TABLE + " AS g INNER JOIN " + USER_GROUP_TABLE
                + " AS ug ON g." + GROUP_ID + "=ug." + GROUP_ID_FOREIGN + " WHERE ug." + USER_ID_FOREIGN + "=?";
        String[] args = {String.valueOf(Util.getInstance(appContext).getLocalUser().getId())};
        Log.d(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, args);
        while (c != null && c.moveToNext()) {
            group = new Group();
            group.setId(c.getInt(c.getColumnIndex(GROUP_ID)));
            group.setName((c.getString(c.getColumnIndex(GROUP_NAME))));
            group.setDescription((c.getString(c.getColumnIndex(GROUP_DESCRIPTION))));
            group.setGroupType(GroupType.values[c.getInt(c.getColumnIndex(GROUP_TYPE))]);
            group.setTerm((c.getString(c.getColumnIndex(GROUP_TERM))));
            group.setDeleted(c.getInt(c.getColumnIndex(GROUP_DELETED)) == 1);
            group.setDeletedRead(c.getInt(c.getColumnIndex(GROUP_DELETED_READ)) == 1);
            group.setGroupAdmin((c.getInt(c.getColumnIndex(GROUP_ADMIN))));
            group.setCreationDate(new DateTime(c.getLong(c.getColumnIndex(GROUP_CREATION_DATE)), TIME_ZONE));
            group.setModificationDate(new DateTime(c.getLong(c.getColumnIndex(GROUP_MODIFICATION_DATE)), TIME_ZONE));
            // TODO Get number of unread conversation messages / ballots.
            groups.add(group);
        }
        if (c != null) {
            c.close();
        }
        Log.d(TAG, "End with " + group);
        return groups;
    }

    /**
     * Updates the given group in the database. Some fields can't be updated.
     *
     * @param group The updated group.
     */
    public void updateGroup(Group group) {
        Log.d(TAG, "Update " + group);
        SQLiteDatabase db = dbm.getWritableDatabase();

        // Group values.
        ContentValues groupValues = new ContentValues();
        groupValues.put(GROUP_NAME, group.getName());
        groupValues.put(GROUP_DESCRIPTION, group.getDescription());
        groupValues.put(GROUP_TERM, group.getTerm());
        groupValues.put(GROUP_ADMIN, group.getGroupAdmin());
        groupValues.put(GROUP_MODIFICATION_DATE, group.getModificationDate().getMillis());

        String where = GROUP_ID + "=?";
        String[] args = {String.valueOf(group.getId())};

        db.update(GROUP_TABLE, groupValues, where, args);

        // Notify observers that database content has changed.
        Intent databaseChanged = new Intent(UPDATE_GROUP);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged);
    }

    /**
     * Deletes the group identified by id.
     *
     * @param groupId The id of the group that should be deleted.
     */
    public void deleteGroup(int groupId) {
        Log.d(TAG, "Delete group " + groupId);
        SQLiteDatabase db = dbm.getWritableDatabase();

        String where = GROUP_ID + "=?";
        String[] args = {String.valueOf(groupId)};
        db.delete(GROUP_TABLE, where, args);

        // Notify observers that database content has changed.
        Intent databaseChanged = new Intent(GROUP_DELETED);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged);
    }

    /**
     * Adds a user as a group member to the group identified by id.
     *
     * @param groupId The id of the group.
     */
    public void addUserToGroup(int groupId, int userId) {
        Log.d(TAG, "Add user " + userId + " to group " + groupId);
        SQLiteDatabase db = dbm.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(GROUP_ID_FOREIGN, groupId);
        values.put(USER_ID_FOREIGN, userId);
        values.put(USER_GROUP_ACTIVE, true);
        try {
            db.insertOrThrow(USER_GROUP_TABLE, null, values);
        } catch (SQLException e) {
            Log.i(TAG, "User " + userId + " is already set as group member for group " + groupId);
        }

        // Notify observers that database content has changed.
        Intent databaseChanged = new Intent(ADD_USER_TO_GROUP);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged);
    }

    /**
     * The specified user leaves the group identified by id.
     *
     * @param groupId The id of the group that should be left.
     * @param userId The id of the user who leaves.
     */
    public void removeUserFromGroup(int groupId, int userId) {
        Log.d(TAG, "User " + userId + " leaves group " + groupId);
        SQLiteDatabase db = dbm.getWritableDatabase();

        String where = GROUP_ID_FOREIGN + "=? AND " + USER_ID_FOREIGN + "=?";
        String[] args = {String.valueOf(groupId), String.valueOf(userId)};
        db.delete(USER_GROUP_TABLE, where, args);

        // Notify observers that database content has changed.
        Intent databaseChanged = new Intent(LEAVE_GROUP);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged);
    }

    /**
     * Checks if the local user is a group member of the group identified by given id.
     *
     * @param groupId The id of the group that should be checked.
     * @return true if the user is a member of the group.
     */
    public boolean isGroupMember(int groupId) {
        SQLiteDatabase db = dbm.getReadableDatabase();
        boolean isGroupMember = false;

        String selectQuery = "SELECT * FROM " + USER_GROUP_TABLE
                + " WHERE " + GROUP_ID_FOREIGN + "=? AND " + USER_ID_FOREIGN + "=?";
        String[] args = {String.valueOf(groupId), String.valueOf(Util.getInstance(appContext).getLocalUser().getId())};
        Log.d(TAG, selectQuery + " -> " + groupId);

        Cursor c = db.rawQuery(selectQuery, args);
        if (c != null && c.moveToFirst()) {
            isGroupMember = true;
            c.close();
        }
        Log.d(TAG, "End with " + isGroupMember);
        return isGroupMember;
    }

    /**
     * Retrieves all group members of the group with given id from the database.
     *
     * @return The group members of the group.
     */
    public List<User> getGroupMembers(int groupId) {
        List<User> users = new ArrayList<>();
        User user = null;
        SQLiteDatabase db = dbm.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + USER_TABLE + " AS u INNER JOIN " + USER_GROUP_TABLE
                + " AS ug ON u." + USER_ID + "=ug." + USER_ID_FOREIGN + " WHERE ug." + GROUP_ID_FOREIGN + "=?";
        String[] args = {String.valueOf(groupId)};
        Log.d(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, args);
        while (c != null && c.moveToNext()) {
            user = new User();
            user.setId(c.getInt(c.getColumnIndex(USER_ID)));
            user.setName((c.getString(c.getColumnIndex(USER_NAME))));
            user.setOldName((c.getString(c.getColumnIndex(USER_OLD_NAME))));
            users.add(user);
        }
        if (c != null) {
            c.close();
        }
        Log.d(TAG, "End with " + users);
        return users;
    }

    /**
     * Marks a deleted group. When a group is marked this way, no group deleted dialog will show again.
     *
     * @param groupId The id of the deleted group which should be set to read.
     */
    public void setGroupDeletedToRead(int groupId) {
        Log.d(TAG, "Set deleted group with id " + groupId + " to read.");
        SQLiteDatabase db = dbm.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(GROUP_DELETED_READ, true);
        String where = GROUP_ID + "=?";
        String[] args = {String.valueOf(groupId)};
        db.update(CHANNEL_TABLE, values, where, args);

        // Notify observers that database content has changed.
        Intent databaseChanged = new Intent(UPDATE_GROUP);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged);
    }

    /**
     * Stores the given conversation in the database.
     *
     * @param conversation The conversation which should be stored.
     */
    public void storeConversation(int groupId, Conversation conversation) {
        Log.d(TAG, "Store " + conversation);
        SQLiteDatabase db = dbm.getWritableDatabase();

        // Conversation values.
        ContentValues conversationValues = new ContentValues();
        conversationValues.put(CONVERSATION_ID, conversation.getId());
        conversationValues.put(CONVERSATION_TITLE, conversation.getTitle());
        conversationValues.put(CONVERSATION_CLOSED, conversation.getClosed());
        conversationValues.put(CONVERSATION_ADMIN, conversation.getAdmin());
        conversationValues.put(GROUP_ID_FOREIGN, groupId);
        try {
            db.insertOrThrow(CONVERSATION_TABLE, null, conversationValues);
        } catch (SQLException e) {
            Log.i(TAG, "Conversation " + conversation.getId() + " is already stored.");
        }

        // Notify observers that database content has changed.
        Intent databaseChanged = new Intent(STORE_CONVERSATION);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged);
    }

    /**
     * Updates the given conversation in the database. Some fields can't be updated.
     *
     * @param conversation The updated conversation.
     */
    public void updateConversation(Conversation conversation) {
        Log.d(TAG, "Update " + conversation);
        SQLiteDatabase db = dbm.getWritableDatabase();

        // Conversation values.
        ContentValues conversationValues = new ContentValues();
        conversationValues.put(CONVERSATION_TITLE, conversation.getTitle());
        conversationValues.put(CONVERSATION_CLOSED, conversation.getClosed());
        conversationValues.put(CONVERSATION_ADMIN, conversation.getAdmin());

        String where = CONVERSATION_ID + "=?";
        String[] args = {String.valueOf(conversation.getId())};

        db.update(CONVERSATION_TABLE, conversationValues, where, args);

        // Notify observers that database content has changed.
        Intent databaseChanged = new Intent(UPDATE_CONVERSATION);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged);
    }

    /**
     * Deletes the conversation identified by id.
     *
     * @param conversationId The id of the conversation that should be deleted.
     */
    public void deleteConversation(int conversationId) {
        Log.d(TAG, "Delete conversation " + conversationId);
        SQLiteDatabase db = dbm.getWritableDatabase();

        String where = CONVERSATION_ID + "=?";
        String[] args = {String.valueOf(conversationId)};
        db.delete(CONVERSATION_TABLE, where, args);

        // Notify observers that database content has changed.
        Intent databaseChanged = new Intent(CONVERSATION_DELETED);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged);
    }

    /**
     * Retrieves the conversation with given id from the database.
     *
     * @param conversationId The id of the conversation.
     * @return The specific conversation of the group.
     */
    public Conversation getConversation(int conversationId) {
        Conversation conversation = null;
        SQLiteDatabase db = dbm.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + CONVERSATION_TABLE + " WHERE " + CONVERSATION_ID + "=?";
        String[] args = {String.valueOf(conversationId)};
        Log.d(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, args);
        if (c != null && c.moveToFirst()) {
            conversation = new Conversation();
            conversation.setId(c.getInt(c.getColumnIndex(CONVERSATION_ID)));
            conversation.setTitle((c.getString(c.getColumnIndex(CONVERSATION_TITLE))));
            conversation.setClosed(c.getInt(c.getColumnIndex(CONVERSATION_CLOSED)) == 1);
            conversation.setAdmin((c.getInt(c.getColumnIndex(CONVERSATION_ADMIN))));
            conversation.setNumberOfUnreadConversationMessages(getNumberOfUnreadConversationMessages(conversationId));
        }
        if (c != null) {
            c.close();
        }
        Log.d(TAG, "End with " + conversation);
        return conversation;
    }

    /**
     * Retrieves the conversations of the specified group from the database.
     *
     * @param groupId The id of the group.
     * @return The conversations of the group.
     */
    public List<Conversation> getConversations(int groupId) {
        List<Conversation> conversations = new ArrayList<>();
        Conversation conversation;
        SQLiteDatabase db = dbm.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + CONVERSATION_TABLE + " WHERE " + GROUP_ID_FOREIGN + "=?";
        String[] args = {String.valueOf(groupId)};
        Log.d(TAG, selectQuery);
        int conversationId;

        Cursor c = db.rawQuery(selectQuery, args);
        while (c != null && c.moveToNext()) {
            conversation = new Conversation();
            conversationId = c.getInt(c.getColumnIndex(CONVERSATION_ID));
            conversation.setId(conversationId);
            conversation.setTitle((c.getString(c.getColumnIndex(CONVERSATION_TITLE))));
            conversation.setClosed(c.getInt(c.getColumnIndex(CONVERSATION_CLOSED)) == 1);
            conversation.setAdmin((c.getInt(c.getColumnIndex(CONVERSATION_ADMIN))));
            conversation.setNumberOfUnreadConversationMessages(getNumberOfUnreadConversationMessages(conversationId));
            conversations.add(conversation);
        }
        if (c != null) {
            c.close();
        }
        Log.d(TAG, "End with " + conversations);
        return conversations;
    }

    /**
     * Stores the given conversation message in the database.
     *
     * @param conversationMessage The conversation message which should be stored.
     */
    public void storeConversationMessage(ConversationMessage conversationMessage) {
        Log.d(TAG, "Store " + conversationMessage);
        SQLiteDatabase db = null;
        try {
            db = dbm.getWritableDatabase();

            // Message values.
            ContentValues messageValues = new ContentValues();
            messageValues.put(MESSAGE_ID, conversationMessage.getId());
            messageValues.put(MESSAGE_TEXT, conversationMessage.getText());
            messageValues.put(MESSAGE_CREATION_DATE, conversationMessage.getCreationDate().getMillis());
            messageValues.put(MESSAGE_PRIORITY, conversationMessage.getPriority().ordinal());
            messageValues.put(MESSAGE_READ, false);

            // Conversation message values.
            ContentValues conversationMessageValues = new ContentValues();
            conversationMessageValues.put(MESSAGE_ID_FOREIGN, conversationMessage.getId());
            conversationMessageValues.put(CONVERSATION_ID_FOREIGN, conversationMessage.getConversationId());
            conversationMessageValues.put(CONVERSATION_MESSAGE_MESSAGE_NUMBER, conversationMessage.getMessageNumber());
            conversationMessageValues.put(CONVERSATION_MESSAGE_AUTHOR, conversationMessage.getAuthorUser());

            // If there are two insert statements make sure that they are performed in one transaction.
            db.beginTransaction();
            db.insertOrThrow(MESSAGE_TABLE, null, messageValues);
            db.insertOrThrow(CONVERSATION_MESSAGE_TABLE, null, conversationMessageValues);

            // Notify observers that specific database content has changed.
            Intent databaseChanged = new Intent(STORE_CONVERSATION_MESSAGE);
            Log.d(TAG, "sendBroadcast:" + LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged));

            // Mark transaction as successful.
            db.setTransactionSuccessful();
            Log.d(TAG, "End. ConversationMessage stored successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Database failure during storeConversationMessage(). Need to rollback transaction.");
        } finally {
            if (db != null) {
                // Commit on success or rollback transaction if an error has occurred.
                db.endTransaction();
            }
        }
    }

    /**
     * Gets all conversation messages of a specific conversation from the database.
     *
     * @param conversationId The id of the conversation.
     * @return A list of conversation objects.
     */
    public List<ConversationMessage> getConversationMessages(int conversationId) {
        List<ConversationMessage> conversationMessages = new ArrayList<>();
        SQLiteDatabase db = dbm.getReadableDatabase();
        String query = "SELECT * FROM " + MESSAGE_TABLE + " AS m JOIN " + CONVERSATION_MESSAGE_TABLE + " AS cm" +
                " ON m." + MESSAGE_ID + "=cm." + MESSAGE_ID_FOREIGN + " WHERE cm." + CONVERSATION_ID_FOREIGN + "=?";
        String[] args = {String.valueOf(conversationId)};
        Log.d(TAG, query);

        // Create fields before while loop, not within every pass.
        ConversationMessage conversationMessage;
        String text;
        int messageId, messageNumber, author;
        boolean read;
        Priority priority;
        DateTime creationDate;

        // Get message data from database.
        Cursor cMessage = db.rawQuery(query, args);
        while (cMessage != null && cMessage.moveToNext()) {
            messageId = cMessage.getInt(cMessage.getColumnIndex(MESSAGE_ID));
            messageNumber = cMessage.getInt(cMessage.getColumnIndex(CONVERSATION_MESSAGE_MESSAGE_NUMBER));
            author = cMessage.getInt(cMessage.getColumnIndex(CONVERSATION_MESSAGE_AUTHOR));
            text = cMessage.getString(cMessage.getColumnIndex(MESSAGE_TEXT));
            priority = Priority.values[(cMessage.getInt(cMessage.getColumnIndex(MESSAGE_PRIORITY)))];
            read = cMessage.getInt(cMessage.getColumnIndex(MESSAGE_READ)) != 0;
            creationDate = new DateTime(cMessage.getLong(cMessage.getColumnIndex(MESSAGE_CREATION_DATE)), TIME_ZONE);

            // Add new conversation message to the conversation message list.
            conversationMessage = new ConversationMessage(messageId, text, messageNumber, priority, creationDate,
                    author, conversationId, read);
            conversationMessages.add(conversationMessage);
        }
        if (cMessage != null) {
            cMessage.close();
        }
        Log.d(TAG, "End with " + conversationMessages);
        return conversationMessages;
    }

    /**
     * Gets the number of unread conversation messages of a specific conversation from the database.
     *
     * @param conversationId The id of the conversation.
     * @return The number of unread conversation messages.
     */
    public int getNumberOfUnreadConversationMessages(int conversationId) {
        SQLiteDatabase db = dbm.getReadableDatabase();
        String query = "SELECT * FROM " + MESSAGE_TABLE + " AS m JOIN " + CONVERSATION_MESSAGE_TABLE + " AS cm" +
                " ON m." + MESSAGE_ID + "=cm." + MESSAGE_ID_FOREIGN + " WHERE cm." + CONVERSATION_ID_FOREIGN + "=?";
        String[] args = {String.valueOf(conversationId)};
        Log.d(TAG, query);

        int numberOfUnreadConversationMessages = 0;
        boolean read;

        // Get message data from database.
        Cursor cMessage = db.rawQuery(query, args);
        while (cMessage != null && cMessage.moveToNext()) {
            read = cMessage.getInt(cMessage.getColumnIndex(MESSAGE_READ)) != 0;
            if (!read) {
                numberOfUnreadConversationMessages++;
            }
        }
        if (cMessage != null) {
            cMessage.close();
        }
        Log.d(TAG, "End with number of unread conversation messages: " + numberOfUnreadConversationMessages);
        return numberOfUnreadConversationMessages;
    }

    /**
     * Gets the biggest message number among conversation messages of a specific conversation from the database.
     *
     * @param conversationId The id of the conversation.
     * @return The biggest message number.
     */
    public int getMaxMessageNumberConversationMessage(int conversationId) {
        SQLiteDatabase db = dbm.getReadableDatabase();
        String announcementsQuery = "SELECT MAX(cm." + CONVERSATION_MESSAGE_MESSAGE_NUMBER + ") FROM " + MESSAGE_TABLE +
                " AS m JOIN " + CONVERSATION_MESSAGE_TABLE + " AS cm ON m." + MESSAGE_ID + "=cm." + MESSAGE_ID_FOREIGN +
                " WHERE cm." + CONVERSATION_ID_FOREIGN + "=?";
        String[] args = {String.valueOf(conversationId)};
        Log.d(TAG, announcementsQuery);

        // Get message data from database.
        int messageNumber = 0;
        Cursor cMessage = db.rawQuery(announcementsQuery, args);
        if (cMessage != null && cMessage.moveToFirst()) {
            messageNumber = cMessage.getInt(0);
            cMessage.close();
        }
        Log.d(TAG, "End with max message number " + messageNumber);
        return messageNumber;
    }

    /**
     * Marks a message as read.
     *
     * @param messageId The message which should be set to read.
     */
    public void setMessageToRead(int messageId) {
        Log.d(TAG, "Update message with id " + messageId);
        SQLiteDatabase db = dbm.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MESSAGE_READ, true);
        String where = MESSAGE_ID + "=?";
        String[] args = {String.valueOf(messageId)};
        db.update(MESSAGE_TABLE, values, where, args);
    }

    /**
     * Stores the given ballot in the database.
     *
     * @param ballot The ballot which should be stored.
     */
    public void storeBallot(int groupId, Ballot ballot) {
        Log.d(TAG, "Store " + ballot);
        SQLiteDatabase db = dbm.getWritableDatabase();

        // Conversation values.
        ContentValues ballotValues = new ContentValues();
        ballotValues.put(BALLOT_ID, ballot.getId());
        ballotValues.put(BALLOT_TITLE, ballot.getTitle());
        ballotValues.put(BALLOT_CLOSED, ballot.getClosed());
        ballotValues.put(BALLOT_ADMIN, ballot.getAdmin());
        ballotValues.put(BALLOT_DESCRIPTION, ballot.getDescription());
        ballotValues.put(BALLOT_MULTIPLE_CHOICE, ballot.getMultipleChoice());
        ballotValues.put(BALLOT_PUBLIC, ballot.getPublicVotes());
        ballotValues.put(GROUP_ID_FOREIGN, groupId);
        try {
            db.insertOrThrow(BALLOT_TABLE, null, ballotValues);
        } catch (SQLException e) {
            Log.i(TAG, "Ballot " + ballot.getId() + " is already stored.");
        }

        // Notify observers that database content has changed.
        Intent databaseChanged = new Intent(STORE_BALLOT);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged);
    }

    /**
     * Retrieves the ballot with given id from the database.
     *
     * @param ballotId The id of the ballot.
     * @return A specific ballot of the group.
     */
    public Ballot getBallot(int ballotId) {
        Ballot ballot = null;
        SQLiteDatabase db = dbm.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + BALLOT_TABLE + " WHERE " + BALLOT_ID + "=?";
        String[] args = {String.valueOf(ballotId)};
        Log.d(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, args);
        if (c != null && c.moveToFirst()) {
            ballot = new Ballot();
            ballot.setId(c.getInt(c.getColumnIndex(BALLOT_ID)));
            ballot.setTitle((c.getString(c.getColumnIndex(BALLOT_TITLE))));
            ballot.setDescription((c.getString(c.getColumnIndex(BALLOT_DESCRIPTION))));
            ballot.setClosed(c.getInt(c.getColumnIndex(BALLOT_CLOSED)) == 1);
            ballot.setAdmin((c.getInt(c.getColumnIndex(BALLOT_ADMIN))));
            ballot.setMultipleChoice(c.getInt(c.getColumnIndex(BALLOT_MULTIPLE_CHOICE)) == 1);
            ballot.setPublicVotes(c.getInt(c.getColumnIndex(BALLOT_PUBLIC)) == 1);
            c.close();
        }
        Log.d(TAG, "End with " + ballot);
        return ballot;
    }

    /**
     * Retrieves the ballots of the specified group from the database.
     *
     * @param groupId The id of the group.
     * @return The ballots of the group.
     */
    public List<Ballot> getBallots(int groupId) {
        List<Ballot> ballots = new ArrayList<>();
        Ballot ballot;
        SQLiteDatabase db = dbm.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + BALLOT_TABLE + " WHERE " + GROUP_ID_FOREIGN + "=?";
        String[] args = {String.valueOf(groupId)};
        Log.d(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, args);
        while (c != null && c.moveToNext()) {
            ballot = new Ballot();
            ballot.setId(c.getInt(c.getColumnIndex(BALLOT_ID)));
            ballot.setTitle((c.getString(c.getColumnIndex(BALLOT_TITLE))));
            ballot.setDescription((c.getString(c.getColumnIndex(BALLOT_DESCRIPTION))));
            ballot.setClosed(c.getInt(c.getColumnIndex(BALLOT_CLOSED)) == 1);
            ballot.setAdmin((c.getInt(c.getColumnIndex(BALLOT_ADMIN))));
            ballot.setMultipleChoice(c.getInt(c.getColumnIndex(BALLOT_MULTIPLE_CHOICE)) == 1);
            ballot.setPublicVotes(c.getInt(c.getColumnIndex(BALLOT_PUBLIC)) == 1);
            ballots.add(ballot);
        }
        if (c != null) {
            c.close();
        }
        Log.d(TAG, "End with " + ballots);
        return ballots;
    }

    /**
     * Updates the given ballot in the database. Some fields can't be updated.
     *
     * @param ballot The updated ballot.
     */
    public void updateBallot(Ballot ballot) {
        Log.d(TAG, "Update " + ballot);
        SQLiteDatabase db = dbm.getWritableDatabase();

        // Ballot values.
        ContentValues ballotValues = new ContentValues();
        ballotValues.put(BALLOT_TITLE, ballot.getTitle());
        ballotValues.put(BALLOT_DESCRIPTION, ballot.getDescription());
        ballotValues.put(BALLOT_CLOSED, ballot.getClosed());
        ballotValues.put(BALLOT_ADMIN, ballot.getAdmin());

        String where = BALLOT_ID + "=?";
        String[] args = {String.valueOf(ballot.getId())};

        db.update(BALLOT_TABLE, ballotValues, where, args);

        // Notify observers that database content has changed.
        Intent databaseChanged = new Intent(UPDATE_BALLOT);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged);
    }

    /**
     * Deletes the ballot identified by id.
     *
     * @param ballotId The id of the ballot that should be deleted.
     */
    public void deleteBallot(int ballotId) {
        Log.d(TAG, "Delete ballot " + ballotId);
        SQLiteDatabase db = dbm.getWritableDatabase();

        String where = BALLOT_ID + "=?";
        String[] args = {String.valueOf(ballotId)};
        db.delete(BALLOT_TABLE, where, args);

        // Notify observers that database content has changed.
        Intent databaseChanged = new Intent(BALLOT_DELETED);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged);
    }
}
