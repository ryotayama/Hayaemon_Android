package com.edolfzoku.hayaemon2;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

        TextView textNumber = (TextView)view.findViewById(R.id.textNumber);
        textNumber.setText(item.getNumber());

        TextView textTitle = (TextView)view.findViewById(R.id.textTitle);
        textTitle.setText(item.getTitle());

        TextView textArtist = (TextView)view.findViewById(R.id.textArtist);
        textArtist.setText(item.getArtist());

        RelativeLayout playlistItem = (RelativeLayout)view.findViewById(R.id.playlistItem);
        int nItem = Integer.parseInt(item.getNumber()) - 1;
        PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
        if(nItem == playlistFragment.getPlaying())
            playlistItem.setBackgroundColor(Color.argb(255, 224, 239, 255));
        else if(nItem % 2 == 0)
            playlistItem.setBackgroundColor(Color.argb(255, 247, 247, 247));
        else
            playlistItem.setBackgroundColor(Color.argb(255, 240, 240, 240));

        return view;
    }
}
