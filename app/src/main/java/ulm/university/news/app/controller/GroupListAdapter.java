package ulm.university.news.app.controller;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Group;
import ulm.university.news.app.data.enums.GroupType;
import ulm.university.news.app.manager.database.GroupDatabaseManager;

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
            ImageView ivIcon = (ImageView) convertView.findViewById(R.id.group_list_item_iv_icon);
            ImageView ivNew = (ImageView) convertView.findViewById(R.id.group_list_item_iv_new);

            String typeName;
            // Set appropriate group name and symbol.
            if (GroupType.TUTORIAL.equals(group.getGroupType())) {
                typeName = getContext().getString(R.string.group_tutorial_name);
            } else {
                typeName = getContext().getString(R.string.group_working_name);
            }

            // Set appropriate group symbol.
            ivIcon.setImageResource(GroupController.getGroupIcon(group, convertView.getContext()));

            if (group.getNewEvents() != null && group.getNewEvents()) {
                // Reset new events flag in database.
                new GroupDatabaseManager(getContext()).setGroupNewEvents(group.getId(), false);
                // Tint new icon grey. Then show the icon.
                ivNew.setColorFilter(ContextCompat.getColor(convertView.getContext(), R.color.grey));
                ivNew.setVisibility(View.VISIBLE);
            } else {
                ivNew.setVisibility(View.GONE);
            }

            tvName.setText(group.getName());
            tvType.setText(typeName);
            tvTerm.setText(group.getTerm());
        }
        return convertView;
    }
}
