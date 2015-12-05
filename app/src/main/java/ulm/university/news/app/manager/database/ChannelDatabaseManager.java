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

import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.Event;
import ulm.university.news.app.data.Lecture;
import ulm.university.news.app.data.Sports;
import ulm.university.news.app.data.enums.ChannelType;
import ulm.university.news.app.data.enums.Faculty;

import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_CONTACTS;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_CREATION_DATE;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_DATES;
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
    public static final String SUBSCRIBE_CHANNEL = "subscribeChannel";
    public static final String UNSUBSCRIBE_CHANNEL = "unsubscribeChannel";

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
            Log.d(TAG, "sendBroadcast: "+LocalBroadcastManager.getInstance(appContext).sendBroadcast(databaseChanged));

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
            String where = CHANNEL_ID + "=" + channel.getId();

            // If there are two update statements make sure that they are performed in one transaction.
            db.beginTransaction();
            db.update(CHANNEL_TABLE, channelValues, where, null);

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
     * Gets the channel identified by id. The returned channel object may be a subclass of channel.
     *
     * @param channelId The id of the channel.
     * @return The channel object.
     */
    public Channel getChannel(int channelId) {
        SQLiteDatabase db = dbm.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + CHANNEL_TABLE + " WHERE " + CHANNEL_ID + "=?";
        String[] args = {"" + channelId};
        Log.d(TAG, selectQuery + " -> " + channelId);

        // Create channel fields.
        Channel channel = null;
        String name, description, term, locations, dates, contacts, website, startDate, endDate, lecturer,
                assistant, cost, organizer, participants;
        DateTime creationDate, modificationDate;
        ChannelType type;
        Faculty faculty;
        int id;

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
            args[0] = "" + id;

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
            args[0] = "" + id;

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
        String[] args = {"" + channelId};
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
        String[] args = {"" + channelId};
        Log.d(TAG, selectQuery + " -> " + channelId);

        Cursor c = db.rawQuery(selectQuery, args);
        if (c != null && c.moveToFirst()) {
            isSubscribedChannel = true;
            c.close();
        }
        Log.d(TAG, "End with " + isSubscribedChannel);
        return isSubscribedChannel;
    }
}
