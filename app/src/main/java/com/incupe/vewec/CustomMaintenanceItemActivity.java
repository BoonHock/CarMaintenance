package com.incupe.vewec;

import androidx.fragment.app.Fragment;

import com.incupe.vewec.fragments.CustomMaintenanceItemFragment;

public class CustomMaintenanceItemActivity extends SingleFragmentActivity {
	@Override
	protected Fragment createFragment() {
		return new CustomMaintenanceItemFragment();
	}
}
