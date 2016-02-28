package ulm.university.news.app.controller;

import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ulm.university.news.app.R;
import ulm.university.news.app.data.ResourceDetail;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class ResourceDetailListAdapter extends BaseAdapter {
    /** This classes tag for logging. */
    private static final String TAG = "ResourceDetailListAdapt";
    /** The resource detail data. */
    private List<ResourceDetail> resourceDetails;

    @Override
    public int getCount() {
        return resourceDetails.size();
    }

    @Override
    public ResourceDetail getItem(int position) {
        return resourceDetails.get(position);
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

        ResourceDetail item = getItem(position);

        if (item != null) {
            TextView tvName = (TextView) convertView.findViewById(R.id.channel_detail_list_item_tv_title);
            TextView tvValue = (TextView) convertView.findViewById(R.id.channel_detail_list_item_tv_value);
            ImageView ivIcon = (ImageView) convertView.findViewById(R.id.channel_detail_list_item_iv_icon);
            ivIcon.setColorFilter(ContextCompat.getColor(convertView.getContext(), R.color.grey));

            tvName.setText(item.getName());
            tvValue.setText(item.getValue());
            ivIcon.setImageResource(item.getResource());
        }
        return convertView;
    }

    /**
     * Sets the adapters resource detail data to the given one.
     *
     * @param resourceDetails The detail data of this resource.
     */
    public void setResourceDetails(List<ResourceDetail> resourceDetails) {
        this.resourceDetails = resourceDetails;
    }
}
