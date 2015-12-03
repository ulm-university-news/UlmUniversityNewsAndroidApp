package ulm.university.news.app.manager.loader;

/**
 * TODO
 *
 * @author Matthias Mak
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import ulm.university.news.app.manager.database.ChannelDatabaseManager;

/**
 * Used by the {@link ChannelLoader}. An observer that listens for
 * application installs, removals, and updates (and notifies the loader when
 * these changes are detected).
 */
public class DatabaseObserver extends BroadcastReceiver {
    private static final String TAG = "DatabaseObserver";

    private ChannelLoader loader;

    public DatabaseObserver(ChannelLoader channelLoader) {
        loader = channelLoader;

        // Register for events related to channel database table.
        IntentFilter filter = new IntentFilter();
        filter.addAction(ChannelDatabaseManager.STORE_CHANNEL);
        filter.addAction(ChannelDatabaseManager.UPDATE_CHANNEL);
        LocalBroadcastManager.getInstance(loader.getContext()).registerReceiver(this, filter);

        // Register for events related to ...
        IntentFilter otherFilter = new IntentFilter();
        otherFilter.addAction("other");
        LocalBroadcastManager.getInstance(loader.getContext()).registerReceiver(this, otherFilter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "+++ The observer has detected a database change! Notifying Loader... +++ " + intent.getAction());

        // Tell the loader about the change.
        loader.onContentChanged();
    }
}
