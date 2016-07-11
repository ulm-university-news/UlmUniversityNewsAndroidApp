package ulm.university.news.app.controller;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.BusEvent;
import ulm.university.news.app.api.GroupAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Group;
import ulm.university.news.app.data.User;
import ulm.university.news.app.manager.database.DatabaseLoader;
import ulm.university.news.app.manager.database.GroupDatabaseManager;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;
import static ulm.university.news.app.util.Constants.GROUP_NOT_FOUND;
import static ulm.university.news.app.util.Constants.GROUP_PARTICIPANT_NOT_FOUND;

public class GroupMembersActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<User>>,
        DialogListener {
    /** This classes tag for logging. */
    private static final String TAG = "GroupMembersActivity";

    /** The loader's id. */
    private static final int LOADER_ID = 25;
    private DatabaseLoader<List<User>> databaseLoader;
    private UserListAdapter listAdapter;
    private int groupId;

    private ListView lvGroupMembers;
    private TextView tvListEmpty;
    private ProgressBar pgrSending;
    private Toast toast;
    private String message;
    private MenuItem menuItemSending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_group_members_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupId = getIntent().getIntExtra("groupId", 0);
        // Initialize or reuse an existing database loader.
        databaseLoader = (DatabaseLoader) getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        databaseLoader.onContentChanged();

        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update option list to make changes visible.
        if (databaseLoader != null) {
            databaseLoader.onContentChanged();
        }
    }

    private void initView() {
        lvGroupMembers = (ListView) findViewById(R.id.activity_group_members_lv_members);
        tvListEmpty = (TextView) findViewById(R.id.activity_group_members_tv_list_empty);
        pgrSending = (ProgressBar) findViewById(R.id.activity_group_members_pgr_sending);

        Group group = databaseLoader.getGroupDBM().getGroup(groupId);
        listAdapter = new UserListAdapter(this, R.layout.option_list_item, group.getGroupAdmin());
        lvGroupMembers.setAdapter(listAdapter);
        lvGroupMembers.setEmptyView(tvListEmpty);

        message = getString(R.string.general_error_no_connection);
        message += " " + getString(R.string.general_error_delete);
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_group_members_menu, menu);
        menuItemSending = menu.findItem(R.id.activity_group_members_menu_sending);
        menuItemSending.setVisible(false);
        menuItemSending.setActionView(pgrSending);
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
     * This method will be called when a BusEvent is posted to the EventBus.
     *
     * @param event The bus event containing an action an maybe additional objects.
     */
    public void onEventMainThread(BusEvent event) {
        Log.d(TAG, "BusEvent: " + event.getAction());
        // Remove user from group in local database.
        if (GroupAPI.REMOVE_USER_FROM_GROUP.equals(event.getAction())) {
            int userId = (int) event.getObject();
            databaseLoader.getGroupDBM().removeUserFromGroup(groupId, userId);
        }
        // Hide loading animation.
        pgrSending.setVisibility(View.GONE);
        menuItemSending.setVisible(false);
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
        // Hide loading animation.
        pgrSending.setVisibility(View.VISIBLE);
        menuItemSending.setVisible(true);
        // Show appropriate error message.
        switch (serverError.getErrorCode()) {
            case CONNECTION_FAILURE:
                toast.setText(message);
                toast.show();
                break;
            case GROUP_PARTICIPANT_NOT_FOUND:
                // User already removed. Remove user from group in local database.
                databaseLoader.getGroupDBM().removeUserFromGroup(groupId, listAdapter.getCurrentUserId());
                break;
            case GROUP_NOT_FOUND:
                databaseLoader.getGroupDBM().setGroupToDeleted(groupId);
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

    @Override
    public Loader<List<User>> onCreateLoader(int id, Bundle args) {
        databaseLoader = new DatabaseLoader<>(this, new DatabaseLoader
                .DatabaseLoaderCallbacks<List<User>>() {
            @Override
            public List<User> onLoadInBackground() {
                // Load all group members.
                return databaseLoader.getGroupDBM().getGroupMembers(groupId);
            }

            @Override
            public IntentFilter observerFilter() {
                // Listen to database changes on new and removed group members.
                IntentFilter filter = new IntentFilter();
                filter.addAction(GroupDatabaseManager.ADD_USER_TO_GROUP);
                filter.addAction(GroupDatabaseManager.REMOVE_USER_FROM_GROUP);
                return filter;
            }
        });
        // This loader uses the group database manager to load data.
        databaseLoader.setGroupDBM(new GroupDatabaseManager(this));
        return databaseLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<User>> loader, List<User> data) {
        // Update list.
        listAdapter.setData(data);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<User>> loader) {
        // Clear adapter data.
        listAdapter.setData(null);
    }

    @Override
    public void onDialogPositiveClick(String tag) {
        if (YesNoDialogFragment.DIALOG_MEMBER_REMOVE.equals(tag)) {
            if (Util.getInstance(this).isOnline()) {
                pgrSending.setVisibility(View.VISIBLE);
                menuItemSending.setVisible(true);
                GroupAPI.getInstance(this).removeUserFromGroup(groupId, listAdapter.getCurrentUserId());

                message = getString(R.string.general_error_connection_failed);
                message += " " + getString(R.string.general_error_delete);
            } else {
                message = getString(R.string.general_error_no_connection);
                message += " " + getString(R.string.general_error_delete);
                toast.setText(message);
                toast.show();
            }
        }
    }
}
