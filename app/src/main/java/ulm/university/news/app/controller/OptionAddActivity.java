package ulm.university.news.app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.GroupAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Option;
import ulm.university.news.app.manager.database.GroupDatabaseManager;
import ulm.university.news.app.util.Constants;
import ulm.university.news.app.util.TextInputLabels;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;

public class OptionAddActivity extends AppCompatActivity implements DialogListener {
    /** This classes tag for logging. */
    private static final String TAG = "OptionAddActivity";

    private TextInputLabels tilText;
    private ProgressBar pgrAdding;
    private TextView tvError;
    private Button btnCreate;
    private Toast toast;

    private GroupDatabaseManager groupDBM;
    private int groupId;
    private int ballotId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set color scheme to ballot green.
        setTheme(R.style.UlmUniversity_Ballot);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_option_add);
        groupId = getIntent().getIntExtra("groupId", 0);
        ballotId = getIntent().getIntExtra("ballotId", 0);
        groupDBM = new GroupDatabaseManager(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_option_add_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        initView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!tilText.getText().isEmpty()) {
                    YesNoDialogFragment dialog = new YesNoDialogFragment();
                    Bundle args = new Bundle();
                    args.putString(YesNoDialogFragment.DIALOG_TITLE, getString(R.string.general_leave_page_title));
                    args.putString(YesNoDialogFragment.DIALOG_TEXT, getString(R.string.general_leave_page));
                    dialog.setArguments(args);
                    dialog.show(getSupportFragmentManager(), YesNoDialogFragment.DIALOG_LEAVE_PAGE_UP);
                    return true;
                } else {
                    navigateUp();
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (!tilText.getText().isEmpty()) {
            YesNoDialogFragment dialog = new YesNoDialogFragment();
            Bundle args = new Bundle();
            args.putString(YesNoDialogFragment.DIALOG_TITLE, getString(R.string.general_leave_page_title));
            args.putString(YesNoDialogFragment.DIALOG_TEXT, getString(R.string.general_leave_page));
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), YesNoDialogFragment.DIALOG_LEAVE_PAGE_BACK);
        } else {
            super.onBackPressed();
        }
    }

    private void navigateUp() {
        Intent intent = NavUtils.getParentActivityIntent(this);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        NavUtils.navigateUpTo(this, intent);
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

    private void initView() {
        tilText = (TextInputLabels) findViewById(R.id.activity_option_add_til_text);
        tvError = (TextView) findViewById(R.id.activity_option_add_tv_error);
        btnCreate = (Button) findViewById(R.id.activity_option_add_btn_create);
        pgrAdding = (ProgressBar) findViewById(R.id.activity_option_add_pgr_adding);

        tilText.setNameAndHint(getString(R.string.general_text));
        tilText.setLength(1, Constants.OPTION_TEXT_MAX_LENGTH);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOption();
            }
        });

        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);
    }

    private void addOption() {
        boolean valid = true;
        if (!tilText.isValid()) {
            valid = false;
        }
        if (!Util.getInstance(this).isOnline()) {
            String message = getString(R.string.general_error_no_connection) + getString(R.string.general_error_create);
            toast.setText(message);
            toast.show();
            valid = false;
        }

        if (valid) {
            // All checks passed. Create new option.
            tvError.setVisibility(View.GONE);
            btnCreate.setVisibility(View.GONE);
            pgrAdding.setVisibility(View.VISIBLE);

            Option option = new Option();
            option.setText(tilText.getText());

            // Send option data to the server.
            GroupAPI.getInstance(this).createOption(groupId, ballotId, option);
        }
    }

    /**
     * This method will be called when an option is posted to the EventBus.
     *
     * @param option The option object.
     */
    public void onEventMainThread(Option option) {
        Log.d(TAG, "EventBus: Option");
        Log.d(TAG, option.toString());

        // Store option in database and show created message.
        groupDBM.storeOption(ballotId, option);
        toast.setText(getString(R.string.option_created));
        toast.show();

        // Go back to ballot view.
        navigateUp();
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
        pgrAdding.setVisibility(View.GONE);
        btnCreate.setVisibility(View.VISIBLE);
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
