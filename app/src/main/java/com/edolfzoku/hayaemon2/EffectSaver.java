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

public class EffectSaver {
    private boolean bSave = false;
    private float fSpeed = 0.0f;
    private float fPitch = 0.0f;
    private int nVol = 0;
    private int nEQ20K = 0;
    private int nEQ16K = 0;
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
    private float fVol1 = 0.0f;
    private float fVol2 = 0.0f;
    private float fVol3 = 0.0f;
    private float fVol4 = 0.0f;
    private float fVol5 = 0.0f;
    private float fVol6 = 0.0f;
    private float fVol7 = 0.0f;
    private float fTimeOfIncreaseSpeed = 1.0f;
    private float fIncreaseSpeed = 0.1f;
    private boolean bABLoop = false;
    private boolean bLoop = false;
    private boolean bLoopA = false;
    private double dLoopA = 0.0;
    private boolean bLoopB = false;
    private double dLoopB = 0.0;
    private ArrayList<Double> arMarkerTime = null;
    private boolean bLoopMarker = false;
    private int nMarker = 0;

    public void setSave(boolean bSave) { this.bSave = bSave; }
    public void setSpeed(float fSpeed) { this.fSpeed = fSpeed; }
    public void setPitch(float fPitch) { this.fPitch = fPitch; }
    public void setVol(int nVol) { this.nVol = nVol; }
    public void setEQ20K(int nEQ20K) { this.nEQ20K = nEQ20K; }
    public void setEQ16K(int nEQ16K) { this.nEQ16K = nEQ16K; }
    public void setEQ12_5K(int nEQ12_5K) { this.nEQ12_5K = nEQ12_5K; }
    public void setEQ10K(int nEQ10K) { this.nEQ10K = nEQ10K; }
    public void setEQ8K(int nEQ8K) { this.nEQ8K = nEQ8K; }
    public void setEQ6_3K(int nEQ6_3K) { this.nEQ6_3K = nEQ6_3K; }
    public void setEQ5K(int nEQ5K) { this.nEQ5K = nEQ5K; }
    public void setEQ4K(int nEQ4K) { this.nEQ4K = nEQ4K; }
    public void setEQ3_15K(int nEQ3_15K) { this.nEQ3_15K = nEQ3_15K; }
    public void setEQ2_5K(int nEQ2_5K) { this.nEQ2_5K = nEQ2_5K; }
    public void setEQ2K(int nEQ2K) { this.nEQ2K = nEQ2K; }
    public void setEQ1_6K(int nEQ1_6K) { this.nEQ1_6K = nEQ1_6K; }
    public void setEQ1_25K(int nEQ1_25K) { this.nEQ1_25K = nEQ1_25K; }
    public void setEQ1K(int nEQ1K) { this.nEQ1K = nEQ1K; }
    public void setEQ800(int nEQ800) { this.nEQ800 = nEQ800; }
    public void setEQ630(int nEQ630) { this.nEQ630 = nEQ630; }
    public void setEQ500(int nEQ500) { this.nEQ500 = nEQ500; }
    public void setEQ400(int nEQ400) { this.nEQ400 = nEQ400; }
    public void setEQ315(int nEQ315) { this.nEQ315 = nEQ315; }
    public void setEQ250(int nEQ250) { this.nEQ250 = nEQ250; }
    public void setEQ200(int nEQ200) { this.nEQ200 = nEQ200; }
    public void setEQ160(int nEQ160) { this.nEQ160 = nEQ160; }
    public void setEQ125(int nEQ125) { this.nEQ125 = nEQ125; }
    public void setEQ100(int nEQ100) { this.nEQ100 = nEQ100; }
    public void setEQ80(int nEQ80) { this.nEQ80 = nEQ80; }
    public void setEQ63(int nEQ63) { this.nEQ63 = nEQ63; }
    public void setEQ50(int nEQ50) { this.nEQ50 = nEQ50; }
    public void setEQ40(int nEQ40) { this.nEQ40 = nEQ40; }
    public void setEQ31_5(int nEQ31_5) { this.nEQ31_5 = nEQ31_5; }
    public void setEQ25(int nEQ25) { this.nEQ25 = nEQ25; }
    public void setEQ20(int nEQ20) { this.nEQ20 = nEQ20; }
    public void setEffectItems(ArrayList<EffectItem> arEffectItems) {
        this.arEffectItems = new ArrayList<>();
        for(int i = 0; i < arEffectItems.size(); i++) {
            EffectItem itemFrom = arEffectItems.get(i);
            EffectItem itemTo = new EffectItem();
            itemTo.setEffectName(itemFrom.getEffectName());
            itemTo.setSelected(itemFrom.isSelected());
            itemTo.setbEditEnabled(itemFrom.isEditEnabled());
            this.arEffectItems.add(itemTo);
        }
    }
    public void setPan(float fPan) { this.fPan = fPan; }
    public void setFreq(float fFreq) { this.fFreq = fFreq; }
    public void setBPM(int nBPM) { this.nBPM = nBPM; }
    public void setVol1(float fVol1) { this.fVol1 = fVol1; }
    public void setVol2(float fVol2) { this.fVol2 = fVol2; }
    public void setVol3(float fVol3) { this.fVol3 = fVol3; }
    public void setVol4(float fVol4) { this.fVol4 = fVol4; }
    public void setVol5(float fVol5) { this.fVol5 = fVol5; }
    public void setVol6(float fVol6) { this.fVol6 = fVol6; }
    public void setVol7(float fVol7) { this.fVol7 = fVol7; }
    public void setTimeOfIncreaseSpeed(float fTimeOfIncreaseSpeed) { this.fTimeOfIncreaseSpeed = fTimeOfIncreaseSpeed; }
    public void setIncreaseSpeed(float fIncreaseSpeed) { this.fIncreaseSpeed = fIncreaseSpeed; }
    public void setIsABLoop(boolean bABLoop) { this.bABLoop = bABLoop; }
    public void setIsLoop(boolean bLoop) { this.bLoop = bLoop; }
    public void setIsLoopA(boolean bLoopA) { this.bLoopA = bLoopA; }
    public void setLoopA(double dLoopA) { this.dLoopA = dLoopA; }
    public void setIsLoopB(boolean bLoopB) { this.bLoopB = bLoopB; }
    public void setLoopB(double dLoopB) { this.dLoopB = dLoopB; }
    public void setArMarkerTime(ArrayList<Double> arMarkerTime) { this.arMarkerTime = (ArrayList<Double>)arMarkerTime.clone(); }
    public void setIsLoopMarker(boolean bLoopMarker) { this.bLoopMarker = bLoopMarker; }
    public void setMarker(int nMarker) { this.nMarker = nMarker; }

