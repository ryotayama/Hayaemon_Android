package com.edolfzoku.hayaemon2;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.un4seen.bass.BASS;

import java.util.List;

public class PlaylistAdapter extends ArrayAdapter<PlaylistItem>
{
    MainActivity activity;
    private int resource;
    private List<PlaylistItem> items;
    private LayoutInflater inflater;

    public PlaylistAdapter(Context context, int resource, List<PlaylistItem> items)
    {
        super(context, resource, items);

        this.activity = (MainActivity)context;
        this.resource = resource;
        this.items = items;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view;

        if (convertView != null) {
            view = convertView;
        }
        else {
            view = inflater.inflate(resource, null);
        }

        PlaylistItem item = items.get(position);
        int nItem = Integer.parseInt(item.getNumber()) - 1;
        PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);

        TextView textNumber = (TextView)view.findViewById(R.id.textNumber);
        textNumber.setText(item.getNumber());

        TextView textTitle = (TextView)view.findViewById(R.id.textTitle);
        textTitle.setText(item.getTitle());

        TextView textArtist = (TextView)view.findViewById(R.id.textArtist);
        textArtist.setText(item.getArtist());

        ImageView imgStatus = (ImageView)view.findViewById(R.id.imgStatus);
        if(nItem == playlistFragment.getPlaying()) {
            if(BASS.BASS_ChannelIsActive(MainActivity.hStream) == BASS.BASS_ACTIVE_PAUSED)
                imgStatus.setImageResource(R.drawable.pause_circle);
            else
                imgStatus.setImageResource(R.drawable.circle_music);
            textNumber.setVisibility(View.INVISIBLE);
        }
        else {
            imgStatus.setImageDrawable(null);
            textNumber.setVisibility(View.VISIBLE);
        }

        final FrameLayout frameSongMenu = (FrameLayout)view.findViewById(R.id.frameSongMenu);
        playlistFragment.registerForContextMenu(frameSongMenu);
        frameSongMenu.setOnClickListener(new View.OnClickListener() {
           @Override
            public void onClick(View v) {
                frameSongMenu.showContextMenu();
            }
        });

        RelativeLayout playlistItem = (RelativeLayout)view.findViewById(R.id.playlistItem);
        if(nItem == playlistFragment.getPlaying())
            playlistItem.setBackgroundColor(Color.argb(255, 224, 239, 255));
        else if(nItem % 2 == 0)
            playlistItem.setBackgroundColor(Color.argb(255, 247, 247, 247));
        else
            playlistItem.setBackgroundColor(Color.argb(255, 240, 240, 240));

        return view;
    }
}
