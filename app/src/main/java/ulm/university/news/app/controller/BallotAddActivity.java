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
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.GroupAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Ballot;
import ulm.university.news.app.manager.database.GroupDatabaseManager;
import ulm.university.news.app.util.TextInputLabels;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;
import static ulm.university.news.app.util.Constants.DESCRIPTION_MAX_LENGTH;
import static ulm.university.news.app.util.Constants.GROUP_NOT_FOUND;
import static ulm.university.news.app.util.Constants.NAME_PATTERN;

public class BallotAddActivity extends AppCompatActivity implements DialogListener {
    /** This classes tag for logging. */
    private static final String TAG = "BallotAddActivity";

    private TextInputLabels tilTitle;
    private TextInputLabels tilDescription;
    private CheckBox chkMultipleChoice;
    private CheckBox chkPublicVotes;
    private TextView tvError;
    private ProgressBar pgrSending;
    private Button btnCreate;

    private Toast toast;
    private int groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ballot_add);

        groupId = getIntent().getIntExtra("groupId", 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_ballot_add_toolbar);
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
                if (warnOnLeave()) {
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
        if (warnOnLeave()) {
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

    private void initView() {
        tilTitle = (TextInputLabels) findViewById(R.id.activity_ballot_add_til_title);
        tilDescription = (TextInputLabels) findViewById(R.id.activity_ballot_add_til_description);
        tvError = (TextView) findViewById(R.id.activity_ballot_add_tv_error);
        pgrSending = (ProgressBar) findViewById(R.id.activity_ballot_add_pgr_adding);
        btnCreate = (Button) findViewById(R.id.activity_ballot_add_btn_create);
        chkMultipleChoice = (CheckBox) findViewById(R.id.activity_ballot_add_chk_multiple_choice);
        chkPublicVotes = (CheckBox) findViewById(R.id.activity_ballot_add_chk_public_votes);

        tilTitle.setNameAndHint(getString(R.string.general_title));
        tilTitle.setLength(3, 45);
        tilTitle.setPattern(NAME_PATTERN);

        tilDescription.setNameAndHint(getString(R.string.general_description));
        tilDescription.setLength(0, DESCRIPTION_MAX_LENGTH);

        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
        if (tv != null) tv.setGravity(Gravity.CENTER);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBallot();
            }
        });
    }

    private boolean warnOnLeave() {
        // Warn user before leaving page if something was entered in the fields.
        return !tilTitle.getText().isEmpty() || !tilDescription.getText().isEmpty();
    }

    private void addBallot() {
        boolean valid = true;
        if (!Util.getInstance(this).isOnline()) {
            String message = getString(R.string.general_error_no_connection);
            message += " " + getString(R.string.general_error_create);
            toast.setText(message);
            toast.show();
            valid = false;
        }
        if (!tilTitle.isValid()) {
            valid = false;
        }
        if (!tilDescription.isValid()) {
            valid = false;
        }
        if (valid) {
            // All checks passed. Create new ballot.
            tvError.setVisibility(View.GONE);
            pgrSending.setVisibility(View.VISIBLE);

            Ballot ballot = new Ballot();
            ballot.setTitle(tilTitle.getText());
            ballot.setDescription(tilDescription.getText());
            ballot.setPublicVotes(chkPublicVotes.isChecked());
            ballot.setMultipleChoice(chkMultipleChoice.isChecked());
            GroupAPI.getInstance(this).createBallot(groupId, ballot);
        }
    }

    /**
     * This method will be called when a ballot is posted to the EventBus.
     *
     * @param ballot The ballot object.
     */
    public void onEventMainThread(Ballot ballot) {
        Log.d(TAG, "EventBus: Ballot");
        Log.d(TAG, ballot.toString());
        pgrSending.setVisibility(View.GONE);
        // Store ballot and continue adding ballot options.
        GroupDatabaseManager groupDBM = new GroupDatabaseManager(this);
        groupDBM.storeBallot(groupId, ballot);
        // Go to another activity to options to the ballot.
        Intent intent = new Intent(this, OptionAddActivity.class);
        intent.putExtra("groupId", groupId);
        intent.putExtra("ballotId", ballot.getId());
        intent.putExtra("numberOfOptions", 2);
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
        pgrSending.setVisibility(View.GONE);
        btnCreate.setVisibility(View.VISIBLE);
        Intent intent;
        switch (serverError.getErrorCode()) {
            case CONNECTION_FAILURE:
                tvError.setText(R.string.general_error_connection_failed);
                break;
            case GROUP_NOT_FOUND:
                new GroupDatabaseManager(this).setGroupToDeleted(groupId);
                toast.setText(getString(R.string.group_deleted));
                toast.show();
                // Close activity and go to the main screen to show deleted dialog on restart activity.
                intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
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
