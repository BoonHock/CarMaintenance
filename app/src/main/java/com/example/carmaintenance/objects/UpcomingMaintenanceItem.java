package com.example.carmaintenance.objects;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.example.carmaintenance.data.MaintenanceContract.MaintenanceEntry;
import com.example.carmaintenance.data.MaintenanceDetailsContract.MaintenanceDetailsEntry;
import com.example.carmaintenance.data.OdometerContract.OdometerEntry;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class UpcomingMaintenanceItem extends MaintenanceItem {
	private int _latestServiceDistance = 0;
	// use int instead of date type for latest service date
	// so that when value is 0, we know no record of service
	private long _latestServiceDate = 0;
	private int _distanceLeft = 0;
	private long _durationDaysLeft = 0;

	public UpcomingMaintenanceItem(
			Context context, MaintenanceItem maintenanceItem, int vehicleId) {
		super(maintenanceItem.getFirebase_item_id(),
				maintenanceItem.getItem(),
				maintenanceItem.getInspect_replace(),
				maintenanceItem.getUsage(),
				maintenanceItem.getFirst_distance(),
				maintenanceItem.getDistance_interval(),
				maintenanceItem.getFirst_duration(),
				maintenanceItem.getDuration_interval());

		int nextDistance = 0;
		int currentOdometer = getCurrentOdometer(context, vehicleId);
		getLatestServiceData(context, vehicleId);

		if (currentOdometer < _latestServiceDistance) {
			currentOdometer = _latestServiceDistance;
		}

		if (_latestServiceDistance == 0) {
			nextDistance = this.getFirst_distance();
		} else {
			nextDistance = _latestServiceDistance + this.getDistance_interval();
		}
		Log.v("CHECK_ME", this.getItem() + " " + currentOdometer
				+ " " + _latestServiceDistance + " " + nextDistance);

		_distanceLeft = nextDistance - currentOdometer;

		if (_latestServiceDate != 0) {
			Calendar calToday = Calendar.getInstance();
			Calendar calNextService = Calendar.getInstance();
			calNextService.setTime(new Date(_latestServiceDate));
			calNextService.add(Calendar.MONTH, this.getDuration_interval());

			long diff = calNextService.getTimeInMillis() - calToday.getTimeInMillis();

			_durationDaysLeft = TimeUnit.DAYS.convert(Math.abs(diff), TimeUnit.MILLISECONDS);
			if (diff < 0) {
				_durationDaysLeft = -_durationDaysLeft;
			}
		}
	}

	private int getCurrentOdometer(Context context, int vehicleId) {
		Cursor odometerCursor = context.getContentResolver().query(
				OdometerEntry.CONTENT_URI,
				OdometerEntry.FULL_PROJECTION,
				OdometerEntry.COLUMN_VEHICLE + "=?",
				new String[]{String.valueOf(vehicleId)},
				OdometerEntry.COLUMN_DATE + " DESC");

		if (odometerCursor != null) {
			if (odometerCursor.moveToFirst()) {
				return odometerCursor.getInt(odometerCursor
						.getColumnIndexOrThrow(OdometerEntry.COLUMN_DISTANCE));
			}
			odometerCursor.close();
		}
		return 0;
	}

	private void getLatestServiceData(Context context, int vehicleId) {
		Cursor cursor = context.getContentResolver().query(
				MaintenanceDetailsEntry.CONTENT_URI_LATEST_MAINTENANCE_DETAILS_BY_ITEM,
				null,
				null,
				new String[]{String.valueOf(vehicleId),
						this.getItem(),
						String.valueOf(this.getInspect_replace())},
				null);

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				_latestServiceDistance = cursor.getInt(cursor.getColumnIndexOrThrow(MaintenanceEntry.COLUMN_ODOMETER));
				_latestServiceDate = cursor.getLong(cursor.getColumnIndexOrThrow(MaintenanceEntry.COLUMN_DATE));
			}
			cursor.close();
		}
	}

	public int get_latestServiceDistance() {
		return _latestServiceDistance;
	}

	public long get_latestServiceDate() {
		return _latestServiceDate;
	}

	public int get_distanceLeft() {
		return _distanceLeft;
	}

	public long get_durationDaysLeft() {
		return _durationDaysLeft;
	}
}
