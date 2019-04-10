package com.edolfzoku.hayaemon2;

import java.util.TimerTask;

class MetronomeTask extends TimerTask {
    private final EffectFragment effectFragment;

    MetronomeTask(EffectFragment effectFragment)
    {
        this.effectFragment = effectFragment;
    }

    @Override
    public void run() {
        effectFragment.playMetronome();
    }
}
