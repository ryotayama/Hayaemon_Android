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

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class EffectFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener, SeekBar.OnSeekBarChangeListener, View.OnFocusChangeListener {
    private static MainActivity sActivity;
    private EffectsAdapter mEffectsAdapter;
    private EffectTemplatesAdapter mEffectTemplatesAdapter;
    public static ArrayList<EffectItem> sEffectItems;
    private static ArrayList<EffectTemplateItem> sReverbItems;
    private static ArrayList<EffectTemplateItem> sEchoItems;
    private static ArrayList<EffectTemplateItem> sChorusItems;
    private static ArrayList<EffectTemplateItem> sDistortionItems;
    private static ArrayList<EffectTemplateItem> sCompItems;
    private static ArrayList<EffectTemplateItem> sPanItems;
    private static ArrayList<EffectTemplateItem> sFreqItems;
    private static ArrayList<EffectTemplateItem> sMetronomeItems;
    private static ArrayList<EffectTemplateItem> sSoundEffectItems;
    private ItemTouchHelper mEffectTemplateTouchHelper;
    private static Metronome sMetronome;
    private static int sDspVocalCancel = 0, sDspMonoral = 0, sDspLeft = 0, sDspRight = 0, sDspExchange = 0, sDspDoubling = 0, sDspPan = 0, sDspNormalize = 0, sFxComp = 0, sDspPhaseReversal = 0, sFxEcho = 0, sFxReverb = 0, sFxChorus = 0, sFxDistortion = 0;
    public static float sPan = 0.0f, sFreq = 1.0f, sPeak = 0.0f, sTimeOfIncreaseSpeedSpecified = 1.0f, sIncreaseSpeedSpecified = 0.1f, sIncreaseSpeedLoop = 1.0f, sTimeOfDecreaseSpeedSpecified = 1.0f, sDecreaseSpeedSpecified = 0.1f, sDecreaseSpeedLoop = 1.0f, sTimeOfRaisePitchSpecified = 1.0f, sRaisePitchSpecified = 0.1f, sRaisePitchLoop = 1.0f, sTimeOfLowerPitchSpecified = 1.0f, sLowerPitchSpecified = 0.1f, sLowerPitchLoop = 1.0f;
    public static int sBpm = 120;
    public static float sCompGain, sCompThreshold, sCompRatio, sCompAttack, sCompRelease;
    public static float sEchoDry, sEchoWet, sEchoFeedback, sEchoDelay;
    public static float sReverbDry, sReverbWet, sReverbRoomSize, sReverbDamp, sReverbWidth;
    public static float sChorusDry, sChorusWet, sChorusFeedback, sChorusMinSweep, sChorusMaxSweep, sChorusRate;
    public static float sDistortionDrive, sDistortionDry, sDistortionWet, sDistortionFeedback, sDistortionVolume;
    public static float sSoundEffectVolume;
    private static final int EFFECTTYPE_RANDOM = 0;
    private static final int EFFECTTYPE_VOCALCANCEL = 1;
    // private static final int EFFECTTYPE_MONORAL = 2;
    // private static final int EFFECTTYPE_LEFTONLY = 3;
    // private static final int EFFECTTYPE_RIGHTONLY = 4;
    // private static final int EFFECTTYPE_REPLACE = 5;
    // private static final int EFFECTTYPE_DOUBLING = 6;
    private static final int EFFECTTYPE_TRANSCRIBESIDEGUITAR = 7;
    private static final int EFFECTTYPE_TRANSCRIBEBASS = 8;
    private static final int EFFECTTYPE_PAN = 9;
    // private static final int EFFECTTYPE_NORMALIZE = 10;
    private static final int EFFECTTYPE_COMP = 11;
    private static final int EFFECTTYPE_FREQ = 12;
    // private static final int EFFECTTYPE_PHASEREVERSAL = 13;
    private static final int EFFECTTYPE_ECHO = 14;
    private static final int EFFECTTYPE_REVERB = 15;
    private static final int EFFECTTYPE_CHORUS = 16;
    private static final int EFFECTTYPE_DISTORTION = 17;
    static final int EFFECTTYPE_REVERSE = 18;
    static final int EFFECTTYPE_INCREASESPEED = 19;
    static final int EFFECTTYPE_DECREASESPEED = 20;
    static final int EFFECTTYPE_RAISEPITCH = 21;
    static final int EFFECTTYPE_LOWERPITCH = 22;
    private static final int EFFECTTYPE_OLDRECORD = 23;
    private static final int EFFECTTYPE_LOWBATTERY = 24;
    private static final int EFFECTTYPE_NOSENSE_STRONG = 25;
    private static final int EFFECTTYPE_NOSENSE_MIDDLE = 26;
    private static final int EFFECTTYPE_NOSENSE_WEAK = 27;
    private static final int EFFECTTYPE_EARTRAINING = 28;
    private static final int EFFECTTYPE_METRONOME = 29;
    static final int EFFECTTYPE_SOUNDEFFECT = 30;
    private static final int EFFECTTYPE_8BITSOUND = 31;
    private static final int SOUNDEFFECTTYPE_RECORDNOISE = 0;
    private static final int SOUNDEFFECTTYPE_ROAROFWAVES = 1;
    private static final int SOUNDEFFECTTYPE_RAIN = 2;
    private static final int SOUNDEFFECTTYPE_RIVER = 3;
    private static final int SOUNDEFFECTTYPE_WAR = 4;
    private static final int SOUNDEFFECTTYPE_FIRE = 5;
    private static final int SOUNDEFFECTTYPE_CONCERTHALL = 6;
    private static final int COMP_GAIN_MAX = 1000;
    private static final int COMP_THRESHOLD_MAX = 6000;
    private static final int COMP_RATIO_MAX = 1900;
    private static final int COMP_ATTACK_MAX = 99999;
    private static final int COMP_RELEASE_MAX = 99999;
    private static final int PAN_VALUE_MAX = 200;
    private static final int FREQ_VALUE_MAX = 390;
    private static final int METRONOME_VALUE_MAX = 390;
    private static final int ECHO_DRY_MAX = 200;
    private static final int ECHO_WET_MAX = 200;
    private static final int ECHO_FEEDBACK_MAX = 98;
    private static final int ECHO_DELAY_MAX = 199;
    private static final int REVERB_DRY_MAX = 100;
    private static final int REVERB_WET_MAX = 300;
    private static final int REVERB_ROOMSIZE_MAX = 100;
    private static final int REVERB_DAMP_MAX = 100;
    private static final int REVERB_WIDTH_MAX = 100;
    private static final int CHORUS_DRY_MAX = 100;
    private static final int CHORUS_WET_MAX = 50;
    private static final int CHORUS_FEEDBACK_MAX = 50;
    private static final int CHORUS_MINSWEEP_MAX = 10000;
    private static final int CHORUS_MAXSWEEP_MAX = 10000;
    private static final int CHORUS_RATE_MAX = 10000;
    private static final int DISTORTION_DRIVE_MAX = 500;
    private static final int DISTORTION_DRY_MAX = 500;
    private static final int DISTORTION_WET_MAX = 500;
    private static final int DISTORTION_FEEDBACK_MAX = 100;
    private static final int DISTORTION_VOLUME_MAX = 200;
    private static Timer sTimer;
    private static Handler sHandler;
    private final Handler mHandlerLongClick;
    private static int sSEStream, sSEStream2, sSync;
    private static boolean sSE1PlayingFlag;
    private boolean mSorting, mAddTemplate;
    private static float sAccel = 0.0f, sVelo1 = 0.0f, sVelo2 = 0.0f;
    private boolean mContinueFlag = true;
    static int sEffectDetail = -1, sReverbSelected = -1, sEchoSelected = -1, sChorusSelected = -1, sDistortionSelected = -1, sCompSelected = -1, sPanSelected = -1, sFreqSelected = -1, sMetronomeSelected = -1, sSoundEffectSelected = -1;
    private RecyclerView mRecyclerEffects, mRecyclerEffectTemplates;
    private TextView mTextEffectName, mTextCompGain, mTextCompThreshold, mTextCompRatio, mTextCompAttack, mTextCompRelease, mTextPanValue, mTextFreqValue, mTextMetronomeValue, mTextEchoDry, mTextEchoWet, mTextEchoFeedback, mTextEchoDelay, mTextReverbDry, mTextReverbWet, mTextReverbRoomSize, mTextReverbDamp, mTextReverbWidth, mTextChorusDry, mTextChorusWet, mTextChorusFeedback, mTextChorusMinSweep, mTextChorusMaxSweep, mTextChorusRate, mTextDistortionDrive, mTextDistortionDry, mTextDistortionWet, mTextDistortionFeedback, mTextDistortionVolume, mTextSoundEffectVolume, mTextFinishSortEffect, mTextCompGainLabel, mTextCompThresholdLabel, mTextCompRatioLabel, mTextCompAttackLabel, mTextCompReleaseLabel, mTextPanValueLabel, mTextFreqValueLabel, mTextMetronomeValueLabel, mTextEchoDryLabel, mTextEchoWetLabel, mTextEchoFeedbackLabel, mTextEchoDelayLabel, mTextReverbDryLabel, mTextReverbWetLabel, mTextReverbRoomSizeLabel, mTextReverbDampLabel, mTextReverbWidthLabel, mTextChorusDryLabel, mTextChorusWetLabel, mTextChorusFeedbackLabel, mTextChorusMinSweepLabel, mTextChorusMaxSweepLabel, mTextChorusRateLabel, mTextDistortionDriveLabel, mTextDistortionDryLabel, mTextDistortionWetLabel, mTextDistortionFeedbackLabel, mTextDistortionVolumeLabel, mTextSoundEffectVolumeLabel, mTextTimeSpecifiedEffectDetail, mTextSpeedSpecifiedEffectDetail, mTextSpeedLoopEffectDetail;
    private RelativeLayout mRelativeEffectDetail, mRelativeSpecifiedEffectDetail, mRelativeLoopEffectDetail, mRelativeEffectTemplates, mRelativeEffectTitle, mRelativeComp, mRelativePan, mRelativeFreq, mRelativeMetronome, mRelativeEcho, mRelativeReverb, mRelativeChorus, mRelativeDistortion, mRelativeSoundEffect;
    private SeekBar mSeekCompGain, mSeekCompThreshold, mSeekCompRatio, mSeekCompAttack, mSeekCompRelease, mSeekPanValue, mSeekFreqValue, mSeekMetronomeValue, mSeekEchoDry, mSeekEchoWet, mSeekEchoFeedback, mSeekEchoDelay, mSeekReverbDry, mSeekReverbWet, mSeekReverbRoomSize, mSeekReverbDamp, mSeekReverbWidth, mSeekChorusDry, mSeekChorusWet, mSeekChorusFeedback, mSeekChorusMinSweep, mSeekChorusMaxSweep, mSeekChorusRate, mSeekDistortionDrive, mSeekDistortionDry, mSeekDistortionWet, mSeekDistortionFeedback, mSeekDistortionVolume, mSeekSoundEffectVolume;
    private ImageButton mBtnCompGainMinus, mBtnCompGainPlus, mBtnCompThresholdMinus, mBtnCompThresholdPlus, mBtnCompRatioMinus, mBtnCompRatioPlus, mBtnCompAttackMinus, mBtnCompAttackPlus, mBtnCompReleaseMinus, mBtnCompReleasePlus, mBtnPanValueMinus, mBtnPanValuePlus, mBtnFreqValueMinus, mBtnFreqValuePlus, mBtnMetronomeValueMinus, mBtnMetronomeValuePlus, mBtnEchoDryMinus, mBtnEchoDryPlus, mBtnEchoWetMinus, mBtnEchoWetPlus, mBtnEchoFeedbackMinus, mBtnEchoFeedbackPlus, mBtnEchoDelayMinus, mBtnEchoDelayPlus, mBtnReverbDryMinus, mBtnReverbDryPlus, mBtnReverbWetMinus, mBtnReverbWetPlus, mBtnReverbRoomSizeMinus, mBtnReverbRoomSizePlus, mBtnReverbDampMinus, mBtnReverbDampPlus, mBtnReverbWidthMinus, mBtnReverbWidthPlus, mBtnChorusDryMinus, mBtnChorusDryPlus, mBtnChorusWetMinus, mBtnChorusWetPlus, mBtnChorusFeedbackMinus, mBtnChorusFeedbackPlus, mBtnChorusMinSweepMinus, mBtnChorusMinSweepPlus, mBtnChorusMaxSweepMinus, mBtnChorusMaxSweepPlus, mBtnChorusRateMinus, mBtnChorusRatePlus, mBtnDistortionDriveMinus, mBtnDistortionDrivePlus, mBtnDistortionDryMinus, mBtnDistortionDryPlus, mBtnDistortionWetMinus, mBtnDistortionWetPlus, mBtnDistortionFeedbackMinus, mBtnDistortionFeedbackPlus, mBtnDistortionVolumeMinus, mBtnDistortionVolumePlus, mBtnSoundEffectVolumeMinus, mBtnSoundEffectVolumePlus;
    private Button mBtnEffectOff, mBtnEffectBack, mBtnEffectFinish, mBtnEffectTemplateOff, mBtnReverbSaveAs, mBtnEchoSaveAs, mBtnChorusSaveAs, mBtnDistortionSaveAs, mBtnCompSaveAs, mBtnCompRandom, mBtnResetComp, mBtnPanSaveAs, mBtnPanRandom, mBtnResetPan, mBtnFreqSaveAs, mBtnFreqRandom, mBtnResetFreq, mBtnMetronomeSaveAs, mBtnMetronomeRandom, mBtnResetMetronome, mBtnEchoRandom, mBtnResetEcho, mBtnReverbRandom, mBtnResetReverb, mBtnChorusRandom, mBtnResetChorus, mBtnDistortionRandom, mBtnResetDistortion;

    static boolean sSpecifiedEffectDetail = true, sLoopEffectDetail = false;
    private RadioGroup mRadioGroupEffectDetail;
    private RadioButton mRadioButtonSpecifiedEffectDetail, mRadioButtonLoopEffectDetail;
    private EditText mEditSpeedSpecifiedEffectDetail, mEditTimeSpecifiedEffectDetail, mEditPitchSpecifiedEffectDetail, mEditPTimeSpecifiedEffectDetail, mEditSpeedLoopEffectDetail, mEditPitchLoopEffectDetail;

    private AnimationButton mBtnEffectTemplateMenu, mBtnAddEffectTemplate;
    private ScrollView mScrollCompCustomize, mScrollPanCustomize, mScrollFreqCustomize, mScrollMetronomeCustomize, mScrollEchoCustomize, mScrollReverbCustomize, mScrollChorusCustomize, mScrollDistortionCustomize, mScrollSoundEffectCustomize;
    private View mViewSepEffectHeader, mViewSepEffectDetail, mViewSepEffectTemplateHeader;
    private ImageView mImgEffectBack;

    ItemTouchHelper getEffectTemplateTouchHelper() {
        return mEffectTemplateTouchHelper;
    }
    private EffectsAdapter getEffectsAdapter() { return mEffectsAdapter; }
    private Button getBtnEffectOff() { return mBtnEffectOff; }
    private TextView getTextEffectName() { return mTextEffectName; }
    private TextView getTextCompGain() { return mTextCompGain; }
    private TextView getTextCompThreshold() { return mTextCompThreshold; }
    private TextView getTextCompRatio() { return mTextCompRatio; }
    private TextView getTextCompAttack() { return mTextCompAttack; }
    private TextView getTextCompRelease() { return mTextCompRelease; }
    private TextView getTextPanValue() { return mTextPanValue; }
    private TextView getTextFreqValue() { return mTextFreqValue; }
    private TextView getTextMetronomeValue() { return mTextMetronomeValue; }
    private TextView getTextEchoDry() { return mTextEchoDry; }
    private TextView getTextEchoWet() { return mTextEchoWet; }
    private TextView getTextEchoFeedback() { return mTextEchoFeedback; }
    private TextView getTextEchoDelay() { return mTextEchoDelay; }
    private TextView getTextReverbDry() { return mTextReverbDry; }
    private TextView getTextReverbWet() { return mTextReverbWet; }
    private TextView getTextReverbRoomSize() { return mTextReverbRoomSize; }
    private TextView getTextReverbDamp() { return mTextReverbDamp; }
    private TextView getTextReverbWidth() { return mTextReverbWidth; }
    private TextView getTextChorusDry() { return mTextChorusDry; }
    private TextView getTextChorusWet() { return mTextChorusWet; }
    private TextView getTextChorusFeedback() { return mTextChorusFeedback; }
    private TextView getTextChorusMinSweep() { return mTextChorusMinSweep; }
    private TextView getTextChorusMaxSweep() { return mTextChorusMaxSweep; }
    private TextView getTextChorusRate() { return mTextChorusRate; }
    private TextView getTextDistortionDrive() { return mTextDistortionDrive; }
    private TextView getTextDistortionDry() { return mTextDistortionDry; }
    private TextView getTextDistortionWet() { return mTextDistortionWet; }
    private TextView getTextDistortionFeedback() { return mTextDistortionFeedback; }
    private TextView getTextDistortionVolume() { return mTextDistortionVolume; }
    private TextView getTextSoundEffectVolume() { return mTextSoundEffectVolume; }
    private SeekBar getSeekCompGain() { return mSeekCompGain; }
    private SeekBar getSeekCompThreshold() { return mSeekCompThreshold; }
    private SeekBar getSeekCompRatio() { return mSeekCompRatio; }
    private SeekBar getSeekCompAttack() { return mSeekCompAttack; }
    private SeekBar getSeekCompRelease() { return mSeekCompRelease; }
    private SeekBar getSeekPanValue() { return mSeekPanValue; }
    private SeekBar getSeekFreqValue() { return mSeekFreqValue; }
    private SeekBar getSeekMetronomeValue() { return mSeekMetronomeValue; }
    private SeekBar getSeekEchoDry() { return mSeekEchoDry; }
    private SeekBar getSeekEchoWet() { return mSeekEchoWet; }
    private SeekBar getSeekEchoFeedback() { return mSeekEchoFeedback; }
    private SeekBar getSeekEchoDelay() { return mSeekEchoDelay; }
    private SeekBar getSeekReverbDry() { return mSeekReverbDry; }
    private SeekBar getSeekReverbWet() { return mSeekReverbWet; }
    private SeekBar getSeekReverbRoomSize() { return mSeekReverbRoomSize; }
    private SeekBar getSeekReverbDamp() { return mSeekReverbDamp; }
    private SeekBar getSeekReverbWidth() { return mSeekReverbWidth; }
    private SeekBar getSeekChorusDry() { return mSeekChorusDry; }
    private SeekBar getSeekChorusWet() { return mSeekChorusWet; }
    private SeekBar getSeekChorusFeedback() { return mSeekChorusFeedback; }
    private SeekBar getSeekChorusMinSweep() { return mSeekChorusMinSweep; }
    private SeekBar getSeekChorusMaxSweep() { return mSeekChorusMaxSweep; }
    private SeekBar getSeekChorusRate() { return mSeekChorusRate; }
    private SeekBar getSeekDistortionDrive() { return mSeekDistortionDrive; }
    private SeekBar getSeekDistortionDry() { return mSeekDistortionDry; }
    private SeekBar getSeekDistortionWet() { return mSeekDistortionWet; }
    private SeekBar getSeekDistortionFeedback() { return mSeekDistortionFeedback; }
    private SeekBar getSeekDistortionVolume() { return mSeekDistortionVolume; }
    private SeekBar getSeekSoundEffectVolume() { return mSeekSoundEffectVolume; }
    private ScrollView getScrollCompCustomize() { return mScrollCompCustomize; }
    private ScrollView getScrollPanCustomize() { return mScrollPanCustomize; }
    private ScrollView getScrollFreqCustomize() { return mScrollFreqCustomize; }
    private ScrollView getScrollMetronomeCustomize() { return mScrollMetronomeCustomize; }
    private ScrollView getScrollEchoCustomize() { return mScrollEchoCustomize; }
    private ScrollView getScrollReverbCustomize() { return mScrollReverbCustomize; }
    private ScrollView getScrollChorusCustomize() { return mScrollChorusCustomize; }
    private ScrollView getScrollDistortionCustomize() { return mScrollDistortionCustomize; }
    private ScrollView getScrollSoundEffectCustomize() { return mScrollSoundEffectCustomize; }
    private RelativeLayout getRelativeEffectTemplates() { return mRelativeEffectTemplates; }
    private Button getBtnEffectTemplateOff() { return mBtnEffectTemplateOff; }
    private EffectTemplatesAdapter getEffectTemplatesAdapter() { return mEffectTemplatesAdapter; }
    private EditText getEditSpeedSpecifiedEffectDetail() { return mEditSpeedSpecifiedEffectDetail; }
    private EditText getEditTimeSpecifiedEffectDetail() { return mEditTimeSpecifiedEffectDetail; }
    private EditText getEditPitchSpecifiedEffectDetail() { return mEditPitchSpecifiedEffectDetail; }
    private EditText getEditPTimeSpecifiedEffectDetail() { return mEditPTimeSpecifiedEffectDetail; }
    private EditText getEditSpeedLoopEffectDetail() { return mEditSpeedLoopEffectDetail; }
    private EditText getEditPitchLoopEffectDetail() { return mEditPitchLoopEffectDetail; }
    RadioGroup getRadioGroupEffectDetail() { return mRadioGroupEffectDetail; }

    public static void setTimeOfRaisePitch(float timeOfRaisePitch) {
        sTimeOfRaisePitchSpecified = timeOfRaisePitch;
        if (sActivity != null) {
            if (sActivity.effectFragment.getTextEffectName().getText().toString().equals(sEffectItems.get(EFFECTTYPE_RAISEPITCH).getEffectName()))
                sActivity.effectFragment.getEditPTimeSpecifiedEffectDetail().setText(String.format(Locale.getDefault(), "%.1f%s", sTimeOfRaisePitchSpecified, sActivity.getString(R.string.sec)));
        }
    }

    public static void setRaisePitchSpecified(float raisePitch) {
        sRaisePitchSpecified = raisePitch;
        if (sActivity != null) {
            if (sActivity.effectFragment.getTextEffectName().getText().toString().equals(sEffectItems.get(EFFECTTYPE_RAISEPITCH).getEffectName())) {
                sActivity.effectFragment.getEditPitchSpecifiedEffectDetail().setText(String.format(Locale.getDefault(), "%s%.1f", sActivity.getString(R.string.sharp), sRaisePitchSpecified));
                sActivity.effectFragment.getEditPitchLoopEffectDetail().setText(String.format(Locale.getDefault(), "%s%.1f", sActivity.getString(R.string.sharp), sRaisePitchLoop));
            }
        }
    }

    public static void setRaisePitchLoop(float raisePitch) {
        sRaisePitchLoop = raisePitch;
        if (sActivity != null) {
            if (sActivity.effectFragment.getTextEffectName().getText().toString().equals(sEffectItems.get(EFFECTTYPE_RAISEPITCH).getEffectName()))
                sActivity.effectFragment.getEditPitchLoopEffectDetail().setText(String.format(Locale.getDefault(), "%s%.1f", sActivity.getString(R.string.sharp), sRaisePitchLoop));
        }
    }

    public static void setTimeOfLowerPitch(float timeOfLowerPitch) {
        sTimeOfLowerPitchSpecified = timeOfLowerPitch;
        if (sActivity != null) {
            if (sActivity.effectFragment.getTextEffectName().getText().toString().equals(sEffectItems.get(EFFECTTYPE_LOWERPITCH).getEffectName()))
                sActivity.effectFragment.getEditPTimeSpecifiedEffectDetail().setText(String.format(Locale.getDefault(), "%.1f%s", sTimeOfLowerPitchSpecified, sActivity.getString(R.string.sec)));
        }
    }

    public static void setLowerPitchSpecified(float lowerPitch) {
        sLowerPitchSpecified = lowerPitch;
        if (sActivity != null) {
            if (sActivity.effectFragment.getTextEffectName().getText().toString().equals(sEffectItems.get(EFFECTTYPE_LOWERPITCH).getEffectName()))
                sActivity.effectFragment.getEditPitchSpecifiedEffectDetail().setText(String.format(Locale.getDefault(), "%s%.1f", sActivity.getString(R.string.flat), sLowerPitchSpecified));
        }
    }

    public static void setLowerPitchLoop(float lowerPitch) {
        sLowerPitchLoop = lowerPitch;
        if (sActivity != null) {
            if (sActivity.effectFragment.getTextEffectName().getText().toString().equals(sEffectItems.get(EFFECTTYPE_LOWERPITCH).getEffectName()))
                sActivity.effectFragment.getEditPitchLoopEffectDetail().setText(String.format(Locale.getDefault(), "%s%.1f", sActivity.getString(R.string.flat), sLowerPitchLoop));
        }
    }

    public static void setTimeOfIncreaseSpeed(float timeOfIncreaseSpeed) {
        sTimeOfIncreaseSpeedSpecified = timeOfIncreaseSpeed;
        if (sActivity != null) {
            if (sActivity.effectFragment.getTextEffectName().getText().toString().equals(sEffectItems.get(EFFECTTYPE_INCREASESPEED).getEffectName()))
                sActivity.effectFragment.getEditTimeSpecifiedEffectDetail().setText(String.format(Locale.getDefault(), "%.1f%s", sTimeOfIncreaseSpeedSpecified, sActivity.getString(R.string.sec)));
        }
    }

    public static void setIncreaseSpeedSpecified(float increaseSpeed) {
        sIncreaseSpeedSpecified = increaseSpeed;
        if (sActivity != null) {
            if (sActivity.effectFragment.getTextEffectName().getText().toString().equals(sEffectItems.get(EFFECTTYPE_INCREASESPEED).getEffectName()))
                sActivity.effectFragment.getEditSpeedSpecifiedEffectDetail().setText(String.format(Locale.getDefault(), "%.1f%%", sIncreaseSpeedSpecified));
        }
    }

    public static void setIncreaseSpeedLoop(float increaseSpeed) {
        sIncreaseSpeedLoop = increaseSpeed;
        if (sActivity != null) {
            if (sActivity.effectFragment.getTextEffectName().getText().toString().equals(sEffectItems.get(EFFECTTYPE_INCREASESPEED).getEffectName()))
                sActivity.effectFragment.getEditSpeedLoopEffectDetail().setText(String.format(Locale.getDefault(), "%.1f%%", sIncreaseSpeedLoop));
        }
    }

    public static void setTimeOfDecreaseSpeed(float timeOfDecreaseSpeed) {
        sTimeOfDecreaseSpeedSpecified = timeOfDecreaseSpeed;
        if (sActivity != null) {
            if (sActivity.effectFragment.getTextEffectName().getText().toString().equals(sEffectItems.get(EFFECTTYPE_DECREASESPEED).getEffectName()))
                sActivity.effectFragment.getEditTimeSpecifiedEffectDetail().setText(String.format(Locale.getDefault(), "%.1f%s", sTimeOfDecreaseSpeedSpecified, sActivity.getString(R.string.sec)));
        }
    }

    public static void setDecreaseSpeedSpecified(float decreaseSpeed) {
        sDecreaseSpeedSpecified = decreaseSpeed;
        if (sActivity != null) {
            if (sActivity.effectFragment.getTextEffectName().getText().toString().equals(sEffectItems.get(EFFECTTYPE_DECREASESPEED).getEffectName()))
                sActivity.effectFragment.getEditSpeedSpecifiedEffectDetail().setText(String.format(Locale.getDefault(), "%.1f%%", sDecreaseSpeedSpecified));
        }
    }

    public static void setDecreaseSpeedLoop(float decreaseSpeed) {
        sDecreaseSpeedLoop = decreaseSpeed;
        if (sActivity != null) {
            if (sActivity.effectFragment.getTextEffectName().getText().toString().equals(sEffectItems.get(EFFECTTYPE_DECREASESPEED).getEffectName()))
                sActivity.effectFragment.getEditSpeedLoopEffectDetail().setText(String.format(Locale.getDefault(), "%.1f%%", sDecreaseSpeedLoop));
        }
    }

    private void setReverbItems(ArrayList<EffectTemplateItem> lists) {
        sReverbItems = lists;
        mEffectTemplatesAdapter.changeItems(sReverbItems);
    }

    private void setEchoItems(ArrayList<EffectTemplateItem> lists) {
        sEchoItems = lists;
        mEffectTemplatesAdapter.changeItems(sEchoItems);
    }

    private void setChorusItems(ArrayList<EffectTemplateItem> lists) {
        sChorusItems = lists;
        mEffectTemplatesAdapter.changeItems(sChorusItems);
    }

    private void setDistortionItems(ArrayList<EffectTemplateItem> lists) {
        sDistortionItems = lists;
        mEffectTemplatesAdapter.changeItems(sDistortionItems);
    }

    private void setCompItems(ArrayList<EffectTemplateItem> lists) {
        sCompItems = lists;
        mEffectTemplatesAdapter.changeItems(sCompItems);
    }

    private void setPanItems(ArrayList<EffectTemplateItem> lists) {
        sPanItems = lists;
        mEffectTemplatesAdapter.changeItems(sPanItems);
    }

    private void setFreqItems(ArrayList<EffectTemplateItem> lists) {
        sFreqItems = lists;
        mEffectTemplatesAdapter.changeItems(sFreqItems);
    }

    private void setMetronomeItems(ArrayList<EffectTemplateItem> lists) {
        sMetronomeItems = lists;
        mEffectTemplatesAdapter.changeItems(sMetronomeItems);
    }

    private void setSoundEffectItems(ArrayList<EffectTemplateItem> lists) {
        sSoundEffectItems = lists;
        mEffectTemplatesAdapter.changeItems(sSoundEffectItems);
    }

    public boolean isSorting() {
        return mSorting;
    }

    boolean isSelectedItem(int nItem) {
        if (nItem >= sEffectItems.size()) return false;
        EffectItem item = sEffectItems.get(nItem);
        return item.isSelected();
    }

    boolean isSelectedTemplateItem(int nItem) {
        if (sEffectDetail == EFFECTTYPE_REVERB) {
            if (nItem >= sReverbItems.size()) return false;
            EffectTemplateItem item = sReverbItems.get(nItem);
            return item.isSelected();
        } else if (sEffectDetail == EFFECTTYPE_ECHO) {
            if (nItem >= sEchoItems.size()) return false;
            EffectTemplateItem item = sEchoItems.get(nItem);
            return item.isSelected();
        } else if (sEffectDetail == EFFECTTYPE_CHORUS) {
            if (nItem >= sChorusItems.size()) return false;
            EffectTemplateItem item = sChorusItems.get(nItem);
            return item.isSelected();
        } else if (sEffectDetail == EFFECTTYPE_DISTORTION) {
            if (nItem >= sDistortionItems.size()) return false;
            EffectTemplateItem item = sDistortionItems.get(nItem);
            return item.isSelected();
        } else if (sEffectDetail == EFFECTTYPE_COMP) {
            if (nItem >= sCompItems.size()) return false;
            EffectTemplateItem item = sCompItems.get(nItem);
            return item.isSelected();
        } else if (sEffectDetail == EFFECTTYPE_PAN) {
            if (nItem >= sPanItems.size()) return false;
            EffectTemplateItem item = sPanItems.get(nItem);
            return item.isSelected();
        } else if (sEffectDetail == EFFECTTYPE_FREQ) {
            if (nItem >= sFreqItems.size()) return false;
            EffectTemplateItem item = sFreqItems.get(nItem);
            return item.isSelected();
        } else if (sEffectDetail == EFFECTTYPE_METRONOME) {
            if (nItem >= sMetronomeItems.size()) return false;
            EffectTemplateItem item = sMetronomeItems.get(nItem);
            return item.isSelected();
        } else if (sEffectDetail == EFFECTTYPE_SOUNDEFFECT) {
            if (nItem >= sSoundEffectItems.size()) return false;
            EffectTemplateItem item = sSoundEffectItems.get(nItem);
            return item.isSelected();
        }
        return false;
    }

    public static boolean isReverse() {
        if (EFFECTTYPE_REVERSE >= sEffectItems.size()) return false;
        return sEffectItems.get(EFFECTTYPE_REVERSE).isSelected();
    }

    public static void setEffectItems(ArrayList<EffectItem> effectItems) {
        boolean bSelected = false;
        for (int i = 0; i < sEffectItems.size(); i++) {
            EffectItem item = sEffectItems.get(i);
            for (int j = 0; j < effectItems.size(); j++) {
                EffectItem itemSaved = effectItems.get(j);
                if (item.getEffectName().equals(itemSaved.getEffectName())) {
                    item.setSelected(itemSaved.isSelected());
                    if (itemSaved.isSelected()) bSelected = true;
                    if(sActivity != null) sActivity.effectFragment.getEffectsAdapter().notifyItemChanged(i);
                }
            }
        }
        if(sActivity != null) sActivity.effectFragment.getBtnEffectOff().setSelected(!bSelected);
    }

    public static void setReverbSelected(int nSelected) {
        sReverbSelected = nSelected;
        for (int i = 0; i < sReverbItems.size(); i++) sReverbItems.get(i).setSelected(i == nSelected);
        if(sActivity != null) {
            if (sActivity.effectFragment.getRelativeEffectTemplates().getVisibility() == View.VISIBLE && sEffectDetail == EFFECTTYPE_REVERB) {
                sActivity.effectFragment.getBtnEffectTemplateOff().setSelected(nSelected == -1);
                sActivity.effectFragment.getEffectTemplatesAdapter().notifyDataSetChanged();
            }
        }
    }

    public static void setEchoSelected(int selected) {
        sEchoSelected = selected;
        for (int i = 0; i < sEchoItems.size(); i++) sEchoItems.get(i).setSelected(i == selected);
        if (sActivity != null) {
            if (sActivity.effectFragment.getRelativeEffectTemplates().getVisibility()
                    == View.VISIBLE && sEffectDetail == EFFECTTYPE_ECHO) {
                sActivity.effectFragment.getBtnEffectTemplateOff().setSelected(selected == -1);
                sActivity.effectFragment.getEffectTemplatesAdapter().notifyDataSetChanged();
            }
        }
    }

    public static void setChorusSelected(int nSelected) {
        sChorusSelected = nSelected;
        for (int i = 0; i < sChorusItems.size(); i++) sChorusItems.get(i).setSelected(i == nSelected);
        if (sActivity != null) {
            if (sActivity.effectFragment.getRelativeEffectTemplates().getVisibility() == View.VISIBLE && sEffectDetail == EFFECTTYPE_CHORUS) {
                sActivity.effectFragment.getBtnEffectTemplateOff().setSelected(nSelected == -1);
                sActivity.effectFragment.getEffectTemplatesAdapter().notifyDataSetChanged();
            }
        }
    }

    public static void setDistortionSelected(int nSelected) {
        sDistortionSelected = nSelected;
        for (int i = 0; i < sDistortionItems.size(); i++) sDistortionItems.get(i).setSelected(i == nSelected);
        if (sActivity != null) {
            if (sActivity.effectFragment.getRelativeEffectTemplates().getVisibility() == View.VISIBLE && sEffectDetail == EFFECTTYPE_DISTORTION) {
                sActivity.effectFragment.getBtnEffectTemplateOff().setSelected(nSelected == -1);
                sActivity.effectFragment.getEffectTemplatesAdapter().notifyDataSetChanged();
            }
        }
    }

    public static void setCompSelected(int nSelected) {
        sCompSelected = nSelected;
        for (int i = 0; i < sCompItems.size(); i++) sCompItems.get(i).setSelected(i == nSelected);
        if (sActivity != null) {
            if (sActivity.effectFragment.getRelativeEffectTemplates().getVisibility() == View.VISIBLE && sEffectDetail == EFFECTTYPE_COMP) {
                sActivity.effectFragment.getBtnEffectTemplateOff().setSelected(nSelected == -1);
                sActivity.effectFragment.getEffectTemplatesAdapter().notifyDataSetChanged();
            }
        }
    }

    public static void setPanSelected(int nSelected) {
        sPanSelected = nSelected;
        for (int i = 0; i < sPanItems.size(); i++) sPanItems.get(i).setSelected(i == nSelected);
        if (sActivity != null) {
            if (sActivity.effectFragment.getRelativeEffectTemplates().getVisibility() == View.VISIBLE && sEffectDetail == EFFECTTYPE_PAN) {
                sActivity.effectFragment.getBtnEffectTemplateOff().setSelected(nSelected == -1);
                sActivity.effectFragment.getEffectTemplatesAdapter().notifyDataSetChanged();
            }
        }
    }

    public static void setFreqSelected(int nSelected) {
        sFreqSelected = nSelected;
        for (int i = 0; i < sFreqItems.size(); i++) sFreqItems.get(i).setSelected(i == nSelected);
        if (sActivity != null) {
            if (sActivity.effectFragment.getRelativeEffectTemplates().getVisibility() == View.VISIBLE && sEffectDetail == EFFECTTYPE_FREQ) {
                sActivity.effectFragment.getBtnEffectTemplateOff().setSelected(nSelected == -1);
                sActivity.effectFragment.getEffectTemplatesAdapter().notifyDataSetChanged();
            }
        }
    }

    public static void setMetronomeSelected(int nSelected) {
        sMetronomeSelected = nSelected;
        for (int i = 0; i < sMetronomeItems.size(); i++) sMetronomeItems.get(i).setSelected(i == nSelected);
        if (sActivity != null) {
            if (sActivity.effectFragment.getRelativeEffectTemplates().getVisibility() == View.VISIBLE && sEffectDetail == EFFECTTYPE_METRONOME) {
                sActivity.effectFragment.getBtnEffectTemplateOff().setSelected(nSelected == -1);
                sActivity.effectFragment.getEffectTemplatesAdapter().notifyDataSetChanged();
            }
        }
    }

    public static void setSoundEffectSelected(int nSelected) {
        sSoundEffectSelected = nSelected;
        for (int i = 0; i < sSoundEffectItems.size(); i++) sSoundEffectItems.get(i).setSelected(i == nSelected);
        if (sActivity != null) {
            if (sActivity.effectFragment.getRelativeEffectTemplates().getVisibility() == View.VISIBLE && sEffectDetail == EFFECTTYPE_SOUNDEFFECT) {
                sActivity.effectFragment.getBtnEffectTemplateOff().setSelected(nSelected == -1);
                sActivity.effectFragment.getEffectTemplatesAdapter().notifyDataSetChanged();
            }
        }
    }

    public EffectFragment() {
        if(MainActivity.sStream == 0) sEffectItems = new ArrayList<>();
        if(sReverbItems == null) sReverbItems = new ArrayList<>();
        if(sEchoItems == null) sEchoItems = new ArrayList<>();
        if(sChorusItems == null) sChorusItems = new ArrayList<>();
        if(sDistortionItems == null) sDistortionItems = new ArrayList<>();
        if(sCompItems == null) sCompItems = new ArrayList<>();
        if(sPanItems == null) sPanItems = new ArrayList<>();
        if(sFreqItems == null) sFreqItems = new ArrayList<>();
        if(sMetronomeItems == null) sMetronomeItems = new ArrayList<>();
        if(sSoundEffectItems == null) sSoundEffectItems = new ArrayList<>();
        mHandlerLongClick = new Handler();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_effect, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity)
            sActivity = (MainActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        sActivity = null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnEffectOff) {
            resetEffect();
            mEffectsAdapter.notifyDataSetChanged();
            if (sSEStream != 0) {
                BASS.BASS_StreamFree(sSEStream);
                sSEStream = 0;
            }
            if (sSEStream2 != 0) {
                BASS.BASS_StreamFree(sSEStream2);
                sSEStream2 = 0;
            }
            if (sHandler != null) {
                sHandler.removeCallbacks(onTimer);
                sHandler = null;
            }
            applyEffect();
            PlaylistFragment.updateSavingEffect();
        } else if (v.getId() == R.id.btnEffectBack) {
            if (mScrollReverbCustomize.getVisibility() == View.VISIBLE) {
                mScrollReverbCustomize.setVisibility(View.INVISIBLE);
                if (sReverbSelected != -1) {
                    ArrayList<Float> arFloats = sReverbItems.get(sReverbSelected).getArPresets();
                    setReverb(arFloats.get(0), arFloats.get(1), arFloats.get(2), arFloats.get(3), arFloats.get(4), true);
                } else resetReverb();
                mBtnEffectFinish.setVisibility(View.GONE);
                mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                mTextEffectName.setText(sEffectItems.get(sEffectDetail).getEffectName());
                mBtnEffectBack.setText(R.string.back);
                mImgEffectBack.setVisibility(View.VISIBLE);
                mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
                if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                    mBtnAddEffectTemplate.setAlpha(1.0f);
            } else if (mScrollEchoCustomize.getVisibility() == View.VISIBLE) {
                mScrollEchoCustomize.setVisibility(View.INVISIBLE);
                if (sEchoSelected != -1) {
                    ArrayList<Float> arFloats = sEchoItems.get(sEchoSelected).getArPresets();
                    setEcho(arFloats.get(0), arFloats.get(1), arFloats.get(2), arFloats.get(3), true);
                } else resetEcho();
                mBtnEffectFinish.setVisibility(View.GONE);
                mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                mTextEffectName.setText(sEffectItems.get(sEffectDetail).getEffectName());
                mBtnEffectBack.setText(R.string.back);
                mImgEffectBack.setVisibility(View.VISIBLE);
                mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
                if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                    mBtnAddEffectTemplate.setAlpha(1.0f);
            } else if (mScrollChorusCustomize.getVisibility() == View.VISIBLE) {
                mScrollChorusCustomize.setVisibility(View.INVISIBLE);
                if (sChorusSelected != -1) {
                    ArrayList<Float> arFloats = sChorusItems.get(sChorusSelected).getArPresets();
                    setChorus(arFloats.get(0), arFloats.get(1), arFloats.get(2), arFloats.get(3), arFloats.get(4), arFloats.get(5), true);
                } else resetChorus();
                mBtnEffectFinish.setVisibility(View.GONE);
                mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                mTextEffectName.setText(sEffectItems.get(sEffectDetail).getEffectName());
                mBtnEffectBack.setText(R.string.back);
                mImgEffectBack.setVisibility(View.VISIBLE);
                mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
                if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                    mBtnAddEffectTemplate.setAlpha(1.0f);
            } else if (mScrollDistortionCustomize.getVisibility() == View.VISIBLE) {
                mScrollDistortionCustomize.setVisibility(View.INVISIBLE);
                if (sDistortionSelected != -1) {
                    ArrayList<Float> arFloats = sDistortionItems.get(sDistortionSelected).getArPresets();
                    setDistortion(arFloats.get(0), arFloats.get(1), arFloats.get(2), arFloats.get(3), arFloats.get(4), true);
                } else resetDistortion();
                mBtnEffectFinish.setVisibility(View.GONE);
                mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                mTextEffectName.setText(sEffectItems.get(sEffectDetail).getEffectName());
                mBtnEffectBack.setText(R.string.back);
                mImgEffectBack.setVisibility(View.VISIBLE);
                mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
                if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                    mBtnAddEffectTemplate.setAlpha(1.0f);
            } else if (mScrollCompCustomize.getVisibility() == View.VISIBLE) {
                mScrollCompCustomize.setVisibility(View.INVISIBLE);
                if (sCompSelected != -1) {
                    ArrayList<Float> arFloats = sCompItems.get(sCompSelected).getArPresets();
                    setComp(arFloats.get(0), arFloats.get(1), arFloats.get(2), arFloats.get(3), arFloats.get(4), true);
                } else resetComp();
                mBtnEffectFinish.setVisibility(View.GONE);
                mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                mTextEffectName.setText(sEffectItems.get(sEffectDetail).getEffectName());
                mBtnEffectBack.setText(R.string.back);
                mImgEffectBack.setVisibility(View.VISIBLE);
                mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
                if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                    mBtnAddEffectTemplate.setAlpha(1.0f);
            } else if (mScrollPanCustomize.getVisibility() == View.VISIBLE) {
                mScrollPanCustomize.setVisibility(View.INVISIBLE);
                if (sPanSelected != -1) {
                    ArrayList<Float> arFloats = sPanItems.get(sPanSelected).getArPresets();
                    setPan(arFloats.get(0), true);
                } else resetPan();
                mBtnEffectFinish.setVisibility(View.GONE);
                mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                mTextEffectName.setText(sEffectItems.get(sEffectDetail).getEffectName());
                mBtnEffectBack.setText(R.string.back);
                mImgEffectBack.setVisibility(View.VISIBLE);
                mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
                if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                    mBtnAddEffectTemplate.setAlpha(1.0f);
            } else if (mScrollFreqCustomize.getVisibility() == View.VISIBLE) {
                mScrollFreqCustomize.setVisibility(View.INVISIBLE);
                if (sFreqSelected != -1) {
                    ArrayList<Float> arFloats = sFreqItems.get(sFreqSelected).getArPresets();
                    setFreq(arFloats.get(0), true);
                } else resetFreq();
                mBtnEffectFinish.setVisibility(View.GONE);
                mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                mTextEffectName.setText(sEffectItems.get(sEffectDetail).getEffectName());
                mBtnEffectBack.setText(R.string.back);
                mImgEffectBack.setVisibility(View.VISIBLE);
                mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
                if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                    mBtnAddEffectTemplate.setAlpha(1.0f);
            } else if (mScrollMetronomeCustomize.getVisibility() == View.VISIBLE) {
                mScrollMetronomeCustomize.setVisibility(View.INVISIBLE);
                if (sMetronomeSelected != -1) {
                    ArrayList<Float> arFloats = sMetronomeItems.get(sMetronomeSelected).getArPresets();
                    setMetronome(arFloats.get(0), true);
                } else resetMetronome();
                mBtnEffectFinish.setVisibility(View.GONE);
                mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                mTextEffectName.setText(sEffectItems.get(sEffectDetail).getEffectName());
                mBtnEffectBack.setText(R.string.back);
                mImgEffectBack.setVisibility(View.VISIBLE);
                mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
                if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                    mBtnAddEffectTemplate.setAlpha(1.0f);
            } else if (mScrollSoundEffectCustomize.getVisibility() == View.VISIBLE) {
                mScrollSoundEffectCustomize.setVisibility(View.INVISIBLE);
                if (sSoundEffectSelected != -1) {
                    ArrayList<Float> arFloats = sSoundEffectItems.get(sSoundEffectSelected).getArPresets();
                    setSoundEffect(arFloats.get(0), true);
                } else resetSoundEffect();
                mBtnEffectFinish.setVisibility(View.GONE);
                mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                mTextEffectName.setText(sEffectItems.get(sEffectDetail).getEffectName());
                mBtnEffectBack.setText(R.string.back);
                mImgEffectBack.setVisibility(View.VISIBLE);
                mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
                if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                    mBtnAddEffectTemplate.setAlpha(1.0f);
            } else {
                mRelativeEffectDetail.setVisibility(View.GONE);
                mRelativeEffectTemplates.setVisibility(View.INVISIBLE);
                mBtnEffectOff.setVisibility(View.VISIBLE);
                mViewSepEffectHeader.setVisibility(View.VISIBLE);
                mRecyclerEffects.setVisibility(View.VISIBLE);
            }
        } else if (v.getId() == R.id.btnEffectFinish) {
            if (mAddTemplate) {
                AlertDialog.Builder builder;
                if(sActivity.isDarkMode())
                    builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
                else
                    builder = new AlertDialog.Builder(sActivity);
                builder.setTitle(R.string.saveTemplate);
                LinearLayout linearLayout = new LinearLayout(sActivity);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                final ClearableEditText editPreset = new ClearableEditText(sActivity, sActivity.isDarkMode());
                editPreset.setHint(R.string.templateName);
                if (sEffectDetail == EFFECTTYPE_REVERB) editPreset.setText(R.string.newReverb);
                else if (sEffectDetail == EFFECTTYPE_ECHO) editPreset.setText(R.string.newEcho);
                else if (sEffectDetail == EFFECTTYPE_CHORUS) editPreset.setText(R.string.newChorus);
                else if (sEffectDetail == EFFECTTYPE_DISTORTION)
                    editPreset.setText(R.string.newDistortion);
                else if (sEffectDetail == EFFECTTYPE_COMP) editPreset.setText(R.string.newComp);
                else if (sEffectDetail == EFFECTTYPE_PAN) editPreset.setText(R.string.newPan);
                else if (sEffectDetail == EFFECTTYPE_FREQ) editPreset.setText(R.string.newFreq);
                else if (sEffectDetail == EFFECTTYPE_METRONOME) editPreset.setText(R.string.newMetronome);
                linearLayout.addView(editPreset);
                builder.setView(linearLayout);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ArrayList<Float> arPresets = new ArrayList<>();
                        if (sEffectDetail == EFFECTTYPE_REVERB) {
                            arPresets.add(Float.parseFloat((String) mTextReverbDry.getText()));
                            arPresets.add(Float.parseFloat((String) mTextReverbWet.getText()));
                            arPresets.add(Float.parseFloat((String) mTextReverbRoomSize.getText()));
                            arPresets.add(Float.parseFloat((String) mTextReverbDamp.getText()));
                            arPresets.add(Float.parseFloat((String) mTextReverbWidth.getText()));
                            sReverbItems.add(new EffectTemplateItem(editPreset.getText().toString(), arPresets));
                            mEffectTemplatesAdapter.notifyItemInserted(sReverbItems.size() - 1);
                            mScrollReverbCustomize.setVisibility(View.INVISIBLE);
                            for (int i = 0; i < sReverbItems.size() - 1; i++)
                                sReverbItems.get(i).setSelected(false);
                            sReverbItems.get(sReverbItems.size() - 1).setSelected(true);
                            sReverbSelected = sReverbItems.size() - 1;
                        } else if (sEffectDetail == EFFECTTYPE_ECHO) {
                            arPresets.add(Float.parseFloat((String) mTextEchoDry.getText()));
                            arPresets.add(Float.parseFloat((String) mTextEchoWet.getText()));
                            arPresets.add(Float.parseFloat((String) mTextEchoFeedback.getText()));
                            arPresets.add(Float.parseFloat((String) mTextEchoDelay.getText()));
                            sEchoItems.add(new EffectTemplateItem(editPreset.getText().toString(), arPresets));
                            mEffectTemplatesAdapter.notifyItemInserted(sEchoItems.size() - 1);
                            mScrollEchoCustomize.setVisibility(View.INVISIBLE);
                            for (int i = 0; i < sEchoItems.size() - 1; i++)
                                sEchoItems.get(i).setSelected(false);
                            sEchoItems.get(sEchoItems.size() - 1).setSelected(true);
                            sEchoSelected = sEchoItems.size() - 1;
                        } else if (sEffectDetail == EFFECTTYPE_CHORUS) {
                            arPresets.add(Float.parseFloat((String) mTextChorusDry.getText()));
                            arPresets.add(Float.parseFloat((String) mTextChorusWet.getText()));
                            arPresets.add(Float.parseFloat((String) mTextChorusFeedback.getText()));
                            arPresets.add(Float.parseFloat((String) mTextChorusMinSweep.getText()));
                            arPresets.add(Float.parseFloat((String) mTextChorusMaxSweep.getText()));
                            arPresets.add(Float.parseFloat((String) mTextChorusRate.getText()));
                            sChorusItems.add(new EffectTemplateItem(editPreset.getText().toString(), arPresets));
                            mEffectTemplatesAdapter.notifyItemInserted(sChorusItems.size() - 1);
                            mScrollChorusCustomize.setVisibility(View.INVISIBLE);
                            for (int i = 0; i < sChorusItems.size() - 1; i++)
                                sChorusItems.get(i).setSelected(false);
                            sChorusItems.get(sChorusItems.size() - 1).setSelected(true);
                            sChorusSelected = sChorusItems.size() - 1;
                        } else if (sEffectDetail == EFFECTTYPE_DISTORTION) {
                            arPresets.add(Float.parseFloat((String) mTextDistortionDrive.getText()));
                            arPresets.add(Float.parseFloat((String) mTextDistortionDry.getText()));
                            arPresets.add(Float.parseFloat((String) mTextDistortionWet.getText()));
                            arPresets.add(Float.parseFloat((String) mTextDistortionFeedback.getText()));
                            arPresets.add(Float.parseFloat((String) mTextDistortionVolume.getText()));
                            sDistortionItems.add(new EffectTemplateItem(editPreset.getText().toString(), arPresets));
                            mEffectTemplatesAdapter.notifyItemInserted(sDistortionItems.size() - 1);
                            mScrollDistortionCustomize.setVisibility(View.INVISIBLE);
                            for (int i = 0; i < sDistortionItems.size() - 1; i++)
                                sDistortionItems.get(i).setSelected(false);
                            sDistortionItems.get(sDistortionItems.size() - 1).setSelected(true);
                            sDistortionSelected = sDistortionItems.size() - 1;
                        } else if (sEffectDetail == EFFECTTYPE_COMP) {
                            arPresets.add(Float.parseFloat((String) mTextCompGain.getText()));
                            arPresets.add(Float.parseFloat((String) mTextCompThreshold.getText()));
                            arPresets.add(Float.parseFloat((String) mTextCompRatio.getText()));
                            arPresets.add(Float.parseFloat((String) mTextCompAttack.getText()));
                            arPresets.add(Float.parseFloat((String) mTextCompRelease.getText()));
                            sCompItems.add(new EffectTemplateItem(editPreset.getText().toString(), arPresets));
                            mEffectTemplatesAdapter.notifyItemInserted(sCompItems.size() - 1);
                            mScrollCompCustomize.setVisibility(View.INVISIBLE);
                            for (int i = 0; i < sCompItems.size() - 1; i++)
                                sCompItems.get(i).setSelected(false);
                            sCompItems.get(sCompItems.size() - 1).setSelected(true);
                            sCompSelected = sCompItems.size() - 1;
                        } else if (sEffectDetail == EFFECTTYPE_PAN) {
                            arPresets.add((mSeekPanValue.getProgress() - 100) / 100f);
                            sPanItems.add(new EffectTemplateItem(editPreset.getText().toString(), arPresets));
                            mEffectTemplatesAdapter.notifyItemInserted(sPanItems.size() - 1);
                            mScrollPanCustomize.setVisibility(View.INVISIBLE);
                            for (int i = 0; i < sPanItems.size() - 1; i++)
                                sPanItems.get(i).setSelected(false);
                            sPanItems.get(sPanItems.size() - 1).setSelected(true);
                            sPanSelected = sPanItems.size() - 1;
                        } else if (sEffectDetail == EFFECTTYPE_FREQ) {
                            arPresets.add((mSeekFreqValue.getProgress() + 10) / 100f);
                            sFreqItems.add(new EffectTemplateItem(editPreset.getText().toString(), arPresets));
                            mEffectTemplatesAdapter.notifyItemInserted(sFreqItems.size() - 1);
                            mScrollFreqCustomize.setVisibility(View.INVISIBLE);
                            for (int i = 0; i < sFreqItems.size() - 1; i++)
                                sFreqItems.get(i).setSelected(false);
                            sFreqItems.get(sFreqItems.size() - 1).setSelected(true);
                            sFreqSelected = sFreqItems.size() - 1;
                        } else if (sEffectDetail == EFFECTTYPE_METRONOME) {
                            arPresets.add((float)(mSeekMetronomeValue.getProgress() + 10));
                            sMetronomeItems.add(new EffectTemplateItem(editPreset.getText().toString(), arPresets));
                            mEffectTemplatesAdapter.notifyItemInserted(sMetronomeItems.size() - 1);
                            mScrollMetronomeCustomize.setVisibility(View.INVISIBLE);
                            for (int i = 0; i < sMetronomeItems.size() - 1; i++)
                                sMetronomeItems.get(i).setSelected(false);
                            sMetronomeItems.get(sMetronomeItems.size() - 1).setSelected(true);
                            sMetronomeSelected = sMetronomeItems.size() - 1;
                        }
                        saveData();

                        mBtnEffectFinish.setVisibility(View.GONE);
                        mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                        mTextEffectName.setText(sEffectItems.get(sEffectDetail).getEffectName());
                        mBtnEffectBack.setText(R.string.back);
                        mImgEffectBack.setVisibility(View.VISIBLE);
                        mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
                        if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                            mBtnAddEffectTemplate.setAlpha(1.0f);

                        mBtnEffectTemplateOff.setSelected(false);

                        sEffectItems.get(sEffectDetail).setSelected(true);
                        checkDuplicate(sEffectDetail);
                        mEffectsAdapter.notifyDataSetChanged();

                        mEffectTemplatesAdapter.notifyDataSetChanged();
                        if (sEffectDetail == EFFECTTYPE_REVERB)
                            mRecyclerEffectTemplates.scrollToPosition(sReverbItems.size() - 1);
                        else if (sEffectDetail == EFFECTTYPE_ECHO)
                            mRecyclerEffectTemplates.scrollToPosition(sEchoItems.size() - 1);
                        else if (sEffectDetail == EFFECTTYPE_CHORUS)
                            mRecyclerEffectTemplates.scrollToPosition(sChorusItems.size() - 1);
                        else if (sEffectDetail == EFFECTTYPE_DISTORTION)
                            mRecyclerEffectTemplates.scrollToPosition(sDistortionItems.size() - 1);
                        else if (sEffectDetail == EFFECTTYPE_COMP)
                            mRecyclerEffectTemplates.scrollToPosition(sCompItems.size() - 1);
                        else if (sEffectDetail == EFFECTTYPE_PAN)
                            mRecyclerEffectTemplates.scrollToPosition(sPanItems.size() - 1);
                        else if (sEffectDetail == EFFECTTYPE_FREQ)
                            mRecyclerEffectTemplates.scrollToPosition(sFreqItems.size() - 1);
                        else if (sEffectDetail == EFFECTTYPE_METRONOME)
                            mRecyclerEffectTemplates.scrollToPosition(sMetronomeItems.size() - 1);
                        else if (sEffectDetail == EFFECTTYPE_SOUNDEFFECT)
                            mRecyclerEffectTemplates.scrollToPosition(sSoundEffectItems.size() - 1);
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        if (alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        editPreset.requestFocus();
                        editPreset.setSelection(editPreset.getText().toString().length());
                        InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (null != imm) imm.showSoftInput(editPreset, 0);
                    }
                });
                alertDialog.show();
            } else {
                EffectTemplateItem item = null;
                int nItem = 0;
                if (sEffectDetail == EFFECTTYPE_REVERB) {
                    for (; nItem < sReverbItems.size(); nItem++) {
                        item = sReverbItems.get(nItem);
                        if (item.isSelected()) break;
                    }
                    if (item != null) {
                        ArrayList<Float> arPresets = item.getArPresets();
                        arPresets.set(0, Float.parseFloat((String) mTextReverbDry.getText()));
                        arPresets.set(1, Float.parseFloat((String) mTextReverbWet.getText()));
                        arPresets.set(2, Float.parseFloat((String) mTextReverbRoomSize.getText()));
                        arPresets.set(3, Float.parseFloat((String) mTextReverbDamp.getText()));
                        arPresets.set(4, Float.parseFloat((String) mTextReverbWidth.getText()));
                        saveData();
                    }
                    mScrollReverbCustomize.setVisibility(View.INVISIBLE);
                } else if (sEffectDetail == EFFECTTYPE_ECHO) {
                    for (; nItem < sEchoItems.size(); nItem++) {
                        item = sEchoItems.get(nItem);
                        if (item.isSelected()) break;
                    }
                    if (item != null) {
                        ArrayList<Float> arPresets = item.getArPresets();
                        arPresets.set(0, Float.parseFloat((String) mTextEchoDry.getText()));
                        arPresets.set(1, Float.parseFloat((String) mTextEchoWet.getText()));
                        arPresets.set(2, Float.parseFloat((String) mTextEchoFeedback.getText()));
                        arPresets.set(3, Float.parseFloat((String) mTextEchoDelay.getText()));
                        saveData();
                    }
                    mScrollEchoCustomize.setVisibility(View.INVISIBLE);
                } else if (sEffectDetail == EFFECTTYPE_CHORUS) {
                    for (; nItem < sChorusItems.size(); nItem++) {
                        item = sChorusItems.get(nItem);
                        if (item.isSelected()) break;
                    }
                    if (item != null) {
                        ArrayList<Float> arPresets = item.getArPresets();
                        arPresets.set(0, Float.parseFloat((String) mTextChorusDry.getText()));
                        arPresets.set(1, Float.parseFloat((String) mTextChorusWet.getText()));
                        arPresets.set(2, Float.parseFloat((String) mTextChorusFeedback.getText()));
                        arPresets.set(3, Float.parseFloat((String) mTextChorusMinSweep.getText()));
                        arPresets.set(4, Float.parseFloat((String) mTextChorusMaxSweep.getText()));
                        arPresets.set(5, Float.parseFloat((String) mTextChorusRate.getText()));
                        saveData();
                    }
                    mScrollChorusCustomize.setVisibility(View.INVISIBLE);
                } else if (sEffectDetail == EFFECTTYPE_DISTORTION) {
                    for (; nItem < sDistortionItems.size(); nItem++) {
                        item = sDistortionItems.get(nItem);
                        if (item.isSelected()) break;
                    }
                    if (item != null) {
                        ArrayList<Float> arPresets = item.getArPresets();
                        arPresets.set(0, Float.parseFloat((String) mTextDistortionDrive.getText()));
                        arPresets.set(1, Float.parseFloat((String) mTextDistortionDry.getText()));
                        arPresets.set(2, Float.parseFloat((String) mTextDistortionWet.getText()));
                        arPresets.set(3, Float.parseFloat((String) mTextDistortionFeedback.getText()));
                        arPresets.set(4, Float.parseFloat((String) mTextDistortionVolume.getText()));
                        saveData();
                    }
                    mScrollDistortionCustomize.setVisibility(View.INVISIBLE);
                } else if (sEffectDetail == EFFECTTYPE_COMP) {
                    for (; nItem < sCompItems.size(); nItem++) {
                        item = sCompItems.get(nItem);
                        if (item.isSelected()) break;
                    }
                    if (item != null) {
                        ArrayList<Float> arPresets = item.getArPresets();
                        arPresets.set(0, Float.parseFloat((String) mTextCompGain.getText()));
                        arPresets.set(1, Float.parseFloat((String) mTextCompThreshold.getText()));
                        arPresets.set(2, Float.parseFloat((String) mTextCompRatio.getText()));
                        arPresets.set(3, Float.parseFloat((String) mTextCompAttack.getText()));
                        arPresets.set(4, Float.parseFloat((String) mTextCompRelease.getText()));
                        saveData();
                    }
                    mScrollCompCustomize.setVisibility(View.INVISIBLE);
                } else if (sEffectDetail == EFFECTTYPE_PAN) {
                    for (; nItem < sPanItems.size(); nItem++) {
                        item = sPanItems.get(nItem);
                        if (item.isSelected()) break;
                    }
                    if (item != null) {
                        ArrayList<Float> arPresets = item.getArPresets();
                        arPresets.set(0, (mSeekPanValue.getProgress() - 100) / 100f);
                        saveData();
                    }
                    mScrollPanCustomize.setVisibility(View.INVISIBLE);
                } else if (sEffectDetail == EFFECTTYPE_FREQ) {
                    for (; nItem < sFreqItems.size(); nItem++) {
                        item = sFreqItems.get(nItem);
                        if (item.isSelected()) break;
                    }
                    if (item != null) {
                        ArrayList<Float> arPresets = item.getArPresets();
                        arPresets.set(0, (mSeekFreqValue.getProgress() + 10) / 100f);
                        saveData();
                    }
                    mScrollFreqCustomize.setVisibility(View.INVISIBLE);
                } else if (sEffectDetail == EFFECTTYPE_METRONOME) {
                    for (; nItem < sMetronomeItems.size(); nItem++) {
                        item = sMetronomeItems.get(nItem);
                        if (item.isSelected()) break;
                    }
                    if (item != null) {
                        ArrayList<Float> arPresets = item.getArPresets();
                        arPresets.set(0, (float)(mSeekMetronomeValue.getProgress() + 10));
                        saveData();
                    }
                    mScrollMetronomeCustomize.setVisibility(View.INVISIBLE);
                } else if (sEffectDetail == EFFECTTYPE_SOUNDEFFECT) {
                    for (; nItem < sSoundEffectItems.size(); nItem++) {
                        item = sSoundEffectItems.get(nItem);
                        if (item.isSelected()) break;
                    }
                    if (item != null) {
                        ArrayList<Float> arPresets = item.getArPresets();
                        arPresets.set(0, Float.parseFloat((String) mTextSoundEffectVolume.getText()));
                        saveData();
                    }
                    mScrollSoundEffectCustomize.setVisibility(View.INVISIBLE);
                }
                mBtnEffectFinish.setVisibility(View.GONE);
                mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                    mBtnAddEffectTemplate.setAlpha(1.0f);
            }
        } else if (v.getId() == R.id.btnEffectTemplateMenu) showTemplateMenu();
        else if (v.getId() == R.id.btnAddEffectTemplate) {
            mAddTemplate = true;

            sEffectItems.get(sEffectDetail).setSelected(true);
            checkDuplicate(sEffectDetail);
            if (mBtnEffectTemplateOff.isSelected()) {
                if (sEffectDetail == EFFECTTYPE_REVERB)
                    setReverb(70, 100, 85, 50, 90, true);
                else if (sEffectDetail == EFFECTTYPE_ECHO)
                    setEcho(100, 30, 60, 7, true);
                else if (sEffectDetail == EFFECTTYPE_CHORUS)
                    setChorus(100, 10, 50, 100, 200, 1000, true);
                else if (sEffectDetail == EFFECTTYPE_DISTORTION)
                    setDistortion(20, 95, 5, 10, 100, true);
                else if (sEffectDetail == EFFECTTYPE_COMP)
                    setComp(200, 4000, 900, 119, 39999, true);
                else if (sEffectDetail == EFFECTTYPE_PAN) setPan(100, true);
                else if (sEffectDetail == EFFECTTYPE_FREQ) setFreq(90, true);
                else if (sEffectDetail == EFFECTTYPE_METRONOME) setFreq(110, true);
            }

            if (sEffectDetail == EFFECTTYPE_REVERB) {
                mTextEffectName.setText(R.string.newReverb);
                mScrollReverbCustomize.setVisibility(View.VISIBLE);
                mBtnReverbSaveAs.setVisibility(View.GONE);
            } else if (sEffectDetail == EFFECTTYPE_ECHO) {
                mTextEffectName.setText(R.string.newEcho);
                mScrollEchoCustomize.setVisibility(View.VISIBLE);
                mBtnEchoSaveAs.setVisibility(View.GONE);
            } else if (sEffectDetail == EFFECTTYPE_CHORUS) {
                mTextEffectName.setText(R.string.newChorus);
                mScrollChorusCustomize.setVisibility(View.VISIBLE);
                mBtnChorusSaveAs.setVisibility(View.GONE);
            } else if (sEffectDetail == EFFECTTYPE_DISTORTION) {
                mTextEffectName.setText(R.string.newDistortion);
                mScrollDistortionCustomize.setVisibility(View.VISIBLE);
                mBtnDistortionSaveAs.setVisibility(View.GONE);
            } else if (sEffectDetail == EFFECTTYPE_COMP) {
                mTextEffectName.setText(R.string.newComp);
                mScrollCompCustomize.setVisibility(View.VISIBLE);
                mBtnCompSaveAs.setVisibility(View.GONE);
            } else if (sEffectDetail == EFFECTTYPE_PAN) {
                mTextEffectName.setText(R.string.newPan);
                mScrollPanCustomize.setVisibility(View.VISIBLE);
                mBtnPanSaveAs.setVisibility(View.GONE);
            } else if (sEffectDetail == EFFECTTYPE_FREQ) {
                mTextEffectName.setText(R.string.newFreq);
                mScrollFreqCustomize.setVisibility(View.VISIBLE);
                mBtnFreqSaveAs.setVisibility(View.GONE);
            } else if (sEffectDetail == EFFECTTYPE_METRONOME) {
                mTextEffectName.setText(R.string.newMetronome);
                mScrollMetronomeCustomize.setVisibility(View.VISIBLE);
                mBtnMetronomeSaveAs.setVisibility(View.GONE);
            }

            mBtnEffectBack.setText(R.string.cancel);
            mBtnEffectFinish.setText(R.string.save);

            mBtnEffectFinish.setVisibility(View.VISIBLE);
            mRelativeEffectTemplates.setVisibility(View.INVISIBLE);
            mImgEffectBack.setVisibility(View.INVISIBLE);
            mBtnEffectBack.setPadding((int) (16 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
            if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                mBtnAddEffectTemplate.setAlpha(0.0f);
        } else if (v.getId() == R.id.textFinishSortEffect) {
            mRecyclerEffectTemplates.setPadding(0, 0, 0, 0);
            mBtnEffectTemplateOff.setVisibility(View.VISIBLE);
            mRelativeEffectTitle.setVisibility(View.VISIBLE);
            mViewSepEffectDetail.setVisibility(View.VISIBLE);
            mViewSepEffectTemplateHeader.setVisibility(View.VISIBLE);
            if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                mBtnAddEffectTemplate.setAlpha(1.0f);
            mTextFinishSortEffect.setVisibility(View.INVISIBLE);
            mSorting = false;
            mEffectTemplatesAdapter.notifyDataSetChanged();
            mEffectTemplateTouchHelper.attachToRecyclerView(null);
        } else if (v.getId() == R.id.btnEffectTemplateOff) {
            mBtnEffectTemplateOff.setSelected(true);
            if (sEffectDetail == EFFECTTYPE_REVERB) {
                for (int i = 0; i < sReverbItems.size(); i++)
                    sReverbItems.get(i).setSelected(false);
                resetReverb();
            } else if (sEffectDetail == EFFECTTYPE_ECHO) {
                for (int i = 0; i < sEchoItems.size(); i++) sEchoItems.get(i).setSelected(false);
                resetEcho();
            } else if (sEffectDetail == EFFECTTYPE_CHORUS) {
                for (int i = 0; i < sChorusItems.size(); i++)
                    sChorusItems.get(i).setSelected(false);
                resetChorus();
            } else if (sEffectDetail == EFFECTTYPE_DISTORTION) {
                for (int i = 0; i < sDistortionItems.size(); i++)
                    sDistortionItems.get(i).setSelected(false);
                resetDistortion();
            } else if (sEffectDetail == EFFECTTYPE_COMP) {
                for (int i = 0; i < sCompItems.size(); i++) sCompItems.get(i).setSelected(false);
                resetComp();
            } else if (sEffectDetail == EFFECTTYPE_PAN) {
                for (int i = 0; i < sPanItems.size(); i++) sPanItems.get(i).setSelected(false);
                resetPan();
            } else if (sEffectDetail == EFFECTTYPE_FREQ) {
                for (int i = 0; i < sFreqItems.size(); i++) sFreqItems.get(i).setSelected(false);
                resetFreq();
            } else if (sEffectDetail == EFFECTTYPE_METRONOME) {
                for (int i = 0; i < sMetronomeItems.size(); i++) sMetronomeItems.get(i).setSelected(false);
                resetMetronome();
            } else if (sEffectDetail == EFFECTTYPE_SOUNDEFFECT) {
                for (int i = 0; i < sSoundEffectItems.size(); i++)
                    sSoundEffectItems.get(i).setSelected(false);
                resetSoundEffect();
            }
            mEffectTemplatesAdapter.notifyDataSetChanged();
        }
        else if (v.getId() == R.id.btnCompGainMinus) minusCompGain();
        else if (v.getId() == R.id.btnCompGainPlus) plusCompGain();
        else if (v.getId() == R.id.btnCompThresholdMinus) minusCompThreshold();
        else if (v.getId() == R.id.btnCompThresholdPlus) plusCompThreshold();
        else if (v.getId() == R.id.btnCompRatioMinus) minusCompRatio();
        else if (v.getId() == R.id.btnCompRatioPlus) plusCompRatio();
        else if (v.getId() == R.id.btnCompAttackMinus) minusCompAttack();
        else if (v.getId() == R.id.btnCompAttackPlus) plusCompAttack();
        else if (v.getId() == R.id.btnCompReleaseMinus) minusCompRelease();
        else if (v.getId() == R.id.btnCompReleasePlus) plusCompRelease();
        else if (v.getId() == R.id.btnCompRandom) setCompRandom();
        else if (v.getId() == R.id.btnResetComp) {
            if (sCompSelected != -1) {
                ArrayList<Float> arFloats = sCompItems.get(sCompSelected).getArPresets();
                setComp(arFloats.get(0), arFloats.get(1), arFloats.get(2), arFloats.get(3), arFloats.get(4), true);
            } else resetComp();
        } else if (v.getId() == R.id.btnCompSaveAs) {
            AlertDialog.Builder builder;
            if(sActivity.isDarkMode())
                builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
            else
                builder = new AlertDialog.Builder(sActivity);
            builder.setTitle(R.string.saveTemplate);
            LinearLayout linearLayout = new LinearLayout(sActivity);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            final ClearableEditText editPreset = new ClearableEditText(sActivity, sActivity.isDarkMode());
            editPreset.setHint(R.string.templateName);
            editPreset.setText(R.string.newComp);
            linearLayout.addView(editPreset);
            builder.setView(linearLayout);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ArrayList<Float> arPresets = new ArrayList<>();
                    arPresets.add(Float.parseFloat((String) mTextCompGain.getText()));
                    arPresets.add(Float.parseFloat((String) mTextCompThreshold.getText()));
                    arPresets.add(Float.parseFloat((String) mTextCompRatio.getText()));
                    arPresets.add(Float.parseFloat((String) mTextCompAttack.getText()));
                    arPresets.add(Float.parseFloat((String) mTextCompRelease.getText()));
                    sCompItems.add(new EffectTemplateItem(editPreset.getText().toString(), arPresets));
                    mEffectTemplatesAdapter.notifyItemInserted(sCompItems.size() - 1);
                    saveData();

                    mBtnEffectFinish.setVisibility(View.GONE);
                    mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                    mScrollCompCustomize.setVisibility(View.INVISIBLE);
                    mImgEffectBack.setVisibility(View.VISIBLE);
                    mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
                    if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                        mBtnAddEffectTemplate.setAlpha(1.0f);

                    for (int i = 0; i < sCompItems.size() - 1; i++)
                        sCompItems.get(i).setSelected(false);
                    sCompItems.get(sCompItems.size() - 1).setSelected(true);
                    sCompSelected = sCompItems.size() - 1;

                    sEffectItems.get(sEffectDetail).setSelected(true);
                    checkDuplicate(sEffectDetail);
                    mEffectsAdapter.notifyDataSetChanged();

                    mEffectTemplatesAdapter.notifyDataSetChanged();
                    mRecyclerEffectTemplates.scrollToPosition(sCompItems.size() - 1);
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    if (alertDialog.getWindow() != null) {
                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                        lp.dimAmount = 0.4f;
                        alertDialog.getWindow().setAttributes(lp);
                    }
                    editPreset.requestFocus();
                    editPreset.setSelection(editPreset.getText().toString().length());
                    InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (null != imm) imm.showSoftInput(editPreset, 0);
                }
            });
            alertDialog.show();
        }
        else if (v.getId() == R.id.btnPanValueMinus) minusPanValue();
        else if (v.getId() == R.id.btnPanValuePlus) plusPanValue();
        else if (v.getId() == R.id.btnPanRandom) setPanRandom();
        else if (v.getId() == R.id.btnResetPan) {
            if (sPanSelected != -1) {
                ArrayList<Float> arFloats = sPanItems.get(sPanSelected).getArPresets();
                setPan(arFloats.get(0), true);
            } else resetPan();
        } else if (v.getId() == R.id.btnPanSaveAs) {
            AlertDialog.Builder builder;
            if (sActivity.isDarkMode())
                builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
            else
                builder = new AlertDialog.Builder(sActivity);
            builder.setTitle(R.string.saveTemplate);
            LinearLayout linearLayout = new LinearLayout(sActivity);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            final ClearableEditText editPreset = new ClearableEditText(sActivity, sActivity.isDarkMode());
            editPreset.setHint(R.string.templateName);
            editPreset.setText(R.string.newPan);
            linearLayout.addView(editPreset);
            builder.setView(linearLayout);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ArrayList<Float> arPresets = new ArrayList<>();
                    arPresets.add((mSeekPanValue.getProgress() - 100) / 100f);
                    sPanItems.add(new EffectTemplateItem(editPreset.getText().toString(), arPresets));
                    mEffectTemplatesAdapter.notifyItemInserted(sPanItems.size() - 1);
                    saveData();

                    mBtnEffectFinish.setVisibility(View.GONE);
                    mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                    mScrollPanCustomize.setVisibility(View.INVISIBLE);
                    mImgEffectBack.setVisibility(View.VISIBLE);
                    mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
                    if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                        mBtnAddEffectTemplate.setAlpha(1.0f);

                    for (int i = 0; i < sPanItems.size() - 1; i++)
                        sPanItems.get(i).setSelected(false);
                    sPanItems.get(sPanItems.size() - 1).setSelected(true);
                    sPanSelected = sPanItems.size() - 1;

                    sEffectItems.get(sEffectDetail).setSelected(true);
                    checkDuplicate(sEffectDetail);
                    mEffectsAdapter.notifyDataSetChanged();

                    mEffectTemplatesAdapter.notifyDataSetChanged();
                    mRecyclerEffectTemplates.scrollToPosition(sPanItems.size() - 1);
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    if (alertDialog.getWindow() != null) {
                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                        lp.dimAmount = 0.4f;
                        alertDialog.getWindow().setAttributes(lp);
                    }
                    editPreset.requestFocus();
                    editPreset.setSelection(editPreset.getText().toString().length());
                    InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (null != imm) imm.showSoftInput(editPreset, 0);
                }
            });
            alertDialog.show();
        }
        else if (v.getId() == R.id.btnFreqValueMinus) minusFreqValue();
        else if (v.getId() == R.id.btnFreqValuePlus) plusFreqValue();
        else if (v.getId() == R.id.btnFreqRandom) setFreqRandom();
        else if (v.getId() == R.id.btnResetFreq) {
            if (sFreqSelected != -1) {
                ArrayList<Float> arFloats = sFreqItems.get(sFreqSelected).getArPresets();
                setFreq(arFloats.get(0), true);
            } else resetFreq();
        } else if (v.getId() == R.id.btnFreqSaveAs) {
            AlertDialog.Builder builder;
            if (sActivity.isDarkMode())
                builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
            else
                builder = new AlertDialog.Builder(sActivity);
            builder.setTitle(R.string.saveTemplate);
            LinearLayout linearLayout = new LinearLayout(sActivity);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            final ClearableEditText editPreset = new ClearableEditText(sActivity, sActivity.isDarkMode());
            editPreset.setHint(R.string.templateName);
            editPreset.setText(R.string.newFreq);
            linearLayout.addView(editPreset);
            builder.setView(linearLayout);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ArrayList<Float> arPresets = new ArrayList<>();
                    arPresets.add((mSeekFreqValue.getProgress() + 10) / 100f);
                    sFreqItems.add(new EffectTemplateItem(editPreset.getText().toString(), arPresets));
                    mEffectTemplatesAdapter.notifyItemInserted(sFreqItems.size() - 1);
                    saveData();

                    mBtnEffectFinish.setVisibility(View.GONE);
                    mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                    mScrollFreqCustomize.setVisibility(View.INVISIBLE);
                    mImgEffectBack.setVisibility(View.VISIBLE);
                    mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
                    if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                        mBtnAddEffectTemplate.setAlpha(1.0f);

                    for (int i = 0; i < sFreqItems.size() - 1; i++)
                        sFreqItems.get(i).setSelected(false);
                    sFreqItems.get(sFreqItems.size() - 1).setSelected(true);
                    sFreqSelected = sFreqItems.size() - 1;

                    sEffectItems.get(sEffectDetail).setSelected(true);
                    checkDuplicate(sEffectDetail);
                    mEffectsAdapter.notifyDataSetChanged();

                    mEffectTemplatesAdapter.notifyDataSetChanged();
                    mRecyclerEffectTemplates.scrollToPosition(sFreqItems.size() - 1);
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    if (alertDialog.getWindow() != null) {
                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                        lp.dimAmount = 0.4f;
                        alertDialog.getWindow().setAttributes(lp);
                    }
                    editPreset.requestFocus();
                    editPreset.setSelection(editPreset.getText().toString().length());
                    InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (null != imm) imm.showSoftInput(editPreset, 0);
                }
            });
            alertDialog.show();
        } else if (v.getId() == R.id.btnMetronomeValueMinus) minusMetronomeValue();
            else if (v.getId() == R.id.btnMetronomeValuePlus) plusMetronomeValue();
            else if (v.getId() == R.id.btnMetronomeRandom) setMetronomeRandom();
            else if (v.getId() == R.id.btnResetMetronome) {
                if (sMetronomeSelected != -1) {
                    ArrayList<Float> arFloats = sMetronomeItems.get(sMetronomeSelected).getArPresets();
                    setMetronome(arFloats.get(0), true);
                } else resetMetronome();
            } else if (v.getId() == R.id.btnMetronomeSaveAs) {
                AlertDialog.Builder builder;
                if (sActivity.isDarkMode())
                    builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
                else
                    builder = new AlertDialog.Builder(sActivity);
                builder.setTitle(R.string.saveTemplate);
                LinearLayout linearLayout = new LinearLayout(sActivity);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                final ClearableEditText editPreset = new ClearableEditText(sActivity, sActivity.isDarkMode());
                editPreset.setHint(R.string.templateName);
                editPreset.setText(R.string.newMetronome);
                linearLayout.addView(editPreset);
                builder.setView(linearLayout);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ArrayList<Float> arPresets = new ArrayList<>();
                        arPresets.add((float)(mSeekMetronomeValue.getProgress() + 10));
                        sMetronomeItems.add(new EffectTemplateItem(editPreset.getText().toString(), arPresets));
                        mEffectTemplatesAdapter.notifyItemInserted(sMetronomeItems.size() - 1);
                        saveData();

                        mBtnEffectFinish.setVisibility(View.GONE);
                        mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                        mScrollMetronomeCustomize.setVisibility(View.INVISIBLE);
                        mImgEffectBack.setVisibility(View.VISIBLE);
                        mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
                        if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                            mBtnAddEffectTemplate.setAlpha(1.0f);

                        for (int i = 0; i < sMetronomeItems.size() - 1; i++)
                            sMetronomeItems.get(i).setSelected(false);
                        sMetronomeItems.get(sMetronomeItems.size() - 1).setSelected(true);
                        sMetronomeSelected = sMetronomeItems.size() - 1;

                        sEffectItems.get(sEffectDetail).setSelected(true);
                        checkDuplicate(sEffectDetail);
                        mEffectsAdapter.notifyDataSetChanged();

                        mEffectTemplatesAdapter.notifyDataSetChanged();
                        mRecyclerEffectTemplates.scrollToPosition(sMetronomeItems.size() - 1);
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        if (alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        editPreset.requestFocus();
                        editPreset.setSelection(editPreset.getText().toString().length());
                        InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (null != imm) imm.showSoftInput(editPreset, 0);
                    }
                });
                alertDialog.show();
        } else if (v.getId() == R.id.btnEchoDryMinus) minusEchoDry();
        else if (v.getId() == R.id.btnEchoDryPlus) plusEchoDry();
        else if (v.getId() == R.id.btnEchoWetMinus) minusEchoWet();
        else if (v.getId() == R.id.btnEchoWetPlus) plusEchoWet();
        else if (v.getId() == R.id.btnEchoFeedbackMinus) minusEchoFeedback();
        else if (v.getId() == R.id.btnEchoFeedbackPlus) plusEchoFeedback();
        else if (v.getId() == R.id.btnEchoDelayMinus) minusEchoDelay();
        else if (v.getId() == R.id.btnEchoDelayPlus) plusEchoDelay();
        else if (v.getId() == R.id.btnEchoRandom) setEchoRandom();
        else if (v.getId() == R.id.btnResetEcho) {
            if (sEchoSelected != -1) {
                ArrayList<Float> arFloats = sEchoItems.get(sEchoSelected).getArPresets();
                setEcho(arFloats.get(0), arFloats.get(1), arFloats.get(2), arFloats.get(3), true);
            } else resetEcho();
        } else if (v.getId() == R.id.btnEchoSaveAs) {
            AlertDialog.Builder builder;
            if(sActivity.isDarkMode())
                builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
            else
                builder = new AlertDialog.Builder(sActivity);
            builder.setTitle(R.string.saveTemplate);
            LinearLayout linearLayout = new LinearLayout(sActivity);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            final ClearableEditText editPreset = new ClearableEditText(sActivity, sActivity.isDarkMode());
            editPreset.setHint(R.string.templateName);
            editPreset.setText(R.string.newEcho);
            linearLayout.addView(editPreset);
            builder.setView(linearLayout);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ArrayList<Float> arPresets = new ArrayList<>();
                    arPresets.add(Float.parseFloat((String) mTextEchoDry.getText()));
                    arPresets.add(Float.parseFloat((String) mTextEchoWet.getText()));
                    arPresets.add(Float.parseFloat((String) mTextEchoFeedback.getText()));
                    arPresets.add(Float.parseFloat((String) mTextEchoDelay.getText()));
                    sEchoItems.add(new EffectTemplateItem(editPreset.getText().toString(), arPresets));
                    mEffectTemplatesAdapter.notifyItemInserted(sEchoItems.size() - 1);
                    saveData();

                    mBtnEffectFinish.setVisibility(View.GONE);
                    mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                    mScrollEchoCustomize.setVisibility(View.INVISIBLE);
                    mImgEffectBack.setVisibility(View.VISIBLE);
                    mTextEffectName.setText(sEffectItems.get(sEffectDetail).getEffectName());
                    mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
                    if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                        mBtnAddEffectTemplate.setAlpha(1.0f);

                    for (int i = 0; i < sEchoItems.size() - 1; i++)
                        sEchoItems.get(i).setSelected(false);
                    sEchoItems.get(sEchoItems.size() - 1).setSelected(true);
                    sEchoSelected = sEchoItems.size() - 1;

                    sEffectItems.get(sEffectDetail).setSelected(true);
                    checkDuplicate(sEffectDetail);
                    mEffectsAdapter.notifyDataSetChanged();

                    mEffectTemplatesAdapter.notifyDataSetChanged();
                    mRecyclerEffectTemplates.scrollToPosition(sEchoItems.size() - 1);
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    if (alertDialog.getWindow() != null) {
                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                        lp.dimAmount = 0.4f;
                        alertDialog.getWindow().setAttributes(lp);
                    }
                    editPreset.requestFocus();
                    editPreset.setSelection(editPreset.getText().toString().length());
                    InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (null != imm) imm.showSoftInput(editPreset, 0);
                }
            });
            alertDialog.show();
        } else if (v.getId() == R.id.btnReverbDryMinus) minusReverbDry();
        else if (v.getId() == R.id.btnReverbDryPlus) plusReverbDry();
        else if (v.getId() == R.id.btnReverbWetMinus) minusReverbWet();
        else if (v.getId() == R.id.btnReverbWetPlus) plusReverbWet();
        else if (v.getId() == R.id.btnReverbRoomSizeMinus) minusReverbRoomSize();
        else if (v.getId() == R.id.btnReverbRoomSizePlus) plusReverbRoomSize();
        else if (v.getId() == R.id.btnReverbDampMinus) minusReverbDamp();
        else if (v.getId() == R.id.btnReverbDampPlus) plusReverbDamp();
        else if (v.getId() == R.id.btnReverbWidthMinus) minusReverbWidth();
        else if (v.getId() == R.id.btnReverbWidthPlus) plusReverbWidth();
        else if (v.getId() == R.id.btnReverbRandom) setReverbRandom();
        else if (v.getId() == R.id.btnResetReverb) {
            if (sReverbSelected != -1) {
                ArrayList<Float> arFloats = sReverbItems.get(sReverbSelected).getArPresets();
                setReverb(arFloats.get(0), arFloats.get(1), arFloats.get(2), arFloats.get(3), arFloats.get(4), true);
            } else resetReverb();
        } else if (v.getId() == R.id.btnReverbSaveAs) {
            AlertDialog.Builder builder;
            if(sActivity.isDarkMode())
                builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
            else
                builder = new AlertDialog.Builder(sActivity);
            builder.setTitle(R.string.saveTemplate);
            LinearLayout linearLayout = new LinearLayout(sActivity);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            final ClearableEditText editPreset = new ClearableEditText(sActivity, sActivity.isDarkMode());
            editPreset.setHint(R.string.templateName);
            editPreset.setText(R.string.newReverb);
            linearLayout.addView(editPreset);
            builder.setView(linearLayout);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ArrayList<Float> arPresets = new ArrayList<>();
                    arPresets.add(Float.parseFloat((String) mTextReverbDry.getText()));
                    arPresets.add(Float.parseFloat((String) mTextReverbWet.getText()));
                    arPresets.add(Float.parseFloat((String) mTextReverbRoomSize.getText()));
                    arPresets.add(Float.parseFloat((String) mTextReverbDamp.getText()));
                    arPresets.add(Float.parseFloat((String) mTextReverbWidth.getText()));
                    sReverbItems.add(new EffectTemplateItem(editPreset.getText().toString(), arPresets));
                    mEffectTemplatesAdapter.notifyItemInserted(sReverbItems.size() - 1);
                    saveData();

                    mBtnEffectFinish.setVisibility(View.GONE);
                    mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                    mScrollReverbCustomize.setVisibility(View.INVISIBLE);
                    mImgEffectBack.setVisibility(View.VISIBLE);
                    mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
                    if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                        mBtnAddEffectTemplate.setAlpha(1.0f);

                    for (int i = 0; i < sReverbItems.size() - 1; i++)
                        sReverbItems.get(i).setSelected(false);
                    sReverbItems.get(sReverbItems.size() - 1).setSelected(true);
                    sReverbSelected = sReverbItems.size() - 1;

                    sEffectItems.get(sEffectDetail).setSelected(true);
                    checkDuplicate(sEffectDetail);
                    mEffectsAdapter.notifyDataSetChanged();

                    mEffectTemplatesAdapter.notifyDataSetChanged();
                    mRecyclerEffectTemplates.scrollToPosition(sReverbItems.size() - 1);
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    if (alertDialog.getWindow() != null) {
                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                        lp.dimAmount = 0.4f;
                        alertDialog.getWindow().setAttributes(lp);
                    }
                    editPreset.requestFocus();
                    editPreset.setSelection(editPreset.getText().toString().length());
                    InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (null != imm) imm.showSoftInput(editPreset, 0);
                }
            });
            alertDialog.show();
        } else if (v.getId() == R.id.btnChorusDryMinus) minusChorusDry();
        else if (v.getId() == R.id.btnChorusDryPlus) plusChorusDry();
        else if (v.getId() == R.id.btnChorusWetMinus) minusChorusWet();
        else if (v.getId() == R.id.btnChorusWetPlus) plusChorusWet();
        else if (v.getId() == R.id.btnChorusFeedbackMinus) minusChorusFeedback();
        else if (v.getId() == R.id.btnChorusFeedbackPlus) plusChorusFeedback();
        else if (v.getId() == R.id.btnChorusMinSweepMinus) minusChorusMinSweep();
        else if (v.getId() == R.id.btnChorusMinSweepPlus) plusChorusMinSweep();
        else if (v.getId() == R.id.btnChorusMaxSweepMinus) minusChorusMaxSweep();
        else if (v.getId() == R.id.btnChorusMaxSweepPlus) plusChorusMaxSweep();
        else if (v.getId() == R.id.btnChorusRateMinus) minusChorusRate();
        else if (v.getId() == R.id.btnChorusRatePlus) plusChorusRate();
        else if (v.getId() == R.id.btnChorusRandom) setChorusRandom();
        else if (v.getId() == R.id.btnResetChorus) {
            if (sChorusSelected != -1) {
                ArrayList<Float> arFloats = sChorusItems.get(sChorusSelected).getArPresets();
                setChorus(arFloats.get(0), arFloats.get(1), arFloats.get(2), arFloats.get(3), arFloats.get(4), arFloats.get(5), true);
            } else resetChorus();
        } else if (v.getId() == R.id.btnChorusSaveAs) {
            AlertDialog.Builder builder;
            if(sActivity.isDarkMode())
                builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
            else
                builder = new AlertDialog.Builder(sActivity);
            builder.setTitle(R.string.saveTemplate);
            LinearLayout linearLayout = new LinearLayout(sActivity);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            final ClearableEditText editPreset = new ClearableEditText(sActivity, sActivity.isDarkMode());
            editPreset.setHint(R.string.templateName);
            editPreset.setText(R.string.newChorus);
            linearLayout.addView(editPreset);
            builder.setView(linearLayout);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ArrayList<Float> arPresets = new ArrayList<>();
                    arPresets.add(Float.parseFloat((String) mTextChorusDry.getText()));
                    arPresets.add(Float.parseFloat((String) mTextChorusWet.getText()));
                    arPresets.add(Float.parseFloat((String) mTextChorusFeedback.getText()));
                    arPresets.add(Float.parseFloat((String) mTextChorusMinSweep.getText()));
                    arPresets.add(Float.parseFloat((String) mTextChorusMaxSweep.getText()));
                    arPresets.add(Float.parseFloat((String) mTextChorusRate.getText()));
                    sChorusItems.add(new EffectTemplateItem(editPreset.getText().toString(), arPresets));
                    mEffectTemplatesAdapter.notifyItemInserted(sChorusItems.size() - 1);
                    saveData();

                    mBtnEffectFinish.setVisibility(View.GONE);
                    mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                    mScrollChorusCustomize.setVisibility(View.INVISIBLE);
                    mImgEffectBack.setVisibility(View.VISIBLE);
                    mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
                    if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                        mBtnAddEffectTemplate.setAlpha(1.0f);

                    for (int i = 0; i < sChorusItems.size() - 1; i++)
                        sChorusItems.get(i).setSelected(false);
                    sChorusItems.get(sChorusItems.size() - 1).setSelected(true);
                    sChorusSelected = sChorusItems.size() - 1;

                    sEffectItems.get(sEffectDetail).setSelected(true);
                    checkDuplicate(sEffectDetail);
                    mEffectsAdapter.notifyDataSetChanged();

                    mEffectTemplatesAdapter.notifyDataSetChanged();
                    mRecyclerEffectTemplates.scrollToPosition(sChorusItems.size() - 1);
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    if (alertDialog.getWindow() != null) {
                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                        lp.dimAmount = 0.4f;
                        alertDialog.getWindow().setAttributes(lp);
                    }
                    editPreset.requestFocus();
                    editPreset.setSelection(editPreset.getText().toString().length());
                    InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (null != imm) imm.showSoftInput(editPreset, 0);
                }
            });
            alertDialog.show();
        } else if (v.getId() == R.id.btnDistortionDriveMinus) minusDistortionDrive();
        else if (v.getId() == R.id.btnDistortionDrivePlus) plusDistortionDrive();
        else if (v.getId() == R.id.btnDistortionDryMinus) minusDistortionDry();
        else if (v.getId() == R.id.btnDistortionDryPlus) plusDistortionDry();
        else if (v.getId() == R.id.btnDistortionWetMinus) minusDistortionWet();
        else if (v.getId() == R.id.btnDistortionWetPlus) plusDistortionWet();
        else if (v.getId() == R.id.btnDistortionFeedbackMinus) minusDistortionFeedback();
        else if (v.getId() == R.id.btnDistortionFeedbackPlus) plusDistortionFeedback();
        else if (v.getId() == R.id.btnDistortionVolumeMinus) minusDistortionVolume();
        else if (v.getId() == R.id.btnDistortionVolumePlus) plusDistortionVolume();
        else if (v.getId() == R.id.btnDistortionRandom) setDistortionRandom();
        else if (v.getId() == R.id.btnResetDistortion) {
            if (sDistortionSelected != -1) {
                ArrayList<Float> arFloats = sDistortionItems.get(sDistortionSelected).getArPresets();
                setDistortion(arFloats.get(0), arFloats.get(1), arFloats.get(2), arFloats.get(3), arFloats.get(4), true);
            } else resetDistortion();
        } else if (v.getId() == R.id.btnDistortionSaveAs) {
            AlertDialog.Builder builder;
            if(sActivity.isDarkMode())
                builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
            else
                builder = new AlertDialog.Builder(sActivity);
            builder.setTitle(R.string.saveTemplate);
            LinearLayout linearLayout = new LinearLayout(sActivity);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            final ClearableEditText editPreset = new ClearableEditText(sActivity, sActivity.isDarkMode());
            editPreset.setHint(R.string.templateName);
            editPreset.setText(R.string.newDistortion);
            linearLayout.addView(editPreset);
            builder.setView(linearLayout);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ArrayList<Float> arPresets = new ArrayList<>();
                    arPresets.add(Float.parseFloat((String) mTextDistortionDrive.getText()));
                    arPresets.add(Float.parseFloat((String) mTextDistortionDry.getText()));
                    arPresets.add(Float.parseFloat((String) mTextDistortionWet.getText()));
                    arPresets.add(Float.parseFloat((String) mTextDistortionFeedback.getText()));
                    arPresets.add(Float.parseFloat((String) mTextDistortionVolume.getText()));
                    sDistortionItems.add(new EffectTemplateItem(editPreset.getText().toString(), arPresets));
                    mEffectTemplatesAdapter.notifyItemInserted(sDistortionItems.size() - 1);
                    saveData();

                    mBtnEffectFinish.setVisibility(View.GONE);
                    mRelativeEffectTemplates.setVisibility(View.VISIBLE);
                    mScrollDistortionCustomize.setVisibility(View.INVISIBLE);
                    mImgEffectBack.setVisibility(View.VISIBLE);
                    mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
                    if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                        mBtnAddEffectTemplate.setAlpha(1.0f);

                    for (int i = 0; i < sDistortionItems.size() - 1; i++)
                        sDistortionItems.get(i).setSelected(false);
                    sDistortionItems.get(sDistortionItems.size() - 1).setSelected(true);
                    sDistortionSelected = sDistortionItems.size() - 1;

                    sEffectItems.get(sEffectDetail).setSelected(true);
                    checkDuplicate(sEffectDetail);
                    mEffectsAdapter.notifyDataSetChanged();

                    mEffectTemplatesAdapter.notifyDataSetChanged();
                    mRecyclerEffectTemplates.scrollToPosition(sDistortionItems.size() - 1);
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    if (alertDialog.getWindow() != null) {
                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                        lp.dimAmount = 0.4f;
                        alertDialog.getWindow().setAttributes(lp);
                    }
                    editPreset.requestFocus();
                    editPreset.setSelection(editPreset.getText().toString().length());
                    InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (null != imm) imm.showSoftInput(editPreset, 0);
                }
            });
            alertDialog.show();
        } else if (v.getId() == R.id.btnSoundEffectVolumeMinus) minusSoundEffectVolume();
        else if (v.getId() == R.id.btnSoundEffectVolumePlus) plusSoundEffectVolume();
    }

    @Override
    public boolean onLongClick(View v) {
        mContinueFlag = true;
        if (v.getId() == R.id.btnCompGainMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusCompGain();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnCompGainPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusCompGain();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnCompThresholdMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusCompThreshold();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnCompThresholdPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusCompThreshold();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnCompRatioMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusCompRatio();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnCompRatioPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusCompRatio();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnCompAttackMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusCompAttack();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnCompAttackPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusCompAttack();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnCompReleaseMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusCompRelease();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnCompReleasePlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusCompRelease();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnPanValueMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusPanValue();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnPanValuePlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusPanValue();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnFreqValueMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusFreqValue();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnFreqValuePlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusFreqValue();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnMetronomeValueMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusMetronomeValue();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnMetronomeValuePlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusMetronomeValue();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnEchoDryMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusEchoDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnEchoDryPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusEchoDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnEchoWetMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusEchoWet();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnEchoWetPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusEchoWet();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnEchoDryMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusEchoDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnEchoDryPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusEchoDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnEchoDryMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusEchoDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnEchoDryPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusEchoDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnReverbDryMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusReverbDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnReverbDryPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusReverbDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnReverbWetMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusReverbWet();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnReverbWetPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusReverbWet();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnReverbRoomSizeMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusReverbRoomSize();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnReverbRoomSizePlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusReverbRoomSize();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnReverbDampMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusReverbDamp();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnReverbDampPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusReverbDamp();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnReverbWidthMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusReverbWidth();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnReverbWidthPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusReverbWidth();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnChorusDryMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusChorusDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnChorusDryPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusChorusDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnChorusWetMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusChorusWet();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnChorusWetPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusChorusWet();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnChorusFeedbackMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusChorusFeedback();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnChorusFeedbackPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusChorusFeedback();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnChorusMinSweepMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusChorusMinSweep();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnChorusMinSweepPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusChorusMinSweep();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnChorusMaxSweepMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusChorusMaxSweep();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnChorusMaxSweepPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusChorusMaxSweep();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnChorusRateMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusChorusRate();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnChorusRatePlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusChorusRate();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnDistortionDriveMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusDistortionDrive();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnDistortionDrivePlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusDistortionDrive();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnDistortionDryMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusDistortionDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnDistortionDryPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusDistortionDry();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnDistortionWetMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusDistortionWet();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnDistortionWetPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusDistortionWet();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnDistortionFeedbackMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusDistortionFeedback();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnDistortionFeedbackPlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusDistortionFeedback();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnDistortionVolumeMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusDistortionVolume();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnDistortionVolumePlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusDistortionVolume();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnSoundEffectVolumeMinus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    minusSoundEffectVolume();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        } else if (v.getId() == R.id.btnSoundEffectVolumePlus) {
            mHandlerLongClick.post(new Runnable() {
                @Override
                public void run() {
                    if (!mContinueFlag) return;
                    plusSoundEffectVolume();
                    mHandlerLongClick.postDelayed(this, 100);
                }
            });
            return true;
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP)
            mContinueFlag = false;
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEffectsAdapter = new EffectsAdapter(sActivity, sEffectItems);
        mEffectTemplatesAdapter = new EffectTemplatesAdapter(sActivity, sReverbItems);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(sMetronome == null) sMetronome = new Metronome(sActivity);

        mTextEffectName = sActivity.findViewById(R.id.textEffectName);
        mEditSpeedSpecifiedEffectDetail = sActivity.findViewById(R.id.editSpeedSpecifiedEffectDetail);
        mEditTimeSpecifiedEffectDetail = sActivity.findViewById(R.id.editTimeSpecifiedEffectDetail);
        mEditPitchSpecifiedEffectDetail = sActivity.findViewById(R.id.editSpeedSpecifiedEffectDetail);
        mEditPTimeSpecifiedEffectDetail = sActivity.findViewById(R.id.editTimeSpecifiedEffectDetail);
        mEditSpeedLoopEffectDetail = sActivity.findViewById(R.id.editSpeedLoopEffectDetail);
        mEditPitchLoopEffectDetail = sActivity.findViewById(R.id.editSpeedLoopEffectDetail);
        mRelativeEffectDetail = sActivity.findViewById(R.id.relativeEffectDetail);
        mRelativeEffectTitle = sActivity.findViewById(R.id.relativeEffectTitle);
        mRelativeEffectTemplates = sActivity.findViewById(R.id.relativeEffectTemplates);
        mRelativeSpecifiedEffectDetail = sActivity.findViewById(R.id.relativeSpecifiedEffectDetail);
        mRelativeLoopEffectDetail = sActivity.findViewById(R.id.relativeLoopEffectDetail);
        mBtnEffectOff = sActivity.findViewById(R.id.btnEffectOff);
        mBtnEffectBack = sActivity.findViewById(R.id.btnEffectBack);
        mBtnEffectFinish = sActivity.findViewById(R.id.btnEffectFinish);
        mBtnEffectTemplateOff = sActivity.findViewById(R.id.btnEffectTemplateOff);
        mBtnEffectTemplateMenu = sActivity.findViewById(R.id.btnEffectTemplateMenu);
        mBtnAddEffectTemplate = sActivity.findViewById(R.id.btnAddEffectTemplate);
        mViewSepEffectHeader = sActivity.findViewById(R.id.viewSepEffectHeader);
        mViewSepEffectDetail = sActivity.findViewById(R.id.viewSepEffectDetail);
        mViewSepEffectTemplateHeader = sActivity.findViewById(R.id.viewSepEffectTemplateHeader);
        mTextFinishSortEffect = sActivity.findViewById(R.id.textFinishSortEffect);
        mImgEffectBack = sActivity.findViewById(R.id.imgEffectBack);
        mBtnReverbSaveAs = sActivity.findViewById(R.id.btnReverbSaveAs);
        mBtnEchoSaveAs = sActivity.findViewById(R.id.btnEchoSaveAs);
        mBtnChorusSaveAs = sActivity.findViewById(R.id.btnChorusSaveAs);
        mBtnDistortionSaveAs = sActivity.findViewById(R.id.btnDistortionSaveAs);
        mBtnCompSaveAs = sActivity.findViewById(R.id.btnCompSaveAs);
        mBtnPanSaveAs = sActivity.findViewById(R.id.btnPanSaveAs);
        mBtnFreqSaveAs = sActivity.findViewById(R.id.btnFreqSaveAs);
        mBtnMetronomeSaveAs = sActivity.findViewById(R.id.btnMetronomeSaveAs);

        mRadioGroupEffectDetail = sActivity.findViewById(R.id.radioGroupEffectDetail);
        mRadioButtonSpecifiedEffectDetail = sActivity.findViewById(R.id.radioButtonSpecifiedEffectDetail);
        mRadioButtonLoopEffectDetail = sActivity.findViewById(R.id.radioButtonLoopEffectDetail);

        mScrollCompCustomize = sActivity.findViewById(R.id.scrollCompCustomize);
        mRelativeComp = sActivity.findViewById(R.id.relativeComp);
        mSeekCompGain = sActivity.findViewById(R.id.seekCompGain);
        mSeekCompThreshold = sActivity.findViewById(R.id.seekCompThreshold);
        mSeekCompRatio = sActivity.findViewById(R.id.seekCompRatio);
        mSeekCompAttack = sActivity.findViewById(R.id.seekCompAttack);
        mSeekCompRelease = sActivity.findViewById(R.id.seekCompRelease);
        mTextCompGain = sActivity.findViewById(R.id.textCompGain);
        mTextCompThreshold = sActivity.findViewById(R.id.textCompThreshold);
        mTextCompRatio = sActivity.findViewById(R.id.textCompRatio);
        mTextCompAttack = sActivity.findViewById(R.id.textCompAttack);
        mTextCompRelease = sActivity.findViewById(R.id.textCompRelease);
        mTextCompGainLabel = sActivity.findViewById(R.id.textCompGainLabel);
        mTextCompThresholdLabel = sActivity.findViewById(R.id.textCompThresholdLabel);
        mTextCompRatioLabel = sActivity.findViewById(R.id.textCompRatioLabel);
        mTextCompAttackLabel = sActivity.findViewById(R.id.textCompAttackLabel);
        mTextCompReleaseLabel = sActivity.findViewById(R.id.textCompReleaseLabel);
        mBtnCompGainMinus = sActivity.findViewById(R.id.btnCompGainMinus);
        mBtnCompGainPlus = sActivity.findViewById(R.id.btnCompGainPlus);
        mBtnCompThresholdMinus = sActivity.findViewById(R.id.btnCompThresholdMinus);
        mBtnCompThresholdPlus = sActivity.findViewById(R.id.btnCompThresholdPlus);
        mBtnCompRatioMinus = sActivity.findViewById(R.id.btnCompRatioMinus);
        mBtnCompRatioPlus = sActivity.findViewById(R.id.btnCompRatioPlus);
        mBtnCompAttackMinus = sActivity.findViewById(R.id.btnCompAttackMinus);
        mBtnCompAttackPlus = sActivity.findViewById(R.id.btnCompAttackPlus);
        mBtnCompReleaseMinus = sActivity.findViewById(R.id.btnCompReleaseMinus);
        mBtnCompReleasePlus = sActivity.findViewById(R.id.btnCompReleasePlus);
        mScrollPanCustomize = sActivity.findViewById(R.id.scrollPanCustomize);
        mRelativePan = sActivity.findViewById(R.id.relativePan);
        mSeekPanValue = sActivity.findViewById(R.id.seekPanValue);
        mTextPanValue = sActivity.findViewById(R.id.textPanValue);
        mTextPanValueLabel = sActivity.findViewById(R.id.textPanValueLabel);
        mBtnPanValueMinus = sActivity.findViewById(R.id.btnPanValueMinus);
        mBtnPanValuePlus = sActivity.findViewById(R.id.btnPanValuePlus);
        mScrollFreqCustomize = sActivity.findViewById(R.id.scrollFreqCustomize);
        mRelativeFreq = sActivity.findViewById(R.id.relativeFreq);
        mSeekFreqValue = sActivity.findViewById(R.id.seekFreqValue);
        mTextFreqValue = sActivity.findViewById(R.id.textFreqValue);
        mTextFreqValueLabel = sActivity.findViewById(R.id.textFreqValueLabel);
        mBtnFreqValueMinus = sActivity.findViewById(R.id.btnFreqValueMinus);
        mBtnFreqValuePlus = sActivity.findViewById(R.id.btnFreqValuePlus);
        mScrollMetronomeCustomize = sActivity.findViewById(R.id.scrollMetronomeCustomize);
        mRelativeMetronome = sActivity.findViewById(R.id.relativeMetronome);
        mSeekMetronomeValue = sActivity.findViewById(R.id.seekMetronomeValue);
        mTextMetronomeValue = sActivity.findViewById(R.id.textMetronomeValue);
        mTextMetronomeValueLabel = sActivity.findViewById(R.id.textMetronomeValueLabel);
        mBtnMetronomeValueMinus = sActivity.findViewById(R.id.btnMetronomeValueMinus);
        mBtnMetronomeValuePlus = sActivity.findViewById(R.id.btnMetronomeValuePlus);
        mScrollEchoCustomize = sActivity.findViewById(R.id.scrollEchoCustomize);
        mRelativeEcho = sActivity.findViewById(R.id.relativeEcho);
        mSeekEchoDry = sActivity.findViewById(R.id.seekEchoDry);
        mSeekEchoWet = sActivity.findViewById(R.id.seekEchoWet);
        mSeekEchoFeedback = sActivity.findViewById(R.id.seekEchoFeedback);
        mSeekEchoDelay = sActivity.findViewById(R.id.seekEchoDelay);
        mTextEchoDry = sActivity.findViewById(R.id.textEchoDry);
        mTextEchoWet = sActivity.findViewById(R.id.textEchoWet);
        mTextEchoFeedback = sActivity.findViewById(R.id.textEchoFeedback);
        mTextEchoDelay = sActivity.findViewById(R.id.textEchoDelay);
        mTextEchoDryLabel = sActivity.findViewById(R.id.textEchoDryLabel);
        mTextEchoWetLabel = sActivity.findViewById(R.id.textEchoWetLabel);
        mTextEchoFeedbackLabel = sActivity.findViewById(R.id.textEchoFeedbackLabel);
        mTextEchoDelayLabel = sActivity.findViewById(R.id.textEchoDelayLabel);
        mBtnEchoDryMinus = sActivity.findViewById(R.id.btnEchoDryMinus);
        mBtnEchoDryPlus = sActivity.findViewById(R.id.btnEchoDryPlus);
        mBtnEchoWetMinus = sActivity.findViewById(R.id.btnEchoWetMinus);
        mBtnEchoWetPlus = sActivity.findViewById(R.id.btnEchoWetPlus);
        mBtnEchoFeedbackMinus = sActivity.findViewById(R.id.btnEchoFeedbackMinus);
        mBtnEchoFeedbackPlus = sActivity.findViewById(R.id.btnEchoFeedbackPlus);
        mBtnEchoDelayMinus = sActivity.findViewById(R.id.btnEchoDelayMinus);
        mBtnEchoDelayPlus = sActivity.findViewById(R.id.btnEchoDelayPlus);
        mScrollReverbCustomize = sActivity.findViewById(R.id.scrollReverbCustomize);
        mRelativeReverb = sActivity.findViewById(R.id.relativeReverb);
        mSeekReverbDry = sActivity.findViewById(R.id.seekReverbDry);
        mSeekReverbWet = sActivity.findViewById(R.id.seekReverbWet);
        mSeekReverbRoomSize = sActivity.findViewById(R.id.seekReverbRoomSize);
        mSeekReverbDamp = sActivity.findViewById(R.id.seekReverbDamp);
        mSeekReverbWidth = sActivity.findViewById(R.id.seekReverbWidth);
        mTextReverbDry = sActivity.findViewById(R.id.textReverbDry);
        mTextReverbWet = sActivity.findViewById(R.id.textReverbWet);
        mTextReverbRoomSize = sActivity.findViewById(R.id.textReverbRoomSize);
        mTextReverbDamp = sActivity.findViewById(R.id.textReverbDamp);
        mTextReverbWidth = sActivity.findViewById(R.id.textReverbWidth);
        mTextReverbDryLabel = sActivity.findViewById(R.id.textReverbDryLabel);
        mTextReverbWetLabel = sActivity.findViewById(R.id.textReverbWetLabel);
        mTextReverbRoomSizeLabel = sActivity.findViewById(R.id.textReverbRoomSizeLabel);
        mTextReverbDampLabel = sActivity.findViewById(R.id.textReverbDampLabel);
        mTextReverbWidthLabel = sActivity.findViewById(R.id.textReverbWidthLabel);
        mBtnReverbDryMinus = sActivity.findViewById(R.id.btnReverbDryMinus);
        mBtnReverbDryPlus = sActivity.findViewById(R.id.btnReverbDryPlus);
        mBtnReverbWetMinus = sActivity.findViewById(R.id.btnReverbWetMinus);
        mBtnReverbWetPlus = sActivity.findViewById(R.id.btnReverbWetPlus);
        mBtnReverbRoomSizeMinus = sActivity.findViewById(R.id.btnReverbRoomSizeMinus);
        mBtnReverbRoomSizePlus = sActivity.findViewById(R.id.btnReverbRoomSizePlus);
        mBtnReverbDampMinus = sActivity.findViewById(R.id.btnReverbDampMinus);
        mBtnReverbDampPlus = sActivity.findViewById(R.id.btnReverbDampPlus);
        mBtnReverbWidthMinus = sActivity.findViewById(R.id.btnReverbWidthMinus);
        mBtnReverbWidthPlus = sActivity.findViewById(R.id.btnReverbWidthPlus);
        mScrollChorusCustomize = sActivity.findViewById(R.id.scrollChorusCustomize);
        mRelativeChorus = sActivity.findViewById(R.id.relativeChorus);
        mSeekChorusDry = sActivity.findViewById(R.id.seekChorusDry);
        mSeekChorusWet = sActivity.findViewById(R.id.seekChorusWet);
        mSeekChorusFeedback = sActivity.findViewById(R.id.seekChorusFeedback);
        mSeekChorusMinSweep = sActivity.findViewById(R.id.seekChorusMinSweep);
        mSeekChorusMaxSweep = sActivity.findViewById(R.id.seekChorusMaxSweep);
        mSeekChorusRate = sActivity.findViewById(R.id.seekChorusRate);
        mTextChorusDry = sActivity.findViewById(R.id.textChorusDry);
        mTextChorusWet = sActivity.findViewById(R.id.textChorusWet);
        mTextChorusFeedback = sActivity.findViewById(R.id.textChorusFeedback);
        mTextChorusMinSweep = sActivity.findViewById(R.id.textChorusMinSweep);
        mTextChorusMaxSweep = sActivity.findViewById(R.id.textChorusMaxSweep);
        mTextChorusRate = sActivity.findViewById(R.id.textChorusRate);
        mTextChorusDryLabel = sActivity.findViewById(R.id.textChorusDryLabel);
        mTextChorusWetLabel = sActivity.findViewById(R.id.textChorusWetLabel);
        mTextChorusFeedbackLabel = sActivity.findViewById(R.id.textChorusFeedbackLabel);
        mTextChorusMinSweepLabel = sActivity.findViewById(R.id.textChorusMinSweepLabel);
        mTextChorusMaxSweepLabel = sActivity.findViewById(R.id.textChorusMaxSweepLabel);
        mTextChorusRateLabel = sActivity.findViewById(R.id.textChorusRateLabel);
        mBtnChorusDryMinus = sActivity.findViewById(R.id.btnChorusDryMinus);
        mBtnChorusDryPlus = sActivity.findViewById(R.id.btnChorusDryPlus);
        mBtnChorusWetMinus = sActivity.findViewById(R.id.btnChorusWetMinus);
        mBtnChorusWetPlus = sActivity.findViewById(R.id.btnChorusWetPlus);
        mBtnChorusFeedbackMinus = sActivity.findViewById(R.id.btnChorusFeedbackMinus);
        mBtnChorusFeedbackPlus = sActivity.findViewById(R.id.btnChorusFeedbackPlus);
        mBtnChorusMinSweepMinus = sActivity.findViewById(R.id.btnChorusMinSweepMinus);
        mBtnChorusMinSweepPlus = sActivity.findViewById(R.id.btnChorusMinSweepPlus);
        mBtnChorusMaxSweepMinus = sActivity.findViewById(R.id.btnChorusMaxSweepMinus);
        mBtnChorusMaxSweepPlus = sActivity.findViewById(R.id.btnChorusMaxSweepPlus);
        mBtnChorusRateMinus = sActivity.findViewById(R.id.btnChorusRateMinus);
        mBtnChorusRatePlus = sActivity.findViewById(R.id.btnChorusRatePlus);

        mScrollDistortionCustomize = sActivity.findViewById(R.id.scrollDistortionCustomize);
        mRelativeDistortion = sActivity.findViewById(R.id.relativeDistortion);
        mSeekDistortionDrive = sActivity.findViewById(R.id.seekDistortionDrive);
        mSeekDistortionDry = sActivity.findViewById(R.id.seekDistortionDry);
        mSeekDistortionWet = sActivity.findViewById(R.id.seekDistortionWet);
        mSeekDistortionFeedback = sActivity.findViewById(R.id.seekDistortionFeedback);
        mSeekDistortionVolume = sActivity.findViewById(R.id.seekDistortionVolume);
        mTextDistortionDrive = sActivity.findViewById(R.id.textDistortionDrive);
        mTextDistortionDry = sActivity.findViewById(R.id.textDistortionDry);
        mTextDistortionWet = sActivity.findViewById(R.id.textDistortionWet);
        mTextDistortionFeedback = sActivity.findViewById(R.id.textDistortionFeedback);
        mTextDistortionVolume = sActivity.findViewById(R.id.textDistortionVolume);
        mTextDistortionDriveLabel = sActivity.findViewById(R.id.textDistortionDriveLabel);
        mTextDistortionDryLabel = sActivity.findViewById(R.id.textDistortionDryLabel);
        mTextDistortionWetLabel = sActivity.findViewById(R.id.textDistortionWetLabel);
        mTextDistortionFeedbackLabel = sActivity.findViewById(R.id.textDistortionFeedbackLabel);
        mTextDistortionVolumeLabel = sActivity.findViewById(R.id.textDistortionVolumeLabel);
        mBtnDistortionDriveMinus = sActivity.findViewById(R.id.btnDistortionDriveMinus);
        mBtnDistortionDrivePlus = sActivity.findViewById(R.id.btnDistortionDrivePlus);
        mBtnDistortionDryMinus = sActivity.findViewById(R.id.btnDistortionDryMinus);
        mBtnDistortionDryPlus = sActivity.findViewById(R.id.btnDistortionDryPlus);
        mBtnDistortionWetMinus = sActivity.findViewById(R.id.btnDistortionWetMinus);
        mBtnDistortionWetPlus = sActivity.findViewById(R.id.btnDistortionWetPlus);
        mBtnDistortionFeedbackMinus = sActivity.findViewById(R.id.btnDistortionFeedbackMinus);
        mBtnDistortionFeedbackPlus = sActivity.findViewById(R.id.btnDistortionFeedbackPlus);
        mBtnDistortionVolumeMinus = sActivity.findViewById(R.id.btnDistortionVolumeMinus);
        mBtnDistortionVolumePlus = sActivity.findViewById(R.id.btnDistortionVolumePlus);
        mScrollSoundEffectCustomize = sActivity.findViewById(R.id.scrollSoundEffectCustomize);
        mRelativeSoundEffect = sActivity.findViewById(R.id.relativeSoundEffect);
        mSeekSoundEffectVolume = sActivity.findViewById(R.id.seekSoundEffectVolume);
        mTextSoundEffectVolume = sActivity.findViewById(R.id.textSoundEffectVolume);
        mTextSoundEffectVolumeLabel = sActivity.findViewById(R.id.textSoundEffectVolumeLabel);
        mBtnSoundEffectVolumeMinus = sActivity.findViewById(R.id.btnSoundEffectVolumeMinus);
        mBtnSoundEffectVolumePlus = sActivity.findViewById(R.id.btnSoundEffectVolumePlus);
        mTextTimeSpecifiedEffectDetail = sActivity.findViewById(R.id.textTimeSpecifiedEffectDetail);
        mTextSpeedSpecifiedEffectDetail = sActivity.findViewById(R.id.textSpeedSpecifiedEffectDetail);
        mTextSpeedLoopEffectDetail = sActivity.findViewById(R.id.textSpeedLoopEffectDetail);

        mRecyclerEffects = sActivity.findViewById(R.id.recyclerEffects);
        mRecyclerEffectTemplates = sActivity.findViewById(R.id.recyclerEffectTemplates);
        mBtnCompRandom = sActivity.findViewById(R.id.btnCompRandom);
        mBtnResetComp = sActivity.findViewById(R.id.btnResetComp);
        mBtnPanRandom = sActivity.findViewById(R.id.btnPanRandom);
        mBtnResetPan = sActivity.findViewById(R.id.btnResetPan);
        mBtnFreqRandom = sActivity.findViewById(R.id.btnFreqRandom);
        mBtnResetFreq = sActivity.findViewById(R.id.btnResetFreq);
        mBtnMetronomeRandom = sActivity.findViewById(R.id.btnMetronomeRandom);
        mBtnResetMetronome = sActivity.findViewById(R.id.btnResetMetronome);
        mBtnEchoRandom = sActivity.findViewById(R.id.btnEchoRandom);
        mBtnResetEcho = sActivity.findViewById(R.id.btnResetEcho);
        mBtnReverbRandom = sActivity.findViewById(R.id.btnReverbRandom);
        mBtnResetReverb = sActivity.findViewById(R.id.btnResetReverb);
        mBtnChorusRandom = sActivity.findViewById(R.id.btnChorusRandom);
        mBtnResetChorus = sActivity.findViewById(R.id.btnResetChorus);
        mBtnDistortionRandom = sActivity.findViewById(R.id.btnDistortionRandom);
        mBtnResetDistortion = sActivity.findViewById(R.id.btnResetDistortion);

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
        mSeekPanValue.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekPanValue.setOnSeekBarChangeListener(this);
        mSeekFreqValue.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekFreqValue.setOnSeekBarChangeListener(this);
        mSeekMetronomeValue.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekMetronomeValue.setOnSeekBarChangeListener(this);
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
        mSeekSoundEffectVolume.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        mSeekSoundEffectVolume.setOnSeekBarChangeListener(this);

        mRadioGroupEffectDetail.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int nItem) {
                if(nItem == R.id.radioButtonSpecifiedEffectDetail) {
                    mEditTimeSpecifiedEffectDetail.setEnabled(true);
                    mEditSpeedSpecifiedEffectDetail.setEnabled(true);
                    mEditPTimeSpecifiedEffectDetail.setEnabled(true);
                    mEditPitchSpecifiedEffectDetail.setEnabled(true);
                    mEditTimeSpecifiedEffectDetail.setAlpha(1.0f);
                    mEditSpeedSpecifiedEffectDetail.setAlpha(1.0f);
                    mEditPTimeSpecifiedEffectDetail.setAlpha(1.0f);
                    mEditPitchSpecifiedEffectDetail.setAlpha(1.0f);
                    mTextTimeSpecifiedEffectDetail.setEnabled(true);
                    mTextSpeedSpecifiedEffectDetail.setEnabled(true);
                    mTextTimeSpecifiedEffectDetail.setAlpha(1.0f);
                    mTextSpeedSpecifiedEffectDetail.setAlpha(1.0f);
                    mEditSpeedLoopEffectDetail.setEnabled(false);
                    mEditPitchLoopEffectDetail.setEnabled(false);
                    mEditSpeedLoopEffectDetail.setAlpha(0.5f);
                    mEditPitchLoopEffectDetail.setAlpha(0.5f);
                    mTextSpeedLoopEffectDetail.setEnabled(false);
                    mTextSpeedLoopEffectDetail.setAlpha(0.5f);
                    sSpecifiedEffectDetail = true;
                    sLoopEffectDetail = false;
                }
                else {
                    mEditTimeSpecifiedEffectDetail.setEnabled(false);
                    mEditSpeedSpecifiedEffectDetail.setEnabled(false);
                    mEditPTimeSpecifiedEffectDetail.setEnabled(false);
                    mEditPitchSpecifiedEffectDetail.setEnabled(false);
                    mEditTimeSpecifiedEffectDetail.setAlpha(0.5f);
                    mEditSpeedSpecifiedEffectDetail.setAlpha(0.5f);
                    mEditPTimeSpecifiedEffectDetail.setAlpha(0.5f);
                    mEditPitchSpecifiedEffectDetail.setAlpha(0.5f);
                    mTextTimeSpecifiedEffectDetail.setEnabled(false);
                    mTextSpeedSpecifiedEffectDetail.setEnabled(false);
                    mTextTimeSpecifiedEffectDetail.setAlpha(0.5f);
                    mTextSpeedSpecifiedEffectDetail.setAlpha(0.5f);
                    mEditSpeedLoopEffectDetail.setEnabled(true);
                    mEditPitchLoopEffectDetail.setEnabled(true);
                    mEditSpeedLoopEffectDetail.setAlpha(1.0f);
                    mEditPitchLoopEffectDetail.setAlpha(1.0f);
                    mTextSpeedLoopEffectDetail.setEnabled(true);
                    mTextSpeedLoopEffectDetail.setAlpha(1.0f);
                    sSpecifiedEffectDetail = false;
                    sLoopEffectDetail = true;
                }
            }
        });

        if(sEffectItems.size() == 0) {
            EffectItem item = new EffectItem(getString(R.string.random), false);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.vocalCancel), false);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.monoral), false);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.leftOnly), false);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.rightOnly), false);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.leftAndRightReplace), false);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.doubling), false);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.transcribeSideGuitar), false);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.transcribeBassOctave), false);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.pan), true);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.normalize), false);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.comp), true);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.freq), true);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.phaseReversal), false);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.echo), true);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.reverb), true);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.chorusFlanger), true);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.distortion), true);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.reverse), false);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.increaseSpeed), true);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.decreaseSpeed), true);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.raisePitch), true);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.lowerPitch), true);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.oldRecord), false);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.lowBattery), false);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.noSenseStrong), false);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.noSenseMiddle), false);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.noSenseWeak), false);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.earTraining), false);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.metronome), true);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.soundEffect), true);
            sEffectItems.add(item);
            item = new EffectItem(getString(R.string.eightBitSound), false);
            sEffectItems.add(item);
        }

        new SwipeHelper(sActivity, mRecyclerEffectTemplates) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        getString(R.string.delete),
                        0,
                        Color.parseColor("#FE3B30"),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                mEffectTemplatesAdapter.notifyDataSetChanged();
                                askDeletePreset(pos);
                            }
                        }
                ));
            }
        };

        loadData();

        mRecyclerEffects.setHasFixedSize(false);
        LinearLayoutManager effectManager = new LinearLayoutManager(sActivity);
        mRecyclerEffects.setLayoutManager(effectManager);
        mRecyclerEffects.setAdapter(mEffectsAdapter);
        if(mRecyclerEffects.getItemAnimator() != null)
            ((DefaultItemAnimator) mRecyclerEffects.getItemAnimator()).setSupportsChangeAnimations(false);
        mRecyclerEffectTemplates.setHasFixedSize(false);
        LinearLayoutManager effectTemplateManager = new LinearLayoutManager(sActivity);
        mRecyclerEffectTemplates.setLayoutManager(effectTemplateManager);
        mRecyclerEffectTemplates.setAdapter(mEffectTemplatesAdapter);
        if(mRecyclerEffectTemplates.getItemAnimator() != null)
            ((DefaultItemAnimator) mRecyclerEffectTemplates.getItemAnimator()).setSupportsChangeAnimations(false);
        mBtnEffectTemplateMenu.setOnClickListener(this);
        mBtnAddEffectTemplate.setOnClickListener(this);
        mTextFinishSortEffect.setOnClickListener(this);
        mBtnEffectOff.setOnClickListener(this);
        mBtnEffectBack.setOnClickListener(this);
        mBtnEffectTemplateOff.setOnClickListener(this);
        mBtnEffectFinish.setOnClickListener(this);
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
        mBtnCompRandom.setOnClickListener(this);
        mBtnResetComp.setOnClickListener(this);
        mBtnPanValueMinus.setOnClickListener(this);
        mBtnPanValueMinus.setOnLongClickListener(this);
        mBtnPanValueMinus.setOnTouchListener(this);
        mBtnPanValuePlus.setOnClickListener(this);
        mBtnPanValuePlus.setOnLongClickListener(this);
        mBtnPanValuePlus.setOnTouchListener(this);
        mBtnPanRandom.setOnClickListener(this);
        mBtnResetPan.setOnClickListener(this);
        mBtnFreqValueMinus.setOnClickListener(this);
        mBtnFreqValueMinus.setOnLongClickListener(this);
        mBtnFreqValueMinus.setOnTouchListener(this);
        mBtnFreqValuePlus.setOnClickListener(this);
        mBtnFreqValuePlus.setOnLongClickListener(this);
        mBtnFreqValuePlus.setOnTouchListener(this);
        mBtnFreqRandom.setOnClickListener(this);
        mBtnResetFreq.setOnClickListener(this);
        mBtnMetronomeValueMinus.setOnClickListener(this);
        mBtnMetronomeValueMinus.setOnLongClickListener(this);
        mBtnMetronomeValueMinus.setOnTouchListener(this);
        mBtnMetronomeValuePlus.setOnClickListener(this);
        mBtnMetronomeValuePlus.setOnLongClickListener(this);
        mBtnMetronomeValuePlus.setOnTouchListener(this);
        mBtnMetronomeRandom.setOnClickListener(this);
        mBtnResetMetronome.setOnClickListener(this);
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
        mBtnEchoRandom.setOnClickListener(this);
        mBtnResetEcho.setOnClickListener(this);
        mBtnEchoSaveAs.setOnClickListener(this);
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
        mBtnReverbRandom.setOnClickListener(this);
        mBtnResetReverb.setOnClickListener(this);
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
        mBtnChorusRandom.setOnClickListener(this);
        mBtnResetChorus.setOnClickListener(this);
        mBtnChorusSaveAs.setOnClickListener(this);
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
        mBtnDistortionRandom.setOnClickListener(this);
        mBtnResetDistortion.setOnClickListener(this);
        mBtnDistortionSaveAs.setOnClickListener(this);
        mBtnCompSaveAs.setOnClickListener(this);
        mBtnPanSaveAs.setOnClickListener(this);
        mBtnFreqSaveAs.setOnClickListener(this);
        mBtnMetronomeSaveAs.setOnClickListener(this);
        mBtnSoundEffectVolumeMinus.setOnClickListener(this);
        mBtnSoundEffectVolumeMinus.setOnLongClickListener(this);
        mBtnSoundEffectVolumeMinus.setOnTouchListener(this);
        mBtnSoundEffectVolumePlus.setOnClickListener(this);
        mBtnSoundEffectVolumePlus.setOnLongClickListener(this);
        mBtnSoundEffectVolumePlus.setOnTouchListener(this);

        //mRadioButtonSpecifiedEffectDetail.setOnClickListener(this);
        //mRadioButtonLoopEffectDetail.setOnClickListener(this);
        mEditTimeSpecifiedEffectDetail.setOnFocusChangeListener(this);
        mEditSpeedSpecifiedEffectDetail.setOnFocusChangeListener(this);
        mEditPTimeSpecifiedEffectDetail.setOnFocusChangeListener(this);
        mEditPitchSpecifiedEffectDetail.setOnFocusChangeListener(this);
        mEditSpeedLoopEffectDetail.setOnFocusChangeListener(this);
        mEditPitchLoopEffectDetail.setOnFocusChangeListener(this);

        boolean selected = false;
        for(int i = 0; i < sEffectItems.size(); i++) {
            if(sEffectItems.get(i).isSelected()) selected = true;
        }
        mBtnEffectOff.setSelected(!selected);

        setComp(200, 4000, 900, 119, 39999, false);
        setPan(100, false);
        setFreq(90, false);
        setMetronome(110, false);
        setEcho(100, 30, 60, 7, false);
        setReverb(70, 100, 85, 50, 90, false);
        setChorus(100, 10, 50, 100, 200, 1000, false);
        setDistortion(20, 95, 5, 10, 100, false);
        setSoundEffect(100, false);

        if(sActivity.isDarkMode()) setDarkMode(false);
    }

    private void loadData() {
        if(MainActivity.sStream != 0) return;

        SharedPreferences preferences = sActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        ArrayList<EffectTemplateItem> reverbItems = gson.fromJson(preferences.getString("sReverbItems", ""), new TypeToken<ArrayList<EffectTemplateItem>>() {
        }.getType());
        ArrayList<EffectTemplateItem> echoItems = gson.fromJson(preferences.getString("sEchoItems", ""), new TypeToken<ArrayList<EffectTemplateItem>>() {
        }.getType());
        ArrayList<EffectTemplateItem> chorusItems = gson.fromJson(preferences.getString("sChorusItems", ""), new TypeToken<ArrayList<EffectTemplateItem>>() {
        }.getType());
        ArrayList<EffectTemplateItem> distortionItems = gson.fromJson(preferences.getString("sDistortionItems", ""), new TypeToken<ArrayList<EffectTemplateItem>>() {
        }.getType());
        ArrayList<EffectTemplateItem> compItems = gson.fromJson(preferences.getString("sCompItems", ""), new TypeToken<ArrayList<EffectTemplateItem>>() {
        }.getType());
        ArrayList<EffectTemplateItem> panItems = gson.fromJson(preferences.getString("sPanItems", ""), new TypeToken<ArrayList<EffectTemplateItem>>() {
        }.getType());
        ArrayList<EffectTemplateItem> freqItems = gson.fromJson(preferences.getString("sFreqItems", ""), new TypeToken<ArrayList<EffectTemplateItem>>() {
        }.getType());
        ArrayList<EffectTemplateItem> metronomeItems = gson.fromJson(preferences.getString("sMetronomeItems", ""), new TypeToken<ArrayList<EffectTemplateItem>>() {
        }.getType());
        ArrayList<EffectTemplateItem> soundEffectItems = gson.fromJson(preferences.getString("sSoundEffectItems", ""), new TypeToken<ArrayList<EffectTemplateItem>>() {
        }.getType());

        if (reverbItems != null) {
            setReverbItems(reverbItems);
            if (MainActivity.sPrevVersion != 0.0f && MainActivity.sPrevVersion < 2.23f) {
                boolean added = false;
                for (int i = reverbItems.size() - 1; i >= 0; i--) {
                    if (reverbItems.get(i).getEffectTemplateName().equals(getString(R.string.church))) {
                        added = true;
                        sReverbItems.add(i, new EffectTemplateItem(getString(R.string.concertHall), new ArrayList<>(Arrays.asList(0.55f, 1.0f, 0.87f, 0.5f, 0.95f))));
                        break;
                    }
                }
                if (!added)
                    sReverbItems.add(new EffectTemplateItem(getString(R.string.concertHall), new ArrayList<>(Arrays.asList(0.55f, 1.0f, 0.87f, 0.5f, 0.95f))));
                saveData();
                mEffectTemplatesAdapter.notifyDataSetChanged();
            }
        }
        else resetReverbs();
        if (echoItems != null) setEchoItems(echoItems);
        else resetEchos();
        if (chorusItems != null) setChorusItems(chorusItems);
        else resetChoruses();
        if (distortionItems != null) setDistortionItems(distortionItems);
        else resetDistortions();
        if (compItems != null) setCompItems(compItems);
        else resetComps();
        if (panItems != null) setPanItems(panItems);
        else resetPans();
        if (freqItems != null) setFreqItems(freqItems);
        else resetFreqs();
        if (metronomeItems != null) setMetronomeItems(metronomeItems);
        else resetMetronomes();
        if (soundEffectItems != null && soundEffectItems.size() != 0)
            setSoundEffectItems(soundEffectItems);
        else resetSoundEffects();

        mBtnEffectTemplateOff.setSelected(true);
        for (int i = 0; i < sReverbItems.size(); i++)
            sReverbItems.get(i).setSelected(false);
        for (int i = 0; i < sEchoItems.size(); i++)
            sEchoItems.get(i).setSelected(false);
        for (int i = 0; i < sChorusItems.size(); i++)
            sChorusItems.get(i).setSelected(false);
        for (int i = 0; i < sDistortionItems.size(); i++)
            sDistortionItems.get(i).setSelected(false);
        for (int i = 0; i < sCompItems.size(); i++)
            sCompItems.get(i).setSelected(false);
        for (int i = 0; i < sPanItems.size(); i++)
            sPanItems.get(i).setSelected(false);
        for (int i = 0; i < sFreqItems.size(); i++)
            sFreqItems.get(i).setSelected(false);
        for (int i = 0; i < sMetronomeItems.size(); i++)
            sMetronomeItems.get(i).setSelected(false);
        for (int i = 0; i < sSoundEffectItems.size(); i++)
            sSoundEffectItems.get(i).setSelected(false);
    }

    private void saveData() {
        SharedPreferences preferences = sActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        preferences.edit().putString("sReverbItems", gson.toJson(sReverbItems)).apply();
        preferences.edit().putString("sEchoItems", gson.toJson(sEchoItems)).apply();
        preferences.edit().putString("sChorusItems", gson.toJson(sChorusItems)).apply();
        preferences.edit().putString("sDistortionItems", gson.toJson(sDistortionItems)).apply();
        preferences.edit().putString("sCompItems", gson.toJson(sCompItems)).apply();
        preferences.edit().putString("sPanItems", gson.toJson(sPanItems)).apply();
        preferences.edit().putString("sFreqItems", gson.toJson(sFreqItems)).apply();
        preferences.edit().putString("sMetronomeItems", gson.toJson(sMetronomeItems)).apply();
        preferences.edit().putString("sSoundEffectItems", gson.toJson(sSoundEffectItems)).apply();
    }

    private void resetReverbs() {
        if (sReverbItems.size() > 0) sReverbItems.clear();

        sReverbItems.add(new EffectTemplateItem(getString(R.string.bathroom), new ArrayList<>(Arrays.asList(1.0f, 2.0f, 0.16f, 0.5f, 1.0f))));
        sReverbItems.add(new EffectTemplateItem(getString(R.string.smallRoom), new ArrayList<>(Arrays.asList(0.95f, 0.99f, 0.3f, 0.5f, 1.0f))));
        sReverbItems.add(new EffectTemplateItem(getString(R.string.mediumRoom), new ArrayList<>(Arrays.asList(0.95f, 0.99f, 0.75f, 0.5f, 0.7f))));
        sReverbItems.add(new EffectTemplateItem(getString(R.string.largeRoom), new ArrayList<>(Arrays.asList(0.7f, 1.0f, 0.85f, 0.5f, 0.9f))));
        sReverbItems.add(new EffectTemplateItem(getString(R.string.concertHall), new ArrayList<>(Arrays.asList(0.55f, 1.0f, 0.87f, 0.5f, 0.95f))));
        sReverbItems.add(new EffectTemplateItem(getString(R.string.church), new ArrayList<>(Arrays.asList(0.4f, 1.0f, 0.9f, 0.5f, 1.0f))));
        sReverbItems.add(new EffectTemplateItem(getString(R.string.cathedral), new ArrayList<>(Arrays.asList(0.0f, 1.0f, 0.9f, 0.5f, 1.0f))));

        saveData();
        mEffectTemplatesAdapter.notifyDataSetChanged();
        resetReverb();
    }

    private void resetEchos() {
        if (sEchoItems.size() > 0) sEchoItems.clear();

        sEchoItems.add(new EffectTemplateItem(getString(R.string.studium), new ArrayList<>(Arrays.asList(0.95f, 0.1f, 0.55f, 0.4f))));
        sEchoItems.add(new EffectTemplateItem(getString(R.string.hall), new ArrayList<>(Arrays.asList(0.95f, 0.1f, 0.5f, 0.3f))));
        sEchoItems.add(new EffectTemplateItem(getString(R.string.livehouse), new ArrayList<>(Arrays.asList(1.0f, 0.125f, 0.3f, 0.2f))));
        sEchoItems.add(new EffectTemplateItem(getString(R.string.room), new ArrayList<>(Arrays.asList(1.0f, 0.15f, 0.5f, 0.1f))));
        sEchoItems.add(new EffectTemplateItem(getString(R.string.bathroom), new ArrayList<>(Arrays.asList(1.0f, 0.3f, 0.6f, 0.08f))));
        sEchoItems.add(new EffectTemplateItem(getString(R.string.vocal), new ArrayList<>(Arrays.asList(1.0f, 0.15f, 0.4f, 0.35f))));
        sEchoItems.add(new EffectTemplateItem(getString(R.string.mountain), new ArrayList<>(Arrays.asList(1.0f, 0.2f, 0.0f, 1.0f))));

        saveData();
        mEffectTemplatesAdapter.notifyDataSetChanged();
        resetEcho();
    }

    private void resetChoruses() {
        if (sChorusItems.size() > 0) sChorusItems.clear();

        sChorusItems.add(new EffectTemplateItem(getString(R.string.chorus), new ArrayList<>(Arrays.asList(0.5f, 0.2f, 0.5f, 1.0f, 2.0f, 10.0f))));
        sChorusItems.add(new EffectTemplateItem(getString(R.string.flanger), new ArrayList<>(Arrays.asList(0.25f, 0.4f, 0.5f, 1.0f, 5.0f, 1.0f))));

        saveData();
        mEffectTemplatesAdapter.notifyDataSetChanged();
        resetChorus();
    }

    private void resetDistortions() {
        if (sDistortionItems.size() > 0) sDistortionItems.clear();

        sDistortionItems.add(new EffectTemplateItem(getString(R.string.strong), new ArrayList<>(Arrays.asList(0.2f, 0.96f, 0.03f, 0.1f, 1.0f))));
        sDistortionItems.add(new EffectTemplateItem(getString(R.string.middle), new ArrayList<>(Arrays.asList(0.2f, 0.97f, 0.02f, 0.1f, 1.0f))));
        sDistortionItems.add(new EffectTemplateItem(getString(R.string.weak), new ArrayList<>(Arrays.asList(0.2f, 0.98f, 0.01f, 0.1f, 1.0f))));

        saveData();
        mEffectTemplatesAdapter.notifyDataSetChanged();
        resetDistortion();
    }

    private void resetComps() {
        if (sCompItems.size() > 0) sCompItems.clear();

        sCompItems.add(new EffectTemplateItem(getString(R.string.comp), new ArrayList<>(Arrays.asList(2.0f, -20.0f, 10.0f, 1.2f, 400.0f))));

        saveData();
        mEffectTemplatesAdapter.notifyDataSetChanged();
        resetComp();
    }

    private void resetPans() {
        if (sPanItems.size() > 0) sPanItems.clear();

        sPanItems.add(new EffectTemplateItem(getString(R.string.panL100), new ArrayList<>(Arrays.asList(-1f))));
        sPanItems.add(new EffectTemplateItem(getString(R.string.panR100), new ArrayList<>(Arrays.asList(1f))));
        sPanItems.add(new EffectTemplateItem(getString(R.string.panL75), new ArrayList<>(Arrays.asList(-0.75f))));
        sPanItems.add(new EffectTemplateItem(getString(R.string.panR75), new ArrayList<>(Arrays.asList(0.75f))));
        sPanItems.add(new EffectTemplateItem(getString(R.string.panL50), new ArrayList<>(Arrays.asList(-0.5f))));
        sPanItems.add(new EffectTemplateItem(getString(R.string.panR50), new ArrayList<>(Arrays.asList(0.5f))));
        sPanItems.add(new EffectTemplateItem(getString(R.string.panL25), new ArrayList<>(Arrays.asList(-0.25f))));
        sPanItems.add(new EffectTemplateItem(getString(R.string.panR25), new ArrayList<>(Arrays.asList(0.25f))));

        saveData();
        mEffectTemplatesAdapter.notifyDataSetChanged();
        resetPan();
    }

    private void resetFreqs() {
        if (sFreqItems.size() > 0) sFreqItems.clear();

        sFreqItems.add(new EffectTemplateItem(getString(R.string.freq50), new ArrayList<>(Arrays.asList(0.5f))));
        sFreqItems.add(new EffectTemplateItem(getString(R.string.freq60), new ArrayList<>(Arrays.asList(0.6f))));
        sFreqItems.add(new EffectTemplateItem(getString(R.string.freq70), new ArrayList<>(Arrays.asList(0.7f))));
        sFreqItems.add(new EffectTemplateItem(getString(R.string.freq80), new ArrayList<>(Arrays.asList(0.8f))));
        sFreqItems.add(new EffectTemplateItem(getString(R.string.freq90), new ArrayList<>(Arrays.asList(0.9f))));
        sFreqItems.add(new EffectTemplateItem(getString(R.string.freq125), new ArrayList<>(Arrays.asList(1.25f))));
        sFreqItems.add(new EffectTemplateItem(getString(R.string.freq150), new ArrayList<>(Arrays.asList(1.5f))));
        sFreqItems.add(new EffectTemplateItem(getString(R.string.freq175), new ArrayList<>(Arrays.asList(1.75f))));
        sFreqItems.add(new EffectTemplateItem(getString(R.string.freq200), new ArrayList<>(Arrays.asList(2f))));

        saveData();
        mEffectTemplatesAdapter.notifyDataSetChanged();
        resetFreq();
    }

    private void resetMetronomes() {
        if (sMetronomeItems.size() > 0) sMetronomeItems.clear();

        sMetronomeItems.add(new EffectTemplateItem(getString(R.string.bpm60), new ArrayList<>(Arrays.asList(60f))));
        sMetronomeItems.add(new EffectTemplateItem(getString(R.string.bpm70), new ArrayList<>(Arrays.asList(70f))));
        sMetronomeItems.add(new EffectTemplateItem(getString(R.string.bpm80), new ArrayList<>(Arrays.asList(80f))));
        sMetronomeItems.add(new EffectTemplateItem(getString(R.string.bpm90), new ArrayList<>(Arrays.asList(90f))));
        sMetronomeItems.add(new EffectTemplateItem(getString(R.string.bpm100), new ArrayList<>(Arrays.asList(100f))));
        sMetronomeItems.add(new EffectTemplateItem(getString(R.string.bpm110), new ArrayList<>(Arrays.asList(110f))));
        sMetronomeItems.add(new EffectTemplateItem(getString(R.string.bpm120), new ArrayList<>(Arrays.asList(120f))));
        sMetronomeItems.add(new EffectTemplateItem(getString(R.string.bpm130), new ArrayList<>(Arrays.asList(130f))));
        sMetronomeItems.add(new EffectTemplateItem(getString(R.string.bpm140), new ArrayList<>(Arrays.asList(140f))));
        sMetronomeItems.add(new EffectTemplateItem(getString(R.string.bpm150), new ArrayList<>(Arrays.asList(150f))));
        sMetronomeItems.add(new EffectTemplateItem(getString(R.string.bpm160), new ArrayList<>(Arrays.asList(160f))));

        saveData();
        mEffectTemplatesAdapter.notifyDataSetChanged();
        resetMetronome();
    }

    private void resetSoundEffects() {
        if (sSoundEffectItems.size() > 0) sSoundEffectItems.clear();

        sSoundEffectItems.add(new EffectTemplateItem(getString(R.string.recordNoise), new ArrayList<>(Arrays.asList(100.0f))));
        sSoundEffectItems.add(new EffectTemplateItem(getString(R.string.wave), new ArrayList<>(Arrays.asList(100.0f))));
        sSoundEffectItems.add(new EffectTemplateItem(getString(R.string.rain), new ArrayList<>(Arrays.asList(100.0f))));
        sSoundEffectItems.add(new EffectTemplateItem(getString(R.string.river), new ArrayList<>(Arrays.asList(100.0f))));
        sSoundEffectItems.add(new EffectTemplateItem(getString(R.string.war), new ArrayList<>(Arrays.asList(100.0f))));
        sSoundEffectItems.add(new EffectTemplateItem(getString(R.string.fire), new ArrayList<>(Arrays.asList(100.0f))));
        sSoundEffectItems.add(new EffectTemplateItem(getString(R.string.concertVenue), new ArrayList<>(Arrays.asList(100.0f))));

        mEffectTemplatesAdapter.notifyDataSetChanged();
        resetSoundEffect();
    }

    public void onEffectItemClick(int nEffect) {
        if (nEffect < 0 || sEffectItems.size() <= nEffect) return;
        EffectItem item = sEffectItems.get(nEffect);

        boolean selected = item.isSelected();

        if (item.isSelected()) deselectEffect(nEffect);
        else item.setSelected(true);
        mEffectsAdapter.notifyItemChanged(nEffect);

        if (!selected) {
            mBtnEffectOff.setSelected(false);
            if (nEffect == EFFECTTYPE_REVERB || nEffect == EFFECTTYPE_ECHO || nEffect == EFFECTTYPE_CHORUS || nEffect == EFFECTTYPE_DISTORTION || nEffect == EFFECTTYPE_COMP || nEffect == EFFECTTYPE_PAN || nEffect == EFFECTTYPE_FREQ || nEffect == EFFECTTYPE_METRONOME || nEffect == EFFECTTYPE_SOUNDEFFECT) {
                onEffectDetailClick(nEffect);
                return;
            }
        }

        if (!item.isSelected() && nEffect == EFFECTTYPE_REVERSE) {
            int chan = BASS_FX.BASS_FX_TempoGetSource(MainActivity.sStream);
            BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_FORWARD);
            MainActivity.setSync();
        }
        if (!item.isSelected() && (nEffect == EFFECTTYPE_RANDOM || nEffect == EFFECTTYPE_TRANSCRIBESIDEGUITAR || nEffect == EFFECTTYPE_OLDRECORD || nEffect == EFFECTTYPE_LOWBATTERY || nEffect == EFFECTTYPE_EARTRAINING))
            EqualizerFragment.resetEQ();
        if (!item.isSelected() && (nEffect == EFFECTTYPE_RANDOM || nEffect == EFFECTTYPE_NOSENSE_STRONG || nEffect == EFFECTTYPE_NOSENSE_MIDDLE || nEffect == EFFECTTYPE_NOSENSE_WEAK)) {
            ControlFragment.setSpeed(0.0f);
            ControlFragment.setPitch(0.0f);
        }
        if (!item.isSelected() && nEffect == EFFECTTYPE_TRANSCRIBEBASS) {
            EqualizerFragment.resetEQ();
            ControlFragment.setPitch(0.0f);
        }
        if (!item.isSelected() && nEffect == EFFECTTYPE_8BITSOUND) ControlFragment.setSpeed(0.0f);
        if (!item.isSelected() && nEffect == EFFECTTYPE_METRONOME) sMetronome.stop();
        checkDuplicate(nEffect);
        if (sSEStream != 0) {
            BASS.BASS_StreamFree(sSEStream);
            sSEStream = 0;
        }
        if (sSEStream2 != 0) {
            BASS.BASS_StreamFree(sSEStream2);
            sSEStream2 = 0;
        }
        if (sHandler != null) {
            sHandler.removeCallbacks(onTimer);
            sHandler = null;
        }
        applyEffect();
        boolean bSelected = false;
        for (int i = 0; i < sEffectItems.size(); i++) {
            if (sEffectItems.get(i).isSelected()) bSelected = true;
        }
        if (!bSelected) mBtnEffectOff.setSelected(true);
        PlaylistFragment.updateSavingEffect();
    }

    void onEffectTemplateItemClick(int nEffectTemplate) {
        boolean bAlreadySelected = false;
        EffectTemplateItem item;
        if (sEffectDetail == EFFECTTYPE_REVERB) {
            item = sReverbItems.get(nEffectTemplate);
            if (item.isSelected()) bAlreadySelected = true;
            for (int i = 0; i < sReverbItems.size(); i++) {
                if (i != nEffectTemplate) sReverbItems.get(i).setSelected(false);
                else sReverbItems.get(i).setSelected(!bAlreadySelected);
            }
            if (bAlreadySelected) sReverbSelected = -1;
            else sReverbSelected = nEffectTemplate;
        } else if (sEffectDetail == EFFECTTYPE_ECHO) {
            item = sEchoItems.get(nEffectTemplate);
            if (item.isSelected()) bAlreadySelected = true;
            for (int i = 0; i < sEchoItems.size(); i++) {
                if (i != nEffectTemplate) sEchoItems.get(i).setSelected(false);
                else sEchoItems.get(i).setSelected(!bAlreadySelected);
            }
            if (bAlreadySelected) sEchoSelected = -1;
            else sEchoSelected = nEffectTemplate;
        } else if (sEffectDetail == EFFECTTYPE_CHORUS) {
            item = sChorusItems.get(nEffectTemplate);
            if (item.isSelected()) bAlreadySelected = true;
            for (int i = 0; i < sChorusItems.size(); i++) {
                if (i != nEffectTemplate) sChorusItems.get(i).setSelected(false);
                else sChorusItems.get(i).setSelected(!bAlreadySelected);
            }
            if (bAlreadySelected) sChorusSelected = -1;
            else sChorusSelected = nEffectTemplate;
        } else if (sEffectDetail == EFFECTTYPE_DISTORTION) {
            item = sDistortionItems.get(nEffectTemplate);
            if (item.isSelected()) bAlreadySelected = true;
            for (int i = 0; i < sDistortionItems.size(); i++) {
                if (i != nEffectTemplate) sDistortionItems.get(i).setSelected(false);
                else sDistortionItems.get(i).setSelected(!bAlreadySelected);
            }
            if (bAlreadySelected) sDistortionSelected = -1;
            else sDistortionSelected = nEffectTemplate;
        } else if (sEffectDetail == EFFECTTYPE_COMP) {
            item = sCompItems.get(nEffectTemplate);
            if (item.isSelected()) bAlreadySelected = true;
            for (int i = 0; i < sCompItems.size(); i++) {
                if (i != nEffectTemplate) sCompItems.get(i).setSelected(false);
                else sCompItems.get(i).setSelected(!bAlreadySelected);
            }
            if (bAlreadySelected) sCompSelected = -1;
            else sCompSelected = nEffectTemplate;
        } else if (sEffectDetail == EFFECTTYPE_PAN) {
            item = sPanItems.get(nEffectTemplate);
            if (item.isSelected()) bAlreadySelected = true;
            for (int i = 0; i < sPanItems.size(); i++) {
                if (i != nEffectTemplate) sPanItems.get(i).setSelected(false);
                else sPanItems.get(i).setSelected(!bAlreadySelected);
            }
            if (bAlreadySelected) sPanSelected = -1;
            else sPanSelected = nEffectTemplate;
        } else if (sEffectDetail == EFFECTTYPE_FREQ) {
            item = sFreqItems.get(nEffectTemplate);
            if (item.isSelected()) bAlreadySelected = true;
            for (int i = 0; i < sFreqItems.size(); i++) {
                if (i != nEffectTemplate) sFreqItems.get(i).setSelected(false);
                else sFreqItems.get(i).setSelected(!bAlreadySelected);
            }
            if (bAlreadySelected) sFreqSelected = -1;
            else sFreqSelected = nEffectTemplate;
        } else if (sEffectDetail == EFFECTTYPE_METRONOME) {
            item = sMetronomeItems.get(nEffectTemplate);
            if (item.isSelected()) bAlreadySelected = true;
            for (int i = 0; i < sMetronomeItems.size(); i++) {
                if (i != nEffectTemplate) sMetronomeItems.get(i).setSelected(false);
                else sMetronomeItems.get(i).setSelected(!bAlreadySelected);
            }
            if (bAlreadySelected) sMetronomeSelected = -1;
            else sMetronomeSelected = nEffectTemplate;
        } else if (sEffectDetail == EFFECTTYPE_SOUNDEFFECT) {
            item = sSoundEffectItems.get(nEffectTemplate);
            if (item.isSelected()) bAlreadySelected = true;
            for (int i = 0; i < sSoundEffectItems.size(); i++) {
                if (i != nEffectTemplate) sSoundEffectItems.get(i).setSelected(false);
                else sSoundEffectItems.get(i).setSelected(!bAlreadySelected);
            }
            if (bAlreadySelected) sSoundEffectSelected = -1;
            else sSoundEffectSelected = nEffectTemplate;
        } else return;
        mBtnEffectTemplateOff.setSelected(bAlreadySelected);

        mEffectTemplatesAdapter.notifyDataSetChanged();

        EffectItem effectItem = sEffectItems.get(sEffectDetail);
        if (bAlreadySelected) deselectEffect(sEffectDetail);
        else {
            effectItem.setSelected(true);
            checkDuplicate(sEffectDetail);
        }
        boolean bSelected = false;
        for (int i = 0; i < sEffectItems.size(); i++) {
            if (sEffectItems.get(i).isSelected()) bSelected = true;
        }
        if (!bSelected) mBtnEffectOff.setSelected(true);
        PlaylistFragment.updateSavingEffect();
        mEffectsAdapter.notifyItemChanged(sEffectDetail);

        if (sEffectDetail == EFFECTTYPE_REVERB) {
            if (sReverbSelected == -1) resetReverb();
            else {
                ArrayList<Float> arFloats = sReverbItems.get(nEffectTemplate).getArPresets();
                setReverb(arFloats.get(0), arFloats.get(1), arFloats.get(2), arFloats.get(3), arFloats.get(4), true);
            }
        } else if (sEffectDetail == EFFECTTYPE_ECHO) {
            if (sEchoSelected == -1) resetEcho();
            else {
                ArrayList<Float> arFloats = sEchoItems.get(nEffectTemplate).getArPresets();
                setEcho(arFloats.get(0), arFloats.get(1), arFloats.get(2), arFloats.get(3), true);
            }
        } else if (sEffectDetail == EFFECTTYPE_CHORUS) {
            if (sChorusSelected == -1) resetChorus();
            else {
                ArrayList<Float> arFloats = sChorusItems.get(nEffectTemplate).getArPresets();
                setChorus(arFloats.get(0), arFloats.get(1), arFloats.get(2), arFloats.get(3), arFloats.get(4), arFloats.get(5), true);
            }
        } else if (sEffectDetail == EFFECTTYPE_DISTORTION) {
            if (sDistortionSelected == -1) resetDistortion();
            else {
                ArrayList<Float> arFloats = sDistortionItems.get(nEffectTemplate).getArPresets();
                setDistortion(arFloats.get(0), arFloats.get(1), arFloats.get(2), arFloats.get(3), arFloats.get(4), true);
            }
        } else if (sEffectDetail == EFFECTTYPE_COMP) {
            if (sCompSelected == -1) resetComp();
            else {
                ArrayList<Float> arFloats = sCompItems.get(nEffectTemplate).getArPresets();
                setComp(arFloats.get(0), arFloats.get(1), arFloats.get(2), arFloats.get(3), arFloats.get(4), true);
            }
        } else if (sEffectDetail == EFFECTTYPE_PAN) {
            if (sPanSelected == -1) resetPan();
            else {
                ArrayList<Float> arFloats = sPanItems.get(nEffectTemplate).getArPresets();
                setPan(arFloats.get(0), true);
            }
        } else if (sEffectDetail == EFFECTTYPE_FREQ) {
            if (sFreqSelected == -1) resetFreq();
            else {
                ArrayList<Float> arFloats = sFreqItems.get(nEffectTemplate).getArPresets();
                setFreq(arFloats.get(0), true);
            }
        } else if (sEffectDetail == EFFECTTYPE_METRONOME) {
            if (sMetronomeSelected == -1) resetMetronome();
            else {
                ArrayList<Float> arFloats = sMetronomeItems.get(nEffectTemplate).getArPresets();
                setMetronome(arFloats.get(0), true);
            }
        } else if (sEffectDetail == EFFECTTYPE_SOUNDEFFECT) {
            if (sSoundEffectSelected == -1) resetSoundEffect();
            else {
                if (sSEStream != 0) {
                    BASS.BASS_StreamFree(sSEStream);
                    sSEStream = 0;
                }
                if (sSEStream2 != 0) {
                    BASS.BASS_StreamFree(sSEStream2);
                    sSEStream2 = 0;
                }
                if (sHandler != null) {
                    sHandler.removeCallbacks(onTimer);
                    sHandler = null;
                }
                ArrayList<Float> arFloats = sSoundEffectItems.get(nEffectTemplate).getArPresets();
                setSoundEffect(arFloats.get(0), true);
            }
        }
    }

    public static void resetEffect() {
        resetEffect(true);
    }

    public static void resetEffect(boolean save) {
        if(sActivity != null) sActivity.effectFragment.getBtnEffectOff().setSelected(true);
        for (int i = 0; i < sEffectItems.size(); i++) {
            if (sEffectItems.get(i).isSelected() && (i == EFFECTTYPE_RANDOM || i == EFFECTTYPE_TRANSCRIBESIDEGUITAR || i == EFFECTTYPE_OLDRECORD || i == EFFECTTYPE_LOWBATTERY || i == EFFECTTYPE_EARTRAINING))
                EqualizerFragment.resetEQ(save);
            if (sEffectItems.get(i).isSelected() && (i == EFFECTTYPE_RANDOM || i == EFFECTTYPE_NOSENSE_STRONG || i == EFFECTTYPE_NOSENSE_MIDDLE || i == EFFECTTYPE_NOSENSE_WEAK)) {
                ControlFragment.setSpeed(0.0f, save);
                ControlFragment.setPitch(0.0f, save);
            }
            if (sEffectItems.get(i).isSelected() && (i == EFFECTTYPE_TRANSCRIBEBASS)) {
                ControlFragment.setPitch(0.0f, save);
                EqualizerFragment.resetEQ(save);
            }
            if (sEffectItems.get(i).isSelected() && (i == EFFECTTYPE_8BITSOUND))
                ControlFragment.setSpeed(0.0f, save);
            sEffectItems.get(i).setSelected(false);
            if(sActivity != null) sActivity.effectFragment.getEffectsAdapter().notifyItemChanged(i);
        }
        sMetronome.stop();
        sPan = 0.0f;
        sFreq = 1.0f;
        setTimeOfIncreaseSpeed(1.0f);
        setIncreaseSpeedSpecified(0.1f);
        setIncreaseSpeedLoop(1.0f);
        setTimeOfDecreaseSpeed(1.0f);
        setDecreaseSpeedSpecified(0.1f);
        setDecreaseSpeedLoop(1.0f);
        setTimeOfRaisePitch(1.0f);
        setRaisePitchSpecified(0.1f);
        setRaisePitchLoop(1.0f);
        setTimeOfLowerPitch(1.0f);
        setLowerPitchSpecified(0.1f);
        setLowerPitchLoop(1.0f);
        resetComp(save);
        resetPan(save);
        resetFreq(save);
        resetMetronome(save);
        resetEcho(save);
        resetReverb(save);
        resetChorus(save);
        resetDistortion(save);
        resetSoundEffect(save);
    }

    private void setCompRandom() {
        setComp(getRandomValue(0, mSeekCompGain.getMax()),
                getRandomValue(0, mSeekCompThreshold.getMax()),
                getRandomValue(0, mSeekCompRatio.getMax()),
                getRandomValue(0, mSeekCompAttack.getMax()),
                getRandomValue(0, mSeekCompRelease.getMax()),
                true);
    }

    private static void resetComp() {
        resetComp(true);
    }

    private static void resetComp(boolean save) {
        if (sActivity != null && sActivity.effectFragment.getScrollCompCustomize().getVisibility() != View.VISIBLE)
            sEffectItems.get(EFFECTTYPE_COMP).setSelected(false);
        boolean bSelected = false;
        for (int i = 0; i < sEffectItems.size(); i++) {
            if (sEffectItems.get(i).isSelected()) bSelected = true;
        }
        for (int i = 0; i < sCompItems.size(); i++) sCompItems.get(i).setSelected(false);

        sCompSelected = -1;
        if (sActivity != null) {
            if (!bSelected) sActivity.effectFragment.getBtnEffectOff().setSelected(true);
            sActivity.effectFragment.getBtnEffectTemplateOff().setSelected(true);
            sActivity.effectFragment.getEffectsAdapter().notifyDataSetChanged();
            sActivity.effectFragment.getEffectTemplatesAdapter().notifyDataSetChanged();
        }

        BASS.BASS_ChannelRemoveFX(MainActivity.sStream, sFxComp);
        sFxComp = 0;

        setComp(200, 4000, 900, 119, 39999, save);
    }

    private void setPanRandom() {
        setPan(getRandomValue(0, mSeekPanValue.getMax()),
                true);
    }

    private static void resetPan() {
        resetPan(true);
    }

    private static void resetPan(boolean save) {
        if (sActivity != null && sActivity.effectFragment.getScrollPanCustomize().getVisibility() != View.VISIBLE)
            sEffectItems.get(EFFECTTYPE_PAN).setSelected(false);
        boolean bSelected = false;
        for (int i = 0; i < sEffectItems.size(); i++) {
            if (sEffectItems.get(i).isSelected()) bSelected = true;
        }
        for (int i = 0; i < sPanItems.size(); i++) sPanItems.get(i).setSelected(false);

        sPanSelected = -1;
        if (sActivity != null) {
            if (!bSelected) sActivity.effectFragment.getBtnEffectOff().setSelected(true);
            sActivity.effectFragment.getBtnEffectTemplateOff().setSelected(true);
            sActivity.effectFragment.getEffectsAdapter().notifyDataSetChanged();
            sActivity.effectFragment.getEffectTemplatesAdapter().notifyDataSetChanged();
        }

        BASS.BASS_ChannelRemoveDSP(MainActivity.sStream, sDspPan);
        sDspPan = 0;

        setPan(100, save);
    }

    private void setFreqRandom() {
        setFreq(getRandomValue(0, mSeekFreqValue.getMax()),
                true);
    }

    private static void resetFreq() {
        resetFreq(true);
    }

    private static void resetFreq(boolean save) {
        if (sActivity != null && sActivity.effectFragment.getScrollFreqCustomize().getVisibility() != View.VISIBLE)
            sEffectItems.get(EFFECTTYPE_FREQ).setSelected(false);
        boolean bSelected = false;
        for (int i = 0; i < sEffectItems.size(); i++) {
            if (sEffectItems.get(i).isSelected()) bSelected = true;
        }
        for (int i = 0; i < sFreqItems.size(); i++) sFreqItems.get(i).setSelected(false);

        sFreqSelected = -1;
        if (sActivity != null) {
            if (!bSelected) sActivity.effectFragment.getBtnEffectOff().setSelected(true);
            sActivity.effectFragment.getBtnEffectTemplateOff().setSelected(true);
            sActivity.effectFragment.getEffectsAdapter().notifyDataSetChanged();
            sActivity.effectFragment.getEffectTemplatesAdapter().notifyDataSetChanged();
        }

        BASS.BASS_CHANNELINFO info = new BASS.BASS_CHANNELINFO();
        BASS.BASS_ChannelGetInfo(MainActivity.sStream, info);
        BASS.BASS_ChannelSetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, info.freq);

        setFreq(90, save);
    }

    private void setMetronomeRandom() {
        setMetronome(getRandomValue(0, mSeekMetronomeValue.getMax()),
                true);
    }

    private static void resetMetronome() {
        resetMetronome(true);
    }

    private static void resetMetronome(boolean save) {
        if (sActivity != null && sActivity.effectFragment.getScrollMetronomeCustomize().getVisibility() != View.VISIBLE)
            sEffectItems.get(EFFECTTYPE_METRONOME).setSelected(false);
        boolean bSelected = false;
        for (int i = 0; i < sEffectItems.size(); i++) {
            if (sEffectItems.get(i).isSelected()) bSelected = true;
        }
        for (int i = 0; i < sMetronomeItems.size(); i++) sMetronomeItems.get(i).setSelected(false);

        sMetronomeSelected = -1;
        if (sActivity != null) {
            if (!bSelected) sActivity.effectFragment.getBtnEffectOff().setSelected(true);
            sActivity.effectFragment.getBtnEffectTemplateOff().setSelected(true);
            sActivity.effectFragment.getEffectsAdapter().notifyDataSetChanged();
            sActivity.effectFragment.getEffectTemplatesAdapter().notifyDataSetChanged();
        }

        sMetronome.stop();

        setMetronome(110, save);
    }

    private void setEchoRandom() {
        int nDry = getRandomValue(50, 100);
        int nWet;
        while (true) {
            nWet = getRandomValue(10, 100);
            if (nWet <= nDry) break;
        }
        setEcho(nDry, nWet, getRandomValue(0, mSeekEchoFeedback.getMax()),
                getRandomValue(0, mSeekReverbWidth.getMax()), true);
    }

    private static void resetEcho() {
        resetEcho(true);
    }

    private static void resetEcho(boolean save) {
        if (sActivity != null && sActivity.effectFragment.getScrollEchoCustomize().getVisibility() != View.VISIBLE)
            sEffectItems.get(EFFECTTYPE_ECHO).setSelected(false);
        boolean bSelected = false;
        for (int i = 0; i < sEffectItems.size(); i++) {
            if (sEffectItems.get(i).isSelected()) bSelected = true;
        }
        for (int i = 0; i < sEchoItems.size(); i++) sEchoItems.get(i).setSelected(false);

        sEchoSelected = -1;
        if (sActivity != null) {
            if (!bSelected) sActivity.effectFragment.getBtnEffectOff().setSelected(true);
            sActivity.effectFragment.getBtnEffectTemplateOff().setSelected(true);
            sActivity.effectFragment.getEffectsAdapter().notifyDataSetChanged();
            sActivity.effectFragment.getEffectTemplatesAdapter().notifyDataSetChanged();
        }

        BASS.BASS_ChannelRemoveFX(MainActivity.sStream, sFxEcho);
        sFxEcho = 0;

        setEcho(100, 30, 60, 7, save);
    }

    private int getRandomValue(int nMin, int nMax) {
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

    private static void resetReverb() {
        resetReverb(true);
    }

    private static void resetReverb(boolean save) {
        if (sActivity != null && sActivity.effectFragment.getScrollReverbCustomize().getVisibility() != View.VISIBLE)
            sEffectItems.get(EFFECTTYPE_REVERB).setSelected(false);
        boolean bSelected = false;
        for (int i = 0; i < sEffectItems.size(); i++) {
            if (sEffectItems.get(i).isSelected()) bSelected = true;
        }
        for (int i = 0; i < sReverbItems.size(); i++) sReverbItems.get(i).setSelected(false);

        sReverbSelected = -1;
        if (sActivity != null) {
            if (!bSelected) sActivity.effectFragment.getBtnEffectOff().setSelected(true);
            sActivity.effectFragment.getBtnEffectTemplateOff().setSelected(true);
            sActivity.effectFragment.getEffectsAdapter().notifyDataSetChanged();
            sActivity.effectFragment.getEffectTemplatesAdapter().notifyDataSetChanged();
        }

        BASS.BASS_ChannelRemoveFX(MainActivity.sStream, sFxReverb);
        sFxReverb = 0;

        setReverb(70, 100, 85, 50, 90, save);
    }

    private void setChorusRandom() {
        int nMaxSweep = getRandomValue(0, mSeekChorusMaxSweep.getMax());
        int nMinSweep;
        while (true) {
            nMinSweep = getRandomValue(0, mSeekChorusMinSweep.getMax());
            if (nMinSweep <= nMaxSweep) break;
        }
        setChorus(getRandomValue(50, 100), getRandomValue(10, 50), getRandomValue(0, mSeekChorusFeedback.getMax()),
                nMinSweep, nMaxSweep, getRandomValue(0, mSeekChorusRate.getMax()), true);
    }

    private static void resetChorus() {
        resetChorus(true);
    }

    private static void resetChorus(boolean save) {
        if (sActivity != null && sActivity.effectFragment.getScrollChorusCustomize().getVisibility() != View.VISIBLE)
            sEffectItems.get(EFFECTTYPE_CHORUS).setSelected(false);
        boolean bSelected = false;
        for (int i = 0; i < sEffectItems.size(); i++) {
            if (sEffectItems.get(i).isSelected()) bSelected = true;
        }
        for (int i = 0; i < sChorusItems.size(); i++) sChorusItems.get(i).setSelected(false);

        sChorusSelected = -1;
        if (sActivity != null) {
            if (!bSelected) sActivity.effectFragment.getBtnEffectOff().setSelected(true);
            sActivity.effectFragment.getBtnEffectTemplateOff().setSelected(true);
            sActivity.effectFragment.getEffectsAdapter().notifyDataSetChanged();
            sActivity.effectFragment.getEffectTemplatesAdapter().notifyDataSetChanged();
        }

        BASS.BASS_ChannelRemoveFX(MainActivity.sStream, sFxChorus);
        sFxChorus = 0;

        setChorus(100, 10, 50, 100, 200, 1000, save);
    }

    private void setDistortionRandom() {
        int nDry = getRandomValue(50, 100);
        int nWet;
        while (true) {
            nWet = getRandomValue(10, 100);
            if (nWet <= nDry) break;
        }
        setDistortion(getRandomValue(0, mSeekDistortionDrive.getMax()), nDry, nWet,
                getRandomValue(0, mSeekDistortionFeedback.getMax()), getRandomValue(80, 120),
                true);
    }

    private static void resetDistortion() {
        resetDistortion(true);
    }

    private static void resetDistortion(boolean save) {
        if (sActivity != null && sActivity.effectFragment.getScrollDistortionCustomize().getVisibility() != View.VISIBLE)
            sEffectItems.get(EFFECTTYPE_DISTORTION).setSelected(false);
        boolean bSelected = false;
        for (int i = 0; i < sEffectItems.size(); i++) {
            if (sEffectItems.get(i).isSelected()) bSelected = true;
        }
        for (int i = 0; i < sDistortionItems.size(); i++)
            sDistortionItems.get(i).setSelected(false);

        sDistortionSelected = -1;
        if (sActivity != null) {
            if (!bSelected) sActivity.effectFragment.getBtnEffectOff().setSelected(true);
            sActivity.effectFragment.getBtnEffectTemplateOff().setSelected(true);
            sActivity.effectFragment.getEffectsAdapter().notifyDataSetChanged();
            sActivity.effectFragment.getEffectTemplatesAdapter().notifyDataSetChanged();
        }

        BASS.BASS_ChannelRemoveFX(MainActivity.sStream, sFxDistortion);
        sFxDistortion = 0;

        setDistortion(20, 95, 5, 10, 100, save);
    }

    private static void resetSoundEffect() {
        resetSoundEffect(true);
    }

    private static void resetSoundEffect(boolean save) {
        if (sActivity != null && sActivity.effectFragment.getScrollSoundEffectCustomize().getVisibility() != View.VISIBLE)
            sEffectItems.get(EFFECTTYPE_SOUNDEFFECT).setSelected(false);
        boolean bSelected = false;
        for (int i = 0; i < sEffectItems.size(); i++) {
            if (sEffectItems.get(i).isSelected()) bSelected = true;
        }
        for (int i = 0; i < sSoundEffectItems.size(); i++)
            sSoundEffectItems.get(i).setSelected(false);

        sSoundEffectSelected = -1;
        if (sActivity != null) {
            if (!bSelected) sActivity.effectFragment.getBtnEffectOff().setSelected(true);
            sActivity.effectFragment.getBtnEffectTemplateOff().setSelected(true);
            sActivity.effectFragment.getEffectsAdapter().notifyDataSetChanged();
            sActivity.effectFragment.getEffectTemplatesAdapter().notifyDataSetChanged();
        }
        if (sSEStream != 0) {
            BASS.BASS_StreamFree(sSEStream);
            sSEStream = 0;
        }
        if (sSEStream2 != 0) {
            BASS.BASS_StreamFree(sSEStream2);
            sSEStream2 = 0;
        }
        if (sHandler != null) {
            sHandler.removeCallbacks(onTimer);
            sHandler = null;
        }

        setSoundEffect(100, save);
    }

    void onEffectDetailClick(int nEffect) {
        sEffectDetail = nEffect;
        EffectItem item = sEffectItems.get(nEffect);
        if (!item.isSelected()) {
            if (sEffectDetail == EFFECTTYPE_REVERB) {
                if (sReverbSelected != -1) onEffectItemClick(nEffect);
            } else if (sEffectDetail == EFFECTTYPE_ECHO) {
                if (sEchoSelected != -1) onEffectItemClick(nEffect);
            } else if (sEffectDetail == EFFECTTYPE_CHORUS) {
                if (sChorusSelected != -1) onEffectItemClick(nEffect);
            } else if (sEffectDetail == EFFECTTYPE_DISTORTION) {
                if (sDistortionSelected != -1) onEffectItemClick(nEffect);
            } else if (sEffectDetail == EFFECTTYPE_COMP) {
                if (sCompSelected != -1) onEffectItemClick(nEffect);
            } else if (sEffectDetail == EFFECTTYPE_PAN) {
                if (sPanSelected != -1) onEffectItemClick(nEffect);
            } else if (sEffectDetail == EFFECTTYPE_FREQ) {
                if (sFreqSelected != -1) onEffectItemClick(nEffect);
            } else if (sEffectDetail == EFFECTTYPE_METRONOME) {
                if (sMetronomeSelected != -1) onEffectItemClick(nEffect);
            } else if (sEffectDetail == EFFECTTYPE_SOUNDEFFECT) {
                if (sSoundEffectSelected != -1) onEffectItemClick(nEffect);
            } else onEffectItemClick(nEffect);
        }

        mTextEffectName.setText(sEffectItems.get(nEffect).getEffectName());
        if (nEffect == EFFECTTYPE_INCREASESPEED) {
            mRadioButtonSpecifiedEffectDetail.setText(R.string.incSpeedTitle);
            mRadioButtonSpecifiedEffectDetail.setVisibility(View.VISIBLE);
            mRadioButtonLoopEffectDetail.setText(R.string.incSpeedLoopTitle);
            mRadioButtonLoopEffectDetail.setVisibility(View.VISIBLE);
            mRadioGroupEffectDetail.setVisibility(View.VISIBLE);
            mEditTimeSpecifiedEffectDetail.setText(String.format(Locale.getDefault(), "%.1f%s", sTimeOfIncreaseSpeedSpecified, getString(R.string.sec)));
            mEditSpeedSpecifiedEffectDetail.setText(String.format(Locale.getDefault(), "%.1f%%", sIncreaseSpeedSpecified));
            mEditSpeedLoopEffectDetail.setText(String.format(Locale.getDefault(), "%.1f%%", sIncreaseSpeedLoop));
        } else if (nEffect == EFFECTTYPE_DECREASESPEED) {
            mRadioButtonSpecifiedEffectDetail.setText(R.string.decSpeedTitle);
            mRadioButtonSpecifiedEffectDetail.setVisibility(View.VISIBLE);
            mRadioButtonLoopEffectDetail.setText(R.string.decSpeedLoopTitle);
            mRadioButtonLoopEffectDetail.setVisibility(View.VISIBLE);
            mRadioGroupEffectDetail.setVisibility(View.VISIBLE);
            mEditTimeSpecifiedEffectDetail.setText(String.format(Locale.getDefault(), "%.1f%s", sTimeOfDecreaseSpeedSpecified, getString(R.string.sec)));
            mEditSpeedSpecifiedEffectDetail.setText(String.format(Locale.getDefault(), "%.1f%%", sDecreaseSpeedSpecified));
            mEditSpeedLoopEffectDetail.setText(String.format(Locale.getDefault(), "%.1f%%", sDecreaseSpeedLoop));
        } else if (nEffect == EFFECTTYPE_RAISEPITCH) {
            mRadioButtonSpecifiedEffectDetail.setText(R.string.rasPitchTitle);
            mRadioButtonSpecifiedEffectDetail.setVisibility(View.VISIBLE);
            mRadioButtonLoopEffectDetail.setText(R.string.rasPitchLoopTitle);
            mRadioButtonLoopEffectDetail.setVisibility(View.VISIBLE);
            mRadioGroupEffectDetail.setVisibility(View.VISIBLE);
            mEditTimeSpecifiedEffectDetail.setText(String.format(Locale.getDefault(), "%.1f%s", sTimeOfRaisePitchSpecified, getString(R.string.sec)));
            mEditSpeedSpecifiedEffectDetail.setText(String.format(Locale.getDefault(), "%s%.1f", getString(R.string.sharp), sRaisePitchSpecified));
            mEditPitchLoopEffectDetail.setText(String.format(Locale.getDefault(), "%s%.1f", getString(R.string.sharp), sRaisePitchLoop));
        } else if (nEffect == EFFECTTYPE_LOWERPITCH) {
            mRadioButtonSpecifiedEffectDetail.setText(R.string.lowPitchTitle);
            mRadioButtonSpecifiedEffectDetail.setVisibility(View.VISIBLE);
            mRadioButtonLoopEffectDetail.setText(R.string.lowPitchLoopTitle);
            mRadioButtonLoopEffectDetail.setVisibility(View.VISIBLE);
            mRadioGroupEffectDetail.setVisibility(View.VISIBLE);
            mEditTimeSpecifiedEffectDetail.setText(String.format(Locale.getDefault(), "%.1f%s", sTimeOfLowerPitchSpecified, getString(R.string.sec)));
            mEditSpeedSpecifiedEffectDetail.setText(String.format(Locale.getDefault(), "%s%.1f", getString(R.string.flat), sLowerPitchSpecified));
            mEditPitchLoopEffectDetail.setText(String.format(Locale.getDefault(), "%s%.1f", getString(R.string.flat), sLowerPitchLoop));
        } else {
            mRadioGroupEffectDetail.setVisibility(View.INVISIBLE);
        }

        if (nEffect == EFFECTTYPE_INCREASESPEED || nEffect == EFFECTTYPE_DECREASESPEED) {
            mTextSpeedSpecifiedEffectDetail.setText(getString(R.string.speedWithColon));
            mRelativeSpecifiedEffectDetail.setVisibility(View.VISIBLE);
            mTextSpeedLoopEffectDetail.setText(getString(R.string.speedWithColon));
            mRelativeLoopEffectDetail.setVisibility(View.VISIBLE);
            mScrollCompCustomize.setVisibility(View.GONE);
            mScrollPanCustomize.setVisibility(View.GONE);
            mScrollFreqCustomize.setVisibility(View.GONE);
            mScrollMetronomeCustomize.setVisibility(View.GONE);
            mScrollEchoCustomize.setVisibility(View.GONE);
            mScrollReverbCustomize.setVisibility(View.GONE);
            mScrollChorusCustomize.setVisibility(View.GONE);
            mScrollDistortionCustomize.setVisibility(View.GONE);
            mBtnEffectTemplateMenu.setVisibility(View.VISIBLE);
            mScrollSoundEffectCustomize.setVisibility(View.GONE);
        } else if (nEffect == EFFECTTYPE_RAISEPITCH || nEffect == EFFECTTYPE_LOWERPITCH) {
            mTextSpeedSpecifiedEffectDetail.setText(getString(R.string.pitchWithColon));
            mRelativeSpecifiedEffectDetail.setVisibility(View.VISIBLE);
            mTextSpeedLoopEffectDetail.setText(getString(R.string.pitchWithColon));
            mRelativeLoopEffectDetail.setVisibility(View.VISIBLE);
            mScrollCompCustomize.setVisibility(View.GONE);
            mScrollPanCustomize.setVisibility(View.GONE);
            mScrollFreqCustomize.setVisibility(View.GONE);
            mScrollMetronomeCustomize.setVisibility(View.GONE);
            mScrollEchoCustomize.setVisibility(View.GONE);
            mScrollReverbCustomize.setVisibility(View.GONE);
            mScrollChorusCustomize.setVisibility(View.GONE);
            mScrollDistortionCustomize.setVisibility(View.GONE);
            mBtnEffectTemplateMenu.setVisibility(View.VISIBLE);
            mScrollSoundEffectCustomize.setVisibility(View.GONE);
        } else if (nEffect == EFFECTTYPE_COMP) {
            mRelativeSpecifiedEffectDetail.setVisibility(View.GONE);
            mRelativeLoopEffectDetail.setVisibility(View.GONE);
            mEffectTemplatesAdapter.changeItems(sCompItems);
            mBtnAddEffectTemplate.setContentDescription(getString(R.string.newComp));
            mBtnAddEffectTemplate.setAlpha(1f);
            mBtnAddEffectTemplate.setVisibility(View.VISIBLE);
            for (int i = 0; i < sCompItems.size(); i++) sCompItems.get(i).setSelected(false);
            if (sCompSelected == -1) resetComp();
            else onEffectTemplateItemClick(sCompSelected);
            mEffectTemplatesAdapter.notifyDataSetChanged();
            mBtnEffectTemplateMenu.setVisibility(View.VISIBLE);
            mRelativeEffectTemplates.setVisibility(View.VISIBLE);
        } else if (nEffect == EFFECTTYPE_PAN) {
            mRelativeSpecifiedEffectDetail.setVisibility(View.GONE);
            mRelativeLoopEffectDetail.setVisibility(View.GONE);
            mEffectTemplatesAdapter.changeItems(sPanItems);
            mBtnAddEffectTemplate.setContentDescription(getString(R.string.newPan));
            mBtnAddEffectTemplate.setAlpha(1f);
            mBtnAddEffectTemplate.setVisibility(View.VISIBLE);
            for (int i = 0; i < sPanItems.size(); i++) sPanItems.get(i).setSelected(false);
            if (sPanSelected == -1) resetPan();
            else onEffectTemplateItemClick(sPanSelected);
            mEffectTemplatesAdapter.notifyDataSetChanged();
            mBtnEffectTemplateMenu.setVisibility(View.VISIBLE);
            mRelativeEffectTemplates.setVisibility(View.VISIBLE);
        } else if (nEffect == EFFECTTYPE_FREQ) {
            mRelativeSpecifiedEffectDetail.setVisibility(View.GONE);
            mRelativeLoopEffectDetail.setVisibility(View.GONE);
            mEffectTemplatesAdapter.changeItems(sFreqItems);
            mBtnAddEffectTemplate.setContentDescription(getString(R.string.newFreq));
            mBtnAddEffectTemplate.setAlpha(1f);
            mBtnAddEffectTemplate.setVisibility(View.VISIBLE);
            for (int i = 0; i < sFreqItems.size(); i++) sFreqItems.get(i).setSelected(false);
            if (sFreqSelected == -1) resetFreq();
            else onEffectTemplateItemClick(sFreqSelected);
            mEffectTemplatesAdapter.notifyDataSetChanged();
            mBtnEffectTemplateMenu.setVisibility(View.VISIBLE);
            mRelativeEffectTemplates.setVisibility(View.VISIBLE);
        } else if (nEffect == EFFECTTYPE_METRONOME) {
            mRelativeSpecifiedEffectDetail.setVisibility(View.GONE);
            mRelativeLoopEffectDetail.setVisibility(View.GONE);
            mEffectTemplatesAdapter.changeItems(sMetronomeItems);
            mBtnAddEffectTemplate.setContentDescription(getString(R.string.newMetronome));
            mBtnAddEffectTemplate.setAlpha(1f);
            mBtnAddEffectTemplate.setVisibility(View.VISIBLE);
            for (int i = 0; i < sMetronomeItems.size(); i++) sMetronomeItems.get(i).setSelected(false);
            if (sMetronomeSelected == -1) resetMetronome();
            else onEffectTemplateItemClick(sMetronomeSelected);
            mEffectTemplatesAdapter.notifyDataSetChanged();
            mBtnEffectTemplateMenu.setVisibility(View.VISIBLE);
            mRelativeEffectTemplates.setVisibility(View.VISIBLE);
        } else if (nEffect == EFFECTTYPE_ECHO) {
            mRelativeSpecifiedEffectDetail.setVisibility(View.GONE);
            mRelativeLoopEffectDetail.setVisibility(View.GONE);
            mEffectTemplatesAdapter.changeItems(sEchoItems);
            mBtnAddEffectTemplate.setContentDescription(getString(R.string.newEcho));
            mBtnAddEffectTemplate.setAlpha(1f);
            mBtnAddEffectTemplate.setVisibility(View.VISIBLE);
            for (int i = 0; i < sEchoItems.size(); i++) sEchoItems.get(i).setSelected(false);
            if (sEchoSelected == -1) resetEcho();
            else onEffectTemplateItemClick(sEchoSelected);
            mEffectTemplatesAdapter.notifyDataSetChanged();
            mBtnEffectTemplateMenu.setVisibility(View.VISIBLE);
            mRelativeEffectTemplates.setVisibility(View.VISIBLE);
        } else if (nEffect == EFFECTTYPE_REVERB) {
            mRelativeSpecifiedEffectDetail.setVisibility(View.GONE);
            mRelativeLoopEffectDetail.setVisibility(View.GONE);
            mEffectTemplatesAdapter.changeItems(sReverbItems);
            mBtnAddEffectTemplate.setContentDescription(getString(R.string.newReverb));
            mBtnAddEffectTemplate.setAlpha(1f);
            mBtnAddEffectTemplate.setVisibility(View.VISIBLE);
            for (int i = 0; i < sReverbItems.size(); i++) sReverbItems.get(i).setSelected(false);
            if (sReverbSelected == -1) resetReverb();
            else onEffectTemplateItemClick(sReverbSelected);
            mEffectTemplatesAdapter.notifyDataSetChanged();
            mBtnEffectTemplateMenu.setVisibility(View.VISIBLE);
            mRelativeEffectTemplates.setVisibility(View.VISIBLE);
        } else if (nEffect == EFFECTTYPE_CHORUS) {
            mRelativeSpecifiedEffectDetail.setVisibility(View.GONE);
            mRelativeLoopEffectDetail.setVisibility(View.GONE);
            mEffectTemplatesAdapter.changeItems(sChorusItems);
            mBtnAddEffectTemplate.setContentDescription(getString(R.string.newChorus));
            mBtnAddEffectTemplate.setAlpha(1f);
            mBtnAddEffectTemplate.setVisibility(View.VISIBLE);
            for (int i = 0; i < sChorusItems.size(); i++) sChorusItems.get(i).setSelected(false);
            if (sChorusSelected == -1) resetChorus();
            else onEffectTemplateItemClick(sChorusSelected);
            mEffectTemplatesAdapter.notifyDataSetChanged();
            mBtnEffectTemplateMenu.setVisibility(View.VISIBLE);
            mRelativeEffectTemplates.setVisibility(View.VISIBLE);
        } else if (nEffect == EFFECTTYPE_DISTORTION) {
            mRelativeSpecifiedEffectDetail.setVisibility(View.GONE);
            mRelativeLoopEffectDetail.setVisibility(View.GONE);
            mEffectTemplatesAdapter.changeItems(sDistortionItems);
            mBtnAddEffectTemplate.setContentDescription(getString(R.string.newDistortion));
            mBtnAddEffectTemplate.setAlpha(1f);
            mBtnAddEffectTemplate.setVisibility(View.VISIBLE);
            for (int i = 0; i < sDistortionItems.size(); i++)
                sDistortionItems.get(i).setSelected(false);
            if (sDistortionSelected == -1) resetDistortion();
            else onEffectTemplateItemClick(sDistortionSelected);
            mEffectTemplatesAdapter.notifyDataSetChanged();
            mBtnEffectTemplateMenu.setVisibility(View.VISIBLE);
            mRelativeEffectTemplates.setVisibility(View.VISIBLE);
        } else if (nEffect == EFFECTTYPE_SOUNDEFFECT) {
            mRelativeSpecifiedEffectDetail.setVisibility(View.GONE);
            mRelativeLoopEffectDetail.setVisibility(View.GONE);
            mEffectTemplatesAdapter.changeItems(sSoundEffectItems);
            mBtnAddEffectTemplate.setAlpha(0f);
            mBtnAddEffectTemplate.setVisibility(View.INVISIBLE);
            for (int i = 0; i < sSoundEffectItems.size(); i++)
                sSoundEffectItems.get(i).setSelected(false);
            if (sSoundEffectSelected == -1) resetSoundEffect();
            else onEffectTemplateItemClick(sSoundEffectSelected);
            mEffectTemplatesAdapter.notifyDataSetChanged();
            mBtnEffectTemplateMenu.setVisibility(View.INVISIBLE);
            mRelativeEffectTemplates.setVisibility(View.VISIBLE);
        } else {
            mRelativeSpecifiedEffectDetail.setVisibility(View.GONE);
            mRelativeLoopEffectDetail.setVisibility(View.GONE);
            mScrollCompCustomize.setVisibility(View.GONE);
            mScrollPanCustomize.setVisibility(View.GONE);
            mScrollFreqCustomize.setVisibility(View.GONE);
            mScrollMetronomeCustomize.setVisibility(View.GONE);
            mScrollEchoCustomize.setVisibility(View.GONE);
            mScrollReverbCustomize.setVisibility(View.GONE);
            mScrollChorusCustomize.setVisibility(View.GONE);
            mScrollDistortionCustomize.setVisibility(View.GONE);
            mScrollSoundEffectCustomize.setVisibility(View.GONE);
        }

        mRelativeEffectDetail.setVisibility(View.VISIBLE);
        mBtnEffectOff.setVisibility(View.INVISIBLE);
        mViewSepEffectHeader.setVisibility(View.INVISIBLE);
        mRecyclerEffects.setVisibility(View.INVISIBLE);
    }

    void onEffectCustomizeClick(int nTemplate) {
        mAddTemplate = false;

        if (!isSelectedTemplateItem(nTemplate)) onEffectTemplateItemClick(nTemplate);

        if (sEffectDetail == EFFECTTYPE_REVERB) {
            EffectTemplateItem item = sReverbItems.get(nTemplate);
            mTextEffectName.setText(item.getEffectTemplateName());

            mBtnEffectBack.setText(R.string.back);
            mBtnEffectFinish.setText(R.string.done);

            mBtnEffectFinish.setVisibility(View.VISIBLE);
            mRelativeEffectTemplates.setVisibility(View.INVISIBLE);
            mScrollReverbCustomize.setVisibility(View.VISIBLE);
            mImgEffectBack.setVisibility(View.VISIBLE);
            mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
            mBtnReverbSaveAs.setVisibility(View.VISIBLE);
        } else if (sEffectDetail == EFFECTTYPE_ECHO) {
            EffectTemplateItem item = sEchoItems.get(nTemplate);
            mTextEffectName.setText(item.getEffectTemplateName());

            mBtnEffectBack.setText(R.string.back);
            mBtnEffectFinish.setText(R.string.done);

            mBtnEffectFinish.setVisibility(View.VISIBLE);
            mRelativeEffectTemplates.setVisibility(View.INVISIBLE);
            mScrollEchoCustomize.setVisibility(View.VISIBLE);
            mImgEffectBack.setVisibility(View.VISIBLE);
            mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
            mBtnEchoSaveAs.setVisibility(View.VISIBLE);
        } else if (sEffectDetail == EFFECTTYPE_CHORUS) {
            EffectTemplateItem item = sChorusItems.get(nTemplate);
            mTextEffectName.setText(item.getEffectTemplateName());

            mBtnEffectBack.setText(R.string.back);
            mBtnEffectFinish.setText(R.string.done);

            mBtnEffectFinish.setVisibility(View.VISIBLE);
            mRelativeEffectTemplates.setVisibility(View.INVISIBLE);
            mScrollChorusCustomize.setVisibility(View.VISIBLE);
            mImgEffectBack.setVisibility(View.VISIBLE);
            mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
            mBtnChorusSaveAs.setVisibility(View.VISIBLE);
        } else if (sEffectDetail == EFFECTTYPE_DISTORTION) {
            EffectTemplateItem item = sDistortionItems.get(nTemplate);
            mTextEffectName.setText(item.getEffectTemplateName());

            mBtnEffectBack.setText(R.string.back);
            mBtnEffectFinish.setText(R.string.done);

            mBtnEffectFinish.setVisibility(View.VISIBLE);
            mRelativeEffectTemplates.setVisibility(View.INVISIBLE);
            mScrollDistortionCustomize.setVisibility(View.VISIBLE);
            mImgEffectBack.setVisibility(View.VISIBLE);
            mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
            mBtnDistortionSaveAs.setVisibility(View.VISIBLE);
        } else if (sEffectDetail == EFFECTTYPE_COMP) {
            EffectTemplateItem item = sCompItems.get(nTemplate);
            mTextEffectName.setText(item.getEffectTemplateName());

            mBtnEffectBack.setText(R.string.back);
            mBtnEffectFinish.setText(R.string.done);

            mBtnEffectFinish.setVisibility(View.VISIBLE);
            mRelativeEffectTemplates.setVisibility(View.INVISIBLE);
            mScrollCompCustomize.setVisibility(View.VISIBLE);
            mImgEffectBack.setVisibility(View.VISIBLE);
            mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
            mBtnCompSaveAs.setVisibility(View.VISIBLE);
        } else if (sEffectDetail == EFFECTTYPE_PAN) {
            EffectTemplateItem item = sPanItems.get(nTemplate);
            mTextEffectName.setText(item.getEffectTemplateName());

            mBtnEffectBack.setText(R.string.back);
            mBtnEffectFinish.setText(R.string.done);

            mBtnEffectFinish.setVisibility(View.VISIBLE);
            mRelativeEffectTemplates.setVisibility(View.INVISIBLE);
            mScrollPanCustomize.setVisibility(View.VISIBLE);
            mImgEffectBack.setVisibility(View.VISIBLE);
            mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
            mBtnPanSaveAs.setVisibility(View.VISIBLE);
        } else if (sEffectDetail == EFFECTTYPE_FREQ) {
            EffectTemplateItem item = sFreqItems.get(nTemplate);
            mTextEffectName.setText(item.getEffectTemplateName());

            mBtnEffectBack.setText(R.string.back);
            mBtnEffectFinish.setText(R.string.done);

            mBtnEffectFinish.setVisibility(View.VISIBLE);
            mRelativeEffectTemplates.setVisibility(View.INVISIBLE);
            mScrollFreqCustomize.setVisibility(View.VISIBLE);
            mImgEffectBack.setVisibility(View.VISIBLE);
            mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
            mBtnFreqSaveAs.setVisibility(View.VISIBLE);
        } else if (sEffectDetail == EFFECTTYPE_METRONOME) {
            EffectTemplateItem item = sMetronomeItems.get(nTemplate);
            mTextEffectName.setText(item.getEffectTemplateName());

            mBtnEffectBack.setText(R.string.back);
            mBtnEffectFinish.setText(R.string.done);

            mBtnEffectFinish.setVisibility(View.VISIBLE);
            mRelativeEffectTemplates.setVisibility(View.INVISIBLE);
            mScrollMetronomeCustomize.setVisibility(View.VISIBLE);
            mImgEffectBack.setVisibility(View.VISIBLE);
            mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
            mBtnMetronomeSaveAs.setVisibility(View.VISIBLE);
        } else if (sEffectDetail == EFFECTTYPE_SOUNDEFFECT) {
            EffectTemplateItem item = sSoundEffectItems.get(nTemplate);
            mTextEffectName.setText(item.getEffectTemplateName());

            mBtnEffectBack.setText(R.string.back);
            mBtnEffectFinish.setText(R.string.done);

            mBtnEffectFinish.setVisibility(View.VISIBLE);
            mRelativeEffectTemplates.setVisibility(View.INVISIBLE);
            mScrollSoundEffectCustomize.setVisibility(View.VISIBLE);
            mImgEffectBack.setVisibility(View.VISIBLE);
            mBtnEffectBack.setPadding((int) (32 * sActivity.getDensity()), mBtnEffectBack.getPaddingTop(), mBtnEffectBack.getPaddingRight(), mBtnEffectBack.getPaddingBottom());
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        if (seekBar.getId() == R.id.seekCompGain)
            setComp(progress, mSeekCompThreshold.getProgress(), mSeekCompRatio.getProgress(), mSeekCompAttack.getProgress(), mSeekCompRelease.getProgress(), fromTouch);
        else if (seekBar.getId() == R.id.seekCompThreshold)
            setComp(mSeekCompGain.getProgress(), progress, mSeekCompRatio.getProgress(), mSeekCompAttack.getProgress(), mSeekCompRelease.getProgress(), fromTouch);
        else if (seekBar.getId() == R.id.seekCompRatio)
            setComp(mSeekCompGain.getProgress(), mSeekCompThreshold.getProgress(), progress, mSeekCompAttack.getProgress(), mSeekCompRelease.getProgress(), fromTouch);
        else if (seekBar.getId() == R.id.seekCompAttack)
            setComp(mSeekCompGain.getProgress(), mSeekCompThreshold.getProgress(), mSeekCompRatio.getProgress(), progress, mSeekCompRelease.getProgress(), fromTouch);
        else if (seekBar.getId() == R.id.seekCompRelease)
            setComp(mSeekCompGain.getProgress(), mSeekCompThreshold.getProgress(), mSeekCompRatio.getProgress(), mSeekCompAttack.getProgress(), progress, fromTouch);
        else if (seekBar.getId() == R.id.seekPanValue) setPan(progress, fromTouch);
        else if (seekBar.getId() == R.id.seekFreqValue) setFreq(progress, fromTouch);
        else if (seekBar.getId() == R.id.seekMetronomeValue) setMetronome(progress, fromTouch);
        else if (seekBar.getId() == R.id.seekEchoDry)
            setEcho(progress, mSeekEchoWet.getProgress(), mSeekEchoFeedback.getProgress(), mSeekEchoDelay.getProgress(), fromTouch);
        else if (seekBar.getId() == R.id.seekEchoWet)
            setEcho(mSeekEchoDry.getProgress(), progress, mSeekEchoFeedback.getProgress(), mSeekEchoDelay.getProgress(), fromTouch);
        else if (seekBar.getId() == R.id.seekEchoFeedback)
            setEcho(mSeekEchoDry.getProgress(), mSeekEchoWet.getProgress(), progress, mSeekEchoDelay.getProgress(), fromTouch);
        else if (seekBar.getId() == R.id.seekEchoDelay)
            setEcho(mSeekEchoDry.getProgress(), mSeekEchoWet.getProgress(), mSeekEchoFeedback.getProgress(), progress, fromTouch);
        else if (seekBar.getId() == R.id.seekReverbDry)
            setReverb(progress, mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress(), fromTouch);
        else if (seekBar.getId() == R.id.seekReverbWet)
            setReverb(mSeekReverbDry.getProgress(), progress, mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress(), fromTouch);
        else if (seekBar.getId() == R.id.seekReverbRoomSize)
            setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress(), progress, mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress(), fromTouch);
        else if (seekBar.getId() == R.id.seekReverbDamp)
            setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress(), progress, mSeekReverbWidth.getProgress(), fromTouch);
        else if (seekBar.getId() == R.id.seekReverbWidth)
            setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress(), progress, fromTouch);
        else if (seekBar.getId() == R.id.seekChorusDry)
            setChorus(progress, mSeekChorusWet.getProgress(), mSeekChorusFeedback.getProgress(), mSeekChorusMinSweep.getProgress(), mSeekChorusMaxSweep.getProgress(), mSeekChorusRate.getProgress(), fromTouch);
        else if (seekBar.getId() == R.id.seekChorusWet)
            setChorus(mSeekChorusDry.getProgress(), progress, mSeekChorusFeedback.getProgress(), mSeekChorusMinSweep.getProgress(), mSeekChorusMaxSweep.getProgress(), mSeekChorusRate.getProgress(), fromTouch);
        else if (seekBar.getId() == R.id.seekChorusFeedback)
            setChorus(mSeekChorusDry.getProgress(), mSeekChorusWet.getProgress(), progress, mSeekChorusMinSweep.getProgress(), mSeekChorusMaxSweep.getProgress(), mSeekChorusRate.getProgress(), fromTouch);
        else if (seekBar.getId() == R.id.seekChorusMinSweep)
            setChorus(mSeekChorusDry.getProgress(), mSeekChorusWet.getProgress(), mSeekChorusFeedback.getProgress(), progress, mSeekChorusMaxSweep.getProgress(), mSeekChorusRate.getProgress(), fromTouch);
        else if (seekBar.getId() == R.id.seekChorusMaxSweep)
            setChorus(mSeekChorusDry.getProgress(), mSeekChorusWet.getProgress(), mSeekChorusFeedback.getProgress(), mSeekChorusMinSweep.getProgress(), progress, mSeekChorusRate.getProgress(), fromTouch);
        else if (seekBar.getId() == R.id.seekChorusRate)
            setChorus(mSeekChorusDry.getProgress(), mSeekChorusWet.getProgress(), mSeekChorusFeedback.getProgress(), mSeekChorusMinSweep.getProgress(), mSeekChorusMaxSweep.getProgress(), progress, fromTouch);
        else if (seekBar.getId() == R.id.seekDistortionDrive)
            setDistortion(progress, mSeekDistortionDry.getProgress(), mSeekDistortionWet.getProgress(), mSeekDistortionFeedback.getProgress(), mSeekDistortionVolume.getProgress(), fromTouch);
        else if (seekBar.getId() == R.id.seekDistortionDry)
            setDistortion(mSeekDistortionDrive.getProgress(), progress, mSeekDistortionWet.getProgress(), mSeekDistortionFeedback.getProgress(), mSeekDistortionVolume.getProgress(), fromTouch);
        else if (seekBar.getId() == R.id.seekDistortionWet)
            setDistortion(mSeekDistortionDrive.getProgress(), mSeekDistortionDry.getProgress(), progress, mSeekDistortionFeedback.getProgress(), mSeekDistortionVolume.getProgress(), fromTouch);
        else if (seekBar.getId() == R.id.seekDistortionFeedback)
            setDistortion(mSeekDistortionDrive.getProgress(), mSeekDistortionDry.getProgress(), mSeekDistortionWet.getProgress(), progress, mSeekDistortionVolume.getProgress(), fromTouch);
        else if (seekBar.getId() == R.id.seekDistortionVolume)
            setDistortion(mSeekDistortionDrive.getProgress(), mSeekDistortionDry.getProgress(), mSeekDistortionWet.getProgress(), mSeekDistortionFeedback.getProgress(), progress, fromTouch);
        else if (seekBar.getId() == R.id.seekSoundEffectVolume)
            setSoundEffect(progress, fromTouch);
    }

    private static void updateComp() {
        if (!sEffectItems.get(EFFECTTYPE_COMP).isSelected() || MainActivity.sStream == 0)
            return;
        if (sFxComp == 0)
            sFxComp = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_COMPRESSOR2, 2);
        BASS_FX.BASS_BFX_COMPRESSOR2 p = new BASS_FX.BASS_BFX_COMPRESSOR2();
        p.fGain = sCompGain;
        p.fThreshold = sCompThreshold;
        p.fRatio = sCompRatio;
        p.fAttack = sCompAttack;
        p.fRelease = sCompRelease;
        p.lChannel = BASS_FX.BASS_BFX_CHANALL;
        BASS.BASS_FXSetParameters(sFxComp, p);
    }

    public static void setComp(int nGain, int nThreshold, int nRatio, int nAttack, int nRelease, boolean bSave) {
        if (nGain < 0) nGain = 0;
        if (nThreshold < 0) nThreshold = 0;
        if (nRatio < 0) nRatio = 0;
        if (nAttack < 0) nAttack = 0;
        if (nRelease < 0) nRelease = 0;

        if (nGain > COMP_GAIN_MAX) nGain = COMP_GAIN_MAX;
        if (nThreshold > COMP_THRESHOLD_MAX) nThreshold = COMP_THRESHOLD_MAX;
        if (nRatio > COMP_RATIO_MAX) nRatio = COMP_RATIO_MAX;
        if (nAttack > COMP_ATTACK_MAX) nAttack = COMP_ATTACK_MAX;
        if (nRelease > COMP_RELEASE_MAX) nRelease = COMP_RELEASE_MAX;

        sCompGain = nGain / 100.0f;
        sCompThreshold = (nThreshold - 6000) / 100.0f;
        sCompRatio = (nRatio + 100) / 100.0f;
        sCompAttack = (nAttack + 1) / 100.0f;
        sCompRelease = (nRelease + 1) / 100.0f;

        if(sActivity != null) {
            sActivity.effectFragment.getTextCompGain().setText(String.format(Locale.getDefault(), "%.2f", sCompGain));
            sActivity.effectFragment.getTextCompThreshold().setText(String.format(Locale.getDefault(), "%.2f", sCompThreshold));
            sActivity.effectFragment.getTextCompRatio().setText(String.format(Locale.getDefault(), "%.2f", sCompRatio));
            sActivity.effectFragment.getTextCompAttack().setText(String.format(Locale.getDefault(), "%.2f", sCompAttack));
            sActivity.effectFragment.getTextCompRelease().setText(String.format(Locale.getDefault(), "%.2f", sCompRelease));

            sActivity.effectFragment.getSeekCompGain().setProgress(nGain);
            sActivity.effectFragment.getSeekCompThreshold().setProgress(nThreshold);
            sActivity.effectFragment.getSeekCompRatio().setProgress(nRatio);
            sActivity.effectFragment.getSeekCompAttack().setProgress(nAttack);
            sActivity.effectFragment.getSeekCompRelease().setProgress(nRelease);
        }

        updateComp();
        if (bSave) PlaylistFragment.updateSavingEffect();
    }

    public static void setComp(float fGain, float fThreshold, float fRatio, float fAttack, float fRelease, boolean bSave) {
        sCompGain = fGain;
        sCompThreshold = fThreshold;
        sCompRatio = fRatio;
        sCompAttack = fAttack;
        sCompRelease = fRelease;

        if(sActivity != null) {
            sActivity.effectFragment.getTextCompGain().setText(String.format(Locale.getDefault(), "%.2f", sCompGain));
            sActivity.effectFragment.getTextCompThreshold().setText(String.format(Locale.getDefault(), "%.2f", sCompThreshold));
            sActivity.effectFragment.getTextCompRatio().setText(String.format(Locale.getDefault(), "%.2f", sCompRatio));
            sActivity.effectFragment.getTextCompAttack().setText(String.format(Locale.getDefault(), "%.2f", sCompAttack));
            sActivity.effectFragment.getTextCompRelease().setText(String.format(Locale.getDefault(), "%.2f", sCompRelease));

            sActivity.effectFragment.getSeekCompGain().setProgress((int) (fGain * 100.0f));
            sActivity.effectFragment.getSeekCompThreshold().setProgress((int) (fThreshold * 100.0f) + 6000);
            sActivity.effectFragment.getSeekCompRatio().setProgress((int) (fRatio * 100.0f) - 100);
            sActivity.effectFragment.getSeekCompAttack().setProgress((int) (fAttack * 100.0f) - 1);
            sActivity.effectFragment.getSeekCompRelease().setProgress((int) (fRelease * 100.0f) - 1);
        }

        updateComp();
        if (bSave) PlaylistFragment.updateSavingEffect();
    }

    private void minusCompGain() {
        setComp(mSeekCompGain.getProgress() - 1, mSeekCompThreshold.getProgress(), mSeekCompRatio.getProgress(), mSeekCompAttack.getProgress(), mSeekCompRelease.getProgress(), true);
    }

    private void plusCompGain() {
        setComp(mSeekCompGain.getProgress() + 1, mSeekCompThreshold.getProgress(), mSeekCompRatio.getProgress(), mSeekCompAttack.getProgress(), mSeekCompRelease.getProgress(), true);
    }

    private void minusCompThreshold() {
        setComp(mSeekCompGain.getProgress(), mSeekCompThreshold.getProgress() - 1, mSeekCompRatio.getProgress(), mSeekCompAttack.getProgress(), mSeekCompRelease.getProgress(), true);
    }

    private void plusCompThreshold() {
        setComp(mSeekCompGain.getProgress(), mSeekCompThreshold.getProgress() + 1, mSeekCompRatio.getProgress(), mSeekCompAttack.getProgress(), mSeekCompRelease.getProgress(), true);
    }

    private void minusCompRatio() {
        setComp(mSeekCompGain.getProgress(), mSeekCompThreshold.getProgress(), mSeekCompRatio.getProgress() - 1, mSeekCompAttack.getProgress(), mSeekCompRelease.getProgress(), true);
    }

    private void plusCompRatio() {
        setComp(mSeekCompGain.getProgress(), mSeekCompThreshold.getProgress(), mSeekCompRatio.getProgress() + 1, mSeekCompAttack.getProgress(), mSeekCompRelease.getProgress(), true);
    }

    private void minusCompAttack() {
        setComp(mSeekCompGain.getProgress(), mSeekCompThreshold.getProgress(), mSeekCompRatio.getProgress(), mSeekCompAttack.getProgress() - 1, mSeekCompRelease.getProgress(), true);
    }

    private void plusCompAttack() {
        setComp(mSeekCompGain.getProgress(), mSeekCompThreshold.getProgress(), mSeekCompRatio.getProgress(), mSeekCompAttack.getProgress() + 1, mSeekCompRelease.getProgress(), true);
    }

    private void minusCompRelease() {
        setComp(mSeekCompGain.getProgress(), mSeekCompThreshold.getProgress(), mSeekCompRatio.getProgress(), mSeekCompAttack.getProgress(), mSeekCompRelease.getProgress() - 1, true);
    }

    private void plusCompRelease() {
        setComp(mSeekCompGain.getProgress(), mSeekCompThreshold.getProgress(), mSeekCompRatio.getProgress(), mSeekCompAttack.getProgress(), mSeekCompRelease.getProgress() + 1, true);
    }

    private static void updatePan() {
        if (!sEffectItems.get(EFFECTTYPE_PAN).isSelected() || MainActivity.sStream == 0)
            return;
        if (sDspPan == 0)
            sDspPan = BASS.BASS_ChannelSetDSP(MainActivity.sStream, panDSP, null, 0);
    }

    public static void setPan(int nValue, boolean bSave) {
        if (nValue < 0) nValue = 0;
        if (nValue > PAN_VALUE_MAX) nValue = PAN_VALUE_MAX;

        sPan = (nValue - 100) / 100f;

        if(sActivity != null) {
            if (sPan == 0f)
                sActivity.effectFragment.getTextPanValue().setText(String.format(Locale.getDefault(), "%.0f%%", sPan * 100f));
            else if (sPan < 0f)
                sActivity.effectFragment.getTextPanValue().setText(String.format(Locale.getDefault(), "L%.0f%%", sPan * -100f));
            else
                sActivity.effectFragment.getTextPanValue().setText(String.format(Locale.getDefault(), "R%.0f%%", sPan * 100f));

            sActivity.effectFragment.getSeekPanValue().setProgress(nValue);
        }

        updatePan();
        if (bSave) PlaylistFragment.updateSavingEffect();
    }

    public static void setPan(float fValue, boolean bSave) {
        sPan = fValue;

        if(sActivity != null) {
            if (sPan == 0f)
                sActivity.effectFragment.getTextPanValue().setText(String.format(Locale.getDefault(), "%.0f%%", sPan * 100f));
            else if (sPan < 0f)
                sActivity.effectFragment.getTextPanValue().setText(String.format(Locale.getDefault(), "L%.0f%%", sPan * -100f));
            else
                sActivity.effectFragment.getTextPanValue().setText(String.format(Locale.getDefault(), "R%.0f%%", sPan * 100f));

            sActivity.effectFragment.getSeekPanValue().setProgress((int)(fValue * 100f + 100));
        }

        updatePan();
        if (bSave) PlaylistFragment.updateSavingEffect();
    }

    private void minusPanValue() {
        setPan(mSeekPanValue.getProgress() - 1, true);
    }

    private void plusPanValue() {
        setPan(mSeekPanValue.getProgress() + 1, true);
    }

    private static void updateFreq() {
        if (!sEffectItems.get(EFFECTTYPE_FREQ).isSelected() || MainActivity.sStream == 0)
            return;
        BASS.BASS_CHANNELINFO info = new BASS.BASS_CHANNELINFO();
        BASS.BASS_ChannelGetInfo(MainActivity.sStream, info);
        BASS.BASS_ChannelSetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, info.freq * sFreq);
    }

    public static void setFreq(int nValue, boolean bSave) {
        if (nValue < 0) nValue = 0;
        if (nValue > FREQ_VALUE_MAX) nValue = FREQ_VALUE_MAX;

        sFreq = (nValue + 10) / 100f;

        if(sActivity != null) {
            sActivity.effectFragment.getTextFreqValue().setText(String.format(Locale.getDefault(), "%.0f", sFreq * 100f));

            sActivity.effectFragment.getSeekFreqValue().setProgress(nValue);
        }

        updateFreq();
        if (bSave) PlaylistFragment.updateSavingEffect();
    }

    public static void setFreq(float fValue, boolean bSave) {
        sFreq = fValue;

        if(sActivity != null) {
            sActivity.effectFragment.getTextFreqValue().setText(String.format(Locale.getDefault(), "%.0f", sFreq * 100f));

            sActivity.effectFragment.getSeekFreqValue().setProgress((int)(fValue * 100f - 10));
        }

        updateFreq();
        if (bSave) PlaylistFragment.updateSavingEffect();
    }

    private void minusFreqValue() {
        setFreq(mSeekFreqValue.getProgress() - 1, true);
    }

    private void plusFreqValue() {
        setFreq(mSeekFreqValue.getProgress() + 1, true);
    }

    private static void updateMetronome() {
        if (!sEffectItems.get(EFFECTTYPE_METRONOME).isSelected()) return;
        sMetronome.setBpm(sBpm);
        if (!sMetronome.isPlaying()) sMetronome.play();
    }

    private static void setMetronome(int nValue, boolean bSave) {
        if (nValue < 0) nValue = 0;
        if (nValue > METRONOME_VALUE_MAX) nValue = METRONOME_VALUE_MAX;

        sBpm = nValue + 10;

        if(sActivity != null) {
            sActivity.effectFragment.getTextMetronomeValue().setText(String.format(Locale.getDefault(), "%d", sBpm));

            sActivity.effectFragment.getSeekMetronomeValue().setProgress(nValue);
        }

        updateMetronome();
        if (bSave) PlaylistFragment.updateSavingEffect();
    }

    public static void setMetronome(float fValue, boolean bSave) {
        sBpm = (int)fValue;

        if(sActivity != null) {
            sActivity.effectFragment.getTextMetronomeValue().setText(String.format(Locale.getDefault(), "%d", sBpm));

            sActivity.effectFragment.getSeekMetronomeValue().setProgress(sBpm - 10);
        }

        updateMetronome();
        if (bSave) PlaylistFragment.updateSavingEffect();
    }

    private void minusMetronomeValue() {
        setMetronome(mSeekMetronomeValue.getProgress() - 1, true);
    }

    private void plusMetronomeValue() {
        setMetronome(mSeekMetronomeValue.getProgress() + 1, true);
    }

    private static void updateEcho() {
        if (!sEffectItems.get(EFFECTTYPE_ECHO).isSelected() || MainActivity.sStream == 0)
            return;
        if (sFxEcho == 0)
            sFxEcho = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_ECHO4, 2);
        BASS_FX.BASS_BFX_ECHO4 echo = new BASS_FX.BASS_BFX_ECHO4();
        echo.fDryMix = sEchoDry;
        echo.fWetMix = sEchoWet;
        echo.fFeedback = sEchoFeedback;
        echo.fDelay = sEchoDelay;
        echo.bStereo = TRUE;
        echo.lChannel = BASS_FX.BASS_BFX_CHANALL;
        BASS.BASS_FXSetParameters(sFxEcho, echo);
    }

    public static void setEcho(int nDry, int nWet, int nFeedback, int nDelay, boolean bSave) {
        if (nDry < 0) nDry = 0;
        if (nWet < 0) nWet = 0;
        if (nFeedback < 0) nFeedback = 0;
        if (nDelay < 0) nDelay = 0;

        if (nDry > ECHO_DRY_MAX) nDry = ECHO_DRY_MAX;
        if (nWet > ECHO_WET_MAX) nWet = ECHO_WET_MAX;
        if (nFeedback > ECHO_FEEDBACK_MAX) nFeedback = ECHO_FEEDBACK_MAX;
        if (nDelay > ECHO_DELAY_MAX) nDelay = ECHO_DELAY_MAX;

        sEchoDry = nDry / 100.0f;
        sEchoWet = nWet / 100.0f;
        sEchoFeedback = nFeedback / 100.0f;
        sEchoDelay = (nDelay + 1) / 100.0f;

        if(sActivity != null) {
            sActivity.effectFragment.getTextEchoDry().setText(String.format(Locale.getDefault(), "%.2f", sEchoDry));
            sActivity.effectFragment.getTextEchoWet().setText(String.format(Locale.getDefault(), "%.2f", sEchoWet));
            sActivity.effectFragment.getTextEchoFeedback().setText(String.format(Locale.getDefault(), "%.2f", sEchoFeedback));
            sActivity.effectFragment.getTextEchoDelay().setText(String.format(Locale.getDefault(), "%.2f", sEchoDelay));

            sActivity.effectFragment.getSeekEchoDry().setProgress(nDry);
            sActivity.effectFragment.getSeekEchoWet().setProgress(nWet);
            sActivity.effectFragment.getSeekEchoFeedback().setProgress(nFeedback);
            sActivity.effectFragment.getSeekEchoDelay().setProgress(nDelay);
        }

        updateEcho();
        if (bSave) PlaylistFragment.updateSavingEffect();
    }

    public static void setEcho(float fDry, float fWet, float fFeedback, float fDelay, boolean bSave) {
        sEchoDry = fDry;
        sEchoWet = fWet;
        sEchoFeedback = fFeedback;
        sEchoDelay = fDelay;

        if(sActivity != null) {
            sActivity.effectFragment.getTextEchoDry().setText(String.format(Locale.getDefault(), "%.2f", sEchoDry));
            sActivity.effectFragment.getTextEchoWet().setText(String.format(Locale.getDefault(), "%.2f", sEchoWet));
            sActivity.effectFragment.getTextEchoFeedback().setText(String.format(Locale.getDefault(), "%.2f", sEchoFeedback));
            sActivity.effectFragment.getTextEchoDelay().setText(String.format(Locale.getDefault(), "%.2f", sEchoDelay));

            sActivity.effectFragment.getSeekEchoDry().setProgress((int) (fDry * 100.0f));
            sActivity.effectFragment.getSeekEchoWet().setProgress((int) (fWet * 100.0f));
            sActivity.effectFragment.getSeekEchoFeedback().setProgress((int) (fFeedback * 100.0f));
            sActivity.effectFragment.getSeekEchoDelay().setProgress((int) (fDelay * 100.0f) - 1);
        }

        updateEcho();
        if (bSave) PlaylistFragment.updateSavingEffect();
    }

    private void minusEchoDry() {
        setEcho(mSeekEchoDry.getProgress() - 1, mSeekEchoWet.getProgress(), mSeekEchoFeedback.getProgress(), mSeekEchoDelay.getProgress(), true);
    }

    private void plusEchoDry() {
        setEcho(mSeekEchoDry.getProgress() + 1, mSeekEchoWet.getProgress(), mSeekEchoFeedback.getProgress(), mSeekEchoDelay.getProgress(), true);
    }

    private void minusEchoWet() {
        setEcho(mSeekEchoDry.getProgress(), mSeekEchoWet.getProgress() - 1, mSeekEchoFeedback.getProgress(), mSeekEchoDelay.getProgress(), true);
    }

    private void plusEchoWet() {
        setEcho(mSeekEchoDry.getProgress(), mSeekEchoWet.getProgress() + 1, mSeekEchoFeedback.getProgress(), mSeekEchoDelay.getProgress(), true);
    }

    private void minusEchoFeedback() {
        setEcho(mSeekEchoDry.getProgress(), mSeekEchoWet.getProgress(), mSeekEchoFeedback.getProgress() - 1, mSeekEchoDelay.getProgress(), true);
    }

    private void plusEchoFeedback() {
        setEcho(mSeekEchoDry.getProgress(), mSeekEchoWet.getProgress(), mSeekEchoFeedback.getProgress() + 1, mSeekEchoDelay.getProgress(), true);
    }

    private void minusEchoDelay() {
        setEcho(mSeekEchoDry.getProgress(), mSeekEchoWet.getProgress(), mSeekEchoFeedback.getProgress(), mSeekEchoDelay.getProgress() - 1, true);
    }

    private void plusEchoDelay() {
        setEcho(mSeekEchoDry.getProgress(), mSeekEchoWet.getProgress(), mSeekEchoFeedback.getProgress(), mSeekEchoDelay.getProgress() + 1, true);
    }

    private static void updateReverb() {
        if (!sEffectItems.get(EFFECTTYPE_REVERB).isSelected() || MainActivity.sStream == 0)
            return;
        if (sFxReverb == 0)
            sFxReverb = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_FREEVERB, 2);
        BASS_FX.BASS_BFX_FREEVERB reverb = new BASS_FX.BASS_BFX_FREEVERB();
        reverb.fDryMix = sReverbDry;
        reverb.fWetMix = sReverbWet;
        reverb.fRoomSize = sReverbRoomSize;
        reverb.fDamp = sReverbDamp;
        reverb.fWidth = sReverbWidth;
        reverb.lMode = 0;
        reverb.lChannel = BASS_FX.BASS_BFX_CHANALL;
        BASS.BASS_FXSetParameters(sFxReverb, reverb);
    }

    public static void setReverb(int nDry, int nWet, int nRoomSize, int nDamp, int nWidth, boolean bSave) {
        if (nDry < 0) nDry = 0;
        if (nWet < 0) nWet = 0;
        if (nRoomSize < 0) nRoomSize = 0;
        if (nDamp < 0) nDamp = 0;
        if (nWidth < 0) nWidth = 0;

        if (nDry > REVERB_DRY_MAX) nDry = REVERB_DRY_MAX;
        if (nWet > REVERB_WET_MAX) nWet = REVERB_WET_MAX;
        if (nRoomSize > REVERB_ROOMSIZE_MAX) nRoomSize = REVERB_ROOMSIZE_MAX;
        if (nDamp > REVERB_DAMP_MAX) nDamp = REVERB_DAMP_MAX;
        if (nWidth > REVERB_WIDTH_MAX) nWidth = REVERB_WIDTH_MAX;

        sReverbDry = nDry / 100.0f;
        sReverbWet = nWet / 100.0f;
        sReverbRoomSize = nRoomSize / 100.0f;
        sReverbDamp = nDamp / 100.0f;
        sReverbWidth = nWidth / 100.0f;

        if(sActivity != null) {
            sActivity.effectFragment.getTextReverbDry().setText(String.format(Locale.getDefault(), "%.2f", sReverbDry));
            sActivity.effectFragment.getTextReverbWet().setText(String.format(Locale.getDefault(), "%.2f", sReverbWet));
            sActivity.effectFragment.getTextReverbRoomSize().setText(String.format(Locale.getDefault(), "%.2f", sReverbRoomSize));
            sActivity.effectFragment.getTextReverbDamp().setText(String.format(Locale.getDefault(), "%.2f", sReverbDamp));
            sActivity.effectFragment.getTextReverbWidth().setText(String.format(Locale.getDefault(), "%.2f", sReverbWidth));

            sActivity.effectFragment.getSeekReverbDry().setProgress(nDry);
            sActivity.effectFragment.getSeekReverbWet().setProgress(nWet);
            sActivity.effectFragment.getSeekReverbRoomSize().setProgress(nRoomSize);
            sActivity.effectFragment.getSeekReverbDamp().setProgress(nDamp);
            sActivity.effectFragment.getSeekReverbWidth().setProgress(nWidth);
        }

        updateReverb();
        if (bSave) PlaylistFragment.updateSavingEffect();
    }

    public static void setReverb(float fDry, float fWet, float fRoomSize, float fDamp, float fWidth, boolean bSave) {
        sReverbDry = fDry;
        sReverbWet = fWet;
        sReverbRoomSize = fRoomSize;
        sReverbDamp = fDamp;
        sReverbWidth = fWidth;

        if(sActivity != null) {
            sActivity.effectFragment.getTextReverbDry().setText(String.format(Locale.getDefault(), "%.2f", sReverbDry));
            sActivity.effectFragment.getTextReverbWet().setText(String.format(Locale.getDefault(), "%.2f", sReverbWet));
            sActivity.effectFragment.getTextReverbRoomSize().setText(String.format(Locale.getDefault(), "%.2f", sReverbRoomSize));
            sActivity.effectFragment.getTextReverbDamp().setText(String.format(Locale.getDefault(), "%.2f", sReverbDamp));
            sActivity.effectFragment.getTextReverbWidth().setText(String.format(Locale.getDefault(), "%.2f", sReverbWidth));

            sActivity.effectFragment.getSeekReverbDry().setProgress((int) (fDry * 100.0f));
            sActivity.effectFragment.getSeekReverbWet().setProgress((int) (fWet * 100.0f));
            sActivity.effectFragment.getSeekReverbRoomSize().setProgress((int) (fRoomSize * 100.0f));
            sActivity.effectFragment.getSeekReverbDamp().setProgress((int) (fDamp * 100.0f));
            sActivity.effectFragment.getSeekReverbWidth().setProgress((int) (fWidth * 100.0f));
        }

        updateReverb();
        if (bSave) PlaylistFragment.updateSavingEffect();
    }

    private void minusReverbDry() {
        setReverb(mSeekReverbDry.getProgress() - 1, mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress(), true);
    }

    private void plusReverbDry() {
        setReverb(mSeekReverbDry.getProgress() + 1, mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress(), true);
    }

    private void minusReverbWet() {
        setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress() - 1, mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress(), true);
    }

    private void plusReverbWet() {
        setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress() + 1, mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress(), true);
    }

    private void minusReverbRoomSize() {
        setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress() - 1, mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress(), true);
    }

    private void plusReverbRoomSize() {
        setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress() + 1, mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress(), true);
    }

    private void minusReverbDamp() {
        setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress() - 1, mSeekReverbWidth.getProgress(), true);
    }

    private void plusReverbDamp() {
        setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress() + 1, mSeekReverbWidth.getProgress(), true);
    }

    private void minusReverbWidth() {
        setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress() - 1, true);
    }

    private void plusReverbWidth() {
        setReverb(mSeekReverbDry.getProgress(), mSeekReverbWet.getProgress(), mSeekReverbRoomSize.getProgress(), mSeekReverbDamp.getProgress(), mSeekReverbWidth.getProgress() + 1, true);
    }

    private static void updateChorus() {
        if (!sEffectItems.get(EFFECTTYPE_CHORUS).isSelected() || MainActivity.sStream == 0)
            return;
        if (sFxChorus == 0)
            sFxChorus = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_CHORUS, 2);
        BASS_FX.BASS_BFX_CHORUS chorus = new BASS_FX.BASS_BFX_CHORUS();
        chorus.fDryMix = sChorusDry;
        chorus.fWetMix = sChorusWet;
        chorus.fFeedback = sChorusFeedback;
        chorus.fMinSweep = sChorusMinSweep;
        chorus.fMaxSweep = sChorusMaxSweep;
        chorus.fRate = sChorusRate;
        chorus.lChannel = BASS_FX.BASS_BFX_CHANALL;
        BASS.BASS_FXSetParameters(sFxChorus, chorus);
    }

    public static void setChorus(int nDry, int nWet, int nFeedback, int nMinSweep, int nMaxSweep, int nRate, boolean bSave) {
        if (nDry < 0) nDry = 0;
        if (nWet < 0) nWet = 0;
        if (nFeedback < 0) nFeedback = 0;
        if (nMinSweep < 0) nMinSweep = 0;
        if (nMaxSweep < 0) nMaxSweep = 0;
        if (nRate < 0) nRate = 0;

        if (nDry > CHORUS_DRY_MAX) nDry = CHORUS_DRY_MAX;
        if (nWet > CHORUS_WET_MAX) nWet = CHORUS_WET_MAX;
        if (nFeedback > CHORUS_FEEDBACK_MAX) nFeedback = CHORUS_FEEDBACK_MAX;
        if (nMinSweep > CHORUS_MINSWEEP_MAX) nMinSweep = CHORUS_MINSWEEP_MAX;
        if (nMaxSweep > CHORUS_MAXSWEEP_MAX) nMaxSweep = CHORUS_MAXSWEEP_MAX;
        if (nRate > CHORUS_RATE_MAX) nRate = CHORUS_RATE_MAX;

        sChorusDry = nDry / 100.0f;
        sChorusWet = nWet / 100.0f;
        sChorusFeedback = nFeedback / 100.0f;
        sChorusMinSweep = nMinSweep / 100.0f;
        sChorusMaxSweep = nMaxSweep / 100.0f;
        sChorusRate = nRate / 100.0f;

        if(sActivity != null) {
            sActivity.effectFragment.getTextChorusDry().setText(String.format(Locale.getDefault(), "%.2f", sChorusDry));
            sActivity.effectFragment.getTextChorusWet().setText(String.format(Locale.getDefault(), "%.2f", sChorusWet));
            sActivity.effectFragment.getTextChorusFeedback().setText(String.format(Locale.getDefault(), "%.2f", sChorusFeedback));
            sActivity.effectFragment.getTextChorusMinSweep().setText(String.format(Locale.getDefault(), "%.2f", sChorusMinSweep));
            sActivity.effectFragment.getTextChorusMaxSweep().setText(String.format(Locale.getDefault(), "%.2f", sChorusMaxSweep));
            sActivity.effectFragment.getTextChorusRate().setText(String.format(Locale.getDefault(), "%.2f", sChorusRate));

            sActivity.effectFragment.getSeekChorusDry().setProgress(nDry);
            sActivity.effectFragment.getSeekChorusWet().setProgress(nWet);
            sActivity.effectFragment.getSeekChorusFeedback().setProgress(nFeedback);
            sActivity.effectFragment.getSeekChorusMinSweep().setProgress(nMinSweep);
            sActivity.effectFragment.getSeekChorusMaxSweep().setProgress(nMaxSweep);
            sActivity.effectFragment.getSeekChorusRate().setProgress(nRate);
        }

        updateChorus();
        if (bSave) PlaylistFragment.updateSavingEffect();
    }

    public static void setChorus(float fDry, float fWet, float fFeedback, float fMinSweep, float fMaxSweep, float fRate, boolean bSave) {
        sChorusDry = fDry;
        sChorusWet = fWet;
        sChorusFeedback = fFeedback;
        sChorusMinSweep = fMinSweep;
        sChorusMaxSweep = fMaxSweep;
        sChorusRate = fRate;

        if (sActivity != null) {
            sActivity.effectFragment.getTextChorusDry().setText(String.format(Locale.getDefault(), "%.2f", sChorusDry));
            sActivity.effectFragment.getTextChorusWet().setText(String.format(Locale.getDefault(), "%.2f", sChorusWet));
            sActivity.effectFragment.getTextChorusFeedback().setText(String.format(Locale.getDefault(), "%.2f", sChorusFeedback));
            sActivity.effectFragment.getTextChorusMinSweep().setText(String.format(Locale.getDefault(), "%.2f", sChorusMinSweep));
            sActivity.effectFragment.getTextChorusMaxSweep().setText(String.format(Locale.getDefault(), "%.2f", sChorusMaxSweep));
            sActivity.effectFragment.getTextChorusRate().setText(String.format(Locale.getDefault(), "%.2f", sChorusRate));

            sActivity.effectFragment.getSeekChorusDry().setProgress((int) (fDry * 100.0f));
            sActivity.effectFragment.getSeekChorusWet().setProgress((int) (fWet * 100.0f));
            sActivity.effectFragment.getSeekChorusFeedback().setProgress((int) (fFeedback * 100.0f));
            sActivity.effectFragment.getSeekChorusMinSweep().setProgress((int) (fMinSweep * 100.0f));
            sActivity.effectFragment.getSeekChorusMaxSweep().setProgress((int) (fMaxSweep * 100.0f));
            sActivity.effectFragment.getSeekChorusRate().setProgress((int) (fRate * 100.0f));
        }

        updateChorus();
        if (bSave) PlaylistFragment.updateSavingEffect();
    }

    private void minusChorusDry() {
        setChorus(mSeekChorusDry.getProgress() - 1, mSeekChorusWet.getProgress(), mSeekChorusFeedback.getProgress(), mSeekChorusMinSweep.getProgress(), mSeekChorusMaxSweep.getProgress(), mSeekChorusRate.getProgress(), true);
    }

    private void plusChorusDry() {
        setChorus(mSeekChorusDry.getProgress() + 1, mSeekChorusWet.getProgress(), mSeekChorusFeedback.getProgress(), mSeekChorusMinSweep.getProgress(), mSeekChorusMaxSweep.getProgress(), mSeekChorusRate.getProgress(), true);
    }

    private void minusChorusWet() {
        setChorus(mSeekChorusDry.getProgress(), mSeekChorusWet.getProgress() - 1, mSeekChorusFeedback.getProgress(), mSeekChorusMinSweep.getProgress(), mSeekChorusMaxSweep.getProgress(), mSeekChorusRate.getProgress(), true);
    }

    private void plusChorusWet() {
        setChorus(mSeekChorusDry.getProgress(), mSeekChorusWet.getProgress() + 1, mSeekChorusFeedback.getProgress(), mSeekChorusMinSweep.getProgress(), mSeekChorusMaxSweep.getProgress(), mSeekChorusRate.getProgress(), true);
    }

    private void minusChorusFeedback() {
        setChorus(mSeekChorusDry.getProgress(), mSeekChorusWet.getProgress(), mSeekChorusFeedback.getProgress() - 1, mSeekChorusMinSweep.getProgress(), mSeekChorusMaxSweep.getProgress(), mSeekChorusRate.getProgress(), true);
    }

    private void plusChorusFeedback() {
        setChorus(mSeekChorusDry.getProgress(), mSeekChorusWet.getProgress(), mSeekChorusFeedback.getProgress() + 1, mSeekChorusMinSweep.getProgress(), mSeekChorusMaxSweep.getProgress(), mSeekChorusRate.getProgress(), true);
    }

    private void minusChorusMinSweep() {
        setChorus(mSeekChorusDry.getProgress(), mSeekChorusWet.getProgress(), mSeekChorusFeedback.getProgress(), mSeekChorusMinSweep.getProgress() - 1, mSeekChorusMaxSweep.getProgress(), mSeekChorusRate.getProgress(), true);
    }

    private void plusChorusMinSweep() {
        setChorus(mSeekChorusDry.getProgress(), mSeekChorusWet.getProgress(), mSeekChorusFeedback.getProgress(), mSeekChorusMinSweep.getProgress() + 1, mSeekChorusMaxSweep.getProgress(), mSeekChorusRate.getProgress(), true);
    }

    private void minusChorusMaxSweep() {
        setChorus(mSeekChorusDry.getProgress(), mSeekChorusWet.getProgress(), mSeekChorusFeedback.getProgress(), mSeekChorusMinSweep.getProgress(), mSeekChorusMaxSweep.getProgress() - 1, mSeekChorusRate.getProgress(), true);
    }

    private void plusChorusMaxSweep() {
        setChorus(mSeekChorusDry.getProgress(), mSeekChorusWet.getProgress(), mSeekChorusFeedback.getProgress(), mSeekChorusMinSweep.getProgress(), mSeekChorusMaxSweep.getProgress() + 1, mSeekChorusRate.getProgress(), true);
    }

    private void minusChorusRate() {
        setChorus(mSeekChorusDry.getProgress(), mSeekChorusWet.getProgress(), mSeekChorusFeedback.getProgress(), mSeekChorusMinSweep.getProgress(), mSeekChorusMaxSweep.getProgress(), mSeekChorusRate.getProgress() - 1, true);
    }

    private void plusChorusRate() {
        setChorus(mSeekChorusDry.getProgress(), mSeekChorusWet.getProgress(), mSeekChorusFeedback.getProgress(), mSeekChorusMinSweep.getProgress(), mSeekChorusMaxSweep.getProgress(), mSeekChorusRate.getProgress() + 1, true);
    }

    private static void updateDistortion() {
        if (!sEffectItems.get(EFFECTTYPE_DISTORTION).isSelected() || MainActivity.sStream == 0)
            return;
        if (sFxDistortion == 0)
            sFxDistortion = BASS.BASS_ChannelSetFX(MainActivity.sStream, BASS_FX.BASS_FX_BFX_DISTORTION, 2);
        BASS_FX.BASS_BFX_DISTORTION distortion = new BASS_FX.BASS_BFX_DISTORTION();
        distortion.fDrive = sDistortionDrive;
        distortion.fDryMix = sDistortionDry;
        distortion.fWetMix = sDistortionWet;
        distortion.fFeedback = sDistortionFeedback;
        distortion.fVolume = sDistortionVolume;
        distortion.lChannel = BASS_FX.BASS_BFX_CHANALL;
        BASS.BASS_FXSetParameters(sFxDistortion, distortion);
    }

    public static void setDistortion(int nDrive, int nDry, int nWet, int nFeedback, int nVolume, boolean bSave) {
        if (nDrive < 0) nDrive = 0;
        if (nDry < 0) nDry = 0;
        if (nWet < 0) nWet = 0;
        if (nFeedback < 0) nFeedback = 0;
        if (nVolume < 0) nVolume = 0;

        if (nDrive > DISTORTION_DRIVE_MAX) nDrive = DISTORTION_DRIVE_MAX;
        if (nDry > DISTORTION_DRY_MAX) nDry = DISTORTION_DRY_MAX;
        if (nWet > DISTORTION_WET_MAX) nWet = DISTORTION_WET_MAX;
        if (nFeedback > DISTORTION_FEEDBACK_MAX) nFeedback = DISTORTION_FEEDBACK_MAX;
        if (nVolume > DISTORTION_VOLUME_MAX) nVolume = DISTORTION_VOLUME_MAX;

        sDistortionDrive = nDrive / 100.0f;
        sDistortionDry = nDry / 100.0f;
        sDistortionWet = nWet / 100.0f;
        sDistortionFeedback = nFeedback / 100.0f;
        sDistortionVolume = nVolume / 100.0f;

        if(sActivity != null) {
            sActivity.effectFragment.getTextDistortionDrive().setText(String.format(Locale.getDefault(), "%.2f", sDistortionDrive));
            sActivity.effectFragment.getTextDistortionDry().setText(String.format(Locale.getDefault(), "%.2f", sDistortionDry));
            sActivity.effectFragment.getTextDistortionWet().setText(String.format(Locale.getDefault(), "%.2f", sDistortionWet));
            sActivity.effectFragment.getTextDistortionFeedback().setText(String.format(Locale.getDefault(), "%.2f", sDistortionFeedback));
            sActivity.effectFragment.getTextDistortionVolume().setText(String.format(Locale.getDefault(), "%.2f", sDistortionVolume));

            sActivity.effectFragment.getSeekDistortionDrive().setProgress(nDrive);
            sActivity.effectFragment.getSeekDistortionDry().setProgress(nDry);
            sActivity.effectFragment.getSeekDistortionWet().setProgress(nWet);
            sActivity.effectFragment.getSeekDistortionFeedback().setProgress(nFeedback);
            sActivity.effectFragment.getSeekDistortionVolume().setProgress(nVolume);
        }

        updateDistortion();
        if (bSave) PlaylistFragment.updateSavingEffect();
    }

    public static void setDistortion(float fDrive, float fDry, float fWet, float fFeedback, float fVolume, boolean bSave) {
        sDistortionDrive = fDrive;
        sDistortionDry = fDry;
        sDistortionWet = fWet;
        sDistortionFeedback = fFeedback;
        sDistortionVolume = fVolume;

        if(sActivity != null) {
            sActivity.effectFragment.getTextDistortionDrive().setText(String.format(Locale.getDefault(), "%.2f", sDistortionDrive));
            sActivity.effectFragment.getTextDistortionDry().setText(String.format(Locale.getDefault(), "%.2f", sDistortionDry));
            sActivity.effectFragment.getTextDistortionWet().setText(String.format(Locale.getDefault(), "%.2f", sDistortionWet));
            sActivity.effectFragment.getTextDistortionFeedback().setText(String.format(Locale.getDefault(), "%.2f", sDistortionFeedback));
            sActivity.effectFragment.getTextDistortionVolume().setText(String.format(Locale.getDefault(), "%.2f", sDistortionVolume));

            sActivity.effectFragment.getSeekDistortionDrive().setProgress((int) (fDrive * 100.0f));
            sActivity.effectFragment.getSeekDistortionDry().setProgress((int) (fDry * 100.0f));
            sActivity.effectFragment.getSeekDistortionWet().setProgress((int) (fWet * 100.0f));
            sActivity.effectFragment.getSeekDistortionFeedback().setProgress((int) (fFeedback * 100.0f));
            sActivity.effectFragment.getSeekDistortionVolume().setProgress((int) (fVolume * 100.0f));
        }

        updateDistortion();
        if (bSave) PlaylistFragment.updateSavingEffect();
    }

    private void minusDistortionDrive() {
        setDistortion(mSeekDistortionDrive.getProgress() - 1, mSeekDistortionDry.getProgress(), mSeekDistortionWet.getProgress(), mSeekDistortionFeedback.getProgress(), mSeekDistortionVolume.getProgress(), true);
    }

    private void plusDistortionDrive() {
        setDistortion(mSeekDistortionDrive.getProgress() + 1, mSeekDistortionDry.getProgress(), mSeekDistortionWet.getProgress(), mSeekDistortionFeedback.getProgress(), mSeekDistortionVolume.getProgress(), true);
    }

    private void minusDistortionDry() {
        setDistortion(mSeekDistortionDrive.getProgress(), mSeekDistortionDry.getProgress() - 1, mSeekDistortionWet.getProgress(), mSeekDistortionFeedback.getProgress(), mSeekDistortionVolume.getProgress(), true);
    }

    private void plusDistortionDry() {
        setDistortion(mSeekDistortionDrive.getProgress(), mSeekDistortionDry.getProgress() + 1, mSeekDistortionWet.getProgress(), mSeekDistortionFeedback.getProgress(), mSeekDistortionVolume.getProgress(), true);
    }

    private void minusDistortionWet() {
        setDistortion(mSeekDistortionDrive.getProgress(), mSeekDistortionDry.getProgress(), mSeekDistortionWet.getProgress() - 1, mSeekDistortionFeedback.getProgress(), mSeekDistortionVolume.getProgress(), true);
    }

    private void plusDistortionWet() {
        setDistortion(mSeekDistortionDrive.getProgress(), mSeekDistortionDry.getProgress(), mSeekDistortionWet.getProgress() + 1, mSeekDistortionFeedback.getProgress(), mSeekDistortionVolume.getProgress(), true);
    }

    private void minusDistortionFeedback() {
        setDistortion(mSeekDistortionDrive.getProgress(), mSeekDistortionDry.getProgress(), mSeekDistortionWet.getProgress(), mSeekDistortionFeedback.getProgress() - 1, mSeekDistortionVolume.getProgress(), true);
    }

    private void plusDistortionFeedback() {
        setDistortion(mSeekDistortionDrive.getProgress(), mSeekDistortionDry.getProgress(), mSeekDistortionWet.getProgress(), mSeekDistortionFeedback.getProgress() + 1, mSeekDistortionVolume.getProgress(), true);
    }

    private void minusDistortionVolume() {
        setDistortion(mSeekDistortionDrive.getProgress(), mSeekDistortionDry.getProgress(), mSeekDistortionWet.getProgress(), mSeekDistortionFeedback.getProgress(), mSeekDistortionVolume.getProgress() - 1, true);
    }

    private void plusDistortionVolume() {
        setDistortion(mSeekDistortionDrive.getProgress(), mSeekDistortionDry.getProgress(), mSeekDistortionWet.getProgress(), mSeekDistortionFeedback.getProgress(), mSeekDistortionVolume.getProgress() + 1, true);
    }

    private static void updateSoundEffect() {
        if (!sEffectItems.get(EFFECTTYPE_SOUNDEFFECT).isSelected()) return;
        if (sSEStream == 0) {
            if (sSoundEffectSelected == SOUNDEFFECTTYPE_RECORDNOISE) {
                sSE1PlayingFlag = true;
                MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                params.inputStream = sActivity.getResources().openRawResource(R.raw.recordnoise);
                sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 4.653), endRecordNoise, null);
                BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f);
                BASS.BASS_ChannelPlay(sSEStream, true);
            } else if (sSoundEffectSelected == SOUNDEFFECTTYPE_ROAROFWAVES) {
                sSE1PlayingFlag = true;
                MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                params.inputStream = sActivity.getResources().openRawResource(R.raw.wave);
                sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 28.399), endWave, null);
                BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f);
                BASS.BASS_ChannelPlay(sSEStream, true);
            } else if (sSoundEffectSelected == SOUNDEFFECTTYPE_RAIN) {
                sSE1PlayingFlag = true;
                MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                params.inputStream = sActivity.getResources().openRawResource(R.raw.rain);
                sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 1.503), endRain, null);
                BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f);
                BASS.BASS_ChannelPlay(sSEStream, true);
            } else if (sSoundEffectSelected == SOUNDEFFECTTYPE_RIVER) {
                sSE1PlayingFlag = true;
                MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                params.inputStream = sActivity.getResources().openRawResource(R.raw.river);
                sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 60.000), endRiver, null);
                BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f);
                BASS.BASS_ChannelPlay(sSEStream, true);
            } else if (sSoundEffectSelected == SOUNDEFFECTTYPE_WAR) {
                sSE1PlayingFlag = true;
                MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                params.inputStream = sActivity.getResources().openRawResource(R.raw.war);
                sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 30.000), endWar, null);
                BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f);
                BASS.BASS_ChannelPlay(sSEStream, true);
            } else if (sSoundEffectSelected == SOUNDEFFECTTYPE_FIRE) {
                sSE1PlayingFlag = true;
                MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                params.inputStream = sActivity.getResources().openRawResource(R.raw.fire);
                sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 10.000), endFire, null);
                BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f);
                BASS.BASS_ChannelPlay(sSEStream, true);
            } else if (sSoundEffectSelected == SOUNDEFFECTTYPE_CONCERTHALL) {
                sSE1PlayingFlag = true;
                MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                params.inputStream = sActivity.getResources().openRawResource(R.raw.cheer);
                sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 14.000), endCheer, null);
                BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f);
                BASS.BASS_ChannelPlay(sSEStream, true);

                sHandler = new Handler();
                sHandler.post(onTimer);
            }
        } else {
            int hSETemp = sSE1PlayingFlag ? sSEStream : sSEStream2;
            BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f);
        }
    }

    public static void setSoundEffect(int nVolume, boolean bSave) {
        if (nVolume < 0) nVolume = 0;
        else if (nVolume > 100) nVolume = 100;

        sSoundEffectVolume = nVolume;

        if(sActivity != null) {
            sActivity.effectFragment.getTextSoundEffectVolume().setText(String.format(Locale.getDefault(), "%.0f", sSoundEffectVolume));
            sActivity.effectFragment.getSeekSoundEffectVolume().setProgress(nVolume);
        }

        updateSoundEffect();
        if (bSave) PlaylistFragment.updateSavingEffect();
    }

    public static void setSoundEffect(float fVolume, boolean bSave) {
        sSoundEffectVolume = fVolume;

        if(sActivity != null) {
            sActivity.effectFragment.getTextSoundEffectVolume().setText(String.format(Locale.getDefault(), "%.0f", sSoundEffectVolume));
            sActivity.effectFragment.getSeekSoundEffectVolume().setProgress((int) fVolume);
        }

        updateSoundEffect();
        if (bSave) PlaylistFragment.updateSavingEffect();
    }

    private void minusSoundEffectVolume() {
        setSoundEffect(mSeekSoundEffectVolume.getProgress() - 1, true);
    }

    private void plusSoundEffectVolume() {
        setSoundEffect(mSeekSoundEffectVolume.getProgress() + 1, true);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private void checkDuplicate(int nSelect) {
        mBtnEffectOff.setSelected(false);
        if (EFFECTTYPE_VOCALCANCEL <= nSelect && nSelect <= EFFECTTYPE_TRANSCRIBESIDEGUITAR || nSelect == EFFECTTYPE_8BITSOUND) {
            for (int i = EFFECTTYPE_VOCALCANCEL; i <= EFFECTTYPE_TRANSCRIBESIDEGUITAR; i++)
                if (i != nSelect) deselectEffect(i);
            if (nSelect != EFFECTTYPE_8BITSOUND) deselectEffect(EFFECTTYPE_8BITSOUND);
        }
        if (nSelect == EFFECTTYPE_FREQ || nSelect == EFFECTTYPE_8BITSOUND) {
            if (nSelect != EFFECTTYPE_FREQ) deselectEffect(EFFECTTYPE_FREQ);
            if (nSelect != EFFECTTYPE_8BITSOUND) deselectEffect(EFFECTTYPE_8BITSOUND);
        }
        if (nSelect == EFFECTTYPE_RANDOM || nSelect == EFFECTTYPE_TRANSCRIBESIDEGUITAR || nSelect == EFFECTTYPE_TRANSCRIBEBASS || nSelect == EFFECTTYPE_OLDRECORD || nSelect == EFFECTTYPE_LOWBATTERY || nSelect == EFFECTTYPE_EARTRAINING || nSelect == EFFECTTYPE_NOSENSE_STRONG || nSelect == EFFECTTYPE_NOSENSE_MIDDLE || nSelect == EFFECTTYPE_NOSENSE_WEAK) {
            if (nSelect != EFFECTTYPE_RANDOM) deselectEffect(EFFECTTYPE_RANDOM);
            if (nSelect != EFFECTTYPE_TRANSCRIBESIDEGUITAR)
                deselectEffect(EFFECTTYPE_TRANSCRIBESIDEGUITAR);
            if (nSelect != EFFECTTYPE_TRANSCRIBEBASS) deselectEffect(EFFECTTYPE_TRANSCRIBEBASS);
            if (nSelect != EFFECTTYPE_OLDRECORD) deselectEffect(EFFECTTYPE_OLDRECORD);
            if (nSelect != EFFECTTYPE_LOWBATTERY) deselectEffect(EFFECTTYPE_LOWBATTERY);
            if (nSelect != EFFECTTYPE_EARTRAINING) deselectEffect(EFFECTTYPE_EARTRAINING);
            if (nSelect != EFFECTTYPE_NOSENSE_STRONG) deselectEffect(EFFECTTYPE_NOSENSE_STRONG);
            if (nSelect != EFFECTTYPE_NOSENSE_MIDDLE) deselectEffect(EFFECTTYPE_NOSENSE_MIDDLE);
            if (nSelect != EFFECTTYPE_NOSENSE_WEAK) deselectEffect(EFFECTTYPE_NOSENSE_WEAK);
        }
        if (nSelect == EFFECTTYPE_DISTORTION || nSelect == EFFECTTYPE_LOWBATTERY) {
            if (nSelect != EFFECTTYPE_DISTORTION) deselectEffect(EFFECTTYPE_DISTORTION);
            if (nSelect != EFFECTTYPE_LOWBATTERY) deselectEffect(EFFECTTYPE_LOWBATTERY);
        }
        if (EFFECTTYPE_INCREASESPEED <= nSelect && nSelect <= EFFECTTYPE_EARTRAINING) {
            for (int i = EFFECTTYPE_INCREASESPEED; i <= EFFECTTYPE_EARTRAINING; i++)
                if (i != nSelect) deselectEffect(i);
        }
        if (nSelect == EFFECTTYPE_OLDRECORD || nSelect == EFFECTTYPE_SOUNDEFFECT) {
            if (nSelect != EFFECTTYPE_OLDRECORD) deselectEffect(EFFECTTYPE_OLDRECORD);
            if (nSelect != EFFECTTYPE_SOUNDEFFECT) deselectEffect(EFFECTTYPE_SOUNDEFFECT);
        }
        PlaylistFragment.updateSavingEffect();
    }

    private void deselectEffect(int nEffect) {
        if (!sEffectItems.get(nEffect).isSelected()) return;

        sEffectItems.get(nEffect).setSelected(false);
        mEffectsAdapter.notifyItemChanged(nEffect);

        if (nEffect == EFFECTTYPE_RANDOM || nEffect == EFFECTTYPE_TRANSCRIBESIDEGUITAR || nEffect == EFFECTTYPE_OLDRECORD || nEffect == EFFECTTYPE_LOWBATTERY || nEffect == EFFECTTYPE_EARTRAINING)
            EqualizerFragment.resetEQ();
        if (nEffect == EFFECTTYPE_RANDOM || nEffect == EFFECTTYPE_NOSENSE_STRONG || nEffect == EFFECTTYPE_NOSENSE_MIDDLE || nEffect == EFFECTTYPE_NOSENSE_WEAK) {
            ControlFragment.setSpeed(0.0f);
            ControlFragment.setPitch(0.0f);
        }
        if (nEffect == EFFECTTYPE_TRANSCRIBEBASS) {
            ControlFragment.setPitch(0.0f);
            EqualizerFragment.resetEQ();
        }
        if (nEffect == EFFECTTYPE_8BITSOUND)
            ControlFragment.setSpeed(0.0f);
        if (nEffect == EFFECTTYPE_REVERB) sReverbSelected = -1;
        if (nEffect == EFFECTTYPE_ECHO) sEchoSelected = -1;
        if (nEffect == EFFECTTYPE_CHORUS) sChorusSelected = -1;
        if (nEffect == EFFECTTYPE_DISTORTION) sDistortionSelected = -1;
        if (nEffect == EFFECTTYPE_COMP) sCompSelected = -1;
        if (nEffect == EFFECTTYPE_PAN) sPanSelected = -1;
        if (nEffect == EFFECTTYPE_FREQ) sFreqSelected = -1;
        if (nEffect == EFFECTTYPE_METRONOME) sMetronomeSelected = -1;
        if (nEffect == EFFECTTYPE_SOUNDEFFECT) sSoundEffectSelected = -1;
    }

    public static void applyEffect() {
        int nPlayingPlaylist = PlaylistFragment.sPlayingPlaylist;
        if (nPlayingPlaylist < 0 || nPlayingPlaylist >= PlaylistFragment.sPlaylists.size()) {
            applyEffect(MainActivity.sStream, null);
            return;
        }
        ArrayList<SongItem> arSongs = PlaylistFragment.sPlaylists.get(nPlayingPlaylist);
        int nPlaying = PlaylistFragment.sPlaying;
        if (nPlaying < 0 || nPlaying >= arSongs.size()) {
            applyEffect(MainActivity.sStream, null);
            return;
        }
        SongItem song = arSongs.get(nPlaying);
        applyEffect(MainActivity.sStream, song);
    }

    public static void applyEffect(int stream, SongItem song) {
        if (sDspVocalCancel != 0) {
            BASS.BASS_ChannelRemoveDSP(stream, sDspVocalCancel);
            sDspVocalCancel = 0;
        }
        if (sDspMonoral != 0) {
            BASS.BASS_ChannelRemoveDSP(stream, sDspMonoral);
            sDspMonoral = 0;
        }
        if (sDspLeft != 0) {
            BASS.BASS_ChannelRemoveDSP(stream, sDspLeft);
            sDspLeft = 0;
        }
        if (sDspRight != 0) {
            BASS.BASS_ChannelRemoveDSP(stream, sDspRight);
            sDspRight = 0;
        }
        if (sDspExchange != 0) {
            BASS.BASS_ChannelRemoveDSP(stream, sDspExchange);
            sDspExchange = 0;
        }
        if (sDspDoubling != 0) {
            BASS.BASS_ChannelRemoveDSP(stream, sDspDoubling);
            sDspDoubling = 0;
        }
        if (sDspPan != 0) {
            BASS.BASS_ChannelRemoveDSP(stream, sDspPan);
            sDspPan = 0;
        }
        if (sDspNormalize != 0) {
            BASS.BASS_ChannelRemoveDSP(stream, sDspNormalize);
            sDspNormalize = 0;
        }
        if (sFxComp != 0) {
            BASS.BASS_ChannelRemoveFX(stream, sFxComp);
            sFxComp = 0;
        }
        if (sDspPhaseReversal != 0) {
            BASS.BASS_ChannelRemoveDSP(stream, sDspPhaseReversal);
            sDspPhaseReversal = 0;
        }
        BASS.BASS_CHANNELINFO info = new BASS.BASS_CHANNELINFO();
        BASS.BASS_ChannelGetInfo(stream, info);
        BASS.BASS_ChannelSetAttribute(stream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, info.freq);
        if (sFxEcho != 0) {
            BASS.BASS_ChannelRemoveFX(stream, sFxEcho);
            sFxEcho = 0;
        }
        if (sFxReverb != 0) {
            BASS.BASS_ChannelRemoveFX(stream, sFxReverb);
            sFxReverb = 0;
        }
        if (sFxChorus != 0) {
            BASS.BASS_ChannelRemoveFX(stream, sFxChorus);
            sFxChorus = 0;
        }
        if (sFxDistortion != 0) {
            BASS.BASS_ChannelRemoveFX(stream, sFxDistortion);
            sFxDistortion = 0;
        }
        if (sTimer != null) {
            sTimer.cancel();
            sTimer = null;
        }
        Context context = sActivity != null ? sActivity : MainActivity.sService;
        for (int i = 0; i < sEffectItems.size(); i++) {
            if (!sEffectItems.get(i).isSelected()) continue;
            String strEffect = sEffectItems.get(i).getEffectName();
            BASS_FX.BASS_BFX_ECHO4 echo;
            BASS_FX.BASS_BFX_FREEVERB reverb;
            BASS_FX.BASS_BFX_CHORUS chorus;
            BASS_FX.BASS_BFX_DISTORTION distortion;
            int[] array;
            if (strEffect.equals(context.getString(R.string.random))) {
                float fMaxSpeed = 1.5f;
                float fMinSpeed = 0.75f;
                fMaxSpeed = (fMaxSpeed - 1.0f) * 100.0f;
                fMinSpeed = (1.0f - fMinSpeed) * -100.0f;
                Random random = new Random();
                float fRand = random.nextFloat();
                float fSpeed = (fRand * (fMaxSpeed - fMinSpeed) * 10.0f) / 10.0f + fMinSpeed;
                ControlFragment.setSpeed(fSpeed);
                float fMaxPitch = 3.0f;
                float fMinPitch = -3.0f;
                fRand = random.nextFloat();
                float fPitch = (fRand * (fMaxPitch - fMinPitch) * 10.0f) / 10.0f + fMinPitch;
                ControlFragment.setPitch(fPitch);
                EqualizerFragment.setEQRandom();
            } else if (strEffect.equals(context.getString(R.string.vocalCancel))) {
                if (info.chans != 1)
                    sDspVocalCancel = BASS.BASS_ChannelSetDSP(stream, vocalCancelDSP, null, 0);
            } else if (strEffect.equals(context.getString(R.string.monoral))) {
                if (info.chans != 1)
                    sDspMonoral = BASS.BASS_ChannelSetDSP(stream, monoralDSP, null, 0);
            } else if (strEffect.equals(context.getString(R.string.leftOnly))) {
                if (info.chans != 1)
                    sDspLeft = BASS.BASS_ChannelSetDSP(stream, leftDSP, null, 0);
            } else if (strEffect.equals(context.getString(R.string.rightOnly))) {
                if (info.chans != 1)
                    sDspRight = BASS.BASS_ChannelSetDSP(stream, rightDSP, null, 0);
            } else if (strEffect.equals(context.getString(R.string.leftAndRightReplace))) {
                if (info.chans != 1)
                    sDspExchange = BASS.BASS_ChannelSetDSP(stream, exchangeDSP, null, 0);
            } else if (strEffect.equals(context.getString(R.string.doubling))) {
                if (info.chans != 1) {
                    for (int j = 0; j < ECHBUFLEN; j++) {
                        echbuf[j][0] = 0;
                        echbuf[j][1] = 0;
                    }
                    echpos = 0;
                    sDspDoubling = BASS.BASS_ChannelSetDSP(stream, doublingDSP, null, 0);
                }
            } else if (strEffect.equals(context.getString(R.string.transcribeSideGuitar))) {
                if (info.chans != 1)
                    sDspVocalCancel = BASS.BASS_ChannelSetDSP(stream, vocalCancelDSP, null, 0);
                array = new int[]{ 50,-30,-20,-11, -5,  1,  2,  3,  4,  5,  5,  5,  5,  5,  4,  3,  2,  1, -5,-11,-20,-24,-27,-28,-29,-30,-30,-30,-30,-30,-30,-30};
                for (int j = 0; j < 32; j++) {
                    int nLevel = array[j];
                    if (j == 0)
                        sActivity.equalizerFragment.setVol(nLevel);
                    else
                        sActivity.equalizerFragment.setEQ(j, nLevel);
                }
            } else if (strEffect.equals(context.getString(R.string.transcribeBassOctave))) {
                sActivity.controlFragment.setLink(false);
                ControlFragment.setPitch(12.0f);
                array = new int[]{ 10,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-20,-10,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6};
                for (int j = 0; j < 32; j++) {
                    int nLevel = array[j];
                    if (j == 0)
                        sActivity.equalizerFragment.setVol(nLevel);
                    else
                        sActivity.equalizerFragment.setEQ(j, nLevel);
                }
            } else if (strEffect.equals(context.getString(R.string.pan))) {
                if (info.chans != 1)
                    sDspPan = BASS.BASS_ChannelSetDSP(stream, panDSP, null, 0);
            } else if (strEffect.equals(context.getString(R.string.normalize))) {
                if(song != null) {
                    if (song.getPeak() == 0.0f) {
                        if (stream != MainActivity.sStream) getPeak(song);
                        else sPeak = 1.0f;
                    } else sPeak = song.getPeak();
                    sDspNormalize = BASS.BASS_ChannelSetDSP(stream, normalizeDSP, null, 0);
                }
            } else if (strEffect.equals(context.getString(R.string.comp))) {
                sFxComp = BASS.BASS_ChannelSetFX(stream, BASS_FX.BASS_FX_BFX_COMPRESSOR2, 2);
                BASS_FX.BASS_BFX_COMPRESSOR2 p = new BASS_FX.BASS_BFX_COMPRESSOR2();
                p.fGain = 2.0f;
                p.fThreshold = -20.0f;
                p.fRatio = 10.0f;
                p.fAttack = 1.2f;
                p.fRelease = 400.0f;
                p.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(sFxComp, p);
            } else if (strEffect.equals(context.getString(R.string.freq)))
                BASS.BASS_ChannelSetAttribute(stream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, info.freq * sFreq);
            else if (strEffect.equals(context.getString(R.string.phaseReversal)))
                sDspPhaseReversal = BASS.BASS_ChannelSetDSP(stream, phaseReversalDSP, null, 0);
            else if (strEffect.equals(context.getString(R.string.echo))) {
                sFxEcho = BASS.BASS_ChannelSetFX(stream, BASS_FX.BASS_FX_BFX_ECHO4, 2);
                echo = new BASS_FX.BASS_BFX_ECHO4();
                echo.fDryMix = sEchoDry;
                echo.fWetMix = sEchoWet;
                echo.fFeedback = sEchoFeedback;
                echo.fDelay = sEchoDelay;
                echo.bStereo = TRUE;
                echo.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(sFxEcho, echo);
            } else if (strEffect.equals(context.getString(R.string.reverb))) {
                sFxReverb = BASS.BASS_ChannelSetFX(stream, BASS_FX.BASS_FX_BFX_FREEVERB, 2);
                reverb = new BASS_FX.BASS_BFX_FREEVERB();
                reverb.fDryMix = sReverbDry;
                reverb.fWetMix = sReverbWet;
                reverb.fRoomSize = sReverbRoomSize;
                reverb.fDamp = sReverbDamp;
                reverb.fWidth = sReverbWidth;
                reverb.lMode = 0;
                reverb.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(sFxReverb, reverb);
            } else if (strEffect.equals(context.getString(R.string.chorusFlanger))) {
                sFxChorus = BASS.BASS_ChannelSetFX(stream, BASS_FX.BASS_FX_BFX_CHORUS, 2);
                chorus = new BASS_FX.BASS_BFX_CHORUS();
                chorus.fDryMix = sChorusDry;
                chorus.fWetMix = sChorusWet;
                chorus.fFeedback = sChorusFeedback;
                chorus.fMinSweep = sChorusMinSweep;
                chorus.fMaxSweep = sChorusMaxSweep;
                chorus.fRate = sChorusRate;
                chorus.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(sFxChorus, chorus);
            } else if (strEffect.equals(context.getString(R.string.distortion))) {
                sFxDistortion = BASS.BASS_ChannelSetFX(stream, BASS_FX.BASS_FX_BFX_DISTORTION, 2);
                distortion = new BASS_FX.BASS_BFX_DISTORTION();
                distortion.fDrive = sDistortionDrive;
                distortion.fDryMix = sDistortionDry;
                distortion.fWetMix = sDistortionWet;
                distortion.fFeedback = sDistortionFeedback;
                distortion.fVolume = sDistortionVolume;
                distortion.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(sFxDistortion, distortion);
            } else if (strEffect.equals(context.getString(R.string.reverse))) {
                if (stream != 0) {
                    int chan = BASS_FX.BASS_FX_TempoGetSource(stream);
                    BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_REVERSE);
                    MainActivity.setSync();
                }
            } else if (strEffect.equals(context.getString(R.string.increaseSpeed))) {
                if (sHandler != null) {
                    sHandler.removeCallbacks(onTimer);
                    sHandler = null;
                }
                sHandler = new Handler();
                sHandler.post(onTimer);
            } else if (strEffect.equals(context.getString(R.string.decreaseSpeed))) {
                if (sHandler != null) {
                    sHandler.removeCallbacks(onTimer);
                    sHandler = null;
                }
                sHandler = new Handler();
                sHandler.post(onTimer);
            } else if (strEffect.equals(context.getString(R.string.raisePitch))) {
                if (sHandler != null) {
                    sHandler.removeCallbacks(onTimer);
                    sHandler = null;
                }
                sHandler = new Handler();
                sHandler.post(onTimer);
            } else if (strEffect.equals(context.getString(R.string.lowerPitch))) {
                if (sHandler != null) {
                    sHandler.removeCallbacks(onTimer);
                    sHandler = null;
                }
                sHandler = new Handler();
                sHandler.post(onTimer);
            } else if (strEffect.equals(context.getString(R.string.oldRecord))) {
                array = new int[]{200,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12};
                for (int j = 0; j < 32; j++) {
                    int nLevel = array[j];
                    if (j == 0)
                        sActivity.equalizerFragment.setVol(nLevel);
                    else
                        sActivity.equalizerFragment.setEQ(j, nLevel);
                }
                if (sSEStream == 0) {
                    sSE1PlayingFlag = true;
                    MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                    params.inputStream = context.getResources().openRawResource(R.raw.recordnoise);
                    sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                    sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 4.653), endRecordNoise, null);
                    BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f);
                    BASS.BASS_ChannelPlay(sSEStream, true);
                }

                sHandler = new Handler();
                sHandler.post(onTimer);
            } else if (strEffect.equals(context.getString(R.string.lowBattery))) {
                sFxDistortion = BASS.BASS_ChannelSetFX(stream, BASS_FX.BASS_FX_BFX_DISTORTION, 2);
                distortion = new BASS_FX.BASS_BFX_DISTORTION();
                distortion.fDrive = (float) 0.2;
                distortion.fDryMix = (float) 0.9;
                distortion.fWetMix = (float) 0.1;
                distortion.fFeedback = (float) 0.1;
                distortion.fVolume = (float) 1.0;
                distortion.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(sFxDistortion, distortion);

                array = new int[]{200,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12};
                for (int j = 0; j < 32; j++) {
                    int nLevel = array[j];
                    if (j == 0)
                        sActivity.equalizerFragment.setVol(nLevel);
                    else
                        sActivity.equalizerFragment.setEQ(j, nLevel);
                }

                sHandler = new Handler();
                sHandler.post(onTimer);
            } else if (strEffect.equals(context.getString(R.string.noSenseStrong)) || strEffect.equals(context.getString(R.string.noSenseMiddle)) || strEffect.equals(context.getString(R.string.noSenseWeak))) {
                sVelo1 = sSoundEffectVolume = 0.0f;
                ControlFragment.setSpeed(0.0f);
                ControlFragment.setPitch(0.0f);

                sHandler = new Handler();
                sHandler.post(onTimer);
            } else if (strEffect.equals(context.getString(R.string.earTraining))) {
                sHandler = new Handler();
                sHandler.post(onTimer);
            } else if (strEffect.equals(context.getString(R.string.metronome))) {
                sMetronome.setBpm(sBpm);
                if(!sMetronome.isPlaying()) sMetronome.play();
            } else if (strEffect.equals(context.getString(R.string.soundEffect))) {
                if (sSoundEffectSelected == SOUNDEFFECTTYPE_RECORDNOISE) {
                    if (sSEStream == 0) {
                        sSE1PlayingFlag = true;
                        MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                        params.inputStream = context.getResources().openRawResource(R.raw.recordnoise);
                        sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                        sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 4.653), endRecordNoise, null);
                        BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f);
                        BASS.BASS_ChannelPlay(sSEStream, true);
                    }
                } else if (sSoundEffectSelected == SOUNDEFFECTTYPE_ROAROFWAVES) {
                    if (sSEStream == 0) {
                        sSE1PlayingFlag = true;
                        MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                        params.inputStream = context.getResources().openRawResource(R.raw.wave);
                        sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                        sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 28.399), endWave, null);
                        BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f);
                        BASS.BASS_ChannelPlay(sSEStream, true);
                    }
                } else if (sSoundEffectSelected == SOUNDEFFECTTYPE_RAIN) {
                    if (sSEStream == 0) {
                        sSE1PlayingFlag = true;
                        MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                        params.inputStream = context.getResources().openRawResource(R.raw.rain);
                        sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                        sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 1.503), endRain, null);
                        BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f);
                        BASS.BASS_ChannelPlay(sSEStream, true);
                    }
                } else if (sSoundEffectSelected == SOUNDEFFECTTYPE_RIVER) {
                    if (sSEStream == 0) {
                        sSE1PlayingFlag = true;
                        MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                        params.inputStream = context.getResources().openRawResource(R.raw.river);
                        sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                        sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 60.000), endRiver, null);
                        BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f);
                        BASS.BASS_ChannelPlay(sSEStream, true);
                    }
                } else if (sSoundEffectSelected == SOUNDEFFECTTYPE_WAR) {
                    if (sSEStream == 0) {
                        sSE1PlayingFlag = true;
                        MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                        params.inputStream = context.getResources().openRawResource(R.raw.war);
                        sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                        sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 30.000), endWar, null);
                        BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f);
                        BASS.BASS_ChannelPlay(sSEStream, true);
                    }
                } else if (sSoundEffectSelected == SOUNDEFFECTTYPE_FIRE) {
                    if (sSEStream == 0) {
                        sSE1PlayingFlag = true;
                        MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                        params.inputStream = context.getResources().openRawResource(R.raw.fire);
                        sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                        sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 10.000), endFire, null);
                        BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f);
                        BASS.BASS_ChannelPlay(sSEStream, true);
                    }
                } else if (sSoundEffectSelected == SOUNDEFFECTTYPE_CONCERTHALL) {
                    if (sSEStream == 0) {
                        sSE1PlayingFlag = true;
                        MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                        params.inputStream = context.getResources().openRawResource(R.raw.cheer);
                        sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                        sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 14.000), endCheer, null);
                        BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f);

                        sHandler = new Handler();
                        sHandler.post(onTimer);
                    }
                }
            } else if (strEffect.equals(context.getString(R.string.eightBitSound))) {
                BASS.BASS_ChannelSetAttribute(stream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, info.freq * 4.0f);
                ControlFragment.setSpeed(25 - 100, false);
                if (info.chans != 1)
                    sDspVocalCancel = BASS.BASS_ChannelSetDSP(stream, vocalCancelDSP, null, 0);
            }
        }
    }

    private static void getPeak(SongItem song) {
        if (song == null) return;
        File file = new File(song.getPath());
        int hTempStream = 0;
        if (file.getParent().equals(sActivity.getFilesDir().toString()))
            hTempStream = BASS.BASS_StreamCreateFile(song.getPath(), 0, 0, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
        else {
            ContentResolver cr = sActivity.getContentResolver();
            try {
                MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
                params.assetFileDescriptor = cr.openAssetFileDescriptor(Uri.parse(song.getPath()), "r");
                if (params.assetFileDescriptor != null) {
                    params.fileChannel = params.assetFileDescriptor.createInputStream().getChannel();
                    hTempStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        BASS.BASS_CHANNELINFO info = new BASS.BASS_CHANNELINFO();
        BASS.BASS_ChannelGetInfo(hTempStream, info);
        boolean bStereo = true;
        if (info.chans == 1) bStereo = false;
        float fTempPeak = 0.0f;
        float[] arLevels = new float[2];
        if (bStereo) {
            while (BASS.BASS_ChannelGetLevelEx(hTempStream, arLevels, 0.1f, BASS.BASS_LEVEL_STEREO)) {
                if (fTempPeak < arLevels[0]) fTempPeak = arLevels[0];
                if (fTempPeak < arLevels[1]) fTempPeak = arLevels[1];
            }
        } else {
            while (BASS.BASS_ChannelGetLevelEx(hTempStream, arLevels, 0.1f, BASS.BASS_LEVEL_MONO)) {
                if (fTempPeak < arLevels[0]) fTempPeak = arLevels[0];
            }
        }
        sPeak = fTempPeak;
    }

    private static final BASS.SYNCPROC endIncreaseSpeedLoop = new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, final Object user){
            BASS.FloatValue speed = new BASS.FloatValue();
            BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO, speed);
            speed.value += sIncreaseSpeedLoop;
            if (speed.value + 100.0f > 400.0f) speed.value = 300.0f;
            if (MainActivity.sStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PAUSED)
                ControlFragment.setSpeed(speed.value, false);
        }
    };

    private static final Runnable onTimer = new Runnable() {
        @Override
        public void run() {
            if (sEffectItems.get(EFFECTTYPE_INCREASESPEED).isSelected()) {
                if(EffectFragment.sSpecifiedEffectDetail) {
                    BASS.FloatValue speed = new BASS.FloatValue();
                    BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO, speed);
                    speed.value += sIncreaseSpeedSpecified;
                    if (speed.value + 100.0f > 400.0f) speed.value = 300.0f;
                    if (MainActivity.sStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PAUSED)
                        ControlFragment.setSpeed(speed.value, false);
                    sHandler.postDelayed(this, (long) (sTimeOfIncreaseSpeedSpecified * 1000.0f));
                }
            } else if (sEffectItems.get(EFFECTTYPE_DECREASESPEED).isSelected()) {
                if(EffectFragment.sSpecifiedEffectDetail) {
                    BASS.FloatValue speed = new BASS.FloatValue();
                    BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO, speed);
                    speed.value -= sDecreaseSpeedSpecified;
                    if (speed.value + 100.0f < 10.0f) speed.value = -90.0f;
                    if (MainActivity.sStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PAUSED)
                        ControlFragment.setSpeed(speed.value, false);
                    sHandler.postDelayed(this, (long) (sTimeOfDecreaseSpeedSpecified * 1000.0f));
                }
            } else if (sEffectItems.get(EFFECTTYPE_RAISEPITCH).isSelected()) {
                if(EffectFragment.sSpecifiedEffectDetail) {
                    BASS.FloatValue pitch = new BASS.FloatValue();
                    BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO_PITCH, pitch);
                    pitch.value += sRaisePitchSpecified;
                    if (pitch.value + 10.0f > 70.0f) pitch.value = 60.0f;
                    if (MainActivity.sStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PAUSED)
                        ControlFragment.setPitch(pitch.value, false);
                    sHandler.postDelayed(this, (long) (sTimeOfRaisePitchSpecified * 1000.0f));
                }
            } else if (sEffectItems.get(EFFECTTYPE_LOWERPITCH).isSelected()) {
                if(EffectFragment.sSpecifiedEffectDetail) {
                    BASS.FloatValue pitch = new BASS.FloatValue();
                    BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO_PITCH, pitch);
                    pitch.value -= sLowerPitchSpecified;
                    if (pitch.value + 70.0f < 10.0f) pitch.value = -60.0f;
                    if (MainActivity.sStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PAUSED)
                        ControlFragment.setPitch(pitch.value, false);
                    sHandler.postDelayed(this, (long) (sTimeOfLowerPitchSpecified * 1000.0f));
                }
            } else if (sEffectItems.get(EFFECTTYPE_OLDRECORD).isSelected()) {
                BASS.FloatValue freq = new BASS.FloatValue();
                BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS.BASS_ATTRIB_FREQ, freq);
                BASS.FloatValue tempoFreq = new BASS.FloatValue();
                BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, tempoFreq);
                tempoFreq.value = tempoFreq.value * 100.0f / sFreq;
                // 加速度の設定
                // 周波数が98以上の場合 : -0.1
                // 　　　　98未満の場合 : +0.1
                float sAccel = tempoFreq.value >= 98.0f ? -0.1f : 0.1f;

                // 周波数の差分に加速度を加える
                sVelo1 += sAccel;

                // 周波数に差分を加える
                tempoFreq.value += sVelo1;

                if (tempoFreq.value <= 90.0) tempoFreq.value = 90.0f;
                if (tempoFreq.value >= 100.0) tempoFreq.value = 100.0f;

                BASS.BASS_ChannelSetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, sFreq * tempoFreq.value / 100.0f);
                sHandler.postDelayed(this, 750);
            } else if (sEffectItems.get(EFFECTTYPE_LOWBATTERY).isSelected()) {
                BASS.FloatValue freq = new BASS.FloatValue();
                BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS.BASS_ATTRIB_FREQ, freq);
                BASS.FloatValue tempoFreq = new BASS.FloatValue();
                BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, tempoFreq);
                tempoFreq.value = tempoFreq.value * 100.0f / freq.value;
                // 加速度の設定
                // 周波数が68以上の場合 : -0.02
                // 　　　　68未満の場合 : +0.01
                float sAccel = tempoFreq.value >= 68.0f ? -0.02f : 0.01f;

                // 周波数の差分に加速度を加える
                sVelo1 += sAccel;

                // 周波数に差分を加える
                tempoFreq.value += sVelo1;

                if (tempoFreq.value <= 65.0) tempoFreq.value = 65.0f;
                if (tempoFreq.value >= 70.0) tempoFreq.value = 70.0f;

                BASS.BASS_ChannelSetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, freq.value * tempoFreq.value / 100.0f);

                sHandler.postDelayed(this, 50);
            } else if (sEffectItems.get(EFFECTTYPE_NOSENSE_STRONG).isSelected() || sEffectItems.get(EFFECTTYPE_NOSENSE_MIDDLE).isSelected() || sEffectItems.get(EFFECTTYPE_NOSENSE_WEAK).isSelected()) {
                BASS.FloatValue speed = new BASS.FloatValue();
                BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO, speed);
                float sAccel;
                Random random = new Random();
                float fRand = random.nextFloat();
                if (sEffectItems.get(EFFECTTYPE_NOSENSE_STRONG).isSelected()) {
                    sAccel = (fRand * 400.0f) / 10000.0f - 0.02f; // 加速度の設定
                    if (speed.value < -20.0f) sAccel = 0.01f;
                    else if (speed.value > 20.0f) sAccel = -0.01f;
                } else if (sEffectItems.get(EFFECTTYPE_NOSENSE_MIDDLE).isSelected()) {
                    sAccel = (fRand * 200.0f) / 10000.0f - 0.01f; // 加速度の設定
                    if (speed.value < -10.0f) sAccel = 0.01f;
                    else if (speed.value > 10.0f) sAccel = -0.01f;
                } else {
                    sAccel = (fRand * 100.0f) / 10000.0f - 0.005f; // 加速度の設定
                    if (speed.value < -5.0f) sAccel = 0.01f;
                    else if (speed.value > 5.0f) sAccel = -0.01f;
                }
                sVelo1 += sAccel; // 速度の差分に加速度を加える
                speed.value += sVelo1; // 速度に差分を加える
                if (MainActivity.sStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PAUSED)
                    ControlFragment.setSpeed(speed.value);

                BASS.FloatValue pitch = new BASS.FloatValue();
                fRand = random.nextFloat();
                BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO_PITCH, pitch);
                if (sEffectItems.get(EFFECTTYPE_NOSENSE_STRONG).isSelected()) {
                    sAccel = (fRand * 400.0f) / 10000.0f - 0.02f; // 加速度の設定
                    if (pitch.value < -4.0f) sAccel = 0.01f;
                    else if (pitch.value > 4.0f) sAccel = -0.01f;
                } else if (sEffectItems.get(EFFECTTYPE_NOSENSE_MIDDLE).isSelected()) {
                    sAccel = (fRand * 200.0f) / 10000.0f - 0.01f; // 加速度の設定
                    if (pitch.value < -2.0f) sAccel = 0.01f;
                    else if (pitch.value > 2.0f) sAccel = -0.01f;
                } else {
                    sAccel = (fRand * 100.0f) / 10000.0f - 0.005f; // 加速度の設定
                    if (pitch.value < -1.0f) sAccel = 0.01f;
                    else if (pitch.value > 1.0f) sAccel = -0.01f;
                }
                sVelo2 += sAccel; // 音程の差分に加速度を加える
                pitch.value += sVelo2; // 音程に差分を加える
                if (MainActivity.sStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PAUSED)
                    ControlFragment.setPitch(pitch.value);
                sHandler.postDelayed(this, 80);
            } else if (sEffectItems.get(EFFECTTYPE_EARTRAINING).isSelected()) {
                EqualizerFragment.setEQRandom();
                sHandler.postDelayed(this, 3000);
            } else if (sEffectItems.get(SOUNDEFFECTTYPE_CONCERTHALL).isSelected()) {
                int hSETemp = sSE1PlayingFlag ? sSEStream : sSEStream2;
                if (BASS.BASS_ChannelIsSliding(hSETemp, BASS.BASS_ATTRIB_VOL)) {
                    sHandler.postDelayed(this, 100);
                    return;
                }
                double dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetPosition(MainActivity.sStream, BASS.BASS_POS_BYTE));
                double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.sStream, BASS.BASS_ChannelGetLength(MainActivity.sStream, BASS.BASS_POS_BYTE));
                if (dLength - dPos < 5.0) {
                    BASS.BASS_ChannelSlideAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, 0.0f, 5000);
                    sHandler.postDelayed(this, 100);
                    return;
                }

                BASS.FloatValue vol = new BASS.FloatValue();
                BASS.BASS_ChannelGetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, vol);
                Random random = new Random();
                float fRand = random.nextFloat();
                fRand = (fRand / 200.0f) - 0.0025f;
                if (vol.value > 1.0f - 0.01f) fRand = -0.0005f;
                else if (vol.value <= 0.5f) fRand = 0.0005f;
                sAccel += fRand;
                vol.value += sAccel;
                if (vol.value > 1.0f) vol.value = 1.0f;
                else if (vol.value < 0.5f) vol.value = 0.5f;
                BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, vol.value * sSoundEffectVolume / 100.0f);
                sHandler.postDelayed(this, 100);
            }
        }
    };

    private static final BASS.SYNCPROC endRecordNoise = new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, final Object user) {
            EffectFragment.onRecordNoiseEnded();
        }
    };

    private static void onRecordNoiseEnded() {
        if (sSE1PlayingFlag) {
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = sActivity.getResources().openRawResource(R.raw.recordnoise);
            sSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
            BASS.BASS_ChannelSetAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(sSEStream2, BASS.BASS_ChannelSeconds2Bytes(sSEStream2, 1.417), BASS.BASS_POS_BYTE);
            if (sSync != 0) {
                BASS.BASS_ChannelRemoveSync(sSEStream, sSync);
                sSync = 0;
            }
            sSync = BASS.BASS_ChannelSetSync(sSEStream2, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream2, 4.653), endRecordNoise, null);
            BASS.BASS_ChannelPlay(sSEStream2, FALSE);
            BASS.BASS_ChannelSlideAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            BASS.BASS_ChannelSlideAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f, 1000);
            sSE1PlayingFlag = false;
        } else {
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = sActivity.getResources().openRawResource(R.raw.recordnoise);
            sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
            BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(sSEStream, BASS.BASS_ChannelSeconds2Bytes(sSEStream2, 1.417), BASS.BASS_POS_BYTE);
            if (sSync != 0) {
                BASS.BASS_ChannelRemoveSync(sSEStream2, sSync);
                sSync = 0;
            }
            sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 4.653), endRecordNoise, null);
            BASS.BASS_ChannelPlay(sSEStream, FALSE);
            BASS.BASS_ChannelSlideAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            BASS.BASS_ChannelSlideAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f, 1000);
            sSE1PlayingFlag = true;
        }
    }

    private static final BASS.SYNCPROC endWave = new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, final Object user) {
            EffectFragment.onWaveEnded();
        }
    };

    private static void onWaveEnded() {
        Context context = sActivity != null ? sActivity : MainActivity.sService;
        if (sSE1PlayingFlag) {
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = context.getResources().openRawResource(R.raw.wave);
            sSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
            BASS.BASS_ChannelSetAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(sSEStream2, BASS.BASS_ChannelSeconds2Bytes(sSEStream2, 0.283), BASS.BASS_POS_BYTE);
            if (sSync != 0) {
                BASS.BASS_ChannelRemoveSync(sSEStream, sSync);
                sSync = 0;
            }
            sSync = BASS.BASS_ChannelSetSync(sSEStream2, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream2, 28.399), endWave, null);
            BASS.BASS_ChannelPlay(sSEStream2, FALSE);
            BASS.BASS_ChannelSlideAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            BASS.BASS_ChannelSlideAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f, 1000);
            sSE1PlayingFlag = false;
        } else if (BASS.BASS_ChannelIsActive(sSEStream2) == BASS.BASS_ACTIVE_PLAYING) {
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = context.getResources().openRawResource(R.raw.wave);
            sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
            BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(sSEStream, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 0.283), BASS.BASS_POS_BYTE);
            if (sSync != 0) {
                BASS.BASS_ChannelRemoveSync(sSEStream2, sSync);
                sSync = 0;
            }
            sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 28.399), endWave, null);
            BASS.BASS_ChannelPlay(sSEStream, FALSE);
            BASS.BASS_ChannelSlideAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            BASS.BASS_ChannelSlideAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f, 1000);
            sSE1PlayingFlag = true;
        }
    }

    private static final BASS.SYNCPROC endRain = new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, final Object user) {
            EffectFragment.onRainEnded();
        }
    };

    private static void onRainEnded() {
        Context context = sActivity != null ? sActivity : MainActivity.sService;
        if (sSE1PlayingFlag) {
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = context.getResources().openRawResource(R.raw.rain);
            sSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
            BASS.BASS_ChannelSetAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(sSEStream2, BASS.BASS_ChannelSeconds2Bytes(sSEStream2, 0.303), BASS.BASS_POS_BYTE);
            if (sSync != 0) {
                BASS.BASS_ChannelRemoveSync(sSEStream, sSync);
                sSync = 0;
            }
            sSync = BASS.BASS_ChannelSetSync(sSEStream2, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream2, 1.503), endRain, null);
            BASS.BASS_ChannelPlay(sSEStream2, FALSE);
            BASS.BASS_ChannelSlideAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f, 150);
            BASS.BASS_ChannelSlideAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, 0.0f, 300);
            sSE1PlayingFlag = false;
        } else if (BASS.BASS_ChannelIsActive(sSEStream2) == BASS.BASS_ACTIVE_PLAYING) {
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = context.getResources().openRawResource(R.raw.rain);
            sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
            BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(sSEStream, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 0.303), BASS.BASS_POS_BYTE);
            if (sSync != 0) {
                BASS.BASS_ChannelRemoveSync(sSEStream2, sSync);
                sSync = 0;
            }
            sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 1.503), endRain, null);
            BASS.BASS_ChannelPlay(sSEStream, FALSE);
            BASS.BASS_ChannelSlideAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f, 150);
            BASS.BASS_ChannelSlideAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f, 300);
            sSE1PlayingFlag = true;
        }
    }

    private static final BASS.SYNCPROC endRiver = new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, final Object user) {
            EffectFragment.onRiverEnded();
        }
    };

    private static void onRiverEnded() {
        Context context = sActivity != null ? sActivity : MainActivity.sService;
        if (sSE1PlayingFlag) {
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = context.getResources().openRawResource(R.raw.river);
            sSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
            BASS.BASS_ChannelSetAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(sSEStream2, BASS.BASS_ChannelSeconds2Bytes(sSEStream2, 0.0), BASS.BASS_POS_BYTE);
            if (sSync != 0) {
                BASS.BASS_ChannelRemoveSync(sSEStream, sSync);
                sSync = 0;
            }
            sSync = BASS.BASS_ChannelSetSync(sSEStream2, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream2, 60.0), endRiver, null);
            BASS.BASS_ChannelPlay(sSEStream2, FALSE);
            BASS.BASS_ChannelSlideAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f, 5000);
            BASS.BASS_ChannelSlideAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, 0.0f, 5000);
            sSE1PlayingFlag = false;
        } else if (BASS.BASS_ChannelIsActive(sSEStream2) == BASS.BASS_ACTIVE_PLAYING) {
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = context.getResources().openRawResource(R.raw.river);
            sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
            BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(sSEStream, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 0.0), BASS.BASS_POS_BYTE);
            if (sSync != 0) {
                BASS.BASS_ChannelRemoveSync(sSEStream2, sSync);
                sSync = 0;
            }
            sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 60.0), endRiver, null);
            BASS.BASS_ChannelPlay(sSEStream, FALSE);
            BASS.BASS_ChannelSlideAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f, 5000);
            BASS.BASS_ChannelSlideAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f, 5000);
            sSE1PlayingFlag = true;
        }
    }

    private static final BASS.SYNCPROC endWar = new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, final Object user) {
            EffectFragment.onWarEnded();
        }
    };

    private static void onWarEnded() {
        Context context = sActivity != null ? sActivity : MainActivity.sService;
        if (sSE1PlayingFlag) {
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = context.getResources().openRawResource(R.raw.war);
            sSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
            BASS.BASS_ChannelSetAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(sSEStream2, BASS.BASS_ChannelSeconds2Bytes(sSEStream2, 0.0), BASS.BASS_POS_BYTE);
            if (sSync != 0) {
                BASS.BASS_ChannelRemoveSync(sSEStream, sSync);
                sSync = 0;
            }
            sSync = BASS.BASS_ChannelSetSync(sSEStream2, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream2, 30.0), endWar, null);
            BASS.BASS_ChannelPlay(sSEStream2, FALSE);
            BASS.BASS_ChannelSlideAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f, 1000);
            BASS.BASS_ChannelSlideAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            sSE1PlayingFlag = false;
        } else if (BASS.BASS_ChannelIsActive(sSEStream2) == BASS.BASS_ACTIVE_PLAYING) {
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = context.getResources().openRawResource(R.raw.war);
            sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
            BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(sSEStream, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 0.0), BASS.BASS_POS_BYTE);
            if (sSync != 0) {
                BASS.BASS_ChannelRemoveSync(sSEStream2, sSync);
                sSync = 0;
            }
            sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 30.0), endWar, null);
            BASS.BASS_ChannelPlay(sSEStream, FALSE);
            BASS.BASS_ChannelSlideAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f, 1000);
            BASS.BASS_ChannelSlideAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            sSE1PlayingFlag = true;
        }
    }

    private static final BASS.SYNCPROC endFire = new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, final Object user) {
            EffectFragment.onFireEnded();
        }
    };

    private static void onFireEnded() {
        Context context = sActivity != null ? sActivity : MainActivity.sService;
        if (sSE1PlayingFlag) {
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = context.getResources().openRawResource(R.raw.fire);
            sSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
            BASS.BASS_ChannelSetAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(sSEStream2, BASS.BASS_ChannelSeconds2Bytes(sSEStream2, 0.0), BASS.BASS_POS_BYTE);
            if (sSync != 0) {
                BASS.BASS_ChannelRemoveSync(sSEStream, sSync);
                sSync = 0;
            }
            sSync = BASS.BASS_ChannelSetSync(sSEStream2, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream2, 10.0), endFire, null);
            BASS.BASS_ChannelPlay(sSEStream2, FALSE);
            BASS.BASS_ChannelSlideAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f, 5000);
            BASS.BASS_ChannelSlideAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, 0.0f, 5000);
            sSE1PlayingFlag = false;
        } else if (BASS.BASS_ChannelIsActive(sSEStream2) == BASS.BASS_ACTIVE_PLAYING) {
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = context.getResources().openRawResource(R.raw.fire);
            sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
            BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(sSEStream, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 0.0), BASS.BASS_POS_BYTE);
            if (sSync != 0) {
                BASS.BASS_ChannelRemoveSync(sSEStream2, sSync);
                sSync = 0;
            }
            sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 10.0), endFire, null);
            BASS.BASS_ChannelPlay(sSEStream, FALSE);
            BASS.BASS_ChannelSlideAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, sSoundEffectVolume / 100.0f, 5000);
            BASS.BASS_ChannelSlideAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f, 5000);
            sSE1PlayingFlag = true;
        }
    }

    private static final BASS.SYNCPROC endCheer = new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, final Object user) {
            EffectFragment.onCheerEnded();
        }
    };

    private static void onCheerEnded() {
        Context context = sActivity != null ? sActivity : MainActivity.sService;
        if (sSE1PlayingFlag) {
            BASS.FloatValue vol = new BASS.FloatValue();
            BASS.BASS_ChannelGetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, vol);
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = context.getResources().openRawResource(R.raw.cheer);
            sSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
            BASS.BASS_ChannelSetAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(sSEStream2, BASS.BASS_ChannelSeconds2Bytes(sSEStream2, 1.0), BASS.BASS_POS_BYTE);
            if (sSync != 0) {
                BASS.BASS_ChannelRemoveSync(sSEStream, sSync);
                sSync = 0;
            }
            sSync = BASS.BASS_ChannelSetSync(sSEStream2, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream2, 14.0), endCheer, null);
            BASS.BASS_ChannelPlay(sSEStream2, FALSE);
            BASS.BASS_ChannelSlideAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, vol.value, 1000);
            BASS.BASS_ChannelSlideAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            sSE1PlayingFlag = false;
        } else if (BASS.BASS_ChannelIsActive(sSEStream2) == BASS.BASS_ACTIVE_PLAYING) {
            BASS.FloatValue vol = new BASS.FloatValue();
            BASS.BASS_ChannelGetAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, vol);
            MainActivity.FileProcsParams params = new MainActivity.FileProcsParams();
            params.inputStream = context.getResources().openRawResource(R.raw.cheer);
            sSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, BASS_FX.BASS_FX_FREESOURCE, MainActivity.fileProcs, params);
            BASS.BASS_ChannelSetAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(sSEStream, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 1.0), BASS.BASS_POS_BYTE);
            if (sSync != 0) {
                BASS.BASS_ChannelRemoveSync(sSEStream2, sSync);
                sSync = 0;
            }
            sSync = BASS.BASS_ChannelSetSync(sSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sSEStream, 14.0), endCheer, null);
            BASS.BASS_ChannelPlay(sSEStream, FALSE);
            BASS.BASS_ChannelSlideAttribute(sSEStream, BASS.BASS_ATTRIB_VOL, vol.value, 1000);
            BASS.BASS_ChannelSlideAttribute(sSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            sSE1PlayingFlag = true;
        }
    }

    void playMetronome() {
        final MediaPlayer mp = MediaPlayer.create(sActivity, R.raw.click);
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
                mp.release();
            }
        });
    }

    private static final BASS.DSPPROC vocalCancelDSP = new BASS.DSPPROC() {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            for (int a = 0; a < length / 4; a += 2) {
                b[a] = b[a + 1] = (-b[a] + b[a + 1]) * 0.5f;
            }
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    private static final BASS.DSPPROC monoralDSP = new BASS.DSPPROC() {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            for (int a = 0; a < length / 4; a += 2)
                b[a] = b[a + 1] = (b[a] + b[a + 1]) * 0.5f;
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    private static final BASS.DSPPROC leftDSP = new BASS.DSPPROC() {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            for (int a = 0; a < length / 4; a += 2)
                b[a + 1] = b[a];
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    private static final BASS.DSPPROC rightDSP = new BASS.DSPPROC() {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            for (int a = 0; a < length / 4; a += 2)
                b[a] = b[a + 1];
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    private static final BASS.DSPPROC exchangeDSP = new BASS.DSPPROC() {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            for (int a = 0; a < length / 4; a += 2) {
                float fTemp = b[a];
                b[a] = b[a + 1];
                b[a + 1] = fTemp;
            }
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    private static final int ECHBUFLEN = 1200;
    private static final float[][] echbuf = new float[ECHBUFLEN][2];
    private static int echpos;
    private static final BASS.DSPPROC doublingDSP = new BASS.DSPPROC() {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            for (int a = 0; a < length / 4; a += 2) {
                float l = echbuf[echpos][0];
                float r = (b[a] + b[a + 1]) * 0.5f;
                echbuf[echpos][0] = (b[a] + b[a + 1]) * 0.5f;
                echbuf[echpos][1] = (b[a] + b[a + 1]) * 0.5f;
                b[a] = l;
                b[a + 1] = r;
                echpos++;
                if (echpos == ECHBUFLEN)
                    echpos = 0;
            }
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    private static final BASS.DSPPROC panDSP = new BASS.DSPPROC() {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            for (int a = 0; a < length / 4; a += 2) {
                if (EffectFragment.sPan > 0.0f)
                    b[a] = b[a] * (1.0f - EffectFragment.sPan);
                else
                    b[a + 1] = b[a + 1] * (1.0f + EffectFragment.sPan);
            }
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    private static final BASS.DSPPROC normalizeDSP = new BASS.DSPPROC() {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            if (EffectFragment.sPeak != 0.0f) {
                for (int a = 0; a < length / 4; a++)
                    b[a] /= EffectFragment.sPeak;
            }
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    private static final BASS.DSPPROC phaseReversalDSP = new BASS.DSPPROC() {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            for (int a = 0; a < length / 4; a++)
                b[a] = -b[a];
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            if (v.getId() == R.id.editTimeSpecifiedEffectDetail)
                showTimeEffectDialog();
            else if (v.getId() == R.id.editSpeedSpecifiedEffectDetail)
                showSpeedEffectDialog();
            else if (v.getId() == R.id.editSpeedLoopEffectDetail)
                showSpeedEffectDialog();
        }
    }

    private void showTimeEffectDialog() {
        TimeEffectDetailFragmentDialog dialog = new TimeEffectDetailFragmentDialog();
        FragmentManager fm = getFragmentManager();
        if (fm != null) dialog.show(fm, "span_setting_dialog");
    }

    private void showSpeedEffectDialog() {
        SpeedEffectDetailFragmentDialog dialog = new SpeedEffectDetailFragmentDialog();
        FragmentManager fm = getFragmentManager();
        if (fm != null) dialog.show(fm, "span_setting_dialog");
    }

    public void clearFocus() {
        mEditTimeSpecifiedEffectDetail.clearFocus();
        mEditSpeedSpecifiedEffectDetail.clearFocus();
        mEditPTimeSpecifiedEffectDetail.clearFocus();
        mEditPitchSpecifiedEffectDetail.clearFocus();
        mEditSpeedLoopEffectDetail.clearFocus();
        mEditPitchLoopEffectDetail.clearFocus();
    }

    private void showTemplateMenu() {
        final BottomMenu menu = new BottomMenu(sActivity);
        if (sEffectDetail == EFFECTTYPE_REVERB) menu.setTitle(getString(R.string.reverbTemplate));
        else if (sEffectDetail == EFFECTTYPE_ECHO) menu.setTitle(getString(R.string.echoTemplate));
        else if (sEffectDetail == EFFECTTYPE_CHORUS)
            menu.setTitle(getString(R.string.chorusTemplate));
        else if (sEffectDetail == EFFECTTYPE_DISTORTION)
            menu.setTitle(getString(R.string.distortionTemplate));
        else if (sEffectDetail == EFFECTTYPE_COMP) menu.setTitle(getString(R.string.compTemplate));
        else if (sEffectDetail == EFFECTTYPE_PAN) menu.setTitle(getString(R.string.panTemplate));
        else if (sEffectDetail == EFFECTTYPE_FREQ) menu.setTitle(getString(R.string.freqTemplate));
        else if (sEffectDetail == EFFECTTYPE_METRONOME) menu.setTitle(getString(R.string.metronomeTemplate));
        menu.addMenu(getString(R.string.sortTemplate), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_sort_dark : R.drawable.ic_actionsheet_sort, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                mRecyclerEffectTemplates.setPadding(0, 0, 0, (int) (64 * sActivity.getDensity()));
                mBtnEffectTemplateOff.setVisibility(View.GONE);
                mBtnEffectTemplateMenu.setVisibility(View.GONE);

                mRelativeEffectTitle.setVisibility(View.GONE);
                mViewSepEffectDetail.setVisibility(View.GONE);
                mViewSepEffectTemplateHeader.setVisibility(View.GONE);
                if (mBtnAddEffectTemplate.getVisibility() == View.VISIBLE)
                    mBtnAddEffectTemplate.setAlpha(0.0f);
                mTextFinishSortEffect.setVisibility(View.VISIBLE);
                mSorting = true;
                mEffectTemplatesAdapter.notifyDataSetChanged();

                mEffectTemplateTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView mRecyclerEqualizers, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        final int fromPos = viewHolder.getAdapterPosition();
                        final int toPos = target.getAdapterPosition();

                        ArrayList<EffectTemplateItem> items = null;
                        if (sEffectDetail == EFFECTTYPE_REVERB) items = sReverbItems;
                        else if (sEffectDetail == EFFECTTYPE_ECHO) items = sEchoItems;
                        else if (sEffectDetail == EFFECTTYPE_CHORUS) items = sChorusItems;
                        else if (sEffectDetail == EFFECTTYPE_DISTORTION) items = sDistortionItems;
                        else if (sEffectDetail == EFFECTTYPE_COMP) items = sCompItems;
                        else if (sEffectDetail == EFFECTTYPE_PAN) items = sPanItems;
                        else if (sEffectDetail == EFFECTTYPE_FREQ) items = sFreqItems;
                        else if (sEffectDetail == EFFECTTYPE_METRONOME) items = sMetronomeItems;

                        EffectTemplateItem itemTemp = items.get(fromPos);
                        items.remove(fromPos);
                        items.add(toPos, itemTemp);

                        mEffectTemplatesAdapter.notifyItemMoved(fromPos, toPos);

                        if (sEffectDetail == EFFECTTYPE_REVERB) {
                            if (fromPos == sReverbSelected) sReverbSelected = toPos;
                            else if (fromPos < sReverbSelected && sReverbSelected <= toPos)
                                sReverbSelected--;
                            else if (fromPos > sReverbSelected && sReverbSelected >= toPos)
                                sReverbSelected++;
                        }
                        else if (sEffectDetail == EFFECTTYPE_ECHO) {
                            if (fromPos == sEchoSelected) sEchoSelected = toPos;
                            else if (fromPos < sEchoSelected && sEchoSelected <= toPos)
                                sEchoSelected--;
                            else if (fromPos > sEchoSelected && sEchoSelected >= toPos)
                                sEchoSelected++;
                        }
                        else if (sEffectDetail == EFFECTTYPE_CHORUS) {
                            if (fromPos == sChorusSelected) sChorusSelected = toPos;
                            else if (fromPos < sChorusSelected && sChorusSelected <= toPos)
                                sChorusSelected--;
                            else if (fromPos > sChorusSelected && sChorusSelected >= toPos)
                                sChorusSelected++;
                        }
                        else if (sEffectDetail == EFFECTTYPE_DISTORTION) {
                            if (fromPos == sDistortionSelected) sDistortionSelected = toPos;
                            else if (fromPos < sDistortionSelected && sDistortionSelected <= toPos)
                                sDistortionSelected--;
                            else if (fromPos > sDistortionSelected && sDistortionSelected >= toPos)
                                sDistortionSelected++;
                        }
                        else if (sEffectDetail == EFFECTTYPE_COMP) {
                            if (fromPos == sCompSelected) sCompSelected = toPos;
                            else if (fromPos < sCompSelected && sCompSelected <= toPos)
                                sCompSelected--;
                            else if (fromPos > sCompSelected && sCompSelected >= toPos)
                                sCompSelected++;
                        }
                        else if (sEffectDetail == EFFECTTYPE_PAN) {
                            if (fromPos == sPanSelected) sPanSelected = toPos;
                            else if (fromPos < sPanSelected && sPanSelected <= toPos)
                                sPanSelected--;
                            else if (fromPos > sPanSelected && sPanSelected >= toPos)
                                sPanSelected++;
                        }
                        else if (sEffectDetail == EFFECTTYPE_FREQ) {
                            if (fromPos == sFreqSelected) sFreqSelected = toPos;
                            else if (fromPos < sFreqSelected && sFreqSelected <= toPos)
                                sFreqSelected--;
                            else if (fromPos > sFreqSelected && sFreqSelected >= toPos)
                                sFreqSelected++;
                        }
                        else if (sEffectDetail == EFFECTTYPE_METRONOME) {
                            if (fromPos == sMetronomeSelected) sMetronomeSelected = toPos;
                            else if (fromPos < sMetronomeSelected && sMetronomeSelected <= toPos)
                                sMetronomeSelected--;
                            else if (fromPos > sMetronomeSelected && sMetronomeSelected >= toPos)
                                sMetronomeSelected++;
                        }

                        for (int i = 0; i < PlaylistFragment.sPlaylists.size(); i++) {

                            ArrayList<SongItem> arSongs = PlaylistFragment.sPlaylists.get(i);
                            ArrayList<EffectSaver> arEffects = PlaylistFragment.sEffects.get(i);
                            for (int j = 0; j < arSongs.size(); j++) {
                                EffectSaver saver = arEffects.get(j);
                                if (saver.isSave()) {
                                    if (sEffectDetail == EFFECTTYPE_REVERB) {
                                        if (fromPos == saver.getReverbSelected())
                                            saver.setReverbSelected(toPos);
                                        else if (fromPos < saver.getReverbSelected() && saver.getReverbSelected() <= toPos)
                                            saver.setReverbSelected(saver.getReverbSelected() - 1);
                                        else if (fromPos > saver.getReverbSelected() && saver.getReverbSelected() >= toPos)
                                            saver.setReverbSelected(saver.getReverbSelected() + 1);
                                    }
                                    else if (sEffectDetail == EFFECTTYPE_ECHO) {
                                        if (fromPos == saver.getEchoSelected())
                                            saver.setEchoSelected(toPos);
                                        else if (fromPos < saver.getEchoSelected() && saver.getEchoSelected() <= toPos)
                                            saver.setEchoSelected(saver.getEchoSelected() - 1);
                                        else if (fromPos > saver.getEchoSelected() && saver.getEchoSelected() >= toPos)
                                            saver.setEchoSelected(saver.getEchoSelected() + 1);
                                    }
                                    else if (sEffectDetail == EFFECTTYPE_CHORUS) {
                                        if (fromPos == saver.getChorusSelected())
                                            saver.setChorusSelected(toPos);
                                        else if (fromPos < saver.getChorusSelected() && saver.getChorusSelected() <= toPos)
                                            saver.setChorusSelected(saver.getChorusSelected() - 1);
                                        else if (fromPos > saver.getChorusSelected() && saver.getChorusSelected() >= toPos)
                                            saver.setChorusSelected(saver.getChorusSelected() + 1);
                                    }
                                    else if (sEffectDetail == EFFECTTYPE_DISTORTION) {
                                        if (fromPos == saver.getDistortionSelected())
                                            saver.setDistortionSelected(toPos);
                                        else if (fromPos < saver.getDistortionSelected() && saver.getDistortionSelected() <= toPos)
                                            saver.setDistortionSelected(saver.getDistortionSelected() - 1);
                                        else if (fromPos > saver.getDistortionSelected() && saver.getDistortionSelected() >= toPos)
                                            saver.setDistortionSelected(saver.getDistortionSelected() + 1);
                                    }
                                    else if (sEffectDetail == EFFECTTYPE_COMP) {
                                        if (fromPos == saver.getCompSelected())
                                            saver.setCompSelected(toPos);
                                        else if (fromPos < saver.getCompSelected() && saver.getCompSelected() <= toPos)
                                            saver.setCompSelected(saver.getCompSelected() - 1);
                                        else if (fromPos > saver.getCompSelected() && saver.getCompSelected() >= toPos)
                                            saver.setCompSelected(saver.getCompSelected() + 1);
                                    }
                                    else if (sEffectDetail == EFFECTTYPE_PAN) {
                                        if (fromPos == saver.getPanSelected())
                                            saver.setPanSelected(toPos);
                                        else if (fromPos < saver.getPanSelected() && saver.getPanSelected() <= toPos)
                                            saver.setPanSelected(saver.getPanSelected() - 1);
                                        else if (fromPos > saver.getPanSelected() && saver.getPanSelected() >= toPos)
                                            saver.setPanSelected(saver.getPanSelected() + 1);
                                    }
                                    else if (sEffectDetail == EFFECTTYPE_FREQ) {
                                        if (fromPos == saver.getFreqSelected())
                                            saver.setFreqSelected(toPos);
                                        else if (fromPos < saver.getFreqSelected() && saver.getFreqSelected() <= toPos)
                                            saver.setFreqSelected(saver.getFreqSelected() - 1);
                                        else if (fromPos > saver.getFreqSelected() && saver.getFreqSelected() >= toPos)
                                            saver.setFreqSelected(saver.getFreqSelected() + 1);
                                    }
                                    else if (sEffectDetail == EFFECTTYPE_METRONOME) {
                                        if (fromPos == saver.getMetronomeSelected())
                                            saver.setMetronomeSelected(toPos);
                                        else if (fromPos < saver.getMetronomeSelected() && saver.getMetronomeSelected() <= toPos)
                                            saver.setMetronomeSelected(saver.getMetronomeSelected() - 1);
                                        else if (fromPos > saver.getMetronomeSelected() && saver.getMetronomeSelected() >= toPos)
                                            saver.setMetronomeSelected(saver.getMetronomeSelected() + 1);
                                    }
                                }
                            }
                        }

                        return true;
                    }

                    @Override
                    public void clearView(@NonNull RecyclerView recyclerSongs, @NonNull RecyclerView.ViewHolder viewHolder) {
                        super.clearView(recyclerSongs, viewHolder);

                        mEffectTemplatesAdapter.notifyDataSetChanged();
                        saveData();
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }

                    @Override
                    public boolean isLongPressDragEnabled() {
                        return false;
                    }
                });
                mEffectTemplateTouchHelper.attachToRecyclerView(mRecyclerEffectTemplates);
            }
        });
        menu.addDestructiveMenu(getString(R.string.initializeTemplate), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_initialize_dark : R.drawable.ic_actionsheet_initialize, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder;
                if(sActivity.isDarkMode())
                    builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
                else
                    builder = new AlertDialog.Builder(sActivity);
                builder.setTitle(R.string.initializeTemplate);
                builder.setMessage(R.string.askinitializeTemplate);
                builder.setPositiveButton(R.string.decideNot, null);
                builder.setNegativeButton(R.string.doInitialize, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (sEffectDetail == EFFECTTYPE_REVERB) resetReverbs();
                        else if (sEffectDetail == EFFECTTYPE_ECHO) resetEchos();
                        else if (sEffectDetail == EFFECTTYPE_CHORUS) resetChoruses();
                        else if (sEffectDetail == EFFECTTYPE_DISTORTION) resetDistortions();
                        else if (sEffectDetail == EFFECTTYPE_COMP) resetComps();
                        else if (sEffectDetail == EFFECTTYPE_PAN) resetPans();
                        else if (sEffectDetail == EFFECTTYPE_FREQ) resetFreqs();
                        else if (sEffectDetail == EFFECTTYPE_METRONOME) resetMetronomes();
                    }
                });
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        if (alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        positiveButton.setTextColor(getResources().getColor(sActivity.isDarkMode() ? R.color.darkModeRed : R.color.lightModeRed));
                    }
                });
                alertDialog.show();
            }
        });
        menu.setCancelMenu();
        menu.show();
    }

    void showMenu(final int nItem) {
        final BottomMenu menu = new BottomMenu(sActivity);
        if (sEffectDetail == EFFECTTYPE_REVERB)
            menu.setTitle(sReverbItems.get(nItem).getEffectTemplateName());
        else if (sEffectDetail == EFFECTTYPE_ECHO)
            menu.setTitle(sEchoItems.get(nItem).getEffectTemplateName());
        else if (sEffectDetail == EFFECTTYPE_CHORUS)
            menu.setTitle(sChorusItems.get(nItem).getEffectTemplateName());
        else if (sEffectDetail == EFFECTTYPE_DISTORTION)
            menu.setTitle(sDistortionItems.get(nItem).getEffectTemplateName());
        else if (sEffectDetail == EFFECTTYPE_COMP)
            menu.setTitle(sCompItems.get(nItem).getEffectTemplateName());
        else if (sEffectDetail == EFFECTTYPE_PAN)
            menu.setTitle(sPanItems.get(nItem).getEffectTemplateName());
        else if (sEffectDetail == EFFECTTYPE_FREQ)
            menu.setTitle(sFreqItems.get(nItem).getEffectTemplateName());
        else if (sEffectDetail == EFFECTTYPE_METRONOME)
            menu.setTitle(sMetronomeItems.get(nItem).getEffectTemplateName());
        menu.addMenu(getString(R.string.changeTemplateName), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_edit_dark : R.drawable.ic_actionsheet_edit, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder;
                if(sActivity.isDarkMode())
                    builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
                else
                    builder = new AlertDialog.Builder(sActivity);
                builder.setTitle(R.string.changeTemplateName);
                LinearLayout linearLayout = new LinearLayout(sActivity);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                final ClearableEditText editPreset = new ClearableEditText(sActivity, sActivity.isDarkMode());
                editPreset.setHint(R.string.templateName);
                if (sEffectDetail == EFFECTTYPE_REVERB)
                    editPreset.setText(sReverbItems.get(nItem).getEffectTemplateName());
                else if (sEffectDetail == EFFECTTYPE_ECHO)
                    editPreset.setText(sEchoItems.get(nItem).getEffectTemplateName());
                else if (sEffectDetail == EFFECTTYPE_CHORUS)
                    editPreset.setText(sChorusItems.get(nItem).getEffectTemplateName());
                else if (sEffectDetail == EFFECTTYPE_DISTORTION)
                    editPreset.setText(sDistortionItems.get(nItem).getEffectTemplateName());
                else if (sEffectDetail == EFFECTTYPE_COMP)
                    editPreset.setText(sCompItems.get(nItem).getEffectTemplateName());
                else if (sEffectDetail == EFFECTTYPE_PAN)
                    editPreset.setText(sPanItems.get(nItem).getEffectTemplateName());
                else if (sEffectDetail == EFFECTTYPE_FREQ)
                    editPreset.setText(sFreqItems.get(nItem).getEffectTemplateName());
                else if (sEffectDetail == EFFECTTYPE_METRONOME)
                    editPreset.setText(sMetronomeItems.get(nItem).getEffectTemplateName());
                linearLayout.addView(editPreset);
                builder.setView(linearLayout);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (sEffectDetail == EFFECTTYPE_REVERB)
                            sReverbItems.get(nItem).setEffectTemplateName(editPreset.getText().toString());
                        else if (sEffectDetail == EFFECTTYPE_ECHO)
                            sEchoItems.get(nItem).setEffectTemplateName(editPreset.getText().toString());
                        else if (sEffectDetail == EFFECTTYPE_CHORUS)
                            sChorusItems.get(nItem).setEffectTemplateName(editPreset.getText().toString());
                        else if (sEffectDetail == EFFECTTYPE_DISTORTION)
                            sDistortionItems.get(nItem).setEffectTemplateName(editPreset.getText().toString());
                        else if (sEffectDetail == EFFECTTYPE_COMP)
                            sCompItems.get(nItem).setEffectTemplateName(editPreset.getText().toString());
                        else if (sEffectDetail == EFFECTTYPE_PAN)
                            sPanItems.get(nItem).setEffectTemplateName(editPreset.getText().toString());
                        else if (sEffectDetail == EFFECTTYPE_FREQ)
                            sFreqItems.get(nItem).setEffectTemplateName(editPreset.getText().toString());
                        else if (sEffectDetail == EFFECTTYPE_METRONOME)
                            sMetronomeItems.get(nItem).setEffectTemplateName(editPreset.getText().toString());
                        mEffectTemplatesAdapter.notifyItemChanged(nItem);
                        saveData();
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        if (alertDialog.getWindow() != null) {
                            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                            lp.dimAmount = 0.4f;
                            alertDialog.getWindow().setAttributes(lp);
                        }
                        editPreset.requestFocus();
                        editPreset.setSelection(editPreset.getText().toString().length());
                        InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (null != imm) imm.showSoftInput(editPreset, 0);
                    }
                });
                alertDialog.show();
            }
        });
        menu.addMenu(getString(R.string.copy), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_copy_dark : R.drawable.ic_actionsheet_copy, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                EffectTemplateItem item;
                if (sEffectDetail == EFFECTTYPE_REVERB) {
                    item = sReverbItems.get(nItem);
                    sReverbItems.add(nItem + 1, new EffectTemplateItem(item.getEffectTemplateName(), new ArrayList<>(Arrays.asList(item.getArPresets().get(0), item.getArPresets().get(1), item.getArPresets().get(2), item.getArPresets().get(3), item.getArPresets().get(4)))));
                } else if (sEffectDetail == EFFECTTYPE_ECHO) {
                    item = sEchoItems.get(nItem);
                    sEchoItems.add(nItem + 1, new EffectTemplateItem(item.getEffectTemplateName(), new ArrayList<>(Arrays.asList(item.getArPresets().get(0), item.getArPresets().get(1), item.getArPresets().get(2), item.getArPresets().get(3)))));
                } else if (sEffectDetail == EFFECTTYPE_CHORUS) {
                    item = sChorusItems.get(nItem);
                    sChorusItems.add(nItem + 1, new EffectTemplateItem(item.getEffectTemplateName(), new ArrayList<>(Arrays.asList(item.getArPresets().get(0), item.getArPresets().get(1), item.getArPresets().get(2), item.getArPresets().get(3), item.getArPresets().get(4), item.getArPresets().get(5)))));
                } else if (sEffectDetail == EFFECTTYPE_DISTORTION) {
                    item = sDistortionItems.get(nItem);
                    sDistortionItems.add(nItem + 1, new EffectTemplateItem(item.getEffectTemplateName(), new ArrayList<>(Arrays.asList(item.getArPresets().get(0), item.getArPresets().get(1), item.getArPresets().get(2), item.getArPresets().get(3), item.getArPresets().get(4)))));
                } else if (sEffectDetail == EFFECTTYPE_COMP) {
                    item = sCompItems.get(nItem);
                    sCompItems.add(nItem + 1, new EffectTemplateItem(item.getEffectTemplateName(), new ArrayList<>(Arrays.asList(item.getArPresets().get(0), item.getArPresets().get(1), item.getArPresets().get(2), item.getArPresets().get(3), item.getArPresets().get(4)))));
                } else if (sEffectDetail == EFFECTTYPE_PAN) {
                    item = sPanItems.get(nItem);
                    sPanItems.add(nItem + 1, new EffectTemplateItem(item.getEffectTemplateName(), new ArrayList<>(Arrays.asList(item.getArPresets().get(0)))));
                } else if (sEffectDetail == EFFECTTYPE_FREQ) {
                    item = sFreqItems.get(nItem);
                    sFreqItems.add(nItem + 1, new EffectTemplateItem(item.getEffectTemplateName(), new ArrayList<>(Arrays.asList(item.getArPresets().get(0)))));
                } else if (sEffectDetail == EFFECTTYPE_METRONOME) {
                    item = sMetronomeItems.get(nItem);
                    sMetronomeItems.add(nItem + 1, new EffectTemplateItem(item.getEffectTemplateName(), new ArrayList<>(Arrays.asList(item.getArPresets().get(0)))));
                } else return;
                mEffectTemplatesAdapter.notifyItemInserted(nItem + 1);

                if (sEffectDetail == EFFECTTYPE_REVERB) {
                    if (nItem < sReverbSelected) sReverbSelected++;
                } else if (sEffectDetail == EFFECTTYPE_ECHO) {
                    if (nItem < sEchoSelected) sEchoSelected++;
                } else if (sEffectDetail == EFFECTTYPE_CHORUS) {
                    if (nItem < sChorusSelected) sChorusSelected++;
                } else if (sEffectDetail == EFFECTTYPE_DISTORTION) {
                    if (nItem < sDistortionSelected) sDistortionSelected++;
                } else if (sEffectDetail == EFFECTTYPE_COMP) {
                    if (nItem < sCompSelected) sCompSelected++;
                } else if (sEffectDetail == EFFECTTYPE_PAN) {
                    if (nItem < sPanSelected) sPanSelected++;
                } else if (sEffectDetail == EFFECTTYPE_FREQ) {
                    if (nItem < sFreqSelected) sFreqSelected++;
                } else if (sEffectDetail == EFFECTTYPE_METRONOME) {
                    if (nItem < sMetronomeSelected) sMetronomeSelected++;
                }

                for (int i = 0; i < PlaylistFragment.sPlaylists.size(); i++) {

                    ArrayList<SongItem> arSongs = PlaylistFragment.sPlaylists.get(i);
                    ArrayList<EffectSaver> arEffects = PlaylistFragment.sEffects.get(i);
                    for (int j = 0; j < arSongs.size(); j++) {
                        EffectSaver saver = arEffects.get(j);
                        if (saver.isSave()) {
                            if (sEffectDetail == EFFECTTYPE_REVERB) {
                                if (nItem < saver.getReverbSelected())
                                    saver.setReverbSelected(saver.getReverbSelected() + 1);
                            } else if (sEffectDetail == EFFECTTYPE_ECHO) {
                                if (nItem < saver.getEchoSelected())
                                    saver.setEchoSelected(saver.getEchoSelected() + 1);
                            } else if (sEffectDetail == EFFECTTYPE_CHORUS) {
                                if (nItem < saver.getChorusSelected())
                                    saver.setChorusSelected(saver.getChorusSelected() + 1);
                            } else if (sEffectDetail == EFFECTTYPE_DISTORTION) {
                                if (nItem < saver.getDistortionSelected())
                                    saver.setDistortionSelected(saver.getDistortionSelected() + 1);
                            } else if (sEffectDetail == EFFECTTYPE_COMP) {
                                if (nItem < saver.getCompSelected())
                                    saver.setCompSelected(saver.getCompSelected() + 1);
                            } else if (sEffectDetail == EFFECTTYPE_PAN) {
                                if (nItem < saver.getPanSelected())
                                    saver.setPanSelected(saver.getPanSelected() + 1);
                            } else if (sEffectDetail == EFFECTTYPE_FREQ) {
                                if (nItem < saver.getFreqSelected())
                                    saver.setFreqSelected(saver.getFreqSelected() + 1);
                            } else if (sEffectDetail == EFFECTTYPE_METRONOME) {
                                if (nItem < saver.getMetronomeSelected())
                                    saver.setMetronomeSelected(saver.getMetronomeSelected() + 1);
                            }
                        }
                    }
                }

                saveData();
                mRecyclerEffectTemplates.scrollToPosition(nItem + 1);
            }
        });
        menu.addDestructiveMenu(getString(R.string.delete), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_delete_dark : R.drawable.ic_actionsheet_delete, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                askDeletePreset(nItem);
            }
        });
        menu.setCancelMenu();
        menu.show();
    }

    private void askDeletePreset(final int item) {
        AlertDialog.Builder builder;
        if(sActivity.isDarkMode())
            builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
        else
            builder = new AlertDialog.Builder(sActivity);
        if (sEffectDetail == EFFECTTYPE_REVERB)
            builder.setTitle(sReverbItems.get(item).getEffectTemplateName());
        else if (sEffectDetail == EFFECTTYPE_ECHO)
            builder.setTitle(sEchoItems.get(item).getEffectTemplateName());
        else if (sEffectDetail == EFFECTTYPE_CHORUS)
            builder.setTitle(sChorusItems.get(item).getEffectTemplateName());
        else if (sEffectDetail == EFFECTTYPE_DISTORTION)
            builder.setTitle(sDistortionItems.get(item).getEffectTemplateName());
        else if (sEffectDetail == EFFECTTYPE_COMP)
            builder.setTitle(sCompItems.get(item).getEffectTemplateName());
        else if (sEffectDetail == EFFECTTYPE_PAN)
            builder.setTitle(sPanItems.get(item).getEffectTemplateName());
        else if (sEffectDetail == EFFECTTYPE_FREQ)
            builder.setTitle(sFreqItems.get(item).getEffectTemplateName());
        else if (sEffectDetail == EFFECTTYPE_METRONOME)
            builder.setTitle(sMetronomeItems.get(item).getEffectTemplateName());
        builder.setMessage(R.string.askDeleteTemplate);
        builder.setPositiveButton(R.string.decideNot, null);
        builder.setNegativeButton(R.string.doDelete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (sEffectDetail == EFFECTTYPE_REVERB) removeReverbItem(item);
                else if (sEffectDetail == EFFECTTYPE_ECHO) removeEchoItem(item);
                else if (sEffectDetail == EFFECTTYPE_CHORUS) removeChorusItem(item);
                else if (sEffectDetail == EFFECTTYPE_DISTORTION)
                    removeDistortionItem(item);
                else if (sEffectDetail == EFFECTTYPE_COMP) removeCompItem(item);
                else if (sEffectDetail == EFFECTTYPE_PAN) removePanItem(item);
                else if (sEffectDetail == EFFECTTYPE_FREQ) removeFreqItem(item);
                else if (sEffectDetail == EFFECTTYPE_METRONOME) removeMetronomeItem(item);
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                if (alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
                Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                positiveButton.setTextColor(getResources().getColor(sActivity.isDarkMode() ? R.color.darkModeRed : R.color.lightModeRed));
            }
        });
        alertDialog.show();
    }

    private void removeReverbItem(int nItem) {
        sReverbItems.remove(nItem);
        mEffectTemplatesAdapter.notifyItemRemoved(nItem);

        if (nItem == sReverbSelected) resetReverb();
        else if (nItem < sReverbSelected) sReverbSelected--;

        for (int i = 0; i < PlaylistFragment.sPlaylists.size(); i++) {

            ArrayList<SongItem> arSongs = PlaylistFragment.sPlaylists.get(i);
            ArrayList<EffectSaver> arEffects = PlaylistFragment.sEffects.get(i);
            for (int j = 0; j < arSongs.size(); j++) {
                EffectSaver saver = arEffects.get(j);
                if (saver.isSave()) {
                    if (nItem == saver.getReverbSelected()) {
                        saver.setReverbSelected(-1);
                        saver.setReverbDry(1.0f);
                        saver.setReverbWet(0.0f);
                        saver.setReverbRoomSize(0.0f);
                        saver.setReverbDamp(0.0f);
                        saver.setReverbWidth(0.0f);
                    } else if (nItem < saver.getReverbSelected())
                        saver.setReverbSelected(saver.getReverbSelected() - 1);
                }
            }
        }
        saveData();
    }

    private void removeEchoItem(int nItem) {
        sEchoItems.remove(nItem);
        mEffectTemplatesAdapter.notifyItemRemoved(nItem);

        if (nItem == sEchoSelected) resetEcho();
        else if (nItem < sEchoSelected) sEchoSelected--;

        for (int i = 0; i < PlaylistFragment.sPlaylists.size(); i++) {

            ArrayList<SongItem> arSongs = PlaylistFragment.sPlaylists.get(i);
            ArrayList<EffectSaver> arEffects = PlaylistFragment.sEffects.get(i);
            for (int j = 0; j < arSongs.size(); j++) {
                EffectSaver saver = arEffects.get(j);
                if (saver.isSave()) {
                    if (nItem == saver.getEchoSelected()) {
                        saver.setEchoSelected(-1);
                        saver.setEchoDry(1.0f);
                        saver.setEchoWet(0.0f);
                        saver.setEchoFeedback(0.0f);
                        saver.setEchoDelay(0.0f);
                    } else if (nItem < saver.getEchoSelected())
                        saver.setEchoSelected(saver.getEchoSelected() - 1);
                }
            }
        }
        saveData();
    }

    private void removeChorusItem(int nItem) {
        sChorusItems.remove(nItem);
        mEffectTemplatesAdapter.notifyItemRemoved(nItem);

        if (nItem == sChorusSelected) resetChorus();
        else if (nItem < sChorusSelected) sChorusSelected--;

        for (int i = 0; i < PlaylistFragment.sPlaylists.size(); i++) {

            ArrayList<SongItem> arSongs = PlaylistFragment.sPlaylists.get(i);
            ArrayList<EffectSaver> arEffects = PlaylistFragment.sEffects.get(i);
            for (int j = 0; j < arSongs.size(); j++) {
                EffectSaver saver = arEffects.get(j);
                if (saver.isSave()) {
                    if (nItem == saver.getChorusSelected()) {
                        saver.setChorusSelected(-1);
                        saver.setChorusDry(1.0f);
                        saver.setChorusWet(0.0f);
                        saver.setChorusFeedback(0.0f);
                        saver.setChorusMinSweep(0.0f);
                        saver.setChorusMaxSweep(0.0f);
                        saver.setChorusRate(0.0f);
                    } else if (nItem < saver.getChorusSelected())
                        saver.setChorusSelected(saver.getChorusSelected() - 1);
                }
            }
        }
        saveData();
    }

    private void removeDistortionItem(int nItem) {
        sDistortionItems.remove(nItem);
        mEffectTemplatesAdapter.notifyItemRemoved(nItem);

        if (nItem == sDistortionSelected) resetDistortion();
        else if (nItem < sDistortionSelected) sDistortionSelected--;

        for (int i = 0; i < PlaylistFragment.sPlaylists.size(); i++) {

            ArrayList<SongItem> arSongs = PlaylistFragment.sPlaylists.get(i);
            ArrayList<EffectSaver> arEffects = PlaylistFragment.sEffects.get(i);
            for (int j = 0; j < arSongs.size(); j++) {
                EffectSaver saver = arEffects.get(j);
                if (saver.isSave()) {
                    if (nItem == saver.getDistortionSelected()) {
                        saver.setDistortionSelected(-1);
                        saver.setDistortionDrive(0.0f);
                        saver.setDistortionDry(1.0f);
                        saver.setDistortionWet(0.0f);
                        saver.setDistortionFeedback(0.0f);
                        saver.setDistortionVolume(0.0f);
                    } else if (nItem < saver.getDistortionSelected())
                        saver.setDistortionSelected(saver.getDistortionSelected() - 1);
                }
            }
        }
        saveData();
    }

    private void removeCompItem(int nItem) {
        sCompItems.remove(nItem);
        mEffectTemplatesAdapter.notifyItemRemoved(nItem);

        if (nItem == sCompSelected) resetComp();
        else if (nItem < sCompSelected) sCompSelected--;

        for (int i = 0; i < PlaylistFragment.sPlaylists.size(); i++) {

            ArrayList<SongItem> arSongs = PlaylistFragment.sPlaylists.get(i);
            ArrayList<EffectSaver> arEffects = PlaylistFragment.sEffects.get(i);
            for (int j = 0; j < arSongs.size(); j++) {
                EffectSaver saver = arEffects.get(j);
                if (saver.isSave()) {
                    if (nItem == saver.getCompSelected()) {
                        saver.setCompSelected(-1);
                        saver.setCompGain(0.0f);
                        saver.setCompThreshold(1.0f);
                        saver.setCompRatio(0.0f);
                        saver.setCompAttack(0.0f);
                        saver.setCompRelease(0.0f);
                    } else if (nItem < saver.getCompSelected())
                        saver.setCompSelected(saver.getCompSelected() - 1);
                }
            }
        }
        saveData();
    }

    private void removePanItem(int nItem) {
        sPanItems.remove(nItem);
        mEffectTemplatesAdapter.notifyItemRemoved(nItem);

        if (nItem == sPanSelected) resetPan();
        else if (nItem < sPanSelected) sPanSelected--;

        for (int i = 0; i < PlaylistFragment.sPlaylists.size(); i++) {

            ArrayList<SongItem> arSongs = PlaylistFragment.sPlaylists.get(i);
            ArrayList<EffectSaver> arEffects = PlaylistFragment.sEffects.get(i);
            for (int j = 0; j < arSongs.size(); j++) {
                EffectSaver saver = arEffects.get(j);
                if (saver.isSave()) {
                    if (nItem == saver.getPanSelected()) {
                        saver.setPanSelected(-1);
                        saver.setPan(0.0f);
                    } else if (nItem < saver.getPanSelected())
                        saver.setPanSelected(saver.getPanSelected() - 1);
                }
            }
        }
        saveData();
    }

    private void removeFreqItem(int nItem) {
        sFreqItems.remove(nItem);
        mEffectTemplatesAdapter.notifyItemRemoved(nItem);

        if (nItem == sFreqSelected) resetFreq();
        else if (nItem < sFreqSelected) sFreqSelected--;

        for (int i = 0; i < PlaylistFragment.sPlaylists.size(); i++) {

            ArrayList<SongItem> arSongs = PlaylistFragment.sPlaylists.get(i);
            ArrayList<EffectSaver> arEffects = PlaylistFragment.sEffects.get(i);
            for (int j = 0; j < arSongs.size(); j++) {
                EffectSaver saver = arEffects.get(j);
                if (saver.isSave()) {
                    if (nItem == saver.getFreqSelected()) {
                        saver.setFreqSelected(-1);
                        saver.setFreq(0.0f);
                    } else if (nItem < saver.getFreqSelected())
                        saver.setFreqSelected(saver.getFreqSelected() - 1);
                }
            }
        }
        saveData();
    }

    private void removeMetronomeItem(int nItem) {
        sMetronomeItems.remove(nItem);
        mEffectTemplatesAdapter.notifyItemRemoved(nItem);

        if (nItem == sMetronomeSelected) resetMetronome();
        else if (nItem < sMetronomeSelected) sMetronomeSelected--;

        for (int i = 0; i < PlaylistFragment.sPlaylists.size(); i++) {

            ArrayList<SongItem> arSongs = PlaylistFragment.sPlaylists.get(i);
            ArrayList<EffectSaver> arEffects = PlaylistFragment.sEffects.get(i);
            for (int j = 0; j < arSongs.size(); j++) {
                EffectSaver saver = arEffects.get(j);
                if (saver.isSave()) {
                    if (nItem == saver.getMetronomeSelected()) {
                        saver.setMetronomeSelected(-1);
                        saver.setBPM(0);
                    } else if (nItem < saver.getMetronomeSelected())
                        saver.setMetronomeSelected(saver.getMetronomeSelected() - 1);
                }
            }
        }
        saveData();
    }

    public void setLightMode(boolean animated) {
        final int nDarkModeBk = getResources().getColor(R.color.darkModeBk);
        final int nLightModeBk = getResources().getColor(R.color.lightModeBk);
        final int nLightModeText = getResources().getColor(android.R.color.black);
        final int nDarkModeText = getResources().getColor(android.R.color.white);
        final int nDarkModeSep = getResources().getColor(R.color.darkModeSep);
        final int nLightModeSep = getResources().getColor(R.color.lightModeSep);
        final int nDarkModeBlue = getResources().getColor(R.color.darkModeBlue);
        final int nLightModeBlue = getResources().getColor(R.color.lightModeBlue);
        if(animated) {
            final ArgbEvaluator eval = new ArgbEvaluator();
            ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float fProgress = valueAnimator.getAnimatedFraction();
                    int nColorModeBk = (Integer) eval.evaluate(fProgress, nDarkModeBk, nLightModeBk);
                    int nColorModeText = (Integer) eval.evaluate(fProgress, nDarkModeText, nLightModeText);
                    int nColorModeSep = (Integer) eval.evaluate(fProgress, nDarkModeSep, nLightModeSep);
                    int nColorModeBlue = (Integer) eval.evaluate(fProgress, nDarkModeBlue, nLightModeBlue);
                    mRelativeEffectTitle.setBackgroundColor(nColorModeBk);
                    mRelativeComp.setBackgroundColor(nColorModeBk);
                    mRelativePan.setBackgroundColor(nColorModeBk);
                    mRelativeFreq.setBackgroundColor(nColorModeBk);
                    mRelativeMetronome.setBackgroundColor(nColorModeBk);
                    mRelativeEcho.setBackgroundColor(nColorModeBk);
                    mRelativeReverb.setBackgroundColor(nColorModeBk);
                    mRelativeChorus.setBackgroundColor(nColorModeBk);
                    mRelativeDistortion.setBackgroundColor(nColorModeBk);
                    mRelativeSoundEffect.setBackgroundColor(nColorModeBk);
                    mTextEffectName.setTextColor(nColorModeText);
                    mTextCompGainLabel.setTextColor(nColorModeText);
                    mTextCompThresholdLabel.setTextColor(nColorModeText);
                    mTextCompRatioLabel.setTextColor(nColorModeText);
                    mTextCompAttackLabel.setTextColor(nColorModeText);
                    mTextCompReleaseLabel.setTextColor(nColorModeText);
                    mTextPanValueLabel.setTextColor(nColorModeText);
                    mTextFreqValueLabel.setTextColor(nColorModeText);
                    mTextMetronomeValueLabel.setTextColor(nColorModeText);
                    mTextEchoDryLabel.setTextColor(nColorModeText);
                    mTextEchoWetLabel.setTextColor(nColorModeText);
                    mTextEchoFeedbackLabel.setTextColor(nColorModeText);
                    mTextEchoDelayLabel.setTextColor(nColorModeText);
                    mTextReverbDryLabel.setTextColor(nColorModeText);
                    mTextReverbWetLabel.setTextColor(nColorModeText);
                    mTextReverbRoomSizeLabel.setTextColor(nColorModeText);
                    mTextReverbDampLabel.setTextColor(nColorModeText);
                    mTextReverbWidthLabel.setTextColor(nColorModeText);
                    mTextChorusDryLabel.setTextColor(nColorModeText);
                    mTextChorusWetLabel.setTextColor(nColorModeText);
                    mTextChorusFeedbackLabel.setTextColor(nColorModeText);
                    mTextChorusMinSweepLabel.setTextColor(nColorModeText);
                    mTextChorusMaxSweepLabel.setTextColor(nColorModeText);
                    mTextChorusRateLabel.setTextColor(nColorModeText);
                    mTextDistortionDriveLabel.setTextColor(nColorModeText);
                    mTextDistortionDryLabel.setTextColor(nColorModeText);
                    mTextDistortionWetLabel.setTextColor(nColorModeText);
                    mTextDistortionFeedbackLabel.setTextColor(nColorModeText);
                    mTextDistortionVolumeLabel.setTextColor(nColorModeText);
                    mTextSoundEffectVolumeLabel.setTextColor(nColorModeText);
                    mTextCompGain.setTextColor(nColorModeText);
                    mTextCompThreshold.setTextColor(nColorModeText);
                    mTextCompRatio.setTextColor(nColorModeText);
                    mTextCompAttack.setTextColor(nColorModeText);
                    mTextCompRelease.setTextColor(nColorModeText);
                    mTextPanValue.setTextColor(nColorModeText);
                    mTextFreqValue.setTextColor(nColorModeText);
                    mTextMetronomeValue.setTextColor(nColorModeText);
                    mTextEchoDry.setTextColor(nColorModeText);
                    mTextEchoWet.setTextColor(nColorModeText);
                    mTextEchoFeedback.setTextColor(nColorModeText);
                    mTextEchoDelay.setTextColor(nColorModeText);
                    mTextReverbDry.setTextColor(nColorModeText);
                    mTextReverbWet.setTextColor(nColorModeText);
                    mTextReverbRoomSize.setTextColor(nColorModeText);
                    mTextReverbDamp.setTextColor(nColorModeText);
                    mTextReverbWidth.setTextColor(nColorModeText);
                    mTextChorusDry.setTextColor(nColorModeText);
                    mTextChorusWet.setTextColor(nColorModeText);
                    mTextChorusFeedback.setTextColor(nColorModeText);
                    mTextChorusMinSweep.setTextColor(nColorModeText);
                    mTextChorusMaxSweep.setTextColor(nColorModeText);
                    mTextChorusRate.setTextColor(nColorModeText);
                    mTextDistortionDrive.setTextColor(nColorModeText);
                    mTextDistortionDry.setTextColor(nColorModeText);
                    mTextDistortionWet.setTextColor(nColorModeText);
                    mTextDistortionFeedback.setTextColor(nColorModeText);
                    mTextDistortionVolume.setTextColor(nColorModeText);
                    mTextSoundEffectVolume.setTextColor(nColorModeText);
                    mTextTimeSpecifiedEffectDetail.setTextColor(nColorModeText);
                    mTextSpeedSpecifiedEffectDetail.setTextColor(nColorModeText);
                    mEditSpeedSpecifiedEffectDetail.setTextColor(nColorModeText);
                    mEditTimeSpecifiedEffectDetail.setTextColor(nColorModeText);
                    mEditPTimeSpecifiedEffectDetail.setTextColor(nColorModeText);
                    mEditPitchSpecifiedEffectDetail.setTextColor(nColorModeText);
                    mTextSpeedLoopEffectDetail.setTextColor(nColorModeText);
                    mEditSpeedLoopEffectDetail.setTextColor(nColorModeText);
                    mEditPitchLoopEffectDetail.setTextColor(nColorModeText);
                    mRadioButtonSpecifiedEffectDetail.setTextColor(nColorModeText);
                    mRadioButtonLoopEffectDetail.setTextColor(nColorModeText);
                    mRadioButtonSpecifiedEffectDetail.setBackgroundColor(nColorModeBk);
                    mRadioButtonLoopEffectDetail.setBackgroundColor(nColorModeBk);
                    mViewSepEffectHeader.setBackgroundColor(nColorModeSep);
                    mViewSepEffectDetail.setBackgroundColor(nColorModeSep);
                    mViewSepEffectTemplateHeader.setBackgroundColor(nColorModeSep);
                    mBtnEffectBack.setTextColor(nColorModeBlue);
                    mBtnEffectFinish.setTextColor(nColorModeBlue);
                    mBtnCompRandom.setTextColor(nColorModeBlue);
                    mBtnResetComp.setTextColor(nColorModeBlue);
                    mBtnPanRandom.setTextColor(nColorModeBlue);
                    mBtnResetPan.setTextColor(nColorModeBlue);
                    mBtnFreqRandom.setTextColor(nColorModeBlue);
                    mBtnResetFreq.setTextColor(nColorModeBlue);
                    mBtnMetronomeRandom.setTextColor(nColorModeBlue);
                    mBtnResetMetronome.setTextColor(nColorModeBlue);
                    mBtnEchoRandom.setTextColor(nColorModeBlue);
                    mBtnResetEcho.setTextColor(nColorModeBlue);
                    mBtnReverbRandom.setTextColor(nColorModeBlue);
                    mBtnResetReverb.setTextColor(nColorModeBlue);
                    mBtnChorusRandom.setTextColor(nColorModeBlue);
                    mBtnResetChorus.setTextColor(nColorModeBlue);
                    mBtnDistortionRandom.setTextColor(nColorModeBlue);
                    mBtnResetDistortion.setTextColor(nColorModeBlue);
                    mTextFinishSortEffect.setBackgroundColor(nColorModeBlue);
                    mTextFinishSortEffect.setTextColor(nColorModeBk);
                }
            });
            TransitionDrawable tdImgEffectBack = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_button_back_dark), getResources().getDrawable(R.drawable.ic_button_back)});
            TransitionDrawable tdBtnEffectTemplateMenu = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_button_more_blue_dark), getResources().getDrawable(R.drawable.ic_button_more_blue)});
            TransitionDrawable tdBtnAddEffectTemplate = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.button_big_add_dark), getResources().getDrawable(R.drawable.button_big_add)});

            mImgEffectBack.setImageDrawable(tdImgEffectBack);
            mBtnEffectTemplateMenu.setImageDrawable(tdBtnEffectTemplateMenu);
            mBtnAddEffectTemplate.setImageDrawable(tdBtnAddEffectTemplate);

            int duration = 300;
            anim.setDuration(duration).start();
            tdImgEffectBack.startTransition(duration);
            tdBtnEffectTemplateMenu.startTransition(duration);
            tdBtnAddEffectTemplate.startTransition(duration);
        }
        else {
            mRelativeEffectTitle.setBackgroundColor(nLightModeBk);
            mRelativeComp.setBackgroundColor(nLightModeBk);
            mRelativePan.setBackgroundColor(nLightModeBk);
            mRelativeFreq.setBackgroundColor(nLightModeBk);
            mRelativeMetronome.setBackgroundColor(nLightModeBk);
            mRelativeEcho.setBackgroundColor(nLightModeBk);
            mRelativeReverb.setBackgroundColor(nLightModeBk);
            mRelativeChorus.setBackgroundColor(nLightModeBk);
            mRelativeDistortion.setBackgroundColor(nLightModeBk);
            mRelativeSoundEffect.setBackgroundColor(nLightModeBk);
            mTextEffectName.setTextColor(nLightModeText);
            mTextCompGainLabel.setTextColor(nLightModeText);
            mTextCompThresholdLabel.setTextColor(nLightModeText);
            mTextCompRatioLabel.setTextColor(nLightModeText);
            mTextCompAttackLabel.setTextColor(nLightModeText);
            mTextCompReleaseLabel.setTextColor(nLightModeText);
            mTextPanValueLabel.setTextColor(nLightModeText);
            mTextFreqValueLabel.setTextColor(nLightModeText);
            mTextMetronomeValueLabel.setTextColor(nLightModeText);
            mTextEchoDryLabel.setTextColor(nLightModeText);
            mTextEchoWetLabel.setTextColor(nLightModeText);
            mTextEchoFeedbackLabel.setTextColor(nLightModeText);
            mTextEchoDelayLabel.setTextColor(nLightModeText);
            mTextReverbDryLabel.setTextColor(nLightModeText);
            mTextReverbWetLabel.setTextColor(nLightModeText);
            mTextReverbRoomSizeLabel.setTextColor(nLightModeText);
            mTextReverbDampLabel.setTextColor(nLightModeText);
            mTextReverbWidthLabel.setTextColor(nLightModeText);
            mTextChorusDryLabel.setTextColor(nLightModeText);
            mTextChorusWetLabel.setTextColor(nLightModeText);
            mTextChorusFeedbackLabel.setTextColor(nLightModeText);
            mTextChorusMinSweepLabel.setTextColor(nLightModeText);
            mTextChorusMaxSweepLabel.setTextColor(nLightModeText);
            mTextChorusRateLabel.setTextColor(nLightModeText);
            mTextDistortionDriveLabel.setTextColor(nLightModeText);
            mTextDistortionDryLabel.setTextColor(nLightModeText);
            mTextDistortionWetLabel.setTextColor(nLightModeText);
            mTextDistortionFeedbackLabel.setTextColor(nLightModeText);
            mTextDistortionVolumeLabel.setTextColor(nLightModeText);
            mTextSoundEffectVolumeLabel.setTextColor(nLightModeText);
            mTextCompGain.setTextColor(nLightModeText);
            mTextCompThreshold.setTextColor(nLightModeText);
            mTextCompRatio.setTextColor(nLightModeText);
            mTextCompAttack.setTextColor(nLightModeText);
            mTextCompRelease.setTextColor(nLightModeText);
            mTextPanValue.setTextColor(nLightModeText);
            mTextFreqValue.setTextColor(nLightModeText);
            mTextMetronomeValue.setTextColor(nLightModeText);
            mTextEchoDry.setTextColor(nLightModeText);
            mTextEchoWet.setTextColor(nLightModeText);
            mTextEchoFeedback.setTextColor(nLightModeText);
            mTextEchoDelay.setTextColor(nLightModeText);
            mTextReverbDry.setTextColor(nLightModeText);
            mTextReverbWet.setTextColor(nLightModeText);
            mTextReverbRoomSize.setTextColor(nLightModeText);
            mTextReverbDamp.setTextColor(nLightModeText);
            mTextReverbWidth.setTextColor(nLightModeText);
            mTextChorusDry.setTextColor(nLightModeText);
            mTextChorusWet.setTextColor(nLightModeText);
            mTextChorusFeedback.setTextColor(nLightModeText);
            mTextChorusMinSweep.setTextColor(nLightModeText);
            mTextChorusMaxSweep.setTextColor(nLightModeText);
            mTextChorusRate.setTextColor(nLightModeText);
            mTextDistortionDrive.setTextColor(nLightModeText);
            mTextDistortionDry.setTextColor(nLightModeText);
            mTextDistortionWet.setTextColor(nLightModeText);
            mTextDistortionFeedback.setTextColor(nLightModeText);
            mTextDistortionVolume.setTextColor(nLightModeText);
            mTextSoundEffectVolume.setTextColor(nLightModeText);
            mTextTimeSpecifiedEffectDetail.setTextColor(nLightModeText);
            mTextSpeedSpecifiedEffectDetail.setTextColor(nLightModeText);
            mEditSpeedSpecifiedEffectDetail.setTextColor(nLightModeText);
            mEditTimeSpecifiedEffectDetail.setTextColor(nLightModeText);
            mEditPTimeSpecifiedEffectDetail.setTextColor(nLightModeText);
            mEditPitchSpecifiedEffectDetail.setTextColor(nLightModeText);
            mTextSpeedLoopEffectDetail.setTextColor(nLightModeText);
            mEditSpeedLoopEffectDetail.setTextColor(nLightModeText);
            mEditPitchLoopEffectDetail.setTextColor(nLightModeText);
            mRadioButtonSpecifiedEffectDetail.setTextColor(nLightModeText);
            mRadioButtonLoopEffectDetail.setTextColor(nLightModeText);
            mViewSepEffectHeader.setBackgroundColor(nLightModeSep);
            mViewSepEffectDetail.setBackgroundColor(nLightModeSep);
            mViewSepEffectTemplateHeader.setBackgroundColor(nLightModeSep);
            mBtnEffectBack.setTextColor(nLightModeBlue);
            mBtnEffectFinish.setTextColor(nLightModeBlue);
            mBtnCompRandom.setTextColor(nLightModeBlue);
            mBtnResetComp.setTextColor(nLightModeBlue);
            mBtnPanRandom.setTextColor(nLightModeBlue);
            mBtnResetPan.setTextColor(nLightModeBlue);
            mBtnFreqRandom.setTextColor(nLightModeBlue);
            mBtnResetFreq.setTextColor(nLightModeBlue);
            mBtnMetronomeRandom.setTextColor(nLightModeBlue);
            mBtnResetMetronome.setTextColor(nLightModeBlue);
            mBtnEchoRandom.setTextColor(nLightModeBlue);
            mBtnResetEcho.setTextColor(nLightModeBlue);
            mBtnReverbRandom.setTextColor(nLightModeBlue);
            mBtnResetReverb.setTextColor(nLightModeBlue);
            mBtnChorusRandom.setTextColor(nLightModeBlue);
            mBtnResetChorus.setTextColor(nLightModeBlue);
            mBtnDistortionRandom.setTextColor(nLightModeBlue);
            mBtnResetDistortion.setTextColor(nLightModeBlue);
            mTextFinishSortEffect.setBackgroundColor(nLightModeBlue);
            mTextFinishSortEffect.setTextColor(nLightModeBk);
            mImgEffectBack.setImageDrawable(getResources().getDrawable(R.drawable.ic_button_back));
            mBtnEffectTemplateMenu.setImageDrawable(getResources().getDrawable(R.drawable.ic_button_more_blue));
            mBtnAddEffectTemplate.setImageDrawable(getResources().getDrawable(R.drawable.button_big_add));
        }
        mBtnEffectOff.setTextColor(getResources().getColorStateList(R.color.btn_text));
        mBtnEffectTemplateOff.setTextColor(getResources().getColorStateList(R.color.btn_text));
        mBtnEffectOff.setBackgroundResource(R.drawable.btn_border_background);
        mBtnEffectTemplateOff.setBackgroundResource(R.drawable.btn_border_background);
        mBtnCompRandom.setBackgroundResource(R.drawable.resetbutton);
        mBtnResetComp.setBackgroundResource(R.drawable.resetbutton);
        mBtnPanRandom.setBackgroundResource(R.drawable.resetbutton);
        mBtnResetPan.setBackgroundResource(R.drawable.resetbutton);
        mBtnFreqRandom.setBackgroundResource(R.drawable.resetbutton);
        mBtnResetFreq.setBackgroundResource(R.drawable.resetbutton);
        mBtnMetronomeRandom.setBackgroundResource(R.drawable.resetbutton);
        mBtnResetMetronome.setBackgroundResource(R.drawable.resetbutton);
        mBtnEchoRandom.setBackgroundResource(R.drawable.resetbutton);
        mBtnResetEcho.setBackgroundResource(R.drawable.resetbutton);
        mBtnReverbRandom.setBackgroundResource(R.drawable.resetbutton);
        mBtnResetReverb.setBackgroundResource(R.drawable.resetbutton);
        mBtnChorusRandom.setBackgroundResource(R.drawable.resetbutton);
        mBtnResetChorus.setBackgroundResource(R.drawable.resetbutton);
        mBtnDistortionRandom.setBackgroundResource(R.drawable.resetbutton);
        mBtnResetDistortion.setBackgroundResource(R.drawable.resetbutton);
        mSeekCompGain.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekCompThreshold.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekCompRatio.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekCompAttack.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekCompRelease.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekPanValue.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekFreqValue.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekMetronomeValue.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekEchoDry.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekEchoWet.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekEchoFeedback.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekEchoDelay.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekReverbDry.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekReverbWet.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekReverbRoomSize.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekReverbDamp.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekReverbWidth.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekChorusDry.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekChorusWet.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekChorusFeedback.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekChorusMinSweep.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekChorusMaxSweep.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekChorusRate.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekDistortionDrive.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekDistortionDry.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekDistortionWet.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekDistortionFeedback.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekDistortionVolume.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekSoundEffectVolume.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekCompGain.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekCompThreshold.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekCompRatio.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekCompAttack.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekCompRelease.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekPanValue.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekFreqValue.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekMetronomeValue.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekEchoDry.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekEchoWet.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekEchoFeedback.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekEchoDelay.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekReverbDry.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekReverbWet.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekReverbRoomSize.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekReverbDamp.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekReverbWidth.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekChorusDry.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekChorusWet.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekChorusFeedback.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekChorusMinSweep.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekChorusMaxSweep.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekChorusRate.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekDistortionDrive.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekDistortionDry.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekDistortionWet.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekDistortionFeedback.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekDistortionVolume.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mSeekSoundEffectVolume.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        mBtnCompGainMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnCompGainPlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnCompThresholdMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnCompThresholdPlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnCompRatioMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnCompRatioPlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnCompAttackMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnCompAttackPlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnCompReleaseMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnCompReleasePlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnPanValueMinus.setBackgroundResource(R.drawable.ic_button_left);
        mBtnPanValuePlus.setBackgroundResource(R.drawable.ic_button_right);
        mBtnFreqValueMinus.setBackgroundResource(R.drawable.ic_button_left);
        mBtnFreqValuePlus.setBackgroundResource(R.drawable.ic_button_right);
        mBtnMetronomeValueMinus.setBackgroundResource(R.drawable.ic_button_left);
        mBtnMetronomeValuePlus.setBackgroundResource(R.drawable.ic_button_right);
        mBtnEchoDryMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnEchoDryPlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnEchoWetMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnEchoWetPlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnEchoFeedbackMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnEchoFeedbackPlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnEchoDelayMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnEchoDelayPlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnReverbDryMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnReverbDryPlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnReverbWetMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnReverbWetPlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnReverbRoomSizeMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnReverbRoomSizePlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnReverbDampMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnReverbDampPlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnReverbWidthMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnReverbWidthPlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnChorusDryMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnChorusDryPlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnChorusWetMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnChorusWetPlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnChorusFeedbackMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnChorusFeedbackPlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnChorusMinSweepMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnChorusMinSweepPlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnChorusMaxSweepMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnChorusMaxSweepPlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnChorusRateMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnChorusRatePlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnDistortionDriveMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnDistortionDrivePlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnDistortionDryMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnDistortionDryPlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnDistortionWetMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnDistortionWetPlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnDistortionFeedbackMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnDistortionFeedbackPlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnDistortionVolumeMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnDistortionVolumePlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnSoundEffectVolumeMinus.setBackgroundResource(R.drawable.ic_button_minus);
        mBtnSoundEffectVolumePlus.setBackgroundResource(R.drawable.ic_button_plus);
        mBtnReverbSaveAs.setTextColor(getResources().getColorStateList(R.color.btn_text));
        mBtnEchoSaveAs.setTextColor(getResources().getColorStateList(R.color.btn_text));
        mBtnChorusSaveAs.setTextColor(getResources().getColorStateList(R.color.btn_text));
        mBtnDistortionSaveAs.setTextColor(getResources().getColorStateList(R.color.btn_text));
        mBtnCompSaveAs.setTextColor(getResources().getColorStateList(R.color.btn_text));
        mBtnPanSaveAs.setTextColor(getResources().getColorStateList(R.color.btn_text));
        mBtnFreqSaveAs.setTextColor(getResources().getColorStateList(R.color.btn_text));
        mBtnMetronomeSaveAs.setTextColor(getResources().getColorStateList(R.color.btn_text));
        mBtnReverbSaveAs.setBackgroundResource(R.drawable.btn_border_background);
        mBtnEchoSaveAs.setBackgroundResource(R.drawable.btn_border_background);
        mBtnChorusSaveAs.setBackgroundResource(R.drawable.btn_border_background);
        mBtnDistortionSaveAs.setBackgroundResource(R.drawable.btn_border_background);
        mBtnCompSaveAs.setBackgroundResource(R.drawable.btn_border_background);
        mBtnPanSaveAs.setBackgroundResource(R.drawable.btn_border_background);
        mBtnFreqSaveAs.setBackgroundResource(R.drawable.btn_border_background);
        mBtnMetronomeSaveAs.setBackgroundResource(R.drawable.btn_border_background);
        mEditSpeedSpecifiedEffectDetail.setBackgroundResource(R.drawable.editborder);
        mEditTimeSpecifiedEffectDetail.setBackgroundResource(R.drawable.editborder);
        mEditPTimeSpecifiedEffectDetail.setBackgroundResource(R.drawable.editborder);
        mEditPitchSpecifiedEffectDetail.setBackgroundResource(R.drawable.editborder);
        mEditSpeedLoopEffectDetail.setBackgroundResource(R.drawable.editborder);
        mEditPitchLoopEffectDetail.setBackgroundResource(R.drawable.editborder);
        mEffectsAdapter.notifyDataSetChanged();
        mEffectTemplatesAdapter.notifyDataSetChanged();
    }

    public void setDarkMode(boolean animated) {
        if(sActivity == null || mImgEffectBack == null) return;
        final int nDarkModeBk = getResources().getColor(R.color.darkModeBk);
        final int nLightModeBk = getResources().getColor(R.color.lightModeBk);
        final int nLightModeText = getResources().getColor(android.R.color.black);
        final int nDarkModeText = getResources().getColor(android.R.color.white);
        final int nDarkModeSep = getResources().getColor(R.color.darkModeSep);
        final int nLightModeSep = getResources().getColor(R.color.lightModeSep);
        final int nDarkModeBlue = getResources().getColor(R.color.darkModeBlue);
        final int nLightModeBlue = getResources().getColor(R.color.lightModeBlue);
        final ArgbEvaluator eval = new ArgbEvaluator();
        ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fProgress = valueAnimator.getAnimatedFraction();
                int nColorModeBk = (Integer) eval.evaluate(fProgress, nLightModeBk, nDarkModeBk);
                int nColorModeText = (Integer) eval.evaluate(fProgress, nLightModeText, nDarkModeText);
                int nColorModeSep = (Integer) eval.evaluate(fProgress, nLightModeSep, nDarkModeSep);
                int nColorModeBlue = (Integer) eval.evaluate(fProgress, nLightModeBlue, nDarkModeBlue);
                mRelativeEffectTitle.setBackgroundColor(nColorModeBk);
                mRelativeComp.setBackgroundColor(nColorModeBk);
                mRelativePan.setBackgroundColor(nColorModeBk);
                mRelativeFreq.setBackgroundColor(nColorModeBk);
                mRelativeMetronome.setBackgroundColor(nColorModeBk);
                mRelativeEcho.setBackgroundColor(nColorModeBk);
                mRelativeReverb.setBackgroundColor(nColorModeBk);
                mRelativeChorus.setBackgroundColor(nColorModeBk);
                mRelativeDistortion.setBackgroundColor(nColorModeBk);
                mRelativeSoundEffect.setBackgroundColor(nColorModeBk);
                mTextEffectName.setTextColor(nColorModeText);
                mTextCompGainLabel.setTextColor(nColorModeText);
                mTextCompThresholdLabel.setTextColor(nColorModeText);
                mTextCompRatioLabel.setTextColor(nColorModeText);
                mTextCompAttackLabel.setTextColor(nColorModeText);
                mTextCompReleaseLabel.setTextColor(nColorModeText);
                mTextPanValueLabel.setTextColor(nColorModeText);
                mTextFreqValueLabel.setTextColor(nColorModeText);
                mTextMetronomeValueLabel.setTextColor(nColorModeText);
                mTextEchoDryLabel.setTextColor(nColorModeText);
                mTextEchoWetLabel.setTextColor(nColorModeText);
                mTextEchoFeedbackLabel.setTextColor(nColorModeText);
                mTextEchoDelayLabel.setTextColor(nColorModeText);
                mTextReverbDryLabel.setTextColor(nColorModeText);
                mTextReverbWetLabel.setTextColor(nColorModeText);
                mTextReverbRoomSizeLabel.setTextColor(nColorModeText);
                mTextReverbDampLabel.setTextColor(nColorModeText);
                mTextReverbWidthLabel.setTextColor(nColorModeText);
                mTextChorusDryLabel.setTextColor(nColorModeText);
                mTextChorusWetLabel.setTextColor(nColorModeText);
                mTextChorusFeedbackLabel.setTextColor(nColorModeText);
                mTextChorusMinSweepLabel.setTextColor(nColorModeText);
                mTextChorusMaxSweepLabel.setTextColor(nColorModeText);
                mTextChorusRateLabel.setTextColor(nColorModeText);
                mTextDistortionDriveLabel.setTextColor(nColorModeText);
                mTextDistortionDryLabel.setTextColor(nColorModeText);
                mTextDistortionWetLabel.setTextColor(nColorModeText);
                mTextDistortionFeedbackLabel.setTextColor(nColorModeText);
                mTextDistortionVolumeLabel.setTextColor(nColorModeText);
                mTextSoundEffectVolumeLabel.setTextColor(nColorModeText);
                mTextCompGain.setTextColor(nColorModeText);
                mTextCompThreshold.setTextColor(nColorModeText);
                mTextCompRatio.setTextColor(nColorModeText);
                mTextCompAttack.setTextColor(nColorModeText);
                mTextCompRelease.setTextColor(nColorModeText);
                mTextPanValue.setTextColor(nColorModeText);
                mTextFreqValue.setTextColor(nColorModeText);
                mTextMetronomeValue.setTextColor(nColorModeText);
                mTextEchoDry.setTextColor(nColorModeText);
                mTextEchoWet.setTextColor(nColorModeText);
                mTextEchoFeedback.setTextColor(nColorModeText);
                mTextEchoDelay.setTextColor(nColorModeText);
                mTextReverbDry.setTextColor(nColorModeText);
                mTextReverbWet.setTextColor(nColorModeText);
                mTextReverbRoomSize.setTextColor(nColorModeText);
                mTextReverbDamp.setTextColor(nColorModeText);
                mTextReverbWidth.setTextColor(nColorModeText);
                mTextChorusDry.setTextColor(nColorModeText);
                mTextChorusWet.setTextColor(nColorModeText);
                mTextChorusFeedback.setTextColor(nColorModeText);
                mTextChorusMinSweep.setTextColor(nColorModeText);
                mTextChorusMaxSweep.setTextColor(nColorModeText);
                mTextChorusRate.setTextColor(nColorModeText);
                mTextDistortionDrive.setTextColor(nColorModeText);
                mTextDistortionDry.setTextColor(nColorModeText);
                mTextDistortionWet.setTextColor(nColorModeText);
                mTextDistortionFeedback.setTextColor(nColorModeText);
                mTextDistortionVolume.setTextColor(nColorModeText);
                mTextSoundEffectVolume.setTextColor(nColorModeText);
                mTextTimeSpecifiedEffectDetail.setTextColor(nColorModeText);
                mTextSpeedSpecifiedEffectDetail.setTextColor(nColorModeText);
                mTextSpeedLoopEffectDetail.setTextColor(nColorModeText);
                mEditSpeedSpecifiedEffectDetail.setTextColor(nColorModeText);
                mEditTimeSpecifiedEffectDetail.setTextColor(nColorModeText);
                mEditPTimeSpecifiedEffectDetail.setTextColor(nColorModeText);
                mEditPitchSpecifiedEffectDetail.setTextColor(nColorModeText);
                mEditSpeedLoopEffectDetail.setTextColor(nColorModeText);
                mEditPitchLoopEffectDetail.setTextColor(nColorModeText);
                mRadioButtonSpecifiedEffectDetail.setTextColor(nColorModeText);
                mRadioButtonLoopEffectDetail.setTextColor(nColorModeText);
                mViewSepEffectHeader.setBackgroundColor(nColorModeSep);
                mViewSepEffectDetail.setBackgroundColor(nColorModeSep);
                mViewSepEffectTemplateHeader.setBackgroundColor(nColorModeSep);
                mBtnEffectBack.setTextColor(nColorModeBlue);
                mBtnEffectFinish.setTextColor(nColorModeBlue);
                mBtnCompRandom.setTextColor(nColorModeBlue);
                mBtnResetComp.setTextColor(nColorModeBlue);
                mBtnPanRandom.setTextColor(nColorModeBlue);
                mBtnResetPan.setTextColor(nColorModeBlue);
                mBtnFreqRandom.setTextColor(nColorModeBlue);
                mBtnResetFreq.setTextColor(nColorModeBlue);
                mBtnMetronomeRandom.setTextColor(nColorModeBlue);
                mBtnResetMetronome.setTextColor(nColorModeBlue);
                mBtnEchoRandom.setTextColor(nColorModeBlue);
                mBtnResetEcho.setTextColor(nColorModeBlue);
                mBtnReverbRandom.setTextColor(nColorModeBlue);
                mBtnResetReverb.setTextColor(nColorModeBlue);
                mBtnChorusRandom.setTextColor(nColorModeBlue);
                mBtnResetChorus.setTextColor(nColorModeBlue);
                mBtnDistortionRandom.setTextColor(nColorModeBlue);
                mBtnResetDistortion.setTextColor(nColorModeBlue);
                mTextFinishSortEffect.setBackgroundColor(nColorModeBlue);
                mTextFinishSortEffect.setTextColor(nColorModeBk);
            }
        });
        TransitionDrawable tdImgEffectBack = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_button_back), getResources().getDrawable(R.drawable.ic_button_back_dark)});
        TransitionDrawable tdBtnEffectTemplateMenu = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_button_more_blue), getResources().getDrawable(R.drawable.ic_button_more_blue_dark)});
        TransitionDrawable tdBtnAddEffectTemplate = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.button_big_add), getResources().getDrawable(R.drawable.button_big_add_dark)});

        mImgEffectBack.setImageDrawable(tdImgEffectBack);
        mBtnEffectTemplateMenu.setImageDrawable(tdBtnEffectTemplateMenu);
        mBtnAddEffectTemplate.setImageDrawable(tdBtnAddEffectTemplate);

        int duration = animated ? 300 : 0;
        anim.setDuration(duration).start();
        tdImgEffectBack.startTransition(duration);
        tdBtnEffectTemplateMenu.startTransition(duration);
        tdBtnAddEffectTemplate.startTransition(duration);

        mBtnEffectOff.setTextColor(getResources().getColorStateList(R.color.btn_text_dark));
        mBtnEffectTemplateOff.setTextColor(getResources().getColorStateList(R.color.btn_text_dark));
        mBtnEffectOff.setBackgroundResource(R.drawable.btn_border_background_dark);
        mBtnEffectTemplateOff.setBackgroundResource(R.drawable.btn_border_background_dark);
        mBtnCompRandom.setBackgroundResource(R.drawable.resetbutton_dark);
        mBtnResetComp.setBackgroundResource(R.drawable.resetbutton_dark);
        mBtnPanRandom.setBackgroundResource(R.drawable.resetbutton_dark);
        mBtnResetPan.setBackgroundResource(R.drawable.resetbutton_dark);
        mBtnFreqRandom.setBackgroundResource(R.drawable.resetbutton_dark);
        mBtnResetFreq.setBackgroundResource(R.drawable.resetbutton_dark);
        mBtnMetronomeRandom.setBackgroundResource(R.drawable.resetbutton_dark);
        mBtnResetMetronome.setBackgroundResource(R.drawable.resetbutton_dark);
        mBtnEchoRandom.setBackgroundResource(R.drawable.resetbutton_dark);
        mBtnResetEcho.setBackgroundResource(R.drawable.resetbutton_dark);
        mBtnReverbRandom.setBackgroundResource(R.drawable.resetbutton_dark);
        mBtnResetReverb.setBackgroundResource(R.drawable.resetbutton_dark);
        mBtnChorusRandom.setBackgroundResource(R.drawable.resetbutton_dark);
        mBtnResetChorus.setBackgroundResource(R.drawable.resetbutton_dark);
        mBtnDistortionRandom.setBackgroundResource(R.drawable.resetbutton_dark);
        mBtnResetDistortion.setBackgroundResource(R.drawable.resetbutton_dark);
        mRadioButtonSpecifiedEffectDetail.setBackgroundResource(R.drawable.resetbutton_dark);
        mRadioButtonLoopEffectDetail.setBackgroundResource(R.drawable.resetbutton_dark);
        mSeekCompGain.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekCompThreshold.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekCompRatio.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekCompAttack.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekCompRelease.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekPanValue.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekFreqValue.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekMetronomeValue.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekEchoDry.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekEchoWet.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekEchoFeedback.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekEchoDelay.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekReverbDry.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekReverbWet.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekReverbRoomSize.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekReverbDamp.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekReverbWidth.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekChorusDry.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekChorusWet.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekChorusFeedback.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekChorusMinSweep.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekChorusMaxSweep.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekChorusRate.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekDistortionDrive.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekDistortionDry.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekDistortionWet.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekDistortionFeedback.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekDistortionVolume.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekSoundEffectVolume.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekCompGain.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekCompThreshold.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekCompRatio.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekCompAttack.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekCompRelease.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekPanValue.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekFreqValue.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekMetronomeValue.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekEchoDry.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekEchoWet.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekEchoFeedback.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekEchoDelay.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekReverbDry.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekReverbWet.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekReverbRoomSize.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekReverbDamp.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekReverbWidth.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekChorusDry.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekChorusWet.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekChorusFeedback.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekChorusMinSweep.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekChorusMaxSweep.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekChorusRate.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekDistortionDrive.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekDistortionDry.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekDistortionWet.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekDistortionFeedback.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekDistortionVolume.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mSeekSoundEffectVolume.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        mBtnCompGainMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnCompGainPlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnCompThresholdMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnCompThresholdPlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnCompRatioMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnCompRatioPlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnCompAttackMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnCompAttackPlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnCompReleaseMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnCompReleasePlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnPanValueMinus.setBackgroundResource(R.drawable.ic_button_left_dark);
        mBtnPanValuePlus.setBackgroundResource(R.drawable.ic_button_right_dark);
        mBtnFreqValueMinus.setBackgroundResource(R.drawable.ic_button_left_dark);
        mBtnFreqValuePlus.setBackgroundResource(R.drawable.ic_button_right_dark);
        mBtnMetronomeValueMinus.setBackgroundResource(R.drawable.ic_button_left_dark);
        mBtnMetronomeValuePlus.setBackgroundResource(R.drawable.ic_button_right_dark);
        mBtnEchoDryMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnEchoDryPlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnEchoWetMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnEchoWetPlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnEchoFeedbackMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnEchoFeedbackPlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnEchoDelayMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnEchoDelayPlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnReverbDryMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnReverbDryPlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnReverbWetMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnReverbWetPlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnReverbRoomSizeMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnReverbRoomSizePlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnReverbDampMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnReverbDampPlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnReverbWidthMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnReverbWidthPlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnChorusDryMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnChorusDryPlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnChorusWetMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnChorusWetPlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnChorusFeedbackMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnChorusFeedbackPlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnChorusMinSweepMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnChorusMinSweepPlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnChorusMaxSweepMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnChorusMaxSweepPlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnChorusRateMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnChorusRatePlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnDistortionDriveMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnDistortionDrivePlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnDistortionDryMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnDistortionDryPlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnDistortionWetMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnDistortionWetPlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnDistortionFeedbackMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnDistortionFeedbackPlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnDistortionVolumeMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnDistortionVolumePlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnSoundEffectVolumeMinus.setBackgroundResource(R.drawable.ic_button_minus_dark);
        mBtnSoundEffectVolumePlus.setBackgroundResource(R.drawable.ic_button_plus_dark);
        mBtnReverbSaveAs.setTextColor(getResources().getColorStateList(R.color.btn_text_dark));
        mBtnEchoSaveAs.setTextColor(getResources().getColorStateList(R.color.btn_text_dark));
        mBtnChorusSaveAs.setTextColor(getResources().getColorStateList(R.color.btn_text_dark));
        mBtnDistortionSaveAs.setTextColor(getResources().getColorStateList(R.color.btn_text_dark));
        mBtnCompSaveAs.setTextColor(getResources().getColorStateList(R.color.btn_text_dark));
        mBtnPanSaveAs.setTextColor(getResources().getColorStateList(R.color.btn_text_dark));
        mBtnFreqSaveAs.setTextColor(getResources().getColorStateList(R.color.btn_text_dark));
        mBtnMetronomeSaveAs.setTextColor(getResources().getColorStateList(R.color.btn_text_dark));
        mBtnReverbSaveAs.setBackgroundResource(R.drawable.btn_border_background_dark);
        mBtnEchoSaveAs.setBackgroundResource(R.drawable.btn_border_background_dark);
        mBtnChorusSaveAs.setBackgroundResource(R.drawable.btn_border_background_dark);
        mBtnDistortionSaveAs.setBackgroundResource(R.drawable.btn_border_background_dark);
        mBtnCompSaveAs.setBackgroundResource(R.drawable.btn_border_background_dark);
        mBtnPanSaveAs.setBackgroundResource(R.drawable.btn_border_background_dark);
        mBtnFreqSaveAs.setBackgroundResource(R.drawable.btn_border_background_dark);
        mBtnMetronomeSaveAs.setBackgroundResource(R.drawable.btn_border_background_dark);
        mEditSpeedSpecifiedEffectDetail.setBackgroundResource(R.drawable.editborder_dark);
        mEditTimeSpecifiedEffectDetail.setBackgroundResource(R.drawable.editborder_dark);
        mEditPTimeSpecifiedEffectDetail.setBackgroundResource(R.drawable.editborder_dark);
        mEditPitchSpecifiedEffectDetail.setBackgroundResource(R.drawable.editborder_dark);
        mEditSpeedLoopEffectDetail.setBackgroundResource(R.drawable.editborder_dark);
        mEditPitchLoopEffectDetail.setBackgroundResource(R.drawable.editborder_dark);
        mEffectsAdapter.notifyDataSetChanged();
        mEffectTemplatesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(MainActivity.sStream == 0) {
            sMetronome.stop();
            if(sSEStream != 0) BASS.BASS_ChannelStop(sSEStream);
            if(sSEStream2 != 0) BASS.BASS_ChannelStop(sSEStream2);
        }
    }
}
