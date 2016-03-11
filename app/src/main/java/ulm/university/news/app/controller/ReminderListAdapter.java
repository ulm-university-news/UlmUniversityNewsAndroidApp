package ulm.university.news.app.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Reminder;
import ulm.university.news.app.util.Util;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class ReminderListAdapter extends ArrayAdapter<Reminder> {
    public ReminderListAdapter(Context context, int resource) {
        super(context, resource);
    }

    public ReminderListAdapter(Context context, int resource, List<Reminder> reminders) {
        super(context, resource, reminders);
    }

    /**
     * Updates the data of the ReminderListAdapter.
     *
     * @param data The updated reminder list.
     */
    public void setData(List<Reminder> data) {
        clear();
        if (data != null) {
            /*
            // DESC
            for (int i = 0; i < data.size(); i++) {
                add(data.get(i));
            }
            */
            // ASC
            for (int i = data.size() - 1; i >= 0; i--) {
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
            convertView = vi.inflate(R.layout.reminder_list_item, parent, false);
        }
        
        Reminder reminder = getItem(position);

        if (reminder != null) {
            TextView tvTitle = (TextView) convertView.findViewById(R.id.reminder_list_item_tv_title);
            TextView tvText = (TextView) convertView.findViewById(R.id.reminder_list_item_tv_text);
            TextView tvDate = (TextView) convertView.findViewById(R.id.reminder_list_item_tv_date);

            tvTitle.setText(reminder.getTitle());
            tvText.setText(reminder.getText());
            // Compute the date on which the reminder gets fired for the next time.
            String nextDateText;
            reminder.computeFirstNextDate();
            if (reminder.isIgnore()) {
                // If ignored, compute new next date.
                reminder.computeNextDate();
            }
            if (reminder.isExpired()) {
                nextDateText = getContext().getString(R.string.reminder_expired);
            } else {
                nextDateText = Util.getInstance(getContext()).getFormattedDateLong(reminder.getNextDate());
                nextDateText = String.format(getContext().getString(R.string.reminder_next_date_on), nextDateText);
            }
            tvDate.setText(nextDateText);
        }

        return convertView;
    }
}
