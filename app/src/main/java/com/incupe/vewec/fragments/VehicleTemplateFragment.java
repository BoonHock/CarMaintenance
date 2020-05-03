package com.incupe.vewec.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.incupe.vewec.R;
import com.incupe.vewec.arrayadapter.VehicleTemplateAdapter;
import com.incupe.vewec.objects.VehicleTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VehicleTemplateFragment extends Fragment {
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		View rootView = inflater.inflate(R.layout.item_list, container, false);
		final ListView listView = (ListView) rootView;

		FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
		DatabaseReference databaseReference = firebaseDatabase.getReference().child("vehicle_template");
		databaseReference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				List<VehicleTemplate> firebaseVehicleTemplates = new ArrayList<>();
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					firebaseVehicleTemplates.add(snapshot.getValue(VehicleTemplate.class));
				}
				Collections.sort(firebaseVehicleTemplates, new VehicleTemplate.CustomComparator());
				final VehicleTemplateAdapter vehicleTemplateAdapter =
						new VehicleTemplateAdapter(requireContext(),
								R.layout.list_vehicle_template, firebaseVehicleTemplates);
				listView.setAdapter(vehicleTemplateAdapter);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		});
		return rootView;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		requireActivity().finish();
		return true;
	}
}
