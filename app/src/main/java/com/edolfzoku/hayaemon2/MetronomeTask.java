package com.edolfzoku.hayaemon2;

import java.util.TimerTask;

public class MetronomeTask extends TimerTask {
    EffectFragment effectFragment;

    MetronomeTask(EffectFragment effectFragment)
    {
        this.effectFragment = effectFragment;
    }

    @Override
    public void run() {
        effectFragment.playMetronome();
    }
}
