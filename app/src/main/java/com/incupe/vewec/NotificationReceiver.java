package com.incupe.vewec;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {
	public static final String NOTIFICATION_TYPE = "notification";
	public static final String UPDATE_ODOMETER_REMINDER = "UPDATE_ODOMETER_REMINDER";

	@Override
	public void onReceive(Context context, Intent intent) {
		String stringExtra = intent.getStringExtra(NOTIFICATION_TYPE);

		if (stringExtra != null) {
			switch (stringExtra) {
				case UPDATE_ODOMETER_REMINDER:
					updateOdometerReminder(context);
			}
		}
	}

	private void updateOdometerReminder(Context context) {
		NotificationManager notificationManager = (NotificationManager)
				context.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent repeatingIntent = new Intent(context, MainActivity.class);
		repeatingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				repeatingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		String channel_id = context
				.getString(R.string.update_odometer_reminder_notification);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = context
					.getString(R.string.update_odometer_reminder);
			String description = "";
			int importance = NotificationManager.IMPORTANCE_DEFAULT;

			if (notificationManager.getNotificationChannel(channel_id) == null) {
				NotificationChannel channel = new NotificationChannel(channel_id, name, importance);
				channel.setDescription(description);
				// Register the channel with the system; you can't change the importance
				// or other notification behaviors after this
				notificationManager.createNotificationChannel(channel);
			}
		}
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel_id)
				.setContentIntent(pendingIntent)
				.setSmallIcon(R.mipmap.ic_launcher_transparent)
				.setContentTitle("Reminder to update your odometer")
				.setContentText("Update to get accurate recommendations.")
				.setAutoCancel(true);

		notificationManager.notify(100, builder.build());

	}
}
