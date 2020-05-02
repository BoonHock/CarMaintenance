package com.incupe.vewec.fragments;

import android.app.Activity;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.incupe.vewec.R;
import com.incupe.vewec.data.APP_MASTER_CONTRACT;
import com.incupe.vewec.data.CustomMaintenanceItemContract.CustomMaintenanceItemEntry;
import com.incupe.vewec.data.MaintenanceContract.MaintenanceEntry;
import com.incupe.vewec.data.MaintenanceDetailsContract.MaintenanceDetailsEntry;
import com.incupe.vewec.data.MaintenanceItemContract.MaintenanceItemEntry;
import com.incupe.vewec.data.OdometerContract;
import com.incupe.vewec.data.OdometerContract.OdometerEntry;
import com.incupe.vewec.data.UserVehicleContract.UserVehicleEntry;
import com.incupe.vewec.objects.FirebaseObj;
import com.incupe.vewec.objects.MaintenanceItem;
import com.incupe.vewec.objects.UserVehicle;
import com.incupe.vewec.objects.VehicleTemplate;
import com.incupe.vewec.utilities.DateUtilities;
import com.incupe.vewec.utilities.SetupViews;
import com.incupe.vewec.utilities.UserDialog;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MaintenanceEditorFragment extends Fragment {
	private static final String DIALOG_ITEM = "DIALOG_ITEM";
	private static final String DIALOG_DATE = "DIALOG_DATE";
	private static final String DIALOG_TEMPLATE = "DIALOG_TEMPLATE";
	private static final int DIALOG_ITEM_RESULT = 0;
	private static final int DIALOG_DATE_RESULT = 1;
	private static final int DIALOG_TEMPLATE_RESULT = 3;

	private Uri _currentUri;

	private List<Integer> _vehicleIds;

	private Calendar _calendarOdometer;
	private final DecimalFormat _decimalFormat = new DecimalFormat("0.00");

	private ScrollView _viewContent;
	private ProgressBar _progressBar;
	private Spinner _spinnerVehicle;
	private EditText _editDate;
	private EditText _editOdometer;
	private EditText _editRemarks;
	private EditText _editTotal;
	private Button _btnSelectByTemplate;
	private LinearLayout _llInspectItems;
	private LinearLayout _llReplaceItems;

	private List<String> _preCheckInspect = new ArrayList<>();
	private List<String> _preCheckReplace = new ArrayList<>();
	private Map<String, Double> _mapInitInspect = new HashMap<>();
	private Map<String, Double> _mapInitReplace = new HashMap<>();

	private ArrayList<Integer> _distanceIntervals;
	private ArrayList<Integer> _durationIntervals;

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

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		setHasOptionsMenu(true); // to indicate this fragment has options menu

		View view = inflater.inflate(R.layout.fragment_maintenance_editor,
				container, false);

		Intent intent = requireActivity().getIntent();
		_currentUri = intent.getData();

		_calendarOdometer = DateUtilities
				.getCalendarAtMidnight(Calendar.getInstance()); // default value
		_viewContent = view.findViewById(R.id.content);
		_progressBar = view.findViewById(R.id.progress_bar);
		_spinnerVehicle = view.findViewById(R.id.spinner_vehicle);
		_editDate = view.findViewById(R.id.edit_date);
		_editOdometer = view.findViewById(R.id.edit_odometer);
		_editRemarks = view.findViewById(R.id.edit_remarks);
		_editTotal = view.findViewById(R.id.edit_total);
		_btnSelectByTemplate = view.findViewById(R.id.btn_select_by_template);
		_llInspectItems = view.findViewById(R.id.ll_inspect_items);
		_llReplaceItems = view.findViewById(R.id.ll_replace_items);

		_viewContent.setVisibility(View.INVISIBLE);
		_progressBar.setVisibility(View.VISIBLE);

		if (_currentUri == null) {
			requireActivity().invalidateOptionsMenu();
		} else {
			// get existing records
			// will populate @_mapInitInspect and @_mapInitReplace
			Cursor cursor = requireContext().getContentResolver().query(
					_currentUri,
					MaintenanceEntry.FULL_PROJECTION,
					null,
					null,
					null);

			if (cursor != null) {
				if (cursor.moveToFirst()) {
					_editVehicleId = cursor.getInt(cursor
							.getColumnIndexOrThrow(MaintenanceEntry.COLUMN_VEHICLE));
					_editOdometer.setText(String.valueOf(cursor.getInt(cursor
							.getColumnIndexOrThrow(MaintenanceEntry.COLUMN_ODOMETER))));
					_calendarOdometer.setTime(new Date(cursor.getLong(cursor
							.getColumnIndexOrThrow(MaintenanceEntry.COLUMN_DATE))));
				}
				cursor.close();
			}
			getDbMaintenanceItems();
		}

		String extraVehicleId = requireActivity().getIntent().getStringExtra("vehicle_id");
		if (extraVehicleId != null) {
			// user setting specific vehicle's maintenance. disable spinner
			_editVehicleId = Integer.parseInt(extraVehicleId);
		}
		_vehicleIds = SetupViews.setupVehicleRegNoSpinner(requireContext(), _spinnerVehicle);
		if (_vehicleIds.isEmpty()) {
			UserDialog.showDialog(requireContext(), "",
					getString(R.string.no_vehicle_found),
					new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							if (dialog != null) {
								requireActivity().finish();
							}
						}
					});
		}

		// show default date
		_editDate.setText(DateUtilities.dateToStringDate(_calendarOdometer.getTime()));

		_editOdometer.setFilters(new InputFilter[]{
				new InputFilter.LengthFilter(String.valueOf(OdometerEntry.DISTANCE_MAX).length())
		});
