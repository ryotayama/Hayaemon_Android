package com.edolfzoku.hayaemon2;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.Locale;

public class TimeEffectDetailFragmentDialog extends DialogFragment
{
    private MainActivity activity = null;
    private NumberPicker intNumberPicker;
    private NumberPicker decimalNumberPicker;

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
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.timeeffectdetailpicker, (ViewGroup)activity.findViewById(R.id.layout_root), false);
        float fTime;
        TextView textEffectName = activity.findViewById(R.id.textEffectName);
        if(textEffectName.getText().toString().equals(getString(R.string.increaseSpeed)))
            fTime = activity.effectFragment.getTimeOfIncreaseSpeed();
        else fTime = activity.effectFragment.getTimeOfDecreaseSpeed();
        int nInt = (int)fTime;
        int nDecimal = (int)((fTime - (float)nInt) * 10.0f + 0.05f);
        String strInt;
        if(nInt >= 10) strInt = String.format(Locale.getDefault(), "%d", nInt);
        else strInt = String.format(Locale.getDefault(), "%d ", nInt);
        String strDecimal = String.format(Locale.getDefault(), "%d", nDecimal);

        intNumberPicker = view.findViewById(R.id.intTimeEffectDetailPicker);
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

                TextView textEffectName = activity.findViewById(R.id.textEffectName);
                if(textEffectName.getText().toString().equals(getString(R.string.increaseSpeed)))
                    activity.effectFragment.setTimeOfIncreaseSpeed(fTime);
                else activity.effectFragment.setTimeOfDecreaseSpeed(fTime);
            }
        });
        for(int i = 0; i < arInts.length; i++)
        {
            if(arInts[i].equals(strInt))
                intNumberPicker.setValue(i);
        }

        decimalNumberPicker = view.findViewById(R.id.decimalTimeEffectDetailPicker);
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

                TextView textEffectName = activity.findViewById(R.id.textEffectName);
                if(textEffectName.getText().toString().equals(getString(R.string.increaseSpeed)))
                    activity.effectFragment.setTimeOfIncreaseSpeed(fTime);
                else activity.effectFragment.setTimeOfDecreaseSpeed(fTime);
            }
        });
        for(int i = 0; i < arDecimals.length; i++)
        {
            if(arDecimals[i].equals(strDecimal))
                decimalNumberPicker.setValue(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.adjustTime);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.effectFragment.clearFocus();
            }
        });
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        activity.effectFragment.clearFocus();
    }
}
