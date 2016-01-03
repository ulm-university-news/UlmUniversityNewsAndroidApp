package ulm.university.news.app.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Group;
import ulm.university.news.app.data.enums.GroupType;
import ulm.university.news.app.util.Util;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class GroupListAdapter extends ArrayAdapter<Group> {

    public GroupListAdapter(Context context, int resource) {
        super(context, resource);
    }

    public GroupListAdapter(Context context, int resource, List<Group> groups) {
        super(context, resource, groups);
    }

    /**
     * Updates the data of the GroupListAdapter.
     *
     * @param data The updated group list.
     */
    public void setData(List<Group> data) {
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
            convertView = vi.inflate(R.layout.group_list_item, parent, false);
        }

        Group group = getItem(position);

        if (group != null) {
            TextView tvName = (TextView) convertView.findViewById(R.id.group_list_item_tv_name);
            TextView tvType = (TextView) convertView.findViewById(R.id.group_list_item_tv_type);
            TextView tvTerm = (TextView) convertView.findViewById(R.id.group_list_item_tv_term);
            TextView tvIcon = (TextView) convertView.findViewById(R.id.group_list_item_tv_icon);
            TextView tvNew = (TextView) convertView.findViewById(R.id.group_list_item_tv_new);

            String typeName;
            // Set appropriate group name and symbol.
            if (GroupType.TUTORIAL.equals(group.getGroupType())) {
                typeName = getContext().getString(R.string.group_tutorial_name);
                tvIcon.setText(getContext().getString(R.string.group_tutorial_symbol));
            } else {
                typeName = getContext().getString(R.string.group_working_name);
                tvIcon.setText(getContext().getString(R.string.group_working_symbol));
            }

            // Set appropriate group symbol.
            if (group.isGroupAdmin(Util.getInstance(getContext()).getUserId())) {
                tvIcon.setBackgroundResource(R.drawable.circle_group_admin);
            } else {
                tvIcon.setBackgroundResource(R.drawable.circle_group);
            }

            tvName.setText(group.getName());
            tvType.setText(typeName);
            tvTerm.setText(group.getTerm());

            // TODO Show number of unread conversation messages.
            /*
            Integer number = group.getNumberOfUnreadMessages();
            if (number != null && number > 0) {
                if (number > 99) {
                    tvNew.setText("" + 99);
                } else {
                    tvNew.setText(number.toString());
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
