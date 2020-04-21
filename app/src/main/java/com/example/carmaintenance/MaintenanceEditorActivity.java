package com.example.carmaintenance;

import android.app.DatePickerDialog;
import android.content.ContentUris;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.carmaintenance.data.APP_MASTER_CONTRACT;
import com.example.carmaintenance.data.FirebaseContract;
import com.example.carmaintenance.data.MaintenanceContract.MaintenanceEntry;
import com.example.carmaintenance.data.MaintenanceDetailsContract.MaintenanceDetailsEntry;
import com.example.carmaintenance.data.MaintenanceItemContract.MaintenanceItemEntry;
import com.example.carmaintenance.data.OdometerContract;
import com.example.carmaintenance.data.OdometerContract.OdometerEntry;
import com.example.carmaintenance.data.UserVehicleContract.UserVehicleEntry;
import com.example.carmaintenance.objects.FirebaseObj;
import com.example.carmaintenance.objects.MaintenanceItem;
import com.example.carmaintenance.objects.UserVehicle;
import com.example.carmaintenance.objects.VehicleTemplate;
import com.example.carmaintenance.utilities.DateUtilities;
import com.example.carmaintenance.utilities.Misc;
import com.example.carmaintenance.utilities.SetupViews;
import com.example.carmaintenance.utilities.UserDialog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MaintenanceEditorActivity extends AppCompatActivity {
	private Uri _currentUri;

	private List<Integer> _vehicleIds;

	private LinearLayout _llMask;
	private LinearLayout _llNewItem;
	private EditText _editNewItemName;
	private EditText _editNewItemPrice;
	private TextView _txtCloseNewItem;

	private ScrollView _viewContent;
	private ProgressBar _progressBar;
	private Spinner _spinnerVehicle;
	private EditText _editDate;
	private EditText _editOdometer;
	private EditText _editRemarks;
	private EditText _editTotal;
	private LinearLayout _llInspectItems;
	private LinearLayout _llReplaceItems;

	private Calendar _calendarOdometer;
	private DecimalFormat _decimalFormat = new DecimalFormat("0.00");

	private List<String> _listInspect;
	private List<String> _listReplace;

	private List<String> _preCheckInspect = new ArrayList<>();
	private List<String> _preCheckReplace = new ArrayList<>();
	private Map<String, Double> _mapInitInspect = new HashMap<>();
	private Map<String, Double> _mapInitReplace = new HashMap<>();

	private int _editVehicleId = -1;
	private boolean _isAddingReplaceItem = true;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maintenance_editor);

		Intent intent = getIntent();
		_currentUri = intent.getData();
		_calendarOdometer = DateUtilities
				.getCalendarAtMidnight(Calendar.getInstance()); // default value

		_llMask = findViewById(R.id.ll_mask);
		_llNewItem = findViewById(R.id.ll_new_item);
		_editNewItemName = findViewById(R.id.edit_new_item_name);
		_editNewItemPrice = findViewById(R.id.edit_new_item_price);
		_txtCloseNewItem = findViewById(R.id.txt_close);

		_viewContent = findViewById(R.id.content);
		_progressBar = findViewById(R.id.progress_bar);
		_spinnerVehicle = findViewById(R.id.spinner_vehicle);
		_editDate = findViewById(R.id.edit_date);
		_editOdometer = findViewById(R.id.edit_odometer);
		_editRemarks = findViewById(R.id.edit_remarks);
		_editTotal = findViewById(R.id.edit_total);
		_llInspectItems = findViewById(R.id.ll_inspect_items);
		_llReplaceItems = findViewById(R.id.ll_replace_items);

		_llMask.setVisibility(View.GONE);
		_llNewItem.setVisibility(View.GONE);
		_viewContent.setVisibility(View.INVISIBLE);
		_progressBar.setVisibility(View.VISIBLE);

		if (_currentUri == null) {
			invalidateOptionsMenu();
		} else {
			// get existing records
			// will populate @_mapInitInspect and @_mapInitReplace
			Cursor cursor = getContentResolver().query(
					_currentUri,
					MaintenanceEntry.FULL_PROJECTION,
					null,
					null,
					null);

			if (cursor != null) {
				if (cursor.moveToFirst()) {
					_editOdometer.setText(String.valueOf(cursor.getInt(cursor
							.getColumnIndexOrThrow(MaintenanceEntry.COLUMN_ODOMETER))));
				}
				cursor.close();
			}
			getDbMaintenanceItems();
		}
		if (getIntent().getStringArrayListExtra("inspect_items") != null
				&& getIntent().getStringArrayListExtra("replace_items") != null
				&& getIntent().getStringExtra("vehicle_id") != null) {
			// user setting specific vehicle's maintenance. disable spinner
			_editVehicleId = Integer.parseInt(getIntent().getStringExtra("vehicle_id"));
			_preCheckInspect = getIntent().getStringArrayListExtra("inspect_items");
			_preCheckReplace = getIntent().getStringArrayListExtra("replace_items");
			_spinnerVehicle.setEnabled(false);
		}

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

		if (_editVehicleId != -1) {
			_spinnerVehicle.setSelection(_vehicleIds.indexOf(_editVehicleId));
		}

		_editDate.setText(DateUtilities.dateToStringDate(_calendarOdometer.getTime()));

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

		_editDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePickerDialog datePickerDialog =
						new DatePickerDialog(MaintenanceEditorActivity.this, dateSetListener,
								_calendarOdometer.get(Calendar.YEAR), _calendarOdometer.get(Calendar.MONTH),
								_calendarOdometer.get(Calendar.DAY_OF_MONTH));
				// user cannot fill in future odometer. sounds logical right?
				datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
				datePickerDialog.show();
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
						UserDialog.showDialog(MaintenanceEditorActivity.this, "",
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
		_editNewItemName.setFilters(new InputFilter[]{
				new InputFilter.LengthFilter(MaintenanceItemEntry.ITEM_NAME_MAX_LENGTH)
		});

		findViewById(R.id.btn_add_inspect).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_llMask.setVisibility(View.VISIBLE);
				_llNewItem.setVisibility(View.VISIBLE);

				_editNewItemName.setText("");
				_editNewItemPrice.setText("");
				_editNewItemName.requestFocus();

				Misc.showKeyboard(MaintenanceEditorActivity.this,
						_editNewItemName);

				_isAddingReplaceItem = false;
			}
		});
		findViewById(R.id.btn_add_replace).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_llMask.setVisibility(View.VISIBLE);
				_llNewItem.setVisibility(View.VISIBLE);

				_editNewItemName.setText("");
				_editNewItemPrice.setText("");
				_editNewItemName.requestFocus();

				Misc.showKeyboard(MaintenanceEditorActivity.this,
						_editNewItemName);

				_isAddingReplaceItem = true;
			}
		});
		findViewById(R.id.txt_add).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String itemName = _editNewItemName.getText().toString().trim();
				String strPrice = _editNewItemPrice.getText().toString().trim();

				if (TextUtils.isEmpty(itemName)) {
					UserDialog.showDialog(MaintenanceEditorActivity.this,
							"",
							getString(R.string.item_name_required),
							null);
					return;
				}

				boolean itemAlreadyExists = false;

				if (_isAddingReplaceItem) {
					for (int i = 0, j = _llReplaceItems.getChildCount(); i < j; i++) {
						TextView txtExistItem = _llReplaceItems.getChildAt(i).findViewById(R.id.txt_item);
						if (txtExistItem.getText().toString().trim().toUpperCase()
								.equals(itemName.toUpperCase())) {
							itemAlreadyExists = true;
							break;
						}
					}
				} else {
					for (int i = 0, j = _llInspectItems.getChildCount(); i < j; i++) {
						TextView txtExistItem = _llInspectItems.getChildAt(i).findViewById(R.id.txt_item);
						if (txtExistItem.getText().toString().trim().toUpperCase()
								.equals(itemName.toUpperCase())) {
							itemAlreadyExists = true;
							break;
						}
					}
				}

				if (itemAlreadyExists) {
					UserDialog.showDialog(MaintenanceEditorActivity.this,
							"", getString(R.string.item_already_exists), null);
					return;
				}

				View view = LayoutInflater.from(MaintenanceEditorActivity.this)
						.inflate(R.layout.template_maintenance_editor_item, null);

				TextView txtItem = view.findViewById(R.id.txt_item);
				EditText editPrice = view.findViewById(R.id.edit_price);
				CheckBox cbItem = view.findViewById(R.id.cb_item);

				if (_isAddingReplaceItem) {
					_llReplaceItems.addView(view, 0);
				} else {
					_llInspectItems.addView(view, 0);
				}

				setMaintenanceItemListeners(editPrice, cbItem);

				txtItem.setText(itemName);
				editPrice.setText(strPrice);
				if (!TextUtils.isEmpty(strPrice)) {
					editPrice.setText(String.format(Locale.getDefault(),
							"%.2f", Double.parseDouble(strPrice)));
				}
				cbItem.setChecked(true);

				// close view
				_txtCloseNewItem.performClick();
			}
		});
		_txtCloseNewItem.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Misc.hideKeyboard(MaintenanceEditorActivity.this);

				_llMask.setVisibility(View.GONE);
				_llNewItem.setVisibility(View.GONE);
			}
		});
		_editNewItemPrice.setOnFocusChangeListener(_focusChangeFormatMoney);

		_spinnerVehicle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// if not editing vehicle or spinner selected editing vehicle, then display
				// maintenance item. or else, if editing vehicle but spinner not selecting
				// editing vehicle's ID, then this is only triggered by initialisation
				// and should be ignored
				if (_editVehicleId == -1 || _vehicleIds.get(_spinnerVehicle
						.getSelectedItemPosition()) == _editVehicleId) {
					getMaintenanceItems();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
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
		// If this is a new pet, hide the "Delete" menu item.
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
				saveMaintenance();
				return true;
			case R.id.action_delete:
				UserDialog.showDeleteConfirmationDialog(
						this, getString(R.string.are_you_sure),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								deleteMaintenance();
							}
						});
				return true;
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void getMaintenanceItems() {
		int vehicleId = _vehicleIds.get(_spinnerVehicle.getSelectedItemPosition());

		Cursor cursor = getContentResolver().query(
				UserVehicleEntry.CONTENT_URI,
				UserVehicleEntry.FULL_PROJECTION,
				UserVehicleEntry._ID + "=?",
				new String[]{String.valueOf(vehicleId)}, null);

		if (cursor != null) {
			if (cursor.getCount() > 0) {
				final UserVehicle userVehicle = new UserVehicle(cursor);

				final String firebaseVehicleId = VehicleTemplate
						.getVehicleIdFromList(FirebaseObj._vehicleTemplates,
								userVehicle.get_brand(),
								userVehicle.get_model(),
								userVehicle.get_variant());

				FirebaseObj.runCallbackMaintenanceDetails(firebaseVehicleId, new FirebaseObj() {
					@Override
					public void callback() {
						_listInspect = new ArrayList<>();
						_listReplace = new ArrayList<>();

						for (MaintenanceItem item : _maintenanceItems.get(firebaseVehicleId)) {
							int itemUsage = item.getUsage();
							if (itemUsage == -1 || itemUsage == userVehicle.get_usage()) {
								if (item.getInspect_replace() ==
										FirebaseContract.FirebaseMaintenanceDetailsEntry.INSPECT) {
									_listInspect.add(item.getItem());
								} else if (item.getInspect_replace() ==
										FirebaseContract.FirebaseMaintenanceDetailsEntry.REPLACE) {
									_listReplace.add(item.getItem());
								}
							}
						}

						Collections.sort(_listInspect);
						Collections.sort(_listReplace);

						if (_preCheckInspect != null) {
							_listInspect.removeAll(_preCheckInspect);
							Collections.sort(_preCheckInspect);
							_listInspect.addAll(0, _preCheckInspect);
						}
						if (_preCheckReplace != null) {
							_listReplace.removeAll(_preCheckReplace);
							Collections.sort(_preCheckReplace);
							_listReplace.addAll(0, _preCheckReplace);
						}

						displayMaintenanceItems();
						_viewContent.setVisibility(View.VISIBLE);
						_progressBar.setVisibility(View.GONE);
					}
				});
			}
			cursor.close();
		}
	}

	private void getDbMaintenanceItems() {
		Uri uri = MaintenanceDetailsEntry.CONTENT_URI_MAINTENANCE;
		long maintenanceId = ContentUris.parseId(_currentUri);
		Cursor cursor = getContentResolver().query(
				ContentUris.withAppendedId(uri, maintenanceId),
				null,
				null,
				new String[]{String.valueOf(maintenanceId)},
				null);

		if (cursor != null) {
			while (cursor.moveToNext()) {
				String itemName = cursor.getString(cursor
						.getColumnIndexOrThrow(MaintenanceItemEntry.COLUMN_ITEM));
				double itemPrice = cursor.getDouble(cursor
						.getColumnIndexOrThrow(MaintenanceDetailsEntry.COLUMN_PRICE));
				switch (cursor.getInt(cursor.getColumnIndexOrThrow(MaintenanceItemEntry
						.COLUMN_INSPECT_REPLACE))) {
					case MaintenanceItemEntry.INSPECT_VALUE:
						_preCheckInspect.add(itemName);
						_mapInitInspect.put(itemName, itemPrice);
						break;
					case MaintenanceItemEntry.REPLACE_VALUE:
						_preCheckReplace.add(itemName);
						_mapInitReplace.put(itemName, itemPrice);
						break;
				}
			}
			cursor.close();
		}
	}

	private void displayMaintenanceItems() {
		LinearLayout llInspect = findViewById(R.id.ll_inspect);
		LinearLayout llReplace = findViewById(R.id.ll_replace);

		// remove all children and re-add them
		_llInspectItems.removeAllViews();
		_llReplaceItems.removeAllViews();

		if (_listInspect.isEmpty()) {
			llInspect.setVisibility(View.GONE);
		} else {
			llInspect.setVisibility(View.VISIBLE);
		}
		if (_listReplace.isEmpty()) {
			llReplace.setVisibility(View.GONE);
		} else {
			llReplace.setVisibility(View.VISIBLE);
		}
		addMaintenanceItemLayout(_listInspect, _preCheckInspect,
				_mapInitInspect, _llInspectItems);
		addMaintenanceItemLayout(_listReplace, _preCheckReplace,
				_mapInitReplace, _llReplaceItems);
	}

	private void addMaintenanceItemLayout(
			List<String> maintenanceItems, List<String> preCheckItems,
			Map<String, Double> initPrice, LinearLayout linearLayout) {
		for (String itemName : maintenanceItems) {
			View view = LayoutInflater.from(this)
					.inflate(R.layout.template_maintenance_editor_item, null);

			TextView txtItem = view.findViewById(R.id.txt_item);
			txtItem.setText(itemName);

			final EditText editPrice = view.findViewById(R.id.edit_price);
			final CheckBox cbItem = view.findViewById(R.id.cb_item);

			if (initPrice.containsKey(itemName)) {
				editPrice.setText(String.valueOf(initPrice.get(itemName)));
			}

			cbItem.setChecked(preCheckItems.contains(itemName));
			setMaintenanceItemListeners(editPrice, cbItem);
			linearLayout.addView(view);
		}
	}

	private void setMaintenanceItemListeners(final EditText editPrice, final CheckBox cbItem) {
		editPrice.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				double totalPrice = 0;

				if (!TextUtils.isEmpty(editPrice.getText().toString())) {
					cbItem.setChecked(true);
				}

				for (int i = 0, j = _llInspectItems.getChildCount(); i < j; i++) {
					EditText editPrice = _llInspectItems.getChildAt(i)
							.findViewById(R.id.edit_price);
					if (!TextUtils.isEmpty(editPrice.getText().toString())) {
						totalPrice += Double.parseDouble(editPrice.getText().toString());
					}
				}
				for (int i = 0, j = _llReplaceItems.getChildCount(); i < j; i++) {
					EditText editPrice = _llReplaceItems.getChildAt(i)
							.findViewById(R.id.edit_price);
					if (!TextUtils.isEmpty(editPrice.getText().toString())) {
						totalPrice += Double.parseDouble(editPrice.getText().toString());
					}
				}

				_editTotal.setText(_decimalFormat.format(totalPrice));
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		editPrice.setOnFocusChangeListener(_focusChangeFormatMoney);
		cbItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					editPrice.setText("");
				}
			}
		});
	}

	private void saveMaintenance() {
		long maintenanceDate = DateUtilities
				.getCalendarAtMidnight(_calendarOdometer).getTime().getTime();
		String strOdometerDistance = _editOdometer.getText().toString();
		int vehicleId = getVehicleId();
		String remarks = _editRemarks.getText().toString().trim();

		if (TextUtils.isEmpty(strOdometerDistance)) {
			UserDialog.showDialog(this, "",
					getString(R.string.odometer_not_provided),
					null);
			return;
		}
		if (!hasCheckedMaintenanceItem()) {
			UserDialog.showDialog(this, "",
					getString(R.string.select_one_maintenance_item),
					null);
			return;
		}

		saveOdometer(vehicleId, maintenanceDate, strOdometerDistance);

		ContentValues values = new ContentValues();
		values.put(MaintenanceEntry.COLUMN_VEHICLE, vehicleId);
		values.put(MaintenanceEntry.COLUMN_DATE, maintenanceDate);
		values.put(MaintenanceEntry.COLUMN_ODOMETER, Integer.valueOf(strOdometerDistance));
		values.put(MaintenanceEntry.COLUMN_REMARKS, remarks);
		values.put(MaintenanceEntry.COLUMN_CREATED_ON, Calendar.getInstance().getTime().getTime());

		if (_currentUri != null) {
			getContentResolver().delete(_currentUri, null, null);
		}
		/*
		// for now, to simplify development, delete maintenance if editing
		// and insert new one again
		int maintenanceId = -1;
		if (_currentUri == null) {
			// insert maintenance
			Uri newUri = getContentResolver().insert(MaintenanceEntry.CONTENT_URI, values);
			if (newUri != null) {
				maintenanceId = (int) ContentUris.parseId(newUri);
			}
		} else {
			// update maintenance
			int rowsAffected = getContentResolver()
					.update(_currentUri, values, null, null);
			if (rowsAffected != 0) {
				maintenanceId = (int) ContentUris.parseId(_currentUri);
			}
		}
		if (maintenanceId == -1) {
			UserDialog.showDialog(this,
					"",
					"An error has occurred.",
					null);
			return;
		}
		*/
		Uri newMaintenanceUri = getContentResolver().insert(MaintenanceEntry.CONTENT_URI, values);
		if (newMaintenanceUri == null) {
			UserDialog.showDialog(this,
					getString(R.string.error_has_occurred),
					"Failed to save maintenance data.",
					null);
			return;
		}
		int maintenanceId = (int) ContentUris.parseId(newMaintenanceUri);

		if (saveMaintenanceDetails(maintenanceId,
				MaintenanceItemEntry.REPLACE_VALUE, _llReplaceItems) &&
				saveMaintenanceDetails(maintenanceId,
						MaintenanceItemEntry.INSPECT_VALUE, _llInspectItems)) {
			Toast.makeText(this, getString(R.string.saved_successfully),
					Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	private boolean saveMaintenanceDetails(int maintenanceId, int inspect_replace, LinearLayout linearLayout) {
		for (int i = 0, j = linearLayout.getChildCount(); i < j; i++) {
			CheckBox cb = linearLayout.getChildAt(i).findViewById(R.id.cb_item);

			if (!cb.isChecked()) {
				continue;
			}

			String itemName = ((TextView) linearLayout.getChildAt(i)
					.findViewById(R.id.txt_item)).getText().toString().trim();
			String itemPrice = ((TextView) linearLayout.getChildAt(i)
					.findViewById(R.id.edit_price)).getText().toString().trim();

			// price is two decimal places only
			double doubPrice = TextUtils.isEmpty(itemPrice) ?
					0 : Math.round(Double.parseDouble(itemPrice) * 100.0) / 100.0;

			Cursor itemCursor = getContentResolver().query(
					MaintenanceItemEntry.CONTENT_URI,
					MaintenanceItemEntry.FULL_PROJECTION,
					MaintenanceItemEntry.COLUMN_ITEM + "=? AND "
							+ MaintenanceItemEntry.COLUMN_INSPECT_REPLACE + "=?",
					new String[]{itemName, String.valueOf(inspect_replace)},
					null);

			long itemId = -1;

			if (itemCursor != null) {
				if (itemCursor.getCount() == 0) {
					ContentValues itemValues = new ContentValues();
					itemValues.put(MaintenanceItemEntry.COLUMN_ITEM, itemName);
					itemValues.put(MaintenanceItemEntry.COLUMN_INSPECT_REPLACE,
							inspect_replace);
					Uri newItemUri = getContentResolver()
							.insert(MaintenanceItemEntry.CONTENT_URI, itemValues);

					if (newItemUri != null) {
						itemId = ContentUris.parseId(newItemUri);
					}
				} else if (itemCursor.moveToFirst()) {
					itemId = itemCursor.getInt(itemCursor
							.getColumnIndexOrThrow(MaintenanceItemEntry._ID));
				}
				itemCursor.close();
			}

			if (itemId == -1) {
				// invalid item ID!!
				UserDialog.showDialog(this,
						getString(R.string.error_has_occurred),
						getString(R.string.maintenance_item_save_failed),
						null);
				return false;
			}

			ContentValues values = new ContentValues();
			values.put(MaintenanceDetailsEntry.COLUMN_MAINTENANCE_ID, maintenanceId);
			values.put(MaintenanceDetailsEntry.COLUMN_ITEM, itemId);
			values.put(MaintenanceDetailsEntry.COLUMN_PRICE, doubPrice);

			Uri newUri = getContentResolver()
					.insert(MaintenanceDetailsEntry.CONTENT_URI, values);

			if (newUri == null) {
				UserDialog.showDialog(this,
						getString(R.string.error_has_occurred),
						getString(R.string.maintenance_details_save_failed),
						null);
				return false;
			}
		}
		return true;
	}

	private void saveOdometer(int vehicleId, long odometerDate, String strOdometerDistance) {
		Cursor cursor = getContentResolver().query(OdometerEntry.CONTENT_URI,
				OdometerEntry.FULL_PROJECTION,
				OdometerEntry.COLUMN_VEHICLE + "=? AND "
						+ OdometerEntry.COLUMN_DATE + "=?",
				new String[]{String.valueOf(vehicleId),
						String.valueOf(odometerDate)},
				null);

		ContentValues values = new ContentValues();
		values.put(OdometerEntry.COLUMN_VEHICLE, vehicleId);
		values.put(OdometerEntry.COLUMN_DATE, odometerDate);
		values.put(OdometerEntry.COLUMN_DISTANCE, Integer.valueOf(strOdometerDistance));

		if (cursor != null) {
			if (cursor.getCount() > 0 && cursor.moveToFirst()) {
				// update odometer
				Uri odometerUri = Uri.withAppendedPath(APP_MASTER_CONTRACT.BASE_CONTENT_URI,
						OdometerContract.PATH_ODOMETER + "/"
								+ cursor.getLong(cursor.getColumnIndexOrThrow(OdometerEntry._ID)));
				getContentResolver().update(odometerUri, values, null, null);
			} else {
				// insert odometer
				getContentResolver().insert(OdometerEntry.CONTENT_URI, values);
			}
			cursor.close();
		}
	}

	private int getVehicleId() {
		return _vehicleIds.get(_spinnerVehicle.getSelectedItemPosition());
	}

	private boolean hasCheckedMaintenanceItem() {
		for (int i = 0, j = _llInspectItems.getChildCount(); i < j; i++) {
			if (((CheckBox) _llInspectItems.getChildAt(i)
					.findViewById(R.id.cb_item)).isChecked()) {
				return true;
			}
		}
		for (int i = 0, j = _llReplaceItems.getChildCount(); i < j; i++) {
			if (((CheckBox) _llReplaceItems.getChildAt(i)
					.findViewById(R.id.cb_item)).isChecked()) {
				return true;
			}
		}
		return false;
	}

	private void deleteMaintenance() {
		if (_currentUri == null) {
			return;
		}
		int rowsDeleted = getContentResolver().delete(_currentUri, null, null);

		if (rowsDeleted == 0) {
			Toast.makeText(this, getString(R.string.error_has_occurred),
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, getString(R.string.maintenance_deleted),
					Toast.LENGTH_SHORT).show();
			finish();
		}
	}
}
