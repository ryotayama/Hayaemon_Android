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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;

import com.un4seen.bass.BASS;

import java.util.ArrayList;

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
        Paint paint = new Paint();
        paint.setStrokeWidth(mWaveView.getContext().getResources().getDisplayMetrics().density);
        int hTempStream = mWaveView.getTempSteam();
        long lMaxLength = BASS.BASS_ChannelGetLength(hTempStream, BASS.BASS_POS_BYTE);
        int nMaxWidth = (int)(mWaveView.getWidth() * mWaveView.getZoom());
        int nTotalWidth = 0;
        int nStart = 0;
        int nEnd = 0;
        BASS.BASS_CHANNELINFO info = new BASS.BASS_CHANNELINFO();
        BASS.BASS_ChannelGetInfo(hTempStream, info);
        Boolean bStereo = true;
        if(info.chans == 1) bStereo = false;
        ArrayList<Canvas> arCanvases = mWaveView.getCanvases();
        ArrayList<Bitmap> arBitmaps = mWaveView.getBitmaps();
        for(int i = 0; i < arCanvases.size(); i++) {
            Canvas canvas = arCanvases.get(i);
            Bitmap bitmap = arBitmaps.get(i);
            int nWidth = bitmap.getWidth();
            nTotalWidth += nWidth;
            nStart = nEnd;
            nEnd = (int)(lMaxLength * nTotalWidth / nMaxWidth);
            long lLength = nEnd - nStart;
            for(int j = 0; j < nWidth; j += mWaveView.getContext().getResources().getDisplayMetrics().density)
            {
                BASS.BASS_ChannelSetPosition(hTempStream, nStart + lLength * j / nWidth, BASS.BASS_POS_BYTE);
                float[] arLevels = new float[2];
                if(bStereo)
                    BASS.BASS_ChannelGetLevelEx(hTempStream, arLevels, 0.1f, BASS.BASS_LEVEL_STEREO);
                else
                    BASS.BASS_ChannelGetLevelEx(hTempStream, arLevels, 0.1f, BASS.BASS_LEVEL_MONO);
                int nHeight = mWaveView.getHeight() / 2;
                int nLeftHeight = (int)(nHeight * arLevels[0]);
                int nRightHeight;
                if(bStereo)
                    nRightHeight= (int)(nHeight * arLevels[1]);
                else
                    nRightHeight= (int)(nHeight * arLevels[0]);
                if(nLeftHeight < 2) nLeftHeight = 2;
                if(nRightHeight < 2) nRightHeight = 2;
                paint.setColor(Color.argb(255, 255, 255, 255));
                canvas.drawLine(j, 0, j, nHeight * 2, paint);
                paint.setColor(Color.argb(255, 128, 166, 199));
                canvas.drawLine(j, nHeight / 2 - nLeftHeight / 2, j, nHeight / 2 + nLeftHeight / 2, paint);
                canvas.drawLine(j, nHeight + nHeight / 2 - nRightHeight / 2, j, nHeight + nHeight / 2 + nRightHeight / 2, paint);
                if (this.isCancelled()) break;
            }
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Integer result)
    {
        mWaveView.invalidate();
    }
}
