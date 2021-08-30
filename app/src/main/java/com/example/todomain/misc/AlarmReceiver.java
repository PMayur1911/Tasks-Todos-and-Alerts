package com.example.todomain.misc;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.todomain.R;

public class AlarmReceiver extends BroadcastReceiver {

    private int notificationNumber = 0;

    @Override
    public void onReceive(Context context, Intent intent) {

        notifyNewTask(context, "Task Alert!", "Task ends now!");
        soundAlarm(context);
    }

    private void soundAlarm(Context context){
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        if(alert == null){
            // alert is null, using backup
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            if(alert == null) {
                // alert backup is null, using 2nd backup
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        }

        MediaPlayer mp = MediaPlayer.create(context, alert);
        mp.start();
    }

    private void notifyNewTask(Context context, String title, String desc){
        Notification notification = new NotificationCompat.Builder(context, NotificationHelper.notifyNewTaskID)
                .setSmallIcon(R.drawable.ic_notification_new_task)
                .setContentTitle(title + " created!")
                .setContentText(desc)
                .setAutoCancel(true)
                .setLights(Color.argb(255,255,0,0), 1500, 1000)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(notificationNumber++, notification);
    }
}
