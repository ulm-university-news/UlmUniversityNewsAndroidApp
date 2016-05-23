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

import ulm.university.news.app.data.Group;
import ulm.university.news.app.data.enums.GroupType;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_TABLE;
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
import static ulm.university.news.app.manager.database.DatabaseManager.LOCAL_USER_TABLE;
import static ulm.university.news.app.manager.database.DatabaseManager.USER_GROUP_ACTIVE;
import static ulm.university.news.app.manager.database.DatabaseManager.USER_GROUP_TABLE;
import static ulm.university.news.app.manager.database.DatabaseManager.USER_ID_FOREIGN;
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
    public static final String LEAVE_GROUP = "leaveGroup";
    public static final String UPDATE_GROUP = "updateGroup";

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

        db.insert(GROUP_TABLE, null, groupValues);

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
     * Retrieves the group with given id from the database.
     *
     * @return The group with given id.
     */
    public List<Group> getMyGroups(int groupId) {
        List<Group> groups = new ArrayList<>();
        Group group = null;
        SQLiteDatabase db = dbm.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + GROUP_TABLE + " WHERE " + GROUP_ID + "=?";
        String[] args = {String.valueOf(groupId)};
        Log.d(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, args);
        if (c != null && c.moveToNext()) {
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

        db.update(LOCAL_USER_TABLE, groupValues, where, args);
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
     * The local user joins the group identified by id.
     *
     * @param groupId The id of the group that should be joined.
     */
    public void joinGroup(int groupId) {
        Log.d(TAG, "Join group " + groupId);
        SQLiteDatabase db = dbm.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(GROUP_ID_FOREIGN, groupId);
        values.put(USER_ID_FOREIGN, Util.getInstance(appContext).getLocalUser().getId());
        values.put(USER_GROUP_ACTIVE, true);
        db.insert(USER_GROUP_TABLE, null, values);

        // Notify observers that database content has changed.
        Intent databaseChanged = new Intent(JOIN_GROUP);
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
     * The local user leaves the group identified by id.
     *
     * @param groupId The id of the group that should be left.
     */
    public void leaveGroup(int groupId) {
        Log.d(TAG, "Leave group " + groupId);
        SQLiteDatabase db = dbm.getWritableDatabase();

        String where = GROUP_ID_FOREIGN + "=? AND " + USER_ID_FOREIGN + "=?";
        String[] args = {String.valueOf(groupId), String.valueOf(Util.getInstance(appContext).getLocalUser().getId())};
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
}
