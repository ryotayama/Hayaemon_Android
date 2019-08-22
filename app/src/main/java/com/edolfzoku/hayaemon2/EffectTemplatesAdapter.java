/*
 * EffectTemplatesAdapter
 *
 * Copyright (c) 2019 Ryota Yamauchi. All rights reserved.
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

public class EffectTemplatesAdapter extends RecyclerView.Adapter<EffectTemplatesAdapter.ViewHolder>
{
    private final MainActivity mActivity;
    private List<EffectTemplateItem> mItems;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout mEffectTemplateItem;
        private TextView mTextEffectTemplate;
        private RelativeLayout mRelativeEffectTemplateDetail;
        private ImageButton mBtnEffectTemplateDetail;
        private ImageView mImgEffectTemplateRight;
        private ImageView mImgEffectTemplateMenu;
        private View mViewSepEffectTemplate;

        RelativeLayout getEffectTemplateItem() { return mEffectTemplateItem; }
        TextView getTextEffectTemplate() { return mTextEffectTemplate; }
        RelativeLayout getRelativeEffectTemplateDetail() { return mRelativeEffectTemplateDetail; }
        ImageButton getBtnEffectTemplateDetail() { return mBtnEffectTemplateDetail; }
        ImageView getImgEffectTemplateRight() { return mImgEffectTemplateRight; }
        ImageView getImgEffectTemplateMenu() { return mImgEffectTemplateMenu; }
        View getViewSepEffectTemplate() { return mViewSepEffectTemplate; }

        ViewHolder(View view) {
            super(view);
            mEffectTemplateItem = view.findViewById(R.id.effectTemplateItem);
            mTextEffectTemplate = view.findViewById(R.id.textEffectTemplate);
            mRelativeEffectTemplateDetail = view.findViewById(R.id.relativeEffectTemplateDetail);
            mBtnEffectTemplateDetail = view.findViewById(R.id.btnEffectTemplateDetail);
            mImgEffectTemplateRight = view.findViewById(R.id.imgEffectTemplateRight);
            mImgEffectTemplateMenu = view.findViewById(R.id.imgEffectTemplateMenu);
            mViewSepEffectTemplate = view.findViewById(R.id.viewSepEffectTemplate);
        }
    }

    EffectTemplatesAdapter(Context context, List<EffectTemplateItem> items)
    {
        mActivity = (MainActivity)context;
        mItems = items;
    }

    void changeItems(List<EffectTemplateItem> items)
    {
        mItems = items;
    }

    @Override
    public @NonNull EffectTemplatesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.effect_template_item, parent, false);

        return new EffectTemplatesAdapter.ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final EffectTemplatesAdapter.ViewHolder holder, int position)
    {
        holder.getBtnEffectTemplateDetail().setBackgroundResource(mActivity.isDarkMode() ? R.drawable.ic_button_info_dark : R.drawable.ic_button_info);
        holder.getImgEffectTemplateRight().setImageResource(mActivity.isDarkMode() ? R.drawable.ic_button_listright_dark : R.drawable.ic_button_listright);
        holder.getViewSepEffectTemplate().setBackgroundColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeSep : R.color.lightModeSep));
        holder.getTextEffectTemplate().setTextColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeGray : R.color.lightModeGray));

        EffectTemplateItem item = mItems.get(position);
        String name = item.getEffectTemplateName();
        holder.getTextEffectTemplate().setText(name);

        if(mActivity.effectFragment.isSelectedTemplateItem(position))
            holder.itemView.setBackgroundColor(mActivity.isDarkMode() ? mActivity.getResources().getColor(R.color.darkModeSelect) : Color.argb(255, 224, 239, 255));
        else
            holder.itemView.setBackgroundColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeBk : R.color.lightModeBk));
        holder.getEffectTemplateItem().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.effectFragment.onEffectTemplateItemClick(holder.getAdapterPosition());
            }
        });
        RelativeLayout.LayoutParams paramRelative = (RelativeLayout.LayoutParams)holder.getRelativeEffectTemplateDetail().getLayoutParams();
        if(mActivity.effectFragment.isSorting()) {
            holder.getImgEffectTemplateMenu().setOnClickListener(null);
            holder.getImgEffectTemplateMenu().setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    mActivity.effectFragment.getEffectTemplateTouchHelper().startDrag(holder);
                    return true;
                }
            });
            holder.getImgEffectTemplateMenu().setImageResource(R.drawable.ic_sort);
            holder.getImgEffectTemplateMenu().setVisibility(View.VISIBLE);
            paramRelative.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)holder.getImgEffectTemplateMenu().getLayoutParams();
            param.leftMargin = param.rightMargin = (int) (8 * mActivity.getDensity());
            holder.getBtnEffectTemplateDetail().setVisibility(View.GONE);
            holder.getImgEffectTemplateRight().setVisibility(View.GONE);
        }
        else {
            holder.getBtnEffectTemplateDetail().setVisibility(View.VISIBLE);
            holder.getImgEffectTemplateRight().setVisibility(View.VISIBLE);
            holder.getBtnEffectTemplateDetail().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.effectFragment.onEffectCustomizeClick(holder.getAdapterPosition());
                }
            });
            holder.getImgEffectTemplateMenu().setOnTouchListener(null);
            holder.getImgEffectTemplateMenu().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.effectFragment.showMenu(holder.getAdapterPosition());
                }
            });
            if(mActivity.effectFragment.getEffectDetail() == EffectFragment.EFFECTTYPE_SOUNDEFFECT) {
                holder.getImgEffectTemplateMenu().setVisibility(View.GONE);
                paramRelative.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
            }
            else {
                holder.getImgEffectTemplateMenu().setImageResource(R.drawable.ic_button_more);
                holder.getImgEffectTemplateMenu().setVisibility(View.VISIBLE);
                paramRelative.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            }
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)holder.getImgEffectTemplateMenu().getLayoutParams();
            param.leftMargin = param.rightMargin = 0;
        }
    }

    @Override
    public int getItemCount()
    {
        return mItems.size();
    }
}
