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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.un4seen.bass.BASS;

import java.util.ArrayList;
import java.util.Locale;

public class LoopFragment extends Fragment implements View.OnTouchListener, View.OnFocusChangeListener, View.OnLongClickListener {
    private int nMarker; // マーカー再生時のループ位置
    private final Handler handler;
    private MainActivity activity;

    private View viewCurPos;
    private WaveView waveView;

    private ArrayList<Double> arMarkerTime;
    private final ArrayList<TextView> arMarkerText;
    private boolean bContinue = false;

    public ArrayList<Double>  getArMarkerTime() { return arMarkerTime; }
    public void setArMarkerTime(ArrayList<Double> arMarkerTime) {
        if(arMarkerTime == null) return;
        this.arMarkerTime = new ArrayList<>();
        int nScreenWidth = waveView.getWidth();
        int nMaxWidth = (int)(nScreenWidth * waveView.getZoom());
        double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
        for(int i = 0; i < arMarkerTime.size(); i++) {
            double dPos = arMarkerTime.get(i);
            this.arMarkerTime.add(dPos);

            TextView textView = new TextView(activity);
            textView.setText("▼");
            RelativeLayout layout = activity.findViewById(R.id.relative_loop);
            layout.addView(textView);
            textView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            int nLeft = (int) (viewCurPos.getX() - nMaxWidth * dPos / dLength - textView.getMeasuredWidth() / 2.0f);
            int nTop = waveView.getTop() - textView.getMeasuredHeight();
            textView.setTranslationX(nLeft);
            textView.setTranslationY(nTop);
            textView.requestLayout();
            arMarkerText.add(i, textView);
        }
    }
    public int getMarker() { return nMarker; }
    public void setMarker(int nMarker) { this.nMarker = nMarker; }

