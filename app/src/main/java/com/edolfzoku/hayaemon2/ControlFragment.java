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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS_FX;

import java.util.Locale;

public class ControlFragment extends Fragment implements View.OnTouchListener, View.OnLongClickListener, View.OnFocusChangeListener
{
    private MainActivity mActivity = null;
    private float mSpeed = 0.0f;
    private float mPitch = 0.0f;
    private boolean mTouchingFlag = false;
    private boolean mLinkFlag = false;
    private boolean mLockSpeedFlag = false;
    private boolean mLockPitchFlag = false;
    private boolean mSnapFlag = false;
    private int mMinSpeed = 10;
    private int mMaxSpeed = 400;
    private int mMinPitch = -12;
    private int mMaxPitch = 12;
    private boolean mContinueFlag = true;
    private final Handler mHandler;

    private ImageView mImgPoint;
    private View mViewBk, mViewSepBelowSpeed, mViewSepBelowPitch, mViewLineHorizontal, mViewLineVertical;
    private AnimationButton mBtnLink, mBtnLockSpeed, mBtnLockPitch, mBtnPitchUp, mBtnPitchDown, mBtnSpeedUp, mBtnSpeedDown;
    private Button mBtnResetSpeed, mBtnResetPitch;
    private TextView mTextSpeed, mTextPitch;
    private EditText mTextSpeedValue, mTextPitchValue;

    public float getSpeed() { return mSpeed; }
    public float getPitch() { return mPitch; }
    public boolean isSnap() { return mSnapFlag; }
    public void setSnap(boolean snapFlag) { mSnapFlag = snapFlag; }
    public int getMinSpeed() { return mMinSpeed; }
    public void setMinSpeed(int minSpeed) { mMinSpeed = minSpeed; }
    public int getMaxSpeed() { return mMaxSpeed; }
    public void setMaxSpeed(int maxSpeed) { mMaxSpeed = maxSpeed; }
    public int getMinPitch() { return mMinPitch; }
    public void setMinPitch(int minPitch) { mMinPitch = minPitch; }
    public int getMaxPitch() { return mMaxPitch; }
    public void setMaxPitch(int maxPitch) { mMaxPitch = maxPitch; }

    public ImageView getImgPoint() { return mImgPoint; }

