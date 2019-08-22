package com.edolfzoku.hayaemon2;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Locale;

public class SettingFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
{
    private MainActivity mActivity = null;
    private Switch mSwitchElegant;
    private Switch mSwitchPurple;
    private Switch mSwitchPinkCamper;
    private Switch mSwitchBlueCamper;
    private Switch mSwitchOrangeCamper;

    public SettingFragment()
    {
    }

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwitchElegant = mActivity.findViewById(R.id.switchElegant);
        mSwitchPurple = mActivity.findViewById(R.id.switchPurple);
        mSwitchPinkCamper = mActivity.findViewById(R.id.switchPinkCamper);
        mSwitchBlueCamper = mActivity.findViewById(R.id.switchBlueCamper);
        mSwitchOrangeCamper = mActivity.findViewById(R.id.switchOrangeCamper);

        Button btnCloseSetting = mActivity.findViewById(R.id.btnCloseSetting);
        btnCloseSetting.setOnClickListener(this);

        mActivity.findViewById(R.id.relativeSpeedRangeValue).setOnClickListener(this);
        TextView textSpeedRangeValue = mActivity.findViewById(R.id.textSpeedRangeValue);
        textSpeedRangeValue.setText(String.format(Locale.getDefault(), "%d%% ～ %d%%", mActivity.controlFragment.getMinSpeed(), mActivity.controlFragment.getMaxSpeed()));

        mActivity.findViewById(R.id.relativePitchRangeValue).setOnClickListener(this);
        TextView textPitchRangeValue = mActivity.findViewById(R.id.textPitchRangeValue);
        textPitchRangeValue.setText(String.format(Locale.getDefault(), "♯%d ～ ♭%d", mActivity.controlFragment.getMaxPitch(), mActivity.controlFragment.getMinPitch() * -1));

        Switch switchRepeat = mActivity.findViewById(R.id.switchRepeat);
        switchRepeat.setChecked(!mActivity.isPlayNextByBPos());
        switchRepeat.setOnCheckedChangeListener(this);

        Switch switchSnap = mActivity.findViewById(R.id.switchSnap);
        switchSnap.setChecked(mActivity.controlFragment.isSnap());
        switchSnap.setOnCheckedChangeListener(this);

        SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        boolean bPurplePurchased = preferences.getBoolean("unipointer_p", false);
        boolean bElegantPurchased = preferences.getBoolean("unipointer_e", false);
        boolean bPinkCamperPurchased = preferences.getBoolean("camperpointer_p", false);
        boolean bBlueCamperPurchased = preferences.getBoolean("camperpointer_b", false);
        boolean bOrangeCamperPurchased = preferences.getBoolean("camperpointer_o", false);
        if(bPurplePurchased || bElegantPurchased || bPinkCamperPurchased || bBlueCamperPurchased || bOrangeCamperPurchased) {
            RelativeLayout relativePurchaseSetting = mActivity.findViewById(R.id.relativePurchaseSetting);
            relativePurchaseSetting.setVisibility(View.VISIBLE);
            ImageView imgPoint = mActivity.findViewById(R.id.imgPoint);
            int nTag = 0;
            if(imgPoint.getTag() != null) nTag = (Integer)imgPoint.getTag();
            if(bPurplePurchased) {
                RelativeLayout relativePurple = mActivity.findViewById(R.id.relativePurple);
                relativePurple.setVisibility(View.VISIBLE);
                mSwitchPurple.setChecked(nTag == 1);
                mSwitchPurple.setOnCheckedChangeListener(this);
            }
            if(bElegantPurchased) {
                RelativeLayout relativeElegant = mActivity.findViewById(R.id.relativeElegant);
                relativeElegant.setVisibility(View.VISIBLE);
                mSwitchElegant.setChecked(nTag == 2);
                mSwitchElegant.setOnCheckedChangeListener(this);
            }
            if(bPinkCamperPurchased) {
                RelativeLayout relativePinkCamper = mActivity.findViewById(R.id.relativePinkCamper);
                relativePinkCamper.setVisibility(View.VISIBLE);
                mSwitchPinkCamper.setChecked(nTag == 3);
                mSwitchPinkCamper.setOnCheckedChangeListener(this);
            }
            if(bBlueCamperPurchased) {
                RelativeLayout relativeBlueCamper = mActivity.findViewById(R.id.relativeBlueCamper);
                relativeBlueCamper.setVisibility(View.VISIBLE);
                mSwitchBlueCamper.setChecked(nTag == 4);
                mSwitchBlueCamper.setOnCheckedChangeListener(this);
            }
            if(bOrangeCamperPurchased) {
                RelativeLayout relativeOrangeCamper = mActivity.findViewById(R.id.relativeOrangeCamper);
                relativeOrangeCamper.setVisibility(View.VISIBLE);
                mSwitchOrangeCamper.setChecked(nTag == 5);
                mSwitchOrangeCamper.setOnCheckedChangeListener(this);
            }
            View viewSepPurchasedHeader = mActivity.findViewById(R.id.viewSepPurchasedHeader);
            View viewDivider1 = mActivity.findViewById(R.id.viewDivider1);
            View viewDivider2 = mActivity.findViewById(R.id.viewDivider2);
            View viewDivider3 = mActivity.findViewById(R.id.viewDivider3);
            View viewDivider4 = mActivity.findViewById(R.id.viewDivider4);
            viewSepPurchasedHeader.setVisibility(bPurplePurchased ? View.VISIBLE : View.GONE);
            viewDivider1.setVisibility(bElegantPurchased ? View.VISIBLE : View.GONE);
            viewDivider2.setVisibility(bPinkCamperPurchased ? View.VISIBLE : View.GONE);
            viewDivider3.setVisibility(bBlueCamperPurchased ? View.VISIBLE : View.GONE);
            viewDivider4.setVisibility(bOrangeCamperPurchased ? View.VISIBLE : View.GONE);
        }

