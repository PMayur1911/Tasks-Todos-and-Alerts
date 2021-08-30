package com.example.todomain.misc;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

/**
 * Wraps the whole application with all its services, activities etc.
 * Used to setup some important settings
 */
public class NotificationHelper extends Application {

    public static final String notifyNewTaskID = "notifyNewTaskID";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Check if Android level is >= 26 (Oreo)
        // Notification Class isn't available on lower levels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notifyNewTaskChannel = new NotificationChannel(
                    notifyNewTaskID,
                    "New Task Created",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notifyNewTaskChannel.setDescription("Notification Channel for notifying new task creation");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notifyNewTaskChannel);
        }
    }
}
