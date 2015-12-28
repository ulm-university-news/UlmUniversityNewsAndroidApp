package ulm.university.news.app.controller;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.ChannelAPI;
import ulm.university.news.app.data.Announcement;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.manager.database.DatabaseLoader;

public class AnnouncementFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Announcement>> {
    /** This classes tag for logging. */
    private static final String TAG = "AnnouncementFragment";

    private AnnouncementListAdapter listAdapter;
    private DatabaseLoader<List<Announcement>> databaseLoader;
    private List<Announcement> announcements;
    private int channelId;

    private ListView lvAnnouncements;
    private TextView tvInfo;

    /** The loader's id. This id is specific to the ChannelFragment's LoaderManager. */
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
        getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        announcements = new ArrayList<>();
        listAdapter = new AnnouncementListAdapter(getActivity(), R.layout.announcement_list_item, announcements);

        channelId = getArguments().getInt("channelId");
        int messageNumber = databaseLoader.getChannelDBM().getMaxMessageNumberAnnouncement(channelId);
        ChannelAPI.getInstance(getActivity()).getAnnouncements(channelId, messageNumber);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_announcement, container, false);
        lvAnnouncements = (ListView) view.findViewById(R.id.fragment_announcement_lv_announcements);
        tvInfo = (TextView) view.findViewById(R.id.fragment_announcement_tv_info);

        lvAnnouncements.setAdapter(listAdapter);

        if (announcements != null && !announcements.isEmpty()) {
            tvInfo.setVisibility(View.GONE);
            lvAnnouncements.setVisibility(View.VISIBLE);
        }
        return view;
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
     * @param announcements The list containing announcement objects.
     */
    public void onEventMainThread(List<Announcement> announcements) {
        Log.d(TAG, "EventBus: List<Announcement>");
        Log.d(TAG, announcements.toString());
        storeAnnouncements(announcements);
    }

    private void storeAnnouncements(List<Announcement> announcements) {
        HashSet<Integer> authorIds = new HashSet<>();

        // Store new announcements.
        for (Announcement announcement : announcements) {
            databaseLoader.getChannelDBM().storeAnnouncement(announcement);
            authorIds.add(announcement.getAuthorModerator());
        }

        // Load and store new moderators.
        for (Integer authorId : authorIds) {
            Log.d(TAG, "authorId:" + authorId);
            // TODO Check moderator existence, load and store if necessary.
        }
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
                // Listen to database changes on channel subscriptions.
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
        // Update view.
        if (announcements.isEmpty()) {
            lvAnnouncements.setVisibility(View.GONE);
            tvInfo.setText(getText(R.string.fragment_announcement_list_empty));
            tvInfo.setVisibility(View.VISIBLE);
        } else {
            lvAnnouncements.setVisibility(View.VISIBLE);
            tvInfo.setVisibility(View.GONE);
            tvInfo.setText(getText(R.string.fragment_announcement_list_loading));
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Announcement>> loader) {
        // Clear adapter data.
        listAdapter.setData(null);
    }
}
