package com.incupe.vewec.cursoradapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.incupe.vewec.R;
import com.incupe.vewec.objects.UpcomingMaintenanceItem;
import com.incupe.vewec.objects.UpcomingMaintenanceItems;
import com.incupe.vewec.objects.UserVehicle;

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
		TextView txtOdometer = view.findViewById(R.id.txt_odometer);

		txtRegNo.setText(userVehicle.get_regNo());
		txtBrandModel.setText(userVehicle.get_brandModelVariant());
		txtOdometer.setText(String.format(Locale.getDefault(),
				"%,d", userVehicle.getLatestOdometer(context)));

		displayUpcomingMaintenance(context, view, userVehicle);
	}

	private void displayUpcomingMaintenance(
			Context context,
			View view,
			UserVehicle userVehicle) {

		LinearLayout llReplace = view.findViewById(R.id.ll_replace_items);
		LinearLayout llInspect = view.findViewById(R.id.ll_inspect_items);

		TextView txtInspect = view.findViewById(R.id.txt_inspect);
		TextView txtReplace = view.findViewById(R.id.txt_replace);

		UpcomingMaintenanceItems upcomingMaintenanceItems =
				UpcomingMaintenanceItems.getInstance(context, userVehicle);

		llReplace.removeAllViews();
		llInspect.removeAllViews();

		addMaintenanceItems(context, llReplace, upcomingMaintenanceItems.get_replaceItems());
		addMaintenanceItems(context, llInspect, upcomingMaintenanceItems.get_inspectItems());

		if (llInspect.getChildCount() != 0) {
			txtInspect.setVisibility(View.VISIBLE);
		}
		if (llReplace.getChildCount() != 0) {
			txtReplace.setVisibility(View.VISIBLE);
		}
		if (llInspect.getChildCount() == 0 && llReplace.getChildCount() == 0) {
			view.findViewById(R.id.no_maintenance_items_for_vehicle).setVisibility(View.VISIBLE);
		}
		view.findViewById(R.id.progress_upcoming).setVisibility(View.GONE);
	}

	private void addMaintenanceItems(
			Context context,
			LinearLayout llParent,
			List<UpcomingMaintenanceItem> upcomingMaintenanceItems) {

		for (UpcomingMaintenanceItem item : upcomingMaintenanceItems) {
			// if item doesn't have any info about maintenance schedule
			// no need to display as upcoming item
			if (item.getDistance_interval() == 0 &&
					item.getDuration_interval() == 0 &&
					item.get_distanceLeft() == 0 &&
					item.get_durationDaysLeft() == 0) {
				continue;
			}

			View view = View.inflate(context, R.layout.template_upcoming_item, null);
			LinearLayout llItem = view.findViewById(R.id.ll_item);
			TextView txtItem = view.findViewById(R.id.txt_item);
			TextView txtUpcomingDue = view.findViewById(R.id.txt_upcoming_due);

			boolean hasDistanceInterval = false;
			boolean hasDurationInterval = false;

			txtItem.setText(item.getItem());

			String upcomingDue = "";

			if (item.getDistance_interval() != 0 ||
					(item.getFirst_distance() != 0 &&
							item.getFirst_distance() >= item.get_userVehicle().getLatestOdometer(context))) {
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

	public static int getUrgencyColour(Context context, int urgency) {
		int colorResourceId;

		switch (urgency) {
			case UpcomingMaintenanceItem.URGENCY_VERY3_URGENT:
				colorResourceId = R.color.urgency1;
				break;
			case UpcomingMaintenanceItem.URGENCY_VERY2_URGENT:
				colorResourceId = R.color.urgency2;
				break;
			case UpcomingMaintenanceItem.URGENCY_VERY_URGENT:
				colorResourceId = R.color.urgency3;
				break;
			case UpcomingMaintenanceItem.URGENCY_URGENT:
				colorResourceId = R.color.urgency4;
				break;
			default:
				colorResourceId = R.color.urgency_not_urgent;
				break;
		}
		return ContextCompat.getColor(context, colorResourceId);
	}

}
