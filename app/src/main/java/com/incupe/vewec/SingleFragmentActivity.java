package com.incupe.vewec;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public abstract class SingleFragmentActivity extends AppCompatActivity {
	protected abstract Fragment createFragment();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment);

		// Load an ad into the AdMob banner view.
		AdView adView = findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);

		Fragment fragment = createFragment();
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.fragment_container, fragment)
				.commit();
	}
}
