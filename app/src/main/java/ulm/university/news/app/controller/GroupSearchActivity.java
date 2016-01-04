package ulm.university.news.app.controller;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.GroupAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Group;
import ulm.university.news.app.data.enums.GroupType;
import ulm.university.news.app.util.Constants;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;

public class GroupSearchActivity extends AppCompatActivity {
    /** This classes tag for logging. */
    private static final String TAG = "GroupSearchActivity";

    private TextView tvError;
    private TextView tvInfo;
    private RadioGroup rgSearch;
    private RadioButton rbId;
    private RadioButton rbName;
    private CheckBox chkTutorial;
    private CheckBox chkWork;
    private SearchView searchView;
    private ListView lvGroups;
    private ProgressBar pgrSearching;

    private boolean isInputValid;
    private int groupId;
    private String groupName;
    private List<Group> groups;
    private GroupListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_group_search_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        groups = new ArrayList<>();
        initView();

        // TODO How to restore state for already loaded channels? (e.g. after rotate or GroupDetailActivity)
        // TODO Use EventBus?

        listAdapter = new GroupListAdapter(this, R.layout.group_list_item);
        lvGroups.setAdapter(listAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        // lvChannels.setOnItemClickListener(itemClickListener);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        // lvChannels.setOnItemClickListener(null);
        super.onStop();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_group_search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.activity_group_search_menu_search);
        SearchManager searchManager = (SearchManager) GroupSearchActivity.this.getSystemService(
                Context.SEARCH_SERVICE);

