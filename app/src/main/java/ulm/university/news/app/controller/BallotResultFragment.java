package ulm.university.news.app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.BusEventOptions;
import ulm.university.news.app.api.GroupAPI;
import ulm.university.news.app.data.Ballot;
import ulm.university.news.app.data.Option;
import ulm.university.news.app.manager.database.GroupDatabaseManager;
import ulm.university.news.app.util.Util;

/**
 * This fragment shows the results of the ballot.
 *
 * @author Matthias Mak
 */
public class BallotResultFragment extends Fragment {
    /** This classes tag for logging. */
    private static final String TAG = "BallotResultFragment";

    private GroupDatabaseManager groupDBM;
    private Ballot ballot;
    private List<Option> options;
    private OptionResultListAdapter listAdapter;

    private ListView lvOptions;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar pgrSending;

    private String errorMessage;
    private Toast toast;
    private int groupId;
    private int ballotId;


    private boolean isAutoRefresh = true;

    public BallotResultFragment() {
    }

    public static BallotResultFragment newInstance(int groupId, int ballotId) {
        BallotResultFragment fragment = new BallotResultFragment();
        Bundle args = new Bundle();
        args.putInt("groupId", groupId);
        args.putInt("ballotId", ballotId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupId = getArguments().getInt("groupId");
        ballotId = getArguments().getInt("ballotId");
        groupDBM = new GroupDatabaseManager(getActivity());
        ballot = groupDBM.getBallot(ballotId);
        options = groupDBM.getOptions(ballotId);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update ballot in case it was edited.
        ballot = groupDBM.getBallot(ballotId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ballot_result, container, false);
        initView(v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = NavUtils.getParentActivityIntent(getActivity());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(getActivity(), intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initView(View v) {
        lvOptions = (ListView) v.findViewById(R.id.fragment_ballot_result_lv_result);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.fragment_ballot_result_swipe_refresh_layout);
        TextView tvListEmpty = (TextView) v.findViewById(R.id.fragment_ballot_result_tv_list_empty);
        pgrSending = (ProgressBar) v.findViewById(R.id.fragment_ballot_result_pgr_sending);

        lvOptions.setEmptyView(tvListEmpty);
        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshOptions();
            }
        });

        toast = Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT);
        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
        if (tv != null) tv.setGravity(Gravity.CENTER);

        listAdapter = new OptionResultListAdapter(getActivity(),
                R.layout.option_result_list_item, ballot.getPublicVotes());

        lvOptions.setAdapter(listAdapter);
        lvOptions.setEmptyView(tvListEmpty);

        listAdapter.setData(options);
    }

    private void refreshOptions() {
        // Refreshing is only possible if there is an internet connection.
        if (Util.getInstance(getContext()).isOnline()) {
            errorMessage = getString(R.string.general_error_connection_failed);
            errorMessage += getString(R.string.general_error_refresh);
            // Get ballot option data.
            GroupAPI.getInstance(getActivity()).getOptions(groupId, ballot.getId(), true);
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
     * This method will be called when a list of options is posted to the EventBus.
     *
     * @param event The bus event containing a list of ballot options objects.
     */
    public void onEventMainThread(BusEventOptions event) {
        Log.d(TAG, event.toString());
        List<Option> options = event.getOptions();
        // Options were refreshed. Hide loading animation.
        swipeRefreshLayout.setRefreshing(false);

        boolean newOptions = GroupController.storeOptions(getContext(), options, ballot.getId());

        if (newOptions) {
            // If ballot data was updated show message no matter if it was a manual or auto refresh.
            String message = getString(R.string.option_info_updated);
            toast.setText(message);
            toast.show();
        } else {
            if (!isAutoRefresh) {
                // Only show updated message if a manual refresh was triggered.
                String message = getString(R.string.option_info_up_to_date);
                toast.setText(message);
                toast.show();
            }
        }

        boolean newVotes = GroupController.storeVoters(getContext(), options);

        if (newVotes) {
            // If createVote data was updated show message no matter if it was a manual or auto refresh.
            String message = getString(R.string.option_info_updated);
            toast.setText(message);
            toast.show();
        } else {
            if (!isAutoRefresh) {
                // Only show updated message if a manual refresh was triggered.
                String message = getString(R.string.option_info_up_to_date);
                toast.setText(message);
                toast.show();
            }
        }
    }
}
