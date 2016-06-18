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
import ulm.university.news.app.data.Ballot;
import ulm.university.news.app.manager.database.GroupDatabaseManager;

public class BallotActivity extends AppCompatActivity {
    /** This classes tag for logging. */
    private static final String TAG = "BallotActivity";

    private int groupId;
    private int ballotId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set color scheme to ballot green.
        setTheme(R.style.UlmUniversity_Ballot);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ballot);

        groupId = getIntent().getIntExtra("groupId", 0);
        ballotId = getIntent().getIntExtra("ballotId", 0);
        Ballot ballot = new GroupDatabaseManager(this).getBallot(ballotId);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_ballot_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(ballot.getTitle());
        }

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.activity_ballot_viewpager);
        viewPager.setAdapter(new BallotFragmentPager(getSupportFragmentManager(), BallotActivity.this, groupId,
                ballotId));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.activity_ballot_sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
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

    @Override
    public void onResume() {
        super.onResume();
        // Update activity title in case the ballot was edited.
        Ballot ballot = new GroupDatabaseManager(this).getBallot(ballotId);
        getSupportActionBar().setTitle(ballot.getTitle());
    }
}
