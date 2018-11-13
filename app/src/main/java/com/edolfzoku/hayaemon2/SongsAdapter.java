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

import android.content.Context;
import android.graphics.Color;
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
    private LayoutInflater inflater;

    public String getTitle(int nPosition)
    {
        SongItem item = items.get(nPosition);
        return item.getTitle();
    }

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
            songItem = (RelativeLayout) view.findViewById(R.id.songItem);
            textNumber = (TextView) view.findViewById(R.id.textNumber);
            textTitle = (TextView) view.findViewById(R.id.textTitle);
            textArtist = (TextView) view.findViewById(R.id.textArtist);
            imgStatus = (ImageView) view.findViewById(R.id.imgStatus);
            imgLock = (ImageView) view.findViewById(R.id.imgLock);
            frameSongMenu = (FrameLayout) view.findViewById(R.id.frameSongMenu);
            imgSongMenu = (ImageView) view.findViewById(R.id.imgSongMenu);
        }
    }

    public SongsAdapter(Context context, int resource, List<SongItem> items)
    {
        this.activity = (MainActivity)context;
        this.resource = resource;
        this.items = items;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public SongsAdapter(Context context, int resource)
    {
        this.activity = (MainActivity)context;
        this.resource = resource;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void changeItems(List<SongItem> items)
    {
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(resource, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position)
    {
        SongItem item = items.get(position);
        final int nItem = Integer.parseInt(item.getNumber()) - 1;
        final PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
        boolean bLock = playlistFragment.isLock(nItem);

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
        if(playlistFragment.getPlayingPlaylist() == playlistFragment.getSelectedPlaylist() && nItem == playlistFragment.getPlaying()) {
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
                playlistFragment.onSongItemClick(nItem);
            }
        });
        if(playlistFragment.isSorting()) {
            holder.frameSongMenu.setOnClickListener(null);
            holder.songItem.setOnLongClickListener(null);
            holder.frameSongMenu.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    playlistFragment.getSongTouchHelper().startDrag(holder);
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
                    playlistFragment.showSongMenu(position);
                }
            });
            holder.songItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    playlistFragment.showSongMenu(position);
                    return true;
                }
            });
            holder.imgSongMenu.setImageResource(R.drawable.ic_listmenu);
        }

        if(playlistFragment.getPlayingPlaylist() == playlistFragment.getSelectedPlaylist() && nItem == playlistFragment.getPlaying())
            holder.songItem.setBackgroundColor(Color.argb(255, 224, 239, 255));
        else if(nItem % 2 == 0)
            holder.songItem.setBackgroundColor(Color.argb(255, 247, 247, 247));
        else
            holder.songItem.setBackgroundColor(Color.argb(255, 240, 240, 240));
    }

    @Override
    public int getItemCount()
    {
        if(items == null) return 0;
        else return items.size();
    }
}
