package com.incupe.vewec.fragments;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.incupe.vewec.R;
import com.incupe.vewec.data.OdometerContract;
import com.incupe.vewec.data.RefuelContract.RefuelEntry;
import com.incupe.vewec.utilities.DateUtilities;
import com.incupe.vewec.utilities.SetupViews;
import com.incupe.vewec.utilities.UserDialog;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RefuelEditorFragment extends Fragment {
	private Uri _currentUri;

	private Spinner _spinnerVehicle;
	private EditText _editDate;
	private Spinner _spinnerFuelType;
	private EditText _editPrice;
	private EditText _editVolume;
	private CheckBox _cbFullTank;
	private EditText _editOdometer;

	private boolean _userEnteredPrice = false;

	private int _editVehicleId = -1;
	private List<Integer> _vehicleIds;
	private Calendar _calendarOdometer;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
							 @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		View view = inflater.inflate(R.layout.fragment_refuel_editor,
				container, false);
		Intent intent = requireActivity().getIntent();
		_currentUri = intent.getData();

		_calendarOdometer = DateUtilities
				.getCalendarAtMidnight(Calendar.getInstance());

		_spinnerVehicle = view.findViewById(R.id.spinner_vehicle);
		_spinnerFuelType = view.findViewById(R.id.spinner_fuel_type);
		_editDate = view.findViewById(R.id.edit_date);
		_editPrice = view.findViewById(R.id.edit_price);
		_editVolume = view.findViewById(R.id.edit_volume);
		_cbFullTank = view.findViewById(R.id.cb_full_tank);
		_editOdometer = view.findViewById(R.id.edit_odometer);

		_vehicleIds = SetupViews.setupVehicleRegNoSpinner(requireContext(), _spinnerVehicle);
		ArrayAdapter<CharSequence> fuelTypeSpinnerAdapter = ArrayAdapter
				.createFromResource(requireContext(),
						R.array.array_fuel_type,
						android.R.layout.simple_spinner_item);

		fuelTypeSpinnerAdapter.setDropDownViewResource(
				android.R.layout.simple_dropdown_item_1line);
		_spinnerFuelType.setAdapter(fuelTypeSpinnerAdapter);

		if (_vehicleIds.isEmpty()) {
			UserDialog.showDialog(getActivity(), "",
					getString(R.string.no_vehicle_found),
					new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							if (dialog != null) {
								requireActivity().finish();
							}
						}
					});
		} else {
			// show default date
			_editDate.setText(DateUtilities.dateToStringDate(_calendarOdometer.getTime()));

			_editOdometer.setFilters(new InputFilter[]{
					new InputFilter.LengthFilter(String.valueOf(OdometerContract.OdometerEntry.DISTANCE_MAX).length())
			});

			// if user is editing, then pre select in spinner
			final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
				@Override
				public void onDateSet(DatePicker view, int year, int monthOfYear,
									  int dayOfMonth) {
					_calendarOdometer.set(Calendar.YEAR, year);
					_calendarOdometer.set(Calendar.MONTH, monthOfYear);
					_calendarOdometer.set(Calendar.DAY_OF_MONTH, dayOfMonth);

					_editDate.setText(DateUtilities.dateToStringDate(_calendarOdometer.getTime()));
				}
			};
			if (_editVehicleId != -1) {
				_spinnerVehicle.setSelection(_vehicleIds.indexOf(_editVehicleId));
				_spinnerVehicle.setEnabled(false);
			}

			_editDate.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					DatePickerDialog datePickerDialog =
							new DatePickerDialog(requireActivity(), dateSetListener,
									_calendarOdometer.get(Calendar.YEAR), _calendarOdometer.get(Calendar.MONTH),
									_calendarOdometer.get(Calendar.DAY_OF_MONTH));
					// user cannot fill in future odometer. sounds logical right?
					datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
					datePickerDialog.show();
				}
			});

			if (_currentUri == null) {
				requireActivity().invalidateOptionsMenu();
			} else {
				// TODO: get existing data from DB
				_spinnerVehicle.setEnabled(false);
				initVehicle(fuelTypeSpinnerAdapter);
			}
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
				processSaveRefuel();
				return true;
			case R.id.action_delete:
				UserDialog.showDeleteConfirmationDialog(requireContext(),
						getString(R.string.are_you_sure),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (_currentUri != null) {
									deleteRefuel();
								}
							}
						});
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void processSaveRefuel() {
		String err = "";
		if (TextUtils.isEmpty(_editPrice.getText().toString())) {
			err += "\n- " + getString(R.string.price_not_provided);
		}
		if (TextUtils.isEmpty(_editVolume.getText().toString())) {
			err += "\n- " + getString(R.string.volume_not_provided);
		}
		if (TextUtils.isEmpty(_editOdometer.getText().toString())) {
			err += "\n- " + getString(R.string.odometer_not_provided);
		}
		if (err.length() > 0) {
			UserDialog.showDialog(requireContext(),
					"Failed to save.",
					err,
					null);
			return;
		}

		final int vehicle = _vehicleIds.get(_spinnerVehicle.getSelectedItemPosition());
		final String fuelType = _spinnerFuelType.getSelectedItem().toString();
		final long date = DateUtilities.getCalendarAtMidnight(_calendarOdometer).getTimeInMillis();
		final double price = Double.parseDouble(_editPrice.getText().toString());
		final double volume = Double.parseDouble(_editVolume.getText().toString());
		final int odometer = Integer.parseInt(_editOdometer.getText().toString());
		final boolean isFullTank = _cbFullTank.isChecked();
		boolean saveSuccess = true;

		ContentValues values = new ContentValues();
		values.put(RefuelEntry.COLUMN_VEHICLE, vehicle);
		values.put(RefuelEntry.COLUMN_DATE, date);
		values.put(RefuelEntry.COLUMN_PRICE, price);
		values.put(RefuelEntry.COLUMN_VOLUME, volume);
		values.put(RefuelEntry.COLUMN_ODOMETER, odometer);
		values.put(RefuelEntry.COLUMN_FUEL_TYPE, fuelType);
		values.put(RefuelEntry.COLUMN_IS_FULL_TANK, isFullTank);

		if (_currentUri == null) {
			// insert
			Uri newUri = requireContext().getContentResolver().insert(RefuelEntry.CONTENT_URI, values);
			saveSuccess = newUri != null;
		} else {
			// update
			int rowsAffected = requireContext().getContentResolver()
					.update(_currentUri,
							values,
							null,
							null);
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

	private void deleteRefuel() {
		if (_currentUri != null) {
			int rowsDeleted = requireContext().getContentResolver()
					.delete(_currentUri, null, null);
			if (rowsDeleted == 0) {
				Toast.makeText(requireContext(),
						requireContext().getString(R.string.error_has_occurred),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(requireContext(),
						requireContext().getString(R.string.refuel_delete),
						Toast.LENGTH_SHORT).show();
			}
		}
		requireActivity().finish();
	}

	private void initVehicle(ArrayAdapter<CharSequence> fuelTypeSpinnerAdapter) {
		Cursor cursor = requireContext().getContentResolver().query(_currentUri,
				RefuelEntry.FULL_PROJECTION,
				null,
				null,
				null);

		if (cursor != null) {
			if (cursor.moveToNext()) {
				int vehicleId = cursor.getInt(cursor
						.getColumnIndexOrThrow(RefuelEntry.COLUMN_VEHICLE));
				_spinnerVehicle.setSelection(_vehicleIds.indexOf(vehicleId));
				_calendarOdometer.setTime(new Date(cursor.getLong(cursor
						.getColumnIndexOrThrow(RefuelEntry.COLUMN_DATE))));
				_editDate.setText(DateUtilities.dateToStringDate(_calendarOdometer.getTime()));
				_editOdometer.setText(String.valueOf(cursor.getInt(cursor
						.getColumnIndexOrThrow(RefuelEntry.COLUMN_ODOMETER))));
				_spinnerFuelType.setSelection(fuelTypeSpinnerAdapter.getPosition(cursor
						.getString(cursor.getColumnIndexOrThrow(RefuelEntry.COLUMN_FUEL_TYPE))));
				_editPrice.setText(String.valueOf(cursor.getDouble(cursor
						.getColumnIndexOrThrow(RefuelEntry.COLUMN_PRICE))));
				_editVolume.setText(String.valueOf(cursor.getDouble(cursor
						.getColumnIndexOrThrow(RefuelEntry.COLUMN_VOLUME))));
				_cbFullTank.setChecked(cursor.getInt(cursor
						.getColumnIndexOrThrow(RefuelEntry.COLUMN_IS_FULL_TANK)) == 1);
			}

			cursor.close();
		}
	}
}
