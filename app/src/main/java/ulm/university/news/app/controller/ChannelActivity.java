package ulm.university.news.app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.Lecture;
import ulm.university.news.app.data.enums.ChannelType;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;

public class ChannelActivity extends AppCompatActivity {
    /** This classes tag for logging. */
    private static final String TAG = "ChannelActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set color theme according to channel and lecture type.
        int channelId = getIntent().getIntExtra("channelId", 0);
        Channel channel = new ChannelDatabaseManager(this).getChannel(channelId);
        setColorTheme(channel);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_channel_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(channel.getName());

        // setChannelIcon(channel);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.activity_channel_viewpager);
        viewPager.setAdapter(new ChannelFragmentPager(getSupportFragmentManager(), ChannelActivity.this, channelId));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.activity_channel_sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
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

    private void setColorTheme(Channel channel) {
        if (ChannelType.LECTURE.equals(channel.getType())) {
            Lecture lecture = (Lecture) channel;
            // Set appropriate faculty color.
            switch (lecture.getFaculty()) {
                case ENGINEERING_COMPUTER_SCIENCE_PSYCHOLOGY:
                    setTheme(R.style.Theme_UlmUniversityInformatics);
                    break;
                case MATHEMATICS_ECONOMICS:
                    setTheme(R.style.Theme_UlmUniversityMathematics);
                    break;
                case MEDICINES:
                    setTheme(R.style.Theme_UlmUniversityMedicines);
                    break;
                case NATURAL_SCIENCES:
                    setTheme(R.style.Theme_UlmUniversityScience);
                    break;
            }
        } else {
            // Use main color for other channels.
            setTheme(R.style.Theme_UlmUniversityMain);
        }
    }

    /**
     * Sets the appropriate channel icon.
     */
    private void setChannelIcon(Channel channel) {
        // Set appropriate channel icon.
        switch (channel.getType()) {
            case LECTURE:
                Lecture lecture = (Lecture) channel;
                // Set icon with appropriate faculty color.
                switch (lecture.getFaculty()) {
                    case ENGINEERING_COMPUTER_SCIENCE_PSYCHOLOGY:
                        getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon_channel_lecture_informatics);
                        break;
                    case MATHEMATICS_ECONOMICS:
                        getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon_channel_lecture_math);
                        break;
                    case MEDICINES:
                        getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon_channel_lecture_medicine);
                        break;
                    case NATURAL_SCIENCES:
                        getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon_channel_lecture_science);
                        break;
                }
                break;
            case EVENT:
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon_channel_event);
                break;
            case SPORTS:
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon_channel_sports);
                break;
            case STUDENT_GROUP:
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon_channel_student_group);
                break;
            case OTHER:
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon_channel_other);
                break;
        }
    }
}
