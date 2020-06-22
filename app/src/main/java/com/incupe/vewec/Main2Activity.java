package com.incupe.vewec;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.navigation.NavigationView;

public class Main2Activity extends AppCompatActivity {

	private AppBarConfiguration mAppBarConfiguration;
	private final int REQUEST_GET_STARTED_MESSAGE = 0;
	private DrawerLayout drawer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);

		// run code for only first time app opened
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (!prefs.getBoolean("firstTime", false)) {
			runFirstTime();
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean("firstTime", true);
			editor.apply();
		}
		if (PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean(getString(R.string.pref_get_started), true)) {
			Intent intent = new Intent(this, GetStartedActivity.class);
			startActivityForResult(intent, REQUEST_GET_STARTED_MESSAGE);
		}

		// Load an ad into the AdMob banner view.
		AdView adView = findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		drawer = findViewById(R.id.drawer_layout);
		final NavigationView navigationView = findViewById(R.id.nav_view);
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		mAppBarConfiguration = new AppBarConfiguration.Builder(
				R.id.nav_home)
				.setDrawerLayout(drawer)
				.build();
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
		NavigationUI.setupWithNavController(navigationView, navController);

		navigationView.setNavigationItemSelectedListener(
				new NavigationView.OnNavigationItemSelectedListener() {
					@Override
					public boolean onNavigationItemSelected(@NonNull MenuItem item) {
						if (item.getItemId() == R.id.nav_settings) {
							Intent intent = new Intent(Main2Activity.this, SettingsActivity.class);
							startActivity(intent);
						}
						drawer.closeDrawer(GravityCompat.START);
						return true;
					}
				});
//		navigationView.getMenu().findItem(R.id.nav_settings).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//			@Override
//			public boolean onMenuItemClick(MenuItem item) {
//
//				Toast.makeText(Main2Activity.this, "", Toast.LENGTH_SHORT).show();
//				return false;
//			}
//		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
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
//			case R.id.action_settings:
//				intent = new Intent(this, SettingsActivity.class);
//				startActivity(intent);
//				return true;
			case R.id.action_custom_maintenance_item:
				intent = new Intent(this, CustomMaintenanceItemActivity.class);
				startActivity(intent);
				return true;
			case R.id.action_clear_preferences:

				PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
//				getPreferences(MODE_PRIVATE).edit().clear().apply();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		final int REQUEST_GET_STARTED_VEHICLE_EDITOR = 1;
		final int REQUEST_GET_STARTED_ODOMETER_EDITOR = 2;
		Intent intent;

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case REQUEST_GET_STARTED_MESSAGE:
					intent = new Intent(this, VehicleEditorActivity.class);
					startActivityForResult(intent, REQUEST_GET_STARTED_VEHICLE_EDITOR);
					break;
				case REQUEST_GET_STARTED_VEHICLE_EDITOR:
					intent = new Intent(this, OdometerEditorActivity.class);
					startActivityForResult(intent, REQUEST_GET_STARTED_ODOMETER_EDITOR);
					break;
				case REQUEST_GET_STARTED_ODOMETER_EDITOR:
					// END OF TUTORIAL
					PreferenceManager.getDefaultSharedPreferences(this)
							.edit()
							.putBoolean(getString(R.string.pref_get_started), false)
							.apply();
					break;
			}
		}
	}

	@Override
	public boolean onSupportNavigateUp() {
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

		return NavigationUI.navigateUp(navController, mAppBarConfiguration)
				|| super.onSupportNavigateUp();
	}

	private void runFirstTime() {
		SettingsActivity.SettingsFragment.setOdoReminderAlarm(this);
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView parent, View view, int position, long id) {

			drawer.closeDrawer(Gravity.LEFT);

		}
	}
}