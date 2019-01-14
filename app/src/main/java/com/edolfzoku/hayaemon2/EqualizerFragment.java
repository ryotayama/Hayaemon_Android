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

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class EqualizerFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    private MainActivity activity = null;
    private RecyclerView recyclerEqualizers;
    private EqualizersAdapter equalizersAdapter;
    private ArrayList<EqualizerItem> arEqualizerItems;
    private ItemTouchHelper equalizerTouchHelper;
    private float[] arCenters;
    private ArrayList<TextView> arTextValue;
    private ArrayList<SeekBar> arSeek;
    private int[] arHFX;
    private int nLastChecked = 0;
    private boolean bSorting = false;

    public ArrayList<SeekBar> getArSeek() { return arSeek; }
    public float[] getArCenters() { return arCenters; }
    public int[] getArHFX() { return arHFX; }
    ArrayList<EqualizerItem> getArEqualizerItems() {
        return arEqualizerItems;
    }
    public void setArEqualizerItems(ArrayList<EqualizerItem> arLists) {
        arEqualizerItems = arLists;
        equalizersAdapter.changeItems(arEqualizerItems);
    }
    public ItemTouchHelper getEqualizerTouchHelper() { return equalizerTouchHelper; }
    public boolean isSelectedItem(int nItem) {
        if(nItem >= arEqualizerItems.size()) return false;
        EqualizerItem item = arEqualizerItems.get(nItem);
        return item.isSelected();
    }
    public boolean isSorting() { return bSorting; }
    public EqualizersAdapter getEqualizersAdapter() { return equalizersAdapter; }

    public EqualizerFragment()
    {
        arHFX = null;
        arEqualizerItems = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_equalizer, container, false);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context != null && context instanceof MainActivity) {
            activity = (MainActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        activity = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        equalizersAdapter = new EqualizersAdapter(activity, R.layout.equalizer_item, arEqualizerItems);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TabLayout tabLayout = (TabLayout)getActivity().findViewById(R.id.tabEqualizer);
        tabLayout.addTab(tabLayout.newTab().setText("プリセット"));

        tabLayout.addTab(tabLayout.newTab().setText("カスタマイズ"));

        final ScrollView scrollView = (ScrollView)getActivity().findViewById(R.id.scrollCustomEqualizer);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getText()=="プリセット") {
                    recyclerEqualizers.setVisibility(View.VISIBLE);
                }
                else{
                    scrollView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if(tab.getText()=="プリセット"){
                    recyclerEqualizers.setVisibility(View.INVISIBLE);
                }
                else{
                    scrollView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        arCenters = new float[] {20000, 16000, 12500, 10000, 8000, 6300, 5000, 4000, 3150, 2500, 2000, 1600, 1250, 1000, 800, 630, 500, 400, 315, 250, 200, 160, 125, 100, 80, 63, 50, 40, 31.5f, 25, 20};

        loadData();

        MainActivity activity = (MainActivity)getActivity();
        recyclerEqualizers = (RecyclerView)activity.findViewById(R.id.recyclerEqualizers);
        recyclerEqualizers.setHasFixedSize(false);
        final LinearLayoutManager equalizersManager = new LinearLayoutManager(activity);
        recyclerEqualizers.setLayoutManager(equalizersManager);
        recyclerEqualizers.setAdapter(equalizersAdapter);

        arTextValue = new ArrayList<TextView>();
        arTextValue.add((TextView)getActivity().findViewById(R.id.textVolValue));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text20KValue));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text16KValue));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text12_5KValue));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text10KValue));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text8KValue));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text6_3KValue));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text5KValue));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text4KValue));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text3_15KValue));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text2_5KValue));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text2KValue));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text1_6KValue));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text1_25KValue));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text1KValue));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text800Value));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text630Value));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text500Value));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text400Value));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text315Value));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text250Value));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text200Value));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text160Value));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text125Value));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text100Value));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text80Value));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text63Value));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text50Value));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text40Value));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text31_5Value));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text25Value));
        arTextValue.add((TextView)getActivity().findViewById(R.id.text20Value));

        arSeek = new ArrayList<SeekBar>();
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seekVol));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek20K));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek16K));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek12_5K));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek10K));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek8K));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek6_3K));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek5K));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek4K));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek3_15K));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek2_5K));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek2K));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek1_6K));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek1_25K));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek1K));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek800));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek630));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek500));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek400));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek315));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek250));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek200));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek160));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek125));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek100));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek80));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek63));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek50));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek40));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek31_5));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek25));
        arSeek.add((SeekBar)getActivity().findViewById(R.id.seek20));

        for(int i = 0; i < arSeek.size(); i++)
        {
            final int j = i;
            arSeek.get(i).setOnSeekBarChangeListener(
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
                                if(MainActivity.hStream != 0)
                                {
                                    BASS_FX.BASS_BFX_VOLUME vol = new BASS_FX.BASS_BFX_VOLUME();
                                    vol.lChannel = 0;
                                    vol.fVolume = fLevel;
                                    BASS.BASS_FXSetParameters(MainActivity.hFxVol, vol);
                                }

                                TextView textView = arTextValue.get(j);
                                textView.setText(String.valueOf(nLevel));
                            }
                            else
                            {
                                if(MainActivity.hStream != 0)
                                {
                                    BASS_FX.BASS_BFX_PEAKEQ eq = new BASS_FX.BASS_BFX_PEAKEQ();
                                    eq.fBandwidth = 0;
                                    eq.fQ = 0.7f;
                                    eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
                                    eq.fGain = nLevel;
                                    eq.fCenter = arCenters[j-1];
                                    BASS.BASS_FXSetParameters(arHFX[j-1], eq);
                                }

                                TextView textView = arTextValue.get(j);
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

        Button btnAddEqualizer = (Button) activity.findViewById(R.id.btnAddEqualizer);
        btnAddEqualizer.setOnClickListener(this);

        TextView textFinishSortEqualizer = (TextView) activity.findViewById(R.id.textFinishSortEqualizer);
        textFinishSortEqualizer.setOnClickListener(this);

        TextView textVolMinus = (TextView) activity.findViewById(R.id.textVolMinus);
        textVolMinus.setOnClickListener(this);
        textVolMinus.setOnLongClickListener(this);

        TextView text20KMinus = (TextView) activity.findViewById(R.id.text20KMinus);
        text20KMinus.setOnClickListener(this);
        text20KMinus.setOnLongClickListener(this);

        TextView text16KMinus = (TextView) activity.findViewById(R.id.text16KMinus);
        text16KMinus.setOnClickListener(this);
        text16KMinus.setOnLongClickListener(this);

        TextView text12_5KMinus = (TextView) activity.findViewById(R.id.text12_5KMinus);
        text12_5KMinus.setOnClickListener(this);
        text12_5KMinus.setOnLongClickListener(this);

        TextView text10KMinus = (TextView) activity.findViewById(R.id.text10KMinus);
        text10KMinus.setOnClickListener(this);
        text10KMinus.setOnLongClickListener(this);

        TextView text8KMinus = (TextView) activity.findViewById(R.id.text8KMinus);
        text8KMinus.setOnClickListener(this);
        text8KMinus.setOnLongClickListener(this);

        TextView text6_3KMinus = (TextView) activity.findViewById(R.id.text6_3KMinus);
        text6_3KMinus.setOnClickListener(this);
        text6_3KMinus.setOnLongClickListener(this);

        TextView text5KMinus = (TextView) activity.findViewById(R.id.text5KMinus);
        text5KMinus.setOnClickListener(this);
        text5KMinus.setOnLongClickListener(this);

        TextView text4KMinus = (TextView) activity.findViewById(R.id.text4KMinus);
        text4KMinus.setOnClickListener(this);
        text4KMinus.setOnLongClickListener(this);

        TextView text3_15KMinus = (TextView) activity.findViewById(R.id.text3_15KMinus);
        text3_15KMinus.setOnClickListener(this);
        text3_15KMinus.setOnLongClickListener(this);

        TextView text2_5KMinus = (TextView) activity.findViewById(R.id.text2_5KMinus);
        text2_5KMinus.setOnClickListener(this);
        text2_5KMinus.setOnLongClickListener(this);

        TextView text2KMinus = (TextView) activity.findViewById(R.id.text2KMinus);
        text2KMinus.setOnClickListener(this);
        text2KMinus.setOnLongClickListener(this);

        TextView text1_6KMinus = (TextView) activity.findViewById(R.id.text1_6KMinus);
        text1_6KMinus.setOnClickListener(this);
        text1_6KMinus.setOnLongClickListener(this);

        TextView text1_25KMinus = (TextView) activity.findViewById(R.id.text1_25KMinus);
        text1_25KMinus.setOnClickListener(this);
        text1_25KMinus.setOnLongClickListener(this);

        TextView text1KMinus = (TextView) activity.findViewById(R.id.text1KMinus);
        text1KMinus.setOnClickListener(this);
        text1KMinus.setOnLongClickListener(this);

        TextView text800Minus = (TextView) activity.findViewById(R.id.text800Minus);
        text800Minus.setOnClickListener(this);
        text800Minus.setOnLongClickListener(this);

        TextView text630Minus = (TextView) activity.findViewById(R.id.text630Minus);
        text630Minus.setOnClickListener(this);
        text630Minus.setOnLongClickListener(this);

        TextView text500Minus = (TextView) activity.findViewById(R.id.text500Minus);
        text500Minus.setOnClickListener(this);
        text500Minus.setOnLongClickListener(this);

        TextView text400Minus = (TextView) activity.findViewById(R.id.text400Minus);
        text400Minus.setOnClickListener(this);
        text400Minus.setOnLongClickListener(this);

        TextView text315Minus = (TextView) activity.findViewById(R.id.text315Minus);
        text315Minus.setOnClickListener(this);
        text315Minus.setOnLongClickListener(this);

        TextView text250Minus = (TextView) activity.findViewById(R.id.text250Minus);
        text250Minus.setOnClickListener(this);
        text250Minus.setOnLongClickListener(this);

        TextView text200Minus = (TextView) activity.findViewById(R.id.text200Minus);
        text200Minus.setOnClickListener(this);
        text200Minus.setOnLongClickListener(this);

        TextView text160Minus = (TextView) activity.findViewById(R.id.text160Minus);
        text160Minus.setOnClickListener(this);
        text160Minus.setOnLongClickListener(this);

        TextView text125Minus = (TextView) activity.findViewById(R.id.text125Minus);
        text125Minus.setOnClickListener(this);
        text125Minus.setOnLongClickListener(this);

        TextView text100Minus = (TextView) activity.findViewById(R.id.text100Minus);
        text100Minus.setOnClickListener(this);
        text100Minus.setOnLongClickListener(this);

        TextView text80Minus = (TextView) activity.findViewById(R.id.text80Minus);
        text80Minus.setOnClickListener(this);
        text80Minus.setOnLongClickListener(this);

        TextView text63Minus = (TextView) activity.findViewById(R.id.text63Minus);
        text63Minus.setOnClickListener(this);
        text63Minus.setOnLongClickListener(this);

        TextView text50Minus = (TextView) activity.findViewById(R.id.text50Minus);
        text50Minus.setOnClickListener(this);
        text50Minus.setOnLongClickListener(this);

        TextView text40Minus = (TextView) activity.findViewById(R.id.text40Minus);
        text40Minus.setOnClickListener(this);
        text40Minus.setOnLongClickListener(this);

        TextView text31_5Minus = (TextView) activity.findViewById(R.id.text31_5Minus);
        text31_5Minus.setOnClickListener(this);
        text31_5Minus.setOnLongClickListener(this);

        TextView text25Minus = (TextView) activity.findViewById(R.id.text25Minus);
        text25Minus.setOnClickListener(this);
        text25Minus.setOnLongClickListener(this);

        TextView text20Minus = (TextView) activity.findViewById(R.id.text20Minus);
        text20Minus.setOnClickListener(this);
        text20Minus.setOnLongClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnAddEqualizer:
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("プリセットの保存");
                LinearLayout linearLayout = new LinearLayout(activity);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                final EditText editPreset = new EditText(activity);
                editPreset.setHint("プリセット名");
                editPreset.setHintTextColor(Color.argb(255, 192, 192, 192));
                editPreset.setText("新規プリセット");
                linearLayout.addView(editPreset);
                builder.setView(linearLayout);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ArrayList<Integer> arPresets = new ArrayList<Integer>();
                        for (int i = 0; i < 32; i++) {
                            arPresets.add(Integer.parseInt((String) arTextValue.get(i).getText()));
                        }
                        arEqualizerItems.add(new EqualizerItem(editPreset.getText().toString(), arPresets));
                        saveData();
                        equalizersAdapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("キャンセル", null);
                builder.show();
                break;

            case R.id.textFinishSortEqualizer:
                recyclerEqualizers.setPadding(0, 0, 0, 0);
                TextView textFinishSortEqualizer = (TextView) activity.findViewById(R.id.textFinishSortEqualizer);
                textFinishSortEqualizer.setVisibility(View.GONE);
                bSorting = false;
                equalizersAdapter.notifyDataSetChanged();
                equalizerTouchHelper.attachToRecyclerView(null);
                break;

            case R.id.textVolMinus:
                int nVolMinusProgress = minusValue(arSeek.get(0));
                setVol(nVolMinusProgress);
                break;

            case R.id.text20KMinus:
                int n20KMinusProgress = minusValue(arSeek.get(1));
                setEQ(1, n20KMinusProgress);
                break;

            case R.id.text16KMinus:
                int n16kMinusProgress = minusValue(arSeek.get(2));
                setEQ(2, n16kMinusProgress);
                break;

            case R.id.text12_5KMinus:
                int n12_5KMinusProgress = minusValue(arSeek.get(3));
                setEQ(3, n12_5KMinusProgress);
                break;

            case R.id.text10KMinus:
                int n10KMinusProgress = minusValue(arSeek.get(4));
                setEQ(4, n10KMinusProgress);
                break;

            case R.id.text8KMinus:
                int n8KMinusProgress = minusValue(arSeek.get(5));
                setEQ(5, n8KMinusProgress);
                break;

            case R.id.text6_3KMinus:
                int n6_3KMinusProgress = minusValue(arSeek.get(6));
                setEQ(6, n6_3KMinusProgress);
                break;

            case R.id.text5KMinus:
                int n5KMinusProgress = minusValue(arSeek.get(7));
                setEQ(7, n5KMinusProgress);
                break;

            case R.id.text4KMinus:
                int n4KMinusProgress = minusValue(arSeek.get(8));
                setEQ(8, n4KMinusProgress);
                break;

            case R.id.text3_15KMinus:
                int n3_15KMinusProgress = minusValue(arSeek.get(9));
                setEQ(9, n3_15KMinusProgress);
                break;

            case R.id.text2_5KMinus:
                int n2_5KMinusProgress = minusValue(arSeek.get(10));
                setEQ(10, n2_5KMinusProgress);
                break;

            case R.id.text2KMinus:
                int n2KMinusProgress = minusValue(arSeek.get(11));
                setEQ(11, n2KMinusProgress);
                break;

            case R.id.text1_6KMinus:
                int n1_6KMinusProgress = minusValue(arSeek.get(12));
                setEQ(12, n1_6KMinusProgress);
                break;

            case R.id.text1_25KMinus:
                int n1_25KMinusProgress = minusValue(arSeek.get(13));
                setEQ(13, n1_25KMinusProgress);
                break;

            case R.id.text1KMinus:
                int n1KMinusProgress = minusValue(arSeek.get(14));
                setEQ(14, n1KMinusProgress);
                break;

            case R.id.text800Minus:
                int n800MinusProgress = minusValue(arSeek.get(15));
                setEQ(15, n800MinusProgress);
                break;

            case R.id.text630Minus:
                int n630MinusProgress = minusValue(arSeek.get(16));
                setEQ(16, n630MinusProgress);
                break;

            case R.id.text500Minus:
                int n500MinusProgress = minusValue(arSeek.get(17));
                setEQ(17, n500MinusProgress);
                break;

            case R.id.text400Minus:
                int n400MinusProgress = minusValue(arSeek.get(18));
                setEQ(18, n400MinusProgress);
                break;

            case R.id.text315Minus:
                int n315MinusProgress = minusValue(arSeek.get(19));
                setEQ(19, n315MinusProgress);
                break;

            case R.id.text250Minus:
                int n250MinusProgress = minusValue(arSeek.get(20));
                setEQ(20, n250MinusProgress);
                break;

            case R.id.text200Minus:
                int n200MinusProgress = minusValue(arSeek.get(21));
                setEQ(21, n200MinusProgress);
                break;

            case R.id.text160Minus:
                int n160MinusProgress = minusValue(arSeek.get(22));
                setEQ(22, n160MinusProgress);
                break;

            case R.id.text125Minus:
                int n125MinusProgress = minusValue(arSeek.get(23));
                setEQ(23, n125MinusProgress);
                break;

            case R.id.text100Minus:
                int n100MinusProgress = minusValue(arSeek.get(24));
                setEQ(24, n100MinusProgress);
                break;

            case R.id.text80Minus:
                int n80MinusProgress = minusValue(arSeek.get(25));
                setEQ(25, n80MinusProgress);
                break;

            case R.id.text63Minus:
                int n63MinusProgress = minusValue(arSeek.get(26));
                setEQ(26, n63MinusProgress);
                break;

            case R.id.text50Minus:
                int n50MinusProgress = minusValue(arSeek.get(27));
                setEQ(27, n50MinusProgress);
                break;

            case R.id.text40Minus:
                int n40MinusProgress = minusValue(arSeek.get(28));
                setEQ(28, n40MinusProgress);
                break;

            case R.id.text31_5Minus:
                int n31_5MinusProgress = minusValue(arSeek.get(29));
                setEQ(29, n31_5MinusProgress);
                break;

            case R.id.text25Minus:
                int n25MinusProgress = minusValue(arSeek.get(30));
                setEQ(30, n25MinusProgress);
                break;

            case R.id.text20Minus:
                int n20MinusProgress = minusValue(arSeek.get(31));
                setEQ(31, n20MinusProgress);
                break;

            case R.id.textVolPlus:
                int nVolPlusProgress = plusValue(arSeek.get(0));
                setVol(nVolPlusProgress);
                break;

            case R.id.text20KPlus:
                int n20KPlusProgress = plusValue(arSeek.get(1));
                setEQ(1, n20KPlusProgress);
                break;

            case R.id.text16KPlus:
                int n16KPlusProgress = plusValue(arSeek.get(2));
                setEQ(2, n16KPlusProgress);
                break;

            case R.id.text12_5KPlus:
                int n12_5KPlusProgress = plusValue(arSeek.get(3));
                setEQ(3, n12_5KPlusProgress);
                break;

            case R.id.text10KPlus:
                int n10KPlusProgress = plusValue(arSeek.get(4));
                setEQ(4, n10KPlusProgress);
                break;

            case R.id.text8KPlus:
                int n8KPlusProgress = plusValue(arSeek.get(5));
                setEQ(5, n8KPlusProgress);
                break;

            case R.id.text6_3KPlus:
                int n6_3KPlusProgress = plusValue(arSeek.get(6));
                setEQ(6, n6_3KPlusProgress);
                break;

            case R.id.text5KPlus:
                int n5KPlusProgress = plusValue(arSeek.get(7));
                setEQ(7, n5KPlusProgress);
                break;

            case R.id.text4KPlus:
                int n4KPlusProgress = plusValue(arSeek.get(8));
                setEQ(8, n4KPlusProgress);
                break;

            case R.id.text3_15KPlus:
                int n3_15KPlusProgress = plusValue(arSeek.get(9));
                setEQ(9, n3_15KPlusProgress);
                break;

            case R.id.text2_5KPlus:
                int n2_5KPlusProgress = plusValue(arSeek.get(10));
                setEQ(10, n2_5KPlusProgress);
                break;

            case R.id.text2KPlus:
                int n2KPlusProgress = plusValue(arSeek.get(11));
                setEQ(11, n2KPlusProgress);
                break;

            case R.id.text1_6KPlus:
                int n1_6KPlusProgress = plusValue(arSeek.get(12));
                setEQ(12, n1_6KPlusProgress);
                break;

            case R.id.text1_25KPlus:
                int n1_25KPlusProgress = plusValue(arSeek.get(13));
                setEQ(13, n1_25KPlusProgress);
                break;

            case R.id.text1KPlus:
                int n1KPlusProgress = plusValue(arSeek.get(14));
                setEQ(14, n1KPlusProgress);
                break;

            case R.id.text800Plus:
                int n800PlusProgress = plusValue(arSeek.get(15));
                setEQ(15, n800PlusProgress);
                break;

            case R.id.text630Plus:
                int n630PlusProgress = plusValue(arSeek.get(16));
                setEQ(16, n630PlusProgress);
                break;

            case R.id.text500Plus:
                int n500PlusProgress = plusValue(arSeek.get(17));
                setEQ(17, n500PlusProgress);
                break;

            case R.id.text400Plus:
                int n400PlusProgress = plusValue(arSeek.get(18));
                setEQ(18, n400PlusProgress);
                break;

            case R.id.text315Plus:
                int n315PlusProgress = plusValue(arSeek.get(19));
                setEQ(19, n315PlusProgress);
                break;

            case R.id.text250Plus:
                int n250PlusProgress = plusValue(arSeek.get(20));
                setEQ(20, n250PlusProgress);
                break;

            case R.id.text200Plus:
                int n200PlusProgress = plusValue(arSeek.get(21));
                setEQ(21, n200PlusProgress);
                break;

            case R.id.text160Plus:
                int n160PlusProgress = plusValue(arSeek.get(22));
                setEQ(22, n160PlusProgress);
                break;

            case R.id.text125Plus:
                int n125PlusProgress = plusValue(arSeek.get(23));
                setEQ(23, n125PlusProgress);
                break;

            case R.id.text100Plus:
                int n100PlusProgress = plusValue(arSeek.get(24));
                setEQ(24, n100PlusProgress);
                break;

            case R.id.text80Plus:
                int n80PlusProgress = plusValue(arSeek.get(25));
                setEQ(25, n80PlusProgress);
                break;

            case R.id.text63Plus:
                int n63PlusProgress = plusValue(arSeek.get(26));
                setEQ(26, n63PlusProgress);
                break;

            case R.id.text50Plus:
                int n50PlusProgress = plusValue(arSeek.get(27));
                setEQ(27, n50PlusProgress);
                break;

            case R.id.text40Plus:
                int n40PlusProgress = plusValue(arSeek.get(28));
                setEQ(28, n40PlusProgress);
                break;

            case R.id.text31_5Plus:
                int n31_5PlusProgress = plusValue(arSeek.get(29));
                setEQ(29, n31_5PlusProgress);
                break;

            case R.id.text25Plus:
                int n25PlusProgress = plusValue(arSeek.get(30));
                setEQ(30, n25PlusProgress);
                break;

            case R.id.text20Plus:
                int n20PlusProgress = plusValue(arSeek.get(31));
                setEQ(31, n20PlusProgress);
                break;

            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View v)
    {
        return false;
    }

    public int minusValue(SeekBar seek)
    {
        int nProgress = seek.getProgress();
        nProgress -= 1;
        if(nProgress < 0) nProgress = 0;
        return (nProgress - 30);
    }

    public int plusValue(SeekBar seek)
    {
        int nProgress = seek.getProgress();
        System.out.println("動作確認; 866 " + nProgress);
        nProgress += 1;
        System.out.println("動作確認; 867 " + nProgress);
        if(nProgress > 60) nProgress = 60;
        System.out.println("動作確認; 868 = " + nProgress);
        System.out.println("動作確認; 869 = " + (nProgress - 30));
        return (nProgress - 30);
    }

    public void onEqualizerItemClick(int nEqualizer)
    {
        for(int i = 0; i < arEqualizerItems.size(); i++) {
            EqualizerItem item = arEqualizerItems.get(i);
            if(i == nEqualizer) item.setSelected(true);
            else item.setSelected(false);
        }
        equalizersAdapter.notifyDataSetChanged();
        setEQ(nEqualizer);
    }

    public void loadData()
    {
        MainActivity activity = (MainActivity)getActivity();
        SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
        ArrayList<EqualizerItem> arEqualizerItems = gson.fromJson(preferences.getString("arEqualizerItems",""), new TypeToken<ArrayList<EqualizerItem>>(){}.getType());
        if(arEqualizerItems != null) setArEqualizerItems(arEqualizerItems);
        else resetPresets();
        for(int i = 0; i < this.arEqualizerItems.size(); i++) {
            this.arEqualizerItems.get(i).setSelected(false);
        }
    }

    public void saveData()
    {
        MainActivity activity = (MainActivity)getActivity();
        SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        preferences.edit().putString("arEqualizerItems", gson.toJson(arEqualizerItems)).commit();
    }

    public void resetPresets()
    {
        if(arEqualizerItems.size() > 0) arEqualizerItems.clear();
        arEqualizerItems.add(new EqualizerItem("フラット",                          new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("ランダム",                          new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("ベースの耳コピ",                    new ArrayList<Integer>(Arrays.asList(  0,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("ボーカル強調",                      new ArrayList<Integer>(Arrays.asList(  0,-30,-20,-12, -7, -4, -3, -2, -1,  0,  0,  0,  0,  0, -1, -2, -3, -4, -7,-12,-20,-24,-27,-28,-29,-30,-30,-30,-30,-30,-30,-30))));
        arEqualizerItems.add(new EqualizerItem("ボーカル抑制",                      new ArrayList<Integer>(Arrays.asList(  0,  0, -5, -8,-10,-12,-13,-14,-14,-15,-15,-15,-15,-15,-14,-14,-13,-12,-11, -8, -5, -3, -2, -1,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("高音強調（超最強）",                new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,-15,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30))));
        arEqualizerItems.add(new EqualizerItem("高音強調（最強）",                  new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -7,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15))));
        arEqualizerItems.add(new EqualizerItem("高音強調（強）",                    new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12))));
        arEqualizerItems.add(new EqualizerItem("高音強調（中）",                    new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -4, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9))));
        arEqualizerItems.add(new EqualizerItem("高音強調（弱）",                    new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6))));
        arEqualizerItems.add(new EqualizerItem("高音強調（最弱）",                  new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3))));
        arEqualizerItems.add(new EqualizerItem("中音強調（超最強）",                new ArrayList<Integer>(Arrays.asList(  0,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,-15,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30))));
        arEqualizerItems.add(new EqualizerItem("中音強調（最強）",                  new ArrayList<Integer>(Arrays.asList(  0,-15,-15,-15,-15,-15,-15,-15,-15,-15, -7,  0,  0,  0,  0,  0,  0,  0,  0,  0, -7,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15))));
        arEqualizerItems.add(new EqualizerItem("中音強調（強）",                    new ArrayList<Integer>(Arrays.asList(  0,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12))));
        arEqualizerItems.add(new EqualizerItem("中音強調（中）",                    new ArrayList<Integer>(Arrays.asList(  0, -9, -9, -9, -9, -9, -9, -9, -9, -9, -4,  0,  0,  0,  0,  0,  0,  0,  0,  0, -4, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9))));
        arEqualizerItems.add(new EqualizerItem("中音強調（弱）",                    new ArrayList<Integer>(Arrays.asList(  0, -6, -6, -6, -6, -6, -6, -6, -6, -6, -3,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6))));
        arEqualizerItems.add(new EqualizerItem("中音強調（最弱）",                  new ArrayList<Integer>(Arrays.asList(  0, -3, -3, -3, -3, -3, -3, -3, -3, -3, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3))));
        arEqualizerItems.add(new EqualizerItem("低音強調（超最強）",                new ArrayList<Integer>(Arrays.asList(  0,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("低音強調（最強）",                  new ArrayList<Integer>(Arrays.asList(  0,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15, -7,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("低音強調（強）",                    new ArrayList<Integer>(Arrays.asList(  0,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("低音強調（中）",                    new ArrayList<Integer>(Arrays.asList(  0, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -4,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("低音強調（弱）",                    new ArrayList<Integer>(Arrays.asList(  0, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("低音強調（最弱）",                  new ArrayList<Integer>(Arrays.asList(  0, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("高音カット（超最強）",              new ArrayList<Integer>(Arrays.asList(  0,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("高音カット（最強）",                new ArrayList<Integer>(Arrays.asList(  0,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15, -8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("高音カット（強）",                  new ArrayList<Integer>(Arrays.asList(  0,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("高音カット（中）",                  new ArrayList<Integer>(Arrays.asList(  0, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("高音カット（弱）",                  new ArrayList<Integer>(Arrays.asList(  0, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("高音カット（最弱）",                new ArrayList<Integer>(Arrays.asList(  0, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("中音カット（超最強）",              new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,-15,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("中音カット（最強）",                new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -8,-15,-15,-15,-15,-15,-15,-15,-15,-15, -8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("中音カット（強）",                  new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("中音カット（中）",                  new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("中音カット（弱）",                  new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3, -6, -6, -6, -6, -6, -6, -6, -6, -6, -3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("中音カット（最弱）",                new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -2, -3, -3, -3, -3, -3, -3, -3, -3, -3, -2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("低音カット（超最強）",              new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,-15,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30))));
        arEqualizerItems.add(new EqualizerItem("低音カット（最強）",                new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -8,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15))));
        arEqualizerItems.add(new EqualizerItem("低音カット（強）",                  new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12))));
        arEqualizerItems.add(new EqualizerItem("低音カット（中）",                  new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9))));
        arEqualizerItems.add(new EqualizerItem("低音カット（弱）",                  new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6))));
        arEqualizerItems.add(new EqualizerItem("低音カット（最弱）",                new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -2, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3))));
        arEqualizerItems.add(new EqualizerItem("Pop",                                new ArrayList<Integer>(Arrays.asList(  0, -6, -6, -6, -5, -5, -5, -5, -4, -3, -3, -2, -1, -1,  0,  0,  0,  0, -1, -1, -2, -3, -3, -4, -5, -5, -5, -5, -6, -6, -6, -6))));
        arEqualizerItems.add(new EqualizerItem("Rock",                               new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -2, -3, -4, -5, -6, -5, -4, -3, -2, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("Jazz",                               new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -2, -3, -4, -5, -6, -6, -6, -6, -3, -2, -1, -2, -2, -2, -2, -2, -1, -1, -1,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("Electronic",                        new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0, -1, -2, -3, -4, -3, -2, -1, -3, -5, -7, -3, -2, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem("Acoustic",                          new ArrayList<Integer>(Arrays.asList(  0, -2, -2, -2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, -3, -3, -3, -3, -3, -4, -4, -3, -2, -1, -1, -1,  0,  0,  0,  0,  0,  0))));
        saveData();
        equalizersAdapter.notifyDataSetChanged();
    }

    public void setEQ()
    {
        for(int i = 0; i < 32; i++)
        {
            int nLevel = arSeek.get(i).getProgress() - 30;
            if(i == 0)
            {
                float fLevel = nLevel;
                if(fLevel == 0) fLevel = 1.0f;
                else if(fLevel < 0) fLevel = (fLevel + 30.0f) / 30.0f;
                else fLevel += 1.0f;
                if(MainActivity.hStream != 0)
                {
                    BASS_FX.BASS_BFX_VOLUME vol = new BASS_FX.BASS_BFX_VOLUME();
                    vol.lChannel = 0;
                    vol.fVolume = fLevel;
                    BASS.BASS_FXSetParameters(MainActivity.hFxVol, vol);
                }
            }
            else
            {
                if(MainActivity.hStream != 0)
                {
                    BASS_FX.BASS_BFX_PEAKEQ eq = new BASS_FX.BASS_BFX_PEAKEQ();
                    eq.fBandwidth = 0;
                    eq.fQ = 0.7f;
                    eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
                    eq.fGain = nLevel;
                    eq.fCenter = arCenters[i-1];
                    BASS.BASS_FXSetParameters(arHFX[i-1], eq);
                }
            }
        }
    }

    public void setEQ(int row)
    {
        MainActivity activity = (MainActivity)getActivity();
        ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(1);
        if(arEqualizerItems.get(nLastChecked).getEqualizerName().equals("ベースの耳コピ（オクターブ上げ）"))
            controlFragment.setPitch(0.0f);
        nLastChecked = row;

        if(arEqualizerItems.get(row).getEqualizerName().equals("ランダム")) {
            setEQRandom();
            return;
        }
        else if(arEqualizerItems.get(row).getEqualizerName().equals("ベースの耳コピ（オクターブ上げ）"))
            controlFragment.setPitch(12.0f);

        for(int i = 0; i < 32; i++)
        {
            int nLevel = arEqualizerItems.get(row).getArPresets().get(i);
            if(i == 0)
            {
                float fLevel = nLevel;
                if(fLevel == 0) fLevel = 1.0f;
                else if(fLevel < 0) fLevel = (fLevel + 30.0f) / 30.0f;
                else fLevel += 1.0f;
                if(MainActivity.hStream != 0)
                {
                    BASS_FX.BASS_BFX_VOLUME vol = new BASS_FX.BASS_BFX_VOLUME();
                    vol.lChannel = 0;
                    vol.fVolume = fLevel;
                    BASS.BASS_FXSetParameters(MainActivity.hFxVol, vol);
                }

                SeekBar seekBar = arSeek.get(i);
                seekBar.setProgress(nLevel + 30);
                TextView textView = arTextValue.get(i);
                textView.setText(String.valueOf(nLevel));
            }
            else
            {
                if(MainActivity.hStream != 0)
                {
                    BASS_FX.BASS_BFX_PEAKEQ eq = new BASS_FX.BASS_BFX_PEAKEQ();
                    eq.fBandwidth = 0;
                    eq.fQ = 0.7f;
                    eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
                    eq.fGain = nLevel;
                    eq.fCenter = arCenters[i-1];
                    BASS.BASS_FXSetParameters(arHFX[i-1], eq);
                }

                SeekBar seekBar = arSeek.get(i);
                seekBar.setProgress(nLevel + 30);
                TextView textView = arTextValue.get(i);
                textView.setText(String.valueOf(nLevel));
            }
        }
        PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
        playlistFragment.updateSavingEffect();
    }

    public void setEQRandom()
    {
        int nLevel = 0;
        float fLevel = 1.0f;
        if(MainActivity.hStream != 0)
        {
            BASS_FX.BASS_BFX_VOLUME vol = new BASS_FX.BASS_BFX_VOLUME();
            vol.lChannel = 0;
            vol.fVolume = fLevel;
            BASS.BASS_FXSetParameters(MainActivity.hFxVol, vol);
        }

        SeekBar seekBar = arSeek.get(0);
        seekBar.setProgress(nLevel + 30);
        TextView textView = arTextValue.get(0);
        textView.setText(String.valueOf(nLevel));

        for(int i = 0; i < 31; i++)
        {
            Random random = new Random();
            nLevel = random.nextInt(30) - 20;
            if(MainActivity.hStream != 0)
            {
                BASS_FX.BASS_BFX_PEAKEQ eq = new BASS_FX.BASS_BFX_PEAKEQ();
                eq.fBandwidth = 0;
                eq.fQ = 0.7f;
                eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
                eq.fGain = nLevel;
                eq.fCenter = arCenters[i];
                BASS.BASS_FXSetParameters(arHFX[i], eq);
            }

            seekBar = arSeek.get(i + 1);
            seekBar.setProgress(nLevel + 30);
            textView = arTextValue.get(i + 1);
            textView.setText(String.valueOf(nLevel));
        }
        MainActivity activity = (MainActivity)getActivity();
        PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
        playlistFragment.updateSavingEffect();
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
        if(MainActivity.hStream != 0)
        {
            BASS_FX.BASS_BFX_VOLUME vol = new BASS_FX.BASS_BFX_VOLUME();
            vol.lChannel = 0;
            vol.fVolume = fLevel;
            BASS.BASS_FXSetParameters(MainActivity.hFxVol, vol);
        }

        SeekBar seekBar = arSeek.get(0);
        seekBar.setProgress(nLevel + 30);
        TextView textView = arTextValue.get(0);
        textView.setText(String.valueOf(nLevel));

        if(bSave) {
            MainActivity activity = (MainActivity)getActivity();
            PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
            playlistFragment.updateSavingEffect();
        }
    }

    public void setEQ(int i, int nLevel)
    {
        setEQ(i, nLevel, true);
    }

    public void setEQ(int i, int nLevel, boolean bSave)
    {
        if(MainActivity.hStream != 0)
        {
            BASS_FX.BASS_BFX_PEAKEQ eq = new BASS_FX.BASS_BFX_PEAKEQ();
            eq.fBandwidth = 0;
            eq.fQ = 0.7f;
            eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
            eq.fGain = nLevel;
            eq.fCenter = arCenters[i-1];
            BASS.BASS_FXSetParameters(arHFX[i-1], eq);
        }

        SeekBar seekBar = arSeek.get(i);
        seekBar.setProgress(nLevel + 30);
        TextView textView = arTextValue.get(i);
        textView.setText(String.valueOf(nLevel));

        if (bSave) {
            MainActivity activity = (MainActivity)getActivity();
            PlaylistFragment playlistFragment = (PlaylistFragment)activity.mSectionsPagerAdapter.getItem(0);
            playlistFragment.updateSavingEffect();
        }
    }

    void setArHFX(int[] arHFX) {
        this.arHFX = arHFX;
    }

    public void showMenu(final int nItem) {
        final BottomSheetDialog dialog = new BottomSheetDialog(activity);
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        ScrollView scroll = new ScrollView(activity);

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView textTitle = new TextView (activity);
        textTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        textTitle.setGravity(Gravity.CENTER);
        textTitle.setText(arEqualizerItems.get(nItem).getEqualizerName());
        textTitle.setHeight((int)(40 *  getResources().getDisplayMetrics().density + 0.5));
        linearLayout.addView(textTitle, param);

        TextView textRemove = new TextView (activity);
        textRemove.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        textRemove.setGravity(Gravity.CENTER);
        textRemove.setText("削除");
        textRemove.setTextColor(Color.argb(255, 255, 0, 0));
        textRemove.setHeight((int)(56 *  getResources().getDisplayMetrics().density + 0.5));
        textRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                removeItem(nItem);
            }
        });
        linearLayout.addView(textRemove, param);

        TextView textSort = new TextView (activity);
        textSort.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        textSort.setGravity(Gravity.CENTER);
        textSort.setText("プリセットの並べ替え");
        textSort.setTextColor(Color.argb(255, 0, 0, 0));
        textSort.setHeight((int)(56 *  getResources().getDisplayMetrics().density + 0.5));
        textSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                recyclerEqualizers.setPadding(0, 0, 0, (int)(64 * getResources().getDisplayMetrics().density + 0.5));
                TextView textFinishSortEqualizer = (TextView) activity.findViewById(R.id.textFinishSortEqualizer);
                textFinishSortEqualizer.setVisibility(View.VISIBLE);
                bSorting = true;
                equalizersAdapter.notifyDataSetChanged();

                equalizerTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
                    @Override
                    public boolean onMove(RecyclerView recyclerEqualizers, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        final int fromPos = viewHolder.getAdapterPosition();
                        final int toPos = target.getAdapterPosition();

                        EqualizerItem itemTemp = arEqualizerItems.get(fromPos);
                        arEqualizerItems.remove(fromPos);
                        arEqualizerItems.add(toPos, itemTemp);

                        equalizersAdapter.notifyItemMoved(fromPos, toPos);

                        return true;
                    }

                    @Override
                    public void clearView(RecyclerView recyclerSongs, RecyclerView.ViewHolder viewHolder) {
                        super.clearView(recyclerSongs, viewHolder);

                        equalizersAdapter.notifyDataSetChanged();
                        saveData();
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    }
                });
                equalizerTouchHelper.attachToRecyclerView(recyclerEqualizers);
            }
        });
        linearLayout.addView(textSort, param);

        TextView textChange = new TextView (activity);
        textChange.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        textChange.setGravity(Gravity.CENTER);
        textChange.setText("プリセット名を変更");
        textChange.setTextColor(Color.argb(255, 0, 0, 0));
        textChange.setHeight((int)(56 *  getResources().getDisplayMetrics().density + 0.5));
        textChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("プリセット名を変更");
                LinearLayout linearLayout = new LinearLayout(activity);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                final EditText editPreset = new EditText (activity);
                editPreset.setHint("プリセット名");
                editPreset.setHintTextColor(Color.argb(255, 192, 192, 192));
                editPreset.setText(arEqualizerItems.get(nItem).getEqualizerName());
                linearLayout.addView(editPreset);
                builder.setView(linearLayout);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        arEqualizerItems.get(nItem).setEqualizerName(editPreset.getText().toString());
                        equalizersAdapter.notifyDataSetChanged();
                        saveData();
                    }
                });
                builder.setNegativeButton("キャンセル", null);
                builder.show();
            }
        });
        linearLayout.addView(textChange, param);

        TextView textReset = new TextView (activity);
        textReset.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        textReset.setGravity(Gravity.CENTER);
        textReset.setText("デフォルトに戻す");
        textReset.setTextColor(Color.argb(255, 0, 0, 0));
        textReset.setHeight((int)(56 *  getResources().getDisplayMetrics().density + 0.5));
        textReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("デフォルトに戻す");
                builder.setMessage("デフォルトを復元すると、現在の設定内容が消えてしまいますが、よろしいでしょうか？");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        resetPresets();
                    }
                });
                builder.setNegativeButton("キャンセル", null);
                builder.show();
            }
        });
        linearLayout.addView(textReset, param);

        TextView textCancel = new TextView (activity);
        textCancel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        textCancel.setGravity(Gravity.CENTER);
        textCancel.setText("キャンセル");
        textCancel.setHeight((int)(56 *  getResources().getDisplayMetrics().density + 0.5));
        textCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        linearLayout.addView(textCancel, param);

        scroll.addView(linearLayout);
        dialog.setContentView(scroll);
        dialog.show();
    }

    public void removeItem(int nItem)
    {
        arEqualizerItems.remove(nItem);
        equalizersAdapter.notifyDataSetChanged();
        saveData();
    }
}
