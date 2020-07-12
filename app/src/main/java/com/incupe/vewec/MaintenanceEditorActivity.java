package com.incupe.vewec;

import androidx.fragment.app.Fragment;

import com.incupe.vewec.fragments.MaintenanceEditorFragment;
import com.incupe.vewec.utilities.Misc;

public class MaintenanceEditorActivity extends SingleFragmentActivity {
	public static final String EXTRA_VEHICLE_ID = "EXTRA_VEHICLE_ID";
	@Override
	protected Fragment createFragment() {
		Misc.startNoInternetActivityIfNoNetwork(this);
		return new MaintenanceEditorFragment();
	}
}