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
import ulm.university.news.app.data.Channel;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class ChannelListAdapter extends ArrayAdapter<Channel> {

    public ChannelListAdapter(Context context, int resource) {
        super(context, resource);
    }

    public ChannelListAdapter(Context context, int resource, List<Channel> channels) {
        super(context, resource, channels);
    }

    /**
     * Updates the data of the ChannelListAdapter.
     *
     * @param data The updated channel list.
     */
    public void setData(List<Channel> data) {
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
            convertView = vi.inflate(R.layout.channel_list_item, parent, false);
        }

        Channel channel = getItem(position);

        if (channel != null) {
            TextView tvName = (TextView) convertView.findViewById(R.id.channel_list_item_tv_name);
            TextView tvType = (TextView) convertView.findViewById(R.id.channel_list_item_tv_type);
            TextView tvTerm = (TextView) convertView.findViewById(R.id.channel_list_item_tv_term);
            TextView tvNew = (TextView) convertView.findViewById(R.id.channel_list_item_tv_new);
            ImageView ivIcon = (ImageView) convertView.findViewById(R.id.channel_list_item_iv_icon);

            String typeName;
            switch (channel.getType()) {
                case LECTURE:
                    typeName = getContext().getString(R.string.channel_type_lecture);
                    break;
                case EVENT:
                    typeName = getContext().getString(R.string.channel_type_event);
                    break;
                case SPORTS:
                    typeName = getContext().getString(R.string.channel_type_sports);
                    break;
                case STUDENT_GROUP:
                    typeName = getContext().getString(R.string.channel_type_student_group);
                    break;
                default:
                    typeName = getContext().getString(R.string.channel_type_other);

            }

            tvName.setText(channel.getName());
            tvType.setText(typeName);
            tvTerm.setText(channel.getTerm());

            // Show number of unread announcements.
            Integer number = channel.getNumberOfUnreadAnnouncements();
            if (number != null && number > 0) {
                if (number > 99) {
                    tvNew.setText(String.valueOf(99));
                } else {
                    tvNew.setText(number.toString());
                }
                tvNew.setVisibility(View.VISIBLE);
            } else {
                tvNew.setVisibility(View.GONE);
            }

            // Set channel icon.
            ivIcon.setImageResource(ChannelController.getChannelIcon(channel));
        }
        return convertView;
    }
}
