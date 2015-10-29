package ulm.university.news.app.controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ulm.university.news.app.R;


public class ChannelFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    public static ChannelFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        ChannelFragment fragment = new ChannelFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_channel, container, false);
        TextView textView = (TextView) view.findViewById(R.id.fragment_channel_tv_info);
        textView.setText("Fragment #" + mPage);
        return view;
    }

}
