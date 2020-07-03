package com.incupe.vewec.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class CustomMaintenanceItemContract {
	private CustomMaintenanceItemContract() {
	}

	public static final String PATH_CUSTOM_MAINTENANCE_ITEM = "custom_maintenance_item";

	public static final class CustomMaintenanceItemEntry implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				APP_MASTER_CONTRACT.BASE_CONTENT_URI, PATH_CUSTOM_MAINTENANCE_ITEM);

		public static final String TABLE_NAME = "custom_maintenance_item";

		public static final String _ID = BaseColumns._ID;
		public static final String COLUMN_ITEM = "item";
		public static final String COLUMN_INSPECT_REPLACE = "inspect_replace";
		public static final String COLUMN_DISTANCE_INTERVAL = "distance_interval";
		public static final String COLUMN_DURATION_INTERVAL = "duration_interval";

		public static final int INSPECT_VALUE = FirebaseContract.MaintenanceDetails.INSPECT;
		public static final int REPLACE_VALUE = FirebaseContract.MaintenanceDetails.REPLACE;

		public static final int ITEM_NAME_MAX_LENGTH =
				com.incupe.vewec.data.MaintenanceItemContract.MaintenanceItemEntry.ITEM_NAME_MAX_LENGTH;

		public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
				+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_ITEM + " TEXT NOT NULL, "
				+ COLUMN_INSPECT_REPLACE + " INTEGER NOT NULL, "
				+ COLUMN_DISTANCE_INTERVAL + " INTEGER NOT NULL, "
				+ COLUMN_DURATION_INTERVAL + " INTEGER NOT NULL, "
				+ "UNIQUE (" + COLUMN_ITEM + ", " + COLUMN_INSPECT_REPLACE + ")"
				+ ");";

		public static final String[] FULL_PROJECTION = {
				_ID,
				COLUMN_ITEM,
				COLUMN_INSPECT_REPLACE,
				COLUMN_DISTANCE_INTERVAL,
				COLUMN_DURATION_INTERVAL
		};
	}
}
