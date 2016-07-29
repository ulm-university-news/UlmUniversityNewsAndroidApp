package ulm.university.news.app.controller;

import android.content.DialogInterface;
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
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.BusEvent;
import ulm.university.news.app.api.BusEventOptions;
import ulm.university.news.app.api.GroupAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Ballot;
import ulm.university.news.app.data.Group;
import ulm.university.news.app.data.Option;
import ulm.university.news.app.manager.database.DatabaseLoader;
import ulm.university.news.app.manager.database.GroupDatabaseManager;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;
import static ulm.university.news.app.util.Constants.GROUP_NOT_FOUND;
import static ulm.university.news.app.util.Constants.GROUP_PARTICIPANT_NOT_FOUND;
import static ulm.university.news.app.util.Constants.OPTION_NOT_FOUND;


public class OptionFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Option>>, DialogListener {
    /** This classes tag for logging. */
    private static final String TAG = "OptionFragment";

    /** The loader's id. */
    private static final int LOADER_ID = 13;

    private DatabaseLoader<List<Option>> databaseLoader;

    private OptionListAdapter listAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Option> options;
    private ListView lvOptions;
    private Button btnVote;
    private ProgressBar pgrSending;
    private int groupId;
    private Ballot ballot;
    private int voteCounterExpected;
    private int voteCounterReceived;
    private TextView headerView;

    private Toast toast;
    private String errorMessage;
    private boolean isAutoRefresh = true;

