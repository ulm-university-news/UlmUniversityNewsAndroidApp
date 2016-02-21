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
import ulm.university.news.app.api.BusEventAnnouncements;
import ulm.university.news.app.api.BusEventModerators;
import ulm.university.news.app.api.ChannelAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Announcement;
import ulm.university.news.app.data.Moderator;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.manager.database.DatabaseLoader;
import ulm.university.news.app.manager.database.ModeratorDatabaseManager;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;

public class AnnouncementFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Announcement>> {
    /** This classes tag for logging. */
    private static final String TAG = "AnnouncementFragment";

    private AnnouncementListAdapter listAdapter;
    private DatabaseLoader<List<Announcement>> databaseLoader;
    private List<Announcement> announcements;
    private SwipeRefreshLayout swipeRefreshLayout;

    private int channelId;
    private Toast toast;
    private String errorMessage;
    private boolean isAutoRefresh = true;

    /** The loader's id. This id is specific to the AnnouncementFragment's LoaderManager. */
    private static final int LOADER_ID = 3;

    public AnnouncementFragment() {
        // Required empty public constructor
    }

    public static AnnouncementFragment newInstance(int channelId) {
        AnnouncementFragment fragment = new AnnouncementFragment();
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

        announcements = new ArrayList<>();
        listAdapter = new AnnouncementListAdapter(getActivity(), R.layout.announcement_list_item, announcements);
        channelId = getArguments().getInt("channelId");

        // Check for new announcement data.
        refreshAnnouncements();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_announcement, container, false);
        TextView tvListEmpty = (TextView) view.findViewById(R.id.fragment_announcement_tv_list_empty);
        ListView lvAnnouncements = (ListView) view.findViewById(R.id.fragment_announcement_lv_announcements);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_announcement_swipe_refresh_layout);

        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isAutoRefresh = false;
                refreshAnnouncements();
            }
        });

        toast = Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);

        lvAnnouncements.setAdapter(listAdapter);
        lvAnnouncements.setEmptyView(tvListEmpty);
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
        inflater.inflate(R.menu.activity_moderator_channel_announcement_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.activity_moderator_channel_announcement_menu_add:
                Intent intent = new Intent(getActivity(), AnnouncementAddActivity.class);
                intent.putExtra("channelId", channelId);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshAnnouncements() {
        // Refreshing is only possible if there is an internet connection.
        if (Util.getInstance(getContext()).isOnline()) {
            errorMessage = getString(R.string.general_error_connection_failed);
            errorMessage += getString(R.string.general_error_refresh);
            // Get announcement data. Request new messages only.
            int messageNumber = databaseLoader.getChannelDBM().getMaxMessageNumberAnnouncement(channelId);
            ChannelAPI.getInstance(getActivity()).getAnnouncements(channelId, messageNumber);
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
     * This method will be called when a list of announcements is posted to the EventBus.
     *
     * @param event The bus event containing a list of announcement objects.
     */
    public void onEventMainThread(BusEventAnnouncements event) {
        Log.d(TAG, event.toString());
        List<Announcement> announcements = event.getAnnouncements();
        ChannelController.storeAnnouncements(getActivity(), announcements);
        // Announcements were refreshed. Hide loading animation.
        swipeRefreshLayout.setRefreshing(false);

        if (!announcements.isEmpty()) {
            // If announcement data was updated show message no matter if it was a manual or auto refresh.
            String message = getString(R.string.announcement_info_updated);
            toast.setText(message);
            toast.show();
        } else {
            if (!isAutoRefresh) {
                // Only show up to date message if a manual refresh was triggered.
                String message = getString(R.string.announcement_info_up_to_date);
                toast.setText(message);
                toast.show();
            }
        }
    }

    /**
     * This method will be called when a list of moderators is posted to the EventBus.
     *
     * @param event The bus event containing a list of moderator objects.
     */
    public void onEventMainThread(BusEventModerators event) {
        Log.d(TAG, event.toString());
        List<Moderator> moderators = event.getModerators();
        processModeratorData(moderators);
    }

    /**
     * Saves new moderators and updates existing ones.
     *
     * @param moderators The moderator list to process.
     */
    private void processModeratorData(List<Moderator> moderators) {
        ModeratorDatabaseManager moderatorDBM = new ModeratorDatabaseManager(getContext());

        List<Moderator> moderatorsDB = moderatorDBM.getModerators();
        // Store or update channels in the database and update local channel list.
        Integer moderatorDBId = null;
        for (Moderator moderator : moderators) {
            // Store new moderators and update existing ones.
            for (int i = 0; i < moderatorsDB.size(); i++) {
                if (moderatorsDB.get(i).getId() == moderator.getId()) {
                    moderatorDBId = i;
                    break;
                }
            }
            if (moderatorDBId == null) {
                moderatorDBM.storeModerator(moderator);
            } else {
                moderatorDBM.updateModerator(moderator);
                moderatorDBId = null;
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
    public Loader<List<Announcement>> onCreateLoader(int id, Bundle args) {
        databaseLoader = new DatabaseLoader<>(getActivity(), new DatabaseLoader
                .DatabaseLoaderCallbacks<List<Announcement>>() {
            @Override
            public List<Announcement> onLoadInBackground() {
                // Load all announcements of a specific channels.
                return databaseLoader.getChannelDBM().getAnnouncements(channelId);
            }

            @Override
            public IntentFilter observerFilter() {
                // Listen to database changes on announcement events.
                IntentFilter filter = new IntentFilter();
                filter.addAction(ChannelDatabaseManager.STORE_ANNOUNCEMENT);
                return filter;
            }
        });
        // This loader uses the channel database manager to load data.
        databaseLoader.setChannelDBM(new ChannelDatabaseManager(getActivity()));
        return databaseLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<Announcement>> loader, List<Announcement> data) {
        // Update list.
        announcements = data;
        listAdapter.setData(data);
        listAdapter.notifyDataSetChanged();

        // Mark loaded and unread announcements as read after displaying.
        for (Announcement announcement : announcements) {
            if (!announcement.isRead()) {
                databaseLoader.getChannelDBM().setMessageToRead(announcement.getId());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Announcement>> loader) {
        // Clear adapter data.
        listAdapter.setData(null);
    }
}
