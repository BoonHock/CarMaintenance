package com.example.carmaintenance.objects;

import android.database.Cursor;

import com.example.carmaintenance.data.UserVehicleContract.UserVehicleEntry;

import java.util.Date;

public class UserVehicle {
	private int _vehicleId;
	private String _regNo;
	private String _brand;
	private String _model;
	private String _variant;
	private int _usage;
	private Date _addedOn = null;

	public UserVehicle(String regNo, String brand, String model) {
		_regNo = regNo;
		_brand = brand;
		_model = model;
	}

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
		_addedOn = new Date(cursor.getLong(cursor
				.getColumnIndexOrThrow(UserVehicleEntry.COLUMN_CREATED_ON)));
	}

	public UserVehicle(String regNo, String brand, String model, Date addedOn) {
		_regNo = regNo;
		_brand = brand;
		_model = model;
		_addedOn = addedOn;
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

	public String get_brandModel() {
		return get_brand() + " " + get_model();
	}

	public String get_brandModelVariant() {
		return get_brand() + " " + get_model() + " " + get_variant();
	}

	// TODO: GET FROM DATABASE!!
	public Date get_nextServiceDate() {
		return new Date();
	}

	// TODO: GET FROM DATABASE!!
	public int get_nextServiceDistance() {
		return 100000;
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

	public String get_usageString() {
		switch (_usage) {
			case UserVehicleEntry.USAGE_SEVERE:
				return "Severe";
			case UserVehicleEntry.USAGE_NORMAL:
			default:
				return "Normal";
		}
	}
}
