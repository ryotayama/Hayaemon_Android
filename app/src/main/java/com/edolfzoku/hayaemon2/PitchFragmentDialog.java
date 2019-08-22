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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.Locale;

class PitchFragmentDialog extends BottomSheetDialog {
    private MainActivity mActivity;
    private NumberPicker mIntNumberPicker1;
    private NumberPicker mIntNumberPicker2;
    private NumberPicker mIntNumberPicker3;
    private NumberPicker mDecNumberPicker;

    private String[] arInts1 = {"♯", "♭"};
    private String[] arInts2 = {"6", "5", "4", "3", "2", "1", "0"};
    private String[] arInts3 = {"9", "8", "7", "6", "5", "4", "3", "2", "1", "0"};
    private String[] arDecs = {"9", "8", "7", "6", "5", "4", "3", "2", "1", "0"};

    @SuppressLint("ClickableViewAccessibility")
    PitchFragmentDialog(@NonNull Context context) {
        super(context);
        mActivity = (MainActivity)context;
        View view = getLayoutInflater().inflate(R.layout.dialog_pitch, null);

        float fPitch = mActivity.controlFragment.getPitch();

        String flag;
        if(fPitch >= 0.0f) flag = "♯";
        else flag = "♭";

        String strTemp = String.format(Locale.getDefault(), "%s%04.1f", flag, fPitch >= 0.0f ? fPitch : -fPitch);
        String strInt1 = strTemp.substring(0, 1);
        String strInt2 = strTemp.substring(1, 2);
        String strInt3 = strTemp.substring(2, 3);
        String strDec = strTemp.substring(4, 5);

        mIntNumberPicker1 = view.findViewById(R.id.intPitchPicker1);
        mIntNumberPicker2 = view.findViewById(R.id.intPitchPicker2);
        mIntNumberPicker3 = view.findViewById(R.id.intPitchPicker3);
        mDecNumberPicker = view.findViewById(R.id.decPitchPicker);
        Button btnSharp12 = view.findViewById(R.id.btnSharp12);
        Button btnFlat12 = view.findViewById(R.id.btnFlat12);
        Button btnResetDialogPitch = view.findViewById(R.id.btnResetDialogPitch);
        Button btnDoneDialogPitch = view.findViewById(R.id.btnDoneDialogPitch);

        mIntNumberPicker1.setDisplayedValues(arInts1);
        mIntNumberPicker2.setDisplayedValues(arInts2);
        mIntNumberPicker3.setDisplayedValues(arInts3);
        mDecNumberPicker.setDisplayedValues(arDecs);

        mIntNumberPicker1.setMaxValue(1);
        mIntNumberPicker2.setMaxValue(6);
        mIntNumberPicker3.setMaxValue(9);
        mDecNumberPicker.setMaxValue(9);

        mIntNumberPicker1.setWrapSelectorWheel(false);
        mIntNumberPicker2.setWrapSelectorWheel(false);
        mIntNumberPicker3.setWrapSelectorWheel(false);
        mDecNumberPicker.setWrapSelectorWheel(false);

        for(int i = 0; i < arInts1.length; i++) {
            if(arInts1[i].equals(strInt1))
                mIntNumberPicker1.setValue(i);
        }
        for(int i = 0; i < arInts2.length; i++) {
            if(arInts2[i].equals(strInt2))
                mIntNumberPicker2.setValue(i);
        }
        for(int i = 0; i < arInts3.length; i++) {
            if(arInts3[i].equals(strInt3))
                mIntNumberPicker3.setValue(i);
        }
        for(int i = 0; i < arDecs.length; i++) {
            if(arDecs[i].equals(strDec))
                mDecNumberPicker.setValue(i);
        }

        btnSharp12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float fPitch = mActivity.controlFragment.getPitch();
                fPitch += 12.0f;
                if(fPitch > 60.0f) fPitch = 60.0f;
                setPitch(fPitch);
            }
        });
        btnFlat12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float fPitch = mActivity.controlFragment.getPitch();
                fPitch -= 12.0f;
                if(fPitch < -60.0f) fPitch = -60.0f;
                setPitch(fPitch);
            }
        });
        btnResetDialogPitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPitch(0.0f);
            }
        });
        btnDoneDialogPitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        NumberPicker.OnValueChangeListener listener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                String strInt1 = arInts1[mIntNumberPicker1.getValue()];
                strInt1 = strInt1.replace("♯", "");
                strInt1 = strInt1.replace("♭", "-");
                String strInt2 = arInts2[mIntNumberPicker2.getValue()];
                String strInt3 = arInts3[mIntNumberPicker3.getValue()];
                String strDec = arDecs[mDecNumberPicker.getValue()];
                String strPitch = strInt1.trim() + strInt2.trim() + strInt3.trim() + "." + strDec;
                float fPitch = Float.parseFloat(strPitch);
                if(fPitch > 60.0f) {
                    fPitch = 60.0f;
                    mIntNumberPicker1.setValue(0);
                    mIntNumberPicker2.setValue(0);
                    mIntNumberPicker3.setValue(9);
                    mDecNumberPicker.setValue(9);
                }
                else if(fPitch < -60.0f) {
                    fPitch = -60.0f;
                    mIntNumberPicker1.setValue(1);
                    mIntNumberPicker2.setValue(0);
                    mIntNumberPicker3.setValue(9);
                    mDecNumberPicker.setValue(9);
                }

                mActivity.controlFragment.setPitch(fPitch);
            }
        };

        mIntNumberPicker1.setOnValueChangedListener(listener);
        mIntNumberPicker2.setOnValueChangedListener(listener);
        mIntNumberPicker3.setOnValueChangedListener(listener);
        mDecNumberPicker.setOnValueChangedListener(listener);

        setContentView(view);
        if(getWindow() != null) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.dimAmount = 0.0f;
            getWindow().setAttributes(lp);
        }

        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                mActivity.controlFragment.clearFocus();
            }
        });

        if(mActivity.isDarkMode()) {
            RelativeLayout relativeDialogPitch = view.findViewById(R.id.relativeDialogPitch);
            View viewSepDialogPitch = view.findViewById(R.id.viewSepDialogPitch);
            TextView textViewPitchDot = view.findViewById(R.id.textViewPitchDot);

            relativeDialogPitch.setBackgroundColor(view.getResources().getColor(R.color.darkModeBk));
            viewSepDialogPitch.setBackgroundColor(view.getResources().getColor(R.color.darkModeSep));
            btnSharp12.setTextColor(view.getResources().getColorStateList(R.color.btn_text_dark));
            btnFlat12.setTextColor(view.getResources().getColorStateList(R.color.btn_text_dark));
            btnResetDialogPitch.setTextColor(view.getResources().getColorStateList(R.color.btn_text_dark));
            btnDoneDialogPitch.setTextColor(view.getResources().getColorStateList(R.color.btn_text_dark));
            mIntNumberPicker1.setBackgroundColor(view.getResources().getColor(R.color.darkModeLightBk));
            mIntNumberPicker2.setBackgroundColor(view.getResources().getColor(R.color.darkModeLightBk));
            mIntNumberPicker3.setBackgroundColor(view.getResources().getColor(R.color.darkModeLightBk));
            mDecNumberPicker.setBackgroundColor(view.getResources().getColor(R.color.darkModeLightBk));
            setNumberPickerTextColor(mIntNumberPicker1, Color.WHITE);
            setNumberPickerTextColor(mIntNumberPicker2, Color.WHITE);
            setNumberPickerTextColor(mIntNumberPicker3, Color.WHITE);
            setNumberPickerTextColor(mDecNumberPicker, Color.WHITE);
            setDividerColor(mIntNumberPicker1, Color.rgb(38, 40, 44));
            setDividerColor(mIntNumberPicker2, Color.rgb(38, 40, 44));
            setDividerColor(mIntNumberPicker3, Color.rgb(38, 40, 44));
            setDividerColor(mDecNumberPicker, Color.rgb(38, 40, 44));
            textViewPitchDot.setTextColor(Color.WHITE);
        }
        else {
            setDividerColor(mIntNumberPicker1, Color.rgb(192, 192, 192));
            setDividerColor(mIntNumberPicker2, Color.rgb(192, 192, 192));
            setDividerColor(mIntNumberPicker3, Color.rgb(192, 192, 192));
            setDividerColor(mDecNumberPicker, Color.rgb(192, 192, 192));
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

    public void setPitch(float fPitch) {
        String flag;
        if(fPitch >= 0.0f) flag = "♯";
        else flag = "♭";

        String strTemp = String.format(Locale.getDefault(), "%s%04.1f", flag, fPitch >= 0.0f ? fPitch : -fPitch);
        String strInt1 = strTemp.substring(0, 1);
        String strInt2 = strTemp.substring(1, 2);
        String strInt3 = strTemp.substring(2, 3);
        String strDec = strTemp.substring(4, 5);

        for(int i = 0; i < arInts1.length; i++) {
            if(arInts1[i].equals(strInt1))
                mIntNumberPicker1.setValue(i);
        }
        for(int i = 0; i < arInts2.length; i++) {
            if(arInts2[i].equals(strInt2))
                mIntNumberPicker2.setValue(i);
        }
        for(int i = 0; i < arInts3.length; i++) {
            if(arInts3[i].equals(strInt3))
                mIntNumberPicker3.setValue(i);
        }
        for(int i = 0; i < arDecs.length; i++) {
            if(arDecs[i].equals(strDec))
                mDecNumberPicker.setValue(i);
        }
        mActivity.controlFragment.setPitch(fPitch);
    }
}
