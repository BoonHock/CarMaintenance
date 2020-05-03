package com.incupe.vewec.data;

import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

public class UserVehicleContract {
	private UserVehicleContract() {
	}

	static final String PATH_USER_VEHICLE = "user_vehicle";

	public static final class UserVehicleEntry implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				APP_MASTER_CONTRACT.BASE_CONTENT_URI, PATH_USER_VEHICLE);

		static final String TABLE_NAME = PATH_USER_VEHICLE;

		public static final String _ID = BaseColumns._ID;
		public static final String COLUMN_REG_NO = "reg_no";
		public static final String COLUMN_BRAND = "brand";
		public static final String COLUMN_MODEL = "model";
		public static final String COLUMN_VARIANT = "variant";
		public static final String COLUMN_USAGE = "usage";
		public static final String COLUMN_UPCOMING_START_FROM = "upcoming_start_from";
		public static final String COLUMN_CREATED_ON = "created_on";

		public static final int USAGE_ALL = -1;
		static final int USAGE_NORMAL = 0;
		public static final int USAGE_SEVERE = 1;

		// maximum input length for registration number
		public static final int REG_NO_MAX_LENGTH = 10;

		public static final String[] FULL_PROJECTION = {
				_ID,
				COLUMN_REG_NO,
				COLUMN_BRAND,
				COLUMN_MODEL,
				COLUMN_VARIANT,
				COLUMN_USAGE,
				COLUMN_UPCOMING_START_FROM,
				COLUMN_CREATED_ON
		};

		static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
				+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_REG_NO + " TEXT NOT NULL UNIQUE, "
				+ COLUMN_BRAND + " TEXT NOT NULL, "
				+ COLUMN_MODEL + " TEXT NOT NULL, "
				+ COLUMN_VARIANT + " TEXT NOT NULL, "
				+ COLUMN_USAGE + " INTEGER NOT NULL, "
				+ COLUMN_UPCOMING_START_FROM + " INTEGER NOT NULL, "
				+ COLUMN_CREATED_ON + " INTEGER NOT NULL)";

		static final String ALTER_TABLE_V4 = "ALTER TABLE " + TABLE_NAME
				+ " ADD COLUMN " + COLUMN_UPCOMING_START_FROM + " INTEGER NOT NULL DEFAULT 0;";

		public static long getCount(Context context) {
			VehicleMaintenanceDbHelper helper = new VehicleMaintenanceDbHelper(context);
			SQLiteDatabase db = helper.getReadableDatabase();
			return DatabaseUtils.queryNumEntries(db, TABLE_NAME);
		}
	}
}
