package ulm.university.news.app.controller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.BusEvent;
import ulm.university.news.app.api.ChannelAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.Event;
import ulm.university.news.app.data.Lecture;
import ulm.university.news.app.data.Sports;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.manager.database.UserDatabaseManager;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;

public class ChannelDetailActivity extends AppCompatActivity {
    /** This classes tag for logging. */
    private static final String TAG = "ChannelDetailActivity";

    private ListView lvChannelDetails;
    private ChannelDetailListAdapter listAdapter;

    private ChannelDatabaseManager channelDBM;

    private Channel channel;
    HashMap<String, String> channelData;

    private ImageView ivChannelIcon;
    private TextView tvChannelName;
    private Button btnSubscribe;
    private Button btnUnsubscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_detail);

        channelData = new HashMap<>();
        channelDBM = new ChannelDatabaseManager(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void initView() {
        lvChannelDetails = (ListView) findViewById(R.id.activity_channel_detail_lv_channel_details);
        tvChannelName = (TextView) findViewById(R.id.activity_channel_detail_tv_channel_name);
        ivChannelIcon = (ImageView) findViewById(R.id.activity_channel_detail_iv_channel_icon);
        btnSubscribe = (Button) findViewById(R.id.activity_channel_detail_btn_subscribe);
        btnUnsubscribe = (Button) findViewById(R.id.activity_channel_detail_btn_unsubscribe);

        btnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChannelAPI.getInstance(v.getContext()).subscribeChannel(channel.getId());
                // TODO Go to channel activity.
            }
        });

        btnUnsubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int localUserId = new UserDatabaseManager(v.getContext()).getLocalUser().getId();
                ChannelAPI.getInstance(v.getContext()).unsubscribeChannel(channel.getId(), localUserId);
                // Go to main activity.
                finish();
            }
        });

        listAdapter = new ChannelDetailListAdapter();
        lvChannelDetails.setAdapter(listAdapter);

        if (channelDBM.isSubscribedChannel(channel.getId())) {
            btnSubscribe.setVisibility(View.INVISIBLE);
            btnUnsubscribe.setVisibility(View.VISIBLE);
        } else {
            btnSubscribe.setVisibility(View.VISIBLE);
            btnUnsubscribe.setVisibility(View.GONE);
        }
        setChannelData();
        listAdapter.setChannelData(channelData);
        tvChannelName.setText(channel.getName());
        setChannelIcon();
    }

    /**
     * This method will be called when a Channel object is posted to the EventBus.
     *
     * @param channel The channel object.
     */
    public void onEvent(Channel channel) {
        Log.d(TAG, "EventBus: Channel");
        EventBus.getDefault().removeStickyEvent(channel);
        this.channel = channel;
        initView();
    }

    /**
     * This method will be called when a Lecture object (subclass of Channel) is posted to the EventBus.
     *
     * @param lecture The lecture object.
     */
    public void onEvent(Lecture lecture) {
        Log.d(TAG, "EventBus: Lecture");
        EventBus.getDefault().removeStickyEvent(lecture);
        this.channel = lecture;
        // Set lecture specific channel data.
        channelData.put(getString(R.string.lecture_faculty), lecture.getFaculty().toString());
        channelData.put(getString(R.string.lecture_lecturer), lecture.getLecturer());
        // Check nullable fields.
        if (lecture.getStartDate() != null) {
            channelData.put(getString(R.string.lecture_start_date), lecture.getStartDate());
        }
        if (lecture.getEndDate() != null) {
            channelData.put(getString(R.string.lecture_end_date), lecture.getEndDate());
        }
        if (lecture.getAssistant() != null) {
            channelData.put(getString(R.string.lecture_assistant), lecture.getAssistant());
        }
        // Initialise the view elements.
        initView();
    }

    /**
     * This method will be called when an Event object (subclass of Channel) is posted to the EventBus.
     *
     * @param event The event object.
     */
    public void onEvent(Event event) {
        Log.d(TAG, "EventBus: Event");
        EventBus.getDefault().removeStickyEvent(event);
        this.channel = event;
        // Set event specific channel data if not null.
        if (event.getCost() != null) {
            channelData.put(getString(R.string.event_cost), event.getCost());
        }
        if (event.getOrganizer() != null) {
            channelData.put(getString(R.string.event_organizer), event.getOrganizer());
        }
        // Initialise the view elements.
        initView();
    }

    /**
     * This method will be called when a Sports object (subclass of Channel) is posted to the EventBus.
     *
     * @param sports The sports object.
     */
    public void onEvent(Sports sports) {
        Log.d(TAG, "EventBus: Sports");
        EventBus.getDefault().removeStickyEvent(sports);
        this.channel = sports;
        // Set sports specific channel data if not null.
        if (sports.getCost() != null) {
            channelData.put(getString(R.string.sports_cost), sports.getCost());
        }
        if (sports.getNumberOfParticipants() != null) {
            channelData.put(getString(R.string.sports_participants), sports.getNumberOfParticipants());
        }
        // Initialise the view elements.
        initView();
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
        if (ChannelAPI.SUBSCRIBE_CHANNEL.equals(action)) {
            channelDBM.subscribeChannel(channel.getId());
            btnSubscribe.setVisibility(View.INVISIBLE);
            btnUnsubscribe.setVisibility(View.VISIBLE);
        } else if (ChannelAPI.UNSUBSCRIBE_CHANNEL.equals(action)) {
            channelDBM.unsubscribeChannel(channel.getId());
            btnSubscribe.setVisibility(View.VISIBLE);
            btnUnsubscribe.setVisibility(View.GONE);
        }
    }

    /**
     * Sets the appropriate channel icon.
     */
    private void setChannelIcon() {
        // Set appropriate channel icon.
        switch (channel.getType()) {
            case LECTURE:
                Lecture lecture = (Lecture) channel;
                // Set icon with appropriate faculty color.
                switch (lecture.getFaculty()) {
                    case ENGINEERING_COMPUTER_SCIENCE_PSYCHOLOGY:
                        ivChannelIcon.setImageResource(R.drawable.icon_channel_lecture_informatics);
                        break;
                    case MATHEMATICS_ECONOMICS:
                        ivChannelIcon.setImageResource(R.drawable.icon_channel_lecture_math);
                        break;
                    case MEDICINES:
                        ivChannelIcon.setImageResource(R.drawable.icon_channel_lecture_medicine);
                        break;
                    case NATURAL_SCIENCES:
                        ivChannelIcon.setImageResource(R.drawable.icon_channel_lecture_science);
                        break;
                }
                break;
            case EVENT:
                ivChannelIcon.setImageResource(R.drawable.icon_channel_event);
                break;
            case SPORTS:
                ivChannelIcon.setImageResource(R.drawable.icon_channel_sports);
                break;
            case STUDENT_GROUP:
                ivChannelIcon.setImageResource(R.drawable.icon_channel_student_group);
                break;
            case OTHER:
                ivChannelIcon.setImageResource(R.drawable.icon_channel_other);
                break;
        }
    }

    private void setChannelData() {
        channelData.put(getString(R.string.channel_type), channel.getType().toString());
        channelData.put(getString(R.string.channel_contacts), channel.getContacts());
        channelData.put(getString(R.string.channel_creation_date), channel.getCreationDate().toString());
        channelData.put(getString(R.string.channel_modification_date), channel.getModificationDate().toString());
        // Check nullable fields.
        if (channel.getLocations() != null) {
            channelData.put(getString(R.string.channel_locations), channel.getLocations());
        }
        if (channel.getDescription() != null) {
            channelData.put(getString(R.string.channel_description), channel.getDescription());
        }
        if (channel.getWebsite() != null) {
            channelData.put(getString(R.string.channel_website), channel.getWebsite());
        }
        if (channel.getTerm() != null) {
            channelData.put(getString(R.string.channel_term), channel.getTerm());
        }
        if (channel.getDates() != null) {
            channelData.put(getString(R.string.channel_dates), channel.getDates());
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
        switch (serverError.getErrorCode()) {
            case CONNECTION_FAILURE:
                break;
        }
    }
}
