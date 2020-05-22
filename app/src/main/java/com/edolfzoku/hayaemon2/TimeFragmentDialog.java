package com.edolfzoku.hayaemon2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.lang.reflect.Field;
import java.util.Locale;

import static com.edolfzoku.hayaemon2.MainActivity.sLoopAPos;
import static com.edolfzoku.hayaemon2.MainActivity.sLoopBPos;
import static com.edolfzoku.hayaemon2.MainActivity.sLength;

public class TimeFragmentDialog extends BottomSheetDialog implements NumberPicker.OnValueChangeListener, NumberPicker.OnScrollListener {
    private MainActivity mActivity;
    private int mScreen;
    private NumberPicker mHourTimePicker;
    private NumberPicker mMinuteTimePicker;
    private NumberPicker mSecondTimePicker;
    private NumberPicker mDecTimePicker;
    private int mScrollState;

    private String[] arInts = {"59", "58", "57", "56", "55", "54", "53", "52", "51", "50", "49", "48", "47", "46", "45", "44", "43", "42", "41", "40", "39", "38", "37", "36", "35", "34", "33", "32", "31", "30", "29", "28", "27", "26", "25", "24", "23", "22", "21", "20", "19", "18", "17", "16", "15", "14", "13", "12", "11", "10", "09", "08", "07", "06", "05", "04", "03", "02", "01", "00"};
    private String[] arDecs = {"99", "98", "97", "96", "95", "94", "93", "92", "91", "90", "89", "88", "87", "86", "85", "84", "83", "82", "81", "80", "79", "78", "77", "76", "75", "74", "73", "72", "71", "70", "69", "68", "67", "66", "65", "64", "63", "62", "61", "60", "59", "58", "57", "56", "55", "54", "53", "52", "51", "50", "49", "48", "47", "46", "45", "44", "43", "42", "41", "40", "39", "38", "37", "36", "35", "34", "33", "32", "31", "30", "29", "28", "27", "26", "25", "24", "23", "22", "21", "20", "19", "18", "17", "16", "15", "14", "13", "12", "11", "10", "09", "08", "07", "06", "05", "04", "03", "02", "01", "00"};
    final static int SCREENTYPE_TIME_A = 0;
    final static int SCREENTYPE_TIME_B = 1;
    final static int SCREENTYPE_TIME_CUR = 2;

