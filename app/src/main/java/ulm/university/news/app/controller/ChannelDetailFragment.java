package ulm.university.news.app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import ulm.university.news.app.data.Event;
import ulm.university.news.app.data.Lecture;
import ulm.university.news.app.data.ResourceDetail;
import ulm.university.news.app.data.Sports;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CHANNEL_NOT_FOUND;
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
    private List<ResourceDetail> resourceDetails;
    ResourceDetailListAdapter listAdapter;

    private ListView lvChannelDetails;
    private Button btnSubscribe;
    private Button btnUnsubscribe;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar pgrSending;

    private String errorMessage;
    private Toast toast;
    private int channelId;

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
        channelId = getArguments().getInt("channelId");
        channel = new ChannelDatabaseManager(getActivity()).getChannel(channelId);
        channelDBM = new ChannelDatabaseManager(getActivity());
        resourceDetails = new ArrayList<>();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update channel in case it was edited.
        channel = channelDBM.getChannel(channelId);
        setChannelDetails();
        listAdapter.setResourceDetails(resourceDetails);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_channel_detail, container, false);
        initView(v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof ModeratorChannelActivity) {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_moderator_channel_detail_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.activity_moderator_channel_details_edit:
                Intent intent = new Intent(getActivity(), ChannelEditActivity.class);
                intent.putExtra("channelId", channelId);
                startActivity(intent);
                return true;
            case R.id.activity_moderator_channel_channel_delete:
                if (Util.getInstance(getContext()).isOnline()) {
                    // Show delete channel dialog.
                    YesNoDialogFragment dialog = new YesNoDialogFragment();
                    Bundle args = new Bundle();
                    args.putString(YesNoDialogFragment.DIALOG_TITLE, getString(R.string
                            .channel_delete_dialog_title));
                    args.putString(YesNoDialogFragment.DIALOG_TEXT, getString(R.string
                            .channel_delete_dialog_text));
                    dialog.setArguments(args);
                    dialog.setTargetFragment(ChannelDetailFragment.this, 0);
                    dialog.show(getFragmentManager(), YesNoDialogFragment.DIALOG_CHANNEL_DELETE);

                    errorMessage = getString(R.string.general_error_connection_failed);
                    errorMessage += " " + getString(R.string.general_error_delete);
                } else {
                    String message = getString(R.string.general_error_no_connection);
                    message += " " + getString(R.string.general_error_delete);
                    toast.setText(message);
                    toast.show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initView(View v) {
        lvChannelDetails = (ListView) v.findViewById(R.id.fragment_channel_detail_lv_channel_details);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.fragment_channel_detail_swipe_refresh_layout);
        TextView tvListEmpty = (TextView) v.findViewById(R.id.fragment_channel_detail_tv_list_empty);
        btnSubscribe = (Button) v.findViewById(R.id.fragment_channel_detail_btn_subscribe);
        btnUnsubscribe = (Button) v.findViewById(R.id.fragment_channel_detail_btn_unsubscribe);
        pgrSending = (ProgressBar) v.findViewById(R.id.fragment_channel_detail_pgr_sending);

        lvChannelDetails.setEmptyView(tvListEmpty);
        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshChannel();
            }
        });

        toast = Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT);
        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
        if (tv != null) tv.setGravity(Gravity.CENTER);

        btnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.getInstance(v.getContext()).isOnline()) {
                    ChannelAPI.getInstance(v.getContext()).subscribeChannel(channel.getId());
                    btnSubscribe.setVisibility(View.GONE);
                    pgrSending.setVisibility(View.VISIBLE);
                    errorMessage = getString(R.string.general_error_connection_failed);
                    errorMessage += " " + getString(R.string.general_error_subscribe);
                } else {
                    String message = getString(R.string.general_error_no_connection);
                    message += " " + getString(R.string.general_error_subscribe);
                    toast.setText(message);
                    toast.show();
                }
            }
        });

        btnUnsubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.getInstance(v.getContext()).isOnline()) {
                    // Show unsubscribe channel dialog.
                    YesNoDialogFragment dialog = new YesNoDialogFragment();
                    Bundle args = new Bundle();
                    args.putString(YesNoDialogFragment.DIALOG_TITLE, getString(R.string
                            .channel_unsubscribe_dialog_title));
                    args.putString(YesNoDialogFragment.DIALOG_TEXT, getString(R.string
                            .channel_unsubscribe_dialog_text));
                    dialog.setArguments(args);
                    dialog.setTargetFragment(ChannelDetailFragment.this, 0);
                    dialog.show(getFragmentManager(), YesNoDialogFragment.DIALOG_CHANNEL_UNSUBSCRIBE);

                    errorMessage = getString(R.string.general_error_connection_failed);
                    errorMessage += " " + getString(R.string.general_error_unsubscribe);
                } else {
                    String message = getString(R.string.general_error_no_connection);
                    message += " " + getString(R.string.general_error_unsubscribe);
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

        // Hide subscribe and unsubscribe buttons in moderator view.
        if (getActivity() instanceof ModeratorChannelActivity) {
            btnSubscribe.setVisibility(View.GONE);
            btnUnsubscribe.setVisibility(View.GONE);
        }

        setChannelDetails();
        listAdapter = new ResourceDetailListAdapter();
        listAdapter.setResourceDetails(resourceDetails);
        lvChannelDetails.setAdapter(listAdapter);
    }

    /**
     * Adds all existing channel detail data to the details list. The details are added in a specific order.
     */
    private void setChannelDetails() {
        resourceDetails.clear();
        ResourceDetail name = new ResourceDetail(getString(R.string.channel_name), channel.getName(),
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
        ResourceDetail type = new ResourceDetail(getString(R.string.channel_type), typeName,
                R.drawable.ic_details_black_36dp);
        ResourceDetail term = new ResourceDetail(getString(R.string.channel_term), channel.getTerm(),
                R.drawable.ic_date_range_black_36dp);
        resourceDetails.add(name);
        resourceDetails.add(type);
        resourceDetails.add(term);

        // Check nullable fields.
        if (channel.getDescription() != null && !channel.getDescription().isEmpty()) {
            ResourceDetail description = new ResourceDetail(getString(R.string.channel_description), channel
                    .getDescription(), R.drawable.ic_info_outline_black_36dp);
            resourceDetails.add(description);
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
                ResourceDetail faculty = new ResourceDetail(getString(R.string.lecture_faculty), facultyName,
                        R.drawable.ic_school_black_36dp);
                ResourceDetail lecturer = new ResourceDetail(getString(R.string.lecture_lecturer),
                        lecture.getLecturer(), R.drawable.ic_person_black_36dp);
                resourceDetails.add(faculty);
                resourceDetails.add(lecturer);

                // Check nullable fields.
                if (lecture.getAssistant() != null && !lecture.getAssistant().isEmpty()) {
                    ResourceDetail assistant = new ResourceDetail(getString(R.string.lecture_assistant),
                            lecture.getAssistant(), R.drawable.ic_person_outline_black_36dp);
                    resourceDetails.add(assistant);
                }
                if (lecture.getStartDate() != null && !lecture.getStartDate().isEmpty()) {
                    ResourceDetail startDate = new ResourceDetail(getString(R.string.lecture_start_date),
                            lecture.getStartDate(), R.drawable.ic_today_black_36dp);
                    resourceDetails.add(startDate);
                }
                if (lecture.getEndDate() != null && !lecture.getEndDate().isEmpty()) {
                    ResourceDetail endDate = new ResourceDetail(getString(R.string.lecture_end_date),
                            lecture.getEndDate(), R.drawable.ic_event_black_36dp);
                    resourceDetails.add(endDate);
                }
                break;
            case EVENT:
                Event event = (Event) channel;
                // Check nullable fields.
                if (event.getCost() != null && !event.getCost().isEmpty()) {
                    ResourceDetail cost = new ResourceDetail(getString(R.string.event_cost),
                            event.getCost(), R.drawable.ic_attach_money_black_36dp);
                    resourceDetails.add(cost);
                }
                if (event.getOrganizer() != null && !event.getOrganizer().isEmpty()) {
                    ResourceDetail organizer = new ResourceDetail(getString(R.string.event_organizer),
                            event.getOrganizer(), R.drawable.ic_person_black_36dp);
                    resourceDetails.add(organizer);
                }
                break;
            case SPORTS:
                Sports sports = (Sports) channel;
                // Check nullable fields.
                if (sports.getCost() != null && !sports.getCost().isEmpty()) {
                    ResourceDetail cost = new ResourceDetail(getString(R.string.sports_cost),
                            sports.getCost(), R.drawable.ic_attach_money_black_36dp);
                    resourceDetails.add(cost);
                }
                if (sports.getNumberOfParticipants() != null && !sports.getNumberOfParticipants().isEmpty()) {
                    ResourceDetail participants = new ResourceDetail(getString(R.string.sports_participants),
                            sports.getNumberOfParticipants(), R.drawable.ic_group_black_36dp);
                    resourceDetails.add(participants);
                }
                break;
        }

        // Check nullable fields.
        if (channel.getDates() != null && !channel.getDates().isEmpty()) {
            ResourceDetail dates = new ResourceDetail(getString(R.string.channel_dates), channel.getDates(),
                    R.drawable.ic_schedule_black_36dp);
            resourceDetails.add(dates);
        }
        if (channel.getLocations() != null && !channel.getLocations().isEmpty()) {
            ResourceDetail locations = new ResourceDetail(getString(R.string.channel_locations), channel.getLocations(),
                    R.drawable.ic_room_black_36dp);
            resourceDetails.add(locations);
        }
        if (channel.getWebsite() != null && !channel.getWebsite().isEmpty()) {
            ResourceDetail website = new ResourceDetail(getString(R.string.channel_website), channel.getWebsite(),
                    R.drawable.ic_public_black_36dp);
            resourceDetails.add(website);
        }

        ResourceDetail contacts = new ResourceDetail(getString(R.string.channel_contacts), channel.getContacts(),
                R.drawable.ic_account_circle_black_36dp);
        ResourceDetail creationDate = new ResourceDetail(getString(R.string.channel_creation_date),
                Util.getInstance(getContext()).getFormattedDateLong(channel.getCreationDate()), R.drawable
                .ic_today_black_36dp);
        ResourceDetail modificationDate = new ResourceDetail(getString(R.string.channel_modification_date),
                Util.getInstance(getContext()).getFormattedDateLong(channel.getModificationDate()), R.drawable
                .ic_event_black_36dp);
        resourceDetails.add(contacts);
        resourceDetails.add(creationDate);
        resourceDetails.add(modificationDate);
    }

    /**
     * Sends a request to the server to get all new channel data.
     */
    private void refreshChannel() {
        // Channel refresh is only possible if there is an internet connection.
        if (Util.getInstance(getContext()).isOnline()) {
            errorMessage = getString(R.string.general_error_connection_failed);
            errorMessage += " " + getString(R.string.general_error_refresh);
            // Update channel on swipe down.
            ChannelAPI.getInstance(getContext()).getChannel(channel.getId());
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
     * This method will be called when a channel is posted to the EventBus.
     *
     * @param channel The bus event containing a channel object.
     */
    public void onEventMainThread(Channel channel) {
        Log.d(TAG, "BusEvent: " + channel.toString());
        processChannelData(channel);
    }

    /**
     * Updates the channel in the database if it was updated on the server.
     *
     * @param channel The channel to process.
     */
    public void processChannelData(Channel channel) {
        // Update channel in the database and channel detail view if necessary.
        boolean hasChannelChanged = this.channel.getModificationDate().isBefore(channel.getModificationDate());
        if (hasChannelChanged) {
            channelDBM.updateChannel(channel);
            this.channel = channel;
            setChannelDetails();
            listAdapter.setResourceDetails(resourceDetails);
            listAdapter.notifyDataSetChanged();
        }

        // Channels were refreshed. Hide loading animation.
        swipeRefreshLayout.setRefreshing(false);

        if (hasChannelChanged) {
            // If channel data was updated show updated message.
            String message = getString(R.string.channel_info_updated);
            toast.setText(message);
            toast.show();
        } else {
            // Otherwise show up to date message.
            String message = getString(R.string.channel_info_up_to_date);
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
        if (ChannelAPI.SUBSCRIBE_CHANNEL.equals(action)) {
            channelDBM.subscribeChannel(channel.getId());
            ChannelAPI.getInstance(getContext()).getResponsibleModerators(channel.getId());
            Intent intent = new Intent(getActivity(), ChannelActivity.class);
            intent.putExtra("channelId", channel.getId());
            startActivity(intent);
            getActivity().finish();
        } else if (ChannelAPI.UNSUBSCRIBE_CHANNEL.equals(action)) {
            channelDBM.unsubscribeChannel(channel.getId());
            // TODO Delete announcements of channel!
            // channelDBM.deleteAnnouncements(channel.getId());
            // Delete unsubscribed channel if it was marked as deleted.
            if (channel.isDeleted()) {
                ChannelController.deleteChannel(getContext(), channel.getId());
            }
            getActivity().finish();
        } else if (ChannelAPI.DELETE_CHANNEL.equals(action)) {
            // Mark local channel as deleted.
            ChannelController.deleteChannel(getContext(), channel.getId());
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
        // Hide loading animation on server response.
        swipeRefreshLayout.setRefreshing(false);
        boolean isSubscribed;
        if (channelDBM.isSubscribedChannel(channel.getId())) {
            btnSubscribe.setVisibility(View.GONE);
            btnUnsubscribe.setVisibility(View.VISIBLE);
            isSubscribed = true;
        } else {
            btnSubscribe.setVisibility(View.VISIBLE);
            btnUnsubscribe.setVisibility(View.GONE);
            isSubscribed = false;
        }
        pgrSending.setVisibility(View.GONE);
        // Show appropriate error message.
        switch (serverError.getErrorCode()) {
            case CONNECTION_FAILURE:
                toast.setText(errorMessage);
                toast.show();
                break;
            case CHANNEL_NOT_FOUND:
                // If channel is subscribed, do nothing.
                if (!isSubscribed) {
                    // Channel was deleted on the server, so delete it on the local database too.
                    channelDBM.deleteChannel(channelId);
                    // Show channel deleted dialog.
                    InfoDialogFragment dialog = new InfoDialogFragment();
                    Bundle args = new Bundle();
                    args.putString(InfoDialogFragment.DIALOG_TITLE, getString(R.string.channel_deleted_dialog_title));
                    String text = String.format(getString(R.string.channel_deleted_dialog_text), channel.getName());
                    args.putString(InfoDialogFragment.DIALOG_TEXT, text);
                    dialog.setArguments(args);
                    dialog.show(getActivity().getSupportFragmentManager(), InfoDialogFragment
                            .DIALOG_SUBSCRIBE_DELETED_CHANNEL);
                }
                break;
        }
    }

    @Override
    public void onDialogPositiveClick(String tag) {
        if (tag.equals(YesNoDialogFragment.DIALOG_CHANNEL_UNSUBSCRIBE)) {
            ChannelAPI.getInstance(getContext()).unsubscribeChannel(channel.getId());
            btnUnsubscribe.setVisibility(View.GONE);
            pgrSending.setVisibility(View.VISIBLE);
        } else if (tag.equals(YesNoDialogFragment.DIALOG_CHANNEL_DELETE)) {
            ChannelAPI.getInstance(getContext()).deleteChannel(channel.getId());
            pgrSending.setVisibility(View.VISIBLE);
        }
    }
}
