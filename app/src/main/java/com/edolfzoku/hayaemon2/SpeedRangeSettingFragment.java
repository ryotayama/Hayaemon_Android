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

public class SpeedRangeSettingFragment extends Fragment implements View.OnClickListener
{
    private MainActivity mActivity = null;

    public SpeedRangeSettingFragment()
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
        return inflater.inflate(R.layout.fragment_setting_speedrange, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnReturnSpeedRangeSetting = mActivity.findViewById(R.id.btnReturnSpeedRangeSetting);
        btnReturnSpeedRangeSetting.setOnClickListener(this);

        Button btnCloseSpeedRangeSetting = mActivity.findViewById(R.id.btnCloseSpeedRangeSetting);
        btnCloseSpeedRangeSetting.setOnClickListener(this);

        final NumberPicker intSpeedRangeFromPicker = mActivity.findViewById(R.id.intSpeedRangeFromPicker);
        final String[] arFromInts = {"90", "80", "70", "60", "50", "40", "30", "20", "10"};
        intSpeedRangeFromPicker.setDisplayedValues(arFromInts);
        intSpeedRangeFromPicker.setMaxValue(8);
        intSpeedRangeFromPicker.setMinValue(0);
        intSpeedRangeFromPicker.setWrapSelectorWheel(false);
        intSpeedRangeFromPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int nOldValue, int nNewValue) {
                int nMinSpeed = Integer.parseInt(arFromInts[nNewValue]);
                mActivity.controlFragment.setMinSpeed(nMinSpeed);
                SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                preferences.edit().putInt("nMinSpeed", nMinSpeed).apply();
            }
        });

        String strMinSpeed = String.format(Locale.getDefault(), "%d", mActivity.controlFragment.getMinSpeed());
        for(int i = 0; i < arFromInts.length; i++)
        {
            if(arFromInts[i].equals(strMinSpeed))
                intSpeedRangeFromPicker.setValue(i);
        }

        final NumberPicker intSpeedRangeToPicker = mActivity.findViewById(R.id.intSpeedRangeToPicker);
        final String[] arToInts = {"5000", "4000", "3000", "2000", "1000", "900 ", "800 ", "700 ", "600 ", "500 ", "400 ", "300 ", "200 "};
        intSpeedRangeToPicker.setDisplayedValues(arToInts);
        intSpeedRangeToPicker.setMaxValue(12);
        intSpeedRangeToPicker.setMinValue(0);
        intSpeedRangeToPicker.setWrapSelectorWheel(false);
        intSpeedRangeToPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int nOldValue, int nNewValue) {
                int nMaxSpeed = Integer.parseInt(arToInts[nNewValue].trim());
                mActivity.controlFragment.setMaxSpeed(nMaxSpeed);
                SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                preferences.edit().putInt("nMaxSpeed", nMaxSpeed).apply();
            }
        });

        int nMaxSpeed = mActivity.controlFragment.getMaxSpeed();
        String strMaxSpeed;
        if(nMaxSpeed >= 1000)
            strMaxSpeed = String.format(Locale.getDefault(), "%d", nMaxSpeed);
        else
            strMaxSpeed = String.format(Locale.getDefault(), "%d ", nMaxSpeed);
        for(int i = 0; i < arToInts.length; i++)
        {
            if(arToInts[i].equals(strMaxSpeed))
                intSpeedRangeToPicker.setValue(i);
        }

        mActivity.findViewById(R.id.btnResetSpeedRange).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.controlFragment.setMinSpeed(10);
                SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                preferences.edit().putInt("nMinSpeed", 10).apply();
                String strMinSpeed = "10";
                for(int i = 0; i < arFromInts.length; i++)
                {
                    if(arFromInts[i].equals(strMinSpeed))
                        intSpeedRangeFromPicker.setValue(i);
                }
                mActivity.controlFragment.setMaxSpeed(400);
                preferences.edit().putInt("nMaxSpeed", 400).apply();
                String strMaxSpeed = "400";
                for(int i = 0; i < arToInts.length; i++)
                {
                    if(arToInts[i].equals(strMaxSpeed))
                        intSpeedRangeToPicker.setValue(i);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnReturnSpeedRangeSetting) {
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
            transaction.replace(R.id.relativeMain, new SettingFragment());
            transaction.commit();
        }
        else if(view.getId() == R.id.btnCloseSpeedRangeSetting) {
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
            transaction.remove(this);
            transaction.commit();
        }
    }
}
