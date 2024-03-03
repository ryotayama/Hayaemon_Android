package com.edolfzoku.hayaemon2;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

        btnCloseItem.setOnClickListener(this);
        btnPurchaseOnce.setOnClickListener(this);
        btnRestoreOnce.setOnClickListener(this);

        btnCloseItem.setTextColor(getResources().getColor(R.color.darkModeBlue));

        // TODO: 購入済みかの判定処理を追加
        btnPurchaseOnce.setBackgroundResource(mActivity.isDarkMode() ? R.drawable.itempurchase_dark : R.drawable.itempurchase);
    }

    public void close() {
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        transaction.remove(this);
        transaction.commit();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnCloseHideAds)
            close();
        else if(view.getId() == R.id.btnPurchaseOnce)
            mActivity.startBillingHideAds();
        else if(view.getId() == R.id.btnRestoreOnce)
            mActivity.checkPurchased();
    }
}
