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
import ulm.university.news.app.api.BusEventBallotChange;
import ulm.university.news.app.api.GroupAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Ballot;
import ulm.university.news.app.manager.database.GroupDatabaseManager;
import ulm.university.news.app.util.TextInputLabels;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.BALLOT_NOT_FOUND;
import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;
import static ulm.university.news.app.util.Constants.DESCRIPTION_MAX_LENGTH;
import static ulm.university.news.app.util.Constants.GROUP_NOT_FOUND;
import static ulm.university.news.app.util.Constants.NAME_PATTERN;

public class BallotEditActivity extends AppCompatActivity implements DialogListener {
    /** This classes tag for logging. */
    private static final String TAG = "BallotEditActivity";

    private TextInputLabels tilTitle;
    private TextInputLabels tilDescription;
    private TextView tvError;
    private ProgressBar pgrSending;
    private Button btnEdit;

    private Toast toast;
    private int groupId;
    private Ballot ballot;
    private GroupDatabaseManager groupDBM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set color scheme to ballot green.
        setTheme(R.style.UlmUniversity_Ballot);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ballot_edit);

        groupId = getIntent().getIntExtra("groupId", 0);
        int ballotId = getIntent().getIntExtra("ballotId", 0);
        groupDBM = new GroupDatabaseManager(this);
        ballot = groupDBM.getBallot(ballotId);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_ballot_edit_toolbar);
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
        tilTitle = (TextInputLabels) findViewById(R.id.activity_ballot_edit_til_title);
        tilDescription = (TextInputLabels) findViewById(R.id.activity_ballot_edit_til_description);
        tvError = (TextView) findViewById(R.id.activity_ballot_edit_tv_error);
        pgrSending = (ProgressBar) findViewById(R.id.activity_ballot_edit_pgr_adding);
        btnEdit = (Button) findViewById(R.id.activity_ballot_edit_btn_edit);

        tilTitle.setNameAndHint(getString(R.string.general_title));
        tilTitle.setLength(3, 45);
        tilTitle.setPattern(NAME_PATTERN);
        tilTitle.setText(ballot.getTitle());

        tilDescription.setNameAndHint(getString(R.string.general_description));
        tilDescription.setLength(0, DESCRIPTION_MAX_LENGTH);
        tilDescription.setText(ballot.getDescription());

        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
        if (tv != null) tv.setGravity(Gravity.CENTER);

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBallot();
            }
        });
    }

    private boolean warnOnLeave() {
        // Warn user before leaving page if fields were edited.
        return !tilTitle.getText().equals(ballot.getTitle()) ||
                !tilDescription.getText().equals(ballot.getDescription());
    }

    private void editBallot() {
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
            // All checks passed. Edit ballot.
            tvError.setVisibility(View.GONE);
            pgrSending.setVisibility(View.VISIBLE);

            ballot.setTitle(tilTitle.getText());
            ballot.setDescription(tilDescription.getText());
            GroupAPI.getInstance(this).changeBallot(groupId, ballot);
        }
    }

    /**
     * This method will be called when a changed ballot is posted to the EventBus.
     *
     * @param event The event containing the changed ballot object.
     */
    public void onEventMainThread(BusEventBallotChange event) {
        Log.d(TAG, event.toString());
        pgrSending.setVisibility(View.GONE);
        // Update ballot and leave activity.
        groupDBM.updateBallot(event.getBallot());
        toast.setText(getString(R.string.ballot_edited));
        toast.show();
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
        btnEdit.setVisibility(View.VISIBLE);
        Intent intent;
        switch (serverError.getErrorCode()) {
            case CONNECTION_FAILURE:
                tvError.setText(R.string.general_error_connection_failed);
                break;
            case GROUP_NOT_FOUND:
                groupDBM.setGroupToDeleted(groupId);
                toast.setText(getString(R.string.group_deleted));
                toast.show();
                // Close activity and go to the main screen to show deleted dialog on restart activity.
                intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
            case BALLOT_NOT_FOUND:
                groupDBM.deleteBallot(ballot.getId());
                toast.setText(getString(R.string.ballot_delete_server));
                toast.show();
                intent = new Intent(this, GroupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
                finish();
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
