package ulm.university.news.app.controller;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import ulm.university.news.app.R;

/**
 * A dialog which asks to decide weather to actually unsubscribe the channel or not.
 *
 * @author Matthias Mak
 */
public class UnsubscribeDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.channel_unsubscribe_dialog_title)
                .setMessage(R.string.channel_unsubscribe_dialog_text)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Unsubscribe channel.
                        listener.onDialogPositiveClick();
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
