package com.incupe.vewec;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class NoInternetActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_no_internet);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		this.finish();
		return true;
	}
}
