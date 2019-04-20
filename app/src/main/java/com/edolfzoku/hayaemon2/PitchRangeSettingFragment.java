package com.edolfzoku.hayaemon2;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

import java.util.Locale;

public class PitchRangeSettingFragment extends Fragment implements View.OnClickListener
{
    private MainActivity mActivity = null;

    public PitchRangeSettingFragment()
    {
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting_pitchrange, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnReturnPitchRangeSetting = mActivity.findViewById(R.id.btnReturnPitchRangeSetting);
        btnReturnPitchRangeSetting.setOnClickListener(this);

        Button btnClosePitchRangeSetting = mActivity.findViewById(R.id.btnClosePitchRangeSetting);
        btnClosePitchRangeSetting.setOnClickListener(this);

        final NumberPicker intPitchRangeFromPicker = mActivity.findViewById(R.id.intPitchRangeFromPicker);
        final String[] arFromInts = {"24", "23", "22", "21", "20", "19", "18", "17", "16", "15", "14", "13", "12", "11", "10", "9", "8", "7", "6"};
        intPitchRangeFromPicker.setDisplayedValues(arFromInts);
        intPitchRangeFromPicker.setMaxValue(18);
        intPitchRangeFromPicker.setMinValue(0);
        intPitchRangeFromPicker.setWrapSelectorWheel(false);
        intPitchRangeFromPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int nOldValue, int nNewValue) {
                int nMaxPitch = Integer.parseInt(arFromInts[nNewValue]);
                mActivity.controlFragment.setMaxPitch(nMaxPitch);
                SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                preferences.edit().putInt("nMaxPitch", nMaxPitch).apply();
            }
        });

        String strMaxPitch = String.format(Locale.getDefault(), "%d", mActivity.controlFragment.getMaxPitch());
        for(int i = 0; i < arFromInts.length; i++)
        {
            if(arFromInts[i].equals(strMaxPitch))
                intPitchRangeFromPicker.setValue(i);
        }

        final NumberPicker intPitchRangeToPicker = mActivity.findViewById(R.id.intPitchRangeToPicker);
        final String[] arToInts = {"24", "23", "22", "21", "20", "19", "18", "17", "16", "15", "14", "13", "12", "11", "10", "9", "8", "7", "6"};
        intPitchRangeToPicker.setDisplayedValues(arToInts);
        intPitchRangeToPicker.setMaxValue(18);
        intPitchRangeToPicker.setMinValue(0);
        intPitchRangeToPicker.setWrapSelectorWheel(false);
        intPitchRangeToPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int nOldValue, int nNewValue) {
                int nMinPitch = Integer.parseInt(arToInts[nNewValue]) * -1;
                mActivity.controlFragment.setMinPitch(nMinPitch);
                SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                preferences.edit().putInt("nMinPitch", nMinPitch).apply();
            }
        });

        String strMinPitch = String.format(Locale.getDefault(), "%d", mActivity.controlFragment.getMinPitch() * -1);
        for(int i = 0; i < arToInts.length; i++)
        {
            if(arToInts[i].equals(strMinPitch))
                intPitchRangeToPicker.setValue(i);
        }

        mActivity.findViewById(R.id.btnResetPitchRange).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.controlFragment.setMaxPitch(12);
                SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                preferences.edit().putInt("nMaxPitch", 12).apply();
                String strMaxPitch = "12";
                for(int i = 0; i < arFromInts.length; i++)
                {
                    if(arFromInts[i].equals(strMaxPitch))
                        intPitchRangeFromPicker.setValue(i);
                }
                mActivity.controlFragment.setMinPitch(-12);
                preferences.edit().putInt("nMinPitch", -12).apply();
                String strMinPitch = "12";
                for(int i = 0; i < arToInts.length; i++)
                {
                    if(arToInts[i].equals(strMinPitch))
                        intPitchRangeToPicker.setValue(i);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnReturnPitchRangeSetting) {
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
            transaction.replace(R.id.relativeMain, new SettingFragment());
            transaction.commit();
        }
        else if(view.getId() == R.id.btnClosePitchRangeSetting) {
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
            transaction.remove(this);
            transaction.commit();
        }
    }
}
