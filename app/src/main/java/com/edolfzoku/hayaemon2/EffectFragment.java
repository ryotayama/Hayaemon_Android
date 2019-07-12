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
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.media.effect.Effect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS_FX;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class EffectFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener, SeekBar.OnSeekBarChangeListener, View.OnFocusChangeListener
{
    private MainActivity mActivity = null;
    private EffectsAdapter mEffectsAdapter;
    private EffectTemplatesAdapter mEffectTemplatesAdapter;
    private final ArrayList<EffectItem> mEffectItems;
    private ArrayList<EffectTemplateItem> mReverbItems;
    private ItemTouchHelper mEffectTemplateTouchHelper;
    private int mDspVocalCancel = 0;
    private int mDspMonoral = 0;
    private int mDspLeft = 0;
    private int mDspRight = 0;
    private int mDspExchange = 0;
    private int mDspDoubling = 0;
    private int mDspPan = 0;
    private int mDspNormalize = 0;
    private int mFxComp = 0;
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
    private float mCompGain, mCompThreshold, mCompRatio, mCompAttack, mCompRelease;
    private float mEchoDry, mEchoWet, mEchoFeedback, mEchoDelay;
    private float mReverbDry, mReverbWet, mReverbRoomSize, mReverbDamp, mReverbWidth;
    private float mChorusDry, mChorusWet, mChorusFeedback, mChorusMinSweep, mChorusMaxSweep, mChorusRate;
    private float mDistortionDrive, mDistortionDry, mDistortionWet, mDistortionFeedback, mDistortionVolume;
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
    private static final int EFFECTTYPE_COMP = 12;
    private static final int EFFECTTYPE_COMP_CUSTOMIZE = 13;
    private static final int EFFECTTYPE_FREQUENCY = 14;
    // private static final int EFFECTTYPE_PHASEREVERSAL = 15;
    private static final int EFFECTTYPE_ECHO_STADIUM = 16;
    // private static final int EFFECTTYPE_ECHO_HALL = 17;
    // private static final int EFFECTTYPE_ECHO_LIVEHOUSE = 18;
    // private static final int EFFECTTYPE_ECHO_ROOM = 19;
    // private static final int EFFECTTYPE_ECHO_BATHROOM = 20;
    // private static final int EFFECTTYPE_ECHO_VOCAL = 21;
    // private static final int EFFECTTYPE_ECHO_MOUNTAIN = 22;
    private static final int EFFECTTYPE_ECHO_CUSTOMIZE = 23;
    private static final int EFFECTTYPE_REVERB = 24;
    private static final int EFFECTTYPE_CHORUS = 25;
    private static final int EFFECTTYPE_FLANGER = 26;
    private static final int EFFECTTYPE_CHORUS_CUSTOMIZE = 27;
    private static final int EFFECTTYPE_DISTORTION_STRONG = 28;
    // private static final int EFFECTTYPE_DISTORTION_MIDDLE = 29;
    // private static final int EFFECTTYPE_DISTORTION_WEAK = 30;
    private static final int EFFECTTYPE_DISTORTION_CUSTOMIZE = 31;
    static final int EFFECTTYPE_REVERSE = 32;
    private static final int EFFECTTYPE_INCREASESPEED = 33;
    private static final int EFFECTTYPE_DECREASESPEED = 34;
    private static final int EFFECTTYPE_OLDRECORD = 35;
    private static final int EFFECTTYPE_LOWBATTERY = 36;
    private static final int EFFECTTYPE_NOSENSE_STRONG = 37;
    private static final int EFFECTTYPE_NOSENSE_MIDDLE = 38;
    private static final int EFFECTTYPE_NOSENSE_WEAK = 39;
    private static final int EFFECTTYPE_EARTRAINING = 40;
    private static final int EFFECTTYPE_METRONOME = 41;
    private static final int EFFECTTYPE_RECORDNOISE = 42;
    private static final int EFFECTTYPE_ROAROFWAVES = 43;
    private static final int EFFECTTYPE_RAIN = 44;
    private static final int EFFECTTYPE_RIVER = 45;
    private static final int EFFECTTYPE_WAR = 46;
    private static final int EFFECTTYPE_FIRE = 47;
    private static final int EFFECTTYPE_CONCERTHALL = 48;
    private Timer mTimer;
    private int mSEStream;
    private int mSEStream2;
    private boolean mSE1PlayingFlag = false;
    private boolean mSorting = false;
    private boolean mAddTemplate;
    private int mSync = 0;
    private Handler mHandler;
    private float mAccel = 0.0f;
    private float mVelo1 = 0.0f;
    private float mVelo2 = 0.0f;
    private boolean mContinueFlag = true;
    private final Handler mHandlerLongClick;
    private int mEffectDetail = -1, mReverbSelected = -1;

    private RecyclerView mRecyclerEffects, mRecyclerEffectTemplates;
    private TextView mTextEffectName, mTextEffectDetail, mTextEffectLabel, mTextCompGain, mTextCompThreshold, mTextCompRatio, mTextCompAttack, mTextCompRelease, mTextEchoDry, mTextEchoWet, mTextEchoFeedback, mTextEchoDelay, mTextReverbDry, mTextReverbWet, mTextReverbRoomSize, mTextReverbDamp, mTextReverbWidth, mTextChorusDry, mTextChorusWet, mTextChorusFeedback, mTextChorusMinSweep, mTextChorusMaxSweep, mTextChorusRate, mTextDistortionDrive, mTextDistortionDry, mTextDistortionWet, mTextDistortionFeedback, mTextDistortionVolume, mTextFinishSortEffect;
    private EditText mEditSpeedEffectDetail, mEditTimeEffectDetail;
    private RelativeLayout mRelativeEffectDetail, mRelativeSliderEffectDatail, mRelativeRollerEffectDetail, mRelativeEffectTemplates, mRelativeEffectTitle;
    private SeekBar mSeekEffectDetail, mSeekCompGain, mSeekCompThreshold, mSeekCompRatio, mSeekCompAttack, mSeekCompRelease, mSeekEchoDry, mSeekEchoWet, mSeekEchoFeedback, mSeekEchoDelay, mSeekReverbDry, mSeekReverbWet, mSeekReverbRoomSize, mSeekReverbDamp, mSeekReverbWidth, mSeekChorusDry, mSeekChorusWet, mSeekChorusFeedback, mSeekChorusMinSweep, mSeekChorusMaxSweep, mSeekChorusRate, mSeekDistortionDrive, mSeekDistortionDry, mSeekDistortionWet, mSeekDistortionFeedback, mSeekDistortionVolume;
    private ImageButton mBtnEffectMinus, mBtnEffectPlus, mBtnCompGainMinus, mBtnCompGainPlus, mBtnCompThresholdMinus, mBtnCompThresholdPlus, mBtnCompRatioMinus, mBtnCompRatioPlus, mBtnCompAttackMinus, mBtnCompAttackPlus, mBtnCompReleaseMinus, mBtnCompReleasePlus, mBtnEchoDryMinus, mBtnEchoDryPlus, mBtnEchoWetMinus, mBtnEchoWetPlus, mBtnEchoFeedbackMinus, mBtnEchoFeedbackPlus, mBtnEchoDelayMinus, mBtnEchoDelayPlus, mBtnReverbDryMinus, mBtnReverbDryPlus, mBtnReverbWetMinus, mBtnReverbWetPlus, mBtnReverbRoomSizeMinus, mBtnReverbRoomSizePlus, mBtnReverbDampMinus, mBtnReverbDampPlus, mBtnReverbWidthMinus, mBtnReverbWidthPlus, mBtnChorusDryMinus, mBtnChorusDryPlus, mBtnChorusWetMinus, mBtnChorusWetPlus, mBtnChorusFeedbackMinus, mBtnChorusFeedbackPlus, mBtnChorusMinSweepMinus, mBtnChorusMinSweepPlus, mBtnChorusMaxSweepMinus, mBtnChorusMaxSweepPlus, mBtnChorusRateMinus, mBtnChorusRatePlus, mBtnDistortionDriveMinus, mBtnDistortionDrivePlus, mBtnDistortionDryMinus, mBtnDistortionDryPlus, mBtnDistortionWetMinus, mBtnDistortionWetPlus, mBtnDistortionFeedbackMinus, mBtnDistortionFeedbackPlus, mBtnDistortionVolumeMinus, mBtnDistortionVolumePlus;
    private Button mBtnEffectBack, mBtnEffectFinish, mBtnEffectTemplateOff, mBtnReverbSaveAs;
    private AnimationButton mBtnEffectTemplateMenu, mBtnAddEffectTemplate;
    private ScrollView mScrollCompCustomize, mScrollEchoCustomize, mScrollReverbCustomize, mScrollChorusCustomize, mScrollDistortionCustomize;
    private View mViewSepEffect, mViewSepEffectTemplate;
    private ImageView mImgEffectBack;

    public ItemTouchHelper getEffectTemplateTouchHelper() { return mEffectTemplateTouchHelper; }
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
    public float getCompGain() { return mCompGain; }
    public float getCompThreshold() { return mCompThreshold; }
    public float getCompRatio() { return mCompRatio; }
    public float getCompAttack() { return mCompAttack; }
    public float getCompRelease() { return mCompRelease; }
    public float getEchoDry() { return mEchoDry; }
    public float getEchoWet() { return mEchoWet; }
    public float getEchoFeedback() { return mEchoFeedback; }
    public float getEchoDelay() { return mEchoDelay; }
    public float getReverbDry() { return mReverbDry; }
    public float getReverbWet() { return mReverbWet; }
    public float getReverbRoomSize() { return mReverbRoomSize; }
    public float getReverbDamp() { return mReverbDamp; }
    public float getReverbWidth() { return mReverbWidth; }
    public float getChorusDry() { return mChorusDry; }
    public float getChorusWet() { return mChorusWet; }
    public float getChorusFeedback() { return mChorusFeedback; }
    public float getChorusMinSweep() { return mChorusMinSweep; }
    public float getChorusMaxSweep() { return mChorusMaxSweep; }
    public float getChorusRate() { return mChorusRate; }
    public float getDistortionDrive() { return mDistortionDrive; }
    public float getDistortionDry() { return mDistortionDry; }
    public float getDistortionWet() { return mDistortionWet; }
    public float getDistortionFeedback() { return mDistortionFeedback; }
    public float getDistortionVolume() { return mDistortionVolume; }
    private void setEffectTemplateItems(ArrayList<EffectTemplateItem> lists) {
        mReverbItems = lists;
        mEffectTemplatesAdapter.changeItems(mReverbItems);
    }

    public boolean isSorting() { return mSorting; }
    public boolean isSelectedItem(int nItem)
    {
        if(nItem >= mEffectItems.size()) return false;
        EffectItem item = mEffectItems.get(nItem);
        return item.isSelected();
    }

    public boolean isSelectedTemplateItem(int nItem)
    {
        if(mEffectDetail == EFFECTTYPE_REVERB) {
            if (nItem >= mReverbItems.size()) return false;
            EffectTemplateItem item = mReverbItems.get(nItem);
            return item.isSelected();
        }
        return false;
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
                if(item.getEffectName().equals(itemSaved.getEffectName())) {
                    item.setSelected(itemSaved.isSelected());
                    mEffectsAdapter.notifyItemChanged(i);
                }
            }
        }
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
    public int getReverbSelected() { return mReverbSelected; }
    public void setReverbSelected(int nSelected) {
        mReverbSelected = nSelected;
        if(mRelativeEffectTemplates.getVisibility() == View.VISIBLE && mEffectDetail == EFFECTTYPE_REVERB) {
            mBtnEffectTemplateOff.setSelected(nSelected == -1);
            for(int i = 0; i < mReverbItems.size(); i++)
                mReverbItems.get(i).setSelected(i == nSelected);
            mEffectTemplatesAdapter.notifyDataSetChanged();
        }
    }

    public EffectFragment()
    {
        mEffectItems = new ArrayList<>();
        mReverbItems = new ArrayList<>();
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
        if (v.getId() == R.id.btnEffectBack) {
            if(mScrollReverbCustomize.getVisibility() == View.VISIBLE) {
                if(mReverbSelected != -1) {
                    ArrayList<Float> arFloats = mReverbItems.get(mReverbSelected).getArPresets();
                    setReverb(arFloats.get(0), arFloats.get(1), arFloats.get(2), arFloats.get(3), arFloats.get(4), true);
                }
                else resetReverb();
                mBtnEffectFinish.setVisibility(View.GONE);
                mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                mScrollReverbCustomize.setVisibility(View.INVISIBLE);
                mBtnEffectBack.setText(R.string.back);
                mImgEffectBack.setVisibility(View.VISIBLE);
                mBtnEffectBack.setPadding((int)(32 * mActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
                mBtnAddEffectTemplate.setAlpha(1.0f);
            }
            else {
                mRelativeEffectDetail.setVisibility(View.GONE);
                mRecyclerEffects.setVisibility(View.VISIBLE);
            }
        }
        else if(v.getId() == R.id.btnEffectFinish) {
            if(mAddTemplate) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(R.string.saveTemplate);
                LinearLayout linearLayout = new LinearLayout(mActivity);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                final EditText editPreset = new EditText(mActivity);
                editPreset.setHint(R.string.templateName);
                editPreset.setHintTextColor(Color.argb(255, 192, 192, 192));
                editPreset.setText(R.string.newReverb);
                linearLayout.addView(editPreset);
                builder.setView(linearLayout);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ArrayList<Float> arPresets = new ArrayList<>();
                        arPresets.add(Float.parseFloat((String)mTextReverbDry.getText()));
                        arPresets.add(Float.parseFloat((String)mTextReverbWet.getText()));
                        arPresets.add(Float.parseFloat((String)mTextReverbRoomSize.getText()));
                        arPresets.add(Float.parseFloat((String)mTextReverbDamp.getText()));
                        arPresets.add(Float.parseFloat((String)mTextReverbWidth.getText()));
                        mReverbItems.add(new EffectTemplateItem(editPreset.getText().toString(), arPresets));
                        mEffectTemplatesAdapter.notifyItemInserted(mReverbItems.size() - 1);
                        saveData();

                        mBtnEffectFinish.setVisibility(View.GONE);
                        mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                        mScrollReverbCustomize.setVisibility(View.INVISIBLE);
                        mBtnEffectBack.setText(R.string.back);
                        mImgEffectBack.setVisibility(View.VISIBLE);
                        mBtnEffectBack.setPadding((int)(32 * mActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
                        mBtnAddEffectTemplate.setAlpha(1.0f);

                        mBtnEffectTemplateOff.setSelected(false);
                        for(int i = 0; i < mReverbItems.size()-1; i++)
                            mReverbItems.get(i).setSelected(false);
                        mReverbItems.get(mReverbItems.size()-1).setSelected(true);
                        mReverbSelected = mReverbItems.size()-1;

                        mEffectItems.get(mEffectDetail).setSelected(true);
                        checkDuplicate(mEffectDetail);
                        mEffectsAdapter.notifyDataSetChanged();

                        mEffectTemplatesAdapter.notifyDataSetChanged();
                        mRecyclerEffectTemplates.scrollToPosition(mReverbItems.size()-1);
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                {
                    @Override
                    public void onShow(DialogInterface arg0)
                    {
                        if(alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        editPreset.requestFocus();
                        editPreset.setSelection(editPreset.getText().toString().length());
                        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (null != imm) imm.showSoftInput(editPreset, 0);
                    }
                });
                alertDialog.show();
            }
            else {
                EffectTemplateItem item = null;
                int nItem = 0;
                for (; nItem < mReverbItems.size(); nItem++) {
                    item = mReverbItems.get(nItem);
                    if (item.isSelected()) break;
                }
                if (item != null) {
                    ArrayList<Float> arPresets = item.getArPresets();
                    arPresets.set(0, Float.parseFloat((String)mTextReverbDry.getText()));
                    arPresets.set(1, Float.parseFloat((String)mTextReverbWet.getText()));
                    arPresets.set(2, Float.parseFloat((String)mTextReverbRoomSize.getText()));
                    arPresets.set(3, Float.parseFloat((String)mTextReverbDamp.getText()));
                    arPresets.set(4, Float.parseFloat((String)mTextReverbWidth.getText()));
                    saveData();
                }
                mBtnEffectFinish.setVisibility(View.GONE);
                mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                mScrollReverbCustomize.setVisibility(View.INVISIBLE);
                mBtnAddEffectTemplate.setAlpha(1.0f);
            }
        }
        else if(v.getId() == R.id.btnEffectTemplateMenu) showTemplateMenu();
        else if(v.getId() == R.id.btnAddEffectTemplate) {
            mAddTemplate = true;

            mEffectItems.get(mEffectDetail).setSelected(true);
            checkDuplicate(mEffectDetail);
            if(mBtnEffectTemplateOff.isSelected())
                setReverb(70, 100, 85, 50, 90, true);

            mTextEffectName.setText(R.string.newReverb);

            mBtnEffectBack.setText(R.string.cancel);
            mBtnEffectFinish.setText(R.string.save);

            mBtnEffectFinish.setVisibility(View.VISIBLE);
            mRelativeEffectTemplates.setVisibility(View.INVISIBLE);
            mScrollReverbCustomize.setVisibility(View.VISIBLE);
            mImgEffectBack.setVisibility(View.INVISIBLE);
            mBtnEffectBack.setPadding((int)(16 * mActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
            mBtnReverbSaveAs.setVisibility(View.GONE);
            mBtnAddEffectTemplate.setAlpha(0.0f);
        }
        else if(v.getId() == R.id.textFinishSortEffect) {
            mRecyclerEffectTemplates.setPadding(0, 0, 0, 0);
            mBtnEffectTemplateOff.setVisibility(View.VISIBLE);
            mRelativeEffectTitle.setVisibility(View.VISIBLE);
            mViewSepEffect.setVisibility(View.VISIBLE);
            mViewSepEffectTemplate.setVisibility(View.VISIBLE);
            mBtnAddEffectTemplate.setAlpha(1.0f);
            mTextFinishSortEffect.setVisibility(View.INVISIBLE);
            mSorting = false;
            mEffectTemplatesAdapter.notifyDataSetChanged();
            mEffectTemplateTouchHelper.attachToRecyclerView(null);
        }
        else if(v.getId() == R.id.btnEffectTemplateOff) {
            mBtnEffectTemplateOff.setSelected(true);
            if(mEffectDetail == EFFECTTYPE_REVERB) {
                for(int i = 0; i < mReverbItems.size(); i++) mReverbItems.get(i).setSelected(false);
                resetReverb();
            }
            mEffectTemplatesAdapter.notifyDataSetChanged();
        }
        else if (v.getId() == R.id.btnEffectMinus) minusValue();
        else if (v.getId() == R.id.btnEffectPlus) plusValue();
        else if(v.getId() == R.id.btnCompGainMinus) minusCompGain();
        else if(v.getId() == R.id.btnCompGainPlus) plusCompGain();
        else if(v.getId() == R.id.btnCompThresholdMinus) minusCompThreshold();
        else if(v.getId() == R.id.btnCompThresholdPlus) plusCompThreshold();
        else if(v.getId() == R.id.btnCompRatioMinus) minusCompRatio();
        else if(v.getId() == R.id.btnCompRatioPlus) plusCompRatio();
        else if(v.getId() == R.id.btnCompAttackMinus) minusCompAttack();
        else if(v.getId() == R.id.btnCompAttackPlus) plusCompAttack();
        else if(v.getId() == R.id.btnCompReleaseMinus) minusCompRelease();
        else if(v.getId() == R.id.btnCompReleasePlus) plusCompRelease();
        else if(v.getId() == R.id.btnCompRandom) setCompRandom();
        else if(v.getId() == R.id.btnResetComp) resetComp();
        else if(v.getId() == R.id.btnEchoDryMinus) minusEchoDry();
        else if(v.getId() == R.id.btnEchoDryPlus) plusEchoDry();
        else if(v.getId() == R.id.btnEchoWetMinus) minusEchoWet();
        else if(v.getId() == R.id.btnEchoWetPlus) plusEchoWet();
        else if(v.getId() == R.id.btnEchoFeedbackMinus) minusEchoFeedback();
        else if(v.getId() == R.id.btnEchoFeedbackPlus) plusEchoFeedback();
        else if(v.getId() == R.id.btnEchoDelayMinus) minusEchoDelay();
        else if(v.getId() == R.id.btnEchoDelayPlus) plusEchoDelay();
        else if(v.getId() == R.id.btnEchoRandom) setEchoRandom();
        else if(v.getId() == R.id.btnResetEcho) resetEcho();
        else if(v.getId() == R.id.btnReverbDryMinus) minusReverbDry();
        else if(v.getId() == R.id.btnReverbDryPlus) plusReverbDry();
        else if(v.getId() == R.id.btnReverbWetMinus) minusReverbWet();
        else if(v.getId() == R.id.btnReverbWetPlus) plusReverbWet();
        else if(v.getId() == R.id.btnReverbRoomSizeMinus) minusReverbRoomSize();
        else if(v.getId() == R.id.btnReverbRoomSizePlus) plusReverbRoomSize();
        else if(v.getId() == R.id.btnReverbDampMinus) minusReverbDamp();
        else if(v.getId() == R.id.btnReverbDampPlus) plusReverbDamp();
        else if(v.getId() == R.id.btnReverbWidthMinus) minusReverbWidth();
        else if(v.getId() == R.id.btnReverbWidthPlus) plusReverbWidth();
        else if(v.getId() == R.id.btnReverbRandom) setReverbRandom();
        else if(v.getId() == R.id.btnResetReverb) {
            if(mReverbSelected != -1) {
                ArrayList<Float> arFloats = mReverbItems.get(mReverbSelected).getArPresets();
                setReverb(arFloats.get(0), arFloats.get(1), arFloats.get(2), arFloats.get(3), arFloats.get(4), true);
            }
            else resetReverb();
        }
        else if(v.getId() == R.id.btnReverbSaveAs) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.saveTemplate);
            LinearLayout linearLayout = new LinearLayout(mActivity);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            final EditText editPreset = new EditText(mActivity);
            editPreset.setHint(R.string.templateName);
            editPreset.setHintTextColor(Color.argb(255, 192, 192, 192));
            editPreset.setText(R.string.newReverb);
            linearLayout.addView(editPreset);
            builder.setView(linearLayout);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ArrayList<Float> arPresets = new ArrayList<>();
                    arPresets.add(Float.parseFloat((String)mTextReverbDry.getText()));
                    arPresets.add(Float.parseFloat((String)mTextReverbWet.getText()));
                    arPresets.add(Float.parseFloat((String)mTextReverbRoomSize.getText()));
                    arPresets.add(Float.parseFloat((String)mTextReverbDamp.getText()));
                    arPresets.add(Float.parseFloat((String)mTextReverbWidth.getText()));
                    mReverbItems.add(new EffectTemplateItem(editPreset.getText().toString(), arPresets));
                    mEffectTemplatesAdapter.notifyItemInserted(mReverbItems.size() - 1);
                    saveData();

                    mBtnEffectFinish.setVisibility(View.GONE);
                    mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                    mScrollReverbCustomize.setVisibility(View.INVISIBLE);
                    mImgEffectBack.setVisibility(View.VISIBLE);
                    mBtnEffectBack.setPadding((int)(32 * mActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
                    mBtnAddEffectTemplate.setAlpha(1.0f);

                    for(int i = 0; i < mReverbItems.size()-1; i++)
                        mReverbItems.get(i).setSelected(false);
                    mReverbItems.get(mReverbItems.size()-1).setSelected(true);
                    mReverbSelected = mReverbItems.size()-1;

                    mEffectItems.get(mEffectDetail).setSelected(true);
                    checkDuplicate(mEffectDetail);
                    mEffectsAdapter.notifyDataSetChanged();

                    mEffectTemplatesAdapter.notifyDataSetChanged();
                    mRecyclerEffectTemplates.scrollToPosition(mReverbItems.size()-1);
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
            {
                @Override
                public void onShow(DialogInterface arg0)
                {
                    if(alertDialog.getWindow() != null) {
                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                        lp.dimAmount = 0.4f;
                        alertDialog.getWindow().setAttributes(lp);
                    }
                    editPreset.requestFocus();
                    editPreset.setSelection(editPreset.getText().toString().length());
                    InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (null != imm) imm.showSoftInput(editPreset, 0);
                }
            });
            alertDialog.show();
        }
        else if(v.getId() == R.id.btnChorusDryMinus) minusChorusDry();
        else if(v.getId() == R.id.btnChorusDryPlus) plusChorusDry();
        else if(v.getId() == R.id.btnChorusWetMinus) minusChorusWet();
        else if(v.getId() == R.id.btnChorusWetPlus) plusChorusWet();
        else if(v.getId() == R.id.btnChorusFeedbackMinus) minusChorusFeedback();
        else if(v.getId() == R.id.btnChorusFeedbackPlus) plusChorusFeedback();
        else if(v.getId() == R.id.btnChorusMinSweepMinus) minusChorusMinSweep();
        else if(v.getId() == R.id.btnChorusMinSweepPlus) plusChorusMinSweep();
        else if(v.getId() == R.id.btnChorusMaxSweepMinus) minusChorusMaxSweep();
        else if(v.getId() == R.id.btnChorusMaxSweepPlus) plusChorusMaxSweep();
        else if(v.getId() == R.id.btnChorusRateMinus) minusChorusRate();
        else if(v.getId() == R.id.btnChorusRatePlus) plusChorusRate();
        else if(v.getId() == R.id.btnChorusRandom) setChorusRandom();
        else if(v.getId() == R.id.btnResetChorus) resetChorus();
        else if(v.getId() == R.id.btnDistortionDriveMinus) minusDistortionDrive();
        else if(v.getId() == R.id.btnDistortionDrivePlus) plusDistortionDrive();
        else if(v.getId() == R.id.btnDistortionDryMinus) minusDistortionDry();
        else if(v.getId() == R.id.btnDistortionDryPlus) plusDistortionDry();
        else if(v.getId() == R.id.btnDistortionWetMinus) minusDistortionWet();
        else if(v.getId() == R.id.btnDistortionWetPlus) plusDistortionWet();
        else if(v.getId() == R.id.btnDistortionFeedbackMinus) minusDistortionFeedback();
        else if(v.getId() == R.id.btnDistortionFeedbackPlus) plusDistortionFeedback();
        else if(v.getId() == R.id.btnDistortionVolumeMinus) minusDistortionVolume();
        else if(v.getId() == R.id.btnDistortionVolumePlus) plusDistortionVolume();
        else if(v.getId() == R.id.btnDistortionRandom) setDistortionRandom();
        else if(v.getId() == R.id.btnResetDistortion) resetDistortion();
    }

    @Override
    public boolean onLongClick(View v)
    {
        mContinueFlag = true;
        if (v.getId() == R.id.btnEffectMinus) {
            mHandlerLongClick.post(repeatMinusValue);
            return true;
        }
        else if (v.getId() == R.id.btnEffectPlus) {
            mHandlerLongClick.post(repeatPlusValue);
            return true;
        }
        else if (v.getId() == R.id.btnCompGainMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusCompGain();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnCompGainPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusCompGain();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnCompThresholdMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusCompThreshold();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnCompThresholdPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusCompThreshold();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnCompRatioMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusCompRatio();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnCompRatioPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusCompRatio();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnCompAttackMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusCompAttack();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnCompAttackPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusCompAttack();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnCompReleaseMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusCompRelease();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnCompReleasePlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusCompRelease();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnEchoDryMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusEchoDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnEchoDryPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusEchoDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnEchoWetMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusEchoWet();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnEchoWetPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusEchoWet();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnEchoDryMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusEchoDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnEchoDryPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusEchoDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnEchoDryMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusEchoDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnEchoDryPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusEchoDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnReverbDryMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusReverbDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnReverbDryPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusReverbDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnReverbWetMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusReverbWet();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnReverbWetPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusReverbWet();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnReverbRoomSizeMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusReverbRoomSize();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnReverbRoomSizePlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusReverbRoomSize();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnReverbDampMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusReverbDamp();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnReverbDampPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusReverbDamp();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnReverbWidthMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusReverbWidth();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnReverbWidthPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusReverbWidth();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnChorusDryMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusChorusDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnChorusDryPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusChorusDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnChorusWetMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusChorusWet();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnChorusWetPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusChorusWet();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnChorusFeedbackMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusChorusFeedback();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnChorusFeedbackPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusChorusFeedback();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnChorusMinSweepMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusChorusMinSweep();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnChorusMinSweepPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusChorusMinSweep();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnChorusMaxSweepMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusChorusMaxSweep();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnChorusMaxSweepPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusChorusMaxSweep();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnChorusRateMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusChorusRate();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnChorusRatePlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusChorusRate();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnDistortionDriveMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusDistortionDrive();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnDistortionDrivePlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusDistortionDrive();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnDistortionDryMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusDistortionDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnDistortionDryPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusDistortionDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnDistortionWetMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusDistortionWet();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnDistortionWetPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusDistortionWet();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnDistortionFeedbackMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusDistortionFeedback();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnDistortionFeedbackPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusDistortionFeedback();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnDistortionVolumeMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    minusDistortionVolume();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        else if (v.getId() == R.id.btnDistortionVolumePlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if(!mContinueFlag) return;
                    plusDistortionVolume();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
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
        mEffectTemplatesAdapter = new EffectTemplatesAdapter(mActivity, mReverbItems);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        mTextEffectName = mActivity.findViewById(R.id.textEffectName);
        mEditSpeedEffectDetail = mActivity.findViewById(R.id.editSpeedEffectDetail);
        mEditTimeEffectDetail = mActivity.findViewById(R.id.editTimeEffectDetail);
        mRelativeEffectDetail = mActivity.findViewById(R.id.relativeEffectDetail);
        mRelativeEffectTitle = mActivity.findViewById(R.id.relativeEffectTitle);
        mRelativeEffectTemplates = mActivity.findViewById(R.id.relativeEffectTemplates);
        mSeekEffectDetail = mActivity.findViewById(R.id.seekEffectDetail);
        mTextEffectDetail = mActivity.findViewById(R.id.textEffectDetail);
        mBtnEffectMinus = mActivity.findViewById(R.id.btnEffectMinus);
        mBtnEffectPlus = mActivity.findViewById(R.id.btnEffectPlus);
        mTextEffectLabel = mActivity.findViewById(R.id.textEffectLabel);
        mRelativeSliderEffectDatail = mActivity.findViewById(R.id.relativeSliderEffectDatail);
        mRelativeRollerEffectDetail = mActivity.findViewById(R.id.relativeRollerEffectDatail);
        mBtnEffectBack = mActivity.findViewById(R.id.btnEffectBack);
        mBtnEffectFinish = mActivity.findViewById(R.id.btnEffectFinish);
        mBtnEffectTemplateOff = mActivity.findViewById(R.id.btnEffectTemplateOff);
        mBtnEffectTemplateMenu = mActivity.findViewById(R.id.btnEffectTemplateMenu);
        mBtnAddEffectTemplate = mActivity.findViewById(R.id.btnAddEffectTemplate);
        mViewSepEffect = mActivity.findViewById(R.id.viewSepEffect);
        mViewSepEffectTemplate = mActivity.findViewById(R.id.viewSepEffectTemplate);
        mTextFinishSortEffect = mActivity.findViewById(R.id.textFinishSortEffect);
        mImgEffectBack = mActivity.findViewById(R.id.imgEffectBack);
        mBtnReverbSaveAs = mActivity.findViewById(R.id.btnReverbSaveAs);

        mScrollCompCustomize = mActivity.findViewById(R.id.scrollCompCustomize);
        mSeekCompGain = mActivity.findViewById(R.id.seekCompGain);
        mSeekCompThreshold = mActivity.findViewById(R.id.seekCompThreshold);
        mSeekCompRatio = mActivity.findViewById(R.id.seekCompRatio);
        mSeekCompAttack = mActivity.findViewById(R.id.seekCompAttack);
        mSeekCompRelease = mActivity.findViewById(R.id.seekCompRelease);
        mTextCompGain = mActivity.findViewById(R.id.textCompGain);
        mTextCompThreshold = mActivity.findViewById(R.id.textCompThreshold);
        mTextCompRatio = mActivity.findViewById(R.id.textCompRatio);
        mTextCompAttack = mActivity.findViewById(R.id.textCompAttack);
        mTextCompRelease = mActivity.findViewById(R.id.textCompRelease);
        mBtnCompGainMinus = mActivity.findViewById(R.id.btnCompGainMinus);
        mBtnCompGainPlus = mActivity.findViewById(R.id.btnCompGainPlus);
        mBtnCompThresholdMinus = mActivity.findViewById(R.id.btnCompThresholdMinus);
        mBtnCompThresholdPlus = mActivity.findViewById(R.id.btnCompThresholdPlus);
        mBtnCompRatioMinus = mActivity.findViewById(R.id.btnCompRatioMinus);
        mBtnCompRatioPlus = mActivity.findViewById(R.id.btnCompRatioPlus);
        mBtnCompAttackMinus = mActivity.findViewById(R.id.btnCompAttackMinus);
        mBtnCompAttackPlus = mActivity.findViewById(R.id.btnCompAttackPlus);
        mBtnCompReleaseMinus = mActivity.findViewById(R.id.btnCompReleaseMinus);
        mBtnCompReleasePlus = mActivity.findViewById(R.id.btnCompReleasePlus);
        mScrollEchoCustomize = mActivity.findViewById(R.id.scrollEchoCustomize);
        mSeekEchoDry = mActivity.findViewById(R.id.seekEchoDry);
        mSeekEchoWet = mActivity.findViewById(R.id.seekEchoWet);
        mSeekEchoFeedback = mActivity.findViewById(R.id.seekEchoFeedback);
        mSeekEchoDelay = mActivity.findViewById(R.id.seekEchoDelay);
        mTextEchoDry = mActivity.findViewById(R.id.textEchoDry);
        mTextEchoWet = mActivity.findViewById(R.id.textEchoWet);
        mTextEchoFeedback = mActivity.findViewById(R.id.textEchoFeedback);
        mTextEchoDelay = mActivity.findViewById(R.id.textEchoDelay);
        mBtnEchoDryMinus = mActivity.findViewById(R.id.btnEchoDryMinus);
        mBtnEchoDryPlus = mActivity.findViewById(R.id.btnEchoDryPlus);
        mBtnEchoWetMinus = mActivity.findViewById(R.id.btnEchoWetMinus);
        mBtnEchoWetPlus = mActivity.findViewById(R.id.btnEchoWetPlus);
        mBtnEchoFeedbackMinus = mActivity.findViewById(R.id.btnEchoFeedbackMinus);
        mBtnEchoFeedbackPlus = mActivity.findViewById(R.id.btnEchoFeedbackPlus);
        mBtnEchoDelayMinus = mActivity.findViewById(R.id.btnEchoDelayMinus);
        mBtnEchoDelayPlus = mActivity.findViewById(R.id.btnEchoDelayPlus);
        mScrollReverbCustomize = mActivity.findViewById(R.id.scrollReverbCustomize);
        mSeekReverbDry = mActivity.findViewById(R.id.seekReverbDry);
        mSeekReverbWet = mActivity.findViewById(R.id.seekReverbWet);
        mSeekReverbRoomSize = mActivity.findViewById(R.id.seekReverbRoomSize);
        mSeekReverbDamp = mActivity.findViewById(R.id.seekReverbDamp);
        mSeekReverbWidth = mActivity.findViewById(R.id.seekReverbWidth);
        mTextReverbDry = mActivity.findViewById(R.id.textReverbDry);
        mTextReverbWet = mActivity.findViewById(R.id.textReverbWet);
        mTextReverbRoomSize = mActivity.findViewById(R.id.textReverbRoomSize);
        mTextReverbDamp = mActivity.findViewById(R.id.textReverbDamp);
        mTextReverbWidth = mActivity.findViewById(R.id.textReverbWidth);
        mBtnReverbDryMinus = mActivity.findViewById(R.id.btnReverbDryMinus);
        mBtnReverbDryPlus = mActivity.findViewById(R.id.btnReverbDryPlus);
        mBtnReverbWetMinus = mActivity.findViewById(R.id.btnReverbWetMinus);
        mBtnReverbWetPlus = mActivity.findViewById(R.id.btnReverbWetPlus);
        mBtnReverbRoomSizeMinus = mActivity.findViewById(R.id.btnReverbRoomSizeMinus);
        mBtnReverbRoomSizePlus = mActivity.findViewById(R.id.btnReverbRoomSizePlus);
        mBtnReverbDampMinus = mActivity.findViewById(R.id.btnReverbDampMinus);
        mBtnReverbDampPlus = mActivity.findViewById(R.id.btnReverbDampPlus);
        mBtnReverbWidthMinus = mActivity.findViewById(R.id.btnReverbWidthMinus);
        mBtnReverbWidthPlus = mActivity.findViewById(R.id.btnReverbWidthPlus);
        mScrollChorusCustomize = mActivity.findViewById(R.id.scrollChorusCustomize);
        mSeekChorusDry = mActivity.findViewById(R.id.seekChorusDry);
        mSeekChorusWet = mActivity.findViewById(R.id.seekChorusWet);
        mSeekChorusFeedback = mActivity.findViewById(R.id.seekChorusFeedback);
        mSeekChorusMinSweep = mActivity.findViewById(R.id.seekChorusMinSweep);
        mSeekChorusMaxSweep = mActivity.findViewById(R.id.seekChorusMaxSweep);
        mSeekChorusRate = mActivity.findViewById(R.id.seekChorusRate);
        mTextChorusDry = mActivity.findViewById(R.id.textChorusDry);
        mTextChorusWet = mActivity.findViewById(R.id.textChorusWet);
        mTextChorusFeedback = mActivity.findViewById(R.id.textChorusFeedback);
        mTextChorusMinSweep = mActivity.findViewById(R.id.textChorusMinSweep);
        mTextChorusMaxSweep = mActivity.findViewById(R.id.textChorusMaxSweep);
        mTextChorusRate = mActivity.findViewById(R.id.textChorusRate);
        mBtnChorusDryMinus = mActivity.findViewById(R.id.btnChorusDryMinus);
        mBtnChorusDryPlus = mActivity.findViewById(R.id.btnChorusDryPlus);
        mBtnChorusWetMinus = mActivity.findViewById(R.id.btnChorusWetMinus);
        mBtnChorusWetPlus = mActivity.findViewById(R.id.btnChorusWetPlus);
        mBtnChorusFeedbackMinus = mActivity.findViewById(R.id.btnChorusFeedbackMinus);
        mBtnChorusFeedbackPlus = mActivity.findViewById(R.id.btnChorusFeedbackPlus);
        mBtnChorusMinSweepMinus = mActivity.findViewById(R.id.btnChorusMinSweepMinus);
        mBtnChorusMinSweepPlus = mActivity.findViewById(R.id.btnChorusMinSweepPlus);
        mBtnChorusMaxSweepMinus = mActivity.findViewById(R.id.btnChorusMaxSweepMinus);
        mBtnChorusMaxSweepPlus = mActivity.findViewById(R.id.btnChorusMaxSweepPlus);
        mBtnChorusRateMinus = mActivity.findViewById(R.id.btnChorusRateMinus);
        mBtnChorusRatePlus = mActivity.findViewById(R.id.btnChorusRatePlus);

        mScrollDistortionCustomize = mActivity.findViewById(R.id.scrollDistortionCustomize);
        mSeekDistortionDrive = mActivity.findViewById(R.id.seekDistortionDrive);
        mSeekDistortionDry = mActivity.findViewById(R.id.seekDistortionDry);
        mSeekDistortionWet = mActivity.findViewById(R.id.seekDistortionWet);
        mSeekDistortionFeedback = mActivity.findViewById(R.id.seekDistortionFeedback);
        mSeekDistortionVolume = mActivity.findViewById(R.id.seekDistortionVolume);
        mTextDistortionDrive = mActivity.findViewById(R.id.textDistortionDrive);
        mTextDistortionDry = mActivity.findViewById(R.id.textDistortionDry);
        mTextDistortionWet = mActivity.findViewById(R.id.textDistortionWet);
        mTextDistortionFeedback = mActivity.findViewById(R.id.textDistortionFeedback);
        mTextDistortionVolume = mActivity.findViewById(R.id.textDistortionVolume);
        mBtnDistortionDriveMinus = mActivity.findViewById(R.id.btnDistortionDriveMinus);
        mBtnDistortionDrivePlus = mActivity.findViewById(R.id.btnDistortionDrivePlus);
        mBtnDistortionDryMinus = mActivity.findViewById(R.id.btnDistortionDryMinus);
        mBtnDistortionDryPlus = mActivity.findViewById(R.id.btnDistortionDryPlus);
        mBtnDistortionWetMinus = mActivity.findViewById(R.id.btnDistortionWetMinus);
        mBtnDistortionWetPlus = mActivity.findViewById(R.id.btnDistortionWetPlus);
        mBtnDistortionFeedbackMinus = mActivity.findViewById(R.id.btnDistortionFeedbackMinus);
        mBtnDistortionFeedbackPlus = mActivity.findViewById(R.id.btnDistortionFeedbackPlus);
        mBtnDistortionVolumeMinus = mActivity.findViewById(R.id.btnDistortionVolumeMinus);
        mBtnDistortionVolumePlus = mActivity.findViewById(R.id.btnDistortionVolumePlus);
        mRecyclerEffects = mActivity.findViewById(R.id.recyclerEffects);
        mRecyclerEffectTemplates = mActivity.findViewById(R.id.recyclerEffectTemplates);
        Button btnCompRandom = mActivity.findViewById(R.id.btnCompRandom);
        Button btnResetComp = mActivity.findViewById(R.id.btnResetComp);
        Button btnEchoRandom = mActivity.findViewById(R.id.btnEchoRandom);
        Button btnResetEcho = mActivity.findViewById(R.id.btnResetEcho);
        Button btnReverbRandom = mActivity.findViewById(R.id.btnReverbRandom);
        Button btnResetReverb = mActivity.findViewById(R.id.btnResetReverb);
        Button btnChorusRandom = mActivity.findViewById(R.id.btnChorusRandom);
        Button btnResetChorus = mActivity.findViewById(R.id.btnResetChorus);
        Button btnDistortionRandom = mActivity.findViewById(R.id.btnDistortionRandom);
        Button btnResetDistortion = mActivity.findViewById(R.id.btnResetDistortion);

        mSeekCompGain.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekCompThreshold.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekCompRatio.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekCompAttack.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekCompRelease.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekCompGain.setOnSeekBarChangeListener(this);
        mSeekCompThreshold.setOnSeekBarChangeListener(this);
        mSeekCompRatio.setOnSeekBarChangeListener(this);
        mSeekCompAttack.setOnSeekBarChangeListener(this);
        mSeekCompRelease.setOnSeekBarChangeListener(this);
        mSeekEchoDry.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekEchoWet.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekEchoFeedback.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekEchoDelay.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekEchoDry.setOnSeekBarChangeListener(this);
        mSeekEchoWet.setOnSeekBarChangeListener(this);
        mSeekEchoFeedback.setOnSeekBarChangeListener(this);
        mSeekEchoDelay.setOnSeekBarChangeListener(this);
        mSeekReverbDry.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekReverbWet.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekReverbRoomSize.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekReverbDamp.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekReverbWidth.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekReverbDry.setOnSeekBarChangeListener(this);
        mSeekReverbWet.setOnSeekBarChangeListener(this);
        mSeekReverbRoomSize.setOnSeekBarChangeListener(this);
        mSeekReverbDamp.setOnSeekBarChangeListener(this);
        mSeekReverbWidth.setOnSeekBarChangeListener(this);
        mSeekChorusDry.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekChorusWet.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekChorusFeedback.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekChorusMinSweep.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekChorusMaxSweep.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekChorusRate.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekChorusDry.setOnSeekBarChangeListener(this);
        mSeekChorusWet.setOnSeekBarChangeListener(this);
        mSeekChorusFeedback.setOnSeekBarChangeListener(this);
        mSeekChorusMinSweep.setOnSeekBarChangeListener(this);
        mSeekChorusMaxSweep.setOnSeekBarChangeListener(this);
        mSeekChorusRate.setOnSeekBarChangeListener(this);
        mSeekDistortionDrive.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekDistortionDry.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekDistortionWet.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekDistortionFeedback.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekDistortionVolume.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekDistortionDrive.setOnSeekBarChangeListener(this);
        mSeekDistortionDry.setOnSeekBarChangeListener(this);
        mSeekDistortionWet.setOnSeekBarChangeListener(this);
        mSeekDistortionFeedback.setOnSeekBarChangeListener(this);
        mSeekDistortionVolume.setOnSeekBarChangeListener(this);

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
        item = new EffectItem(getString(R.string.comp), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.compCustomize), true);
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
        item = new EffectItem(getString(R.string.echoCustomize), true);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.reverb), true);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.chorus), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.flanger), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.chorusCustomize), true);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.distortionStrong), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.distortionMiddle), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.distortionWeak), false);
        mEffectItems.add(item);
        item = new EffectItem(getString(R.string.distortionCustomize), true);
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

        loadData();

        mRecyclerEffects.setHasFixedSize(false);
        LinearLayoutManager effectManager = new LinearLayoutManager(mActivity);
        mRecyclerEffects.setLayoutManager(effectManager);
        mRecyclerEffects.setAdapter(mEffectsAdapter);
        ((DefaultItemAnimator) mRecyclerEffects.getItemAnimator()).setSupportsChangeAnimations(false);
        mRecyclerEffectTemplates.setHasFixedSize(false);
        LinearLayoutManager effectTemplateManager = new LinearLayoutManager(mActivity);
        mRecyclerEffectTemplates.setLayoutManager(effectTemplateManager);
        mRecyclerEffectTemplates.setAdapter(mEffectTemplatesAdapter);
        ((DefaultItemAnimator) mRecyclerEffectTemplates.getItemAnimator()).setSupportsChangeAnimations(false);
        mBtnEffectTemplateMenu.setOnClickListener(this);
        mBtnAddEffectTemplate.setOnClickListener(this);
        mTextFinishSortEffect.setOnClickListener(this);
        mBtnEffectBack.setOnClickListener(this);
        mBtnEffectTemplateOff.setOnClickListener(this);
        mBtnEffectFinish.setOnClickListener(this);
        mBtnEffectMinus.setOnClickListener(this);
        mBtnEffectMinus.setOnLongClickListener(this);
        mBtnEffectMinus.setOnTouchListener(this);
        mBtnEffectPlus.setOnClickListener(this);
        mBtnEffectPlus.setOnLongClickListener(this);
        mBtnEffectPlus.setOnTouchListener(this);
        mBtnCompGainMinus.setOnClickListener(this);
        mBtnCompGainMinus.setOnLongClickListener(this);
        mBtnCompGainMinus.setOnTouchListener(this);
        mBtnCompGainPlus.setOnClickListener(this);
        mBtnCompGainPlus.setOnLongClickListener(this);
        mBtnCompGainPlus.setOnTouchListener(this);
        mBtnCompThresholdMinus.setOnClickListener(this);
        mBtnCompThresholdMinus.setOnLongClickListener(this);
        mBtnCompThresholdMinus.setOnTouchListener(this);
        mBtnCompThresholdPlus.setOnClickListener(this);
        mBtnCompThresholdPlus.setOnLongClickListener(this);
        mBtnCompThresholdPlus.setOnTouchListener(this);
        mBtnCompRatioMinus.setOnClickListener(this);
        mBtnCompRatioMinus.setOnLongClickListener(this);
        mBtnCompRatioMinus.setOnTouchListener(this);
        mBtnCompRatioPlus.setOnClickListener(this);
        mBtnCompRatioPlus.setOnLongClickListener(this);
        mBtnCompRatioPlus.setOnTouchListener(this);
        mBtnCompAttackMinus.setOnClickListener(this);
        mBtnCompAttackMinus.setOnLongClickListener(this);
        mBtnCompAttackMinus.setOnTouchListener(this);
        mBtnCompAttackPlus.setOnClickListener(this);
        mBtnCompAttackPlus.setOnLongClickListener(this);
        mBtnCompAttackPlus.setOnTouchListener(this);
        mBtnCompReleaseMinus.setOnClickListener(this);
        mBtnCompReleaseMinus.setOnLongClickListener(this);
        mBtnCompReleaseMinus.setOnTouchListener(this);
        mBtnCompReleasePlus.setOnClickListener(this);
        mBtnCompReleasePlus.setOnLongClickListener(this);
        mBtnCompReleasePlus.setOnTouchListener(this);
        btnCompRandom.setOnClickListener(this);
        btnResetComp.setOnClickListener(this);
        mBtnEchoDryMinus.setOnClickListener(this);
        mBtnEchoDryMinus.setOnLongClickListener(this);
        mBtnEchoDryMinus.setOnTouchListener(this);
        mBtnEchoDryPlus.setOnClickListener(this);
        mBtnEchoDryPlus.setOnLongClickListener(this);
        mBtnEchoDryPlus.setOnTouchListener(this);
        mBtnEchoWetMinus.setOnClickListener(this);
        mBtnEchoWetMinus.setOnLongClickListener(this);
        mBtnEchoWetMinus.setOnTouchListener(this);
        mBtnEchoWetPlus.setOnClickListener(this);
        mBtnEchoWetPlus.setOnLongClickListener(this);
        mBtnEchoWetPlus.setOnTouchListener(this);
        mBtnEchoFeedbackMinus.setOnClickListener(this);
        mBtnEchoFeedbackMinus.setOnLongClickListener(this);
        mBtnEchoFeedbackMinus.setOnTouchListener(this);
        mBtnEchoFeedbackPlus.setOnClickListener(this);
        mBtnEchoFeedbackPlus.setOnLongClickListener(this);
        mBtnEchoFeedbackPlus.setOnTouchListener(this);
        mBtnEchoDelayMinus.setOnClickListener(this);
        mBtnEchoDelayMinus.setOnLongClickListener(this);
        mBtnEchoDelayMinus.setOnTouchListener(this);
        mBtnEchoDelayPlus.setOnClickListener(this);
        mBtnEchoDelayPlus.setOnLongClickListener(this);
        mBtnEchoDelayPlus.setOnTouchListener(this);
        btnEchoRandom.setOnClickListener(this);
        btnResetEcho.setOnClickListener(this);
        mBtnReverbDryMinus.setOnClickListener(this);
        mBtnReverbDryMinus.setOnLongClickListener(this);
        mBtnReverbDryMinus.setOnTouchListener(this);
        mBtnReverbDryPlus.setOnClickListener(this);
        mBtnReverbDryPlus.setOnLongClickListener(this);
        mBtnReverbDryPlus.setOnTouchListener(this);
        mBtnReverbWetMinus.setOnClickListener(this);
        mBtnReverbWetMinus.setOnLongClickListener(this);
        mBtnReverbWetMinus.setOnTouchListener(this);
        mBtnReverbWetPlus.setOnClickListener(this);
        mBtnReverbWetPlus.setOnLongClickListener(this);
        mBtnReverbWetPlus.setOnTouchListener(this);
        mBtnReverbRoomSizeMinus.setOnClickListener(this);
        mBtnReverbRoomSizeMinus.setOnLongClickListener(this);
        mBtnReverbRoomSizeMinus.setOnTouchListener(this);
        mBtnReverbRoomSizePlus.setOnClickListener(this);
        mBtnReverbRoomSizePlus.setOnLongClickListener(this);
        mBtnReverbRoomSizePlus.setOnTouchListener(this);
        mBtnReverbDampMinus.setOnClickListener(this);
        mBtnReverbDampMinus.setOnLongClickListener(this);
        mBtnReverbDampMinus.setOnTouchListener(this);
        mBtnReverbDampPlus.setOnClickListener(this);
        mBtnReverbDampPlus.setOnLongClickListener(this);
        mBtnReverbDampPlus.setOnTouchListener(this);
        mBtnReverbWidthMinus.setOnClickListener(this);
        mBtnReverbWidthMinus.setOnLongClickListener(this);
        mBtnReverbWidthMinus.setOnTouchListener(this);
        mBtnReverbWidthPlus.setOnClickListener(this);
        mBtnReverbWidthPlus.setOnLongClickListener(this);
        mBtnReverbWidthPlus.setOnTouchListener(this);
        btnReverbRandom.setOnClickListener(this);
        btnResetReverb.setOnClickListener(this);
        mBtnReverbSaveAs.setOnClickListener(this);
        mBtnChorusDryMinus.setOnClickListener(this);
        mBtnChorusDryMinus.setOnLongClickListener(this);
        mBtnChorusDryMinus.setOnTouchListener(this);
        mBtnChorusDryPlus.setOnClickListener(this);
        mBtnChorusDryPlus.setOnLongClickListener(this);
        mBtnChorusDryPlus.setOnTouchListener(this);
        mBtnChorusWetMinus.setOnClickListener(this);
        mBtnChorusWetMinus.setOnLongClickListener(this);
        mBtnChorusWetMinus.setOnTouchListener(this);
        mBtnChorusWetPlus.setOnClickListener(this);
        mBtnChorusWetPlus.setOnLongClickListener(this);
        mBtnChorusWetPlus.setOnTouchListener(this);
        mBtnChorusFeedbackMinus.setOnClickListener(this);
        mBtnChorusFeedbackMinus.setOnLongClickListener(this);
        mBtnChorusFeedbackMinus.setOnTouchListener(this);
        mBtnChorusFeedbackPlus.setOnClickListener(this);
        mBtnChorusFeedbackPlus.setOnLongClickListener(this);
        mBtnChorusFeedbackPlus.setOnTouchListener(this);
        mBtnChorusMinSweepMinus.setOnClickListener(this);
        mBtnChorusMinSweepMinus.setOnLongClickListener(this);
        mBtnChorusMinSweepMinus.setOnTouchListener(this);
        mBtnChorusMinSweepPlus.setOnClickListener(this);
        mBtnChorusMinSweepPlus.setOnLongClickListener(this);
        mBtnChorusMinSweepPlus.setOnTouchListener(this);
        mBtnChorusMaxSweepMinus.setOnClickListener(this);
        mBtnChorusMaxSweepMinus.setOnLongClickListener(this);
        mBtnChorusMaxSweepMinus.setOnTouchListener(this);
        mBtnChorusMaxSweepPlus.setOnClickListener(this);
        mBtnChorusMaxSweepPlus.setOnLongClickListener(this);
        mBtnChorusMaxSweepPlus.setOnTouchListener(this);
        mBtnChorusRateMinus.setOnClickListener(this);
        mBtnChorusRateMinus.setOnLongClickListener(this);
        mBtnChorusRateMinus.setOnTouchListener(this);
        mBtnChorusRatePlus.setOnClickListener(this);
        mBtnChorusRatePlus.setOnLongClickListener(this);
        mBtnChorusRatePlus.setOnTouchListener(this);
        btnChorusRandom.setOnClickListener(this);
        btnResetChorus.setOnClickListener(this);
        mBtnDistortionDriveMinus.setOnClickListener(this);
        mBtnDistortionDriveMinus.setOnLongClickListener(this);
        mBtnDistortionDriveMinus.setOnTouchListener(this);
        mBtnDistortionDrivePlus.setOnClickListener(this);
        mBtnDistortionDrivePlus.setOnLongClickListener(this);
        mBtnDistortionDrivePlus.setOnTouchListener(this);
        mBtnDistortionDryMinus.setOnClickListener(this);
        mBtnDistortionDryMinus.setOnLongClickListener(this);
        mBtnDistortionDryMinus.setOnTouchListener(this);
        mBtnDistortionDryPlus.setOnClickListener(this);
        mBtnDistortionDryPlus.setOnLongClickListener(this);
        mBtnDistortionDryPlus.setOnTouchListener(this);
        mBtnDistortionWetMinus.setOnClickListener(this);
        mBtnDistortionWetMinus.setOnLongClickListener(this);
        mBtnDistortionWetMinus.setOnTouchListener(this);
        mBtnDistortionWetPlus.setOnClickListener(this);
        mBtnDistortionWetPlus.setOnLongClickListener(this);
        mBtnDistortionWetPlus.setOnTouchListener(this);
        mBtnDistortionFeedbackMinus.setOnClickListener(this);
        mBtnDistortionFeedbackMinus.setOnLongClickListener(this);
        mBtnDistortionFeedbackMinus.setOnTouchListener(this);
        mBtnDistortionFeedbackPlus.setOnClickListener(this);
        mBtnDistortionFeedbackPlus.setOnLongClickListener(this);
        mBtnDistortionFeedbackPlus.setOnTouchListener(this);
        mBtnDistortionVolumeMinus.setOnClickListener(this);
        mBtnDistortionVolumeMinus.setOnLongClickListener(this);
        mBtnDistortionVolumeMinus.setOnTouchListener(this);
        mBtnDistortionVolumePlus.setOnClickListener(this);
        mBtnDistortionVolumePlus.setOnLongClickListener(this);
        mBtnDistortionVolumePlus.setOnTouchListener(this);
        btnDistortionRandom.setOnClickListener(this);
        btnResetDistortion.setOnClickListener(this);

        mEditTimeEffectDetail.setOnFocusChangeListener(this);
        mEditSpeedEffectDetail.setOnFocusChangeListener(this);

        mEffectItems.get(0).setSelected(true);
        mEffectsAdapter.notifyItemChanged(0);

        setCompGain(200, false);
        setCompThreshold(4000, false);
        setCompRatio(900, false);
        setCompAttack(119, false);
        setCompRelease(39999, false);
        setEchoDry(100, false);
        setEchoWet(30, false);
        setEchoFeedback(60, false);
        setEchoDelay(8, false);
        setReverb(70, 100, 85, 50, 90, false);
        setChorusDry(100, false);
        setChorusWet(10, false);
        setChorusFeedback(50, false);
        setChorusMinSweep(100, false);
        setChorusMaxSweep(200, false);
        setChorusRate(1000, false);
        setDistortionDrive(20, false);
        setDistortionDry(95, false);
        setDistortionWet(5, false);
        setDistortionFeedback(10, false);
        setDistortionVolume(100, false);
    }

    private void loadData()
    {
        SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        ArrayList<EffectTemplateItem> effectTemplateItems = gson.fromJson(preferences.getString("mReverbItems",""), new TypeToken<ArrayList<EffectTemplateItem>>(){}.getType());
        if(effectTemplateItems != null) setEffectTemplateItems(effectTemplateItems);
        else resetReverbs();
        mBtnEffectTemplateOff.setSelected(true);
        for(int i = 0; i < mReverbItems.size(); i++)
            mReverbItems.get(i).setSelected(false);
    }

    private void saveData()
    {
        SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        preferences.edit().putString("mReverbItems", gson.toJson(mReverbItems)).apply();
    }

    private void resetReverbs()
    {
        if(mReverbItems.size() > 0) mReverbItems.clear();

        mReverbItems.add(new EffectTemplateItem(getString(R.string.bathroom), new ArrayList<>(Arrays.asList(1.0f, 2.0f, 0.16f, 0.5f, 1.0f))));
        mReverbItems.add(new EffectTemplateItem(getString(R.string.smallRoom), new ArrayList<>(Arrays.asList(0.95f, 0.99f, 0.3f, 0.5f, 1.0f))));
        mReverbItems.add(new EffectTemplateItem(getString(R.string.mediumRoom), new ArrayList<>(Arrays.asList(0.95f, 0.99f, 0.75f, 0.5f, 0.7f))));
        mReverbItems.add(new EffectTemplateItem(getString(R.string.largeRoom), new ArrayList<>(Arrays.asList(0.7f, 1.0f, 0.85f, 0.5f, 0.9f))));
        mReverbItems.add(new EffectTemplateItem(getString(R.string.church), new ArrayList<>(Arrays.asList(0.4f, 1.0f, 0.9f, 0.5f, 1.0f))));
        mReverbItems.add(new EffectTemplateItem(getString(R.string.cathedral), new ArrayList<>(Arrays.asList(0.0f, 1.0f, 0.9f, 0.5f, 1.0f))));

        saveData();
        mEffectTemplatesAdapter.notifyDataSetChanged();
        resetReverb();
    }

    public void onEffectItemClick(int nEffect)
    {
        if(nEffect < 0 || mEffectItems.size() <= nEffect) return;
        EffectItem item = mEffectItems.get(nEffect);

        if(!item.isSelected() && nEffect == EFFECTTYPE_REVERB) {
            onEffectDetailClick(nEffect);
            return;
        }

        if(item.isSelected()) deselectEffect(nEffect);
        else item.setSelected(true);
        mEffectsAdapter.notifyItemChanged(nEffect);
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
        mEffectsAdapter.notifyItemChanged(0);
        mActivity.playlistFragment.updateSavingEffect();
    }

    public void onEffectTemplateItemClick(int nEffectTemplate)
    {
        boolean bAlreadySelected = false;
        EffectTemplateItem item;
        if(mEffectDetail == EFFECTTYPE_REVERB) {
            item = mReverbItems.get(nEffectTemplate);
            if(item.isSelected()) bAlreadySelected = true;
            for(int i = 0; i < mReverbItems.size(); i++) {
                if (i != nEffectTemplate) mReverbItems.get(i).setSelected(false);
                else mReverbItems.get(i).setSelected(!bAlreadySelected);
            }
            if(bAlreadySelected) mReverbSelected = -1;
            else mReverbSelected = nEffectTemplate;
        }
        else return;
        mBtnEffectTemplateOff.setSelected(bAlreadySelected);
        mEffectTemplatesAdapter.notifyDataSetChanged();

        EffectItem effectItem = mEffectItems.get(mEffectDetail);
        if(bAlreadySelected) deselectEffect(mEffectDetail);
        else {
            effectItem.setSelected(true);
            checkDuplicate(mEffectDetail);
        }
        boolean bSelected = false;
        for(int i = 0; i < mEffectItems.size(); i++) {
            if(mEffectItems.get(i).isSelected()) bSelected = true;
        }
        if(!bSelected) mEffectItems.get(0).setSelected(true);
        mActivity.playlistFragment.updateSavingEffect();
        mEffectsAdapter.notifyItemChanged(mEffectDetail);

        if(mReverbSelected == -1) resetReverb();
        else {
            ArrayList<Float> arFloats = mReverbItems.get(nEffectTemplate).getArPresets();
            setReverb(arFloats.get(0), arFloats.get(1), arFloats.get(2), arFloats.get(3), arFloats.get(4), true);
        }
    }

    public void resetEffect()
    {
        EffectItem item = mEffectItems.get(0);
        item.setSelected(true);
        mEffectsAdapter.notifyItemChanged(0);
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
            mEffectsAdapter.notifyItemChanged(i);
        }
        mPan = 0.0f;
        mFreq = 1.0f;
        setTimeOfIncreaseSpeed(1.0f);
        setIncreaseSpeed(0.1f);
        setTimeOfDecreaseSpeed(1.0f);
        setDecreaseSpeed(0.1f);
        setCompGain(200, false);
        setCompThreshold(4000, false);
        setCompRatio(900, false);
        setCompAttack(119, false);
        setCompRelease(39999, false);
        setEchoDry(100, false);
        setEchoWet(30, false);
        setEchoFeedback(60, false);
        setEchoDelay(8, false);
        resetReverb();
        setChorusDry(100, false);
        setChorusWet(10, false);
        setChorusFeedback(50, false);
        setChorusMinSweep(100, false);
        setChorusMaxSweep(200, false);
        setChorusRate(1000, false);
        setDistortionDrive(20, false);
        setDistortionDry(95, false);
        setDistortionWet(5, false);
        setDistortionFeedback(10, false);
        setDistortionVolume(100, false);
    }

    private void setCompRandom()
    {
        setCompGain(getRandomValue(0, mSeekCompGain.getMax()), true);
        setCompThreshold(getRandomValue(0, mSeekCompThreshold.getMax()), true);
        setCompRatio(getRandomValue(0, mSeekCompRatio.getMax()), true);
        setCompAttack(getRandomValue(0, mSeekCompAttack.getMax()), true);
        setCompRelease(getRandomValue(0, mSeekCompRelease.getMax()), true);
    }

    private void resetComp()
    {
        setCompGain(200, true);
        setCompThreshold(4000, true);
        setCompRatio(900, true);
        setCompAttack(119, true);
        setCompRelease(39999, true);
    }

    private void setEchoRandom()
    {
        int nDry = getRandomValue(50, 100);
        setEchoDry(nDry, true);
        int nWet;
        while(true) {
            nWet = getRandomValue(10, 100);
            if(nWet <= nDry) break;
        }
        setEchoWet(nWet, true);
        setEchoFeedback(getRandomValue(0, mSeekEchoFeedback.getMax()), true);
        setEchoDelay(getRandomValue(0, 50), true);
    }

    private void resetEcho()
    {
        setEchoDry(100, true);
        setEchoWet(30, true);
        setEchoFeedback(60, true);
        setEchoDelay(8, true);
    }

    private int getRandomValue(int nMin, int nMax)
    {
        Random random = new Random();
        return random.nextInt(nMax - nMin) + nMin;
    }

    private void setReverbRandom() {
        setReverb(getRandomValue(0, mSeekReverbDry.getMax()),
            getRandomValue(0, mSeekReverbWet.getMax()),
            getRandomValue(0, mSeekReverbRoomSize.getMax()),
            getRandomValue(0, mSeekReverbDamp.getMax()),
            getRandomValue(0, mSeekReverbWidth.getMax()),
            true
        );
    }

    private void resetReverb() {
        mEffectItems.get(EFFECTTYPE_REVERB).setSelected(false);
        boolean bSelected = false;
        for(int i = 0; i < mEffectItems.size(); i++) {
            if(mEffectItems.get(i).isSelected()) bSelected = true;
        }
        if(!bSelected) mEffectItems.get(0).setSelected(true);
        mBtnEffectTemplateOff.setSelected(true);
        for(int i = 0; i < mReverbItems.size(); i++) mReverbItems.get(i).setSelected(false);

        mReverbSelected = -1;
        mEffectsAdapter.notifyDataSetChanged();
        mEffectTemplatesAdapter.notifyDataSetChanged();

        BASS.BASS_ChannelRemoveFX(MainActivity.sStream, mFxReverb);
        mFxReverb = 0;

        setReverb(70, 100, 85, 50, 90, true);
    }

    private void setChorusRandom()
    {
        setChorusDry(getRandomValue(50, 100), true);
        setChorusWet(getRandomValue(10, 50), true);
        setChorusFeedback(getRandomValue(0, mSeekChorusFeedback.getMax()), true);
        int nMaxSweep = getRandomValue(0, mSeekChorusMaxSweep.getMax());
        setChorusMaxSweep(nMaxSweep, true);
        int nMinSweep;
        while(true) {
            nMinSweep = getRandomValue(0, mSeekChorusMinSweep.getMax());
            if(nMinSweep <= nMaxSweep) break;
        }
        setChorusMinSweep(nMinSweep, true);
        setChorusRate(getRandomValue(0, mSeekChorusRate.getMax()), true);
    }

    private void resetChorus()
    {
        setChorusDry(100, true);
        setChorusWet(10, true);
        setChorusFeedback(50, true);
        setChorusMinSweep(100, true);
        setChorusMaxSweep(200, true);
        setChorusRate(1000, true);
    }

    private void setDistortionRandom()
    {
    	setDistortionDrive(getRandomValue(0, mSeekDistortionDrive.getMax()), true);
    	int nDry = getRandomValue(50, 100);
        setDistortionDry(nDry, true);
        int nWet;
        while(true) {
            nWet = getRandomValue(10, 100);
            if(nWet <= nDry) break;
        }
        setDistortionWet(nWet, true);
        setDistortionFeedback(getRandomValue(0, mSeekDistortionFeedback.getMax()), true);
        setDistortionVolume(getRandomValue(80, 120), true);
    }

    private void resetDistortion()
    {
        setDistortionDrive(20, true);
        setDistortionDry(95, true);
        setDistortionWet(5, true);
        setDistortionFeedback(10, true);
        setDistortionVolume(100, true);
    }

    public void onEffectDetailClick(int nEffect)
    {
        mEffectDetail = nEffect;
        EffectItem item = mEffectItems.get(nEffect);
        if(!item.isSelected()) {
            if(mEffectDetail == EFFECTTYPE_REVERB) {
                if(mReverbSelected != -1) onEffectItemClick(nEffect);
            }
            else onEffectItemClick(nEffect);
        }

        mTextEffectName.setText(mEffectItems.get(nEffect).getEffectName());
        mSeekEffectDetail.setOnSeekBarChangeListener(null);
        if(nEffect == EFFECTTYPE_PAN)
        {
            mTextEffectLabel.setText(R.string.pan);
            mTextEffectLabel.setVisibility(View.VISIBLE);
            int nPan = (int)(mPan * 100.0f);
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", nPan));
            // SeekBarについてはAPIエベル26以降しか最小値を設定できない為、最大値に200を設定（本来は-100～100にしたい）
            mSeekEffectDetail.setMax(200);
            mSeekEffectDetail.setProgress(nPan + 100);
        }
        else if(nEffect == EFFECTTYPE_FREQUENCY)
        {
            mTextEffectLabel.setText(R.string.frequency);
            mTextEffectLabel.setVisibility(View.VISIBLE);
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%.1f", mFreq));
            // SeekBarについてはAPIエベル26以降しか最小値を設定できない為、最大値に39を設定（本来は1～40にしたい）
            mSeekEffectDetail.setMax(39);
            mSeekEffectDetail.setProgress((int)(mFreq * 10.0f) - 1);
        }
        else if(nEffect == EFFECTTYPE_INCREASESPEED)
        {
            mTextEffectLabel.setText(R.string.incSpeedTitle);
            mTextEffectLabel.setVisibility(View.VISIBLE);
            mEditTimeEffectDetail.setText(String.format(Locale.getDefault(), "%.1f%s", mTimeOfIncreaseSpeed, getString(R.string.sec)));
            mEditSpeedEffectDetail.setText(String.format(Locale.getDefault(), "%.1f%%", mIncreaseSpeed));
        }
        else if(nEffect == EFFECTTYPE_DECREASESPEED)
        {
            mTextEffectLabel.setText(R.string.decSpeedTitle);
            mTextEffectLabel.setVisibility(View.VISIBLE);
            mEditTimeEffectDetail.setText(String.format(Locale.getDefault(), "%.1f%s", mTimeOfDecreaseSpeed, getString(R.string.sec)));
            mEditSpeedEffectDetail.setText(String.format(Locale.getDefault(), "%.1f%%", mDecreaseSpeed));
        }
        else if(nEffect == EFFECTTYPE_METRONOME)
        {
            mTextEffectLabel.setText(R.string.BPM);
            mTextEffectLabel.setVisibility(View.VISIBLE);
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", mBpm));
            mSeekEffectDetail.setProgress(0);
            // SeekBarについてはAPIエベル26以降しか最小値を設定できない為、最大値に290を設定（本来は10～300にしたい）
            mSeekEffectDetail.setMax(290);
            mSeekEffectDetail.setProgress(mBpm - 10);
        }
        else if(nEffect == EFFECTTYPE_RECORDNOISE)
        {
            mTextEffectLabel.setText(R.string.volume);
            mTextEffectLabel.setVisibility(View.VISIBLE);
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", (int)(mVol1 * 100)));
            mSeekEffectDetail.setMax(100);
            mSeekEffectDetail.setProgress((int)(mVol1 * 100));
        }
        else if(nEffect == EFFECTTYPE_ROAROFWAVES)
        {
            mTextEffectLabel.setText(R.string.volume);
            mTextEffectLabel.setVisibility(View.VISIBLE);
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", (int)(mVol2 * 100)));
            mSeekEffectDetail.setMax(100);
            mSeekEffectDetail.setProgress((int)(mVol2 * 100));
        }
        else if(nEffect == EFFECTTYPE_RAIN)
        {
            mTextEffectLabel.setText(R.string.volume);
            mTextEffectLabel.setVisibility(View.VISIBLE);
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", (int)(mVol3 * 100)));
            mSeekEffectDetail.setMax(100);
            mSeekEffectDetail.setProgress((int)(mVol3 * 100));
        }
        else if(nEffect == EFFECTTYPE_RIVER)
        {
            mTextEffectLabel.setText(R.string.volume);
            mTextEffectLabel.setVisibility(View.VISIBLE);
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", (int)(mVol4 * 100)));
            mSeekEffectDetail.setMax(100);
            mSeekEffectDetail.setProgress((int)(mVol4 * 100));
        }
        else if(nEffect == EFFECTTYPE_WAR)
        {
            mTextEffectLabel.setText(R.string.volume);
            mTextEffectLabel.setVisibility(View.VISIBLE);
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", (int)(mVol5 * 100)));
            mSeekEffectDetail.setMax(100);
            mSeekEffectDetail.setProgress((int)(mVol5 * 100));
        }
        else if(nEffect == EFFECTTYPE_FIRE)
        {
            mTextEffectLabel.setText(R.string.volume);
            mTextEffectLabel.setVisibility(View.VISIBLE);
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", (int)(mVol6 * 100)));
            mSeekEffectDetail.setMax(100);
            mSeekEffectDetail.setProgress((int)(mVol6 * 100));
        }
        else if(nEffect == EFFECTTYPE_CONCERTHALL)
        {
            mTextEffectLabel.setText(R.string.volume);
            mTextEffectLabel.setVisibility(View.VISIBLE);
            mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", (int)(mVol7 * 100)));
            mSeekEffectDetail.setMax(100);
            mSeekEffectDetail.setProgress((int)(mVol7 * 100));
        }
        else mTextEffectLabel.setVisibility(View.INVISIBLE);

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
            mScrollCompCustomize.setVisibility(View.GONE);
            mScrollEchoCustomize.setVisibility(View.GONE);
            mScrollReverbCustomize.setVisibility(View.GONE);
            mScrollChorusCustomize.setVisibility(View.GONE);
            mScrollDistortionCustomize.setVisibility(View.GONE);
        }
        else if(nEffect == EFFECTTYPE_COMP_CUSTOMIZE) {
            mRelativeSliderEffectDatail.setVisibility(View.GONE);
            mRelativeRollerEffectDetail.setVisibility(View.GONE);
            mScrollCompCustomize.setVisibility(View.VISIBLE);
            mScrollEchoCustomize.setVisibility(View.GONE);
            mScrollReverbCustomize.setVisibility(View.GONE);
            mScrollChorusCustomize.setVisibility(View.GONE);
            mScrollDistortionCustomize.setVisibility(View.GONE);
        }
        else if(nEffect == EFFECTTYPE_ECHO_CUSTOMIZE) {
            mRelativeSliderEffectDatail.setVisibility(View.GONE);
            mRelativeRollerEffectDetail.setVisibility(View.GONE);
            mScrollCompCustomize.setVisibility(View.GONE);
            mScrollEchoCustomize.setVisibility(View.VISIBLE);
            mScrollReverbCustomize.setVisibility(View.GONE);
            mScrollChorusCustomize.setVisibility(View.GONE);
            mScrollDistortionCustomize.setVisibility(View.GONE);
        }
        else if(nEffect == EFFECTTYPE_REVERB) {
            for(int i = 0; i < mReverbItems.size(); i++) mReverbItems.get(i).setSelected(false);
            if(mReverbSelected == -1) resetReverb();
            else onEffectTemplateItemClick(mReverbSelected);
            mEffectTemplatesAdapter.notifyDataSetChanged();
            mRelativeEffectTemplates.setVisibility(View.VISIBLE);
        }
        else if(nEffect == EFFECTTYPE_CHORUS_CUSTOMIZE) {
            mRelativeSliderEffectDatail.setVisibility(View.GONE);
            mRelativeRollerEffectDetail.setVisibility(View.GONE);
            mScrollCompCustomize.setVisibility(View.GONE);
            mScrollEchoCustomize.setVisibility(View.GONE);
            mScrollReverbCustomize.setVisibility(View.GONE);
            mScrollChorusCustomize.setVisibility(View.VISIBLE);
            mScrollDistortionCustomize.setVisibility(View.GONE);
        }
        else if(nEffect == EFFECTTYPE_DISTORTION_CUSTOMIZE) {
            mRelativeSliderEffectDatail.setVisibility(View.GONE);
            mRelativeRollerEffectDetail.setVisibility(View.GONE);
            mScrollCompCustomize.setVisibility(View.GONE);
            mScrollEchoCustomize.setVisibility(View.GONE);
            mScrollReverbCustomize.setVisibility(View.GONE);
            mScrollChorusCustomize.setVisibility(View.GONE);
            mScrollDistortionCustomize.setVisibility(View.VISIBLE);
        }
        else {
            mRelativeSliderEffectDatail.setVisibility(View.VISIBLE);
            mRelativeRollerEffectDetail.setVisibility(View.GONE);
            mScrollCompCustomize.setVisibility(View.GONE);
            mScrollEchoCustomize.setVisibility(View.GONE);
            mScrollReverbCustomize.setVisibility(View.GONE);
            mScrollChorusCustomize.setVisibility(View.GONE);
            mScrollDistortionCustomize.setVisibility(View.GONE);
            mSeekEffectDetail.setOnSeekBarChangeListener(this);
        }

        mRelativeEffectDetail.setVisibility(View.VISIBLE);
        mRecyclerEffects.setVisibility(View.INVISIBLE);
    }

    public void onEffectCustomizeClick(int nTemplate) {
        mAddTemplate = false;

        if(!isSelectedTemplateItem(nTemplate)) onEffectTemplateItemClick(nTemplate);

        if(mEffectDetail == EFFECTTYPE_REVERB) {
            EffectTemplateItem item = mReverbItems.get(nTemplate);
            mTextEffectName.setText(item.getEffectTemplateName());

            mBtnEffectBack.setText(R.string.back);
            mBtnEffectFinish.setText(R.string.done);

            mBtnEffectFinish.setVisibility(View.VISIBLE);
            mRelativeEffectTemplates.setVisibility(View.INVISIBLE);
            mScrollReverbCustomize.setVisibility(View.VISIBLE);
            mImgEffectBack.setVisibility(View.VISIBLE);
            mBtnEffectBack.setPadding((int)(32 * mActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
            mBtnReverbSaveAs.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch)
    {
        if(seekBar.getId() == R.id.seekEffectDetail) {
            if (mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_PAN).getEffectName())) {
                float mPan = (progress - 100) / 100.0f;
                mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", progress - 100));
                setPan(mPan);
            } else if (mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_FREQUENCY).getEffectName())) {
                double dProgress = (double) (progress + 1) / 10.0;
                mTextEffectDetail.setText(String.format(Locale.getDefault(), "%.1f", dProgress));
                setFreq((float) dProgress);
            } else if (mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_METRONOME).getEffectName())) {
                mBpm = progress + 10;
                mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", mBpm));
                applyEffect();
            } else if (mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_RECORDNOISE).getEffectName())) {
                mVol1 = progress / 100.0f;
                mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", progress));
                applyEffect();
                if (mEffectItems.get(EFFECTTYPE_RECORDNOISE).isSelected()) {
                    int hSETemp = mSE1PlayingFlag ? mSEStream : mSEStream2;
                    BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, mVol1);
                }
            } else if (mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_ROAROFWAVES).getEffectName())) {
                mVol2 = progress / 100.0f;
                mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", progress));
                applyEffect();
                if (mEffectItems.get(EFFECTTYPE_ROAROFWAVES).isSelected()) {
                    int hSETemp = mSE1PlayingFlag ? mSEStream : mSEStream2;
                    BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, mVol2);
                }
            } else if (mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_RAIN).getEffectName())) {
                mVol3 = progress / 100.0f;
                mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", progress));
                applyEffect();
                if (mEffectItems.get(EFFECTTYPE_RAIN).isSelected()) {
                    int hSETemp = mSE1PlayingFlag ? mSEStream : mSEStream2;
                    BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, mVol3);
                }
            } else if (mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_RIVER).getEffectName())) {
                mVol4 = progress / 100.0f;
                mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", progress));
                applyEffect();
                if (mEffectItems.get(EFFECTTYPE_RIVER).isSelected()) {
                    int hSETemp = mSE1PlayingFlag ? mSEStream : mSEStream2;
                    BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, mVol4);
                }
            } else if (mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_WAR).getEffectName())) {
                mVol5 = progress / 100.0f;
                mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", progress));
                applyEffect();
                if (mEffectItems.get(EFFECTTYPE_WAR).isSelected()) {
                    int hSETemp = mSE1PlayingFlag ? mSEStream : mSEStream2;
                    BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, mVol5);
                }
            } else if (mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_FIRE).getEffectName())) {
                mVol6 = progress / 100.0f;
                mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", progress));
                applyEffect();
                if (mEffectItems.get(EFFECTTYPE_FIRE).isSelected()) {
                    int hSETemp = mSE1PlayingFlag ? mSEStream : mSEStream2;
                    BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, mVol6);
                }
            } else if (mTextEffectName.getText().toString().equals(mEffectItems.get(EFFECTTYPE_CONCERTHALL).getEffectName())) {
                mVol7 = progress / 100.0f;
                mTextEffectDetail.setText(String.format(Locale.getDefault(), "%d", progress));
                applyEffect();
                if (mEffectItems.get(EFFECTTYPE_CONCERTHALL).isSelected()) {
                    int hSETemp = mSE1PlayingFlag ? mSEStream : mSEStream2;
                    BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, mVol7);
                }
            }
            mActivity.playlistFragment.updateSavingEffect();
        }
        else if(seekBar.getId() == R.id.seekCompGain) setCompGain(progress, fromTouch);
        else if(seekBar.getId() == R.id.seekCompThreshold) setCompThreshold(progress, fromTouch);
        else if(seekBar.getId() == R.id.seekCompRatio) setCompRatio(progress, fromTouch);
        else if(seekBar.getId() == R.id.seekCompAttack) setCompAttack(progress, fromTouch);
        else if(seekBar.getId() == R.id.seekCompRelease) setCompRelease(progress, fromTouch);
        else if(seekBar.getId() == R.id.seekEchoDry) setEchoDry(progress, fromTouch);
        else if(seekBar.getId() == R.id.seekEchoWet) setEchoWet(progress, fromTouch);
        else if(seekBar.getId() == R.id.seekEchoFeedback) setEchoFeedback(progress, fromTouch);
        else if(seekBar.getId() == R.id.seekEchoDelay) setEchoDelay(progress, fromTouch);
        else if(seekBar.getId() == R.id.seekReverbDry)
            setReverb(progress, mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress(), fromTouch);
        else if(seekBar.getId() == R.id.seekReverbWet)
            setReverb(mSeekReverbDry.getProgress(), progress, mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress(), fromTouch);
        else if(seekBar.getId() == R.id.seekReverbRoomSize)
            setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress(), progress, mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress(), fromTouch);
        else if(seekBar.getId() == R.id.seekReverbDamp)
            setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress(), progress, mSeekReverbWidth.getProgress(), fromTouch);
        else if(seekBar.getId() == R.id.seekReverbWidth)
            setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress(), progress, fromTouch);
        else if(seekBar.getId() == R.id.seekChorusDry) setChorusDry(progress, fromTouch);
        else if(seekBar.getId() == R.id.seekChorusWet) setChorusWet(progress, fromTouch);
        else if(seekBar.getId() == R.id.seekChorusFeedback) setChorusFeedback(progress, fromTouch);
        else if(seekBar.getId() == R.id.seekChorusMinSweep) setChorusMinSweep(progress, fromTouch);
        else if(seekBar.getId() == R.id.seekChorusMaxSweep) setChorusMaxSweep(progress, fromTouch);
        else if(seekBar.getId() == R.id.seekChorusRate) setChorusRate(progress, fromTouch);
        else if(seekBar.getId() == R.id.seekDistortionDrive) setDistortionDrive(progress, fromTouch);
        else if(seekBar.getId() == R.id.seekDistortionDry) setDistortionDry(progress, fromTouch);
        else if(seekBar.getId() == R.id.seekDistortionWet) setDistortionWet(progress, fromTouch);
        else if(seekBar.getId() == R.id.seekDistortionFeedback) setDistortionFeedback(progress, fromTouch);
        else if(seekBar.getId() == R.id.seekDistortionVolume) setDistortionVolume(progress, fromTouch);
    }

    private void updateComp()
    {
        if(!mEffectItems.get(EFFECTTYPE_COMP_CUSTOMIZE).isSelected() || MainActivity.sStream == 0)
            return;
        if(mFxComp == 0) mFxComp = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_COMPRESSOR2, 2);
        BASS_FX.BASS_BFX_COMPRESSOR2 p = new BASS_FX.BASS_BFX_COMPRESSOR2();
        p.fGain = mCompGain;
        p.fThreshold = mCompThreshold;
        p.fRatio = mCompRatio;
        p.fAttack = mCompAttack;
        p.fRelease = mCompRelease;
        p.lChannel = BASS_FX.BASS_BFX_CHANALL;
        BASS.BASS_FXSetParameters(mFxComp, p);
    }

    public void setCompGain(int nValue, boolean bSave)
    {
        mCompGain = nValue / 100.0f;
        mTextCompGain.setText(String.format(Locale.getDefault(), "%.2f", mCompGain));
        mSeekCompGain.setProgress(nValue);
        updateComp();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setCompGain(float fValue, boolean bSave)
    {
        mCompGain = fValue;
        mTextCompGain.setText(String.format(Locale.getDefault(), "%.2f", mCompGain));
        mSeekCompGain.setProgress((int)(fValue * 100.0f));
        updateComp();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setCompThreshold(int nValue, boolean bSave)
    {
        mCompThreshold = (nValue - 6000) / 100.0f;
        mTextCompThreshold.setText(String.format(Locale.getDefault(), "%.2f", mCompThreshold));
        mSeekCompThreshold.setProgress(nValue);
        updateComp();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setCompThreshold(float fValue, boolean bSave)
    {
        mCompThreshold = fValue;
        mTextCompThreshold.setText(String.format(Locale.getDefault(), "%.2f", mCompThreshold));
        mSeekCompThreshold.setProgress((int)(fValue * 100.0f) + 6000);
        updateComp();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setCompRatio(int nValue, boolean bSave)
    {
        mCompRatio = (nValue + 100) / 100.0f;
        mTextCompRatio.setText(String.format(Locale.getDefault(), "%.2f", mCompRatio));
        mSeekCompRatio.setProgress(nValue);
        updateComp();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setCompRatio(float fValue, boolean bSave)
    {
        mCompRatio = fValue;
        mTextCompRatio.setText(String.format(Locale.getDefault(), "%.2f", mCompRatio));
        mSeekCompRatio.setProgress((int)(fValue * 100.0f) - 100);
        updateComp();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setCompAttack(int nValue, boolean bSave)
    {
        mCompAttack = (nValue + 1) / 100.0f;
        mTextCompAttack.setText(String.format(Locale.getDefault(), "%.2f", mCompAttack));
        mSeekCompAttack.setProgress(nValue);
        updateComp();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setCompAttack(float fValue, boolean bSave)
    {
        mCompAttack = fValue;
        mTextCompAttack.setText(String.format(Locale.getDefault(), "%.2f", mCompAttack));
        mSeekCompAttack.setProgress((int)(fValue * 100.0f) - 1);
        updateComp();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setCompRelease(int nValue, boolean bSave)
    {
        mCompRelease = (nValue + 1) / 100.0f;
        mTextCompRelease.setText(String.format(Locale.getDefault(), "%.2f", mCompRelease));
        mSeekCompRelease.setProgress(nValue);
        updateComp();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setCompRelease(float fValue, boolean bSave)
    {
        mCompRelease = fValue;
        mTextCompRelease.setText(String.format(Locale.getDefault(), "%.2f", mCompRelease));
        mSeekCompRelease.setProgress((int)(fValue * 100.0f) - 1);
        updateComp();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    private void minusCompGain() {
        int nValue = mSeekCompGain.getProgress() - 1;
        if(nValue < 0) nValue = 0;
        setCompGain(nValue, true);
    }

    private void plusCompGain() {
        int nValue = mSeekCompGain.getProgress() + 1;
        if(nValue > mSeekCompGain.getMax()) nValue = mSeekCompGain.getMax();
        setCompGain(nValue, true);
    }

    private void minusCompThreshold() {
        int nValue = mSeekCompThreshold.getProgress() - 1;
        if(nValue < 0) nValue = 0;
        setCompThreshold(nValue, true);
    }

    private void plusCompThreshold() {
        int nValue = mSeekCompThreshold.getProgress() + 1;
        if(nValue > mSeekCompThreshold.getMax()) nValue = mSeekCompThreshold.getMax();
        setCompThreshold(nValue, true);
    }

    private void minusCompRatio() {
        int nValue = mSeekCompRatio.getProgress() - 1;
        if(nValue < 0) nValue = 0;
        setCompRatio(nValue, true);
    }

    private void plusCompRatio() {
        int nValue = mSeekCompRatio.getProgress() + 1;
        if(nValue > mSeekCompRatio.getMax()) nValue = mSeekCompRatio.getMax();
        setCompRatio(nValue, true);
    }

    private void minusCompAttack() {
        int nValue = mSeekCompAttack.getProgress() - 1;
        if(nValue < 0) nValue = 0;
        setCompAttack(nValue, true);
    }

    private void plusCompAttack() {
        int nValue = mSeekCompAttack.getProgress() + 1;
        if(nValue > mSeekCompAttack.getMax()) nValue = mSeekCompAttack.getMax();
        setCompAttack(nValue, true);
    }

    private void minusCompRelease() {
        int nValue = mSeekCompRelease.getProgress() - 1;
        if(nValue < 0) nValue = 0;
        setCompRelease(nValue, true);
    }

    private void plusCompRelease() {
        int nValue = mSeekCompRelease.getProgress() + 1;
        if(nValue > mSeekCompRelease.getMax()) nValue = mSeekCompRelease.getMax();
        setCompRelease(nValue, true);
    }

    private void updateEcho()
    {
        if(!mEffectItems.get(EFFECTTYPE_ECHO_CUSTOMIZE).isSelected() || MainActivity.sStream == 0)
            return;
        if(mFxEcho == 0) mFxEcho = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_ECHO4, 2);
        BASS_FX.BASS_BFX_ECHO4 echo = new BASS_FX.BASS_BFX_ECHO4();
        echo.fDryMix = mEchoDry;
        echo.fWetMix = mEchoWet;
        echo.fFeedback = mEchoFeedback;
        echo.fDelay = mEchoDelay;
        echo.bStereo = TRUE;
        echo.lChannel = BASS_FX.BASS_BFX_CHANALL;
        BASS.BASS_FXSetParameters(mFxEcho, echo);
    }

    public void setEchoDry(int nValue, boolean bSave)
    {
        mEchoDry = nValue / 100.0f;
        mTextEchoDry.setText(String.format(Locale.getDefault(), "%.2f", mEchoDry));
        mSeekEchoDry.setProgress(nValue);
        updateEcho();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setEchoDry(float fValue, boolean bSave)
    {
        mEchoDry = fValue;
        mTextEchoDry.setText(String.format(Locale.getDefault(), "%.2f", mEchoDry));
        mSeekEchoDry.setProgress((int)(fValue * 100.0f));
        updateEcho();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setEchoWet(int nValue, boolean bSave)
    {
        mEchoWet = nValue / 100.0f;
        mTextEchoWet.setText(String.format(Locale.getDefault(), "%.2f", mEchoWet));
        mSeekEchoWet.setProgress(nValue);
        updateEcho();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setEchoWet(float fValue, boolean bSave)
    {
        mEchoWet = fValue;
        mTextEchoWet.setText(String.format(Locale.getDefault(), "%.2f", mEchoWet));
        mSeekEchoWet.setProgress((int)(fValue * 100.0f));
        updateEcho();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setEchoFeedback(int nValue, boolean bSave)
    {
        mEchoFeedback = nValue / 100.0f;
        mTextEchoFeedback.setText(String.format(Locale.getDefault(), "%.2f", mEchoFeedback));
        mSeekEchoFeedback.setProgress(nValue);
        updateEcho();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setEchoFeedback(float fValue, boolean bSave)
    {
        mEchoFeedback = fValue;
        mTextEchoFeedback.setText(String.format(Locale.getDefault(), "%.2f", mEchoFeedback));
        mSeekEchoFeedback.setProgress((int)(fValue * 100.0f));
        updateEcho();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setEchoDelay(int nValue, boolean bSave)
    {
        mEchoDelay = nValue / 100.0f;
        mTextEchoDelay.setText(String.format(Locale.getDefault(), "%.2f", mEchoDelay));
        mSeekEchoDelay.setProgress(nValue);
        updateEcho();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setEchoDelay(float fValue, boolean bSave)
    {
        mEchoDelay = fValue;
        mTextEchoDelay.setText(String.format(Locale.getDefault(), "%.2f", mEchoDelay));
        mSeekEchoDelay.setProgress((int)(fValue * 100.0f));
        updateEcho();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    private void minusEchoDry() {
        int nValue = mSeekEchoDry.getProgress() - 1;
        if(nValue < 0) nValue = 0;
        setEchoDry(nValue, true);
    }

    private void plusEchoDry() {
        int nValue = mSeekEchoDry.getProgress() + 1;
        if(nValue > mSeekEchoDry.getMax()) nValue = mSeekEchoDry.getMax();
        setEchoDry(nValue, true);
    }

    private void minusEchoWet() {
        int nValue = mSeekEchoWet.getProgress() - 1;
        if(nValue < 0) nValue = 0;
        setEchoWet(nValue, true);
    }

    private void plusEchoWet() {
        int nValue = mSeekEchoWet.getProgress() + 1;
        if(nValue > mSeekEchoWet.getMax()) nValue = mSeekEchoWet.getMax();
        setEchoWet(nValue, true);
    }

    private void minusEchoFeedback() {
        int nValue = mSeekEchoFeedback.getProgress() - 1;
        if(nValue < 0) nValue = 0;
        setEchoFeedback(nValue, true);
    }

    private void plusEchoFeedback() {
        int nValue = mSeekEchoFeedback.getProgress() + 1;
        if(nValue > mSeekEchoFeedback.getMax()) nValue = mSeekEchoFeedback.getMax();
        setEchoFeedback(nValue, true);
    }

    private void minusEchoDelay() {
        int nValue = mSeekEchoDelay.getProgress() - 1;
        if(nValue < 0) nValue = 0;
        setEchoDelay(nValue, true);
    }

    private void plusEchoDelay() {
        int nValue = mSeekEchoDelay.getProgress() + 1;
        if(nValue > mSeekEchoDelay.getMax()) nValue = mSeekEchoDelay.getMax();
        setEchoDelay(nValue, true);
    }

    private void updateReverb()
    {
        if(!mEffectItems.get(EFFECTTYPE_REVERB).isSelected() || MainActivity.sStream == 0)
            return;
        if(mFxReverb == 0) mFxReverb = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_FREEVERB, 2);
        BASS_FX.BASS_BFX_FREEVERB reverb = new BASS_FX.BASS_BFX_FREEVERB();
        reverb.fDryMix = mReverbDry;
        reverb.fWetMix = mReverbWet;
        reverb.fRoomSize = mReverbRoomSize;
        reverb.fDamp = mReverbDamp;
        reverb.fWidth = mReverbWidth;
        reverb.lMode = 0;
        reverb.lChannel = BASS_FX.BASS_BFX_CHANALL;
        BASS.BASS_FXSetParameters(mFxReverb, reverb);
    }

    public void setReverb(int nDry, int nWet, int nRoomSize, int nDamp, int nWidth, boolean bSave) {
        if(nDry < 0) nDry = 0;
        if(nWet < 0) nWet = 0;
        if(nRoomSize < 0) nRoomSize = 0;
        if(nDamp < 0) nDamp = 0;
        if(nWidth < 0) nWidth = 0;

        if(nDry > mSeekReverbDry.getMax()) nDry = mSeekReverbDry.getMax();
        if(nWet > mSeekReverbWet.getMax()) nWet = mSeekReverbWet.getMax();
        if(nRoomSize > mSeekReverbRoomSize.getMax()) nRoomSize = mSeekReverbRoomSize.getMax();
        if(nDamp > mSeekReverbDamp.getMax()) nDamp = mSeekReverbDamp.getMax();
        if(nWidth > mSeekReverbWidth.getMax()) nWidth = mSeekReverbWidth.getMax();

        mReverbDry = nDry / 100.0f;
        mReverbWet = nWet / 100.0f;
        mReverbRoomSize = nRoomSize / 100.0f;
        mReverbDamp = nDamp / 100.0f;
        mReverbWidth = nWidth / 100.0f;

        mTextReverbDry.setText(String.format(Locale.getDefault(), "%.2f", mReverbDry));
        mTextReverbWet.setText(String.format(Locale.getDefault(), "%.2f", mReverbWet));
        mTextReverbRoomSize.setText(String.format(Locale.getDefault(), "%.2f", mReverbRoomSize));
        mTextReverbDamp.setText(String.format(Locale.getDefault(), "%.2f", mReverbDamp));
        mTextReverbWidth.setText(String.format(Locale.getDefault(), "%.2f", mReverbWidth));

        mSeekReverbDry.setProgress(nDry);
        mSeekReverbWet.setProgress(nWet);
        mSeekReverbRoomSize.setProgress(nRoomSize);
        mSeekReverbDamp.setProgress(nDamp);
        mSeekReverbWidth.setProgress(nWidth);

        updateReverb();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setReverb(float fDry, float fWet, float fRoomSize, float fDamp, float fWidth, boolean bSave)
    {
        mReverbDry = fDry;
        mReverbWet = fWet;
        mReverbRoomSize = fRoomSize;
        mReverbDamp = fDamp;
        mReverbWidth = fWidth;

        mTextReverbDry.setText(String.format(Locale.getDefault(), "%.2f", mReverbDry));
        mTextReverbWet.setText(String.format(Locale.getDefault(), "%.2f", mReverbWet));
        mTextReverbRoomSize.setText(String.format(Locale.getDefault(), "%.2f", mReverbRoomSize));
        mTextReverbDamp.setText(String.format(Locale.getDefault(), "%.2f", mReverbDamp));
        mTextReverbWidth.setText(String.format(Locale.getDefault(), "%.2f", mReverbWidth));

        mSeekReverbDry.setProgress((int)(fDry * 100.0f));
        mSeekReverbWet.setProgress((int)(fWet * 100.0f));
        mSeekReverbRoomSize.setProgress((int)(fRoomSize * 100.0f));
        mSeekReverbDamp.setProgress((int)(fDamp * 100.0f));
        mSeekReverbWidth.setProgress((int)(fWidth * 100.0f));

        updateReverb();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    private void minusReverbDry() {
        setReverb(mSeekReverbDry.getProgress()-1, mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress(), true);
    }

    private void plusReverbDry() {
        setReverb(mSeekReverbDry.getProgress()+1, mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress(), true);
    }

    private void minusReverbWet() {
        setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress()-1, mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress(), true);
    }

    private void plusReverbWet() {
        setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress()+1, mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress(), true);
    }

    private void minusReverbRoomSize() {
        setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress()-1, mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress(), true);
    }

    private void plusReverbRoomSize() {
        setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress()+1, mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress(), true);
    }

    private void minusReverbDamp() {
        setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress()-1, mSeekReverbWidth.getProgress(), true);
    }

    private void plusReverbDamp() {
        setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress()+1, mSeekReverbWidth.getProgress(), true);
    }

    private void minusReverbWidth() {
        setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress()-1, true);
    }

    private void plusReverbWidth() {
        setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress()+1, true);
    }

    private void updateChorus()
    {
        if(!mEffectItems.get(EFFECTTYPE_CHORUS_CUSTOMIZE).isSelected() || MainActivity.sStream == 0)
            return;
        if(mFxChorus == 0) mFxChorus = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_CHORUS, 2);
        BASS_FX.BASS_BFX_CHORUS chorus = new BASS_FX.BASS_BFX_CHORUS();
        chorus.fDryMix = mChorusDry;
        chorus.fWetMix = mChorusWet;
        chorus.fFeedback = mChorusFeedback;
        chorus.fMinSweep = mChorusMinSweep;
        chorus.fMaxSweep = mChorusMaxSweep;
        chorus.fRate = mChorusRate;
        chorus.lChannel = BASS_FX.BASS_BFX_CHANALL;
        BASS.BASS_FXSetParameters(mFxChorus, chorus);
    }

    public void setChorusDry(int nValue, boolean bSave)
    {
        mChorusDry = nValue / 100.0f;
        mTextChorusDry.setText(String.format(Locale.getDefault(), "%.2f", mChorusDry));
        mSeekChorusDry.setProgress(nValue);
        updateChorus();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setChorusDry(float fValue, boolean bSave)
    {
        mChorusDry = fValue;
        mTextChorusDry.setText(String.format(Locale.getDefault(), "%.2f", mChorusDry));
        mSeekChorusDry.setProgress((int)(fValue * 100.0f));
        updateChorus();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setChorusWet(int nValue, boolean bSave)
    {
        mChorusWet = nValue / 100.0f;
        mTextChorusWet.setText(String.format(Locale.getDefault(), "%.2f", mChorusWet));
        mSeekChorusWet.setProgress(nValue);
        updateChorus();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setChorusWet(float fValue, boolean bSave)
    {
        mChorusWet = fValue;
        mTextChorusWet.setText(String.format(Locale.getDefault(), "%.2f", mChorusWet));
        mSeekChorusWet.setProgress((int)(fValue * 100.0f));
        updateChorus();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setChorusFeedback(int nValue, boolean bSave)
    {
        mChorusFeedback = nValue / 100.0f;
        mTextChorusFeedback.setText(String.format(Locale.getDefault(), "%.2f", mChorusFeedback));
        mSeekChorusFeedback.setProgress(nValue);
        updateChorus();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setChorusFeedback(float fValue, boolean bSave)
    {
        mChorusFeedback = fValue;
        mTextChorusFeedback.setText(String.format(Locale.getDefault(), "%.2f", mChorusFeedback));
        mSeekChorusFeedback.setProgress((int)(fValue * 100.0f));
        updateChorus();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setChorusMinSweep(int nValue, boolean bSave)
    {
        mChorusMinSweep = nValue / 100.0f;
        mTextChorusMinSweep.setText(String.format(Locale.getDefault(), "%.2f", mChorusMinSweep));
        mSeekChorusMinSweep.setProgress(nValue);
        updateChorus();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setChorusMinSweep(float fValue, boolean bSave)
    {
        mChorusMinSweep = fValue;
        mTextChorusMinSweep.setText(String.format(Locale.getDefault(), "%.2f", mChorusMinSweep));
        mSeekChorusMinSweep.setProgress((int)(fValue * 100.0f));
        updateChorus();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setChorusMaxSweep(int nValue, boolean bSave)
    {
        mChorusMaxSweep = nValue / 100.0f;
        mTextChorusMaxSweep.setText(String.format(Locale.getDefault(), "%.2f", mChorusMaxSweep));
        mSeekChorusMaxSweep.setProgress(nValue);
        updateChorus();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setChorusMaxSweep(float fValue, boolean bSave)
    {
        mChorusMaxSweep = fValue;
        mTextChorusMaxSweep.setText(String.format(Locale.getDefault(), "%.2f", mChorusMaxSweep));
        mSeekChorusMaxSweep.setProgress((int)(fValue * 100.0f));
        updateChorus();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setChorusRate(int nValue, boolean bSave)
    {
        mChorusRate = nValue / 100.0f;
        mTextChorusRate.setText(String.format(Locale.getDefault(), "%.2f", mChorusRate));
        mSeekChorusRate.setProgress(nValue);
        updateChorus();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setChorusRate(float fValue, boolean bSave)
    {
        mChorusRate = fValue;
        mTextChorusRate.setText(String.format(Locale.getDefault(), "%.2f", mChorusRate));
        mSeekChorusRate.setProgress((int)(fValue * 100.0f));
        updateChorus();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    private void minusChorusDry() {
        int nValue = mSeekChorusDry.getProgress() - 1;
        if(nValue < 0) nValue = 0;
        setChorusDry(nValue, true);
    }

    private void plusChorusDry() {
        int nValue = mSeekChorusDry.getProgress() + 1;
        if(nValue > mSeekChorusDry.getMax()) nValue = mSeekChorusDry.getMax();
        setChorusDry(nValue, true);
    }

    private void minusChorusWet() {
        int nValue = mSeekChorusWet.getProgress() - 1;
        if(nValue < 0) nValue = 0;
        setChorusWet(nValue, true);
    }

    private void plusChorusWet() {
        int nValue = mSeekChorusWet.getProgress() + 1;
        if(nValue > mSeekChorusWet.getMax()) nValue = mSeekChorusWet.getMax();
        setChorusWet(nValue, true);
    }

    private void minusChorusFeedback() {
        int nValue = mSeekChorusFeedback.getProgress() - 1;
        if(nValue < 0) nValue = 0;
        setChorusFeedback(nValue, true);
    }

    private void plusChorusFeedback() {
        int nValue = mSeekChorusFeedback.getProgress() + 1;
        if(nValue > mSeekChorusFeedback.getMax()) nValue = mSeekChorusFeedback.getMax();
        setChorusFeedback(nValue, true);
    }

    private void minusChorusMinSweep() {
        int nValue = mSeekChorusMinSweep.getProgress() - 1;
        if(nValue < 0) nValue = 0;
        setChorusMinSweep(nValue, true);
    }

    private void plusChorusMinSweep() {
        int nValue = mSeekChorusMinSweep.getProgress() + 1;
        if(nValue > mSeekChorusMinSweep.getMax()) nValue = mSeekChorusMinSweep.getMax();
        setChorusMinSweep(nValue, true);
    }

    private void minusChorusMaxSweep() {
        int nValue = mSeekChorusMaxSweep.getProgress() - 1;
        if(nValue < 0) nValue = 0;
        setChorusMaxSweep(nValue, true);
    }

    private void plusChorusMaxSweep() {
        int nValue = mSeekChorusMaxSweep.getProgress() + 1;
        if(nValue > mSeekChorusMaxSweep.getMax()) nValue = mSeekChorusMaxSweep.getMax();
        setChorusMaxSweep(nValue, true);
    }

    private void minusChorusRate() {
        int nValue = mSeekChorusRate.getProgress() - 1;
        if(nValue < 0) nValue = 0;
        setChorusRate(nValue, true);
    }

    private void plusChorusRate() {
        int nValue = mSeekChorusRate.getProgress() + 1;
        if(nValue > mSeekChorusRate.getMax()) nValue = mSeekChorusRate.getMax();
        setChorusRate(nValue, true);
    }

    private void updateDistortion()
    {
        if(!mEffectItems.get(EFFECTTYPE_DISTORTION_CUSTOMIZE).isSelected() || MainActivity.sStream == 0)
            return;
        if(mFxDistortion == 0) mFxDistortion = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_DISTORTION, 2);
        BASS_FX.BASS_BFX_DISTORTION distortion = new BASS_FX.BASS_BFX_DISTORTION();
        distortion.fDrive = mDistortionDrive;
        distortion.fDryMix = mDistortionDry;
        distortion.fWetMix = mDistortionWet;
        distortion.fFeedback = mDistortionFeedback;
        distortion.fVolume = mDistortionVolume;
        distortion.lChannel = BASS_FX.BASS_BFX_CHANALL;
        BASS.BASS_FXSetParameters(mFxDistortion, distortion);
    }

    public void setDistortionDrive(int nValue, boolean bSave)
    {
        mDistortionDrive = nValue / 100.0f;
        mTextDistortionDrive.setText(String.format(Locale.getDefault(), "%.2f", mDistortionDrive));
        mSeekDistortionDrive.setProgress(nValue);
        updateDistortion();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setDistortionDrive(float fValue, boolean bSave)
    {
        mDistortionDrive = fValue;
        mTextDistortionDrive.setText(String.format(Locale.getDefault(), "%.2f", mDistortionDrive));
        mSeekDistortionDrive.setProgress((int)(fValue * 100.0f));
        updateDistortion();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setDistortionDry(int nValue, boolean bSave)
    {
        mDistortionDry = nValue / 100.0f;
        mTextDistortionDry.setText(String.format(Locale.getDefault(), "%.2f", mDistortionDry));
        mSeekDistortionDry.setProgress(nValue);
        updateDistortion();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setDistortionDry(float fValue, boolean bSave)
    {
        mDistortionDry = fValue;
        mTextDistortionDry.setText(String.format(Locale.getDefault(), "%.2f", mDistortionDry));
        mSeekDistortionDry.setProgress((int)(fValue * 100.0f));
        updateDistortion();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setDistortionWet(int nValue, boolean bSave)
    {
        mDistortionWet = nValue / 100.0f;
        mTextDistortionWet.setText(String.format(Locale.getDefault(), "%.2f", mDistortionWet));
        mSeekDistortionWet.setProgress(nValue);
        updateDistortion();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setDistortionWet(float fValue, boolean bSave)
    {
        mDistortionWet = fValue;
        mTextDistortionWet.setText(String.format(Locale.getDefault(), "%.2f", mDistortionWet));
        mSeekDistortionWet.setProgress((int)(fValue * 100.0f));
        updateDistortion();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setDistortionFeedback(int nValue, boolean bSave)
    {
        mDistortionFeedback = nValue / 100.0f;
        mTextDistortionFeedback.setText(String.format(Locale.getDefault(), "%.2f", mDistortionFeedback));
        mSeekDistortionFeedback.setProgress(nValue);
        updateDistortion();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setDistortionFeedback(float fValue, boolean bSave)
    {
        mDistortionFeedback = fValue;
        mTextDistortionFeedback.setText(String.format(Locale.getDefault(), "%.2f", mDistortionFeedback));
        mSeekDistortionFeedback.setProgress((int)(fValue * 100.0f));
        updateDistortion();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setDistortionVolume(int nValue, boolean bSave)
    {
        mDistortionVolume = nValue / 100.0f;
        mTextDistortionVolume.setText(String.format(Locale.getDefault(), "%.2f", mDistortionVolume));
        mSeekDistortionVolume.setProgress(nValue);
        updateDistortion();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setDistortionVolume(float fValue, boolean bSave)
    {
        mDistortionVolume = fValue;
        mTextDistortionVolume.setText(String.format(Locale.getDefault(), "%.2f", mDistortionVolume));
        mSeekDistortionVolume.setProgress((int)(fValue * 100.0f));
        updateDistortion();
        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    private void minusDistortionDrive() {
        int nValue = mSeekDistortionDrive.getProgress() - 1;
        if(nValue < 0) nValue = 0;
        setDistortionDrive(nValue, true);
    }

    private void plusDistortionDrive() {
        int nValue = mSeekDistortionDrive.getProgress() + 1;
        if(nValue > mSeekDistortionDrive.getMax()) nValue = mSeekDistortionDrive.getMax();
        setDistortionDrive(nValue, true);
    }

    private void minusDistortionDry() {
        int nValue = mSeekDistortionDry.getProgress() - 1;
        if(nValue < 0) nValue = 0;
        setDistortionDry(nValue, true);
    }

    private void plusDistortionDry() {
        int nValue = mSeekDistortionDry.getProgress() + 1;
        if(nValue > mSeekDistortionDry.getMax()) nValue = mSeekDistortionDry.getMax();
        setDistortionDry(nValue, true);
    }

    private void minusDistortionWet() {
        int nValue = mSeekDistortionWet.getProgress() - 1;
        if(nValue < 0) nValue = 0;
        setDistortionWet(nValue, true);
    }

    private void plusDistortionWet() {
        int nValue = mSeekDistortionWet.getProgress() + 1;
        if(nValue > mSeekDistortionWet.getMax()) nValue = mSeekDistortionWet.getMax();
        setDistortionWet(nValue, true);
    }

    private void minusDistortionFeedback() {
        int nValue = mSeekDistortionFeedback.getProgress() - 1;
        if(nValue < 0) nValue = 0;
        setDistortionFeedback(nValue, true);
    }

    private void plusDistortionFeedback() {
        int nValue = mSeekDistortionFeedback.getProgress() + 1;
        if(nValue > mSeekDistortionFeedback.getMax()) nValue = mSeekDistortionFeedback.getMax();
        setDistortionFeedback(nValue, true);
    }

    private void minusDistortionVolume() {
        int nValue = mSeekDistortionVolume.getProgress() - 1;
        if(nValue < 0) nValue = 0;
        setDistortionVolume(nValue, true);
    }

    private void plusDistortionVolume() {
        int nValue = mSeekDistortionVolume.getProgress() + 1;
        if(nValue > mSeekDistortionVolume.getMax()) nValue = mSeekDistortionVolume.getMax();
        setDistortionVolume(nValue, true);
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
            if(EFFECTTYPE_COMP <= nSelect && nSelect <= EFFECTTYPE_COMP_CUSTOMIZE)
            {
                for(int i = EFFECTTYPE_COMP; i <= EFFECTTYPE_COMP_CUSTOMIZE; i++)
                    if(i != nSelect) deselectEffect(i);
            }
            if(EFFECTTYPE_ECHO_STADIUM <= nSelect && nSelect <= EFFECTTYPE_ECHO_CUSTOMIZE)
            {
                for(int i = EFFECTTYPE_ECHO_STADIUM; i <= EFFECTTYPE_ECHO_CUSTOMIZE; i++)
                    if(i != nSelect) deselectEffect(i);
            }
            if(EFFECTTYPE_CHORUS <= nSelect && nSelect <= EFFECTTYPE_CHORUS_CUSTOMIZE)
            {
                for(int i = EFFECTTYPE_CHORUS; i <= EFFECTTYPE_CHORUS_CUSTOMIZE; i++)
                    if(i != nSelect) deselectEffect(i);
            }
            if((EFFECTTYPE_DISTORTION_STRONG <= nSelect && nSelect <= EFFECTTYPE_DISTORTION_CUSTOMIZE) || nSelect == EFFECTTYPE_LOWBATTERY)
            {
                for(int i = EFFECTTYPE_DISTORTION_STRONG; i <= EFFECTTYPE_DISTORTION_CUSTOMIZE; i++)
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
        mEffectsAdapter.notifyItemChanged(nEffect);

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
        if(nEffect == EFFECTTYPE_REVERB) mReverbSelected = -1;
    }

    public void applyEffect()
    {
        int nPlayingPlaylist = mActivity.playlistFragment.getPlayingPlaylist();
        if(nPlayingPlaylist < 0 || nPlayingPlaylist >= mActivity.playlistFragment.getPlaylists().size()) return;
        ArrayList<SongItem> arSongs = mActivity.playlistFragment.getPlaylists().get(nPlayingPlaylist);
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
        if(mFxComp != 0)
        {
            BASS.BASS_ChannelRemoveFX(sStream, mFxComp);
            mFxComp = 0;
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
            BASS_FX.BASS_BFX_ECHO4 echo;
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
            else if(strEffect.equals(getString(R.string.comp))) {
                mFxComp = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_COMPRESSOR2, 2);
                BASS_FX.BASS_BFX_COMPRESSOR2 p = new BASS_FX.BASS_BFX_COMPRESSOR2();
                p.fGain = 2.0f;
                p.fThreshold = -20.0f;
                p.fRatio = 10.0f;
                p.fAttack = 1.2f;
                p.fRelease = 400.0f;
                p.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxComp, p);
            }
            else if(strEffect.equals(getString(R.string.compCustomize))) {
                mFxComp = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_COMPRESSOR2, 2);
                BASS_FX.BASS_BFX_COMPRESSOR2 p = new BASS_FX.BASS_BFX_COMPRESSOR2();
                p.fGain = mCompGain;
                p.fThreshold = mCompThreshold;
                p.fRatio = mCompRatio;
                p.fAttack = mCompAttack;
                p.fRelease = mCompRelease;
                p.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxComp, p);
            }
            else if(strEffect.equals(getString(R.string.frequency)))
                BASS.BASS_ChannelSetAttribute(sStream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, info.freq * mFreq);
            else if(strEffect.equals(getString(R.string.phaseReversal)))
                mDspPhaseReversal = BASS.BASS_ChannelSetDSP(sStream, phaseReversalDSP, null, 0);
            else if(strEffect.equals(getString(R.string.studiumEcho))) {
                mFxEcho = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_ECHO4, 2);
                echo = new BASS_FX.BASS_BFX_ECHO4();
                echo.fDryMix = 0.95f;
                echo.fWetMix = 0.1f;
                echo.fFeedback = 0.55f;
                echo.fDelay = 0.4f;
                echo.bStereo = TRUE;
                echo.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxEcho, echo);
            }
            else if(strEffect.equals(getString(R.string.hallEcho))) {
                mFxEcho = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_ECHO4, 2);
                echo = new BASS_FX.BASS_BFX_ECHO4();
                echo.fDryMix = 0.95f;
                echo.fWetMix = 0.1f;
                echo.fFeedback = 0.5f;
                echo.fDelay = 0.3f;
                echo.bStereo = TRUE;
                echo.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxEcho, echo);
            }
            else if(strEffect.equals(getString(R.string.livehouseEcho))) {
                mFxEcho = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_ECHO4, 2);
                echo = new BASS_FX.BASS_BFX_ECHO4();
                echo.fDryMix = 1.0f;
                echo.fWetMix = 0.125f;
                echo.fFeedback = 0.3f;
                echo.fDelay = 0.2f;
                echo.bStereo = TRUE;
                echo.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxEcho, echo);
            }
            else if(strEffect.equals(getString(R.string.roomEcho))) {
                mFxEcho = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_ECHO4, 2);
                echo = new BASS_FX.BASS_BFX_ECHO4();
                echo.fDryMix = 1.0f;
                echo.fWetMix = 0.15f;
                echo.fFeedback = 0.5f;
                echo.fDelay = 0.1f;
                echo.bStereo = TRUE;
                echo.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxEcho, echo);
            }
            else if(strEffect.equals(getString(R.string.bathroomEcho))) {
                mFxEcho = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_ECHO4, 2);
                echo = new BASS_FX.BASS_BFX_ECHO4();
                echo.fDryMix = 1.0f;
                echo.fWetMix = 0.3f;
                echo.fFeedback = 0.6f;
                echo.fDelay = 0.075f;
                echo.bStereo = TRUE;
                echo.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxEcho, echo);
            }
            else if(strEffect.equals(getString(R.string.vocalEcho))) {
                mFxEcho = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_ECHO4, 2);
                echo = new BASS_FX.BASS_BFX_ECHO4();
                echo.fDryMix = 1.0f;
                echo.fWetMix = 0.15f;
                echo.fFeedback = 0.4f;
                echo.fDelay = 0.35f;
                echo.bStereo = TRUE;
                echo.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxEcho, echo);
            }
            else if(strEffect.equals(getString(R.string.mountainEcho))) {
                mFxEcho = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_ECHO4, 2);
                echo = new BASS_FX.BASS_BFX_ECHO4();
                echo.fDryMix = 1.0f;
                echo.fWetMix = 0.2f;
                echo.fFeedback = 0.0f;
                echo.fDelay = 1.0f;
                echo.bStereo = TRUE;
                echo.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxEcho, echo);
            }
            else if(strEffect.equals(getString(R.string.echoCustomize))) {
                mFxEcho = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_ECHO4, 2);
                echo = new BASS_FX.BASS_BFX_ECHO4();
                echo.fDryMix = mEchoDry;
                echo.fWetMix = mEchoWet;
                echo.fFeedback = mEchoFeedback;
                echo.fDelay = mEchoDelay;
                echo.bStereo = TRUE;
                echo.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(mFxEcho, echo);
            }
            else if(strEffect.equals(getString(R.string.reverb))) {
                mFxReverb = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_FREEVERB, 2);
                reverb = new BASS_FX.BASS_BFX_FREEVERB();
                reverb.fDryMix = mReverbDry;
                reverb.fWetMix = mReverbWet;
                reverb.fRoomSize = mReverbRoomSize;
                reverb.fDamp = mReverbDamp;
                reverb.fWidth = mReverbWidth;
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
            else if(strEffect.equals(getString(R.string.chorusCustomize))) {
                mFxChorus = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_CHORUS, 2);
                chorus = new BASS_FX.BASS_BFX_CHORUS();
                chorus.fDryMix = mChorusDry;
                chorus.fWetMix = mChorusWet;
                chorus.fFeedback = mChorusFeedback;
                chorus.fMinSweep = mChorusMinSweep;
                chorus.fMaxSweep = mChorusMaxSweep;
                chorus.fRate = mChorusRate;
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
            else if(strEffect.equals(getString(R.string.distortionCustomize))) {
                mFxDistortion = BASS.BASS_ChannelSetFX(sStream, BASS_FX.BASS_FX_BFX_DISTORTION, 2);
                distortion = new BASS_FX.BASS_BFX_DISTORTION();
                distortion.fDrive = mDistortionDrive;
                distortion.fDryMix = mDistortionDry;
                distortion.fWetMix = mDistortionWet;
                distortion.fFeedback = mDistortionFeedback;
                distortion.fVolume = mDistortionVolume;
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
                    MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                    params.inputStream = getResources().openRawResource(R.raw.recordnoise);
                    mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
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
                    MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                    params.inputStream = getResources().openRawResource(R.raw.recordnoise);
                    mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                    mSync = BASS.BASS_ChannelSetSync(mSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 4.653), endRecordNoise, this);
                    BASS.BASS_ChannelSetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, mVol1);
                    BASS.BASS_ChannelPlay(mSEStream, true);
                }
            }
            else if(strEffect.equals(getString(R.string.wave))) {
                if (mSEStream == 0) {
                    mSE1PlayingFlag = true;
                    MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                    params.inputStream = getResources().openRawResource(R.raw.wave);
                    mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                    mSync = BASS.BASS_ChannelSetSync(mSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 38.399), endWave, this);
                    BASS.BASS_ChannelSetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, mVol2);
                    BASS.BASS_ChannelPlay(mSEStream, true);
                }
            }
            else if(strEffect.equals(getString(R.string.rain))) {
                if (mSEStream == 0) {
                    mSE1PlayingFlag = true;
                    MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                    params.inputStream = getResources().openRawResource(R.raw.rain);
                    mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                    mSync = BASS.BASS_ChannelSetSync(mSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 1.503), endRain, this);
                    BASS.BASS_ChannelSetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, mVol3);
                    BASS.BASS_ChannelPlay(mSEStream, true);
                }
            }
            else if(strEffect.equals(getString(R.string.river))) {
                if (mSEStream == 0) {
                    mSE1PlayingFlag = true;
                    MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                    params.inputStream = getResources().openRawResource(R.raw.river);
                    mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                    mSync = BASS.BASS_ChannelSetSync(mSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 60.000), endRiver, this);
                    BASS.BASS_ChannelSetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, mVol4);
                    BASS.BASS_ChannelPlay(mSEStream, true);
                }
            }
            else if(strEffect.equals(getString(R.string.war))) {
                if (mSEStream == 0) {
                    mSE1PlayingFlag = true;
                    MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                    params.inputStream = getResources().openRawResource(R.raw.war);
                    mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                    mSync = BASS.BASS_ChannelSetSync(mSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 30.000), endWar, this);
                    BASS.BASS_ChannelSetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, mVol5);
                    BASS.BASS_ChannelPlay(mSEStream, true);
                }
            }
            else if(strEffect.equals(getString(R.string.fire))) {
                if (mSEStream == 0) {
                    mSE1PlayingFlag = true;
                    MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                    params.inputStream = getResources().openRawResource(R.raw.fire);
                    mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                    mSync = BASS.BASS_ChannelSetSync(mSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(mSEStream, 90.000), endFire, this);
                    BASS.BASS_ChannelSetAttribute(mSEStream, BASS.BASS_ATTRIB_VOL, mVol6);
                    BASS.BASS_ChannelPlay(mSEStream, true);
                }
            }
            else if(strEffect.equals(getString(R.string.concertHall))) {
                if (mSEStream == 0) {
                    mSE1PlayingFlag = true;
                    MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                    params.inputStream = getResources().openRawResource(R.raw.cheer);
                    mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
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
            ContentResolver cr = mActivity.getContentResolver();
            try {
                MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                params.assetFileDescriptor = cr.openAssetFileDescriptor(Uri.parse(song.getPath()), "r");
                if(params.assetFileDescriptor != null) {
                    params.fileChannel = params.assetFileDescriptor.createInputStream().getChannel();
                    hTempStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                }
            }
            catch(Exception e) {
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
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = getResources().openRawResource(R.raw.recordnoise);
            mSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
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
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = getResources().openRawResource(R.raw.recordnoise);
            mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
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
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = getResources().openRawResource(R.raw.wave);
            mSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
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
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = getResources().openRawResource(R.raw.wave);
            mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
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
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = getResources().openRawResource(R.raw.rain);
            mSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
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
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = getResources().openRawResource(R.raw.rain);
            mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
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
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = getResources().openRawResource(R.raw.river);
            mSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
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
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = getResources().openRawResource(R.raw.river);
            mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
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
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = getResources().openRawResource(R.raw.war);
            mSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
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
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = getResources().openRawResource(R.raw.war);
            mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
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
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = getResources().openRawResource(R.raw.fire);
            mSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
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
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = getResources().openRawResource(R.raw.fire);
            mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
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
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = getResources().openRawResource(R.raw.cheer);
            mSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
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
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = getResources().openRawResource(R.raw.cheer);
            mSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
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

    public void showTemplateMenu() {
        final BottomMenu menu = new BottomMenu(mActivity);
        menu.setTitle(getString(R.string.reverbTemplate));
        menu.addMenu(getString(R.string.sortTemplate), R.drawable.ic_actionsheet_sort, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                mRecyclerEffectTemplates.setPadding(0, 0, 0, (int)(64 * mActivity.getDensity()));
                mBtnEffectTemplateOff.setVisibility(View.GONE);

                mRelativeEffectTitle.setVisibility(View.GONE);
                mViewSepEffect.setVisibility(View.GONE);
                mViewSepEffectTemplate.setVisibility(View.GONE);
                mBtnAddEffectTemplate.setAlpha(0.0f);
                mTextFinishSortEffect.setVisibility(View.VISIBLE);
                mSorting = true;
                mEffectTemplatesAdapter.notifyDataSetChanged();

                mEffectTemplateTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
                    @Override
                    public boolean onMove(RecyclerView mRecyclerEqualizers, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        final int fromPos = viewHolder.getAdapterPosition();
                        final int toPos = target.getAdapterPosition();

                        EffectTemplateItem itemTemp = mReverbItems.get(fromPos);
                        mReverbItems.remove(fromPos);
                        mReverbItems.add(toPos, itemTemp);

                        mEffectTemplatesAdapter.notifyItemMoved(fromPos, toPos);

                        if(fromPos == mReverbSelected) mReverbSelected = toPos;
                        else if(fromPos < mReverbSelected && mReverbSelected <= toPos) mReverbSelected--;
                        else if(fromPos > mReverbSelected && mReverbSelected >= toPos) mReverbSelected++;

                        for(int i = 0; i < mActivity.playlistFragment.getPlaylists().size(); i++) {

                            ArrayList<SongItem> arSongs = mActivity.playlistFragment.getPlaylists().get(i);
                            ArrayList<EffectSaver> arEffects = mActivity.playlistFragment.getEffects().get(i);
                            for(int j = 0; j < arSongs.size(); j++) {
                                EffectSaver saver = arEffects.get(j);
                                if(saver.isSave()) {
                                    if(fromPos == saver.getReverbSelected()) saver.setReverbSelected(toPos);
                                    else if(fromPos < saver.getReverbSelected() && saver.getReverbSelected() <= toPos) saver.setReverbSelected(saver.getReverbSelected()-1);
                                    else if(fromPos > saver.getReverbSelected() && saver.getReverbSelected() >= toPos) saver.setReverbSelected(saver.getReverbSelected()+1);
                                }
                            }
                        }

                        return true;
                    }

                    @Override
                    public void clearView(RecyclerView recyclerSongs, RecyclerView.ViewHolder viewHolder) {
                        super.clearView(recyclerSongs, viewHolder);

                        mEffectTemplatesAdapter.notifyDataSetChanged();
                        saveData();
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    }
                });
                mEffectTemplateTouchHelper.attachToRecyclerView(mRecyclerEffectTemplates);
            }
        });
        menu.addDestructiveMenu(getString(R.string.initializeTemplate), R.drawable.ic_actionsheet_initialize, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(R.string.initializeTemplate);
                builder.setMessage(R.string.askinitializeTemplate);
                builder.setPositiveButton(R.string.decideNot, null);
                builder.setNegativeButton(R.string.doInitialize, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        resetReverbs();
                    }
                });
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                {
                    @Override
                    public void onShow(DialogInterface arg0)
                    {
                        if(alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        positiveButton.setTextColor(Color.argb(255, 255, 0, 0));
                    }
                });
                alertDialog.show();
            }
        });
        menu.setCancelMenu();
        menu.show();
    }

    public void showMenu(final int nItem) {
        final BottomMenu menu = new BottomMenu(mActivity);
        if(mEffectDetail == EFFECTTYPE_REVERB)
            menu.setTitle(mReverbItems.get(nItem).getEffectTemplateName());
        menu.addMenu(getString(R.string.changeTemplateName), R.drawable.ic_actionsheet_edit, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(R.string.changeTemplateName);
                LinearLayout linearLayout = new LinearLayout(mActivity);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                final EditText editPreset = new EditText (mActivity);
                editPreset.setHint(R.string.templateName);
                editPreset.setHintTextColor(Color.argb(255, 192, 192, 192));
                if(mEffectDetail == EFFECTTYPE_REVERB)
                    editPreset.setText(mReverbItems.get(nItem).getEffectTemplateName());
                linearLayout.addView(editPreset);
                builder.setView(linearLayout);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mReverbItems.get(nItem).setEffectTemplateName(editPreset.getText().toString());
                        mEffectTemplatesAdapter.notifyItemChanged(nItem);
                        saveData();
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                {
                    @Override
                    public void onShow(DialogInterface arg0)
                    {
                        if(alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        editPreset.requestFocus();
                        editPreset.setSelection(editPreset.getText().toString().length());
                        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (null != imm) imm.showSoftInput(editPreset, 0);
                    }
                });
                alertDialog.show();
            }
        });
        menu.addMenu(getString(R.string.copy), R.drawable.ic_actionsheet_copy, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                EffectTemplateItem item = mReverbItems.get(nItem);
                ArrayList<Integer> arPresets = new ArrayList<>();
                mReverbItems.add(nItem+1, new EffectTemplateItem(item.getEffectTemplateName(), new ArrayList<>(Arrays.asList(item.getArPresets().get(0), item.getArPresets().get(1), item.getArPresets().get(2), item.getArPresets().get(3), item.getArPresets().get(4)))));
                mEffectTemplatesAdapter.notifyItemInserted(nItem+1);

                if(nItem < mReverbSelected) mReverbSelected++;

                for(int i = 0; i < mActivity.playlistFragment.getPlaylists().size(); i++) {

                    ArrayList<SongItem> arSongs = mActivity.playlistFragment.getPlaylists().get(i);
                    ArrayList<EffectSaver> arEffects = mActivity.playlistFragment.getEffects().get(i);
                    for(int j = 0; j < arSongs.size(); j++) {
                        EffectSaver saver = arEffects.get(j);
                        if(saver.isSave()) {
                            if(nItem < saver.getReverbSelected()) saver.setReverbSelected(saver.getReverbSelected()+1);
                        }
                    }
                }

                saveData();
                mRecyclerEffectTemplates.scrollToPosition(nItem+1);
            }
        });
        menu.addDestructiveMenu(getString(R.string.delete), R.drawable.ic_actionsheet_delete, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(mReverbItems.get(nItem).getEffectTemplateName());
                builder.setMessage(R.string.askDeleteTemplate);
                builder.setPositiveButton(R.string.decideNot, null);
                builder.setNegativeButton(R.string.doDelete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        removeReverbItem(nItem);
                    }
                });
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                {
                    @Override
                    public void onShow(DialogInterface arg0)
                    {
                        if(alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        positiveButton.setTextColor(Color.argb(255, 255, 0, 0));
                    }
                });
                alertDialog.show();
            }
        });
        menu.setCancelMenu();
        menu.show();
    }

    private void removeReverbItem(int nItem)
    {
        mReverbItems.remove(nItem);
        mEffectTemplatesAdapter.notifyItemRemoved(nItem);

        if(nItem == mReverbSelected) resetReverb();
        else if(nItem < mReverbSelected) mReverbSelected--;

        for(int i = 0; i < mActivity.playlistFragment.getPlaylists().size(); i++) {

            ArrayList<SongItem> arSongs = mActivity.playlistFragment.getPlaylists().get(i);
            ArrayList<EffectSaver> arEffects = mActivity.playlistFragment.getEffects().get(i);
            for(int j = 0; j < arSongs.size(); j++) {
                EffectSaver saver = arEffects.get(j);
                if(saver.isSave()) {
                    if(nItem == saver.getReverbSelected()) {
                        saver.setReverbSelected(-1);
                        saver.setReverbDry(1.0f);
                        saver.setReverbWet(0.0f);
                        saver.setReverbRoomSize(0.0f);
                        saver.setReverbDamp(0.0f);
                        saver.setReverbWidth(0.0f);
                    }
                    else if(nItem < saver.getReverbSelected())
                        saver.setReverbSelected(saver.getReverbSelected()-1);
                }
            }
        }
        saveData();
    }
}
