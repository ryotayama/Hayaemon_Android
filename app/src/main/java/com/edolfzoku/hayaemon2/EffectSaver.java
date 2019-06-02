/*
 * EffectSaver
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

import java.util.ArrayList;

class EffectSaver {
    private boolean mSave = false;
    private float mSpeed = 0.0f;
    private float mPitch = 0.0f;
    private int mVol = 0;
    private int mEQ20K = 0;
    private int mEQ16K = 0;
    private int nEQ12_5K = 0;
    private int nEQ10K = 0;
    private int nEQ8K = 0;
    private int nEQ6_3K = 0;
    private int nEQ5K = 0;
    private int nEQ4K = 0;
    private int nEQ3_15K = 0;
    private int nEQ2_5K = 0;
    private int nEQ2K = 0;
    private int nEQ1_6K = 0;
    private int nEQ1_25K = 0;
    private int nEQ1K = 0;
    private int nEQ800 = 0;
    private int nEQ630 = 0;
    private int nEQ500 = 0;
    private int nEQ400 = 0;
    private int nEQ315 = 0;
    private int nEQ250 = 0;
    private int nEQ200 = 0;
    private int nEQ160 = 0;
    private int nEQ125 = 0;
    private int nEQ100 = 0;
    private int nEQ80 = 0;
    private int nEQ63 = 0;
    private int nEQ50 = 0;
    private int nEQ40 = 0;
    private int nEQ31_5 = 0;
    private int nEQ25 = 0;
    private int nEQ20 = 0;
    private ArrayList<EffectItem> arEffectItems = null;
    private float fPan = 0.0f;
    private float fFreq = 0.0f;
    private int nBPM = 0;
    private float mVol1 = 0.0f;
    private float mVol2 = 0.0f;
    private float mVol3 = 0.0f;
    private float mVol4 = 0.0f;
    private float mVol5 = 0.0f;
    private float mVol6 = 0.0f;
    private float mVol7 = 0.0f;
    private float mTimeOmIncreaseSpeed = 1.0f;
    private float mIncreaseSpeed = 0.1f;
    private float mTimeOmDecreaseSpeed = 1.0f;
    private float mDecreaseSpeed = 0.1f;
    private float mCompGain = 2.0f, mCompThreshold = -20.0f, mCompRatio = 10.0f, mCompAttack = 1.2f, mCompRelease = 400.0f;
    private float mEchoDry, mEchoWet, mEchoFeedback, mEchoDelay;
    private float mReverbDry = 0.7f, mReverbWet = 1.0f, mReverbRoomSize = 0.85f, mReverbDamp = 0.5f, mReverbWidth = 0.9f;
    private float mChorusDry = 1.0f, mChorusWet = 0.1f, mChorusFeedback = 0.5f, mChorusMinSweep = 1.0f, mChorusMaxSweep = 2.0f, mChorusRate = 10.0f;
    private float mDistortionDrive = 0.2f, mDistortionDry = 0.95f, mDistortionWet = 0.05f, mDistortionFeedback = 0.1f, mDistortionVolume = 1.0f;
    private boolean bABLoop = false;
    private boolean bLoop = false;
    private boolean bLoopA = false;
    private double dLoopA = 0.0;
    private boolean bLoopB = false;
    private double dLoopB = 0.0;
    private ArrayList<Double> arMarkerTime = null;
    private boolean bLoopMarker = false;
    private int nMarker = 0;

    void setSave(boolean save) { mSave = save; }
    void setSpeed(float speed) { mSpeed = speed; }
    void setPitch(float pitch) { mPitch = pitch; }
    void setVol(int vol) { mVol = vol; }
    void setEQ20K(int eq20K) { mEQ20K = eq20K; }
    void setEQ16K(int eq16K) { mEQ16K = eq16K; }
    void setEQ12_5K(int eq12_5K) { nEQ12_5K = eq12_5K; }
    void setEQ10K(int eq10K) { nEQ10K = eq10K; }
    void setEQ8K(int eq8K) { nEQ8K = eq8K; }
    void setEQ6_3K(int eq6_3K) { nEQ6_3K = eq6_3K; }
    void setEQ5K(int eq5K) { nEQ5K = eq5K; }
    void setEQ4K(int eq4K) { nEQ4K = eq4K; }
    void setEQ3_15K(int eq3_15K) { nEQ3_15K = eq3_15K; }
    void setEQ2_5K(int eq2_5K) { nEQ2_5K = eq2_5K; }
    void setEQ2K(int eq2K) { nEQ2K = eq2K; }
    void setEQ1_6K(int eq1_6K) { nEQ1_6K = eq1_6K; }
    void setEQ1_25K(int eq1_25K) { nEQ1_25K = eq1_25K; }
    void setEQ1K(int eq1K) { nEQ1K = eq1K; }
    void setEQ800(int eq800) { nEQ800 = eq800; }
    void setEQ630(int eq630) { nEQ630 = eq630; }
    void setEQ500(int eq500) { nEQ500 = eq500; }
    void setEQ400(int eq400) { nEQ400 = eq400; }
    void setEQ315(int eq315) { nEQ315 = eq315; }
    void setEQ250(int eq250) { nEQ250 = eq250; }
    void setEQ200(int eq200) { nEQ200 = eq200; }
    void setEQ160(int eq160) { nEQ160 = eq160; }
    void setEQ125(int eq125) { nEQ125 = eq125; }
    void setEQ100(int eq100) { nEQ100 = eq100; }
    void setEQ80(int eq80) { nEQ80 = eq80; }
    void setEQ63(int eq63) { nEQ63 = eq63; }
    void setEQ50(int eq50) { nEQ50 = eq50; }
    void setEQ40(int eq40) { nEQ40 = eq40; }
    void setEQ31_5(int eq31_5) { nEQ31_5 = eq31_5; }
    void setEQ25(int eq25) { nEQ25 = eq25; }
    void setEQ20(int eq20) { nEQ20 = eq20; }
    void setEffectItems(ArrayList<EffectItem> effectItems) {
        arEffectItems = new ArrayList<>();
        for(int i = 0; i < effectItems.size(); i++) {
            EffectItem itemFrom = effectItems.get(i);
            EffectItem itemTo = new EffectItem();
            itemTo.setEffectName(itemFrom.getEffectName());
            itemTo.setSelected(itemFrom.isSelected());
            itemTo.setbEditEnabled(itemFrom.isEditEnabled());
            arEffectItems.add(itemTo);
        }
    }
    void setPan(float fPan) { this.fPan = fPan; }
    void setFreq(float fFreq) { this.fFreq = fFreq; }
    void setBPM(int nBPM) { this.nBPM = nBPM; }
    void setVol1(float mVol1) { this.mVol1 = mVol1; }
    void setVol2(float mVol2) { this.mVol2 = mVol2; }
    void setVol3(float mVol3) { this.mVol3 = mVol3; }
    void setVol4(float mVol4) { this.mVol4 = mVol4; }
    void setVol5(float mVol5) { this.mVol5 = mVol5; }
    void setVol6(float mVol6) { this.mVol6 = mVol6; }
    void setVol7(float mVol7) { this.mVol7 = mVol7; }
    void setTimeOmIncreaseSpeed(float mTimeOmIncreaseSpeed) { this.mTimeOmIncreaseSpeed = mTimeOmIncreaseSpeed; }
    void setIncreaseSpeed(float mIncreaseSpeed) { this.mIncreaseSpeed = mIncreaseSpeed; }
    void setTimeOmDecreaseSpeed(float mTimeOmDecreaseSpeed) { this.mTimeOmDecreaseSpeed = mTimeOmDecreaseSpeed; }
    void setDecreaseSpeed(float mDecreaseSpeed) { this.mDecreaseSpeed = mDecreaseSpeed; }
    void setCompGain(float compGain) { mCompGain = compGain; }
    void setCompThreshold(float compThreshold) { mCompThreshold = compThreshold; }
    void setCompRatio(float compRatio) { mCompRatio = compRatio; }
    void setCompAttack(float compAttack) { mCompAttack = compAttack; }
    void setCompRelease(float compRelease) { mCompRelease = compRelease; }
    void setEchoDry(float echoDry) { mEchoDry = echoDry; }
    void setEchoWet(float echoWet) { mEchoWet = echoWet; }
    void setEchoFeedback(float echoFeedback) { mEchoFeedback = echoFeedback; }
    void setEchoDelay(float echoDelay) { mEchoDelay = echoDelay; }
    void setReverbDry(float reverbDry) { mReverbDry = reverbDry; }
    void setReverbWet(float reverbWet) { mReverbWet = reverbWet; }
    void setReverbRoomSize(float reverbRoomSize) { mReverbRoomSize = reverbRoomSize; }
    void setReverbDamp(float reverbDamp) { mReverbDamp = reverbDamp; }
    void setReverbWidth(float reverbWidth) { mReverbWidth = reverbWidth; }
    void setChorusDry(float chorusDry) { mChorusDry = chorusDry; }
    void setChorusWet(float chorusWet) { mChorusWet = chorusWet; }
    void setChorusFeedback(float chorusFeedback) { mChorusFeedback = chorusFeedback; }
    void setChorusMinSweep(float chorusMinSweep) { mChorusMinSweep = chorusMinSweep; }
    void setChorusMaxSweep(float chorusMaxSweep) { mChorusMaxSweep = chorusMaxSweep; }
    void setChorusRate(float chorusRate) { mChorusRate = chorusRate; }
    void setDistortionDrive(float distortionDrive) { mDistortionDrive = distortionDrive; }
    void setDistortionDry(float distortionDry) { mDistortionDry = distortionDry; }
    void setDistortionWet(float distortionWet) { mDistortionWet = distortionWet; }
    void setDistortionFeedback(float distortionFeedback) { mDistortionFeedback = distortionFeedback; }
    void setDistortionVolume(float distortionVolume) { mDistortionVolume = distortionVolume; }
    void setIsABLoop(boolean bABLoop) { this.bABLoop = bABLoop; }
    void setIsLoopA(boolean bLoopA) { this.bLoopA = bLoopA; }
    void setLoopA(double dLoopA) { this.dLoopA = dLoopA; }
    void setIsLoopB(boolean bLoopB) { this.bLoopB = bLoopB; }
    void setLoopB(double dLoopB) { this.dLoopB = dLoopB; }
    void setArMarkerTime(ArrayList<Double> arMarkerTime) { this.arMarkerTime = new ArrayList<>(arMarkerTime); }
    void setIsLoopMarker(boolean bLoopMarker) { this.bLoopMarker = bLoopMarker; }
    void setMarker(int nMarker) { this.nMarker = nMarker; }

    boolean isSave() { return mSave; }
    float getSpeed() { return mSpeed; }
    float getPitch() { return mPitch; }
    int getVol() { return mVol; }
    int getEQ20K() { return mEQ20K; }
    int getEQ16K() { return mEQ16K; }
    int getEQ12_5K() { return nEQ12_5K; }
    int getEQ10K() { return nEQ10K; }
    int getEQ8K() { return nEQ8K; }
    int getEQ6_3K() { return nEQ6_3K; }
    int getEQ5K() { return nEQ5K; }
    int getEQ4K() { return nEQ4K; }
    int getEQ3_15K() { return nEQ3_15K; }
    int getEQ2_5K() { return nEQ2_5K; }
    int getEQ2K() { return nEQ2K; }
    int getEQ1_6K() { return nEQ1_6K; }
    int getEQ1_25K() { return nEQ1_25K; }
    int getEQ1K() { return nEQ1K; }
    int getEQ800() { return nEQ800; }
    int getEQ630() { return nEQ630; }
    int getEQ500() { return nEQ500; }
    int getEQ400() { return nEQ400; }
    int getEQ315() { return nEQ315; }
    int getEQ250() { return nEQ250; }
    int getEQ200() { return nEQ200; }
    int getEQ160() { return nEQ160; }
    int getEQ125() { return nEQ125; }
    int getEQ100() { return nEQ100; }
    int getEQ80() { return nEQ80; }
    int getEQ63() { return nEQ63; }
    int getEQ50() { return nEQ50; }
    int getEQ40() { return nEQ40; }
    int getEQ31_5() { return nEQ31_5; }
    int getEQ25() { return nEQ25; }
    int getEQ20() { return nEQ20; }
    ArrayList<EffectItem> getEffectItems() { return arEffectItems; }
    float getPan() { return fPan; }
    float getFreq() { return fFreq; }
    int getBPM() { return nBPM; }
    float getVol1() { return mVol1; }
    float getVol2() { return mVol2; }
    float getVol3() { return mVol3; }
    float getVol4() { return mVol4; }
    float getVol5() { return mVol5; }
    float getVol6() { return mVol6; }
    float getVol7() { return mVol7; }
    float getTimeOmIncreaseSpeed() { return mTimeOmIncreaseSpeed; }
    float getIncreaseSpeed() { return mIncreaseSpeed; }
    float getTimeOmDecreaseSpeed() { return mTimeOmDecreaseSpeed; }
    float getDecreaseSpeed() { return mDecreaseSpeed; }
    float getCompGain() { return mCompGain; }
    float getCompThreshold() { return mCompThreshold; }
    float getCompRatio() { return mCompRatio; }
    float getCompAttack() { return mCompAttack; }
    float getCompRelease() { return mCompRelease; }
    float getEchoDry() { return mEchoDry; }
    float getEchoWet() { return mEchoWet; }
    float getEchoFeedback() { return mEchoFeedback; }
    float getEchoDelay() { return mEchoDelay; }
    float getReverbDry() { return mReverbDry; }
    float getReverbWet() { return mReverbWet; }
    float getReverbRoomSize() { return mReverbRoomSize; }
    float getReverbDamp() { return mReverbDamp; }
    float getReverbWidth() { return mReverbWidth; }
    float getChorusDry() { return mChorusDry; }
    float getChorusWet() { return mChorusWet; }
    float getChorusFeedback() { return mChorusFeedback; }
    float getChorusMinSweep() { return mChorusMinSweep; }
    float getChorusMaxSweep() { return mChorusMaxSweep; }
    float getChorusRate() { return mChorusRate; }
    float getDistortionDrive() { return mDistortionDrive; }
    float getDistortionDry() { return mDistortionDry; }
    float getDistortionWet() { return mDistortionWet; }
    float getDistortionFeedback() { return mDistortionFeedback; }
    float getDistortionVolume() { return mDistortionVolume; }
    boolean isABLoop() { return bABLoop; }
    boolean isLoopA() { return bLoopA; }
    double getLoopA() { return dLoopA; }
    boolean isLoopB() { return bLoopB; }
    double getLoopB() { return dLoopB; }
    ArrayList<Double> getArMarkerTime() { return arMarkerTime; }
    boolean isLoopMarker() { return bLoopMarker; }
    int getMarker() { return nMarker; }

    EffectSaver() {
    }

    EffectSaver(EffectSaver saver) {
        mSave = saver.mSave;
        mSpeed = saver.mSpeed;
        mPitch = saver.mPitch;
        mVol = saver.mVol;
        mEQ20K = saver.mEQ20K;
        mEQ16K = saver.mEQ16K;
        nEQ12_5K = saver.nEQ12_5K;
        nEQ10K = saver.nEQ10K;
        nEQ8K = saver.nEQ8K;
        nEQ6_3K = saver.nEQ6_3K;
        nEQ5K = saver.nEQ5K;
        nEQ4K = saver.nEQ4K;
        nEQ3_15K = saver.nEQ3_15K;
        nEQ2_5K = saver.nEQ2_5K;
        nEQ2K = saver.nEQ2K;
        nEQ1_6K = saver.nEQ1_6K;
        nEQ1_25K = saver.nEQ1_25K;
        nEQ1K = saver.nEQ1K;
        nEQ800 = saver.nEQ800;
        nEQ630 = saver.nEQ630;
        nEQ500 = saver.nEQ500;
        nEQ400 = saver.nEQ400;
        nEQ315 = saver.nEQ315;
        nEQ250 = saver.nEQ250;
        nEQ200 = saver.nEQ200;
        nEQ160 = saver.nEQ160;
        nEQ125 = saver.nEQ125;
        nEQ100 = saver.nEQ100;
        nEQ80 = saver.nEQ80;
        nEQ63 = saver.nEQ63;
        nEQ50 = saver.nEQ50;
        nEQ40 = saver.nEQ40;
        nEQ31_5 = saver.nEQ31_5;
        nEQ25 = saver.nEQ25;
        nEQ20 = saver.nEQ20;
        if(saver.arEffectItems == null) arEffectItems = null;
        else arEffectItems = new ArrayList<>(saver.arEffectItems);
        fPan = saver.fPan;
        fFreq = saver.fFreq;
        nBPM = saver.nBPM;
        mVol1 = saver.mVol1;
        mVol2 = saver.mVol2;
        mVol3 = saver.mVol3;
        mVol4 = saver.mVol4;
        mVol5 = saver.mVol5;
        mVol6 = saver.mVol6;
        mVol7 = saver.mVol7;
        mTimeOmIncreaseSpeed = saver.mTimeOmIncreaseSpeed;
        mIncreaseSpeed = saver.mIncreaseSpeed;
        mTimeOmDecreaseSpeed = saver.mTimeOmDecreaseSpeed;
        mDecreaseSpeed = saver.mDecreaseSpeed;
        mCompGain = saver.mCompGain;
        mCompThreshold = saver.mCompThreshold;
        mCompRatio = saver.mCompRatio;
        mCompAttack = saver.mCompAttack;
        mCompRelease = saver.mCompRelease;
        mEchoDry = saver.mEchoDry;
        mEchoWet = saver.mEchoWet;
        mEchoFeedback = saver.mEchoFeedback;
        mEchoDelay = saver.mEchoDelay;
        mReverbDry = saver.mReverbDry;
        mReverbWet = saver.mReverbWet;
        mReverbRoomSize = saver.mReverbRoomSize;
        mReverbDamp = saver.mReverbDamp;
        mReverbWidth = saver.mReverbWidth;
        mChorusDry = saver.mChorusDry;
        mChorusWet = saver.mChorusWet;
        mChorusFeedback = saver.mChorusFeedback;
        mChorusMinSweep = saver.mChorusMinSweep;
        mChorusMaxSweep = saver.mChorusMaxSweep;
        mChorusRate = saver.mChorusRate;
        mDistortionDrive = saver.mDistortionDrive;
        mDistortionDry = saver.mDistortionDry;
        mDistortionWet = saver.mDistortionWet;
        mDistortionFeedback = saver.mDistortionFeedback;
        mDistortionVolume = saver.mDistortionVolume;
        bABLoop = saver.bABLoop;
        bLoop = saver.bLoop;
        bLoopA = saver.bLoopA;
        dLoopA = saver.dLoopA;
        bLoopB = saver.bLoopB;
        dLoopB = saver.dLoopB;
        if(saver.arMarkerTime == null)
            arMarkerTime = null;
        else
            arMarkerTime = new ArrayList<>(saver.arMarkerTime);
        bLoopMarker = saver.bLoopMarker;
        nMarker = saver.nMarker;
    }
}
