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
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.ViewHolder>
{
    private final MainActivity mActivity;
    private final List<String> mItems;

    static class ViewHolder extends RecyclerView.ViewHolder {
        final RelativeLayout playlistItem;
        final TextView textName;
        final TextView textSongCount;
        final ImageView imgRight;
        final ImageView imgPlaylistMenu;
        final View viewSepPlaylist;

        ViewHolder(View view) {
            super(view);
            playlistItem = view.findViewById(R.id.playlistItem);
            textName = view.findViewById(R.id.textName);
            textSongCount = view.findViewById(R.id.textSongCount);
            imgRight = view.findViewById(R.id.imgRight);
            imgPlaylistMenu = view.findViewById(R.id.imgPlaylistMenu);
            viewSepPlaylist = view.findViewById(R.id.viewSepPlaylist);
        }
    }

    PlaylistsAdapter(Context context, List<String> items)
    {
        mActivity = (MainActivity)context;
        mItems = items;
    }

    @Override
    public @NonNull PlaylistsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlist_item, parent, false);

        return new PlaylistsAdapter.ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final PlaylistsAdapter.ViewHolder holder, int position)
    {
        holder.viewSepPlaylist.setBackgroundColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeSep : R.color.lightModeSep));

        String item = mItems.get(position);

        holder.textName.setText(item);
        holder.textName.setTextColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? android.R.color.white : android.R.color.black));
        holder.textSongCount.setText(String.format(Locale.getDefault(), "%d曲", mActivity.playlistFragment.getSongCount(position)));
        holder.textSongCount.setTextColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeGray : R.color.lightModeGray));

        holder.playlistItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.playlistFragment.onPlaylistItemClick(holder.getAdapterPosition());
            }
        });
        if(mActivity.playlistFragment.isSorting()) {
            holder.imgPlaylistMenu.setOnClickListener(null);
            holder.playlistItem.setOnLongClickListener(null);
            holder.imgPlaylistMenu.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    mActivity.playlistFragment.getPlaylistTouchHelper().startDrag(holder);
                    return true;
                }
            });
            holder.imgPlaylistMenu.setImageResource(R.drawable.ic_sort);
            holder.imgRight.setVisibility(View.GONE);
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)holder.imgPlaylistMenu.getLayoutParams();
            param.leftMargin = param.rightMargin = (int) (8 * mActivity.getDensity());
        }
        else {
            holder.imgPlaylistMenu.setOnTouchListener(null);
            holder.imgPlaylistMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.playlistFragment.showPlaylistMenu(holder.getAdapterPosition());
                }
            });
            holder.playlistItem.setOnLongClickListener(new View.OnLongClickListener()
            {
                public boolean onLongClick(View v) {
                    mActivity.playlistFragment.showPlaylistMenu(holder.getAdapterPosition());
                    return true;
                }
            });
            holder.imgPlaylistMenu.setImageResource(R.drawable.ic_button_more);
            holder.imgRight.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)holder.imgPlaylistMenu.getLayoutParams();
            param.leftMargin = param.rightMargin = 0;
        }

        if(mActivity.playlistFragment.getPlayingPlaylist() == position && mActivity.playlistFragment.getPlaying() != -1)
            holder.playlistItem.setBackgroundColor(mActivity.isDarkMode() ? mActivity.getResources().getColor(R.color.darkModeSelect) : Color.argb(255, 224, 239, 255));
        else
            holder.playlistItem.setBackgroundColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeBk : R.color.lightModeBk));
    }

    @Override
    public int getItemCount()
    {
        return mItems.size();
    }
}
