package com.incupe.vewec.cursoradapter;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cursoradapter.widget.CursorAdapter;

import com.incupe.vewec.R;
import com.incupe.vewec.data.MaintenanceContract.MaintenanceEntry;
import com.incupe.vewec.data.MaintenanceDetailsContract.MaintenanceDetailsEntry;
import com.incupe.vewec.data.MaintenanceItemContract.MaintenanceItemEntry;
import com.incupe.vewec.data.UserVehicleContract.UserVehicleEntry;
import com.incupe.vewec.objects.UserVehicle;
import com.incupe.vewec.utilities.DateUtilities;
import com.incupe.vewec.utilities.Misc;

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

		totalPrice += setupInspectReplaceItems(context,
				maintenanceId,
				MaintenanceItemEntry.REPLACE_VALUE,
				llReplace,
				txtReplace);
		totalPrice += setupInspectReplaceItems(context,
				maintenanceId,
				MaintenanceItemEntry.INSPECT_VALUE,
				llInspect,
				txtInspect);

		txtTotalPrice.setText(String.format(Locale.getDefault(), "%.2f", totalPrice));
	}

	private double setupInspectReplaceItems(Context context, int maintenanceId, int inspectReplace,
											LinearLayout linearLayout, TextView textView) {
		double totalPrice = 0;
		Cursor cursor = context.getContentResolver().query(
				ContentUris.withAppendedId(MaintenanceDetailsEntry
						.CONTENT_URI_MAINTENANCE, maintenanceId),
				null,
				null,
				new String[]{String.valueOf(maintenanceId),
						String.valueOf(inspectReplace)},
				null);

		if (cursor == null) {
			return totalPrice;
		}

		if (cursor.getCount() == 0) {
			textView.setVisibility(View.GONE);
		} else {
			textView.setVisibility(View.VISIBLE);
		}

		while (cursor.moveToNext()) {
			View itemView = View.inflate(context, R.layout.template_maintenance_item, null);
			String itemName = cursor.getString(cursor
					.getColumnIndexOrThrow(MaintenanceItemEntry.COLUMN_ITEM));
			double itemPrice = cursor.getDouble(cursor
					.getColumnIndexOrThrow(MaintenanceDetailsEntry.COLUMN_PRICE));
			totalPrice += itemPrice;

			TextView txtItem = itemView.findViewById(R.id.txt_item);
			txtItem.setText(itemName);

			TextView txtPrice = itemView.findViewById(R.id.txt_price);
			String strPrice = context.getString(R.string.myr) + " "
					+ String.format(Locale.getDefault(), "%.2f", itemPrice);
			txtPrice.setText(strPrice);

			linearLayout.addView(itemView);
		}
		cursor.close();
		return totalPrice;
	}
}
