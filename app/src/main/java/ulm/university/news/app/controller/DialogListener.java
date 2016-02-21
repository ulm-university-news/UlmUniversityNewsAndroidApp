package ulm.university.news.app.controller;

/**
 * The activity that creates an instance of this dialog fragment must implement this interface in order to receive
 * event callbacks. Each method passes the DialogFragment in case the host needs to query it.
 *
 * @author Matthias Mak
 */
public interface DialogListener {
    /**
     * The dialogs positive button was clicked. This method handles the button click.
     *
     * @param tag - The tag which identifies the dialog.
     */
    void onDialogPositiveClick(String tag);
}
