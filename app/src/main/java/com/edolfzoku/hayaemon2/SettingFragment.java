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

public class SettingFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
{
    private MainActivity activity = null;

    public SettingFragment()
    {
    }

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnCloseSetting = activity.findViewById(R.id.btnCloseSetting);
        btnCloseSetting.setOnClickListener(this);

        Switch switchRepeat = activity.findViewById(R.id.switchRepeat);
        switchRepeat.setChecked(!activity.isPlayNextByBPos());
        switchRepeat.setOnCheckedChangeListener(this);

        Switch switchSnap = activity.findViewById(R.id.switchSnap);
        ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(2);
        switchSnap.setChecked(controlFragment.isSnap());
        switchSnap.setOnCheckedChangeListener(this);

        SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Boolean bPurplePurchased = preferences.getBoolean("unipointer_p", false);
        Boolean bElegantPurchased = preferences.getBoolean("unipointer_e", false);
        if(bPurplePurchased || bElegantPurchased) {
            RelativeLayout relativePurchaseSetting = activity.findViewById(R.id.relativePurchaseSetting);
            relativePurchaseSetting.setVisibility(View.VISIBLE);
            ImageView imgPoint = activity.findViewById(R.id.imgPoint);
            int nTag = 0;
            if(imgPoint.getTag() != null) nTag = (Integer)imgPoint.getTag();
            if(bPurplePurchased) {
                RelativeLayout relativePurple = activity.findViewById(R.id.relativePurple);
                relativePurple.setVisibility(View.VISIBLE);
                Switch switchPurple = activity.findViewById(R.id.switchPurple);
                switchPurple.setChecked(nTag == 1);
                switchPurple.setOnCheckedChangeListener(this);
            }
            if(bElegantPurchased) {
                RelativeLayout relativeElegant = activity.findViewById(R.id.relativeElegant);
                relativeElegant.setVisibility(View.VISIBLE);
                Switch switchElegant = activity.findViewById(R.id.switchElegant);
                switchElegant.setChecked(nTag == 2);
                switchElegant.setOnCheckedChangeListener(this);
            }
            if(!bPurplePurchased || !bElegantPurchased) {
                View viewDivider = activity.findViewById(R.id.viewDivider);
                viewDivider.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View view)
    {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        transaction.remove(this);
        transaction.commit();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b)
    {
        SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        if(compoundButton.getId() == R.id.switchRepeat) {
            activity.setPlayNextByBPos(!b);
            preferences.edit().putBoolean("bPlayNextByBPos", activity.isPlayNextByBPos()).apply();
        }
        else if(compoundButton.getId() == R.id.switchSnap) {
            ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(2);
            controlFragment.setSnap(b);
            preferences.edit().putBoolean("bSnap", b).apply();
        }
        else if(compoundButton.getId() == R.id.switchPurple) {
            Switch switchElegant = activity.findViewById(R.id.switchElegant);
            ImageView imgPoint = activity.findViewById(R.id.imgPoint);
            if(b) {
                switchElegant.setChecked(false);
                imgPoint.setImageResource(R.drawable.control_pointer_uni_murasaki);
                imgPoint.setTag(1);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 1).apply();
            }
            else {
                imgPoint.setImageResource(R.drawable.ic_point);
                imgPoint.setTag(0);
                imgPoint.getLayoutParams().width = (int) (20 * activity.getResources().getDisplayMetrics().density + 0.5);
                imgPoint.getLayoutParams().height = (int) (20 * activity.getResources().getDisplayMetrics().density + 0.5);
                preferences.edit().putInt("imgPointTag", 0).apply();
            }
            imgPoint.requestLayout();
        }
        else if(compoundButton.getId() == R.id.switchElegant) {
            Switch switchPurple = activity.findViewById(R.id.switchPurple);
            ImageView imgPoint = activity.findViewById(R.id.imgPoint);
            if(b) {
                switchPurple.setChecked(false);
                imgPoint.setImageResource(R.drawable.control_pointer_uni_bafun);
                imgPoint.setTag(2);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 2).apply();
            }
            else {
                imgPoint.setImageResource(R.drawable.ic_point);
                imgPoint.setTag(0);
                imgPoint.getLayoutParams().width = (int) (20 * activity.getResources().getDisplayMetrics().density + 0.5);
                imgPoint.getLayoutParams().height = (int) (20 * activity.getResources().getDisplayMetrics().density + 0.5);
                preferences.edit().putInt("imgPointTag", 0).apply();
            }
            imgPoint.requestLayout();
        }
    }
}
