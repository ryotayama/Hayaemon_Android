package com.edolfzoku.hayaemon2;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

public class SpeedEffectDetailFragmentDialog extends DialogFragment {
    NumberPicker intNumberPicker;
    NumberPicker decimalNumberPicker;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainActivity activity = (MainActivity)getActivity();
        EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.speedeffectdetailpicker, null, false);
        float fTime = effectFragment.getIncreaseSpeed();
        int nInt = (int)fTime;
        int nDecimal = (int)((fTime - (float)nInt) * 10.0f + 0.05f);
        String strInt = null;
        if(nInt >= 10) strInt = String.format("%d", nInt);
        else strInt = String.format("%d ", nInt);
        String strDecimal = String.format("%d", nDecimal);

        intNumberPicker = view.findViewById(R.id.intSpeedEffectDetailPicker);
        final String[] arInts = {"10", "9 ", "8 ", "7 ", "6 ", "5 ", "4 ", "3 ", "2 ", "1 ", "0 "};
        final String[] arDecimals = {"9", "8", "7", "6", "5", "4", "3", "2", "1", "0"};
        intNumberPicker.setDisplayedValues(arInts);
        intNumberPicker.setMaxValue(10);
        intNumberPicker.setMinValue(0);
        intNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int nOldValue, int nNewValue) {
                String strTime = arInts[nNewValue].trim() + "." + arDecimals[decimalNumberPicker.getValue()];
                float fTime = Float.parseFloat(strTime);
                if(fTime < 0.1f) {
                    fTime = 0.1f;
                    numberPicker.setValue(10);
                    decimalNumberPicker.setValue(8);
                }

                MainActivity activity = (MainActivity) getActivity();
                EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
                effectFragment.setIncreaseSpeed(fTime);
            }
        });
        for(int i = 0; i < arInts.length; i++)
        {
            if(arInts[i].equals(strInt))
                intNumberPicker.setValue(i);
        }

        decimalNumberPicker = (NumberPicker)view.findViewById(R.id.decimalSpeedEffectDetailPicker);
        decimalNumberPicker.setDisplayedValues(arDecimals);
        decimalNumberPicker.setMaxValue(9);
        decimalNumberPicker.setMinValue(0);
        decimalNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int nOldValue, int nNewValue) {
                String strTime = arInts[intNumberPicker.getValue()].trim() + "." + arDecimals[nNewValue];
                float fTime = Float.parseFloat(strTime);
                if(fTime < 0.1f) {
                    fTime = 0.1f;
                    intNumberPicker.setValue(10);
                    numberPicker.setValue(8);
                }

                MainActivity activity = (MainActivity) getActivity();
                EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
                effectFragment.setIncreaseSpeed(fTime);
            }
        });
        for(int i = 0; i < arDecimals.length; i++)
        {
            if(arDecimals[i].equals(strDecimal))
                decimalNumberPicker.setValue(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("速度の調整");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity activity = (MainActivity)getActivity();
                EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
                effectFragment.clearFocus();
            }
        });
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        MainActivity activity = (MainActivity) getActivity();
        EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
        effectFragment.clearFocus();
    }
}
