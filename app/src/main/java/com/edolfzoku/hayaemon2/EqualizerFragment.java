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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
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
import java.util.Random;

public class EqualizerFragment extends Fragment implements View.OnClickListener {
    private MainActivity mActivity = null;
    private RecyclerView mRecyclerEqualizers;
    private EqualizersAdapter mEqualizersAdapter;
    private ArrayList<EqualizerItem> mEqualizerItems;
    private ItemTouchHelper mEqualizerTouchHelper;
    private float[] mCenters;
    private ArrayList<TextView> mTextValues;
    private ArrayList<SeekBar> mSeeks;
    private ArrayList<Integer> mHfxs;
    private boolean mSorting = false;
    private boolean mContinue = true;
    private boolean mAddTemplate;
    private final Handler mHandler;
    private int mLongClick = 0;

    private RelativeLayout mRelativeTemplateHeader;
    private ScrollView mScrollView;
    private TextView mTextTemplateName, mTextFinishSortEqualizer;
    private View mViewSepEqualizer, mViewSepEqualizerCustomize;
    private Button mBtnEqualizerOff, mBtnBackCustomize, mBtnFinishCustomize, mBtnEqualizerRandom, mBtnResetEqualizer, mBtnEqualizerSaveAs;
    private AnimationButton mBtnEqualizerMenu, mBtnAddEqualizerTemplate;
    private ImageView mImgBackEqualizer;

    public ArrayList<SeekBar> getArSeek() { return mSeeks; }
    public float[] getArCenters() { return mCenters; }
    ArrayList<EqualizerItem> getArEqualizerItems() {
        return mEqualizerItems;
    }
    private void setArEqualizerItems(ArrayList<EqualizerItem> arLists) {
        mEqualizerItems = arLists;
        mEqualizersAdapter.changeItems(mEqualizerItems);
    }
    public ItemTouchHelper getEqualizerTouchHelper() { return mEqualizerTouchHelper; }
    public boolean isSelectedItem(int nItem) {
        if(nItem >= mEqualizerItems.size()) return false;
        EqualizerItem item = mEqualizerItems.get(nItem);
        return item.isSelected();
    }
    public boolean isSorting() { return mSorting; }
    public EqualizersAdapter getEqualizersAdapter() { return mEqualizersAdapter; }

    public EqualizerFragment()
    {
        mHfxs = new ArrayList<>();
        mEqualizerItems = new ArrayList<>();
        mHandler = new Handler();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_equalizer, container, false);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEqualizersAdapter = new EqualizersAdapter(mActivity, mEqualizerItems);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerEqualizers = mActivity.findViewById(R.id.recyclerEqualizers);
        mTextFinishSortEqualizer = mActivity.findViewById(R.id.textFinishSortEqualizer);
        mRelativeTemplateHeader = mActivity.findViewById(R.id.relativeTemplateHeader);
        mTextTemplateName = mActivity.findViewById(R.id.textTemplateName);
        mViewSepEqualizer = mActivity.findViewById(R.id.viewSepEqualizer);
        mViewSepEqualizerCustomize = mActivity.findViewById(R.id.viewSepEqualizerCustomize);
        mBtnEqualizerOff = mActivity.findViewById(R.id.btnEqualizerOff);
        mBtnEqualizerMenu = mActivity.findViewById(R.id.btnEqualizerMenu);
        mBtnAddEqualizerTemplate = mActivity.findViewById(R.id.btnAddEqualizerTemplate);
        mImgBackEqualizer = mActivity.findViewById(R.id.imgBackEqualizer);
        mBtnBackCustomize = mActivity.findViewById(R.id.btnBackCustomize);
        mBtnEqualizerRandom = mActivity.findViewById(R.id.btnEqualizerRandom);
        mBtnFinishCustomize = mActivity.findViewById(R.id.btnFinishCustomize);
        mBtnResetEqualizer = mActivity.findViewById(R.id.btnResetEqualizer);
        mBtnEqualizerSaveAs = mActivity.findViewById(R.id.btnEqualizerSaveAs);
        mScrollView = mActivity.findViewById(R.id.scrollCustomEqualizer);

