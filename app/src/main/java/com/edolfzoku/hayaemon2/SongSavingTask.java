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
import androidx.appcompat.app.AlertDialog;

import com.un4seen.bass.BASS;
import com.un4seen.bass.BASSenc;

import java.nio.ByteBuffer;

class SongSavingTask extends AsyncTask<Integer, Integer, Integer> {
    private final int mPurpose; // 0: saveSongToLocal, 1: export, 2: saveSongToGallery
    private final PlaylistFragment mPlaylistFragment;
    private final int mTempStream;
    private final int mEncode;
    private final String mPathTo;
    private final AlertDialog mAlert;
    private final double mEnd;

    SongSavingTask(int nPurpose, PlaylistFragment playlistFragment, int tempStream, int encode, String pathTo, AlertDialog alert, double end)
    {
        mPurpose = nPurpose;
        mPlaylistFragment = playlistFragment;
        mTempStream = tempStream;
        mEncode = encode;
        mPathTo = pathTo;
        mAlert = alert;
        mEnd = end;
    }

    @Override
    protected Integer doInBackground(Integer... params)
    {
        while(BASS.BASS_ChannelIsActive(mTempStream) > 0 && BASSenc.BASS_Encode_IsActive(mEncode) > 0)
        {
            ByteBuffer buffer = ByteBuffer.allocate(10000);
            BASS.BASS_ChannelGetData(mTempStream, buffer, 10000 | BASS.BASS_DATA_FLOAT);
            double dPos = BASS.BASS_ChannelBytes2Seconds(mTempStream, BASS.BASS_ChannelGetPosition(mTempStream, BASS.BASS_POS_BYTE));
            if(dPos >= mEnd) break;
            if(mPlaylistFragment.isFinish()) break;

            MainActivity activity = (MainActivity)mPlaylistFragment.getActivity();
            if(activity == null) return 0;
            if(activity.effectFragment.isReverse())
                publishProgress((int)((mEnd - dPos) / mEnd * 100.0));
            else
                publishProgress((int)(dPos / mEnd * 100.0));
        }
        return 0;
    }

    @Override
    protected  void onProgressUpdate(Integer... progress)
    {
        if(mPurpose == 2)
            mPlaylistFragment.setProgress(progress[0] / 2);
        else
            mPlaylistFragment.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(Integer result)
    {
        if(mPurpose == 0)
            mPlaylistFragment.finishSaveSongToLocal(mTempStream, mEncode, mPathTo, mAlert);
        else if(mPurpose == 1)
            mPlaylistFragment.finishExport(mTempStream, mEncode, mPathTo, mAlert);
        else
            mPlaylistFragment.finishSaveSongToGallery(mTempStream, mEncode, mPathTo, mAlert);
    }
}
