package com.incupe.vewec.data;

public class FirebaseContract {
	public static final class MaintenanceDetails {
		public static final int INSPECT = 0;
		public static final int REPLACE = 1;
	}

	public static final class FuelPrice {
		public static final String FUEL_PRICE_KEY = "fuel_prices";
		public static final String ACTUAL_KEY = "actual";
		public static final String FORECAST_KEY = "forecast";

		public static final String USER_KEY = "admin_users";
		public static final String RON95_KEY = "ron 95";
		public static final String RON97_KEY = "ron 97";
		public static final String DIESEL_KEY = "diesel";

		public static final String PRICES_KEY = "prices";
		public static final String DATE_TO_KEY = "date_to";
		public static final String DATE_FROM_KEY = "date_from";
	}

	public static final class News {
		public static final String NEWS_KEY = "news";

		public static final String ITEM_UPDATED_ON = "updated_on";
	}
}
