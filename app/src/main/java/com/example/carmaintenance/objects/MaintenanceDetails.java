package com.example.carmaintenance.objects;

import java.util.List;

public class MaintenanceDetails {
	private String _firebase_vehicle_id;
	private List<MaintenanceItem> _maintenanceItems;

	public MaintenanceDetails(String firebase_vehicle_id, List<MaintenanceItem> maintenanceItems) {
		_firebase_vehicle_id = firebase_vehicle_id;
		_maintenanceItems = maintenanceItems;
	}

	public String getFirebase_Vehicle_id() {
		return _firebase_vehicle_id;
	}

	public void setFirebase_Vehicle_id(String vehicle_id) {
		this._firebase_vehicle_id = vehicle_id;
	}

	public List<MaintenanceItem> getMaintenanceItems() {
		return _maintenanceItems;
	}

	public void setMaintenanceItems(List<MaintenanceItem> maintenanceItems) {
		this._maintenanceItems = maintenanceItems;
	}
}
