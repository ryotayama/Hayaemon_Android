package com.edolfzoku.hayaemon2;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
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
        final String[] arFromInts = {"60", "48", "36", "24", "23", "22", "21", "20", "19", "18", "17", "16", "15", "14", "13", "12", "11", "10", "9 ", "8 ", "7 ", "6 "};
        intPitchRangeFromPicker.setDisplayedValues(arFromInts);
        intPitchRangeFromPicker.setMaxValue(21);
        intPitchRangeFromPicker.setMinValue(0);
        intPitchRangeFromPicker.setWrapSelectorWheel(false);
        intPitchRangeFromPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int nOldValue, int nNewValue) {
                int nMaxPitch = Integer.parseInt(arFromInts[nNewValue].trim());
                mActivity.controlFragment.setMaxPitch(nMaxPitch);
                SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                preferences.edit().putInt("nMaxPitch", nMaxPitch).apply();
            }
        });

        int nMaxPitch = mActivity.controlFragment.getMaxPitch();
        String strMaxPitch;
        if(nMaxPitch >= 10)
            strMaxPitch = String.format(Locale.getDefault(), "%d", nMaxPitch);
        else
            strMaxPitch = String.format(Locale.getDefault(), "%d ", nMaxPitch);
        for(int i = 0; i < arFromInts.length; i++)
        {
            if(arFromInts[i].equals(strMaxPitch))
                intPitchRangeFromPicker.setValue(i);
        }

        final NumberPicker intPitchRangeToPicker = mActivity.findViewById(R.id.intPitchRangeToPicker);
        final String[] arToInts = {"60", "48", "36", "24", "23", "22", "21", "20", "19", "18", "17", "16", "15", "14", "13", "12", "11", "10", "9 ", "8 ", "7 ", "6 "};
        intPitchRangeToPicker.setDisplayedValues(arToInts);
        intPitchRangeToPicker.setMaxValue(21);
        intPitchRangeToPicker.setMinValue(0);
        intPitchRangeToPicker.setWrapSelectorWheel(false);
        intPitchRangeToPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int nOldValue, int nNewValue) {
                int nMinPitch = Integer.parseInt(arToInts[nNewValue].trim()) * -1;
                mActivity.controlFragment.setMinPitch(nMinPitch);
                SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                preferences.edit().putInt("nMinPitch", nMinPitch).apply();
            }
        });

        int nMinPitch = mActivity.controlFragment.getMinPitch() * -1;
        String strMinPitch;
        if(nMinPitch >= 10)
            strMinPitch = String.format(Locale.getDefault(), "%d", nMinPitch);
        else
            strMinPitch = String.format(Locale.getDefault(), "%d ", nMinPitch);
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

        if(mActivity.isDarkMode()) {
            RelativeLayout relativePitchRangeScreen = mActivity.findViewById(R.id.relativePitchRangeScreen);
            RelativeLayout relativePitchRangeSettingTitle = mActivity.findViewById(R.id.relativePitchRangeSettingTitle);
            ImageView imgBackPitchRange = mActivity.findViewById(R.id.imgBackPitchRange);
            TextView textPitchRangeSettingTitle = mActivity.findViewById(R.id.textPitchRangeSettingTitle);
            View viewSepPitchRangeSetting = mActivity.findViewById(R.id.viewSepPitchRangeSetting);
            LinearLayout linearPitchRangeSetting = mActivity.findViewById(R.id.linearPitchRangeSetting);
            TextView textSharp = mActivity.findViewById(R.id.textSharp);
            TextView textPitchRangeSign = mActivity.findViewById(R.id.textPitchRangeSign);
            TextView textFlat = mActivity.findViewById(R.id.textFlat);
            Button btnResetPitchRange = mActivity.findViewById(R.id.btnResetPitchRange);

            relativePitchRangeScreen.setBackgroundColor(getResources().getColor(R.color.darkModeBk));
            relativePitchRangeSettingTitle.setBackgroundColor(getResources().getColor(R.color.darkModeBk));
            imgBackPitchRange.setImageDrawable(getResources().getDrawable(R.drawable.ic_button_back_dark));
            btnReturnPitchRangeSetting.setTextColor(getResources().getColor(R.color.darkModeBlue));
            btnClosePitchRangeSetting.setTextColor(getResources().getColor(R.color.darkModeBlue));
            textPitchRangeSettingTitle.setTextColor(getResources().getColor(android.R.color.white));
            viewSepPitchRangeSetting.setBackgroundColor(getResources().getColor(R.color.darkModeSep));
            linearPitchRangeSetting.setBackgroundColor(getResources().getColor(R.color.darkModeLightBk));
            setNumberPickerTextColor(intPitchRangeFromPicker, Color.WHITE);
            setDividerColor(intPitchRangeFromPicker, Color.rgb(38, 40, 44));
            setNumberPickerTextColor(intPitchRangeToPicker, Color.WHITE);
            setDividerColor(intPitchRangeToPicker, Color.rgb(38, 40, 44));
            textSharp.setTextColor(getResources().getColor(android.R.color.white));
            textPitchRangeSign.setTextColor(getResources().getColor(android.R.color.white));
            textFlat.setTextColor(getResources().getColor(android.R.color.white));
            btnResetPitchRange.setTextColor(getResources().getColorStateList(R.color.btn_text_dark));
            btnResetPitchRange.setBackgroundResource(R.drawable.resetbutton_dark);
        }
        else {
            setDividerColor(intPitchRangeFromPicker, Color.rgb(192, 192, 192));
            setDividerColor(intPitchRangeToPicker, Color.rgb(192, 192, 192));
        }
    }

    private static void setNumberPickerTextColor(NumberPicker numberPicker, int color)
    {
        try{
            Field selectorWheelPaintField = numberPicker.getClass()
                    .getDeclaredField("mSelectorWheelPaint");
            selectorWheelPaintField.setAccessible(true);
            ((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        final int count = numberPicker.getChildCount();
        for(int i = 0; i < count; i++){
            View child = numberPicker.getChildAt(i);
            if(child instanceof EditText)
                ((EditText)child).setTextColor(color);
        }
        numberPicker.invalidate();
    }

    private void setDividerColor(NumberPicker picker, int color) {
        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
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
