/*
 * EffectsAdapter
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

public class EffectsAdapter extends RecyclerView.Adapter<EffectsAdapter.ViewHolder>
{
    private final MainActivity mActivity;
    private final List<EffectItem> mItems;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout mEffectItem;
        private TextView mTextEffect;
        private ImageButton mBtnEffectDetail;
        private ImageView mImgRight;
        private View mViewSepEffect;

        RelativeLayout getEffectItem() { return mEffectItem; }
        TextView getTextEffect() { return mTextEffect; }
        ImageButton getButtonEffectDetail() { return mBtnEffectDetail; }
        ImageView getImgRight() { return mImgRight; }
        View getViewSepEffect() { return mViewSepEffect; }

        ViewHolder(View view) {
            super(view);
            mEffectItem = view.findViewById(R.id.effectItem);
            mTextEffect = view.findViewById(R.id.textEffect);
            mBtnEffectDetail = view.findViewById(R.id.btnEffectDetail);
            mImgRight = view.findViewById(R.id.imgRight);
            mViewSepEffect = view.findViewById(R.id.viewSepEffect);
        }
    }

    EffectsAdapter(Context context, List<EffectItem> items)
    {
        mActivity = (MainActivity)context;
        this.mItems = items;
    }

    @Override
    public @NonNull EffectsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.effect_item, parent, false);

        return new EffectsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final EffectsAdapter.ViewHolder holder, int position)
    {
        holder.getButtonEffectDetail().setBackgroundResource(mActivity.isDarkMode() ? R.drawable.ic_button_info_dark : R.drawable.ic_button_info);
        holder.getImgRight().setImageResource(mActivity.isDarkMode() ? R.drawable.ic_button_listright_dark : R.drawable.ic_button_listright);
        holder.getViewSepEffect().setBackgroundColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeSep : R.color.lightModeSep));
        holder.getTextEffect().setTextColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeGray : R.color.lightModeGray));

        EffectItem item = mItems.get(position);
        String name = item.getEffectName();
        holder.getTextEffect().setText(name);

        if(mActivity.effectFragment.isSelectedItem(position))
            holder.itemView.setBackgroundColor(mActivity.isDarkMode() ? mActivity.getResources().getColor(R.color.darkModeSelect) : Color.argb(255, 224, 239, 255));
        else
            holder.itemView.setBackgroundColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeBk : R.color.lightModeBk));
        holder.getEffectItem().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.effectFragment.onEffectItemClick(holder.getAdapterPosition());
            }
        });

        if(!item.isEditEnabled()) {
            holder.getButtonEffectDetail().setVisibility(View.GONE);
            holder.getImgRight().setVisibility(View.GONE);
        }
        else {
            holder.getButtonEffectDetail().setVisibility(View.VISIBLE);
            holder.getImgRight().setVisibility(View.VISIBLE);
            holder.getButtonEffectDetail().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.effectFragment.onEffectDetailClick(holder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount()
    {
        return mItems.size();
    }
}
