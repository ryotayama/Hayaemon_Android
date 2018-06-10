/*
 * WaveView
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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;

import com.un4seen.bass.BASS;

/**
 * Created by yamauchiryouta on 2017/10/03.
 */

public class WaveView extends View {

    private int hTempStream;
    private Bitmap mBitmap = null;
    private Canvas mCanvas = null;
    private Paint mPaint = null;

    private WaveViewTask task;

    public int getTempSteam() { return hTempStream; }
    public Canvas getCanvas() { return mCanvas; }
    public Paint getPaint() { return mPaint; }

    public WaveView(Context context) {
        super(context);
        setWillNotDraw(false);
        task = new WaveViewTask(this);
    }

    public WaveView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setWillNotDraw(false);
        task = new WaveViewTask(this);
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        task = new WaveViewTask(this);
    }

    // 描画処理を記述
    @Override
    protected void onDraw(Canvas canvas)
    {
        if(mBitmap == null) mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        canvas.drawBitmap( mBitmap, 0, 0, null );
    }

    public void drawWaveForm(String strPath)
    {
        if(task.getStatus() == AsyncTask.Status.RUNNING)
            task.cancel(true);
        if(hTempStream != 0)
        {
            BASS.BASS_StreamFree(hTempStream);
            hTempStream = 0;
        }
        hTempStream = BASS.BASS_StreamCreateFile(strPath, 0, 0, BASS.BASS_STREAM_DECODE);
        if(mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        if(mCanvas != null)
        {
            mCanvas = null;
        }
        mCanvas = new Canvas(mBitmap);
        if(mPaint != null)
        {
            mPaint = null;
        }
        mPaint = new Paint();
        clearWaveForm();
        mPaint.setColor(Color.argb(255, 128, 166, 199));
        mPaint.setStrokeWidth(1);
        task = new WaveViewTask(this);
        task.execute(0);
    }

    public void clearWaveForm()
    {
        if(mCanvas == null) mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }
}
