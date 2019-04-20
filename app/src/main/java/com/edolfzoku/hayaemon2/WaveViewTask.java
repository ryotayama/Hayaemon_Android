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

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by yamauchiryouta on 2018/01/16.
 */

class WaveViewTask extends AsyncTask<Integer, Integer, Integer>
{
    private final WeakReference<WaveView> mWaveViewRef;
    private float mPeak = 0.0f;
    private final int mTempStream;
    private final int mWidth;
    private final int mHeight;
    private final float mZoom;
    private final ArrayList<Bitmap> mBitmaps;
    private final ArrayList<Canvas> mCanvases;

    WaveViewTask(WaveView view, int tempStream, int width, int height, float zoom, ArrayList<Bitmap> bitmaps, ArrayList<Canvas> canvases)
    {
        mWaveViewRef = new WeakReference<>(view);
        mTempStream = tempStream;
        mWidth = width;
        mHeight = height;
        mZoom = zoom;
        mBitmaps = bitmaps;
        mCanvases = canvases;
    }

    @Override
    protected Integer doInBackground(Integer... params)
    {
        Paint paint = new Paint();
        paint.setStrokeWidth(1.0f);
        long lMaxLength = BASS.BASS_ChannelGetLength(mTempStream, BASS.BASS_POS_BYTE);
        int nMaxWidth = (int)(mWidth * mZoom);
        int nTotalWidth = 0;
        int nEnd = 0;
        BASS.BASS_CHANNELINFO info = new BASS.BASS_CHANNELINFO();
        BASS.BASS_ChannelGetInfo(mTempStream, info);
        boolean bStereo = true;
        if(info.chans == 1) bStereo = false;
        float fTempPeak = 0.0f;
        for(int i = 0; i < mCanvases.size(); i++) {
            Canvas canvas = mCanvases.get(i);
            Bitmap bitmap = mBitmaps.get(i);
            int mWidth = bitmap.getWidth();
            nTotalWidth += mWidth;
            int nStart = nEnd;
            nEnd = (int)(lMaxLength * nTotalWidth / nMaxWidth);
            long lLength = nEnd - nStart;
            for(int j = 0; j < mWidth; j++)
            {
                BASS.BASS_ChannelSetPosition(mTempStream, nStart + lLength * j / mWidth, BASS.BASS_POS_BYTE);
                float[] arLevels = new float[2];
                if(bStereo) {
                    BASS.BASS_ChannelGetLevelEx(mTempStream, arLevels, 0.1f, BASS.BASS_LEVEL_STEREO);
                    if (fTempPeak < arLevels[0]) fTempPeak = arLevels[0];
                    if (fTempPeak < arLevels[1]) fTempPeak = arLevels[1];
                }
                else {
                    BASS.BASS_ChannelGetLevelEx(mTempStream, arLevels, 0.1f, BASS.BASS_LEVEL_MONO);
                    if (fTempPeak < arLevels[0]) fTempPeak = arLevels[0];
                }
                int nHalfHeight = mHeight / 2;
                int nLeftHeight = (int)(nHalfHeight * arLevels[0]);
                int nRightHeight;
                if(bStereo)
                    nRightHeight= (int)(nHalfHeight * arLevels[1]);
                else
                    nRightHeight= (int)(nHalfHeight * arLevels[0]);
                if(nLeftHeight < 2) nLeftHeight = 2;
                if(nRightHeight < 2) nRightHeight = 2;
                paint.setColor(Color.argb(255, 255, 255, 255));
                canvas.drawLine(j, 0, j, nHalfHeight * 2, paint);
                paint.setColor(Color.argb(255, 128, 166, 199));
                canvas.drawLine(j, nHalfHeight / 2.0f - nLeftHeight / 2.0f, j, nHalfHeight / 2.0f + nLeftHeight / 2.0f, paint);
                canvas.drawLine(j, nHalfHeight + nHalfHeight / 2.0f - nRightHeight / 2.0f, j, nHalfHeight + nHalfHeight / 2.0f + nRightHeight / 2.0f, paint);
                if (this.isCancelled()) break;
            }
        }

        mPeak = fTempPeak;
        return 0;
    }

    @Override
    protected void onPostExecute(Integer result)
    {
        WaveView waveView = mWaveViewRef.get();
        waveView.invalidate();
        MainActivity activity = (MainActivity)waveView.getLoopFragment().getActivity();
        if(activity != null) activity.playlistFragment.setPeak(mPeak);
    }
}
