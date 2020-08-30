package com.incupe.vewec.objects;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UpcomingMaintenanceItems {
	private List<UpcomingMaintenanceItem> _replaceItems;
	private List<UpcomingMaintenanceItem> _inspectItems;

	private UpcomingMaintenanceItems() {
	}

	public List<UpcomingMaintenanceItem> get_replaceItems() {
		return _replaceItems;
	}

	public List<UpcomingMaintenanceItem> get_inspectItems() {
		return _inspectItems;
	}

	public static UpcomingMaintenanceItems getInstance(
			Context context,
			UserVehicle userVehicle) {
		UpcomingMaintenanceItems items = new UpcomingMaintenanceItems();
		items._replaceItems = new ArrayList<>();
		items._inspectItems = new ArrayList<>();

		MaintenanceItems maintenanceItems = MaintenanceItems
				.getDbMaintenanceItems(context, userVehicle.get_vehicleId());

		for (MaintenanceItem maintenanceItem : maintenanceItems.get_replaceItems()) {
			if ((maintenanceItem.getDistance_interval() != 0 ||
					maintenanceItem.getDuration_interval() != 0) ||
					(userVehicle.is_isNew() &&
							(maintenanceItem.getFirst_distance() != 0 ||
									maintenanceItem.getFirst_duration() != 0))) {
				items._replaceItems.add(new UpcomingMaintenanceItem(
						context, maintenanceItem, userVehicle));
			}
		}
		for (MaintenanceItem maintenanceItem : maintenanceItems.get_inspectItems()) {
			if ((maintenanceItem.getDistance_interval() != 0 ||
					maintenanceItem.getDuration_interval() != 0) ||
					(userVehicle.is_isNew() &&
							(maintenanceItem.getFirst_distance() != 0 ||
									maintenanceItem.getFirst_duration() != 0))) {
				items._inspectItems.add(new UpcomingMaintenanceItem(
						context, maintenanceItem, userVehicle));
			}
		}

		Collections.sort(items._replaceItems, new CustomComparator());
		Collections.sort(items._inspectItems, new CustomComparator());

		return items;
	}

	public static class CustomComparator implements Comparator<UpcomingMaintenanceItem> {
		@Override
		public int compare(UpcomingMaintenanceItem o1, UpcomingMaintenanceItem o2) {
			// positive value means @compareItem is before @this
			int compareResults = o1.get_distanceLeft() - o2.get_distanceLeft();
			// if both same urgency, and both items have distance intervals,
			// then check distance left
			// if either one doesn't have distance interval,
			// the one without distance interval shall be placed after the ones with
			if (compareResults == 0) {
				if (o1.getDistance_interval() != 0
						&& o2.getDistance_interval() != 0) {
					compareResults = o1.get_distanceLeft() - o2.get_distanceLeft(); // ascending
				} else if (o1.getDistance_interval() == 0) {
					compareResults = 1;
				} else if (o2.getDistance_interval() == 0) {
					compareResults = -1;
				}
			}

			// if still same, then arrange by name alphabetically
			if (compareResults == 0) {
				compareResults = o1.getItem().compareToIgnoreCase(o2.getItem());
			}

			return compareResults;
		}
	}
}
