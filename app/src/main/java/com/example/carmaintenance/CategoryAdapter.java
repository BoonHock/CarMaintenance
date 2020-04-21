package com.example.carmaintenance;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.carmaintenance.fragments.UserVehicleFragment;
import com.example.carmaintenance.fragments.HistoryFragment;
import com.example.carmaintenance.fragments.OdometerFragment;
import com.example.carmaintenance.fragments.SettingsFragment;
import com.example.carmaintenance.fragments.UpcomingFragment;

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
			case 3:
				return _context.getString(R.string.tab_vehicles);
			default:
				return _context.getString(R.string.tab_settings);
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
			case 3:
				return new UserVehicleFragment();
			default:
				return new SettingsFragment();
		}
	}

	@Override
	public int getCount() {
		return 5;
	}
}
