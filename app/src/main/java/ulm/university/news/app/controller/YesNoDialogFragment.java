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
            listener = (DialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            // The target fragment doesn't implement the interface, throw exception.
            throw new ClassCastException(getTargetFragment().toString() + " must implement DialogListener.");
        }
    }
}
