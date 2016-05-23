package ulm.university.news.app.controller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Group;
import ulm.university.news.app.manager.database.GroupDatabaseManager;

public class GroupDetailActivity extends AppCompatActivity {
    /** This classes tag for logging. */
    private static final String TAG = "GroupDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int groupId = getIntent().getIntExtra("groupId", 0);
        Group group = new GroupDatabaseManager(this).getGroup(groupId);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_group_detail_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(group.getName());

        // Add the fragment to the 'fragment_container' FrameLayout
        GroupDetailFragment fragment = GroupDetailFragment.newInstance(groupId);
        getSupportFragmentManager().beginTransaction().add(R.id.activity_group_detail_fragment_container, fragment)
                .commit();
    }
}
