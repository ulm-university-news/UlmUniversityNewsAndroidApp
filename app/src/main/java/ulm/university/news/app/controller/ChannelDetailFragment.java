package ulm.university.news.app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import ulm.university.news.app.data.ChannelDetail;
import ulm.university.news.app.data.Event;
import ulm.university.news.app.data.Lecture;
import ulm.university.news.app.data.Sports;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class ChannelDetailFragment extends Fragment implements DialogListener {
    /** This classes tag for logging. */
    private static final String TAG = "ChannelDetailFragment";

    private ChannelDatabaseManager channelDBM;
    private Channel channel;
    List<ChannelDetail> channelDetails;

    private ListView lvChannelDetails;
    private Button btnSubscribe;
    private Button btnUnsubscribe;
    private String errorMessage;
    private Toast toast;

    public ChannelDetailFragment() {
    }

    public static ChannelDetailFragment newInstance(int channelId) {
        ChannelDetailFragment fragment = new ChannelDetailFragment();
        Bundle args = new Bundle();
        args.putInt("channelId", channelId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int channelId = getArguments().getInt("channelId");
        channel = new ChannelDatabaseManager(getActivity()).getChannel(channelId);
        channelDBM = new ChannelDatabaseManager(getActivity());
        channelDetails = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_channel_detail, container, false);
        initView(v);
        initChannel();
        return v;
    }

    private void initView(View v) {
        lvChannelDetails = (ListView) v.findViewById(R.id.fragment_channel_detail_lv_channel_details);
        btnSubscribe = (Button) v.findViewById(R.id.fragment_channel_detail_btn_subscribe);
        btnUnsubscribe = (Button) v.findViewById(R.id.fragment_channel_detail_btn_unsubscribe);
        toast = Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT);
        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
        if (tv != null) tv.setGravity(Gravity.CENTER);
    }

    private void initChannel() {
        btnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.isOnline(v.getContext())) {
                    ChannelAPI.getInstance(v.getContext()).subscribeChannel(channel.getId());
                    errorMessage = getString(R.string.general_error_connection_failed);
                    errorMessage += getString(R.string.general_error_subscribe);
                } else {
                    String message = getString(R.string.general_error_no_connection);
                    message += getString(R.string.general_error_subscribe);
                    toast.setText(message);
                    toast.show();
                }
            }
        });

        btnUnsubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.isOnline(v.getContext())) {
                    UnsubscribeDialogFragment dialog = new UnsubscribeDialogFragment();
                    dialog.setTargetFragment(ChannelDetailFragment.this, 0);
                    dialog.show(getFragmentManager(), "unsubscribeDialog");
                    errorMessage = getString(R.string.general_error_connection_failed);
                    errorMessage += getString(R.string.general_error_unsubscribe);
                } else {
                    String message = getString(R.string.general_error_no_connection);
                    message += getString(R.string.general_error_unsubscribe);
                    toast.setText(message);
                    toast.show();
                }
            }
        });

        if (channelDBM.isSubscribedChannel(channel.getId())) {
            btnSubscribe.setVisibility(View.GONE);
            btnUnsubscribe.setVisibility(View.VISIBLE);
        } else {
            btnSubscribe.setVisibility(View.VISIBLE);
            btnUnsubscribe.setVisibility(View.GONE);
        }

        setChannelDetails();
        ChannelDetailListAdapter listAdapter = new ChannelDetailListAdapter();
        listAdapter.setChannelDetails(channelDetails);
        lvChannelDetails.setAdapter(listAdapter);
    }

    /**
     * Adds all existing channel detail data to the details list. The details are adde in a specific order.
     */
    private void setChannelDetails() {
        ChannelDetail name = new ChannelDetail(getString(R.string.channel_name), channel.getName(),
                R.drawable.ic_info_black_36dp);
        String typeName;
        switch (channel.getType()) {
            case LECTURE:
                typeName = getString(R.string.channel_type_lecture);
                break;
            case EVENT:
                typeName = getString(R.string.channel_type_event);
                break;
            case SPORTS:
                typeName = getString(R.string.channel_type_sports);
                break;
            case STUDENT_GROUP:
                typeName = getString(R.string.channel_type_student_group);
                break;
            default:
                typeName = getString(R.string.channel_type_other);

        }
        ChannelDetail type = new ChannelDetail(getString(R.string.channel_type), typeName,
                R.drawable.ic_details_black_36dp);
        ChannelDetail term = new ChannelDetail(getString(R.string.channel_term), channel.getTerm(),
                R.drawable.ic_date_range_black_36dp);
        channelDetails.add(name);
        channelDetails.add(type);
        channelDetails.add(term);

        // Check nullable fields.
        if (channel.getDescription() != null) {
            ChannelDetail description = new ChannelDetail(getString(R.string.channel_description), channel.getDescription(),
                    R.drawable.ic_info_outline_black_36dp);
            channelDetails.add(description);
        }

        // Check type specific fields.
        switch (channel.getType()) {
            case LECTURE:
                Lecture lecture = (Lecture) channel;
                String facultyName;
                switch (lecture.getFaculty()) {
                    case ENGINEERING_COMPUTER_SCIENCE_PSYCHOLOGY:
                        facultyName = getString(R.string.lecture_faculty_informatics);
                        break;
                    case MEDICINES:
                        facultyName = getString(R.string.lecture_faculty_medicines);
                        break;
                    case NATURAL_SCIENCES:
                        facultyName = getString(R.string.lecture_faculty_sciences);
                        break;
                    default:
                        facultyName = getString(R.string.lecture_faculty_mathematics);
                }
                ChannelDetail faculty = new ChannelDetail(getString(R.string.lecture_faculty), facultyName,
                        R.drawable.ic_school_black_36dp);
                ChannelDetail lecturer = new ChannelDetail(getString(R.string.lecture_lecturer),
                        lecture.getLecturer(), R.drawable.ic_person_black_36dp);
                channelDetails.add(faculty);
                channelDetails.add(lecturer);

                // Check nullable fields.
                if (lecture.getAssistant() != null) {
                    ChannelDetail assistant = new ChannelDetail(getString(R.string.lecture_assistant),
                            lecture.getAssistant(), R.drawable.ic_person_outline_black_36dp);
                    channelDetails.add(assistant);
                }
                if (lecture.getStartDate() != null) {
                    ChannelDetail startDate = new ChannelDetail(getString(R.string.lecture_start_date),
                            lecture.getStartDate(), R.drawable.ic_today_black_36dp);
                    channelDetails.add(startDate);
                }
                if (lecture.getEndDate() != null) {
                    ChannelDetail endDate = new ChannelDetail(getString(R.string.lecture_end_date),
                            lecture.getEndDate(), R.drawable.ic_event_black_36dp);
                    channelDetails.add(endDate);
                }
                break;
            case EVENT:
                Event event = (Event) channel;
                // Check nullable fields.
                if (event.getCost() != null) {
                    ChannelDetail cost = new ChannelDetail(getString(R.string.event_cost),
                            event.getCost(), R.drawable.ic_attach_money_black_36dp);
                    channelDetails.add(cost);
                }
                if (event.getOrganizer() != null) {
                    ChannelDetail organizer = new ChannelDetail(getString(R.string.event_organizer),
                            event.getOrganizer(), R.drawable.ic_person_black_36dp);
                    channelDetails.add(organizer);
                }
                break;
            case SPORTS:
                Sports sports = (Sports) channel;
                // Check nullable fields.
                if (sports.getCost() != null) {
                    ChannelDetail cost = new ChannelDetail(getString(R.string.sports_cost),
                            sports.getCost(), R.drawable.ic_attach_money_black_36dp);
                    channelDetails.add(cost);
                }
                if (sports.getNumberOfParticipants() != null) {
                    ChannelDetail participants = new ChannelDetail(getString(R.string.sports_participants),
                            sports.getNumberOfParticipants(), R.drawable.ic_group_black_36dp);
                    channelDetails.add(participants);
                }
                break;
        }

        // Check nullable fields.
        if (channel.getDates() != null) {
            ChannelDetail dates = new ChannelDetail(getString(R.string.channel_dates), channel.getDates(),
                    R.drawable.ic_schedule_black_36dp);
            channelDetails.add(dates);
        }
        if (channel.getLocations() != null) {
            ChannelDetail locations = new ChannelDetail(getString(R.string.channel_locations), channel.getLocations(),
                    R.drawable.ic_room_black_36dp);
            channelDetails.add(locations);
        }
        if (channel.getWebsite() != null) {
            ChannelDetail website = new ChannelDetail(getString(R.string.channel_website), channel.getWebsite(),
                    R.drawable.ic_public_black_36dp);
            channelDetails.add(website);
        }

        ChannelDetail contacts = new ChannelDetail(getString(R.string.channel_contacts), channel.getContacts(),
                R.drawable.ic_account_circle_black_36dp);
        ChannelDetail creationDate = new ChannelDetail(getString(R.string.channel_creation_date),
                ChannelController.getFormattedDateLong(channel.getCreationDate()), R.drawable.ic_today_black_36dp);
        ChannelDetail modificationDate = new ChannelDetail(getString(R.string.channel_modification_date),
                ChannelController.getFormattedDateLong(channel.getModificationDate()), R.drawable.ic_event_black_36dp);
        channelDetails.add(contacts);
        channelDetails.add(creationDate);
        channelDetails.add(modificationDate);
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
            // btnSubscribe.setVisibility(View.GONE);
            // btnUnsubscribe.setVisibility(View.VISIBLE);
            Intent intent = new Intent(getActivity(), ChannelActivity.class);
            intent.putExtra("channelId", channel.getId());
            startActivity(intent);
            getActivity().finish();
        } else if (ChannelAPI.UNSUBSCRIBE_CHANNEL.equals(action)) {
            channelDBM.unsubscribeChannel(channel.getId());
            // btnSubscribe.setVisibility(View.VISIBLE);
            // btnUnsubscribe.setVisibility(View.GONE);
            getActivity().finish();
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
                toast.setText(errorMessage);
                toast.show();
                break;
        }
    }

    @Override
    public void onDialogPositiveClick() {
        ChannelAPI.getInstance(getContext()).unsubscribeChannel(channel.getId());
    }
}
