package ulm.university.news.app.manager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

    /** Creates a new instance of ChannelDatabaseManager. */
    public ChannelDatabaseManager(Context context) {
        dbm = DatabaseManager.getInstance(context);
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
            description = c.getString(c.getColumnIndex(CHANNEL_NAME));
            type = ChannelType.values[(c.getInt(c.getColumnIndex(CHANNEL_TYPE)))];
            term = c.getString(c.getColumnIndex(CHANNEL_TERM));
            locations = c.getString(c.getColumnIndex(CHANNEL_LOCATIONS));
            contacts = c.getString(c.getColumnIndex(CHANNEL_CONTACTS));
            creationDate = new DateTime(c.getInt(c.getColumnIndex(CHANNEL_CREATION_DATE)), TIME_ZONE);
            modificationDate = new DateTime(c.getInt(c.getColumnIndex(CHANNEL_MODIFICATION_DATE)), TIME_ZONE);
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
        Cursor cSuper = db.rawQuery(selectQuery, null);
        while (cSuper != null && cSuper.moveToNext()) {
            id = cSuper.getInt(cSuper.getColumnIndex(CHANNEL_ID));
            name = cSuper.getString(cSuper.getColumnIndex(CHANNEL_NAME));
            description = cSuper.getString(cSuper.getColumnIndex(CHANNEL_NAME));
            type = ChannelType.values[(cSuper.getInt(cSuper.getColumnIndex(CHANNEL_TYPE)))];
            term = cSuper.getString(cSuper.getColumnIndex(CHANNEL_TERM));
            locations = cSuper.getString(cSuper.getColumnIndex(CHANNEL_LOCATIONS));
            contacts = cSuper.getString(cSuper.getColumnIndex(CHANNEL_CONTACTS));
            creationDate = new DateTime(cSuper.getInt(cSuper.getColumnIndex(CHANNEL_CREATION_DATE)), TIME_ZONE);
            modificationDate = new DateTime(cSuper.getInt(cSuper.getColumnIndex(CHANNEL_MODIFICATION_DATE)), TIME_ZONE);
            dates = cSuper.getString(cSuper.getColumnIndex(CHANNEL_DATES));
            website = cSuper.getString(cSuper.getColumnIndex(CHANNEL_WEBSITE));
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
        if (cSuper != null) {
            cSuper.close();
        }
        Log.d(TAG, "End with " + channels);
        return channels;
    }
}
