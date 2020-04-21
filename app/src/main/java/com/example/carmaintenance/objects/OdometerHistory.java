package com.example.carmaintenance.objects;

import java.util.Date;

public class OdometerHistory {
	private int _vehicle;
	private String _regNo;
	private Date _date;
	private int _distance;

	public OdometerHistory(String regNo, Date date, int distance) {
		_regNo = regNo;
		_date = date;
		_distance = distance;
	}

	public String get_regNo() {
		return _regNo;
	}

	public Date get_date() {
		return _date;
	}

	public int get_distance() {
		return _distance;
	}
}
