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

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS_FX;

import java.util.ArrayList;

public class EqualizerFragment extends Fragment implements AdapterView.OnItemClickListener {
    private ListView listView;
    private int[][] arPresets;
    private ArrayList<String> arPresetTitle;
    private float[] arCenters;
    private ArrayList<TextView> arTextValue;
    private ArrayList<SeekBar> arSeek;
    private int[] arHFX;

    public EqualizerFragment()
    {
        arHFX = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_equalizer, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TabLayout tabLayout = (TabLayout)getActivity().findViewById(R.id.tabEqualizer);
        tabLayout.addTab(tabLayout.newTab().setText("プリセット"));

        tabLayout.addTab(tabLayout.newTab().setText("カスタマイズ"));

        listView = (ListView)getActivity().findViewById(R.id.equalizerPresets);
        final ScrollView scrollView = (ScrollView)getActivity().findViewById(R.id.scrollCustomEqualizer);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getText()=="プリセット") {
                    listView.setVisibility(View.VISIBLE);
                }
                else{
                    scrollView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if(tab.getText()=="プリセット"){
                    listView.setVisibility(View.INVISIBLE);
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

        arPresets = new int[45][32];
        arPresets[0] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[1] = new int[] {  0,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[2] = new int[] {  0,-30,-20,-12, -7, -4, -3, -2, -1,  0,  0,  0,  0,  0, -1, -2, -3, -4, -7,-12,-20,-24,-27,-28,-29,-30,-30,-30,-30,-30,-30,-30};
        arPresets[3] = new int[] {  0,  0, -5, -8,-10,-12,-13,-14,-14,-15,-15,-15,-15,-15,-14,-14,-13,-12,-11, -8, -5, -3, -2, -1,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[4] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,-15,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30};
        arPresets[5] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -7,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15};
        arPresets[6] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12};
        arPresets[7] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -4, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9};
        arPresets[8] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6};
        arPresets[9] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3};
        arPresets[10] = new int[] {  0,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,-15,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30};
        arPresets[11] = new int[] {  0,-15,-15,-15,-15,-15,-15,-15,-15,-15, -7,  0,  0,  0,  0,  0,  0,  0,  0,  0, -7,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15};
        arPresets[12] = new int[] {  0,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12};
        arPresets[13] = new int[] {  0, -9, -9, -9, -9, -9, -9, -9, -9, -9, -4,  0,  0,  0,  0,  0,  0,  0,  0,  0, -4, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9};
        arPresets[14] = new int[] {  0, -6, -6, -6, -6, -6, -6, -6, -6, -6, -3,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6};
        arPresets[15] = new int[] {  0, -3, -3, -3, -3, -3, -3, -3, -3, -3, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3};
        arPresets[16] = new int[] {  0,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[17] = new int[] {  0,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15, -7,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[18] = new int[] {  0,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[19] = new int[] {  0, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -4,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[20] = new int[] {  0, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[21] = new int[] {  0, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[22] = new int[] {  0,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[23] = new int[] {  0,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15, -8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[24] = new int[] {  0,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[25] = new int[] {  0, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[26] = new int[] {  0, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[27] = new int[] {  0, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[28] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,-15,-30,-30,-30,-30,-30,-30,-30,-30,-30,-15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[29] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -8,-15,-15,-15,-15,-15,-15,-15,-15,-15, -8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[30] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12, -6,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[31] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[32] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3, -6, -6, -6, -6, -6, -6, -6, -6, -6, -3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[33] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -2, -3, -3, -3, -3, -3, -3, -3, -3, -3, -2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[34] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,-15,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30,-30};
        arPresets[35] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -8,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15,-15};
        arPresets[36] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -6,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12,-12};
        arPresets[37] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9};
        arPresets[38] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -3, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6};
        arPresets[39] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -2, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3};
        arPresets[40] = new int[] {  0, -6, -6, -6, -5, -5, -5, -5, -4, -3, -3, -2, -1, -1,  0,  0,  0,  0, -1, -1, -2, -3, -3, -4, -5, -5, -5, -5, -6, -6, -6, -6};
        arPresets[41] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -2, -3, -4, -5, -6, -5, -4, -3, -2, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[42] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -2, -3, -4, -5, -6, -6, -6, -6, -3, -2, -1, -2, -2, -2, -2, -2, -1, -1, -1,  0,  0,  0};
        arPresets[43] = new int[] {  0,  0,  0,  0,  0,  0,  0,  0, -1, -2, -3, -4, -3, -2, -1, -3, -5, -7, -3, -2, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};
        arPresets[44] = new int[] {  0, -2, -2, -2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, -3, -3, -3, -3, -3, -4, -4, -3, -2, -1, -1, -1,  0,  0,  0,  0,  0,  0};

        arPresetTitle = new ArrayList<String>();
        arPresetTitle.add("フラット");
        arPresetTitle.add("ベースの耳コピ");
        arPresetTitle.add("ボーカル強調");
        arPresetTitle.add("ボーカル抑制");
        arPresetTitle.add("高音強調（超最強）");
        arPresetTitle.add("高音強調（最強）");
        arPresetTitle.add("高音強調（強）");
        arPresetTitle.add("高音強調（中）");
        arPresetTitle.add("高音強調（弱）");
        arPresetTitle.add("高音強調（最弱）");
        arPresetTitle.add("中音強調（超最強）");
        arPresetTitle.add("中音強調（最強）");
        arPresetTitle.add("中音強調（強）");
        arPresetTitle.add("中音強調（中）");
        arPresetTitle.add("中音強調（弱）");
        arPresetTitle.add("中音強調（最弱）");
        arPresetTitle.add("低音強調（超最強）");
        arPresetTitle.add("低音強調（最強）");
        arPresetTitle.add("低音強調（強）");
        arPresetTitle.add("低音強調（中）");
        arPresetTitle.add("低音強調（弱）");
        arPresetTitle.add("低音強調（最弱）");
        arPresetTitle.add("高音カット（超最強）");
        arPresetTitle.add("高音カット（最強）");
        arPresetTitle.add("高音カット（強）");
        arPresetTitle.add("高音カット（中）");
        arPresetTitle.add("高音カット（弱）");
        arPresetTitle.add("高音カット（最弱）");
        arPresetTitle.add("中音カット（超最強）");
        arPresetTitle.add("中音カット（最強）");
        arPresetTitle.add("中音カット（強）");
        arPresetTitle.add("中音カット（中）");
        arPresetTitle.add("中音カット（弱）");
        arPresetTitle.add("中音カット（最弱）");
        arPresetTitle.add("低音カット（超最強）");
        arPresetTitle.add("低音カット（最強）");
        arPresetTitle.add("低音カット（強）");
        arPresetTitle.add("低音カット（中）");
        arPresetTitle.add("低音カット（弱）");
        arPresetTitle.add("低音カット（最弱）");
        arPresetTitle.add("Pop");
        arPresetTitle.add("Rock");
        arPresetTitle.add("Jazz");
        arPresetTitle.add("Electronic");
        arPresetTitle.add("Acoustic");

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(
                        getActivity(),
                        android.R.layout.simple_list_item_single_choice,
                        arPresetTitle);

        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(adapter);
        listView.setItemChecked(0, true);

        listView.setOnItemClickListener(this);

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
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
    {
        listView.setItemChecked(position, listView.isItemChecked(position));
        setEQ(position);
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
        for(int i = 0; i < 32; i++)
        {
            int nLevel = arPresets[row][i];
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
    }

    void setArHFX(int[] arHFX) {
        this.arHFX = arHFX;
    }
}
