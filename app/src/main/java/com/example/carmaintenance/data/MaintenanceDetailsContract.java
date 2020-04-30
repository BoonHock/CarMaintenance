package com.example.carmaintenance.data;

import android.net.Uri;
import android.provider.BaseColumns;

import com.example.carmaintenance.data.MaintenanceContract.MaintenanceEntry;
import com.example.carmaintenance.data.MaintenanceItemContract.MaintenanceItemEntry;

public class MaintenanceDetailsContract {
	private MaintenanceDetailsContract() {
	}

	public static final String PATH_MAINTENANCE_DETAILS = "maintenance_details";

	public static final class MaintenanceDetailsEntry implements BaseColumns {
		public static final Uri CONTENT_URI = Uri
				.withAppendedPath(APP_MASTER_CONTRACT.BASE_CONTENT_URI, PATH_MAINTENANCE_DETAILS);

		// this is for selecting maintenance details via maintenance id
		public static final Uri CONTENT_URI_MAINTENANCE = Uri
				.withAppendedPath(CONTENT_URI, "maintenance");

		public static final Uri CONTENT_URI_LATEST_MAINTENANCE_DETAILS_BY_ITEM =
				Uri.withAppendedPath(CONTENT_URI, "latest_by_item");

		public static final String TABLE_NAME = "maintenance_details";
		public static final String _ID = BaseColumns._ID;
		public static final String COLUMN_MAINTENANCE_ID = "maintenance";
		public static final String COLUMN_ITEM = "item";
		public static final String COLUMN_PRICE = "price";

		// arbitrary value. if need to set higher in future then set lo
		public static final int PRICE_MAX_LENGTH = 8;

		public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
				+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_MAINTENANCE_ID + " INTEGER NOT NULL, "
				+ COLUMN_ITEM + " INTEGER NOT NULL, "
				+ COLUMN_PRICE + " REAL NOT NULL, "
				+ "FOREIGN KEY(" + COLUMN_MAINTENANCE_ID + ") REFERENCES "
				+ MaintenanceEntry.TABLE_NAME + "(" + MaintenanceEntry._ID
				+ ") ON DELETE CASCADE, "
				+ "FOREIGN KEY(" + COLUMN_ITEM + ") REFERENCES "
				+ MaintenanceItemEntry.TABLE_NAME + "(" + MaintenanceItemEntry._ID
				+ ") ON DELETE RESTRICT, UNIQUE (" + COLUMN_MAINTENANCE_ID + ", "
				+ COLUMN_ITEM + "));";

		// select maintenance details and also its item name
		public static final String SELECT_LATEST_MAINTENANCE_DETAILS_BY_ITEM =
				"SELECT " + MaintenanceEntry.TABLE_NAME + "." + MaintenanceEntry.COLUMN_DATE + ", "
						+ MaintenanceEntry.TABLE_NAME + "." + MaintenanceEntry.COLUMN_ODOMETER
						+ " FROM " + TABLE_NAME
						+ " JOIN " + MaintenanceEntry.TABLE_NAME
						+ " ON " + TABLE_NAME + "." + COLUMN_MAINTENANCE_ID
						+ " = " + MaintenanceEntry.TABLE_NAME + "." + MaintenanceEntry._ID
						+ " JOIN " + MaintenanceItemEntry.TABLE_NAME
						+ " ON " + TABLE_NAME + "." + MaintenanceDetailsEntry.COLUMN_ITEM
						+ " = " + MaintenanceItemEntry.TABLE_NAME + "." + MaintenanceItemEntry._ID
						+ " WHERE "
						+ MaintenanceEntry.TABLE_NAME + "." + MaintenanceEntry.COLUMN_VEHICLE
						+ " =? AND "
						+ MaintenanceItemEntry.TABLE_NAME + "." + MaintenanceItemEntry.COLUMN_ITEM
						+ " =? AND "
						+ MaintenanceItemEntry.TABLE_NAME + "." + MaintenanceItemEntry.COLUMN_INSPECT_REPLACE
						+ " =? ORDER BY "
						+ MaintenanceEntry.TABLE_NAME + "." + MaintenanceEntry.COLUMN_DATE
						+ " DESC LIMIT 1;";

		// select maintenance details and also its item name by maintenance id
		public static final String SELECT_JOIN_MAINTENANCE_ITEM_ID =
				"SELECT " + TABLE_NAME + "." + _ID + ", "
						+ TABLE_NAME + "." + COLUMN_MAINTENANCE_ID + ", "
						+ TABLE_NAME + "." + COLUMN_PRICE + ", "
						+ MaintenanceItemEntry.TABLE_NAME + "." + MaintenanceItemEntry.COLUMN_ITEM + ", "
						+ MaintenanceItemEntry.TABLE_NAME + "." + MaintenanceItemEntry.COLUMN_INSPECT_REPLACE
						+ " FROM " + TABLE_NAME
						+ " JOIN " + MaintenanceItemEntry.TABLE_NAME
						+ " ON " + MaintenanceItemEntry.TABLE_NAME + "." + _ID
						+ "=" + TABLE_NAME + "." + COLUMN_ITEM
						+ " WHERE " + TABLE_NAME + "." + COLUMN_MAINTENANCE_ID + "=? ORDER BY "
						+ MaintenanceItemEntry.TABLE_NAME + "." + MaintenanceItemEntry.COLUMN_ITEM
						+ ";";

		public static final String[] FULL_PROJECTION = {
				_ID,
				COLUMN_MAINTENANCE_ID,
				COLUMN_ITEM,
				COLUMN_PRICE
		};
	}
}
