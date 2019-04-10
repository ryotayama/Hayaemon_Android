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
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.opengl.GLES10;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;

import com.un4seen.bass.BASS;

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

/**
 * Created by yamauchiryouta on 2017/10/03.
 */

public class WaveView extends View {

    private LoopFragment loopFragment = null;
    private int hTempStream = 0;
    private ArrayList<Bitmap> arBitmaps = null;
    private ArrayList<Canvas> arCanvases = null;
    private String strPath = null;
    private float fZoom = 1.0f;
    private WaveViewTask task = null;
    private Rect srcRect = null;
    private Rect dstRect = null;

    public LoopFragment getLoopFragment() { return loopFragment; }
    public void setLoopFragment(LoopFragment loopFragment) { this.loopFragment = loopFragment; }
    public float getZoom() { return fZoom; }
    public void setZoom(float fZoom) {
        if(this.fZoom == fZoom) return;
        if(fZoom < 1.0f) fZoom = 1.0f;
        else if(fZoom > 10.0f) fZoom = 10.0f;
        this.fZoom = fZoom;
    }

    public WaveView(Context context) {
        super(context);
        setWillNotDraw(false);
        arBitmaps = new ArrayList<>();
        arCanvases = new ArrayList<>();
        srcRect = new Rect();
        dstRect = new Rect();
    }

