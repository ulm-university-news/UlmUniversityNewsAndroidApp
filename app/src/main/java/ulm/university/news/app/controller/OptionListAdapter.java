package ulm.university.news.app.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Option;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class OptionListAdapter extends ArrayAdapter<Option> {
    /** This classes tag for logging. */
    private static final String TAG = "OptionListAdapter";

    private boolean multipleChoice;
    private int singleSelectedPosition = -1;
    private HashSet<Integer> multipleSelectedPositions;

    public OptionListAdapter(Context context, int resource, boolean multipleChoice) {
        super(context, resource);
        this.multipleChoice = multipleChoice;

        // TODO If already voted, preselect options.
        singleSelectedPosition = -1;
        multipleSelectedPositions = new HashSet<>();
    }

    public OptionListAdapter(Context context, int resource, List<Option> ballots) {
        super(context, resource, ballots);
    }

    /**
     * Updates the data of the OptionListAdapter.
     *
     * @param data The updated option list.
     */
    public void setData(List<Option> data) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            convertView = vi.inflate(R.layout.option_list_item, parent, false);
        }

        Option option = getItem(position);

        if (option != null) {
            CheckBox chkText = (CheckBox) convertView.findViewById(R.id.option_list_item_chk_text);
            final RadioButton rbText = (RadioButton) convertView.findViewById(R.id.option_list_item_rb_text);
            TextView tvCount = (TextView) convertView.findViewById(R.id.option_list_item_tv_count);

            // TODO If already voted, preselect options.

            if (multipleChoice) {
                chkText.setText(option.getText());
                chkText.setVisibility(View.VISIBLE);
                rbText.setVisibility(View.GONE);
                chkText.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            multipleSelectedPositions.add(position);
                        } else {
                            multipleSelectedPositions.remove(position);
                        }
                    }
                });
            } else {
                rbText.setText(option.getText());
                chkText.setVisibility(View.GONE);
                rbText.setVisibility(View.VISIBLE);
                rbText.setChecked(position == singleSelectedPosition);
                rbText.setTag(position);
                rbText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        singleSelectedPosition = (Integer) view.getTag();
                        notifyDataSetChanged();
                    }
                });
            }

            // TODO Show user votes as count?
        }
        return convertView;
    }

    public Option getSingleSelectedOption() {
        return getItem(singleSelectedPosition);
    }

    public List<Option> getMultipleSelectedOptions() {
        List<Option> selectedOptions = new ArrayList<>();

        for (Integer i : multipleSelectedPositions) {
            selectedOptions.add(getItem(i));
        }

        return selectedOptions;
    }
}
