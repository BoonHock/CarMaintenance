package com.example.carmaintenance.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.carmaintenance.data.MaintenanceContract.MaintenanceEntry;
import com.example.carmaintenance.data.MaintenanceDetailsContract.MaintenanceDetailsEntry;
import com.example.carmaintenance.data.MaintenanceItemContract.MaintenanceItemEntry;
import com.example.carmaintenance.data.OdometerContract.OdometerEntry;
import com.example.carmaintenance.data.UpcomingMaintenanceContract.UpcomingMaintenanceEntry;
import com.example.carmaintenance.data.UserVehicleContract.UserVehicleEntry;
import com.example.carmaintenance.data.CustomMaintenanceItemContract.CustomMaintenanceItemEntry;

public class VehicleMaintenanceProvider extends ContentProvider {
	/**
	 * Tag for the log messages
	 */
	public static final String LOG_TAG = VehicleMaintenanceProvider.class.getSimpleName();

	/**
	 * URI matcher code for the content URI for sqlite table
	 */
	private static final int USER_VEHICLE = 1;
	private static final int USER_VEHICLE_ID = 2;

	private static final int ODOMETER = 10;
	private static final int ODOMETER_ID = 11;
	private static final int ODOMETER_VEHICLE = 12;

	private static final int UPCOMING_MAINTENANCE = 20;

	private static final int MAINTENANCE = 30;
	private static final int MAINTENANCE_ID = 31;

	private static final int MAINTENANCE_DETAILS = 40;
	private static final int MAINTENANCE_DETAILS_ID = 41;
	private static final int MAINTENANCE_DETAILS_MAINTENANCE_ID = 42;
	private static final int LATEST_MAINTENANCE_DETAILS_BY_ITEM = 43;

	private static final int MAINTENANCE_ITEM = 50;
	private static final int MAINTENANCE_ITEM_ID = 51;

	private static final int CUSTOM_MAINTENANCE_ITEM = 60;

	/**
	 * UriMatcher object to match a content URI to a corresponding code.
	 * The input passed into the constructor represents the code to return for the root URI.
	 * It's common to use NO_MATCH as the input for this case.
	 */
	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	// Static initializer. This is run the first time anything is called from this class.
	static {
		sUriMatcher.addURI(APP_MASTER_CONTRACT.CONTENT_AUTHORITY,
				UserVehicleContract.PATH_USER_VEHICLE, USER_VEHICLE);
		sUriMatcher.addURI(APP_MASTER_CONTRACT.CONTENT_AUTHORITY,
				UserVehicleContract.PATH_USER_VEHICLE + "/#", USER_VEHICLE_ID);

		sUriMatcher.addURI(APP_MASTER_CONTRACT.CONTENT_AUTHORITY,
				OdometerContract.PATH_ODOMETER, ODOMETER);
		sUriMatcher.addURI(APP_MASTER_CONTRACT.CONTENT_AUTHORITY,
				OdometerContract.PATH_ODOMETER + "/#", ODOMETER_ID);
		sUriMatcher.addURI(APP_MASTER_CONTRACT.CONTENT_AUTHORITY,
				OdometerContract.PATH_ODOMETER_VEHICLE, ODOMETER_VEHICLE);
		sUriMatcher.addURI(APP_MASTER_CONTRACT.CONTENT_AUTHORITY,
				UpcomingMaintenanceContract.PATH_UPCOMING_MAINTENANCE, UPCOMING_MAINTENANCE);

		sUriMatcher.addURI(APP_MASTER_CONTRACT.CONTENT_AUTHORITY,
				MaintenanceContract.PATH_MAINTENANCE, MAINTENANCE);
		sUriMatcher.addURI(APP_MASTER_CONTRACT.CONTENT_AUTHORITY,
				MaintenanceContract.PATH_MAINTENANCE + "/#",
				MAINTENANCE_ID);

		sUriMatcher.addURI(APP_MASTER_CONTRACT.CONTENT_AUTHORITY,
				MaintenanceDetailsContract.PATH_MAINTENANCE_DETAILS,
				MAINTENANCE_DETAILS);
		sUriMatcher.addURI(APP_MASTER_CONTRACT.CONTENT_AUTHORITY,
				MaintenanceDetailsContract.PATH_MAINTENANCE_DETAILS + "/#",
				MAINTENANCE_DETAILS_ID);
		sUriMatcher.addURI(APP_MASTER_CONTRACT.CONTENT_AUTHORITY,
				MaintenanceDetailsContract.PATH_MAINTENANCE_DETAILS + "/maintenance/#",
				MAINTENANCE_DETAILS_MAINTENANCE_ID);
		sUriMatcher.addURI(APP_MASTER_CONTRACT.CONTENT_AUTHORITY,
				MaintenanceDetailsContract.PATH_MAINTENANCE_DETAILS + "/latest_by_item",
				LATEST_MAINTENANCE_DETAILS_BY_ITEM);

		sUriMatcher.addURI(APP_MASTER_CONTRACT.CONTENT_AUTHORITY,
				MaintenanceItemContract.PATH_MAINTENANCE_ITEM,
				MAINTENANCE_ITEM);
		sUriMatcher.addURI(APP_MASTER_CONTRACT.CONTENT_AUTHORITY,
				MaintenanceItemContract.PATH_MAINTENANCE_ITEM + "/#",
				MAINTENANCE_ITEM_ID);

		sUriMatcher.addURI(APP_MASTER_CONTRACT.CONTENT_AUTHORITY,
				CustomMaintenanceItemContract.PATH_CUSTOM_MAINTENANCE_ITEM,
				CUSTOM_MAINTENANCE_ITEM);
	}

