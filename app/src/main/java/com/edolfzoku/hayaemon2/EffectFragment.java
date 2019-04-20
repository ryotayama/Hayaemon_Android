/*
 * EffectFragment
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
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS_FX;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;

import static java.lang.Boolean.FALSE;

public class EffectFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener, SeekBar.OnSeekBarChangeListener, View.OnFocusChangeListener
{
    private MainActivity mActivity = null;
    private EffectsAdapter mEffectsAdapter;
    private final ArrayList<EffectItem> mEffectItems;
    private int mDspVocalCancel = 0;
    private int mDspMonoral = 0;
    private int mDspLeft = 0;
    private int mDspRight = 0;
    private int mDspExchange = 0;
    private int mDspDoubling = 0;
    private int mDspPan = 0;
    private int mDspNormalize = 0;
    private int mFxCompressor = 0;
    private int mDspPhaseReversal = 0;
    private int mFxEcho = 0;
    private int mFxReverb = 0;
    private int mFxChorus = 0;
    private int mFxDistortion = 0;
    private float mPan = 0.0f;
    private float mFreq = 1.0f;
    private int mBpm = 120;
    private float mVol1 = 1.0f;
    private float mVol2 = 1.0f;
    private float mVol3 = 1.0f;
    private float mVol4 = 1.0f;
    private float mVol5 = 1.0f;
    private float mVol6 = 1.0f;
    private float mVol7 = 1.0f;
    private float mPeak = 0.0f;
    private float mTimeOfIncreaseSpeed = 1.0f;
    private float mIncreaseSpeed = 0.1f;
    private float mTimeOfDecreaseSpeed = 1.0f;
    private float mDecreaseSpeed = 0.1f;
    private static final int EFFECTTYPE_RANDOM = 1;
    private static final int EFFECTTYPE_VOCALCANCEL = 2;
    // private static final int EFFECTTYPE_MONORAL = 3;
    // private static final int EFFECTTYPE_LEFTONLY = 4;
    // private static final int EFFECTTYPE_RIGHTONLY = 5;
    // private static final int EFFECTTYPE_REPLACE = 6;
    // private static final int EFFECTTYPE_DOUBLING = 7;
    private static final int EFFECTTYPE_TRANSCRIBESIDEGUITAR = 8;
    private static final int EFFECTTYPE_TRANSCRIBEBASS = 9;
    private static final int EFFECTTYPE_PAN = 10;
    // private static final int EFFECTTYPE_NORMALIZE = 11;
    // private static final int EFFECTTYPE_COMPRESSOR = 12;
    private static final int EFFECTTYPE_FREQUENCY = 13;
    // private static final int EFFECTTYPE_PHASEREVERSAL = 14;
    private static final int EFFECTTYPE_ECHO_STADIUM = 15;
    // private static final int EFFECTTYPE_ECHO_HALL = 16;
    // private static final int EFFECTTYPE_ECHO_LIVEHOUSE = 17;
    // private static final int EFFECTTYPE_ECHO_ROOM = 18;
    // private static final int EFFECTTYPE_ECHO_BATHROOM = 19;
    // private static final int EFFECTTYPE_ECHO_VOCAL = 20;
    private static final int EFFECTTYPE_ECHO_MOUNTAIN = 21;
    private static final int EFFECTTYPE_REVERB_BATHROOM = 22;
    // private static final int EFFECTTYPE_REVERB_SMALLROOM = 23;
    // private static final int EFFECTTYPE_REVERB_MEDIUMROOM = 24;
    // private static final int EFFECTTYPE_REVERB_LARGEROOM = 25;
    // private static final int EFFECTTYPE_REVERB_CHURCH = 26;
    private static final int EFFECTTYPE_REVERB_CATHEDRAL = 27;
    private static final int EFFECTTYPE_CHORUS = 28;
    private static final int EFFECTTYPE_FLANGER = 29;
    private static final int EFFECTTYPE_DISTORTION_STRONG = 30;
    // private static final int EFFECTTYPE_DISTORTION_MIDDLE = 31;
    private static final int EFFECTTYPE_DISTORTION_WEAK = 32;
    static final int EFFECTTYPE_REVERSE = 33;
    private static final int EFFECTTYPE_INCREASESPEED = 34;
    private static final int EFFECTTYPE_DECREASESPEED = 35;
    private static final int EFFECTTYPE_OLDRECORD = 36;
    private static final int EFFECTTYPE_LOWBATTERY = 37;
    private static final int EFFECTTYPE_NOSENSE_STRONG = 38;
    private static final int EFFECTTYPE_NOSENSE_MIDDLE = 39;
    private static final int EFFECTTYPE_NOSENSE_WEAK = 40;
    private static final int EFFECTTYPE_EARTRAINING = 41;
    private static final int EFFECTTYPE_METRONOME = 42;
    private static final int EFFECTTYPE_RECORDNOISE = 43;
    private static final int EFFECTTYPE_ROAROFWAVES = 44;
    private static final int EFFECTTYPE_RAIN = 45;
    private static final int EFFECTTYPE_RIVER = 46;
    private static final int EFFECTTYPE_WAR = 47;
    private static final int EFFECTTYPE_FIRE = 48;
    private static final int EFFECTTYPE_CONCERTHALL = 49;
    private Timer mTimer;
    private int mSEStream;
    private int mSEStream2;
    private boolean mSE1PlayingFlag = false;
    private int mSync = 0;
    private Handler mHandler;
    private float mAccel = 0.0f;
    private float mVelo1 = 0.0f;
    private float mVelo2 = 0.0f;
    private boolean mContinueFlag = true;
    private final Handler mHandlerLongClick;

    private TextView mTextEffectName, mTextEffectDetail, mTextEffectLabel;
    private EditText mEditSpeedEffectDetail, mEditTimeEffectDetail;
    private RelativeLayout mRelativeEffectDetail, mRelativeEffect, mRelativeSliderEffectDatail, mRelativeRollerEffectDetail;
    private SeekBar mSeekEffectDetail;
    private ImageButton mBtnEffectMinus, mBtnEffectPlus;

    public void setPeak(float peak) { mPeak = peak; }
    public float getTimeOfIncreaseSpeed() { return mTimeOfIncreaseSpeed; }
    public void setTimeOfIncreaseSpeed(float timeOfIncreaseSpeed) {
        mTimeOfIncreaseSpeed = timeOfIncreaseSpeed;
        if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_INCREASESPEED).getEffectName()))
            mEditTimeEffectDetail.setText(String.format(Locale.getDefault(), "%.1f%s", mTimeOfIncreaseSpeed, getString(R.string.sec)));
    }
    public float getIncreaseSpeed() { return mIncreaseSpeed; }
    public void setIncreaseSpeed(float increaseSpeed) {
        mIncreaseSpeed = increaseSpeed;
        if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_INCREASESPEED).getEffectName()))
            mEditSpeedEffectDetail.setText(String.format(Locale.getDefault(), "%.1f%%", mIncreaseSpeed));
    }
    public float getTimeOfDecreaseSpeed() { return mTimeOfDecreaseSpeed; }
    public void setTimeOfDecreaseSpeed(float timeOmDecreaseSpeed) {
        mTimeOfDecreaseSpeed = timeOmDecreaseSpeed;
        if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_DECREASESPEED).getEffectName()))
            mEditTimeEffectDetail.setText(String.format(Locale.getDefault(), "%.1f%s", mTimeOfDecreaseSpeed, getString(R.string.sec)));
    }
    public float getDecreaseSpeed() { return mDecreaseSpeed; }
    public void setDecreaseSpeed(float decreaseSpeed) {
        mDecreaseSpeed = decreaseSpeed;
        if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_DECREASESPEED).getEffectName()))
            mEditSpeedEffectDetail.setText(String.format(Locale.getDefault(), "%.1f%%", mDecreaseSpeed));
    }

    public boolean isSelectedItem(int nItem)
    {
        if(nItem >= mEffectItems.size()) return false;
        EffectItem item = mEffectItems.get(nItem);
        return item.isSelected();
    }

    public boolean isReverse()
    {
        return mEffectItems.get(EFFECTTYPE_REVERSE).isSelected();
    }

    public ArrayList<EffectItem> getEffectItems()
    {
         return mEffectItems;
    }

    public void setEffectItems(ArrayList<EffectItem> effectItems)
    {
        for(int i = 0; i < mEffectItems.size(); i++)
        {
            EffectItem item = mEffectItems.get(i);
            for(int j = 0; j < effectItems.size(); j++)
            {
                EffectItem itemSaved = effectItems.get(j);
                if(item.getEffectName().equals(itemSaved.getEffectName()))
                    item.setSelected(itemSaved.isSelected());
            }
        }

        mEffectsAdapter.notifyDataSetChanged();
    }
    public float getPan()
    {
        return mPan;
    }
    public float getFreq()
    {
        return mFreq;
    }
    public int getBPM()
    {
        return mBpm;
    }
    public void setBPM(int bpm)
    {
        mBpm = bpm;
    }
    public float getVol1()
    {
        return mVol1;
    }
    public void setVol1(float vol1)
    {
        mVol1 = vol1;
    }
    public float getVol2()
    {
        return mVol2;
    }
    public void setVol2(float vol2)
    {
        mVol2 = vol2;
    }
    public float getVol3()
    {
        return mVol3;
    }
    public void setVol3(float vol3)
    {
        mVol3 = vol3;
    }
    public float getVol4()
    {
        return mVol4;
    }
    public void setVol4(float vol4)
    {
        mVol4 = vol4;
    }
    public float getVol5()
    {
        return mVol5;
    }
    public void setVol5(float vol5)
    {
        mVol5 = vol5;
    }
    public float getVol6()
    {
        return mVol6;
    }
    public void setVol6(float vol6)
    {
        mVol6 = vol6;
    }
    public float getVol7()
    {
        return mVol7;
    }
    public void setVol7(float vol7) { mVol7 = vol7; }

    public EffectFragment()
    {
        mEffectItems = new ArrayList<>();
        mHandlerLongClick = new Handler();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_effect, container, false);
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof MainActivity)
            mActivity = (MainActivity) context;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.btnFinish)
        {
            mRelativeEffectDetail.setVisibility(View.GONE);
            mRelativeEffect.setVisibility(View.VISIBLE);
        }
        else if (v.getId() == R.id.btnEffectMinus)
            minusValue();
        else if (v.getId() == R.id.btnEffectPlus)
            plusValue();
    }

    @Override
    public boolean onLongClick(View v)
    {
        if (v.getId() == R.id.btnEffectMinus)
        {
            mContinueFlag = true;
            mHandlerLongClick.post(repeatMinusValue);
            return true;
        }
        else if (v.getId() == R.id.btnEffectPlus)
        {
            mContinueFlag = true;
            mHandlerLongClick.post(repeatPlusValue);
            return true;
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP)
            mContinueFlag = false;
        return false;
    }

    private final Runnable repeatMinusValue = new Runnable()
    {
        @Override
        public void run()
        {
            if(!mContinueFlag)
                return;
            minusValue();
            mHandlerLongClick.postDelayed(this, 100);
        }
    };

    private final Runnable repeatPlusValue = new Runnable()
    {
        @Override
        public void run()
        {
            if(!mContinueFlag)
                return;
            plusValue();
            mHandlerLongClick.postDelayed(this, 100);
        }
    };

    private void minusValue()
    {
        int nProgress = mSeekEffectDetail.getProgress();
        nProgress -= 1;
        if(nProgress < 0) nProgress = 0;
        mSeekEffectDetail.setProgress(nProgress);
        if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_PAN).getEffectName()))
        {
            float fProgress = (nProgress - 100) / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", nProgress - 100));
            setPan(fProgress);
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_FREQUENCY).getEffectName()))
        {
            double dProgress = (double)(nProgress + 1) / 10.0;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%.1f", dProgress));
            setFreq((float)dProgress);
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_METRONOME).getEffectName()))
        {
            mBpm = nProgress + 10;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", mBpm));
            applyEffect();
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_RECORDNOISE).getEffectName()))
        {
            mVol1 = nProgress / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", nProgress));
            applyEffect();
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_ROAROFWAVES).getEffectName()))
        {
            mVol2 = nProgress / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", nProgress));
            applyEffect();
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_RAIN).getEffectName()))
        {
            mVol3 = nProgress / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", nProgress));
            applyEffect();
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_RIVER).getEffectName()))
        {
            mVol4 = nProgress / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", nProgress));
            applyEffect();
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_WAR).getEffectName()))
        {
            mVol5 = nProgress / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", nProgress));
            applyEffect();
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_FIRE).getEffectName()))
        {
            mVol6 = nProgress / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", nProgress));
            applyEffect();
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_CONCERTHALL).getEffectName()))
        {
            mVol7 = nProgress / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", nProgress));
            applyEffect();
        }
        mActivity.playlistFragment.updateSavingEffect();
    }

    private void plusValue()
    {
        int nProgress = mSeekEffectDetail.getProgress();
        nProgress += 1;
        if(nProgress > mSeekEffectDetail.getMax()) nProgress = mSeekEffectDetail.getMax();
        mSeekEffectDetail.setProgress(nProgress);
        if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_PAN).getEffectName()))
        {
            float fProgress = (nProgress - 100) / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", nProgress - 100));
            setPan(fProgress);
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_FREQUENCY).getEffectName()))
        {
            double dProgress = (double)(nProgress + 1) / 10.0;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%.1f", dProgress));
            setFreq((float)dProgress);
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_METRONOME).getEffectName()))
        {
            mBpm = nProgress + 10;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", mBpm));
            applyEffect();
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_RECORDNOISE).getEffectName()))
        {
            mVol1 = nProgress / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", nProgress));
            applyEffect();
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_ROAROFWAVES).getEffectName()))
        {
            mVol2 = nProgress / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", nProgress));
            applyEffect();
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_RAIN).getEffectName()))
        {
            mVol3 = nProgress / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", nProgress));
            applyEffect();
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_RIVER).getEffectName()))
        {
            mVol4 = nProgress / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", nProgress));
            applyEffect();
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_WAR).getEffectName()))
        {
            mVol5 = nProgress / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", nProgress));
            applyEffect();
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_FIRE).getEffectName()))
        {
            mVol6 = nProgress / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", nProgress));
            applyEffect();
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_CONCERTHALL).getEffectName()))
        {
            mVol7 = nProgress / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", nProgress));
            applyEffect();
        }
        mActivity.playlistFragment.updateSavingEffect();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mEffectsAdapter = new EffectsAdapter(mActivity, mEffectItems);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        mTextEffectName = mActivity.findViewById(R.id.textEffectName);
        mEditSpeedEffectDetail = mActivity.findViewById(R.id.editSpeedEffectDetail);
        mEditTimeEffectDetail = mActivity.findViewById(R.id.editTimeEffectDetail);
        mRelativeEffectDetail = mActivity.findViewById(R.id.relativeEffectDetail);
        mRelativeEffect = mActivity.findViewById(R.id.relativeEffects);
        mSeekEffectDetail = mActivity.findViewById(R.id.seekEffectDetail);
        mTextEffectDetail = mActivity.findViewById(R.id.textEffectDetail);
        mBtnEffectMinus = mActivity.findViewById(R.id.btnEffectMinus);
        mBtnEffectPlus = mActivity.findViewById(R.id.btnEffectPlus);
        mTextEffectLabel = mActivity.findViewById(R.id.textEffectLabel);
        mRelativeSliderEffectDatail = mActivity.findViewById(R.id.relativeSliderEffectDatail);
        mRelativeRollerEffectDetail = mActivity.findViewById(R.id.relativeRollerEffectDatail);
        RecyclerView recyclerEffects = mActivity.findViewById(R.id.recyclerEffects);
        Button mBtnFinith = mActivity.findViewById(R.id.btnFinish);

        EffectItem item = new EffectItem(getString(R.string.off), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.random), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.vocalCancel), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.monoral), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.leftOnly), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.rightOnly), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.leftAndRightReplace), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.doubling), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.transcribeSideGuitar), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.transcribeBassOctave), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.pan), true);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.normalize), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.compressor), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.frequency), true);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.phaseReversal), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.studiumEcho), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.hallEcho), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.livehouseEcho), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.roomEcho), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.bathroomEcho), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.vocalEcho), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.mountainEcho), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.reverbBathroom), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.reverbSmallRoom), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.reverbMediumRoom), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.reverbLargeRoom), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.reverbChurch), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.reverbCathedral), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.chorus), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.flanger), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.distortionStrong), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.distortionMiddle), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.distortionWeak), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.reverse), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.increaseSpeed), true);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.decreaseSpeed), true);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.oldRecord), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.lowBattery), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.noSenseStrong), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.noSenseMiddle), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.noSenseWeak), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.earTraining), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.metronome), true);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.recordNoise), true);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.wave), true);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.rain), true);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.river), true);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.war), true);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.fire), true);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.concertHall), true);
        mEffectItems.add(item);
        recyclerEffects.setHasFixedSize(false);
        LinearLayoutManager playlistsManager = new LinearLayoutManager(mActivity);
        recyclerEffects.setLayoutManager(playlistsManager);
        recyclerEffects.setAdapter(mEffectsAdapter);
        mBtnFinith.setOnClickListener(this);
        mBtnEffectMinus.setOnClickListener(this);
        mBtnEffectMinus.setOnLongClickListener(this);
        mBtnEffectMinus.setOnTouchListener(this);
        mBtnEffectPlus.setOnClickListener(this);
        mBtnEffectPlus.setOnLongClickListener(this);
        mBtnEffectPlus.setOnTouchListener(this);

        mEditTimeEffectDetail.setOnFocusChangeListener(this);
        mEditSpeedEffectDetail.setOnFocusChangeListener(this);

        mEffectItems.get(0).setSelected(true);
    }

    public void onEffectItemClick(int nEffect)
    {
        EffectItem item = mEffectItems.get(nEffect);
        item.setSelected(!item.isSelected());
        if(!item.isSelected() && nEffect == EFFECTTYPE_REVERSE)
        {
            int chan = BASS_FX.BASS_FX_TempoGetSource(MainActivity.sStream);
            BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_FORWARD);
            mActivity.setSync();
        }
        if(!item.isSelected() && (nEffect == EFFECTTYPE_RANDOM || nEffect == EFFECTTYPE_OLDRECORD || nEffect == EFFECTTYPE_LOWBATTERY || nEffect == EFFECTTYPE_EARTRAINING))
            mActivity.equalizerFragment.setEQ(0);
        if(!item.isSelected() && (nEffect == EFFECTTYPE_RANDOM || nEffect == EFFECTTYPE_NOSENSE_STRONG || nEffect == EFFECTTYPE_NOSENSE_MIDDLE || nEffect == EFFECTTYPE_NOSENSE_WEAK))
        {
            mActivity.controlFragment.setSpeed(0.0f);
            mActivity.controlFragment.setPitch(0.0f);
        }
        if(!item.isSelected() && nEffect == EFFECTTYPE_TRANSCRIBEBASS)
        {
            mActivity.equalizerFragment.setEQ(0);
            mActivity.controlFragment.setPitch(0.0f);
        }
        checkDuplicate(nEffect);
        if(mSEStream != 0)
        {
            BASS.BASS_StreamFree(mSEStream);
            mSEStream = 0;
        }
        if(mSEStream2 != 0)
        {
            BASS.BASS_StreamFree(mSEStream2);
            mSEStream2 = 0;
        }
        if(mHandler != null)
        {
            mHandler.removeCallbacks(onTimer);
            mHandler = null;
        }
        applyEffect();
        boolean bSelected = false;
        for(int i = 0; i < mEffectItems.size(); i++) {
            if(mEffectItems.get(i).isSelected()) bSelected = true;
        }
        if(!bSelected) mEffectItems.get(0).setSelected(true);
        mEffectsAdapter.notifyDataSetChanged();
        mActivity.playlistFragment.updateSavingEffect();
    }

    public void resetEffect()
    {
        EffectItem item = mEffectItems.get(0);
        item.setSelected(true);
        for(int i = 1; i < mEffectItems.size(); i++)
        {
            if(mEffectItems.get(i).isSelected() && (i == EFFECTTYPE_RANDOM || i == EFFECTTYPE_OLDRECORD || i == EFFECTTYPE_LOWBATTERY || i == EFFECTTYPE_EARTRAINING))
                mActivity.equalizerFragment.setEQ(0);
            if(mEffectItems.get(i).isSelected() && (i == EFFECTTYPE_RANDOM || i == EFFECTTYPE_NOSENSE_STRONG || i == EFFECTTYPE_NOSENSE_MIDDLE || i == EFFECTTYPE_NOSENSE_WEAK))
            {
                mActivity.controlFragment.setSpeed(0.0f);
                mActivity.controlFragment.setPitch(0.0f);
            }
            if(mEffectItems.get(i).isSelected() && (i == EFFECTTYPE_TRANSCRIBEBASS))
            {
                mActivity.equalizerFragment.setEQ(0);
                mActivity.controlFragment.setPitch(0.0f);
            }
            mEffectItems.get(i).setSelected(false);
        }
        mPan = 0.0f;
        mFreq = 1.0f;
        mEffectsAdapter.notifyDataSetChanged();
        setTimeOfIncreaseSpeed(1.0f);
        setIncreaseSpeed(0.1f);
        setTimeOfDecreaseSpeed(1.0f);
        setDecreaseSpeed(0.1f);
    }

    public void onEffectDetailClick(int nEffect)
    {
        mTextEffectName.setText(mEffectItems.get(nEffect).getEffectName());
        mSeekEffectDetail.setOnSeekBarChangeListener(null);
        if(nEffect == EFFECTTYPE_PAN)
        {
            mTextEffectLabel.setText(R.string.pan);
            int nPan = (int)(mPan * 100.0f);
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", nPan));
            // SeekBarについてはAPIエベル26以降しか最小値を設定できない為、最大値に200を設定（本来は-100～100にしたい）
            mSeekEffectDetail.setMax(200);
            mSeekEffectDetail.setProgress(nPan + 100);
        }
        else if(nEffect == EFFECTTYPE_FREQUENCY)
        {
            mTextEffectLabel.setText(R.string.frequency);
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%.1f", mFreq));
            // SeekBarについてはAPIエベル26以降しか最小値を設定できない為、最大値に39を設定（本来は1～40にしたい）
            mSeekEffectDetail.setMax(39);
            mSeekEffectDetail.setProgress((int)(mFreq * 10.0f) - 1);
        }
        else if(nEffect == EFFECTTYPE_INCREASESPEED)
        {
            mTextEffectLabel.setText(R.string.incSpeedTitle);
            mEditTimeEffectDetail.setText(String.format(Locale.getDefault(), "%.1f%s", mTimeOfIncreaseSpeed, getString(R.string.sec)));
            mEditSpeedEffectDetail.setText(String.format(Locale.getDefault(), "%.1f%%", mIncreaseSpeed));
        }
        else if(nEffect == EFFECTTYPE_DECREASESPEED)
        {
            mTextEffectLabel.setText(R.string.decSpeedTitle);
            mEditTimeEffectDetail.setText(String.format(Locale.getDefault(), "%.1f%s", mTimeOfDecreaseSpeed, getString(R.string.sec)));
            mEditSpeedEffectDetail.setText(String.format(Locale.getDefault(), "%.1f%%", mDecreaseSpeed));
        }
        else if(nEffect == EFFECTTYPE_METRONOME)
        {
            mTextEffectLabel.setText(R.string.BPM);
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", mBpm));
            mSeekEffectDetail.setProgress(0);
            // SeekBarについてはAPIエベル26以降しか最小値を設定できない為、最大値に290を設定（本来は10～300にしたい）
            mSeekEffectDetail.setMax(290);
            mSeekEffectDetail.setProgress(mBpm - 10);
        }
        else if(nEffect == EFFECTTYPE_RECORDNOISE)
        {
            mTextEffectLabel.setText(R.string.volume);
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", (int)(mVol1 * 100)));
            mSeekEffectDetail.setMax(100);
            mSeekEffectDetail.setProgress((int)(mVol1 * 100));
        }
        else if(nEffect == EFFECTTYPE_ROAROFWAVES)
        {
            mTextEffectLabel.setText(R.string.volume);
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", (int)(mVol2 * 100)));
            mSeekEffectDetail.setMax(100);
            mSeekEffectDetail.setProgress((int)(mVol2 * 100));
        }
        else if(nEffect == EFFECTTYPE_RAIN)
        {
            mTextEffectLabel.setText(R.string.volume);
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", (int)(mVol3 * 100)));
            mSeekEffectDetail.setMax(100);
            mSeekEffectDetail.setProgress((int)(mVol3 * 100));
        }
        else if(nEffect == EFFECTTYPE_RIVER)
        {
            mTextEffectLabel.setText(R.string.volume);
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", (int)(mVol4 * 100)));
            mSeekEffectDetail.setMax(100);
            mSeekEffectDetail.setProgress((int)(mVol4 * 100));
        }
        else if(nEffect == EFFECTTYPE_WAR)
        {
            mTextEffectLabel.setText(R.string.volume);
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", (int)(mVol5 * 100)));
            mSeekEffectDetail.setMax(100);
            mSeekEffectDetail.setProgress((int)(mVol5 * 100));
        }
        else if(nEffect == EFFECTTYPE_FIRE)
        {
            mTextEffectLabel.setText(R.string.volume);
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", (int)(mVol6 * 100)));
            mSeekEffectDetail.setMax(100);
            mSeekEffectDetail.setProgress((int)(mVol6 * 100));
        }
        else if(nEffect == EFFECTTYPE_CONCERTHALL)
        {
            mTextEffectLabel.setText(R.string.volume);
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", (int)(mVol7 * 100)));
            mSeekEffectDetail.setMax(100);
            mSeekEffectDetail.setProgress((int)(mVol7 * 100));
        }

        if(nEffect == EFFECTTYPE_PAN)
        {
            mBtnEffectMinus.setImageResource(R.drawable.leftbutton);
            mBtnEffectMinus.setContentDescription(getString(R.string.changeLeft));
            mBtnEffectPlus.setImageResource(R.drawable.rightbutton);
            mBtnEffectPlus.setContentDescription(getString(R.string.changeRight));
        }
        else
        {
            mBtnEffectMinus.setImageResource(R.drawable.minusbutton);
            mBtnEffectMinus.setContentDescription(getString(R.string.minus));
            mBtnEffectPlus.setImageResource(R.drawable.plusbutton);
            mBtnEffectPlus.setContentDescription(getString(R.string.plus));
        }

        if(nEffect == EFFECTTYPE_INCREASESPEED || nEffect == EFFECTTYPE_DECREASESPEED) {
            mRelativeSliderEffectDatail.setVisibility(View.GONE);
            mRelativeRollerEffectDetail.setVisibility(View.VISIBLE);
        }
        else {
            mRelativeSliderEffectDatail.setVisibility(View.VISIBLE);
            mRelativeRollerEffectDetail.setVisibility(View.GONE);
            mSeekEffectDetail.setOnSeekBarChangeListener(this);
        }

        mRelativeEffectDetail.setVisibility(View.VISIBLE);
        mRelativeEffect.setVisibility(View.GONE);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch)
    {
        if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_PAN).getEffectName()))
        {
            float mPan = (progress - 100) / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", progress - 100));
            setPan(mPan);
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_FREQUENCY).getEffectName()))
        {
            double dProgress = (double)(progress + 1) / 10.0;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%.1f", dProgress));
            setFreq((float)dProgress);
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_METRONOME).getEffectName()))
        {
            mBpm = progress + 10;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", mBpm));
            applyEffect();
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_RECORDNOISE).getEffectName()))
        {
            mVol1 = progress / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", progress));
            applyEffect();
            if(mEffectItems.get(EFFECTTYPE_RECORDNOISE).isSelected())
            {
                int hSETemp = mSE1PlayingFlag ? mSEStream : mSEStream2;
                BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, mVol1);
            }
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_ROAROFWAVES).getEffectName()))
        {
            mVol2 = progress / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", progress));
            applyEffect();
            if(mEffectItems.get(EFFECTTYPE_ROAROFWAVES).isSelected())
            {
                int hSETemp = mSE1PlayingFlag ? mSEStream : mSEStream2;
                BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, mVol2);
            }
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_RAIN).getEffectName()))
        {
            mVol3 = progress / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", progress));
            applyEffect();
            if(mEffectItems.get(EFFECTTYPE_RAIN).isSelected())
            {
                int hSETemp = mSE1PlayingFlag ? mSEStream : mSEStream2;
                BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, mVol3);
            }
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_RIVER).getEffectName()))
        {
            mVol4 = progress / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", progress));
            applyEffect();
            if(mEffectItems.get(EFFECTTYPE_RIVER).isSelected())
            {
                int hSETemp = mSE1PlayingFlag ? mSEStream : mSEStream2;
                BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, mVol4);
            }
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_WAR).getEffectName()))
        {
            mVol5 = progress / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", progress));
            applyEffect();
            if(mEffectItems.get(EFFECTTYPE_WAR).isSelected())
            {
                int hSETemp = mSE1PlayingFlag ? mSEStream : mSEStream2;
                BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, mVol5);
            }
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_FIRE).getEffectName()))
        {
            mVol6 = progress / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", progress));
            applyEffect();
            if(mEffectItems.get(EFFECTTYPE_FIRE).isSelected())
            {
                int hSETemp = mSE1PlayingFlag ? mSEStream : mSEStream2;
                BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, mVol6);
            }
        }
        else if(mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_CONCERTHALL).getEffectName()))
        {
            mVol7 = progress / 100.0f;
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", progress));
            applyEffect();
            if(mEffectItems.get(EFFECTTYPE_CONCERTHALL).isSelected())
            {
                int hSETemp = mSE1PlayingFlag ? mSEStream : mSEStream2;
                BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, mVol7);
            }
        }
        mActivity.playlistFragment.updateSavingEffect();
    }

    private void setPan(float pan)
    {
        setPan(pan, true);
    }

    public void setPan(float pan, boolean bSave)
    {
        if(pan < -1.0f) pan = -1.0f;
        else if(pan > 1.0f) pan = 1.0f;
        mPan = pan;
        if(mEffectItems.get(EFFECTTYPE_PAN).isSelected() && MainActivity.sStream != 0)
        {
            if(mDspPan != 0)
            {
                BASS.BASS_ChannelRemoveDSP(MainActivity.sStream, mDspPan);
                mDspPan = 0;
            }
            mDspPan = BASS.BASS_ChannelSetDSP(MainActivity.sStream, panDSP, this, 0);
        }
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    private void setFreq(float freq)
    {
        setFreq(freq, true);
    }

    public void setFreq(float freq, boolean bSave)
    {
        if(freq < 0.1f) freq = 0.1f;
        else if(freq > 4.0f) freq = 4.0f;
        mFreq = freq;
        if(mEffectItems.get(EFFECTTYPE_FREQUENCY).isSelected() && MainActivity.sStream != 0)
        {
            BASS.BASS_CHANNELINFO info = new BASS.BASS_CHANNELINFO();
            BASS.BASS_ChannelGetInfo(MainActivity.sStream, info);
            BASS.BASS_ChannelSetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, info.freq * mFreq);
        }
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {

    }

    private void checkDuplicate(int nSelect)
    {
        if(nSelect == 0)
        {
            for(int i = 1; i < mEffectItems.size(); i++)
                deselectEffect(i);
        }
        else
        {
            deselectEffect(0);
            if(EFFECTTYPE_VOCALCANCEL <= nSelect && nSelect <= EFFECTTYPE_TRANSCRIBESIDEGUITAR)
            {
                for (int i = EFFECTTYPE_VOCALCANCEL; i <= EFFECTTYPE_TRANSCRIBESIDEGUITAR; i++)
                    if(i != nSelect) deselectEffect(i);
            }
            if(nSelect == EFFECTTYPE_RANDOM || nSelect == EFFECTTYPE_TRANSCRIBESIDEGUITAR || nSelect == EFFECTTYPE_TRANSCRIBEBASS || nSelect == EFFECTTYPE_OLDRECORD || nSelect == EFFECTTYPE_LOWBATTERY || nSelect == EFFECTTYPE_EARTRAINING || nSelect == EFFECTTYPE_NOSENSE_STRONG || nSelect == EFFECTTYPE_NOSENSE_MIDDLE || nSelect == EFFECTTYPE_NOSENSE_WEAK) {
                if(nSelect != EFFECTTYPE_RANDOM) deselectEffect(EFFECTTYPE_RANDOM);
                if(nSelect != EFFECTTYPE_TRANSCRIBESIDEGUITAR) deselectEffect(EFFECTTYPE_TRANSCRIBESIDEGUITAR);
                if (nSelect != EFFECTTYPE_TRANSCRIBEBASS) deselectEffect(EFFECTTYPE_TRANSCRIBEBASS);
                if (nSelect != EFFECTTYPE_OLDRECORD) deselectEffect(EFFECTTYPE_OLDRECORD);
                if (nSelect != EFFECTTYPE_LOWBATTERY) deselectEffect(EFFECTTYPE_LOWBATTERY);
                if (nSelect != EFFECTTYPE_EARTRAINING) deselectEffect(EFFECTTYPE_EARTRAINING);
                if (nSelect != EFFECTTYPE_NOSENSE_STRONG) deselectEffect(EFFECTTYPE_NOSENSE_STRONG);
                if (nSelect != EFFECTTYPE_NOSENSE_MIDDLE) deselectEffect(EFFECTTYPE_NOSENSE_MIDDLE);
                if (nSelect != EFFECTTYPE_NOSENSE_WEAK) deselectEffect(EFFECTTYPE_NOSENSE_WEAK);
            }
            if(EFFECTTYPE_ECHO_STADIUM <= nSelect && nSelect <= EFFECTTYPE_ECHO_MOUNTAIN)
            {
                for(int i = EFFECTTYPE_ECHO_STADIUM; i <= EFFECTTYPE_ECHO_MOUNTAIN; i++)
                    if(i != nSelect) deselectEffect(i);
            }
            if(EFFECTTYPE_REVERB_BATHROOM <= nSelect && nSelect <= EFFECTTYPE_REVERB_CATHEDRAL)
            {
                for(int i = EFFECTTYPE_REVERB_BATHROOM; i <= EFFECTTYPE_REVERB_CATHEDRAL; i++)
                    if(i != nSelect) deselectEffect(i);
            }
            if(EFFECTTYPE_CHORUS <= nSelect && nSelect <= EFFECTTYPE_FLANGER)
            {
                for(int i = EFFECTTYPE_CHORUS; i <= EFFECTTYPE_FLANGER; i++)
                    if(i != nSelect) deselectEffect(i);
            }
            if((EFFECTTYPE_DISTORTION_STRONG <= nSelect && nSelect <= EFFECTTYPE_DISTORTION_WEAK) || nSelect == EFFECTTYPE_LOWBATTERY)
            {
                for(int i = EFFECTTYPE_DISTORTION_STRONG; i <= EFFECTTYPE_DISTORTION_WEAK; i++)
                    if(i != nSelect) deselectEffect(i);
                if(nSelect != EFFECTTYPE_LOWBATTERY) deselectEffect(EFFECTTYPE_LOWBATTERY);
            }
            if(EFFECTTYPE_INCREASESPEED <= nSelect && nSelect <= EFFECTTYPE_METRONOME)
            {
                for(int i = EFFECTTYPE_INCREASESPEED; i <= EFFECTTYPE_METRONOME; i++)
                    if(i != nSelect) deselectEffect(i);
            }
            if(nSelect == EFFECTTYPE_OLDRECORD || (EFFECTTYPE_METRONOME <= nSelect && nSelect <= EFFECTTYPE_CONCERTHALL))
            {
                if(nSelect != EFFECTTYPE_OLDRECORD) deselectEffect(EFFECTTYPE_OLDRECORD);
                for(int i = EFFECTTYPE_METRONOME; i <= EFFECTTYPE_CONCERTHALL; i++)
                    if(i != nSelect) deselectEffect(i);
            }
        }
        mActivity.playlistFragment.updateSavingEffect();
    }

    private void deselectEffect(int nEffect)
    {
        if(!mEffectItems.get(nEffect).isSelected()) return;

        mEffectItems.get(nEffect).setSelected(false);

        if(nEffect == EFFECTTYPE_RANDOM || nEffect == EFFECTTYPE_TRANSCRIBESIDEGUITAR || nEffect == EFFECTTYPE_OLDRECORD || nEffect == EFFECTTYPE_LOWBATTERY || nEffect == EFFECTTYPE_EARTRAINING)
            mActivity.equalizerFragment.setEQ(0);
        if(nEffect == EFFECTTYPE_RANDOM || nEffect == EFFECTTYPE_NOSENSE_STRONG || nEffect == EFFECTTYPE_NOSENSE_MIDDLE || nEffect == EFFECTTYPE_NOSENSE_WEAK)
        {
            mActivity.controlFragment.setSpeed(0.0f);
            mActivity.controlFragment.setPitch(0.0f);
        }
        if(nEffect == EFFECTTYPE_TRANSCRIBEBASS)
        {
            mActivity.equalizerFragment.setEQ(0);
            mActivity.controlFragment.setPitch(0.0f);
        }
    }

    public void applyEffect()
    {
        int nPlayingPlaylist = mActivity.playlistFragment.getPlayingPlaylist();
        if(nPlayingPlaylist < 0 || nPlayingPlaylist >= mActivity.playlistFragment.getArPlaylists().size()) return;
        ArrayList<SongItem> arSongs = mActivity.playlistFragment.getArPlaylists().get(nPlayingPlaylist);
        int nPlaying = mActivity.playlistFragment.getPlaying();
        if(nPlaying < 0 || nPlaying >= arSongs.size()) return;
        SongItem song = arSongs.get(nPlaying);
        applyEffect(MainActivity.sStream, song);
    }

    public void applyEffect(int sStream, SongItem song)
    {
        if(mDspVocalCancel != 0)
        {
            BASS.BASS_ChannelRemoveDSP(sStream, mDspVocalCancel);
            mDspVocalCancel = 0;
        }
        if(mDspMonoral != 0)
        {
            BASS.BASS_ChannelRemoveDSP(sStream, mDspMonoral);
            mDspMonoral = 0;
        }
        if(mDspLeft != 0)
        {
            BASS.BASS_ChannelRemoveDSP(sStream, mDspLeft);
            mDspLeft = 0;
        }
        if(mDspRight != 0)
        {
            BASS.BASS_ChannelRemoveDSP(sStream, mDspRight);
            mDspRight = 0;
        }
        if(mDspExchange != 0)
        {
            BASS.BASS_ChannelRemoveDSP(sStream, mDspExchange);
            mDspExchange = 0;
        }
        if(mDspDoubling != 0)
        {
            BASS.BASS_ChannelRemoveDSP(sStream, mDspDoubling);
            mDspDoubling = 0;
        }
        if(mDspPan != 0)
        {
            BASS.BASS_ChannelRemoveDSP(sStream, mDspPan);
            mDspPan = 0;
        }
        if(mDspNormalize != 0)
        {
            BASS.BASS_ChannelRemoveDSP(sStream, mDspNormalize);
            mDspNormalize = 0;
        }
        if(mFxCompressor != 0)
        {
            BASS.BASS_ChannelRemoveFX(sStream, mFxCompressor);
            mFxCompressor = 0;
        }
        if(mDspPhaseReversal != 0)
        {
            BASS.BASS_ChannelRemoveDSP(sStream, mDspPhaseReversal);
            mDspPhaseReversal = 0;
        }
        BASS.BASS_CHANNELINFO info = new BASS.BASS_CHANNELINFO();
        BASS.BASS_ChannelGetInfo(sStream, info);
        BASS.BASS_ChannelSetAttribute(sStream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, info.freq);
        if(mFxEcho != 0)
        {
            BASS.BASS_ChannelRemoveFX(sStream, mFxEcho);
            mFxEcho = 0;
        }
        if(mFxReverb != 0)
        {
            BASS.BASS_ChannelRemoveFX(sStream, mFxReverb);
            mFxReverb = 0;
        }
        if(mFxChorus != 0)
        {
            BASS.BASS_ChannelRemoveFX(sStream, mFxChorus);
            mFxChorus = 0;
        }
        if(mFxDistortion != 0)
        {
            BASS.BASS_ChannelRemoveFX(sStream, mFxDistortion);
            mFxDistortion = 0;
        }
        if(mTimer != null)
        {
            mTimer.cancel();
            mTimer = null;
        }
        for(int i = 0; i < mEffectItems.size(); i++)
        {
            if(!mEffectItems.get(i).isSelected())
                continue;
            String strEffect = mEffectItems.get(i).getEffectName();
            BASS.BASS_DX8_ECHO echo;
            BASS_FX.BASS_BFX_FREEVERB reverb;
            BASS_FX.BASS_BFX_CHORUS chorus;
            BASS_FX.BASS_BFX_DISTORTION distortion;
            int[] array;
            if(strEffect.equals(getString(R.string.random))) {
                float fMaxSpeed = 1.5f;
                float fMinSpeed = 0.75f;
                fMaxSpeed = (fMaxSpeed - 1.0f) * 100.0f;
                fMinSpeed = (1.0f - fMinSpeed) * -100.0f;
                Random random = new Random();
                float fRand = random.nextFloat();
                float fSpeed = (fRand * (fMaxSpeed - fMinSpeed) * 10.0f) / 10.0f + fMinSpeed;
                mActivity.controlFragment.setSpeed(fSpeed);
                float fMaxPitch = 3.0f;
                float fMinPitch = -3.0f;
                fRand = random.nextFloat();
                float fPitch = (fRand * (fMaxPitch - fMinPitch) * 10.0f) / 10.0f + fMinPitch;
                mActivity.controlFragment.setPitch(fPitch);
                mActivity.equalizerFragment.setEQRandom();
            }
            else if(strEffect.equals(getString(R.string.vocalCancel))) {
                if (info.chans != 1)
                    mDspVocalCancel = BASS.BASS_ChannelSetDSP(sStream, vocalCancelDSP, null, 0);
            }
            else if(strEffect.equals(getString(R.string.monoral))) {
                if (info.chans != 1)
                    mDspMonoral = BASS.BASS_ChannelSetDSP(sStream, monoralDSP, null, 0);
            }
            else if(strEffect.equals(getString(R.string.leftOnly))) {
                if (info.chans != 1)
                    mDspLeft = BASS.BASS_ChannelSetDSP(sStream, leftDSP, null, 0);
            }
            else if(strEffect.equals(getString(R.string.rightOnly))) {
                if (info.chans != 1)
                    mDspRight = BASS.BASS_ChannelSetDSP(sStream, rightDSP, null, 0);
            }
            else if(strEffect.equals(getString(R.string.leftAndRightReplace))) {
                if (info.chans != 1)
                    mDspExchange = BASS.BASS_ChannelSetDSP(sStream, exchangeDSP, null, 0);
            }
            else if(strEffect.equals(getString(R.string.doubling))) {
                if (info.chans != 1) {
                    for (int j = 0; j < ECHBUFLEN; j++) {
                        echbuf[j][0] = 0;
                        echbuf[j][1] = 0;
                    }
                    echpos = 0;
                    mDspDoubling = BASS.BASS_ChannelSetDSP(sStream, doublingDSP, null, 0);
                }
            }
            else if(strEffect.equals(getString(R.string.transcribeSideGuitar))) {
                if (info.chans != 1)
                    mDspVocalCancel = BASS.BASS_ChannelSetDSP(sStream, vocalCancelDSP, null, 0);
                array = new int[]{0, -30, -20, -12, -7, -4, -3, -2, -1, 0, 0, 0, 0, 0, -1, -2, -3, -4, -7, -12, -20, -24, -27, -28, -29, -30, -30, -30, -30, -30, -30, -30};
                for (int j = 0; j < 32; j++) {
                    int nLevel = array[j];
                    if (j == 0)
                        mActivity.equalizerFragment.setVol(nLevel);
                    else
                        mActivity.equalizerFragment.setEQ(j, nLevel);
                }
            }
            else if(strEffect.equals(getString(R.string.transcribeBassOctave))) {
                mActivity.controlFragment.setLink(false);
                mActivity.controlFragment.setPitch(12.0f);
                array = new int[]{0, -30, -30, -30, -30, -30, -30, -30, -30, -30, -30, -20, -10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                for (int j = 0; j < 32; j++) {
                    int nLevel = array[j];
                    if (j == 0)
                        mActivity.equalizerFragment.setVol(nLevel);
                    else
                        mActivity.equalizerFragment.setEQ(j, nLevel);
                }
            }
            else if(strEffect.equals(getString(R.string.pan))) {
                if (info.chans != 1)
                    mDspPan = BASS.BASS_ChannelSetDSP(sStream, panDSP, this, 0);
            }
            else if(strEffect.equals(getString(R.string.normalize))) {
                if (song.getPeak() == 0.0f) {
                    if (sStream != MainActivity.sStream) getPeak(song);
                    else mPeak = 1.0f;
                } else mPeak = song.getPeak();
                mDspNormalize = BASS.BASS_ChannelSetDSP(sStream, normalizeDSP, this, 0);
            }
            else if(strEffect.equals(getString(R.string.compressor))) {
                mFxCompressor = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_COMPRESSOR2, 2);
                BASS_FX.BASS_BFX_COMPRESSOR2 p = new BASS_FX.BASS_BFX_COMPRESSOR2();
                p.fGain = 2.0f;
                p.fThreshold = -20.0f;
                p.fRatio = 10.0f;
                p.fAttack = 1.2f;
                p.fRelease = 400.0f;
                p.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxCompressor, p);
            }
            else if(strEffect.equals(getString(R.string.frequency)))
                BASS.BASS_ChannelSetAttribute(sStream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, info.freq * mFreq);
            else if(strEffect.equals(getString(R.string.phaseReversal)))
                mDspPhaseReversal = BASS.BASS_ChannelSetDSP(sStream, phaseReversalDSP, null, 0);
            else if(strEffect.equals(getString(R.string.studiumEcho))) {
                mFxEcho = BASS.BASS_ChannelSetFX(sStream, BASS.BASS_FX_DX8_ECHO, 2);
                echo = new BASS.BASS_DX8_ECHO();
                echo.fFeedback = (float) 55.0;
                echo.fLeftDelay = (float) 400.0;
                echo.fRightDelay = (float) 400.0;
                echo.fWetDryMix = (float) 10.0;
                echo.lPanDelay = FALSE;
                BASS.BASS_FXSetParameters(mFxEcho, echo);
            }
            else if(strEffect.equals(getString(R.string.hallEcho))) {
                mFxEcho = BASS.BASS_ChannelSetFX(sStream, BASS.BASS_FX_DX8_ECHO, 2);
                echo = new BASS.BASS_DX8_ECHO();
                echo.fFeedback = (float) 50.0;
                echo.fLeftDelay = (float) 300.0;
                echo.fRightDelay = (float) 300.0;
                echo.fWetDryMix = (float) 10.0;
                echo.lPanDelay = FALSE;
                BASS.BASS_FXSetParameters(mFxEcho, echo);
            }
            else if(strEffect.equals(getString(R.string.livehouseEcho))) {
                mFxEcho = BASS.BASS_ChannelSetFX(sStream, BASS.BASS_FX_DX8_ECHO, 2);
                echo = new BASS.BASS_DX8_ECHO();
                echo.fFeedback = (float) 30.0;
                echo.fLeftDelay = (float) 200.0;
                echo.fRightDelay = (float) 200.0;
                echo.fWetDryMix = (float) 11.1;
                echo.lPanDelay = FALSE;
                BASS.BASS_FXSetParameters(mFxEcho, echo);
            }
            else if(strEffect.equals(getString(R.string.roomEcho))) {
                mFxEcho = BASS.BASS_ChannelSetFX(sStream, BASS.BASS_FX_DX8_ECHO, 2);
                echo = new BASS.BASS_DX8_ECHO();
                echo.fFeedback = (float) 50.0;
                echo.fLeftDelay = (float) 100.0;
                echo.fRightDelay = (float) 100.0;
                echo.fWetDryMix = (float) 13.0;
                echo.lPanDelay = FALSE;
                BASS.BASS_FXSetParameters(mFxEcho, echo);
            }
            else if(strEffect.equals(getString(R.string.bathroomEcho))) {
                mFxEcho = BASS.BASS_ChannelSetFX(sStream, BASS.BASS_FX_DX8_ECHO, 2);
                echo = new BASS.BASS_DX8_ECHO();
                echo.fFeedback = (float) 60.0;
                echo.fLeftDelay = (float) 75.0;
                echo.fRightDelay = (float) 75.0;
                echo.fWetDryMix = (float) 23.0;
                echo.lPanDelay = FALSE;
                BASS.BASS_FXSetParameters(mFxEcho, echo);
            }
            else if(strEffect.equals(getString(R.string.vocalEcho))) {
                mFxEcho = BASS.BASS_ChannelSetFX(sStream, BASS.BASS_FX_DX8_ECHO, 2);
                echo = new BASS.BASS_DX8_ECHO();
                echo.fFeedback = (float) 40.0;
                echo.fLeftDelay = (float) 350.0;
                echo.fRightDelay = (float) 350.0;
                echo.fWetDryMix = (float) 13.0;
                echo.lPanDelay = FALSE;
                BASS.BASS_FXSetParameters(mFxEcho, echo);
            }
            else if(strEffect.equals(getString(R.string.mountainEcho))) {
                mFxEcho = BASS.BASS_ChannelSetFX(sStream, BASS.BASS_FX_DX8_ECHO, 2);
                echo = new BASS.BASS_DX8_ECHO();
                echo.fFeedback = (float) 0.0;
                echo.fLeftDelay = (float) 1000.0;
                echo.fRightDelay = (float) 1000.0;
                echo.fWetDryMix = (float) 16.6;
                echo.lPanDelay = FALSE;
                BASS.BASS_FXSetParameters(mFxEcho, echo);
            }
            else if(strEffect.equals(getString(R.string.reverbBathroom))) {
                mFxReverb = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_FREEVERB, 2);
                reverb = new BASS_FX.BASS_BFX_FREEVERB();
                reverb.fDryMix = (float) 1.0;
                reverb.fWetMix = (float) 2.0;
                reverb.fRoomSize = (float) 0.16;
                reverb.fDamp = (float) 0.5;
                reverb.fWidth = (float) 1.0;
                reverb.lMode = 0;
                reverb.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxReverb, reverb);
            }
            else if(strEffect.equals(getString(R.string.reverbSmallRoom))) {
                mFxReverb = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_FREEVERB, 2);
                reverb = new BASS_FX.BASS_BFX_FREEVERB();
                reverb.fDryMix = (float) 0.95;
                reverb.fWetMix = (float) 0.995;
                reverb.fRoomSize = (float) 0.3;
                reverb.fDamp = (float) 0.5;
                reverb.fWidth = (float) 1.0;
                reverb.lMode = 0;
                reverb.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxReverb, reverb);
            }
            else if(strEffect.equals(getString(R.string.reverbMediumRoom))) {
                mFxReverb = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_FREEVERB, 2);
                reverb = new BASS_FX.BASS_BFX_FREEVERB();
                reverb.fDryMix = (float) 0.95;
                reverb.fWetMix = (float) 0.995;
                reverb.fRoomSize = (float) 0.75;
                reverb.fDamp = (float) 0.5;
                reverb.fWidth = (float) 0.7;
                reverb.lMode = 0;
                reverb.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxReverb, reverb);
            }
            else if(strEffect.equals(getString(R.string.reverbLargeRoom))) {
                mFxReverb = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_FREEVERB, 2);
                reverb = new BASS_FX.BASS_BFX_FREEVERB();
                reverb.fDryMix = (float) 0.7;
                reverb.fWetMix = (float) 1.0;
                reverb.fRoomSize = (float) 0.85;
                reverb.fDamp = (float) 0.5;
                reverb.fWidth = (float) 0.9;
                reverb.lMode = 0;
                reverb.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxReverb, reverb);
            }
            else if(strEffect.equals(getString(R.string.reverbChurch))) {
                mFxReverb = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_FREEVERB, 2);
                reverb = new BASS_FX.BASS_BFX_FREEVERB();
                reverb.fDryMix = (float) 0.4;
                reverb.fWetMix = (float) 1.0;
                reverb.fRoomSize = (float) 0.9;
                reverb.fDamp = (float) 0.5;
                reverb.fWidth = (float) 1.0;
                reverb.lMode = 0;
                reverb.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxReverb, reverb);
            }
            else if(strEffect.equals(getString(R.string.reverbCathedral))) {
                mFxReverb = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_FREEVERB, 2);
                reverb = new BASS_FX.BASS_BFX_FREEVERB();
                reverb.fDryMix = (float) 0.0;
                reverb.fWetMix = (float) 1.0;
                reverb.fRoomSize = (float) 0.9;
                reverb.fDamp = (float) 0.5;
                reverb.fWidth = (float) 1.0;
                reverb.lMode = 0;
                reverb.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxReverb, reverb);
            }
            else if(strEffect.equals(getString(R.string.chorus))) {
                mFxChorus = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_CHORUS, 2);
                chorus = new BASS_FX.BASS_BFX_CHORUS();
                chorus.fDryMix = (float) 0.5;
                chorus.fWetMix = (float) 0.2;
                chorus.fFeedback = (float) 0.5;
                chorus.fMinSweep = (float) 1.0;
                chorus.fMaxSweep = (float) 2.0;
                chorus.fRate = (float) 10.0;
                chorus.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxChorus, chorus);
            }
            else if(strEffect.equals(getString(R.string.flanger))) {
                mFxChorus = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_CHORUS, 2);
                chorus = new BASS_FX.BASS_BFX_CHORUS();
                chorus.fDryMix = (float) 0.25;
                chorus.fWetMix = (float) 0.4;
                chorus.fFeedback = (float) 0.5;
                chorus.fMinSweep = (float) 1.0;
                chorus.fMaxSweep = (float) 5.0;
                chorus.fRate = (float) 1.0;
                chorus.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxChorus, chorus);
            }
            else if(strEffect.equals(getString(R.string.distortionStrong))) {
                mFxDistortion = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_DISTORTION, 2);
                distortion = new BASS_FX.BASS_BFX_DISTORTION();
                distortion.fDrive = (float) 0.2;
                distortion.fDryMix = (float) 0.96;
                distortion.fWetMix = (float) 0.03;
                distortion.fFeedback = (float) 0.1;
                distortion.fVolume = (float) 1.0;
                distortion.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxDistortion, distortion);
            }
            else if(strEffect.equals(getString(R.string.distortionMiddle))) {
                mFxDistortion = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_DISTORTION, 2);
                distortion = new BASS_FX.BASS_BFX_DISTORTION();
                distortion.fDrive = (float) 0.2;
                distortion.fDryMix = (float) 0.97;
                distortion.fWetMix = (float) 0.02;
                distortion.fFeedback = (float) 0.1;
                distortion.fVolume = (float) 1.0;
                distortion.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxDistortion, distortion);
            }
            else if(strEffect.equals(getString(R.string.distortionWeak))) {
                mFxDistortion = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_DISTORTION, 2);
                distortion = new BASS_FX.BASS_BFX_DISTORTION();
                distortion.fDrive = (float) 0.2;
                distortion.fDryMix = (float) 0.98;
                distortion.fWetMix = (float) 0.01;
                distortion.fFeedback = (float) 0.1;
                distortion.fVolume = (float) 1.0;
                distortion.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxDistortion, distortion);
            }
            else if(strEffect.equals(getString(R.string.reverse))) {
                if (sStream != 0) {
                    int chan = BASS_FX.BASS_FX_TempoGetSource(sStream);
                    BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_REVERSE);
                    mActivity.setSync();
                }
            }
            else if(strEffect.equals(getString(R.string.increaseSpeed))) {
                if (mHandler != null) {
                    mHandler.removeCallbacks(onTimer);
                    mHandler = null;
                }
                mHandler = new Handler();
                mHandler.post(onTimer);
            }
            else if(strEffect.equals(getString(R.string.decreaseSpeed))) {
                if (mHandler != null) {
                    mHandler.removeCallbacks(onTimer);
                    mHandler = null;
                }
                mHandler = new Handler();
                mHandler.post(onTimer);
            }
            else if(strEffect.equals(getString(R.string.oldRecord))) {
                array = new int[]{2, -12, -12, -12, -12, -12, -12, -12, -12, -12, -6, 0, 0, 0, 0, 0, 0, 0, 0, 0, -6, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12};
                for (int j = 0; j < 32; j++) {
                    int nLevel = array[j];
                    if (j == 0)
                        mActivity.equalizerFragment.setVol(nLevel);
                    else
                        mActivity.equalizerFragment.setEQ(j, nLevel);
                }
                if (mSEStream == 0) {
                    mSE1PlayingFlag = true;
                    InputStream is = getResources().openRawResource(R.raw.recordnoise);
                    mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
                    mSync = BASS.BASS_ChannelSetSync(mSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 4.653), endRecordNoise, this);
                    BASS.BASS_ChannelSetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, mVol1);
                    BASS.BASS_ChannelPlay(mSEStream, true);
                }

                mHandler = new Handler();
                mHandler.post(onTimer);
            }
            else if(strEffect.equals(getString(R.string.lowBattery))) {
                mFxDistortion = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_DISTORTION, 2);
                distortion = new BASS_FX.BASS_BFX_DISTORTION();
                distortion.fDrive = (float) 0.2;
                distortion.fDryMix = (float) 0.9;
                distortion.fWetMix = (float) 0.1;
                distortion.fFeedback = (float) 0.1;
                distortion.fVolume = (float) 1.0;
                distortion.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxDistortion, distortion);

                array = new int[]{2, -12, -12, -12, -12, -12, -12, -12, -12, -12, -6, 0, 0, 0, 0, 0, 0, 0, 0, 0, -6, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12};
                for (int j = 0; j < 32; j++) {
                    int nLevel = array[j];
                    if (j == 0)
                        mActivity.equalizerFragment.setVol(nLevel);
                    else
                        mActivity.equalizerFragment.setEQ(j, nLevel);
                }

                mHandler = new Handler();
                mHandler.post(onTimer);
            }
            else if(strEffect.equals(getString(R.string.noSenseStrong)) || strEffect.equals(getString(R.string.noSenseMiddle)) || strEffect.equals(getString(R.string.noSenseWeak))) {
                mVelo1 = mVol2 = 0.0f;
                mActivity.controlFragment.setSpeed(0.0f);
                mActivity.controlFragment.setPitch(0.0f);

                mHandler = new Handler();
                mHandler.post(onTimer);
            }
            else if(strEffect.equals(getString(R.string.earTraining))) {
                mHandler = new Handler();
                mHandler.post(onTimer);
            }
            else if(strEffect.equals(getString(R.string.metronome))) {
                mTimer = new Timer();
                MetronomeTask metronomeTask = new MetronomeTask(this);
                long lPeriod = (long) ((60.0 / mBpm) * 1000);
                mTimer.schedule(metronomeTask, lPeriod, lPeriod);
            }
            else if(strEffect.equals(getString(R.string.recordNoise))) {
                if (mSEStream == 0) {
                    mSE1PlayingFlag = true;
                    InputStream is = getResources().openRawResource(R.raw.recordnoise);
                    mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
                    mSync = BASS.BASS_ChannelSetSync(mSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 4.653), endRecordNoise, this);
                    BASS.BASS_ChannelSetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, mVol1);
                    BASS.BASS_ChannelPlay(mSEStream, true);
                }
            }
            else if(strEffect.equals(getString(R.string.wave))) {
                if (mSEStream == 0) {
                    mSE1PlayingFlag = true;
                    InputStream is = getResources().openRawResource(R.raw.wave);
                    mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
                    mSync = BASS.BASS_ChannelSetSync(mSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 38.399), endWave, this);
                    BASS.BASS_ChannelSetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, mVol2);
                    BASS.BASS_ChannelPlay(mSEStream, true);
                }
            }
            else if(strEffect.equals(getString(R.string.rain))) {
                if (mSEStream == 0) {
                    mSE1PlayingFlag = true;
                    InputStream is = getResources().openRawResource(R.raw.rain);
                    mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
                    mSync = BASS.BASS_ChannelSetSync(mSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 1.503), endRain, this);
                    BASS.BASS_ChannelSetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, mVol3);
                    BASS.BASS_ChannelPlay(mSEStream, true);
                }
            }
            else if(strEffect.equals(getString(R.string.river))) {
                if (mSEStream == 0) {
                    mSE1PlayingFlag = true;
                    InputStream is = getResources().openRawResource(R.raw.river);
                    mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
                    mSync = BASS.BASS_ChannelSetSync(mSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 60.000), endRiver, this);
                    BASS.BASS_ChannelSetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, mVol4);
                    BASS.BASS_ChannelPlay(mSEStream, true);
                }
            }
            else if(strEffect.equals(getString(R.string.war))) {
                if (mSEStream == 0) {
                    mSE1PlayingFlag = true;
                    InputStream is = getResources().openRawResource(R.raw.war);
                    mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
                    mSync = BASS.BASS_ChannelSetSync(mSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 30.000), endWar, this);
                    BASS.BASS_ChannelSetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, mVol5);
                    BASS.BASS_ChannelPlay(mSEStream, true);
                }
            }
            else if(strEffect.equals(getString(R.string.fire))) {
                if (mSEStream == 0) {
                    mSE1PlayingFlag = true;
                    InputStream is = getResources().openRawResource(R.raw.fire);
                    mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
                    mSync = BASS.BASS_ChannelSetSync(mSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 90.000), endFire, this);
                    BASS.BASS_ChannelSetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, mVol6);
                    BASS.BASS_ChannelPlay(mSEStream, true);
                }
            }
            else if(strEffect.equals(getString(R.string.concertHall))) {
                if (mSEStream == 0) {
                    mSE1PlayingFlag = true;
                    InputStream is = getResources().openRawResource(R.raw.cheer);
                    mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
                    mSync = BASS.BASS_ChannelSetSync(mSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 14.000), endCheer, this);
                    BASS.BASS_ChannelSetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, mVol7);

                    mHandler = new Handler();
                    mHandler.post(onTimer);
                }
            }
        }
    }

    private void getPeak(SongItem song)
    {
        if(song == null) return;
        File file = new File(song.getPath());
        int hTempStream = 0;
        if(file.getParent().equals(mActivity.getFilesDir().toString()))
            hTempStream = BASS.BASS_StreamCreateFile(song.getPath(), 0, 0, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
        else
        {
            BASS.BASS_FILEPROCS fileprocs=new BASS.BASS_FILEPROCS() {
                @Override
                public boolean FILESEEKPROC(long offset, Object user) {
                    FileChannel fc=(FileChannel)user;
                    try {
                        fc.position(offset);
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return false;
                }

                @Override
                public int FILEREADPROC(ByteBuffer buffer, int length, Object user) {
                    FileChannel fc=(FileChannel)user;
                    try {
                        return fc.read(buffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }

                @Override
                public long FILELENPROC(Object user) {
                    FileChannel fc=(FileChannel)user;
                    try {
                        return fc.size();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }

                @Override
                public void FILECLOSEPROC(Object user) {
                    FileChannel fc=(FileChannel)user;
                    try {
                        fc.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            try {
                mmr.setDataSource(getContext(), Uri.parse(song.getPath()));
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            ContentResolver cr = mActivity.getContentResolver();
            try {
                AssetFileDescriptor afd = cr.openAssetFileDescriptor(Uri.parse(song.getPath()), "r");
                if(afd != null) {
                    FileChannel fc = afd.createInputStream().getChannel();
                    hTempStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE, fileprocs, fc);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        BASS.BASS_CHANNELINFO info = new BASS.BASS_CHANNELINFO();
        BASS.BASS_ChannelGetInfo(hTempStream, info);
        boolean bStereo = true;
        if(info.chans == 1) bStereo = false;
        float fTempPeak = 0.0f;
        float[] arLevels = new float[2];
        if(bStereo) {
            while (BASS.BASS_ChannelGetLevelEx(hTempStream, arLevels, 0.1f, BASS.BASS_LEVEL_STEREO)) {
                if (fTempPeak < arLevels[0]) fTempPeak = arLevels[0];
                if (fTempPeak < arLevels[1]) fTempPeak = arLevels[1];
            }
        }
        else {
            while (BASS.BASS_ChannelGetLevelEx(hTempStream, arLevels, 0.1f, BASS.BASS_LEVEL_MONO)) {
                if (fTempPeak < arLevels[0]) fTempPeak = arLevels[0];
            }
        }
        mPeak = fTempPeak;
    }

    private final Runnable onTimer = new Runnable()
    {
        @Override
        public void run()
        {
            if(mEffectItems.get(EFFECTTYPE_INCREASESPEED).isSelected())
            {
                Float fSpeed = 0.0f;
                BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO, fSpeed);
                fSpeed += mIncreaseSpeed;
                if(fSpeed + 100.0f > 400.0f) fSpeed = 300.0f;
                if(MainActivity.sStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PAUSED)
                    mActivity.controlFragment.setSpeed(fSpeed, false);
                mHandler.postDelayed(this, (long)(mTimeOfIncreaseSpeed * 1000.0f));
            }
            else if(mEffectItems.get(EFFECTTYPE_DECREASESPEED).isSelected())
            {
                Float fSpeed = 0.0f;
                BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO, fSpeed);
                fSpeed -= mDecreaseSpeed;
                if(fSpeed + 100.0f < 10.0f) fSpeed = -90.0f;
                if(MainActivity.sStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PAUSED)
                    mActivity.controlFragment.setSpeed(fSpeed, false);
                mHandler.postDelayed(this, (long)(mTimeOfIncreaseSpeed * 1000.0f));
            }
            else if(mEffectItems.get(EFFECTTYPE_OLDRECORD).isSelected())
            {
                Float mFreq = 0.0f;
                BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS.BASS_ATTRIB_FREQ, mFreq);
                Float fTempoFreq = 0.0f;
                BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, fTempoFreq);
                fTempoFreq = fTempoFreq * 100.0f / mFreq;
                // 加速度の設定
                // 周波数が98以上の場合 : -0.1
                // 　　　　98未満の場合 : +0.1
                float mAccel = fTempoFreq >= 98.0f ? -0.1f : 0.1f;

                // 周波数の差分に加速度を加える
                mVelo1 += mAccel;

                // 周波数に差分を加える
                fTempoFreq += mVelo1;

                if(fTempoFreq <= 90.0) fTempoFreq = 90.0f;
                if(fTempoFreq >= 100.0) fTempoFreq = 100.0f;

                BASS.BASS_ChannelSetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, mFreq * fTempoFreq / 100.0f);
                mHandler.postDelayed(this, 750);
            }
            else if(mEffectItems.get(EFFECTTYPE_LOWBATTERY).isSelected())
            {
                Float mFreq = 0.0f;
                BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS.BASS_ATTRIB_FREQ, mFreq);
                Float fTempoFreq = 0.0f;
                BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, fTempoFreq);
                fTempoFreq = fTempoFreq * 100.0f / mFreq;
                // 加速度の設定
                // 周波数が68以上の場合 : -0.02
                // 　　　　68未満の場合 : +0.01
                float mAccel = fTempoFreq >= 68.0f ? -0.02f : 0.01f;

                // 周波数の差分に加速度を加える
                mVelo1 += mAccel;

                // 周波数に差分を加える
                fTempoFreq += mVelo1;

                if(fTempoFreq <= 65.0) fTempoFreq = 65.0f;
                if(fTempoFreq >= 70.0) fTempoFreq = 70.0f;

                BASS.BASS_ChannelSetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, mFreq * fTempoFreq / 100.0f);

                mHandler.postDelayed(this, 50);
            }
            else if(mEffectItems.get(EFFECTTYPE_NOSENSE_STRONG).isSelected() || mEffectItems.get(EFFECTTYPE_NOSENSE_MIDDLE).isSelected() || mEffectItems.get(EFFECTTYPE_NOSENSE_WEAK).isSelected()) {
                Float fSpeed = 0.0f;
                BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO, fSpeed);
                float mAccel;
                Random random = new Random();
                float fRand = random.nextFloat();
                if(mEffectItems.get(EFFECTTYPE_NOSENSE_STRONG).isSelected())
                {
                    mAccel = (fRand * 400.0f) / 10000.0f - 0.02f; // 加速度の設定
                    if(fSpeed < -20.0f) mAccel = 0.01f;
                    else if(fSpeed > 20.0f) mAccel = -0.01f;
                }
                else if(mEffectItems.get(EFFECTTYPE_NOSENSE_MIDDLE).isSelected())
                {
                    mAccel = (fRand * 200.0f) / 10000.0f - 0.01f; // 加速度の設定
                    if(fSpeed < -10.0f) mAccel = 0.01f;
                    else if(fSpeed > 10.0f) mAccel = -0.01f;
                }
                else
                {
                    mAccel = (fRand * 100.0f) / 10000.0f - 0.005f; // 加速度の設定
                    if(fSpeed < -5.0f) mAccel = 0.01f;
                    else if(fSpeed > 5.0f) mAccel = -0.01f;
                }
                mVelo1 += mAccel; // 速度の差分に加速度を加える
                fSpeed += mVelo1; // 速度に差分を加える
                if(MainActivity.sStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PAUSED)
                    mActivity.controlFragment.setSpeed(fSpeed);

                Float fPitch = 0.0f;
                fRand = random.nextFloat();
                BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO_PITCH, fPitch);
                if(mEffectItems.get(EFFECTTYPE_NOSENSE_STRONG).isSelected())
                {
                    mAccel = (fRand * 400.0f) / 10000.0f - 0.02f; // 加速度の設定
                    if(fPitch < -4.0f) mAccel = 0.01f;
                    else if(fPitch > 4.0f) mAccel = -0.01f;
                }
                else if(mEffectItems.get(EFFECTTYPE_NOSENSE_MIDDLE).isSelected())
                {
                    mAccel = (fRand * 200.0f) / 10000.0f - 0.01f; // 加速度の設定
                    if(fPitch < -2.0f) mAccel = 0.01f;
                    else if(fPitch > 2.0f) mAccel = -0.01f;
                }
                else
                {
                    mAccel = (fRand * 100.0f) / 10000.0f - 0.005f; // 加速度の設定
                    if(fPitch < -1.0f) mAccel = 0.01f;
                    else if(fPitch > 1.0f) mAccel = -0.01f;
                }
                mVelo2 += mAccel; // 音程の差分に加速度を加える
                fPitch += mVelo2; // 音程に差分を加える
                if(MainActivity.sStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PAUSED)
                    mActivity.controlFragment.setPitch(fPitch);
                mHandler.postDelayed(this, 80);
            }
            else if(mEffectItems.get(EFFECTTYPE_EARTRAINING).isSelected())
            {
                mActivity.equalizerFragment.setEQRandom();
                mHandler.postDelayed(this, 3000);
            }
            else if(mEffectItems.get(EFFECTTYPE_CONCERTHALL).isSelected())
            {
                int hSETemp = mSE1PlayingFlag ? mSEStream : mSEStream2;
                if(BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PLAYING)
                {
                    if(BASS.BASS_ChannelIsActive(hSETemp) == BASS.BASS_ACTIVE_PAUSED || BASS.BASS_ChannelIsActive(hSETemp) == BASS.BASS_ACTIVE_STOPPED)
                    {
                        BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, mVol7);
                        BASS.BASS_ChannelPlay(hSETemp, true);
                        mHandler.postDelayed(this, 100);
                        return;
                    }
                }
                if(MainActivity.sStream == 0 || BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_PAUSED || BASS.BASS_ChannelIsActive(MainActivity.sStream) == BASS.BASS_ACTIVE_STOPPED)
                {
                    if(BASS.BASS_ChannelIsActive(hSETemp) == BASS.BASS_ACTIVE_PLAYING)
                        BASS.BASS_ChannelPause(hSETemp);
                    mHandler.postDelayed(this, 100);
                    return;
                }
                if(BASS.BASS_ChannelIsActive(hSETemp) == BASS.BASS_ACTIVE_PAUSED)
                {
                    mHandler.postDelayed(this, 100);
                    return;
                }
                if(BASS.BASS_ChannelIsSliding(hSETemp, BASS.BASS_ATTRIB_VOL))
                {
                    mHandler.postDelayed(this, 100);
                    return;
                }
                double dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE));
                double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE));
                if(dLength - dPos < 5.0)
                {
                    BASS.BASS_ChannelSlideAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, 0.0f, 5000);
                    mHandler.postDelayed(this, 100);
                    return;
                }

                Float fVol = 0.0f;
                BASS.BASS_ChannelGetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, fVol);
                Random random = new Random();
                float fRand = random.nextFloat();
                fRand = (fRand / 200.0f) - 0.0025f;
                if(fVol > 1.0f - 0.01f) fRand = -0.0005f;
                else if(fVol <= 0.5f) fRand = 0.0005f;
                mAccel += fRand;
                fVol += mAccel;
                if(fVol > 1.0f) fVol = 1.0f;
                else if(fVol < 0.5f) fVol = 0.5f;
                BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, fVol * mVol7);
                mHandler.postDelayed(this, 100);
            }
        }
    };

    private final BASS.BASS_FILEPROCS fileprocs = new BASS.BASS_FILEPROCS()
    {
        @Override
        public boolean FILESEEKPROC(long offset, Object user)
        {
            return false;
        }

        @Override
        public int FILEREADPROC(ByteBuffer buffer, int length, Object user)
        { 
            if(length == 0)
                return 0;
            InputStream is=(InputStream)user;
            byte b[]=new byte[length];
            int r;
            try
            {
                r = is.read(b);
            }
            catch (Exception e)
            {
                return 0;
            }
            if (r <= 0) return 0;
            buffer.put(b, 0, r);
            return r;
        }

        @Override
        public long FILELENPROC(Object user)
        {
            return 0;
        }

        @Override
        public void FILECLOSEPROC(Object user)
        {
            InputStream is=(InputStream)user;
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    };

    private final BASS.SYNCPROC endRecordNoise = new BASS.SYNCPROC()
    {
        public void SYNCPROC(int handle, int channel, int data, final Object user)
        {
            EffectFragment effectFragment = (EffectFragment)user;
            effectFragment.onRecordNoiseEnded();
        }
    };

    private void onRecordNoiseEnded()
    {
        if(mSE1PlayingFlag)
        {
            InputStream is = getResources().openRawResource(R.raw.recordnoise);
            mSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(mSEStream2, BASS.BASS_ChannelSeconds2Bytes(mSEStream2, 1.417), BASS.BASS_POS_BYTE);
            if(mSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(mSEStream, mSync);
                mSync = 0;
            }
            mSync = BASS.BASS_ChannelSetSync(mSEStream2, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream2, 4.653), endRecordNoise, this);
            BASS.BASS_ChannelPlay(mSEStream2, FALSE);
            BASS.BASS_ChannelSlideAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            BASS.BASS_ChannelSlideAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, mVol1, 1000);
            mSE1PlayingFlag = false;
        }
        else if(BASS.BASS_ChannelIsActive(mSEStream2) == BASS.BASS_ACTIVE_PLAYING)
        {
            InputStream is = getResources().openRawResource(R.raw.recordnoise);
            mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(mSEStream, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 1.417), BASS.BASS_POS_BYTE);
            if(mSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(mSEStream2, mSync);
                mSync = 0;
            }
            mSync = BASS.BASS_ChannelSetSync(mSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 4.653), endRecordNoise, this);
            BASS.BASS_ChannelPlay(mSEStream, FALSE);
            BASS.BASS_ChannelSlideAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            BASS.BASS_ChannelSlideAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, mVol1, 1000);
            mSE1PlayingFlag = true;
        }
    }

    private final BASS.SYNCPROC endWave = new BASS.SYNCPROC()
    {
        public void SYNCPROC(int handle, int channel, int data, final Object user)
        {
            EffectFragment effectFragment = (EffectFragment)user;
            effectFragment.onWaveEnded();
        }
    };

    private void onWaveEnded()
    {
        if(mSE1PlayingFlag)
        {
            InputStream is = getResources().openRawResource(R.raw.wave);
            mSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(mSEStream2, BASS.BASS_ChannelSeconds2Bytes(mSEStream2, 0.283), BASS.BASS_POS_BYTE);
            if(mSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(mSEStream, mSync);
                mSync = 0;
            }
            mSync = BASS.BASS_ChannelSetSync(mSEStream2, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream2, 38.399), endWave, this);
            BASS.BASS_ChannelPlay(mSEStream2, FALSE);
            BASS.BASS_ChannelSlideAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            BASS.BASS_ChannelSlideAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, mVol2, 1000);
            mSE1PlayingFlag = false;
        }
        else if(BASS.BASS_ChannelIsActive(mSEStream2) == BASS.BASS_ACTIVE_PLAYING)
        {
            InputStream is = getResources().openRawResource(R.raw.wave);
            mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(mSEStream, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 0.283), BASS.BASS_POS_BYTE);
            if(mSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(mSEStream2, mSync);
                mSync = 0;
            }
            mSync = BASS.BASS_ChannelSetSync(mSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 38.399), endWave, this);
            BASS.BASS_ChannelPlay(mSEStream, FALSE);
            BASS.BASS_ChannelSlideAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            BASS.BASS_ChannelSlideAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, mVol2, 1000);
            mSE1PlayingFlag = true;
        }
    }

    private final BASS.SYNCPROC endRain = new BASS.SYNCPROC()
    {
        public void SYNCPROC(int handle, int channel, int data, final Object user)
        {
            EffectFragment effectFragment = (EffectFragment)user;
            effectFragment.onRainEnded();
        }
    };

    private void onRainEnded()
    {
        if(mSE1PlayingFlag)
        {
            InputStream is = getResources().openRawResource(R.raw.rain);
            mSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(mSEStream2, BASS.BASS_ChannelSeconds2Bytes(mSEStream2, 0.303), BASS.BASS_POS_BYTE);
            if(mSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(mSEStream, mSync);
                mSync = 0;
            }
            mSync = BASS.BASS_ChannelSetSync(mSEStream2, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream2, 1.503), endRain, this);
            BASS.BASS_ChannelPlay(mSEStream2, FALSE);
            BASS.BASS_ChannelSlideAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, mVol3, 150);
            BASS.BASS_ChannelSlideAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, 0.0f, 300);
            mSE1PlayingFlag = false;
        }
        else if(BASS.BASS_ChannelIsActive(mSEStream2) == BASS.BASS_ACTIVE_PLAYING)
        {
            InputStream is = getResources().openRawResource(R.raw.rain);
            mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(mSEStream, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 0.303), BASS.BASS_POS_BYTE);
            if(mSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(mSEStream2, mSync);
                mSync = 0;
            }
            mSync = BASS.BASS_ChannelSetSync(mSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 1.503), endRain, this);
            BASS.BASS_ChannelPlay(mSEStream, FALSE);
            BASS.BASS_ChannelSlideAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, mVol3, 150);
            BASS.BASS_ChannelSlideAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f, 300);
            mSE1PlayingFlag = true;
        }
    }

    private final BASS.SYNCPROC endRiver = new BASS.SYNCPROC()
    {
        public void SYNCPROC(int handle, int channel, int data, final Object user)
        {
            EffectFragment effectFragment = (EffectFragment)user;
            effectFragment.onRiverEnded();
        }
    };

    private void onRiverEnded()
    {
        if(mSE1PlayingFlag)
        {
            InputStream is = getResources().openRawResource(R.raw.river);
            mSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(mSEStream2, BASS.BASS_ChannelSeconds2Bytes(mSEStream2, 0.0), BASS.BASS_POS_BYTE);
            if(mSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(mSEStream, mSync);
                mSync = 0;
            }
            mSync = BASS.BASS_ChannelSetSync(mSEStream2, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream2, 60.0), endRiver, this);
            BASS.BASS_ChannelPlay(mSEStream2, FALSE);
            BASS.BASS_ChannelSlideAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, mVol4, 5000);
            BASS.BASS_ChannelSlideAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, 0.0f, 5000);
            mSE1PlayingFlag = false;
        }
        else if(BASS.BASS_ChannelIsActive(mSEStream2) == BASS.BASS_ACTIVE_PLAYING)
        {
            InputStream is = getResources().openRawResource(R.raw.river);
            mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(mSEStream, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 0.0), BASS.BASS_POS_BYTE);
            if(mSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(mSEStream2, mSync);
                mSync = 0;
            }
            mSync = BASS.BASS_ChannelSetSync(mSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 60.0), endRiver, this);
            BASS.BASS_ChannelPlay(mSEStream, FALSE);
            BASS.BASS_ChannelSlideAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, mVol4, 5000);
            BASS.BASS_ChannelSlideAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f, 5000);
            mSE1PlayingFlag = true;
        }
    }

    private final BASS.SYNCPROC endWar = new BASS.SYNCPROC()
    {
        public void SYNCPROC(int handle, int channel, int data, final Object user)
        {
            EffectFragment effectFragment = (EffectFragment)user;
            effectFragment.onWarEnded();
        }
    };

    private void onWarEnded()
    {
        if(mSE1PlayingFlag)
        {
            InputStream is = getResources().openRawResource(R.raw.war);
            mSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(mSEStream2, BASS.BASS_ChannelSeconds2Bytes(mSEStream2, 0.0), BASS.BASS_POS_BYTE);
            if(mSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(mSEStream, mSync);
                mSync = 0;
            }
            mSync = BASS.BASS_ChannelSetSync(mSEStream2, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream2, 30.0), endWar, this);
            BASS.BASS_ChannelPlay(mSEStream2, FALSE);
            BASS.BASS_ChannelSlideAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, mVol5, 1000);
            BASS.BASS_ChannelSlideAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            mSE1PlayingFlag = false;
        }
        else if(BASS.BASS_ChannelIsActive(mSEStream2) == BASS.BASS_ACTIVE_PLAYING)
        {
            InputStream is = getResources().openRawResource(R.raw.war);
            mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(mSEStream, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 0.0), BASS.BASS_POS_BYTE);
            if(mSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(mSEStream2, mSync);
                mSync = 0;
            }
            mSync = BASS.BASS_ChannelSetSync(mSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 30.0), endWar, this);
            BASS.BASS_ChannelPlay(mSEStream, FALSE);
            BASS.BASS_ChannelSlideAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, mVol5, 1000);
            BASS.BASS_ChannelSlideAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            mSE1PlayingFlag = true;
        }
    }

    private final BASS.SYNCPROC endFire = new BASS.SYNCPROC()
    {
        public void SYNCPROC(int handle, int channel, int data, final Object user)
        {
            EffectFragment effectFragment = (EffectFragment)user;
            effectFragment.onFireEnded();
        }
    };

    private void onFireEnded()
    {
        if(mSE1PlayingFlag)
        {
            InputStream is = getResources().openRawResource(R.raw.fire);
            mSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(mSEStream2, BASS.BASS_ChannelSeconds2Bytes(mSEStream2, 0.0), BASS.BASS_POS_BYTE);
            if(mSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(mSEStream, mSync);
                mSync = 0;
            }
            mSync = BASS.BASS_ChannelSetSync(mSEStream2, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream2, 90.0), endFire, this);
            BASS.BASS_ChannelPlay(mSEStream2, FALSE);
            BASS.BASS_ChannelSlideAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, mVol6, 5000);
            BASS.BASS_ChannelSlideAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, 0.0f, 5000);
            mSE1PlayingFlag = false;
        }
        else if(BASS.BASS_ChannelIsActive(mSEStream2) == BASS.BASS_ACTIVE_PLAYING)
        {
            InputStream is = getResources().openRawResource(R.raw.fire);
            mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(mSEStream, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 0.0), BASS.BASS_POS_BYTE);
            if(mSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(mSEStream2, mSync);
                mSync = 0;
            }
            mSync = BASS.BASS_ChannelSetSync(mSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 90.0), endFire, this);
            BASS.BASS_ChannelPlay(mSEStream, FALSE);
            BASS.BASS_ChannelSlideAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, mVol6, 5000);
            BASS.BASS_ChannelSlideAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f, 5000);
            mSE1PlayingFlag = true;
        }
    }

    private final BASS.SYNCPROC endCheer = new BASS.SYNCPROC()
    {
        public void SYNCPROC(int handle, int channel, int data, final Object user)
        {
            EffectFragment effectFragment = (EffectFragment)user;
            effectFragment.onCheerEnded();
        }
    };

    private void onCheerEnded()
    {
        if(mSE1PlayingFlag)
        {
            Float fVol = 0.0f;
            BASS.BASS_ChannelGetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, fVol);
            InputStream is = getResources().openRawResource(R.raw.cheer);
            mSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(mSEStream2, BASS.BASS_ChannelSeconds2Bytes(mSEStream2, 1.0), BASS.BASS_POS_BYTE);
            if(mSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(mSEStream, mSync);
                mSync = 0;
            }
            mSync = BASS.BASS_ChannelSetSync(mSEStream2, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream2, 14.0), endCheer, this);
            BASS.BASS_ChannelPlay(mSEStream2, FALSE);
            BASS.BASS_ChannelSlideAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, fVol, 1000);
            BASS.BASS_ChannelSlideAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            mSE1PlayingFlag = false;
        }
        else if(BASS.BASS_ChannelIsActive(mSEStream2) == BASS.BASS_ACTIVE_PLAYING)
        {
            Float fVol = 0.0f;
            BASS.BASS_ChannelGetAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, fVol);
            InputStream is = getResources().openRawResource(R.raw.cheer);
            mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(mSEStream, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 1.0), BASS.BASS_POS_BYTE);
            if(mSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(mSEStream2, mSync);
                mSync = 0;
            }
            mSync = BASS.BASS_ChannelSetSync(mSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 14.0), endCheer, this);
            BASS.BASS_ChannelPlay(mSEStream, FALSE);
            BASS.BASS_ChannelSlideAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, fVol, 1000);
            BASS.BASS_ChannelSlideAttribute(mSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            mSE1PlayingFlag = true;
        }
    }

    public void playMetronome()
    {
        final MediaPlayer mp = MediaPlayer.create(mActivity, R.raw.click);
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            public void onCompletion(MediaPlayer mp)
            {
                mp.reset();
                mp.release();
            }
        });
    }

    private final BASS.DSPPROC vocalCancelDSP = new BASS.DSPPROC()
        {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user)
        {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            for(int a = 0; a < length / 4; a += 2)
            {
                b[a] = b[a + 1] = (-b[a] + b[a + 1]) * 0.5f;
            }
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    private final BASS.DSPPROC monoralDSP = new BASS.DSPPROC()
    {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user)
        {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            for(int a = 0; a < length / 4; a += 2)
                b[a] = b[a + 1] = (b[a] + b[a + 1]) * 0.5f;
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    private final BASS.DSPPROC leftDSP = new BASS.DSPPROC()
    {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user)
        {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            for(int a = 0; a < length / 4; a += 2)
                b[a + 1] = b[a];
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    private final BASS.DSPPROC rightDSP = new BASS.DSPPROC()
    {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user)
        {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            for(int a = 0; a < length / 4; a += 2)
                b[a] = b[a + 1];
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    private final BASS.DSPPROC exchangeDSP = new BASS.DSPPROC()
    {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user)
        {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            for(int a = 0; a < length / 4; a += 2)
            {
                float fTemp = b[a];
                b[a] = b[a + 1];
                b[a + 1] = fTemp;
            }
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    private final int ECHBUFLEN = 1200;
    private final float[][] echbuf = new float[ECHBUFLEN][2];
    private int echpos;
    private final BASS.DSPPROC doublingDSP = new BASS.DSPPROC()
    {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user)
        {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            for(int a = 0; a < length / 4; a += 2)
            {
                float l = echbuf[echpos][0];
                float r = (b[a] + b[a+1]) * 0.5f;
                echbuf[echpos][0]=(b[a] + b[a+1]) * 0.5f;
                echbuf[echpos][1]=(b[a] + b[a+1]) * 0.5f;
                b[a] = l;
                b[a + 1] = r;
                echpos++;
                if (echpos==ECHBUFLEN)
                    echpos=0;
            }
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    private final BASS.DSPPROC panDSP = new BASS.DSPPROC()
    {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user)
        {
            EffectFragment effectFragment = (EffectFragment)user;
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            for(int a = 0; a < length / 4; a += 2)
            {
                if(effectFragment.mPan > 0.0f)
                    b[a] = b[a] * (1.0f - effectFragment.mPan);
                else
                    b[a + 1] = b[a + 1] * (1.0f + effectFragment.mPan);
            }
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    private final BASS.DSPPROC normalizeDSP = new BASS.DSPPROC()
    {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user)
        {
            EffectFragment effectFragment = (EffectFragment)user;
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            if(effectFragment.mPeak != 0.0f)
            {
                for(int a = 0; a < length / 4; a++)
                    b[a] /= effectFragment.mPeak;
            }
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    private final BASS.DSPPROC phaseReversalDSP = new BASS.DSPPROC()
    {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user)
        {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            for(int a = 0; a < length / 4; a++)
                b[a] = -b[a];
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if(hasFocus)
        {
            if(v.getId() == R.id.editTimeEffectDetail)
                showTimeEffectDialog();
            else if(v.getId() == R.id.editSpeedEffectDetail)
                showSpeedEffectDialog();
        }
    }

    private void showTimeEffectDialog()
    {
        TimeEffectDetailFragmentDialog dialog = new TimeEffectDetailFragmentDialog();
        FragmentManager fm = getFragmentManager();
        if(fm != null) dialog.show(fm, "span_setting_dialog");
    }

    private void showSpeedEffectDialog()
    {
        SpeedEffectDetailFragmentDialog dialog = new SpeedEffectDetailFragmentDialog();
        FragmentManager fm = getFragmentManager();
        if(fm != null) dialog.show(fm, "span_setting_dialog");
    }

    public void clearFocus()
    {
        mEditTimeEffectDetail.clearFocus();
        mEditSpeedEffectDetail.clearFocus();
    }
}
