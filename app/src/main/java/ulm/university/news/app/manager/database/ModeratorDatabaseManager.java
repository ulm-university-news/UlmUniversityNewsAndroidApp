package ulm.university.news.app.manager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import ulm.university.news.app.data.Moderator;

import static ulm.university.news.app.manager.database.DatabaseManager.LOCAL_MODERATOR_EMAIL;
import static ulm.university.news.app.manager.database.DatabaseManager.LOCAL_MODERATOR_FIRST_NAME;
import static ulm.university.news.app.manager.database.DatabaseManager.LOCAL_MODERATOR_ID;
import static ulm.university.news.app.manager.database.DatabaseManager.LOCAL_MODERATOR_LAST_NAME;
import static ulm.university.news.app.manager.database.DatabaseManager.LOCAL_MODERATOR_NAME;
import static ulm.university.news.app.manager.database.DatabaseManager.LOCAL_MODERATOR_TABLE;
import static ulm.university.news.app.manager.database.DatabaseManager.MODERATOR_EMAIL;
import static ulm.university.news.app.manager.database.DatabaseManager.MODERATOR_FIRST_NAME;
import static ulm.university.news.app.manager.database.DatabaseManager.MODERATOR_ID;
import static ulm.university.news.app.manager.database.DatabaseManager.MODERATOR_LAST_NAME;
import static ulm.university.news.app.manager.database.DatabaseManager.MODERATOR_TABLE;

/**
 * TODO
 * Methods won't throw exceptions if database failure of whatever kind occurs.
 *
 * @author Matthias Mak
 */
public class ModeratorDatabaseManager {
    /** This classes tag for logging. */
    private static final String TAG = "ModeratorDatabaseMgr";
    /** The instance of DatabaseManager. */
    private DatabaseManager dbm;
    /** The application context. */
    private Context appContext;

    /** Creates a new instance of ModeratorDatabaseManager. */
    public ModeratorDatabaseManager(Context context) {
        dbm = DatabaseManager.getInstance(context);
        appContext = context.getApplicationContext();
    }

    /**
     * Stores the local moderator in the database.
     *
     * @param moderator The local moderator.
     */
    public void storeLocalModerator(Moderator moderator) {
        Log.d(TAG, "Store local " + moderator);
        SQLiteDatabase db = dbm.getWritableDatabase();

        ContentValues moderatorValues = new ContentValues();
        moderatorValues.put(LOCAL_MODERATOR_ID, moderator.getId());
        moderatorValues.put(LOCAL_MODERATOR_NAME, moderator.getName());
        moderatorValues.put(LOCAL_MODERATOR_FIRST_NAME, moderator.getFirstName());
        moderatorValues.put(LOCAL_MODERATOR_LAST_NAME, moderator.getLastName());
        moderatorValues.put(LOCAL_MODERATOR_EMAIL, moderator.getEmail());
        db.insert(LOCAL_MODERATOR_TABLE, null, moderatorValues);
    }

    /**
     * Stores the given moderator in the database.
     *
     * @param moderator The moderator which should be stored.
     */
    public void storeModerator(Moderator moderator) {
        Log.d(TAG, "Store " + moderator);
        SQLiteDatabase db = dbm.getWritableDatabase();

        ContentValues moderatorValues = new ContentValues();
        moderatorValues.put(MODERATOR_ID, moderator.getId());
        moderatorValues.put(MODERATOR_FIRST_NAME, moderator.getFirstName());
        moderatorValues.put(MODERATOR_LAST_NAME, moderator.getLastName());
        moderatorValues.put(MODERATOR_EMAIL, moderator.getEmail());
        db.insert(MODERATOR_TABLE, null, moderatorValues);
    }

    /**
     * Retrieves the local moderator from the database.
     *
     * @return The local moderator.
     */
    public Moderator getLocalModerator() {
        Moderator moderator = null;
        SQLiteDatabase db = dbm.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + LOCAL_MODERATOR_TABLE + ";";
        Log.d(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null) {
            if (c.moveToFirst()) {
                moderator = new Moderator();
                moderator.setId(c.getInt(c.getColumnIndex(LOCAL_MODERATOR_ID)));
                moderator.setName((c.getString(c.getColumnIndex(LOCAL_MODERATOR_NAME))));
                moderator.setFirstName(c.getString(c.getColumnIndex(LOCAL_MODERATOR_FIRST_NAME)));
                moderator.setLastName(c.getString(c.getColumnIndex(LOCAL_MODERATOR_LAST_NAME)));
                moderator.setEmail(c.getString(c.getColumnIndex(LOCAL_MODERATOR_EMAIL)));
            }
            c.close();
        }
        Log.d(TAG, "End with " + moderator);
        return moderator;
    }

    /**
     * Retrieves the local moderator from the database.
     *
     * @return The local moderator.
     */
    public Moderator getModerator(int moderatorId) {
        Moderator moderator = null;
        SQLiteDatabase db = dbm.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + MODERATOR_TABLE + " WHERE " + MODERATOR_ID + "=?;";
        String[] args = {String.valueOf(moderatorId)};
        Log.d(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, args);
        if (c != null) {
            if (c.moveToFirst()) {
                moderator = new Moderator();
                moderator.setId(c.getInt(c.getColumnIndex(MODERATOR_ID)));
                moderator.setFirstName(c.getString(c.getColumnIndex(MODERATOR_FIRST_NAME)));
                moderator.setLastName(c.getString(c.getColumnIndex(MODERATOR_LAST_NAME)));
                moderator.setEmail(c.getString(c.getColumnIndex(MODERATOR_EMAIL)));
            }
            c.close();
        }
        Log.d(TAG, "End with " + moderator);
        return moderator;
    }
}
