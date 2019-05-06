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
        Boolean bPurplePurchased = preferences.getBoolean("unipointer_p", false);
        Boolean bElegantPurchased = preferences.getBoolean("unipointer_e", false);
        Boolean bPinkCamperPurchased = preferences.getBoolean("camperpointer_p", false);
        if(bPurplePurchased || bElegantPurchased || bPinkCamperPurchased) {
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
            View viewDivider1 = mActivity.findViewById(R.id.viewDivider1);
            View viewDivider2 = mActivity.findViewById(R.id.viewDivider2);
            if(bPurplePurchased && bElegantPurchased && bPinkCamperPurchased) {
                viewDivider1.setVisibility(View.VISIBLE);
                viewDivider2.setVisibility(View.VISIBLE);
            }
            else if(bPurplePurchased && bElegantPurchased) {
                viewDivider1.setVisibility(View.VISIBLE);
                viewDivider2.setVisibility(View.GONE);
            }
            else if(bPurplePurchased && bPinkCamperPurchased) {
                viewDivider1.setVisibility(View.VISIBLE);
                viewDivider2.setVisibility(View.GONE);
            }
            else if(bElegantPurchased && bPinkCamperPurchased) {
                viewDivider1.setVisibility(View.GONE);
                viewDivider2.setVisibility(View.VISIBLE);
            }
            else {
                viewDivider1.setVisibility(View.GONE);
                viewDivider2.setVisibility(View.GONE);
            }
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
                imgPoint.setImageResource(R.drawable.control_pointer_uni_murasaki);
                imgPoint.setTag(1);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 1).apply();
            }
            else {
                imgPoint.setImageResource(R.drawable.ic_point);
                imgPoint.setTag(0);
                imgPoint.getLayoutParams().width = (int) (20 * mActivity.getDensity());
                imgPoint.getLayoutParams().height = (int) (20 * mActivity.getDensity());
                preferences.edit().putInt("imgPointTag", 0).apply();
            }
            imgPoint.requestLayout();
        }
        else if(compoundButton.getId() == R.id.switchElegant) {
            ImageView imgPoint = mActivity.controlFragment.getImgPoint();
            if(b) {
                mSwitchPurple.setChecked(false);
                mSwitchPinkCamper.setChecked(false);
                imgPoint.setImageResource(R.drawable.control_pointer_uni_bafun);
                imgPoint.setTag(2);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 2).apply();
            }
            else {
                imgPoint.setImageResource(R.drawable.ic_point);
                imgPoint.setTag(0);
                imgPoint.getLayoutParams().width = (int) (20 * mActivity.getDensity());
                imgPoint.getLayoutParams().height = (int) (20 * mActivity.getDensity());
                preferences.edit().putInt("imgPointTag", 0).apply();
            }
            imgPoint.requestLayout();
        }
        else if(compoundButton.getId() == R.id.switchPinkCamper) {
            ImageView imgPoint = mActivity.controlFragment.getImgPoint();
            if(b) {
                mSwitchPurple.setChecked(false);
                mSwitchElegant.setChecked(false);
                imgPoint.setImageResource(R.drawable.control_pointer_camper_pk);
                imgPoint.setTag(3);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 3).apply();
            }
            else {
                imgPoint.setImageResource(R.drawable.ic_point);
                imgPoint.setTag(0);
                imgPoint.getLayoutParams().width = (int) (20 * mActivity.getDensity());
                imgPoint.getLayoutParams().height = (int) (20 * mActivity.getDensity());
                preferences.edit().putInt("imgPointTag", 0).apply();
            }
            imgPoint.requestLayout();
        }
    }
}
