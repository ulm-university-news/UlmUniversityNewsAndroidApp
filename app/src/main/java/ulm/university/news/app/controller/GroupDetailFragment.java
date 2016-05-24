package ulm.university.news.app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.BusEvent;
import ulm.university.news.app.api.GroupAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Group;
import ulm.university.news.app.data.ResourceDetail;
import ulm.university.news.app.data.User;
import ulm.university.news.app.data.enums.GroupType;
import ulm.university.news.app.manager.database.GroupDatabaseManager;
import ulm.university.news.app.manager.database.UserDatabaseManager;
import ulm.university.news.app.util.TextInputLabels;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;
import static ulm.university.news.app.util.Constants.GROUP_INCORRECT_PASSWORD;
import static ulm.university.news.app.util.Constants.GROUP_NOT_FOUND;
import static ulm.university.news.app.util.Constants.PASSWORD_GROUP_PATTERN;

/**
 * This fragment shows group information and provides a join or leave button.
 *
 * @author Matthias Mak
 */
public class GroupDetailFragment extends Fragment implements DialogListener {
    /** This classes tag for logging. */
    private static final String TAG = "GroupDetailFragment";

    private GroupDatabaseManager groupDBM;
    private Group group;
    private List<ResourceDetail> resourceDetails;
    private ResourceDetailListAdapter listAdapter;

    private ListView lvGroupDetails;
    private Button btnJoin;
    private Button btnLeave;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar pgrSending;
    private TextInputLabels tilPassword;

    private String errorMessage;
    private Toast toast;
    private int groupId;

    public GroupDetailFragment() {
    }

