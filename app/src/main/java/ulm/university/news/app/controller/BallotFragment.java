package ulm.university.news.app.controller;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.BusEventBallots;
import ulm.university.news.app.api.GroupAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Ballot;
import ulm.university.news.app.data.Group;
import ulm.university.news.app.manager.database.DatabaseLoader;
import ulm.university.news.app.manager.database.GroupDatabaseManager;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;
import static ulm.university.news.app.util.Constants.GROUP_NOT_FOUND;


public class BallotFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Ballot>> {
    /** This classes tag for logging. */
    private static final String TAG = "BallotFragment";

    /** The loader's id. */
    private static final int LOADER_ID = 12;

    private AdapterView.OnItemClickListener itemClickListener;
    private DatabaseLoader<List<Ballot>> databaseLoader;

    private BallotListAdapter listAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Ballot> ballots;
    private ListView lvBallots;
    private int groupId;

    private Toast toast;
    private String errorMessage;
    private boolean isAutoRefresh = true;

    public static BallotFragment newInstance(int groupId) {
        BallotFragment fragment = new BallotFragment();
        Bundle args = new Bundle();
        args.putInt("groupId", groupId);
        fragment.setArguments(args);
        return fragment;
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
        groupId = getArguments().getInt("groupId");

        // Initialize or reuse an existing database loader.
        databaseLoader = (DatabaseLoader) getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        databaseLoader.onContentChanged();

        listAdapter = new BallotListAdapter(getActivity(), R.layout.ballot_list_item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ballot, container, false);
        lvBallots = (ListView) view.findViewById(R.id.fragment_ballot_lv_ballots);
        TextView tvListEmpty = (TextView) view.findViewById(R.id.fragment_ballot_tv_list_empty);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_ballot_swipe_refresh_layout);

        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isAutoRefresh = false;
                refreshBallots();
            }
        });

        itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Ballot ballot = (Ballot) lvBallots.getItemAtPosition(position);
                Intent intent = new Intent(arg0.getContext(), BallotActivity.class);
                intent.putExtra("groupId", groupId);
                intent.putExtra("ballotId", ballot.getId());
                startActivity(intent);
            }
        };

        lvBallots.setAdapter(listAdapter);
        lvBallots.setOnItemClickListener(itemClickListener);
        lvBallots.setEmptyView(tvListEmpty);

        toast = Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);

        refreshBallots();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_group_ballot_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.activity_group_ballot_tab_add:
                Intent intent = new Intent(getContext(), BallotAddActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    private void refreshBallots() {
        // Refreshing is only possible if there is an internet connection.
        if (Util.getInstance(getContext()).isOnline()) {
            Group group = databaseLoader.getGroupDBM().getGroup(groupId);
            // Don't refresh if group is already marked as deleted.
            if (!group.getDeleted()) {
                errorMessage = getString(R.string.general_error_connection_failed);
                errorMessage += getString(R.string.general_error_refresh);
                // Get ballot data.
                GroupAPI.getInstance(getActivity()).getBallots(groupId);
            } else {
                // Stop loading animation.
                swipeRefreshLayout.setRefreshing(false);
            }
        } else {
            if (!isAutoRefresh) {
                errorMessage = getString(R.string.general_error_no_connection);
                errorMessage += getString(R.string.general_error_refresh);
                // Only show error message if refreshing was triggered manually.
                toast.setText(errorMessage);
                toast.show();
                // Reset the auto refresh flag.
                isAutoRefresh = true;
                // Can't refresh. Hide loading animation.
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    /**
     * This method will be called when a list of ballots is posted to the EventBus.
     *
     * @param event The bus event containing a list of ballot objects.
     */
    public void onEventMainThread(BusEventBallots event) {
        Log.d(TAG, event.toString());
        List<Ballot> ballots = event.getBallots();
        boolean newBallots = GroupController.storeBallots(getActivity(), ballots, groupId);
        // Ballots were refreshed. Hide loading animation.
        swipeRefreshLayout.setRefreshing(false);

        if (newBallots) {
            // If ballot data was updated show message no matter if it was a manual or auto refresh.
            String message = getString(R.string.ballot_info_updated);
            toast.setText(message);
            toast.show();
        } else {
            if (!isAutoRefresh) {
                // Only show up to date message if a manual refresh was triggered.
                String message = getString(R.string.ballot_info_up_to_date);
                toast.setText(message);
                toast.show();
            }
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
        // Show appropriate error message.
        switch (serverError.getErrorCode()) {
            case CONNECTION_FAILURE:
                toast.setText(errorMessage);
                toast.show();
                break;
            case GROUP_NOT_FOUND:
                new GroupDatabaseManager(getContext()).setGroupToDeleted(groupId);
                // Close activity and go to the main screen to show deleted dialog on restart activity.
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
                break;
        }
    }

    @Override
    public Loader<List<Ballot>> onCreateLoader(int id, Bundle args) {
        databaseLoader = new DatabaseLoader<>(getActivity(), new DatabaseLoader
                .DatabaseLoaderCallbacks<List<Ballot>>() {
            @Override
            public List<Ballot> onLoadInBackground() {
                // Load all ballots of the group.
                return databaseLoader.getGroupDBM().getBallots(groupId);
            }

            @Override
            public IntentFilter observerFilter() {
                // Listen to database changes on new or updated ballots.
                IntentFilter filter = new IntentFilter();
                filter.addAction(GroupDatabaseManager.STORE_BALLOT);
                filter.addAction(GroupDatabaseManager.UPDATE_BALLOT);
                filter.addAction(GroupDatabaseManager.BALLOT_DELETED);
                filter.addAction(GroupDatabaseManager.STORE_BALLOT_OPTION);
                return filter;
            }
        });
        // This loader uses the group database manager to load data.
        databaseLoader.setGroupDBM(new GroupDatabaseManager(getActivity()));
        return databaseLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<Ballot>> loader, List<Ballot> data) {
        // Update list.
        ballots = data;
        listAdapter.setData(data);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<Ballot>> loader) {
        // Clear adapter data.
        listAdapter.setData(null);
    }
}
