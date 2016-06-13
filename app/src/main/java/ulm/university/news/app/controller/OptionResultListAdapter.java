package ulm.university.news.app.controller;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Option;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class OptionResultListAdapter extends ArrayAdapter<Option> {
    /** This classes tag for logging. */
    private static final String TAG = "OptionResultListAdapter";

    private int totalNumberOfVotes;
    private boolean publicVotes;

    public OptionResultListAdapter(Context context, int resource, boolean publicVotes) {
        super(context, resource);
        this.publicVotes = publicVotes;
    }

    public OptionResultListAdapter(Context context, int resource, List<Option> options) {
        super(context, resource, options);
    }

    /**
     * Updates the data of the OptionListAdapter.
     *
     * @param data The updated option list.
     */
    public void setData(List<Option> data) {
        clear();
        totalNumberOfVotes = 0;
        if (data != null) {
            GroupController.sortOptions(data);
            for (int i = 0; i < data.size(); i++) {
                add(data.get(i));
                // Compute total number of votes.
                totalNumberOfVotes += data.get(i).getVoters().size();
            }
        }
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            convertView = vi.inflate(R.layout.option_result_list_item, parent, false);
        }

        Option option = getItem(position);

        if (option != null) {
            TextView tvText = (TextView) convertView.findViewById(R.id.option_result_list_item_tv_text);
            TextView tvVoters = (TextView) convertView.findViewById(R.id.option_result_list_item_tv_voters);
            TextView tvCount = (TextView) convertView.findViewById(R.id.option_result_list_item_tv_count);
            ProgressBar pgrChart = (ProgressBar) convertView.findViewById(R.id.option_result_list_item_pgr_chart);

            tvText.setText(option.getText());
            int numberOfVotes = option.getVoters().size();
            tvCount.setText(String.valueOf(numberOfVotes));
            int progress = Math.round(100 * (float) numberOfVotes / (float) totalNumberOfVotes);
            Log.d(TAG, "Progress: " + progress);
            pgrChart.setProgress(progress);

            if (publicVotes) {
                // Show the names of all voters.
                tvVoters.setText(GroupController.getVoterNames(getContext(), option.getVoters()));
                tvVoters.setVisibility(View.VISIBLE);
            }
        }
        return convertView;
    }
}
