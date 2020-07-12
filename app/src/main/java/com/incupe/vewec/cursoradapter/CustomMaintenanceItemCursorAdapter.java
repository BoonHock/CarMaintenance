package com.incupe.vewec.cursoradapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cursoradapter.widget.CursorAdapter;

import com.incupe.vewec.R;
import com.incupe.vewec.data.CustomMaintenanceItemContract.CustomMaintenanceItemEntry;

import java.util.Locale;

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
		TextView txtInspectReplace = view.findViewById(R.id.txt_inspect_replace);
		TextView txtSeparator = view.findViewById(R.id.txt_separator);
		TextView txtDistance = view.findViewById(R.id.txt_value);
		TextView txtDuration = view.findViewById(R.id.txt_duration);

		int intDistanceInterval = cursor.getInt(cursor.getColumnIndexOrThrow(
				CustomMaintenanceItemEntry.COLUMN_DISTANCE_INTERVAL));
		int intDurationInterval = cursor.getInt(cursor.getColumnIndexOrThrow(
				CustomMaintenanceItemEntry.COLUMN_DURATION_INTERVAL));

		txtItem.setText(cursor.getString(cursor
				.getColumnIndexOrThrow(CustomMaintenanceItemEntry.COLUMN_ITEM)));
		txtSeparator.setVisibility(View.GONE);

		switch (cursor.getInt(cursor.getColumnIndexOrThrow(CustomMaintenanceItemEntry
				.COLUMN_INSPECT_REPLACE))) {
			case CustomMaintenanceItemEntry.INSPECT_VALUE:
				txtInspectReplace.setText(context.getString(R.string.inspect));
				break;
			case CustomMaintenanceItemEntry.REPLACE_VALUE:
				txtInspectReplace.setText(context.getString(R.string.replace));
				break;
		}

		if (intDistanceInterval != 0) {
			String interval = String.format(Locale.getDefault(), "%,d",
					intDistanceInterval) + " " + context.getString(R.string.kilometer);
			txtDistance.setText(interval);
		}
		if (intDurationInterval != 0) {
			String interval = intDurationInterval + " " + context.getString(R.string.months);
			txtDuration.setText(interval);
		}
		if (intDistanceInterval != 0 && intDurationInterval != 0) {
			txtSeparator.setVisibility(View.VISIBLE);
		}
	}
}
