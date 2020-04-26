package com.example.carmaintenance.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VehicleMaintenanceDbHelper extends SQLiteOpenHelper {
	public static final String LOG_TAG = VehicleMaintenanceDbHelper.class.getSimpleName();

	/**
	 * Name of the database file
	 */
	private static final String DATABASE_NAME = "car_maintenance.db";

	@Override
	public void onOpen(SQLiteDatabase db) {
		db.setForeignKeyConstraintsEnabled(true);
		super.onOpen(db);
	}

	/**
	 * Database version. If you change the database schema, you must increment the database version.
	 */
	private static final int DATABASE_VERSION = 4;

	/**
	 * Constructs a new instance of {@link VehicleMaintenanceDbHelper}.
	 *
	 * @param context of the app
	 */
	public VehicleMaintenanceDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * This is called when the database is created for the first time.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(UserVehicleContract.UserVehicleEntry.CREATE_TABLE);
		db.execSQL(OdometerContract.OdometerEntry.CREATE_TABLE);
		db.execSQL(MaintenanceContract.MaintenanceEntry.CREATE_TABLE);
		db.execSQL(MaintenanceDetailsContract.MaintenanceDetailsEntry.CREATE_TABLE);
		db.execSQL(MaintenanceItemContract.MaintenanceItemEntry.CREATE_TABLE);
	}

	/**
	 * This is called when the database needs to be upgraded.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < 2) {
			db.execSQL("DROP TABLE IF EXISTS user_maintenance_item");
		}
		if (oldVersion < 3) {
			db.execSQL(CustomMaintenanceItemContract.CustomMaintenanceItemEntry.CREATE_TABLE);
		}
		if (oldVersion < 4) {
			db.execSQL(UserVehicleContract.UserVehicleEntry.ALTER_TABLE_V4);
		}
	}
}
