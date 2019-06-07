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
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class EqualizersAdapter extends RecyclerView.Adapter<EqualizersAdapter.ViewHolder>
{
    private final MainActivity mActivity;
    private List<EqualizerItem> mItems;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout mEqualizerItem;
        private TextView mTextEqualizer;
        private ImageButton mBtnEqualizerDetail;
        private ImageView mImgEqualizerRight;
        private ImageView mImgEqualizerMenu;

        RelativeLayout getEqualizerItem() { return mEqualizerItem; }
        TextView getTextEqualizer() { return mTextEqualizer; }
        ImageButton getBtnEqualizerDetail() { return mBtnEqualizerDetail; }
        ImageView getImgEqualizerRight() { return mImgEqualizerRight; }
        ImageView getImgEqualizerMenu() { return mImgEqualizerMenu; }

        ViewHolder(View view) {
            super(view);
            mEqualizerItem = view.findViewById(R.id.equalizerItem);
            mTextEqualizer = view.findViewById(R.id.textEqualizer);
            mBtnEqualizerDetail = view.findViewById(R.id.btnEqualizerDetail);
            mImgEqualizerRight = view.findViewById(R.id.imgEqualizerRight);
            mImgEqualizerMenu = view.findViewById(R.id.imgEqualizerMenu);
        }
    }

    EqualizersAdapter(Context context, List<EqualizerItem> items)
    {
        mActivity = (MainActivity)context;
        mItems = items;
    }

    void changeItems(List<EqualizerItem> items)
    {
        mItems = items;
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
        EqualizerItem item = mItems.get(position);
        String name = item.getEqualizerName();
        holder.getTextEqualizer().setText(name);

        if(mActivity.equalizerFragment.isSelectedItem(position))
            holder.itemView.setBackgroundColor(Color.argb(255, 221, 221, 221));
        else
            holder.itemView.setBackgroundColor(Color.argb(255, 255, 255, 255));
        holder.getEqualizerItem().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.equalizerFragment.onEqualizerItemClick(holder.getAdapterPosition());
            }
        });

        if(mActivity.equalizerFragment.isSorting()) {
            holder.getImgEqualizerMenu().setOnClickListener(null);
            holder.getImgEqualizerMenu().setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    mActivity.equalizerFragment.getEqualizerTouchHelper().startDrag(holder);
                    return true;
                }
            });
            holder.getImgEqualizerMenu().setImageResource(R.drawable.ic_sort);
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)holder.getImgEqualizerMenu().getLayoutParams();
            param.leftMargin = param.rightMargin = (int) (8 * mActivity.getDensity());
            holder.getBtnEqualizerDetail().setVisibility(View.GONE);
            holder.getImgEqualizerRight().setVisibility(View.GONE);
        }
        else {
            holder.getBtnEqualizerDetail().setVisibility(View.VISIBLE);
            holder.getImgEqualizerRight().setVisibility(View.VISIBLE);
            holder.getBtnEqualizerDetail().setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    if(event.getAction() == MotionEvent.ACTION_DOWN)
                        holder.getBtnEqualizerDetail().setColorFilter(new PorterDuffColorFilter(Color.parseColor("#ffcce4ff"), PorterDuff.Mode.SRC_IN));
                    else if(event.getAction() == MotionEvent.ACTION_UP)
                        holder.getBtnEqualizerDetail().setColorFilter(null);
                    else if(event.getAction() == MotionEvent.ACTION_CANCEL)
                        holder.getBtnEqualizerDetail().setColorFilter(null);
                    return false;
                }
            });
            holder.getBtnEqualizerDetail().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.equalizerFragment.onEqualizerDetailClick(holder.getAdapterPosition());
                }
            });
            holder.getImgEqualizerMenu().setOnTouchListener(null);
            holder.getImgEqualizerMenu().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.equalizerFragment.showMenu(holder.getAdapterPosition());
                }
            });
            holder.getImgEqualizerMenu().setImageResource(R.drawable.ic_button_more);
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)holder.getImgEqualizerMenu().getLayoutParams();
            param.leftMargin = param.rightMargin = 0;
        }
    }

    @Override
    public int getItemCount()
    {
        return mItems.size();
    }
}
