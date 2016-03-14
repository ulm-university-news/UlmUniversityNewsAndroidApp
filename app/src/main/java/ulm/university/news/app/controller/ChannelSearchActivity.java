package ulm.university.news.app.controller;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.BusEventChannels;
import ulm.university.news.app.api.ChannelAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.manager.database.DatabaseLoader;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;

public class ChannelSearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Channel>> {
    /** This classes tag for logging. */
    private static final String TAG = "ChannelSearchActivity";

    /** The loader's id. This id is specific to the ChannelSearchActivity's LoaderManager. */
    private static final int LOADER_ID = 1;

    private DatabaseLoader<List<Channel>> databaseLoader;

    private AdapterView.OnItemClickListener itemClickListener;
    private ChannelListAdapter listAdapter;
    private List<Channel> channels;

    // GUI elements.
    private ListView lvChannels;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;

    private String errorMessage;
    private Toast toast;
    private boolean isAutoRefresh = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_channel_search_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize a Loader with id '1'. If the Loader with this id already
        // exists, then the LoaderManager will reuse the existing Loader.
        databaseLoader = (DatabaseLoader) getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        // Load initial channel data directly, don't use async database loader.
        channels = databaseLoader.getChannelDBM().getChannels();

        // Initialise GUI elements.
        initView();

        // Update channel data.
        refreshChannels();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update channel list to make changes like deleted channels visible.
        if (databaseLoader != null) {
            databaseLoader.onContentChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_channel_search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.activity_channel_search_menu_search);

        SearchManager searchManager = (SearchManager) ChannelSearchActivity.this.getSystemService(
                Context.SEARCH_SERVICE);

        if (searchItem != null) {
            searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(ChannelSearchActivity
                    .this.getComponentName()));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchView.clearFocus();
                    search(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    search(newText);
                    return true;
                }
            });
            searchView.setImeOptions(searchView.getImeOptions() | EditorInfo.IME_ACTION_SEARCH);
            searchView.setQueryHint(getString(R.string.activity_channel_search_hint));
            searchView.setIconified(true);

        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        lvChannels.setOnItemClickListener(itemClickListener);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        lvChannels.setOnItemClickListener(null);
        super.onStop();
    }

    @Override
    public Loader<List<Channel>> onCreateLoader(int id, Bundle args) {
        databaseLoader = new DatabaseLoader<>(this, new DatabaseLoader
                .DatabaseLoaderCallbacks<List<Channel>>() {
            @Override
            public List<Channel> onLoadInBackground() {
                return databaseLoader.getChannelDBM().getChannels();
            }

            @Override
            public IntentFilter observerFilter() {
                // Listen to database changes on channel subscriptions.
                IntentFilter filter = new IntentFilter();
                filter.addAction(ChannelDatabaseManager.STORE_CHANNEL);
                filter.addAction(ChannelDatabaseManager.UPDATE_CHANNEL);
                return filter;
            }
        });
        databaseLoader.setChannelDBM(new ChannelDatabaseManager(this));
        return databaseLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<Channel>> loader, List<Channel> data) {
        Util.getInstance(this).sortChannels(data);
        channels = data;
        listAdapter.setData(data);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<Channel>> loader) {
        listAdapter.setData(null);
    }

    /**
     * Initialises all view elements of this activity.
     */
    private void initView() {
        lvChannels = (ListView) findViewById(R.id.activity_channel_search_lv_channels);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_channel_search_swipe_refresh_layout);
        TextView tvListEmpty = (TextView) findViewById(R.id.activity_channel_search_tv_list_empty);

        toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);

        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isAutoRefresh = false;
                refreshChannels();
            }
        });


        itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Channel channel = (Channel) lvChannels.getItemAtPosition(position);
                Intent intent = new Intent(arg0.getContext(), ChannelDetailActivity.class);
                intent.putExtra("channelId", channel.getId());
                startActivity(intent);
            }
        };

        listAdapter = new ChannelListAdapter(this, R.layout.channel_list_item);
        lvChannels.setAdapter(listAdapter);
        lvChannels.setEmptyView(tvListEmpty);
    }

    /**
     * Searches in the loaded channel list for matching channel names. The channel list will be updated accordingly.
     * Called by the search widget.
     *
     * @param query - The given search string.
     */
    private void search(String query) {
        List<Channel> channelsSelection = new ArrayList<>();
        for (Channel c : channels) {
            if (StringUtils.containsIgnoreCase(c.getName(), query)) {
                channelsSelection.add(c);
            }
        }
        listAdapter.setData(channelsSelection);
        listAdapter.notifyDataSetChanged();
    }

    /**
     * Sends a request to the server to get all new channel data.
     */
    private void refreshChannels() {
        // Channel refresh is only possible if there is an internet connection.
        if (Util.getInstance(this).isOnline()) {
            errorMessage = getString(R.string.general_error_connection_failed);
            errorMessage += getString(R.string.general_error_refresh);
            // Get date from latest updated channel.
            DateTime latestUpdated = new DateTime(0);
            for (Channel channel : channels) {
                if (channel.getModificationDate().isAfter(latestUpdated)) {
                    latestUpdated = channel.getModificationDate();
                }
            }
            // Update channels when activity is created. Request new data only.
            ChannelAPI.getInstance(this).getChannels(null, latestUpdated.toString());
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

    /**
     * This method will be called when a list of channels is posted to the EventBus.
     *
     * @param event The bus event containing a list of channel objects.
     */
    public void onEventMainThread(BusEventChannels event) {
        Log.d(TAG, event.toString());
        List<Channel> channels = event.getChannels();
        processChannelData(channels);
    }

    /**
     * Saves a new channel or updates an existing one.
     *
     * @param channels The channel list to process.
     */
    public void processChannelData(List<Channel> channels) {
        // Store or update channels in the database.
        Integer localChannelListId = null;
        for (Channel channel : channels) {
            for (int i = 0; i < this.channels.size(); i++) {
                if (this.channels.get(i).getId() == channel.getId()) {
                    localChannelListId = i;
                    break;
                }
            }
            if (localChannelListId == null) {
                databaseLoader.getChannelDBM().storeChannel(channel);
                // this.channels.add(channel);
            } else {
                databaseLoader.getChannelDBM().updateChannel(channel);
                // this.channels.remove(localChannelListId.intValue());
                // this.channels.add(channel);
                localChannelListId = null;
            }
        }

        // Channels were refreshed. Hide loading animation.
        swipeRefreshLayout.setRefreshing(false);

        if (!channels.isEmpty()) {
            // If channel data was updated show message no matter if it was a manual or auto refresh.
            String message = getString(R.string.channel_info_updated);
            toast.setText(message);
            toast.show();
        } else {
            if (!isAutoRefresh) {
                // Only show up to date message if a manual refresh was triggered.
                String message = getString(R.string.channel_info_up_to_date);
                toast.setText(message);
                toast.show();
            }
        }
        // Reset the auto refresh flag.
        isAutoRefresh = true;
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
}
