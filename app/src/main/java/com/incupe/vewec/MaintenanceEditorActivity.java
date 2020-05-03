package com.incupe.vewec;

import androidx.fragment.app.Fragment;

import com.incupe.vewec.fragments.MaintenanceEditorFragment;

public class MaintenanceEditorActivity extends SingleFragmentActivity {
	@Override
	protected Fragment createFragment() {
		return new MaintenanceEditorFragment();
	}
}