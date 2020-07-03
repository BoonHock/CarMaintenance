package com.incupe.vewec.objects;

import com.google.firebase.database.Exclude;

import java.util.HashMap;

public class FuelPrice {
	@Exclude
	private String firebaseKey;
	public String updated_by;
	public long updated_on;
	public long date_from;
	public long date_to;
	public HashMap<String, Double> prices = new HashMap<>();
	public HashMap<String, Double> price_changes = new HashMap<>();

	// required for Firebase
	public FuelPrice() {
	}

	public FuelPrice(String updated_by,
					 long updated_on,
					 long date_from,
					 long date_to,
					 HashMap<String, Double> prices,
					 HashMap<String, Double> price_changes) {
		this.updated_by = updated_by;
		this.updated_on = updated_on;
		this.date_from = date_from;
		this.date_to = date_to;
		this.prices = prices;
		this.price_changes = price_changes;
	}

	/*
	 * have to write this function this way because ternary operation will give
	 * {Unboxing of HashMap 'get(key)' may produce 'NullPointerException'}
	 * warning
	 * Options for @key:
	 * 	- FirebaseContract.RON95_KEY
	 * 	- FirebaseContract.RON97_KEY
	 * 	- FirebaseContract.DIESEL_KEY
	 *  */
	public double getPrice(String key) {
		Double value = 0.0;
		if (prices.containsKey(key)) {
			value = prices.get(key);
		}

		return value == null ? 0 : value;
	}

	public double getChange(String key) {
		Double value = 0.0;
		if (price_changes.containsKey(key)) {
			value = price_changes.get(key);
		}

		return value == null ? 0 : value;
	}

	public String getFirebaseKey() {
		return firebaseKey;
	}

	public void setFirebaseKey(String firebaseKey) {
		this.firebaseKey = firebaseKey;
	}
}
