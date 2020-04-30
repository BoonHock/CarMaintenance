package com.example.carmaintenance;

import androidx.fragment.app.Fragment;

import com.example.carmaintenance.fragments.MaintenanceEditorFragment;

public class MaintenanceEditorActivity extends SingleFragmentActivity {
	@Override
	protected Fragment createFragment() {
		return new MaintenanceEditorFragment();
	}

//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//
////		setContentView(R.layout.activity_fragment);
////		// Load an ad into the AdMob banner view.
////		AdView adView = findViewById(R.id.adView);
////		AdRequest adRequest = new AdRequest.Builder().build();
////		adView.loadAd(adRequest);
//
////		getSupportFragmentManager()
////				.beginTransaction()
////				.replace(R.id.fragment_container, new MaintenanceEditorFragment())
////				.commit();
//
////		setContentView(R.layout.fragment_maintenance_editor);
////
////		Intent intent = getIntent();
////		_currentUri = intent.getData();
////		_calendarOdometer = DateUtilities
////				.getCalendarAtMidnight(Calendar.getInstance()); // default value
////
//////		_llMask = findViewById(R.id.ll_mask);
//////		_llNewItem = findViewById(R.id.ll_custom_item_edit);
//////		_editNewItemName = findViewById(R.id.edit_new_item_name);
//////		_editNewItemPrice = findViewById(R.id.edit_new_item_price);
//////		_txtCloseNewItem = findViewById(R.id.txt_close);
////
////		_viewContent = findViewById(R.id.content);
////		_progressBar = findViewById(R.id.progress_bar);
////		_spinnerVehicle = findViewById(R.id.spinner_vehicle);
////		_editDate = findViewById(R.id.edit_date);
////		_editOdometer = findViewById(R.id.edit_odometer);
////		_editRemarks = findViewById(R.id.edit_remarks);
////		_editTotal = findViewById(R.id.edit_total);
////		_btnSelectByTemplate = findViewById(R.id.btn_select_by_template);
////		_llInspectItems = findViewById(R.id.ll_inspect_items);
////		_llReplaceItems = findViewById(R.id.ll_replace_items);
////
//////		_llMask.setVisibility(View.GONE);
//////		_llNewItem.setVisibility(View.GONE);
////		_viewContent.setVisibility(View.INVISIBLE);
////		_progressBar.setVisibility(View.VISIBLE);
////
////		if (_currentUri == null) {
////			invalidateOptionsMenu();
////		} else {
////			// get existing records
////			// will populate @_mapInitInspect and @_mapInitReplace
////			Cursor cursor = getContentResolver().query(
////					_currentUri,
////					MaintenanceEntry.FULL_PROJECTION,
////					null,
////					null,
////					null);
////
////			if (cursor != null) {
////				if (cursor.moveToFirst()) {
////					_editVehicleId = cursor.getInt(cursor
////							.getColumnIndexOrThrow(MaintenanceEntry.COLUMN_VEHICLE));
////					_editOdometer.setText(String.valueOf(cursor.getInt(cursor
////							.getColumnIndexOrThrow(MaintenanceEntry.COLUMN_ODOMETER))));
////					_calendarOdometer.setTime(new Date(cursor.getLong(cursor
////							.getColumnIndexOrThrow(MaintenanceEntry.COLUMN_DATE))));
////				}
////				cursor.close();
////			}
////			getDbMaintenanceItems();
////		}
////		String extraVehicleId = getIntent().getStringExtra("vehicle_id");
////		if (extraVehicleId != null) {
////			// user setting specific vehicle's maintenance. disable spinner
////			_editVehicleId = Integer.parseInt(extraVehicleId);
////		}
////
////		_vehicleIds = SetupViews.setupVehicleRegNoSpinner(this, _spinnerVehicle);
////
////		if (_vehicleIds.isEmpty()) {
////			UserDialog.showDialog(this, "",
////					getString(R.string.no_vehicle_found),
////					new DialogInterface.OnDismissListener() {
////						@Override
////						public void onDismiss(DialogInterface dialog) {
////							if (dialog != null) {
////								finish();
////							}
////						}
////					});
////		}
////
////		// show default date
////		_editDate.setText(DateUtilities.dateToStringDate(_calendarOdometer.getTime()));
////
////		_editOdometer.setFilters(new InputFilter[]{
////				new InputFilter.LengthFilter(String.valueOf(OdometerEntry.DISTANCE_MAX).length())
////		});
//////		_editNewItemName.setFilters(new InputFilter[]{
//////				new InputFilter.LengthFilter(MaintenanceItemEntry.ITEM_NAME_MAX_LENGTH)
//////		});
////		_editRemarks.setFilters(new InputFilter[]{
////				new InputFilter.LengthFilter(MaintenanceEntry.REMARKS_MAX_LENGTH)
////		});
////
////		setupListeners();
////
////		// if user is editing, then pre select in spinner
////		if (_editVehicleId != -1) {
////			_spinnerVehicle.setSelection(_vehicleIds.indexOf(_editVehicleId));
////			_spinnerVehicle.setEnabled(false);
////		}
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.menu_editor, menu);
//		return true;
//	}
//
//	/**
//	 * This method is called after invalidateOptionsMenu(), so that the
//	 * menu can be updated (some menu items can be hidden or made visible).
//	 */
//	@Override
//	public boolean onPrepareOptionsMenu(Menu menu) {
//		super.onPrepareOptionsMenu(menu);
//		// If this is a new pet, hide the "Delete" menu item.
//		if (_currentUri == null) {
//			MenuItem menuItem = menu.findItem(R.id.action_delete);
//			menuItem.setVisible(false);
//		}
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//		switch (item.getItemId()) {
//			case R.id.action_save:
//				saveMaintenance();
//				return true;
//			case R.id.action_delete:
//				UserDialog.showDeleteConfirmationDialog(
//						this, getString(R.string.are_you_sure),
//						new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog, int which) {
//								deleteMaintenance();
//							}
//						});
//				return true;
//			case android.R.id.home:
//				finish();
//				return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
//
//	private void setupListeners() {
//		_editDate.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				DatePickerDialog datePickerDialog = new DatePickerDialog(
//						MaintenanceEditorActivity.this,
//						new DatePickerDialog.OnDateSetListener() {
//							@Override
//							public void onDateSet(DatePicker view, int year, int monthOfYear,
//												  int dayOfMonth) {
//								_calendarOdometer.set(Calendar.YEAR, year);
//								_calendarOdometer.set(Calendar.MONTH, monthOfYear);
//								_calendarOdometer.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//
//								_editDate.setText(DateUtilities
//										.dateToStringDate(_calendarOdometer.getTime()));
//							}
//						},
//						_calendarOdometer.get(Calendar.YEAR),
//						_calendarOdometer.get(Calendar.MONTH),
//						_calendarOdometer.get(Calendar.DAY_OF_MONTH));
//				// user cannot fill in future odometer. sounds logical right?
//				datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
//				datePickerDialog.show();
//			}
//		});
//		_editOdometer.addTextChangedListener(new TextWatcher() {
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//			}
//
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//				if (!TextUtils.isEmpty(_editOdometer.getText().toString().trim())) {
//					int distance = Integer.parseInt(_editOdometer.getText().toString().trim());
//					if (distance > OdometerEntry.DISTANCE_MAX || distance < OdometerEntry.DISTANCE_MIN) {
//						UserDialog.showDialog(MaintenanceEditorActivity.this, "",
//								getString(R.string.odometer_input_too_large), null);
//					}
//				}
//			}
//
//			@Override
//			public void afterTextChanged(Editable s) {
//			}
//		});
//
//		findViewById(R.id.btn_add_inspect).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
////				_llMask.setVisibility(View.VISIBLE);
////				_llNewItem.setVisibility(View.VISIBLE);
////
////				_editNewItemName.setText("");
////				_editNewItemPrice.setText("");
////				_editNewItemName.requestFocus();
////
////				Misc.showKeyboard(MaintenanceEditorActivity.this,
////						_editNewItemName);
//
//				_isAddingReplaceItem = false;
//			}
//		});
//		findViewById(R.id.btn_add_replace).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
////				_llMask.setVisibility(View.VISIBLE);
////				_llNewItem.setVisibility(View.VISIBLE);
////
////				_editNewItemName.setText("");
////				_editNewItemPrice.setText("");
////				_editNewItemName.requestFocus();
////
////				Misc.showKeyboard(MaintenanceEditorActivity.this,
////						_editNewItemName);
////
////				_isAddingReplaceItem = true;
//			}
//		});
////		findViewById(R.id.txt_add).setOnClickListener(new View.OnClickListener() {
////			@Override
////			public void onClick(View v) {
////				String itemName = _editNewItemName.getText().toString().trim();
////				String strPrice = _editNewItemPrice.getText().toString().trim();
////
////				if (TextUtils.isEmpty(itemName)) {
////					UserDialog.showDialog(MaintenanceEditorActivity.this,
////							"",
////							getString(R.string.item_name_required),
////							null);
////					return;
////				}
////
////				boolean itemAlreadyExists = false;
////
////				if (_isAddingReplaceItem) {
////					for (int i = 0, j = _llReplaceItems.getChildCount(); i < j; i++) {
////						TextView txtExistItem = _llReplaceItems.getChildAt(i).findViewById(R.id.txt_item);
////						if (txtExistItem.getText().toString().trim().toUpperCase()
////								.equals(itemName.toUpperCase())) {
////							itemAlreadyExists = true;
////							break;
////						}
////					}
////				} else {
////					for (int i = 0, j = _llInspectItems.getChildCount(); i < j; i++) {
////						TextView txtExistItem = _llInspectItems.getChildAt(i).findViewById(R.id.txt_item);
////						if (txtExistItem.getText().toString().trim().toUpperCase()
////								.equals(itemName.toUpperCase())) {
////							itemAlreadyExists = true;
////							break;
////						}
////					}
////				}
////
////				if (itemAlreadyExists) {
////					UserDialog.showDialog(MaintenanceEditorActivity.this,
////							"", getString(R.string.item_already_exists), null);
////					return;
////				}
////
////				View view = View.inflate(MaintenanceEditorActivity.this,
////						R.layout.template_maintenance_editor_item, null);
////
////				TextView txtItem = view.findViewById(R.id.txt_item);
////				EditText editPrice = view.findViewById(R.id.edit_price);
////				CheckBox cbItem = view.findViewById(R.id.cb_item);
////
////				if (_isAddingReplaceItem) {
////					_llReplaceItems.addView(view, 0);
////				} else {
////					_llInspectItems.addView(view, 0);
////				}
////
////				setMaintenanceItemListeners(editPrice, cbItem);
////
////				txtItem.setText(itemName);
////				editPrice.setText(strPrice);
////				if (!TextUtils.isEmpty(strPrice)) {
////					editPrice.setText(String.format(Locale.getDefault(),
////							"%.2f", Double.parseDouble(strPrice)));
////				}
////				cbItem.setChecked(true);
////
////				// close view
//////				_txtCloseNewItem.performClick();
////			}
////		});
////		_txtCloseNewItem.setOnClickListener(new View.OnClickListener() {
////			@Override
////			public void onClick(View v) {
////				Misc.hideKeyboard(MaintenanceEditorActivity.this);
////
////				_llMask.setVisibility(View.GONE);
////				_llNewItem.setVisibility(View.GONE);
////			}
////		});
////		_editNewItemPrice.setOnFocusChangeListener(_focusChangeFormatMoney);
//
//		_spinnerVehicle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//			@Override
//			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//				// if not editing vehicle or spinner selected editing vehicle, then display
//				// maintenance item. or else, if editing vehicle but spinner not selecting
//				// editing vehicle's ID, then this is only triggered by initialisation
//				// and should be ignored
//				Log.v("CHECK_ME", "" + _vehicleIds.get(_spinnerVehicle
//						.getSelectedItemPosition()) + " " + _editVehicleId);
//				Log.v("CHECK_ME", (new Gson()).toJson(_vehicleIds));
//
//				if (_editVehicleId == -1 || _vehicleIds.get(_spinnerVehicle
//						.getSelectedItemPosition()) == _editVehicleId) {
//					getMaintenanceItems();
//				}
//			}
//
//			@Override
//			public void onNothingSelected(AdapterView<?> parent) {
//			}
//		});
//
//		_btnSelectByTemplate.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//
//			}
//		});
//	}
//
//	private void getMaintenanceItems() {
//		int vehicleId = _vehicleIds.get(_spinnerVehicle.getSelectedItemPosition());
//
//		Cursor cursor = getContentResolver().query(
//				UserVehicleEntry.CONTENT_URI,
//				UserVehicleEntry.FULL_PROJECTION,
//				UserVehicleEntry._ID + "=?",
//				new String[]{String.valueOf(vehicleId)}, null);
//
//		if (cursor == null) {
//			return;
//		}
//		if (cursor.getCount() > 0) {
//			final UserVehicle userVehicle = new UserVehicle(cursor);
//
//			final String firebaseVehicleId = VehicleTemplate
//					.getVehicleIdFromList(FirebaseObj._vehicleTemplates,
//							userVehicle.get_brand(),
//							userVehicle.get_model(),
//							userVehicle.get_variant());
//
//			FirebaseObj.runCallbackMaintenanceDetails(firebaseVehicleId, new FirebaseObj() {
//				@Override
//				public void callback() {
//					List<MaintenanceItem> preCheckInspectList = new ArrayList<>();
//					List<MaintenanceItem> preCheckReplaceList = new ArrayList<>();
//
//					List<List<MaintenanceItem>> firebaseItems = FirebaseObj
//							.getItemsByInspectReplace(firebaseVehicleId, userVehicle.get_usage());
//					List<MaintenanceItem> maintenanceItemsInspect = firebaseItems.get(0);
//					List<MaintenanceItem> maintenanceItemsReplace = firebaseItems.get(1);
//
//					maintenanceItemsInspect.addAll(MaintenanceItem
//							.getCustomMaintenanceItemNotInFirebase(
//									MaintenanceEditorActivity.this,
//									maintenanceItemsInspect, userVehicle,
//									CustomMaintenanceItemEntry.INSPECT_VALUE));
//
//					maintenanceItemsReplace.addAll(MaintenanceItem
//							.getCustomMaintenanceItemNotInFirebase(
//									MaintenanceEditorActivity.this,
//									maintenanceItemsReplace, userVehicle,
//									CustomMaintenanceItemEntry.REPLACE_VALUE));
//
//					for (int i = maintenanceItemsInspect.size() - 1; i >= 0; i--) {
//						if (_preCheckInspect.contains(maintenanceItemsInspect.get(i).getItem())) {
//							maintenanceItemsInspect.remove(i);
//						}
//					}
//					for (int i = maintenanceItemsReplace.size() - 1; i >= 0; i--) {
//						if (_preCheckReplace.contains(maintenanceItemsReplace.get(i).getItem())) {
//							maintenanceItemsReplace.remove(i);
//						}
//					}
//
//					for (String item : _preCheckInspect) {
//						preCheckInspectList.add(new MaintenanceItem("",
//								item, CustomMaintenanceItemEntry.INSPECT_VALUE,
//								0, 0, 0, 0, 0));
//					}
//					for (String item : _preCheckReplace) {
//						preCheckReplaceList.add(new MaintenanceItem("",
//								item, CustomMaintenanceItemEntry.REPLACE_VALUE,
//								0, 0, 0, 0, 0));
//					}
//
//					Collections.sort(maintenanceItemsInspect);
//					Collections.sort(maintenanceItemsReplace);
//					Collections.sort(preCheckInspectList);
//					Collections.sort(preCheckReplaceList);
//
//					maintenanceItemsInspect.addAll(0, preCheckInspectList);
//					maintenanceItemsReplace.addAll(0, preCheckReplaceList);
//
//					displayMaintenanceItems(maintenanceItemsInspect, maintenanceItemsReplace);
//					_viewContent.setVisibility(View.VISIBLE);
//					_progressBar.setVisibility(View.GONE);
//				}
//			});
//		}
//		cursor.close();
//	}
//
//	// if editing, get maintenance items already selected previously
//	private void getDbMaintenanceItems() {
//		Uri uri = MaintenanceDetailsEntry.CONTENT_URI_MAINTENANCE;
//		long maintenanceId = ContentUris.parseId(_currentUri);
//		Cursor cursor = getContentResolver().query(
//				ContentUris.withAppendedId(uri, maintenanceId),
//				null,
//				null,
//				new String[]{String.valueOf(maintenanceId)},
//				null);
//
//		if (cursor != null) {
//			while (cursor.moveToNext()) {
//				String itemName = cursor.getString(cursor
//						.getColumnIndexOrThrow(MaintenanceItemEntry.COLUMN_ITEM));
//				double itemPrice = cursor.getDouble(cursor
//						.getColumnIndexOrThrow(MaintenanceDetailsEntry.COLUMN_PRICE));
//				switch (cursor.getInt(cursor.getColumnIndexOrThrow(MaintenanceItemEntry
//						.COLUMN_INSPECT_REPLACE))) {
//					case MaintenanceItemEntry.INSPECT_VALUE:
//						_preCheckInspect.add(itemName);
//						_mapInitInspect.put(itemName, itemPrice);
//						break;
//					case MaintenanceItemEntry.REPLACE_VALUE:
//						_preCheckReplace.add(itemName);
//						_mapInitReplace.put(itemName, itemPrice);
//						break;
//				}
//			}
//			cursor.close();
//		}
//	}
//
//	private void displayMaintenanceItems(List<MaintenanceItem> maintenanceItemsInspect,
//										 List<MaintenanceItem> maintenanceItemsReplace) {
//		LinearLayout llInspect = findViewById(R.id.ll_inspect);
//		LinearLayout llReplace = findViewById(R.id.ll_replace);
//
//		// remove all children and re-add them
//		_llInspectItems.removeAllViews();
//		_llReplaceItems.removeAllViews();
//
//		if (maintenanceItemsInspect.isEmpty()) {
//			llInspect.setVisibility(View.GONE);
//		} else {
//			llInspect.setVisibility(View.VISIBLE);
//		}
//		if (maintenanceItemsReplace.isEmpty()) {
//			llReplace.setVisibility(View.GONE);
//		} else {
//			llReplace.setVisibility(View.VISIBLE);
//		}
//		addMaintenanceItemLayouts(_llInspectItems, maintenanceItemsInspect, _mapInitInspect);
//		addMaintenanceItemLayouts(_llReplaceItems, maintenanceItemsReplace, _mapInitReplace);
//	}
//
//	private void addMaintenanceItemLayouts(
//			LinearLayout linearLayout, List<MaintenanceItem> maintenanceItems,
//			Map<String, Double> initPrice) {
//		for (MaintenanceItem maintenanceItem : maintenanceItems) {
//			View view = LayoutInflater.from(this).inflate(
//					R.layout.template_maintenance_editor_item, linearLayout, false);
//			TextView txtItem = view.findViewById(R.id.txt_item);
//			txtItem.setText(maintenanceItem.getItem());
//
//			final EditText editPrice = view.findViewById(R.id.edit_price);
//			final CheckBox cbItem = view.findViewById(R.id.cb_item);
//
//			if (initPrice.containsKey(maintenanceItem.getItem())) {
//				editPrice.setText(String.valueOf(initPrice.get(maintenanceItem.getItem())));
//				cbItem.setChecked(true);
//			}
//			setMaintenanceItemListeners(editPrice, cbItem);
//			linearLayout.addView(view);
//		}
//	}
//
//	private void setMaintenanceItemListeners(final EditText editPrice, final CheckBox cbItem) {
//		editPrice.addTextChangedListener(new TextWatcher() {
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//			}
//
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//				double totalPrice = 0;
//
//				if (!TextUtils.isEmpty(editPrice.getText().toString())) {
//					cbItem.setChecked(true);
//				}
//
//				for (int i = 0, j = _llInspectItems.getChildCount(); i < j; i++) {
//					EditText editPrice = _llInspectItems.getChildAt(i)
//							.findViewById(R.id.edit_price);
//					if (!TextUtils.isEmpty(editPrice.getText().toString())) {
//						totalPrice += Double.parseDouble(editPrice.getText().toString());
//					}
//				}
//				for (int i = 0, j = _llReplaceItems.getChildCount(); i < j; i++) {
//					EditText editPrice = _llReplaceItems.getChildAt(i)
//							.findViewById(R.id.edit_price);
//					if (!TextUtils.isEmpty(editPrice.getText().toString())) {
//						totalPrice += Double.parseDouble(editPrice.getText().toString());
//					}
//				}
//
//				_editTotal.setText(_decimalFormat.format(totalPrice));
//			}
//
//			@Override
//			public void afterTextChanged(Editable s) {
//			}
//		});
//		editPrice.setOnFocusChangeListener(_focusChangeFormatMoney);
//		cbItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				if (!isChecked) {
//					editPrice.setText("");
//				}
//			}
//		});
//	}
//
//	private void saveMaintenance() {
//		long maintenanceDate = DateUtilities
//				.getCalendarAtMidnight(_calendarOdometer).getTime().getTime();
//		String strOdometerDistance = _editOdometer.getText().toString();
//		int vehicleId = getVehicleId();
//		String remarks = _editRemarks.getText().toString().trim();
//
//		if (TextUtils.isEmpty(strOdometerDistance)) {
//			UserDialog.showDialog(this, "",
//					getString(R.string.odometer_not_provided),
//					null);
//			return;
//		}
//		if (!hasCheckedMaintenanceItem()) {
//			UserDialog.showDialog(this, "",
//					getString(R.string.select_one_maintenance_item),
//					null);
//			return;
//		}
//
//		saveOdometer(vehicleId, maintenanceDate, strOdometerDistance);
//
//		Log.v("VEHICLE_ID_CHECK", "CHECK: " + vehicleId);
//
//		ContentValues values = new ContentValues();
//		values.put(MaintenanceEntry.COLUMN_VEHICLE, vehicleId);
//		values.put(MaintenanceEntry.COLUMN_DATE, maintenanceDate);
//		values.put(MaintenanceEntry.COLUMN_ODOMETER, Integer.valueOf(strOdometerDistance));
//		values.put(MaintenanceEntry.COLUMN_REMARKS, remarks);
//		values.put(MaintenanceEntry.COLUMN_CREATED_ON, Calendar.getInstance().getTimeInMillis());
//
//		if (_currentUri != null) {
//			getContentResolver().delete(_currentUri, null, null);
//		}
//		/*
//		// for now, to simplify development, delete maintenance if editing
//		// and insert new one again
//		int maintenanceId = -1;
//		if (_currentUri == null) {
//			// insert maintenance
//			Uri newUri = getContentResolver().insert(MaintenanceEntry.CONTENT_URI, values);
//			if (newUri != null) {
//				maintenanceId = (int) ContentUris.parseId(newUri);
//			}
//		} else {
//			// update maintenance
//			int rowsAffected = getContentResolver()
//					.update(_currentUri, values, null, null);
//			if (rowsAffected != 0) {
//				maintenanceId = (int) ContentUris.parseId(_currentUri);
//			}
//		}
//		if (maintenanceId == -1) {
//			UserDialog.showDialog(this,
//					"",
//					"An error has occurred.",
//					null);
//			return;
//		}
//		*/
//		Uri newMaintenanceUri = getContentResolver().insert(MaintenanceEntry.CONTENT_URI, values);
//		if (newMaintenanceUri == null) {
//			UserDialog.showDialog(this,
//					getString(R.string.error_has_occurred),
//					"Failed to save maintenance data.",
//					null);
//			return;
//		}
//		int maintenanceId = (int) ContentUris.parseId(newMaintenanceUri);
//
//		if (saveMaintenanceDetails(maintenanceId,
//				MaintenanceItemEntry.REPLACE_VALUE, _llReplaceItems) &&
//				saveMaintenanceDetails(maintenanceId,
//						MaintenanceItemEntry.INSPECT_VALUE, _llInspectItems)) {
//			Toast.makeText(this, getString(R.string.saved_successfully),
//					Toast.LENGTH_SHORT).show();
//			finish();
//		}
//	}
//
//	private boolean saveMaintenanceDetails(int maintenanceId, int inspect_replace, LinearLayout linearLayout) {
//		for (int i = 0, j = linearLayout.getChildCount(); i < j; i++) {
//			CheckBox cb = linearLayout.getChildAt(i).findViewById(R.id.cb_item);
//
//			if (!cb.isChecked()) {
//				continue;
//			}
//
//			String itemName = ((TextView) linearLayout.getChildAt(i)
//					.findViewById(R.id.txt_item)).getText().toString().trim();
//			String itemPrice = ((TextView) linearLayout.getChildAt(i)
//					.findViewById(R.id.edit_price)).getText().toString().trim();
//
//			// price is two decimal places only
//			double doublePrice = TextUtils.isEmpty(itemPrice) ?
//					0 : Math.round(Double.parseDouble(itemPrice) * 100.0) / 100.0;
//
//			Cursor itemCursor = getContentResolver().query(
//					MaintenanceItemEntry.CONTENT_URI,
//					MaintenanceItemEntry.FULL_PROJECTION,
//					MaintenanceItemEntry.COLUMN_ITEM + "=? AND "
//							+ MaintenanceItemEntry.COLUMN_INSPECT_REPLACE + "=?",
//					new String[]{itemName, String.valueOf(inspect_replace)},
//					null);
//
//			long itemId = -1;
//
//			if (itemCursor != null) {
//				if (itemCursor.getCount() == 0) {
//					ContentValues itemValues = new ContentValues();
//					itemValues.put(MaintenanceItemEntry.COLUMN_ITEM, itemName);
//					itemValues.put(MaintenanceItemEntry.COLUMN_INSPECT_REPLACE,
//							inspect_replace);
//					Uri newItemUri = getContentResolver()
//							.insert(MaintenanceItemEntry.CONTENT_URI, itemValues);
//
//					if (newItemUri != null) {
//						itemId = ContentUris.parseId(newItemUri);
//					}
//				} else if (itemCursor.moveToFirst()) {
//					itemId = itemCursor.getInt(itemCursor
//							.getColumnIndexOrThrow(MaintenanceItemEntry._ID));
//				}
//				itemCursor.close();
//			}
//
//			if (itemId == -1) {
//				// invalid item ID!!
//				UserDialog.showDialog(this,
//						getString(R.string.error_has_occurred),
//						getString(R.string.maintenance_item_save_failed),
//						null);
//				return false;
//			}
//
//			ContentValues values = new ContentValues();
//			values.put(MaintenanceDetailsEntry.COLUMN_MAINTENANCE_ID, maintenanceId);
//			values.put(MaintenanceDetailsEntry.COLUMN_ITEM, itemId);
//			values.put(MaintenanceDetailsEntry.COLUMN_PRICE, doublePrice);
//
//			Uri newUri = getContentResolver()
//					.insert(MaintenanceDetailsEntry.CONTENT_URI, values);
//
//			if (newUri == null) {
//				UserDialog.showDialog(this,
//						getString(R.string.error_has_occurred),
//						getString(R.string.maintenance_details_save_failed),
//						null);
//				return false;
//			}
//		}
//		return true;
//	}
//
//	private void saveOdometer(int vehicleId, long odometerDate, String strOdometerDistance) {
//		Cursor cursor = getContentResolver().query(OdometerEntry.CONTENT_URI,
//				OdometerEntry.FULL_PROJECTION,
//				OdometerEntry.COLUMN_VEHICLE + "=? AND "
//						+ OdometerEntry.COLUMN_DATE + "=?",
//				new String[]{String.valueOf(vehicleId),
//						String.valueOf(odometerDate)},
//				null);
//
//		ContentValues values = new ContentValues();
//		values.put(OdometerEntry.COLUMN_VEHICLE, vehicleId);
//		values.put(OdometerEntry.COLUMN_DATE, odometerDate);
//		values.put(OdometerEntry.COLUMN_DISTANCE, Integer.valueOf(strOdometerDistance));
//
//		if (cursor != null) {
//			if (cursor.getCount() > 0 && cursor.moveToFirst()) {
//				// update odometer
//				// update 20200424: don't update odometer. just insert if not record
////				Uri odometerUri = Uri.withAppendedPath(APP_MASTER_CONTRACT.BASE_CONTENT_URI,
////						OdometerContract.PATH_ODOMETER + "/"
////								+ cursor.getLong(cursor.getColumnIndexOrThrow(OdometerEntry._ID)));
////				getContentResolver().update(odometerUri, values, null, null);
//			} else {
//				// insert odometer
//				getContentResolver().insert(OdometerEntry.CONTENT_URI, values);
//			}
//			cursor.close();
//		}
//	}
//
//	private int getVehicleId() {
//		return _vehicleIds.get(_spinnerVehicle.getSelectedItemPosition());
//	}
//
//	private boolean hasCheckedMaintenanceItem() {
//		for (int i = 0, j = _llInspectItems.getChildCount(); i < j; i++) {
//			if (((CheckBox) _llInspectItems.getChildAt(i)
//					.findViewById(R.id.cb_item)).isChecked()) {
//				return true;
//			}
//		}
//		for (int i = 0, j = _llReplaceItems.getChildCount(); i < j; i++) {
//			if (((CheckBox) _llReplaceItems.getChildAt(i)
//					.findViewById(R.id.cb_item)).isChecked()) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	private void deleteMaintenance() {
//		if (_currentUri == null) {
//			return;
//		}
//		int rowsDeleted = getContentResolver().delete(_currentUri, null, null);
//
//		if (rowsDeleted == 0) {
//			Toast.makeText(this, getString(R.string.error_has_occurred),
//					Toast.LENGTH_SHORT).show();
//		} else {
//			Toast.makeText(this, getString(R.string.maintenance_deleted),
//					Toast.LENGTH_SHORT).show();
//			finish();
//		}
//	}
}
