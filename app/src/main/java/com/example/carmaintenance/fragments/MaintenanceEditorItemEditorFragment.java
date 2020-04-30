package com.example.carmaintenance.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.carmaintenance.R;
import com.example.carmaintenance.data.CustomMaintenanceItemContract.CustomMaintenanceItemEntry;
import com.example.carmaintenance.data.MaintenanceDetailsContract.MaintenanceDetailsEntry;
import com.example.carmaintenance.utilities.UserDialog;

import java.text.DecimalFormat;

public class MaintenanceEditorItemEditorFragment extends DialogFragment {
	static final String EXTRA_NAME = "name";
	static final String EXTRA_PRICE = "price";

	private EditText _editName;
	private EditText _editPrice;
	private DecimalFormat _decimalFormat = new DecimalFormat("0.00");

	private View.OnFocusChangeListener _focusChangeFormatMoney =
			new View.OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus) {
						EditText editText = (EditText) v;
						String price = editText.getText().toString();
						if (!TextUtils.isEmpty(price)) {
							String formattedPrice = _decimalFormat.format(Double.valueOf(price));
							editText.setText(formattedPrice);
						}
					}
				}
			};


	static MaintenanceEditorItemEditorFragment newInstance() {
		return new MaintenanceEditorItemEditorFragment();
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		View v = View.inflate(getActivity(), R.layout.dialog_maintenance_item, null);

		_editName = v.findViewById(R.id.edit_item_name);
		_editPrice = v.findViewById(R.id.edit_price);
		_editPrice.setOnFocusChangeListener(_focusChangeFormatMoney);

		_editName.setFilters(new InputFilter[]{
				new InputFilter.LengthFilter(
						CustomMaintenanceItemEntry.ITEM_NAME_MAX_LENGTH)
		});
		_editPrice.setFilters(new InputFilter[]{
				new InputFilter.LengthFilter(MaintenanceDetailsEntry.PRICE_MAX_LENGTH)
		});

		return new AlertDialog.Builder(getActivity())
				.setView(v)
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok, null)
				.create();
	}

	/*
	 * Prevent DialogFragment from dismissing when OK button is clicked because
	 * need to validate input first
	 * https://stackoverflow.com/questions/13746412/prevent-dialogfragment-from-dismissing-when-button-is-clicked
	 * */
	@Override
	public void onStart() {
		super.onStart();

		final AlertDialog dialog = (AlertDialog) getDialog();
		if (dialog != null) {
			Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String name = _editName.getText().toString().trim();
					String strPrice = _editPrice.getText().toString().trim();
					double price = 0;

					if (TextUtils.isEmpty(name)) {
						UserDialog.showDialog(requireContext(),
								"",
								getString(R.string.item_name_required),
								null);
						return;
					}
					if (!TextUtils.isEmpty(strPrice)) {
						price = Double.parseDouble(strPrice);
					}

					sendResults(name, price);
					dialog.dismiss();
				}
			});
		}
	}

	private void sendResults(String name, double price) {
		if (getTargetFragment() == null) {
			return;
		}

		Intent intent = new Intent();
		intent.putExtra(EXTRA_NAME, name);
		intent.putExtra(EXTRA_PRICE, price);
		getTargetFragment().onActivityResult(getTargetRequestCode(),
				Activity.RESULT_OK, intent);
	}
}
