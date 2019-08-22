/*
 * SongsAdapter
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.un4seen.bass.BASS;

import java.util.ArrayList;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder>
{
    private final MainActivity mActivity;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout mSongItem;
        private final ImageView mImgSelectSong;
        private final TextView mTextNumber;
        private final TextView mTextTitle;
        private final TextView mTextArtist;
        private final ImageView mImgStatus;
        private final ImageView mImgLock;
        private final TextView mTextTime;
        private final ImageView mImgSongMenu;
        private final View mViewSepSong;

        RelativeLayout getSongItem() { return mSongItem; }
        ImageView getImgSelectSong() { return mImgSelectSong; }
        TextView getTextNumber() { return mTextNumber; }
        TextView getTextTitle() { return mTextTitle; }
        TextView getTextArtist() { return mTextArtist; }
        ImageView getImgStatus() { return mImgStatus; }
        ImageView getImgLock() { return mImgLock; }
        TextView getTextTime() { return mTextTime; }
        ImageView getImgSongMenu() { return mImgSongMenu; }
        View getViewSepSong() { return mViewSepSong; }

        ViewHolder(View view) {
            super(view);
            mSongItem = view.findViewById(R.id.songItem);
            mImgSelectSong = view.findViewById(R.id.imgSelectSong);
            mTextNumber = view.findViewById(R.id.textNumber);
            mTextTitle = view.findViewById(R.id.textTitle);
            mTextArtist = view.findViewById(R.id.textArtist);
            mImgStatus = view.findViewById(R.id.imgStatus);
            mImgLock = view.findViewById(R.id.imgLock);
            mTextTime = view.findViewById(R.id.textTime);
            mImgSongMenu = view.findViewById(R.id.imgSongMenu);
            mViewSepSong = view.findViewById(R.id.viewSepSong);
        }
    }

    SongsAdapter(Context context)
    {
        mActivity = (MainActivity)context;
    }

    @Override
    public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_item, parent, false);

        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position)
    {
        holder.getViewSepSong().setBackgroundColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeSep : R.color.lightModeSep));

        ArrayList<SongItem> arSongs = mActivity.playlistFragment.getPlaylists ().get(mActivity.playlistFragment.getSelectedPlaylist());
        SongItem item = arSongs.get(position);
        if(item.getTime() == null) mActivity.playlistFragment.updateSongTime(item);
        final int nItem = Integer.parseInt(item.getNumber()) - 1;
        boolean bLock = mActivity.playlistFragment.isLock(nItem);
        boolean bSelected = mActivity.playlistFragment.isSelected(nItem);

        holder.itemView.setLongClickable(true);

        holder.getTextNumber().setText(item.getNumber());
        holder.getTextNumber().setTextColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeGray : R.color.lightModeGray));
        holder.getTextTitle().setText(item.getTitle());
        holder.getTextTitle().setTextColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? android.R.color.white : android.R.color.black));
        if(item.getArtist() == null || item.getArtist().equals(""))
        {
            if(mActivity.isDarkMode()) holder.getTextArtist().setTextColor(mActivity.getResources().getColor(R.color.darkModeTextDarkGray));
            else holder.getTextArtist().setTextColor(Color.argb(255, 147, 156, 160));
            holder.getTextArtist().setText(R.string.unknownArtist);
        }
        else
        {
            holder.getTextArtist().setTextColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeGray : R.color.lightModeGray));
            holder.getTextArtist().setText(item.getArtist());
        }
        if(mActivity.playlistFragment.getPlayingPlaylist() == mActivity.playlistFragment.getSelectedPlaylist() && nItem == mActivity.playlistFragment.getPlaying()) {
            if(BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PLAYING)
                holder.getImgStatus().setImageResource(mActivity.isDarkMode() ? R.drawable.ic_icon_playlist_playing_dark : R.drawable.ic_icon_playlist_playing);
            else
                holder.getImgStatus().setImageResource(mActivity.isDarkMode() ? R.drawable.ic_icon_playlist_pause_dark : R.drawable.ic_icon_playlist_pause);
            holder.getTextNumber().setVisibility(View.INVISIBLE);
        }
        else {
            holder.getImgStatus().setImageDrawable(null);
            holder.getTextNumber().setVisibility(View.VISIBLE);
        }

        if(bLock) {
            holder.getImgLock().setImageResource(mActivity.isDarkMode() ? R.drawable.ic_icon_playlist_lock_dark : R.drawable.ic_icon_playlist_lock);
            holder.getImgLock().setVisibility(View.VISIBLE);
        }
        else holder.getImgLock().setVisibility(View.GONE);

        String strTime = item.getTime();
        holder.getTextTime().setText(strTime);
        holder.getTextTime().setTextColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? android.R.color.white : android.R.color.black));
        if(strTime != null && strTime.length() >= 6) holder.getTextTime().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
        else holder.getTextTime().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);

        if(mActivity.playlistFragment.isMultiSelecting()) {
            holder.getSongItem().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.playlistFragment.onTouchMultipleSelectionItem(holder.getAdapterPosition());
                    mActivity.playlistFragment.getSongsAdapter().notifyItemChanged(holder.getAdapterPosition(), holder.getImgSelectSong());
                }
            });
            holder.getImgSongMenu().setOnClickListener(null);
            holder.getSongItem().setOnLongClickListener(null);
            holder.getImgSongMenu().setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    mActivity.playlistFragment.getSongTouchHelper().startDrag(holder);
                    return true;
                }
            });
            if(bSelected) holder.getImgSelectSong().setImageResource(mActivity.isDarkMode() ? R.drawable.ic_button_check_on_dark : R.drawable.ic_button_check_on);
            else holder.getImgSelectSong().setImageResource(mActivity.isDarkMode() ? R.drawable.ic_button_check_off_dark : R.drawable.ic_button_check_off);
            holder.getImgSelectSong().setVisibility(View.VISIBLE);
            holder.getImgSongMenu().setImageResource(R.drawable.ic_sort);
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)holder.getImgSongMenu().getLayoutParams();
            param.leftMargin = param.rightMargin = (int) (8 * mActivity.getDensity());
        }
        else if(mActivity.playlistFragment.isSorting()) {
            holder.getSongItem().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.playlistFragment.onSongItemClick(nItem);
                }
            });
            holder.getImgSongMenu().setOnClickListener(null);
            holder.getSongItem().setOnLongClickListener(null);
            holder.getImgSongMenu().setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    mActivity.playlistFragment.getSongTouchHelper().startDrag(holder);
                    return true;
                }
            });
            holder.getImgSelectSong().setVisibility(View.GONE);
            holder.getImgSongMenu().setImageResource(R.drawable.ic_sort);
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)holder.getImgSongMenu().getLayoutParams();
            param.leftMargin = param.rightMargin = (int) (8 * mActivity.getDensity());
        }
        else {
            holder.getSongItem().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.playlistFragment.onSongItemClick(nItem);
                }
            });
            holder.getImgSongMenu().setOnTouchListener(null);
            holder.getImgSongMenu().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.playlistFragment.showSongMenu(holder.getAdapterPosition());
                }
            });
            holder.getSongItem().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mActivity.playlistFragment.startMultipleSelection(holder.getAdapterPosition());
                    return true;
                }
            });
            holder.getImgSelectSong().setVisibility(View.GONE);
            holder.getImgSongMenu().setImageResource(R.drawable.ic_button_more);
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)holder.getImgSongMenu().getLayoutParams();
            param.leftMargin = param.rightMargin = 0;
        }

        if(mActivity.playlistFragment.getPlayingPlaylist() == mActivity.playlistFragment.getSelectedPlaylist() && nItem == mActivity.playlistFragment.getPlaying())
            holder.getSongItem().setBackgroundColor(mActivity.isDarkMode() ? mActivity.getResources().getColor(R.color.darkModeSelect) : Color.argb(255, 224, 239, 255));
        else
            holder.getSongItem().setBackgroundColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeBk : R.color.lightModeBk));
    }

    @Override
    public int getItemCount()
    {
        ArrayList<SongItem> arSongs = mActivity.playlistFragment.getPlaylists ().get(mActivity.playlistFragment.getSelectedPlaylist());
        return arSongs.size();
    }
}
