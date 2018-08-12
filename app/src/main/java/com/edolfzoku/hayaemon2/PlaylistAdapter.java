package com.edolfzoku.hayaemon2;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.un4seen.bass.BASS;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder>
{
    MainActivity activity;
    private int resource;
    private List<PlaylistItem> items;
    private LayoutInflater inflater;

    public String getTitle(int nPosition)
    {
        PlaylistItem item = items.get(nPosition);
        return item.getTitle();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout playlistItem;
        TextView textNumber;
        TextView textTitle;
        TextView textArtist;
        ImageView imgStatus;
        FrameLayout frameSongMenu;

        ViewHolder(View view) {
            super(view);
            playlistItem = (RelativeLayout) view.findViewById(R.id.playlistItem);
            textNumber = (TextView) view.findViewById(R.id.textNumber);
            textTitle = (TextView) view.findViewById(R.id.textTitle);
            textArtist = (TextView) view.findViewById(R.id.textArtist);
            imgStatus = (ImageView) view.findViewById(R.id.imgStatus);
            frameSongMenu = (FrameLayout) view.findViewById(R.id.frameSongMenu);
        }
    }

    public PlaylistAdapter(Context context, int resource, List<PlaylistItem> items)
    {
        this.activity = (MainActivity)context;
        this.resource = resource;
        this.items = items;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(resource, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        PlaylistItem item = items.get(position);
        final int nItem = Integer.parseInt(item.getNumber()) - 1;
        final PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);

        holder.itemView.setLongClickable(true);

        holder.textNumber.setText(item.getNumber());
        holder.textTitle.setText(item.getTitle());
        holder.textArtist.setText(item.getArtist());
        if(nItem == playlistFragment.getPlaying()) {
            if(BASS.BASS_ChannelIsActive(MainActivity.hStream) == BASS.BASS_ACTIVE_PAUSED)
                holder.imgStatus.setImageResource(R.drawable.pause_circle);
            else
                holder.imgStatus.setImageResource(R.drawable.circle_music);
            holder.textNumber.setVisibility(View.INVISIBLE);
        }
        else {
            holder.imgStatus.setImageDrawable(null);
            holder.textNumber.setVisibility(View.VISIBLE);
        }

        playlistFragment.registerForContextMenu(holder.playlistItem);
        playlistFragment.registerForContextMenu(holder.frameSongMenu);
        final ViewHolder _holder = holder;
        holder.playlistItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playlistFragment.playSong(nItem);
            }
        });
        holder.frameSongMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _holder.frameSongMenu.showContextMenu();
            }
        });

        if(nItem == playlistFragment.getPlaying())
            holder.playlistItem.setBackgroundColor(Color.argb(255, 224, 239, 255));
        else if(nItem % 2 == 0)
            holder.playlistItem.setBackgroundColor(Color.argb(255, 247, 247, 247));
        else
            holder.playlistItem.setBackgroundColor(Color.argb(255, 240, 240, 240));
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }
}