    public ControlFragment()
    {
        mHandler = new Handler();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof MainActivity) {
            mActivity = (MainActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mActivity = null;
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

        mImgPoint = mActivity.findViewById(R.id.imgPoint);
        mViewBk = mActivity.findViewById(R.id.imgBack);
        mViewSepBelowSpeed = mActivity.findViewById(R.id.viewSepBelowSpeed);
        mViewSepBelowPitch = mActivity.findViewById(R.id.viewSepBelowPitch);
        mViewLineHorizontal = mActivity.findViewById(R.id.viewLineHorizontal);
        mViewLineVertical = mActivity.findViewById(R.id.viewLineVertical);
        mBtnLink = mActivity.findViewById(R.id.btnLink);
        mBtnLockSpeed = mActivity.findViewById(R.id.btnLockSpeed);
        mBtnLockPitch = mActivity.findViewById(R.id.btnLockPitch);
        mTextSpeed = mActivity.findViewById(R.id.textSpeed);
        mTextSpeedValue = mActivity.findViewById(R.id.textSpeedValue);
        mTextPitch = mActivity.findViewById(R.id.textPitch);
        mTextPitchValue = mActivity.findViewById(R.id.textPitchValue);
        mBtnResetSpeed = mActivity.findViewById(R.id.btnResetSpeed);
        mBtnResetPitch = mActivity.findViewById(R.id.btnResetPitch);
        mBtnSpeedUp = mActivity.findViewById(R.id.btnSpeedUp);
        mBtnSpeedDown = mActivity.findViewById(R.id.btnSpeedDown);
        mBtnPitchUp = mActivity.findViewById(R.id.btnPitchUp);
        mBtnPitchDown = mActivity.findViewById(R.id.btnPitchDown);

        SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        int nImgPointTag = preferences.getInt("imgPointTag", 0);
        if(nImgPointTag == 0) mImgPoint.setTag(0);
        else if(nImgPointTag == 1) {
            mImgPoint.setBackgroundResource(R.drawable.control_pointer_uni_murasaki);
            mImgPoint.setTag(1);
        }
        else if(nImgPointTag == 2) {
            mImgPoint.setBackgroundResource(R.drawable.control_pointer_uni_bafun);
            mImgPoint.setTag(2);
        }
        else if(nImgPointTag == 3) {
            mImgPoint.setBackgroundResource(mActivity.isDarkMode() ? R.drawable.control_pointer_camper_pk_dark : R.drawable.control_pointer_camper_pk);
            mImgPoint.setTag(3);
            AnimationDrawable anime = (AnimationDrawable)mImgPoint.getBackground();
            anime.start();
        }
        else if(nImgPointTag == 4) {
            mImgPoint.setBackgroundResource(mActivity.isDarkMode() ? R.drawable.control_pointer_camper_bl_dark : R.drawable.control_pointer_camper_bl);
            mImgPoint.setTag(4);
            AnimationDrawable anime = (AnimationDrawable)mImgPoint.getBackground();
            anime.start();
        }
        else if(nImgPointTag == 5) {
            mImgPoint.setBackgroundResource(mActivity.isDarkMode() ? R.drawable.control_pointer_camper_or_dark : R.drawable.control_pointer_camper_or);
            mImgPoint.setTag(5);
            AnimationDrawable anime = (AnimationDrawable)mImgPoint.getBackground();
            anime.start();
        }
        mImgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
        mImgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

        mViewBk.setOnTouchListener(this);
        mBtnLink.setOnTouchListener(this);
        mBtnLockSpeed.setOnTouchListener(this);
        mBtnLockPitch.setOnTouchListener(this);
        mBtnResetSpeed.setOnTouchListener(this);
        mBtnResetPitch.setOnTouchListener(this);
        mBtnSpeedUp.setOnTouchListener(this);
        mBtnSpeedUp.setOnLongClickListener(this);
        mBtnSpeedDown.setOnTouchListener(this);
        mBtnSpeedDown.setOnLongClickListener(this);
        mBtnPitchUp.setOnTouchListener(this);
        mBtnPitchUp.setOnLongClickListener(this);
        mBtnPitchDown.setOnTouchListener(this);
        mBtnPitchDown.setOnLongClickListener(this);
        mTextSpeedValue.setOnFocusChangeListener(this);
        mTextPitchValue.setOnFocusChangeListener(this);

        if(mActivity.isDarkMode()) setDarkMode(false);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if(hasFocus)
        {
            if(v.getId() == R.id.textSpeedValue) {
                mTextSpeedValue.clearFocus();
                mTextSpeedValue.setCursorVisible(true);
                mTextSpeedValue.setSelection(mTextSpeedValue.getText().length());
                showSpeedDialog();
            }
            else if(v.getId() == R.id.textPitchValue) {
                mTextPitchValue.clearFocus();
                mTextPitchValue.setCursorVisible(true);
                mTextPitchValue.setSelection(mTextPitchValue.getText().length());
                showPitchDialog();
            }
        }
    }

    private void showSpeedDialog()
    {
        SpeedFragmentDialog dialog = new SpeedFragmentDialog(mActivity);
        dialog.show();
    }

    private void showPitchDialog()
    {
        PitchFragmentDialog dialog = new PitchFragmentDialog(mActivity);
        dialog.show();
    }

    private void setSpeedUp()
    {
        float fMaxSpeed = 50.0f;
        fMaxSpeed = (fMaxSpeed - 1.0f) * 100.0f;
        mSpeed += 1.0;
        if(mSpeed >= fMaxSpeed) mSpeed = fMaxSpeed;
            setSpeed(mSpeed);
    }

    private void setSpeedDown()
    {
        float fMinSpeed = 0.1f;
        fMinSpeed = (1.0f - fMinSpeed) * -100.0f;
        mSpeed -= 1.0;
        if(mSpeed <= fMinSpeed) mSpeed = fMinSpeed;
            setSpeed(mSpeed);
    }

    public void setSpeed(float speed)
    {
        if(mTouchingFlag) setSpeed(speed, false);
        else setSpeed(speed, true);
    }

    public void setSpeed(float speed, boolean bSave)
    {
        mSpeed = speed;
        if(MainActivity.sStream != 0)
            BASS.BASS_ChannelSetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO, mSpeed);
        mTextSpeedValue.clearFocus();
        float tempSpeed = mSpeed + 100;
        int speedInt = (int)tempSpeed;
        int speedDec = (int)((tempSpeed * 10) % 10);
        String strSpeed = speedInt + "." + speedDec + "%";
        mTextSpeedValue.setText(strSpeed);

        int nPtWidth = mImgPoint.getWidth();
        int nPtHeight = mImgPoint.getHeight();
        int nBkLeft = mViewBk.getLeft();
        int nBkWidth = mViewBk.getWidth();
        float fCenter = nBkWidth / 2.0f;
        float fMaxSpeed = mMaxSpeed / 100.0f;
        float fMinSpeed = mMinSpeed / 100.0f;
        fMaxSpeed = (fMaxSpeed - 1.0f) * 100.0f;
        fMinSpeed = (1.0f - fMinSpeed) * -100.0f;
        float fDummySpeed = mSpeed;
        if(fDummySpeed > fMaxSpeed) fDummySpeed = fMaxSpeed;
        else if(fDummySpeed < fMinSpeed) fDummySpeed = fMinSpeed;
        float fX;
        if(mSpeed >= 0.0)
            fX = nBkLeft + fCenter + (fDummySpeed / fMaxSpeed) * (fCenter - nPtWidth / 2.0f);
        else
            fX = nBkLeft + fCenter - (fDummySpeed / fMinSpeed) * (fCenter - nPtWidth / 2.0f);
        float fY = mImgPoint.getY() + nPtHeight / 2.0f;
        mImgPoint.animate()
            .x(fX - nPtWidth / 2.0f)
            .y(fY - nPtHeight / 2.0f)
            .setDuration(0)
            .start();

        if(bSave) mActivity.playlistFragment.updateSavingEffect();

        if(mLinkFlag) {
            mLinkFlag = false;
            if(mSpeed == 0.0f) setPitch(0, bSave);
            else if(mSpeed > 0.0f) {
                double dSpeed = ((double)mSpeed + 100.0) / 100.0;
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
                double dSpeed = ((double)mSpeed + 100.0) / 100.0;
                double dSemitone = -0.1;
                double dMaximum = Math.pow(2.0, -0.09 / 12.0);
                while(true) {
                    double dTempSpeed = dSpeed / Math.pow(2.0, dSemitone / 12.0);
                    if(dTempSpeed > dMaximum) break;
                    dSemitone -= 0.1;
                }
                setPitch((float)dSemitone, bSave);
            }
            mLinkFlag = true;
        }
    }

