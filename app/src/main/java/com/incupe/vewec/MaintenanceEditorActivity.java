package com.incupe.vewec;

import androidx.fragment.app.Fragment;

import com.incupe.vewec.fragments.MaintenanceEditorFragment;
import com.incupe.vewec.utilities.Misc;

public class MaintenanceEditorActivity extends SingleFragmentActivity {
	@Override
	protected Fragment createFragment() {
		Misc.startNoInternetActivityIfNoNetwork(this);
		return new MaintenanceEditorFragment();
	}
}