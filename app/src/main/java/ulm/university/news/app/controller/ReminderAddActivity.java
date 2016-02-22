package ulm.university.news.app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.joda.time.DateTime;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.Reminder;
import ulm.university.news.app.data.enums.Priority;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.util.Constants;
import ulm.university.news.app.util.TextInputLabels;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;
import static ulm.university.news.app.util.Constants.TIME_ZONE;

public class ReminderAddActivity extends AppCompatActivity implements DatePickerListener, TimePickerListener {
    /** This classes tag for logging. */
    private static final String TAG = "ReminderAddActivity";

    private TextInputLabels tilTitle;
    private TextInputLabels tilText;
    private Spinner spPriority;
    private ProgressBar pgrAdding;
    private TextView tvStartDateValue;
    private TextView tvEndDateValue;
    private TextView tvNextDateValue;
    private TextView tvTime;
    private TextView tvError;
    private Button btnCreate;

    private ChannelDatabaseManager channelDBM;
    private int channelId;
    private Toast toast;

    private DateTime startDate;
    private DateTime endDate;
    private DateTime nextDate;
    private Reminder reminder;
    private boolean isStartDateSet;
    private boolean isTimeSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set color theme according to channel and lecture type.
        channelId = getIntent().getIntExtra("channelId", 0);
        channelDBM = new ChannelDatabaseManager(this);
        Channel channel = channelDBM.getChannel(channelId);
        ChannelController.setColorTheme(this, channel);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_add);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_reminder_add_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        startDate = new DateTime(TIME_ZONE);
        endDate = new DateTime(TIME_ZONE);
        reminder = new Reminder();
        isStartDateSet = false;
        isTimeSet = false;

        initView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateUp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
        tilTitle = (TextInputLabels) findViewById(R.id.activity_reminder_add_til_title);
        tilText = (TextInputLabels) findViewById(R.id.activity_reminder_add_til_text);
        spPriority = (Spinner) findViewById(R.id.activity_reminder_add_sp_priority);
        pgrAdding = (ProgressBar) findViewById(R.id.activity_reminder_add_pgr_adding);
        tvStartDateValue = (TextView) findViewById(R.id.activity_reminder_add_tv_start_date_value);
        tvEndDateValue = (TextView) findViewById(R.id.activity_reminder_add_tv_end_date_value);
        tvNextDateValue = (TextView) findViewById(R.id.activity_reminder_add_tv_next_date_value);
        tvTime = (TextView) findViewById(R.id.activity_reminder_add_tv_time_value);
        tvError = (TextView) findViewById(R.id.activity_reminder_add_tv_error);
        btnCreate = (Button) findViewById(R.id.activity_reminder_add_btn_create);

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
                addReminder();
            }
        });

        tvStartDateValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dateFragment = new DatePickerFragment();
                dateFragment.show(getSupportFragmentManager(), "startDate");
            }
        });

        tvEndDateValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dateFragment = new DatePickerFragment();
                dateFragment.show(getSupportFragmentManager(), "endDate");
            }
        });

        tvTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timeFragment = new TimePickerFragment();
                timeFragment.show(getSupportFragmentManager(), "time");
            }
        });

        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);
    }

    private void addReminder() {
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

            Reminder reminder = new Reminder();
            reminder.setTitle(tilTitle.getText());
            reminder.setText(tilText.getText());
            reminder.setPriority(priority);

            // Send announcement data to the server.
            // ChannelAPI.getInstance(this).createReminder(reminder);
        }
    }

    /**
     * This method will be called when a reminder is posted to the EventBus.
     *
     * @param reminder The reminder object.
     */
    public void onEventMainThread(Reminder reminder) {
        Log.d(TAG, "EventBus: Reminder");
        Log.d(TAG, reminder.toString());

        // Store reminder in database and show created message.
        ChannelDatabaseManager channelDBM = new ChannelDatabaseManager(this);
        channelDBM.storeReminder(reminder);
        toast.setText(getString(R.string.channel_created));
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
    public void onDateSet(String tag, DatePicker view, int year, int month, int day) {
        if (tag.equals("startDate")) {
            startDate = startDate.year().setCopy(year);
            startDate = startDate.monthOfYear().setCopy(month + 1);
            startDate = startDate.dayOfMonth().setCopy(day);
            isStartDateSet = true;
            tvStartDateValue.setText(ChannelController.getFormattedDateOnly(startDate));
            if (isTimeSet) {
                reminder.setStartDate(startDate);
                reminder.computeFirstNextDate();
                tvNextDateValue.setText(ChannelController.getFormattedDateLong(reminder.getNextDate()));
            }
        } else {
            endDate = endDate.year().setCopy(year);
            endDate = endDate.monthOfYear().setCopy(month + 1);
            endDate = endDate.dayOfMonth().setCopy(day);
            reminder.setEndDate(endDate);
            tvEndDateValue.setText(ChannelController.getFormattedDateOnly(endDate));
        }
    }

    @Override
    public void onTimeSet(String tag, TimePicker view, int hourOfDay, int minute) {
        if (tag.equals("time")) {
            startDate = startDate.hourOfDay().setCopy(hourOfDay);
            startDate = startDate.minuteOfHour().setCopy(minute);
            endDate = endDate.hourOfDay().setCopy(hourOfDay);
            endDate = endDate.minuteOfHour().setCopy(minute);
            isTimeSet = true;
            reminder.setStartDate(startDate);
            reminder.setEndDate(endDate);
            tvTime.setText(ChannelController.getFormattedTimeOnly(startDate));
            if (isStartDateSet) {
                reminder.setStartDate(startDate);
                reminder.computeFirstNextDate();
                tvNextDateValue.setText(ChannelController.getFormattedDateLong(reminder.getNextDate()));
            }
        }
    }
}