    public static GroupDetailFragment newInstance(int groupId) {
        GroupDetailFragment fragment = new GroupDetailFragment();
        Bundle args = new Bundle();
        args.putInt("groupId", groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupId = getArguments().getInt("groupId");
        groupDBM = new GroupDatabaseManager(getActivity());
        group = groupDBM.getGroup(groupId);
        resourceDetails = new ArrayList<>();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update group in case it was edited.
        group = groupDBM.getGroup(groupId);
        setGroupDetails();
        listAdapter.setResourceDetails(resourceDetails);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_group_detail, container, false);
        initView(v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof ModeratorChannelActivity) {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_moderator_channel_detail_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.activity_moderator_channel_details_edit:
                Intent intent = new Intent(getActivity(), ChannelEditActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
                return true;
            case R.id.activity_moderator_channel_channel_delete:
                if (Util.getInstance(getContext()).isOnline()) {
                    // Show delete channel dialog.
                    YesNoDialogFragment dialog = new YesNoDialogFragment();
                    Bundle args = new Bundle();
                    args.putString(YesNoDialogFragment.DIALOG_TITLE, getString(R.string
                            .channel_delete_dialog_title));
                    args.putString(YesNoDialogFragment.DIALOG_TEXT, getString(R.string
                            .channel_delete_dialog_text));
                    dialog.setArguments(args);
                    dialog.setTargetFragment(GroupDetailFragment.this, 0);
                    dialog.show(getFragmentManager(), YesNoDialogFragment.DIALOG_CHANNEL_DELETE);

                    errorMessage = getString(R.string.general_error_connection_failed);
                    errorMessage += " " + getString(R.string.general_error_delete);
                } else {
                    String message = getString(R.string.general_error_no_connection);
                    message += " " + getString(R.string.general_error_delete);
                    toast.setText(message);
                    toast.show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initView(View v) {
        lvGroupDetails = (ListView) v.findViewById(R.id.fragment_group_detail_lv_group_details);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.fragment_group_detail_swipe_refresh_layout);
        TextView tvListEmpty = (TextView) v.findViewById(R.id.fragment_group_detail_tv_list_empty);
        btnJoin = (Button) v.findViewById(R.id.fragment_group_detail_btn_join);
        btnLeave = (Button) v.findViewById(R.id.fragment_group_detail_btn_leave);
        pgrSending = (ProgressBar) v.findViewById(R.id.fragment_group_detail_pgr_sending);
        tilPassword = (TextInputLabels) v.findViewById(R.id.fragment_group_detail_til_password);

        lvGroupDetails.setEmptyView(tvListEmpty);
        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshGroup();
            }
        });

        tilPassword.setNameAndHint(getString(R.string.group_password));
        tilPassword.setLength(8, 20);
        tilPassword.setPattern(PASSWORD_GROUP_PATTERN);
        tilPassword.setToPasswordField();

        toast = Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT);
        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
        if (tv != null) tv.setGravity(Gravity.CENTER);

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.getInstance(v.getContext()).isOnline()) {
                    if (tilPassword.isValid()) {
                        String password = tilPassword.getText();
                        password = Util.hashPassword(password);
                        GroupAPI.getInstance(v.getContext()).joinGroup(groupId, password);
                        btnJoin.setVisibility(View.GONE);
                        pgrSending.setVisibility(View.VISIBLE);
                        errorMessage = getString(R.string.general_error_connection_failed);
                        errorMessage += " " + getString(R.string.general_error_subscribe);
                    }
                } else {
                    String message = getString(R.string.general_error_no_connection);
                    message += " " + getString(R.string.general_error_subscribe);
                    toast.setText(message);
                    toast.show();
                }
            }
        });

        btnLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.getInstance(v.getContext()).isOnline()) {
                    // Show leave channel dialog.
                    YesNoDialogFragment dialog = new YesNoDialogFragment();
                    Bundle args = new Bundle();
                    args.putString(YesNoDialogFragment.DIALOG_TITLE, getString(R.string
                            .group_leave_dialog_title));
                    args.putString(YesNoDialogFragment.DIALOG_TEXT, getString(R.string
                            .group_leave_dialog_text));
                    dialog.setArguments(args);
                    dialog.setTargetFragment(GroupDetailFragment.this, 0);
                    dialog.show(getFragmentManager(), YesNoDialogFragment.DIALOG_GROUP_LEAVE);

                    errorMessage = getString(R.string.general_error_connection_failed);
                    errorMessage += " " + getString(R.string.general_error_leave);
                } else {
                    String message = getString(R.string.general_error_no_connection);
                    message += " " + getString(R.string.general_error_leave);
                    toast.setText(message);
                    toast.show();
                }
            }
        });

        if (groupDBM.isGroupMember(group.getId())) {
            btnJoin.setVisibility(View.GONE);
            tilPassword.setVisibility(View.GONE);
            btnLeave.setVisibility(View.VISIBLE);
        } else {
            btnJoin.setVisibility(View.VISIBLE);
            tilPassword.setVisibility(View.VISIBLE);
            btnLeave.setVisibility(View.GONE);
        }

        setGroupDetails();
        listAdapter = new ResourceDetailListAdapter();
        listAdapter.setResourceDetails(resourceDetails);
        lvGroupDetails.setAdapter(listAdapter);
    }

    /**
     * Adds all existing channel detail data to the details list. The details are added in a specific order.
     */
    private void setGroupDetails() {
        resourceDetails.clear();
        ResourceDetail name = new ResourceDetail(getString(R.string.group_name), group.getName(),
                R.drawable.ic_info_black_36dp);
        String typeName;
        if (group.getGroupType().equals(GroupType.TUTORIAL)) {
            typeName = getString(R.string.group_tutorial_name);
        } else {
            typeName = getString(R.string.group_working_name);
        }
        ResourceDetail type = new ResourceDetail(getString(R.string.group_type), typeName,
                R.drawable.ic_details_black_36dp);
        ResourceDetail term = new ResourceDetail(getString(R.string.channel_term), Util.getInstance(getContext())
                .getTermLong(group.getTerm()), R.drawable.ic_date_range_black_36dp);
        resourceDetails.add(name);
        resourceDetails.add(type);
        resourceDetails.add(term);

        // Check nullable fields.
        if (group.getDescription() != null && !group.getDescription().isEmpty()) {
            ResourceDetail description = new ResourceDetail(getString(R.string.group_description), group
                    .getDescription(), R.drawable.ic_info_outline_black_36dp);
            resourceDetails.add(description);
        }

        // Include group admin detail only if local user is a group member.
        if (groupDBM.isGroupMember(groupId)) {
            UserDatabaseManager userDBM = new UserDatabaseManager(getContext());
            User user = userDBM.getUser(group.getGroupAdmin());
            String adminName = "Unknown";
            if (user != null) {
                adminName = user.getName();
            }
            ResourceDetail groupAdmin = new ResourceDetail(getString(R.string.group_admin), adminName,
                    R.drawable.ic_person_black_36dp);
            resourceDetails.add(groupAdmin);
        }

        ResourceDetail creationDate = new ResourceDetail(getString(R.string.channel_creation_date),
                Util.getInstance(getContext()).getFormattedDateLong(group.getCreationDate()), R.drawable
                .ic_today_black_36dp);
        ResourceDetail modificationDate = new ResourceDetail(getString(R.string.channel_modification_date),
                Util.getInstance(getContext()).getFormattedDateLong(group.getModificationDate()), R.drawable
                .ic_event_black_36dp);
        resourceDetails.add(creationDate);
        resourceDetails.add(modificationDate);
    }

    /**
     * Sends a request to the server to get all new group data.
     */
    private void refreshGroup() {
        // Group refresh is only possible if there is an internet connection.
        if (Util.getInstance(getContext()).isOnline()) {
            errorMessage = getString(R.string.general_error_connection_failed);
            errorMessage += " " + getString(R.string.general_error_refresh);
            // Get group on swipe down.
            GroupAPI.getInstance(getContext()).getGroup(groupId);
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
     * This method will be called when a group is posted to the EventBus.
     *
     * @param group The bus event containing a group object.
     */
    public void onEventMainThread(Group group) {
        Log.d(TAG, "BusEvent: " + group.toString());
        processGroupData(group);
    }

    /**
     * Updates the group in the database if it was updated on the server.
     *
     * @param group The group to process.
     */
    public void processGroupData(Group group) {
        // Update group in the database and group detail view if necessary.
        boolean hasGroupChanged = this.group.getModificationDate().isBefore(group.getModificationDate());
        if (hasGroupChanged) {
            groupDBM.updateGroup(group);
            this.group = group;
            setGroupDetails();
            listAdapter.setResourceDetails(resourceDetails);
            listAdapter.notifyDataSetChanged();
        }

        // Group was refreshed. Hide loading animation.
        swipeRefreshLayout.setRefreshing(false);

        if (hasGroupChanged) {
            // If group data was updated show updated message.
            String message = getString(R.string.group_info_updated);
            toast.setText(message);
            toast.show();
        } else {
            // Otherwise show up to date message.
            String message = getString(R.string.group_info_up_to_date);
            toast.setText(message);
            toast.show();
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
        if (GroupAPI.JOIN_GROUP.equals(action)) {
            groupDBM.joinGroup(groupId);
            // TODO Get group members.
            // ChannelAPI.getInstance(getContext()).getResponsibleModerators(channel.getId());
            Intent intent = new Intent(getActivity(), GroupActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
            getActivity().finish();
        } else if (GroupAPI.LEAVE_GROUP.equals(action)) {
            groupDBM.leaveGroup(groupId);
            // Delete group from local database.
            groupDBM.deleteGroup(groupId);
            getActivity().finish();
        } else if (GroupAPI.DELETE_GROUP.equals(action)) {
            // TODO Mark local group as deleted.
            // ChannelController.deleteChannel(getContext(), channel.getId());
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
        boolean isGroupMember;
        if (groupDBM.isGroupMember(groupId)) {
            btnJoin.setVisibility(View.GONE);
            tilPassword.setVisibility(View.GONE);
            btnLeave.setVisibility(View.VISIBLE);
            isGroupMember = true;
        } else {
            btnJoin.setVisibility(View.VISIBLE);
            tilPassword.setVisibility(View.VISIBLE);
            btnLeave.setVisibility(View.GONE);
            isGroupMember = false;
        }
        pgrSending.setVisibility(View.GONE);
        // Show appropriate error message.
        switch (serverError.getErrorCode()) {
            case CONNECTION_FAILURE:
                toast.setText(errorMessage);
                toast.show();
                break;
            case GROUP_NOT_FOUND:
                // If local user is a group member, do nothing.
                if (!isGroupMember) {
                    // Group was deleted on the server, so delete it on the local database too.
                    groupDBM.deleteGroup(groupId);
                    // Show group deleted dialog.
                    InfoDialogFragment dialog = new InfoDialogFragment();
                    Bundle args = new Bundle();
                    args.putString(InfoDialogFragment.DIALOG_TITLE, getString(R.string.group_deleted_dialog_title));
                    String text = String.format(getString(R.string.group_deleted_dialog_text), group.getName());
                    args.putString(InfoDialogFragment.DIALOG_TEXT, text);
                    dialog.setArguments(args);
                    dialog.show(getActivity().getSupportFragmentManager(), InfoDialogFragment
                            .DIALOG_JOIN_DELETED_GROUP);
                }
                break;
            case GROUP_INCORRECT_PASSWORD:
                String text = getString(R.string.group_password_invalid);
                toast.setText(text);
                toast.show();
                break;
        }
    }

    @Override
    public void onDialogPositiveClick(String tag) {
        if (tag.equals(YesNoDialogFragment.DIALOG_GROUP_LEAVE)) {
            GroupAPI.getInstance(getContext()).leaveGroup(groupId);
            btnLeave.setVisibility(View.GONE);
            pgrSending.setVisibility(View.VISIBLE);
        } else if (tag.equals(YesNoDialogFragment.DIALOG_GROUP_DELETE)) {
            GroupAPI.getInstance(getContext()).deleteGroup(groupId);
            pgrSending.setVisibility(View.VISIBLE);
        }
    }
}