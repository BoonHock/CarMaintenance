package com.incupe.vewec.data;

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

import com.incupe.vewec.data.CustomMaintenanceItemContract.CustomMaintenanceItemEntry;
import com.incupe.vewec.data.MaintenanceContract.MaintenanceEntry;
import com.incupe.vewec.data.MaintenanceDetailsContract.MaintenanceDetailsEntry;
import com.incupe.vewec.data.MaintenanceItemContract.MaintenanceItemEntry;
import com.incupe.vewec.data.OdometerContract.OdometerEntry;
import com.incupe.vewec.data.RefuelContract.RefuelEntry;
import com.incupe.vewec.data.UserVehicleContract.UserVehicleEntry;

public class VehicleMaintenanceProvider extends ContentProvider {
	/**
	 * Tag for the log messages
	 */
	public static final String LOG_TAG = VehicleMaintenanceProvider.class.getSimpleName();

	/**
	 * URI matcher code for the content URI for sql table
	 */
	private static final int USER_VEHICLE = 1;
	private static final int USER_VEHICLE_ID = 2;

	private static final int ODOMETER = 10;
	private static final int ODOMETER_ID = 11;
	private static final int ODOMETER_VEHICLE = 12;

	private static final int MAINTENANCE = 30;
	private static final int MAINTENANCE_ID = 31;

	private static final int MAINTENANCE_DETAILS = 40;
	private static final int MAINTENANCE_DETAILS_ID = 41;
	private static final int MAINTENANCE_DETAILS_MAINTENANCE_ID = 42;
	private static final int LATEST_MAINTENANCE_DETAILS_BY_ITEM = 43;

	private static final int MAINTENANCE_ITEM = 50;
	private static final int MAINTENANCE_ITEM_ID = 51;

	private static final int CUSTOM_MAINTENANCE_ITEM = 60;
	private static final int CUSTOM_MAINTENANCE_ITEM_ID = 61;

