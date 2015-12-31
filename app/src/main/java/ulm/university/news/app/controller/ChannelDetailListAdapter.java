package ulm.university.news.app.controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import ulm.university.news.app.R;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class ChannelDetailListAdapter extends BaseAdapter {

    private ArrayList channelData;

    public ChannelDetailListAdapter() {
        channelData = new ArrayList();
    }

    public ChannelDetailListAdapter(Map<String, String> map) {
        channelData = new ArrayList();
        channelData.addAll(map.entrySet());
    }

    @Override
    public int getCount() {
        return channelData.size();
    }

    @Override
    public Map.Entry<String, String> getItem(int position) {
        return (Map.Entry) channelData.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO implement you own logic with ID
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(parent.getContext());
            convertView = vi.inflate(R.layout.channel_detail_list_item, parent, false);
        }

        Map.Entry<String, String> item = getItem(position);

        if (item != null) {
            TextView tvFirstLine = (TextView) convertView.findViewById(R.id.channel_detail_list_item_tv_first_title);
            TextView tvSecondLine = (TextView) convertView.findViewById(R.id.channel_detail_list_item_tv_content);

            tvFirstLine.setText(item.getKey());
            tvSecondLine.setText(item.getValue());
            setItemIcon(convertView, item.getKey());
        }
        return convertView;
    }

    private void setItemIcon(View view, String key) {
        // TODO Add nice and appropriate icons.
        ImageView ivIcon = (ImageView) view.findViewById(R.id.channel_detail_list_item_iv_icon);
        if (key.equals(view.getContext().getString(R.string.channel_description))) {
            ivIcon.setImageResource(android.R.drawable.ic_menu_info_details);
        } else if (key.equals(view.getContext().getString(R.string.channel_dates))) {
            ivIcon.setImageResource(android.R.drawable.ic_menu_my_calendar);
        } else if (key.equals(view.getContext().getString(R.string.channel_locations))) {
            ivIcon.setImageResource(android.R.drawable.ic_menu_mylocation);
        } else if (key.equals(view.getContext().getString(R.string.channel_contacts))) {
            ivIcon.setImageResource(android.R.drawable.ic_menu_call);
        } else if (key.equals(view.getContext().getString(R.string.channel_website))) {
            ivIcon.setImageResource(android.R.drawable.ic_menu_add);
        } else if (key.equals(view.getContext().getString(R.string.channel_creation_date))) {
            ivIcon.setImageResource(android.R.drawable.ic_menu_today);
        }else if (key.equals(view.getContext().getString(R.string.channel_modification_date))) {
            ivIcon.setImageResource(android.R.drawable.ic_menu_today);
        }else if (key.equals(view.getContext().getString(R.string.channel_term))) {
            ivIcon.setImageResource(android.R.drawable.ic_menu_directions);
        }else if (key.equals(view.getContext().getString(R.string.channel_type))) {
            ivIcon.setImageResource(android.R.drawable.ic_menu_help);
        }
    }

    public void setChannelData(Map<String, String> map) {
        channelData.addAll(map.entrySet());
    }
}
