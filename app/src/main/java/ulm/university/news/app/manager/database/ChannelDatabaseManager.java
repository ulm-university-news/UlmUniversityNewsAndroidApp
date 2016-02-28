package ulm.university.news.app.manager.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import ulm.university.news.app.data.Announcement;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.Event;
import ulm.university.news.app.data.Lecture;
import ulm.university.news.app.data.Reminder;
import ulm.university.news.app.data.Sports;
import ulm.university.news.app.data.enums.ChannelType;
import ulm.university.news.app.data.enums.Faculty;
import ulm.university.news.app.data.enums.Priority;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.manager.database.DatabaseManager.ANNOUNCEMENT_AUTHOR;
import static ulm.university.news.app.manager.database.DatabaseManager.ANNOUNCEMENT_MESSAGE_NUMBER;
import static ulm.university.news.app.manager.database.DatabaseManager.ANNOUNCEMENT_TABLE;
import static ulm.university.news.app.manager.database.DatabaseManager.ANNOUNCEMENT_TITLE;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_CONTACTS;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_CREATION_DATE;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_DATES;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_DELETED;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_DESCRIPTION;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_ID;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_ID_FOREIGN;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_LOCATIONS;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_MODIFICATION_DATE;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_NAME;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_TABLE;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_TERM;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_TYPE;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_WEBSITE;
import static ulm.university.news.app.manager.database.DatabaseManager.EVENT_COST;
import static ulm.university.news.app.manager.database.DatabaseManager.EVENT_ORGANIZER;
import static ulm.university.news.app.manager.database.DatabaseManager.EVENT_TABLE;
import static ulm.university.news.app.manager.database.DatabaseManager.LECTURE_ASSISTANT;
import static ulm.university.news.app.manager.database.DatabaseManager.LECTURE_END_DATE;
import static ulm.university.news.app.manager.database.DatabaseManager.LECTURE_FACULTY;
import static ulm.university.news.app.manager.database.DatabaseManager.LECTURE_LECTURER;
import static ulm.university.news.app.manager.database.DatabaseManager.LECTURE_START_DATE;
import static ulm.university.news.app.manager.database.DatabaseManager.LECTURE_TABLE;
import static ulm.university.news.app.manager.database.DatabaseManager.MESSAGE_CREATION_DATE;
import static ulm.university.news.app.manager.database.DatabaseManager.MESSAGE_ID;
import static ulm.university.news.app.manager.database.DatabaseManager.MESSAGE_ID_FOREIGN;
import static ulm.university.news.app.manager.database.DatabaseManager.MESSAGE_PRIORITY;
import static ulm.university.news.app.manager.database.DatabaseManager.MESSAGE_READ;
import static ulm.university.news.app.manager.database.DatabaseManager.MESSAGE_TABLE;
import static ulm.university.news.app.manager.database.DatabaseManager.MESSAGE_TEXT;
import static ulm.university.news.app.manager.database.DatabaseManager.MODERATOR_CHANNEL_ACTIVE;
import static ulm.university.news.app.manager.database.DatabaseManager.MODERATOR_CHANNEL_TABLE;
import static ulm.university.news.app.manager.database.DatabaseManager.MODERATOR_ID_FOREIGN;
import static ulm.university.news.app.manager.database.DatabaseManager.REMINDER_AUTHOR;
import static ulm.university.news.app.manager.database.DatabaseManager.REMINDER_CREATION_DATE;
import static ulm.university.news.app.manager.database.DatabaseManager.REMINDER_END_DATE;
import static ulm.university.news.app.manager.database.DatabaseManager.REMINDER_ID;
import static ulm.university.news.app.manager.database.DatabaseManager.REMINDER_IGNORE;
import static ulm.university.news.app.manager.database.DatabaseManager.REMINDER_INTERVAL;
import static ulm.university.news.app.manager.database.DatabaseManager.REMINDER_MODIFICATION_DATE;
import static ulm.university.news.app.manager.database.DatabaseManager.REMINDER_PRIORITY;
import static ulm.university.news.app.manager.database.DatabaseManager.REMINDER_START_DATE;
import static ulm.university.news.app.manager.database.DatabaseManager.REMINDER_TABLE;
import static ulm.university.news.app.manager.database.DatabaseManager.REMINDER_TEXT;
import static ulm.university.news.app.manager.database.DatabaseManager.REMINDER_TITLE;
import static ulm.university.news.app.manager.database.DatabaseManager.SPORTS_COST;
import static ulm.university.news.app.manager.database.DatabaseManager.SPORTS_PARTICIPANTS;
import static ulm.university.news.app.manager.database.DatabaseManager.SPORTS_TABLE;
import static ulm.university.news.app.manager.database.DatabaseManager.SUBSCRIBED_CHANNELS_TABLE;
import static ulm.university.news.app.util.Constants.TIME_ZONE;

/**
 * TODO
 * Methods won't throw exceptions if database failure of whatever kind occurs.
 *
 * @author Matthias Mak
 */
public class ChannelDatabaseManager {
    /** This classes tag for logging. */
    private static final String TAG = "ChannelDatabaseManager";
    /** The instance of DatabaseManager. */
    private DatabaseManager dbm;
    /** The application context. */
    private Context appContext;

    public static final String STORE_CHANNEL = "storeChannel";
    public static final String UPDATE_CHANNEL = "updateChannel";
    public static final String MARK_CHANNEL_DELETED = "markChannelDeleted";
    public static final String SUBSCRIBE_CHANNEL = "subscribeChannel";
    public static final String UNSUBSCRIBE_CHANNEL = "unsubscribeChannel";
    public static final String STORE_ANNOUNCEMENT = "storeAnnouncement";
    public static final String STORE_REMINDER = "storeReminder";
    public static final String UPDATE_REMINDER = "updateReminder";
    public static final String MODERATE_CHANNEL = "moderateChannel";
    public static final String DELETE_CHANNEL = "deleteChannel";
    public static final String DELETE_REMINDER = "deleteReminder";

    /** Creates a new instance of ChannelDatabaseManager. */
    public ChannelDatabaseManager(Context context) {
        dbm = DatabaseManager.getInstance(context);
        appContext = context.getApplicationContext();
    }

