package com.incupe.vewec.fragments;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.incupe.vewec.MaintenanceEditorActivity;
import com.incupe.vewec.OdometerEditorActivity;
import com.incupe.vewec.R;
import com.incupe.vewec.cursoradapter.UpcomingMaintenanceCursorAdapter;
import com.incupe.vewec.data.CustomMaintenanceItemContract.CustomMaintenanceItemEntry;
import com.incupe.vewec.data.MaintenanceContract.MaintenanceEntry;
import com.incupe.vewec.data.MaintenanceDetailsContract.MaintenanceDetailsEntry;
import com.incupe.vewec.data.MaintenanceItemContract.MaintenanceItemEntry;
import com.incupe.vewec.data.OdometerContract.OdometerEntry;
import com.incupe.vewec.data.UserVehicleContract.UserVehicleEntry;
import com.incupe.vewec.objects.FirebaseObj;
import com.incupe.vewec.objects.MaintenanceItem;
import com.incupe.vewec.objects.UpcomingMaintenanceItem;
import com.incupe.vewec.objects.UserVehicle;
import com.incupe.vewec.objects.VehicleTemplate;
import com.incupe.vewec.utilities.DateUtilities;
import com.incupe.vewec.utilities.SetupViews;
import com.incupe.vewec.utilities.UserDialog;

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
	private LinearLayout _llReplace;
	private LinearLayout _llInspect;

	private ArrayList<Integer> _distanceIntervals;
	private ArrayList<Integer> _durationIntervals;

	private int _editVehicleId = -1;
	private boolean _isInit = true;
	private boolean _isAddingReplaceItem = true;
	private long _maintenanceOriDate = 0;

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
		_llReplace = view.findViewById(R.id.ll_replace);
		_llInspect = view.findViewById(R.id.ll_inspect);
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
					_maintenanceOriDate = cursor.getLong(cursor
							.getColumnIndexOrThrow(MaintenanceEntry.COLUMN_DATE));
					_calendarOdometer.setTime(new Date(_maintenanceOriDate));
				}
				cursor.close();
			}
		}

		String extraVehicleId = requireActivity().getIntent()
				.getStringExtra(MaintenanceEditorActivity.EXTRA_VEHICLE_ID);
		if (extraVehicleId != null) {
			// user setting specific vehicle's maintenance. disable spinner
			_editVehicleId = Integer.parseInt(extraVehicleId);
		}
		_vehicleIds = SetupViews.setupVehicleRegNoSpinner(requireContext(), _spinnerVehicle);
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

			getMaintenanceItems();
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
		_editOdometer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				String strOdometer = _editOdometer.getText().toString().trim();
				if (!_isInit && !hasFocus && !TextUtils.isEmpty(strOdometer)) {
					int distance = Integer.parseInt(strOdometer);
					if (distance < OdometerEntry.DISTANCE_MIN) {
						_editOdometer.setText("");
						UserDialog.showDialog(requireActivity(), "",
								getString(R.string.odometer_input_too_small), null);
					}
					if (distance > OdometerEntry.DISTANCE_MAX) {
						_editOdometer.setText("");
						UserDialog.showDialog(requireActivity(), "",
								getString(R.string.odometer_input_too_large), null);
					}
				}
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
				if (!_isInit) {
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
					.getFirebaseVehicleIdFromList(FirebaseObj._vehicleTemplates,
							userVehicle.get_brand(),
							userVehicle.get_model(),
							userVehicle.get_variant());

			FirebaseObj.runCallbackMaintenanceDetails(firebaseVehicleId, new FirebaseObj() {
				@Override
				public void callback() {
					List<UpcomingMaintenanceItem> preCheckInspectList = new ArrayList<>();
					List<UpcomingMaintenanceItem> preCheckReplaceList = new ArrayList<>();
					Map<String, Double> initReplace = new HashMap<>();
					Map<String, Double> initInspect = new HashMap<>();

					List<List<UpcomingMaintenanceItem>> firebaseItems = FirebaseObj
							.getUpcomingItemsByInspectReplace(requireContext(),
									firebaseVehicleId,
									userVehicle);
					List<UpcomingMaintenanceItem> itemsInspect = firebaseItems.get(0);
					List<UpcomingMaintenanceItem> itemsReplace = firebaseItems.get(1);

					itemsInspect.addAll(MaintenanceItem
							.getUpcomingCustomItemNotInFirebase(
									requireActivity(), itemsInspect, userVehicle,
									CustomMaintenanceItemEntry.INSPECT_VALUE));

					itemsReplace.addAll(MaintenanceItem
							.getUpcomingCustomItemNotInFirebase(
									requireActivity(), itemsReplace, userVehicle,
									CustomMaintenanceItemEntry.REPLACE_VALUE));

					if (_currentUri != null) {
						// if editing, get db items
						initReplace = getDbItems(MaintenanceItemEntry.REPLACE_VALUE);
						initInspect = getDbItems(MaintenanceItemEntry.INSPECT_VALUE);

						// remove item from list and put in another list
						// re-add at beginning of array so that these items appear
						// at top of list
						for (int i = itemsReplace.size() - 1; i >= 0; i--) {
							if (initReplace.containsKey(itemsReplace.get(i).getItem())) {
								preCheckReplaceList.add(itemsReplace.get(i));
								itemsReplace.remove(i);
							}
						}
						for (int i = itemsInspect.size() - 1; i >= 0; i--) {
							if (initInspect.containsKey(itemsInspect.get(i).getItem())) {
								preCheckInspectList.add(itemsInspect.get(i));
								itemsInspect.remove(i);
							}
						}
					}

					Collections.sort(itemsInspect, new UpcomingMaintenanceItem.CustomComparator());
					Collections.sort(itemsReplace, new UpcomingMaintenanceItem.CustomComparator());
					Collections.sort(preCheckInspectList, new UpcomingMaintenanceItem.CustomComparator());
					Collections.sort(preCheckReplaceList, new UpcomingMaintenanceItem.CustomComparator());

					itemsInspect.addAll(0, preCheckInspectList);
					itemsReplace.addAll(0, preCheckReplaceList);

					_distanceIntervals = new ArrayList<>();
					_durationIntervals = new ArrayList<>();

					for (MaintenanceItem item : itemsInspect) {
						if (!_distanceIntervals.contains(item.getDistance_interval())) {
							_distanceIntervals.add(item.getDistance_interval());
						}
						if (!_durationIntervals.contains(item.getDuration_interval())) {
							_durationIntervals.add(item.getDuration_interval());
						}
					}
					for (MaintenanceItem item : itemsReplace) {
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

					displayMaintenanceItems(itemsReplace,
							itemsInspect,
							initReplace,
							initInspect);

					_viewContent.setVisibility(View.VISIBLE);
					_progressBar.setVisibility(View.GONE);

					// as discussed, in 20200521, temporarily disable this first.
					// Too confusing for users
//					if (_isInit) {
//						_isInit = false;
//						// display template dialog only if adding new record
//						if (_currentUri == null) {
//							FragmentManager manager = requireActivity().getSupportFragmentManager();
//							MaintenanceTemplateDialogFragment dialog =
//									MaintenanceTemplateDialogFragment.newInstance(_distanceIntervals,
//											MaintenanceTemplateDialogFragment.BY_DISTANCE);
//							dialog.setTargetFragment(MaintenanceEditorFragment.this,
//									DIALOG_TEMPLATE_RESULT);
//							dialog.show(manager, DIALOG_TEMPLATE);
//						}
//					}
				}
			});
		}
		cursor.close();
	}

	private Map<String, Double> getDbItems(int inspectReplace) {
		Map<String, Double> returnValue = new HashMap<>();

		long maintenanceId = ContentUris.parseId(_currentUri);
		Cursor cursor = requireContext().getContentResolver().query(
				ContentUris.withAppendedId(MaintenanceDetailsEntry.CONTENT_URI_MAINTENANCE,
						maintenanceId),
				null,
				null,
				new String[]{String.valueOf(maintenanceId), String.valueOf(inspectReplace)},
				null);

		if (cursor == null) {
			return returnValue;
		}

		while (cursor.moveToNext()) {
			returnValue.put(cursor.getString(cursor
							.getColumnIndexOrThrow(MaintenanceItemEntry.COLUMN_ITEM)),
					cursor.getDouble(cursor
							.getColumnIndexOrThrow(MaintenanceDetailsEntry.COLUMN_PRICE)));
		}

		cursor.close();
		return returnValue;
	}

	private void displayMaintenanceItems(List<UpcomingMaintenanceItem> itemsReplace,
										 List<UpcomingMaintenanceItem> itemsInspect,
										 Map<String, Double> initReplace,
										 Map<String, Double> initInspect) {
		// remove all children and re-add them
		_llReplaceItems.removeAllViews();
		_llInspectItems.removeAllViews();

		if (itemsReplace.isEmpty()) {
			_llReplace.setVisibility(View.GONE);
		} else {
			_llReplace.setVisibility(View.VISIBLE);
		}
		if (itemsInspect.isEmpty()) {
			_llInspect.setVisibility(View.GONE);
		} else {
			_llInspect.setVisibility(View.VISIBLE);
		}
		addMaintenanceItemLayouts(_llReplaceItems, itemsReplace, initReplace);
		addMaintenanceItemLayouts(_llInspectItems, itemsInspect, initInspect);
	}

	private void addMaintenanceItemLayouts(
			LinearLayout linearLayout, List<UpcomingMaintenanceItem> maintenanceItems,
			Map<String, Double> initPrice) {
		for (UpcomingMaintenanceItem maintenanceItem : maintenanceItems) {
			View view = LayoutInflater.from(requireContext()).inflate(
					R.layout.template_maintenance_editor_item, linearLayout, false);
			TextView txtItem = view.findViewById(R.id.txt_item);
			EditText editPrice = view.findViewById(R.id.edit_price);
			CheckBox cbItem = view.findViewById(R.id.cb_item);
			TextView txtDistanceLeft = view.findViewById(R.id.txt_distance_left);

			txtItem.setText(maintenanceItem.getItem());
			txtItem.setTag(R.string.by_distance_interval, maintenanceItem.getDistance_interval());
			txtItem.setTag(R.string.by_duration_interval, maintenanceItem.getDuration_interval());

			// display distance left only if adding record
			if (_currentUri == null) {
				String strDistanceLeft = String.format(Locale.getDefault(), "%,d",
						maintenanceItem.get_distanceLeft()) + " "
						+ getString(R.string.kilometer) + " left";

				txtDistanceLeft.setText(strDistanceLeft);

				if (maintenanceItem.getUrgency() != UpcomingMaintenanceItem.URGENCY_NOT_URGENT) {
					txtDistanceLeft.setTextColor(UpcomingMaintenanceCursorAdapter
							.getUrgencyColour(getContext(), maintenanceItem.getUrgency()));
				}

			} else {
				txtDistanceLeft.setVisibility(View.GONE);
			}

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
		final long maintenanceDate = DateUtilities
				.getCalendarAtMidnight(_calendarOdometer).getTime().getTime();
		final String strOdometerDistance = _editOdometer.getText().toString();
		final int vehicleId = getVehicleId();
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

		// temporarily disable save odometer first.
		// need to discuss with others

		//		saveOdometer(vehicleId, maintenanceDate, Integer.parseInt(strOdometerDistance));

		ContentValues values = new ContentValues();
		values.put(MaintenanceEntry.COLUMN_VEHICLE, vehicleId);
		values.put(MaintenanceEntry.COLUMN_DATE, maintenanceDate);
		values.put(MaintenanceEntry.COLUMN_ODOMETER, Integer.parseInt(strOdometerDistance));
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

			final Uri editOdoUri = _currentUri == null ?
					getOdometerUri(requireContext(), vehicleId, maintenanceDate) :
					getOdometerUri(requireContext(), vehicleId, _maintenanceOriDate);

			String updateOdoMessage = editOdoUri == null ?
					"Do you want to create an odometer record too?" :
					"Do you want to update odometer record too?";

			UserDialog.showDialog(requireActivity(),
					"",
					updateOdoMessage,
					getString(R.string.yes),
					getString(R.string.no),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// update odometer
							// if original date is 0 means user not editing existing
							if (editOdoUri != null) {
								Log.v("CHECK_ME", editOdoUri.toString());
							}
							OdometerEditorActivity.saveOdometer(requireContext(),
									editOdoUri,
									vehicleId,
									maintenanceDate,
									Integer.parseInt(strOdometerDistance));

							Toast.makeText(requireContext(),
									getString(R.string.saved_successfully),
									Toast.LENGTH_SHORT).show();
							requireActivity().finish();
						}
					},
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Toast.makeText(requireContext(),
									getString(R.string.saved_successfully),
									Toast.LENGTH_SHORT).show();
							requireActivity().finish();
						}
					},
					new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							Toast.makeText(requireContext(),
									getString(R.string.saved_successfully),
									Toast.LENGTH_SHORT).show();
							requireActivity().finish();
						}
					});
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

	private static Uri getOdometerUri(Context context, long vehicleId, long date) {
		Cursor cursor = context.getContentResolver().query(
				OdometerEntry.CONTENT_URI,
				OdometerEntry.FULL_PROJECTION,
				OdometerEntry.COLUMN_VEHICLE + "=? AND "
						+ OdometerEntry.COLUMN_DATE + "=?",
				new String[]{String.valueOf(vehicleId),
						String.valueOf(date)},
				null);

		Uri odoUri = null;

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				odoUri = ContentUris.withAppendedId(OdometerEntry.CONTENT_URI,
						cursor.getInt(cursor.getColumnIndexOrThrow(OdometerEntry._ID)));
			}
			cursor.close();
		}
		return odoUri;
	}

	public static Uri getOdometerUri(Context context, Uri maintenanceUri) {
		Uri odoUri = null;
		Cursor cursor = context.getContentResolver().query(
				maintenanceUri,
				MaintenanceEntry.FULL_PROJECTION,
				null,
				null,
				null);

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				int vehicleId = cursor.getInt(cursor
						.getColumnIndexOrThrow(MaintenanceEntry.COLUMN_VEHICLE));
				long date = cursor.getLong(cursor
						.getColumnIndexOrThrow(MaintenanceEntry.COLUMN_DATE));

				odoUri = getOdometerUri(context, vehicleId, date);
			}
			cursor.close();
		}
		return odoUri;
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
		final Uri odoUri = getOdometerUri(requireContext(), _currentUri);
		int rowsDeleted = requireContext().getContentResolver().delete(_currentUri, null, null);

		if (rowsDeleted == 0) {
			Toast.makeText(requireContext(), getString(R.string.error_has_occurred),
					Toast.LENGTH_SHORT).show();
		} else {
			if (odoUri == null) {
				Toast.makeText(requireContext(), getString(R.string.maintenance_deleted),
						Toast.LENGTH_SHORT).show();
				requireActivity().finish();
			} else {
				UserDialog.showDialog(
						requireContext(),
						"",
						getString(R.string.maintenance_deleted_confirm_delete_odo_too),
						getString(R.string.yes),
						getString(R.string.no),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								requireContext().getContentResolver().delete(
										odoUri,
										null,
										null);
								Toast.makeText(requireContext(),
										getString(R.string.maintenance_deleted),
										Toast.LENGTH_SHORT).show();
								requireActivity().finish();
							}
						},
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Toast.makeText(requireContext(),
										getString(R.string.maintenance_deleted),
										Toast.LENGTH_SHORT).show();
								requireActivity().finish();
							}
						},
						new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								Toast.makeText(requireContext(),
										getString(R.string.maintenance_deleted),
										Toast.LENGTH_SHORT).show();
								requireActivity().finish();
							}
						});
			}
		}
	}
}
