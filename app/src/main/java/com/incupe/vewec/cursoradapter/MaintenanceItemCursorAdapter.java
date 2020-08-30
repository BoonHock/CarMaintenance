package com.incupe.vewec.cursoradapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.incupe.vewec.R;
import com.incupe.vewec.data.MaintenanceItemContract.MaintenanceItemEntry;
import com.incupe.vewec.objects.MaintenanceItem;

public class MaintenanceItemCursorAdapter extends CursorAdapter {
	public MaintenanceItemCursorAdapter(Context context, Cursor c) {
		super(context, c, 0);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context)
				.inflate(R.layout.list_maintenance_item, parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		MaintenanceItem maintenanceItem = new MaintenanceItem(
				"",
				cursor.getString(cursor.getColumnIndexOrThrow(
						MaintenanceItemEntry.COLUMN_ITEM)),
				cursor.getInt(cursor.getColumnIndexOrThrow(
						MaintenanceItemEntry.COLUMN_INSPECT_REPLACE)),
				cursor.getInt(cursor.getColumnIndexOrThrow(
						MaintenanceItemEntry.COLUMN_FIRST_DISTANCE)),
				cursor.getInt(cursor.getColumnIndexOrThrow(
						MaintenanceItemEntry.COLUMN_DISTANCE_INTERVAL)),
				cursor.getInt(cursor.getColumnIndexOrThrow(
						MaintenanceItemEntry.COLUMN_FIRST_DURATION)),
				cursor.getInt(cursor.getColumnIndexOrThrow(
						MaintenanceItemEntry.COLUMN_DURATION_INTERVAL))
		);
		TextView txtItemName = view.findViewById(R.id.txt_item_name);
		TextView txtInspectReplace = view.findViewById(R.id.txt_inspect_replace);
		TextView txtDistance = view.findViewById(R.id.txt_distance);
		TextView txtDuration = view.findViewById(R.id.txt_duration);

		txtItemName.setText(maintenanceItem.getItem());
		if (maintenanceItem.getInspect_replace() == MaintenanceItemEntry.REPLACE_VALUE) {
			txtInspectReplace.setText(context.getString(R.string.replace));
		} else {
			txtInspectReplace.setText(context.getString(R.string.inspect));
		}

		if (maintenanceItem.getFirst_distance() == 0 ||
				maintenanceItem.getFirst_distance() == maintenanceItem.getDistance_interval()) {
			txtDistance.setText(context.getString(
					R.string.maintenance_item_interval,
					maintenanceItem.getDistance_interval(),
					context.getString(R.string.kilometer)
					)
			);
		} else {
			txtDistance.setText(context.getString(
					R.string.maintenance_item_interval_with_first,
					maintenanceItem.getFirst_distance(),
					context.getString(R.string.kilometer),
					maintenanceItem.getDistance_interval()
					)
			);
		}
		if (maintenanceItem.getFirst_duration() == 0 ||
				maintenanceItem.getFirst_duration() == maintenanceItem.getDuration_interval()) {
			txtDuration.setText(context.getString(
					R.string.maintenance_item_interval,
					maintenanceItem.getDuration_interval(),
					context.getString(R.string.months)
					)
			);
		} else {
			txtDuration.setText(context.getString(
					R.string.maintenance_item_interval_with_first,
					maintenanceItem.getFirst_duration(),
					context.getString(R.string.months),
					maintenanceItem.getDuration_interval()
					)
			);
		}
	}
}
