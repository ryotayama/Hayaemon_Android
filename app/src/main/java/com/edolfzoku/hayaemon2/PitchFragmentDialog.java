/*
 * PitchFragmentDialog
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

import java.util.Locale;

public class PitchFragmentDialog extends DialogFragment {
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
        View view = inflater.inflate(R.layout.pitchpicker, (ViewGroup)activity.findViewById(R.id.layout_root), false);

        ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(2);
        float fPitch = controlFragment.fPitch;
        int nIntPitch = (int)fPitch;
        int nDecimalPitch;
        if(fPitch >= 0.05) {
            nDecimalPitch = (int) ((fPitch - (float) nIntPitch) * 10.0f + 0.05f);
        }
        else {
            nDecimalPitch = (int) ((fPitch - (float) nIntPitch) * 10.0f - 0.05f);
        }
        String strIntPitch;
        String strDecimalPitch;
        if(fPitch >= 0.05) {
            if(nIntPitch < 10)
                strIntPitch = String.format(Locale.getDefault(), "♯%d ", nIntPitch);
            else
                strIntPitch = String.format(Locale.getDefault(), "♯%d", nIntPitch);
            strDecimalPitch = String.format(Locale.getDefault(), "%d", nDecimalPitch);
        }
        else {
            if(nIntPitch > -10)
                strIntPitch = String.format(Locale.getDefault(), "♭%d ", nIntPitch * -1);
            else
                strIntPitch = String.format(Locale.getDefault(), "♭%d", nIntPitch * -1);
            strDecimalPitch = String.format(Locale.getDefault(), "%d", nDecimalPitch * -1);
        }

        intNumberPicker = view.findViewById(R.id.intPitchPicker);
        final String[] arInts = {"♯24", "♯23", "♯22", "♯21", "♯20", "♯19", "♯18", "♯17", "♯16", "♯15", "♯14", "♯13", "♯12", "♯11", "♯10", "♯9 ", "♯8 ", "♯7 ", "♯6 ", "♯5 ", "♯4 ", "♯3 ", "♯2 ", "♯1 ", "♯0 ", "♭0 ", "♭1 ", "♭2 ", "♭3 ", "♭4 ", "♭5 ", "♭6 ", "♭7 ", "♭8 ", "♭9 ", "♭10", "♭11", "♭12", "♭13", "♭14", "♭15", "♭16", "♭17", "♭18", "♭19", "♭20", "♭21", "♭22", "♭23", "♭24"};
        intNumberPicker.setDisplayedValues(arInts);
        intNumberPicker.setMaxValue(49);
        intNumberPicker.setMinValue(0);
        for(int i = 0; i < arInts.length; i++)
        {
            if(arInts[i].equals(strIntPitch))
                intNumberPicker.setValue(i);
        }

        decimalNumberPicker = view.findViewById(R.id.decimalPitchPicker);
        final String[] arDecimals = {"9", "8", "7", "6", "5", "4", "3", "2", "1", "0"};
        decimalNumberPicker.setDisplayedValues(arDecimals);
        decimalNumberPicker.setMaxValue(9);
        decimalNumberPicker.setMinValue(0);
        for(int i = 0; i < arDecimals.length; i++)
        {
            if(arDecimals[i].equals(strDecimalPitch))
                decimalNumberPicker.setValue(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("音程の調整");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strInt = arInts[intNumberPicker.getValue()];
                strInt = strInt.replace("♯", "");
                strInt = strInt.replace("♭", "-");
                String strDecimal = arDecimals[decimalNumberPicker.getValue()];
                String strPitch = strInt.trim() + "." + strDecimal;
                float fPitch = Float.parseFloat(strPitch);

                ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(2);
                controlFragment.setPitch(fPitch);
            }
        });
        builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(2);
                controlFragment.clearFocus();
            }
        });
        builder.setView(view);
        return builder.create();
    }
    @Override
    public void onCancel(DialogInterface dialog) {
        ControlFragment controlFragment = (ControlFragment) activity.mSectionsPagerAdapter.getItem(2);
        controlFragment.clearFocus();
    }
}
