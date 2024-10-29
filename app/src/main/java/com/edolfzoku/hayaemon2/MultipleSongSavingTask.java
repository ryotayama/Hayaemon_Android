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

import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS_FX;
import com.un4seen.bass.BASSenc;
import com.un4seen.bass.BASSenc_MP3;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
    private final HashMap<SongItem, Integer> mSelectedSongs;
    private final AlertDialog mAlert;
    private final ArrayList<MultipleSongSavingTask.SongStreamInfo> mSongStreamInfos = new ArrayList<>();
    private final ArrayList<Uri> mUris = new ArrayList<>();

    private int mProgress = 0;
    private int mPreviousProgress = 0;

    MultipleSongSavingTask(PlaylistFragment playlistFragment, HashMap<SongItem, Integer> selectedSongs, AlertDialog alert) {
        mPlaylistFragment = playlistFragment;
        mSelectedSongs = selectedSongs;
        mAlert = alert;
    }

    @Override
    protected Integer doInBackground(Integer... _params) {
        int j = 0;
        for (SongItem item : mSelectedSongs.keySet()) {
            String strPath = item.getPath();
            Uri uri = Uri.parse(strPath);
            int sSongIndex = mSelectedSongs.get(item);
            EffectSaver saver = PlaylistFragment.sEffects.get(PlaylistFragment.sSelectedPlaylist).get(sSongIndex);
            if (mPlaylistFragment.hasActiveEffectOrEq(sSongIndex) || strPath.equals("potatoboy.m4a")) {
                int _hTempStream;
                if (uri.getScheme() != null && uri.getScheme().equals("content")) {
                    ContentResolver cr = PlaylistFragment.sActivity.getApplicationContext().getContentResolver();
                    try {
                        MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                        params.assetFileDescriptor = cr.openAssetFileDescriptor(Uri.parse(strPath), "r");
                        if (params.assetFileDescriptor == null) return 0;
                        params.fileChannel = params.assetFileDescriptor.createInputStream().getChannel();
                        _hTempStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return 0;
                    }
                    if (_hTempStream == 0) {
                        try {
                            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                            params.assetFileDescriptor = cr.openAssetFileDescriptor(Uri.parse(strPath), "r");
                            if (params.assetFileDescriptor == null) return 0;
                            params.fileChannel = params.assetFileDescriptor.createInputStream().getChannel();
                            _hTempStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return 0;
                        }
                    }
                } else {
                    if (strPath.equals("potatoboy.m4a"))
                        _hTempStream = BASS.BASS_StreamCreateFile(new BASS.Asset(PlaylistFragment.sActivity.getAssets(), strPath), 0, 0, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
                    else
                        _hTempStream = BASS.BASS_StreamCreateFile(strPath, 0, 0, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
                }
                if (_hTempStream == 0) return 0;

                _hTempStream = BASS_FX.BASS_FX_ReverseCreate(_hTempStream, 2, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
                _hTempStream = BASS_FX.BASS_FX_TempoCreate(_hTempStream, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
                final int hTempStream = _hTempStream;
                int chan = BASS_FX.BASS_FX_TempoGetSource(hTempStream);
                if (sSongIndex == PlaylistFragment.sPlaying ? EffectFragment.isReverse() : saver.isSave() && saver.getEffectItems().get(EffectFragment.EFFECTTYPE_REVERSE).isSelected())
                    BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_REVERSE);
                else
                    BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_FORWARD);
                int hTempFxVol = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_VOLUME, 0);
                int hTempFx20K = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx16K = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx12_5K = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx10K = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx8K = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx6_3K = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx5K = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx4K = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx3_15K = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx2_5K = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx2K = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx1_6K = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx1_25K = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx1K = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx800 = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx630 = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx500 = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx400 = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx315 = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx250 = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx200 = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx160 = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx125 = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx100 = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx80 = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx63 = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx50 = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx40 = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx31_5 = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx25 = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                int hTempFx20 = BASS.BASS_ChannelSetFX(hTempStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
                BASS.BASS_ChannelSetAttribute(hTempStream, BASS_FX.BASS_ATTRIB_TEMPO,
                        sSongIndex == PlaylistFragment.sPlaying ? ControlFragment.sSpeed : saver.isSave() ? saver.getSpeed() : 0.0f);
                BASS.BASS_ChannelSetAttribute(hTempStream, BASS_FX.BASS_ATTRIB_TEMPO_PITCH,
                        sSongIndex == PlaylistFragment.sPlaying ? ControlFragment.sPitch : saver.isSave() ? saver.getPitch() : 0.0f);
                int[] arHFX = new int[]{hTempFx20K, hTempFx16K, hTempFx12_5K, hTempFx10K, hTempFx8K, hTempFx6_3K, hTempFx5K, hTempFx4K, hTempFx3_15K, hTempFx2_5K, hTempFx2K, hTempFx1_6K, hTempFx1_25K, hTempFx1K, hTempFx800, hTempFx630, hTempFx500, hTempFx400, hTempFx315, hTempFx250, hTempFx200, hTempFx160, hTempFx125, hTempFx100, hTempFx80, hTempFx63, hTempFx50, hTempFx40, hTempFx31_5, hTempFx25, hTempFx20};
                float fLevel = sSongIndex == PlaylistFragment.sPlaying ? PlaylistFragment.sActivity.equalizerFragment.getSeeks().get(0).getProgress() / 100.0f : saver.isSave() ? saver.getVol() / 100.0f : 1.0f;
                BASS_FX.BASS_BFX_VOLUME vol = new BASS_FX.BASS_BFX_VOLUME();
                vol.lChannel = 0;
                vol.fVolume = fLevel;
                BASS.BASS_FXSetParameters(hTempFxVol, vol);

                List<Integer> saverEqs = null;
                if (saver.isSave()) {
                    saverEqs = Arrays.asList(saver.getEQ20K(), saver.getEQ16K(), saver.getEQ12_5K(), saver.getEQ10K(), saver.getEQ8K(), saver.getEQ6_3K(), saver.getEQ5K(), saver.getEQ4K(), saver.getEQ3_15K(), saver.getEQ2_5K(), saver.getEQ2K(), saver.getEQ1_6K(), saver.getEQ1_25K(), saver.getEQ1K(), saver.getEQ800(), saver.getEQ630(), saver.getEQ500(), saver.getEQ400(), saver.getEQ315(), saver.getEQ250(), saver.getEQ200(), saver.getEQ160(), saver.getEQ125(), saver.getEQ100(), saver.getEQ80(), saver.getEQ63(), saver.getEQ50(), saver.getEQ40(), saver.getEQ31_5(), saver.getEQ25(), saver.getEQ20());
                }
                for (int i = 0; i < 31; i++) {
                    int nLevel = sSongIndex == PlaylistFragment.sPlaying ? PlaylistFragment.sActivity.equalizerFragment.getSeeks().get(i + 1).getProgress() - 30 : saverEqs != null ? saverEqs.get(i) : 0;
                    BASS_FX.BASS_BFX_PEAKEQ eq = new BASS_FX.BASS_BFX_PEAKEQ();
                    eq.fBandwidth = 0.7f;
                    eq.fQ = 0.0f;
                    eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
                    eq.fGain = nLevel;
                    eq.fCenter = PlaylistFragment.sActivity.equalizerFragment.getArCenters()[i];
                    BASS.BASS_FXSetParameters(arHFX[i], eq);
                }
                EffectFragment.applyEffect(hTempStream, item);
                mPlaylistFragment.makeAndClearExportDirIfNeeded();
                String strPathTo = PlaylistFragment.sActivity.getExternalCacheDir() + "/export/";
                strPathTo += item.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_") + ".mp3";
                File file = new File(strPathTo);

                double _dEnd = BASS.BASS_ChannelBytes2Seconds(hTempStream, BASS.BASS_ChannelGetLength(hTempStream, BASS.BASS_POS_BYTE));
                if (PlaylistFragment.sSelectedPlaylist == PlaylistFragment.sPlayingPlaylist) {
                    if (sSongIndex == PlaylistFragment.sPlaying) {
                        if (MainActivity.sLoopA)
                            BASS.BASS_ChannelSetPosition(hTempStream, BASS.BASS_ChannelSeconds2Bytes(hTempStream, MainActivity.sLoopAPos), BASS.BASS_POS_BYTE);
                        if (MainActivity.sLoopB)
                            _dEnd = MainActivity.sLoopBPos;
                    } else if (saver.isSave()) {
                        if (saver.isLoopA()) {
                            BASS.BASS_ChannelSetPosition(hTempStream, BASS.BASS_ChannelSeconds2Bytes(hTempStream, saver.getLoopA()), BASS.BASS_POS_BYTE);
                        }
                        if (saver.isLoopB())
                            _dEnd = saver.getLoopB();
                    }
                }
                final double dEnd = _dEnd;

                mUris.add(FileProvider.getUriForFile(PlaylistFragment.sActivity, "com.edolfzoku.hayaemon2", file));
                mSongStreamInfos.add(new MultipleSongSavingTask.SongStreamInfo(hTempStream, strPathTo, dEnd));
            }
            else {
                mProgress = (int) (100.0 / mSelectedSongs.size() * j++ + 1);
                publishProgress(mProgress);
                String filename = "export/" + item.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_") + mPlaylistFragment.getAudioFileExtension(uri);
                uri = PlaylistFragment.sActivity.copyTempFileAs(uri, filename);
                if (uri != null)
                    mUris.add(FileProvider.getUriForFile(PlaylistFragment.sActivity, "com.edolfzoku.hayaemon2", new File(uri.getPath())));
                else
                    mUris.add(null);
                mSongStreamInfos.add(null);
            }
        }

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
                int progress;
                if (EffectFragment.isReverse()) {
                    progress = mProgress + (int) ((songStreamInfo.mEnd - dPos) / songStreamInfo.mEnd * 100.0 / mSongStreamInfos.size());
                } else {
                    progress = mProgress + (int) (dPos / songStreamInfo.mEnd * 100.0 / mSongStreamInfos.size());
                }

                if (mPreviousProgress != progress) {
                    publishProgress(progress);
                    mPreviousProgress = progress;
                }
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
