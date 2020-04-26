package com.example.carmaintenance.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.carmaintenance.R;
import com.example.carmaintenance.cursoradapter.CustomMaintenanceItemCursorAdapter;
import com.example.carmaintenance.data.CustomMaintenanceItemContract.CustomMaintenanceItemEntry;
import com.example.carmaintenance.utilities.Misc;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class CustomMaintenanceItemFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private CustomMaintenanceItemCursorAdapter _customMaintenanceItemCursorAdapter;
	private static final int LOADER_ID = 0;
	private long _longClickId = 0;
	private RelativeLayout _llDialog;
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
		_customMaintenanceItemCursorAdapter =
				new CustomMaintenanceItemCursorAdapter(getContext(), null);
		View rootView = inflater.inflate(R.layout.listview_with_empty_view,
				container, false);

		_llDialog = (RelativeLayout) inflater.inflate(R.layout.dialog_custom_maintenance_item,
				(ViewGroup) rootView, true);
		_llDialog.setVisibility(View.GONE);

		ListView listView = rootView.findViewById(R.id.item_list);
		View emptyView = rootView.findViewById(R.id.empty_view);
		_spinnerInspectReplace = rootView.findViewById(R.id.spinner_inspect_replace);

		((TextView) rootView.findViewById(R.id.empty_title_text))
				.setText(requireContext().getString(R.string.empty_here));
		((ImageView) rootView.findViewById(R.id.empty_image))
				.setImageResource(R.drawable.ic_build_24dp);

		listView.setEmptyView(emptyView);
		listView.setAdapter(_customMaintenanceItemCursorAdapter);

		if (getContext() != null) {
			final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
			final View sheetView = getLayoutInflater()
					.inflate(R.layout.bottom_sheet_edit_delete, listView, false);
			bottomSheetDialog.setContentView(sheetView);
		}
		View.OnClickListener closeDialogListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Misc.hideKeyboard(requireActivity());
				_llDialog.setVisibility(View.GONE);
			}
		};
		rootView.findViewById(R.id.txt_close).setOnClickListener(closeDialogListener);
//		rootView.findViewById(R.id.ll_mask).setOnClickListener(closeDialogListener);

		rootView.findViewById(R.id.txt_save).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: save item
				_llDialog.setVisibility(View.GONE);
			}
		});

		// TODO:
		// item name filter length
		// distance interval filter length
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

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_add:
				// show dialog to add
				_llDialog.setVisibility(View.VISIBLE);
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
				null);
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
