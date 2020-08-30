package com.incupe.vewec.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.incupe.vewec.R;
import com.incupe.vewec.data.MaintenanceItemContract.MaintenanceItemEntry;
import com.incupe.vewec.data.OdometerContract.OdometerEntry;
import com.incupe.vewec.utilities.UserDialog;

public class MaintenanceItemEditorFragment extends Fragment {
	private Uri _currentUri;

	private EditText _editItemName;
	private Spinner _spinnerInspectReplace;
	private EditText _editDistanceInterval;
	private EditText _editDurationInterval;
	private EditText _editFirstDistance;
	private EditText _editFirstDuration;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
							 @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		View view = inflater.inflate(
				R.layout.fragment_maintenance_item_editor,
				container,
				false);

		Intent intent = requireActivity().getIntent();
		_currentUri = intent.getData();

		_editItemName = view.findViewById(R.id.edit_item_name);
		_spinnerInspectReplace = view.findViewById(R.id.spinner_inspect_replace);
		_editDistanceInterval = view.findViewById(R.id.edit_distance_interval);
		_editDurationInterval = view.findViewById(R.id.edit_duration_interval);
		_editFirstDistance = view.findViewById(R.id.edit_first_distance);
		_editFirstDuration = view.findViewById(R.id.edit_first_duration);
		TextView txtFirstDistance = view.findViewById(R.id.label_first_distance);
		TextView txtFirstDuration = view.findViewById(R.id.label_first_duration);

		ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
				requireContext(),
				R.array.array_inspect_replace_options,
				android.R.layout.simple_spinner_item);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		_spinnerInspectReplace.setAdapter(spinnerAdapter);

		_editItemName.setFilters(new InputFilter[]{
				new InputFilter.LengthFilter(MaintenanceItemEntry.ITEM_NAME_MAX_LENGTH)
		});
		_editDistanceInterval.setFilters(new InputFilter[]{
				new InputFilter.LengthFilter(String.valueOf(OdometerEntry.DISTANCE_MAX).length())
		});
		_editDurationInterval.setFilters(new InputFilter[]{
				// maximum 99 months. more than that too long la. car die d lo
				new InputFilter.LengthFilter(2)
		});
		_editFirstDistance.setFilters(new InputFilter[]{
				new InputFilter.LengthFilter(String.valueOf(OdometerEntry.DISTANCE_MAX).length())
		});
		_editFirstDuration.setFilters(new InputFilter[]{
				// maximum 99 months. more than that too long la. car die d lo
				new InputFilter.LengthFilter(2)
		});
		txtFirstDistance.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				UserDialog.showDialog(requireContext(),
						requireContext().getString(R.string.first_distance),
						requireContext().getString(R.string.first_distance_explanation),
						null);
			}
		});
		txtFirstDuration.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				UserDialog.showDialog(requireContext(),
						requireContext().getString(R.string.first_duration),
						requireContext().getString(R.string.first_duration_explanation),
						null);
			}
		});

		if (_currentUri == null) {
			requireActivity().invalidateOptionsMenu();
		} else {
			initMaintenanceItem();
		}

		return view;
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		requireActivity().getMenuInflater().inflate(R.menu.menu_editor, menu);
	}

	/**
	 * This method is called after invalidateOptionsMenu(), so that the
	 * menu can be updated (some menu items can be hidden or made visible).
	 */
	@Override
	public void onPrepareOptionsMenu(@NonNull Menu menu) {
		super.onPrepareOptionsMenu(menu);
		// If this is new, hide the "Delete" menu item.
		if (_currentUri == null) {
			MenuItem menuItem = menu.findItem(R.id.action_delete);
			menuItem.setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_save:
				saveMaintenanceItem();
				return true;
			case R.id.action_delete:
				UserDialog.showDeleteConfirmationDialog(requireContext(),
						getString(R.string.are_you_sure),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (_currentUri != null) {
									deleteMaintenanceItem();
								}
							}
						});
				return true;
			case android.R.id.home:
				requireActivity().finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void saveMaintenanceItem() {
		String itemName = _editItemName.getText().toString().trim();
		int inspectReplace = MaintenanceItemEntry.REPLACE_VALUE;
		String strDistanceInterval = _editDistanceInterval.getText().toString().trim();
		String strDurationInterval = _editDurationInterval.getText().toString().trim();
		String strFirstDistance = _editFirstDistance.getText().toString().trim();
		String strFirstDuration = _editFirstDuration.getText().toString().trim();

		int distanceInterval = 0;
		int durationInterval = 0;
		int firstDistance = 0;
		int firstDuration = 0;

		if (_spinnerInspectReplace.getSelectedItemPosition() == 1) {
			inspectReplace = MaintenanceItemEntry.INSPECT_VALUE;
		}
		if (!TextUtils.isEmpty(strDistanceInterval)) {
			distanceInterval = Integer.parseInt(strDistanceInterval);
		}
		if (!TextUtils.isEmpty(strDurationInterval)) {
			durationInterval = Integer.parseInt(strDurationInterval);
		}
		if (!TextUtils.isEmpty(strFirstDistance)) {
			firstDistance = Integer.parseInt(strFirstDistance);
		}
		if (!TextUtils.isEmpty(strFirstDuration)) {
			firstDuration = Integer.parseInt(strFirstDuration);
		}

		boolean saveSuccess = true;

		SharedPreferences sharedPreferences =
				PreferenceManager.getDefaultSharedPreferences(requireContext());
		int vehicleId = sharedPreferences.getInt(requireContext()
				.getString(R.string.pref_session_vehicle), 0);

		if (_currentUri == null) {
			// insert new
			Uri newUri = MaintenanceItemEntry.INSERT_MAINTENANCE_iTEM(
					requireContext(),
					itemName,
					inspectReplace,
					vehicleId,
					firstDistance,
					firstDuration,
					distanceInterval,
					durationInterval
			);
			saveSuccess = newUri != null;
		} else {
			// update existing
			int rowsAffected = MaintenanceItemEntry.UPDATE_MAINTENANCE_ITEM(
					requireContext(),
					_currentUri,
					itemName,
					inspectReplace,
					vehicleId,
					firstDistance,
					firstDuration,
					distanceInterval,
					durationInterval
			);
			saveSuccess = rowsAffected > 0;
		}
		if (saveSuccess) {
			Toast.makeText(requireContext(),
					requireContext().getString(R.string.saved_successfully),
					Toast.LENGTH_SHORT).show();
			requireActivity().finish();
		} else {
			Toast.makeText(requireContext(),
					requireContext().getString(R.string.error_has_occurred),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void deleteMaintenanceItem() {
		if (_currentUri != null) {
			int rowsDeleted = requireContext().getContentResolver()
					.delete(_currentUri,
							null,
							null);
			if (rowsDeleted == 0) {
				Toast.makeText(requireContext(),
						requireContext().getString(R.string.error_has_occurred),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(requireContext(),
						requireContext().getString(R.string.item_deleted),
						Toast.LENGTH_SHORT).show();
			}
		}
		requireActivity().finish();
	}

	private void initMaintenanceItem() {
		Cursor cursor = requireContext().getContentResolver().query(
				_currentUri,
				MaintenanceItemEntry.FULL_PROJECTION,
				null,
				null,
				null);

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				String itemName = cursor.getString(cursor
						.getColumnIndexOrThrow(MaintenanceItemEntry.COLUMN_ITEM));
				int inspectReplace = cursor.getInt(cursor
						.getColumnIndexOrThrow(MaintenanceItemEntry.COLUMN_INSPECT_REPLACE));
				int durationInterval = cursor.getInt(cursor
						.getColumnIndexOrThrow(MaintenanceItemEntry.COLUMN_DURATION_INTERVAL));
				int distanceInterval = cursor.getInt(cursor
						.getColumnIndexOrThrow(MaintenanceItemEntry.COLUMN_DISTANCE_INTERVAL));
				int firstDistance = cursor.getInt(cursor
						.getColumnIndexOrThrow(MaintenanceItemEntry.COLUMN_FIRST_DISTANCE));
				int firstDuration = cursor.getInt(cursor
						.getColumnIndexOrThrow(MaintenanceItemEntry.COLUMN_FIRST_DURATION));

				_editItemName.setText(itemName);
				_spinnerInspectReplace.setSelection(
						inspectReplace == MaintenanceItemEntry.REPLACE_VALUE ? 0 : 1);
				_editDurationInterval.setText(String.valueOf(durationInterval));
				_editDistanceInterval.setText(String.valueOf(distanceInterval));
				_editFirstDuration.setText(String.valueOf(firstDuration));
				_editFirstDistance.setText(String.valueOf(firstDistance));
			}

			cursor.close();
		}
	}
}
