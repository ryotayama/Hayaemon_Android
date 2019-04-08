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
    private MainActivity activity = null;

    public SpeedRangeSettingFragment()
    {
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting_speedrange, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnReturnSpeedRangeSetting = activity.findViewById(R.id.btnReturnSpeedRangeSetting);
        btnReturnSpeedRangeSetting.setOnClickListener(this);

        Button btnCloseSpeedRangeSetting = activity.findViewById(R.id.btnCloseSpeedRangeSetting);
        btnCloseSpeedRangeSetting.setOnClickListener(this);

        final NumberPicker intSpeedRangeFromPicker = activity.findViewById(R.id.intSpeedRangeFromPicker);
        final String[] arFromInts = {"90", "80", "70", "60", "50", "40", "30", "20", "10"};
        intSpeedRangeFromPicker.setDisplayedValues(arFromInts);
        intSpeedRangeFromPicker.setMaxValue(8);
        intSpeedRangeFromPicker.setMinValue(0);
        intSpeedRangeFromPicker.setWrapSelectorWheel(false);
        intSpeedRangeFromPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int nOldValue, int nNewValue) {
                int nMinSpeed = Integer.parseInt(arFromInts[nNewValue]);
                activity.controlFragment.setMinSpeed(nMinSpeed);
                SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                preferences.edit().putInt("nMinSpeed", nMinSpeed).apply();
            }
        });

        String strMinSpeed = String.format(Locale.getDefault(), "%d", activity.controlFragment.getMinSpeed());
        for(int i = 0; i < arFromInts.length; i++)
        {
            if(arFromInts[i].equals(strMinSpeed))
                intSpeedRangeFromPicker.setValue(i);
        }

        final NumberPicker intSpeedRangeToPicker = activity.findViewById(R.id.intSpeedRangeToPicker);
        final String[] arToInts = {"400", "300", "200"};
        intSpeedRangeToPicker.setDisplayedValues(arToInts);
        intSpeedRangeToPicker.setMaxValue(2);
        intSpeedRangeToPicker.setMinValue(0);
        intSpeedRangeToPicker.setWrapSelectorWheel(false);
        intSpeedRangeToPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int nOldValue, int nNewValue) {
                int nMaxSpeed = Integer.parseInt(arToInts[nNewValue]);
                activity.controlFragment.setMaxSpeed(nMaxSpeed);
                SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                preferences.edit().putInt("nMaxSpeed", nMaxSpeed).apply();
            }
        });

        String strMaxSpeed = String.format(Locale.getDefault(), "%d", activity.controlFragment.getMaxSpeed());
        for(int i = 0; i < arToInts.length; i++)
        {
            if(arToInts[i].equals(strMaxSpeed))
                intSpeedRangeToPicker.setValue(i);
        }

        activity.findViewById(R.id.btnResetSpeedRange).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.controlFragment.setMinSpeed(10);
                SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                preferences.edit().putInt("nMinSpeed", 10).apply();
                String strMinSpeed = "10";
                for(int i = 0; i < arFromInts.length; i++)
                {
                    if(arFromInts[i].equals(strMinSpeed))
                        intSpeedRangeFromPicker.setValue(i);
                }
                activity.controlFragment.setMaxSpeed(400);
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
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
            transaction.replace(R.id.relativeMain, new SettingFragment());
            transaction.commit();
        }
        else if(view.getId() == R.id.btnCloseSpeedRangeSetting) {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
            transaction.remove(this);
            transaction.commit();
        }
    }
}
