package com.incupe.vewec;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.incupe.vewec.utilities.UserDialog;

public class GetStartedActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new GetStartedFragment();
	}

	public static class GetStartedFragment extends Fragment {
		@Nullable
		@Override
		public View onCreateView(@NonNull LayoutInflater inflater,
								 @Nullable ViewGroup container,
								 @Nullable Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.activity_get_started,
					container, false);

			Button btnNext = view.findViewById(R.id.btn_next);
			Button btnSkip = view.findViewById(R.id.btn_skip);

			btnNext.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.putExtra("get_started",
							GetStartedActivity.class.getSimpleName());
					requireActivity().setResult(RESULT_OK);
					requireActivity().finish();
				}
			});

			btnSkip.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					PreferenceManager
							.getDefaultSharedPreferences(requireContext())
							.edit()
							.putBoolean(getString(R.string.pref_get_started),false)
							.apply();
					requireActivity().finish();
				}
			});

			return view;
		}
	}

	@Override
	public void onBackPressed() {
		UserDialog.showDialog(this,
				"",
				getString(R.string.skip_tutorial_question),
				getString(R.string.skip),
				getString(R.string.continue_r),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						PreferenceManager
								.getDefaultSharedPreferences(GetStartedActivity.this)
								.edit()
								.putBoolean(getString(R.string.pref_get_started), false)
								.apply();
						GetStartedActivity.super.onBackPressed();
					}
				},
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (dialog != null) {
							dialog.dismiss();
						}
					}
				},
				null);
	}
}