    public WaveView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setWillNotDraw(false);
        arBitmaps = new ArrayList<>();
        arCanvases = new ArrayList<>();
        srcRect = new Rect();
        dstRect = new Rect();
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
        if(arBitmaps.size() == 0) {
            return;
        }
        long nLength = BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE);
        long nPos = BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE);
        int nScreenWidth = getWidth();
        int nMaxWidth = (int)(nScreenWidth * fZoom);
        int nLeft = (int) (nMaxWidth * nPos / nLength);
        if(nLeft < nScreenWidth / 2) {
            srcRect.set(0, 0, getWidth(), getHeight());
            dstRect.set(0, 0, getWidth(), getHeight());
            canvas.drawBitmap(arBitmaps.get(0), srcRect, dstRect, null);
        }
        else if(nScreenWidth / 2 <= nLeft && nLeft < nMaxWidth - nScreenWidth / 2) {
            int nStart = nLeft - nScreenWidth / 2;
            int nEnd = nLeft + nScreenWidth / 2;
            int nTotalWidth = 0;
            int nPaintLeft = 0;
            for(int i = 0; i < arBitmaps.size(); i++) {
                Bitmap bitmap = arBitmaps.get(i);
                int nBitmapStart = nTotalWidth;
                int nBitmapEnd = nTotalWidth + bitmap.getWidth();
                if(nStart <= nBitmapEnd && nBitmapStart <= nEnd) {
                    int nX = 0;
                    int nWidth = getWidth();
                    if(nPaintLeft == 0) {
                        nX = nStart - nTotalWidth;
                        if(nX + nWidth > bitmap.getWidth()) nWidth = bitmap.getWidth() - nX;
                        srcRect.set(nX, 0, nX + nWidth, getHeight());
                        dstRect.set(nPaintLeft, 0, nPaintLeft + nWidth, getHeight());
                        canvas.drawBitmap(bitmap, srcRect, dstRect, null);
                    }
                    else
                        nWidth -= nPaintLeft;
                    srcRect.set(nX, 0, nX + nWidth, getHeight());
                    dstRect.set(nPaintLeft, 0, nPaintLeft + nWidth, getHeight());
                    canvas.drawBitmap(bitmap, srcRect, dstRect, null);
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
            for(int i = 0; i < arBitmaps.size(); i++) {
                Bitmap bitmap = arBitmaps.get(i);
                int nBitmapStart = nTotalWidth;
                int nBitmapEnd = nTotalWidth + bitmap.getWidth();
                if(nStart <= nBitmapEnd && nBitmapStart <= nEnd) {
                    int nX = 0;
                    int nWidth = getWidth();
                    if(nPaintLeft == 0) {
                        nX = nStart - nTotalWidth;
                        if(nX + nWidth > bitmap.getWidth()) nWidth = bitmap.getWidth() - nX;
                        srcRect.set(nX, 0, nX + nWidth, getHeight());
                        dstRect.set(nPaintLeft, 0, nPaintLeft + nWidth, getHeight());
                        canvas.drawBitmap(bitmap, srcRect, dstRect, null);
                    }
                    else
                        nWidth -= nPaintLeft;
                    srcRect.set(nX, 0, nX + nWidth, getHeight());
                    dstRect.set(nPaintLeft, 0, nPaintLeft + nWidth, getHeight());
                    canvas.drawBitmap(bitmap, srcRect, dstRect, null);
                    nPaintLeft += nWidth;
                }
                nTotalWidth += bitmap.getWidth();
            }
        }
        if(getScaleX() != 1.0f) setScaleX(1.0f);
    }

    public void drawWaveForm(String strPath)
    {
        if(task != null && task.getStatus() == AsyncTask.Status.RUNNING)
            task.cancel(true);
        this.strPath = strPath;
        streamCreate();
        if(arBitmaps.size() > 0) {
            for(int i = 0; i < arBitmaps.size(); i++) {
                Bitmap bitmap = arBitmaps.get(i);
                bitmap.recycle();
            }
            arBitmaps.clear();
        }
        if(arCanvases.size() > 0) arCanvases.clear();
        int nMaxWidth = (int)(getWidth() * fZoom);
        int nTotalWidth = 0;
        clearWaveForm(false);
        while(nTotalWidth < nMaxWidth) {
            int nWidth = getMaxTextureSize();
            if(nTotalWidth + nWidth > nMaxWidth)
                nWidth = nMaxWidth - nTotalWidth;
            Bitmap bitmap = Bitmap.createBitmap(nWidth, getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            arBitmaps.add(bitmap);
            arCanvases.add(canvas);
            nTotalWidth += nWidth;
        }
        task = new WaveViewTask(this, hTempStream, getWidth(), getHeight(), fZoom, arBitmaps, arCanvases);
        task.execute(0);
    }

    private int getMaxTextureSize()
    {
        EGL10 egl = (EGL10) EGLContext.getEGL();

        EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        int[] vers = new int[2];
        egl.eglInitialize(dpy, vers);

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
        if(hTempStream != 0)
        {
            BASS.BASS_StreamFree(hTempStream);
            hTempStream = 0;
        }

        File file = new File(strPath);
        if(file.getParent().equals(getContext().getFilesDir().toString()))
        {
            hTempStream = BASS.BASS_StreamCreateFile(strPath, 0, 0, BASS.BASS_STREAM_DECODE);
        }
        else
        {
            BASS.BASS_FILEPROCS fileprocs=new BASS.BASS_FILEPROCS() {
                @Override
                public boolean FILESEEKPROC(long offset, Object user) {
                    FileChannel fc=(FileChannel)user;
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
                    FileChannel fc=(FileChannel)user;
                    try {
                        return fc.read(buffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }

                @Override
                public long FILELENPROC(Object user) {
                    FileChannel fc=(FileChannel)user;
                    try {
                        return fc.size();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }

                @Override
                public void FILECLOSEPROC(Object user) {
                    FileChannel fc=(FileChannel)user;
                    try {
                        fc.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            try {
                mmr.setDataSource(getContext(), Uri.parse(strPath));
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            ContentResolver cr = getContext().getContentResolver();
            try {
                AssetFileDescriptor afd = cr.openAssetFileDescriptor(Uri.parse(strPath), "r");
                if(afd != null) {
                    FileChannel fc = afd.createInputStream().getChannel();
                    hTempStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS.BASS_STREAM_DECODE, fileprocs, fc);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void redrawWaveForm()
    {
        if(task != null && task.getStatus() == AsyncTask.Status.RUNNING)
            task.cancel(true);
        streamCreate();
        if(arBitmaps.size() > 0) {
            for(int i = 0; i < arBitmaps.size(); i++) {
                Bitmap bitmap = arBitmaps.get(i);
                bitmap.recycle();
            }
            arBitmaps.clear();
        }
        if(arCanvases.size() > 0) arCanvases.clear();
        int nMaxWidth = (int)(getWidth() * fZoom);
        int nTotalWidth = 0;
        while(nTotalWidth < nMaxWidth) {
            int nWidth = getMaxTextureSize();
            if(nTotalWidth + nWidth > nMaxWidth)
                nWidth = nMaxWidth - nTotalWidth;
            Bitmap bitmap = Bitmap.createBitmap(nWidth, getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            arBitmaps.add(bitmap);
            arCanvases.add(canvas);
            nTotalWidth += nWidth;
        }
        task = new WaveViewTask(this, hTempStream, getWidth(), getHeight(), fZoom, arBitmaps, arCanvases);
        task.execute(0);
    }

    public void clearWaveForm(boolean bInvalidate)
    {
        if(arBitmaps.size() > 0) {
            for(int i = 0; i < arBitmaps.size(); i++) {
                Bitmap bitmap = arBitmaps.get(i);
                bitmap.recycle();
            }
            arBitmaps.clear();
        }
        if(arCanvases.size() > 0) arCanvases.clear();
        if(bInvalidate) invalidate();
    }
}
