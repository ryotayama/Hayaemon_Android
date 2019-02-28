package com.edolfzoku.hayaemon2;

import android.app.Activity;
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
import android.widget.TableLayout;

import com.google.android.gms.ads.AdView;

public class SettingFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
{
    public SettingFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnCloseSetting = (Button)getActivity().findViewById(R.id.btnCloseSetting);
        btnCloseSetting.setOnClickListener(this);

        MainActivity activity = (MainActivity)getActivity();
        Switch switchRepeat = (Switch)getActivity().findViewById(R.id.switchRepeat);
        switchRepeat.setChecked(!activity.isPlayNextByBPos());
        switchRepeat.setOnCheckedChangeListener(this);

        SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Boolean bPurplePurchased = preferences.getBoolean("unipointer_p", false);
        Boolean bElegantPurchased = preferences.getBoolean("unipointer_e", false);
        if(bPurplePurchased || bElegantPurchased) {
            RelativeLayout relativePurchaseSetting = (RelativeLayout)activity.findViewById(R.id.relativePurchaseSetting);
            relativePurchaseSetting.setVisibility(View.VISIBLE);
            ImageView imgPoint = (ImageView)activity.findViewById(R.id.imgPoint);
            int nTag = 0;
            if(imgPoint.getTag() != null) nTag = (Integer)imgPoint.getTag();
            if(bPurplePurchased) {
                RelativeLayout relativePurple = (RelativeLayout) activity.findViewById(R.id.relativePurple);
                relativePurple.setVisibility(View.VISIBLE);
                Switch switchPurple = (Switch)getActivity().findViewById(R.id.switchPurple);
                switchPurple.setChecked(nTag == 1);
                switchPurple.setOnCheckedChangeListener(this);
            }
            if(bElegantPurchased) {
                RelativeLayout relativeElegant = (RelativeLayout) activity.findViewById(R.id.relativeElegant);
                relativeElegant.setVisibility(View.VISIBLE);
                Switch switchElegant = (Switch)getActivity().findViewById(R.id.switchElegant);
                switchElegant.setChecked(nTag == 2);
                switchElegant.setOnCheckedChangeListener(this);
            }
            if(!bPurplePurchased || !bElegantPurchased) {
                View viewDivider = (View)activity.findViewById(R.id.viewDivider);
                viewDivider.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View view)
    {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        transaction.remove(this);
        transaction.commit();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b)
    {
        MainActivity activity = (MainActivity) getActivity();
        SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        if(compoundButton.getId() == R.id.switchRepeat) {
            activity.setPlayNextByBPos(!b);
            preferences.edit().putBoolean("bPlayNextByBPos", activity.isPlayNextByBPos()).commit();
        }
        else if(compoundButton.getId() == R.id.switchPurple) {
            Switch switchElegant = (Switch)getActivity().findViewById(R.id.switchElegant);
            ImageView imgPoint = (ImageView)activity.findViewById(R.id.imgPoint);
            if(b) {
                switchElegant.setChecked(false);
                imgPoint.setImageResource(R.drawable.control_pointer_uni_murasaki);
                imgPoint.setTag(1);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 1).commit();
            }
            else {
                imgPoint.setImageResource(R.drawable.ic_point);
                imgPoint.setTag(0);
                imgPoint.getLayoutParams().width = (int) (20 * activity.getResources().getDisplayMetrics().density + 0.5);
                imgPoint.getLayoutParams().height = (int) (20 * activity.getResources().getDisplayMetrics().density + 0.5);
                preferences.edit().putInt("imgPointTag", 0).commit();
            }
            imgPoint.requestLayout();
        }
        else if(compoundButton.getId() == R.id.switchElegant) {
            Switch switchPurple = (Switch)getActivity().findViewById(R.id.switchPurple);
            ImageView imgPoint = (ImageView)activity.findViewById(R.id.imgPoint);
            if(b) {
                switchPurple.setChecked(false);
                imgPoint.setImageResource(R.drawable.control_pointer_uni_bafun);
                imgPoint.setTag(2);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 2).commit();
            }
            else {
                imgPoint.setImageResource(R.drawable.ic_point);
                imgPoint.setTag(0);
                imgPoint.getLayoutParams().width = (int) (20 * activity.getResources().getDisplayMetrics().density + 0.5);
                imgPoint.getLayoutParams().height = (int) (20 * activity.getResources().getDisplayMetrics().density + 0.5);
                preferences.edit().putInt("imgPointTag", 0).commit();
            }
            imgPoint.requestLayout();
        }
    }
}
