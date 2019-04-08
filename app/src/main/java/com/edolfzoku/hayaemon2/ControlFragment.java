/*
 * ControlFragment
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
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS_FX;

import java.util.Locale;

public class ControlFragment extends Fragment implements View.OnTouchListener, View.OnLongClickListener, View.OnFocusChangeListener
{
    private MainActivity activity = null;
    float fSpeed = 0.0f;
    float fPitch = 0.0f;
    boolean bTouching = false;
    boolean bLink = false;
    boolean bLockSpeed = false;
    boolean bLockPitch = false;
    boolean bSnap = false;
    private int nMinSpeed = 10;
    private int nMaxSpeed = 400;
    private int nMinPitch = -12;
    private int nMaxPitch = 12;
    private boolean isContinue = true;
    private Handler handler;

    public boolean isSnap() { return bSnap; }
    public void setSnap(boolean bSnap) { this.bSnap = bSnap; }
    public int getMinSpeed() { return nMinSpeed; }
    public void setMinSpeed(int nMinSpeed) { this.nMinSpeed = nMinSpeed; }
    public int getMaxSpeed() { return nMaxSpeed; }
    public void setMaxSpeed(int nMaxSpeed) { this.nMaxSpeed = nMaxSpeed; }
    public int getMinPitch() { return nMinPitch; }
    public void setMinPitch(int nMinPitch) { this.nMinPitch = nMinPitch; }
    public int getMaxPitch() { return nMaxPitch; }
    public void setMaxPitch(int nMaxPitch) { this.nMaxPitch = nMaxPitch; }

    public ControlFragment()
    {
        handler = new Handler();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof MainActivity) {
            activity = (MainActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        activity = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        int nImgPointTag = preferences.getInt("imgPointTag", 0);
        ImageView imgPoint = activity.findViewById(R.id.imgPoint);
        if(nImgPointTag == 1) {
            imgPoint.setImageResource(R.drawable.control_pointer_uni_murasaki);
            imgPoint.setTag(1);
        }
        else if(nImgPointTag == 2) {
            imgPoint.setImageResource(R.drawable.control_pointer_uni_bafun);
            imgPoint.setTag(2);
        }
        if(nImgPointTag != 0) {
            imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
            imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        View viewBk = activity.findViewById(R.id.imgBack);
        viewBk.setOnTouchListener(this);

        activity.findViewById(R.id.btnLink).setOnTouchListener(this);

        AnimationButton btnLockSpeed = activity.findViewById(R.id.btnLockSpeed);
        btnLockSpeed.setOnTouchListener(this);

        AnimationButton btnLockPitch = activity.findViewById(R.id.btnLockPitch);
        btnLockPitch.setOnTouchListener(this);

        Button btnResetSpeed = activity.findViewById(R.id.btnResetSpeed);
        btnResetSpeed.setOnTouchListener(this);

        Button btnResetPitch = activity.findViewById(R.id.btnResetPitch);
        btnResetPitch.setOnTouchListener(this);

        AnimationButton btnSpeedUp = activity.findViewById(R.id.btnSpeedUp);
        btnSpeedUp.setOnTouchListener(this);
        btnSpeedUp.setOnLongClickListener(this);

        AnimationButton btnSpeedDown = activity.findViewById(R.id.btnSpeedDown);
        btnSpeedDown.setOnTouchListener(this);
        btnSpeedDown.setOnLongClickListener(this);

        AnimationButton btnPitchUp = activity.findViewById(R.id.btnPitchUp);
        btnPitchUp.setOnTouchListener(this);
        btnPitchUp.setOnLongClickListener(this);

        AnimationButton btnPitchDown = activity.findViewById(R.id.btnPitchDown);
        btnPitchDown.setOnTouchListener(this);
        btnPitchDown.setOnLongClickListener(this);

        EditText textSpeedValue = activity.findViewById(R.id.textSpeedValue);
        textSpeedValue.setOnFocusChangeListener(this);

        EditText textPitchValue = activity.findViewById(R.id.textPitchValue);
        textPitchValue.setOnFocusChangeListener(this);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if(hasFocus)
        {
            if(v.getId() == R.id.textSpeedValue)
                showSpeedDialog();
            else if(v.getId() == R.id.textPitchValue)
                showPitchDialog();
        }
    }

    public void showSpeedDialog()
    {
        SpeedFragmentDialog dialog = new SpeedFragmentDialog();
        FragmentManager fm = getFragmentManager();
        if(fm != null) dialog.show(fm, "span_setting_dialog");
    }

    public void showPitchDialog()
    {
        PitchFragmentDialog dialog = new PitchFragmentDialog();
        FragmentManager fm = getFragmentManager();
        if(fm != null) dialog.show(fm, "span_setting_dialog");
    }

    public void setSpeedUp()
    {
        float fMaxSpeed = 4.0f;
        fMaxSpeed = (fMaxSpeed - 1.0f) * 100.0f;
        fSpeed += 1.0;
        if(fSpeed >= fMaxSpeed) fSpeed = fMaxSpeed;
            setSpeed(fSpeed);
    }

    public void setSpeedDown()
    {
        float fMinSpeed = 0.1f;
        fMinSpeed = (1.0f - fMinSpeed) * -100.0f;
        fSpeed -= 1.0;
        if(fSpeed <= fMinSpeed) fSpeed = fMinSpeed;
            setSpeed(fSpeed);
    }

    public void setSpeed(float fSpeed)
    {
        if(bTouching) setSpeed(fSpeed, false);
        else setSpeed(fSpeed, true);
    }

    public void setSpeed(float fSpeed, boolean bSave)
    {
        this.fSpeed = fSpeed;
        if(MainActivity.hStream != 0)
            BASS.BASS_ChannelSetAttribute(MainActivity.hStream, BASS_FX.BASS_ATTRIB_TEMPO, fSpeed);
        EditText textSpeedValue = activity.findViewById(R.id.textSpeedValue);
        textSpeedValue.clearFocus();
        textSpeedValue.setText(String.format(Locale.getDefault(), "%.1f%%", fSpeed + 100));

        ImageView imgPoint = activity.findViewById(R.id.imgPoint);
        int nPtWidth = imgPoint.getWidth();
        int nPtHeight = imgPoint.getHeight();
        View viewBk = activity.findViewById(R.id.imgBack);
        int nBkLeft = viewBk.getLeft();
        int nBkWidth = viewBk.getWidth();
        float fCenter = nBkWidth / 2.0f;
        float fMaxSpeed = nMaxSpeed / 100.0f;
        float fMinSpeed = nMinSpeed / 100.0f;
        fMaxSpeed = (fMaxSpeed - 1.0f) * 100.0f;
        fMinSpeed = (1.0f - fMinSpeed) * -100.0f;
        float fX;
        if(fSpeed >= 0.0)
            fX = nBkLeft + fCenter + (fSpeed / fMaxSpeed) * (fCenter - nPtWidth / 2.0f);
        else
            fX = nBkLeft + fCenter - (fSpeed / fMinSpeed) * (fCenter - nPtWidth / 2.0f);
        float fY = imgPoint.getY() + nPtHeight / 2.0f;
        imgPoint.animate()
            .x(fX - nPtWidth / 2.0f)
            .y(fY - nPtHeight / 2.0f)
            .setDuration(0)
            .start();

        if(bSave) activity.playlistFragment.updateSavingEffect();

        if(bLink) {
            bLink = false;
            if(fSpeed == 0.0f) setPitch(0, bSave);
            else if(fSpeed > 0.0f) {
                double dSpeed = ((double)fSpeed + 100.0) / 100.0;
                double dSemitone = 0.1;
                double dMinimum = Math.pow(2.0, 0.09 / 12.0);
                while(true) {
                    double dTempSpeed = dSpeed / Math.pow(2.0, dSemitone / 12.0);
                    if(dTempSpeed < dMinimum) break;
                    dSemitone += 0.1;
                }
                double dMaxPitch = 60.0;
                if(dSemitone > dMaxPitch) dSemitone = dMaxPitch;
                setPitch((float)dSemitone, bSave);
            }
            else {
                double dSpeed = ((double)fSpeed + 100.0) / 100.0;
                double dSemitone = -0.1;
                double dMaximum = Math.pow(2.0, -0.09 / 12.0);
                while(true) {
                    double dTempSpeed = dSpeed / Math.pow(2.0, dSemitone / 12.0);
                    if(dTempSpeed > dMaximum) break;
                    dSemitone -= 0.1;
                }
                setPitch((float)dSemitone, bSave);
            }
            bLink = true;
        }
    }

    public void setPitchUp()
    {
        float fMaxPitch = 24.0f;
        fPitch += 1.0;
        if(fPitch >= fMaxPitch) fPitch = fMaxPitch;
            setPitch(fPitch);
    }

    public void setPitchDown()
    {
        float fMinPitch = -24.0f;
        fPitch -= 1.0;
        if(fPitch <= fMinPitch) fPitch = fMinPitch;
            setPitch(fPitch);
    }

    public void setPitch(float fPitch)
    {
        setPitch(fPitch, true);
    }

    public void setPitch(float fPitch, boolean bSave)
    {
        this.fPitch = fPitch;
        if(MainActivity.hStream != 0)
            BASS.BASS_ChannelSetAttribute(MainActivity.hStream, BASS_FX.BASS_ATTRIB_TEMPO_PITCH, fPitch);
        TextView textPitchValue = activity.findViewById(R.id.textPitchValue);
        textPitchValue.clearFocus();
        if(fPitch > 0.05)
            textPitchValue.setText(String.format(Locale.getDefault(), "♯%.1f", fPitch));
        else if(fPitch < -0.05)
            textPitchValue.setText(String.format(Locale.getDefault(), "♭%.1f", fPitch * -1));
        else
        {
            textPitchValue.setText(String.format(Locale.getDefault(), "　%.1f", fPitch));
            if(textPitchValue.getText().toString().equals("　-0.0"))
                textPitchValue.setText("　0.0");
        }

        ImageView imgPoint = activity.findViewById(R.id.imgPoint);
        int nPtWidth = imgPoint.getWidth();
        int nPtHeight = imgPoint.getHeight();
        View viewBk = activity.findViewById(R.id.imgBack);
        int nBkTop = viewBk.getTop();
        int nBkHeight = viewBk.getHeight();
        float fBkHeight = nBkHeight - nPtHeight;
        float fMaxPitch = nMaxPitch;
        float fMinPitch = nMinPitch;
        float fDummyPitch = fPitch;
        if(fDummyPitch > fMaxPitch) fDummyPitch = fMaxPitch;
        else if(fDummyPitch < fMinPitch) fDummyPitch = fMinPitch;
        float fX = imgPoint.getX() + nPtWidth / 2.0f;
        float fY = nBkTop + fBkHeight - ((fDummyPitch - fMinPitch) / (fMaxPitch - fMinPitch)) * fBkHeight;
        imgPoint.animate()
            .x(fX - nPtWidth / 2.0f)
            .y(fY)
            .setDuration(0)
            .start();

        if(bSave) activity.playlistFragment.updateSavingEffect();

        if(bLink) {
            bLink = false;
            if(Math.pow(2.0, fPitch / 12.0) < 0.1) setSpeed(-90.0f, bSave);
            else setSpeed((float)(Math.pow(2.0, fPitch / 12.0) * 100.0f - 100.0f), bSave);
            bLink = true;
        }
    }

    public void clearFocus()
    {
        EditText textSpeedValue = activity.findViewById(R.id.textSpeedValue);
        textSpeedValue.clearFocus();

        EditText textPitchValue = activity.findViewById(R.id.textPitchValue);
        textPitchValue.clearFocus();
    }

    Runnable repeatSpeedUp = new Runnable()
    {
        @Override
        public void run()
        {
            if(!isContinue)
                return;
            setSpeedUp();
            handler.postDelayed(this, 100);
        }
    };

    Runnable repeatSpeedDown = new Runnable()
    {
        @Override
        public void run()
        {
            if(!isContinue)
                return;
            setSpeedDown();
            handler.postDelayed(this, 100);
        }
    };

    Runnable repeatPitchUp = new Runnable()
    {
        @Override
        public void run()
        {
            if(!isContinue)
                return;
            setPitchUp();
            handler.postDelayed(this, 200);
        }
    };

    Runnable repeatPitchDown = new Runnable()
    {
        @Override
        public void run()
        {
            if(!isContinue)
                return;
            setPitchDown();
            handler.postDelayed(this, 200);
        }
    };

    @Override
    public boolean onLongClick(View v)
    {
        if(v.getId() == R.id.btnSpeedUp)
        {
            isContinue = true;
            handler.post(repeatSpeedUp);
            return true;
        }
        else if(v.getId() == R.id.btnSpeedDown)
        {
            isContinue = true;
            handler.post(repeatSpeedDown);
            return true;
        }
        else if(v.getId() == R.id.btnPitchUp)
        {
            isContinue = true;
            handler.post(repeatPitchUp);
            return true;
        }
        else if(v.getId() == R.id.btnPitchDown)
        {
            isContinue = true;
            handler.post(repeatPitchDown);
            return true;
        }
        return false;
    }

    public void setLink(boolean bLink)
    {
        AnimationButton btnLink = activity.findViewById(R.id.btnLink);
        this.bLink = bLink;
        if(this.bLink) {
            btnLink.setImageResource(R.drawable.control_link_on);
            bLockSpeed = bLockPitch = false;
            AnimationButton btnLockSpeed = activity.findViewById(R.id.btnLockSpeed);
            btnLockSpeed.setImageResource(R.drawable.ic_control_unlock);
            AnimationButton btnLockPitch = activity.findViewById(R.id.btnLockPitch);
            btnLockPitch.setImageResource(R.drawable.ic_control_unlock);
        }
        else btnLink.setImageResource(R.drawable.control_link_off);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if(v.getId() == R.id.btnLink)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
                setLink(!bLink);
            return false;
        }
        else if(v.getId() == R.id.btnLockSpeed)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                AnimationButton btnLockSpeed = (AnimationButton)v;
                bLockSpeed = !bLockSpeed;
                if(bLockSpeed) {
                    btnLockSpeed.setImageResource(R.drawable.ic_control_lock);
                    bLink = false;
                    AnimationButton btnLink = activity.findViewById(R.id.btnLink);
                    btnLink.setImageResource(R.drawable.control_link_off);
                }
                else btnLockSpeed.setImageResource(R.drawable.ic_control_unlock);
            }
            return false;
        }
        else if(v.getId() == R.id.btnLockPitch)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                AnimationButton btnLockPitch = (AnimationButton)v;
                bLockPitch = !bLockPitch;
                if(bLockPitch) {
                    btnLockPitch.setImageResource(R.drawable.ic_control_lock);
                    bLink = false;
                    AnimationButton btnLink = activity.findViewById(R.id.btnLink);
                    btnLink.setImageResource(R.drawable.control_link_off);
                }
                else btnLockPitch.setImageResource(R.drawable.ic_control_unlock);
            }
            return false;
        }
        else if(v.getId() == R.id.btnResetSpeed)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                setSpeed(0.0f);
            }
            return false;
        }
        else if(v.getId() == R.id.btnResetPitch)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                setPitch(0.0f);
            }
            return false;
        }
        else if(v.getId() == R.id.btnSpeedUp)
        {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                setSpeedUp();
            else if (event.getAction() == MotionEvent.ACTION_UP)
                isContinue = false;
            return false;
        }
        else if(v.getId() == R.id.btnSpeedDown)
        {
            if(event.getAction() == MotionEvent.ACTION_DOWN)
                setSpeedDown();
            else if (event.getAction() == MotionEvent.ACTION_UP)
                isContinue = false;
            return false;
        }
        else if(v.getId() == R.id.btnPitchUp)
        {
            if(event.getAction() == MotionEvent.ACTION_DOWN)
                setPitchUp();
            else if (event.getAction() == MotionEvent.ACTION_UP)
                isContinue = false;
            return false;
        }
        else if(v.getId() == R.id.btnPitchDown)
        {
            if(event.getAction() == MotionEvent.ACTION_DOWN)
                setPitchDown();
            else if (event.getAction() == MotionEvent.ACTION_UP)
                isContinue = false;
            return false;
        }
        else if(v.getId() == R.id.imgBack)
        {
            if(event.getAction() == MotionEvent.ACTION_DOWN) bTouching = true;
            else if(event.getAction() == MotionEvent.ACTION_UP) bTouching = false;

            ImageView imgPoint = activity.findViewById(R.id.imgPoint);
            View viewBk = activity.findViewById(R.id.imgBack);
            int nPtWidth = imgPoint.getWidth();
            int nPtHeight = imgPoint.getHeight();
            int nBkLeft = viewBk.getLeft();
            int nBkTop = viewBk.getTop();
            int nBkWidth = viewBk.getWidth();
            int nBkHeight = viewBk.getHeight();
            float fX = imgPoint.getX();
            float fY = imgPoint.getY();

            if(!bLockSpeed)
            {
                fX = nBkLeft + event.getX();
                if(fX < nBkLeft + nPtWidth / 2) fX = nBkLeft + nPtWidth / 2.0f;
                else if(fX > nBkLeft + nBkWidth - nPtWidth / 2) fX = nBkLeft + nBkWidth - nPtWidth / 2.0f;

                float fBkHalfWidth = nBkWidth / 2.0f - nPtWidth / 2.0f;
                float fDX = fX - (nBkLeft + nBkWidth / 2.0f);
                fX -= nPtWidth / 2.0f;
                float fMaxSpeed = nMaxSpeed / 100.0f;
                float fMinSpeed = nMinSpeed / 100.0f;
                fMaxSpeed = (fMaxSpeed - 1.0f) * 100.0f;
                fMinSpeed = (1.0f - fMinSpeed) * -100.0f;
                if(fX >= nBkLeft + nBkWidth / 2) fSpeed = (fDX / fBkHalfWidth) * fMaxSpeed;
                else fSpeed = (fDX / fBkHalfWidth) * -fMinSpeed;
                if(bSnap) fSpeed = Math.round(fSpeed);
                setSpeed(fSpeed);
            }

            if(!bLockPitch && !bLink)
            {
                fY = nBkTop + event.getY();
                if(fY < nBkTop + nPtHeight / 2) fY = nBkTop + nPtHeight / 2.0f;
                else if(fY > nBkTop + nBkHeight - nPtHeight / 2) fY = nBkTop + nBkHeight - nPtHeight / 2.0f;
                float fBkHeight = nBkHeight - nPtHeight;
                float fDY = fY - (nBkTop + nPtHeight / 2.0f);
                fY -= nPtHeight / 2.0f;
                float fMaxPitch = nMaxPitch;
                float fMinPitch = nMinPitch;
                fPitch = ((fDY / fBkHeight) * (fMaxPitch - fMinPitch) + fMinPitch) * -1;
                if(bSnap) fPitch = Math.round(fPitch);
                if(MainActivity.hStream != 0)
                    BASS.BASS_ChannelSetAttribute(MainActivity.hStream, BASS_FX.BASS_ATTRIB_TEMPO_PITCH, fPitch);

                TextView textPitchValue = activity.findViewById(R.id.textPitchValue);
                if(fPitch > 0.05)
                    textPitchValue.setText(String.format(Locale.getDefault(), "♯%.1f", fPitch));
                else if(fPitch < -0.05)
                    textPitchValue.setText(String.format(Locale.getDefault(), "♭%.1f", fPitch * -1));
                else
                {
                    textPitchValue.setText(String.format(Locale.getDefault(), "　%.1f", fPitch));
                    if(textPitchValue.getText().toString().equals("　-0.0"))
                        textPitchValue.setText("　0.0");
                }
            }

            if(!bLink) {
                imgPoint.animate()
                        .x(fX)
                        .y(fY)
                        .setDuration(0)
                        .start();

                activity.playlistFragment.updateSavingEffect();
            }

            return true;
        }
        return false;
    }
}
