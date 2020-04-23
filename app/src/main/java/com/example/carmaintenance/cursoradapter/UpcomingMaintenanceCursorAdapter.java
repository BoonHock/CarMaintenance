package com.example.carmaintenance.cursoradapter;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.carmaintenance.R;
import com.example.carmaintenance.data.FirebaseContract.FirebaseMaintenanceDetailsEntry;
import com.example.carmaintenance.data.UserVehicleContract.UserVehicleEntry;
import com.example.carmaintenance.objects.FirebaseObj;
import com.example.carmaintenance.objects.MaintenanceItem;
import com.example.carmaintenance.objects.UpcomingMaintenanceItem;
import com.example.carmaintenance.objects.VehicleTemplate;

import java.util.List;
import java.util.Locale;

public class UpcomingMaintenanceCursorAdapter extends CursorAdapter {
	public UpcomingMaintenanceCursorAdapter(Context context, Cursor c) {
		super(context, c, 0);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context)
				.inflate(R.layout.list_upcoming, parent, false);
	}

	@Override
	public void bindView(final View view, final Context context, Cursor cursor) {
		final int vehicleId = cursor.getInt(cursor
				.getColumnIndexOrThrow(UserVehicleEntry._ID));
		String regNo = cursor.getString(cursor
				.getColumnIndexOrThrow(UserVehicleEntry.COLUMN_REG_NO));
		String brand = cursor.getString(cursor
				.getColumnIndexOrThrow(UserVehicleEntry.COLUMN_BRAND));
		String model = cursor.getString(cursor
				.getColumnIndexOrThrow(UserVehicleEntry.COLUMN_MODEL));
		String variant = cursor.getString(cursor
				.getColumnIndexOrThrow(UserVehicleEntry.COLUMN_VARIANT));
		final int currentUsage = cursor.getInt(cursor
				.getColumnIndexOrThrow(UserVehicleEntry.COLUMN_USAGE));

		view.findViewById(R.id.txt_replace).setVisibility(View.GONE);
		view.findViewById(R.id.txt_inspect).setVisibility(View.GONE);

		TextView txtRegNo = view.findViewById(R.id.txt_reg_no);
		TextView txtBrandModel = view.findViewById(R.id.txt_brand_model);
		String brandModel = brand + " " + model + " " + variant;

		final String firebaseVehicleId = VehicleTemplate
				.getVehicleIdFromList(FirebaseObj._vehicleTemplates,
						brand,
						model,
						variant);

		txtRegNo.setText(regNo);
		txtBrandModel.setText(brandModel);

		FirebaseObj.runCallbackMaintenanceDetails(firebaseVehicleId, new FirebaseObj() {
			@Override
			public void callback() {
				displayUpcomingMaintenance(context, view,
						firebaseVehicleId, vehicleId, currentUsage);
			}
		});
	}

	private void displayUpcomingMaintenance(
			Context context, View view, String firebaseVehicleId,
			int vehicleId, int usage) {

		LinearLayout llInspect = view.findViewById(R.id.ll_inspect_items);
		LinearLayout llReplace = view.findViewById(R.id.ll_replace_items);

		TextView txtInspect = view.findViewById(R.id.txt_inspect);
		TextView txtReplace = view.findViewById(R.id.txt_replace);

		List<MaintenanceItem> maintenanceItems = FirebaseObj._maintenanceItems.get(firebaseVehicleId);

		llInspect.removeAllViews();
		llReplace.removeAllViews();

		if (maintenanceItems != null) {
			for (MaintenanceItem maintenanceItem : maintenanceItems) {
				UpcomingMaintenanceItem upcomingMaintenanceItem =
						new UpcomingMaintenanceItem(context, maintenanceItem, vehicleId);

				if (upcomingMaintenanceItem.getInspect_replace()
						== FirebaseMaintenanceDetailsEntry.INSPECT) {

					addMaintenanceItem(context, llInspect, upcomingMaintenanceItem);
				} else if (upcomingMaintenanceItem.getInspect_replace()
						== FirebaseMaintenanceDetailsEntry.REPLACE) {

					addMaintenanceItem(context, llReplace, upcomingMaintenanceItem);
				}
			}
		}
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

	private void addMaintenanceItem(Context context, LinearLayout llParent,
									UpcomingMaintenanceItem upcomingMaintenanceItem) {
		View view = LayoutInflater.from(context).inflate(R.layout.template_upcoming_item, null);
		LinearLayout llItem = view.findViewById(R.id.ll_item);
		TextView txtItem = view.findViewById(R.id.txt_item);
		TextView txtUpcomingDue = view.findViewById(R.id.txt_upcoming_due);
		boolean hasDistanceInterval = false;
		boolean hasDurationInterval = false;

		txtItem.setText(upcomingMaintenanceItem.getItem());

		String upcomingDue = "";

		if (upcomingMaintenanceItem.getDistance_interval() != 0) {
			hasDistanceInterval = true;
			upcomingDue = String.format(Locale.getDefault(), "%,d",
					upcomingMaintenanceItem.get_distanceLeft())
					+ " " + context.getString(R.string.kilometer);
			if (upcomingMaintenanceItem.get_durationDaysLeft() != 0) {
				hasDurationInterval = true;
				upcomingDue += " or ";
			}
		}
		if (upcomingMaintenanceItem.get_durationDaysLeft() != 0) {
			hasDurationInterval = true;
			upcomingDue += upcomingMaintenanceItem.get_durationDaysLeft() + " days";
		}
		if (hasDistanceInterval || hasDurationInterval) {
			upcomingDue += " left. ";
		}
		if (hasDistanceInterval && hasDurationInterval) {
			upcomingDue += "Whichever comes first.";
		}
		txtUpcomingDue.setText(upcomingDue);

		if ((hasDistanceInterval && upcomingMaintenanceItem.get_distanceLeft() < 100)
				|| (hasDurationInterval && upcomingMaintenanceItem.get_durationDaysLeft() < 5)) {
			llItem.setBackgroundColor(getMagnitudeColor(context, 4));
			txtItem.setTextColor(ContextCompat.getColor(context, R.color.white));
			txtUpcomingDue.setTextColor(ContextCompat.getColor(context, R.color.white));

		} else if ((hasDistanceInterval && upcomingMaintenanceItem.get_distanceLeft() < 500)
				|| (hasDurationInterval && upcomingMaintenanceItem.get_durationDaysLeft() < 10)) {
			llItem.setBackgroundColor(getMagnitudeColor(context, 3));
			txtItem.setTextColor(ContextCompat.getColor(context, R.color.white));
			txtUpcomingDue.setTextColor(ContextCompat.getColor(context, R.color.white));

		} else if ((hasDistanceInterval && upcomingMaintenanceItem.get_distanceLeft() < 1000)
				|| (hasDurationInterval && upcomingMaintenanceItem.get_durationDaysLeft() < 14)) {
			llItem.setBackgroundColor(getMagnitudeColor(context, 2));
			txtItem.setTextColor(ContextCompat.getColor(context, R.color.white));
			txtUpcomingDue.setTextColor(ContextCompat.getColor(context, R.color.white));

		} else if ((hasDistanceInterval && upcomingMaintenanceItem.get_distanceLeft() < 1500)
				|| (hasDurationInterval && upcomingMaintenanceItem.get_durationDaysLeft() < 21)) {
			llItem.setBackgroundColor(getMagnitudeColor(context, 1));
			txtItem.setTextColor(ContextCompat.getColor(context, R.color.white));
			txtUpcomingDue.setTextColor(ContextCompat.getColor(context, R.color.white));
		}

		llParent.addView(view);
	}

	private int getMagnitudeColor(Context context, double mag) {
		int magnitudeColorResourceId;
		int magFloor = (int) Math.floor(mag);

		switch (magFloor) {
			case 1:
				magnitudeColorResourceId = R.color.magnitude4;
				break;
			case 2:
				magnitudeColorResourceId = R.color.magnitude6;
				break;
			case 3:
				magnitudeColorResourceId = R.color.magnitude7;
				break;
			default:
				magnitudeColorResourceId = R.color.magnitude10plus;
				break;
		}
		return ContextCompat.getColor(context, magnitudeColorResourceId);
	}

}
