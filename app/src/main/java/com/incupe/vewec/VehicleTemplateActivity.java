package com.incupe.vewec;

import androidx.fragment.app.Fragment;

import com.incupe.vewec.fragments.VehicleTemplateFragment;

public class VehicleTemplateActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new VehicleTemplateFragment();
	}
}
