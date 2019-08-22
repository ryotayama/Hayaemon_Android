/*
 * SpeedFragmentDialog
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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.Locale;

class SpeedFragmentDialog extends BottomSheetDialog {
    private MainActivity mActivity;
    private NumberPicker mIntNumberPicker1;
    private NumberPicker mIntNumberPicker2;
    private NumberPicker mIntNumberPicker3;
    private NumberPicker mIntNumberPicker4;
    private NumberPicker mDecNumberPicker;

    private String[] arInts1 = {"5", "4", "3", "2", "1", "0"};
    private String[] arInts2 = {"9", "8", "7", "6", "5", "4", "3", "2", "1", "0"};
    private String[] arInts3 = {"9", "8", "7", "6", "5", "4", "3", "2", "1", "0"};
    private String[] arInts4 = {"9", "8", "7", "6", "5", "4", "3", "2", "1", "0"};
    private String[] arDecs = {"9", "8", "7", "6", "5", "4", "3", "2", "1", "0"};

    @SuppressLint("ClickableViewAccessibility")
    SpeedFragmentDialog(@NonNull Context context) {
        super(context);
        mActivity = (MainActivity)context;
        View view = getLayoutInflater().inflate(R.layout.dialog_speed, null);

        float fSpeed = mActivity.controlFragment.getSpeed() + 100;

        String strTemp = String.format(Locale.getDefault(), "%06.1f", fSpeed >= 0.0f ? fSpeed : -fSpeed);
        String strInt1 = strTemp.substring(0, 1);
        String strInt2 = strTemp.substring(1, 2);
        String strInt3 = strTemp.substring(2, 3);
        String strInt4 = strTemp.substring(3, 4);
        String strDec = strTemp.substring(5, 6);

        mIntNumberPicker1 = view.findViewById(R.id.intSpeedPicker1);
        mIntNumberPicker2 = view.findViewById(R.id.intSpeedPicker2);
        mIntNumberPicker3 = view.findViewById(R.id.intSpeedPicker3);
        mIntNumberPicker4 = view.findViewById(R.id.intSpeedPicker4);
        mDecNumberPicker = view.findViewById(R.id.decSpeedPicker);
        Button btnMinus10 = view.findViewById(R.id.btnMinus10);
        Button btnPlus10 = view.findViewById(R.id.btnPlus10);
        Button btnResetDialogSpeed = view.findViewById(R.id.btnResetDialogSpeed);
        Button btnDoneDialogSpeed = view.findViewById(R.id.btnDoneDialogSpeed);

        mIntNumberPicker1.setDisplayedValues(arInts1);
        mIntNumberPicker2.setDisplayedValues(arInts2);
        mIntNumberPicker3.setDisplayedValues(arInts3);
        mIntNumberPicker4.setDisplayedValues(arInts4);
        mDecNumberPicker.setDisplayedValues(arDecs);

        mIntNumberPicker1.setMaxValue(5);
        mIntNumberPicker2.setMaxValue(9);
        mIntNumberPicker3.setMaxValue(9);
        mIntNumberPicker4.setMaxValue(9);
        mDecNumberPicker.setMaxValue(9);

        mIntNumberPicker1.setWrapSelectorWheel(false);
        mIntNumberPicker2.setWrapSelectorWheel(false);
        mIntNumberPicker3.setWrapSelectorWheel(false);
        mIntNumberPicker4.setWrapSelectorWheel(false);
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
        for(int i = 0; i < arInts4.length; i++) {
            if(arInts4[i].equals(strInt4))
                mIntNumberPicker4.setValue(i);
        }
        for(int i = 0; i < arDecs.length; i++) {
            if(arDecs[i].equals(strDec))
                mDecNumberPicker.setValue(i);
        }

        btnMinus10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float fSpeed = mActivity.controlFragment.getSpeed() + 100;
                fSpeed -= 10.0f;
                if(fSpeed < 10.0f) fSpeed = 10.0f;
                setSpeed(fSpeed);
            }
        });
        btnPlus10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float fSpeed = mActivity.controlFragment.getSpeed() + 100;
                fSpeed += 10.0f;
                if(fSpeed > 5000.0f) fSpeed = 5000.0f;
                setSpeed(fSpeed);
            }
        });
        btnResetDialogSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSpeed(100.0f);
            }
        });
        btnDoneDialogSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        NumberPicker.OnValueChangeListener listener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                String strInt1 = arInts1[mIntNumberPicker1.getValue()];
                String strInt2 = arInts2[mIntNumberPicker2.getValue()];
                String strInt3 = arInts3[mIntNumberPicker3.getValue()];
                String strInt4 = arInts4[mIntNumberPicker4.getValue()];
                String strDec = arDecs[mDecNumberPicker.getValue()];
                String strSpeed = strInt1.trim() + strInt2.trim() + strInt3.trim() + strInt4.trim() + "." + strDec;
                float fSpeed = Float.parseFloat(strSpeed);
                if(fSpeed > 5000.0f) {
                    fSpeed = 5000.0f;
                    mIntNumberPicker1.setValue(0);
                    mIntNumberPicker2.setValue(9);
                    mIntNumberPicker3.setValue(9);
                    mIntNumberPicker4.setValue(9);
                    mDecNumberPicker.setValue(9);
                }
                else if(fSpeed < 10.0f) {
                    fSpeed = 10.0f;
                    mIntNumberPicker1.setValue(5);
                    mIntNumberPicker2.setValue(9);
                    mIntNumberPicker3.setValue(8);
                    mIntNumberPicker4.setValue(9);
                    mDecNumberPicker.setValue(9);
                }

                fSpeed -= 100.0f;
                mActivity.controlFragment.setSpeed(fSpeed);
            }
        };

        mIntNumberPicker1.setOnValueChangedListener(listener);
        mIntNumberPicker2.setOnValueChangedListener(listener);
        mIntNumberPicker3.setOnValueChangedListener(listener);
        mIntNumberPicker4.setOnValueChangedListener(listener);
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
            RelativeLayout relativeDialogSpeed = view.findViewById(R.id.relativeDialogSpeed);
            View viewSepDialogSpeed = view.findViewById(R.id.viewSepDialogSpeed);
            TextView textViewSpeedDot = view.findViewById(R.id.textViewSpeedDot);

            relativeDialogSpeed.setBackgroundColor(view.getResources().getColor(R.color.darkModeBk));
            viewSepDialogSpeed.setBackgroundColor(view.getResources().getColor(R.color.darkModeSep));
            btnMinus10.setTextColor(view.getResources().getColorStateList(R.color.btn_text_dark));
            btnPlus10.setTextColor(view.getResources().getColorStateList(R.color.btn_text_dark));
            btnResetDialogSpeed.setTextColor(view.getResources().getColorStateList(R.color.btn_text_dark));
            btnDoneDialogSpeed.setTextColor(view.getResources().getColorStateList(R.color.btn_text_dark));
            mIntNumberPicker1.setBackgroundColor(view.getResources().getColor(R.color.darkModeLightBk));
            mIntNumberPicker2.setBackgroundColor(view.getResources().getColor(R.color.darkModeLightBk));
            mIntNumberPicker3.setBackgroundColor(view.getResources().getColor(R.color.darkModeLightBk));
            mIntNumberPicker4.setBackgroundColor(view.getResources().getColor(R.color.darkModeLightBk));
            mDecNumberPicker.setBackgroundColor(view.getResources().getColor(R.color.darkModeLightBk));
            setNumberPickerTextColor(mIntNumberPicker1, Color.WHITE);
            setNumberPickerTextColor(mIntNumberPicker2, Color.WHITE);
            setNumberPickerTextColor(mIntNumberPicker3, Color.WHITE);
            setNumberPickerTextColor(mIntNumberPicker4, Color.WHITE);
            setNumberPickerTextColor(mDecNumberPicker, Color.WHITE);
            setDividerColor(mIntNumberPicker1, Color.rgb(38, 40, 44));
            setDividerColor(mIntNumberPicker2, Color.rgb(38, 40, 44));
            setDividerColor(mIntNumberPicker3, Color.rgb(38, 40, 44));
            setDividerColor(mIntNumberPicker4, Color.rgb(38, 40, 44));
            setDividerColor(mDecNumberPicker, Color.rgb(38, 40, 44));
            textViewSpeedDot.setTextColor(Color.WHITE);
        }
        else {
            setDividerColor(mIntNumberPicker1, Color.rgb(192, 192, 192));
            setDividerColor(mIntNumberPicker2, Color.rgb(192, 192, 192));
            setDividerColor(mIntNumberPicker3, Color.rgb(192, 192, 192));
            setDividerColor(mIntNumberPicker4, Color.rgb(192, 192, 192));
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

    public void setSpeed(float fSpeed) {
        String strTemp = String.format(Locale.getDefault(), "%06.1f", fSpeed >= 0.0f ? fSpeed : -fSpeed);
        String strInt1 = strTemp.substring(0, 1);
        String strInt2 = strTemp.substring(1, 2);
        String strInt3 = strTemp.substring(2, 3);
        String strInt4 = strTemp.substring(3, 4);
        String strDec = strTemp.substring(5, 6);

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
        fSpeed -= 100.0f;
        mActivity.controlFragment.setSpeed(fSpeed);
    }
}
