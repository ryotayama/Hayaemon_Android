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
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.un4seen.bass.BASS;

public class ForegroundService extends IntentService {

    public ForegroundService() {
        super("ForegroundService");
    }

    @Override
    public void onDestroy() {
        stopForeground(true);

        super.onDestroy();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if(intent == null) return super.onStartCommand(null, flags, startId);
        if(intent.getAction() == null) {
            String strTitle = intent.getStringExtra("strTitle");
            String strArtist = intent.getStringExtra("strArtist");
            String strPathArtwork = intent.getStringExtra("strPathArtwork");
            String strPath = intent.getStringExtra("strPath");
            Bitmap bitmap = null;
            if(strPathArtwork != null && !strPathArtwork.equals("")) {
                bitmap = BitmapFactory.decodeFile(strPathArtwork);
            }
            else {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                boolean bError = false;
                try {
                    mmr.setDataSource(getApplicationContext(), Uri.parse(strPath));
                } catch (Exception e) {
                    bError = true;
                }
                if (!bError) {
                    byte[] data = mmr.getEmbeddedPicture();
                    if (data != null) {
                        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= 26) {
                NotificationManager notificationManager =
                        (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel channel = new NotificationChannel("default",
                        getString(R.string.notificationDescription),
                        NotificationManager.IMPORTANCE_LOW);
                channel.setDescription(getString(R.string.notificationDescription));
                if (notificationManager != null)
                    notificationManager.createNotificationChannel(channel);
            }

            Intent intentRewind = new Intent(getApplicationContext(), ForegroundService.class);
            intentRewind.setAction("action_rewind");
            PendingIntent pendingIntentRewind = PendingIntent.getService(getApplicationContext(), 1, intentRewind, 0);

            Intent intentPlayPause = new Intent(getApplicationContext(), ForegroundService.class);
            intentPlayPause.setAction("action_playpause");
            PendingIntent pendingIntentPlayPause = PendingIntent.getService(getApplicationContext(), 1, intentPlayPause, 0);

            Intent intentForward = new Intent(getApplicationContext(), ForegroundService.class);
            intentForward.setAction("action_forward");
            PendingIntent pendingIntentForward = PendingIntent.getService(getApplicationContext(), 1, intentForward, 0);

            Intent intentForeground = new Intent(getApplicationContext(), MainActivity.class);
            intentForeground.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntentForeground = PendingIntent.getActivity(getApplicationContext(), 1, intentForeground, 0);

            if (bitmap == null)
                bitmap = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher);

            NotificationCompat.Action actionPlayPause;
            if (BASS.BASS_ChannelIsActive(MainActivity.hStream) == BASS.BASS_ACTIVE_PLAYING)
                actionPlayPause = new NotificationCompat.Action.Builder(R.drawable.ic_pause, "Pause", pendingIntentPlayPause).build();
            else
                actionPlayPause = new NotificationCompat.Action.Builder(R.drawable.ic_play, "Play", pendingIntentPlayPause).build();
            Notification notification = new NotificationCompat.Builder(this, "default")
                    .addAction(new NotificationCompat.Action.Builder(R.drawable.ic_rewind, "Previous", pendingIntentRewind).build())
                    .addAction(actionPlayPause)
                    .addAction(new NotificationCompat.Action.Builder(R.drawable.ic_forward, "Next", pendingIntentForward).build())
                    .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2))
                    .setSmallIcon(R.drawable.ic_statusbar)
                    .setLargeIcon(bitmap)
                    .setContentTitle(strTitle)
                    .setContentText(strArtist)
                    .setContentIntent(pendingIntentForeground)
                    .build();

            startForeground(1, notification);
        }
        else getBaseContext().sendBroadcast(new Intent(intent.getAction()));
        return START_STICKY_COMPATIBILITY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }
}
