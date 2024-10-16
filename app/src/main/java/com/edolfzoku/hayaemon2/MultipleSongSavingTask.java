/*
 * MultipleSongSavingTask
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

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;

import com.un4seen.bass.BASS;
import com.un4seen.bass.BASSenc;
import com.un4seen.bass.BASSenc_MP3;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class MultipleSongSavingTask extends AsyncTask<Integer, Integer, Integer> {
    static class SongStreamInfo {
        private final int mTempStream;
        private final String mStrPathTo;
        private final double mEnd;

        SongStreamInfo(int tempStream, String strPathTo, double end) {
            mTempStream = tempStream;
            mStrPathTo = strPathTo;
            mEnd = end;
        }
    }

    private final PlaylistFragment mPlaylistFragment;
    private final ArrayList<Uri> mUris;
    private final ArrayList<SongStreamInfo> mSongStreamInfos;
    private final AlertDialog mAlert;

    MultipleSongSavingTask(PlaylistFragment playlistFragment, ArrayList<Uri> uris, AlertDialog alert, ArrayList<SongStreamInfo> songStreamInfos) {
        mPlaylistFragment = playlistFragment;
        mUris = uris;
        mAlert = alert;
        mSongStreamInfos = songStreamInfos;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        for (int i = 0; i < mSongStreamInfos.size(); i++) {
            SongStreamInfo songStreamInfo = mSongStreamInfos.get(i);
            if (songStreamInfo == null) continue;

            int hEncode = BASSenc_MP3.BASS_Encode_MP3_StartFile(songStreamInfo.mTempStream, "", 0, songStreamInfo.mStrPathTo);

            while (BASS.BASS_ChannelIsActive(songStreamInfo.mTempStream) > 0 && BASSenc.BASS_Encode_IsActive(hEncode) > 0) {
                ByteBuffer buffer = ByteBuffer.allocate(10000);
                BASS.BASS_ChannelGetData(songStreamInfo.mTempStream, buffer, 10000 | BASS.BASS_DATA_FLOAT);
                double dPos = BASS.BASS_ChannelBytes2Seconds(songStreamInfo.mTempStream, BASS.BASS_ChannelGetPosition(songStreamInfo.mTempStream, BASS.BASS_POS_BYTE));
                if (dPos >= songStreamInfo.mEnd) break;
                if (PlaylistFragment.sFinish) break;

                MainActivity activity = (MainActivity) mPlaylistFragment.getActivity();
                if (activity == null) return 0;
                int previousProgress = (int) (100.0 / mSongStreamInfos.size() * i);
                if (activity.effectFragment.isReverse())
                    publishProgress((int) ((songStreamInfo.mEnd - dPos) / songStreamInfo.mEnd * 100.0 / mSongStreamInfos.size() + previousProgress));
                else
                    publishProgress((int) (dPos / songStreamInfo.mEnd * 100.0 / mSongStreamInfos.size() + previousProgress));
            }

            BASSenc.BASS_Encode_Stop(hEncode);
            BASS.BASS_StreamFree(songStreamInfo.mTempStream);
        }
        return 0;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        mPlaylistFragment.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(Integer result) {
        List<Boolean> isNeededDeleteFile;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            isNeededDeleteFile = mSongStreamInfos.stream().map(Objects::nonNull).collect(Collectors.toList());
        } else {
            List<Boolean> _isNeedDeleteFile = new ArrayList<>();
            for (SongStreamInfo songStreamInfo : mSongStreamInfos) {
                _isNeedDeleteFile.add(songStreamInfo != null);
            }
            isNeededDeleteFile = _isNeedDeleteFile;
        }

        mPlaylistFragment.finishExportMultipleSelection(mUris, mAlert, isNeededDeleteFile);
    }
}
