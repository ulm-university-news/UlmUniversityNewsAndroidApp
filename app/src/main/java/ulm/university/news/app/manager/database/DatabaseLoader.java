package ulm.university.news.app.manager.database;

import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;

/**
 * The DatabaseLoader is used to load data from database in an asynchronous background task. Furthermore, the loader
 * monitors the database for changes to perform immediate updates. The loader accepts a generic list type to load all
 * kinds of different data.
 *
 * @author Matthias Mak
 */
public class DatabaseLoader<T extends List<?>> extends AsyncTaskLoader<T> {
    /** This classes tag for logging. */
    private static final String TAG = "DatabaseLoader";

    /** A reference to the loader’s current data. */
    private T data;

    /** The interface which describes the data loading and observer filtering. */
    private DatabaseLoaderCallbacks<T> databaseLoaderCallbacks;

    /** The observer is able to detect content changes in the database and report them to the loader. */
    private DatabaseObserver databaseObserver;

    // References to the database managers.
    private ChannelDatabaseManager channelDBM;
    private UserDatabaseManager userDBM;

    /**
     * Instantiates a new DatabaseLoader.
     *
     * @param ctx The context from which the constructor is called.
     * @param callbacks The interface which describes the data loading and observer filtering.
     */
    public DatabaseLoader(Context ctx, DatabaseLoaderCallbacks<T> callbacks) {
        super(ctx);
        databaseLoaderCallbacks = callbacks;
    }

    @Override
    public T loadInBackground() {
        // This method is called on a background thread and uses the DatabaseLoaderCallbacks interface method
        // onLoadInBackground to generates a new set of data to be delivered back to the caller.
        return databaseLoaderCallbacks.onLoadInBackground();
    }

    @Override
    public void deliverResult(T data) {
        if (isReset()) {
            // The loader has been reset; ignore the result and invalidate the data.
            releaseResources(data);
            return;
        }

        // Hold a reference to the old data so it doesn't get garbage collected.
        // Protect it until the new data has been delivered.
        T oldData = this.data;
        this.data = data;

        if (isStarted()) {
            // If the Loader is in a started state, deliver the results to the client.
            super.deliverResult(data);
        }

        // Invalidate the old data as it isn't needed any more.
        if (oldData != null && oldData != data) {
            releaseResources(oldData);
        }
    }

    @Override
    protected void onStartLoading() {
        Log.d(TAG, "onStartLoading");
        if (data != null) {
            // Deliver any previously loaded data immediately.
            deliverResult(data);
        }

        // Register the observer that will notify the loader when changes are made.
        if (databaseObserver == null) {
            databaseObserver = new DatabaseObserver(this, databaseLoaderCallbacks.observerFilter());
        }

        if (takeContentChanged() || data == null) {
            // When the observer detects a change, it should call onContentChanged()
            // on the Loader, which will cause the next call to takeContentChanged()
            // to return true. If this is ever the case (or if the current data is
            // null), we force a new load.
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // The Loader is in a stopped state, so attempt to cancel the current load (if there is one).
        cancelLoad();
        // Note that the observer isn't changed here. Loaders in a stopped state should still monitor the data source
        // for changes so that the Loader will know to force a new load if it is ever started again.
    }

    @Override
    protected void onReset() {
        // Ensure the loader has been stopped.
        onStopLoading();

        // Release resources associated with 'data'.
        if (data != null) {
            releaseResources(data);
            data = null;
        }

        // The Loader is being reset, so stop monitoring for changes.
        if (databaseObserver != null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(databaseObserver);
            databaseObserver = null;
        }
    }

    @Override
    public void onCanceled(T data) {
        // Attempt to cancel the current asynchronous load.
        super.onCanceled(data);

        // The load has been canceled, so release the resources associated with 'data'.
        releaseResources(data);
    }

    /**
     * Releases all resources associated with the loader.
     *
     * @param data The data to release.
     */
    private void releaseResources(T data) {
        // For a simple list, there is nothing to do. For something like a cursor, one would close it in this method.
        // All resources associated with the loader should be released here.
    }

    /**
     * This interface describes the data loading and observer filtering of the DatabaseLoader and DatabaseObserver.
     *
     * @param <T> The type of data which should be loaded.
     */
    public interface DatabaseLoaderCallbacks<T> {
        /**
         * Performs the loading of database data in a background thread.
         *
         * @return The data loaded from database.
         */
        T onLoadInBackground();

        /**
         * Creates filters to which the database observer should listen.
         *
         * @return The filter which describes to what actions the observer should listen.
         */
        IntentFilter observerFilter();
    }

    public ChannelDatabaseManager getChannelDBM() {
        return channelDBM;
    }

    public void setChannelDBM(ChannelDatabaseManager channelDBM) {
        this.channelDBM = channelDBM;
    }

    public UserDatabaseManager getUserDBM() {
        return userDBM;
    }

    public void setUserDBM(UserDatabaseManager userDBM) {
        this.userDBM = userDBM;
    }
}
