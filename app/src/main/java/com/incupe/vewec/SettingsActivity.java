package com.incupe.vewec;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

		private SharedPreferences _sharedPreferences;

		@Nullable
		@Override
		public View onCreateView(@NonNull LayoutInflater inflater,
								 @Nullable ViewGroup container,
								 @Nullable Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.activity_settings,
					container, false);

			_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

			_txtUpdateOdoDay = rootView.findViewById(R.id.txt_update_odo_day);
			_txtUpdateOdoDay.setText(_sharedPreferences
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
			_txtUpdateOdoTime.setText(_sharedPreferences
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

			if (resultCode == Activity.RESULT_OK) {
				if (requestCode == REQUEST_UPDATE_ODO_DAY && data != null) {
					String days = data.getStringExtra(PrefUpdateOdoDayFragment.EXTRA_DAYS);
					_sharedPreferences.edit()
							.putString(getString(R.string.pref_odo_update_notification_day), days)
							.apply();
					_txtUpdateOdoDay.setText(days);
					setOdoReminderAlarm(requireContext());
				}
				if (requestCode == REQUEST_UPDATE_ODO_TIME && data != null) {
					int hour = data.getIntExtra(TimePickerFragment.EXTRA_HOUR, 0);
					int minute = data.getIntExtra(TimePickerFragment.EXTRA_MINUTE, 0);

					String time = String.format(Locale.getDefault(), "%02d", hour)
							+ ":" + String.format(Locale.getDefault(), "%02d", minute);
					_sharedPreferences.edit()
							.putString(getString(R.string.pref_odo_update_notification_time), time)
							.apply();
					_txtUpdateOdoTime.setText(time);
					setOdoReminderAlarm(requireContext());
				}
			}
		}

		public static void setOdoReminderAlarm(Context context) {
			SharedPreferences sharedPreferences =
					PreferenceManager.getDefaultSharedPreferences(context);
			String days = sharedPreferences
					.getString(context.getString(R.string.pref_odo_update_notification_day),
							context.getString(R.string.friday));
			String time = sharedPreferences
					.getString(context.getString(R.string.pref_odo_update_notification_time),
							context.getString(R.string.default_notification_time));

			com.incupe.vewec.utilities.NotificationReceiver
					.createNotificationChannel(context,
							context.getString(R.string.update_odometer_reminder_notification),
							context.getString(R.string.update_odometer_reminder),
							"");

			AlarmManager alarmManager = (AlarmManager) context
					.getSystemService(ALARM_SERVICE);
			Intent intent = new Intent(context, NotificationReceiver.class);
			intent.putExtra(NotificationReceiver.NOTIFICATION_TYPE,
					NotificationReceiver.UPDATE_ODOMETER_REMINDER);
			PendingIntent pendingIntent;

			// cancel all previous alarms, and re-add if got selected days
			for (int i = 1; i <= 7; i++) {
				pendingIntent = PendingIntent.getBroadcast(context,
						i,
						intent, 0);
				if (alarmManager != null) {
					alarmManager.cancel(pendingIntent);
				}
			}

			int hour = Integer.parseInt(time.substring(0, 2));
			int minute = Integer.parseInt(time.substring(3, 5));

			if (!days.trim().equals("")) {
				String[] listDays = days.split(
						PrefUpdateOdoDayFragment.DAY_SEPARATOR, 0);
				for (String day : listDays) {
					Calendar calendar = getNextUpcomingDayTime(day, hour, minute);

					if (calendar != null) {
						Log.v("CHECK_ME", "alarm set for: "
								+ day + " "
								+ calendar.get(Calendar.DAY_OF_MONTH) + "/"
								+ (calendar.get(Calendar.MONTH) + 1) + "/"
								+ calendar.get(Calendar.YEAR) + " "
								+ calendar.get(Calendar.HOUR_OF_DAY) + ":"
								+ calendar.get(Calendar.MINUTE));

						pendingIntent = PendingIntent.getBroadcast(context,
								calendar.get(Calendar.DAY_OF_WEEK), // id to differentiate with other days
								intent, 0);
						if (alarmManager != null) {
							alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
									calendar.getTimeInMillis(),
									AlarmManager.INTERVAL_DAY * 7,
									pendingIntent);
						}
					}
					// TODO: if calendar null means got error!!!
				}
			}
		}

		private static Calendar getNextUpcomingDayTime(String dayName, int hourOfDay, int minute) {
			Calendar calendar = Calendar.getInstance(),
					tmpCal = Calendar.getInstance();

			SimpleDateFormat dateFormat = new SimpleDateFormat("E", Locale.getDefault());
			Date date = null;

			try {
				date = dateFormat.parse(dayName);
			} catch (ParseException e) {
				e.printStackTrace();
				Log.v("CHECK_ME", "ERROR PARSING DAY!!!");
			}

			if (date == null) {
				return null;
			}
			tmpCal.setTime(date);

			int dayOfWeekDiff = tmpCal.get(Calendar.DAY_OF_WEEK)
					- calendar.get(Calendar.DAY_OF_WEEK);

			if (dayOfWeekDiff < 0) {
				dayOfWeekDiff += 7;
			} else if (dayOfWeekDiff == 0) {
				tmpCal.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
				tmpCal.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
				tmpCal.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR));
				tmpCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
				tmpCal.set(Calendar.MINUTE, minute);
				tmpCal.set(Calendar.SECOND, 0);
				tmpCal.set(Calendar.MILLISECOND, 0);

				// if alarm is on the same day as today and time is before current time,
				// set alarm to be next week
				if (tmpCal.getTimeInMillis() < calendar.getTimeInMillis()) {
					dayOfWeekDiff = 7; // next week
				}
			}

			calendar.add(Calendar.DAY_OF_YEAR, dayOfWeekDiff);
			calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			calendar.set(Calendar.MINUTE, minute);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);

			return calendar;
		}
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		finish();
		return true;
	}
}