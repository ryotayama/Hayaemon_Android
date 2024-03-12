package com.edolfzoku.hayaemon2;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class HideAdsFragment extends Fragment implements View.OnClickListener {
    private MainActivity mActivity = null;

    public HideAdsFragment() {
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hide_ads, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnCloseItem = mActivity.findViewById(R.id.btnCloseHideAds);
        Button btnPurchaseOnce = mActivity.findViewById(R.id.btnPurchaseOnce);
        Button btnRestoreOnce = mActivity.findViewById(R.id.btnRestoreOnce);
        Button btnPurchaseContinuous = mActivity.findViewById(R.id.btnPurchaseContinuous);
        Button btnRestoreContinuous = mActivity.findViewById(R.id.btnRestoreContinuous);
        TextView textPurchaseContinuousPlanDescription = mActivity.findViewById(R.id.textPurchaseContinuousPlanDescription);

        btnCloseItem.setOnClickListener(this);
        btnPurchaseOnce.setOnClickListener(this);
        btnRestoreOnce.setOnClickListener(this);
        btnPurchaseContinuous.setOnClickListener(this);
        btnRestoreContinuous.setOnClickListener(this);

        String stringHtml = getString(R.string.purchaseContinuousPlanDescription);
        if (mActivity.isDarkMode()) {
            stringHtml = stringHtml.replaceAll("<a ", "<font color='#66A4FF'><a ");
            stringHtml = stringHtml.replaceAll("</a>", "</font></a>");
        }
        textPurchaseContinuousPlanDescription.setText(HtmlCompat.fromHtml(stringHtml, HtmlCompat.FROM_HTML_MODE_LEGACY));
        textPurchaseContinuousPlanDescription.setMovementMethod(LinkMovementMethod.getInstance());

        if(mActivity.isDarkMode()) {
            final int nDarkModeLightBk = getResources().getColor(R.color.darkModeLightBk);
            if (Build.VERSION.SDK_INT >= 23) {
                mActivity.getWindow().setStatusBarColor(nDarkModeLightBk);
            }
            RelativeLayout relativeHideAdsScreen = mActivity.findViewById(R.id.relativeHideAdsScreen);
            RelativeLayout relativeHideAdsTitle = mActivity.findViewById(R.id.relativeHideAdsTitle);
            RelativeLayout relativePurchaseOnce = mActivity.findViewById(R.id.relativePurchaseOnce);
            RelativeLayout relativePurchaseContinuous = mActivity.findViewById(R.id.relativePurchaseContinuous);
            TextView textHideAdsTitle = mActivity.findViewById(R.id.textHideAdsTitle);
            TextView textHideAdsHeader = mActivity.findViewById(R.id.textHideAdsHeader);
            TextView textPurchaseOncePlan = mActivity.findViewById(R.id.textPurchaseOncePlan);
            TextView textPurchaseOncePlanDescription = mActivity.findViewById(R.id.textPurchaseOncePlanDescription);
            TextView textPurchaseContinuousPlan = mActivity.findViewById(R.id.textPurchaseContinuousPlan);
            View viewSepHideAds = mActivity.findViewById(R.id.viewSepHideAds);
            View viewSepHideAdsHeader = mActivity.findViewById(R.id.viewSepHideAdsHeader);
            View viewSepHideAdsOnceBottom = mActivity.findViewById(R.id.viewSepHideAdsOnceBottom);

            btnCloseItem.setTextColor(getResources().getColor(R.color.darkModeBlue));
            btnRestoreOnce.setTextColor(getResources().getColor(R.color.darkModeBlue));
            btnRestoreContinuous.setTextColor(getResources().getColor(R.color.darkModeBlue));
            relativeHideAdsScreen.setBackgroundColor(getResources().getColor(R.color.darkModeLightBk));
            relativeHideAdsTitle.setBackgroundColor(getResources().getColor(R.color.darkModeLightBk));
            relativePurchaseOnce.setBackgroundColor(getResources().getColor(R.color.darkModeLightBk));
            relativePurchaseContinuous.setBackgroundColor(getResources().getColor(R.color.darkModeLightBk));
            textHideAdsTitle.setTextColor(getResources().getColor(android.R.color.white));
            textHideAdsHeader.setTextColor(getResources().getColor(R.color.darkModeGray));
            textPurchaseOncePlan.setTextColor(getResources().getColor(android.R.color.white));
            textPurchaseOncePlanDescription.setTextColor(getResources().getColor(R.color.darkModeGray));
            textPurchaseContinuousPlan.setTextColor(getResources().getColor(android.R.color.white));
            textPurchaseContinuousPlanDescription.setTextColor(getResources().getColor(R.color.darkModeGray));
            viewSepHideAds.setBackgroundColor(getResources().getColor(R.color.darkModeSep));
            viewSepHideAdsHeader.setBackgroundColor(getResources().getColor(R.color.darkModeSep));
            viewSepHideAdsOnceBottom.setBackgroundColor(getResources().getColor(R.color.darkModeSep));

            btnPurchaseOnce.setBackgroundResource(R.drawable.itempurchase_dark);
            btnPurchaseContinuous.setBackgroundResource(R.drawable.itempurchase_dark);
        } else {
            final int nTitleBk = Color.parseColor("#F9F9F9");
            if (Build.VERSION.SDK_INT >= 23) {
                mActivity.getWindow().setStatusBarColor(nTitleBk);
            }
            btnPurchaseOnce.setBackgroundResource(R.drawable.itempurchase);
            btnPurchaseContinuous.setBackgroundResource(R.drawable.itempurchase);
        }
    }

    public void close() {
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        transaction.remove(this);
        transaction.commit();

        final int nColorBk = getResources().getColor(mActivity.isDarkMode() ? R.color.darkModeBk : R.color.lightModeBk);
        if (Build.VERSION.SDK_INT >= 23) {
            mActivity.getWindow().setStatusBarColor(nColorBk);
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnCloseHideAds)
            close();
        else if(view.getId() == R.id.btnPurchaseOnce)
            mActivity.startBillingHideAds();
        else if(view.getId() == R.id.btnRestoreOnce)
            mActivity.checkInAppPurchased();
        else if(view.getId() == R.id.btnPurchaseContinuous)
            mActivity.startBillingHideAdsMonthly();
        else if(view.getId() == R.id.btnRestoreContinuous)
            mActivity.checkSubsPurchased();
    }
}
