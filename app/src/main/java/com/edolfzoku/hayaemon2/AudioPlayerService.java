package com.edolfzoku.hayaemon2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.un4seen.bass.BASS;

import java.io.IOException;
import java.util.List;

public class AudioPlayerService extends MediaBrowserServiceCompat {

    private final String TAG = "AudioPlayerService";

    private MediaSessionCompat mMediaSession = null;

    private NotificationManager mNotificationManager = null;

    public Notification showNotification(String strPath, String strTitle, String strArtist, String strPathArtwork) {
        Bitmap bitmap = null;
        if (strPathArtwork != null && strPathArtwork.equals("potatoboy"))
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.potatoboy);
        else if(strPathArtwork != null && !strPathArtwork.equals("")) {
            bitmap = BitmapFactory.decodeFile(strPathArtwork);
        }
        else {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            try {
                mmr.setDataSource(this, Uri.parse(strPath));
                byte[] data = mmr.getEmbeddedPicture();
                if(data != null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(data, 0, data.length, options);
                    int imageHeight = options.outHeight;
                    int imageWidth = options.outWidth;
                    int maxSize = (int)(128 * getResources().getDisplayMetrics().density);
                    while ((imageHeight > maxSize) || (imageWidth > maxSize)) {
                        imageHeight /= 2; imageWidth /= 2;
                    }
                    bitmap = ForegroundService.decodeSampledBitmapFromByteArray(data, imageWidth, imageHeight);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    mmr.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "playsound");
        notificationBuilder
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mMediaSession.getSessionToken()).setShowActionsInCompactView(0))
                .setColor(ContextCompat.getColor(getApplicationContext(), android.R.color.background_light))
                .setSmallIcon(R.drawable.ic_statusbar)
                .setLargeIcon(bitmap)
                .setContentTitle(strTitle)
                .setContentText(strArtist);

        NotificationCompat.Action previousAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_rewind, "previous",  MediaButtonReceiver.buildMediaButtonPendingIntent(getApplicationContext(), PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS))
                        .build();
        notificationBuilder.addAction(previousAction);
        if (BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PLAYING) {
            NotificationCompat.Action pauseAction =
                    new NotificationCompat.Action.Builder(R.drawable.ic_pause, "pause", MediaButtonReceiver.buildMediaButtonPendingIntent(getApplicationContext(), PlaybackStateCompat.ACTION_PAUSE))
                            .build();
            notificationBuilder.addAction(pauseAction);
            setNewState(PlaybackStateCompat.STATE_PLAYING);
        } else {
            NotificationCompat.Action startAction =
                    new NotificationCompat.Action.Builder(R.drawable.ic_play, "play", MediaButtonReceiver.buildMediaButtonPendingIntent(getApplicationContext(), PlaybackStateCompat.ACTION_PLAY))
                            .build();
            notificationBuilder.addAction(startAction);
            setNewState(PlaybackStateCompat.STATE_PAUSED);
        }
        NotificationCompat.Action nextAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_forward, "next",  MediaButtonReceiver.buildMediaButtonPendingIntent(getApplicationContext(), PlaybackStateCompat.ACTION_SKIP_TO_NEXT))
                        .build();
        notificationBuilder.addAction(nextAction);

        Notification notify = notificationBuilder.build();
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // メディアセッションを作成
        mMediaSession = new MediaSessionCompat(getApplicationContext(), TAG);
        MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {

            @Override
            public boolean onMediaButtonEvent(Intent intent) {
                KeyEvent key = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                Log.d(TAG, String.valueOf(key.getKeyCode()));
                return super.onMediaButtonEvent(intent);
            }

            @Override
            public void onPlay() {
                PlaylistFragment.onPlayBtnClick();
                setNewState(PlaybackStateCompat.STATE_PLAYING);
                mMediaSession.setActive(true);
            }

            @Override
            public void onPause() {
                mMediaSession.setActive(true);
                setNewState(PlaybackStateCompat.STATE_PAUSED);
                PlaylistFragment.onPlayBtnClick();
            }

            @Override
            public void onSkipToPrevious() {
                PlaylistFragment.onRewindBtnClick();
            }

            @Override
            public void onSkipToNext() {
                PlaylistFragment.onForwardBtnClick();
            }
        };

        mMediaSession.setCallback(mediaSessionCallback);
        if (this.getSessionToken() == null) this.setSessionToken(mMediaSession.getSessionToken());
        mMediaSession.setActive(true);

        // 通知チャンネルを作成
        createNotifyChannel();

        // 通知を表示
        String strPath = intent.getStringExtra("strPath");
        String strTitle = intent.getStringExtra("strTitle");
        String strArtist = intent.getStringExtra("strArtist");
        String strPathArtwork = intent.getStringExtra("strPathArtwork");
        showNotification(strPath, strTitle, strArtist, strPathArtwork);

        MediaButtonReceiver.handleIntent(mMediaSession, intent);

        return START_NOT_STICKY;
    }

    private void setNewState(int newState) {
        mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions())
                .setState(newState, 0, 1f)
                .build());
    }

    private long getAvailableActions() {
        return PlaybackStateCompat.ACTION_PLAY_PAUSE |
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                // PlaybackStateCompat.ACTION_SEEK_TO |
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        stopSelf();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        mMediaSession.setActive(false);
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
