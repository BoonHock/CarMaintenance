package com.example.carmaintenance;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.carmaintenance.arrayadapter.VehicleTemplateAdapter;
import com.example.carmaintenance.objects.VehicleTemplate;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class VehicleTemplateActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vehicle_template);

		ListView listView = findViewById(R.id.ll_vehicle_template);

		final List<VehicleTemplate> vehicleTemplates = new ArrayList<>();
		final VehicleTemplateAdapter vehicleTemplateAdapter = new VehicleTemplateAdapter(
				this, R.layout.list_vehicle_template, vehicleTemplates);
		listView.setAdapter(vehicleTemplateAdapter);

		FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
		DatabaseReference databaseReference = firebaseDatabase.getReference().child("vehicle_template");
		ChildEventListener childEventListener = new ChildEventListener() {
			@Override
			public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				VehicleTemplate vehicleTemplate = dataSnapshot.getValue(VehicleTemplate.class);
				vehicleTemplateAdapter.add(vehicleTemplate);
			}

			@Override
			public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
			}

			@Override
			public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
			}

			@Override
			public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		};
		databaseReference.addChildEventListener(childEventListener);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		finish();
		return true;
	}
}
