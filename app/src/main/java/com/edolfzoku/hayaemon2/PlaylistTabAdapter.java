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
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class PlaylistTabAdapter extends RecyclerView.Adapter<PlaylistTabAdapter.ViewHolder>
{
    private final MainActivity activity;
    private final List<String> items;

    static class ViewHolder extends RecyclerView.ViewHolder {
        final RelativeLayout relativePlaylistTab;
        final TextView textPlaylistTab;
        final AnimationButton btnPlaylistMenu;

        ViewHolder(View view) {
            super(view);
            relativePlaylistTab = view.findViewById(R.id.relativePlaylistTab);
            textPlaylistTab = view.findViewById(R.id.textPlaylistTab);
            btnPlaylistMenu = view.findViewById(R.id.btnPlaylistMenu);
        }
    }

    PlaylistTabAdapter(Context context, List<String> items)
    {
        this.activity = (MainActivity)context;
        this.items = items;
    }

    @Override
    public @NonNull PlaylistTabAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlist_tab_item, parent, false);

        return new PlaylistTabAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PlaylistTabAdapter.ViewHolder holder, int position)
    {
        String item = items.get(position);

        holder.textPlaylistTab.setLongClickable(true);

        holder.textPlaylistTab.setText(item);
        if(position == activity.playlistFragment.getSelectedPlaylist()) {
            holder.textPlaylistTab.setContentDescription(item + "、選択ずみ");
            holder.textPlaylistTab.setTypeface(Typeface.DEFAULT_BOLD);
            holder.relativePlaylistTab.setBackgroundResource(R.drawable.playlisttab_select);
            holder.textPlaylistTab.setPadding(holder.textPlaylistTab.getPaddingLeft(), holder.textPlaylistTab.getPaddingTop(), 0, holder.textPlaylistTab.getPaddingBottom());
            holder.btnPlaylistMenu.setVisibility(View.VISIBLE);
            holder.btnPlaylistMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.playlistFragment.showPlaylistMenu(holder.getAdapterPosition());
                }
            });
        }
        else if(activity.playlistFragment.getPlayingPlaylist() == position && activity.playlistFragment.getPlaying() != -1) {
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
                activity.playlistFragment.selectPlaylist(holder.getAdapterPosition());
            }
        });

        holder.textPlaylistTab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                activity.playlistFragment.showPlaylistMenu(holder.getAdapterPosition());
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
