/*
 * PlaylistFragment
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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS_AAC;
import com.un4seen.bass.BASS_FX;
import com.un4seen.bass.BASSenc;
import com.un4seen.bass.BASSenc_MP3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.text.DateFormat;

import static android.app.Activity.RESULT_OK;

public class PlaylistFragment extends Fragment implements View.OnClickListener {
    private ArrayList<String> arPlaylistNames;
    private  ArrayList<ArrayList<SongItem>> arPlaylists;
    private ArrayList<ArrayList<EffectSaver>> arEffects;
    private ArrayList<ArrayList<String>> arLyrics;
    private int hFx20K, hFx16K, hFx12_5K, hFx10K, hFx8K, hFx6_3K, hFx5K, hFx4K, hFx3_15K, hFx2_5K, hFx2K, hFx1_6K, hFx1_25K, hFx1K, hFx800, hFx630, hFx500, hFx400, hFx315, hFx250, hFx200, hFx160, hFx125, hFx100, hFx80, hFx63, hFx50, hFx40, hFx31_5, hFx25, hFx20;
    private List<Boolean> arPlayed;
    private RecyclerView recyclerPlaylists;
    private RecyclerView recyclerTab;
    private RecyclerView recyclerSongs;
    private PlaylistsAdapter playlistsAdapter;
    private PlaylistTabAdapter tabAdapter;
    private SongsAdapter songsAdapter;
    private ItemTouchHelper playlistTouchHelper;
    private ItemTouchHelper songTouchHelper;
    private MainActivity activity;
    private int nPlayingPlaylist = -1;
    private int nSelectedPlaylist = 0;
    private int nPlaying;
    private int nSelectedItem;
    private boolean bSorting = false;
    private int hRecord;
    private ByteBuffer recbuf;
    private SongSavingTask task;
    private boolean bFinish = false;
    private ProgressBar progress;

    public ArrayList<ArrayList<SongItem>> getArPlaylists() { return arPlaylists; }
    public void setArPlaylists(ArrayList<ArrayList<SongItem>> arLists) { arPlaylists = arLists; }
    public ArrayList<ArrayList<EffectSaver>> getArEffects() { return arEffects; }
    public void setArEffects(ArrayList<ArrayList<EffectSaver>> arEffects) { this.arEffects = arEffects; }
    public ArrayList<ArrayList<String>> getArLyrics() { return arLyrics; }
    public void setArLyrics(ArrayList<ArrayList<String>> arLyrics) { this.arLyrics = arLyrics; }
    public ArrayList<String> getArPlaylistNames() { return arPlaylistNames; }
    public void setArPlaylistNames(ArrayList<String> arNames) { arPlaylistNames = arNames; }
    public int getSelectedPlaylist() { return nSelectedPlaylist; }
    public int getPlaying() { return nPlaying; }
    public int getPlayingPlaylist() { return nPlayingPlaylist; }
    public ItemTouchHelper getPlaylistTouchHelper() { return playlistTouchHelper; }
    public ItemTouchHelper getSongTouchHelper() { return songTouchHelper; }
    public boolean isSorting() { return bSorting; }
    public void setPlayingPlaylist(int nPlaylist) { nPlayingPlaylist = nPlaylist; }
    public int getSongCount(int nPlaylist) { return arPlaylists.get(nPlaylist).size(); }
    public boolean isFinish() { return bFinish; }
    public void setProgress(int nProgress) { progress.setProgress(nProgress); }
    public boolean isLock(int nSong) {
        ArrayList<EffectSaver> arEffectSavers = arEffects.get(nSelectedPlaylist);
        EffectSaver saver = arEffectSavers.get(nSong);
        return saver.isSave();
    }

    public PlaylistFragment()
    {
        activity = null;
        nPlaying = -1;
        arPlaylistNames = new ArrayList<>();
        arPlaylists = new ArrayList<>();
        arEffects = new ArrayList<>();
        arLyrics = new ArrayList<>();
        arPlayed = new ArrayList<>();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context != null && context instanceof MainActivity) {
            activity = (MainActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        activity = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tabAdapter = new PlaylistTabAdapter(activity, R.layout.playlist_tab_item, arPlaylistNames);
        playlistsAdapter = new PlaylistsAdapter(activity, R.layout.playlist_item, arPlaylistNames);
        if(nSelectedPlaylist < arPlaylists.size()) {
            songsAdapter = new SongsAdapter(activity, R.layout.song_item, arPlaylists.get(nSelectedPlaylist));
        }
        else {
            songsAdapter = new SongsAdapter(activity, R.layout.song_item);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnSortPlaylist)
        {
            if(bSorting)
            {
                recyclerPlaylists.setPadding(0, 0, 0, (int)(80 * getResources().getDisplayMetrics().density + 0.5));
                TextView textAddPlaylist = (TextView) activity.findViewById(R.id.textAddPlaylist);
                textAddPlaylist.setVisibility(View.VISIBLE);
                bSorting = false;
                playlistsAdapter.notifyDataSetChanged();
                Button btnSortPlaylist = (Button) activity.findViewById(R.id.btnSortPlaylist);
                btnSortPlaylist.setText("並べ替え");

                playlistTouchHelper.attachToRecyclerView(null);
            }
            else
            {
                recyclerPlaylists.setPadding(0, 0, 0, 0);
                TextView textAddPlaylist = (TextView) activity.findViewById(R.id.textAddPlaylist);
                textAddPlaylist.setVisibility(View.GONE);
                bSorting = true;
                playlistsAdapter.notifyDataSetChanged();
                Button btnSortPlaylist = (Button) activity.findViewById(R.id.btnSortPlaylist);
                btnSortPlaylist.setText("並べ替えを終了");

                playlistTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
                    @Override
                    public boolean onMove(RecyclerView recyclerSongs, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        final int fromPos = viewHolder.getAdapterPosition();
                        final int toPos = target.getAdapterPosition();

                        ArrayList<SongItem> arSongsTemp = arPlaylists.get(fromPos);
                        arPlaylists.remove(fromPos);
                        arPlaylists.add(toPos, arSongsTemp);

                        ArrayList<EffectSaver> arEffectSavers = arEffects.get(fromPos);
                        arEffects.remove(fromPos);
                        arEffects.add(toPos, arEffectSavers);

                        ArrayList<String> arTempLyrics = arLyrics.get(fromPos);
                        arLyrics.remove(fromPos);
                        arLyrics.add(toPos, arTempLyrics);

                        String strTemp = arPlaylistNames.get(fromPos);
                        arPlaylistNames.remove(fromPos);
                        arPlaylistNames.add(toPos, strTemp);

                        if(fromPos == nPlayingPlaylist) nPlayingPlaylist = toPos;
                        else if(fromPos < nPlayingPlaylist && nPlayingPlaylist <= toPos) nPlayingPlaylist--;
                        else if(fromPos > nPlayingPlaylist && nPlayingPlaylist >= toPos) nPlayingPlaylist++;

                        tabAdapter.notifyItemMoved(fromPos, toPos);
                        playlistsAdapter.notifyItemMoved(fromPos, toPos);

                        return true;
                    }

                    @Override
                    public void clearView(RecyclerView recyclerSongs, RecyclerView.ViewHolder viewHolder) {
                        super.clearView(recyclerSongs, viewHolder);

                        tabAdapter.notifyDataSetChanged();
                        playlistsAdapter.notifyDataSetChanged();

                        SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                        Gson gson = new Gson();
                        preferences.edit().putString("arPlaylists", gson.toJson(arPlaylists)).commit();
                        preferences.edit().putString("arEffects", gson.toJson(arEffects)).commit();
                        preferences.edit().putString("arLyrics", gson.toJson(arLyrics)).commit();
                        preferences.edit().putString("arPlaylistNames", gson.toJson(arPlaylistNames)).commit();
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    }
                });
                playlistTouchHelper.attachToRecyclerView(recyclerPlaylists);
            }
        }
        else if(v.getId() == R.id.textAddPlaylist)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("新しいリストを追加する");
            final EditText editText = new EditText (activity);
            editText.setHint("再生リスト");
            editText.setHintTextColor(Color.argb(255, 192, 192, 192));
            editText.setText("再生リスト");
            builder.setView(editText);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    addPlaylist(editText.getText().toString());
                }
            });
            builder.setNegativeButton("キャンセル", null);
            builder.show();
        }
        else if(v.getId() == R.id.btnRewind)
        {
            if(activity.hStream == 0) return;
            EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
            if(!effectFragment.isReverse() && BASS.BASS_ChannelBytes2Seconds(activity.hStream, BASS.BASS_ChannelGetPosition(activity.hStream, BASS.BASS_POS_BYTE)) > activity.dLoopA + 1.0)
                BASS.BASS_ChannelSetPosition(activity.hStream, BASS.BASS_ChannelSeconds2Bytes(activity.hStream, activity.dLoopA), BASS.BASS_POS_BYTE);
            else if(effectFragment.isReverse() && BASS.BASS_ChannelBytes2Seconds(activity.hStream, BASS.BASS_ChannelGetPosition(activity.hStream, BASS.BASS_POS_BYTE)) < activity.dLoopA - 1.0)
                BASS.BASS_ChannelSetPosition(activity.hStream, BASS.BASS_ChannelSeconds2Bytes(activity.hStream, activity.dLoopB), BASS.BASS_POS_BYTE);
            else
                playPrev();
        }
        else if(v.getId() == R.id.btnStop)
        {
            if(activity.hStream == 0) return;
            stop();
        }
        else if(v.getId() == R.id.btnPlay)
        {
            if(BASS.BASS_ChannelIsActive(activity.hStream) == BASS.BASS_ACTIVE_PLAYING)
            {
                pause();
            }
            else
            {
                if(BASS.BASS_ChannelIsActive(activity.hStream) == BASS.BASS_ACTIVE_PAUSED)
                {
                    play();
                }
                else
                {
                    if(activity.hStream == 0)
                    {
                        nPlayingPlaylist = nSelectedPlaylist;
                        ArrayList<SongItem> arSongs = arPlaylists.get(nSelectedPlaylist);
                        arPlayed = new ArrayList<Boolean>();
                        for(int i = 0; i < arSongs.size(); i++)
                            arPlayed.add(false);
                        playNext();
                    }
                }
            }
        }
        else if(v.getId() == R.id.btnForward)
        {
            if(activity.hStream == 0) return;
            playNext();
        }
        else if(v.getId() == R.id.btnPlayMode)
        {
            Button btnPlayMode = (Button)activity.findViewById(R.id.btnPlayMode);
            if(btnPlayMode.getText().equals("連続再生"))
            {
                btnPlayMode.setText("１曲リピート");
                btnPlayMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_sloop, 0, 0);
            }
            else if(btnPlayMode.getText().equals("１曲リピート"))
            {
                btnPlayMode.setText("全曲リピート");
                btnPlayMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_aloop, 0, 0);
            }
            else if(btnPlayMode.getText().equals("全曲リピート"))
            {
                btnPlayMode.setText("シャッフル");
                btnPlayMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_random, 0, 0);
            }
            else if(btnPlayMode.getText().equals("シャッフル"))
            {
                btnPlayMode.setText("連続再生");
                btnPlayMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_normal, 0, 0);
            }

            SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
            int nPlayMode = 0;
            if(btnPlayMode.getText().equals("連続再生"))
                nPlayMode = 0;
            else if(btnPlayMode.getText().equals("１曲リピート"))
                nPlayMode = 1;
            else if(btnPlayMode.getText().equals("全曲リピート"))
                nPlayMode = 2;
            else if(btnPlayMode.getText().equals("シャッフル"))
                nPlayMode = 3;
            preferences.edit().putInt("playmode", nPlayMode).commit();
        }
        else if(v.getId() == R.id.btnRecord)
        {
            startRecord();
        }
        else if(v.getId() == R.id.textLeft)
        {
            RelativeLayout relativeSongs = (RelativeLayout)activity.findViewById(R.id.relativeSongs);
            relativeSongs.setVisibility(View.INVISIBLE);
            playlistsAdapter.notifyDataSetChanged();
            RelativeLayout relativePlaylists = (RelativeLayout)activity.findViewById(R.id.relativePlaylists);
            relativePlaylists.setVisibility(View.VISIBLE);
        }
        else if(v.getId() == R.id.textAddPlaylist_small)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("新しいリストを追加する");
            final EditText editText = new EditText (activity);
            editText.setHint("再生リスト");
            editText.setHintTextColor(Color.argb(255, 192, 192, 192));
            editText.setText("再生リスト");
            builder.setView(editText);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    addPlaylist(editText.getText().toString());
                }
            });
            builder.setNegativeButton("キャンセル", null);
            builder.show();
        }
        else if(v.getId() == R.id.textAddSong)
        {
            activity.open();
        }
        else if(v.getId() == R.id.textFinishSort)
        {
            recyclerSongs.setPadding(0, 0, 0, (int)(80 * getResources().getDisplayMetrics().density + 0.5));
            TextView textFinishSort = (TextView) activity.findViewById(R.id.textFinishSort);
            textFinishSort.setVisibility(View.GONE);
            TextView textAddSong = (TextView) activity.findViewById(R.id.textAddSong);
            textAddSong.setVisibility(View.VISIBLE);
            bSorting = false;
            songsAdapter.notifyDataSetChanged();
        }
        else if(v.getId() == R.id.btnFinishLyrics)
        {
            Button btnFinishLyrics = (Button)activity.findViewById(R.id.btnFinishLyrics);
            if(btnFinishLyrics.getText().equals("閉じる")) {
                RelativeLayout relativeSongs = (RelativeLayout)activity.findViewById(R.id.relativeSongs);
                relativeSongs.setVisibility(View.VISIBLE);
                RelativeLayout relativeLyrics = (RelativeLayout)activity.findViewById(R.id.relativeLyrics);
                relativeLyrics.setVisibility(View.INVISIBLE);
            }
            else {
                TextView textLyrics = (TextView)activity.findViewById(R.id.textLyrics);
                EditText editLyrics = (EditText)activity.findViewById(R.id.editLyrics);
                ImageButton btnEdit = (ImageButton)activity.findViewById(R.id.btnEdit);
                TextView textNoLyrics = (TextView)activity.findViewById(R.id.textNoLyrics);
                ImageView imgEdit = (ImageView)activity.findViewById(R.id.imgEdit);
                TextView textTapEdit = (TextView)activity.findViewById(R.id.textTapEdit);
                String strLyrics = editLyrics.getText().toString();
                ArrayList<String> arTempLyrics = arLyrics.get(nSelectedPlaylist);
                arTempLyrics.set(nSelectedItem, strLyrics);
                textLyrics.setText(strLyrics);
                btnFinishLyrics.setText("閉じる");
                textLyrics.setText(strLyrics);
                if(strLyrics == null || strLyrics.equals("")) {
                    editLyrics.setVisibility(View.INVISIBLE);
                    textNoLyrics.setVisibility(View.VISIBLE);
                    textLyrics.setVisibility(View.INVISIBLE);
                    btnEdit.setVisibility(View.INVISIBLE);
                    imgEdit.setVisibility(View.VISIBLE);
                    textTapEdit.setVisibility(View.VISIBLE);
                }
                else {
                    editLyrics.setVisibility(View.INVISIBLE);
                    textNoLyrics.setVisibility(View.INVISIBLE);
                    textLyrics.setVisibility(View.VISIBLE);
                    btnEdit.setVisibility(View.VISIBLE);
                    imgEdit.setVisibility(View.INVISIBLE);
                    textTapEdit.setVisibility(View.INVISIBLE);
                }
                editLyrics.clearFocus();
                InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editLyrics.getWindowToken(), 0);

                SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                Gson gson = new Gson();
                preferences.edit().putString("arLyrics", gson.toJson(arLyrics)).commit();
            }
        }
        else if(v.getId() == R.id.btnEdit)
        {
            TextView textLyrics = (TextView)activity.findViewById(R.id.textLyrics);
            textLyrics.setVisibility(View.INVISIBLE);
            Button btnFinishLyrics = (Button)activity.findViewById(R.id.btnFinishLyrics);
            btnFinishLyrics.setText("完了");
            ImageButton btnEdit = (ImageButton)activity.findViewById(R.id.btnEdit);
            btnEdit.setVisibility(View.INVISIBLE);
            EditText editLyrics = (EditText)activity.findViewById(R.id.editLyrics);
            editLyrics.setText(textLyrics.getText());
            editLyrics.setVisibility(View.VISIBLE);
            editLyrics.requestFocus();
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editLyrics, InputMethodManager.SHOW_IMPLICIT);
            int nPos = editLyrics.getText().length();
            editLyrics.setSelection(nPos);
        }
        else if(v.getId() == R.id.textNoLyrics)
        {
            TextView textNoLyrics = (TextView)activity.findViewById(R.id.textNoLyrics);
            textNoLyrics.setVisibility(View.INVISIBLE);
            ImageView imgEdit = (ImageView)activity.findViewById(R.id.imgEdit);
            imgEdit.setVisibility(View.INVISIBLE);
            TextView textTapEdit = (TextView)activity.findViewById(R.id.textTapEdit);
            textTapEdit.setVisibility(View.INVISIBLE);

            TextView textLyrics = (TextView)activity.findViewById(R.id.textLyrics);
            textLyrics.setVisibility(View.INVISIBLE);
            Button btnFinishLyrics = (Button)activity.findViewById(R.id.btnFinishLyrics);
            btnFinishLyrics.setText("完了");
            ImageButton btnEdit = (ImageButton)activity.findViewById(R.id.btnEdit);
            btnEdit.setVisibility(View.INVISIBLE);
            EditText editLyrics = (EditText)activity.findViewById(R.id.editLyrics);
            editLyrics.setText("");
            editLyrics.setVisibility(View.VISIBLE);
            editLyrics.requestFocus();
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editLyrics, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void startRecord()
    {
        StatFs sf = new StatFs(activity.getFilesDir().toString());
        long nFreeSpace = 0;
        if(Build.VERSION.SDK_INT >= 18)
            nFreeSpace = sf.getAvailableBlocksLong() * sf.getBlockSizeLong();
        else
            nFreeSpace = (long)sf.getAvailableBlocks() * (long)sf.getBlockSize();
        if(nFreeSpace < 100) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("端末内の空き容量が少なくなっています");
            builder.setMessage("こんにちは♪\n\nハヤえもん開発者のりょーたです！\n\n端末内の空き容量が少なくなっています。\n\n不要なファイルを削除した上で、再度試してみてください。\n\nそれでは引き続き、Enjoy \"Your\" Music with Hayaemon!!");
            builder.setPositiveButton("OK", null);
            builder.show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("録音中…");
        RelativeLayout relative = new RelativeLayout(activity);
        final TextView text = new TextView (activity);
        text.setText("00:00:00.00");
        text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);

        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        param.addRule(RelativeLayout.CENTER_IN_PARENT);
        param.topMargin = 32;
        relative.addView(text, param);
        builder.setView(relative);
        builder.setPositiveButton("完了", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                stopRecord();
            }
        });
        builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                BASS.BASS_ChannelStop(hRecord);
                hRecord = 0;
            }
        });

        builder.show();

        BASS.BASS_RecordInit(-1);
        recbuf = ByteBuffer.allocateDirect(200000);
        recbuf.order(ByteOrder.LITTLE_ENDIAN);
        recbuf.put(new byte[]{'R','I','F','F',0,0,0,0,'W','A','V','E','f','m','t',' ',16,0,0,0});
        recbuf.putShort((short)1);
        recbuf.putShort((short)1);
        recbuf.putInt(44100);
        recbuf.putInt(44100 * 2);
        recbuf.putShort((short)2);
        recbuf.putShort((short)16);
        recbuf.put(new byte[]{'d','a','t','a',0,0,0,0});
        BASS.RECORDPROC RecordingCallback = new BASS.RECORDPROC() {
            public boolean RECORDPROC(int handle, ByteBuffer buffer, int length, Object user) {
                try {
                    recbuf.put(buffer);
                } catch (BufferOverflowException e) {
                    ByteBuffer temp;
                    try {
                        temp = ByteBuffer.allocateDirect(recbuf.position() + length + 200000);
                    } catch (Error e2) {
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                stopRecord();
                            }
                        });
                        return false;
                    }
                    temp.order(ByteOrder.LITTLE_ENDIAN);
                    recbuf.limit(recbuf.position());
                    recbuf.position(0);
                    temp.put(recbuf);
                    recbuf = temp;
                    recbuf.put(buffer);
                }
                return true;
            }
        };
        if(hRecord != 0) {
            BASS.BASS_ChannelStop(hRecord);
            hRecord = 0;
        }
        if(Build.VERSION.SDK_INT >= 23) {
            if (activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                return;
            }
        }
        hRecord = BASS.BASS_RecordStart(44100, 1, 0, RecordingCallback, 0);

        final Handler handler = new Handler();
        Runnable timer=new Runnable() {
            public void run()
            {
                if (hRecord == 0) return;
                double dPos = BASS.BASS_ChannelBytes2Seconds(hRecord, BASS.BASS_ChannelGetPosition(hRecord, BASS.BASS_POS_BYTE));
                int nHour = (int)(dPos / (60 * 60) % 60);
                int nMinute = (int)(dPos / 60 % 60);
                int nSecond = (int)(dPos % 60);
                int nMillisecond = (int)(dPos * 100 % 100);
                text.setText(String.format("%02d:%02d:%02d.%02d", nHour, nMinute, nSecond, nMillisecond));
                handler.postDelayed(this, 50);
            }
        };
        handler.postDelayed(timer, 50);
    }

    public void stopRecord()
    {
        BASS.BASS_ChannelStop(hRecord);
        hRecord = 0;
        recbuf.limit(recbuf.position());
        recbuf.putInt(4, recbuf.position()-8);
        recbuf.putInt(40, recbuf.position()-44);
        int i = 0;
        String strPath;
        File fileForCheck;
        while(true) {
            strPath = activity.getFilesDir() + "/recorded" + String.format("%d", i) + ".wav";
            fileForCheck = new File(strPath);
            if(!fileForCheck.exists()) break;
            i++;
        }
        final File file = new File(strPath);
        try {
            FileChannel fc = new FileOutputStream(file).getChannel();
            recbuf.position(0);
            fc.write(recbuf);
            fc.close();
        } catch (IOException e) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("新規録音");
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final EditText editTitle = new EditText (activity);
        editTitle.setHint("タイトル");
        editTitle.setHintTextColor(Color.argb(255, 192, 192, 192));
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        editTitle.setText("新規録音(" + df.format(date) + ")");
        final EditText editArtist = new EditText (activity);
        editArtist.setHint("アーティスト名");
        editArtist.setHintTextColor(Color.argb(255, 192, 192, 192));
        editArtist.setText("");
        linearLayout.addView(editTitle);
        linearLayout.addView(editArtist);
        builder.setView(linearLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ArrayList<SongItem> arSongs = arPlaylists.get(nSelectedPlaylist);
                SongItem item = new SongItem(String.format("%d", arSongs.size()+1), editTitle.getText().toString(), editArtist.getText().toString(), file.getPath());
                arSongs.add(item);
                ArrayList<EffectSaver> arEffectSavers = arEffects.get(nSelectedPlaylist);
                EffectSaver saver = new EffectSaver();
                arEffectSavers.add(saver);
                ArrayList<String> arTempLyrics = arLyrics.get(nSelectedPlaylist);
                arTempLyrics.add(null);
                if(nSelectedPlaylist == nPlayingPlaylist) arPlayed.add(false);
                songsAdapter.notifyDataSetChanged();
                SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                Gson gson = new Gson();
                preferences.edit().putString("arPlaylists", gson.toJson(arPlaylists)).commit();
                preferences.edit().putString("arEffects", gson.toJson(arEffects)).commit();
                preferences.edit().putString("arLyrics", gson.toJson(arLyrics)).commit();
                preferences.edit().putString("arPlaylistNames", gson.toJson(arPlaylistNames)).commit();
            }
        });
        builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                file.delete();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                file.delete();
            }
        });
        builder.show();
    }

    public void addPlaylist(String strName)
    {
        arPlaylistNames.add(strName);
        ArrayList<SongItem> arSongs = new ArrayList<>();
        arPlaylists.add(arSongs);
        ArrayList<EffectSaver> arEffectSavers = new ArrayList<>();
        arEffects.add(arEffectSavers);
        ArrayList<String> arTempLyrics = new ArrayList<>();
        arLyrics.add(arTempLyrics);
        if(activity != null)
        {
            SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
            Gson gson = new Gson();
            preferences.edit().putString("arPlaylists", gson.toJson(arPlaylists)).commit();
            preferences.edit().putString("arEffects", gson.toJson(arEffects)).commit();
            preferences.edit().putString("arLyrics", gson.toJson(arLyrics)).commit();
            preferences.edit().putString("arPlaylistNames", gson.toJson(arPlaylistNames)).commit();
        }
        selectPlaylist(arPlaylists.size() - 1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_playlist, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        recyclerPlaylists = (RecyclerView)activity.findViewById(R.id.recyclerPlaylists);
        recyclerPlaylists.setHasFixedSize(false);
        LinearLayoutManager playlistsManager = new LinearLayoutManager(activity);
        recyclerPlaylists.setLayoutManager(playlistsManager);
        recyclerPlaylists.setAdapter(playlistsAdapter);
        recyclerPlaylists.setOnClickListener(this);

        recyclerTab = (RecyclerView)activity.findViewById(R.id.recyclerTab);
        recyclerTab.setHasFixedSize(false);
        LinearLayoutManager tabManager = new LinearLayoutManager(activity);
        tabManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerTab.setLayoutManager(tabManager);
        recyclerTab.setAdapter(tabAdapter);

        recyclerSongs = (RecyclerView)activity.findViewById(R.id.recyclerSongs);
        recyclerSongs.setHasFixedSize(false);
        LinearLayoutManager songsManager = new LinearLayoutManager(activity);
        recyclerSongs.setLayoutManager(songsManager);
        recyclerSongs.setAdapter(songsAdapter);
        recyclerSongs.setOnClickListener(this);
        songTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(RecyclerView recyclerSongs, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                final int fromPos = viewHolder.getAdapterPosition();
                final int toPos = target.getAdapterPosition();

                ArrayList<SongItem> arSongs = arPlaylists.get(nSelectedPlaylist);
                SongItem itemTemp = arSongs.get(fromPos);
                arSongs.remove(fromPos);
                arSongs.add(toPos, itemTemp);

                ArrayList<EffectSaver> arEffectSavers = arEffects.get(nSelectedPlaylist);
                EffectSaver saver = arEffectSavers.get(fromPos);
                arEffectSavers.remove(fromPos);
                arEffectSavers.add(toPos, saver);

                ArrayList<String> arTempLyrics = arLyrics.get(nSelectedPlaylist);
                String strLyrics = arTempLyrics.get(fromPos);
                arTempLyrics.remove(fromPos);
                arTempLyrics.add(toPos, strLyrics);

                if(nPlayingPlaylist == nSelectedPlaylist)
                {
                    Boolean bTemp = arPlayed.get(fromPos);
                    arPlayed.remove(fromPos);
                    arPlayed.add(toPos, bTemp);
                }

                int nStart = fromPos < toPos ? fromPos : toPos;
                for(int i = nStart; i < arSongs.size(); i++) {
                    SongItem songItem = arSongs.get(i);
                    songItem.setNumber(String.format("%d", i+1));
                }

                if(fromPos == nPlaying) nPlaying = toPos;
                else if(fromPos < nPlaying && nPlaying <= toPos) nPlaying--;
                else if(fromPos > nPlaying && nPlaying >= toPos) nPlaying++;

                songsAdapter.notifyItemMoved(fromPos, toPos);

                return true;
            }

            @Override
            public void clearView(RecyclerView recyclerSongs, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerSongs, viewHolder);

                songsAdapter.notifyDataSetChanged();

                SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                Gson gson = new Gson();
                preferences.edit().putString("arPlaylists", gson.toJson(arPlaylists)).commit();
                preferences.edit().putString("arEffects", gson.toJson(arEffects)).commit();
                preferences.edit().putString("arLyrics", gson.toJson(arLyrics)).commit();
                preferences.edit().putString("arPlaylistNames", gson.toJson(arPlaylistNames)).commit();
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }
        });
        songTouchHelper.attachToRecyclerView(recyclerSongs);

        Button btnRewind = (Button) activity.findViewById(R.id.btnRewind);
        btnRewind.setOnClickListener(this);

        Button btnPlay = (Button) activity.findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(this);

        Button btnStop = (Button) activity.findViewById(R.id.btnStop);
        btnStop.setOnClickListener(this);

        Button btnForward = (Button) activity.findViewById(R.id.btnForward);
        btnForward.setOnClickListener(this);

        Button btnPlayMode = (Button) activity.findViewById(R.id.btnPlayMode);
        btnPlayMode.setOnClickListener(this);

        Button btnRecord = (Button) activity.findViewById(R.id.btnRecord);
        btnRecord.setOnClickListener(this);

        Button btnSortPlaylist = (Button) activity.findViewById(R.id.btnSortPlaylist);
        btnSortPlaylist.setOnClickListener(this);

        TextView textAddPlaylist = (TextView) activity.findViewById(R.id.textAddPlaylist);
        textAddPlaylist.setOnClickListener(this);

        TextView textLeft = (TextView) activity.findViewById(R.id.textLeft);
        textLeft.setOnClickListener(this);

        TextView textAddPlaylist_small = (TextView) activity.findViewById(R.id.textAddPlaylist_small);
        textAddPlaylist_small.setOnClickListener(this);

        TextView textAddSong = (TextView) activity.findViewById(R.id.textAddSong);
        textAddSong.setOnClickListener(this);

        TextView textFinishSort = (TextView) activity.findViewById(R.id.textFinishSort);
        textFinishSort.setOnClickListener(this);

        Button btnFinishLyrics = (Button) activity.findViewById(R.id.btnFinishLyrics);
        btnFinishLyrics.setOnClickListener(this);

        ImageButton btnEdit = (ImageButton)activity.findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(this);

        TextView textNoLyrics = (TextView)activity.findViewById(R.id.textNoLyrics);
        textNoLyrics.setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1)
        {
            if(resultCode == RESULT_OK)
            {
                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if(Build.VERSION.SDK_INT < 19)
                {
                    addSong(activity, data.getData());
                }
                else
                {
                    if(data.getClipData() == null)
                    {
                        addSong(activity, data.getData());
                        activity.getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
                    }
                    else
                    {
                        for(int i = 0; i < data.getClipData().getItemCount(); i++)
                        {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            addSong(activity, uri);
                            activity.getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        }
                    }
                }
                songsAdapter.notifyDataSetChanged();
            }
        }

        SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        preferences.edit().putString("arPlaylists", gson.toJson(arPlaylists)).commit();
        preferences.edit().putString("arEffects", gson.toJson(arEffects)).commit();
        preferences.edit().putString("arLyrics", gson.toJson(arLyrics)).commit();
        preferences.edit().putString("arPlaylistNames", gson.toJson(arPlaylistNames)).commit();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo info)
    {
        super.onCreateContextMenu(menu, view, info);
        if(view instanceof RelativeLayout)
        {
            RelativeLayout relativeSongs = (RelativeLayout)activity.findViewById(R.id.relativeSongs);
            if(relativeSongs.getVisibility() == View.VISIBLE) {
                RelativeLayout relative = (RelativeLayout)view;
                TextView textNumber = (TextView)relative.getChildAt(0);
                nSelectedItem = Integer.parseInt((String)textNumber.getText()) - 1;
                String strSong = songsAdapter.getTitle(nSelectedItem);
                menu.setHeaderTitle(strSong);
                menu.add("保存／エクスポート");
                menu.add("別の再生リストに移動");
                menu.add("コピー");
                menu.add("削除");
                if(bSorting) menu.add("並べ替えを終了する");
                else menu.add("曲順の並べ替え");
                menu.add("タイトルとアーティスト名を変更");
                menu.add("歌詞を表示");
                ArrayList<EffectSaver> arEffectSavers = arEffects.get(nSelectedPlaylist);
                EffectSaver saver = arEffectSavers.get(nSelectedItem);
                if(saver.isSave()) menu.add("エフェクトの設定保持を解除");
                else menu.add("エフェクトの設定状態を保持");
            }
            else {
                if(!playlistsAdapter.isClicked()) return;
                playlistsAdapter.setClicked(false);
                int nPosition = playlistsAdapter.getPosition();
                RelativeLayout relative = (RelativeLayout)view;
                menu.setHeaderTitle(playlistsAdapter.getName(nPosition));
                menu.add("再生リスト名を変更");
                menu.add("再生リストを削除");
                menu.add("再生リストを空にする");
            }
        }
        else if(view instanceof TextView)
        {
            int nPosition = tabAdapter.getPosition();
            selectPlaylist(nPosition);
            String strPlaylist = arPlaylistNames.get(nPosition);
            menu.setHeaderTitle(strPlaylist);
            menu.add("再生リスト名を変更");
            menu.add("再生リストを削除");
            menu.add("再生リストを空にする");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        if(item.getTitle().equals("保存／エクスポート"))
        {
            final BottomSheetDialog dialog = new BottomSheetDialog(activity);
            LinearLayout linearLayout = new LinearLayout(activity);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            ScrollView scroll = new ScrollView(activity);

            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            TextView textLocal = new TextView (activity);
            textLocal.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            textLocal.setGravity(Gravity.CENTER);
            textLocal.setText("ローカルに保存");
            textLocal.setHeight((int)(48 *  getResources().getDisplayMetrics().density + 0.5));
            textLocal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    saveSongToLocal();
                }
            });
            linearLayout.addView(textLocal, param);

            TextView textExport = new TextView (activity);
            textExport.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            textExport.setGravity(Gravity.CENTER);
            textExport.setText("他のアプリにエクスポート");
            textExport.setTag(1);
            textExport.setHeight((int)(48 *  getResources().getDisplayMetrics().density + 0.5));
            textExport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    export();
                }
            });
            linearLayout.addView(textExport, param);

            TextView textCancel = new TextView (activity);
            textCancel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            textCancel.setGravity(Gravity.CENTER);
            textCancel.setText("キャンセル");
            textCancel.setHeight((int)(48 *  getResources().getDisplayMetrics().density + 0.5));
            textCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            linearLayout.addView(textCancel, param);

            scroll.addView(linearLayout);
            dialog.setContentView(scroll);
            dialog.show();
        }
        else if(item.getTitle().equals("別の再生リストに移動"))
        {
            if(arPlaylistNames.size() <= 1) return super.onContextItemSelected(item);
            final BottomSheetDialog dialog = new BottomSheetDialog(activity);
            LinearLayout linearLayout = new LinearLayout(activity);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            ScrollView scroll = new ScrollView(activity);
            ArrayList<TextView> arTempText = new ArrayList<>();
            for(int i = 0; i < arPlaylistNames.size(); i++) {
                if(i == nSelectedPlaylist) continue;
                TextView text = new TextView (activity);
                text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                text.setGravity(Gravity.CENTER);
                text.setText(arPlaylistNames.get(i).toString());
                text.setTag(i);
                text.setHeight((int)(48 *  getResources().getDisplayMetrics().density + 0.5));
                arTempText.add(text);
            }
            TextView textCancel = new TextView (activity);
            textCancel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            textCancel.setGravity(Gravity.CENTER);
            textCancel.setText("キャンセル");
            textCancel.setHeight((int)(48 *  getResources().getDisplayMetrics().density + 0.5));
            arTempText.add(textCancel);
            final ArrayList<TextView> arText = arTempText;
            for(int i = 0; i < arText.size(); i++) {
                TextView text = arText.get(i);
                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for(int j = 0; j < arText.size(); j ++) {
                            if(arText.get(j).equals(view))
                            {
                                if(view.getTag() != null) {
                                    int nPlaylistTo = (int)view.getTag();
                                    ArrayList<SongItem> arSongsFrom = arPlaylists.get(nSelectedPlaylist);
                                    ArrayList<SongItem> arSongsTo = arPlaylists.get(nPlaylistTo);
                                    SongItem item = arSongsFrom.get(nSelectedItem);
                                    arSongsTo.add(item);
                                    item.setNumber(String.format("%d", arSongsTo.size()));
                                    arSongsFrom.remove(nSelectedItem);

                                    ArrayList<EffectSaver> arEffectSaversFrom = arEffects.get(nSelectedPlaylist);
                                    ArrayList<EffectSaver> arEffectSaversTo = arEffects.get(nPlaylistTo);
                                    EffectSaver saver = arEffectSaversFrom.get(nSelectedItem);
                                    arEffectSaversTo.add(saver);
                                    arEffectSaversFrom.remove(nSelectedItem);

                                    ArrayList<String> arTempLyricsFrom = arLyrics.get(nSelectedPlaylist);
                                    ArrayList<String> arTempLyricsTo = arLyrics.get(nPlaylistTo);
                                    String strLyrics = arTempLyricsFrom.get(nSelectedItem);
                                    arTempLyricsTo.add(strLyrics);
                                    arTempLyricsFrom.remove(nSelectedItem);

                                    if(nSelectedPlaylist == nPlayingPlaylist)
                                        arPlayed.remove(nSelectedItem);
                                    if(nPlaylistTo == nPlayingPlaylist)
                                        arPlayed.add(false);

                                    for(int i = nSelectedItem; i < arSongsFrom.size(); i++) {
                                        SongItem songItem = arSongsFrom.get(i);
                                        songItem.setNumber(String.format("%d", i+1));
                                    }

                                    if(nSelectedPlaylist == nPlayingPlaylist) {
                                        if(nSelectedItem == nPlaying) {
                                            nPlayingPlaylist = nPlaylistTo;
                                            nPlaying = arSongsTo.size() - 1;
                                        }
                                        else if(nSelectedItem < nPlaying) nPlaying--;
                                    }

                                    songsAdapter.notifyDataSetChanged();
                                }
                                dialog.dismiss();
                            }
                        }
                    }
                });
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                linearLayout.addView(text, param);
            }
            scroll.addView(linearLayout);
            dialog.setContentView(scroll);
            dialog.show();
        }
        else if(item.getTitle().equals("コピー"))
        {
            final BottomSheetDialog dialog = new BottomSheetDialog(activity);
            LinearLayout linearLayout = new LinearLayout(activity);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            ScrollView scroll = new ScrollView(activity);
            ArrayList<TextView> arTempText = new ArrayList<>();
            for(int i = 0; i < arPlaylistNames.size(); i++) {
                TextView text = new TextView (activity);
                text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                text.setGravity(Gravity.CENTER);
                text.setText(arPlaylistNames.get(i).toString());
                text.setTag(i);
                text.setHeight((int)(48 *  getResources().getDisplayMetrics().density + 0.5));
                arTempText.add(text);
            }
            TextView textCancel = new TextView (activity);
            textCancel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            textCancel.setGravity(Gravity.CENTER);
            textCancel.setText("キャンセル");
            textCancel.setHeight((int)(48 *  getResources().getDisplayMetrics().density + 0.5));
            arTempText.add(textCancel);
            final ArrayList<TextView> arText = arTempText;
            for(int i = 0; i < arText.size(); i++) {
                TextView text = arText.get(i);
                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for(int j = 0; j < arText.size(); j ++) {
                            if(arText.get(j).equals(view))
                            {
                                if(view.getTag() != null) {
                                    int nPlaylistTo = (int)view.getTag();
                                    ArrayList<SongItem> arSongsFrom = arPlaylists.get(nSelectedPlaylist);
                                    ArrayList<SongItem> arSongsTo = arPlaylists.get(nPlaylistTo);
                                    SongItem itemFrom = arSongsFrom.get(nSelectedItem);
                                    File file = new File(itemFrom.getPath());
                                    String strPath = itemFrom.getPath();
                                    if(file.getParent().equals(activity.getFilesDir()))
                                        strPath = activity.copyFile(Uri.parse(itemFrom.getPath())).toString();
                                    SongItem itemTo = new SongItem(String.format("%d", arSongsTo.size()+1), itemFrom.getTitle(), itemFrom.getArtist(), strPath);
                                    arSongsTo.add(itemTo);

                                    ArrayList<EffectSaver> arEffectSaversFrom = arEffects.get(nSelectedPlaylist);
                                    ArrayList<EffectSaver> arEffectSaversTo = arEffects.get(nPlaylistTo);
                                    EffectSaver saverFrom = arEffectSaversFrom.get(nSelectedItem);
                                    if(saverFrom.isSave()) {
                                        EffectSaver saverTo = new EffectSaver(saverFrom);
                                        arEffectSaversTo.add(saverTo);
                                    }
                                    else {
                                        EffectSaver saverTo = new EffectSaver();
                                        arEffectSaversTo.add(saverTo);
                                    }

                                    ArrayList<String> arTempLyricsFrom = arLyrics.get(nSelectedPlaylist);
                                    ArrayList<String> arTempLyricsTo = arLyrics.get(nPlaylistTo);
                                    String strLyrics = arTempLyricsFrom.get(nSelectedItem);
                                    arTempLyricsTo.add(strLyrics);

                                    if(nPlaylistTo == nPlayingPlaylist)
                                        arPlayed.add(false);

                                    for(int i = nSelectedItem; i < arSongsFrom.size(); i++) {
                                        SongItem songItem = arSongsFrom.get(i);
                                        songItem.setNumber(String.format("%d", i+1));
                                    }

                                    songsAdapter.notifyDataSetChanged();
                                }
                                dialog.dismiss();
                            }
                        }
                    }
                });
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                linearLayout.addView(text, param);
            }
            scroll.addView(linearLayout);
            dialog.setContentView(scroll);
            dialog.show();
        }
        else if(item.getTitle().equals("削除"))
        {
            removeSong(nSelectedPlaylist, nSelectedItem);
        }
        else if(item.getTitle().equals("曲順の並べ替え"))
        {
            recyclerSongs.setPadding(0, 0, 0, (int)(64 * getResources().getDisplayMetrics().density + 0.5));
            TextView textFinishSort = (TextView) activity.findViewById(R.id.textFinishSort);
            textFinishSort.setVisibility(View.VISIBLE);
            TextView textAddSong = (TextView) activity.findViewById(R.id.textAddSong);
            textAddSong.setVisibility(View.GONE);
            bSorting = true;
            songsAdapter.notifyDataSetChanged();
        }
        else if(item.getTitle().equals("並べ替えを終了する"))
        {
            bSorting = false;
            songsAdapter.notifyDataSetChanged();
        }
        else if(item.getTitle().equals("タイトルとアーティスト名を変更"))
        {
            ArrayList<SongItem> arSongs = arPlaylists.get(nSelectedPlaylist);
            final SongItem songItem = arSongs.get(nSelectedItem);

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("タイトルとアーティスト名を変更");
            LinearLayout linearLayout = new LinearLayout(activity);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            final EditText editTitle = new EditText (activity);
            editTitle.setHint("タイトル");
            editTitle.setHintTextColor(Color.argb(255, 192, 192, 192));
            editTitle.setText(songItem.getTitle());
            final EditText editArtist = new EditText (activity);
            editArtist.setHint("アーティスト名");
            editArtist.setHintTextColor(Color.argb(255, 192, 192, 192));
            editArtist.setText(songItem.getArtist());
            linearLayout.addView(editTitle);
            linearLayout.addView(editArtist);
            builder.setView(linearLayout);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    songItem.setTitle(editTitle.getText().toString());
                    songItem.setArtist(editArtist.getText().toString());

                    songsAdapter.notifyDataSetChanged();

                    SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                    Gson gson = new Gson();
                    preferences.edit().putString("arPlaylists", gson.toJson(arPlaylists)).commit();
                    preferences.edit().putString("arEffects", gson.toJson(arEffects)).commit();
                    preferences.edit().putString("arLyrics", gson.toJson(arLyrics)).commit();
                    preferences.edit().putString("arPlaylistNames", gson.toJson(arPlaylistNames)).commit();
                }
            });
            builder.setNegativeButton("キャンセル", null);
            builder.show();
        }
        else if(item.getTitle().equals("歌詞を表示"))
        {
            showLyrics();
        }
        else if(item.getTitle().equals("エフェクトの設定状態を保持"))
        {
            setSavingEffect();
            songsAdapter.notifyDataSetChanged();
        }
        else if(item.getTitle().equals("エフェクトの設定保持を解除"))
        {
            ArrayList<EffectSaver> arEffectSavers = arEffects.get(nSelectedPlaylist);
            EffectSaver saver = arEffectSavers.get(nSelectedItem);
            saver.setSave(false);
            songsAdapter.notifyDataSetChanged();
        }
        else if(item.getTitle().equals("再生リスト名を変更"))
        {
            final int nPlaylist;
            RelativeLayout relativeSongs = (RelativeLayout)activity.findViewById(R.id.relativeSongs);
            if(relativeSongs.getVisibility() == View.VISIBLE)
                nPlaylist = tabAdapter.getPosition();
            else
                nPlaylist = playlistsAdapter.getPosition();
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("再生リスト名を変更");
            final EditText editText = new EditText (activity);
            editText.setHint("再生リスト");
            editText.setHintTextColor(Color.argb(255, 192, 192, 192));
            editText.setText(arPlaylistNames.get(nPlaylist));
            builder.setView(editText);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    arPlaylistNames.set(nPlaylist, editText.getText().toString());

                    tabAdapter.notifyDataSetChanged();
                    playlistsAdapter.notifyDataSetChanged();

                    SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                    Gson gson = new Gson();
                    preferences.edit().putString("arPlaylists", gson.toJson(arPlaylists)).commit();
                    preferences.edit().putString("arEffects", gson.toJson(arEffects)).commit();
                    preferences.edit().putString("arLyrics", gson.toJson(arLyrics)).commit();
                    preferences.edit().putString("arPlaylistNames", gson.toJson(arPlaylistNames)).commit();
                }
            });
            builder.setNegativeButton("キャンセル", null);
            builder.show();
        }
        else if(item.getTitle().equals("再生リストを削除"))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("再生リストを削除");
            builder.setMessage("再生リストを削除しますが、よろしいでしょうか？");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    int nDelete;
                    RelativeLayout relativeSongs = (RelativeLayout)activity.findViewById(R.id.relativeSongs);
                    if(relativeSongs.getVisibility() == View.VISIBLE)
                        nDelete = tabAdapter.getPosition();
                    else
                        nDelete = playlistsAdapter.getPosition();
                    if(nDelete == nPlayingPlaylist) stop();
                    else if(nDelete < nPlayingPlaylist) nPlayingPlaylist--;
                    ArrayList<SongItem> arSongs = arPlaylists.get(nDelete);
                    for(int i = 0; i < arSongs.size(); i++) {
                        SongItem song = arSongs.get(i);
                        File file = new File(song.getPath());
                        if(file.getParent().equals(activity.getFilesDir())) {
                            file.delete();
                        }
                    }
                    arPlaylists.remove(nDelete);
                    arEffects.remove(nDelete);
                    arPlaylistNames.remove(nDelete);
                    arLyrics.remove(nDelete);
                    if(arPlaylists.size() == 0)
                        addPlaylist("再生リスト 1");

                    int nSelect = nDelete;
                    if(nSelect >= arPlaylists.size()) nSelect = arPlaylists.size() - 1;

                    selectPlaylist(nSelect);

                    SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                    Gson gson = new Gson();
                    preferences.edit().putString("arPlaylists", gson.toJson(arPlaylists)).commit();
                    preferences.edit().putString("arEffects", gson.toJson(arEffects)).commit();
                    preferences.edit().putString("arLyrics", gson.toJson(arLyrics)).commit();
                    preferences.edit().putString("arPlaylistNames", gson.toJson(arPlaylistNames)).commit();
                }
            });
            builder.setNegativeButton("キャンセル", null);
            builder.show();
        }
        else if(item.getTitle().equals("再生リストを空にする"))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("再生リストを空にする");
            builder.setMessage("再生リストを空にしますが、よろしいでしょうか？");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ArrayList<SongItem> arSongs;
                    ArrayList<EffectSaver> arEffectSavers;
                    ArrayList<String> arTempLyrics;
                    RelativeLayout relativeSongs = (RelativeLayout)activity.findViewById(R.id.relativeSongs);
                    if(relativeSongs.getVisibility() == View.VISIBLE) {
                        arSongs = arPlaylists.get(tabAdapter.getPosition());
                        arEffectSavers = arEffects.get(tabAdapter.getPosition());
                        arTempLyrics = arLyrics.get(tabAdapter.getPosition());
                    }
                    else {
                        arSongs = arPlaylists.get(playlistsAdapter.getPosition());
                        arEffectSavers = arEffects.get(playlistsAdapter.getPosition());
                        arTempLyrics = arLyrics.get(playlistsAdapter.getPosition());
                    }
                    for(int i = 0; i < arSongs.size(); i++) {
                        SongItem song = arSongs.get(i);
                        File file = new File(song.getPath());
                        if(file.getParent() != null && file.getParent().equals(activity.getFilesDir())) {
                            file.delete();
                        }
                    }
                    arSongs.clear();
                    arEffectSavers.clear();
                    arTempLyrics.clear();

                    songsAdapter.notifyDataSetChanged();
                    playlistsAdapter.notifyDataSetChanged();

                    SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                    Gson gson = new Gson();
                    preferences.edit().putString("arPlaylists", gson.toJson(arPlaylists)).commit();
                    preferences.edit().putString("arEffects", gson.toJson(arEffects)).commit();
                    preferences.edit().putString("arLyrics", gson.toJson(arLyrics)).commit();
                    preferences.edit().putString("arPlaylistNames", gson.toJson(arPlaylistNames)).commit();
                }
            });
            builder.setNegativeButton("キャンセル", null);
            builder.show();
        }
        return super.onContextItemSelected(item);
    }

    public void showLyrics()
    {
        ArrayList<SongItem> arSongs = arPlaylists.get(nSelectedPlaylist);
        SongItem songItem = arSongs.get(nSelectedItem);

        ArrayList<String> arTempLyrics = arLyrics.get(nSelectedPlaylist);
        String strLyrics = arTempLyrics.get(nSelectedItem);

        TextView textLyricsTitle = (TextView)activity.findViewById(R.id.textLyricsTitle);
        String strTitle = songItem.getTitle();
        if(songItem.getArtist() != null && !songItem.getArtist().equals(""))
            strTitle += " - " + songItem.getArtist();
        textLyricsTitle.setText(strTitle);

        TextView textNoLyrics = (TextView)activity.findViewById(R.id.textNoLyrics);
        TextView textLyrics = (TextView)activity.findViewById(R.id.textLyrics);
        ImageButton btnEdit = (ImageButton)activity.findViewById(R.id.btnEdit);
        ImageView imgEdit = (ImageView)activity.findViewById(R.id.imgEdit);
        TextView textTapEdit = (TextView)activity.findViewById(R.id.textTapEdit);
        if(strLyrics == null || strLyrics.equals(""))
            strLyrics = getLyrics(nSelectedPlaylist, nSelectedItem);
        if(strLyrics == null || strLyrics.equals("")) {
            textNoLyrics.setVisibility(View.VISIBLE);
            textLyrics.setVisibility(View.INVISIBLE);
            btnEdit.setVisibility(View.INVISIBLE);
            imgEdit.setVisibility(View.VISIBLE);
            textTapEdit.setVisibility(View.VISIBLE);
        }
        else {
            textLyrics.setText(strLyrics);
            textNoLyrics.setVisibility(View.INVISIBLE);
            textLyrics.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.VISIBLE);
            imgEdit.setVisibility(View.INVISIBLE);
            textTapEdit.setVisibility(View.INVISIBLE);
        }

        RelativeLayout relativeSongs = (RelativeLayout)activity.findViewById(R.id.relativeSongs);
        relativeSongs.setVisibility(View.INVISIBLE);
        RelativeLayout relativeLyrics = (RelativeLayout)activity.findViewById(R.id.relativeLyrics);
        relativeLyrics.setVisibility(View.VISIBLE);
    }

    public void setSavingEffect()
    {
        ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(1);
        LoopFragment loopFragment = (LoopFragment)activity.mSectionsPagerAdapter.getItem(2);
        EqualizerFragment equalizerFragment = (EqualizerFragment)activity.mSectionsPagerAdapter.getItem(3);
        EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
        ArrayList<EffectSaver> arEffectSavers = arEffects.get(nSelectedPlaylist);
        EffectSaver saver = arEffectSavers.get(nSelectedItem);
        saver.setSave(true);
        saver.setSpeed(controlFragment.fSpeed);
        saver.setPitch(controlFragment.fPitch);
        saver.setVol(equalizerFragment.getArSeek().get(0).getProgress() - 30);
        saver.setEQ20K(equalizerFragment.getArSeek().get(1).getProgress() - 30);
        saver.setEQ16K(equalizerFragment.getArSeek().get(2).getProgress() - 30);
        saver.setEQ12_5K(equalizerFragment.getArSeek().get(3).getProgress() - 30);
        saver.setEQ10K(equalizerFragment.getArSeek().get(4).getProgress() - 30);
        saver.setEQ8K(equalizerFragment.getArSeek().get(5).getProgress() - 30);
        saver.setEQ6_3K(equalizerFragment.getArSeek().get(6).getProgress() - 30);
        saver.setEQ5K(equalizerFragment.getArSeek().get(7).getProgress() - 30);
        saver.setEQ4K(equalizerFragment.getArSeek().get(8).getProgress() - 30);
        saver.setEQ3_15K(equalizerFragment.getArSeek().get(9).getProgress() - 30);
        saver.setEQ2_5K(equalizerFragment.getArSeek().get(10).getProgress() - 30);
        saver.setEQ2K(equalizerFragment.getArSeek().get(11).getProgress() - 30);
        saver.setEQ1_6K(equalizerFragment.getArSeek().get(12).getProgress() - 30);
        saver.setEQ1_25K(equalizerFragment.getArSeek().get(13).getProgress() - 30);
        saver.setEQ1K(equalizerFragment.getArSeek().get(14).getProgress() - 30);
        saver.setEQ800(equalizerFragment.getArSeek().get(15).getProgress() - 30);
        saver.setEQ630(equalizerFragment.getArSeek().get(16).getProgress() - 30);
        saver.setEQ500(equalizerFragment.getArSeek().get(17).getProgress() - 30);
        saver.setEQ400(equalizerFragment.getArSeek().get(18).getProgress() - 30);
        saver.setEQ315(equalizerFragment.getArSeek().get(19).getProgress() - 30);
        saver.setEQ250(equalizerFragment.getArSeek().get(20).getProgress() - 30);
        saver.setEQ200(equalizerFragment.getArSeek().get(21).getProgress() - 30);
        saver.setEQ160(equalizerFragment.getArSeek().get(22).getProgress() - 30);
        saver.setEQ125(equalizerFragment.getArSeek().get(23).getProgress() - 30);
        saver.setEQ100(equalizerFragment.getArSeek().get(24).getProgress() - 30);
        saver.setEQ80(equalizerFragment.getArSeek().get(25).getProgress() - 30);
        saver.setEQ63(equalizerFragment.getArSeek().get(26).getProgress() - 30);
        saver.setEQ50(equalizerFragment.getArSeek().get(27).getProgress() - 30);
        saver.setEQ40(equalizerFragment.getArSeek().get(28).getProgress() - 30);
        saver.setEQ31_5(equalizerFragment.getArSeek().get(29).getProgress() - 30);
        saver.setEQ25(equalizerFragment.getArSeek().get(30).getProgress() - 30);
        saver.setEQ20(equalizerFragment.getArSeek().get(31).getProgress() - 30);
        saver.setEffectItems(effectFragment.getEffectItems());
        ArrayList<EffectItem> arItems = effectFragment.getEffectItems();
        saver.setPan(effectFragment.getPan());
        saver.setFreq(effectFragment.getFreq());
        saver.setBPM(effectFragment.getBPM());
        saver.setVol1(effectFragment.getVol1());
        saver.setVol2(effectFragment.getVol2());
        saver.setVol3(effectFragment.getVol3());
        saver.setVol4(effectFragment.getVol4());
        saver.setVol5(effectFragment.getVol5());
        saver.setVol6(effectFragment.getVol6());
        saver.setVol7(effectFragment.getVol7());
        if(nSelectedPlaylist == nPlayingPlaylist && nSelectedItem == nPlaying) {
            LinearLayout ABButton = (LinearLayout)activity.findViewById(R.id.ABButton);
            LinearLayout MarkerButton = (LinearLayout)activity.findViewById(R.id.MarkerButton);
            ImageButton btnLoopmarker = (ImageButton)activity.findViewById(R.id.btnLoopmarker);
            if(ABButton.getVisibility() == View.VISIBLE) saver.setIsABLoop(true);
            else saver.setIsABLoop(false);
            saver.setIsLoop(true);
            saver.setIsLoopA(activity.bLoopA);
            saver.setLoopA(activity.dLoopA);
            saver.setIsLoopB(activity.bLoopB);
            saver.setLoopB(activity.dLoopB);
            saver.setArMarkerTime(loopFragment.getArMarkerTime());
            saver.setIsLoopMarker(btnLoopmarker.isSelected());
            saver.setMarker(loopFragment.getMarker());
        }
        SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        preferences.edit().putString("arEffects", gson.toJson(arEffects)).commit();
    }

    public void updateSavingEffect()
    {
        if(MainActivity.hStream == 0 || nPlaying == -1) return;
        ArrayList<EffectSaver> arEffectSavers = arEffects.get(nPlayingPlaylist);
        EffectSaver saver = arEffectSavers.get(nPlaying);
        if(saver.isSave()) {
            ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(1);
            LoopFragment loopFragment = (LoopFragment)activity.mSectionsPagerAdapter.getItem(2);
            EqualizerFragment equalizerFragment = (EqualizerFragment)activity.mSectionsPagerAdapter.getItem(3);
            EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
            saver.setSpeed(controlFragment.fSpeed);
            saver.setPitch(controlFragment.fPitch);
            saver.setVol(equalizerFragment.getArSeek().get(0).getProgress() - 30);
            saver.setEQ20K(equalizerFragment.getArSeek().get(1).getProgress() - 30);
            saver.setEQ16K(equalizerFragment.getArSeek().get(2).getProgress() - 30);
            saver.setEQ12_5K(equalizerFragment.getArSeek().get(3).getProgress() - 30);
            saver.setEQ10K(equalizerFragment.getArSeek().get(4).getProgress() - 30);
            saver.setEQ8K(equalizerFragment.getArSeek().get(5).getProgress() - 30);
            saver.setEQ6_3K(equalizerFragment.getArSeek().get(6).getProgress() - 30);
            saver.setEQ5K(equalizerFragment.getArSeek().get(7).getProgress() - 30);
            saver.setEQ4K(equalizerFragment.getArSeek().get(8).getProgress() - 30);
            saver.setEQ3_15K(equalizerFragment.getArSeek().get(9).getProgress() - 30);
            saver.setEQ2_5K(equalizerFragment.getArSeek().get(10).getProgress() - 30);
            saver.setEQ2K(equalizerFragment.getArSeek().get(11).getProgress() - 30);
            saver.setEQ1_6K(equalizerFragment.getArSeek().get(12).getProgress() - 30);
            saver.setEQ1_25K(equalizerFragment.getArSeek().get(13).getProgress() - 30);
            saver.setEQ1K(equalizerFragment.getArSeek().get(14).getProgress() - 30);
            saver.setEQ800(equalizerFragment.getArSeek().get(15).getProgress() - 30);
            saver.setEQ630(equalizerFragment.getArSeek().get(16).getProgress() - 30);
            saver.setEQ500(equalizerFragment.getArSeek().get(17).getProgress() - 30);
            saver.setEQ400(equalizerFragment.getArSeek().get(18).getProgress() - 30);
            saver.setEQ315(equalizerFragment.getArSeek().get(19).getProgress() - 30);
            saver.setEQ250(equalizerFragment.getArSeek().get(20).getProgress() - 30);
            saver.setEQ200(equalizerFragment.getArSeek().get(21).getProgress() - 30);
            saver.setEQ160(equalizerFragment.getArSeek().get(22).getProgress() - 30);
            saver.setEQ125(equalizerFragment.getArSeek().get(23).getProgress() - 30);
            saver.setEQ100(equalizerFragment.getArSeek().get(24).getProgress() - 30);
            saver.setEQ80(equalizerFragment.getArSeek().get(25).getProgress() - 30);
            saver.setEQ63(equalizerFragment.getArSeek().get(26).getProgress() - 30);
            saver.setEQ50(equalizerFragment.getArSeek().get(27).getProgress() - 30);
            saver.setEQ40(equalizerFragment.getArSeek().get(28).getProgress() - 30);
            saver.setEQ31_5(equalizerFragment.getArSeek().get(29).getProgress() - 30);
            saver.setEQ25(equalizerFragment.getArSeek().get(30).getProgress() - 30);
            saver.setEQ20(equalizerFragment.getArSeek().get(31).getProgress() - 30);
            saver.setEffectItems(effectFragment.getEffectItems());
            saver.setPan(effectFragment.getPan());
            saver.setFreq(effectFragment.getFreq());
            saver.setBPM(effectFragment.getBPM());
            saver.setVol1(effectFragment.getVol1());
            saver.setVol2(effectFragment.getVol2());
            saver.setVol3(effectFragment.getVol3());
            saver.setVol4(effectFragment.getVol4());
            saver.setVol5(effectFragment.getVol5());
            saver.setVol6(effectFragment.getVol6());
            saver.setVol7(effectFragment.getVol7());
            LinearLayout ABButton = (LinearLayout)activity.findViewById(R.id.ABButton);
            LinearLayout MarkerButton = (LinearLayout)activity.findViewById(R.id.MarkerButton);
            ImageButton btnLoopmarker = (ImageButton)activity.findViewById(R.id.btnLoopmarker);
            if(ABButton.getVisibility() == View.VISIBLE) saver.setIsABLoop(true);
            else saver.setIsABLoop(false);
            saver.setIsLoop(true);
            saver.setIsLoopA(activity.bLoopA);
            saver.setLoopA(activity.dLoopA);
            saver.setIsLoopB(activity.bLoopB);
            saver.setLoopB(activity.dLoopB);
            saver.setArMarkerTime(loopFragment.getArMarkerTime());
            saver.setIsLoopMarker(btnLoopmarker.isSelected());
            saver.setMarker(loopFragment.getMarker());
            SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
            Gson gson = new Gson();
            preferences.edit().putString("arEffects", gson.toJson(arEffects)).commit();
        }
    }

    public void restoreEffect()
    {
        ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(1);
        LoopFragment loopFragment = (LoopFragment)activity.mSectionsPagerAdapter.getItem(2);
        EqualizerFragment equalizerFragment = (EqualizerFragment)activity.mSectionsPagerAdapter.getItem(3);
        EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
        ArrayList<EffectSaver> arEffectSavers = arEffects.get(nPlayingPlaylist);
        EffectSaver saver = arEffectSavers.get(nPlaying);
        controlFragment.setSpeed(saver.getSpeed(), false);
        controlFragment.setPitch(saver.getPitch(), false);
        equalizerFragment.setVol(saver.getVol(), false);
        equalizerFragment.setEQ(1, saver.getEQ20K(), false);
        equalizerFragment.setEQ(2, saver.getEQ16K(), false);
        equalizerFragment.setEQ(3, saver.getEQ12_5K(), false);
        equalizerFragment.setEQ(4, saver.getEQ10K(), false);
        equalizerFragment.setEQ(5, saver.getEQ8K(), false);
        equalizerFragment.setEQ(6, saver.getEQ6_3K(), false);
        equalizerFragment.setEQ(7, saver.getEQ5K(), false);
        equalizerFragment.setEQ(8, saver.getEQ4K(), false);
        equalizerFragment.setEQ(9, saver.getEQ3_15K(), false);
        equalizerFragment.setEQ(10, saver.getEQ2_5K(), false);
        equalizerFragment.setEQ(11, saver.getEQ2K(), false);
        equalizerFragment.setEQ(12, saver.getEQ1_6K(), false);
        equalizerFragment.setEQ(13, saver.getEQ1_25K(), false);
        equalizerFragment.setEQ(14, saver.getEQ1K(), false);
        equalizerFragment.setEQ(15, saver.getEQ800(), false);
        equalizerFragment.setEQ(16, saver.getEQ630(), false);
        equalizerFragment.setEQ(17, saver.getEQ500(), false);
        equalizerFragment.setEQ(18, saver.getEQ400(), false);
        equalizerFragment.setEQ(19, saver.getEQ315(), false);
        equalizerFragment.setEQ(20, saver.getEQ250(), false);
        equalizerFragment.setEQ(21, saver.getEQ200(), false);
        equalizerFragment.setEQ(22, saver.getEQ160(), false);
        equalizerFragment.setEQ(23, saver.getEQ125(), false);
        equalizerFragment.setEQ(24, saver.getEQ100(), false);
        equalizerFragment.setEQ(25, saver.getEQ80(), false);
        equalizerFragment.setEQ(26, saver.getEQ63(), false);
        equalizerFragment.setEQ(27, saver.getEQ50(), false);
        equalizerFragment.setEQ(28, saver.getEQ40(), false);
        equalizerFragment.setEQ(29, saver.getEQ31_5(), false);
        equalizerFragment.setEQ(30, saver.getEQ25(), false);
        equalizerFragment.setEQ(31, saver.getEQ20(), false);
        ArrayList<EqualizerItem> arEqualizerItems = equalizerFragment.getArEqualizerItems();
        for(int i = 0; i < arEqualizerItems.size(); i++) {
            EqualizerItem item = arEqualizerItems.get(i);
            item.setSelected(false);
        }
        equalizerFragment.getEqualizersAdapter().notifyDataSetChanged();
        effectFragment.setEffectItems(saver.getEffectItems());
        effectFragment.setPan(saver.getPan(), false);
        effectFragment.setFreq(saver.getFreq(), false);
        effectFragment.setBPM(saver.getBPM());
        effectFragment.setVol1(saver.getVol1());
        effectFragment.setVol2(saver.getVol2());
        effectFragment.setVol3(saver.getVol3());
        effectFragment.setVol4(saver.getVol4());
        effectFragment.setVol5(saver.getVol5());
        effectFragment.setVol6(saver.getVol6());
        effectFragment.setVol7(saver.getVol7());
        ImageButton btnLoopmarker = (ImageButton)activity.findViewById(R.id.btnLoopmarker);
        TabLayout tabLayout = (TabLayout)getActivity().findViewById(R.id.abTab_Layout);
        if(saver.isABLoop()) tabLayout.getTabAt(0).select();
        else tabLayout.getTabAt(1).select();
        if(saver.isLoopA()) loopFragment.setLoopA(saver.getLoopA(), false);
        if(saver.isLoopB()) loopFragment.setLoopB(saver.getLoopB(), false);
        loopFragment.setArMarkerTime(saver.getArMarkerTime());
        if(saver.isLoopMarker()) {
            btnLoopmarker.setSelected(true);
            btnLoopmarker.setAlpha(0.3f);
        }
        else {
            btnLoopmarker.setSelected(false);
            btnLoopmarker.setAlpha(1.0f);
        }
        loopFragment.setMarker(saver.getMarker());
    }

    public void saveSong(int nPurpose, String strFileName)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("セーブ中…");
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        progress = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgress(0);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        param.topMargin = (int)(24 *  getResources().getDisplayMetrics().density + 0.5);
        param.leftMargin = (int)(16 *  getResources().getDisplayMetrics().density + 0.5);
        param.rightMargin = (int)(16 *  getResources().getDisplayMetrics().density + 0.5);
        linearLayout.addView(progress, param);
        builder.setView(linearLayout);

        ArrayList<SongItem> arSongs = arPlaylists.get(nSelectedPlaylist);
        SongItem item = arSongs.get(nSelectedItem);
        String strPath = item.getPath();
        int _hTempStream = 0;
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
        Uri uri = Uri.parse(strPath);
        if(uri.getScheme() != null && uri.getScheme().equals("content")) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            boolean bError = false;
            try {
                mmr.setDataSource(activity.getApplicationContext(), Uri.parse(strPath));
            }
            catch(Exception e) {
                bError = true;
            }
            String strMimeType = null;
            if(!bError)
                strMimeType = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            ContentResolver cr = activity.getApplicationContext().getContentResolver();

            try {
                AssetFileDescriptor afd = cr.openAssetFileDescriptor(Uri.parse(strPath), "r");
                if(afd == null) return;
                FileChannel fc = afd.createInputStream().getChannel();
                if(strMimeType == "audio/mp4")
                    _hTempStream = BASS_AAC.BASS_AAC_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS.BASS_STREAM_DECODE, fileprocs, fc);
                else
                    _hTempStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS.BASS_STREAM_DECODE, fileprocs, fc);
            } catch (Exception e) {
                return;
            }
        }
        else {
            _hTempStream = BASS.BASS_StreamCreateFile(strPath, 0, 0, BASS.BASS_STREAM_DECODE);
        }
        if(_hTempStream == 0) return;

        _hTempStream = BASS_FX.BASS_FX_ReverseCreate(_hTempStream, 2, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
        _hTempStream = BASS_FX.BASS_FX_TempoCreate(_hTempStream, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
        final int hTempStream = _hTempStream;
        int chan = BASS_FX.BASS_FX_TempoGetSource(hTempStream);
        EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
        if(effectFragment.isReverse())
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
        ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(1);
        BASS.BASS_ChannelSetAttribute(hTempStream, BASS_FX.BASS_ATTRIB_TEMPO, controlFragment.fSpeed);
        BASS.BASS_ChannelSetAttribute(hTempStream, BASS_FX.BASS_ATTRIB_TEMPO_PITCH, controlFragment.fPitch);
        EqualizerFragment equalizerFragment = (EqualizerFragment)activity.mSectionsPagerAdapter.getItem(3);
        int[] arHFX = new int[] {hTempFx20K, hTempFx16K, hTempFx12_5K, hTempFx10K, hTempFx8K, hTempFx6_3K, hTempFx5K, hTempFx4K, hTempFx3_15K, hTempFx2_5K, hTempFx2K, hTempFx1_6K, hTempFx1_25K, hTempFx1K, hTempFx800, hTempFx630, hTempFx500, hTempFx400, hTempFx315, hTempFx250, hTempFx200, hTempFx160, hTempFx125, hTempFx100, hTempFx80, hTempFx63, hTempFx50, hTempFx40, hTempFx31_5, hTempFx25, hTempFx20};
        int nVol = equalizerFragment.getArSeek().get(0).getProgress() - 30;
        float fLevel = nVol;
        if(fLevel == 0) fLevel = 1.0f;
        else if(fLevel < 0) fLevel = (fLevel + 30.0f) / 30.0f;
        else fLevel += 1.0f;
        BASS_FX.BASS_BFX_VOLUME vol = new BASS_FX.BASS_BFX_VOLUME();
        vol.lChannel = 0;
        vol.fVolume = fLevel;
        BASS.BASS_FXSetParameters(hTempFxVol, vol);

        for(int i = 0; i < 31; i++)
        {
            int nLevel = equalizerFragment.getArSeek().get(i+1).getProgress() - 30;
            BASS_FX.BASS_BFX_PEAKEQ eq = new BASS_FX.BASS_BFX_PEAKEQ();
            eq.fBandwidth = 0;
            eq.fQ = 0.7f;
            eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
            eq.fGain = nLevel;
            eq.fCenter = equalizerFragment.getArCenters()[i];
            BASS.BASS_FXSetParameters(arHFX[i], eq);
        }
        effectFragment.applyEffect(hTempStream);
        String strPathTo;
        if(nPurpose == 0) {
            int i = 0;
            File fileForCheck;
            while (true) {
                strPathTo = activity.getFilesDir() + "/recorded" + String.format("%d", i) + ".mp3";
                fileForCheck = new File(strPathTo);
                if (!fileForCheck.exists()) break;
                i++;
            }
        }
        else {
            File fileDir = new File(activity.getExternalCacheDir() + "/export");
            if(!fileDir.exists()) fileDir.mkdir();
            strPathTo = activity.getExternalCacheDir() + "/export/";
            strPathTo += strFileName + ".mp3";
            File file = new File(strPathTo);
            if(file.exists()) file.delete();
        }

        double _dEnd = BASS.BASS_ChannelBytes2Seconds(hTempStream, BASS.BASS_ChannelGetLength(hTempStream, BASS.BASS_POS_BYTE));
        if(nSelectedPlaylist == nPlayingPlaylist && nSelectedItem == nPlaying)
        {
            if(activity.bLoopA)
                BASS.BASS_ChannelSetPosition(hTempStream, BASS.BASS_ChannelSeconds2Bytes(hTempStream, activity.dLoopA), BASS.BASS_POS_BYTE);
            if(activity.bLoopB)
                _dEnd = activity.dLoopB;
        }
        final double dEnd = _dEnd;
        final int hEncode = BASSenc_MP3.BASS_Encode_MP3_StartFile(hTempStream, "", 0, strPathTo);
        bFinish = false;
        builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                bFinish = true;
            }
        });
        AlertDialog alert = builder.show();

        if(task != null && task.getStatus() == AsyncTask.Status.RUNNING)
            task.cancel(true);
        task = new SongSavingTask(nPurpose, this, hTempStream, hEncode, strPathTo, alert, dEnd);
        task.execute(0);
    }

    public void saveSongToLocal()
    {
        saveSong(0, null);
    }

    public void finishSaveSongToLocal(int hTempStream, int hEncode, String strPathTo, AlertDialog alert)
    {
        if(alert.isShowing()) alert.dismiss();

        BASSenc.BASS_Encode_Stop(hEncode);
        BASS.BASS_StreamFree(hTempStream);

        if(bFinish) {
            File file = new File(strPathTo);
            file.delete();
            bFinish = false;
            return;
        }

        ArrayList<SongItem> arSongs = arPlaylists.get(nSelectedPlaylist);
        SongItem item = arSongs.get(nSelectedItem);
        ArrayList<EffectSaver> arEffectSavers = arEffects.get(nSelectedPlaylist);
        EffectSaver saver = arEffectSavers.get(nSelectedItem);
        ArrayList<String> arTempLyrics = arLyrics.get(nSelectedPlaylist);
        String strLyrics = arTempLyrics.get(nSelectedItem);

        String strTitle = item.getTitle();
        ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(1);
        float fSpeed = controlFragment.fSpeed;
        float fPitch = controlFragment.fPitch;
        String strSpeed = String.format("%.1f%%", fSpeed + 100);
        String strPitch = "";
        if(fPitch >= 0.05f)
            strPitch = String.format("♯%.1f", fPitch);
        else if(fPitch <= -0.05f)
            strPitch = String.format("♭%.1f", fPitch * -1);
        else {
            strPitch = String.format("%.1f", fPitch < 0.0f ? fPitch * -1 : fPitch);
            if(strPitch.equals("-0.0")) strPitch = "0.0";
        }

        if(fSpeed != 0.0f && fPitch != 0.0f)
            strTitle += "(速度" + strSpeed + ",音程" + strPitch + ")";
        else if(fSpeed != 0.0f)
            strTitle += "(速度" + strSpeed + ")";
        else if(fPitch != 0.0f)
            strTitle += "(音程" + strPitch + ")";

        SongItem itemNew = new SongItem(String.format("%d", arSongs.size()+1), strTitle, item.getArtist(), strPathTo);
        arSongs.add(itemNew);
        EffectSaver saverNew = new EffectSaver(saver);
        arEffectSavers.add(saverNew);
        arTempLyrics.add(strLyrics);
        if(nSelectedPlaylist == nPlayingPlaylist) arPlayed.add(false);
        songsAdapter.notifyDataSetChanged();
        SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        preferences.edit().putString("arPlaylists", gson.toJson(arPlaylists)).commit();
        preferences.edit().putString("arEffects", gson.toJson(arEffects)).commit();
        preferences.edit().putString("arLyrics", gson.toJson(arLyrics)).commit();
        preferences.edit().putString("arPlaylistNames", gson.toJson(arPlaylistNames)).commit();
    }

    public void export()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("他のアプリにエクスポート");
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final EditText editTitle = new EditText (activity);
        editTitle.setHint("ファイル名");
        editTitle.setHintTextColor(Color.argb(255, 192, 192, 192));
        ArrayList<SongItem> arSongs = arPlaylists.get(nSelectedPlaylist);
        SongItem item = arSongs.get(nSelectedItem);
        String strTitle = item.getTitle();
        ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(1);
        float fSpeed = controlFragment.fSpeed;
        float fPitch = controlFragment.fPitch;
        String strSpeed = String.format("%.1f%%", fSpeed + 100);
        String strPitch = "";
        if(fPitch >= 0.05f)
            strPitch = String.format("♯%.1f", fPitch);
        else if(fPitch <= -0.05f)
            strPitch = String.format("♭%.1f", fPitch * -1);
        else {
            strPitch = String.format("%.1f", fPitch < 0.0f ? fPitch * -1 : fPitch);
            if(strPitch.equals("-0.0")) strPitch = "0.0";
        }
        if(fSpeed != 0.0f && fPitch != 0.0f)
            strTitle += "(速度" + strSpeed + ",音程" + strPitch + ")";
        else if(fSpeed != 0.0f)
            strTitle += "(速度" + strSpeed + ")";
        else if(fPitch != 0.0f)
            strTitle += "(音程" + strPitch + ")";
        DateFormat df = new SimpleDateFormat("_yyyyMMdd_HHmmss");
        Date date = new Date(System.currentTimeMillis());
        editTitle.setText(strTitle + df.format(date));
        linearLayout.addView(editTitle);
        builder.setView(linearLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                saveSong(1, editTitle.getText().toString());
            }
        });
        builder.setNegativeButton("キャンセル", null);
        builder.show();
    }

    public void finishExport(int hTempStream, int hEncode, String strPathTo, AlertDialog alert)
    {
        if(alert.isShowing()) alert.dismiss();

        BASSenc.BASS_Encode_Stop(hEncode);
        BASS.BASS_StreamFree(hTempStream);

        if(bFinish) {
            File file = new File(strPathTo);
            file.delete();
            bFinish = false;
            return;
        }

        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND);
        share.setType("audio/mp3");
        File file = new File(strPathTo);
        Uri uri = FileProvider.getUriForFile(getContext(), "com.edolfzoku.hayaemon2", file);
        List<ResolveInfo> resInfoList = getContext().getPackageManager().queryIntentActivities(share, PackageManager.MATCH_ALL);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            getContext().grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        share.putExtra(Intent.EXTRA_STREAM, uri);
        startActivityForResult(Intent.createChooser(share, "他のアプリにエクスポート"), 0);

        file.deleteOnExit();
    }

    public void play()
    {
        if(MainActivity.hStream == 0) return;
        BASS.BASS_ChannelPlay(MainActivity.hStream, false);
        Button btnPlay = (Button)getActivity().findViewById(R.id.btnPlay);
        btnPlay.setText("一時停止");
        btnPlay.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_pause, 0, 0);
        songsAdapter.notifyDataSetChanged();
    }

    public void pause()
    {
        if(MainActivity.hStream == 0) return;
        BASS.BASS_ChannelPause(MainActivity.hStream);
        Button btnPlay = (Button)getActivity().findViewById(R.id.btnPlay);
        btnPlay.setText("再生");
        btnPlay.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_play, 0, 0);
        songsAdapter.notifyDataSetChanged();
    }

    public void playPrev()
    {
        if(MainActivity.hStream == 0) return;
        nPlaying--;
        if(nPlaying < 0) return;
        playSong(nPlaying);
    }

    public void playNext()
    {
        int nTempPlaying = nPlaying;
        MainActivity activity = (MainActivity)getActivity();
        Button btnPlayMode = (Button)activity.findViewById(R.id.btnPlayMode);
        ArrayList<SongItem> arSongs = arPlaylists.get(nPlayingPlaylist);
        if(btnPlayMode.getText().equals("連続再生") || btnPlayMode.getText().equals("１曲リピート"))
        {
            nTempPlaying++;
            if(nTempPlaying >= arSongs.size())
            {
                stop();
                return;
            }
        }
        else if(btnPlayMode.getText().equals("全曲リピート"))
        {
            nTempPlaying++;
            if(nTempPlaying >= arSongs.size())
            {
                nTempPlaying = 0;
            }
        }
        else if(btnPlayMode.getText().equals("シャッフル"))
        {
            ArrayList<Integer> arTemp = new ArrayList<Integer>();
            for(int i = 0; i < arPlayed.size(); i++)
            {
                if(i == nTempPlaying) continue;
                Boolean bPlayed = arPlayed.get(i);
                if(!bPlayed.booleanValue())
                {
                    arTemp.add(i);
                }
            }
            if(arTemp.size() == 0)
            {
                for(int i = 0; i < arPlayed.size(); i++)
                {
                    arPlayed.set(i, false);
                }
            }
            if(arPlayed.size() > 1)
            {
                Random random = new Random();
                if(arTemp.size() == 0 || arTemp.size() == arPlayed.size())
                {
                    nTempPlaying = random.nextInt(arPlayed.size());
                }
                else
                {
                    int nRandom = random.nextInt(arTemp.size());
                    nTempPlaying = arTemp.get(nRandom);
                }
            }
        }
        playSong(nTempPlaying);
    }

    public void onPlaylistItemClick(int nPlaylist)
    {
        selectPlaylist(nPlaylist);
        RelativeLayout relativeSongs = (RelativeLayout)activity.findViewById(R.id.relativeSongs);
        relativeSongs.setVisibility(View.VISIBLE);
        RelativeLayout relativePlaylists = (RelativeLayout)activity.findViewById(R.id.relativePlaylists);
        relativePlaylists.setVisibility(View.INVISIBLE);
    }

    public void onSongItemClick(int nSong)
    {
        ArrayList<SongItem> arSongs = arPlaylists.get(nSelectedPlaylist);
        if(nPlayingPlaylist == nSelectedPlaylist && nPlaying == nSong)
        {
            if(BASS.BASS_ChannelIsActive(activity.hStream) == BASS.BASS_ACTIVE_PLAYING)
                pause();
            else play();
            return;
        }
        if(nPlayingPlaylist != nSelectedPlaylist) {
            arPlayed = new ArrayList<Boolean>();
            for(int i = 0; i < arSongs.size(); i++)
                arPlayed.add(false);
        }
        nPlayingPlaylist = nSelectedPlaylist;
        playSong(nSong);
    }

    public void playSong(int nSong)
    {
        MainActivity activity = (MainActivity)getActivity();
        activity.clearLoop(false);

        boolean bReloadLyrics = false;
        RelativeLayout relativeLyrics = (RelativeLayout)activity.findViewById(R.id.relativeLyrics);
        TextView textLyrics = (TextView)activity.findViewById(R.id.textLyrics);
        if(relativeLyrics.getVisibility() == View.VISIBLE && textLyrics.getVisibility() == View.VISIBLE && nPlayingPlaylist == nSelectedPlaylist && nPlaying == nSelectedItem) {
            bReloadLyrics = true;
            nSelectedItem = nSong;
        }

        ArrayList<EffectSaver> arEffectSavers = arEffects.get(nPlayingPlaylist);
        if(0 <= nPlaying && nPlaying < arEffectSavers.size() && 0 <= nSong && nSong < arEffectSavers.size()) {
            EffectSaver saverBefore = arEffectSavers.get(nPlaying);
            EffectSaver saverAfter = arEffectSavers.get(nSong);
            if(saverBefore.isSave() && !saverAfter.isSave()) {
                ControlFragment controlFragment = (ControlFragment) activity.mSectionsPagerAdapter.getItem(1);
                controlFragment.setSpeed(0.0f, false);
                controlFragment.setPitch(0.0f, false);
                EqualizerFragment equalizerFragment = (EqualizerFragment) activity.mSectionsPagerAdapter.getItem(3);
                equalizerFragment.setVol(0, false);
                for (int i = 1; i <= 31; i++) {
                    equalizerFragment.setEQ(i, 0, false);
                }
                ArrayList<EqualizerItem> arEqualizerItems = equalizerFragment.getArEqualizerItems();
                for(int i = 0; i < arEqualizerItems.size(); i++) {
                    EqualizerItem item = arEqualizerItems.get(i);
                    item.setSelected(false);
                }
                equalizerFragment.getEqualizersAdapter().notifyDataSetChanged();
                EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
                effectFragment.resetEffect();
            }
        }
        nPlaying = nSong;
        if(arPlaylists.size() == 0 || nPlayingPlaylist >= arPlaylists.size() || arPlaylists.get(nPlayingPlaylist).size() == 0 || nSong >= arPlaylists.get(nPlayingPlaylist).size())
            return;
        SongItem item = arPlaylists.get(nPlayingPlaylist).get(nSong);
        String strPath = item.getPath();
        if(MainActivity.hStream != 0)
        {
            BASS.BASS_StreamFree(MainActivity.hStream);
            MainActivity.hStream = 0;
        }
        arPlayed.set(nSong, true);

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

        Uri uri = Uri.parse(strPath);
        if(uri.getScheme() != null && uri.getScheme().equals("content")) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            boolean bError = false;
            try {
                mmr.setDataSource(activity.getApplicationContext(), Uri.parse(strPath));
            }
            catch(Exception e) {
                bError = true;
            }
            String strMimeType = null;
            if(!bError)
                strMimeType = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            ContentResolver cr = activity.getApplicationContext().getContentResolver();

            try {
                AssetFileDescriptor afd = cr.openAssetFileDescriptor(Uri.parse(strPath), "r");
                if(afd == null) return;
                FileChannel fc = afd.createInputStream().getChannel();
                if(strMimeType == "audio/mp4")
                    MainActivity.hStream = BASS_AAC.BASS_AAC_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS.BASS_STREAM_DECODE, fileprocs, fc);
                else
                    MainActivity.hStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS.BASS_STREAM_DECODE, fileprocs, fc);
            } catch (Exception e) {
                removeSong(nPlayingPlaylist, nPlaying);
                if(nPlaying >= arPlaylists.get(nPlayingPlaylist).size())
                    nPlaying = 0;
                if(arPlaylists.get(nPlayingPlaylist).size() != 0)
                    playSong(nPlaying);
                return;
            }
        }
        else {
            MainActivity.hStream = BASS.BASS_StreamCreateFile(strPath, 0, 0, BASS.BASS_STREAM_DECODE);
        }
        if(MainActivity.hStream == 0) return;

        MainActivity.hStream = BASS_FX.BASS_FX_ReverseCreate(MainActivity.hStream, 2, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
        MainActivity.hStream = BASS_FX.BASS_FX_TempoCreate(MainActivity.hStream, BASS_FX.BASS_FX_FREESOURCE);
        int chan = BASS_FX.BASS_FX_TempoGetSource(MainActivity.hStream);
        EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
        if(effectFragment.isReverse())
            BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_REVERSE);
        else
            BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_FORWARD);
        MainActivity.hFxVol = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_VOLUME, 0);
        hFx20K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx16K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx12_5K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx10K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx8K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx6_3K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx5K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx4K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx3_15K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx2_5K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx2K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx1_6K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx1_25K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx1K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx800 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx630 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx500 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx400 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx315 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx250 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx200 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx160 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx125 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx100 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx80 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx63 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx50 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx40 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx31_5 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx25 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx20 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        EqualizerFragment equalizerFragment = (EqualizerFragment) activity.mSectionsPagerAdapter.getItem(3);
        equalizerFragment.setArHFX(new int[]{hFx20K, hFx16K, hFx12_5K, hFx10K, hFx8K, hFx6_3K, hFx5K, hFx4K, hFx3_15K, hFx2_5K, hFx2K, hFx1_6K, hFx1_25K, hFx1K, hFx800, hFx630, hFx500, hFx400, hFx315, hFx250, hFx200, hFx160, hFx125, hFx100, hFx80, hFx63, hFx50, hFx40, hFx31_5, hFx25, hFx20});
        EffectSaver saver = arEffectSavers.get(nPlaying);
        if(saver.isSave()) restoreEffect();
        ControlFragment controlFragment = (ControlFragment) activity.mSectionsPagerAdapter.getItem(1);
        BASS.BASS_ChannelSetAttribute(MainActivity.hStream, BASS_FX.BASS_ATTRIB_TEMPO, controlFragment.fSpeed);
        BASS.BASS_ChannelSetAttribute(MainActivity.hStream, BASS_FX.BASS_ATTRIB_TEMPO_PITCH, controlFragment.fPitch);
        equalizerFragment.setEQ();
        effectFragment.applyEffect(MainActivity.hStream);
        activity.setSync();
        BASS.BASS_ChannelPlay(MainActivity.hStream, false);
        Button btnPlay = (Button)getActivity().findViewById(R.id.btnPlay);
        btnPlay.setText("一時停止");
        btnPlay.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_pause,0,0);
        LoopFragment loopFragment = (LoopFragment)activity.mSectionsPagerAdapter.getItem(2);
        loopFragment.drawWaveForm(strPath);
        songsAdapter.notifyDataSetChanged();
        if(bReloadLyrics) showLyrics();
    }

    public String getLyrics(int nPlaylist, int nSong) {
        ArrayList<SongItem> arSongs = arPlaylists.get(nPlaylist);
        final SongItem songItem = arSongs.get(nSong);

        try {
            File file = new File(getFilePath(getContext(), Uri.parse(songItem.getPath())));
            Mp3File mp3file = new Mp3File(file);
            ID3v2 id3v2Tag;
            if (mp3file.hasId3v2Tag()) {
                id3v2Tag = mp3file.getId3v2Tag();
                return id3v2Tag.getLyrics();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("NewApi")
    public static String getFilePath(Context context, Uri uri) throws URISyntaxException {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public void stop()
    {
        if(MainActivity.hStream == 0) return;
        nPlaying = -1;
        BASS.BASS_ChannelStop(MainActivity.hStream);
        MainActivity.hStream = 0;
        Button btnPlay = (Button)getActivity().findViewById(R.id.btnPlay);
        btnPlay.setText("再生");
        btnPlay.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_play, 0, 0);
        MainActivity activity = (MainActivity)getActivity();
        activity.clearLoop();
        songsAdapter.notifyDataSetChanged();
    }

    public void addSong(MainActivity activity, Uri uri)
    {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        boolean bError = false;
        try {
            mmr.setDataSource(activity.getApplicationContext(), uri);
        }
        catch(Exception e) {
            bError = true;
        }
        ArrayList<SongItem> arSongs = arPlaylists.get(nSelectedPlaylist);
        String strTitle = null;
        String strArtist = null;
        if(!bError) {
            strTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            strArtist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        }
        if(strTitle != null) {
            SongItem item = new SongItem(String.format("%d", arSongs.size()+1), strTitle, strArtist, uri.toString());
            arSongs.add(item);
        }
        else
        {
            strTitle = getFileNameFromUri(activity.getApplicationContext(), uri);
            if(strTitle == null) {
                int startIndex = uri.toString().lastIndexOf('/');
                strTitle = uri.toString().substring(startIndex + 1);
            }
            SongItem item = new SongItem(String.format("%d", arSongs.size()+1), strTitle, "", uri.toString());
            arSongs.add(item);
        }
        ArrayList<EffectSaver> arEffectSavers = arEffects.get(nSelectedPlaylist);
        EffectSaver saver = new EffectSaver();
        arEffectSavers.add(saver);

        ArrayList<String> arTempLyrics = arLyrics.get(nSelectedPlaylist);
        arTempLyrics.add(null);

        if(nSelectedPlaylist == nPlayingPlaylist) arPlayed.add(false);
    }

    public void removeSong(int nPlaylist, int nSong)
    {
        if(nSong < nPlaying) nPlaying--;

        ArrayList<SongItem> arSongs = arPlaylists.get(nPlaylist);
        SongItem song = arSongs.get(nSong);
        File file = new File(song.getPath());
        if(file.getParent().equals(activity.getFilesDir())) {
            file.delete();
        }

        arSongs.remove(nSong);
        if(nPlaylist == nPlayingPlaylist) arPlayed.remove(nSong);

        for(int i = nSong; i < arSongs.size(); i++) {
            SongItem songItem = arSongs.get(i);
            songItem.setNumber(String.format("%d", i+1));
        }

        songsAdapter.notifyDataSetChanged();

        ArrayList<EffectSaver> arEffectSavers = arEffects.get(nPlaylist);
        arEffectSavers.remove(nSong);

        ArrayList<String> arTempLyrics = arLyrics.get(nPlaylist);
        arTempLyrics.remove(nSong);

        SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        preferences.edit().putString("arPlaylists", gson.toJson(arPlaylists)).commit();
        preferences.edit().putString("arEffects", gson.toJson(arEffects)).commit();
        preferences.edit().putString("arLyrics", gson.toJson(arLyrics)).commit();
        preferences.edit().putString("arPlaylistNames", gson.toJson(arPlaylistNames)).commit();
    }

    public String getFileNameFromUri(Context context, Uri uri) {
        if (null == uri) return null;

        String scheme = uri.getScheme();

        String fileName = null;
        if(scheme == null) return null;
        switch (scheme) {
            case "content":
                String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
                Cursor cursor = null;
                try {
                    cursor = context.getContentResolver()
                            .query(uri, projection, null, null, null);
                }
                catch(Exception e) {
                    return null;
                }
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        fileName = cursor.getString(
                                cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
                    }
                    cursor.close();
                }
                break;

            case "file":
                fileName = new File(uri.getPath()).getName();
                break;

            default:
                break;
        }
        return fileName;
    }

    public void selectPlaylist(int nSelect)
    {
        if(recyclerTab != null) recyclerTab.scrollToPosition(nSelect);
        nSelectedPlaylist = nSelect;
        ArrayList<SongItem> arSongs = arPlaylists.get(nSelect);
        if(tabAdapter != null) tabAdapter.notifyDataSetChanged();
        if(songsAdapter != null) {
            songsAdapter.changeItems(arSongs);
            songsAdapter.notifyDataSetChanged();
        }
        if(playlistsAdapter != null) playlistsAdapter.notifyDataSetChanged();
    }

    public void updateSongs()
    {
        if(songsAdapter != null)
            songsAdapter.notifyDataSetChanged();
    }
}
