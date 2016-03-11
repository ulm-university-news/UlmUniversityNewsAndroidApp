package ulm.university.news.app.manager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import ulm.university.news.app.data.Settings;
import ulm.university.news.app.data.enums.Language;
import ulm.university.news.app.data.enums.NotificationSettings;
import ulm.university.news.app.data.enums.OrderSettings;

import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_ID;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_TABLE;
import static ulm.university.news.app.manager.database.DatabaseManager.GROUP_ID;
import static ulm.university.news.app.manager.database.DatabaseManager.GROUP_TABLE;
import static ulm.university.news.app.manager.database.DatabaseManager.SETTINGS_MESSAGE;
import static ulm.university.news.app.manager.database.DatabaseManager.SETTINGS_BALLOT;
import static ulm.university.news.app.manager.database.DatabaseManager.SETTINGS_CHANNEL;
import static ulm.university.news.app.manager.database.DatabaseManager.SETTINGS_CONVERSATION;
import static ulm.university.news.app.manager.database.DatabaseManager.SETTINGS_GENERAL;
import static ulm.university.news.app.manager.database.DatabaseManager.SETTINGS_GROUP;
import static ulm.university.news.app.manager.database.DatabaseManager.SETTINGS_ID;
import static ulm.university.news.app.manager.database.DatabaseManager.SETTINGS_LANGUAGE;
import static ulm.university.news.app.manager.database.DatabaseManager.SETTINGS_NOTIFICATION;
import static ulm.university.news.app.manager.database.DatabaseManager.SETTINGS_TABLE;

/**
 * TODO
 * Methods won't throw exceptions if database failure of whatever kind occurs.
 *
 * @author Matthias Mak
 */
public class SettingsDatabaseManager {
    /** This classes tag for logging. */
    private static final String TAG = "SettingsDatabaseManager";
    /** The instance of DatabaseManager. */
    private DatabaseManager dbm;

    /** Creates a new instance of SettingsDatabaseManager. */
    public SettingsDatabaseManager(Context context) {
        dbm = DatabaseManager.getInstance(context);
    }

