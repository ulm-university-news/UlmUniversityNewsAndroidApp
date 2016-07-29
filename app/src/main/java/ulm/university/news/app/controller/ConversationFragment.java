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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.BusEventConversations;
import ulm.university.news.app.api.GroupAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Conversation;
import ulm.university.news.app.data.Group;
import ulm.university.news.app.manager.database.DatabaseLoader;
import ulm.university.news.app.manager.database.GroupDatabaseManager;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;
import static ulm.university.news.app.util.Constants.GROUP_NOT_FOUND;
import static ulm.university.news.app.util.Constants.GROUP_PARTICIPANT_NOT_FOUND;


public class ConversationFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Conversation>> {
    /** This classes tag for logging. */
    private static final String TAG = "ConversationFragment";

    /** The loader's id. */
    private static final int LOADER_ID = 6;

    private AdapterView.OnItemClickListener itemClickListener;
    private DatabaseLoader<List<Conversation>> databaseLoader;

    private ConversationListAdapter listAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Conversation> conversations;
    private ListView lvConversations;
    private int groupId;

    private Toast toast;
    private String errorMessage;
    private boolean isAutoRefresh = true;

    public static ConversationFragment newInstance(int groupId) {
        ConversationFragment fragment = new ConversationFragment();
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

        listAdapter = new ConversationListAdapter(getActivity(), R.layout.conversation_list_item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation, container, false);
        lvConversations = (ListView) view.findViewById(R.id.fragment_conversation_lv_conversations);
        TextView tvListEmpty = (TextView) view.findViewById(R.id.fragment_conversation_tv_list_empty);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_conversation_swipe_refresh_layout);

        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isAutoRefresh = false;
                refreshConversations();
            }
        });

        itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Conversation conversation = (Conversation) lvConversations.getItemAtPosition(position);
                Intent intent = new Intent(arg0.getContext(), ConversationActivity.class);
                intent.putExtra("groupId", groupId);
                intent.putExtra("conversationId", conversation.getId());
                startActivity(intent);
            }
        };

        lvConversations.setAdapter(listAdapter);
        lvConversations.setOnItemClickListener(itemClickListener);
        lvConversations.setEmptyView(tvListEmpty);

        toast = Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);

        refreshConversations();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_group_conversation_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.activity_group_conversation_tab_add:
                Intent intent = new Intent(getContext(), ConversationAddActivity.class);
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

    private void refreshConversations() {
        // Refreshing is only possible if there is an internet connection.
        if (Util.getInstance(getContext()).isOnline()) {
            Group group = databaseLoader.getGroupDBM().getGroup(groupId);
            // Don't refresh if group is already marked as deleted.
            if (!group.getDeleted()) {
                errorMessage = getString(R.string.general_error_connection_failed);
                errorMessage += getString(R.string.general_error_refresh);
                // Get conversation data.
                GroupAPI.getInstance(getActivity()).getConversations(groupId);
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
     * This method will be called when a list of announcements is posted to the EventBus.
     *
     * @param event The bus event containing a list of announcement objects.
     */
    public void onEventMainThread(BusEventConversations event) {
        Log.d(TAG, event.toString());
        List<Conversation> conversations = event.getConversations();
        boolean newConversations = GroupController.storeConversations(getActivity(), conversations, groupId);
        // Conversations were refreshed. Hide loading animation.
        swipeRefreshLayout.setRefreshing(false);

        if (newConversations) {
            // If conversation data was updated show message no matter if it was a manual or auto refresh.
            String message = getString(R.string.conversation_info_updated);
            toast.setText(message);
            toast.show();
        } else {
            if (!isAutoRefresh) {
                // Only show up to date message if a manual refresh was triggered.
                String message = getString(R.string.conversation_info_up_to_date);
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
        removeLocalUserAsGroupMember();
    }

    private void showRemovedFromGroupDialog() {
        // Show group deleted dialog if it wasn't shown before.
        InfoDialogFragment dialog = new InfoDialogFragment();
        Bundle args = new Bundle();
        args.putString(InfoDialogFragment.DIALOG_TITLE, getString(R.string.group_member_removed_dialog_title));
        args.putString(InfoDialogFragment.DIALOG_TEXT, getString(R.string.group_member_removed_dialog_text));
        dialog.setArguments(args);
        dialog.show(getActivity().getSupportFragmentManager(), "removedFromGroup");

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
    public Loader<List<Conversation>> onCreateLoader(int id, Bundle args) {
        databaseLoader = new DatabaseLoader<>(getActivity(), new DatabaseLoader
                .DatabaseLoaderCallbacks<List<Conversation>>() {
            @Override
            public List<Conversation> onLoadInBackground() {
                // Load all conversations of the group.
                return databaseLoader.getGroupDBM().getConversations(groupId);
            }

            @Override
            public IntentFilter observerFilter() {
                // Listen to database changes on new or updated conversations.
                IntentFilter filter = new IntentFilter();
                filter.addAction(GroupDatabaseManager.STORE_CONVERSATION);
                filter.addAction(GroupDatabaseManager.UPDATE_CONVERSATION);
                filter.addAction(GroupDatabaseManager.CONVERSATION_DELETED);
                filter.addAction(GroupDatabaseManager.STORE_CONVERSATION_MESSAGE);
                return filter;
            }
        });
        // This loader uses the group database manager to load data.
        databaseLoader.setGroupDBM(new GroupDatabaseManager(getActivity()));
        return databaseLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<Conversation>> loader, List<Conversation> data) {
        // Update list.
        GroupController.sortConversationsName(data);
        conversations = data;
        listAdapter.setData(data);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<Conversation>> loader) {
        // Clear adapter data.
        listAdapter.setData(null);
    }
}
