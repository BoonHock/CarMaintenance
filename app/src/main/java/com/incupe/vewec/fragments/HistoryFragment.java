package com.incupe.vewec.fragments;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.incupe.vewec.MaintenanceEditorActivity;
import com.incupe.vewec.R;
import com.incupe.vewec.cursoradapter.HistoryCursorAdapter;
import com.incupe.vewec.data.MaintenanceContract.MaintenanceEntry;
import com.incupe.vewec.data.MaintenanceDetailsContract;
import com.incupe.vewec.utilities.UserDialog;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class HistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final int LOADER_ID = 0;

	private HistoryCursorAdapter _historyCursorAdapter;
	private long _longClickId = 0;

	public HistoryFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		_historyCursorAdapter = new HistoryCursorAdapter(getContext(), null);

		View rootView = inflater.inflate(R.layout.listview_with_empty_view, container, false);
		View emptyView = rootView.findViewById(R.id.empty_view);

		ProgressBar _progressBar = rootView.findViewById(R.id.progress_bar);
		RelativeLayout _content = rootView.findViewById(R.id.rl_content);
		ListView listView = rootView.findViewById(R.id.item_list);

		_progressBar.setVisibility(View.VISIBLE);
		_content.setVisibility(View.INVISIBLE);

		listView.setEmptyView(emptyView);
		listView.setAdapter(_historyCursorAdapter);

		final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
		final View sheetView = requireActivity().getLayoutInflater()
				.inflate(R.layout.bottom_sheet_edit_delete, container, false);
		bottomSheetDialog.setContentView(sheetView);

		getLoaderManager().initLoader(LOADER_ID, null, this);

		((TextView) rootView.findViewById(R.id.empty_title_text))
				.setText(R.string.empty_here);
		((ImageView) rootView.findViewById(R.id.empty_image))
				.setImageResource(R.drawable.ic_history_24dp);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(getContext(), MaintenanceEditorActivity.class);
				Uri currentUri = ContentUris.withAppendedId(MaintenanceEntry.CONTENT_URI, id);
				intent.setData(currentUri);
				startActivity(intent);
			}
		});

		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				_longClickId = id;
				bottomSheetDialog.show();
				return true;
			}
		});

		sheetView.findViewById(R.id.bottom_sheet_edit)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getContext(), MaintenanceEditorActivity.class);
						Uri currentUri = ContentUris.withAppendedId(MaintenanceEntry.CONTENT_URI, _longClickId);
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
								getString(R.string.are_you_sure),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										requireContext().getContentResolver().delete(
												ContentUris.withAppendedId(
														MaintenanceEntry.CONTENT_URI, _longClickId),
												null,
												null);
										bottomSheetDialog.hide();
									}
								});
					}
				});

		_progressBar.setVisibility(View.GONE);
		_content.setVisibility(View.VISIBLE);

		Cursor tmpCursor = requireContext().getContentResolver().query(
				MaintenanceDetailsContract.MaintenanceDetailsEntry.CONTENT_URI,
				MaintenanceDetailsContract.MaintenanceDetailsEntry.FULL_PROJECTION,
				null,
				null,
				null);

		if (tmpCursor != null) {
			Log.v("VEHICLE_ID_CHECK", "CHECK COUNT ITEM: " + tmpCursor.getCount());
			tmpCursor.close();
		}

		return rootView;
	}

	@NonNull
	@Override
	public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
		return new CursorLoader(requireContext(),
				MaintenanceEntry.CONTENT_URI,
				MaintenanceEntry.FULL_PROJECTION,
				null,
				null,
				MaintenanceEntry.COLUMN_DATE + " DESC");
	}

	@Override
	public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
		_historyCursorAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(@NonNull Loader<Cursor> loader) {
		_historyCursorAdapter.swapCursor(null);
	}
}
