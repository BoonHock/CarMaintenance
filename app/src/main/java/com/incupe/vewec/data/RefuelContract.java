package com.incupe.vewec.data;

import android.net.Uri;
import android.provider.BaseColumns;

import com.incupe.vewec.data.UserVehicleContract.UserVehicleEntry;

public class RefuelContract {
	public static final String PATH_REFUEL = "REFUEL";

	public static final class RefuelEntry implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				APP_MASTER_CONTRACT.BASE_CONTENT_URI, PATH_REFUEL);

		public static final String TABLE_NAME = "refuel";
		public static final String _ID = BaseColumns._ID;
		public static final String COLUMN_VEHICLE = "vehicle";
		public static final String COLUMN_DATE = "date";
		public static final String COLUMN_PRICE = "price";
		public static final String COLUMN_VOLUME = "volume";
		public static final String COLUMN_ODOMETER = "odometer";
		public static final String COLUMN_FUEL_TYPE = "fuel_type";
		public static final String COLUMN_IS_FULL_TANK = "is_full_tank";

		public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
				+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_VEHICLE + " INTEGER NOT NULL, "
				+ COLUMN_DATE + " INTEGER NOT NULL, "
				+ COLUMN_PRICE + " REAL NOT NULL, "
				+ COLUMN_VOLUME + " REAL NOT NULL, "
				+ COLUMN_ODOMETER + " INTEGER NOT NULL, "
				+ COLUMN_FUEL_TYPE + " TEXT NOT NULL, "
				+ COLUMN_IS_FULL_TANK + " INTEGER NOT NULL, "
				+ "FOREIGN KEY (" + COLUMN_VEHICLE + ") REFERENCES "
				+ UserVehicleEntry.TABLE_NAME + "(" + UserVehicleEntry._ID
				+ ") ON DELETE CASCADE);";

		public static final String[] FULL_PROJECTION = {
				_ID,
				COLUMN_VEHICLE,
				COLUMN_DATE,
				COLUMN_PRICE,
				COLUMN_VOLUME,
				COLUMN_ODOMETER,
				COLUMN_FUEL_TYPE,
				COLUMN_IS_FULL_TANK
		};
	}
}
