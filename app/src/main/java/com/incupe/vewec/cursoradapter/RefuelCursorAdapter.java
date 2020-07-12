package com.incupe.vewec.cursoradapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.incupe.vewec.R;
import com.incupe.vewec.data.RefuelContract.RefuelEntry;
import com.incupe.vewec.data.UserVehicleContract.UserVehicleEntry;
import com.incupe.vewec.utilities.DateUtilities;

import java.util.Date;
import java.util.Locale;

public class RefuelCursorAdapter extends CursorAdapter {
	public RefuelCursorAdapter(Context context, Cursor c) {
		super(context, c, 0);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context)
				.inflate(R.layout.list_refuel, parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		String regNo = cursor.getString(cursor
				.getColumnIndexOrThrow(UserVehicleEntry.COLUMN_REG_NO));
		Date date = new Date(cursor.getLong(cursor
				.getColumnIndexOrThrow(RefuelEntry.COLUMN_DATE)));
		double price = cursor.getDouble(cursor
				.getColumnIndexOrThrow(RefuelEntry.COLUMN_PRICE));
		double volume = cursor.getDouble(cursor
				.getColumnIndexOrThrow(RefuelEntry.COLUMN_VOLUME));

		TextView txtRegNo = view.findViewById(R.id.txt_reg_no);
		TextView txtDate = view.findViewById(R.id.txt_date);
		TextView txtPrice = view.findViewById(R.id.txt_price);
		TextView txtVolume = view.findViewById(R.id.txt_volume);

		String strPrice = context.getString(R.string.myr) + " "
				+ String.format(Locale.getDefault(), "%.2f", price);
		String strVolume = String.format(Locale.getDefault(), "%.3f", volume)
				+ " " + context.getString(R.string.litre);

		txtRegNo.setText(regNo);
		txtDate.setText(DateUtilities.dateToStringDate(date));
		txtPrice.setText(strPrice);
		txtVolume.setText(strVolume);
	}
}
