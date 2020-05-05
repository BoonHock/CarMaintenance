package com.incupe.vewec.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import androidx.preference.PreferenceManager;

import com.incupe.vewec.MaintenanceEditorActivity;
import com.incupe.vewec.R;
import com.incupe.vewec.cursoradapter.UpcomingMaintenanceCursorAdapter;
import com.incupe.vewec.data.UserVehicleContract;

/**
 * A simple {@link Fragment} subclass.
 */
public class UpcomingFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	/**
	 * Identifier for the data loader
	 */
	private static final int USER_VEHICLE_LOADER = 0;

	private UpcomingMaintenanceCursorAdapter _upcomingMaintenanceCursorAdapter;
	private ProgressBar _progressBar;
	private RelativeLayout _rlContent;

	private LinearLayout _llIntro;
	private TextView _txtIntro;
	private Button _btnPrev;
	private Button _btnNext;

	private int introPageCounter = 0;

	public UpcomingFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.listview_with_empty_view,
				container, false);

		_llIntro = rootView.findViewById(R.id.intro_msg);
		_btnNext = rootView.findViewById(R.id.btn_next);
		_btnPrev = rootView.findViewById(R.id.btn_previous);
		_txtIntro = rootView.findViewById(R.id.txt_intro);

		if (PreferenceManager.getDefaultSharedPreferences(requireContext())
				.getBoolean(getString(R.string.pref_upcoming_intro), true)) {
			_llIntro.setVisibility(View.VISIBLE);
			setupIntro();
		}

		_progressBar = rootView.findViewById(R.id.progress_bar);
		_rlContent = rootView.findViewById(R.id.rl_content);
		ListView listView = rootView.findViewById(R.id.item_list);
		View emptyView = rootView.findViewById(R.id.empty_view);

		_progressBar.setVisibility(View.VISIBLE);
		_rlContent.setVisibility(View.INVISIBLE);

		listView.setEmptyView(emptyView);

		((TextView) rootView.findViewById(R.id.empty_title_text))
				.setText(R.string.add_vehicle_to_get_started);
		((ImageView) rootView.findViewById(R.id.empty_image))
				.setImageResource(R.drawable.ic_sentiment_satisfied_24dp);

		_upcomingMaintenanceCursorAdapter = new UpcomingMaintenanceCursorAdapter(getContext(), null);
		listView.setAdapter(_upcomingMaintenanceCursorAdapter);

		getLoaderManager().initLoader(USER_VEHICLE_LOADER, null, UpcomingFragment.this);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(getContext(), MaintenanceEditorActivity.class);
				intent.putExtra("vehicle_id", String.valueOf(id));
				startActivity(intent);
			}
		});
		_btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				introPageCounter += 1; // next page
				setupIntro();
			}
		});
		_btnPrev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				introPageCounter -= 1; // prev page
				setupIntro();
			}
		});

		return rootView;
	}

	@NonNull
	@Override
	public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
		// This loader will execute the ContentProvider's query method on a background thread
		return new CursorLoader(requireContext(),   // Parent activity context
				UserVehicleContract.UserVehicleEntry.CONTENT_URI,   // Provider content URI to query
				UserVehicleContract.UserVehicleEntry.FULL_PROJECTION,    // Columns to include in the resulting Cursor
				null,                   // No selection clause
				null,                   // No selection arguments
				null);                  // Default sort order
	}

	@Override
	public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
		_upcomingMaintenanceCursorAdapter.swapCursor(data);
		_progressBar.setVisibility(View.GONE);
		_rlContent.setVisibility(View.VISIBLE);
	}

	@Override
	public void onLoaderReset(@NonNull Loader<Cursor> loader) {
		_upcomingMaintenanceCursorAdapter.swapCursor(null);
	}

	private void setupIntro() {
		_btnPrev.setEnabled(true);
		_btnNext.setText(getString(R.string.next));

		switch (introPageCounter) {
			case 0:
				_txtIntro.setText(Html.fromHtml(getString(R.string.get_started_upcoming_msg0)));
				_btnPrev.setEnabled(false);
				break;
			case 1:
				_txtIntro.setText(Html.fromHtml(getString(R.string.get_started_upcoming_msg1)));
				break;
			case 2:
				_txtIntro.setText(Html.fromHtml(getString(R.string.get_started_upcoming_msg2)));
				_btnNext.setText(getString(R.string.got_it));
				break;
			default:
				PreferenceManager
						.getDefaultSharedPreferences(requireContext())
						.edit()
						.putBoolean(getString(R.string.pref_upcoming_intro), false)
						.apply();
				_llIntro.setVisibility(View.GONE);
				break;
		}
	}
}