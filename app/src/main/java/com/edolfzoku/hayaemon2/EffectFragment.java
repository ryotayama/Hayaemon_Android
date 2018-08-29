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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS_FX;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static java.lang.Boolean.FALSE;

public class EffectFragment extends Fragment implements AdapterView.OnItemClickListener {
    private ListView listView;
    private ArrayList<String> arPresetTitle;
    private int hDspVocalCancel = 0;
    private int hDspMonoral = 0;
    private int hDspLeft = 0;
    private int hDspRight = 0;
    private int hDspExchange = 0;
    private int hFxEcho = 0;
    private int hFxReverb = 0;
    private int hFxChorus = 0;
    private int hFxDistortion = 0;
    private final int kEffectTypeVocalCancel = 1;
    private final int kEffectTypeMonoral = 2;
    private final int kEffectTypeLeftOnly = 3;
    private final int kEffectTypeRightOnly = 4;
    private final int kEffectTypeReplace = 5;
    private final int kEffectTypeStadiumEcho = 6;
    private final int kEffectTypeHallEcho = 7;
    private final int kEffectTypeLiveHouseEcho = 8;
    private final int kEffectTypeRoomEcho = 9;
    private final int kEffectTypeBathroomEcho = 10;
    private final int kEffectTypeVocalEcho = 11;
    private final int kEffectTypeMountainEcho = 12;
    private final int kEffectTypeReverb_Bathroom = 13;
    private final int kEffectTypeReverb_SmallRoom = 14;
    private final int kEffectTypeReverb_MediumRoom = 15;
    private final int kEffectTypeReverb_LargeRoom = 16;
    private final int kEffectTypeReverb_Church = 17;
    private final int kEffectTypeReverb_Cathedral = 18;
    private final int kEffectTypeChorus = 19;
    private final int kEffectTypeFlanger = 20;
    private final int kEffectTypeDistortion_Strong = 21;
    private final int kEffectTypeDistortion_Middle = 22;
    private final int kEffectTypeDistortion_Weak = 23;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_effect, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        arPresetTitle = new ArrayList<>();
        arPresetTitle.add("なし");
        arPresetTitle.add("ボーカルキャンセル");
        arPresetTitle.add("モノラル");
        arPresetTitle.add("左のみ再生");
        arPresetTitle.add("右のみ再生");
        arPresetTitle.add("左右入れ替え");
        arPresetTitle.add("スタジアムエコー");
        arPresetTitle.add("ホールエコー");
        arPresetTitle.add("ライブハウスエコー");
        arPresetTitle.add("ルームエコー");
        arPresetTitle.add("バスルームエコー");
        arPresetTitle.add("ボーカルエコー");
        arPresetTitle.add("やまびこエコー");
        arPresetTitle.add("リバーブ（バスルーム）");
        arPresetTitle.add("リバーブ（狭い部屋）");
        arPresetTitle.add("リバーブ（普通の部屋）");
        arPresetTitle.add("リバーブ（広い部屋）");
        arPresetTitle.add("リバーブ（教会）");
        arPresetTitle.add("リバーブ（大聖堂）");
        arPresetTitle.add("コーラス");
        arPresetTitle.add("フランジャー");
        arPresetTitle.add("ディストーション（強）");
        arPresetTitle.add("ディストーション（中）");
        arPresetTitle.add("ディストーション（弱）");

