package com.incupe.vewec.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.incupe.vewec.R;

public class TimePickerFragment extends DialogFragment {
	private static final String ARG_HOUR = "hour";
	private static final String ARG_MINUTE = "minute";

	public static final String EXTRA_HOUR = ARG_HOUR;
	public static final String EXTRA_MINUTE = ARG_MINUTE;

	private TimePicker _timepicker;

	public static TimePickerFragment newInstance(int hour, int minute) {
		Bundle bundle = new Bundle();
		bundle.putInt(ARG_HOUR, hour);
		bundle.putInt(ARG_MINUTE, minute);

		TimePickerFragment timePickerFragment = new TimePickerFragment();
		timePickerFragment.setArguments(bundle);
		return timePickerFragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		_timepicker = (TimePicker) View.inflate(requireContext(),
				R.layout.dialog_timepicker, null);
		_timepicker.setIs24HourView(true);

		if (getArguments() != null) {
			_timepicker.setHour(getArguments().getInt(ARG_HOUR));
			_timepicker.setMinute(getArguments().getInt(ARG_MINUTE));
		}

		return new AlertDialog.Builder(requireContext())
				.setView(_timepicker)
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								sendResults(_timepicker.getHour(), _timepicker.getMinute());
							}
						})
				.create();
	}

	private void sendResults(int hour, int minute) {
		if (getTargetFragment() == null) {
			return;
		}
		Intent intent = new Intent();
		intent.putExtra(EXTRA_HOUR, hour);
		intent.putExtra(EXTRA_MINUTE, minute);

		getTargetFragment().onActivityResult(getTargetRequestCode(),
				Activity.RESULT_OK, intent);
	}
}
