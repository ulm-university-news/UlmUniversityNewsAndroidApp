package ulm.university.news.app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.GroupAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Group;
import ulm.university.news.app.manager.database.GroupDatabaseManager;
import ulm.university.news.app.util.TextInputLabels;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.ACCOUNT_NAME_PATTERN;
import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;
import static ulm.university.news.app.util.Constants.DESCRIPTION_MAX_LENGTH;
import static ulm.university.news.app.util.Constants.PASSWORD_GROUP_PATTERN;

public class GroupEditActivity extends AppCompatActivity implements DialogListener {
    /** This classes tag for logging. */
    private static final String TAG = "GroupEditActivity";

    private TextInputLabels tilName;
    private TextInputLabels tilDescription;
    private TextInputLabels tilPassword;
    private EditText etYear;
    private Spinner spTerm;
    private TextView tvError;
    private ProgressBar pgrSearching;
    private Button btnEdit;
    private CheckBox chkPassword;

    private Toast toast;
    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);

        int groupId = getIntent().getIntExtra("groupId", 0);
        group = new GroupDatabaseManager(this).getGroup(groupId);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_group_edit_toolbar);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                YesNoDialogFragment dialog = new YesNoDialogFragment();
                Bundle args = new Bundle();
                args.putString(YesNoDialogFragment.DIALOG_TITLE, getString(R.string.general_leave_page_title));
                args.putString(YesNoDialogFragment.DIALOG_TEXT, getString(R.string.general_leave_page));
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), YesNoDialogFragment.DIALOG_LEAVE_PAGE_UP);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        YesNoDialogFragment dialog = new YesNoDialogFragment();
        Bundle args = new Bundle();
        args.putString(YesNoDialogFragment.DIALOG_TITLE, getString(R.string.general_leave_page_title));
        args.putString(YesNoDialogFragment.DIALOG_TEXT, getString(R.string.general_leave_page));
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), YesNoDialogFragment.DIALOG_LEAVE_PAGE_BACK);
    }

    private void navigateUp() {
        Intent intent = NavUtils.getParentActivityIntent(this);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        NavUtils.navigateUpTo(this, intent);
    }

    private void initView() {
        tilName = (TextInputLabels) findViewById(R.id.activity_group_edit_til_name);
        tilDescription = (TextInputLabels) findViewById(R.id.activity_group_edit_til_description);
        tilPassword = (TextInputLabels) findViewById(R.id.activity_group_edit_til_password);
        etYear = (EditText) findViewById(R.id.activity_group_edit_et_year);
        spTerm = (Spinner) findViewById(R.id.activity_group_edit_sp_term);
        tvError = (TextView) findViewById(R.id.activity_group_edit_tv_error);
        pgrSearching = (ProgressBar) findViewById(R.id.activity_group_edit_pgr_adding);
        btnEdit = (Button) findViewById(R.id.activity_group_edit_btn_edit);
        chkPassword = (CheckBox) findViewById(R.id.activity_group_edit_chk_password);

        tilName.setNameAndHint(getString(R.string.group_name));
        tilName.setLength(3, 35);
        tilName.setPattern(ACCOUNT_NAME_PATTERN);
        tilName.setText(group.getName());

        tilDescription.setNameAndHint(getString(R.string.general_description));
        tilDescription.setLength(0, DESCRIPTION_MAX_LENGTH);
        tilDescription.setText(group.getDescription());

        tilPassword.setNameAndHint(getString(R.string.group_password));
        tilPassword.setLength(1, 20);
        tilPassword.setPattern(PASSWORD_GROUP_PATTERN);
        tilPassword.setToPasswordField();

        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
        if (tv != null) tv.setGravity(Gravity.CENTER);

        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.terms, R.layout
                .spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTerm.setAdapter(adapter);
        if (group.getTerm().contains("S")) {
            spTerm.setSelection(0);
        } else {
            spTerm.setSelection(1);
        }

        String year = group.getTerm().substring(1);
        etYear.setText(year);
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

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editGroup();
            }
        });

        chkPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tilPassword.setVisibility(View.VISIBLE);
                } else {
                    tilPassword.setVisibility(View.GONE);
                }
            }
        });
    }

    private void editGroup() {
        boolean valid = true;
        if (!Util.getInstance(this).isOnline()) {
            String message = getString(R.string.general_error_no_connection);
            message += " " + getString(R.string.general_error_edit);
            toast.setText(message);
            toast.show();
            valid = false;
        }
        String year = etYear.getText().toString().trim();
        if (!tilName.isValid()) {
            valid = false;
        }
        if (chkPassword.isChecked() && !tilPassword.isValid()) {
            valid = false;
        }
        if (!tilDescription.isValid()) {
            valid = false;
        }
        try {
            int y = Integer.parseInt(year);
            if (y < 2016) {
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

            String password = null;
            if (chkPassword.isChecked()) {
                password = tilPassword.getText();
                password = Util.hashPassword(password);
            }

            Group g = new Group(tilName.getText(), tilDescription.getText(), group.getGroupType(), term, password);
            g.setId(group.getId());
            GroupAPI.getInstance(this).changeGroup(g);
        }
    }

    /**
     * This method will be called when a group is posted to the EventBus.
     *
     * @param group The group object.
     */
    public void onEventMainThread(Group group) {
        Log.d(TAG, "EventBus: Group");
        Log.d(TAG, group.toString());
        pgrSearching.setVisibility(View.GONE);
        // Update group in database.
        GroupDatabaseManager groupDBM = new GroupDatabaseManager(this);
        groupDBM.updateGroup(group);
        Intent intent = new Intent(this, GroupActivity.class);
        intent.putExtra("groupId", group.getId());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
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

    @Override
    public void onDialogPositiveClick(String tag) {
        if (tag.equals(YesNoDialogFragment.DIALOG_LEAVE_PAGE_UP)) {
            navigateUp();
        } else if (tag.equals(YesNoDialogFragment.DIALOG_LEAVE_PAGE_BACK)) {
            super.onBackPressed();
        }
    }
}
