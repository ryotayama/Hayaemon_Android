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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.un4seen.bass.BASS;

import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder>
{
    MainActivity activity;
    private int resource;
    private List<SongItem> items = null;

    static class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout songItem;
        TextView textNumber;
        TextView textTitle;
        TextView textArtist;
        ImageView imgStatus;
        ImageView imgLock;
        FrameLayout frameSongMenu;
        ImageView imgSongMenu;

        ViewHolder(View view) {
            super(view);
            songItem = view.findViewById(R.id.songItem);
            textNumber = view.findViewById(R.id.textNumber);
            textTitle = view.findViewById(R.id.textTitle);
            textArtist = view.findViewById(R.id.textArtist);
            imgStatus = view.findViewById(R.id.imgStatus);
            imgLock = view.findViewById(R.id.imgLock);
            frameSongMenu = view.findViewById(R.id.frameSongMenu);
            imgSongMenu = view.findViewById(R.id.imgSongMenu);
        }
    }

    SongsAdapter(Context context, int resource, List<SongItem> items)
    {
        this.activity = (MainActivity)context;
        this.resource = resource;
        this.items = items;
    }

    SongsAdapter(Context context, int resource)
    {
        this.activity = (MainActivity)context;
        this.resource = resource;
    }

    void changeItems(List<SongItem> items)
    {
        this.items = items;
    }

    @Override
    public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(resource, parent, false);

        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position)
    {
        SongItem item = items.get(position);
        final int nItem = Integer.parseInt(item.getNumber()) - 1;
        boolean bLock = activity.playlistFragment.isLock(nItem);

        holder.itemView.setLongClickable(true);

        holder.textNumber.setText(item.getNumber());
        holder.textTitle.setText(item.getTitle());
        if(item.getArtist() == null || item.getArtist().equals(""))
        {
            holder.textArtist.setTextColor(Color.argb(255, 147, 156, 160));
            holder.textArtist.setText("〈不明なアーティスト〉");
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

        holder.songItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.playlistFragment.onSongItemClick(nItem);
            }
        });
        if(activity.playlistFragment.isSorting()) {
            holder.frameSongMenu.setOnClickListener(null);
            holder.songItem.setOnLongClickListener(null);
            holder.frameSongMenu.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    activity.playlistFragment.getSongTouchHelper().startDrag(holder);
                    return true;
                }
            });
            holder.imgSongMenu.setImageResource(R.drawable.ic_sort);
        }
        else {
            holder.frameSongMenu.setOnTouchListener(null);
            holder.frameSongMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.playlistFragment.showSongMenu(holder.getAdapterPosition());
                }
            });
            holder.songItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    activity.playlistFragment.showSongMenu(holder.getAdapterPosition());
                    return true;
                }
            });
            holder.imgSongMenu.setImageResource(R.drawable.ic_listmenu);
        }

        if(activity.playlistFragment.getPlayingPlaylist() == activity.playlistFragment.getSelectedPlaylist() && nItem == activity.playlistFragment.getPlaying())
            holder.songItem.setBackgroundColor(Color.argb(255, 224, 239, 255));
        else
            holder.songItem.setBackgroundColor(Color.argb(255, 255, 255, 255));
    }

    @Override
    public int getItemCount()
    {
        if(items == null) return 0;
        else return items.size();
    }
}