    /**
     * Stores the given channel in the database.
     *
     * @param channel The channel which should be stored.
     */
    public void storeChannel(Channel channel) {
        Log.d(TAG, "Store " + channel);
        SQLiteDatabase db = null;
        try {
            db = dbm.getWritableDatabase();

            ContentValues channelValues = new ContentValues();
            channelValues.put(CHANNEL_ID, channel.getId());
            channelValues.put(CHANNEL_NAME, channel.getName());
            channelValues.put(CHANNEL_DESCRIPTION, channel.getDescription());
            channelValues.put(CHANNEL_TYPE, channel.getType().ordinal());
            channelValues.put(CHANNEL_TERM, channel.getTerm());
            channelValues.put(CHANNEL_LOCATIONS, channel.getLocations());
            channelValues.put(CHANNEL_CONTACTS, channel.getContacts());
            channelValues.put(CHANNEL_CREATION_DATE, channel.getCreationDate().getMillis());
            channelValues.put(CHANNEL_MODIFICATION_DATE, channel.getModificationDate().getMillis());
            channelValues.put(CHANNEL_DATES, channel.getDates());
            channelValues.put(CHANNEL_WEBSITE, channel.getWebsite());
            channelValues.put(CHANNEL_DELETED, false);

            // If there are two insert statements make sure that they are performed in one transaction.
            db.beginTransaction();
            db.insertOrThrow(CHANNEL_TABLE, null, channelValues);

            // Check if there is a subclass of channel which has to be stored in another table.
            ContentValues subClassValues = new ContentValues();
            switch (channel.getType()) {
                case LECTURE:
                    Lecture lecture = (Lecture) channel;
                    subClassValues.put(CHANNEL_ID_FOREIGN, channel.getId());
                    subClassValues.put(LECTURE_FACULTY, lecture.getFaculty().ordinal());
                    subClassValues.put(LECTURE_START_DATE, lecture.getStartDate());
                    subClassValues.put(LECTURE_END_DATE, lecture.getEndDate());
                    subClassValues.put(LECTURE_LECTURER, lecture.getLecturer());
                    subClassValues.put(LECTURE_ASSISTANT, lecture.getAssistant());
                    db.insertOrThrow(LECTURE_TABLE, null, subClassValues);
                    break;
                case EVENT:
                    Event event = (Event) channel;
                    subClassValues.put(CHANNEL_ID_FOREIGN, channel.getId());
                    subClassValues.put(EVENT_COST, event.getCost());
                    subClassValues.put(EVENT_ORGANIZER, event.getOrganizer());
                    db.insertOrThrow(EVENT_TABLE, null, subClassValues);
                    break;
                case SPORTS:
                    Sports sports = (Sports) channel;
                    subClassValues.put(CHANNEL_ID_FOREIGN, channel.getId());
                    subClassValues.put(SPORTS_COST, sports.getCost());
                    subClassValues.put(SPORTS_PARTICIPANTS, sports.getNumberOfParticipants());
                    db.insertOrThrow(SPORTS_TABLE, null, subClassValues);
                    break;
            }

            // Notify observers that database content has changed.
            Intent databaseChanged = new Intent(STORE_CHANNEL);
            Log.d(TAG, "sendBroadcast: " + LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged));

            // Mark transaction as successful.
            db.setTransactionSuccessful();
            Log.d(TAG, "End. Channel stored successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Database failure during storeChannel(). Need to rollback transaction.");
        } finally {
            if (db != null) {
                // Commit on success or rollback transaction if an error has occurred.
                db.endTransaction();
            }
        }
    }

    /**
     * Updates the given channel in the database.
     *
     * @param channel The updated channel.
     */
    public void updateChannel(Channel channel) {
        Log.d(TAG, "Update " + channel);
        SQLiteDatabase db = null;
        try {
            db = dbm.getWritableDatabase();
            ContentValues channelValues = new ContentValues();
            channelValues.put(CHANNEL_NAME, channel.getName());
            channelValues.put(CHANNEL_DESCRIPTION, channel.getDescription());
            channelValues.put(CHANNEL_TYPE, channel.getType().ordinal());
            channelValues.put(CHANNEL_TERM, channel.getTerm());
            channelValues.put(CHANNEL_LOCATIONS, channel.getLocations());
            channelValues.put(CHANNEL_CONTACTS, channel.getContacts());
            channelValues.put(CHANNEL_CREATION_DATE, channel.getCreationDate().getMillis());
            channelValues.put(CHANNEL_MODIFICATION_DATE, channel.getModificationDate().getMillis());
            channelValues.put(CHANNEL_DATES, channel.getDates());
            channelValues.put(CHANNEL_WEBSITE, channel.getWebsite());

            String where = CHANNEL_ID + "=?";
            String[] whereArgs = new String[]{String.valueOf(channel.getId())};

            // If there are two update statements make sure that they are performed in one transaction.
            db.beginTransaction();
            db.update(CHANNEL_TABLE, channelValues, where, whereArgs);

            // Check if there is a subclass of channel which has to be updated in another table.
            ContentValues subClassValues = new ContentValues();
            switch (channel.getType()) {
                case LECTURE:
                    Lecture lecture = (Lecture) channel;
                    subClassValues.put(LECTURE_FACULTY, lecture.getFaculty().ordinal());
                    subClassValues.put(LECTURE_START_DATE, lecture.getStartDate());
                    subClassValues.put(LECTURE_END_DATE, lecture.getEndDate());
                    subClassValues.put(LECTURE_LECTURER, lecture.getLecturer());
                    subClassValues.put(LECTURE_ASSISTANT, lecture.getAssistant());
                    where = CHANNEL_ID_FOREIGN + "=" + channel.getId();
                    db.update(LECTURE_TABLE, subClassValues, where, null);
                    break;
                case EVENT:
                    Event event = (Event) channel;
                    subClassValues.put(EVENT_COST, event.getCost());
                    subClassValues.put(EVENT_ORGANIZER, event.getOrganizer());
                    where = CHANNEL_ID_FOREIGN + "=" + channel.getId();
                    db.update(EVENT_TABLE, subClassValues, where, null);
                    break;
                case SPORTS:
                    Sports sports = (Sports) channel;
                    subClassValues.put(SPORTS_COST, sports.getCost());
                    subClassValues.put(SPORTS_PARTICIPANTS, sports.getNumberOfParticipants());
                    where = CHANNEL_ID_FOREIGN + "=" + channel.getId();
                    db.update(SPORTS_TABLE, subClassValues, where, null);
                    break;
            }

            // Notify observers that database content has changed.
            Intent databaseChanged = new Intent(UPDATE_CHANNEL);
            LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged);

            // Mark transaction as successful.
            db.setTransactionSuccessful();
            Log.d(TAG, "End. Channel updated successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Database failure during updateChannel(). Need to rollback transaction.");
        } finally {
            if (db != null) {
                // Commit on success or rollback transaction if an error has occurred.
                db.endTransaction();
            }
        }
    }

    /**
     * Marks a channel as deleted.
     *
     * @param channelId The channel which should be set to deleted.
     */
    public void setChannelToDeleted(int channelId) {
        Log.d(TAG, "Set channel with id " + channelId + " to deleted.");
        SQLiteDatabase db = dbm.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CHANNEL_DELETED, true);
        String where = CHANNEL_ID + "=?";
        String[] args = {String.valueOf(channelId)};
        db.update(CHANNEL_TABLE, values, where, args);

        // Notify observers that database content has changed.
        Intent databaseChanged = new Intent(MARK_CHANNEL_DELETED);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged);
    }

    /**
     * Deletes the channel identified by id. Deletes rows of other tables if linked to the channel.
     *
     * @param channelId The id of the channel.
     */
    public void deleteChannel(int channelId) {
        Log.d(TAG, "Delete channel with id " + channelId);
        SQLiteDatabase db = dbm.getWritableDatabase();
        String whereClause = CHANNEL_ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(channelId)};
        db.delete(CHANNEL_TABLE, whereClause, whereArgs);
    }

    /**
     * Gets the channel identified by id. The returned channel object may be a subclass of channel.
     *
     * @param channelId The id of the channel.
     * @return The channel object.
     */
    public Channel getChannel(int channelId) {
        SQLiteDatabase db = dbm.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + CHANNEL_TABLE + " WHERE " + CHANNEL_ID + "=?";
        String[] args = {String.valueOf(channelId)};
        Log.d(TAG, selectQuery + " -> " + channelId);

        // Create channel fields.
        Channel channel = null;
        String name, description, term, locations, dates, contacts, website, startDate, endDate, lecturer,
                assistant, cost, organizer, participants;
        DateTime creationDate, modificationDate;
        ChannelType type;
        Faculty faculty;
        int id;
        boolean deleted;

        // Get channel data from database.
        Cursor c = db.rawQuery(selectQuery, args);
        if (c != null && c.moveToFirst()) {
            id = c.getInt(c.getColumnIndex(CHANNEL_ID));
            name = c.getString(c.getColumnIndex(CHANNEL_NAME));
            description = c.getString(c.getColumnIndex(CHANNEL_DESCRIPTION));
            type = ChannelType.values[(c.getInt(c.getColumnIndex(CHANNEL_TYPE)))];
            term = c.getString(c.getColumnIndex(CHANNEL_TERM));
            locations = c.getString(c.getColumnIndex(CHANNEL_LOCATIONS));
            contacts = c.getString(c.getColumnIndex(CHANNEL_CONTACTS));
            creationDate = new DateTime(c.getLong(c.getColumnIndex(CHANNEL_CREATION_DATE)), TIME_ZONE);
            modificationDate = new DateTime(c.getLong(c.getColumnIndex(CHANNEL_MODIFICATION_DATE)), TIME_ZONE);
            dates = c.getString(c.getColumnIndex(CHANNEL_DATES));
            website = c.getString(c.getColumnIndex(CHANNEL_WEBSITE));
            deleted = c.getInt(c.getColumnIndex(CHANNEL_DELETED)) == 1;
            c.close();

            // If necessary get additional channel data and create corresponding channel subclass.
            switch (type) {
                case LECTURE:
                    selectQuery = "SELECT * FROM " + LECTURE_TABLE + " WHERE " + CHANNEL_ID_FOREIGN + "=?";
                    c = db.rawQuery(selectQuery, args);
                    if (c != null && c.moveToFirst()) {
                        faculty = Faculty.values[c.getInt(c.getColumnIndex(LECTURE_FACULTY))];
                        startDate = c.getString(c.getColumnIndex(LECTURE_START_DATE));
                        endDate = c.getString(c.getColumnIndex(LECTURE_END_DATE));
                        lecturer = c.getString(c.getColumnIndex(LECTURE_LECTURER));
                        assistant = c.getString(c.getColumnIndex(LECTURE_ASSISTANT));
                        channel = new Lecture(id, name, description, type, creationDate, modificationDate, term,
                                locations, dates, contacts, website, faculty, startDate, endDate, lecturer, assistant);
                        c.close();
                    }
                    break;
                case EVENT:
                    selectQuery = "SELECT * FROM " + EVENT_TABLE + " WHERE " + CHANNEL_ID_FOREIGN + "=?";
                    c = db.rawQuery(selectQuery, args);
                    if (c != null && c.moveToFirst()) {
                        cost = c.getString(c.getColumnIndex(EVENT_COST));
                        organizer = c.getString(c.getColumnIndex(EVENT_ORGANIZER));
                        channel = new Event(id, name, description, type, creationDate, modificationDate, term,
                                locations, dates, contacts, website, cost, organizer);
                        c.close();
                    }
                    break;
                case SPORTS:
                    selectQuery = "SELECT * FROM " + SPORTS_TABLE + " WHERE " + CHANNEL_ID_FOREIGN + "=?";
                    c = db.rawQuery(selectQuery, args);
                    if (c != null && c.moveToFirst()) {
                        cost = c.getString(c.getColumnIndex(SPORTS_COST));
                        participants = c.getString(c.getColumnIndex(SPORTS_PARTICIPANTS));
                        channel = new Sports(id, name, description, type, creationDate, modificationDate, term,
                                locations, dates, contacts, website, cost, participants);
                        c.close();
                    }
                    break;
                default:
                    channel = new Channel(id, name, description, type, creationDate, modificationDate, term,
                            locations, dates, contacts, website);
            }
            if (channel != null) {
                // Add weather the channel is marked as deleted or not.
                channel.setDeleted(deleted);
            }
        }
        Log.d(TAG, "End with " + channel);
        return channel;
    }

    /**
     * Gets all channels from the database. The returned channel objects may be a subclasses of channel.
     *
     * @return A list of channel objects.
     */
    public List<Channel> getChannels() {
        SQLiteDatabase db = dbm.getReadableDatabase();
        List<Channel> channels = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + CHANNEL_TABLE;
        String[] args = new String[1];
        Log.d(TAG, selectQuery);

        // Create fields before while loop, not within every pass.
        Channel channel = null;
        String name, description, term, locations, dates, contacts, website, startDate, endDate, lecturer,
                assistant, cost, organizer, participants;
        DateTime creationDate, modificationDate;
        ChannelType type;
        Faculty faculty;
        int id;
        boolean deleted;

        // Get channel data from database.
        Cursor cSub;
        Cursor cSup = db.rawQuery(selectQuery, null);
        while (cSup != null && cSup.moveToNext()) {
            id = cSup.getInt(cSup.getColumnIndex(CHANNEL_ID));
            name = cSup.getString(cSup.getColumnIndex(CHANNEL_NAME));
            description = cSup.getString(cSup.getColumnIndex(CHANNEL_DESCRIPTION));
            type = ChannelType.values[(cSup.getInt(cSup.getColumnIndex(CHANNEL_TYPE)))];
            term = cSup.getString(cSup.getColumnIndex(CHANNEL_TERM));
            locations = cSup.getString(cSup.getColumnIndex(CHANNEL_LOCATIONS));
            contacts = cSup.getString(cSup.getColumnIndex(CHANNEL_CONTACTS));
            creationDate = new DateTime(cSup.getLong(cSup.getColumnIndex(CHANNEL_CREATION_DATE)), TIME_ZONE);
            modificationDate = new DateTime(cSup.getLong(cSup.getColumnIndex(CHANNEL_MODIFICATION_DATE)), TIME_ZONE);
            dates = cSup.getString(cSup.getColumnIndex(CHANNEL_DATES));
            website = cSup.getString(cSup.getColumnIndex(CHANNEL_WEBSITE));
            deleted = cSup.getInt(cSup.getColumnIndex(CHANNEL_DELETED)) == 1;
            args[0] = String.valueOf(id);

            // If necessary get additional channel data and create corresponding channel subclass.
            switch (type) {
                case LECTURE:
                    selectQuery = "SELECT * FROM " + LECTURE_TABLE + " WHERE " + CHANNEL_ID_FOREIGN + "=?";
                    cSub = db.rawQuery(selectQuery, args);
                    if (cSub != null && cSub.moveToFirst()) {
                        faculty = Faculty.values[cSub.getInt(cSub.getColumnIndex(LECTURE_FACULTY))];
                        startDate = cSub.getString(cSub.getColumnIndex(LECTURE_START_DATE));
                        endDate = cSub.getString(cSub.getColumnIndex(LECTURE_END_DATE));
                        lecturer = cSub.getString(cSub.getColumnIndex(LECTURE_LECTURER));
                        assistant = cSub.getString(cSub.getColumnIndex(LECTURE_ASSISTANT));
                        channel = new Lecture(id, name, description, type, creationDate, modificationDate, term,
                                locations, dates, contacts, website, faculty, startDate, endDate, lecturer, assistant);
                        cSub.close();
                    }
                    break;
                case EVENT:
                    selectQuery = "SELECT * FROM " + EVENT_TABLE + " WHERE " + CHANNEL_ID_FOREIGN + "=?";
                    cSub = db.rawQuery(selectQuery, args);
                    if (cSub != null && cSub.moveToFirst()) {
                        cost = cSub.getString(cSub.getColumnIndex(EVENT_COST));
                        organizer = cSub.getString(cSub.getColumnIndex(EVENT_ORGANIZER));
                        channel = new Event(id, name, description, type, creationDate, modificationDate, term,
                                locations, dates, contacts, website, cost, organizer);
                        cSub.close();
                    }
                    break;
                case SPORTS:
                    selectQuery = "SELECT * FROM " + SPORTS_TABLE + " WHERE " + CHANNEL_ID_FOREIGN + "=?";
                    cSub = db.rawQuery(selectQuery, args);
                    if (cSub != null && cSub.moveToFirst()) {
                        cost = cSub.getString(cSub.getColumnIndex(SPORTS_COST));
                        participants = cSub.getString(cSub.getColumnIndex(SPORTS_PARTICIPANTS));
                        channel = new Sports(id, name, description, type, creationDate, modificationDate, term,
                                locations, dates, contacts, website, cost, participants);
                        cSub.close();
                    }
                    break;
                default:
                    channel = new Channel(id, name, description, type, creationDate, modificationDate, term,
                            locations, dates, contacts, website);
            }
            if (channel != null) {
                // Add weather the channel is marked as deleted or not.
                channel.setDeleted(deleted);
            }
            // Add created channel to the channel list.
            channels.add(channel);
        }
        if (cSup != null) {
            cSup.close();
        }
        Log.d(TAG, "End with " + channels);
        return channels;
    }

    /**
     * Gets all subscribed channels from the database. The returned channel objects may be a subclasses of channel.
     *
     * @return A list of channel objects.
     */
    public List<Channel> getSubscribedChannels() {
        SQLiteDatabase db = dbm.getReadableDatabase();
        List<Channel> channels = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + CHANNEL_TABLE + " AS c INNER JOIN " + SUBSCRIBED_CHANNELS_TABLE
                + " AS s ON c." + CHANNEL_ID + "=s." + CHANNEL_ID_FOREIGN;
        String[] args = new String[1];
        Log.d(TAG, selectQuery);

        // Create fields before while loop, not within every pass.
        Channel channel = null;
        String name, description, term, locations, dates, contacts, website, startDate, endDate, lecturer,
                assistant, cost, organizer, participants;
        DateTime creationDate, modificationDate;
        ChannelType type;
        Faculty faculty;
        int id;
        boolean deleted;

        // Get channel data from database.
        Cursor cSub;
        Cursor cSup = db.rawQuery(selectQuery, null);
        while (cSup != null && cSup.moveToNext()) {
            id = cSup.getInt(cSup.getColumnIndex(CHANNEL_ID));
            name = cSup.getString(cSup.getColumnIndex(CHANNEL_NAME));
            description = cSup.getString(cSup.getColumnIndex(CHANNEL_DESCRIPTION));
            type = ChannelType.values[(cSup.getInt(cSup.getColumnIndex(CHANNEL_TYPE)))];
            term = cSup.getString(cSup.getColumnIndex(CHANNEL_TERM));
            locations = cSup.getString(cSup.getColumnIndex(CHANNEL_LOCATIONS));
            contacts = cSup.getString(cSup.getColumnIndex(CHANNEL_CONTACTS));
            creationDate = new DateTime(cSup.getLong(cSup.getColumnIndex(CHANNEL_CREATION_DATE)), TIME_ZONE);
            modificationDate = new DateTime(cSup.getLong(cSup.getColumnIndex(CHANNEL_MODIFICATION_DATE)), TIME_ZONE);
            dates = cSup.getString(cSup.getColumnIndex(CHANNEL_DATES));
            website = cSup.getString(cSup.getColumnIndex(CHANNEL_WEBSITE));
            deleted = cSup.getInt(cSup.getColumnIndex(CHANNEL_DELETED)) == 1;
            args[0] = String.valueOf(id);

            // If necessary get additional channel data and create corresponding channel subclass.
            switch (type) {
                case LECTURE:
                    selectQuery = "SELECT * FROM " + LECTURE_TABLE + " WHERE " + CHANNEL_ID_FOREIGN + "=?";
                    cSub = db.rawQuery(selectQuery, args);
                    if (cSub != null && cSub.moveToFirst()) {
                        faculty = Faculty.values[cSub.getInt(cSub.getColumnIndex(LECTURE_FACULTY))];
                        startDate = cSub.getString(cSub.getColumnIndex(LECTURE_START_DATE));
                        endDate = cSub.getString(cSub.getColumnIndex(LECTURE_END_DATE));
                        lecturer = cSub.getString(cSub.getColumnIndex(LECTURE_LECTURER));
                        assistant = cSub.getString(cSub.getColumnIndex(LECTURE_ASSISTANT));
                        channel = new Lecture(id, name, description, type, creationDate, modificationDate, term,
                                locations, dates, contacts, website, faculty, startDate, endDate, lecturer, assistant);
                        cSub.close();
                    }
                    break;
                case EVENT:
                    selectQuery = "SELECT * FROM " + EVENT_TABLE + " WHERE " + CHANNEL_ID_FOREIGN + "=?";
                    cSub = db.rawQuery(selectQuery, args);
                    if (cSub != null && cSub.moveToFirst()) {
                        cost = cSub.getString(cSub.getColumnIndex(EVENT_COST));
                        organizer = cSub.getString(cSub.getColumnIndex(EVENT_ORGANIZER));
                        channel = new Event(id, name, description, type, creationDate, modificationDate, term,
                                locations, dates, contacts, website, cost, organizer);
                        cSub.close();
                    }
                    break;
                case SPORTS:
                    selectQuery = "SELECT * FROM " + SPORTS_TABLE + " WHERE " + CHANNEL_ID_FOREIGN + "=?";
                    cSub = db.rawQuery(selectQuery, args);
                    if (cSub != null && cSub.moveToFirst()) {
                        cost = cSub.getString(cSub.getColumnIndex(SPORTS_COST));
                        participants = cSub.getString(cSub.getColumnIndex(SPORTS_PARTICIPANTS));
                        channel = new Sports(id, name, description, type, creationDate, modificationDate, term,
                                locations, dates, contacts, website, cost, participants);
                        cSub.close();
                    }
                    break;
                default:
                    channel = new Channel(id, name, description, type, creationDate, modificationDate, term,
                            locations, dates, contacts, website);
            }
            // Add count of unread announcements and weather the channel is deleted to the channel.
            if (channel != null) {
                channel.setNumberOfUnreadAnnouncements(getNumberOfUnreadAnnouncements(id));
                channel.setDeleted(deleted);
            }
            // Add created channel to the channel list.
            channels.add(channel);
        }
        if (cSup != null) {
            cSup.close();
        }
        Log.d(TAG, "End with " + channels);
        return channels;
    }

    /**
     * Gets all channels for which the local moderator is responsible from the database. The returned channel objects
     * may be a subclasses of channel.
     *
     * @return A list of channel objects.
     */
    public List<Channel> getResponsibleChannels() {
        SQLiteDatabase db = dbm.getReadableDatabase();
        List<Channel> channels = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + CHANNEL_TABLE + " AS c INNER JOIN " + MODERATOR_CHANNEL_TABLE
                + " AS mc ON c." + CHANNEL_ID + "=mc." + CHANNEL_ID_FOREIGN
                + " WHERE mc." + MODERATOR_ID_FOREIGN + "=? AND c." + CHANNEL_DELETED + "=?";
        String[] args = new String[2];
        args[0] = String.valueOf(Util.getInstance(appContext).getLoggedInModerator().getId());
        args[1] = String.valueOf(0);
        Log.d(TAG, selectQuery);

        // Create fields before while loop, not within every pass.
        Channel channel = null;
        String name, description, term, locations, dates, contacts, website, startDate, endDate, lecturer,
                assistant, cost, organizer, participants;
        DateTime creationDate, modificationDate;
        ChannelType type;
        Faculty faculty;
        int id;
        boolean deleted;

        // Get channel data from database.
        Cursor cSub;
        Cursor cSup = db.rawQuery(selectQuery, args);
        args = new String[1];
        while (cSup != null && cSup.moveToNext()) {
            id = cSup.getInt(cSup.getColumnIndex(CHANNEL_ID));
            name = cSup.getString(cSup.getColumnIndex(CHANNEL_NAME));
            description = cSup.getString(cSup.getColumnIndex(CHANNEL_DESCRIPTION));
            type = ChannelType.values[(cSup.getInt(cSup.getColumnIndex(CHANNEL_TYPE)))];
            term = cSup.getString(cSup.getColumnIndex(CHANNEL_TERM));
            locations = cSup.getString(cSup.getColumnIndex(CHANNEL_LOCATIONS));
            contacts = cSup.getString(cSup.getColumnIndex(CHANNEL_CONTACTS));
            creationDate = new DateTime(cSup.getLong(cSup.getColumnIndex(CHANNEL_CREATION_DATE)), TIME_ZONE);
            modificationDate = new DateTime(cSup.getLong(cSup.getColumnIndex(CHANNEL_MODIFICATION_DATE)), TIME_ZONE);
            dates = cSup.getString(cSup.getColumnIndex(CHANNEL_DATES));
            website = cSup.getString(cSup.getColumnIndex(CHANNEL_WEBSITE));
            deleted = cSup.getInt(cSup.getColumnIndex(CHANNEL_DELETED)) == 1;
            args[0] = String.valueOf(id);

            // If necessary get additional channel data and create corresponding channel subclass.
            switch (type) {
                case LECTURE:
                    selectQuery = "SELECT * FROM " + LECTURE_TABLE + " WHERE " + CHANNEL_ID_FOREIGN + "=?";
                    cSub = db.rawQuery(selectQuery, args);
                    if (cSub != null && cSub.moveToFirst()) {
                        faculty = Faculty.values[cSub.getInt(cSub.getColumnIndex(LECTURE_FACULTY))];
                        startDate = cSub.getString(cSub.getColumnIndex(LECTURE_START_DATE));
                        endDate = cSub.getString(cSub.getColumnIndex(LECTURE_END_DATE));
                        lecturer = cSub.getString(cSub.getColumnIndex(LECTURE_LECTURER));
                        assistant = cSub.getString(cSub.getColumnIndex(LECTURE_ASSISTANT));
                        channel = new Lecture(id, name, description, type, creationDate, modificationDate, term,
                                locations, dates, contacts, website, faculty, startDate, endDate, lecturer, assistant);
                        cSub.close();
                    }
                    break;
                case EVENT:
                    selectQuery = "SELECT * FROM " + EVENT_TABLE + " WHERE " + CHANNEL_ID_FOREIGN + "=?";
                    cSub = db.rawQuery(selectQuery, args);
                    if (cSub != null && cSub.moveToFirst()) {
                        cost = cSub.getString(cSub.getColumnIndex(EVENT_COST));
                        organizer = cSub.getString(cSub.getColumnIndex(EVENT_ORGANIZER));
                        channel = new Event(id, name, description, type, creationDate, modificationDate, term,
                                locations, dates, contacts, website, cost, organizer);
                        cSub.close();
                    }
                    break;
                case SPORTS:
                    selectQuery = "SELECT * FROM " + SPORTS_TABLE + " WHERE " + CHANNEL_ID_FOREIGN + "=?";
                    cSub = db.rawQuery(selectQuery, args);
                    if (cSub != null && cSub.moveToFirst()) {
                        cost = cSub.getString(cSub.getColumnIndex(SPORTS_COST));
                        participants = cSub.getString(cSub.getColumnIndex(SPORTS_PARTICIPANTS));
                        channel = new Sports(id, name, description, type, creationDate, modificationDate, term,
                                locations, dates, contacts, website, cost, participants);
                        cSub.close();
                    }
                    break;
                default:
                    channel = new Channel(id, name, description, type, creationDate, modificationDate, term,
                            locations, dates, contacts, website);
            }
            // Add count of unread announcements and weather the channel is deleted to the channel.
            if (channel != null) {
                channel.setNumberOfUnreadAnnouncements(getNumberOfUnreadAnnouncements(id));
                channel.setDeleted(deleted);
            }
            // Add created channel to the channel list.
            channels.add(channel);
        }
        if (cSup != null) {
            cSup.close();
        }
        Log.d(TAG, "End with " + channels);
        return channels;
    }

    /**
     * Subscribes the local user to the channel identified by id.
     *
     * @param channelId The id of the channel that should be subscribed.
     */
    public void subscribeChannel(int channelId) {
        Log.d(TAG, "Subscribe channel " + channelId);
        SQLiteDatabase db = dbm.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CHANNEL_ID_FOREIGN, channelId);
        db.insert(SUBSCRIBED_CHANNELS_TABLE, null, values);

        // Notify observers that database content has changed.
        Intent databaseChanged = new Intent(SUBSCRIBE_CHANNEL);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged);
    }

    /**
     * Unsubscribes the local user from the channel identified by id.
     *
     * @param channelId The id of the channel that should be unsubscribed.
     */
    public void unsubscribeChannel(int channelId) {
        Log.d(TAG, "Unubscribe channel " + channelId);
        SQLiteDatabase db = dbm.getWritableDatabase();

        String where = CHANNEL_ID_FOREIGN + "=?";
        String[] args = {String.valueOf(channelId)};
        db.delete(SUBSCRIBED_CHANNELS_TABLE, where, args);

        // Notify observers that database content has changed.
        Intent databaseChanged = new Intent(UNSUBSCRIBE_CHANNEL);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged);
    }

    /**
     * Checks if the channel identified by given id is subscribed or not.
     *
     * @param channelId The id of the channel that should be checked.
     * @return true if the channel is subscribed.
     */
    public boolean isSubscribedChannel(int channelId) {
        SQLiteDatabase db = dbm.getReadableDatabase();
        boolean isSubscribedChannel = false;

        String selectQuery = "SELECT * FROM " + SUBSCRIBED_CHANNELS_TABLE + " WHERE " + CHANNEL_ID_FOREIGN + "=?";
        String[] args = {String.valueOf(channelId)};
        Log.d(TAG, selectQuery + " -> " + channelId);

        Cursor c = db.rawQuery(selectQuery, args);
        if (c != null && c.moveToFirst()) {
            isSubscribedChannel = true;
            c.close();
        }
        Log.d(TAG, "End with " + isSubscribedChannel);
        return isSubscribedChannel;
    }

    /**
     * Adds the moderator with given id as responsible moderator to the channel with given id.
     *
     * @param channelId The channel id.
     * @param moderatorId The moderator id.
     */
    public void moderateChannel(int channelId, int moderatorId) {
        Log.d(TAG, "Moderate channel " + channelId + " by moderator " + moderatorId);
        SQLiteDatabase db = dbm.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CHANNEL_ID_FOREIGN, channelId);
        values.put(MODERATOR_ID_FOREIGN, moderatorId);
        values.put(MODERATOR_CHANNEL_ACTIVE, true);

        try {
            db.insertOrThrow(MODERATOR_CHANNEL_TABLE, null, values);
        } catch (Exception e) {
            if (e.getMessage().contains("constraint failed")) {
                // Database entry already exists. Doesn't matter, so do nothing.
                Log.i(TAG, "Moderator " + moderatorId + " already moderates channel " + channelId);
            } else {
                // Print other errors.
                e.printStackTrace();
            }
        }

        // Notify observers that database content has changed.
        Intent databaseChanged = new Intent(MODERATE_CHANNEL);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged);
    }

    /**
     * Stores the given announcement in the database.
     *
     * @param announcement The announcement which should be stored.
     */
    public void storeAnnouncement(Announcement announcement) {
        Log.d(TAG, "Store " + announcement);
        SQLiteDatabase db = null;
        try {
            db = dbm.getWritableDatabase();

            // Message values.
            ContentValues messageValues = new ContentValues();
            messageValues.put(MESSAGE_ID, announcement.getId());
            messageValues.put(MESSAGE_TEXT, announcement.getText());
            messageValues.put(MESSAGE_CREATION_DATE, announcement.getCreationDate().getMillis());
            messageValues.put(MESSAGE_PRIORITY, announcement.getPriority().ordinal());
            messageValues.put(MESSAGE_READ, false);

            // Announcement values.
            ContentValues announcementValues = new ContentValues();
            announcementValues.put(MESSAGE_ID_FOREIGN, announcement.getId());
            announcementValues.put(CHANNEL_ID_FOREIGN, announcement.getChannelId());
            announcementValues.put(ANNOUNCEMENT_TITLE, announcement.getTitle());
            announcementValues.put(ANNOUNCEMENT_MESSAGE_NUMBER, announcement.getMessageNumber());
            announcementValues.put(ANNOUNCEMENT_AUTHOR, announcement.getAuthorModerator());

            // If there are two insert statements make sure that they are performed in one transaction.
            db.beginTransaction();
            db.insertOrThrow(MESSAGE_TABLE, null, messageValues);
            db.insertOrThrow(ANNOUNCEMENT_TABLE, null, announcementValues);

            // Notify observers that specific database content has changed.
            Intent databaseChanged = new Intent(STORE_ANNOUNCEMENT);
            Log.d(TAG, "sendBroadcast:" + LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged));

            // Mark transaction as successful.
            db.setTransactionSuccessful();
            Log.d(TAG, "End. Announcement stored successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Database failure during storeAnnouncement(). Need to rollback transaction.");
        } finally {
            if (db != null) {
                // Commit on success or rollback transaction if an error has occurred.
                db.endTransaction();
            }
        }
    }

    /**
     * Gets all announcements of a specific channel from the database.
     *
     * @param channelId The id of the channel.
     * @return A list of announcement objects.
     */
    public List<Announcement> getAnnouncements(int channelId) {
        SQLiteDatabase db = dbm.getReadableDatabase();
        List<Announcement> announcements = new ArrayList<>();
        String announcementsQuery = "SELECT * FROM " + MESSAGE_TABLE + " AS m JOIN " + ANNOUNCEMENT_TABLE + " AS a" +
                " ON m." + MESSAGE_ID + "=a." + MESSAGE_ID_FOREIGN + " WHERE a." + CHANNEL_ID_FOREIGN + "=?";
        // "SELECT * FROM Message AS m JOIN Announcement AS a ON m.Id=a.Message_Id WHERE a.Channel_Id=? AND a" +
        //        ".MessageNumber>?;";
        String[] args = {String.valueOf(channelId)};
        Log.d(TAG, announcementsQuery);

        // Create fields before while loop, not within every pass.
        Announcement announcement;
        String text, title;
        int messageId, messageNumber, author;
        boolean read;
        Priority priority;
        DateTime creationDate;

        // Get message data from database.
        Cursor cMessage = db.rawQuery(announcementsQuery, args);
        while (cMessage != null && cMessage.moveToNext()) {
            messageId = cMessage.getInt(cMessage.getColumnIndex(MESSAGE_ID));
            messageNumber = cMessage.getInt(cMessage.getColumnIndex(ANNOUNCEMENT_MESSAGE_NUMBER));
            author = cMessage.getInt(cMessage.getColumnIndex(ANNOUNCEMENT_AUTHOR));
            text = cMessage.getString(cMessage.getColumnIndex(MESSAGE_TEXT));
            title = cMessage.getString(cMessage.getColumnIndex(ANNOUNCEMENT_TITLE));
            priority = Priority.values[(cMessage.getInt(cMessage.getColumnIndex(MESSAGE_PRIORITY)))];
            read = cMessage.getInt(cMessage.getColumnIndex(MESSAGE_READ)) != 0;
            creationDate = new DateTime(cMessage.getLong(cMessage.getColumnIndex(MESSAGE_CREATION_DATE)), TIME_ZONE);

            // Add new announcement to the announcement list.
            announcement = new Announcement(messageId, text, messageNumber, creationDate, priority, channelId,
                    author, title, read);
            announcements.add(announcement);
        }
        if (cMessage != null) {
            cMessage.close();
        }
        Log.d(TAG, "End with " + announcements);
        return announcements;
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
     * Gets the biggest message number of announcements of a specific channel from the database.
     *
     * @param channelId The id of the channel.
     * @return The biggest message number.
     */
    public int getMaxMessageNumberAnnouncement(int channelId) {
        SQLiteDatabase db = dbm.getReadableDatabase();
        String announcementsQuery = "SELECT MAX(a." + ANNOUNCEMENT_MESSAGE_NUMBER + ") FROM " + MESSAGE_TABLE +
                " AS m JOIN " + ANNOUNCEMENT_TABLE + " AS a ON m." + MESSAGE_ID + "=a." + MESSAGE_ID_FOREIGN +
                " WHERE a." + CHANNEL_ID_FOREIGN + "=?";
        String[] args = {String.valueOf(channelId)};
        Log.d(TAG, announcementsQuery);

        // Get message data from database.
        int messageNumber = 0;
        Cursor cMessage = db.rawQuery(announcementsQuery, args);
        if (cMessage != null && cMessage.moveToNext()) {
            messageNumber = cMessage.getInt(0);
            cMessage.close();
        }
        Log.d(TAG, "End with max message number " + messageNumber);
        return messageNumber;
    }

    private int getNumberOfUnreadAnnouncements(int channelId) {
        int count = 0;
        SQLiteDatabase db = dbm.getReadableDatabase();
        String announcementsQuery = "SELECT * FROM " + MESSAGE_TABLE +
                " AS m JOIN " + ANNOUNCEMENT_TABLE + " AS a ON m." + MESSAGE_ID + "=a." + MESSAGE_ID_FOREIGN +
                " WHERE a." + CHANNEL_ID_FOREIGN + "=?";
        String[] args = {String.valueOf(channelId)};
        Log.d(TAG, announcementsQuery);

        // Count unread announcements in database.
        Cursor cMessage = db.rawQuery(announcementsQuery, args);
        while (cMessage != null && cMessage.moveToNext()) {
            if (cMessage.getInt(cMessage.getColumnIndex(MESSAGE_READ)) == 0) {
                count++;
            }
        }
        if (cMessage != null) {
            cMessage.close();
        }

        Log.d(TAG, "End with unread announcements count " + count);
        return count;
    }

    /**
     * Deletes all announcements of the channel identified by id. Deletes entries in the message table linked to the
     * announcements of the channel.
     *
     * @param channelId The id of the channel.
     */
    public void deleteAnnouncements(int channelId) {
        Log.d(TAG, "Delete announcements of channel with id " + channelId);
        SQLiteDatabase db = dbm.getWritableDatabase();
        String whereClause = CHANNEL_ID_FOREIGN + "=?";
        String[] whereArgs = {String.valueOf(channelId)};
        db.delete(ANNOUNCEMENT_TABLE, whereClause, whereArgs);
    }

    /**
     * Stores the given reminder in the database.
     *
     * @param reminder The reminder which should be stored.
     */
    public void storeReminder(Reminder reminder) {
        Log.d(TAG, "Store " + reminder);
        SQLiteDatabase db = dbm.getWritableDatabase();

        // Message values.
        ContentValues reminderValues = new ContentValues();
        reminderValues.put(REMINDER_ID, reminder.getId());
        reminderValues.put(REMINDER_TEXT, reminder.getText());
        reminderValues.put(REMINDER_TITLE, reminder.getTitle());
        reminderValues.put(REMINDER_CREATION_DATE, reminder.getCreationDate().getMillis());
        reminderValues.put(REMINDER_MODIFICATION_DATE, reminder.getModificationDate().getMillis());
        reminderValues.put(REMINDER_START_DATE, reminder.getStartDate().getMillis());
        reminderValues.put(REMINDER_END_DATE, reminder.getEndDate().getMillis());
        reminderValues.put(REMINDER_PRIORITY, reminder.getPriority().ordinal());
        reminderValues.put(REMINDER_IGNORE, reminder.isIgnore());
        reminderValues.put(REMINDER_INTERVAL, reminder.getInterval());
        reminderValues.put(REMINDER_AUTHOR, reminder.getAuthorModerator());
        reminderValues.put(CHANNEL_ID_FOREIGN, reminder.getChannelId());
        db.insert(REMINDER_TABLE, null, reminderValues);

        // Notify observers that specific database content has changed.
        Intent databaseChanged = new Intent(STORE_REMINDER);
        Log.d(TAG, "sendBroadcast:" + LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged));
        Log.d(TAG, "End. Reminder stored successfully.");
    }

    /**
     * Updates the given reminder in the database.
     *
     * @param reminder The reminder which should be updated.
     */
    public void updateReminder(Reminder reminder) {
        Log.d(TAG, "Update reminder with id " + reminder.getId());
        SQLiteDatabase db = dbm.getWritableDatabase();

        ContentValues reminderValues = new ContentValues();
        reminderValues.put(REMINDER_TEXT, reminder.getText());
        reminderValues.put(REMINDER_TITLE, reminder.getTitle());
        reminderValues.put(REMINDER_CREATION_DATE, reminder.getCreationDate().getMillis());
        reminderValues.put(REMINDER_MODIFICATION_DATE, reminder.getModificationDate().getMillis());
        reminderValues.put(REMINDER_START_DATE, reminder.getStartDate().getMillis());
        reminderValues.put(REMINDER_END_DATE, reminder.getEndDate().getMillis());
        reminderValues.put(REMINDER_PRIORITY, reminder.getPriority().ordinal());
        reminderValues.put(REMINDER_IGNORE, reminder.isIgnore());
        reminderValues.put(REMINDER_INTERVAL, reminder.getInterval());
        reminderValues.put(REMINDER_AUTHOR, reminder.getAuthorModerator());
        String where = REMINDER_ID + "=?";
        String[] args = {String.valueOf(reminder.getId())};
        db.update(REMINDER_TABLE, reminderValues, where, args);

        // Notify observers that specific database content has changed.
        Intent databaseChanged = new Intent(UPDATE_REMINDER);
        Log.d(TAG, "sendBroadcast:" + LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged));
        Log.d(TAG, "End. Reminder updated successfully.");
    }

    /**
     * Gets all reminders of a specific channel from the database.
     *
     * @param channelId The id of the channel.
     * @return A list of reminder objects.
     */
    public List<Reminder> getReminders(int channelId) {
        SQLiteDatabase db = dbm.getReadableDatabase();
        List<Reminder> reminders = new ArrayList<>();
        String remindersQuery = "SELECT * FROM " + REMINDER_TABLE + " WHERE " + CHANNEL_ID_FOREIGN + "=?";
        String[] args = {String.valueOf(channelId)};
        Log.d(TAG, remindersQuery);

        // Create fields before while loop, not within every pass.
        Reminder reminder;
        String text, title;
        int id, author, interval;
        boolean ignore;
        Priority priority;
        DateTime creationDate, modificationDate, startDate, endDate;

        // Get message data from database.
        Cursor cReminder = db.rawQuery(remindersQuery, args);
        while (cReminder != null && cReminder.moveToNext()) {
            id = cReminder.getInt(cReminder.getColumnIndex(REMINDER_ID));
            interval = cReminder.getInt(cReminder.getColumnIndex(REMINDER_INTERVAL));
            text = cReminder.getString(cReminder.getColumnIndex(REMINDER_TEXT));
            title = cReminder.getString(cReminder.getColumnIndex(REMINDER_TITLE));
            priority = Priority.values[(cReminder.getInt(cReminder.getColumnIndex(REMINDER_PRIORITY)))];
            ignore = cReminder.getInt(cReminder.getColumnIndex(REMINDER_IGNORE)) != 0;
            author = cReminder.getInt(cReminder.getColumnIndex(REMINDER_AUTHOR));
            creationDate = new DateTime(cReminder.getLong(cReminder.getColumnIndex(REMINDER_CREATION_DATE)), TIME_ZONE);
            modificationDate = new DateTime(cReminder.getLong(cReminder.getColumnIndex(REMINDER_MODIFICATION_DATE)),
                    TIME_ZONE);
            startDate = new DateTime(cReminder.getLong(cReminder.getColumnIndex(REMINDER_START_DATE)), TIME_ZONE);
            endDate = new DateTime(cReminder.getLong(cReminder.getColumnIndex(REMINDER_END_DATE)), TIME_ZONE);

            // Add new reminder to the reminder list.
            reminder = new Reminder(id, creationDate, modificationDate, startDate, endDate, interval, ignore,
                    channelId, author, title, text, priority);
            reminders.add(reminder);
        }
        if (cReminder != null) {
            cReminder.close();
        }
        Log.d(TAG, "End with " + reminders);
        return reminders;
    }

    /**
     * Gets the reminder with the specified id from the database.
     *
     * @param reminderId The id of the reminder.
     * @return The reminder object.
     */
    public Reminder getReminder(int reminderId) {
        SQLiteDatabase db = dbm.getReadableDatabase();
        String remindersQuery = "SELECT * FROM " + REMINDER_TABLE + " WHERE " + REMINDER_ID + "=?";
        String[] args = {String.valueOf(reminderId)};
        Log.d(TAG, remindersQuery);

        // Create fields before while loop, not within every pass.
        Reminder reminder = null;
        String text, title;
        int id, author, interval, channelId;
        boolean ignore;
        Priority priority;
        DateTime creationDate, modificationDate, startDate, endDate;

        // Get message data from database.
        Cursor cReminder = db.rawQuery(remindersQuery, args);
        if (cReminder != null && cReminder.moveToNext()) {
            id = cReminder.getInt(cReminder.getColumnIndex(REMINDER_ID));
            interval = cReminder.getInt(cReminder.getColumnIndex(REMINDER_INTERVAL));
            text = cReminder.getString(cReminder.getColumnIndex(REMINDER_TEXT));
            title = cReminder.getString(cReminder.getColumnIndex(REMINDER_TITLE));
            priority = Priority.values[(cReminder.getInt(cReminder.getColumnIndex(REMINDER_PRIORITY)))];
            ignore = cReminder.getInt(cReminder.getColumnIndex(REMINDER_IGNORE)) != 0;
            author = cReminder.getInt(cReminder.getColumnIndex(REMINDER_AUTHOR));
            creationDate = new DateTime(cReminder.getLong(cReminder.getColumnIndex(REMINDER_CREATION_DATE)), TIME_ZONE);
            modificationDate = new DateTime(cReminder.getLong(cReminder.getColumnIndex(REMINDER_MODIFICATION_DATE)),
                    TIME_ZONE);
            startDate = new DateTime(cReminder.getLong(cReminder.getColumnIndex(REMINDER_START_DATE)), TIME_ZONE);
            endDate = new DateTime(cReminder.getLong(cReminder.getColumnIndex(REMINDER_END_DATE)), TIME_ZONE);
            channelId = cReminder.getInt(cReminder.getColumnIndex(CHANNEL_ID_FOREIGN));

            // Add new reminder to the reminder list.
            reminder = new Reminder(id, creationDate, modificationDate, startDate, endDate, interval, ignore,
                    channelId, author, title, text, priority);
        }
        if (cReminder != null) {
            cReminder.close();
        }
        Log.d(TAG, "End with " + reminder);
        return reminder;
    }

    /**
     * Deletes the reminder identified by id.
     *
     * @param reminderId The id of the reminder.
     */
    public void deleteReminder(int reminderId) {
        Log.d(TAG, "Delete reminder with id " + reminderId);
        SQLiteDatabase db = dbm.getWritableDatabase();
        String whereClause = REMINDER_ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(reminderId)};
        db.delete(REMINDER_TABLE, whereClause, whereArgs);
        // Notify observers that specific database content has changed.
        Intent databaseChanged = new Intent(DELETE_REMINDER);
        Log.d(TAG, "sendBroadcast:" + LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged));
    }
}
