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

import java.util.List;
import java.util.Locale;

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.ViewHolder>
{
    MainActivity activity;
    private int resource;
    private List<String> items;

    static class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout playlistItem;
        TextView textName;
        TextView textSongCount;
        ImageView imgRight;
        FrameLayout framePlaylistMenu;
        ImageView imgPlaylistMenu;

        ViewHolder(View view) {
            super(view);
            playlistItem = view.findViewById(R.id.playlistItem);
            textName = view.findViewById(R.id.textName);
            textSongCount = view.findViewById(R.id.textSongCount);
            imgRight = view.findViewById(R.id.imgRight);
            framePlaylistMenu = view.findViewById(R.id.framePlaylistMenu);
            imgPlaylistMenu = view.findViewById(R.id.imgPlaylistMenu);
        }
    }

    PlaylistsAdapter(Context context, int resource, List<String> items)
    {
        this.activity = (MainActivity)context;
        this.resource = resource;
        this.items = items;
    }

    @Override
    public @NonNull PlaylistsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(resource, parent, false);

        return new PlaylistsAdapter.ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final PlaylistsAdapter.ViewHolder holder, int position)
    {
        String item = items.get(position);

        holder.textName.setText(item);
        holder.textSongCount.setText(String.format(Locale.getDefault(), "%d曲", activity.playlistFragment.getSongCount(position)));

        holder.playlistItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.playlistFragment.onPlaylistItemClick(holder.getAdapterPosition());
            }
        });
        if(activity.playlistFragment.isSorting()) {
            holder.framePlaylistMenu.setOnClickListener(null);
            holder.playlistItem.setOnLongClickListener(null);
            holder.framePlaylistMenu.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    activity.playlistFragment.getPlaylistTouchHelper().startDrag(holder);
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
                    activity.playlistFragment.showPlaylistMenu(holder.getAdapterPosition());
                }
            });
            holder.playlistItem.setOnLongClickListener(new View.OnLongClickListener()
            {
                public boolean onLongClick(View v) {
                    activity.playlistFragment.showPlaylistMenu(holder.getAdapterPosition());
                    return true;
                }
            });
            holder.imgPlaylistMenu.setImageResource(R.drawable.ic_listmenu);
            holder.imgRight.setVisibility(View.VISIBLE);
        }

        if(activity.playlistFragment.getPlayingPlaylist() == position && activity.playlistFragment.getPlaying() != -1)
            holder.playlistItem.setBackgroundColor(Color.argb(255, 224, 239, 255));
        else
            holder.playlistItem.setBackgroundColor(Color.argb(255, 255, 255, 255));
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }
}
