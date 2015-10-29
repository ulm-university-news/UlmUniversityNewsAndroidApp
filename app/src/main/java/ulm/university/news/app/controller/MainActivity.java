package ulm.university.news.app.controller;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import ulm.university.news.app.R;
import ulm.university.news.app.api.ChannelAPI;

public class MainActivity extends AppCompatActivity {

    /** This classes tag for logging. */
    private static final String LOG_TAG = "MainActivity";

    /** An instance of the ChannelAPI class. */
    private ChannelAPI channelAPI = new ChannelAPI(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new SampleFragmentPagerAdapter(getSupportFragmentManager(),
                MainActivity.this));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        String accessToken = "510e4f3dafa2568c59d94787030292f81a37e5a4baf6a727cd5274db79d0b17d";
        // channelAPI.getChannel(accessToken, 1);
        channelAPI.getChannels(accessToken, null, null);
    }

}
