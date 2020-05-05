package com.incupe.vewec;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.incupe.vewec.fragments.PrefUpdateOdoDayFragment;
import com.incupe.vewec.fragments.TimePickerFragment;

import java.util.Locale;

public class SettingsActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new SettingsFragment();
	}

	public static class SettingsFragment extends Fragment {
		private static final String DIALOG_UPDATE_ODO_DAY = "DIALOG_UPDATE_ODO_DAY";
		private static final int REQUEST_UPDATE_ODO_DAY = 0;

		private static final String DIALOG_UPDATE_ODO_TIME = "DIALOG_UPDATE_ODO_TIME";
		private static final int REQUEST_UPDATE_ODO_TIME = 1;

		private TextView _txtUpdateOdoDay;
		private TextView _txtUpdateOdoTime;

		@Nullable
		@Override
		public View onCreateView(@NonNull LayoutInflater inflater,
								 @Nullable ViewGroup container,
								 @Nullable Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.activity_settings,
					container, false);

			_txtUpdateOdoDay = rootView.findViewById(R.id.txt_update_odo_day);
			_txtUpdateOdoDay.setText(PreferenceManager
					.getDefaultSharedPreferences(requireContext())
					.getString(getString(R.string.pref_odo_update_notification_day),
							getString(R.string.friday)));
			rootView.findViewById(R.id.pref_update_odo_day)
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							FragmentManager manager = requireActivity().getSupportFragmentManager();
							PrefUpdateOdoDayFragment dialog = PrefUpdateOdoDayFragment
									.newInstance(_txtUpdateOdoDay.getText().toString());
							dialog.setTargetFragment(SettingsFragment.this,
									REQUEST_UPDATE_ODO_DAY);
							dialog.show(manager, DIALOG_UPDATE_ODO_DAY);
						}
					});

			_txtUpdateOdoTime = rootView.findViewById(R.id.txt_update_odo_time);
			_txtUpdateOdoTime.setText(PreferenceManager
					.getDefaultSharedPreferences(requireContext())
					.getString(getString(R.string.pref_odo_update_notification_time),
							getString(R.string.default_notification_time)));
			rootView.findViewById(R.id.pref_update_odo_time)
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							FragmentManager manager = requireActivity().getSupportFragmentManager();
							String time = getString(R.string.default_notification_time);
							String txtTime = _txtUpdateOdoTime.getText().toString();

							if (txtTime.length() == 5) {
								try {
									int iHour = Integer.parseInt(txtTime.substring(0, 2));
									int iMin = Integer.parseInt(txtTime.substring(3, 5));

									time = String.format(Locale.getDefault(),
											"%02d", iHour) + ":"
											+ String.format(Locale.getDefault(),
											"%02d", iMin);
								} catch (Exception ex) {
									// ignore
								}
							}

							int hour = Integer.parseInt(time.substring(0, 2));
							int minute = Integer.parseInt(time.substring(3, 5));

							TimePickerFragment dialog =
									TimePickerFragment.newInstance(hour, minute);
							dialog.setTargetFragment(SettingsFragment.this,
									REQUEST_UPDATE_ODO_TIME);
							dialog.show(manager, DIALOG_UPDATE_ODO_TIME);
						}
					});

			return rootView;
		}

		@Override
		public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
			SharedPreferences manager = PreferenceManager
					.getDefaultSharedPreferences(requireContext());

			if (resultCode == Activity.RESULT_OK) {
				if (requestCode == REQUEST_UPDATE_ODO_DAY && data != null) {
					String days = data.getStringExtra(PrefUpdateOdoDayFragment.EXTRA_DAYS);
					manager.edit()
							.putString(getString(R.string.pref_odo_update_notification_day), days)
							.apply();
					_txtUpdateOdoDay.setText(days);
				}
				if (requestCode == REQUEST_UPDATE_ODO_TIME && data != null) {
					int hour = data.getIntExtra(TimePickerFragment.EXTRA_HOUR, 0);
					int minute = data.getIntExtra(TimePickerFragment.EXTRA_MINUTE, 0);

					String time = String.format(Locale.getDefault(), "%02d", hour)
							+ ":" + String.format(Locale.getDefault(), "%02d", minute);
					manager.edit()
							.putString(getString(R.string.pref_odo_update_notification_time), time)
							.apply();
					_txtUpdateOdoTime.setText(time);
				}
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		finish();
		return true;
	}
}