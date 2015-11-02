package ulm.university.news.app.manager.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * The DatabaseManager class provides basic functionality to open and retrieve a connection to the SQLite database.
 * If database and tables are not existing yet, they will be created. This class implements the Singleton pattern to
 * ensure proper database access for concurrent threads.
 *
 * @author Matthias Mak
 */
public class DatabaseManager extends SQLiteOpenHelper {
    /** This classes tag for logging. */
    private static final String LOG_TAG = "DatabaseManager";
    /** The single instance of DatabaseManager. */
    private static DatabaseManager _instance;
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

    // Columns of the Channel table.
    public static final String CHANNEL_TABLE = "Channel";
    public static final String CHANNEL_ID = "_id";
    public static final String CHANNEL_NAME = "Name";
    public static final String CHANNEL_DESCRIPTION = "Description";
    public static final String CHANNEL_TYPE = "Type";
    public static final String CHANNEL_TERM = "Term";
    public static final String CHANNEL_LOCATIONS = "Locations";
    public static final String CHANNEL_CONTACTS = "Contacts";
    public static final String CHANNEL_CREATION_DATE = "CreationDate";
    public static final String CHANNEL_MODIFICATION_DATE = "ModificationDate";
    public static final String CHANNEL_DATES = "Dates";
    public static final String CHANNEL_WEBSITE = "Website";

    /** SQL statement to create the LocalUser table. */
    private static final String CREATE_TABLE_CHANNEL = "CREATE TABLE " + CHANNEL_TABLE + "("
            + CHANNEL_ID + " INTEGER PRIMARY KEY, "
            + CHANNEL_NAME + " TEXT NOT NULL, "
            + CHANNEL_DESCRIPTION + " TEXT, "
            + CHANNEL_TYPE + " INTEGER NOT NULL, "
            + CHANNEL_TERM + " TEXT, "
            + CHANNEL_LOCATIONS + " TEXT, "
            + CHANNEL_CONTACTS + " TEXT NOT NULL, "
            + CHANNEL_CREATION_DATE + " TEXT NOT NULL, "
            + CHANNEL_MODIFICATION_DATE + " TEXT NOT NULL, "
            + CHANNEL_DATES + " TEXT, "
            + CHANNEL_WEBSITE + " TEXT);";

    // Column of the SubscribedChannels table.
    public static final String SUBSCRIBED_CHANNELS_TABLE = "SubscribedChannels";

    /** SQL statement to create the SubscribedChannels table. */
    private static final String CREATE_TABLE_SUBSCRIBED_CHANNELS = "CREATE TABLE " + SUBSCRIBED_CHANNELS_TABLE + "("
            + CHANNEL_ID + " INTEGER PRIMARY KEY, "
            + "FOREIGN KEY(" + CHANNEL_ID + ") REFERENCES " + CHANNEL_TABLE + "(" + CHANNEL_ID + "));";

    /** SQL statement to enable foreign key support. */
    private static final String FOREIGN_KEYS_ON = "PRAGMA foreign_keys=ON;";

    /**
     * Get the instance of the DatabaseManager class.
     *
     * @param context The context from which this method is called.
     * @return Instance of DatabaseManager.
     */
    public static synchronized DatabaseManager getInstance(Context context) {
        if (_instance == null) {
            _instance = new DatabaseManager(context);
        }
        return _instance;
    }

    /**
     * Creates the instance of DatabaseManager.
     *
     * @param context The context from which this method is called.
     */
    private DatabaseManager(Context context) {
        // Use the application context, which will ensure that one don't accidentally leak an Activity's context.
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.i(LOG_TAG, "Creating database " + DATABASE_NAME);
        // Enable foreign key support.
        Log.i(LOG_TAG, FOREIGN_KEYS_ON);
        database.execSQL(FOREIGN_KEYS_ON);
        // Create tables.
        Log.i(LOG_TAG, CREATE_TABLE_LOCAL_USER);
        database.execSQL(CREATE_TABLE_LOCAL_USER);
        Log.i(LOG_TAG, CREATE_TABLE_CHANNEL);
        database.execSQL(CREATE_TABLE_CHANNEL);
        Log.i(LOG_TAG, CREATE_TABLE_SUBSCRIBED_CHANNELS);
        database.execSQL(CREATE_TABLE_SUBSCRIBED_CHANNELS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DatabaseManager.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion
                + ", which will destroy all old data.");
        db.execSQL("DROP TABLE IF EXISTS " + LOCAL_USER_TABLE);
        onCreate(db);
    }
}
