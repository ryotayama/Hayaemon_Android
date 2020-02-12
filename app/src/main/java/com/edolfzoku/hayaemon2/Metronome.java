package com.edolfzoku.hayaemon2;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.io.IOException;
import java.io.InputStream;

public class Metronome {
    private int mBpm;
    private boolean mPlaying;
    private byte[] mSound;
    private AudioTrack mAudioTrack;
    private Runnable mRunnable;
    private Thread mThread;

    public void setBpm(int bpm) { mBpm = bpm; }
    public boolean isPlaying() { return mPlaying; }

    public Metronome(Context context) {
        InputStream is = context.getResources().openRawResource(R.raw.click);
        int size = 1024 * 1024;
        try {
            size = is.available();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        byte[] wavInfo = new byte[44];
        byte[] sound = new byte[size];

        try {
            is.read(wavInfo);
            is.read(sound);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 44100, AudioTrack.MODE_STREAM);

        mPlaying = false;
        mBpm = 120;
        mSound = sound;
    }

    private byte[] buildSpace(int beatLength, int soundLength) {
        int spaceLength = beatLength - soundLength;
        byte[] space = new byte[spaceLength];
        return space;
    }

    public void play() {
        stop();
        if(mThread != null) {
            try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mAudioTrack.play();
        mPlaying = true;

        mRunnable = new Runnable() {
            @Override
            public void run() {
                while (mPlaying) {
                    int beatLength = (int) Math.round((60.0 / mBpm) * mAudioTrack.getSampleRate());
                    beatLength = beatLength * 2;
                    int soundLength = mSound.length > beatLength ? beatLength : mSound.length;
                    mAudioTrack.write(mSound, 0, soundLength);
                    byte[] space = buildSpace(beatLength, soundLength);
                    mAudioTrack.write(space, 0, space.length);
                }
            }
        };

        mThread = new Thread(mRunnable);
        mThread.start();
    }

    public void stop() {
        mPlaying = false;
        mAudioTrack.pause();
        mAudioTrack.flush();
    }
}