        if(mActivity.isDarkMode()) {
            RelativeLayout relativeSettingScreen = mActivity.findViewById(R.id.relativeSettingScreen);
            RelativeLayout relativeSettingTitle = mActivity.findViewById(R.id.relativeSettingTitle);
            RelativeLayout relativeRepeat = mActivity.findViewById(R.id.relativeRepeat);
            RelativeLayout relativeSpeedRange = mActivity.findViewById(R.id.relativeSpeedRange);
            RelativeLayout relativePitchRange = mActivity.findViewById(R.id.relativePitchRange);
            RelativeLayout relativeSnap = mActivity.findViewById(R.id.relativeSnap);
            RelativeLayout relativePurple = mActivity.findViewById(R.id.relativePurple);
            RelativeLayout relativeElegant = mActivity.findViewById(R.id.relativeElegant);
            RelativeLayout relativePinkCamper = mActivity.findViewById(R.id.relativePinkCamper);
            RelativeLayout relativeBlueCamper = mActivity.findViewById(R.id.relativeBlueCamper);
            RelativeLayout relativeOrangeCamper = mActivity.findViewById(R.id.relativeOrangeCamper);
            TextView textSettingTitle = mActivity.findViewById(R.id.textSettingTitle);
            TextView textPlayHeader = mActivity.findViewById(R.id.textPlayHeader);
            TextView textControlHeader = mActivity.findViewById(R.id.textControlHeader);
            TextView textPurchasedHeader = mActivity.findViewById(R.id.textPurchasedHeader);
            TextView textRepeat = mActivity.findViewById(R.id.textRepeat);
            TextView textSpeedRange = mActivity.findViewById(R.id.textSpeedRange);
            TextView textPitchRange = mActivity.findViewById(R.id.textPitchRange);
            TextView textSnap = mActivity.findViewById(R.id.textSnap);
            TextView textPurple = mActivity.findViewById(R.id.textPurple);
            TextView textElegant = mActivity.findViewById(R.id.textElegant);
            TextView textPinkCamper = mActivity.findViewById(R.id.textPinkCamper);
            TextView textBlueCamper = mActivity.findViewById(R.id.textBlueCamper);
            TextView textOrangeCamper = mActivity.findViewById(R.id.textOrangeCamper);
            View viewSepSetting = mActivity.findViewById(R.id.viewSepSetting);
            View viewSepPlayHeader = mActivity.findViewById(R.id.viewSepPlayHeader);
            View viewSepPlayFooter = mActivity.findViewById(R.id.viewSepPlayFooter);
            View viewSepControlHeader = mActivity.findViewById(R.id.viewSepControlHeader);
            View viewSepSpeedRange = mActivity.findViewById(R.id.viewSepSpeedRange);
            View viewSepPitchRange = mActivity.findViewById(R.id.viewSepPitchRange);
            View viewSepControlFooter = mActivity.findViewById(R.id.viewSepControlFooter);
            View viewSepPurchasedHeader = mActivity.findViewById(R.id.viewSepPurchasedHeader);
            View viewDivider1 = mActivity.findViewById(R.id.viewDivider1);
            View viewDivider2 = mActivity.findViewById(R.id.viewDivider2);
            View viewDivider3 = mActivity.findViewById(R.id.viewDivider3);
            View viewDivider4 = mActivity.findViewById(R.id.viewDivider4);
            View viewSepPurchasedFooter = mActivity.findViewById(R.id.viewSepPurchasedFooter);
            ImageView imgSpeedRangeRight = mActivity.findViewById(R.id.imgSpeedRangeRight);
            ImageView imgPitchRangeRight = mActivity.findViewById(R.id.imgPitchRangeRight);

            relativeSettingScreen.setBackgroundColor(getResources().getColor(R.color.darkModeLightBk));
            relativeSettingTitle.setBackgroundColor(getResources().getColor(R.color.darkModeBk));
            relativeRepeat.setBackgroundColor(getResources().getColor(R.color.darkModeBk));
            relativeSpeedRange.setBackgroundColor(getResources().getColor(R.color.darkModeBk));
            relativePitchRange.setBackgroundColor(getResources().getColor(R.color.darkModeBk));
            relativeSnap.setBackgroundColor(getResources().getColor(R.color.darkModeBk));
            relativePurple.setBackgroundColor(getResources().getColor(R.color.darkModeBk));
            relativeElegant.setBackgroundColor(getResources().getColor(R.color.darkModeBk));
            relativePinkCamper.setBackgroundColor(getResources().getColor(R.color.darkModeBk));
            relativeBlueCamper.setBackgroundColor(getResources().getColor(R.color.darkModeBk));
            relativeOrangeCamper.setBackgroundColor(getResources().getColor(R.color.darkModeBk));
            textSettingTitle.setTextColor(getResources().getColor(android.R.color.white));
            textPlayHeader.setTextColor(getResources().getColor(R.color.darkModeGray));
            textControlHeader.setTextColor(getResources().getColor(R.color.darkModeGray));
            textPurchasedHeader.setTextColor(getResources().getColor(R.color.darkModeGray));
            textRepeat.setTextColor(getResources().getColor(android.R.color.white));
            textSpeedRange.setTextColor(getResources().getColor(android.R.color.white));
            textPitchRange.setTextColor(getResources().getColor(android.R.color.white));
            textSnap.setTextColor(getResources().getColor(android.R.color.white));
            textPurple.setTextColor(getResources().getColor(android.R.color.white));
            textElegant.setTextColor(getResources().getColor(android.R.color.white));
            textPinkCamper.setTextColor(getResources().getColor(android.R.color.white));
            textBlueCamper.setTextColor(getResources().getColor(android.R.color.white));
            textOrangeCamper.setTextColor(getResources().getColor(android.R.color.white));
            textSpeedRangeValue.setTextColor(getResources().getColor(R.color.darkModeTextDarkGray));
            textPitchRangeValue.setTextColor(getResources().getColor(R.color.darkModeTextDarkGray));
            btnCloseSetting.setTextColor(getResources().getColor(R.color.darkModeBlue));
            viewSepSetting.setBackgroundColor(getResources().getColor(R.color.darkModeSep));
            viewSepPlayHeader.setBackgroundColor(getResources().getColor(R.color.darkModeSep));
            viewSepPlayFooter.setBackgroundColor(getResources().getColor(R.color.darkModeSep));
            viewSepControlHeader.setBackgroundColor(getResources().getColor(R.color.darkModeSep));
            viewSepSpeedRange.setBackgroundColor(getResources().getColor(R.color.darkModeSep));
            viewSepPitchRange.setBackgroundColor(getResources().getColor(R.color.darkModeSep));
            viewSepControlFooter.setBackgroundColor(getResources().getColor(R.color.darkModeSep));
            viewSepPurchasedHeader.setBackgroundColor(getResources().getColor(R.color.darkModeSep));
            viewDivider1.setBackgroundColor(getResources().getColor(R.color.darkModeSep));
            viewDivider2.setBackgroundColor(getResources().getColor(R.color.darkModeSep));
            viewDivider3.setBackgroundColor(getResources().getColor(R.color.darkModeSep));
            viewDivider4.setBackgroundColor(getResources().getColor(R.color.darkModeSep));
            viewSepPurchasedFooter.setBackgroundColor(getResources().getColor(R.color.darkModeSep));
            imgSpeedRangeRight.setImageResource(R.drawable.ic_button_listright_dark);
            imgPitchRangeRight.setImageResource(R.drawable.ic_button_listright_dark);
        }
    }

    @Override
    public void onClick(View view)
    {
        if(view.getId() == R.id.btnCloseSetting) {
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
            transaction.remove(this);
            transaction.commit();
        }
        else if(view.getId() == R.id.relativeSpeedRangeValue) {
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
            transaction.replace(R.id.relativeMain, new SpeedRangeSettingFragment());
            transaction.commit();
        }
        else if(view.getId() == R.id.relativePitchRangeValue) {
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
            transaction.replace(R.id.relativeMain, new PitchRangeSettingFragment());
            transaction.commit();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b)
    {
        SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        if(compoundButton.getId() == R.id.switchRepeat) {
            mActivity.setPlayNextByBPos(!b);
            preferences.edit().putBoolean("bPlayNextByBPos", mActivity.isPlayNextByBPos()).apply();
        }
        else if(compoundButton.getId() == R.id.switchSnap) {
            mActivity.controlFragment.setSnap(b);
            preferences.edit().putBoolean("bSnap", b).apply();
        }
        else if(compoundButton.getId() == R.id.switchPurple) {
            ImageView imgPoint = mActivity.controlFragment.getImgPoint();
            if(b) {
                mSwitchElegant.setChecked(false);
                mSwitchPinkCamper.setChecked(false);
                mSwitchBlueCamper.setChecked(false);
                mSwitchOrangeCamper.setChecked(false);
                imgPoint.setImageResource(R.drawable.control_pointer_uni_murasaki);
                imgPoint.setTag(1);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 1).apply();
            }
            else {
                imgPoint.setImageResource(R.drawable.ic_control_pointer);
                imgPoint.setTag(0);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 0).apply();
            }
            imgPoint.requestLayout();
        }
        else if(compoundButton.getId() == R.id.switchElegant) {
            ImageView imgPoint = mActivity.controlFragment.getImgPoint();
            if(b) {
                mSwitchPurple.setChecked(false);
                mSwitchPinkCamper.setChecked(false);
                mSwitchBlueCamper.setChecked(false);
                mSwitchOrangeCamper.setChecked(false);
                imgPoint.setImageResource(R.drawable.control_pointer_uni_bafun);
                imgPoint.setTag(2);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 2).apply();
            }
            else {
                imgPoint.setImageResource(R.drawable.ic_control_pointer);
                imgPoint.setTag(0);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 0).apply();
            }
            imgPoint.requestLayout();
        }
        else if(compoundButton.getId() == R.id.switchPinkCamper) {
            ImageView imgPoint = mActivity.controlFragment.getImgPoint();
            if(b) {
                mSwitchPurple.setChecked(false);
                mSwitchElegant.setChecked(false);
                mSwitchBlueCamper.setChecked(false);
                mSwitchOrangeCamper.setChecked(false);
                imgPoint.setImageResource(R.drawable.control_pointer_camper_pk);
                imgPoint.setTag(3);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 3).apply();
            }
            else {
                imgPoint.setImageResource(R.drawable.ic_control_pointer);
                imgPoint.setTag(0);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 0).apply();
            }
            imgPoint.requestLayout();
        }
        else if(compoundButton.getId() == R.id.switchBlueCamper) {
            ImageView imgPoint = mActivity.controlFragment.getImgPoint();
            if(b) {
                mSwitchPurple.setChecked(false);
                mSwitchElegant.setChecked(false);
                mSwitchPinkCamper.setChecked(false);
                mSwitchOrangeCamper.setChecked(false);
                imgPoint.setImageResource(R.drawable.control_pointer_camper_bl);
                imgPoint.setTag(4);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 4).apply();
            }
            else {
                imgPoint.setImageResource(R.drawable.ic_control_pointer);
                imgPoint.setTag(0);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 0).apply();
            }
            imgPoint.requestLayout();
        }
        else if(compoundButton.getId() == R.id.switchOrangeCamper) {
            ImageView imgPoint = mActivity.controlFragment.getImgPoint();
            if(b) {
                mSwitchPurple.setChecked(false);
                mSwitchElegant.setChecked(false);
                mSwitchPinkCamper.setChecked(false);
                mSwitchBlueCamper.setChecked(false);
                imgPoint.setImageResource(R.drawable.control_pointer_camper_or);
                imgPoint.setTag(5);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 5).apply();
            }
            else {
                imgPoint.setImageResource(R.drawable.ic_control_pointer);
                imgPoint.setTag(0);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 0).apply();
            }
            imgPoint.requestLayout();
        }
    }
}
