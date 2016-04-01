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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.joda.time.DateTime;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.ChannelAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.Reminder;
import ulm.university.news.app.data.enums.Priority;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.util.Constants;
import ulm.university.news.app.util.TextInputLabels;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;

public class ReminderEditActivity extends AppCompatActivity implements DatePickerListener, TimePickerListener,
        IntervalPickerListener, DialogListener {
    /** This classes tag for logging. */
    private static final String TAG = "ReminderEditActivity";

    private TextInputLabels tilTitle;
    private TextInputLabels tilText;
    private Spinner spPriority;
    private ProgressBar pgrAdding;
    private TextView tvStartDateValue;
    private TextView tvEndDateValue;
    private TextView tvNextDateValue;
    private TextView tvIntervalValue;
    private TextView tvTimeValue;
    private TextView tvError;
    private Button btnCreate;
    private CheckBox chkIgnore;
    // private ImageView ivIconTitle;

    private ChannelDatabaseManager channelDBM;
    private int channelId;
    private Toast toast;

    private DateTime startDate;
    private DateTime endDate;
    private Reminder reminder;
    private boolean isTimeSet;
    private int interval;
    private Integer intervalType;
    private int hour, minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Get reminder data from database.
        channelDBM = new ChannelDatabaseManager(this);
        int reminderId = getIntent().getIntExtra("reminderId", 0);
        reminder = channelDBM.getReminder(reminderId);
        channelId = reminder.getChannelId();
        // Set color theme according to channel and lecture type.
        Channel channel = channelDBM.getChannel(channelId);
        ChannelController.setColorTheme(this, channel);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_add);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_reminder_add_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initView();
        setStoredValues();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                YesNoDialogFragment dialog = new YesNoDialogFragment();
                Bundle args = new Bundle();
                args.putString(YesNoDialogFragment.DIALOG_TITLE, getString(R.string.general_leave_page_title));
                args.putString(YesNoDialogFragment.DIALOG_TEXT, getString(R.string.general_leave_page_edited));
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
        args.putString(YesNoDialogFragment.DIALOG_TEXT, getString(R.string.general_leave_page_edited));
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
        tilTitle = (TextInputLabels) findViewById(R.id.activity_reminder_add_til_title);
        tilText = (TextInputLabels) findViewById(R.id.activity_reminder_add_til_text);
        spPriority = (Spinner) findViewById(R.id.activity_reminder_add_sp_priority);
        pgrAdding = (ProgressBar) findViewById(R.id.activity_reminder_add_pgr_adding);
        tvStartDateValue = (TextView) findViewById(R.id.activity_reminder_add_tv_start_date_value);
        tvEndDateValue = (TextView) findViewById(R.id.activity_reminder_add_tv_end_date_value);
        tvNextDateValue = (TextView) findViewById(R.id.activity_reminder_add_tv_next_date_value);
        tvIntervalValue = (TextView) findViewById(R.id.activity_reminder_add_tv_interval_value);
        tvTimeValue = (TextView) findViewById(R.id.activity_reminder_add_tv_time_value);
        tvError = (TextView) findViewById(R.id.activity_reminder_add_tv_error);
        btnCreate = (Button) findViewById(R.id.activity_reminder_add_btn_create);
        chkIgnore = (CheckBox) findViewById(R.id.activity_reminder_add_chk_ignore);
        // ivIconTitle = (ImageView) findViewById(R.id.activity_reminder_add_iv_icon_title);

        tilTitle.setNameAndHint(getString(R.string.announcement_title));
        tilTitle.setLength(1, Constants.ANNOUNCEMENT_TITLE_MAX_LENGTH);
        tilText.setNameAndHint(getString(R.string.message_text));
        tilText.setLength(1, Constants.MESSAGE_MAX_LENGTH);

        // ivIconTitle.setColorFilter(ContextCompat.getColor(this, R.color.grey));

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
                DialogFragment dateDialog = new DatePickerDialogFragment();
                Bundle args = new Bundle();
                args.putInt(DatePickerDialogFragment.YEAR, startDate.getYear());
                args.putInt(DatePickerDialogFragment.MONTH, startDate.getMonthOfYear() - 1);
                args.putInt(DatePickerDialogFragment.DAY, startDate.getDayOfMonth());
                dateDialog.setArguments(args);
                dateDialog.show(getSupportFragmentManager(), "startDate");
            }
        });

        tvEndDateValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dateDialog = new DatePickerDialogFragment();
                Bundle args = new Bundle();
                args.putInt(DatePickerDialogFragment.YEAR, endDate.getYear());
                args.putInt(DatePickerDialogFragment.MONTH, endDate.getMonthOfYear() - 1);
                args.putInt(DatePickerDialogFragment.DAY, endDate.getDayOfMonth());
                dateDialog.setArguments(args);
                dateDialog.show(getSupportFragmentManager(), "endDate");
            }
        });

        tvTimeValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timeDialog = new TimePickerDialogFragment();
                Bundle args = new Bundle();
                args.putInt(TimePickerDialogFragment.HOUR, hour);
                args.putInt(TimePickerDialogFragment.MINUTE, minute);
                timeDialog.setArguments(args);
                timeDialog.show(getSupportFragmentManager(), "time");
            }
        });

        tvIntervalValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment intervalDialog = new IntervalPickerDialogFragment();
                Bundle args = new Bundle();
                args.putInt(IntervalPickerDialogFragment.INTERVAL, interval);
                if (intervalType != null) {
                    // Set previous selected value.
                    args.putInt(IntervalPickerDialogFragment.INTERVAL_TYPE, intervalType);
                } else {
                    // If nothing was set previously, set daily as default.
                    args.putInt(IntervalPickerDialogFragment.INTERVAL_TYPE, 1);
                }
                intervalDialog.setArguments(args);
                intervalDialog.show(getSupportFragmentManager(), "interval");
            }
        });

        chkIgnore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setNextDate();
            }
        });

        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);
    }

    private void setStoredValues() {
        startDate = reminder.getStartDate();
        endDate = reminder.getEndDate();
        interval = reminder.getInterval();
        hour = startDate.getHourOfDay();
        minute = startDate.getMinuteOfHour();
        // Reconstruct interval type.
        if (interval == 0) {
            intervalType = 0;
        } else {
            // Set interval to days.
            interval /= 86400;
            intervalType = 1;
            if (interval % 7 == 0) {
                // Set interval to weeks.
                interval /= 7;
                intervalType = 2;
            }
        }
        isTimeSet = true;
        tilTitle.setText(reminder.getTitle());
        tilText.setText(reminder.getText());
        if (reminder.getPriority().equals(Priority.HIGH)) {
            spPriority.setSelection(0);
        } else {
            spPriority.setSelection(1);
        }
        btnCreate.setText(getString(R.string.general_edit));
        tvStartDateValue.setText(Util.getInstance(this).getFormattedDateOnly(startDate));
        tvEndDateValue.setText(Util.getInstance(this).getFormattedDateOnly(endDate));
        tvTimeValue.setText(Util.getInstance(this).getFormattedTimeOnly(startDate));
        switch (intervalType) {
            case 0:
                tvIntervalValue.setText(getString(R.string.reminder_interval_once_text));
                break;
            case 1:
                if (interval == 1) {
                    tvIntervalValue.setText(getString(R.string.reminder_interval_one_day));
                } else {
                    tvIntervalValue.setText(String.format(getString(R.string.reminder_interval_multiple_days),
                            interval));
                }
                break;
            case 2:
                if (interval == 1) {
                    tvIntervalValue.setText(getString(R.string.reminder_interval_one_week));
                } else {
                    tvIntervalValue.setText(String.format(getString(R.string.reminder_interval_multiple_weeks),
                            interval));
                }
                break;
        }
        setNextDate();
        chkIgnore.setChecked(reminder.isIgnore());
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
            String message = getString(R.string.general_error_no_connection) + " " + getString(R.string
                    .general_error_create);
            toast.setText(message);
            toast.show();
            valid = false;
        }

        if (!reminder.isValidDates()) {
            tvError.setText(getString(R.string.activity_reminder_add_error_invalid_dates));
            tvError.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (reminder.getStartDate() == null || reminder.getEndDate() == null || reminder.getInterval() == null) {
            tvError.setText(getString(R.string.activity_reminder_add_error_not_set));
            tvError.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (valid) {
            // All checks passed. Create new reminder.
            tvError.setVisibility(View.GONE);
            btnCreate.setVisibility(View.GONE);
            pgrAdding.setVisibility(View.VISIBLE);

            Priority priority;
            if (spPriority.getSelectedItemPosition() == 0) {
                priority = Priority.HIGH;
            } else {
                priority = Priority.NORMAL;
            }

            reminder.setTitle(tilTitle.getText());
            reminder.setText(tilText.getText());
            reminder.setPriority(priority);
            reminder.setIgnore(chkIgnore.isChecked());
            reminder.setChannelId(channelId);
            reminder.setNextDate(null);
            reminder.setCreationDate(null);
            reminder.setModificationDate(null);

            // Send reminder data to the server.
            ChannelAPI.getInstance(this).changeReminder(reminder);
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
        channelDBM.updateReminder(reminder);
        toast.setText(getString(R.string.reminder_edited));
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
            reminder.setStartDate(startDate);
            tvStartDateValue.setText(Util.getInstance(this).getFormattedDateOnly(startDate));
        } else {
            endDate = endDate.year().setCopy(year);
            endDate = endDate.monthOfYear().setCopy(month + 1);
            endDate = endDate.dayOfMonth().setCopy(day);
            reminder.setEndDate(endDate);
            tvEndDateValue.setText(Util.getInstance(this).getFormattedDateOnly(endDate));
        }
        setNextDate();
        tvError.setVisibility(View.GONE);
    }

    @Override
    public void onTimeSet(String tag, TimePicker view, int hourOfDay, int minute) {
        hour = hourOfDay;
        this.minute = minute;
        startDate = startDate.hourOfDay().setCopy(hourOfDay);
        startDate = startDate.minuteOfHour().setCopy(minute);
        endDate = endDate.hourOfDay().setCopy(hourOfDay);
        endDate = endDate.minuteOfHour().setCopy(minute);
        reminder.setStartDate(startDate);
        reminder.setEndDate(endDate);
        isTimeSet = true;
        tvTimeValue.setText(Util.getInstance(this).getFormattedTimeOnly(startDate));
        setNextDate();
        tvError.setVisibility(View.GONE);
    }

    @Override
    public void onIntervalSet(String tag, int interval, int type, String intervalText) {
        this.interval = interval;
        intervalType = type;
        switch (type) {
            case 0:
                // One time reminder.
                interval = 0;
                break;
            case 1:
                // Days: 86400 is one day in seconds.
                interval *= 86400;
                break;
            case 2:
                // Weeks: One day times seven.
                interval *= 86400 * 7;
        }
        reminder.setInterval(interval);
        tvIntervalValue.setText(intervalText);
        setNextDate();
        tvError.setVisibility(View.GONE);
    }

    private void setNextDate() {
        if (isTimeSet && reminder.getStartDate() != null && reminder.getEndDate() != null
                && reminder.getInterval() != null) {
            reminder.computeFirstNextDate();
            if (chkIgnore.isChecked()) {
                // If checked, compute new next date.
                reminder.computeNextDate();
            }
            if (reminder.isExpired()) {
                tvNextDateValue.setText(getString(R.string.reminder_expired));
            } else {
                tvNextDateValue.setText(Util.getInstance(this).getFormattedDateLong(reminder.getNextDate()));
            }
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
