/*
 * SectionsPagerAdapter
 *
 * Copyright (c) 2018 Ryota Yamauchi. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.edolfzoku.hayaemon2;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class SectionsPagerAdapter extends FragmentPagerAdapter {
    private final PlaylistFragment playlistFragment;
    private final LoopFragment loopFragment;
    private final ControlFragment controlFragment;
    private final EqualizerFragment equalizerFragment;
    private final EffectFragment effectFragment;

    SectionsPagerAdapter(Context context, FragmentManager fm)
    {
        super(fm);
        MainActivity activity = (MainActivity)context;
        playlistFragment = activity.playlistFragment == null ? new PlaylistFragment() : activity.playlistFragment;
        loopFragment = activity.loopFragment == null ? new LoopFragment() : activity.loopFragment;
        controlFragment = activity.controlFragment == null ? new ControlFragment() : activity.controlFragment;
        equalizerFragment = activity.equalizerFragment == null ? new EqualizerFragment() : activity.equalizerFragment;
        effectFragment = activity.effectFragment == null ? new EffectFragment() : activity.effectFragment;
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position) {
            case 0: return playlistFragment;
            case 1: return loopFragment;
            case 2: return controlFragment;
            case 3: return equalizerFragment;
            case 4: return effectFragment;
            default: return null;
        }
    }

    @Override
    public int getCount()
    {
        return 5;
    }
}
