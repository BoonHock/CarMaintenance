package com.incupe.vewec;

import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.incupe.vewec.fragments.NewsHomeFragment;

public class NewsHomeActivity extends SingleFragmentActivity {
	@Override
	protected Fragment createFragment() {
		return new NewsHomeFragment();
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		finish();
		return true;
	}
}