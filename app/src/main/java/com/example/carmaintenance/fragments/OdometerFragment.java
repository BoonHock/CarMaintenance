package com.example.carmaintenance.fragments;

import android.content.ContentUris;
import android.content.Intent;
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

import com.example.carmaintenance.OdometerEditorActivity;
import com.example.carmaintenance.R;
import com.example.carmaintenance.cursoradapter.OdometerCursorAdapter;
import com.example.carmaintenance.data.OdometerContract.OdometerEntry;

public class OdometerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private OdometerCursorAdapter _odometerAdapter;
	private static final int LOADER_ID = 0;

	public OdometerFragment() {
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		_odometerAdapter = new OdometerCursorAdapter(getContext(), null);
		View rootView = inflater.inflate(R.layout.listview_with_empty_view,
				container, false);

		ListView listView = rootView.findViewById(R.id.item_list);
		View emptyView = rootView.findViewById(R.id.empty_view);

		((TextView) rootView.findViewById(R.id.empty_title_text))
				.setText(R.string.empty_here);
		((ImageView) rootView.findViewById(R.id.empty_image))
				.setImageResource(R.drawable.ic_near_me_24dp);

		listView.setEmptyView(emptyView);
		listView.setAdapter(_odometerAdapter);

		// TODO: fix deprecated call
		// Kick off the loader
		getLoaderManager().initLoader(LOADER_ID, null, this);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(getContext(), OdometerEditorActivity.class);
				Uri currentUri = ContentUris.withAppendedId(OdometerEntry.CONTENT_URI, id);
				intent.setData(currentUri);
				startActivity(intent);
			}
		});

		rootView.findViewById(R.id.progress_bar).setVisibility(View.GONE);

		return rootView;
	}

	@NonNull
	@Override
	public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
		return new CursorLoader(getContext(),
				OdometerEntry.CONTENT_URI_VEHICLE,
				null,
				null,
				null,
				null);
	}

	@Override
	public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
		_odometerAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(@NonNull Loader<Cursor> loader) {
		_odometerAdapter.swapCursor(null);
	}
}
