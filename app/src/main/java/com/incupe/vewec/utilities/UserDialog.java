package com.incupe.vewec.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.incupe.vewec.R;

public class UserDialog {
	/**
	 * Show a dialog that warns the user there are unsaved changes that will be lost
	 * if they continue leaving the editor.
	 */
	public static void showUnsavedChangesDialog(
			Context context, DialogInterface.OnClickListener discardButtonClickListener) {
		showDialog(context,
				"",
				context.getString(R.string.unsaved_changes_dialog_msg),
				context.getString(R.string.discard),
				context.getString(R.string.keep_editing),
				discardButtonClickListener,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// User clicked the "Keep editing" button, so dismiss the dialog
						// and continue editing
						if (dialog != null) {
							dialog.dismiss();
						}
					}
				},
				null);
	}

	public static void showDeleteConfirmationDialog(
			Context context, String message, DialogInterface.OnClickListener deleteListener) {
		showDialog(context,
				"",
				message,
				context.getString(R.string.delete),
				context.getString(R.string.cancel),
				deleteListener, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User clicked the "Cancel" button, so dismiss the dialog
						// and continue editing.
						if (dialog != null) {
							dialog.dismiss();
						}
					}
				},
				null);
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

	public static void showDialog(Context context, String title, String message,
								  String positiveButtonText, String negativeButtonText,
								  DialogInterface.OnClickListener positiveListener,
								  DialogInterface.OnClickListener negativeListener,
								  DialogInterface.OnDismissListener dismissListener) {
		// Create an AlertDialog.Builder and set the message, and click listeners
		// for the positive and negative buttons on the dialog.
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton(positiveButtonText, positiveListener);
		builder.setNegativeButton(negativeButtonText, negativeListener);
		builder.setOnDismissListener(dismissListener);

		// Create and show the AlertDialog
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}
}
