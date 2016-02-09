package ulm.university.news.app.manager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ulm.university.news.app.data.Moderator;

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
     * Updates a moderator specified by id in the database. The moderators id can't be updated.
     *
     * @param moderator The updated moderator.
     */
    public void updateModerator(Moderator moderator) {
        Log.d(TAG, "Update " + moderator);
        SQLiteDatabase db = dbm.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MODERATOR_FIRST_NAME, moderator.getFirstName());
        values.put(MODERATOR_LAST_NAME, moderator.getLastName());
        values.put(MODERATOR_EMAIL, moderator.getEmail());

        String where = MODERATOR_ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(moderator.getId())};

        db.update(MODERATOR_TABLE, values, where, whereArgs);
    }

    /**
     * Retrieves the moderator with specified id from the database.
     *
     * @return The local moderator.
     */
    public Moderator getModerator(int moderatorId) {
        Moderator moderator = null;
        SQLiteDatabase db = dbm.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + MODERATOR_TABLE + " WHERE " + MODERATOR_ID + "=?";
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

    /**
     * Retrieves all moderators from the database.
     *
     * @return The a list of moderators.
     */
    public List<Moderator> getModerators() {
        SQLiteDatabase db = dbm.getReadableDatabase();
        List<Moderator> moderators = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + MODERATOR_TABLE;
        Log.d(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null) {
            Moderator moderator;
            while (c.moveToNext()) {
                moderator = new Moderator();
                moderator.setId(c.getInt(c.getColumnIndex(MODERATOR_ID)));
                moderator.setFirstName(c.getString(c.getColumnIndex(MODERATOR_FIRST_NAME)));
                moderator.setLastName(c.getString(c.getColumnIndex(MODERATOR_LAST_NAME)));
                moderator.setEmail(c.getString(c.getColumnIndex(MODERATOR_EMAIL)));
                moderators.add(moderator);
            }
            c.close();
        }
        Log.d(TAG, "End with " + moderators);
        return moderators;
    }
}
