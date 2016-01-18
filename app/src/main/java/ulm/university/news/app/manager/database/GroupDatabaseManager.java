package ulm.university.news.app.manager.database;

import android.content.Context;

/**
 * TODO
 * Methods won't throw exceptions if database failure of whatever kind occurs.
 *
 * @author Matthias Mak
 */
public class GroupDatabaseManager {
    /** This classes tag for logging. */
    private static final String TAG = "GroupDatabaseManager";
    /** The instance of DatabaseManager. */
    private DatabaseManager dbm;
    /** The application context. */
    private Context appContext;

    /** Creates a new instance of GroupDatabaseManager. */
    public GroupDatabaseManager(Context context) {
        dbm = DatabaseManager.getInstance(context);
        appContext = context.getApplicationContext();
    }
}
