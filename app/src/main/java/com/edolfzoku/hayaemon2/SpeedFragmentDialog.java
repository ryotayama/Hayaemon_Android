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

public class SpeedFragmentDialog extends DialogFragment {
    private MainActivity activity = null;
    NumberPicker intNumberPicker;
    NumberPicker decimalNumberPicker;

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
        ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(2);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.speedpicker, (ViewGroup)activity.findViewById(R.id.layout_root), false);
        float fSpeed = controlFragment.fSpeed + 100;
        int nIntSpeed = (int)fSpeed;
        int nDecimalSpeed = (int)((fSpeed - (float)nIntSpeed) * 10.0f + 0.05f);
        String strIntSpeed;
        if(nIntSpeed < 100)
            strIntSpeed = String.format(Locale.getDefault(), "%d ", nIntSpeed);
        else
            strIntSpeed = String.format(Locale.getDefault(), "%d", nIntSpeed);
        String strDecimalSpeed = String.format(Locale.getDefault(), "%d", nDecimalSpeed);

        intNumberPicker = view.findViewById(R.id.intSpeedPicker);
        final String[] arInts = {"400", "399", "398", "397", "396", "395", "394", "393", "392", "391", "390", "389", "388", "387", "386", "385", "384", "383", "382", "381", "380", "379", "378", "377", "376", "375", "374", "373", "372", "371", "370", "369", "368", "367", "366", "365", "364", "363", "362", "361", "360", "359", "358", "357", "356", "355", "354", "353", "352", "351", "350", "349", "348", "347", "346", "345", "344", "343", "342", "341", "340", "339", "338", "337", "336", "335", "334", "333", "332", "331", "330", "329", "328", "327", "326", "325", "324", "323", "322", "321", "320", "319", "318", "317", "316", "315", "314", "313", "312", "311", "310", "309", "308", "307", "306", "305", "304", "303", "302", "301", "300", "299", "298", "297", "296", "295", "294", "293", "292", "291", "290", "289", "288", "287", "286", "285", "284", "283", "282", "281", "280", "279", "278", "277", "276", "275", "274", "273", "272", "271", "270", "269", "268", "267", "266", "265", "264", "263", "262", "261", "260", "259", "258", "257", "256", "255", "254", "253", "252", "251", "250", "249", "248", "247", "246", "245", "244", "243", "242", "241", "240", "239", "238", "237", "236", "235", "234", "233", "232", "231", "230", "229", "228", "227", "226", "225", "224", "223", "222", "221", "220", "219", "218", "217", "216", "215", "214", "213", "212", "211", "210", "209", "208", "207", "206", "205", "204", "203", "202", "201", "200", "199", "198", "197", "196", "195", "194", "193", "192", "191", "190", "189", "188", "187", "186", "185", "184", "183", "182", "181", "180", "179", "178", "177", "176", "175", "174", "173", "172", "171", "170", "169", "168", "167", "166", "165", "164", "163", "162", "161", "160", "159", "158", "157", "156", "155", "154", "153", "152", "151", "150", "149", "148", "147", "146", "145", "144", "143", "142", "141", "140", "139", "138", "137", "136", "135", "134", "133", "132", "131", "130", "129", "128", "127", "126", "125", "124", "123", "122", "121", "120", "119", "118", "117", "116", "115", "114", "113", "112", "111", "110", "109", "108", "107", "106", "105", "104", "103", "102", "101", "100", "99 ", "98 ", "97 ", "96 ", "95 ", "94 ", "93 ", "92 ", "91 ", "90 ", "89 ", "88 ", "87 ", "86 ", "85 ", "84 ", "83 ", "82 ", "81 ", "80 ", "79 ", "78 ", "77 ", "76 ", "75 ", "74 ", "73 ", "72 ", "71 ", "70 ", "69 ", "68 ", "67 ", "66 ", "65 ", "64 ", "63 ", "62 ", "61 ", "60 ", "59 ", "58 ", "57 ", "56 ", "55 ", "54 ", "53 ", "52 ", "51 ", "50 ", "49 ", "48 ", "47 ", "46 ", "45 ", "44 ", "43 ", "42 ", "41 ", "40 ", "39 ", "38 ", "37 ", "36 ", "35 ", "34 ", "33 ", "32 ", "31 ", "30 ", "29 ", "28 ", "27 ", "26 ", "25 ", "24 ", "23 ", "22 ", "21 ", "20 ", "19 ", "18 ", "17 ", "16 ", "15 ", "14 ", "13 ", "12 ", "11 ", "10 "};
        intNumberPicker.setDisplayedValues(arInts);
        intNumberPicker.setMaxValue(390);
        intNumberPicker.setMinValue(0);
        for(int i = 0; i < arInts.length; i++)
        {
            if(arInts[i].equals(strIntSpeed))
                intNumberPicker.setValue(i);
        }

        decimalNumberPicker = view.findViewById(R.id.decimalSpeedPicker);
        final String[] arDecimals = {"9", "8", "7", "6", "5", "4", "3", "2", "1", "0"};
        decimalNumberPicker.setDisplayedValues(arDecimals);
        decimalNumberPicker.setMaxValue(9);
        decimalNumberPicker.setMinValue(0);
        for(int i = 0; i < arDecimals.length; i++)
        {
            if(arDecimals[i].equals(strDecimalSpeed))
                decimalNumberPicker.setValue(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("速度の調整");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                LayoutInflater inflater = activity.getLayoutInflater();
                inflater.inflate(R.layout.speedpicker, (ViewGroup)activity.findViewById(R.id.layout_root), false);
                String strSpeed = arInts[intNumberPicker.getValue()].trim() + "." + arDecimals[decimalNumberPicker.getValue()];
                float fSpeed = Float.parseFloat(strSpeed);
                fSpeed -= 100;

                ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(2);
                controlFragment.setSpeed(fSpeed);
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