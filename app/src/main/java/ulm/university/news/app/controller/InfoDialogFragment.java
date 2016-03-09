package ulm.university.news.app.controller;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

/**
 * A dialog which shows some sort of information. There is only an ok button to close the dialog.
 *
 * @author Matthias Mak
 */
public class InfoDialogFragment extends AppCompatDialogFragment {

    public static final String DIALOG_TITLE = "dialogTitle";
    public static final String DIALOG_TEXT = "dialogText";

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
                        // Do nothing.
                    }
                });
        // Create the AlertDialog object and return it.
        return builder.create();
    }
}
