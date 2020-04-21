package com.example.carmaintenance.objects;

import java.util.ArrayList;
import java.util.Date;

public class UserVehicleService extends UserVehicle {
	private ArrayList<Part> _parts;
	private Date _serviceDate;
	private int _serviceDistance;

	public UserVehicleService(String regNo, String brand, String model, ArrayList<Part> parts,
							  Date serviceDate, int serviceDistance) {
		super(regNo, brand, model);

		_parts = parts;
		_serviceDate = serviceDate;
		_serviceDistance = serviceDistance;
	}

	public ArrayList<Part> get_parts() {
		return _parts;
	}

	public Date get_serviceDate() {
		return _serviceDate;
	}

	public void set_serviceDate(Date _serviceDate) {
		this._serviceDate = _serviceDate;
	}

	public int get_serviceDistance() {
		return _serviceDistance;
	}

	public void set_serviceDistance(int _serviceDistance) {
		this._serviceDistance = _serviceDistance;
	}
}
