/*
 * LoopFragment
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

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.un4seen.bass.BASS;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Locale;

public class LoopFragment extends Fragment implements View.OnTouchListener, View.OnFocusChangeListener, View.OnLongClickListener {
    static MainActivity sActivity;
    static ArrayList<Double> sMarkerTimes;
    static int sMarker; // マーカー再生時のループ位置
    static boolean sABLoop, sMarkerPlay;
    private static Handler sHandler;
    private WaveView mWaveView;
    private LinearLayout mABButton, sMarkerButton;
    private EditText mTextCurValue;
    private View mViewCurPos, mViewMaskA, mViewMaskB, mViewSep1Loop, mViewSep2Loop, mViewSep3Loop;
    private RadioGroup mRadioGroupLoopMode;
    private RadioButton mRadioButtonABLoop, mRadioButtonMarkerPlay;
    private RelativeLayout mRelativeLoop, mRelativeWave;
    private AnimationButton mBtnRewind5Sec, mBtnRewind5Sec2, mBtnForward5Sec, mBtnForward5Sec2, mBtnLoopmarker, mBtnA, mBtnB, mBtnZoomIn, mBtnZoomOut, mBtnPrevmarker, mBtnDelmarker, mBtnAddmarker, mBtnNextmarker;
    private TextView mTextA, mTextB;
    private EditText mTextAValue, mTextBValue;
    private boolean mContinue, mTouching;
    private final ArrayList<ImageView> mMarkerTexts;

    AnimationButton getBtnLoopmarker() { return mBtnLoopmarker; }
    RadioGroup getRadioGroupLoopMode() { return mRadioGroupLoopMode; }
    private WaveView getWaveView() { return mWaveView; }
    private View getViewCurPos() { return mViewCurPos; }
    private View getViewMaskA() { return mViewMaskA; }
    private View getViewMaskB() { return mViewMaskB; }
    private RelativeLayout getRelativeLoop() { return mRelativeLoop; }
    private AnimationButton getBtnA() { return mBtnA; }
    private AnimationButton getBtnB() { return mBtnB; }
    private AnimationButton getBtnZoomIn() { return mBtnZoomIn; }
    private AnimationButton getBtnZoomOut() { return mBtnZoomOut; }
    private EditText getTextAValue() { return mTextAValue; }
    private EditText getTextBValue() { return mTextBValue; }

    static void setArMarkerTime(ArrayList<Double> markerTimes) {
        if(markerTimes == null) return;
        sMarkerTimes = new ArrayList<>();
        int nScreenWidth, nMaxWidth = 0;
        if(sActivity != null) {
            nScreenWidth = sActivity.loopFragment.getWaveView().getWidth();
            nMaxWidth = (int) (nScreenWidth * sActivity.loopFragment.getWaveView().getZoom());
        }
        double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE));
        for(int i = 0; i < markerTimes.size(); i++) {
            double dPos = markerTimes.get(i);
            sMarkerTimes.add(dPos);

            if(sActivity != null) {
                ImageView imgView = new ImageView(sActivity);
                imgView.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_marker_dark : R.drawable.ic_abloop_marker);
                sActivity.loopFragment.getRelativeLoop().addView(imgView);
                imgView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                int nLeft = (int) (sActivity.loopFragment.getViewCurPos().getX() - nMaxWidth * dPos / dLength - imgView.getMeasuredWidth() / 2.0f);
                int nTop = sActivity.loopFragment.getWaveView().getTop() - imgView.getMeasuredHeight();
                imgView.setTranslationX(nLeft);
                imgView.setTranslationY(nTop);
                imgView.requestLayout();
                sActivity.loopFragment.getMarkerTexts().add(i, imgView);
            }
        }
    }
    private ArrayList<ImageView> getMarkerTexts() { return mMarkerTexts; }
    static void setMarker(int marker) { sMarker = marker; }
    EditText getTextCurValue() { return mTextCurValue; }

    public LoopFragment() {
        if(sHandler == null) sHandler = new Handler();
        if(sMarkerTimes == null) sMarkerTimes = new ArrayList<>();
        mMarkerTexts = new ArrayList<>();
        if(MainActivity.sStream == 0) {
            sMarker = 0;
            sABLoop = true;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        sActivity = (MainActivity)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        sActivity = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loop, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewCurPos = sActivity.findViewById(R.id.viewCurPos);
        mViewCurPos.setX(-(int) (1.0 * sActivity.getDensity()));
        mABButton = sActivity.findViewById(R.id.ABButton);
        sMarkerButton = sActivity.findViewById(R.id.MarkerButton);
        mTextCurValue = sActivity.findViewById(R.id.textCurValue);
        mViewMaskA = sActivity.findViewById(R.id.viewMaskA);
        mViewMaskB = sActivity.findViewById(R.id.viewMaskB);
        mViewSep1Loop = sActivity.findViewById(R.id.viewSep1Loop);
        mViewSep2Loop = sActivity.findViewById(R.id.viewSep2Loop);
        mViewSep3Loop = sActivity.findViewById(R.id.viewSep3Loop);
        mWaveView = sActivity.findViewById(R.id.waveView);
        mBtnLoopmarker = sActivity.findViewById(R.id.btnLoopmarker);
        mRadioGroupLoopMode = sActivity.findViewById(R.id.radioGroupLoopMode);
        mRadioButtonABLoop = sActivity.findViewById(R.id.radioButtonABLoop);
        mRadioButtonMarkerPlay = sActivity.findViewById(R.id.radioButtonMarkerPlay);
        mRelativeLoop = sActivity.findViewById(R.id.relativeLoop);
        mBtnRewind5Sec = sActivity.findViewById(R.id.btnRewind5Sec);
        mBtnRewind5Sec2 = sActivity.findViewById(R.id.btnRewind5Sec2);
        mBtnForward5Sec = sActivity.findViewById(R.id.btnForward5Sec);
        mBtnForward5Sec2 = sActivity.findViewById(R.id.btnForward5Sec2);
        mTextA = sActivity.findViewById(R.id.textA);
        mTextB = sActivity.findViewById(R.id.textB);
        mTextAValue = sActivity.findViewById(R.id.textAValue);
        mTextBValue  = sActivity.findViewById(R.id.textBValue);
        mBtnA = sActivity.findViewById(R.id.btnA);
        mBtnB = sActivity.findViewById(R.id.btnB);
        mRelativeWave = sActivity.findViewById(R.id.relativeWave);
        mBtnDelmarker= sActivity.findViewById(R.id.btnDelmarker);
        mBtnPrevmarker = sActivity.findViewById(R.id.btnPrevmarker);
        mBtnZoomIn = sActivity.findViewById(R.id.btnZoomIn);
        mBtnZoomOut = sActivity.findViewById(R.id.btnZoomOut);
        mBtnAddmarker = sActivity.findViewById(R.id.btnAddmarker);
        mBtnNextmarker = sActivity.findViewById(R.id.btnNextmarker);
        final LinearLayout ABLabel = sActivity.findViewById(R.id.ABLabel);

        mTextAValue.setText(getString(R.string.zeroHMS));
        mTextBValue.setText(getString(R.string.zeroHMS));
        mTextCurValue.setText(getString(R.string.zeroHMS));
        mBtnZoomIn.setColorFilter(new PorterDuffColorFilter(Color.parseColor(sActivity.isDarkMode() ? "#FF939CA0" : "#FFCCCCCC"), PorterDuff.Mode.SRC_IN));
        mBtnZoomIn.setEnabled(false);
        mBtnZoomOut.setColorFilter(new PorterDuffColorFilter(Color.parseColor(sActivity.isDarkMode() ? "#FF939CA0" : "#FFCCCCCC"), PorterDuff.Mode.SRC_IN));
        mBtnZoomOut.setEnabled(false);

        mWaveView.setLoopFragment(this);
        mWaveView.setOnTouchListener(this);
        mBtnZoomOut.setOnTouchListener(this);
        mBtnZoomOut.setOnLongClickListener(this);
        mBtnZoomIn.setOnTouchListener(this);
        mBtnZoomIn.setOnLongClickListener(this);
        sActivity.findViewById(R.id.viewBtnALeft).setOnTouchListener(this);
        mBtnA.setSelected(false);
        mBtnA.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_a_dark : R.drawable.ic_abloop_a);
        mBtnA.setOnTouchListener(this);
        sActivity.findViewById(R.id.viewBtnARight).setOnTouchListener(this);
        sActivity.findViewById(R.id.viewBtnRewindLeft).setOnTouchListener(this);
        mBtnRewind5Sec.setOnTouchListener(this);
        mBtnRewind5Sec.setOnLongClickListener(this);
        mBtnRewind5Sec.setTag(5);
        sActivity.findViewById(R.id.viewBtnRewindRight).setOnTouchListener(this);
        sActivity.findViewById(R.id.viewBtnForwardLeft).setOnTouchListener(this);
        mBtnForward5Sec.setOnTouchListener(this);
        mBtnForward5Sec.setOnLongClickListener(this);
        mBtnForward5Sec.setTag(5);
        sActivity.findViewById(R.id.viewBtnForwardRight).setOnTouchListener(this);
        sActivity.findViewById(R.id.viewBtnBLeft).setOnTouchListener(this);
        mBtnB.setSelected(false);
        mBtnB.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_b_dark : R.drawable.ic_abloop_b);
        mBtnB.setOnTouchListener(this);
        sActivity.findViewById(R.id.viewBtnBRight).setOnTouchListener(this);

        mBtnRewind5Sec2.setOnTouchListener(this);
        mBtnRewind5Sec2.setOnLongClickListener(this);
        mBtnRewind5Sec2.setTag(5);
        mBtnPrevmarker.setOnTouchListener(this);
        mBtnDelmarker.setOnTouchListener(this);
        mBtnAddmarker.setOnTouchListener(this);
        mBtnNextmarker.setOnTouchListener(this);
        mBtnLoopmarker.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_marker_loop_dark : R.drawable.ic_abloop_marker_loop);
        mBtnLoopmarker.setOnTouchListener(this);
        mBtnForward5Sec2.setOnTouchListener(this);
        mBtnForward5Sec2.setOnLongClickListener(this);
        mBtnForward5Sec2.setTag(5);

        mRadioGroupLoopMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int nItem) {
                if(nItem == R.id.radioButtonABLoop) {
                    ABLabel.setVisibility(View.VISIBLE);
                    mABButton.setVisibility(View.VISIBLE);
                    sABLoop = true;
                    if(MainActivity.sLoopA) mViewMaskA.setVisibility(View.VISIBLE);
                    if(MainActivity.sLoopB) mViewMaskB.setVisibility(View.VISIBLE);
                    MainActivity.setSync();
                    PlaylistFragment.updateSavingEffect();

                    sMarkerButton.setVisibility(View.INVISIBLE);
                    for(int i = 0 ; i < mMarkerTexts.size(); i++)
                    {
                        ImageView imgView = mMarkerTexts.get(i);
                        imgView.setVisibility(View.INVISIBLE);
                    }
                }
                else {
                    sMarkerButton.setVisibility(View.VISIBLE);
                    for(int i = 0 ; i < mMarkerTexts.size(); i++)
                    {
                        ImageView imgView = mMarkerTexts.get(i);
                        imgView.setVisibility(View.VISIBLE);
                    }
                    sABLoop = false;
                    MainActivity.setSync();
                    PlaylistFragment.updateSavingEffect();

                    ABLabel.setVisibility(View.INVISIBLE);
                    mABButton.setVisibility(View.INVISIBLE);
                    mViewMaskA.setVisibility(View.INVISIBLE);
                    mViewMaskB.setVisibility(View.INVISIBLE);
                }
            }
        });

        mTextAValue.setOnFocusChangeListener(this);
        mTextBValue.setOnFocusChangeListener(this);
        mTextCurValue.setOnFocusChangeListener(this);

        if(sActivity.isDarkMode()) setDarkMode(false);

        if(MainActivity.sStream != 0) {
            if(MainActivity.sLoopA) {
                mBtnA.setSelected(true);
                mBtnA.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_a_on_dark : R.drawable.ic_abloop_a_on);
            }
            if(MainActivity.sLoopB) {
                mBtnB.setSelected(true);
                mBtnB.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_b_on_dark : R.drawable.ic_abloop_b_on);
            }
            if(sMarkerPlay) {
                mBtnLoopmarker.setSelected(true);
                mBtnLoopmarker.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_marker_loop_on_dark : R.drawable.ic_abloop_marker_loop_on);
            }
            mRelativeLoop.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if(Build.VERSION.SDK_INT >= 16) mRelativeLoop.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    else mRelativeLoop.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    int nScreenWidth = mWaveView.getWidth();
                    int nMaxWidth = (int) (nScreenWidth * mWaveView.getZoom());
                    double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE));
                    for(int i = 0; i < sMarkerTimes.size(); i++) {
                        ImageView imgView = new ImageView(sActivity);
                        imgView.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_marker_dark : R.drawable.ic_abloop_marker);
                        mRelativeLoop.addView(imgView);
                        imgView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                        int nLeft = (int) (mViewCurPos.getX() - nMaxWidth * sMarkerTimes.get(i) / dLength - imgView.getMeasuredWidth() / 2.0f);
                        int nTop = mRelativeWave.getTop() - imgView.getMeasuredHeight();
                        imgView.setTranslationX(nLeft);
                        imgView.setTranslationY(nTop);
                        imgView.requestLayout();
                        mMarkerTexts.add(imgView);
                    }
                }
            });
        }
    }

    private static Runnable sRunnable = new Runnable() {
        @Override
        public void run() {
            long lDelay = 250;
            if(sActivity != null) sActivity.loopFragment.updateCurPos();
            sHandler.postDelayed(this, lDelay);
        }
    };

    void updateCurPos() {
        if(sActivity == null) return;

        long lDelay = 0;
        long nPos = 0;
        int nScreenWidth = mWaveView.getWidth();
        int nMaxWidth = (int) (nScreenWidth * mWaveView.getZoom());
        int nLeft = -(int) (1.0 * sActivity.getDensity());
        if (MainActivity.sStream != 0) {
            if(BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PLAYING && !mTouching)
                lDelay = 250;
            double dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE));
            if (mABButton.getVisibility() == View.VISIBLE && ((MainActivity.sLoopA && dPos < MainActivity.sLoopAPos) || (MainActivity.sLoopB && MainActivity.sLoopBPos < dPos)) && !MainActivity.sPlayNextByBPos) {
                if (EffectFragment.isReverse())
                    dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, MainActivity.sLoopBPos));
                else
                    dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, MainActivity.sLoopAPos));
                BASS.BASS_ChannelSetPosition(MainActivity.sStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, dPos), BASS.BASS_POS_BYTE);
            }
            if (dPos < 0) dPos = 0;
            int nMinute = (int) (dPos / 60);
            int nSecond = (int) (dPos % 60);
            int nHour = nMinute / 60;

            if (sActivity.getSeekCurPos().getVisibility() == View.VISIBLE) {
                double dRemain = MainActivity.sLength - dPos;
                int nRemainMinute = (int) (dRemain / 60);
                int nRemainSecond = (int) (dRemain % 60);

                String strCurPos = nMinute + (nSecond < 10 ? ":0" : ":") + nSecond;
                sActivity.getTextCurPos().setText(strCurPos);
                String strRemain = "-" + nRemainMinute + (nRemainSecond < 10 ? ":0" : ":") + nRemainSecond;
                sActivity.getTextRemain().setText(strRemain);
                sActivity.getSeekCurPos().setProgress((int) dPos);
            }

            nMinute = nMinute % 60;
            int nDec = (int) ((dPos * 100) % 100);

            if (sActivity.getViewPager().getCurrentItem() == 1) {
                String strCurValue = (nHour < 10 ? "0" : "") + nHour + (nMinute < 10 ? ":0" : ":") + nMinute + (nSecond < 10 ? ":0" : ":") + nSecond + (nDec < 10 ? ".0" : ".") + nDec;
                if (!mTextCurValue.getText().toString().equals(strCurValue))
                    mTextCurValue.setText(strCurValue);
            }
            nPos = BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE);
            nLeft = (int) (nMaxWidth * nPos / MainActivity.sByteLength);
        }
        if (sActivity.getViewPager().getCurrentItem() == 1) {
            int nTop = mViewCurPos.getTop();
            if (nScreenWidth / 2 <= nLeft && nLeft < nMaxWidth - nScreenWidth / 2)
                nLeft = (int) (nScreenWidth / 2.0f);
            else if (nMaxWidth - nScreenWidth / 2 <= nLeft)
                nLeft = nScreenWidth - (nMaxWidth - nLeft);
            mViewCurPos.animate()
                    .x(nLeft)
                    .y(nTop)
                    .setDuration(lDelay)
                    .setInterpolator(new LinearInterpolator())
                    .start();
            if (!mContinue) mWaveView.invalidate();
            if (MainActivity.sLoopA) {
                long nPosA = 0;
                if (MainActivity.sStream != 0)
                    nPosA = BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, MainActivity.sLoopAPos);
                nPosA = nPos - nPosA;
                int nLeftA = (int) (nLeft - nMaxWidth * nPosA / MainActivity.sByteLength);
                if (nLeftA < 0) nLeftA = 0;
                mViewMaskA.getLayoutParams().width = nLeftA;
                mViewMaskA.requestLayout();
            }
            if (MainActivity.sLoopB) {
                long nPosB = 0;
                if (MainActivity.sStream != 0)
                    nPosB = BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, MainActivity.sLoopBPos);
                nPosB = nPos - nPosB;
                int nLeftB = (int) (nLeft - nMaxWidth * nPosB / MainActivity.sByteLength);
                if (nLeftB < 0) nLeftB = 0;
                else if (nLeftB > mWaveView.getWidth()) nLeftB = mWaveView.getWidth();
                mViewMaskB.setTranslationX(nLeftB);
                mViewMaskB.getLayoutParams().width = mWaveView.getWidth() - nLeftB;
                mViewMaskB.requestLayout();
            }
            if (sMarkerButton.getVisibility() == View.VISIBLE) {
                for (int i = 0; i < sMarkerTimes.size(); i++) {
                    double dMarkerPos = sMarkerTimes.get(i);
                    long sMarkerPos = BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, dMarkerPos);
                    if(0 <= i && i < mMarkerTexts.size()) {
                        ImageView imgView = mMarkerTexts.get(i);
                        sMarkerPos = nPos - sMarkerPos;
                        int sMarkerLeft = (int) (nLeft - nMaxWidth * sMarkerPos / (float) MainActivity.sByteLength - imgView.getWidth() / 2.0f);
                        if (sMarkerLeft < -imgView.getWidth()) sMarkerLeft = -imgView.getWidth();
                        imgView.setTranslationX(sMarkerLeft);
                        imgView.requestLayout();
                    }
                }
            }
        }
    }

    static void startTimer() {
        stopTimer();
        sHandler.post(sRunnable);
    }

    static void stopTimer() {
        sHandler.removeCallbacks(sRunnable);
        if(sActivity != null) sActivity.loopFragment.updateCurPos();
    }

    private void setZoomOut() {
        mWaveView.setZoom(mWaveView.getZoom() * 0.99f);
        if(mWaveView.getZoom() <= 1.0f) {
            mBtnZoomOut.setColorFilter(new PorterDuffColorFilter(Color.parseColor(sActivity.isDarkMode() ? "#FF939CA0" : "#FFCCCCCC"), PorterDuff.Mode.SRC_IN));
            mBtnZoomOut.setEnabled(false);
        }
        mBtnZoomIn.setColorFilter(null);
        mBtnZoomIn.setEnabled(true);
    }

    private void setZoomIn() {
        mWaveView.setZoom(mWaveView.getZoom() * 1.01f);
        if(mWaveView.getZoom() >= 10.0f) {
            mBtnZoomIn.setColorFilter(new PorterDuffColorFilter(Color.parseColor(sActivity.isDarkMode() ? "#FF939CA0" : "#FFCCCCCC"), PorterDuff.Mode.SRC_IN));
            mBtnZoomIn.setEnabled(false);
        }
        mBtnZoomOut.setColorFilter(null);
        mBtnZoomOut.setEnabled(true);
    }

    private final Runnable repeatZoomOut = new Runnable() {
        @Override
        public void run() {
            if(!mContinue) return;
            setZoomOut();
            long nLength = BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE);
            long nPos = BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE);
            int nScreenWidth = mWaveView.getWidth();
            int nMaxWidth = (int)(nScreenWidth * mWaveView.getZoom());
            int nLeft = (int) (nMaxWidth * nPos / nLength);
            if(nLeft < nScreenWidth / 2) mWaveView.setPivotX(0.0f);
            else if(nScreenWidth / 2 <= nLeft && nLeft < nMaxWidth - nScreenWidth / 2)
                mWaveView.setPivotX(0.5f);
            else mWaveView.setPivotX(1.0f);
            mWaveView.setScaleX(mWaveView.getZoom());
            sHandler.postDelayed(this, 10);
        }
    };

    private final Runnable repeatZoomIn = new Runnable() {
        @Override
        public void run() {
            if(!mContinue) return;
            setZoomIn();
            long nLength = BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE);
            long nPos = BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE);
            int nScreenWidth = mWaveView.getWidth();
            int nMaxWidth = (int)(nScreenWidth * mWaveView.getZoom());
            int nLeft = (int) (nMaxWidth * nPos / nLength);
            if(nLeft < nScreenWidth / 2) mWaveView.setPivotX(0.0f);
            else if(nScreenWidth / 2 <= nLeft && nLeft < nMaxWidth - nScreenWidth / 2)
                mWaveView.setPivotX(0.5f);
            else mWaveView.setPivotX(1.0f);
            mWaveView.setScaleX(mWaveView.getZoom());
            sHandler.postDelayed(this, 10);
        }
    };

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.btnZoomOut) {
            if(MainActivity.sStream == 0) return false;
            mContinue = true;
            sHandler.post(repeatZoomOut);
            return true;
        }
        else if (v.getId() == R.id.btnZoomIn) {
            if(MainActivity.sStream == 0) return false;
            mContinue = true;
            sHandler.post(repeatZoomIn);
            return true;
        }
        else if(v.getId() == R.id.btnRewind5Sec || v.getId() == R.id.btnRewind5Sec2) {
            final BottomMenu menu = new BottomMenu(sActivity);
            menu.setTitle(getString(R.string.chooseRewindButton));
            menu.addMenu(getString(R.string.rewind1Sec), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_01sec_prev_dark : R.drawable.ic_actionsheet_01sec_prev, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBtnRewind5Sec.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_01sec_prev_dark : R.drawable.ic_abloop_01sec_prev);
                    mBtnRewind5Sec2.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_01sec_prev_dark : R.drawable.ic_abloop_01sec_prev);
                    mBtnRewind5Sec.setContentDescription(getString(R.string.rewind1Sec));
                    mBtnRewind5Sec2.setContentDescription(getString(R.string.rewind1Sec));
                    mBtnRewind5Sec.setTag(1);
                    mBtnRewind5Sec2.setTag(1);
                    menu.dismiss();
                }
            });
            menu.addMenu(getString(R.string.rewind2Sec), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_02sec_prev_dark : R.drawable.ic_actionsheet_02sec_prev, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBtnRewind5Sec.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_02sec_prev_dark : R.drawable.ic_abloop_02sec_prev);
                    mBtnRewind5Sec2.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_02sec_prev_dark : R.drawable.ic_abloop_02sec_prev);
                    mBtnRewind5Sec.setContentDescription(getString(R.string.rewind2Sec));
                    mBtnRewind5Sec2.setContentDescription(getString(R.string.rewind2Sec));
                    mBtnRewind5Sec.setTag(2);
                    mBtnRewind5Sec2.setTag(2);
                    menu.dismiss();
                }
            });
            menu.addMenu(getString(R.string.rewind3Sec), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_03sec_prev_dark : R.drawable.ic_actionsheet_03sec_prev, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBtnRewind5Sec.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_03sec_prev_dark : R.drawable.ic_abloop_03sec_prev);
                    mBtnRewind5Sec2.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_03sec_prev_dark : R.drawable.ic_abloop_03sec_prev);
                    mBtnRewind5Sec.setContentDescription(getString(R.string.rewind3Sec));
                    mBtnRewind5Sec2.setContentDescription(getString(R.string.rewind3Sec));
                    mBtnRewind5Sec.setTag(3);
                    mBtnRewind5Sec2.setTag(3);
                    menu.dismiss();
                }
            });
            menu.addMenu(getString(R.string.rewind5Sec), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_05sec_prev_dark : R.drawable.ic_actionsheet_05sec_prev, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBtnRewind5Sec.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_05sec_prev_dark : R.drawable.ic_abloop_05sec_prev);
                    mBtnRewind5Sec2.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_05sec_prev_dark : R.drawable.ic_abloop_05sec_prev);
                    mBtnRewind5Sec.setContentDescription(getString(R.string.rewind5Sec));
                    mBtnRewind5Sec2.setContentDescription(getString(R.string.rewind5Sec));
                    mBtnRewind5Sec.setTag(5);
                    mBtnRewind5Sec2.setTag(5);
                    menu.dismiss();
                }
            });
            menu.addMenu(getString(R.string.rewind10Sec), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_10sec_prev_dark : R.drawable.ic_actionsheet_10sec_prev, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBtnRewind5Sec.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_10sec_prev_dark : R.drawable.ic_abloop_10sec_prev);
                    mBtnRewind5Sec2.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_10sec_prev_dark : R.drawable.ic_abloop_10sec_prev);
                    mBtnRewind5Sec.setContentDescription(getString(R.string.rewind10Sec));
                    mBtnRewind5Sec2.setContentDescription(getString(R.string.rewind10Sec));
                    mBtnRewind5Sec.setTag(10);
                    mBtnRewind5Sec2.setTag(10);
                    menu.dismiss();
                }
            });
            menu.setCancelMenu();
            menu.show();
        }
        else if(v.getId() == R.id.btnForward5Sec || v.getId() == R.id.btnForward5Sec2) {
            final BottomMenu menu = new BottomMenu(sActivity);
            menu.setTitle(getString(R.string.chooseForwardButton));
            menu.addMenu(getString(R.string.forward1Sec), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_01sec_next_dark : R.drawable.ic_actionsheet_01sec_next, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBtnForward5Sec.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_01sec_next_dark : R.drawable.ic_abloop_01sec_next);
                    mBtnForward5Sec2.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_01sec_next_dark : R.drawable.ic_abloop_01sec_next);
                    mBtnForward5Sec.setContentDescription(getString(R.string.forward1Sec));
                    mBtnForward5Sec2.setContentDescription(getString(R.string.forward1Sec));
                    mBtnForward5Sec.setTag(1);
                    mBtnForward5Sec2.setTag(1);
                    menu.dismiss();
                }
            });
            menu.addMenu(getString(R.string.forward2Sec), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_02sec_next_dark : R.drawable.ic_actionsheet_02sec_next, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBtnForward5Sec.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_02sec_next_dark : R.drawable.ic_abloop_02sec_next);
                    mBtnForward5Sec2.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_02sec_next_dark : R.drawable.ic_abloop_02sec_next);
                    mBtnForward5Sec.setContentDescription(getString(R.string.forward2Sec));
                    mBtnForward5Sec2.setContentDescription(getString(R.string.forward2Sec));
                    mBtnForward5Sec.setTag(2);
                    mBtnForward5Sec2.setTag(2);
                    menu.dismiss();
                }
            });
            menu.addMenu(getString(R.string.forward3Sec), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_03sec_next_dark : R.drawable.ic_actionsheet_03sec_next, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBtnForward5Sec.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_03sec_next_dark : R.drawable.ic_abloop_03sec_next);
                    mBtnForward5Sec2.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_03sec_next_dark : R.drawable.ic_abloop_03sec_next);
                    mBtnForward5Sec.setContentDescription(getString(R.string.forward3Sec));
                    mBtnForward5Sec2.setContentDescription(getString(R.string.forward3Sec));
                    mBtnForward5Sec.setTag(3);
                    mBtnForward5Sec2.setTag(3);
                    menu.dismiss();
                }
            });
            menu.addMenu(getString(R.string.forward5Sec), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_05sec_next_dark : R.drawable.ic_actionsheet_05sec_next, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBtnForward5Sec.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_05sec_next_dark : R.drawable.ic_abloop_05sec_next);
                    mBtnForward5Sec2.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_05sec_next_dark : R.drawable.ic_abloop_05sec_next);
                    mBtnForward5Sec.setContentDescription(getString(R.string.forward5Sec));
                    mBtnForward5Sec2.setContentDescription(getString(R.string.forward5Sec));
                    mBtnForward5Sec.setTag(5);
                    mBtnForward5Sec2.setTag(5);
                    menu.dismiss();
                }
            });
            menu.addMenu(getString(R.string.forward10Sec), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_10sec_next_dark : R.drawable.ic_actionsheet_10sec_next, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBtnForward5Sec.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_10sec_next_dark : R.drawable.ic_abloop_10sec_next);
                    mBtnForward5Sec2.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_10sec_next_dark : R.drawable.ic_abloop_10sec_next);
                    mBtnForward5Sec.setContentDescription(getString(R.string.forward10Sec));
                    mBtnForward5Sec2.setContentDescription(getString(R.string.forward10Sec));
                    mBtnForward5Sec.setTag(10);
                    mBtnForward5Sec2.setTag(10);
                    menu.dismiss();
                }
            });
            menu.setCancelMenu();
            menu.show();
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus) {
            if(v.getId() == R.id.textAValue) showABLoopPicker(true);
            else if(v.getId() == R.id.textBValue) showABLoopPicker(false);
            else if(v.getId() == R.id.textCurValue) showCurPicker();
        }
    }

    private void showABLoopPicker(boolean bAPos) {
        LayoutInflater inflater = sActivity.getLayoutInflater();
        View view = inflater.inflate(R.layout.ablooppicker, (ViewGroup)sActivity.findViewById(R.id.layout_root), false);
        final NumberPicker hourPicker = view.findViewById(R.id.abLoopHourPicker);
        final NumberPicker minutePicker = view.findViewById(R.id.abLoopMinutePicker);
        final NumberPicker secondPicker = view.findViewById(R.id.abLoopSecondPicker);
        final NumberPicker decPicker = view.findViewById(R.id.abLoopDecPicker);

        double dValue;
        if(bAPos) dValue = MainActivity.sLoopAPos;
        else dValue = MainActivity.sLoopBPos;
        int nMinute = (int)(dValue / 60);
        int nSecond = (int)(dValue % 60);
        int nHour = nMinute / 60;
        nMinute = nMinute % 60;
        int nDec = (int)((dValue * 100) % 100);
        String strHour = String.format(Locale.getDefault(), "%02d", nHour);
        String strMinute = String.format(Locale.getDefault(), "%02d", nMinute);
        String strSecond = String.format(Locale.getDefault(), "%02d", nSecond);
        String strDec = String.format(Locale.getDefault(), "%02d", nDec);

        final String[] arInts = {"59", "58", "57", "56", "55", "54", "53", "52", "51", "50", "49", "48", "47", "46", "45", "44", "43", "42", "41", "40", "39", "38", "37", "36", "35", "34", "33", "32", "31", "30", "29", "28", "27", "26", "25", "24", "23", "22", "21", "20", "19", "18", "17", "16", "15", "14", "13", "12", "11", "10", "09", "08", "07", "06", "05", "04", "03", "02", "01", "00"};
        hourPicker.setDisplayedValues(arInts);
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(59);
        if(sActivity.isDarkMode()) {
            setNumberPickerTextColor(hourPicker, Color.WHITE);
            setNumberPickerTextColor(minutePicker, Color.WHITE);
            setNumberPickerTextColor(secondPicker, Color.WHITE);
            setNumberPickerTextColor(decPicker, Color.WHITE);
            setDividerColor(hourPicker, Color.rgb(38, 40, 44));
            setDividerColor(minutePicker, Color.rgb(38, 40, 44));
            setDividerColor(secondPicker, Color.rgb(38, 40, 44));
            setDividerColor(decPicker, Color.rgb(38, 40, 44));
        }
        for(int i = 0; i < arInts.length; i++) {
            if(arInts[i].equals(strHour)) hourPicker.setValue(i);
        }

        minutePicker.setDisplayedValues(arInts);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        for(int i = 0; i < arInts.length; i++) {
            if(arInts[i].equals(strMinute)) minutePicker.setValue(i);
        }
        secondPicker.setDisplayedValues(arInts);
        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(59);
        for(int i = 0; i < arInts.length; i++) {
            if(arInts[i].equals(strSecond)) secondPicker.setValue(i);
        }

        final String[] arDecs = {"99", "98", "97", "96", "95", "94", "93", "92", "91", "90", "89", "88", "87", "86", "85", "84", "83", "82", "81", "80", "79", "78", "77", "76", "75", "74", "73", "72", "71", "70", "69", "68", "67", "66", "65", "64", "63", "62", "61", "60", "59", "58", "57", "56", "55", "54", "53", "52", "51", "50", "49", "48", "47", "46", "45", "44", "43", "42", "41", "40", "39", "38", "37", "36", "35", "34", "33", "32", "31", "30", "29", "28", "27", "26", "25", "24", "23", "22", "21", "20", "19", "18", "17", "16", "15", "14", "13", "12", "11", "10", "09", "08", "07", "06", "05", "04", "03", "02", "01", "00"};
        decPicker.setDisplayedValues(arDecs);
        decPicker.setMinValue(0);
        decPicker.setMaxValue(99);
        for(int i = 0; i < arDecs.length; i++) {
            if(arDecs[i].equals(strDec)) decPicker.setValue(i);
        }

        AlertDialog.Builder builder;
        if(sActivity.isDarkMode()) builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
        else builder = new AlertDialog.Builder(sActivity);
        if(bAPos) builder.setTitle(R.string.adjustAPos);
        else builder.setTitle(R.string.adjustBPos);
        final boolean f_bAPos = bAPos;
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                LayoutInflater inflater = sActivity.getLayoutInflater();
                inflater.inflate(R.layout.ablooppicker, (ViewGroup)sActivity.findViewById(R.id.layout_root), false);
                int nHour = Integer.parseInt(arInts[hourPicker.getValue()]);
                int nMinute = Integer.parseInt(arInts[minutePicker.getValue()]);
                int nSecond = Integer.parseInt(arInts[secondPicker.getValue()]);
                double dDec = Double.parseDouble(arDecs[decPicker.getValue()]);
                double dPos = nHour * 3600 + nMinute * 60 + nSecond + dDec / 100.0;

                if(f_bAPos) setLoopA(dPos);
                else setLoopB(dPos);
                clearFocus();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearFocus();
            }
        });
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
            }
        });
        alertDialog.show();
    }

    private static void setNumberPickerTextColor(NumberPicker numberPicker, int color) {
        try{
            Field selectorWheelPaintField = numberPicker.getClass()
                    .getDeclaredField("mSelectorWheelPaint");
            selectorWheelPaintField.setAccessible(true);
            ((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        final int count = numberPicker.getChildCount();
        for(int i = 0; i < count; i++){
            View child = numberPicker.getChildAt(i);
            if(child instanceof EditText)
                ((EditText)child).setTextColor(color);
        }
        numberPicker.invalidate();
    }

    private void setDividerColor(NumberPicker picker, int color) {
        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void showCurPicker() {
        LayoutInflater inflater = sActivity.getLayoutInflater();
        View view = inflater.inflate(R.layout.ablooppicker, (ViewGroup)sActivity.findViewById(R.id.layout_root), false);
        final NumberPicker hourPicker = view.findViewById(R.id.abLoopHourPicker);
        final NumberPicker minutePicker = view.findViewById(R.id.abLoopMinutePicker);
        final NumberPicker secondPicker = view.findViewById(R.id.abLoopSecondPicker);
        final NumberPicker decPicker = view.findViewById(R.id.abLoopDecPicker);

        String strCurPos = mTextCurValue.getText().toString();
        String strHour = strCurPos.substring(0, 2);
        String strMinute = strCurPos.substring(3, 5);
        String strSecond = strCurPos.substring(6, 8);
        String strDec = strCurPos.substring(9, 11);

        final String[] arInts = {"59", "58", "57", "56", "55", "54", "53", "52", "51", "50", "49", "48", "47", "46", "45", "44", "43", "42", "41", "40", "39", "38", "37", "36", "35", "34", "33", "32", "31", "30", "29", "28", "27", "26", "25", "24", "23", "22", "21", "20", "19", "18", "17", "16", "15", "14", "13", "12", "11", "10", "09", "08", "07", "06", "05", "04", "03", "02", "01", "00"};
        final String[] arDecs = {"99", "98", "97", "96", "95", "94", "93", "92", "91", "90", "89", "88", "87", "86", "85", "84", "83", "82", "81", "80", "79", "78", "77", "76", "75", "74", "73", "72", "71", "70", "69", "68", "67", "66", "65", "64", "63", "62", "61", "60", "59", "58", "57", "56", "55", "54", "53", "52", "51", "50", "49", "48", "47", "46", "45", "44", "43", "42", "41", "40", "39", "38", "37", "36", "35", "34", "33", "32", "31", "30", "29", "28", "27", "26", "25", "24", "23", "22", "21", "20", "19", "18", "17", "16", "15", "14", "13", "12", "11", "10", "09", "08", "07", "06", "05", "04", "03", "02", "01", "00"};

        hourPicker.setDisplayedValues(arInts);
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(59);
        if(sActivity.isDarkMode()) {
            setNumberPickerTextColor(hourPicker, Color.WHITE);
            setNumberPickerTextColor(minutePicker, Color.WHITE);
            setNumberPickerTextColor(secondPicker, Color.WHITE);
            setNumberPickerTextColor(decPicker, Color.WHITE);
            setDividerColor(hourPicker, Color.rgb(38, 40, 44));
            setDividerColor(minutePicker, Color.rgb(38, 40, 44));
            setDividerColor(secondPicker, Color.rgb(38, 40, 44));
            setDividerColor(decPicker, Color.rgb(38, 40, 44));
        }
        hourPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int nOldValue, int nNewValue) {
                int nHour = Integer.parseInt(arInts[nNewValue]);
                int nMinute = Integer.parseInt(arInts[minutePicker.getValue()]);
                int nSecond = Integer.parseInt(arInts[secondPicker.getValue()]);
                double dDec = Double.parseDouble(arDecs[decPicker.getValue()]);
                double dPos = nHour * 3600 + nMinute * 60 + nSecond + dDec / 100.0;
                setCurPos(dPos);
            }
        });
        for(int i = 0; i < arInts.length; i++) {
            if(arInts[i].equals(strHour)) hourPicker.setValue(i);
        }
        minutePicker.setDisplayedValues(arInts);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int nOldValue, int nNewValue) {
                int nHour = Integer.parseInt(arInts[hourPicker.getValue()]);
                int nMinute = Integer.parseInt(arInts[nNewValue]);
                int nSecond = Integer.parseInt(arInts[secondPicker.getValue()]);
                double dDec = Double.parseDouble(arDecs[decPicker.getValue()]);
                double dPos = nHour * 3600 + nMinute * 60 + nSecond + dDec / 100.0;
                setCurPos(dPos);
            }
        });
        for(int i = 0; i < arInts.length; i++) {
            if(arInts[i].equals(strMinute)) minutePicker.setValue(i);
        }
        secondPicker.setDisplayedValues(arInts);
        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(59);
        secondPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int nOldValue, int nNewValue) {
                int nHour = Integer.parseInt(arInts[hourPicker.getValue()]);
                int nMinute = Integer.parseInt(arInts[minutePicker.getValue()]);
                int nSecond = Integer.parseInt(arInts[nNewValue]);
                double dDec = Double.parseDouble(arDecs[decPicker.getValue()]);
                double dPos = nHour * 3600 + nMinute * 60 + nSecond + dDec / 100.0;
                setCurPos(dPos);
            }
        });
        for(int i = 0; i < arInts.length; i++) {
            if(arInts[i].equals(strSecond)) secondPicker.setValue(i);
        }

        decPicker.setDisplayedValues(arDecs);
        decPicker.setMinValue(0);
        decPicker.setMaxValue(99);
        decPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int nOldValue, int nNewValue) {
                int nHour = Integer.parseInt(arInts[hourPicker.getValue()]);
                int nMinute = Integer.parseInt(arInts[minutePicker.getValue()]);
                int nSecond = Integer.parseInt(arInts[secondPicker.getValue()]);
                double dDec = Double.parseDouble(arDecs[nNewValue]);
                double dPos = nHour * 3600 + nMinute * 60 + nSecond + dDec / 100.0;
                setCurPos(dPos);
            }
        });
        for(int i = 0; i < arDecs.length; i++) {
            if(arDecs[i].equals(strDec)) decPicker.setValue(i);
        }

        AlertDialog.Builder builder;
        if(sActivity.isDarkMode())
            builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
        else
            builder = new AlertDialog.Builder(sActivity);
        builder.setTitle(R.string.adjustCurPos);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearFocus();
            }
        });
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
            }
        });
        alertDialog.show();
    }

    private void clearFocus() {
        mTextAValue.clearFocus();
        mTextBValue.clearFocus();
        mTextCurValue.clearFocus();
    }

    private static void setLoopA(double dLoopA) {
        setLoopA(dLoopA, true);
    }

    static void setLoopA(double dLoopA, boolean bSave) {
        if(MainActivity.sStream == 0) return;

        double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE));
        if(dLoopA >= dLength)
            dLoopA = dLength;

        if(MainActivity.sLoopB && dLoopA >= MainActivity.sLoopBPos)
            dLoopA = MainActivity.sLoopBPos - 1.0;

        MainActivity.sLoopAPos = dLoopA;
        MainActivity.sLoopA = true;
        if(sActivity != null) {
            sActivity.loopFragment.getBtnA().setSelected(true);
            sActivity.loopFragment.getBtnA().setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_a_on_dark : R.drawable.ic_abloop_a_on);

            long nLength = BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE);
            long nPos = BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, dLoopA);
            int nScreenWidth = sActivity.loopFragment.getWaveView().getWidth();
            int nMaxWidth = (int) (nScreenWidth * sActivity.loopFragment.getWaveView().getZoom());
            int nLeft = (int) (sActivity.loopFragment.getViewCurPos().getX() - nMaxWidth * nPos / nLength);
            if (nLeft > 0) nLeft = 0;
            sActivity.loopFragment.getViewMaskA().getLayoutParams().width = nLeft;
            sActivity.loopFragment.getViewMaskA().setVisibility(View.VISIBLE);
            sActivity.loopFragment.getViewMaskA().requestLayout();

            int nMinute = (int) (MainActivity.sLoopAPos / 60);
            int nSecond = (int) (MainActivity.sLoopAPos % 60);
            int nHour = nMinute / 60;
            nMinute = nMinute % 60;
            int nDec = (int) ((MainActivity.sLoopAPos * 100) % 100);
            sActivity.loopFragment.getTextAValue().setText((nHour < 10 ? "0" : "") + nHour + (nMinute < 10 ? ":0" : ":") + nMinute + (nSecond < 10 ? ":0" : ":") + nSecond + (nDec < 10 ? ".0" : ".") + nDec);
        }

        if(bSave) PlaylistFragment.updateSavingEffect();
    }

    private static void setLoopB(double dLoopB) {
        setLoopB(dLoopB, true);
    }

    static void setLoopB(double dLoopB, boolean bSave) {
        if(MainActivity.sStream == 0) return;

        double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE));
        if(dLoopB >= dLength)
            dLoopB = dLength;

        if(MainActivity.sLoopA && dLoopB <= MainActivity.sLoopAPos)
            dLoopB = MainActivity.sLoopAPos + 1.0;

        MainActivity.sLoopBPos = dLoopB;
        MainActivity.sLoopB = true;
        if(sActivity != null) {
            sActivity.loopFragment.getBtnB().setSelected(true);
            sActivity.loopFragment.getBtnB().setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_b_on_dark : R.drawable.ic_abloop_b_on);
            long nLength = BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE);
            long nPos = BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, dLoopB);
            int nScreenWidth = sActivity.loopFragment.getWaveView().getWidth();
            int nMaxWidth = (int) (nScreenWidth * sActivity.loopFragment.getWaveView().getZoom());
            int nLeft = (int) (sActivity.loopFragment.getViewCurPos().getX() - nMaxWidth * nPos / nLength);
            if (nLeft < 0) nLeft = 0;
            else if (nLeft > sActivity.loopFragment.getWaveView().getWidth()) nLeft = sActivity.loopFragment.getWaveView().getWidth();
            sActivity.loopFragment.getViewMaskB().setTranslationX(nLeft);
            sActivity.loopFragment.getViewMaskB().getLayoutParams().width = sActivity.loopFragment.getWaveView().getWidth() - nLeft;
            sActivity.loopFragment.getViewMaskB().setVisibility(View.VISIBLE);
            sActivity.loopFragment.getViewMaskB().requestLayout();

            int nMinute = (int) (MainActivity.sLoopBPos / 60);
            int nSecond = (int) (MainActivity.sLoopBPos % 60);
            int nHour = nMinute / 60;
            nMinute = nMinute % 60;
            int nDec = (int) ((MainActivity.sLoopBPos * 100) % 100);
            sActivity.loopFragment.getTextBValue().setText((nHour < 10 ? "0" : "") + nHour + (nMinute < 10 ? ":0" : ":") + nMinute + (nSecond < 10 ? ":0" : ":") + nSecond + (nDec < 10 ? ".0" : ".") + nDec);
        }

        BASS.BASS_ChannelSetPosition(MainActivity.sStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, MainActivity.sLoopAPos), BASS.BASS_POS_BYTE);
        MainActivity.setSync();

        if(bSave) PlaylistFragment.updateSavingEffect();
    }

    void setCurPos(double dPos) {
        if(MainActivity.sStream == 0) return;

        double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE));
        boolean bReverse = EffectFragment.isReverse();
        if(bReverse) {
            if(dPos <= 0.0f) {
                if(BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PLAYING) {
                    MainActivity.onEnded(true);
                    return;
                }
                dPos = 0.0f;
            }
        }
        else {
            if(dLength <= dPos) {
                if(BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PLAYING) {
                    MainActivity.onEnded(true);
                    return;
                }
                dPos = dLength;
            }
        }
        BASS.BASS_ChannelSetPosition(MainActivity.sStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, dPos), BASS.BASS_POS_BYTE);
        MainActivity.setSync();

        double dCurPos = dPos;
        int i = 0;
        for( ; i < sMarkerTimes.size(); i++) {
            dPos = sMarkerTimes.get(i);
            if((!bReverse && dCurPos < dPos) || (bReverse && dCurPos < dPos - 1.0))
                break;
        }
        sMarker = i - 1;

        MainActivity.setSync();

        PlaylistFragment.updateSavingEffect();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.btnZoomOut) {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(MainActivity.sStream == 0) return false;
                mContinue = false;
                setZoomOut();
                mWaveView.redrawWaveForm();
            }
        }
        else if(v.getId() == R.id.btnZoomIn) {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(MainActivity.sStream == 0) return false;
                mContinue = false;
                setZoomIn();
                mWaveView.redrawWaveForm();
            }
        }
        else if(v.getId() == R.id.viewBtnRewindLeft || v.getId() == R.id.viewBtnRewindRight || v.getId() == R.id.btnRewind5Sec || v.getId() == R.id.btnRewind5Sec2) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (MainActivity.sStream != 0) {
                    double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE));
                    double dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE));
                    if((Integer)mBtnRewind5Sec.getTag() == 1) dPos -= 1.0;
                    else if((Integer)mBtnRewind5Sec.getTag() == 2) dPos -= 2.0;
                    else if((Integer)mBtnRewind5Sec.getTag() == 3) dPos -= 3.0;
                    else if((Integer)mBtnRewind5Sec.getTag() == 5) dPos -= 5.0;
                    else if((Integer)mBtnRewind5Sec.getTag() == 10) dPos -= 10.0;
                    boolean bReverse = EffectFragment.isReverse();
                    if(bReverse) {
                        if(dPos <= 0.0f) {
                            if(BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PLAYING) {
                                MainActivity.onEnded(true);
                                return true;
                            }
                            dPos = 0.0f;
                        }
                    }
                    else {
                        if(dLength <= dPos) {
                            if(BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PLAYING) {
                                MainActivity.onEnded(true);
                                return true;
                            }
                            dPos = dLength;
                        }
                    }
                    if(mABButton.getVisibility() == View.VISIBLE && ((MainActivity.sLoopA && dPos < MainActivity.sLoopAPos) || (MainActivity.sLoopB && MainActivity.sLoopBPos < dPos)))
                        dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, MainActivity.sLoopAPos));
                    BASS.BASS_ChannelSetPosition(MainActivity.sStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, dPos), BASS.BASS_POS_BYTE);
                    MainActivity.setSync();
                    mTouching = true;
                    updateCurPos();
                    mTouching = false;
                }
            }
        }
        else if(v.getId() == R.id.viewBtnForwardLeft || v.getId() == R.id.viewBtnForwardRight || v.getId() == R.id.btnForward5Sec || v.getId() == R.id.btnForward5Sec2) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (MainActivity.sStream != 0) {
                    double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE));
                    double dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE));
                    if((Integer)mBtnForward5Sec.getTag() == 1) dPos += 1.0;
                    else if((Integer)mBtnForward5Sec.getTag() == 2) dPos += 2.0;
                    else if((Integer)mBtnForward5Sec.getTag() == 3) dPos += 3.0;
                    else if((Integer)mBtnForward5Sec.getTag() == 5) dPos += 5.0;
                    else if((Integer)mBtnForward5Sec.getTag() == 10) dPos += 10.0;
                    boolean bReverse = EffectFragment.isReverse();
                    if(bReverse) {
                        if(dPos <= 0.0f) {
                            if(BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PLAYING) {
                                MainActivity.onEnded(true);
                                return true;
                            }
                            dPos = 0.0f;
                        }
                    }
                    else {
                        if(dLength <= dPos) {
                            if(BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PLAYING) {
                                MainActivity.onEnded(true);
                                return true;
                            }
                            dPos = dLength;
                        }
                    }
                    if(mABButton.getVisibility() == View.VISIBLE && ((MainActivity.sLoopA && dPos < MainActivity.sLoopAPos) || (MainActivity.sLoopB && MainActivity.sLoopBPos < dPos)))
                        dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, MainActivity.sLoopAPos));
                    BASS.BASS_ChannelSetPosition(MainActivity.sStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, dPos), BASS.BASS_POS_BYTE);
                    MainActivity.setSync();
                    mTouching = true;
                    updateCurPos();
                    mTouching = false;
                }
            }
        }
        else if(v.getId() == R.id.viewBtnALeft || v.getId() == R.id.viewBtnARight || v.getId() == R.id.btnA) {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(MainActivity.sStream != 0) {
                    if(mBtnA.isSelected()) {
                        MainActivity.sLoopAPos = 0.0;
                        MainActivity.sLoopA = false;
                        mBtnA.setSelected(false);
                        mBtnA.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_a_dark : R.drawable.ic_abloop_a);
                        mViewMaskA.setVisibility(View.INVISIBLE);
                        mTextAValue.setText("00:00:00.00");
                    }
                    else {
                        MainActivity.sLoopAPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE));
                        MainActivity.sLoopA = true;
                        mBtnA.setSelected(true);
                        mBtnA.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_a_on_dark : R.drawable.ic_abloop_a_on);
                        long nLength = BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE);
                        long nPos = BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE);
                        int nBkWidth = mWaveView.getWidth();
                        mViewMaskA.getLayoutParams().width = (int) (nBkWidth * nPos / nLength);
                        mViewMaskA.setVisibility(View.VISIBLE);
                        mViewMaskA.requestLayout();
                        if(EffectFragment.isReverse())
                            BASS.BASS_ChannelSetPosition(MainActivity.sStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, MainActivity.sLoopBPos), BASS.BASS_POS_BYTE);

                        int nMinute = (int)(MainActivity.sLoopAPos / 60);
                        int nSecond = (int)(MainActivity.sLoopAPos % 60);
                        int nHour = nMinute / 60;
                        nMinute = nMinute % 60;
                        int nDec = (int)((MainActivity.sLoopAPos * 100) % 100);
                        mTextAValue.setText((nHour < 10 ? "0" : "") + nHour + (nMinute < 10 ? ":0" : ":") + nMinute + (nSecond < 10 ? ":0" : ":") + nSecond + (nDec < 10 ? ".0" : ".") + nDec);
                    }
                    PlaylistFragment.updateSavingEffect();
                }
            }
        }
        else if(v.getId() == R.id.viewBtnBLeft || v.getId() == R.id.viewBtnBRight || v.getId() == R.id.btnB) {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(MainActivity.sStream != 0) {
                    if(mBtnB.isSelected()) {
                        MainActivity.sLoopBPos = 0.0;
                        MainActivity.sLoopB = false;
                        mBtnB.setSelected(false);
                        mBtnB.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_b_dark : R.drawable.ic_abloop_b);
                        mViewMaskB.setVisibility(View.INVISIBLE);

                        double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE));
                        int nMinute = (int)(dLength / 60);
                        int nSecond = (int)(dLength % 60);
                        int nHour = nMinute / 60;
                        nMinute = nMinute % 60;
                        int nDec = (int)((dLength * 100) % 100);
                        mTextBValue.setText((nHour < 10 ? "0" : "") + nHour + (nMinute < 10 ? ":0" : ":") + nMinute + (nSecond < 10 ? ":0" : ":") + nSecond + (nDec < 10 ? ".0" : ".") + nDec);
                    }
                    else {
                        MainActivity.sLoopBPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE));
                        MainActivity.sLoopB = true;
                        mBtnB.setSelected(true);
                        mBtnB.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_b_on_dark : R.drawable.ic_abloop_b_on);
                        long nLength = BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE);
                        long nPos = BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE);
                        int nBkWidth = mWaveView.getWidth();
                        int nLeft = (int) (nBkWidth * nPos / nLength);
                        mViewMaskB.setTranslationX(nLeft);
                        mViewMaskB.getLayoutParams().width = nBkWidth - nLeft;
                        mViewMaskB.setVisibility(View.VISIBLE);
                        mViewMaskB.requestLayout();
                        if(!EffectFragment.isReverse())
                            BASS.BASS_ChannelSetPosition(MainActivity.sStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, MainActivity.sLoopAPos), BASS.BASS_POS_BYTE);

                        int nMinute = (int)(MainActivity.sLoopBPos / 60);
                        int nSecond = (int)(MainActivity.sLoopBPos % 60);
                        int nHour = nMinute / 60;
                        nMinute = nMinute % 60;
                        int nDec = (int)((MainActivity.sLoopBPos * 100) % 100);
                        mTextBValue.setText((nHour < 10 ? "0" : "") + nHour + (nMinute < 10 ? ":0" : ":") + nMinute + (nSecond < 10 ? ":0" : ":") + nSecond + (nDec < 10 ? ".0" : ".") + nDec);
                    }
                    MainActivity.setSync();

                    PlaylistFragment.updateSavingEffect();
                }
            }
        }
        else if(v.getId() == R.id.btnPrevmarker) {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(MainActivity.sStream != 0) {
                    boolean bReverse = EffectFragment.isReverse();
                    double dCurPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE));
                    int i = sMarkerTimes.size() - 1;
                    for( ; i >= 0; i--) {
                        double dPos = sMarkerTimes.get(i);
                        if((!bReverse && dCurPos >= dPos + 1.0) || (bReverse && dCurPos >= dPos)) {
                            BASS.BASS_ChannelSetPosition(MainActivity.sStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, dPos), BASS.BASS_POS_BYTE);
                            break;
                        }
                    }
                    sMarker = i;
                    MainActivity.setSync();

                    PlaylistFragment.updateSavingEffect();
                }
            }
        }
        else if(v.getId() == R.id.btnNextmarker) {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(MainActivity.sStream != 0) {
                    boolean bReverse = EffectFragment.isReverse();
                    double dCurPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE));
                    int i = 0;
                    for( ; i < sMarkerTimes.size(); i++) {
                        double dPos = sMarkerTimes.get(i);
                        if((!bReverse && dCurPos < dPos) || (bReverse && dCurPos < dPos - 1.0)) {
                            BASS.BASS_ChannelSetPosition(MainActivity.sStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.sStream, dPos), BASS.BASS_POS_BYTE);
                            break;
                        }
                    }
                    sMarker = i;
                    MainActivity.setSync();

                    PlaylistFragment.updateSavingEffect();
                }
            }
        }
        else if(v.getId() == R.id.btnAddmarker) {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(MainActivity.sStream != 0) {
                    int nScreenWidth = mWaveView.getWidth();
                    int nMaxWidth = (int)(nScreenWidth * mWaveView.getZoom());
                    double dCurPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE));
                    double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE));
                    ImageView imgView = new ImageView(sActivity);
                    imgView.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_marker_dark : R.drawable.ic_abloop_marker);
                    mRelativeLoop.addView(imgView);
                    imgView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    int nLeft = (int) (mViewCurPos.getX() - nMaxWidth * dCurPos / dLength - imgView.getMeasuredWidth() / 2.0f);
                    int nTop = mRelativeWave.getTop() - imgView.getMeasuredHeight();
                    imgView.setTranslationX(nLeft);
                    imgView.setTranslationY(nTop);
                    imgView.requestLayout();
                    boolean bAdded = false;
                    int i = 0;
                    for( ; i < sMarkerTimes.size(); i++)
                    {
                        double dPos = sMarkerTimes.get(i);
                        if(dCurPos < dPos)
                        {
                            bAdded = true;
                            sMarkerTimes.add(i, dCurPos);
                            mMarkerTexts.add(i, imgView);
                            break;
                        }
                    }
                    if(!bAdded)
                    {
                        sMarkerTimes.add(dCurPos);
                        mMarkerTexts.add(imgView);
                    }
                    sMarker = i;

                    PlaylistFragment.updateSavingEffect();
                }
            }
        }
        else if (v.getId() == R.id.btnDelmarker) {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(MainActivity.sStream != 0) {
                    double dCurPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE));
                    for(int i = sMarkerTimes.size() - 1; i >= 0; i--) {
                        double dPos = sMarkerTimes.get(i);
                        if(dCurPos >= dPos) {
                            sMarkerTimes.remove(i);
                            ImageView imgView = mMarkerTexts.get(i);
                            mRelativeLoop.removeView(imgView);
                            mMarkerTexts.remove(i);
                            break;
                        }
                    }
                    PlaylistFragment.updateSavingEffect();
                }
            }
        }
        else if (v.getId() == R.id.btnLoopmarker) {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(mBtnLoopmarker.isSelected()) {
                    mBtnLoopmarker.setSelected(false);
                    sMarkerPlay = false;
                    mBtnLoopmarker.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_marker_loop_dark : R.drawable.ic_abloop_marker_loop);
                }
                else {
                    mBtnLoopmarker.setSelected(true);
                    sMarkerPlay = true;
                    mBtnLoopmarker.setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_marker_loop_on_dark : R.drawable.ic_abloop_marker_loop_on);
                }

                if(MainActivity.sStream != 0)
                {
                    double dCurPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE));
                    int i = 0;
                    for( ; i < sMarkerTimes.size(); i++) {
                        double dPos = sMarkerTimes.get(i);
                        if(dCurPos < dPos)
                        {
                            break;
                        }
                    }
                    sMarker = i - 1;

                    MainActivity.setSync();

                    PlaylistFragment.updateSavingEffect();
                }
            }
        }
        else if(v.getId() == R.id.waveView) {
            if(event.getAction() == MotionEvent.ACTION_DOWN) mTouching = true;
            else if(event.getAction() == MotionEvent.ACTION_UP)  mTouching = false;

            float fX = event.getX();
            int nBkWidth = mWaveView.getWidth();
            double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE));
            double dPos = dLength * fX / nBkWidth;
            if(mABButton.getVisibility() == View.VISIBLE && ((MainActivity.sLoopA && dPos < MainActivity.sLoopAPos) || (MainActivity.sLoopB && MainActivity.sLoopBPos < dPos)))
                dPos = MainActivity.sLoopAPos;
            setCurPos(dPos);
            updateCurPos();
            return true;
        }

        return false;
    }

    static void clearLoop(boolean bSave) {
        sMarkerTimes.clear();
        if(bSave) PlaylistFragment.updateSavingEffect();

        if (sActivity != null) {
            if (sActivity.loopFragment.getBtnA() != null) {
                sActivity.loopFragment.getBtnA().setSelected(false);
                sActivity.loopFragment.getBtnA().setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_a_dark : R.drawable.ic_abloop_a);
            }

            if (sActivity.loopFragment.getViewMaskA() != null) sActivity.loopFragment.getViewMaskA().setVisibility(View.INVISIBLE);

            if (sActivity.loopFragment.getBtnB() != null) {
                sActivity.loopFragment.getBtnB().setSelected(false);
                sActivity.loopFragment.getBtnB().setImageResource(sActivity.isDarkMode() ? R.drawable.ic_abloop_b_dark : R.drawable.ic_abloop_b);
            }

            if (sActivity.loopFragment.getViewMaskB() != null) sActivity.loopFragment.getViewMaskB().setVisibility(View.INVISIBLE);

            for (int i = 0; i < sActivity.loopFragment.getMarkerTexts().size(); i++) {
                ImageView imgView = sActivity.loopFragment.getMarkerTexts().get(i);
                sActivity.loopFragment.getRelativeLoop().removeView(imgView);
            }

            sActivity.loopFragment.getTextAValue().setText(sActivity.getString(R.string.zeroHMS));
            sActivity.loopFragment.getTextBValue().setText(sActivity.getString(R.string.zeroHMS));

            sActivity.loopFragment.getMarkerTexts().clear();

            sActivity.loopFragment.getWaveView().clearWaveForm(true);

            sActivity.loopFragment.getBtnZoomIn().setColorFilter(new PorterDuffColorFilter(Color.parseColor(sActivity.isDarkMode() ? "#FF939CA0" : "#FFCCCCCC"), PorterDuff.Mode.SRC_IN));
            sActivity.loopFragment.getBtnZoomIn().setEnabled(false);
            sActivity.loopFragment.getBtnZoomOut().setColorFilter(new PorterDuffColorFilter(Color.parseColor(sActivity.isDarkMode() ? "#FF939CA0" : "#FFCCCCCC"), PorterDuff.Mode.SRC_IN));
            sActivity.loopFragment.getBtnZoomOut().setEnabled(false);
        }
    }

    void drawWaveForm(String strPath) {
        if(mWaveView.getZoom() >= 10.0f) {
            mBtnZoomIn.setColorFilter(new PorterDuffColorFilter(Color.parseColor(sActivity.isDarkMode() ? "#FF939CA0" : "#FFCCCCCC"), PorterDuff.Mode.SRC_IN));
            mBtnZoomIn.setEnabled(false);
        }
        else {
            mBtnZoomIn.setColorFilter(null);
            mBtnZoomIn.setEnabled(true);
        }
        if(mWaveView.getZoom() <= 1.0f) {
            mBtnZoomOut.setColorFilter(new PorterDuffColorFilter(Color.parseColor(sActivity.isDarkMode() ? "#FF939CA0" : "#FFCCCCCC"), PorterDuff.Mode.SRC_IN));
            mBtnZoomOut.setEnabled(false);
        }
        else {
            mBtnZoomOut.setColorFilter(null);
            mBtnZoomOut.setEnabled(true);
        }
        double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE));
        int nMinute = (int)(dLength / 60);
        int nSecond = (int)(dLength % 60);
        int nHour = nMinute / 60;
        nMinute = nMinute % 60;
        int nDec = (int)((dLength * 100) % 100);
        String strTextBValue = (nHour < 10 ? "0" : "") + nHour + (nMinute < 10 ? ":0" : ":") + nMinute + (nSecond < 10 ? ":0" : ":") + nSecond + (nDec < 10 ? ".0" : ".") + nDec;
        mTextBValue.setText(strTextBValue);
        mWaveView.drawWaveForm(strPath);
    }

    static double getMarkerSrcPos() {
        double dPos = 0.0;

        if(EffectFragment.isReverse()) {
            if(!LoopFragment.sABLoop && LoopFragment.sMarkerPlay) // マーカー再生中
            {
                dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE));
                if(sMarker >= 0 && sMarker < sMarkerTimes.size()) {
                    dPos = sMarkerTimes.get(sMarker);
                }
            }
        }
        else {
            if(!LoopFragment.sABLoop && LoopFragment.sMarkerPlay) // マーカー再生中
            {
                if(sMarker >= 0 && sMarker < sMarkerTimes.size()) {
                    dPos = sMarkerTimes.get(sMarker);
                }
            }
        }
        return dPos;
    }

    static double getMarkerDstPos() {
        double dPos = 0.0;

        if(EffectFragment.isReverse()) {
            if(!LoopFragment.sABLoop && LoopFragment.sMarkerPlay) // マーカー再生中
            {
                if(sMarker - 1 >= 0 && sMarker - 1 < sMarkerTimes.size()) {
                    dPos = sMarkerTimes.get(sMarker - 1);
                }
            }
        }
        else {
            if(!LoopFragment.sABLoop && LoopFragment.sMarkerPlay) // マーカー再生中
            {
                dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE));
                if(sMarker + 1 >= 0 && sMarker + 1 < sMarkerTimes.size()) {
                    dPos = sMarkerTimes.get(sMarker + 1);
                }
            }
        }
        return dPos;
    }

    public void setLightMode(boolean animated) {
        final int nLightModeBk = getResources().getColor(R.color.lightModeBk);
        final int nDarkModeBk = getResources().getColor(R.color.darkModeBk);
        final int nLightModeSep = getResources().getColor(R.color.lightModeSep);
        final int nDarkModeSep = getResources().getColor(R.color.darkModeSep);
        final int nLightModeText = getResources().getColor(android.R.color.black);
        final int nDarkModeText = getResources().getColor(android.R.color.white);
        final int nDarkModeGray = getResources().getColor(R.color.darkModeGray);
        if(animated) {
            final ArgbEvaluator eval = new ArgbEvaluator();
            ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float fProgress = valueAnimator.getAnimatedFraction();
                    int nColorModeBk = (Integer) eval.evaluate(fProgress, nDarkModeBk, nLightModeBk);
                    int nColorModeSep = (Integer) eval.evaluate(fProgress, nDarkModeSep, nLightModeSep);
                    int nColorModeText = (Integer) eval.evaluate(fProgress, nDarkModeText, nLightModeText);
                    int nColorCurPos = (Integer) eval.evaluate(fProgress, nDarkModeGray, Color.BLACK);
                    mViewMaskA.setBackgroundColor(nColorModeBk);
                    mViewMaskB.setBackgroundColor(nColorModeBk);
                    mTextA.setTextColor(nColorModeText);
                    mTextB.setTextColor(nColorModeText);
                    mViewSep1Loop.setBackgroundColor(nColorModeSep);
                    mViewSep2Loop.setBackgroundColor(nColorModeSep);
                    mViewSep3Loop.setBackgroundColor(nColorModeSep);
                    mTextAValue.setTextColor(nColorModeText);
                    mTextBValue.setTextColor(nColorModeText);
                    mTextCurValue.setTextColor(nColorModeText);
                    mViewCurPos.setBackgroundColor(nColorCurPos);
                }
            });

            TransitionDrawable tdBtnZoomIn = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_zoom_in_dark), getResources().getDrawable(R.drawable.ic_abloop_zoom_in)});
            TransitionDrawable tdBtnZoomOut = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_zoom_out_dark), getResources().getDrawable(R.drawable.ic_abloop_zoom_out)});
            TransitionDrawable tdBtnA = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_a_dark), getResources().getDrawable(R.drawable.ic_abloop_a)});
            TransitionDrawable tdBtnRewind5Sec, tdBtnRewind5Sec2;
            if((Integer)mBtnRewind5Sec.getTag() == 1) {
                tdBtnRewind5Sec = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_01sec_prev_dark), getResources().getDrawable(R.drawable.ic_abloop_01sec_prev)});
                tdBtnRewind5Sec2 = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_01sec_prev_dark), getResources().getDrawable(R.drawable.ic_abloop_01sec_prev)});
            }
            else if((Integer)mBtnRewind5Sec.getTag() == 2) {
                tdBtnRewind5Sec = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_02sec_prev_dark), getResources().getDrawable(R.drawable.ic_abloop_02sec_prev)});
                tdBtnRewind5Sec2 = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_02sec_prev_dark), getResources().getDrawable(R.drawable.ic_abloop_02sec_prev)});
            }
            else if((Integer)mBtnRewind5Sec.getTag() == 3) {
                tdBtnRewind5Sec = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_03sec_prev_dark), getResources().getDrawable(R.drawable.ic_abloop_03sec_prev)});
                tdBtnRewind5Sec2 = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_03sec_prev_dark), getResources().getDrawable(R.drawable.ic_abloop_03sec_prev)});
            }
            else if((Integer)mBtnRewind5Sec.getTag() == 5) {
                tdBtnRewind5Sec = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_05sec_prev_dark), getResources().getDrawable(R.drawable.ic_abloop_05sec_prev)});
                tdBtnRewind5Sec2 = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_05sec_prev_dark), getResources().getDrawable(R.drawable.ic_abloop_05sec_prev)});
            }
            else {
                tdBtnRewind5Sec = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_10sec_prev_dark), getResources().getDrawable(R.drawable.ic_abloop_10sec_prev)});
                tdBtnRewind5Sec2 = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_10sec_prev_dark), getResources().getDrawable(R.drawable.ic_abloop_10sec_prev)});
            }
            TransitionDrawable tdBtnForward5Sec, tdBtnForward5Sec2;
            if((Integer)mBtnForward5Sec.getTag() == 1) {
                tdBtnForward5Sec = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_01sec_next_dark), getResources().getDrawable(R.drawable.ic_abloop_01sec_next)});
                tdBtnForward5Sec2 = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_01sec_next_dark), getResources().getDrawable(R.drawable.ic_abloop_01sec_next)});
            }
            else if((Integer)mBtnForward5Sec.getTag() == 2) {
                tdBtnForward5Sec = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_02sec_next_dark), getResources().getDrawable(R.drawable.ic_abloop_02sec_next)});
                tdBtnForward5Sec2 = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_02sec_next_dark), getResources().getDrawable(R.drawable.ic_abloop_02sec_next)});
            }
            else if((Integer)mBtnForward5Sec.getTag() == 3) {
                tdBtnForward5Sec = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_03sec_next_dark), getResources().getDrawable(R.drawable.ic_abloop_03sec_next)});
                tdBtnForward5Sec2 = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_03sec_next_dark), getResources().getDrawable(R.drawable.ic_abloop_03sec_next)});
            }
            else if((Integer)mBtnForward5Sec.getTag() == 5) {
                tdBtnForward5Sec = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_05sec_next_dark), getResources().getDrawable(R.drawable.ic_abloop_05sec_next)});
                tdBtnForward5Sec2 = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_05sec_next_dark), getResources().getDrawable(R.drawable.ic_abloop_05sec_next)});
            }
            else {
                tdBtnForward5Sec = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_10sec_next_dark), getResources().getDrawable(R.drawable.ic_abloop_10sec_next)});
                tdBtnForward5Sec2 = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_10sec_next_dark), getResources().getDrawable(R.drawable.ic_abloop_10sec_next)});
            }
            TransitionDrawable tdBtnB = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_b_dark), getResources().getDrawable(R.drawable.ic_abloop_b)});
            TransitionDrawable tdBtnPrevmarker = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_marker_lead_dark), getResources().getDrawable(R.drawable.ic_abloop_marker_lead)});
            TransitionDrawable tdBtnDelmarker = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_marker_erase_dark), getResources().getDrawable(R.drawable.ic_abloop_marker_erase)});
            TransitionDrawable tdBtnAddmarker = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_marker_add_dark), getResources().getDrawable(R.drawable.ic_abloop_marker_add)});
            TransitionDrawable tdBtnNextmarker = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_marker_end_dark), getResources().getDrawable(R.drawable.ic_abloop_marker_end)});
            TransitionDrawable tdBtnLoopmarker;
            if(mBtnLoopmarker.isSelected())
                tdBtnLoopmarker = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_marker_loop_on_dark), getResources().getDrawable(R.drawable.ic_abloop_marker_loop_on)});
            else
                tdBtnLoopmarker = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_marker_loop_dark), getResources().getDrawable(R.drawable.ic_abloop_marker_loop)});

            mBtnZoomIn.setImageDrawable(tdBtnZoomIn);
            mBtnZoomOut.setImageDrawable(tdBtnZoomOut);
            mBtnA.setImageDrawable(tdBtnA);
            mBtnRewind5Sec.setImageDrawable(tdBtnRewind5Sec);
            mBtnForward5Sec.setImageDrawable(tdBtnForward5Sec);
            mBtnB.setImageDrawable(tdBtnB);
            mBtnRewind5Sec2.setImageDrawable(tdBtnRewind5Sec2);
            mBtnForward5Sec2.setImageDrawable(tdBtnForward5Sec2);
            mBtnPrevmarker.setImageDrawable(tdBtnPrevmarker);
            mBtnDelmarker.setImageDrawable(tdBtnDelmarker);
            mBtnAddmarker.setImageDrawable(tdBtnAddmarker);
            mBtnNextmarker.setImageDrawable(tdBtnNextmarker);
            mBtnLoopmarker.setImageDrawable(tdBtnLoopmarker);

            if(mWaveView.getZoom() >= 10.0f) {
                mBtnZoomIn.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#FFCCCCCC"), PorterDuff.Mode.SRC_IN));
                mBtnZoomIn.setEnabled(false);
            }
            else {
                mBtnZoomIn.setColorFilter(null);
                mBtnZoomIn.setEnabled(true);
            }
            if(mWaveView.getZoom() <= 1.0f) {
                mBtnZoomOut.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#FFCCCCCC"), PorterDuff.Mode.SRC_IN));
                mBtnZoomOut.setEnabled(false);
            }
            else {
                mBtnZoomOut.setColorFilter(null);
                mBtnZoomOut.setEnabled(true);
            }

            int duration = 300;
            anim.setDuration(duration).start();
            tdBtnZoomIn.startTransition(duration);
            tdBtnZoomOut.startTransition(duration);
            tdBtnA.startTransition(duration);
            tdBtnRewind5Sec.startTransition(duration);
            tdBtnForward5Sec.startTransition(duration);
            tdBtnB.startTransition(duration);
            tdBtnRewind5Sec2.startTransition(duration);
            tdBtnForward5Sec2.startTransition(duration);
            tdBtnPrevmarker.startTransition(duration);
            tdBtnDelmarker.startTransition(duration);
            tdBtnAddmarker.startTransition(duration);
            tdBtnNextmarker.startTransition(duration);
            tdBtnLoopmarker.startTransition(duration);

            if(MainActivity.sStream != 0) mWaveView.redrawWaveForm();
        }
        else {
            mViewMaskA.setBackgroundColor(nLightModeBk);
            mViewMaskB.setBackgroundColor(nLightModeBk);
            mTextA.setTextColor(nLightModeText);
            mTextB.setTextColor(nLightModeText);
            mViewSep1Loop.setBackgroundColor(nLightModeSep);
            mViewSep2Loop.setBackgroundColor(nLightModeSep);
            mViewSep3Loop.setBackgroundColor(nLightModeSep);
            mTextAValue.setTextColor(nLightModeText);
            mTextBValue.setTextColor(nLightModeText);
            mTextCurValue.setTextColor(nLightModeText);
            mViewCurPos.setBackgroundColor(Color.BLACK);
            mBtnZoomIn.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_zoom_in));
            mBtnZoomOut.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_zoom_out));
            mBtnA.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_a));
            if((Integer)mBtnRewind5Sec.getTag() == 1) {
                mBtnRewind5Sec.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_01sec_prev));
                mBtnRewind5Sec2.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_01sec_prev));
            }
            else if((Integer)mBtnRewind5Sec.getTag() == 2) {
                mBtnRewind5Sec.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_02sec_prev));
                mBtnRewind5Sec2.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_02sec_prev));
            }
            else if((Integer)mBtnRewind5Sec.getTag() == 3) {
                mBtnRewind5Sec.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_03sec_prev));
                mBtnRewind5Sec2.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_03sec_prev));
            }
            else if((Integer)mBtnRewind5Sec.getTag() == 5) {
                mBtnRewind5Sec.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_05sec_prev));
                mBtnRewind5Sec2.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_05sec_prev));
            }
            else {
                mBtnRewind5Sec.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_10sec_prev));
                mBtnRewind5Sec2.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_10sec_prev));
            }
            if((Integer)mBtnForward5Sec.getTag() == 1) {
                mBtnForward5Sec.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_01sec_next));
                mBtnForward5Sec2.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_01sec_next));
            }
            else if((Integer)mBtnForward5Sec.getTag() == 2) {
                mBtnForward5Sec.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_02sec_next));
                mBtnForward5Sec2.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_02sec_next));
            }
            else if((Integer)mBtnForward5Sec.getTag() == 3) {
                mBtnForward5Sec.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_03sec_next));
                mBtnForward5Sec2.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_03sec_next));
            }
            else if((Integer)mBtnForward5Sec.getTag() == 5) {
                mBtnForward5Sec.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_05sec_next));
                mBtnForward5Sec2.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_05sec_next));
            }
            else {
                mBtnForward5Sec.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_10sec_next));
                mBtnForward5Sec2.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_10sec_next));
            }
            mBtnB.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_b));
            mBtnPrevmarker.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_marker_lead));
            mBtnDelmarker.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_marker_erase));
            mBtnAddmarker.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_marker_add));
            mBtnNextmarker.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_marker_end));
            if(mBtnLoopmarker.isSelected())
                mBtnLoopmarker.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_marker_loop_on));
            else
                mBtnLoopmarker.setImageDrawable(getResources().getDrawable(R.drawable.ic_abloop_marker_loop));

            if(MainActivity.sStream != 0) mWaveView.redrawWaveForm();
        }
        mRadioButtonABLoop.setTextColor(getResources().getColorStateList(R.color.radio_text_color));
        mRadioButtonABLoop.setBackgroundResource(R.drawable.radio_left);
        mRadioButtonMarkerPlay.setTextColor(getResources().getColorStateList(R.color.radio_text_color));
        mRadioButtonMarkerPlay.setBackgroundResource(R.drawable.radio_right);
        mTextAValue.setBackgroundResource(R.drawable.editborder);
        mTextBValue.setBackgroundResource(R.drawable.editborder);
        mTextCurValue.setBackgroundResource(R.drawable.editborder);
        for(int i = 0; i < mMarkerTexts.size(); i++) {
            ImageView imgView = mMarkerTexts.get(i);
            imgView.setImageResource(R.drawable.ic_abloop_marker);
        }
    }

    public void setDarkMode(boolean animated) {
        if(sActivity == null) return;
        final int nLightModeBk = getResources().getColor(R.color.lightModeBk);
        final int nDarkModeBk = getResources().getColor(R.color.darkModeBk);
        final int nLightModeSep = getResources().getColor(R.color.lightModeSep);
        final int nDarkModeSep = getResources().getColor(R.color.darkModeSep);
        final int nLightModeText = getResources().getColor(android.R.color.black);
        final int nDarkModeText = getResources().getColor(android.R.color.white);
        final int nDarkModeGray = getResources().getColor(R.color.darkModeGray);
        final ArgbEvaluator eval = new ArgbEvaluator();
        ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fProgress = valueAnimator.getAnimatedFraction();
                int nColorModeBk = (Integer) eval.evaluate(fProgress, nLightModeBk, nDarkModeBk);
                int nColorModeSep = (Integer) eval.evaluate(fProgress, nLightModeSep, nDarkModeSep);
                int nColorModeText = (Integer) eval.evaluate(fProgress, nLightModeText, nDarkModeText);
                int nColorCurPos = (Integer) eval.evaluate(fProgress, Color.BLACK, nDarkModeGray);
                mViewMaskA.setBackgroundColor(nColorModeBk);
                mViewMaskB.setBackgroundColor(nColorModeBk);
                mTextA.setTextColor(nColorModeText);
                mTextB.setTextColor(nColorModeText);
                mViewSep1Loop.setBackgroundColor(nColorModeSep);
                mViewSep2Loop.setBackgroundColor(nColorModeSep);
                mViewSep3Loop.setBackgroundColor(nColorModeSep);
                mTextAValue.setTextColor(nColorModeText);
                mTextBValue.setTextColor(nColorModeText);
                mTextCurValue.setTextColor(nColorModeText);
                mViewCurPos.setBackgroundColor(nColorCurPos);
            }
        });

        TransitionDrawable tdBtnZoomIn = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_zoom_in), getResources().getDrawable(R.drawable.ic_abloop_zoom_in_dark)});
        TransitionDrawable tdBtnZoomOut = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_zoom_out), getResources().getDrawable(R.drawable.ic_abloop_zoom_out_dark)});
        TransitionDrawable tdBtnA = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_a), getResources().getDrawable(R.drawable.ic_abloop_a_dark)});
        TransitionDrawable tdBtnRewind5Sec, tdBtnRewind5Sec2;
        if((Integer)mBtnRewind5Sec.getTag() == 1) {
            tdBtnRewind5Sec = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_01sec_prev), getResources().getDrawable(R.drawable.ic_abloop_01sec_prev_dark)});
            tdBtnRewind5Sec2 = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_01sec_prev), getResources().getDrawable(R.drawable.ic_abloop_01sec_prev_dark)});
        }
        else if((Integer)mBtnRewind5Sec.getTag() == 2) {
            tdBtnRewind5Sec = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_02sec_prev), getResources().getDrawable(R.drawable.ic_abloop_02sec_prev_dark)});
            tdBtnRewind5Sec2 = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_02sec_prev), getResources().getDrawable(R.drawable.ic_abloop_02sec_prev_dark)});
        }
        else if((Integer)mBtnRewind5Sec.getTag() == 3) {
            tdBtnRewind5Sec = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_03sec_prev), getResources().getDrawable(R.drawable.ic_abloop_03sec_prev_dark)});
            tdBtnRewind5Sec2 = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_03sec_prev), getResources().getDrawable(R.drawable.ic_abloop_03sec_prev_dark)});
        }
        else if((Integer)mBtnRewind5Sec.getTag() == 5) {
            tdBtnRewind5Sec = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_05sec_prev), getResources().getDrawable(R.drawable.ic_abloop_05sec_prev_dark)});
            tdBtnRewind5Sec2 = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_05sec_prev), getResources().getDrawable(R.drawable.ic_abloop_05sec_prev_dark)});
        }
        else {
            tdBtnRewind5Sec = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_10sec_prev), getResources().getDrawable(R.drawable.ic_abloop_10sec_prev_dark)});
            tdBtnRewind5Sec2 = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_10sec_prev), getResources().getDrawable(R.drawable.ic_abloop_10sec_prev_dark)});
        }
        TransitionDrawable tdBtnForward5Sec, tdBtnForward5Sec2;
        if((Integer)mBtnForward5Sec.getTag() == 1) {
            tdBtnForward5Sec = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_01sec_next), getResources().getDrawable(R.drawable.ic_abloop_01sec_next_dark)});
            tdBtnForward5Sec2 = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_01sec_next), getResources().getDrawable(R.drawable.ic_abloop_01sec_next_dark)});
        }
        else if((Integer)mBtnForward5Sec.getTag() == 2) {
            tdBtnForward5Sec = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_02sec_next), getResources().getDrawable(R.drawable.ic_abloop_02sec_next_dark)});
            tdBtnForward5Sec2 = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_02sec_next), getResources().getDrawable(R.drawable.ic_abloop_02sec_next_dark)});
        }
        else if((Integer)mBtnForward5Sec.getTag() == 3) {
            tdBtnForward5Sec = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_03sec_next), getResources().getDrawable(R.drawable.ic_abloop_03sec_next_dark)});
            tdBtnForward5Sec2 = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_03sec_next), getResources().getDrawable(R.drawable.ic_abloop_03sec_next_dark)});
        }
        else if((Integer)mBtnForward5Sec.getTag() == 5) {
            tdBtnForward5Sec = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_05sec_next), getResources().getDrawable(R.drawable.ic_abloop_05sec_next_dark)});
            tdBtnForward5Sec2 = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_05sec_next), getResources().getDrawable(R.drawable.ic_abloop_05sec_next_dark)});
        }
        else {
            tdBtnForward5Sec = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_10sec_next), getResources().getDrawable(R.drawable.ic_abloop_10sec_next_dark)});
            tdBtnForward5Sec2 = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_10sec_next), getResources().getDrawable(R.drawable.ic_abloop_10sec_next_dark)});
        }
        TransitionDrawable tdBtnB = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_b), getResources().getDrawable(R.drawable.ic_abloop_b_dark)});
        TransitionDrawable tdBtnPrevmarker = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_marker_lead), getResources().getDrawable(R.drawable.ic_abloop_marker_lead_dark)});
        TransitionDrawable tdBtnDelmarker = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_marker_erase), getResources().getDrawable(R.drawable.ic_abloop_marker_erase_dark)});
        TransitionDrawable tdBtnAddmarker = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_marker_add), getResources().getDrawable(R.drawable.ic_abloop_marker_add_dark)});
        TransitionDrawable tdBtnNextmarker = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_marker_end), getResources().getDrawable(R.drawable.ic_abloop_marker_end_dark)});
        TransitionDrawable tdBtnLoopmarker;
        if(mBtnLoopmarker.isSelected())
            tdBtnLoopmarker = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_marker_loop_on), getResources().getDrawable(R.drawable.ic_abloop_marker_loop_on_dark)});
        else
            tdBtnLoopmarker = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_abloop_marker_loop), getResources().getDrawable(R.drawable.ic_abloop_marker_loop_dark)});

        mBtnZoomIn.setImageDrawable(tdBtnZoomIn);
        mBtnZoomOut.setImageDrawable(tdBtnZoomOut);
        mBtnA.setImageDrawable(tdBtnA);
        mBtnRewind5Sec.setImageDrawable(tdBtnRewind5Sec);
        mBtnForward5Sec.setImageDrawable(tdBtnForward5Sec);
        mBtnB.setImageDrawable(tdBtnB);
        mBtnRewind5Sec2.setImageDrawable(tdBtnRewind5Sec2);
        mBtnForward5Sec2.setImageDrawable(tdBtnForward5Sec2);
        mBtnPrevmarker.setImageDrawable(tdBtnPrevmarker);
        mBtnDelmarker.setImageDrawable(tdBtnDelmarker);
        mBtnAddmarker.setImageDrawable(tdBtnAddmarker);
        mBtnNextmarker.setImageDrawable(tdBtnNextmarker);
        mBtnLoopmarker.setImageDrawable(tdBtnLoopmarker);
        if(mWaveView.getZoom() >= 10.0f) {
            mBtnZoomIn.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#FF939CA0"), PorterDuff.Mode.SRC_IN));
            mBtnZoomIn.setEnabled(false);
        }
        else {
            mBtnZoomIn.setColorFilter(null);
            mBtnZoomIn.setEnabled(true);
        }
        if(mWaveView.getZoom() <= 1.0f) {
            mBtnZoomOut.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#FF939CA0"), PorterDuff.Mode.SRC_IN));
            mBtnZoomOut.setEnabled(false);
        }
        else {
            mBtnZoomOut.setColorFilter(null);
            mBtnZoomOut.setEnabled(true);
        }

        int duration = animated ? 300 : 0;
        anim.setDuration(duration).start();
        tdBtnZoomIn.startTransition(duration);
        tdBtnZoomOut.startTransition(duration);
        tdBtnA.startTransition(duration);
        tdBtnRewind5Sec.startTransition(duration);
        tdBtnForward5Sec.startTransition(duration);
        tdBtnB.startTransition(duration);
        tdBtnRewind5Sec2.startTransition(duration);
        tdBtnForward5Sec2.startTransition(duration);
        tdBtnPrevmarker.startTransition(duration);
        tdBtnDelmarker.startTransition(duration);
        tdBtnAddmarker.startTransition(duration);
        tdBtnNextmarker.startTransition(duration);
        tdBtnLoopmarker.startTransition(duration);

        if(MainActivity.sStream != 0) mWaveView.redrawWaveForm();

        mRadioButtonABLoop.setTextColor(getResources().getColorStateList(R.color.radio_text_color_dark));
        mRadioButtonABLoop.setBackgroundResource(R.drawable.radio_left_dark);
        mRadioButtonMarkerPlay.setTextColor(getResources().getColorStateList(R.color.radio_text_color_dark));
        mRadioButtonMarkerPlay.setBackgroundResource(R.drawable.radio_right_dark);
        mTextAValue.setBackgroundResource(R.drawable.editborder_dark);
        mTextBValue.setBackgroundResource(R.drawable.editborder_dark);
        mTextCurValue.setBackgroundResource(R.drawable.editborder_dark);
        for(int i = 0; i < mMarkerTexts.size(); i++) {
            ImageView imgView = mMarkerTexts.get(i);
            imgView.setImageResource(R.drawable.ic_abloop_marker_dark);
        }
    }
}
