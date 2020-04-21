package com.example.carmaintenance.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.example.carmaintenance.R;

public class UserDialog {
	/**
	 * Show a dialog that warns the user there are unsaved changes that will be lost
	 * if they continue leaving the editor.
	 */
	public static void showUnsavedChangesDialog(
			Context context, DialogInterface.OnClickListener discardButtonClickListener) {
		// Create an AlertDialog.Builder and set the message, and click listeners
		// for the positive and negative buttons on the dialog.
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(R.string.unsaved_changes_dialog_msg);
		builder.setPositiveButton(R.string.discard, discardButtonClickListener);
		builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User clicked the "Keep editing" button, so dismiss the dialog
				// and continue editing
				if (dialog != null) {
					dialog.dismiss();
				}
			}
		});

		// Create and show the AlertDialog
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}

	public static void showDeleteConfirmationDialog(
			Context context, String message, DialogInterface.OnClickListener deleteListener) {
		// Create an AlertDialog.Builder and set the message, and click listeners
		// for the postivie and negative buttons on the dialog.
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message);
		builder.setPositiveButton(R.string.delete, deleteListener);
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User clicked the "Cancel" button, so dismiss the dialog
				// and continue editing the pet.
				if (dialog != null) {
					dialog.dismiss();
				}
			}
		});

		// Create and show the AlertDialog
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}

	public static void showDialog(Context context, String title, String message,
								  DialogInterface.OnDismissListener dismissListener) {
		// Create an AlertDialog.Builder and set the message, and click listeners
		// for the positive and negative buttons on the dialog.
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				if (dialog != null) {
					dialog.dismiss();
				}
			}
		});
		builder.setOnDismissListener(dismissListener);

		// Create and show the AlertDialog
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}
}
