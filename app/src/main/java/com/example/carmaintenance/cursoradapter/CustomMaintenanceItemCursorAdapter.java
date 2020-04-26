package com.example.carmaintenance.cursoradapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cursoradapter.widget.CursorAdapter;

import com.example.carmaintenance.R;
import com.example.carmaintenance.data.CustomMaintenanceItemContract.CustomMaintenanceItemEntry;

public class CustomMaintenanceItemCursorAdapter extends CursorAdapter {
	public CustomMaintenanceItemCursorAdapter(Context context, Cursor c) {
		super(context, c, 0);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context)
				.inflate(R.layout.list_custom_maintenance_item,
						parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView txtItem = view.findViewById(R.id.txt_item);
		TextView txtDistance = view.findViewById(R.id.txt_distance);
		TextView txtDuration = view.findViewById(R.id.txt_duration);

		int intDistanceInterval = cursor.getInt(cursor.getColumnIndexOrThrow(
				CustomMaintenanceItemEntry.COLUMN_DISTANCE_INTERVAL));
		int intDurationInterval = cursor.getInt(cursor.getColumnIndexOrThrow(
				CustomMaintenanceItemEntry.COLUMN_DURATION_INTERVAL));
		String strDistanceInterval =
				context.getString(R.string.distance_interval_label) + ": ";
		String strDurationInterval =
				context.getString(R.string.duration_interval_label) + ": ";

		txtItem.setText(cursor.getString(cursor
				.getColumnIndexOrThrow(CustomMaintenanceItemEntry.COLUMN_ITEM)));

		if (intDistanceInterval != 0) {
			strDistanceInterval += String.valueOf(intDistanceInterval)
					+ " " + context.getString(R.string.kilometer);
			txtDistance.setText(strDistanceInterval);
		}
		if (intDurationInterval != 0) {
			strDurationInterval += String.valueOf(intDurationInterval)
					+ " " + context.getString(R.string.months);
			txtDistance.setText(strDurationInterval);
		}
	}
}
