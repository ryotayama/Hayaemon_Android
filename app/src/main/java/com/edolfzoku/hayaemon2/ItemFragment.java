package com.edolfzoku.hayaemon2;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ItemFragment extends Fragment implements View.OnClickListener
{
    private MainActivity mActivity = null;

    public ItemFragment()
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
        return inflater.inflate(R.layout.fragment_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        boolean bPurplePurchased = preferences.getBoolean("unipointer_p", false);
        Button btnPurplePurchase = mActivity.findViewById(R.id.btnPurplePurchase);
        Button btnPurpleSet = mActivity.findViewById(R.id.btnPurpleSet);
        if(bPurplePurchased)
        {
            btnPurplePurchase.setBackgroundResource(mActivity.isDarkMode() ? R.drawable.itempurchased_dark : R.drawable.itempurchased);
            btnPurplePurchase.setTextColor(mActivity.isDarkMode() ? getResources().getColor(R.color.darkModeTextDarkGray) : Color.argb(255, 148, 148, 148));
            btnPurplePurchase.setText(R.string.purchased);
            btnPurplePurchase.setShadowLayer(0, 0, 0, Color.argb(0,0, 0, 0));
            btnPurpleSet.setVisibility(View.VISIBLE);
        }
        else btnPurplePurchase.setBackgroundResource(mActivity.isDarkMode() ? R.drawable.itempurchase_dark : R.drawable.itempurchase);

        boolean bElegantPurchased = preferences.getBoolean("unipointer_e", false);
        Button btnElegantPurchase = mActivity.findViewById(R.id.btnElegantPurchase);
        Button btnElegantSet = mActivity.findViewById(R.id.btnElegantSet);
        if(bElegantPurchased)
        {
            btnElegantPurchase.setBackgroundResource(mActivity.isDarkMode() ? R.drawable.itempurchased_dark : R.drawable.itempurchased);
            btnElegantPurchase.setTextColor(mActivity.isDarkMode() ? getResources().getColor(R.color.darkModeTextDarkGray) : Color.argb(255, 148, 148, 148));
            btnElegantPurchase.setText(R.string.purchased);
            btnElegantPurchase.setShadowLayer(0, 0, 0, Color.argb(0,0, 0, 0));
            btnElegantSet.setVisibility(View.VISIBLE);
        }
        else btnElegantPurchase.setBackgroundResource(mActivity.isDarkMode() ? R.drawable.itempurchase_dark : R.drawable.itempurchase);

        boolean bPinkCamperPurchased = preferences.getBoolean("camperpointer_p", false);
        Button btnPinkCamperPurchase = mActivity.findViewById(R.id.btnPinkCamperPurchase);
        Button btnPinkCamperSet = mActivity.findViewById(R.id.btnPinkCamperSet);
        if(bPinkCamperPurchased)
        {
            btnPinkCamperPurchase.setBackgroundResource(mActivity.isDarkMode() ? R.drawable.itempurchased_dark : R.drawable.itempurchased);
            btnPinkCamperPurchase.setTextColor(mActivity.isDarkMode() ? getResources().getColor(R.color.darkModeTextDarkGray) : Color.argb(255, 148, 148, 148));
            btnPinkCamperPurchase.setText(R.string.purchased);
            btnPinkCamperPurchase.setShadowLayer(0, 0, 0, Color.argb(0,0, 0, 0));
            btnPinkCamperSet.setVisibility(View.VISIBLE);
        }
        else btnPinkCamperPurchase.setBackgroundResource(mActivity.isDarkMode() ? R.drawable.itempurchase_dark : R.drawable.itempurchase);

        boolean bBlueCamperPurchased = preferences.getBoolean("camperpointer_b", false);
        Button btnBlueCamperPurchase = mActivity.findViewById(R.id.btnBlueCamperPurchase);
        Button btnBlueCamperSet = mActivity.findViewById(R.id.btnBlueCamperSet);
        if(bBlueCamperPurchased)
        {
            btnBlueCamperPurchase.setBackgroundResource(mActivity.isDarkMode() ? R.drawable.itempurchased_dark : R.drawable.itempurchased);
            btnBlueCamperPurchase.setTextColor(mActivity.isDarkMode() ? getResources().getColor(R.color.darkModeTextDarkGray) : Color.argb(255, 148, 148, 148));
            btnBlueCamperPurchase.setText(R.string.purchased);
            btnBlueCamperPurchase.setShadowLayer(0, 0, 0, Color.argb(0,0, 0, 0));
            btnBlueCamperSet.setVisibility(View.VISIBLE);
        }
        else btnBlueCamperPurchase.setBackgroundResource(mActivity.isDarkMode() ? R.drawable.itempurchase_dark : R.drawable.itempurchase);

        boolean bOrangeCamperPurchased = preferences.getBoolean("camperpointer_o", false);
        Button btnOrangeCamperPurchase = mActivity.findViewById(R.id.btnOrangeCamperPurchase);
        Button btnOrangeCamperSet = mActivity.findViewById(R.id.btnOrangeCamperSet);
        if(bOrangeCamperPurchased)
        {
            btnOrangeCamperPurchase.setBackgroundResource(mActivity.isDarkMode() ? R.drawable.itempurchased_dark : R.drawable.itempurchased);
            btnOrangeCamperPurchase.setTextColor(mActivity.isDarkMode() ? getResources().getColor(R.color.darkModeTextDarkGray) : Color.argb(255, 148, 148, 148));
            btnOrangeCamperPurchase.setText(R.string.purchased);
            btnOrangeCamperPurchase.setShadowLayer(0, 0, 0, Color.argb(0,0, 0, 0));
            btnOrangeCamperSet.setVisibility(View.VISIBLE);
        }
        else btnOrangeCamperPurchase.setBackgroundResource(mActivity.isDarkMode() ? R.drawable.itempurchase_dark : R.drawable.itempurchase);

        mActivity.findViewById(R.id.btnCloseItem).setOnClickListener(this);
        if(!bPurplePurchased) mActivity.findViewById(R.id.btnPurplePurchase).setOnClickListener(this);
        if(!bElegantPurchased) mActivity.findViewById(R.id.btnElegantPurchase).setOnClickListener(this);
        if(!bPinkCamperPurchased) mActivity.findViewById(R.id.btnPinkCamperPurchase).setOnClickListener(this);
        if(!bBlueCamperPurchased) mActivity.findViewById(R.id.btnBlueCamperPurchase).setOnClickListener(this);
        if(!bOrangeCamperPurchased) mActivity.findViewById(R.id.btnOrangeCamperPurchase).setOnClickListener(this);
        mActivity.findViewById(R.id.btnPurpleSet).setOnClickListener(this);
        mActivity.findViewById(R.id.btnElegantSet).setOnClickListener(this);
        mActivity.findViewById(R.id.btnPinkCamperSet).setOnClickListener(this);
        mActivity.findViewById(R.id.btnBlueCamperSet).setOnClickListener(this);
        mActivity.findViewById(R.id.btnOrangeCamperSet).setOnClickListener(this);
        ImageView imgPinkCamper = mActivity.findViewById(R.id.imgPinkCamper);
        ImageView imgBlueCamper = mActivity.findViewById(R.id.imgBlueCamper);
        ImageView imgOrangeCamper = mActivity.findViewById(R.id.imgOrangeCamper);

        if(mActivity.isDarkMode()) {
            RelativeLayout relativeItemScreen = mActivity.findViewById(R.id.relativeItemScreen);
            RelativeLayout relativeItemTitle = mActivity.findViewById(R.id.relativeItemTitle);
            TextView textItemTitle = mActivity.findViewById(R.id.textItemTitle);
            TextView textItemHeader = mActivity.findViewById(R.id.textItemHeader);
            TextView textItem = mActivity.findViewById(R.id.textItem);
            TextView textCamper = mActivity.findViewById(R.id.textCamper);
            TextView textPurpleTitle = mActivity.findViewById(R.id.textPurpleTitle);
            TextView textElegantTitle = mActivity.findViewById(R.id.textElegantTitle);
            TextView textPinkCamperTitle = mActivity.findViewById(R.id.textPinkCamperTitle);
            TextView textBlueCamperTitle = mActivity.findViewById(R.id.textBlueCamperTitle);
            TextView textOrangeCamperTitle = mActivity.findViewById(R.id.textOrangeCamperTitle);
            TextView textPurpleDescription = mActivity.findViewById(R.id.textPurpleDescription);
            TextView textElegantDescription = mActivity.findViewById(R.id.textElegantDescription);
            TextView textPinkCamperDescription = mActivity.findViewById(R.id.textPinkCamperDescription);
            TextView textBlueCamperDescription = mActivity.findViewById(R.id.textBlueCamperDescription);
            TextView textOrangeCamperDescription = mActivity.findViewById(R.id.textOrangeCamperDescription);
            Button btnCloseItem = mActivity.findViewById(R.id.btnCloseItem);
            View viewSepItem = mActivity.findViewById(R.id.viewSepItem);
            View viewSepItemHeader = mActivity.findViewById(R.id.viewSepItemHeader);
            View viewSepItemBottom = mActivity.findViewById(R.id.viewSepItemBottom);
            View viewPurpleBorder = mActivity.findViewById(R.id.viewPurpleBorder);
            View viewElegantBorder = mActivity.findViewById(R.id.viewElegantBorder);
            View viewPinkCamperBorder = mActivity.findViewById(R.id.viewPinkCamperBorder);
            View viewBlueCamperBorder = mActivity.findViewById(R.id.viewBlueCamperBorder);
            View viewOrangeCamperBorder = mActivity.findViewById(R.id.viewOrangeCamperBorder);
            RelativeLayout relativeItems = mActivity.findViewById(R.id.relativeItems);

            relativeItemScreen.setBackgroundColor(getResources().getColor(R.color.darkModeLightBk));
            relativeItemTitle.setBackgroundColor(getResources().getColor(R.color.darkModeBk));
            textItemTitle.setTextColor(getResources().getColor(android.R.color.white));
            textItemHeader.setTextColor(getResources().getColor(R.color.darkModeGray));
            textItem.setTextColor(getResources().getColor(android.R.color.white));
            textCamper.setTextColor(getResources().getColor(android.R.color.white));
            textPurpleTitle.setTextColor(getResources().getColor(android.R.color.white));
            textPurpleTitle.setShadowLayer(0, 0, 0, 0);
            textPurpleDescription.setTextColor(getResources().getColor(android.R.color.white));
            textPurpleDescription.setShadowLayer(0, 0, 0, 0);
            textElegantTitle.setTextColor(getResources().getColor(android.R.color.white));
            textElegantTitle.setShadowLayer(0, 0, 0, 0);
            textElegantDescription.setTextColor(getResources().getColor(android.R.color.white));
            textElegantDescription.setShadowLayer(0, 0, 0, 0);
            textPinkCamperTitle.setTextColor(getResources().getColor(android.R.color.white));
            textPinkCamperTitle.setShadowLayer(0, 0, 0, 0);
            textPinkCamperDescription.setTextColor(getResources().getColor(android.R.color.white));
            textPinkCamperDescription.setShadowLayer(0, 0, 0, 0);
            textBlueCamperTitle.setTextColor(getResources().getColor(android.R.color.white));
            textBlueCamperTitle.setShadowLayer(0, 0, 0, 0);
            textBlueCamperDescription.setTextColor(getResources().getColor(android.R.color.white));
            textBlueCamperDescription.setShadowLayer(0, 0, 0, 0);
            textOrangeCamperTitle.setTextColor(getResources().getColor(android.R.color.white));
            textOrangeCamperTitle.setShadowLayer(0, 0, 0, 0);
            textOrangeCamperDescription.setTextColor(getResources().getColor(android.R.color.white));
            textOrangeCamperDescription.setShadowLayer(0, 0, 0, 0);
            btnCloseItem.setTextColor(getResources().getColor(R.color.darkModeBlue));
            btnPurpleSet.setTextColor(getResources().getColor(R.color.darkModeBlue));
            btnElegantSet.setTextColor(getResources().getColor(R.color.darkModeBlue));
            btnPinkCamperSet.setTextColor(getResources().getColor(R.color.darkModeBlue));
            btnBlueCamperSet.setTextColor(getResources().getColor(R.color.darkModeBlue));
            btnOrangeCamperSet.setTextColor(getResources().getColor(R.color.darkModeBlue));
            viewSepItem.setBackgroundColor(getResources().getColor(R.color.darkModeSep));
            viewSepItemHeader.setBackgroundColor(getResources().getColor(R.color.darkModeSep));
            viewSepItemBottom.setBackgroundColor(getResources().getColor(R.color.darkModeSep));
            viewPurpleBorder.setBackgroundResource(R.drawable.seaurchinborder_dark);
            viewElegantBorder.setBackgroundResource(R.drawable.seaurchinborder_dark);
            viewPinkCamperBorder.setBackgroundResource(R.drawable.camperborder_dark);
            viewBlueCamperBorder.setBackgroundResource(R.drawable.camperborder_dark);
            viewOrangeCamperBorder.setBackgroundResource(R.drawable.camperborder_dark);
            relativeItems.setBackgroundColor(getResources().getColor(R.color.darkModeBk));
            imgPinkCamper.setBackgroundResource(R.drawable.item_control_pointer_camper_pk_dark);
            imgBlueCamper.setBackgroundResource(R.drawable.item_control_pointer_camper_bl_dark);
            imgOrangeCamper.setBackgroundResource(R.drawable.item_control_pointer_camper_or_dark);
        }

        AnimationDrawable anime = (AnimationDrawable)imgPinkCamper.getBackground();
        anime.start();
        AnimationDrawable anime2 = (AnimationDrawable)imgBlueCamper.getBackground();
        anime2.start();
        AnimationDrawable anime3 = (AnimationDrawable)imgOrangeCamper.getBackground();
        anime3.start();
    }

    public void close()
    {
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        transaction.remove(this);
        transaction.commit();
    }

    @Override
    public void onClick(View view)
    {
        if(view.getId() == R.id.btnCloseItem)
            close();
        else if(view.getId() == R.id.btnPurplePurchase)
            mActivity.startBillingPurpleSeaUrchinPointer();
        else if(view.getId() == R.id.btnPurpleSet) {
            close();
            mActivity.openSetting();
        }
        else if(view.getId() == R.id.btnElegantPurchase)
            mActivity.startBillingElegantSeaUrchinPointer();
        else if(view.getId() == R.id.btnElegantSet) {
            close();
            mActivity.openSetting();
        }
        else if(view.getId() == R.id.btnPinkCamperPurchase)
            mActivity.startBillingPinkCamperPointer();
        else if(view.getId() == R.id.btnPinkCamperSet) {
            close();
            mActivity.openSetting();
        }
        else if(view.getId() == R.id.btnBlueCamperPurchase)
            mActivity.startBillingBlueCamperPointer();
        else if(view.getId() == R.id.btnBlueCamperSet) {
            close();
            mActivity.openSetting();
        }
        else if(view.getId() == R.id.btnOrangeCamperPurchase)
            mActivity.startBillingOrangeCamperPointer();
        else if(view.getId() == R.id.btnOrangeCamperSet) {
            close();
            mActivity.openSetting();
        }
    }
}
