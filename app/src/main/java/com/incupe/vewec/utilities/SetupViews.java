package com.incupe.vewec.utilities;

import android.content.Context;
import android.database.Cursor;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.incupe.vewec.data.UserVehicleContract.UserVehicleEntry;

import java.util.ArrayList;
import java.util.List;

public class SetupViews {
	public static List<Integer> setupVehicleRegNoSpinner(Context context, Spinner spinner) {
		List<Integer> vehicleIds = new ArrayList<>();
		Cursor cursor = context.getContentResolver().query(
				UserVehicleEntry.CONTENT_URI,
				UserVehicleEntry.FULL_PROJECTION,
				null, null, UserVehicleEntry.COLUMN_REG_NO);

		List<String> vehicles = new ArrayList<>();

		if (cursor != null) {
			if (cursor.getCount() > 0) {
				vehicleIds.clear(); // clear and add
				while (cursor.moveToNext()) {
					vehicleIds.add(cursor.getInt(cursor
							.getColumnIndexOrThrow(UserVehicleEntry._ID)));
					vehicles.add(cursor.getString(cursor
							.getColumnIndexOrThrow(UserVehicleEntry.COLUMN_REG_NO)));

				}
				ArrayAdapter<String> _vehicleAdapter =
						new ArrayAdapter<>(context,
								android.R.layout.simple_spinner_item, vehicles);
				_vehicleAdapter.setDropDownViewResource(
						android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(_vehicleAdapter);
			}
			cursor.close();
		}
		return vehicleIds;
	}
}
