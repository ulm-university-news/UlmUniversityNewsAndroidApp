package ulm.university.news.app.controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;


public class ChannelFragment extends Fragment {
    /** This classes tag for logging. */
    private static final String TAG = "ChannelFragment";

    private AdapterView.OnItemClickListener itemClickListener;

    ChannelListAdapter listAdapter;

    private ListView lvChannels;
    private  TextView tvInfo;

    public static ChannelFragment newInstance() {
        return new ChannelFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ChannelDatabaseManager channelDBM = new ChannelDatabaseManager(getActivity());
        List<Channel> channels = channelDBM.getChannels();

        listAdapter = new ChannelListAdapter(getActivity(), R.layout.channel_list_item, channels);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_channel, container, false);
        lvChannels = (ListView) view.findViewById(R.id.fragment_channel_lv_channels);
        tvInfo = (TextView) view.findViewById(R.id.fragment_channel_tv_info);
        tvInfo.setText("My channels");

        itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Channel channel = (Channel) lvChannels.getItemAtPosition(position);
                Log.d(TAG, "++++++++++++++ Type: " + channel.getType() + ", Name: " + channel.getName());
            }
        };

        lvChannels.setAdapter(listAdapter);
        lvChannels.setOnItemClickListener(itemClickListener);

        return view;
    }
}
