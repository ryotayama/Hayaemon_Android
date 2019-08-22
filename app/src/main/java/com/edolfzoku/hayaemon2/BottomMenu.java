package com.edolfzoku.hayaemon2;

import android.annotation.SuppressLint;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

class BottomMenu extends BottomSheetDialog {
    private MainActivity mActivity;
    private final LinearLayout mLinearLayoutParent;
    private final LinearLayout mLinearLayout;
    private final ScrollView mScroll;
    private int mTag = 0;
    private int mHeight = 0;

    @SuppressLint("ClickableViewAccessibility")
    BottomMenu(@NonNull Context context) {
        super(context, ((MainActivity)context).isDarkMode() ? R.style.BottomSheetDialog_dark : R.style.BottomSheetDialog);
        mActivity = (MainActivity)context;
        mLinearLayoutParent = new LinearLayout(context);
        mLinearLayoutParent.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout = new LinearLayout(context);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mScroll = new ScrollView(context);
        mScroll.addView(mLinearLayout);
        mScroll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    setCancelable(true);
                    for(int i = 0; i < mLinearLayout.getChildCount(); i++) {
                        View childView = mLinearLayout.getChildAt(i);
                        if(childView.getHeight() != 1)
                            childView.setBackgroundColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeBk : android.R.color.white));
                    }
                }
                return false;
            }
        });
        setContentView(mLinearLayoutParent);
        BottomSheetBehavior behavior = BottomSheetBehavior.from((View) mLinearLayoutParent.getParent());
        behavior.setPeekHeight(context.getResources().getDisplayMetrics().heightPixels - getStatusBarHeight() - (int) (16.0 * mActivity.getDensity()));
        setDialogBorder(this);

        if(getWindow() != null) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.dimAmount = 0.4f;
            getWindow().setAttributes(lp);
        }
    }

    private int getStatusBarHeight(){
        final Rect rect = new Rect();
        Window window = getWindow();
        if(window != null) window.getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.top;
    }

    private void setDialogBorder(Dialog dialog) {
        Window window = dialog.getWindow();
        if(window != null) {
            FrameLayout bottomSheet = window.findViewById(android.support.design.R.id.design_bottom_sheet);
            setMargins(bottomSheet, (int) (16 * mActivity.getDensity()), (int) (16 * mActivity.getDensity()));
        }
    }

    private void setMargins(View view, int left, int right) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, 0, right, 0);
            view.requestLayout();
        }
    }

    public void setTitle(String strTitle) {
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        param.leftMargin = (int) (16 * mActivity.getDensity());
        param.rightMargin = (int) (16 * mActivity.getDensity());
        TextView textTitle = new TextView(getContext());
        textTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        textTitle.setGravity(Gravity.CENTER);
        textTitle.setText(strTitle);
        textTitle.setTextColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeGray : R.color.lightModeGray));
        textTitle.setEllipsize(TextUtils.TruncateAt.END);
        textTitle.setSingleLine();
        textTitle.setHeight((int) (48 * mActivity.getDensity()));
        mLinearLayoutParent.addView(textTitle, param);

        View viewSep = new View(getContext());
        viewSep.setBackgroundColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeSep : R.color.lightModeSep));
        LinearLayout.LayoutParams paramViewSep = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramViewSep.topMargin = 0;
        paramViewSep.bottomMargin = (int) (8 * mActivity.getDensity());
        paramViewSep.leftMargin = (int) (16 * mActivity.getDensity());
        paramViewSep.rightMargin = (int) (16 * mActivity.getDensity());
        paramViewSep.height = 1;
        mLinearLayoutParent.addView(viewSep, paramViewSep);

        mLinearLayoutParent.addView(mScroll);
    }

    void addSeparator() {
        View viewSep = new View(getContext());
        viewSep.setBackgroundColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeSep : R.color.lightModeSep));
        LinearLayout.LayoutParams paramViewSep = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramViewSep.topMargin = (int) (8 * mActivity.getDensity());
        paramViewSep.bottomMargin = (int) (8 * mActivity.getDensity());
        paramViewSep.leftMargin = (int) (16 * mActivity.getDensity());
        paramViewSep.rightMargin = (int) (16 * mActivity.getDensity());
        paramViewSep.height = 1;
        mLinearLayout.addView(viewSep, paramViewSep);
        mHeight += paramViewSep.topMargin + paramViewSep.bottomMargin + paramViewSep.height;
    }

    @SuppressLint("ClickableViewAccessibility")
    void addMenu(String strText, int resID, android.view.View.OnClickListener listener) {
        final RelativeLayout relativeLocal = new RelativeLayout(getContext());
        relativeLocal.setTag(mTag);
        mTag += 1;
        ImageView imgLocal = new ImageView(getContext());
        imgLocal.setImageResource(resID);
        imgLocal.setId(R.id.imgLocal);
        RelativeLayout.LayoutParams paramImgLocal = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramImgLocal.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        paramImgLocal.addRule(RelativeLayout.CENTER_VERTICAL);
        paramImgLocal.leftMargin = (int) (18 * mActivity.getDensity());
        paramImgLocal.rightMargin = (int) (8 * mActivity.getDensity());
        relativeLocal.addView(imgLocal, paramImgLocal);

        TextView textLocal = new TextView(getContext());
        textLocal.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textLocal.setGravity(Gravity.START | Gravity.CENTER);
        textLocal.setText(strText);
        textLocal.setTextColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? android.R.color.white : android.R.color.black));
        relativeLocal.setOnClickListener(listener);
        relativeLocal.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    setCancelable(false);
                    relativeLocal.setBackgroundColor(mActivity.isDarkMode() ? mActivity.getResources().getColor(R.color.darkModeLightBk) : Color.argb(255, 229, 229, 229));
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    setCancelable(true);
                    relativeLocal.setBackgroundColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeBk : android.R.color.white));
                }
                return false;
            }
        });
        RelativeLayout.LayoutParams paramTextLocal = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramTextLocal.addRule(RelativeLayout.RIGHT_OF, imgLocal.getId());
        paramTextLocal.addRule(RelativeLayout.CENTER_VERTICAL);
        paramTextLocal.topMargin = (int) (17 * mActivity.getDensity());
        paramTextLocal.bottomMargin = (int) (17 * mActivity.getDensity());
        paramTextLocal.rightMargin = (int) (16 * mActivity.getDensity());
        relativeLocal.addView(textLocal, paramTextLocal);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mLinearLayout.addView(relativeLocal, param);
        mHeight += (int) (48 * mActivity.getDensity());
    }

    @SuppressLint("ClickableViewAccessibility")
    void addDestructiveMenu(String strText, int resID, android.view.View.OnClickListener listener) {
        final RelativeLayout relativeLocal = new RelativeLayout(getContext());
        ImageView imgLocal = new ImageView(getContext());
        imgLocal.setImageResource(resID);
        imgLocal.setId(R.id.imgLocal);
        RelativeLayout.LayoutParams paramImgLocal = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramImgLocal.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        paramImgLocal.addRule(RelativeLayout.CENTER_VERTICAL);
        paramImgLocal.leftMargin = (int) (18 * mActivity.getDensity());
        paramImgLocal.rightMargin = (int) (8 * mActivity.getDensity());
        relativeLocal.addView(imgLocal, paramImgLocal);

        TextView textLocal = new TextView(getContext());
        textLocal.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textLocal.setGravity(Gravity.START | Gravity.CENTER);
        textLocal.setText(strText);
        textLocal.setTextColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeRed : R.color.lightModeRed));
        relativeLocal.setOnClickListener(listener);
        relativeLocal.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    setCancelable(false);
                    relativeLocal.setBackgroundColor(mActivity.isDarkMode() ? mActivity.getResources().getColor(R.color.darkModeLightBk) : Color.argb(255, 229, 229, 229));
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    setCancelable(true);
                    relativeLocal.setBackgroundColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeBk : android.R.color.white));
                }
                return false;
            }
        });
        RelativeLayout.LayoutParams paramTextLocal = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramTextLocal.addRule(RelativeLayout.RIGHT_OF, imgLocal.getId());
        paramTextLocal.addRule(RelativeLayout.CENTER_VERTICAL);
        paramTextLocal.topMargin = (int) (17 * mActivity.getDensity());
        paramTextLocal.bottomMargin = (int) (17 * mActivity.getDensity());
        paramTextLocal.rightMargin = (int) (16 * mActivity.getDensity());
        relativeLocal.addView(textLocal, paramTextLocal);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mLinearLayout.addView(relativeLocal, param);
        mHeight += (int) (48 * mActivity.getDensity());
    }

    @SuppressLint("ClickableViewAccessibility")
    void setCancelMenu() {
        View viewSep = new View(getContext());
        viewSep.setBackgroundColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeSep : R.color.lightModeSep));
        LinearLayout.LayoutParams paramViewSep = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramViewSep.topMargin = (int) (8 * mActivity.getDensity());
        paramViewSep.bottomMargin = 0;
        paramViewSep.leftMargin = (int) (16 * mActivity.getDensity());
        paramViewSep.rightMargin = (int) (16 * mActivity.getDensity());
        paramViewSep.height = 1;
        mLinearLayoutParent.addView(viewSep, paramViewSep);

        final TextView textCancel = new TextView(getContext());
        textCancel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textCancel.setGravity(Gravity.CENTER);
        textCancel.setText(R.string.cancel);
        textCancel.setTextColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeGray : R.color.lightModeGray));
        textCancel.setHeight((int) (48 * mActivity.getDensity()));
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
                    textCancel.setBackgroundColor(mActivity.isDarkMode() ? mActivity.getResources().getColor(R.color.darkModeLightBk) : Color.argb(255, 229, 229, 229));
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    setCancelable(true);
                    textCancel.setBackgroundColor(mActivity.getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeBk : android.R.color.white));
                }
                return false;
            }
        });
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mLinearLayoutParent.addView(textCancel, param);

        int nSpaceHeight = (int) (8 * mActivity.getDensity());
        int nSepHeight = 1;
        int nMenuHeight = (int) (48 * mActivity.getDensity());
        int nScrollMaxHeight = getContext().getResources().getDisplayMetrics().heightPixels - getStatusBarHeight() - nSpaceHeight * 4 - nMenuHeight * 2 - nSepHeight * 2;
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams)mScroll.getLayoutParams();
        if(mHeight > nScrollMaxHeight) {
            marginLayoutParams.height = nScrollMaxHeight;
            mScroll.setLayoutParams(marginLayoutParams);
        }
    }
}