package com.edolfzoku.hayaemon2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;

import java.util.List;

public class AudioPlayerService extends MediaBrowserServiceCompat {

    private final String TAG = "AudioPlayerService";

    private MediaSessionCompat mMediaSession = null;
    private ExoPlayer mExoPlayer = null;

    private NotificationManager mNotificationManager = null;
    private NotificationCompat.Builder mNotificationBuilder = null;

    public Notification showNotification(String strTitle, String strArtist, String strPathArtwork) {
        mNotificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), "playsound");
        mNotificationBuilder
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mMediaSession.getSessionToken()).setShowActionsInCompactView(0))
                .setColor(ContextCompat.getColor(getApplicationContext(), android.R.color.background_light))
                .setSmallIcon(R.drawable.ic_statusbar)
                .setLargeIcon(BitmapFactory.decodeFile(strPathArtwork))
                .setContentTitle(strTitle)
                .setContentText(strArtist);

        Notification notify = mNotificationBuilder.build();
        // ForegroundServiceを起動するためには、通知を表示する必要がある
        startForeground(1, notify);
        return notify;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNotifyChannel() {
        NotificationChannel channel = new NotificationChannel("playsound",
                getString(R.string.notificationDescription), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(getString(R.string.notificationDescription));
        getApplicationContext();
        mNotificationManager = (NotificationManager)getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(channel);
    }

    public void audioPlay() {
        mMediaSession.setActive(true);
        mExoPlayer.setPlayWhenReady(true);
        Log.d(TAG, "onPlay");
    }

    public void playSetUp(Uri mediaURI) {
        ExoPlayer player = new ExoPlayer.Builder(getApplicationContext()).build();
        DataSource.Factory sourceFactory = new DefaultDataSource.Factory(getApplicationContext());
        ProgressiveMediaSource mediaSource= new ProgressiveMediaSource.Factory(sourceFactory).createMediaSource(MediaItem.fromUri(mediaURI));
        player.setMediaSource(mediaSource);
        player.prepare();
        mExoPlayer = player;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // メディアセッションを作成
        mMediaSession = new MediaSessionCompat(getApplicationContext(), TAG);
        // 通知チャンネルを作成
        createNotifyChannel();
        // ExoPlayerを作成し、再生準備
        String strPath = intent.getStringExtra("strPath");
        playSetUp(Uri.parse(strPath));
        audioPlay();

        // 通知を表示
        String strTitle = intent.getStringExtra("strTitle");
        String strArtist = intent.getStringExtra("strArtist");
        String strPathArtwork = intent.getStringExtra("strPathArtwork");
        showNotification(strTitle, strArtist, strPathArtwork);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        stopSelf();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        mExoPlayer.stop();
        stopForeground(true);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("root", null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }
}
