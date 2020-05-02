package com.incupe.vewec;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.incupe.vewec.data.OdometerContract.OdometerEntry;
import com.incupe.vewec.data.UserVehicleContract.UserVehicleEntry;
import com.incupe.vewec.objects.FirebaseObj;
import com.incupe.vewec.objects.UserVehicle;
import com.incupe.vewec.objects.VehicleTemplate;
import com.incupe.vewec.utilities.UserDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Date;

public class VehicleEditorActivity extends AppCompatActivity
		implements LoaderManager.LoaderCallbacks<Cursor> {
	/**
	 * Identifier for the pet data loader
	 */
	private static final int EXISTING_RECORD_LOADER = 0;

	private EditText _editRegNo;
	private Spinner _spinnerBrand;
	private Spinner _spinnerModel;
	private Spinner _spinnerVariant;
	private Spinner _spinnerUsage;
	private EditText _editUpcomingStartFrom;

	private ProgressBar _progressBar;

	private Uri _currentUri;
	private boolean _hasChanges = false;
	private boolean _isEditing = false;
	private boolean _initialising = true;
	private UserVehicle _initUserVehicle = null;
	private ArrayAdapter<String> _modelAdapter;
	private ArrayAdapter<String> _variantAdapter;

	private DialogInterface.OnClickListener _discardButtonClickListener =
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					// User clicked "Discard" button, navigate to parent activity.
//								NavUtils.navigateUpFromSameTask(VehicleEditorActivity.this);
					finish();
				}
			};

	private DialogInterface.OnClickListener _deleteButtonClickListener =
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					// User clicked the "Delete" button
					deleteVehicle();
				}
			};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vehicle_editor);
		Intent intent = getIntent();
		_currentUri = intent.getData();

		// Load an ad into the AdMob banner view.
		AdView adView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);

		_spinnerBrand = findViewById(R.id.spinner_brand);
		_spinnerModel = findViewById(R.id.spinner_model);
		_spinnerVariant = findViewById(R.id.spinner_variant);
		_spinnerUsage = findViewById(R.id.spinner_usage);

		_editRegNo = findViewById(R.id.edit_reg_no);
		_progressBar = findViewById(R.id.indeterminateBar);
		_editUpcomingStartFrom = findViewById(R.id.edit_upcoming_start_from);

		_progressBar.setVisibility(View.VISIBLE);
		findViewById(R.id.ll_content).setVisibility(View.INVISIBLE);

		_editRegNo.setFilters(new InputFilter[]{
				new InputFilter.LengthFilter(UserVehicleEntry.REG_NO_MAX_LENGTH),
				new InputFilter.AllCaps()
		});
		_editUpcomingStartFrom.setFilters(new InputFilter[]{
				new InputFilter.LengthFilter(String.valueOf(OdometerEntry.DISTANCE_MAX).length())
		});

		ArrayAdapter usageSpinnerAdapter = ArrayAdapter.createFromResource(this,
				R.array.array_usage_options, android.R.layout.simple_spinner_item);
		// Specify dropdown layout style - simple list view with 1 item per line
		usageSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		// Apply the adapter to the spinner
		_spinnerUsage.setAdapter(usageSpinnerAdapter);

		if (_currentUri == null) {
			setTitle(R.string.car_editor_title_add_vehicle);
			invalidateOptionsMenu();
			initComponents();
		} else {
			setTitle(R.string.car_editor_title_edit_vehicle);

			getLoaderManager().initLoader(EXISTING_RECORD_LOADER, null, this);
		}

		// tried adding these listeners in the end of onDataChange()
		// in onLoadFinished() after setting up spinners
		// but still fired. so have to use global variable as flag
		_editRegNo.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				_hasChanges = !_initialising;
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		_spinnerBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (_initialising) return;
				_hasChanges = true;
				setupSpinnerModel();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		_spinnerModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (_initialising) return;
				_hasChanges = true;
				setupSpinnerVariant();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		_spinnerVariant.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (_initialising) {
					_initialising = false;
					return;
				}
				_hasChanges = true;
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
	public void onBackPressed() {
		if (!_hasChanges) {
			super.onBackPressed();
			return;
		}
		// Show dialog that there are unsaved changes
		UserDialog.showUnsavedChangesDialog(this, _discardButtonClickListener);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_save:
				saveVehicle();
				return true;
			case R.id.action_delete:
				UserDialog.showDeleteConfirmationDialog(this,
						"Delete " + _initUserVehicle.get_regNo() + "?",
						_deleteButtonClickListener);
				return true;
			case android.R.id.home:
				if (!_hasChanges) {
					finish();
//					NavUtils.navigateUpFromSameTask(VehicleEditorActivity.this);
					return true;
				}
				// Otherwise if there are unsaved changes, setup a dialog to warn the user.
				// Create a click listener to handle the user confirming that
				// changes should be discarded.

				// Show a dialog that notifies the user they have unsaved changes
				UserDialog.showUnsavedChangesDialog(this, _discardButtonClickListener);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void saveVehicle() {
		String regNo = _editRegNo.getText().toString().trim().toUpperCase();
		String brand = _spinnerBrand.getSelectedItem().toString();
		String model = _spinnerModel.getSelectedItem().toString();
		String variant = _spinnerVariant.getSelectedItem().toString();
		int usage = _spinnerUsage.getSelectedItemPosition();
		int upcomingStartFrom = 0;
		String strUpcomingStartFrom = _editUpcomingStartFrom.getText().toString().trim();

		if (!TextUtils.isEmpty(strUpcomingStartFrom)) {
			upcomingStartFrom = Integer.parseInt(strUpcomingStartFrom);
		}

		if (TextUtils.isEmpty(regNo) || TextUtils.isEmpty(brand)
				|| TextUtils.isEmpty(model) || TextUtils.isEmpty(variant)) {
			Toast.makeText(this, "All information is required.",
					Toast.LENGTH_SHORT).show();
			return;
		}

		// check if vehicle already registered
		Cursor cursor = getContentResolver().query(
				UserVehicleEntry.CONTENT_URI,
				new String[]{UserVehicleEntry._ID},    // projection
				UserVehicleEntry.COLUMN_REG_NO + "=?",    // WHERE clause
				new String[]{regNo},    // value for WHERE
				null);

		boolean hasDuplicate = false;

		if (cursor != null) {
			if (cursor.getCount() > 0) {
				if (_currentUri != null && cursor.moveToFirst()) {
					// the ID cursor returned is different from current item's ID
					hasDuplicate = cursor.getLong(cursor
							.getColumnIndexOrThrow(UserVehicleEntry._ID)) !=
							ContentUris.parseId(_currentUri);
				} else {
					hasDuplicate = true;
				}
			}
			cursor.close();
		}
		if (hasDuplicate) {
			showDuplicateRegNoDialog();
			return;
		}

		ContentValues values = new ContentValues();
		values.put(UserVehicleEntry.COLUMN_REG_NO, regNo);
		values.put(UserVehicleEntry.COLUMN_BRAND, brand);
		values.put(UserVehicleEntry.COLUMN_MODEL, model);
		values.put(UserVehicleEntry.COLUMN_VARIANT, variant);
		values.put(UserVehicleEntry.COLUMN_USAGE, usage);
		values.put(UserVehicleEntry.COLUMN_UPCOMING_START_FROM, upcomingStartFrom);

		boolean saveSuccess = false;

		if (_currentUri == null) {
			// insert new record
			values.put(UserVehicleEntry.COLUMN_CREATED_ON, new Date().getTime());
			Uri newUri = getContentResolver().insert(UserVehicleEntry.CONTENT_URI, values);
			saveSuccess = newUri != null;
			Log.v("VEHICLE_ID_CHECK", "CREATED: " + ContentUris.parseId(newUri));
		} else {
			// update existing record
			int rowsAffected = getContentResolver().update(_currentUri,
					values, null, null);
			saveSuccess = rowsAffected != 0;
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

	private void deleteVehicle() {
		if (_currentUri != null) {
			int rowsDeleted = getContentResolver()
					.delete(_currentUri, null, null);
			if (rowsDeleted == 0) {
				Toast.makeText(this, getString(R.string.error_has_occurred),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, getString(R.string.vehicle_deleted),
						Toast.LENGTH_SHORT).show();
			}
		}
		finish();
	}

	private void showDuplicateRegNoDialog() {
		// Create an AlertDialog.Builder and set the message, and click listeners
		// for the postivie and negative buttons on the dialog.
		UserDialog.showDialog(this, getString(R.string.duplicate_record),
				getString(R.string.duplicate_reg_no_message),
				null);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this,
				_currentUri,
				UserVehicleEntry.FULL_PROJECTION,
				null,
				null,
				null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		_isEditing = false;
		if (data != null && data.getCount() > 0) {
			if (data.moveToFirst()) {
				_isEditing = true;
				_initUserVehicle = new UserVehicle(data);

				if (_initUserVehicle.get_usage() == UserVehicleEntry.USAGE_SEVERE) {
					_spinnerUsage.setSelection(1);
				} else {
					_spinnerUsage.setSelection(0);
				}

				_editRegNo.setText(_initUserVehicle.get_regNo());
			}
		}
		initComponents();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		_editRegNo.setText("");
	}

	private void initComponents() {
		ArrayAdapter<String> _brandAdapter = new ArrayAdapter<>(VehicleEditorActivity.this,
				android.R.layout.simple_spinner_item, VehicleTemplate.getBrands(FirebaseObj._vehicleTemplates));
		_brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		_spinnerBrand.setAdapter(_brandAdapter);

		if (_isEditing) {
			_spinnerBrand.setSelection(_brandAdapter
					.getPosition(_initUserVehicle.get_brand()));
		}

		setupSpinnerModel();
		if (_isEditing) {
			_spinnerModel.setSelection(_modelAdapter
					.getPosition(_initUserVehicle.get_model()));
		}

		setupSpinnerVariant();
		if (_isEditing) {
			_spinnerVariant.setSelection(_variantAdapter
					.getPosition(_initUserVehicle.get_variant()));
			_editUpcomingStartFrom.setText(String
					.valueOf(_initUserVehicle.get_upcomingStartFrom()));
		}

		_progressBar.setVisibility(View.GONE);
		findViewById(R.id.ll_content).setVisibility(View.VISIBLE);
	}

	public void setupSpinnerModel() {
		String selectedBrand = (String) _spinnerBrand.getSelectedItem();
		_modelAdapter = new ArrayAdapter<>(VehicleEditorActivity.this,
				android.R.layout.simple_spinner_item,
				VehicleTemplate.getModels(FirebaseObj._vehicleTemplates,
						selectedBrand));
		_modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		_spinnerModel.setAdapter(_modelAdapter);
	}

	public void setupSpinnerVariant() {
		String selectedBrand = (String) _spinnerBrand.getSelectedItem();
		String selectedModel = (String) _spinnerModel.getSelectedItem();
		_variantAdapter = new ArrayAdapter<>(VehicleEditorActivity.this,
				android.R.layout.simple_spinner_item,
				VehicleTemplate.getVariants(FirebaseObj._vehicleTemplates,
						selectedBrand, selectedModel));
		_variantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		_spinnerVariant.setAdapter(_variantAdapter);
	}
}
