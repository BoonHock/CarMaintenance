package com.incupe.vewec.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.incupe.vewec.R;

public class SettingsFragment extends PreferenceFragmentCompat {
	public SettingsFragment() {
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
	}
}
