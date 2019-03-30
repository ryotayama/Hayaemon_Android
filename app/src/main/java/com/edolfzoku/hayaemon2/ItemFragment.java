package com.edolfzoku.hayaemon2;

import android.app.Activity;
import android.app.PendingIntent;
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
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.ads.AdView;

public class ItemFragment extends Fragment implements View.OnClickListener
{
    public ItemFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_item, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity)getActivity();
        SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Boolean bPurplePurchased = preferences.getBoolean("unipointer_p", false);
        if(bPurplePurchased)
        {
            Button btnPurplePurchase = activity.findViewById(R.id.btnPurplePurchase);
            btnPurplePurchase.setBackgroundResource(R.drawable.itempurchased);
            btnPurplePurchase.setTextColor(Color.argb(255, 148, 148, 148));
            btnPurplePurchase.setText("購入済み");
            Button btnPurpleSet = activity.findViewById(R.id.btnPurpleSet);
            btnPurpleSet.setVisibility(View.VISIBLE);
        }

        Boolean bElegantPurchased = preferences.getBoolean("unipointer_e", false);
        if(bElegantPurchased)
        {
            Button btnElegantPurchase = activity.findViewById(R.id.btnElegantPurchase);
            btnElegantPurchase.setBackgroundResource(R.drawable.itempurchased);
            btnElegantPurchase.setTextColor(Color.argb(255, 148, 148, 148));
            btnElegantPurchase.setText("購入済み");
            Button btnElegantSet = activity.findViewById(R.id.btnElegantSet);
            btnElegantSet.setVisibility(View.VISIBLE);
        }

        getActivity().findViewById(R.id.btnCloseItem).setOnClickListener(this);
        if(!bPurplePurchased) getActivity().findViewById(R.id.btnPurplePurchase).setOnClickListener(this);
        if(!bElegantPurchased) getActivity().findViewById(R.id.btnElegantPurchase).setOnClickListener(this);
        getActivity().findViewById(R.id.btnPurpleSet).setOnClickListener(this);
        getActivity().findViewById(R.id.btnElegantSet).setOnClickListener(this);
    }

    public void close()
    {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
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
            MainActivity activity = (MainActivity)getActivity();
            try {
                Bundle buyIntentBundle = activity.getService().getBuyIntent(3, activity.getPackageName(), "unipointer_p", "inapp", "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkVvqgLyPSTyJKuyNw3Z0luaxCnOtbFwj65HGYmDS4KiyGaJNgFsLOc9wpmIQaQI+zrntxbufWXsT0gIh1/MRRmX2FgA0G6WDS0+w39ZsbgJRbXsxOzOOZaHbSo2NLOA29GXPo9FraFtNrOL9v4vLu7hxDPdfqoFNR80BUWwQqMBsiMNFqJ12sq1HzxHd2MIk/QooBZIB3EeM0QX5EYIsWcaKIAyzetuKjRGvO9Oi2a86dOBUfOFnHMMCvQ5+dldx5UkzmnhlbTm/KBWQCO3AqNy82NKxN9ND6GWVrlHuQGYX1FRiApMeXCmEvmwEyU2ArztpV8CfHyK2d0mM4bp0bwIDAQAB");
                int response = buyIntentBundle.getInt("RESPONSE_CODE");
                if(response == 0) {
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    activity.startIntentSenderForResult(
                            pendingIntent.getIntentSender(),
                            1002,
                            new Intent(),
                            Integer.valueOf(0),
                            Integer.valueOf(0),
                            Integer.valueOf(0)
                    );
                }
                else if(response == 1) {
                    // 購入がキャンセルされた
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
            MainActivity activity = (MainActivity)getActivity();
            activity.openSetting();
        }
        else if(view.getId() == R.id.btnElegantPurchase)
        {
            MainActivity activity = (MainActivity)getActivity();
            try {
                Bundle buyIntentBundle = activity.getService().getBuyIntent(3, activity.getPackageName(), "unipointer_e", "inapp", "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkVvqgLyPSTyJKuyNw3Z0luaxCnOtbFwj65HGYmDS4KiyGaJNgFsLOc9wpmIQaQI+zrntxbufWXsT0gIh1/MRRmX2FgA0G6WDS0+w39ZsbgJRbXsxOzOOZaHbSo2NLOA29GXPo9FraFtNrOL9v4vLu7hxDPdfqoFNR80BUWwQqMBsiMNFqJ12sq1HzxHd2MIk/QooBZIB3EeM0QX5EYIsWcaKIAyzetuKjRGvO9Oi2a86dOBUfOFnHMMCvQ5+dldx5UkzmnhlbTm/KBWQCO3AqNy82NKxN9ND6GWVrlHuQGYX1FRiApMeXCmEvmwEyU2ArztpV8CfHyK2d0mM4bp0bwIDAQAB");
                int response = buyIntentBundle.getInt("RESPONSE_CODE");
                if(response == 0) {
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    activity.startIntentSenderForResult(
                            pendingIntent.getIntentSender(),
                            1003,
                            new Intent(),
                            Integer.valueOf(0),
                            Integer.valueOf(0),
                            Integer.valueOf(0)
                    );
                }
                else if(response == 1) {
                    // 購入がキャンセルされた
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
            MainActivity activity = (MainActivity)getActivity();
            activity.openSetting();
        }
    }

    public void buyPurpleSeaUrchinPointer()
    {
        final MainActivity activity = (MainActivity)getActivity();
        Button btnPurplePurchase = activity.findViewById(R.id.btnPurplePurchase);
        btnPurplePurchase.setBackgroundResource(R.drawable.itempurchased);
        btnPurplePurchase.setTextColor(Color.argb(255, 148, 148, 148));
        btnPurplePurchase.setText("購入済み");
        btnPurplePurchase.setOnClickListener(null);
        Button btnPurpleSet = activity.findViewById(R.id.btnPurpleSet);
        btnPurpleSet.setVisibility(View.VISIBLE);
        SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("unipointer_p", true).commit();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("【謹製】お布施ウニポインター【むらさき】");
        builder.setMessage("購入したアイテムをさっそく適用しますか？\n（あとからも メニュー→オプション設定 から変更できます）");
        builder.setPositiveButton("する！", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                ImageView imgPoint = activity.findViewById(R.id.imgPoint);
                imgPoint.setImageResource(R.drawable.control_pointer_uni_murasaki);
                imgPoint.setTag(1);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 1).commit();
            }
        });
        builder.setNegativeButton("今はしない", null);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void buyElegantSeaUrchinPointer()
    {
        final MainActivity activity = (MainActivity)getActivity();
        Button btnElegantPurchase = activity.findViewById(R.id.btnElegantPurchase);
        btnElegantPurchase.setBackgroundResource(R.drawable.itempurchased);
        btnElegantPurchase.setTextColor(Color.argb(255, 148, 148, 148));
        btnElegantPurchase.setText("購入済み");
        btnElegantPurchase.setOnClickListener(null);
        Button btnElegantSet = activity.findViewById(R.id.btnElegantSet);
        btnElegantSet.setVisibility(View.VISIBLE);
        SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("unipointer_e", true).commit();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("【謹製】お布施ウニポインター【ばふん】");
        builder.setMessage("購入したアイテムをさっそく適用しますか？\n（あとからも メニュー→オプション設定 から変更できます）");
        builder.setPositiveButton("する！", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                ImageView imgPoint = activity.findViewById(R.id.imgPoint);
                imgPoint.setImageResource(R.drawable.control_pointer_uni_bafun);
                imgPoint.setTag(2);
                imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                preferences.edit().putInt("imgPointTag", 2).commit();
            }
        });
        builder.setNegativeButton("今はしない", null);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
