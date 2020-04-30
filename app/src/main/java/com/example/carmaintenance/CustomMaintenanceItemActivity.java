package com.example.carmaintenance;

import androidx.fragment.app.Fragment;

import com.example.carmaintenance.fragments.CustomMaintenanceItemFragment;

public class CustomMaintenanceItemActivity extends SingleFragmentActivity {
	@Override
	protected Fragment createFragment() {
		return new CustomMaintenanceItemFragment();
	}
}
