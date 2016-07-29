package ulm.university.news.app.controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.BusEvent;
import ulm.university.news.app.api.BusEventBallotChange;
import ulm.university.news.app.api.GroupAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Ballot;
import ulm.university.news.app.data.ResourceDetail;
import ulm.university.news.app.data.User;
import ulm.university.news.app.manager.database.GroupDatabaseManager;
import ulm.university.news.app.manager.database.UserDatabaseManager;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.BALLOT_NOT_FOUND;
import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;
import static ulm.university.news.app.util.Constants.GROUP_NOT_FOUND;
import static ulm.university.news.app.util.Constants.GROUP_PARTICIPANT_NOT_FOUND;

/**
 * This fragment shows ballot information.
 *
 * @author Matthias Mak
 */
public class BallotDetailFragment extends Fragment implements DialogListener {
    /** This classes tag for logging. */
    private static final String TAG = "BallotDetailFragment";

    private GroupDatabaseManager groupDBM;
    private Ballot ballot;
    private List<ResourceDetail> resourceDetails;
    private ResourceDetailListAdapter listAdapter;

    private ListView lvBallotDetails;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String errorMessage;
    private Toast toast;
    private int groupId;
    private int ballotId;
    private MenuItem menuItemClose;
    private MenuItem menuItemOpen;
    private MenuItem menuItemEdit;
    private MenuItem menuItemDelete;
    private ProgressBar pgrSending;
    private boolean autoRefresh = true;

    public BallotDetailFragment() {
    }

