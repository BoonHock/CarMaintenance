package com.example.carmaintenance.fragments;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.carmaintenance.R;
import com.example.carmaintenance.VehicleEditorActivity;
import com.example.carmaintenance.cursoradapter.UserVehicleCursorAdapter;
import com.example.carmaintenance.data.UserVehicleContract.UserVehicleEntry;
import com.example.carmaintenance.utilities.UserDialog;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class UserVehicleFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private UserVehicleCursorAdapter _userVehicleCursorAdapter;
	/**
	 * Identifier for the data loader
	 */
	private static final int USER_VEHICLE_LOADER = 0;
	private String _longClickRegNo = "";
	private long _longClickId = 0;

	public UserVehicleFragment() {
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
							 @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		_userVehicleCursorAdapter = new UserVehicleCursorAdapter(getContext(), null);

		View rootView = inflater.inflate(R.layout.listview_with_empty_view, container, false);
		rootView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
		ListView listView = rootView.findViewById(R.id.item_list);

		View emptyView = rootView.findViewById(R.id.empty_view);

		listView.setEmptyView(emptyView);
		listView.setAdapter(_userVehicleCursorAdapter);

		final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
		final View sheetView = requireActivity().getLayoutInflater()
				.inflate(R.layout.bottom_sheet_edit_delete, container, false);
		bottomSheetDialog.setContentView(sheetView);

		// TODO: fix deprecated call
		// Kick off the loader
		getLoaderManager().initLoader(USER_VEHICLE_LOADER, null, this);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(getContext(), VehicleEditorActivity.class);
				Uri currentUri = ContentUris.withAppendedId(UserVehicleEntry.CONTENT_URI, id);
				intent.setData(currentUri);
				startActivity(intent);
			}
		});

		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				_longClickRegNo = ((TextView) view.findViewById(R.id.txt_reg_no)).getText().toString();
				_longClickId = id;
				bottomSheetDialog.show();
				return true;
			}
		});

		sheetView.findViewById(R.id.bottom_sheet_edit)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getContext(), VehicleEditorActivity.class);
						Uri currentUri = ContentUris.withAppendedId(UserVehicleEntry.CONTENT_URI, _longClickId);
						intent.setData(currentUri);
						startActivity(intent);
						bottomSheetDialog.hide();
					}
				});
		sheetView.findViewById(R.id.bottom_sheet_delete)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						UserDialog.showDeleteConfirmationDialog(getContext(),
								getString(R.string.delete) + " " + _longClickRegNo + "?",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										requireContext().getContentResolver().delete(
												ContentUris.withAppendedId(
														UserVehicleEntry.CONTENT_URI, _longClickId),
												null,
												null);
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
		// This loader will execute the ContentProvider's query method on a background thread
		return new CursorLoader(requireContext(),   // Parent activity context
				UserVehicleEntry.CONTENT_URI,   // Provider content URI to query
				UserVehicleEntry.FULL_PROJECTION,    // Columns to include in the resulting Cursor
				null,                   // No selection clause
				null,                   // No selection arguments
				null);                  // Default sort order
	}

	@Override
	public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
		_userVehicleCursorAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(@NonNull Loader<Cursor> loader) {
		_userVehicleCursorAdapter.swapCursor(null);
	}
}