    @SuppressLint("ClickableViewAccessibility")
    TimeFragmentDialog(@NonNull Context context, int screen) {
        super(context);
        mActivity = (MainActivity)context;
        View view = getLayoutInflater().inflate(R.layout.dialog_time, null);

        mScreen = screen;

        String strHour, strMinute, strSecond, strDec;
        if (mScreen == SCREENTYPE_TIME_A || mScreen == SCREENTYPE_TIME_B) {
            double dValue;
            if (mScreen == SCREENTYPE_TIME_A) dValue = sLoopAPos;
            else {
                dValue = sLoopBPos;
                if (dValue == 0.0) dValue = sLength;
            }
            int nMinute = (int) (dValue / 60);
            int nSecond = (int) (dValue % 60);
            int nHour = nMinute / 60;
            nMinute = nMinute % 60;
            int nDec = (int) ((dValue * 100) % 100);
            strHour = String.format(Locale.getDefault(), "%02d", nHour);
            strMinute = String.format(Locale.getDefault(), "%02d", nMinute);
            strSecond = String.format(Locale.getDefault(), "%02d", nSecond);
            strDec = String.format(Locale.getDefault(), "%02d", nDec);
        }
        else {
            String strCurPos = mActivity.loopFragment.getTextCurValue().getText().toString();
            strHour = strCurPos.substring(0, 2);
            strMinute = strCurPos.substring(3, 5);
            strSecond = strCurPos.substring(6, 8);
            strDec = strCurPos.substring(9, 11);
        }

        mHourTimePicker = view.findViewById(R.id.hourTimePicker);
        mMinuteTimePicker = view.findViewById(R.id.minuteTimePicker);
        mSecondTimePicker = view.findViewById(R.id.secondTimePicker);
        mDecTimePicker = view.findViewById(R.id.decTimePicker);

        mHourTimePicker.setDisplayedValues(arInts);
        mMinuteTimePicker.setDisplayedValues(arInts);
        mSecondTimePicker.setDisplayedValues(arInts);
        mDecTimePicker.setDisplayedValues(arDecs);

        mHourTimePicker.setMaxValue(59);
        mMinuteTimePicker.setMaxValue(59);
        mSecondTimePicker.setMaxValue(59);
        mDecTimePicker.setMaxValue(99);

        mHourTimePicker.setWrapSelectorWheel(false);
        mMinuteTimePicker.setWrapSelectorWheel(false);
        mSecondTimePicker.setWrapSelectorWheel(false);
        mDecTimePicker.setWrapSelectorWheel(false);

        for(int i = 0; i < arInts.length; i++) {
            if(arInts[i].equals(strHour)) mHourTimePicker.setValue(i);
            if(arInts[i].equals(strMinute)) mMinuteTimePicker.setValue(i);
            if(arInts[i].equals(strSecond)) mSecondTimePicker.setValue(i);
        }
        for(int i = 0; i < arDecs.length; i++) {
            if(arDecs[i].equals(strDec)) mDecTimePicker.setValue(i);
        }

        mHourTimePicker.setOnScrollListener(this);
        mMinuteTimePicker.setOnScrollListener(this);
        mSecondTimePicker.setOnScrollListener(this);
        mDecTimePicker.setOnScrollListener(this);

        setContentView(view);
        if(getWindow() != null) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.dimAmount = 0.0f;
            getWindow().setAttributes(lp);
        }

        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                mActivity.loopFragment.clearFocus();
            }
        });

        if(mActivity.isDarkMode()) {
            mHourTimePicker.setBackgroundColor(view.getResources().getColor(R.color.darkModeLightBk));
            mMinuteTimePicker.setBackgroundColor(view.getResources().getColor(R.color.darkModeLightBk));
            mSecondTimePicker.setBackgroundColor(view.getResources().getColor(R.color.darkModeLightBk));
            mDecTimePicker.setBackgroundColor(view.getResources().getColor(R.color.darkModeLightBk));
            setNumberPickerTextColor(mHourTimePicker, Color.WHITE);
            setNumberPickerTextColor(mMinuteTimePicker, Color.WHITE);
            setNumberPickerTextColor(mSecondTimePicker, Color.WHITE);
            setNumberPickerTextColor(mDecTimePicker, Color.WHITE);
            setDividerColor(mHourTimePicker, Color.rgb(38, 40, 44));
            setDividerColor(mMinuteTimePicker, Color.rgb(38, 40, 44));
            setDividerColor(mSecondTimePicker, Color.rgb(38, 40, 44));
            setDividerColor(mDecTimePicker, Color.rgb(38, 40, 44));
        }
        else {
            setDividerColor(mHourTimePicker, Color.rgb(192, 192, 192));
            setDividerColor(mMinuteTimePicker, Color.rgb(192, 192, 192));
            setDividerColor(mSecondTimePicker, Color.rgb(192, 192, 192));
            setDividerColor(mDecTimePicker, Color.rgb(192, 192, 192));
        }
    }

    @Override
    public void onScrollStateChange(NumberPicker numberPicker, int scrollState) {
        mScrollState = scrollState;
        if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) updateValue();
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        if (mScrollState == SCROLL_STATE_IDLE) updateValue();
    }

    private void updateValue() {
        int nHour = Integer.parseInt(arInts[mHourTimePicker.getValue()]);
        int nMinute = Integer.parseInt(arInts[mMinuteTimePicker.getValue()]);
        int nSecond = Integer.parseInt(arInts[mSecondTimePicker.getValue()]);
        double dDec = Double.parseDouble(arDecs[mDecTimePicker.getValue()]);
        double dTime = nHour * 3600 + nMinute * 60 + nSecond + dDec / 100.0;
        if (mScreen == SCREENTYPE_TIME_A) LoopFragment.setLoopA(dTime);
        else if (mScreen == SCREENTYPE_TIME_B) LoopFragment.setLoopB(dTime);
        else mActivity.loopFragment.setCurPos(dTime);
    }

    private static void setNumberPickerTextColor(NumberPicker numberPicker, int color) {
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
}
