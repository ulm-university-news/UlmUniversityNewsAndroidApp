package ulm.university.news.app.manager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import ulm.university.news.app.data.LocalUser;
import ulm.university.news.app.data.enums.Platform;
import ulm.university.news.app.util.exceptions.DatabaseException;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class UserDatabaseManager extends DatabaseManager {
    /** This classes tag for logging. */
    private static final String LOG_TAG = "UserDatabaseManager";

    public UserDatabaseManager(Context context) {
        super(context);
    }

    /**
     * Stores the local user in the database.
     *
     * @param localUser The local user of the app.
     * @throws DatabaseException If a database failure occurred.
     */
    public void storeLocalUser(LocalUser localUser) throws DatabaseException {
        Log.d(LOG_TAG, "Start with localUser: " + localUser);
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(LOCAL_USER_ID, localUser.getId());
            values.put(LOCAL_USER_NAME, localUser.getName());
            values.put(LOCAL_USER_SERVER_ACCESS_TOKEN, localUser.getServerAccessToken());
            values.put(LOCAL_USER_PUSH_ACCESS_TOKEN, localUser.getPushAccessToken());
            values.put(LOCAL_USER_PLATFORM, localUser.getPlatform().ordinal());
            // TODO No exception?
            db.insert(LOCAL_USER_TABLE, null, values);
        } catch (Exception e) {
            throw new DatabaseException("Database failure while storeLocalUser.");
        }
        Log.d(LOG_TAG, "End. LocalUser created successfully.");
    }

    public LocalUser getLocalUser() {
        LocalUser localUser = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + LOCAL_USER_TABLE + ";";
        Log.d(LOG_TAG, "selectQuery: " + selectQuery);

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
        Log.d(LOG_TAG, "End with localUser: " + localUser);
        return localUser;
    }
}
