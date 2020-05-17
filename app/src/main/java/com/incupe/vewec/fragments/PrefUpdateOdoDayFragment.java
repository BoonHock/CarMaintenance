package com.incupe.vewec.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.incupe.vewec.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrefUpdateOdoDayFragment extends DialogFragment {
	private static final String ARG_DAYS = "day";
	public static final String EXTRA_DAYS = ARG_DAYS;

	public static final String DAY_SEPARATOR = ", ";

	private List<CheckBox> _cbs;

	public static PrefUpdateOdoDayFragment newInstance(String days) {
		Bundle bundle = new Bundle();
		bundle.putString(ARG_DAYS, days);

		PrefUpdateOdoDayFragment fragment = new PrefUpdateOdoDayFragment();
		fragment.setArguments(bundle);

		return fragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout) View.inflate(getActivity(),
				R.layout.dialog_pref_update_odo_day, null);

		_cbs = new ArrayList<>();
		for (int i = 0, j = rootView.getChildCount(); i < j; i++) {
			if (rootView.getChildAt(i) instanceof CheckBox) {
				_cbs.add((CheckBox) rootView.getChildAt(i));
			}
		}

		if (getArguments() != null) {
			String days = getArguments().getString(ARG_DAYS);
			if (days != null) {
				List<String> preSelectDays = Arrays.asList(days.split(DAY_SEPARATOR, 0));
				for (CheckBox cb : _cbs) {
					cb.setChecked(preSelectDays.contains(cb.getText().toString()));
				}
			}
		}

		return new AlertDialog.Builder(getActivity())
				.setView(rootView)
				.setTitle(getString(R.string.update_odo_day_label))
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								StringBuilder selectedDays = new StringBuilder();
								String result = "";
								for (CheckBox cb : _cbs) {
									if (cb.isChecked()) {
										selectedDays.append(cb.getText().toString()
												.trim()).append(DAY_SEPARATOR);
									}
								}
								if (selectedDays.length() > 0) {
									selectedDays = new StringBuilder(selectedDays
											.substring(0, selectedDays.length() - 2));
									result = selectedDays.toString();
								}
								sendResults(result);
							}
						})
				.create();
	}

	private void sendResults(String days) {
		if (getTargetFragment() == null) {
			return;
		}
		Intent intent = new Intent();
		intent.putExtra(EXTRA_DAYS, days);

		getTargetFragment().onActivityResult(getTargetRequestCode(),
				Activity.RESULT_OK, intent);
	}
}
