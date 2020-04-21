package com.example.carmaintenance.data;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.carmaintenance.objects.VehicleTemplate;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

public class VehicleTemplateFirebase {
	String LOG_TAG = VehicleTemplateFirebase.class.getSimpleName();

	private VehicleTemplateFirebase() {
	}

	public ArrayList<VehicleTemplate> getVehicleTemplate(Context context, DataSnapshot dataSnapshot) {
		ArrayList<VehicleTemplate> vehicleTemplates = new ArrayList<>();

		try {
			for (DataSnapshot vehicleSnapshot : dataSnapshot.getChildren()) {
				VehicleTemplate vehicleTemplate = vehicleSnapshot.getValue(VehicleTemplate.class);
				vehicleTemplates.add(vehicleTemplate);
			}
		} catch (Exception ex) {
			Toast.makeText(context, "WORK IN PROGRESS", Toast.LENGTH_SHORT).show();
			Log.e(LOG_TAG, "ERROR RETRIEVING VEHICLES" + ex);
		}

		return vehicleTemplates;
	}
}
