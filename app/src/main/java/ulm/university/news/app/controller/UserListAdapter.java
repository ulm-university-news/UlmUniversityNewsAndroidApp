package ulm.university.news.app.controller;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ulm.university.news.app.R;
import ulm.university.news.app.data.User;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class UserListAdapter extends ArrayAdapter<User> {

    private int currentUserId;
    private int groupAdminId;

    public UserListAdapter(Context context, int resource, int groupAdminId) {
        super(context, resource);
        this.groupAdminId = groupAdminId;
        currentUserId = -1;
    }

    public UserListAdapter(Context context, int resource, int groupAdminId, List<User> users) {
        super(context, resource, users);
        this.groupAdminId = groupAdminId;
        currentUserId = -1;
    }

    /**
     * Updates the data of the UserListAdapter.
     *
     * @param data The updated user list.
     */
    public void setData(List<User> data) {
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
            convertView = vi.inflate(R.layout.user_list_item, parent, false);
        }

        final User user = getItem(position);

        if (user != null) {
            TextView tvName = (TextView) convertView.findViewById(R.id.user_list_item_tv_name);
            ImageView ivRemove = (ImageView) convertView.findViewById(R.id.user_list_item_iv_remove);
            ImageView ivPerson = (ImageView) convertView.findViewById(R.id.user_list_item_iv_person);

            tvName.setText(user.getName());

            if(groupAdminId == user.getId()){
                ivPerson.setColorFilter(ContextCompat.getColor(convertView.getContext(), R.color.grey));
                ivPerson.setImageResource(R.drawable.ic_person_outline_black_36dp);
                ivRemove.setVisibility(View.GONE);
            }else{
                ivPerson.setColorFilter(ContextCompat.getColor(convertView.getContext(), R.color.grey));
                ivRemove.setColorFilter(ContextCompat.getColor(convertView.getContext(), R.color.grey));
                ivRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeUser(user.getId());
                    }
                });
            }
        }
        return convertView;
    }

    private void removeUser(int userId) {
        currentUserId = userId;
        // Show delete group dialog.
        YesNoDialogFragment dialog = new YesNoDialogFragment();
        Bundle args = new Bundle();
        args.putString(YesNoDialogFragment.DIALOG_TITLE, getContext().getString(R.string
                .group_member_remove_dialog_title));
        args.putString(YesNoDialogFragment.DIALOG_TEXT, getContext().getString(R.string
                .group_member_remove_dialog_text));
        dialog.setArguments(args);
        dialog.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), YesNoDialogFragment
                .DIALOG_MEMBER_REMOVE);
    }

    public int getCurrentUserId() {
        return currentUserId;
    }
}
