package ulm.university.news.app.manager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import ulm.university.news.app.data.LocalUser;
import ulm.university.news.app.data.enums.Platform;

import static ulm.university.news.app.manager.database.DatabaseManager.LOCAL_USER_ID;
import static ulm.university.news.app.manager.database.DatabaseManager.LOCAL_USER_NAME;
import static ulm.university.news.app.manager.database.DatabaseManager.LOCAL_USER_PLATFORM;
import static ulm.university.news.app.manager.database.DatabaseManager.LOCAL_USER_PUSH_ACCESS_TOKEN;
import static ulm.university.news.app.manager.database.DatabaseManager.LOCAL_USER_SERVER_ACCESS_TOKEN;
import static ulm.university.news.app.manager.database.DatabaseManager.LOCAL_USER_TABLE;

/**
 * TODO
 * Methods won't throw exceptions if database failure of whatever kind occurs.
 *
 * @author Matthias Mak
 */
public class UserDatabaseManager {
    /** This classes tag for logging. */
    private static final String TAG = "UserDatabaseManager";
    /** The instance of DatabaseManager. */
    private DatabaseManager dbm;

    /** Creates a new instance of UserDatabaseManager. */
    public UserDatabaseManager(Context context) {
        dbm = DatabaseManager.getInstance(context);
    }

    /**
     * Stores the local user in the database.
     *
     * @param localUser The local user of the app.
     */
    public void storeLocalUser(LocalUser localUser) {
        Log.d(TAG, "Store " + localUser);
        SQLiteDatabase db = dbm.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LOCAL_USER_ID, localUser.getId());
        values.put(LOCAL_USER_NAME, localUser.getName());
        values.put(LOCAL_USER_SERVER_ACCESS_TOKEN, localUser.getServerAccessToken());
        values.put(LOCAL_USER_PUSH_ACCESS_TOKEN, localUser.getPushAccessToken());
        values.put(LOCAL_USER_PLATFORM, localUser.getPlatform().ordinal());
        db.insert(LOCAL_USER_TABLE, null, values);
    }

    /**
     * Retrieves the local user from the database.
     *
     * @return The local user.
     */
    public LocalUser getLocalUser() {
        LocalUser localUser = null;
        SQLiteDatabase db = dbm.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + LOCAL_USER_TABLE;
        Log.d(TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.moveToFirst()) {
            localUser = new LocalUser();
            localUser.setId(c.getInt(c.getColumnIndex(LOCAL_USER_ID)));
            localUser.setName((c.getString(c.getColumnIndex(LOCAL_USER_NAME))));
            localUser.setServerAccessToken(c.getString(c.getColumnIndex(LOCAL_USER_SERVER_ACCESS_TOKEN)));
            localUser.setPushAccessToken(c.getString(c.getColumnIndex(LOCAL_USER_PUSH_ACCESS_TOKEN)));
            localUser.setPlatform(Platform.values[c.getInt(c.getColumnIndex(LOCAL_USER_PLATFORM))]);
            c.close();
        }
        Log.d(TAG, "End with " + localUser);
        return localUser;
    }

    /**
     * Updates the local user in the database. Only the users name and push access token can be updated.
     *
     * @param localUser The updated local user of the app.
     */
    public void updateLocalUser(LocalUser localUser) {
        Log.d(TAG, "Update " + localUser);
        SQLiteDatabase db = dbm.getWritableDatabase();

        ContentValues values = new ContentValues();
        // values.put(LOCAL_USER_ID, localUser.getId());
        values.put(LOCAL_USER_NAME, localUser.getName());
        // values.put(LOCAL_USER_SERVER_ACCESS_TOKEN, localUser.getServerAccessToken());
        values.put(LOCAL_USER_PUSH_ACCESS_TOKEN, localUser.getPushAccessToken());
        // values.put(LOCAL_USER_PLATFORM, localUser.getPlatform().ordinal());

        db.update(LOCAL_USER_TABLE, values, null, null);
    }
}
