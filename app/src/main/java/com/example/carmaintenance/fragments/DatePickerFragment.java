package com.example.carmaintenance.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.carmaintenance.R;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DatePickerFragment extends DialogFragment {
	static final String ARG_DATE = "date";
	private static final String ARG_MAX_DATE = "min_date";
	private static final String ARG_MIN_DATE = "max_date";

	private DatePicker _datePicker;

	public static DatePickerFragment newInstance(Date date) {
		Bundle bundle = new Bundle();
		bundle.putSerializable(ARG_DATE, date);

		DatePickerFragment datePickerFragment = new DatePickerFragment();
		datePickerFragment.setArguments(bundle);
		return datePickerFragment;
	}

	static DatePickerFragment newInstance(Date date, Date maxDate, Date minDate) {
		Bundle bundle = new Bundle();
		bundle.putSerializable(ARG_DATE, date);
		bundle.putSerializable(ARG_MAX_DATE, maxDate);
		bundle.putSerializable(ARG_MIN_DATE, minDate);

		DatePickerFragment datePickerFragment = new DatePickerFragment();
		datePickerFragment.setArguments(bundle);
		return datePickerFragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		View v = View.inflate(requireContext(), R.layout.dialog_date, null);
		_datePicker = v.findViewById(R.id.dialog_date_picker);

		final Calendar calendar = Calendar.getInstance();

		if (getArguments() != null) {
			Date date = (Date) getArguments().getSerializable(ARG_DATE);
			if (date != null) {
				calendar.setTime(date);
			}
			date = (Date) getArguments().getSerializable(ARG_MAX_DATE);
			if (date != null) {
				_datePicker.setMaxDate(date.getTime());
			}
			date = (Date) getArguments().getSerializable(ARG_MIN_DATE);
			if (date != null) {
				_datePicker.setMinDate(date.getTime());
			}
		}

		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

		_datePicker.init(year, month, dayOfMonth, null);

		return new AlertDialog.Builder(requireActivity())
				.setView(v)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int year = _datePicker.getYear();
						int month = _datePicker.getMonth();
						int dayOfMonth = _datePicker.getDayOfMonth();
						Date date = new GregorianCalendar(year, month, dayOfMonth).getTime();
						sendResult(date);
					}
				})
				.create();
	}

	private void sendResult(Date date) {
		if (getTargetFragment() == null) return;

		Intent intent = new Intent();
		intent.putExtra(ARG_DATE, date);

		getTargetFragment().onActivityResult(getTargetRequestCode(),
				Activity.RESULT_OK, intent);
	}
}
