package ulm.university.news.app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.BusEvent;
import ulm.university.news.app.api.ChannelAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.Moderator;
import ulm.university.news.app.data.Reminder;
import ulm.university.news.app.data.ResourceDetail;
import ulm.university.news.app.data.enums.Priority;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.manager.database.ModeratorDatabaseManager;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;
import static ulm.university.news.app.util.Constants.REMINDER_NOT_FOUND;

public class ReminderDetailActivity extends AppCompatActivity implements DialogListener {
    /** This classes tag for logging. */
    private static final String TAG = "ReminderDetailActivity";

    private ListView lvReminderDetails;
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<ResourceDetail> resourceDetails;
    private ResourceDetailListAdapter listAdapter;
    private ChannelDatabaseManager channelDBM;
    private ModeratorDatabaseManager moderatorDBM;
    private Reminder reminder;
    private int channelId;
    private Toast toast;
    private String errorMessage;

    private MenuItem menuItemActivate;
    private MenuItem menuItemDeactivate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Get reminder data from database.
        channelDBM = new ChannelDatabaseManager(this);
        moderatorDBM = new ModeratorDatabaseManager(this);
        int reminderId = getIntent().getIntExtra("reminderId", 0);
        reminder = channelDBM.getReminder(reminderId);
        channelId = reminder.getChannelId();
        // Set color theme according to channel and lecture type.
        Channel channel = channelDBM.getChannel(channelId);
        ChannelController.setColorTheme(this, channel);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_reminder_detail_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        resourceDetails = new ArrayList<>();
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update reminder in case it was edited.
        reminder = channelDBM.getReminder(reminder.getId());
        setReminderDetails();
        listAdapter.setResourceDetails(resourceDetails);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_moderator_reminder_detail_menu, menu);
        menuItemActivate = menu.findItem(R.id.activity_moderator_reminder_detail_activate);
        menuItemDeactivate = menu.findItem(R.id.activity_moderator_reminder_detail_deactivate);
        if (reminder.isActive()) {
            menuItemActivate.setVisible(false);
            menuItemDeactivate.setVisible(true);
        } else {
            menuItemActivate.setVisible(true);
            menuItemDeactivate.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.activity_moderator_reminder_detail_edit:
                Intent intent = new Intent(this, ReminderEditActivity.class);
                intent.putExtra("reminderId", reminder.getId());
                startActivity(intent);
                return true;
            case R.id.activity_moderator_reminder_detail_delete:
                if (Util.getInstance(this).isOnline()) {
                    // Show delete reminder dialog.
                    YesNoDialogFragment dialog = new YesNoDialogFragment();
                    Bundle args = new Bundle();
                    args.putString(YesNoDialogFragment.DIALOG_TITLE, getString(R.string
                            .reminder_delete_dialog_title));
                    args.putString(YesNoDialogFragment.DIALOG_TEXT, getString(R.string
                            .reminder_delete_dialog_text));
                    dialog.setArguments(args);
                    dialog.show(getSupportFragmentManager(), YesNoDialogFragment.DIALOG_REMINDER_DELETE);

                    errorMessage = getString(R.string.general_error_connection_failed);
                    errorMessage += " " + getString(R.string.general_error_delete);
                } else {
                    String message = getString(R.string.general_error_no_connection);
                    message += " " + getString(R.string.general_error_delete);
                    toast.setText(message);
                    toast.show();
                }
                return true;
            case R.id.activity_moderator_reminder_detail_activate:
                if (Util.getInstance(this).isOnline()) {
                    // Show activate reminder dialog.
                    YesNoDialogFragment dialog = new YesNoDialogFragment();
                    Bundle args = new Bundle();
                    args.putString(YesNoDialogFragment.DIALOG_TITLE, getString(R.string
                            .reminder_activate_dialog_title));
                    args.putString(YesNoDialogFragment.DIALOG_TEXT, getString(R.string
                            .reminder_activate_dialog_text));
                    dialog.setArguments(args);
                    dialog.show(getSupportFragmentManager(), YesNoDialogFragment.DIALOG_REMINDER_ACTIVATE);

                    errorMessage = getString(R.string.general_error_connection_failed);
                    errorMessage += " " + getString(R.string.general_error_delete);
                } else {
                    String message = getString(R.string.general_error_no_connection);
                    message += " " + getString(R.string.general_error_delete);
                    toast.setText(message);
                    toast.show();
                }
                return true;
            case R.id.activity_moderator_reminder_detail_deactivate:
                if (Util.getInstance(this).isOnline()) {
                    // Show deactivate reminder dialog.
                    YesNoDialogFragment dialog = new YesNoDialogFragment();
                    Bundle args = new Bundle();
                    args.putString(YesNoDialogFragment.DIALOG_TITLE, getString(R.string
                            .reminder_deactivate_dialog_title));
                    args.putString(YesNoDialogFragment.DIALOG_TEXT, getString(R.string
                            .reminder_deactivate_dialog_text));
                    dialog.setArguments(args);
                    dialog.show(getSupportFragmentManager(), YesNoDialogFragment.DIALOG_REMINDER_DEACTIVATE);

                    errorMessage = getString(R.string.general_error_connection_failed);
                    errorMessage += " " + getString(R.string.general_error_delete);
                } else {
                    String message = getString(R.string.general_error_no_connection);
                    message += " " + getString(R.string.general_error_delete);
                    toast.setText(message);
                    toast.show();
                }
                return true;
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

    private void initView() {
        lvReminderDetails = (ListView) findViewById(R.id.activity_reminder_detail_lv_reminder_details);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_reminder_detail_swipe_refresh_layout);
        TextView tvListEmpty = (TextView) findViewById(R.id.activity_reminder_detail_tv_list_empty);

        lvReminderDetails.setEmptyView(tvListEmpty);
        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshReminder();
            }
        });

        toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
        if (tv != null) tv.setGravity(Gravity.CENTER);

        setReminderDetails();
        listAdapter = new ResourceDetailListAdapter();
        listAdapter.setResourceDetails(resourceDetails);
        lvReminderDetails.setAdapter(listAdapter);
    }

    /**
     * Adds all existing reminder detail data to the details list. The details are added in a specific order.
     */
    private void setReminderDetails() {
        resourceDetails.clear();
        ResourceDetail title = new ResourceDetail(getString(R.string.general_title), reminder.getTitle(),
                R.drawable.ic_info_black_36dp);
        ResourceDetail text = new ResourceDetail(getString(R.string.message_text), reminder.getText(),
                R.drawable.ic_info_outline_black_36dp);
        ResourceDetail startDate = new ResourceDetail(getString(R.string.reminder_start_date),
                Util.getInstance(this).getFormattedDateOnly(reminder.getStartDate()), R.drawable.ic_today_black_36dp);
        ResourceDetail endDate = new ResourceDetail(getString(R.string.reminder_end_date),
                Util.getInstance(this).getFormattedDateOnly(reminder.getEndDate()), R.drawable.ic_event_black_36dp);
        ResourceDetail time = new ResourceDetail(getString(R.string.reminder_time),
                Util.getInstance(this).getFormattedTimeOnly(reminder.getStartDate()), R.drawable.ic_schedule_black_36dp);
        ResourceDetail nextDate = new ResourceDetail(getString(R.string.reminder_next_date_info),
                computeNextDate(), R.drawable.ic_date_range_black_36dp);

        String priorityName;
        if (reminder.getPriority().equals(Priority.HIGH)) {
            priorityName = getString(R.string.message_priority_high);
        } else {
            priorityName = getString(R.string.message_priority_normal);
        }
        ResourceDetail priority = new ResourceDetail(getString(R.string.message_priority), priorityName,
                R.drawable.ic_report_problem_black_36dp);

        String ignoreText;
        if (reminder.isIgnore()) {
            ignoreText = getString(R.string.general_yes);
        } else {
            ignoreText = getString(R.string.general_no);
        }
        ResourceDetail ignore = new ResourceDetail(getString(R.string.activity_reminder_add_ignore_flag),
                ignoreText, R.drawable.ic_notifications_off_black_36dp);

        // Reconstruct interval type.
        int i = reminder.getInterval();
        int intervalType;
        if (i == 0) {
            intervalType = 0;
        } else {
            // Set interval to days.
            i /= 86400;
            intervalType = 1;
            if (i % 7 == 0) {
                // Set interval to weeks.
                i /= 7;
                intervalType = 2;
            }
        }
        String intervalText;
        switch (intervalType) {
            case 1:
                if (i == 1) {
                    intervalText = getString(R.string.reminder_interval_one_day);
                } else {
                    intervalText = String.format(getString(R.string.reminder_interval_multiple_days), i);
                }
                break;
            case 2:
                if (i == 1) {
                    intervalText = getString(R.string.reminder_interval_one_week);
                } else {
                    intervalText = String.format(getString(R.string.reminder_interval_multiple_weeks), i);
                }
                break;
            default:
                intervalText = getString(R.string.reminder_interval_once_text);
                break;
        }
        ResourceDetail interval = new ResourceDetail(getString(R.string.reminder_interval), intervalText,
                R.drawable.ic_update_black_36dp);

        Moderator m = moderatorDBM.getModerator(reminder.getAuthorModerator());
        String moderatorName;
        if (m != null && m.getFirstName() != null && m.getLastName() != null) {
            moderatorName = m.getFirstName() + " " + m.getLastName();
        } else {
            moderatorName = getString(R.string.moderator_unknown);
        }
        ResourceDetail moderator = new ResourceDetail(getString(R.string.reminder_last_edited_by), moderatorName,
                R.drawable.ic_person_black_36dp);

        resourceDetails.add(title);
        resourceDetails.add(text);
        resourceDetails.add(priority);
        resourceDetails.add(startDate);
        resourceDetails.add(endDate);
        resourceDetails.add(time);
        resourceDetails.add(interval);
        resourceDetails.add(nextDate);
        resourceDetails.add(ignore);
        resourceDetails.add(moderator);
    }

    private String computeNextDate() {
        reminder.computeFirstNextDate();
        if (reminder.isIgnore()) {
            // If ignored, compute new next date.
            reminder.computeNextDate();
        }
        if (reminder.isExpired()) {
            return getString(R.string.reminder_expired);
        } else if (!reminder.isActive()) {
            return getString(R.string.reminder_deactivated);
        } else {
            return Util.getInstance(this).getFormattedDateLong(reminder.getNextDate());
        }
    }

    /**
     * Sends a request to the server to get all new channel data.
     */
    private void refreshReminder() {
        // Channel refresh is only possible if there is an internet connection.
        if (Util.getInstance(this).isOnline()) {
            errorMessage = getString(R.string.general_error_connection_failed);
            errorMessage += " " + getString(R.string.general_error_refresh);
            // Update reminder on swipe down.
            ChannelAPI.getInstance(this).getReminder(channelId, reminder.getId());
        } else {
            errorMessage = getString(R.string.general_error_no_connection);
            errorMessage += " " + getString(R.string.general_error_refresh);
            toast.setText(errorMessage);
            toast.show();
            // Can't refresh. Hide loading animation.
            swipeRefreshLayout.setRefreshing(false);
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
     * This method will be called when a reminder is posted to the EventBus.
     *
     * @param reminder The bus event containing a reminder object.
     */
    public void onEventMainThread(Reminder reminder) {
        Log.d(TAG, "BusEvent: " + reminder.toString());
        processReminderData(reminder);
    }

    /**
     * Updates the reminder in the database if it was updated on the server.
     *
     * @param reminder The reminder to process.
     */
    public void processReminderData(Reminder reminder) {
        // Update reminder in the database and reminder detail view if necessary.
        boolean hasChannelChanged = this.reminder.getModificationDate().isBefore(reminder.getModificationDate());
        if (hasChannelChanged) {
            channelDBM.updateReminder(reminder);
            this.reminder = reminder;
            setReminderDetails();
            listAdapter.setResourceDetails(resourceDetails);
            listAdapter.notifyDataSetChanged();
        }

        // Channels were refreshed. Hide loading animation.
        swipeRefreshLayout.setRefreshing(false);

        if (hasChannelChanged) {
            // If reminder data was updated show updated message.
            String message = getString(R.string.reminder_info_updated);
            toast.setText(message);
            toast.show();
        } else {
            // Otherwise show up to date message.
            String message = getString(R.string.reminder_info_up_to_date);
            toast.setText(message);
            toast.show();
        }
    }

    /**
     * This method will be called when a BusEvent object is posted to the EventBus. The action value determines of
     * which type the included object is.
     *
     * @param busEvent The busEvent which includes an object.
     */
    public void onEventMainThread(BusEvent busEvent) {
        Log.d(TAG, "EventBus: BusEvent");
        String action = busEvent.getAction();
        if (ChannelAPI.DELETE_REMINDER.equals(action)) {
            // Delete local reminder.
            channelDBM.deleteReminder(reminder.getId());
            finish();
        } else if (ChannelAPI.ACTIVATE_REMINDER.equals(action)) {
            Reminder r = (Reminder) busEvent.getObject();
            channelDBM.updateReminder(r);
            menuItemActivate.setVisible(false);
            menuItemDeactivate.setVisible(true);
            this.reminder = r;
            setReminderDetails();
            listAdapter.setResourceDetails(resourceDetails);
            listAdapter.notifyDataSetChanged();
        } else if (ChannelAPI.DEACTIVATE_REMINDER.equals(action)) {
            Reminder r = (Reminder) busEvent.getObject();
            channelDBM.updateReminder(r);
            menuItemActivate.setVisible(true);
            menuItemDeactivate.setVisible(false);
            this.reminder = r;
            setReminderDetails();
            listAdapter.setResourceDetails(resourceDetails);
            listAdapter.notifyDataSetChanged();
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
        // Hide loading animation on server response.
        swipeRefreshLayout.setRefreshing(false);

        // Show appropriate error message.
        switch (serverError.getErrorCode()) {
            case CONNECTION_FAILURE:
                toast.setText(errorMessage);
                toast.show();
                break;
            case REMINDER_NOT_FOUND:
                toast.setText(R.string.reminder_not_found);
                toast.show();
                channelDBM.deleteReminder(reminder.getId());
                navigateUp();
        }
    }

    @Override
    public void onDialogPositiveClick(String tag) {
        if (tag.equals(YesNoDialogFragment.DIALOG_REMINDER_DELETE)) {
            ChannelAPI.getInstance(this).deleteReminder(channelId, reminder.getId());
        } else if (tag.equals(YesNoDialogFragment.DIALOG_REMINDER_ACTIVATE)) {
            Reminder activate = new Reminder();
            activate.setChannelId(reminder.getChannelId());
            activate.setId(reminder.getId());
            activate.setActive(true);
            ChannelAPI.getInstance(this).changeReminder(activate);
        } else if (tag.equals(YesNoDialogFragment.DIALOG_REMINDER_DEACTIVATE)) {
            Reminder deactivate = new Reminder();
            deactivate.setChannelId(reminder.getChannelId());
            deactivate.setId(reminder.getId());
            deactivate.setActive(false);
            ChannelAPI.getInstance(this).changeReminder(deactivate);
        }
    }
}
