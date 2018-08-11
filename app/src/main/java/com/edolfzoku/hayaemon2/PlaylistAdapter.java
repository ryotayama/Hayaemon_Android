package com.edolfzoku.hayaemon2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class PlaylistAdapter extends ArrayAdapter<PlaylistItem>
{
    private int resource;
    private List<PlaylistItem> items;
    private LayoutInflater inflater;

    public PlaylistAdapter(Context context, int resource, List<PlaylistItem> items)
    {
        super(context, resource, items);

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

        return view;
    }
}
