package com.au.assure;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// Used for handling when the user chooses on the notification that
// they did have a seizure
public class SeizureReceiverPositive extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Recorder recorder = new Recorder();
        recorder.saveSeizureFeedback(true);

        // Close the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(3);
    }
}
