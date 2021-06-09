package com.au.assure;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull @org.jetbrains.annotations.NotNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean displayNotification = sharedPref.getBoolean("receiveRemote", false);

        if(displayNotification){
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, "Remote")
                            .setContentTitle(title)
                            .setContentText(body)
                            .setSmallIcon(R.drawable.ic_exclamation_triangle_solid);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            int id = (int) System.currentTimeMillis();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel channel = new NotificationChannel("Remote","Remote Channel",NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(id,notificationBuilder.build());
        }
    }
}
