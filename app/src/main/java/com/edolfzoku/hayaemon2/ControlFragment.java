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

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS_FX;

public class ControlFragment extends Fragment implements View.OnTouchListener, View.OnLongClickListener, View.OnFocusChangeListener {
    float fSpeed = 0.0f;
    float fPitch = 0.0f;
    private boolean isContinue = true;
    private Handler handler;

    public ControlFragment()
    {
        handler = new Handler();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_control, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View viewBk = getActivity().findViewById(R.id.imgBack);
        viewBk.setOnTouchListener(this);

        TextView textResetSpeed = (TextView)getActivity().findViewById(R.id.textResetSpeed);
        textResetSpeed.setOnTouchListener(this);

        TextView textResetPitch = (TextView)getActivity().findViewById(R.id.textResetPitch);
        textResetPitch.setOnTouchListener(this);

        TextView textSpeedUp = (TextView)getActivity().findViewById(R.id.textSpeedUp);
        textSpeedUp.setOnTouchListener(this);
        textSpeedUp.setOnLongClickListener(this);

        TextView textSpeedDown = (TextView)getActivity().findViewById(R.id.textSpeedDown);
        textSpeedDown.setOnTouchListener(this);
        textSpeedDown.setOnLongClickListener(this);

        TextView textPitchUp = (TextView)getActivity().findViewById(R.id.textPitchUp);
        textPitchUp.setOnTouchListener(this);
        textPitchUp.setOnLongClickListener(this);

        TextView textPitchDown = (TextView)getActivity().findViewById(R.id.textPitchDown);
        textPitchDown.setOnTouchListener(this);
        textPitchDown.setOnLongClickListener(this);

        EditText textSpeedValue = (EditText)getActivity().findViewById(R.id.textSpeedValue);
        textSpeedValue.setOnFocusChangeListener(this);

        EditText textPitchValue = (EditText)getActivity().findViewById(R.id.textPitchValue);
        textPitchValue.setOnFocusChangeListener(this);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if(hasFocus)
        {
            if(v.getId() == R.id.textSpeedValue)
            {
                showSpeedDialog();
            }
            else if(v.getId() == R.id.textPitchValue)
            {
                showPitchDialog();
            }
        }
    }

    public void showSpeedDialog() {
        SpeedFragmentDialog dialog = new SpeedFragmentDialog();
        dialog.show(getFragmentManager(), "span_setting_dialog");
    }

    public void showPitchDialog() {
        PitchFragmentDialog dialog = new PitchFragmentDialog();
        dialog.show(getFragmentManager(), "span_setting_dialog");
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
        this.fSpeed = fSpeed;
        if(MainActivity.hStream != 0)
            BASS.BASS_ChannelSetAttribute(MainActivity.hStream, BASS_FX.BASS_ATTRIB_TEMPO, fSpeed);
        EditText textSpeedValue = (EditText)getActivity().findViewById(R.id.textSpeedValue);
        textSpeedValue.clearFocus();
        textSpeedValue.setText(String.format("%.1f%%", fSpeed + 100));

        ImageView imgPoint = (ImageView)getActivity().findViewById(R.id.imgPoint);
        int nPtWidth = imgPoint.getWidth();
        int nPtHeight = imgPoint.getHeight();
        View viewBk = getActivity().findViewById(R.id.imgBack);
        int nBkWidth = viewBk.getWidth();
        float fCenter = nBkWidth / 2;
        float fMaxSpeed = 4.0f;
        float fMinSpeed = 0.1f;
        fMaxSpeed = (fMaxSpeed - 1.0f) * 100.0f;
        fMinSpeed = (1.0f - fMinSpeed) * -100.0f;
        float fX = 0.0f;
        if(fSpeed >= 0.0)
            fX = fCenter + (fSpeed / fMaxSpeed) * fCenter;
        else
            fX = fCenter - (fSpeed / fMinSpeed) * fCenter;
        float fY = imgPoint.getY() + nPtHeight / 2;
        imgPoint.animate()
                .x(fX - nPtWidth / 2)
                .y(fY - nPtHeight / 2)
                .setDuration(0)
                .start();
    }

    public void setPitchUp()
    {
        float fMaxPitch = 12.0f;
        fPitch += 1.0;
        if(fPitch >= fMaxPitch) fPitch = fMaxPitch;
        setPitch(fPitch);
    }

    public void setPitchDown()
    {
        float fMinPitch = -12.0f;
        fPitch -= 1.0;
        if(fPitch <= fMinPitch) fPitch = fMinPitch;
        setPitch(fPitch);
    }

