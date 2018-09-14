/*
 * SongSavingTask
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
import android.support.v7.app.AlertDialog;
import android.widget.ProgressBar;

import com.un4seen.bass.BASS;
import com.un4seen.bass.BASSenc;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

public class SongSavingTask extends AsyncTask<Integer, Integer, Integer> {
    private int nPurpose; // 0: saveSongToLocal, 1: export
    private PlaylistFragment playlistFragment = null;
    private int hTempStream = 0;
    private int hEncode = 0;
    private String strPathTo;
    private AlertDialog alert;
    private double dEnd;

    public SongSavingTask(int nPurpose, PlaylistFragment playlistFragment, int hTempStream, int hEncode, String strPathTo, AlertDialog alert, double dEnd)
    {
        this.nPurpose = nPurpose;
        this.playlistFragment = playlistFragment;
        this.hTempStream = hTempStream;
        this.hEncode = hEncode;
        this.strPathTo = strPathTo;
        this.alert = alert;
        this.dEnd = dEnd;
    }

    @Override
    protected Integer doInBackground(Integer... params)
    {
        while(BASS.BASS_ChannelIsActive(hTempStream) > 0 && BASSenc.BASS_Encode_IsActive(hEncode) > 0)
        {
            ByteBuffer buffer = ByteBuffer.allocate(10000);
            BASS.BASS_ChannelGetData(hTempStream, buffer, 10000 | BASS.BASS_DATA_FLOAT);
            double dPos = BASS.BASS_ChannelBytes2Seconds(hTempStream, BASS.BASS_ChannelGetPosition(hTempStream, BASS.BASS_POS_BYTE));
            if(dPos >= dEnd) break;
            if(playlistFragment.isFinish()) break;

            MainActivity activity = (MainActivity)playlistFragment.getActivity();
            EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
            if(effectFragment.isReverse())
                publishProgress((int)((dEnd - dPos) / dEnd * 100.0));
            else
                publishProgress((int)(dPos / dEnd * 100.0));
        }
        return 0;
    }

    @Override
    protected  void onProgressUpdate(Integer... progress)
    {
        playlistFragment.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(Integer result)
    {
        if(nPurpose == 0)
            playlistFragment.finishSaveSongToLocal(hTempStream, hEncode, strPathTo, alert);
        else
            playlistFragment.finishExport(hTempStream, hEncode, strPathTo, alert);
    }
}
