package com.incupe.vewec;

import androidx.fragment.app.Fragment;

import com.incupe.vewec.fragments.MaintenanceItemEditorFragment;

public class MaintenanceItemEditorActivity extends SingleFragmentActivity {
	@Override
	protected Fragment createFragment() {
		return new MaintenanceItemEditorFragment();
	}
}