    public void setPitch(float fPitch)
    {
        this.fPitch = fPitch;
        if(MainActivity.hStream != 0)
            BASS.BASS_ChannelSetAttribute(MainActivity.hStream, BASS_FX.BASS_ATTRIB_TEMPO_PITCH, fPitch);
        TextView textPitchValue = (TextView)getActivity().findViewById(R.id.textPitchValue);
        textPitchValue.clearFocus();
        if(fPitch > 0.05)
            textPitchValue.setText(String.format("♯%.1f", fPitch));
        else if(fPitch < -0.05)
            textPitchValue.setText(String.format("♭%.1f", fPitch * -1));
        else
        {
            textPitchValue.setText(String.format("　%.1f", fPitch));
            if(textPitchValue.getText().equals("　-0.0"))
                textPitchValue.setText("　0.0");
        }

        ImageView imgPoint = (ImageView)getActivity().findViewById(R.id.imgPoint);
        int nPtWidth = imgPoint.getWidth();
        int nPtHeight = imgPoint.getHeight();
        View viewBk = getActivity().findViewById(R.id.imgBack);
        int nBkHeight = viewBk.getHeight();
        float fMaxPitch = 12.0f;
        float fMinPitch = -12.0f;
        float fX = imgPoint.getX() + nPtWidth / 2;
        float fY = nBkHeight - ((fPitch - fMinPitch) / (fMaxPitch - fMinPitch)) * nBkHeight;
        imgPoint.animate()
                .x(fX - nPtWidth / 2)
                .y(fY - nPtHeight / 2)
                .setDuration(0)
                .start();
    }

    public void clearFocus()
    {
        EditText textSpeedValue = (EditText)getActivity().findViewById(R.id.textSpeedValue);
        textSpeedValue.clearFocus();

        EditText textPitchValue = (EditText)getActivity().findViewById(R.id.textPitchValue);
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
            handler.postDelayed(this, 200);
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
            handler.postDelayed(this, 200);
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
        if(v.getId() == R.id.textSpeedUp)
        {
            isContinue = true;
            handler.post(repeatSpeedUp);
            return true;
        }
        else if(v.getId() == R.id.textSpeedDown)
        {
            isContinue = true;
            handler.post(repeatSpeedDown);
            return true;
        }
        else if(v.getId() == R.id.textPitchUp)
        {
            isContinue = true;
            handler.post(repeatPitchUp);
            return true;
        }
        else if(v.getId() == R.id.textPitchDown)
        {
            isContinue = true;
            handler.post(repeatPitchDown);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if(v.getId() == R.id.textResetSpeed)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                setSpeed(0.0f);
            }
            return false;
        }
        else if(v.getId() == R.id.textResetPitch)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                setPitch(0.0f);
            }
            return false;
        }
        else if(v.getId() == R.id.textSpeedUp)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                isContinue = false;
                setSpeedUp();
            }
            return false;
        }
        else if(v.getId() == R.id.textSpeedDown)
        {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                isContinue = false;
                setSpeedDown();
            }
            return false;
        }
        else if(v.getId() == R.id.textPitchUp)
        {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                isContinue = false;
                setPitchUp();
            }
            return false;
        }
        else if(v.getId() == R.id.textPitchDown)
        {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                isContinue = false;
                setPitchDown();
            }
            return false;
        }
        else if(v.getId() == R.id.imgBack)
        {
            ImageView imgPoint = (ImageView)getActivity().findViewById(R.id.imgPoint);
            float fX = event.getX();
            float fY = event.getY();
            View viewBk = getActivity().findViewById(R.id.imgBack);
            int nPtWidth = imgPoint.getWidth();
            int nPtHeight = imgPoint.getHeight();
            int nBkWidth = viewBk.getWidth();
            int nBkHeight = viewBk.getHeight();

            if(fX < 0) fX = 0;
            else if(fX > nBkWidth) fX = nBkWidth;
            if(fY < 0) fY = 0;
            else if(fY > nBkHeight) fY = nBkHeight;

            float fCenter = nBkWidth / 2;
            float fDX = fX - fCenter;
            float fMaxSpeed = 4.0f;
            float fMinSpeed = 0.1f;
            fMaxSpeed = (fMaxSpeed - 1.0f) * 100.0f;
            fMinSpeed = (1.0f - fMinSpeed) * -100.0f;
            if(fX >= fCenter) fSpeed = (fDX / fCenter) * fMaxSpeed;
            else fSpeed = (fDX / fCenter) * -fMinSpeed;
            if(MainActivity.hStream != 0)
                BASS.BASS_ChannelSetAttribute(MainActivity.hStream, BASS_FX.BASS_ATTRIB_TEMPO, fSpeed);

            float fMaxPitch = 12.0f;
            float fMinPitch = -12.0f;
            fPitch = ((fY / nBkHeight) * (fMaxPitch - fMinPitch) + fMinPitch) * -1;
            if(MainActivity.hStream != 0)
                BASS.BASS_ChannelSetAttribute(MainActivity.hStream, BASS_FX.BASS_ATTRIB_TEMPO_PITCH, fPitch);

            EditText textSpeedValue = (EditText)getActivity().findViewById(R.id.textSpeedValue);
            textSpeedValue.setText(String.format("%.1f%%", fSpeed + 100));

            TextView textPitchValue = (TextView)getActivity().findViewById(R.id.textPitchValue);
            if(fPitch > 0.05)
                textPitchValue.setText(String.format("♯%.1f", fPitch));
            else if(fPitch < -0.05)
                textPitchValue.setText(String.format("♭%.1f", fPitch * -1));
            else
            {
                textPitchValue.setText(String.format("　%.1f", fPitch));
                if(textPitchValue.getText().equals("　-0.0"))
                    textPitchValue.setText("　0.0");
            }

            imgPoint.animate()
                    .x(fX - nPtWidth / 2)
                    .y(fY - nPtHeight / 2)
                    .setDuration(0)
                    .start();

            return true;
        }
        return false;
    }

}
