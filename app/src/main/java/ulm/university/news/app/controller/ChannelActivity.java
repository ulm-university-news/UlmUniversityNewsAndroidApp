package ulm.university.news.app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.BusEventModerators;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.Moderator;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.manager.database.ModeratorDatabaseManager;

public class ChannelActivity extends AppCompatActivity {
    /** This classes tag for logging. */
    private static final String TAG = "ChannelActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set color theme according to channel and lecture type.
        int channelId = getIntent().getIntExtra("channelId", 0);
        Channel channel = new ChannelDatabaseManager(this).getChannel(channelId);
        ChannelController.setColorTheme(this, channel);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_channel_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(channel.getName());
        }

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.activity_channel_viewpager);
        viewPager.setAdapter(new ChannelFragmentPager(getSupportFragmentManager(), ChannelActivity.this, channelId));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.activity_channel_sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        showChannelDeletedDialog(channel);
    }

    private void showChannelDeletedDialog(Channel channel) {
        if (channel.isDeleted() && !channel.isDeletedRead()) {
            // Show channel deleted dialog if it wasn't shown before.
            InfoDialogFragment dialog = new InfoDialogFragment();
            Bundle args = new Bundle();
            args.putString(InfoDialogFragment.DIALOG_TITLE, getString(R.string.channel_deleted_subscribed_dialog_title));
            String text = String.format(getString(R.string.channel_deleted_subscribed_dialog_text), channel.getName());
            args.putString(InfoDialogFragment.DIALOG_TEXT, text);
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "channelDeleted");
            // Set deleted read flag to true.
            new ChannelDatabaseManager(this).setChannelDeletedToRead(channel.getId());
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_channel_menu, menu);
        return super.onCreateOptionsMenu(menu);
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
            case R.id.activity_channel_menu_settings:
                intent = new Intent(this, SettingsChannelActivity.class);
                intent.putExtra("channelId", getIntent().getIntExtra("channelId", 0));
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
     * This method will be called when a list of moderators is posted to the EventBus.
     *
     * @param event The bus event containing a list of moderator objects.
     */
    public void onEvent(BusEventModerators event) {
        Log.d(TAG, event.toString());
        List<Moderator> moderators = event.getModerators();

        ModeratorDatabaseManager moderatorDBM = new ModeratorDatabaseManager(this);
        for (Moderator m : moderators) {
            moderatorDBM.storeModerator(m);
        }
    }
}