//		_editNewItemName.setFilters(new InputFilter[]{
//				new InputFilter.LengthFilter(MaintenanceItemEntry.ITEM_NAME_MAX_LENGTH)
//		});
		_editRemarks.setFilters(new InputFilter[]{
				new InputFilter.LengthFilter(MaintenanceEntry.REMARKS_MAX_LENGTH)
		});

		setupListeners(view);

		// if user is editing, then pre select in spinner
		if (_editVehicleId != -1) {
			_spinnerVehicle.setSelection(_vehicleIds.indexOf(_editVehicleId));
			_spinnerVehicle.setEnabled(false);
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
		// If this is a new pet, hide the "Delete" menu item.
		if (_currentUri == null) {
			MenuItem menuItem = menu.findItem(R.id.action_delete);
			menuItem.setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_save:
				saveMaintenance();
				return true;
			case R.id.action_delete:
				UserDialog.showDeleteConfirmationDialog(
						requireContext(), getString(R.string.are_you_sure),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								deleteMaintenance();
							}
						});
				return true;
			case android.R.id.home:
				requireActivity().finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setupListeners(View view) {
		_editDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentManager manager = requireActivity().getSupportFragmentManager();
				DatePickerFragment dialog = DatePickerFragment
						.newInstance(_calendarOdometer.getTime(), new Date(), null);
				dialog.setTargetFragment(MaintenanceEditorFragment.this,
						DIALOG_DATE_RESULT);
				dialog.show(manager, DIALOG_DATE);
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
						UserDialog.showDialog(requireActivity(), "",
								getString(R.string.odometer_input_too_large), null);
					}
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		view.findViewById(R.id.btn_add_inspect).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_isAddingReplaceItem = false;
				FragmentManager manager = requireActivity().getSupportFragmentManager();
				MaintenanceEditorItemEditorFragment dialog =
						MaintenanceEditorItemEditorFragment.newInstance();
				dialog.setTargetFragment(MaintenanceEditorFragment.this,
						DIALOG_ITEM_RESULT);
				dialog.show(manager, DIALOG_ITEM);
			}
		});
		view.findViewById(R.id.btn_add_replace).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_isAddingReplaceItem = true;
				FragmentManager manager = requireActivity().getSupportFragmentManager();
				MaintenanceEditorItemEditorFragment dialog =
						MaintenanceEditorItemEditorFragment.newInstance();
				dialog.setTargetFragment(MaintenanceEditorFragment.this,
						DIALOG_ITEM_RESULT);
				dialog.show(manager, DIALOG_ITEM);
			}
		});

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

		final BottomSheetDialog bottomSheetDialog =
				new BottomSheetDialog(requireActivity());

		final View sheetView = requireActivity().getLayoutInflater()
				.inflate(R.layout.bottom_sheet_maintenance_template,
						(ViewGroup) view, false);
		bottomSheetDialog.setContentView(sheetView);

		_btnSelectByTemplate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bottomSheetDialog.show();
			}
		});

		TextView txtBottomDistance = bottomSheetDialog.findViewById(R.id.bottom_sheet_distance);
		TextView txtBottomDuration = bottomSheetDialog.findViewById(R.id.bottom_sheet_duration);

		if (txtBottomDistance != null) {
			txtBottomDistance.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					FragmentManager manager = requireActivity().getSupportFragmentManager();
					MaintenanceTemplateDialogFragment dialog = MaintenanceTemplateDialogFragment
							.newInstance(_distanceIntervals,
									MaintenanceTemplateDialogFragment.BY_DISTANCE);
					dialog.setTargetFragment(MaintenanceEditorFragment.this,
							DIALOG_TEMPLATE_RESULT);
					dialog.show(manager, DIALOG_TEMPLATE);
					bottomSheetDialog.hide();
				}
			});
		}
		if (txtBottomDuration != null) {
			txtBottomDuration.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					FragmentManager manager = requireActivity().getSupportFragmentManager();
					MaintenanceTemplateDialogFragment dialog = MaintenanceTemplateDialogFragment
							.newInstance(_durationIntervals,
									MaintenanceTemplateDialogFragment.BY_DURATION);
					dialog.setTargetFragment(MaintenanceEditorFragment.this,
							DIALOG_TEMPLATE_RESULT);
					dialog.show(manager, DIALOG_TEMPLATE);
					bottomSheetDialog.hide();
				}
			});
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (resultCode != Activity.RESULT_OK || data == null) {
			return;
		}
		if (requestCode == DIALOG_ITEM_RESULT) {
			String itemName = (String) data.getSerializableExtra(
					MaintenanceEditorItemEditorFragment.EXTRA_NAME);
			double price = (double) data.getSerializableExtra(
					MaintenanceEditorItemEditorFragment.EXTRA_PRICE);

			if (itemName == null) {
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
				UserDialog.showDialog(requireContext(),
						"", getString(R.string.item_already_exists), null);
				return;
			}
			View view = View.inflate(requireContext(),
					R.layout.template_maintenance_editor_item, null);
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
			// set distance and duration intervals as zero
			txtItem.setTag(R.string.by_distance_interval, 0);
			txtItem.setTag(R.string.by_duration_interval, 0);

			if (price != 0) {
				String strPrice = String.valueOf(price);
				editPrice.setText(strPrice);
				editPrice.setText(String.format(Locale.getDefault(), "%.2f", price));
			}
			cbItem.setChecked(true);
		} else if (requestCode == DIALOG_DATE_RESULT) {
			Date date = (Date) data.getSerializableExtra(DatePickerFragment.ARG_DATE);

			if (date == null) {
				return;
			}
			_calendarOdometer.setTime(date);
			_editDate.setText(DateUtilities.dateToStringDate(date));
		} else if (requestCode == DIALOG_TEMPLATE_RESULT) {
			int interval = (int) data.getSerializableExtra(
					MaintenanceTemplateDialogFragment.EXTRA_RESULT);
			int type = (int) data.getSerializableExtra(
					MaintenanceTemplateDialogFragment.EXTRA_TYPE);

			if (type == MaintenanceTemplateDialogFragment.BY_DISTANCE) {
				selectByTemplate(_llInspectItems,
						R.string.by_distance_interval, interval);
				selectByTemplate(_llReplaceItems,
						R.string.by_distance_interval, interval);
			} else {
				selectByTemplate(_llInspectItems,
						R.string.by_duration_interval, interval);
				selectByTemplate(_llReplaceItems,
						R.string.by_duration_interval, interval);
			}
		}
	}

	private void selectByTemplate(LinearLayout linearLayout, int tagId, int intervalValue) {
		for (int i = 0, j = linearLayout.getChildCount(); i < j; i++) {
			View parentView = linearLayout.getChildAt(i);
			TextView txtItem = parentView.findViewById(R.id.txt_item);
			EditText editPrice = parentView.findViewById(R.id.edit_price);
			CheckBox cb = parentView.findViewById(R.id.cb_item);

			int itemInterval = (int) txtItem.getTag(tagId);

			if (itemInterval != 0 && intervalValue % itemInterval == 0) {
				cb.setChecked(true);
			} else {
				editPrice.setText("");
				cb.setChecked(false);
			}
		}
	}

	private void getMaintenanceItems() {
		int vehicleId = _vehicleIds.get(_spinnerVehicle.getSelectedItemPosition());

		Cursor cursor = requireContext().getContentResolver().query(
				UserVehicleEntry.CONTENT_URI,
				UserVehicleEntry.FULL_PROJECTION,
				UserVehicleEntry._ID + "=?",
				new String[]{String.valueOf(vehicleId)}, null);

		if (cursor == null) {
			return;
		}
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
					List<MaintenanceItem> preCheckInspectList = new ArrayList<>();
					List<MaintenanceItem> preCheckReplaceList = new ArrayList<>();

					List<List<MaintenanceItem>> firebaseItems = FirebaseObj
							.getItemsByInspectReplace(firebaseVehicleId, userVehicle.get_usage());
					List<MaintenanceItem> maintenanceItemsInspect = firebaseItems.get(0);
					List<MaintenanceItem> maintenanceItemsReplace = firebaseItems.get(1);

					maintenanceItemsInspect.addAll(MaintenanceItem
							.getCustomMaintenanceItemNotInFirebase(
									requireActivity(), maintenanceItemsInspect,
									CustomMaintenanceItemEntry.INSPECT_VALUE));

					maintenanceItemsReplace.addAll(MaintenanceItem
							.getCustomMaintenanceItemNotInFirebase(
									requireActivity(), maintenanceItemsReplace,
									CustomMaintenanceItemEntry.REPLACE_VALUE));

					for (int i = maintenanceItemsInspect.size() - 1; i >= 0; i--) {
						if (_preCheckInspect.contains(maintenanceItemsInspect.get(i).getItem())) {
							maintenanceItemsInspect.remove(i);
						}
					}
					for (int i = maintenanceItemsReplace.size() - 1; i >= 0; i--) {
						if (_preCheckReplace.contains(maintenanceItemsReplace.get(i).getItem())) {
							maintenanceItemsReplace.remove(i);
						}
					}

					for (String item : _preCheckInspect) {
						preCheckInspectList.add(new MaintenanceItem("",
								item, CustomMaintenanceItemEntry.INSPECT_VALUE,
								0, 0, 0, 0, 0));
					}
					for (String item : _preCheckReplace) {
						preCheckReplaceList.add(new MaintenanceItem("",
								item, CustomMaintenanceItemEntry.REPLACE_VALUE,
								0, 0, 0, 0, 0));
					}

					Collections.sort(maintenanceItemsInspect);
					Collections.sort(maintenanceItemsReplace);
					Collections.sort(preCheckInspectList);
					Collections.sort(preCheckReplaceList);

					maintenanceItemsInspect.addAll(0, preCheckInspectList);
					maintenanceItemsReplace.addAll(0, preCheckReplaceList);

					_distanceIntervals = new ArrayList<>();
					_durationIntervals = new ArrayList<>();

					for (MaintenanceItem item : maintenanceItemsInspect) {
						if (!_distanceIntervals.contains(item.getDistance_interval())) {
							_distanceIntervals.add(item.getDistance_interval());
						}
						if (!_durationIntervals.contains(item.getDuration_interval())) {
							_durationIntervals.add(item.getDuration_interval());
						}
					}
					for (MaintenanceItem item : maintenanceItemsReplace) {
						if (!_distanceIntervals.contains(item.getDistance_interval())) {
							_distanceIntervals.add(item.getDistance_interval());
						}
						if (!_durationIntervals.contains(item.getDuration_interval())) {
							_durationIntervals.add(item.getDuration_interval());
						}
					}

					Collections.sort(_distanceIntervals);
					Collections.sort(_durationIntervals);

					// no need show 0
					if (_distanceIntervals.contains(0)) {
						_distanceIntervals.remove(0);
					}
					if (_durationIntervals.contains(0)) {
						_durationIntervals.remove(0);
					}

					displayMaintenanceItems(maintenanceItemsInspect, maintenanceItemsReplace);
					_viewContent.setVisibility(View.VISIBLE);
					_progressBar.setVisibility(View.GONE);
				}
			});
		}
		cursor.close();
	}

	// if editing, get maintenance items already selected previously
	private void getDbMaintenanceItems() {
		_preCheckInspect.clear();
		_preCheckReplace.clear();
		_mapInitInspect.clear();
		_mapInitReplace.clear();

		Uri uri = MaintenanceDetailsEntry.CONTENT_URI_MAINTENANCE;
		long maintenanceId = ContentUris.parseId(_currentUri);
		Cursor cursor = requireContext().getContentResolver().query(
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

	private void displayMaintenanceItems(List<MaintenanceItem> maintenanceItemsInspect,
										 List<MaintenanceItem> maintenanceItemsReplace) {
		LinearLayout llInspect = requireActivity().findViewById(R.id.ll_inspect);
		LinearLayout llReplace = requireActivity().findViewById(R.id.ll_replace);

		// remove all children and re-add them
		_llInspectItems.removeAllViews();
		_llReplaceItems.removeAllViews();

		if (maintenanceItemsInspect.isEmpty()) {
			llInspect.setVisibility(View.GONE);
		} else {
			llInspect.setVisibility(View.VISIBLE);
		}
		if (maintenanceItemsReplace.isEmpty()) {
			llReplace.setVisibility(View.GONE);
		} else {
			llReplace.setVisibility(View.VISIBLE);
		}
		addMaintenanceItemLayouts(_llInspectItems, maintenanceItemsInspect, _mapInitInspect);
		addMaintenanceItemLayouts(_llReplaceItems, maintenanceItemsReplace, _mapInitReplace);
	}

	private void addMaintenanceItemLayouts(
			LinearLayout linearLayout, List<MaintenanceItem> maintenanceItems,
			Map<String, Double> initPrice) {
		for (MaintenanceItem maintenanceItem : maintenanceItems) {
			View view = LayoutInflater.from(requireContext()).inflate(
					R.layout.template_maintenance_editor_item, linearLayout, false);
			TextView txtItem = view.findViewById(R.id.txt_item);
			txtItem.setText(maintenanceItem.getItem());
			txtItem.setTag(R.string.by_distance_interval, maintenanceItem.getDistance_interval());
			txtItem.setTag(R.string.by_duration_interval, maintenanceItem.getDuration_interval());

			final EditText editPrice = view.findViewById(R.id.edit_price);
			final CheckBox cbItem = view.findViewById(R.id.cb_item);

			if (initPrice.containsKey(maintenanceItem.getItem())) {
				editPrice.setText(String.valueOf(initPrice.get(maintenanceItem.getItem())));
				cbItem.setChecked(true);
			}
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
			UserDialog.showDialog(requireContext(), "",
					getString(R.string.odometer_not_provided),
					null);
			return;
		}
		if (!hasCheckedMaintenanceItem()) {
			UserDialog.showDialog(requireContext(), "",
					getString(R.string.select_one_maintenance_item),
					null);
			return;
		}

		saveOdometer(vehicleId, maintenanceDate, Integer.parseInt(strOdometerDistance));

		Log.v("VEHICLE_ID_CHECK", "CHECK: " + vehicleId);

		ContentValues values = new ContentValues();
		values.put(MaintenanceEntry.COLUMN_VEHICLE, vehicleId);
		values.put(MaintenanceEntry.COLUMN_DATE, maintenanceDate);
		values.put(MaintenanceEntry.COLUMN_ODOMETER, Integer.valueOf(strOdometerDistance));
		values.put(MaintenanceEntry.COLUMN_REMARKS, remarks);
		values.put(MaintenanceEntry.COLUMN_CREATED_ON, Calendar.getInstance().getTimeInMillis());

		if (_currentUri != null) {
			requireContext().getContentResolver().delete(_currentUri, null, null);
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
		Uri newMaintenanceUri = requireContext().getContentResolver()
				.insert(MaintenanceEntry.CONTENT_URI, values);
		if (newMaintenanceUri == null) {
			UserDialog.showDialog(requireContext(),
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
			Toast.makeText(requireContext(), getString(R.string.saved_successfully),
					Toast.LENGTH_SHORT).show();
			requireActivity().finish();
		}
	}

	private boolean saveMaintenanceDetails(int maintenanceId, int inspect_replace,
										   LinearLayout linearLayout) {
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
			double doublePrice = TextUtils.isEmpty(itemPrice) ?
					0 : Math.round(Double.parseDouble(itemPrice) * 100.0) / 100.0;

			Cursor itemCursor = requireContext().getContentResolver().query(
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
					Uri newItemUri = requireContext().getContentResolver()
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
				UserDialog.showDialog(requireContext(),
						getString(R.string.error_has_occurred),
						getString(R.string.maintenance_item_save_failed),
						null);
				return false;
			}

			ContentValues values = new ContentValues();
			values.put(MaintenanceDetailsEntry.COLUMN_MAINTENANCE_ID, maintenanceId);
			values.put(MaintenanceDetailsEntry.COLUMN_ITEM, itemId);
			values.put(MaintenanceDetailsEntry.COLUMN_PRICE, doublePrice);

			Uri newUri = requireContext().getContentResolver()
					.insert(MaintenanceDetailsEntry.CONTENT_URI, values);

			if (newUri == null) {
				UserDialog.showDialog(requireContext(),
						getString(R.string.error_has_occurred),
						getString(R.string.maintenance_details_save_failed),
						null);
				return false;
			}
		}
		return true;
	}

	private void saveOdometer(int vehicleId, long odometerDate, int odometerDistance) {
		Cursor cursor = requireContext().getContentResolver().query(OdometerEntry.CONTENT_URI,
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
					requireContext().getContentResolver()
							.update(odometerUri, values, null, null);
				}
			} else {
				// insert odometer
				requireContext().getContentResolver().insert(OdometerEntry.CONTENT_URI, values);
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
		int rowsDeleted = requireContext().getContentResolver().delete(_currentUri, null, null);

		if (rowsDeleted == 0) {
			Toast.makeText(requireContext(), getString(R.string.error_has_occurred),
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(requireContext(), getString(R.string.maintenance_deleted),
					Toast.LENGTH_SHORT).show();
			requireActivity().finish();
		}
	}

}
