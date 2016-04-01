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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.ChannelAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Announcement;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.enums.Priority;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.util.Constants;
import ulm.university.news.app.util.TextInputLabels;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;

public class AnnouncementAddActivity extends AppCompatActivity implements DialogListener {
    /** This classes tag for logging. */
    private static final String TAG = "AnnouncementAddActivity";

    private TextInputLabels tilTitle;
    private TextInputLabels tilText;
    private Spinner spPriority;
    private ProgressBar pgrAdding;
    private TextView tvError;
    private Button btnCreate;
    private Toast toast;

    private ChannelDatabaseManager channelDBM;
    private int channelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set color theme according to channel and lecture type.
        channelId = getIntent().getIntExtra("channelId", 0);
        channelDBM = new ChannelDatabaseManager(this);
        Channel channel = channelDBM.getChannel(channelId);
        ChannelController.setColorTheme(this, channel);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_add);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_announcement_add_toolbar);
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
        tilTitle = (TextInputLabels) findViewById(R.id.activity_announcement_add_til_title);
        tilText = (TextInputLabels) findViewById(R.id.activity_announcement_add_til_text);
        spPriority = (Spinner) findViewById(R.id.activity_announcement_add_sp_priority);
        pgrAdding = (ProgressBar) findViewById(R.id.activity_announcement_add_pgr_adding);
        tvError = (TextView) findViewById(R.id.activity_announcement_add_tv_error);
        btnCreate = (Button) findViewById(R.id.activity_announcement_add_btn_create);

        tilTitle.setNameAndHint(getString(R.string.announcement_title));
        tilTitle.setLength(1, Constants.ANNOUNCEMENT_TITLE_MAX_LENGTH);
        tilText.setNameAndHint(getString(R.string.message_text));
        tilText.setLength(1, Constants.MESSAGE_MAX_LENGTH);

        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.priority, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        spPriority.setAdapter(adapter);
        spPriority.setSelection(0);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAnnouncement();
            }
        });

        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);
    }

    private void addAnnouncement() {
        boolean valid = true;
        if (!tilTitle.isValid()) {
            valid = false;
        }
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
            // All checks passed. Create new group.
            tvError.setVisibility(View.GONE);
            btnCreate.setVisibility(View.GONE);
            pgrAdding.setVisibility(View.VISIBLE);

            Priority priority;
            if (spPriority.getSelectedItemPosition() == 0) {
                priority = Priority.HIGH;
            } else {
                priority = Priority.NORMAL;
            }

            Announcement announcement = new Announcement();
            announcement.setTitle(tilTitle.getText());
            announcement.setText(tilText.getText());
            announcement.setPriority(priority);

            // Send announcement data to the server.
            ChannelAPI.getInstance(this).createAnnouncement(channelId, announcement);
        }
    }

    /**
     * This method will be called when an announcement is posted to the EventBus.
     *
     * @param announcement The announcement object.
     */
    public void onEventMainThread(Announcement announcement) {
        Log.d(TAG, "EventBus: Announcement");
        Log.d(TAG, announcement.toString());

        // Store announcement in database and show created message.
        channelDBM.storeAnnouncement(announcement);
        toast.setText(getString(R.string.announcement_created));
        toast.show();

        // Go back to moderator channel view.
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
