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

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
import com.un4seen.bass.BASSFLAC;
import com.un4seen.bass.BASS_AAC;
import com.un4seen.bass.BASS_FX;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;

import static java.lang.Boolean.FALSE;

public class EffectFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener, SeekBar.OnSeekBarChangeListener, View.OnFocusChangeListener
{
    private MainActivity activity = null;
    private RecyclerView recyclerEffects;
    private EffectsAdapter effectsAdapter;
    private ArrayList<EffectItem> arEffectItems;
    private int hDspVocalCancel = 0;
    private int hDspMonoral = 0;
    private int hDspLeft = 0;
    private int hDspRight = 0;
    private int hDspExchange = 0;
    private int hDspDoubling = 0;
    private int hDspPan = 0;
    private int hDspNormalize = 0;
    private int hFxCompressor = 0;
    private int hDspPhaseReversal = 0;
    private int hFxEcho = 0;
    private int hFxReverb = 0;
    private int hFxChorus = 0;
    private int hFxDistortion = 0;
    private float fPan = 0.0f;
    private float fFreq = 1.0f;
    private int nBPM = 120;
    private float fVol1 = 1.0f;
    private float fVol2 = 1.0f;
    private float fVol3 = 1.0f;
    private float fVol4 = 1.0f;
    private float fVol5 = 1.0f;
    private float fVol6 = 1.0f;
    private float fVol7 = 1.0f;
    private float fPeak = 0.0f;
    private float fTimeOfIncreaseSpeed = 1.0f;
    private float fIncreaseSpeed = 0.1f;
    private float fTimeOfDecreaseSpeed = 1.0f;
    private float fDecreaseSpeed = 0.1f;
    static final int kEffectTypeRandom = 1;
    static final int kEffectTypeVocalCancel = 2;
    static final int kEffectTypeMonoral = 3;
    static final int kEffectTypeLeftOnly = 4;
    static final int kEffectTypeRightOnly = 5;
    static final int kEffectTypeReplace = 6;
    static final int kEffectTypeDoubling = 7;
    static final int kEffectTypeTranscribeSideGuitar = 8;
    static final int kEffectTypeTranscribeBass = 9;
    static final int kEffectTypePan = 10;
    static final int kEffectTypeNormalize = 11;
    static final int kEffectTypeCompressor = 12;
    static final int kEffectTypeFrequency = 13;
    static final int kEffectTypePhaseReversal = 14;
    static final int kEffectTypeStadiumEcho = 15;
    static final int kEffectTypeHallEcho = 16;
    static final int kEffectTypeLiveHouseEcho = 17;
    static final int kEffectTypeRoomEcho = 18;
    static final int kEffectTypeBathroomEcho = 19;
    static final int kEffectTypeVocalEcho = 20;
    static final int kEffectTypeMountainEcho = 21;
    static final int kEffectTypeReverb_Bathroom = 22;
    static final int kEffectTypeReverb_SmallRoom = 23;
    static final int kEffectTypeReverb_MediumRoom = 24;
    static final int kEffectTypeReverb_LargeRoom = 25;
    static final int kEffectTypeReverb_Church = 26;
    static final int kEffectTypeReverb_Cathedral = 27;
    static final int kEffectTypeChorus = 28;
    static final int kEffectTypeFlanger = 29;
    static final int kEffectTypeDistortion_Strong = 30;
    static final int kEffectTypeDistortion_Middle = 31;
    static final int kEffectTypeDistortion_Weak = 32;
    static final int kEffectTypeReverse = 33;
    static final int kEffectTypeIncreaseSpeed = 34;
    static final int kEffectTypeDecreaseSpeed = 35;
    static final int kEffectTypeOldRecord = 36;
    static final int kEffectTypeLowBattery = 37;
    static final int kEffectTypeNoSense_Strong = 38;
    static final int kEffectTypeNoSense_Middle = 39;
    static final int kEffectTypeNoSense_Weak = 40;
    static final int kEffectTypeEarTraining = 41;
    static final int kEffectTypeMetronome = 42;
    static final int kEffectTypeRecordNoise = 43;
    static final int kEffectTypeRoarOfWaves = 44;
    static final int kEffectTypeRain = 45;
    static final int kEffectTypeRiver = 46;
    static final int kEffectTypeWar = 47;
    static final int kEffectTypeFire = 48;
    static final int kEffectTypeConcertHall = 49;
    private Timer timer;
    private int hSEStream;
    private int hSEStream2;
    private boolean bSE1Playing = false;
    private int hSync = 0;
    private Handler handler;
    private float fAccel = 0.0f;
    private float fVelo1 = 0.0f;
    private float fVelo2 = 0.0f;
    private boolean isContinue = true;
    private Handler handlerLongClick;