	private static final int REFUEL = 70;
	private static final int REFUEL_ID = 71;
	private static final int REFUEL_VEHICLE = 72;

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
				com.incupe.vewec.data.MaintenanceItemContract.PATH_MAINTENANCE_ITEM,
				MAINTENANCE_ITEM);
		sUriMatcher.addURI(APP_MASTER_CONTRACT.CONTENT_AUTHORITY,
				com.incupe.vewec.data.MaintenanceItemContract.PATH_MAINTENANCE_ITEM + "/#",
				MAINTENANCE_ITEM_ID);

		sUriMatcher.addURI(APP_MASTER_CONTRACT.CONTENT_AUTHORITY,
				CustomMaintenanceItemContract.PATH_CUSTOM_MAINTENANCE_ITEM,
				CUSTOM_MAINTENANCE_ITEM);
		sUriMatcher.addURI(APP_MASTER_CONTRACT.CONTENT_AUTHORITY,
				CustomMaintenanceItemContract.PATH_CUSTOM_MAINTENANCE_ITEM + "/#",
				CUSTOM_MAINTENANCE_ITEM_ID);

		sUriMatcher.addURI(APP_MASTER_CONTRACT.CONTENT_AUTHORITY,
				RefuelContract.PATH_REFUEL, REFUEL);
		sUriMatcher.addURI(APP_MASTER_CONTRACT.CONTENT_AUTHORITY,
				RefuelContract.PATH_REFUEL + "/#", REFUEL_ID);
		sUriMatcher.addURI(APP_MASTER_CONTRACT.CONTENT_AUTHORITY,
				RefuelContract.PATH_REFUEL_VEHICLE, REFUEL_VEHICLE);
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
			case REFUEL_ID:
				selection = RefuelEntry._ID + "=?";
				selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
				cursor = database.query(RefuelEntry.TABLE_NAME,
						projection,
						selection,
						selectionArgs,
						null,
						null,
						sortOrder);
				break;
			case REFUEL_VEHICLE:
				cursor = selectRefuelWithVehicle(database);
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
			case CUSTOM_MAINTENANCE_ITEM:
				return insertCustomMaintenanceItem(uri, values);
			case REFUEL:
				return insertRefuel(uri, values);
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
			case CUSTOM_MAINTENANCE_ITEM_ID:
				rowsDeleted = deleteCustomMaintenanceItem(uri);
				break;
			case REFUEL_ID:
				rowsDeleted = deleteRefuel(uri);
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
			case CUSTOM_MAINTENANCE_ITEM_ID:
				selection = CustomMaintenanceItemEntry._ID + "=?";
				selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
				return updateCustomMaintenanceItem(values, selection, selectionArgs);
			case REFUEL_ID:
				selection = RefuelEntry._ID + "=?";
				selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
				return updateRefuel(values, selection, selectionArgs);
			default:
				throw new IllegalArgumentException("Update not supported for " + uri);
		}
	}

	private boolean userVehicleDataNotValid(String regNo, String brand, String model,
											String variant, int usage) {
		return TextUtils.isEmpty(regNo)
				|| TextUtils.isEmpty(brand)
				|| TextUtils.isEmpty(model)
				|| TextUtils.isEmpty(variant)
				|| (usage != UserVehicleEntry.USAGE_NORMAL
				&& usage != UserVehicleEntry.USAGE_SEVERE);
	}

	private Uri insertUserVehicle(Uri uri, ContentValues values) {
		String regNo = values.getAsString(UserVehicleEntry.COLUMN_REG_NO);
		String brand = values.getAsString(UserVehicleEntry.COLUMN_BRAND);
		String model = values.getAsString(UserVehicleEntry.COLUMN_MODEL);
		String variant = values.getAsString(UserVehicleEntry.COLUMN_VARIANT);
		int usage = values.getAsInteger(UserVehicleEntry.COLUMN_USAGE);

		if (userVehicleDataNotValid(regNo, brand, model, variant, usage)) {
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
			notifyUris(getContext(), UserVehicleEntry.CONTENT_URI);
		}

		return ContentUris.withAppendedId(uri, id);
	}

	private Uri insertOdometer(Uri uri, ContentValues values) {
		SQLiteDatabase database = _DbHelper.getWritableDatabase();

		long id = database.insert(OdometerEntry.TABLE_NAME, null, values);

		if (id == -1) {
			Log.e(LOG_TAG, "Failed to insert row for " + uri);
			return null;
		}

		if (getContext() != null) {
			notifyUris(getContext(),
					UserVehicleEntry.CONTENT_URI,
					OdometerEntry.CONTENT_URI,
					MaintenanceEntry.CONTENT_URI);
		}

		return ContentUris.withAppendedId(OdometerEntry.CONTENT_URI, id);
	}

	private Uri insertMaintenance(Uri uri, ContentValues values) {
		SQLiteDatabase database = _DbHelper.getWritableDatabase();

		long id = database.insert(MaintenanceEntry.TABLE_NAME, null, values);

		if (id == -1) {
			Log.e(LOG_TAG, "Failed to insert row for " + uri);
			return null;
		}

		if (getContext() != null) {
			notifyUris(getContext(),
					UserVehicleEntry.CONTENT_URI,
					OdometerEntry.CONTENT_URI,
					MaintenanceEntry.CONTENT_URI);
		}

		return ContentUris.withAppendedId(MaintenanceEntry.CONTENT_URI, id);
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
			notifyUris(getContext(),
					UserVehicleEntry.CONTENT_URI,
					OdometerEntry.CONTENT_URI,
					MaintenanceEntry.CONTENT_URI);
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
			notifyUris(getContext(),
					UserVehicleEntry.CONTENT_URI,
					OdometerEntry.CONTENT_URI,
					MaintenanceEntry.CONTENT_URI);
		}

		return ContentUris.withAppendedId(MaintenanceItemEntry.CONTENT_URI, id);
	}

	private Uri insertCustomMaintenanceItem(Uri uri, ContentValues values) {
		SQLiteDatabase database = _DbHelper.getWritableDatabase();
		String itemName = values.getAsString(CustomMaintenanceItemEntry.COLUMN_ITEM);

		if (itemName.length() > CustomMaintenanceItemEntry.ITEM_NAME_MAX_LENGTH) {
			values.remove(CustomMaintenanceItemEntry.COLUMN_ITEM);
			values.put(CustomMaintenanceItemEntry.COLUMN_ITEM,
					itemName.substring(0,
							CustomMaintenanceItemEntry.ITEM_NAME_MAX_LENGTH - 1));
		}
		long id = database.insert(CustomMaintenanceItemEntry.TABLE_NAME,
				null, values);
		if (id == -1) {
			Log.e(LOG_TAG, "Failed to insert row for " + uri);
			return null;
		}
		if (getContext() != null) {
			notifyUris(getContext(),
					UserVehicleEntry.CONTENT_URI,
					CustomMaintenanceItemEntry.CONTENT_URI);
		}
		return ContentUris.withAppendedId(CustomMaintenanceItemEntry.CONTENT_URI, id);
	}

	private Uri insertRefuel(Uri uri, ContentValues values) {
		SQLiteDatabase database = _DbHelper.getWritableDatabase();
		long id = database.insert(RefuelEntry.TABLE_NAME, null, values);

		if (id == -1) {
			Log.e(LOG_TAG,
					"Failed to insert row for insertRefuel: " + uri);
			return null;
		}
		if (getContext() != null) {
			notifyUris(getContext(), RefuelEntry.CONTENT_URI);
		}
		return ContentUris.withAppendedId(RefuelEntry.CONTENT_URI, id);
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

		if (rowsUpdated > 0 && getContext() != null) {
			notifyUris(getContext(),
					UserVehicleEntry.CONTENT_URI,
					OdometerEntry.CONTENT_URI,
					MaintenanceEntry.CONTENT_URI);
		}
		// Return the number of rows updated
		return rowsUpdated;
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

		if (userVehicleDataNotValid(regNo, brand, model, variant, usage)) {
			throw new IllegalArgumentException("Failed to update row for "
					+ uri + ". One or more columns not provided value.");
		}

		SQLiteDatabase database = _DbHelper.getWritableDatabase();

		int rowsUpdated = database.update(UserVehicleEntry.TABLE_NAME,
				values, selection, selectionArgs);

		if (rowsUpdated != 0 && getContext() != null) {
			notifyUris(getContext(),
					UserVehicleEntry.CONTENT_URI,
					OdometerEntry.CONTENT_URI,
					MaintenanceEntry.CONTENT_URI);
		}
		// Return the number of rows updated
		return rowsUpdated;
	}

	private int updateCustomMaintenanceItem(ContentValues values, String selection,
											String[] selectionArgs) {
		if (values.size() == 0
				|| !values.containsKey(CustomMaintenanceItemEntry.COLUMN_ITEM)
				|| !values.containsKey(CustomMaintenanceItemEntry.COLUMN_INSPECT_REPLACE)
				|| !values.containsKey(CustomMaintenanceItemEntry.COLUMN_DISTANCE_INTERVAL)
				|| !values.containsKey(CustomMaintenanceItemEntry.COLUMN_DURATION_INTERVAL)) {
			return 0;
		}

		SQLiteDatabase database = _DbHelper.getWritableDatabase();
		int rowsUpdated = database.update(CustomMaintenanceItemEntry.TABLE_NAME,
				values,
				selection,
				selectionArgs);

		if (rowsUpdated > 0 && getContext() != null) {
			notifyUris(getContext(),
					// will update upcoming page
					UserVehicleEntry.CONTENT_URI,
					// if user change item name, will need to update history there also
					MaintenanceEntry.CONTENT_URI,
					// duh
					CustomMaintenanceItemEntry.CONTENT_URI);
		}
		return rowsUpdated;
	}

	private int updateRefuel(ContentValues values, String selection,
							 String[] selectionArgs) {
		if (values.size() == 0
				|| !values.containsKey(RefuelEntry.COLUMN_VEHICLE)
				|| !values.containsKey(RefuelEntry.COLUMN_DATE)
				|| !values.containsKey(RefuelEntry.COLUMN_PRICE)
				|| !values.containsKey(RefuelEntry.COLUMN_VOLUME)
				|| !values.containsKey(RefuelEntry.COLUMN_ODOMETER)
				|| !values.containsKey(RefuelEntry.COLUMN_FUEL_TYPE)
				|| !values.containsKey(RefuelEntry.COLUMN_IS_FULL_TANK)) {
			return 0;
		}

		SQLiteDatabase database = _DbHelper.getWritableDatabase();
		int rowsUpdated = database.update(RefuelEntry.TABLE_NAME,
				values,
				selection,
				selectionArgs);

		if (rowsUpdated > 0 && getContext() != null) {
			notifyUris(getContext(), RefuelEntry.CONTENT_URI);
		}
		return rowsUpdated;
	}

	private int deleteUserVehicle(Uri uri) {
		int rowsDeleted = deleteById(uri, UserVehicleEntry._ID, UserVehicleEntry.TABLE_NAME);
		if (rowsDeleted != 0 && getContext() != null) {
			notifyUris(getContext(),
					UserVehicleEntry.CONTENT_URI,
					OdometerEntry.CONTENT_URI,
					MaintenanceEntry.CONTENT_URI);
		}
		return rowsDeleted;
	}

	private int deleteOdometer(Uri uri) {
		int rowsDeleted = deleteById(uri, OdometerEntry._ID, OdometerEntry.TABLE_NAME);

		if (rowsDeleted != 0 && getContext() != null) {
			notifyUris(getContext(),
					UserVehicleEntry.CONTENT_URI,
					OdometerEntry.CONTENT_URI,
					MaintenanceEntry.CONTENT_URI);
		}

		return rowsDeleted;
	}

	private int deleteMaintenance(Uri uri) {
		int rowsDeleted = deleteById(uri, MaintenanceEntry._ID, MaintenanceEntry.TABLE_NAME);
		if (rowsDeleted > 0 && getContext() != null) {
			notifyUris(getContext(),
					UserVehicleEntry.CONTENT_URI,
					OdometerEntry.CONTENT_URI,
					MaintenanceEntry.CONTENT_URI);
		}
		return rowsDeleted;
	}

	private int deleteRefuel(Uri uri) {
		int rowsDeleted = deleteById(uri, RefuelEntry._ID, RefuelEntry.TABLE_NAME);
		if (rowsDeleted != 0 && getContext() != null) {
			notifyUris(getContext(), RefuelEntry.CONTENT_URI);
		}
		return rowsDeleted;
	}

	private int deleteCustomMaintenanceItem(Uri uri) {
		int rowsDeleted = deleteById(uri, CustomMaintenanceItemEntry._ID,
				CustomMaintenanceItemEntry.TABLE_NAME);
		if (rowsDeleted > 0 && getContext() != null) {
			notifyUris(getContext(),
					// will update upcoming page
					UserVehicleEntry.CONTENT_URI,
					// if user delete item, will delete in history there too
					MaintenanceEntry.CONTENT_URI,
					// duh
					CustomMaintenanceItemEntry.CONTENT_URI);
		}
		return rowsDeleted;
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

	private Cursor selectRefuelWithVehicle(SQLiteDatabase database) {
		return database.rawQuery("SELECT "
						+ RefuelEntry.TABLE_NAME + "." + RefuelEntry._ID + ","
						+ RefuelEntry.TABLE_NAME + "." + RefuelEntry.COLUMN_VEHICLE + ","
						+ RefuelEntry.TABLE_NAME + "." + RefuelEntry.COLUMN_DATE + ","
						+ RefuelEntry.TABLE_NAME + "." + RefuelEntry.COLUMN_PRICE + ","
						+ RefuelEntry.TABLE_NAME + "." + RefuelEntry.COLUMN_VOLUME + ","
						+ RefuelEntry.TABLE_NAME + "." + RefuelEntry.COLUMN_ODOMETER + ","
						+ RefuelEntry.TABLE_NAME + "." + RefuelEntry.COLUMN_FUEL_TYPE + ","
						+ RefuelEntry.TABLE_NAME + "." + RefuelEntry.COLUMN_IS_FULL_TANK + ","
						+ UserVehicleEntry.TABLE_NAME + "." + UserVehicleEntry.COLUMN_REG_NO
						+ " FROM " + RefuelEntry.TABLE_NAME + " JOIN "
						+ UserVehicleEntry.TABLE_NAME + " ON "
						+ RefuelEntry.TABLE_NAME + "." + RefuelEntry.COLUMN_VEHICLE + "="
						+ UserVehicleEntry.TABLE_NAME + "." + UserVehicleEntry._ID
						+ " ORDER BY " + RefuelEntry.TABLE_NAME + "."
						+ RefuelEntry.COLUMN_DATE + " DESC;",
				null);
	}

	public static void notifyUris(Context context, Uri... uris) {
		for (Uri uri : uris) {
			context.getContentResolver().notifyChange(uri, null);
		}
	}
}
