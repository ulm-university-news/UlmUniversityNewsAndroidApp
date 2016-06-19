package ulm.university.news.app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.BusEventGroupMembers;
import ulm.university.news.app.api.GroupAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Group;
import ulm.university.news.app.data.User;
import ulm.university.news.app.manager.database.GroupDatabaseManager;
import ulm.university.news.app.manager.database.UserDatabaseManager;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;
import static ulm.university.news.app.util.Constants.GROUP_NOT_FOUND;

public class GroupActivity extends AppCompatActivity {
    /** This classes tag for logging. */
    private static final String TAG = "GroupActivity";

    private int groupId;
    private Toast toast;

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

        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);

        showGroupDeletedDialog(group);
        refreshGroupMembers();
    }

    private void refreshGroupMembers() {
        // Refreshing is only possible if there is an internet connection.
        if (Util.getInstance(this).isOnline()) {
            Group group = new GroupDatabaseManager(this).getGroup(groupId);
            // Don't refresh if group is already marked as deleted.
            if (!group.getDeleted()) {
                GroupAPI.getInstance(this).getGroupMembers(groupId);
            }
        }
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
        // Handle error.
        // Show appropriate error message.
        switch (serverError.getErrorCode()) {
            case CONNECTION_FAILURE:
                toast.setText( getString(R.string.general_error_connection_failed));
                toast.show();
                break;
            case GROUP_NOT_FOUND:
                new GroupDatabaseManager(this).setGroupToDeleted(groupId);
                toast.setText(getString(R.string.group_deleted));
                toast.show();
                // Close activity and go to the main screen to show deleted dialog on restart activity.
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
        }
    }
}
