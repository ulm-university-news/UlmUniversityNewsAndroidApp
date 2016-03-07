package ulm.university.news.app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import ulm.university.news.app.R;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class GroupFragment extends Fragment {
    /** This classes tag for logging. */
    private static final String TAG = "GroupFragment";

    public static GroupFragment newInstance() {
        return new GroupFragment();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GroupFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_main_group_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.activity_main_group_menu_search:
                startActivity(new Intent(getActivity(), GroupSearchActivity.class));
                return true;
            case R.id.activity_main_group_menu_settings:
                startActivity(new Intent(getActivity(), SettingsUserActivity.class));
                return true;
            case R.id.activity_main_group_menu_add:
                startActivity(new Intent(getActivity(), GroupAddActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
