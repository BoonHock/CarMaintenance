package com.incupe.vewec;

import android.app.DatePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
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
import androidx.preference.PreferenceManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.incupe.vewec.data.OdometerContract.OdometerEntry;
import com.incupe.vewec.utilities.DateUtilities;
import com.incupe.vewec.utilities.SetupViews;
import com.incupe.vewec.utilities.UserDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class OdometerEditorActivity extends AppCompatActivity {
	// arbitrary value
	private static final int DISTANCE_DIFFERENCE_FOR_WARNING = 20000;

	private Uri _currentUri;

	private Spinner _spinnerVehicle;
	private EditText _editDate;
	private EditText _editOdometer;

	private Calendar _calendarOdometer;
	private List<Integer> _vehicleIds = new ArrayList<>();

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
		} else {
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

			_editOdometer.setFilters(new InputFilter[]{
					new InputFilter.LengthFilter(String.valueOf(OdometerEntry.DISTANCE_MAX).length())
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_editor, menu);
		return true;
	}

	@Override
	public void onBackPressed() {
		if (PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean(getString(R.string.pref_get_started), true)) {
			UserDialog.showDialog(this,
					"",
					"Quit tutorial?",
					getString(R.string.quit),
					getString(R.string.cancel),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							PreferenceManager
									.getDefaultSharedPreferences(OdometerEditorActivity.this)
									.edit()
									.putBoolean(getString(R.string.pref_get_started), false)
									.apply();
							OdometerEditorActivity.super.onBackPressed();
						}
					},
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (dialog != null) dialog.dismiss();
						}
					},
					null);
		} else {
			super.onBackPressed();
		}
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
				processSaveOdometer();
				return true;
			case R.id.action_delete:
				UserDialog.showDeleteConfirmationDialog(
						this, getString(R.string.are_you_sure),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// User clicked the "Delete" button
								if (_currentUri != null) {
									deleteOdometer(_currentUri);
								}
							}
						});
				return true;
			case android.R.id.home:
				if (PreferenceManager.getDefaultSharedPreferences(this)
						.getBoolean(getString(R.string.pref_get_started), true)) {
					UserDialog.showDialog(this,
							"",
							"Quit tutorial?",
							getString(R.string.quit),
							getString(R.string.cancel),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									PreferenceManager
											.getDefaultSharedPreferences(OdometerEditorActivity.this)
											.edit()
											.putBoolean(getString(R.string.pref_get_started), false)
											.apply();
									finish();
								}
							},
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									if (dialog != null) dialog.dismiss();
								}
							},
							null);
				} else {
					finish();
				}
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void processSaveOdometer() {
		if (TextUtils.isEmpty(_editOdometer.getText().toString())) {
			UserDialog.showDialog(this, "",
					getString(R.string.odometer_not_provided),
					null);
			return;
		}
		final int vehicle = getSelectedVehicleId();
		final long date = getCalendarAtMidnight();
		final int distance = Integer.parseInt(_editOdometer.getText().toString());

		if (distance < OdometerEntry.DISTANCE_MIN) {
			UserDialog.showDialog(this, "",
					getString(R.string.odometer_input_too_small), null);
			return;
		}
		if (distance > OdometerEntry.DISTANCE_MAX) {
			UserDialog.showDialog(this, "",
					getString(R.string.odometer_input_too_large), null);
			return;
		}

		int diff = bigDiff(this, vehicle, date, distance);

		if (diff > DISTANCE_DIFFERENCE_FOR_WARNING) {
			UserDialog.showDialog(this,
					"",
					"Entered odometer has huge difference of " + diff
							+ " " + getString(R.string.kilometer)
							+ " with previous record. Continue?",
					getString(android.R.string.ok),
					getString(android.R.string.cancel),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (dialog != null) {
								dialog.dismiss();
								if (saveOdometer(OdometerEditorActivity.this,
										_currentUri, vehicle, date, distance)) {
									Toast.makeText(OdometerEditorActivity.this,
											getString(R.string.saved_successfully),
											Toast.LENGTH_SHORT).show();
									finish();
								} else {
									Toast.makeText(OdometerEditorActivity.this,
											getString(R.string.error_has_occurred),
											Toast.LENGTH_SHORT).show();
								}
							}
						}
					},
					null,
					null);
		} else {
			if (saveOdometer(OdometerEditorActivity.this,
					_currentUri, vehicle, date, distance)) {
				Toast.makeText(OdometerEditorActivity.this,
						getString(R.string.saved_successfully),
						Toast.LENGTH_SHORT).show();
				finish();
			} else {
				Toast.makeText(OdometerEditorActivity.this,
						getString(R.string.error_has_occurred),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	public static boolean saveOdometer(Context context, Uri updateUri,
									   int vehicle, long date, int distance) {
		Uri existingRecordUri = null;

		Cursor cursor = context.getContentResolver().query(
				OdometerEntry.CONTENT_URI,
				OdometerEntry.FULL_PROJECTION,
				OdometerEntry.COLUMN_VEHICLE + "=? AND "
						+ OdometerEntry.COLUMN_DATE + "=?",
				new String[]{String.valueOf(vehicle),
						String.valueOf(date)},
				null);

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				long existingRecordId = cursor.getLong(cursor
						.getColumnIndexOrThrow(OdometerEntry._ID));
				existingRecordUri = ContentUris.withAppendedId(
						OdometerEntry.CONTENT_URI, existingRecordId);
				// if user updating and existing record on this date is this record itself,
				// then set existingRecordUri to null
				// existingRecordUri if not null, will be deleted because clash with current
				// editing record's date
				if (updateUri != null && ContentUris.parseId(updateUri) == existingRecordId) {
					existingRecordUri = null;
				}
			}
			cursor.close();
		}

		ContentValues values = new ContentValues();
		values.put(OdometerEntry.COLUMN_VEHICLE, vehicle);
		values.put(OdometerEntry.COLUMN_DATE, date);
		values.put(OdometerEntry.COLUMN_DISTANCE, distance);

		boolean saveSuccess = true;

		if (updateUri == null) {
			// user selected add maintenance
			if (existingRecordUri == null) {
				// no previous existing record. Insert new record
				Uri newUri = context.getContentResolver().insert(OdometerEntry.CONTENT_URI, values);
				saveSuccess = newUri != null;
			} else {
				// previous existing record found. Update record
				saveSuccess = updateOdometer(context, existingRecordUri, values);
			}
		} else {
			// user selected edit maintenance
			if (existingRecordUri == null) {
				// if no previous record, update current record right away
				saveSuccess = updateOdometer(context, updateUri, values);
			} else {
				// existing record found. delete the record first then update
				// or else will violate UNIQUE constraint
				int rowsDeleted = context.getContentResolver().delete(
						existingRecordUri,
						null,
						null);
				if (rowsDeleted > 0) {
					saveSuccess = updateOdometer(context, updateUri, values);
				}
			}
		}
		return saveSuccess;
	}

	private static boolean updateOdometer(Context context, Uri updateUri, ContentValues values) {
		int rowsAffected = context.getContentResolver().update(
				updateUri,
				values,
				null,
				null);

		return rowsAffected != 0;
	}

/*
	public static boolean saveOdometer(Context context, int vehicleId, long odometerDate,
									   int odometerDistance, Uri updateUri) {
		Cursor cursor = context.getContentResolver().query(OdometerEntry.CONTENT_URI,
				OdometerEntry.FULL_PROJECTION,
				OdometerEntry.COLUMN_VEHICLE + "=? AND "
						+ OdometerEntry.COLUMN_DATE + "=?",
				new String[]{String.valueOf(vehicleId),
						String.valueOf(odometerDate)},
				null);

		ContentValues values = new ContentValues();
		values.put(OdometerEntry.COLUMN_VEHICLE, vehicleId);
		values.put(OdometerEntry.COLUMN_DATE, odometerDate);
		values.put(OdometerEntry.COLUMN_DISTANCE, odometerDistance);

		if (cursor != null) {
			if (cursor.getCount() > 0 && cursor.moveToFirst()) {
				// update odometer
				// update 20200424: if maintenance odometer entry more than
				// today's odometer entry, update
				int dbOdometer = cursor.getInt(cursor
						.getColumnIndexOrThrow(OdometerEntry.COLUMN_DISTANCE));

				if (odometerDistance > dbOdometer) {
					Uri odometerUri = Uri.withAppendedPath(APP_MASTER_CONTRACT.BASE_CONTENT_URI,
							OdometerContract.PATH_ODOMETER + "/"
									+ cursor.getLong(cursor
									.getColumnIndexOrThrow(OdometerEntry._ID)));
					context.getContentResolver()
							.update(odometerUri, values, null, null);
				}
			} else {
				// insert odometer
				context.getContentResolver().insert(OdometerEntry.CONTENT_URI, values);
			}
			cursor.close();
		}
	}
*/

	private void deleteOdometer(Uri deleteUri) {
		int rowsDeleted = getContentResolver()
				.delete(deleteUri, null, null);
		if (rowsDeleted == 0) {
			Toast.makeText(this, getString(R.string.error_has_occurred),
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, getString(R.string.odometer_deleted),
					Toast.LENGTH_SHORT).show();
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

	public static int bigDiff(
			Context context, int vehicleId, long currentDate, int compareDistance) {
		Cursor cursor = context.getContentResolver().query(
				OdometerEntry.CONTENT_URI,
				OdometerEntry.FULL_PROJECTION,
				OdometerEntry.COLUMN_VEHICLE + "=? AND "
						+ OdometerEntry.COLUMN_DATE + "<?",
				new String[]{String.valueOf(vehicleId),
						String.valueOf(currentDate)},
				OdometerEntry.COLUMN_DATE + " DESC LIMIT 1");

		int latestOdometer = 0;

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				latestOdometer = cursor.getInt(cursor
						.getColumnIndexOrThrow(OdometerEntry.COLUMN_DISTANCE));
			}
			cursor.close();
		}
		if (latestOdometer == 0) {
			// no previous record found. then it's ok. return as no big diff
			return 0;
		}
		return compareDistance - latestOdometer;
	}
}