    public static OptionFragment newInstance(int groupId, int ballotId) {
        OptionFragment fragment = new OptionFragment();
        Bundle args = new Bundle();
        args.putInt("groupId", groupId);
        args.putInt("ballotId", ballotId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update option list to make changes visible.
        if (databaseLoader != null) {
            databaseLoader.onContentChanged();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupId = getArguments().getInt("groupId");
        int ballotId = getArguments().getInt("ballotId");

        // Initialize or reuse an existing database loader.
        databaseLoader = (DatabaseLoader) getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        databaseLoader.onContentChanged();
        ballot = databaseLoader.getGroupDBM().getBallot(ballotId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_option, container, false);
        lvOptions = (ListView) view.findViewById(R.id.fragment_option_lv_options);
        TextView tvListEmpty = (TextView) view.findViewById(R.id.fragment_option_tv_list_empty);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_option_swipe_refresh_layout);
        btnVote = (Button) view.findViewById(R.id.fragment_option_btn_vote);
        pgrSending = (ProgressBar) view.findViewById(R.id.fragment_option_pgr_sending);
        TextView tvBallotClosed = (TextView) view.findViewById(R.id.fragment_option_tv_ballot_closed);

        if (ballot.getMultipleChoice()) {
            lvOptions.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        } else {
            lvOptions.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        }

        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isAutoRefresh = false;
                refreshOptions();
            }
        });

        listAdapter = new OptionListAdapter(getActivity(), R.layout.option_list_item, ballot, btnVote, this);

        lvOptions.setAdapter(listAdapter);
        lvOptions.setEmptyView(tvListEmpty);

        // Create and add closed header text if ballot is closed.
        if (ballot.getClosed()) {
            tvBallotClosed.setVisibility(View.VISIBLE);
            btnVote.setVisibility(View.GONE);
        }

        btnVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.getInstance(v.getContext()).isOnline()) {
                    vote();
                } else {
                    String message = getString(R.string.general_error_no_connection);
                    message += " " + getString(R.string.general_error_vote);
                    toast.setText(message);
                    toast.show();
                }
            }
        });

        toast = Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);

        refreshOptions();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (ballot.getAdmin() == Util.getInstance(getContext()).getLocalUser().getId() && !ballot.getClosed()) {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_ballot_option_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.activity_ballot_option_tab_add:
                Intent intent = new Intent(getContext(), OptionAddActivity.class);
                intent.putExtra("groupId", groupId);
                intent.putExtra("ballotId", ballot.getId());
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

    private void refreshOptions() {
        // Refreshing is only possible if there is an internet connection.
        if (Util.getInstance(getContext()).isOnline()) {
            Group group = databaseLoader.getGroupDBM().getGroup(groupId);
            // Don't refresh if group is already marked as deleted.
            if (!group.getDeleted()) {
                errorMessage = getString(R.string.general_error_connection_failed);
                errorMessage += getString(R.string.general_error_refresh);
                // Get ballot option data.
                GroupAPI.getInstance(getActivity()).getOptions(groupId, ballot.getId(), true);
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

    private void vote() {
        btnVote.setVisibility(View.GONE);
        pgrSending.setVisibility(View.VISIBLE);
        errorMessage = getString(R.string.general_error_connection_failed);
        errorMessage += " " + getString(R.string.general_error_vote);
        voteCounterExpected = 0;
        voteCounterReceived = 0;

        if (ballot.getMultipleChoice()) {
            // Vote for selected options.
            for (Option option : listAdapter.getMultipleSelectedOptions()) {
                GroupAPI.getInstance(getContext()).createVote(groupId, ballot.getId(), option.getId());
                voteCounterExpected++;
            }
            // Delete unselected options.
            for (Option optionPreviouslySelected : GroupController.getMyOptions(getContext(), options)) {
                boolean unselected = true;
                for (Option optionCurrentlySelected : listAdapter.getMultipleSelectedOptions()) {
                    if (optionCurrentlySelected.getId() == optionPreviouslySelected.getId()) {
                        unselected = false;
                        break;
                    }
                }
                if (unselected) {
                    // Delete unselected vote.
                    GroupAPI.getInstance(getContext()).deleteVote(groupId, ballot.getId(),
                            optionPreviouslySelected.getId());
                }
            }
        } else {
            Option option = GroupController.getMyOption(getContext(), options);
            if (option == null) {
                // There is no previously selected vote. Vote directly.
                GroupAPI.getInstance(getContext()).createVote(groupId, ballot.getId(),
                        listAdapter.getSingleSelectedOption().getId());
            } else {
                // Delete previously selected vote. Vote after deletion.
                GroupAPI.getInstance(getContext()).deleteVote(groupId, ballot.getId(), option.getId());
            }
            voteCounterExpected = 1;
        }
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
            // If vote data was updated show message no matter if it was a manual or auto refresh.
            String message = getString(R.string.vote_info_updated);
            toast.setText(message);
            toast.show();
        } else {
            if (!isAutoRefresh) {
                // Only show updated message if a manual refresh was triggered.
                String message = getString(R.string.vote_info_up_to_date);
                toast.setText(message);
                toast.show();
            }
        }
    }

    /**
     * This method will be called when a BusEvent is posted to the EventBus.
     *
     * @param event The bus event containing an action an maybe additional objects.
     */
    public void onEventMainThread(BusEvent event) {
        Log.d(TAG, "BusEvent: " + event.getAction());
        if (GroupAPI.OPTION_DELETED.equals(event.getAction())) {
            databaseLoader.getGroupDBM().deleteOption((int) event.getObject());

            pgrSending.setVisibility(View.GONE);
            btnVote.setVisibility(View.VISIBLE);
            toast.setText(getString(R.string.option_deleted));
            toast.show();
        } else {
            if (GroupAPI.VOTE_CREATED.equals(event.getAction())) {
                Log.d(TAG, "Vote created message received.");
                // Store vote in database.
                databaseLoader.getGroupDBM().storeVote((int) event.getObject(), Util.getInstance(getContext())
                        .getLocalUser().getId());
                voteCounterReceived++;
            } else if (GroupAPI.VOTE_DELETED.equals(event.getAction())) {
                Log.d(TAG, "Vote deleted message received.");
                // Delete vote from database.
                databaseLoader.getGroupDBM().deleteVote((int) event.getObject(), Util.getInstance(getContext())
                        .getLocalUser().getId());
                if (!ballot.getMultipleChoice()) {
                    // After deletion vote for new option.
                    GroupAPI.getInstance(getContext()).createVote(groupId, ballot.getId(),
                            listAdapter.getSingleSelectedOption().getId());
                }
            }
            if (voteCounterReceived == voteCounterExpected) {
                databaseLoader.onContentChanged();
                pgrSending.setVisibility(View.GONE);
                btnVote.setVisibility(View.VISIBLE);
                String message = getString(R.string.vote_voted);
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
            case OPTION_NOT_FOUND:
                toast.setText(getString(R.string.option_delete_server));
                getActivity().finish();
            case GROUP_NOT_FOUND:
                new GroupDatabaseManager(getContext()).setGroupToDeleted(groupId);
                toast.setText(getString(R.string.group_deleted));
                toast.show();
                // Close activity and go to the main screen to show deleted dialog on restart activity.
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
                break;
            case GROUP_PARTICIPANT_NOT_FOUND:
                // showRemovedFromGroupDialog();
                showRemovedFromGroupMessage();
                break;
        }
    }

    private void showRemovedFromGroupMessage() {
        toast.setText(R.string.group_member_removed_dialog_text);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
        removeLocalUserAsGroupMember();
    }

    private void showRemovedFromGroupDialog() {
        // Show group deleted dialog if it wasn't shown before.
        InfoDialogFragment dialog = new InfoDialogFragment();
        Bundle args = new Bundle();
        args.putString(InfoDialogFragment.DIALOG_TITLE, getString(R.string.group_member_removed_dialog_title));
        args.putString(InfoDialogFragment.DIALOG_TEXT, getString(R.string.group_member_removed_dialog_text));
        dialog.setArguments(args);
        dialog.show(getFragmentManager(), "removedFromGroup");

        dialog.onDismiss(new DialogInterface() {
            @Override
            public void cancel() {
                removeLocalUserAsGroupMember();
            }

            @Override
            public void dismiss() {
                removeLocalUserAsGroupMember();
            }
        });
    }

    private void removeLocalUserAsGroupMember(){
        // Remove local user as group member.
        new GroupDatabaseManager(getContext()).removeUserFromGroup(groupId,
                Util.getInstance(getContext()).getLocalUser().getId());
        // Close activity and go to the main screen to show deleted dialog on restart activity.
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public Loader<List<Option>> onCreateLoader(int id, Bundle args) {
        databaseLoader = new DatabaseLoader<>(getActivity(), new DatabaseLoader
                .DatabaseLoaderCallbacks<List<Option>>() {
            @Override
            public List<Option> onLoadInBackground() {
                // Load all options of the ballot.
                return databaseLoader.getGroupDBM().getOptions(ballot.getId());
            }

            @Override
            public IntentFilter observerFilter() {
                // Listen to database changes on new options.
                IntentFilter filter = new IntentFilter();
                filter.addAction(GroupDatabaseManager.STORE_BALLOT_OPTION);
                filter.addAction(GroupDatabaseManager.DELETE_BALLOT_OPTION);
                filter.addAction(GroupDatabaseManager.STORE_BALLOT_OPTION_VOTE);
                filter.addAction(GroupDatabaseManager.DELETE_BALLOT_OPTION_VOTE);
                return filter;
            }
        });
        // This loader uses the group database manager to load data.
        databaseLoader.setGroupDBM(new GroupDatabaseManager(getActivity()));
        return databaseLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<Option>> loader, List<Option> data) {
        // Update list.
        GroupController.sortOptionsName(data);
        options = data;
        listAdapter.setData(data);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<Option>> loader) {
        // Clear adapter data.
        listAdapter.setData(null);
    }

    @Override
    public void onDialogPositiveClick(String tag) {
        if (YesNoDialogFragment.DIALOG_OPTION_DELETE.equals(tag)) {
            if (Util.getInstance(getContext()).isOnline()) {
                btnVote.setVisibility(View.GONE);
                pgrSending.setVisibility(View.VISIBLE);
                GroupAPI.getInstance(getContext()).deleteOption(groupId, ballot.getId(),
                        listAdapter.getCurrentOptionId());

                errorMessage = getString(R.string.general_error_connection_failed);
                errorMessage += " " + getString(R.string.general_error_delete);
            } else {
                String message = getString(R.string.general_error_no_connection);
                message += " " + getString(R.string.general_error_delete);
                toast.setText(message);
                toast.show();
            }
        }
    }
}
