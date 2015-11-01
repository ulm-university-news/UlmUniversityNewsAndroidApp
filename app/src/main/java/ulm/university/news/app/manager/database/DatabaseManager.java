package ulm.university.news.app.manager.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * The DatabaseManager class provides basic functionality to open and retrieve a connection to the SQLite database.
 * If database and tables are not existing yet, they will be created.
 *
 * @author Matthias Mak
 */
public class DatabaseManager extends SQLiteOpenHelper {
    /** This classes tag for logging. */
    private static final String LOG_TAG = "DatabaseManager";
    /** The name of the database. */
    private static final String DATABASE_NAME = "ulm_university_news.db";
    /** The version of the database. */
    private static final int DATABASE_VERSION = 1;

    // Columns of the LocalUser table.
    public static final String LOCAL_USER_TABLE = "LocalUser";
    public static final String LOCAL_USER_ID = "_id";
    public static final String LOCAL_USER_NAME = "Name";
    public static final String LOCAL_USER_SERVER_ACCESS_TOKEN = "ServerAccessToken";
    public static final String LOCAL_USER_PUSH_ACCESS_TOKEN = "PushAccessToken";
    public static final String LOCAL_USER_PLATFORM = "Platform";

    /** SQL statement to create the LocalUser table. */
    private static final String CREATE_TABLE_LOCAL_USER = "CREATE TABLE " + LOCAL_USER_TABLE + "("
            + LOCAL_USER_ID + " INTEGER PRIMARY KEY, "
            + LOCAL_USER_NAME + " TEXT NOT NULL, "
            + LOCAL_USER_SERVER_ACCESS_TOKEN + " TEXT NOT NULL, "
            + LOCAL_USER_PUSH_ACCESS_TOKEN + " TEXT NOT NULL, "
            + LOCAL_USER_PLATFORM + " INTEGER NOT NULL);";

    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.d(LOG_TAG, "Creating database " + DATABASE_NAME + " and tables.");
        // Create tables.
        database.execSQL(CREATE_TABLE_LOCAL_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DatabaseManager.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion
                + ", which will destroy all old data.");
        db.execSQL("DROP TABLE IF EXISTS " + LOCAL_USER_TABLE);
        onCreate(db);
    }
}
