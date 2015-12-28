package ulm.university.news.app.controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashSet;
import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.ChannelAPI;
import ulm.university.news.app.data.Announcement;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;

public class AnnouncementFragment extends Fragment {
    /** This classes tag for logging. */
    private static final String TAG = "AnnouncementFragment";

    private ChannelDatabaseManager channelDBM;

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
        channelDBM = new ChannelDatabaseManager(getActivity());
        int channelId = getArguments().getInt("channelId");
        int messageNumber = channelDBM.getMaxMessageNumberAnnouncement(channelId);
        ChannelAPI.getInstance(getActivity()).getAnnouncements(channelId, messageNumber);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_announcement, container, false);
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
            channelDBM.storeAnnouncement(announcement);
            authorIds.add(announcement.getAuthorModerator());
        }

        // Load and store new moderators.
        for (Integer authorId : authorIds) {
            Log.d(TAG, "authorId:" + authorId);
            // TODO Check moderator existence, load and store if necessary.
        }
    }
}
