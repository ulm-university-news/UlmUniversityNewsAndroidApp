package ulm.university.news.app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Conversation;
import ulm.university.news.app.manager.database.GroupDatabaseManager;
import ulm.university.news.app.util.Util;

public class ConversationActivity extends AppCompatActivity implements DialogListener {
    /** This classes tag for logging. */
    private static final String TAG = "ConversationActivity";

    Conversation conversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set color scheme to conversation yellow.
        setTheme(R.style.UlmUniversity_Conversation);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        int groupId = getIntent().getIntExtra("groupId", 0);
        int conversationId = getIntent().getIntExtra("conversationId", 0);
        conversation = new GroupDatabaseManager(this).getConversation(conversationId);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_conversation_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(conversation.getTitle());
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (conversation.isAdmin(Util.getInstance(this).getLocalUser().getId()) && !conversation.getClosed()) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.activity_conversation_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
            case R.id.activity_conversation_menu_close:
                YesNoDialogFragment dialog = new YesNoDialogFragment();
                Bundle args = new Bundle();
                args.putString(YesNoDialogFragment.DIALOG_TITLE, getString(R.string.conversation_close));
                args.putString(YesNoDialogFragment.DIALOG_TEXT, getString(R.string.conversation_close_text));
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), YesNoDialogFragment.DIALOG_CONVERSATION_CLOSE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDialogPositiveClick(String tag) {
        if (tag.equals(YesNoDialogFragment.DIALOG_CONVERSATION_CLOSE)) {
            // TODO get dbm from loader.
            // TODO send close request to server.
            conversation.setClosed(true);
            new GroupDatabaseManager(this).updateConversation(conversation);
            finish();
        }
    }
}
