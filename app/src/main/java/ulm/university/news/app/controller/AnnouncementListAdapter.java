package ulm.university.news.app.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Announcement;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class AnnouncementListAdapter extends ArrayAdapter<Announcement> {
    public AnnouncementListAdapter(Context context, int resource) {
        super(context, resource);
    }

    public AnnouncementListAdapter(Context context, int resource, List<Announcement> announcements) {
        super(context, resource, announcements);
    }

    /**
     * Updates the data of the AnnouncementListAdapter.
     *
     * @param data The updated announcement list.
     */
    public void setData(List<Announcement> data) {
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
            convertView = vi.inflate(R.layout.announcement_list_item, parent, false);
        }

        Announcement announcement = getItem(position);

        if (announcement != null) {
            TextView tvFirstLine = (TextView) convertView.findViewById(R.id.announcement_list_item_tv_title);
            TextView tvSecondLine = (TextView) convertView.findViewById(R.id.announcement_list_item_tv_text);
            TextView tvThirdLine = (TextView) convertView.findViewById(R.id.announcement_list_item_tv_date);

            // Format the date for output.
            DateTimeFormatter dtfOut = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm");

            tvFirstLine.setText(announcement.getTitle());
            tvSecondLine.setText(announcement.getText());
            tvThirdLine.setText(dtfOut.print(announcement.getCreationDate()));
        }
        return convertView;
    }
}
