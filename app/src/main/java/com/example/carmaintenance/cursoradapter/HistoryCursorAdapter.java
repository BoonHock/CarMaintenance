package com.example.carmaintenance.cursoradapter;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cursoradapter.widget.CursorAdapter;

import com.example.carmaintenance.R;
import com.example.carmaintenance.data.MaintenanceContract.MaintenanceEntry;
import com.example.carmaintenance.data.MaintenanceDetailsContract.MaintenanceDetailsEntry;
import com.example.carmaintenance.data.MaintenanceItemContract.MaintenanceItemEntry;
import com.example.carmaintenance.data.UserVehicleContract.UserVehicleEntry;
import com.example.carmaintenance.objects.UserVehicle;
import com.example.carmaintenance.utilities.DateUtilities;
import com.example.carmaintenance.utilities.Misc;

import java.util.Date;
import java.util.Locale;

public class HistoryCursorAdapter extends CursorAdapter {
	public HistoryCursorAdapter(Context context, Cursor c) {
		super(context, c, 0);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context)
				.inflate(R.layout.list_history, parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView txtDate = view.findViewById(R.id.txt_date);
		TextView txtRegNo = view.findViewById(R.id.txt_reg_no);
		TextView txtBrandModel = view.findViewById(R.id.txt_brand_model_variant);
		TextView txtOdometer = view.findViewById(R.id.txt_odometer);
		TextView txtInspect = view.findViewById(R.id.txt_inspect);
		TextView txtReplace = view.findViewById(R.id.txt_replace);
		LinearLayout llInspect = view.findViewById(R.id.ll_inspect_items);
		LinearLayout llReplace = view.findViewById(R.id.ll_replace_items);
		TextView txtTotalPrice = view.findViewById(R.id.txt_total_price);

		llInspect.removeAllViews();
		llReplace.removeAllViews();

		double totalPrice = 0;

		int maintenanceId = cursor.getInt(cursor
				.getColumnIndexOrThrow(MaintenanceEntry._ID));
		int vehicleId = cursor.getInt(cursor
				.getColumnIndexOrThrow(MaintenanceEntry.COLUMN_VEHICLE));
		Date date = new Date(cursor.getLong(cursor
				.getColumnIndexOrThrow(MaintenanceEntry.COLUMN_DATE)));
		int odometer = cursor.getInt(cursor
				.getColumnIndexOrThrow(MaintenanceEntry.COLUMN_ODOMETER));

		txtDate.setText(DateUtilities.dateToStringDate(date));
		txtOdometer.setText(Misc.getDistanceWithUnit(odometer, context));

		Cursor userVehicleCursor = context.getContentResolver().query(
				UserVehicleEntry.CONTENT_URI,
				UserVehicleEntry.FULL_PROJECTION,
				UserVehicleEntry._ID + "=?",
				new String[]{String.valueOf(vehicleId)},
				null);

		if (userVehicleCursor != null) {
			if (userVehicleCursor.moveToFirst()) {
				UserVehicle userVehicle = new UserVehicle(userVehicleCursor);
				txtRegNo.setText(userVehicle.get_regNo());
				txtBrandModel.setText(userVehicle.get_brandModelVariant());
			}
			userVehicleCursor.close();
		}

		Cursor maintenanceDetailsCursor = context.getContentResolver().query(
				ContentUris.withAppendedId(MaintenanceDetailsEntry
						.CONTENT_URI_MAINTENANCE, maintenanceId),
				null,
				null,
				new String[]{String.valueOf(maintenanceId)},
				null);

		if (maintenanceDetailsCursor != null) {
			while (maintenanceDetailsCursor.moveToNext()) {
				View itemView = LayoutInflater.from(context)
						.inflate(R.layout.template_maintenance_item, null);
				String itemName = maintenanceDetailsCursor.getString(maintenanceDetailsCursor
						.getColumnIndexOrThrow(MaintenanceItemEntry.COLUMN_ITEM));
				double itemPrice = maintenanceDetailsCursor.getDouble(maintenanceDetailsCursor
						.getColumnIndexOrThrow(MaintenanceDetailsEntry.COLUMN_PRICE));
				totalPrice += itemPrice;

				TextView txtItem = itemView.findViewById(R.id.txt_item);
				txtItem.setText(itemName);

				TextView txtPrice = itemView.findViewById(R.id.txt_price);
				String strPrice = context.getString(R.string.myr) + " "
						+ String.format(Locale.getDefault(), "%.2f", itemPrice);
				txtPrice.setText(strPrice);

				switch (maintenanceDetailsCursor.getInt(maintenanceDetailsCursor
						.getColumnIndexOrThrow(MaintenanceItemEntry.COLUMN_INSPECT_REPLACE))) {
					case MaintenanceItemEntry.INSPECT_VALUE:
						llInspect.addView(itemView);
						break;
					case MaintenanceItemEntry.REPLACE_VALUE:
						llReplace.addView(itemView);
						break;
				}

			}
			maintenanceDetailsCursor.close();
		}

		txtTotalPrice.setText(String.format(Locale.getDefault(), "%.2f", totalPrice));

		if (llInspect.getChildCount() == 0) {
			txtInspect.setVisibility(View.GONE);
		} else {
			txtInspect.setVisibility(View.VISIBLE);
		}
		if (llReplace.getChildCount() == 0) {
			txtReplace.setVisibility(View.GONE);
		} else {
			txtReplace.setVisibility(View.VISIBLE);
		}
	}
}
