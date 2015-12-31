package ulm.university.news.app.controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ulm.university.news.app.R;
import ulm.university.news.app.data.ChannelDetail;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class ChannelDetailListAdapter extends BaseAdapter {
    /** This classes tag for logging. */
    private static final String TAG = "ChannelDetailListAdapt";
    /** The channel detail data. */
    private List<ChannelDetail> channelDetails;

    @Override
    public int getCount() {
        return channelDetails.size();
    }

    @Override
    public ChannelDetail getItem(int position) {
        return channelDetails.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(parent.getContext());
            convertView = vi.inflate(R.layout.channel_detail_list_item, parent, false);
        }

        ChannelDetail item = getItem(position);

        if (item != null) {
            TextView tvName = (TextView) convertView.findViewById(R.id.channel_detail_list_item_tv_title);
            TextView tvValue = (TextView) convertView.findViewById(R.id.channel_detail_list_item_tv_value);
            ImageView ivIcon = (ImageView) convertView.findViewById(R.id.channel_detail_list_item_iv_icon);

            tvName.setText(item.getName());
            tvValue.setText(item.getValue());
            ivIcon.setImageResource(item.getResource());
        }
        return convertView;
    }

    /**
     * Sets the adapters channel detail data to the given one.
     *
     * @param channelDetails The detail data of this channel.
     */
    public void setChannelDetails(List<ChannelDetail> channelDetails) {
        this.channelDetails = channelDetails;
    }
}
