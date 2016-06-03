package ulm.university.news.app.controller;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.BusEvent;
import ulm.university.news.app.api.BusEventConversationChange;
import ulm.university.news.app.api.BusEventConversationMessages;
import ulm.university.news.app.api.GroupAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Conversation;
import ulm.university.news.app.data.ConversationMessage;
import ulm.university.news.app.data.enums.Priority;
import ulm.university.news.app.manager.database.DatabaseLoader;
import ulm.university.news.app.manager.database.GroupDatabaseManager;
import ulm.university.news.app.util.Constants;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;
import static ulm.university.news.app.util.Constants.CONVERSATION_NOT_FOUND;

public class ConversationActivity extends AppCompatActivity implements DialogListener, LoaderManager
        .LoaderCallbacks<List<ConversationMessage>> {
    /** This classes tag for logging. */
    private static final String TAG = "ConversationActivity";

    /** The loader's id. This id is specific to the ChannelSearchActivity's LoaderManager. */
    private static final int LOADER_ID = 8;

    private DatabaseLoader<List<ConversationMessage>> databaseLoader;

    private ConversationMessageListAdapter listAdapter;
    private List<ConversationMessage> conversationMessages;
    private Conversation conversation;
    private int groupId;

    private ProgressBar pgrSending;
    private ListView lvConversationMessages;
    private EditText etMessage;
    private ImageView ivSend;
    private String errorMessage;
    private Toast toast;
    private MenuItem menuItemClose;
    private MenuItem menuItemOpen;
    private MenuItem menuItemEdit;
    private MenuItem menuItemDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set color scheme to conversation yellow.
        setTheme(R.style.UlmUniversity_Conversation);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        groupId = getIntent().getIntExtra("groupId", 0);
        int conversationId = getIntent().getIntExtra("conversationId", 0);
        conversation = new GroupDatabaseManager(this).getConversation(conversationId);

        // Initialize the loader. If the Loader with this id already exists, then the LoaderManager will reuse the
        // existing Loader.
        databaseLoader = (DatabaseLoader) getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_conversation_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(conversation.getTitle());

        initView();
        if (conversation.getClosed()) {
            etMessage.setVisibility(View.GONE);
            ivSend.setVisibility(View.GONE);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (conversation.isAdmin(Util.getInstance(this).getLocalUser().getId())) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.activity_conversation_menu, menu);
            menuItemClose = menu.findItem(R.id.activity_conversation_menu_close);
            menuItemOpen = menu.findItem(R.id.activity_conversation_menu_open);
            menuItemEdit = menu.findItem(R.id.activity_conversation_menu_edit);
            menuItemDelete = menu.findItem(R.id.activity_conversation_menu_delete);
            if (conversation.getClosed()) {
                menuItemClose.setVisible(false);
                menuItemEdit.setVisible(false);
                menuItemOpen.setVisible(true);
            } else {
                menuItemClose.setVisible(true);
                menuItemEdit.setVisible(true);
                menuItemOpen.setVisible(false);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        Bundle args = new Bundle();
        YesNoDialogFragment dialog = new YesNoDialogFragment();
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
            case R.id.activity_conversation_menu_close:
                args.putString(YesNoDialogFragment.DIALOG_TITLE, getString(R.string.conversation_close));
                args.putString(YesNoDialogFragment.DIALOG_TEXT, getString(R.string.conversation_close_text));
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), YesNoDialogFragment.DIALOG_CONVERSATION_CLOSE);
                return true;
            case R.id.activity_conversation_menu_open:
                args.putString(YesNoDialogFragment.DIALOG_TITLE, getString(R.string.conversation_open));
                args.putString(YesNoDialogFragment.DIALOG_TEXT, getString(R.string.conversation_open_text));
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), YesNoDialogFragment.DIALOG_CONVERSATION_OPEN);
                return true;
            case R.id.activity_conversation_menu_edit:
                intent = new Intent(this, ConversationEditActivity.class);
                intent.putExtra("groupId", groupId);
                intent.putExtra("conversationId", conversation.getId());
                startActivity(intent);
                return true;
            case R.id.activity_conversation_menu_delete:
                args.putString(YesNoDialogFragment.DIALOG_TITLE, getString(R.string.conversation_delete));
                args.putString(YesNoDialogFragment.DIALOG_TEXT, getString(R.string.conversation_delete_text));
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), YesNoDialogFragment.DIALOG_CONVERSATION_DELETE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update activity title in case the conversation was edited.
        conversation = databaseLoader.getGroupDBM().getConversation(conversation.getId());
        getSupportActionBar().setTitle(conversation.getTitle());
        // Check for new messages whenever the conversation is shown.
        refreshConversationMessages();
        // Update channel list to make new conversation messages visible.
        if (databaseLoader != null) {
            databaseLoader.onContentChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * Initialises all view elements of this activity.
     */
    private void initView() {
        lvConversationMessages = (ListView) findViewById(R.id.activity_conversation_lv_conversation_messages);
        etMessage = (EditText) findViewById(R.id.activity_conversation_et_message);
        ivSend = (ImageView) findViewById(R.id.activity_conversation_iv_send);
        pgrSending = (ProgressBar) findViewById(R.id.activity_conversation_pgr_sending);
        TextView tvListEmpty = (TextView) findViewById(R.id.activity_conversation_tv_list_empty);

        toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);

        listAdapter = new ConversationMessageListAdapter(this, R.layout.conversation_message_list_item);
        lvConversationMessages.setAdapter(listAdapter);
        lvConversationMessages.setEmptyView(tvListEmpty);

        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        if (!Util.getInstance(this).isOnline()) {
            errorMessage = getString(R.string.general_error_no_connection);
            toast.setText(errorMessage);
            toast.show();
            return;
        }
        String message = etMessage.getText().toString();
        if (message.isEmpty()) {
            return;
        }
        if (message.length() > Constants.MESSAGE_MAX_LENGTH) {
            errorMessage = getString(R.string.message_text_to_long);
            toast.setText(errorMessage);
            toast.show();
            return;
        }
        // Send message to the server.
        ivSend.setVisibility(View.GONE);
        pgrSending.setVisibility(View.VISIBLE);
        ConversationMessage conversationMessage = new ConversationMessage();
        conversationMessage.setText(message);
        conversationMessage.setConversationId(conversation.getId());
        conversationMessage.setPriority(Priority.NORMAL);
        GroupAPI.getInstance(this).createConversationMessage(groupId, conversationMessage);
    }

    private void refreshConversationMessages() {
        // Refreshing is only possible if there is an internet connection.
        if (Util.getInstance(this).isOnline()) {
            errorMessage = getString(R.string.general_error_connection_failed);
            errorMessage += getString(R.string.general_error_refresh);
            // Get conversation message data. Request new messages only.
            int messageNumber = databaseLoader.getGroupDBM().getMaxMessageNumberConversationMessage(
                    conversation.getId());
            GroupAPI.getInstance(this).getConversationMessages(groupId, conversation.getId(), messageNumber);
        }
    }

    @Override
    public void onDialogPositiveClick(String tag) {
        if (tag.equals(YesNoDialogFragment.DIALOG_CONVERSATION_CLOSE)) {
            conversation.setClosed(true);
            GroupAPI.getInstance(this).changeConversation(groupId, conversation);
            menuItemClose.setActionView(pgrSending);
        } else if (tag.equals(YesNoDialogFragment.DIALOG_CONVERSATION_OPEN)) {
            conversation.setClosed(false);
            GroupAPI.getInstance(this).changeConversation(groupId, conversation);
            menuItemOpen.setActionView(pgrSending);
        } else if (tag.equals(YesNoDialogFragment.DIALOG_CONVERSATION_DELETE)) {
            GroupAPI.getInstance(this).deleteConversation(groupId, conversation.getId());
            menuItemDelete.setActionView(pgrSending);
        }
    }

    @Override
    public Loader<List<ConversationMessage>> onCreateLoader(int id, Bundle args) {
        databaseLoader = new DatabaseLoader<>(this, new DatabaseLoader
                .DatabaseLoaderCallbacks<List<ConversationMessage>>() {
            @Override
            public List<ConversationMessage> onLoadInBackground() {
                return databaseLoader.getGroupDBM().getConversationMessages(conversation.getId());
            }

            @Override
            public IntentFilter observerFilter() {
                // Listen to database changes on new conversation messages.
                IntentFilter filter = new IntentFilter();
                filter.addAction(GroupDatabaseManager.STORE_CONVERSATION_MESSAGE);
                return filter;
            }
        });
        databaseLoader.setGroupDBM(new GroupDatabaseManager(this));
        return databaseLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<ConversationMessage>> loader, List<ConversationMessage> data) {
        conversationMessages = data;
        listAdapter.setData(data);
        listAdapter.notifyDataSetChanged();
        lvConversationMessages.setSelection(data.size() - 1);

        // Mark loaded and unread conversation messages as read after displaying.
        for (ConversationMessage cm : conversationMessages) {
            if (!cm.isRead()) {
                databaseLoader.getGroupDBM().setMessageToRead(cm.getId());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<ConversationMessage>> loader) {
        listAdapter.setData(null);
    }


    /**
     * This method will be called when a updated conversation is posted to the EventBus.
     *
     * @param event The event containing the updated conversation.
     */
    public void onEventMainThread(BusEventConversationChange event) {
        Log.d(TAG, event.toString());
        menuItemClose.setActionView(null);
        menuItemOpen.setActionView(null);
        conversation = event.getConversation();
        databaseLoader.getGroupDBM().updateConversation(conversation);
        if (conversation.getClosed()) {
            etMessage.setVisibility(View.GONE);
            ivSend.setVisibility(View.GONE);
            menuItemClose.setVisible(false);
            menuItemEdit.setVisible(false);
            menuItemOpen.setVisible(true);
            finish();
        } else {
            etMessage.setVisibility(View.VISIBLE);
            ivSend.setVisibility(View.VISIBLE);
            menuItemClose.setVisible(true);
            menuItemEdit.setVisible(true);
            menuItemOpen.setVisible(false);
        }
    }

    /**
     * This method will be called when a BusEvent is posted to the EventBus.
     *
     * @param event The event containing an action.
     */
    public void onEventMainThread(BusEvent event) {
        Log.d(TAG, "EventBus: BusEvent");
        Log.d(TAG, event.toString());
        if (GroupAPI.DELETE_CONVERSATION.equals(event.getAction())) {
            databaseLoader.getGroupDBM().deleteConversation(conversation.getId());
            finish();
        }
    }

    /**
     * This method will be called when a conversation message is posted to the EventBus.
     *
     * @param conversationMessage The conversation message.
     */
    public void onEventMainThread(ConversationMessage conversationMessage) {
        Log.d(TAG, "EventBus: ConversationMessage");
        Log.d(TAG, conversationMessage.toString());
        // Hide progress bar and show send button again.
        ivSend.setVisibility(View.VISIBLE);
        pgrSending.setVisibility(View.GONE);
        // Clear text input field.
        etMessage.setText(null);
        // Store new conversation message in database.
        databaseLoader.getGroupDBM().storeConversationMessage(conversationMessage);
    }

    /**
     * This method will be called when a list of conversation messages is posted to the EventBus.
     *
     * @param event The bus event containing a list of conversation message objects.
     */
    public void onEventMainThread(BusEventConversationMessages event) {
        Log.d(TAG, event.toString());
        List<ConversationMessage> conversationMessages = event.getConversationMessages();
        // Store new conversation messages in database.
        for (ConversationMessage cm : conversationMessages) {
            databaseLoader.getGroupDBM().storeConversationMessage(cm);
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
        // Hide progress bar and show send button again.
        ivSend.setVisibility(View.VISIBLE);
        pgrSending.setVisibility(View.GONE);

        // Show appropriate error message.
        switch (serverError.getErrorCode()) {
            case CONNECTION_FAILURE:
                toast.setText(errorMessage);
                toast.show();
                break;
            case CONVERSATION_NOT_FOUND:
                databaseLoader.getGroupDBM().deleteConversation(conversation.getId());
                toast.setText(getString(R.string.conversation_created));
                toast.show();
        }
    }
}
