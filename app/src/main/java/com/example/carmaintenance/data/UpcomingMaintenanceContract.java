package com.example.carmaintenance.data;

import android.net.Uri;
import android.provider.BaseColumns;

import com.example.carmaintenance.data.OdometerContract.OdometerEntry;
import com.example.carmaintenance.data.UserVehicleContract.UserVehicleEntry;

public class UpcomingMaintenanceContract {
	private UpcomingMaintenanceContract() {
	}

	public static final String PATH_UPCOMING_MAINTENANCE = "upcoming_maintenance";

	public static final class UpcomingMaintenanceEntry implements BaseColumns {
		public static final Uri CONTENT_URI = Uri
				.withAppendedPath(APP_MASTER_CONTRACT.BASE_CONTENT_URI,
						PATH_UPCOMING_MAINTENANCE);

		public static final String SELECT_MOST_RECENT_ODOMETER = "SELECT "
				+ OdometerEntry.TABLE_NAME + "." + OdometerEntry.COLUMN_VEHICLE + ", "
				+ OdometerEntry.TABLE_NAME + "." + OdometerEntry.COLUMN_DISTANCE + ", "
				+ OdometerEntry.TABLE_NAME + "." + OdometerEntry.COLUMN_DATE
				+ " FROM " + OdometerEntry.TABLE_NAME
				+ " JOIN (SELECT " + OdometerEntry.COLUMN_VEHICLE + ", "
				+ "MAX(" + OdometerEntry.COLUMN_DATE + ") " + OdometerEntry.COLUMN_DATE
				+ " FROM " + OdometerEntry.TABLE_NAME + " GROUP BY "
				+ OdometerEntry.COLUMN_VEHICLE + " ) [MAX_DATE] ON "
				+ OdometerEntry.TABLE_NAME + "." + OdometerEntry.COLUMN_VEHICLE +
				" = [MAX_DATE]." + OdometerEntry.COLUMN_VEHICLE + " AND "
				+ OdometerEntry.TABLE_NAME + "." + OdometerEntry.COLUMN_DATE
				+ " = [MAX_DATE]." + OdometerEntry.COLUMN_DATE;

		public static final String SELECT_QUERY = "SELECT "
				+ UserVehicleEntry.TABLE_NAME + "." + UserVehicleEntry._ID + ", "
				+ UserVehicleEntry.TABLE_NAME + "." + UserVehicleEntry.COLUMN_REG_NO + ", "
				+ UserVehicleEntry.TABLE_NAME + "." + UserVehicleEntry.COLUMN_BRAND + ", "
				+ UserVehicleEntry.TABLE_NAME + "." + UserVehicleEntry.COLUMN_MODEL + ", "
				+ UserVehicleEntry.TABLE_NAME + "." + UserVehicleEntry.COLUMN_VARIANT + ", "
				+ UserVehicleEntry.TABLE_NAME + "." + UserVehicleEntry.COLUMN_USAGE + ", "
				+ " IFNULL([MAX_DISTANCE]." + OdometerEntry.COLUMN_DISTANCE
				+ ", 0) " + OdometerEntry.COLUMN_DISTANCE + "  FROM "
				+ UserVehicleEntry.TABLE_NAME + " LEFT JOIN (" + SELECT_MOST_RECENT_ODOMETER
				+ ") [MAX_DISTANCE] ON "
				+ UserVehicleEntry.TABLE_NAME + "." + UserVehicleEntry._ID + " = "
				+ "[MAX_DISTANCE]." + OdometerEntry.COLUMN_VEHICLE + ";";

	}
}
