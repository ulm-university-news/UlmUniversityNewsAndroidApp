package ulm.university.news.app.controller;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.ChannelAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.manager.database.DatabaseLoader;
import ulm.university.news.app.util.Util;

public class ModeratorMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<List<Channel>> {
    /** This classes tag for logging. */
    private static final String TAG = "ModeratorMainActivity";

    private AdapterView.OnItemClickListener itemClickListener;
    private List<Channel> channels;
    private ChannelListAdapter listAdapter;

    /** The loader's id. This id is specific to the ChannelSearchActivity's LoaderManager. */
    private static final int LOADER_ID = 1;
    private DatabaseLoader<List<Channel>> databaseLoader;

    // GUI elements.
    private ListView lvChannels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_moderator_main_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_moderator_main_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.activity_moderator_main_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Initialize a Loader with id '1'. If the Loader with this id already
        // exists, then the LoaderManager will reuse the existing Loader.
        databaseLoader = (DatabaseLoader) getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        databaseLoader.onContentChanged();

        initView();
        refreshResponsibleChannels();
    }

    @Override
    public Loader<List<Channel>> onCreateLoader(int id, Bundle args) {
        databaseLoader = new DatabaseLoader<>(this, new DatabaseLoader
                .DatabaseLoaderCallbacks<List<Channel>>() {
            @Override
            public List<Channel> onLoadInBackground() {
                return databaseLoader.getChannelDBM().getResponsibleChannels();
            }

            @Override
            public IntentFilter observerFilter() {
                // Listen to database changes on channel subscriptions.
                IntentFilter filter = new IntentFilter();
                filter.addAction(ChannelDatabaseManager.STORE_CHANNEL);
                filter.addAction(ChannelDatabaseManager.UPDATE_CHANNEL);
                filter.addAction(ChannelDatabaseManager.MODERATE_CHANNEL);
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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_moderator_main_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_moderator_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.activity_moderator_main_menu_add_channel) {
            Intent intent = new Intent(this, ModeratorChannelAddActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.activity_moderator_main_nav_settings) {

        } else if (id == R.id.activity_moderator_main_nav_about) {

        } else if (id == R.id.activity_moderator_main_nav_logout) {
            logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_moderator_main_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        // To logout, just invalidate moderator access token and go to user main screen.
        Util.getInstance(this).setLoggedInModerator(null);
        Util.getInstance(this).setCurrentAccessToken();
        Intent intent = new Intent(this, MainActivity.class);
        // Prevent back navigation to logged in state.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Initialises all view elements of this activity.
     */
    private void initView() {
        lvChannels = (ListView) findViewById(R.id.activity_moderator_main_lv_channels);
        TextView tvListEmpty = (TextView) findViewById(R.id.activity_moderator_main_tv_list_empty);

        itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Channel channel = (Channel) lvChannels.getItemAtPosition(position);
                Intent intent = new Intent(arg0.getContext(), ModeratorChannelActivity.class);
                intent.putExtra("channelId", channel.getId());
                startActivity(intent);
            }
        };

        listAdapter = new ChannelListAdapter(this, R.layout.channel_list_item);
        lvChannels.setAdapter(listAdapter);
        lvChannels.setEmptyView(tvListEmpty);
    }

    private void refreshResponsibleChannels() {
        // Update responsible channels when activity is created. Request new data only.
        ChannelAPI.getInstance(this).getChannels(Util.getInstance(this).getLoggedInModerator().getId(), null);
    }

    /**
     * This method will be called when a list of channels is posted to the EventBus.
     *
     * @param channels The list containing channel objects.
     */
    public void onEventMainThread(List<Channel> channels) {
        Log.d(TAG, "EventBus: List<Channel>");
        processChannelData(channels);
    }

    /**
     * Saves new channels and updates existing ones.
     *
     * @param channels The channel list to process.
     */
    public void processChannelData(List<Channel> channels) {
        // Store or update channels in the database and update local channel list.
        Integer localChannelListId = null;
        List<Channel> channelsDB = databaseLoader.getChannelDBM().getChannels();
        for (Channel channel : channels) {
            for (int i = 0; i < channelsDB.size(); i++) {
                if (channelsDB.get(i).getId() == channel.getId()) {
                    localChannelListId = i;
                    break;
                }
            }
            if (localChannelListId == null) {
                databaseLoader.getChannelDBM().storeChannel(channel);
            } else {
                databaseLoader.getChannelDBM().updateChannel(channel);
                channelsDB.remove(localChannelListId.intValue());
                localChannelListId = null;
            }
            // Mark local moderator as responsible for this channel.
            databaseLoader.getChannelDBM().moderateChannel(channel.getId(), Util.getInstance(this)
                    .getLoggedInModerator().getId());
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
    }
}
