package com.incupe.vewec.data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VehicleMaintenanceDbHelper extends SQLiteOpenHelper {
//	public static final String LOG_TAG = VehicleMaintenanceDbHelper.class.getSimpleName();

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
	private static final int DATABASE_VERSION = 6;

	/**
	 * Constructs a new instance of {@link VehicleMaintenanceDbHelper}.
	 *
	 * @param context of the app
	 */
	VehicleMaintenanceDbHelper(Context context) {
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
		db.execSQL(com.incupe.vewec.data.MaintenanceItemContract.MaintenanceItemEntry.CREATE_TABLE);
		db.execSQL(CustomMaintenanceItemContract.CustomMaintenanceItemEntry.CREATE_TABLE);

		runQueryIgnoreException(db, UserVehicleContract.UserVehicleEntry.ALTER_TABLE_V5);
	}

	/**
	 * This is called when the database needs to be upgraded.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < 5) {
			runQueryIgnoreException(db, UserVehicleContract.UserVehicleEntry.ALTER_TABLE_V5);
		}
		if (oldVersion < 6) {
			db.execSQL(RefuelContract.RefuelEntry.CREATE_TABLE);
		}
	}

	private void runQueryIgnoreException(SQLiteDatabase db, String query) {
		try {
			db.execSQL(query);
		} catch (SQLException e) {
			// ignore
		}
	}
}
