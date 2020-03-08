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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
        private AnimationButton mBtnEqualizerMenu;
        private View mViewSepEqualizer;

        RelativeLayout getEqualizerItem() { return mEqualizerItem; }
        TextView getTextEqualizer() { return mTextEqualizer; }
        ImageButton getBtnEqualizerDetail() { return mBtnEqualizerDetail; }
        ImageView getImgEqualizerRight() { return mImgEqualizerRight; }
        AnimationButton getBtnEqualizerMenu() { return mBtnEqualizerMenu; }
        View getViewSepEqualizer() { return mViewSepEqualizer; }

        ViewHolder(View view) {
            super(view);
            mEqualizerItem = view.findViewById(R.id.equalizerItem);
            mTextEqualizer = view.findViewById(R.id.textEqualizer);
            mBtnEqualizerDetail = view.findViewById(R.id.btnEqualizerDetail);
            mImgEqualizerRight = view.findViewById(R.id.imgEqualizerRight);
            mBtnEqualizerMenu = view.findViewById(R.id.btnEqualizerMenu);
            mViewSepEqualizer = view.findViewById(R.id.viewSepEqualizer);
        }
    }

    EqualizersAdapter(Context context, List<EqualizerItem> items) {
        mActivity = (MainActivity)context;
        mItems = items;
    }

    void changeItems(List<EqualizerItem> items) {
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
    public void onBindViewHolder(@NonNull final EqualizersAdapter.ViewHolder holder, int position) {
        holder.getBtnEqualizerDetail().setBackgroundResource(mActivity.isDarkMode() ? R.drawable.ic_button_info_dark : R.drawable.ic_button_info);
        holder.getImgEqualizerRight().setImageResource(mActivity.isDarkMode() ? R.drawable.ic_button_listright_dark : R.drawable.ic_button_listright);
        holder.getViewSepEqualizer().setBackgroundColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeSep : R.color.lightModeSep));
        holder.getTextEqualizer().setTextColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? android.R.color.white : android.R.color.black));

        EqualizerItem item = mItems.get(position);
        String name = item.getEqualizerName();
        holder.getTextEqualizer().setText(name);

        if(EqualizerFragment.isSelectedItem(position))
            holder.itemView.setBackgroundColor(mActivity.isDarkMode() ? mActivity.getResources().getColor(R.color.darkModeSelect) : Color.argb(255, 224, 239, 255));
        else
            holder.itemView.setBackgroundColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeBk : R.color.lightModeBk));
        holder.getEqualizerItem().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.equalizerFragment.onEqualizerItemClick(holder.getAdapterPosition());
            }
        });

        if(mActivity.equalizerFragment.isSorting()) {
            holder.getBtnEqualizerMenu().setOnClickListener(null);
            holder.getBtnEqualizerMenu().setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    mActivity.equalizerFragment.getEqualizerTouchHelper().startDrag(holder);
                    return true;
                }
            });
            holder.getBtnEqualizerMenu().setImageResource(R.drawable.ic_sort);
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)holder.getBtnEqualizerMenu().getLayoutParams();
            param.leftMargin = param.rightMargin = (int) (8 * mActivity.getDensity());
            holder.getBtnEqualizerDetail().setVisibility(View.GONE);
            holder.getImgEqualizerRight().setVisibility(View.GONE);
        }
        else {
            holder.getBtnEqualizerDetail().setVisibility(View.VISIBLE);
            holder.getImgEqualizerRight().setVisibility(View.VISIBLE);
            holder.getBtnEqualizerDetail().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.equalizerFragment.onEqualizerDetailClick(holder.getAdapterPosition());
                }
            });
            holder.getBtnEqualizerMenu().setOnTouchListener(null);
            holder.getBtnEqualizerMenu().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.equalizerFragment.showMenu(holder.getAdapterPosition());
                }
            });
            holder.getBtnEqualizerMenu().setImageResource(R.drawable.ic_button_more);
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)holder.getBtnEqualizerMenu().getLayoutParams();
            param.leftMargin = param.rightMargin = 0;
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
