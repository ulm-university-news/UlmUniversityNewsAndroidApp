package ulm.university.news.app.controller;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

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
    /** The broadcast receiver for this activity. */
    private BroadcastReceiver receiver;
    /** This filter accepts broadcasts about channels. */
    IntentFilter channelFilter;
    /** The Gson object used to parse from an to JSON. */
    private Gson gson;

    // GUI elements.
    private ProgressBar pgrUpdating;
    private TextView tvInfo;
    private TextView tvError;
    private ListView lvChannels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_search);
        // Initialise GUI elements.
        initGUI();

        ChannelAPI.getInstance(this).getChannels(null, null);
        ChannelAPI.getInstance(this).subscribeChannel(1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * Initialises all view elements of this activity.
     */
    private void initGUI() {
        pgrUpdating = (ProgressBar) findViewById(R.id.activity_channel_search_pgr_updating);
        tvInfo = (TextView) findViewById(R.id.activity_channel_search_tv_info);
        // tvError = (TextView) findViewById(R.id.activity_channel_search_);
        lvChannels = (ListView) findViewById(R.id.activity_channel_search_lv_channels);
    }

    /**
     * This method will be called when a server error is posted to the EventBus.
     *
     * @param serverError The error which occurred on the server.
     */
    public void onEventMainThread(ServerError serverError) {
        Log.d(TAG, "###############################################");
        handleServerError(serverError);
    }

    /**
     * This method will be called when a list of channels is posted to the EventBus.
     *
     * @param channels The list containing channel objects.
     */
    public void onEventMainThread(List<Channel> channels) {
        Log.d(TAG, "***********************************************");
        getChannels(channels);
    }

    /**
     * This method will be called when a BusEvent is posted to the EventBus. The action value determines of which
     * type the included object is.
     *
     * @param busEvent The busEvent which includes an object.
     */
    public void onEventMainThread(BusEvent busEvent) {
        Log.d(TAG, "+++++++++++++++++++++++++++++++++++++++++++++++");
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
        ChannelListAdapter adapter = new ChannelListAdapter(this, R.layout.channel_list_item, channels);
        ListView listView = (ListView) findViewById(R.id.activity_channel_search_lv_channels);
        listView.setAdapter(adapter);

        // Store channels in the database.
        ChannelDatabaseManager channelDBM = new ChannelDatabaseManager(this);
        for (Channel channel : channels) {
            channelDBM.storeChannel(channel);
        }

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
     * Handles the server error and shows appropriate error message.
     *
     * @param serverError The error which occurred on the server.
     */
    public void handleServerError(ServerError serverError) {
        // Update view.
        // Show appropriate error message.
        switch (serverError.getErrorCode()) {
            case CONNECTION_FAILURE:
                break;
        }
    }
}
