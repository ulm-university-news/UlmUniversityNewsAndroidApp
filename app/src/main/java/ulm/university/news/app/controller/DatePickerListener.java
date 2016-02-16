package ulm.university.news.app.controller;

import android.widget.DatePicker;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public interface DatePickerListener {
    void onDateSet(String tag, DatePicker view, int year, int month, int day);
}
