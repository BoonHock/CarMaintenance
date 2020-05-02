package com.incupe.vewec.objects;

import android.content.Context;
import android.database.Cursor;

import com.incupe.vewec.data.MaintenanceContract.MaintenanceEntry;
import com.incupe.vewec.data.MaintenanceDetailsContract.MaintenanceDetailsEntry;
import com.incupe.vewec.data.OdometerContract.OdometerEntry;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class UpcomingMaintenanceItem extends MaintenanceItem {
	//	private final String LOG_TAG = this.getClass().getSimpleName();
	private int _latestServiceDistance = 0;
	// use int instead of date type for latest service date
	// so that when value is 0, we know no record of service
	private long _latestServiceDate = 0;
	private int _distanceLeft;
	private long _durationDaysLeft = 0;

	public static final int URGENCY_NOT_URGENT = 0;
	public static final int URGENCY_URGENT = 1;
	public static final int URGENCY_VERY_URGENT = 2;
	public static final int URGENCY_VERY2_URGENT = 3;
	public static final int URGENCY_VERY3_URGENT = 4;

	public UpcomingMaintenanceItem(
			Context context, MaintenanceItem maintenanceItem, UserVehicle userVehicle) {
		super(maintenanceItem.getFirebase_item_id(),
				maintenanceItem.getItem(),
				maintenanceItem.getInspect_replace(),
				maintenanceItem.getUsage(),
				maintenanceItem.getFirst_distance(),
				maintenanceItem.getDistance_interval(),
				maintenanceItem.getFirst_duration(),
				maintenanceItem.getDuration_interval());

		int nextDistance;
		int currentOdometer = getCurrentOdometer(context, userVehicle.get_vehicleId());
		getLatestServiceData(context, userVehicle.get_vehicleId());

		if (currentOdometer < _latestServiceDistance) {
			currentOdometer = _latestServiceDistance;
		}

		int odoStartFrom = userVehicle.get_upcomingStartFrom();

		if (_latestServiceDistance == 0) {
			if (odoStartFrom < this.getFirst_distance()) {
				nextDistance = this.getFirst_distance();
			} else {
				nextDistance = odoStartFrom + this.getDistance_interval();
			}
		} else {
			nextDistance = _latestServiceDistance + this.getDistance_interval();
		}
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

	public int getUrgency() {
		boolean hasDistanceInterval = this.getDistance_interval() != 0;
		boolean hasDurationInterval = this.getDuration_interval() != 0;
		boolean hasLastServiceDate = this._latestServiceDate != 0;
		boolean hasDurationUrgency = hasDurationInterval && hasLastServiceDate;

		if ((hasDistanceInterval && this.get_distanceLeft() < 100)
				|| (hasDurationUrgency && this.get_durationDaysLeft() < 5)) {
			return URGENCY_VERY3_URGENT;
		} else if ((hasDistanceInterval && this.get_distanceLeft() < 500)
				|| (hasDurationUrgency && this.get_durationDaysLeft() < 10)) {
			return URGENCY_VERY2_URGENT;
		} else if ((hasDistanceInterval && this.get_distanceLeft() < 1000)
				|| (hasDurationUrgency && this.get_durationDaysLeft() < 14)) {
			return URGENCY_VERY_URGENT;
		} else if ((hasDistanceInterval && this.get_distanceLeft() < 1500)
				|| (hasDurationUrgency && this.get_durationDaysLeft() < 21)) {
			return URGENCY_URGENT;
		}
		return URGENCY_NOT_URGENT;
	}

	public static class CustomComparator implements Comparator<UpcomingMaintenanceItem> {
		@Override
		public int compare(UpcomingMaintenanceItem o1, UpcomingMaintenanceItem o2) {
			// positive value means @compareItem is before @this
			int compareResults = o2.getUrgency() - o1.getUrgency(); // descending
			// if both same urgency, and both items have distance intervals,
			// then check distance left
			// if either one doesn't have distance interval,
			// the one without distance interval shall be placed after the ones with
			if (compareResults == 0) {
				if (o1.getDistance_interval() != 0
						&& o2.getDistance_interval() != 0) {
					compareResults = o1.get_distanceLeft() - o2.get_distanceLeft(); // ascending
				} else if (o1.getDistance_interval() == 0) {
					compareResults = 1;
				} else if (o2.getDistance_interval() == 0) {
					compareResults = -1;
				}
			}

			// if still same, then arrange by name alphabetically
			if (compareResults == 0) {
				compareResults = o1.getItem().compareToIgnoreCase(o2.getItem());
			}

			return compareResults;
		}
	}
}
