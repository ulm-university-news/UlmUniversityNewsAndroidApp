package ulm.university.news.app.controller;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class TimePickerDialogFragment extends AppCompatDialogFragment implements android.app.TimePickerDialog
        .OnTimeSetListener {

    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";

    // Use this instance of the interface to deliver action events.
    TimePickerListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use previous selected values if existing.
        int hour = getArguments().getInt(HOUR);
        int minute = getArguments().getInt(MINUTE);
        if (hour == 0 && minute == 0) {
            // Use the current time as the default values for the picker.
            final Calendar c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        }

        // Create a new instance of TimePickerDialogFragment and return it.
        return new android.app.TimePickerDialog(getActivity(), this, hour, minute, DateFormat
                .is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user.
        listener.onTimeSet(getTag(), view, hourOfDay, minute);
    }

    @Override
    public void onAttach(Activity activity) {
        // Override the Fragment.onAttach() method to instantiate the DialogListener.
        super.onAttach(activity);
        // Verify that the host fragment implements the callback interface.
        try {
            // Instantiate the DialogListener so we can send events to the host.
            listener = (TimePickerListener) getActivity();
        } catch (ClassCastException e) {
            // The target fragment doesn't implement the interface, throw exception.
            throw new ClassCastException(getTargetFragment().toString() + " must implement TimePickerListener.");
        }
    }
}
