package com.incupe.vewec.data;

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

		public static final int INSPECT_VALUE = FirebaseContract
				.FirebaseMaintenanceDetailsEntry.INSPECT;
		public static final int REPLACE_VALUE = FirebaseContract
				.FirebaseMaintenanceDetailsEntry.REPLACE;

		// checked with vendor's items. their maximum are about 40 characters
		public static final int ITEM_NAME_MAX_LENGTH = 50;

		public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
				+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_ITEM + " TEXT NOT NULL, "
				+ COLUMN_INSPECT_REPLACE + " INTEGER NOT NULL, "
				+ "UNIQUE (" + COLUMN_ITEM + ", " + COLUMN_INSPECT_REPLACE + ")"
				+ ");";

		public static final String[] FULL_PROJECTION = {
				_ID,
				COLUMN_ITEM,
				COLUMN_INSPECT_REPLACE
		};
	}
}
