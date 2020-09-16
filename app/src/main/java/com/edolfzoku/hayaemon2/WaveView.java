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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.opengl.GLES10;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;

import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS_FX;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import static com.edolfzoku.hayaemon2.MainActivity.sActivity;

/**
 * Created by yamauchiryouta on 2017/10/03.
 */

public class WaveView extends View {

    private LoopFragment mLoopFragment = null;
    private int mTempStream = 0;
    private ArrayList<Bitmap> mBitmaps = null;
    private ArrayList<Canvas> mCanvases = null;
    private String mPath = null;
    private float mZoom = 1.0f;
    private WaveViewTask mTask = null;
    private Rect mSrcRect = null;
    private Rect mDstRect = null;

    public LoopFragment getLoopFragment() { return mLoopFragment; }
    public void setLoopFragment(LoopFragment loopFragment) { mLoopFragment = loopFragment; }
    public float getZoom() { return mZoom; }
    public void setZoom(float zoom) {
        if(mZoom == zoom) return;
        if(zoom < 1.0f) zoom = 1.0f;
        else if(zoom > 10.0f) zoom = 10.0f;
        mZoom = zoom;
    }

    public WaveView(Context context) {
        super(context);
        setWillNotDraw(false);
        mBitmaps = new ArrayList<>();
        mCanvases = new ArrayList<>();
        mSrcRect = new Rect();
        mDstRect = new Rect();
    }

