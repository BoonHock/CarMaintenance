package com.example.carmaintenance.data;

import android.net.Uri;
import android.provider.BaseColumns;

import com.example.carmaintenance.data.UserVehicleContract.UserVehicleEntry;

public class OdometerContract {
	private OdometerContract() {
	}

	public static final String PATH_ODOMETER = "odometer";
	public static final String PATH_ODOMETER_VEHICLE = PATH_ODOMETER + "/vehicle";

	public static final class OdometerEntry implements BaseColumns {
		public static final Uri CONTENT_URI = Uri
				.withAppendedPath(APP_MASTER_CONTRACT.BASE_CONTENT_URI, PATH_ODOMETER);
		// content uri to include vehicle reg no. with odometer data
		public static final Uri CONTENT_URI_VEHICLE =
				Uri.withAppendedPath(APP_MASTER_CONTRACT.BASE_CONTENT_URI,
						PATH_ODOMETER_VEHICLE);

		public static final String TABLE_NAME = "odometer";
		public static final String _ID = BaseColumns._ID;
		public static final String COLUMN_VEHICLE = "vehicle";
		public static final String COLUMN_DISTANCE = "distance";
		public static final String COLUMN_DATE = "date";

		public static final int DISTANCE_MIN = 1;
		public static final int DISTANCE_MAX = 9999999;

		public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
				+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_VEHICLE + " INTEGER NOT NULL, "
				+ COLUMN_DISTANCE + " INTEGER NOT NULL, "
				+ COLUMN_DATE + " INTEGER NOT NULL, "
				+ "FOREIGN KEY (" + COLUMN_VEHICLE + ") REFERENCES "
				+ UserVehicleEntry.TABLE_NAME + "(" + UserVehicleEntry._ID + ") ON DELETE CASCADE, "
				+ "UNIQUE (" + COLUMN_VEHICLE + ", " + COLUMN_DATE + ")"
				+ ");";

//		public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

		public static final String[] FULL_PROJECTION = {
				_ID,
				COLUMN_VEHICLE,
				COLUMN_DISTANCE,
				COLUMN_DATE
		};
	}
}
