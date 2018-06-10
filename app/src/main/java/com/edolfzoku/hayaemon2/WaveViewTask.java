/*
 * WaveViewTask
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

import android.os.AsyncTask;

import com.un4seen.bass.BASS;

/**
 * Created by yamauchiryouta on 2018/01/16.
 */

public class WaveViewTask extends AsyncTask<Integer, Integer, Integer>
{
    private WaveView mWaveView;

    public WaveViewTask(WaveView view)
    {
        mWaveView = view;
    }

    @Override
    protected Integer doInBackground(Integer... params)
    {
        int hTempStream = mWaveView.getTempSteam();
        long lLength = BASS.BASS_ChannelGetLength(hTempStream, BASS.BASS_POS_BYTE);
        for(int i = 0; i < mWaveView.getWidth(); i++)
        {
            BASS.BASS_ChannelSetPosition(hTempStream, lLength * i / mWaveView.getWidth(), BASS.BASS_POS_BYTE);
            float[] arLevels = new float[2];
            BASS.BASS_ChannelGetLevelEx(hTempStream, arLevels, 0.1f, BASS.BASS_LEVEL_STEREO);
            int nHeight = mWaveView.getHeight() / 2;
            int nLeftHeight = (int)(nHeight * arLevels[0]);
            int nRightHeight = (int)(nHeight * arLevels[1]);
            mWaveView.getCanvas().drawLine(i, nHeight / 2 - nLeftHeight / 2, i+1, nHeight / 2 + nLeftHeight / 2, mWaveView.getPaint());
            mWaveView.getCanvas().drawLine(i, nHeight + nHeight / 2 - nRightHeight / 2, i + 1, nHeight + nHeight / 2 + nRightHeight / 2, mWaveView.getPaint());
            if (this.isCancelled()) break;
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Integer result)
    {
        mWaveView.invalidate();
    }
}
