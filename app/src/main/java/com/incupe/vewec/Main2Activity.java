package com.incupe.vewec;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

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
						Intent intent;
						switch (item.getItemId()) {
							case R.id.nav_settings:
								intent = new Intent(Main2Activity.this, SettingsActivity.class);
								startActivity(intent);
								break;
							case R.id.nav_fuel_price:
								intent = new Intent(Main2Activity.this, FuelPriceActivity.class);
								startActivity(intent);
								break;
//							case R.id.nav_share:
//								intent = new Intent(Intent.ACTION_SEND);
//								intent.setType("text/plain");
//								intent.putExtra(android.content.Intent.EXTRA_TEXT,
//										"Hi! Vewec is doing a great job at helping me " +
//												"manage my vehicle's maintenance and trips. " +
//												"You can get it too.\n" +
//												"https://vewecweb.wixsite.com/app-installation");
//								startActivity(Intent.createChooser(intent, "Share now"));
//								break;
						}
						drawer.closeDrawer(GravityCompat.START);
						return true;
					}
				});

		FirebaseInstanceId.getInstance().getInstanceId()
				.addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
					@Override
					public void onComplete(@NonNull Task<InstanceIdResult> task) {
						if (!task.isSuccessful()) {
							Log.w("CHECK_ME", "getInstanceId failed", task.getException());
							return;
						}

						// Get new Instance ID token
						InstanceIdResult result = task.getResult();
						if (result != null) {
							String token = result.getToken();

							// Log and toast
							Log.v("CHECK_ME", token);

//							ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//							ClipData clip = ClipData.newPlainText("firebase_token", token);
//							if (clipboard != null) {
//								clipboard.setPrimaryClip(clip);
//							}
						}
					}
				});
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
}