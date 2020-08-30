package com.incupe.vewec.fragments;

import android.animation.Animator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.incupe.vewec.CategoryAdapter;
import com.incupe.vewec.MaintenanceEditorActivity;
import com.incupe.vewec.MaintenanceItemEditorActivity;
import com.incupe.vewec.OdometerEditorActivity;
import com.incupe.vewec.R;
import com.incupe.vewec.RefuelEditorActivity;
import com.incupe.vewec.VehicleEditorActivity;
import com.incupe.vewec.data.UserVehicleContract;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
	private AppBarConfiguration mAppBarConfiguration;

	private boolean _isOpenFab = true;
	private ArrayList<View> _fabButtons;

	private FloatingActionButton _fabMenu;
	private LinearLayout _llMask;
	private LinearLayout _llFabVehicle;
	private LinearLayout _llFabRefuel;
	private LinearLayout _llFabOdometer;
	private LinearLayout _llFabMaintenance;
	private LinearLayout _llFabMaintenanceItem;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
							 @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_home, container, false);

		final ProgressBar progressBar = root.findViewById(R.id.progress_bar);
		final RelativeLayout rlContent = root.findViewById(R.id.rl_content);
		_llMask = root.findViewById(R.id.ll_mask);
		_fabMenu = root.findViewById(R.id.fab);
		_llFabVehicle = root.findViewById(R.id.ll_fab_vehicle);
		_llFabRefuel = root.findViewById(R.id.ll_fab_refuel);
		_llFabOdometer = root.findViewById(R.id.ll_fab_odometer);
		_llFabMaintenance = root.findViewById(R.id.ll_fab_maintenance);
		_llFabMaintenanceItem = root.findViewById(R.id.ll_fab_maintenance_item);

		_llMask.setVisibility(View.GONE);

		setupFabMenu();

		progressBar.setVisibility(View.GONE);
		rlContent.setVisibility(View.VISIBLE);

		ViewPager viewPager = root.findViewById(R.id.viewpager);
		CategoryAdapter categoryAdapter = new CategoryAdapter(requireContext(),
				requireActivity().getSupportFragmentManager(), 1);
		viewPager.setAdapter(categoryAdapter);

		// Find the tab layout that shows the tabs
		TabLayout tabLayout = root.findViewById(R.id.tabs);

		// Connect the tab layout with the view pager. This will
		//   1. Update the tab layout when the view pager is swiped
		//   2. Update the view pager when a tab is selected
		//   3. Set the tab layout's tab names with the view pager's adapter's titles
		//      by calling onPageTitle()
		tabLayout.setupWithViewPager(viewPager);

		_llMask.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideFabMenu();
			}
		});

//		getDeviceFirebaseToken();

		return root;
	}

	@Override
	public void onResume() {
		super.onResume();

		// when activity is resumed, recheck if has vehicle registered to current session
		if (getContext() != null) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

			if (prefs.getInt(getString(R.string.pref_session_vehicle), 0) == 0) {
				Cursor cursor = getContext().getContentResolver().query(
						UserVehicleContract.UserVehicleEntry.CONTENT_URI,
						UserVehicleContract.UserVehicleEntry.FULL_PROJECTION,
						null,
						null,
						null
				);

				if (cursor != null) {
					if (cursor.moveToFirst()) {
						prefs.edit()
								.putInt(getString(R.string.pref_session_vehicle),
										cursor.getInt(cursor.getColumnIndexOrThrow(
												UserVehicleContract.UserVehicleEntry._ID)))
								.apply();
					}
					cursor.close();
				}

				if (prefs.getInt(getString(R.string.pref_session_vehicle), 0) == 0) {
					// if still no session vehicle set, do not display these
					_llFabRefuel.setVisibility(View.GONE);
					_llFabOdometer.setVisibility(View.GONE);
					_llFabMaintenance.setVisibility(View.GONE);
					_llFabMaintenanceItem.setVisibility(View.GONE);
				}
			}
		}
	}

	private void getDeviceFirebaseToken() {
		FirebaseInstanceId.getInstance().getInstanceId()
				.addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
					@Override
					public void onComplete(@NonNull Task<InstanceIdResult> task) {
						if (!task.isSuccessful()) {
							Log.w("CHECK_ME", "getInstanceId failed", task.getException());
							return;
						}

						// Get new Instance ID token
						InstanceIdResult instanceIdResult = task.getResult();

						if (instanceIdResult != null) {
							String token = task.getResult().getToken();
							// Log and toast
							Log.v("CHECK_ME", token);
						}
					}
				});
	}

	private void setupFabMenu() {
		_fabButtons = new ArrayList<>();
		_fabButtons.add(_llFabVehicle);
		_fabButtons.add(_llFabRefuel);
		_fabButtons.add(_llFabOdometer);
		_fabButtons.add(_llFabMaintenance);
		_fabButtons.add(_llFabMaintenanceItem);

		for (View v : _fabButtons) {
			v.setY(_fabMenu.getTop());
			v.setVisibility(View.INVISIBLE);
		}
		_fabMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (_isOpenFab) {
					showFabMenu();
				} else {
					hideFabMenu();
				}
			}
		});

		_llFabVehicle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideFabMenu();
				Intent intent = new Intent(requireActivity(), VehicleEditorActivity.class);
				startActivity(intent);
			}
		});
		_llFabRefuel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideFabMenu();
				Intent intent = new Intent(requireActivity(), RefuelEditorActivity.class);
				startActivity(intent);
			}
		});
		_llFabOdometer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideFabMenu();
				Intent intent = new Intent(requireActivity(), OdometerEditorActivity.class);
				startActivity(intent);
			}
		});
		_llFabMaintenance.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideFabMenu();
				Intent intent = new Intent(requireActivity(), MaintenanceEditorActivity.class);
				startActivity(intent);
			}
		});
		_llFabMaintenanceItem.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideFabMenu();
				Intent intent = new Intent(requireActivity(), MaintenanceItemEditorActivity.class);
				startActivity(intent);
			}
		});
	}

	private void showFabMenu() {
		if (getContext() != null) {
			_llMask.setAlpha(0);

			_llMask.setVisibility(View.VISIBLE);
			_llMask.animate().alpha((float) 0.5);

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
			for (View v : _fabButtons) {
				if (v.getId() == R.id.ll_fab_vehicle) {
					// TODO: temporary limit only one vehicle allowed
					if (UserVehicleContract.UserVehicleEntry.getCount(requireContext()) > 0) {
						_llFabVehicle.setVisibility(View.GONE);
					} else {
						showLinearLayoutFab(v);
					}
				} else {
					if (prefs.getInt(getString(R.string.pref_session_vehicle), 0) != 0) {
						showLinearLayoutFab(v);
					}
				}
			}
			_fabMenu.animate().rotation(45);
			_isOpenFab = false;
		}
	}

	private void hideFabMenu() {
		_llMask.setAlpha((float) 0.5);

		// fade out and make gone
		_llMask.animate().alpha(0).setListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (_llMask.getAlpha() == 0) {
					// need to make mask gone or else it is blocking content view
					_llMask.setVisibility(View.GONE);
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}
		});

		for (View v : _fabButtons) {
			v.animate().alpha(0).y(_fabMenu.getTop());
		}

		_fabMenu.animate().rotation(0);
		_isOpenFab = true;
	}

	private void showLinearLayoutFab(View llFab) {
		// set initial position before animation
		llFab.setY(_fabMenu.getTop());
		llFab.setAlpha(0);

		llFab.setVisibility(View.VISIBLE);
		llFab.animate().translationY(0).alpha(1);
	}
}