    private void setPitchUp()
    {
        float fMaxPitch = 60.0f;
        mPitch += 1.0;
        if(mPitch >= fMaxPitch) mPitch = fMaxPitch;
            setPitch(mPitch);
    }

    private void setPitchDown()
    {
        float fMinPitch = -60.0f;
        mPitch -= 1.0;
        if(mPitch <= fMinPitch) mPitch = fMinPitch;
            setPitch(mPitch);
    }

    public void setPitch(float pitchValue)
    {
        setPitch(pitchValue, true);
    }

    public void setPitch(float pitchValue, boolean bSave)
    {
        mPitch = pitchValue;
        if(MainActivity.sStream != 0)
            BASS.BASS_ChannelSetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO_PITCH, mPitch);
        mTextPitchValue.clearFocus();
        float fTempPitch = mPitch < 0.0f ? mPitch * -1 : mPitch;
        int pitchInt = (int)fTempPitch;
        int pitchDec = (int)((fTempPitch * 10) % 10);
        String strPitch;
        if(mPitch > 0.05) strPitch = "♯" + pitchInt + "." + pitchDec;
        else if(mPitch < -0.05) strPitch = "♭" + pitchInt + "." + pitchDec;
        else strPitch = "　" + pitchInt + "." + pitchDec;
        mTextPitchValue.setText(strPitch);

        int nPtWidth = mImgPoint.getWidth();
        int nPtHeight = mImgPoint.getHeight();
        int nBkTop = mViewBk.getTop();
        int nBkHeight = mViewBk.getHeight();
        float fBkHeight = nBkHeight - nPtHeight;
        float fHalfHeight = fBkHeight / 2.0f;
        float fMaxPitch = mMaxPitch;
        float fMinPitch = mMinPitch;
        float fDummyPitch = mPitch;
        if(fDummyPitch > fMaxPitch) fDummyPitch = fMaxPitch;
        else if(fDummyPitch < fMinPitch) fDummyPitch = fMinPitch;
        float fX = mImgPoint.getX() + nPtWidth / 2.0f;
        float fY;
        if(mPitch > 0.0f) fY = nBkTop + fHalfHeight - (fDummyPitch / fMaxPitch) * fHalfHeight;
        else fY = nBkTop + fHalfHeight - (fDummyPitch / -fMinPitch) * fHalfHeight;
        mImgPoint.animate()
            .x(fX - nPtWidth / 2.0f)
            .y(fY)
            .setDuration(0)
            .start();

        if(bSave) mActivity.playlistFragment.updateSavingEffect();

