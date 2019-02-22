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
import android.widget.Switch;

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

        Switch switchRepeat = (Switch)getActivity().findViewById(R.id.switchRepeat);
        MainActivity activity = (MainActivity)getActivity();
        switchRepeat.setChecked(!activity.isPlayNextByBPos());
        switchRepeat.setOnCheckedChangeListener(this);
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
        MainActivity activity = (MainActivity)getActivity();
        activity.setPlayNextByBPos(!b);
        SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("bPlayNextByBPos", activity.isPlayNextByBPos()).commit();
    }
}
