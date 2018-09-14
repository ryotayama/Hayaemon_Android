/*
 * MainActivity
 *
 * Copyright (c) 2018 Ryota Yamauchi. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.edolfzoku.hayaemon2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;
import com.edolfzoku.hayaemon2.model.MenuSheet;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS_FX;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.un4seen.bass.BASS_AAC.BASS_CONFIG_AAC_MP4;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        View.OnLongClickListener, View.OnTouchListener
{
    static int hFxVol;
    static int hStream;
    SectionsPagerAdapter mSectionsPagerAdapter;
    boolean bLoopA, bLoopB;
    double dLoopA, dLoopB;
    private HoldableViewPager mViewPager;
    private int hSync;
    private MenuSheet menuSheet;
    private IInAppBillingService mService;
    private ServiceConnection mServiceConn;
    private boolean bShowUpdateLog;
    private BroadcastReceiver myPromoReceiver;
    private AdView mAdView;
    private boolean mBound = false;

    public MainActivity() {
        bLoopA = false;
        bLoopB = false;
        dLoopA = 0.0;
        dLoopB = 0.0;
        bShowUpdateLog = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initialize();
        loadData();

        Intent intent = getIntent();
        if(intent != null && intent.getType() != null) {
            if(intent.getType().indexOf("audio/") != -1) {
                PlaylistFragment playlistFragment = (PlaylistFragment)mSectionsPagerAdapter.getItem(0);
                if(Build.VERSION.SDK_INT < 16)
                {
                    Uri uri = copyFile(intent.getData());
                    playlistFragment.addSong(this, uri);
                }
                else
                {
                    if(intent.getClipData() == null)
                    {
                        Uri uri = copyFile(intent.getData());
                        playlistFragment.addSong(this, uri);
                    }
                    else
                    {
                        for(int i = 0; i < intent.getClipData().getItemCount(); i++)
                        {
                            Uri uri = copyFile(intent.getClipData().getItemAt(i).getUri());
                            playlistFragment.addSong(this, uri);
                        }
                    }
                }
                playlistFragment.updateSongs();
                SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                Gson gson = new Gson();
                preferences.edit().putString("arPlaylists", gson.toJson(playlistFragment.getArPlaylists())).commit();
                preferences.edit().putString("arPlaylistNames", gson.toJson(playlistFragment.getArPlaylistNames())).commit();
            }
        }

        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
                mBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
                mBound = false;
            }
        };
        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        if (!mBound) {
            bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
            mBound = true;
        }
    }

    public Uri copyFile(Uri uri)
    {
        int i = 0;
        String strPath;
        File file;
        while(true) {
            strPath = getFilesDir() + "/copied" + String.format("%d", i);
            file = new File(strPath);
            if(!file.exists()) break;
            i++;
        }
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            FileOutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            in.close();
            out.close();
        } catch (IOException e) {
            return null;
        }
        return Uri.parse(strPath);
    }

    @Override
    protected void onResume() {
        super.onResume();

        myPromoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
                    if(ownedItems != null && ownedItems.getInt("RESPONSE_CODE") == 0) {
                        ArrayList<String> ownedSkus =
                                ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                        ArrayList<String> purchaseDataList =
                                ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                        for(int i = 0; i < purchaseDataList.size(); i++) {
                            String purchaseData = purchaseDataList.get(i);
                            String sku = ownedSkus.get(i);

                            if(sku.equals("hideads")) {
                                hideAds();
                            }
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                    finish();
                }
            }
        };
        IntentFilter promoFilter =
                new IntentFilter("com.android.vending.billing.PURCHASES_UPDATED");
        registerReceiver(myPromoReceiver, promoFilter);

        try {
            Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
            if(ownedItems != null && ownedItems.getInt("RESPONSE_CODE") == 0) {
                ArrayList<String> ownedSkus =
                        ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                ArrayList<String> purchaseDataList =
                        ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                for(int i = 0; i < purchaseDataList.size(); i++) {
                    String purchaseData = purchaseDataList.get(i);
                    String sku = ownedSkus.get(i);

                    if(sku.equals("hideads"))
                        hideAds();
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(myPromoReceiver);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if(bShowUpdateLog) {
            bShowUpdateLog = false;

            final LayoutInflater inflater = (LayoutInflater)this.getSystemService(
                    LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.updatelogdialog,
                    (ViewGroup)findViewById(R.id.layout_root));
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            TextView title = new TextView(this);
            try {
                title.setText("ハヤえもんAndroid版ver." + getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName + "に\nアップデートされました！");
            }
            catch(PackageManager.NameNotFoundException e) {
                title.setText("ハヤえもんAndroid版が\nアップデートされました！");
            }
            title.setGravity(Gravity.CENTER);
            title.setTextSize(20);
            builder.setCustomTitle(title);
            title.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            builder.setView(layout);

            TextView textViewBlog = (TextView)layout.findViewById(R.id.textViewBlog);
            String strBlog = "この内容は<a href=\"http://hayaemon.jp/blog/\">開発者ブログ</a>から";
            CharSequence blogChar = Html.fromHtml(strBlog);
            textViewBlog.setText(blogChar);
            MovementMethod mMethod = LinkMovementMethod.getInstance();
            textViewBlog.setMovementMethod(mMethod);
            textViewBlog.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            TextView textViewArticle = (TextView)layout.findViewById(R.id.textViewArticle);
            String strArticle = "<a href=\"http://hayaemon.jp/blog/archives/5583\">→該当記事へ</a>";
            CharSequence blogChar2 = Html.fromHtml(strArticle);
            textViewArticle.setText(blogChar2);
            textViewArticle.setMovementMethod(mMethod);

            TextView textView = (TextView)layout.findViewById(R.id.textView);
            textView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            textView.setMovementMethod(ScrollingMovementMethod.getInstance());
            textView.setText(readChangeLog());

            final AlertDialog alertDialog = builder.create();
            alertDialog.show();
            Button buttonClose = (Button)layout.findViewById(R.id.buttonClose);
            buttonClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Switch switchNextHidden = (Switch)layout.findViewById(R.id.switchNextHidden);
                    boolean bChecked = switchNextHidden.isChecked();
                    SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                    preferences.edit().putBoolean("hideupdatelognext", bChecked).commit();
                    alertDialog.dismiss();
                }
            });
            Button buttonShare = (Button)layout.findViewById(R.id.buttonShare);
            buttonShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    try {
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "ハヤえもんAndroid版ver." + getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName + "にアップデートしました！ https://play.google.com/store/apps/details?id=com.edolfzoku.hayaemon2");
                    }
                    catch(PackageManager.NameNotFoundException e) {
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "ハヤえもんAndroid版をアップデートしました！ https://play.google.com/store/apps/details?id=com.edolfzoku.hayaemon2");
                    }
                    sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(getScreenshot(layout.getRootView())));
                    sendIntent.setType("*/*");
                    startActivity(sendIntent);
                }
            });
        }
    }

    public File getScreenshot(View view) {
        view.setDrawingCacheEnabled(true);

        // Viewのキャッシュを取得
        Bitmap cache = view.getDrawingCache();
        Bitmap screenShot = Bitmap.createBitmap(cache);
        view.setDrawingCacheEnabled(false);

        File file = new File(Environment.getExternalStorageDirectory() + "/capture.jpeg");
        file.getParentFile().mkdir();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, false);
            // 画像のフォーマットと画質と出力先を指定して保存
            screenShot.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ie) {
                    ie.printStackTrace();
                }
            }
        }
        return file;
    }

    private void loadData()
    {
        SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        PlaylistFragment playlistFragment = (PlaylistFragment)mSectionsPagerAdapter.getItem(0);
        ArrayList<ArrayList<SongItem>> arPlaylists = gson.fromJson(preferences.getString("arPlaylists",""), new TypeToken<ArrayList<ArrayList<SongItem>>>(){}.getType());
        ArrayList<String> arPlaylistNames = gson.fromJson(preferences.getString("arPlaylistNames",""), new TypeToken<ArrayList<String>>(){}.getType());
        List<String> arSongsPath = gson.fromJson(preferences.getString("arSongsPath",""), new TypeToken<List<String>>(){}.getType());
        if(arPlaylists != null && arPlaylistNames != null) {
            for(int i = 0; i < arPlaylists.size(); i++) {
                playlistFragment.setArPlaylists(arPlaylists);
                playlistFragment.setArPlaylistNames(arPlaylistNames);
            }
        }
        else if(arSongsPath != null) {
            playlistFragment.addPlaylist("再生リスト 1");
            playlistFragment.addPlaylist("再生リスト 2");
            playlistFragment.addPlaylist("再生リスト 3");
            playlistFragment.selectPlaylist(0);
            for(int i = 0; i < arSongsPath.size(); i++) {
                playlistFragment.addSong(this, Uri.parse(arSongsPath.get(i)));
            }
        }
        else {
            playlistFragment.addPlaylist("再生リスト 1");
            playlistFragment.addPlaylist("再生リスト 2");
            playlistFragment.addPlaylist("再生リスト 3");
            playlistFragment.selectPlaylist(0);
        }

        String strVersionName = preferences.getString("versionname", null);
        Boolean bHideUpdateLogNext = preferences.getBoolean("hideupdatelognext", false);
        String strCurrentVersionName;
        try {
            strCurrentVersionName = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
        }
        catch(PackageManager.NameNotFoundException e) {
            strCurrentVersionName = strVersionName;
        }
        if(!bHideUpdateLogNext)
        {
            if(strVersionName != null && !strCurrentVersionName.equals(strVersionName))
            {
                bShowUpdateLog = true;
            }
        }
        preferences.edit().putString("versionname", strCurrentVersionName).commit();

        boolean bHideAds = preferences.getBoolean("hideads", false);
        if(bHideAds) hideAds();
        else
        {
            try {
                Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
                if(ownedItems.getInt("RESPONSE_CODE") == 0)
                {
                    ArrayList<String> ownedSkus =
                            ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                    ArrayList<String> purchaseDataList =
                            ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                    for(int i = 0; i < purchaseDataList.size(); i++) {
                        String purchaseData = purchaseDataList.get(i);
                        String sku = ownedSkus.get(i);

                        if(sku.equals("hideads"))
                            hideAds();
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        int nPlayMode = preferences.getInt("playmode", 0);
        Button btnPlayMode = (Button)findViewById(R.id.btnPlayMode);
        if(nPlayMode == 0)
        {
            btnPlayMode.setText("連続再生");
            btnPlayMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_normal, 0, 0);
        }
        else if(nPlayMode == 1)
        {
            btnPlayMode.setText("１曲リピート");
            btnPlayMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_sloop, 0, 0);
        }
        else if(nPlayMode == 2)
        {
            btnPlayMode.setText("全曲リピート");
            btnPlayMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_aloop, 0, 0);
        }
        else if(nPlayMode == 3)
        {
            btnPlayMode.setText("シャッフル");
            btnPlayMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_random, 0, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == 1)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                PlaylistFragment playlistFragment = (PlaylistFragment)mSectionsPagerAdapter.getItem(0);
                playlistFragment.startRecord();
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("マイクへのアクセスが許可されていません");
                builder.setMessage("こんにちは♪\n\nハヤえもん開発者のりょーたです！\n\nマイクへのアクセスが許可されていません。\n\nお手数をおかけしますが、設定→アプリと通知→アプリの権限→マイク（環境によって、メニューの場所は異なります）などからハヤえもんのアクセスを許可してください。\n\nそれでは引き続き、Enjoy \"Your\" Music with Hayaemon!!");
                builder.setPositiveButton("OK", null);
                builder.show();
            }
        }
    }

    @Override
    public boolean onLongClick(View v)
    {
        if(v.getId() == R.id.btnRewind)
        {
            if(hStream == 0) return false;
            int chan = BASS_FX.BASS_FX_TempoGetSource(hStream);
            EffectFragment effectFragment = (EffectFragment)mSectionsPagerAdapter.getItem(4);
            if(effectFragment.isReverse())
                BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_FORWARD);
            else
                BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_REVERSE);
            ControlFragment controlFragment = (ControlFragment)mSectionsPagerAdapter.getItem(1);
            BASS.BASS_ChannelSetAttribute(hStream, BASS_FX.BASS_ATTRIB_TEMPO, controlFragment.fSpeed + 100);
            return true;
        }
        else if(v.getId() == R.id.btnForward)
        {
            if(hStream == 0) return false;
            ControlFragment controlFragment = (ControlFragment)mSectionsPagerAdapter.getItem(1);
            BASS.BASS_ChannelSetAttribute(hStream, BASS_FX.BASS_ATTRIB_TEMPO, controlFragment.fSpeed + 100);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if(event.getAction() == MotionEvent.ACTION_UP)
        {
            if(v.getId() == R.id.btnRewind)
            {
                if(hStream == 0) return false;
                int chan = BASS_FX.BASS_FX_TempoGetSource(hStream);
                EffectFragment effectFragment = (EffectFragment)mSectionsPagerAdapter.getItem(4);
                if(effectFragment.isReverse())
                    BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_REVERSE);
                else
                    BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_FORWARD);
                ControlFragment controlFragment = (ControlFragment)mSectionsPagerAdapter.getItem(1);
                BASS.BASS_ChannelSetAttribute(hStream, BASS_FX.BASS_ATTRIB_TEMPO, controlFragment.fSpeed);
            }
            else if(v.getId() == R.id.btnForward)
            {
                if(hStream == 0) return false;
                ControlFragment controlFragment = (ControlFragment)mSectionsPagerAdapter.getItem(1);
                BASS.BASS_ChannelSetAttribute(hStream, BASS_FX.BASS_ATTRIB_TEMPO, controlFragment.fSpeed);
            }
        }
        return false;
    }

    @Override
    public void onClick(View v)
    {
        PlaylistFragment playlistFragment = (PlaylistFragment)mSectionsPagerAdapter.getItem(0);
        if(v.getId() == R.id.btnMenu)
        {
            menuSheet.show(getSupportFragmentManager(), menuSheet.getTag());
        }
        else if(v.getId() == R.id.menuOpen)
        {
            open();
            menuSheet.dismiss();
        }
        else if(v.getId() == R.id.menuTwitter)
        {
            Uri uri = Uri.parse("https://twitter.com/ryota_yama");
            Intent i = new Intent(Intent.ACTION_VIEW,uri);
            startActivity(i);
            menuSheet.dismiss();
        }
        else if(v.getId() == R.id.menuReview)
        {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.edolfzoku.hayaemon2&hl=ja");
            Intent i = new Intent(Intent.ACTION_VIEW,uri);
            startActivity(i);
            menuSheet.dismiss();
        }
        else if(v.getId() == R.id.menuHideAds)
        {
            try {
                Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "hideads", "inapp", "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkVvqgLyPSTyJKuyNw3Z0luaxCnOtbFwj65HGYmDS4KiyGaJNgFsLOc9wpmIQaQI+zrntxbufWXsT0gIh1/MRRmX2FgA0G6WDS0+w39ZsbgJRbXsxOzOOZaHbSo2NLOA29GXPo9FraFtNrOL9v4vLu7hxDPdfqoFNR80BUWwQqMBsiMNFqJ12sq1HzxHd2MIk/QooBZIB3EeM0QX5EYIsWcaKIAyzetuKjRGvO9Oi2a86dOBUfOFnHMMCvQ5+dldx5UkzmnhlbTm/KBWQCO3AqNy82NKxN9ND6GWVrlHuQGYX1FRiApMeXCmEvmwEyU2ArztpV8CfHyK2d0mM4bp0bwIDAQAB");
                int response = buyIntentBundle.getInt("RESPONSE_CODE");
                if(response == 0) {
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    startIntentSenderForResult(
                            pendingIntent.getIntentSender(),
                            1001,
                            new Intent(),
                            Integer.valueOf(0),
                            Integer.valueOf(0),
                            Integer.valueOf(0)
                    );
                }
                else if(response == 1) {
                    // 購入がキャンセルされた
                }
                else if(response == 7){
                    hideAds();
                }
            }
            catch(Exception e) {
            }
            menuSheet.dismiss();
        }
        else if(v.getId() == R.id.menuAbout)
        {
            menuSheet.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            try {
                String strVersionName = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
                builder.setMessage("バージョン: " + strVersionName);
            }
            catch(PackageManager.NameNotFoundException e) {
                builder.setMessage("バージョン: 〈バージョン情報を取得できませんでした〉");
            }

            builder.setTitle("ハヤえもんについて");
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            builder.show();
        }
        else if(v.getId() == R.id.menuCancel)
        {
            menuSheet.dismiss();
        }
    }

    public void open()
    {
        PlaylistFragment playlistFragment = (PlaylistFragment)mSectionsPagerAdapter.getItem(0);
        if (Build.VERSION.SDK_INT < 19)
        {
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            playlistFragment.startActivityForResult(intent, 1);
        }
        else
        {
            final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            playlistFragment.startActivityForResult(intent, 1);
        }
    }

    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK) return;

        if(requestCode == 1001)
        {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

            if (resultCode == RESULT_OK)
            {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String productId = jo.getString("productId");

                    hideAds();
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void hideAds()
    {
        if(mAdView.getVisibility() != AdView.GONE) {
            mAdView.setVisibility(AdView.GONE);

            SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
            preferences.edit().putBoolean("hideads", mAdView.getVisibility() == AdView.GONE).commit();
        }
    }

    private void initialize()
    {
        MobileAds.initialize(this, "ca-app-pub-9499594730627438~9516019647");

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        BASS.BASS_Init(-1, 44100, 0);
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_FLOATDSP, 1);
        BASS.BASS_SetConfig(BASS_CONFIG_AAC_MP4, 1);

        BASS.BASS_PluginLoad("libbass_aac.so", 0);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (HoldableViewPager) findViewById(R.id.container);
        mViewPager.setSwipeHold(true);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        TextView tab0 = (TextView) LayoutInflater.from(this).inflate(R.layout.tab, null);
        tab0.setText("再生リスト");
        tab0.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_playlist, 0, 0);
        tabLayout.getTabAt(0).setCustomView(tab0);

        TextView tab1 = (TextView) LayoutInflater.from(this).inflate(R.layout.tab, null);
        tab1.setText("コントロール");
        tab1.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_control, 0, 0);
        tabLayout.getTabAt(1).setCustomView(tab1);

        TextView tab2 = (TextView) LayoutInflater.from(this).inflate(R.layout.tab, null);
        tab2.setText("ループ");
        tab2.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_abloop, 0, 0);
        tabLayout.getTabAt(2).setCustomView(tab2);

        TextView tab3 = (TextView) LayoutInflater.from(this).inflate(R.layout.tab, null);
        tab3.setText("イコライザ");
        tab3.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_equalizer, 0, 0);
        tabLayout.getTabAt(3).setCustomView(tab3);

        TextView tab4 = (TextView) LayoutInflater.from(this).inflate(R.layout.tab, null);
        tab4.setText("エフェクト");
        tab4.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_effect, 0, 0);
        tabLayout.getTabAt(4).setCustomView(tab4);

        Button btnMenu = (Button)findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(this);

        Button btnRewind = (Button)findViewById(R.id.btnRewind);
        btnRewind.setOnLongClickListener(this);
        btnRewind.setOnTouchListener(this);

        Button btnForward = (Button)findViewById(R.id.btnForward);
        btnForward.setOnLongClickListener(this);
        btnForward.setOnTouchListener(this);

        menuSheet = new MenuSheet();
    }

    public boolean isAdsVisible() {
        if (mAdView.getVisibility() == AdView.GONE) return false;
        else return true;
    }

    public void setSync()
    {
        if(hSync != 0)
        {
            BASS.BASS_ChannelRemoveSync(hStream, hSync);
            hSync = 0;
        }

        LinearLayout ABButton = (LinearLayout)findViewById(R.id.ABButton);
        LinearLayout MarkerButton = (LinearLayout)findViewById(R.id.MarkerButton);
        ImageButton btnLoopmarker = (ImageButton)findViewById(R.id.btnLoopmarker);

        EffectFragment effectFragment = (EffectFragment)mSectionsPagerAdapter.getItem(4);
        if(effectFragment.isReverse()) {
            if(ABButton.getVisibility() == View.VISIBLE && bLoopA) // ABループ中でA位置が設定されている
                hSync = BASS.BASS_ChannelSetSync(hStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hStream, dLoopA), EndSync, null);
            else if(MarkerButton.getVisibility() == View.VISIBLE && btnLoopmarker.isSelected()) // マーカー再生中
            {
                LoopFragment loopFragment = (LoopFragment)mSectionsPagerAdapter.getItem(2);
                hSync = BASS.BASS_ChannelSetSync(hStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hStream, loopFragment.getMarkerDstPos()), EndSync, null);
            }
            else
                hSync = BASS.BASS_ChannelSetSync(hStream, BASS.BASS_SYNC_END, 0, EndSync, null);
        }
        else {
            if(ABButton.getVisibility() == View.VISIBLE && bLoopB) // ABループ中でB位置が設定されている
                hSync = BASS.BASS_ChannelSetSync(hStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hStream, dLoopB), EndSync, null);
            else if(MarkerButton.getVisibility() == View.VISIBLE && btnLoopmarker.isSelected()) // マーカー再生中
            {
                LoopFragment loopFragment = (LoopFragment)mSectionsPagerAdapter.getItem(2);
                hSync = BASS.BASS_ChannelSetSync(hStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hStream, loopFragment.getMarkerDstPos()), EndSync, null);
            }
            else
                hSync = BASS.BASS_ChannelSetSync(hStream, BASS.BASS_SYNC_END, 0, EndSync, null);
        }
    }

    private final BASS.SYNCPROC EndSync = new BASS.SYNCPROC()
    {
        public void SYNCPROC(int handle, int channel, int data, Object user)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LoopFragment loopFragment = (LoopFragment)mSectionsPagerAdapter.getItem(2);
                    EffectFragment effectFragment = (EffectFragment)mSectionsPagerAdapter.getItem(4);
                    LinearLayout ABButton = (LinearLayout)findViewById(R.id.ABButton);
                    LinearLayout MarkerButton = (LinearLayout)findViewById(R.id.MarkerButton);
                    ImageButton btnLoopmarker = (ImageButton)findViewById(R.id.btnLoopmarker);

                    if(ABButton.getVisibility() == View.VISIBLE && (bLoopA || bLoopB))
                    {
                        if(effectFragment.isReverse())
                            BASS.BASS_ChannelSetPosition(hStream, BASS.BASS_ChannelSeconds2Bytes(hStream, dLoopB), BASS.BASS_POS_BYTE);
                        else
                            BASS.BASS_ChannelSetPosition(hStream, BASS.BASS_ChannelSeconds2Bytes(hStream, dLoopA), BASS.BASS_POS_BYTE);
                        setSync();
                        if(BASS.BASS_ChannelIsActive(hStream) != BASS.BASS_ACTIVE_PLAYING)
                            BASS.BASS_ChannelPlay(hStream, false);
                    }
                    else if(MarkerButton.getVisibility() == View.VISIBLE && btnLoopmarker.isSelected())
                    {
                        BASS.BASS_ChannelSetPosition(hStream, BASS.BASS_ChannelSeconds2Bytes(hStream, loopFragment.getMarkerSrcPos()), BASS.BASS_POS_BYTE);
                        setSync();
                        if(BASS.BASS_ChannelIsActive(hStream) != BASS.BASS_ACTIVE_PLAYING)
                            BASS.BASS_ChannelPlay(hStream, false);
                    }
                    else
                    {
                        Button btnPlayMode = (Button) findViewById(R.id.btnPlayMode);
                        if (btnPlayMode.getText().equals("連続再生") || btnPlayMode.getText().equals("全曲リピート") || btnPlayMode.getText().equals("シャッフル")) {
                            PlaylistFragment playlistFragment = (PlaylistFragment) mSectionsPagerAdapter.getItem(0);
                            playlistFragment.playNext();
                        } else if (btnPlayMode.getText().equals("１曲リピート")) {
                            BASS.BASS_ChannelPlay(hStream, true);
                        }
                    }
                }
            });
        }
    };

    public void clearLoop()
    {
        dLoopA = 0.0;
        bLoopA = false;
        dLoopB = 0.0;
        bLoopB = false;
        LoopFragment loopFragment = (LoopFragment)mSectionsPagerAdapter.getItem(2);
        loopFragment.clearLoop();
    }

    @Override
    public void onDestroy() {
        BASS.BASS_Free();

        unbindService(mServiceConn);
        mBound = false;
        super.onDestroy();
    }

    private String readChangeLog() {
        StringBuffer sb = new StringBuffer("");
        String tmp;
        BufferedReader br = null;
        Boolean bFirst = true;
        try {
            br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.changelog)));
            while ((tmp = br.readLine()) != null) {
                if(bFirst) bFirst = false;
                else
                    sb.append(System.getProperty("line.separator"));
                sb.append(tmp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {}
            }
            return sb.toString();
        }
    }
}
