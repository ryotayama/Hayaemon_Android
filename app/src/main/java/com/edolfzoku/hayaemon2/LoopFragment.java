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

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.un4seen.bass.BASS;

import java.util.ArrayList;

public class LoopFragment extends Fragment implements View.OnTouchListener, View.OnFocusChangeListener, View.OnLongClickListener {
    private int nMarker; // マーカー再生時のループ位置
    private Handler handler;
    private MainActivity activity;

    private View viewCurPos;
    private WaveView waveView;

    private ArrayList<Double> arMarkerTime;
    private ArrayList<TextView> arMarkerText;
    private boolean bContinue = false;

    public ArrayList<Double>  getArMarkerTime() { return arMarkerTime; }
    public void setArMarkerTime(ArrayList<Double> arMarkerTime) {
        this.arMarkerTime = new ArrayList<>();
        int nScreenWidth = waveView.getWidth();
        int nMaxWidth = (int)(nScreenWidth * waveView.getZoom());
        double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
        for(int i = 0; i < arMarkerTime.size(); i++) {
            double dPos = arMarkerTime.get(i).doubleValue();
            this.arMarkerTime.add(dPos);

            TextView textView = new TextView(getActivity());
            textView.setText("▼");
            RelativeLayout layout = (RelativeLayout)getActivity().findViewById(R.id.relative_loop);
            layout.addView(textView);
            textView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            int nLeft = (int) (viewCurPos.getX() - nMaxWidth * dPos / dLength - textView.getMeasuredWidth() / 2);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_loop, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewCurPos = getActivity().findViewById(R.id.viewCurPos);
        waveView = (WaveView)getActivity().findViewById(R.id.waveView);
        waveView.setLoopFragment(this);
        waveView.setOnTouchListener(this);
        final long lDelay = 250;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (activity != null) {
                    LinearLayout ABButton = (LinearLayout)activity.findViewById(R.id.ABButton);
                    EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
                    long nLength = 0;
                    long nPos = 0;
                    int nScreenWidth = waveView.getWidth();
                    int nMaxWidth = (int)(nScreenWidth * waveView.getZoom());
                    int nLeft = -1;
                    EditText textCurValue = activity.findViewById(R.id.textCurValue);
                    if(MainActivity.hStream != 0) {
                        double dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                        if (ABButton.getVisibility() == View.VISIBLE && ((activity.bLoopA && dPos < activity.dLoopA) || (activity.bLoopB && activity.dLoopB < dPos)) && !activity.isPlayNextByBPos()) {
                            if (effectFragment.isReverse())
                                dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopB));
                            else
                                dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopA));
                            BASS.BASS_ChannelSetPosition(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, dPos), BASS.BASS_POS_BYTE);
                        }
                        if (dPos < 0) dPos = 0;
                        int nMinute = (int) (dPos / 60);
                        int nSecond = (int) (dPos % 60);
                        int nHour = (int) (nMinute / 60);
                        nMinute = nMinute % 60;
                        int nDec = (int) ((dPos * 100) % 100);
                        textCurValue.setText(String.format("%02d:%02d:%02d.%02d", nHour, nMinute, nSecond, nDec));
                        nLength = BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE);
                        nPos = BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE);
                        nLeft = (int) (nMaxWidth * nPos / nLength);
                    }
                    else
                        textCurValue.setText("00:00:00.00");
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
                                .x(nScreenWidth / 2)
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
                    View viewMaskA = getActivity().findViewById(R.id.viewMaskA);
                    View viewMaskB = getActivity().findViewById(R.id.viewMaskB);
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
                    final LinearLayout MarkerButton = (LinearLayout)getActivity().findViewById(R.id.MarkerButton);
                    if(MarkerButton.getVisibility() == View.VISIBLE) {
                        for (int i = 0; i < arMarkerTime.size(); i++) {
                            TextView textView = arMarkerText.get(i);
                            double dMarkerPos = arMarkerTime.get(i).doubleValue();
                            long nMarkerPos = BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, dMarkerPos);
                            nMarkerPos = nPos - nMarkerPos;
                            int nMarkerLeft = (int) (viewCurPos.getX() - nMaxWidth * nMarkerPos / nLength - textView.getMeasuredWidth() / 2);
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
        RelativeLayout relativeZoomOut = (RelativeLayout)getActivity().findViewById(R.id.relativeZoomOut);
        relativeZoomOut.setOnTouchListener(this);
        relativeZoomOut.setOnLongClickListener(this);
        RelativeLayout relativeZoomIn = (RelativeLayout)getActivity().findViewById(R.id.relativeZoomIn);
        relativeZoomIn.setOnTouchListener(this);
        relativeZoomIn.setOnLongClickListener(this);
        getActivity().findViewById(R.id.viewBtnALeft).setOnTouchListener(this);
        ImageButton btnA = (ImageButton)getActivity().findViewById(R.id.btnA);
        btnA.setSelected(false);
        btnA.setImageResource(R.drawable.ic_abloop_a);
        btnA.setOnTouchListener(this);
        getActivity().findViewById(R.id.viewBtnARight).setOnTouchListener(this);
        getActivity().findViewById(R.id.viewBtnRiwindLeft).setOnTouchListener(this);
        ImageButton btnRewind5Sec = (ImageButton)getActivity().findViewById(R.id.btnRewind5Sec);
        btnRewind5Sec.setOnTouchListener(this);
        getActivity().findViewById(R.id.viewBtnRiwindRight).setOnTouchListener(this);
        getActivity().findViewById(R.id.viewBtnForwardLeft).setOnTouchListener(this);
        ImageButton btnForward5Sec = (ImageButton)getActivity().findViewById(R.id.btnForward5Sec);
        btnForward5Sec.setOnTouchListener(this);
        getActivity().findViewById(R.id.viewBtnForwardRight).setOnTouchListener(this);
        getActivity().findViewById(R.id.viewBtnBLeft).setOnTouchListener(this);
        ImageButton btnB = (ImageButton)getActivity().findViewById(R.id.btnB);
        btnB.setSelected(false);
        btnB.setImageResource(R.drawable.ic_abloop_b);
        btnB.setOnTouchListener(this);
        getActivity().findViewById(R.id.viewBtnBRight).setOnTouchListener(this);

        ImageButton btnRewind5sec2 = (ImageButton)getActivity().findViewById(R.id.btnRewind5Sec2);
        btnRewind5sec2.setOnTouchListener(this);
        ImageButton btnPrevmarker = (ImageButton)getActivity().findViewById(R.id.btnPrevmarker);
        btnPrevmarker.setOnTouchListener(this);
        ImageButton btnDelmarker= (ImageButton)getActivity().findViewById(R.id.btnDelmarker);
        btnDelmarker.setOnTouchListener(this);
        ImageButton btnAddmarker = (ImageButton)getActivity().findViewById(R.id.btnAddmarker);
        btnAddmarker.setOnTouchListener(this);
        ImageButton btnNextmarker = (ImageButton)getActivity().findViewById(R.id.btnNextmarker);
        btnNextmarker.setOnTouchListener(this);
        ImageButton btnLoopmarker = (ImageButton)getActivity().findViewById(R.id.btnLoopmarker);
        btnLoopmarker.setSelected(false);
        btnLoopmarker.setImageResource(R.drawable.ic_abloop_marker_loop);
        btnLoopmarker.setOnTouchListener(this);
        ImageButton btnForward5Sec2 = (ImageButton)getActivity().findViewById(R.id.btnForward5Sec2);
        btnForward5Sec2.setOnTouchListener(this);

        final EditText textCurValue = getActivity().findViewById(R.id.textCurValue);
        final LinearLayout ABLabel = (LinearLayout)getActivity().findViewById(R.id.ABLabel);
        final LinearLayout ABButton = (LinearLayout)getActivity().findViewById(R.id.ABButton);
        final LinearLayout MarkerButton = (LinearLayout)getActivity().findViewById(R.id.MarkerButton);
        final View viewMaskA = getActivity().findViewById(R.id.viewMaskA);
        final View viewMaskB = getActivity().findViewById(R.id.viewMaskB);
        final MainActivity activity = (MainActivity)getActivity();

        final RadioGroup radioGroupLoopMode = getActivity().findViewById(R.id.radioGroupLoopMode);
        radioGroupLoopMode.check(R.id.radioButtonABLoop);
        radioGroupLoopMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int nItem) {
                if(nItem == R.id.radioButtonABLoop) {
                    textCurValue.setVisibility(View.VISIBLE);
                    ABLabel.setVisibility(View.VISIBLE);
                    ABButton.setVisibility(View.VISIBLE);
                    if(activity.bLoopA) viewMaskA.setVisibility(View.VISIBLE);
                    if(activity.bLoopB) viewMaskB.setVisibility(View.VISIBLE);
                    activity.setSync();
                    MainActivity activity = (MainActivity)getActivity();
                    PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
                    playlistFragment.updateSavingEffect();

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
                    MainActivity activity = (MainActivity)getActivity();
                    PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
                    playlistFragment.updateSavingEffect();

                    textCurValue.setVisibility(View.INVISIBLE);
                    ABLabel.setVisibility(View.INVISIBLE);
                    ABButton.setVisibility(View.INVISIBLE);
                    viewMaskA.setVisibility(View.INVISIBLE);
                    viewMaskB.setVisibility(View.INVISIBLE);
                }
            }
        });

        getActivity().findViewById(R.id.textAValue).setOnFocusChangeListener(this);
        getActivity().findViewById(R.id.textBValue).setOnFocusChangeListener(this);
        getActivity().findViewById(R.id.textCurValue).setOnFocusChangeListener(this);
    }

    public void setZoomOut() {
        waveView = (WaveView)getActivity().findViewById(R.id.waveView);
        waveView.setZoom(waveView.getZoom() * 0.99f);
    }

    public void setZoomIn() {
        waveView = (WaveView)getActivity().findViewById(R.id.waveView);
        waveView.setZoom(waveView.getZoom() * 1.01f);
    }

    Runnable repeatZoomOut = new Runnable()
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

    Runnable repeatZoomIn = new Runnable()
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

    public void showABLoopPicker(boolean bAPos) {
        MainActivity activity = (MainActivity)getActivity();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.ablooppicker, null, false);
        double dValue = 0.0;
        if(bAPos) dValue = activity.dLoopA;
        else dValue = activity.dLoopB;
        int nMinute = (int)(dValue / 60);
        int nSecond = (int)(dValue % 60);
        int nHour = (int)(nMinute / 60);
        nMinute = nMinute % 60;
        int nDec = (int)((dValue * 100) % 100);
        String strHour = String.format("%02d", nHour);
        String strMinute = String.format("%02d", nMinute);
        String strSecond = String.format("%02d", nSecond);
        String strDec = String.format("%02d", nDec);

        final NumberPicker hourPicker = (NumberPicker)view.findViewById(R.id.abLoopHourPicker);
        final String[] arInts = {"59", "58", "57", "56", "55", "54", "53", "52", "51", "50", "49", "48", "47", "46", "45", "44", "43", "42", "41", "40", "39", "38", "37", "36", "35", "34", "33", "32", "31", "30", "29", "28", "27", "26", "25", "24", "23", "22", "21", "20", "19", "18", "17", "16", "15", "14", "13", "12", "11", "10", "09", "08", "07", "06", "05", "04", "03", "02", "01", "00"};
        hourPicker.setDisplayedValues(arInts);
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(59);
        for(int i = 0; i < arInts.length; i++)
        {
            if(arInts[i].equals(strHour))
                hourPicker.setValue(i);
        }

        final NumberPicker minutePicker = (NumberPicker)view.findViewById(R.id.abLoopMinutePicker);
        minutePicker.setDisplayedValues(arInts);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        for(int i = 0; i < arInts.length; i++)
        {
            if(arInts[i].equals(strMinute))
                minutePicker.setValue(i);
        }
        final NumberPicker secondPicker = (NumberPicker)view.findViewById(R.id.abLoopSecondPicker);
        secondPicker.setDisplayedValues(arInts);
        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(59);
        for(int i = 0; i < arInts.length; i++)
        {
            if(arInts[i].equals(strSecond))
                secondPicker.setValue(i);
        }

        final NumberPicker decimalPicker = (NumberPicker)view.findViewById(R.id.abLoopDecimalPicker);
        final String[] arDecimals = {"99", "98", "97", "96", "95", "94", "93", "92", "91", "90", "89", "88", "87", "86", "85", "84", "83", "82", "81", "80", "79", "78", "77", "76", "75", "74", "73", "72", "71", "70", "69", "68", "67", "66", "65", "64", "63", "62", "61", "60", "59", "58", "57", "56", "55", "54", "53", "52", "51", "50", "49", "48", "47", "46", "45", "44", "43", "42", "41", "40", "39", "38", "37", "36", "35", "34", "33", "32", "31", "30", "29", "28", "27", "26", "25", "24", "23", "22", "21", "20", "19", "18", "17", "16", "15", "14", "13", "12", "11", "10", "09", "08", "07", "06", "05", "04", "03", "02", "01", "00"};
        decimalPicker.setDisplayedValues(arDecimals);
        decimalPicker.setMinValue(0);
        decimalPicker.setMaxValue(99);
        for(int i = 0; i < arDecimals.length; i++)
        {
            if(arDecimals[i].equals(strDec))
                decimalPicker.setValue(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if(bAPos) builder.setTitle("A位置の調整");
        else builder.setTitle("B位置の調整");
        final boolean f_bAPos = bAPos;
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.ablooppicker, null, false);
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
        builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearFocus();
            }
        });
        builder.setView(view);
        builder.show();
    }

    public void showCurPicker() {
        MainActivity activity = (MainActivity)getActivity();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.ablooppicker, null, false);
        EditText textCurValue = activity.findViewById(R.id.textCurValue);
        String strCurPos = textCurValue.getText().toString();
        String strHour = strCurPos.substring(0, 2);
        String strMinute = strCurPos.substring(3, 5);
        String strSecond = strCurPos.substring(6, 8);
        String strDec = strCurPos.substring(9, 11);

        final NumberPicker hourPicker = (NumberPicker)view.findViewById(R.id.abLoopHourPicker);
        final NumberPicker minutePicker = (NumberPicker)view.findViewById(R.id.abLoopMinutePicker);
        final NumberPicker secondPicker = (NumberPicker)view.findViewById(R.id.abLoopSecondPicker);
        final NumberPicker decimalPicker = (NumberPicker)view.findViewById(R.id.abLoopDecimalPicker);

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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("再生位置の調整");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearFocus();
            }
        });
        builder.setView(view);
        builder.show();
    }

    public void clearFocus()
    {
        getActivity().findViewById(R.id.textAValue).clearFocus();
        getActivity().findViewById(R.id.textBValue).clearFocus();
        getActivity().findViewById(R.id.textCurValue).clearFocus();
    }

    public void setLoopA(double dLoopA)
    {
        setLoopA(dLoopA, true);
    }

    public void setLoopA(double dLoopA, boolean bSave)
    {
        if(MainActivity.hStream == 0) return;

        MainActivity activity = (MainActivity)getActivity();
        if(activity == null) return;

        double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
        if(dLoopA >= dLength)
            dLoopA = dLength;

        if(activity.bLoopB && dLoopA >= activity.dLoopB)
            dLoopA = activity.dLoopB - 1.0;

        activity.dLoopA = dLoopA;
        activity.bLoopA = true;
        ImageButton btnA = (ImageButton)getActivity().findViewById(R.id.btnA);
        btnA.setSelected(true);
        btnA.setImageResource(R.drawable.ic_abloop_a_on);

        long nLength = BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE);
        long nPos = BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, dLoopA);
        int nScreenWidth = waveView.getWidth();
        int nMaxWidth = (int)(nScreenWidth * waveView.getZoom());
        int nLeft = (int) (viewCurPos.getX() - nMaxWidth * nPos / nLength);
        if(nLeft > 0) nLeft = 0;
        View viewMaskA = getActivity().findViewById(R.id.viewMaskA);
        viewMaskA.getLayoutParams().width = nLeft;
        viewMaskA.setVisibility(View.VISIBLE);
        viewMaskA.requestLayout();

        int nMinute = (int)(activity.dLoopA / 60);
        int nSecond = (int)(activity.dLoopA % 60);
        int nHour = (int)(nMinute / 60);
        nMinute = nMinute % 60;
        int nDec = (int)((activity.dLoopA * 100) % 100);
        EditText textAValue = (EditText)getActivity().findViewById(R.id.textAValue);
        textAValue.setText(String.format("%02d:%02d:%02d.%02d", nHour, nMinute, nSecond, nDec));

        if(bSave) {
            PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
            playlistFragment.updateSavingEffect();
        }
    }

    public void setLoopB(double dLoopB)
    {
        setLoopB(dLoopB, true);
    }

    public void setLoopB(double dLoopB, boolean bSave)
    {
        if(MainActivity.hStream == 0) return;

        MainActivity activity = (MainActivity)getActivity();
        if(activity == null) return;

        double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
        if(dLoopB >= dLength)
            dLoopB = dLength;

        if(activity.bLoopA && dLoopB <= activity.dLoopA)
            dLoopB = activity.dLoopA + 1.0;

        activity.dLoopB = dLoopB;
        activity.bLoopB = true;
        ImageButton btnB = (ImageButton)getActivity().findViewById(R.id.btnB);
        btnB.setSelected(true);
        btnB.setImageResource(R.drawable.ic_abloop_b_on);
        long nLength = BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE);
        long nPos = BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, dLoopB);
        int nScreenWidth = waveView.getWidth();
        int nMaxWidth = (int)(nScreenWidth * waveView.getZoom());
        int nLeft = (int) (viewCurPos.getX() - nMaxWidth * nPos / nLength);
        if(nLeft < 0) nLeft = 0;
        else if(nLeft > waveView.getWidth()) nLeft = waveView.getWidth();
        View viewMaskB = getActivity().findViewById(R.id.viewMaskB);
        viewMaskB.setTranslationX(nLeft);
        viewMaskB.getLayoutParams().width = waveView.getWidth() - nLeft;
        viewMaskB.setVisibility(View.VISIBLE);
        viewMaskB.requestLayout();
        BASS.BASS_ChannelSetPosition(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopA), BASS.BASS_POS_BYTE);

        int nMinute = (int)(activity.dLoopB / 60);
        int nSecond = (int)(activity.dLoopB % 60);
        int nHour = (int)(nMinute / 60);
        nMinute = nMinute % 60;
        int nDec = (int)((activity.dLoopB * 100) % 100);
        EditText textBValue  = (EditText)getActivity().findViewById(R.id.textBValue);
        textBValue.setText(String.format("%02d:%02d:%02d.%02d", nHour, nMinute, nSecond, nDec));

        activity.setSync();

        if(bSave) {
            PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
            playlistFragment.updateSavingEffect();
        }
    }

    public void setCurPos(double dPos)
    {
        if(MainActivity.hStream == 0) return;

        double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
        EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
        boolean bReverse = effectFragment.isReverse();
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

        PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
        playlistFragment.updateSavingEffect();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        MainActivity activity = (MainActivity)getActivity();
        if(v.getId() == R.id.relativeZoomOut) {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(MainActivity.hStream == 0) return false;
                bContinue = false;
                setZoomOut();
                waveView = (WaveView)getActivity().findViewById(R.id.waveView);
                waveView.redrawWaveForm();
            }
        }
        else if(v.getId() == R.id.relativeZoomIn) {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(MainActivity.hStream == 0) return false;
                bContinue = false;
                setZoomIn();
                waveView = (WaveView)getActivity().findViewById(R.id.waveView);
                waveView.redrawWaveForm();
            }
        }
        else if(v.getId() == R.id.viewBtnRiwindLeft || v.getId() == R.id.viewBtnRiwindRight || v.getId() == R.id.btnRewind5Sec || v.getId() == R.id.btnRewind5Sec2)
        {
            if (event.getAction() == MotionEvent.ACTION_UP)
            {
                if (MainActivity.hStream != 0)
                {
                    double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
                    double dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                    dPos -= 5.0;
                    EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
                    boolean bReverse = effectFragment.isReverse();
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
                    LinearLayout ABButton = (LinearLayout)getActivity().findViewById(R.id.ABButton);
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
                    double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
                    double dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                    dPos += 5.0;
                    EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
                    boolean bReverse = effectFragment.isReverse();
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
                    LinearLayout ABButton = (LinearLayout)getActivity().findViewById(R.id.ABButton);
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
                    ImageButton btnA = (ImageButton)getActivity().findViewById(R.id.btnA);
                    if(btnA.isSelected()) {
                        activity.dLoopA = 0.0;
                        activity.bLoopA = false;
                        btnA.setSelected(false);
                        btnA.setImageResource(R.drawable.ic_abloop_a);
                        View viewMaskA = getActivity().findViewById(R.id.viewMaskA);
                        viewMaskA.setVisibility(View.INVISIBLE);
                        EditText textAValue  = (EditText)getActivity().findViewById(R.id.textAValue);
                        textAValue.setText("00:00:00.00");
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
                        View viewMaskA = getActivity().findViewById(R.id.viewMaskA);
                        viewMaskA.getLayoutParams().width = nLeft;
                        viewMaskA.setVisibility(View.VISIBLE);
                        viewMaskA.requestLayout();
                        EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
                        if(effectFragment.isReverse())
                            BASS.BASS_ChannelSetPosition(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopB), BASS.BASS_POS_BYTE);

                        int nMinute = (int)(activity.dLoopA / 60);
                        int nSecond = (int)(activity.dLoopA % 60);
                        int nHour = (int)(nMinute / 60);
                        nMinute = nMinute % 60;
                        int nDec = (int)((activity.dLoopA * 100) % 100);
                        EditText textAValue = (EditText)getActivity().findViewById(R.id.textAValue);
                        textAValue.setText(String.format("%02d:%02d:%02d.%02d", nHour, nMinute, nSecond, nDec));
                    }
                    PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
                    playlistFragment.updateSavingEffect();
                }
            }
        }
        else if(v.getId() == R.id.viewBtnBLeft || v.getId() == R.id.viewBtnBRight || v.getId() == R.id.btnB)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                if(MainActivity.hStream != 0)
                {
                    ImageButton btnB = (ImageButton)getActivity().findViewById(R.id.btnB);
                    if(btnB.isSelected()) {
                        activity.dLoopB = 0.0;
                        activity.bLoopB = false;
                        btnB.setSelected(false);
                        btnB.setImageResource(R.drawable.ic_abloop_b);
                        View viewMaskB = getActivity().findViewById(R.id.viewMaskB);
                        viewMaskB.setVisibility(View.INVISIBLE);

                        EditText textBValue  = (EditText)getActivity().findViewById(R.id.textBValue);
                        textBValue.setText("00:00:00.00");
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
                        View viewMaskB = getActivity().findViewById(R.id.viewMaskB);
                        viewMaskB.setTranslationX(nLeft);
                        viewMaskB.getLayoutParams().width = nBkWidth - nLeft;
                        viewMaskB.setVisibility(View.VISIBLE);
                        viewMaskB.requestLayout();
                        EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
                        if(!effectFragment.isReverse())
                            BASS.BASS_ChannelSetPosition(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopA), BASS.BASS_POS_BYTE);

                        int nMinute = (int)(activity.dLoopB / 60);
                        int nSecond = (int)(activity.dLoopB % 60);
                        int nHour = (int)(nMinute / 60);
                        nMinute = nMinute % 60;
                        int nDec = (int)((activity.dLoopB * 100) % 100);
                        EditText textBValue  = (EditText)getActivity().findViewById(R.id.textBValue);
                        textBValue.setText(String.format("%02d:%02d:%02d.%02d", nHour, nMinute, nSecond, nDec));
                    }
                    activity.setSync();

                    PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
                    playlistFragment.updateSavingEffect();
                }
            }
        }
        else if(v.getId() == R.id.btnPrevmarker)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                if(MainActivity.hStream != 0)
                {
                    EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
                    boolean bReverse = effectFragment.isReverse();
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

                    PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
                    playlistFragment.updateSavingEffect();
                }
            }
        }
        else if(v.getId() == R.id.btnNextmarker)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                if(MainActivity.hStream != 0)
                {
                    EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
                    boolean bReverse = effectFragment.isReverse();
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

                    PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
                    playlistFragment.updateSavingEffect();
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
                    TextView textView = new TextView(getActivity());
                    textView.setText("▼");
                    RelativeLayout layout = (RelativeLayout)getActivity().findViewById(R.id.relative_loop);
                    layout.addView(textView);
                    textView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    int nLeft = (int) (viewCurPos.getX() - nMaxWidth * dCurPos / dLength - textView.getMeasuredWidth() / 2);
                    RelativeLayout relativeWave = (RelativeLayout)getActivity().findViewById(R.id.relativeWave);
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

                    PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
                    playlistFragment.updateSavingEffect();
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
                            RelativeLayout layout = (RelativeLayout)getActivity().findViewById(R.id.relative_loop);
                            layout.removeView(textView);
                            arMarkerText.remove(i);
                            break;
                        }
                    }
                    PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
                    playlistFragment.updateSavingEffect();
                }
            }
        }
        else if (v.getId() == R.id.btnLoopmarker)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                ImageButton btnLoopmarker = (ImageButton)getActivity().findViewById(R.id.btnLoopmarker);
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

                    PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
                    playlistFragment.updateSavingEffect();
                }
            }
        }
        else if(v.getId() == R.id.waveView)
        {
            float fX = event.getX();
            float fY = event.getY();
            viewCurPos = getActivity().findViewById(R.id.viewCurPos);
            int nBkWidth = waveView.getWidth();
            double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
            double dPos = dLength * fX / nBkWidth;
            LinearLayout ABButton = (LinearLayout)getActivity().findViewById(R.id.ABButton);
            if(ABButton.getVisibility() == View.VISIBLE && ((activity.bLoopA && dPos < activity.dLoopA) || (activity.bLoopB && activity.dLoopB < dPos)))
                dPos = activity.dLoopA;
            setCurPos(dPos);
            return true;
        }

        return false;
    }

    public void clearLoop()
    {
        clearLoop(true);
    }

    public void clearLoop(boolean bSave)
    {
        if(getActivity() == null) return;

        if(getActivity().findViewById(R.id.btnA) != null)
        {
            ImageButton btnA = (ImageButton)getActivity().findViewById(R.id.btnA);
            btnA.setSelected(false);
            btnA.setImageResource(R.drawable.ic_abloop_a);
        }

        if(getActivity().findViewById(R.id.viewMaskA) != null)
        {
            View viewMaskA = getActivity().findViewById(R.id.viewMaskA);
            viewMaskA.setVisibility(View.INVISIBLE);
        }

        if(getActivity().findViewById(R.id.btnB) != null)
        {
            ImageButton btnB = (ImageButton)getActivity().findViewById(R.id.btnB);
            btnB.setSelected(false);
            btnB.setImageResource(R.drawable.ic_abloop_b);
        }

        if(getActivity().findViewById(R.id.viewMaskB) != null)
        {
            View viewMaskB = getActivity().findViewById(R.id.viewMaskB);
            viewMaskB.setVisibility(View.INVISIBLE);
        }

        for(int i = 0; i < arMarkerText.size(); i++)
        {
            TextView textView = arMarkerText.get(i);
            RelativeLayout layout = (RelativeLayout)getActivity().findViewById(R.id.relative_loop);
            layout.removeView(textView);
        }

        EditText textAValue  = (EditText)getActivity().findViewById(R.id.textAValue);
        textAValue.setText("00:00:00.00");

        EditText textBValue  = (EditText)getActivity().findViewById(R.id.textBValue);
        textBValue.setText("00:00:00.00");

        arMarkerTime.clear();
        arMarkerText.clear();

        waveView.clearWaveForm(true);

        if(bSave) {
            PlaylistFragment playlistFragment = (PlaylistFragment) activity.mSectionsPagerAdapter.getItem(0);
            playlistFragment.updateSavingEffect();
        }
    }

    public void drawWaveForm(String strPath)
    {
        waveView.drawWaveForm(strPath);
    }

    public double getMarkerSrcPos()
    {
        double dPos = 0.0;
        LinearLayout MarkerButton = (LinearLayout)getActivity().findViewById(R.id.MarkerButton);
        ImageButton btnLoopmarker = (ImageButton)getActivity().findViewById(R.id.btnLoopmarker);

        MainActivity activity = (MainActivity)getActivity();
        EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
        if(effectFragment.isReverse()) {
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
        LinearLayout MarkerButton = (LinearLayout)getActivity().findViewById(R.id.MarkerButton);
        ImageButton btnLoopmarker = (ImageButton)getActivity().findViewById(R.id.btnLoopmarker);

        MainActivity activity = (MainActivity)getActivity();
        EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
        if(effectFragment.isReverse()) {
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