        if (searchItem != null) {
            searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(GroupSearchActivity
                    .this.getComponentName()));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchGroup(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    tvError.setVisibility(View.GONE);
                    tvInfo.setVisibility(View.GONE);
                    lvGroups.setVisibility(View.GONE);
                    return true;
                }
            });
            searchView.setImeOptions(searchView.getImeOptions() | EditorInfo.IME_ACTION_SEARCH);
            searchView.setQueryHint(getString(R.string.activity_group_search_et_hint_id));
            searchView.setIconified(false);
        }
        return super.onCreateOptionsMenu(menu);
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

    private void initView() {
        tvError = (TextView) findViewById(R.id.activity_group_search_tv_error);
        tvInfo = (TextView) findViewById(R.id.activity_group_search_tv_info);
        rbId = (RadioButton) findViewById(R.id.activity_group_search_rb_id);
        rbName = (RadioButton) findViewById(R.id.activity_group_search_rb_name);
        rgSearch = (RadioGroup) findViewById(R.id.activity_group_search_rg_search);
        chkTutorial = (CheckBox) findViewById(R.id.activity_group_search_chk_tutorial);
        chkWork = (CheckBox) findViewById(R.id.activity_group_search_chk_work);
        lvGroups = (ListView) findViewById(R.id.activity_group_search_lv_groups);
        pgrSearching = (ProgressBar) findViewById(R.id.activity_group_search_pgr_searching);

        rgSearch.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                onRadioButtonClicked(checkedId);
            }
        });

        chkTutorial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    chkWork.setClickable(false);
                } else {
                    chkWork.setClickable(true);
                }
            }
        });

        chkWork.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    chkTutorial.setClickable(false);
                } else {
                    chkTutorial.setClickable(true);
                }
            }
        });
    }

    private void validateTextInput(String input) {
        groupName = input;
        if (input.length() == 0) {
            isInputValid = false;
            tvError.setText(getString(R.string.activity_group_search_error_search_empty));
        } else if (rbId.isChecked()) {
            // Validate group id.
            try {
                groupId = Integer.valueOf(input);
                isInputValid = true;
            } catch (NumberFormatException e) {
                // Invalid group id.
                tvError.setText(getString(R.string.activity_group_search_error_search_numbers));
                isInputValid = false;
            }
        } else {
            // Validate group name.
            if (input.matches(Constants.NAME_PATTERN_SHORT)) {
                isInputValid = true;
            } else {
                // Invalid group name.
                tvError.setText(getString(R.string.activity_group_search_error_search_name));
                isInputValid = false;
            }
        }
        // Enable or disable button and show or hide error message.
        if (isInputValid) {
            tvError.setVisibility(View.GONE);
            searchView.clearFocus();
        } else {
            tvError.setVisibility(View.VISIBLE);
        }
    }

    private void onRadioButtonClicked(int checkedId) {
        // Check which radio button is selected.
        switch (checkedId) {
            case R.id.activity_group_search_rb_id:
                if (searchView != null) {
                    searchView.setQueryHint(getString(R.string.activity_group_search_et_hint_id));
                }

                chkTutorial.setVisibility(View.GONE);
                chkWork.setVisibility(View.GONE);
                break;
            case R.id.activity_group_search_rb_name:
                if (searchView != null) {
                    searchView.setQueryHint(getString(R.string.activity_group_search_et_hint_name));
                }
                chkTutorial.setVisibility(View.VISIBLE);
                chkWork.setVisibility(View.VISIBLE);
                break;
        }
        // Clear view.
        tvError.setVisibility(View.GONE);
        tvInfo.setVisibility(View.GONE);
        lvGroups.setVisibility(View.GONE);
    }

    private void searchGroup(String input) {
        // Validate input.
        validateTextInput(input);
        // Check if device is connected to the internet.
        if (!Util.getInstance(this).isOnline()) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText(getString(R.string.general_error_no_connection));
            // Check if input is valid.
        } else if (isInputValid) {
            // Set searching view.
            lvGroups.setVisibility(View.GONE);
            tvError.setVisibility(View.GONE);
            tvInfo.setVisibility(View.GONE);
            pgrSearching.setVisibility(View.VISIBLE);
            // Send search request to server.
            if (rbId.isChecked()) {
                GroupAPI.getInstance(this).getGroup(groupId);
            } else {
                // Search for group name.
                if (chkTutorial.isChecked() && chkWork.isChecked()) {
                    GroupAPI.getInstance(this).getGroups(groupName, null);
                }
                if (chkTutorial.isChecked() && !chkWork.isChecked()) {
                    GroupAPI.getInstance(this).getGroups(groupName, GroupType.TUTORIAL.toString());
                }
                if (!chkTutorial.isChecked() && chkWork.isChecked()) {
                    GroupAPI.getInstance(this).getGroups(groupName, GroupType.WORKING.toString());
                }
            }
        }
    }

    /**
     * This method will be called when a group is posted to the EventBus.
     *
     * @param group The group object.
     */
    public void onEventMainThread(Group group) {
        Log.d(TAG, "EventBus: List<Group>");
        Log.d(TAG, group.toString());
        groups.clear();
        groups.add(group);
        updateGroupData();
    }

    /**
     * This method will be called when a list of groups is posted to the EventBus.
     *
     * @param groups The list containing group objects.
     */
    public void onEventMainThread(List<Group> groups) {
        Log.d(TAG, "EventBus: List<Group>");
        Log.d(TAG, groups.toString());
        if (groups.isEmpty()) {
            handleServerError(new ServerError(404, Constants.GROUP_NOT_FOUND));
        } else {
            this.groups = groups;
            updateGroupData();
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
        // Show appropriate error message.
        pgrSearching.setVisibility(View.GONE);
        switch (serverError.getErrorCode()) {
            case CONNECTION_FAILURE:
                tvError.setText(R.string.general_error_connection_failed);
                break;
            case Constants.GROUP_NOT_FOUND:
                tvInfo.setText(R.string.activity_group_search_error_not_found);
                tvInfo.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updateGroupData() {
        pgrSearching.setVisibility(View.GONE);
        lvGroups.setVisibility(View.VISIBLE);
        listAdapter.setData(groups);
        listAdapter.notifyDataSetChanged();
    }
}
