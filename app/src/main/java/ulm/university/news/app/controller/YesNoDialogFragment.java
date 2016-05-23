package ulm.university.news.app.controller;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

/**
 * A dialog which asks to decide weather to actually perform an action or not.
 *
 * @author Matthias Mak
 */
public class YesNoDialogFragment extends AppCompatDialogFragment {

    public static final String DIALOG_TITLE = "dialogTitle";
    public static final String DIALOG_TEXT = "dialogText";
    public static final String DIALOG_CHANNEL_UNSUBSCRIBE = "dialogChannelUnsubscribe";
    public static final String DIALOG_CHANNEL_DELETE = "dialogChannelDelete";
    public static final String DIALOG_GROUP_LEAVE = "dialogGroupLeave";
    public static final String DIALOG_GROUP_DELETE = "dialogGroupDelete";
    public static final String DIALOG_REMINDER_DELETE = "dialogReminderDelete";
    public static final String DIALOG_LEAVE_PAGE_UP = "dialogLeavePageUp";
    public static final String DIALOG_LEAVE_PAGE_BACK = "dialogLeavePageBack";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String dialogTitle = getArguments().getString(DIALOG_TITLE);
        String dialogText = getArguments().getString(DIALOG_TEXT);
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(dialogTitle)
                .setMessage(dialogText)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Unsubscribe channel.
                        listener.onDialogPositiveClick(getTag());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Canceled. Do nothing.
                    }
                });
        // Create the AlertDialog object and return it.
        return builder.create();
    }

    // Use this instance of the interface to deliver action events.
    DialogListener listener;

    @Override
    public void onAttach(Activity activity) {
        // Override the Fragment.onAttach() method to instantiate the DialogListener.
        super.onAttach(activity);
        // Verify that the host fragment implements the callback interface.
        try {
            // Instantiate the DialogListener so we can send events to the host.
            if (getTargetFragment() != null) {
                listener = (DialogListener) getTargetFragment();
            } else {
                listener = (DialogListener) getActivity();
            }
        } catch (ClassCastException e) {
            // The target fragment doesn't implement the interface, throw exception.
            if (getTargetFragment() != null) {
                throw new ClassCastException(getTargetFragment().toString() + " must implement DialogListener.");
            } else {
                throw new ClassCastException(getActivity().toString() + " must implement DialogListener.");
            }
        }
    }
}
