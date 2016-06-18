package ulm.university.news.app.controller;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Ballot;
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

    private int singleSelectedPosition = -1;
    private HashSet<Integer> multipleSelectedPositions;
    private Button btnVote;
    private Ballot ballot;
    private OptionFragment optionFragment;
    private int currentOptionId;

    public OptionListAdapter(Context context, int resource, Ballot ballot, Button btnVote,
                             OptionFragment optionFragment) {
        super(context, resource);
        this.btnVote = btnVote;
        this.ballot = ballot;
        this.optionFragment = optionFragment;

        // If already voted, preselect options.
        singleSelectedPosition = -1;
        multipleSelectedPositions = new HashSet<>();
        updateVoteButton();
    }

    public OptionListAdapter(Context context, int resource, List<Option> options) {
        super(context, resource, options);
    }

    /**
     * Updates the data of the OptionListAdapter.
     *
     * @param data The updated option list.
     */
    public void setData(List<Option> data) {
        // Reset previous data.
        clear();
        multipleSelectedPositions.clear();
        singleSelectedPosition = -1;
        if (data != null) {
            // Set new data.
            List<Integer> voters;
            for (int i = 0; i < data.size(); i++) {
                add(data.get(i));

                voters = data.get(i).getVoters();
                for (Integer voter : voters) {
                    if (ballot.getMultipleChoice()) {
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

        final Option option = getItem(position);

        if (option != null) {
            CheckBox chkText = (CheckBox) convertView.findViewById(R.id.option_list_item_chk_text);
            final RadioButton rbText = (RadioButton) convertView.findViewById(R.id.option_list_item_rb_text);
            ImageView ivDelete = (ImageView) convertView.findViewById(R.id.option_list_item_iv_delete);

            // If local user is admin, show delete option buttons.
            if (ballot.isBallotAdmin(Util.getInstance(convertView.getContext()).getLocalUser().getId())) {
                ivDelete.setVisibility(View.VISIBLE);
                ivDelete.setColorFilter(ContextCompat.getColor(convertView.getContext(), R.color.grey));
                ivDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteBallotOption(option.getId());
                    }
                });
            }

            if (ballot.getClosed()) {
                chkText.setEnabled(false);
                rbText.setEnabled(false);
                ivDelete.setEnabled(false);
                ivDelete.setAlpha(0.5f);
            } else {
                chkText.setEnabled(true);
                rbText.setEnabled(true);
                ivDelete.setEnabled(true);
                ivDelete.setAlpha(1f);
            }

            // If already voted, preselect options.
            if (ballot.getMultipleChoice()) {
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
        }
        return convertView;
    }

    private void deleteBallotOption(int optionId) {
        currentOptionId = optionId;
        // Show delete group dialog.
        YesNoDialogFragment dialog = new YesNoDialogFragment();
        Bundle args = new Bundle();
        args.putString(YesNoDialogFragment.DIALOG_TITLE, getContext().getString(R.string
                .option_delete_dialog_title));
        args.putString(YesNoDialogFragment.DIALOG_TEXT, getContext().getString(R.string
                .option_delete_dialog_text));
        dialog.setArguments(args);
        dialog.setTargetFragment(optionFragment, 0);
        dialog.show(optionFragment.getFragmentManager(), YesNoDialogFragment.DIALOG_OPTION_DELETE);
    }

    private void updateVoteButton() {
        if (ballot.getClosed()) {
            // Disable vote button if ballot is closed.
            btnVote.setAlpha(.5f);
            btnVote.setEnabled(false);
            return;
        }

        List<Option> allItems = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) {
            allItems.add(getItem(i));
        }

        if (ballot.getMultipleChoice()) {
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

    public int getCurrentOptionId() {
        return currentOptionId;
    }
}
