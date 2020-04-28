package com.example.carmaintenance.fragments;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.carmaintenance.R;
import com.example.carmaintenance.cursoradapter.CustomMaintenanceItemCursorAdapter;
import com.example.carmaintenance.data.CustomMaintenanceItemContract.CustomMaintenanceItemEntry;
import com.example.carmaintenance.data.OdometerContract.OdometerEntry;
import com.example.carmaintenance.utilities.Misc;
import com.example.carmaintenance.utilities.UserDialog;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class CustomMaintenanceItemFragment extends Fragment
		implements LoaderManager.LoaderCallbacks<Cursor> {
	private CustomMaintenanceItemCursorAdapter _customMaintenanceItemCursorAdapter;
	private static final int LOADER_ID = 0;
	private long _editItemId = 0;

	private RelativeLayout _viewDialog;
	private EditText _editName;
	private EditText _editDistance;
	private EditText _editDuration;
	private Spinner _spinnerInspectReplace;

	public CustomMaintenanceItemFragment() {
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true); // make fragment respond to options menu
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.listview_with_empty_view,
				container, false);

		_viewDialog = (RelativeLayout) View.inflate(getContext(), R.layout.dialog_custom_maintenance_item, null);
		((RelativeLayout) rootView).addView(_viewDialog);
		_viewDialog.setVisibility(View.GONE);

		// initialise view variables
		ListView listView = rootView.findViewById(R.id.item_list);
		_editName = rootView.findViewById(R.id.edit_item_name);
		_spinnerInspectReplace = rootView.findViewById(R.id.spinner_inspect_replace);
		_editDistance = rootView.findViewById(R.id.edit_distance_interval);
		_editDuration = rootView.findViewById(R.id.edit_duration_interval);
		final TextView txtClose = rootView.findViewById(R.id.txt_close);

		final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
		final View sheetView = requireActivity().getLayoutInflater()
				.inflate(R.layout.bottom_sheet_edit_delete, container, false);
		bottomSheetDialog.setContentView(sheetView);

		setupEmptyView(rootView, listView);
		setupBottomSheetDialog(listView);

		_customMaintenanceItemCursorAdapter =
				new CustomMaintenanceItemCursorAdapter(getContext(), null);
		listView.setAdapter(_customMaintenanceItemCursorAdapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor cursor = requireActivity().getContentResolver().query(
						CustomMaintenanceItemEntry.CONTENT_URI,
						CustomMaintenanceItemEntry.FULL_PROJECTION,
						CustomMaintenanceItemEntry._ID + "=?",
						new String[]{String.valueOf(id)},
						null);

				if (cursor != null) {
					if (cursor.moveToFirst()) {
						_editItemId = id;
						_editName.setText(cursor.getString(cursor.getColumnIndexOrThrow(
								CustomMaintenanceItemEntry.COLUMN_ITEM)));
						int inspectReplace = cursor.getInt(cursor.getColumnIndexOrThrow(
								CustomMaintenanceItemEntry.COLUMN_INSPECT_REPLACE));
						int distanceInterval = cursor.getInt(cursor.getColumnIndexOrThrow(
								CustomMaintenanceItemEntry.COLUMN_DISTANCE_INTERVAL));
						int durationInterval = cursor.getInt(cursor.getColumnIndexOrThrow(
								CustomMaintenanceItemEntry.COLUMN_DURATION_INTERVAL));

						if (inspectReplace == CustomMaintenanceItemEntry.INSPECT_VALUE) {
							_spinnerInspectReplace.setSelection(0);
						} else if (inspectReplace == CustomMaintenanceItemEntry.REPLACE_VALUE) {
							_spinnerInspectReplace.setSelection(1);
						}
						if (distanceInterval != 0) {
							_editDistance.setText(String.valueOf(distanceInterval));
						}
						if (durationInterval != 0) {
							_editDuration.setText(String.valueOf(durationInterval));
						}
						_viewDialog.setVisibility(View.VISIBLE);
					}
					cursor.close();
				}
			}
		});
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				_editItemId = id;
				bottomSheetDialog.show();
				return true;
			}
		});
		// no need edit la. already can edit when click directly
		sheetView.findViewById(R.id.bottom_sheet_ll_edit).setVisibility(View.GONE);
		sheetView.findViewById(R.id.bottom_sheet_ll_delete).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						UserDialog.showDeleteConfirmationDialog(requireContext(),
								getString(R.string.are_you_sure),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										int rowsDeleted = requireContext().getContentResolver()
												.delete(ContentUris.withAppendedId(
														CustomMaintenanceItemEntry.CONTENT_URI,
														_editItemId),
														null,
														null);
										if (rowsDeleted == 0) {
											UserDialog.showDialog(requireContext(),
													"",
													getString(R.string.error_has_occurred),
													null);
										} else {
											Toast.makeText(requireContext(),
													getString(R.string.item_deleted),
													Toast.LENGTH_SHORT).show();
										}
										bottomSheetDialog.hide();
									}
								});
					}
				});

		View.OnClickListener closeDialogListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// hide dialog and clear all fields
				Misc.hideKeyboard(requireActivity());
				_viewDialog.setVisibility(View.GONE);
				_editName.setText("");
				_spinnerInspectReplace.setSelection(0);
				_editDistance.setText("");
				_editDuration.setText("");
			}
		};
		txtClose.setOnClickListener(closeDialogListener);
		rootView.findViewById(R.id.ll_mask).setOnClickListener(closeDialogListener);

		rootView.findViewById(R.id.txt_save).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (saveItem()) {
					// if save success, close dialog
					txtClose.performClick();
				}
			}
		});

		_editName.setFilters(new InputFilter[]{
				new InputFilter.LengthFilter(CustomMaintenanceItemEntry.ITEM_NAME_MAX_LENGTH)
		});
		_editDistance.setFilters(new InputFilter[]{
				new InputFilter.LengthFilter(String.valueOf(OdometerEntry.DISTANCE_MAX).length())
		});
		_editDuration.setFilters(new InputFilter[]{
				// maximum 99 months. more than that too long la. car die d lo
				new InputFilter.LengthFilter(2)
		});

		ArrayAdapter spinnerAdapter = ArrayAdapter.createFromResource(
				requireContext(),
				R.array.array_inspect_replace_options,
				android.R.layout.simple_spinner_item);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		_spinnerInspectReplace.setAdapter(spinnerAdapter);

		rootView.findViewById(R.id.progress_bar).setVisibility(View.GONE);

		// TODO: fix deprecated call
		// Kick off the loader
		getLoaderManager().initLoader(LOADER_ID, null, this);

		return rootView;
	}

	private void setupEmptyView(View rootView, ListView listView) {
		View emptyView = rootView.findViewById(R.id.empty_view);
		((TextView) rootView.findViewById(R.id.empty_title_text))
				.setText(getString(R.string.empty_here));
		((ImageView) rootView.findViewById(R.id.empty_image))
				.setImageResource(R.drawable.ic_build_24dp);

		listView.setEmptyView(emptyView);
	}

	private void setupBottomSheetDialog(ListView listView) {
		final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
		final View sheetView = getLayoutInflater()
				.inflate(R.layout.bottom_sheet_edit_delete, listView, false);
		bottomSheetDialog.setContentView(sheetView);
	}

	private boolean saveItem() {
		String itemName = _editName.getText().toString().trim();
		int inspectReplace = CustomMaintenanceItemEntry.INSPECT_VALUE; // default
		String strDistanceInterval = _editDistance.getText().toString().trim();
		String strDurationInterval = _editDuration.getText().toString().trim();
		int distanceInterval = 0;
		int durationInterval = 0;

		if (!TextUtils.isEmpty(strDistanceInterval)) {
			distanceInterval = Integer.parseInt(strDistanceInterval);
		}
		if (!TextUtils.isEmpty(strDurationInterval)) {
			durationInterval = Integer.parseInt(strDurationInterval);
		}

		if (TextUtils.isEmpty(itemName)) {
			UserDialog.showDialog(requireContext(),
					"",
					getString(R.string.item_name_required),
					null);
			return false;
		}
		if (distanceInterval == 0 && durationInterval == 0) {
			UserDialog.showDialog(requireContext(),
					"",
					"At least one interval is required.",
					null);
			return false;
		}

		String selection = (String) _spinnerInspectReplace.getSelectedItem();
		if (!TextUtils.isEmpty(selection)
				&& selection.equals(getString(R.string.replace))) {
			inspectReplace = CustomMaintenanceItemEntry.REPLACE_VALUE;
		}

		if (!checkItemExists(itemName, inspectReplace, _editItemId)) {
			UserDialog.showDialog(requireContext(),
					"",
					getString(R.string.item_already_exists),
					null);
			return false;
		}

		ContentValues values = new ContentValues();
		values.put(CustomMaintenanceItemEntry.COLUMN_ITEM, itemName);
		values.put(CustomMaintenanceItemEntry.COLUMN_INSPECT_REPLACE, inspectReplace);
		values.put(CustomMaintenanceItemEntry.COLUMN_DISTANCE_INTERVAL, distanceInterval);
		values.put(CustomMaintenanceItemEntry.COLUMN_DURATION_INTERVAL, durationInterval);

		if (_editItemId == 0) {
			Log.v("CHECK_ME", "ADDING ITEM");
			Uri newUri = requireContext().getContentResolver()
					.insert(CustomMaintenanceItemEntry.CONTENT_URI, values);

			if (newUri == null) {
				UserDialog.showDialog(requireContext(),
						"",
						getString(R.string.error_has_occurred),
						null);
				return false;
			}
		} else {
			int rowsAffected = requireContext().getContentResolver().update(
					ContentUris.withAppendedId(CustomMaintenanceItemEntry.CONTENT_URI, _editItemId),
					values,
					null,
					null);
			if (rowsAffected == 0) {
				UserDialog.showDialog(requireContext(),
						"",
						getString(R.string.error_has_occurred),
						null);
				return false;
			}
		}
		return true;
	}

	private boolean checkItemExists(String itemName, int inspectReplace, long excludeId) {
		boolean returnValue = false;
		Cursor cursor = requireContext().getContentResolver().query(
				CustomMaintenanceItemEntry.CONTENT_URI,
				CustomMaintenanceItemEntry.FULL_PROJECTION,
				CustomMaintenanceItemEntry.COLUMN_ITEM + "=? AND "
						+ CustomMaintenanceItemEntry.COLUMN_INSPECT_REPLACE + "=? AND "
						+ CustomMaintenanceItemEntry._ID + "!=?",
				new String[]{itemName,
						String.valueOf(inspectReplace),
						String.valueOf(excludeId)},
				null);

		if (cursor != null) {
			returnValue = cursor.getCount() == 0;
			cursor.close();
		}
		return returnValue;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_add:
				// set edit item id to 0 to indicate adding record
				_editItemId = 0;
				// show dialog to add
				_viewDialog.setVisibility(View.VISIBLE);
				_editName.requestFocus();
				Misc.showKeyboard(requireActivity(), _editName);
				break;
			case android.R.id.home:
				requireActivity().finish();
				break;
		}
		return true;
	}

	@NonNull
	@Override
	public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
		return new CursorLoader(requireContext(),
				CustomMaintenanceItemEntry.CONTENT_URI,
				null,
				null,
				null,
				CustomMaintenanceItemEntry.COLUMN_ITEM + " ASC");
	}

	@Override
	public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
		_customMaintenanceItemCursorAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(@NonNull Loader<Cursor> loader) {
		_customMaintenanceItemCursorAdapter.swapCursor(null);
	}
}
