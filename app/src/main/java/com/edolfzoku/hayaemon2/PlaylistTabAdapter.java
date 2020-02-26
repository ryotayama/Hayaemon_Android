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
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class PlaylistTabAdapter extends RecyclerView.Adapter<PlaylistTabAdapter.ViewHolder>
{
    private final MainActivity mActivity;
    private final List<String> mItems;

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
        mActivity = (MainActivity)context;
        mItems = items;
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
        String item = mItems.get(position);

        holder.textPlaylistTab.setLongClickable(true);

        holder.textPlaylistTab.setText(item);
        holder.textPlaylistTab.setTextColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? android.R.color.white : android.R.color.black));
        if(position == PlaylistFragment.sSelectedPlaylist) {
            holder.textPlaylistTab.setContentDescription(item + "、選択ずみ");
            holder.textPlaylistTab.setTypeface(Typeface.DEFAULT_BOLD);
            holder.relativePlaylistTab.setBackgroundResource(mActivity.isDarkMode() ? R.drawable.playlisttab_select_dark : R.drawable.playlisttab_select);
            holder.textPlaylistTab.setPadding(holder.textPlaylistTab.getPaddingLeft(), holder.textPlaylistTab.getPaddingTop(), 0, holder.textPlaylistTab.getPaddingBottom());
            holder.btnPlaylistMenu.setVisibility(View.VISIBLE);
            holder.btnPlaylistMenu.setImageResource(mActivity.isDarkMode() ? R.drawable.ic_button_more_blue_dark : R.drawable.ic_button_more_blue);
            holder.btnPlaylistMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.playlistFragment.showPlaylistTabMenu(holder.getAdapterPosition());
                }
            });
        }
        else if(PlaylistFragment.sPlayingPlaylist == position && PlaylistFragment.sPlaying != -1) {
            holder.textPlaylistTab.setContentDescription(item);
            holder.textPlaylistTab.setTypeface(Typeface.DEFAULT);
            holder.relativePlaylistTab.setBackgroundResource(mActivity.isDarkMode() ? R.drawable.playlisttab_play_dark : R.drawable.playlisttab_play);
            holder.textPlaylistTab.setPadding(holder.textPlaylistTab.getPaddingLeft(), holder.textPlaylistTab.getPaddingTop(), (int) (16 * mActivity.getDensity()), holder.textPlaylistTab.getPaddingBottom());
            holder.btnPlaylistMenu.setVisibility(View.GONE);
        }
        else {
            holder.textPlaylistTab.setContentDescription(item);
            holder.textPlaylistTab.setTypeface(Typeface.DEFAULT);
            holder.relativePlaylistTab.setBackgroundResource(mActivity.isDarkMode() ? R.drawable.playlisttab_normal_dark : R.drawable.playlisttab_normal);
            holder.textPlaylistTab.setPadding(holder.textPlaylistTab.getPaddingLeft(), holder.textPlaylistTab.getPaddingTop(), (int) (16 * mActivity.getDensity()), holder.textPlaylistTab.getPaddingBottom());
            holder.btnPlaylistMenu.setVisibility(View.GONE);
        }

        holder.textPlaylistTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.playlistFragment.selectPlaylist(holder.getAdapterPosition());
            }
        });

        holder.textPlaylistTab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mActivity.playlistFragment.showPlaylistTabMenu(holder.getAdapterPosition());
                return true;
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return mItems.size();
    }
}
