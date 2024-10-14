package com.edolfzoku.hayaemon2;

import android.os.Build;
import android.view.ViewGroup;
import android.view.WindowMetrics;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public class ProgressDialogBuilder extends LightDarkDialogBuilder {
    public ProgressDialogBuilder(@NonNull MainActivity sActivity, @StringRes int titleId, ProgressBar progressBar) {
        super(sActivity, titleId);

        LinearLayout linearLayout = new LinearLayout(sActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        param.topMargin = (int)(24 *  sActivity.getDensity());
        param.leftMargin = param.rightMargin = (int)(16 *  sActivity.getDensity());
        int nWidth;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = sActivity.getWindowManager().getCurrentWindowMetrics();
            nWidth = windowMetrics.getBounds().width();
        } else {
            nWidth = sActivity.getResources().getDisplayMetrics().widthPixels;
        }
        param.width = (int)(nWidth * 0.8);
        linearLayout.addView(progressBar, param);
        setView(linearLayout);
    }
}
