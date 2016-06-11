package ulm.university.news.app.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Option;
import ulm.university.news.app.util.Util;

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
    private Button btnVote;

    public OptionListAdapter(Context context, int resource, boolean multipleChoice, Button btnVote) {
        super(context, resource);
        this.multipleChoice = multipleChoice;
        this.btnVote = btnVote;

        // TODO If already voted, preselect options.
        singleSelectedPosition = -1;
        multipleSelectedPositions = new HashSet<>();
        updateVoteButton();
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
            List<Integer> voters;
            for (int i = 0; i < data.size(); i++) {
                add(data.get(i));

                voters = data.get(i).getVoters();
                for (Integer voter : voters) {
                    if (multipleChoice) {
                        if (voter.intValue() == Util.getInstance(getContext()).getLocalUser().getId()) {
                            multipleSelectedPositions.add(i);
                        }
                    } else {
                        if (voter.intValue() == Util.getInstance(getContext()).getLocalUser().getId()) {
                            singleSelectedPosition = i;
                            break;
                        }
                    }
                }
            }
            updateVoteButton();
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
                for (Integer i : multipleSelectedPositions) {
                    if (i == position) {
                        chkText.setChecked(true);
                    }
                }
                chkText.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            multipleSelectedPositions.add(position);
                        } else {
                            multipleSelectedPositions.remove(position);
                        }
                        updateVoteButton();
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
                        updateVoteButton();
                    }
                });
            }

            // TODO Show user votes as count?
        }
        return convertView;
    }

    private void updateVoteButton() {
        List<Option> allItems = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) {
            allItems.add(getItem(i));
        }

        if (multipleChoice) {
            boolean sameSelection = true;
            List<Option> selected = getMultipleSelectedOptions();
            if (selected != null && !selected.isEmpty()) {
                List<Option> myOptions = GroupController.getMyOptions(getContext(), allItems);
                if (myOptions.size() != selected.size()) {
                    sameSelection = false;
                } else {
                    for (Option selectedOption : selected) {
                        if (!myOptions.contains(selectedOption)) {
                            sameSelection = false;
                        }
                    }
                }
                if (!sameSelection) {
                    btnVote.setAlpha(1f);
                    btnVote.setEnabled(true);
                } else {
                    btnVote.setAlpha(.5f);
                    btnVote.setEnabled(false);
                }
            } else {
                btnVote.setAlpha(.5f);
                btnVote.setEnabled(false);
            }
        } else {
            Option selected = getSingleSelectedOption();
            if (selected != null && !selected.equals(GroupController.getMyOption(getContext(), allItems))) {
                btnVote.setAlpha(1f);
                btnVote.setEnabled(true);
            } else {
                btnVote.setAlpha(.5f);
                btnVote.setEnabled(false);
            }
        }
    }

    public Option getSingleSelectedOption() {
        if (singleSelectedPosition > -1) {
            return getItem(singleSelectedPosition);
        } else {
            return null;
        }
    }

    public List<Option> getMultipleSelectedOptions() {
        List<Option> selectedOptions = new ArrayList<>();

        for (Integer i : multipleSelectedPositions) {
            selectedOptions.add(getItem(i));
        }

        return selectedOptions;
    }
}
