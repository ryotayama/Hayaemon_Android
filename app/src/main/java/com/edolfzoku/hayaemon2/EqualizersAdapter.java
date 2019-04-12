/*
 * EqualizersAdapter
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class EqualizersAdapter extends RecyclerView.Adapter<EqualizersAdapter.ViewHolder>
{
    private final MainActivity activity;
    private List<EqualizerItem> items;

    static class ViewHolder extends RecyclerView.ViewHolder {
        final RelativeLayout equalizerItem;
        final TextView textEqualizer;
        final ImageView imgEqualizerMenu;

        ViewHolder(View view) {
            super(view);
            equalizerItem = view.findViewById(R.id.equalizerItem);
            textEqualizer = view.findViewById(R.id.textEqualizer);
            imgEqualizerMenu = view.findViewById(R.id.imgEqualizerMenu);
        }
    }

    EqualizersAdapter(Context context, List<EqualizerItem> items)
    {
        this.activity = (MainActivity)context;
        this.items = items;
    }

    void changeItems(List<EqualizerItem> items)
    {
        this.items = items;
    }

    @Override
    public @NonNull EqualizersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.equalizer_item, parent, false);

        return new EqualizersAdapter.ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final EqualizersAdapter.ViewHolder holder, int position)
    {
        EqualizerItem item = items.get(position);
        String name = item.getEqualizerName();
        holder.textEqualizer.setText(name);

        if(activity.equalizerFragment.isSelectedItem(position))
            holder.itemView.setBackgroundColor(Color.argb(255, 221, 221, 221));
        else
            holder.itemView.setBackgroundColor(Color.argb(255, 255, 255, 255));
        holder.equalizerItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.equalizerFragment.onEqualizerItemClick(holder.getAdapterPosition());
            }
        });

        if(activity.equalizerFragment.isSorting()) {
            holder.imgEqualizerMenu.setOnClickListener(null);
            holder.imgEqualizerMenu.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    activity.equalizerFragment.getEqualizerTouchHelper().startDrag(holder);
                    return true;
                }
            });
            holder.imgEqualizerMenu.setImageResource(R.drawable.ic_sort);
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)holder.imgEqualizerMenu.getLayoutParams();
            param.leftMargin = param.rightMargin = (int) (8 * activity.getResources().getDisplayMetrics().density + 0.5);
        }
        else {
            holder.imgEqualizerMenu.setOnTouchListener(null);
            holder.imgEqualizerMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.equalizerFragment.showMenu(holder.getAdapterPosition());
                }
            });
            holder.imgEqualizerMenu.setImageResource(R.drawable.ic_button_more);
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)holder.imgEqualizerMenu.getLayoutParams();
            param.leftMargin = param.rightMargin = 0;
        }
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }
}