    /**
     * Stores the default settings in the database. This method should be called only once on user account creation.
     */
    public void createDefaultSettings() {
        Log.d(TAG, "Store default settings.");
        SQLiteDatabase db = dbm.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SETTINGS_ID, 0);
        values.put(SETTINGS_CHANNEL, OrderSettings.TYPE_AND_FACULTY.ordinal());
        values.put(SETTINGS_CONVERSATION, OrderSettings.LATEST_DATE.ordinal());
        values.put(SETTINGS_GROUP, OrderSettings.ALPHABETICAL.ordinal());
        values.put(SETTINGS_BALLOT, OrderSettings.LATEST_DATE.ordinal());
        values.put(SETTINGS_MESSAGE, OrderSettings.ASCENDING.ordinal());
        values.put(SETTINGS_GENERAL, OrderSettings.ASCENDING.ordinal());
        values.put(SETTINGS_LANGUAGE, Language.GERMAN.ordinal());
        values.put(SETTINGS_NOTIFICATION, NotificationSettings.ALL.ordinal());
        db.insert(SETTINGS_TABLE, null, values);
    }

    /**
     * Retrieves the the app wide settings from the database.
     *
     * @return The the settings object.
     */
    public Settings getSettings() {
        Settings settings = null;
        SQLiteDatabase db = dbm.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + SETTINGS_TABLE;
        Log.d(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.moveToFirst()) {
            settings = new Settings();
            settings.setChannelSettings(OrderSettings.values[c.getInt(c.getColumnIndex(SETTINGS_CHANNEL))]);
            settings.setMessageSettings(OrderSettings.values[c.getInt(c.getColumnIndex(SETTINGS_MESSAGE))]);
            settings.setConversationSettings(OrderSettings.values[c.getInt(c.getColumnIndex(SETTINGS_CONVERSATION))]);
            settings.setBallotSettings(OrderSettings.values[c.getInt(c.getColumnIndex(SETTINGS_BALLOT))]);
            settings.setGeneralSettings(OrderSettings.values[c.getInt(c.getColumnIndex(SETTINGS_GENERAL))]);
            settings.setGroupSettings(OrderSettings.values[c.getInt(c.getColumnIndex(SETTINGS_GROUP))]);
            settings.setLanguage(Language.values[c.getInt(c.getColumnIndex(SETTINGS_LANGUAGE))]);
            settings.setNotificationSettings(NotificationSettings.values[c.getInt(c.getColumnIndex
                    (SETTINGS_NOTIFICATION))]);
            c.close();
        }
        Log.d(TAG, "End with " + settings);
        return settings;
    }

    /**
     * Updates the app wide settings in the database.
     *
     * @param settings The updated settings of the app.
     */
    public void updateSettings(Settings settings) {
        Log.d(TAG, "Update " + settings);
        SQLiteDatabase db = dbm.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SETTINGS_CHANNEL, settings.getChannelSettings().ordinal());
        values.put(SETTINGS_MESSAGE, settings.getMessageSettings().ordinal());
        values.put(SETTINGS_CONVERSATION, settings.getConversationSettings().ordinal());
        values.put(SETTINGS_BALLOT, settings.getBallotSettings().ordinal());
        values.put(SETTINGS_GENERAL, settings.getGeneralSettings().ordinal());
        values.put(SETTINGS_GROUP, settings.getGroupSettings().ordinal());
        values.put(SETTINGS_LANGUAGE, settings.getLanguage().ordinal());
        values.put(SETTINGS_NOTIFICATION, settings.getNotificationSettings().ordinal());

        db.update(SETTINGS_TABLE, values, null, null);
    }

    /**
     * Gets the notification settings for a specific channel in the database.
     *
     * @param channelId The channels id.
     * @return The channels notification settings.
     */
    public NotificationSettings getChannelNotificationSettings(int channelId) {
        NotificationSettings notificationSettings = null;
        SQLiteDatabase db = dbm.getReadableDatabase();

        String selectQuery = "SELECT " + SETTINGS_NOTIFICATION + " FROM " + CHANNEL_TABLE
                + " WHERE " + CHANNEL_ID + "=?";
        String[] args = {String.valueOf(channelId)};
        Log.d(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, args);
        if (c != null && c.moveToFirst()) {
            notificationSettings = NotificationSettings.values[c.getInt(c.getColumnIndex(SETTINGS_NOTIFICATION))];
            c.close();
        }
        Log.d(TAG, "End with " + notificationSettings);
        return notificationSettings;
    }

    /**
     * Gets the notification settings for a specific group in the database.
     *
     * @param groupId The groups id.
     * @return The groups notification settings.
     */
    public NotificationSettings getGroupNotificationSettings(int groupId) {
        NotificationSettings notificationSettings = null;
        SQLiteDatabase db = dbm.getReadableDatabase();

        String selectQuery = "SELECT " + SETTINGS_NOTIFICATION + " FROM " + GROUP_TABLE
                + " WHERE " + GROUP_ID + "=?";
        String[] args = {String.valueOf(groupId)};
        Log.d(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, args);
        if (c != null && c.moveToFirst()) {
            notificationSettings = NotificationSettings.values[c.getInt(c.getColumnIndex(SETTINGS_NOTIFICATION))];
            c.close();
        }
        Log.d(TAG, "End with " + notificationSettings);
        return notificationSettings;
    }

    /**
     * Updates the notification settings for a specific channel in the database.
     *
     * @param notificationSettings The updated notification settings.
     * @param channelId The channels id.
     */
    public void updateChannelNotificationSettings(int channelId, NotificationSettings notificationSettings) {
        Log.d(TAG, "Update notification settings of channel " + channelId + " to " + notificationSettings);
        SQLiteDatabase db = dbm.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SETTINGS_NOTIFICATION, notificationSettings.ordinal());
        String where = CHANNEL_ID + "=?";
        String[] args = {String.valueOf(channelId)};

        db.update(CHANNEL_TABLE, values, where, args);
    }

    /**
     * Updates the notification settings for a specific group in the database.
     *
     * @param notificationSettings The updated notification settings.
     * @param groupId The groups id.
     */
    public void updateGroupNotificationSettings(int groupId, NotificationSettings notificationSettings) {
        Log.d(TAG, "Update notification settings of group " + groupId + " to " + notificationSettings);
        SQLiteDatabase db = dbm.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SETTINGS_NOTIFICATION, notificationSettings.ordinal());
        String where = GROUP_ID + "=?";
        String[] args = {String.valueOf(groupId)};

        db.update(GROUP_TABLE, values, where, args);
    }
}