    public boolean isSave() { return bSave; }
    public float getSpeed() { return fSpeed; }
    public float getPitch() { return fPitch; }
    public int getVol() { return nVol; }
    public int getEQ20K() { return nEQ20K; }
    public int getEQ16K() { return nEQ16K; }
    public int getEQ12_5K() { return nEQ12_5K; }
    public int getEQ10K() { return nEQ10K; }
    public int getEQ8K() { return nEQ8K; }
    public int getEQ6_3K() { return nEQ6_3K; }
    public int getEQ5K() { return nEQ5K; }
    public int getEQ4K() { return nEQ4K; }
    public int getEQ3_15K() { return nEQ3_15K; }
    public int getEQ2_5K() { return nEQ2_5K; }
    public int getEQ2K() { return nEQ2K; }
    public int getEQ1_6K() { return nEQ1_6K; }
    public int getEQ1_25K() { return nEQ1_25K; }
    public int getEQ1K() { return nEQ1K; }
    public int getEQ800() { return nEQ800; }
    public int getEQ630() { return nEQ630; }
    public int getEQ500() { return nEQ500; }
    public int getEQ400() { return nEQ400; }
    public int getEQ315() { return nEQ315; }
    public int getEQ250() { return nEQ250; }
    public int getEQ200() { return nEQ200; }
    public int getEQ160() { return nEQ160; }
    public int getEQ125() { return nEQ125; }
    public int getEQ100() { return nEQ100; }
    public int getEQ80() { return nEQ80; }
    public int getEQ63() { return nEQ63; }
    public int getEQ50() { return nEQ50; }
    public int getEQ40() { return nEQ40; }
    public int getEQ31_5() { return nEQ31_5; }
    public int getEQ25() { return nEQ25; }
    public int getEQ20() { return nEQ20; }
    public ArrayList<EffectItem> getEffectItems() { return arEffectItems; }
    public float getPan() { return fPan; }
    public float getFreq() { return fFreq; }
    public int getBPM() { return nBPM; }
    public float getVol1() { return fVol1; }
    public float getVol2() { return fVol2; }
    public float getVol3() { return fVol3; }
    public float getVol4() { return fVol4; }
    public float getVol5() { return fVol5; }
    public float getVol6() { return fVol6; }
    public float getVol7() { return fVol7; }
    public float getTimeOfIncreaseSpeed() { return fTimeOfIncreaseSpeed; }
    public float getIncreaseSpeed() { return fIncreaseSpeed; }
    public boolean isABLoop() { return bABLoop; }
    public boolean isLoop() { return bLoop; }
    public boolean isLoopA() { return bLoopA; }
    public double getLoopA() { return dLoopA; }
    public boolean isLoopB() { return bLoopB; }
    public double getLoopB() { return dLoopB; }
    public ArrayList<Double> getArMarkerTime() { return arMarkerTime; }
    public boolean isLoopMarker() { return bLoopMarker; }
    public int getMarker() { return nMarker; }

    public EffectSaver() {
    }

    public EffectSaver(EffectSaver saver) {
        bSave = saver.bSave;
        fSpeed = saver.fSpeed;
        fPitch = saver.fPitch;
        nVol = saver.nVol;
        nEQ20K = saver.nEQ20K;
        nEQ16K = saver.nEQ16K;
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
        if(saver.arEffectItems == null)
            arEffectItems = null;
        else
            arEffectItems = (ArrayList<EffectItem>)saver.arEffectItems.clone();
        fPan = saver.fPan;
        fFreq = saver.fFreq;
        nBPM = saver.nBPM;
        fVol1 = saver.fVol1;
        fVol2 = saver.fVol2;
        fVol3 = saver.fVol3;
        fVol4 = saver.fVol4;
        fVol5 = saver.fVol5;
        fVol6 = saver.fVol6;
        fVol7 = saver.fVol7;
        fTimeOfIncreaseSpeed = saver.fTimeOfIncreaseSpeed;
        fIncreaseSpeed = saver.fIncreaseSpeed;
        bABLoop = saver.bABLoop;
        bLoop = saver.bLoop;
        bLoopA = saver.bLoopA;
        dLoopA = saver.dLoopA;
        bLoopB = saver.bLoopB;
        dLoopB = saver.dLoopB;
        if(saver.arMarkerTime == null)
            arMarkerTime = null;
        else
            arMarkerTime = (ArrayList<Double>)saver.arMarkerTime.clone();
        bLoopMarker = saver.bLoopMarker;
        nMarker = saver.nMarker;
    }
}
