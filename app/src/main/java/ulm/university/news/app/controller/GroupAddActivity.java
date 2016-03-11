package ulm.university.news.app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.GroupAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Group;
import ulm.university.news.app.data.enums.GroupType;
import ulm.university.news.app.util.TextInputLabels;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.ACCOUNT_NAME_PATTERN;
import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;
import static ulm.university.news.app.util.Constants.DESCRIPTION_MAX_LENGTH;
import static ulm.university.news.app.util.Constants.PASSWORD_PATTERN;

public class GroupAddActivity extends AppCompatActivity {
    /** This classes tag for logging. */
    private static final String TAG = "GroupAddActivity";

    private TextInputLabels tilName;
    private TextInputLabels tilDescription;
    private TextInputLabels tilPassword;
    private EditText etYear;
    private Spinner spGroupType;
    private Spinner spTerm;
    private TextView tvError;
    private ProgressBar pgrSearching;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_add);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_group_add_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_group_add_menu, menu);
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
            case R.id.activity_group_add_add:
                addGroup();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initView() {
        tilName = (TextInputLabels) findViewById(R.id.activity_group_add_til_name);
        tilDescription = (TextInputLabels) findViewById(R.id.activity_group_add_til_description);
        tilPassword = (TextInputLabels) findViewById(R.id.activity_group_add_til_password);
        etYear = (EditText) findViewById(R.id.activity_group_add_et_year);
        spGroupType = (Spinner) findViewById(R.id.activity_group_add_sp_group_type);
        spTerm = (Spinner) findViewById(R.id.activity_group_add_sp_term);
        tvError = (TextView) findViewById(R.id.activity_group_add_tv_error);
        pgrSearching = (ProgressBar) findViewById(R.id.activity_group_add_pgr_adding);

        tilName.setNameAndHint(getString(R.string.group_name));
        tilName.setLength(3, 35);
        tilName.setPattern(ACCOUNT_NAME_PATTERN);

        tilDescription.setNameAndHint(getString(R.string.group_description));
        tilDescription.setLength(0, DESCRIPTION_MAX_LENGTH);

        tilPassword.setNameAndHint(getString(R.string.group_password));
        tilPassword.setLength(8, 20);
        tilPassword.setPattern(PASSWORD_PATTERN);
        tilPassword.setToPasswordField();

        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.group_types, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        spGroupType.setAdapter(adapter);
        spGroupType.setSelection(0);

        adapter = ArrayAdapter.createFromResource(this, R.array.terms, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTerm.setAdapter(adapter);
        spTerm.setSelection(0);

        etYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                tvError.setVisibility(View.GONE);
            }
        });
    }

    private void addGroup() {
        boolean valid = true;
        String year = etYear.getText().toString().trim();
        if (!tilName.isValid()) {
            valid = false;
        }
        if (!tilPassword.isValid()) {
            valid = false;
        }
        if (tilDescription.isValid()) {
            valid = false;
        }
        try {
            int y = Integer.parseInt(year);
            if(y < 2016){
                tvError.setText(getString(R.string.channel_term_year_error));
                tvError.setVisibility(View.VISIBLE);
                valid = false;
            }
        } catch (NumberFormatException e) {
            tvError.setText(getString(R.string.channel_term_year_error));
            tvError.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (valid) {
            // All checks passed. Create new group.
            tvError.setVisibility(View.GONE);
            pgrSearching.setVisibility(View.VISIBLE);

            String term;
            if (spTerm.getSelectedItemPosition() == 0) {
                term = getString(R.string.channel_term_summer_short);
            } else {
                term = getString(R.string.channel_term_winter_short);
            }
            term += year;

            GroupType groupType;
            if (spGroupType.getSelectedItemPosition() == 0) {
                groupType = GroupType.WORKING;
            } else {
                groupType = GroupType.TUTORIAL;
            }

            String password = tilPassword.getText();
            password = Util.hashPassword(password);

            Group group = new Group(tilName.getText(), tilDescription.getText(), groupType, term, password);
            GroupAPI.getInstance(this).createGroup(group);
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
        pgrSearching.setVisibility(View.GONE);
        // TODO Save group in database. Then show GroupActivity.
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
        }
    }
}
