package com.example.carmaintenance.objects;

import java.util.ArrayList;
import java.util.Date;

public class History {
	private Date _date;
	private ArrayList<UserVehicleService> _carServices;

	public History(Date date, ArrayList<UserVehicleService> carServices) {
		_date = date;
		_carServices = carServices;
	}

	public Date get_date() {
		return _date;
	}

	public ArrayList<UserVehicleService> get_carServices() {
		return _carServices;
	}

	public void set_carServices(ArrayList<UserVehicleService> _carServices) {
		this._carServices = _carServices;
	}
}
