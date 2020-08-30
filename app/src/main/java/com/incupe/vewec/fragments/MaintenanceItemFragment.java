package com.incupe.vewec.fragments;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.incupe.vewec.MaintenanceItemEditorActivity;
import com.incupe.vewec.R;
import com.incupe.vewec.cursoradapter.MaintenanceItemCursorAdapter;
import com.incupe.vewec.data.MaintenanceItemContract.MaintenanceItemEntry;
import com.incupe.vewec.utilities.UserDialog;

public class MaintenanceItemFragment extends Fragment
		implements LoaderManager.LoaderCallbacks<Cursor> {
	private MaintenanceItemCursorAdapter _maintenanceItemCursorAdapter;
	private static final int LOADER_ID = 0;

	private String _longClickItem = "";
	private long _longClickId = 0;

	public MaintenanceItemFragment() {
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
							 @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		_maintenanceItemCursorAdapter = new MaintenanceItemCursorAdapter(getContext(), null);

		View rootView = inflater.inflate(
				R.layout.listview_with_empty_view,
				container,
				false);
		rootView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
		ListView listView = rootView.findViewById(R.id.item_list);
		View emptyView = rootView.findViewById(R.id.empty_view);

		((TextView) rootView.findViewById(R.id.empty_title_text))
				.setText(R.string.empty_here);
		((ImageView) rootView.findViewById(R.id.empty_image))
				.setImageResource(R.drawable.ic_library_add_grey_24);
		listView.setEmptyView(emptyView);
		listView.setAdapter(_maintenanceItemCursorAdapter);

		final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
		final View sheetView = requireActivity().getLayoutInflater()
				.inflate(R.layout.bottom_sheet_edit_delete, container, false);
		bottomSheetDialog.setContentView(sheetView);

		// TODO: fix deprecated call
		getLoaderManager().initLoader(LOADER_ID, null, this);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(requireContext(), MaintenanceItemEditorActivity.class);
				Uri currentUri = ContentUris.withAppendedId(MaintenanceItemEntry.CONTENT_URI, id);
				intent.setData(currentUri);
				startActivity(intent);
			}
		});

		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				_longClickItem = ((TextView) view.findViewById(R.id.txt_item_name)).getText().toString();
				_longClickId = id;
				bottomSheetDialog.show();
				return true;
			}
		});

		sheetView.findViewById(R.id.bottom_sheet_edit)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO
					}
				});

		sheetView.findViewById(R.id.bottom_sheet_delete)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						UserDialog.showDeleteConfirmationDialog(
								getContext(),
								getString(R.string.delete) + " " + _longClickItem + "?",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										requireContext().getContentResolver().delete(
												ContentUris.withAppendedId(
														MaintenanceItemEntry.CONTENT_URI, _longClickId
												),
												null,
												null
										);
										bottomSheetDialog.hide();
									}
								});
					}
				});

		return rootView;
	}

	@NonNull
	@Override
	public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
		// select maintenance item for the vehicle user is currently viewing only


		// sort by inspect/replace first. then sort by item name
		return new CursorLoader(
				requireContext(),
				MaintenanceItemEntry.CONTENT_URI,
				MaintenanceItemEntry.FULL_PROJECTION,
				null,
				null,
				MaintenanceItemEntry.COLUMN_INSPECT_REPLACE + " DESC, " +
						MaintenanceItemEntry.COLUMN_ITEM + " ASC"
		);
//		// CANNOT use this method. cursor loader is "notified" according to the exact
		// same query and onCreateLoader is not called. so will fetch wrong data with wrong
		// vehicle Id
//		int vehicleId = 0;
//		SharedPreferences sharedPreferences =
//				PreferenceManager.getDefaultSharedPreferences(requireContext());
//		vehicleId = sharedPreferences.getInt(requireContext()
//				.getString(R.string.pref_session_vehicle), 0);
//		return new CursorLoader(
//				requireContext(),
//				MaintenanceItemEntry.CONTENT_URI,
//				MaintenanceItemEntry.FULL_PROJECTION,
//				MaintenanceItemEntry.COLUMN_VEHICLE + "=?",
//				new String[]{String.valueOf(vehicleId)},
//				MaintenanceItemEntry.COLUMN_INSPECT_REPLACE + " DESC, " +
//						MaintenanceItemEntry.COLUMN_ITEM + " ASC"
//		);
	}

	@Override
	public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
		_maintenanceItemCursorAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(@NonNull Loader<Cursor> loader) {
		_maintenanceItemCursorAdapter.swapCursor(null);
	}
}