	/**
	 * Database helper object
	 */
	private VehicleMaintenanceDbHelper _DbHelper;

	@Override
	public boolean onCreate() {
		_DbHelper = new VehicleMaintenanceDbHelper(getContext());
		return true;
	}

	@Nullable
	@Override
	public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
						@Nullable String[] selectionArgs, @Nullable String sortOrder) {

		// Get readable database
		SQLiteDatabase database = _DbHelper.getReadableDatabase();

		// This cursor will hold the result of the query
		Cursor cursor;

		// Figure out if the URI matcher can match the URI to a specific code
		int match = sUriMatcher.match(uri);

		switch (match) {
			case USER_VEHICLE:
				cursor = database.query(UserVehicleEntry.TABLE_NAME,
						projection,
						selection,
						selectionArgs,
						null,
						null,
						sortOrder);
				break;
			case USER_VEHICLE_ID:
				selection = UserVehicleEntry._ID + "=?";
				selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
				cursor = database.query(UserVehicleEntry.TABLE_NAME,
						projection,
						selection,
						selectionArgs,
						null,
						null,
						sortOrder);
				break;
			case ODOMETER:
				cursor = database.query(OdometerEntry.TABLE_NAME,
						projection,
						selection,
						selectionArgs,
						null,
						null,
						sortOrder);
				break;
			case ODOMETER_ID:
				selection = OdometerEntry._ID + "=?";
				selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
				cursor = database.query(OdometerEntry.TABLE_NAME,
						projection,
						selection,
						selectionArgs,
						null,
						null,
						sortOrder);
				break;
			case ODOMETER_VEHICLE:
				cursor = selectOdometerWithVehicle(database);
				break;
			case UPCOMING_MAINTENANCE:
				cursor = selectUpcomingMaintenance(database);
				break;
			case MAINTENANCE:
				cursor = database.query(MaintenanceEntry.TABLE_NAME,
						projection,
						selection,
						selectionArgs,
						null,
						null,
						sortOrder);
				break;
			case MAINTENANCE_ID:
				selection = MaintenanceEntry._ID + "=?";
				selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
				cursor = database.query(MaintenanceEntry.TABLE_NAME,
						projection,
						selection,
						selectionArgs,
						null,
						null,
						sortOrder);
				break;
			case MAINTENANCE_DETAILS:
				cursor = database.query(MaintenanceDetailsEntry.TABLE_NAME,
						projection,
						selection,
						selectionArgs,
						null,
						null,
						sortOrder);
				break;
			case MAINTENANCE_DETAILS_MAINTENANCE_ID:
				selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
				cursor = database.rawQuery(
						MaintenanceDetailsEntry.SELECT_JOIN_MAINTENANCE_ITEM_ID,
						selectionArgs);
				break;
			case LATEST_MAINTENANCE_DETAILS_BY_ITEM:
				cursor = database.rawQuery(
						MaintenanceDetailsEntry.SELECT_LATEST_MAINTENANCE_DETAILS_BY_ITEM,
						selectionArgs);
				break;
			case MAINTENANCE_ITEM:
				cursor = database.query(MaintenanceItemEntry.TABLE_NAME,
						projection,
						selection,
						selectionArgs,
						null,
						null,
						sortOrder);
				break;
			case CUSTOM_MAINTENANCE_ITEM:
				cursor = database.query(CustomMaintenanceItemEntry.TABLE_NAME,
						projection,
						selection,
						selectionArgs,
						null,
						null,
						sortOrder);
				break;
			default:
				throw new IllegalArgumentException("Cannot query unknown URI " + uri);
		}

		// Set notification URI on the Cursor,
		// so we know what content URI the Cursor was created for.
		// If the data at this URI changes, then we know we need to update the Cursor.
		if (getContext() != null) {
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
		}

		// Return the cursor
		return cursor;
	}

	@Nullable
	@Override
	public String getType(@NonNull Uri uri) {
		return null;
	}

	@Nullable
	@Override
	public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
		final int match = sUriMatcher.match(uri);

		if (values == null) {
			throw new IllegalArgumentException("NO VALUES PROVIDED: " + uri);
		}

		switch (match) {
			case USER_VEHICLE:
				return insertUserVehicle(uri, values);
			case ODOMETER:
				return insertOdometer(uri, values);
			case MAINTENANCE:
				return insertMaintenance(uri, values);
			case MAINTENANCE_DETAILS:
				return insertMaintenanceDetails(uri, values);
			case MAINTENANCE_ITEM:
				return insertMaintenanceItem(uri, values);
			default:
				throw new IllegalArgumentException("Insertion is not supported for " + uri);
		}
	}

	@Override
	public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
		final int match = sUriMatcher.match(uri);
		// Track the number of rows that were deleted
		int rowsDeleted;

		switch (match) {
			case USER_VEHICLE_ID:
				// Delete a single row given by the ID in the URI
				rowsDeleted = deleteUserVehicle(uri);
				break;
			case ODOMETER_ID:
				rowsDeleted = deleteOdometer(uri);
				break;
			case MAINTENANCE_ID:
				rowsDeleted = deleteMaintenance(uri);
				break;
			default:
				throw new IllegalArgumentException("Deletion is not supported for " + uri);
		}
		return rowsDeleted;
	}

	@Override
	public int update(@NonNull Uri uri, @Nullable ContentValues values,
					  @Nullable String selection, @Nullable String[] selectionArgs) {
		final int match = sUriMatcher.match(uri);

		if (values == null) {
			throw new IllegalArgumentException("NO VALUES PROVIDED: " + uri);
		}

		switch (match) {
			case USER_VEHICLE_ID:
				selection = UserVehicleEntry._ID + "=?";
				selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
				return updateUserVehicle(uri, values, selection, selectionArgs);
			case ODOMETER_ID:
				selection = OdometerEntry._ID + "=?";
				selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
				return updateOdometer(values, selection, selectionArgs);
			default:
				throw new IllegalArgumentException("Deletion is not supported for " + uri);
		}
	}

	private Uri insertUserVehicle(Uri uri, ContentValues values) {
		String regNo = values.getAsString(UserVehicleEntry.COLUMN_REG_NO);
		String brand = values.getAsString(UserVehicleEntry.COLUMN_BRAND);
		String model = values.getAsString(UserVehicleEntry.COLUMN_MODEL);
		String variant = values.getAsString(UserVehicleEntry.COLUMN_VARIANT);
		int usage = values.getAsInteger(UserVehicleEntry.COLUMN_USAGE);

		if (!isUserVehicleDataValid(regNo, brand, model, variant, usage)) {
			Log.e(LOG_TAG, "Failed to insert row for " + uri +
					". One or more columns not provided value.");
			return null;
		}

		SQLiteDatabase database = _DbHelper.getWritableDatabase();

		long id = database.insert(UserVehicleEntry.TABLE_NAME,
				null, values);

		// If the ID is -1, then the insertion failed. Log an error and return null.
		if (id == -1) {
			Log.e(LOG_TAG, "Failed to insert row for " + uri);
			return null;
		}

		if (getContext() != null) {
			notifyVehicleChanged(getContext());
		}

		return ContentUris.withAppendedId(uri, id);
	}

	private int updateUserVehicle(Uri uri, ContentValues values,
								  String selection, String[] selectionArgs) {
		if (values.size() == 0
				|| !values.containsKey(UserVehicleEntry.COLUMN_REG_NO)
				|| !values.containsKey(UserVehicleEntry.COLUMN_BRAND)
				|| !values.containsKey(UserVehicleEntry.COLUMN_MODEL)
				|| !values.containsKey(UserVehicleEntry.COLUMN_VARIANT)) {
			return 0;
		}
		String regNo = values.getAsString(UserVehicleEntry.COLUMN_REG_NO);
		String brand = values.getAsString(UserVehicleEntry.COLUMN_BRAND);
		String model = values.getAsString(UserVehicleEntry.COLUMN_MODEL);
		String variant = values.getAsString(UserVehicleEntry.COLUMN_VARIANT);
		int usage = values.getAsInteger(UserVehicleEntry.COLUMN_USAGE);

		if (!isUserVehicleDataValid(regNo, brand, model, variant, usage)) {
			throw new IllegalArgumentException("Failed to update row for "
					+ uri + ". One or more columns not provided value.");
		}

		SQLiteDatabase database = _DbHelper.getWritableDatabase();

		int rowsUpdated = database.update(UserVehicleEntry.TABLE_NAME,
				values, selection, selectionArgs);

		if (rowsUpdated != 0 && getContext() != null) {
//			getContext().getContentResolver().notifyChange(uri, null);
			notifyVehicleChanged(getContext());
		}
		// Return the number of rows updated
		return rowsUpdated;
	}

	private int deleteUserVehicle(Uri uri) {
		int rowsDeleted = deleteById(uri, UserVehicleEntry._ID, UserVehicleEntry.TABLE_NAME);
		if (rowsDeleted != 0 && getContext() != null) {
//			getContext().getContentResolver().notifyChange(uri, null);
			notifyVehicleChanged(getContext());
		}
		return rowsDeleted;
	}

	private boolean isUserVehicleDataValid(String regNo, String brand, String model,
										   String variant, int usage) {
		return !TextUtils.isEmpty(regNo)
				&& !TextUtils.isEmpty(brand)
				&& !TextUtils.isEmpty(model)
				&& !TextUtils.isEmpty(variant)
				&& (usage == UserVehicleEntry.USAGE_NORMAL
				|| usage == UserVehicleEntry.USAGE_SEVERE);
	}

	private Uri insertOdometer(Uri uri, ContentValues values) {
		SQLiteDatabase database = _DbHelper.getWritableDatabase();

		long id = database.insert(OdometerEntry.TABLE_NAME, null, values);

		if (id == -1) {
			Log.e(LOG_TAG, "Failed to insert row for " + uri);
			return null;
		}

		if (getContext() != null) {
			notifyVehicleChanged(getContext());
		}

		return ContentUris.withAppendedId(OdometerEntry.CONTENT_URI, id);
	}

	private int updateOdometer(ContentValues values,
							   String selection, String[] selectionArgs) {
		if (values.size() == 0
				|| !values.containsKey(OdometerEntry.COLUMN_VEHICLE)
				|| !values.containsKey(OdometerEntry.COLUMN_DATE)
				|| !values.containsKey(OdometerEntry.COLUMN_DISTANCE)) {
			return 0;
		}

		SQLiteDatabase database = _DbHelper.getWritableDatabase();
		int rowsUpdated = database.update(OdometerEntry.TABLE_NAME,
				values, selection, selectionArgs);

		if (rowsUpdated != 0 && getContext() != null) {
			notifyVehicleChanged(getContext());
		}
		// Return the number of rows updated
		return rowsUpdated;
	}

	private int deleteOdometer(Uri uri) {
		int rowsDeleted = deleteById(uri, OdometerEntry._ID, OdometerEntry.TABLE_NAME);

		if (rowsDeleted != 0 && getContext() != null) {
			notifyVehicleChanged(getContext());
		}

		return rowsDeleted;
	}

	private Uri insertMaintenance(Uri uri, ContentValues values) {
		SQLiteDatabase database = _DbHelper.getWritableDatabase();

		long id = database.insert(MaintenanceEntry.TABLE_NAME, null, values);

		if (id == -1) {
			Log.e(LOG_TAG, "Failed to insert row for " + uri);
			return null;
		}

		if (getContext() != null) {
			notifyVehicleChanged(getContext());
		}

		return ContentUris.withAppendedId(MaintenanceEntry.CONTENT_URI, id);
	}

	private int deleteMaintenance(Uri uri) {
		int rowsDeleted = deleteById(uri, MaintenanceEntry._ID, MaintenanceEntry.TABLE_NAME);
		if (rowsDeleted != 0 && getContext() != null) {
			notifyVehicleChanged(getContext());
		}
		return rowsDeleted;
	}

	private Uri insertMaintenanceDetails(Uri uri, ContentValues values) {
		SQLiteDatabase database = _DbHelper.getWritableDatabase();

		long id = database.insert(MaintenanceDetailsEntry.TABLE_NAME,
				null, values);

		if (id == -1) {
			Log.e(LOG_TAG,
					"Failed to insert row for insertMaintenanceDetails: " + uri);
			return null;
		}
		if (getContext() != null) {
			notifyVehicleChanged(getContext());
		}

		return ContentUris.withAppendedId(MaintenanceDetailsEntry.CONTENT_URI, id);
	}

	private Uri insertMaintenanceItem(Uri uri, ContentValues values) {
		SQLiteDatabase database = _DbHelper.getWritableDatabase();

		String itemName = values.getAsString(MaintenanceItemEntry.COLUMN_ITEM);

		// max length of item name
		if (itemName.length() > MaintenanceItemEntry.ITEM_NAME_MAX_LENGTH) {
			values.remove(MaintenanceItemEntry.COLUMN_ITEM);
			values.put(MaintenanceItemEntry.COLUMN_ITEM,
					itemName.substring(0,
							MaintenanceItemEntry.ITEM_NAME_MAX_LENGTH - 1));
		}

		long id = database.insert(MaintenanceItemEntry.TABLE_NAME,
				null, values);

		if (id == -1) {
			Log.e(LOG_TAG, "Failed to insert row for " + uri);
			return null;
		}

		if (getContext() != null) {
			notifyVehicleChanged(getContext());
		}

		return ContentUris.withAppendedId(MaintenanceItemEntry.CONTENT_URI, id);
	}

	private int deleteById(Uri uri, String colId, String tableName) {
		SQLiteDatabase database = _DbHelper.getWritableDatabase();

		// Delete a single row given by the ID in the URI
		String selection = colId + "=?";
		String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
		return database.delete(tableName, selection, selectionArgs);
	}

	private Cursor selectOdometerWithVehicle(SQLiteDatabase database) {
		return database.rawQuery("SELECT "
						+ OdometerEntry.TABLE_NAME + "." + OdometerEntry._ID + ", "
						+ OdometerEntry.TABLE_NAME + "." + OdometerEntry.COLUMN_VEHICLE + ", "
						+ OdometerEntry.TABLE_NAME + "." + OdometerEntry.COLUMN_DATE + ", "
						+ OdometerEntry.TABLE_NAME + "." + OdometerEntry.COLUMN_DISTANCE + ", "
						+ UserVehicleEntry.TABLE_NAME + "." + UserVehicleEntry.COLUMN_REG_NO
						+ " FROM " + OdometerEntry.TABLE_NAME + " JOIN "
						+ UserVehicleEntry.TABLE_NAME + " ON "
						+ OdometerEntry.TABLE_NAME + "." + OdometerEntry.COLUMN_VEHICLE
						+ " = " + UserVehicleEntry.TABLE_NAME + "." + UserVehicleEntry._ID
						+ " ORDER BY " + OdometerEntry.TABLE_NAME + "."
						+ OdometerEntry.COLUMN_DATE + " DESC;",
				null);
	}

	private Cursor selectUpcomingMaintenance(SQLiteDatabase database) {
		return database.rawQuery(UpcomingMaintenanceEntry.SELECT_QUERY, null);
	}

	public static void notifyVehicleChanged(Context context) {
		context.getContentResolver().notifyChange(UserVehicleEntry.CONTENT_URI, null);
		context.getContentResolver().notifyChange(OdometerEntry.CONTENT_URI, null);
		context.getContentResolver().notifyChange(MaintenanceDetailsEntry.CONTENT_URI, null);
		context.getContentResolver().notifyChange(UpcomingMaintenanceEntry.CONTENT_URI, null);
		context.getContentResolver().notifyChange(MaintenanceEntry.CONTENT_URI, null);
	}
//	private long utcNow() {
//		return new Date().getTime();
//	}
}
