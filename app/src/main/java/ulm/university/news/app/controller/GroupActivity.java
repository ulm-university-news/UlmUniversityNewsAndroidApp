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

import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.BusEventGroupMembers;
import ulm.university.news.app.api.GroupAPI;
import ulm.university.news.app.data.Group;
import ulm.university.news.app.data.User;
import ulm.university.news.app.manager.database.GroupDatabaseManager;
import ulm.university.news.app.manager.database.UserDatabaseManager;

public class GroupActivity extends AppCompatActivity {
    /** This classes tag for logging. */
    private static final String TAG = "GroupActivity";

    private int groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        groupId = getIntent().getIntExtra("groupId", 0);
        Group group = new GroupDatabaseManager(this).getGroup(groupId);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_group_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(group.getName());
        }

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.activity_group_viewpager);
        viewPager.setAdapter(new GroupFragmentPager(getSupportFragmentManager(), GroupActivity.this, groupId));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.activity_group_sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        showGroupDeletedDialog(group);
        refreshGroupMembers();
    }

    private void refreshGroupMembers() {
        GroupAPI.getInstance(this).getGroupMembers(groupId);
    }

    private void showGroupDeletedDialog(Group group) {
        if (group.getDeleted() && !group.getDeletedRead()) {
            // Show group deleted dialog if it wasn't shown before.
            InfoDialogFragment dialog = new InfoDialogFragment();
            Bundle args = new Bundle();
            args.putString(InfoDialogFragment.DIALOG_TITLE, getString(R.string.group_deleted_member_dialog_title));
            String text = String.format(getString(R.string.group_deleted_member_dialog_text), group.getName());
            args.putString(InfoDialogFragment.DIALOG_TEXT, text);
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "groupDeleted");
            // Set deleted read flag to true.
            new GroupDatabaseManager(this).setGroupDeletedToRead(group.getId());
        }
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
     * This method will be called when a list of users is posted to the EventBus.
     *
     * @param event The bus event containing a list of user objects.
     */
    public void onEvent(BusEventGroupMembers event) {
        Log.d(TAG, event.toString());
        List<User> users = event.getUsers();

        // Store users in database an add them as group members to the group.
        UserDatabaseManager userDBM = new UserDatabaseManager(this);
        GroupDatabaseManager groupDBM = new GroupDatabaseManager(this);
        for (User u : users) {
            userDBM.storeUser(u);
            groupDBM.addUserToGroup(groupId, u.getId());
        }
    }
}
