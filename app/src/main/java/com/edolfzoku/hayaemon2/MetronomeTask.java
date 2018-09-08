package com.edolfzoku.hayaemon2;

import android.media.MediaPlayer;

import java.util.TimerTask;

public class MetronomeTask extends TimerTask {
    EffectFragment effectFragment;

    public MetronomeTask(EffectFragment effectFragment)
    {
        this.effectFragment = effectFragment;
    }

    @Override
    public void run() {
        effectFragment.playMetronome();
    }
}
