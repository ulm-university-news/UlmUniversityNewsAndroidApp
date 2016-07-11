package ulm.university.news.app.controller;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Group;
import ulm.university.news.app.manager.database.DatabaseLoader;
import ulm.university.news.app.manager.database.GroupDatabaseManager;


public class GroupFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Group>> {
    /** This classes tag for logging. */
    private static final String TAG = "GroupFragment";

    /** The loader's id. */
    private static final int LOADER_ID = 5;

    private AdapterView.OnItemClickListener itemClickListener;
    private DatabaseLoader<List<Group>> databaseLoader;

    private GroupListAdapter listAdapter;
    private List<Group> groups;
    private ListView lvGroups;

    public static GroupFragment newInstance() {
        return new GroupFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update channel list to make changes like read messages visible.
        if (databaseLoader != null) {
            databaseLoader.onContentChanged();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize or reuse an existing database loader.
        databaseLoader = (DatabaseLoader) getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        databaseLoader.onContentChanged();

        listAdapter = new GroupListAdapter(getActivity(), R.layout.group_list_item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        lvGroups = (ListView) view.findViewById(R.id.fragment_group_lv_groups);
        TextView tvListEmpty = (TextView) view.findViewById(R.id.fragment_group_tv_list_empty);

        itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Group group = (Group) lvGroups.getItemAtPosition(position);
                Intent intent = new Intent(arg0.getContext(), GroupActivity.class);
                intent.putExtra("groupId", group.getId());
                startActivity(intent);
            }
        };

        lvGroups.setAdapter(listAdapter);
        lvGroups.setOnItemClickListener(itemClickListener);
        lvGroups.setEmptyView(tvListEmpty);

        return view;
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
            case R.id.activity_main_group_menu_add:
                startActivity(new Intent(getActivity(), GroupAddActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<List<Group>> onCreateLoader(int id, Bundle args) {
        databaseLoader = new DatabaseLoader<>(getActivity(), new DatabaseLoader
                .DatabaseLoaderCallbacks<List<Group>>() {
            @Override
            public List<Group> onLoadInBackground() {
                // Load all subscribed groups with announcement data.
                return databaseLoader.getGroupDBM().getMyGroups();
            }

            @Override
            public IntentFilter observerFilter() {
                // Listen to database changes on channel subscriptions and new announcements.
                IntentFilter filter = new IntentFilter();
                filter.addAction(GroupDatabaseManager.JOIN_GROUP);
                filter.addAction(GroupDatabaseManager.REMOVE_USER_FROM_GROUP);
                return filter;
            }
        });
        // This loader uses the channel database manager to load data.
        databaseLoader.setGroupDBM(new GroupDatabaseManager(getActivity()));
        return databaseLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<Group>> loader, List<Group> data) {
        // Update list.
        GroupController.sortGroups(getContext(), data);
        groups = data;
        listAdapter.setData(data);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<Group>> loader) {
        // Clear adapter data.
        listAdapter.setData(null);
    }
}
