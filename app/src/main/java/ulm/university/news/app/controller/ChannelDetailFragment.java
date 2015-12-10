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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.BusEvent;
import ulm.university.news.app.api.ChannelAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.Lecture;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class ChannelDetailFragment extends Fragment {
    /** This classes tag for logging. */
    private static final String TAG = "ChannelDetailFragment";

    private ListView lvChannelDetails;
    private ChannelDetailListAdapter listAdapter;

    private ChannelDatabaseManager channelDBM;

    private Channel channel;
    HashMap<String, String> channelData;

    private ImageView ivChannelIcon;
    private TextView tvChannelName;
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
        channelData = new HashMap<>();
        channelDBM = new ChannelDatabaseManager(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_channel_detail, container, false);
        initView(v);
        initChannel();
        return v;
    }

    private void initView(View v) {
        lvChannelDetails = (ListView) v.findViewById(R.id.activity_channel_detail_lv_channel_details);
        tvChannelName = (TextView) v.findViewById(R.id.activity_channel_detail_tv_channel_name);
        ivChannelIcon = (ImageView) v.findViewById(R.id.activity_channel_detail_iv_channel_icon);
        btnSubscribe = (Button) v.findViewById(R.id.activity_channel_detail_btn_subscribe);
        btnUnsubscribe = (Button) v.findViewById(R.id.activity_channel_detail_btn_unsubscribe);

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
                    ChannelAPI.getInstance(v.getContext()).unsubscribeChannel(channel.getId());
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
            btnSubscribe.setVisibility(View.INVISIBLE);
            btnUnsubscribe.setVisibility(View.VISIBLE);
        } else {
            btnSubscribe.setVisibility(View.VISIBLE);
            btnUnsubscribe.setVisibility(View.INVISIBLE);
        }
        setChannelData();
        tvChannelName.setText(channel.getName());
        setChannelIcon();

        listAdapter = new ChannelDetailListAdapter();
        listAdapter.setChannelData(channelData);
        lvChannelDetails.setAdapter(listAdapter);
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
            btnSubscribe.setVisibility(View.INVISIBLE);
            btnUnsubscribe.setVisibility(View.VISIBLE);
            Intent intent = new Intent(getActivity(), ChannelActivity.class);
            intent.putExtra("channelId", channel.getId());
            startActivity(intent);
            getActivity().finish();
        } else if (ChannelAPI.UNSUBSCRIBE_CHANNEL.equals(action)) {
            channelDBM.unsubscribeChannel(channel.getId());
            btnSubscribe.setVisibility(View.VISIBLE);
            btnUnsubscribe.setVisibility(View.INVISIBLE);
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
}
