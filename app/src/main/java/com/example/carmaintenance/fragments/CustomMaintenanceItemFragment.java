package com.example.carmaintenance.fragments;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.carmaintenance.R;
import com.example.carmaintenance.cursoradapter.CustomMaintenanceItemCursorAdapter;
import com.example.carmaintenance.data.CustomMaintenanceItemContract.CustomMaintenanceItemEntry;
import com.example.carmaintenance.utilities.UserDialog;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class CustomMaintenanceItemFragment extends Fragment
		implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String DIALOG_CUSTOM_ITEM = "DIALOG_CUSTOM_ITEM";
	private static final int DIALOG_RESULT = 0;
	private static final int LOADER_ID = 0;

	private CustomMaintenanceItemCursorAdapter _customMaintenanceItemCursorAdapter;
	private long _editItemId = 0;

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

		// initialise view variables
		ListView listView = rootView.findViewById(R.id.item_list);

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
						String name = cursor.getString(cursor.getColumnIndexOrThrow(
								CustomMaintenanceItemEntry.COLUMN_ITEM));
						int inspectReplace = cursor.getInt(cursor.getColumnIndexOrThrow(
								CustomMaintenanceItemEntry.COLUMN_INSPECT_REPLACE));
						int distanceInterval = cursor.getInt(cursor.getColumnIndexOrThrow(
								CustomMaintenanceItemEntry.COLUMN_DISTANCE_INTERVAL));
						int durationInterval = cursor.getInt(cursor.getColumnIndexOrThrow(
								CustomMaintenanceItemEntry.COLUMN_DURATION_INTERVAL));

						FragmentManager manager = requireActivity().getSupportFragmentManager();
						CustomMaintenanceItemEditorFragment dialog =
								CustomMaintenanceItemEditorFragment.newInstance(name,
										inspectReplace, distanceInterval, durationInterval);
						dialog.setTargetFragment(CustomMaintenanceItemFragment.this,
								DIALOG_RESULT);
						dialog.show(manager, DIALOG_CUSTOM_ITEM);
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
		sheetView.findViewById(R.id.bottom_sheet_edit).setVisibility(View.GONE);
		sheetView.findViewById(R.id.bottom_sheet_delete).setOnClickListener(
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

		rootView.findViewById(R.id.progress_bar).setVisibility(View.GONE);

		// TODO: fix deprecated call
		// Kick off the loader
		getLoaderManager().initLoader(LOADER_ID, null, this);

		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		requireActivity().getMenuInflater().inflate(R.menu.menu_add, menu);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		if (requestCode == DIALOG_RESULT && data != null) {
			String itemName = (String) data.getSerializableExtra(
					CustomMaintenanceItemEditorFragment.ARG_NAME);
			int inspectReplace = (int) data.getSerializableExtra(
					CustomMaintenanceItemEditorFragment.ARG_INSPECT_REPLACE);
			int distanceInterval = (int) data.getSerializableExtra(
					CustomMaintenanceItemEditorFragment.ARG_DISTANCE);
			int durationInterval = (int) data.getSerializableExtra(
					CustomMaintenanceItemEditorFragment.ARG_DURATION);

			if (checkItemExists(itemName, inspectReplace, _editItemId)) {
				UserDialog.showDialog(requireContext(),
						"",
						getString(R.string.item_already_exists),
						null);
				return;
			}

			ContentValues values = new ContentValues();
			values.put(CustomMaintenanceItemEntry.COLUMN_ITEM, itemName);
			values.put(CustomMaintenanceItemEntry.COLUMN_INSPECT_REPLACE, inspectReplace);
			values.put(CustomMaintenanceItemEntry.COLUMN_DISTANCE_INTERVAL, distanceInterval);
			values.put(CustomMaintenanceItemEntry.COLUMN_DURATION_INTERVAL, durationInterval);

			if (_editItemId == 0) {
				Uri newUri = requireContext().getContentResolver()
						.insert(CustomMaintenanceItemEntry.CONTENT_URI, values);

				if (newUri == null) {
					UserDialog.showDialog(requireContext(),
							"",
							getString(R.string.error_has_occurred),
							null);
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
				}
			}
		}
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
			returnValue = cursor.getCount() > 0;
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
				FragmentManager manager = requireActivity().getSupportFragmentManager();
				CustomMaintenanceItemEditorFragment dialog =
						CustomMaintenanceItemEditorFragment.newInstance();
				dialog.setTargetFragment(CustomMaintenanceItemFragment.this,
						DIALOG_RESULT);
				dialog.show(manager, DIALOG_CUSTOM_ITEM);
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
