package com.incupe.vewec.data;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;

/*
 * not sure if really need to separate out maintenance item
 * from maintenance details column...
 * is it really necessary? will it take too much space if not?
 * */
public class MaintenanceItemContract {
	private MaintenanceItemContract() {
	}

	public static final String PATH_MAINTENANCE_ITEM = "maintenance_item";

	public static final class MaintenanceItemEntry {
		public static final Uri CONTENT_URI = Uri
				.withAppendedPath(APP_MASTER_CONTRACT.BASE_CONTENT_URI,
						PATH_MAINTENANCE_ITEM);

		public static final String TABLE_NAME = "maintenance_item";
		public static final String _ID = BaseColumns._ID;
		public static final String COLUMN_ITEM = "item";
		public static final String COLUMN_INSPECT_REPLACE = "inspect_replace";
		public static final String COLUMN_VEHICLE = "vehicle";
		public static final String COLUMN_FIRST_DISTANCE = "first_distance";
		public static final String COLUMN_FIRST_DURATION = "first_duration";
		public static final String COLUMN_DISTANCE_INTERVAL = "distance_interval";
		public static final String COLUMN_DURATION_INTERVAL = "duration_interval";

		public static final int INSPECT_VALUE = FirebaseContract.MaintenanceDetails.INSPECT;
		public static final int REPLACE_VALUE = FirebaseContract.MaintenanceDetails.REPLACE;

		// checked with vendor's items. their maximum are about 40 characters
		public static final int ITEM_NAME_MAX_LENGTH = 50;

		//		public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
//				+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
//				+ COLUMN_ITEM + " TEXT NOT NULL, "
//				+ COLUMN_INSPECT_REPLACE + " INTEGER NOT NULL, "
//				+ "UNIQUE (" + COLUMN_ITEM + ", " + COLUMN_INSPECT_REPLACE + ")"
//				+ ");";
		public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
				_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				COLUMN_ITEM + " TEXT NOT NULL, " +
				COLUMN_INSPECT_REPLACE + " INTEGER NOT NULL, " +
				COLUMN_VEHICLE + " INTEGER NOT NULL, " +
				COLUMN_FIRST_DISTANCE + " INTEGER NOT NULL, " +
				COLUMN_FIRST_DURATION + " INTEGER NOT NULL, " +
				COLUMN_DISTANCE_INTERVAL + " INTEGER NOT NULL, " +
				COLUMN_DURATION_INTERVAL + " INTEGER NOT NULL, " +
				" FOREIGN KEY(" + COLUMN_VEHICLE + ") REFERENCES " +
				UserVehicleContract.UserVehicleEntry.TABLE_NAME + "(" +
				UserVehicleContract.UserVehicleEntry._ID +
				") ON DELETE CASCADE, UNIQUE (" +
				COLUMN_ITEM + ", " +
				COLUMN_INSPECT_REPLACE + ", " +
				COLUMN_VEHICLE +
				")" +
				");";

		public static final String ALTER_TABLE_V9 = "ALTER TABLE " + TABLE_NAME +
				" RENAME TO " + TABLE_NAME + "_TMP;";

		public static final String INSERT_FROM_TMP_V9 = "INSERT INTO " + TABLE_NAME +
				" SELECT " + _ID + ", " +
				COLUMN_ITEM + ", " +
				COLUMN_INSPECT_REPLACE + ", " +
				"(SELECT " + UserVehicleContract.UserVehicleEntry._ID + " FROM " +
				UserVehicleContract.UserVehicleEntry.TABLE_NAME + " LIMIT 1), " +
				"0, " +
				"0 FROM " + TABLE_NAME + "_TMP;";

		public static final String DROP_TMP_TABLE_V9 = " DROP TABLE " + TABLE_NAME + "_TMP";

		//		public static final String[] FULL_PROJECTION = {
//				_ID,
//				COLUMN_ITEM,
//				COLUMN_INSPECT_REPLACE
//		};
		public static final String[] FULL_PROJECTION = {
				_ID,
				COLUMN_ITEM,
				COLUMN_INSPECT_REPLACE,
				COLUMN_VEHICLE,
				COLUMN_FIRST_DISTANCE,
				COLUMN_FIRST_DURATION,
				COLUMN_DISTANCE_INTERVAL,
				COLUMN_DURATION_INTERVAL
		};

		public static Uri INSERT_MAINTENANCE_iTEM(Context context,
												  String itemName,
												  int inspectReplace,
												  int vehicleId,
												  int firstDistance,
												  int firstDuration,
												  int distanceInterval,
												  int durationInterval) {
			ContentValues values = new ContentValues();
			values.put(COLUMN_ITEM, itemName);
			values.put(COLUMN_INSPECT_REPLACE, inspectReplace);
			values.put(COLUMN_VEHICLE, vehicleId);
			values.put(COLUMN_FIRST_DISTANCE, firstDistance);
			values.put(COLUMN_FIRST_DURATION, firstDuration);
			values.put(COLUMN_DISTANCE_INTERVAL, distanceInterval);
			values.put(COLUMN_DURATION_INTERVAL, durationInterval);

			return context.getContentResolver()
					.insert(CONTENT_URI, values);
		}

		public static int UPDATE_MAINTENANCE_ITEM(Context context,
												  Uri updateUri,
												  String itemName,
												  int inspectReplace,
												  int vehicleId,
												  int firstDistance,
												  int firstDuration,
												  int distanceInterval,
												  int durationInterval) {
			ContentValues values = new ContentValues();
			values.put(COLUMN_ITEM, itemName);
			values.put(COLUMN_INSPECT_REPLACE, inspectReplace);
			values.put(COLUMN_VEHICLE, vehicleId);
			values.put(COLUMN_FIRST_DISTANCE, firstDistance);
			values.put(COLUMN_FIRST_DURATION, firstDuration);
			values.put(COLUMN_DISTANCE_INTERVAL, distanceInterval);
			values.put(COLUMN_DURATION_INTERVAL, durationInterval);

			return context.getContentResolver().update(
					updateUri,
					values,
					null,
					null);
		}
	}
}
