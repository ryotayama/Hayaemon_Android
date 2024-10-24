/*
 * SongSavingTask
 *
 * Copyright (c) 2019 Ryota Yamauchi. All rights reserved.
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
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.appcompat.app.AlertDialog;

import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Locale;

class VideoSavingTask extends AsyncTask<Integer, Integer, Integer> {
    private final PlaylistFragment mPlaylistFragment;
    private final String mPathTo;
    private final AlertDialog mAlert;
    private final int mLength, mChans, mSampleRate;
    private String mMP4Path;

    VideoSavingTask(PlaylistFragment playlistFragment, String pathTo, AlertDialog alert, int length, int chans, int sampleRate) {
        mPlaylistFragment = playlistFragment;
        mPathTo = pathTo;
        mAlert = alert;
        mLength = length;
        mChans = chans;
        mSampleRate = sampleRate;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Integer doInBackground(Integer... params)
    {
        MediaCodec codec = null;
        MediaMuxer mux;
        File inputFile = new File(mPathTo);
        File inputMp4File = null;
        FileInputStream fis;
        try {
            fis = new FileInputStream(inputFile);
            if(fis.skip(44) != 44) System.out.println("44バイトのスキップに失敗しました");

            String strTempPath = mPlaylistFragment.getActivity().getExternalCacheDir() + "/temp.mp4";
            inputMp4File = new File(strTempPath);
            if (inputMp4File.exists()) {
                if(!inputMp4File.delete()) System.out.println("ファイルが削除できませんでした");
            }
            SeekableByteChannel out = NIOUtils.writableFileChannel(strTempPath);
            AndroidSequenceEncoder encoder = new AndroidSequenceEncoder(out, Rational.R(1, 1));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            Bitmap bitmap = BitmapFactory.decodeResource(mPlaylistFragment.getResources(), R.drawable.cameraroll, options);
            encoder.encodeImage(bitmap);
            encoder.encodeImage(bitmap);
            bitmap.recycle();
            encoder.finish();
            NIOUtils.closeQuietly(out);

            ArrayList<SongItem> arSongs = PlaylistFragment.sPlaylists.get(PlaylistFragment.sSelectedPlaylist);
            SongItem item = arSongs.get(PlaylistFragment.sSelectedItem);
            String strTitle = item.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_");
            Uri mpegUri = Uri.EMPTY;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                ContentResolver contentResolver = mPlaylistFragment.getActivity().getContentResolver();
                values.put(MediaStore.Video.Media.DISPLAY_NAME, strTitle + ".mp4");
                values.put(MediaStore.Video.Media.TITLE, strTitle);
                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
                values.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Hayaemon");
                values.put(MediaStore.Video.Media.IS_PENDING, 1);
                mpegUri = contentResolver.insert(MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values);
                if (mpegUri == null) return 0;
                FileDescriptor fd = contentResolver.openFileDescriptor(mpegUri, "rw").getFileDescriptor();
                if (fd == null) return 0;
                mux = new MediaMuxer(fd, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            }
            else {
                mMP4Path = Environment.getExternalStorageDirectory() + "/" + strTitle + ".mp4";
                File outputFile = new File(mMP4Path);
                if (outputFile.exists()) {
                    int i = 2;
                    File fileForCheck;
                    String strTemp;
                    while (true) {
                        strTemp = Environment.getExternalStorageDirectory() + "/" + strTitle + String.format(Locale.getDefault(), "%d", i) + ".mp4";
                        fileForCheck = new File(strTemp);
                        if (!fileForCheck.exists()) break;
                        i++;
                    }
                    mMP4Path = Environment.getExternalStorageDirectory() + "/" + strTitle + String.format(Locale.getDefault(), "%d", i) + ".mp4";
                    outputFile = new File(mMP4Path);
                }

                mux = new MediaMuxer(outputFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            }

            MediaExtractor videoExtractor = new MediaExtractor();
            videoExtractor.setDataSource(strTempPath);
            videoExtractor.selectTrack(0);
            MediaFormat videoFormat = videoExtractor.getTrackFormat(0);

            codec = MediaCodec.createEncoderByType("audio/mp4a-latm");
            MediaFormat outputFormat = new MediaFormat();
            outputFormat.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
            outputFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, mChans);
            outputFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, mSampleRate);
            outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, 128 * 1024);
            outputFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);

            codec.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            codec.start();

            MediaCodec.BufferInfo outBuffInfo = new MediaCodec.BufferInfo();
            byte[] tempBuffer = new byte[44100];
            boolean hasMoreData = true;
            double presentationTimeUs = 0;
            int audioTrackIdx = 0;
            int videoTrackIdx = 0;
            int totalBytesRead = 0;
            do {
                if(PlaylistFragment.sFinish) break;
                int inputBufIndex = 0;
                while (inputBufIndex != -1 && hasMoreData) {
                    if(PlaylistFragment.sFinish) break;
                    inputBufIndex = codec.dequeueInputBuffer(5000);

                    if (inputBufIndex >= 0) {
                        ByteBuffer dstBuf = codec.getInputBuffer(inputBufIndex);
                        dstBuf.clear();

                        int bytesRead = fis.read(tempBuffer, 0, dstBuf.limit());
                        if (bytesRead == -1) { // -1 implies EOS
                            hasMoreData = false;
                            codec.queueInputBuffer(inputBufIndex, 0, 0, (long) presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        } else {
                            totalBytesRead += bytesRead;
                            dstBuf.put(tempBuffer, 0, bytesRead);
                            codec.queueInputBuffer(inputBufIndex, 0, bytesRead, (long) presentationTimeUs, 0);
                            presentationTimeUs = 1000000L * (totalBytesRead / 2 / mChans) / 44100;
                        }
                    }
                }
                int outputBufIndex = 0;
                while (outputBufIndex != MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if(PlaylistFragment.sFinish) break;
                    outputBufIndex = codec.dequeueOutputBuffer(outBuffInfo, 5000);
                    if (outputBufIndex >= 0) {
                        ByteBuffer encodedData = codec.getOutputBuffer(outputBufIndex);
                        encodedData.position(outBuffInfo.offset);
                        encodedData.limit(outBuffInfo.offset + outBuffInfo.size);
                        if ((outBuffInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0 && outBuffInfo.size != 0) {
                            codec.releaseOutputBuffer(outputBufIndex, false);
                        }else{
                            int outBitsSize   = outBuffInfo.size;
                            byte[] data = new byte[outBitsSize];
                            encodedData.get(data, 0, outBitsSize);
                            encodedData.position(outBuffInfo.offset);
                            encodedData.limit(outBuffInfo.offset + outBuffInfo.size);
                            mux.writeSampleData(audioTrackIdx, encodedData, outBuffInfo);
                            encodedData.clear();
                            codec.releaseOutputBuffer(outputBufIndex, false);
                        }
                    } else if (outputBufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        outputFormat = codec.getOutputFormat();
                        audioTrackIdx = mux.addTrack(outputFormat);
                        videoTrackIdx = mux.addTrack(videoFormat);
                        mux.start();
                    }
                }
                int nComplete = (int) Math.round(((float) totalBytesRead / (float) inputFile.length()) * 100.0 / 4.0);
                publishProgress(50 + nComplete);
            } while (outBuffInfo.flags != MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            fis.close();

            int offset = 0;
            int sampleSize = 1024 * 1024;
            ByteBuffer videoBuf = ByteBuffer.allocate(sampleSize);
            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            long videoPresentationTimeUs = 0;
            long lastEndVideoTimeUs = 0;
            while (true) {
                if(PlaylistFragment.sFinish) break;
                videoBufferInfo.offset = offset;
                int readVideoSampleSize = videoExtractor.readSampleData(videoBuf, offset);
                if ((videoBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    videoBufferInfo.size = 0;
                } else if (readVideoSampleSize  < 0) {
                    videoExtractor.unselectTrack(0);
                    if (videoPresentationTimeUs >= presentationTimeUs) {
                        break;
                    }
                    else {
                        lastEndVideoTimeUs = videoPresentationTimeUs;
                        videoExtractor.selectTrack(0);
                        continue;
                    }
                } else {
                    long videoSampleTime = videoExtractor.getSampleTime();
                    videoBufferInfo.size = readVideoSampleSize;
                    videoBufferInfo.presentationTimeUs = videoSampleTime + lastEndVideoTimeUs;
                    if (videoBufferInfo.presentationTimeUs > presentationTimeUs) {
                        videoExtractor.unselectTrack(0);
                        break;
                    }
                    videoPresentationTimeUs = videoBufferInfo.presentationTimeUs;
                    videoBufferInfo.offset = 0;
                    videoBufferInfo.flags = videoExtractor.getSampleFlags();

                    mux.writeSampleData(videoTrackIdx, videoBuf, videoBufferInfo);
                    videoExtractor.advance();
                }
                int nComplete = (int) Math.round(((float) videoPresentationTimeUs / (float) presentationTimeUs) * 100.0 / 4.0);
                publishProgress(75 + nComplete);
            }
            mux.stop();
            mux.release();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                ContentResolver contentResolver = mPlaylistFragment.getActivity().getContentResolver();
                values.put(MediaStore.Video.Media.IS_PENDING, 0);
                contentResolver.update(mpegUri, values, null, null);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            if(codec != null) {
                codec.flush();
                codec.stop();
                codec.release();
            }
            if (inputFile.exists()) {
                if(!inputFile.delete()) System.out.println("ファイルが削除できませんでした");
            }
            if (inputMp4File != null && inputMp4File.exists()) {
                if(!inputMp4File.delete()) System.out.println("ファイルが削除できませんでした");
            }
        }

        return 0;
    }

    @Override
    protected  void onProgressUpdate(Integer... progress)
    {
        mPlaylistFragment.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(Integer result)
    {
        mPlaylistFragment.finishSaveSongToGallery2(mLength, mMP4Path, mAlert, mPathTo);
    }
}
