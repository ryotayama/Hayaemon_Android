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
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS_FX;
import com.un4seen.bass.BASSenc;
import com.un4seen.bass.BASSenc_MP3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.text.DateFormat;

import static android.app.Activity.RESULT_OK;

public class PlaylistFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    private MainActivity mActivity = null;
    private ArrayList<String> mPlaylistNames;
    private  ArrayList<ArrayList<SongItem>> mPlaylists;
    private ArrayList<ArrayList<EffectSaver>> mEffects;
    private ArrayList<ArrayList<String>> mLyrics;
    private List<Boolean> mPlays;
    private PlaylistsAdapter mPlaylistsAdapter;
    private PlaylistTabAdapter mTabAdapter;
    private SongsAdapter mSongsAdapter;
    private ItemTouchHelper mPlaylistTouchHelper;
    private ItemTouchHelper mSongTouchHelper;
    private int mPlayingPlaylist = -1;
    private int mSelectedPlaylist = 0;
    private int mPlaying;
    private int mSelectedItem;
    private boolean mSorting = false;
    private boolean mMultiSelecting = false;
    private boolean mAllowSelectNone = false;
    private ByteBuffer mRecbuf;
    private SongSavingTask mSongSavingTask;
    private VideoSavingTask mVideoSavingTask;
    private DownloadTask mDownloadTask;
    private boolean mFinish = false;
    private ProgressBar mProgress;
    private boolean mForceNormal = false;
    private boolean mForceReverse = false;

    private RecyclerView mRecyclerPlaylists, mRecyclerTab, mRecyclerSongs;
    private Button mBtnSortPlaylist, mBtnFinishLyrics;
    private AnimationButton mBtnAddPlaylist, mBtnArtworkInPlayingBar, mBtnAddSong, mBtnEdit;
    private TextView mTextTitleInPlayingBar, mTextArtistInPlayingBar, mTextFinishSort, mTextLyricsTitle, mTextNoLyrics, mTextLyrics, mTextTapEdit, mTextPlaylistInMultipleSelection, mTextPlaylist;
    private RelativeLayout mRelativeSongs, mRelativePlaylists, mRelativeLyrics, mRelativeLyricsTitle;
    private ImageView mImgEdit, mImgSelectAllInMultipleSelection;
    private EditText mEditLyrics;
    private ImageButton mBtnLeft, mBtnAddPlaylist_small;
    private View mDevider1, mDevider2, mViewMultipleSelection, mViewSepLyrics;

    public ArrayList<ArrayList<SongItem>> getPlaylists() { return mPlaylists; }
    public void setPlaylists(ArrayList<ArrayList<SongItem>> arLists) { mPlaylists = arLists; }
    public ArrayList<ArrayList<EffectSaver>> getEffects() { return mEffects; }
    public void setEffects(ArrayList<ArrayList<EffectSaver>> mEffects) { this.mEffects = mEffects; }
    public ArrayList<ArrayList<String>> getLyrics() { return mLyrics; }
    public void setLyrics(ArrayList<ArrayList<String>> mLyrics) { this.mLyrics = mLyrics; }
    public ArrayList<String> getPlaylistNames() { return mPlaylistNames; }
    public void setPlaylistNames(ArrayList<String> arNames) { mPlaylistNames = arNames; }
    public int getSelectedPlaylist() { return mSelectedPlaylist; }
    public void setSelectedItem(int nSelected) { mSelectedItem = nSelected; }
    public int getSelectedItem() { return mSelectedItem; }
    public int getPlaying() { return mPlaying; }
    public int getPlayingPlaylist() { return mPlayingPlaylist; }
    public ItemTouchHelper getPlaylistTouchHelper() { return mPlaylistTouchHelper; }
    public ItemTouchHelper getSongTouchHelper() { return mSongTouchHelper; }
    public boolean isSorting() { return mSorting; }
    public boolean isMultiSelecting() { return mMultiSelecting; }
    public int getSongCount(int nPlaylist) { return mPlaylists.get(nPlaylist).size(); }
    public SongsAdapter getSongsAdapter() { return mSongsAdapter; }
    public boolean isFinish() { return mFinish; }
    public void setProgress(int nProgress) { mProgress.setProgress(nProgress); }
    public boolean isSelected(int nSong) {
        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        SongItem item = arSongs.get(nSong);
        return item.isSelected();
    }
    public boolean isLock(int nSong) {
        ArrayList<EffectSaver> arEffectSavers = mEffects.get(mSelectedPlaylist);
        EffectSaver saver = arEffectSavers.get(nSong);
        return saver.isSave();
    }

    public PlaylistFragment()
    {
        mPlaying = -1;
        mPlaylistNames = new ArrayList<>();
        mPlaylists = new ArrayList<>();
        mEffects = new ArrayList<>();
        mLyrics = new ArrayList<>();
        mPlays = new ArrayList<>();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof MainActivity) {
            mActivity = (MainActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mActivity = null;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnSortPlaylist)
        {
            if(mSorting)
            {
                mRecyclerPlaylists.setPadding(0, 0, 0, (int)(80 * mActivity.getDensity()));
                mBtnAddPlaylist.setVisibility(View.VISIBLE);
                mSorting = false;
                mPlaylistsAdapter.notifyDataSetChanged();
                mBtnSortPlaylist.setText(R.string.sort);
                mPlaylistTouchHelper.attachToRecyclerView(null);
            }
            else
            {
                mRecyclerPlaylists.setPadding(0, 0, 0, 0);
                mBtnAddPlaylist.setVisibility(View.GONE);
                mSorting = true;
                mPlaylistsAdapter.notifyDataSetChanged();
                mBtnSortPlaylist.setText(R.string.finishSort);

                mPlaylistTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
                    @Override
                    public boolean onMove(RecyclerView mRecyclerSongs, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        final int fromPos = viewHolder.getAdapterPosition();
                        final int toPos = target.getAdapterPosition();

                        ArrayList<SongItem> arSongsTemp = mPlaylists.get(fromPos);
                        mPlaylists.remove(fromPos);
                        mPlaylists.add(toPos, arSongsTemp);

                        ArrayList<EffectSaver> arEffectSavers = mEffects.get(fromPos);
                        mEffects.remove(fromPos);
                        mEffects.add(toPos, arEffectSavers);

                        ArrayList<String> arTempLyrics = mLyrics.get(fromPos);
                        mLyrics.remove(fromPos);
                        mLyrics.add(toPos, arTempLyrics);

                        String strTemp = mPlaylistNames.get(fromPos);
                        mPlaylistNames.remove(fromPos);
                        mPlaylistNames.add(toPos, strTemp);

                        if(fromPos == mPlayingPlaylist) mPlayingPlaylist = toPos;
                        else if(fromPos < mPlayingPlaylist && mPlayingPlaylist <= toPos) mPlayingPlaylist--;
                        else if(fromPos > mPlayingPlaylist && mPlayingPlaylist >= toPos) mPlayingPlaylist++;

                        mTabAdapter.notifyItemMoved(fromPos, toPos);
                        mPlaylistsAdapter.notifyItemMoved(fromPos, toPos);

                        return true;
                    }

                    @Override
                    public void clearView(RecyclerView mRecyclerSongs, RecyclerView.ViewHolder viewHolder) {
                        super.clearView(mRecyclerSongs, viewHolder);

                        mTabAdapter.notifyDataSetChanged();
                        mPlaylistsAdapter.notifyDataSetChanged();

                        saveFiles(true, true, true, true, false);
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    }
                });
                mPlaylistTouchHelper.attachToRecyclerView(mRecyclerPlaylists);
            }
        }
        else if(v.getId() == R.id.btnAddPlaylist)
        {
            final Handler handler = new Handler();
            Runnable timer=new Runnable() {
                public void run()
                {
                    AlertDialog.Builder builder;
                    if(mActivity.isDarkMode())
                        builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
                    else
                        builder = new AlertDialog.Builder(mActivity);
                    builder.setTitle(R.string.addNewList);
                    final ClearableEditText editText = new ClearableEditText(mActivity, mActivity.isDarkMode());
                    editText.setHint(R.string.playlist);
                    editText.setText(R.string.playlist);
                    builder.setView(editText);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            addPlaylist(editText.getText().toString());
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, null);
                    final AlertDialog alertDialog = builder.create();
                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                    {
                        @Override
                        public void onShow(DialogInterface arg0)
                        {
                            if(alertDialog.getWindow() != null) {
                                WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                                lp.dimAmount = 0.4f;
                                alertDialog.getWindow().setAttributes(lp);
                            }
                            editText.requestFocus();
                            editText.setSelection(editText.getText().toString().length());
                            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (null != imm) imm.showSoftInput(editText, 0);
                        }
                    });
                    alertDialog.show();
                }
            };
            handler.postDelayed(timer, 80);
        }
        else if(v.getId() == R.id.btnRewind)
            onRewindBtnClick();
        else if(v.getId() == R.id.btnPlay)
            onPlayBtnClick();
        else if(v.getId() == R.id.btnForward)
            onForwardBtnClick();
        else if(v.getId() == R.id.btnRecord) {
            if(MainActivity.sRecord != 0) stopRecord();
            else startRecord();
        }
        else if(v.getId() == R.id.btnLeft)
        {
            mRelativeSongs.setVisibility(View.INVISIBLE);
            mPlaylistsAdapter.notifyDataSetChanged();
            mRelativePlaylists.setVisibility(View.VISIBLE);
            mActivity.getViewSep1().setVisibility(View.VISIBLE);
        }
        else if(v.getId() == R.id.btnAddPlaylist_small)
        {
            AlertDialog.Builder builder;
            if(mActivity.isDarkMode())
                builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
            else
                builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.addNewList);
            final ClearableEditText editText = new ClearableEditText(mActivity, mActivity.isDarkMode());
            editText.setHint(R.string.playlist);
            editText.setText(R.string.playlist);
            builder.setView(editText);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    addPlaylist(editText.getText().toString());
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
            {
                @Override
                public void onShow(DialogInterface arg0)
                {
                    if(alertDialog.getWindow() != null) {
                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                        lp.dimAmount = 0.4f;
                        alertDialog.getWindow().setAttributes(lp);
                    }
                    editText.requestFocus();
                    editText.setSelection(editText.getText().toString().length());
                    InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (null != imm) imm.showSoftInput(editText, 0);
                }
            });
            alertDialog.show();
        }
        else if(v.getId() == R.id.btnAddSong)
        {
            final Handler handler = new Handler();
            Runnable timer=new Runnable() {
                public void run()
                {
                    final BottomMenu menu = new BottomMenu(mActivity);
                    menu.setTitle(getString(R.string.addSong));
                    menu.addMenu(getString(R.string.addFromLocal), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_music_dark : R.drawable.ic_actionsheet_music, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            menu.dismiss();
                            mActivity.open();
                        }
                    });
                    if(Build.VERSION.SDK_INT >= 18) {
                        menu.addMenu(getString(R.string.addFromVideo), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_film_dark : R.drawable.ic_actionsheet_film, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                menu.dismiss();
                                mActivity.openGallery();
                            }
                        });
                    }
                    menu.addMenu(getString(R.string.addURL), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_globe_dark : R.drawable.ic_actionsheet_globe, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            menu.dismiss();

                            AlertDialog.Builder builder;
                            if(mActivity.isDarkMode())
                                builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
                            else
                                builder = new AlertDialog.Builder(mActivity);
                            builder.setTitle(R.string.addURL);
                            LinearLayout linearLayout = new LinearLayout(mActivity);
                            linearLayout.setOrientation(LinearLayout.VERTICAL);
                            final ClearableEditText editURL = new ClearableEditText(mActivity, mActivity.isDarkMode());
                            editURL.setHint(R.string.URL);
                            editURL.setText("");
                            linearLayout.addView(editURL);
                            builder.setView(linearLayout);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    startAddURL(editURL.getText().toString());
                                }
                            });
                            builder.setNegativeButton(R.string.cancel, null);
                            final AlertDialog alertDialog = builder.create();
                            alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                            {
                                @Override
                                public void onShow(DialogInterface arg0)
                                {
                                    if(alertDialog.getWindow() != null) {
                                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                                        lp.dimAmount = 0.4f;
                                        alertDialog.getWindow().setAttributes(lp);
                                    }
                                    editURL.requestFocus();
                                    editURL.setSelection(editURL.getText().toString().length());
                                    InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                    if (null != imm) imm.showSoftInput(editURL, 0);
                                }
                            });
                            alertDialog.show();
                        }
                    });
                    menu.setCancelMenu();
                    menu.show();
                }
            };
            handler.postDelayed(timer, 80);
        }
        else if(v.getId() == R.id.textFinishSort)
        {
            mRecyclerSongs.setPadding(0, 0, 0, (int)(80 * mActivity.getDensity()));
            mTextFinishSort.setVisibility(View.GONE);
            mBtnAddSong.setVisibility(View.VISIBLE);
            mSorting = false;
            mSongsAdapter.notifyDataSetChanged();

            mSongTouchHelper.attachToRecyclerView(null);
        }
        else if(v.getId() == R.id.btnFinishLyrics)
        {
            if(mBtnFinishLyrics.getText().toString().equals("閉じる")) {
                mRelativeSongs.setVisibility(View.VISIBLE);
                mRelativeLyrics.setVisibility(View.INVISIBLE);
                mActivity.getViewSep1().setVisibility(View.INVISIBLE);
            }
            else {
                String strLyrics = mEditLyrics.getText().toString();
                if(mSelectedPlaylist < 0) mSelectedPlaylist = 0;
                else if(mSelectedPlaylist >= mLyrics.size()) mSelectedPlaylist = mLyrics.size() - 1;
                ArrayList<String> arTempLyrics = mLyrics.get(mSelectedPlaylist);
                arTempLyrics.set(mSelectedItem, strLyrics);
                mTextLyrics.setText(strLyrics);
                mBtnFinishLyrics.setText(R.string.close);
                mTextLyrics.setText(strLyrics);
                if(strLyrics.equals("")) {
                    mEditLyrics.setVisibility(View.INVISIBLE);
                    mTextNoLyrics.setVisibility(View.VISIBLE);
                    mTextLyrics.setVisibility(View.INVISIBLE);
                    mBtnEdit.setVisibility(View.INVISIBLE);
                    mImgEdit.setVisibility(View.VISIBLE);
                    mTextTapEdit.setVisibility(View.VISIBLE);
                }
                else {
                    mEditLyrics.setVisibility(View.INVISIBLE);
                    mTextNoLyrics.setVisibility(View.INVISIBLE);
                    mTextLyrics.setVisibility(View.VISIBLE);
                    mBtnEdit.setVisibility(View.VISIBLE);
                    mImgEdit.setVisibility(View.INVISIBLE);
                    mTextTapEdit.setVisibility(View.INVISIBLE);
                }
                mEditLyrics.clearFocus();
                InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm != null) imm.hideSoftInputFromWindow(mEditLyrics.getWindowToken(), 0);

                saveFiles(false, false, true, false, false);
            }
        }
        else if(v.getId() == R.id.btnEdit)
        {
            final Handler handler = new Handler();
            Runnable timer=new Runnable() {
                public void run()
                {
                    mTextLyrics.setVisibility(View.INVISIBLE);
                    mBtnFinishLyrics.setText(R.string.done);
                    mBtnEdit.setVisibility(View.INVISIBLE);
                    mEditLyrics.setText(mTextLyrics.getText());
                    mEditLyrics.setVisibility(View.VISIBLE);
                    mEditLyrics.requestFocus();
                    InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(imm != null) imm.showSoftInput(mEditLyrics, InputMethodManager.SHOW_IMPLICIT);
                    int nPos = mEditLyrics.getText().length();
                    mEditLyrics.setSelection(nPos);
                }
            };
            handler.postDelayed(timer, 80);
        }
        else if(v.getId() == R.id.textNoLyrics)
        {
            mTextNoLyrics.setVisibility(View.INVISIBLE);
            mImgEdit.setVisibility(View.INVISIBLE);
            mTextTapEdit.setVisibility(View.INVISIBLE);

            mTextLyrics.setVisibility(View.INVISIBLE);
            mBtnFinishLyrics.setText(R.string.done);
            mBtnEdit.setVisibility(View.INVISIBLE);
            mEditLyrics.setText("");
            mEditLyrics.setVisibility(View.VISIBLE);
            mEditLyrics.requestFocus();
            InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm != null) imm.showSoftInput(mEditLyrics, InputMethodManager.SHOW_IMPLICIT);
        }
        else if(v.getId() == R.id.btnCloseInMultipleSelection)
            finishMultipleSelection();
        else if(v.getId() == R.id.imgSelectAllInMultipleSelection)
            selectAllMultipleSelection();
        else if(v.getId() == R.id.btnDeleteInMultipleSelection)
            deleteMultipleSelection();
        else if(v.getId() == R.id.btnCopyInMultipleSelection)
            copyMultipleSelection();
        else if(v.getId() == R.id.btnMoveInMultipleSelection)
            moveMultipleSelection();
        else if(v.getId() == R.id.btnMoreInMultipleSelection)
            showMenuMultipleSelection();
    }

    @Override
    public boolean onLongClick(View v)
    {
        if(v.getId() == R.id.btnPlay) {
            final BottomMenu menu = new BottomMenu(mActivity);
            menu.setTitle(getString(R.string.playStop));
            if(MainActivity.sStream == 0 || BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PLAYING || mActivity.effectFragment.isReverse()) {
                menu.addMenu(getString(R.string.play), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_play_dark : R.drawable.ic_actionsheet_play, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    menu.dismiss();
                    if(mActivity.effectFragment.isReverse()) mActivity.effectFragment.onEffectItemClick(EffectFragment.EFFECTTYPE_REVERSE);
                    if(MainActivity.sStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PAUSED)
                        play();
                    else if(MainActivity.sStream == 0 || BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PLAYING) {
                        mForceNormal = true;
                        onPlayBtnClick();
                    }
                        }
                });
            }
            if(MainActivity.sStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PLAYING) {
                menu.addMenu(getString(R.string.pause), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_pause_dark : R.drawable.ic_actionsheet_pause, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menu.dismiss();
                        pause();
                    }
                });
            }
            if(MainActivity.sStream == 0 || BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PLAYING || !mActivity.effectFragment.isReverse()) {
                menu.addMenu(getString(R.string.reverse), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_reverse_dark : R.drawable.ic_actionsheet_reverse, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menu.dismiss();
                        if(!mActivity.effectFragment.isReverse()) mActivity.effectFragment.onEffectItemClick(EffectFragment.EFFECTTYPE_REVERSE);
                        if(MainActivity.sStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PAUSED)
                            play();
                        else if(MainActivity.sStream == 0 || BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PLAYING) {
                            mForceReverse = true;
                            onPlayBtnClick();
                        }
                    }
                });
            }
            if(MainActivity.sStream != 0) {
                menu.addDestructiveMenu(getString(R.string.stop), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_stop_dark : R.drawable.ic_actionsheet_stop, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menu.dismiss();
                        stop();
                            }
                    });
            }
            menu.setCancelMenu();
            menu.show();
        }
        return false;
    }

    public void onTouchMultipleSelectionItem(final int nItem)
    {
        ArrayList<SongItem> arSongs = mActivity.playlistFragment.getPlaylists().get(mActivity.playlistFragment.getSelectedPlaylist());
        SongItem item = arSongs.get(nItem);
        item.setSelected(!item.isSelected());
        int nSelected = 0;
        for(int i = 0; i < arSongs.size(); i++) {
            if(arSongs.get(i).isSelected()) nSelected++;
        }
        if(nSelected == 0 && !mAllowSelectNone) finishMultipleSelection();
        else if(nSelected == arSongs.size())
            mImgSelectAllInMultipleSelection.setImageResource(mActivity.isDarkMode() ? R.drawable.ic_button_check_on_dark : R.drawable.ic_button_check_on);
        else mImgSelectAllInMultipleSelection.setImageResource(mActivity.isDarkMode() ? R.drawable.ic_button_check_off_dark : R.drawable.ic_button_check_off);
    }

    public void startMultipleSelection(final int nItem)
    {
        mAllowSelectNone = (nItem == -1);
        mMultiSelecting = true;
        mSorting = false;
        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        for(int i = 0; i < arSongs.size(); i++)
            arSongs.get(i).setSelected(i == nItem);
        mSongsAdapter.notifyDataSetChanged();

        mTextPlaylistInMultipleSelection.setText(mPlaylistNames.get(mSelectedPlaylist));
        mBtnAddSong.clearAnimation();
        mBtnAddSong.setVisibility(View.GONE);

        int nTabHeight = mRecyclerTab.getHeight();
        int nDuration = 200;
        mRecyclerTab.animate().translationY(-nTabHeight).setDuration(nDuration).start();
        mBtnLeft.animate().translationY(-nTabHeight).setDuration(nDuration).start();
        mBtnAddPlaylist_small.animate().translationY(-nTabHeight).setDuration(nDuration).start();
        mDevider2.animate().translationY(-nTabHeight).setDuration(nDuration).start();
        int nHeight = (int)(66 *  mActivity.getDensity());
        mViewMultipleSelection.setTranslationY(-nHeight);
        mViewMultipleSelection.setVisibility(View.VISIBLE);
        mViewMultipleSelection.animate().setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mActivity.getViewSep1().setVisibility(View.VISIBLE);
            }
        }).translationY(0).setDuration(nDuration).start();

        startSort();
    }

    public void finishMultipleSelection()
    {
        mMultiSelecting = false;
        mSorting = false;
        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        for(int i = 0; i < arSongs.size(); i++)
            arSongs.get(i).setSelected(false);
        mSongsAdapter.notifyDataSetChanged();

        mActivity.getViewSep1().setVisibility(View.INVISIBLE);
        mBtnAddSong.setVisibility(View.VISIBLE);

        int nDuration = 200;
        mRecyclerTab.animate().translationY(0).setDuration(nDuration).start();
        mBtnLeft.animate().translationY(0).setDuration(nDuration).start();
        mBtnAddPlaylist_small.animate().translationY(0).setDuration(nDuration).start();
        mDevider2.animate().translationY(0).setDuration(nDuration).start();
        mViewMultipleSelection.animate().setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mViewMultipleSelection.setVisibility(View.GONE);
                mImgSelectAllInMultipleSelection.setImageResource(mActivity.isDarkMode() ? R.drawable.ic_button_check_off_dark : R.drawable.ic_button_check_off);
            }
        }).translationY(-mViewMultipleSelection.getHeight()).setDuration(nDuration).start();
    }

    private void selectAllMultipleSelection()
    {
        boolean bUnselectSongFounded = false;
        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        for(int i = 0; i < arSongs.size(); i++) {
            SongItem song = arSongs.get(i);
            if(!song.isSelected()) {
                bUnselectSongFounded = true;
                break;
            }
        }

        if(bUnselectSongFounded) {
            mImgSelectAllInMultipleSelection.setImageResource(mActivity.isDarkMode() ? R.drawable.ic_button_check_on_dark : R.drawable.ic_button_check_on);
            for(int i = 0; i < arSongs.size(); i++) {
                SongItem song = arSongs.get(i);
                song.setSelected(true);
            }
        }
        else {
            mImgSelectAllInMultipleSelection.setImageResource(mActivity.isDarkMode() ? R.drawable.ic_button_check_off_dark : R.drawable.ic_button_check_off);
            for(int i = 0; i < arSongs.size(); i++) {
                SongItem song = arSongs.get(i);
                song.setSelected(false);
            }
            if(!mAllowSelectNone) finishMultipleSelection();
        }
        mSongsAdapter.notifyDataSetChanged();
    }

    private void deleteMultipleSelection()
    {
        AlertDialog.Builder builder;
        if(mActivity.isDarkMode())
            builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
        else
            builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.delete);
        builder.setMessage(R.string.askDeleteSong);
        builder.setPositiveButton(getString(R.string.decideNot), null);
        builder.setNegativeButton(getString(R.string.doDelete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                boolean bDeletePlaying = false; // 再生中の曲を削除したか
                ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
                for(int i = 0; i < arSongs.size(); i++) {
                    if (arSongs.get(i).isSelected()) {
                        if(mSelectedPlaylist == mPlayingPlaylist && i == mPlaying)
                            bDeletePlaying = true;
                        removeSong(mSelectedPlaylist, i);
                        i--;
                    }
                }

                if(bDeletePlaying) {
                    arSongs = mPlaylists.get(mSelectedPlaylist);
                    if(mPlaying < arSongs.size())
                        playSong(mPlaying, true);
                    else if(mPlaying > 0 && mPlaying == arSongs.size())
                        playSong(mPlaying-1, true);
                    else
                        stop();
                }
                finishMultipleSelection();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface arg0)
            {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
                Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                positiveButton.setTextColor(getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeRed : R.color.lightModeRed));
            }
        });
        alertDialog.show();
    }

    private void copyMultipleSelection()
    {
        final BottomMenu menu = new BottomMenu(mActivity);
        menu.setTitle(getString(R.string.copy));
        for(int i = 0; i < mPlaylistNames.size(); i++)
        {
            final int nPlaylistTo = i;
            menu.addMenu(mPlaylistNames.get(i), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_folder_dark : R.drawable.ic_actionsheet_folder, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    copyMultipleSelection(nPlaylistTo);
                    menu.dismiss();
                }
            });
        }
        menu.setCancelMenu();
        menu.show();
    }

    private void copyMultipleSelection(int nPlaylistTo)
    {
        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        for(int i = 0; i < arSongs.size(); i++) {
            if (arSongs.get(i).isSelected())
                copySong(mSelectedPlaylist, i, nPlaylistTo);
        }
        finishMultipleSelection();
    }

    private void moveMultipleSelection()
    {
        final BottomMenu menu = new BottomMenu(mActivity);
        menu.setTitle(getString(R.string.moveToAnotherPlaylist));
        for(int i = 0; i < mPlaylistNames.size(); i++)
        {
            if(mSelectedPlaylist == i) continue;
            final int nPlaylistTo = i;
            menu.addMenu(mPlaylistNames.get(i), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_folder_dark : R.drawable.ic_actionsheet_folder, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    moveMultipleSelection(nPlaylistTo);
                    menu.dismiss();
                }
            });
        }
        menu.setCancelMenu();
        menu.show();
    }

    private void moveMultipleSelection(int nPlaylistTo)
    {
        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        for(int i = 0; i < arSongs.size(); i++) {
            if (arSongs.get(i).isSelected()) {
                moveSong(mSelectedPlaylist, i, nPlaylistTo);
                i--;
            }
        }
        finishMultipleSelection();
    }

    private void showMenuMultipleSelection()
    {
        boolean bLockFounded = false;
        boolean bUnlockFounded = false;
        boolean bChangeArtworkFounded = false;
        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        ArrayList<EffectSaver> arEffectSavers = mEffects.get(mSelectedPlaylist);
        for(int i = 0; i < arSongs.size(); i++) {
            SongItem song = arSongs.get(i);
            EffectSaver saver = arEffectSavers.get(i);
            if (song.isSelected()) {
                if(song.getPathArtwork() != null && !song.getPathArtwork().equals("")) bChangeArtworkFounded = true;
                if(saver.isSave()) bLockFounded = true;
                else bUnlockFounded = true;
            }
        }

        final BottomMenu menu = new BottomMenu(mActivity);
        menu.setTitle(getString(R.string.selectedSongs));
        menu.addMenu(getString(R.string.changeArtwork), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_image_dark : R.drawable.ic_actionsheet_image, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeArtworkMultipleSelection();
                menu.dismiss();
            }
        });
        if(bChangeArtworkFounded)
            menu.addDestructiveMenu(getString(R.string.resetArtwork), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_initialize_dark : R.drawable.ic_actionsheet_initialize, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resetArtworkMultipleSelection();
                    menu.dismiss();
                }
            });
        if(bUnlockFounded)
            menu.addMenu(getString(R.string.restoreEffect), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_lock_dark : R.drawable.ic_actionsheet_lock, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    restoreEffectMultipleSelection();
                    menu.dismiss();
                }
            });
        if(bLockFounded)
            menu.addMenu(getString(R.string.cancelRestoreEffect), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_unlock_dark : R.drawable.ic_actionsheet_unlock, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cancelRestoreEffectMultipleSelection();
                    menu.dismiss();
                }
            });
        menu.setCancelMenu();
        menu.show();
    }

    private void changeArtworkMultipleSelection()
    {
        if (Build.VERSION.SDK_INT < 19)
        {
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 4);
        }
        else
        {
            final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, 4);
        }
    }

    private void resetArtworkMultipleSelection()
    {
        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        for(int i = 0; i < arSongs.size(); i++) {
            SongItem song = arSongs.get(i);
            if(song.isSelected()) {
                boolean bFounded = false;
                // 同じアートワークを使っている曲が無いかチェック
                for(int j = 0; j < mPlaylists.size(); j++) {
                    ArrayList<SongItem> arTempSongs = mPlaylists.get(j);
                    for(int k = 0; k < arTempSongs.size(); k++) {
                        if(j == mSelectedPlaylist && k == i) continue;
                        SongItem songTemp = arTempSongs.get(k);
                        if(song.getPathArtwork() != null && songTemp.getPathArtwork() != null && song.getPathArtwork().equals(songTemp.getPathArtwork())) {
                            bFounded = true;
                            break;
                        }
                    }
                }

                // 同じアートワークを使っている曲が無ければ削除
                if(!bFounded) {
                    if(song.getPathArtwork() != null) {
                        File fileBefore = new File(song.getPathArtwork());
                        if (fileBefore.exists()) {
                            if (!fileBefore.delete()) System.out.println("ファイルの削除に失敗しました");
                            song.setPathArtwork(null);
                        }
                    }
                }

                song.setPathArtwork(null);
                if(mSelectedPlaylist == mPlayingPlaylist && i == mPlaying) {
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    Bitmap bitmap = null;
                    try {
                        mmr.setDataSource(mActivity, Uri.parse(song.getPath()));
                        byte[] data = mmr.getEmbeddedPicture();
                        if(data != null) bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                    finally {
                        mmr.release();
                    }
                    if(bitmap != null) mActivity.getBtnArtworkInPlayingBar().setImageBitmap(bitmap);
                    else mActivity.getBtnArtworkInPlayingBar().setImageResource(mActivity.isDarkMode() ? R.drawable.ic_playing_large_artwork_dark : R.drawable.ic_playing_large_artwork);
                    mActivity.getBtnArtworkInPlayingBar().setImageBitmap(bitmap);
                }
            }
        }
        saveFiles(true, false, false, false, false);
        finishMultipleSelection();
    }

    private void restoreEffectMultipleSelection()
    {
        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        for(int i = 0; i < arSongs.size(); i++) {
            SongItem song = arSongs.get(i);
            if (song.isSelected()) {
                mSelectedItem = i;
                setSavingEffect();
                mSongsAdapter.notifyItemChanged(i);
            }
        }
        finishMultipleSelection();
    }

    private void cancelRestoreEffectMultipleSelection()
    {
        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        for(int i = 0; i < arSongs.size(); i++) {
            SongItem song = arSongs.get(i);
            if (song.isSelected()) {
                mSelectedItem = i;
                cancelSavingEffect();
                mSongsAdapter.notifyItemChanged(i);
            }
        }
        finishMultipleSelection();
    }

    public void onRewindBtnClick()
    {
        if(MainActivity.sStream == 0) return;
        if(!mActivity.effectFragment.isReverse() && BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE)) > mActivity.getLoopAPos() + 1.0)
            BASS.BASS_ChannelSetPosition(MainActivity.sStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, mActivity.getLoopAPos()), BASS.BASS_POS_BYTE);
        else if(mActivity.effectFragment.isReverse() && BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE)) < mActivity.getLoopAPos() - 1.0)
            BASS.BASS_ChannelSetPosition(MainActivity.sStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, mActivity.getLoopBPos()), BASS.BASS_POS_BYTE);
        else
            playPrev();
    }

    public void onForwardBtnClick()
    {
        if(MainActivity.sStream == 0) return;
        playNext(true);
    }

    public void onPlayBtnClick()
    {
        if(BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PLAYING)
            pause();
        else
        {
            if(BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PAUSED)
            {
                double dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE));
                double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE));
                if(!mActivity.effectFragment.isReverse() && dPos >= dLength - 0.75) {
                    play();
                    mActivity.onEnded(false);
                }
                else play();
            }
            else
            {
                if(MainActivity.sStream == 0)
                {
                    if(mSelectedPlaylist < 0) mSelectedPlaylist = 0;
                    else if(mSelectedPlaylist >= mPlaylists.size()) mSelectedPlaylist = mPlaylists.size() - 1;
                    mPlayingPlaylist = mSelectedPlaylist;
                    ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
                    mPlays = new ArrayList<>();
                    for(int i = 0; i < arSongs.size(); i++)
                        mPlays.add(false);
                    playNext(true);
                }
                else
                    play();
            }
        }
    }

    public void startAddURL(String strURL)
    {
        StatFs sf = new StatFs(mActivity.getFilesDir().toString());
        long nFreeSpace;
        if(Build.VERSION.SDK_INT >= 18)
            nFreeSpace = sf.getAvailableBlocksLong() * sf.getBlockSizeLong();
        else
            nFreeSpace = (long)sf.getAvailableBlocks() * (long)sf.getBlockSize();
        if(nFreeSpace < 100) {
            AlertDialog.Builder builder;
            if(mActivity.isDarkMode())
                builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
            else
                builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.diskFullError);
            builder.setMessage(R.string.diskFullErrorDetail);
            builder.setPositiveButton("OK", null);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
            {
                @Override
                public void onShow(DialogInterface arg0)
                {
                    if(alertDialog.getWindow() != null) {
                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                        lp.dimAmount = 0.4f;
                        alertDialog.getWindow().setAttributes(lp);
                    }
                }
            });
            alertDialog.show();
            return;
        }
        AlertDialog.Builder builder;
        if(mActivity.isDarkMode())
            builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
        else
            builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.downloading);
        LinearLayout linearLayout = new LinearLayout(mActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        mProgress = new ProgressBar(mActivity, null, android.R.attr.progressBarStyleHorizontal);
        mProgress.setMax(100);
        mProgress.setProgress(0);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        param.topMargin = (int)(24 *  mActivity.getDensity());
        param.leftMargin = (int)(16 *  mActivity.getDensity());
        param.rightMargin = (int)(16 *  mActivity.getDensity());
        linearLayout.addView(mProgress, param);
        builder.setView(linearLayout);

        String strPathTo;
        int i = 0;
        File fileForCheck;
        while (true) {
            strPathTo = mActivity.getFilesDir() + "/recorded" +  String.format(Locale.getDefault(), "%d", i) + ".mp3";
            fileForCheck = new File(strPathTo);
            if (!fileForCheck.exists()) break;
            i++;
        }
        mFinish = false;
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mFinish = true;
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface arg0)
            {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
            }
        });
        alertDialog.show();

        if(mDownloadTask != null && mDownloadTask.getStatus() == AsyncTask.Status.RUNNING)
            mDownloadTask.cancel(true);
        try
        {
            mDownloadTask = new DownloadTask(this, new URL(strURL), strPathTo, alertDialog);
            mDownloadTask.execute(0);
        }
        catch (MalformedURLException e)
        {
            if(alertDialog.isShowing()) alertDialog.dismiss();
        }
    }

    public void finishAddURL(String strPathTo, AlertDialog alert, int nError)
    {
        if(alert.isShowing()) alert.dismiss();

        final File file = new File(strPathTo);
        if(nError == 1)
        {
            if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            AlertDialog.Builder builder;
            if(mActivity.isDarkMode())
                builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
            else
                builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.downloadError);
            builder.setMessage(R.string.downloadErrorDetail);
            builder.setPositiveButton("OK", null);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
            {
                @Override
                public void onShow(DialogInterface arg0)
                {
                    if(alertDialog.getWindow() != null) {
                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                        lp.dimAmount = 0.4f;
                        alertDialog.getWindow().setAttributes(lp);
                    }
                }
            });
            alertDialog.show();
            return;
        }

        int hTempStream = BASS.BASS_StreamCreateFile(strPathTo, 0, 0, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
        if(hTempStream == 0)
        {
            if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            AlertDialog.Builder builder = new    AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.playableError);
            builder.setMessage(R.string.playableErrorDetail);
            builder.setPositiveButton("OK", null);
            builder.show();
            return;
        }

        AlertDialog.Builder builder;
        if(mActivity.isDarkMode())
            builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
        else
            builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.addURL);
        LinearLayout linearLayout = new LinearLayout(mActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final ClearableEditText editTitle = new ClearableEditText(mActivity, mActivity.isDarkMode());
        editTitle.setHint(R.string.title);
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        editTitle.setText(String.format(Locale.getDefault(), "タイトル(%s)", df.format(date)));
        final ClearableEditText editArtist = new ClearableEditText(mActivity, mActivity.isDarkMode());
        editArtist.setHint(R.string.artist);
        editArtist.setText("");
        linearLayout.addView(editTitle);
        linearLayout.addView(editArtist);
        builder.setView(linearLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
                SongItem item = new SongItem(String.format(Locale.getDefault(), "%d", arSongs.size()+1), editTitle.getText().toString(), editArtist.getText().toString(), file.getPath());
                arSongs.add(item);
                ArrayList<EffectSaver> arEffectSavers = mEffects.get(mSelectedPlaylist);
                EffectSaver saver = new EffectSaver();
                arEffectSavers.add(saver);
                ArrayList<String> arTempLyrics = mLyrics.get(mSelectedPlaylist);
                arTempLyrics.add(null);
                if(mSelectedPlaylist == mPlayingPlaylist) mPlays.add(false);
                mSongsAdapter.notifyItemInserted(arSongs.size() - 1);

                saveFiles(true, true, true, true, false);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface arg0)
            {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
                editTitle.requestFocus();
                editTitle.setSelection(editTitle.getText().toString().length());
                InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (null != imm) imm.showSoftInput(editTitle, 0);
            }
        });
        alertDialog.show();
    }

    public void startRecord()
    {
        if(MainActivity.sRecord != 0) {
            stopRecord();
            return;
        }
        if(Build.VERSION.SDK_INT >= 23) {
            if (mActivity.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                mActivity.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                return;
            }
        }
        StatFs sf = new StatFs(mActivity.getFilesDir().toString());
        long nFreeSpace;
        if(Build.VERSION.SDK_INT >= 18)
            nFreeSpace = sf.getAvailableBlocksLong() * sf.getBlockSizeLong();
        else
            nFreeSpace = (long)sf.getAvailableBlocks() * (long)sf.getBlockSize();
        if(nFreeSpace < 100) {
            AlertDialog.Builder builder;
            if(mActivity.isDarkMode())
                builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
            else
                builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.diskFullError);
            builder.setMessage(R.string.diskFullErrorDetail);
            builder.setPositiveButton("OK", null);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
            {
                @Override
                public void onShow(DialogInterface arg0)
                {
                    if(alertDialog.getWindow() != null) {
                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                        lp.dimAmount = 0.4f;
                        alertDialog.getWindow().setAttributes(lp);
                    }
                }
            });
            alertDialog.show();
            return;
        }

        mActivity.getBtnStopRecording().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecord();
            }
        });

        final RelativeLayout.LayoutParams paramContainer = (RelativeLayout.LayoutParams)mActivity.getViewPager().getLayoutParams();
        final RelativeLayout.LayoutParams paramRecording = (RelativeLayout.LayoutParams)mActivity.getRelativeRecording().getLayoutParams();
        paramContainer.addRule(RelativeLayout.ABOVE, R.id.relativeRecording);
        paramContainer.bottomMargin = 0;
        if(mActivity.getSeekCurPos().getVisibility() == View.VISIBLE)
            paramRecording.addRule(RelativeLayout.ABOVE, R.id.adView);
        else paramRecording.addRule(RelativeLayout.ABOVE, R.id.relativePlayingWithShadow);
        if(MainActivity.sStream == 0) paramRecording.bottomMargin = 0;
        else {
            if(mActivity.getSeekCurPos().getVisibility() == View.VISIBLE)
                paramRecording.bottomMargin = (int) (60 * mActivity.getDensity());
            else paramRecording.bottomMargin = (int) (-22 * mActivity.getDensity());
        }

        mBtnAddPlaylist.clearAnimation();
        mBtnAddPlaylist.setVisibility(View.INVISIBLE);
        mBtnAddSong.clearAnimation();
        mBtnAddSong.setVisibility(View.INVISIBLE);
        mBtnEdit.clearAnimation();
        mBtnEdit.setVisibility(View.INVISIBLE);
        mActivity.getRelativeRecording().setTranslationY((int)(64 * mActivity.getDensity()));
        mActivity.getRelativeRecording().setVisibility(View.VISIBLE);
        mActivity.getRelativeRecording().animate()
                .translationY(0)
                .setDuration(200);

        BASS.BASS_RecordInit(-1);
        mRecbuf = ByteBuffer.allocateDirect(200000);
        mRecbuf.order(ByteOrder.LITTLE_ENDIAN);
        mRecbuf.put(new byte[]{'R','I','F','F',0,0,0,0,'W','A','V','E','f','m','t',' ',16,0,0,0});
        mRecbuf.putShort((short)1);
        mRecbuf.putShort((short)2);
        mRecbuf.putInt(44100);
        mRecbuf.putInt(44100 * 2);
        mRecbuf.putShort((short)2);
        mRecbuf.putShort((short)16);
        mRecbuf.put(new byte[]{'d','a','t','a',0,0,0,0});
        BASS.RECORDPROC RecordingCallback = new BASS.RECORDPROC() {
            public boolean RECORDPROC(int handle, ByteBuffer buffer, int length, Object user) {
                try {
                    mRecbuf.put(buffer);
                } catch (BufferOverflowException e) {
                    ByteBuffer temp;
                    try {
                        temp = ByteBuffer.allocateDirect(mRecbuf.position() + length + 200000);
                    } catch (Error e2) {
                        mActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                stopRecord();
                            }
                        });
                        return false;
                    }
                    temp.order(ByteOrder.LITTLE_ENDIAN);
                    mRecbuf.limit(mRecbuf.position());
                    mRecbuf.position(0);
                    temp.put(mRecbuf);
                    mRecbuf = temp;
                    mRecbuf.put(buffer);
                }
                return true;
            }
        };
        MainActivity.sRecord = BASS.BASS_RecordStart(44100, 2, 0, RecordingCallback, 0);

        mActivity.getBtnRecord().setColorFilter(new PorterDuffColorFilter(mActivity.isDarkMode() ? getResources().getColor(R.color.darkModeBlue) : getResources().getColor(R.color.lightModeBlue), PorterDuff.Mode.SRC_IN));

        final Handler handler = new Handler();
        Runnable timer=new Runnable() {
            public void run()
            {
                if (MainActivity.sRecord == 0) return;
                double dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sRecord, BASS.BASS_ChannelGetPosition(MainActivity.sRecord, BASS.BASS_POS_BYTE));
                int nHour = (int)(dPos / (60 * 60) % 60);
                int nMinute = (int)(dPos / 60 % 60);
                int nSecond = (int)(dPos % 60);
                int nMillisecond = (int)(dPos * 100 % 100);
                mActivity.getTextRecordingTime().setText(String.format(Locale.getDefault(), "%02d:%02d:%02d.%02d", nHour, nMinute, nSecond, nMillisecond));
                handler.postDelayed(this, 50);
            }
        };
        handler.postDelayed(timer, 50);
    }

    private void stopRecord()
    {
        final RelativeLayout.LayoutParams paramContainer = (RelativeLayout.LayoutParams)mActivity.getViewPager().getLayoutParams();
        final RelativeLayout.LayoutParams paramRecording = (RelativeLayout.LayoutParams)mActivity.getRelativeRecording().getLayoutParams();
        paramRecording.bottomMargin = 0;
        if(MainActivity.sStream == 0) paramContainer.bottomMargin = 0;
        else paramContainer.bottomMargin = (int) (-22 * mActivity.getDensity());

        mActivity.getRelativeRecording().setVisibility(View.GONE);
        mBtnAddPlaylist.setVisibility(View.VISIBLE);
        mBtnAddSong.setVisibility(View.VISIBLE);
        mBtnEdit.setVisibility(View.VISIBLE);

        BASS.BASS_ChannelStop(MainActivity.sRecord);
        MainActivity.sRecord = 0;

        mActivity.getBtnRecord().clearColorFilter();

        mRecbuf.limit(mRecbuf.position());
        mRecbuf.putInt(4, mRecbuf.position()-8);
        mRecbuf.putInt(40, mRecbuf.position()-44);
        int i = 0;
        String strPath;
        File fileForCheck;
        while(true) {
            strPath = mActivity.getFilesDir() + "/recorded" + String.format(Locale.getDefault(), "%d", i) + ".wav";
            fileForCheck = new File(strPath);
            if(!fileForCheck.exists()) break;
            i++;
        }
        final File file = new File(strPath);
        try {
            FileChannel fc = new FileOutputStream(file).getChannel();
            mRecbuf.position(0);
            fc.write(mRecbuf);
            fc.close();
        } catch (IOException e) {
            return;
        }

        AlertDialog.Builder builder;
        if(mActivity.isDarkMode())
            builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
        else
            builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.newRecord);
        LinearLayout linearLayout = new LinearLayout(mActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final ClearableEditText editTitle = new ClearableEditText(mActivity, mActivity.isDarkMode());
        editTitle.setHint(R.string.title);
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        editTitle.setText(String.format(Locale.getDefault(), "%s(%s)", getString(R.string.newRecord), df.format((date))));
        final ClearableEditText editArtist = new ClearableEditText(mActivity, mActivity.isDarkMode());
        editArtist.setHint(R.string.artist);
        editArtist.setText("");
        linearLayout.addView(editTitle);
        linearLayout.addView(editArtist);
        builder.setView(linearLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
                SongItem item = new SongItem(String.format(Locale.getDefault(), "%d", arSongs.size()+1), editTitle.getText().toString(), editArtist.getText().toString(), file.getPath());
                arSongs.add(item);
                ArrayList<EffectSaver> arEffectSavers = mEffects.get(mSelectedPlaylist);
                EffectSaver saver = new EffectSaver();
                arEffectSavers.add(saver);
                ArrayList<String> arTempLyrics = mLyrics.get(mSelectedPlaylist);
                arTempLyrics.add(null);
                if(mSelectedPlaylist == mPlayingPlaylist) mPlays.add(false);
                mSongsAdapter.notifyItemInserted(arSongs.size() - 1);

                saveFiles(true, true, true, true, false);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface arg0)
            {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
                editTitle.requestFocus();
                editTitle.setSelection(editTitle.getText().toString().length());
                InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (null != imm) imm.showSoftInput(editTitle, 0);
            }
        });
        alertDialog.show();
    }

    public void addPlaylist(String strName)
    {
        mPlaylistNames.add(strName);
        ArrayList<SongItem> arSongs = new ArrayList<>();
        mPlaylists.add(arSongs);
        ArrayList<EffectSaver> arEffectSavers = new ArrayList<>();
        mEffects.add(arEffectSavers);
        ArrayList<String> arTempLyrics = new ArrayList<>();
        mLyrics.add(arTempLyrics);
        if(mActivity != null)
            saveFiles(true, true, true, true, false);
        selectPlaylist(mPlaylists.size() - 1);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        mActivity = (MainActivity)getActivity();
        if(mActivity == null) return;

        mBtnAddPlaylist = mActivity.findViewById(R.id.btnAddPlaylist);
        mRecyclerPlaylists = mActivity.findViewById(R.id.recyclerPlaylists);
        mRecyclerTab = mActivity.findViewById(R.id.recyclerTab);
        mRecyclerSongs = mActivity.findViewById(R.id.recyclerSongs);
        mBtnSortPlaylist = mActivity.findViewById(R.id.btnSortPlaylist);
        mBtnArtworkInPlayingBar = mActivity.findViewById(R.id.btnArtworkInPlayingBar);
        mTextTitleInPlayingBar = mActivity.findViewById(R.id.textTitleInPlayingBar);
        mTextArtistInPlayingBar = mActivity.findViewById(R.id.textArtistInPlayingBar);
        mRelativeSongs = mActivity.findViewById(R.id.relativeSongs);
        mRelativePlaylists = mActivity.findViewById(R.id.relativePlaylists);
        mTextFinishSort = mActivity.findViewById(R.id.textFinishSort);
        mBtnAddSong = mActivity.findViewById(R.id.btnAddSong);
        mTextLyricsTitle = mActivity.findViewById(R.id.textLyricsTitle);
        mTextNoLyrics = mActivity.findViewById(R.id.textNoLyrics);
        mTextLyrics = mActivity.findViewById(R.id.textLyrics);
        mBtnEdit = mActivity.findViewById(R.id.btnEdit);
        mImgEdit = mActivity.findViewById(R.id.imgEdit);
        mTextTapEdit = mActivity.findViewById(R.id.textTapEdit);
        mRelativeLyrics = mActivity.findViewById(R.id.relativeLyrics);
        mRelativeLyricsTitle = mActivity.findViewById(R.id.relativeLyricsTitle);
        mBtnFinishLyrics = mActivity.findViewById(R.id.btnFinishLyrics);
        mEditLyrics = mActivity.findViewById(R.id.editLyrics);
        mImgSelectAllInMultipleSelection = mActivity.findViewById(R.id.imgSelectAllInMultipleSelection);
        mBtnLeft = mActivity.findViewById(R.id.btnLeft);
        mBtnAddPlaylist_small = mActivity.findViewById(R.id.btnAddPlaylist_small);
        mDevider1 = mActivity.findViewById(R.id.devider1);
        mDevider2 = mActivity.findViewById(R.id.devider2);
        mViewMultipleSelection = mActivity.findViewById(R.id.viewMultipleSelection);
        mViewSepLyrics = mActivity.findViewById(R.id.viewSepLyrics);
        mTextPlaylistInMultipleSelection = mActivity.findViewById(R.id.textPlaylistInMultipleSelection);
        mTextPlaylist = mActivity.findViewById(R.id.textPlaylist);
        AnimationButton btnRewind = mActivity.findViewById(R.id.btnRewind);
        AnimationButton btnPlay = mActivity.findViewById(R.id.btnPlay);
        AnimationButton btnForward = mActivity.findViewById(R.id.btnForward);
        AnimationButton btnRecord = mActivity.findViewById(R.id.btnRecord);
        AnimationButton btnCloseInMultipleSelection = mActivity.findViewById(R.id.btnCloseInMultipleSelection);
        AnimationButton btnDeleteInMultipleSelection = mActivity.findViewById(R.id.btnDeleteInMultipleSelection);
        AnimationButton btnCopyInMultipleSelection = mActivity.findViewById(R.id.btnCopyInMultipleSelection);
        AnimationButton btnMoveInMultipleSelection = mActivity.findViewById(R.id.btnMoveInMultipleSelection);
        AnimationButton btnMoreInMultipleSelection = mActivity.findViewById(R.id.btnMoreInMultipleSelection);

        mTabAdapter = new PlaylistTabAdapter(mActivity, mPlaylistNames);
        mPlaylistsAdapter = new PlaylistsAdapter(mActivity, mPlaylistNames);
        mSongsAdapter = new SongsAdapter(mActivity);

        mRecyclerPlaylists.setHasFixedSize(false);
        LinearLayoutManager playlistsManager = new LinearLayoutManager(mActivity);
        mRecyclerPlaylists.setLayoutManager(playlistsManager);
        mRecyclerPlaylists.setAdapter(mPlaylistsAdapter);
        ((DefaultItemAnimator) mRecyclerPlaylists.getItemAnimator()).setSupportsChangeAnimations(false);
        mRecyclerPlaylists.setOnClickListener(this);

        mRecyclerTab.setHasFixedSize(false);
        CenterLayoutManager tabManager = new CenterLayoutManager(mActivity);
        tabManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerTab.setLayoutManager(tabManager);
        mRecyclerTab.setAdapter(mTabAdapter);
        ((DefaultItemAnimator) mRecyclerTab.getItemAnimator()).setSupportsChangeAnimations(false);

        mRecyclerSongs.setHasFixedSize(false);
        LinearLayoutManager songsManager = new LinearLayoutManager(mActivity);
        mRecyclerSongs.setLayoutManager(songsManager);
        mRecyclerSongs.setAdapter(mSongsAdapter);
        ((DefaultItemAnimator) mRecyclerSongs.getItemAnimator()).setSupportsChangeAnimations(false);
        mRecyclerSongs.setOnClickListener(this);
        btnRewind.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnPlay.setOnLongClickListener(this);
        btnForward.setOnClickListener(this);
        btnRecord.setOnClickListener(this);
        mBtnSortPlaylist.setOnClickListener(this);
        mBtnAddPlaylist.setOnClickListener(this);
        mBtnLeft.setOnClickListener(this);
        mBtnAddPlaylist_small.setOnClickListener(this);
        mBtnAddSong.setOnClickListener(this);
        mTextFinishSort.setOnClickListener(this);
        mBtnFinishLyrics.setOnClickListener(this);
        mBtnEdit.setOnClickListener(this);
        mTextNoLyrics.setOnClickListener(this);
        mImgSelectAllInMultipleSelection.setOnClickListener(this);
        btnCloseInMultipleSelection.setOnClickListener(this);
        btnDeleteInMultipleSelection.setOnClickListener(this);
        btnCopyInMultipleSelection.setOnClickListener(this);
        btnMoveInMultipleSelection.setOnClickListener(this);
        btnMoreInMultipleSelection.setOnClickListener(this);

        SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        int nSelectedPlaylist = preferences.getInt("SelectedPlaylist", 0);
        selectPlaylist(nSelectedPlaylist);

        if(mActivity.isDarkMode()) setDarkMode(false);
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
                    addSong(mActivity, data.getData());
                }
                else
                {
                    if(data.getClipData() == null)
                    {
                        addSong(mActivity, data.getData());
                        Uri uri = data.getData();
                        if(uri != null)
                            mActivity.getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    }
                    else
                    {
                        for(int i = 0; i < data.getClipData().getItemCount(); i++)
                        {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            addSong(mActivity, uri);
                            mActivity.getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        }
                    }
                }
            }
        }
        else if(requestCode == 2)
        {
            if(resultCode == RESULT_OK)
            {
                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if(Build.VERSION.SDK_INT < 19)
                    addVideo(mActivity, data.getData());
                else
                {
                    if(data.getClipData() == null)
                    {
                        addVideo(mActivity, data.getData());
                        Uri uri = data.getData();
                        if(uri != null)
                            mActivity.getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    }
                    else
                    {
                        for(int i = 0; i < data.getClipData().getItemCount(); i++)
                        {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            addVideo(mActivity, uri);
                            mActivity.getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        }
                    }
                }
            }
        }
        else if(requestCode == 3)
        {
            if(resultCode == RESULT_OK)
            {
                final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if(Build.VERSION.SDK_INT < 19)
                    setArtwork(data.getData());
                else
                {
                    if(data.getClipData() == null)
                    {
                        setArtwork(data.getData());
                        Uri uri = data.getData();
                        if(uri != null)
                            mActivity.getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    }
                    else
                    {
                        for(int i = 0; i < data.getClipData().getItemCount(); i++)
                        {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            setArtwork(uri);
                            mActivity.getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        }
                    }
                }
            }
        }
        else if(requestCode == 4)
        {
            if(resultCode == RESULT_OK)
            {
                final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if(Build.VERSION.SDK_INT < 19)
                    setArtworkMultipleSelection(data.getData());
                else
                {
                    if(data.getClipData() == null)
                    {
                        setArtworkMultipleSelection(data.getData());
                        Uri uri = data.getData();
                        if(uri != null)
                            mActivity.getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    }
                    else
                    {
                        for(int i = 0; i < data.getClipData().getItemCount(); i++)
                        {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            setArtworkMultipleSelection(uri);
                            mActivity.getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        }
                    }
                }
            }
        }

        saveFiles(true, true, true, true, false);
    }

    private void setArtwork(Uri uri)
    {
        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        SongItem song = arSongs.get(mSelectedItem);
        String strPathArtwork = song.getPathArtwork();
        if(strPathArtwork != null) {
            boolean bFounded = false;
            // 同じアートワークを使っている曲が無いかチェック
            for(int j = 0; j < mPlaylists.size(); j++) {
                ArrayList<SongItem> arTempSongs = mPlaylists.get(j);
                for(int k = 0; k < arTempSongs.size(); k++) {
                    if(j == mSelectedPlaylist && k == mSelectedItem) continue;
                    SongItem songTemp = arTempSongs.get(k);
                    if(song.getPathArtwork() != null && songTemp.getPathArtwork() != null && song.getPathArtwork().equals(songTemp.getPathArtwork())) {
                        bFounded = true;
                        break;
                    }
                }
            }

            // 同じアートワークを使っている曲が無ければ削除
            if(!bFounded) {
                File fileBefore = new File(strPathArtwork);
                if (fileBefore.exists()) {
                    if (!fileBefore.delete()) System.out.println("ファイルの削除に失敗しました");
                    song.setPathArtwork(null);
                }
            }
        }

        Bitmap bitmap;
        int nArtworkSize = getResources().getDisplayMetrics().widthPixels / 2;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(mActivity.getContentResolver(), uri);
        }catch (IOException e) {
            e.printStackTrace();
            return;
        }
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, nArtworkSize, nArtworkSize, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        mBtnArtworkInPlayingBar.setImageBitmap(bitmap);
        String strPathTo;
        int i = 0;
        File file;
        while (true) {
            strPathTo = mActivity.getFilesDir() + "/artwork" +  String.format(Locale.getDefault(), "%d", i) + ".png";
            file = new File(strPathTo);
            if (!file.exists()) break;
            i++;
        }

        try {
            FileOutputStream outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.close();
            song.setPathArtwork(strPathTo);
            saveFiles(true, false, false, false, false);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void resetArtwork()
    {
        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        SongItem song = arSongs.get(mSelectedItem);
        String strPathArtwork = song.getPathArtwork();
        if(strPathArtwork != null) {
            boolean bFounded = false;
            // 同じアートワークを使っている曲が無いかチェック
            for(int j = 0; j < mPlaylists.size(); j++) {
                ArrayList<SongItem> arTempSongs = mPlaylists.get(j);
                for(int k = 0; k < arTempSongs.size(); k++) {
                    if(j == mSelectedPlaylist && k == mSelectedItem) continue;
                    SongItem songTemp = arTempSongs.get(k);
                    if(song.getPathArtwork() != null && songTemp.getPathArtwork() != null && song.getPathArtwork().equals(songTemp.getPathArtwork())) {
                        bFounded = true;
                        break;
                    }
                }
            }

            // 同じアートワークを使っている曲が無ければ削除
            if(!bFounded) {
                File fileBefore = new File(strPathArtwork);
                if (fileBefore.exists()) {
                    if (!fileBefore.delete()) System.out.println("ファイルの削除に失敗しました");
                    song.setPathArtwork(null);
                }
            }
        }
        song.setPathArtwork(null);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        Bitmap bitmap = null;
        try {
            mmr.setDataSource(mActivity, Uri.parse(song.getPath()));
            byte[] data = mmr.getEmbeddedPicture();
            if(data != null) bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            mmr.release();
        }
        if(bitmap != null) mBtnArtworkInPlayingBar.setImageBitmap(bitmap);
        else mBtnArtworkInPlayingBar.setImageResource(mActivity.isDarkMode() ? R.drawable.ic_playing_large_artwork_dark : R.drawable.ic_playing_large_artwork);
        saveFiles(true, false, false, false, false);
    }

    private void setArtworkMultipleSelection(Uri uri)
    {
        Bitmap bitmap;
        int nArtworkSize = getResources().getDisplayMetrics().widthPixels / 2;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(mActivity.getContentResolver(), uri);
        }catch (IOException e) {
            e.printStackTrace();
            return;
        }
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, nArtworkSize, nArtworkSize, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        String strPathTo;
        int i = 0;
        File file;
        while (true) {
            strPathTo = mActivity.getFilesDir() + "/artwork" +  String.format(Locale.getDefault(), "%d", i) + ".png";
            file = new File(strPathTo);
            if (!file.exists()) break;
            i++;
        }

        try {
            FileOutputStream outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        for(i = 0; i < arSongs.size(); i++) {
            SongItem song = arSongs.get(i);
            if(song.isSelected()) {
                boolean bFounded = false;
                // 同じアートワークを使っている曲が無いかチェック
                for(int j = 0; j < mPlaylists.size(); j++) {
                    ArrayList<SongItem> arTempSongs = mPlaylists.get(j);
                    for(int k = 0; k < arTempSongs.size(); k++) {
                        if(j == mSelectedPlaylist && k == i) continue;
                        SongItem songTemp = arTempSongs.get(k);
                        if(song.getPathArtwork() != null && songTemp.getPathArtwork() != null && song.getPathArtwork().equals(songTemp.getPathArtwork())) {
                            bFounded = true;
                            break;
                        }
                    }
                }

                // 同じアートワークを使っている曲が無ければ削除
                if(!bFounded) {
                    if(song.getPathArtwork() != null) {
                        File fileBefore = new File(song.getPathArtwork());
                        if (fileBefore.exists()) {
                            if (!fileBefore.delete()) System.out.println("ファイルの削除に失敗しました");
                            song.setPathArtwork(null);
                        }
                    }
                }

                song.setPathArtwork(strPathTo);
                if(mSelectedPlaylist == mPlayingPlaylist && i == mPlaying)
                    mBtnArtworkInPlayingBar.setImageBitmap(bitmap);
            }
        }
        saveFiles(true, false, false, false, false);
        finishMultipleSelection();
    }

    public void showSongMenu(final int nItem)
    {
        mSelectedItem = nItem;
        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        final SongItem songItem = arSongs.get(nItem);
        String strTitle = songItem.getTitle();

        final BottomMenu menu = new BottomMenu(mActivity);
        menu.setTitle(strTitle);
        menu.addMenu(getString(R.string.saveExport), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_save_dark : R.drawable.ic_actionsheet_save, new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                menu.dismiss();

                final BottomMenu menu = new BottomMenu(mActivity);
                menu.setTitle(getString(R.string.saveExport));
                menu.addMenu(getString(R.string.saveToApp), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_save_dark : R.drawable.ic_actionsheet_save, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menu.dismiss();
                        saveSongToLocal();
                    }
                });
                if(Build.VERSION.SDK_INT >= 18) {
                    menu.addMenu(getString(R.string.saveAsVideo), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_film_dark : R.drawable.ic_actionsheet_film, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            menu.dismiss();
                            saveSongToGallery();
                        }
                    });
                }
                menu.addMenu(getString(R.string.export), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_share_dark : R.drawable.ic_actionsheet_share, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menu.dismiss();
                        export();
                    }
                });
                menu.setCancelMenu();
                menu.show();
            }
        });
        ArrayList<EffectSaver> arEffectSavers = mEffects.get(mSelectedPlaylist);
        EffectSaver saver = arEffectSavers.get(nItem);
        if(saver.isSave())
        {
            menu.addMenu(getString(R.string.cancelRestoreEffect), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_unlock_dark : R.drawable.ic_actionsheet_unlock, new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    cancelSavingEffect();
                    mSongsAdapter.notifyItemChanged(mSelectedItem);
                    menu.dismiss();
                }
            });
        }
        else
        {
            menu.addMenu(getString(R.string.restoreEffect), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_lock_dark : R.drawable.ic_actionsheet_lock, new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    setSavingEffect();
                    mSongsAdapter.notifyItemChanged(mSelectedItem);
                    menu.dismiss();
                }
            });
        }
        menu.addSeparator();
        menu.addMenu(getString(R.string.changeArtwork), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_image_dark : R.drawable.ic_actionsheet_image, new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                menu.dismiss();
                if (Build.VERSION.SDK_INT < 19)
                {
                    final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, 3);
                }
                else
                {
                    final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, 3);
                }
            }
        });
        menu.addMenu(getString(R.string.changeTitleAndArtist), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_edit_dark : R.drawable.ic_actionsheet_edit, new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                menu.dismiss();
                changeTitleAndArtist(nItem);
            }
        });
        menu.addMenu(getString(R.string.showLyrics), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_file_text_dark : R.drawable.ic_actionsheet_file_text, new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                menu.dismiss();
                showLyrics();
            }
        });
        menu.addSeparator();
        menu.addMenu(getString(R.string.copy), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_copy_dark : R.drawable.ic_actionsheet_copy, new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                menu.dismiss();
                final BottomMenu menu = new BottomMenu(mActivity);
                menu.setTitle(getString(R.string.copy));
                for(int i = 0; i < mPlaylistNames.size(); i++)
                {
                    final int nPlaylistTo = i;
                    menu.addMenu(mPlaylistNames.get(i), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_folder_dark : R.drawable.ic_actionsheet_folder, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            copySong(mSelectedPlaylist, nItem, nPlaylistTo);
                            menu.dismiss();
                        }
                    });
                }
                menu.setCancelMenu();
                menu.show();
            }
        });
        menu.addMenu(getString(R.string.moveToAnotherPlaylist), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_folder_move_dark : R.drawable.ic_actionsheet_folder_move, new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                menu.dismiss();

                final BottomMenu menu = new BottomMenu(mActivity);
                menu.setTitle(getString(R.string.moveToAnotherPlaylist));
                for(int i = 0; i < mPlaylistNames.size(); i++)
                {
                    if(mSelectedPlaylist == i) continue;
                    final int nPlaylistTo = i;
                    menu.addMenu(mPlaylistNames.get(i), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_folder_dark : R.drawable.ic_actionsheet_folder, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            moveSong(mSelectedPlaylist, nItem, nPlaylistTo);
                            menu.dismiss();
                        }
                    });
                }
                menu.setCancelMenu();
                menu.show();
            }
        });
        menu.addDestructiveMenu(getString(R.string.delete), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_delete_dark : R.drawable.ic_actionsheet_delete, new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                menu.dismiss();
                AlertDialog.Builder builder;
                if(mActivity.isDarkMode())
                    builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
                else
                    builder = new AlertDialog.Builder(mActivity);
                String strTitle = songItem.getTitle();
                builder.setTitle(strTitle);
                builder.setMessage(R.string.askDeleteSong);
                builder.setPositiveButton(getString(R.string.decideNot), null);
                builder.setNegativeButton(getString(R.string.doDelete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        boolean bDeletePlaying = false; // 再生中の曲を削除したか
                        if(mSelectedPlaylist == mPlayingPlaylist && nItem == mPlaying)
                            bDeletePlaying = true;
                        removeSong(mSelectedPlaylist, nItem);
                        if(bDeletePlaying) {
                            ArrayList<SongItem> arSongs = mPlaylists.get(mPlayingPlaylist);
                            if(mPlaying < arSongs.size())
                                playSong(mPlaying, true);
                            else if(mPlaying > 0 && mPlaying == arSongs.size())
                                playSong(mPlaying-1, true);
                            else
                                stop();
                        }
                    }
                });
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                {
                    @Override
                    public void onShow(DialogInterface arg0)
                    {
                        if(alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        positiveButton.setTextColor(getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeRed : R.color.lightModeRed));
                    }
                });
                alertDialog.show();
            }
        });
        menu.setCancelMenu();
        menu.show();
    }

    private void copySong(int nPlaylistFrom, int nItem, int nPlaylistTo)
    {
        ArrayList<SongItem> arSongsFrom = mPlaylists.get(nPlaylistFrom);
        ArrayList<SongItem> arSongsTo = mPlaylists.get(nPlaylistTo);
        SongItem itemFrom = arSongsFrom.get(nItem);
        File file = new File(itemFrom.getPath());
        String strPath = itemFrom.getPath();
        if(file.getParent().equals(mActivity.getFilesDir().toString()))
            strPath = mActivity.copyFile(Uri.parse(itemFrom.getPath())).toString();
        SongItem itemTo = new SongItem(String.format(Locale.getDefault(), "%d", arSongsTo.size()+1), itemFrom.getTitle(), itemFrom.getArtist(), strPath);
        arSongsTo.add(itemTo);

        ArrayList<EffectSaver> arEffectSaversFrom = mEffects.get(mSelectedPlaylist);
        ArrayList<EffectSaver> arEffectSaversTo = mEffects.get(nPlaylistTo);
        EffectSaver saverFrom = arEffectSaversFrom.get(nItem);
        if(saverFrom.isSave()) {
            EffectSaver saverTo = new EffectSaver(saverFrom);
            arEffectSaversTo.add(saverTo);
        }
        else {
            EffectSaver saverTo = new EffectSaver();
            arEffectSaversTo.add(saverTo);
        }

        ArrayList<String> arTempLyricsFrom = mLyrics.get(mSelectedPlaylist);
        ArrayList<String> arTempLyricsTo = mLyrics.get(nPlaylistTo);
        String strLyrics = arTempLyricsFrom.get(nItem);
        arTempLyricsTo.add(strLyrics);

        if(nPlaylistTo == mPlayingPlaylist)
            mPlays.add(false);

        if(mSelectedPlaylist == nPlaylistTo)
            mSongsAdapter.notifyItemInserted(arSongsTo.size() - 1);
        saveFiles(true, true, true, true, false);
    }

    private void moveSong(int nPlaylistFrom, int nItem, int nPlaylistTo)
    {
        ArrayList<SongItem> arSongsFrom = mPlaylists.get(nPlaylistFrom);
        ArrayList<SongItem> arSongsTo = mPlaylists.get(nPlaylistTo);
        SongItem item = arSongsFrom.get(nItem);
        arSongsTo.add(item);
        item.setNumber(String.format(Locale.getDefault(), "%d", arSongsTo.size()));
        arSongsFrom.remove(nItem);

        ArrayList<EffectSaver> arEffectSaversFrom = mEffects.get(mSelectedPlaylist);
        ArrayList<EffectSaver> arEffectSaversTo = mEffects.get(nPlaylistTo);
        EffectSaver saver = arEffectSaversFrom.get(nItem);
        arEffectSaversTo.add(saver);
        arEffectSaversFrom.remove(nItem);

        ArrayList<String> arTempLyricsFrom = mLyrics.get(mSelectedPlaylist);
        ArrayList<String> arTempLyricsTo = mLyrics.get(nPlaylistTo);
        String strLyrics = arTempLyricsFrom.get(nItem);
        arTempLyricsTo.add(strLyrics);
        arTempLyricsFrom.remove(nItem);

        if(mSelectedPlaylist == mPlayingPlaylist)
            mPlays.remove(nItem);
        if(nPlaylistTo == mPlayingPlaylist)
            mPlays.add(false);

        for(int i = nItem; i < arSongsFrom.size(); i++) {
            SongItem songItem = arSongsFrom.get(i);
            songItem.setNumber(String.format(Locale.getDefault(), "%d", i+1));
        }

        if(mSelectedPlaylist == mPlayingPlaylist) {
            if(nItem == mPlaying) {
                mPlayingPlaylist = nPlaylistTo;
                mPlaying = arSongsTo.size() - 1;
                mPlaylistsAdapter.notifyDataSetChanged();
                mTabAdapter.notifyDataSetChanged();
            }
            else if(nItem < mPlaying) mPlaying--;
        }

        mSongsAdapter.notifyDataSetChanged();
        saveFiles(true, true, true, true, false);
    }

    public void changeTitleAndArtist(final int nItem)
    {
        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        final SongItem songItem = arSongs.get(nItem);

        AlertDialog.Builder builder;
        if(mActivity.isDarkMode())
            builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
        else
            builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.changeTitleAndArtist);
        LinearLayout linearLayout = new LinearLayout(mActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final ClearableEditText editTitle = new ClearableEditText(mActivity, mActivity.isDarkMode());
        editTitle.setHint(R.string.title);
        editTitle.setText(songItem.getTitle());
        final ClearableEditText editArtist = new ClearableEditText(mActivity, mActivity.isDarkMode());
        editArtist.setHint(R.string.artist);
        editArtist.setText(songItem.getArtist());
        linearLayout.addView(editTitle);
        linearLayout.addView(editArtist);
        builder.setView(linearLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                songItem.setTitle(editTitle.getText().toString());
                songItem.setArtist(editArtist.getText().toString());

                if(mSelectedPlaylist == mPlayingPlaylist && nItem == mPlaying)
                {
                    mTextTitleInPlayingBar.setText(songItem.getTitle());
                    if(songItem.getArtist() == null || songItem.getArtist().equals(""))
                    {
                        if(mActivity.isDarkMode()) mTextArtistInPlayingBar.setTextColor(mActivity.getResources().getColor(R.color.darkModeTextDarkGray));
                        else mTextArtistInPlayingBar.setTextColor(Color.argb(255, 147, 156, 160));
                        mTextArtistInPlayingBar.setText(R.string.unknownArtist);
                    }
                    else
                    {
                        mTextArtistInPlayingBar.setTextColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeGray : R.color.lightModeGray));
                        mTextArtistInPlayingBar.setText(songItem.getArtist());
                    }
                }

                mSongsAdapter.notifyItemChanged(nItem);

                saveFiles(true, true, true, true, false);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface arg0)
            {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
                editTitle.requestFocus();
                editTitle.setSelection(editTitle.getText().toString().length());
                InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (null != imm) imm.showSoftInput(editTitle, 0);
            }
        });
        alertDialog.show();
    }

    public void showPlaylistMenu(final int nPosition)
    {
        selectPlaylist(nPosition);
        String strPlaylist = mPlaylistNames.get(nPosition);

        final BottomMenu menu = new BottomMenu(mActivity);
        menu.setTitle(strPlaylist);
        menu.addMenu(getString(R.string.changePlaylistName), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_edit_dark : R.drawable.ic_actionsheet_edit, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder;
                if(mActivity.isDarkMode())
                    builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
                else
                    builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(R.string.changePlaylistName);
                final ClearableEditText editText = new ClearableEditText(mActivity, mActivity.isDarkMode());
                editText.setHint(R.string.playlist);
                editText.setText(mPlaylistNames.get(nPosition));
                builder.setView(editText);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mPlaylistNames.set(nPosition, editText.getText().toString());

                        mTabAdapter.notifyItemChanged(nPosition);
                        mPlaylistsAdapter.notifyItemChanged(nPosition);

                        saveFiles(true, true, true, true, false);
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                {
                    @Override
                    public void onShow(DialogInterface arg0)
                    {
                        if(alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        editText.requestFocus();
                        editText.setSelection(editText.getText().toString().length());
                        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (null != imm) imm.showSoftInput(editText, 0);
                    }
                });
                alertDialog.show();
            }
        });
        menu.addMenu(getString(R.string.copyPlaylist), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_copy_dark : R.drawable.ic_actionsheet_copy, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder;
                if(mActivity.isDarkMode())
                    builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
                else
                    builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(R.string.copyPlaylist);
                final ClearableEditText editText = new ClearableEditText(mActivity, mActivity.isDarkMode());
                editText.setHint(R.string.playlist);
                editText.setText(String.format(Locale.getDefault(), "%s のコピー", mPlaylistNames.get(nPosition)));
                builder.setView(editText);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int nTo = nPosition + 1;
                        mPlaylistNames.add(nTo, editText.getText().toString());
                        ArrayList<SongItem> arSongs = new ArrayList<>();
                        mPlaylists.add(nTo, arSongs);
                        ArrayList<EffectSaver> arEffectSavers = new ArrayList<>();
                        mEffects.add(nTo, arEffectSavers);
                        ArrayList<String> arTempLyrics = new ArrayList<>();
                        mLyrics.add(nTo, arTempLyrics);

                        ArrayList<SongItem> arSongsFrom = mPlaylists.get(nPosition);
                        for(SongItem item : arSongsFrom) {
                            File file = new File(item.getPath());
                            String strPath = item.getPath();
                            if(file.getParent().equals(mActivity.getFilesDir().toString()))
                                strPath = mActivity.copyFile(Uri.parse(item.getPath())).toString();
                            SongItem itemTo = new SongItem(String.format(Locale.getDefault(), "%d", arSongs.size()+1), item.getTitle(), item.getArtist(), strPath);
                            arSongs.add(itemTo);
                        }

                        ArrayList<EffectSaver> arEffectSaversFrom = mEffects.get(nPosition);
                        for(EffectSaver saver : arEffectSaversFrom) {
                            if(saver.isSave()) {
                                EffectSaver saverTo = new EffectSaver(saver);
                                arEffectSavers.add(saverTo);
                            }
                            else {
                                EffectSaver saverTo = new EffectSaver();
                                arEffectSavers.add(saverTo);
                            }
                        }

                        ArrayList<String> mLyricsFrom = mLyrics.get(mSelectedPlaylist);
                        arTempLyrics.addAll(mLyricsFrom);

                        mTabAdapter.notifyItemInserted(nTo);
                        mPlaylistsAdapter.notifyItemInserted(nTo);
                        selectPlaylist(nTo);
                        if(mActivity != null)
                            saveFiles(true, true, true, true, false);
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                {
                    @Override
                    public void onShow(DialogInterface arg0)
                    {
                        if(alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        editText.requestFocus();
                        editText.setSelection(editText.getText().toString().length());
                        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (null != imm) imm.showSoftInput(editText, 0);
                    }
                });
                alertDialog.show();
            }
        });
        menu.addDestructiveMenu(getString(R.string.emptyPlaylist), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_folder_erase_dark : R.drawable.ic_actionsheet_folder_erase, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder;
                if(mActivity.isDarkMode())
                    builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
                else
                    builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(R.string.emptyPlaylist);
                builder.setMessage(R.string.askEmptyPlaylist);
                builder.setPositiveButton(getString(R.string.decideNot), null);
                builder.setNegativeButton(getString(R.string.doEmpty), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ArrayList<SongItem> arSongs = mPlaylists.get(nPosition);
                        ArrayList<EffectSaver> arEffectSavers = mEffects.get(nPosition);
                        ArrayList<String> arTempLyrics = mLyrics.get(nPosition);
                        for(int i = 0; i < arSongs.size(); i++) {
                            SongItem song = arSongs.get(i);
                            File file = new File(song.getPath());
                            if(file.getParent() != null && file.getParent().equals(mActivity.getFilesDir().toString())) {
                                if(!file.delete()) System.out.println("ファイルが削除できませんでした");
                            }
                        }
                        arSongs.clear();
                        arEffectSavers.clear();
                        arTempLyrics.clear();

                        mSongsAdapter.notifyDataSetChanged();
                        mPlaylistsAdapter.notifyItemChanged(nPosition);
                        mTabAdapter.notifyItemChanged(nPosition);

                        saveFiles(true, true, true, true, false);
                    }
                });
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                {
                    @Override
                    public void onShow(DialogInterface arg0)
                    {
                        if(alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        positiveButton.setTextColor(getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeRed : R.color.lightModeRed));
                    }
                });
                alertDialog.show();
            }
        });
        menu.addDestructiveMenu(getString(R.string.deletePlaylist), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_delete_dark : R.drawable.ic_actionsheet_delete, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder;
                if(mActivity.isDarkMode())
                    builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
                else
                    builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(R.string.deletePlaylist);
                builder.setMessage(R.string.askDeletePlaylist);
                builder.setPositiveButton(getString(R.string.decideNot), null);
                builder.setNegativeButton(getString(R.string.doDelete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(nPosition == mPlayingPlaylist) stop();
                        else if(nPosition < mPlayingPlaylist) mPlayingPlaylist--;
                        ArrayList<SongItem> arSongs = mPlaylists.get(nPosition);
                        for(int i = 0; i < arSongs.size(); i++) {
                            SongItem song = arSongs.get(i);
                            File file = new File(song.getPath());
                            if(file.getParent().equals(mActivity.getFilesDir().toString())) {
                                if(!file.delete()) System.out.println("ファイルが削除できませんでした");
                            }
                        }
                        mPlaylists.remove(nPosition);
                        mEffects.remove(nPosition);
                        mPlaylistNames.remove(nPosition);
                        mLyrics.remove(nPosition);
                        if(mPlaylists.size() == 0)
                            addPlaylist(String.format(Locale.getDefault(), "%s 1", getString(R.string.playlist)));

                        int nSelect = nPosition;
                        if(nSelect >= mPlaylists.size()) nSelect = mPlaylists.size() - 1;

                        selectPlaylist(nSelect);

                        saveFiles(true, true, true, true, false);
                    }
                });
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                {
                    @Override
                    public void onShow(DialogInterface arg0)
                    {
                        if(alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        positiveButton.setTextColor(getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeRed : R.color.lightModeRed));
                    }
                });
                alertDialog.show();
            }
        });
        menu.setCancelMenu();
        menu.show();
    }

    public void showPlaylistTabMenu(final int nPosition)
    {
        selectPlaylist(nPosition);
        String strPlaylist = mPlaylistNames.get(nPosition);
        boolean bLockFounded = false;
        boolean bUnlockFounded = false;
        boolean bChangeArtworkFounded = false;
        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        ArrayList<EffectSaver> arEffectSavers = mEffects.get(mSelectedPlaylist);
        for(int i = 0; i < arSongs.size(); i++) {
            SongItem song = arSongs.get(i);
            song.setSelected(true);
            EffectSaver saver = arEffectSavers.get(i);
            if(song.getPathArtwork() != null && !song.getPathArtwork().equals("")) bChangeArtworkFounded = true;
            if(saver.isSave()) bLockFounded = true;
            else bUnlockFounded = true;
        }

        final BottomMenu menu = new BottomMenu(mActivity);
        menu.setTitle(strPlaylist);
        if(arSongs.size() >= 1)
            menu.addMenu(getString(R.string.selectSongs), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_select_dark : R.drawable.ic_actionsheet_select, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menu.dismiss();
                    startMultipleSelection(-1);
                }
            });
        if(arSongs.size() >= 2)
            menu.addMenu(getString(R.string.sortSongs), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_sort_dark : R.drawable.ic_actionsheet_sort, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menu.dismiss();
                    mRecyclerSongs.setPadding(0, 0, 0, (int) (64 * mActivity.getDensity()));
                    mTextFinishSort.setVisibility(View.VISIBLE);
                    mBtnAddSong.clearAnimation();
                    mBtnAddSong.setVisibility(View.GONE);
                    mSorting = true;
                    menu.dismiss();
                    mSongsAdapter.notifyDataSetChanged();

                    startSort();
                }
            });
        if(arSongs.size() >= 1) menu.addSeparator();
        if(arSongs.size() >= 1) {
            menu.addMenu(getString(R.string.changeArtwork), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_image_dark : R.drawable.ic_actionsheet_image, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeArtworkMultipleSelection();
                    menu.dismiss();
                }
            });
            if (bChangeArtworkFounded)
                menu.addDestructiveMenu(getString(R.string.resetArtwork), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_initialize_dark : R.drawable.ic_actionsheet_initialize, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        resetArtworkMultipleSelection();
                        menu.dismiss();
                    }
                });
            if (bUnlockFounded)
                menu.addMenu(getString(R.string.restoreEffect), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_lock_dark : R.drawable.ic_actionsheet_lock, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        restoreEffectMultipleSelection();
                        menu.dismiss();
                    }
                });
            if (bLockFounded)
                menu.addMenu(getString(R.string.cancelRestoreEffect), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_unlock_dark : R.drawable.ic_actionsheet_unlock, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cancelRestoreEffectMultipleSelection();
                        menu.dismiss();
                    }
                });
            menu.addSeparator();
        }
        menu.addMenu(getString(R.string.changePlaylistName), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_edit_dark : R.drawable.ic_actionsheet_edit, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder;
                if(mActivity.isDarkMode())
                    builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
                else
                    builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(R.string.changePlaylistName);
                final ClearableEditText editText = new ClearableEditText(mActivity, mActivity.isDarkMode());
                editText.setHint(R.string.playlist);
                editText.setText(mPlaylistNames.get(nPosition));
                builder.setView(editText);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mPlaylistNames.set(nPosition, editText.getText().toString());

                        mTabAdapter.notifyItemChanged(nPosition);
                        mPlaylistsAdapter.notifyItemChanged(nPosition);

                        saveFiles(true, true, true, true, false);
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                {
                    @Override
                    public void onShow(DialogInterface arg0)
                    {
                        if(alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        editText.requestFocus();
                        editText.setSelection(editText.getText().toString().length());
                        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (null != imm) imm.showSoftInput(editText, 0);
                    }
                });
                alertDialog.show();
            }
        });
        menu.addMenu(getString(R.string.copyPlaylist), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_copy_dark : R.drawable.ic_actionsheet_copy, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder;
                if(mActivity.isDarkMode())
                    builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
                else
                    builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(R.string.copyPlaylist);
                final ClearableEditText editText = new ClearableEditText(mActivity, mActivity.isDarkMode());
                editText.setHint(R.string.playlist);
                editText.setText(String.format(Locale.getDefault(), "%s のコピー", mPlaylistNames.get(nPosition)));
                builder.setView(editText);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int nTo = nPosition + 1;
                        mPlaylistNames.add(nTo, editText.getText().toString());
                        ArrayList<SongItem> arSongs = new ArrayList<>();
                        mPlaylists.add(nTo, arSongs);
                        ArrayList<EffectSaver> arEffectSavers = new ArrayList<>();
                        mEffects.add(nTo, arEffectSavers);
                        ArrayList<String> arTempLyrics = new ArrayList<>();
                        mLyrics.add(nTo, arTempLyrics);

                        ArrayList<SongItem> arSongsFrom = mPlaylists.get(nPosition);
                        for(SongItem item : arSongsFrom) {
                            File file = new File(item.getPath());
                            String strPath = item.getPath();
                            if(file.getParent().equals(mActivity.getFilesDir().toString()))
                                strPath = mActivity.copyFile(Uri.parse(item.getPath())).toString();
                            SongItem itemTo = new SongItem(String.format(Locale.getDefault(), "%d", arSongs.size()+1), item.getTitle(), item.getArtist(), strPath);
                            arSongs.add(itemTo);
                        }

                        ArrayList<EffectSaver> arEffectSaversFrom = mEffects.get(nPosition);
                        for(EffectSaver saver : arEffectSaversFrom) {
                            if(saver.isSave()) {
                                EffectSaver saverTo = new EffectSaver(saver);
                                arEffectSavers.add(saverTo);
                            }
                            else {
                                EffectSaver saverTo = new EffectSaver();
                                arEffectSavers.add(saverTo);
                            }
                        }

                        ArrayList<String> mLyricsFrom = mLyrics.get(mSelectedPlaylist);
                        arTempLyrics.addAll(mLyricsFrom);

                        mTabAdapter.notifyItemInserted(nTo);
                        mPlaylistsAdapter.notifyItemInserted(nTo);
                        selectPlaylist(nTo);
                        if(mActivity != null)
                            saveFiles(true, true, true, true, false);
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                {
                    @Override
                    public void onShow(DialogInterface arg0)
                    {
                        if(alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        editText.requestFocus();
                        editText.setSelection(editText.getText().toString().length());
                        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (null != imm) imm.showSoftInput(editText, 0);
                    }
                });
                alertDialog.show();
            }
        });
        menu.addDestructiveMenu(getString(R.string.emptyPlaylist), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_folder_erase_dark : R.drawable.ic_actionsheet_folder_erase, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder;
                if(mActivity.isDarkMode())
                    builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
                else
                    builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(R.string.emptyPlaylist);
                builder.setMessage(R.string.askEmptyPlaylist);
                builder.setPositiveButton(getString(R.string.decideNot), null);
                builder.setNegativeButton(getString(R.string.doEmpty), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ArrayList<SongItem> arSongs;
                        ArrayList<EffectSaver> arEffectSavers;
                        ArrayList<String> arTempLyrics;
                        arSongs = mPlaylists.get(nPosition);
                        arEffectSavers = mEffects.get(nPosition);
                        arTempLyrics = mLyrics.get(nPosition);
                        for(int i = 0; i < arSongs.size(); i++) {
                            SongItem song = arSongs.get(i);
                            File file = new File(song.getPath());
                            if(file.getParent() != null && file.getParent().equals(mActivity.getFilesDir().toString())) {
                                if(!file.delete()) System.out.println("ファイルが削除できませんでした");
                            }
                        }
                        arSongs.clear();
                        arEffectSavers.clear();
                        arTempLyrics.clear();

                        mSongsAdapter.notifyDataSetChanged();
                        mPlaylistsAdapter.notifyItemChanged(nPosition);
                        mTabAdapter.notifyItemChanged(nPosition);

                        saveFiles(true, true, true, true, false);
                    }
                });
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                {
                    @Override
                    public void onShow(DialogInterface arg0)
                    {
                        if(alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        positiveButton.setTextColor(getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeRed : R.color.lightModeRed));
                    }
                });
                alertDialog.show();
            }
        });
        menu.addDestructiveMenu(getString(R.string.deletePlaylist), mActivity.isDarkMode() ? R.drawable.ic_actionsheet_delete_dark : R.drawable.ic_actionsheet_delete, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder;
                if(mActivity.isDarkMode())
                    builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
                else
                    builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(R.string.deletePlaylist);
                builder.setMessage(R.string.askDeletePlaylist);
                builder.setPositiveButton(getString(R.string.decideNot), null);
                builder.setNegativeButton(getString(R.string.doDelete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(nPosition == mPlayingPlaylist) stop();
                        else if(nPosition < mPlayingPlaylist) mPlayingPlaylist--;
                        ArrayList<SongItem> arSongs = mPlaylists.get(nPosition);
                        for(int i = 0; i < arSongs.size(); i++) {
                            SongItem song = arSongs.get(i);
                            File file = new File(song.getPath());
                            if(file.getParent().equals(mActivity.getFilesDir().toString())) {
                                if(!file.delete()) System.out.println("ファイルが削除できませんでした");
                            }
                        }
                        mPlaylists.remove(nPosition);
                        mEffects.remove(nPosition);
                        mPlaylistNames.remove(nPosition);
                        mLyrics.remove(nPosition);
                        if(mPlaylists.size() == 0)
                            addPlaylist(String.format(Locale.getDefault(), "%s 1", getString(R.string.playlist)));

                        int nSelect = nPosition;
                        if(nSelect >= mPlaylists.size()) nSelect = mPlaylists.size() - 1;

                        selectPlaylist(nSelect);

                        saveFiles(true, true, true, true, false);
                    }
                });
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                {
                    @Override
                    public void onShow(DialogInterface arg0)
                    {
                        if(alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        positiveButton.setTextColor(getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeRed : R.color.lightModeRed));
                    }
                });
                alertDialog.show();
            }
        });
        menu.setCancelMenu();
        menu.show();
    }

    private void startSort()
    {
        mSongTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(RecyclerView mRecyclerSongs, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                final int fromPos = viewHolder.getAdapterPosition();
                final int toPos = target.getAdapterPosition();

                ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
                SongItem itemTemp = arSongs.get(fromPos);
                arSongs.remove(fromPos);
                arSongs.add(toPos, itemTemp);

                ArrayList<EffectSaver> arEffectSavers = mEffects.get(mSelectedPlaylist);
                EffectSaver saver = arEffectSavers.get(fromPos);
                arEffectSavers.remove(fromPos);
                arEffectSavers.add(toPos, saver);

                ArrayList<String> arTempLyrics = mLyrics.get(mSelectedPlaylist);
                String strLyrics = arTempLyrics.get(fromPos);
                arTempLyrics.remove(fromPos);
                arTempLyrics.add(toPos, strLyrics);

                if (mPlayingPlaylist == mSelectedPlaylist) {
                    Boolean bTemp = mPlays.get(fromPos);
                    mPlays.remove(fromPos);
                    mPlays.add(toPos, bTemp);
                }

                int nStart = fromPos < toPos ? fromPos : toPos;
                for (int i = nStart; i < arSongs.size(); i++) {
                    SongItem songItem = arSongs.get(i);
                    songItem.setNumber(String.format(Locale.getDefault(), "%d", i + 1));
                }

                if (fromPos == mPlaying) mPlaying = toPos;
                else if (fromPos < mPlaying && mPlaying <= toPos) mPlaying--;
                else if (fromPos > mPlaying && mPlaying >= toPos) mPlaying++;

                mSongsAdapter.notifyItemMoved(fromPos, toPos);
                int nMin, nMax;
                if(fromPos < toPos) {
                    nMin = fromPos;
                    nMax = toPos;
                }
                else {
                    nMin = toPos;
                    nMax = fromPos;
                }
                for(int i = nMin; i <= nMax; i++) {
                    mSongsAdapter.notifyItemChanged(i);
                }

                return true;
            }

            @Override
            public void clearView(RecyclerView mRecyclerSongs, RecyclerView.ViewHolder viewHolder) {
                super.clearView(mRecyclerSongs, viewHolder);

                saveFiles(true, true, true, true, false);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }
        });
        mSongTouchHelper.attachToRecyclerView(mRecyclerSongs);
    }

    public void showLyrics()
    {
        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        SongItem songItem = arSongs.get(mSelectedItem);

        ArrayList<String> arTempLyrics = mLyrics.get(mSelectedPlaylist);
        String strLyrics = arTempLyrics.get(mSelectedItem);

        String strTitle = songItem.getTitle();
        if(songItem.getArtist() != null && !songItem.getArtist().equals(""))
            strTitle += " - " + songItem.getArtist();
        mTextLyricsTitle.setText(strTitle);

        if(strLyrics == null || strLyrics.equals(""))
            strLyrics = getLyrics(mSelectedPlaylist, mSelectedItem);
        if(strLyrics == null || strLyrics.equals("")) {
            mTextNoLyrics.setVisibility(View.VISIBLE);
            mTextLyrics.setVisibility(View.INVISIBLE);
            mBtnEdit.clearAnimation();
            mBtnEdit.setVisibility(View.INVISIBLE);
            mImgEdit.setVisibility(View.VISIBLE);
            mTextTapEdit.setVisibility(View.VISIBLE);
        }
        else {
            mTextLyrics.setText(strLyrics);
            mTextNoLyrics.setVisibility(View.INVISIBLE);
            mTextLyrics.setVisibility(View.VISIBLE);
            mBtnEdit.setVisibility(View.VISIBLE);
            mImgEdit.setVisibility(View.INVISIBLE);
            mTextTapEdit.setVisibility(View.INVISIBLE);
        }

        mRelativeSongs.setVisibility(View.INVISIBLE);
        mRelativeLyrics.setVisibility(View.VISIBLE);
        mActivity.getViewSep1().setVisibility(View.VISIBLE);
    }

    public void setSavingEffect()
    {
        ArrayList<EffectSaver> arEffectSavers = mEffects.get(mSelectedPlaylist);
        EffectSaver saver = arEffectSavers.get(mSelectedItem);
        saver.setSave(true);
        saver.setSpeed(mActivity.controlFragment.getSpeed());
        saver.setPitch(mActivity.controlFragment.getPitch());
        saver.setVol(mActivity.equalizerFragment.getArSeek().get(0).getProgress() - 30);
        saver.setEQ20K(mActivity.equalizerFragment.getArSeek().get(1).getProgress() - 30);
        saver.setEQ16K(mActivity.equalizerFragment.getArSeek().get(2).getProgress() - 30);
        saver.setEQ12_5K(mActivity.equalizerFragment.getArSeek().get(3).getProgress() - 30);
        saver.setEQ10K(mActivity.equalizerFragment.getArSeek().get(4).getProgress() - 30);
        saver.setEQ8K(mActivity.equalizerFragment.getArSeek().get(5).getProgress() - 30);
        saver.setEQ6_3K(mActivity.equalizerFragment.getArSeek().get(6).getProgress() - 30);
        saver.setEQ5K(mActivity.equalizerFragment.getArSeek().get(7).getProgress() - 30);
        saver.setEQ4K(mActivity.equalizerFragment.getArSeek().get(8).getProgress() - 30);
        saver.setEQ3_15K(mActivity.equalizerFragment.getArSeek().get(9).getProgress() - 30);
        saver.setEQ2_5K(mActivity.equalizerFragment.getArSeek().get(10).getProgress() - 30);
        saver.setEQ2K(mActivity.equalizerFragment.getArSeek().get(11).getProgress() - 30);
        saver.setEQ1_6K(mActivity.equalizerFragment.getArSeek().get(12).getProgress() - 30);
        saver.setEQ1_25K(mActivity.equalizerFragment.getArSeek().get(13).getProgress() - 30);
        saver.setEQ1K(mActivity.equalizerFragment.getArSeek().get(14).getProgress() - 30);
        saver.setEQ800(mActivity.equalizerFragment.getArSeek().get(15).getProgress() - 30);
        saver.setEQ630(mActivity.equalizerFragment.getArSeek().get(16).getProgress() - 30);
        saver.setEQ500(mActivity.equalizerFragment.getArSeek().get(17).getProgress() - 30);
        saver.setEQ400(mActivity.equalizerFragment.getArSeek().get(18).getProgress() - 30);
        saver.setEQ315(mActivity.equalizerFragment.getArSeek().get(19).getProgress() - 30);
        saver.setEQ250(mActivity.equalizerFragment.getArSeek().get(20).getProgress() - 30);
        saver.setEQ200(mActivity.equalizerFragment.getArSeek().get(21).getProgress() - 30);
        saver.setEQ160(mActivity.equalizerFragment.getArSeek().get(22).getProgress() - 30);
        saver.setEQ125(mActivity.equalizerFragment.getArSeek().get(23).getProgress() - 30);
        saver.setEQ100(mActivity.equalizerFragment.getArSeek().get(24).getProgress() - 30);
        saver.setEQ80(mActivity.equalizerFragment.getArSeek().get(25).getProgress() - 30);
        saver.setEQ63(mActivity.equalizerFragment.getArSeek().get(26).getProgress() - 30);
        saver.setEQ50(mActivity.equalizerFragment.getArSeek().get(27).getProgress() - 30);
        saver.setEQ40(mActivity.equalizerFragment.getArSeek().get(28).getProgress() - 30);
        saver.setEQ31_5(mActivity.equalizerFragment.getArSeek().get(29).getProgress() - 30);
        saver.setEQ25(mActivity.equalizerFragment.getArSeek().get(30).getProgress() - 30);
        saver.setEQ20(mActivity.equalizerFragment.getArSeek().get(31).getProgress() - 30);
        saver.setEffectItems(mActivity.effectFragment.getEffectItems());
        saver.setPan(mActivity.effectFragment.getPan());
        saver.setFreq(mActivity.effectFragment.getFreq());
        saver.setBPM(mActivity.effectFragment.getBPM());
        saver.setSoundEffectVolume(mActivity.effectFragment.getSoundEffectVolume());
        saver.setTimeOmIncreaseSpeed(mActivity.effectFragment.getTimeOfIncreaseSpeed());
        saver.setIncreaseSpeed(mActivity.effectFragment.getIncreaseSpeed());
        saver.setTimeOmDecreaseSpeed(mActivity.effectFragment.getTimeOfDecreaseSpeed());
        saver.setDecreaseSpeed(mActivity.effectFragment.getDecreaseSpeed());
        saver.setCompGain(mActivity.effectFragment.getCompGain());
        saver.setCompThreshold(mActivity.effectFragment.getCompThreshold());
        saver.setCompRatio(mActivity.effectFragment.getCompRatio());
        saver.setCompAttack(mActivity.effectFragment.getCompRatio());
        saver.setCompRelease(mActivity.effectFragment.getCompRelease());
        saver.setEchoDry(mActivity.effectFragment.getEchoDry());
        saver.setEchoWet(mActivity.effectFragment.getEchoWet());
        saver.setEchoFeedback(mActivity.effectFragment.getEchoFeedback());
        saver.setEchoDelay(mActivity.effectFragment.getEchoDelay());
        saver.setReverbDry(mActivity.effectFragment.getReverbDry());
        saver.setReverbWet(mActivity.effectFragment.getReverbWet());
        saver.setReverbRoomSize(mActivity.effectFragment.getReverbRoomSize());
        saver.setReverbDamp(mActivity.effectFragment.getReverbDamp());
        saver.setReverbWidth(mActivity.effectFragment.getReverbWidth());
        saver.setChorusDry(mActivity.effectFragment.getChorusDry());
        saver.setChorusWet(mActivity.effectFragment.getChorusWet());
        saver.setChorusFeedback(mActivity.effectFragment.getChorusFeedback());
        saver.setChorusMinSweep(mActivity.effectFragment.getChorusMinSweep());
        saver.setChorusMaxSweep(mActivity.effectFragment.getChorusMaxSweep());
        saver.setChorusRate(mActivity.effectFragment.getChorusRate());
        saver.setDistortionDrive(mActivity.effectFragment.getDistortionDrive());
        saver.setDistortionDry(mActivity.effectFragment.getDistortionDry());
        saver.setDistortionWet(mActivity.effectFragment.getDistortionWet());
        saver.setDistortionFeedback(mActivity.effectFragment.getDistortionFeedback());
        saver.setDistortionVolume(mActivity.effectFragment.getDistortionVolume());
        if(mSelectedPlaylist == mPlayingPlaylist && mSelectedItem == mPlaying) {
            if(mActivity.loopFragment.getABButton().getVisibility() == View.VISIBLE) saver.setIsABLoop(true);
            else saver.setIsABLoop(false);
            saver.setIsLoopA(mActivity.isLoopA());
            saver.setLoopA(mActivity.getLoopAPos());
            saver.setIsLoopB(mActivity.isLoopB());
            saver.setLoopB(mActivity.getLoopBPos());
            saver.setArMarkerTime(mActivity.loopFragment.getArMarkerTime());
            saver.setIsLoopMarker(mActivity.loopFragment.getBtnLoopmarker().isSelected());
            saver.setMarker(mActivity.loopFragment.getMarker());
        }
        saver.setReverbSelected(mActivity.effectFragment.getReverbSelected());
        saver.setEchoSelected(mActivity.effectFragment.getEchoSelected());
        saver.setChorusSelected(mActivity.effectFragment.getChorusSelected());
        saver.setDistortionSelected(mActivity.effectFragment.getDistortionSelected());
        saver.setCompSelected(mActivity.effectFragment.getCompSelected());
        saver.setSoundEffectSelected(mActivity.effectFragment.getSoundEffectSelected());

        saveFiles(false, true, false, false, false);
    }

    private void cancelSavingEffect()
    {
        ArrayList<EffectSaver> arEffectSavers = mEffects.get(mSelectedPlaylist);
        EffectSaver saver = arEffectSavers.get(mSelectedItem);
        saver.setSave(false);

        saveFiles(false, true, false, false, false);
    }

    public void updateSavingEffect()
    {
        if(MainActivity.sStream == 0 || mPlaying == -1) return;
        ArrayList<EffectSaver> arEffectSavers = mEffects.get(mPlayingPlaylist);
        EffectSaver saver = arEffectSavers.get(mPlaying);
        if(saver.isSave()) {
            saver.setSpeed(mActivity.controlFragment.getSpeed());
            saver.setPitch(mActivity.controlFragment.getPitch());
            saver.setVol(mActivity.equalizerFragment.getArSeek().get(0).getProgress() - 30);
            saver.setEQ20K(mActivity.equalizerFragment.getArSeek().get(1).getProgress() - 30);
            saver.setEQ16K(mActivity.equalizerFragment.getArSeek().get(2).getProgress() - 30);
            saver.setEQ12_5K(mActivity.equalizerFragment.getArSeek().get(3).getProgress() - 30);
            saver.setEQ10K(mActivity.equalizerFragment.getArSeek().get(4).getProgress() - 30);
            saver.setEQ8K(mActivity.equalizerFragment.getArSeek().get(5).getProgress() - 30);
            saver.setEQ6_3K(mActivity.equalizerFragment.getArSeek().get(6).getProgress() - 30);
            saver.setEQ5K(mActivity.equalizerFragment.getArSeek().get(7).getProgress() - 30);
            saver.setEQ4K(mActivity.equalizerFragment.getArSeek().get(8).getProgress() - 30);
            saver.setEQ3_15K(mActivity.equalizerFragment.getArSeek().get(9).getProgress() - 30);
            saver.setEQ2_5K(mActivity.equalizerFragment.getArSeek().get(10).getProgress() - 30);
            saver.setEQ2K(mActivity.equalizerFragment.getArSeek().get(11).getProgress() - 30);
            saver.setEQ1_6K(mActivity.equalizerFragment.getArSeek().get(12).getProgress() - 30);
            saver.setEQ1_25K(mActivity.equalizerFragment.getArSeek().get(13).getProgress() - 30);
            saver.setEQ1K(mActivity.equalizerFragment.getArSeek().get(14).getProgress() - 30);
            saver.setEQ800(mActivity.equalizerFragment.getArSeek().get(15).getProgress() - 30);
            saver.setEQ630(mActivity.equalizerFragment.getArSeek().get(16).getProgress() - 30);
            saver.setEQ500(mActivity.equalizerFragment.getArSeek().get(17).getProgress() - 30);
            saver.setEQ400(mActivity.equalizerFragment.getArSeek().get(18).getProgress() - 30);
            saver.setEQ315(mActivity.equalizerFragment.getArSeek().get(19).getProgress() - 30);
            saver.setEQ250(mActivity.equalizerFragment.getArSeek().get(20).getProgress() - 30);
            saver.setEQ200(mActivity.equalizerFragment.getArSeek().get(21).getProgress() - 30);
            saver.setEQ160(mActivity.equalizerFragment.getArSeek().get(22).getProgress() - 30);
            saver.setEQ125(mActivity.equalizerFragment.getArSeek().get(23).getProgress() - 30);
            saver.setEQ100(mActivity.equalizerFragment.getArSeek().get(24).getProgress() - 30);
            saver.setEQ80(mActivity.equalizerFragment.getArSeek().get(25).getProgress() - 30);
            saver.setEQ63(mActivity.equalizerFragment.getArSeek().get(26).getProgress() - 30);
            saver.setEQ50(mActivity.equalizerFragment.getArSeek().get(27).getProgress() - 30);
            saver.setEQ40(mActivity.equalizerFragment.getArSeek().get(28).getProgress() - 30);
            saver.setEQ31_5(mActivity.equalizerFragment.getArSeek().get(29).getProgress() - 30);
            saver.setEQ25(mActivity.equalizerFragment.getArSeek().get(30).getProgress() - 30);
            saver.setEQ20(mActivity.equalizerFragment.getArSeek().get(31).getProgress() - 30);
            saver.setEffectItems(mActivity.effectFragment.getEffectItems());
            saver.setPan(mActivity.effectFragment.getPan());
            saver.setFreq(mActivity.effectFragment.getFreq());
            saver.setBPM(mActivity.effectFragment.getBPM());
            saver.setSoundEffectVolume(mActivity.effectFragment.getSoundEffectVolume());
            saver.setTimeOmIncreaseSpeed(mActivity.effectFragment.getTimeOfIncreaseSpeed());
            saver.setIncreaseSpeed(mActivity.effectFragment.getIncreaseSpeed());
            saver.setTimeOmDecreaseSpeed(mActivity.effectFragment.getTimeOfDecreaseSpeed());
            saver.setDecreaseSpeed(mActivity.effectFragment.getDecreaseSpeed());
            saver.setCompGain(mActivity.effectFragment.getCompGain());
            saver.setCompThreshold(mActivity.effectFragment.getCompThreshold());
            saver.setCompRatio(mActivity.effectFragment.getCompRatio());
            saver.setCompAttack(mActivity.effectFragment.getCompAttack());
            saver.setCompRelease(mActivity.effectFragment.getCompRelease());
            saver.setEchoDry(mActivity.effectFragment.getEchoDry());
            saver.setEchoWet(mActivity.effectFragment.getEchoWet());
            saver.setEchoFeedback(mActivity.effectFragment.getEchoFeedback());
            saver.setEchoDelay(mActivity.effectFragment.getEchoDelay());
            saver.setReverbDry(mActivity.effectFragment.getReverbDry());
            saver.setReverbWet(mActivity.effectFragment.getReverbWet());
            saver.setReverbRoomSize(mActivity.effectFragment.getReverbRoomSize());
            saver.setReverbDamp(mActivity.effectFragment.getReverbDamp());
            saver.setReverbWidth(mActivity.effectFragment.getReverbWidth());
            saver.setChorusDry(mActivity.effectFragment.getChorusDry());
            saver.setChorusWet(mActivity.effectFragment.getChorusWet());
            saver.setChorusFeedback(mActivity.effectFragment.getChorusFeedback());
            saver.setChorusMinSweep(mActivity.effectFragment.getChorusMinSweep());
            saver.setChorusMaxSweep(mActivity.effectFragment.getChorusMaxSweep());
            saver.setChorusRate(mActivity.effectFragment.getChorusRate());
            saver.setDistortionDrive(mActivity.effectFragment.getDistortionDrive());
            saver.setDistortionDry(mActivity.effectFragment.getDistortionDry());
            saver.setDistortionWet(mActivity.effectFragment.getDistortionWet());
            saver.setDistortionFeedback(mActivity.effectFragment.getDistortionFeedback());
            saver.setDistortionVolume(mActivity.effectFragment.getDistortionVolume());
            if(mActivity.loopFragment.getABButton().getVisibility() == View.VISIBLE) saver.setIsABLoop(true);
            else saver.setIsABLoop(false);
            saver.setIsLoopA(mActivity.isLoopA());
            saver.setLoopA(mActivity.getLoopAPos());
            saver.setIsLoopB(mActivity.isLoopB());
            saver.setLoopB(mActivity.getLoopBPos());
            saver.setArMarkerTime(mActivity.loopFragment.getArMarkerTime());
            saver.setIsLoopMarker(mActivity.loopFragment.getBtnLoopmarker().isSelected());
            saver.setMarker(mActivity.loopFragment.getMarker());
            saver.setReverbSelected(mActivity.effectFragment.getReverbSelected());
            saver.setEchoSelected(mActivity.effectFragment.getEchoSelected());
            saver.setChorusSelected(mActivity.effectFragment.getChorusSelected());
            saver.setDistortionSelected(mActivity.effectFragment.getDistortionSelected());
            saver.setCompSelected(mActivity.effectFragment.getCompSelected());
            saver.setSoundEffectSelected(mActivity.effectFragment.getSoundEffectSelected());

            saveFiles(false, true, false, false, false);
        }
    }

    private void restoreEffect()
    {
        ArrayList<EffectSaver> arEffectSavers = mEffects.get(mPlayingPlaylist);
        EffectSaver saver = arEffectSavers.get(mPlaying);
        mActivity.controlFragment.setSpeed(saver.getSpeed(), false);
        mActivity.controlFragment.setPitch(saver.getPitch(), false);
        mActivity.equalizerFragment.setVol(saver.getVol(), false);
        mActivity.equalizerFragment.setEQ(1, saver.getEQ20K(), false);
        mActivity.equalizerFragment.setEQ(2, saver.getEQ16K(), false);
        mActivity.equalizerFragment.setEQ(3, saver.getEQ12_5K(), false);
        mActivity.equalizerFragment.setEQ(4, saver.getEQ10K(), false);
        mActivity.equalizerFragment.setEQ(5, saver.getEQ8K(), false);
        mActivity.equalizerFragment.setEQ(6, saver.getEQ6_3K(), false);
        mActivity.equalizerFragment.setEQ(7, saver.getEQ5K(), false);
        mActivity.equalizerFragment.setEQ(8, saver.getEQ4K(), false);
        mActivity.equalizerFragment.setEQ(9, saver.getEQ3_15K(), false);
        mActivity.equalizerFragment.setEQ(10, saver.getEQ2_5K(), false);
        mActivity.equalizerFragment.setEQ(11, saver.getEQ2K(), false);
        mActivity.equalizerFragment.setEQ(12, saver.getEQ1_6K(), false);
        mActivity.equalizerFragment.setEQ(13, saver.getEQ1_25K(), false);
        mActivity.equalizerFragment.setEQ(14, saver.getEQ1K(), false);
        mActivity.equalizerFragment.setEQ(15, saver.getEQ800(), false);
        mActivity.equalizerFragment.setEQ(16, saver.getEQ630(), false);
        mActivity.equalizerFragment.setEQ(17, saver.getEQ500(), false);
        mActivity.equalizerFragment.setEQ(18, saver.getEQ400(), false);
        mActivity.equalizerFragment.setEQ(19, saver.getEQ315(), false);
        mActivity.equalizerFragment.setEQ(20, saver.getEQ250(), false);
        mActivity.equalizerFragment.setEQ(21, saver.getEQ200(), false);
        mActivity.equalizerFragment.setEQ(22, saver.getEQ160(), false);
        mActivity.equalizerFragment.setEQ(23, saver.getEQ125(), false);
        mActivity.equalizerFragment.setEQ(24, saver.getEQ100(), false);
        mActivity.equalizerFragment.setEQ(25, saver.getEQ80(), false);
        mActivity.equalizerFragment.setEQ(26, saver.getEQ63(), false);
        mActivity.equalizerFragment.setEQ(27, saver.getEQ50(), false);
        mActivity.equalizerFragment.setEQ(28, saver.getEQ40(), false);
        mActivity.equalizerFragment.setEQ(29, saver.getEQ31_5(), false);
        mActivity.equalizerFragment.setEQ(30, saver.getEQ25(), false);
        mActivity.equalizerFragment.setEQ(31, saver.getEQ20(), false);
        ArrayList<EqualizerItem> arEqualizerItems = mActivity.equalizerFragment.getArEqualizerItems();
        for(int i = 0; i < arEqualizerItems.size(); i++) {
            EqualizerItem item = arEqualizerItems.get(i);
            item.setSelected(false);
        }
        mActivity.equalizerFragment.getEqualizersAdapter().notifyDataSetChanged();
        mActivity.effectFragment.setEffectItems(saver.getEffectItems());
        mActivity.effectFragment.setPan(saver.getPan(), false);
        mActivity.effectFragment.setFreq(saver.getFreq(), false);
        mActivity.effectFragment.setBPM(saver.getBPM());
        mActivity.effectFragment.setSoundEffect(saver.getSoundEffectVolume(), false);
        mActivity.effectFragment.setTimeOfIncreaseSpeed(saver.getTimeOmIncreaseSpeed());
        mActivity.effectFragment.setIncreaseSpeed(saver.getIncreaseSpeed());
        mActivity.effectFragment.setTimeOfDecreaseSpeed(saver.getTimeOmDecreaseSpeed());
        mActivity.effectFragment.setDecreaseSpeed(saver.getDecreaseSpeed());
        mActivity.effectFragment.setComp(saver.getCompGain(), saver.getCompThreshold(), saver.getCompRatio(), saver.getCompAttack(), saver.getCompRelease(), false);
        mActivity.effectFragment.setEcho(saver.getEchoDry(), saver.getEchoWet(), saver.getEchoFeedback(), saver.getEchoDelay(), false);
        mActivity.effectFragment.setReverb(saver.getReverbDry(), saver.getReverbWet(), saver.getReverbRoomSize(), saver.getReverbDamp(), saver.getReverbWidth(), false);
        mActivity.effectFragment.setChorus(saver.getChorusDry(), saver.getChorusWet(), saver.getChorusFeedback(), saver.getChorusMinSweep(), saver.getChorusMaxSweep(), saver.getChorusRate(), false);
        mActivity.effectFragment.setDistortion(saver.getDistortionDrive(), saver.getDistortionDry(), saver.getDistortionWet(), saver.getDistortionFeedback(), saver.getDistortionVolume(), false);
        if(saver.isABLoop()) mActivity.loopFragment.getRadioGroupLoopMode().check(R.id.radioButtonABLoop);
        else mActivity.loopFragment.getRadioGroupLoopMode().check(R.id.radioButtonMarkerPlay);
        if(saver.isLoopA()) mActivity.loopFragment.setLoopA(saver.getLoopA(), false);
        if(saver.isLoopB()) mActivity.loopFragment.setLoopB(saver.getLoopB(), false);
        mActivity.loopFragment.setArMarkerTime(saver.getArMarkerTime());
        if(saver.isLoopMarker()) {
            mActivity.loopFragment.getBtnLoopmarker().setSelected(true);
            mActivity.loopFragment.getBtnLoopmarker().setAlpha(0.3f);
        }
        else {
            mActivity.loopFragment.getBtnLoopmarker().setSelected(false);
            mActivity.loopFragment.getBtnLoopmarker().setAlpha(1.0f);
        }
        mActivity.loopFragment.setMarker(saver.getMarker());
        mActivity.effectFragment.setReverbSelected(saver.getReverbSelected());
        mActivity.effectFragment.setEchoSelected(saver.getEchoSelected());
        mActivity.effectFragment.setChorusSelected(saver.getChorusSelected());
        mActivity.effectFragment.setDistortionSelected(saver.getDistortionSelected());
        mActivity.effectFragment.setCompSelected(saver.getCompSelected());
        mActivity.effectFragment.setSoundEffectSelected(saver.getSoundEffectSelected());
    }

    private void saveSong(int nPurpose, String strFileName)
    {
        AlertDialog.Builder builder;
        if(mActivity.isDarkMode())
            builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
        else
            builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.saving);
        LinearLayout linearLayout = new LinearLayout(mActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        mProgress = new ProgressBar(mActivity, null, android.R.attr.progressBarStyleHorizontal);
        mProgress.setMax(100);
        mProgress.setProgress(0);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        param.topMargin = (int)(24 *  mActivity.getDensity());
        param.leftMargin = param.rightMargin = (int)(16 *  mActivity.getDensity());
        linearLayout.addView(mProgress, param);
        builder.setView(linearLayout);

        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        SongItem item = arSongs.get(mSelectedItem);
        String strPath = item.getPath();
        int _hTempStream;
        Uri uri = Uri.parse(strPath);
        if(uri.getScheme() != null && uri.getScheme().equals("content")) {
            ContentResolver cr = mActivity.getApplicationContext().getContentResolver();
            try {
                MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                params.assetFileDescriptor = cr.openAssetFileDescriptor(Uri.parse(strPath), "r");
                if(params.assetFileDescriptor == null) return;
                params.fileChannel = params.assetFileDescriptor.createInputStream().getChannel();
                _hTempStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        else {
            _hTempStream = BASS.BASS_StreamCreateFile(strPath, 0, 0, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
        }
        if(_hTempStream == 0) return;

        _hTempStream = BASS_FX.BASS_FX_ReverseCreate(_hTempStream, 2, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
        _hTempStream = BASS_FX.BASS_FX_TempoCreate(_hTempStream, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
        final int hTempStream = _hTempStream;
        int chan = BASS_FX.BASS_FX_TempoGetSource(hTempStream);
        if(mActivity.effectFragment.isReverse())
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
        BASS.BASS_ChannelSetAttribute(hTempStream, BASS_FX.BASS_ATTRIB_TEMPO, mActivity.controlFragment.getSpeed());
        BASS.BASS_ChannelSetAttribute(hTempStream, BASS_FX.BASS_ATTRIB_TEMPO_PITCH, mActivity.controlFragment.getPitch());
        int[] arHFX = new int[] {hTempFx20K, hTempFx16K, hTempFx12_5K, hTempFx10K, hTempFx8K, hTempFx6_3K, hTempFx5K, hTempFx4K, hTempFx3_15K, hTempFx2_5K, hTempFx2K, hTempFx1_6K, hTempFx1_25K, hTempFx1K, hTempFx800, hTempFx630, hTempFx500, hTempFx400, hTempFx315, hTempFx250, hTempFx200, hTempFx160, hTempFx125, hTempFx100, hTempFx80, hTempFx63, hTempFx50, hTempFx40, hTempFx31_5, hTempFx25, hTempFx20};
        float fLevel = mActivity.equalizerFragment.getArSeek().get(0).getProgress() - 30;
        if(fLevel == 0) fLevel = 1.0f;
        else if(fLevel < 0) fLevel = (fLevel + 30.0f) / 30.0f;
        else fLevel += 1.0f;
        BASS_FX.BASS_BFX_VOLUME vol = new BASS_FX.BASS_BFX_VOLUME();
        vol.lChannel = 0;
        vol.fVolume = fLevel;
        BASS.BASS_FXSetParameters(hTempFxVol, vol);

        for(int i = 0; i < 31; i++)
        {
            int nLevel = mActivity.equalizerFragment.getArSeek().get(i+1).getProgress() - 30;
            BASS_FX.BASS_BFX_PEAKEQ eq = new BASS_FX.BASS_BFX_PEAKEQ();
            eq.fBandwidth = 0.7f;
            eq.fQ = 0.0f;
            eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
            eq.fGain = nLevel;
            eq.fCenter = mActivity.equalizerFragment.getArCenters()[i];
            BASS.BASS_FXSetParameters(arHFX[i], eq);
        }
        mActivity.effectFragment.applyEffect(hTempStream, item);
        String strPathTo;
        if(nPurpose == 0) // saveSongToLocal
        {
            int i = 0;
            File fileForCheck;
            while (true) {
                strPathTo = mActivity.getFilesDir() + "/recorded" + String.format(Locale.getDefault(), "%d", i) + ".mp3";
                fileForCheck = new File(strPathTo);
                if (!fileForCheck.exists()) break;
                i++;
            }
        }
        else if(nPurpose == 1) // export
        {
            File fileDir = new File(mActivity.getExternalCacheDir() + "/export");
            if(!fileDir.exists()) {
                if(!fileDir.mkdir()) System.out.println("ディレクトリが作成できませんでした");
            }
            strPathTo = mActivity.getExternalCacheDir() + "/export/";
            strPathTo += strFileName + ".mp3";
            File file = new File(strPathTo);
            if(file.exists()) {
                if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            }
        }
        else // saveSongToGallery
        {
            File fileDir = new File(mActivity.getExternalCacheDir() + "/export");
            if(!fileDir.exists()) {
                if(!fileDir.mkdir()) System.out.println("ディレクトリが作成できませんでした");
            }
            strPathTo = mActivity.getExternalCacheDir() + "/export/export.wav";
            File file = new File(strPathTo);
            if (file.exists()) {
                if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            }
        }

        double _dEnd = BASS.BASS_ChannelBytes2Seconds(hTempStream, BASS.BASS_ChannelGetLength(hTempStream, BASS.BASS_POS_BYTE));
        if(mSelectedPlaylist == mPlayingPlaylist && mSelectedItem == mPlaying)
        {
            if(mActivity.isLoopA())
                BASS.BASS_ChannelSetPosition(hTempStream, BASS.BASS_ChannelSeconds2Bytes(hTempStream, mActivity.getLoopAPos()), BASS.BASS_POS_BYTE);
            if(mActivity.isLoopB())
                _dEnd = mActivity.getLoopBPos();
        }
        final double dEnd = _dEnd;
        int hTempEncode;
        if(nPurpose == 2) // saveSongToGallery
            hTempEncode = BASSenc.BASS_Encode_Start(hTempStream, strPathTo, BASSenc.BASS_ENCODE_PCM | BASSenc.BASS_ENCODE_FP_16BIT, null, null);
        else
            hTempEncode = BASSenc_MP3.BASS_Encode_MP3_StartFile(hTempStream, "", 0, strPathTo);
        final int hEncode = hTempEncode;
        mFinish = false;
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mFinish = true;
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface arg0)
            {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
            }
        });
        alertDialog.show();

        if(mSongSavingTask != null && mSongSavingTask.getStatus() == AsyncTask.Status.RUNNING)
            mSongSavingTask.cancel(true);
        mSongSavingTask = new SongSavingTask(nPurpose, this, hTempStream, hEncode, strPathTo, alertDialog, dEnd);
        mSongSavingTask.execute(0);
    }

    public void saveSongToLocal()
    {
        saveSong(0, null);
    }

    public void saveSongToGallery()
    {
        if(Build.VERSION.SDK_INT >= 23) {
            if (mActivity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    mActivity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                mActivity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return;
            }
        }
        saveSong(2, null);
    }

    public void finishSaveSongToLocal(int hTempStream, int hEncode, String strPathTo, AlertDialog alert)
    {
        if(alert.isShowing()) alert.dismiss();

        BASSenc.BASS_Encode_Stop(hEncode);
        BASS.BASS_StreamFree(hTempStream);

        if(mFinish) {
            File file = new File(strPathTo);
            if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            mFinish = false;
            return;
        }

        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        SongItem item = arSongs.get(mSelectedItem);
        ArrayList<EffectSaver> arEffectSavers = mEffects.get(mSelectedPlaylist);
        EffectSaver saver = arEffectSavers.get(mSelectedItem);
        ArrayList<String> arTempLyrics = mLyrics.get(mSelectedPlaylist);
        String strLyrics = arTempLyrics.get(mSelectedItem);

        String strTitle = item.getTitle();
        float fSpeed = mActivity.controlFragment.getSpeed();
        float fPitch = mActivity.controlFragment.getPitch();
        String strSpeed = String.format(Locale.getDefault(), "%.1f%%", fSpeed + 100);
        String strPitch;
        if(fPitch >= 0.05f)
            strPitch = String.format(Locale.getDefault(), "♯%.1f", fPitch);
        else if(fPitch <= -0.05f)
            strPitch = String.format(Locale.getDefault(), "♭%.1f", fPitch * -1);
        else {
            strPitch = String.format(Locale.getDefault(), "%.1f", fPitch < 0.0f ? fPitch * -1 : fPitch);
            if(strPitch.equals("-0.0")) strPitch = "0.0";
        }

        if(fSpeed != 0.0f && fPitch != 0.0f)
            strTitle += "(" + getString(R.string.speed) + strSpeed + "," + getString(R.string.pitch) + strPitch + ")";
        else if(fSpeed != 0.0f)
            strTitle += "(" + getString(R.string.speed) + strSpeed + ")";
        else if(fPitch != 0.0f)
            strTitle += "(" + getString(R.string.pitch) + strPitch + ")";

        SongItem itemNew = new SongItem(String.format(Locale.getDefault(), "%d", arSongs.size()+1), strTitle, item.getArtist(), strPathTo);
        arSongs.add(itemNew);
        EffectSaver saverNew = new EffectSaver(saver);
        arEffectSavers.add(saverNew);
        arTempLyrics.add(strLyrics);
        if(mSelectedPlaylist == mPlayingPlaylist) mPlays.add(false);
        mSongsAdapter.notifyItemInserted(arSongs.size() - 1);

        saveFiles(true, true, true, true, false);
    }

    public void export()
    {
        AlertDialog.Builder builder;
        if(mActivity.isDarkMode())
            builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
        else
            builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.export);
        LinearLayout linearLayout = new LinearLayout(mActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final ClearableEditText editTitle = new ClearableEditText(mActivity, mActivity.isDarkMode());
        editTitle.setHint(R.string.fileName);
        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        SongItem item = arSongs.get(mSelectedItem);
        String strTitle = item.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_");
        float fSpeed = mActivity.controlFragment.getSpeed();
        float fPitch = mActivity.controlFragment.getPitch();
        String strSpeed = String.format(Locale.getDefault(), "%.1f%%", fSpeed + 100);
        String strPitch;
        if(fPitch >= 0.05f)
            strPitch = String.format(Locale.getDefault(), "♯%.1f", fPitch);
        else if(fPitch <= -0.05f)
            strPitch = String.format(Locale.getDefault(), "♭%.1f", fPitch * -1);
        else {
            strPitch = String.format(Locale.getDefault(), "%.1f", fPitch < 0.0f ? fPitch * -1 : fPitch);
            if(strPitch.equals("-0.0")) strPitch = "0.0";
        }
        if(fSpeed != 0.0f && fPitch != 0.0f)
            strTitle += "(" + getString(R.string.speed) + strSpeed + "," + getString(R.string.pitch) + strPitch + ")";
        else if(fSpeed != 0.0f)
            strTitle += "(" + getString(R.string.speed) + strSpeed + ")";
        else if(fPitch != 0.0f)
            strTitle += "(" + getString(R.string.pitch) + strPitch + ")";
        DateFormat df = new SimpleDateFormat("_yyyyMMdd_HHmmss", Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        editTitle.setText(String.format(Locale.getDefault(), "%s%s", strTitle, df.format(date)));
        linearLayout.addView(editTitle);
        builder.setView(linearLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                saveSong(1, editTitle.getText().toString().replaceAll("[\\\\/:*?\"<>|]", "_"));
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface arg0)
            {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
                editTitle.requestFocus();
                editTitle.setSelection(editTitle.getText().toString().length());
                InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (null != imm) imm.showSoftInput(editTitle, 0);
            }
        });
        alertDialog.show();
    }

    public void finishExport(int hTempStream, int hEncode, String strPathTo, AlertDialog alert)
    {
        if(alert.isShowing()) alert.dismiss();

        BASSenc.BASS_Encode_Stop(hEncode);
        BASS.BASS_StreamFree(hTempStream);

        if(mFinish) {
            File file = new File(strPathTo);
            if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            mFinish = false;
            return;
        }

        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND);
        share.setType("audio/mp3");
        File file = new File(strPathTo);
        Uri uri = FileProvider.getUriForFile(mActivity, "com.edolfzoku.hayaemon2", file);
        PackageManager pm = mActivity.getPackageManager();
        int flag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flag = PackageManager.MATCH_ALL;
        else flag = PackageManager.MATCH_DEFAULT_ONLY;
        List<ResolveInfo> resInfoList = pm.queryIntentActivities(share, flag);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            mActivity.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        share.putExtra(Intent.EXTRA_STREAM, uri);
        startActivityForResult(Intent.createChooser(share, getString(R.string.export)), 0);

        file.deleteOnExit();
    }

    public void finishSaveSongToGallery(int hTempStream, int hEncode, String strPathTo, AlertDialog alert)
    {
        BASSenc.BASS_Encode_Stop(hEncode);
        int nLength = (int)BASS.BASS_ChannelBytes2Seconds(hTempStream, BASS.BASS_ChannelGetLength(hTempStream, BASS.BASS_POS_BYTE)) + 1;
        BASS.BASS_StreamFree(hTempStream);

        if (mFinish) {
            if (alert.isShowing()) alert.dismiss();
            File file = new File(strPathTo);
            if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            mFinish = false;
            return;
        }

        if(mVideoSavingTask != null && mVideoSavingTask.getStatus() == AsyncTask.Status.RUNNING)
            mVideoSavingTask.cancel(true);
        mVideoSavingTask = new VideoSavingTask(this, strPathTo, alert, nLength);
        mVideoSavingTask.execute(0);
    }

    public void finishSaveSongToGallery2(int nLength, String strMP4Path, AlertDialog alert, String strPathTo)
    {
        if (alert.isShowing()) alert.dismiss();

        if (mFinish) {
            File file = new File(strPathTo);
            if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            mFinish = false;
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DURATION, nLength * 1000);
        values.put("_data", strMP4Path);
        ContentResolver cr = mActivity.getContentResolver();
        cr.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

        AlertDialog.Builder builder;
        if(mActivity.isDarkMode())
            builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
        else
            builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.saveAsVideo);
        builder.setMessage(R.string.saved);
        builder.setPositiveButton("OK", null);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface arg0)
            {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
            }
        });
        alertDialog.show();
    }

    public void play()
    {
        if(MainActivity.sStream == 0) return;
        BASS.BASS_ChannelPlay(MainActivity.sStream, false);
        mActivity.loopFragment.startTimer();
        mActivity.getBtnPlay().setContentDescription(getString(R.string.pause));
        mActivity.getBtnPlay().setImageResource(mActivity.isDarkMode() ? R.drawable.ic_bar_button_pause_dark : R.drawable.ic_bar_button_pause);
        mActivity.getBtnPlayInPlayingBar().setContentDescription(getString(R.string.pause));
        if(mActivity.getSeekCurPos().getVisibility() == View.VISIBLE)
            mActivity.getBtnPlayInPlayingBar().setImageResource(mActivity.isDarkMode() ? R.drawable.ic_playing_large_pause_dark : R.drawable.ic_playing_large_pause);
        else mActivity.getBtnPlayInPlayingBar().setImageResource(mActivity.isDarkMode() ? R.drawable.ic_bar_button_pause_dark : R.drawable.ic_bar_button_pause);
        mSongsAdapter.notifyDataSetChanged();
        mPlaylistsAdapter.notifyDataSetChanged();
        mTabAdapter.notifyDataSetChanged();
        mActivity.startNotification();
    }

    public void pause()
    {
        if(MainActivity.sStream == 0) return;
        BASS.BASS_ChannelPause(MainActivity.sStream);
        mActivity.loopFragment.stopTimer();
        mActivity.getBtnPlay().setContentDescription(getString(R.string.play));
        mActivity.getBtnPlay().setImageResource(mActivity.isDarkMode() ? R.drawable.ic_bar_button_play_dark : R.drawable.ic_bar_button_play);
        mActivity.getBtnPlayInPlayingBar().setContentDescription(getString(R.string.play));
        if(mActivity.getSeekCurPos().getVisibility() == View.VISIBLE)
            mActivity.getBtnPlayInPlayingBar().setImageResource(mActivity.isDarkMode() ? R.drawable.ic_playing_large_play_dark : R.drawable.ic_playing_large_play);
        else mActivity.getBtnPlayInPlayingBar().setImageResource(mActivity.isDarkMode() ? R.drawable.ic_bar_button_play_dark : R.drawable.ic_bar_button_play);
        mSongsAdapter.notifyDataSetChanged();
        mActivity.startNotification();
    }

    public void playPrev()
    {
        mActivity.setWaitEnd(false);
        if(MainActivity.sStream == 0) return;
        mPlaying--;
        if(mPlaying < 0) return;
        playSong(mPlaying, true);
    }

    public void playNext(boolean bPlay) {
        mActivity.setWaitEnd(false);
        int nTempPlaying = mPlaying;
        ArrayList<SongItem> arSongs = mPlaylists.get(mPlayingPlaylist);

        boolean bShuffle = false;
        boolean bSingle = false;
        if(mActivity.getBtnShuffle().getContentDescription().toString().equals(getString(R.string.shuffleOn)))
            bShuffle = true;
        else if(mActivity.getBtnShuffle().getContentDescription().toString().equals(getString(R.string.singleOn)))
            bSingle = true;

        boolean bRepeatAll = false;
        boolean bRepeatSingle = false;
        if(mActivity.getBtnRepeat().getContentDescription().toString().equals(getString(R.string.repeatAllOn)))
            bRepeatAll = true;
        else if(mActivity.getBtnRepeat().getContentDescription().toString().equals(getString(R.string.repeatSingleOn)))
            bRepeatSingle = true;

        if(bSingle) // １曲のみ
        {
            if(!bRepeatSingle) nTempPlaying++;
            if (nTempPlaying >= arSongs.size())
            {
                if(!bRepeatAll)
                {
                    stop();
                    return;
                }
                nTempPlaying = 0;
            }
        }
        else if(bShuffle) // シャッフル
        {
            ArrayList<Integer> arTemp = new ArrayList<>();
            for (int i = 0; i < mPlays.size(); i++) {
                if (i == nTempPlaying) continue;
                boolean bPlayed = mPlays.get(i);
                if (!bPlayed) {
                    arTemp.add(i);
                }
            }
            if (arTemp.size() == 0)
            {
                if(!bRepeatAll)
                {
                    stop();
                    return;
                }
                for (int i = 0; i < mPlays.size(); i++)
                {
                    mPlays.set(i, false);
                }
            }
            if (mPlays.size() >= 1)
            {
                Random random = new Random();
                if (arTemp.size() == 0 || arTemp.size() == mPlays.size())
                {
                    nTempPlaying = random.nextInt(mPlays.size());
                }
                else {
                    int nRandom = random.nextInt(arTemp.size());
                    nTempPlaying = arTemp.get(nRandom);
                }
            }
        }
        else
        {
            nTempPlaying++;
            if (nTempPlaying >= arSongs.size())
            {
                if(!bRepeatAll)
                {
                    stop();
                    return;
                }
                nTempPlaying = 0;
            }
        }
        ArrayList<EffectSaver> arEffectSavers = mEffects.get(mPlayingPlaylist);
        EffectSaver saver = arEffectSavers.get(nTempPlaying);
        if(saver.isSave()) {
            ArrayList<EffectItem> arSavedEffectItems = saver.getEffectItems();
            for(int i = 0; i < arSavedEffectItems.size(); i++) {
                EffectItem item = arSavedEffectItems.get(i);
                if(item.getEffectName().equals(mActivity.effectFragment.getEffectItems().get(EffectFragment.EFFECTTYPE_REVERSE).getEffectName())) {
                    if(mForceNormal) item.setSelected(false);
                    else if(mForceReverse) item.setSelected(true);
                }
            }
        }
        mForceNormal = mForceReverse = false;
        playSong(nTempPlaying, bPlay);
        if(!bPlay) pause();
    }

    public void onPlaylistItemClick(int nPlaylist)
    {
        selectPlaylist(nPlaylist);
        mRelativeSongs.setVisibility(View.VISIBLE);
        mRelativePlaylists.setVisibility(View.INVISIBLE);
        mActivity.getViewSep1().setVisibility(View.INVISIBLE);
    }

    public void onSongItemClick(int nSong)
    {
        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        if(mPlayingPlaylist == mSelectedPlaylist && mPlaying == nSong)
        {
            if(BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PLAYING)
                pause();
            else play();
            return;
        }
        if(mPlayingPlaylist != mSelectedPlaylist) {
            mPlays = new ArrayList<>();
            for(int i = 0; i < arSongs.size(); i++)
                mPlays.add(false);
        }
        mPlayingPlaylist = mSelectedPlaylist;
        playSong(nSong, true);
    }

    private void playSong(int nSong, boolean bPlay)
    {
        mActivity.setWaitEnd(false);
        mActivity.clearLoop(false);

        boolean bReloadLyrics = false;
        if(mRelativeLyrics.getVisibility() == View.VISIBLE && mTextLyrics.getVisibility() == View.VISIBLE && mPlayingPlaylist == mSelectedPlaylist && mPlaying == mSelectedItem) {
            bReloadLyrics = true;
            mSelectedItem = nSong;
        }

        if(mPlayingPlaylist < 0) mPlayingPlaylist = 0;
        else if(mPlayingPlaylist >= mEffects.size()) mPlayingPlaylist = mEffects.size() - 1;
        ArrayList<EffectSaver> arEffectSavers = mEffects.get(mPlayingPlaylist);
        if(0 <= mPlaying && mPlaying < arEffectSavers.size() && 0 <= nSong && nSong < arEffectSavers.size()) {
            EffectSaver saverBefore = arEffectSavers.get(mPlaying);
            EffectSaver saverAfter = arEffectSavers.get(nSong);
            if(saverBefore.isSave() && !saverAfter.isSave()) {
                mActivity.controlFragment.setSpeed(0.0f, false);
                mActivity.controlFragment.setPitch(0.0f, false);
                mActivity.equalizerFragment.setVol(0, false);
                for (int i = 1; i <= 31; i++) {
                    mActivity.equalizerFragment.setEQ(i, 0, false);
                }
                ArrayList<EqualizerItem> arEqualizerItems = mActivity.equalizerFragment.getArEqualizerItems();
                for(int i = 0; i < arEqualizerItems.size(); i++) {
                    EqualizerItem item = arEqualizerItems.get(i);
                    item.setSelected(false);
                }
                mActivity.equalizerFragment.getEqualizersAdapter().notifyDataSetChanged();
                mPlaying = nSong;
                mActivity.effectFragment.resetEffect();
            }
        }
        mPlaying = nSong;
        if(mPlaylists.size() == 0 || mPlayingPlaylist >= mPlaylists.size() || mPlaylists.get(mPlayingPlaylist).size() == 0 || nSong >= mPlaylists.get(mPlayingPlaylist).size())
            return;
        if(nSong < 0) nSong = 0;
        else if(nSong >= mPlaylists.get(mPlayingPlaylist).size()) nSong = mPlaylists.get(mPlayingPlaylist).size() - 1;
        SongItem item = mPlaylists.get(mPlayingPlaylist).get(nSong);
        final String strPath = item.getPath();
        if(MainActivity.sStream != 0)
        {
            BASS.BASS_StreamFree(MainActivity.sStream);
            MainActivity.sStream = 0;
        }
        mPlays.set(nSong, true);

        Uri uri = Uri.parse(strPath);
        if(uri.getScheme() != null && uri.getScheme().equals("content")) {
            ContentResolver cr = mActivity.getApplicationContext().getContentResolver();
            try {
                MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                params.assetFileDescriptor = cr.openAssetFileDescriptor(Uri.parse(strPath), "r");
                if(params.assetFileDescriptor == null) return;
                params.fileChannel = params.assetFileDescriptor.createInputStream().getChannel();
                MainActivity.sStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS.BASS_STREAM_PRESCAN | BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
            } catch (Exception e) {
                removeSong(mPlayingPlaylist, mPlaying);
                if(mPlaying >= mPlaylists.get(mPlayingPlaylist).size())
                    mPlaying = 0;
                if(mPlaylists.get(mPlayingPlaylist).size() != 0)
                    playSong(mPlaying, true);
                return;
            }
        }
        else
            MainActivity.sStream = BASS.BASS_StreamCreateFile(strPath, 0, 0, BASS.BASS_STREAM_PRESCAN | BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
        if(MainActivity.sStream == 0) return;
        long byteLength = BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE);
        mActivity.setByteLength(byteLength);
        double length = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, byteLength);
        mActivity.setLength(length);
        mActivity.getSeekCurPos().setMax((int)length);

        Bitmap bitmap = null;
        if(item.getPathArtwork() != null && !item.getPathArtwork().equals("")) {
            bitmap = BitmapFactory.decodeFile(item.getPathArtwork());
        }
        else {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            try {
                mmr.setDataSource(mActivity.getApplicationContext(), Uri.parse(item.getPath()));
                byte[] data = mmr.getEmbeddedPicture();
                if (data != null) bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                mmr.release();
            }
        }
        if(bitmap != null) mBtnArtworkInPlayingBar.setImageBitmap(bitmap);
        else mBtnArtworkInPlayingBar.setImageResource(mActivity.isDarkMode() ? R.drawable.ic_playing_large_artwork_dark : R.drawable.ic_playing_large_artwork);
        mTextTitleInPlayingBar.setText(item.getTitle());
        if(item.getArtist() == null || item.getArtist().equals(""))
        {
            if(mActivity.isDarkMode()) mTextArtistInPlayingBar.setTextColor(mActivity.getResources().getColor(R.color.darkModeTextDarkGray));
            else mTextArtistInPlayingBar.setTextColor(Color.argb(255, 147, 156, 160));
            mTextArtistInPlayingBar.setText(R.string.unknownArtist);
        }
        else
        {
            mTextArtistInPlayingBar.setTextColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeGray : R.color.lightModeGray));
            mTextArtistInPlayingBar.setText(item.getArtist());
        }

        if(mActivity.getRelativePlayingWithShadow().getVisibility() != View.VISIBLE)
        {
            final RelativeLayout.LayoutParams paramContainer = (RelativeLayout.LayoutParams)mActivity.getViewPager().getLayoutParams();
            final RelativeLayout.LayoutParams paramRecording = (RelativeLayout.LayoutParams)mActivity.getRelativeRecording().getLayoutParams();
            if(MainActivity.sRecord == 0) {
                paramContainer.bottomMargin = (int) (-22 * mActivity.getDensity());
                paramRecording.bottomMargin = 0;
            }
            else {
                paramContainer.bottomMargin = 0;
                paramRecording.bottomMargin = (int) (-22 * mActivity.getDensity());
            }
            mActivity.getRelativePlayingWithShadow().setTranslationY((int) (82 * mActivity.getDensity()));
            mActivity.getRelativePlayingWithShadow().setVisibility(View.VISIBLE);
            mActivity.getRelativePlayingWithShadow().animate()
                    .translationY(0)
                    .setDuration(200)
                    .setListener(new AnimatorListenerAdapter() {
                                     @Override
                                     public void onAnimationEnd(Animator animation) {
                                         super.onAnimationEnd(animation);
                                         mActivity.loopFragment.drawWaveForm(strPath);
                                     }
                                 });
        }
        else mActivity.loopFragment.drawWaveForm(strPath);

        MainActivity.sStream = BASS_FX.BASS_FX_ReverseCreate(MainActivity.sStream, 2, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
        MainActivity.sStream = BASS_FX.BASS_FX_TempoCreate(MainActivity.sStream, BASS_FX.BASS_FX_FREESOURCE);
        int chan = BASS_FX.BASS_FX_TempoGetSource(MainActivity.sStream);
        if(mActivity.effectFragment.isReverse())
            BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_REVERSE);
        else
            BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_FORWARD);
        MainActivity.sFxVol = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_VOLUME, 0);
        int hFx20K = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx16K = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx12_5K = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx10K = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx8K = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx6_3K = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx5K = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx4K = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx3_15K = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx2_5K = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx2K = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx1_6K = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx1_25K = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx1K = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx800 = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx630 = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx500 = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx400 = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx315 = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx250 = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx200 = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx160 = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx125 = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx100 = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx80 = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx63 = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx50 = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx40 = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx31_5 = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx25 = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        int hFx20 = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        mActivity.equalizerFragment.setArHFX(new ArrayList<>(Arrays.asList(hFx20K, hFx16K, hFx12_5K, hFx10K, hFx8K, hFx6_3K, hFx5K, hFx4K, hFx3_15K, hFx2_5K, hFx2K, hFx1_6K, hFx1_25K, hFx1K, hFx800, hFx630, hFx500, hFx400, hFx315, hFx250, hFx200, hFx160, hFx125, hFx100, hFx80, hFx63, hFx50, hFx40, hFx31_5, hFx25, hFx20)));
        if(mPlaying < 0) mPlaying = 0;
        else if(mPlaying >= arEffectSavers.size()) mPlaying = arEffectSavers.size() - 1;
        EffectSaver saver = arEffectSavers.get(mPlaying);
        if(saver.isSave()) restoreEffect();
        BASS.BASS_ChannelSetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO, mActivity.controlFragment.getSpeed());
        BASS.BASS_ChannelSetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO_PITCH, mActivity.controlFragment.getPitch());
        mActivity.equalizerFragment.setEQ();
        mActivity.effectFragment.applyEffect();
        mActivity.setSync();
        if(bPlay) {
            BASS.BASS_ChannelPlay(MainActivity.sStream, false);
            mActivity.loopFragment.startTimer();
        }
        mActivity.getBtnPlay().setContentDescription(getString(R.string.pause));
        mActivity.getBtnPlay().setImageResource(mActivity.isDarkMode() ? R.drawable.ic_bar_button_pause_dark : R.drawable.ic_bar_button_pause);
        mActivity.getBtnPlayInPlayingBar().setContentDescription(getString(R.string.pause));
        if(mActivity.getSeekCurPos().getVisibility() == View.VISIBLE)
            mActivity.getBtnPlayInPlayingBar().setImageResource(mActivity.isDarkMode() ? R.drawable.ic_playing_large_pause_dark : R.drawable.ic_playing_large_pause);
        else mActivity.getBtnPlayInPlayingBar().setImageResource(mActivity.isDarkMode() ? R.drawable.ic_bar_button_pause_dark : R.drawable.ic_bar_button_pause);
        mSongsAdapter.notifyDataSetChanged();
        if(mSelectedPlaylist == mPlayingPlaylist && !mMultiSelecting && !mSorting) mRecyclerSongs.scrollToPosition(mPlaying);
        mPlaylistsAdapter.notifyDataSetChanged();
        mTabAdapter.notifyDataSetChanged();
        if(bReloadLyrics) showLyrics();

        mActivity.startNotification();
    }

    private String getLyrics(int nPlaylist, int nSong) {
        ArrayList<SongItem> arSongs = mPlaylists.get(nPlaylist);
        final SongItem songItem = arSongs.get(nSong);

        try {
            String strPath = getFilePath(mActivity, Uri.parse(songItem.getPath()));
            if(strPath != null) {
                File file = new File(strPath);
                Mp3File mp3file = new Mp3File(file);
                ID3v2 id3v2Tag;
                if (mp3file.hasId3v2Tag()) {
                    id3v2Tag = mp3file.getId3v2Tag();
                    String strLyrics = id3v2Tag.getLyrics();
                    if(file.getParent().equals(mActivity.getExternalCacheDir().toString()))
                        file.deleteOnExit();
                    ArrayList<String> arTempLyrics = mLyrics.get(nPlaylist);
                    arTempLyrics.set(nSong, strLyrics);
                    return strLyrics;
                }
                if(file.getParent().equals(mActivity.getExternalCacheDir().toString()))
                    file.deleteOnExit();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("NewApi")
    private String getFilePath(Context context, Uri uri) {
        String selection = null;
        String[] selectionArgs = null;
        Uri tempUri = uri;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                tempUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    tempUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    tempUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    tempUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(tempUri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor;
            try {
                cursor = context.getContentResolver()
                        .query(tempUri, projection, selection, selectionArgs, null);
                if(cursor != null) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    if (cursor.moveToFirst()) {
                        String strPath = cursor.getString(column_index);
                        cursor.close();
                        return strPath;
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(tempUri.getScheme())) {
            return tempUri.getPath();
        }
        return mActivity.copyTempFile(uri).toString();
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public void stop()
    {
        mActivity.setWaitEnd(false);

        if(MainActivity.sStream == 0) return;

        if(mActivity.getSeekCurPos().getVisibility() == View.VISIBLE)
            mActivity.downViewPlaying(true);
        else {
            mActivity.getRelativePlayingWithShadow().setVisibility(View.VISIBLE);
            mActivity.getRelativePlayingWithShadow().animate()
                    .translationY((int) (82 * mActivity.getDensity()))
                    .setDuration(200)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mActivity.getRelativePlayingWithShadow().setVisibility(View.GONE);
                            final RelativeLayout.LayoutParams paramContainer = (RelativeLayout.LayoutParams) mActivity.getViewPager().getLayoutParams();
                            final RelativeLayout.LayoutParams paramRecording = (RelativeLayout.LayoutParams) mActivity.getRelativeRecording().getLayoutParams();
                            if (MainActivity.sRecord == 0) {
                                paramContainer.bottomMargin = 0;
                                paramRecording.bottomMargin = 0;
                            } else {
                                paramContainer.bottomMargin = 0;
                                paramRecording.bottomMargin = 0;
                            }
                        }
                    });
        }

        mPlaying = -1;
        BASS.BASS_ChannelStop(MainActivity.sStream);
        BASS.BASS_StreamFree(MainActivity.sStream);
        MainActivity.sStream = 0;
        mActivity.loopFragment.stopTimer();
        mActivity.loopFragment.getTextCurValue().setText(getString(R.string.zeroHMS));
        mActivity.getBtnPlay().setContentDescription(getString(R.string.play));
        mActivity.getBtnPlay().setImageResource(mActivity.isDarkMode() ? R.drawable.ic_bar_button_play_dark : R.drawable.ic_bar_button_play);
        mActivity.getBtnPlayInPlayingBar().setContentDescription(getString(R.string.play));
        if(mActivity.getSeekCurPos().getVisibility() == View.VISIBLE)
            mActivity.getBtnPlayInPlayingBar().setImageResource(mActivity.isDarkMode() ? R.drawable.ic_playing_large_play_dark : R.drawable.ic_playing_large_play);
        else mActivity.getBtnPlayInPlayingBar().setImageResource(mActivity.isDarkMode() ? R.drawable.ic_bar_button_play_dark : R.drawable.ic_bar_button_play);
        mActivity.clearLoop();
        mSongsAdapter.notifyDataSetChanged();
        mPlaylistsAdapter.notifyDataSetChanged();
        mTabAdapter.notifyDataSetChanged();

        mActivity.stopNotification();
    }

    public void addSong(MainActivity mActivity, Uri uri)
    {
        if(mSelectedPlaylist < 0) mSelectedPlaylist = 0;
        else if(mSelectedPlaylist >= mPlaylists.size()) mSelectedPlaylist = mPlaylists.size() - 1;
        ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
        String strTitle = null;
        String strArtist = null;
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(mActivity.getApplicationContext(), uri);
            strTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            strArtist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            mmr.release();
        }
        if(strTitle != null) {
            SongItem item = new SongItem(String.format(Locale.getDefault(), "%d", arSongs.size()+1), strTitle, strArtist, uri.toString());
            arSongs.add(item);
        }
        else
        {
            strTitle = getFileNameFromUri(mActivity.getApplicationContext(), uri);
            if(strTitle == null) {
                int startIndex = uri.toString().lastIndexOf('/');
                strTitle = uri.toString().substring(startIndex + 1);
            }
            SongItem item = new SongItem(String.format(Locale.getDefault(), "%d", arSongs.size()+1), strTitle, "", uri.toString());
            arSongs.add(item);
        }
        ArrayList<EffectSaver> arEffectSavers = mEffects.get(mSelectedPlaylist);
        EffectSaver saver = new EffectSaver();
        arEffectSavers.add(saver);

        ArrayList<String> arTempLyrics = mLyrics.get(mSelectedPlaylist);
        arTempLyrics.add(null);

        if(mSelectedPlaylist == mPlayingPlaylist) mPlays.add(false);

        if(mSongsAdapter != null)
            mSongsAdapter.notifyItemInserted(arSongs.size() - 1);
    }

    @SuppressWarnings("deprecation")
    private void addVideo(final MainActivity mActivity, Uri uri)
    {
        if(Build.VERSION.SDK_INT < 18) return;
        ContentResolver cr = mActivity.getApplicationContext().getContentResolver();

        String strPathTo;
        int n = 0;
        File fileForCheck;
        while (true) {
            strPathTo = mActivity.getFilesDir() + "/recorded" + String.format(Locale.getDefault(), "%d", n) + ".mp3";
            fileForCheck = new File(strPathTo);
            if (!fileForCheck.exists()) break;
            n++;
        }
        final File file = new File(strPathTo);

        AssetFileDescriptor afd = null;
        MediaExtractor extractor = null;
        MediaMuxer muxer = null;
        try {
            afd = cr.openAssetFileDescriptor(uri, "r");
            extractor = new MediaExtractor();
            if(afd != null) extractor.setDataSource(afd.getFileDescriptor());
            int trackCount = extractor.getTrackCount();
            muxer = new MediaMuxer(strPathTo, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int audioTrackIndex = 0;
            for (int i = 0; i < trackCount; i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String COMPRESSED_AUDIO_FILE_MIME_TYPE = format.getString(MediaFormat.KEY_MIME);

                if(COMPRESSED_AUDIO_FILE_MIME_TYPE.startsWith("audio/")) {
                    extractor.selectTrack(i);
                    audioTrackIndex = muxer.addTrack(format);
                }
            }
            boolean sawEOS = false;
            int offset = 100;
            ByteBuffer dstBuf = ByteBuffer.allocate(256 * 1024);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            muxer.start();
            while (!sawEOS) {
                bufferInfo.offset = offset;
                bufferInfo.size = extractor.readSampleData(dstBuf, offset);
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    bufferInfo.size = 0;
                }
                else if (bufferInfo.size < 0) {
                    sawEOS = true;
                    bufferInfo.size = 0;
                }
                else {
                    bufferInfo.presentationTimeUs = extractor.getSampleTime();
                    if(Build.VERSION.SDK_INT >= 21)
                        bufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                    else
                        bufferInfo.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
                    dstBuf.position(bufferInfo.offset);
                    dstBuf.limit(bufferInfo.offset + bufferInfo.size);
                    muxer.writeSampleData(audioTrackIndex, dstBuf, bufferInfo);
                    extractor.advance();
                }
            }
            muxer.stop();
        }
        catch(NullPointerException ne) {
            ne.printStackTrace();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if(afd != null) afd.close();
                if(extractor != null) extractor.release();
                if(muxer != null) muxer.release();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        AlertDialog.Builder builder;
        if(mActivity.isDarkMode())
            builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
        else
            builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.addFromVideo);
        LinearLayout linearLayout = new LinearLayout(mActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final ClearableEditText editTitle = new ClearableEditText(mActivity, mActivity.isDarkMode());
        editTitle.setHint(R.string.title);
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        editTitle.setText(String.format(Locale.getDefault(), "ムービー(%s)", df.format(date)));
        final ClearableEditText editArtist = new ClearableEditText(mActivity, mActivity.isDarkMode());
        editArtist.setHint(R.string.artist);
        editArtist.setText("");
        linearLayout.addView(editTitle);
        linearLayout.addView(editArtist);
        builder.setView(linearLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ArrayList<SongItem> arSongs = mPlaylists.get(mSelectedPlaylist);
                SongItem item = new SongItem(String.format(Locale.getDefault(), "%d", arSongs.size()+1), editTitle.getText().toString(), editArtist.getText().toString(), file.getPath());
                arSongs.add(item);
                ArrayList<EffectSaver> arEffectSavers = mEffects.get(mSelectedPlaylist);
                EffectSaver saver = new EffectSaver();
                arEffectSavers.add(saver);
                ArrayList<String> arTempLyrics = mLyrics.get(mSelectedPlaylist);
                arTempLyrics.add(null);
                if(mSelectedPlaylist == mPlayingPlaylist) mPlays.add(false);
                mSongsAdapter.notifyItemInserted(arSongs.size() - 1);

                saveFiles(true, true, true, true, false);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface arg0)
            {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
                editTitle.requestFocus();
                editTitle.setSelection(editTitle.getText().toString().length());
                InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (null != imm) imm.showSoftInput(editTitle, 0);
            }
        });
        alertDialog.show();
    }

    private void removeSong(int nPlaylist, int nSong)
    {
        if(nSong < mPlaying) mPlaying--;

        ArrayList<SongItem> arSongs = mPlaylists.get(nPlaylist);
        SongItem song = arSongs.get(nSong);
        Uri uri = Uri.parse(song.getPath());
        if(!(uri.getScheme() != null && uri.getScheme().equals("content"))) {
            File file = new File(song.getPath());
            if(!file.delete()) System.out.println("ファイルが削除できませんでした");
        }

        arSongs.remove(nSong);
        if(nPlaylist == mPlayingPlaylist) mPlays.remove(nSong);

        for(int i = nSong; i < arSongs.size(); i++) {
            SongItem songItem = arSongs.get(i);
            songItem.setNumber(String.format(Locale.getDefault(), "%d", i+1));
        }

        ArrayList<EffectSaver> arEffectSavers = mEffects.get(nPlaylist);
        arEffectSavers.remove(nSong);

        ArrayList<String> arTempLyrics = mLyrics.get(nPlaylist);
        arTempLyrics.remove(nSong);

        mSongsAdapter.notifyDataSetChanged();

        saveFiles(true, true, true, true, false);
    }

    private String getFileNameFromUri(Context context, Uri uri) {
        if (null == uri) return null;

        String scheme = uri.getScheme();

        String fileName = null;
        if(scheme == null) return null;
        switch (scheme) {
            case "content":
                String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
                Cursor cursor;
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
                String strPath = uri.getPath();
                if(strPath != null) fileName = new File(strPath).getName();
                break;

            default:
                break;
        }
        return fileName;
    }

    public void selectPlaylist(int nSelect)
    {
        if(mRecyclerTab != null) mRecyclerTab.smoothScrollToPosition(nSelect);
        mSelectedPlaylist = nSelect;
        if(mTabAdapter != null) mTabAdapter.notifyDataSetChanged();
        if(mSongsAdapter != null) mSongsAdapter.notifyDataSetChanged();
        if(mPlaylistsAdapter != null) mPlaylistsAdapter.notifyDataSetChanged();
        if(mActivity != null) {
            SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
            preferences.edit().putInt("SelectedPlaylist", mSelectedPlaylist).apply();
        }
    }

    public void updateSongTime(SongItem item)
    {
        String strPath = item.getPath();
        int hTempStream = 0;

        Uri uri = Uri.parse(strPath);
        if(uri.getScheme() != null && uri.getScheme().equals("content")) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            long durationMs = 0;
            try {
                mmr.setDataSource(mActivity.getApplicationContext(), Uri.parse(strPath));
                durationMs = Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                long duration = durationMs / 1000;
                long lMinutes = duration / 60;
                long lSeconds = duration % 60;
                item.setTime(String.format(Locale.getDefault(), "%d:%02d", lMinutes, lSeconds));
                saveFiles(true, false, false, false, false);
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            finally {
                mmr.release();
            }
            if(durationMs == 0) {
                ContentResolver cr = mActivity.getApplicationContext().getContentResolver();
                try {
                    MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                    params.assetFileDescriptor = cr.openAssetFileDescriptor(Uri.parse(strPath), "r");
                    if (params.assetFileDescriptor == null) return;
                    params.fileChannel = params.assetFileDescriptor.createInputStream().getChannel();
                    hTempStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
        else hTempStream = BASS.BASS_StreamCreateFile(strPath, 0, 0, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
        double dLength = BASS.BASS_ChannelBytes2Seconds(hTempStream, BASS.BASS_ChannelGetLength(hTempStream, BASS.BASS_POS_BYTE)) + 0.5;
        int nMinutes = (int)(dLength / 60);
        int nSeconds = (int)(dLength % 60);
        item.setTime(String.format(Locale.getDefault(), "%d:%02d", nMinutes, nSeconds));
        saveFiles(true, false, false, false, false);
    }

    public void saveFiles(boolean bPlaylists, boolean bEffects, boolean bLyrics, boolean bPlaylistNames, boolean bPlayMode)
    {
        SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        if(bPlaylists)
            preferences.edit().putString("arPlaylists", gson.toJson(mPlaylists)).apply();
        if(bEffects)
            preferences.edit().putString("arEffects", gson.toJson(mEffects)).apply();
        if(bLyrics)
            preferences.edit().putString("arLyrics", gson.toJson(mLyrics)).apply();
        if(bPlaylistNames)
            preferences.edit().putString("arPlaylistNames", gson.toJson(mPlaylistNames)).apply();
        if(bPlayMode)
        {
            int nShuffle = 0;
            if(mActivity.getBtnShuffle().getContentDescription().toString().equals(getString(R.string.shuffleOn)))
                nShuffle = 1;
            else if(mActivity.getBtnShuffle().getContentDescription().toString().equals(getString(R.string.singleOn)))
                nShuffle = 2;
            preferences.edit().putInt("shufflemode", nShuffle).apply();
            int nRepeat = 0;
            if(mActivity.getBtnRepeat().getContentDescription().toString().equals(getString(R.string.repeatAllOn)))
                nRepeat = 1;
            else if(mActivity.getBtnRepeat().getContentDescription().toString().equals(getString(R.string.repeatSingleOn)))
                nRepeat = 2;
            preferences.edit().putInt("repeatmode", nRepeat).apply();
        }
    }

    public void setPeak(float fPeak)
    {
        if(mPlayingPlaylist < 0 || mPlayingPlaylist >= mPlaylists.size()) return;
        ArrayList<SongItem> arSongs = mPlaylists.get(mPlayingPlaylist);
        if(mPlaying < 0 || mPlaying >= arSongs.size()) return;
        SongItem song = arSongs.get(mPlaying);
        if(song.getPeak() != fPeak) {
            song.setPeak(fPeak);
            saveFiles(true, false, false, false, false);
            mActivity.effectFragment.setPeak(fPeak);
        }
    }

    public void setLightMode(boolean animated) {
        final RelativeLayout relativePlaylistFragment = mActivity.findViewById(R.id.relativePlaylistFragment);
        final int nLightModeBk = getResources().getColor(R.color.lightModeBk);
        final int nDarkModeBk = getResources().getColor(R.color.darkModeBk);
        final int nLightModeSep = getResources().getColor(R.color.lightModeSep);
        final int nDarkModeSep = getResources().getColor(R.color.darkModeSep);
        final int nLightModeBlue = getResources().getColor(R.color.lightModeBlue);
        final int nDarkModeBlue = getResources().getColor(R.color.darkModeBlue);
        final int nLightModeText = getResources().getColor(android.R.color.black);
        final int nDarkModeText = getResources().getColor(android.R.color.white);
        if(animated) {
            final ArgbEvaluator eval = new ArgbEvaluator();
            ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float fProgress = valueAnimator.getAnimatedFraction();
                    int nColorModeBk = (Integer) eval.evaluate(fProgress, nDarkModeBk, nLightModeBk);
                    int nColorModeSep = (Integer) eval.evaluate(fProgress, nDarkModeSep, nLightModeSep);
                    int nColorModeBlue = (Integer) eval.evaluate(fProgress, nDarkModeBlue, nLightModeBlue);
                    int nColorModeText = (Integer) eval.evaluate(fProgress, nDarkModeText, nLightModeText);
                    relativePlaylistFragment.setBackgroundColor(nColorModeBk);
                    mRelativePlaylists.setBackgroundColor(nColorModeBk);
                    mBtnAddPlaylist_small.setBackgroundColor(nColorModeBk);
                    mRelativeLyricsTitle.setBackgroundColor(nColorModeBk);
                    mDevider1.setBackgroundColor(nColorModeSep);
                    mDevider2.setBackgroundColor(nColorModeSep);
                    mViewSepLyrics.setBackgroundColor(nColorModeSep);
                    mTextPlaylist.setTextColor(nColorModeText);
                    mTextLyricsTitle.setTextColor(nColorModeText);
                    mTextLyrics.setTextColor(nColorModeText);
                    mEditLyrics.setTextColor(nColorModeText);
                    mBtnSortPlaylist.setTextColor(nColorModeBlue);
                    mBtnFinishLyrics.setTextColor(nColorModeBlue);
                    mTextFinishSort.setBackgroundColor(nColorModeBlue);
                    mTextFinishSort.setTextColor(nColorModeBk);
                }
            });

            TransitionDrawable tdBtnAddSong = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.button_big_add_music_dark), getResources().getDrawable(R.drawable.button_big_add_music) });
            TransitionDrawable tdBtnAddPlaylist = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.button_big_add_folder_dark), getResources().getDrawable(R.drawable.button_big_add_folder) });
            TransitionDrawable tdBtnLeft = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.ic_button_arrow_left_dark), getResources().getDrawable(R.drawable.ic_button_arrow_left) });
            TransitionDrawable tdBtnAddPlaylist_small = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.ic_button_plus_dark), getResources().getDrawable(R.drawable.ic_button_plus) });
            TransitionDrawable tdBtnEdit = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.button_big_edit_dark), getResources().getDrawable(R.drawable.button_big_edit) });
            TransitionDrawable tdImgEdit = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.ic_bg_edit_dark), getResources().getDrawable(R.drawable.ic_bg_edit) });

            mBtnAddSong.setImageDrawable(tdBtnAddSong);
            mBtnAddPlaylist.setImageDrawable(tdBtnAddPlaylist);
            mBtnLeft.setImageDrawable(tdBtnLeft);
            mBtnAddPlaylist_small.setImageDrawable(tdBtnAddPlaylist_small);
            mBtnEdit.setImageDrawable(tdBtnEdit);
            mImgEdit.setImageDrawable(tdImgEdit);

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mImgEdit.setImageDrawable(getResources().getDrawable(R.drawable.ic_bg_edit));
                }
            });

            int duration = 300;
            anim.setDuration(duration).start();
            tdBtnAddSong.startTransition(duration);
            tdBtnAddPlaylist.startTransition(duration);
            tdBtnLeft.startTransition(duration);
            tdBtnAddPlaylist_small.startTransition(duration);
            tdBtnEdit.startTransition(duration);
            tdImgEdit.startTransition(duration);
        }
        else {
            relativePlaylistFragment.setBackgroundColor(nLightModeBk);
            mRelativePlaylists.setBackgroundColor(nLightModeBk);
            mBtnAddPlaylist_small.setBackgroundColor(nLightModeBk);
            mRelativeLyricsTitle.setBackgroundColor(nLightModeBk);
            mDevider1.setBackgroundColor(nLightModeSep);
            mDevider2.setBackgroundColor(nLightModeSep);
            mViewSepLyrics.setBackgroundColor(nLightModeSep);
            mTextPlaylist.setTextColor(nLightModeText);
            mTextLyricsTitle.setTextColor(nLightModeText);
            mTextLyrics.setTextColor(nLightModeText);
            mEditLyrics.setTextColor(nLightModeText);
            mBtnSortPlaylist.setTextColor(nLightModeBlue);
            mBtnFinishLyrics.setTextColor(nLightModeBlue);
            mTextFinishSort.setBackgroundColor(nLightModeBlue);
            mTextFinishSort.setTextColor(nLightModeBk);

            mBtnAddSong.setImageDrawable(getResources().getDrawable(R.drawable.button_big_add_music));
            mBtnAddPlaylist.setImageDrawable(getResources().getDrawable(R.drawable.button_big_add_folder));
            mBtnLeft.setImageDrawable(getResources().getDrawable(R.drawable.ic_button_arrow_left));
            mBtnAddPlaylist_small.setImageDrawable(getResources().getDrawable(R.drawable.ic_button_plus));
            mBtnEdit.setImageDrawable(getResources().getDrawable(R.drawable.button_big_edit));
            mImgEdit.setImageDrawable(getResources().getDrawable(R.drawable.ic_bg_edit));
        }
        mSongsAdapter.notifyDataSetChanged();
        mPlaylistsAdapter.notifyDataSetChanged();
        mTabAdapter.notifyDataSetChanged();
    }

    public void setDarkMode(boolean animated) {
        if(mActivity == null) return;
        final RelativeLayout relativePlaylistFragment = mActivity.findViewById(R.id.relativePlaylistFragment);
        final int nLightModeBk = getResources().getColor(R.color.lightModeBk);
        final int nDarkModeBk = getResources().getColor(R.color.darkModeBk);
        final int nLightModeSep = getResources().getColor(R.color.lightModeSep);
        final int nDarkModeSep = getResources().getColor(R.color.darkModeSep);
        final int nLightModeBlue = getResources().getColor(R.color.lightModeBlue);
        final int nDarkModeBlue = getResources().getColor(R.color.darkModeBlue);
        final int nLightModeText = getResources().getColor(android.R.color.black);
        final int nDarkModeText = getResources().getColor(android.R.color.white);
        final ArgbEvaluator eval = new ArgbEvaluator();
        ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float fProgress = valueAnimator.getAnimatedFraction();
            int nColorModeBk = (Integer) eval.evaluate(fProgress, nLightModeBk, nDarkModeBk);
            int nColorModeSep = (Integer) eval.evaluate(fProgress, nLightModeSep, nDarkModeSep);
            int nColorModeBlue = (Integer) eval.evaluate(fProgress, nLightModeBlue, nDarkModeBlue);
            int nColorModeText = (Integer) eval.evaluate(fProgress, nLightModeText, nDarkModeText);
            relativePlaylistFragment.setBackgroundColor(nColorModeBk);
            mRelativePlaylists.setBackgroundColor(nColorModeBk);
            mBtnAddPlaylist_small.setBackgroundColor(nColorModeBk);
            mRelativeLyricsTitle.setBackgroundColor(nColorModeBk);
            mDevider1.setBackgroundColor(nColorModeSep);
            mDevider2.setBackgroundColor(nColorModeSep);
            mViewSepLyrics.setBackgroundColor(nColorModeSep);
            mTextPlaylist.setTextColor(nColorModeText);
            mTextLyricsTitle.setTextColor(nColorModeText);
            mTextLyrics.setTextColor(nColorModeText);
            mEditLyrics.setTextColor(nColorModeText);
            mBtnSortPlaylist.setTextColor(nColorModeBlue);
            mBtnFinishLyrics.setTextColor(nColorModeBlue);
            mTextFinishSort.setBackgroundColor(nColorModeBlue);
            mTextFinishSort.setTextColor(nColorModeBk);
            }
        });

        TransitionDrawable tdBtnAddSong = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.button_big_add_music), getResources().getDrawable(R.drawable.button_big_add_music_dark) });
        TransitionDrawable tdBtnAddPlaylist = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.button_big_add_folder), getResources().getDrawable(R.drawable.button_big_add_folder_dark) });
        TransitionDrawable tdBtnLeft = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.ic_button_arrow_left), getResources().getDrawable(R.drawable.ic_button_arrow_left_dark) });
        TransitionDrawable tdBtnAddPlaylist_small = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.ic_button_plus), getResources().getDrawable(R.drawable.ic_button_plus_dark) });
        TransitionDrawable tdBtnEdit = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.button_big_edit), getResources().getDrawable(R.drawable.button_big_edit_dark) });
        TransitionDrawable tdImgEdit = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.ic_bg_edit), getResources().getDrawable(R.drawable.ic_bg_edit_dark) });

        mBtnAddSong.setImageDrawable(tdBtnAddSong);
        mBtnAddPlaylist.setImageDrawable(tdBtnAddPlaylist);
        mBtnLeft.setImageDrawable(tdBtnLeft);
        mBtnAddPlaylist_small.setImageDrawable(tdBtnAddPlaylist_small);
        mBtnEdit.setImageDrawable(tdBtnEdit);
        mImgEdit.setImageDrawable(tdImgEdit);

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mImgEdit.setImageDrawable(getResources().getDrawable(R.drawable.ic_bg_edit_dark));
            }
        });

        int duration = animated ? 300 : 0;
        anim.setDuration(duration).start();
        tdBtnAddSong.startTransition(duration);
        tdBtnAddPlaylist.startTransition(duration);
        tdBtnLeft.startTransition(duration);
        tdBtnAddPlaylist_small.startTransition(duration);
        tdBtnEdit.startTransition(duration);
        tdImgEdit.startTransition(duration);

        mSongsAdapter.notifyDataSetChanged();
        mPlaylistsAdapter.notifyDataSetChanged();
        mTabAdapter.notifyDataSetChanged();
    }
}