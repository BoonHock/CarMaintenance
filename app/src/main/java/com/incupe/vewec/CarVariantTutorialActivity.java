package com.incupe.vewec;

import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.incupe.vewec.fragments.CarVariantTutorialFragment;

public class CarVariantTutorialActivity extends SingleFragmentActivity {
	@Override
	protected Fragment createFragment() {
		return new CarVariantTutorialFragment();
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		finish();
		return true;
	}
}
