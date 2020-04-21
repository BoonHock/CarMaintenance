package com.example.carmaintenance.cursoradapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.carmaintenance.R;
import com.example.carmaintenance.data.FirebaseContract.FirebaseMaintenanceDetailsEntry;
import com.example.carmaintenance.data.OdometerContract.OdometerEntry;
import com.example.carmaintenance.data.UserVehicleContract.UserVehicleEntry;
import com.example.carmaintenance.objects.FirebaseObj;
import com.example.carmaintenance.objects.MaintenanceItem;
import com.example.carmaintenance.objects.VehicleTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		int vehicleId = cursor.getInt(cursor
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
		final int currentOdometer = cursor.getInt(cursor
				.getColumnIndexOrThrow(OdometerEntry.COLUMN_DISTANCE));

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
						firebaseVehicleId, currentOdometer, currentUsage);
			}
		});
	}

	private void displayUpcomingMaintenance(
			Context context, View view,
			String firebaseVehicleId, int currentOdometer, int usage) {
		TextView txtDueDistance = view.findViewById(R.id.txt_due_distance);
		TextView txtDueDate = view.findViewById(R.id.txt_due_date);
		TextView txtInspect = view.findViewById(R.id.txt_inspect);
		TextView txtReplace = view.findViewById(R.id.txt_replace);
		LinearLayout llInspectItems = view.findViewById(R.id.ll_inspect_items);
		LinearLayout llReplaceItems = view.findViewById(R.id.ll_replace_items);

		List<MaintenanceItem> maintenanceItems = FirebaseObj._maintenanceItems.get(firebaseVehicleId);

		// map of recommended odometer by vehicle company to list of items to change
		Map<Integer, Map<Integer, List<String>>> upcomingItems = new HashMap<>();

		// loop though each maintenance items and put in @upcomingItems according to
		// their distance recommendation
		if (maintenanceItems != null) {
			for (MaintenanceItem item : maintenanceItems) {
				int itemUsage = item.getUsage();

				// -1 usage means apply to all usage type
				// if not -1 then check if maintenance's usage type is
				// same with user vehicle's usage type
				if (itemUsage == -1 || itemUsage == usage) {
					int cumulativeDistance = item.getFirst_distance();
					// some items may not have distance interval
					if (item.getDistance_interval() > 0) {
						// if got, loop until cumulative distance is more than
						// user's current odometer
						while (cumulativeDistance <= currentOdometer) {
							cumulativeDistance += item.getDistance_interval();
						}
					}
					// if cumulative distance key not created in map yet, create
					if (upcomingItems.get(cumulativeDistance) == null) {
						upcomingItems.put(cumulativeDistance, new HashMap<Integer, List<String>>());
						upcomingItems.get(cumulativeDistance).put(FirebaseMaintenanceDetailsEntry.INSPECT, new ArrayList<String>());
						upcomingItems.get(cumulativeDistance).put(FirebaseMaintenanceDetailsEntry.REPLACE, new ArrayList<String>());
					}
					upcomingItems.get(cumulativeDistance).get(item.getInspect_replace()).add(item.getItem());
				}
			}

			if (upcomingItems.size() > 0) {
				// get minimum upcoming distance, which is next service
				int minUpcomingDistance = Collections.min(upcomingItems.keySet());
				txtDueDistance.setText(String.valueOf(minUpcomingDistance));

				if (upcomingItems.get(minUpcomingDistance).
						get(FirebaseMaintenanceDetailsEntry.INSPECT).size() > 0) {
					txtInspect.setVisibility(View.VISIBLE);
					addMaintenanceItemViews(context,
							llInspectItems,
							upcomingItems.get(minUpcomingDistance)
									.get(FirebaseMaintenanceDetailsEntry.INSPECT));
				}
				if (upcomingItems.get(minUpcomingDistance)
						.get(FirebaseMaintenanceDetailsEntry.REPLACE).size() > 0) {
					txtReplace.setVisibility(View.VISIBLE);
					addMaintenanceItemViews(context,
							llReplaceItems,
							upcomingItems.get(minUpcomingDistance)
									.get(FirebaseMaintenanceDetailsEntry.REPLACE));
				}
			}
		}
	}

	private void addMaintenanceItemViews(Context context,
										 LinearLayout llParent,
										 List<String> listItems) {
		llParent.removeAllViews(); // remove all child views and re-add
		Collections.sort(listItems);
		for (String strItem : listItems) {
			View newView = LayoutInflater.from(context)
					.inflate(R.layout.template_maintenance_item, null);
			TextView txtItem = newView.findViewById(R.id.txt_item);
			txtItem.setText(strItem);
			llParent.addView(newView);
		}
	}
}
