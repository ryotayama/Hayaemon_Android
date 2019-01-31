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
import android.os.Handler;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

public class EqualizerFragment extends Fragment implements View.OnClickListener {
    private MainActivity activity = null;
    private RecyclerView recyclerEqualizers;
    private EqualizersAdapter equalizersAdapter;
    private ArrayList<EqualizerItem> arEqualizerItems;
    private ItemTouchHelper equalizerTouchHelper;
    private float[] arCenters;
    private ArrayList<TextView> arTextValue;
    private ArrayList<SeekBar> arSeek;
    private ArrayList<ImageButton> arButtonMinus;
    private ArrayList<ImageButton> arButtonPlus;
    private int[] arHFX;
    private int nLastChecked = 0;
    private boolean bSorting = false;
    private boolean isContinue = true;
    private Handler handler;
    private int nLongClick = 0;

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
        handler = new Handler();
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

        arButtonMinus = new ArrayList<ImageButton>();
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.buttonVolMinus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button20KMinus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button16KMinus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button12_5KMinus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button10KMinus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button8KMinus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button6_3KMinus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button5KMinus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button4KMinus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button3_15KMinus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button2_5KMinus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button2KMinus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button1_6KMinus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button1_25KMinus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button1KMinus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button800Minus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button630Minus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button500Minus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button400Minus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button315Minus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button250Minus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button200Minus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button160Minus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button125Minus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button100Minus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button80Minus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button63Minus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button50Minus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button40Minus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button31_5Minus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button25Minus));
        arButtonMinus.add((ImageButton)getActivity().findViewById(R.id.button20Minus));

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

        arButtonPlus = new ArrayList<ImageButton>();
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.buttonVolPlus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button20KPlus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button16KPlus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button12_5KPlus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button10KPlus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button8KPlus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button6_3KPlus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button5KPlus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button4KPlus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button3_15KPlus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button2_5KPlus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button2KPlus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button1_6KPlus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button1_25KPlus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button1KPlus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button800Plus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button630Plus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button500Plus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button400Plus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button315Plus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button250Plus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button200Plus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button160Plus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button125Plus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button100Plus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button80Plus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button63Plus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button50Plus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button40Plus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button31_5Plus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button25Plus));
        arButtonPlus.add((ImageButton)getActivity().findViewById(R.id.button20Plus));

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

    Runnable repeatMinusValue = new Runnable()
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

    Runnable repeatPlusValue = new Runnable()
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
                TextView textFinishSortEqualizer = (TextView) activity.findViewById(R.id.textFinishSortEqualizer);
                textFinishSortEqualizer.setVisibility(View.GONE);
                bSorting = false;
                equalizersAdapter.notifyDataSetChanged();
                equalizerTouchHelper.attachToRecyclerView(null);
                break;

            default:
                break;
        }
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
        nProgress += 1;
        if(nProgress > 60) nProgress = 60;
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
        arEqualizerItems.add(new EqualizerItem("オフ",                              new ArrayList<Integer>(Arrays.asList(  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0))));
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
        linearLayout.addView(textChange, param);

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

        TextView textReset = new TextView (activity);
        textReset.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        textReset.setGravity(Gravity.CENTER);
        textReset.setText("すべてのプリセットを初期化");
        textReset.setTextColor(Color.argb(255, 255, 0, 0));
        textReset.setHeight((int)(56 *  getResources().getDisplayMetrics().density + 0.5));
        textReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("すべてのプリセットを初期化");
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
