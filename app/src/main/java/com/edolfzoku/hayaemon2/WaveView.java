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

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;

import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS_AAC;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

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

        BASS.BASS_FILEPROCS fileprocs=new BASS.BASS_FILEPROCS() {
            @Override
            public boolean FILESEEKPROC(long offset, Object user) {
                FileChannel fc=(FileChannel)user;
                try {
                    fc.position(offset);
                    return true;
                } catch (IOException e) {
                }
                return false;
            }

            @Override
            public int FILEREADPROC(ByteBuffer buffer, int length, Object user) {
                FileChannel fc=(FileChannel)user;
                try {
                    return fc.read(buffer);
                } catch (IOException e) {
                }
                return 0;
            }

            @Override
            public long FILELENPROC(Object user) {
                FileChannel fc=(FileChannel)user;
                try {
                    return fc.size();
                } catch (IOException e) {
                }
                return 0;
            }

            @Override
            public void FILECLOSEPROC(Object user) {
                FileChannel fc=(FileChannel)user;
                try {
                    fc.close();
                } catch (IOException e) {
                }
            }
        };

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        boolean bError = false;
        try {
            mmr.setDataSource(getContext(), Uri.parse(strPath));
        }
        catch(Exception e) {
            bError = true;
        }
        String strMimeType = null;
        if(!bError)
            strMimeType = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
        ContentResolver cr = getContext().getContentResolver();
        try {
            AssetFileDescriptor afd = cr.openAssetFileDescriptor(Uri.parse(strPath), "r");
            FileChannel fc = afd.createInputStream().getChannel();
            if(strMimeType == "audio/mp4")
                hTempStream = BASS_AAC.BASS_AAC_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS.BASS_STREAM_DECODE, fileprocs, fc);
            else
                hTempStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS.BASS_STREAM_DECODE, fileprocs, fc);
        } catch (IOException e) {
        }

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
        if(mBitmap == null) mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        if(mCanvas == null) mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }
}
