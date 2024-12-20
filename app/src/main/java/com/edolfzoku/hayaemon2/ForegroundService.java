package com.edolfzoku.hayaemon2;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.view.KeyEvent.KEYCODE_MEDIA_NEXT;
import static android.view.KeyEvent.KEYCODE_MEDIA_PAUSE;
import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY;
import static android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import com.un4seen.bass.BASS;

import java.io.IOException;

public class ForegroundService extends IntentService {
    private PendingIntent pendingIntentForeground = null;
    private NotificationCompat.Action actionRewind= null;
    private NotificationCompat.Action actionPlay= null;
    private NotificationCompat.Action actionPause= null;
    private NotificationCompat.Action actionForward= null;
    private Notification notification;
    private NotificationCompat.Builder builder;
    private BroadcastReceiver mReceiver;
    private Bitmap mBitmap;
    private MediaSessionCompat mediaSession;
    private static final long AVAILABLE_ACTIONS = PlaybackStateCompat.ACTION_PLAY_PAUSE |
            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
            // PlaybackStateCompat.ACTION_SEEK_TO |
            PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT;

    public ForegroundService() {
        super("ForegroundService");
        MainActivity.sService = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if(mReceiver == null) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction() == null) return;
                    if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                        if (BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PLAYING)
                            PlaylistFragment.pause();
                    }
                }
            };
            registerReceiver(mReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        }

        mediaSession = new MediaSessionCompat(this, "MediaSessionTag", null, PendingIntent.getBroadcast(this, 0, new Intent(Intent.ACTION_MEDIA_BUTTON), FLAG_IMMUTABLE));
        MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                PlaylistFragment.onPlayBtnClick();
            }

            @Override
            public void onPause() {
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

            @Override
            public boolean onMediaButtonEvent(Intent intent) {
                final KeyEvent key = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (key != null && key.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (key.getKeyCode()) {
                        case KEYCODE_MEDIA_PLAY:
                            onPlay();
                            return true;
                        case KEYCODE_MEDIA_PAUSE:
                            onPause();
                            return true;
                        case KEYCODE_MEDIA_PREVIOUS:
                            onSkipToPrevious();
                            return true;
                        case KEYCODE_MEDIA_NEXT:
                            onSkipToNext();
                            return true;
                    }
                }
                return super.onMediaButtonEvent(intent);
            }
        };

        mediaSession.setCallback(mediaSessionCallback);

        PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1f)
                .setActions(AVAILABLE_ACTIONS)
                .build();
        mediaSession.setPlaybackState(playbackState);

        mediaSession.setActive(true);
    }

    @Override
    public void onDestroy() {
        PlaylistFragment.stop(true);
        stopForeground(true);
        MainActivity.sService = null;
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }

        if(mReceiver != null) {
            try {
                unregisterReceiver(mReceiver);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            mReceiver = null;
        }

        mediaSession.release();

        super.onDestroy();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if(intent == null) return super.onStartCommand(null, flags, startId);

        if(actionRewind == null) {
            Intent intentRewind = new Intent(this, ForegroundService.class);
            intentRewind.setAction("action_rewind");
            PendingIntent pendingIntentRewind = PendingIntent.getService(this, 1, intentRewind, Build.VERSION.SDK_INT >= 23 ? FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE : FLAG_UPDATE_CURRENT);
            actionRewind = new NotificationCompat.Action.Builder(R.drawable.ic_rewind, "Previous", pendingIntentRewind).build();
        }

        if(actionPlay == null) {
            Intent intentPlay = new Intent(this, ForegroundService.class);
            intentPlay.setAction("action_play");
            PendingIntent pendingIntentPlay = PendingIntent.getService(this, 1, intentPlay, Build.VERSION.SDK_INT >= 23 ? FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE : FLAG_UPDATE_CURRENT);
            actionPlay = new NotificationCompat.Action.Builder(R.drawable.ic_play, "Play", pendingIntentPlay).build();
        }

        if(actionPause == null) {
            Intent intentPause = new Intent(this, ForegroundService.class);
            intentPause.setAction("action_pause");
            PendingIntent pendingIntentPause = PendingIntent.getService(this, 1, intentPause, Build.VERSION.SDK_INT >= 23 ? FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE : FLAG_UPDATE_CURRENT);
            actionPause = new NotificationCompat.Action.Builder(R.drawable.ic_pause, "Pause", pendingIntentPause).build();
        }

        if(actionForward == null) {
            Intent intentForward = new Intent(this, ForegroundService.class);
            intentForward.setAction("action_forward");
            PendingIntent pendingIntentForward = PendingIntent.getService(this, 1, intentForward, Build.VERSION.SDK_INT >= 23 ? FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE : FLAG_UPDATE_CURRENT);
            actionForward = new NotificationCompat.Action.Builder(R.drawable.ic_forward, "Next", pendingIntentForward).build();
        }

        if(pendingIntentForeground == null) {
            Intent intentForeground = new Intent(this, MainActivity.class);
            intentForeground.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntentForeground = PendingIntent.getActivity(this, 1, intentForeground, Build.VERSION.SDK_INT >= 23 ? FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE : FLAG_UPDATE_CURRENT);
        }

        if(intent.getAction() == null) {
            String strTitle = intent.getStringExtra("strTitle");
            String strArtist = intent.getStringExtra("strArtist");
            String strPathArtwork = intent.getStringExtra("strPathArtwork");
            String strPath = intent.getStringExtra("strPath");
            if (mBitmap != null) {
                mBitmap.recycle();
                mBitmap = null;
            }
            if (strPathArtwork != null && strPathArtwork.equals("potatoboy"))
                mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.potatoboy);
            else if(strPathArtwork != null && !strPathArtwork.equals("")) {
                mBitmap = BitmapFactory.decodeFile(strPathArtwork);
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
                        mBitmap = decodeSampledBitmapFromByteArray(data, imageWidth, imageHeight);
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

            if (Build.VERSION.SDK_INT >= 26) {
                NotificationManager notificationManager =
                        (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel channel = new NotificationChannel("playsound",
                        getString(R.string.notificationDescription),
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.setSound(null, null);
                channel.enableVibration(false);
                channel.setDescription(getString(R.string.notificationDescription));
                if (notificationManager != null)
                    notificationManager.createNotificationChannel(channel);
            }

            if (mBitmap == null)
                mBitmap = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher);

            // long length = (long)(BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE)) * 1000);
            if (BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PLAYING) {
                PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
                        .setActions(AVAILABLE_ACTIONS)
                        .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1f)
                        // .setBufferedPosition(length)
                        .build();
                mediaSession.setPlaybackState(playbackState);
                // MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                //         .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, length)
                //         .build();
                // mediaSession.setMetadata(metadata);
                mediaSession.setActive(true);
            }
            else {
                PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
                        .setActions(AVAILABLE_ACTIONS)
                        .setState(PlaybackStateCompat.STATE_PAUSED, 0, 1f)
                        // .setBufferedPosition(length)
                        .build();
                mediaSession.setPlaybackState(playbackState);
                // MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                //         .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, length)
                //         .build();
                // mediaSession.setMetadata(metadata);
                mediaSession.setActive(true);
            }
            if (builder == null) {
                builder = new NotificationCompat.Builder(this, "playsound");
                if (Build.VERSION.SDK_INT < 33) {
                    builder.addAction(actionRewind);
                    builder.addAction(actionPause);
                    builder.addAction(actionForward);
                    builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2));
                }
                else {
                    builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSession.getSessionToken())
                    );
                }
                builder.setSmallIcon(R.drawable.ic_statusbar);
                builder.setLargeIcon(mBitmap);
                builder.setContentTitle(strTitle);
                builder.setContentText(strArtist);
                builder.setContentIntent(pendingIntentForeground);
                builder.setOngoing(true);
                notification = builder.build();
            }
            else {
                builder.setLargeIcon(mBitmap);
                builder.setContentTitle(strTitle);
                builder.setContentText(strArtist);
                if (Build.VERSION.SDK_INT >= 33 ) {
                    builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSession.getSessionToken())
                    );
                }
                notification = builder.build();
                NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                if(notificationManager != null)
                    notificationManager.notify(1, notification);
            }
            if (Build.VERSION.SDK_INT >= 29) {
                startForeground(1, notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
            } else {
                startForeground(1, notification);
            }
        }
        else {
            boolean stop = false;
            if(builder == null) {
                stop = true;
                if (Build.VERSION.SDK_INT >= 26) {
                    NotificationManager notificationManager =
                            (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationChannel channel = new NotificationChannel("playsound",
                            getString(R.string.notificationDescription),
                            NotificationManager.IMPORTANCE_DEFAULT);
                    channel.setSound(null, null);
                    channel.enableVibration(false);
                    channel.setDescription(getString(R.string.notificationDescription));
                    if (notificationManager != null)
                        notificationManager.createNotificationChannel(channel);
                }

                if (mBitmap != null) {
                    mBitmap.recycle();
                    mBitmap = null;
                }
                mBitmap = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher);
                builder = new NotificationCompat.Builder(this, "playsound");
                if (Build.VERSION.SDK_INT < 33) {
                    builder.addAction(actionRewind);
                    builder.addAction(actionPause);
                    builder.addAction(actionForward);
                    builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2));
                }
                else {
                    builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSession.getSessionToken())
                    );
                }
                builder.setSmallIcon(R.drawable.ic_statusbar);
                builder.setLargeIcon(mBitmap);
                builder.setContentTitle(getString(R.string.app_name));
                builder.setContentIntent(pendingIntentForeground);
                builder.setOngoing(true);
                notification = builder.build();
            }
            if (Build.VERSION.SDK_INT >= 29) {
                startForeground(1, notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
            } else {
                startForeground(1, notification);
            }
            if (stop || intent.getAction().equals("stop")) stopSelf();
            else if (intent.getAction().equals("action_rewind")) PlaylistFragment.onRewindBtnClick();
            else if(intent.getAction().equals("action_play")) {
                PlaylistFragment.onPlayBtnClick();
                updateNotificationIfSdkLessThan33(actionPause);
            }
            else if(intent.getAction().equals("action_pause")) {
                PlaylistFragment.onPlayBtnClick();
                updateNotificationIfSdkLessThan33(actionPlay);
            }
            else if (intent.getAction().equals("action_forward")) PlaylistFragment.onForwardBtnClick();
            else getBaseContext().sendBroadcast(new Intent(intent.getAction()));
        }

        MediaButtonReceiver.handleIntent(mediaSession, intent);

        return START_STICKY_COMPATIBILITY;
    }

    /**
     * SDK33未満の場合に通知の再生/停止ボタンを切り替えるため通知を更新(出しなおす)
     * @param actionPlayOrPause actionPlay or actionPause
     */
    private void updateNotificationIfSdkLessThan33(NotificationCompat.Action actionPlayOrPause) {
        if (Build.VERSION.SDK_INT < 33) {
            builder.clearActions();
            builder.addAction(actionRewind);
            builder.addAction(actionPlayOrPause);
            builder.addAction(actionForward);
            Notification newNotification = builder.build();
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            if(notificationManager != null)
                notificationManager.notify(1, newNotification);
        }
    }

    public static Bitmap decodeSampledBitmapFromByteArray(byte[] data,
                                                          int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }
}
