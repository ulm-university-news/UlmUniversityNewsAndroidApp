package ulm.university.news.app.controller;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.BusEventReminders;
import ulm.university.news.app.api.ChannelAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Reminder;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.manager.database.DatabaseLoader;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;

public class ReminderFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Reminder>> {
    /** This classes tag for logging. */
    private static final String TAG = "ReminderFragment";

    private ReminderListAdapter listAdapter;
    private DatabaseLoader<List<Reminder>> databaseLoader;
    private List<Reminder> reminders;
    private SwipeRefreshLayout swipeRefreshLayout;

    private int channelId;
    private Toast toast;
    private String errorMessage;
    private boolean isAutoRefresh = true;

    /** The loader's id. This id is specific to TODO ?. */
    private static final int LOADER_ID = 2;

    public ReminderFragment() {
        // Required empty public constructor
    }

    public static ReminderFragment newInstance(int channelId) {
        ReminderFragment fragment = new ReminderFragment();
        Bundle args = new Bundle();
        args.putInt("channelId", channelId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize or reuse an existing database loader.
        databaseLoader = (DatabaseLoader) getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        databaseLoader.onContentChanged();

        reminders = new ArrayList<>();
        listAdapter = new ReminderListAdapter(getActivity(), R.layout.reminder_list_item, reminders);
        channelId = getArguments().getInt("channelId");

        // Check for new reminder data.
        refreshReminders();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminder, container, false);
        TextView tvListEmpty = (TextView) view.findViewById(R.id.fragment_reminder_tv_list_empty);
        ListView lvReminders = (ListView) view.findViewById(R.id.fragment_reminder_lv_reminders);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_reminder_swipe_refresh_layout);

        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isAutoRefresh = false;
                refreshReminders();
            }
        });

        toast = Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);

        lvReminders.setAdapter(listAdapter);
        lvReminders.setEmptyView(tvListEmpty);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof ModeratorChannelActivity) {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_moderator_channel_reminder_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.activity_moderator_channel_reminder_add:
                Intent intent = new Intent(getActivity(), ReminderAddActivity.class);
                intent.putExtra("channelId", channelId);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshReminders() {
        // Refreshing is only possible if there is an internet connection.
        if (Util.getInstance(getContext()).isOnline()) {
            errorMessage = getString(R.string.general_error_connection_failed);
            errorMessage += getString(R.string.general_error_refresh);
            // Get reminder data.
            ChannelAPI.getInstance(getActivity()).getReminders(channelId);
        } else {
            if (!isAutoRefresh) {
                errorMessage = getString(R.string.general_error_no_connection);
                errorMessage += getString(R.string.general_error_refresh);
                // Only show error message if refreshing was triggered manually.
                toast.setText(errorMessage);
                toast.show();
                // Reset the auto refresh flag.
                isAutoRefresh = true;
                // Can't refresh. Hide loading animation.
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * This method will be called when a list of reminders is posted to the EventBus.
     *
     * @param event The bus event containing a list of reminder objects.
     */
    public void onEventMainThread(BusEventReminders event) {
        Log.d(TAG, event.toString());
        List<Reminder> reminders = event.getReminders();
        boolean newReminders = ChannelController.storeReminders(getActivity(), reminders);
        // Reminders were refreshed. Hide loading animation.
        swipeRefreshLayout.setRefreshing(false);

        if (newReminders) {
            // If reminder data was updated show message no matter if it was a manual or auto refresh.
            String message = getString(R.string.reminder_info_updated);
            toast.setText(message);
            toast.show();
        } else {
            if (!isAutoRefresh) {
                // Only show up to date message if a manual refresh was triggered.
                String message = getString(R.string.reminder_info_up_to_date);
                toast.setText(message);
                toast.show();
            }
        }
    }

    /**
     * This method will be called when a server error is posted to the EventBus.
     *
     * @param serverError The error which occurred on the server.
     */
    public void onEventMainThread(ServerError serverError) {
        Log.d(TAG, "EventBus: ServerError");
        handleServerError(serverError);
    }

    /**
     * Handles the server error and shows appropriate error message.
     *
     * @param serverError The error which occurred on the server.
     */
    public void handleServerError(ServerError serverError) {
        Log.d(TAG, serverError.toString());
        // Can't refresh. Hide loading animation.
        swipeRefreshLayout.setRefreshing(false);

        // Show appropriate error message.
        switch (serverError.getErrorCode()) {
            case CONNECTION_FAILURE:
                if (!isAutoRefresh) {
                    // Only show error message if refreshing was triggered manually.
                    toast.setText(errorMessage);
                    toast.show();
                }
                break;
        }

        // Reset the auto refresh flag.
        isAutoRefresh = true;
    }

    @Override
    public Loader<List<Reminder>> onCreateLoader(int id, Bundle args) {
        databaseLoader = new DatabaseLoader<>(getActivity(), new DatabaseLoader
                .DatabaseLoaderCallbacks<List<Reminder>>() {
            @Override
            public List<Reminder> onLoadInBackground() {
                // Load all reminders of a specific channels.
                return databaseLoader.getChannelDBM().getReminders(channelId);
            }

            @Override
            public IntentFilter observerFilter() {
                // Listen to database changes on reminder events.
                IntentFilter filter = new IntentFilter();
                filter.addAction(ChannelDatabaseManager.STORE_REMINDER);
                filter.addAction(ChannelDatabaseManager.UPDATE_REMINDER);
                return filter;
            }
        });
        // This loader uses the channel database manager to load data.
        databaseLoader.setChannelDBM(new ChannelDatabaseManager(getActivity()));
        return databaseLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<Reminder>> loader, List<Reminder> data) {
        // Update list.
        reminders = data;
        listAdapter.setData(data);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<Reminder>> loader) {
        // Clear adapter data.
        listAdapter.setData(null);
    }
}
