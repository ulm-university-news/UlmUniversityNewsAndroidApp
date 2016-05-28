package ulm.university.news.app.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Conversation;
import ulm.university.news.app.data.ConversationMessage;
import ulm.university.news.app.data.User;
import ulm.university.news.app.manager.database.UserDatabaseManager;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class ConversationListAdapter extends ArrayAdapter<Conversation> {

    public ConversationListAdapter(Context context, int resource) {
        super(context, resource);
    }

    public ConversationListAdapter(Context context, int resource, List<Conversation> conversations) {
        super(context, resource, conversations);
    }

    /**
     * Updates the data of the ConversationListAdapter.
     *
     * @param data The updated conversation list.
     */
    public void setData(List<Conversation> data) {
        clear();
        if (data != null) {
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
            convertView = vi.inflate(R.layout.conversation_list_item, parent, false);
        }

        Conversation conversation = getItem(position);

        if (conversation != null) {
            TextView tvName = (TextView) convertView.findViewById(R.id.conversation_list_item_tv_name);
            TextView tvAdmin = (TextView) convertView.findViewById(R.id.conversation_list_item_tv_admin);
            TextView tvCount = (TextView) convertView.findViewById(R.id.conversation_list_item_tv_count);
            ImageView ivIcon = (ImageView) convertView.findViewById(R.id.conversation_list_item_iv_icon);
            TextView tvNew = (TextView) convertView.findViewById(R.id.conversation_list_item_tv_new);

            // Set user name of the conversation admin.
            User user = new UserDatabaseManager(convertView.getContext()).getUser(conversation.getAdmin());
            String adminName = "Unknown";
            if (user != null) {
                adminName = user.getName();
            }

            // Set appropriate conversation symbol.
            ivIcon.setImageResource(GroupController.getConversationIcon(conversation, convertView.getContext()));

            tvName.setText(conversation.getTitle());
            tvAdmin.setText(adminName);

            // Show number of unread conversation messages.
            List<ConversationMessage> conversationMessages = conversation.getConversationMessages();
            int number = 0;
            if (conversationMessages != null) {
                number = conversationMessages.size();
            }
            if (number > 0) {
                if (number > 99) {
                    tvNew.setText(String.valueOf(99));
                } else {
                    tvNew.setText(number);
                }
                tvNew.setVisibility(View.VISIBLE);
            } else {
                tvNew.setVisibility(View.GONE);
            }
        }
        return convertView;
    }
}
