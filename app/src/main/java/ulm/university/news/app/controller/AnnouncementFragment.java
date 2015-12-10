package ulm.university.news.app.controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ulm.university.news.app.R;

public class AnnouncementFragment extends Fragment {

    public AnnouncementFragment() {
        // Required empty public constructor
    }

    public static AnnouncementFragment newInstance() {
        return new AnnouncementFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_announcement, container, false);
    }
}
