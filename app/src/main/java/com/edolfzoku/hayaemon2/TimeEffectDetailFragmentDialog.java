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
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.Locale;

public class TimeEffectDetailFragmentDialog extends DialogFragment
{
    private MainActivity mActivity = null;
    private NumberPicker mIntNumberPicker;
    private NumberPicker mDecimalNumberPicker;

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
            fTime = mActivity.effectFragment.getTimeOfIncreaseSpeed();
        else fTime = mActivity.effectFragment.getTimeOfDecreaseSpeed();
        int nInt = (int)fTime;
        int nDecimal = (int)((fTime - (float)nInt) * 10.0f + 0.05f);
        String strInt;
        if(nInt >= 10) strInt = String.format(Locale.getDefault(), "%d", nInt);
        else strInt = String.format(Locale.getDefault(), "%d ", nInt);
        String strDecimal = String.format(Locale.getDefault(), "%d", nDecimal);

        mIntNumberPicker = view.findViewById(R.id.intTimeEffectDetailPicker);
        final String[] arInts = {"10", "9 ", "8 ", "7 ", "6 ", "5 ", "4 ", "3 ", "2 ", "1 ", "0 "};
        final String[] arDecimals = {"9", "8", "7", "6", "5", "4", "3", "2", "1", "0"};
        mIntNumberPicker.setDisplayedValues(arInts);
        mIntNumberPicker.setMaxValue(10);
        mIntNumberPicker.setMinValue(0);
        mIntNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int nOldValue, int nNewValue) {
                String strTime = arInts[nNewValue].trim() + "." + arDecimals[mDecimalNumberPicker.getValue()];
                float fTime = Float.parseFloat(strTime);
                if(fTime < 0.1f) {
                    fTime = 0.1f;
                    numberPicker.setValue(10);
                    mDecimalNumberPicker.setValue(8);
                }

                TextView textEffectName = mActivity.findViewById(R.id.textEffectName);
                if(textEffectName.getText().toString().equals(getString(R.string.increaseSpeed)))
                    mActivity.effectFragment.setTimeOfIncreaseSpeed(fTime);
                else mActivity.effectFragment.setTimeOfDecreaseSpeed(fTime);
            }
        });
        for(int i = 0; i < arInts.length; i++)
        {
            if(arInts[i].equals(strInt))
                mIntNumberPicker.setValue(i);
        }

        mDecimalNumberPicker = view.findViewById(R.id.decimalTimeEffectDetailPicker);
        mDecimalNumberPicker.setDisplayedValues(arDecimals);
        mDecimalNumberPicker.setMaxValue(9);
        mDecimalNumberPicker.setMinValue(0);
        mDecimalNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int nOldValue, int nNewValue) {
                String strTime = arInts[mIntNumberPicker.getValue()].trim() + "." + arDecimals[nNewValue];
                float fTime = Float.parseFloat(strTime);
                if(fTime < 0.1f) {
                    fTime = 0.1f;
                    mIntNumberPicker.setValue(10);
                    numberPicker.setValue(8);
                }

                TextView textEffectName = mActivity.findViewById(R.id.textEffectName);
                if(textEffectName.getText().toString().equals(getString(R.string.increaseSpeed)))
                    mActivity.effectFragment.setTimeOfIncreaseSpeed(fTime);
                else mActivity.effectFragment.setTimeOfDecreaseSpeed(fTime);
            }
        });
        for(int i = 0; i < arDecimals.length; i++)
        {
            if(arDecimals[i].equals(strDecimal))
                mDecimalNumberPicker.setValue(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
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
        return alertDialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mActivity.effectFragment.clearFocus();
    }
}
