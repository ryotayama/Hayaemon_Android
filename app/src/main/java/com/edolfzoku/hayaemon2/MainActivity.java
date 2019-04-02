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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
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
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.un4seen.bass.BASS_AAC.BASS_CONFIG_AAC_MP4;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        View.OnLongClickListener, View.OnTouchListener, DrawerLayout.DrawerListener
{
    static int hFxVol;
    static int hStream;
    SectionsPagerAdapter mSectionsPagerAdapter;
    private DrawerLayout mDrawerLayout;
    boolean bLoopA, bLoopB;
    double dLoopA, dLoopB;
    private HoldableViewPager mViewPager;
    private int hSync;
    private IInAppBillingService mService;
    private ServiceConnection mServiceConn;
    private boolean bShowUpdateLog;
    private boolean bPlayNextByBPos;
    private boolean bWaitEnd = false;
    private BroadcastReceiver receiver;
    private AdView mAdView;
    private boolean mBound = false;
    private ForegroundService foregroundService = null;
    private ServiceConnection connection = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder service) {
            foregroundService = ((ForegroundService.ForegroundServiceBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            foregroundService = null;
        }
    };

    public ForegroundService getForegroundService() { return foregroundService; }

    public IInAppBillingService getService() { return mService; }
    public void setPlayNextByBPos(boolean bPlayNextByBPos) { this.bPlayNextByBPos = bPlayNextByBPos; }
    public boolean isPlayNextByBPos() { return bPlayNextByBPos; }
    public void setWaitEnd(boolean bWaitEnd) { this.bWaitEnd = bWaitEnd; }

    public MainActivity() {
        bLoopA = false;
        bLoopB = false;
        dLoopA = 0.0;
        dLoopB = 0.0;
        bShowUpdateLog = false;
    }

    private GestureDetector gestureDetector;
    private int nLastY = 0;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        startService(new Intent(this, ForegroundService.class));
        Intent in = new Intent(getApplicationContext(), ForegroundService.class);
        bindService(in, connection, Context.BIND_AUTO_CREATE);

        setContentView(R.layout.activity_main);

        initialize();
        loadData();

        Intent intent = getIntent();
        if(intent != null && intent.getType() != null) {
            if(intent.getType().contains("audio/")) {
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
                preferences.edit().putString("arPlaylists", gson.toJson(playlistFragment.getArPlaylists())).apply();
                preferences.edit().putString("arEffects", gson.toJson(playlistFragment.getArEffects())).apply();
                preferences.edit().putString("arLyrics", gson.toJson(playlistFragment.getArLyrics())).apply();
                preferences.edit().putString("arPlaylistNames", gson.toJson(playlistFragment.getArPlaylistNames())).apply();
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

        findViewById(R.id.btnShuffle).setOnClickListener(this);
        findViewById(R.id.btnRepeat).setOnClickListener(this);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.addDrawerListener(this);
        RelativeLayout relativeSave = findViewById(R.id.relativeSave);
        relativeSave.setOnTouchListener(this);
        relativeSave.setOnClickListener(this);
        RelativeLayout relativeLock = findViewById(R.id.relativeLock);
        relativeLock.setOnTouchListener(this);
        relativeLock.setOnClickListener(this);
        RelativeLayout relativeAddSong = findViewById(R.id.relativeAddSong);
        relativeAddSong.setOnTouchListener(this);
        relativeAddSong.setOnClickListener(this);
        RelativeLayout relativeItem = findViewById(R.id.relativeItem);
        relativeItem.setOnTouchListener(this);
        relativeItem.setOnClickListener(this);
        RelativeLayout relativeReport = findViewById(R.id.relativeReport);
        relativeReport.setOnTouchListener(this);
        relativeReport.setOnClickListener(this);
        RelativeLayout relativeReview = findViewById(R.id.relativeReview);
        relativeReview.setOnTouchListener(this);
        relativeReview.setOnClickListener(this);
        RelativeLayout relativeHideAds = findViewById(R.id.relativeHideAds);
        relativeHideAds.setOnTouchListener(this);
        relativeHideAds.setOnClickListener(this);
        RelativeLayout relativeInfo = findViewById(R.id.relativeInfo);
        relativeInfo.setOnTouchListener(this);
        relativeInfo.setOnClickListener(this);
        AnimationButton btnSetting = findViewById(R.id.btnSetting);
        btnSetting.setOnClickListener(this);

        findViewById(R.id.btnPlayInPlayingBar).setOnClickListener(this);
        AnimationButton btnForwardInPlayingBar = findViewById(R.id.btnForwardInPlayingBar);
        btnForwardInPlayingBar.setOnClickListener(this);
        btnForwardInPlayingBar.setOnLongClickListener(this);
        btnForwardInPlayingBar.setOnTouchListener(this);
        AnimationButton btnRewindInPlayingBar = findViewById(R.id.btnRewindInPlayingBar);
        btnRewindInPlayingBar.setOnClickListener(this);
        btnRewindInPlayingBar.setOnLongClickListener(this);
        btnRewindInPlayingBar.setOnTouchListener(this);
        findViewById(R.id.btnCloseInPlayingBar).setOnClickListener(this);
        findViewById(R.id.btnShuffleInPlayingBar).setOnClickListener(this);
        findViewById(R.id.btnRepeatInPlayingBar).setOnClickListener(this);
        findViewById(R.id.btnMoreInPlayingBar).setOnClickListener(this);
        final RelativeLayout relativePlaying = findViewById(R.id.relativePlaying);
        final SeekBar seekCurPos = findViewById(R.id.seekCurPos);
        seekCurPos.getProgressDrawable().setColorFilter(Color.parseColor("#A0A0A0"), PorterDuff.Mode.SRC_IN);
        seekCurPos.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                LoopFragment loopFragment = (LoopFragment)mSectionsPagerAdapter.getItem(1);
                loopFragment.setCurPos((double)seekBar.getProgress());
            }
        });
        final ImageView imgViewDown = findViewById(R.id.imgViewDown);
        imgViewDown.setOnClickListener(this);
        imgViewDown.setOnTouchListener(this);
        relativePlaying.setOnClickListener(this);
        gestureDetector = new GestureDetector(this, new SingleTapConfirm());
        relativePlaying.setOnTouchListener(this);
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onSingleTapUp(MotionEvent event)
        {
            return true;
        }
    }

    public void advanceAnimation(View view, String strTarget, int nFrom, int nTo, float fProgress)
    {
        RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)view.getLayoutParams();
        switch (strTarget) {
            case "height":
                param.height = (int) (nFrom + (nTo - nFrom) * fProgress);
                break;
            case "width":
                param.width = (int) (nFrom + (nTo - nFrom) * fProgress);
                break;
            case "leftMargin":
                param.leftMargin = (int) (nFrom + (nTo - nFrom) * fProgress);
                break;
            case "topMargin":
                param.topMargin = (int) (nFrom + (nTo - nFrom) * fProgress);
                break;
            case "rightMargin":
                param.rightMargin = (int) (nFrom + (nTo - nFrom) * fProgress);
                break;
            case "bottomMargin":
                param.bottomMargin = (int) (nFrom + (nTo - nFrom) * fProgress);
                break;
        }
        view.setLayoutParams(param);
    }

    public int getStatusBarHeight(){
        final Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.top;
    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView)
    {
    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView)
    {
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset)
    {
    }

    @Override
    public void onDrawerStateChanged(int newState)
    {
        if(newState == DrawerLayout.STATE_IDLE)
        {
            findViewById(R.id.relativeSave).setBackgroundColor(Color.argb(255, 255, 255, 255));
            findViewById(R.id.relativeLock).setBackgroundColor(Color.argb(255, 255, 255, 255));
            findViewById(R.id.relativeAddSong).setBackgroundColor(Color.argb(255, 255, 255, 255));
            findViewById(R.id.relativeHideAds).setBackgroundColor(Color.argb(255, 255, 255, 255));
            findViewById(R.id.relativeItem).setBackgroundColor(Color.argb(255, 255, 255, 255));
            findViewById(R.id.relativeReport).setBackgroundColor(Color.argb(255, 255, 255, 255));
            findViewById(R.id.relativeReview).setBackgroundColor(Color.argb(255, 255, 255, 255));
            findViewById(R.id.relativeInfo).setBackgroundColor(Color.argb(255, 255, 255, 255));
        }
    }

    public Uri copyFile(Uri uri)
    {
        int i = 0;
        String strPath;
        File file;
        while(true) {
            strPath = getFilesDir() + "/copied" + String.format(Locale.getDefault(), "%d", i);
            file = new File(strPath);
            if(!file.exists()) break;
            i++;
        }
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            FileOutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            if(in != null) {
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                in.close();
            }
            out.close();
        } catch (IOException e) {
            return null;
        }
        return Uri.parse(strPath);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction() == null) return;
                if(intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                    if(BASS.BASS_ChannelIsActive(hStream) == BASS.BASS_ACTIVE_PLAYING) {
                        PlaylistFragment playlistFragment = (PlaylistFragment) mSectionsPagerAdapter.getItem(0);
                        playlistFragment.pause();
                    }
                    return;
                }
                try {
                    Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
                    if(ownedItems != null && ownedItems.getInt("RESPONSE_CODE") == 0) {
                        ArrayList<String> ownedSkus =
                                ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                        ArrayList<String> purchaseDataList =
                                ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                        if(purchaseDataList != null && ownedSkus != null) {
                            for (int i = 0; i < purchaseDataList.size(); i++) {
                                String sku = ownedSkus.get(i);

                                if (sku.equals("hideads")) {
                                    hideAds();
                                }
                            }
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                    finish();
                }
            }
        };
        registerReceiver(receiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        registerReceiver(receiver, new IntentFilter("com.android.vending.billing.PURCHASES_UPDATED"));

        try {
            Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
            if(ownedItems != null && ownedItems.getInt("RESPONSE_CODE") == 0) {
                ArrayList<String> ownedSkus =
                        ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                ArrayList<String> purchaseDataList =
                        ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                if(purchaseDataList != null && ownedSkus != null) {
                    for (int i = 0; i < purchaseDataList.size(); i++) {
                        String sku = ownedSkus.get(i);

                        if (sku.equals("hideads"))
                            hideAds();
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if(bShowUpdateLog) {
            bShowUpdateLog = false;

            LayoutInflater inflater = getLayoutInflater();
            final View layout = inflater.inflate(R.layout.updatelogdialog,
                    (ViewGroup)findViewById(R.id.layout_root));
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            TextView title = new TextView(this);
            try {
                title.setText(String.format(Locale.getDefault(), "ハヤえもんAndroid版ver.%sに\nアップデートされました！", getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName));
            }
            catch(PackageManager.NameNotFoundException e) {
                title.setText("ハヤえもんAndroid版が\nアップデートされました！");
            }
            title.setGravity(Gravity.CENTER);
            title.setTextSize(18);
            LinearLayout.LayoutParams lp = new LinearLayout. LayoutParams(LinearLayout. LayoutParams. WRAP_CONTENT, LinearLayout. LayoutParams. WRAP_CONTENT);
            title. setPadding(0,16,0,16);
            title. setLayoutParams(lp);
            builder.setCustomTitle(title);
            title.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            builder.setView(layout);

            TextView textViewBlog = layout.findViewById(R.id.textViewBlog);
            String strBlog = "この内容は<a href=\"http://hayaemon.jp/blog/\">開発者ブログ</a>から";
            CharSequence blogChar = Html.fromHtml(strBlog);
            textViewBlog.setText(blogChar);
            MovementMethod mMethod = LinkMovementMethod.getInstance();
            textViewBlog.setMovementMethod(mMethod);
            textViewBlog.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            TextView textViewArticle = layout.findViewById(R.id.textViewArticle);
            String strArticle = "<a href=\"http://hayaemon.jp/blog/archives/6633\">→該当記事へ</a>";
            CharSequence blogChar2 = Html.fromHtml(strArticle);
            textViewArticle.setText(blogChar2);
            textViewArticle.setMovementMethod(mMethod);

            TextView textView = layout.findViewById(R.id.textView);
            textView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            textView.setMovementMethod(ScrollingMovementMethod.getInstance());
            textView.setText(readChangeLog());

            final AlertDialog alertDialog = builder.create();
            alertDialog.show();
            Button buttonClose = layout.findViewById(R.id.buttonClose);
            buttonClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Switch switchNextHidden = layout.findViewById(R.id.switchNextHidden);
                    boolean bChecked = switchNextHidden.isChecked();
                    SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                    preferences.edit().putBoolean("hideupdatelognext", bChecked).apply();
                    alertDialog.dismiss();
                }
            });
            Button buttonShare = layout.findViewById(R.id.buttonShare);
            buttonShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    try {
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "ハヤえもんAndroid版ver." + getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName + "にアップデートしました！ https://bit.ly/2D3jY89");
                    }
                    catch(PackageManager.NameNotFoundException e) {
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "ハヤえもんAndroid版をアップデートしました！ https://bit.ly/2D3jY89");
                    }
                    sendIntent.setType("*/*");
                    File file = getScreenshot(layout.getRootView());
                    Uri uri = FileProvider.getUriForFile(getApplicationContext(), "com.edolfzoku.hayaemon2", file);
                    int flag;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flag = PackageManager.MATCH_ALL;
                    else flag = PackageManager.MATCH_DEFAULT_ONLY;
                    List<ResolveInfo> resInfoList = getApplicationContext().getPackageManager().queryIntentActivities(sendIntent, flag);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        getApplicationContext().grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(sendIntent);

                    file.deleteOnExit();
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

        File file = new File(getExternalCacheDir() + "/export/capture.jpeg");
        if(!file.getParentFile().mkdir()) System.out.println("ディレクトリが作成できませんでした");
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
        ArrayList<ArrayList<EffectSaver>> arEffects = gson.fromJson(preferences.getString("arEffects",""), new TypeToken<ArrayList<ArrayList<EffectSaver>>>(){}.getType());
        ArrayList<ArrayList<String>> arLyrics = gson.fromJson(preferences.getString("arLyrics",""), new TypeToken<ArrayList<ArrayList<String>>>(){}.getType());
        ArrayList<String> arPlaylistNames = gson.fromJson(preferences.getString("arPlaylistNames",""), new TypeToken<ArrayList<String>>(){}.getType());
        List<String> arSongsPath = gson.fromJson(preferences.getString("arSongsPath",""), new TypeToken<List<String>>(){}.getType());
        if(arPlaylists != null && arPlaylistNames != null) {
            for(int i = 0; i < arPlaylists.size(); i++) {
                playlistFragment.setArPlaylists(arPlaylists);
                playlistFragment.setArPlaylistNames(arPlaylistNames);
            }
            if(arEffects != null && arPlaylists.size() == arEffects.size())
                playlistFragment.setArEffects(arEffects);
            else {
                arEffects = playlistFragment.getArEffects();
                for(int i = 0; i < arPlaylists.size(); i++) {
                    ArrayList<EffectSaver> arEffectSavers = new ArrayList<>();
                    ArrayList<SongItem> arSongs = arPlaylists.get(i);
                    for(int j = 0; j < arSongs.size(); j++) {
                        EffectSaver saver = new EffectSaver();
                        arEffectSavers.add(saver);
                    }
                    arEffects.add(arEffectSavers);
                }
            }
            if(arLyrics != null && arPlaylists.size() == arLyrics.size())
                playlistFragment.setArLyrics(arLyrics);
            else {
                arLyrics = playlistFragment.getArLyrics();
                for(int i = 0; i < arPlaylists.size(); i++) {
                    ArrayList<String> arTempLyrics = new ArrayList<>();
                    ArrayList<SongItem> arSongs = arPlaylists.get(i);
                    for(int j = 0; j < arSongs.size(); j++) {
                        arTempLyrics.add(null);
                    }
                    arLyrics.add(arTempLyrics);
                }
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
        boolean bHideUpdateLogNext = preferences.getBoolean("hideupdatelognext", false);
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
                bShowUpdateLog = true;
        }
        preferences.edit().putString("versionname", strCurrentVersionName).apply();

        bPlayNextByBPos = preferences.getBoolean("bPlayNextByBPos", false);

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
                    if(purchaseDataList != null && ownedSkus != null) {
                        for (int i = 0; i < purchaseDataList.size(); i++) {
                            String sku = ownedSkus.get(i);

                            if (sku.equals("hideads"))
                                hideAds();
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        int nShuffle = preferences.getInt("shufflemode", 0);
        AnimationButton btnShuffle = findViewById(R.id.btnShuffle);
        AnimationButton btnShuffleInPlayingBar = findViewById(R.id.btnShuffleInPlayingBar);
        if(nShuffle == 1)
        {
            btnShuffle.setContentDescription("シャッフルあり");
            btnShuffle.setImageResource(R.drawable.bar_button_mode_shuffle_on);
            btnShuffleInPlayingBar.setContentDescription("シャッフルあり");
            btnShuffleInPlayingBar.setImageResource(R.drawable.playing_large_mode_shuffle_on);
        }
        else if(nShuffle == 2)
        {
            btnShuffle.setContentDescription("１曲のみ");
            btnShuffle.setImageResource(R.drawable.bar_button_mode_single_on);
            btnShuffleInPlayingBar.setContentDescription("１曲のみ");
            btnShuffleInPlayingBar.setImageResource(R.drawable.playing_large_mode_single_on);
        }
        else
        {
            btnShuffle.setContentDescription("シャッフルなし");
            btnShuffle.setImageResource(R.drawable.bar_button_mode_shuffle);
            btnShuffleInPlayingBar.setContentDescription("シャッフルなし");
            btnShuffleInPlayingBar.setImageResource(R.drawable.playing_large_mode_shuffle);
        }

        int nRepeat = preferences.getInt("repeatmode", 0);
        AnimationButton btnRepeat = findViewById(R.id.btnRepeat);
        AnimationButton btnRepeatInPlayingBar = findViewById(R.id.btnRepeatInPlayingBar);
        if(nRepeat == 1)
        {
            btnRepeat.setContentDescription("全曲リピート");
            btnRepeat.setImageResource(R.drawable.bar_button_mode_repeat_all_on);
            btnRepeatInPlayingBar.setContentDescription("全曲リピート");
            btnRepeatInPlayingBar.setImageResource(R.drawable.playing_large_mode_repeat_all_on);
        }
        else if(nRepeat == 2)
        {
            btnRepeat.setContentDescription("１曲リピート");
            btnRepeat.setImageResource(R.drawable.bar_button_mode_repeat_single_on);
            btnRepeatInPlayingBar.setContentDescription("１曲リピート");
            btnRepeatInPlayingBar.setImageResource(R.drawable.playing_large_mode_repeat_one_on);
        }
        else
        {
            btnRepeat.setContentDescription("リピートなし");
            btnRepeat.setImageResource(R.drawable.bar_button_mode_repeat);
            btnRepeatInPlayingBar.setContentDescription("リピートなし");
            btnRepeatInPlayingBar.setImageResource(R.drawable.playing_large_mode_repeat_all);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
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
        if(v.getId() == R.id.btnRewind || v.getId() == R.id.btnRewindInPlayingBar)
        {
            if(hStream == 0) return false;
            int chan = BASS_FX.BASS_FX_TempoGetSource(hStream);
            EffectFragment effectFragment = (EffectFragment)mSectionsPagerAdapter.getItem(4);
            if(effectFragment.isReverse())
                BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_FORWARD);
            else
                BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_REVERSE);
            ControlFragment controlFragment = (ControlFragment)mSectionsPagerAdapter.getItem(2);
            BASS.BASS_ChannelSetAttribute(hStream, BASS_FX.BASS_ATTRIB_TEMPO, controlFragment.fSpeed + 100);
            AnimationButton btnRewind = findViewById(R.id.btnRewind);
            btnRewind.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#FF007AFF"), PorterDuff.Mode.SRC_IN));
            AnimationButton btnRewindInPlayingBar = findViewById(R.id.btnRewindInPlayingBar);
            btnRewindInPlayingBar.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#FF007AFF"), PorterDuff.Mode.SRC_IN));
            return true;
        }
        else if(v.getId() == R.id.btnForward || v.getId() == R.id.btnForwardInPlayingBar)
        {
            if(hStream == 0) return false;
            ControlFragment controlFragment = (ControlFragment)mSectionsPagerAdapter.getItem(2);
            BASS.BASS_ChannelSetAttribute(hStream, BASS_FX.BASS_ATTRIB_TEMPO, controlFragment.fSpeed + 100);
            AnimationButton btnForward = findViewById(R.id.btnForward);
            btnForward.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#FF007AFF"), PorterDuff.Mode.SRC_IN));
            AnimationButton btnForwardInPlayingBar = findViewById(R.id.btnForwardInPlayingBar);
            btnForwardInPlayingBar.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#FF007AFF"), PorterDuff.Mode.SRC_IN));
            return true;
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if(v.getId() == R.id.relativePlaying || v.getId() == R.id.imgViewDown)
        {
            final SeekBar seekCurPos = findViewById(R.id.seekCurPos);
            final RelativeLayout relativePlaying = findViewById(R.id.relativePlaying);
            int nY = (int) event.getRawY();
            if (gestureDetector.onTouchEvent(event)) return false;
            if(seekCurPos.getVisibility() != View.VISIBLE) {
                PlaylistFragment playlistFragment = (PlaylistFragment) mSectionsPagerAdapter.getItem(0);
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    RelativeLayout.LayoutParams paramContainer = (RelativeLayout.LayoutParams) findViewById(R.id.container).getLayoutParams();
                    RelativeLayout.LayoutParams paramRecording = (RelativeLayout.LayoutParams) findViewById(R.id.relativeRecording).getLayoutParams();
                    if (playlistFragment.hRecord != 0) {
                        paramContainer.addRule(RelativeLayout.ABOVE, R.id.relativeRecording);
                        paramContainer.bottomMargin = 0;
                        paramRecording.addRule(RelativeLayout.ABOVE, R.id.adView);
                        paramRecording.bottomMargin = (int) (60.0 * getResources().getDisplayMetrics().density + 0.5);
                    } else {
                        paramContainer.addRule(RelativeLayout.ABOVE, R.id.adView);
                        paramContainer.bottomMargin = (int) (60.0 * getResources().getDisplayMetrics().density + 0.5);
                    }
                    RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) relativePlaying.getLayoutParams();
                    int nHeight = param.height - (nY - nLastY);
                    int nMinHeight = (int) (82.0 * getResources().getDisplayMetrics().density + 0.5);
                    int nMaxHeight = (int) (142.0 * getResources().getDisplayMetrics().density + 0.5);
                    if (nHeight < nMinHeight) nHeight = nMinHeight;
                    else if (nHeight > nMaxHeight) nHeight = nMaxHeight;
                    param.height = nHeight;
                    relativePlaying.setLayoutParams(param);
                }
                nLastY = nY;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) relativePlaying.getLayoutParams();
                    int nMinHeight = (int) (82.0 * getResources().getDisplayMetrics().density + 0.5);
                    if (param.height > nMinHeight) return false;
                    else {
                        RelativeLayout.LayoutParams paramContainer = (RelativeLayout.LayoutParams) findViewById(R.id.container).getLayoutParams();
                        RelativeLayout.LayoutParams paramRecording = (RelativeLayout.LayoutParams) findViewById(R.id.relativeRecording).getLayoutParams();
                        if (playlistFragment.hRecord != 0) {
                            paramContainer.addRule(RelativeLayout.ABOVE, R.id.relativeRecording);
                            paramContainer.bottomMargin = 0;
                            paramRecording.addRule(RelativeLayout.ABOVE, R.id.relativePlaying);
                            paramRecording.bottomMargin = (int) (-22 * getResources().getDisplayMetrics().density + 0.5);
                        } else {
                            paramContainer.addRule(RelativeLayout.ABOVE, R.id.relativePlaying);
                            paramContainer.bottomMargin = (int) (-22 * getResources().getDisplayMetrics().density + 0.5);
                        }
                    }
                }
            }
            else {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    final TabLayout tabLayout = findViewById(R.id.tabs);
                    final int nCurrentHeight = getResources().getDisplayMetrics().heightPixels - tabLayout.getHeight() - findViewById(R.id.linearControl).getHeight() - getStatusBarHeight() + (int) (16.0 * getResources().getDisplayMetrics().density + 0.5);
                    final int nMaxHeight = getResources().getDisplayMetrics().heightPixels - tabLayout.getHeight() - getStatusBarHeight() + (int) (22.0 * getResources().getDisplayMetrics().density + 0.5);
                    final int nMinHeight = (int) (82.0 * getResources().getDisplayMetrics().density + 0.5);
                    int nMinTranslationY = nCurrentHeight - nMaxHeight;
                    int nMaxTranslationY = nCurrentHeight - nMinHeight;
                    int nTranslationY = nY - nLastY;
                    if(nTranslationY < nMinTranslationY) nTranslationY = nMinTranslationY;
                    else if(nTranslationY > nMaxTranslationY) nTranslationY = nMaxTranslationY;
                    relativePlaying.setTranslationY(nTranslationY);
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    nLastY = (int)relativePlaying.getTranslationY() + nY;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if(relativePlaying.getTranslationY() > (int) (100.0 * getResources().getDisplayMetrics().density + 0.5)) {
                        downViewPlaying(false);
                    }
                    else {
                        final int nTranslationYFrom = (int)relativePlaying.getTranslationY();
                        final int nTranslationY = 0;

                        ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
                        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                float fProgress = valueAnimator.getAnimatedFraction();
                                relativePlaying.setTranslationY(nTranslationYFrom + (nTranslationY - nTranslationYFrom) * fProgress);
                            }
                        });
                        anim.setDuration(200).start();
                    }
                }
            }
            if(event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_UP)
                return true;
            return (v.getId() == R.id.relativePlaying && seekCurPos.getVisibility() == View.VISIBLE);
        }

        if(event.getAction() == MotionEvent.ACTION_UP)
        {
            findViewById(R.id.relativeSave).setBackgroundColor(Color.argb(255, 255, 255, 255));
            findViewById(R.id.relativeLock).setBackgroundColor(Color.argb(255, 255, 255, 255));
            findViewById(R.id.relativeAddSong).setBackgroundColor(Color.argb(255, 255, 255, 255));
            findViewById(R.id.relativeHideAds).setBackgroundColor(Color.argb(255, 255, 255, 255));
            findViewById(R.id.relativeItem).setBackgroundColor(Color.argb(255, 255, 255, 255));
            findViewById(R.id.relativeReport).setBackgroundColor(Color.argb(255, 255, 255, 255));
            findViewById(R.id.relativeReview).setBackgroundColor(Color.argb(255, 255, 255, 255));
            findViewById(R.id.relativeInfo).setBackgroundColor(Color.argb(255, 255, 255, 255));
            if(v.getId() == R.id.btnRewind || v.getId() == R.id.btnRewindInPlayingBar)
            {
                if(hStream == 0) return false;
                int chan = BASS_FX.BASS_FX_TempoGetSource(hStream);
                EffectFragment effectFragment = (EffectFragment)mSectionsPagerAdapter.getItem(4);
                if(effectFragment.isReverse())
                    BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_REVERSE);
                else
                    BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_FORWARD);
                ControlFragment controlFragment = (ControlFragment)mSectionsPagerAdapter.getItem(2);
                BASS.BASS_ChannelSetAttribute(hStream, BASS_FX.BASS_ATTRIB_TEMPO, controlFragment.fSpeed);
                AnimationButton btnRewind = findViewById(R.id.btnRewind);
                btnRewind.clearColorFilter();
                AnimationButton btnRewindInPlayingBar = findViewById(R.id.btnRewindInPlayingBar);
                btnRewindInPlayingBar.clearColorFilter();
            }
            else if(v.getId() == R.id.btnForward || v.getId() == R.id.btnForwardInPlayingBar)
            {
                if(hStream == 0) return false;
                ControlFragment controlFragment = (ControlFragment)mSectionsPagerAdapter.getItem(2);
                BASS.BASS_ChannelSetAttribute(hStream, BASS_FX.BASS_ATTRIB_TEMPO, controlFragment.fSpeed);
                AnimationButton btnForward = findViewById(R.id.btnForward);
                btnForward.clearColorFilter();
                AnimationButton btnForwardInPlayingBar = findViewById(R.id.btnForwardInPlayingBar);
                btnForwardInPlayingBar.clearColorFilter();
            }
        }
        if(event.getAction() == MotionEvent.ACTION_DOWN)
        {
            if(v.getId() == R.id.relativeSave)
                findViewById(R.id.relativeSave).setBackgroundColor(Color.argb(255, 229, 229, 229));
            if(v.getId() == R.id.relativeLock)
                findViewById(R.id.relativeLock).setBackgroundColor(Color.argb(255, 229, 229, 229));
            if(v.getId() == R.id.relativeAddSong)
                findViewById(R.id.relativeAddSong).setBackgroundColor(Color.argb(255, 229, 229, 229));
            if(v.getId() == R.id.relativeHideAds)
                findViewById(R.id.relativeHideAds).setBackgroundColor(Color.argb(255, 229, 229, 229));
            if(v.getId() == R.id.relativeItem)
                findViewById(R.id.relativeItem).setBackgroundColor(Color.argb(255, 229, 229, 229));
            if(v.getId() == R.id.relativeReport)
                findViewById(R.id.relativeReport).setBackgroundColor(Color.argb(255, 229, 229, 229));
            if(v.getId() == R.id.relativeReview)
                findViewById(R.id.relativeReview).setBackgroundColor(Color.argb(255, 229, 229, 229));
            if(v.getId() == R.id.relativeInfo)
                findViewById(R.id.relativeInfo).setBackgroundColor(Color.argb(255, 229, 229, 229));
        }
        return false;
    }

    @Override
    public void onClick(View v)
    {
        final PlaylistFragment playlistFragment = (PlaylistFragment)mSectionsPagerAdapter.getItem(0);
        if(v.getId() == R.id.btnMenu)
        {
            findViewById(R.id.textPlaying).setVisibility(hStream == 0 ? View.GONE : View.VISIBLE);
            findViewById(R.id.relativePlayingInMenu).setVisibility(hStream == 0 ? View.GONE : View.VISIBLE);
            findViewById(R.id.relativeSave).setVisibility(hStream == 0 ? View.GONE : View.VISIBLE);
            findViewById(R.id.relativeLock).setVisibility(hStream == 0 ? View.GONE : View.VISIBLE);
            findViewById(R.id.dividerMenu).setVisibility(hStream == 0 ? View.GONE : View.VISIBLE);
            if(!isAdsVisible()) findViewById(R.id.relativeHideAds).setVisibility(View.GONE);
            if(hStream != 0) {
                playlistFragment.selectPlaylist(playlistFragment.getPlayingPlaylist());
                playlistFragment.setSelectedItem(playlistFragment.getPlaying());

                SongItem item = playlistFragment.getArPlaylists().get(playlistFragment.getPlayingPlaylist()).get(playlistFragment.getPlaying());
                ImageView imgViewArtworkInMenu = findViewById(R.id.imgViewArtworkInMenu);
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                Bitmap bitmap = null;
                boolean bError = false;
                try {
                    mmr.setDataSource(getApplicationContext(), Uri.parse(item.getPath()));
                }
                catch(Exception e) {
                    bError = true;
                }
                if(!bError) {
                    byte[] data = mmr.getEmbeddedPicture();
                    if(data != null) {
                        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    }
                }
                if(bitmap != null) imgViewArtworkInMenu.setImageBitmap(bitmap);
                else imgViewArtworkInMenu.setImageResource(R.drawable.playing_large_artwork);
                TextView textTitleInMenu = findViewById(R.id.textTitleInMenu);
                textTitleInMenu.setText(item.getTitle());
                TextView textArtistInMenu = findViewById(R.id.textArtistInMenu);
                if(item.getArtist() == null || item.getArtist().equals(""))
                {
                    textArtistInMenu.setTextColor(Color.argb(255, 147, 156, 160));
                    textArtistInMenu.setText("〈不明なアーティスト〉");
                }
                else
                {
                    textArtistInMenu.setTextColor(Color.argb(255, 102, 102, 102));
                    textArtistInMenu.setText(item.getArtist());
                }

                ArrayList<EffectSaver> arEffectSavers = playlistFragment.getArEffects().get(playlistFragment.getPlayingPlaylist());
                EffectSaver saver = arEffectSavers.get(playlistFragment.getPlaying());
                ImageView imgLock = findViewById(R.id.imgLockInMenu);
                TextView textLock = findViewById(R.id.textLock);
                if(saver.isSave()) {
                    imgLock.setImageResource(R.drawable.leftmenu_playing_unlock);
                    textLock.setText("各画面の設定保持を解除");
                }
                else {
                    imgLock.setImageResource(R.drawable.leftmenu_playing_lock);
                    textLock.setText("各画面の設定を保持");
                }
            }
            mDrawerLayout.openDrawer(Gravity.START);
        }
        else if(v.getId() == R.id.btnShuffle || v.getId() == R.id.btnShuffleInPlayingBar)
        {
            AnimationButton btnShuffle = findViewById(R.id.btnShuffle);
            AnimationButton btnShuffleInPlayingBar = findViewById(R.id.btnShuffleInPlayingBar);
            switch(btnShuffle.getContentDescription().toString()) {
                case "シャッフルなし":
                    btnShuffle.setContentDescription("シャッフルあり");
                    btnShuffle.setImageResource(R.drawable.bar_button_mode_shuffle_on);
                    btnShuffleInPlayingBar.setContentDescription("シャッフルあり");
                    btnShuffleInPlayingBar.setImageResource(R.drawable.playing_large_mode_shuffle_on);
                    break;
                case "シャッフルあり":
                    btnShuffle.setContentDescription("１曲のみ");
                    btnShuffle.setImageResource(R.drawable.bar_button_mode_single_on);
                    btnShuffleInPlayingBar.setContentDescription("１曲のみ");
                    btnShuffleInPlayingBar.setImageResource(R.drawable.playing_large_mode_single_on);
                    break;
                default:
                    btnShuffle.setContentDescription("シャッフルなし");
                    btnShuffle.setImageResource(R.drawable.bar_button_mode_shuffle);
                    btnShuffleInPlayingBar.setContentDescription("シャッフルなし");
                    btnShuffleInPlayingBar.setImageResource(R.drawable.playing_large_mode_shuffle);
            }
            playlistFragment.saveFiles(false, false, false, false, true);
        }
        else if(v.getId() == R.id.btnRepeat || v.getId() == R.id.btnRepeatInPlayingBar)
        {
            AnimationButton btnRepeat = findViewById(R.id.btnRepeat);
            AnimationButton btnRepeatInPlayingBar = findViewById(R.id.btnRepeatInPlayingBar);
            switch (btnRepeat.getContentDescription().toString()) {
                case "リピートなし":
                    btnRepeat.setContentDescription("全曲リピート");
                    btnRepeat.setImageResource(R.drawable.bar_button_mode_repeat_all_on);
                    btnRepeatInPlayingBar.setContentDescription("全曲リピート");
                    btnRepeatInPlayingBar.setImageResource(R.drawable.playing_large_mode_repeat_all_on);
                    break;
                case "全曲リピート":
                    btnRepeat.setContentDescription("１曲リピート");
                    btnRepeat.setImageResource(R.drawable.bar_button_mode_repeat_single_on);
                    btnRepeatInPlayingBar.setContentDescription("１曲リピート");
                    btnRepeatInPlayingBar.setImageResource(R.drawable.playing_large_mode_repeat_one_on);
                    break;
                default:
                    btnRepeat.setContentDescription("リピートなし");
                    btnRepeat.setImageResource(R.drawable.bar_button_mode_repeat);
                    btnRepeatInPlayingBar.setContentDescription("リピートなし");
                    btnRepeatInPlayingBar.setImageResource(R.drawable.playing_large_mode_repeat_all);
            }
            playlistFragment.saveFiles(false, false, false, false, true);
        }
        else if(v.getId() == R.id.relativeLock)
        {
            mDrawerLayout.closeDrawer(Gravity.START);

            ArrayList<EffectSaver> arEffectSavers = playlistFragment.getArEffects().get(playlistFragment.getPlayingPlaylist());
            EffectSaver saver = arEffectSavers.get(playlistFragment.getPlaying());
            if(saver.isSave()) {
                saver.setSave(false);
                playlistFragment.getSongsAdapter().notifyDataSetChanged();

                playlistFragment.saveFiles(false, true, false, false, false);
            }
            else {
                playlistFragment.setSavingEffect();
                playlistFragment.getSongsAdapter().notifyDataSetChanged();
            }
        }
        else if(v.getId() == R.id.relativeSave)
        {
            mDrawerLayout.closeDrawer(Gravity.START);
            showSaveExportMenu();
        }
        else if(v.getId() == R.id.relativeAddSong)
        {
            mDrawerLayout.closeDrawer(Gravity.START);

            final BottomMenu menu = new BottomMenu(this);
            menu.setTitle("曲を追加");
            menu.addMenu("端末内から追加", R.drawable.actionsheet_music, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menu.dismiss();
                    open();
                }
            });
            if(Build.VERSION.SDK_INT >= 18) {
                menu.addMenu("ギャラリーから追加", R.drawable.actionsheet_film, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menu.dismiss();
                        openGallery();
                    }
                });
            }
            final Activity activity = this;
            menu.addMenu("URLから追加", R.drawable.actionsheet_globe, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menu.dismiss();

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("URLから追加");
                    LinearLayout linearLayout = new LinearLayout(activity);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    final EditText editURL = new EditText (activity);
                    editURL.setHint("URL");
                    editURL.setHintTextColor(Color.argb(255, 192, 192, 192));
                    editURL.setText("");
                    linearLayout.addView(editURL);
                    builder.setView(linearLayout);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            playlistFragment.startAddURL(editURL.getText().toString());
                        }
                    });
                    builder.setNegativeButton("キャンセル", null);
                    final AlertDialog alertDialog = builder.create();
                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                    {
                        @Override
                        public void onShow(DialogInterface arg0)
                        {
                            editURL.requestFocus();
                            editURL.setSelection(editURL.getText().toString().length());
                            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (null != imm) imm.showSoftInput(editURL, 0);
                        }
                    });
                    alertDialog.show();
                }
            });
            menu.setCancelMenu();
            menu.show();
        }
        else if(v.getId() == R.id.relativeHideAds)
        {
            try {
                Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "hideads", "inapp", "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkVvqgLyPSTyJKuyNw3Z0luaxCnOtbFwj65HGYmDS4KiyGaJNgFsLOc9wpmIQaQI+zrntxbufWXsT0gIh1/MRRmX2FgA0G6WDS0+w39ZsbgJRbXsxOzOOZaHbSo2NLOA29GXPo9FraFtNrOL9v4vLu7hxDPdfqoFNR80BUWwQqMBsiMNFqJ12sq1HzxHd2MIk/QooBZIB3EeM0QX5EYIsWcaKIAyzetuKjRGvO9Oi2a86dOBUfOFnHMMCvQ5+dldx5UkzmnhlbTm/KBWQCO3AqNy82NKxN9ND6GWVrlHuQGYX1FRiApMeXCmEvmwEyU2ArztpV8CfHyK2d0mM4bp0bwIDAQAB");
                int response = buyIntentBundle.getInt("RESPONSE_CODE");
                if(response == 0) {
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    if(pendingIntent != null)
                        startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), 0, 0, 0);
                }
                else if(response == 7){
                    hideAds();
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            mDrawerLayout.closeDrawer(Gravity.START);
        }
        else if(v.getId() == R.id.relativeItem)
        {
            mDrawerLayout.closeDrawer(Gravity.START);
            openItem();
        }
        else if(v.getId() == R.id.relativeReport)
        {
            Uri uri = Uri.parse("https://twitter.com/ryota_yama");
            Intent i = new Intent(Intent.ACTION_VIEW,uri);
            startActivity(i);
            mDrawerLayout.closeDrawer(Gravity.START);
        }
        else if(v.getId() == R.id.relativeReview)
        {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.edolfzoku.hayaemon2&hl=ja");
            Intent i = new Intent(Intent.ACTION_VIEW,uri);
            startActivity(i);
            mDrawerLayout.closeDrawer(Gravity.START);
        }
        else if(v.getId() == R.id.relativeInfo)
        {
            mDrawerLayout.closeDrawer(Gravity.START);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            try {
                String strVersionName = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
                builder.setMessage("バージョン: Android ver." + strVersionName);
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
        else if(v.getId() == R.id.btnSetting)
        {
            mDrawerLayout.closeDrawer(Gravity.START);
            openSetting();
        }
        else if(v.getId() == R.id.btnPlayInPlayingBar)
            playlistFragment.onPlayBtnClicked();
        else if(v.getId() == R.id.btnForwardInPlayingBar)
            playlistFragment.playNext(true);
        else if(v.getId() == R.id.btnRewindInPlayingBar) {
            if(hStream == 0) return;
            EffectFragment effectFragment = (EffectFragment)mSectionsPagerAdapter.getItem(4);
            if(!effectFragment.isReverse() && BASS.BASS_ChannelBytes2Seconds(hStream, BASS.BASS_ChannelGetPosition(hStream, BASS.BASS_POS_BYTE)) > dLoopA + 1.0)
                BASS.BASS_ChannelSetPosition(hStream, BASS.BASS_ChannelSeconds2Bytes(hStream, dLoopA), BASS.BASS_POS_BYTE);
            else if(effectFragment.isReverse() && BASS.BASS_ChannelBytes2Seconds(hStream, BASS.BASS_ChannelGetPosition(hStream, BASS.BASS_POS_BYTE)) < dLoopA - 1.0)
                BASS.BASS_ChannelSetPosition(hStream, BASS.BASS_ChannelSeconds2Bytes(hStream, dLoopB), BASS.BASS_POS_BYTE);
            else
                playlistFragment.playPrev();
        }
        else if(v.getId() == R.id.relativePlaying)
            upViewPlaying();
        else if(v.getId() == R.id.imgViewDown)
            downViewPlaying(false);
        else if(v.getId() == R.id.btnCloseInPlayingBar)
            playlistFragment.stop();
        else if(v.getId() == R.id.btnMoreInPlayingBar) {
            final BottomMenu menu = new BottomMenu(this);
            final int nPlaying = playlistFragment.getPlaying();
            playlistFragment.setSelectedItem(nPlaying);
            SongItem item = playlistFragment.getArPlaylists().get(playlistFragment.getPlayingPlaylist()).get(nPlaying);
            menu.setTitle(item.getTitle());
            menu.addMenu("保存／エクスポート", R.drawable.actionsheet_save, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menu.dismiss();
                    showSaveExportMenu();
                }
            });
            menu.addMenu("タイトルとアーティスト名を変更", R.drawable.actionsheet_edit, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menu.dismiss();
                    playlistFragment.changeTitleAndArtist(nPlaying);
                }
            });
            menu.addMenu("歌詞を表示", R.drawable.actionsheet_file_text, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menu.dismiss();
                    downViewPlaying(false);
                    playlistFragment.showLyrics();
                    mViewPager.setCurrentItem(0);
                }
            });
            ArrayList<EffectSaver> arEffectSavers = playlistFragment.getArEffects().get(playlistFragment.getSelectedPlaylist());
            final EffectSaver saver = arEffectSavers.get(nPlaying);
            if(saver.isSave())
            {
                menu.addMenu("各画面の設定保持を解除", R.drawable.actionsheet_unlock, new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        saver.setSave(false);
                        playlistFragment.getSongsAdapter().notifyDataSetChanged();

                        playlistFragment.saveFiles(false, true, false, false, false);
                        menu.dismiss();
                    }
                });
            }
            else
            {
                menu.addMenu("各画面の設定を保持", R.drawable.actionsheet_lock, new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        playlistFragment.setSavingEffect();
                        playlistFragment.getSongsAdapter().notifyDataSetChanged();
                        menu.dismiss();
                    }
                });
            }
            menu.setCancelMenu();
            menu.show();
        }
    }

    public void upViewPlaying()
    {
        final PlaylistFragment playlistFragment = (PlaylistFragment)mSectionsPagerAdapter.getItem(0);
        playlistFragment.selectPlaylist(playlistFragment.getPlayingPlaylist());
        final ImageView imgViewDown = findViewById(R.id.imgViewDown);
        final SeekBar seekCurPos = findViewById(R.id.seekCurPos);
        final RelativeLayout relativePlaying = findViewById(R.id.relativePlaying);
        relativePlaying.setOnClickListener(null);
        final long lDuration = 400;
        int nScreenWidth = getResources().getDisplayMetrics().widthPixels;
        relativePlaying.setBackgroundResource(R.drawable.playingview);
        RelativeLayout.LayoutParams paramContainer = (RelativeLayout.LayoutParams) findViewById(R.id.container).getLayoutParams();
        RelativeLayout.LayoutParams paramRecording = (RelativeLayout.LayoutParams) findViewById(R.id.relativeRecording).getLayoutParams();
        if (playlistFragment.hRecord != 0) {
            paramContainer.addRule(RelativeLayout.ABOVE, R.id.relativeRecording);
            paramContainer.bottomMargin = 0;
            paramRecording.addRule(RelativeLayout.ABOVE, R.id.adView);
            paramRecording.bottomMargin = (int) (60.0 * getResources().getDisplayMetrics().density + 0.5);
        } else {
            paramContainer.addRule(RelativeLayout.ABOVE, R.id.adView);
            paramContainer.bottomMargin = (int) (60.0 * getResources().getDisplayMetrics().density + 0.5);
        }

        final TabLayout tabLayout = findViewById(R.id.tabs);
        final View viewSep2 = findViewById(R.id.viewSep2);
        final AdView adView = findViewById(R.id.adView);
        final ImageView imgViewArtwork = findViewById(R.id.imgViewArtworkInPlayingBar);
        final TextView textTitle = findViewById(R.id.textTitleInPlayingBar);
        final TextView textArtist = findViewById(R.id.textArtistInPlayingBar);
        final AnimationButton btnPlay = findViewById(R.id.btnPlayInPlayingBar);
        final AnimationButton btnForward = findViewById(R.id.btnForwardInPlayingBar);
        final TextView textCurPos = findViewById(R.id.textCurPos);
        final TextView textRemain = findViewById(R.id.textRemain);
        final AnimationButton btnRewind = findViewById(R.id.btnRewindInPlayingBar);
        final AnimationButton btnMore = findViewById(R.id.btnMoreInPlayingBar);
        final AnimationButton btnShuffle = findViewById(R.id.btnShuffleInPlayingBar);
        final AnimationButton btnRepeat = findViewById(R.id.btnRepeatInPlayingBar);

        final RelativeLayout.LayoutParams paramTitle = (RelativeLayout.LayoutParams) textTitle.getLayoutParams();
        final RelativeLayout.LayoutParams paramArtist = (RelativeLayout.LayoutParams) textArtist.getLayoutParams();
        final RelativeLayout.LayoutParams paramBtnPlay = (RelativeLayout.LayoutParams) btnPlay.getLayoutParams();
        final RelativeLayout.LayoutParams paramBtnForward = (RelativeLayout.LayoutParams) btnForward.getLayoutParams();

        textTitle.setGravity(Gravity.CENTER);
        textArtist.setGravity(Gravity.CENTER);
        paramTitle.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        paramArtist.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);

        final int nTranslationYFrom = (int)relativePlaying.getTranslationY();
        final int nTranslationY = 0;
        final int nRelativePlayingHeightFrom = relativePlaying.getHeight();
        final int nRelativePlayingHeight = getResources().getDisplayMetrics().heightPixels - tabLayout.getHeight() - findViewById(R.id.linearControl).getHeight() - getStatusBarHeight() + (int) (16.0 * getResources().getDisplayMetrics().density + 0.5);
        final int nArtworkWidthFrom = imgViewArtwork.getWidth();
        final int nArtworkWidth = nScreenWidth / 2;
        final int nArtworkMarginFrom = (int) (8.0 * getResources().getDisplayMetrics().density + 0.5);
        final int nArtworkLeftMargin = nScreenWidth / 2 - nArtworkWidth / 2;
        final int nArtworkTopMargin = (int) (64.0 * getResources().getDisplayMetrics().density + 0.5);
        final int nTitleTopMarginFrom = paramTitle.topMargin;
        final int nTitleLeftMarginFrom = paramTitle.leftMargin;
        final int nTitleRightMarginFrom = paramTitle.rightMargin;
        final int nTitleMargin = (int) (32.0 * getResources().getDisplayMetrics().density + 0.5);
        final int nTitleTopMargin = nArtworkTopMargin + nArtworkWidth + (int) (32.0 * getResources().getDisplayMetrics().density) + (int) (24.0 * getResources().getDisplayMetrics().density) + (int) (34.0 * getResources().getDisplayMetrics().density);
        final int nArtistTopMarginFrom = paramArtist.topMargin;
        final int nArtistTopMargin = nTitleTopMargin + textTitle.getHeight() + (int) (4.0 * getResources().getDisplayMetrics().density + 0.5);
        final int nBtnPlayTopMargin = nArtistTopMargin + textArtist.getHeight() + (int) (20.0 * getResources().getDisplayMetrics().density + 0.5);
        final int nBtnPlayRightMarginFrom = paramBtnPlay.rightMargin;
        final int nBtnPlayRightMargin = nScreenWidth / 2 - btnPlay.getWidth() / 2;
        final int nBtnForwardRightMarginFrom = paramBtnForward.rightMargin;
        final int nBtnForwardRightMargin = nBtnPlayRightMargin - btnForward.getWidth() - (int) (16.0 * getResources().getDisplayMetrics().density + 0.5);
        final float fTitleFontFrom = 13.0f;
        final float fTitleFont = 15.0f;
        Paint paint = new Paint();
        paint.setTextSize(textTitle.getTextSize());
        final int nTitleWidthFrom = (int) paint.measureText(textTitle.getText().toString());
        final int nTitleWidth = nScreenWidth;
        final float fArtistFontFrom = 10.0f;
        final float fArtistFont = 13.0f;
        paint.setTextSize(textArtist.getTextSize());
        final int nArtistWidthFrom = (int) paint.measureText(textArtist.getText().toString());
        final int nArtistWidth = nScreenWidth;
        final int nBtnHeightFrom = (int) (60.0 * getResources().getDisplayMetrics().density + 0.5);
        final int nBtnHeight = (int) (44.0 * getResources().getDisplayMetrics().density + 0.5);

        ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fProgress = valueAnimator.getAnimatedFraction();
                advanceAnimation(tabLayout, "bottomMargin", 0, -tabLayout.getHeight(), fProgress);
                advanceAnimation(viewSep2, "bottomMargin", 0, tabLayout.getHeight(), fProgress);
                relativePlaying.setTranslationY(nTranslationYFrom + (nTranslationY - nTranslationYFrom) * fProgress);
                advanceAnimation(relativePlaying, "height", nRelativePlayingHeightFrom, nRelativePlayingHeight, fProgress);
                advanceAnimation(imgViewArtwork, "width", nArtworkWidthFrom, nArtworkWidth, fProgress);
                advanceAnimation(imgViewArtwork, "height", nArtworkWidthFrom, nArtworkWidth, fProgress);
                advanceAnimation(imgViewArtwork, "leftMargin", nArtworkMarginFrom, nArtworkLeftMargin, fProgress);
                advanceAnimation(imgViewArtwork, "topMargin", nArtworkMarginFrom, nArtworkTopMargin, fProgress);
                advanceAnimation(textTitle, "width", nTitleWidthFrom, nTitleWidth, fProgress);
                advanceAnimation(textTitle, "topMargin", nTitleTopMarginFrom, nTitleTopMargin, fProgress);
                advanceAnimation(textTitle, "leftMargin", nTitleLeftMarginFrom, nTitleMargin, fProgress);
                advanceAnimation(textTitle, "rightMargin", nTitleRightMarginFrom, nTitleMargin, fProgress);
                textTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fTitleFontFrom + (fTitleFont - fTitleFontFrom) * fProgress);
                advanceAnimation(textArtist, "width", nArtistWidthFrom, nArtistWidth, fProgress);
                advanceAnimation(textArtist, "topMargin", nArtistTopMarginFrom, nArtistTopMargin, fProgress);
                advanceAnimation(textArtist, "leftMargin", nTitleLeftMarginFrom, nTitleMargin, fProgress);
                advanceAnimation(textArtist, "rightMargin", nTitleRightMarginFrom, nTitleMargin, fProgress);
                textArtist.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fArtistFontFrom + (fArtistFont - fArtistFontFrom) * fProgress);
                advanceAnimation(btnPlay, "topMargin", 0, nBtnPlayTopMargin, fProgress);
                advanceAnimation(btnForward, "topMargin", 0, nBtnPlayTopMargin, fProgress);
                advanceAnimation(btnPlay, "rightMargin", nBtnPlayRightMarginFrom, nBtnPlayRightMargin, fProgress);
                advanceAnimation(btnPlay, "height", nBtnHeightFrom, nBtnHeight, fProgress);
                advanceAnimation(btnForward, "height", nBtnHeightFrom, nBtnHeight, fProgress);
                advanceAnimation(btnRewind, "height", nBtnHeightFrom, nBtnHeight, fProgress);
                advanceAnimation(btnForward, "rightMargin", nBtnForwardRightMarginFrom, nBtnForwardRightMargin, fProgress);
                btnMore.requestLayout();
                btnShuffle.requestLayout();
                btnRepeat.requestLayout();
            }
        });
        anim.setDuration(lDuration).start();

        imgViewDown.setVisibility(View.VISIBLE);
        seekCurPos.setVisibility(View.VISIBLE);
        textCurPos.setVisibility(View.VISIBLE);
        textRemain.setVisibility(View.VISIBLE);
        btnRewind.setVisibility(View.VISIBLE);
        btnMore.setVisibility(View.VISIBLE);
        btnShuffle.setVisibility(View.VISIBLE);
        btnRepeat.setVisibility(View.VISIBLE);

        if (BASS.BASS_ChannelIsActive(MainActivity.hStream) != BASS.BASS_ACTIVE_PLAYING)
            btnPlay.setImageResource(R.drawable.playing_large_play);
        else btnPlay.setImageResource(R.drawable.playing_large_pause);
        btnForward.setImageResource(R.drawable.playing_large_forward);

        imgViewDown.animate().alpha(1.0f).setDuration(lDuration);
        seekCurPos.animate().alpha(1.0f).setDuration(lDuration);
        textCurPos.animate().alpha(1.0f).setDuration(lDuration);
        textRemain.animate().alpha(1.0f).setDuration(lDuration);
        btnRewind.animate().alpha(1.0f).setDuration(lDuration);
        btnMore.animate().alpha(1.0f).setDuration(lDuration);
        btnShuffle.animate().alpha(1.0f).setDuration(lDuration);
        btnRepeat.animate().alpha(1.0f).setDuration(lDuration);
        findViewById(R.id.btnCloseInPlayingBar).animate().alpha(0.0f).setDuration(lDuration);
        adView.animate().translationY(tabLayout.getHeight() + adView.getHeight()).setDuration(lDuration);
        tabLayout.animate().translationY(tabLayout.getHeight() + adView.getHeight()).setDuration(lDuration);
        findViewById(R.id.viewSep2).animate().translationY(tabLayout.getHeight()).setDuration(lDuration);
    }

    public void downViewPlaying(final boolean bBottom)
    {
        final MainActivity activity = this;
        final RelativeLayout relativePlaying = findViewById(R.id.relativePlaying);
        final ImageView imgViewDown = findViewById(R.id.imgViewDown);
        final SeekBar seekCurPos = findViewById(R.id.seekCurPos);
        final long lDuration = 400;
        relativePlaying.setBackgroundResource(R.drawable.topshadow);

        final TabLayout tabLayout = findViewById(R.id.tabs);
        final View viewSep2 = findViewById(R.id.viewSep2);
        final AdView adView = findViewById(R.id.adView);
        final ImageView imgViewArtwork = findViewById(R.id.imgViewArtworkInPlayingBar);
        final TextView textTitle = findViewById(R.id.textTitleInPlayingBar);
        final TextView textArtist = findViewById(R.id.textArtistInPlayingBar);
        final AnimationButton btnPlay = findViewById(R.id.btnPlayInPlayingBar);
        final AnimationButton btnForward = findViewById(R.id.btnForwardInPlayingBar);
        final TextView textCurPos = findViewById(R.id.textCurPos);
        final TextView textRemain = findViewById(R.id.textRemain);
        final AnimationButton btnRewind = findViewById(R.id.btnRewindInPlayingBar);
        final AnimationButton btnMore = findViewById(R.id.btnMoreInPlayingBar);
        final AnimationButton btnShuffle = findViewById(R.id.btnShuffleInPlayingBar);
        final AnimationButton btnRepeat = findViewById(R.id.btnRepeatInPlayingBar);

        final RelativeLayout.LayoutParams paramArtwork = (RelativeLayout.LayoutParams) imgViewArtwork.getLayoutParams();
        final RelativeLayout.LayoutParams paramTitle = (RelativeLayout.LayoutParams) textTitle.getLayoutParams();
        final RelativeLayout.LayoutParams paramArtist = (RelativeLayout.LayoutParams) textArtist.getLayoutParams();
        final RelativeLayout.LayoutParams paramBtnPlay = (RelativeLayout.LayoutParams) btnPlay.getLayoutParams();
        final RelativeLayout.LayoutParams paramBtnForward = (RelativeLayout.LayoutParams) btnForward.getLayoutParams();

        final int nTranslationYFrom = (int)relativePlaying.getTranslationY();
        final int nTranslationY = 0;
        final int nRelativePlayingHeightFrom = getResources().getDisplayMetrics().heightPixels - tabLayout.getMeasuredHeight() - findViewById(R.id.linearControl).getMeasuredHeight() - getStatusBarHeight() + (int) (16.0 * getResources().getDisplayMetrics().density + 0.5);
        int nTempRelativePlayingHeight = (int) (82.0 * getResources().getDisplayMetrics().density + 0.5);
        if(bBottom) nTempRelativePlayingHeight = 0;
        final int nRelativePlayingHeight = nTempRelativePlayingHeight;
        final int nArtworkWidthFrom = imgViewArtwork.getWidth();
        final int nArtworkWidth = (int) (44.0 * getResources().getDisplayMetrics().density + 0.5);
        final int nArtworkLeftMarginFrom = paramArtwork.leftMargin;
        final int nArtworkLeftMargin = (int) (8.0 * getResources().getDisplayMetrics().density + 0.5);
        final int nArtworkTopMarginFrom = paramArtwork.topMargin;
        final int nArtworkTopMargin = (int) (8.0 * getResources().getDisplayMetrics().density + 0.5);
        final int nTitleTopMarginFrom = paramTitle.topMargin;
        final int nTitleLeftMarginFrom = paramTitle.leftMargin;
        final int nTitleRightMarginFrom = paramTitle.rightMargin;
        final float fTitleFontFrom = 15.0f;
        final float fTitleFont = 13.0f;
        Paint paint = new Paint();
        textTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fTitleFont);
        paint.setTextSize(textTitle.getTextSize());
        textTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fTitleFontFrom);
        final int nTitleWidthFrom = paramTitle.width;
        final int nTitleWidth = (int) paint.measureText(textTitle.getText().toString());
        final int nArtistTopMarginFrom = paramArtist.topMargin;
        final float fArtistFontFrom = 13.0f;
        final float fArtistFont = 10.0f;
        textArtist.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fArtistFont);
        paint.setTextSize(textArtist.getTextSize());
        textArtist.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fArtistFontFrom);
        final int nArtistWidthFrom = paramArtist.width;
        final int nArtistWidth = (int) paint.measureText(textArtist.getText().toString());
        final int nBtnPlayTopMarginFrom = paramBtnPlay.topMargin;
        final int nBtnPlayRightMarginFrom = paramBtnPlay.rightMargin;
        final int nBtnPlayRightMargin = (int) (88.0 * getResources().getDisplayMetrics().density + 0.5);
        final int nBtnHeightFrom = (int) (44.0 * getResources().getDisplayMetrics().density + 0.5);
        final int nBtnHeight = (int) (60.0 * getResources().getDisplayMetrics().density + 0.5);
        final int nBtnForwardRightMarginFrom = paramBtnForward.rightMargin;
        final int nBtnForwardRightMargin = (int) (44.0 * getResources().getDisplayMetrics().density + 0.5);

        ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fProgress = valueAnimator.getAnimatedFraction();
                advanceAnimation(tabLayout, "bottomMargin", -tabLayout.getHeight(), 0, fProgress);
                advanceAnimation(viewSep2, "bottomMargin", tabLayout.getHeight(), 0, fProgress);
                relativePlaying.setTranslationY(nTranslationYFrom + (nTranslationY - nTranslationYFrom) * fProgress);
                advanceAnimation(relativePlaying, "height", nRelativePlayingHeightFrom, nRelativePlayingHeight, fProgress);
                advanceAnimation(imgViewArtwork, "width", nArtworkWidthFrom, nArtworkWidth, fProgress);
                advanceAnimation(imgViewArtwork, "height", nArtworkWidthFrom, nArtworkWidth, fProgress);
                advanceAnimation(imgViewArtwork, "leftMargin", nArtworkLeftMarginFrom, nArtworkLeftMargin, fProgress);
                advanceAnimation(imgViewArtwork, "topMargin", nArtworkTopMarginFrom, nArtworkTopMargin, fProgress);
                advanceAnimation(textTitle, "width", nTitleWidthFrom, nTitleWidth, fProgress);
                advanceAnimation(textTitle, "topMargin", nTitleTopMarginFrom, (int) (14.0 * getResources().getDisplayMetrics().density + 0.5), fProgress);
                advanceAnimation(textTitle, "leftMargin", nTitleLeftMarginFrom, (int) (60.0 * getResources().getDisplayMetrics().density + 0.5), fProgress);
                advanceAnimation(textTitle, "rightMargin", nTitleRightMarginFrom, (int) (132.0 * getResources().getDisplayMetrics().density + 0.5), fProgress);
                textTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fTitleFontFrom + (fTitleFont - fTitleFontFrom) * fProgress);
                advanceAnimation(textArtist, "width", nArtistWidthFrom, nArtistWidth, fProgress);
                advanceAnimation(textArtist, "topMargin", nArtistTopMarginFrom, (int) (33.0 * getResources().getDisplayMetrics().density + 0.5), fProgress);
                advanceAnimation(textArtist, "leftMargin", nTitleLeftMarginFrom, (int) (60.0 * getResources().getDisplayMetrics().density + 0.5), fProgress);
                advanceAnimation(textArtist, "rightMargin", nTitleRightMarginFrom, (int) (132.0 * getResources().getDisplayMetrics().density + 0.5), fProgress);
                textArtist.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fArtistFontFrom + (fArtistFont - fArtistFontFrom) * fProgress);
                advanceAnimation(btnPlay, "topMargin", nBtnPlayTopMarginFrom, 0, fProgress);
                advanceAnimation(btnForward, "topMargin", nBtnPlayTopMarginFrom, 0, fProgress);
                advanceAnimation(btnPlay, "rightMargin", nBtnPlayRightMarginFrom, nBtnPlayRightMargin, fProgress);
                advanceAnimation(btnPlay, "height", nBtnHeightFrom, nBtnHeight, fProgress);
                advanceAnimation(btnForward, "height", nBtnHeightFrom, nBtnHeight, fProgress);
                advanceAnimation(btnRewind, "height", nBtnHeightFrom, nBtnHeight, fProgress);
                advanceAnimation(btnForward, "rightMargin", nBtnForwardRightMarginFrom, nBtnForwardRightMargin, fProgress);
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                textTitle.setGravity(Gravity.START);
                paramTitle.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
                textArtist.setGravity(Gravity.START);
                paramArtist.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
                imgViewDown.clearAnimation();
                imgViewDown.setVisibility(View.GONE);
                seekCurPos.clearAnimation();
                seekCurPos.setVisibility(View.GONE);
                textCurPos.clearAnimation();
                textCurPos.setVisibility(View.GONE);
                textRemain.clearAnimation();
                textRemain.setVisibility(View.GONE);
                btnRewind.clearAnimation();
                btnRewind.setVisibility(View.GONE);
                btnMore.clearAnimation();
                btnMore.setVisibility(View.GONE);
                btnShuffle.clearAnimation();
                btnShuffle.setVisibility(View.GONE);
                btnRepeat.clearAnimation();
                btnRepeat.setVisibility(View.GONE);
                relativePlaying.setOnClickListener(activity);

                final PlaylistFragment playlistFragment = (PlaylistFragment)mSectionsPagerAdapter.getItem(0);
                RelativeLayout.LayoutParams paramContainer = (RelativeLayout.LayoutParams) findViewById(R.id.container).getLayoutParams();
                RelativeLayout.LayoutParams paramRecording = (RelativeLayout.LayoutParams) findViewById(R.id.relativeRecording).getLayoutParams();
                if (playlistFragment.hRecord != 0) {
                    paramContainer.addRule(RelativeLayout.ABOVE, R.id.relativeRecording);
                    paramContainer.bottomMargin = 0;
                    paramRecording.addRule(RelativeLayout.ABOVE, R.id.relativePlaying);
                    if(bBottom) paramRecording.bottomMargin = 0;
                    else paramRecording.bottomMargin = (int) (-22 * getResources().getDisplayMetrics().density + 0.5);
                } else {
                    paramContainer.addRule(RelativeLayout.ABOVE, R.id.relativePlaying);
                    if(bBottom) paramContainer.bottomMargin = 0;
                    else paramContainer.bottomMargin = (int) (-22 * getResources().getDisplayMetrics().density + 0.5);
                }

                if(bBottom) {
                    relativePlaying.setVisibility(View.GONE);
                    RelativeLayout.LayoutParams paramPlaying = (RelativeLayout.LayoutParams) findViewById(R.id.relativePlaying).getLayoutParams();
                    paramPlaying.height = (int) (82.0 * getResources().getDisplayMetrics().density + 0.5);
                }
            }
        });
        anim.setDuration(lDuration).start();

        if (BASS.BASS_ChannelIsActive(MainActivity.hStream) != BASS.BASS_ACTIVE_PLAYING)
            btnPlay.setImageResource(R.drawable.bar_button_play);
        else btnPlay.setImageResource(R.drawable.bar_button_pause);
        btnForward.setImageResource(R.drawable.bar_button_forward);

        imgViewDown.animate().alpha(0.0f).setDuration(lDuration);
        seekCurPos.animate().alpha(0.0f).setDuration(lDuration);
        textCurPos.animate().alpha(0.0f).setDuration(lDuration);
        textRemain.animate().alpha(0.0f).setDuration(lDuration);
        btnRewind.animate().alpha(0.0f).setDuration(lDuration);
        btnMore.animate().alpha(0.0f).setDuration(lDuration);
        btnShuffle.animate().alpha(0.0f).setDuration(lDuration);
        btnRepeat.animate().alpha(0.0f).setDuration(lDuration);
        findViewById(R.id.btnCloseInPlayingBar).animate().alpha(1.0f).setDuration(lDuration);
        adView.animate().translationY(0).setDuration(lDuration);
        tabLayout.animate().translationY(0).setDuration(lDuration);
        findViewById(R.id.viewSep2).animate().translationY(0).setDuration(lDuration);
    }

    public void showSaveExportMenu()
    {
        final PlaylistFragment playlistFragment = (PlaylistFragment)mSectionsPagerAdapter.getItem(0);
        final BottomMenu menu = new BottomMenu(this);
        menu.setTitle("保存／エクスポート");
        menu.addMenu("アプリ内に保存", R.drawable.actionsheet_save, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                playlistFragment.saveSongToLocal();
            }
        });
        if(Build.VERSION.SDK_INT >= 18) {
            menu.addMenu("ギャラリーに保存", R.drawable.actionsheet_film, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menu.dismiss();
                    playlistFragment.saveSongToGallery();
                }
            });
        }
        menu.addMenu("他のアプリにエクスポート", R.drawable.actionsheet_share, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                playlistFragment.export();
            }
        });
        menu.setCancelMenu();
        menu.show();
    }

    public void openItem()
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        transaction.replace(R.id.relativeMain, new ItemFragment());
        transaction.commit();
    }

    public void openSetting()
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        transaction.replace(R.id.relativeMain, new SettingFragment());
        transaction.commit();
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
            final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            playlistFragment.startActivityForResult(intent, 1);
        }
    }

    public void openGallery()
    {
        PlaylistFragment playlistFragment = (PlaylistFragment)mSectionsPagerAdapter.getItem(0);
        if (Build.VERSION.SDK_INT < 19)
        {
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            playlistFragment.startActivityForResult(intent, 2);
        }
        else
        {
            final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("video/*");
            playlistFragment.startActivityForResult(intent, 2);
        }
    }

    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK) return;

        if(requestCode == 1001)
        {
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

            try {
                JSONObject jo = new JSONObject(purchaseData);
                jo.getString("productId");

                hideAds();
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if(requestCode == 1002)
        {
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

            try {
                JSONObject jo = new JSONObject(purchaseData);
                jo.getString("productId");

                FragmentManager fragmentManager = getSupportFragmentManager();
                ItemFragment itemFragment = (ItemFragment)fragmentManager.findFragmentById(R.id.relativeMain);
                itemFragment.buyPurpleSeaUrchinPointer();
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if(requestCode == 1003)
        {
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

            try {
                JSONObject jo = new JSONObject(purchaseData);
                jo.getString("productId");

                FragmentManager fragmentManager = getSupportFragmentManager();
                ItemFragment itemFragment = (ItemFragment)fragmentManager.findFragmentById(R.id.relativeMain);
                itemFragment.buyElegantSeaUrchinPointer();
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void hideAds()
    {
        if(mAdView.getVisibility() != AdView.GONE) {
            mAdView.setVisibility(AdView.GONE);

            SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
            preferences.edit().putBoolean("hideads", mAdView.getVisibility() == AdView.GONE).apply();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initialize()
    {
        MobileAds.initialize(this, "ca-app-pub-9499594730627438~9516019647");

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        BASS.BASS_Init(-1, 44100, 0);
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_FLOATDSP, 1);
        BASS.BASS_SetConfig(BASS_CONFIG_AAC_MP4, 1);

        BASS.BASS_PluginLoad("libbass_aac.so", 0);
        BASS.BASS_PluginLoad("libbassflac.so", 0);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = findViewById(R.id.container);
        mViewPager.setSwipeHold(true);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                TabLayout tabLayout = findViewById(R.id.tabs);
                if(position == 0 && findViewById(R.id.relativeSongs).getVisibility() == View.VISIBLE) findViewById(R.id.viewSep1).setVisibility(View.INVISIBLE);
                else findViewById(R.id.viewSep1).setVisibility(View.VISIBLE);
                for(int i = 0; i < 5; i++) {
                    TabLayout.Tab tab = tabLayout.getTabAt(i);
                    if(tab == null) continue;
                    TextView textView = (TextView)tab.getCustomView();
                    if(textView == null) continue;
                    if(i == position) {
                        int color = Color.parseColor("#FF007AFF");
                        textView.setTextColor(color);
                        for (Drawable drawable : textView.getCompoundDrawables()) {
                            if (drawable != null)
                                drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
                        }
                    }
                    else {
                        int color = Color.parseColor("#FF808080");
                        textView.setTextColor(color);
                        for (Drawable drawable : textView.getCompoundDrawables()) {
                            if (drawable != null)
                                drawable.setColorFilter(null);
                        }
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        ViewGroup vg = findViewById(R.id.layout_root);
        TextView textView0 = (TextView) LayoutInflater.from(this).inflate(R.layout.tab, vg);
        textView0.setText("再生リスト");
        textView0.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_playlist, 0, 0);
        int color = Color.parseColor("#FF007AFF");
        textView0.setTextColor(color);
        for (Drawable drawable : textView0.getCompoundDrawables()) {
            if (drawable != null)
                drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        }
        TabLayout.Tab tab0 = tabLayout.getTabAt(0);
        if(tab0 != null) tab0.setCustomView(textView0);

        TextView textView1 = (TextView) LayoutInflater.from(this).inflate(R.layout.tab, vg);
        textView1.setText("ループ");
        textView1.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_abloop, 0, 0);
        color = Color.parseColor("#FF808080");
        textView1.setTextColor(color);
        TabLayout.Tab tab1 = tabLayout.getTabAt(1);
        if(tab1 != null) tab1.setCustomView(textView1);

        TextView textView2 = (TextView) LayoutInflater.from(this).inflate(R.layout.tab, vg);
        textView2.setText("コントロール");
        textView2.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_control, 0, 0);
        textView2.setTextColor(color);
        TabLayout.Tab tab2 = tabLayout.getTabAt(2);
        if(tab2 != null) tab2.setCustomView(textView2);

        TextView textView3 = (TextView) LayoutInflater.from(this).inflate(R.layout.tab, vg);
        textView3.setText("イコライザ");
        textView3.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_equalizer, 0, 0);
        textView3.setTextColor(color);
        TabLayout.Tab tab3 = tabLayout.getTabAt(3);
        if(tab3 != null) tab3.setCustomView(textView3);

        TextView textView4 = (TextView) LayoutInflater.from(this).inflate(R.layout.tab, vg);
        textView4.setText("エフェクト");
        textView4.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_effect, 0, 0);
        textView4.setTextColor(color);
        TabLayout.Tab tab4 = tabLayout.getTabAt(4);
        if(tab4 != null) tab4.setCustomView(textView4);

        AnimationButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(this);

        AnimationButton btnRewind = findViewById(R.id.btnRewind);
        btnRewind.setOnLongClickListener(this);
        btnRewind.setOnTouchListener(this);

        AnimationButton btnForward = findViewById(R.id.btnForward);
        btnForward.setOnLongClickListener(this);
        btnForward.setOnTouchListener(this);

        ScrollView scrollMenu = findViewById(R.id.scrollMenu);
        scrollMenu.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    findViewById(R.id.relativeSave).setBackgroundColor(Color.argb(255, 255, 255, 255));
                    findViewById(R.id.relativeLock).setBackgroundColor(Color.argb(255, 255, 255, 255));
                    findViewById(R.id.relativeAddSong).setBackgroundColor(Color.argb(255, 255, 255, 255));
                    findViewById(R.id.relativeHideAds).setBackgroundColor(Color.argb(255, 255, 255, 255));
                    findViewById(R.id.relativeItem).setBackgroundColor(Color.argb(255, 255, 255, 255));
                    findViewById(R.id.relativeReport).setBackgroundColor(Color.argb(255, 255, 255, 255));
                    findViewById(R.id.relativeReview).setBackgroundColor(Color.argb(255, 255, 255, 255));
                    findViewById(R.id.relativeInfo).setBackgroundColor(Color.argb(255, 255, 255, 255));
                }
                return false;
            }
        });
    }

    public boolean isAdsVisible() {
        return (mAdView.getVisibility() != AdView.GONE);
    }

    public void setSync()
    {
        if(hSync != 0)
        {
            BASS.BASS_ChannelRemoveSync(hStream, hSync);
            hSync = 0;
        }

        LinearLayout ABButton = findViewById(R.id.ABButton);
        LinearLayout MarkerButton = findViewById(R.id.MarkerButton);
        AnimationButton btnLoopmarker = findViewById(R.id.btnLoopmarker);

        EffectFragment effectFragment = (EffectFragment)mSectionsPagerAdapter.getItem(4);
        if(effectFragment.isReverse()) {
            if(ABButton.getVisibility() == View.VISIBLE && bLoopA) // ABループ中でA位置が設定されている
                hSync = BASS.BASS_ChannelSetSync(hStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hStream, dLoopA), EndSync, this);
            else if(MarkerButton.getVisibility() == View.VISIBLE && btnLoopmarker.isSelected()) // マーカー再生中
            {
                LoopFragment loopFragment = (LoopFragment)mSectionsPagerAdapter.getItem(1);
                hSync = BASS.BASS_ChannelSetSync(hStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hStream, loopFragment.getMarkerDstPos()), EndSync, this);
            }
            else
                hSync = BASS.BASS_ChannelSetSync(hStream, BASS.BASS_SYNC_END, 0, EndSync, this);
        }
        else {
            double dLength = BASS.BASS_ChannelBytes2Seconds(hStream, BASS.BASS_ChannelGetLength(hStream, BASS.BASS_POS_BYTE));
            if(ABButton.getVisibility() == View.VISIBLE && bLoopB) // ABループ中でB位置が設定されている
                hSync = BASS.BASS_ChannelSetSync(hStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hStream, dLoopB), EndSync, this);
            else if(MarkerButton.getVisibility() == View.VISIBLE && btnLoopmarker.isSelected()) // マーカー再生中
            {
                LoopFragment loopFragment = (LoopFragment)mSectionsPagerAdapter.getItem(1);
                hSync = BASS.BASS_ChannelSetSync(hStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hStream, loopFragment.getMarkerDstPos()), EndSync, this);
            }
            else
                hSync = BASS.BASS_ChannelSetSync(hStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(hStream, dLength - 0.75), EndSync, this);
        }
    }

    public void onEnded(final boolean bForce)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LoopFragment loopFragment = (LoopFragment) mSectionsPagerAdapter.getItem(1);
                EffectFragment effectFragment = (EffectFragment) mSectionsPagerAdapter.getItem(4);
                LinearLayout ABButton = findViewById(R.id.ABButton);
                LinearLayout MarkerButton = findViewById(R.id.MarkerButton);
                AnimationButton btnLoopmarker = findViewById(R.id.btnLoopmarker);

                if (ABButton.getVisibility() == View.VISIBLE && (bLoopA || bLoopB) && !bPlayNextByBPos) {
                    if (effectFragment.isReverse())
                        BASS.BASS_ChannelSetPosition(hStream, BASS.BASS_ChannelSeconds2Bytes(hStream, dLoopB), BASS.BASS_POS_BYTE);
                    else
                        BASS.BASS_ChannelSetPosition(hStream, BASS.BASS_ChannelSeconds2Bytes(hStream, dLoopA), BASS.BASS_POS_BYTE);
                    setSync();
                    if (BASS.BASS_ChannelIsActive(hStream) != BASS.BASS_ACTIVE_PLAYING)
                        BASS.BASS_ChannelPlay(hStream, false);
                } else if (MarkerButton.getVisibility() == View.VISIBLE && btnLoopmarker.isSelected()) {
                    BASS.BASS_ChannelSetPosition(hStream, BASS.BASS_ChannelSeconds2Bytes(hStream, loopFragment.getMarkerSrcPos()), BASS.BASS_POS_BYTE);
                    setSync();
                    if (BASS.BASS_ChannelIsActive(hStream) != BASS.BASS_ACTIVE_PLAYING)
                        BASS.BASS_ChannelPlay(hStream, false);
                } else {
                    bWaitEnd = true;
                    final Handler handler = new Handler();
                    Runnable timer = new Runnable() {
                        public void run() {
                            if (!bForce && BASS.BASS_ChannelIsActive(hStream) == BASS.BASS_ACTIVE_PLAYING) {
                                if(bWaitEnd) {
                                    handler.postDelayed(this, 100);
                                    return;
                                }
                            }
                            bWaitEnd = false;

                            AnimationButton btnShuffle = findViewById(R.id.btnShuffle);
                            boolean bSingle = false;
                            if (btnShuffle.getContentDescription().toString().equals("１曲のみ"))
                                bSingle = true;

                            AnimationButton btnRepeat = findViewById(R.id.btnRepeat);
                            boolean bRepeatSingle = false;
                            if (btnRepeat.getContentDescription().toString().equals("１曲リピート"))
                                bRepeatSingle = true;

                            PlaylistFragment playlistFragment = (PlaylistFragment) mSectionsPagerAdapter.getItem(0);
                            if (bSingle)
                                playlistFragment.playNext(false);
                            else if (bRepeatSingle)
                                BASS.BASS_ChannelPlay(hStream, true);
                            else
                                playlistFragment.playNext(true);
                        }
                    };
                    handler.postDelayed(timer, 0);
                }
            }
        });
    }

    private final BASS.SYNCPROC EndSync = new BASS.SYNCPROC()
    {
        public void SYNCPROC(int handle, int channel, int data, Object user)
        {
            MainActivity activity = (MainActivity)user;
            activity.onEnded(false);
        }
    };

    public void clearLoop()
    {
        clearLoop(true);
    }

    public void clearLoop(boolean bSave)
    {
        dLoopA = 0.0;
        bLoopA = false;
        dLoopB = 0.0;
        bLoopB = false;
        LoopFragment loopFragment = (LoopFragment)mSectionsPagerAdapter.getItem(1);
        loopFragment.clearLoop(bSave);
    }

    @Override
    public void onDestroy() {
        BASS.BASS_Free();

        foregroundService.stopForeground();
        unbindService(connection);
        unbindService(mServiceConn);
        mBound = false;
        super.onDestroy();
    }

    private String readChangeLog() {
        StringBuilder sb = new StringBuilder();
        String tmp;
        BufferedReader br = null;
        boolean bFirst = true;
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            TabLayout tabLayout = findViewById(R.id.tabs);
            if(tabLayout.getSelectedTabPosition() == 0) { // 再生リスト画面
                if(findViewById(R.id.relativeLyrics).getVisibility() == View.VISIBLE) {
                    findViewById(R.id.btnFinishLyrics).performClick();
                    return true;
                }
            }
            else if(tabLayout.getSelectedTabPosition() == 4) { // エフェクト画面
                if(findViewById(R.id.relativeEffectDetail).getVisibility() == View.VISIBLE) {
                    findViewById(R.id.btnFinish).performClick();
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
