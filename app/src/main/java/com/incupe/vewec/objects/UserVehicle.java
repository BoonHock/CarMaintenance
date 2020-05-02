package com.incupe.vewec.objects;

import android.content.Context;
import android.database.Cursor;

import com.incupe.vewec.data.OdometerContract.OdometerEntry;
import com.incupe.vewec.data.UserVehicleContract.UserVehicleEntry;

import java.util.Date;

public class UserVehicle {
	private int _vehicleId;
	private String _regNo;
	private String _brand;
	private String _model;
	private String _variant;
	private int _usage;
	private int _upcomingStartFrom;
	private Date _addedOn = null;

	// cursor is select data from user_vehicle table
	public UserVehicle(Cursor cursor) {
		if (cursor.getPosition() == -1) {
			if (!cursor.moveToNext()) return;
		}
		_vehicleId = cursor.getInt(cursor
				.getColumnIndexOrThrow(UserVehicleEntry._ID));
		_regNo = cursor.getString(cursor
				.getColumnIndexOrThrow(UserVehicleEntry.COLUMN_REG_NO));
		_brand = cursor.getString(cursor
				.getColumnIndexOrThrow(UserVehicleEntry.COLUMN_BRAND));
		_model = cursor.getString(cursor
				.getColumnIndexOrThrow(UserVehicleEntry.COLUMN_MODEL));
		_variant = cursor.getString(cursor
				.getColumnIndexOrThrow(UserVehicleEntry.COLUMN_VARIANT));
		_usage = cursor.getInt(cursor
				.getColumnIndexOrThrow(UserVehicleEntry.COLUMN_USAGE));
		_upcomingStartFrom = cursor.getInt(cursor
				.getColumnIndexOrThrow(UserVehicleEntry.COLUMN_UPCOMING_START_FROM));
		_addedOn = new Date(cursor.getLong(cursor
				.getColumnIndexOrThrow(UserVehicleEntry.COLUMN_CREATED_ON)));
	}

	public String get_regNo() {
		return _regNo;
	}

	public String get_brand() {
		return _brand;
	}

	public String get_model() {
		return _model;
	}

	public String get_brandModelVariant() {
		return get_brand() + " " + get_model() + " " + get_variant();
	}

	public Date get_addedOn() {
		return _addedOn;
	}

	public String get_variant() {
		return _variant;
	}

	public int get_usage() {
		return _usage;
	}

	public int get_vehicleId() {
		return _vehicleId;
	}

	public int get_upcomingStartFrom() {
		return _upcomingStartFrom;
	}

	public int getLatestOdometer(Context context) {
		int latestOdo = 0;

		Cursor cursor = context.getContentResolver().query(
				OdometerEntry.CONTENT_URI,
				OdometerEntry.FULL_PROJECTION,
				OdometerEntry.COLUMN_VEHICLE + "=?",
				new String[]{String.valueOf(this._vehicleId)},
				OdometerEntry.COLUMN_DATE + " DESC LIMIT 1");
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				latestOdo = cursor.getInt(cursor
						.getColumnIndexOrThrow(OdometerEntry.COLUMN_DISTANCE));
			}
			cursor.close();
		}
		return latestOdo;
	}
}
