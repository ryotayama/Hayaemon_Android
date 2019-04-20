package com.edolfzoku.hayaemon2;

import java.util.TimerTask;

class MetronomeTask extends TimerTask {
    private final EffectFragment mEffectFragment;

    MetronomeTask(EffectFragment effectFragment)
    {
        mEffectFragment = effectFragment;
    }

    @Override
    public void run() {
        mEffectFragment.playMetronome();
    }
}
