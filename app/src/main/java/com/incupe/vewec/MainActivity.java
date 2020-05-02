package com.incupe.vewec;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.incupe.vewec.objects.FirebaseObj;
import com.incupe.vewec.objects.VehicleTemplate;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
	private boolean _isOpenFab = true;
	private LinearLayout _llFabVehicle;
	private LinearLayout _llFabMaintenance;
	private LinearLayout _llFabOdometer;

	private FloatingActionButton _fabMenu;
	private LinearLayout _llMask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Load an ad into the AdMob banner view.
		AdView adView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);

		final ProgressBar progressBar = findViewById(R.id.progress_bar);
		final RelativeLayout rlContent = findViewById(R.id.rl_content);

		_fabMenu = findViewById(R.id.fab);
		_llFabVehicle = findViewById(R.id.ll_fab_vehicle);
		_llFabOdometer = findViewById(R.id.ll_fab_odometer);
		_llFabMaintenance = findViewById(R.id.ll_fab_maintenance);

		_llMask = findViewById(R.id.ll_mask);

		_llFabVehicle.setVisibility(View.INVISIBLE);
		_llFabOdometer.setVisibility(View.INVISIBLE);
		_llFabMaintenance.setVisibility(View.INVISIBLE);

		_llMask.setVisibility(View.GONE);

		progressBar.setVisibility(View.VISIBLE);
		rlContent.setVisibility(View.GONE);

		FirebaseDatabase _firebaseDatabase = FirebaseDatabase.getInstance();
		DatabaseReference _databaseReference = _firebaseDatabase.getReference().child("vehicle_template");
		_databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					FirebaseObj._vehicleTemplates
							.add(snapshot.getValue(VehicleTemplate.class));
				}
				progressBar.setVisibility(View.GONE);
				rlContent.setVisibility(View.VISIBLE);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		});

		setupFabMenu();

		ViewPager viewPager = findViewById(R.id.viewpager);
		CategoryAdapter categoryAdapter = new CategoryAdapter(this, getSupportFragmentManager(), 1);
		viewPager.setAdapter(categoryAdapter);

		// Find the tab layout that shows the tabs
		TabLayout tabLayout = findViewById(R.id.tabs);

		// Connect the tab layout with the view pager. This will
		//   1. Update the tab layout when the view pager is swiped
		//   2. Update the view pager when a tab is selected
		//   3. Set the tab layout's tab names with the view pager's adapter's titles
		//      by calling onPageTitle()
		tabLayout.setupWithViewPager(viewPager);

		_fabMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (_isOpenFab) {
					showFabMenu();
				} else {
					Log.v("HIDEMENU", "CALLED");
					hideFabMenu();
				}
			}
		});
		_llFabVehicle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideFabMenu();
				Intent intent = new Intent(MainActivity.this, VehicleEditorActivity.class);
				startActivity(intent);
			}
		});
		_llFabOdometer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideFabMenu();
				Intent intent = new Intent(MainActivity.this, OdometerEditorActivity.class);
				startActivity(intent);
			}
		});
		_llFabMaintenance.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideFabMenu();
				Intent intent = new Intent(MainActivity.this, MaintenanceEditorActivity.class);
				startActivity(intent);
			}
		});

		findViewById(R.id.ll_mask).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideFabMenu();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case R.id.action_vehicle_template:
				intent = new Intent(this, VehicleTemplateActivity.class);
				startActivity(intent);
				return true;
			case R.id.action_settings:
				intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
			case R.id.action_custom_maintenance_item:
				intent = new Intent(this, CustomMaintenanceItemActivity.class);
				startActivity(intent);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setupFabMenu() {
		_llFabVehicle.setY(_fabMenu.getTop());
		_llFabVehicle.setVisibility(View.INVISIBLE);

		_llFabOdometer.setY(_fabMenu.getTop());
		_llFabOdometer.setVisibility(View.INVISIBLE);

		_llFabMaintenance.setY(_fabMenu.getTop());
		_llFabMaintenance.setVisibility(View.INVISIBLE);
	}

	private void showFabMenu() {
		_llMask.setAlpha(0);

		_llMask.setVisibility(View.VISIBLE);
		_llMask.animate().alpha((float) 0.5);

		showLinearLayoutFab(_llFabVehicle);
		showLinearLayoutFab(_llFabOdometer);
		showLinearLayoutFab(_llFabMaintenance);

		_fabMenu.animate().rotation(45);
		_isOpenFab = false;
	}

	private void hideFabMenu() {
		_llMask.setAlpha((float) 0.5);
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

		hideLinearLayoutFab(_llFabVehicle);
		hideLinearLayoutFab(_llFabOdometer);
		hideLinearLayoutFab(_llFabMaintenance);

		_fabMenu.animate().rotation(0);
		_isOpenFab = true;
	}

	private void showLinearLayoutFab(LinearLayout llFab) {
		// set initial position before animation
		llFab.setY(_fabMenu.getTop());
		llFab.setAlpha(0);

		llFab.setVisibility(View.VISIBLE);
		llFab.animate().translationY(0).alpha(1);
	}

	private void hideLinearLayoutFab(LinearLayout llFab) {
		llFab.animate().alpha(0).y(_fabMenu.getTop());
	}
}