    public static BallotDetailFragment newInstance(int groupId, int ballotId) {
        BallotDetailFragment fragment = new BallotDetailFragment();
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
        resourceDetails = new ArrayList<>();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update ballot in case it was edited.
        ballot = groupDBM.getBallot(ballotId);
        setBallotDetails();
        listAdapter.setResourceDetails(resourceDetails);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ballot_detail, container, false);
        initView(v);
        refreshBallot();
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (ballot.getAdmin() == Util.getInstance(getContext()).getLocalUser().getId()) {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_ballot_detail_menu, menu);
        menuItemClose = menu.findItem(R.id.activity_ballot_detail_menu_close);
        menuItemOpen = menu.findItem(R.id.activity_ballot_detail_menu_open);
        menuItemEdit = menu.findItem(R.id.activity_ballot_detail_menu_edit);
        menuItemDelete = menu.findItem(R.id.activity_ballot_detail_menu_delete);
        if (ballot.getClosed()) {
            menuItemClose.setVisible(false);
            menuItemEdit.setVisible(false);
            menuItemOpen.setVisible(true);
        } else {
            menuItemClose.setVisible(true);
            menuItemEdit.setVisible(true);
            menuItemOpen.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        Bundle args = new Bundle();
        YesNoDialogFragment dialog = new YesNoDialogFragment();
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = NavUtils.getParentActivityIntent(getActivity());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(getActivity(), intent);
                return true;
            case R.id.activity_ballot_detail_menu_close:
                args.putString(YesNoDialogFragment.DIALOG_TITLE, getString(R.string.ballot_close));
                args.putString(YesNoDialogFragment.DIALOG_TEXT, getString(R.string.ballot_close_text));
                dialog.setArguments(args);
                dialog.setTargetFragment(this, 0);
                dialog.show(getFragmentManager(), YesNoDialogFragment.DIALOG_BALLOT_CLOSE);
                return true;
            case R.id.activity_ballot_detail_menu_open:
                args.putString(YesNoDialogFragment.DIALOG_TITLE, getString(R.string.ballot_open));
                args.putString(YesNoDialogFragment.DIALOG_TEXT, getString(R.string.ballot_open_text));
                dialog.setArguments(args);
                dialog.setTargetFragment(this, 0);
                dialog.show(getFragmentManager(), YesNoDialogFragment.DIALOG_BALLOT_OPEN);
                return true;
            case R.id.activity_ballot_detail_menu_edit:
                intent = new Intent(getContext(), BallotEditActivity.class);
                intent.putExtra("groupId", groupId);
                intent.putExtra("ballotId", ballotId);
                startActivity(intent);
                return true;
            case R.id.activity_ballot_detail_menu_delete:
                args.putString(YesNoDialogFragment.DIALOG_TITLE, getString(R.string.ballot_delete));
                args.putString(YesNoDialogFragment.DIALOG_TEXT, getString(R.string.ballot_delete_text));
                dialog.setArguments(args);
                dialog.setTargetFragment(this, 0);
                dialog.show(getFragmentManager(), YesNoDialogFragment.DIALOG_BALLOT_DELETE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initView(View v) {
        lvBallotDetails = (ListView) v.findViewById(R.id.fragment_ballot_detail_lv_ballot_details);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.fragment_ballot_detail_swipe_refresh_layout);
        TextView tvListEmpty = (TextView) v.findViewById(R.id.fragment_ballot_detail_tv_list_empty);
        pgrSending = (ProgressBar) v.findViewById(R.id.fragment_ballot_detail_pgr_sending);

        lvBallotDetails.setEmptyView(tvListEmpty);
        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                autoRefresh = false;
                refreshBallot();
            }
        });

        toast = Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT);
        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
        if (tv != null) tv.setGravity(Gravity.CENTER);

        setBallotDetails();
        listAdapter = new ResourceDetailListAdapter();
        listAdapter.setResourceDetails(resourceDetails);
        lvBallotDetails.setAdapter(listAdapter);
    }

    /**
     * Adds all existing channel detail data to the details list. The details are added in a specific order.
     */
    private void setBallotDetails() {
        resourceDetails.clear();
        ResourceDetail title = new ResourceDetail(getString(R.string.general_title), ballot.getTitle(),
                R.drawable.ic_info_black_36dp);
        resourceDetails.add(title);

        // Check nullable fields.
        if (ballot.getDescription() != null && !ballot.getDescription().isEmpty()) {
            ResourceDetail description = new ResourceDetail(getString(R.string.general_description), ballot.getDescription(), R.drawable
                    .ic_info_outline_black_36dp);
            resourceDetails.add(description);
        }

        String yesNo = getString(R.string.general_no);
        if (ballot.getMultipleChoice()) {
            yesNo = getString(R.string.general_yes);
        }
        ResourceDetail multipleChoice = new ResourceDetail(String.format(getString(R.string
                .ballot_multiple_choice_detail), yesNo), getString(R.string.ballot_multiple_choice_info),
                R.drawable.ic_done_all_black_36dp);
        yesNo = getString(R.string.general_no);
        if (ballot.getPublicVotes()) {
            yesNo = getString(R.string.general_yes);
        }
        ResourceDetail publicVotes = new ResourceDetail(String.format(getString(R.string
                .ballot_public_votes_detail), yesNo), getString(R.string.ballot_public_votes_info),
                R.drawable.ic_public_black_36dp);
        resourceDetails.add(multipleChoice);
        resourceDetails.add(publicVotes);

        User user = new UserDatabaseManager(getContext()).getUser(ballot.getAdmin());
        String adminName = getString(R.string.general_unknown);
        if (user != null) {
            adminName = user.getName();
        }
        ResourceDetail groupAdmin = new ResourceDetail(getString(R.string.ballot_admin), adminName,
                R.drawable.ic_person_outline_black_36dp);
        resourceDetails.add(groupAdmin);
    }

    /**
     * Sends a request to the server to get all new group data.
     */
    private void refreshBallot() {
        // Group refresh is only possible if there is an internet connection.
        if (Util.getInstance(getContext()).isOnline()) {
            errorMessage = getString(R.string.general_error_connection_failed);
            errorMessage += " " + getString(R.string.general_error_refresh);
            // Get ballot on swipe down.
            GroupAPI.getInstance(getContext()).getBallot(groupId, ballotId);
        } else {
            errorMessage = getString(R.string.general_error_no_connection);
            errorMessage += " " + getString(R.string.general_error_refresh);
            toast.setText(errorMessage);
            toast.show();
            // Can't refresh. Hide loading animation.
            swipeRefreshLayout.setRefreshing(false);
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
     * This method will be called when a ballot is posted to the EventBus.
     *
     * @param ballot The bus event containing a ballot object.
     */
    public void onEventMainThread(Ballot ballot) {
        Log.d(TAG, "BusEvent: " + ballot.toString());
        // Update ballot in the database and ballot detail view.
        groupDBM.updateBallot(ballot);
        this.ballot = ballot;
        setBallotDetails();
        listAdapter.setResourceDetails(resourceDetails);
        listAdapter.notifyDataSetChanged();

        // Ballot was refreshed. Hide loading animation.
        swipeRefreshLayout.setRefreshing(false);
        // Show updated message.
        String message = getString(R.string.ballot_info_refreshed);
        toast.setText(message);
        if (!autoRefresh) {
            toast.show();
            autoRefresh = true;
        }
    }

    /**
     * This method will be called when a changed ballot is posted to the EventBus.
     *
     * @param event The bus event containing a changed ballot object.
     */
    public void onEventMainThread(BusEventBallotChange event) {
        Log.d(TAG, "BusEvent: " + ballot.toString());
        // Update ballot in the database and ballot detail view.
        this.ballot = event.getBallot();
        groupDBM.updateBallot(ballot);
        setBallotDetails();
        listAdapter.setResourceDetails(resourceDetails);
        listAdapter.notifyDataSetChanged();

        menuItemClose.setActionView(null);
        menuItemOpen.setActionView(null);
        if (ballot.getClosed()) {
            menuItemClose.setVisible(false);
            menuItemEdit.setVisible(false);
            menuItemOpen.setVisible(true);
            getActivity().finish();
        } else {
            menuItemClose.setVisible(true);
            menuItemEdit.setVisible(true);
            menuItemOpen.setVisible(false);
            getActivity().finish();
        }
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
        if (GroupAPI.DELETE_BALLOT.equals(action)) {
            groupDBM.deleteBallot(ballotId);
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
        // Hide loading animation on server response.
        swipeRefreshLayout.setRefreshing(false);
        // Show appropriate error message.
        switch (serverError.getErrorCode()) {
            case CONNECTION_FAILURE:
                toast.setText(errorMessage);
                toast.show();
                break;
            case GROUP_NOT_FOUND:
                groupDBM.setGroupToDeleted(groupId);
                toast.setText(getString(R.string.group_deleted));
                toast.show();
                // Close activity and go to the main screen to show deleted dialog on restart activity.
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
                break;
            case BALLOT_NOT_FOUND:
                groupDBM.deleteBallot(ballotId);
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

    private void removeLocalUserAsGroupMember() {
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
    public void onDialogPositiveClick(String tag) {
        if (tag.equals(YesNoDialogFragment.DIALOG_BALLOT_CLOSE)) {
            ballot.setClosed(true);
            GroupAPI.getInstance(getContext()).changeBallot(groupId, ballot);
            menuItemClose.setActionView(pgrSending);
        } else if (tag.equals(YesNoDialogFragment.DIALOG_BALLOT_OPEN)) {
            ballot.setClosed(false);
            GroupAPI.getInstance(getContext()).changeBallot(groupId, ballot);
            menuItemOpen.setActionView(pgrSending);
        } else if (tag.equals(YesNoDialogFragment.DIALOG_BALLOT_DELETE)) {
            GroupAPI.getInstance(getContext()).deleteBallot(groupId, ballot.getId());
            menuItemDelete.setActionView(pgrSending);
        }
    }
}
