package com.incupe.vewec.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.incupe.vewec.R;
import com.incupe.vewec.data.CustomMaintenanceItemContract.CustomMaintenanceItemEntry;
import com.incupe.vewec.data.OdometerContract.OdometerEntry;
import com.incupe.vewec.utilities.UserDialog;

public class CustomMaintenanceItemEditorFragment extends DialogFragment {
	static final String ARG_NAME = "name";
	static final String ARG_INSPECT_REPLACE = "inspect_replace";
	static final String ARG_DISTANCE = "distance";
	static final String ARG_DURATION = "duration";

	private EditText _editName;
	private Spinner _spinnerInspectReplace;
	private EditText _editDistance;
	private EditText _editDuration;

	static CustomMaintenanceItemEditorFragment newInstance() {
		return new CustomMaintenanceItemEditorFragment();
	}

	static CustomMaintenanceItemEditorFragment newInstance(
			String name, int inspectReplace, int distance, int duration) {
		Bundle bundle = new Bundle();
		bundle.putSerializable(ARG_NAME, name);
		bundle.putSerializable(ARG_INSPECT_REPLACE, inspectReplace);
		bundle.putSerializable(ARG_DISTANCE, distance);
		bundle.putSerializable(ARG_DURATION, duration);

		CustomMaintenanceItemEditorFragment fragment =
				new CustomMaintenanceItemEditorFragment();

		fragment.setArguments(bundle);
		return fragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		View v = View.inflate(getActivity(),
				R.layout.dialog_custom_maintenance_item, null);

		_editName = v.findViewById(R.id.edit_item_name);
		_spinnerInspectReplace = v.findViewById(R.id.spinner_inspect_replace);
		_editDistance = v.findViewById(R.id.edit_distance_interval);
		_editDuration = v.findViewById(R.id.edit_duration_interval);

		_editName.setFilters(new InputFilter[]{
				new InputFilter.LengthFilter(CustomMaintenanceItemEntry.ITEM_NAME_MAX_LENGTH)
		});
		_editDistance.setFilters(new InputFilter[]{
				new InputFilter.LengthFilter(String.valueOf(OdometerEntry.DISTANCE_MAX).length())
		});
		_editDuration.setFilters(new InputFilter[]{
				// maximum 99 months. more than that too long la. car die d lo
				new InputFilter.LengthFilter(2)
		});

		ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
				requireContext(),
				R.array.array_inspect_replace_options,
				android.R.layout.simple_spinner_item);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		_spinnerInspectReplace.setAdapter(spinnerAdapter);

		initFields();

		// positive button listener must be null here.
		// if not, will always dismiss dialog when clicked
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
					int inspectReplace = CustomMaintenanceItemEntry.INSPECT_VALUE;
					String strDistance = _editDistance.getText().toString().trim();
					String strDuration = _editDuration.getText().toString().trim();
					int distance = TextUtils.isEmpty(strDistance) ?
							0 : Integer.parseInt(strDistance);
					int duration = TextUtils.isEmpty(strDuration) ?
							0 : Integer.parseInt(strDuration);

					if (_spinnerInspectReplace.getSelectedItemPosition() == 0) {
						inspectReplace = CustomMaintenanceItemEntry.REPLACE_VALUE;
					}

					if (TextUtils.isEmpty(name)) {
						UserDialog.showDialog(requireContext(),
								"",
								getString(R.string.item_name_required),
								null);
						return;
					}
					if (distance == 0 && duration == 0) {
						UserDialog.showDialog(requireContext(),
								"",
								"At least one interval is required.",
								null);
						return;
					}
					sendResults(name, inspectReplace, distance, duration);
					dialog.dismiss();
				}
			});
		}
	}

	private void initFields() {
		if (getArguments() == null) {
			return; // if no arguments then exit function
		}
		String name = (String) getArguments().getSerializable(ARG_NAME);
		int inspectReplace = (int) getArguments().getSerializable(ARG_INSPECT_REPLACE);
		int distance = (int) getArguments().getSerializable(ARG_DISTANCE);
		int duration = (int) getArguments().getSerializable(ARG_DURATION);

		_editName.setText(name);

		if (inspectReplace == CustomMaintenanceItemEntry.INSPECT_VALUE) {
			_spinnerInspectReplace.setSelection(1);
		} else if (inspectReplace == CustomMaintenanceItemEntry.REPLACE_VALUE) {
			_spinnerInspectReplace.setSelection(0);
		}

		if (distance != 0)
			_editDistance.setText(String.valueOf(distance));
		if (duration != 0)
			_editDuration.setText(String.valueOf(duration));
	}

	private void sendResults(String name, int inspectReplace,
							 int distance, int duration) {
		if (getTargetFragment() == null) {
			return;
		}
		Intent intent = new Intent();
		intent.putExtra(ARG_NAME, name);
		intent.putExtra(ARG_INSPECT_REPLACE, inspectReplace);
		intent.putExtra(ARG_DISTANCE, distance);
		intent.putExtra(ARG_DURATION, duration);

		getTargetFragment().onActivityResult(getTargetRequestCode(),
				Activity.RESULT_OK, intent);
	}
}
