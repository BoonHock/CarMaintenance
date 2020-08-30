package com.incupe.vewec;

import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.incupe.vewec.fragments.NewsViewFragment;
import com.incupe.vewec.utilities.Misc;

public class NewsViewActivity extends SingleFragmentActivity {
	@Override
	protected Fragment createFragment() {
		Misc.startNoInternetActivityIfNoNetwork(this);

		return NewsViewFragment.newInstance(getIntent()
				.getStringExtra(NewsViewFragment.EXTRA_NEWS_URL));
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		finish();
		return true;
	}
}