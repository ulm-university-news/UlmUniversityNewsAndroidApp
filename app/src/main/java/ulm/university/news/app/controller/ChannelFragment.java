package ulm.university.news.app.controller;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.manager.database.DatabaseLoader;
import ulm.university.news.app.util.Util;


public class ChannelFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Channel>> {
    /** This classes tag for logging. */
    private static final String TAG = "ChannelFragment";

    /** The loader's id. This id is specific to the ChannelFragment's LoaderManager. */
    private static final int LOADER_ID = 2;

    private AdapterView.OnItemClickListener itemClickListener;
    private DatabaseLoader<List<Channel>> databaseLoader;

    private ChannelListAdapter listAdapter;
    private List<Channel> channels;
    private ListView lvChannels;

    public static ChannelFragment newInstance() {
        return new ChannelFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update channel list to make changes like read messages visible.
        if (databaseLoader != null) {
            databaseLoader.onContentChanged();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize or reuse an existing database loader.
        databaseLoader = (DatabaseLoader) getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        databaseLoader.onContentChanged();

        listAdapter = new ChannelListAdapter(getActivity(), R.layout.channel_list_item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_channel, container, false);
        lvChannels = (ListView) view.findViewById(R.id.fragment_channel_lv_channels);
        TextView tvListEmpty = (TextView) view.findViewById(R.id.fragment_channel_tv_list_empty);

        itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Channel channel = (Channel) lvChannels.getItemAtPosition(position);
                Intent intent = new Intent(arg0.getContext(), ChannelActivity.class);
                intent.putExtra("channelId", channel.getId());
                startActivity(intent);
            }
        };

        lvChannels.setAdapter(listAdapter);
        lvChannels.setOnItemClickListener(itemClickListener);
        lvChannels.setEmptyView(tvListEmpty);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_main_channel_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.activity_main_channel_menu_search:
                startActivity(new Intent(getActivity(), ChannelSearchActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<List<Channel>> onCreateLoader(int id, Bundle args) {
        databaseLoader = new DatabaseLoader<>(getActivity(), new DatabaseLoader
                .DatabaseLoaderCallbacks<List<Channel>>() {
            @Override
            public List<Channel> onLoadInBackground() {
                // Load all subscribed channels with announcement data.
                return databaseLoader.getChannelDBM().getSubscribedChannels();
            }

            @Override
            public IntentFilter observerFilter() {
                // Listen to database changes on channel subscriptions and new announcements.
                IntentFilter filter = new IntentFilter();
                filter.addAction(ChannelDatabaseManager.SUBSCRIBE_CHANNEL);
                filter.addAction(ChannelDatabaseManager.UNSUBSCRIBE_CHANNEL);
                filter.addAction(ChannelDatabaseManager.STORE_ANNOUNCEMENT);
                filter.addAction(ChannelDatabaseManager.MARK_CHANNEL_DELETED);
                return filter;
            }
        });
        // This loader uses the channel database manager to load data.
        databaseLoader.setChannelDBM(new ChannelDatabaseManager(getActivity()));
        return databaseLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<Channel>> loader, List<Channel> data) {
        // Update list.
        Util.getInstance(getContext()).sortChannels(data);
        channels = data;
        listAdapter.setData(data);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<Channel>> loader) {
        // Clear adapter data.
        listAdapter.setData(null);
    }
}
