/*
 * PlaylistsAdapter
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
import android.graphics.Typeface;
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

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.ViewHolder>
{
    MainActivity activity;
    private int resource;
    private List<String> items;
    private LayoutInflater inflater;

    static class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout playlistItem;
        TextView textName;
        TextView textSongCount;
        ImageView imgRight;
        FrameLayout framePlaylistMenu;
        ImageView imgPlaylistMenu;

        ViewHolder(View view) {
            super(view);
            playlistItem = (RelativeLayout) view.findViewById(R.id.playlistItem);
            textName = (TextView) view.findViewById(R.id.textName);
            textSongCount = (TextView) view.findViewById(R.id.textSongCount);
            imgRight = (ImageView) view.findViewById(R.id.imgRight);
            framePlaylistMenu = (FrameLayout)view.findViewById(R.id.framePlaylistMenu);
            imgPlaylistMenu = (ImageView) view.findViewById(R.id.imgPlaylistMenu);
        }
    }

    public PlaylistsAdapter(Context context, int resource, List<String> items)
    {
        this.activity = (MainActivity)context;
        this.resource = resource;
        this.items = items;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public PlaylistsAdapter(Context context, int resource)
    {
        this.activity = (MainActivity)context;
        this.resource = resource;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public PlaylistsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(resource, parent, false);

        return new PlaylistsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PlaylistsAdapter.ViewHolder holder, final int position)
    {
        String item = items.get(position);
        final PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);

        holder.textName.setText(item);
        holder.textSongCount.setText(String.format("%d曲", playlistFragment.getSongCount(position)));

        holder.playlistItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playlistFragment.onPlaylistItemClick(position);
            }
        });
        if(playlistFragment.isSorting()) {
            holder.framePlaylistMenu.setOnClickListener(null);
            holder.playlistItem.setOnLongClickListener(null);
            holder.framePlaylistMenu.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    playlistFragment.getPlaylistTouchHelper().startDrag(holder);
                    return true;
                }
            });
            holder.imgPlaylistMenu.setImageResource(R.drawable.ic_sort);
            holder.imgRight.setVisibility(View.GONE);
        }
        else {
            holder.framePlaylistMenu.setOnTouchListener(null);
            holder.framePlaylistMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playlistFragment.showPlaylistMenu(position);
                }
            });
            holder.playlistItem.setOnLongClickListener(new View.OnLongClickListener()
            {
                public boolean onLongClick(View v) {
                    playlistFragment.showPlaylistMenu(position);
                    return true;
                }
            });
            holder.imgPlaylistMenu.setImageResource(R.drawable.ic_listmenu);
            holder.imgRight.setVisibility(View.VISIBLE);
        }

        holder.playlistItem.setBackgroundColor(Color.argb(255, 255, 255, 255));
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }
}
