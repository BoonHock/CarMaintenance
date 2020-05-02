package com.incupe.vewec.objects;

import android.util.Log;

import androidx.annotation.NonNull;

import com.incupe.vewec.data.MaintenanceItemContract.MaintenanceItemEntry;
import com.incupe.vewec.data.UserVehicleContract;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class FirebaseObj {
	// MASTER _vehicleTemplates list. should be loaded when user loads app
	public static List<VehicleTemplate> _vehicleTemplates = new ArrayList<>();

	// to be used in UpcomingMaintenanceCursorAdapter
	// create variable here instead of in the class itself
	// because the class is cursorAdapter and will be reconstructed
	// when user flip the pages around
	// persist firebase's maintenance items to reduce internet loading time.
	// Maps firebase vehicle id to list of maintenance items
	public static Map<String, List<MaintenanceItem>> _maintenanceItems = new HashMap<>();

	public FirebaseObj() {
	}

	public abstract void callback();

	/*
	 * if maintenance details data of @firebaseVehicleId is required to perform
	 * some action, this method will check if firebase
	 * */
	public static void runCallbackMaintenanceDetails(final String firebaseVehicleId, final FirebaseObj fbo) {
		if (_maintenanceItems.containsKey(firebaseVehicleId)) {
			Log.v("FIREBASE_OBJ", "LOCAL");
			fbo.callback();
		} else {
			Log.v("FIREBASE_OBJ", "ONLINE");
			FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
			DatabaseReference databaseReference = firebaseDatabase
					.getReference().child("maintenance_details").child(firebaseVehicleId);
			databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					List<MaintenanceItem> maintenanceItems = new ArrayList<>();
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						maintenanceItems.add(snapshot.getValue(MaintenanceItem.class));
					}
					_maintenanceItems.put(firebaseVehicleId, maintenanceItems);
					fbo.callback();
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {
				}
			});
		}
	}

	public static List<List<MaintenanceItem>> getItemsByInspectReplace(String firebaseVehicleId, int usage) {
		List<List<MaintenanceItem>> items = new ArrayList<>();
		items.add(new ArrayList<MaintenanceItem>()); // inspect
		items.add(new ArrayList<MaintenanceItem>()); // replace

		List<MaintenanceItem> firebaseItems = _maintenanceItems.get(firebaseVehicleId);

		if (firebaseItems != null) {
			for (MaintenanceItem item : firebaseItems) {
				if (item.getUsage() == UserVehicleContract.UserVehicleEntry.USAGE_ALL
						|| item.getUsage() == usage) {
					if (item.getInspect_replace() == MaintenanceItemEntry.INSPECT_VALUE) {
						items.get(0).add(item);
					} else if (item.getInspect_replace() == MaintenanceItemEntry.REPLACE_VALUE) {
						items.get(1).add(item);
					}
				}
			}
		}

		return items;
	}
}
