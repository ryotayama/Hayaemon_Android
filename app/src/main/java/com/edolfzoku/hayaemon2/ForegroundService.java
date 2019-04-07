package com.edolfzoku.hayaemon2;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.un4seen.bass.BASS;

public class ForegroundService extends IntentService {
    private MainActivity activity;

    public void setMainActivity(MainActivity activity) { this.activity = activity; }

    class ForegroundServiceBinder extends Binder {
        ForegroundService getService() {
            return ForegroundService.this;
        }
    }

    private final IBinder mBinder = new ForegroundServiceBinder();

    public ForegroundService() {
        super("ForegroundService");
    }

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

    public void startForeground(String strTitle, String strArtist, Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager notificationManager =
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("default",
                    "ハヤえもんによる音声の再生",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("ハヤえもんによる音声の再生");
            if(notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }

        Notification notification;
        if (Build.VERSION.SDK_INT >= 21) {
            Intent intentRewind = new Intent(getApplicationContext(), ForegroundService.class );
            intentRewind.setAction("action_rewind");
            PendingIntent pendingIntentRewind = PendingIntent.getService(getApplicationContext(), 1, intentRewind, 0);

            Intent intentPause = new Intent(getApplicationContext(), ForegroundService.class );
            intentPause.setAction("action_pause");
            PendingIntent pendingIntentPause = PendingIntent.getService(getApplicationContext(), 1, intentPause, 0);

            Intent intentForward = new Intent(getApplicationContext(), ForegroundService.class );
            intentForward.setAction("action_forward");
            PendingIntent pendingIntentForward = PendingIntent.getService(getApplicationContext(), 1, intentForward, 0);

            if(bitmap == null)
                bitmap = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher);

            notification = new NotificationCompat.Builder(this, "default")
                    .addAction(new NotificationCompat.Action.Builder(R.drawable.ic_rewind, "Previous", pendingIntentRewind).build())
                    .addAction(new NotificationCompat.Action.Builder(R.drawable.ic_pause, "Pause", pendingIntentPause).build())
                    .addAction(new NotificationCompat.Action.Builder(R.drawable.ic_forward, "Next", pendingIntentForward).build())
                    .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(bitmap)
                    .setContentTitle(strTitle)
                    .setContentText(strArtist)
                    .build();
        }
        else {
            notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(bitmap)
                    .setContentTitle(strTitle)
                    .setContentText(strArtist)
                    .build();
        }

        startForeground(1, notification);
    }

    public void stopForeground()
    {
        stopForeground(true);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent.getAction() == null) return;

        switch(intent.getAction()) {
            case "action_rewind":
                if (MainActivity.hStream == 0) return;
                if (!activity.effectFragment.isReverse() && BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE)) > activity.dLoopA + 1.0)
                    BASS.BASS_ChannelSetPosition(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopA), BASS.BASS_POS_BYTE);
                else if (activity.effectFragment.isReverse() && BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE)) < activity.dLoopA - 1.0)
                    BASS.BASS_ChannelSetPosition(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopB), BASS.BASS_POS_BYTE);
                else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activity.playlistFragment.playPrev();
                        }
                    });
                }
                break;
            case "action_pause":
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    if (BASS.BASS_ChannelIsActive(MainActivity.hStream) == BASS.BASS_ACTIVE_PLAYING)
                        activity.playlistFragment.pause();
                    else if (BASS.BASS_ChannelIsActive(MainActivity.hStream) == BASS.BASS_ACTIVE_PAUSED)
                        activity.playlistFragment.play();
                    }
                });
                break;
            case "action_forward":
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    activity.playlistFragment.playNext(true);
                    }
                });
                break;
        }
    }
}
