package com.edolfzoku.hayaemon2;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class BottomMenu extends BottomSheetDialog {
    LinearLayout linearLayoutParent;
    LinearLayout linearLayout;
    ScrollView scroll;
    int nTag = 0;
    int nHeight = 0;

    public BottomMenu(@NonNull Context context) {
        super(context);
        linearLayoutParent = new LinearLayout(context);
        linearLayoutParent.setOrientation(LinearLayout.VERTICAL);
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        scroll = new ScrollView(context);
        scroll.addView(linearLayout);
        scroll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    setCancelable(true);
                    for(int i = 0; i < linearLayout.getChildCount(); i++) {
                        View childView = linearLayout.getChildAt(i);
                        if(childView.getHeight() != (int) (0.5 * getContext().getResources().getDisplayMetrics().density + 0.5))
                            childView.setBackgroundColor(Color.argb(255, 255, 255, 255));
                    }
                }
                return false;
            }
        });
        setContentView(linearLayoutParent);
        BottomSheetBehavior behavior = BottomSheetBehavior.from((View) linearLayoutParent.getParent());
        behavior.setPeekHeight(context.getResources().getDisplayMetrics().heightPixels - getStatusBarHeight() - (int) (16.0 * getContext().getResources().getDisplayMetrics().density + 0.5));
        setDialogBorder(this);
    }

    public int getStatusBarHeight(){
        final Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.top;
    }

    public void setDialogBorder(Dialog dialog) {
        FrameLayout bottomSheet = (FrameLayout) dialog.getWindow().findViewById(android.support.design.R.id.design_bottom_sheet);
        setMargins(bottomSheet, (int) (16 * getContext().getResources().getDisplayMetrics().density + 0.5), 0, (int) (16 * getContext().getResources().getDisplayMetrics().density + 0.5), 0);
    }

    private void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    public void setTitle(String strTitle) {
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        param.leftMargin = (int) (16 * getContext().getResources().getDisplayMetrics().density + 0.5);
        param.rightMargin = (int) (16 * getContext().getResources().getDisplayMetrics().density + 0.5);
        TextView textTitle = new TextView(getContext());
        textTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        textTitle.setGravity(Gravity.CENTER);
        textTitle.setText(strTitle);
        textTitle.setEllipsize(TextUtils.TruncateAt.END);
        textTitle.setSingleLine();
        textTitle.setHeight((int) (48 * getContext().getResources().getDisplayMetrics().density + 0.5));
        linearLayoutParent.addView(textTitle, param);

        View viewSep = new View(getContext());
        viewSep.setBackgroundColor(Color.argb(255, 208, 208, 208));
        LinearLayout.LayoutParams paramViewSep = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramViewSep.topMargin = 0;
        paramViewSep.bottomMargin = (int) (8 * getContext().getResources().getDisplayMetrics().density + 0.5);
        paramViewSep.leftMargin = (int) (16 * getContext().getResources().getDisplayMetrics().density + 0.5);
        paramViewSep.rightMargin = (int) (16 * getContext().getResources().getDisplayMetrics().density + 0.5);
        paramViewSep.height = (int) (0.5 * getContext().getResources().getDisplayMetrics().density + 0.5);
        linearLayoutParent.addView(viewSep, paramViewSep);

        linearLayoutParent.addView(scroll);
    }

    public void addSeparator() {
        View viewSep = new View(getContext());
        viewSep.setBackgroundColor(Color.argb(255, 208, 208, 208));
        LinearLayout.LayoutParams paramViewSep = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramViewSep.topMargin = (int) (8 * getContext().getResources().getDisplayMetrics().density + 0.5);
        paramViewSep.bottomMargin = (int) (8 * getContext().getResources().getDisplayMetrics().density + 0.5);
        paramViewSep.leftMargin = (int) (16 * getContext().getResources().getDisplayMetrics().density + 0.5);
        paramViewSep.rightMargin = (int) (16 * getContext().getResources().getDisplayMetrics().density + 0.5);
        paramViewSep.height = (int) (0.5 * getContext().getResources().getDisplayMetrics().density + 0.5);
        linearLayout.addView(viewSep, paramViewSep);
        nHeight += paramViewSep.topMargin + paramViewSep.bottomMargin + paramViewSep.height;
    }

    public void addMenu(String strText, int resID, android.view.View.OnClickListener listener) {
        final RelativeLayout relativeLocal = new RelativeLayout(getContext());
        relativeLocal.setTag(nTag);
        nTag += 1;
        ImageView imgLocal = new ImageView(getContext());
        imgLocal.setImageResource(resID);
        imgLocal.setId(1000);
        RelativeLayout.LayoutParams paramImgLocal = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramImgLocal.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        paramImgLocal.addRule(RelativeLayout.CENTER_VERTICAL);
        paramImgLocal.leftMargin = (int) (18 * getContext().getResources().getDisplayMetrics().density + 0.5);
        paramImgLocal.rightMargin = (int) (8 * getContext().getResources().getDisplayMetrics().density + 0.5);
        relativeLocal.addView(imgLocal, paramImgLocal);

        TextView textLocal = new TextView(getContext());
        textLocal.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textLocal.setGravity(Gravity.LEFT | Gravity.CENTER);
        textLocal.setText(strText);
        textLocal.setTextColor(Color.argb(255, 0, 0, 0));
        relativeLocal.setOnClickListener(listener);
        relativeLocal.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    setCancelable(false);
                    relativeLocal.setBackgroundColor(Color.argb(255, 229, 229, 229));
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    setCancelable(true);
                    relativeLocal.setBackgroundColor(Color.argb(255, 255, 255, 255));
                }
                return false;
            }
        });
        RelativeLayout.LayoutParams paramTextLocal = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramTextLocal.addRule(RelativeLayout.RIGHT_OF, imgLocal.getId());
        paramTextLocal.addRule(RelativeLayout.CENTER_VERTICAL);
        paramTextLocal.topMargin = (int) (17 * getContext().getResources().getDisplayMetrics().density + 0.5);
        paramTextLocal.bottomMargin = (int) (17 * getContext().getResources().getDisplayMetrics().density + 0.5);
        paramTextLocal.rightMargin = (int) (16 * getContext().getResources().getDisplayMetrics().density + 0.5);
        relativeLocal.addView(textLocal, paramTextLocal);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.addView(relativeLocal, param);
        nHeight += (int) (48 * getContext().getResources().getDisplayMetrics().density + 0.5);
    }

    public void addDestructiveMenu(String strText, int resID, android.view.View.OnClickListener listener) {
        final RelativeLayout relativeLocal = new RelativeLayout(getContext());
        ImageView imgLocal = new ImageView(getContext());
        imgLocal.setImageResource(resID);
        imgLocal.setId(1000);
        RelativeLayout.LayoutParams paramImgLocal = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramImgLocal.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        paramImgLocal.addRule(RelativeLayout.CENTER_VERTICAL);
        paramImgLocal.leftMargin = (int) (18 * getContext().getResources().getDisplayMetrics().density + 0.5);
        paramImgLocal.rightMargin = (int) (8 * getContext().getResources().getDisplayMetrics().density + 0.5);
        relativeLocal.addView(imgLocal, paramImgLocal);

        TextView textLocal = new TextView(getContext());
        textLocal.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textLocal.setGravity(Gravity.LEFT | Gravity.CENTER);
        textLocal.setText(strText);
        textLocal.setTextColor(Color.argb(255, 255, 45, 85));
        relativeLocal.setOnClickListener(listener);
        relativeLocal.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    setCancelable(false);
                    relativeLocal.setBackgroundColor(Color.argb(255, 229, 229, 229));
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    setCancelable(true);
                    relativeLocal.setBackgroundColor(Color.argb(255, 255, 255, 255));
                }
                return false;
            }
        });
        RelativeLayout.LayoutParams paramTextLocal = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramTextLocal.addRule(RelativeLayout.RIGHT_OF, imgLocal.getId());
        paramTextLocal.addRule(RelativeLayout.CENTER_VERTICAL);
        paramTextLocal.topMargin = (int) (17 * getContext().getResources().getDisplayMetrics().density + 0.5);
        paramTextLocal.bottomMargin = (int) (17 * getContext().getResources().getDisplayMetrics().density + 0.5);
        paramTextLocal.rightMargin = (int) (16 * getContext().getResources().getDisplayMetrics().density + 0.5);
        relativeLocal.addView(textLocal, paramTextLocal);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.addView(relativeLocal, param);
        nHeight += (int) (48 * getContext().getResources().getDisplayMetrics().density + 0.5);
    }

    public void setCancelMenu() {
        View viewSep = new View(getContext());
        viewSep.setBackgroundColor(Color.argb(255, 208, 208, 208));
        LinearLayout.LayoutParams paramViewSep = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramViewSep.topMargin = (int) (8 * getContext().getResources().getDisplayMetrics().density + 0.5);
        paramViewSep.bottomMargin = 0;
        paramViewSep.leftMargin = (int) (16 * getContext().getResources().getDisplayMetrics().density + 0.5);
        paramViewSep.rightMargin = (int) (16 * getContext().getResources().getDisplayMetrics().density + 0.5);
        paramViewSep.height = (int) (0.5 * getContext().getResources().getDisplayMetrics().density + 0.5);
        linearLayoutParent.addView(viewSep, paramViewSep);

        final TextView textCancel = new TextView(getContext());
        textCancel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textCancel.setGravity(Gravity.CENTER);
        textCancel.setText("キャンセル");
        textCancel.setHeight((int) (48 * getContext().getResources().getDisplayMetrics().density + 0.5));
        textCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        textCancel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    setCancelable(false);
                    textCancel.setBackgroundColor(Color.argb(255, 229, 229, 229));
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    setCancelable(true);
                    textCancel.setBackgroundColor(Color.argb(255, 255, 255, 255));
                }
                return false;
            }
        });
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayoutParent.addView(textCancel, param);

        int nSpaceHeight = (int) (8 * getContext().getResources().getDisplayMetrics().density + 0.5);
        int nSepHeight = (int) (0.5 * getContext().getResources().getDisplayMetrics().density + 0.5);
        int nMenuHeight = (int) (48 * getContext().getResources().getDisplayMetrics().density + 0.5);
        int nScrollMaxHeight = getContext().getResources().getDisplayMetrics().heightPixels - getStatusBarHeight() - nSpaceHeight * 4 - nMenuHeight * 2 - nSepHeight * 2;
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams)scroll.getLayoutParams();
        System.out.println(nHeight);
        System.out.println(nScrollMaxHeight);
        if(nHeight > nScrollMaxHeight) {
            marginLayoutParams.height = nScrollMaxHeight;
            scroll.setLayoutParams(marginLayoutParams);
        }
    }
}