        listView = (ListView)getActivity().findViewById(R.id.effectPresets);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(
                        getActivity(),
                        android.R.layout.simple_list_item_multiple_choice,
                        arPresetTitle);

        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);
        listView.setItemChecked(0, true);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
    {
        listView.setItemChecked(position, listView.isItemChecked(position));
        checkDuplicate(position);
        applyEffect(MainActivity.hStream);
    }

    public void checkDuplicate(int nSelect)
    {
        if(nSelect == 0)
        {
            for(int i = 1; i < arPresetTitle.size(); i++)
            {
                listView.setItemChecked(i, false);
            }
        }
        else
        {
            listView.setItemChecked(0, false);
            if(kEffectTypeVocalCancel <= nSelect && nSelect <= kEffectTypeReplace) {
                for (int i = kEffectTypeVocalCancel; i <= kEffectTypeReplace; i++) {
                    if(i != nSelect)
                        listView.setItemChecked(i, false);
                }
            }
            if(kEffectTypeStadiumEcho <= nSelect && nSelect <= kEffectTypeMountainEcho) {
                for(int i = kEffectTypeStadiumEcho; i <= kEffectTypeMountainEcho; i++) {
                    if(i != nSelect)
                        listView.setItemChecked(i, false);
                }
            }
            if(kEffectTypeReverb_Bathroom <= nSelect && nSelect <= kEffectTypeReverb_Cathedral) {
                for(int i = kEffectTypeReverb_Bathroom; i <= kEffectTypeReverb_Cathedral; i++) {
                    if(nSelect != i)
                        listView.setItemChecked(i, false);
                }
            }
            if(kEffectTypeChorus <= nSelect && nSelect <= kEffectTypeFlanger) {
                for(int i = kEffectTypeChorus; i <= kEffectTypeFlanger; i++) {
                    if(nSelect != i)
                        listView.setItemChecked(i, false);
                }
            }
            if(kEffectTypeDistortion_Strong <= nSelect && nSelect <= kEffectTypeDistortion_Weak) {
                for(int i = kEffectTypeDistortion_Strong; i <= kEffectTypeDistortion_Weak; i++) {
                    if(i != nSelect)
                        listView.setItemChecked(i, false);
                }
            }
        }
    }

    public void applyEffect(int hStream)
    {
        if(hDspVocalCancel != 0)
        {
            BASS.BASS_ChannelRemoveDSP(hStream, hDspVocalCancel);
            hDspVocalCancel = 0;
        }
        if(hDspMonoral != 0)
        {
            BASS.BASS_ChannelRemoveDSP(hStream, hDspMonoral);
            hDspMonoral = 0;
        }
        if(hDspLeft != 0)
        {
            BASS.BASS_ChannelRemoveDSP(hStream, hDspLeft);
            hDspLeft = 0;
        }
        if(hDspRight != 0)
        {
            BASS.BASS_ChannelRemoveDSP(hStream, hDspRight);
            hDspRight = 0;
        }
        if(hDspExchange != 0)
        {
            BASS.BASS_ChannelRemoveDSP(hStream, hDspExchange);
            hDspExchange = 0;
        }
        if(hFxEcho != 0) {
            BASS.BASS_ChannelRemoveFX(hStream, hFxEcho);
            hFxEcho = 0;
        }
        if(hFxReverb != 0) {
            BASS.BASS_ChannelRemoveFX(hStream, hFxReverb);
            hFxReverb = 0;
        }
        if(hFxChorus != 0) {
            BASS.BASS_ChannelRemoveFX(hStream, hFxChorus);
            hFxChorus = 0;
        }
        if(hFxDistortion != 0) {
            BASS.BASS_ChannelRemoveFX(hStream, hFxDistortion);
            hFxDistortion = 0;
        }
        for(int i = 0; i < arPresetTitle.size(); i++)
        {
            if(!listView.isItemChecked(i))
                continue;
            String strEffect = arPresetTitle.get(i);
            if(strEffect.equals("なし"))
            {
            }
            else if(strEffect.equals("ボーカルキャンセル"))
                hDspVocalCancel = BASS.BASS_ChannelSetDSP(hStream, vocalCancelDSP, null, 0);
            else if(strEffect.equals("モノラル"))
                hDspMonoral = BASS.BASS_ChannelSetDSP(hStream, monoralDSP, null, 0);
            else if(strEffect.equals("左のみ再生"))
                hDspLeft = BASS.BASS_ChannelSetDSP(hStream, leftDSP, null, 0);
            else if(strEffect.equals("右のみ再生"))
                hDspRight = BASS.BASS_ChannelSetDSP(hStream, rightDSP, null, 0);
            else if(strEffect.equals("左右入れ替え"))
                hDspExchange = BASS.BASS_ChannelSetDSP(hStream, exchangeDSP, null, 0);
            else if(strEffect.equals("スタジアムエコー"))
            {
                hFxEcho = BASS.BASS_ChannelSetFX(hStream, BASS.BASS_FX_DX8_ECHO, 2);
                BASS.BASS_DX8_ECHO echo = new BASS.BASS_DX8_ECHO();
                echo.fFeedback = (float)55.0;
                echo.fLeftDelay = (float)400.0;
                echo.fRightDelay = (float)400.0;
                echo.fWetDryMix = (float)10.0;
                echo.lPanDelay = FALSE;
                BASS.BASS_FXSetParameters(hFxEcho, echo);
            }
            else if(strEffect.equals("ホールエコー"))
            {
                hFxEcho = BASS.BASS_ChannelSetFX(hStream, BASS.BASS_FX_DX8_ECHO, 2);
                BASS.BASS_DX8_ECHO echo = new BASS.BASS_DX8_ECHO();
                echo.fFeedback = (float)50.0;
                echo.fLeftDelay = (float)300.0;
                echo.fRightDelay = (float)300.0;
                echo.fWetDryMix = (float)10.0;
                echo.lPanDelay = FALSE;
                BASS.BASS_FXSetParameters(hFxEcho, echo);
            }
            else if(strEffect.equals("ライブハウスエコー"))
            {
                hFxEcho = BASS.BASS_ChannelSetFX(hStream, BASS.BASS_FX_DX8_ECHO, 2);
                BASS.BASS_DX8_ECHO echo = new BASS.BASS_DX8_ECHO();
                echo.fFeedback = (float)30.0;
                echo.fLeftDelay = (float)200.0;
                echo.fRightDelay = (float)200.0;
                echo.fWetDryMix = (float)11.1;
                echo.lPanDelay = FALSE;
                BASS.BASS_FXSetParameters(hFxEcho, echo);
            }
            else if(strEffect.equals("ルームエコー"))
            {
                hFxEcho = BASS.BASS_ChannelSetFX(hStream, BASS.BASS_FX_DX8_ECHO, 2);
                BASS.BASS_DX8_ECHO echo = new BASS.BASS_DX8_ECHO();
                echo.fFeedback = (float)50.0;
                echo.fLeftDelay = (float)100.0;
                echo.fRightDelay = (float)100.0;
                echo.fWetDryMix = (float)13.0;
                echo.lPanDelay = FALSE;
                BASS.BASS_FXSetParameters(hFxEcho, echo);
            }
            else if(strEffect.equals("バスルームエコー"))
            {
                hFxEcho = BASS.BASS_ChannelSetFX(hStream, BASS.BASS_FX_DX8_ECHO, 2);
                BASS.BASS_DX8_ECHO echo = new BASS.BASS_DX8_ECHO();
                echo.fFeedback = (float)60.0;
                echo.fLeftDelay = (float)75.0;
                echo.fRightDelay = (float)75.0;
                echo.fWetDryMix = (float)23.0;
                echo.lPanDelay = FALSE;
                BASS.BASS_FXSetParameters(hFxEcho, echo);
            }
            else if(strEffect.equals("ボーカルエコー"))
            {
                hFxEcho = BASS.BASS_ChannelSetFX(hStream, BASS.BASS_FX_DX8_ECHO, 2);
                BASS.BASS_DX8_ECHO echo = new BASS.BASS_DX8_ECHO();
                echo.fFeedback = (float)40.0;
                echo.fLeftDelay = (float)350.0;
                echo.fRightDelay = (float)350.0;
                echo.fWetDryMix = (float)13.0;
                echo.lPanDelay = FALSE;
                BASS.BASS_FXSetParameters(hFxEcho, echo);
            }
            else if(strEffect.equals("やまびこエコー"))
            {
                hFxEcho = BASS.BASS_ChannelSetFX(hStream, BASS.BASS_FX_DX8_ECHO, 2);
                BASS.BASS_DX8_ECHO echo = new BASS.BASS_DX8_ECHO();
                echo.fFeedback = (float)0.0;
                echo.fLeftDelay = (float)1000.0;
                echo.fRightDelay = (float)1000.0;
                echo.fWetDryMix = (float)16.6;
                echo.lPanDelay = FALSE;
                BASS.BASS_FXSetParameters(hFxEcho, echo);
            }
            else if(strEffect.equals("リバーブ（バスルーム）"))
            {
                hFxReverb = BASS.BASS_ChannelSetFX(hStream, BASS_FX.BASS_FX_BFX_FREEVERB, 2);
                BASS_FX.BASS_BFX_FREEVERB reverb = new BASS_FX.BASS_BFX_FREEVERB();
                reverb.fDryMix = (float)1.0;
                reverb.fWetMix = (float)2.0;
                reverb.fRoomSize = (float)0.16;
                reverb.fDamp = (float)0.5;
                reverb.fWidth = (float)1.0;
                reverb.lMode = 0;
                reverb.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(hFxReverb, reverb);
            }
            else if(strEffect.equals("リバーブ（狭い部屋）"))
            {
                hFxReverb = BASS.BASS_ChannelSetFX(hStream, BASS_FX.BASS_FX_BFX_FREEVERB, 2);
                BASS_FX.BASS_BFX_FREEVERB reverb = new BASS_FX.BASS_BFX_FREEVERB();
                reverb.fDryMix = (float)0.95;
                reverb.fWetMix = (float)0.995;
                reverb.fRoomSize = (float)0.3;
                reverb.fDamp = (float)0.5;
                reverb.fWidth = (float)1.0;
                reverb.lMode = 0;
                reverb.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(hFxReverb, reverb);
            }
            else if(strEffect.equals("リバーブ（普通の部屋）"))
            {
                hFxReverb = BASS.BASS_ChannelSetFX(hStream, BASS_FX.BASS_FX_BFX_FREEVERB, 2);
                BASS_FX.BASS_BFX_FREEVERB reverb = new BASS_FX.BASS_BFX_FREEVERB();
                reverb.fDryMix = (float)0.95;
                reverb.fWetMix = (float)0.995;
                reverb.fRoomSize = (float)0.75;
                reverb.fDamp = (float)0.5;
                reverb.fWidth = (float)0.7;
                reverb.lMode = 0;
                reverb.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(hFxReverb, reverb);
            }
            else if(strEffect.equals("リバーブ（広い部屋）"))
            {
                hFxReverb = BASS.BASS_ChannelSetFX(hStream, BASS_FX.BASS_FX_BFX_FREEVERB, 2);
                BASS_FX.BASS_BFX_FREEVERB reverb = new BASS_FX.BASS_BFX_FREEVERB();
                reverb.fDryMix = (float)0.7;
                reverb.fWetMix = (float)1.0;
                reverb.fRoomSize = (float)0.85;
                reverb.fDamp = (float)0.5;
                reverb.fWidth = (float)0.9;
                reverb.lMode = 0;
                reverb.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(hFxReverb, reverb);
            }
            else if(strEffect.equals("リバーブ（教会）"))
            {
                hFxReverb = BASS.BASS_ChannelSetFX(hStream, BASS_FX.BASS_FX_BFX_FREEVERB, 2);
                BASS_FX.BASS_BFX_FREEVERB reverb = new BASS_FX.BASS_BFX_FREEVERB();
                reverb.fDryMix = (float)0.4;
                reverb.fWetMix = (float)1.0;
                reverb.fRoomSize = (float)0.9;
                reverb.fDamp = (float)0.5;
                reverb.fWidth = (float)1.0;
                reverb.lMode = 0;
                reverb.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(hFxReverb, reverb);
            }
            else if(strEffect.equals("リバーブ（大聖堂）"))
            {
                hFxReverb = BASS.BASS_ChannelSetFX(hStream, BASS_FX.BASS_FX_BFX_FREEVERB, 2);
                BASS_FX.BASS_BFX_FREEVERB reverb = new BASS_FX.BASS_BFX_FREEVERB();
                reverb.fDryMix = (float)0.0;
                reverb.fWetMix = (float)1.0;
                reverb.fRoomSize = (float)0.9;
                reverb.fDamp = (float)0.5;
                reverb.fWidth = (float)1.0;
                reverb.lMode = 0;
                reverb.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(hFxReverb, reverb);
            }
            else if(strEffect.equals("コーラス"))
            {
                hFxChorus = BASS.BASS_ChannelSetFX(hStream, BASS_FX.BASS_FX_BFX_CHORUS, 2);
                BASS_FX.BASS_BFX_CHORUS chorus = new BASS_FX.BASS_BFX_CHORUS();
                chorus.fDryMix = (float)0.5;
                chorus.fWetMix = (float)0.2;
                chorus.fFeedback = (float)0.5;
                chorus.fMinSweep = (float)1.0;
                chorus.fMaxSweep = (float)2.0;
                chorus.fRate = (float)10.0;
                chorus.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(hFxChorus, chorus);
            }
            else if(strEffect.equals("フランジャー"))
            {
                hFxChorus = BASS.BASS_ChannelSetFX(hStream, BASS_FX.BASS_FX_BFX_CHORUS, 2);
                BASS_FX.BASS_BFX_CHORUS chorus = new BASS_FX.BASS_BFX_CHORUS();
                chorus.fDryMix = (float)0.25;
                chorus.fWetMix = (float)0.4;
                chorus.fFeedback = (float)0.5;
                chorus.fMinSweep = (float)1.0;
                chorus.fMaxSweep = (float)5.0;
                chorus.fRate = (float)1.0;
                chorus.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(hFxChorus, chorus);
            }
            else if(strEffect.equals("ディストーション（強）"))
            {
                hFxDistortion = BASS.BASS_ChannelSetFX(hStream, BASS_FX.BASS_FX_BFX_DISTORTION, 2);
                BASS_FX.BASS_BFX_DISTORTION distortion = new BASS_FX.BASS_BFX_DISTORTION();
                distortion.fDrive = (float)0.2;
                distortion.fDryMix = (float)0.96;
                distortion.fWetMix = (float)0.03;
                distortion.fFeedback = (float)0.1;
                distortion.fVolume = (float)1.0;
                distortion.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(hFxDistortion, distortion);
            }
            else if(strEffect.equals("ディストーション（中）"))
            {
                hFxDistortion = BASS.BASS_ChannelSetFX(hStream, BASS_FX.BASS_FX_BFX_DISTORTION, 2);
                BASS_FX.BASS_BFX_DISTORTION distortion = new BASS_FX.BASS_BFX_DISTORTION();
                distortion.fDrive = (float)0.2;
                distortion.fDryMix = (float)0.97;
                distortion.fWetMix = (float)0.02;
                distortion.fFeedback = (float)0.1;
                distortion.fVolume = (float)1.0;
                distortion.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(hFxDistortion, distortion);
            }
            else if(strEffect.equals("ディストーション（弱）"))
            {
                hFxDistortion = BASS.BASS_ChannelSetFX(hStream, BASS_FX.BASS_FX_BFX_DISTORTION, 2);
                BASS_FX.BASS_BFX_DISTORTION distortion = new BASS_FX.BASS_BFX_DISTORTION();
                distortion.fDrive = (float)0.2;
                distortion.fDryMix = (float)0.98;
                distortion.fWetMix = (float)0.01;
                distortion.fFeedback = (float)0.1;
                distortion.fVolume = (float)1.0;
                distortion.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(hFxDistortion, distortion);
            }
        }
    }

    private final BASS.DSPPROC vocalCancelDSP = new BASS.DSPPROC() {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            for(int a = 0; a < length / 4; a += 2) {
                b[a] = b[a + 1] = (-b[a] + b[a + 1]) * 0.5f;
            }
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    private final BASS.DSPPROC monoralDSP = new BASS.DSPPROC() {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            for(int a = 0; a < length / 4; a += 2) {
                b[a] = b[a + 1] = (b[a] + b[a + 1]) * 0.5f;
            }
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    private final BASS.DSPPROC leftDSP = new BASS.DSPPROC() {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            for(int a = 0; a < length / 4; a += 2) {
                b[a + 1] = b[a];
            }
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    private final BASS.DSPPROC rightDSP = new BASS.DSPPROC() {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            for(int a = 0; a < length / 4; a += 2) {
                b[a] = b[a + 1];
            }
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };

    private final BASS.DSPPROC exchangeDSP = new BASS.DSPPROC() {
        public void DSPPROC(int handle, int channel, ByteBuffer buffer, int length, Object user) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer ibuffer = buffer.asFloatBuffer();
            float[] b = new float[length / 4];
            ibuffer.get(b);
            for(int a = 0; a < length / 4; a += 2) {
                float fTemp = b[a];
                b[a] = b[a + 1];
                b[a + 1] = fTemp;
            }
            ibuffer.rewind();
            ibuffer.put(b);
        }
    };
}
