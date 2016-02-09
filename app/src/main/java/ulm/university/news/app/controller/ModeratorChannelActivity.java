package ulm.university.news.app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.BusEvent;
import ulm.university.news.app.api.ChannelAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.Moderator;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.manager.database.ModeratorDatabaseManager;

public class ModeratorChannelActivity extends AppCompatActivity {
    /** This classes tag for logging. */
    private static final String TAG = "ModeratorChannelAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set color theme according to channel and lecture type.
        int channelId = getIntent().getIntExtra("channelId", 0);
        Channel channel = new ChannelDatabaseManager(this).getChannel(channelId);
        ChannelController.setColorTheme(this, channel);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_channel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_moderator_channel_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(channel.getName());
        }

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.activity_moderator_channel_viewpager);
        viewPager.setAdapter(new ModeratorChannelFragmentPager(getSupportFragmentManager(),
                ModeratorChannelActivity.this, channelId));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.activity_moderator_channel_sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        // Refresh responsible moderators of this channel.
        ChannelAPI.getInstance(this).getResponsibleModerators(channelId);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This method will be called when a BusEvent is posted to the EventBus.
     *
     * @param busEvent The BusEvent object.
     */
    public void onEvent(BusEvent busEvent) {
        Log.d(TAG, "EventBus: BusEvent");
        Log.d(TAG, busEvent.toString());

        if (ChannelAPI.GET_RESPONSIBLE_MODERATORS.equals(busEvent.getAction())) {
            processModeratorData((ArrayList<Moderator>) busEvent.getObject());
        }
    }

    /**
     * Saves new moderators and updates existing ones.
     *
     * @param moderators The moderator list to process.
     */
    private void processModeratorData(List<Moderator> moderators) {
        ModeratorDatabaseManager moderatorDBM = new ModeratorDatabaseManager(this);

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
    }
}
