package com.edolfzoku.hayaemon2;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

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
        if(bPurplePurchased)
        {
            Button btnPurplePurchase = mActivity.findViewById(R.id.btnPurplePurchase);
            btnPurplePurchase.setBackgroundResource(R.drawable.itempurchased);
            btnPurplePurchase.setTextColor(Color.argb(255, 148, 148, 148));
            btnPurplePurchase.setText(R.string.purchased);
            btnPurplePurchase.setShadowLayer(0, 0, 0, Color.argb(0,0, 0, 0));
            Button btnPurpleSet = mActivity.findViewById(R.id.btnPurpleSet);
            btnPurpleSet.setVisibility(View.VISIBLE);
        }

        boolean bElegantPurchased = preferences.getBoolean("unipointer_e", false);
        if(bElegantPurchased)
        {
            Button btnElegantPurchase = mActivity.findViewById(R.id.btnElegantPurchase);
            btnElegantPurchase.setBackgroundResource(R.drawable.itempurchased);
            btnElegantPurchase.setTextColor(Color.argb(255, 148, 148, 148));
            btnElegantPurchase.setText(R.string.purchased);
            btnElegantPurchase.setShadowLayer(0, 0, 0, Color.argb(0,0, 0, 0));
            Button btnElegantSet = mActivity.findViewById(R.id.btnElegantSet);
            btnElegantSet.setVisibility(View.VISIBLE);
        }

        boolean bPinkCamperPurchased = preferences.getBoolean("camperpointer_p", false);
        if(bPinkCamperPurchased)
        {
            Button btnPinkCamperPurchase = mActivity.findViewById(R.id.btnPinkCamperPurchase);
            btnPinkCamperPurchase.setBackgroundResource(R.drawable.itempurchased);
            btnPinkCamperPurchase.setTextColor(Color.argb(255, 148, 148, 148));
            btnPinkCamperPurchase.setText(R.string.purchased);
            btnPinkCamperPurchase.setShadowLayer(0, 0, 0, Color.argb(0,0, 0, 0));
            Button btnPinkCamperSet = mActivity.findViewById(R.id.btnPinkCamperSet);
            btnPinkCamperSet.setVisibility(View.VISIBLE);
        }

        boolean bBlueCamperPurchased = preferences.getBoolean("camperpointer_b", false);
        if(bBlueCamperPurchased)
        {
            Button btnBlueCamperPurchase = mActivity.findViewById(R.id.btnBlueCamperPurchase);
            btnBlueCamperPurchase.setBackgroundResource(R.drawable.itempurchased);
            btnBlueCamperPurchase.setTextColor(Color.argb(255, 148, 148, 148));
            btnBlueCamperPurchase.setText(R.string.purchased);
            btnBlueCamperPurchase.setShadowLayer(0, 0, 0, Color.argb(0,0, 0, 0));
            Button btnBlueCamperSet = mActivity.findViewById(R.id.btnBlueCamperSet);
            btnBlueCamperSet.setVisibility(View.VISIBLE);
        }

        boolean bOrangeCamperPurchased = preferences.getBoolean("camperpointer_o", false);
        if(bOrangeCamperPurchased)
        {
            Button btnOrangeCamperPurchase = mActivity.findViewById(R.id.btnOrangeCamperPurchase);
            btnOrangeCamperPurchase.setBackgroundResource(R.drawable.itempurchased);
            btnOrangeCamperPurchase.setTextColor(Color.argb(255, 148, 148, 148));
            btnOrangeCamperPurchase.setText(R.string.purchased);
            btnOrangeCamperPurchase.setShadowLayer(0, 0, 0, Color.argb(0,0, 0, 0));
            Button btnOrangeCamperSet = mActivity.findViewById(R.id.btnOrangeCamperSet);
            btnOrangeCamperSet.setVisibility(View.VISIBLE);
        }

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
    }

    private void close()
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
        {
            try {
                Bundle buyIntentBundle = mActivity.getService().getBuyIntent(3, mActivity.getPackageName(), "unipointer_p", "inapp", "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkVvqgLyPSTyJKuyNw3Z0luaxCnOtbFwj65HGYmDS4KiyGaJNgFsLOc9wpmIQaQI+zrntxbufWXsT0gIh1/MRRmX2FgA0G6WDS0+w39ZsbgJRbXsxOzOOZaHbSo2NLOA29GXPo9FraFtNrOL9v4vLu7hxDPdfqoFNR80BUWwQqMBsiMNFqJ12sq1HzxHd2MIk/QooBZIB3EeM0QX5EYIsWcaKIAyzetuKjRGvO9Oi2a86dOBUfOFnHMMCvQ5+dldx5UkzmnhlbTm/KBWQCO3AqNy82NKxN9ND6GWVrlHuQGYX1FRiApMeXCmEvmwEyU2ArztpV8CfHyK2d0mM4bp0bwIDAQAB");
                int response = buyIntentBundle.getInt("RESPONSE_CODE");
                if(response == 0) {
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    if(pendingIntent != null)
                        mActivity.startIntentSenderForResult(pendingIntent.getIntentSender(), 1002, new Intent(), 0, 0, 0);
                }
                else if(response == 7) {
                    buyPurpleSeaUrchinPointer();
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        else if(view.getId() == R.id.btnPurpleSet) {
            close();
            mActivity.openSetting();
        }
        else if(view.getId() == R.id.btnElegantPurchase)
        {
            try {
                Bundle buyIntentBundle = mActivity.getService().getBuyIntent(3, mActivity.getPackageName(), "unipointer_e", "inapp", "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkVvqgLyPSTyJKuyNw3Z0luaxCnOtbFwj65HGYmDS4KiyGaJNgFsLOc9wpmIQaQI+zrntxbufWXsT0gIh1/MRRmX2FgA0G6WDS0+w39ZsbgJRbXsxOzOOZaHbSo2NLOA29GXPo9FraFtNrOL9v4vLu7hxDPdfqoFNR80BUWwQqMBsiMNFqJ12sq1HzxHd2MIk/QooBZIB3EeM0QX5EYIsWcaKIAyzetuKjRGvO9Oi2a86dOBUfOFnHMMCvQ5+dldx5UkzmnhlbTm/KBWQCO3AqNy82NKxN9ND6GWVrlHuQGYX1FRiApMeXCmEvmwEyU2ArztpV8CfHyK2d0mM4bp0bwIDAQAB");
                int response = buyIntentBundle.getInt("RESPONSE_CODE");
                if(response == 0) {
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    if(pendingIntent != null)
                            mActivity.startIntentSenderForResult(pendingIntent.getIntentSender(), 1003, new Intent(), 0, 0, 0);
                }
                else if(response == 7) {
                    buyElegantSeaUrchinPointer();
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        else if(view.getId() == R.id.btnElegantSet) {
            close();
            mActivity.openSetting();
        }
        else if(view.getId() == R.id.btnPinkCamperPurchase)
        {
            try {
                Bundle buyIntentBundle = mActivity.getService().getBuyIntent(3, mActivity.getPackageName(), "camperpointer_p", "inapp", "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkVvqgLyPSTyJKuyNw3Z0luaxCnOtbFwj65HGYmDS4KiyGaJNgFsLOc9wpmIQaQI+zrntxbufWXsT0gIh1/MRRmX2FgA0G6WDS0+w39ZsbgJRbXsxOzOOZaHbSo2NLOA29GXPo9FraFtNrOL9v4vLu7hxDPdfqoFNR80BUWwQqMBsiMNFqJ12sq1HzxHd2MIk/QooBZIB3EeM0QX5EYIsWcaKIAyzetuKjRGvO9Oi2a86dOBUfOFnHMMCvQ5+dldx5UkzmnhlbTm/KBWQCO3AqNy82NKxN9ND6GWVrlHuQGYX1FRiApMeXCmEvmwEyU2ArztpV8CfHyK2d0mM4bp0bwIDAQAB");
                int response = buyIntentBundle.getInt("RESPONSE_CODE");
                if(response == 0) {
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    if(pendingIntent != null)
                        mActivity.startIntentSenderForResult(pendingIntent.getIntentSender(), 1004, new Intent(), 0, 0, 0);
                }
                else if(response == 7) {
                    buyPinkCamperPointer();
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        else if(view.getId() == R.id.btnPinkCamperSet) {
            close();
            mActivity.openSetting();
        }
        else if(view.getId() == R.id.btnBlueCamperPurchase)
        {
            try {
                Bundle buyIntentBundle = mActivity.getService().getBuyIntent(3, mActivity.getPackageName(), "camperpointer_b", "inapp", "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkVvqgLyPSTyJKuyNw3Z0luaxCnOtbFwj65HGYmDS4KiyGaJNgFsLOc9wpmIQaQI+zrntxbufWXsT0gIh1/MRRmX2FgA0G6WDS0+w39ZsbgJRbXsxOzOOZaHbSo2NLOA29GXPo9FraFtNrOL9v4vLu7hxDPdfqoFNR80BUWwQqMBsiMNFqJ12sq1HzxHd2MIk/QooBZIB3EeM0QX5EYIsWcaKIAyzetuKjRGvO9Oi2a86dOBUfOFnHMMCvQ5+dldx5UkzmnhlbTm/KBWQCO3AqNy82NKxN9ND6GWVrlHuQGYX1FRiApMeXCmEvmwEyU2ArztpV8CfHyK2d0mM4bp0bwIDAQAB");
                int response = buyIntentBundle.getInt("RESPONSE_CODE");
                if(response == 0) {
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    if(pendingIntent != null)
                        mActivity.startIntentSenderForResult(pendingIntent.getIntentSender(), 1005, new Intent(), 0, 0, 0);
                }
                else if(response == 7) {
                    buyBlueCamperPointer();
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        else if(view.getId() == R.id.btnBlueCamperSet) {
            close();
            mActivity.openSetting();
        }
        else if(view.getId() == R.id.btnOrangeCamperPurchase)
        {
            try {
                Bundle buyIntentBundle = mActivity.getService().getBuyIntent(3, mActivity.getPackageName(), "camperpointer_o", "inapp", "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkVvqgLyPSTyJKuyNw3Z0luaxCnOtbFwj65HGYmDS4KiyGaJNgFsLOc9wpmIQaQI+zrntxbufWXsT0gIh1/MRRmX2FgA0G6WDS0+w39ZsbgJRbXsxOzOOZaHbSo2NLOA29GXPo9FraFtNrOL9v4vLu7hxDPdfqoFNR80BUWwQqMBsiMNFqJ12sq1HzxHd2MIk/QooBZIB3EeM0QX5EYIsWcaKIAyzetuKjRGvO9Oi2a86dOBUfOFnHMMCvQ5+dldx5UkzmnhlbTm/KBWQCO3AqNy82NKxN9ND6GWVrlHuQGYX1FRiApMeXCmEvmwEyU2ArztpV8CfHyK2d0mM4bp0bwIDAQAB");
                int response = buyIntentBundle.getInt("RESPONSE_CODE");
                if(response == 0) {
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    if(pendingIntent != null)
                        mActivity.startIntentSenderForResult(pendingIntent.getIntentSender(), 1006, new Intent(), 0, 0, 0);
                }
                else if(response == 7) {
                    buyOrangeCamperPointer();
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        else if(view.getId() == R.id.btnOrangeCamperSet) {
            close();
            mActivity.openSetting();
        }
    }

    public void buyPurpleSeaUrchinPointer()
    {
        Button btnPurplePurchase = mActivity.findViewById(R.id.btnPurplePurchase);
        btnPurplePurchase.setBackgroundResource(R.drawable.itempurchased);
        btnPurplePurchase.setTextColor(Color.argb(255, 148, 148, 148));
        btnPurplePurchase.setText(R.string.purchased);
        btnPurplePurchase.setShadowLayer(0, 0, 0, Color.argb(0,0, 0, 0));
        btnPurplePurchase.setOnClickListener(null);
        Button btnPurpleSet = mActivity.findViewById(R.id.btnPurpleSet);
        btnPurpleSet.setVisibility(View.VISIBLE);
        SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("unipointer_p", true).apply();

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.purpleSeaUrchinPointer);
        builder.setMessage(R.string.askApply);
        builder.setPositiveButton(getString(R.string.Do), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                ImageView imgPoint = mActivity.findViewById(R.id.imgPoint);
                imgPoint.setImageResource(R.drawable.control_pointer_uni_murasaki);
                imgPoint.setTag(1);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 1).apply();
            }
        });
        builder.setNegativeButton(getString(R.string.NotYet), null);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface arg0)
            {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
            }
        });
        alertDialog.show();
    }

    public void buyElegantSeaUrchinPointer()
    {
        Button btnElegantPurchase = mActivity.findViewById(R.id.btnElegantPurchase);
        btnElegantPurchase.setBackgroundResource(R.drawable.itempurchased);
        btnElegantPurchase.setTextColor(Color.argb(255, 148, 148, 148));
        btnElegantPurchase.setText(R.string.purchased);
        btnElegantPurchase.setShadowLayer(0, 0, 0, Color.argb(0,0, 0, 0));
        btnElegantPurchase.setOnClickListener(null);
        Button btnElegantSet = mActivity.findViewById(R.id.btnElegantSet);
        btnElegantSet.setVisibility(View.VISIBLE);
        SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("unipointer_e", true).apply();

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.elegantSeaUrchinPointer);
        builder.setMessage(R.string.askApply);
        builder.setPositiveButton(getString(R.string.Do), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                ImageView imgPoint = mActivity.findViewById(R.id.imgPoint);
                imgPoint.setImageResource(R.drawable.control_pointer_uni_bafun);
                imgPoint.setTag(2);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 2).apply();
            }
        });
        builder.setNegativeButton(getString(R.string.NotYet), null);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface arg0)
            {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
            }
        });
        alertDialog.show();
    }

    public void buyPinkCamperPointer()
    {
        Button btnPinkCamperPurchase = mActivity.findViewById(R.id.btnPinkCamperPurchase);
        btnPinkCamperPurchase.setBackgroundResource(R.drawable.itempurchased);
        btnPinkCamperPurchase.setTextColor(Color.argb(255, 148, 148, 148));
        btnPinkCamperPurchase.setText(R.string.purchased);
        btnPinkCamperPurchase.setShadowLayer(0, 0, 0, Color.argb(0,0, 0, 0));
        btnPinkCamperPurchase.setOnClickListener(null);
        Button btnPinkCamperSet = mActivity.findViewById(R.id.btnPinkCamperSet);
        btnPinkCamperSet.setVisibility(View.VISIBLE);
        SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("camperpointer_p", true).apply();

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.pinkCamperPointer);
        builder.setMessage(R.string.askApply);
        builder.setPositiveButton(getString(R.string.Do), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                ImageView imgPoint = mActivity.findViewById(R.id.imgPoint);
                imgPoint.setImageResource(R.drawable.control_pointer_camper_pk);
                imgPoint.setTag(3);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 3).apply();
            }
        });
        builder.setNegativeButton(getString(R.string.NotYet), null);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface arg0)
            {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
            }
        });
        alertDialog.show();
    }

    public void buyBlueCamperPointer()
    {
        Button btnBlueCamperPurchase = mActivity.findViewById(R.id.btnBlueCamperPurchase);
        btnBlueCamperPurchase.setBackgroundResource(R.drawable.itempurchased);
        btnBlueCamperPurchase.setTextColor(Color.argb(255, 148, 148, 148));
        btnBlueCamperPurchase.setText(R.string.purchased);
        btnBlueCamperPurchase.setShadowLayer(0, 0, 0, Color.argb(0,0, 0, 0));
        btnBlueCamperPurchase.setOnClickListener(null);
        Button btnBlueCamperSet = mActivity.findViewById(R.id.btnBlueCamperSet);
        btnBlueCamperSet.setVisibility(View.VISIBLE);
        SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("camperpointer_b", true).apply();

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.blueCamperPointer);
        builder.setMessage(R.string.askApply);
        builder.setPositiveButton(getString(R.string.Do), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                ImageView imgPoint = mActivity.findViewById(R.id.imgPoint);
                imgPoint.setImageResource(R.drawable.control_pointer_camper_bl);
                imgPoint.setTag(3);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 3).apply();
            }
        });
        builder.setNegativeButton(getString(R.string.NotYet), null);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface arg0)
            {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
            }
        });
        alertDialog.show();
    }

    public void buyOrangeCamperPointer()
    {
        Button btnOrangeCamperPurchase = mActivity.findViewById(R.id.btnOrangeCamperPurchase);
        btnOrangeCamperPurchase.setBackgroundResource(R.drawable.itempurchased);
        btnOrangeCamperPurchase.setTextColor(Color.argb(255, 148, 148, 148));
        btnOrangeCamperPurchase.setText(R.string.purchased);
        btnOrangeCamperPurchase.setShadowLayer(0, 0, 0, Color.argb(0,0, 0, 0));
        btnOrangeCamperPurchase.setOnClickListener(null);
        Button btnOrangeCamperSet = mActivity.findViewById(R.id.btnOrangeCamperSet);
        btnOrangeCamperSet.setVisibility(View.VISIBLE);
        SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("camperpointer_o", true).apply();

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.orangeCamperPointer);
        builder.setMessage(R.string.askApply);
        builder.setPositiveButton(getString(R.string.Do), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SharedPreferences preferences = mActivity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                ImageView imgPoint = mActivity.findViewById(R.id.imgPoint);
                imgPoint.setImageResource(R.drawable.control_pointer_camper_or);
                imgPoint.setTag(3);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 3).apply();
            }
        });
        builder.setNegativeButton(getString(R.string.NotYet), null);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface arg0)
            {
                if(alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
            }
        });
        alertDialog.show();
    }
}
