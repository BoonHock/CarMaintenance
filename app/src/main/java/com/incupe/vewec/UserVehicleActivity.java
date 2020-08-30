package com.incupe.vewec;

import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.incupe.vewec.fragments.UserVehicleFragment;

public class UserVehicleActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new UserVehicleFragment();
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		finish();
		return true;
	}
}