package com.incupe.vewec;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.incupe.vewec.data.MaintenanceItemContract.MaintenanceItemEntry;
import com.incupe.vewec.data.UserVehicleContract;
import com.incupe.vewec.data.UserVehicleContract.UserVehicleEntry;
import com.incupe.vewec.objects.FirebaseObj;
import com.incupe.vewec.objects.MaintenanceItem;
import com.incupe.vewec.objects.UserVehicle;
import com.incupe.vewec.objects.VehicleTemplate;
import com.incupe.vewec.utilities.UserDialog;

import java.util.Date;
import java.util.List;

public class VehicleEditorActivity extends AppCompatActivity
		implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final int EXISTING_RECORD_LOADER = 0;

	private EditText _editRegNo;
	private Switch _switchUseTemplate;
	private Spinner _spinnerBrand;
	private Spinner _spinnerModel;
	private Spinner _spinnerVariant;
	private Spinner _spinnerVariantCustom;
	private EditText _editBrand;
	private EditText _editModel;
	private Spinner _spinnerUsage;
	private LinearLayout _llNewVehicle;
	private RadioButton _radIsNew;
	private RadioButton _radIsNotNew;

	private LinearLayout _llMask;
	private ProgressBar _progressBar;

	private Uri _currentUri;
	private boolean _hasChanges = false;
	private boolean _initialising = true;
	private UserVehicle _initUserVehicle = null;
	private ArrayAdapter<String> _modelAdapter;
	private ArrayAdapter<String> _variantAdapter;
	private ArrayAdapter<CharSequence> _variantAdapter_custom;

	final private DialogInterface.OnClickListener _discardButtonClickListener =
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					// User clicked "Discard" button, navigate to parent activity.
//								NavUtils.navigateUpFromSameTask(VehicleEditorActivity.this);
					finish();
				}
			};

	@Override
	protected void onResume() {
		super.onResume();
		// TODO: temporary limit only one vehicle allowed
		if (_currentUri == null
				&& UserVehicleContract.UserVehicleEntry.getCount(this) > 0) {
			UserDialog.showDialog(this,
					"",
					"Number of vehicle is currently limited to one only. " +
							"More vehicles can be created in future release.",
					new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							finish();
						}
					});
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vehicle_editor);
		_currentUri = getIntent().getData();

		// Load an ad into the AdMob banner view.
		AdView adView = findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);

		_switchUseTemplate = findViewById(R.id.switch_use_template);
		_editBrand = findViewById(R.id.edit_brand);
		_editModel = findViewById(R.id.edit_model);
		_spinnerBrand = findViewById(R.id.spinner_brand);
		_spinnerModel = findViewById(R.id.spinner_model);
		_spinnerVariant = findViewById(R.id.spinner_variant);
		_spinnerVariantCustom = findViewById(R.id.spinner_variant_custom);
		_spinnerUsage = findViewById(R.id.spinner_usage);

		_editRegNo = findViewById(R.id.edit_reg_no);
		_llMask = findViewById(R.id.ll_mask);
		_progressBar = findViewById(R.id.indeterminateBar);
		_llNewVehicle = findViewById(R.id.ll_is_new_vehicle);
		_radIsNew = findViewById(R.id.rad_new);
		_radIsNotNew = findViewById(R.id.rad_not_new);

		_progressBar.setVisibility(View.VISIBLE);
		findViewById(R.id.ll_content).setVisibility(View.INVISIBLE);

		_editRegNo.setFilters(new InputFilter[]{
				new InputFilter.LengthFilter(UserVehicleEntry.REG_NO_MAX_LENGTH),
				new InputFilter.AllCaps()
		});
		_editBrand.setFilters(new InputFilter[]{
				new InputFilter.AllCaps()
		});
		_editModel.setFilters(new InputFilter[]{
				new InputFilter.AllCaps()
		});

		ArrayAdapter<CharSequence> usageSpinnerAdapter =
				ArrayAdapter.createFromResource(this,
						R.array.array_usage_options,
						android.R.layout.simple_spinner_item);
		_variantAdapter_custom =
				ArrayAdapter.createFromResource(this,
						R.array.array_custom_vehicle_variant,
						android.R.layout.simple_spinner_item);
		// Specify dropdown layout style - simple list view with 1 item per line
		usageSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		// Apply the adapter to the spinner
		_spinnerUsage.setAdapter(usageSpinnerAdapter);
		_variantAdapter_custom.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		_spinnerVariantCustom.setAdapter(_variantAdapter_custom);

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

		findViewById(R.id.intro_msg).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// open google forms to submit brand-model-variant
				Intent intent = new Intent(Intent.ACTION_VIEW);

				if (intent.resolveActivity(getPackageManager()) != null) {
					intent.setData(Uri.parse("https://docs.google.com/forms/d/e/1FAIpQLScbmnApgQj5wDmW2pMZHJwFlkfwNm_xpGFLtv1WamSEf1j41A/viewform?usp=sf_link"));
					startActivity(intent);
				} else {
					Toast.makeText(VehicleEditorActivity.this,
							getString(R.string.unable_to_open_link),
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		final TextView txtCarVariantHelp = findViewById(R.id.txt_car_variant_help);
		TextView txtVariant = findViewById(R.id.txt_variant);
		TextView txtUsage = findViewById(R.id.txt_usage);

		txtVariant.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				UserDialog.showDialog(
						VehicleEditorActivity.this,
						getString(R.string.variant),
						getString(R.string.variant_tip),
						null);
			}
		});
		txtCarVariantHelp.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(VehicleEditorActivity.this,
								CarVariantTutorialActivity.class);
						startActivity(intent);
					}
				});

		txtUsage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				UserDialog.showDialog(
						VehicleEditorActivity.this,
						getString(R.string.usage),
						getString(R.string.maintenance_may_vary),
						null
				);
			}
		});

		_switchUseTemplate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				int useTemplateVisibility = _switchUseTemplate.isChecked() ?
						View.VISIBLE : View.GONE;
				int useCustomVisibility = _switchUseTemplate.isChecked() ?
						View.GONE : View.VISIBLE;
				_spinnerBrand.setVisibility(useTemplateVisibility);
				_spinnerModel.setVisibility(useTemplateVisibility);
				_spinnerVariant.setVisibility(useTemplateVisibility);

				txtCarVariantHelp.setVisibility(useTemplateVisibility);

				_editBrand.setVisibility(useCustomVisibility);
				_editModel.setVisibility(useCustomVisibility);
				_spinnerVariantCustom.setVisibility(useCustomVisibility);
			}
		});
		_switchUseTemplate.setChecked(true);
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
									.getDefaultSharedPreferences(VehicleEditorActivity.this)
									.edit()
									.putBoolean(getString(R.string.pref_get_started), false)
									.apply();
							VehicleEditorActivity.super.onBackPressed();
						}
					},
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (dialog != null) dialog.dismiss();
						}
					},
					null);
		} else if (_hasChanges) {
			// Show dialog that there are unsaved changes
			UserDialog.showUnsavedChangesDialog(this, _discardButtonClickListener);
		} else {
			super.onBackPressed();
		}
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
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// User clicked the "Delete" button
								deleteVehicle();
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
											.getDefaultSharedPreferences(VehicleEditorActivity.this)
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
				} else if (_hasChanges) {
					// Otherwise if there are unsaved changes, setup a dialog to warn the user.
					// Create a click listener to handle the user confirming that
					// changes should be discarded.

					// Show a dialog that notifies the user they have unsaved changes
					UserDialog.showUnsavedChangesDialog(this, _discardButtonClickListener);
				} else {
					finish();
				}
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void saveVehicle() {
		String regNo = _editRegNo.getText().toString().trim().toUpperCase();
		String brand = "";
		String model = "";
		String variant = "";
		boolean insertingDefaultMaintenanceItems = false;

		if (_switchUseTemplate.isChecked()) {
			if (_spinnerBrand.getSelectedItem() == null ||
					_spinnerModel.getSelectedItem() == null ||
					_spinnerVariant.getSelectedItem() == null) {
				UserDialog.showDialog(this, "",
						"All information is required.", null);
				return;
			}

			brand = _spinnerBrand.getSelectedItem().toString();
			model = _spinnerModel.getSelectedItem().toString();
			variant = _spinnerVariant.getSelectedItem().toString();
		} else {
			brand = _editBrand.getText().toString();
			model = _editModel.getText().toString();
			variant = _spinnerVariantCustom.getSelectedItem().toString();
		}

		int usage = _spinnerUsage.getSelectedItemPosition();
		boolean isNew = _radIsNew.isChecked();

		if (TextUtils.isEmpty(regNo) || TextUtils.isEmpty(brand)
				|| TextUtils.isEmpty(model) || TextUtils.isEmpty(variant) ||
				(!_radIsNew.isChecked() && !_radIsNotNew.isChecked())) {
			UserDialog.showDialog(this, "",
					"All information is required.", null);
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
		values.put(UserVehicleEntry.COLUMN_USE_TEMPLATE, _switchUseTemplate.isChecked());
		values.put(UserVehicleEntry.COLUMN_IS_NEW, isNew);

		boolean saveSuccess;

		if (_currentUri == null) {
			// insert new record
			values.put(UserVehicleEntry.COLUMN_CREATED_ON, new Date().getTime());
			Uri newUri = getContentResolver().insert(UserVehicleEntry.CONTENT_URI, values);
			// if using template, get maintenance items template accordingly
			if (newUri != null) {
				Cursor cursor1 = getContentResolver().query(
						newUri,
						UserVehicleEntry.FULL_PROJECTION,
						null,
						null,
						null
				);
				if (cursor1 != null) {
					if (cursor1.moveToFirst()) {
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
						UserVehicle userVehicle = new UserVehicle(cursor1);
						prefs.edit().putInt(getString(R.string.pref_session_vehicle),
								userVehicle.get_vehicleId()).apply();
						Log.v("CHECK_ME", String.valueOf(userVehicle.get_vehicleId()));
						if (_switchUseTemplate.isChecked()) {
							String vehicleId = VehicleTemplate.getFirebaseVehicleIdFromList(
									userVehicle.get_brand(),
									userVehicle.get_model(),
									userVehicle.get_variant());
							insertingDefaultMaintenanceItems = true;
							createFirebaseItems(vehicleId,
									userVehicle.get_vehicleId(),
									userVehicle.get_usage());
						} else {
							String vehicleId = "";

							if (variant.toUpperCase().equals(
									getString(R.string.hybrid_option).toUpperCase())) {
								vehicleId = "hybrid_general";
							} else if (variant.toUpperCase().equals(
									getString(R.string.manual_option).toUpperCase())) {
								vehicleId = "manual_general";
							} else {
								vehicleId = "auto_general";
							}

							insertingDefaultMaintenanceItems = true;
							createFirebaseItems(vehicleId,
									userVehicle.get_vehicleId(),
									userVehicle.get_usage());
						}
					}
					cursor1.close();
				}
			}
			saveSuccess = newUri != null;
		} else {
			// update existing record
			int rowsAffected = getContentResolver().update(_currentUri,
					values, null, null);
			saveSuccess = rowsAffected != 0;
		}
		if (saveSuccess) {
			// this is for if user is being guided by tutorial.
			// used by main activity
			// return save ok results.
			setResult(RESULT_OK);

			Toast.makeText(this, getString(R.string.saved_successfully),
					Toast.LENGTH_SHORT).show();


			if (!insertingDefaultMaintenanceItems) {
				finish();
			}
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
		// for the positive and negative buttons on the dialog.
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
		if (data != null && data.getCount() > 0) {
			if (data.moveToFirst()) {
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
		if (_initUserVehicle != null) {
			_switchUseTemplate.setChecked(_initUserVehicle.is_useTemplate());
		}
		if (FirebaseObj._vehicleTemplates.size() == 0) {
			// firebase vehicle templates not loaded yet. proceed to load first
			FirebaseDatabase _firebaseDatabase = FirebaseDatabase.getInstance();
			DatabaseReference _databaseReference =
					_firebaseDatabase.getReference().child("vehicle_template");
			_databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						FirebaseObj._vehicleTemplates
								.add(snapshot.getValue(VehicleTemplate.class));
					}
					// init spinners here
					initViewsAfterFirebase();
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {
				}
			});
		} else {
			// already loaded firebase vehicle templates. proceed initialising views
			initViewsAfterFirebase();
		}
	}

	private void initViewsAfterFirebase() {
		ArrayAdapter<String> _brandAdapter = new ArrayAdapter<>(VehicleEditorActivity.this,
				android.R.layout.simple_spinner_item, VehicleTemplate.getBrands(FirebaseObj._vehicleTemplates));
		_brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		_spinnerBrand.setAdapter(_brandAdapter);

		if (_initUserVehicle != null && _initUserVehicle.is_useTemplate()) {
			_spinnerBrand.setSelection(_brandAdapter
					.getPosition(_initUserVehicle.get_brand()));
		}

		setupSpinnerModel();
		if (_initUserVehicle != null && _initUserVehicle.is_useTemplate()) {
			_spinnerModel.setSelection(_modelAdapter
					.getPosition(_initUserVehicle.get_model()));
		}

		setupSpinnerVariant();
		if (_initUserVehicle != null && _initUserVehicle.is_useTemplate()) {
			_spinnerVariant.setSelection(_variantAdapter
					.getPosition(_initUserVehicle.get_variant()));
		}

		if (_initUserVehicle != null && !_initUserVehicle.is_useTemplate()) {
			_editBrand.setText(_initUserVehicle.get_brand());
			_editModel.setText(_initUserVehicle.get_model());
			_spinnerVariantCustom.setSelection(_variantAdapter_custom
					.getPosition(_initUserVehicle.get_variant()));
		}

		// if editing
		if (_initUserVehicle != null) {
			if (_initUserVehicle.is_isNew()) {
				_radIsNew.setChecked(true);
			} else {
				_radIsNotNew.setChecked(true);
			}
			_llNewVehicle.setVisibility(View.GONE); // not editable
		}
		_progressBar.setVisibility(View.GONE);
		findViewById(R.id.ll_content).setVisibility(View.VISIBLE);
	}

	private void setupSpinnerModel() {
		String selectedBrand = (String) _spinnerBrand.getSelectedItem();
		_modelAdapter = new ArrayAdapter<>(VehicleEditorActivity.this,
				android.R.layout.simple_spinner_item,
				VehicleTemplate.getModels(FirebaseObj._vehicleTemplates,
						selectedBrand));
		_modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		_spinnerModel.setAdapter(_modelAdapter);
	}

	private void setupSpinnerVariant() {
		String selectedBrand = (String) _spinnerBrand.getSelectedItem();
		String selectedModel = (String) _spinnerModel.getSelectedItem();
		_variantAdapter = new ArrayAdapter<>(VehicleEditorActivity.this,
				android.R.layout.simple_spinner_item,
				VehicleTemplate.getVariants(FirebaseObj._vehicleTemplates,
						selectedBrand, selectedModel));
		_variantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		_spinnerVariant.setAdapter(_variantAdapter);
	}

	private void createFirebaseItems(final String firebaseVehicleId,
									 final int vehicleId,
									 final int usage) {
		_llMask.setVisibility(View.VISIBLE);
		_progressBar.setVisibility(View.VISIBLE);

		if (firebaseVehicleId.length() > 0) {
			FirebaseObj.runCallbackMaintenanceDetails(firebaseVehicleId, new FirebaseObj() {
				@Override
				public void callback() {
					List<MaintenanceItem> items = _maintenanceItems.get(firebaseVehicleId);
					if (items != null) {
						for (MaintenanceItem item : items) {
							if (item.getUsage() == UserVehicleEntry.USAGE_ALL ||
									item.getUsage() == usage) {
								MaintenanceItemEntry.INSERT_MAINTENANCE_iTEM(
										VehicleEditorActivity.this,
										item.getItem(),
										item.getInspect_replace(),
										vehicleId,
										item.getFirst_distance(),
										item.getFirst_duration(),
										item.getDistance_interval(),
										item.getDuration_interval()
								);
							}
						}
					}
					finish();
				}
			});
		}
	}
}
