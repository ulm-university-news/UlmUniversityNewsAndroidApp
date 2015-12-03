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
import ulm.university.news.app.data.Lecture;

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

    public void setData(List<Channel> data) {
        clear();
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                add(data.get(i));
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            convertView = vi.inflate(R.layout.channel_list_item, parent, false);
            // convertView = vi.inflate(R.layout.channel_list_item, parent, false);
        }

        Channel channel = getItem(position);

        if (channel != null) {
            TextView tvFirstLine = (TextView) convertView.findViewById(R.id.channel_list_view_tv_first_line);
            TextView tvSecondLine = (TextView) convertView.findViewById(R.id.channel_list_view_tv_second_line);
            ImageView ivIcon = (ImageView) convertView.findViewById(R.id.channel_list_view_iv_icon);

            tvFirstLine.setText(channel.getName());
            tvSecondLine.setText(channel.getTerm());
            // tvSecondLine.setText(channel.getCreationDate().toString());

            // Set appropriate channel icon.
            switch (channel.getType()){
                case LECTURE:
                    Lecture lecture = (Lecture) channel;
                    tvSecondLine.setText(lecture.getLecturer());
                    // Set icon with appropriate faculty color.
                    switch (lecture.getFaculty()){
                        case ENGINEERING_COMPUTER_SCIENCE_PSYCHOLOGY:
                            ivIcon.setImageResource(R.drawable.icon_channel_lecture_informatics);
                            break;
                        case MATHEMATICS_ECONOMICS:
                            ivIcon.setImageResource(R.drawable.icon_channel_lecture_math);
                            break;
                        case MEDICINES:
                            ivIcon.setImageResource(R.drawable.icon_channel_lecture_medicine);
                            break;
                        case NATURAL_SCIENCES:
                            ivIcon.setImageResource(R.drawable.icon_channel_lecture_science);
                            break;
                    }
                    break;
                case EVENT:
                    ivIcon.setImageResource(R.drawable.icon_channel_event);
                    break;
                case SPORTS:
                    ivIcon.setImageResource(R.drawable.icon_channel_sports);
                    break;
                case STUDENT_GROUP:
                    ivIcon.setImageResource(R.drawable.icon_channel_student_group);
                    break;
                case OTHER:
                    ivIcon.setImageResource(R.drawable.icon_channel_other);
                    break;
            }
        }
        return convertView;
    }
}
