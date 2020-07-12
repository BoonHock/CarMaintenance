package com.incupe.vewec;

import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.incupe.vewec.fragments.RefuelEditorFragment;

public class RefuelEditorActivity extends SingleFragmentActivity {
	public static final String EXTRA_VEHICLE_ID = "EXTRA_VEHICLE_ID";

	@Override
	protected Fragment createFragment() {
		return new RefuelEditorFragment();
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
