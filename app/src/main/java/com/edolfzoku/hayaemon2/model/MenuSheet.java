package com.edolfzoku.hayaemon2.model;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;
import android.widget.TextView;

import com.edolfzoku.hayaemon2.MainActivity;
import com.edolfzoku.hayaemon2.R;

/**
 * Created by yamauchiryouta on 2018/01/22.
 */

public class MenuSheet extends BottomSheetDialogFragment
{
    @Override
    public void setupDialog(Dialog dialog, int style)
    {
        super.setupDialog(dialog, style);
        View view = View.inflate(getContext(), R.layout.menu_sheet, null);
        dialog.setContentView(view);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;

                MainActivity activity = (MainActivity)getActivity();

                TextView menuOpen = (TextView)d.findViewById(R.id.menuOpen);
                menuOpen.setOnClickListener(activity);

                TextView menuTwitter = (TextView)d.findViewById(R.id.menuTwitter);
                menuTwitter.setOnClickListener(activity);

                TextView menuReview = (TextView)d.findViewById(R.id.menuReview);
                menuReview.setOnClickListener(activity);

                TextView menuHideAds = (TextView)d.findViewById(R.id.menuHideAds);
                menuHideAds.setOnClickListener(activity);
                if(!activity.isAdsVisible())
                    menuHideAds.setVisibility(View.GONE);

                TextView menuAbout = (TextView)d.findViewById(R.id.menuAbout);
                menuAbout.setOnClickListener(activity);

                TextView menuCancel = (TextView)d.findViewById(R.id.menuCancel);
                menuCancel.setOnClickListener(activity);
            }
        });

        return dialog;
    }
}