        mBtnEqualizerOff.setOnClickListener(this);
        mBtnEqualizerRandom.setOnClickListener(this);
        mBtnEqualizerMenu.setOnClickListener(this);
        mBtnAddEqualizerTemplate.setOnClickListener(this);
        mBtnBackCustomize.setOnClickListener(this);
        mBtnFinishCustomize.setOnClickListener(this);
        mBtnResetEqualizer.setOnClickListener(this);
        mBtnEqualizerSaveAs.setOnClickListener(this);

        mCenters = new float[] {20000, 16000, 12500, 10000, 8000, 6300, 5000, 4000, 3150, 2500, 2000, 1600, 1250, 1000, 800, 630, 500, 400, 315, 250, 200, 160, 125, 100, 80, 63, 50, 40, 31.5f, 25, 20};

        mRecyclerEqualizers.setHasFixedSize(false);
        final LinearLayoutManager equalizersManager = new LinearLayoutManager(mActivity);
        mRecyclerEqualizers.setLayoutManager(equalizersManager);
        mRecyclerEqualizers.setAdapter(mEqualizersAdapter);
        ((DefaultItemAnimator) mRecyclerEqualizers.getItemAnimator()).setSupportsChangeAnimations(false);

        mTextValues = new ArrayList<>();
        mTextValues.add((TextView)mActivity.findViewById(R.id.textVolValue));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text20KValue));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text16KValue));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text12_5KValue));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text10KValue));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text8KValue));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text6_3KValue));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text5KValue));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text4KValue));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text3_15KValue));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text2_5KValue));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text2KValue));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text1_6KValue));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text1_25KValue));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text1KValue));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text800Value));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text630Value));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text500Value));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text400Value));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text315Value));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text250Value));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text200Value));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text160Value));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text125Value));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text100Value));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text80Value));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text63Value));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text50Value));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text40Value));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text31_5Value));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text25Value));
        mTextValues.add((TextView)mActivity.findViewById(R.id.text20Value));
        for(int i = 0; i < mTextValues.size(); i++)
            mTextValues.get(i).setText("0");

        mSeeks = new ArrayList<>();
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seekVol));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek20K));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek16K));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek12_5K));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek10K));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek8K));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek6_3K));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek5K));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek4K));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek3_15K));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek2_5K));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek2K));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek1_6K));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek1_25K));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek1K));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek800));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek630));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek500));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek400));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek315));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek250));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek200));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek160));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek125));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek100));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek80));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek63));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek50));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek40));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek31_5));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek25));
        mSeeks.add((SeekBar)mActivity.findViewById(R.id.seek20));

        for(int i = 0; i < mSeeks.size(); i++)
        {
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
                                    eq.fGain = nLevel;
                                    eq.fCenter = mCenters[j-1];
                                    BASS.BASS_FXSetParameters(mHfxs.get(j-1), eq);
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

        ArrayList<ImageButton> arButtonMinus = new ArrayList<>();
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btnVolMinus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn20KMinus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn16KMinus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn12_5KMinus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn10KMinus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn8KMinus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn6_3KMinus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn5KMinus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn4KMinus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn3_15KMinus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn2_5KMinus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn2KMinus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn1_6KMinus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn1_25KMinus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn1KMinus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn800Minus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn630Minus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn500Minus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn400Minus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn315Minus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn250Minus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn200Minus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn160Minus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn125Minus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn100Minus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn80Minus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn63Minus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn50Minus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn40Minus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn31_5Minus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn25Minus));
        arButtonMinus.add((ImageButton)mActivity.findViewById(R.id.btn20Minus));

        for(int i = 0; i < arButtonMinus.size(); i++)
        {
            final int j = i;
            arButtonMinus.get(i).setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    int nProgress = minusValue(mSeeks.get(j));
                    if(j == 0) setVol(nProgress);
                    else setEQ(j, nProgress);
                }
            });
            arButtonMinus.get(i).setOnLongClickListener(new View.OnLongClickListener()
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
            arButtonMinus.get(i).setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                        mContinue = false;
                    return false;
                }
            });
        }

        ArrayList<ImageButton> arButtonPlus = new ArrayList<>();
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btnVolPlus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn20KPlus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn16KPlus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn12_5KPlus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn10KPlus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn8KPlus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn6_3KPlus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn5KPlus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn4KPlus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn3_15KPlus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn2_5KPlus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn2KPlus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn1_6KPlus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn1_25KPlus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn1KPlus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn800Plus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn630Plus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn500Plus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn400Plus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn315Plus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn250Plus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn200Plus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn160Plus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn125Plus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn100Plus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn80Plus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn63Plus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn50Plus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn40Plus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn31_5Plus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn25Plus));
        arButtonPlus.add((ImageButton)mActivity.findViewById(R.id.btn20Plus));

        for(int i = 0; i < arButtonPlus.size(); i++)
        {
            final int j = i;
            arButtonPlus.get(i).setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    int nProgress = plusValue(mSeeks.get(j));
                    if(j == 0) setVol(nProgress);
                    else setEQ(j, nProgress);
                }
            });
            arButtonPlus.get(i).setOnLongClickListener(new View.OnLongClickListener()
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
            arButtonPlus.get(i).setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                        mContinue = false;
                    return false;
                }
            });
        }

        loadData();
    }

    private final Runnable repeatMinusValue = new Runnable()
    {
        @Override
        public void run()
        {
            if(!mContinue)
                return;
            int nProgress = minusValue(mSeeks.get(mLongClick));
            if(mLongClick == 0) setVol(nProgress);
            else setEQ(mLongClick, nProgress);
            mHandler.postDelayed(this, 100);
        }
    };

    private final Runnable repeatPlusValue = new Runnable()
    {
        @Override
        public void run()
        {
            if(!mContinue)
                return;
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
                mViewSepEqualizer.setVisibility(View.VISIBLE);
                mBtnAddEqualizerTemplate.setAlpha(1.0f);
                mTextFinishSortEqualizer.setVisibility(View.GONE);
                mSorting = false;
                mEqualizersAdapter.notifyDataSetChanged();
                mEqualizerTouchHelper.attachToRecyclerView(null);
                break;

            case R.id.btnEqualizerOff:
                mBtnEqualizerOff.setSelected(true);
                for(int i = 0; i < mEqualizerItems.size(); i++)
                    mEqualizerItems.get(i).setSelected(false);
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
                mBtnBackCustomize.setPadding((int)(16 * mActivity.getDensity()), mBtnBackCustomize.getPaddingTop(), mBtnBackCustomize.getPaddingRight(), mBtnBackCustomize.getPaddingBottom());
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

                EqualizerItem item = null;
                int nItem = 0;
                for (; nItem < mEqualizerItems.size(); nItem++) {
                    item = mEqualizerItems.get(nItem);
                    if (item.isSelected()) break;
                }
                if (nItem < mEqualizerItems.size()) setEQ(nItem);
                else resetEQ();
            }
                break;

            case R.id.btnFinishCustomize:
                if(mAddTemplate) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setTitle(R.string.addTemplate);
                    LinearLayout linearLayout = new LinearLayout(mActivity);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    final EditText editPreset = new EditText(mActivity);
                    editPreset.setHint(R.string.templateName);
                    editPreset.setHintTextColor(Color.argb(255, 192, 192, 192));
                    editPreset.setText(R.string.newTemplate);
                    linearLayout.addView(editPreset);
                    builder.setView(linearLayout);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ArrayList<Integer> arPresets = new ArrayList<>();
                            for (int i = 0; i < 32; i++) {
                                arPresets.add(Integer.parseInt((String) mTextValues.get(i).getText()));
                            }
                            mEqualizerItems.add(new EqualizerItem(editPreset.getText().toString(), arPresets));
                            mEqualizersAdapter.notifyItemInserted(mEqualizerItems.size() - 1);
                            saveData();

                            mRelativeTemplateHeader.setVisibility(View.INVISIBLE);
                            mViewSepEqualizerCustomize.setVisibility(View.INVISIBLE);
                            mBtnEqualizerRandom.setVisibility(View.INVISIBLE);
                            mScrollView.setVisibility(View.INVISIBLE);
                            mBtnEqualizerOff.setVisibility(View.VISIBLE);
                            mRecyclerEqualizers.setVisibility(View.VISIBLE);
                            mBtnAddEqualizerTemplate.setAlpha(1.0f);

                            for(int i = 0; i < mEqualizerItems.size()-1; i++)
                                mEqualizerItems.get(i).setSelected(false);
                            mEqualizerItems.get(mEqualizerItems.size()-1).setSelected(true);
                            mEqualizersAdapter.notifyDataSetChanged();
                            mRecyclerEqualizers.scrollToPosition(mEqualizerItems.size()-1);
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
                    EqualizerItem item = null;
                    int nItem = 0;
                    for (; nItem < mEqualizerItems.size(); nItem++) {
                        item = mEqualizerItems.get(nItem);
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
                EqualizerItem item = null;
                int nItem = 0;
                for (; nItem < mEqualizerItems.size(); nItem++) {
                    item = mEqualizerItems.get(nItem);
                    if (item.isSelected()) break;
                }
                if (nItem < mEqualizerItems.size()) setEQ(nItem);
                else resetEQ();
                break;

            case R.id.btnEqualizerSaveAs:
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(R.string.addTemplate);
                LinearLayout linearLayout = new LinearLayout(mActivity);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                final EditText editPreset = new EditText(mActivity);
                editPreset.setHint(R.string.templateName);
                editPreset.setHintTextColor(Color.argb(255, 192, 192, 192));
                editPreset.setText(R.string.newTemplate);
                linearLayout.addView(editPreset);
                builder.setView(linearLayout);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ArrayList<Integer> arPresets = new ArrayList<>();
                        for (int i = 0; i < 32; i++) {
                            arPresets.add(Integer.parseInt((String) mTextValues.get(i).getText()));
                        }
                        mEqualizerItems.add(new EqualizerItem(editPreset.getText().toString(), arPresets));
                        mEqualizersAdapter.notifyItemInserted(mEqualizerItems.size() - 1);
                        saveData();

                        mRelativeTemplateHeader.setVisibility(View.INVISIBLE);
                        mViewSepEqualizerCustomize.setVisibility(View.INVISIBLE);
                        mBtnEqualizerRandom.setVisibility(View.INVISIBLE);
                        mScrollView.setVisibility(View.INVISIBLE);
                        mBtnEqualizerOff.setVisibility(View.VISIBLE);
                        mRecyclerEqualizers.setVisibility(View.VISIBLE);
                        mBtnAddEqualizerTemplate.setAlpha(1.0f);

                        for(int i = 0; i < mEqualizerItems.size()-1; i++)
                            mEqualizerItems.get(i).setSelected(false);
                        mEqualizerItems.get(mEqualizerItems.size()-1).setSelected(true);
                        mEqualizersAdapter.notifyDataSetChanged();
                        mRecyclerEqualizers.scrollToPosition(mEqualizerItems.size()-1);
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
                break;

            default:
                break;
        }
    }

    private int minusValue(SeekBar seek)
    {
        int nProgress = seek.getProgress();
        nProgress -= 1;
        if(nProgress < 0) nProgress = 0;
        return (nProgress - 30);
    }

    private int plusValue(SeekBar seek)
    {
        int nProgress = seek.getProgress();
        nProgress += 1;
        if(nProgress > 60) nProgress = 60;
        return (nProgress - 30);
    }

    public void onEqualizerItemClick(int nEqualizer)
    {
        EqualizerItem itemSelected = mEqualizerItems.get(nEqualizer);
        boolean bSelected = !itemSelected.isSelected();
        mBtnEqualizerOff.setSelected(!bSelected);
        int nSelected = -1;
        if(bSelected) {
            nSelected = nEqualizer;
            setEQ(nSelected);
        }
        else resetEQ();
        for (int i = 0; i < mEqualizerItems.size(); i++) {
            EqualizerItem item = mEqualizerItems.get(i);
            if (i == nSelected) item.setSelected(true);
            else item.setSelected(false);
        }
        mEqualizersAdapter.notifyDataSetChanged();
    }

    public void onEqualizerDetailClick(int nEqualizer)
    {
        mAddTemplate = false;

        if(!isSelectedItem(nEqualizer)) onEqualizerItemClick(nEqualizer);

        EqualizerItem item = mEqualizerItems.get(nEqualizer);
        mTextTemplateName.setText(item.getEqualizerName());

        mBtnBackCustomize.setText(R.string.back);
        mBtnFinishCustomize.setText(R.string.done);

        mRelativeTemplateHeader.setVisibility(View.VISIBLE);
        mViewSepEqualizerCustomize.setVisibility(View.VISIBLE);
        mBtnEqualizerRandom.setVisibility(View.VISIBLE);
        mScrollView.setVisibility(View.VISIBLE);
        mImgBackEqualizer.setVisibility(View.VISIBLE);
        mBtnBackCustomize.setPadding((int)(32 * mActivity.getDensity()), mBtnBackCustomize.getPaddingTop(), mBtnBackCustomize.getPaddingRight(), mBtnBackCustomize.getPaddingBottom());
        mBtnEqualizerOff.setVisibility(View.INVISIBLE);
        mRecyclerEqualizers.setVisibility(View.INVISIBLE);
        mBtnEqualizerSaveAs.setVisibility(View.VISIBLE);
        mBtnAddEqualizerTemplate.setAlpha(0.0f);
    }

    private void loadData()
    {
        SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        ArrayList<EqualizerItem> equalizerItems = gson.fromJson(preferences.getString("arEqualizerItems",""), new TypeToken<ArrayList<EqualizerItem>>(){}.getType());
        if(equalizerItems != null) setArEqualizerItems(equalizerItems);
        else resetPresets();
        mBtnEqualizerOff.setSelected(true);
        for(int i = 0; i < mEqualizerItems.size(); i++)
            mEqualizerItems.get(i).setSelected(false);
        if(mEqualizerItems.get(0).getEqualizerName().equals(getString(R.string.off)))
            removeItem(0);
        if(mEqualizerItems.get(0).getEqualizerName().equals(getString(R.string.random)))
            removeItem(0);
    }

    private void saveData()
    {
        SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        preferences.edit().putString("arEqualizerItems", gson.toJson(mEqualizerItems)).apply();
    }

    private void resetPresets()
    {
        if(mEqualizerItems.size() > 0) mEqualizerItems.clear();
        mEqualizerItems.add(new EqualizerItem(getString(R.string.transcribeBass), new ArrayList<>(Arrays.asList(  0,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.VocalBoost), new ArrayList<>(Arrays.asList(  0,-30,-20,-12, -7, -4, -3, -2, -1,  0,  0,  0,  0,  0, -1, -2, -3, -4, -7,-12,-20,-24,-27,-28,-29,-30,-30,-30,-30,-30,-30,-30))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.VocalReducer), new ArrayList<>(Arrays.asList(  0,  0, -5, -8,-10,-12,-13,-14,-14,-15,-15,-15,-15,-15,-14,-14,-13,-12,-11, -8, -5, -3, -2, -1,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleBoostUltraStrong), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,-15,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleBoostStrongest), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -7,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleBoostStrong), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleBoostMiddle), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -4, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleBoostWeak), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleBoostWeakest), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleBoostUltraStrong), new ArrayList<>(Arrays.asList(  0,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,-15,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleBoostStrongest), new ArrayList<>(Arrays.asList(  0,-15,-15,-15,-15,-15,-15,-15,-15,-15, -7,  0,  0,  0,  0,  0,  0,  0,  0,  0, -7,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleBoostStrong), new ArrayList<>(Arrays.asList(  0,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleBoostMiddle), new ArrayList<>(Arrays.asList(  0, -9, -9, -9, -9, -9, -9, -9, -9, -9, -4,  0,  0,  0,  0,  0,  0,  0,  0,  0, -4, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleBoostWeak), new ArrayList<>(Arrays.asList(  0, -6, -6, -6, -6, -6, -6, -6, -6, -6, -3,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleBoostWeakest), new ArrayList<>(Arrays.asList(  0, -3, -3, -3, -3, -3, -3, -3, -3, -3, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.BassBoostUltraStrong), new ArrayList<>(Arrays.asList(  0,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.BassBoostStrongest), new ArrayList<>(Arrays.asList(  0,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15, -7,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.BassBoostStrong), new ArrayList<>(Arrays.asList(  0,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.BassBoostMiddle), new ArrayList<>(Arrays.asList(  0, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -4,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.BassBoostWeak), new ArrayList<>(Arrays.asList(  0, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.BassBoostWeakest), new ArrayList<>(Arrays.asList(  0, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleCutUltraStrong), new ArrayList<>(Arrays.asList(  0,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleCutStrongest), new ArrayList<>(Arrays.asList(  0,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15, -8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleCutStrong), new ArrayList<>(Arrays.asList(  0,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleCutMiddle), new ArrayList<>(Arrays.asList(  0, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleCutWeak), new ArrayList<>(Arrays.asList(  0, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleCutWeakest), new ArrayList<>(Arrays.asList(  0, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleCutUltraStrong), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,-15,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleCutStrongest), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -8,-15,-15,-15,-15,-15,-15,-15,-15,-15, -8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleCutStrong), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleCutMiddle), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleCutWeak), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3, -6, -6, -6, -6, -6, -6, -6, -6, -6, -3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleCutWeakest), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -2, -3, -3, -3, -3, -3, -3, -3, -3, -3, -2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.BassCutUltraStrong), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,-15,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.BassCutStrongest), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -8,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.BassCutStrong), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.BassCutMiddle), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.BassCutWeak), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.BassCutWeakest), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -2, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.Pop), new ArrayList<>(Arrays.asList(  0, -6, -6, -6, -5, -5, -5, -5, -4, -3, -3, -2, -1, -1,  0,  0,  0,  0, -1, -1, -2, -3, -3, -4, -5, -5, -5, -5, -6, -6, -6, -6))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.Rock), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -2, -3, -4, -5, -6, -5, -4, -3, -2, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.Jazz), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -2, -3, -4, -5, -6, -6, -6, -6, -3, -2, -1, -2, -2, -2, -2, -2, -1, -1, -1,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.Electronic), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0, -1, -2, -3, -4, -3, -2, -1, -3, -5, -7, -3, -2, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        mEqualizerItems.add(new EqualizerItem(getString(R.string.Acoustic), new ArrayList<>(Arrays.asList(  0, -2, -2, -2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, -3, -3, -3, -3, -3, -4, -4, -3, -2, -1, -1, -1,  0,  0,  0,  0,  0,  0))));
        saveData();
        mEqualizersAdapter.notifyDataSetChanged();

        mBtnEqualizerOff.setSelected(true);
        resetEQ();
    }

    public void setEQ()
    {
        for(int i = 0; i < 32; i++)
        {
            int nLevel = mSeeks.get(i).getProgress() - 30;
            if(i == 0)
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
            }
            else
            {
                if(MainActivity.sStream != 0)
                {
                    BASS_FX.BASS_BFX_PEAKEQ eq = new BASS_FX.BASS_BFX_PEAKEQ();
                    eq.fBandwidth = 0.7f;
                    eq.fQ = 0.0f;
                    eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
                    eq.fGain = nLevel;
                    eq.fCenter = mCenters[i-1];
                    BASS.BASS_FXSetParameters(mHfxs.get(i-1), eq);
                }
            }
        }
    }

    public void setEQ(int row)
    {
        if(mEqualizerItems.get(row).getEqualizerName().equals(getString(R.string.random))) {
            setEQRandom();
            return;
        }
        else if(mEqualizerItems.get(row).getEqualizerName().equals(getString(R.string.transcribeBassOctave)))
            mActivity.controlFragment.setPitch(12.0f);

        for(int i = 0; i < 32; i++)
        {
            int nLevel = mEqualizerItems.get(row).getArPresets().get(i);
            if(i == 0)
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

                SeekBar seekBar = mSeeks.get(i);
                seekBar.setProgress(nLevel + 30);
                TextView textView = mTextValues.get(i);
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
                    eq.fGain = nLevel;
                    eq.fCenter = mCenters[i-1];
                    BASS.BASS_FXSetParameters(mHfxs.get(i-1), eq);
                }

                SeekBar seekBar = mSeeks.get(i);
                seekBar.setProgress(nLevel + 30);
                TextView textView = mTextValues.get(i);
                textView.setText(String.valueOf(nLevel));
            }
        }
        mActivity.playlistFragment.updateSavingEffect();
    }

    public void setEQRandom()
    {
        int nLevel = 0;
        float fLevel = 1.0f;
        if(MainActivity.sStream != 0)
        {
            BASS_FX.BASS_BFX_VOLUME vol = new BASS_FX.BASS_BFX_VOLUME();
            vol.lChannel = 0;
            vol.fVolume = fLevel;
            BASS.BASS_FXSetParameters(MainActivity.sFxVol, vol);
        }

        SeekBar seekBar = mSeeks.get(0);
        seekBar.setProgress(nLevel + 30);
        TextView textView = mTextValues.get(0);
        textView.setText(String.valueOf(nLevel));

        for(int i = 0; i < 31; i++)
        {
            Random random = new Random();
            nLevel = random.nextInt(30) - 20;
            if(MainActivity.sStream != 0)
            {
                BASS_FX.BASS_BFX_PEAKEQ eq = new BASS_FX.BASS_BFX_PEAKEQ();
                eq.fBandwidth = 0.7f;
                eq.fQ = 0.0f;
                eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
                eq.fGain = nLevel;
                eq.fCenter = mCenters[i];
                BASS.BASS_FXSetParameters(mHfxs.get(i), eq);
            }

            seekBar = mSeeks.get(i + 1);
            seekBar.setProgress(nLevel + 30);
            textView = mTextValues.get(i + 1);
            textView.setText(String.valueOf(nLevel));
        }
        mActivity.playlistFragment.updateSavingEffect();
    }

    public void resetEQ()
    {
        int nLevel = 0;
        float fLevel = 1.0f;
        if(MainActivity.sStream != 0)
        {
            BASS_FX.BASS_BFX_VOLUME vol = new BASS_FX.BASS_BFX_VOLUME();
            vol.lChannel = 0;
            vol.fVolume = fLevel;
            BASS.BASS_FXSetParameters(MainActivity.sFxVol, vol);
        }

        SeekBar seekBar = mSeeks.get(0);
        seekBar.setProgress(nLevel + 30);
        TextView textView = mTextValues.get(0);
        textView.setText(String.valueOf(nLevel));

        for(int i = 0; i < 31; i++)
        {
            if(MainActivity.sStream != 0)
            {
                BASS_FX.BASS_BFX_PEAKEQ eq = new BASS_FX.BASS_BFX_PEAKEQ();
                eq.fBandwidth = 0.7f;
                eq.fQ = 0.0f;
                eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
                eq.fGain = 0;
                eq.fCenter = mCenters[i];
                BASS.BASS_FXSetParameters(mHfxs.get(i), eq);
            }

            seekBar = mSeeks.get(i + 1);
            seekBar.setProgress(nLevel + 30);
            textView = mTextValues.get(i + 1);
            textView.setText(String.valueOf(nLevel));
        }
        mActivity.playlistFragment.updateSavingEffect();
    }

    public void setVol(int nLevel)
    {
        setVol(nLevel, true);
    }

    public void setVol(int nLevel, boolean bSave)
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

        SeekBar seekBar = mSeeks.get(0);
        seekBar.setProgress(nLevel + 30);
        TextView textView = mTextValues.get(0);
        textView.setText(String.valueOf(nLevel));

        if(bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    public void setEQ(int i, int nLevel)
    {
        setEQ(i, nLevel, true);
    }

    public void setEQ(int i, int nLevel, boolean bSave)
    {
        if(MainActivity.sStream != 0)
        {
            BASS_FX.BASS_BFX_PEAKEQ eq = new BASS_FX.BASS_BFX_PEAKEQ();
            eq.fBandwidth = 0.7f;
            eq.fQ = 0.0f;
            eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
            eq.fGain = nLevel;
            eq.fCenter = mCenters[i-1];
            BASS.BASS_FXSetParameters(mHfxs.get(i-1), eq);
        }

        SeekBar seekBar = mSeeks.get(i);
        seekBar.setProgress(nLevel + 30);
        TextView textView = mTextValues.get(i);
        textView.setText(String.valueOf(nLevel));

        if (bSave) mActivity.playlistFragment.updateSavingEffect();
    }

    void setArHFX(ArrayList<Integer> hfxs) {
        mHfxs = new ArrayList<>(hfxs);
    }

    public void showEqualizerMenu() {
        final BottomMenu menu = new BottomMenu(mActivity);
        menu.setTitle(getString(R.string.equalizerTemplate));
        menu.addMenu(getString(R.string.sortTemplate), R.drawable.ic_actionsheet_sort, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                mRecyclerEqualizers.setPadding(0, 0, 0, (int)(64 * mActivity.getDensity()));
                mBtnEqualizerOff.setVisibility(View.GONE);
                mViewSepEqualizer.setVisibility(View.GONE);
                mBtnAddEqualizerTemplate.setAlpha(0.0f);
                mTextFinishSortEqualizer.setVisibility(View.VISIBLE);
                mSorting = true;
                mEqualizersAdapter.notifyDataSetChanged();

                mEqualizerTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
                    @Override
                    public boolean onMove(RecyclerView mRecyclerEqualizers, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        final int fromPos = viewHolder.getAdapterPosition();
                        final int toPos = target.getAdapterPosition();

                        EqualizerItem itemTemp = mEqualizerItems.get(fromPos);
                        mEqualizerItems.remove(fromPos);
                        mEqualizerItems.add(toPos, itemTemp);

                        mEqualizersAdapter.notifyItemMoved(fromPos, toPos);

                        return true;
                    }

                    @Override
                    public void clearView(RecyclerView recyclerSongs, RecyclerView.ViewHolder viewHolder) {
                        super.clearView(recyclerSongs, viewHolder);

                        mEqualizersAdapter.notifyDataSetChanged();
                        saveData();
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    }
                });
                mEqualizerTouchHelper.attachToRecyclerView(mRecyclerEqualizers);
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
        menu.setTitle(mEqualizerItems.get(nItem).getEqualizerName());
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
                editPreset.setText(mEqualizerItems.get(nItem).getEqualizerName());
                linearLayout.addView(editPreset);
                builder.setView(linearLayout);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mEqualizerItems.get(nItem).setEqualizerName(editPreset.getText().toString());
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
                EqualizerItem item = mEqualizerItems.get(nItem);
                ArrayList<Integer> arPresets = new ArrayList<>();
                for (int i = 0; i < 32; i++) {
                    arPresets.add(item.getArPresets().get(i));
                }
                mEqualizerItems.add(nItem+1, new EqualizerItem(item.getEqualizerName(), arPresets));
                mEqualizersAdapter.notifyItemInserted(nItem+1);
                saveData();
                mRecyclerEqualizers.scrollToPosition(nItem+1);
            }
        });
        menu.addDestructiveMenu(getString(R.string.delete), R.drawable.ic_actionsheet_delete, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(mEqualizerItems.get(nItem).getEqualizerName());
                builder.setMessage(R.string.askDeleteTemplate);
                builder.setPositiveButton(R.string.decideNot, null);
                builder.setNegativeButton(R.string.doDelete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        removeItem(nItem);
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

    private void removeItem(int nItem)
    {
        if(isSelectedItem(nItem)) {
            mBtnEqualizerOff.setSelected(true);
            resetEQ();
        }
        mEqualizerItems.remove(nItem);
        mEqualizersAdapter.notifyItemRemoved(nItem);
        saveData();
    }
}
