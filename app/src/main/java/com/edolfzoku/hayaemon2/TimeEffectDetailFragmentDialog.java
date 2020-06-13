package com.edolfzoku.hayaemon2;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.Locale;

public class TimeEffectDetailFragmentDialog extends DialogFragment
{
    private MainActivity mActivity = null;
    private NumberPicker mIntNumberPicker;
    private NumberPicker mDecNumberPicker;

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
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View view = inflater.inflate(R.layout.timeeffectdetailpicker, (ViewGroup)mActivity.findViewById(R.id.layout_root), false);
        float fTime;
        TextView textEffectName = mActivity.findViewById(R.id.textEffectName);
        if(textEffectName.getText().toString().equals(getString(R.string.increaseSpeed)))
            fTime = EffectFragment.sTimeOfIncreaseSpeedSpecified;
        else if(textEffectName.getText().toString().equals(getString(R.string.decreaseSpeed)))
            fTime = EffectFragment.sTimeOfDecreaseSpeedSpecified;
        else if(textEffectName.getText().toString().equals(getString(R.string.raisePitch)))
            fTime = EffectFragment.sTimeOfRaisePitchSpecified;
        else
            fTime = EffectFragment.sTimeOfLowerPitchSpecified;
        int nInt = (int)fTime;
        int nDec = (int)((fTime - (float)nInt) * 10.0f + 0.05f);
        String strInt;
        if(nInt >= 10) strInt = String.format(Locale.getDefault(), "%d", nInt);
        else strInt = String.format(Locale.getDefault(), "%d ", nInt);
        String strDec = String.format(Locale.getDefault(), "%d", nDec);

        mIntNumberPicker = view.findViewById(R.id.intTimeEffectDetailPicker);
        final String[] arInts = {"10", "9 ", "8 ", "7 ", "6 ", "5 ", "4 ", "3 ", "2 ", "1 ", "0 "};
        final String[] arDecs = {"9", "8", "7", "6", "5", "4", "3", "2", "1", "0"};
        mIntNumberPicker.setDisplayedValues(arInts);
        mIntNumberPicker.setMaxValue(10);
        mIntNumberPicker.setMinValue(0);
        mIntNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int nOldValue, int nNewValue) {
                String strTime = arInts[nNewValue].trim() + "." + arDecs[mDecNumberPicker.getValue()];
                float fTime = Float.parseFloat(strTime);
                if(fTime < 0.1f) {
                    fTime = 0.1f;
                    numberPicker.setValue(10);
                    mDecNumberPicker.setValue(8);
                }

                TextView textEffectName = mActivity.findViewById(R.id.textEffectName);
                if(textEffectName.getText().toString().equals(getString(R.string.increaseSpeed)))
                    mActivity.effectFragment.setTimeOfIncreaseSpeed(fTime);
                else if(textEffectName.getText().toString().equals(getString(R.string.decreaseSpeed)))
                    mActivity.effectFragment.setTimeOfDecreaseSpeed(fTime);
                else if(textEffectName.getText().toString().equals(getString(R.string.raisePitch)))
                    mActivity.effectFragment.setTimeOfRaisePitch(fTime);
                else
                    mActivity.effectFragment.setTimeOfLowerPitch(fTime);
            }
        });
        for(int i = 0; i < arInts.length; i++)
        {
            if(arInts[i].equals(strInt))
                mIntNumberPicker.setValue(i);
        }

        mDecNumberPicker = view.findViewById(R.id.decTimeEffectDetailPicker);
        mDecNumberPicker.setDisplayedValues(arDecs);
        mDecNumberPicker.setMaxValue(9);
        mDecNumberPicker.setMinValue(0);
        mDecNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int nOldValue, int nNewValue) {
                String strTime = arInts[mIntNumberPicker.getValue()].trim() + "." + arDecs[nNewValue];
                float fTime = Float.parseFloat(strTime);
                if(fTime < 0.1f) {
                    fTime = 0.1f;
                    mIntNumberPicker.setValue(10);
                    numberPicker.setValue(8);
                }

                TextView textEffectName = mActivity.findViewById(R.id.textEffectName);
                if(textEffectName.getText().toString().equals(getString(R.string.increaseSpeed)))
                    mActivity.effectFragment.setTimeOfIncreaseSpeed(fTime);
                else if(textEffectName.getText().toString().equals(getString(R.string.decreaseSpeed)))
                    mActivity.effectFragment.setTimeOfDecreaseSpeed(fTime);
                else if(textEffectName.getText().toString().equals(getString(R.string.raisePitch)))
                    mActivity.effectFragment.setTimeOfRaisePitch(fTime);
                else
                    mActivity.effectFragment.setTimeOfLowerPitch(fTime);
            }
        });
        for(int i = 0; i < arDecs.length; i++)
        {
            if(arDecs[i].equals(strDec))
                mDecNumberPicker.setValue(i);
        }

        AlertDialog.Builder builder;
        if(mActivity.isDarkMode())
            builder = new AlertDialog.Builder(mActivity, R.style.DarkModeDialog);
        else
            builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.adjustTime);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mActivity.effectFragment.clearFocus();
            }
        });
        builder.setView(view);
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
            }
        });
        if(mActivity.isDarkMode()) {
            setNumberPickerTextColor(mIntNumberPicker, Color.WHITE);
            setNumberPickerTextColor(mDecNumberPicker, Color.WHITE);
            setDividerColor(mIntNumberPicker, Color.rgb(38, 40, 44));
            setDividerColor(mDecNumberPicker, Color.rgb(38, 40, 44));
        }
        return alertDialog;
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
    public void onCancel(DialogInterface dialog) {
        mActivity.effectFragment.clearFocus();
    }
}
