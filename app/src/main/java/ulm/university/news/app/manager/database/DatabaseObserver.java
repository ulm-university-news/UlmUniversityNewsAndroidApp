package ulm.university.news.app.manager.database;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * The DatabaseObserver is an observer that listens for a given set of filters. The filter describes to what
 * database actions the observer should listen. This class is used by the DatabaseLoader.
 *
 * @author Matthias Mak
 */
public class DatabaseObserver extends BroadcastReceiver {
    /** This classes tag for logging. */
    private static final String TAG = "DatabaseObserver";

    /** The database loader to which the observer should report changes. */
    private DatabaseLoader loader;

    /**
     * Creates a new DatabaseObserver.
     *
     * @param databaseLoader The database loader to which the observer should report changes.
     * @param filter The filter which describes to what actions the observer should listen.
     */
    public DatabaseObserver(DatabaseLoader databaseLoader, IntentFilter filter) {
        loader = databaseLoader;
        // Register for events related to channel database table.
        LocalBroadcastManager.getInstance(loader.getContext()).registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, intent.getAction());
        // Notify loader about data change.
        loader.onContentChanged();
    }
}
