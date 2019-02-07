package com.edolfzoku.hayaemon2;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class BottomMenu extends BottomSheetDialog
{
    LinearLayout linearLayout;
    ScrollView scroll;
    int nTag = 0;

    public BottomMenu(@NonNull Context context)
    {
        super(context);
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        scroll = new ScrollView(context);
        scroll.addView(linearLayout);
        setContentView(scroll);
        BottomSheetBehavior behavior = BottomSheetBehavior.from((View)scroll.getParent());
        behavior.setPeekHeight(context.getResources().getDisplayMetrics().heightPixels);
    }

    public void setTitle(String strTitle)
    {
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView textTitle = new TextView (getContext());
        textTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        textTitle.setGravity(Gravity.CENTER);
        textTitle.setText(strTitle);
        textTitle.setHeight((int)(48 *  getContext().getResources().getDisplayMetrics().density + 0.5));
        linearLayout.addView(textTitle, param);

        View viewSep = new View(getContext());
        viewSep.setBackgroundColor(Color.argb(255, 208, 208, 208));
        LinearLayout.LayoutParams paramViewSep = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramViewSep.topMargin = 0;
        paramViewSep.bottomMargin = (int)(8 *  getContext().getResources().getDisplayMetrics().density + 0.5);
        paramViewSep.leftMargin = (int)(16 *  getContext().getResources().getDisplayMetrics().density + 0.5);
        paramViewSep.rightMargin = (int)(16 *  getContext().getResources().getDisplayMetrics().density + 0.5);
        paramViewSep.height = (int)(0.5 *  getContext().getResources().getDisplayMetrics().density + 0.5);
        linearLayout.addView(viewSep, paramViewSep);
    }

    public void addSeparator()
    {
        View viewSep = new View(getContext());
        viewSep.setBackgroundColor(Color.argb(255, 208, 208, 208));
        LinearLayout.LayoutParams paramViewSep = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramViewSep.topMargin = (int)(8 *  getContext().getResources().getDisplayMetrics().density + 0.5);
        paramViewSep.bottomMargin = (int)(8 *  getContext().getResources().getDisplayMetrics().density + 0.5);
        paramViewSep.leftMargin = (int)(16 *  getContext().getResources().getDisplayMetrics().density + 0.5);
        paramViewSep.rightMargin = (int)(16 *  getContext().getResources().getDisplayMetrics().density + 0.5);
        paramViewSep.height = (int)(0.5 *  getContext().getResources().getDisplayMetrics().density + 0.5);
        linearLayout.addView(viewSep, paramViewSep);
    }

    public void addMenu(String strText, int resID, android.view.View.OnClickListener listener)
    {
        RelativeLayout relativeLocal = new RelativeLayout(getContext());
        relativeLocal.setTag(nTag);
        nTag += 1;
        ImageView imgLocal = new ImageView(getContext());
        imgLocal.setImageResource(resID);
        imgLocal.setId(1000);
        RelativeLayout.LayoutParams paramImgLocal = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramImgLocal.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        paramImgLocal.addRule(RelativeLayout.CENTER_VERTICAL);
        paramImgLocal.leftMargin = (int)(18 *  getContext().getResources().getDisplayMetrics().density + 0.5);
        paramImgLocal.rightMargin = (int)(8 *  getContext().getResources().getDisplayMetrics().density + 0.5);
        relativeLocal.addView(imgLocal, paramImgLocal);

        TextView textLocal = new TextView (getContext());
        textLocal.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textLocal.setGravity(Gravity.CENTER);
        textLocal.setText(strText);
        textLocal.setTextColor(Color.argb(255, 0, 0, 0));
        textLocal.setHeight((int)(48 *  getContext().getResources().getDisplayMetrics().density + 0.5));
        relativeLocal.setOnClickListener(listener);
        RelativeLayout.LayoutParams paramTextLocal = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramTextLocal.addRule(RelativeLayout.RIGHT_OF, imgLocal.getId());
        paramTextLocal.addRule(RelativeLayout.CENTER_VERTICAL);
        relativeLocal.addView(textLocal, paramTextLocal);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.addView(relativeLocal, param);
    }

    public void addDestructiveMenu(String strText, int resID, android.view.View.OnClickListener listener)
    {
        RelativeLayout relativeLocal = new RelativeLayout(getContext());
        ImageView imgLocal = new ImageView(getContext());
        imgLocal.setImageResource(resID);
        imgLocal.setId(1000);
        RelativeLayout.LayoutParams paramImgLocal = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramImgLocal.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        paramImgLocal.addRule(RelativeLayout.CENTER_VERTICAL);
        paramImgLocal.leftMargin = (int)(18 *  getContext().getResources().getDisplayMetrics().density + 0.5);
        paramImgLocal.rightMargin = (int)(8 *  getContext().getResources().getDisplayMetrics().density + 0.5);
        relativeLocal.addView(imgLocal, paramImgLocal);

        TextView textLocal = new TextView (getContext());
        textLocal.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textLocal.setGravity(Gravity.CENTER);
        textLocal.setText(strText);
        textLocal.setTextColor(Color.argb(255, 255, 0, 0));
        textLocal.setHeight((int)(48 *  getContext().getResources().getDisplayMetrics().density + 0.5));
        relativeLocal.setOnClickListener(listener);
        RelativeLayout.LayoutParams paramTextLocal = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramTextLocal.addRule(RelativeLayout.RIGHT_OF, imgLocal.getId());
        paramTextLocal.addRule(RelativeLayout.CENTER_VERTICAL);
        relativeLocal.addView(textLocal, paramTextLocal);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.addView(relativeLocal, param);
    }

    public void setCancelMenu()
    {
        View viewSep = new View(getContext());
        viewSep.setBackgroundColor(Color.argb(255, 208, 208, 208));
        LinearLayout.LayoutParams paramViewSep = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramViewSep.topMargin = (int)(8 *  getContext().getResources().getDisplayMetrics().density + 0.5);
        paramViewSep.bottomMargin = 0;
        paramViewSep.leftMargin = (int)(16 *  getContext().getResources().getDisplayMetrics().density + 0.5);
        paramViewSep.rightMargin = (int)(16 *  getContext().getResources().getDisplayMetrics().density + 0.5);
        paramViewSep.height = (int)(0.5 *  getContext().getResources().getDisplayMetrics().density + 0.5);
        linearLayout.addView(viewSep, paramViewSep);

        TextView textCancel = new TextView (getContext());
        textCancel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textCancel.setGravity(Gravity.CENTER);
        textCancel.setText("キャンセル");
        textCancel.setHeight((int)(48 *  getContext().getResources().getDisplayMetrics().density + 0.5));
        textCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.addView(textCancel, param);
    }
}
