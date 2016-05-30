package ulm.university.news.app.controller;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ulm.university.news.app.R;
import ulm.university.news.app.data.ConversationMessage;
import ulm.university.news.app.data.User;
import ulm.university.news.app.manager.database.UserDatabaseManager;
import ulm.university.news.app.util.Util;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class ConversationMessageListAdapter extends ArrayAdapter<ConversationMessage> {
    public ConversationMessageListAdapter(Context context, int resource) {
        super(context, resource);
    }

    public ConversationMessageListAdapter(Context context, int resource, List<ConversationMessage> conversationMessages) {
        super(context, resource, conversationMessages);
    }

    /**
     * Updates the data of the ConversationMessageListAdapter.
     *
     * @param data The updated conversation message list.
     */
    public void setData(List<ConversationMessage> data) {
        clear();
        if (data != null) {
            /*
            // Ascending.
            for (int i = data.size() - 1; i >= 0; i--) {
                add(data.get(i));
            }
            */
            // Descending.
            for (int i = 0; i < data.size(); i++) {
                add(data.get(i));
            }
        }
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            convertView = vi.inflate(R.layout.conversation_message_list_item, parent, false);
        }

        ConversationMessage conversationMessage = getItem(position);

        if (conversationMessage != null) {
            TextView tvAuthor = (TextView) convertView.findViewById(R.id.conversation_message_list_item_tv_author);
            TextView tvText = (TextView) convertView.findViewById(R.id.conversation_message_list_item_tv_text);
            TextView tvDate = (TextView) convertView.findViewById(R.id.conversation_message_list_item_tv_date);

            User user = new UserDatabaseManager(getContext()).getUser(conversationMessage.getAuthorUser());
            String author = getContext().getString(R.string.general_unknown);
            if (user != null) {
                author = user.getName();
            }
            tvAuthor.setText(author);
            tvText.setText(conversationMessage.getText());
            tvDate.setText(Util.getInstance(getContext()).getFormattedDateShort(conversationMessage.getCreationDate()));

            // Mark unread conversation messages.
            if (!conversationMessage.isRead()) {
                convertView.setBackgroundColor(Color.parseColor("#f2f2f2"));
            } else {
                convertView.setBackgroundColor(Color.parseColor("#ffffff"));
            }
        }
        return convertView;
    }
}
