package com.incupe.vewec.objects;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.incupe.vewec.data.CustomMaintenanceItemContract.CustomMaintenanceItemEntry;
import com.incupe.vewec.data.MaintenanceContract.MaintenanceEntry;
import com.incupe.vewec.data.UserVehicleContract.UserVehicleEntry;

import java.util.ArrayList;
import java.util.List;

public class MaintenanceItem implements Comparable<MaintenanceItem> {
	private String firebase_item_id;
	private String item;
	private int inspect_replace;
	private int usage;
	private int first_distance;
	private int distance_interval;
	private int first_duration;
	private int duration_interval;

	// required for firebase
	public MaintenanceItem() {
	}

	public MaintenanceItem(String cFirebaseId, String cItem, int cInspectReplace,
						   int cUsage, int cFirstDistance, int cDistanceInterval,
						   int cFirstDuration, int cDurationInterval) {
		firebase_item_id = cFirebaseId;
		item = cItem;
		inspect_replace = cInspectReplace;
		usage = cUsage;
		first_distance = cFirstDistance;
		distance_interval = cDistanceInterval;
		first_duration = cFirstDuration;
		duration_interval = cDurationInterval;
	}

	String getFirebase_item_id() {
		return firebase_item_id;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public int getInspect_replace() {
		return inspect_replace;
	}

	public int getUsage() {
		return usage;
	}

	public int getFirst_distance() {
		// if no first distance defined, return distance interval
		return first_distance == 0 ? distance_interval : first_distance;
	}

	public int getDistance_interval() {
		return distance_interval;
	}

	public int getFirst_duration() {
		return first_duration;
	}

	public int getDuration_interval() {
		return duration_interval;
	}

	/*
	 * if got latest service record, will return array with 2 elements
	 * array[0] = distance
	 * array[1] = date
	 * if not record, return null
	 * */
	public int[] getLatestService(Context context, int vehicleId) {
		Cursor cursor = context.getContentResolver().query(
				ContentUris.withAppendedId(MaintenanceEntry.CONTENT_URI, vehicleId),
				MaintenanceEntry.FULL_PROJECTION,
				null,
				null,
				MaintenanceEntry.COLUMN_DATE + " DESC");

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				int serviceDistance = cursor.getInt(cursor
						.getColumnIndexOrThrow(MaintenanceEntry.COLUMN_ODOMETER));
				int serviceDate = cursor.getInt(cursor
						.getColumnIndexOrThrow(MaintenanceEntry.COLUMN_DATE));
				return new int[]{serviceDistance, serviceDate};
			}
			cursor.close();
		}
		return null;
	}

	@Override
	public int compareTo(@NonNull MaintenanceItem compareItem) {
		int compareResults = 0;

		// if both have distance interval, sort ascending
		// else, the one without distance interval is below the one with
		if (this.distance_interval != 0 && compareItem.getDistance_interval() != 0) {
			compareResults = this.distance_interval - compareItem.getDistance_interval();
		} else if (this.distance_interval == 0) {
			compareResults = 1;
		} else if (compareItem.getDistance_interval() == 0) {
			compareResults = -1;
		}

		if (compareResults == 0) {
			String compareName = compareItem.getItem().toUpperCase();
			String thisName = this.getItem().toUpperCase();

			compareResults = thisName.compareTo(compareName);
		}
		return compareResults;
	}

	public static List<MaintenanceItem> getCustomMaintenanceItemNotInFirebase(
			Context context, List<MaintenanceItem> firebaseItems, int inspectReplace) {
		List<MaintenanceItem> maintenanceItems = new ArrayList<>();

		Cursor cursor = context.getContentResolver().query(
				CustomMaintenanceItemEntry.CONTENT_URI,
				CustomMaintenanceItemEntry.FULL_PROJECTION,
				CustomMaintenanceItemEntry.COLUMN_INSPECT_REPLACE + "=?",
				new String[]{String.valueOf(inspectReplace)},
				null);

		if (cursor != null) {
			while (cursor.moveToNext()) {
				String itemName = cursor.getString(cursor.getColumnIndexOrThrow(
						CustomMaintenanceItemEntry.COLUMN_ITEM));
				boolean add = true;

				for (MaintenanceItem firebaseItem : firebaseItems) {
					if (firebaseItem.getItem().toUpperCase().trim()
							.equals(itemName.toUpperCase().trim())
							&& firebaseItem.getInspect_replace() == inspectReplace) {
						add = false;
						break;
					}
				}
				if (add) {
					MaintenanceItem item = new MaintenanceItem("", itemName,
							cursor.getInt(cursor.getColumnIndexOrThrow(
									CustomMaintenanceItemEntry.COLUMN_INSPECT_REPLACE)),
							UserVehicleEntry.USAGE_ALL,
							cursor.getInt(cursor.getColumnIndexOrThrow(
									CustomMaintenanceItemEntry.COLUMN_DISTANCE_INTERVAL)),
							cursor.getInt(cursor.getColumnIndexOrThrow(
									CustomMaintenanceItemEntry.COLUMN_DISTANCE_INTERVAL)),
							cursor.getInt(cursor.getColumnIndexOrThrow(
									CustomMaintenanceItemEntry.COLUMN_DURATION_INTERVAL)),
							cursor.getInt(cursor.getColumnIndexOrThrow(
									CustomMaintenanceItemEntry.COLUMN_DURATION_INTERVAL)));
					maintenanceItems.add(item);
				}
			}
			cursor.close();
		}

		return maintenanceItems;
	}
}
