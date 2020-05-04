package com.incupe.vewec;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.incupe.vewec.data.OdometerContract.OdometerEntry;

public class SettingsActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new SettingsFragment();
	}

	public static class SettingsFragment extends PreferenceFragmentCompat {
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource(R.xml.preferences, rootKey);
			EditTextPreference editTextStartUpcoming = findPreference(getString(
					R.string.pref_key_start_upcoming_maintenance_from));
			if (editTextStartUpcoming != null) {
				editTextStartUpcoming.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
					@Override
					public void onBindEditText(@NonNull EditText editText) {
						editText.setFilters(new InputFilter[]{
								new InputFilter.LengthFilter(String.valueOf(OdometerEntry.DISTANCE_MAX).length())
						});
						editText.setInputType(InputType.TYPE_CLASS_NUMBER);
					}
				});
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		finish();
		return true;
	}
}