/*
 * EqualizerFragment
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
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class EqualizerFragment extends Fragment implements View.OnClickListener {
    static MainActivity sActivity;
    static ArrayList<EqualizerItem> sEqualizerItems;
    static int sVol;
    static int[] sEQs = new int[31];
    private static ArrayList<Integer> sHfxs;
    private static float[] sCenters = new float[] {20000, 16000, 12500, 10000, 8000, 6300, 5000, 4000, 3150, 2500, 2000, 1600, 1250, 1000, 800, 630, 500, 400, 315, 250, 200, 160, 125, 100, 80, 63, 50, 40, 31.5f, 25, 20};
    private RecyclerView mRecyclerEqualizers;
    private EqualizersAdapter mEqualizersAdapter;
    private ItemTouchHelper mEqualizerTouchHelper;
    private ArrayList<TextView> mTextNames;
    private ArrayList<TextView> mTextValues;
    private ArrayList<SeekBar> mSeeks;
    private ArrayList<HighlightImageButton> mButtonMinus, mButtonPlus;
    private boolean mSorting, mAddTemplate, mContinue = true;
    private final Handler mHandler;
    private int mLongClick = 0;

    private RelativeLayout mRelativeTemplateHeader;
    private ScrollView mScrollView;
    private TextView mTextTemplateName, mTextFinishSortEqualizer;
    private View mViewSepEqualizerHeader, mViewSepEqualizerCustomize;
    private Button mBtnEqualizerOff, mBtnBackCustomize, mBtnFinishCustomize, mBtnEqualizerRandom, mBtnResetEqualizer, mBtnEqualizerSaveAs;
    private AnimationButton mBtnEqualizerMenu, mBtnAddEqualizerTemplate;
    private ImageView mImgBackEqualizer;
    private ArrayList<TextView> getTextValues() { return mTextValues; }

    ArrayList<SeekBar> getSeeks() { return mSeeks; }
    float[] getArCenters() { return sCenters; }
    private void setArEqualizerItems(ArrayList<EqualizerItem> arLists) {
        sEqualizerItems = arLists;
        mEqualizersAdapter.changeItems(sEqualizerItems);
    }
    ItemTouchHelper getEqualizerTouchHelper() { return mEqualizerTouchHelper; }
    static boolean isSelectedItem(int nItem) {
        if(nItem >= sEqualizerItems.size()) return false;
        EqualizerItem item = sEqualizerItems.get(nItem);
        return item.isSelected();
    }
    public boolean isSorting() { return mSorting; }
    EqualizersAdapter getEqualizersAdapter() { return mEqualizersAdapter; }
    private Button getBtnEqualizerOff() { return mBtnEqualizerOff; }

    public EqualizerFragment() {
        if(sHfxs == null) sHfxs = new ArrayList<>();
        if(sEqualizerItems == null) sEqualizerItems = new ArrayList<>();
        mHandler = new Handler();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_equalizer, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof MainActivity) {
            sActivity = (MainActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        sActivity = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEqualizersAdapter = new EqualizersAdapter(sActivity, sEqualizerItems);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerEqualizers = sActivity.findViewById(R.id.recyclerEqualizers);
        mTextFinishSortEqualizer = sActivity.findViewById(R.id.textFinishSortEqualizer);
        mRelativeTemplateHeader = sActivity.findViewById(R.id.relativeTemplateHeader);
        mTextTemplateName = sActivity.findViewById(R.id.textTemplateName);
        mViewSepEqualizerHeader = sActivity.findViewById(R.id.viewSepEqualizerHeader);
        mViewSepEqualizerCustomize = sActivity.findViewById(R.id.viewSepEqualizerCustomize);
        mBtnEqualizerOff = sActivity.findViewById(R.id.btnEqualizerOff);
        mBtnEqualizerMenu = sActivity.findViewById(R.id.btnEqualizerMenu);
        mBtnAddEqualizerTemplate = sActivity.findViewById(R.id.btnAddEqualizerTemplate);
        mImgBackEqualizer = sActivity.findViewById(R.id.imgBackEqualizer);
        mBtnBackCustomize = sActivity.findViewById(R.id.btnBackCustomize);
        mBtnEqualizerRandom = sActivity.findViewById(R.id.btnEqualizerRandom);
        mBtnFinishCustomize = sActivity.findViewById(R.id.btnFinishCustomize);
        mBtnResetEqualizer = sActivity.findViewById(R.id.btnResetEqualizer);
        mBtnEqualizerSaveAs = sActivity.findViewById(R.id.btnEqualizerSaveAs);
        mScrollView = sActivity.findViewById(R.id.scrollCustomEqualizer);

        mBtnEqualizerOff.setOnClickListener(this);
        mBtnEqualizerRandom.setOnClickListener(this);
        mBtnEqualizerMenu.setOnClickListener(this);
        mBtnAddEqualizerTemplate.setOnClickListener(this);
        mBtnBackCustomize.setOnClickListener(this);
        mBtnFinishCustomize.setOnClickListener(this);
        mBtnResetEqualizer.setOnClickListener(this);
        mBtnEqualizerSaveAs.setOnClickListener(this);

        mRecyclerEqualizers.setHasFixedSize(false);
        final LinearLayoutManager equalizersManager = new LinearLayoutManager(sActivity);
        mRecyclerEqualizers.setLayoutManager(equalizersManager);
        mRecyclerEqualizers.setAdapter(mEqualizersAdapter);
        if(mRecyclerEqualizers.getItemAnimator() != null)
            ((DefaultItemAnimator) mRecyclerEqualizers.getItemAnimator()).setSupportsChangeAnimations(false);

        mTextNames = new ArrayList<>();
        mTextNames.add((TextView)sActivity.findViewById(R.id.textVolName));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text20KName));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text16KName));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text12_5KName));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text10KName));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text8KName));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text6_3KName));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text5KName));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text4KName));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text3_15KName));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text2_5KName));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text2KName));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text1_6KName));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text1_25KName));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text1KName));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text800Name));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text630Name));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text500Name));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text400Name));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text315Name));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text250Name));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text200Name));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text160Name));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text125Name));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text100Name));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text80Name));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text63Name));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text50Name));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text40Name));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text31_5Name));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text25Name));
        mTextNames.add((TextView)sActivity.findViewById(R.id.text20Name));

        mTextValues = new ArrayList<>();
        mTextValues.add((TextView)sActivity.findViewById(R.id.textVolValue));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text20KValue));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text16KValue));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text12_5KValue));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text10KValue));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text8KValue));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text6_3KValue));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text5KValue));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text4KValue));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text3_15KValue));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text2_5KValue));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text2KValue));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text1_6KValue));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text1_25KValue));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text1KValue));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text800Value));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text630Value));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text500Value));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text400Value));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text315Value));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text250Value));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text200Value));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text160Value));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text125Value));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text100Value));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text80Value));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text63Value));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text50Value));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text40Value));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text31_5Value));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text25Value));
        mTextValues.add((TextView)sActivity.findViewById(R.id.text20Value));
        for(int i = 0; i < mTextValues.size(); i++)
            mTextValues.get(i).setText("0");

        mSeeks = new ArrayList<>();
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seekVol));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek20K));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek16K));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek12_5K));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek10K));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek8K));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek6_3K));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek5K));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek4K));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek3_15K));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek2_5K));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek2K));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek1_6K));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek1_25K));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek1K));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek800));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek630));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek500));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek400));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek315));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek250));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek200));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek160));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek125));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek100));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek80));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek63));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek50));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek40));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek31_5));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek25));
        mSeeks.add((SeekBar)sActivity.findViewById(R.id.seek20));

        for(int i = 0; i < mSeeks.size(); i++) {
            final int j = i;
            mSeeks.get(i).setOnSeekBarChangeListener(
                    new SeekBar.OnSeekBarChangeListener() {
                        public void onProgressChanged(SeekBar seekBar,
                                                      int progress, boolean fromUser) {
                            int nLevel = progress - 30;
                            if(j == 0)
                            {
                                float fLevel = nLevel;
                                if(fLevel == 0) fLevel = 1.0f;
                                else if(fLevel < 0) fLevel = (fLevel + 30.0f) / 30.0f;
                                else fLevel += 1.0f;
                                if(MainActivity.sStream != 0)
                                {
                                    BASS_FX.BASS_BFX_VOLUME vol = new BASS_FX.BASS_BFX_VOLUME();
                                    vol.lChannel = 0;
                                    vol.fVolume = fLevel;
                                    BASS.BASS_FXSetParameters(MainActivity.sFxVol, vol);
                                }

                                TextView textView = mTextValues.get(j);
                                textView.setText(String.valueOf(nLevel));
                            }
                            else
                            {
                                if(MainActivity.sStream != 0)
                                {
                                    BASS_FX.BASS_BFX_PEAKEQ eq = new BASS_FX.BASS_BFX_PEAKEQ();
                                    eq.fBandwidth = 0.7f;
                                    eq.fQ = 0.0f;
                                    eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
                                    eq.fGain = sEQs[j-1] = nLevel;
                                    eq.fCenter = sCenters[j-1];
                                    BASS.BASS_FXSetParameters(sHfxs.get(j-1), eq);
                                }

                                TextView textView = mTextValues.get(j);
                                textView.setText(String.valueOf(nLevel));
                            }
                        }

                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    }
            );
        }

        mTextFinishSortEqualizer.setOnClickListener(this);

        mButtonMinus = new ArrayList<>();
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btnVolMinus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn20KMinus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn16KMinus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn12_5KMinus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn10KMinus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn8KMinus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn6_3KMinus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn5KMinus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn4KMinus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn3_15KMinus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn2_5KMinus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn2KMinus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn1_6KMinus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn1_25KMinus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn1KMinus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn800Minus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn630Minus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn500Minus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn400Minus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn315Minus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn250Minus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn200Minus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn160Minus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn125Minus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn100Minus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn80Minus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn63Minus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn50Minus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn40Minus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn31_5Minus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn25Minus));
        mButtonMinus.add((HighlightImageButton)sActivity.findViewById(R.id.btn20Minus));

        for(int i = 0; i < mButtonMinus.size(); i++) {
            final int j = i;
            mButtonMinus.get(i).setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    int nProgress = minusValue(mSeeks.get(j));
                    if(j == 0) setVol(nProgress);
                    else setEQ(j, nProgress);
                }
            });
            mButtonMinus.get(i).setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    mLongClick = j;
                    mContinue = true;
                    mHandler.post(repeatMinusValue);
                    return true;
                }
            });
            mButtonMinus.get(i).setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                        mContinue = false;
                    return false;
                }
            });
        }

        mButtonPlus = new ArrayList<>();
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btnVolPlus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn20KPlus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn16KPlus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn12_5KPlus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn10KPlus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn8KPlus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn6_3KPlus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn5KPlus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn4KPlus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn3_15KPlus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn2_5KPlus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn2KPlus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn1_6KPlus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn1_25KPlus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn1KPlus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn800Plus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn630Plus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn500Plus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn400Plus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn315Plus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn250Plus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn200Plus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn160Plus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn125Plus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn100Plus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn80Plus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn63Plus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn50Plus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn40Plus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn31_5Plus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn25Plus));
        mButtonPlus.add((HighlightImageButton)sActivity.findViewById(R.id.btn20Plus));

        for(int i = 0; i < mButtonPlus.size(); i++) {
            final int j = i;
            mButtonPlus.get(i).setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    int nProgress = plusValue(mSeeks.get(j));
                    if(j == 0) setVol(nProgress);
                    else setEQ(j, nProgress);
                }
            });
            mButtonPlus.get(i).setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    mLongClick = j;
                    mContinue = true;
                    mHandler.post(repeatPlusValue);
                    return true;
                }
            });
            mButtonPlus.get(i).setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                        mContinue = false;
                    return false;
                }
            });
        }

        new SwipeHelper(sActivity, mRecyclerEqualizers) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        getString(R.string.delete),
                        0,
                        Color.parseColor("#FE3B30"),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                mEqualizersAdapter.notifyDataSetChanged();
                                askDeletePreset(pos);
                            }
                        }
                ));
            }
        };

        loadData();

        boolean selected = false;
        for(int i = 0; i < sEqualizerItems.size(); i++) {
            if(sEqualizerItems.get(i).isSelected()) selected = true;
        }
        mBtnEqualizerOff.setSelected(!selected);

        if(sActivity.isDarkMode()) setDarkMode(false);
    }

    private final Runnable repeatMinusValue = new Runnable() {
        @Override
        public void run() {
            if(!mContinue) return;
            int nProgress = minusValue(mSeeks.get(mLongClick));
            if(mLongClick == 0) setVol(nProgress);
            else setEQ(mLongClick, nProgress);
            mHandler.postDelayed(this, 100);
        }
    };

    private final Runnable repeatPlusValue = new Runnable() {
        @Override
        public void run() {
            if(!mContinue) return;
            int nProgress = plusValue(mSeeks.get(mLongClick));
            if(mLongClick == 0) setVol(nProgress);
            else setEQ(mLongClick, nProgress);
            mHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.textFinishSortEqualizer:
                mRecyclerEqualizers.setPadding(0, 0, 0, 0);
                mBtnEqualizerOff.setVisibility(View.VISIBLE);
                mViewSepEqualizerHeader.setVisibility(View.VISIBLE);
                mBtnAddEqualizerTemplate.setAlpha(1.0f);
                mTextFinishSortEqualizer.setVisibility(View.GONE);
                mSorting = false;
                mEqualizersAdapter.notifyDataSetChanged();
                mEqualizerTouchHelper.attachToRecyclerView(null);
                break;

            case R.id.btnEqualizerOff:
                mBtnEqualizerOff.setSelected(true);
                for(int i = 0; i < sEqualizerItems.size(); i++)
                    sEqualizerItems.get(i).setSelected(false);
                mEqualizersAdapter.notifyDataSetChanged();
                resetEQ();
                break;

            case R.id.btnEqualizerMenu:
                showEqualizerMenu();
                break;

            case R.id.btnAddEqualizerTemplate:
                mAddTemplate = true;
                mTextTemplateName.setText(R.string.newEqualizer);

                mBtnBackCustomize.setText(R.string.cancel);
                mBtnFinishCustomize.setText(R.string.save);

                mRelativeTemplateHeader.setVisibility(View.VISIBLE);
                mViewSepEqualizerCustomize.setVisibility(View.VISIBLE);
                mBtnEqualizerRandom.setVisibility(View.VISIBLE);
                mScrollView.setVisibility(View.VISIBLE);
                mImgBackEqualizer.setVisibility(View.INVISIBLE);
                mBtnBackCustomize.setPadding((int)(16 * sActivity.getDensity()), mBtnBackCustomize.getPaddingTop(), mBtnBackCustomize.getPaddingRight(), mBtnBackCustomize.getPaddingBottom());
                mBtnEqualizerOff.setVisibility(View.INVISIBLE);
                mRecyclerEqualizers.setVisibility(View.INVISIBLE);
                mBtnEqualizerSaveAs.setVisibility(View.GONE);
                mBtnAddEqualizerTemplate.setAlpha(0.0f);
                break;

            case R.id.btnBackCustomize:
            {
                mRelativeTemplateHeader.setVisibility(View.INVISIBLE);
                mViewSepEqualizerCustomize.setVisibility(View.INVISIBLE);
                mBtnEqualizerRandom.setVisibility(View.INVISIBLE);
                mScrollView.setVisibility(View.INVISIBLE);
                mBtnEqualizerOff.setVisibility(View.VISIBLE);
                mRecyclerEqualizers.setVisibility(View.VISIBLE);
                mBtnAddEqualizerTemplate.setAlpha(1.0f);

                EqualizerItem item;
                int nItem = 0;
                for (; nItem < sEqualizerItems.size(); nItem++) {
                    item = sEqualizerItems.get(nItem);
                    if (item.isSelected()) break;
                }
                if (nItem < sEqualizerItems.size()) setEQ(nItem);
                else resetEQ();
            }
                break;

            case R.id.btnFinishCustomize:
                if(mAddTemplate) {
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
                    editPreset.setText(R.string.newTemplate);
                    linearLayout.addView(editPreset);
                    builder.setView(linearLayout);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ArrayList<Integer> arPresets = new ArrayList<>();
                            for (int i = 0; i < 32; i++) {
                                arPresets.add(Integer.parseInt((String) mTextValues.get(i).getText()));
                            }
                            sEqualizerItems.add(new EqualizerItem(editPreset.getText().toString(), arPresets));
                            mEqualizersAdapter.notifyItemInserted(sEqualizerItems.size() - 1);
                            saveData();

                            mRelativeTemplateHeader.setVisibility(View.INVISIBLE);
                            mViewSepEqualizerCustomize.setVisibility(View.INVISIBLE);
                            mBtnEqualizerRandom.setVisibility(View.INVISIBLE);
                            mScrollView.setVisibility(View.INVISIBLE);
                            mBtnEqualizerOff.setVisibility(View.VISIBLE);
                            mRecyclerEqualizers.setVisibility(View.VISIBLE);
                            mBtnAddEqualizerTemplate.setAlpha(1.0f);

                            for(int i = 0; i < sEqualizerItems.size()-1; i++)
                                sEqualizerItems.get(i).setSelected(false);
                            sEqualizerItems.get(sEqualizerItems.size()-1).setSelected(true);
                            mEqualizersAdapter.notifyDataSetChanged();
                            mRecyclerEqualizers.scrollToPosition(sEqualizerItems.size()-1);
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
                            InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (null != imm) imm.showSoftInput(editPreset, 0);
                        }
                    });
                    alertDialog.show();
                }
                else {
                    EqualizerItem item = null;
                    int nItem = 0;
                    for (; nItem < sEqualizerItems.size(); nItem++) {
                        item = sEqualizerItems.get(nItem);
                        if (item.isSelected()) break;
                    }
                    if (item != null) {
                        ArrayList<Integer> arPresets = item.getArPresets();
                        for (int i = 0; i < 32; i++) {
                            arPresets.set(i, Integer.parseInt((String) mTextValues.get(i).getText()));
                        }
                        saveData();
                    }
                    mRelativeTemplateHeader.setVisibility(View.INVISIBLE);
                    mViewSepEqualizerCustomize.setVisibility(View.INVISIBLE);
                    mBtnEqualizerRandom.setVisibility(View.INVISIBLE);
                    mScrollView.setVisibility(View.INVISIBLE);
                    mBtnEqualizerOff.setVisibility(View.VISIBLE);
                    mRecyclerEqualizers.setVisibility(View.VISIBLE);
                    mBtnAddEqualizerTemplate.setAlpha(1.0f);
                }
                break;

            case R.id.btnEqualizerRandom:
                setEQRandom();
                break;

            case R.id.btnResetEqualizer:
                EqualizerItem item;
                int nItem = 0;
                for (; nItem < sEqualizerItems.size(); nItem++) {
                    item = sEqualizerItems.get(nItem);
                    if (item.isSelected()) break;
                }
                if (nItem < sEqualizerItems.size()) setEQ(nItem);
                else resetEQ();
                break;

            case R.id.btnEqualizerSaveAs:
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
                editPreset.setText(R.string.newTemplate);
                linearLayout.addView(editPreset);
                builder.setView(linearLayout);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ArrayList<Integer> arPresets = new ArrayList<>();
                        for (int i = 0; i < 32; i++) {
                            arPresets.add(Integer.parseInt((String) mTextValues.get(i).getText()));
                        }
                        sEqualizerItems.add(new EqualizerItem(editPreset.getText().toString(), arPresets));
                        mEqualizersAdapter.notifyItemInserted(sEqualizerItems.size() - 1);
                        saveData();

                        mRelativeTemplateHeader.setVisibility(View.INVISIBLE);
                        mViewSepEqualizerCustomize.setVisibility(View.INVISIBLE);
                        mBtnEqualizerRandom.setVisibility(View.INVISIBLE);
                        mScrollView.setVisibility(View.INVISIBLE);
                        mBtnEqualizerOff.setVisibility(View.VISIBLE);
                        mRecyclerEqualizers.setVisibility(View.VISIBLE);
                        mBtnAddEqualizerTemplate.setAlpha(1.0f);

                        for(int i = 0; i < sEqualizerItems.size()-1; i++)
                            sEqualizerItems.get(i).setSelected(false);
                        sEqualizerItems.get(sEqualizerItems.size()-1).setSelected(true);
                        mEqualizersAdapter.notifyDataSetChanged();
                        mRecyclerEqualizers.scrollToPosition(sEqualizerItems.size()-1);
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
                        InputMethodManager imm = (InputMethodManager) sActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (null != imm) imm.showSoftInput(editPreset, 0);
                    }
                });
                alertDialog.show();
                break;

            default:
                break;
        }
    }

    private int minusValue(SeekBar seek) {
        int nProgress = seek.getProgress();
        nProgress -= 1;
        if(nProgress < 0) nProgress = 0;
        return (nProgress - 30);
    }

    private int plusValue(SeekBar seek) {
        int nProgress = seek.getProgress();
        nProgress += 1;
        if(nProgress > 60) nProgress = 60;
        return (nProgress - 30);
    }

    void onEqualizerItemClick(int nEqualizer) {
        EqualizerItem itemSelected = sEqualizerItems.get(nEqualizer);
        boolean bSelected = !itemSelected.isSelected();
        mBtnEqualizerOff.setSelected(!bSelected);
        int nSelected = -1;
        if(bSelected) {
            nSelected = nEqualizer;
            setEQ(nSelected);
        }
        else resetEQ();
        for (int i = 0; i < sEqualizerItems.size(); i++) {
            EqualizerItem item = sEqualizerItems.get(i);
            if (i == nSelected) item.setSelected(true);
            else item.setSelected(false);
        }
        mEqualizersAdapter.notifyDataSetChanged();
    }

    void onEqualizerDetailClick(int nEqualizer) {
        mAddTemplate = false;

        if(!isSelectedItem(nEqualizer)) onEqualizerItemClick(nEqualizer);

        EqualizerItem item = sEqualizerItems.get(nEqualizer);
        mTextTemplateName.setText(item.getEqualizerName());

        mBtnBackCustomize.setText(R.string.back);
        mBtnFinishCustomize.setText(R.string.done);

        mRelativeTemplateHeader.setVisibility(View.VISIBLE);
        mViewSepEqualizerCustomize.setVisibility(View.VISIBLE);
        mBtnEqualizerRandom.setVisibility(View.VISIBLE);
        mScrollView.setVisibility(View.VISIBLE);
        mImgBackEqualizer.setVisibility(View.VISIBLE);
        mBtnBackCustomize.setPadding((int)(32 * sActivity.getDensity()), mBtnBackCustomize.getPaddingTop(), mBtnBackCustomize.getPaddingRight(), mBtnBackCustomize.getPaddingBottom());
        mBtnEqualizerOff.setVisibility(View.INVISIBLE);
        mRecyclerEqualizers.setVisibility(View.INVISIBLE);
        mBtnEqualizerSaveAs.setVisibility(View.VISIBLE);
        mBtnAddEqualizerTemplate.setAlpha(0.0f);
    }

    private void loadData() {
        if(sEqualizerItems != null && sEqualizerItems.size() > 0) return;

        SharedPreferences preferences = sActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        ArrayList<EqualizerItem> equalizerItems = gson.fromJson(preferences.getString("arEqualizerItems",""), new TypeToken<ArrayList<EqualizerItem>>(){}.getType());
        if(equalizerItems != null) setArEqualizerItems(equalizerItems);
        else resetPresets();
        mBtnEqualizerOff.setSelected(true);
        for(int i = 0; i < sEqualizerItems.size(); i++)
            sEqualizerItems.get(i).setSelected(false);
        if(sEqualizerItems.get(0).getEqualizerName().equals(getString(R.string.off)))
            removeItem(0);
        if(sEqualizerItems.get(0).getEqualizerName().equals(getString(R.string.random)))
            removeItem(0);
    }

    private void saveData() {
        SharedPreferences preferences = sActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        preferences.edit().putString("arEqualizerItems", gson.toJson(sEqualizerItems)).apply();
    }

    private void resetPresets() {
        if(sEqualizerItems.size() > 0) sEqualizerItems.clear();
        sEqualizerItems.add(new EqualizerItem(getString(R.string.transcribeBass), new ArrayList<>(Arrays.asList(  0,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.VocalBoost), new ArrayList<>(Arrays.asList(  0,-30,-20,-12, -7, -4, -3, -2, -1,  0,  0,  0,  0,  0, -1, -2, -3, -4, -7,-12,-20,-24,-27,-28,-29,-30,-30,-30,-30,-30,-30,-30))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.VocalReducer), new ArrayList<>(Arrays.asList(  0,  0, -5, -8,-10,-12,-13,-14,-14,-15,-15,-15,-15,-15,-14,-14,-13,-12,-11, -8, -5, -3, -2, -1,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleBoostUltraStrong), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,-15,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleBoostStrongest), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -7,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleBoostStrong), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleBoostMiddle), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -4, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleBoostWeak), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleBoostWeakest), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleBoostUltraStrong), new ArrayList<>(Arrays.asList(  0,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,-15,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleBoostStrongest), new ArrayList<>(Arrays.asList(  0,-15,-15,-15,-15,-15,-15,-15,-15,-15, -7,  0,  0,  0,  0,  0,  0,  0,  0,  0, -7,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleBoostStrong), new ArrayList<>(Arrays.asList(  0,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleBoostMiddle), new ArrayList<>(Arrays.asList(  0, -9, -9, -9, -9, -9, -9, -9, -9, -9, -4,  0,  0,  0,  0,  0,  0,  0,  0,  0, -4, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleBoostWeak), new ArrayList<>(Arrays.asList(  0, -6, -6, -6, -6, -6, -6, -6, -6, -6, -3,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleBoostWeakest), new ArrayList<>(Arrays.asList(  0, -3, -3, -3, -3, -3, -3, -3, -3, -3, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.BassBoostUltraStrong), new ArrayList<>(Arrays.asList(  0,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.BassBoostStrongest), new ArrayList<>(Arrays.asList(  0,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15, -7,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.BassBoostStrong), new ArrayList<>(Arrays.asList(  0,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.BassBoostMiddle), new ArrayList<>(Arrays.asList(  0, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -4,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.BassBoostWeak), new ArrayList<>(Arrays.asList(  0, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.BassBoostWeakest), new ArrayList<>(Arrays.asList(  0, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleCutUltraStrong), new ArrayList<>(Arrays.asList(  0,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleCutStrongest), new ArrayList<>(Arrays.asList(  0,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15, -8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleCutStrong), new ArrayList<>(Arrays.asList(  0,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleCutMiddle), new ArrayList<>(Arrays.asList(  0, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleCutWeak), new ArrayList<>(Arrays.asList(  0, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleCutWeakest), new ArrayList<>(Arrays.asList(  0, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleCutUltraStrong), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,-15,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleCutStrongest), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -8,-15,-15,-15,-15,-15,-15,-15,-15,-15, -8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleCutStrong), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleCutMiddle), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleCutWeak), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3, -6, -6, -6, -6, -6, -6, -6, -6, -6, -3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleCutWeakest), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -2, -3, -3, -3, -3, -3, -3, -3, -3, -3, -2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.BassCutUltraStrong), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,-15,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.BassCutStrongest), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -8,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.BassCutStrong), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.BassCutMiddle), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.BassCutWeak), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.BassCutWeakest), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -2, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.Pop), new ArrayList<>(Arrays.asList(  0, -6, -6, -6, -5, -5, -5, -5, -4, -3, -3, -2, -1, -1,  0,  0,  0,  0, -1, -1, -2, -3, -3, -4, -5, -5, -5, -5, -6, -6, -6, -6))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.Rock), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -2, -3, -4, -5, -6, -5, -4, -3, -2, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.Jazz), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -2, -3, -4, -5, -6, -6, -6, -6, -3, -2, -1, -2, -2, -2, -2, -2, -1, -1, -1,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.Electronic), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0, -1, -2, -3, -4, -3, -2, -1, -3, -5, -7, -3, -2, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        sEqualizerItems.add(new EqualizerItem(getString(R.string.Acoustic), new ArrayList<>(Arrays.asList(  0, -2, -2, -2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, -3, -3, -3, -3, -3, -4, -4, -3, -2, -1, -1, -1,  0,  0,  0,  0,  0,  0))));
        saveData();
        mEqualizersAdapter.notifyDataSetChanged();

        resetEQ();
    }

    public static void setEQ() {
        for(int i = 0; i < 32; i++) {
            if(i == 0) {
                float fLevel = sVol;
                if(fLevel == 0) fLevel = 1.0f;
                else if(fLevel < 0) fLevel = (fLevel + 30.0f) / 30.0f;
                else fLevel += 1.0f;
                if(MainActivity.sStream != 0) {
                    BASS_FX.BASS_BFX_VOLUME vol = new BASS_FX.BASS_BFX_VOLUME();
                    vol.lChannel = 0;
                    vol.fVolume = fLevel;
                    BASS.BASS_FXSetParameters(MainActivity.sFxVol, vol);
                }
            }
            else {
                if(MainActivity.sStream != 0) {
                    BASS_FX.BASS_BFX_PEAKEQ eq = new BASS_FX.BASS_BFX_PEAKEQ();
                    eq.fBandwidth = 0.7f;
                    eq.fQ = 0.0f;
                    eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
                    eq.fGain = sEQs[i-1];
                    eq.fCenter = sCenters[i-1];
                    BASS.BASS_FXSetParameters(sHfxs.get(i-1), eq);
                }
            }
        }
    }

    public void setEQ(int row) {
        if(sEqualizerItems.get(row).getEqualizerName().equals(getString(R.string.random))) {
            setEQRandom();
            return;
        }
        else if(sEqualizerItems.get(row).getEqualizerName().equals(getString(R.string.transcribeBassOctave)))
            sActivity.controlFragment.setPitch(12.0f);

        for(int i = 0; i < 32; i++) {
            int nLevel = sEqualizerItems.get(row).getArPresets().get(i);
            if(i == 0) {
                float fLevel = nLevel;
                if(fLevel == 0) fLevel = 1.0f;
                else if(fLevel < 0) fLevel = (fLevel + 30.0f) / 30.0f;
                else fLevel += 1.0f;
                if(MainActivity.sStream != 0) {
                    BASS_FX.BASS_BFX_VOLUME vol = new BASS_FX.BASS_BFX_VOLUME();
                    vol.lChannel = 0;
                    vol.fVolume = fLevel;
                    BASS.BASS_FXSetParameters(MainActivity.sFxVol, vol);
                }

                sVol = nLevel;
                SeekBar seekBar = mSeeks.get(i);
                seekBar.setProgress(nLevel + 30);
                TextView textView = mTextValues.get(i);
                textView.setText(String.valueOf(nLevel));
            }
            else {
                if(MainActivity.sStream != 0) {
                    BASS_FX.BASS_BFX_PEAKEQ eq = new BASS_FX.BASS_BFX_PEAKEQ();
                    eq.fBandwidth = 0.7f;
                    eq.fQ = 0.0f;
                    eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
                    eq.fGain = sEQs[i-1] = nLevel;
                    eq.fCenter = sCenters[i-1];
                    BASS.BASS_FXSetParameters(sHfxs.get(i-1), eq);
                }

                sEQs[i-1] = nLevel;
                SeekBar seekBar = mSeeks.get(i);
                seekBar.setProgress(nLevel + 30);
                TextView textView = mTextValues.get(i);
                textView.setText(String.valueOf(nLevel));
            }
        }
        PlaylistFragment.updateSavingEffect();
    }

    static void setEQRandom() {
        int nLevel = 0;
        float fLevel = 1.0f;
        if(MainActivity.sStream != 0) {
            BASS_FX.BASS_BFX_VOLUME vol = new BASS_FX.BASS_BFX_VOLUME();
            vol.lChannel = 0;
            vol.fVolume = fLevel;
            BASS.BASS_FXSetParameters(MainActivity.sFxVol, vol);
        }

        if(sActivity != null) {
            SeekBar seekBar = sActivity.equalizerFragment.getSeeks().get(0);
            seekBar.setProgress(nLevel + 30);
            TextView textView = sActivity.equalizerFragment.getTextValues().get(0);
            textView.setText(String.valueOf(nLevel));
        }

        for(int i = 0; i < 31; i++) {
            Random random = new Random();
            nLevel = random.nextInt(30) - 20;
            if(MainActivity.sStream != 0) {
                BASS_FX.BASS_BFX_PEAKEQ eq = new BASS_FX.BASS_BFX_PEAKEQ();
                eq.fBandwidth = 0.7f;
                eq.fQ = 0.0f;
                eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
                eq.fGain = sEQs[i] = nLevel;
                eq.fCenter = sCenters[i];
                BASS.BASS_FXSetParameters(sHfxs.get(i), eq);
            }

            if(sActivity != null) {
                SeekBar seekBar = sActivity.equalizerFragment.getSeeks().get(i + 1);
                seekBar.setProgress(nLevel + 30);
                TextView textView = sActivity.equalizerFragment.getTextValues().get(i + 1);
                textView.setText(String.valueOf(nLevel));
            }
        }
        PlaylistFragment.updateSavingEffect();
    }

    public static void resetEQ() {
        resetEQ(true);
    }

    public static void resetEQ(boolean save) {
        resetEQ(save, true);
    }

    public static void resetEQ(boolean save, boolean btnOffSelected) {
        int nLevel = 0;
        float fLevel = 1.0f;
        if(MainActivity.sStream != 0) {
            BASS_FX.BASS_BFX_VOLUME vol = new BASS_FX.BASS_BFX_VOLUME();
            vol.lChannel = 0;
            vol.fVolume = fLevel;
            BASS.BASS_FXSetParameters(MainActivity.sFxVol, vol);
        }

        if (sActivity != null) {
            if (btnOffSelected) sActivity.equalizerFragment.getBtnEqualizerOff().setSelected(true);

            SeekBar seekBar = sActivity.equalizerFragment.getSeeks().get(0);
            seekBar.setProgress(nLevel + 30);
            TextView textView = sActivity.equalizerFragment.getTextValues().get(0);
            textView.setText(String.valueOf(nLevel));
        }

        for(int i = 0; i < 31; i++) {
            if(MainActivity.sStream != 0) {
                BASS_FX.BASS_BFX_PEAKEQ eq = new BASS_FX.BASS_BFX_PEAKEQ();
                eq.fBandwidth = 0.7f;
                eq.fQ = 0.0f;
                eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
                eq.fGain = sEQs[i] = 0;
                eq.fCenter = sCenters[i];
                BASS.BASS_FXSetParameters(sHfxs.get(i), eq);
            }

            if (sActivity != null) {
                SeekBar seekBar = sActivity.equalizerFragment.getSeeks().get(i + 1);
                seekBar.setProgress(nLevel + 30);
                TextView textView = sActivity.equalizerFragment.getTextValues().get(i + 1);
                textView.setText(String.valueOf(nLevel));
            }
        }
        for(int i = 0; i < sEqualizerItems.size(); i++) {
            EqualizerItem item = sEqualizerItems.get(i);
            item.setSelected(false);
        }
        if (sActivity != null) sActivity.equalizerFragment.getEqualizersAdapter().notifyDataSetChanged();
        if(save) PlaylistFragment.updateSavingEffect();
    }

    public void setVol(int nLevel) {
        setVol(nLevel, true);
    }

    public static void setVol(int nLevel, boolean bSave) {
        sVol = nLevel;
        float fLevel = nLevel;
        if(fLevel == 0) fLevel = 1.0f;
        else if(fLevel < 0) fLevel = (fLevel + 30.0f) / 30.0f;
        else fLevel += 1.0f;
        if(MainActivity.sStream != 0) {
            BASS_FX.BASS_BFX_VOLUME vol = new BASS_FX.BASS_BFX_VOLUME();
            vol.lChannel = 0;
            vol.fVolume = fLevel;
            BASS.BASS_FXSetParameters(MainActivity.sFxVol, vol);
        }

        if(bSave) PlaylistFragment.updateSavingEffect();

        if(sActivity != null) sActivity.equalizerFragment.updateVol(nLevel);
    }

    private void updateVol(int nLevel) {
        SeekBar seekBar = mSeeks.get(0);
        seekBar.setProgress(nLevel + 30);
        TextView textView = mTextValues.get(0);
        textView.setText(String.valueOf(nLevel));
    }

    public void setEQ(int i, int nLevel) {
        setEQ(i, nLevel, true);
    }

    public static void setEQ(int i, int nLevel, boolean bSave) {
        sEQs[i-1] = nLevel;
        if(MainActivity.sStream != 0) {
            BASS_FX.BASS_BFX_PEAKEQ eq = new BASS_FX.BASS_BFX_PEAKEQ();
            eq.fBandwidth = 0.7f;
            eq.fQ = 0.0f;
            eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
            eq.fGain = sEQs[i-1] = nLevel;
            eq.fCenter = sCenters[i-1];
            BASS.BASS_FXSetParameters(sHfxs.get(i-1), eq);
        }

        if (bSave) PlaylistFragment.updateSavingEffect();

        if(sActivity != null) sActivity.equalizerFragment.updateEQ(i, nLevel);
    }

    private void updateEQ(int i, int nLevel) {
        SeekBar seekBar = mSeeks.get(i);
        seekBar.setProgress(nLevel + 30);
        TextView textView = mTextValues.get(i);
        textView.setText(String.valueOf(nLevel));
    }

    static void setArHFX(ArrayList<Integer> hfxs) {
        sHfxs = new ArrayList<>(hfxs);
    }

    private void showEqualizerMenu() {
        final BottomMenu menu = new BottomMenu(sActivity);
        menu.setTitle(getString(R.string.equalizerTemplate));
        menu.addMenu(getString(R.string.sortTemplate), sActivity.isDarkMode() ? R.drawable.ic_actionsheet_sort_dark : R.drawable.ic_actionsheet_sort, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                mRecyclerEqualizers.setPadding(0, 0, 0, (int)(64 * sActivity.getDensity()));
                mBtnEqualizerOff.setVisibility(View.GONE);
                mViewSepEqualizerHeader.setVisibility(View.GONE);
                mBtnAddEqualizerTemplate.setAlpha(0.0f);
                mTextFinishSortEqualizer.setVisibility(View.VISIBLE);
                mSorting = true;
                mEqualizersAdapter.notifyDataSetChanged();

                mEqualizerTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView mRecyclerEqualizers, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        final int fromPos = viewHolder.getAdapterPosition();
                        final int toPos = target.getAdapterPosition();

                        EqualizerItem itemTemp = sEqualizerItems.get(fromPos);
                        sEqualizerItems.remove(fromPos);
                        sEqualizerItems.add(toPos, itemTemp);

                        mEqualizersAdapter.notifyItemMoved(fromPos, toPos);

                        return true;
                    }

                    @Override
                    public void clearView(@NonNull RecyclerView recyclerSongs, @NonNull RecyclerView.ViewHolder viewHolder) {
                        super.clearView(recyclerSongs, viewHolder);

                        mEqualizersAdapter.notifyDataSetChanged();
                        saveData();
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }

                    @Override
                    public boolean isLongPressDragEnabled() {
                        return false;
                    }
                });
                mEqualizerTouchHelper.attachToRecyclerView(mRecyclerEqualizers);
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
                        resetPresets();
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
        menu.setTitle(sEqualizerItems.get(nItem).getEqualizerName());
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
                editPreset.setText(sEqualizerItems.get(nItem).getEqualizerName());
                linearLayout.addView(editPreset);
                builder.setView(linearLayout);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sEqualizerItems.get(nItem).setEqualizerName(editPreset.getText().toString());
                        mEqualizersAdapter.notifyItemChanged(nItem);
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
                EqualizerItem item = sEqualizerItems.get(nItem);
                ArrayList<Integer> arPresets = new ArrayList<>();
                for (int i = 0; i < 32; i++) {
                    arPresets.add(item.getArPresets().get(i));
                }
                sEqualizerItems.add(nItem+1, new EqualizerItem(item.getEqualizerName(), arPresets));
                mEqualizersAdapter.notifyItemInserted(nItem+1);
                saveData();
                mRecyclerEqualizers.scrollToPosition(nItem+1);
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
        builder.setTitle(sEqualizerItems.get(item).getEqualizerName());
        builder.setMessage(R.string.askDeleteTemplate);
        builder.setPositiveButton(R.string.decideNot, null);
        builder.setNegativeButton(R.string.doDelete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                removeItem(item);
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
                positiveButton.setTextColor(getResources().getColor(sActivity.isDarkMode() ? R.color.darkModeRed : R.color.lightModeRed));
            }
        });
        alertDialog.show();
    }

    private void removeItem(int nItem) {
        if(isSelectedItem(nItem)) resetEQ();
        sEqualizerItems.remove(nItem);
        mEqualizersAdapter.notifyItemRemoved(nItem);
        saveData();
    }

    public void setLightMode(boolean animated) {
        final int nLightModeBk = getResources().getColor(R.color.lightModeBk);
        final int nDarkModeBk = getResources().getColor(R.color.darkModeBk);
        final int nLightModeText = getResources().getColor(android.R.color.black);
        final int nDarkModeText = getResources().getColor(android.R.color.white);
        final int nDarkModeSep = getResources().getColor(R.color.darkModeSep);
        final int nLightModeSep = getResources().getColor(R.color.lightModeSep);
        final int nLightModeBlue = getResources().getColor(R.color.lightModeBlue);
        final int nDarkModeBlue = getResources().getColor(R.color.darkModeBlue);
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
                    mRelativeTemplateHeader.setBackgroundColor(nColorModeBk);
                    mTextTemplateName.setTextColor(nColorModeText);
                    mViewSepEqualizerHeader.setBackgroundColor(nColorModeSep);
                    mViewSepEqualizerCustomize.setBackgroundColor(nColorModeSep);
                    mBtnBackCustomize.setTextColor(nColorModeBlue);
                    mBtnFinishCustomize.setTextColor(nColorModeBlue);
                    mBtnEqualizerRandom.setTextColor(nColorModeBlue);
                    mBtnResetEqualizer.setTextColor(nColorModeBlue);
                    for(int i = 0; i < mTextNames.size(); i++) {
                        mTextNames.get(i).setTextColor(nColorModeText);
                    }
                    for(int i = 0; i < mTextValues.size(); i++) {
                        mTextValues.get(i).setTextColor(nColorModeText);
                    }
                    mTextFinishSortEqualizer.setBackgroundColor(nColorModeBlue);
                    mTextFinishSortEqualizer.setTextColor(nColorModeBk);
                }
            });
            TransitionDrawable tdBtnEqualizerMenu = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_button_more_blue_dark), getResources().getDrawable(R.drawable.ic_button_more_blue)});
            TransitionDrawable tdBtnAddEqualizerTemplate = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.button_big_equalizer_dark), getResources().getDrawable(R.drawable.button_big_equalizer)});
            TransitionDrawable tdImgBackEqualizer = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_button_back_dark), getResources().getDrawable(R.drawable.ic_button_back)});

            mBtnEqualizerMenu.setImageDrawable(tdBtnEqualizerMenu);
            mBtnAddEqualizerTemplate.setImageDrawable(tdBtnAddEqualizerTemplate);
            mImgBackEqualizer.setImageDrawable(tdImgBackEqualizer);

            int duration = 300;
            anim.setDuration(duration).start();
            tdBtnEqualizerMenu.startTransition(duration);
            tdBtnAddEqualizerTemplate.startTransition(duration);
            tdImgBackEqualizer.startTransition(duration);
        }
        else {
            mRelativeTemplateHeader.setBackgroundColor(nLightModeBk);
            mTextTemplateName.setTextColor(nLightModeText);
            mViewSepEqualizerHeader.setBackgroundColor(nLightModeSep);
            mViewSepEqualizerCustomize.setBackgroundColor(nLightModeSep);
            mBtnBackCustomize.setTextColor(nLightModeBlue);
            mBtnFinishCustomize.setTextColor(nLightModeBlue);
            mBtnEqualizerRandom.setTextColor(nLightModeBlue);
            mBtnResetEqualizer.setTextColor(nLightModeBlue);
            for(int i = 0; i < mTextNames.size(); i++) {
                mTextNames.get(i).setTextColor(nLightModeText);
            }
            for(int i = 0; i < mTextValues.size(); i++) {
                mTextValues.get(i).setTextColor(nLightModeText);
            }
            mTextFinishSortEqualizer.setBackgroundColor(nLightModeBlue);
            mTextFinishSortEqualizer.setTextColor(nLightModeBk);
            mBtnEqualizerMenu.setImageDrawable(getResources().getDrawable(R.drawable.ic_button_more_blue));
            mBtnAddEqualizerTemplate.setImageDrawable(getResources().getDrawable(R.drawable.button_big_equalizer));
            mImgBackEqualizer.setImageDrawable(getResources().getDrawable(R.drawable.ic_button_back));
        }

        mBtnEqualizerOff.setTextColor(getResources().getColorStateList(R.color.btn_text));
        mBtnEqualizerOff.setBackgroundResource(R.drawable.btn_border_background);
        mBtnEqualizerRandom.setBackgroundResource(R.drawable.resetbutton);
        mBtnResetEqualizer.setBackgroundResource(R.drawable.resetbutton);
        for(int i = 0; i < mSeeks.size(); i++) {
            mSeeks.get(i).setProgressDrawable(getResources().getDrawable(R.drawable.progress));
            mSeeks.get(i).setThumb(getResources().getDrawable(R.drawable.thumbplaying));
        }
        for(int i = 0; i < mButtonMinus.size(); i++) {
            mButtonMinus.get(i).setBackgroundResource(R.drawable.ic_button_minus);
        }
        for(int i = 0; i < mButtonPlus.size(); i++) {
            mButtonPlus.get(i).setBackgroundResource(R.drawable.ic_button_plus);
        }
        mBtnEqualizerSaveAs.setTextColor(getResources().getColorStateList(R.color.btn_text));
        mBtnEqualizerSaveAs.setBackgroundResource(R.drawable.btn_border_background);
        mEqualizersAdapter.notifyDataSetChanged();
    }

    public void setDarkMode(boolean animated) {
        if(sActivity == null) return;
        final int nLightModeBk = getResources().getColor(R.color.lightModeBk);
        final int nDarkModeBk = getResources().getColor(R.color.darkModeBk);
        final int nLightModeText = getResources().getColor(android.R.color.black);
        final int nDarkModeText = getResources().getColor(android.R.color.white);
        final int nDarkModeSep = getResources().getColor(R.color.darkModeSep);
        final int nLightModeSep = getResources().getColor(R.color.lightModeSep);
        final int nLightModeBlue = getResources().getColor(R.color.lightModeBlue);
        final int nDarkModeBlue = getResources().getColor(R.color.darkModeBlue);
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
                mRelativeTemplateHeader.setBackgroundColor(nColorModeBk);
                mTextTemplateName.setTextColor(nColorModeText);
                mViewSepEqualizerHeader.setBackgroundColor(nColorModeSep);
                mViewSepEqualizerCustomize.setBackgroundColor(nColorModeSep);
                mBtnBackCustomize.setTextColor(nColorModeBlue);
                mBtnFinishCustomize.setTextColor(nColorModeBlue);
                mBtnEqualizerRandom.setTextColor(nColorModeBlue);
                mBtnResetEqualizer.setTextColor(nColorModeBlue);
                for(int i = 0; i < mTextNames.size(); i++) {
                    mTextNames.get(i).setTextColor(nColorModeText);
                }
                for(int i = 0; i < mTextValues.size(); i++) {
                    mTextValues.get(i).setTextColor(nColorModeText);
                }
                mTextFinishSortEqualizer.setBackgroundColor(nColorModeBlue);
                mTextFinishSortEqualizer.setTextColor(nColorModeBk);
            }
        });
        TransitionDrawable tdBtnEqualizerMenu = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_button_more_blue), getResources().getDrawable(R.drawable.ic_button_more_blue_dark)});
        TransitionDrawable tdBtnAddEqualizerTemplate = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.button_big_equalizer), getResources().getDrawable(R.drawable.button_big_equalizer_dark)});
        TransitionDrawable tdImgBackEqualizer = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_button_back), getResources().getDrawable(R.drawable.ic_button_back_dark)});

        mBtnEqualizerMenu.setImageDrawable(tdBtnEqualizerMenu);
        mBtnAddEqualizerTemplate.setImageDrawable(tdBtnAddEqualizerTemplate);
        mImgBackEqualizer.setImageDrawable(tdImgBackEqualizer);

        int duration = animated ? 300 : 0;
        anim.setDuration(duration).start();
        tdBtnEqualizerMenu.startTransition(duration);
        tdBtnAddEqualizerTemplate.startTransition(duration);
        tdImgBackEqualizer.startTransition(duration);

        mBtnEqualizerOff.setTextColor(getResources().getColorStateList(R.color.btn_text_dark));
        mBtnEqualizerOff.setBackgroundResource(R.drawable.btn_border_background_dark);
        mBtnEqualizerRandom.setBackgroundResource(R.drawable.resetbutton_dark);
        mBtnResetEqualizer.setBackgroundResource(R.drawable.resetbutton_dark);
        for(int i = 0; i < mSeeks.size(); i++) {
            mSeeks.get(i).setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
            mSeeks.get(i).setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
        }
        for(int i = 0; i < mButtonMinus.size(); i++) {
            mButtonMinus.get(i).setBackgroundResource(R.drawable.ic_button_minus_dark);
        }
        for(int i = 0; i < mButtonPlus.size(); i++) {
            mButtonPlus.get(i).setBackgroundResource(R.drawable.ic_button_plus_dark);
        }
        mBtnEqualizerSaveAs.setTextColor(getResources().getColorStateList(R.color.btn_text_dark));
        mBtnEqualizerSaveAs.setBackgroundResource(R.drawable.btn_border_background_dark);
        mEqualizersAdapter.notifyDataSetChanged();
    }
}
