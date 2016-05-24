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
    private static final String TAG = "DatabaseManager";
    /** The single instance of DatabaseManager. */
    private static DatabaseManager _instance;
    /** The name of the database. */
    private static final String DATABASE_NAME = "ulm_university_news.db";
    /** The version of the database. */
    private static final int DATABASE_VERSION = 1;

    /** SQL statement to enable foreign key support. */
    private static final String FOREIGN_KEYS_ON = "PRAGMA foreign_keys=ON;";

    // Columns of the Settings table.
    public static final String SETTINGS_TABLE = "Settings";
    public static final String SETTINGS_ID = "_id";
    public static final String SETTINGS_CHANNEL = "ChannelSettings";
    public static final String SETTINGS_CONVERSATION = "ConversationSettings";
    public static final String SETTINGS_GROUP = "GroupSettings";
    public static final String SETTINGS_BALLOT = "BallotSettings";
    public static final String SETTINGS_MESSAGE = "MessageSettings";
    public static final String SETTINGS_GENERAL = "GeneralSettings";
    public static final String SETTINGS_LANGUAGE = "LanguageSettings";
    public static final String SETTINGS_NOTIFICATION = "NotificationSettings";

    /** SQL statement to create the Settings table. */
    private static final String CREATE_TABLE_SETTINGS = "CREATE TABLE " + SETTINGS_TABLE + "("
            + SETTINGS_ID + " INTEGER PRIMARY KEY NOT NULL, "
            + SETTINGS_CHANNEL + " INTEGER NOT NULL, "
            + SETTINGS_CONVERSATION + " INTEGER NOT NULL, "
            + SETTINGS_GROUP + " INTEGER NOT NULL, "
            + SETTINGS_BALLOT + " INTEGER NOT NULL, "
            + SETTINGS_MESSAGE + " INTEGER NOT NULL, "
            + SETTINGS_GENERAL + " INTEGER NOT NULL, "
            + SETTINGS_LANGUAGE + " INTEGER NOT NULL, "
            + SETTINGS_NOTIFICATION + " INTEGER NOT NULL);";

    // Columns of the LocalUser table.
    public static final String LOCAL_USER_TABLE = "LocalUser";
    public static final String LOCAL_USER_ID = "_id";
    public static final String LOCAL_USER_NAME = "Name";
    public static final String LOCAL_USER_SERVER_ACCESS_TOKEN = "ServerAccessToken";
    public static final String LOCAL_USER_PUSH_ACCESS_TOKEN = "PushAccessToken";
    public static final String LOCAL_USER_PLATFORM = "Platform";

    /** SQL statement to create the LocalUser table. */
    private static final String CREATE_TABLE_LOCAL_USER = "CREATE TABLE " + LOCAL_USER_TABLE + "("
            + LOCAL_USER_ID + " INTEGER PRIMARY KEY NOT NULL, "
            + LOCAL_USER_NAME + " TEXT NOT NULL, "
            + LOCAL_USER_SERVER_ACCESS_TOKEN + " TEXT NOT NULL, "
            + LOCAL_USER_PUSH_ACCESS_TOKEN + " TEXT NOT NULL, "
            + LOCAL_USER_PLATFORM + " INTEGER NOT NULL);";

    // Columns of the User table.
    public static final String USER_TABLE = "User";
    public static final String USER_ID = "_id";
    public static final String USER_ID_FOREIGN = "User_Id";
    public static final String USER_NAME = "Name";
    public static final String USER_OLD_NAME = "OldName";

    /** SQL statement to create the User table. */
    private static final String CREATE_TABLE_USER = "CREATE TABLE " + USER_TABLE + "("
            + USER_ID + " INTEGER PRIMARY KEY NOT NULL, "
            + USER_NAME + " TEXT NOT NULL, "
            + USER_OLD_NAME + " TEXT);";

    // Columns of the LocalModerator table.
    public static final String LOCAL_MODERATOR_TABLE = "LocalModerator";
    public static final String LOCAL_MODERATOR_ID = "_id";
    public static final String LOCAL_MODERATOR_NAME = "Name";
    public static final String LOCAL_MODERATOR_FIRST_NAME = "FirstName";
    public static final String LOCAL_MODERATOR_LAST_NAME = "LastName";
    public static final String LOCAL_MODERATOR_EMAIL = "Email";

    /** SQL statement to create the LocalModerator table. */
    private static final String CREATE_TABLE_LOCAL_MODERATOR = "CREATE TABLE " + LOCAL_MODERATOR_TABLE + "("
            + LOCAL_MODERATOR_ID + " INTEGER PRIMARY KEY NOT NULL, "
            + LOCAL_MODERATOR_NAME + " TEXT NOT NULL, "
            + LOCAL_MODERATOR_FIRST_NAME + " TEXT NOT NULL, "
            + LOCAL_MODERATOR_LAST_NAME + " TEXT NOT NULL, "
            + LOCAL_MODERATOR_EMAIL + " TEXT NOT NULL);";

    // Columns of the Moderator table.
    public static final String MODERATOR_TABLE = "Moderator";
    public static final String MODERATOR_ID = "_id";
    public static final String MODERATOR_ID_FOREIGN = "Moderator_Id";
    public static final String MODERATOR_FIRST_NAME = "FirstName";
    public static final String MODERATOR_LAST_NAME = "LastName";
    public static final String MODERATOR_EMAIL = "Email";

    /** SQL statement to create the Moderator table. */
    private static final String CREATE_TABLE_MODERATOR = "CREATE TABLE " + MODERATOR_TABLE + "("
            + MODERATOR_ID + " INTEGER PRIMARY KEY NOT NULL, "
            + MODERATOR_FIRST_NAME + " TEXT NOT NULL, "
            + MODERATOR_LAST_NAME + " TEXT NOT NULL, "
            + MODERATOR_EMAIL + " TEXT NOT NULL);";

    // Columns of the Channel table.
    public static final String CHANNEL_TABLE = "Channel";
    public static final String CHANNEL_ID = "_id";
    public static final String CHANNEL_ID_FOREIGN = "Channel_Id";
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
    public static final String CHANNEL_DELETED = "Deleted";
    public static final String CHANNEL_DELETED_READ = "DeletedRead";

    /** SQL statement to create the Channel table. */
    private static final String CREATE_TABLE_CHANNEL = "CREATE TABLE " + CHANNEL_TABLE + "("
            + CHANNEL_ID + " INTEGER PRIMARY KEY NOT NULL, "
            + CHANNEL_NAME + " TEXT NOT NULL, "
            + CHANNEL_DESCRIPTION + " TEXT, "
            + CHANNEL_TYPE + " INTEGER NOT NULL, "
            + CHANNEL_TERM + " TEXT, "
            + CHANNEL_LOCATIONS + " TEXT, "
            + CHANNEL_CONTACTS + " TEXT NOT NULL, "
            + CHANNEL_CREATION_DATE + " INTEGER NOT NULL, "
            + CHANNEL_MODIFICATION_DATE + " INTEGER NOT NULL, "
            + CHANNEL_DATES + " TEXT, "
            + CHANNEL_WEBSITE + " TEXT, "
            + SETTINGS_NOTIFICATION + " INTEGER, "
            + CHANNEL_DELETED + " INTEGER NOT NULL, "
            + CHANNEL_DELETED_READ + " INTEGER NOT NULL);";

    // Column of the SubscribedChannels table.
    public static final String SUBSCRIBED_CHANNELS_TABLE = "SubscribedChannels";

    /** SQL statement to create the SubscribedChannels table. */
    private static final String CREATE_TABLE_SUBSCRIBED_CHANNELS = "CREATE TABLE " + SUBSCRIBED_CHANNELS_TABLE + "("
            + CHANNEL_ID_FOREIGN + " INTEGER PRIMARY KEY NOT NULL, "
            + "FOREIGN KEY(" + CHANNEL_ID_FOREIGN + ") REFERENCES " + CHANNEL_TABLE + "(" + CHANNEL_ID + ") "
            + "ON DELETE CASCADE);";

    // Columns of the Lecture table.
    public static final String LECTURE_TABLE = "Lecture";
    public static final String LECTURE_FACULTY = "Faculty";
    public static final String LECTURE_START_DATE = "StartDate";
    public static final String LECTURE_END_DATE = "EndDate";
    public static final String LECTURE_LECTURER = "Lecturer";
    public static final String LECTURE_ASSISTANT = "Assistant";

    /** SQL statement to create the Lecture table. */
    private static final String CREATE_TABLE_LECTURE = "CREATE TABLE " + LECTURE_TABLE + "("
            + CHANNEL_ID_FOREIGN + " INTEGER PRIMARY KEY NOT NULL, "
            + LECTURE_FACULTY + " INTEGER NOT NULL, "
            + LECTURE_START_DATE + " TEXT, "
            + LECTURE_END_DATE + " TEXT, "
            + LECTURE_LECTURER + " TEXT NOT NULL, "
            + LECTURE_ASSISTANT + " TEXT, "
            + "FOREIGN KEY(" + CHANNEL_ID_FOREIGN + ") REFERENCES " + CHANNEL_TABLE + "(" + CHANNEL_ID + ") ON DELETE CASCADE);";

    // Columns of the Event table.
    public static final String EVENT_TABLE = "Event";
    public static final String EVENT_COST = "Cost";
    public static final String EVENT_ORGANIZER = "Organizer";

    /** SQL statement to create the Event table. */
    private static final String CREATE_TABLE_EVENT = "CREATE TABLE " + EVENT_TABLE + "("
            + CHANNEL_ID_FOREIGN + " INTEGER PRIMARY KEY NOT NULL, "
            + EVENT_COST + " TEXT, "
            + EVENT_ORGANIZER + " TEXT, "
            + "FOREIGN KEY(" + CHANNEL_ID_FOREIGN + ") REFERENCES " + CHANNEL_TABLE + "(" + CHANNEL_ID + ") ON DELETE CASCADE);";

    // Columns of the Sports table.
    public static final String SPORTS_TABLE = "Sports";
    public static final String SPORTS_COST = "Cost";
    public static final String SPORTS_PARTICIPANTS = "NumberOfParticipants";

    /** SQL statement to create the Sports table. */
    private static final String CREATE_TABLE_SPORTS = "CREATE TABLE " + SPORTS_TABLE + "("
            + CHANNEL_ID_FOREIGN + " INTEGER PRIMARY KEY NOT NULL, "
            + SPORTS_COST + " TEXT, "
            + SPORTS_PARTICIPANTS + " TEXT, "
            + "FOREIGN KEY(" + CHANNEL_ID_FOREIGN + ") REFERENCES " + CHANNEL_TABLE + "(" + CHANNEL_ID + ") ON DELETE CASCADE);";

    // Columns of the Message table.
    public static final String MESSAGE_TABLE = "Message";
    public static final String MESSAGE_ID = "_id";
    public static final String MESSAGE_ID_FOREIGN = "Message_Id";
    public static final String MESSAGE_TEXT = "Text";
    public static final String MESSAGE_PRIORITY = "Priority";
    public static final String MESSAGE_CREATION_DATE = "CreationDate";
    public static final String MESSAGE_READ = "Read";

    /** SQL statement to create the Message table. */
    private static final String CREATE_TABLE_MESSAGE = "CREATE TABLE " + MESSAGE_TABLE + "("
            + MESSAGE_ID + " INTEGER PRIMARY KEY NOT NULL, "
            + MESSAGE_TEXT + " TEXT NOT NULL, "
            + MESSAGE_PRIORITY + " INTEGER NOT NULL, "
            + MESSAGE_CREATION_DATE + " INTEGER NOT NULL, "
            + MESSAGE_READ + " INTEGER NOT NULL);";

    // Columns of the Announcement table.
    public static final String ANNOUNCEMENT_TABLE = "Announcement";
    public static final String ANNOUNCEMENT_MESSAGE_NUMBER = "MessageNumber";
    public static final String ANNOUNCEMENT_TITLE = "Title";
    public static final String ANNOUNCEMENT_AUTHOR = "Author_Moderator_Id";

    /** SQL statement to create the Announcement table. */
    private static final String CREATE_TABLE_ANNOUNCEMENT = "CREATE TABLE " + ANNOUNCEMENT_TABLE + "("
            + ANNOUNCEMENT_MESSAGE_NUMBER + " INTEGER NOT NULL, "
            + CHANNEL_ID_FOREIGN + " INTEGER NOT NULL, "
            + ANNOUNCEMENT_TITLE + " TEXT NOT NULL, "
            + ANNOUNCEMENT_AUTHOR + " INTEGER NOT NULL, "
            + MESSAGE_ID_FOREIGN + " INTEGER NOT NULL, "
            + "PRIMARY KEY(" + ANNOUNCEMENT_MESSAGE_NUMBER + ", " + CHANNEL_ID_FOREIGN + "), "
            + "FOREIGN KEY(" + CHANNEL_ID_FOREIGN + ") REFERENCES " + CHANNEL_TABLE + "(" + CHANNEL_ID + ") ON DELETE CASCADE, "
            //        + "FOREIGN KEY(" + ANNOUNCEMENT_AUTHOR + ") REFERENCES " + MODERATOR_TABLE + "(" + MODERATOR_ID + "), "
            + "FOREIGN KEY(" + MESSAGE_ID_FOREIGN + ") REFERENCES " + MESSAGE_TABLE + "(" + MESSAGE_ID + ") ON DELETE CASCADE);";

    // Columns of the Reminder table.
    public static final String REMINDER_TABLE = "Reminder";
    public static final String REMINDER_ID = "_id";
    public static final String REMINDER_AUTHOR = "Author_Moderator_Id";
    public static final String REMINDER_START_DATE = "StartDate";
    public static final String REMINDER_END_DATE = "EndDate";
    public static final String REMINDER_CREATION_DATE = "CreationDate";
    public static final String REMINDER_MODIFICATION_DATE = "ModificationDate";
    public static final String REMINDER_INTERVAL = "Interval";
    public static final String REMINDER_IGNORE = "Ignore";
    public static final String REMINDER_TITLE = "Title";
    public static final String REMINDER_TEXT = "Text";
    public static final String REMINDER_PRIORITY = "Priority";
    public static final String REMINDER_ACTIVE = "Active";

    /** SQL statement to create the Reminder table. */
    private static final String CREATE_TABLE_REMINDER = "CREATE TABLE " + REMINDER_TABLE + "("
            + REMINDER_ID + " INTEGER PRIMARY KEY NOT NULL, "
            + REMINDER_START_DATE + " INTEGER NOT NULL, "
            + REMINDER_END_DATE + " INTEGER NOT NULL, "
            + REMINDER_CREATION_DATE + " INTEGER NOT NULL, "
            + REMINDER_MODIFICATION_DATE + " INTEGER NOT NULL, "
            + REMINDER_INTERVAL + " INTEGER, "
            + REMINDER_IGNORE + " INTEGER NOT NULL, "
            + REMINDER_ACTIVE + " INTEGER NOT NULL, "
            + REMINDER_TITLE + " TEXT NOT NULL, "
            + REMINDER_TEXT + " TEXT NOT NULL, "
            + REMINDER_PRIORITY + " INTEGER NOT NULL, "
            + REMINDER_AUTHOR + " INTEGER NOT NULL, "
            + CHANNEL_ID_FOREIGN + " INTEGER NOT NULL, "
            + "FOREIGN KEY(" + CHANNEL_ID_FOREIGN + ") REFERENCES " + CHANNEL_TABLE + "(" + CHANNEL_ID + ") ON DELETE CASCADE, "
            + "FOREIGN KEY(" + REMINDER_AUTHOR + ") REFERENCES " + MODERATOR_TABLE + "(" + MODERATOR_ID + "));";

    // Columns of the Group table.
    public static final String GROUP_TABLE = "\"Group\"";
    public static final String GROUP_ID = "_id";
    public static final String GROUP_ID_FOREIGN = "Group_Id";
    public static final String GROUP_NAME = "Name";
    public static final String GROUP_DESCRIPTION = "Description";
    public static final String GROUP_TYPE = "Type";
    public static final String GROUP_CREATION_DATE = "CreationDate";
    public static final String GROUP_MODIFICATION_DATE = "ModificationDate";
    public static final String GROUP_TERM = "Term";
    public static final String GROUP_DELETED = "Deleted";
    public static final String GROUP_DELETED_READ = "DeletedRead";
    public static final String GROUP_ADMIN = "GroupAdmin_User_Id";

    /** SQL statement to create the Group table. */
    private static final String CREATE_TABLE_GROUP = "CREATE TABLE " + GROUP_TABLE + "("
            + GROUP_ID + " INTEGER PRIMARY KEY NOT NULL, "
            + GROUP_NAME + " TEXT NOT NULL, "
            + GROUP_DESCRIPTION + " TEXT, "
            + GROUP_TYPE + " INTEGER NOT NULL, "
            + GROUP_CREATION_DATE + " INTEGER NOT NULL, "
            + GROUP_MODIFICATION_DATE + " INTEGER NOT NULL, "
            + GROUP_TERM + " TEXT, "
            + GROUP_DELETED + " INTEGER NOT NULL, "
            + GROUP_DELETED_READ + " INTEGER NOT NULL, "
            + GROUP_ADMIN + " INTEGER NOT NULL, "
            + SETTINGS_NOTIFICATION + " INTEGER);";
    // Don't use constraints to allow an easy group storage process with a later user update process.
    // + "FOREIGN KEY(" + GROUP_ADMIN + ") REFERENCES " + USER_TABLE + "(" + USER_ID + "));";

    // Column of the UserGroup table.
    public static final String USER_GROUP_TABLE = "UserGroup";
    public static final String USER_GROUP_ACTIVE = "Active";

    /** SQL statement to create the UserGroup table. */
    private static final String CREATE_TABLE_USER_GROUP = "CREATE TABLE " + USER_GROUP_TABLE + "("
            + USER_ID_FOREIGN + " INTEGER NOT NULL, "
            + GROUP_ID_FOREIGN + " INTEGER NOT NULL, "
            + USER_GROUP_ACTIVE + " INTEGER NOT NULL, "
            + "PRIMARY KEY(" + USER_ID_FOREIGN + ", " + GROUP_ID_FOREIGN + "), "
            + "FOREIGN KEY(" + USER_ID_FOREIGN + ") REFERENCES " + USER_TABLE + "(" + USER_ID + ") ON DELETE CASCADE, "
            + "FOREIGN KEY(" + GROUP_ID_FOREIGN + ") REFERENCES " + GROUP_TABLE + "(" + GROUP_ID + ") "
            + "ON DELETE CASCADE);";

    // Columns of the Conversation table.
    public static final String CONVERSATION_TABLE = "Conversation";
    public static final String CONVERSATION_ID = "_id";
    public static final String CONVERSATION_ID_FOREIGN = "Conversation_Id";
    public static final String CONVERSATION_TITLE = "Title";
    public static final String CONVERSATION_CLOSED = "Closed";
    public static final String CONVERSATION_ADMIN = "ConversationAdmin_User_Id";

    /** SQL statement to create the Conversation table. */
    private static final String CREATE_TABLE_CONVERSATION = "CREATE TABLE " + CONVERSATION_TABLE + "("
            + CONVERSATION_ID + " INTEGER PRIMARY KEY NOT NULL, "
            + CONVERSATION_TITLE + " TEXT NOT NULL, "
            + CONVERSATION_CLOSED + " INTEGER NOT NULL, "
            + CONVERSATION_ADMIN + " INTEGER NOT NULL, "
            + GROUP_ID_FOREIGN + " INTEGER NOT NULL, "
            + "FOREIGN KEY(" + GROUP_ID_FOREIGN + ") REFERENCES " + GROUP_TABLE + "(" + GROUP_ID + "), "
            + "FOREIGN KEY(" + CONVERSATION_ADMIN + ") REFERENCES " + USER_TABLE + "(" + USER_ID + "));";

    // Columns of the ConversationMessage table.
    public static final String CONVERSATION_MESSAGE_TABLE = "ConversationMessage";
    public static final String CONVERSATION_MESSAGE_MESSAGE_NUMBER = "MessageNumber";
    public static final String CONVERSATION_MESSAGE_AUTHOR = "Author_User_Id";

    /** SQL statement to create the ConversationMessage table. */
    private static final String CREATE_TABLE_CONVERSATION_MESSAGE = "CREATE TABLE " + CONVERSATION_MESSAGE_TABLE + "("
            + CONVERSATION_MESSAGE_MESSAGE_NUMBER + " INTEGER NOT NULL, "
            + CONVERSATION_ID_FOREIGN + " INTEGER NOT NULL, "
            + CONVERSATION_MESSAGE_AUTHOR + " INTEGER NOT NULL, "
            + MESSAGE_ID_FOREIGN + " INTEGER NOT NULL, "
            + "PRIMARY KEY(" + CONVERSATION_MESSAGE_MESSAGE_NUMBER + ", " + CONVERSATION_ID_FOREIGN + "), "
            + "FOREIGN KEY(" + CONVERSATION_ID_FOREIGN + ") REFERENCES "
            + CONVERSATION_TABLE + "(" + CONVERSATION_ID + "), "
            + "FOREIGN KEY(" + CONVERSATION_MESSAGE_AUTHOR + ") REFERENCES " + USER_TABLE + "(" + USER_ID + "), "
            + "FOREIGN KEY(" + MESSAGE_ID_FOREIGN + ") REFERENCES " + MESSAGE_TABLE + "(" + MESSAGE_ID + "));";

    // Columns of the Ballot table.
    public static final String BALLOT_TABLE = "Ballot";
    public static final String BALLOT_ID = "_id";
    public static final String BALLOT_ID_FOREIGN = "Ballot_Id";
    public static final String BALLOT_TITLE = "Title";
    public static final String BALLOT_DESCRIPTION = "Description";
    public static final String BALLOT_MULTIPLE_CHOICE = "MultipleChoice";
    public static final String BALLOT_PUBLIC = "Public";
    public static final String BALLOT_CLOSED = "Closed";
    public static final String BALLOT_ADMIN = "BallotAdmin_User_Id";

    /** SQL statement to create the Ballot table. */
    private static final String CREATE_TABLE_BALLOT = "CREATE TABLE " + BALLOT_TABLE + "("
            + BALLOT_ID + " INTEGER PRIMARY KEY NOT NULL, "
            + BALLOT_TITLE + " TEXT NOT NULL, "
            + BALLOT_DESCRIPTION + " TEXT, "
            + BALLOT_MULTIPLE_CHOICE + " INTEGER NOT NULL, "
            + BALLOT_PUBLIC + " INTEGER NOT NULL, "
            + BALLOT_CLOSED + " INTEGER NOT NULL, "
            + BALLOT_ADMIN + " INTEGER NOT NULL, "
            + GROUP_ID_FOREIGN + " INTEGER NOT NULL, "
            + "FOREIGN KEY(" + GROUP_ID_FOREIGN + ") REFERENCES " + GROUP_TABLE + "(" + GROUP_ID + "), "
            + "FOREIGN KEY(" + BALLOT_ADMIN + ") REFERENCES " + USER_TABLE + "(" + USER_ID + "));";

    // Columns of the Option table.
    public static final String OPTION_TABLE = "Option";
    public static final String OPTION_ID = "_id";
    public static final String OPTION_ID_FOREIGN = "Option_Id";
    public static final String OPTION_TEXT = "Text";

    /** SQL statement to create the Option table. */
    private static final String CREATE_TABLE_OPTION = "CREATE TABLE " + OPTION_TABLE + "("
            + OPTION_ID + " INTEGER PRIMARY KEY NOT NULL, "
            + OPTION_TEXT + " TEXT NOT NULL, "
            + BALLOT_ID_FOREIGN + " INTEGER NOT NULL, "
            + "FOREIGN KEY(" + BALLOT_ID_FOREIGN + ") REFERENCES " + BALLOT_TABLE + "(" + BALLOT_ID + "));";

    // Columns of the UserOption table.
    public static final String USER_OPTION_TABLE = "UserOption";

    /** SQL statement to create the UserOption table. */
    private static final String CREATE_TABLE_USER_OPTION = "CREATE TABLE " + USER_OPTION_TABLE + "("
            + OPTION_ID_FOREIGN + " INTEGER NOT NULL, "
            + USER_ID_FOREIGN + " INTEGER NOT NULL, "
            + "PRIMARY KEY(" + OPTION_ID_FOREIGN + ", " + USER_ID_FOREIGN + "), "
            + "FOREIGN KEY(" + OPTION_ID_FOREIGN + ") REFERENCES " + OPTION_TABLE + "(" + OPTION_ID + "), "
            + "FOREIGN KEY(" + USER_ID_FOREIGN + ") REFERENCES " + USER_TABLE + "(" + USER_ID + "));";

    // Columns of the ModeratorChannel table.
    public static final String MODERATOR_CHANNEL_TABLE = "ModeratorChannel";
    public static final String MODERATOR_CHANNEL_ACTIVE = "Active";

    /** SQL statement to create the ModeratorChannel table. */
    private static final String CREATE_TABLE_MODERATOR_CHANNEL = "CREATE TABLE " + MODERATOR_CHANNEL_TABLE + "("
            + MODERATOR_ID_FOREIGN + " INTEGER NOT NULL, "
            + CHANNEL_ID_FOREIGN + " INTEGER NOT NULL, "
            + MODERATOR_CHANNEL_ACTIVE + " INTEGER NOT NULL, "
            + "PRIMARY KEY(" + MODERATOR_ID_FOREIGN + ", " + CHANNEL_ID_FOREIGN + "), "
            + "FOREIGN KEY(" + MODERATOR_ID_FOREIGN + ") REFERENCES " + MODERATOR_TABLE + "(" + MODERATOR_ID + "), "
            + "FOREIGN KEY(" + CHANNEL_ID_FOREIGN + ") REFERENCES " + CHANNEL_TABLE + "(" + CHANNEL_ID + ") ON DELETE CASCADE);";

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
     * @param context The context from which the constructor is called.
     */
    private DatabaseManager(Context context) {
        // Use the application context, which will ensure that one don't accidentally leak an Activity's context.
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.i(TAG, "Creating database " + DATABASE_NAME);
        // Create tables.
        Log.i(TAG, CREATE_TABLE_LOCAL_USER);
        database.execSQL(CREATE_TABLE_LOCAL_USER);
        Log.i(TAG, CREATE_TABLE_USER);
        database.execSQL(CREATE_TABLE_USER);
        Log.i(TAG, CREATE_TABLE_LOCAL_MODERATOR);
        database.execSQL(CREATE_TABLE_LOCAL_MODERATOR);
        Log.i(TAG, CREATE_TABLE_MODERATOR);
        database.execSQL(CREATE_TABLE_MODERATOR);
        Log.i(TAG, CREATE_TABLE_CHANNEL);
        database.execSQL(CREATE_TABLE_CHANNEL);
        Log.i(TAG, CREATE_TABLE_LECTURE);
        database.execSQL(CREATE_TABLE_LECTURE);
        Log.i(TAG, CREATE_TABLE_EVENT);
        database.execSQL(CREATE_TABLE_EVENT);
        Log.i(TAG, CREATE_TABLE_SPORTS);
        database.execSQL(CREATE_TABLE_SPORTS);
        Log.i(TAG, CREATE_TABLE_MESSAGE);
        database.execSQL(CREATE_TABLE_MESSAGE);
        Log.i(TAG, CREATE_TABLE_ANNOUNCEMENT);
        database.execSQL(CREATE_TABLE_ANNOUNCEMENT);
        Log.i(TAG, CREATE_TABLE_REMINDER);
        database.execSQL(CREATE_TABLE_REMINDER);
        Log.i(TAG, CREATE_TABLE_GROUP);
        database.execSQL(CREATE_TABLE_GROUP);
        Log.i(TAG, CREATE_TABLE_CONVERSATION);
        database.execSQL(CREATE_TABLE_CONVERSATION);
        Log.i(TAG, CREATE_TABLE_CONVERSATION_MESSAGE);
        database.execSQL(CREATE_TABLE_CONVERSATION_MESSAGE);
        Log.i(TAG, CREATE_TABLE_BALLOT);
        database.execSQL(CREATE_TABLE_BALLOT);
        Log.i(TAG, CREATE_TABLE_OPTION);
        database.execSQL(CREATE_TABLE_OPTION);
        Log.i(TAG, CREATE_TABLE_USER_OPTION);
        database.execSQL(CREATE_TABLE_USER_OPTION);
        Log.i(TAG, CREATE_TABLE_SUBSCRIBED_CHANNELS);
        database.execSQL(CREATE_TABLE_SUBSCRIBED_CHANNELS);
        Log.i(TAG, CREATE_TABLE_USER_GROUP);
        database.execSQL(CREATE_TABLE_USER_GROUP);
        Log.i(TAG, CREATE_TABLE_MODERATOR_CHANNEL);
        database.execSQL(CREATE_TABLE_MODERATOR_CHANNEL);
        Log.i(TAG, CREATE_TABLE_SETTINGS);
        database.execSQL(CREATE_TABLE_SETTINGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                + ", which will destroy all old data.");
        db.execSQL("DROP TABLE IF EXISTS " + LOCAL_USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + LOCAL_MODERATOR_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + MODERATOR_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CHANNEL_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + LECTURE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + EVENT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + SPORTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ANNOUNCEMENT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + REMINDER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + GROUP_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CONVERSATION_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CONVERSATION_MESSAGE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + BALLOT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + OPTION_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USER_OPTION_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + SUBSCRIBED_CHANNELS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USER_GROUP_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + MODERATOR_CHANNEL_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + SETTINGS_TABLE);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Foreign keys must be activated every time the database is opened.
        db.execSQL(FOREIGN_KEYS_ON);
    }
}
