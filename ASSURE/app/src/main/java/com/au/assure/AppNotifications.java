package com.au.assure;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

public class AppNotifications extends Application {
    public static final String CHANNEL1_ID = "ServiceChannel";
    public static final String CHANNEL2_ID = "DisconnectChannel";
    public static final String CHANNEL3_ID = "SeizureChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannelForeground();
        createNotificaitonChannelDisconnect();
        createNotificationChannelSeizure();
    }

    private void createNotificationChannelForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL1_ID,
                    "Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );

            serviceChannel.setDescription(getString(R.string.serviceChannelDesc));

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void createNotificaitonChannelDisconnect() {
        NotificationChannel DisconnectChannel = new NotificationChannel(
            CHANNEL2_ID,
            "DisconnectChannel",
            NotificationManager.IMPORTANCE_HIGH
        );

        DisconnectChannel.setDescription("This channel shows messages when device is disconnected");
        DisconnectChannel.enableVibration(true);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(DisconnectChannel);
    }

    private void createNotificationChannelSeizure() {
                NotificationChannel SeizureChannel = new NotificationChannel(
                CHANNEL3_ID,
                "SeizureChannel",
                NotificationManager.IMPORTANCE_HIGH
        );

        // Sound https://www.e2s.com/references-and-guidelines/listen-and-download-alarm-tones
        Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getApplicationContext().getPackageName()
                + "/" + R.raw.tone15);
        SeizureChannel.setDescription("This is seizure channel");
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
        SeizureChannel.setSound(uri, audioAttributes);
        SeizureChannel.setVibrationPattern(new long[]{0, 8000});
        SeizureChannel.enableVibration(true);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(SeizureChannel);
    }
}
