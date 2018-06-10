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
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.un4seen.bass.BASS;

import java.util.ArrayList;

public class LoopFragment extends Fragment implements View.OnTouchListener {
    private int nMarker; // マーカー再生時のループ位置
    private Handler handler;
    private MainActivity mainActivity;

    private View viewCurPos;
    private WaveView waveView;

    private ArrayList<Double> arMarkerTime;
    private ArrayList<TextView> arMarkerText;

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
        mainActivity = (MainActivity)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
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
                if (mainActivity != null) {
                    LinearLayout ABButton = (LinearLayout)mainActivity.findViewById(R.id.ABButton);
                    if(ABButton.getVisibility() == View.VISIBLE && ((mainActivity.bLoopA && dPos < mainActivity.dLoopA) || (mainActivity.bLoopB && mainActivity.dLoopB < dPos))) {
                        dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, mainActivity.dLoopA));
                        BASS.BASS_ChannelSetPosition(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, dPos), BASS.BASS_POS_BYTE);
                    }
                    long nLength = BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE);
                    long nPos = BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE);
                    int nBkWidth = waveView.getWidth();
                    int nLeft = (int) (nBkWidth * nPos / nLength);
                    int nTop = viewCurPos.getTop();
                    viewCurPos.animate()
                            .x(nLeft)
                            .y(nTop)
                            .setDuration(0)
                            .start();
                }
                handler.postDelayed(this, 50);
            }
        });
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

        final LinearLayout ABButton = (LinearLayout)getActivity().findViewById(R.id.ABButton);
        final LinearLayout MarkerButton = (LinearLayout)getActivity().findViewById(R.id.MarkerButton);
        final View viewMaskA = getActivity().findViewById(R.id.viewMaskA);
        final View viewMaskB = getActivity().findViewById(R.id.viewMaskB);
        final MainActivity activity = (MainActivity)getActivity();

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getText()=="ABループ"){
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
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        MainActivity activity = (MainActivity)getActivity();
        if(v.getId() == R.id.btnRewind5Sec || v.getId() == R.id.btnRewind5Sec2)
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
                        BASS.BASS_ChannelSetPosition(MainActivity.hStream, BASS.BASS_ChannelSeconds2Bytes(MainActivity.hStream, activity.dLoopA), BASS.BASS_POS_BYTE);
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
                    double dCurPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                    int i = arMarkerTime.size() - 1;
                    for( ; i >= 0; i--)
                    {
                        double dPos = arMarkerTime.get(i);
                        if(dCurPos >= dPos + 1.0)
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
                    double dCurPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                    int i = 0;
                    for( ; i < arMarkerTime.size(); i++)
                    {
                        double dPos = arMarkerTime.get(i);
                        if(dCurPos < dPos)
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
                    double dCurPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                    double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
                    TextView textView = new TextView(getActivity());
                    textView.setText("▼");
                    RelativeLayout layout = (RelativeLayout)getActivity().findViewById(R.id.relative_loop);
                    layout.addView(textView);
                    int nBkWidth = waveView.getWidth();
                    final TabLayout tabLayout = (TabLayout)getActivity().findViewById(R.id.abTab_Layout);
                    textView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    int nLeft = (int)(dCurPos * nBkWidth / dLength - textView.getMeasuredWidth() / 2);
                    int nTop = tabLayout.getHeight();
                    textView.animate()
                            .x(nLeft)
                            .y(nTop)
                            .setDuration(0)
                            .start();

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
            for( ; i < arMarkerTime.size(); i++) {
                dPos = arMarkerTime.get(i);
                if(dCurPos < dPos)
                {
                    break;
                }
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
        arMarkerTime.clear();
        arMarkerText.clear();

        waveView.clearWaveForm();
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

        if(MarkerButton.getVisibility() == View.VISIBLE && btnLoopmarker.isSelected()) // マーカー再生中
        {
            if(nMarker >= 0 && nMarker < arMarkerTime.size()) {
                dPos = arMarkerTime.get(nMarker);
            }
        }
        return dPos;
    }

    public double getMarkerDstPos()
    {
        double dPos = 0.0;
        LinearLayout MarkerButton = (LinearLayout)getActivity().findViewById(R.id.MarkerButton);
        ImageButton btnLoopmarker = (ImageButton)getActivity().findViewById(R.id.btnLoopmarker);

        if(MarkerButton.getVisibility() == View.VISIBLE && btnLoopmarker.isSelected()) // マーカー再生中
        {
            if(nMarker + 1 >= 0 && nMarker + 1 < arMarkerTime.size()) {
                dPos = arMarkerTime.get(nMarker + 1);
            }
        }
        return dPos;
    }
}
