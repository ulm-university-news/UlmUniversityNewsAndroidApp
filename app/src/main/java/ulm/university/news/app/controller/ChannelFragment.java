package ulm.university.news.app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;


public class ChannelFragment extends Fragment {
    /** This classes tag for logging. */
    private static final String TAG = "ChannelFragment";

    private AdapterView.OnItemClickListener itemClickListener;

    ChannelListAdapter listAdapter;
    List<Channel> channels = null;
    ChannelDatabaseManager channelDBM;

    private ListView lvChannels;
    private  TextView tvInfo;

    public static ChannelFragment newInstance() {
        return new ChannelFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        channelDBM = new ChannelDatabaseManager(getActivity());
        channels = channelDBM.getSubscribedChannels();
        listAdapter = new ChannelListAdapter(getActivity(), R.layout.channel_list_item, channels);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_channel, container, false);
        lvChannels = (ListView) view.findViewById(R.id.fragment_channel_lv_channels);
        tvInfo = (TextView) view.findViewById(R.id.fragment_channel_tv_info);

        itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Channel channel = (Channel) lvChannels.getItemAtPosition(position);
                Intent intent = new Intent(arg0.getContext(), ChannelDetailActivity.class);
                EventBus.getDefault().postSticky(channel);
                startActivity(intent);
            }
        };

        lvChannels.setAdapter(listAdapter);
        lvChannels.setOnItemClickListener(itemClickListener);

        if(channels != null && !channels.isEmpty()){
            tvInfo.setVisibility(View.GONE);
            lvChannels.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        channels.clear();
        channels.addAll(channelDBM.getSubscribedChannels());
        listAdapter.notifyDataSetChanged();
        if(channels.isEmpty()){
            lvChannels.setVisibility(View.GONE);
            tvInfo.setVisibility(View.VISIBLE);
        } else {
            lvChannels.setVisibility(View.VISIBLE);
            tvInfo.setVisibility(View.GONE);
        }
    }
}
