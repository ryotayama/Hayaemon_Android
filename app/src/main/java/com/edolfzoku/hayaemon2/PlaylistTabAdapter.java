/*
 * PlaylistTabAdapter
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class PlaylistTabAdapter extends RecyclerView.Adapter<PlaylistTabAdapter.ViewHolder>
{
    MainActivity activity;
    private int resource;
    private List<String> items;
    private LayoutInflater inflater;
    private int nPosition;

    public void setPosition(int nPosition) { this.nPosition = nPosition; }
    public int getPosition() { return nPosition; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textPlaylistTab;

        ViewHolder(View view) {
            super(view);
            textPlaylistTab = (TextView) view.findViewById(R.id.textPlaylistTab);
        }
    }

    public PlaylistTabAdapter(Context context, int resource, List<String> items)
    {
        this.activity = (MainActivity)context;
        this.resource = resource;
        this.items = items;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public PlaylistTabAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(resource, parent, false);

        return new PlaylistTabAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PlaylistTabAdapter.ViewHolder holder, final int position)
    {
        String item = items.get(position);
        final PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);

        holder.textPlaylistTab.setLongClickable(true);
        playlistFragment.registerForContextMenu(holder.textPlaylistTab);

        holder.textPlaylistTab.setText(item);
        if(position == playlistFragment.getSelectedPlaylist()) {
            holder.textPlaylistTab.setTypeface(Typeface.DEFAULT_BOLD);
            holder.textPlaylistTab.setBackgroundColor(Color.argb(255, 170, 170, 170));
        }
        else {
            holder.textPlaylistTab.setTypeface(Typeface.DEFAULT);
            holder.textPlaylistTab.setBackgroundColor(Color.argb(255, 255, 255, 255));
        }

        holder.textPlaylistTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playlistFragment.selectPlaylist(position);
            }
        });

        holder.textPlaylistTab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                nPosition = position;
                return false;
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }
}
