package com.incupe.vewec;

import androidx.fragment.app.Fragment;

import com.incupe.vewec.fragments.VehicleTemplateFragment;
import com.incupe.vewec.utilities.Misc;

public class VehicleTemplateActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		Misc.startNoInternetActivityIfNoNetwork(this);
		return new VehicleTemplateFragment();
	}
}
