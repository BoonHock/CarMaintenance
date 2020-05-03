package com.incupe.vewec;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.incupe.vewec.data.APP_MASTER_CONTRACT;
import com.incupe.vewec.data.OdometerContract;
import com.incupe.vewec.data.OdometerContract.OdometerEntry;
import com.incupe.vewec.utilities.DateUtilities;
import com.incupe.vewec.utilities.SetupViews;
import com.incupe.vewec.utilities.UserDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class OdometerEditorActivity extends AppCompatActivity {
	private Uri _currentUri;

	private Spinner _spinnerVehicle;
	private EditText _editDate;
	private EditText _editOdometer;

	private Calendar _calendarOdometer;
	List<Integer> _vehicleIds = new ArrayList<>();

	private DialogInterface.OnClickListener _discardButtonClickListener =
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					// User clicked "Discard" button, navigate to parent activity.
//								NavUtils.navigateUpFromSameTask(VehicleEditorActivity.this);
					finish();
				}
			};

	private DialogInterface.OnClickListener _closeDialog = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int id) {
			if (dialog != null) {
				dialog.dismiss();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_odometer_editor);

		// Load an ad into the AdMob banner view.
		AdView adView = findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);

		Intent intent = getIntent();
		_currentUri = intent.getData();

		_calendarOdometer = DateUtilities.getCalendarAtMidnight(Calendar
				.getInstance()); // default value

		_spinnerVehicle = findViewById(R.id.spinner_vehicle);
		_editDate = findViewById(R.id.edit_date);
		_editOdometer = findViewById(R.id.edit_odometer);

		_vehicleIds = SetupViews.setupVehicleRegNoSpinner(this, _spinnerVehicle);

		if (_vehicleIds.isEmpty()) {
			UserDialog.showDialog(this, "",
					getString(R.string.no_vehicle_found),
					new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							if (dialog != null) {
								finish();
							}
						}
					});
		}

		_editDate.setText(DateUtilities.dateToStringDate(_calendarOdometer.getTime()));

		if (_currentUri == null) {
			invalidateOptionsMenu();
		} else {
			_spinnerVehicle.setEnabled(false);
			initVehicle();
		}

		final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
								  int dayOfMonth) {
				_calendarOdometer.set(Calendar.YEAR, year);
				_calendarOdometer.set(Calendar.MONTH, monthOfYear);
				_calendarOdometer.set(Calendar.DAY_OF_MONTH, dayOfMonth);

				_editDate.setText(DateUtilities.dateToStringDate(_calendarOdometer.getTime()));
				getDistance();
			}
		};

		_editDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePickerDialog datePickerDialog =
						new DatePickerDialog(OdometerEditorActivity.this, dateSetListener,
								_calendarOdometer.get(Calendar.YEAR), _calendarOdometer.get(Calendar.MONTH),
								_calendarOdometer.get(Calendar.DAY_OF_MONTH));
				// user cannot fill in future odometer. sounds logical right?
				datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
				datePickerDialog.show();
			}
		});

		_spinnerVehicle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				getDistance();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		_editOdometer.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (!TextUtils.isEmpty(_editOdometer.getText().toString().trim())) {
					int distance = Integer.parseInt(_editOdometer.getText().toString().trim());
					if (distance > OdometerEntry.DISTANCE_MAX || distance < OdometerEntry.DISTANCE_MIN) {
						UserDialog.showDialog(OdometerEditorActivity.this, "",
								getString(R.string.odometer_input_too_large), null);
					}
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		_editOdometer.setFilters(new InputFilter[]{
				new InputFilter.LengthFilter(String.valueOf(OdometerEntry.DISTANCE_MAX).length())
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_editor, menu);
		return true;
	}

	/**
	 * This method is called after invalidateOptionsMenu(), so that the
	 * menu can be updated (some menu items can be hidden or made visible).
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		// If this is new, hide the "Delete" menu item.
		if (_currentUri == null) {
			MenuItem menuItem = menu.findItem(R.id.action_delete);
			menuItem.setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_save:
				saveOdometer();
				return true;
			case R.id.action_delete:
				UserDialog.showDeleteConfirmationDialog(
						this, getString(R.string.are_you_sure),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// User clicked the "Delete" button
								deleteOdometer();
							}
						});
				return true;
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void saveOdometer() {
		if (TextUtils.isEmpty(_editOdometer.getText().toString())) {
			UserDialog.showDialog(this, "",
					getString(R.string.odometer_not_provided),
					null);
			return;
		}

		int vehicle = getSelectedVehicleId();
		long date = getCalendarAtMidnight();

		int distance = Integer.parseInt(_editOdometer.getText().toString());

		Cursor cursor = getContentResolver().query(OdometerEntry.CONTENT_URI,
				OdometerEntry.FULL_PROJECTION,
				OdometerEntry.COLUMN_VEHICLE + "=? AND "
						+ OdometerEntry.COLUMN_DATE + "=?",
				new String[]{String.valueOf(vehicle), String.valueOf(date)},
				null);

		boolean isUpdate = false;

		if (_currentUri != null) {
			isUpdate = true;
		} else if (cursor != null) {
			if (cursor.moveToFirst()) {
				_currentUri = Uri.withAppendedPath(APP_MASTER_CONTRACT.BASE_CONTENT_URI,
						OdometerContract.PATH_ODOMETER + "/"
								+ cursor.getLong(cursor.getColumnIndexOrThrow(OdometerEntry._ID)));
				isUpdate = true;
			}
			cursor.close();
		}

		ContentValues values = new ContentValues();
		values.put(OdometerEntry.COLUMN_VEHICLE, vehicle);
		values.put(OdometerEntry.COLUMN_DATE, date);
		values.put(OdometerEntry.COLUMN_DISTANCE, distance);

		boolean saveSuccess;

		if (isUpdate) {
			int rowsAffected = getContentResolver().update(_currentUri,
					values, null, null);
			saveSuccess = rowsAffected != 0;
		} else {
			Uri newUri = getContentResolver().insert(OdometerEntry.CONTENT_URI, values);
			saveSuccess = newUri != null;
		}
		if (saveSuccess) {
			Toast.makeText(this, getString(R.string.saved_successfully),
					Toast.LENGTH_SHORT).show();
			finish();
		} else {
			Toast.makeText(this, getString(R.string.error_has_occurred),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void deleteOdometer() {
		if (_currentUri != null) {
			int rowsDeleted = getContentResolver()
					.delete(_currentUri, null, null);
			if (rowsDeleted == 0) {
				Toast.makeText(this, getString(R.string.error_has_occurred),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, getString(R.string.odometer_deleted),
						Toast.LENGTH_SHORT).show();
			}
		}
		finish();
	}

	private void initVehicle() {
		Cursor cursor = getContentResolver().query(
				_currentUri,
				OdometerEntry.FULL_PROJECTION,
				null,
				null,
				null);

		if (cursor != null) {
			if (cursor.moveToNext()) {
				int vehicleId = cursor.getInt(cursor
						.getColumnIndexOrThrow(OdometerEntry.COLUMN_VEHICLE));
				_spinnerVehicle.setSelection(_vehicleIds.indexOf(vehicleId));
				_calendarOdometer.setTime(new Date(cursor.getLong(cursor
						.getColumnIndexOrThrow(OdometerEntry.COLUMN_DATE))));
				_editDate.setText(DateUtilities.dateToStringDate(_calendarOdometer.getTime()));
				_editOdometer.setText(String.valueOf(cursor.getInt(cursor
						.getColumnIndexOrThrow(OdometerEntry.COLUMN_DISTANCE))));
				Log.v("DISTANCE", String.valueOf(cursor.getInt(cursor
						.getColumnIndexOrThrow(OdometerEntry.COLUMN_DISTANCE))));
			}
			cursor.close();
		}
	}

	private void getDistance() {
		Cursor cursor = getContentResolver().query(
				OdometerEntry.CONTENT_URI,
				OdometerEntry.FULL_PROJECTION,
				OdometerEntry.COLUMN_VEHICLE + "=? AND "
						+ OdometerEntry.COLUMN_DATE + "=?",
				new String[]{String.valueOf(getSelectedVehicleId()),
						String.valueOf(getCalendarAtMidnight())},
				null);

		if (cursor != null) {
			if (cursor.moveToNext()) {
				_editOdometer.setText(String.valueOf(cursor.getInt(cursor.
						getColumnIndexOrThrow(OdometerEntry.COLUMN_DISTANCE))));
			}
			cursor.close();
		}
	}

	private int getSelectedVehicleId() {
		return _vehicleIds.get(_spinnerVehicle.getSelectedItemPosition());
	}

	private long getCalendarAtMidnight() {
		_calendarOdometer.set(Calendar.HOUR_OF_DAY, 0);
		_calendarOdometer.set(Calendar.MINUTE, 0);
		_calendarOdometer.set(Calendar.SECOND, 0);
		_calendarOdometer.set(Calendar.MILLISECOND, 0);
		return _calendarOdometer.getTime().getTime();
	}
}
