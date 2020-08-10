package com.incupe.vewec;

import androidx.fragment.app.Fragment;

import com.incupe.vewec.fragments.NewsHomeFragment;

public class NewsHomeActivity extends SingleFragmentActivity {
	@Override
	protected Fragment createFragment() {
		return new NewsHomeFragment();
	}
}