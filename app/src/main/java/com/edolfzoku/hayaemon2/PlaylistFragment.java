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
import java.io.FileNotFoundException;
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
    static MainActivity sActivity;
    static ArrayList<ArrayList<SongItem>> sPlaylists;
    static int sPlayingPlaylist = -1, sPlaying, sSelectedPlaylist, sSelectedItem;
    static boolean sFinish;
    static ArrayList<String> sPlaylistNames;
    static ArrayList<ArrayList<EffectSaver>> sEffects;
    static ArrayList<ArrayList<String>> sLyrics;
    boolean mSorting;
    private boolean mMultiSelecting, mAllowSelectNone;
    private static boolean sForceNormal, sForceReverse;
    private static List<Boolean> sPlays;

    private PlaylistsAdapter mPlaylistsAdapter;
    private PlaylistTabAdapter mTabAdapter;
    private SongsAdapter mSongsAdapter;
    private ItemTouchHelper mPlaylistTouchHelper, mSongTouchHelper;
    private ByteBuffer mRecbuf;
    private SongSavingTask mSongSavingTask;
    private VideoSavingTask mVideoSavingTask;
    private DownloadTask mDownloadTask;
    private ProgressBar mProgress;
    private RecyclerView mRecyclerPlaylists, mRecyclerTab, mRecyclerSongs;
    private Button mBtnSortPlaylist, mBtnFinishLyrics;
    private AnimationButton mBtnAddPlaylist, mBtnArtworkInPlayingBar, mBtnAddSong, mBtnEdit, mBtnCopyInMultipleSelection, mBtnMoveInMultipleSelection, mBtnDeleteInMultipleSelection, mBtnMoreInMultipleSelection;
    private TextView mTextTitleInPlayingBar, mTextArtistInPlayingBar, mTextFinishSort, mTextLyricsTitle, mTextNoLyrics, mTextLyrics, mTextTapEdit, mTextPlaylistInMultipleSelection, mTextPlaylist;
    private RelativeLayout mRelativeSongs, mRelativePlaylists, mRelativeLyrics, mRelativeLyricsTitle;
    private ImageView mImgEdit, mImgSelectAllInMultipleSelection;
    private EditText mEditLyrics;
    private ImageButton mBtnLeft, mBtnAddPlaylist_small;
    private View mDivider1, mDivider2, mViewMultipleSelection, mViewSepLyrics;

    static int getSongCount(int nPlaylist) { return sPlaylists.get(nPlaylist).size(); }
    public void setProgress(int nProgress) { mProgress.setProgress(nProgress); }
    public boolean isSorting() { return mSorting; }
    boolean isMultiSelecting() { return mMultiSelecting; }
    private RelativeLayout getRelativeLyrics() { return mRelativeLyrics; }
    private TextView getTextLyrics() { return mTextLyrics; }
    private AnimationButton getBtnArtworkInPlayingBar() { return mBtnArtworkInPlayingBar; }
    private TextView getTextTitleInPlayingBar() { return mTextTitleInPlayingBar; }
    private TextView getTextArtistInPlayingBar() { return mTextArtistInPlayingBar; }
    private RecyclerView getRecyclerSongs() { return mRecyclerSongs; }
    private PlaylistsAdapter getPlaylistsAdapter() { return mPlaylistsAdapter; }
    private PlaylistTabAdapter getTabAdapter() { return mTabAdapter; }
    ItemTouchHelper getPlaylistTouchHelper() { return mPlaylistTouchHelper; }
    ItemTouchHelper getSongTouchHelper() { return mSongTouchHelper; }
    SongsAdapter getSongsAdapter() { return mSongsAdapter; }
    public static boolean isSelected(int nSong) {
        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        SongItem item = arSongs.get(nSong);
        return item.isSelected();
    }
    static boolean isLock(int nSong) {
        ArrayList<EffectSaver> arEffectSavers = sEffects.get(sSelectedPlaylist);
        EffectSaver saver = arEffectSavers.get(nSong);
        return saver.isSave();
    }

    public PlaylistFragment() {
        if(MainActivity.sStream == 0) sPlaying = -1;
        if(sPlaylistNames == null) sPlaylistNames = new ArrayList<>();
        if(sPlaylists == null) sPlaylists = new ArrayList<>();
        if(sEffects == null) sEffects = new ArrayList<>();
        if(sLyrics == null) sLyrics = new ArrayList<>();
        if(sPlays == null) sPlays = new ArrayList<>();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof MainActivity) sActivity = (MainActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        sActivity = null;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnSortPlaylist) {
            if(mSorting) {
                mRecyclerPlaylists.setPadding(0, 0, 0, (int)(80 * sActivity.getDensity()));
                mBtnAddPlaylist.setVisibility(View.VISIBLE);
                mSorting = false;
                mPlaylistsAdapter.notifyDataSetChanged();
                mBtnSortPlaylist.setText(R.string.sort);
                mPlaylistTouchHelper.attachToRecyclerView(null);
            }
            else {
                mRecyclerPlaylists.setPadding(0, 0, 0, 0);
                mBtnAddPlaylist.setVisibility(View.GONE);
                mSorting = true;
                mPlaylistsAdapter.notifyDataSetChanged();
                mBtnSortPlaylist.setText(R.string.finishSort);

                mPlaylistTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerSongs, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        final int fromPos = viewHolder.getAdapterPosition();
                        final int toPos = target.getAdapterPosition();

                        ArrayList<SongItem> arSongsTemp = sPlaylists.get(fromPos);
                        sPlaylists.remove(fromPos);
                        sPlaylists.add(toPos, arSongsTemp);

                        ArrayList<EffectSaver> arEffectSavers = sEffects.get(fromPos);
                        sEffects.remove(fromPos);
                        sEffects.add(toPos, arEffectSavers);

                        ArrayList<String> arTempLyrics = sLyrics.get(fromPos);
                        sLyrics.remove(fromPos);
                        sLyrics.add(toPos, arTempLyrics);

                        String strTemp = sPlaylistNames.get(fromPos);
                        sPlaylistNames.remove(fromPos);
                        sPlaylistNames.add(toPos, strTemp);

                        if(fromPos == sPlayingPlaylist) sPlayingPlaylist = toPos;
                        else if(fromPos < sPlayingPlaylist && sPlayingPlaylist <= toPos) sPlayingPlaylist--;
                        else if(fromPos > sPlayingPlaylist && sPlayingPlaylist >= toPos) sPlayingPlaylist++;

                        mTabAdapter.notifyItemMoved(fromPos, toPos);
                        mPlaylistsAdapter.notifyItemMoved(fromPos, toPos);

                        return true;
                    }

                    @Override
                    public void clearView(@NonNull RecyclerView recyclerSongs, @NonNull RecyclerView.ViewHolder viewHolder) {
                        super.clearView(recyclerSongs, viewHolder);

                        mTabAdapter.notifyDataSetChanged();
                        mPlaylistsAdapter.notifyDataSetChanged();

                        saveFiles(true, true, true, true, false);
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }

                    @Override
                    public boolean isLongPressDragEnabled() {
                        return false;
                    }
                });
                mPlaylistTouchHelper.attachToRecyclerView(mRecyclerPlaylists);
            }
        }
        else if(v.getId() == R.id.btnAddPlaylist) {
            final Handler handler = new Handler();
            Runnable timer=new Runnable() {
                public void run() {
                    AlertDialog.Builder builder;
                    if(sActivity.isDarkMode())
                        builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
                    else
                        builder = new AlertDialog.Builder(sActivity);
                    builder.setTitle(R.string.addNewList);
                    final ClearableEditText editText = new ClearableEditText(sActivity, sActivity.isDarkMode());
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
                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            if(alertDialog.getWindow() != null) {
                                WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                                lp.dimAmount = 0.4f;
                                alertDialog.getWindow().setAttributes(lp);
                            }
                            editText.requestFocus();
                            editText.setSelection(editText.getText().toString().length());
                            InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (null != imm) imm.showSoftInput(editText, 0);
                        }
                    });
                    alertDialog.show();
                }
            };
            handler.postDelayed(timer, 80);
        }
        else if(v.getId() == R.id.btnRewind) onRewindBtnClick();
        else if(v.getId() == R.id.btnPlay) onPlayBtnClick();
        else if(v.getId() == R.id.btnForward) onForwardBtnClick();
        else if(v.getId() == R.id.btnRecord) {
            if(MainActivity.sRecord != 0) stopRecord();
            else startRecord();
        }
        else if(v.getId() == R.id.btnLeft) {
            mRelativeSongs.setVisibility(View.INVISIBLE);
            mPlaylistsAdapter.notifyDataSetChanged();
            mRelativePlaylists.setVisibility(View.VISIBLE);
            sActivity.getViewSep1().setVisibility(View.VISIBLE);
        }
        else if(v.getId() == R.id.btnAddPlaylist_small) {
            AlertDialog.Builder builder;
            if(sActivity.isDarkMode()) builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
            else builder = new AlertDialog.Builder(sActivity);
            builder.setTitle(R.string.addNewList);
            final ClearableEditText editText = new ClearableEditText(sActivity, sActivity.isDarkMode());
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
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    if(alertDialog.getWindow() != null) {
                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                        lp.dimAmount = 0.4f;
                        alertDialog.getWindow().setAttributes(lp);
                    }
                    editText.requestFocus();
                    editText.setSelection(editText.getText().toString().length());
                    InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (null != imm) imm.showSoftInput(editText, 0);
                }
            });
            alertDialog.show();
        }
        else if(v.getId() == R.id.btnAddSong) {
            final Handler handler = new Handler();
            Runnable timer=new Runnable() {
                public void run() {
                    final BottomMenu menu = new BottomMenu(sActivity);
                    menu.setTitle(getString(R.string.addSong));
                    menu.addMenu(getString(R.string.addFromLocal), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_music_dark : R.drawable.ic_actionsheet_music, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            menu.dismiss();
                            sActivity.open();
                        }
                    });
                    if(Build.VERSION.SDK_INT >= 18) {
                        menu.addMenu(getString(R.string.addFromVideo), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_film_dark : R.drawable.ic_actionsheet_film, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                menu.dismiss();
                                sActivity.openGallery();
                            }
                        });
                    }
                    menu.addMenu(getString(R.string.addURL), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_globe_dark : R.drawable.ic_actionsheet_globe, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            menu.dismiss();

                            AlertDialog.Builder builder;
                            if(sActivity.isDarkMode())
                                builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
                            else
                                builder = new AlertDialog.Builder(sActivity);
                            builder.setTitle(R.string.addURL);
                            LinearLayout linearLayout = new LinearLayout(sActivity);
                            linearLayout.setOrientation(LinearLayout.VERTICAL);
                            final ClearableEditText editURL = new ClearableEditText(sActivity, sActivity.isDarkMode());
                            editURL.setHint(R.string.URL);
                            editURL.setText("");
                            linearLayout.addView(editURL);
                            builder.setView(linearLayout);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    startAddURL(editURL.getText().toString());
                                }
                            });
                            builder.setNegativeButton(R.string.cancel, null);
                            final AlertDialog alertDialog = builder.create();
                            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface arg0) {
                                    if(alertDialog.getWindow() != null) {
                                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                                        lp.dimAmount = 0.4f;
                                        alertDialog.getWindow().setAttributes(lp);
                                    }
                                    editURL.requestFocus();
                                    editURL.setSelection(editURL.getText().toString().length());
                                    InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
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
        else if(v.getId() == R.id.textFinishSort) {
            mRecyclerSongs.setPadding(0, 0, 0, (int)(80 * sActivity.getDensity()));
            mTextFinishSort.setVisibility(View.GONE);
            mBtnAddSong.setVisibility(View.VISIBLE);
            mSorting = false;
            mSongsAdapter.notifyDataSetChanged();

            mSongTouchHelper.attachToRecyclerView(null);
        }
        else if(v.getId() == R.id.btnFinishLyrics) {
            if(mBtnFinishLyrics.getText().toString().equals(getString(R.string.close))) {
                mRelativeSongs.setVisibility(View.VISIBLE);
                mRelativeLyrics.setVisibility(View.INVISIBLE);
                sActivity.getViewSep1().setVisibility(View.INVISIBLE);
            }
            else {
                String strLyrics = mEditLyrics.getText().toString();
                if(sSelectedPlaylist < 0) sSelectedPlaylist = 0;
                else if(sSelectedPlaylist >= sLyrics.size()) sSelectedPlaylist = sLyrics.size() - 1;
                ArrayList<String> arTempLyrics = sLyrics.get(sSelectedPlaylist);
                arTempLyrics.set(sSelectedItem, strLyrics);
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
                InputMethodManager imm = (InputMethodManager)sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm != null) imm.hideSoftInputFromWindow(mEditLyrics.getWindowToken(), 0);

                saveFiles(false, false, true, false, false);
            }
        }
        else if(v.getId() == R.id.btnEdit) {
            final Handler handler = new Handler();
            Runnable timer=new Runnable() {
                public void run() {
                    mTextLyrics.setVisibility(View.INVISIBLE);
                    mBtnFinishLyrics.setText(R.string.done);
                    mBtnEdit.setVisibility(View.INVISIBLE);
                    mEditLyrics.setText(mTextLyrics.getText());
                    mEditLyrics.setVisibility(View.VISIBLE);
                    mEditLyrics.requestFocus();
                    InputMethodManager imm = (InputMethodManager)sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(imm != null) imm.showSoftInput(mEditLyrics, InputMethodManager.SHOW_IMPLICIT);
                    int nPos = mEditLyrics.getText().length();
                    mEditLyrics.setSelection(nPos);
                }
            };
            handler.postDelayed(timer, 80);
        }
        else if(v.getId() == R.id.textNoLyrics) {
            mTextNoLyrics.setVisibility(View.INVISIBLE);
            mImgEdit.setVisibility(View.INVISIBLE);
            mTextTapEdit.setVisibility(View.INVISIBLE);

            mTextLyrics.setVisibility(View.INVISIBLE);
            mBtnFinishLyrics.setText(R.string.done);
            mBtnEdit.setVisibility(View.INVISIBLE);
            mEditLyrics.setText("");
            mEditLyrics.setVisibility(View.VISIBLE);
            mEditLyrics.requestFocus();
            InputMethodManager imm = (InputMethodManager)sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm != null) imm.showSoftInput(mEditLyrics, InputMethodManager.SHOW_IMPLICIT);
        }
        else if(v.getId() == R.id.btnCloseInMultipleSelection) finishMultipleSelection();
        else if(v.getId() == R.id.imgSelectAllInMultipleSelection) selectAllMultipleSelection();
        else if(v.getId() == R.id.btnDeleteInMultipleSelection) deleteMultipleSelection();
        else if(v.getId() == R.id.btnCopyInMultipleSelection) copyMultipleSelection();
        else if(v.getId() == R.id.btnMoveInMultipleSelection) moveMultipleSelection();
        else if(v.getId() == R.id.btnMoreInMultipleSelection) showMenuMultipleSelection();
    }

    @Override
    public boolean onLongClick(View v) {
        if(v.getId() == R.id.btnPlay) {
            final BottomMenu menu = new BottomMenu(sActivity);
            menu.setTitle(getString(R.string.playStop));
            if(MainActivity.sStream == 0 || BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PLAYING || EffectFragment.isReverse()) {
                menu.addMenu(getString(R.string.play), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_play_dark : R.drawable.ic_actionsheet_play, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menu.dismiss();
                        if(EffectFragment.isReverse()) sActivity.effectFragment.onEffectItemClick(EffectFragment.EFFECTTYPE_REVERSE);
                        if(MainActivity.sStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PAUSED)
                            play();
                        else if(MainActivity.sStream == 0 || BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PLAYING) {
                            sForceNormal = true;
                            onPlayBtnClick();
                        }
                    }
                });
            }
            if(MainActivity.sStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PLAYING) {
                menu.addMenu(getString(R.string.pause), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_pause_dark : R.drawable.ic_actionsheet_pause, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menu.dismiss();
                        pause();
                    }
                });
            }
            if(MainActivity.sStream == 0 || BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PLAYING || !EffectFragment.isReverse()) {
                menu.addMenu(getString(R.string.reverse), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_reverse_dark : R.drawable.ic_actionsheet_reverse, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menu.dismiss();
                        if(!EffectFragment.isReverse()) sActivity.effectFragment.onEffectItemClick(EffectFragment.EFFECTTYPE_REVERSE);
                        if(MainActivity.sStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PAUSED)
                            play();
                        else if(MainActivity.sStream == 0 || BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PLAYING) {
                            sForceReverse = true;
                            onPlayBtnClick();
                        }
                    }
                });
            }
            if(MainActivity.sStream != 0) {
                menu.addDestructiveMenu(getString(R.string.stop), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_stop_dark : R.drawable.ic_actionsheet_stop, new View.OnClickListener() {
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

    void onTouchMultipleSelectionItem(final int nItem) {
        ArrayList<SongItem> arSongs = PlaylistFragment.sPlaylists.get(PlaylistFragment.sSelectedPlaylist);
        SongItem item = arSongs.get(nItem);
        item.setSelected(!item.isSelected());
        int nSelected = 0;
        for(int i = 0; i < arSongs.size(); i++) {
            if(arSongs.get(i).isSelected()) nSelected++;
        }
        if(nSelected == 0 && !mAllowSelectNone) finishMultipleSelection();
        else if(nSelected == arSongs.size())
            mImgSelectAllInMultipleSelection.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_button_check_on_dark : R.drawable.ic_button_check_on);
        else mImgSelectAllInMultipleSelection.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_button_check_off_dark : R.drawable.ic_button_check_off);
    }

    void startMultipleSelection(final int nItem) {
        mAllowSelectNone = (nItem == -1);
        mMultiSelecting = true;
        mSorting = false;
        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        for(int i = 0; i < arSongs.size(); i++) arSongs.get(i).setSelected(i == nItem);
        mSongsAdapter.notifyDataSetChanged();

        mTextPlaylistInMultipleSelection.setText(sPlaylistNames.get(sSelectedPlaylist));
        mBtnAddSong.clearAnimation();
        mBtnAddSong.setVisibility(View.GONE);

        int nTabHeight = mRecyclerTab.getHeight();
        int nDuration = 200;
        mRecyclerTab.animate().translationY(-nTabHeight).setDuration(nDuration).start();
        mBtnLeft.animate().translationY(-nTabHeight).setDuration(nDuration).start();
        mBtnAddPlaylist_small.animate().translationY(-nTabHeight).setDuration(nDuration).start();
        mDivider2.animate().translationY(-nTabHeight).setDuration(nDuration).start();
        int nHeight = (int)(66 *  sActivity.getDensity());
        mViewMultipleSelection.setTranslationY(-nHeight);
        mViewMultipleSelection.setVisibility(View.VISIBLE);
        mViewMultipleSelection.animate().setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                sActivity.getViewSep1().setVisibility(View.VISIBLE);
            }
        }).translationY(0).setDuration(nDuration).start();

        startSort();
    }

    void finishMultipleSelection() {
        mMultiSelecting = false;
        mSorting = false;
        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        for(int i = 0; i < arSongs.size(); i++) arSongs.get(i).setSelected(false);
        mSongsAdapter.notifyDataSetChanged();

        sActivity.getViewSep1().setVisibility(View.INVISIBLE);
        mBtnAddSong.setVisibility(View.VISIBLE);

        int nDuration = 200;
        mRecyclerTab.animate().translationY(0).setDuration(nDuration).start();
        mBtnLeft.animate().translationY(0).setDuration(nDuration).start();
        mBtnAddPlaylist_small.animate().translationY(0).setDuration(nDuration).start();
        mDivider2.animate().translationY(0).setDuration(nDuration).start();
        mViewMultipleSelection.animate().setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mViewMultipleSelection.setVisibility(View.GONE);
                mImgSelectAllInMultipleSelection.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_button_check_off_dark : R.drawable.ic_button_check_off);
            }
        }).translationY(-mViewMultipleSelection.getHeight()).setDuration(nDuration).start();
    }

    private void selectAllMultipleSelection() {
        boolean bUnselectSongFounded = false;
        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        for(int i = 0; i < arSongs.size(); i++) {
            SongItem song = arSongs.get(i);
            if(!song.isSelected()) {
                bUnselectSongFounded = true;
                break;
            }
        }

        if(bUnselectSongFounded) {
            mImgSelectAllInMultipleSelection.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_button_check_on_dark : R.drawable.ic_button_check_on);
            for(int i = 0; i < arSongs.size(); i++) {
                SongItem song = arSongs.get(i);
                song.setSelected(true);
            }
        }
        else {
            mImgSelectAllInMultipleSelection.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_button_check_off_dark : R.drawable.ic_button_check_off);
            for(int i = 0; i < arSongs.size(); i++) {
                SongItem song = arSongs.get(i);
                song.setSelected(false);
            }
            if(!mAllowSelectNone) finishMultipleSelection();
        }
        mSongsAdapter.notifyDataSetChanged();
    }

    private void deleteMultipleSelection() {
        AlertDialog.Builder builder;
        if(sActivity.isDarkMode()) builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
        else builder = new AlertDialog.Builder(sActivity);
        builder.setTitle(R.string.delete);
        builder.setMessage(R.string.askDeleteSong);
        builder.setPositiveButton(getString(R.string.decideNot), null);
        builder.setNegativeButton(getString(R.string.doDelete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                boolean bDeletePlaying = false; // 再生中の曲を削除したか
                ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
                for(int i = 0; i < arSongs.size(); i++) {
                    if (arSongs.get(i).isSelected()) {
                        if(sSelectedPlaylist == sPlayingPlaylist && i == sPlaying)
                            bDeletePlaying = true;
                        removeSong(sSelectedPlaylist, i);
                        i--;
                    }
                }

                if(bDeletePlaying) {
                    arSongs = sPlaylists.get(sSelectedPlaylist);
                    if(sPlaying < arSongs.size())
                        playSong(sPlaying, true);
                    else if(sPlaying > 0 && sPlaying == arSongs.size())
                        playSong(sPlaying-1, true);
                    else
                        stop();
                }
                finishMultipleSelection();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
                Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                positiveButton.setTextColor(getResources().getColor(sActivity.isDarkMode() ? R.color.darkModeRed : R.color.lightModeRed));
            }
        });
        alertDialog.show();
    }

    private void copyMultipleSelection() {
        final BottomMenu menu = new BottomMenu(sActivity);
        menu.setTitle(getString(R.string.copy));
        for(int i = 0; i < sPlaylistNames.size(); i++) {
            final int nPlaylistTo = i;
            menu.addMenu(sPlaylistNames.get(i), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_folder_dark : R.drawable.ic_actionsheet_folder, new View.OnClickListener() {
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

    private void copyMultipleSelection(int nPlaylistTo) {
        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        for(int i = 0; i < arSongs.size(); i++) {
            if (arSongs.get(i).isSelected()) copySong(sSelectedPlaylist, i, nPlaylistTo);
        }
        finishMultipleSelection();
    }

    private void moveMultipleSelection() {
        final BottomMenu menu = new BottomMenu(sActivity);
        menu.setTitle(getString(R.string.moveToAnotherPlaylist));
        for(int i = 0; i < sPlaylistNames.size(); i++) {
            if(sSelectedPlaylist == i) continue;
            final int nPlaylistTo = i;
            menu.addMenu(sPlaylistNames.get(i), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_folder_dark : R.drawable.ic_actionsheet_folder, new View.OnClickListener() {
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

    private void moveMultipleSelection(int nPlaylistTo) {
        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        for(int i = 0; i < arSongs.size(); i++) {
            if (arSongs.get(i).isSelected()) {
                moveSong(sSelectedPlaylist, i, nPlaylistTo);
                i--;
            }
        }
        finishMultipleSelection();
    }

    private void showMenuMultipleSelection() {
        boolean bLockFounded = false;
        boolean bUnlockFounded = false;
        boolean bChangeArtworkFounded = false;
        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        ArrayList<EffectSaver> arEffectSavers = sEffects.get(sSelectedPlaylist);
        for(int i = 0; i < arSongs.size(); i++) {
            SongItem song = arSongs.get(i);
            EffectSaver saver = arEffectSavers.get(i);
            if (song.isSelected()) {
                if(song.getPathArtwork() != null && !song.getPathArtwork().equals("")) bChangeArtworkFounded = true;
                if(saver.isSave()) bLockFounded = true;
                else bUnlockFounded = true;
            }
        }

        final BottomMenu menu = new BottomMenu(sActivity);
        menu.setTitle(getString(R.string.selectedSongs));
        menu.addMenu(getString(R.string.changeArtwork), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_image_dark : R.drawable.ic_actionsheet_image, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeArtworkMultipleSelection();
                menu.dismiss();
            }
        });
        if(bChangeArtworkFounded)
            menu.addDestructiveMenu(getString(R.string.resetArtwork), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_initialize_dark : R.drawable.ic_actionsheet_initialize, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resetArtworkMultipleSelection();
                    menu.dismiss();
                }
            });
        if(bUnlockFounded)
            menu.addMenu(getString(R.string.restoreEffect), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_lock_dark : R.drawable.ic_actionsheet_lock, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    restoreEffectMultipleSelection();
                    menu.dismiss();
                }
            });
        if(bLockFounded)
            menu.addMenu(getString(R.string.cancelRestoreEffect), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_unlock_dark : R.drawable.ic_actionsheet_unlock, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cancelRestoreEffectMultipleSelection();
                    menu.dismiss();
                }
            });
        menu.setCancelMenu();
        menu.show();
    }

    private void changeArtworkMultipleSelection() {
        if (Build.VERSION.SDK_INT < 19) {
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 4);
        }
        else {
            final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, 4);
        }
    }

    private void resetArtworkMultipleSelection() {
        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        for(int i = 0; i < arSongs.size(); i++) {
            SongItem song = arSongs.get(i);
            if(song.isSelected()) {
                boolean bFounded = false;
                // 同じアートワークを使っている曲が無いかチェック
                for(int j = 0; j < sPlaylists.size(); j++) {
                    ArrayList<SongItem> arTempSongs = sPlaylists.get(j);
                    for(int k = 0; k < arTempSongs.size(); k++) {
                        if(j == sSelectedPlaylist && k == i) continue;
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
                if(sSelectedPlaylist == sPlayingPlaylist && i == sPlaying) {
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    Bitmap bitmap = null;
                    try {
                        mmr.setDataSource(sActivity, Uri.parse(song.getPath()));
                        byte[] data = mmr.getEmbeddedPicture();
                        if(data != null) bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                    finally {
                        mmr.release();
                    }
                    if(bitmap != null) sActivity.getBtnArtworkInPlayingBar().setImageBitmap(bitmap);
                    else sActivity.getBtnArtworkInPlayingBar().setImageResource(sActivity.isDarkMode() ? R.drawable.ic_playing_large_artwork_dark : R.drawable.ic_playing_large_artwork);
                    sActivity.getBtnArtworkInPlayingBar().setImageBitmap(bitmap);
                }
            }
        }
        saveFiles(true, false, false, false, false);
        finishMultipleSelection();
    }

    private void restoreEffectMultipleSelection() {
        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        for(int i = 0; i < arSongs.size(); i++) {
            SongItem song = arSongs.get(i);
            if (song.isSelected()) {
                sSelectedItem = i;
                setSavingEffect();
                mSongsAdapter.notifyItemChanged(i);
            }
        }
        finishMultipleSelection();
    }

    private void cancelRestoreEffectMultipleSelection() {
        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        for(int i = 0; i < arSongs.size(); i++) {
            SongItem song = arSongs.get(i);
            if (song.isSelected()) {
                sSelectedItem = i;
                cancelSavingEffect();
                mSongsAdapter.notifyItemChanged(i);
            }
        }
        finishMultipleSelection();
    }

    static void onRewindBtnClick() {
        if(MainActivity.sStream == 0) return;
        if(!EffectFragment.isReverse() && BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE)) > MainActivity.sLoopAPos + 1.0)
            BASS.BASS_ChannelSetPosition(MainActivity.sStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, MainActivity.sLoopAPos), BASS.BASS_POS_BYTE);
        else if(EffectFragment.isReverse() && BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE)) < MainActivity.sLoopAPos - 1.0)
            BASS.BASS_ChannelSetPosition(MainActivity.sStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, MainActivity.sLoopBPos), BASS.BASS_POS_BYTE);
        else playPrev();
    }

    static void onForwardBtnClick() {
        if(MainActivity.sStream == 0) return;
        playNext(true);
    }

    static void onPlayBtnClick() {
        if(BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PLAYING) pause();
        else {
            if(BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PAUSED) {
                double dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE));
                double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE));
                if(!EffectFragment.isReverse() && dPos >= dLength - 0.75) {
                    play();
                    MainActivity.onEnded(false);
                }
                else play();
            }
            else {
                if(MainActivity.sStream == 0) {
                    if(sSelectedPlaylist < 0) sSelectedPlaylist = 0;
                    else if(sSelectedPlaylist >= sPlaylists.size()) sSelectedPlaylist = sPlaylists.size() - 1;
                    sPlayingPlaylist = sSelectedPlaylist;
                    ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
                    sPlays = new ArrayList<>();
                    for(int i = 0; i < arSongs.size(); i++)
                        sPlays.add(false);
                    playNext(true);
                }
                else play();
            }
        }
    }

    void startAddURL(String strURL) {
        StatFs sf = new StatFs(sActivity.getFilesDir().toString());
        long nFreeSpace;
        if(Build.VERSION.SDK_INT >= 18)
            nFreeSpace = sf.getAvailableBlocksLong() * sf.getBlockSizeLong();
        else nFreeSpace = (long)sf.getAvailableBlocks() * (long)sf.getBlockSize();
        if(nFreeSpace < 100) {
            AlertDialog.Builder builder;
            if(sActivity.isDarkMode())
                builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
            else builder = new AlertDialog.Builder(sActivity);
            builder.setTitle(R.string.diskFullError);
            builder.setMessage(R.string.diskFullErrorDetail);
            builder.setPositiveButton("OK", null);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
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
        if(sActivity.isDarkMode()) builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
        else builder = new AlertDialog.Builder(sActivity);
        builder.setTitle(R.string.downloading);
        LinearLayout linearLayout = new LinearLayout(sActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        mProgress = new ProgressBar(sActivity, null, android.R.attr.progressBarStyleHorizontal);
        mProgress.setMax(100);
        mProgress.setProgress(0);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        param.topMargin = (int)(24 *  sActivity.getDensity());
        param.leftMargin = (int)(16 *  sActivity.getDensity());
        param.rightMargin = (int)(16 *  sActivity.getDensity());
        linearLayout.addView(mProgress, param);
        builder.setView(linearLayout);

        String strPathTo;
        int i = 0;
        File fileForCheck;
        while (true) {
            strPathTo = sActivity.getFilesDir() + "/recorded" +  String.format(Locale.getDefault(), "%d", i) + ".mp3";
            fileForCheck = new File(strPathTo);
            if (!fileForCheck.exists()) break;
            i++;
        }
        sFinish = false;
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sFinish = true;
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
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
        try {
            mDownloadTask = new DownloadTask(this, new URL(strURL), strPathTo, alertDialog);
            mDownloadTask.execute(0);
        }
        catch (MalformedURLException e) {
            if(alertDialog.isShowing()) alertDialog.dismiss();
        }
    }

    void finishAddURL(String strPathTo, AlertDialog alert, int nError) {
        if(alert.isShowing()) alert.dismiss();

        final File file = new File(strPathTo);
        if(nError == 1) {
            if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            AlertDialog.Builder builder;
            if(sActivity.isDarkMode())
                builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
            else builder = new AlertDialog.Builder(sActivity);
            builder.setTitle(R.string.downloadError);
            builder.setMessage(R.string.downloadErrorDetail);
            builder.setPositiveButton("OK", null);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
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
        if(hTempStream == 0) {
            if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            AlertDialog.Builder builder = new    AlertDialog.Builder(sActivity);
            builder.setTitle(R.string.playableError);
            builder.setMessage(R.string.playableErrorDetail);
            builder.setPositiveButton("OK", null);
            builder.show();
            return;
        }

        AlertDialog.Builder builder;
        if(sActivity.isDarkMode())
            builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
        else builder = new AlertDialog.Builder(sActivity);
        builder.setTitle(R.string.addURL);
        LinearLayout linearLayout = new LinearLayout(sActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final ClearableEditText editTitle = new ClearableEditText(sActivity, sActivity.isDarkMode());
        editTitle.setHint(R.string.title);
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        editTitle.setText(String.format(Locale.getDefault(), "タイトル(%s)", df.format(date)));
        final ClearableEditText editArtist = new ClearableEditText(sActivity, sActivity.isDarkMode());
        editArtist.setHint(R.string.artist);
        editArtist.setText("");
        linearLayout.addView(editTitle);
        linearLayout.addView(editArtist);
        builder.setView(linearLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
                SongItem item = new SongItem(String.format(Locale.getDefault(), "%d", arSongs.size()+1), editTitle.getText().toString(), editArtist.getText().toString(), file.getPath());
                arSongs.add(item);
                ArrayList<EffectSaver> arEffectSavers = sEffects.get(sSelectedPlaylist);
                EffectSaver saver = new EffectSaver();
                arEffectSavers.add(saver);
                ArrayList<String> arTempLyrics = sLyrics.get(sSelectedPlaylist);
                arTempLyrics.add(null);
                if(sSelectedPlaylist == sPlayingPlaylist) sPlays.add(false);
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
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
                editTitle.requestFocus();
                editTitle.setSelection(editTitle.getText().toString().length());
                InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (null != imm) imm.showSoftInput(editTitle, 0);
            }
        });
        alertDialog.show();
    }

    void startRecord() {
        if(MainActivity.sRecord != 0) {
            stopRecord();
            return;
        }
        if(Build.VERSION.SDK_INT >= 23) {
            if (sActivity.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                sActivity.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                return;
            }
        }
        StatFs sf = new StatFs(sActivity.getFilesDir().toString());
        long nFreeSpace;
        if(Build.VERSION.SDK_INT >= 18)
            nFreeSpace = sf.getAvailableBlocksLong() * sf.getBlockSizeLong();
        else nFreeSpace = (long)sf.getAvailableBlocks() * (long)sf.getBlockSize();
        if(nFreeSpace < 100) {
            AlertDialog.Builder builder;
            if(sActivity.isDarkMode())
                builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
            else builder = new AlertDialog.Builder(sActivity);
            builder.setTitle(R.string.diskFullError);
            builder.setMessage(R.string.diskFullErrorDetail);
            builder.setPositiveButton("OK", null);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
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

        sActivity.getBtnStopRecording().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecord();
            }
        });

        final RelativeLayout.LayoutParams paramContainer = (RelativeLayout.LayoutParams)sActivity.getViewPager().getLayoutParams();
        final RelativeLayout.LayoutParams paramRecording = (RelativeLayout.LayoutParams)sActivity.getRelativeRecording().getLayoutParams();
        paramContainer.addRule(RelativeLayout.ABOVE, R.id.relativeRecording);
        paramContainer.bottomMargin = 0;
        if(sActivity.getSeekCurPos().getVisibility() == View.VISIBLE)
            paramRecording.addRule(RelativeLayout.ABOVE, R.id.adView);
        else paramRecording.addRule(RelativeLayout.ABOVE, R.id.relativePlayingWithShadow);
        if(MainActivity.sStream == 0) paramRecording.bottomMargin = 0;
        else {
            if(sActivity.getSeekCurPos().getVisibility() == View.VISIBLE)
                paramRecording.bottomMargin = (int) (60 * sActivity.getDensity());
            else paramRecording.bottomMargin = (int) (-22 * sActivity.getDensity());
        }

        mBtnAddPlaylist.clearAnimation();
        mBtnAddPlaylist.setVisibility(View.INVISIBLE);
        mBtnAddSong.clearAnimation();
        mBtnAddSong.setVisibility(View.INVISIBLE);
        mBtnEdit.clearAnimation();
        mBtnEdit.setVisibility(View.INVISIBLE);
        sActivity.getRelativeRecording().setTranslationY((int)(64 * sActivity.getDensity()));
        sActivity.getRelativeRecording().setVisibility(View.VISIBLE);
        sActivity.getRelativeRecording().animate()
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
                        sActivity.runOnUiThread(new Runnable() {
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

        sActivity.getBtnRecord().setColorFilter(new PorterDuffColorFilter(sActivity.isDarkMode() ? getResources().getColor(R.color.darkModeBlue) : getResources().getColor(R.color.lightModeBlue), PorterDuff.Mode.SRC_IN));

        final Handler handler = new Handler();
        Runnable timer=new Runnable() {
            public void run() {
                if (MainActivity.sRecord == 0) return;
                double dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sRecord, BASS.BASS_ChannelGetPosition(MainActivity.sRecord, BASS.BASS_POS_BYTE));
                int nHour = (int)(dPos / (60 * 60) % 60);
                int nMinute = (int)(dPos / 60 % 60);
                int nSecond = (int)(dPos % 60);
                int nMillisecond = (int)(dPos * 100 % 100);
                sActivity.getTextRecordingTime().setText((nHour < 10 ? "0" : "") + nHour + (nMinute < 10 ? ":0" : ":") + nMinute + (nSecond < 10 ? ":0" : ":") + nSecond + (nMillisecond < 10 ? ".0" : ".") + nMillisecond);
                handler.postDelayed(this, 50);
            }
        };
        handler.postDelayed(timer, 50);
    }

    private void stopRecord() {
        final RelativeLayout.LayoutParams paramContainer = (RelativeLayout.LayoutParams)sActivity.getViewPager().getLayoutParams();
        final RelativeLayout.LayoutParams paramRecording = (RelativeLayout.LayoutParams)sActivity.getRelativeRecording().getLayoutParams();
        paramRecording.bottomMargin = 0;
        if(MainActivity.sStream == 0) paramContainer.bottomMargin = 0;
        else paramContainer.bottomMargin = (int) (-22 * sActivity.getDensity());

        sActivity.getRelativeRecording().setVisibility(View.GONE);
        mBtnAddPlaylist.setVisibility(View.VISIBLE);
        mBtnAddSong.setVisibility(View.VISIBLE);
        mBtnEdit.setVisibility(View.VISIBLE);

        BASS.BASS_ChannelStop(MainActivity.sRecord);
        MainActivity.sRecord = 0;

        sActivity.getBtnRecord().clearColorFilter();

        mRecbuf.limit(mRecbuf.position());
        mRecbuf.putInt(4, mRecbuf.position()-8);
        mRecbuf.putInt(40, mRecbuf.position()-44);
        int i = 0;
        String strPath;
        File fileForCheck;
        while(true) {
            strPath = sActivity.getFilesDir() + "/recorded" + String.format(Locale.getDefault(), "%d", i) + ".wav";
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
        if(sActivity.isDarkMode())
            builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
        else builder = new AlertDialog.Builder(sActivity);
        builder.setTitle(R.string.newRecord);
        LinearLayout linearLayout = new LinearLayout(sActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final ClearableEditText editTitle = new ClearableEditText(sActivity, sActivity.isDarkMode());
        editTitle.setHint(R.string.title);
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        editTitle.setText(String.format(Locale.getDefault(), "%s(%s)", getString(R.string.newRecord), df.format((date))));
        final ClearableEditText editArtist = new ClearableEditText(sActivity, sActivity.isDarkMode());
        editArtist.setHint(R.string.artist);
        editArtist.setText("");
        linearLayout.addView(editTitle);
        linearLayout.addView(editArtist);
        builder.setView(linearLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
                SongItem item = new SongItem(String.format(Locale.getDefault(), "%d", arSongs.size()+1), editTitle.getText().toString(), editArtist.getText().toString(), file.getPath());
                arSongs.add(item);
                ArrayList<EffectSaver> arEffectSavers = sEffects.get(sSelectedPlaylist);
                EffectSaver saver = new EffectSaver();
                arEffectSavers.add(saver);
                ArrayList<String> arTempLyrics = sLyrics.get(sSelectedPlaylist);
                arTempLyrics.add(null);
                if(sSelectedPlaylist == sPlayingPlaylist) sPlays.add(false);
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
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
                editTitle.requestFocus();
                editTitle.setSelection(editTitle.getText().toString().length());
                InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (null != imm) imm.showSoftInput(editTitle, 0);
            }
        });
        alertDialog.show();
    }

    void addPlaylist(String strName) {
        sPlaylistNames.add(strName);
        ArrayList<SongItem> arSongs = new ArrayList<>();
        sPlaylists.add(arSongs);
        ArrayList<EffectSaver> arEffectSavers = new ArrayList<>();
        sEffects.add(arEffectSavers);
        ArrayList<String> arTempLyrics = new ArrayList<>();
        sLyrics.add(arTempLyrics);
        if(sActivity != null)
            saveFiles(true, true, true, true, false);
        selectPlaylist(sPlaylists.size() - 1);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sActivity = (MainActivity)getActivity();
        if(sActivity == null) return;

        mBtnAddPlaylist = sActivity.findViewById(R.id.btnAddPlaylist);
        mRecyclerPlaylists = sActivity.findViewById(R.id.recyclerPlaylists);
        mRecyclerTab = sActivity.findViewById(R.id.recyclerTab);
        mRecyclerSongs = sActivity.findViewById(R.id.recyclerSongs);
        mBtnSortPlaylist = sActivity.findViewById(R.id.btnSortPlaylist);
        mBtnArtworkInPlayingBar = sActivity.findViewById(R.id.btnArtworkInPlayingBar);
        mTextTitleInPlayingBar = sActivity.findViewById(R.id.textTitleInPlayingBar);
        mTextArtistInPlayingBar = sActivity.findViewById(R.id.textArtistInPlayingBar);
        mRelativeSongs = sActivity.findViewById(R.id.relativeSongs);
        mRelativePlaylists = sActivity.findViewById(R.id.relativePlaylists);
        mTextFinishSort = sActivity.findViewById(R.id.textFinishSort);
        mBtnAddSong = sActivity.findViewById(R.id.btnAddSong);
        mTextLyricsTitle = sActivity.findViewById(R.id.textLyricsTitle);
        mTextNoLyrics = sActivity.findViewById(R.id.textNoLyrics);
        mTextLyrics = sActivity.findViewById(R.id.textLyrics);
        mBtnEdit = sActivity.findViewById(R.id.btnEdit);
        mBtnCopyInMultipleSelection = sActivity.findViewById(R.id.btnCopyInMultipleSelection);
        mBtnMoveInMultipleSelection = sActivity.findViewById(R.id.btnMoveInMultipleSelection);
        mBtnDeleteInMultipleSelection = sActivity.findViewById(R.id.btnDeleteInMultipleSelection);
        mBtnMoreInMultipleSelection = sActivity.findViewById(R.id.btnMoreInMultipleSelection);
        mImgEdit = sActivity.findViewById(R.id.imgEdit);
        mTextTapEdit = sActivity.findViewById(R.id.textTapEdit);
        mRelativeLyrics = sActivity.findViewById(R.id.relativeLyrics);
        mRelativeLyricsTitle = sActivity.findViewById(R.id.relativeLyricsTitle);
        mBtnFinishLyrics = sActivity.findViewById(R.id.btnFinishLyrics);
        mEditLyrics = sActivity.findViewById(R.id.editLyrics);
        mImgSelectAllInMultipleSelection = sActivity.findViewById(R.id.imgSelectAllInMultipleSelection);
        mBtnLeft = sActivity.findViewById(R.id.btnLeft);
        mBtnAddPlaylist_small = sActivity.findViewById(R.id.btnAddPlaylist_small);
        mDivider1 = sActivity.findViewById(R.id.devider1);
        mDivider2 = sActivity.findViewById(R.id.devider2);
        mViewMultipleSelection = sActivity.findViewById(R.id.viewMultipleSelection);
        mViewSepLyrics = sActivity.findViewById(R.id.viewSepLyrics);
        mTextPlaylistInMultipleSelection = sActivity.findViewById(R.id.textPlaylistInMultipleSelection);
        mTextPlaylist = sActivity.findViewById(R.id.textPlaylist);
        AnimationButton btnRewind = sActivity.findViewById(R.id.btnRewind);
        AnimationButton btnPlay = sActivity.findViewById(R.id.btnPlay);
        AnimationButton btnForward = sActivity.findViewById(R.id.btnForward);
        AnimationButton btnRecord = sActivity.findViewById(R.id.btnRecord);
        AnimationButton btnCloseInMultipleSelection = sActivity.findViewById(R.id.btnCloseInMultipleSelection);

        mTabAdapter = new PlaylistTabAdapter(sActivity, sPlaylistNames);
        mPlaylistsAdapter = new PlaylistsAdapter(sActivity, sPlaylistNames);
        mSongsAdapter = new SongsAdapter(sActivity);

        mRecyclerPlaylists.setHasFixedSize(false);
        LinearLayoutManager playlistsManager = new LinearLayoutManager(sActivity);
        mRecyclerPlaylists.setLayoutManager(playlistsManager);
        mRecyclerPlaylists.setAdapter(mPlaylistsAdapter);
        if(mRecyclerPlaylists.getItemAnimator() != null)
            ((DefaultItemAnimator) mRecyclerPlaylists.getItemAnimator()).setSupportsChangeAnimations(false);
        mRecyclerPlaylists.setOnClickListener(this);

        mRecyclerTab.setHasFixedSize(false);
        CenterLayoutManager tabManager = new CenterLayoutManager(sActivity);
        tabManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerTab.setLayoutManager(tabManager);
        mRecyclerTab.setAdapter(mTabAdapter);
        if(mRecyclerTab.getItemAnimator() != null)
            ((DefaultItemAnimator) mRecyclerTab.getItemAnimator()).setSupportsChangeAnimations(false);

        mRecyclerSongs.setHasFixedSize(false);
        LinearLayoutManager songsManager = new LinearLayoutManager(sActivity);
        mRecyclerSongs.setLayoutManager(songsManager);
        mRecyclerSongs.setAdapter(mSongsAdapter);
        if(mRecyclerSongs.getItemAnimator() != null)
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
        mBtnDeleteInMultipleSelection.setOnClickListener(this);
        mBtnCopyInMultipleSelection.setOnClickListener(this);
        mBtnMoveInMultipleSelection.setOnClickListener(this);
        mBtnMoreInMultipleSelection.setOnClickListener(this);

        new SwipeHelper(sActivity, mRecyclerSongs) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        getString(R.string.delete),
                        0,
                        Color.parseColor("#FE3B30"),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                mSongsAdapter.notifyDataSetChanged();
                                askDeleteSong(pos);
                            }
                        }
                ));

                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        getString(R.string.lyrics),
                        0,
                        Color.parseColor("#5856D6"),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                mSongsAdapter.notifyDataSetChanged();
                                sSelectedItem = pos;
                                showLyrics();
                            }
                        }
                ));
            }
        };

        new SwipeHelper(sActivity, mRecyclerPlaylists) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        getString(R.string.delete),
                        0,
                        Color.parseColor("#FE3B30"),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                askDeletePlaylist(pos);
                            }
                        }
                ));
            }
        };

        SharedPreferences preferences = sActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        int nSelectedPlaylist = preferences.getInt("SelectedPlaylist", 0);
        selectPlaylist(nSelectedPlaylist);

        if(sActivity.isDarkMode()) setDarkMode(false);

        if(MainActivity.sStream != 0) {
            SongItem item = sPlaylists.get(sPlayingPlaylist).get(sPlaying);
            final String strPath = item.getPath();
            long byteLength = BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE);
            MainActivity.sByteLength = byteLength;
            double length = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, byteLength);
            MainActivity.sLength = length;
            sActivity.getSeekCurPos().setMax((int)length);
            Bitmap bitmap = null;
            if (item.getPathArtwork() != null && item.getPathArtwork().equals("potatoboy"))
                bitmap = BitmapFactory.decodeResource(sActivity.getResources(), R.drawable.potatoboy);
            else if(item.getPathArtwork() != null && !item.getPathArtwork().equals(""))
                bitmap = BitmapFactory.decodeFile(item.getPathArtwork());
            else {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                try {
                    mmr.setDataSource(sActivity.getApplicationContext(), Uri.parse(item.getPath()));
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
            else mBtnArtworkInPlayingBar.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_playing_large_artwork_dark : R.drawable.ic_playing_large_artwork);
            mTextTitleInPlayingBar.setText(item.getTitle());
            if(item.getArtist() == null || item.getArtist().equals("")) {
                if(sActivity.isDarkMode()) mTextArtistInPlayingBar.setTextColor(sActivity.getResources().getColor(R.color.darkModeTextDarkGray));
                else mTextArtistInPlayingBar.setTextColor(Color.argb(255, 147, 156, 160));
                mTextArtistInPlayingBar.setText(R.string.unknownArtist);
            }
            else {
                mTextArtistInPlayingBar.setTextColor(sActivity.getResources().getColor(sActivity.isDarkMode() ? R.color.darkModeGray : R.color.lightModeGray));
                mTextArtistInPlayingBar.setText(item.getArtist());
            }
            if(BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PLAYING) {
                sActivity.getBtnPlay().setContentDescription(sActivity.getString(R.string.pause));
                sActivity.getBtnPlay().setImageResource(sActivity.isDarkMode() ? R.drawable.ic_bar_button_pause_dark : R.drawable.ic_bar_button_pause);
                sActivity.getBtnPlayInPlayingBar().setContentDescription(sActivity.getString(R.string.pause));
                if (sActivity.getSeekCurPos().getVisibility() == View.VISIBLE)
                    sActivity.getBtnPlayInPlayingBar().setImageResource(sActivity.isDarkMode() ? R.drawable.ic_playing_large_pause_dark : R.drawable.ic_playing_large_pause);
                else
                    sActivity.getBtnPlayInPlayingBar().setImageResource(sActivity.isDarkMode() ? R.drawable.ic_bar_button_pause_dark : R.drawable.ic_bar_button_pause);
            }
            final RelativeLayout.LayoutParams paramContainer = (RelativeLayout.LayoutParams)sActivity.getViewPager().getLayoutParams();
            final RelativeLayout.LayoutParams paramRecording = (RelativeLayout.LayoutParams)sActivity.getRelativeRecording().getLayoutParams();
            if(MainActivity.sRecord == 0) {
                paramContainer.bottomMargin = (int) (-22 * sActivity.getDensity());
                paramRecording.bottomMargin = 0;
            }
            else {
                paramContainer.bottomMargin = 0;
                paramRecording.bottomMargin = (int) (-22 * sActivity.getDensity());
            }
            sActivity.getRelativePlayingWithShadow().setTranslationY((int) (82 * sActivity.getDensity()));
            sActivity.getRelativePlayingWithShadow().setVisibility(View.VISIBLE);
            sActivity.getRelativePlayingWithShadow().animate()
                    .translationY(0)
                    .setDuration(200)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            sActivity.loopFragment.drawWaveForm(strPath);
                        }
                    });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1) {
            if(resultCode == RESULT_OK) {
                if(Build.VERSION.SDK_INT < 19) addSong(sActivity, data.getData());
                else {
                    if(data.getClipData() == null) {
                        addSong(sActivity, data.getData());
                        Uri uri = data.getData();
                        if(uri != null) {
                            try {
                                sActivity.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else {
                        for(int i = 0; i < data.getClipData().getItemCount(); i++) {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            addSong(sActivity, uri);
                            try {
                                sActivity.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        else if(requestCode == 2) {
            if(resultCode == RESULT_OK) {
                if(Build.VERSION.SDK_INT < 19) addVideo(sActivity, data.getData());
                else {
                    if(data.getClipData() == null) addVideo(sActivity, data.getData());
                    else {
                        for(int i = 0; i < data.getClipData().getItemCount(); i++) {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            addVideo(sActivity, uri);
                        }
                    }
                }
            }
        }
        else if(requestCode == 3) {
            if(resultCode == RESULT_OK) {
                final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if(Build.VERSION.SDK_INT < 19) setArtwork(data.getData());
                else {
                    if(data.getClipData() == null) {
                        setArtwork(data.getData());
                        Uri uri = data.getData();
                        if(uri != null)
                            sActivity.getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    }
                    else {
                        for(int i = 0; i < data.getClipData().getItemCount(); i++) {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            setArtwork(uri);
                            sActivity.getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        }
                    }
                }
            }
        }
        else if(requestCode == 4) {
            if(resultCode == RESULT_OK) {
                final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if(Build.VERSION.SDK_INT < 19) setArtworkMultipleSelection(data.getData());
                else {
                    if(data.getClipData() == null) {
                        setArtworkMultipleSelection(data.getData());
                        Uri uri = data.getData();
                        if(uri != null)
                            sActivity.getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    }
                    else {
                        for(int i = 0; i < data.getClipData().getItemCount(); i++) {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            setArtworkMultipleSelection(uri);
                            sActivity.getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        }
                    }
                }
            }
        }

        saveFiles(true, true, true, true, false);
    }

    private void setArtwork(Uri uri) {
        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        SongItem song = arSongs.get(sSelectedItem);
        String strPathArtwork = song.getPathArtwork();
        if(strPathArtwork != null) {
            boolean bFounded = false;
            // 同じアートワークを使っている曲が無いかチェック
            for(int j = 0; j < sPlaylists.size(); j++) {
                ArrayList<SongItem> arTempSongs = sPlaylists.get(j);
                for(int k = 0; k < arTempSongs.size(); k++) {
                    if(j == sSelectedPlaylist && k == sSelectedItem) continue;
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
            bitmap = MediaStore.Images.Media.getBitmap(sActivity.getContentResolver(), uri);
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
            strPathTo = sActivity.getFilesDir() + "/artwork" +  String.format(Locale.getDefault(), "%d", i) + ".png";
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

    void resetArtwork() {
        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        SongItem song = arSongs.get(sSelectedItem);
        String strPathArtwork = song.getPathArtwork();
        if(strPathArtwork != null) {
            boolean bFounded = false;
            // 同じアートワークを使っている曲が無いかチェック
            for(int j = 0; j < sPlaylists.size(); j++) {
                ArrayList<SongItem> arTempSongs = sPlaylists.get(j);
                for(int k = 0; k < arTempSongs.size(); k++) {
                    if(j == sSelectedPlaylist && k == sSelectedItem) continue;
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
            mmr.setDataSource(sActivity, Uri.parse(song.getPath()));
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
        else mBtnArtworkInPlayingBar.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_playing_large_artwork_dark : R.drawable.ic_playing_large_artwork);
        saveFiles(true, false, false, false, false);
    }

    private void setArtworkMultipleSelection(Uri uri) {
        Bitmap bitmap;
        int nArtworkSize = getResources().getDisplayMetrics().widthPixels / 2;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(sActivity.getContentResolver(), uri);
        }catch (IOException e) {
            e.printStackTrace();
            return;
        }
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, nArtworkSize, nArtworkSize, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        String strPathTo;
        int i = 0;
        File file;
        while (true) {
            strPathTo = sActivity.getFilesDir() + "/artwork" +  String.format(Locale.getDefault(), "%d", i) + ".png";
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

        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        for(i = 0; i < arSongs.size(); i++) {
            SongItem song = arSongs.get(i);
            if(song.isSelected()) {
                boolean bFounded = false;
                // 同じアートワークを使っている曲が無いかチェック
                for(int j = 0; j < sPlaylists.size(); j++) {
                    ArrayList<SongItem> arTempSongs = sPlaylists.get(j);
                    for(int k = 0; k < arTempSongs.size(); k++) {
                        if(j == sSelectedPlaylist && k == i) continue;
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
                if(sSelectedPlaylist == sPlayingPlaylist && i == sPlaying)
                    mBtnArtworkInPlayingBar.setImageBitmap(bitmap);
            }
        }
        saveFiles(true, false, false, false, false);
        finishMultipleSelection();
    }

    void showSongMenu(final int nItem) {
        sSelectedItem = nItem;
        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        final SongItem songItem = arSongs.get(nItem);
        String strTitle = songItem.getTitle();

        final BottomMenu menu = new BottomMenu(sActivity);
        menu.setTitle(strTitle);
        menu.addMenu(getString(R.string.saveExport), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_save_dark : R.drawable.ic_actionsheet_save, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();

                final BottomMenu menu = new BottomMenu(sActivity);
                menu.setTitle(getString(R.string.saveExport));
                menu.addMenu(getString(R.string.saveToApp), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_save_dark : R.drawable.ic_actionsheet_save, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menu.dismiss();
                        saveSongToLocal();
                    }
                });
                if(Build.VERSION.SDK_INT >= 18) {
                    menu.addMenu(getString(R.string.saveAsVideo), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_film_dark : R.drawable.ic_actionsheet_film, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            menu.dismiss();
                            saveSongToGallery();
                        }
                    });
                }
                menu.addMenu(getString(R.string.export), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_share_dark : R.drawable.ic_actionsheet_share, new View.OnClickListener() {
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
        ArrayList<EffectSaver> arEffectSavers = sEffects.get(sSelectedPlaylist);
        EffectSaver saver = arEffectSavers.get(nItem);
        if(saver.isSave()) {
            menu.addMenu(getString(R.string.cancelRestoreEffect), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_unlock_dark : R.drawable.ic_actionsheet_unlock, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cancelSavingEffect();
                    mSongsAdapter.notifyItemChanged(sSelectedItem);
                    menu.dismiss();
                }
            });
        }
        else {
            menu.addMenu(getString(R.string.restoreEffect), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_lock_dark : R.drawable.ic_actionsheet_lock, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setSavingEffect();
                    mSongsAdapter.notifyItemChanged(sSelectedItem);
                    menu.dismiss();
                }
            });
        }
        menu.addSeparator();
        menu.addMenu(getString(R.string.changeArtwork), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_image_dark : R.drawable.ic_actionsheet_image, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                if (Build.VERSION.SDK_INT < 19) {
                    final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, 3);
                }
                else {
                    final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, 3);
                }
            }
        });
        menu.addMenu(getString(R.string.changeTitleAndArtist), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_edit_dark : R.drawable.ic_actionsheet_edit, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                changeTitleAndArtist(nItem);
            }
        });
        menu.addMenu(getString(R.string.showLyrics), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_file_text_dark : R.drawable.ic_actionsheet_file_text, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                showLyrics();
            }
        });
        menu.addSeparator();
        menu.addMenu(getString(R.string.copy), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_copy_dark : R.drawable.ic_actionsheet_copy, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                final BottomMenu menu = new BottomMenu(sActivity);
                menu.setTitle(getString(R.string.copy));
                for(int i = 0; i < sPlaylistNames.size(); i++) {
                    final int nPlaylistTo = i;
                    menu.addMenu(sPlaylistNames.get(i), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_folder_dark : R.drawable.ic_actionsheet_folder, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            copySong(sSelectedPlaylist, nItem, nPlaylistTo);
                            menu.dismiss();
                        }
                    });
                }
                menu.setCancelMenu();
                menu.show();
            }
        });
        menu.addMenu(getString(R.string.moveToAnotherPlaylist), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_folder_move_dark : R.drawable.ic_actionsheet_folder_move, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();

                final BottomMenu menu = new BottomMenu(sActivity);
                menu.setTitle(getString(R.string.moveToAnotherPlaylist));
                for(int i = 0; i < sPlaylistNames.size(); i++) {
                    if(sSelectedPlaylist == i) continue;
                    final int nPlaylistTo = i;
                    menu.addMenu(sPlaylistNames.get(i), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_folder_dark : R.drawable.ic_actionsheet_folder, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            moveSong(sSelectedPlaylist, nItem, nPlaylistTo);
                            menu.dismiss();
                        }
                    });
                }
                menu.setCancelMenu();
                menu.show();
            }
        });
        menu.addDestructiveMenu(getString(R.string.delete), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_delete_dark : R.drawable.ic_actionsheet_delete, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                askDeleteSong(nItem);
            }
        });
        menu.setCancelMenu();
        menu.show();
    }

    private void askDeletePlaylist(final int item) {
        AlertDialog.Builder builder;
        if(sActivity.isDarkMode())
            builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
        else builder = new AlertDialog.Builder(sActivity);
        builder.setTitle(R.string.deletePlaylist);
        builder.setMessage(R.string.askDeletePlaylist);
        builder.setPositiveButton(getString(R.string.decideNot), null);
        builder.setNegativeButton(getString(R.string.doDelete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(item == sPlayingPlaylist) stop();
                else if(item < sPlayingPlaylist) sPlayingPlaylist--;
                ArrayList<SongItem> arSongs = sPlaylists.get(item);
                for(int i = 0; i < arSongs.size(); i++) {
                    SongItem song = arSongs.get(i);
                    File file = new File(song.getPath());
                    if(file.getParent().equals(sActivity.getFilesDir().toString())) {
                        if(!file.delete()) System.out.println("ファイルが削除できませんでした");
                    }
                }
                sPlaylists.remove(item);
                sEffects.remove(item);
                sPlaylistNames.remove(item);
                sLyrics.remove(item);
                if(sPlaylists.size() == 0)
                    addPlaylist(String.format(Locale.getDefault(), "%s 1", getString(R.string.playlist)));

                int nSelect = item;
                if(nSelect >= sPlaylists.size()) nSelect = sPlaylists.size() - 1;

                selectPlaylist(nSelect);

                saveFiles(true, true, true, true, false);
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
                Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                positiveButton.setTextColor(getResources().getColor(sActivity.isDarkMode() ? R.color.darkModeRed : R.color.lightModeRed));
            }
        });
        alertDialog.show();
    }

    private void askDeleteSong(final int item) {
        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        final SongItem songItem = arSongs.get(item);
        AlertDialog.Builder builder;
        if(sActivity.isDarkMode())
            builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
        else
            builder = new AlertDialog.Builder(sActivity);
        String strTitle = songItem.getTitle();
        builder.setTitle(strTitle);
        builder.setMessage(R.string.askDeleteSong);
        builder.setPositiveButton(getString(R.string.decideNot), null);
        builder.setNegativeButton(getString(R.string.doDelete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                boolean bDeletePlaying = false; // 再生中の曲を削除したか
                if(sSelectedPlaylist == sPlayingPlaylist && item == sPlaying)
                    bDeletePlaying = true;
                removeSong(sSelectedPlaylist, item);
                if(bDeletePlaying) {
                    ArrayList<SongItem> arSongs = sPlaylists.get(sPlayingPlaylist);
                    if(sPlaying < arSongs.size())
                        playSong(sPlaying, true);
                    else if(sPlaying > 0 && sPlaying == arSongs.size())
                        playSong(sPlaying-1, true);
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
                positiveButton.setTextColor(getResources().getColor(sActivity.isDarkMode() ? R.color.darkModeRed : R.color.lightModeRed));
            }
        });
        alertDialog.show();
    }

    private void copySong(int nPlaylistFrom, int nItem, int nPlaylistTo) {
        ArrayList<SongItem> arSongsFrom = sPlaylists.get(nPlaylistFrom);
        ArrayList<SongItem> arSongsTo = sPlaylists.get(nPlaylistTo);
        SongItem itemFrom = arSongsFrom.get(nItem);
        File file = new File(itemFrom.getPath());
        String strPath = itemFrom.getPath();
        if(file.getParent().equals(sActivity.getFilesDir().toString()))
            strPath = sActivity.copyFile(Uri.parse(itemFrom.getPath())).toString();
        SongItem itemTo = new SongItem(String.format(Locale.getDefault(), "%d", arSongsTo.size()+1), itemFrom.getTitle(), itemFrom.getArtist(), strPath);
        arSongsTo.add(itemTo);

        ArrayList<EffectSaver> arEffectSaversFrom = sEffects.get(sSelectedPlaylist);
        ArrayList<EffectSaver> arEffectSaversTo = sEffects.get(nPlaylistTo);
        EffectSaver saverFrom = arEffectSaversFrom.get(nItem);
        if(saverFrom.isSave()) {
            EffectSaver saverTo = new EffectSaver(saverFrom);
            arEffectSaversTo.add(saverTo);
        }
        else {
            EffectSaver saverTo = new EffectSaver();
            arEffectSaversTo.add(saverTo);
        }

        ArrayList<String> arTempLyricsFrom = sLyrics.get(sSelectedPlaylist);
        ArrayList<String> arTempLyricsTo = sLyrics.get(nPlaylistTo);
        String strLyrics = arTempLyricsFrom.get(nItem);
        arTempLyricsTo.add(strLyrics);

        if(nPlaylistTo == sPlayingPlaylist) sPlays.add(false);

        if(sSelectedPlaylist == nPlaylistTo)
            mSongsAdapter.notifyItemInserted(arSongsTo.size() - 1);
        saveFiles(true, true, true, true, false);
    }

    private void moveSong(int nPlaylistFrom, int nItem, int nPlaylistTo) {
        ArrayList<SongItem> arSongsFrom = sPlaylists.get(nPlaylistFrom);
        ArrayList<SongItem> arSongsTo = sPlaylists.get(nPlaylistTo);
        SongItem item = arSongsFrom.get(nItem);
        arSongsTo.add(item);
        item.setNumber(String.format(Locale.getDefault(), "%d", arSongsTo.size()));
        arSongsFrom.remove(nItem);

        ArrayList<EffectSaver> arEffectSaversFrom = sEffects.get(sSelectedPlaylist);
        ArrayList<EffectSaver> arEffectSaversTo = sEffects.get(nPlaylistTo);
        EffectSaver saver = arEffectSaversFrom.get(nItem);
        arEffectSaversTo.add(saver);
        arEffectSaversFrom.remove(nItem);

        ArrayList<String> arTempLyricsFrom = sLyrics.get(sSelectedPlaylist);
        ArrayList<String> arTempLyricsTo = sLyrics.get(nPlaylistTo);
        String strLyrics = arTempLyricsFrom.get(nItem);
        arTempLyricsTo.add(strLyrics);
        arTempLyricsFrom.remove(nItem);

        if(sSelectedPlaylist == sPlayingPlaylist) sPlays.remove(nItem);
        if(nPlaylistTo == sPlayingPlaylist) sPlays.add(false);

        for(int i = nItem; i < arSongsFrom.size(); i++) {
            SongItem songItem = arSongsFrom.get(i);
            songItem.setNumber(String.format(Locale.getDefault(), "%d", i+1));
        }

        if(sSelectedPlaylist == sPlayingPlaylist) {
            if(nItem == sPlaying) {
                sPlayingPlaylist = nPlaylistTo;
                sPlaying = arSongsTo.size() - 1;
                mPlaylistsAdapter.notifyDataSetChanged();
                mTabAdapter.notifyDataSetChanged();
            }
            else if(nItem < sPlaying) sPlaying--;
        }

        mSongsAdapter.notifyDataSetChanged();
        saveFiles(true, true, true, true, false);
    }

    void changeTitleAndArtist(final int nItem) {
        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        final SongItem songItem = arSongs.get(nItem);

        AlertDialog.Builder builder;
        if(sActivity.isDarkMode())
            builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
        else builder = new AlertDialog.Builder(sActivity);
        builder.setTitle(R.string.changeTitleAndArtist);
        LinearLayout linearLayout = new LinearLayout(sActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final ClearableEditText editTitle = new ClearableEditText(sActivity, sActivity.isDarkMode());
        editTitle.setHint(R.string.title);
        editTitle.setText(songItem.getTitle());
        final ClearableEditText editArtist = new ClearableEditText(sActivity, sActivity.isDarkMode());
        editArtist.setHint(R.string.artist);
        editArtist.setText(songItem.getArtist());
        linearLayout.addView(editTitle);
        linearLayout.addView(editArtist);
        builder.setView(linearLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                songItem.setTitle(editTitle.getText().toString());
                songItem.setArtist(editArtist.getText().toString());

                if(sSelectedPlaylist == sPlayingPlaylist && nItem == sPlaying) {
                    mTextTitleInPlayingBar.setText(songItem.getTitle());
                    if(songItem.getArtist() == null || songItem.getArtist().equals("")) {
                        if(sActivity.isDarkMode()) mTextArtistInPlayingBar.setTextColor(sActivity.getResources().getColor(R.color.darkModeTextDarkGray));
                        else mTextArtistInPlayingBar.setTextColor(Color.argb(255, 147, 156, 160));
                        mTextArtistInPlayingBar.setText(R.string.unknownArtist);
                    }
                    else {
                        mTextArtistInPlayingBar.setTextColor(sActivity.getResources().getColor(sActivity.isDarkMode() ? R.color.darkModeGray : R.color.lightModeGray));
                        mTextArtistInPlayingBar.setText(songItem.getArtist());
                    }
                }

                mSongsAdapter.notifyItemChanged(nItem);

                saveFiles(true, true, true, true, false);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
                editTitle.requestFocus();
                editTitle.setSelection(editTitle.getText().toString().length());
                InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (null != imm) imm.showSoftInput(editTitle, 0);
            }
        });
        alertDialog.show();
    }

    void showPlaylistMenu(final int nPosition) {
        selectPlaylist(nPosition);
        String strPlaylist = sPlaylistNames.get(nPosition);

        final BottomMenu menu = new BottomMenu(sActivity);
        menu.setTitle(strPlaylist);
        menu.addMenu(getString(R.string.changePlaylistName), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_edit_dark : R.drawable.ic_actionsheet_edit, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder;
                if(sActivity.isDarkMode())
                    builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
                else
                    builder = new AlertDialog.Builder(sActivity);
                builder.setTitle(R.string.changePlaylistName);
                final ClearableEditText editText = new ClearableEditText(sActivity, sActivity.isDarkMode());
                editText.setHint(R.string.playlist);
                editText.setText(sPlaylistNames.get(nPosition));
                builder.setView(editText);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sPlaylistNames.set(nPosition, editText.getText().toString());

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
                        InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (null != imm) imm.showSoftInput(editText, 0);
                    }
                });
                alertDialog.show();
            }
        });
        menu.addMenu(getString(R.string.copyPlaylist), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_copy_dark : R.drawable.ic_actionsheet_copy, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder;
                if(sActivity.isDarkMode())
                    builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
                else
                    builder = new AlertDialog.Builder(sActivity);
                builder.setTitle(R.string.copyPlaylist);
                final ClearableEditText editText = new ClearableEditText(sActivity, sActivity.isDarkMode());
                editText.setHint(R.string.playlist);
                editText.setText(String.format(Locale.getDefault(), "%s のコピー", sPlaylistNames.get(nPosition)));
                builder.setView(editText);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int nTo = nPosition + 1;
                        sPlaylistNames.add(nTo, editText.getText().toString());
                        ArrayList<SongItem> arSongs = new ArrayList<>();
                        sPlaylists.add(nTo, arSongs);
                        ArrayList<EffectSaver> arEffectSavers = new ArrayList<>();
                        sEffects.add(nTo, arEffectSavers);
                        ArrayList<String> arTempLyrics = new ArrayList<>();
                        sLyrics.add(nTo, arTempLyrics);

                        ArrayList<SongItem> arSongsFrom = sPlaylists.get(nPosition);
                        for(SongItem item : arSongsFrom) {
                            File file = new File(item.getPath());
                            String strPath = item.getPath();
                            if(file.getParent().equals(sActivity.getFilesDir().toString()))
                                strPath = sActivity.copyFile(Uri.parse(item.getPath())).toString();
                            SongItem itemTo = new SongItem(String.format(Locale.getDefault(), "%d", arSongs.size()+1), item.getTitle(), item.getArtist(), strPath);
                            arSongs.add(itemTo);
                        }

                        ArrayList<EffectSaver> arEffectSaversFrom = sEffects.get(nPosition);
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

                        ArrayList<String> lyricsFrom = sLyrics.get(sSelectedPlaylist);
                        arTempLyrics.addAll(lyricsFrom);

                        mTabAdapter.notifyItemInserted(nTo);
                        mPlaylistsAdapter.notifyItemInserted(nTo);
                        selectPlaylist(nTo);
                        if(sActivity != null)
                            saveFiles(true, true, true, true, false);
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        if(alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        editText.requestFocus();
                        editText.setSelection(editText.getText().toString().length());
                        InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (null != imm) imm.showSoftInput(editText, 0);
                    }
                });
                alertDialog.show();
            }
        });
        menu.addDestructiveMenu(getString(R.string.emptyPlaylist), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_folder_erase_dark : R.drawable.ic_actionsheet_folder_erase, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder;
                if(sActivity.isDarkMode())
                    builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
                else
                    builder = new AlertDialog.Builder(sActivity);
                builder.setTitle(R.string.emptyPlaylist);
                builder.setMessage(R.string.askEmptyPlaylist);
                builder.setPositiveButton(getString(R.string.decideNot), null);
                builder.setNegativeButton(getString(R.string.doEmpty), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ArrayList<SongItem> arSongs = sPlaylists.get(nPosition);
                        ArrayList<EffectSaver> arEffectSavers = sEffects.get(nPosition);
                        ArrayList<String> arTempLyrics = sLyrics.get(nPosition);
                        for(int i = 0; i < arSongs.size(); i++) {
                            SongItem song = arSongs.get(i);
                            File file = new File(song.getPath());
                            if(file.getParent() != null && file.getParent().equals(sActivity.getFilesDir().toString())) {
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
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        if(alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        positiveButton.setTextColor(getResources().getColor(sActivity.isDarkMode() ? R.color.darkModeRed : R.color.lightModeRed));
                    }
                });
                alertDialog.show();
            }
        });
        menu.addDestructiveMenu(getString(R.string.deletePlaylist), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_delete_dark : R.drawable.ic_actionsheet_delete, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                askDeletePlaylist(nPosition);
            }
        });
        menu.setCancelMenu();
        menu.show();
    }

    void showPlaylistTabMenu(final int nPosition) {
        selectPlaylist(nPosition);
        String strPlaylist = sPlaylistNames.get(nPosition);
        boolean bLockFounded = false;
        boolean bUnlockFounded = false;
        boolean bChangeArtworkFounded = false;
        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        ArrayList<EffectSaver> arEffectSavers = sEffects.get(sSelectedPlaylist);
        for(int i = 0; i < arSongs.size(); i++) {
            SongItem song = arSongs.get(i);
            song.setSelected(true);
            EffectSaver saver = arEffectSavers.get(i);
            if(song.getPathArtwork() != null && !song.getPathArtwork().equals("")) bChangeArtworkFounded = true;
            if(saver.isSave()) bLockFounded = true;
            else bUnlockFounded = true;
        }

        final BottomMenu menu = new BottomMenu(sActivity);
        menu.setTitle(strPlaylist);
        if(arSongs.size() >= 1)
            menu.addMenu(getString(R.string.selectSongs), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_select_dark : R.drawable.ic_actionsheet_select, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menu.dismiss();
                    startMultipleSelection(-1);
                }
            });
        if(arSongs.size() >= 2)
            menu.addMenu(getString(R.string.sortSongs), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_sort_dark : R.drawable.ic_actionsheet_sort, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menu.dismiss();
                    mRecyclerSongs.setPadding(0, 0, 0, (int) (64 * sActivity.getDensity()));
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
            menu.addMenu(getString(R.string.changeArtwork), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_image_dark : R.drawable.ic_actionsheet_image, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeArtworkMultipleSelection();
                    menu.dismiss();
                }
            });
            if (bChangeArtworkFounded)
                menu.addDestructiveMenu(getString(R.string.resetArtwork), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_initialize_dark : R.drawable.ic_actionsheet_initialize, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        resetArtworkMultipleSelection();
                        menu.dismiss();
                    }
                });
            if (bUnlockFounded)
                menu.addMenu(getString(R.string.restoreEffect), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_lock_dark : R.drawable.ic_actionsheet_lock, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        restoreEffectMultipleSelection();
                        menu.dismiss();
                    }
                });
            if (bLockFounded)
                menu.addMenu(getString(R.string.cancelRestoreEffect), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_unlock_dark : R.drawable.ic_actionsheet_unlock, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cancelRestoreEffectMultipleSelection();
                        menu.dismiss();
                    }
                });
            menu.addSeparator();
        }
        menu.addMenu(getString(R.string.changePlaylistName), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_edit_dark : R.drawable.ic_actionsheet_edit, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder;
                if(sActivity.isDarkMode())
                    builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
                else
                    builder = new AlertDialog.Builder(sActivity);
                builder.setTitle(R.string.changePlaylistName);
                final ClearableEditText editText = new ClearableEditText(sActivity, sActivity.isDarkMode());
                editText.setHint(R.string.playlist);
                editText.setText(sPlaylistNames.get(nPosition));
                builder.setView(editText);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sPlaylistNames.set(nPosition, editText.getText().toString());

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
                        InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (null != imm) imm.showSoftInput(editText, 0);
                    }
                });
                alertDialog.show();
            }
        });
        menu.addMenu(getString(R.string.copyPlaylist), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_copy_dark : R.drawable.ic_actionsheet_copy, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder;
                if(sActivity.isDarkMode())
                    builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
                else
                    builder = new AlertDialog.Builder(sActivity);
                builder.setTitle(R.string.copyPlaylist);
                final ClearableEditText editText = new ClearableEditText(sActivity, sActivity.isDarkMode());
                editText.setHint(R.string.playlist);
                editText.setText(String.format(Locale.getDefault(), "%s のコピー", sPlaylistNames.get(nPosition)));
                builder.setView(editText);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int nTo = nPosition + 1;
                        sPlaylistNames.add(nTo, editText.getText().toString());
                        ArrayList<SongItem> arSongs = new ArrayList<>();
                        sPlaylists.add(nTo, arSongs);
                        ArrayList<EffectSaver> arEffectSavers = new ArrayList<>();
                        sEffects.add(nTo, arEffectSavers);
                        ArrayList<String> arTempLyrics = new ArrayList<>();
                        sLyrics.add(nTo, arTempLyrics);

                        ArrayList<SongItem> arSongsFrom = sPlaylists.get(nPosition);
                        for(SongItem item : arSongsFrom) {
                            File file = new File(item.getPath());
                            String strPath = item.getPath();
                            if(file.getParent().equals(sActivity.getFilesDir().toString()))
                                strPath = sActivity.copyFile(Uri.parse(item.getPath())).toString();
                            SongItem itemTo = new SongItem(String.format(Locale.getDefault(), "%d", arSongs.size()+1), item.getTitle(), item.getArtist(), strPath);
                            arSongs.add(itemTo);
                        }

                        ArrayList<EffectSaver> arEffectSaversFrom = sEffects.get(nPosition);
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

                        ArrayList<String> lyricsFrom = sLyrics.get(sSelectedPlaylist);
                        arTempLyrics.addAll(lyricsFrom);

                        mTabAdapter.notifyItemInserted(nTo);
                        mPlaylistsAdapter.notifyItemInserted(nTo);
                        selectPlaylist(nTo);
                        if(sActivity != null)
                            saveFiles(true, true, true, true, false);
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        if(alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        editText.requestFocus();
                        editText.setSelection(editText.getText().toString().length());
                        InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (null != imm) imm.showSoftInput(editText, 0);
                    }
                });
                alertDialog.show();
            }
        });
        menu.addDestructiveMenu(getString(R.string.emptyPlaylist), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_folder_erase_dark : R.drawable.ic_actionsheet_folder_erase, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder;
                if(sActivity.isDarkMode())
                    builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
                else
                    builder = new AlertDialog.Builder(sActivity);
                builder.setTitle(R.string.emptyPlaylist);
                builder.setMessage(R.string.askEmptyPlaylist);
                builder.setPositiveButton(getString(R.string.decideNot), null);
                builder.setNegativeButton(getString(R.string.doEmpty), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ArrayList<SongItem> arSongs;
                        ArrayList<EffectSaver> arEffectSavers;
                        ArrayList<String> arTempLyrics;
                        arSongs = sPlaylists.get(nPosition);
                        arEffectSavers = sEffects.get(nPosition);
                        arTempLyrics = sLyrics.get(nPosition);
                        for(int i = 0; i < arSongs.size(); i++) {
                            SongItem song = arSongs.get(i);
                            File file = new File(song.getPath());
                            if(file.getParent() != null && file.getParent().equals(sActivity.getFilesDir().toString())) {
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
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        if(alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        positiveButton.setTextColor(getResources().getColor(sActivity.isDarkMode() ? R.color.darkModeRed : R.color.lightModeRed));
                    }
                });
                alertDialog.show();
            }
        });
        menu.addDestructiveMenu(getString(R.string.deletePlaylist), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_delete_dark : R.drawable.ic_actionsheet_delete, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder;
                if(sActivity.isDarkMode())
                    builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
                else
                    builder = new AlertDialog.Builder(sActivity);
                builder.setTitle(R.string.deletePlaylist);
                builder.setMessage(R.string.askDeletePlaylist);
                builder.setPositiveButton(getString(R.string.decideNot), null);
                builder.setNegativeButton(getString(R.string.doDelete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(nPosition == sPlayingPlaylist) stop();
                        else if(nPosition < sPlayingPlaylist) sPlayingPlaylist--;
                        ArrayList<SongItem> arSongs = sPlaylists.get(nPosition);
                        for(int i = 0; i < arSongs.size(); i++) {
                            SongItem song = arSongs.get(i);
                            File file = new File(song.getPath());
                            if(file.getParent().equals(sActivity.getFilesDir().toString())) {
                                if(!file.delete()) System.out.println("ファイルが削除できませんでした");
                            }
                        }
                        sPlaylists.remove(nPosition);
                        sEffects.remove(nPosition);
                        sPlaylistNames.remove(nPosition);
                        sLyrics.remove(nPosition);
                        if(sPlaylists.size() == 0)
                            addPlaylist(String.format(Locale.getDefault(), "%s 1", getString(R.string.playlist)));

                        int nSelect = nPosition;
                        if(nSelect >= sPlaylists.size()) nSelect = sPlaylists.size() - 1;

                        selectPlaylist(nSelect);

                        saveFiles(true, true, true, true, false);
                    }
                });
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        if(alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        positiveButton.setTextColor(getResources().getColor(sActivity.isDarkMode() ? R.color.darkModeRed : R.color.lightModeRed));
                    }
                });
                alertDialog.show();
            }
        });
        menu.setCancelMenu();
        menu.show();
    }

    private void startSort() {
        mSongTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerSongs, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                final int fromPos = viewHolder.getAdapterPosition();
                final int toPos = target.getAdapterPosition();

                ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
                SongItem itemTemp = arSongs.get(fromPos);
                arSongs.remove(fromPos);
                arSongs.add(toPos, itemTemp);

                ArrayList<EffectSaver> arEffectSavers = sEffects.get(sSelectedPlaylist);
                EffectSaver saver = arEffectSavers.get(fromPos);
                arEffectSavers.remove(fromPos);
                arEffectSavers.add(toPos, saver);

                ArrayList<String> arTempLyrics = sLyrics.get(sSelectedPlaylist);
                String strLyrics = arTempLyrics.get(fromPos);
                arTempLyrics.remove(fromPos);
                arTempLyrics.add(toPos, strLyrics);

                if (sPlayingPlaylist == sSelectedPlaylist) {
                    Boolean bTemp = sPlays.get(fromPos);
                    sPlays.remove(fromPos);
                    sPlays.add(toPos, bTemp);
                }

                int nStart = fromPos < toPos ? fromPos : toPos;
                for (int i = nStart; i < arSongs.size(); i++) {
                    SongItem songItem = arSongs.get(i);
                    songItem.setNumber(String.format(Locale.getDefault(), "%d", i + 1));
                }

                if (fromPos == sPlaying) sPlaying = toPos;
                else if (fromPos < sPlaying && sPlaying <= toPos) sPlaying--;
                else if (fromPos > sPlaying && sPlaying >= toPos) sPlaying++;

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
            public void clearView(@NonNull RecyclerView recyclerSongs, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerSongs, viewHolder);

                saveFiles(true, true, true, true, false);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }
        });
        mSongTouchHelper.attachToRecyclerView(mRecyclerSongs);
    }

    void showLyrics() {
        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        SongItem songItem = arSongs.get(sSelectedItem);

        ArrayList<String> arTempLyrics = sLyrics.get(sSelectedPlaylist);
        String strLyrics = arTempLyrics.get(sSelectedItem);

        String strTitle = songItem.getTitle();
        if(songItem.getArtist() != null && !songItem.getArtist().equals(""))
            strTitle += " - " + songItem.getArtist();
        mTextLyricsTitle.setText(strTitle);

        if(strLyrics == null || strLyrics.equals(""))
            strLyrics = getLyrics(sSelectedPlaylist, sSelectedItem);
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
        sActivity.getViewSep1().setVisibility(View.VISIBLE);
    }

    static void setSavingEffect() {
        ArrayList<EffectSaver> arEffectSavers = sEffects.get(sSelectedPlaylist);
        EffectSaver saver = arEffectSavers.get(sSelectedItem);
        saver.setSave(true);
        saver.setSpeed(ControlFragment.sSpeed);
        saver.setPitch(ControlFragment.sPitch);
        saver.setVol(EqualizerFragment.sVol);
        saver.setEQ20K(EqualizerFragment.sEQs[0]);
        saver.setEQ16K(EqualizerFragment.sEQs[1]);
        saver.setEQ12_5K(EqualizerFragment.sEQs[2]);
        saver.setEQ10K(EqualizerFragment.sEQs[3]);
        saver.setEQ8K(EqualizerFragment.sEQs[4]);
        saver.setEQ6_3K(EqualizerFragment.sEQs[5]);
        saver.setEQ5K(EqualizerFragment.sEQs[6]);
        saver.setEQ4K(EqualizerFragment.sEQs[7]);
        saver.setEQ3_15K(EqualizerFragment.sEQs[8]);
        saver.setEQ2_5K(EqualizerFragment.sEQs[9]);
        saver.setEQ2K(EqualizerFragment.sEQs[10]);
        saver.setEQ1_6K(EqualizerFragment.sEQs[11]);
        saver.setEQ1_25K(EqualizerFragment.sEQs[12]);
        saver.setEQ1K(EqualizerFragment.sEQs[13]);
        saver.setEQ800(EqualizerFragment.sEQs[14]);
        saver.setEQ630(EqualizerFragment.sEQs[15]);
        saver.setEQ500(EqualizerFragment.sEQs[16]);
        saver.setEQ400(EqualizerFragment.sEQs[17]);
        saver.setEQ315(EqualizerFragment.sEQs[18]);
        saver.setEQ250(EqualizerFragment.sEQs[19]);
        saver.setEQ200(EqualizerFragment.sEQs[20]);
        saver.setEQ160(EqualizerFragment.sEQs[21]);
        saver.setEQ125(EqualizerFragment.sEQs[22]);
        saver.setEQ100(EqualizerFragment.sEQs[23]);
        saver.setEQ80(EqualizerFragment.sEQs[24]);
        saver.setEQ63(EqualizerFragment.sEQs[25]);
        saver.setEQ50(EqualizerFragment.sEQs[26]);
        saver.setEQ40(EqualizerFragment.sEQs[27]);
        saver.setEQ31_5(EqualizerFragment.sEQs[28]);
        saver.setEQ25(EqualizerFragment.sEQs[29]);
        saver.setEQ20(EqualizerFragment.sEQs[30]);
        saver.setEffectItems(EffectFragment.sEffectItems);
        saver.setPan(EffectFragment.sPan);
        saver.setFreq(EffectFragment.sFreq);
        saver.setBPM(EffectFragment.sBpm);
        saver.setSoundEffectVolume(EffectFragment.sSoundEffectVolume);
        saver.setTimeOmIncreaseSpeed(EffectFragment.sTimeOfIncreaseSpeed);
        saver.setIncreaseSpeed(EffectFragment.sIncreaseSpeed);
        saver.setTimeOmDecreaseSpeed(EffectFragment.sTimeOfDecreaseSpeed);
        saver.setDecreaseSpeed(EffectFragment.sDecreaseSpeed);
        saver.setCompGain(EffectFragment.sCompGain);
        saver.setCompThreshold(EffectFragment.sCompThreshold);
        saver.setCompRatio(EffectFragment.sCompRatio);
        saver.setCompAttack(EffectFragment.sCompRatio);
        saver.setCompRelease(EffectFragment.sCompRelease);
        saver.setEchoDry(EffectFragment.sEchoDry);
        saver.setEchoWet(EffectFragment.sEchoWet);
        saver.setEchoFeedback(EffectFragment.sEchoFeedback);
        saver.setEchoDelay(EffectFragment.sEchoDelay);
        saver.setReverbDry(EffectFragment.sReverbDry);
        saver.setReverbWet(EffectFragment.sReverbWet);
        saver.setReverbRoomSize(EffectFragment.sReverbRoomSize);
        saver.setReverbDamp(EffectFragment.sReverbDamp);
        saver.setReverbWidth(EffectFragment.sReverbWidth);
        saver.setChorusDry(EffectFragment.sChorusDry);
        saver.setChorusWet(EffectFragment.sChorusWet);
        saver.setChorusFeedback(EffectFragment.sChorusFeedback);
        saver.setChorusMinSweep(EffectFragment.sChorusMinSweep);
        saver.setChorusMaxSweep(EffectFragment.sChorusMaxSweep);
        saver.setChorusRate(EffectFragment.sChorusRate);
        saver.setDistortionDrive(EffectFragment.sDistortionDrive);
        saver.setDistortionDry(EffectFragment.sDistortionDry);
        saver.setDistortionWet(EffectFragment.sDistortionWet);
        saver.setDistortionFeedback(EffectFragment.sDistortionFeedback);
        saver.setDistortionVolume(EffectFragment.sDistortionVolume);
        if(sSelectedPlaylist == sPlayingPlaylist && sSelectedItem == sPlaying) {
            if(LoopFragment.sABLoop) saver.setIsABLoop(true);
            else saver.setIsABLoop(false);
            saver.setIsLoopA(MainActivity.sLoopA);
            saver.setLoopA(MainActivity.sLoopAPos);
            saver.setIsLoopB(MainActivity.sLoopB);
            saver.setLoopB(MainActivity.sLoopBPos);
            saver.setArMarkerTime(LoopFragment.sMarkerTimes);
            saver.setIsLoopMarker(LoopFragment.sMarkerPlay);
            saver.setMarker(LoopFragment.sMarker);
        }
        saver.setReverbSelected(EffectFragment.sReverbSelected);
        saver.setEchoSelected(EffectFragment.sEchoSelected);
        saver.setChorusSelected(EffectFragment.sChorusSelected);
        saver.setDistortionSelected(EffectFragment.sDistortionSelected);
        saver.setCompSelected(EffectFragment.sCompSelected);
        saver.setSoundEffectSelected(EffectFragment.sSoundEffectSelected);

        saveFiles(false, true, false, false, false);
    }

    private void cancelSavingEffect() {
        ArrayList<EffectSaver> arEffectSavers = sEffects.get(sSelectedPlaylist);
        EffectSaver saver = arEffectSavers.get(sSelectedItem);
        saver.setSave(false);

        saveFiles(false, true, false, false, false);
    }

    public static void updateSavingEffect() {
        if(MainActivity.sStream == 0 || sPlaying == -1) return;
        ArrayList<EffectSaver> arEffectSavers = sEffects.get(sPlayingPlaylist);
        EffectSaver saver = arEffectSavers.get(sPlaying);
        if(saver.isSave()) {
            saver.setSpeed(ControlFragment.sSpeed);
            saver.setPitch(ControlFragment.sPitch);
            saver.setVol(EqualizerFragment.sVol);
            saver.setEQ20K(EqualizerFragment.sEQs[0]);
            saver.setEQ16K(EqualizerFragment.sEQs[1]);
            saver.setEQ12_5K(EqualizerFragment.sEQs[2]);
            saver.setEQ10K(EqualizerFragment.sEQs[3]);
            saver.setEQ8K(EqualizerFragment.sEQs[4]);
            saver.setEQ6_3K(EqualizerFragment.sEQs[5]);
            saver.setEQ5K(EqualizerFragment.sEQs[6]);
            saver.setEQ4K(EqualizerFragment.sEQs[7]);
            saver.setEQ3_15K(EqualizerFragment.sEQs[8]);
            saver.setEQ2_5K(EqualizerFragment.sEQs[9]);
            saver.setEQ2K(EqualizerFragment.sEQs[10]);
            saver.setEQ1_6K(EqualizerFragment.sEQs[11]);
            saver.setEQ1_25K(EqualizerFragment.sEQs[12]);
            saver.setEQ1K(EqualizerFragment.sEQs[13]);
            saver.setEQ800(EqualizerFragment.sEQs[14]);
            saver.setEQ630(EqualizerFragment.sEQs[15]);
            saver.setEQ500(EqualizerFragment.sEQs[16]);
            saver.setEQ400(EqualizerFragment.sEQs[17]);
            saver.setEQ315(EqualizerFragment.sEQs[18]);
            saver.setEQ250(EqualizerFragment.sEQs[19]);
            saver.setEQ200(EqualizerFragment.sEQs[20]);
            saver.setEQ160(EqualizerFragment.sEQs[21]);
            saver.setEQ125(EqualizerFragment.sEQs[22]);
            saver.setEQ100(EqualizerFragment.sEQs[23]);
            saver.setEQ80(EqualizerFragment.sEQs[24]);
            saver.setEQ63(EqualizerFragment.sEQs[25]);
            saver.setEQ50(EqualizerFragment.sEQs[26]);
            saver.setEQ40(EqualizerFragment.sEQs[27]);
            saver.setEQ31_5(EqualizerFragment.sEQs[28]);
            saver.setEQ25(EqualizerFragment.sEQs[29]);
            saver.setEQ20(EqualizerFragment.sEQs[30]);
            saver.setEffectItems(EffectFragment.sEffectItems);
            saver.setPan(EffectFragment.sPan);
            saver.setFreq(EffectFragment.sFreq);
            saver.setBPM(EffectFragment.sBpm);
            saver.setSoundEffectVolume(EffectFragment.sSoundEffectVolume);
            saver.setTimeOmIncreaseSpeed(EffectFragment.sTimeOfIncreaseSpeed);
            saver.setIncreaseSpeed(EffectFragment.sIncreaseSpeed);
            saver.setTimeOmDecreaseSpeed(EffectFragment.sTimeOfDecreaseSpeed);
            saver.setDecreaseSpeed(EffectFragment.sDecreaseSpeed);
            saver.setCompGain(EffectFragment.sCompGain);
            saver.setCompThreshold(EffectFragment.sCompThreshold);
            saver.setCompRatio(EffectFragment.sCompRatio);
            saver.setCompAttack(EffectFragment.sCompAttack);
            saver.setCompRelease(EffectFragment.sCompRelease);
            saver.setEchoDry(EffectFragment.sEchoDry);
            saver.setEchoWet(EffectFragment.sEchoWet);
            saver.setEchoFeedback(EffectFragment.sEchoFeedback);
            saver.setEchoDelay(EffectFragment.sEchoDelay);
            saver.setReverbDry(EffectFragment.sReverbDry);
            saver.setReverbWet(EffectFragment.sReverbWet);
            saver.setReverbRoomSize(EffectFragment.sReverbRoomSize);
            saver.setReverbDamp(EffectFragment.sReverbDamp);
            saver.setReverbWidth(EffectFragment.sReverbWidth);
            saver.setChorusDry(EffectFragment.sChorusDry);
            saver.setChorusWet(EffectFragment.sChorusWet);
            saver.setChorusFeedback(EffectFragment.sChorusFeedback);
            saver.setChorusMinSweep(EffectFragment.sChorusMinSweep);
            saver.setChorusMaxSweep(EffectFragment.sChorusMaxSweep);
            saver.setChorusRate(EffectFragment.sChorusRate);
            saver.setDistortionDrive(EffectFragment.sDistortionDrive);
            saver.setDistortionDry(EffectFragment.sDistortionDry);
            saver.setDistortionWet(EffectFragment.sDistortionWet);
            saver.setDistortionFeedback(EffectFragment.sDistortionFeedback);
            saver.setDistortionVolume(EffectFragment.sDistortionVolume);
            saver.setIsABLoop(LoopFragment.sABLoop);
            saver.setIsLoopA(MainActivity.sLoopA);
            saver.setLoopA(MainActivity.sLoopAPos);
            saver.setIsLoopB(MainActivity.sLoopB);
            saver.setLoopB(MainActivity.sLoopBPos);
            saver.setArMarkerTime(LoopFragment.sMarkerTimes);
            saver.setIsLoopMarker(LoopFragment.sMarkerPlay);
            saver.setMarker(LoopFragment.sMarker);
            saver.setReverbSelected(EffectFragment.sReverbSelected);
            saver.setEchoSelected(EffectFragment.sEchoSelected);
            saver.setChorusSelected(EffectFragment.sChorusSelected);
            saver.setDistortionSelected(EffectFragment.sDistortionSelected);
            saver.setCompSelected(EffectFragment.sCompSelected);
            saver.setSoundEffectSelected(EffectFragment.sSoundEffectSelected);

            saveFiles(false, true, false, false, false);
        }
    }

    private static void restoreEffect() {
        ArrayList<EffectSaver> arEffectSavers = sEffects.get(sPlayingPlaylist);
        EffectSaver saver = arEffectSavers.get(sPlaying);
        ControlFragment.setSpeed(saver.getSpeed(), false);
        ControlFragment.setPitch(saver.getPitch(), false);
        EqualizerFragment.setVol(saver.getVol(), false);
        EqualizerFragment.setEQ(1, saver.getEQ20K(), false);
        EqualizerFragment.setEQ(2, saver.getEQ16K(), false);
        EqualizerFragment.setEQ(3, saver.getEQ12_5K(), false);
        EqualizerFragment.setEQ(4, saver.getEQ10K(), false);
        EqualizerFragment.setEQ(5, saver.getEQ8K(), false);
        EqualizerFragment.setEQ(6, saver.getEQ6_3K(), false);
        EqualizerFragment.setEQ(7, saver.getEQ5K(), false);
        EqualizerFragment.setEQ(8, saver.getEQ4K(), false);
        EqualizerFragment.setEQ(9, saver.getEQ3_15K(), false);
        EqualizerFragment.setEQ(10, saver.getEQ2_5K(), false);
        EqualizerFragment.setEQ(11, saver.getEQ2K(), false);
        EqualizerFragment.setEQ(12, saver.getEQ1_6K(), false);
        EqualizerFragment.setEQ(13, saver.getEQ1_25K(), false);
        EqualizerFragment.setEQ(14, saver.getEQ1K(), false);
        EqualizerFragment.setEQ(15, saver.getEQ800(), false);
        EqualizerFragment.setEQ(16, saver.getEQ630(), false);
        EqualizerFragment.setEQ(17, saver.getEQ500(), false);
        EqualizerFragment.setEQ(18, saver.getEQ400(), false);
        EqualizerFragment.setEQ(19, saver.getEQ315(), false);
        EqualizerFragment.setEQ(20, saver.getEQ250(), false);
        EqualizerFragment.setEQ(21, saver.getEQ200(), false);
        EqualizerFragment.setEQ(22, saver.getEQ160(), false);
        EqualizerFragment.setEQ(23, saver.getEQ125(), false);
        EqualizerFragment.setEQ(24, saver.getEQ100(), false);
        EqualizerFragment.setEQ(25, saver.getEQ80(), false);
        EqualizerFragment.setEQ(26, saver.getEQ63(), false);
        EqualizerFragment.setEQ(27, saver.getEQ50(), false);
        EqualizerFragment.setEQ(28, saver.getEQ40(), false);
        EqualizerFragment.setEQ(29, saver.getEQ31_5(), false);
        EqualizerFragment.setEQ(30, saver.getEQ25(), false);
        EqualizerFragment.setEQ(31, saver.getEQ20(), false);
        ArrayList<EqualizerItem> arEqualizerItems = EqualizerFragment.sEqualizerItems;
        for(int i = 0; i < arEqualizerItems.size(); i++) {
            EqualizerItem item = arEqualizerItems.get(i);
            item.setSelected(false);
        }
        if(sActivity != null) sActivity.equalizerFragment.getEqualizersAdapter().notifyDataSetChanged();
        EffectFragment.setEffectItems(saver.getEffectItems());
        EffectFragment.setPan(saver.getPan(), false);
        EffectFragment.setFreq(saver.getFreq(), false);
        EffectFragment.sBpm = saver.getBPM();
        EffectFragment.setSoundEffect(saver.getSoundEffectVolume(), false);
        EffectFragment.setTimeOfIncreaseSpeed(saver.getTimeOmIncreaseSpeed());
        EffectFragment.setIncreaseSpeed(saver.getIncreaseSpeed());
        EffectFragment.setTimeOfDecreaseSpeed(saver.getTimeOmDecreaseSpeed());
        EffectFragment.setDecreaseSpeed(saver.getDecreaseSpeed());
        EffectFragment.setComp(saver.getCompGain(), saver.getCompThreshold(), saver.getCompRatio(), saver.getCompAttack(), saver.getCompRelease(), false);
        EffectFragment.setEcho(saver.getEchoDry(), saver.getEchoWet(), saver.getEchoFeedback(), saver.getEchoDelay(), false);
        EffectFragment.setReverb(saver.getReverbDry(), saver.getReverbWet(), saver.getReverbRoomSize(), saver.getReverbDamp(), saver.getReverbWidth(), false);
        EffectFragment.setChorus(saver.getChorusDry(), saver.getChorusWet(), saver.getChorusFeedback(), saver.getChorusMinSweep(), saver.getChorusMaxSweep(), saver.getChorusRate(), false);
        EffectFragment.setDistortion(saver.getDistortionDrive(), saver.getDistortionDry(), saver.getDistortionWet(), saver.getDistortionFeedback(), saver.getDistortionVolume(), false);
        if(saver.isABLoop()) {
            LoopFragment.sABLoop = true;
            if(sActivity != null) sActivity.loopFragment.getRadioGroupLoopMode().check(R.id.radioButtonABLoop);
        }
        else {
            LoopFragment.sABLoop = false;
            if(sActivity != null) sActivity.loopFragment.getRadioGroupLoopMode().check(R.id.radioButtonMarkerPlay);
        }
        if(saver.isLoopA()) LoopFragment.setLoopA(saver.getLoopA(), false);
        if(saver.isLoopB()) LoopFragment.setLoopB(saver.getLoopB(), false);
        LoopFragment.setArMarkerTime(saver.getArMarkerTime());
        if(saver.isLoopMarker()) {
            LoopFragment.sMarkerPlay = true;
            if(sActivity != null) {
                sActivity.loopFragment.getBtnLoopmarker().setSelected(true);
                sActivity.loopFragment.getBtnLoopmarker().setAlpha(0.3f);
            }
        }
        else {
            LoopFragment.sMarkerPlay = false;
            if(sActivity != null) {
                sActivity.loopFragment.getBtnLoopmarker().setSelected(false);
                sActivity.loopFragment.getBtnLoopmarker().setAlpha(1.0f);
            }
        }
        LoopFragment.setMarker(saver.getMarker());
        EffectFragment.setReverbSelected(saver.getReverbSelected());
        EffectFragment.setEchoSelected(saver.getEchoSelected());
        EffectFragment.setChorusSelected(saver.getChorusSelected());
        EffectFragment.setDistortionSelected(saver.getDistortionSelected());
        EffectFragment.setCompSelected(saver.getCompSelected());
        EffectFragment.setSoundEffectSelected(saver.getSoundEffectSelected());
    }

    private void saveSong(int nPurpose, String strFileName) {
        AlertDialog.Builder builder;
        if(sActivity.isDarkMode())
            builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
        else builder = new AlertDialog.Builder(sActivity);
        builder.setTitle(R.string.saving);
        LinearLayout linearLayout = new LinearLayout(sActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        mProgress = new ProgressBar(sActivity, null, android.R.attr.progressBarStyleHorizontal);
        mProgress.setMax(100);
        mProgress.setProgress(0);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        param.topMargin = (int)(24 *  sActivity.getDensity());
        param.leftMargin = param.rightMargin = (int)(16 *  sActivity.getDensity());
        linearLayout.addView(mProgress, param);
        builder.setView(linearLayout);

        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        SongItem item = arSongs.get(sSelectedItem);
        String strPath = item.getPath();
        int _hTempStream;
        Uri uri = Uri.parse(strPath);
        if(uri.getScheme() != null && uri.getScheme().equals("content")) {
            ContentResolver cr = sActivity.getApplicationContext().getContentResolver();
            try {
                MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                params.assetFileDescriptor = cr.openAssetFileDescriptor(Uri.parse(strPath), "r");
                if(params.assetFileDescriptor == null) return;
                params.fileChannel = params.assetFileDescriptor.createInputStream().getChannel();
                _hTempStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        else _hTempStream = BASS.BASS_StreamCreateFile(strPath, 0, 0, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
        if(_hTempStream == 0) return;

        _hTempStream = BASS_FX.BASS_FX_ReverseCreate(_hTempStream, 2, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
        _hTempStream = BASS_FX.BASS_FX_TempoCreate(_hTempStream, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
        final int hTempStream = _hTempStream;
        int chan = BASS_FX.BASS_FX_TempoGetSource(hTempStream);
        if(EffectFragment.isReverse())
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
        BASS.BASS_ChannelSetAttribute(hTempStream, BASS_FX.BASS_ATTRIB_TEMPO, ControlFragment.sSpeed);
        BASS.BASS_ChannelSetAttribute(hTempStream, BASS_FX.BASS_ATTRIB_TEMPO_PITCH, ControlFragment.sPitch);
        int[] arHFX = new int[] {hTempFx20K, hTempFx16K, hTempFx12_5K, hTempFx10K, hTempFx8K, hTempFx6_3K, hTempFx5K, hTempFx4K, hTempFx3_15K, hTempFx2_5K, hTempFx2K, hTempFx1_6K, hTempFx1_25K, hTempFx1K, hTempFx800, hTempFx630, hTempFx500, hTempFx400, hTempFx315, hTempFx250, hTempFx200, hTempFx160, hTempFx125, hTempFx100, hTempFx80, hTempFx63, hTempFx50, hTempFx40, hTempFx31_5, hTempFx25, hTempFx20};
        float fLevel = sActivity.equalizerFragment.getSeeks().get(0).getProgress() - 30;
        if(fLevel == 0) fLevel = 1.0f;
        else if(fLevel < 0) fLevel = (fLevel + 30.0f) / 30.0f;
        else fLevel += 1.0f;
        BASS_FX.BASS_BFX_VOLUME vol = new BASS_FX.BASS_BFX_VOLUME();
        vol.lChannel = 0;
        vol.fVolume = fLevel;
        BASS.BASS_FXSetParameters(hTempFxVol, vol);

        for(int i = 0; i < 31; i++) {
            int nLevel = sActivity.equalizerFragment.getSeeks().get(i+1).getProgress() - 30;
            BASS_FX.BASS_BFX_PEAKEQ eq = new BASS_FX.BASS_BFX_PEAKEQ();
            eq.fBandwidth = 0.7f;
            eq.fQ = 0.0f;
            eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
            eq.fGain = nLevel;
            eq.fCenter = sActivity.equalizerFragment.getArCenters()[i];
            BASS.BASS_FXSetParameters(arHFX[i], eq);
        }
        EffectFragment.applyEffect(hTempStream, item);
        String strPathTo;
        if(nPurpose == 0) { // saveSongToLocal
            int i = 0;
            File fileForCheck;
            while (true) {
                strPathTo = sActivity.getFilesDir() + "/recorded" + String.format(Locale.getDefault(), "%d", i) + ".mp3";
                fileForCheck = new File(strPathTo);
                if (!fileForCheck.exists()) break;
                i++;
            }
        }
        else if(nPurpose == 1) { // export
            File fileDir = new File(sActivity.getExternalCacheDir() + "/export");
            if(!fileDir.exists()) {
                if(!fileDir.mkdir()) System.out.println("ディレクトリが作成できませんでした");
            }
            strPathTo = sActivity.getExternalCacheDir() + "/export/";
            strPathTo += strFileName + ".mp3";
            File file = new File(strPathTo);
            if(file.exists()) {
                if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            }
        }
        else { // saveSongToGallery
            File fileDir = new File(sActivity.getExternalCacheDir() + "/export");
            if(!fileDir.exists()) {
                if(!fileDir.mkdir()) System.out.println("ディレクトリが作成できませんでした");
            }
            strPathTo = sActivity.getExternalCacheDir() + "/export/export.wav";
            File file = new File(strPathTo);
            if (file.exists()) {
                if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            }
        }

        double _dEnd = BASS.BASS_ChannelBytes2Seconds(hTempStream, BASS.BASS_ChannelGetLength(hTempStream, BASS.BASS_POS_BYTE));
        if(sSelectedPlaylist == sPlayingPlaylist && sSelectedItem == sPlaying) {
            if(MainActivity.sLoopA)
                BASS.BASS_ChannelSetPosition(hTempStream, BASS.BASS_ChannelSeconds2Bytes(hTempStream, MainActivity.sLoopAPos), BASS.BASS_POS_BYTE);
            if(MainActivity.sLoopB)
                _dEnd = MainActivity.sLoopBPos;
        }
        final double dEnd = _dEnd;
        int hTempEncode;
        if(nPurpose == 2) // saveSongToGallery
            hTempEncode = BASSenc.BASS_Encode_Start(hTempStream, strPathTo, BASSenc.BASS_ENCODE_PCM | BASSenc.BASS_ENCODE_FP_16BIT, null, null);
        else
            hTempEncode = BASSenc_MP3.BASS_Encode_MP3_StartFile(hTempStream, "", 0, strPathTo);
        final int hEncode = hTempEncode;
        sFinish = false;
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sFinish = true;
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
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

    void saveSongToLocal() {
        saveSong(0, null);
    }

    void saveSongToGallery() {
        if(Build.VERSION.SDK_INT >= 23) {
            if (sActivity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    sActivity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                sActivity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return;
            }
        }
        saveSong(2, null);
    }

    void finishSaveSongToLocal(int hTempStream, int hEncode, String strPathTo, AlertDialog alert) {
        if(alert.isShowing()) alert.dismiss();

        BASSenc.BASS_Encode_Stop(hEncode);
        BASS.BASS_StreamFree(hTempStream);

        if(sFinish) {
            File file = new File(strPathTo);
            if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            sFinish = false;
            return;
        }

        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        SongItem item = arSongs.get(sSelectedItem);
        ArrayList<EffectSaver> arEffectSavers = sEffects.get(sSelectedPlaylist);
        EffectSaver saver = arEffectSavers.get(sSelectedItem);
        ArrayList<String> arTempLyrics = sLyrics.get(sSelectedPlaylist);
        String strLyrics = arTempLyrics.get(sSelectedItem);

        String strTitle = item.getTitle();
        float fSpeed = ControlFragment.sSpeed;
        float fPitch = ControlFragment.sPitch;
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
        else if(fSpeed != 0.0f) strTitle += "(" + getString(R.string.speed) + strSpeed + ")";
        else if(fPitch != 0.0f) strTitle += "(" + getString(R.string.pitch) + strPitch + ")";

        SongItem itemNew = new SongItem(String.format(Locale.getDefault(), "%d", arSongs.size()+1), strTitle, item.getArtist(), strPathTo);
        arSongs.add(itemNew);
        EffectSaver saverNew = new EffectSaver(saver);
        arEffectSavers.add(saverNew);
        arTempLyrics.add(strLyrics);
        if(sSelectedPlaylist == sPlayingPlaylist) sPlays.add(false);
        mSongsAdapter.notifyItemInserted(arSongs.size() - 1);

        saveFiles(true, true, true, true, false);
    }

    void export() {
        AlertDialog.Builder builder;
        if(sActivity.isDarkMode())
            builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
        else builder = new AlertDialog.Builder(sActivity);
        builder.setTitle(R.string.export);
        LinearLayout linearLayout = new LinearLayout(sActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final ClearableEditText editTitle = new ClearableEditText(sActivity, sActivity.isDarkMode());
        editTitle.setHint(R.string.fileName);
        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        SongItem item = arSongs.get(sSelectedItem);
        String strTitle = item.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_");
        float fSpeed = ControlFragment.sSpeed;
        float fPitch = ControlFragment.sPitch;
        String strSpeed = String.format(Locale.getDefault(), "%.1f%%", fSpeed + 100);
        String strPitch;
        if(fPitch >= 0.05f) strPitch = String.format(Locale.getDefault(), "♯%.1f", fPitch);
        else if(fPitch <= -0.05f)
            strPitch = String.format(Locale.getDefault(), "♭%.1f", fPitch * -1);
        else {
            strPitch = String.format(Locale.getDefault(), "%.1f", fPitch < 0.0f ? fPitch * -1 : fPitch);
            if(strPitch.equals("-0.0")) strPitch = "0.0";
        }
        if(fSpeed != 0.0f && fPitch != 0.0f)
            strTitle += "(" + getString(R.string.speed) + strSpeed + "," + getString(R.string.pitch) + strPitch + ")";
        else if(fSpeed != 0.0f) strTitle += "(" + getString(R.string.speed) + strSpeed + ")";
        else if(fPitch != 0.0f) strTitle += "(" + getString(R.string.pitch) + strPitch + ")";
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
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
                editTitle.requestFocus();
                editTitle.setSelection(editTitle.getText().toString().length());
                InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (null != imm) imm.showSoftInput(editTitle, 0);
            }
        });
        alertDialog.show();
    }

    void finishExport(int hTempStream, int hEncode, String strPathTo, AlertDialog alert) {
        if(alert.isShowing()) alert.dismiss();

        BASSenc.BASS_Encode_Stop(hEncode);
        BASS.BASS_StreamFree(hTempStream);

        if(sFinish) {
            File file = new File(strPathTo);
            if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            sFinish = false;
            return;
        }

        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND);
        share.setType("audio/mp3");
        File file = new File(strPathTo);
        Uri uri = FileProvider.getUriForFile(sActivity, "com.edolfzoku.hayaemon2", file);
        PackageManager pm = sActivity.getPackageManager();
        int flag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flag = PackageManager.MATCH_ALL;
        else flag = PackageManager.MATCH_DEFAULT_ONLY;
        List<ResolveInfo> resInfoList = pm.queryIntentActivities(share, flag);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            sActivity.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        share.putExtra(Intent.EXTRA_STREAM, uri);
        startActivityForResult(Intent.createChooser(share, getString(R.string.export)), 0);

        file.deleteOnExit();
    }

    void finishSaveSongToGallery(int hTempStream, int hEncode, String strPathTo, AlertDialog alert) {
        BASSenc.BASS_Encode_Stop(hEncode);
        int nLength = (int)BASS.BASS_ChannelBytes2Seconds(hTempStream, BASS.BASS_ChannelGetLength(hTempStream, BASS.BASS_POS_BYTE)) + 1;
        BASS.BASS_StreamFree(hTempStream);

        if (sFinish) {
            if (alert.isShowing()) alert.dismiss();
            File file = new File(strPathTo);
            if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            sFinish = false;
            return;
        }

        if(mVideoSavingTask != null && mVideoSavingTask.getStatus() == AsyncTask.Status.RUNNING)
            mVideoSavingTask.cancel(true);
        mVideoSavingTask = new VideoSavingTask(this, strPathTo, alert, nLength);
        mVideoSavingTask.execute(0);
    }

    void finishSaveSongToGallery2(int nLength, String strMP4Path, AlertDialog alert, String strPathTo) {
        if (alert.isShowing()) alert.dismiss();

        if (sFinish) {
            File file = new File(strPathTo);
            if(!file.delete()) System.out.println("ファイルが削除できませんでした");
            sFinish = false;
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DURATION, nLength * 1000);
        values.put("_data", strMP4Path);
        ContentResolver cr = sActivity.getContentResolver();
        cr.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

        AlertDialog.Builder builder;
        if(sActivity.isDarkMode())
            builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
        else builder = new AlertDialog.Builder(sActivity);
        builder.setTitle(R.string.saveAsVideo);
        builder.setMessage(R.string.saved);
        builder.setPositiveButton("OK", null);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
            }
        });
        alertDialog.show();
    }

    public static void play() {
        if(MainActivity.sStream == 0) return;
        BASS.BASS_ChannelPlay(MainActivity.sStream, false);
        LoopFragment.startTimer();
        if(sActivity != null) {
            sActivity.getBtnPlay().setContentDescription(sActivity.getString(R.string.pause));
            sActivity.getBtnPlay().setImageResource(sActivity.isDarkMode() ? R.drawable.ic_bar_button_pause_dark : R.drawable.ic_bar_button_pause);
            sActivity.getBtnPlayInPlayingBar().setContentDescription(sActivity.getString(R.string.pause));
            if(sActivity.getSeekCurPos().getVisibility() == View.VISIBLE)
                sActivity.getBtnPlayInPlayingBar().setImageResource(sActivity.isDarkMode() ? R.drawable.ic_playing_large_pause_dark : R.drawable.ic_playing_large_pause);
            else sActivity.getBtnPlayInPlayingBar().setImageResource(sActivity.isDarkMode() ? R.drawable.ic_bar_button_pause_dark : R.drawable.ic_bar_button_pause);
            sActivity.playlistFragment.getSongsAdapter().notifyDataSetChanged();
            sActivity.playlistFragment.getPlaylistsAdapter().notifyDataSetChanged();
            sActivity.playlistFragment.getTabAdapter().notifyDataSetChanged();
        }
        MainActivity.startNotification();
    }

    static void pause() {
        if(MainActivity.sStream == 0) return;
        BASS.BASS_ChannelPause(MainActivity.sStream);
        if(sActivity != null) {
            LoopFragment.stopTimer();
            sActivity.getBtnPlay().setContentDescription(sActivity.getString(R.string.play));
            sActivity.getBtnPlay().setImageResource(sActivity.isDarkMode() ? R.drawable.ic_bar_button_play_dark : R.drawable.ic_bar_button_play);
            sActivity.getBtnPlayInPlayingBar().setContentDescription(sActivity.getString(R.string.play));
            if (sActivity.getSeekCurPos().getVisibility() == View.VISIBLE)
                sActivity.getBtnPlayInPlayingBar().setImageResource(sActivity.isDarkMode() ? R.drawable.ic_playing_large_play_dark : R.drawable.ic_playing_large_play);
            else
                sActivity.getBtnPlayInPlayingBar().setImageResource(sActivity.isDarkMode() ? R.drawable.ic_bar_button_play_dark : R.drawable.ic_bar_button_play);
            sActivity.playlistFragment.getSongsAdapter().notifyDataSetChanged();
        }
        MainActivity.startNotification();
    }

    private static void playPrev() {
        MainActivity.sWaitEnd = false;
        if(MainActivity.sStream == 0) return;
        sPlaying--;
        if(sPlaying < 0) return;
        playSong(sPlaying, true);
    }

    static void playNext(boolean bPlay) {
        MainActivity.sWaitEnd = false;
        int nTempPlaying = sPlaying;
        ArrayList<SongItem> arSongs = sPlaylists.get(sPlayingPlaylist);

        boolean bShuffle = MainActivity.sShuffle == 1;
        boolean bSingle = MainActivity.sShuffle == 2;
        boolean bRepeatAll = MainActivity.sRepeat == 1;
        boolean bRepeatSingle = MainActivity.sRepeat == 2;

        if(bSingle) { // １曲のみ
            if(!bRepeatSingle) nTempPlaying++;
            if (nTempPlaying >= arSongs.size()) {
                if(!bRepeatAll) {
                    stop();
                    return;
                }
                nTempPlaying = 0;
            }
        }
        else if(bShuffle) { // シャッフル
            ArrayList<Integer> arTemp = new ArrayList<>();
            for (int i = 0; i < sPlays.size(); i++) {
                if (i == nTempPlaying) continue;
                boolean bPlayed = sPlays.get(i);
                if (!bPlayed) arTemp.add(i);
            }
            if (arTemp.size() == 0) {
                if(!bRepeatAll) {
                    stop();
                    return;
                }
                for (int i = 0; i < sPlays.size(); i++) sPlays.set(i, false);
            }
            if (sPlays.size() >= 1) {
                Random random = new Random();
                if (arTemp.size() == 0 || arTemp.size() == sPlays.size())
                    nTempPlaying = random.nextInt(sPlays.size());
                else {
                    int nRandom = random.nextInt(arTemp.size());
                    nTempPlaying = arTemp.get(nRandom);
                }
            }
        }
        else {
            nTempPlaying++;
            if (nTempPlaying >= arSongs.size()) {
                if(!bRepeatAll) {
                    stop();
                    return;
                }
                nTempPlaying = 0;
            }
        }
        if(sActivity != null) {
            ArrayList<EffectSaver> arEffectSavers = sEffects.get(sPlayingPlaylist);
            EffectSaver saver = arEffectSavers.get(nTempPlaying);
            if(saver.isSave()) {
                ArrayList<EffectItem> arSavedEffectItems = saver.getEffectItems();
                for(int i = 0; i < arSavedEffectItems.size(); i++) {
                    EffectItem item = arSavedEffectItems.get(i);
                    if(item.getEffectName().equals(EffectFragment.sEffectItems.get(EffectFragment.EFFECTTYPE_REVERSE).getEffectName())) {
                        if(PlaylistFragment.sForceNormal) item.setSelected(false);
                        else if(PlaylistFragment.sForceReverse) item.setSelected(true);
                    }
                }
            }
            PlaylistFragment.sForceNormal = false;
            PlaylistFragment.sForceReverse = false;
        }
        playSong(nTempPlaying, bPlay);
        if(!bPlay) pause();
    }

    void onPlaylistItemClick(int nPlaylist) {
        selectPlaylist(nPlaylist);
        mRelativeSongs.setVisibility(View.VISIBLE);
        mRelativePlaylists.setVisibility(View.INVISIBLE);
        sActivity.getViewSep1().setVisibility(View.INVISIBLE);
    }

    void onSongItemClick(int nSong) {
        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        if(sPlayingPlaylist == sSelectedPlaylist && sPlaying == nSong) {
            if(BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PLAYING) pause();
            else play();
            return;
        }
        if(sPlayingPlaylist != sSelectedPlaylist) {
            sPlays = new ArrayList<>();
            for(int i = 0; i < arSongs.size(); i++)
                sPlays.add(false);
        }
        sPlayingPlaylist = sSelectedPlaylist;
        playSong(nSong, true);
    }

    private static void playSong(int nSong, boolean bPlay) {
        MainActivity.sWaitEnd = false;
        MainActivity.clearLoop(false);

        boolean bReloadLyrics = false;
        if(sActivity != null) {
            if (sActivity.playlistFragment.getRelativeLyrics().getVisibility() == View.VISIBLE && sActivity.playlistFragment.getTextLyrics().getVisibility() == View.VISIBLE && sPlayingPlaylist == sSelectedPlaylist && sPlaying == sSelectedItem) {
                bReloadLyrics = true;
                sSelectedItem = nSong;
            }
        }

        if(sPlayingPlaylist < 0) sPlayingPlaylist = 0;
        else if(sPlayingPlaylist >= sEffects.size()) sPlayingPlaylist = sEffects.size() - 1;
        ArrayList<EffectSaver> arEffectSavers = sEffects.get(sPlayingPlaylist);
        if(0 <= sPlaying && sPlaying < arEffectSavers.size() && 0 <= nSong && nSong < arEffectSavers.size()) {
            EffectSaver saverBefore = arEffectSavers.get(sPlaying);
            EffectSaver saverAfter = arEffectSavers.get(nSong);
            if(saverBefore.isSave() && !saverAfter.isSave()) {
                ControlFragment.setSpeed(0.0f, false);
                ControlFragment.setPitch(0.0f, false);
                EqualizerFragment.resetEQ(false);
                sPlaying = nSong;
                EffectFragment.resetEffect(false);
            }
        }
        sPlaying = nSong;
        if(sPlaylists.size() == 0 || sPlayingPlaylist >= sPlaylists.size() || sPlaylists.get(sPlayingPlaylist).size() == 0 || nSong >= sPlaylists.get(sPlayingPlaylist).size())
            return;
        if(nSong < 0) nSong = 0;
        else if(nSong >= sPlaylists.get(sPlayingPlaylist).size()) nSong = sPlaylists.get(sPlayingPlaylist).size() - 1;
        SongItem item = sPlaylists.get(sPlayingPlaylist).get(nSong);
        final String strPath = item.getPath();
        if(MainActivity.sStream != 0) {
            BASS.BASS_StreamFree(MainActivity.sStream);
            MainActivity.sStream = 0;
        }
        sPlays.set(nSong, true);

        Uri uri = Uri.parse(strPath);
        Context context = sActivity != null ? sActivity : MainActivity.sService;
        if(uri.getScheme() != null && uri.getScheme().equals("content")) {
            boolean lostPermission = false;
            if(Build.VERSION.SDK_INT >= 19) {
                try {
                    context.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                catch (SecurityException e) {
                    e.printStackTrace();
                    lostPermission = true;
                }
            }
            ContentResolver cr = context.getApplicationContext().getContentResolver();
            try {
                MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                params.assetFileDescriptor = cr.openAssetFileDescriptor(Uri.parse(strPath), "r");
                if(params.assetFileDescriptor == null) return;
                params.fileChannel = params.assetFileDescriptor.createInputStream().getChannel();
                MainActivity.sStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS.BASS_STREAM_PRESCAN | BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
                removeSong(sPlayingPlaylist, sPlaying);
                if(sPlaying >= sPlaylists.get(sPlayingPlaylist).size())
                    sPlaying = 0;
                if(sPlaylists.get(sPlayingPlaylist).size() != 0)
                    playSong(sPlaying, true);
                return;
            }
            catch (Exception e) {
                e.printStackTrace();
                if (lostPermission) removeSong(sPlayingPlaylist, sPlaying);
                else sPlaying++;
                if(sPlaying >= sPlaylists.get(sPlayingPlaylist).size())
                    sPlaying = 0;
                if(sPlaylists.get(sPlayingPlaylist).size() != 0)
                    playSong(sPlaying, true);
                return;
            }
        }
        else {
            if (strPath.equals("potatoboy.m4a"))
                MainActivity.sStream = BASS.BASS_StreamCreateFile(new BASS.Asset(sActivity.getAssets(), strPath), 0, 0, BASS.BASS_STREAM_PRESCAN | BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
            else
                MainActivity.sStream = BASS.BASS_StreamCreateFile(strPath, 0, 0, BASS.BASS_STREAM_PRESCAN | BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
        }
        if(MainActivity.sStream == 0) return;
        long byteLength = BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE);
        MainActivity.sByteLength = byteLength;
        double length = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, byteLength);
        MainActivity.sLength = length;
        if(sActivity != null) sActivity.getSeekCurPos().setMax((int)length);

        Bitmap bitmap = null;
        if (item.getPathArtwork() != null && item.getPathArtwork().equals("potatoboy"))
            bitmap = BitmapFactory.decodeResource(sActivity.getResources(), R.drawable.potatoboy);
        else if(item.getPathArtwork() != null && !item.getPathArtwork().equals(""))
            bitmap = BitmapFactory.decodeFile(item.getPathArtwork());
        else {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            try {
                mmr.setDataSource(context.getApplicationContext(), Uri.parse(item.getPath()));
                byte[] data = mmr.getEmbeddedPicture();
                if (data != null) bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                mmr.release();
            }
        }
        if(sActivity != null) {
            if (bitmap != null) sActivity.playlistFragment.getBtnArtworkInPlayingBar().setImageBitmap(bitmap);
            else
                sActivity.playlistFragment.getBtnArtworkInPlayingBar().setImageResource(sActivity.isDarkMode() ? R.drawable.ic_playing_large_artwork_dark : R.drawable.ic_playing_large_artwork);
            sActivity.playlistFragment.getTextTitleInPlayingBar().setText(item.getTitle());
            if(item.getArtist() == null || item.getArtist().equals("")) {
                if(sActivity.isDarkMode()) sActivity.playlistFragment.getTextArtistInPlayingBar().setTextColor(sActivity.getResources().getColor(R.color.darkModeTextDarkGray));
                else sActivity.playlistFragment.getTextArtistInPlayingBar().setTextColor(Color.argb(255, 147, 156, 160));
                sActivity.playlistFragment.getTextArtistInPlayingBar().setText(R.string.unknownArtist);
            }
            else {
                sActivity.playlistFragment.getTextArtistInPlayingBar().setTextColor(sActivity.getResources().getColor(sActivity.isDarkMode() ? R.color.darkModeGray : R.color.lightModeGray));
                sActivity.playlistFragment.getTextArtistInPlayingBar().setText(item.getArtist());
            }

            if(sActivity.getRelativePlayingWithShadow().getVisibility() != View.VISIBLE)
            {
                final RelativeLayout.LayoutParams paramContainer = (RelativeLayout.LayoutParams)sActivity.getViewPager().getLayoutParams();
                final RelativeLayout.LayoutParams paramRecording = (RelativeLayout.LayoutParams)sActivity.getRelativeRecording().getLayoutParams();
                if(MainActivity.sRecord == 0) {
                    paramContainer.bottomMargin = (int) (-22 * sActivity.getDensity());
                    paramRecording.bottomMargin = 0;
                }
                else {
                    paramContainer.bottomMargin = 0;
                    paramRecording.bottomMargin = (int) (-22 * sActivity.getDensity());
                }
                sActivity.getRelativePlayingWithShadow().setTranslationY((int) (82 * sActivity.getDensity()));
                sActivity.getRelativePlayingWithShadow().setVisibility(View.VISIBLE);
                sActivity.getRelativePlayingWithShadow().animate()
                        .translationY(0)
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                sActivity.loopFragment.drawWaveForm(strPath);
                            }
                        });
            }
            else sActivity.loopFragment.drawWaveForm(strPath);
        }

        MainActivity.sStream = BASS_FX.BASS_FX_ReverseCreate(MainActivity.sStream, 2, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
        MainActivity.sStream = BASS_FX.BASS_FX_TempoCreate(MainActivity.sStream, BASS_FX.BASS_FX_FREESOURCE);
        int chan = BASS_FX.BASS_FX_TempoGetSource(MainActivity.sStream);
        if(EffectFragment.isReverse())
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
        EqualizerFragment.setArHFX(new ArrayList<>(Arrays.asList(hFx20K, hFx16K, hFx12_5K, hFx10K, hFx8K, hFx6_3K, hFx5K, hFx4K, hFx3_15K, hFx2_5K, hFx2K, hFx1_6K, hFx1_25K, hFx1K, hFx800, hFx630, hFx500, hFx400, hFx315, hFx250, hFx200, hFx160, hFx125, hFx100, hFx80, hFx63, hFx50, hFx40, hFx31_5, hFx25, hFx20)));
        if(sPlaying < 0) sPlaying = 0;
        else if(sPlaying >= arEffectSavers.size()) sPlaying = arEffectSavers.size() - 1;
        EffectSaver saver = arEffectSavers.get(sPlaying);
        if(saver.isSave()) restoreEffect();
        BASS.BASS_ChannelSetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO, ControlFragment.sSpeed);
        BASS.BASS_ChannelSetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO_PITCH, ControlFragment.sPitch);
        EqualizerFragment.setEQ();
        EffectFragment.applyEffect();
        MainActivity.setSync();
        if(bPlay) {
            BASS.BASS_ChannelPlay(MainActivity.sStream, false);
            LoopFragment.startTimer();
        }
        if(sActivity != null) {
            sActivity.getBtnPlay().setContentDescription(sActivity.getString(R.string.pause));
            sActivity.getBtnPlay().setImageResource(sActivity.isDarkMode() ? R.drawable.ic_bar_button_pause_dark : R.drawable.ic_bar_button_pause);
            sActivity.getBtnPlayInPlayingBar().setContentDescription(sActivity.getString(R.string.pause));
            if(sActivity.getSeekCurPos().getVisibility() == View.VISIBLE)
                sActivity.getBtnPlayInPlayingBar().setImageResource(sActivity.isDarkMode() ? R.drawable.ic_playing_large_pause_dark : R.drawable.ic_playing_large_pause);
            else sActivity.getBtnPlayInPlayingBar().setImageResource(sActivity.isDarkMode() ? R.drawable.ic_bar_button_pause_dark : R.drawable.ic_bar_button_pause);
            sActivity.playlistFragment.getSongsAdapter().notifyDataSetChanged();
            if (sSelectedPlaylist == sPlayingPlaylist && !sActivity.playlistFragment.isMultiSelecting() && !sActivity.playlistFragment.isSorting())
                sActivity.playlistFragment.getRecyclerSongs().scrollToPosition(sPlaying);
            sActivity.playlistFragment.getPlaylistsAdapter().notifyDataSetChanged();
            sActivity.playlistFragment.getTabAdapter().notifyDataSetChanged();
            if(bReloadLyrics) sActivity.playlistFragment.showLyrics();
        }

        MainActivity.startNotification();
    }

    private String getLyrics(int nPlaylist, int nSong) {
        ArrayList<SongItem> arSongs = sPlaylists.get(nPlaylist);
        final SongItem songItem = arSongs.get(nSong);

        try {
            String strPath = getFilePath(sActivity, Uri.parse(songItem.getPath()));
            if(strPath != null) {
                File file = new File(strPath);
                Mp3File mp3file = new Mp3File(file);
                ID3v2 id3v2Tag;
                if (mp3file.hasId3v2Tag()) {
                    id3v2Tag = mp3file.getId3v2Tag();
                    String strLyrics = id3v2Tag.getLyrics();
                    if(sActivity.getExternalCacheDir() != null && file.getParent().equals(sActivity.getExternalCacheDir().toString()))
                        file.deleteOnExit();
                    ArrayList<String> arTempLyrics = sLyrics.get(nPlaylist);
                    arTempLyrics.set(nSong, strLyrics);
                    return strLyrics;
                }
                if(sActivity.getExternalCacheDir() != null && file.getParent().equals(sActivity.getExternalCacheDir().toString()))
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
        return sActivity.copyTempFile(uri).toString();
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

    public static void stop() {
        MainActivity.sWaitEnd = false;

        if(MainActivity.sStream == 0) return;

        if(sActivity != null) {
            if (sActivity.getSeekCurPos().getVisibility() == View.VISIBLE)
                sActivity.downViewPlaying(true);
            else {
                sActivity.getRelativePlayingWithShadow().setVisibility(View.VISIBLE);
                sActivity.getRelativePlayingWithShadow().animate()
                        .translationY((int) (82 * sActivity.getDensity()))
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                sActivity.getRelativePlayingWithShadow().setVisibility(View.GONE);
                                final RelativeLayout.LayoutParams paramContainer = (RelativeLayout.LayoutParams) sActivity.getViewPager().getLayoutParams();
                                final RelativeLayout.LayoutParams paramRecording = (RelativeLayout.LayoutParams) sActivity.getRelativeRecording().getLayoutParams();
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
        }

        if(sPlayingPlaylist < 0) sPlayingPlaylist = 0;
        else if(sPlayingPlaylist >= sEffects.size()) sPlayingPlaylist = sEffects.size() - 1;
        ArrayList<EffectSaver> arEffectSavers = sEffects.get(sPlayingPlaylist);
        if(0 <= sPlaying && sPlaying < arEffectSavers.size()) {
            EffectSaver saverBefore = arEffectSavers.get(sPlaying);
            if(saverBefore.isSave()) {
                ControlFragment.setSpeed(0.0f, false);
                ControlFragment.setPitch(0.0f, false);
                EqualizerFragment.resetEQ(false);
                EffectFragment.resetEffect(false);
            }
        }
        sPlaying = -1;
        BASS.BASS_ChannelStop(MainActivity.sStream);
        BASS.BASS_StreamFree(MainActivity.sStream);
        MainActivity.sStream = 0;
        LoopFragment.stopTimer();
        if(sActivity != null) {
            sActivity.loopFragment.getTextCurValue().setText(sActivity.getString(R.string.zeroHMS));
            sActivity.getBtnPlay().setContentDescription(sActivity.getString(R.string.play));
            sActivity.getBtnPlay().setImageResource(sActivity.isDarkMode() ? R.drawable.ic_bar_button_play_dark : R.drawable.ic_bar_button_play);
            sActivity.getBtnPlayInPlayingBar().setContentDescription(sActivity.getString(R.string.play));
            if (sActivity.getSeekCurPos().getVisibility() == View.VISIBLE)
                sActivity.getBtnPlayInPlayingBar().setImageResource(sActivity.isDarkMode() ? R.drawable.ic_playing_large_play_dark : R.drawable.ic_playing_large_play);
            else
                sActivity.getBtnPlayInPlayingBar().setImageResource(sActivity.isDarkMode() ? R.drawable.ic_bar_button_play_dark : R.drawable.ic_bar_button_play);
            sActivity.playlistFragment.getSongsAdapter().notifyDataSetChanged();
            sActivity.playlistFragment.getPlaylistsAdapter().notifyDataSetChanged();
            sActivity.playlistFragment.getTabAdapter().notifyDataSetChanged();
        }

        MainActivity.clearLoop();
        MainActivity.stopNotification();
    }

    public void addSong(MainActivity sActivity, Uri uri) {
        if(sSelectedPlaylist < 0) sSelectedPlaylist = 0;
        else if(sSelectedPlaylist >= sPlaylists.size()) sSelectedPlaylist = sPlaylists.size() - 1;
        ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
        String strTitle = null;
        String strArtist = null;
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(sActivity.getApplicationContext(), uri);
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
        else {
            strTitle = getFileNameFromUri(sActivity.getApplicationContext(), uri);
            if(strTitle == null) {
                int startIndex = uri.toString().lastIndexOf('/');
                strTitle = uri.toString().substring(startIndex + 1);
            }
            SongItem item = new SongItem(String.format(Locale.getDefault(), "%d", arSongs.size()+1), strTitle, "", uri.toString());
            arSongs.add(item);
        }
        ArrayList<EffectSaver> arEffectSavers = sEffects.get(sSelectedPlaylist);
        EffectSaver saver = new EffectSaver();
        arEffectSavers.add(saver);

        ArrayList<String> arTempLyrics = sLyrics.get(sSelectedPlaylist);
        arTempLyrics.add(null);

        if(sSelectedPlaylist == sPlayingPlaylist) sPlays.add(false);

        if(mSongsAdapter != null) mSongsAdapter.notifyItemInserted(arSongs.size() - 1);
    }

    @SuppressWarnings("deprecation")
    private void addVideo(final MainActivity sActivity, Uri uri) {
        if(Build.VERSION.SDK_INT < 18) return;
        ContentResolver cr = sActivity.getApplicationContext().getContentResolver();

        String strPathTo;
        int n = 0;
        File fileForCheck;
        while (true) {
            strPathTo = sActivity.getFilesDir() + "/recorded" + String.format(Locale.getDefault(), "%d", n) + ".mp3";
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
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0)
                    bufferInfo.size = 0;
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
        if(sActivity.isDarkMode())
            builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
        else builder = new AlertDialog.Builder(sActivity);
        builder.setTitle(R.string.addFromVideo);
        LinearLayout linearLayout = new LinearLayout(sActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final ClearableEditText editTitle = new ClearableEditText(sActivity, sActivity.isDarkMode());
        editTitle.setHint(R.string.title);
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        editTitle.setText(String.format(Locale.getDefault(), "ムービー(%s)", df.format(date)));
        final ClearableEditText editArtist = new ClearableEditText(sActivity, sActivity.isDarkMode());
        editArtist.setHint(R.string.artist);
        editArtist.setText("");
        linearLayout.addView(editTitle);
        linearLayout.addView(editArtist);
        builder.setView(linearLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
                SongItem item = new SongItem(String.format(Locale.getDefault(), "%d", arSongs.size()+1), editTitle.getText().toString(), editArtist.getText().toString(), file.getPath());
                arSongs.add(item);
                ArrayList<EffectSaver> arEffectSavers = sEffects.get(sSelectedPlaylist);
                EffectSaver saver = new EffectSaver();
                arEffectSavers.add(saver);
                ArrayList<String> arTempLyrics = sLyrics.get(sSelectedPlaylist);
                arTempLyrics.add(null);
                if(sSelectedPlaylist == sPlayingPlaylist) sPlays.add(false);
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
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
                editTitle.requestFocus();
                editTitle.setSelection(editTitle.getText().toString().length());
                InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (null != imm) imm.showSoftInput(editTitle, 0);
            }
        });
        alertDialog.show();
    }

    private static void removeSong(int nPlaylist, int nSong) {
        if(nSong < sPlaying) sPlaying--;

        ArrayList<SongItem> arSongs = sPlaylists.get(nPlaylist);
        SongItem song = arSongs.get(nSong);
        Uri uri = Uri.parse(song.getPath());
        if(!(uri.getScheme() != null && uri.getScheme().equals("content"))) {
            File file = new File(song.getPath());
            if(!file.delete()) System.out.println("ファイルが削除できませんでした");
        }

        arSongs.remove(nSong);
        if(nPlaylist == sPlayingPlaylist) sPlays.remove(nSong);

        for(int i = nSong; i < arSongs.size(); i++) {
            SongItem songItem = arSongs.get(i);
            songItem.setNumber(String.format(Locale.getDefault(), "%d", i+1));
        }

        ArrayList<EffectSaver> arEffectSavers = sEffects.get(nPlaylist);
        arEffectSavers.remove(nSong);

        ArrayList<String> arTempLyrics = sLyrics.get(nPlaylist);
        arTempLyrics.remove(nSong);

        if(sActivity != null) sActivity.playlistFragment.getSongsAdapter().notifyDataSetChanged();

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

    void selectPlaylist(int nSelect) {
        if(mRecyclerTab != null) mRecyclerTab.smoothScrollToPosition(nSelect);
        sSelectedPlaylist = nSelect;
        if(mTabAdapter != null) mTabAdapter.notifyDataSetChanged();
        if(mSongsAdapter != null) mSongsAdapter.notifyDataSetChanged();
        if(mPlaylistsAdapter != null) mPlaylistsAdapter.notifyDataSetChanged();
        if(sActivity != null) {
            SharedPreferences preferences = sActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
            preferences.edit().putInt("SelectedPlaylist", sSelectedPlaylist).apply();
        }
    }

    void updateSongTime(final SongItem item) {
        String strPath = item.getPath();
        int hTempStream = 0;

        Uri uri = Uri.parse(strPath);
        if(uri.getScheme() != null && uri.getScheme().equals("content")) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            long durationMs = 0;
            try {
                mmr.setDataSource(sActivity.getApplicationContext(), Uri.parse(strPath));
                durationMs = Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                long duration = durationMs / 1000;
                long lMinutes = duration / 60;
                long lSeconds = duration % 60;
                item.setTime(String.format(Locale.getDefault(), "%d:%02d", lMinutes, lSeconds));
                sActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        mSongsAdapter.notifyItemChanged(Integer.parseInt(item.getNumber()) - 1);
                    }
                });
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
                ContentResolver cr = sActivity.getApplicationContext().getContentResolver();
                try {
                    MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                    params.assetFileDescriptor = cr.openAssetFileDescriptor(Uri.parse(strPath), "r");
                    if (params.assetFileDescriptor == null) return;
                    params.fileChannel = params.assetFileDescriptor.createInputStream().getChannel();
                    hTempStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
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
        sActivity.runOnUiThread(new Runnable() {
            public void run() {
                mSongsAdapter.notifyItemChanged(Integer.parseInt(item.getNumber()) - 1);
            }
        });
        saveFiles(true, false, false, false, false);
    }

    static void saveFiles(boolean bPlaylists, boolean bEffects, boolean bLyrics, boolean bPlaylistNames, boolean bPlayMode) {
        SharedPreferences preferences = sActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        if(bPlaylists)
            preferences.edit().putString("arPlaylists", gson.toJson(sPlaylists)).apply();
        if(bEffects) preferences.edit().putString("arEffects", gson.toJson(sEffects)).apply();
        if(bLyrics) preferences.edit().putString("arLyrics", gson.toJson(sLyrics)).apply();
        if(bPlaylistNames)
            preferences.edit().putString("arPlaylistNames", gson.toJson(sPlaylistNames)).apply();
        if(bPlayMode) {
            preferences.edit().putInt("shufflemode", MainActivity.sShuffle).apply();
            preferences.edit().putInt("repeatmode", MainActivity.sRepeat).apply();
        }
    }

    public void setPeak(float fPeak) {
        if(sPlayingPlaylist < 0 || sPlayingPlaylist >= sPlaylists.size()) return;
        ArrayList<SongItem> arSongs = sPlaylists.get(sPlayingPlaylist);
        if(sPlaying < 0 || sPlaying >= arSongs.size()) return;
        SongItem song = arSongs.get(sPlaying);
        if(song.getPeak() != fPeak) {
            song.setPeak(fPeak);
            saveFiles(true, false, false, false, false);
            EffectFragment.sPeak = fPeak;
        }
    }

    public void setLightMode(boolean animated) {
        final RelativeLayout relativePlaylistFragment = sActivity.findViewById(R.id.relativePlaylistFragment);
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
                    mDivider1.setBackgroundColor(nColorModeSep);
                    mDivider2.setBackgroundColor(nColorModeSep);
                    mViewSepLyrics.setBackgroundColor(nColorModeSep);
                    mTextPlaylist.setTextColor(nColorModeText);
                    mTextLyricsTitle.setTextColor(nColorModeText);
                    mTextLyrics.setTextColor(nColorModeText);
                    mEditLyrics.setTextColor(nColorModeText);
                    mTextPlaylistInMultipleSelection.setTextColor(nColorModeText);
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
            mDivider1.setBackgroundColor(nLightModeSep);
            mDivider2.setBackgroundColor(nLightModeSep);
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

        mViewMultipleSelection.setBackgroundResource(R.drawable.bottomshadow);
        mImgSelectAllInMultipleSelection.setImageResource(R.drawable.ic_button_check_off);
        mBtnCopyInMultipleSelection.setImageResource(R.drawable.ic_bar_button_copy);
        mBtnMoveInMultipleSelection.setImageResource(R.drawable.ic_bar_button_folder_move);
        mBtnDeleteInMultipleSelection.setImageResource(R.drawable.ic_bar_button_delete);
    }

    public void setDarkMode(boolean animated) {
        if(sActivity == null) return;
        final RelativeLayout relativePlaylistFragment = sActivity.findViewById(R.id.relativePlaylistFragment);
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
            mDivider1.setBackgroundColor(nColorModeSep);
            mDivider2.setBackgroundColor(nColorModeSep);
            mViewSepLyrics.setBackgroundColor(nColorModeSep);
            mTextPlaylist.setTextColor(nColorModeText);
            mTextLyricsTitle.setTextColor(nColorModeText);
            mTextLyrics.setTextColor(nColorModeText);
            mEditLyrics.setTextColor(nColorModeText);
            mTextPlaylistInMultipleSelection.setTextColor(nColorModeText);
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

        mViewMultipleSelection.setBackgroundResource(R.drawable.bottomshadow_dark);
        mImgSelectAllInMultipleSelection.setImageResource(R.drawable.ic_button_check_off_dark);
        mBtnCopyInMultipleSelection.setImageResource(R.drawable.ic_bar_button_copy_dark);
        mBtnMoveInMultipleSelection.setImageResource(R.drawable.ic_bar_button_folder_move_dark);
        mBtnDeleteInMultipleSelection.setImageResource(R.drawable.ic_bar_button_delete_dark);
    }
}