        if(mLinkFlag) {
            mLinkFlag = false;
            if(Math.pow(2.0, mPitch / 12.0) < 0.1) setSpeed(-90.0f, bSave);
            else setSpeed((float)(Math.pow(2.0, mPitch / 12.0) * 100.0f - 100.0f), bSave);
            mLinkFlag = true;
        }
    }

    public void clearFocus()
    {
        mTextSpeedValue.clearFocus();
        mTextPitchValue.clearFocus();
        mTextSpeedValue.setCursorVisible(false);
        mTextPitchValue.setCursorVisible(false);
    }

    private final Runnable repeatSpeedUp = new Runnable()
    {
        @Override
        public void run()
        {
            if(!mContinueFlag)
                return;
            setSpeedUp();
            mHandler.postDelayed(this, 100);
        }
    };

    private final Runnable repeatSpeedDown = new Runnable()
    {
        @Override
        public void run()
        {
            if(!mContinueFlag)
                return;
            setSpeedDown();
            mHandler.postDelayed(this, 100);
        }
    };

    private final Runnable repeatPitchUp = new Runnable()
    {
        @Override
        public void run()
        {
            if(!mContinueFlag)
                return;
            setPitchUp();
            mHandler.postDelayed(this, 200);
        }
    };

    private final Runnable repeatPitchDown = new Runnable()
    {
        @Override
        public void run()
        {
            if(!mContinueFlag)
                return;
            setPitchDown();
            mHandler.postDelayed(this, 200);
        }
    };

    @Override
    public boolean onLongClick(View v)
    {
        if(v.getId() == R.id.btnSpeedUp)
        {
            mContinueFlag = true;
            mHandler.post(repeatSpeedUp);
            return true;
        }
        else if(v.getId() == R.id.btnSpeedDown)
        {
            mContinueFlag = true;
            mHandler.post(repeatSpeedDown);
            return true;
        }
        else if(v.getId() == R.id.btnPitchUp)
        {
            mContinueFlag = true;
            mHandler.post(repeatPitchUp);
            return true;
        }
        else if(v.getId() == R.id.btnPitchDown)
        {
            mContinueFlag = true;
            mHandler.post(repeatPitchDown);
            return true;
        }
        return false;
    }

    public void setLink(boolean linkFrag)
    {
        mLinkFlag = linkFrag;
        if(mLinkFlag) {
            mBtnLink.setImageResource(mActivity.isDarkMode() ? R.drawable.ic_control_link_on_dark : R.drawable.ic_control_link_on);
            mLockSpeedFlag = mLockPitchFlag = false;
            mBtnLockSpeed.setImageResource(mActivity.isDarkMode() ? R.drawable.ic_control_unlock_dark : R.drawable.ic_control_unlock);
            mBtnLockPitch.setImageResource(mActivity.isDarkMode() ? R.drawable.ic_control_unlock_dark : R.drawable.ic_control_unlock);
        }
        else mBtnLink.setImageResource(mActivity.isDarkMode() ? R.drawable.ic_control_link_off_dark : R.drawable.ic_control_link_off);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if(v.getId() == R.id.btnLink)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
                setLink(!mLinkFlag);
            return false;
        }
        else if(v.getId() == R.id.btnLockSpeed)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                mLockSpeedFlag = !mLockSpeedFlag;
                if(mLockSpeedFlag) {
                    mBtnLockSpeed.setImageResource(mActivity.isDarkMode() ? R.drawable.ic_control_lock_dark : R.drawable.ic_control_lock);
                    mLinkFlag = false;
                    mBtnLink.setImageResource(mActivity.isDarkMode() ? R.drawable.ic_control_link_off_dark : R.drawable.ic_control_link_off);
                }
                else mBtnLockSpeed.setImageResource(mActivity.isDarkMode() ? R.drawable.ic_control_unlock_dark : R.drawable.ic_control_unlock);
            }
            return false;
        }
        else if(v.getId() == R.id.btnLockPitch)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                mLockPitchFlag = !mLockPitchFlag;
                if(mLockPitchFlag) {
                    mBtnLockPitch.setImageResource(mActivity.isDarkMode() ? R.drawable.ic_control_lock_dark : R.drawable.ic_control_lock);
                    mLinkFlag = false;
                    mBtnLink.setImageResource(mActivity.isDarkMode() ? R.drawable.ic_control_link_off_dark : R.drawable.ic_control_link_off);
                }
                else mBtnLockPitch.setImageResource(mActivity.isDarkMode() ? R.drawable.ic_control_unlock_dark : R.drawable.ic_control_unlock);
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
                setPitch(0.0f);
            return false;
        }
        else if(v.getId() == R.id.btnSpeedUp)
        {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                setSpeedUp();
            else if (event.getAction() == MotionEvent.ACTION_UP)
                mContinueFlag = false;
            return false;
        }
        else if(v.getId() == R.id.btnSpeedDown)
        {
            if(event.getAction() == MotionEvent.ACTION_DOWN)
                setSpeedDown();
            else if (event.getAction() == MotionEvent.ACTION_UP)
                mContinueFlag = false;
            return false;
        }
        else if(v.getId() == R.id.btnPitchUp)
        {
            if(event.getAction() == MotionEvent.ACTION_DOWN)
                setPitchUp();
            else if (event.getAction() == MotionEvent.ACTION_UP)
                mContinueFlag = false;
            return false;
        }
        else if(v.getId() == R.id.btnPitchDown)
        {
            if(event.getAction() == MotionEvent.ACTION_DOWN)
                setPitchDown();
            else if (event.getAction() == MotionEvent.ACTION_UP)
                mContinueFlag = false;
            return false;
        }
        else if(v.getId() == R.id.imgBack)
        {
            if(event.getAction() == MotionEvent.ACTION_DOWN) mTouchingFlag = true;
            else if(event.getAction() == MotionEvent.ACTION_UP) mTouchingFlag = false;

            boolean bSave = event.getAction() == MotionEvent.ACTION_UP;
            int nPtWidth = mImgPoint.getWidth();
            int nPtHeight = mImgPoint.getHeight();
            int nBkLeft = mViewBk.getLeft();
            int nBkTop = mViewBk.getTop();
            int nBkWidth = mViewBk.getWidth();
            int nBkHeight = mViewBk.getHeight();
            float fX = mImgPoint.getX();
            float fY = mImgPoint.getY();

            if(!mLockSpeedFlag)
            {
                fX = nBkLeft + event.getX();
                if(fX < nBkLeft + nPtWidth / 2) fX = nBkLeft + nPtWidth / 2.0f;
                else if(fX > nBkLeft + nBkWidth - nPtWidth / 2) fX = nBkLeft + nBkWidth - nPtWidth / 2.0f;

                float fBkHalfWidth = nBkWidth / 2.0f - nPtWidth / 2.0f;
                float fDX = fX - (nBkLeft + nBkWidth / 2.0f);
                fX -= nPtWidth / 2.0f;
                float fMaxSpeed = mMaxSpeed / 100.0f;
                float fMinSpeed = mMinSpeed / 100.0f;
                fMaxSpeed = (fMaxSpeed - 1.0f) * 100.0f;
                fMinSpeed = (1.0f - fMinSpeed) * -100.0f;
                if(fX >= nBkLeft + nBkWidth / 2) mSpeed = (fDX / fBkHalfWidth) * fMaxSpeed;
                else mSpeed = (fDX / fBkHalfWidth) * -fMinSpeed;
                if(mSnapFlag) mSpeed = Math.round(mSpeed);
                setSpeed(mSpeed, bSave);
            }

            if(!mLockPitchFlag && !mLinkFlag)
            {
                fY = nBkTop + event.getY();
                if(fY < nBkTop + nPtHeight / 2) fY = nBkTop + nPtHeight / 2.0f;
                else if(fY > nBkTop + nBkHeight - nPtHeight / 2) fY = nBkTop + nBkHeight - nPtHeight / 2.0f;
                float fBkHeight = nBkHeight - nPtHeight;
                float fHalfHeight = fBkHeight / 2.0f;
                float fDY = fY - (nBkTop + nPtHeight / 2.0f);
                fY -= nPtHeight / 2.0f;
                float fMaxPitch = mMaxPitch;
                float fMinPitch = mMinPitch;
                if(fDY <= fHalfHeight) mPitch = ((fHalfHeight - fDY) / fHalfHeight) * fMaxPitch;
                else mPitch = ((fDY - fHalfHeight) / fHalfHeight) * fMinPitch;
                if(mSnapFlag) mPitch = Math.round(mPitch);
                if(MainActivity.sStream != 0)
                    BASS.BASS_ChannelSetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO_PITCH, mPitch);

                if(mPitch > 0.05)
                    mTextPitchValue.setText(String.format(Locale.getDefault(), "♯%.1f", mPitch));
                else if(mPitch < -0.05)
                    mTextPitchValue.setText(String.format(Locale.getDefault(), "♭%.1f", mPitch * -1));
                else
                {
                    mTextPitchValue.setText(String.format(Locale.getDefault(), "　%.1f", mPitch));
                    if(mTextPitchValue.getText().toString().equals("　-0.0"))
                        mTextPitchValue.setText("　0.0");
                }
            }

            if(!mLinkFlag) {
                mImgPoint.animate()
                        .x(fX)
                        .y(fY)
                        .setDuration(0)
                        .start();

                if(bSave) mActivity.playlistFragment.updateSavingEffect();
            }

            return true;
        }
        return false;
    }

    public void setLightMode(boolean animated) {
        final int nDarkModeSep = getResources().getColor(R.color.darkModeSep);
        final int nLightModeSep = getResources().getColor(R.color.lightModeSep);
        final int nLightModeText = getResources().getColor(android.R.color.black);
        final int nDarkModeText = getResources().getColor(android.R.color.white);
        final int nLightModeBlue = getResources().getColor(R.color.lightModeBlue);
        final int nDarkModeBlue = getResources().getColor(R.color.darkModeBlue);
        if(animated) {
            final ArgbEvaluator eval = new ArgbEvaluator();
            ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float fProgress = valueAnimator.getAnimatedFraction();
                    int nColorModeSep = (Integer) eval.evaluate(fProgress, nDarkModeSep, nLightModeSep);
                    int nColorModeText = (Integer) eval.evaluate(fProgress, nDarkModeText, nLightModeText);
                    int nColorModeBlue = (Integer) eval.evaluate(fProgress, nDarkModeBlue, nLightModeBlue);
                    mViewSepBelowSpeed.setBackgroundColor(nColorModeSep);
                    mViewSepBelowPitch.setBackgroundColor(nColorModeSep);
                    mViewLineHorizontal.setBackgroundColor(nColorModeSep);
                    mViewLineVertical.setBackgroundColor(nColorModeSep);
                    mTextSpeed.setTextColor(nColorModeText);
                    mTextSpeedValue.setTextColor(nColorModeText);
                    mTextPitch.setTextColor(nColorModeText);
                    mTextPitchValue.setTextColor(nColorModeText);
                    mBtnResetSpeed.setTextColor(nColorModeBlue);
                    mBtnResetPitch.setTextColor(nColorModeBlue);
                }
            });

            TransitionDrawable tdBtnLink;
            if(mLinkFlag)
                tdBtnLink = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_control_link_on_dark), getResources().getDrawable(R.drawable.ic_control_link_on)});
            else
                tdBtnLink = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_control_link_off_dark), getResources().getDrawable(R.drawable.ic_control_link_off)});
            TransitionDrawable tdBtnLockSpeed;
            if(mLockSpeedFlag)
                tdBtnLockSpeed = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_control_lock_dark), getResources().getDrawable(R.drawable.ic_control_lock)});
            else
                tdBtnLockSpeed = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_control_unlock_dark), getResources().getDrawable(R.drawable.ic_control_unlock)});
            TransitionDrawable tdBtnLockPitch;
            if(mLockPitchFlag)
                tdBtnLockPitch = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_control_lock_dark), getResources().getDrawable(R.drawable.ic_control_lock)});
            else
                tdBtnLockPitch = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_control_unlock_dark), getResources().getDrawable(R.drawable.ic_control_unlock)});
            TransitionDrawable tdBtnPitchUp = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.control_pitch_sharp_dark), getResources().getDrawable(R.drawable.control_pitch_sharp)});
            TransitionDrawable tdBtnPitchDown = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.control_pitch_flat_dark), getResources().getDrawable(R.drawable.control_pitch_flat)});
            TransitionDrawable tdBtnSpeedUp = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.control_speed_up_dark), getResources().getDrawable(R.drawable.control_speed_up)});
            TransitionDrawable tdBtnSpeedDown = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.control_speed_down_dark), getResources().getDrawable(R.drawable.control_speed_down)});

            mBtnLink.setImageDrawable(tdBtnLink);
            mBtnLockSpeed.setImageDrawable(tdBtnLockSpeed);
            mBtnLockPitch.setImageDrawable(tdBtnLockPitch);
            mBtnPitchUp.setImageDrawable(tdBtnPitchUp);
            mBtnPitchDown.setImageDrawable(tdBtnPitchDown);
            mBtnSpeedUp.setImageDrawable(tdBtnSpeedUp);
            mBtnSpeedDown.setImageDrawable(tdBtnSpeedDown);

            final boolean linkFlag = mLinkFlag;
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mBtnLink.setImageDrawable(linkFlag ? getResources().getDrawable(R.drawable.ic_control_link_on) : getResources().getDrawable(R.drawable.ic_control_link_off));
                }
            });

            int duration = 300;
            anim.setDuration(duration).start();
            tdBtnLink.startTransition(duration);
            tdBtnLockSpeed.startTransition(duration);
            tdBtnLockPitch.startTransition(duration);
            tdBtnPitchUp.startTransition(duration);
            tdBtnPitchDown.startTransition(duration);
            tdBtnSpeedUp.startTransition(duration);
            tdBtnSpeedDown.startTransition(duration);
            if((Integer)mImgPoint.getTag() == 0) {
                TransitionDrawable tdImgPoint = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_control_pointer_dark), getResources().getDrawable(R.drawable.ic_control_pointer)});
                mImgPoint.setImageDrawable(tdImgPoint);
                tdImgPoint.startTransition(duration);
            }
        }
        else {
            mViewSepBelowSpeed.setBackgroundColor(nLightModeSep);
            mViewSepBelowPitch.setBackgroundColor(nLightModeSep);
            mViewLineHorizontal.setBackgroundColor(nLightModeSep);
            mViewLineVertical.setBackgroundColor(nLightModeSep);
            mTextSpeed.setTextColor(nLightModeText);
            mTextSpeedValue.setTextColor(nLightModeText);
            mTextPitch.setTextColor(nLightModeText);
            mTextPitchValue.setTextColor(nLightModeText);
            mBtnResetSpeed.setTextColor(nLightModeBlue);
            mBtnResetPitch.setTextColor(nLightModeBlue);

            if(mLinkFlag)
                mBtnLink.setImageDrawable(getResources().getDrawable(R.drawable.ic_control_link_on));
            else
                mBtnLink.setImageDrawable(getResources().getDrawable(R.drawable.ic_control_link_off));
            if(mLockSpeedFlag)
                mBtnLockSpeed.setImageDrawable(getResources().getDrawable(R.drawable.ic_control_lock));
            else
                mBtnLockSpeed.setImageDrawable(getResources().getDrawable(R.drawable.ic_control_unlock));
            if(mLockPitchFlag)
                mBtnLockPitch.setImageDrawable(getResources().getDrawable(R.drawable.ic_control_lock));
            else
                mBtnLockPitch.setImageDrawable(getResources().getDrawable(R.drawable.ic_control_unlock));
            mBtnPitchUp.setImageDrawable(getResources().getDrawable(R.drawable.control_pitch_sharp));
            mBtnPitchDown.setImageDrawable(getResources().getDrawable(R.drawable.control_pitch_flat));
            mBtnSpeedUp.setImageDrawable(getResources().getDrawable(R.drawable.control_speed_up));
            mBtnSpeedDown.setImageDrawable(getResources().getDrawable(R.drawable.control_speed_down));
            if((Integer)mImgPoint.getTag() == 0)
                mImgPoint.setImageDrawable(getResources().getDrawable(R.drawable.ic_control_pointer));
        }

        mTextSpeedValue.setBackgroundResource(R.drawable.editborder);
        mTextPitchValue.setBackgroundResource(R.drawable.editborder);
        mBtnResetSpeed.setBackgroundResource(R.drawable.resetbutton);
        mBtnResetPitch.setBackgroundResource(R.drawable.resetbutton);
        if((Integer)mImgPoint.getTag() == 0)
            mImgPoint.setBackgroundResource(R.drawable.ic_control_pointer);
        else if((Integer)mImgPoint.getTag() == 3) {
            mImgPoint.setBackgroundResource(R.drawable.control_pointer_camper_pk);
            AnimationDrawable anime = (AnimationDrawable)mImgPoint.getBackground();
            anime.start();
        }
        else if((Integer)mImgPoint.getTag() == 4) {
            mImgPoint.setBackgroundResource(R.drawable.control_pointer_camper_bl);
            AnimationDrawable anime = (AnimationDrawable)mImgPoint.getBackground();
            anime.start();
        }
        else if((Integer)mImgPoint.getTag() == 5) {
            mImgPoint.setBackgroundResource(R.drawable.control_pointer_camper_or);
            AnimationDrawable anime = (AnimationDrawable)mImgPoint.getBackground();
            anime.start();
        }
    }

    public void setDarkMode(boolean animated) {
        if(mActivity == null) return;
        final int nDarkModeSep = getResources().getColor(R.color.darkModeSep);
        final int nLightModeSep = getResources().getColor(R.color.lightModeSep);
        final int nLightModeText = getResources().getColor(android.R.color.black);
        final int nDarkModeText = getResources().getColor(android.R.color.white);
        final int nLightModeBlue = getResources().getColor(R.color.lightModeBlue);
        final int nDarkModeBlue = getResources().getColor(R.color.darkModeBlue);
        final ArgbEvaluator eval = new ArgbEvaluator();
        ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fProgress = valueAnimator.getAnimatedFraction();
                int nColorModeSep = (Integer) eval.evaluate(fProgress, nLightModeSep, nDarkModeSep);
                int nColorModeText = (Integer) eval.evaluate(fProgress, nLightModeText, nDarkModeText);
                int nColorModeBlue = (Integer) eval.evaluate(fProgress, nLightModeBlue, nDarkModeBlue);
                mViewSepBelowSpeed.setBackgroundColor(nColorModeSep);
                mViewSepBelowPitch.setBackgroundColor(nColorModeSep);
                mViewLineHorizontal.setBackgroundColor(nColorModeSep);
                mViewLineVertical.setBackgroundColor(nColorModeSep);
                mTextSpeed.setTextColor(nColorModeText);
                mTextSpeedValue.setTextColor(nColorModeText);
                mTextPitch.setTextColor(nColorModeText);
                mTextPitchValue.setTextColor(nColorModeText);
                mBtnResetSpeed.setTextColor(nColorModeBlue);
                mBtnResetPitch.setTextColor(nColorModeBlue);
            }
        });

        TransitionDrawable tdBtnLink;
        if(mLinkFlag)
            tdBtnLink = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_control_link_on), getResources().getDrawable(R.drawable.ic_control_link_on_dark)});
        else
            tdBtnLink = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_control_link_off), getResources().getDrawable(R.drawable.ic_control_link_off_dark)});
        TransitionDrawable tdBtnLockSpeed;
        if(mLockSpeedFlag)
            tdBtnLockSpeed = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_control_lock), getResources().getDrawable(R.drawable.ic_control_lock_dark)});
        else
            tdBtnLockSpeed = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_control_unlock), getResources().getDrawable(R.drawable.ic_control_unlock_dark)});
        TransitionDrawable tdBtnLockPitch;
        if(mLockPitchFlag)
            tdBtnLockPitch = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_control_lock), getResources().getDrawable(R.drawable.ic_control_lock_dark)});
        else
            tdBtnLockPitch = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_control_unlock), getResources().getDrawable(R.drawable.ic_control_unlock_dark)});
        TransitionDrawable tdBtnPitchUp = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.control_pitch_sharp), getResources().getDrawable(R.drawable.control_pitch_sharp_dark)});
        TransitionDrawable tdBtnPitchDown = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.control_pitch_flat), getResources().getDrawable(R.drawable.control_pitch_flat_dark)});
        TransitionDrawable tdBtnSpeedUp = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.control_speed_up), getResources().getDrawable(R.drawable.control_speed_up_dark)});
        TransitionDrawable tdBtnSpeedDown = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.control_speed_down), getResources().getDrawable(R.drawable.control_speed_down_dark)});

        mBtnLink.setImageDrawable(tdBtnLink);
        mBtnLockSpeed.setImageDrawable(tdBtnLockSpeed);
        mBtnLockPitch.setImageDrawable(tdBtnLockPitch);
        mBtnPitchUp.setImageDrawable(tdBtnPitchUp);
        mBtnPitchDown.setImageDrawable(tdBtnPitchDown);
        mBtnSpeedUp.setImageDrawable(tdBtnSpeedUp);
        mBtnSpeedDown.setImageDrawable(tdBtnSpeedDown);

        final boolean linkFlag = mLinkFlag;
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mBtnLink.setImageDrawable(linkFlag ? getResources().getDrawable(R.drawable.ic_control_link_on_dark) : getResources().getDrawable(R.drawable.ic_control_link_off_dark));
            }
        });

        int duration = animated ? 300 : 0;
        anim.setDuration(duration).start();
        tdBtnLink.startTransition(duration);
        tdBtnLockSpeed.startTransition(duration);
        tdBtnLockPitch.startTransition(duration);
        tdBtnPitchUp.startTransition(duration);
        tdBtnPitchDown.startTransition(duration);
        tdBtnSpeedUp.startTransition(duration);
        tdBtnSpeedDown.startTransition(duration);

        mTextSpeedValue.setBackgroundResource(R.drawable.editborder_dark);
        mTextPitchValue.setBackgroundResource(R.drawable.editborder_dark);
        mBtnResetSpeed.setBackgroundResource(R.drawable.resetbutton_dark);
        mBtnResetPitch.setBackgroundResource(R.drawable.resetbutton_dark);
        if((Integer)mImgPoint.getTag() == 0)
            mImgPoint.setBackgroundResource(R.drawable.ic_control_pointer_dark);
        else if((Integer)mImgPoint.getTag() == 3) {
            mImgPoint.setBackgroundResource(R.drawable.control_pointer_camper_pk_dark);
            AnimationDrawable anime = (AnimationDrawable)mImgPoint.getBackground();
            anime.start();
        }
        else if((Integer)mImgPoint.getTag() == 4) {
            mImgPoint.setBackgroundResource(R.drawable.control_pointer_camper_bl_dark);
            AnimationDrawable anime = (AnimationDrawable)mImgPoint.getBackground();
            anime.start();
        }
        else if((Integer)mImgPoint.getTag() == 5) {
            mImgPoint.setBackgroundResource(R.drawable.control_pointer_camper_or_dark);
            AnimationDrawable anime = (AnimationDrawable)mImgPoint.getBackground();
            anime.start();
        }
    }
}
