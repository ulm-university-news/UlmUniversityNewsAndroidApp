package ulm.university.news.app.controller;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.Field;

import ulm.university.news.app.R;

/**
 * A dialog which asks to decide weather to actually perform an action or not.
 *
 * @author Matthias Mak
 */
public class IntervalPickerDialogFragment extends AppCompatDialogFragment {

    public static final String INTERVAL_TYPE = "intervalType";
    public static final String INTERVAL = "interval";

    TextView tvInterval;
    Spinner spInterval;
    NumberPicker npInterval;
    private boolean isFirstPopup = true;

    // Use this instance of the interface to deliver action events.
    IntervalPickerListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String dialogTitle = getString(R.string.reminder_interval);
        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(dialogTitle)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onIntervalSet(getTag(), npInterval.getValue(), spInterval.getSelectedItemPosition(),
                                tvInterval.getText().toString());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Canceled. Do nothing.
                    }
                });

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_interval_picker, null);
        builder.setView(dialogView);
        initView(dialogView);

        // Create and return the AlertDialog object.
        return builder.create();
    }

    private void initView(View dialogView) {
        tvInterval = (TextView) dialogView.findViewById(R.id.dialog_interval_picker_tv_interval);
        spInterval = (Spinner) dialogView.findViewById(R.id.dialog_interval_picker_sp_unit);
        npInterval = (NumberPicker) dialogView.findViewById(R.id.dialog_interval_picker_np_interval);

        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.interval, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spInterval.setAdapter(adapter);
        // On first dialog popup show previous selected value.
        spInterval.setSelection(getArguments().getInt(INTERVAL_TYPE));

        spInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        npInterval.setEnabled(false);
                        npInterval.setMinValue(1);
                        npInterval.setMaxValue(1);
                        break;
                    case 1:
                        npInterval.setMinValue(1);
                        npInterval.setMaxValue(28);
                        npInterval.setEnabled(true);
                        break;
                    case 2:
                        npInterval.setMinValue(1);
                        npInterval.setMaxValue(4);
                        npInterval.setEnabled(true);
                        break;
                }
                // On first dialog popup show previous selected value.
                if (isFirstPopup) {
                    npInterval.setValue(1);
                    if (getArguments().getInt(INTERVAL) != 0) {
                        npInterval.setValue(getArguments().getInt(INTERVAL));
                    }
                    isFirstPopup = false;
                }
                setIntervalText(position, npInterval.getValue());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        setNumberPickerTextColor(npInterval, Color.BLACK);
        // Set divider color to current accent color.
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.colorAccent, typedValue, true);
        int color = typedValue.data;
        setNumberPickerDividerColor(npInterval, color);

        npInterval.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                setIntervalText(spInterval.getSelectedItemPosition(), newVal);
            }
        });
    }

    private void setIntervalText(int spPosition, int npValue) {
        switch (spPosition) {
            case 0:
                tvInterval.setText(getString(R.string.reminder_interval_once_text));
                break;
            case 1:
                if (npValue == 1) {
                    tvInterval.setText(getString(R.string.reminder_interval_one_day));
                } else {
                    tvInterval.setText(String.format(getString(R.string.reminder_interval_multiple_days), npValue));
                }
                break;
            case 2:
                if (npValue == 1) {
                    tvInterval.setText(getString(R.string.reminder_interval_one_week));
                } else {
                    tvInterval.setText(String.format(getString(R.string.reminder_interval_multiple_weeks), npValue));
                }
                break;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        // Override the Fragment.onAttach() method to instantiate the DialogListener.
        super.onAttach(activity);
        // Verify that the host fragment implements the callback interface.
        try {
            // Instantiate the DialogListener so we can send events to the host.
            listener = (IntervalPickerListener) getActivity();
        } catch (ClassCastException e) {
            // The target fragment doesn't implement the interface, throw exception.
            throw new ClassCastException(getTargetFragment().toString() + " must implement IntervalPickerListener.");
        }
    }

    private void setNumberPickerDividerColor(NumberPicker picker, int color) {
        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public void setNumberPickerTextColor(NumberPicker numberPicker, int color) {
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                try {
                    Field selectorWheelPaintField = numberPicker.getClass().getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint) selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText) child).setTextColor(color);
                    numberPicker.invalidate();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