    public LoopFragment()
    {
        nMarker = 0;
        handler = new Handler();
        arMarkerText = new ArrayList<>();
        arMarkerTime = new ArrayList<>();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_loop, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewCurPos = activity.findViewById(R.id.viewCurPos);
        waveView = activity.findViewById(R.id.waveView);
        waveView.setLoopFragment(this);
        waveView.setOnTouchListener(this);
        final long lDelay = 250;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (activity != null) {
                    LinearLayout ABButton = activity.findViewById(R.id.ABButton);
                    long nLength = 0;
                    long nPos = 0;
                    int nScreenWidth = waveView.getWidth();
                    int nMaxWidth = (int)(nScreenWidth * waveView.getZoom());
                    int nLeft = -(int)(1.0 * getResources().getDisplayMetrics().density + 0.5);
                    EditText textCurValue = activity.findViewById(R.id.textCurValue);
                    if(MainActivity.hStream != 0) {
                        double dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                        if (ABButton.getVisibility() == View.VISIBLE && ((activity.bLoopA && dPos < activity.dLoopA) || (activity.bLoopB && activity.dLoopB < dPos)) && !activity.isPlayNextByBPos()) {
                            if (activity.effectFragment.isReverse())
                                dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopB));
                            else
                                dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopA));
                            BASS.BASS_ChannelSetPosition(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, dPos), BASS.BASS_POS_BYTE);
                        }
                        if (dPos < 0) dPos = 0;
                        int nMinute = (int) (dPos / 60);
                        int nSecond = (int) (dPos % 60);
                        int nHour = nMinute / 60;

                        double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
                        if(dLength < 0) dLength = 0;
                        double dRemain = dLength - dPos;
                        int nRemainMinute = (int)(dRemain / 60);
                        int nRemainSecond = (int)(dRemain % 60);

                        TextView textCurPos = activity.findViewById(R.id.textCurPos);
                        textCurPos.setText(String.format(Locale.getDefault(), "%d:%02d", nMinute, nSecond));
                        TextView textRemain = activity.findViewById(R.id.textRemain);
                        textRemain.setText(String.format(Locale.getDefault(), "-%d:%02d", nRemainMinute, nRemainSecond));
                        SeekBar seekCurPos = activity.findViewById(R.id.seekCurPos);
                        seekCurPos.setMax((int)dLength);
                        seekCurPos.setProgress((int)dPos);

                        nMinute = nMinute % 60;
                        int nDec = (int) ((dPos * 100) % 100);
                        textCurValue.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d.%02d", nHour, nMinute, nSecond, nDec));
                        nLength = BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE);
                        nPos = BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE);
                        nLeft = (int) (nMaxWidth * nPos / nLength);
                    }
                    else
                        textCurValue.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d.%02d", 0, 0, 0, 0));
                    int nTop = viewCurPos.getTop();
                    if(nLeft < nScreenWidth / 2) {
                        viewCurPos.animate()
                                .x(nLeft)
                                .y(nTop)
                                .setDuration(lDelay)
                                .setInterpolator(new LinearInterpolator())
                                .start();
                        if(!bContinue) waveView.invalidate();
                    }
                    else if(nScreenWidth / 2 <= nLeft && nLeft < nMaxWidth - nScreenWidth / 2) {
                        viewCurPos.animate()
                                .x(nScreenWidth / 2.0f)
                                .y(nTop)
                                .setDuration(lDelay)
                                .setInterpolator(new LinearInterpolator())
                                .start();
                        if(!bContinue) waveView.invalidate();
                    }
                    else {
                        viewCurPos.animate()
                                .x(nScreenWidth - (nMaxWidth - nLeft))
                                .y(nTop)
                                .setDuration(lDelay)
                                .setInterpolator(new LinearInterpolator())
                                .start();
                        if(!bContinue) waveView.invalidate();
                    }
                    View viewMaskA = activity.findViewById(R.id.viewMaskA);
                    View viewMaskB = activity.findViewById(R.id.viewMaskB);
                    if(activity.bLoopA) {
                        long nPosA = 0;
                        if(MainActivity.hStream != 0)
                            nPosA = BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopA);
                        nPosA = nPos - nPosA;
                        int nLeftA = (int) (viewCurPos.getX() - nMaxWidth * nPosA / nLength);
                        if(nLeftA < 0) nLeftA = 0;
                        viewMaskA.getLayoutParams().width = nLeftA;
                        viewMaskA.requestLayout();
                    }
                    if(activity.bLoopB) {
                        long nPosB = 0;
                        if(MainActivity.hStream != 0)
                            nPosB = BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopB);
                        nPosB = nPos - nPosB;
                        int nLeftB = (int) (viewCurPos.getX() - nMaxWidth * nPosB / nLength);
                        if(nLeftB < 0) nLeftB = 0;
                        else if(nLeftB > waveView.getWidth()) nLeftB = waveView.getWidth();
                        viewMaskB.setTranslationX(nLeftB);
                        viewMaskB.getLayoutParams().width = waveView.getWidth() - nLeftB;
                        viewMaskB.requestLayout();
                    }
                    final LinearLayout MarkerButton = activity.findViewById(R.id.MarkerButton);
                    if(MarkerButton.getVisibility() == View.VISIBLE) {
                        for (int i = 0; i < arMarkerTime.size(); i++) {
                            TextView textView = arMarkerText.get(i);
                            double dMarkerPos = arMarkerTime.get(i);
                            long nMarkerPos = BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, dMarkerPos);
                            nMarkerPos = nPos - nMarkerPos;
                            int nMarkerLeft = (int) (viewCurPos.getX() - nMaxWidth * nMarkerPos / (float)nLength - textView.getMeasuredWidth() / 2.0f);
                            if (nMarkerLeft < -textView.getMeasuredWidth())
                                nMarkerLeft = -textView.getMeasuredWidth();
                            textView.setTranslationX(nMarkerLeft);
                            textView.requestLayout();
                        }
                    }
                }
                handler.postDelayed(this, lDelay);
            }
        });
        RelativeLayout relativeZoomOut = activity.findViewById(R.id.relativeZoomOut);
        relativeZoomOut.setOnTouchListener(this);
        relativeZoomOut.setOnLongClickListener(this);
        RelativeLayout relativeZoomIn = activity.findViewById(R.id.relativeZoomIn);
        relativeZoomIn.setOnTouchListener(this);
        relativeZoomIn.setOnLongClickListener(this);
        activity.findViewById(R.id.viewBtnALeft).setOnTouchListener(this);
        AnimationButton btnA = activity.findViewById(R.id.btnA);
        btnA.setSelected(false);
        btnA.setImageResource(R.drawable.ic_abloop_a);
        btnA.setOnTouchListener(this);
        activity.findViewById(R.id.viewBtnARight).setOnTouchListener(this);
        activity.findViewById(R.id.viewBtnRewindLeft).setOnTouchListener(this);
        AnimationButton btnRewind5Sec = activity.findViewById(R.id.btnRewind5Sec);
        btnRewind5Sec.setOnTouchListener(this);
        btnRewind5Sec.setOnLongClickListener(this);
        btnRewind5Sec.setTag(5);
        activity.findViewById(R.id.viewBtnRewindRight).setOnTouchListener(this);
        activity.findViewById(R.id.viewBtnForwardLeft).setOnTouchListener(this);
        AnimationButton btnForward5Sec = activity.findViewById(R.id.btnForward5Sec);
        btnForward5Sec.setOnTouchListener(this);
        btnForward5Sec.setOnLongClickListener(this);
        btnForward5Sec.setTag(5);
        activity.findViewById(R.id.viewBtnForwardRight).setOnTouchListener(this);
        activity.findViewById(R.id.viewBtnBLeft).setOnTouchListener(this);
        AnimationButton btnB = activity.findViewById(R.id.btnB);
        btnB.setSelected(false);
        btnB.setImageResource(R.drawable.ic_abloop_b);
        btnB.setOnTouchListener(this);
        activity.findViewById(R.id.viewBtnBRight).setOnTouchListener(this);

        AnimationButton btnRewind5Sec2 = activity.findViewById(R.id.btnRewind5Sec2);
        btnRewind5Sec2.setOnTouchListener(this);
        btnRewind5Sec2.setOnLongClickListener(this);
        btnRewind5Sec2.setTag(5);
        AnimationButton btnPrevmarker = activity.findViewById(R.id.btnPrevmarker);
        btnPrevmarker.setOnTouchListener(this);
        AnimationButton btnDelmarker= activity.findViewById(R.id.btnDelmarker);
        btnDelmarker.setOnTouchListener(this);
        AnimationButton btnAddmarker = activity.findViewById(R.id.btnAddmarker);
        btnAddmarker.setOnTouchListener(this);
        AnimationButton btnNextmarker = activity.findViewById(R.id.btnNextmarker);
        btnNextmarker.setOnTouchListener(this);
        AnimationButton btnLoopmarker = activity.findViewById(R.id.btnLoopmarker);
        btnLoopmarker.setSelected(false);
        btnLoopmarker.setImageResource(R.drawable.ic_abloop_marker_loop);
        btnLoopmarker.setOnTouchListener(this);
        AnimationButton btnForward5Sec2 = activity.findViewById(R.id.btnForward5Sec2);
        btnForward5Sec2.setOnTouchListener(this);
        btnForward5Sec.setOnLongClickListener(this);
        btnForward5Sec.setTag(5);

        final LinearLayout ABLabel = activity.findViewById(R.id.ABLabel);
        final LinearLayout ABButton = activity.findViewById(R.id.ABButton);
        final LinearLayout MarkerButton = activity.findViewById(R.id.MarkerButton);
        final View viewMaskA = activity.findViewById(R.id.viewMaskA);
        final View viewMaskB = activity.findViewById(R.id.viewMaskB);

        final RadioGroup radioGroupLoopMode = activity.findViewById(R.id.radioGroupLoopMode);
        radioGroupLoopMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int nItem) {
                if(nItem == R.id.radioButtonABLoop) {
                    ABLabel.setVisibility(View.VISIBLE);
                    ABButton.setVisibility(View.VISIBLE);
                    if(activity.bLoopA) viewMaskA.setVisibility(View.VISIBLE);
                    if(activity.bLoopB) viewMaskB.setVisibility(View.VISIBLE);
                    activity.setSync();
                    activity.playlistFragment.updateSavingEffect();

                    MarkerButton.setVisibility(View.INVISIBLE);
                    for(int i = 0 ; i < arMarkerText.size(); i++)
                    {
                        TextView textView = arMarkerText.get(i);
                        textView.setVisibility(View.INVISIBLE);
                    }
                }
                else {
                    MarkerButton.setVisibility(View.VISIBLE);
                    for(int i = 0 ; i < arMarkerText.size(); i++)
                    {
                        TextView textView = arMarkerText.get(i);
                        textView.setVisibility(View.VISIBLE);
                    }
                    activity.setSync();
                    activity.playlistFragment.updateSavingEffect();

                    ABLabel.setVisibility(View.INVISIBLE);
                    ABButton.setVisibility(View.INVISIBLE);
                    viewMaskA.setVisibility(View.INVISIBLE);
                    viewMaskB.setVisibility(View.INVISIBLE);
                }
            }
        });

        activity.findViewById(R.id.textAValue).setOnFocusChangeListener(this);
        activity.findViewById(R.id.textBValue).setOnFocusChangeListener(this);
        activity.findViewById(R.id.textCurValue).setOnFocusChangeListener(this);
    }

    private void setZoomOut() {
        waveView = activity.findViewById(R.id.waveView);
        waveView.setZoom(waveView.getZoom() * 0.99f);
    }

    private void setZoomIn() {
        waveView = activity.findViewById(R.id.waveView);
        waveView.setZoom(waveView.getZoom() * 1.01f);
    }

    private final Runnable repeatZoomOut = new Runnable()
    {
        @Override
        public void run()
        {
            if(!bContinue)
                return;
            setZoomOut();
            long nLength = BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE);
            long nPos = BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE);
            int nScreenWidth = waveView.getWidth();
            int nMaxWidth = (int)(nScreenWidth * waveView.getZoom());
            int nLeft = (int) (nMaxWidth * nPos / nLength);
            if(nLeft < nScreenWidth / 2)
                waveView.setPivotX(0.0f);
            else if(nScreenWidth / 2 <= nLeft && nLeft < nMaxWidth - nScreenWidth / 2)
                waveView.setPivotX(0.5f);
            else
                waveView.setPivotX(1.0f);
            waveView.setScaleX(waveView.getZoom());
            handler.postDelayed(this, 10);
        }
    };

    private final Runnable repeatZoomIn = new Runnable()
    {
        @Override
        public void run()
        {
            if(!bContinue)
                return;
            setZoomIn();
            long nLength = BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE);
            long nPos = BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE);
            int nScreenWidth = waveView.getWidth();
            int nMaxWidth = (int)(nScreenWidth * waveView.getZoom());
            int nLeft = (int) (nMaxWidth * nPos / nLength);
            if(nLeft < nScreenWidth / 2)
                waveView.setPivotX(0.0f);
            else if(nScreenWidth / 2 <= nLeft && nLeft < nMaxWidth - nScreenWidth / 2)
                waveView.setPivotX(0.5f);
            else
                waveView.setPivotX(1.0f);
            waveView.setScaleX(waveView.getZoom());
            handler.postDelayed(this, 10);
        }
    };

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.relativeZoomOut) {
            if(MainActivity.hStream == 0) return false;
            bContinue = true;
            handler.post(repeatZoomOut);
            return true;
        }
        else if (v.getId() == R.id.relativeZoomIn) {
            if(MainActivity.hStream == 0) return false;
            bContinue = true;
            handler.post(repeatZoomIn);
            return true;
        }
        else if(v.getId() == R.id.btnRewind5Sec || v.getId() == R.id.btnRewind5Sec2) {
            final BottomMenu menu = new BottomMenu(activity);
            menu.setTitle(getString(R.string.chooseRewindButton));
            final AnimationButton btnRewind5Sec = activity.findViewById(R.id.btnRewind5Sec);
            final AnimationButton btnRewind5Sec2 = activity.findViewById(R.id.btnRewind5Sec2);
            menu.addMenu(getString(R.string.rewind1Sec), R.drawable.ic_actionsheet_01sec_prev, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnRewind5Sec.setImageResource(R.drawable.ic_abloop_01sec_prev);
                    btnRewind5Sec2.setImageResource(R.drawable.ic_abloop_01sec_prev);
                    btnRewind5Sec.setContentDescription(getString(R.string.rewind1Sec));
                    btnRewind5Sec2.setContentDescription(getString(R.string.rewind1Sec));
                    btnRewind5Sec.setTag(1);
                    btnRewind5Sec2.setTag(1);
                    menu.dismiss();
                }
            });
            menu.addMenu(getString(R.string.rewind2Sec), R.drawable.ic_actionsheet_02sec_prev, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnRewind5Sec.setImageResource(R.drawable.ic_abloop_02sec_prev);
                    btnRewind5Sec2.setImageResource(R.drawable.ic_abloop_02sec_prev);
                    btnRewind5Sec.setContentDescription(getString(R.string.rewind2Sec));
                    btnRewind5Sec2.setContentDescription(getString(R.string.rewind2Sec));
                    btnRewind5Sec.setTag(2);
                    btnRewind5Sec2.setTag(2);
                    menu.dismiss();
                }
            });
            menu.addMenu(getString(R.string.rewind3Sec), R.drawable.ic_actionsheet_03sec_prev, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnRewind5Sec.setImageResource(R.drawable.ic_abloop_03sec_prev);
                    btnRewind5Sec2.setImageResource(R.drawable.ic_abloop_03sec_prev);
                    btnRewind5Sec.setContentDescription(getString(R.string.rewind3Sec));
                    btnRewind5Sec2.setContentDescription(getString(R.string.rewind3Sec));
                    btnRewind5Sec.setTag(3);
                    btnRewind5Sec2.setTag(3);
                    menu.dismiss();
                }
            });
            menu.addMenu(getString(R.string.rewind5Sec), R.drawable.ic_actionsheet_05sec_prev, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnRewind5Sec.setImageResource(R.drawable.ic_abloop_05sec_prev);
                    btnRewind5Sec2.setImageResource(R.drawable.ic_abloop_05sec_prev);
                    btnRewind5Sec.setContentDescription(getString(R.string.rewind5Sec));
                    btnRewind5Sec2.setContentDescription(getString(R.string.rewind5Sec));
                    btnRewind5Sec.setTag(5);
                    btnRewind5Sec2.setTag(5);
                    menu.dismiss();
                }
            });
            menu.addMenu(getString(R.string.rewind10Sec), R.drawable.ic_actionsheet_10sec_prev, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnRewind5Sec.setImageResource(R.drawable.ic_abloop_10sec_prev);
                    btnRewind5Sec2.setImageResource(R.drawable.ic_abloop_10sec_prev);
                    btnRewind5Sec.setContentDescription(getString(R.string.rewind10Sec));
                    btnRewind5Sec2.setContentDescription(getString(R.string.rewind10Sec));
                    btnRewind5Sec.setTag(10);
                    btnRewind5Sec2.setTag(10);
                    menu.dismiss();
                }
            });
            menu.setCancelMenu();
            menu.show();
        }
        else if(v.getId() == R.id.btnForward5Sec || v.getId() == R.id.btnForward5Sec2) {
            final BottomMenu menu = new BottomMenu(activity);
            menu.setTitle(getString(R.string.chooseForwardButton));
            final AnimationButton btnForward5Sec = activity.findViewById(R.id.btnForward5Sec);
            final AnimationButton btnForward5Sec2 = activity.findViewById(R.id.btnForward5Sec2);
            menu.addMenu(getString(R.string.forward1Sec), R.drawable.ic_actionsheet_01sec_next, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnForward5Sec.setImageResource(R.drawable.ic_abloop_01sec_next);
                    btnForward5Sec2.setImageResource(R.drawable.ic_abloop_01sec_next);
                    btnForward5Sec.setContentDescription(getString(R.string.forward1Sec));
                    btnForward5Sec2.setContentDescription(getString(R.string.forward1Sec));
                    btnForward5Sec.setTag(1);
                    btnForward5Sec2.setTag(1);
                    menu.dismiss();
                }
            });
            menu.addMenu(getString(R.string.forward2Sec), R.drawable.ic_actionsheet_02sec_next, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnForward5Sec.setImageResource(R.drawable.ic_abloop_02sec_next);
                    btnForward5Sec2.setImageResource(R.drawable.ic_abloop_02sec_next);
                    btnForward5Sec.setContentDescription(getString(R.string.forward2Sec));
                    btnForward5Sec2.setContentDescription(getString(R.string.forward2Sec));
                    btnForward5Sec.setTag(2);
                    btnForward5Sec2.setTag(2);
                    menu.dismiss();
                }
            });
            menu.addMenu(getString(R.string.forward3Sec), R.drawable.ic_actionsheet_03sec_next, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnForward5Sec.setImageResource(R.drawable.ic_abloop_03sec_next);
                    btnForward5Sec2.setImageResource(R.drawable.ic_abloop_03sec_next);
                    btnForward5Sec.setContentDescription(getString(R.string.forward3Sec));
                    btnForward5Sec2.setContentDescription(getString(R.string.forward3Sec));
                    btnForward5Sec.setTag(3);
                    btnForward5Sec2.setTag(3);
                    menu.dismiss();
                }
            });
            menu.addMenu(getString(R.string.forward5Sec), R.drawable.ic_actionsheet_05sec_next, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnForward5Sec.setImageResource(R.drawable.ic_abloop_05sec_next);
                    btnForward5Sec2.setImageResource(R.drawable.ic_abloop_05sec_next);
                    btnForward5Sec.setContentDescription(getString(R.string.forward5Sec));
                    btnForward5Sec2.setContentDescription(getString(R.string.forward5Sec));
                    btnForward5Sec.setTag(5);
                    btnForward5Sec2.setTag(5);
                    menu.dismiss();
                }
            });
            menu.addMenu(getString(R.string.forward10Sec), R.drawable.ic_actionsheet_10sec_next, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnForward5Sec.setImageResource(R.drawable.ic_abloop_10sec_next);
                    btnForward5Sec2.setImageResource(R.drawable.ic_abloop_10sec_next);
                    btnForward5Sec.setContentDescription(getString(R.string.forward10Sec));
                    btnForward5Sec2.setContentDescription(getString(R.string.forward10Sec));
                    btnForward5Sec.setTag(10);
                    btnForward5Sec2.setTag(10);
                    menu.dismiss();
                }
            });
            menu.setCancelMenu();
            menu.show();
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if(hasFocus)
        {
            if(v.getId() == R.id.textAValue)
                showABLoopPicker(true);
            else if(v.getId() == R.id.textBValue)
                showABLoopPicker(false);
            else if(v.getId() == R.id.textCurValue)
                showCurPicker();
        }
    }

    private void showABLoopPicker(boolean bAPos) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.ablooppicker, (ViewGroup)activity.findViewById(R.id.layout_root), false);
        double dValue;
        if(bAPos) dValue = activity.dLoopA;
        else dValue = activity.dLoopB;
        int nMinute = (int)(dValue / 60);
        int nSecond = (int)(dValue % 60);
        int nHour = nMinute / 60;
        nMinute = nMinute % 60;
        int nDec = (int)((dValue * 100) % 100);
        String strHour = String.format(Locale.getDefault(), "%02d", nHour);
        String strMinute = String.format(Locale.getDefault(), "%02d", nMinute);
        String strSecond = String.format(Locale.getDefault(), "%02d", nSecond);
        String strDec = String.format(Locale.getDefault(), "%02d", nDec);

        final NumberPicker hourPicker = view.findViewById(R.id.abLoopHourPicker);
        final String[] arInts = {"59", "58", "57", "56", "55", "54", "53", "52", "51", "50", "49", "48", "47", "46", "45", "44", "43", "42", "41", "40", "39", "38", "37", "36", "35", "34", "33", "32", "31", "30", "29", "28", "27", "26", "25", "24", "23", "22", "21", "20", "19", "18", "17", "16", "15", "14", "13", "12", "11", "10", "09", "08", "07", "06", "05", "04", "03", "02", "01", "00"};
        hourPicker.setDisplayedValues(arInts);
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(59);
        for(int i = 0; i < arInts.length; i++)
        {
            if(arInts[i].equals(strHour))
                hourPicker.setValue(i);
        }

        final NumberPicker minutePicker = view.findViewById(R.id.abLoopMinutePicker);
        minutePicker.setDisplayedValues(arInts);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        for(int i = 0; i < arInts.length; i++)
        {
            if(arInts[i].equals(strMinute))
                minutePicker.setValue(i);
        }
        final NumberPicker secondPicker = view.findViewById(R.id.abLoopSecondPicker);
        secondPicker.setDisplayedValues(arInts);
        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(59);
        for(int i = 0; i < arInts.length; i++)
        {
            if(arInts[i].equals(strSecond))
                secondPicker.setValue(i);
        }

        final NumberPicker decimalPicker = view.findViewById(R.id.abLoopDecimalPicker);
        final String[] arDecimals = {"99", "98", "97", "96", "95", "94", "93", "92", "91", "90", "89", "88", "87", "86", "85", "84", "83", "82", "81", "80", "79", "78", "77", "76", "75", "74", "73", "72", "71", "70", "69", "68", "67", "66", "65", "64", "63", "62", "61", "60", "59", "58", "57", "56", "55", "54", "53", "52", "51", "50", "49", "48", "47", "46", "45", "44", "43", "42", "41", "40", "39", "38", "37", "36", "35", "34", "33", "32", "31", "30", "29", "28", "27", "26", "25", "24", "23", "22", "21", "20", "19", "18", "17", "16", "15", "14", "13", "12", "11", "10", "09", "08", "07", "06", "05", "04", "03", "02", "01", "00"};
        decimalPicker.setDisplayedValues(arDecimals);
        decimalPicker.setMinValue(0);
        decimalPicker.setMaxValue(99);
        for(int i = 0; i < arDecimals.length; i++)
        {
            if(arDecimals[i].equals(strDec))
                decimalPicker.setValue(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        if(bAPos) builder.setTitle(R.string.adjustAPos);
        else builder.setTitle(R.string.adjustBPos);
        final boolean f_bAPos = bAPos;
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                LayoutInflater inflater = activity.getLayoutInflater();
                inflater.inflate(R.layout.ablooppicker, (ViewGroup)activity.findViewById(R.id.layout_root), false);
                int nHour = Integer.parseInt(arInts[hourPicker.getValue()]);
                int nMinute = Integer.parseInt(arInts[minutePicker.getValue()]);
                int nSecond = Integer.parseInt(arInts[secondPicker.getValue()]);
                double dDec = Double.parseDouble(arDecimals[decimalPicker.getValue()]);
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
        builder.show();
    }

    private void showCurPicker() {
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.ablooppicker, (ViewGroup)activity.findViewById(R.id.layout_root), false);
        EditText textCurValue = activity.findViewById(R.id.textCurValue);
        String strCurPos = textCurValue.getText().toString();
        String strHour = strCurPos.substring(0, 2);
        String strMinute = strCurPos.substring(3, 5);
        String strSecond = strCurPos.substring(6, 8);
        String strDec = strCurPos.substring(9, 11);

        final NumberPicker hourPicker = view.findViewById(R.id.abLoopHourPicker);
        final NumberPicker minutePicker = view.findViewById(R.id.abLoopMinutePicker);
        final NumberPicker secondPicker = view.findViewById(R.id.abLoopSecondPicker);
        final NumberPicker decimalPicker = view.findViewById(R.id.abLoopDecimalPicker);

        final String[] arInts = {"59", "58", "57", "56", "55", "54", "53", "52", "51", "50", "49", "48", "47", "46", "45", "44", "43", "42", "41", "40", "39", "38", "37", "36", "35", "34", "33", "32", "31", "30", "29", "28", "27", "26", "25", "24", "23", "22", "21", "20", "19", "18", "17", "16", "15", "14", "13", "12", "11", "10", "09", "08", "07", "06", "05", "04", "03", "02", "01", "00"};
        final String[] arDecimals = {"99", "98", "97", "96", "95", "94", "93", "92", "91", "90", "89", "88", "87", "86", "85", "84", "83", "82", "81", "80", "79", "78", "77", "76", "75", "74", "73", "72", "71", "70", "69", "68", "67", "66", "65", "64", "63", "62", "61", "60", "59", "58", "57", "56", "55", "54", "53", "52", "51", "50", "49", "48", "47", "46", "45", "44", "43", "42", "41", "40", "39", "38", "37", "36", "35", "34", "33", "32", "31", "30", "29", "28", "27", "26", "25", "24", "23", "22", "21", "20", "19", "18", "17", "16", "15", "14", "13", "12", "11", "10", "09", "08", "07", "06", "05", "04", "03", "02", "01", "00"};

        hourPicker.setDisplayedValues(arInts);
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(59);
        hourPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int nOldValue, int nNewValue) {
                int nHour = Integer.parseInt(arInts[nNewValue]);
                int nMinute = Integer.parseInt(arInts[minutePicker.getValue()]);
                int nSecond = Integer.parseInt(arInts[secondPicker.getValue()]);
                double dDec = Double.parseDouble(arDecimals[decimalPicker.getValue()]);
                double dPos = nHour * 3600 + nMinute * 60 + nSecond + dDec / 100.0;
                setCurPos(dPos);
            }
        });
        for(int i = 0; i < arInts.length; i++)
        {
            if(arInts[i].equals(strHour))
                hourPicker.setValue(i);
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
                double dDec = Double.parseDouble(arDecimals[decimalPicker.getValue()]);
                double dPos = nHour * 3600 + nMinute * 60 + nSecond + dDec / 100.0;
                setCurPos(dPos);
            }
        });
        for(int i = 0; i < arInts.length; i++)
        {
            if(arInts[i].equals(strMinute))
                minutePicker.setValue(i);
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
                double dDec = Double.parseDouble(arDecimals[decimalPicker.getValue()]);
                double dPos = nHour * 3600 + nMinute * 60 + nSecond + dDec / 100.0;
                setCurPos(dPos);
            }
        });
        for(int i = 0; i < arInts.length; i++)
        {
            if(arInts[i].equals(strSecond))
                secondPicker.setValue(i);
        }

        decimalPicker.setDisplayedValues(arDecimals);
        decimalPicker.setMinValue(0);
        decimalPicker.setMaxValue(99);
        decimalPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int nOldValue, int nNewValue) {
                int nHour = Integer.parseInt(arInts[hourPicker.getValue()]);
                int nMinute = Integer.parseInt(arInts[minutePicker.getValue()]);
                int nSecond = Integer.parseInt(arInts[secondPicker.getValue()]);
                double dDec = Double.parseDouble(arDecimals[nNewValue]);
                double dPos = nHour * 3600 + nMinute * 60 + nSecond + dDec / 100.0;
                setCurPos(dPos);
            }
        });
        for(int i = 0; i < arDecimals.length; i++)
        {
            if(arDecimals[i].equals(strDec))
                decimalPicker.setValue(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.adjustCurPos);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearFocus();
            }
        });
        builder.setView(view);
        builder.show();
    }

    private void clearFocus()
    {
        activity.findViewById(R.id.textAValue).clearFocus();
        activity.findViewById(R.id.textBValue).clearFocus();
        activity.findViewById(R.id.textCurValue).clearFocus();
    }

    private void setLoopA(double dLoopA)
    {
        setLoopA(dLoopA, true);
    }

    public void setLoopA(double dLoopA, boolean bSave)
    {
        if(MainActivity.hStream == 0) return;

        double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
        if(dLoopA >= dLength)
            dLoopA = dLength;

        if(activity.bLoopB && dLoopA >= activity.dLoopB)
            dLoopA = activity.dLoopB - 1.0;

        activity.dLoopA = dLoopA;
        activity.bLoopA = true;
        AnimationButton btnA = activity.findViewById(R.id.btnA);
        btnA.setSelected(true);
        btnA.setImageResource(R.drawable.ic_abloop_a_on);

        long nLength = BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE);
        long nPos = BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, dLoopA);
        int nScreenWidth = waveView.getWidth();
        int nMaxWidth = (int)(nScreenWidth * waveView.getZoom());
        int nLeft = (int) (viewCurPos.getX() - nMaxWidth * nPos / nLength);
        if(nLeft > 0) nLeft = 0;
        View viewMaskA = activity.findViewById(R.id.viewMaskA);
        viewMaskA.getLayoutParams().width = nLeft;
        viewMaskA.setVisibility(View.VISIBLE);
        viewMaskA.requestLayout();

        int nMinute = (int)(activity.dLoopA / 60);
        int nSecond = (int)(activity.dLoopA % 60);
        int nHour = nMinute / 60;
        nMinute = nMinute % 60;
        int nDec = (int)((activity.dLoopA * 100) % 100);
        EditText textAValue = activity.findViewById(R.id.textAValue);
        textAValue.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d.%02d", nHour, nMinute, nSecond, nDec));

        if(bSave) activity.playlistFragment.updateSavingEffect();
    }

    private void setLoopB(double dLoopB)
    {
        setLoopB(dLoopB, true);
    }

    public void setLoopB(double dLoopB, boolean bSave)
    {
        if(MainActivity.hStream == 0) return;

        double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
        if(dLoopB >= dLength)
            dLoopB = dLength;

        if(activity.bLoopA && dLoopB <= activity.dLoopA)
            dLoopB = activity.dLoopA + 1.0;

        activity.dLoopB = dLoopB;
        activity.bLoopB = true;
        AnimationButton btnB = activity.findViewById(R.id.btnB);
        btnB.setSelected(true);
        btnB.setImageResource(R.drawable.ic_abloop_b_on);
        long nLength = BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE);
        long nPos = BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, dLoopB);
        int nScreenWidth = waveView.getWidth();
        int nMaxWidth = (int)(nScreenWidth * waveView.getZoom());
        int nLeft = (int) (viewCurPos.getX() - nMaxWidth * nPos / nLength);
        if(nLeft < 0) nLeft = 0;
        else if(nLeft > waveView.getWidth()) nLeft = waveView.getWidth();
        View viewMaskB = activity.findViewById(R.id.viewMaskB);
        viewMaskB.setTranslationX(nLeft);
        viewMaskB.getLayoutParams().width = waveView.getWidth() - nLeft;
        viewMaskB.setVisibility(View.VISIBLE);
        viewMaskB.requestLayout();
        BASS.BASS_ChannelSetPosition(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopA), BASS.BASS_POS_BYTE);

        int nMinute = (int)(activity.dLoopB / 60);
        int nSecond = (int)(activity.dLoopB % 60);
        int nHour = nMinute / 60;
        nMinute = nMinute % 60;
        int nDec = (int)((activity.dLoopB * 100) % 100);
        EditText textBValue  = activity.findViewById(R.id.textBValue);
        textBValue.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d.%02d", nHour, nMinute, nSecond, nDec));

        activity.setSync();

        if(bSave) activity.playlistFragment.updateSavingEffect();
    }

    public void setCurPos(double dPos)
    {
        if(MainActivity.hStream == 0) return;

        double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
        boolean bReverse = activity.effectFragment.isReverse();
        if(bReverse) {
            if(dPos <= 0.0f) {
                if(BASS.BASS_ChannelIsActive(MainActivity.hStream) == BASS.BASS_ACTIVE_PLAYING) {
                    activity.onEnded(true);
                    return;
                }
                dPos = 0.0f;
            }
        }
        else {
            if(dLength <= dPos) {
                if(BASS.BASS_ChannelIsActive(MainActivity.hStream) == BASS.BASS_ACTIVE_PLAYING) {
                    activity.onEnded(true);
                    return;
                }
                dPos = dLength;
            }
        }
        BASS.BASS_ChannelSetPosition(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, dPos), BASS.BASS_POS_BYTE);
        activity.setSync();

        double dCurPos = dPos;
        int i = 0;
        for( ; i < arMarkerTime.size(); i++) {
            dPos = arMarkerTime.get(i);
            if((!bReverse && dCurPos < dPos) || (bReverse && dCurPos < dPos - 1.0))
                break;
        }
        nMarker = i - 1;

        activity.setSync();

        activity.playlistFragment.updateSavingEffect();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if(v.getId() == R.id.relativeZoomOut) {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(MainActivity.hStream == 0) return false;
                bContinue = false;
                setZoomOut();
                waveView = activity.findViewById(R.id.waveView);
                waveView.redrawWaveForm();
            }
        }
        else if(v.getId() == R.id.relativeZoomIn) {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(MainActivity.hStream == 0) return false;
                bContinue = false;
                setZoomIn();
                waveView = activity.findViewById(R.id.waveView);
                waveView.redrawWaveForm();
            }
        }
        else if(v.getId() == R.id.viewBtnRewindLeft || v.getId() == R.id.viewBtnRewindRight || v.getId() == R.id.btnRewind5Sec || v.getId() == R.id.btnRewind5Sec2)
        {
            if (event.getAction() == MotionEvent.ACTION_UP)
            {
                if (MainActivity.hStream != 0)
                {
                    AnimationButton btnRewind5Sec = activity.findViewById(R.id.btnRewind5Sec);
                    double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
                    double dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                    if((Integer)btnRewind5Sec.getTag() == 1) dPos -= 1.0;
                    else if((Integer)btnRewind5Sec.getTag() == 2) dPos -= 2.0;
                    else if((Integer)btnRewind5Sec.getTag() == 3) dPos -= 3.0;
                    else if((Integer)btnRewind5Sec.getTag() == 5) dPos -= 5.0;
                    else if((Integer)btnRewind5Sec.getTag() == 10) dPos -= 10.0;
                    boolean bReverse = activity.effectFragment.isReverse();
                    if(bReverse) {
                        if(dPos <= 0.0f) {
                            if(BASS.BASS_ChannelIsActive(MainActivity.hStream) == BASS.BASS_ACTIVE_PLAYING) {
                                activity.onEnded(true);
                                return true;
                            }
                            dPos = 0.0f;
                        }
                    }
                    else {
                        if(dLength <= dPos) {
                            if(BASS.BASS_ChannelIsActive(MainActivity.hStream) == BASS.BASS_ACTIVE_PLAYING) {
                                activity.onEnded(true);
                                return true;
                            }
                            dPos = dLength;
                        }
                    }
                    LinearLayout ABButton = activity.findViewById(R.id.ABButton);
                    if(ABButton.getVisibility() == View.VISIBLE && ((activity.bLoopA && dPos < activity.dLoopA) || (activity.bLoopB && activity.dLoopB < dPos)))
                        dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopA));
                    BASS.BASS_ChannelSetPosition(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, dPos), BASS.BASS_POS_BYTE);
                    activity.setSync();
                }
            }
        }
        else if(v.getId() == R.id.viewBtnForwardLeft || v.getId() == R.id.viewBtnForwardRight || v.getId() == R.id.btnForward5Sec || v.getId() == R.id.btnForward5Sec2)
        {
            if (event.getAction() == MotionEvent.ACTION_UP)
            {
                if (MainActivity.hStream != 0)
                {
                    AnimationButton btnForward5Sec = activity.findViewById(R.id.btnForward5Sec);
                    double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
                    double dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                    if((Integer)btnForward5Sec.getTag() == 1) dPos += 1.0;
                    else if((Integer)btnForward5Sec.getTag() == 2) dPos += 2.0;
                    else if((Integer)btnForward5Sec.getTag() == 3) dPos += 3.0;
                    else if((Integer)btnForward5Sec.getTag() == 5) dPos += 5.0;
                    else if((Integer)btnForward5Sec.getTag() == 10) dPos += 10.0;
                    boolean bReverse = activity.effectFragment.isReverse();
                    if(bReverse) {
                        if(dPos <= 0.0f) {
                            if(BASS.BASS_ChannelIsActive(MainActivity.hStream) == BASS.BASS_ACTIVE_PLAYING) {
                                activity.onEnded(true);
                                return true;
                            }
                            dPos = 0.0f;
                        }
                    }
                    else {
                        if(dLength <= dPos) {
                            if(BASS.BASS_ChannelIsActive(MainActivity.hStream) == BASS.BASS_ACTIVE_PLAYING) {
                                activity.onEnded(true);
                                return true;
                            }
                            dPos = dLength;
                        }
                    }
                    LinearLayout ABButton = activity.findViewById(R.id.ABButton);
                    if(ABButton.getVisibility() == View.VISIBLE && ((activity.bLoopA && dPos < activity.dLoopA) || (activity.bLoopB && activity.dLoopB < dPos)))
                        dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopA));
                    BASS.BASS_ChannelSetPosition(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, dPos), BASS.BASS_POS_BYTE);
                    activity.setSync();
                }
            }
        }
        else if(v.getId() == R.id.viewBtnALeft || v.getId() == R.id.viewBtnARight || v.getId() == R.id.btnA)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                if(MainActivity.hStream != 0)
                {
                    AnimationButton btnA = activity.findViewById(R.id.btnA);
                    if(btnA.isSelected()) {
                        activity.dLoopA = 0.0;
                        activity.bLoopA = false;
                        btnA.setSelected(false);
                        btnA.setImageResource(R.drawable.ic_abloop_a);
                        View viewMaskA = activity.findViewById(R.id.viewMaskA);
                        viewMaskA.setVisibility(View.INVISIBLE);
                        EditText textAValue  = activity.findViewById(R.id.textAValue);
                        textAValue.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d.%02d", 0, 0, 0, 0));
                    }
                    else {
                        activity.dLoopA = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                        activity.bLoopA = true;
                        btnA.setSelected(true);
                        btnA.setImageResource(R.drawable.ic_abloop_a_on);
                        long nLength = BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE);
                        long nPos = BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE);
                        int nBkWidth = waveView.getWidth();
                        int nLeft = (int) (nBkWidth * nPos / nLength);
                        View viewMaskA = activity.findViewById(R.id.viewMaskA);
                        viewMaskA.getLayoutParams().width = nLeft;
                        viewMaskA.setVisibility(View.VISIBLE);
                        viewMaskA.requestLayout();
                        if(activity.effectFragment.isReverse())
                            BASS.BASS_ChannelSetPosition(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopB), BASS.BASS_POS_BYTE);

                        int nMinute = (int)(activity.dLoopA / 60);
                        int nSecond = (int)(activity.dLoopA % 60);
                        int nHour = nMinute / 60;
                        nMinute = nMinute % 60;
                        int nDec = (int)((activity.dLoopA * 100) % 100);
                        EditText textAValue = activity.findViewById(R.id.textAValue);
                        textAValue.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d.%02d", nHour, nMinute, nSecond, nDec));
                    }
                    activity.playlistFragment.updateSavingEffect();
                }
            }
        }
        else if(v.getId() == R.id.viewBtnBLeft || v.getId() == R.id.viewBtnBRight || v.getId() == R.id.btnB)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                if(MainActivity.hStream != 0)
                {
                    AnimationButton btnB = activity.findViewById(R.id.btnB);
                    if(btnB.isSelected()) {
                        activity.dLoopB = 0.0;
                        activity.bLoopB = false;
                        btnB.setSelected(false);
                        btnB.setImageResource(R.drawable.ic_abloop_b);
                        View viewMaskB = activity.findViewById(R.id.viewMaskB);
                        viewMaskB.setVisibility(View.INVISIBLE);

                        double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
                        int nMinute = (int)(dLength / 60);
                        int nSecond = (int)(dLength % 60);
                        int nHour = nMinute / 60;
                        nMinute = nMinute % 60;
                        int nDec = (int)((dLength * 100) % 100);
                        EditText textBValue  = activity.findViewById(R.id.textBValue);
                        textBValue.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d.%02d", nHour, nMinute, nSecond, nDec));
                    }
                    else {
                        activity.dLoopB = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                        activity.bLoopB = true;
                        btnB.setSelected(true);
                        btnB.setImageResource(R.drawable.ic_abloop_b_on);
                        long nLength = BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE);
                        long nPos = BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE);
                        int nBkWidth = waveView.getWidth();
                        int nLeft = (int) (nBkWidth * nPos / nLength);
                        View viewMaskB = activity.findViewById(R.id.viewMaskB);
                        viewMaskB.setTranslationX(nLeft);
                        viewMaskB.getLayoutParams().width = nBkWidth - nLeft;
                        viewMaskB.setVisibility(View.VISIBLE);
                        viewMaskB.requestLayout();
                        if(!activity.effectFragment.isReverse())
                            BASS.BASS_ChannelSetPosition(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopA), BASS.BASS_POS_BYTE);

                        int nMinute = (int)(activity.dLoopB / 60);
                        int nSecond = (int)(activity.dLoopB % 60);
                        int nHour = nMinute / 60;
                        nMinute = nMinute % 60;
                        int nDec = (int)((activity.dLoopB * 100) % 100);
                        EditText textBValue  = activity.findViewById(R.id.textBValue);
                        textBValue.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d.%02d", nHour, nMinute, nSecond, nDec));
                    }
                    activity.setSync();

                    activity.playlistFragment.updateSavingEffect();
                }
            }
        }
        else if(v.getId() == R.id.btnPrevmarker)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                if(MainActivity.hStream != 0)
                {
                    boolean bReverse = activity.effectFragment.isReverse();
                    double dCurPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                    int i = arMarkerTime.size() - 1;
                    for( ; i >= 0; i--)
                    {
                        double dPos = arMarkerTime.get(i);
                        if((!bReverse && dCurPos >= dPos + 1.0) || (bReverse && dCurPos >= dPos))
                        {
                            BASS.BASS_ChannelSetPosition(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, dPos), BASS.BASS_POS_BYTE);
                            break;
                        }
                    }
                    nMarker = i;
                    activity.setSync();

                    activity.playlistFragment.updateSavingEffect();
                }
            }
        }
        else if(v.getId() == R.id.btnNextmarker)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                if(MainActivity.hStream != 0)
                {
                    boolean bReverse = activity.effectFragment.isReverse();
                    double dCurPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                    int i = 0;
                    for( ; i < arMarkerTime.size(); i++)
                    {
                        double dPos = arMarkerTime.get(i);
                        if((!bReverse && dCurPos < dPos) || (bReverse && dCurPos < dPos - 1.0))
                        {
                            BASS.BASS_ChannelSetPosition(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, dPos), BASS.BASS_POS_BYTE);
                            break;
                        }
                    }
                    nMarker = i;
                    activity.setSync();

                    activity.playlistFragment.updateSavingEffect();
                }
            }
        }
        else if(v.getId() == R.id.btnAddmarker)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                if(MainActivity.hStream != 0)
                {
                    int nScreenWidth = waveView.getWidth();
                    int nMaxWidth = (int)(nScreenWidth * waveView.getZoom());
                    double dCurPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                    double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
                    TextView textView = new TextView(activity);
                    textView.setText("▼");
                    RelativeLayout layout = activity.findViewById(R.id.relative_loop);
                    layout.addView(textView);
                    textView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    int nLeft = (int) (viewCurPos.getX() - nMaxWidth * dCurPos / dLength - textView.getMeasuredWidth() / 2.0f);
                    RelativeLayout relativeWave = activity.findViewById(R.id.relativeWave);
                    int nTop = relativeWave.getTop() - textView.getMeasuredHeight();
                    textView.setTranslationX(nLeft);
                    textView.setTranslationY(nTop);
                    textView.requestLayout();
                    boolean bAdded = false;
                    int i = 0;
                    for( ; i < arMarkerTime.size(); i++)
                    {
                        double dPos = arMarkerTime.get(i);
                        if(dCurPos < dPos)
                        {
                            bAdded = true;
                            arMarkerTime.add(i, dCurPos);
                            arMarkerText.add(i, textView);
                            break;
                        }
                    }
                    if(!bAdded)
                    {
                        arMarkerTime.add(dCurPos);
                        arMarkerText.add(textView);
                    }
                    nMarker = i;

                    activity.playlistFragment.updateSavingEffect();
                }
            }
        }
        else if (v.getId() == R.id.btnDelmarker)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                if(MainActivity.hStream != 0)
                {
                    double dCurPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                    for(int i = arMarkerTime.size() - 1; i >= 0; i--)
                    {
                        double dPos = arMarkerTime.get(i);
                        if(dCurPos >= dPos)
                        {
                            arMarkerTime.remove(i);
                            TextView textView = arMarkerText.get(i);
                            RelativeLayout layout = activity.findViewById(R.id.relative_loop);
                            layout.removeView(textView);
                            arMarkerText.remove(i);
                            break;
                        }
                    }
                    activity.playlistFragment.updateSavingEffect();
                }
            }
        }
        else if (v.getId() == R.id.btnLoopmarker)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                AnimationButton btnLoopmarker = activity.findViewById(R.id.btnLoopmarker);
                if(btnLoopmarker.isSelected())
                {
                    btnLoopmarker.setSelected(false);
                    btnLoopmarker.setImageResource(R.drawable.ic_abloop_marker_loop);
                }
                else
                {
                    btnLoopmarker.setSelected(true);
                    btnLoopmarker.setImageResource(R.drawable.ic_abloop_marker_loop_on);
                }

                if(MainActivity.hStream != 0)
                {
                    double dCurPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                    int i = 0;
                    for( ; i < arMarkerTime.size(); i++) {
                        double dPos = arMarkerTime.get(i);
                        if(dCurPos < dPos)
                        {
                            break;
                        }
                    }
                    nMarker = i - 1;

                    activity.setSync();

                    activity.playlistFragment.updateSavingEffect();
                }
            }
        }
        else if(v.getId() == R.id.waveView)
        {
            float fX = event.getX();
            viewCurPos = activity.findViewById(R.id.viewCurPos);
            int nBkWidth = waveView.getWidth();
            double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
            double dPos = dLength * fX / nBkWidth;
            LinearLayout ABButton = activity.findViewById(R.id.ABButton);
            if(ABButton.getVisibility() == View.VISIBLE && ((activity.bLoopA && dPos < activity.dLoopA) || (activity.bLoopB && activity.dLoopB < dPos)))
                dPos = activity.dLoopA;
            setCurPos(dPos);
            return true;
        }

        return false;
    }

    public void clearLoop(boolean bSave)
    {
        if(activity == null) return;

        if(activity.findViewById(R.id.btnA) != null)
        {
            AnimationButton btnA = activity.findViewById(R.id.btnA);
            btnA.setSelected(false);
            btnA.setImageResource(R.drawable.ic_abloop_a);
        }

        if(activity.findViewById(R.id.viewMaskA) != null)
        {
            View viewMaskA = activity.findViewById(R.id.viewMaskA);
            viewMaskA.setVisibility(View.INVISIBLE);
        }

        if(activity.findViewById(R.id.btnB) != null)
        {
            AnimationButton btnB = activity.findViewById(R.id.btnB);
            btnB.setSelected(false);
            btnB.setImageResource(R.drawable.ic_abloop_b);
        }

        if(activity.findViewById(R.id.viewMaskB) != null)
        {
            View viewMaskB = activity.findViewById(R.id.viewMaskB);
            viewMaskB.setVisibility(View.INVISIBLE);
        }

        for(int i = 0; i < arMarkerText.size(); i++)
        {
            TextView textView = arMarkerText.get(i);
            RelativeLayout layout = activity.findViewById(R.id.relative_loop);
            layout.removeView(textView);
        }

        EditText textAValue  = activity.findViewById(R.id.textAValue);
        textAValue.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d.%02d", 0, 0, 0, 0));

        EditText textBValue  = activity.findViewById(R.id.textBValue);
        textBValue.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d.%02d", 0, 0, 0, 0));

        arMarkerTime.clear();
        arMarkerText.clear();

        waveView.clearWaveForm(true);

        if(bSave) activity.playlistFragment.updateSavingEffect();
    }

    public void drawWaveForm(String strPath)
    {
        EditText textBValue  = activity.findViewById(R.id.textBValue);
        double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
        int nMinute = (int)(dLength / 60);
        int nSecond = (int)(dLength % 60);
        int nHour = nMinute / 60;
        nMinute = nMinute % 60;
        int nDec = (int)((dLength * 100) % 100);
        textBValue.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d.%02d", nHour, nMinute, nSecond, nDec));
        waveView.drawWaveForm(strPath);
    }

    public double getMarkerSrcPos()
    {
        double dPos = 0.0;
        LinearLayout MarkerButton = activity.findViewById(R.id.MarkerButton);
        AnimationButton btnLoopmarker = activity.findViewById(R.id.btnLoopmarker);

        if(activity.effectFragment.isReverse()) {
            if(MarkerButton.getVisibility() == View.VISIBLE && btnLoopmarker.isSelected()) // マーカー再生中
            {
                dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
                if(nMarker >= 0 && nMarker < arMarkerTime.size()) {
                    dPos = arMarkerTime.get(nMarker);
                }
            }
        }
        else {
            if(MarkerButton.getVisibility() == View.VISIBLE && btnLoopmarker.isSelected()) // マーカー再生中
            {
                if(nMarker >= 0 && nMarker < arMarkerTime.size()) {
                    dPos = arMarkerTime.get(nMarker);
                }
            }
        }
        return dPos;
    }

    public double getMarkerDstPos()
    {
        double dPos = 0.0;
        LinearLayout MarkerButton = activity.findViewById(R.id.MarkerButton);
        AnimationButton btnLoopmarker = activity.findViewById(R.id.btnLoopmarker);

        if(activity.effectFragment.isReverse()) {
            if(MarkerButton.getVisibility() == View.VISIBLE && btnLoopmarker.isSelected()) // マーカー再生中
            {
                if(nMarker - 1 >= 0 && nMarker - 1 < arMarkerTime.size()) {
                    dPos = arMarkerTime.get(nMarker - 1);
                }
            }
        }
        else {
            if(MarkerButton.getVisibility() == View.VISIBLE && btnLoopmarker.isSelected()) // マーカー再生中
            {
                dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
                if(nMarker + 1 >= 0 && nMarker + 1 < arMarkerTime.size()) {
                    dPos = arMarkerTime.get(nMarker + 1);
                }
            }
        }
        return dPos;
    }
}
