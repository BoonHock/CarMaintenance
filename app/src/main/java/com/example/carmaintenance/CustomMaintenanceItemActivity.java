package com.example.carmaintenance;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.carmaintenance.fragments.CustomMaintenanceItemFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class CustomMaintenanceItemActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment);

		// Load an ad into the AdMob banner view.
		AdView adView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);

		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.fragment_container, new CustomMaintenanceItemFragment())
				.commit();

//		FragmentManager fragmentManager = getSupportFragmentManager();
//		Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
//		if (fragment == null) {
//			fragment = new CustomMaintenanceItemFragment();
//			fragmentManager.beginTransaction()
//					.add(R.id.fragment_container, fragment)
//					.commit();
//		}
	}

//	@Override
//	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//		switch (item.getItemId()) {
//			case R.id.action_add:
//				// show dialog to add
//				return true;
//			case android.R.id.home:
//				finish();
//				return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_add, menu);
		return true;
	}
}
