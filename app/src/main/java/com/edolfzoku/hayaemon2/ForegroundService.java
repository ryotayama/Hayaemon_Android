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
    private PendingIntent pendingIntentForeground = null;
    private NotificationCompat.Action actionRewind= null;
    private NotificationCompat.Action actionPlayPause= null;
    private NotificationCompat.Action actionForward= null;
    private Notification notification;
    private NotificationCompat.Builder builder;

    public ForegroundService()
    {
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
                try {
                    mmr.setDataSource(this, Uri.parse(strPath));
                    byte[] data = mmr.getEmbeddedPicture();
                    if (data != null) bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    int nSize = (int)(64 * getResources().getDisplayMetrics().density);
                    if(bitmap != null) bitmap = Bitmap.createScaledBitmap(bitmap, nSize, nSize, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    mmr.release();
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

            if (bitmap == null)
                bitmap = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher);

            if(actionRewind == null) {
                Intent intentRewind = new Intent(this, ForegroundService.class);
                intentRewind.setAction("action_rewind");
                PendingIntent pendingIntentRewind = PendingIntent.getService(this, 1, intentRewind, 0);
                actionRewind = new NotificationCompat.Action.Builder(R.drawable.ic_rewind, "Previous", pendingIntentRewind).build();
            }

            if(actionPlayPause == null) {
                Intent intentPlayPause = new Intent(this, ForegroundService.class);
                intentPlayPause.setAction("action_playpause");
                PendingIntent pendingIntentPlayPause = PendingIntent.getService(this, 1, intentPlayPause, 0);
                actionPlayPause = new NotificationCompat.Action.Builder(R.drawable.ic_pause, "Pause", pendingIntentPlayPause).build();
            }

            if(actionForward == null) {
                Intent intentForward = new Intent(this, ForegroundService.class);
                intentForward.setAction("action_forward");
                PendingIntent pendingIntentForward = PendingIntent.getService(this, 1, intentForward, 0);
                actionForward = new NotificationCompat.Action.Builder(R.drawable.ic_forward, "Next", pendingIntentForward).build();
            }

            if(pendingIntentForeground == null) {
                Intent intentForeground = new Intent(this, MainActivity.class);
                intentForeground.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                pendingIntentForeground = PendingIntent.getActivity(this, 1, intentForeground, 0);
            }

            int iconPlayPause;
            String playPauseTitle;
            if (BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PLAYING) {
                iconPlayPause = R.drawable.ic_pause;
                playPauseTitle = "Pause";
            }
            else {
                iconPlayPause = R.drawable.ic_play;
                playPauseTitle = "Play";
            }
            actionPlayPause.icon = iconPlayPause;
            actionPlayPause.title = playPauseTitle;
            if(builder == null) {
                builder = new NotificationCompat.Builder(this, "default");
                builder.addAction(actionRewind);
                builder.addAction(actionPlayPause);
                builder.addAction(actionForward);
                builder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2));
                builder.setSmallIcon(R.drawable.ic_statusbar);
                builder.setLargeIcon(bitmap);
                builder.setContentTitle(strTitle);
                builder.setContentText(strArtist);
                builder.setContentIntent(pendingIntentForeground);
                builder.setOngoing(true);
                notification = builder.build();
                startForeground(1, notification);
            }
            else {
                builder.setLargeIcon(bitmap);
                builder.setContentTitle(strTitle);
                builder.setContentText(strArtist);
                if(Build.VERSION.SDK_INT >= 19) {
                    notification.actions[1].icon = iconPlayPause;
                    notification.actions[1].title = playPauseTitle;
                }
                notification = builder.build();
                NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                if(notificationManager != null)
                    notificationManager.notify(1, notification);
            }
            bitmap.recycle();
        }
        else getBaseContext().sendBroadcast(new Intent(intent.getAction()));
        return START_STICKY_COMPATIBILITY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }
}
