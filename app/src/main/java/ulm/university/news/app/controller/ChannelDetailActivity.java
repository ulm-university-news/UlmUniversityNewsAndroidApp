package ulm.university.news.app.controller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;

public class ChannelDetailActivity extends AppCompatActivity {
    /** This classes tag for logging. */
    private static final String TAG = "ChannelDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_detail);

        int channelId = getIntent().getIntExtra("channelId", 0);
        Channel channel = new ChannelDatabaseManager(this).getChannel(channelId);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_channel_detail_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(channel.getName());

        // Add the fragment to the 'fragment_container' FrameLayout
        ChannelDetailFragment fragment = ChannelDetailFragment.newInstance(channelId);
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment).commit();
    }
}
