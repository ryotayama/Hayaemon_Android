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
        View view = inflater.inflate(R.layout.pitchpicker, (ViewGroup)mActivity.findViewById(R.id.layout_root), false);

        float fPitch = mActivity.controlFragment.getPitch();
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

        mIntNumberPicker = view.findViewById(R.id.intPitchPicker);
        final String[] arInts = {"♯60", "♯59", "♯58", "♯57", "♯56", "♯55", "♯54", "♯53", "♯52", "♯51", "♯50", "♯49", "♯48", "♯47", "♯46", "♯45", "♯44", "♯43", "♯42", "♯41", "♯40", "♯39", "♯38", "♯37", "♯36", "♯35", "♯34", "♯33", "♯32", "♯31", "♯30", "♯29", "♯28", "♯27", "♯26", "♯25", "♯24", "♯23", "♯22", "♯21", "♯20", "♯19", "♯18", "♯17", "♯16", "♯15", "♯14", "♯13", "♯12", "♯11", "♯10", "♯9 ", "♯8 ", "♯7 ", "♯6 ", "♯5 ", "♯4 ", "♯3 ", "♯2 ", "♯1 ", "♯0 ", "♭0 ", "♭1 ", "♭2 ", "♭3 ", "♭4 ", "♭5 ", "♭6 ", "♭7 ", "♭8 ", "♭9 ", "♭10", "♭11", "♭12", "♭13", "♭14", "♭15", "♭16", "♭17", "♭18", "♭19", "♭20", "♭21", "♭22", "♭23", "♭24", "♭25", "♭26", "♭27", "♭28", "♭29", "♭30", "♭31", "♭32", "♭33", "♭34", "♭35", "♭36", "♭37", "♭38", "♭39", "♭40", "♭41", "♭42", "♭43", "♭44", "♭45", "♭46", "♭47", "♭48", "♭49", "♭50", "♭51", "♭52", "♭53", "♭54", "♭55", "♭56", "♭57", "♭58", "♭59", "♭60"};
        mIntNumberPicker.setDisplayedValues(arInts);
        mIntNumberPicker.setMaxValue(121);
        mIntNumberPicker.setMinValue(0);
        mIntNumberPicker.setWrapSelectorWheel(false);
        for(int i = 0; i < arInts.length; i++)
        {
            if(arInts[i].equals(strIntPitch))
                mIntNumberPicker.setValue(i);
        }

        mDecimalNumberPicker = view.findViewById(R.id.decimalPitchPicker);
        final String[] arDecimals = {"9", "8", "7", "6", "5", "4", "3", "2", "1", "0"};
        mDecimalNumberPicker.setDisplayedValues(arDecimals);
        mDecimalNumberPicker.setMaxValue(9);
        mDecimalNumberPicker.setMinValue(0);
        mDecimalNumberPicker.setWrapSelectorWheel(false);
        for(int i = 0; i < arDecimals.length; i++)
        {
            if(arDecimals[i].equals(strDecimalPitch))
                mDecimalNumberPicker.setValue(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.adjustPitch);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strInt = arInts[mIntNumberPicker.getValue()];
                strInt = strInt.replace("♯", "");
                strInt = strInt.replace("♭", "-");
                String strDecimal = arDecimals[mDecimalNumberPicker.getValue()];
                String strPitch = strInt.trim() + "." + strDecimal;
                float fPitch = Float.parseFloat(strPitch);

                mActivity.controlFragment.setPitch(fPitch);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mActivity.controlFragment.clearFocus();
            }
        });
        builder.setView(view);
        return builder.create();
    }
    @Override
    public void onCancel(DialogInterface dialog) {
        mActivity.controlFragment.clearFocus();
    }
}
