package com.incupe.vewec;

import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.incupe.vewec.fragments.FuelPriceFragment;

public class FuelPriceActivity extends SingleFragmentActivity {
	@Override
	protected Fragment createFragment() {
		return new FuelPriceFragment();
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		finish();
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
}