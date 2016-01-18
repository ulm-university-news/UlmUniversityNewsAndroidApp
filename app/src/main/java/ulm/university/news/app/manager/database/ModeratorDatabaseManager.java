package ulm.university.news.app.manager.database;

import android.content.ContentValues;
import android.content.Context;
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
}
