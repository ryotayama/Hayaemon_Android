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
    private final MainActivity activity;

    static class ViewHolder extends RecyclerView.ViewHolder {
        final RelativeLayout songItem;
        final ImageView imgSelectSong;
        final TextView textNumber;
        final TextView textTitle;
        final TextView textArtist;
        final ImageView imgStatus;
        final ImageView imgLock;
        final TextView textTime;
        final ImageView imgSongMenu;

        ViewHolder(View view) {
            super(view);
            songItem = view.findViewById(R.id.songItem);
            imgSelectSong = view.findViewById(R.id.imgSelectSong);
            textNumber = view.findViewById(R.id.textNumber);
            textTitle = view.findViewById(R.id.textTitle);
            textArtist = view.findViewById(R.id.textArtist);
            imgStatus = view.findViewById(R.id.imgStatus);
            imgLock = view.findViewById(R.id.imgLock);
            textTime = view.findViewById(R.id.textTime);
            imgSongMenu = view.findViewById(R.id.imgSongMenu);
        }
    }

    SongsAdapter(Context context)
    {
        this.activity = (MainActivity)context;
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
        ArrayList<SongItem> arSongs = activity.playlistFragment.getArPlaylists ().get(activity.playlistFragment.getSelectedPlaylist());
        SongItem item = arSongs.get(position);
        if(item.getTime() == null) activity.playlistFragment.updateSongTime(item);
        final int nItem = Integer.parseInt(item.getNumber()) - 1;
        boolean bLock = activity.playlistFragment.isLock(nItem);
        boolean bSelected = activity.playlistFragment.isSelected(nItem);

        holder.itemView.setLongClickable(true);

        holder.textNumber.setText(item.getNumber());
        holder.textTitle.setText(item.getTitle());
        if(item.getArtist() == null || item.getArtist().equals(""))
        {
            holder.textArtist.setTextColor(Color.argb(255, 147, 156, 160));
            holder.textArtist.setText(R.string.unknownArtist);
        }
        else
        {
            holder.textArtist.setTextColor(Color.argb(255, 102, 102, 102));
            holder.textArtist.setText(item.getArtist());
        }
        if(activity.playlistFragment.getPlayingPlaylist() == activity.playlistFragment.getSelectedPlaylist() && nItem == activity.playlistFragment.getPlaying()) {
            if(BASS.BASS_ChannelIsActive(MainActivity.hStream) == BASS.BASS_ACTIVE_PLAYING)
                holder.imgStatus.setImageResource(R.drawable.circle_music);
            else
                holder.imgStatus.setImageResource(R.drawable.pause_circle);
            holder.textNumber.setVisibility(View.INVISIBLE);
        }
        else {
            holder.imgStatus.setImageDrawable(null);
            holder.textNumber.setVisibility(View.VISIBLE);
        }

        if(bLock) holder.imgLock.setVisibility(View.VISIBLE);
        else holder.imgLock.setVisibility(View.GONE);

        String strTime = item.getTime();
        holder.textTime.setText(strTime);
        if(strTime != null && strTime.length() >= 6) holder.textTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
        else holder.textTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);

        if(activity.playlistFragment.isMultiSelecting()) {
            holder.songItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.playlistFragment.onTouchMultipleSelectionItem(holder.getAdapterPosition());
                    activity.playlistFragment.getSongsAdapter().notifyItemChanged(holder.getAdapterPosition(), holder.imgSelectSong);
                }
            });
            holder.imgSongMenu.setOnClickListener(null);
            holder.songItem.setOnLongClickListener(null);
            holder.imgSongMenu.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    activity.playlistFragment.getSongTouchHelper().startDrag(holder);
                    return true;
                }
            });
            if(bSelected) holder.imgSelectSong.setImageResource(R.drawable.ic_button_check_on);
            else holder.imgSelectSong.setImageResource(R.drawable.ic_button_check_off);
            holder.imgSelectSong.setVisibility(View.VISIBLE);
            holder.imgSongMenu.setImageResource(R.drawable.ic_sort);
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)holder.imgSongMenu.getLayoutParams();
            param.leftMargin = param.rightMargin = (int) (8 * activity.getResources().getDisplayMetrics().density + 0.5);
        }
        else if(activity.playlistFragment.isSorting()) {
            holder.songItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.playlistFragment.onSongItemClick(nItem);
                }
            });
            holder.imgSongMenu.setOnClickListener(null);
            holder.songItem.setOnLongClickListener(null);
            holder.imgSongMenu.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    activity.playlistFragment.getSongTouchHelper().startDrag(holder);
                    return true;
                }
            });
            holder.imgSelectSong.setVisibility(View.GONE);
            holder.imgSongMenu.setImageResource(R.drawable.ic_sort);
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)holder.imgSongMenu.getLayoutParams();
            param.leftMargin = param.rightMargin = (int) (8 * activity.getResources().getDisplayMetrics().density + 0.5);
        }
        else {
            holder.songItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.playlistFragment.onSongItemClick(nItem);
                }
            });
            holder.imgSongMenu.setOnTouchListener(null);
            holder.imgSongMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.playlistFragment.showSongMenu(holder.getAdapterPosition());
                }
            });
            holder.songItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    activity.playlistFragment.startMultipleSelection(holder.getAdapterPosition());
                    return true;
                }
            });
            holder.imgSelectSong.setVisibility(View.GONE);
            holder.imgSongMenu.setImageResource(R.drawable.ic_button_more);
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)holder.imgSongMenu.getLayoutParams();
            param.leftMargin = param.rightMargin = 0;
        }

        if(activity.playlistFragment.getPlayingPlaylist() == activity.playlistFragment.getSelectedPlaylist() && nItem == activity.playlistFragment.getPlaying())
            holder.songItem.setBackgroundColor(Color.argb(255, 224, 239, 255));
        else
            holder.songItem.setBackgroundColor(Color.argb(255, 255, 255, 255));
    }

    @Override
    public int getItemCount()
    {
        ArrayList<SongItem> arSongs = activity.playlistFragment.getArPlaylists ().get(activity.playlistFragment.getSelectedPlaylist());
        return arSongs.size();
    }
}
