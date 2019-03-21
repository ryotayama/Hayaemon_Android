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
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class PlaylistTabAdapter extends RecyclerView.Adapter<PlaylistTabAdapter.ViewHolder>
{
    MainActivity activity;
    private int resource;
    private List<String> items;
    private LayoutInflater inflater;

    static class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout relativePlaylistTab;
        TextView textPlaylistTab;
        AnimationButton btnPlaylistMenu;

        ViewHolder(View view) {
            super(view);
            relativePlaylistTab = view.findViewById(R.id.relativePlaylistTab);
            textPlaylistTab = view.findViewById(R.id.textPlaylistTab);
            btnPlaylistMenu = view.findViewById(R.id.btnPlaylistMenu);
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

        holder.textPlaylistTab.setText(item);
        if(position == playlistFragment.getSelectedPlaylist()) {
            holder.textPlaylistTab.setContentDescription(item + "、選択ずみ");
            holder.textPlaylistTab.setTypeface(Typeface.DEFAULT_BOLD);
            holder.relativePlaylistTab.setBackgroundResource(R.drawable.playlisttab_select);
            holder.textPlaylistTab.setPadding(holder.textPlaylistTab.getPaddingLeft(), holder.textPlaylistTab.getPaddingTop(), 0, holder.textPlaylistTab.getPaddingBottom());
            holder.btnPlaylistMenu.setVisibility(View.VISIBLE);
            holder.btnPlaylistMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playlistFragment.showPlaylistMenu(position);
                }
            });
        }
        else if(playlistFragment.getPlayingPlaylist() == position && playlistFragment.getPlaying() != -1) {
            holder.textPlaylistTab.setContentDescription(item);
            holder.textPlaylistTab.setTypeface(Typeface.DEFAULT);
            holder.relativePlaylistTab.setBackgroundResource(R.drawable.playlisttab_play);
            holder.textPlaylistTab.setPadding(holder.textPlaylistTab.getPaddingLeft(), holder.textPlaylistTab.getPaddingTop(), (int) (16 * activity.getResources().getDisplayMetrics().density + 0.5), holder.textPlaylistTab.getPaddingBottom());
            holder.btnPlaylistMenu.setVisibility(View.GONE);
        }
        else {
            holder.textPlaylistTab.setContentDescription(item);
            holder.textPlaylistTab.setTypeface(Typeface.DEFAULT);
            holder.relativePlaylistTab.setBackgroundResource(R.drawable.playlisttab_normal);
            holder.textPlaylistTab.setPadding(holder.textPlaylistTab.getPaddingLeft(), holder.textPlaylistTab.getPaddingTop(), (int) (16 * activity.getResources().getDisplayMetrics().density + 0.5), holder.textPlaylistTab.getPaddingBottom());
            holder.btnPlaylistMenu.setVisibility(View.GONE);
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
                playlistFragment.showPlaylistMenu(position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }
}
