package com.example.carmaintenance.cursoradapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.carmaintenance.R;
import com.example.carmaintenance.data.OdometerContract.OdometerEntry;
import com.example.carmaintenance.data.UserVehicleContract.UserVehicleEntry;
import com.example.carmaintenance.utilities.DateUtilities;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

public class OdometerCursorAdapter extends CursorAdapter {
	public OdometerCursorAdapter(Context context, Cursor c) {
		super(context, c, 0);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context)
				.inflate(R.layout.list_odometer, parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		String regNo = cursor.getString(cursor
				.getColumnIndexOrThrow(UserVehicleEntry.COLUMN_REG_NO));
		Date date = new Date(cursor.getLong(cursor
				.getColumnIndexOrThrow(OdometerEntry.COLUMN_DATE)));
		int distance = cursor.getInt(cursor
				.getColumnIndexOrThrow(OdometerEntry.COLUMN_DISTANCE));

		TextView txtRegNo = view.findViewById(R.id.txt_reg_no);
		TextView txtDate = view.findViewById(R.id.txt_date);
		TextView txtDistance = view.findViewById(R.id.txt_distance);

		txtRegNo.setText(regNo);
		txtDate.setText(DateUtilities.dateToStringDate(date));
		txtDistance.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(distance));
	}
}
