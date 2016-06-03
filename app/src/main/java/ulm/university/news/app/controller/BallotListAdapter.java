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
import ulm.university.news.app.data.Ballot;
import ulm.university.news.app.data.User;
import ulm.university.news.app.manager.database.UserDatabaseManager;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class BallotListAdapter extends ArrayAdapter<Ballot> {

    public BallotListAdapter(Context context, int resource) {
        super(context, resource);
    }

    public BallotListAdapter(Context context, int resource, List<Ballot> ballots) {
        super(context, resource, ballots);
    }

    /**
     * Updates the data of the BallotListAdapter.
     *
     * @param data The updated ballot list.
     */
    public void setData(List<Ballot> data) {
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
            convertView = vi.inflate(R.layout.ballot_list_item, parent, false);
        }

        Ballot ballot = getItem(position);

        if (ballot != null) {
            TextView tvName = (TextView) convertView.findViewById(R.id.ballot_list_item_tv_title);
            TextView tvAdmin = (TextView) convertView.findViewById(R.id.ballot_list_item_tv_admin);
            TextView tvCount = (TextView) convertView.findViewById(R.id.ballot_list_item_tv_count);
            ImageView ivIcon = (ImageView) convertView.findViewById(R.id.ballot_list_item_iv_icon);
            TextView tvNew = (TextView) convertView.findViewById(R.id.ballot_list_item_tv_new);

            // Set user name of the ballot admin.
            User user = new UserDatabaseManager(convertView.getContext()).getUser(ballot.getAdmin());
            String adminName = "Unknown";
            if (user != null) {
                adminName = user.getName();
            }

            // Set appropriate conversation symbol.
            ivIcon.setImageResource(GroupController.getBallotIcon(ballot, convertView.getContext()));

            tvName.setText(ballot.getTitle());
            tvAdmin.setText(adminName);

            /*
            TODO Show exclamation mark for due ballots!?
            // Show number of unread conversation messages.
            Integer number = ballot.getNumberOfUnreadConversationMessages();
            if (number == null) {
                number = 0;
            }
            if (number > 0) {
                if (number > 99) {
                    tvNew.setText(String.valueOf(99));
                } else {
                    tvNew.setText(String.valueOf(number));
                }
                tvNew.setVisibility(View.VISIBLE);
            } else {
                tvNew.setVisibility(View.GONE);
            }
            */
        }
        return convertView;
    }
}
