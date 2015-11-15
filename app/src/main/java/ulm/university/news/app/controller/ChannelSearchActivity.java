package ulm.university.news.app.controller;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.BusEvent;
import ulm.university.news.app.api.ChannelAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;

public class ChannelSearchActivity extends AppCompatActivity {
    /** This classes tag for logging. */
    private static final String TAG = "ChannelSearchActivity";

    private AdapterView.OnItemClickListener itemClickListener;
    ChannelListAdapter listAdapter;

    private List<Channel> channels;
    private ChannelDatabaseManager channelDBM;

    // GUI elements.
    private ProgressBar pgrUpdating;
    private TextView tvInfo;
    private TextView tvError;
    private ListView lvChannels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_search);

        channelDBM = new ChannelDatabaseManager(this);
        channels = channelDBM.getChannels();

        listAdapter = new ChannelListAdapter(this, R.layout.channel_list_item, this.channels);

        // Initialise GUI elements.
        initView();

        DateTime latestUpdated = new DateTime(0);
        // Get date from latest updated channel.
        for (Channel channel : channels) {
            if (channel.getModificationDate().isAfter(latestUpdated)) {
                latestUpdated = channel.getModificationDate();
            }
        }

        // Update channels when activity is created. Request new data only.
        ChannelAPI.getInstance(this).getChannels(null, latestUpdated.toString());
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search_menu_action_search);

        SearchManager searchManager = (SearchManager) ChannelSearchActivity.this.getSystemService(
                Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(ChannelSearchActivity
                    .this.getComponentName()));
        }
        return super.onCreateOptionsMenu(menu);
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

    /**
     * Initialises all view elements of this activity.
     */
    private void initView() {
        pgrUpdating = (ProgressBar) findViewById(R.id.activity_channel_search_pgr_updating);
        tvInfo = (TextView) findViewById(R.id.activity_channel_search_tv_info);
        // tvError = (TextView) findViewById(R.id.activity_channel_search_);
        lvChannels = (ListView) findViewById(R.id.activity_channel_search_lv_channels);

        itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Channel channel = (Channel) lvChannels.getItemAtPosition(position);
                Intent intent = new Intent(arg0.getContext(), ChannelDetailActivity.class);
                EventBus.getDefault().postSticky(channel);
                startActivity(intent);
            }
        };
    }

    /**
     * This method will be called when a list of channels is posted to the EventBus.
     *
     * @param channels The list containing channel objects.
     */
    public void onEventMainThread(List<Channel> channels) {
        Log.d(TAG, "EventBus: List<Channel>");
        getChannels(channels);
    }

    /**
     * This method will be called when a BusEvent is posted to the EventBus. The action value determines of which
     * type the included object is.
     *
     * @param busEvent The busEvent which includes an object.
     */
    public void onEventMainThread(BusEvent busEvent) {
        Log.d(TAG, "EventBus: BusEvent");
        String action = busEvent.getAction();
        if (ChannelAPI.GET_CHANNEL.equals(action)) {
            Channel channel = (Channel) busEvent.getObject();
            getChannel(channel);
        } else if (ChannelAPI.UPDATE_CHANNEL.equals(action)) {
            Channel channel = (Channel) busEvent.getObject();
            updateChannel(channel);
        } else if (ChannelAPI.SUBSCRIBE_CHANNEL.equals(action)) {
            Log.d(TAG, action);
        }
    }

    public void getChannels(List<Channel> channels) {
        // Store or update channels in the database and update local channel list.
        Integer localChannelListId = null;
        for (Channel channel : channels) {
            for (int i = 0; i<this.channels.size(); i++) {
                if (this.channels.get(i).getId() == channel.getId()) {
                    localChannelListId = i;
                    break;
                }
            }
            if (localChannelListId == null) {
                channelDBM.storeChannel(channel);
                this.channels.add(channel);
            } else {
                // TODO channelDBM.updateChannel(channel);
                this.channels.remove(localChannelListId.intValue());
                this.channels.add(channel);
            }
        }

        // Show updated channel list.
        listAdapter = new ChannelListAdapter(this, R.layout.channel_list_item, this.channels);
        ListView listView = (ListView) findViewById(R.id.activity_channel_search_lv_channels);
        listView.setAdapter(listAdapter);

        // Update view.
        pgrUpdating.setVisibility(View.GONE);
        // tvInfo.setVisibility(View.GONE);
        tvInfo.setText("Channel data is up to date.");
        lvChannels.setVisibility(View.VISIBLE);
    }

    private void getChannel(Channel channel) {
        Log.d(TAG, "getChannel(): " + channel.toString());
    }

    private void updateChannel(Channel channel) {
        Log.d(TAG, "updateChannel(): " + channel.toString());
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
        // Show appropriate error message.
        switch (serverError.getErrorCode()) {
            case CONNECTION_FAILURE:
                break;
        }
    }
}
