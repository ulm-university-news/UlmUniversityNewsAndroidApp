package ulm.university.news.app.controller;

import android.widget.TimePicker;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public interface TimePickerListener {
    void onTimeSet(String tag, TimePicker view, int hourOfDay, int minute);
}
