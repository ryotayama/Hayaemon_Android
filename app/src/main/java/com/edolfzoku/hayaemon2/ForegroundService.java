package com.edolfzoku.hayaemon2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

public class ForegroundService extends Service {
    private Notification notification;
    private PendingIntent pendIntent;
    public class ForegroundServiceBinder extends Binder {
        public ForegroundService getService() {
            return ForegroundService.this;
        }
    }

    private final IBinder mBinder = new ForegroundServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        stopForeground();

        super.onDestroy();
    }

    public void startForeground(String strTitle, String strArtist) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager notificationManager =
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("default",
                    "ハヤえもんによる音声の再生",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("ハヤえもんによる音声の再生");
            notificationManager.createNotificationChannel(channel);
            notification = new NotificationCompat.Builder(this, "default")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                    .setContentTitle(strTitle)
                    .setContentText(strArtist)
                    .build();
        }
        else {
            notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                    .setContentTitle(strTitle)
                    .setContentText(strArtist)
                    .build();
        }
        notification.flags |= Notification.FLAG_NO_CLEAR;

        startForeground(1, notification);
    }

    public void stopForeground()
    {
        stopForeground(true);
    }
}
