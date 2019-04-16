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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
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
    private boolean isContinue = true;
    private final Handler handler;
    private int nLongClick = 0;

    public ArrayList<SeekBar> getArSeek() { return arSeek; }
    public float[] getArCenters() { return arCenters; }
    ArrayList<EqualizerItem> getArEqualizerItems() {
        return arEqualizerItems;
    }
    private void setArEqualizerItems(ArrayList<EqualizerItem> arLists) {
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
        handler = new Handler();
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

        equalizersAdapter = new EqualizersAdapter(activity, arEqualizerItems);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ScrollView scrollView = activity.findViewById(R.id.scrollCustomEqualizer);

        final RadioGroup radioGroupEqualizer = activity.findViewById(R.id.radioGroupEqualizer);
        radioGroupEqualizer.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int nItem) {
                if(nItem == R.id.radioButtonPreset) {
                    recyclerEqualizers.setVisibility(View.VISIBLE);
                    scrollView.setVisibility(View.INVISIBLE);
                }
                else {
                    scrollView.setVisibility(View.VISIBLE);
                    recyclerEqualizers.setVisibility(View.INVISIBLE);
                }
            }
        });

        arCenters = new float[] {20000, 16000, 12500, 10000, 8000, 6300, 5000, 4000, 3150, 2500, 2000, 1600, 1250, 1000, 800, 630, 500, 400, 315, 250, 200, 160, 125, 100, 80, 63, 50, 40, 31.5f, 25, 20};

        loadData();

        recyclerEqualizers = activity.findViewById(R.id.recyclerEqualizers);
        recyclerEqualizers.setHasFixedSize(false);
        final LinearLayoutManager equalizersManager = new LinearLayoutManager(activity);
        recyclerEqualizers.setLayoutManager(equalizersManager);
        recyclerEqualizers.setAdapter(equalizersAdapter);

        arTextValue = new ArrayList<>();
        arTextValue.add((TextView)activity.findViewById(R.id.textVolValue));
        arTextValue.add((TextView)activity.findViewById(R.id.text20KValue));
        arTextValue.add((TextView)activity.findViewById(R.id.text16KValue));
        arTextValue.add((TextView)activity.findViewById(R.id.text12_5KValue));
        arTextValue.add((TextView)activity.findViewById(R.id.text10KValue));
        arTextValue.add((TextView)activity.findViewById(R.id.text8KValue));
        arTextValue.add((TextView)activity.findViewById(R.id.text6_3KValue));
        arTextValue.add((TextView)activity.findViewById(R.id.text5KValue));
        arTextValue.add((TextView)activity.findViewById(R.id.text4KValue));
        arTextValue.add((TextView)activity.findViewById(R.id.text3_15KValue));
        arTextValue.add((TextView)activity.findViewById(R.id.text2_5KValue));
        arTextValue.add((TextView)activity.findViewById(R.id.text2KValue));
        arTextValue.add((TextView)activity.findViewById(R.id.text1_6KValue));
        arTextValue.add((TextView)activity.findViewById(R.id.text1_25KValue));
        arTextValue.add((TextView)activity.findViewById(R.id.text1KValue));
        arTextValue.add((TextView)activity.findViewById(R.id.text800Value));
        arTextValue.add((TextView)activity.findViewById(R.id.text630Value));
        arTextValue.add((TextView)activity.findViewById(R.id.text500Value));
        arTextValue.add((TextView)activity.findViewById(R.id.text400Value));
        arTextValue.add((TextView)activity.findViewById(R.id.text315Value));
        arTextValue.add((TextView)activity.findViewById(R.id.text250Value));
        arTextValue.add((TextView)activity.findViewById(R.id.text200Value));
        arTextValue.add((TextView)activity.findViewById(R.id.text160Value));
        arTextValue.add((TextView)activity.findViewById(R.id.text125Value));
        arTextValue.add((TextView)activity.findViewById(R.id.text100Value));
        arTextValue.add((TextView)activity.findViewById(R.id.text80Value));
        arTextValue.add((TextView)activity.findViewById(R.id.text63Value));
        arTextValue.add((TextView)activity.findViewById(R.id.text50Value));
        arTextValue.add((TextView)activity.findViewById(R.id.text40Value));
        arTextValue.add((TextView)activity.findViewById(R.id.text31_5Value));
        arTextValue.add((TextView)activity.findViewById(R.id.text25Value));
        arTextValue.add((TextView)activity.findViewById(R.id.text20Value));
        for(int i = 0; i < arTextValue.size(); i++)
            arTextValue.get(i).setText("0");

        arSeek = new ArrayList<>();
        arSeek.add((SeekBar)activity.findViewById(R.id.seekVol));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek20K));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek16K));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek12_5K));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek10K));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek8K));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek6_3K));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek5K));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek4K));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek3_15K));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek2_5K));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek2K));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek1_6K));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek1_25K));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek1K));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek800));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek630));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek500));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek400));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek315));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek250));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek200));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek160));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek125));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek100));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek80));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek63));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek50));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek40));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek31_5));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek25));
        arSeek.add((SeekBar)activity.findViewById(R.id.seek20));

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
                                    eq.fBandwidth = 0.7f;
                                    eq.fQ = 0.0f;
                                    eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
                                    eq.fGain = nLevel;
                                    eq.fCenter = arCenters[j-1];
                                    BASS.BASS_FXSetParameters(arHFX[j-1], eq);
                                }

                                TextView textView = arTextValue.get(j);
                                textView.setText(String.valueOf(nLevel));
                            }

                            for (int j = 0; j < arEqualizerItems.size(); j++)
                                arEqualizerItems.get(j).setSelected(false);
                            equalizersAdapter.notifyDataSetChanged();
                        }

                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    }
            );
        }

        Button btnAddEqualizer = activity.findViewById(R.id.btnAddEqualizer);
        btnAddEqualizer.setOnClickListener(this);

        TextView textFinishSortEqualizer = activity.findViewById(R.id.textFinishSortEqualizer);
        textFinishSortEqualizer.setOnClickListener(this);

        ArrayList<ImageButton> arButtonMinus = new ArrayList<>();
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.buttonVolMinus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button20KMinus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button16KMinus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button12_5KMinus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button10KMinus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button8KMinus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button6_3KMinus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button5KMinus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button4KMinus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button3_15KMinus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button2_5KMinus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button2KMinus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button1_6KMinus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button1_25KMinus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button1KMinus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button800Minus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button630Minus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button500Minus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button400Minus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button315Minus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button250Minus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button200Minus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button160Minus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button125Minus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button100Minus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button80Minus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button63Minus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button50Minus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button40Minus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button31_5Minus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button25Minus));
        arButtonMinus.add((ImageButton)activity.findViewById(R.id.button20Minus));

        for(int i = 0; i < arButtonMinus.size(); i++)
        {
            final int j = i;
            arButtonMinus.get(i).setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    int nProgress = minusValue(arSeek.get(j));
                    if(j == 0) setVol(nProgress);
                    else setEQ(j, nProgress);
                }
            });
            arButtonMinus.get(i).setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    nLongClick = j;
                    isContinue = true;
                    handler.post(repeatMinusValue);
                    return true;
                }
            });
            arButtonMinus.get(i).setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                        isContinue = false;
                    return false;
                }
            });
        }

        ArrayList<ImageButton> arButtonPlus = new ArrayList<>();
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.buttonVolPlus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button20KPlus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button16KPlus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button12_5KPlus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button10KPlus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button8KPlus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button6_3KPlus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button5KPlus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button4KPlus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button3_15KPlus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button2_5KPlus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button2KPlus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button1_6KPlus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button1_25KPlus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button1KPlus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button800Plus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button630Plus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button500Plus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button400Plus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button315Plus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button250Plus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button200Plus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button160Plus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button125Plus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button100Plus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button80Plus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button63Plus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button50Plus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button40Plus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button31_5Plus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button25Plus));
        arButtonPlus.add((ImageButton)activity.findViewById(R.id.button20Plus));

        for(int i = 0; i < arButtonPlus.size(); i++)
        {
            final int j = i;
            arButtonPlus.get(i).setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    int nProgress = plusValue(arSeek.get(j));
                    if(j == 0) setVol(nProgress);
                    else setEQ(j, nProgress);
                }
            });
            arButtonPlus.get(i).setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    nLongClick = j;
                    isContinue = true;
                    handler.post(repeatPlusValue);
                    return true;
                }
            });
            arButtonPlus.get(i).setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                        isContinue = false;
                    return false;
                }
            });
        }
    }

    private final Runnable repeatMinusValue = new Runnable()
    {
        @Override
        public void run()
        {
            if(!isContinue)
                return;
            int nProgress = minusValue(arSeek.get(nLongClick));
            if(nLongClick == 0) setVol(nProgress);
            else setEQ(nLongClick, nProgress);
            handler.postDelayed(this, 100);
        }
    };

    private final Runnable repeatPlusValue = new Runnable()
    {
        @Override
        public void run()
        {
            if(!isContinue)
                return;
            int nProgress = plusValue(arSeek.get(nLongClick));
            if(nLongClick == 0) setVol(nProgress);
            else setEQ(nLongClick, nProgress);
            handler.postDelayed(this, 100);
        }
    };

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnAddEqualizer:
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.addPreset);
                LinearLayout linearLayout = new LinearLayout(activity);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                final EditText editPreset = new EditText(activity);
                editPreset.setHint(R.string.presetName);
                editPreset.setHintTextColor(Color.argb(255, 192, 192, 192));
                editPreset.setText(R.string.newPreset);
                linearLayout.addView(editPreset);
                builder.setView(linearLayout);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ArrayList<Integer> arPresets = new ArrayList<>();
                        for (int i = 0; i < 32; i++) {
                            arPresets.add(Integer.parseInt((String) arTextValue.get(i).getText()));
                        }
                        arEqualizerItems.add(new EqualizerItem(editPreset.getText().toString(), arPresets));
                        saveData();
                        equalizersAdapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                {
                    @Override
                    public void onShow(DialogInterface arg0)
                    {
                        editPreset.requestFocus();
                        editPreset.setSelection(editPreset.getText().toString().length());
                        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (null != imm) imm.showSoftInput(editPreset, 0);
                    }
                });
                alertDialog.show();
                break;

            case R.id.textFinishSortEqualizer:
                recyclerEqualizers.setPadding(0, 0, 0, 0);
                TextView textFinishSortEqualizer = activity.findViewById(R.id.textFinishSortEqualizer);
                textFinishSortEqualizer.setVisibility(View.GONE);
                bSorting = false;
                equalizersAdapter.notifyDataSetChanged();
                equalizerTouchHelper.attachToRecyclerView(null);
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
        EqualizerItem itemSelected = arEqualizerItems.get(nEqualizer);
        boolean bSelected = !itemSelected.isSelected();
        int nSelected = 0;
        if(bSelected) nSelected = nEqualizer;
        setEQ(nSelected);
        for (int i = 0; i < arEqualizerItems.size(); i++) {
            EqualizerItem item = arEqualizerItems.get(i);
            if (i == nSelected) item.setSelected(true);
            else item.setSelected(false);
        }
        equalizersAdapter.notifyDataSetChanged();
    }

    private void loadData()
    {
        SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        ArrayList<EqualizerItem> arEqualizerItems = gson.fromJson(preferences.getString("arEqualizerItems",""), new TypeToken<ArrayList<EqualizerItem>>(){}.getType());
        if(arEqualizerItems != null) setArEqualizerItems(arEqualizerItems);
        else resetPresets();
        this.arEqualizerItems.get(0).setSelected(true);
        for(int i = 1; i < this.arEqualizerItems.size(); i++)
            this.arEqualizerItems.get(i).setSelected(false);
    }

    private void saveData()
    {
        SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        preferences.edit().putString("arEqualizerItems", gson.toJson(arEqualizerItems)).apply();
    }

    private void resetPresets()
    {
        if(arEqualizerItems.size() > 0) arEqualizerItems.clear();
        arEqualizerItems.add(new EqualizerItem(getString(R.string.off), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.random), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.transcribeBass), new ArrayList<>(Arrays.asList(  0,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.VocalBoost), new ArrayList<>(Arrays.asList(  0,-30,-20,-12, -7, -4, -3, -2, -1,  0,  0,  0,  0,  0, -1, -2, -3, -4, -7,-12,-20,-24,-27,-28,-29,-30,-30,-30,-30,-30,-30,-30))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.VocalReducer), new ArrayList<>(Arrays.asList(  0,  0, -5, -8,-10,-12,-13,-14,-14,-15,-15,-15,-15,-15,-14,-14,-13,-12,-11, -8, -5, -3, -2, -1,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleBoostUltraStrong), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,-15,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleBoostStrongest), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -7,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleBoostStrong), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleBoostMiddle), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -4, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleBoostWeak), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleBoostWeakest), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleBoostUltraStrong), new ArrayList<>(Arrays.asList(  0,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,-15,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleBoostStrongest), new ArrayList<>(Arrays.asList(  0,-15,-15,-15,-15,-15,-15,-15,-15,-15, -7,  0,  0,  0,  0,  0,  0,  0,  0,  0, -7,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleBoostStrong), new ArrayList<>(Arrays.asList(  0,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleBoostMiddle), new ArrayList<>(Arrays.asList(  0, -9, -9, -9, -9, -9, -9, -9, -9, -9, -4,  0,  0,  0,  0,  0,  0,  0,  0,  0, -4, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleBoostWeak), new ArrayList<>(Arrays.asList(  0, -6, -6, -6, -6, -6, -6, -6, -6, -6, -3,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleBoostWeakest), new ArrayList<>(Arrays.asList(  0, -3, -3, -3, -3, -3, -3, -3, -3, -3, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.BassBoostUltraStrong), new ArrayList<>(Arrays.asList(  0,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.BassBoostStrongest), new ArrayList<>(Arrays.asList(  0,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15, -7,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.BassBoostStrong), new ArrayList<>(Arrays.asList(  0,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.BassBoostMiddle), new ArrayList<>(Arrays.asList(  0, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -4,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.BassBoostWeak), new ArrayList<>(Arrays.asList(  0, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.BassBoostWeakest), new ArrayList<>(Arrays.asList(  0, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleCutUltraStrong), new ArrayList<>(Arrays.asList(  0,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleCutStrongest), new ArrayList<>(Arrays.asList(  0,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15, -8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleCutStrong), new ArrayList<>(Arrays.asList(  0,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleCutMiddle), new ArrayList<>(Arrays.asList(  0, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleCutWeak), new ArrayList<>(Arrays.asList(  0, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.TrebleCutWeakest), new ArrayList<>(Arrays.asList(  0, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleCutUltraStrong), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,-15,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleCutStrongest), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -8,-15,-15,-15,-15,-15,-15,-15,-15,-15, -8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleCutStrong), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleCutMiddle), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleCutWeak), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3, -6, -6, -6, -6, -6, -6, -6, -6, -6, -3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.MiddleCutWeakest), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -2, -3, -3, -3, -3, -3, -3, -3, -3, -3, -2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.BassCutUltraStrong), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,-15,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.BassCutStrongest), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -8,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.BassCutStrong), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.BassCutMiddle), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.BassCutWeak), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.BassCutWeakest), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -2, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.Pop), new ArrayList<>(Arrays.asList(  0, -6, -6, -6, -5, -5, -5, -5, -4, -3, -3, -2, -1, -1,  0,  0,  0,  0, -1, -1, -2, -3, -3, -4, -5, -5, -5, -5, -6, -6, -6, -6))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.Rock), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -2, -3, -4, -5, -6, -5, -4, -3, -2, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.Jazz), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -2, -3, -4, -5, -6, -6, -6, -6, -3, -2, -1, -2, -2, -2, -2, -2, -1, -1, -1,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.Electronic), new ArrayList<>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0, -1, -2, -3, -4, -3, -2, -1, -3, -5, -7, -3, -2, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
        arEqualizerItems.add(new EqualizerItem(getString(R.string.Acoustic), new ArrayList<>(Arrays.asList(  0, -2, -2, -2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, -3, -3, -3, -3, -3, -4, -4, -3, -2, -1, -1, -1,  0,  0,  0,  0,  0,  0))));
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
                    eq.fBandwidth = 0.7f;
                    eq.fQ = 0.0f;
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
        if(arEqualizerItems.get(nLastChecked).getEqualizerName().equals(getString(R.string.transcribeBassOctave)))
            activity.controlFragment.setPitch(0.0f);
        nLastChecked = row;

        if(arEqualizerItems.get(row).getEqualizerName().equals(getString(R.string.random))) {
            setEQRandom();
            return;
        }
        else if(arEqualizerItems.get(row).getEqualizerName().equals(getString(R.string.transcribeBassOctave)))
            activity.controlFragment.setPitch(12.0f);

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
                    eq.fBandwidth = 0.7f;
                    eq.fQ = 0.0f;
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
        activity.playlistFragment.updateSavingEffect();
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
                eq.fBandwidth = 0.7f;
                eq.fQ = 0.0f;
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
        activity.playlistFragment.updateSavingEffect();
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

        if(bSave) activity.playlistFragment.updateSavingEffect();
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
            eq.fBandwidth = 0.7f;
            eq.fQ = 0.0f;
            eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
            eq.fGain = nLevel;
            eq.fCenter = arCenters[i-1];
            BASS.BASS_FXSetParameters(arHFX[i-1], eq);
        }

        SeekBar seekBar = arSeek.get(i);
        seekBar.setProgress(nLevel + 30);
        TextView textView = arTextValue.get(i);
        textView.setText(String.valueOf(nLevel));

        if (bSave) activity.playlistFragment.updateSavingEffect();
    }

    void setArHFX(int[] arHFX) {
        this.arHFX = arHFX;
    }

    public void showMenu(final int nItem) {
        final BottomMenu menu = new BottomMenu(activity);
        menu.setTitle(arEqualizerItems.get(nItem).getEqualizerName());
        menu.addMenu(getString(R.string.changePresetName), R.drawable.ic_actionsheet_edit, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.changePresetName);
                LinearLayout linearLayout = new LinearLayout(activity);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                final EditText editPreset = new EditText (activity);
                editPreset.setHint(R.string.presetName);
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
                builder.setNegativeButton(R.string.cancel, null);
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                {
                    @Override
                    public void onShow(DialogInterface arg0)
                    {
                        editPreset.requestFocus();
                        editPreset.setSelection(editPreset.getText().toString().length());
                        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (null != imm) imm.showSoftInput(editPreset, 0);
                    }
                });
                alertDialog.show();
            }
        });
        menu.addMenu(getString(R.string.sortPreset), R.drawable.ic_actionsheet_sort, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                recyclerEqualizers.setPadding(0, 0, 0, (int)(64 * getResources().getDisplayMetrics().density + 0.5));
                TextView textFinishSortEqualizer = activity.findViewById(R.id.textFinishSortEqualizer);
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
        menu.addDestructiveMenu(getString(R.string.delete), R.drawable.ic_actionsheet_delete, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(arEqualizerItems.get(nItem).getEqualizerName());
                builder.setMessage(R.string.askDeletePreset);
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
                        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        positiveButton.setTextColor(Color.argb(255, 255, 0, 0));
                    }
                });
                alertDialog.show();
            }
        });
        menu.addDestructiveMenu(getString(R.string.initializePreset), R.drawable.ic_actionsheet_initialize, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.initializePreset);
                builder.setMessage(R.string.askInitializePreset);
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
        arEqualizerItems.remove(nItem);
        equalizersAdapter.notifyDataSetChanged();
        saveData();
    }
}
