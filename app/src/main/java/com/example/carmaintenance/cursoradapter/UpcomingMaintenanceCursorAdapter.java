package com.example.carmaintenance.cursoradapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.carmaintenance.R;
import com.example.carmaintenance.data.CustomMaintenanceItemContract.CustomMaintenanceItemEntry;
import com.example.carmaintenance.data.MaintenanceItemContract.MaintenanceItemEntry;
import com.example.carmaintenance.data.UserVehicleContract.UserVehicleEntry;
import com.example.carmaintenance.objects.FirebaseObj;
import com.example.carmaintenance.objects.MaintenanceItem;
import com.example.carmaintenance.objects.UpcomingMaintenanceItem;
import com.example.carmaintenance.objects.UserVehicle;
import com.example.carmaintenance.objects.VehicleTemplate;

import java.util.ArrayList;
import java.util.Collections;
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
		final UserVehicle userVehicle = new UserVehicle(cursor);

		view.findViewById(R.id.txt_replace).setVisibility(View.GONE);
		view.findViewById(R.id.txt_inspect).setVisibility(View.GONE);

		TextView txtRegNo = view.findViewById(R.id.txt_reg_no);
		TextView txtBrandModel = view.findViewById(R.id.txt_brand_model);

		final String firebaseVehicleId = VehicleTemplate
				.getVehicleIdFromList(FirebaseObj._vehicleTemplates,
						userVehicle.get_brand(),
						userVehicle.get_model(),
						userVehicle.get_variant());

		txtRegNo.setText(userVehicle.get_regNo());
		txtBrandModel.setText(userVehicle.get_brandModelVariant());

		FirebaseObj.runCallbackMaintenanceDetails(firebaseVehicleId, new FirebaseObj() {
			@Override
			public void callback() {
				displayUpcomingMaintenance(context, view,
						firebaseVehicleId, userVehicle);
			}
		});
	}

	private void displayUpcomingMaintenance(
			Context context, View view, String firebaseVehicleId,
			UserVehicle userVehicle) {

		LinearLayout llInspect = view.findViewById(R.id.ll_inspect_items);
		LinearLayout llReplace = view.findViewById(R.id.ll_replace_items);

		TextView txtInspect = view.findViewById(R.id.txt_inspect);
		TextView txtReplace = view.findViewById(R.id.txt_replace);

		List<List<MaintenanceItem>> firebaseItems = FirebaseObj
				.getItemsByInspectReplace(firebaseVehicleId, userVehicle.get_usage());

		List<MaintenanceItem> maintenanceItemsInspect = firebaseItems.get(0);
		List<MaintenanceItem> maintenanceItemsReplace = firebaseItems.get(1);

		maintenanceItemsInspect.addAll(MaintenanceItem.getCustomMaintenanceItemNotInFirebase(
				context,
				maintenanceItemsInspect,
				MaintenanceItemEntry.INSPECT_VALUE));
		maintenanceItemsReplace.addAll(MaintenanceItem.getCustomMaintenanceItemNotInFirebase(
				context,
				maintenanceItemsReplace,
				MaintenanceItemEntry.REPLACE_VALUE));

		List<UpcomingMaintenanceItem> upcomingMaintenanceItemsInspect = new ArrayList<>();
		List<UpcomingMaintenanceItem> upcomingMaintenanceItemsReplace = new ArrayList<>();

		for (MaintenanceItem item : maintenanceItemsInspect) {
			upcomingMaintenanceItemsInspect.add(
					new UpcomingMaintenanceItem(context, item, userVehicle));
		}
		for (MaintenanceItem item : maintenanceItemsReplace) {
			upcomingMaintenanceItemsReplace.add(
					new UpcomingMaintenanceItem(context, item, userVehicle));
		}

		Collections.sort(upcomingMaintenanceItemsInspect, new UpcomingMaintenanceItem.CustomComparator());
		Collections.sort(upcomingMaintenanceItemsReplace, new UpcomingMaintenanceItem.CustomComparator());

		llInspect.removeAllViews();
		llReplace.removeAllViews();

		addMaintenanceItems(context, llInspect, upcomingMaintenanceItemsInspect);
		addMaintenanceItems(context, llReplace, upcomingMaintenanceItemsReplace);

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

	private List<UpcomingMaintenanceItem> getCustomMaintenanceItemNotInFirebase(
			Context context, List<MaintenanceItem> firebaseItems,
			UserVehicle userVehicle, int inspectReplace) {
		List<UpcomingMaintenanceItem> upcomingMaintenanceItems = new ArrayList<>();

		Cursor cursor = context.getContentResolver().query(
				CustomMaintenanceItemEntry.CONTENT_URI,
				CustomMaintenanceItemEntry.FULL_PROJECTION,
				CustomMaintenanceItemEntry.COLUMN_INSPECT_REPLACE + "=?",
				new String[]{String.valueOf(inspectReplace)},
				null);

		if (cursor != null) {
			while (cursor.moveToNext()) {
				String itemName = cursor.getString(cursor.getColumnIndexOrThrow(
						CustomMaintenanceItemEntry.COLUMN_ITEM));
				boolean add = true;

				for (MaintenanceItem firebaseItem : firebaseItems) {
					if (firebaseItem.getItem().toUpperCase().trim()
							.equals(itemName.toUpperCase().trim())
							&& firebaseItem.getInspect_replace() == inspectReplace) {
						add = false;
						break;
					}
				}
				if (add) {
					MaintenanceItem item = new MaintenanceItem("", itemName,
							cursor.getInt(cursor.getColumnIndexOrThrow(
									CustomMaintenanceItemEntry.COLUMN_INSPECT_REPLACE)),
							UserVehicleEntry.USAGE_ALL,
							cursor.getInt(cursor.getColumnIndexOrThrow(
									CustomMaintenanceItemEntry.COLUMN_DISTANCE_INTERVAL)),
							cursor.getInt(cursor.getColumnIndexOrThrow(
									CustomMaintenanceItemEntry.COLUMN_DISTANCE_INTERVAL)),
							cursor.getInt(cursor.getColumnIndexOrThrow(
									CustomMaintenanceItemEntry.COLUMN_DURATION_INTERVAL)),
							cursor.getInt(cursor.getColumnIndexOrThrow(
									CustomMaintenanceItemEntry.COLUMN_DURATION_INTERVAL)));
					upcomingMaintenanceItems.add(new UpcomingMaintenanceItem(
							context, item, userVehicle));
				}
			}
			cursor.close();
		}

		return upcomingMaintenanceItems;
	}

	private void addMaintenanceItems(
			Context context, LinearLayout llParent,
			List<UpcomingMaintenanceItem> upcomingMaintenanceItems) {
		for (UpcomingMaintenanceItem item : upcomingMaintenanceItems) {
			View view = View.inflate(context, R.layout.template_upcoming_item, null);
			LinearLayout llItem = view.findViewById(R.id.ll_item);
			TextView txtItem = view.findViewById(R.id.txt_item);
			TextView txtUpcomingDue = view.findViewById(R.id.txt_upcoming_due);

			boolean hasDistanceInterval = false;
			boolean hasDurationInterval = false;

			txtItem.setText(item.getItem());

			String upcomingDue = "";

			if (item.getDistance_interval() != 0) {
				hasDistanceInterval = true;
				upcomingDue = String.format(Locale.getDefault(), "%,d",
						item.get_distanceLeft())
						+ " " + context.getString(R.string.kilometer);
				if (item.get_durationDaysLeft() != 0) {
					hasDurationInterval = true;
					upcomingDue += " or ";
				}
			}
			if (item.get_durationDaysLeft() != 0) {
				hasDurationInterval = true;
				upcomingDue += item.get_durationDaysLeft() + " days";
			}
			if (hasDistanceInterval || hasDurationInterval) {
				upcomingDue += " left. ";
			}
			if (hasDistanceInterval && hasDurationInterval) {
				upcomingDue += "Whichever comes first.";
			}
			txtUpcomingDue.setText(upcomingDue);

			int urgency = item.getUrgency();

			if (urgency != UpcomingMaintenanceItem.URGENCY_NOT_URGENT) {
				llItem.setBackgroundColor(getUrgencyColour(context, urgency));
				txtItem.setTextColor(ContextCompat.getColor(context, R.color.white));
				txtUpcomingDue.setTextColor(ContextCompat.getColor(context, R.color.white));
			}

			llParent.addView(view);
		}
	}

	private int getUrgencyColour(Context context, double mag) {
		int magnitudeColorResourceId;
		int magFloor = (int) Math.floor(mag);

		switch (magFloor) {
			case UpcomingMaintenanceItem.URGENCY_VERY3_URGENT:
				magnitudeColorResourceId = R.color.urgency1;
				break;
			case UpcomingMaintenanceItem.URGENCY_VERY2_URGENT:
				magnitudeColorResourceId = R.color.urgency2;
				break;
			case UpcomingMaintenanceItem.URGENCY_VERY_URGENT:
				magnitudeColorResourceId = R.color.urgency3;
				break;
			case UpcomingMaintenanceItem.URGENCY_URGENT:
				magnitudeColorResourceId = R.color.urgency4;
				break;
			default:
				magnitudeColorResourceId = Color.TRANSPARENT;
				break;
		}
		return ContextCompat.getColor(context, magnitudeColorResourceId);
	}

}
