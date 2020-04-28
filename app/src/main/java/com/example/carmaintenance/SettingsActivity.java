package com.example.carmaintenance;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.carmaintenance.data.OdometerContract.OdometerEntry;
import com.example.carmaintenance.data.VehicleMaintenanceProvider;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class SettingsActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment);

		// Load an ad into the AdMob banner view.
		AdView adView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);

		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.fragment_container, new SettingsFragment())
				.commit();
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
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