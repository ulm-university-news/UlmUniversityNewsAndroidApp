package ulm.university.news.app.manager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.Event;
import ulm.university.news.app.data.Lecture;
import ulm.university.news.app.data.Sports;
import ulm.university.news.app.util.exceptions.DatabaseException;

import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_CONTACTS;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_CREATION_DATE;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_DATES;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_DESCRIPTION;
import static ulm.university.news.app.manager.database.DatabaseManager.CHANNEL_ID;
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

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class ChannelDatabaseManager {
    /** This classes tag for logging. */
    private static final String LOG_TAG = "ChannelDatabaseManager";
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
     * @throws DatabaseException If a database failure occurred.
     */
    public void storeChannel(Channel channel) throws DatabaseException {
        Log.d(LOG_TAG, "Start with channel: " + channel);
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
                    subClassValues.put(LECTURE_FACULTY, lecture.getFaculty().ordinal());
                    subClassValues.put(LECTURE_START_DATE, lecture.getStartDate());
                    subClassValues.put(LECTURE_END_DATE, lecture.getEndDate());
                    subClassValues.put(LECTURE_LECTURER, lecture.getLecturer());
                    subClassValues.put(LECTURE_ASSISTANT, lecture.getAssistant());
                    db.insertOrThrow(LECTURE_TABLE, null, subClassValues);
                    break;
                case EVENT:
                    Event event = (Event) channel;
                    subClassValues.put(EVENT_COST, event.getCost());
                    subClassValues.put(EVENT_ORGANIZER, event.getOrganizer());
                    db.insertOrThrow(EVENT_TABLE, null, subClassValues);
                    break;
                case SPORTS:
                    Sports sports = (Sports) channel;
                    subClassValues.put(SPORTS_COST, sports.getCost());
                    subClassValues.put(SPORTS_PARTICIPANTS, sports.getNumberOfParticipants());
                    db.insertOrThrow(SPORTS_TABLE, null, subClassValues);
                    break;
            }
            // Mark transaction as successful.
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Database failure during storeChannel. Need to rollback transaction.");
            throw new DatabaseException("Database failure during storeChannel.");
        } finally {
            if (db != null) {
                // Commit on success or rollback transaction if an error has occurred.
                db.endTransaction();
            }
        }
        Log.d(LOG_TAG, "End. Channel stored successfully.");
    }
}