    public WaveView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setWillNotDraw(false);
        mBitmaps = new ArrayList<>();
        mCanvases = new ArrayList<>();
        mSrcRect = new Rect();
        mDstRect = new Rect();
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
    }

    // 描画処理を記述
    @Override
    protected void onDraw(Canvas canvas)
    {
        if(mBitmaps.size() == 0) {
            return;
        }
        long nLength = BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE);
        long nPos = BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE);
        int nScreenWidth = getWidth();
        int nMaxWidth = (int)(nScreenWidth * mZoom);
        int nLeft = (int) (nMaxWidth * nPos / nLength);
        if(nLeft < nScreenWidth / 2) {
            mSrcRect.set(0, 0, getWidth(), getHeight());
            mDstRect.set(0, 0, getWidth(), getHeight());
            canvas.drawBitmap(mBitmaps.get(0), mSrcRect, mDstRect, null);
        }
        else if(nScreenWidth / 2 <= nLeft && nLeft < nMaxWidth - nScreenWidth / 2) {
            int nStart = nLeft - nScreenWidth / 2;
            int nEnd = nLeft + nScreenWidth / 2;
            int nTotalWidth = 0;
            int nPaintLeft = 0;
            for(int i = 0; i < mBitmaps.size(); i++) {
                Bitmap bitmap = mBitmaps.get(i);
                int nBitmapStart = nTotalWidth;
                int nBitmapEnd = nTotalWidth + bitmap.getWidth();
                if(nStart <= nBitmapEnd && nBitmapStart <= nEnd) {
                    int nX = 0;
                    int nWidth = getWidth();
                    if(nPaintLeft == 0) {
                        nX = nStart - nTotalWidth;
                        if(nX + nWidth > bitmap.getWidth()) nWidth = bitmap.getWidth() - nX;
                        mSrcRect.set(nX, 0, nX + nWidth, getHeight());
                        mDstRect.set(nPaintLeft, 0, nPaintLeft + nWidth, getHeight());
                        canvas.drawBitmap(bitmap, mSrcRect, mDstRect, null);
                    }
                    else
                        nWidth -= nPaintLeft;
                    mSrcRect.set(nX, 0, nX + nWidth, getHeight());
                    mDstRect.set(nPaintLeft, 0, nPaintLeft + nWidth, getHeight());
                    canvas.drawBitmap(bitmap, mSrcRect, mDstRect, null);
                    nPaintLeft += nWidth;
                }
                nTotalWidth += bitmap.getWidth();
            }
        }
        else {
            nLeft = nMaxWidth - nScreenWidth / 2;
            int nStart = nLeft - nScreenWidth / 2;
            int nEnd = nLeft + nScreenWidth / 2;
            int nTotalWidth = 0;
            int nPaintLeft = 0;
            for(int i = 0; i < mBitmaps.size(); i++) {
                Bitmap bitmap = mBitmaps.get(i);
                int nBitmapStart = nTotalWidth;
                int nBitmapEnd = nTotalWidth + bitmap.getWidth();
                if(nStart <= nBitmapEnd && nBitmapStart <= nEnd) {
                    int nX = 0;
                    int nWidth = getWidth();
                    if(nPaintLeft == 0) {
                        nX = nStart - nTotalWidth;
                        if(nX + nWidth > bitmap.getWidth()) nWidth = bitmap.getWidth() - nX;
                        mSrcRect.set(nX, 0, nX + nWidth, getHeight());
                        mDstRect.set(nPaintLeft, 0, nPaintLeft + nWidth, getHeight());
                        canvas.drawBitmap(bitmap, mSrcRect, mDstRect, null);
                    }
                    else
                        nWidth -= nPaintLeft;
                    mSrcRect.set(nX, 0, nX + nWidth, getHeight());
                    mDstRect.set(nPaintLeft, 0, nPaintLeft + nWidth, getHeight());
                    canvas.drawBitmap(bitmap, mSrcRect, mDstRect, null);
                    nPaintLeft += nWidth;
                }
                nTotalWidth += bitmap.getWidth();
            }
        }
        if(getScaleX() != 1.0f) setScaleX(1.0f);
    }

    public void drawWaveForm(String path)
    {
        if(mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING)
            mTask.cancel(true);
        mPath = path;
        streamCreate();
        if(mBitmaps.size() > 0) {
            for(int i = 0; i < mBitmaps.size(); i++) {
                Bitmap bitmap = mBitmaps.get(i);
                bitmap.recycle();
            }
            mBitmaps.clear();
        }
        if(mCanvases.size() > 0) mCanvases.clear();
        int nMaxWidth = (int)(getWidth() * mZoom);
        int nTotalWidth = 0;
        clearWaveForm(false);
        while(nTotalWidth < nMaxWidth) {
            int nWidth = getMaxTextureSize();
            if(nTotalWidth + nWidth > nMaxWidth)
                nWidth = nMaxWidth - nTotalWidth;
            if(nWidth <= 0) break;
            try {
                Bitmap bitmap = Bitmap.createBitmap(nWidth, getHeight(), Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(LoopFragment.sActivity.isDarkMode() ? getResources().getColor(R.color.darkModeBk) : getResources().getColor(R.color.lightModeBk));
                mBitmaps.add(bitmap);
                mCanvases.add(canvas);
                nTotalWidth += nWidth;
            }
            catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        int colorBk = LoopFragment.sActivity.isDarkMode() ? getResources().getColor(R.color.darkModeBk) : getResources().getColor(R.color.lightModeBk);
        int colorWave = LoopFragment.sActivity.isDarkMode() ? Color.argb(255, 47, 86, 119) : Color.argb(255, 128, 166, 199);
        mTask = new WaveViewTask(this, mTempStream, getWidth(), getHeight(), mZoom, mBitmaps, mCanvases, colorBk, colorWave);
        mTask.execute(0);
    }

    private int getMaxTextureSize()
    {
        EGL10 egl = (EGL10) EGLContext.getEGL();

        EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        int[] vers = new int[2];
        egl.eglInitialize(dpy, vers);
        vers = null;

        int[] configAttr = {
                EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
                EGL10.EGL_LEVEL, 0,
                EGL10.EGL_SURFACE_TYPE, EGL10.EGL_PBUFFER_BIT,
                EGL10.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfig = new int[1];
        egl.eglChooseConfig(dpy, configAttr, configs, 1, numConfig);
        EGLConfig config = configs[0];

        int[] surfAttr = {
                EGL10.EGL_WIDTH, 64,
                EGL10.EGL_HEIGHT, 64,
                EGL10.EGL_NONE
        };
        EGLSurface surf = egl.eglCreatePbufferSurface(dpy, config, surfAttr);
        final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;  // missing in EGL10
        int[] ctxAttrib = {
                EGL_CONTEXT_CLIENT_VERSION, 1,
                EGL10.EGL_NONE
        };
        EGLContext ctx = egl.eglCreateContext(dpy, config, EGL10.EGL_NO_CONTEXT, ctxAttrib);
        egl.eglMakeCurrent(dpy, surf, surf, ctx);
        int[] maxSize = new int[1];
        GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, maxSize, 0);
        egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(dpy, surf);
        egl.eglDestroyContext(dpy, ctx);
        egl.eglTerminate(dpy);
        return maxSize[0];
    }

    private void streamCreate()
    {
        if(mTempStream != 0)
        {
            BASS.BASS_StreamFree(mTempStream);
            mTempStream = 0;
        }

        if (mPath.equals("potatoboy.m4a"))
            mTempStream = BASS.BASS_StreamCreateFile(new BASS.Asset(sActivity.getAssets(), mPath), 0, 0, BASS.BASS_STREAM_PRESCAN | BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
        else {
            File file = new File(mPath);
            if (file.getParent().equals(getContext().getFilesDir().toString())) {
                mTempStream = BASS.BASS_StreamCreateFile(mPath, 0, 0, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
            } else {
                BASS.BASS_FILEPROCS fileprocs = new BASS.BASS_FILEPROCS() {
                    @Override
                    public boolean FILESEEKPROC(long offset, Object user) {
                        FileChannel fc = (FileChannel) user;
                        try {
                            fc.position(offset);
                            return true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return false;
                    }

                    @Override
                    public int FILEREADPROC(ByteBuffer buffer, int length, Object user) {
                        FileChannel fc = (FileChannel) user;
                        try {
                            return fc.read(buffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return 0;
                    }

                    @Override
                    public long FILELENPROC(Object user) {
                        FileChannel fc = (FileChannel) user;
                        try {
                            return fc.size();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return 0;
                    }

                    @Override
                    public void FILECLOSEPROC(Object user) {
                        FileChannel fc = (FileChannel) user;
                        try {
                            fc.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };

                ContentResolver cr = getContext().getContentResolver();
                try {
                    MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                    params.assetFileDescriptor = cr.openAssetFileDescriptor(Uri.parse(mPath), "r");
                    if (params.assetFileDescriptor != null) {
                        params.fileChannel = params.assetFileDescriptor.createInputStream().getChannel();
                        mTempStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (mTempStream == 0) {
                    try {
                        MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                        params.assetFileDescriptor = cr.openAssetFileDescriptor(Uri.parse(mPath), "r");
                        if (params.assetFileDescriptor != null) {
                            params.fileChannel = params.assetFileDescriptor.createInputStream().getChannel();
                            mTempStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void redrawWaveForm()
    {
        if(mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING)
            mTask.cancel(true);
        if (mPath == null) return;
        streamCreate();
        if(mBitmaps.size() > 0) {
            for(int i = 0; i < mBitmaps.size(); i++) {
                Bitmap bitmap = mBitmaps.get(i);
                bitmap.recycle();
            }
            mBitmaps.clear();
        }
        if(mCanvases.size() > 0) mCanvases.clear();
        int nMaxWidth = (int)(getWidth() * mZoom);
        int nTotalWidth = 0;
        while(nTotalWidth < nMaxWidth) {
            int nWidth = getMaxTextureSize();
            if(nTotalWidth + nWidth > nMaxWidth)
                nWidth = nMaxWidth - nTotalWidth;
            if(nWidth <= 0) break;
            try {
                Bitmap bitmap = Bitmap.createBitmap(nWidth, getHeight(), Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(LoopFragment.sActivity.isDarkMode() ? getResources().getColor(R.color.darkModeBk) : getResources().getColor(R.color.lightModeBk));
                mBitmaps.add(bitmap);
                mCanvases.add(canvas);
                nTotalWidth += nWidth;
            }
            catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        int colorBk = LoopFragment.sActivity.isDarkMode() ? getResources().getColor(R.color.darkModeBk) : getResources().getColor(R.color.lightModeBk);
        int colorWave = LoopFragment.sActivity.isDarkMode() ? Color.argb(255, 47, 86, 119) : Color.argb(255, 128, 166, 199);
        mTask = new WaveViewTask(this, mTempStream, getWidth(), getHeight(), mZoom, mBitmaps, mCanvases, colorBk, colorWave);
        mTask.execute(0);
    }

    public void clearWaveForm(boolean bInvalidate)
    {
        if(mBitmaps.size() > 0) {
            for(int i = 0; i < mBitmaps.size(); i++) {
                Bitmap bitmap = mBitmaps.get(i);
                bitmap.recycle();
            }
            mBitmaps.clear();
        }
        if(mCanvases.size() > 0) mCanvases.clear();
        if(bInvalidate) invalidate();
    }
}
