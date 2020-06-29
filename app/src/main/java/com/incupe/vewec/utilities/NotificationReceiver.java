package com.incupe.vewec.utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class NotificationReceiver {
	private static final int ID_UPDATE_ODOMETER_REMINDER = 0;

	private NotificationManager _notificationManager;

	public NotificationReceiver() {
	}

	private NotificationManager get_notificationManager(Context context) {
		if (_notificationManager == null) {
			_notificationManager = (NotificationManager)
					context.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		return _notificationManager;
	}

	public static void createNotificationChannel(Context context, String channelId,
												 String channelName, String channelDescription) {
		// Create the NotificationChannel, but only on API 26+ because
		// the NotificationChannel class is new and not in the support library
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel =
					new NotificationChannel(channelId, channelName,
							NotificationManager.IMPORTANCE_DEFAULT);
			channel.setDescription(channelDescription);
//			NotificationChannel channel =
//					new NotificationChannel(context
//							.getString(R.string.update_odometer_reminder_notification),
//							context.getString(R.string.update_odometer_reminder),
//							NotificationManager.IMPORTANCE_DEFAULT);
			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this
			NotificationManager notificationManager =
					context.getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}
	}
}
