package com.incupe.vewec.utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtilities {
	private static final String LOG_TAG = DateUtilities.class.getSimpleName();

	public static String dateToStringDate(Date date) {
		DateFormat df = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
		df.setTimeZone(Calendar.getInstance().getTimeZone());
		return df.format(date);
	}

	public static String dateToStringDateTime(Date date) {
		DateFormat df = new SimpleDateFormat("d MMM yyyy HH:mm", Locale.getDefault());
		df.setTimeZone(Calendar.getInstance().getTimeZone());
		return df.format(date);
	}

	public static Calendar getCalendarAtMidnight(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}
/*
	public static Date dbStringToDate(String dbString) {
		try {
			SimpleDateFormat dateFormat =
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
			// db date format is UTC
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			return dateFormat.parse(dbString);
		} catch (ParseException e) {
			Log.e(LOG_TAG, "failed to convert database date string for: "
					+ e.getMessage());
		}
		return null;
	}
*/
}
