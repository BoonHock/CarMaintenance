package com.example.carmaintenance.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.example.carmaintenance.R;

public class PreferenceContract {
	public static int getStartUpcomingMaintenanceFrom(Context context) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		return Integer.parseInt(preferences.getString(context.getString(
				R.string.pref_key_start_upcoming_maintenance_from), "0"));
	}
}
