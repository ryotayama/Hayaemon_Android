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
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
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

        final TabLayout tabLayout = (TabLayout)getActivity().findViewById(R.id.abTab_Layout);
        tabLayout.addTab(tabLayout.newTab().setText("ABループ"));

        tabLayout.addTab(tabLayout.newTab().setText("マーカー再生"));

        viewCurPos = getActivity().findViewById(R.id.viewCurPos);
        waveView = (WaveView)getActivity().findViewById(R.id.waveView);
        waveView.setOnTouchListener(this);
        handler.post(new Runnable() {
            @Override
            public void run() {
                double dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                if (activity != null) {
                    LinearLayout ABButton = (LinearLayout)activity.findViewById(R.id.ABButton);
                    EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
                    if(ABButton.getVisibility() == View.VISIBLE && ((activity.bLoopA && dPos < activity.dLoopA) || (activity.bLoopB && activity.dLoopB < dPos))) {
                        if(effectFragment.isReverse())
                            dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopB));
                        else
                            dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopA));
                        BASS.BASS_ChannelSetPosition(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, dPos), BASS.BASS_POS_BYTE);
                    }
                    long nLength = BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE);
                    long nPos = BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE);
                    int nScreenWidth = waveView.getWidth();
                    int nMaxWidth = (int)(nScreenWidth * waveView.getZoom());
                    int nLeft = (int) (nMaxWidth * nPos / nLength);
                    int nTop = viewCurPos.getTop();
                    if(nLeft < nScreenWidth / 2) {
                        viewCurPos.animate()
                                .x(nLeft)
                                .y(nTop)
                                .setDuration(0)
                                .start();
                        if(!bContinue) waveView.invalidate();
                    }
                    else if(nScreenWidth / 2 <= nLeft && nLeft < nMaxWidth - nScreenWidth / 2) {
                        viewCurPos.animate()
                                .x(nScreenWidth / 2)
                                .y(nTop)
                                .setDuration(0)
                                .start();
                        if(!bContinue) waveView.invalidate();
                    }
                    else {
                        viewCurPos.animate()
                                .x(nScreenWidth - (nMaxWidth - nLeft))
                                .y(nTop)
                                .setDuration(0)
                                .start();
                        if(!bContinue) waveView.invalidate();
                    }
                    View viewMaskA = getActivity().findViewById(R.id.viewMaskA);
                    View viewMaskB = getActivity().findViewById(R.id.viewMaskB);
                    if(activity.bLoopA) {
                        long nPosA = BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopA);
                        nPosA = nPos - nPosA;
                        int nLeftA = (int) (viewCurPos.getX() - nMaxWidth * nPosA / nLength);
                        if(nLeftA < 0) nLeftA = 0;
                        viewMaskA.getLayoutParams().width = nLeftA;
                        viewMaskA.requestLayout();
                    }
                    if(activity.bLoopB) {
                        long nPosB = BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopB);
                        nPosB = nPos - nPosB;
                        int nLeftB = (int) (viewCurPos.getX() - nMaxWidth * nPosB / nLength);
                        if(nLeftB < 0) nLeftB = 0;
                        else if(nLeftB > waveView.getWidth()) nLeftB = waveView.getWidth();
                        viewMaskB.setTranslationX(nLeftB);
                        viewMaskB.getLayoutParams().width = waveView.getWidth() - nLeftB;
                        viewMaskB.requestLayout();
                    }
                    for(int i = 0; i < arMarkerTime.size(); i++) {
                        TextView textView = arMarkerText.get(i);
                        double dMarkerPos = arMarkerTime.get(i).doubleValue();
                        long nMarkerPos = BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, dMarkerPos);
                        nMarkerPos = nPos - nMarkerPos;
                        int nMarkerLeft = (int) (viewCurPos.getX() - nMaxWidth * nMarkerPos / nLength - textView.getMeasuredWidth() / 2);
                        if(nMarkerLeft < -textView.getMeasuredWidth()) nMarkerLeft = -textView.getMeasuredWidth();
                        textView.setTranslationX(nMarkerLeft);
                        textView.requestLayout();
                    }
                }
                handler.postDelayed(this, 50);
            }
        });
        RelativeLayout relativeZoomOut = (RelativeLayout)getActivity().findViewById(R.id.relativeZoomOut);
        relativeZoomOut.setOnTouchListener(this);
        relativeZoomOut.setOnLongClickListener(this);
        RelativeLayout relativeZoomIn = (RelativeLayout)getActivity().findViewById(R.id.relativeZoomIn);
        relativeZoomIn.setOnTouchListener(this);
        relativeZoomIn.setOnLongClickListener(this);
        ImageButton btnRewind5Sec = (ImageButton)getActivity().findViewById(R.id.btnRewind5Sec);
        btnRewind5Sec.setOnTouchListener(this);
        ImageButton btnA = (ImageButton)getActivity().findViewById(R.id.btnA);
        btnA.setSelected(false);
        btnA.setAlpha(1.0f);
        btnA.setOnTouchListener(this);
        ImageButton btnB = (ImageButton)getActivity().findViewById(R.id.btnB);
        btnB.setSelected(false);
        btnB.setAlpha(1.0f);
        btnB.setOnTouchListener(this);
        ImageButton btnForward5Sec = (ImageButton)getActivity().findViewById(R.id.btnForward5Sec);
        btnForward5Sec.setOnTouchListener(this);

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
        btnLoopmarker.setAlpha(1.0f);
        btnLoopmarker.setOnTouchListener(this);
        ImageButton btnForward5Sec2 = (ImageButton)getActivity().findViewById(R.id.btnForward5Sec2);
        btnForward5Sec2.setOnTouchListener(this);

        final LinearLayout ABLabel = (LinearLayout)getActivity().findViewById(R.id.ABLabel);
        final LinearLayout ABButton = (LinearLayout)getActivity().findViewById(R.id.ABButton);
        final LinearLayout MarkerButton = (LinearLayout)getActivity().findViewById(R.id.MarkerButton);
        final View viewMaskA = getActivity().findViewById(R.id.viewMaskA);
        final View viewMaskB = getActivity().findViewById(R.id.viewMaskB);
        final MainActivity activity = (MainActivity)getActivity();

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getText()=="ABループ"){
                    ABLabel.setVisibility(View.VISIBLE);
                    ABButton.setVisibility(View.VISIBLE);
                    if(activity.bLoopA) viewMaskA.setVisibility(View.VISIBLE);
                    if(activity.bLoopB) viewMaskB.setVisibility(View.VISIBLE);
                    activity.setSync();
                }
                else{
                    MarkerButton.setVisibility(View.VISIBLE);
                    for(int i = 0 ; i < arMarkerText.size(); i++)
                    {
                        TextView textView = arMarkerText.get(i);
                        textView.setVisibility(View.VISIBLE);
                    }
                    activity.setSync();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if(tab.getText()=="ABループ"){
                    ABLabel.setVisibility(View.INVISIBLE);
                    ABButton.setVisibility(View.INVISIBLE);
                    viewMaskA.setVisibility(View.INVISIBLE);
                    viewMaskB.setVisibility(View.INVISIBLE);
                }
                else{
                    MarkerButton.setVisibility(View.INVISIBLE);
                    for(int i = 0 ; i < arMarkerText.size(); i++)
                    {
                        TextView textView = arMarkerText.get(i);
                        textView.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        EditText textAValue = (EditText)getActivity().findViewById(R.id.textAValue);
        textAValue.setOnFocusChangeListener(this);

        EditText textBValue = (EditText)getActivity().findViewById(R.id.textBValue);
        textBValue.setOnFocusChangeListener(this);
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
            bContinue = true;
            handler.post(repeatZoomOut);
            return true;
        }
        else if (v.getId() == R.id.relativeZoomIn) {
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
            {
                showABLoopPicker(true);
            }
            else if(v.getId() == R.id.textBValue)
            {
                showABLoopPicker(false);
            }
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
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

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
        EditText textAValue = (EditText)getActivity().findViewById(R.id.textAValue);
        textAValue.clearFocus();

        EditText textBValue = (EditText)getActivity().findViewById(R.id.textBValue);
        textBValue.clearFocus();
    }

    public void setLoopA(double dLoopA)
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
        btnA.setAlpha(0.3f);

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
    }

    public void setLoopB(double dLoopB)
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
        btnB.setAlpha(0.3f);
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
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        MainActivity activity = (MainActivity)getActivity();
        if(v.getId() == R.id.relativeZoomOut) {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                bContinue = false;
                setZoomOut();
                waveView = (WaveView)getActivity().findViewById(R.id.waveView);
                waveView.redrawWaveForm();
            }
        }
        else if(v.getId() == R.id.relativeZoomIn) {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                bContinue = false;
                setZoomIn();
                waveView = (WaveView)getActivity().findViewById(R.id.waveView);
                waveView.redrawWaveForm();
            }
        }
        else if(v.getId() == R.id.btnRewind5Sec || v.getId() == R.id.btnRewind5Sec2)
        {
            if (event.getAction() == MotionEvent.ACTION_UP)
            {
                if (MainActivity.hStream != 0)
                {
                    double dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                    dPos -= 5.0;
                    if(dPos <= 0.0) dPos = 0.0;
                    LinearLayout ABButton = (LinearLayout)getActivity().findViewById(R.id.ABButton);
                    if(ABButton.getVisibility() == View.VISIBLE && ((activity.bLoopA && dPos < activity.dLoopA) || (activity.bLoopB && activity.dLoopB < dPos)))
                        dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopA));
                    BASS.BASS_ChannelSetPosition(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, dPos), BASS.BASS_POS_BYTE);
                    activity.setSync();
                }
            }
        }
        else if(v.getId() == R.id.btnForward5Sec || v.getId() == R.id.btnForward5Sec2)
        {
            if (event.getAction() == MotionEvent.ACTION_UP)
            {
                if (MainActivity.hStream != 0)
                {
                    double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
                    double dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                    dPos += 5.0;
                    if (dPos >= dLength) dPos = dLength - 1.0;
                    LinearLayout ABButton = (LinearLayout)getActivity().findViewById(R.id.ABButton);
                    if(ABButton.getVisibility() == View.VISIBLE && ((activity.bLoopA && dPos < activity.dLoopA) || (activity.bLoopB && activity.dLoopB < dPos)))
                        dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopA));
                    BASS.BASS_ChannelSetPosition(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, dPos), BASS.BASS_POS_BYTE);
                    activity.setSync();
                }
            }
        }
        else if(v.getId() == R.id.btnA)
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
                        btnA.setAlpha(1.0f);
                        View viewMaskA = getActivity().findViewById(R.id.viewMaskA);
                        viewMaskA.setVisibility(View.INVISIBLE);
                        EditText textAValue  = (EditText)getActivity().findViewById(R.id.textAValue);
                        textAValue.setText("00:00:00.00");
                    }
                    else {
                        activity.dLoopA = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                        activity.bLoopA = true;
                        btnA.setSelected(true);
                        btnA.setAlpha(0.3f);
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
                }
            }
        }
        else if(v.getId() == R.id.btnB)
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
                        btnB.setAlpha(1.0f);
                        View viewMaskB = getActivity().findViewById(R.id.viewMaskB);
                        viewMaskB.setVisibility(View.INVISIBLE);

                        EditText textBValue  = (EditText)getActivity().findViewById(R.id.textBValue);
                        textBValue.setText("00:00:00.00");
                    }
                    else {
                        activity.dLoopB = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                        activity.bLoopB = true;
                        btnB.setSelected(true);
                        btnB.setAlpha(0.3f);
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
                    final TabLayout tabLayout = (TabLayout)getActivity().findViewById(R.id.abTab_Layout);
                    textView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    int nLeft = (int) (viewCurPos.getX() - nMaxWidth * dCurPos / dLength - textView.getMeasuredWidth() / 2);
                    int nTop = waveView.getTop() - textView.getMeasuredHeight();
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
                }
            }
        }
        else if (v.getId() == R.id.btnLoopmarker)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                if(MainActivity.hStream != 0)
                {
                    ImageButton btnLoopmarker = (ImageButton)getActivity().findViewById(R.id.btnLoopmarker);
                    if(btnLoopmarker.isSelected())
                    {
                        btnLoopmarker.setSelected(false);
                        btnLoopmarker.setAlpha(1.0f);
                    }
                    else
                    {
                        btnLoopmarker.setSelected(true);
                        btnLoopmarker.setAlpha(0.3f);
                    }

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
                }
            }
        }
        else if(v.getId() == R.id.waveView)
        {
            float fX = event.getX();
            float fY = event.getY();
            viewCurPos = getActivity().findViewById(R.id.viewCurPos);
            int nBkWidth = waveView.getWidth();
            long nLength = BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE);
            long nPos = (long)(nLength * fX / nBkWidth);

            double dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, nPos);
            LinearLayout ABButton = (LinearLayout)getActivity().findViewById(R.id.ABButton);
            if(ABButton.getVisibility() == View.VISIBLE && ((activity.bLoopA && dPos < activity.dLoopA) || (activity.bLoopB && activity.dLoopB < dPos)))
                nPos = BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopA);
            BASS.BASS_ChannelSetPosition(MainActivity.hStream, nPos, BASS.BASS_POS_BYTE);
            activity.setSync();

            double dCurPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, nPos);
            int i = 0;
            EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
            boolean bReverse = effectFragment.isReverse();
            for( ; i < arMarkerTime.size(); i++) {
                dPos = arMarkerTime.get(i);
                if((!bReverse && dCurPos < dPos) || (bReverse && dCurPos < dPos - 1.0))
                    break;
            }
            nMarker = i - 1;

            activity.setSync();
            return true;
        }

        return false;
    }

    public void clearLoop()
    {
        if(getActivity() == null) return;

        if(getActivity().findViewById(R.id.btnA) != null)
        {
            ImageButton btnA = (ImageButton)getActivity().findViewById(R.id.btnA);
            btnA.setSelected(false);
            btnA.setAlpha(1.0f);
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
            btnB.setAlpha(1.0f);
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
