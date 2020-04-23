package com.example.carmaintenance.objects;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;

import com.example.carmaintenance.data.MaintenanceContract.MaintenanceEntry;

public class MaintenanceItem {
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

	public String getFirebase_item_id() {
		return firebase_item_id;
	}

	public void setFirebase_item_id(String firebase_item_id) {
		this.firebase_item_id = firebase_item_id;
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

	public void setInspect_replace(int inspect_replace) {
		this.inspect_replace = inspect_replace;
	}

	public int getUsage() {
		return usage;
	}

	public void setUsage(int usage) {
		this.usage = usage;
	}

	public int getFirst_distance() {
		return first_distance;
	}

	public void setFirst_distance(int first_distance) {
		this.first_distance = first_distance;
	}

	public int getDistance_interval() {
		return distance_interval;
	}

	public void setDistance_interval(int distance_interval) {
		this.distance_interval = distance_interval;
	}

	public int getFirst_duration() {
		return first_duration;
	}

	public void setFirst_duration(int first_duration) {
		this.first_duration = first_duration;
	}

	public int getDuration_interval() {
		return duration_interval;
	}

	public void setDuration_interval(int duration_interval) {
		this.duration_interval = duration_interval;
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
}
