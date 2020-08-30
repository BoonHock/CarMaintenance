package com.incupe.vewec.objects;

import android.content.Context;
import android.database.Cursor;

import com.incupe.vewec.data.MaintenanceItemContract.MaintenanceItemEntry;

import java.util.ArrayList;
import java.util.List;

public class MaintenanceItems {
	private List<MaintenanceItem> _replaceItems;
	private List<MaintenanceItem> _inspectItems;

	public static MaintenanceItems getDbMaintenanceItems(Context context, int vehicleId) {
		MaintenanceItems items = new MaintenanceItems();
		items._replaceItems = new ArrayList<>();
		items._inspectItems = new ArrayList<>();

//		Cursor cursor = context.getContentResolver().query(
//				MaintenanceItemEntry.CONTENT_URI,
//				MaintenanceItemEntry.FULL_PROJECTION,
//				null,
//				null,
//				null
//		);
		Cursor cursor = context.getContentResolver().query(
				MaintenanceItemEntry.CONTENT_URI,
				MaintenanceItemEntry.FULL_PROJECTION,
				MaintenanceItemEntry.COLUMN_VEHICLE + "=?",
				new String[]{String.valueOf(vehicleId)},
				null
		);

		if (cursor != null) {
			while (cursor.moveToNext()) {
				MaintenanceItem item = new MaintenanceItem(
						"",
						cursor.getString(cursor.getColumnIndexOrThrow(
								MaintenanceItemEntry.COLUMN_ITEM)),
						cursor.getInt(cursor.getColumnIndexOrThrow(
								MaintenanceItemEntry.COLUMN_INSPECT_REPLACE)),
						cursor.getInt(cursor.getColumnIndexOrThrow(
								MaintenanceItemEntry.COLUMN_FIRST_DISTANCE)),
						cursor.getInt(cursor.getColumnIndexOrThrow(
								MaintenanceItemEntry.COLUMN_DISTANCE_INTERVAL)),
						cursor.getInt(cursor.getColumnIndexOrThrow(
								MaintenanceItemEntry.COLUMN_FIRST_DURATION)),
						cursor.getInt(cursor.getColumnIndexOrThrow(
								MaintenanceItemEntry.COLUMN_DURATION_INTERVAL))
				);
				if (item.getInspect_replace() == MaintenanceItemEntry.REPLACE_VALUE) {
					items._replaceItems.add(item);
				} else {
					items._inspectItems.add(item);
				}
			}
			cursor.close();
		}

		return items;
	}

	public List<MaintenanceItem> get_replaceItems() {
		return _replaceItems;
	}

	public List<MaintenanceItem> get_inspectItems() {
		return _inspectItems;
	}
}