    public void setPeak(float fPeak) { this.fPeak = fPeak; }
    public float getTimeOfIncreaseSpeed() { return fTimeOfIncreaseSpeed; }
    public void setTimeOfIncreaseSpeed(float fTimeOfIncreaseSpeed) {
        this.fTimeOfIncreaseSpeed = fTimeOfIncreaseSpeed;
        TextView textEffectName = activity.findViewById(R.id.textEffectName);
        if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeIncreaseSpeed).getEffectName())) {
            EditText editTimeEffectDetail = getActivity().findViewById(R.id.editTimeEffectDetail);
            editTimeEffectDetail.setText(String.format("%.1f秒", fTimeOfIncreaseSpeed));
        }
    }
    public float getIncreaseSpeed() { return fIncreaseSpeed; }
    public void setIncreaseSpeed(float fIncreaseSpeed) {
        this.fIncreaseSpeed = fIncreaseSpeed;
        TextView textEffectName = activity.findViewById(R.id.textEffectName);
        if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeIncreaseSpeed).getEffectName())) {
            EditText editSpeedEffectDetail = getActivity().findViewById(R.id.editSpeedEffectDetail);
            editSpeedEffectDetail.setText(String.format("%.1f%%", fIncreaseSpeed));
        }
    }
    public float getTimeOfDecreaseSpeed() { return fTimeOfDecreaseSpeed; }
    public void setTimeOfDecreaseSpeed(float fTimeOfDecreaseSpeed) {
        this.fTimeOfDecreaseSpeed = fTimeOfDecreaseSpeed;
        TextView textEffectName = activity.findViewById(R.id.textEffectName);
        if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeDecreaseSpeed).getEffectName())) {
            EditText editTimeEffectDetail = getActivity().findViewById(R.id.editTimeEffectDetail);
            editTimeEffectDetail.setText(String.format("%.1f秒", fTimeOfDecreaseSpeed));
        }
    }
    public float getDecreaseSpeed() { return fDecreaseSpeed; }
    public void setDecreaseSpeed(float fDecreaseSpeed) {
        this.fDecreaseSpeed = fDecreaseSpeed;
        TextView textEffectName = activity.findViewById(R.id.textEffectName);
        if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeDecreaseSpeed).getEffectName())) {
            EditText editSpeedEffectDetail = getActivity().findViewById(R.id.editSpeedEffectDetail);
            editSpeedEffectDetail.setText(String.format("%.1f%%", fDecreaseSpeed));
        }
    }

    public boolean isSelectedItem(int nItem)
    {
        if(nItem >= arEffectItems.size()) return false;
        EffectItem item = arEffectItems.get(nItem);
        return item.isSelected();
    }

    public boolean isReverse()
    {
        return arEffectItems.get(kEffectTypeReverse).isSelected();
    }

    public ArrayList<EffectItem> getEffectItems()
    {
         return arEffectItems;
    }

    public void setEffectItems(ArrayList<EffectItem> arEffectItems)
    {
        for(int i = 0; i < this.arEffectItems.size(); i++)
        {
            EffectItem item = this.arEffectItems.get(i);
            for(int j = 0; j < arEffectItems.size(); j++)
            {
                EffectItem itemSaved = arEffectItems.get(j);
                if(item.getEffectName().equals(itemSaved.getEffectName()))
                    item.setSelected(itemSaved.isSelected());
            }
        }

        effectsAdapter.notifyDataSetChanged();
    }

    public float getPan()
    {
        return fPan;
    }

    public float getFreq()
    {
        return fFreq;
    }

    public int getBPM()
    {
        return nBPM;
    }

    public void setBPM(int nBPM)
    {
        this.nBPM = nBPM;
    }

    public float getVol1()
    {
        return fVol1;
    }

    public void setVol1(float fVol1)
    {
        this.fVol1 = fVol1;
    }

    public float getVol2()
    {
        return fVol2;
    }

    public void setVol2(float fVol1)
    {
        this.fVol2 = fVol2;
    }

    public float getVol3()
    {
        return fVol3;
    }

    public void setVol3(float fVol1)
    {
        this.fVol3 = fVol3;
    }

    public float getVol4()
    {
        return fVol4;
    }

    public void setVol4(float fVol1)
    {
        this.fVol4 = fVol4;
    }

    public float getVol5()
    {
        return fVol5;
    }

    public void setVol5(float fVol1)
    {
        this.fVol5 = fVol5;
    }

    public float getVol6()
    {
        return fVol6;
    }

    public void setVol6(float fVol1)
    {
        this.fVol6 = fVol6;
    }

    public float getVol7()
    {
        return fVol7;
    }

    public void setVol7(float fVol1)
    {
        this.fVol7 = fVol7;
    }

    public EffectFragment()
    {
        arEffectItems = new ArrayList<>();
        handlerLongClick = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_effect, container, false);
        return rootView;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context != null && context instanceof MainActivity)
            activity = (MainActivity) context;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.btnFinish)
        {
            RelativeLayout relativeEffectDetail = (RelativeLayout) activity.findViewById(R.id.relativeEffectDetail);
            relativeEffectDetail.setVisibility(View.GONE);
            RelativeLayout relativeEffect = (RelativeLayout) activity.findViewById(R.id.relativeEffects);
            relativeEffect.setVisibility(View.VISIBLE);
        }
        else if (v.getId() == R.id.buttonEffectMinus)
            minusValue();
        else if (v.getId() == R.id.buttonEffectPlus)
            plusValue();
    }

    @Override
    public boolean onLongClick(View v)
    {
        if (v.getId() == R.id.buttonEffectMinus)
        {
            isContinue = true;
            handlerLongClick.post(repeatMinusValue);
            return true;
        }
        else if (v.getId() == R.id.buttonEffectPlus)
        {
            isContinue = true;
            handlerLongClick.post(repeatPlusValue);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP)
            isContinue = false;
        return false;
    }

    Runnable repeatMinusValue = new Runnable()
    {
        @Override
        public void run()
        {
            if(!isContinue)
                return;
            minusValue();
            handlerLongClick.postDelayed(this, 100);
        }
    };

    Runnable repeatPlusValue = new Runnable()
    {
        @Override
        public void run()
        {
            if(!isContinue)
                return;
            plusValue();
            handlerLongClick.postDelayed(this, 100);
        }
    };

    public void minusValue()
    {
        TextView textEffectName = (TextView) activity.findViewById(R.id.textEffectName);
        SeekBar seek = (SeekBar) activity.findViewById(R.id.seekEffectDetail);
        TextView textEffectDetail = (TextView) activity.findViewById(R.id.textEffectDetail);
        int nProgress = seek.getProgress();
        nProgress -= 1;
        if(nProgress < 0) nProgress = 0;
        seek.setProgress(nProgress);
        if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypePan).getEffectName()))
        {
            float fProgress = (nProgress - 100) / 100.0f;
            textEffectDetail.setText(String.format("%d", nProgress - 100));
            setPan(fProgress);
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeFrequency).getEffectName()))
        {
            double dProgress = (double)(nProgress + 1) / 10.0;
            textEffectDetail.setText(String.format("%.1f", dProgress));
            setFreq((float)dProgress);
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeMetronome).getEffectName()))
        {
            nBPM = nProgress + 10;
            textEffectDetail.setText(String.format("%d", nBPM));
            applyEffect();
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeRecordNoise).getEffectName()))
        {
            fVol1 = nProgress / 100.0f;
            textEffectDetail.setText(String.format("%d", nProgress));
            applyEffect();
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeRoarOfWaves).getEffectName()))
        {
            fVol2 = nProgress / 100.0f;
            textEffectDetail.setText(String.format("%d", nProgress));
            applyEffect();
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeRain).getEffectName()))
        {
            fVol3 = nProgress / 100.0f;
            textEffectDetail.setText(String.format("%d", nProgress));
            applyEffect();
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeRiver).getEffectName()))
        {
            fVol4 = nProgress / 100.0f;
            textEffectDetail.setText(String.format("%d", nProgress));
            applyEffect();
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeWar).getEffectName()))
        {
            fVol5 = nProgress / 100.0f;
            textEffectDetail.setText(String.format("%d", nProgress));
            applyEffect();
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeFire).getEffectName()))
        {
            fVol6 = nProgress / 100.0f;
            textEffectDetail.setText(String.format("%d", nProgress));
            applyEffect();
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeConcertHall).getEffectName()))
        {
            fVol7 = nProgress / 100.0f;
            textEffectDetail.setText(String.format("%d", nProgress));
            applyEffect();
        }
        MainActivity activity = (MainActivity)getActivity();
        PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
        playlistFragment.updateSavingEffect();
    }

    public void plusValue()
    {
        TextView textEffectName = (TextView) activity.findViewById(R.id.textEffectName);
        SeekBar seek = (SeekBar) activity.findViewById(R.id.seekEffectDetail);
        TextView textEffectDetail = (TextView) activity.findViewById(R.id.textEffectDetail);
        int nProgress = seek.getProgress();
        nProgress += 1;
        if(nProgress > seek.getMax()) nProgress = seek.getMax();
        seek.setProgress(nProgress);
        if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypePan).getEffectName()))
        {
            float fProgress = (nProgress - 100) / 100.0f;
            textEffectDetail.setText(String.format("%d", nProgress - 100));
            setPan(fProgress);
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeFrequency).getEffectName()))
        {
            double dProgress = (double)(nProgress + 1) / 10.0;
            textEffectDetail.setText(String.format("%.1f", dProgress));
            setFreq((float)dProgress);
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeMetronome).getEffectName()))
        {
            nBPM = nProgress + 10;
            textEffectDetail.setText(String.format("%d", nBPM));
            applyEffect();
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeRecordNoise).getEffectName()))
        {
            fVol1 = nProgress / 100.0f;
            textEffectDetail.setText(String.format("%d", nProgress));
            applyEffect();
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeRoarOfWaves).getEffectName()))
        {
            fVol2 = nProgress / 100.0f;
            textEffectDetail.setText(String.format("%d", nProgress));
            applyEffect();
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeRain).getEffectName()))
        {
            fVol3 = nProgress / 100.0f;
            textEffectDetail.setText(String.format("%d", nProgress));
            applyEffect();
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeRiver).getEffectName()))
        {
            fVol4 = nProgress / 100.0f;
            textEffectDetail.setText(String.format("%d", nProgress));
            applyEffect();
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeWar).getEffectName()))
        {
            fVol5 = nProgress / 100.0f;
            textEffectDetail.setText(String.format("%d", nProgress));
            applyEffect();
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeFire).getEffectName()))
        {
            fVol6 = nProgress / 100.0f;
            textEffectDetail.setText(String.format("%d", nProgress));
            applyEffect();
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeConcertHall).getEffectName()))
        {
            fVol7 = nProgress / 100.0f;
            textEffectDetail.setText(String.format("%d", nProgress));
            applyEffect();
        }
        MainActivity activity = (MainActivity)getActivity();
        PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
        playlistFragment.updateSavingEffect();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        effectsAdapter = new EffectsAdapter(activity, R.layout.effect_item, arEffectItems);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        EffectItem item = new EffectItem("オフ", false);
        arEffectItems.add(item);
        item = new EffectItem("ランダム", false);
        arEffectItems.add(item);
        item = new EffectItem("ボーカルキャンセル", false);
        arEffectItems.add(item);
        item = new EffectItem("モノラル", false);
        arEffectItems.add(item);
        item = new EffectItem("左のみ再生", false);
        arEffectItems.add(item);
        item = new EffectItem("右のみ再生", false);
        arEffectItems.add(item);
        item = new EffectItem("左右入れ替え", false);
        arEffectItems.add(item);
        item = new EffectItem("ダブリング", false);
        arEffectItems.add(item);
        item = new EffectItem("サイドギターの耳コピ", false);
        arEffectItems.add(item);
        item = new EffectItem("ベースの耳コピ（オクターブ上げ）", false);
        arEffectItems.add(item);
        item = new EffectItem("パン", true);
        arEffectItems.add(item);
        item = new EffectItem("ノーマライズ", false);
        arEffectItems.add(item);
        item = new EffectItem("コンプレッサー", false);
        arEffectItems.add(item);
        item = new EffectItem("再生周波数", true);
        arEffectItems.add(item);
        item = new EffectItem("位相反転", false);
        arEffectItems.add(item);
        item = new EffectItem("スタジアムエコー", false);
        arEffectItems.add(item);
        item = new EffectItem("ホールエコー", false);
        arEffectItems.add(item);
        item = new EffectItem("ライブハウスエコー", false);
        arEffectItems.add(item);
        item = new EffectItem("ルームエコー", false);
        arEffectItems.add(item);
        item = new EffectItem("バスルームエコー", false);
        arEffectItems.add(item);
        item = new EffectItem("ボーカルエコー", false);
        arEffectItems.add(item);
        item = new EffectItem("やまびこエコー", false);
        arEffectItems.add(item);
        item = new EffectItem("リバーブ（バスルーム）", false);
        arEffectItems.add(item);
        item = new EffectItem("リバーブ（狭い部屋）", false);
        arEffectItems.add(item);
        item = new EffectItem("リバーブ（普通の部屋）", false);
        arEffectItems.add(item);
        item = new EffectItem("リバーブ（広い部屋）", false);
        arEffectItems.add(item);
        item = new EffectItem("リバーブ（教会）", false);
        arEffectItems.add(item);
        item = new EffectItem("リバーブ（大聖堂）", false);
        arEffectItems.add(item);
        item = new EffectItem("コーラス", false);
        arEffectItems.add(item);
        item = new EffectItem("フランジャー", false);
        arEffectItems.add(item);
        item = new EffectItem("ディストーション（強）", false);
        arEffectItems.add(item);
        item = new EffectItem("ディストーション（中）", false);
        arEffectItems.add(item);
        item = new EffectItem("ディストーション（弱）", false);
        arEffectItems.add(item);
        item = new EffectItem("逆回転再生", false);
        arEffectItems.add(item);
        item = new EffectItem("だんだん速く", true);
        arEffectItems.add(item);
        item = new EffectItem("だんだん遅く", true);
        arEffectItems.add(item);
        item = new EffectItem("古びたレコード再生", false);
        arEffectItems.add(item);
        item = new EffectItem("電池切れ", false);
        arEffectItems.add(item);
        item = new EffectItem("歌へた（強）", false);
        arEffectItems.add(item);
        item = new EffectItem("歌へた（中）", false);
        arEffectItems.add(item);
        item = new EffectItem("歌へた（弱）", false);
        arEffectItems.add(item);
        item = new EffectItem("聴覚トレーニング", false);
        arEffectItems.add(item);
        item = new EffectItem("メトロノーム", true);
        arEffectItems.add(item);
        item = new EffectItem("レコードノイズ", true);
        arEffectItems.add(item);
        item = new EffectItem("波の音", true);
        arEffectItems.add(item);
        item = new EffectItem("雨の音", true);
        arEffectItems.add(item);
        item = new EffectItem("川の音", true);
        arEffectItems.add(item);
        item = new EffectItem("戦の音", true);
        arEffectItems.add(item);
        item = new EffectItem("焚き火", true);
        arEffectItems.add(item);
        item = new EffectItem("コンサート会場", true);
        arEffectItems.add(item);
        MainActivity activity = (MainActivity)getActivity();
        recyclerEffects = activity.findViewById(R.id.recyclerEffects);
        recyclerEffects.setHasFixedSize(false);
        LinearLayoutManager playlistsManager = new LinearLayoutManager(activity);
        recyclerEffects.setLayoutManager(playlistsManager);
        recyclerEffects.setAdapter(effectsAdapter);
        Button btnFinith = activity.findViewById(R.id.btnFinish);
        btnFinith.setOnClickListener(this);
        ImageButton buttonEffectMinus = activity.findViewById(R.id.buttonEffectMinus);
        buttonEffectMinus.setOnClickListener(this);
        buttonEffectMinus.setOnLongClickListener(this);
        buttonEffectMinus.setOnTouchListener(this);
        ImageButton buttonEffectPlus = activity.findViewById(R.id.buttonEffectPlus);
        buttonEffectPlus.setOnClickListener(this);
        buttonEffectPlus.setOnLongClickListener(this);
        buttonEffectPlus.setOnTouchListener(this);

        getActivity().findViewById(R.id.editTimeEffectDetail).setOnFocusChangeListener(this);
        getActivity().findViewById(R.id.editSpeedEffectDetail).setOnFocusChangeListener(this);
    }

    public void onEffectItemClick(int nEffect)
    {
        EffectItem item = arEffectItems.get(nEffect);
        item.setSelected(!item.isSelected());
        if(!item.isSelected() && nEffect == kEffectTypeReverse)
        {
            int chan = BASS_FX.BASS_FX_TempoGetSource(MainActivity.hStream);
            BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_FORWARD);
            activity.setSync();
        }
        if(!item.isSelected() && (nEffect == kEffectTypeRandom || nEffect == kEffectTypeOldRecord || nEffect == kEffectTypeLowBattery || nEffect == kEffectTypeEarTraining))
        {
            EqualizerFragment equalizerFragment = (EqualizerFragment)activity.mSectionsPagerAdapter.getItem(3);
            equalizerFragment.setEQ(0);
        }
        if(!item.isSelected() && (nEffect == kEffectTypeRandom || nEffect == kEffectTypeNoSense_Strong || nEffect == kEffectTypeNoSense_Middle || nEffect == kEffectTypeNoSense_Weak))
        {
            ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(2);
            controlFragment.setSpeed(0.0f);
            controlFragment.setPitch(0.0f);
        }
        if(!item.isSelected() && nEffect == kEffectTypeTranscribeBass)
        {
            EqualizerFragment equalizerFragment = (EqualizerFragment)activity.mSectionsPagerAdapter.getItem(3);
            equalizerFragment.setEQ(0);
            ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(2);
            controlFragment.setPitch(0.0f);
        }
        checkDuplicate(nEffect);
        if(hSEStream != 0)
        {
            BASS.BASS_StreamFree(hSEStream);
            hSEStream = 0;
        }
        if(hSEStream2 != 0)
        {
            BASS.BASS_StreamFree(hSEStream2);
            hSEStream2 = 0;
        }
        if(handler != null)
        {
            handler.removeCallbacks(onTimer);
            handler = null;
        }
        applyEffect();
        effectsAdapter.notifyDataSetChanged();
        MainActivity activity = (MainActivity)getActivity();
        PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
        playlistFragment.updateSavingEffect();
    }

    public void resetEffect()
    {
        EffectItem item = arEffectItems.get(0);
        item.setSelected(true);
        for(int i = 1; i < arEffectItems.size(); i++)
        {
            if(arEffectItems.get(i).isSelected() && (i == kEffectTypeRandom || i == kEffectTypeOldRecord || i == kEffectTypeLowBattery || i == kEffectTypeEarTraining))
            {
                EqualizerFragment equalizerFragment = (EqualizerFragment)activity.mSectionsPagerAdapter.getItem(3);
                equalizerFragment.setEQ(0);
            }
            if(arEffectItems.get(i).isSelected() && (i == kEffectTypeRandom || i == kEffectTypeNoSense_Strong || i == kEffectTypeNoSense_Middle || i == kEffectTypeNoSense_Weak))
            {
                ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(2);
                controlFragment.setSpeed(0.0f);
                controlFragment.setPitch(0.0f);
            }
            if(arEffectItems.get(i).isSelected() && (i == kEffectTypeTranscribeBass))
            {
                EqualizerFragment equalizerFragment = (EqualizerFragment)activity.mSectionsPagerAdapter.getItem(3);
                equalizerFragment.setEQ(0);
                ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(2);
                controlFragment.setPitch(0.0f);
            }
            arEffectItems.get(i).setSelected(false);
        }
        fPan = 0.0f;
        fFreq = 1.0f;
        effectsAdapter.notifyDataSetChanged();
        setTimeOfIncreaseSpeed(1.0f);
        setIncreaseSpeed(0.1f);
        setTimeOfDecreaseSpeed(1.0f);
        setDecreaseSpeed(0.1f);
    }

    public void onEffectDetailClick(int nEffect)
    {
        TextView textEffectName = (TextView) activity.findViewById(R.id.textEffectName);
        TextView textEffectLabel = (TextView) activity.findViewById(R.id.textEffectLabel);
        TextView textEffectDetail = (TextView) activity.findViewById(R.id.textEffectDetail);
        SeekBar seek = (SeekBar) activity.findViewById(R.id.seekEffectDetail);
        textEffectName.setText(arEffectItems.get(nEffect).getEffectName());
        seek.setOnSeekBarChangeListener(null);
        if(nEffect == kEffectTypePan)
        {
            textEffectLabel.setText("パン");
            int nPan = (int)(fPan * 100.0f);
            textEffectDetail.setText(String.format("%d", nPan));
            // SeekBarについてはAPIエベル26以降しか最小値を設定できない為、最大値に200を設定（本来は-100～100にしたい）
            seek.setMax(200);
            seek.setProgress(nPan + 100);
        }
        else if(nEffect == kEffectTypeFrequency)
        {
            textEffectLabel.setText("再生周波数");
            textEffectDetail.setText(String.format("%.1f", fFreq));
            // SeekBarについてはAPIエベル26以降しか最小値を設定できない為、最大値に39を設定（本来は1～40にしたい）
            seek.setMax(39);
            seek.setProgress((int)(fFreq * 10.0f) - 1);
        }
        else if(nEffect == kEffectTypeIncreaseSpeed)
        {
            textEffectLabel.setText("指定時間ごとに加速");
            EditText editTimeEffectDetail = getActivity().findViewById(R.id.editTimeEffectDetail);
            editTimeEffectDetail.setText(String.format("%.1f秒", fTimeOfIncreaseSpeed));
            EditText editSpeedEffectDetail = getActivity().findViewById(R.id.editSpeedEffectDetail);
            editSpeedEffectDetail.setText(String.format("%.1f%%", fIncreaseSpeed));
        }
        else if(nEffect == kEffectTypeDecreaseSpeed)
        {
            textEffectLabel.setText("指定時間ごとに減速");
            EditText editTimeEffectDetail = getActivity().findViewById(R.id.editTimeEffectDetail);
            editTimeEffectDetail.setText(String.format("%.1f秒", fTimeOfDecreaseSpeed));
            EditText editSpeedEffectDetail = getActivity().findViewById(R.id.editSpeedEffectDetail);
            editSpeedEffectDetail.setText(String.format("%.1f%%", fDecreaseSpeed));
        }
        else if(nEffect == kEffectTypeMetronome)
        {
            textEffectLabel.setText("BPM");
            textEffectDetail.setText(String.format("%d", nBPM));
            seek.setProgress(0);
            // SeekBarについてはAPIエベル26以降しか最小値を設定できない為、最大値に290を設定（本来は10～300にしたい）
            seek.setMax(290);
            seek.setProgress(nBPM - 10);
        }
        else if(nEffect == kEffectTypeRecordNoise)
        {
            textEffectLabel.setText("音量");
            textEffectDetail.setText(String.format("%d", (int)(fVol1 * 100)));
            seek.setMax(100);
            seek.setProgress((int)(fVol1 * 100));
        }
        else if(nEffect == kEffectTypeRoarOfWaves)
        {
            textEffectLabel.setText("音量");
            textEffectDetail.setText(String.format("%d", (int)(fVol2 * 100)));
            seek.setMax(100);
            seek.setProgress((int)(fVol2 * 100));
        }
        else if(nEffect == kEffectTypeRain)
        {
            textEffectLabel.setText("音量");
            textEffectDetail.setText(String.format("%d", (int)(fVol3 * 100)));
            seek.setMax(100);
            seek.setProgress((int)(fVol3 * 100));
        }
        else if(nEffect == kEffectTypeRiver)
        {
            textEffectLabel.setText("音量");
            textEffectDetail.setText(String.format("%d", (int)(fVol4 * 100)));
            seek.setMax(100);
            seek.setProgress((int)(fVol4 * 100));
        }
        else if(nEffect == kEffectTypeWar)
        {
            textEffectLabel.setText("音量");
            textEffectDetail.setText(String.format("%d", (int)(fVol5 * 100)));
            seek.setMax(100);
            seek.setProgress((int)(fVol5 * 100));
        }
        else if(nEffect == kEffectTypeFire)
        {
            textEffectLabel.setText("音量");
            textEffectDetail.setText(String.format("%d", (int)(fVol6 * 100)));
            seek.setMax(100);
            seek.setProgress((int)(fVol6 * 100));
        }
        else if(nEffect == kEffectTypeConcertHall)
        {
            textEffectLabel.setText("音量");
            textEffectDetail.setText(String.format("%d", (int)(fVol7 * 100)));
            seek.setMax(100);
            seek.setProgress((int)(fVol7 * 100));
        }

        ImageButton buttonEffectMinus = activity.findViewById(R.id.buttonEffectMinus);
        ImageButton buttonEffectPlus = activity.findViewById(R.id.buttonEffectPlus);
        if(nEffect == kEffectTypePan)
        {
            buttonEffectMinus.setImageResource(R.drawable.leftbutton);
            buttonEffectMinus.setContentDescription("値を左に変更");
            buttonEffectPlus.setImageResource(R.drawable.rightbutton);
            buttonEffectPlus.setContentDescription("値を右に変更");
        }
        else
        {
            buttonEffectMinus.setImageResource(R.drawable.minusbutton);
            buttonEffectMinus.setContentDescription("マイナス");
            buttonEffectPlus.setImageResource(R.drawable.plusbutton);
            buttonEffectPlus.setContentDescription("プラス");
        }

        RelativeLayout relativeSliderEffectDatail = (RelativeLayout) activity.findViewById(R.id.relativeSliderEffectDatail);
        RelativeLayout relativeRollerEffectDetail = (RelativeLayout) activity.findViewById(R.id.relativeRollerEffectDatail);
        if(nEffect == kEffectTypeIncreaseSpeed || nEffect == kEffectTypeDecreaseSpeed) {
            relativeSliderEffectDatail.setVisibility(View.GONE);
            relativeRollerEffectDetail.setVisibility(View.VISIBLE);
        }
        else {
            relativeSliderEffectDatail.setVisibility(View.VISIBLE);
            relativeRollerEffectDetail.setVisibility(View.GONE);
            seek.setOnSeekBarChangeListener(this);
        }

        RelativeLayout relativeEffectDetail = (RelativeLayout) activity.findViewById(R.id.relativeEffectDetail);
        relativeEffectDetail.setVisibility(View.VISIBLE);
        RelativeLayout relativeEffect = (RelativeLayout) activity.findViewById(R.id.relativeEffects);
        relativeEffect.setVisibility(View.GONE);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch)
    {
        TextView textEffectName = (TextView) activity.findViewById(R.id.textEffectName);
        TextView textEffectDetail = (TextView) activity.findViewById(R.id.textEffectDetail);
        if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypePan).getEffectName()))
        {
            float fPan = (progress - 100) / 100.0f;
            textEffectDetail.setText(String.format("%d", progress - 100));
            setPan(fPan);
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeFrequency).getEffectName()))
        {
            double dProgress = (double)(progress + 1) / 10.0;
            textEffectDetail.setText(String.format("%.1f", dProgress));
            setFreq((float)dProgress);
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeMetronome).getEffectName()))
        {
            nBPM = progress + 10;
            textEffectDetail.setText(String.format("%d", nBPM));
            applyEffect();
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeRecordNoise).getEffectName()))
        {
            fVol1 = progress / 100.0f;
            textEffectDetail.setText(String.format("%d", progress));
            applyEffect();
            if(arEffectItems.get(kEffectTypeRecordNoise).isSelected())
            {
                int hSETemp = bSE1Playing ? hSEStream : hSEStream2;
                BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, fVol1);
            }
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeRoarOfWaves).getEffectName()))
        {
            fVol2 = progress / 100.0f;
            textEffectDetail.setText(String.format("%d", progress));
            applyEffect();
            if(arEffectItems.get(kEffectTypeRoarOfWaves).isSelected())
            {
                int hSETemp = bSE1Playing ? hSEStream : hSEStream2;
                BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, fVol2);
            }
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeRain).getEffectName()))
        {
            fVol3 = progress / 100.0f;
            textEffectDetail.setText(String.format("%d", progress));
            applyEffect();
            if(arEffectItems.get(kEffectTypeRain).isSelected())
            {
                int hSETemp = bSE1Playing ? hSEStream : hSEStream2;
                BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, fVol3);
            }
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeRiver).getEffectName()))
        {
            fVol4 = progress / 100.0f;
            textEffectDetail.setText(String.format("%d", progress));
            applyEffect();
            if(arEffectItems.get(kEffectTypeRiver).isSelected())
            {
                int hSETemp = bSE1Playing ? hSEStream : hSEStream2;
                BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, fVol4);
            }
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeWar).getEffectName()))
        {
            fVol5 = progress / 100.0f;
            textEffectDetail.setText(String.format("%d", progress));
            applyEffect();
            if(arEffectItems.get(kEffectTypeWar).isSelected())
            {
                int hSETemp = bSE1Playing ? hSEStream : hSEStream2;
                BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, fVol5);
            }
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeFire).getEffectName()))
        {
            fVol6 = progress / 100.0f;
            textEffectDetail.setText(String.format("%d", progress));
            applyEffect();
            if(arEffectItems.get(kEffectTypeFire).isSelected())
            {
                int hSETemp = bSE1Playing ? hSEStream : hSEStream2;
                BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, fVol6);
            }
        }
        else if(textEffectName.getText().toString().equals(arEffectItems.get(kEffectTypeConcertHall).getEffectName()))
        {
            fVol7 = progress / 100.0f;
            textEffectDetail.setText(String.format("%d", progress));
            applyEffect();
            if(arEffectItems.get(kEffectTypeConcertHall).isSelected())
            {
                int hSETemp = bSE1Playing ? hSEStream : hSEStream2;
                BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, fVol7);
            }
        }
        MainActivity activity = (MainActivity)getActivity();
        PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
        playlistFragment.updateSavingEffect();
    }

    public void setPan(float fPan)
    {
        setPan(fPan, true);
    }

    public void setPan(float fPan, boolean bSave)
    {
        if(fPan < -1.0f) fPan = -1.0f;
        else if(fPan > 1.0f) fPan = 1.0f;
        this.fPan = fPan;
        if(arEffectItems.get(kEffectTypePan).isSelected() && MainActivity.hStream != 0)
        {
            if(hDspPan != 0)
            {
                BASS.BASS_ChannelRemoveDSP(MainActivity.hStream, hDspPan);
                hDspPan = 0;
            }
            hDspPan = BASS.BASS_ChannelSetDSP(MainActivity.hStream, panDSP, this, 0);
        }
        if(bSave)
        {
            MainActivity activity = (MainActivity)getActivity();
            PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
            playlistFragment.updateSavingEffect();
        }
    }

    public void setFreq(float fFreq)
    {
        setFreq(fFreq, true);
    }

    public void setFreq(float fFreq, boolean bSave)
    {
        if(fFreq < 0.1f) fFreq = 0.1f;
        else if(fFreq > 4.0f) fFreq = 4.0f;
        this.fFreq = fFreq;
        if(arEffectItems.get(kEffectTypeFrequency).isSelected() && MainActivity.hStream != 0)
        {
            BASS.BASS_CHANNELINFO info = new BASS.BASS_CHANNELINFO();
            BASS.BASS_ChannelGetInfo(MainActivity.hStream, info);
            BASS.BASS_ChannelSetAttribute(MainActivity.hStream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, info.freq * fFreq);
        }
        if(bSave)
        {
            MainActivity activity = (MainActivity)getActivity();
            PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
            playlistFragment.updateSavingEffect();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {

    }

    public void checkDuplicate(int nSelect)
    {
        if(nSelect == 0)
        {
            for(int i = 1; i < arEffectItems.size(); i++)
                deselectEffect(i);
        }
        else
        {
            deselectEffect(0);
            if(kEffectTypeVocalCancel <= nSelect && nSelect <= kEffectTypeTranscribeSideGuitar)
            {
                for (int i = kEffectTypeVocalCancel; i <= kEffectTypeTranscribeSideGuitar; i++)
                    if(i != nSelect) deselectEffect(i);
            }
            if(nSelect == kEffectTypeRandom || nSelect == kEffectTypeTranscribeSideGuitar || nSelect == kEffectTypeTranscribeBass || nSelect == kEffectTypeOldRecord || nSelect == kEffectTypeLowBattery || nSelect == kEffectTypeEarTraining || nSelect == kEffectTypeNoSense_Strong || nSelect == kEffectTypeNoSense_Middle || nSelect == kEffectTypeNoSense_Weak) {
                if(nSelect != kEffectTypeRandom) deselectEffect(kEffectTypeRandom);
                if(nSelect != kEffectTypeTranscribeSideGuitar) deselectEffect(kEffectTypeTranscribeSideGuitar);
                if (nSelect != kEffectTypeTranscribeBass) deselectEffect(kEffectTypeTranscribeBass);
                if (nSelect != kEffectTypeOldRecord) deselectEffect(kEffectTypeOldRecord);
                if (nSelect != kEffectTypeLowBattery) deselectEffect(kEffectTypeLowBattery);
                if (nSelect != kEffectTypeEarTraining) deselectEffect(kEffectTypeEarTraining);
                if (nSelect != kEffectTypeNoSense_Strong) deselectEffect(kEffectTypeNoSense_Strong);
                if (nSelect != kEffectTypeNoSense_Middle) deselectEffect(kEffectTypeNoSense_Middle);
                if (nSelect != kEffectTypeNoSense_Weak) deselectEffect(kEffectTypeNoSense_Weak);
            }
            if(kEffectTypeStadiumEcho <= nSelect && nSelect <= kEffectTypeMountainEcho)
            {
                for(int i = kEffectTypeStadiumEcho; i <= kEffectTypeMountainEcho; i++)
                    if(i != nSelect) deselectEffect(i);
            }
            if(kEffectTypeReverb_Bathroom <= nSelect && nSelect <= kEffectTypeReverb_Cathedral)
            {
                for(int i = kEffectTypeReverb_Bathroom; i <= kEffectTypeReverb_Cathedral; i++)
                    if(i != nSelect) deselectEffect(i);
            }
            if(kEffectTypeChorus <= nSelect && nSelect <= kEffectTypeFlanger)
            {
                for(int i = kEffectTypeChorus; i <= kEffectTypeFlanger; i++)
                    if(i != nSelect) deselectEffect(i);
            }
            if((kEffectTypeDistortion_Strong <= nSelect && nSelect <= kEffectTypeDistortion_Weak) || nSelect == kEffectTypeLowBattery)
            {
                for(int i = kEffectTypeDistortion_Strong; i <= kEffectTypeDistortion_Weak; i++)
                    if(i != nSelect) deselectEffect(i);
                if(nSelect != kEffectTypeLowBattery) deselectEffect(kEffectTypeLowBattery);
            }
            if(kEffectTypeIncreaseSpeed <= nSelect && nSelect <= kEffectTypeMetronome)
            {
                for(int i = kEffectTypeIncreaseSpeed; i <= kEffectTypeMetronome; i++)
                    if(i != nSelect) deselectEffect(i);
            }
            if(nSelect == kEffectTypeOldRecord || (kEffectTypeMetronome <= nSelect && nSelect <= kEffectTypeConcertHall))
            {
                if(nSelect != kEffectTypeOldRecord) deselectEffect(kEffectTypeOldRecord);
                for(int i = kEffectTypeMetronome; i <= kEffectTypeConcertHall; i++)
                    if(i != nSelect) deselectEffect(i);
            }
        }
        MainActivity activity = (MainActivity)getActivity();
        PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
        playlistFragment.updateSavingEffect();
    }

    public void deselectEffect(int nEffect)
    {
        if(!arEffectItems.get(nEffect).isSelected()) return;

        arEffectItems.get(nEffect).setSelected(false);

        ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(2);
        EqualizerFragment equalizerFragment = (EqualizerFragment)activity.mSectionsPagerAdapter.getItem(3);
        if(nEffect == kEffectTypeRandom || nEffect == kEffectTypeTranscribeSideGuitar || nEffect == kEffectTypeOldRecord || nEffect == kEffectTypeLowBattery || nEffect == kEffectTypeEarTraining)
            equalizerFragment.setEQ(0);
        if(nEffect == kEffectTypeRandom || nEffect == kEffectTypeNoSense_Strong || nEffect == kEffectTypeNoSense_Middle || nEffect == kEffectTypeNoSense_Weak)
        {
            controlFragment.setSpeed(0.0f);
            controlFragment.setPitch(0.0f);
        }
        if(nEffect == kEffectTypeTranscribeBass)
        {
            equalizerFragment.setEQ(0);
            controlFragment.setPitch(0.0f);
        }
    }

    public void applyEffect()
    {
        MainActivity activity = (MainActivity)getActivity();
        PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
        int nPlayingPlaylist = playlistFragment.getPlayingPlaylist();
        if(nPlayingPlaylist < 0 || nPlayingPlaylist >= playlistFragment.getArPlaylists().size()) return;
        ArrayList<SongItem> arSongs = playlistFragment.getArPlaylists().get(nPlayingPlaylist);
        int nPlaying = playlistFragment.getPlaying();
        if(nPlaying < 0 || nPlaying >= arSongs.size()) return;
        SongItem song = arSongs.get(nPlaying);
        applyEffect(MainActivity.hStream, song);
    }

    public void applyEffect(int hStream, SongItem song)
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
        if(hDspDoubling != 0)
        {
            BASS.BASS_ChannelRemoveDSP(hStream, hDspDoubling);
            hDspDoubling = 0;
        }
        if(hDspPan != 0)
        {
            BASS.BASS_ChannelRemoveDSP(hStream, hDspPan);
            hDspPan = 0;
        }
        if(hDspNormalize != 0)
        {
            BASS.BASS_ChannelRemoveDSP(hStream, hDspNormalize);
            hDspNormalize = 0;
        }
        if(hDspPhaseReversal != 0)
        {
            BASS.BASS_ChannelRemoveDSP(hStream, hDspPhaseReversal);
            hDspPhaseReversal = 0;
        }
        BASS.BASS_CHANNELINFO info = new BASS.BASS_CHANNELINFO();
        BASS.BASS_ChannelGetInfo(hStream, info);
        BASS.BASS_ChannelSetAttribute(hStream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, info.freq);
        if(hFxEcho != 0)
        {
            BASS.BASS_ChannelRemoveFX(hStream, hFxEcho);
            hFxEcho = 0;
        }
        if(hFxReverb != 0)
        {
            BASS.BASS_ChannelRemoveFX(hStream, hFxReverb);
            hFxReverb = 0;
        }
        if(hFxChorus != 0)
        {
            BASS.BASS_ChannelRemoveFX(hStream, hFxChorus);
            hFxChorus = 0;
        }
        if(hFxDistortion != 0)
        {
            BASS.BASS_ChannelRemoveFX(hStream, hFxDistortion);
            hFxDistortion = 0;
        }
        if(timer != null)
        {
            timer.cancel();
            timer = null;
        }
        for(int i = 0; i < arEffectItems.size(); i++)
        {
            if(!arEffectItems.get(i).isSelected())
                continue;
            String strEffect = arEffectItems.get(i).getEffectName();
            if(strEffect.equals("オフ"))
            {

            }
            else if(strEffect.equals("ランダム"))
            {
                ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(2);
                float fMaxSpeed = 1.5f;
                float fMinSpeed = 0.75f;
                fMaxSpeed = (fMaxSpeed - 1.0f) * 100.0f;
                fMinSpeed = (1.0f - fMinSpeed) * -100.0f;
                Random random = new Random();
                float fRand = random.nextFloat();
                float fSpeed = (fRand * (fMaxSpeed - fMinSpeed) * 10.0f) / 10.0f + fMinSpeed;
                controlFragment.setSpeed(fSpeed);
                float fMaxPitch = 3.0f;
                float fMinPitch = -3.0f;
                fRand = random.nextFloat();
                float fPitch = (fRand * (fMaxPitch - fMinPitch) * 10.0f) / 10.0f + fMinPitch;
                controlFragment.setPitch(fPitch);
                EqualizerFragment equalizerFragment = (EqualizerFragment)activity.mSectionsPagerAdapter.getItem(3);
                equalizerFragment.setEQRandom();
            }
            else if(strEffect.equals("ボーカルキャンセル"))
            {
                if(info.chans != 1)
                    hDspVocalCancel = BASS.BASS_ChannelSetDSP(hStream, vocalCancelDSP, null, 0);
            }
            else if(strEffect.equals("モノラル"))
            {
                if(info.chans != 1)
                    hDspMonoral = BASS.BASS_ChannelSetDSP(hStream, monoralDSP, null, 0);
            }
            else if(strEffect.equals("左のみ再生"))
            {
                if(info.chans != 1)
                    hDspLeft = BASS.BASS_ChannelSetDSP(hStream, leftDSP, null, 0);
            }
            else if(strEffect.equals("右のみ再生"))
            {
                if(info.chans != 1)
                    hDspRight = BASS.BASS_ChannelSetDSP(hStream, rightDSP, null, 0);
            }
            else if(strEffect.equals("左右入れ替え"))
            {
                if(info.chans != 1)
                    hDspExchange = BASS.BASS_ChannelSetDSP(hStream, exchangeDSP, null, 0);
            }
            else if(strEffect.equals("ダブリング"))
            {
                if(info.chans != 1)
                {
                    for(int j = 0; j < ECHBUFLEN; j++)
                    {
                        echbuf[j][0] = 0;
                        echbuf[j][1] = 0;
                    }
                    echpos = 0;
                    hDspDoubling = BASS.BASS_ChannelSetDSP(hStream, doublingDSP, null, 0);
                }
            }
            else if(strEffect.equals("サイドギターの耳コピ"))
            {
                if(info.chans != 1)
                    hDspVocalCancel = BASS.BASS_ChannelSetDSP(hStream, vocalCancelDSP, null, 0);
                EqualizerFragment equalizerFragment = (EqualizerFragment)activity.mSectionsPagerAdapter.getItem(3);
                int[] array = new int[] {0,-30,-20,-12, -7, -4, -3, -2, -1,  0,  0,  0,  0,  0, -1, -2, -3, -4, -7,-12,-20,-24,-27,-28,-29,-30,-30,-30,-30,-30,-30,-30};
                for(int j = 0; j < 32; j++)
                {
                    int nLevel = array[j];
                    if(j == 0)
                        equalizerFragment.setVol(nLevel);
                    else
                        equalizerFragment.setEQ(j, nLevel);
                }
            }
            else if(strEffect.equals("ベースの耳コピ（オクターブ上げ）"))
            {
                ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(2);
                controlFragment.setLink(false);
                controlFragment.setPitch(12.0f);
                EqualizerFragment equalizerFragment = (EqualizerFragment)activity.mSectionsPagerAdapter.getItem(3);
                int[] array = new int[] {0,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-20,-10,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
                for(int j = 0; j < 32; j++)
                {
                    int nLevel = array[j];
                    if(j == 0)
                        equalizerFragment.setVol(nLevel);
                    else
                        equalizerFragment.setEQ(j, nLevel);
                }
            }
            else if(strEffect.equals("パン"))
            {
                if(info.chans != 1)
                    hDspPan = BASS.BASS_ChannelSetDSP(hStream, panDSP, this, 0);
            }
            else if(strEffect.equals("ノーマライズ"))
            {
                if(song.getPeak() == 0.0f) {
                    if(hStream != MainActivity.hStream) getPeak(song);
                    else fPeak = 1.0f;
                }
                else fPeak = song.getPeak();
                hDspNormalize = BASS.BASS_ChannelSetDSP(hStream, normalizeDSP, this, 0);
            }
            else if(strEffect.equals("コンプレッサー"))
            {
                hFxCompressor = BASS.BASS_ChannelSetFX(hStream, BASS_FX.BASS_FX_BFX_COMPRESSOR2, 2);
                BASS_FX.BASS_BFX_COMPRESSOR2 p = new BASS_FX.BASS_BFX_COMPRESSOR2();
                p.fGain = 2.0f;
                p.fThreshold = -20.0f;
                p.fRatio = 10.0f;
                p.fAttack = 1.2f;
                p.fRelease = 400.0f;
                p.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(hFxCompressor, p);
            }
            else if(strEffect.equals("再生周波数"))
                BASS.BASS_ChannelSetAttribute(hStream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, info.freq * fFreq);
            else if(strEffect.equals("位相反転"))
                hDspPhaseReversal = BASS.BASS_ChannelSetDSP(hStream, phaseReversalDSP, null, 0);
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
            else if(strEffect.equals("逆回転再生"))
            {
                if(hStream != 0)
                {
                    int chan = BASS_FX.BASS_FX_TempoGetSource(hStream);
                    BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_REVERSE);
                    activity.setSync();
                }
            }
            else if(strEffect.equals("だんだん速く"))
            {
                if(handler != null) {
                    handler.removeCallbacks(onTimer);
                    handler = null;
                }
                handler = new Handler();
                handler.post(onTimer);
            }
            else if(strEffect.equals("だんだん遅く"))
            {
                if(handler != null) {
                    handler.removeCallbacks(onTimer);
                    handler = null;
                }
                handler = new Handler();
                handler.post(onTimer);
            }
            else if(strEffect.equals("古びたレコード再生"))
            {
                EqualizerFragment equalizerFragment = (EqualizerFragment)activity.mSectionsPagerAdapter.getItem(3);
                int[] array = new int[] {2,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12};
                for(int j = 0; j < 32; j++)
                {
                    int nLevel = array[j];
                    if(j == 0)
                        equalizerFragment.setVol(nLevel);
                    else
                        equalizerFragment.setEQ(j, nLevel);
                }
                if(hSEStream == 0)
                {
                    bSE1Playing = true;
                    InputStream is = getResources().openRawResource(R.raw.recordnoise);
                    hSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
                    hSync = BASS.BASS_ChannelSetSync(hSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 4.653), endRecordNoise, this);
                    BASS.BASS_ChannelSetAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, fVol1);
                    BASS.BASS_ChannelPlay(hSEStream, true);
                }

                handler = new Handler();
                handler.post(onTimer);
            }
            else if(strEffect.equals("電池切れ"))
            {
                hFxDistortion = BASS.BASS_ChannelSetFX(hStream, BASS_FX.BASS_FX_BFX_DISTORTION, 2);
                BASS_FX.BASS_BFX_DISTORTION distortion = new BASS_FX.BASS_BFX_DISTORTION();
                distortion.fDrive = (float)0.2;
                distortion.fDryMix = (float)0.9;
                distortion.fWetMix = (float)0.1;
                distortion.fFeedback = (float)0.1;
                distortion.fVolume = (float)1.0;
                distortion.lChannel = BASS_FX.BASS_BFX_CHANALL;
                BASS.BASS_FXSetParameters(hFxDistortion, distortion);

                EqualizerFragment equalizerFragment = (EqualizerFragment)activity.mSectionsPagerAdapter.getItem(3);
                int[] array = new int[] {2,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12};
                for(int j = 0; j < 32; j++)
                {
                    int nLevel = array[j];
                    if(j == 0)
                        equalizerFragment.setVol(nLevel);
                    else
                        equalizerFragment.setEQ(j, nLevel);
                }

                handler = new Handler();
                handler.post(onTimer);
            }
            else if(strEffect.equals("歌へた（強）") || strEffect.equals("歌へた（中）") || strEffect.equals("歌へた（弱）"))
            {
                fVelo1 = fVol2 = 0.0f;
                ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(2);
                controlFragment.setSpeed(0.0f);
                controlFragment.setPitch(0.0f);

                handler = new Handler();
                handler.post(onTimer);
            }
            else if(strEffect.equals("聴覚トレーニング"))
            {
                handler = new Handler();
                handler.post(onTimer);
            }
            else if(strEffect.equals("メトロノーム"))
            {
                timer = new Timer();
                MetronomeTask metronomeTask = new MetronomeTask(this);
                long lPeriod = (long)((60.0 / nBPM) * 1000);
                timer.schedule(metronomeTask, lPeriod, lPeriod);
            }
            else if(strEffect.equals("レコードノイズ"))
            {
                if(hSEStream == 0)
                {
                    bSE1Playing = true;
                    InputStream is = getResources().openRawResource(R.raw.recordnoise);
                    hSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
                    hSync = BASS.BASS_ChannelSetSync(hSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 4.653), endRecordNoise, this);
                    BASS.BASS_ChannelSetAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, fVol1);
                    BASS.BASS_ChannelPlay(hSEStream, true);
                }
            }
            else if(strEffect.equals("波の音"))
            {
                if(hSEStream == 0)
                {
                    bSE1Playing = true;
                    InputStream is = getResources().openRawResource(R.raw.wave);
                    hSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
                    hSync = BASS.BASS_ChannelSetSync(hSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 38.399), endWave, this);
                    BASS.BASS_ChannelSetAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, fVol2);
                    BASS.BASS_ChannelPlay(hSEStream, true);
                }
            }
            else if(strEffect.equals("雨の音"))
            {
                if(hSEStream == 0)
                {
                    bSE1Playing = true;
                    InputStream is = getResources().openRawResource(R.raw.rain);
                    hSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
                    hSync = BASS.BASS_ChannelSetSync(hSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 1.503), endRain, this);
                    BASS.BASS_ChannelSetAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, fVol3);
                    BASS.BASS_ChannelPlay(hSEStream, true);
                }
            }
            else if(strEffect.equals("川の音"))
            {
                if(hSEStream == 0)
                {
                    bSE1Playing = true;
                    InputStream is = getResources().openRawResource(R.raw.river);
                    hSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
                    hSync = BASS.BASS_ChannelSetSync(hSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 60.000), endRiver, this);
                    BASS.BASS_ChannelSetAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, fVol4);
                    BASS.BASS_ChannelPlay(hSEStream, true);
                }
            }
            else if(strEffect.equals("戦の音"))
            {
                if(hSEStream == 0)
                {
                    bSE1Playing = true;
                    InputStream is = getResources().openRawResource(R.raw.war);
                    hSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
                    hSync = BASS.BASS_ChannelSetSync(hSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 30.000), endWar, this);
                    BASS.BASS_ChannelSetAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, fVol5);
                    BASS.BASS_ChannelPlay(hSEStream, true);
                }
            }
            else if(strEffect.equals("焚き火"))
            {
                if(hSEStream == 0)
                {
                    bSE1Playing = true;
                    InputStream is = getResources().openRawResource(R.raw.fire);
                    hSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
                    hSync = BASS.BASS_ChannelSetSync(hSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 90.000), endFire, this);
                    BASS.BASS_ChannelSetAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, fVol6);
                    BASS.BASS_ChannelPlay(hSEStream, true);
                }
            }
            else if(strEffect.equals("コンサート会場"))
            {
                if(hSEStream == 0)
                {
                    bSE1Playing = true;
                    InputStream is = getResources().openRawResource(R.raw.cheer);
                    hSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
                    hSync = BASS.BASS_ChannelSetSync(hSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 14.000), endCheer, this);
                    BASS.BASS_ChannelSetAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, fVol7);

                    handler = new Handler();
                    handler.post(onTimer);
                }
            }
        }
    }

    public void getPeak(SongItem song)
    {
        if(song == null) return;
        File file = new File(song.getPath());
        int hTempStream = 0;
        if(file.getParent().equals(getContext().getFilesDir().toString()))
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
                    }
                    return false;
                }

                @Override
                public int FILEREADPROC(ByteBuffer buffer, int length, Object user) {
                    FileChannel fc=(FileChannel)user;
                    try {
                        return fc.read(buffer);
                    } catch (IOException e) {
                    }
                    return 0;
                }

                @Override
                public long FILELENPROC(Object user) {
                    FileChannel fc=(FileChannel)user;
                    try {
                        return fc.size();
                    } catch (IOException e) {
                    }
                    return 0;
                }

                @Override
                public void FILECLOSEPROC(Object user) {
                    FileChannel fc=(FileChannel)user;
                    try {
                        fc.close();
                    } catch (IOException e) {
                    }
                }
            };

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            boolean bError = false;
            try {
                mmr.setDataSource(getContext(), Uri.parse(song.getPath()));
            }
            catch(Exception e) {
                bError = true;
            }
            String strMimeType = null;
            if(!bError)
                strMimeType = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            ContentResolver cr = getContext().getContentResolver();
            try {
                AssetFileDescriptor afd = cr.openAssetFileDescriptor(Uri.parse(song.getPath()), "r");
                FileChannel fc = afd.createInputStream().getChannel();
                if(strMimeType == "audio/mp4")
                    hTempStream = BASS_AAC.BASS_AAC_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE, fileprocs, fc);
                else if(strMimeType == "audio/flac")
                    hTempStream = BASSFLAC.BASS_FLAC_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE, fileprocs, fc);
                else
                    hTempStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE, fileprocs, fc);
            } catch (IOException e) {
            }
        }

        BASS.BASS_CHANNELINFO info = new BASS.BASS_CHANNELINFO();
        BASS.BASS_ChannelGetInfo(hTempStream, info);
        Boolean bStereo = true;
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
        fPeak = fTempPeak;
    }

    Runnable onTimer = new Runnable()
    {
        @Override
        public void run()
        {
            if(arEffectItems.get(kEffectTypeIncreaseSpeed).isSelected())
            {
                Float fSpeed = 0.0f;
                BASS.BASS_ChannelGetAttribute(MainActivity.hStream, BASS_FX.BASS_ATTRIB_TEMPO, fSpeed);
                fSpeed += fIncreaseSpeed;
                ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(2);
                if(fSpeed + 100.0f > 400.0f) fSpeed = 300.0f;
                if(MainActivity.hStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.hStream) != BASS.BASS_ACTIVE_PAUSED)
                    controlFragment.setSpeed(fSpeed, false);
                handler.postDelayed(this, (long)(fTimeOfIncreaseSpeed * 1000.0f));
                return;
            }
            else if(arEffectItems.get(kEffectTypeDecreaseSpeed).isSelected())
            {
                Float fSpeed = 0.0f;
                BASS.BASS_ChannelGetAttribute(MainActivity.hStream, BASS_FX.BASS_ATTRIB_TEMPO, fSpeed);
                fSpeed -= fDecreaseSpeed;
                ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(2);
                if(fSpeed + 100.0f < 10.0f) fSpeed = -90.0f;
                if(MainActivity.hStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.hStream) != BASS.BASS_ACTIVE_PAUSED)
                    controlFragment.setSpeed(fSpeed, false);
                handler.postDelayed(this, (long)(fTimeOfIncreaseSpeed * 1000.0f));
                return;
            }
            else if(arEffectItems.get(kEffectTypeOldRecord).isSelected())
            {
                Float fFreq = new Float(0.0f);
                BASS.BASS_ChannelGetAttribute(MainActivity.hStream, BASS.BASS_ATTRIB_FREQ, fFreq);
                Float fTempoFreq = new Float(0.0f);
                BASS.BASS_ChannelGetAttribute(MainActivity.hStream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, fTempoFreq);
                fTempoFreq = fTempoFreq * 100.0f / fFreq;
                // 加速度の設定
                // 周波数が98以上の場合 : -0.1
                // 　　　　98未満の場合 : +0.1
                float fAccel = fTempoFreq.floatValue() >= 98.0f ? -0.1f : 0.1f;

                // 周波数の差分に加速度を加える
                fVelo1 += fAccel;

                // 周波数に差分を加える
                fTempoFreq += fVelo1;

                if(fTempoFreq <= 90.0) fTempoFreq = 90.0f;
                if(fTempoFreq >= 100.0) fTempoFreq = 100.0f;

                BASS.BASS_ChannelSetAttribute(MainActivity.hStream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, fFreq * fTempoFreq / 100.0f);
                handler.postDelayed(this, 750);
                return;
            }
            else if(arEffectItems.get(kEffectTypeLowBattery).isSelected())
            {
                Float fFreq = new Float(0.0f);
                BASS.BASS_ChannelGetAttribute(MainActivity.hStream, BASS.BASS_ATTRIB_FREQ, fFreq);
                Float fTempoFreq = new Float(0.0f);
                BASS.BASS_ChannelGetAttribute(MainActivity.hStream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, fTempoFreq);
                fTempoFreq = fTempoFreq * 100.0f / fFreq;
                // 加速度の設定
                // 周波数が68以上の場合 : -0.02
                // 　　　　68未満の場合 : +0.01
                float fAccel = fTempoFreq.floatValue() >= 68.0f ? -0.02f : 0.01f;

                // 周波数の差分に加速度を加える
                fVelo1 += fAccel;

                // 周波数に差分を加える
                fTempoFreq += fVelo1;

                if(fTempoFreq <= 65.0) fTempoFreq = 65.0f;
                if(fTempoFreq >= 70.0) fTempoFreq = 70.0f;

                BASS.BASS_ChannelSetAttribute(MainActivity.hStream, BASS_FX.BASS_ATTRIB_TEMPO_FREQ, fFreq * fTempoFreq / 100.0f);

                handler.postDelayed(this, 50);
                return;
            }
            else if(arEffectItems.get(kEffectTypeNoSense_Strong).isSelected() || arEffectItems.get(kEffectTypeNoSense_Middle).isSelected() || arEffectItems.get(kEffectTypeNoSense_Weak).isSelected()) {
                Float fSpeed = 0.0f;
                BASS.BASS_ChannelGetAttribute(MainActivity.hStream, BASS_FX.BASS_ATTRIB_TEMPO, fSpeed);
                float fAccel;
                Random random = new Random();
                float fRand = random.nextFloat();
                if(arEffectItems.get(kEffectTypeNoSense_Strong).isSelected())
                {
                    fAccel = (fRand * 400.0f) / 10000.0f - 0.02f; // 加速度の設定
                    if(fSpeed < -20.0f) fAccel = 0.01f;
                    else if(fSpeed > 20.0f) fAccel = -0.01f;
                }
                else if(arEffectItems.get(kEffectTypeNoSense_Middle).isSelected())
                {
                    fAccel = (fRand * 200.0f) / 10000.0f - 0.01f; // 加速度の設定
                    if(fSpeed < -10.0f) fAccel = 0.01f;
                    else if(fSpeed > 10.0f) fAccel = -0.01f;
                }
                else
                {
                    fAccel = (fRand * 100.0f) / 10000.0f - 0.005f; // 加速度の設定
                    if(fSpeed < -5.0f) fAccel = 0.01f;
                    else if(fSpeed > 5.0f) fAccel = -0.01f;
                }
                fVelo1 += fAccel; // 速度の差分に加速度を加える
                fSpeed += fVelo1; // 速度に差分を加える
                ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(2);
                if(MainActivity.hStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.hStream) != BASS.BASS_ACTIVE_PAUSED)
                    controlFragment.setSpeed(fSpeed);

                Float fPitch = 0.0f;
                fRand = random.nextFloat();
                BASS.BASS_ChannelGetAttribute(MainActivity.hStream, BASS_FX.BASS_ATTRIB_TEMPO_PITCH, fPitch);
                if(arEffectItems.get(kEffectTypeNoSense_Strong).isSelected())
                {
                    fAccel = (fRand * 400.0f) / 10000.0f - 0.02f; // 加速度の設定
                    if(fPitch < -4.0f) fAccel = 0.01f;
                    else if(fPitch > 4.0f) fAccel = -0.01f;
                }
                if(arEffectItems.get(kEffectTypeNoSense_Middle).isSelected())
                {
                    fAccel = (fRand * 200.0f) / 10000.0f - 0.01f; // 加速度の設定
                    if(fPitch < -2.0f) fAccel = 0.01f;
                    else if(fPitch > 2.0f) fAccel = -0.01f;
                }
                else
                {
                    fAccel = (fRand * 100.0f) / 10000.0f - 0.005f; // 加速度の設定
                    if(fPitch < -1.0f) fAccel = 0.01f;
                    else if(fPitch > 1.0f) fAccel = -0.01f;
                }
                fVelo2 += fAccel; // 音程の差分に加速度を加える
                fPitch += fVelo2; // 音程に差分を加える
                if(MainActivity.hStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.hStream) != BASS.BASS_ACTIVE_PAUSED)
                    controlFragment.setPitch(fPitch);
                handler.postDelayed(this, 80);
                return;
            }
            else if(arEffectItems.get(kEffectTypeEarTraining).isSelected())
            {
                EqualizerFragment equalizerFragment = (EqualizerFragment)activity.mSectionsPagerAdapter.getItem(3);
                equalizerFragment.setEQRandom();
                handler.postDelayed(this, 3000);
                return;
            }
            else if(arEffectItems.get(kEffectTypeConcertHall).isSelected())
            {
                int hSETemp = bSE1Playing ? hSEStream : hSEStream2;
                if(BASS.BASS_ChannelIsActive(MainActivity.hStream) == BASS.BASS_ACTIVE_PLAYING)
                {
                    if(BASS.BASS_ChannelIsActive(hSETemp) == BASS.BASS_ACTIVE_PAUSED || BASS.BASS_ChannelIsActive(hSETemp) == BASS.BASS_ACTIVE_STOPPED)
                    {
                        BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, fVol7);
                        BASS.BASS_ChannelPlay(hSETemp, true);
                        handler.postDelayed(this, 100);
                        return;
                    }
                }
                if(MainActivity.hStream == 0 || BASS.BASS_ChannelIsActive(MainActivity.hStream) == BASS.BASS_ACTIVE_PAUSED || BASS.BASS_ChannelIsActive(MainActivity.hStream) == BASS.BASS_ACTIVE_STOPPED)
                {
                    if(BASS.BASS_ChannelIsActive(hSETemp) == BASS.BASS_ACTIVE_PLAYING)
                        BASS.BASS_ChannelPause(hSETemp);
                    handler.postDelayed(this, 100);
                    return;
                }
                if(BASS.BASS_ChannelIsActive(hSETemp) == BASS.BASS_ACTIVE_PAUSED)
                {
                    handler.postDelayed(this, 100);
                    return;
                }
                if(BASS.BASS_ChannelIsSliding(hSETemp, BASS.BASS_ATTRIB_VOL))
                {
                    handler.postDelayed(this, 100);
                    return;
                }
                double dPos = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetPosition(MainActivity.hStream, BASS.BASS_POS_BYTE));
                double dLength = BASS.BASS_ChannelBytes2Seconds(MainActivity.hStream, BASS.BASS_ChannelGetLength(MainActivity.hStream, BASS.BASS_POS_BYTE));
                if(dLength - dPos < 5.0)
                {
                    BASS.BASS_ChannelSlideAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, 0.0f, 5000);
                    handler.postDelayed(this, 100);
                    return;
                }

                Float fVol = new Float(0.0f);
                BASS.BASS_ChannelGetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, fVol);
                Random random = new Random();
                float fRand = random.nextFloat();
                fRand = (fRand / 200.0f) - 0.0025f;
                if(fVol > 1.0f - 0.01f) fRand = -0.0005f;
                else if(fVol <= 0.5f) fRand = 0.0005f;
                fAccel += fRand;
                fVol += fAccel;
                if(fVol > 1.0f) fVol = 1.0f;
                else if(fVol < 0.5f) fVol = 0.5f;
                BASS.BASS_ChannelSetAttribute(hSETemp, BASS.BASS_ATTRIB_VOL, fVol.floatValue() * fVol7);
                handler.postDelayed(this, 100);
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
            int r=0;
            try
            {
                r=is.read(b);
            }
            catch (Exception e)
            {
                return 0;
            }
            if (r<=0) return 0;
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

    public void onRecordNoiseEnded()
    {
        if(bSE1Playing)
        {
            InputStream is = getResources().openRawResource(R.raw.recordnoise);
            hSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(hSEStream2, BASS.BASS_ChannelSeconds2Bytes(hSEStream2, 1.417), BASS.BASS_POS_BYTE);
            if(hSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(hSEStream, hSync);
                hSync = 0;
            }
            hSync = BASS.BASS_ChannelSetSync(hSEStream2, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream2, 4.653), endRecordNoise, this);
            BASS.BASS_ChannelPlay(hSEStream2, FALSE);
            BASS.BASS_ChannelSlideAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            BASS.BASS_ChannelSlideAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, fVol1, 1000);
            bSE1Playing = false;
        }
        else if(BASS.BASS_ChannelIsActive(hSEStream2) == BASS.BASS_ACTIVE_PLAYING)
        {
            InputStream is = getResources().openRawResource(R.raw.recordnoise);
            hSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(hSEStream, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 1.417), BASS.BASS_POS_BYTE);
            if(hSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(hSEStream2, hSync);
                hSync = 0;
            }
            hSync = BASS.BASS_ChannelSetSync(hSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 4.653), endRecordNoise, this);
            BASS.BASS_ChannelPlay(hSEStream, FALSE);
            BASS.BASS_ChannelSlideAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            BASS.BASS_ChannelSlideAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, fVol1, 1000);
            bSE1Playing = true;
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

    public void onWaveEnded()
    {
        if(bSE1Playing)
        {
            InputStream is = getResources().openRawResource(R.raw.wave);
            hSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(hSEStream2, BASS.BASS_ChannelSeconds2Bytes(hSEStream2, 0.283), BASS.BASS_POS_BYTE);
            if(hSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(hSEStream, hSync);
                hSync = 0;
            }
            hSync = BASS.BASS_ChannelSetSync(hSEStream2, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream2, 38.399), endWave, this);
            BASS.BASS_ChannelPlay(hSEStream2, FALSE);
            BASS.BASS_ChannelSlideAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            BASS.BASS_ChannelSlideAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, fVol2, 1000);
            bSE1Playing = false;
        }
        else if(BASS.BASS_ChannelIsActive(hSEStream2) == BASS.BASS_ACTIVE_PLAYING)
        {
            InputStream is = getResources().openRawResource(R.raw.wave);
            hSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(hSEStream, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 0.283), BASS.BASS_POS_BYTE);
            if(hSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(hSEStream2, hSync);
                hSync = 0;
            }
            hSync = BASS.BASS_ChannelSetSync(hSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 38.399), endWave, this);
            BASS.BASS_ChannelPlay(hSEStream, FALSE);
            BASS.BASS_ChannelSlideAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            BASS.BASS_ChannelSlideAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, fVol2, 1000);
            bSE1Playing = true;
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

    public void onRainEnded()
    {
        if(bSE1Playing)
        {
            InputStream is = getResources().openRawResource(R.raw.rain);
            hSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(hSEStream2, BASS.BASS_ChannelSeconds2Bytes(hSEStream2, 0.303), BASS.BASS_POS_BYTE);
            if(hSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(hSEStream, hSync);
                hSync = 0;
            }
            hSync = BASS.BASS_ChannelSetSync(hSEStream2, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream2, 1.503), endRain, this);
            BASS.BASS_ChannelPlay(hSEStream2, FALSE);
            BASS.BASS_ChannelSlideAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, fVol3, 150);
            BASS.BASS_ChannelSlideAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, 0.0f, 300);
            bSE1Playing = false;
        }
        else if(BASS.BASS_ChannelIsActive(hSEStream2) == BASS.BASS_ACTIVE_PLAYING)
        {
            InputStream is = getResources().openRawResource(R.raw.rain);
            hSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(hSEStream, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 0.303), BASS.BASS_POS_BYTE);
            if(hSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(hSEStream2, hSync);
                hSync = 0;
            }
            hSync = BASS.BASS_ChannelSetSync(hSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 1.503), endRain, this);
            BASS.BASS_ChannelPlay(hSEStream, FALSE);
            BASS.BASS_ChannelSlideAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, fVol3, 150);
            BASS.BASS_ChannelSlideAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f, 300);
            bSE1Playing = true;
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

    public void onRiverEnded()
    {
        if(bSE1Playing)
        {
            InputStream is = getResources().openRawResource(R.raw.river);
            hSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(hSEStream2, BASS.BASS_ChannelSeconds2Bytes(hSEStream2, 0.0), BASS.BASS_POS_BYTE);
            if(hSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(hSEStream, hSync);
                hSync = 0;
            }
            hSync = BASS.BASS_ChannelSetSync(hSEStream2, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream2, 60.0), endRiver, this);
            BASS.BASS_ChannelPlay(hSEStream2, FALSE);
            BASS.BASS_ChannelSlideAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, fVol4, 5000);
            BASS.BASS_ChannelSlideAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, 0.0f, 5000);
            bSE1Playing = false;
        }
        else if(BASS.BASS_ChannelIsActive(hSEStream2) == BASS.BASS_ACTIVE_PLAYING)
        {
            InputStream is = getResources().openRawResource(R.raw.river);
            hSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(hSEStream, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 0.0), BASS.BASS_POS_BYTE);
            if(hSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(hSEStream2, hSync);
                hSync = 0;
            }
            hSync = BASS.BASS_ChannelSetSync(hSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 60.0), endRiver, this);
            BASS.BASS_ChannelPlay(hSEStream, FALSE);
            BASS.BASS_ChannelSlideAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, fVol4, 5000);
            BASS.BASS_ChannelSlideAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f, 5000);
            bSE1Playing = true;
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

    public void onWarEnded()
    {
        if(bSE1Playing)
        {
            InputStream is = getResources().openRawResource(R.raw.war);
            hSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(hSEStream2, BASS.BASS_ChannelSeconds2Bytes(hSEStream2, 0.0), BASS.BASS_POS_BYTE);
            if(hSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(hSEStream, hSync);
                hSync = 0;
            }
            hSync = BASS.BASS_ChannelSetSync(hSEStream2, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream2, 30.0), endWar, this);
            BASS.BASS_ChannelPlay(hSEStream2, FALSE);
            BASS.BASS_ChannelSlideAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, fVol5, 1000);
            BASS.BASS_ChannelSlideAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            bSE1Playing = false;
        }
        else if(BASS.BASS_ChannelIsActive(hSEStream2) == BASS.BASS_ACTIVE_PLAYING)
        {
            InputStream is = getResources().openRawResource(R.raw.war);
            hSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(hSEStream, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 0.0), BASS.BASS_POS_BYTE);
            if(hSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(hSEStream2, hSync);
                hSync = 0;
            }
            hSync = BASS.BASS_ChannelSetSync(hSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 30.0), endWar, this);
            BASS.BASS_ChannelPlay(hSEStream, FALSE);
            BASS.BASS_ChannelSlideAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, fVol5, 1000);
            BASS.BASS_ChannelSlideAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            bSE1Playing = true;
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

    public void onFireEnded()
    {
        if(bSE1Playing)
        {
            InputStream is = getResources().openRawResource(R.raw.fire);
            hSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(hSEStream2, BASS.BASS_ChannelSeconds2Bytes(hSEStream2, 0.0), BASS.BASS_POS_BYTE);
            if(hSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(hSEStream, hSync);
                hSync = 0;
            }
            hSync = BASS.BASS_ChannelSetSync(hSEStream2, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream2, 90.0), endFire, this);
            BASS.BASS_ChannelPlay(hSEStream2, FALSE);
            BASS.BASS_ChannelSlideAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, fVol6, 5000);
            BASS.BASS_ChannelSlideAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, 0.0f, 5000);
            bSE1Playing = false;
        }
        else if(BASS.BASS_ChannelIsActive(hSEStream2) == BASS.BASS_ACTIVE_PLAYING)
        {
            InputStream is = getResources().openRawResource(R.raw.fire);
            hSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(hSEStream, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 0.0), BASS.BASS_POS_BYTE);
            if(hSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(hSEStream2, hSync);
                hSync = 0;
            }
            hSync = BASS.BASS_ChannelSetSync(hSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 90.0), endFire, this);
            BASS.BASS_ChannelPlay(hSEStream, FALSE);
            BASS.BASS_ChannelSlideAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, fVol6, 5000);
            BASS.BASS_ChannelSlideAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f, 5000);
            bSE1Playing = true;
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

    public void onCheerEnded()
    {
        if(bSE1Playing)
        {
            Float fVol = new Float(0.0f);
            BASS.BASS_ChannelGetAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, fVol);
            InputStream is = getResources().openRawResource(R.raw.cheer);
            hSEStream2 = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(hSEStream2, BASS.BASS_ChannelSeconds2Bytes(hSEStream2, 1.0), BASS.BASS_POS_BYTE);
            if(hSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(hSEStream, hSync);
                hSync = 0;
            }
            hSync = BASS.BASS_ChannelSetSync(hSEStream2, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream2, 14.0), endCheer, this);
            BASS.BASS_ChannelPlay(hSEStream2, FALSE);
            BASS.BASS_ChannelSlideAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, fVol.floatValue(), 1000);
            BASS.BASS_ChannelSlideAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            bSE1Playing = false;
        }
        else if(BASS.BASS_ChannelIsActive(hSEStream2) == BASS.BASS_ACTIVE_PLAYING)
        {
            Float fVol = new Float(0.0f);
            BASS.BASS_ChannelGetAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, fVol);
            InputStream is = getResources().openRawResource(R.raw.cheer);
            hSEStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_BUFFER, 0, fileprocs, is);
            BASS.BASS_ChannelSetAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, 0.0f);
            BASS.BASS_ChannelSetPosition(hSEStream, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 1.0), BASS.BASS_POS_BYTE);
            if(hSync != 0)
            {
                BASS.BASS_ChannelRemoveSync(hSEStream2, hSync);
                hSync = 0;
            }
            hSync = BASS.BASS_ChannelSetSync(hSEStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hSEStream, 14.0), endCheer, this);
            BASS.BASS_ChannelPlay(hSEStream, FALSE);
            BASS.BASS_ChannelSlideAttribute(hSEStream, BASS.BASS_ATTRIB_VOL, fVol.floatValue(), 1000);
            BASS.BASS_ChannelSlideAttribute(hSEStream2, BASS.BASS_ATTRIB_VOL, 0.0f, 1000);
            bSE1Playing = true;
        }
    }

    public void playMetronome()
    {
        final MediaPlayer mp = MediaPlayer.create(activity, R.raw.click);
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

    int ECHBUFLEN = 1200;
    float[][] echbuf = new float[ECHBUFLEN][2];
    int echpos;
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
                if(effectFragment.fPan > 0.0f)
                    b[a] = b[a] * (1.0f - effectFragment.fPan);
                else
                    b[a + 1] = b[a + 1] * (1.0f + effectFragment.fPan);
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
            if(effectFragment.fPeak != 0.0f)
            {
                for(int a = 0; a < length / 4; a++)
                    b[a] /= effectFragment.fPeak;
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

    public void showTimeEffectDialog()
    {
        TimeEffectDetailFragmentDialog dialog = new TimeEffectDetailFragmentDialog();
        dialog.show(getFragmentManager(), "span_setting_dialog");
    }

    public void showSpeedEffectDialog()
    {
        SpeedEffectDetailFragmentDialog dialog = new SpeedEffectDetailFragmentDialog();
        dialog.show(getFragmentManager(), "span_setting_dialog");
    }

    public void clearFocus()
    {
        EditText editTimeEffectDetail = getActivity().findViewById(R.id.editTimeEffectDetail);
        editTimeEffectDetail.clearFocus();
        EditText editSpeedEffectDetail = getActivity().findViewById(R.id.editSpeedEffectDetail);
        editSpeedEffectDetail.clearFocus();
    }
}
