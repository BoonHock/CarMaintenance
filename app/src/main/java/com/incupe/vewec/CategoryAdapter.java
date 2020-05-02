package com.incupe.vewec;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.incupe.vewec.fragments.HistoryFragment;
import com.incupe.vewec.fragments.OdometerFragment;
import com.incupe.vewec.fragments.UpcomingFragment;
import com.incupe.vewec.fragments.UserVehicleFragment;

public class CategoryAdapter extends FragmentPagerAdapter {
	private Context _context;

	public CategoryAdapter(Context context, @NonNull FragmentManager fm, int behavior) {
		super(fm, behavior);
		_context = context;
	}

	@Nullable
	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
			case 0:
				return _context.getString(R.string.tab_upcoming);
			case 1:
				return _context.getString(R.string.tab_history);
			case 2:
				return _context.getString(R.string.odometer);
			default:
				return _context.getString(R.string.tab_vehicles);
		}
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0:
				return new UpcomingFragment();
			case 1:
				return new HistoryFragment();
			case 2:
				return new OdometerFragment();
			default:
				return new UserVehicleFragment();
		}
	}

	@Override
	public int getCount() {
		return 4;
	}
}
