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
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.*;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingFlowParams.*;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsParams.*;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentForm;
import com.google.android.material.tabs.TabLayout;

import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;
import androidx.core.view.accessibility.AccessibilityEventCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS_FX;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import static com.edolfzoku.hayaemon2.Constants.ITEM_BLUE_CAMPER_POINTER;
import static com.edolfzoku.hayaemon2.Constants.ITEM_ELEGANT_SEA_URCHIN_POINTER;
import static com.edolfzoku.hayaemon2.Constants.ITEM_HIDE_ADS;
import static com.edolfzoku.hayaemon2.Constants.ITEM_HIDE_ADS_MONTHLY;
import static com.edolfzoku.hayaemon2.Constants.ITEM_ORANGE_CAMPER_POINTER;
import static com.edolfzoku.hayaemon2.Constants.ITEM_PINK_CAMPER_POINTER;
import static com.edolfzoku.hayaemon2.Constants.ITEM_PURPLE_SEA_URCHIN_POINTER;
import static com.edolfzoku.hayaemon2.Constants.PRODUCT_ID_BLUE_CAMPER_POINTER;
import static com.edolfzoku.hayaemon2.Constants.PRODUCT_ID_ELEGANT_SEA_URCHIN_POINTER;
import static com.edolfzoku.hayaemon2.Constants.PRODUCT_ID_HIDE_ADS;
import static com.edolfzoku.hayaemon2.Constants.PRODUCT_ID_HIDE_ADS_MONTHLY;
import static com.edolfzoku.hayaemon2.Constants.PRODUCT_ID_ORANGE_CAMPER_POINTER;
import static com.edolfzoku.hayaemon2.Constants.PRODUCT_ID_PINK_CAMPER_POINTER;
import static com.edolfzoku.hayaemon2.Constants.PRODUCT_ID_PURPLE_SEA_URCHIN_POINTER;
import static com.edolfzoku.hayaemon2.PlaylistFragment.sEffects;
import static com.edolfzoku.hayaemon2.PlaylistFragment.sLyrics;
import static com.edolfzoku.hayaemon2.PlaylistFragment.sPlayingPlaylist;
import static com.edolfzoku.hayaemon2.PlaylistFragment.sPlaylists;
import static com.edolfzoku.hayaemon2.PlaylistFragment.sSelectedPlaylist;
import static com.un4seen.bass.BASS_AAC.BASS_CONFIG_AAC_MP4;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        View.OnLongClickListener, View.OnTouchListener, DrawerLayout.DrawerListener
{
    private static final String TAG = "MainActivity";
    static MainActivity sActivity;
    static ForegroundService sService;
    static int sStream, sRecord, sEncode, sFxVol, sSync, sShuffle, sRepeat;
    static boolean sLoopA, sLoopB, sPlayNextByBPos, sWaitEnd;
    static double sLoopAPos, sLoopBPos, sLength, sPrevVersion;
    static long sByteLength;
    static Handler sHandler;
    PlaylistFragment playlistFragment;
    LoopFragment loopFragment;
    ControlFragment controlFragment;
    EqualizerFragment equalizerFragment;
    EffectFragment effectFragment;
    private boolean mShowDeadline, mShowUpdateLog, mDarkMode, mShowReviewRequest;
    private float mDensity;
    private int mLastY, mPurchasingItem;

    private DrawerLayout mDrawerLayout;
    private HoldableViewPager mViewPager;
    private BillingClient mBillingClient;

    private ConsentInformation consentInformation;

    // Use an atomic boolean to initialize the Google Mobile Ads SDK and load ads once.
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);

    private FrameLayout mAdContainerView;
    private AdView mAdView;
    private LinearLayout mLinearControl;
    private SeekBar mSeekCurPos;
    private ImageView mImgViewDown, mImgViewArtworkInMenu, mImgViewRecording;
    private TabLayout mTabLayout;
    private View mViewSep0, mViewSep1, mViewSep2, mViewSep3, mDividerMenu;
    private TextView mTextCurPos, mTextRemain, mTextTitle, mTextArtist, mTextRecordingTime, mTextSave, mTextLock, mTextHideAds, mTextItemInMenu, mTextHelp, mTextInquiry, mTextReview, mTextInfo, mTextNotice, mTextAddSong, mTextPlaying, mTextTitleInMenu, mTextArtistInMenu, mTextRecording;
    private AnimationButton mBtnMenu, mBtnRewind, mBtnPlay, mBtnForward, mBtnShuffle, mBtnRepeat, mBtnRecord, mBtnPlayInPlayingBar, mBtnForwardInPlayingBar, mBtnRewindInPlayingBar, mBtnMoreInPlayingBar, mBtnShuffleInPlayingBar, mBtnRepeatInPlayingBar, mBtnCloseInPlayingBar, mBtnStopRecording, mBtnArtworkInPlayingBar, mBtnSetting, mBtnDarkMode;
    private RelativeLayout mRelativeRecording, mRelativeSave, mRelativeLock, mRelativeNotice, mRelativeAddSong, mRelativeItem, mRelativeHelp, mRelativeInquiry, mRelativeReview, mRelativeHideAds, mRelativeInfo, mRelativePlayingWithShadow, mRelativePlaying, mRelativeLeftMenu;
    private GestureDetector mGestureDetector;

    public HoldableViewPager getViewPager() { return mViewPager; }
    public SeekBar getSeekCurPos() { return mSeekCurPos; }
    public TextView getTextCurPos() { return mTextCurPos; }
    public TextView getTextRemain() { return mTextRemain; }
    public AnimationButton getBtnPlay() { return mBtnPlay; }
    public AnimationButton getBtnPlayInPlayingBar() { return mBtnPlayInPlayingBar; }
    public AnimationButton getBtnArtworkInPlayingBar() { return mBtnArtworkInPlayingBar; }
    public AnimationButton getBtnRecord() { return mBtnRecord; }
    public RelativeLayout getRelativePlayingWithShadow() { return mRelativePlayingWithShadow; }
    public RelativeLayout getRelativeRecording() { return mRelativeRecording; }
    public View getViewSep1() { return mViewSep1; }
    public TextView getTextRecordingTime() { return mTextRecordingTime; }
    public AnimationButton getBtnStopRecording() { return mBtnStopRecording; }
    public float getDensity() { return mDensity; }
    public BillingClient getBillingClient() { return mBillingClient; }
    public boolean isDarkMode() { return mDarkMode; }

    public MainActivity() {
        sActivity = this;
        sHandler = new Handler();
    }

    static class FileProcsParams {
        AssetFileDescriptor assetFileDescriptor = null;
        FileChannel fileChannel = null;
        InputStream inputStream = null;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDensity = getResources().getDisplayMetrics().density;

        setContentView(R.layout.activity_main);

        mAdContainerView = findViewById(R.id.ad_view_container);
        if(!getSharedPreferences("SaveData", MODE_PRIVATE).getBoolean("hideads", false)) {
            ConsentDebugSettings debugSettings = new ConsentDebugSettings
                    .Builder(this)
                    // .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                    .addTestDeviceHashedId("C274AC018E6509532711089B8BC4433D")
                    .build();

            // Create a ConsentRequestParameters object.
            ConsentRequestParameters params = new ConsentRequestParameters
                    .Builder()
                    .setConsentDebugSettings(debugSettings)
                    .build();

            consentInformation = UserMessagingPlatform.getConsentInformation(this);
            // consentInformation.reset();
            consentInformation.requestConsentInfoUpdate(
                    this,
                    params,
                    (ConsentInformation.OnConsentInfoUpdateSuccessListener) () -> {
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                                this,
                                (ConsentForm.OnConsentFormDismissedListener) loadAndShowError -> {
                                    if (loadAndShowError != null) {
                                        // Consent gathering failed.
                                        Log.w(TAG, String.format("%s: %s",
                                                loadAndShowError.getErrorCode(),
                                                loadAndShowError.getMessage()));
                                    }

                                    // Consent has been gathered.
                                    if (consentInformation.canRequestAds()) {
                                        initializeMobileAdsSdk();
                                    }
                                }
                        );
                    },
                    (ConsentInformation.OnConsentInfoUpdateFailureListener) requestConsentError -> {
                        // Consent gathering failed.
                        Log.w(TAG, String.format("%s: %s",
                                requestConsentError.getErrorCode(),
                                requestConsentError.getMessage()));
                    });

            // Check if you can initialize the Google Mobile Ads SDK in parallel
            // while checking for new consent information. Consent obtained in
            // the previous session can be used to request ads.
            if (consentInformation.canRequestAds()) {
                initializeMobileAdsSdk();
            }
        }

        mBtnShuffle = findViewById(R.id.btnShuffle);
        mBtnRepeat = findViewById(R.id.btnRepeat);
        mBtnRecord = findViewById(R.id.btnRecord);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mBtnPlay = findViewById(R.id.btnPlay);
        mBtnPlayInPlayingBar = findViewById(R.id.btnPlayInPlayingBar);
        mBtnForward = findViewById(R.id.btnForward);
        mBtnForwardInPlayingBar = findViewById(R.id.btnForwardInPlayingBar);
        mBtnMenu = findViewById(R.id.btnMenu);
        mBtnRewind = findViewById(R.id.btnRewind);
        mBtnRewindInPlayingBar = findViewById(R.id.btnRewindInPlayingBar);
        mBtnCloseInPlayingBar = findViewById(R.id.btnCloseInPlayingBar);
        mBtnShuffleInPlayingBar = findViewById(R.id.btnShuffleInPlayingBar);
        mBtnRepeatInPlayingBar = findViewById(R.id.btnRepeatInPlayingBar);
        mBtnMoreInPlayingBar = findViewById(R.id.btnMoreInPlayingBar);
        mRelativePlaying = findViewById(R.id.relativePlaying);
        mSeekCurPos = findViewById(R.id.seekCurPos);
        mImgViewDown = findViewById(R.id.imgViewDown);
        mImgViewArtworkInMenu = findViewById(R.id.imgViewArtworkInMenu);
        mImgViewRecording = findViewById(R.id.imgViewRecording);
        mBtnArtworkInPlayingBar = findViewById(R.id.btnArtworkInPlayingBar);
        mRelativePlayingWithShadow = findViewById(R.id.relativePlayingWithShadow);
        mTextCurPos = findViewById(R.id.textCurPos);
        mTextRemain = findViewById(R.id.textRemain);
        mTabLayout = findViewById(R.id.tabs);
        mViewSep0 = findViewById(R.id.viewSep0);
        mViewSep1 = findViewById(R.id.viewSep1);
        mViewSep2 = findViewById(R.id.viewSep2);
        mViewSep3 = findViewById(R.id.viewSep3);
        mDividerMenu = findViewById(R.id.dividerMenu);
        mTextTitle = findViewById(R.id.textTitleInPlayingBar);
        mTextTitleInMenu = findViewById(R.id.textTitleInMenu);
        mTextArtist = findViewById(R.id.textArtistInPlayingBar);
        mTextArtistInMenu = findViewById(R.id.textArtistInMenu);
        mRelativeRecording = findViewById(R.id.relativeRecording);
        mLinearControl = findViewById(R.id.linearControl);
        mRelativeSave = findViewById(R.id.relativeSave);
        mRelativeLock = findViewById(R.id.relativeLock);
        mRelativeNotice = findViewById(R.id.relativeNotice);
        mRelativeAddSong = findViewById(R.id.relativeAddSong);
        mRelativeItem = findViewById(R.id.relativeItem);
        mRelativeHelp = findViewById(R.id.relativeHelp);
        mRelativeInquiry = findViewById(R.id.relativeInquiry);
        mRelativeReview = findViewById(R.id.relativeReview);
        mRelativeHideAds = findViewById(R.id.relativeHideAds);
        mRelativeInfo = findViewById(R.id.relativeInfo);
        mTextRecordingTime = findViewById(R.id.textRecordingTime);
        mBtnStopRecording = findViewById(R.id.btnStopRecording);
        mRelativeLeftMenu = findViewById(R.id.relativeLeftMenu);
        mTextSave = findViewById(R.id.textSave);
        mTextLock = findViewById(R.id.textLock);
        mTextHideAds = findViewById(R.id.textHideAds);
        mTextItemInMenu = findViewById(R.id.textItemInMenu);
        mTextHelp = findViewById(R.id.textHelp);
        mTextInquiry = findViewById(R.id.textInquiry);
        mTextReview = findViewById(R.id.textReview);
        mTextInfo = findViewById(R.id.textInfo);
        mTextNotice = findViewById(R.id.textNotice);
        mTextAddSong = findViewById(R.id.textAddSong);
        mTextPlaying = findViewById(R.id.textPlaying);
        mBtnSetting = findViewById(R.id.btnSetting);
        mBtnDarkMode = findViewById(R.id.btnDarkMode);
        mTextRecording = findViewById(R.id.textRecording);

        initialize(savedInstanceState);
        loadData();

        Intent intent = getIntent();
        if(intent != null && intent.getType() != null) {
            if(intent.getType().contains("audio/")) {
                if(Build.VERSION.SDK_INT < 16)
                {
                    Uri uri = copyFile(intent.getData());
                    if (uri != null)
                        playlistFragment.addSong(this, uri);
                }
                else
                {
                    if(intent.getClipData() == null)
                    {
                        if (intent.getData() != null) {
                            Uri uri = copyFile(intent.getData());
                            if (uri != null)
                                playlistFragment.addSong(this, uri);
                        }
                    }
                    else
                    {
                        for(int i = 0; i < intent.getClipData().getItemCount(); i++)
                        {
                            Uri uri = copyFile(intent.getClipData().getItemAt(i).getUri());
                            if (uri != null)
                                playlistFragment.addSong(this, uri);
                        }
                    }
                }
                if(playlistFragment.getSongsAdapter() != null)
                    playlistFragment.getSongsAdapter().notifyDataSetChanged();
            }
        }
        SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        preferences.edit().putString("arPlaylists", gson.toJson(sPlaylists)).apply();
        preferences.edit().putString("arEffects", gson.toJson(sEffects)).apply();
        preferences.edit().putString("arLyrics", gson.toJson(sLyrics)).apply();
        preferences.edit().putString("arPlaylistNames", gson.toJson(PlaylistFragment.sPlaylistNames)).apply();

        int bootCount = getSharedPreferences("SaveData", MODE_PRIVATE).getInt("bootcount", 0);
        bootCount++;
        if (bootCount >= 5 && bootCount % 5 == 0) mShowReviewRequest = true;
        getSharedPreferences("SaveData", MODE_PRIVATE).edit().putInt("bootcount", bootCount).apply();

        setupBillingClient();

        mBtnShuffle.setOnClickListener(this);
        mBtnRepeat.setOnClickListener(this);
        mDrawerLayout.addDrawerListener(this);
        mRelativeSave.setOnTouchListener(this);
        mRelativeSave.setOnClickListener(this);
        mRelativeLock.setOnTouchListener(this);
        mRelativeLock.setOnClickListener(this);
        mRelativeNotice.setOnTouchListener(this);
        mRelativeNotice.setOnClickListener(this);
        mRelativeAddSong.setOnTouchListener(this);
        mRelativeAddSong.setOnClickListener(this);
        mRelativeItem.setOnTouchListener(this);
        mRelativeItem.setOnClickListener(this);
        mRelativeHelp.setOnTouchListener(this);
        mRelativeHelp.setOnClickListener(this);
        mRelativeInquiry.setOnTouchListener(this);
        mRelativeInquiry.setOnClickListener(this);
        mRelativeReview.setOnTouchListener(this);
        mRelativeReview.setOnClickListener(this);
        mRelativeHideAds.setOnTouchListener(this);
        mRelativeHideAds.setOnClickListener(this);
        mRelativeInfo.setOnTouchListener(this);
        mRelativeInfo.setOnClickListener(this);
        mBtnSetting.setOnClickListener(this);
        mBtnDarkMode.setOnClickListener(this);
        mBtnPlayInPlayingBar.setOnClickListener(this);
        mBtnForwardInPlayingBar.setOnClickListener(this);
        mBtnForwardInPlayingBar.setOnLongClickListener(this);
        mBtnForwardInPlayingBar.setOnTouchListener(this);
        mBtnRewindInPlayingBar.setOnClickListener(this);
        mBtnRewindInPlayingBar.setOnLongClickListener(this);
        mBtnRewindInPlayingBar.setOnTouchListener(this);
        mBtnCloseInPlayingBar.setOnClickListener(this);
        mBtnShuffleInPlayingBar.setOnClickListener(this);
        mBtnRepeatInPlayingBar.setOnClickListener(this);
        mBtnMoreInPlayingBar.setOnClickListener(this);
        mSeekCurPos.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) { }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                loopFragment.setCurPos((double)seekBar.getProgress());
            }
        });
        mImgViewDown.setOnClickListener(this);
        mImgViewDown.setOnTouchListener(this);
        mRelativePlaying.setOnClickListener(this);
        mGestureDetector = new GestureDetector(this, new SingleTapConfirm());
        mRelativePlaying.setOnTouchListener(this);

        mBtnArtworkInPlayingBar.setAnimation(false);
        mBtnArtworkInPlayingBar.setClickable(false);

        mDrawerLayout.setScrimColor(Color.argb(102, 0, 0, 0));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            MainActivity.setSystemBarTheme(this, false);

        updateDrawer();
    }

    private void initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return;
        }

        // Initialize the Google Mobile Ads SDK.
        List<String> testDeviceIds = Arrays.asList("C274AC018E6509532711089B8BC4433D", "7A4DCE2E7A051D5905BDA9790B453502");
        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);
        MobileAds.initialize(this);

        if(!getSharedPreferences("SaveData", MODE_PRIVATE).getBoolean("hideads", false)) {
            mAdContainerView.getLayoutParams().height = (int) (getAdSize().getHeight() * mDensity);
            mAdContainerView.post(new Runnable() {
                @Override
                public void run() {
                    loadBanner();
                }
            });
        }
    }

    private void setupBillingClient() {
        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
            }
        };
        mBillingClient = BillingClient.newBuilder(this)
                .setListener((billingResult, purchases) -> {
                    // Handle updated purchases here
                    if (billingResult.getResponseCode() == BillingResponseCode.OK && purchases != null) {
                        for (Purchase purchase : purchases) {
                            // Handle the purchased item
                            List<String> productIds = purchase.getProducts();
                            for (String productId: productIds) {
                                runOnUiThread(() -> handlePurchase(productId));
                            }

                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                if (!purchase.isAcknowledged()) {
                                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                                            AcknowledgePurchaseParams.newBuilder()
                                                    .setPurchaseToken(purchase.getPurchaseToken())
                                                    .build();
                                    mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
                                }
                            }
                        }
                    }
                })
                .enablePendingPurchases()
                .build();

        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                int responseCode = billingResult.getResponseCode();
                String debugMessage = billingResult.getDebugMessage();

                if (responseCode == BillingResponseCode.OK) {
                    // Ready to make purchases
                    checkPurchased();
                } else {
                    Log.e(TAG, "onBillingSetupFinished: " + debugMessage);
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Handle disconnection
                Log.w(TAG, "onBillingServiceDisconnected");
            }
        });
    }

    private void launchPurchaseFlow(String productId) {

        List<Product> productList = new ArrayList<>();
        switch (productId) {
            case PRODUCT_ID_HIDE_ADS:
            case PRODUCT_ID_PURPLE_SEA_URCHIN_POINTER:
            case PRODUCT_ID_ELEGANT_SEA_URCHIN_POINTER:
            case PRODUCT_ID_PINK_CAMPER_POINTER:
            case PRODUCT_ID_BLUE_CAMPER_POINTER:
            case PRODUCT_ID_ORANGE_CAMPER_POINTER:
                productList.add(
                        Product.newBuilder()
                                .setProductId(productId)
                                .setProductType(BillingClient.ProductType.INAPP)
                                .build()
                );
                break;
            case PRODUCT_ID_HIDE_ADS_MONTHLY:
            default:
                productList.add(
                        Product.newBuilder()
                                .setProductId(productId)
                                .setProductType(BillingClient.ProductType.SUBS)
                                .build()
                );
                break;
        }

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        final MainActivity activity = this;
        mBillingClient.queryProductDetailsAsync(params, new ProductDetailsResponseListener() {
            @Override
            public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {
                // check billingResult
                // process returned productDetailsList
                if (list.size() == 0) return;

                ProductDetails productDetails = list.get(0);

                ArrayList<ProductDetailsParams> productList = new ArrayList<>();
                switch (productId) {
                    case PRODUCT_ID_HIDE_ADS:
                    case PRODUCT_ID_PURPLE_SEA_URCHIN_POINTER:
                    case PRODUCT_ID_ELEGANT_SEA_URCHIN_POINTER:
                    case PRODUCT_ID_PINK_CAMPER_POINTER:
                    case PRODUCT_ID_BLUE_CAMPER_POINTER:
                    case PRODUCT_ID_ORANGE_CAMPER_POINTER:
                        productList.add(
                                ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .build());
                        break;
                    case PRODUCT_ID_HIDE_ADS_MONTHLY:
                    default:
                        List<ProductDetails.SubscriptionOfferDetails> offerDetails = productDetails.getSubscriptionOfferDetails();
                        if (offerDetails == null || offerDetails.size() == 0) return;
                        productList.add(
                                ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .setOfferToken(offerDetails.get(0).getOfferToken())
                                        .build());
                        break;
                }

                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productList)
                        .build();

                BillingResult result = mBillingClient.launchBillingFlow(activity, billingFlowParams);

                int responseCode = result.getResponseCode();
                String debugMessage = result.getDebugMessage();

                if (responseCode != BillingResponseCode.OK) {
                    Log.e(TAG, "launchPurchaseFlow: " + debugMessage);
                }
            }
        });
    }

    private void handlePurchase(String productId) {
        switch (productId) {
            case PRODUCT_ID_HIDE_ADS:
                hideAds(true, false);
                break;
            case PRODUCT_ID_HIDE_ADS_MONTHLY:
                hideAds(false, true);
                break;
            case PRODUCT_ID_PURPLE_SEA_URCHIN_POINTER:
                buyPurpleSeaUrchinPointer(mPurchasingItem == ITEM_PURPLE_SEA_URCHIN_POINTER);
                break;
            case PRODUCT_ID_ELEGANT_SEA_URCHIN_POINTER:
                buyElegantSeaUrchinPointer(mPurchasingItem == ITEM_ELEGANT_SEA_URCHIN_POINTER);
                break;
            case PRODUCT_ID_PINK_CAMPER_POINTER:
                buyPinkCamperPointer(mPurchasingItem == ITEM_PINK_CAMPER_POINTER);
                break;
            case PRODUCT_ID_BLUE_CAMPER_POINTER:
                buyBlueCamperPointer(mPurchasingItem == ITEM_BLUE_CAMPER_POINTER);
                break;
            case PRODUCT_ID_ORANGE_CAMPER_POINTER:
                buyOrangeCamperPointer(mPurchasingItem == ITEM_ORANGE_CAMPER_POINTER);
                break;
        }
    }

    private void loadBanner() {
        // Create an ad request.
        mAdView = new AdView(this);
        mAdView.setAdUnitId("ca-app-pub-9499594730627438/5954202671"); // 本番用
        // mAdView.setAdUnitId("ca-app-pub-3940256099942544/6300978111"); // テスト用
        mAdContainerView.removeAllViews();
        mAdContainerView.addView(mAdView);

        AdSize adSize = getAdSize();
        mAdView.setAdSize(adSize);

        AdRequest adRequest = new AdRequest.Builder().build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);
    }

    public AdSize getAdSize() {
        // Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = mAdContainerView.getWidth();

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void setSystemBarTheme(final Activity pActivity, @SuppressWarnings("UnusedParameters")final boolean pIsDark) {
        MainActivity activity = (MainActivity)pActivity;
        final int lFlags = pActivity.getWindow().getDecorView().getSystemUiVisibility();
        pActivity.getWindow().getDecorView().setSystemUiVisibility(activity.mDarkMode ? (lFlags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) : (lFlags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        if(playlistFragment != null && playlistFragment.isAdded())
            getSupportFragmentManager().putFragment(savedInstanceState, "playlistFragment", playlistFragment);
        if(loopFragment != null && loopFragment.isAdded())
            getSupportFragmentManager().putFragment(savedInstanceState, "loopFragment", loopFragment);
        if(controlFragment != null && controlFragment.isAdded())
            getSupportFragmentManager().putFragment(savedInstanceState, "controlFragment", controlFragment);
        if(equalizerFragment != null && equalizerFragment.isAdded())
            getSupportFragmentManager().putFragment(savedInstanceState, "equalizerFragment", equalizerFragment);
        if(effectFragment != null && effectFragment.isAdded())
            getSupportFragmentManager().putFragment(savedInstanceState, "effectFragment", effectFragment);
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }

    private void advanceAnimation(View view, String strTarget, int nFrom, int nTo, float fProgress) {
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
    }

    private int getStatusBarHeight(){
        final Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.top;
    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) { }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) { }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) { }

    @Override
    public void onDrawerStateChanged(int newState) {
        if(newState == DrawerLayout.STATE_IDLE) {
            int color = getResources().getColor(mDarkMode ? R.color.darkModeBk : R.color.lightModeBk);
            mRelativeSave.setBackgroundColor(color);
            mRelativeLock.setBackgroundColor(color);
            mRelativeNotice.setBackgroundColor(color);
            mRelativeAddSong.setBackgroundColor(color);
            mRelativeHideAds.setBackgroundColor(color);
            mRelativeItem.setBackgroundColor(color);
            mRelativeHelp.setBackgroundColor(color);
            mRelativeInquiry.setBackgroundColor(color);
            mRelativeReview.setBackgroundColor(color);
            mRelativeInfo.setBackgroundColor(color);
        }
        else if(newState == DrawerLayout.STATE_DRAGGING) updateDrawer();
    }

    public void updateDrawer() {
        SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        boolean bPinkCamperDisplayed = preferences.getBoolean("bPinkCamperDisplayed", false);
        boolean bBlueCamperDisplayed = preferences.getBoolean("bBlueCamperDisplayed", false);
        boolean bOrangeCamperDisplayed = preferences.getBoolean("bOrangeCamperDisplayed", false);
        int nCount = 0;
        if(!bPinkCamperDisplayed) nCount++;
        if(!bBlueCamperDisplayed) nCount++;
        if(!bOrangeCamperDisplayed) nCount++;

        findViewById(R.id.textPlaying).setVisibility(sStream == 0 ? View.GONE : View.VISIBLE);
        findViewById(R.id.relativePlayingInMenu).setVisibility(sStream == 0 ? View.GONE : View.VISIBLE);
        findViewById(R.id.relativeSave).setVisibility(sStream == 0 ? View.GONE : View.VISIBLE);
        findViewById(R.id.relativeLock).setVisibility(sStream == 0 ? View.GONE : View.VISIBLE);
        findViewById(R.id.dividerMenu).setVisibility(sStream == 0 ? View.GONE : View.VISIBLE);
        TextView textView = findViewById(R.id.textItemNew);
        textView.setVisibility(nCount == 0 ? View.GONE : View.VISIBLE);
        textView.setText(String.format(Locale.getDefault(), "%d", nCount));

        if(!needsToDisplayDeadline(true, true))  findViewById(R.id.relativeNotice).setVisibility(View.GONE);
        findViewById(R.id.relativeHideAds).setVisibility(isAdsVisible() ? View.VISIBLE : View.GONE);
        if(sStream != 0) {
            playlistFragment.selectPlaylist(sPlayingPlaylist);
            PlaylistFragment.sSelectedItem = PlaylistFragment.sPlaying;

            SongItem item = sPlaylists.get(sPlayingPlaylist).get(PlaylistFragment.sPlaying);
            Bitmap bitmap = null;
            if (item.getPathArtwork() != null && item.getPathArtwork().equals("potatoboy"))
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.potatoboy);
            else if(item.getPathArtwork() != null && !item.getPathArtwork().equals(""))
                bitmap = BitmapFactory.decodeFile(item.getPathArtwork());
            else {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                try {
                    mmr.setDataSource(getApplicationContext(), Uri.parse(item.getPath()));
                    byte[] data = mmr.getEmbeddedPicture();
                    if (data != null) bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    try {
                        mmr.release();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(bitmap != null) mImgViewArtworkInMenu.setImageBitmap(bitmap);
            else mImgViewArtworkInMenu.setImageResource(mDarkMode ? R.drawable.ic_playing_large_artwork_dark : R.drawable.ic_playing_large_artwork);
            TextView textTitleInMenu = findViewById(R.id.textTitleInMenu);
            textTitleInMenu.setText(item.getTitle());
            TextView textArtistInMenu = findViewById(R.id.textArtistInMenu);
            if(item.getArtist() == null || item.getArtist().equals("")) {
                textArtistInMenu.setTextColor(Color.argb(255, 147, 156, 160));
                textArtistInMenu.setText(R.string.unknownArtist);
            }
            else {
                textArtistInMenu.setTextColor(Color.argb(255, 102, 102, 102));
                textArtistInMenu.setText(item.getArtist());
            }

            ArrayList<EffectSaver> arEffectSavers = sEffects.get(sPlayingPlaylist);
            EffectSaver saver = arEffectSavers.get(PlaylistFragment.sPlaying);
            ImageView imgLock = findViewById(R.id.imgLockInMenu);
            TextView textLock = findViewById(R.id.textLock);
            if(saver.isSave()) {
                imgLock.setImageResource(R.drawable.ic_leftmenu_playing_unlock);
                textLock.setText(R.string.cancelRestoreEffect);
            }
            else {
                imgLock.setImageResource(R.drawable.ic_leftmenu_playing_lock);
                textLock.setText(R.string.restoreEffect);
            }
        }
    }

    public Uri copyFile(Uri uri) {
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
            InputStream in;
            if(uri.getScheme() != null && uri.getScheme().equals("content"))
                in = getContentResolver().openInputStream(uri);
            else in = new FileInputStream(uri.toString());
            FileOutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            if(in != null) {
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                in.close();
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return Uri.parse(strPath);
    }

    public Uri copyTempFile(Uri uri) {
        int i = 0;
        String strPath;
        File file;
        while(true) {
            strPath = getExternalCacheDir() + "/copied" + String.format(Locale.getDefault(), "%d", i);
            file = new File(strPath);
            if(!file.exists()) break;
            i++;
        }
        try {
            InputStream in;
            if(uri.getScheme() != null && uri.getScheme().equals("content"))
                in = getContentResolver().openInputStream(uri);
            else in = new FileInputStream(uri.toString());
            FileOutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            if(in != null) {
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                in.close();
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
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

        if (mAdView != null) mAdView.resume();

        if (mShowReviewRequest) {
            mShowReviewRequest = false;

            final MainActivity activity = this;
            final ReviewManager manager = ReviewManagerFactory.create(this);
            Task<ReviewInfo> request = manager.requestReviewFlow();
            request.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // We can get the ReviewInfo object
                    ReviewInfo reviewInfo = task.getResult();
                    Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);
                    flow.addOnCompleteListener(task1 -> {
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown. Thus, no
                        // matter the result, we continue our app flow.
                    });
                }

            });
        }

        checkPurchased();
    }

    public void checkPurchased() {
        checkInAppPurchasedWithCheckSubs(true);
    }

    public void checkInAppPurchased() {
        checkInAppPurchasedWithCheckSubs(false);
    }

    public void checkInAppPurchasedWithCheckSubs(boolean checkSubs) {
        mBillingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(ProductType.INAPP)
                        .build(),
                (billingResult, purchases) -> {
                    // check billingResult
                    // process returned purchase list, e.g. display the plans user owns
                    if (billingResult.getResponseCode() == BillingResponseCode.OK) {
                        runOnUiThread(() -> {
                            boolean hideAdsRestored = false;
                            for (Purchase purchase : purchases) {
                                // Handle the purchased item
                                List<String> productIds = purchase.getProducts();
                                for (String productId: productIds) {
                                    handlePurchase(productId);
                                    if (productId.equals(PRODUCT_ID_HIDE_ADS)) {
                                        hideAdsRestored = true;
                                    }
                                }

                                acknowledgePurchaseIfNeeded(purchase);
                            }
                            if (hideAdsRestored) {
                                if (isHideAdsFragmentShowing() && !checkSubs) {
                                    showRestoredDialog();
                                }
                            } else {
                                if (checkSubs) {
                                    checkSubsPurchasedFromInApp(true);
                                } else {
                                    if (isHideAdsFragmentShowing()) {
                                        showNotRestoredDialog();
                                    }
                                }
                            }
                        });
                    }
                }
        );
    }

    public void checkSubsPurchased() {
        checkSubsPurchasedFromInApp(false);
    }

    public void checkSubsPurchasedFromInApp(boolean fromInApp) {
        mBillingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(ProductType.SUBS)
                        .build(),
                (subsBillingResult, subsPurchases) -> {
                    // check billingResult
                    // process returned purchase list, e.g. display the plans user owns
                    if (subsBillingResult.getResponseCode() == BillingResponseCode.OK) {
                        runOnUiThread(() -> {
                            boolean hideAdsRestored = false;
                            for (Purchase purchase : subsPurchases) {
                                // Handle the purchased item
                                List<String> productIds = purchase.getProducts();
                                for (String productId: productIds) {
                                    handlePurchase(productId);
                                    if (productId.equals(PRODUCT_ID_HIDE_ADS_MONTHLY)) {
                                        hideAdsRestored = true;
                                    }
                                }

                                acknowledgePurchaseIfNeeded(purchase);
                            }
                            if (hideAdsRestored) {
                                if (isHideAdsFragmentShowing() && !fromInApp) {
                                    showRestoredDialog();
                                }
                            } else {
                                if (fromInApp) {
                                    showAdsAgainIfNeeded();
                                } else {
                                    if (isHideAdsFragmentShowing()) {
                                        showNotRestoredDialog();
                                    }
                                }
                            }
                        });
                    }
                }
        );
    }

    private void showAdsAgainIfNeeded() {
        if(mAdContainerView.getVisibility() == View.GONE) {
            mAdContainerView.getLayoutParams().height = (int)(getAdSize().getHeight() * mDensity);
            mAdContainerView.post(new Runnable() {
                @Override
                public void run() {
                    loadBanner();
                }
            });
            mAdContainerView.setVisibility(View.VISIBLE);

            SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
            preferences.edit().putBoolean("hideads", false).apply();
        }
    }

    private void acknowledgePurchaseIfNeeded(Purchase purchase) {
        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
            }
        };
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            }
        }
    }

    private boolean isHideAdsFragmentShowing() {
        HideAdsFragment hideAdsFragment = null;
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f.getClass().getName().equals("com.edolfzoku.hayaemon2.HideAdsFragment"))
                hideAdsFragment = (HideAdsFragment)f;
        }
        return hideAdsFragment != null;
    }

    private void showRestoredDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.restored);
        builder.setPositiveButton("OK", null);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                if (alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
            }
        });
        alertDialog.show();
    }

    private void showNotRestoredDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.notRestored);
        builder.setPositiveButton("OK", null);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                if (alertDialog.getWindow() != null) {
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
            }
        });
        alertDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mAdView != null) mAdView.pause();
    }

    public static void startNotification() {
        if(sPlayingPlaylist < 0 || sPlaylists.size() <= sPlayingPlaylist)
            return;
        ArrayList<SongItem> arSongs = sPlaylists.get(sPlayingPlaylist);
        if(PlaylistFragment.sPlaying < 0 || arSongs.size() <= PlaylistFragment.sPlaying) return;
        int playing = PlaylistFragment.sPlaying;
        SongItem item = arSongs.get(playing);
        Context context = sActivity != null ? sActivity : sService;
        if (context == null) return;
        Intent intent = new Intent(context, ForegroundService.class);
        intent.putExtra("strTitle", item.getTitle());
        intent.putExtra("strArtist", item.getArtist());
        intent.putExtra("strPathArtwork", item.getPathArtwork());
        intent.putExtra("strPath", item.getPath());
        if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent);
        else context.startService(intent);
    }

    public static void stopNotification() {
        Context context = sActivity != null ? sActivity : sService;
        Intent intent = new Intent(context, ForegroundService.class)
                .setAction("stop");
        if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent);
        else context.startService(intent);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (mShowDeadline) {
            mShowDeadline = false;
            showDeadline();
        }

        if(mShowUpdateLog) {
            mShowUpdateLog = false;

            LayoutInflater inflater = getLayoutInflater();
            final View layout = inflater.inflate(mDarkMode ? R.layout.updatelogdialog_dark : R.layout.updatelogdialog,
                    (ViewGroup)findViewById(R.id.layout_root));
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            TextView textUpdatelogTitle = layout.findViewById(R.id.textUpdatelogTitle);
            try {
                textUpdatelogTitle.setText(String.format(Locale.getDefault(), "ハヤえもんAndroid版ver.%sに\nアップデートされました！", getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName));
            }
            catch(PackageManager.NameNotFoundException e) {
                textUpdatelogTitle.setText("ハヤえもんAndroid版が\nアップデートされました！");
            }
            builder.setView(layout);

            TextView textViewBlog = layout.findViewById(R.id.textViewBlog);
            String strBlog = "この内容は<a href=\"http://hayaemon.jp/blog/\">開発者ブログ</a>から";
            CharSequence blogChar = Html.fromHtml(strBlog);
            textViewBlog.setText(blogChar);
            MovementMethod mMethod = LinkMovementMethod.getInstance();
            textViewBlog.setMovementMethod(mMethod);
            textViewBlog.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            TextView textViewArticle = layout.findViewById(R.id.textViewArticle);
            String strArticle = "<a href=\"http://hayaemon.jp/blog/archives/8991\">→該当記事へ</a>";
            CharSequence blogChar2 = Html.fromHtml(strArticle);
            textViewArticle.setText(blogChar2);
            textViewArticle.setMovementMethod(mMethod);

            TextView textView = layout.findViewById(R.id.textView);
            textView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            textView.setMovementMethod(ScrollingMovementMethod.getInstance());
            textView.setText(readChangeLog());

            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    if(alertDialog.getWindow() != null) {
                        alertDialog.getWindow().getDecorView().getBackground().setColorFilter(Color.parseColor("#00000000"), PorterDuff.Mode.SRC_IN);
                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                        lp.dimAmount = 0.4f;
                        alertDialog.getWindow().setAttributes(lp);
                    }
                }
            });
            alertDialog.show();
            Button btnClose = layout.findViewById(R.id.btnClose);
            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Switch switchNextHidden = layout.findViewById(R.id.switchNextHidden);
                    boolean bChecked = switchNextHidden.isChecked();
                    SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                    preferences.edit().putBoolean("hideupdatelognext", bChecked).apply();
                    alertDialog.dismiss();
                }
            });
            Button btnShare = layout.findViewById(R.id.btnShare);
            btnShare.setOnClickListener(new View.OnClickListener() {
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

    private void showDeadline() {
        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(mDarkMode ? R.layout.tenth_anniversary_dialog_dark : R.layout.tenth_anniversary_dialog,
                (ViewGroup) findViewById(R.id.layout_root));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                if (alertDialog.getWindow() != null) {
                    alertDialog.getWindow().getDecorView().getBackground().setColorFilter(Color.parseColor("#00000000"), PorterDuff.Mode.SRC_IN);
                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                    lp.dimAmount = 0.4f;
                    alertDialog.getWindow().setAttributes(lp);
                }
            }
        });
        alertDialog.show();
        Button btnDetail = layout.findViewById(R.id.btnDetail);
        btnDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://hayaemon.jp/blog/nenga2025");
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(i);
                alertDialog.dismiss();
            }
        });
        Button btnClose = layout.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    private File getScreenshot(View view) {
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

    private void loadData() {
        SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        ArrayList<ArrayList<SongItem>> arPlaylists = gson.fromJson(preferences.getString("arPlaylists",""), new TypeToken<ArrayList<ArrayList<SongItem>>>(){}.getType());
        ArrayList<ArrayList<EffectSaver>> arEffects = gson.fromJson(preferences.getString("arEffects",""), new TypeToken<ArrayList<ArrayList<EffectSaver>>>(){}.getType());
        ArrayList<ArrayList<String>> arLyrics = gson.fromJson(preferences.getString("arLyrics",""), new TypeToken<ArrayList<ArrayList<String>>>(){}.getType());
        ArrayList<String> arPlaylistNames = gson.fromJson(preferences.getString("arPlaylistNames",""), new TypeToken<ArrayList<String>>(){}.getType());
        List<String> arSongsPath = gson.fromJson(preferences.getString("arSongsPath",""), new TypeToken<List<String>>(){}.getType());
        if(arPlaylists != null && arPlaylistNames != null) {
            for(int i = 0; i < arPlaylists.size(); i++) {
                sPlaylists = arPlaylists;
                PlaylistFragment.sPlaylistNames = arPlaylistNames;
            }
            if(arEffects != null && arPlaylists.size() == arEffects.size())
                sEffects = arEffects;
            else {
                arEffects = sEffects;
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
                sLyrics = arLyrics;
            else {
                arLyrics = sLyrics;
                for(int i = 0; i < arPlaylists.size(); i++) {
                    ArrayList<String> arTempLyrics = new ArrayList<>();
                    ArrayList<SongItem> arSongs = arPlaylists.get(i);
                    for(int j = 0; j < arSongs.size(); j++) arTempLyrics.add(null);
                    arLyrics.add(arTempLyrics);
                }
            }
        }
        else if(arSongsPath != null) {
            playlistFragment.addPlaylist(String.format(Locale.getDefault(), "%s 1", getString(R.string.playlist)));
            playlistFragment.addPlaylist(String.format(Locale.getDefault(), "%s 2", getString(R.string.playlist)));
            playlistFragment.addPlaylist(String.format(Locale.getDefault(), "%s 3", getString(R.string.playlist)));
            playlistFragment.selectPlaylist(0);
            for(int i = 0; i < arSongsPath.size(); i++) {
                playlistFragment.addSong(this, Uri.parse(arSongsPath.get(i)));
            }
        }
        else {
            playlistFragment.addPlaylist(String.format(Locale.getDefault(), "%s 1", getString(R.string.playlist)));
            playlistFragment.addPlaylist(String.format(Locale.getDefault(), "%s 2", getString(R.string.playlist)));
            playlistFragment.addPlaylist(String.format(Locale.getDefault(), "%s 3", getString(R.string.playlist)));
            playlistFragment.selectPlaylist(0);
            ArrayList<SongItem> arSongs = sPlaylists.get(sSelectedPlaylist);
            SongItem item = new SongItem(String.format(Locale.getDefault(), "%d", arSongs.size()+1), "POTATOBOY", "airch", "potatoboy.m4a");
            item.setTime("4:30");
            item.setPathArtwork("potatoboy");
            arSongs.add(item);
            ArrayList<EffectSaver> arEffectSavers = sEffects.get(sSelectedPlaylist);
            EffectSaver saver = new EffectSaver();
            arEffectSavers.add(saver);
            ArrayList<String> arTempLyrics = sLyrics.get(sSelectedPlaylist);
            arTempLyrics.add("lonely lonely lonely,,yeah yeah\nlove me,,, yeah,yeah\n\n僕はロボット\n店の隅に置かれているよ\nコインを入れたら\n3分で君を笑顔にする\n\n楽しいリズムで\n踊ったら  これを食べればいいよ\n暖かくて  少し懐かしい？\n僕はロボット\n\n小さな君が恐る恐る\n僕を覗きこんだ\nボタンを早く早く押して？\n君を笑顔にしたい！！\n\nねえ、\n愛し愛されることなんて\n僕にはきっと分からないけど\n嫌なこと全部投げ出して\nほら、さあ、踊ろうよ！\n\nねえ、\n君がいつかこの街を\n誰かと抜け出してしまってもね\n嫌なことあった時にはさ\nほら、さあ、オモイダシテ！ \n\n僕はロボット\n寂しさなんて知らないはずさ\nだけどこの頃 \n仕上がりが少し冷めてるんだ\n\n君が来なくなって\n何度目の冬になるのだろうか\nコインを入れる所も少し錆びてきた\n\n大きくなった君はもう\n僕の背を抜かしたのかな\nこんな冷えた体じゃ\n君を笑顔にできないかな\n\nねえ、\n愛し愛されることなんて\n僕は望んじゃいけない事も\n忘れるくらいに踊りたい\nほら、さあ、動け体！\nねえ、\nいつか君がこの街に\n帰ってくると信じたいのは\n僕の愚かなバグデータかな？\nほら、さあ、踊ろうか\n\nやがて光も消えてこの街の\n明けない夜も\n冷えた床の温度も蹴散らすくらいに あああ\n僕のラストダンスだと錆だらけの\nロボットが\n動きだしたステップ踏んで あああ\n\nねえ\nとうとう君はこの街に\n帰ってこない わかってるけど\n暖かいポテトをどうぞ\nほら、さあ、召し上がれ\n抱いたバグデータもて余し\nもうすぐ僕は止まるんだろう\n嫌なこと全部投げ出して\nほら、さあ、踊ろうよ");
        }

        updateMenuButton();
        if (needsToDisplayDeadline(false, false)) {
            mShowDeadline = true;
            preferences.edit().putBoolean("b2025AnniversaryDisplayed", true).apply();
        }

        String strVersionName = preferences.getString("versionname", null);
        if (strVersionName != null) sPrevVersion = Float.parseFloat(strVersionName);
        boolean bHideUpdateLogNext = preferences.getBoolean("hideupdatelognext", false);
        String strCurrentVersionName;
        try {
            strCurrentVersionName = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
        }
        catch(PackageManager.NameNotFoundException e) {
            strCurrentVersionName = strVersionName;
        }
        if(!mShowDeadline && !bHideUpdateLogNext && Locale.getDefault().equals(Locale.JAPAN)) {
            if(strVersionName != null && !strCurrentVersionName.equals(strVersionName))
                mShowUpdateLog = true;
        }
        if(strVersionName == null) {
            preferences.edit().putBoolean("bPinkCamperDisplayed", true).apply();
            preferences.edit().putBoolean("bBlueCamperDisplayed", true).apply();
            preferences.edit().putBoolean("bOrangeCamperDisplayed", true).apply();
        }
        preferences.edit().putString("versionname", strCurrentVersionName).apply();

        sPlayNextByBPos = preferences.getBoolean("bPlayNextByBPos", false);
        boolean bSnap = preferences.getBoolean("bSnap", false);
        if (controlFragment != null) {
            controlFragment.setSnap(bSnap);
            controlFragment.setMinSpeed(preferences.getInt("nMinSpeed", 10));
            controlFragment.setMaxSpeed(preferences.getInt("nMaxSpeed", 400));
            controlFragment.setMinPitch(preferences.getInt("nMinPitch", -12));
            controlFragment.setMaxPitch(preferences.getInt("nMaxPitch", 12));
        }

        boolean bHideAds = preferences.getBoolean("hideads", false);
        if(bHideAds) hideAds(false, false);
        else {
            List<String> testDeviceIds = Arrays.asList("C274AC018E6509532711089B8BC4433D", "7A4DCE2E7A051D5905BDA9790B453502");
            RequestConfiguration configuration =
                    new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
            MobileAds.setRequestConfiguration(configuration);
            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                }
            });

            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                }
            });
            mAdContainerView.getLayoutParams().height = (int)(getAdSize().getHeight() * mDensity);
            mAdContainerView.post(new Runnable() {
                @Override
                public void run() {
                    loadBanner();
                }
            });
        }

        sShuffle = preferences.getInt("shufflemode", 0);
        if(sShuffle == 1) {
            mBtnShuffle.setContentDescription(getString(R.string.shuffleOn));
            mBtnShuffle.setImageResource(mDarkMode ? R.drawable.ic_bar_button_mode_shuffle_on_dark : R.drawable.ic_bar_button_mode_shuffle_on);
            mBtnShuffleInPlayingBar.setContentDescription(getString(R.string.shuffleOn));
            mBtnShuffleInPlayingBar.setImageResource(mDarkMode ? R.drawable.ic_playing_large_mode_shuffle_on_dark : R.drawable.ic_playing_large_mode_shuffle_on);
        }
        else if(sShuffle == 2) {
            mBtnShuffle.setContentDescription(getString(R.string.singleOn));
            mBtnShuffle.setImageResource(mDarkMode ? R.drawable.ic_bar_button_mode_single_on_dark : R.drawable.ic_bar_button_mode_single_on);
            mBtnShuffleInPlayingBar.setContentDescription(getString(R.string.singleOn));
            mBtnShuffleInPlayingBar.setImageResource(mDarkMode ? R.drawable.ic_playing_large_mode_single_on_dark : R.drawable.ic_playing_large_mode_single_on);
        }
        else {
            mBtnShuffle.setContentDescription(getString(R.string.shuffleOff));
            mBtnShuffle.setImageResource(mDarkMode ? R.drawable.ic_bar_button_mode_shuffle_dark : R.drawable.ic_bar_button_mode_shuffle);
            mBtnShuffleInPlayingBar.setContentDescription(getString(R.string.shuffleOff));
            mBtnShuffleInPlayingBar.setImageResource(mDarkMode ? R.drawable.ic_playing_large_mode_shuffle_dark : R.drawable.ic_playing_large_mode_shuffle);
        }

        sRepeat = preferences.getInt("repeatmode", 0);
        if(sRepeat == 1) {
            mBtnRepeat.setContentDescription(getString(R.string.repeatAllOn));
            mBtnRepeat.setImageResource(mDarkMode ? R.drawable.ic_bar_button_mode_repeat_all_on_dark : R.drawable.ic_bar_button_mode_repeat_all_on);
            mBtnRepeatInPlayingBar.setContentDescription(getString(R.string.repeatAllOn));
            mBtnRepeatInPlayingBar.setImageResource(mDarkMode ? R.drawable.ic_playing_large_mode_repeat_all_on_dark : R.drawable.ic_playing_large_mode_repeat_all_on);
        }
        else if(sRepeat == 2) {
            mBtnRepeat.setContentDescription(getString(R.string.repeatSingleOn));
            mBtnRepeat.setImageResource(mDarkMode ? R.drawable.ic_bar_button_mode_repeat_single_on_dark : R.drawable.ic_bar_button_mode_repeat_single_on);
            mBtnRepeatInPlayingBar.setContentDescription(getString(R.string.repeatSingleOn));
            mBtnRepeatInPlayingBar.setImageResource(mDarkMode ? R.drawable.ic_playing_large_mode_repeat_one_on_dark : R.drawable.ic_playing_large_mode_repeat_one_on);
        }
        else {
            mBtnRepeat.setContentDescription(getString(R.string.repeatOff));
            mBtnRepeat.setImageResource(mDarkMode ? R.drawable.ic_bar_button_mode_repeat_dark : R.drawable.ic_bar_button_mode_repeat);
            mBtnRepeatInPlayingBar.setContentDescription(getString(R.string.repeatOff));
            mBtnRepeatInPlayingBar.setImageResource(mDarkMode ? R.drawable.ic_playing_large_mode_repeat_all_dark : R.drawable.ic_playing_large_mode_repeat_all);
        }

        boolean darkMode = preferences.getBoolean("DarkMode", false);
        if(darkMode) setDarkMode(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) playlistFragment.startRecord();
            else if(Build.VERSION.SDK_INT >= 23) {
                if(!shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    AlertDialog.Builder builder;
                    if (mDarkMode)
                        builder = new AlertDialog.Builder(this, R.style.DarkModeDialog);
                    else builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.permitMicError);
                    builder.setMessage("");
                    builder.setNeutralButton(R.string.NotYet, null);
                    builder.setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String uriString = "package:" + getPackageName();
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(uriString));
                            startActivity(intent);
                        }
                    });
                    final AlertDialog alertDialog = builder.create();
                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            if (alertDialog.getWindow() != null) {
                                WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                                lp.dimAmount = 0.4f;
                                alertDialog.getWindow().setAttributes(lp);
                            }
                        }
                    });
                    alertDialog.show();
                }
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if(v.getId() == R.id.btnRewind || v.getId() == R.id.btnRewindInPlayingBar) {
            if(sStream == 0) return false;
            int chan = BASS_FX.BASS_FX_TempoGetSource(sStream);
            if(EffectFragment.isReverse())
                BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_FORWARD);
            else BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_REVERSE);
            BASS.BASS_ChannelSetAttribute(sStream, BASS_FX.BASS_ATTRIB_TEMPO, ControlFragment.sSpeed + 100);
            mBtnRewind.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#FF007AFF"), PorterDuff.Mode.SRC_IN));
            mBtnRewindInPlayingBar.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#FF007AFF"), PorterDuff.Mode.SRC_IN));
            return true;
        }
        else if(v.getId() == R.id.btnForward || v.getId() == R.id.btnForwardInPlayingBar) {
            if(sStream == 0) return false;
            BASS.BASS_ChannelSetAttribute(sStream, BASS_FX.BASS_ATTRIB_TEMPO, ControlFragment.sSpeed + 100);
            mBtnForward.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#FF007AFF"), PorterDuff.Mode.SRC_IN));
            mBtnForwardInPlayingBar.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#FF007AFF"), PorterDuff.Mode.SRC_IN));
            return true;
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.relativePlaying || v.getId() == R.id.imgViewDown) {
            int nY = (int) event.getRawY();
            if (mGestureDetector.onTouchEvent(event)) return false;
            if(mSeekCurPos.getVisibility() != View.VISIBLE) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    RelativeLayout.LayoutParams paramRelativePlaying = (RelativeLayout.LayoutParams) mRelativePlaying.getLayoutParams();
                    RelativeLayout.LayoutParams paramContainer = (RelativeLayout.LayoutParams) mViewPager.getLayoutParams();
                    RelativeLayout.LayoutParams paramRecording = (RelativeLayout.LayoutParams) mRelativeRecording.getLayoutParams();
                    if (MainActivity.sRecord != 0) {
                        paramContainer.addRule(RelativeLayout.ABOVE, R.id.relativeRecording);
                        paramContainer.bottomMargin = 0;
                        paramRecording.addRule(RelativeLayout.ABOVE, R.id.ad_view_container);
                        paramRecording.bottomMargin = (int) (60.0 * mDensity);
                    } else {
                        paramContainer.addRule(RelativeLayout.ABOVE, R.id.ad_view_container);
                        paramContainer.bottomMargin = (int) (60.0 * mDensity);
                    }
                    RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) mRelativePlayingWithShadow.getLayoutParams();
                    int nHeight = param.height - (nY - mLastY);
                    int nMinHeight = (int) (82.0 * mDensity);
                    int nMaxHeight = (int) (142.0 * mDensity);
                    if (nHeight < nMinHeight) nHeight = nMinHeight;
                    else if (nHeight > nMaxHeight) nHeight = nMaxHeight;
                    paramRelativePlaying.bottomMargin = nHeight - nMinHeight;
                    param.height = nHeight;
                    mRelativePlayingWithShadow.setLayoutParams(param);
                }
                mLastY = nY;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) mRelativePlayingWithShadow.getLayoutParams();
                    int nMinHeight = (int) (82.0 * mDensity);
                    if (param.height > nMinHeight) return false;
                    else {
                        RelativeLayout.LayoutParams paramContainer = (RelativeLayout.LayoutParams) mViewPager.getLayoutParams();
                        RelativeLayout.LayoutParams paramRecording = (RelativeLayout.LayoutParams) mRelativeRecording.getLayoutParams();
                        if (MainActivity.sRecord != 0) {
                            paramContainer.addRule(RelativeLayout.ABOVE, R.id.relativeRecording);
                            paramContainer.bottomMargin = 0;
                            paramRecording.addRule(RelativeLayout.ABOVE, R.id.relativePlayingWithShadow);
                            paramRecording.bottomMargin = (int) (-22 * mDensity);
                        } else {
                            paramContainer.addRule(RelativeLayout.ABOVE, R.id.relativePlayingWithShadow);
                            paramContainer.bottomMargin = (int) (-22 * mDensity);
                        }
                    }
                }
            }
            else {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    final int nCurrentHeight = getResources().getDisplayMetrics().heightPixels - mTabLayout.getHeight() - mLinearControl.getHeight() - getStatusBarHeight() + (int) (16.0 * mDensity);
                    final int nMaxHeight = getResources().getDisplayMetrics().heightPixels - mTabLayout.getHeight() - getStatusBarHeight() + (int) (22.0 * mDensity);
                    final int nMinHeight = (int) (82.0 * mDensity);
                    int nMinTranslationY = nCurrentHeight - nMaxHeight;
                    int nMaxTranslationY = nCurrentHeight - nMinHeight;
                    int nTranslationY = (mLastY + nY);
                    if(nTranslationY < nMinTranslationY) nTranslationY = nMinTranslationY;
                    else if(nTranslationY > nMaxTranslationY) nTranslationY = nMaxTranslationY;
                    mRelativePlayingWithShadow.setTranslationY(nTranslationY);
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    mLastY = (int) mRelativePlayingWithShadow.getTranslationY() - nY;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if(mRelativePlayingWithShadow.getTranslationY() > (int) (100.0 * mDensity)) {
                        downViewPlaying(false);
                    }
                    else {
                        final int nTranslationYFrom = (int)mRelativePlayingWithShadow.getTranslationY();
                        final int nTranslationY = 0;

                        ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
                        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                float fProgress = valueAnimator.getAnimatedFraction();
                                mRelativePlayingWithShadow.setTranslationY(nTranslationYFrom + (nTranslationY - nTranslationYFrom) * fProgress);
                            }
                        });
                        anim.setDuration(200).start();
                    }
                }
            }
            if(event.getAction() == MotionEvent.ACTION_MOVE)
                return true;
            return (v.getId() == R.id.relativePlaying && mSeekCurPos.getVisibility() == View.VISIBLE);
        }

        if(event.getAction() == MotionEvent.ACTION_UP) {
            int color = getResources().getColor(mDarkMode ? R.color.darkModeBk : R.color.lightModeBk);
            mRelativeSave.setBackgroundColor(color);
            mRelativeLock.setBackgroundColor(color);
            mRelativeNotice.setBackgroundColor(color);
            mRelativeAddSong.setBackgroundColor(color);
            mRelativeHideAds.setBackgroundColor(color);
            mRelativeItem.setBackgroundColor(color);
            mRelativeHelp.setBackgroundColor(color);
            mRelativeInquiry.setBackgroundColor(color);
            mRelativeReview.setBackgroundColor(color);
            mRelativeInfo.setBackgroundColor(color);
            if(v.getId() == R.id.btnRewind || v.getId() == R.id.btnRewindInPlayingBar) {
                if(sStream == 0) return false;
                int chan = BASS_FX.BASS_FX_TempoGetSource(sStream);
                if(EffectFragment.isReverse())
                    BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_REVERSE);
                else
                    BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_FORWARD);
                BASS.BASS_ChannelSetAttribute(sStream, BASS_FX.BASS_ATTRIB_TEMPO, ControlFragment.sSpeed);
                mBtnRewind.clearColorFilter();
                mBtnRewindInPlayingBar.clearColorFilter();
            }
            else if(v.getId() == R.id.btnForward || v.getId() == R.id.btnForwardInPlayingBar) {
                if(sStream == 0) return false;
                BASS.BASS_ChannelSetAttribute(sStream, BASS_FX.BASS_ATTRIB_TEMPO, ControlFragment.sSpeed);
                mBtnForward.clearColorFilter();
                mBtnForwardInPlayingBar.clearColorFilter();
            }
        }
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            int color = mDarkMode ? getResources().getColor(R.color.darkModeLightBk) : Color.argb(255, 229, 229, 229);
            if(v.getId() == R.id.relativeSave) mRelativeSave.setBackgroundColor(color);
            if(v.getId() == R.id.relativeLock) mRelativeLock.setBackgroundColor(color);
            if(v.getId() == R.id.relativeNotice) mRelativeNotice.setBackgroundColor(color);
            if(v.getId() == R.id.relativeAddSong) mRelativeAddSong.setBackgroundColor(color);
            if(v.getId() == R.id.relativeHideAds) mRelativeHideAds.setBackgroundColor(color);
            if(v.getId() == R.id.relativeItem) mRelativeItem.setBackgroundColor(color);
            if(v.getId() == R.id.relativeHelp) mRelativeHelp.setBackgroundColor(color);
            if(v.getId() == R.id.relativeInquiry) mRelativeInquiry.setBackgroundColor(color);
            if(v.getId() == R.id.relativeReview) mRelativeReview.setBackgroundColor(color);
            if(v.getId() == R.id.relativeInfo) mRelativeInfo.setBackgroundColor(color);
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnMenu) {
            updateDrawer();
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
        else if(v.getId() == R.id.btnShuffle || v.getId() == R.id.btnShuffleInPlayingBar) {
            if(mBtnShuffle.getContentDescription().toString().equals(getString(R.string.shuffleOff))) {
                mBtnShuffle.setContentDescription(getString(R.string.shuffleOn));
                sShuffle = 1;
                mBtnShuffle.setImageResource(mDarkMode ? R.drawable.ic_bar_button_mode_shuffle_on_dark : R.drawable.ic_bar_button_mode_shuffle_on);
                mBtnShuffleInPlayingBar.setContentDescription(getString(R.string.shuffleOn));
                mBtnShuffleInPlayingBar.setImageResource(mDarkMode ? R.drawable.ic_playing_large_mode_shuffle_on_dark : R.drawable.ic_playing_large_mode_shuffle_on);
                sendAccessibilityEvent(getString(R.string.shuffleOn), v);
            }
            else if(mBtnShuffle.getContentDescription().toString().equals(getString(R.string.shuffleOn))) {
                mBtnShuffle.setContentDescription(getString(R.string.singleOn));
                sShuffle = 2;
                mBtnShuffle.setImageResource(mDarkMode ? R.drawable.ic_bar_button_mode_single_on_dark : R.drawable.ic_bar_button_mode_single_on);
                mBtnShuffleInPlayingBar.setContentDescription(getString(R.string.singleOn));
                mBtnShuffleInPlayingBar.setImageResource(mDarkMode ? R.drawable.ic_playing_large_mode_single_on_dark : R.drawable.ic_playing_large_mode_single_on);
                sendAccessibilityEvent(getString(R.string.singleOn), v);
            }
            else {
                mBtnShuffle.setContentDescription(getString(R.string.shuffleOff));
                sShuffle = 0;
                mBtnShuffle.setImageResource(mDarkMode ? R.drawable.ic_bar_button_mode_shuffle_dark : R.drawable.ic_bar_button_mode_shuffle);
                mBtnShuffleInPlayingBar.setContentDescription(getString(R.string.shuffleOff));
                mBtnShuffleInPlayingBar.setImageResource(mDarkMode ? R.drawable.ic_playing_large_mode_shuffle_dark : R.drawable.ic_playing_large_mode_shuffle);
                sendAccessibilityEvent(getString(R.string.shuffleOff), v);
            }
            PlaylistFragment.saveFiles(false, false, false, false, true);
        }
        else if(v.getId() == R.id.btnRepeat || v.getId() == R.id.btnRepeatInPlayingBar) {
            if(mBtnRepeat.getContentDescription().toString().equals(getString(R.string.repeatOff))) {
                mBtnRepeat.setContentDescription(getString(R.string.repeatAllOn));
                sRepeat = 1;
                mBtnRepeat.setImageResource(mDarkMode ? R.drawable.ic_bar_button_mode_repeat_all_on_dark : R.drawable.ic_bar_button_mode_repeat_all_on);
                mBtnRepeatInPlayingBar.setContentDescription(getString(R.string.repeatAllOn));
                mBtnRepeatInPlayingBar.setImageResource(mDarkMode ? R.drawable.ic_playing_large_mode_repeat_all_on_dark : R.drawable.ic_playing_large_mode_repeat_all_on);
                sendAccessibilityEvent(getString(R.string.repeatAllOn), v);
            }
            else if(mBtnRepeat.getContentDescription().toString().equals(getString(R.string.repeatAllOn))) {
                mBtnRepeat.setContentDescription(getString(R.string.repeatSingleOn));
                sRepeat = 2;
                mBtnRepeat.setImageResource(mDarkMode ? R.drawable.ic_bar_button_mode_repeat_single_on_dark : R.drawable.ic_bar_button_mode_repeat_single_on);
                mBtnRepeatInPlayingBar.setContentDescription(getString(R.string.repeatSingleOn));
                mBtnRepeatInPlayingBar.setImageResource(mDarkMode ? R.drawable.ic_playing_large_mode_repeat_one_on_dark : R.drawable.ic_playing_large_mode_repeat_one_on);
                sendAccessibilityEvent(getString(R.string.repeatSingleOn), v);
            }
            else {
                mBtnRepeat.setContentDescription(getString(R.string.repeatOff));
                sRepeat = 0;
                mBtnRepeat.setImageResource(mDarkMode ? R.drawable.ic_bar_button_mode_repeat_dark : R.drawable.ic_bar_button_mode_repeat);
                mBtnRepeatInPlayingBar.setContentDescription(getString(R.string.repeatOff));
                mBtnRepeatInPlayingBar.setImageResource(mDarkMode ? R.drawable.ic_playing_large_mode_repeat_all_dark : R.drawable.ic_playing_large_mode_repeat_all);
                sendAccessibilityEvent(getString(R.string.repeatOff), v);
            }
            PlaylistFragment.saveFiles(false, false, false, false, true);
        }
        else if(v.getId() == R.id.relativeLock) {
            mDrawerLayout.closeDrawer(GravityCompat.START);

            ArrayList<EffectSaver> arEffectSavers = sEffects.get(sPlayingPlaylist);
            EffectSaver saver = arEffectSavers.get(PlaylistFragment.sPlaying);
            if(saver.isSave()) {
                saver.setSave(false);
                playlistFragment.getSongsAdapter().notifyItemChanged(PlaylistFragment.sPlaying);

                PlaylistFragment.saveFiles(false, true, false, false, false);
            }
            else {
                PlaylistFragment.setSavingEffect();
                playlistFragment.getSongsAdapter().notifyItemChanged(PlaylistFragment.sPlaying);
            }
        }
        else if(v.getId() == R.id.relativeSave) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            showSaveExportMenu();
        }
        else if(v.getId() == R.id.relativeNotice) {
            mDrawerLayout.closeDrawer(GravityCompat.START);

            showDeadline();
            SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
            preferences.edit().putBoolean("b2025AnniversaryDisplayedFromNotice", true).apply();
            updateMenuButton();
        }
        else if(v.getId() == R.id.relativeAddSong) {
            mDrawerLayout.closeDrawer(GravityCompat.START);

            final BottomMenu menu = new BottomMenu(this);
            menu.setTitle(getString(R.string.addSong));
            menu.addMenu(getString(R.string.addFromLocal), mDarkMode ? R.drawable.ic_actionsheet_music_dark : R.drawable.ic_actionsheet_music, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menu.dismiss();
                    open();
                }
            });
            if(Build.VERSION.SDK_INT >= 18) {
                menu.addMenu(getString(R.string.addFromVideo), mDarkMode ? R.drawable.ic_actionsheet_film_dark : R.drawable.ic_actionsheet_film, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menu.dismiss();
                        openGallery();
                    }
                });
            }
            final MainActivity activity = this;
            menu.addMenu(getString(R.string.addURL), mDarkMode ? R.drawable.ic_actionsheet_globe_dark : R.drawable.ic_actionsheet_globe, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menu.dismiss();

                    AlertDialog.Builder builder;
                    if(mDarkMode)
                        builder = new AlertDialog.Builder(activity, R.style.DarkModeDialog);
                    else
                        builder = new AlertDialog.Builder(activity);
                    builder.setTitle(R.string.addURL);
                    LinearLayout linearLayout = new LinearLayout(activity);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    final ClearableEditText editURL = new ClearableEditText(activity, activity.isDarkMode());
                    editURL.setHint(R.string.URL);
                    editURL.setText("");
                    linearLayout.addView(editURL);
                    builder.setView(linearLayout);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            playlistFragment.startAddURL(editURL.getText().toString());
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, null);
                    final AlertDialog alertDialog = builder.create();
                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            if(alertDialog.getWindow() != null) {
                                WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                                lp.dimAmount = 0.4f;
                                alertDialog.getWindow().setAttributes(lp);
                            }
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
        else if(v.getId() == R.id.relativeHideAds) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            openHideAds();
        }
        else if(v.getId() == R.id.relativeItem) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            openItem();
        }
        else if(v.getId() == R.id.relativeHelp) {
            Uri uri = Uri.parse("http://hayaemon.jp/blog/faq");
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(i);
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        else if(v.getId() == R.id.relativeInquiry) {
            String strVersionName;
            try {
                strVersionName = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
            }
            catch(PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return;
            }
            if (Locale.getDefault().equals(Locale.JAPAN)) {
                Uri uri = Uri.parse("https://docs.google.com/forms/d/e/1FAIpQLSdtMuEZsi8Xb5CDnyvPyLutgrKhFNQgVLdPmV93eo1s_Q6xTQ/viewform?usp=pp_url&entry.1013674018=" + Build.MODEL + "&entry.676820163=" + "Android " + Build.VERSION.RELEASE + "&entry.2144508540=Android ver." + strVersionName);
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(i);
            } else {
                Uri uri = Uri.parse("https://docs.google.com/forms/d/e/1FAIpQLSfV2ql2kSRo_xk-tiQ8hZ8on0bDmVCeSZjBus4mPF_m1TU_rQ/viewform?usp=pp_url&entry.1811262495=" + Build.MODEL + "&entry.1786651102=" + "Android " + Build.VERSION.RELEASE + "&entry.169112498=Android ver." + strVersionName);
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(i);
            }
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        else if(v.getId() == R.id.relativeReview) {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.edolfzoku.hayaemon2&hl=ja");
            Intent i = new Intent(Intent.ACTION_VIEW,uri);
            startActivity(i);
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        else if(v.getId() == R.id.relativeInfo) {
            mDrawerLayout.closeDrawer(GravityCompat.START);

            AlertDialog.Builder builder;
            if(mDarkMode)
                builder = new AlertDialog.Builder(this, R.style.DarkModeDialog);
            else builder = new AlertDialog.Builder(this);
            try {
                String strVersionName = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
                builder.setMessage(String.format(Locale.getDefault(), "%s: Android ver.%s", getString(R.string.version), strVersionName));
            }
            catch(PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return;
            }

            builder.setTitle(R.string.about);
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) { }
            });
            builder.setNeutralButton(getString(R.string.copyInfo), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    String strVersionName;
                    try {
                        strVersionName = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
                    }
                    catch(PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        return;
                    }
                    final String copyText = getString(R.string.app_name) + " Android ver." + strVersionName;
                    ClipboardManager clipboardManager =
                            (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (null == clipboardManager) {
                        return;
                    }
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("", copyText));

                    sHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder;
                            if(mDarkMode)
                                builder = new AlertDialog.Builder(sActivity, R.style.DarkModeDialog);
                            else builder = new AlertDialog.Builder(sActivity);

                            builder.setTitle(R.string.about);
                            builder.setMessage(getString(R.string.copied1) + copyText + getString(R.string.copied2));
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) { }
                            });
                            final AlertDialog alertDialog = builder.create();
                            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface arg0) {
                                    if(alertDialog.getWindow() != null) {
                                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                                        lp.dimAmount = 0.4f;
                                        alertDialog.getWindow().setAttributes(lp);
                                    }
                                }
                            });
                            alertDialog.show();
                        }
                    }, 200);
                }
            });
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    if(alertDialog.getWindow() != null) {
                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                        lp.dimAmount = 0.4f;
                        alertDialog.getWindow().setAttributes(lp);
                    }
                }
            });
            alertDialog.show();
        }
        else if(v.getId() == R.id.btnSetting) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            openSetting();
        }
        else if(v.getId() == R.id.btnDarkMode) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            if(mDarkMode) setLightMode();
            else setDarkMode(true);
        }
        else if(v.getId() == R.id.btnPlayInPlayingBar) PlaylistFragment.onPlayBtnClick();
        else if(v.getId() == R.id.btnForwardInPlayingBar) PlaylistFragment.onForwardBtnClick();
        else if(v.getId() == R.id.btnRewindInPlayingBar) PlaylistFragment.onRewindBtnClick();
        else if(v.getId() == R.id.relativePlaying) upViewPlaying();
        else if(v.getId() == R.id.imgViewDown) downViewPlaying(false);
        else if(v.getId() == R.id.btnCloseInPlayingBar) PlaylistFragment.stop(false);
        else if(v.getId() == R.id.btnMoreInPlayingBar) {
            final BottomMenu menu = new BottomMenu(this);
            final int nPlaying = PlaylistFragment.sPlaying;
            PlaylistFragment.sSelectedItem = nPlaying;
            SongItem item = sPlaylists.get(sPlayingPlaylist).get(nPlaying);
            menu.setTitle(item.getTitle());
            menu.addMenu(getString(R.string.saveExport), mDarkMode ? R.drawable.ic_actionsheet_save_dark : R.drawable.ic_actionsheet_save, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menu.dismiss();
                    showSaveExportMenu();
                }
            });
            menu.addMenu(getString(R.string.changeTitleAndArtist), mDarkMode ? R.drawable.ic_actionsheet_edit_dark : R.drawable.ic_actionsheet_edit, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menu.dismiss();
                    playlistFragment.changeTitleAndArtist(nPlaying);
                }
            });
            menu.addMenu(getString(R.string.showLyrics), mDarkMode ? R.drawable.ic_actionsheet_file_text_dark : R.drawable.ic_actionsheet_file_text, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menu.dismiss();
                    downViewPlaying(false);
                    playlistFragment.showLyrics();
                    mViewPager.setCurrentItem(0);
                }
            });
            ArrayList<EffectSaver> arEffectSavers = sEffects.get(sSelectedPlaylist);
            final EffectSaver saver = arEffectSavers.get(nPlaying);
            if(saver.isSave()) {
                menu.addMenu(getString(R.string.cancelRestoreEffect), mDarkMode ? R.drawable.ic_actionsheet_unlock_dark : R.drawable.ic_actionsheet_unlock, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        saver.setSave(false);
                        playlistFragment.getSongsAdapter().notifyItemChanged(nPlaying);

                        PlaylistFragment.saveFiles(false, true, false, false, false);
                        menu.dismiss();
                    }
                });
            }
            else {
                menu.addMenu(getString(R.string.restoreEffect), mDarkMode ? R.drawable.ic_actionsheet_lock_dark : R.drawable.ic_actionsheet_lock, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PlaylistFragment.setSavingEffect();
                        playlistFragment.getSongsAdapter().notifyItemChanged(nPlaying);
                        menu.dismiss();
                    }
                });
            }
            menu.setCancelMenu();
            menu.show();
        }
        else if(v.getId() == R.id.btnArtworkInPlayingBar) {
            final int nPlaying = PlaylistFragment.sPlaying;
            PlaylistFragment.sSelectedItem = nPlaying;
            final SongItem item = sPlaylists.get(sPlayingPlaylist).get(nPlaying);
            final BottomMenu menu = new BottomMenu(this);
            menu.setTitle(getString(R.string.changeArtwork));
            menu.addMenu(getString(R.string.setImage), mDarkMode ? R.drawable.ic_actionsheet_image_dark : R.drawable.ic_actionsheet_image, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menu.dismiss();
                    if (Build.VERSION.SDK_INT < 19) {
                        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        playlistFragment.startActivityForResult(intent, 3);
                    }
                    else {
                        final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/*");
                        playlistFragment.startActivityForResult(intent, 3);
                    }
                }
            });
            if(item.getPathArtwork() != null && !item.getPathArtwork().equals("")) {
                final MainActivity activity = this;
                menu.addDestructiveMenu(getString(R.string.resetArtwork), mDarkMode ? R.drawable.ic_actionsheet_initialize_dark : R.drawable.ic_actionsheet_initialize, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menu.dismiss();
                        AlertDialog.Builder builder;
                        if(mDarkMode)
                            builder = new AlertDialog.Builder(activity, R.style.DarkModeDialog);
                        else builder = new AlertDialog.Builder(activity);
                        builder.setTitle(R.string.resetArtwork);
                        builder.setMessage(R.string.askResetArtwork);
                        builder.setPositiveButton(getString(R.string.decideNot), null);
                        builder.setNegativeButton(getString(R.string.doReset), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                playlistFragment.resetArtwork();
                            }
                        });
                        final AlertDialog alertDialog = builder.create();
                        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface arg0) {
                                if(alertDialog.getWindow() != null) {
                                    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                                    lp.dimAmount = 0.4f;
                                    alertDialog.getWindow().setAttributes(lp);
                                }
                                Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                                negativeButton.setTextColor(mDarkMode ? getResources().getColor(R.color.darkModeRed) : Color.argb(255, 255, 0, 0));
                            }
                        });
                        alertDialog.show();
                    }
                });
            }
            menu.setCancelMenu();
            menu.show();
        }
    }

    private void upViewPlaying() {
        playlistFragment.selectPlaylist(sPlayingPlaylist);
        mRelativePlaying.setOnClickListener(null);
        mBtnArtworkInPlayingBar.setOnClickListener(this);
        mBtnArtworkInPlayingBar.setAnimation(true);
        mBtnArtworkInPlayingBar.setClickable(true);
        final long lDuration = 400;
        int nScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mRelativePlayingWithShadow.setBackgroundResource(mDarkMode ? R.drawable.playingview_dark : R.drawable.playingview);
        RelativeLayout.LayoutParams paramContainer = (RelativeLayout.LayoutParams) mViewPager.getLayoutParams();
        RelativeLayout.LayoutParams paramRecording = (RelativeLayout.LayoutParams) mRelativeRecording.getLayoutParams();
        if (MainActivity.sRecord != 0) {
            paramContainer.addRule(RelativeLayout.ABOVE, R.id.relativeRecording);
            paramContainer.bottomMargin = 0;
            paramRecording.addRule(RelativeLayout.ABOVE, R.id.ad_view_container);
            paramRecording.bottomMargin = (int) (60.0 * mDensity);
        } else {
            paramContainer.addRule(RelativeLayout.ABOVE, R.id.ad_view_container);
            paramContainer.bottomMargin = (int) (60.0 * mDensity);
        }

        final RelativeLayout.LayoutParams paramRelativePlaying = (RelativeLayout.LayoutParams) mRelativePlaying.getLayoutParams();
        final RelativeLayout.LayoutParams paramRelativePlayingWithShadow = (RelativeLayout.LayoutParams) mRelativePlayingWithShadow.getLayoutParams();
        final RelativeLayout.LayoutParams paramTitle = (RelativeLayout.LayoutParams) mTextTitle.getLayoutParams();
        final RelativeLayout.LayoutParams paramArtist = (RelativeLayout.LayoutParams) mTextArtist.getLayoutParams();
        final RelativeLayout.LayoutParams paramBtnPlay = (RelativeLayout.LayoutParams) mBtnPlayInPlayingBar.getLayoutParams();
        final RelativeLayout.LayoutParams paramBtnForward = (RelativeLayout.LayoutParams) mBtnForwardInPlayingBar.getLayoutParams();

        paramRelativePlayingWithShadow.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
        paramRelativePlayingWithShadow.addRule(RelativeLayout.ABOVE, 0);
        mTextTitle.setGravity(Gravity.CENTER);
        mTextArtist.setGravity(Gravity.CENTER);
        paramTitle.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        paramArtist.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        if(Build.VERSION.SDK_INT >= 17) {
            paramTitle.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
            paramArtist.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
        }

        final int nTranslationYFrom = (int)mRelativePlayingWithShadow.getTranslationY();
        final int nTranslationY = 0;
        final int nRelativePlayingHeightFrom = mRelativePlayingWithShadow.getHeight();
        int nHeight;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = this.getWindowManager().getCurrentWindowMetrics();
            Insets insets = windowMetrics.getWindowInsets()
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            int ScreenHeight = windowMetrics.getBounds().height();
            int StatusBar = insets.top;
            int NavigationBar = insets.bottom;
            nHeight = ScreenHeight - StatusBar - NavigationBar - mLinearControl.getHeight();
        } else {
            nHeight = getResources().getDisplayMetrics().heightPixels - mLinearControl.getHeight(); //  - safeInsetTop - mLinearControl.getHeight(); // (int) (22.0 * mDensity); // displaySize.y - safeInsetTop; // getResources().getDisplayMetrics().heightPixels + (int) (60.0 * mDensity) - mLinearControl.getHeight();
        }
        final int nRelativePlayingHeight = nHeight;
        final int nRelativePlayingBottomMarginFrom = paramRelativePlaying.bottomMargin;
        final int nRelativePlayingBottomMargin = 0;
        final int nArtworkWidthFrom = mBtnArtworkInPlayingBar.getWidth();
        final int nArtworkWidth = nScreenWidth / 2;
        final int nArtworkMarginFrom = (int) (8.0 * mDensity);
        final int nArtworkLeftMargin = nScreenWidth / 2 - nArtworkWidth / 2;
        final int nArtworkTopMargin = (int) (64.0 * mDensity);
        final int nTitleTopMarginFrom = paramTitle.topMargin;
        final int nTitleLeftMarginFrom = paramTitle.leftMargin;
        final int nTitleRightMarginFrom = paramTitle.rightMargin;
        final int nTitleMargin = (int) (32.0 * mDensity);
        final int nTitleTopMargin = nArtworkTopMargin + nArtworkWidth + (int) (32.0 * mDensity) + (int) (24.0 * mDensity) + (int) (34.0 * mDensity);
        final int nArtistTopMarginFrom = paramArtist.topMargin;
        final int nArtistTopMargin = nTitleTopMargin + mTextTitle.getHeight() + (int) (4.0 * mDensity);
        final int nBtnPlayTopMargin = nArtistTopMargin + mTextArtist.getHeight() + (int) (20.0 * mDensity);
        final int nBtnPlayRightMarginFrom = paramBtnPlay.rightMargin;
        final int nBtnPlayRightMargin = nScreenWidth / 2 - mBtnPlayInPlayingBar.getWidth() / 2;
        final int nBtnForwardRightMarginFrom = paramBtnForward.rightMargin;
        final int nBtnForwardRightMargin = nBtnPlayRightMargin - mBtnForwardInPlayingBar.getWidth() - (int) (16.0 * mDensity);
        final float fTitleFontFrom = 13.0f;
        final float fTitleFont = 15.0f;
        Paint paint = new Paint();
        paint.setTextSize(mTextTitle.getTextSize());
        final int nTitleWidthFrom = (int) paint.measureText(mTextTitle.getText().toString());
        final int nTitleWidth = nScreenWidth;
        final float fArtistFontFrom = 10.0f;
        final float fArtistFont = 13.0f;
        paint.setTextSize(mTextArtist.getTextSize());
        final int nArtistWidthFrom = (int) paint.measureText(mTextArtist.getText().toString());
        final int nArtistWidth = nScreenWidth;
        final int nBtnHeightFrom = (int) (60.0 * mDensity);
        final int nBtnHeight = (int) (44.0 * mDensity);

        ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fProgress = valueAnimator.getAnimatedFraction();
                advanceAnimation(mTabLayout, "bottomMargin", 0, -mTabLayout.getHeight(), fProgress);
                advanceAnimation(mViewSep2, "bottomMargin", 0, mTabLayout.getHeight(), fProgress);
                mRelativePlayingWithShadow.setTranslationY(nTranslationYFrom + (nTranslationY - nTranslationYFrom) * fProgress);
                advanceAnimation(mRelativePlayingWithShadow, "height", nRelativePlayingHeightFrom, nRelativePlayingHeight, fProgress);
                advanceAnimation(mRelativePlayingWithShadow, "bottomMargin", mTabLayout.getHeight() + mAdContainerView.getHeight(), 0, fProgress);
                advanceAnimation(mRelativePlaying, "height", nRelativePlayingHeightFrom, nRelativePlayingHeight, fProgress);
                advanceAnimation(mRelativePlaying, "bottomMargin", nRelativePlayingBottomMarginFrom, nRelativePlayingBottomMargin, fProgress);
                advanceAnimation(mBtnArtworkInPlayingBar, "width", nArtworkWidthFrom, nArtworkWidth, fProgress);
                advanceAnimation(mBtnArtworkInPlayingBar, "height", nArtworkWidthFrom, nArtworkWidth, fProgress);
                advanceAnimation(mBtnArtworkInPlayingBar, "leftMargin", nArtworkMarginFrom, nArtworkLeftMargin, fProgress);
                advanceAnimation(mBtnArtworkInPlayingBar, "topMargin", nArtworkMarginFrom, nArtworkTopMargin, fProgress);
                advanceAnimation(mTextTitle, "width", nTitleWidthFrom, nTitleWidth, fProgress);
                advanceAnimation(mTextTitle, "topMargin", nTitleTopMarginFrom, nTitleTopMargin, fProgress);
                advanceAnimation(mTextTitle, "leftMargin", nTitleLeftMarginFrom, nTitleMargin, fProgress);
                advanceAnimation(mTextTitle, "rightMargin", nTitleRightMarginFrom, nTitleMargin, fProgress);
                mTextTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fTitleFontFrom + (fTitleFont - fTitleFontFrom) * fProgress);
                advanceAnimation(mTextArtist, "width", nArtistWidthFrom, nArtistWidth, fProgress);
                advanceAnimation(mTextArtist, "topMargin", nArtistTopMarginFrom, nArtistTopMargin, fProgress);
                advanceAnimation(mTextArtist, "leftMargin", nTitleLeftMarginFrom, nTitleMargin, fProgress);
                advanceAnimation(mTextArtist, "rightMargin", nTitleRightMarginFrom, nTitleMargin, fProgress);
                mTextArtist.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fArtistFontFrom + (fArtistFont - fArtistFontFrom) * fProgress);
                advanceAnimation(mBtnPlayInPlayingBar, "topMargin", 0, nBtnPlayTopMargin, fProgress);
                advanceAnimation(mBtnForwardInPlayingBar, "topMargin", 0, nBtnPlayTopMargin, fProgress);
                advanceAnimation(mBtnPlayInPlayingBar, "rightMargin", nBtnPlayRightMarginFrom, nBtnPlayRightMargin, fProgress);
                advanceAnimation(mBtnPlayInPlayingBar, "height", nBtnHeightFrom, nBtnHeight, fProgress);
                advanceAnimation(mBtnForwardInPlayingBar, "height", nBtnHeightFrom, nBtnHeight, fProgress);
                advanceAnimation(mBtnRewindInPlayingBar, "height", nBtnHeightFrom, nBtnHeight, fProgress);
                advanceAnimation(mBtnForwardInPlayingBar, "rightMargin", nBtnForwardRightMarginFrom, nBtnForwardRightMargin, fProgress);
                mBtnMoreInPlayingBar.requestLayout();
                mBtnShuffleInPlayingBar.requestLayout();
                mBtnRepeatInPlayingBar.requestLayout();
            }
        });
        anim.setDuration(lDuration).start();

        mImgViewDown.setVisibility(View.VISIBLE);
        mSeekCurPos.setVisibility(View.VISIBLE);
        mTextCurPos.setVisibility(View.VISIBLE);
        mTextRemain.setVisibility(View.VISIBLE);
        mBtnRewindInPlayingBar.setVisibility(View.VISIBLE);
        mBtnMoreInPlayingBar.setVisibility(View.VISIBLE);
        mBtnShuffleInPlayingBar.setVisibility(View.VISIBLE);
        mBtnRepeatInPlayingBar.setVisibility(View.VISIBLE);

        if (BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PLAYING)
            mBtnPlayInPlayingBar.setImageResource(mDarkMode ? R.drawable.ic_playing_large_play_dark : R.drawable.ic_playing_large_play);
        else mBtnPlayInPlayingBar.setImageResource(mDarkMode ? R.drawable.ic_playing_large_pause_dark : R.drawable.ic_playing_large_pause);
        mBtnForwardInPlayingBar.setImageResource(mDarkMode ? R.drawable.ic_playing_large_forward_dark : R.drawable.ic_playing_large_forward);

        mImgViewDown.animate().alpha(1.0f).setDuration(lDuration);
        mSeekCurPos.animate().alpha(1.0f).setDuration(lDuration);
        mTextCurPos.animate().alpha(1.0f).setDuration(lDuration);
        mTextRemain.animate().alpha(1.0f).setDuration(lDuration);
        mBtnRewindInPlayingBar.animate().alpha(1.0f).setDuration(lDuration);
        mBtnMoreInPlayingBar.animate().alpha(1.0f).setDuration(lDuration);
        mBtnShuffleInPlayingBar.animate().alpha(1.0f).setDuration(lDuration);
        mBtnRepeatInPlayingBar.animate().alpha(1.0f).setDuration(lDuration);
        mBtnCloseInPlayingBar.animate().alpha(0.0f).setDuration(lDuration);
        mAdContainerView.animate().translationY(mTabLayout.getHeight() + mAdContainerView.getHeight() + 1).setDuration(lDuration);
        mTabLayout.animate().translationY(mTabLayout.getHeight() + mAdContainerView.getHeight()).setDuration(lDuration);
        mViewSep2.animate().translationY(mTabLayout.getHeight()).setDuration(lDuration);
    }

    public void downViewPlaying(final boolean bBottom) {
        final MainActivity activity = this;
        final long lDuration = 400;
        mRelativePlayingWithShadow.setBackgroundResource(mDarkMode ? R.drawable.topshadow_dark : R.drawable.topshadow);

        final RelativeLayout.LayoutParams paramRelativePlayingWithShadow = (RelativeLayout.LayoutParams) mRelativePlayingWithShadow.getLayoutParams();
        final RelativeLayout.LayoutParams paramArtwork = (RelativeLayout.LayoutParams) mBtnArtworkInPlayingBar.getLayoutParams();
        final RelativeLayout.LayoutParams paramTitle = (RelativeLayout.LayoutParams) mTextTitle.getLayoutParams();
        final RelativeLayout.LayoutParams paramArtist = (RelativeLayout.LayoutParams) mTextArtist.getLayoutParams();
        final RelativeLayout.LayoutParams paramBtnPlay = (RelativeLayout.LayoutParams) mBtnPlayInPlayingBar.getLayoutParams();
        final RelativeLayout.LayoutParams paramBtnForward = (RelativeLayout.LayoutParams) mBtnForwardInPlayingBar.getLayoutParams();

        final int nTranslationYFrom = (int)mRelativePlayingWithShadow.getTranslationY();
        final int nTranslationY = 0;
        int nHeight;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = this.getWindowManager().getCurrentWindowMetrics();
            Insets insets = windowMetrics.getWindowInsets()
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            int ScreenHeight = windowMetrics.getBounds().height();
            int StatusBar = insets.top;
            int NavigationBar = insets.bottom;
            nHeight = ScreenHeight - StatusBar - NavigationBar - mLinearControl.getHeight();
        } else {
            nHeight = getResources().getDisplayMetrics().heightPixels - mLinearControl.getHeight(); //  - safeInsetTop - mLinearControl.getHeight(); // (int) (22.0 * mDensity); // displaySize.y - safeInsetTop; // getResources().getDisplayMetrics().heightPixels + (int) (60.0 * mDensity) - mLinearControl.getHeight();
        }
        final int nRelativePlayingWithShadowHeightFrom = nHeight;
        int nTempRelativePlayingWithShadowHeight = (int) (82.0 * mDensity);
        if(bBottom) nTempRelativePlayingWithShadowHeight = 0;
        final int nRelativePlayingWithShadowHeight = nTempRelativePlayingWithShadowHeight;
        final int nRelativePlayingHeight = (int) (60.0 * mDensity);
        final int nArtworkWidthFrom = mBtnArtworkInPlayingBar.getWidth();
        final int nArtworkWidth = (int) (44.0 * mDensity);
        final int nArtworkLeftMarginFrom = paramArtwork.leftMargin;
        final int nArtworkLeftMargin = (int) (8.0 * mDensity);
        final int nArtworkTopMarginFrom = paramArtwork.topMargin;
        final int nArtworkTopMargin = (int) (8.0 * mDensity);
        final int nTitleTopMarginFrom = paramTitle.topMargin;
        final int nTitleLeftMarginFrom = paramTitle.leftMargin;
        final int nTitleRightMarginFrom = paramTitle.rightMargin;
        final float fTitleFontFrom = 15.0f;
        final float fTitleFont = 13.0f;
        Paint paint = new Paint();
        mTextTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fTitleFont);
        paint.setTextSize(mTextTitle.getTextSize());
        mTextTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fTitleFontFrom);
        final int nTitleWidthFrom = paramTitle.width;
        final int nTitleWidth = (int) paint.measureText(mTextTitle.getText().toString());
        final int nArtistTopMarginFrom = paramArtist.topMargin;
        final float fArtistFontFrom = 13.0f;
        final float fArtistFont = 10.0f;
        mTextArtist.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fArtistFont);
        paint.setTextSize(mTextArtist.getTextSize());
        mTextArtist.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fArtistFontFrom);
        final int nArtistWidthFrom = paramArtist.width;
        final int nArtistWidth = (int) paint.measureText(mTextArtist.getText().toString());
        final int nBtnPlayTopMarginFrom = paramBtnPlay.topMargin;
        final int nBtnPlayRightMarginFrom = paramBtnPlay.rightMargin;
        final int nBtnPlayRightMargin = (int) (88.0 * mDensity);
        final int nBtnHeightFrom = (int) (44.0 * mDensity);
        final int nBtnHeight = (int) (60.0 * mDensity);
        final int nBtnForwardRightMarginFrom = paramBtnForward.rightMargin;
        final int nBtnForwardRightMargin = (int) (44.0 * mDensity);

        ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fProgress = valueAnimator.getAnimatedFraction();
                advanceAnimation(mTabLayout, "bottomMargin", -mTabLayout.getHeight(), 0, fProgress);
                advanceAnimation(mViewSep2, "bottomMargin", mTabLayout.getHeight(), 0, fProgress);
                mRelativePlayingWithShadow.setTranslationY(nTranslationYFrom + (nTranslationY - nTranslationYFrom) * fProgress);
                advanceAnimation(mRelativePlayingWithShadow, "height", nRelativePlayingWithShadowHeightFrom, nRelativePlayingWithShadowHeight, fProgress);
                advanceAnimation(mRelativePlayingWithShadow, "bottomMargin", 0, mTabLayout.getHeight() + mAdContainerView.getHeight(), fProgress);
                advanceAnimation(mRelativePlaying, "height", nRelativePlayingWithShadowHeightFrom, nRelativePlayingHeight, fProgress);
                advanceAnimation(mBtnArtworkInPlayingBar, "width", nArtworkWidthFrom, nArtworkWidth, fProgress);
                advanceAnimation(mBtnArtworkInPlayingBar, "height", nArtworkWidthFrom, nArtworkWidth, fProgress);
                advanceAnimation(mBtnArtworkInPlayingBar, "leftMargin", nArtworkLeftMarginFrom, nArtworkLeftMargin, fProgress);
                advanceAnimation(mBtnArtworkInPlayingBar, "topMargin", nArtworkTopMarginFrom, nArtworkTopMargin, fProgress);
                advanceAnimation(mTextTitle, "width", nTitleWidthFrom, nTitleWidth, fProgress);
                advanceAnimation(mTextTitle, "topMargin", nTitleTopMarginFrom, (int) (14.0 * mDensity), fProgress);
                advanceAnimation(mTextTitle, "leftMargin", nTitleLeftMarginFrom, (int) (60.0 * mDensity), fProgress);
                advanceAnimation(mTextTitle, "rightMargin", nTitleRightMarginFrom, (int) (132.0 * mDensity), fProgress);
                mTextTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fTitleFontFrom + (fTitleFont - fTitleFontFrom) * fProgress);
                advanceAnimation(mTextArtist, "width", nArtistWidthFrom, nArtistWidth, fProgress);
                advanceAnimation(mTextArtist, "topMargin", nArtistTopMarginFrom, (int) (33.0 * mDensity), fProgress);
                advanceAnimation(mTextArtist, "leftMargin", nTitleLeftMarginFrom, (int) (60.0 * mDensity), fProgress);
                advanceAnimation(mTextArtist, "rightMargin", nTitleRightMarginFrom, (int) (132.0 * mDensity), fProgress);
                mTextArtist.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fArtistFontFrom + (fArtistFont - fArtistFontFrom) * fProgress);
                advanceAnimation(mBtnPlayInPlayingBar, "topMargin", nBtnPlayTopMarginFrom, 0, fProgress);
                advanceAnimation(mBtnForwardInPlayingBar, "topMargin", nBtnPlayTopMarginFrom, 0, fProgress);
                advanceAnimation(mBtnPlayInPlayingBar, "rightMargin", nBtnPlayRightMarginFrom, nBtnPlayRightMargin, fProgress);
                advanceAnimation(mBtnPlayInPlayingBar, "height", nBtnHeightFrom, nBtnHeight, fProgress);
                advanceAnimation(mBtnForwardInPlayingBar, "height", nBtnHeightFrom, nBtnHeight, fProgress);
                advanceAnimation(mBtnRewindInPlayingBar, "height", nBtnHeightFrom, nBtnHeight, fProgress);
                advanceAnimation(mBtnForwardInPlayingBar, "rightMargin", nBtnForwardRightMarginFrom, nBtnForwardRightMargin, fProgress);
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mTextTitle.setGravity(Gravity.START);
                paramTitle.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
                mTextArtist.setGravity(Gravity.START);
                paramArtist.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
                if(Build.VERSION.SDK_INT >= 17) {
                    paramTitle.addRule(RelativeLayout.ALIGN_PARENT_END, 1);
                    paramArtist.addRule(RelativeLayout.ALIGN_PARENT_END, 1);
                }
                mImgViewDown.clearAnimation();
                mImgViewDown.setVisibility(View.GONE);
                mSeekCurPos.clearAnimation();
                mSeekCurPos.setVisibility(View.GONE);
                mTextCurPos.clearAnimation();
                mTextCurPos.setVisibility(View.GONE);
                mTextRemain.clearAnimation();
                mTextRemain.setVisibility(View.GONE);
                mBtnRewindInPlayingBar.clearAnimation();
                mBtnRewindInPlayingBar.setVisibility(View.GONE);
                mBtnMoreInPlayingBar.clearAnimation();
                mBtnMoreInPlayingBar.setVisibility(View.GONE);
                mBtnShuffleInPlayingBar.clearAnimation();
                mBtnShuffleInPlayingBar.setVisibility(View.GONE);
                mBtnRepeatInPlayingBar.clearAnimation();
                mBtnRepeatInPlayingBar.setVisibility(View.GONE);
                mRelativePlaying.setOnClickListener(activity);
                mBtnArtworkInPlayingBar.setOnClickListener(null);
                mBtnArtworkInPlayingBar.setAnimation(false);
                mBtnArtworkInPlayingBar.setClickable(false);

                paramRelativePlayingWithShadow.addRule(RelativeLayout.ABOVE, R.id.ad_view_container);
                paramRelativePlayingWithShadow.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
                paramRelativePlayingWithShadow.bottomMargin = 0;
                RelativeLayout.LayoutParams paramContainer = (RelativeLayout.LayoutParams) mViewPager.getLayoutParams();
                RelativeLayout.LayoutParams paramRecording = (RelativeLayout.LayoutParams) mRelativeRecording.getLayoutParams();
                if (MainActivity.sRecord != 0) {
                    paramContainer.addRule(RelativeLayout.ABOVE, R.id.relativeRecording);
                    paramContainer.bottomMargin = 0;
                    paramRecording.addRule(RelativeLayout.ABOVE, R.id.relativePlayingWithShadow);
                    if(bBottom) paramRecording.bottomMargin = 0;
                    else paramRecording.bottomMargin = (int) (-22 * mDensity);
                } else {
                    paramContainer.addRule(RelativeLayout.ABOVE, R.id.relativePlayingWithShadow);
                    if(bBottom) paramContainer.bottomMargin = 0;
                    else paramContainer.bottomMargin = (int) (-22 * mDensity);
                }

                if(bBottom) {
                    mRelativePlayingWithShadow.setVisibility(View.GONE);
                    RelativeLayout.LayoutParams paramPlayingWithShadow = (RelativeLayout.LayoutParams) mRelativePlayingWithShadow.getLayoutParams();
                    paramPlayingWithShadow.height = (int) (82.0 * mDensity);
                    RelativeLayout.LayoutParams paramPlaying = (RelativeLayout.LayoutParams) mRelativePlaying.getLayoutParams();
                    paramPlaying.height = (int) (82.0 * mDensity);
                }
            }
        });
        anim.setDuration(lDuration).start();

        if (BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PLAYING)
            mBtnPlayInPlayingBar.setImageResource(mDarkMode ? R.drawable.ic_bar_button_play_dark : R.drawable.ic_bar_button_play);
        else mBtnPlayInPlayingBar.setImageResource(mDarkMode ? R.drawable.ic_bar_button_pause_dark : R.drawable.ic_bar_button_pause);
        mBtnForwardInPlayingBar.setImageResource(mDarkMode ? R.drawable.ic_bar_button_forward_dark : R.drawable.ic_bar_button_forward);

        mImgViewDown.animate().alpha(0.0f).setDuration(lDuration);
        mSeekCurPos.animate().alpha(0.0f).setDuration(lDuration);
        mTextCurPos.animate().alpha(0.0f).setDuration(lDuration);
        mTextRemain.animate().alpha(0.0f).setDuration(lDuration);
        mBtnRewindInPlayingBar.animate().alpha(0.0f).setDuration(lDuration);
        mBtnMoreInPlayingBar.animate().alpha(0.0f).setDuration(lDuration);
        mBtnShuffleInPlayingBar.animate().alpha(0.0f).setDuration(lDuration);
        mBtnRepeatInPlayingBar.animate().alpha(0.0f).setDuration(lDuration);
        mBtnCloseInPlayingBar.animate().alpha(1.0f).setDuration(lDuration);
        mAdContainerView.animate().translationY(0).setDuration(lDuration);
        mTabLayout.animate().translationY(0).setDuration(lDuration);
        mViewSep2.animate().translationY(0).setDuration(lDuration);
    }

    private void showSaveExportMenu() {
        final BottomMenu menu = new BottomMenu(this);
        menu.setTitle(getString(R.string.saveExport));
        menu.addMenu(getString(R.string.saveToApp), mDarkMode ? R.drawable.ic_actionsheet_save_dark : R.drawable.ic_actionsheet_save, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                playlistFragment.saveSongToLocal();
            }
        });
        if(Build.VERSION.SDK_INT >= 18) {
            menu.addMenu(getString(R.string.saveAsVideo), mDarkMode ? R.drawable.ic_actionsheet_film_dark : R.drawable.ic_actionsheet_film, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menu.dismiss();
                    playlistFragment.saveSongToGallery();
                }
            });
        }
        menu.addMenu(getString(R.string.export), mDarkMode ? R.drawable.ic_actionsheet_share_dark : R.drawable.ic_actionsheet_share, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.dismiss();
                playlistFragment.export();
            }
        });
        menu.setCancelMenu();
        menu.show();
    }

    private void openHideAds() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        transaction.replace(R.id.relativeMain, new HideAdsFragment());
        transaction.commit();
    }

    private void openItem() {
        SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("bPinkCamperDisplayed", true).apply();
        preferences.edit().putBoolean("bBlueCamperDisplayed", true).apply();
        preferences.edit().putBoolean("bOrangeCamperDisplayed", true).apply();
        FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        transaction.replace(R.id.relativeMain, new ItemFragment());
        transaction.commit();
    }

    public void openSetting() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        transaction.replace(R.id.relativeMain, new SettingFragment());
        transaction.commit();
    }

    public void open() {
        if (Build.VERSION.SDK_INT < 19) {
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            playlistFragment.startActivityForResult(intent, 1);
        }
        else {
            final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            playlistFragment.startActivityForResult(intent, 1);
        }
    }

    public void openGallery() {
        if (Build.VERSION.SDK_INT < 19) {
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            playlistFragment.startActivityForResult(intent, 2);
        }
        else {
            final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("video/*");
            playlistFragment.startActivityForResult(intent, 2);
        }
    }

    public void startBillingHideAds() {
        mPurchasingItem = ITEM_HIDE_ADS;
        launchPurchaseFlow(PRODUCT_ID_HIDE_ADS);
    }

    public void startBillingHideAdsMonthly() {
        mPurchasingItem = ITEM_HIDE_ADS_MONTHLY;
        launchPurchaseFlow(PRODUCT_ID_HIDE_ADS_MONTHLY);
    }

    private void hideAds(boolean updatePurchaseOnceButton, boolean updatePurchaseContinuousButton) {
        HideAdsFragment hideAdsFragment = null;
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f.getClass().getName().equals("com.edolfzoku.hayaemon2.HideAdsFragment"))
                hideAdsFragment = (HideAdsFragment)f;
        }
        if (hideAdsFragment != null) {
            if (updatePurchaseOnceButton) {
                Button btnPurchaseOnce = findViewById(R.id.btnPurchaseOnce);
                btnPurchaseOnce.setBackgroundResource(mDarkMode ? R.drawable.itempurchased_dark : R.drawable.itempurchased);
                btnPurchaseOnce.setTextColor(mDarkMode ? getResources().getColor(R.color.darkModeTextDarkGray) : Color.argb(255, 148, 148, 148));
                btnPurchaseOnce.setText(R.string.purchased);
                btnPurchaseOnce.setShadowLayer(0, 0, 0, Color.argb(0, 0, 0, 0));
                btnPurchaseOnce.setOnClickListener(null);
                Button btnRestoreOnce = findViewById(R.id.btnRestoreOnce);
                btnRestoreOnce.setVisibility(View.GONE);

                RelativeLayout relativePurchaseOnce = findViewById(R.id.relativePurchaseOnce);
                RelativeLayout.LayoutParams paramBtnPurchaseOnce = (RelativeLayout.LayoutParams) relativePurchaseOnce.getLayoutParams();
                paramBtnPurchaseOnce.bottomMargin = (int) (14 * getDensity());
                relativePurchaseOnce.setLayoutParams(paramBtnPurchaseOnce);
            }
            if (updatePurchaseContinuousButton) {
                Button btnPurchaseContinuous = findViewById(R.id.btnPurchaseContinuous);
                btnPurchaseContinuous.setBackgroundResource(mDarkMode ? R.drawable.itempurchased_dark : R.drawable.itempurchased);
                btnPurchaseContinuous.setTextColor(mDarkMode ? getResources().getColor(R.color.darkModeTextDarkGray) : Color.argb(255, 148, 148, 148));
                btnPurchaseContinuous.setText(R.string.purchased);
                btnPurchaseContinuous.setShadowLayer(0, 0, 0, Color.argb(0, 0, 0, 0));
                btnPurchaseContinuous.setOnClickListener(null);
                Button btnRestoreContinuous = findViewById(R.id.btnRestoreContinuous);
                btnRestoreContinuous.setVisibility(View.GONE);

                RelativeLayout relativePurchaseContinuous = findViewById(R.id.relativePurchaseContinuous);
                RelativeLayout.LayoutParams paramBtnPurchaseContinuous = (RelativeLayout.LayoutParams) relativePurchaseContinuous.getLayoutParams();
                paramBtnPurchaseContinuous.bottomMargin = (int) (14 * getDensity());
                relativePurchaseContinuous.setLayoutParams(paramBtnPurchaseContinuous);
            }
        }
        if(mAdContainerView.getVisibility() != View.GONE) {
            mAdContainerView.setVisibility(View.GONE);

            SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
            preferences.edit().putBoolean("hideads", mAdContainerView.getVisibility() == View.GONE).apply();

            if (mAdView != null) mAdView.destroy();
        }
    }

    public void startBillingPurpleSeaUrchinPointer() {
        mPurchasingItem = ITEM_PURPLE_SEA_URCHIN_POINTER;
        launchPurchaseFlow(PRODUCT_ID_PURPLE_SEA_URCHIN_POINTER);
    }

    private void buyPurpleSeaUrchinPointer(boolean ask) {
        ItemFragment itemFragment = null;
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f.getClass().getName().equals("com.edolfzoku.hayaemon2.ItemFragment"))
                    itemFragment = (ItemFragment)f;
        }
        if (itemFragment != null) {
            Button btnPurplePurchase = findViewById(R.id.btnPurplePurchase);
            btnPurplePurchase.setBackgroundResource(mDarkMode ? R.drawable.itempurchased_dark : R.drawable.itempurchased);
            btnPurplePurchase.setTextColor(mDarkMode ? getResources().getColor(R.color.darkModeTextDarkGray) : Color.argb(255, 148, 148, 148));
            btnPurplePurchase.setText(R.string.purchased);
            btnPurplePurchase.setShadowLayer(0, 0, 0, Color.argb(0,0, 0, 0));
            btnPurplePurchase.setOnClickListener(null);
            Button btnPurpleSet = findViewById(R.id.btnPurpleSet);
            btnPurpleSet.setVisibility(View.VISIBLE);
        }
        SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("unipointer_p", true).apply();

        if (ask) {
            AlertDialog.Builder builder;
            if (mDarkMode)
                builder = new AlertDialog.Builder(this, R.style.DarkModeDialog);
            else
                builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.purpleSeaUrchinPointer);
            builder.setMessage(R.string.askApply);
            builder.setPositiveButton(getString(R.string.Do), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ItemFragment itemFragment = null;
                    for (Fragment f : getSupportFragmentManager().getFragments()) {
                        if (f.getClass().getName().equals("com.edolfzoku.hayaemon2.ItemFragment"))
                            itemFragment = (ItemFragment)f;
                    }
                    if (itemFragment != null) itemFragment.close();
                    mViewPager.setCurrentItem(2);
                    SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                    ImageView imgPoint = findViewById(R.id.imgPoint);
                    imgPoint.setBackgroundResource(R.drawable.control_pointer_uni_murasaki);
                    imgPoint.setTag(1);
                    imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    preferences.edit().putInt("imgPointTag", 1).apply();
                }
            });
            builder.setNegativeButton(getString(R.string.NotYet), null);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    if (alertDialog.getWindow() != null) {
                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                        lp.dimAmount = 0.4f;
                        alertDialog.getWindow().setAttributes(lp);
                    }
                }
            });
            alertDialog.show();
        }
    }

    public void startBillingElegantSeaUrchinPointer() {
        mPurchasingItem = ITEM_ELEGANT_SEA_URCHIN_POINTER;
        launchPurchaseFlow(PRODUCT_ID_ELEGANT_SEA_URCHIN_POINTER);
    }

    private void buyElegantSeaUrchinPointer(boolean ask) {
        ItemFragment itemFragment = null;
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f.getClass().getName().equals("com.edolfzoku.hayaemon2.ItemFragment"))
                    itemFragment = (ItemFragment)f;
        }
        if (itemFragment != null) {
            Button btnElegantPurchase = findViewById(R.id.btnElegantPurchase);
            btnElegantPurchase.setBackgroundResource(mDarkMode ? R.drawable.itempurchased_dark : R.drawable.itempurchased);
            btnElegantPurchase.setTextColor(mDarkMode ? getResources().getColor(R.color.darkModeTextDarkGray) : Color.argb(255, 148, 148, 148));
            btnElegantPurchase.setText(R.string.purchased);
            btnElegantPurchase.setShadowLayer(0, 0, 0, Color.argb(0, 0, 0, 0));
            btnElegantPurchase.setOnClickListener(null);
            Button btnElegantSet = findViewById(R.id.btnElegantSet);
            btnElegantSet.setVisibility(View.VISIBLE);
        }
        SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("unipointer_e", true).apply();

        if (ask) {
            AlertDialog.Builder builder;
            if (mDarkMode)
                builder = new AlertDialog.Builder(this, R.style.DarkModeDialog);
            else
                builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.elegantSeaUrchinPointer);
            builder.setMessage(R.string.askApply);
            builder.setPositiveButton(getString(R.string.Do), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ItemFragment itemFragment = null;
                    for (Fragment f : getSupportFragmentManager().getFragments()) {
                        if (f.getClass().getName().equals("com.edolfzoku.hayaemon2.ItemFragment"))
                            itemFragment = (ItemFragment)f;
                    }
                    if (itemFragment != null) itemFragment.close();
                    mViewPager.setCurrentItem(2);
                    SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                    ImageView imgPoint = findViewById(R.id.imgPoint);
                    imgPoint.setBackgroundResource(R.drawable.control_pointer_uni_bafun);
                    imgPoint.setTag(2);
                    imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    preferences.edit().putInt("imgPointTag", 2).apply();
                }
            });
            builder.setNegativeButton(getString(R.string.NotYet), null);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    if (alertDialog.getWindow() != null) {
                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                        lp.dimAmount = 0.4f;
                        alertDialog.getWindow().setAttributes(lp);
                    }
                }
            });
            alertDialog.show();
        }
    }

    public void startBillingPinkCamperPointer() {
        mPurchasingItem = ITEM_PINK_CAMPER_POINTER;
        launchPurchaseFlow(PRODUCT_ID_PINK_CAMPER_POINTER);
    }

    private void buyPinkCamperPointer(boolean ask) {
        ItemFragment itemFragment = null;
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f.getClass().getName().equals("com.edolfzoku.hayaemon2.ItemFragment"))
                    itemFragment = (ItemFragment)f;
        }
        if (itemFragment != null) {
            Button btnPinkCamperPurchase = findViewById(R.id.btnPinkCamperPurchase);
            btnPinkCamperPurchase.setBackgroundResource(mDarkMode ? R.drawable.itempurchased_dark : R.drawable.itempurchased);
            btnPinkCamperPurchase.setTextColor(mDarkMode ? getResources().getColor(R.color.darkModeTextDarkGray) : Color.argb(255, 148, 148, 148));
            btnPinkCamperPurchase.setText(R.string.purchased);
            btnPinkCamperPurchase.setShadowLayer(0, 0, 0, Color.argb(0, 0, 0, 0));
            btnPinkCamperPurchase.setOnClickListener(null);
            Button btnPinkCamperSet = findViewById(R.id.btnPinkCamperSet);
            btnPinkCamperSet.setVisibility(View.VISIBLE);
        }
        SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("camperpointer_p", true).apply();

        if (ask) {
            AlertDialog.Builder builder;
            if (mDarkMode)
                builder = new AlertDialog.Builder(this, R.style.DarkModeDialog);
            else
                builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.pinkCamperPointer);
            builder.setMessage(R.string.askApply);
            builder.setPositiveButton(getString(R.string.Do), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ItemFragment itemFragment = null;
                    for (Fragment f : getSupportFragmentManager().getFragments()) {
                        if (f.getClass().getName().equals("com.edolfzoku.hayaemon2.ItemFragment"))
                            itemFragment = (ItemFragment)f;
                    }
                    if (itemFragment != null) itemFragment.close();
                    mViewPager.setCurrentItem(2);
                    SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                    ImageView imgPoint = findViewById(R.id.imgPoint);
                    imgPoint.setBackgroundResource(mDarkMode ? R.drawable.control_pointer_camper_pk_dark : R.drawable.control_pointer_camper_pk);
                    imgPoint.setTag(3);
                    imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    preferences.edit().putInt("imgPointTag", 3).apply();
                    AnimationDrawable anime = (AnimationDrawable) imgPoint.getBackground();
                    anime.start();
                }
            });
            builder.setNegativeButton(getString(R.string.NotYet), null);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    if (alertDialog.getWindow() != null) {
                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                        lp.dimAmount = 0.4f;
                        alertDialog.getWindow().setAttributes(lp);
                    }
                }
            });
            alertDialog.show();
        }
    }

    public void startBillingBlueCamperPointer() {
        mPurchasingItem = ITEM_BLUE_CAMPER_POINTER;
        launchPurchaseFlow(PRODUCT_ID_BLUE_CAMPER_POINTER);
    }

    private void buyBlueCamperPointer(boolean ask) {
        ItemFragment itemFragment = null;
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f.getClass().getName().equals("com.edolfzoku.hayaemon2.ItemFragment"))
                    itemFragment = (ItemFragment)f;
        }
        if (itemFragment != null) {
            Button btnBlueCamperPurchase = findViewById(R.id.btnBlueCamperPurchase);
            btnBlueCamperPurchase.setBackgroundResource(mDarkMode ? R.drawable.itempurchased_dark : R.drawable.itempurchased);
            btnBlueCamperPurchase.setTextColor(mDarkMode ? getResources().getColor(R.color.darkModeTextDarkGray) : Color.argb(255, 148, 148, 148));
            btnBlueCamperPurchase.setText(R.string.purchased);
            btnBlueCamperPurchase.setShadowLayer(0, 0, 0, Color.argb(0, 0, 0, 0));
            btnBlueCamperPurchase.setOnClickListener(null);
            Button btnBlueCamperSet = findViewById(R.id.btnBlueCamperSet);
            btnBlueCamperSet.setVisibility(View.VISIBLE);
        }
        SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("camperpointer_b", true).apply();

        if (ask) {
            AlertDialog.Builder builder;
            if (mDarkMode)
                builder = new AlertDialog.Builder(this, R.style.DarkModeDialog);
            else
                builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.blueCamperPointer);
            builder.setMessage(R.string.askApply);
            builder.setPositiveButton(getString(R.string.Do), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ItemFragment itemFragment = null;
                    for (Fragment f : getSupportFragmentManager().getFragments()) {
                        if (f.getClass().getName().equals("com.edolfzoku.hayaemon2.ItemFragment"))
                            itemFragment = (ItemFragment)f;
                    }
                    if (itemFragment != null) itemFragment.close();
                    mViewPager.setCurrentItem(2);
                    SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                    ImageView imgPoint = findViewById(R.id.imgPoint);
                    imgPoint.setBackgroundResource(mDarkMode ? R.drawable.control_pointer_camper_bl_dark : R.drawable.control_pointer_camper_bl);
                    imgPoint.setTag(4);
                    imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    preferences.edit().putInt("imgPointTag", 4).apply();
                    AnimationDrawable anime = (AnimationDrawable) imgPoint.getBackground();
                    anime.start();
                }
            });
            builder.setNegativeButton(getString(R.string.NotYet), null);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    if (alertDialog.getWindow() != null) {
                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                        lp.dimAmount = 0.4f;
                        alertDialog.getWindow().setAttributes(lp);
                    }
                }
            });
            alertDialog.show();
        }
    }

    public void startBillingOrangeCamperPointer() {
        mPurchasingItem = ITEM_ORANGE_CAMPER_POINTER;
        launchPurchaseFlow(PRODUCT_ID_ORANGE_CAMPER_POINTER);
    }

    private void buyOrangeCamperPointer(boolean ask) {
        ItemFragment itemFragment = null;
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f.getClass().getName().equals("com.edolfzoku.hayaemon2.ItemFragment"))
                    itemFragment = (ItemFragment)f;
        }
        if (itemFragment != null) {
            Button btnOrangeCamperPurchase = findViewById(R.id.btnOrangeCamperPurchase);
            btnOrangeCamperPurchase.setBackgroundResource(mDarkMode ? R.drawable.itempurchased_dark : R.drawable.itempurchased);
            btnOrangeCamperPurchase.setTextColor(mDarkMode ? getResources().getColor(R.color.darkModeTextDarkGray) : Color.argb(255, 148, 148, 148));
            btnOrangeCamperPurchase.setText(R.string.purchased);
            btnOrangeCamperPurchase.setShadowLayer(0, 0, 0, Color.argb(0, 0, 0, 0));
            btnOrangeCamperPurchase.setOnClickListener(null);
            Button btnOrangeCamperSet = findViewById(R.id.btnOrangeCamperSet);
            btnOrangeCamperSet.setVisibility(View.VISIBLE);
        }
        SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("camperpointer_o", true).apply();

        if (ask) {
            AlertDialog.Builder builder;
            if (mDarkMode)
                builder = new AlertDialog.Builder(this, R.style.DarkModeDialog);
            else
                builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.orangeCamperPointer);
            builder.setMessage(R.string.askApply);
            builder.setPositiveButton(getString(R.string.Do), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ItemFragment itemFragment = null;
                    for (Fragment f : getSupportFragmentManager().getFragments()) {
                        if (f.getClass().getName().equals("com.edolfzoku.hayaemon2.ItemFragment"))
                            itemFragment = (ItemFragment)f;
                    }
                    if (itemFragment != null) itemFragment.close();
                    mViewPager.setCurrentItem(2);
                    SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
                    ImageView imgPoint = findViewById(R.id.imgPoint);
                    imgPoint.setBackgroundResource(mDarkMode ? R.drawable.control_pointer_camper_or_dark : R.drawable.control_pointer_camper_or);
                    imgPoint.setTag(5);
                    imgPoint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    imgPoint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    preferences.edit().putInt("imgPointTag", 5).apply();
                    AnimationDrawable anime = (AnimationDrawable) imgPoint.getBackground();
                    anime.start();
                }
            });
            builder.setNegativeButton(getString(R.string.NotYet), null);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    if (alertDialog.getWindow() != null) {
                        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                        lp.dimAmount = 0.4f;
                        alertDialog.getWindow().setAttributes(lp);
                    }
                }
            });
            alertDialog.show();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initialize(Bundle savedInstanceState) {
        if(sStream == 0) {
            BASS.BASS_SetConfig(BASS.BASS_CONFIG_ANDROID_AAUDIO, 0);
            BASS.BASS_Init(-1, 44100, 0);
            BASS.BASS_SetConfig(BASS.BASS_CONFIG_FLOATDSP, 1);
            BASS.BASS_SetConfig(BASS_CONFIG_AAC_MP4, 1);

            BASS.BASS_PluginLoad("libbass_aac.so", 0);
            BASS.BASS_PluginLoad("libbassflac.so", 0);
        }

        SectionsPagerAdapter sectionsPagerAdapter;
        if(savedInstanceState == null) {
            sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
            playlistFragment = (PlaylistFragment)sectionsPagerAdapter.getItem(0);
            loopFragment = (LoopFragment)sectionsPagerAdapter.getItem(1);
            controlFragment = (ControlFragment)sectionsPagerAdapter.getItem(2);
            equalizerFragment = (EqualizerFragment)sectionsPagerAdapter.getItem(3);
            effectFragment = (EffectFragment)sectionsPagerAdapter.getItem(4);
        }
        else {
            playlistFragment = (PlaylistFragment)getSupportFragmentManager().getFragment(savedInstanceState, "playlistFragment");
            loopFragment = (LoopFragment)getSupportFragmentManager().getFragment(savedInstanceState, "loopFragment");
            controlFragment = (ControlFragment)getSupportFragmentManager().getFragment(savedInstanceState, "controlFragment");
            equalizerFragment = (EqualizerFragment)getSupportFragmentManager().getFragment(savedInstanceState, "equalizerFragment");
            effectFragment = (EffectFragment)getSupportFragmentManager().getFragment(savedInstanceState, "effectFragment");
            sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        }

        mViewPager = findViewById(R.id.container);
        mViewPager.setSwipeHold(true);
        mViewPager.setAdapter(sectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override
            public void onPageSelected(int position) {
                if(position == 0 && findViewById(R.id.relativeSongs).getVisibility() == View.VISIBLE) mViewSep1.setVisibility(View.INVISIBLE);
                else mViewSep1.setVisibility(View.VISIBLE);
                if(position == 1) loopFragment.updateCurPos();
                for(int i = 0; i < 5; i++) {
                    TabLayout.Tab tab = mTabLayout.getTabAt(i);
                    if(tab == null) continue;
                    TextView textView = (TextView)tab.getCustomView();
                    if(textView == null) continue;
                    if(i == position) {
                        int color = getResources().getColor(mDarkMode ? R.color.darkModeBlue : R.color.lightModeBlue);
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
            public void onPageScrollStateChanged(int state) { }
        });
        mTabLayout.setupWithViewPager(mViewPager);

        ViewGroup vg = findViewById(R.id.layout_root);
        TextView textView0 = (TextView) LayoutInflater.from(this).inflate(R.layout.tab, vg);
        textView0.setText(R.string.playlist);
        textView0.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_playlist, 0, 0);
        int color = Color.parseColor("#FF007AFF");
        textView0.setTextColor(color);
        for (Drawable drawable : textView0.getCompoundDrawables()) {
            if (drawable != null)
                drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        }
        TabLayout.Tab tab0 = mTabLayout.getTabAt(0);
        if(tab0 != null) tab0.setCustomView(textView0);

        TextView textView1 = (TextView) LayoutInflater.from(this).inflate(R.layout.tab, vg);
        textView1.setText(R.string.loop);
        textView1.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_abloop, 0, 0);
        color = Color.parseColor("#FF808080");
        textView1.setTextColor(color);
        TabLayout.Tab tab1 = mTabLayout.getTabAt(1);
        if(tab1 != null) tab1.setCustomView(textView1);

        TextView textView2 = (TextView) LayoutInflater.from(this).inflate(R.layout.tab, vg);
        textView2.setText(R.string.control);
        textView2.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_control, 0, 0);
        textView2.setTextColor(color);
        TabLayout.Tab tab2 = mTabLayout.getTabAt(2);
        if(tab2 != null) tab2.setCustomView(textView2);

        TextView textView3 = (TextView) LayoutInflater.from(this).inflate(R.layout.tab, vg);
        textView3.setText(R.string.equalizer);
        textView3.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_equalizer, 0, 0);
        textView3.setTextColor(color);
        TabLayout.Tab tab3 = mTabLayout.getTabAt(3);
        if(tab3 != null) tab3.setCustomView(textView3);

        TextView textView4 = (TextView) LayoutInflater.from(this).inflate(R.layout.tab, vg);
        textView4.setText(R.string.effect);
        textView4.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_effect, 0, 0);
        textView4.setTextColor(color);
        TabLayout.Tab tab4 = mTabLayout.getTabAt(4);
        if(tab4 != null) tab4.setCustomView(textView4);

        AnimationButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(this);

        mBtnRewind.setOnLongClickListener(this);
        mBtnRewind.setOnTouchListener(this);

        mBtnForward.setOnLongClickListener(this);
        mBtnForward.setOnTouchListener(this);

        ScrollView scrollMenu = findViewById(R.id.scrollMenu);
        scrollMenu.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    int color = getResources().getColor(mDarkMode ? R.color.darkModeBk : R.color.lightModeBk);
                    findViewById(R.id.relativeSave).setBackgroundColor(color);
                    findViewById(R.id.relativeLock).setBackgroundColor(color);
                    findViewById(R.id.relativeNotice).setBackgroundColor(color);
                    findViewById(R.id.relativeAddSong).setBackgroundColor(color);
                    findViewById(R.id.relativeHideAds).setBackgroundColor(color);
                    findViewById(R.id.relativeItem).setBackgroundColor(color);
                    findViewById(R.id.relativeHelp).setBackgroundColor(color);
                    findViewById(R.id.relativeInquiry).setBackgroundColor(color);
                    findViewById(R.id.relativeReview).setBackgroundColor(color);
                    findViewById(R.id.relativeInfo).setBackgroundColor(color);
                }
                return false;
            }
        });
    }

    private boolean isAdsVisible() {
        return (mAdContainerView.getVisibility() != View.GONE);
    }

    public static void setSync() {
        if(sSync != 0) {
            BASS.BASS_ChannelRemoveSync(sStream, sSync);
            sSync = 0;
        }

        if(EffectFragment.isReverse()) {
            if(LoopFragment.sABLoop && sLoopA) // ABループ中でA位置が設定されている
                sSync = BASS.BASS_ChannelSetSync(sStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sStream, sLoopAPos), EndSync, null);
            else if(!LoopFragment.sABLoop && LoopFragment.sMarkerPlay) // マーカー再生中
                sSync = BASS.BASS_ChannelSetSync(sStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sStream, LoopFragment.getMarkerDstPos()), EndSync, null);
            else
                sSync = BASS.BASS_ChannelSetSync(sStream, BASS.BASS_SYNC_END, 0, EndSync, null);
        }
        else {
            double sLength = BASS.BASS_ChannelBytes2Seconds(sStream, BASS.BASS_ChannelGetLength(sStream, BASS.BASS_POS_BYTE));
            if(LoopFragment.sABLoop && sLoopB) // ABループ中でB位置が設定されている
                sSync = BASS.BASS_ChannelSetSync(sStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sStream, sLoopBPos), EndSync, null);
            else if(!LoopFragment.sABLoop && LoopFragment.sMarkerPlay) // マーカー再生中
                sSync = BASS.BASS_ChannelSetSync(sStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sStream, LoopFragment.getMarkerDstPos()), EndSync, null);
            else
                sSync = BASS.BASS_ChannelSetSync(sStream, BASS.BASS_SYNC_POS, BASS.BASS_ChannelSeconds2Bytes(sStream, sLength - 0.75), EndSync, null);
        }
    }

    public static void onEnded(final boolean bForce) {
        if (LoopFragment.sABLoop && (sLoopA || sLoopB)) {
            boolean bRepeatSingle = sRepeat == 2;
            if (!bRepeatSingle && sPlayNextByBPos) {
                Runnable timer = new Runnable() {
                    public void run() {
                        boolean bSingle = sShuffle == 2;

                        if (bSingle)
                            PlaylistFragment.playNext(false);
                        else if (bRepeatSingle)
                            BASS.BASS_ChannelPlay(sStream, true);
                        else
                            PlaylistFragment.playNext(true);
                    }
                };
                sHandler.postDelayed(timer, 0);
            } else {
                if (EffectFragment.isReverse())
                    BASS.BASS_ChannelSetPosition(sStream, BASS.BASS_ChannelSeconds2Bytes(sStream, sLoopBPos), BASS.BASS_POS_BYTE);
                else
                    BASS.BASS_ChannelSetPosition(sStream, BASS.BASS_ChannelSeconds2Bytes(sStream, sLoopAPos), BASS.BASS_POS_BYTE);
                setSync();
                if (BASS.BASS_ChannelIsActive(sStream) != BASS.BASS_ACTIVE_PLAYING)
                    BASS.BASS_ChannelPlay(sStream, false);
            }
        } else if (!LoopFragment.sABLoop && LoopFragment.sMarkerPlay) {
            BASS.BASS_ChannelSetPosition(sStream, BASS.BASS_ChannelSeconds2Bytes(sStream, LoopFragment.getMarkerSrcPos()), BASS.BASS_POS_BYTE);
            setSync();
            if (BASS.BASS_ChannelIsActive(sStream) != BASS.BASS_ACTIVE_PLAYING)
                BASS.BASS_ChannelPlay(sStream, false);
        } else {
            sWaitEnd = true;
            Runnable timer = new Runnable() {
                public void run() {
                    if (!bForce && BASS.BASS_ChannelIsActive(sStream) == BASS.BASS_ACTIVE_PLAYING) {
                        if(sWaitEnd) {
                            sHandler.postDelayed(this, 100);
                            return;
                        }
                    }
                    sWaitEnd = false;

                    boolean bSingle = sShuffle == 2;
                    boolean bRepeatSingle = sRepeat == 2;

                    if (bSingle)
                        PlaylistFragment.playNext(false);
                    else if (bRepeatSingle)
                        BASS.BASS_ChannelPlay(sStream, true);
                    else
                        PlaylistFragment.playNext(true);
                }
            };
            sHandler.postDelayed(timer, 0);
        }

        if(EffectFragment.sLoopEffectDetail) {
            if (EffectFragment.sEffectDetail == EffectFragment.EFFECTTYPE_INCREASESPEED) {
                BASS.FloatValue speed = new BASS.FloatValue();
                BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO, speed);
                speed.value += EffectFragment.sIncreaseSpeedLoop;
                if (speed.value + 100.0f > 400.0f) speed.value = 300.0f;
                if (MainActivity.sStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PAUSED)
                    ControlFragment.setSpeed(speed.value, false);
            } else if (EffectFragment.sEffectDetail == EffectFragment.EFFECTTYPE_DECREASESPEED) {
                BASS.FloatValue speed = new BASS.FloatValue();
                BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO, speed);
                speed.value -= EffectFragment.sDecreaseSpeedLoop;
                if (speed.value + 100.0f < 10.0f) speed.value = -90.0f;
                if (MainActivity.sStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PAUSED)
                    ControlFragment.setSpeed(speed.value, false);
            } else if (EffectFragment.sEffectDetail == EffectFragment.EFFECTTYPE_RAISEPITCH) {
                BASS.FloatValue pitch = new BASS.FloatValue();
                BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO_PITCH, pitch);
                pitch.value += EffectFragment.sRaisePitchLoop;
                if (pitch.value + 10.0f > 70.0f) pitch.value = 60.0f;
                if (MainActivity.sStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PAUSED)
                    ControlFragment.setPitch(pitch.value, false);
            } else if (EffectFragment.sEffectDetail == EffectFragment.EFFECTTYPE_LOWERPITCH) {
                BASS.FloatValue pitch = new BASS.FloatValue();
                BASS.BASS_ChannelGetAttribute(MainActivity.sStream, BASS_FX.BASS_ATTRIB_TEMPO_PITCH, pitch);
                pitch.value -= EffectFragment.sLowerPitchLoop;
                if (pitch.value + 70.0f < 10.0f) pitch.value = -60.0f;
                if (MainActivity.sStream != 0 && BASS.BASS_ChannelIsActive(MainActivity.sStream) != BASS.BASS_ACTIVE_PAUSED)
                    ControlFragment.setPitch(pitch.value, false);
            }
        }
    }

    private static final BASS.SYNCPROC EndSync = new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, Object user) {
            MainActivity.onEnded(false);
        }
    };

    public static void clearLoop() {
        clearLoop(true);
    }

    public static void clearLoop(boolean bSave) {
        sLoopAPos = 0.0;
        sLoopA = false;
        sLoopBPos = 0.0;
        sLoopB = false;
        LoopFragment.clearLoop(bSave);
    }

    @Override
    public void onDestroy() {
        // BASS.BASS_Free();

        // stopNotification();
        sActivity = null;
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
            ItemFragment itemFragment = null;
            SettingFragment settingFragment = null;
            SpeedRangeSettingFragment speedRangeSettingFragment = null;
            PitchRangeSettingFragment pitchRangeSettingFragment = null;
            for (Fragment f : getSupportFragmentManager().getFragments()) {
                switch(f.getClass().getName())
                {
                    case "com.edolfzoku.hayaemon2.ItemFragment":
                        itemFragment = (ItemFragment)f;
                        break;
                    case "com.edolfzoku.hayaemon2.SettingFragment":
                        settingFragment = (SettingFragment)f;
                        break;
                    case "com.edolfzoku.hayaemon2.SpeedRangeSettingFragment":
                        speedRangeSettingFragment = (SpeedRangeSettingFragment)f;
                        break;
                    case "com.edolfzoku.hayaemon2.PitchRangeSettingFragment":
                        pitchRangeSettingFragment = (PitchRangeSettingFragment)f;
                        break;
                }
            }
            if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
            else if(mSeekCurPos.getVisibility() == View.VISIBLE) {
                downViewPlaying(false);
                return true;
            }
            else if(itemFragment != null) { // 課金アイテム画面
                findViewById(R.id.btnCloseItem).performClick();
                return true;
            }
            else if(settingFragment != null) { // オプション設定画面
                findViewById(R.id.btnCloseSetting).performClick();
                return true;
            }
            else if(speedRangeSettingFragment != null) { // 速度の表示範囲画面
                findViewById(R.id.btnReturnSpeedRangeSetting).performClick();
                return true;
            }
            else if(pitchRangeSettingFragment != null) { // 音程の表示範囲画面
                findViewById(R.id.btnReturnPitchRangeSetting).performClick();
                return true;
            }
            else if(mTabLayout.getSelectedTabPosition() == 0) { // 再生リスト画面
                if(findViewById(R.id.relativeLyrics).getVisibility() == View.VISIBLE) { // 歌詞表示画面
                    findViewById(R.id.btnFinishLyrics).performClick();
                    return true;
                }
                else if(findViewById(R.id.relativePlaylists).getVisibility() == View.VISIBLE) { // 再生リスト整理画面
                    if(playlistFragment.isSorting()) { // 並べ替え中
                        findViewById(R.id.btnSortPlaylist).performClick();
                        return true;
                    }
                    else { // 通常の再生リスト整理画面
                        playlistFragment.onPlaylistItemClick(sSelectedPlaylist);
                        return true;
                    }
                }
                else if(playlistFragment.isMultiSelecting()) { // 複数選択モード中
                    playlistFragment.finishMultipleSelection();
                    return true;
                }
                else if(playlistFragment.isSorting()) { // 並べ替え中
                    findViewById(R.id.textFinishSort).performClick();
                    return true;
                }
            }
            else if(mTabLayout.getSelectedTabPosition() == 1) { // ループ画面
                mViewPager.setCurrentItem(0);
                return true;
            }
            else if(mTabLayout.getSelectedTabPosition() == 2) { // コントロール画面
                mViewPager.setCurrentItem(0);
                return true;
            }
            else if(mTabLayout.getSelectedTabPosition() == 3) { // イコライザ画面
                if(findViewById(R.id.scrollCustomEqualizer).getVisibility() == View.VISIBLE) // カスタマイズ画面
                    findViewById(R.id.btnBackCustomize).performClick();
                else mViewPager.setCurrentItem(0);
                return true;
            }
            else if(mTabLayout.getSelectedTabPosition() == 4) { // エフェクト画面
                if(findViewById(R.id.relativeEffectDetail).getVisibility() == View.VISIBLE) { // エフェクト詳細画面
                    findViewById(R.id.btnEffectBack).performClick();
                    return true;
                }
                else { // 通常のエフェクト画面
                    mViewPager.setCurrentItem(0);
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void sendAccessibilityEvent(String text, View source) {
        AccessibilityManager manager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        if(manager == null || !manager.isEnabled()) return;
        int nEventType;
        if (Build.VERSION.SDK_INT < 16) nEventType = AccessibilityEvent.TYPE_VIEW_FOCUSED;
        else nEventType = AccessibilityEventCompat.TYPE_ANNOUNCEMENT;
        AccessibilityEvent event = AccessibilityEvent.obtain(nEventType);
        event.setClassName(getClass().getName());
        event.getText().add(text);
        event.setSource(source);
        manager.sendAccessibilityEvent(event);
    }

    public static final BASS.BASS_FILEPROCS fileProcs = new BASS.BASS_FILEPROCS() {
        @Override
        public boolean FILESEEKPROC(long offset, Object user) {
            FileProcsParams params = (FileProcsParams)user;
            FileChannel fileChannel = params.fileChannel;
            if(fileChannel != null) {
                try {
                    fileChannel.position(offset);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        public int FILEREADPROC(ByteBuffer buffer, int length, Object user) {
            FileProcsParams params = (FileProcsParams)user;
            FileChannel fileChannel = params.fileChannel;
            InputStream inputStream = params.inputStream;
            if(fileChannel != null) {
                try {
                    return fileChannel.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                if(length == 0) return 0;
                byte[] b = new byte[length];
                int r;
                try {
                    r = inputStream.read(b);
                }
                catch (Exception e) {
                    return 0;
                }
                if (r <= 0) return 0;
                buffer.put(b, 0, r);
                return r;
            }
            return 0;
        }

        @Override
        public long FILELENPROC(Object user) {
            FileProcsParams params = (FileProcsParams)user;
            FileChannel fileChannel = params.fileChannel;
            if(fileChannel != null) {
                try {
                    return fileChannel.size();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return 0;
        }

        @Override
        public void FILECLOSEPROC(Object user) {
            FileProcsParams params = (FileProcsParams)user;
            AssetFileDescriptor assetFileDescriptor = params.assetFileDescriptor;
            FileChannel fileChannel = params.fileChannel;
            InputStream inputStream = params.inputStream;
            try {
                if(fileChannel != null) fileChannel.close();
                if(assetFileDescriptor != null) assetFileDescriptor.close();
                if(inputStream != null) inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public void setLightMode() {
        SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("DarkMode", false).apply();

        mDarkMode = false;

        if (Build.VERSION.SDK_INT >= 23) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        final RelativeLayout relativeMain = findViewById(R.id.relativeMain);
        final ArgbEvaluator eval = new ArgbEvaluator();
        final int nLightModeBk = getResources().getColor(R.color.lightModeBk);
        final int nDarkModeBk = getResources().getColor(R.color.darkModeBk);
        final int nDarkModeLightBk = getResources().getColor(R.color.darkModeLightBk);
        final int nLightModeText = getResources().getColor(android.R.color.black);
        final int nDarkModeText = getResources().getColor(android.R.color.white);
        final int nLightModeSep = getResources().getColor(R.color.lightModeSep);
        final int nDarkModeSep = getResources().getColor(R.color.darkModeSep);
        final int nLightModeBlue = getResources().getColor(R.color.lightModeBlue);
        final int nDarkModeBlue = getResources().getColor(R.color.darkModeBlue);
        final TabLayout.Tab tab = mTabLayout.getTabAt(mTabLayout.getSelectedTabPosition());
        final TextView textView = (tab == null) ? null : (TextView) tab.getCustomView();
        int nColorBefore;
        if(mTextArtist.getText() == null || mTextArtist.getText().equals(""))
            nColorBefore = Color.argb(255, 147, 156, 160);
        else nColorBefore = getResources().getColor(R.color.lightModeGray);
        int nColorAfter;
        if(mTextArtist.getText() == null || mTextArtist.getText().equals(""))
            nColorAfter = getResources().getColor(R.color.darkModeTextDarkGray);
        else nColorAfter = getResources().getColor(R.color.darkModeGray);
        final int nLightModeArtist = nColorBefore;
        final int nDarkModeArtist = nColorAfter;
        final int nLightModeTextDarkGray = getResources().getColor(R.color.lightModeTextDarkGray);
        final int nDarkModeTextDarkGray = getResources().getColor(R.color.darkModeTextDarkGray);

        ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fProgress = valueAnimator.getAnimatedFraction();
                int nColorModeBk = (Integer)eval.evaluate(fProgress, nDarkModeBk, nLightModeBk);
                int nColorModePlaying = (Integer) eval.evaluate(fProgress, nDarkModeLightBk, nLightModeBk);
                int nColorModeSep = (Integer)eval.evaluate(fProgress, nDarkModeSep, nLightModeSep);
                int nColorModeBlue = (Integer)eval.evaluate(fProgress, nDarkModeBlue, nLightModeBlue);
                int nColorModeText = (Integer)eval.evaluate(fProgress, nDarkModeText, nLightModeText);
                int nColorModeArtist = (Integer)eval.evaluate(fProgress, nDarkModeArtist, nLightModeArtist);
                int nColorModeTextDarkGray = (Integer)eval.evaluate(fProgress, nDarkModeTextDarkGray, nLightModeTextDarkGray);
                int nColorRecording = (Integer) eval.evaluate(fProgress, nDarkModeBk, Color.WHITE);
                if (Build.VERSION.SDK_INT >= 23) getWindow().setStatusBarColor(nColorModeBk);
                relativeMain.setBackgroundColor(nColorModeBk);
                mTabLayout.setBackgroundColor(nColorModeBk);
                mTabLayout.setSelectedTabIndicatorColor(nColorModeBk);
                mAdContainerView.setBackgroundColor(nColorModeBk);
                mRelativeLeftMenu.setBackgroundColor(nColorModeBk);
                mRelativeSave.setBackgroundColor(nColorModeBk);
                mRelativeLock.setBackgroundColor(nColorModeBk);
                mRelativeNotice.setBackgroundColor(nColorModeBk);
                mRelativeAddSong.setBackgroundColor(nColorModeBk);
                mRelativeHideAds.setBackgroundColor(nColorModeBk);
                mRelativeItem.setBackgroundColor(nColorModeBk);
                mRelativeHelp.setBackgroundColor(nColorModeBk);
                mRelativeInquiry.setBackgroundColor(nColorModeBk);
                mRelativeReview.setBackgroundColor(nColorModeBk);
                mRelativeInfo.setBackgroundColor(nColorModeBk);
                mTextSave.setTextColor(nColorModeText);
                mTextLock.setTextColor(nColorModeText);
                mTextHideAds.setTextColor(nColorModeText);
                mTextItemInMenu.setTextColor(nColorModeText);
                mTextHelp.setTextColor(nColorModeText);
                mTextInquiry.setTextColor(nColorModeText);
                mTextReview.setTextColor(nColorModeText);
                mTextInfo.setTextColor(nColorModeText);
                mTextNotice.setTextColor(nColorModeText);
                mTextAddSong.setTextColor(nColorModeText);
                mTextTitle.setTextColor(nColorModeText);
                mTextTitleInMenu.setTextColor(nColorModeText);
                mTextArtist.setTextColor(nColorModeArtist);
                mTextArtistInMenu.setTextColor(nColorModeArtist);
                mTextCurPos.setTextColor(nColorModeTextDarkGray);
                mTextRemain.setTextColor(nColorModeTextDarkGray);
                mViewSep0.setBackgroundColor(nColorModeSep);
                mViewSep1.setBackgroundColor(nColorModeSep);
                mViewSep2.setBackgroundColor(nColorModeSep);
                mViewSep3.setBackgroundColor(nColorModeSep);
                mDividerMenu.setBackgroundColor(nColorModeSep);
                mRelativeRecording.setBackgroundColor(nColorModeBlue);
                mTextRecording.setTextColor(nColorRecording);
                mTextRecordingTime.setTextColor(nColorRecording);
                if(textView != null) {
                    textView.setTextColor(nColorModeBlue);
                    for (Drawable drawable : textView.getCompoundDrawables()) {
                        if (drawable != null)
                            drawable.setColorFilter(new PorterDuffColorFilter(nColorModeBlue, PorterDuff.Mode.SRC_IN));
                    }
                }
            }
        });

        TransitionDrawable tdBtnMenu;
        if (needsToDisplayDeadline(true, false)) {
            tdBtnMenu = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.ic_bar_button_menu_notice_dark), getResources().getDrawable(R.drawable.ic_bar_button_menu_notice) });
        } else {
            tdBtnMenu = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.ic_bar_button_menu_dark), getResources().getDrawable(R.drawable.ic_bar_button_menu) });
        }
        TransitionDrawable tdBtnMoreInPlayingBar = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.ic_playing_large_more_dark), getResources().getDrawable(R.drawable.ic_playing_large_more) });
        TransitionDrawable tdBtnRewind = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.ic_bar_button_rewind_dark), getResources().getDrawable(R.drawable.ic_bar_button_rewind) });
        TransitionDrawable tdBtnPlay, tdBtnPlayInPlayingBar;
        if(BASS.BASS_ChannelIsActive(sStream) != BASS.BASS_ACTIVE_PLAYING) {
            tdBtnPlay = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_play_dark), getResources().getDrawable(R.drawable.ic_bar_button_play)});
            if(mSeekCurPos.getVisibility() == View.VISIBLE)
                tdBtnPlayInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_playing_large_play_dark), getResources().getDrawable(R.drawable.ic_playing_large_play)});
            else
                tdBtnPlayInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_play_dark), getResources().getDrawable(R.drawable.ic_bar_button_play)});
        }
        else {
            tdBtnPlay = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_pause_dark), getResources().getDrawable(R.drawable.ic_bar_button_pause)});
            if(mSeekCurPos.getVisibility() == View.VISIBLE)
                tdBtnPlayInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_playing_large_pause_dark), getResources().getDrawable(R.drawable.ic_playing_large_pause)});
            else
                tdBtnPlayInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_pause_dark), getResources().getDrawable(R.drawable.ic_bar_button_pause)});
        }
        TransitionDrawable tdBtnForward = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.ic_bar_button_forward_dark), getResources().getDrawable(R.drawable.ic_bar_button_forward) });
        TransitionDrawable tdBtnShuffle, tdBtnShuffleInPlayingBar;
        if(mBtnShuffle.getContentDescription().toString().equals(getString(R.string.shuffleOff))) {
            tdBtnShuffle = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_mode_shuffle_dark), getResources().getDrawable(R.drawable.ic_bar_button_mode_shuffle)});
            tdBtnShuffleInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_playing_large_mode_shuffle_dark), getResources().getDrawable(R.drawable.ic_playing_large_mode_shuffle)});
        }
        else if(mBtnShuffle.getContentDescription().toString().equals(getString(R.string.shuffleOn))) {
            tdBtnShuffle = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_mode_shuffle_on_dark), getResources().getDrawable(R.drawable.ic_bar_button_mode_shuffle_on)});
            tdBtnShuffleInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_playing_large_mode_shuffle_on_dark), getResources().getDrawable(R.drawable.ic_playing_large_mode_shuffle_on)});
        }
        else {
            tdBtnShuffle = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_mode_single_on_dark), getResources().getDrawable(R.drawable.ic_bar_button_mode_single_on)});
            tdBtnShuffleInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_playing_large_mode_single_on_dark), getResources().getDrawable(R.drawable.ic_playing_large_mode_single_on)});
        }
        TransitionDrawable tdBtnRepeat, tdBtnRepeatInPlayingBar;
        if(mBtnRepeat.getContentDescription().toString().equals(getString(R.string.repeatOff))) {
            tdBtnRepeat = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_mode_repeat_dark), getResources().getDrawable(R.drawable.ic_bar_button_mode_repeat)});
            tdBtnRepeatInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_playing_large_mode_repeat_all_dark), getResources().getDrawable(R.drawable.ic_playing_large_mode_repeat_all)});
        }
        else if(mBtnRepeat.getContentDescription().toString().equals(getString(R.string.repeatAllOn))) {
            tdBtnRepeat = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_mode_repeat_all_on_dark), getResources().getDrawable(R.drawable.ic_bar_button_mode_repeat_all_on)});
            tdBtnRepeatInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_playing_large_mode_repeat_all_on_dark), getResources().getDrawable(R.drawable.ic_playing_large_mode_repeat_all_on)});
        }
        else {
            tdBtnRepeat = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_mode_repeat_single_on_dark), getResources().getDrawable(R.drawable.ic_bar_button_mode_repeat_single_on)});
            tdBtnRepeatInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_playing_large_mode_repeat_one_on_dark), getResources().getDrawable(R.drawable.ic_playing_large_mode_repeat_one_on)});
        }
        TransitionDrawable tdBtnRecord = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.ic_bar_button_rec_dark), getResources().getDrawable(R.drawable.ic_bar_button_rec) });
        TransitionDrawable tdBtnSetting = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.ic_leftmenu_settings_dark), getResources().getDrawable(R.drawable.ic_leftmenu_settings) });
        TransitionDrawable tdBtnDarkMode = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.ic_leftmenu_dark_dark), getResources().getDrawable(R.drawable.ic_leftmenu_dark) });
        TransitionDrawable tdBtnRewindInPlayingBar = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.ic_playing_large_rewind_dark), getResources().getDrawable(R.drawable.ic_playing_large_rewind) });
        TransitionDrawable tdBtnForwardInPlayingBar;
        if(mSeekCurPos.getVisibility() == View.VISIBLE)
            tdBtnForwardInPlayingBar = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.ic_playing_large_forward_dark), getResources().getDrawable(R.drawable.ic_playing_large_forward) });
        else
            tdBtnForwardInPlayingBar = new TransitionDrawable( new Drawable[] {getResources().getDrawable(R.drawable.ic_bar_button_forward_dark), getResources().getDrawable(R.drawable.ic_bar_button_forward) });
        TransitionDrawable tdBtnImgViewArtwork = new TransitionDrawable(new Drawable[] { getResources().getDrawable(R.drawable.ic_playing_large_artwork_dark), getResources().getDrawable(R.drawable.ic_playing_large_artwork)});
        TransitionDrawable tdImgViewDown = new TransitionDrawable(new Drawable[] { getResources().getDrawable(R.drawable.ic_playing_large_down_dark), getResources().getDrawable(R.drawable.ic_playing_large_down)});
        TransitionDrawable tdImgViewRecording = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_rec_now_dark), getResources().getDrawable(R.drawable.ic_rec_now)});
        TransitionDrawable tdBtnStopRecording = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_rec_stop_dark), getResources().getDrawable(R.drawable.ic_rec_stop)});

        mBtnMenu.setImageDrawable(tdBtnMenu);
        mBtnMoreInPlayingBar.setImageDrawable(tdBtnMoreInPlayingBar);
        mBtnRewind.setImageDrawable(tdBtnRewind);
        mBtnPlay.setImageDrawable(tdBtnPlay);
        mBtnForward.setImageDrawable(tdBtnForward);
        mBtnShuffle.setImageDrawable(tdBtnShuffle);
        mBtnShuffleInPlayingBar.setImageDrawable(tdBtnShuffleInPlayingBar);
        mBtnRepeat.setImageDrawable(tdBtnRepeat);
        mBtnRepeatInPlayingBar.setImageDrawable(tdBtnRepeatInPlayingBar);
        mBtnRecord.setImageDrawable(tdBtnRecord);
        mBtnSetting.setImageDrawable(tdBtnSetting);
        mBtnDarkMode.setImageDrawable(tdBtnDarkMode);
        mBtnRewindInPlayingBar.setImageDrawable(tdBtnRewindInPlayingBar);
        mBtnPlayInPlayingBar.setImageDrawable(tdBtnPlayInPlayingBar);
        mBtnForwardInPlayingBar.setImageDrawable(tdBtnForwardInPlayingBar);
        mBtnArtworkInPlayingBar.setImageDrawable(tdBtnImgViewArtwork);
        mImgViewDown.setImageDrawable(tdImgViewDown);
        mImgViewRecording.setImageDrawable(tdImgViewRecording);
        mBtnStopRecording.setImageDrawable(tdBtnStopRecording);

        playlistFragment.setLightMode(mTabLayout.getSelectedTabPosition() == 0);
        loopFragment.setLightMode(mTabLayout.getSelectedTabPosition() == 1);
        controlFragment.setLightMode(mTabLayout.getSelectedTabPosition() == 2);
        equalizerFragment.setLightMode(mTabLayout.getSelectedTabPosition() == 3);
        effectFragment.setLightMode(mTabLayout.getSelectedTabPosition() == 4);

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mBtnShuffle.getContentDescription().toString().equals(getString(R.string.shuffleOff))) {
                    mBtnShuffle.setImageDrawable(getResources().getDrawable(R.drawable.ic_bar_button_mode_shuffle));
                    mBtnShuffleInPlayingBar.setImageDrawable(getResources().getDrawable(R.drawable.ic_playing_large_mode_shuffle));
                } else if (mBtnShuffle.getContentDescription().toString().equals(getString(R.string.shuffleOn))) {
                    mBtnShuffle.setImageDrawable(getResources().getDrawable(R.drawable.ic_bar_button_mode_shuffle_on));
                    mBtnShuffleInPlayingBar.setImageDrawable(getResources().getDrawable(R.drawable.ic_playing_large_mode_shuffle_on));
                } else {
                    mBtnShuffle.setImageDrawable(getResources().getDrawable(R.drawable.ic_bar_button_mode_single_on));
                    mBtnShuffleInPlayingBar.setImageDrawable(getResources().getDrawable(R.drawable.ic_playing_large_mode_single_on));
                }
                if (mBtnRepeat.getContentDescription().toString().equals(getString(R.string.repeatOff))) {
                    mBtnRepeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_bar_button_mode_repeat));
                    mBtnRepeatInPlayingBar.setImageDrawable(getResources().getDrawable(R.drawable.ic_playing_large_mode_repeat_all));
                } else if (mBtnRepeat.getContentDescription().toString().equals(getString(R.string.repeatAllOn))) {
                    mBtnRepeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_bar_button_mode_repeat_all_on));
                    mBtnRepeatInPlayingBar.setImageDrawable(getResources().getDrawable(R.drawable.ic_playing_large_mode_repeat_all_on));
                } else {
                    mBtnRepeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_bar_button_mode_repeat_single_on));
                    mBtnRepeatInPlayingBar.setImageDrawable(getResources().getDrawable(R.drawable.ic_playing_large_mode_repeat_one_on));
                }
            }
        });

        int duration = 300;
        anim.setDuration(duration).start();
        tdBtnMenu.startTransition(duration);
        tdBtnMoreInPlayingBar.startTransition(duration);
        tdBtnRewind.startTransition(duration);
        tdBtnPlay.startTransition(duration);
        tdBtnForward.startTransition(duration);
        tdBtnShuffle.startTransition(duration);
        tdBtnShuffleInPlayingBar.startTransition(duration);
        tdBtnRepeat.startTransition(duration);
        tdBtnRepeatInPlayingBar.startTransition(duration);
        tdBtnRecord.startTransition(duration);
        tdBtnSetting.startTransition(duration);
        tdBtnDarkMode.startTransition(duration);
        tdBtnRewindInPlayingBar.startTransition(duration);
        tdBtnPlayInPlayingBar.startTransition(duration);
        tdBtnForwardInPlayingBar.startTransition(duration);
        tdBtnImgViewArtwork.startTransition(duration);
        tdImgViewDown.startTransition(duration);
        tdImgViewRecording.startTransition(duration);
        tdBtnStopRecording.startTransition(duration);

        if(mSeekCurPos.getVisibility() == View.VISIBLE)
            mRelativePlayingWithShadow.setBackgroundResource(R.drawable.playingview);
        else
            mRelativePlayingWithShadow.setBackgroundResource(R.drawable.topshadow);
        mImgViewArtworkInMenu.setBackgroundResource(R.drawable.frameborder);
        mBtnArtworkInPlayingBar.setBackgroundResource(R.drawable.frameborder);
        mTextPlaying.setTextColor(Color.parseColor("#808080"));
        mSeekCurPos.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
        mSeekCurPos.setThumb(getResources().getDrawable(R.drawable.thumbplaying));
    }

    public void setDarkMode(boolean animated) {
        SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("DarkMode", true).apply();

        mDarkMode = true;

        if (Build.VERSION.SDK_INT >= 23) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(ContextCompat.getColor(sActivity, R.color.lightModeBk));
        }
        final RelativeLayout relativeMain = findViewById(R.id.relativeMain);
        final ArgbEvaluator eval = new ArgbEvaluator();
        final int nLightModeBk = getResources().getColor(R.color.lightModeBk);
        final int nDarkModeBk = getResources().getColor(R.color.darkModeBk);
        final int nLightModeText = getResources().getColor(android.R.color.black);
        final int nDarkModeText = getResources().getColor(android.R.color.white);
        final int nLightModeSep = getResources().getColor(R.color.lightModeSep);
        final int nDarkModeSep = getResources().getColor(R.color.darkModeSep);
        final int nLightModeBlue = getResources().getColor(R.color.lightModeBlue);
        final int nDarkModeBlue = getResources().getColor(R.color.darkModeBlue);
        final TabLayout.Tab tab = mTabLayout.getTabAt(mTabLayout.getSelectedTabPosition());
        final TextView textView = (tab == null) ? null : (TextView) tab.getCustomView();
        int nColorBefore;
        if(mTextArtist.getText() == null || mTextArtist.getText().equals(""))
            nColorBefore = Color.argb(255, 147, 156, 160);
        else nColorBefore = getResources().getColor(R.color.lightModeGray);
        int nColorAfter;
        if(mTextArtist.getText() == null || mTextArtist.getText().equals(""))
            nColorAfter = getResources().getColor(R.color.darkModeTextDarkGray);
        else nColorAfter = getResources().getColor(R.color.darkModeGray);
        final int nLightModeArtist = nColorBefore;
        final int nDarkModeArtist = nColorAfter;
        final int nLightModeTextDarkGray = getResources().getColor(R.color.lightModeTextDarkGray);
        final int nDarkModeTextDarkGray = getResources().getColor(R.color.darkModeTextDarkGray);

        ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fProgress = valueAnimator.getAnimatedFraction();
                int nColorModeBk = (Integer) eval.evaluate(fProgress, nLightModeBk, nDarkModeBk);
                int nColorModeSep = (Integer) eval.evaluate(fProgress, nLightModeSep, nDarkModeSep);
                int nColorModeBlue = (Integer) eval.evaluate(fProgress, nLightModeBlue, nDarkModeBlue);
                int nColorModeText = (Integer) eval.evaluate(fProgress, nLightModeText, nDarkModeText);
                int nColorModeArtist = (Integer) eval.evaluate(fProgress, nLightModeArtist, nDarkModeArtist);
                int nColorModeTextDarkGray = (Integer) eval.evaluate(fProgress, nLightModeTextDarkGray, nDarkModeTextDarkGray);
                int nColorRecording = (Integer) eval.evaluate(fProgress, Color.WHITE, nDarkModeBk);
                if (Build.VERSION.SDK_INT >= 23) getWindow().setStatusBarColor(nColorModeBk);
                relativeMain.setBackgroundColor(nColorModeBk);
                mTabLayout.setBackgroundColor(nColorModeBk);
                mTabLayout.setSelectedTabIndicatorColor(nColorModeBk);
                mAdContainerView.setBackgroundColor(nColorModeBk);
                mRelativeLeftMenu.setBackgroundColor(nColorModeBk);
                mRelativeSave.setBackgroundColor(nColorModeBk);
                mRelativeLock.setBackgroundColor(nColorModeBk);
                mRelativeNotice.setBackgroundColor(nColorModeBk);
                mRelativeAddSong.setBackgroundColor(nColorModeBk);
                mRelativeHideAds.setBackgroundColor(nColorModeBk);
                mRelativeItem.setBackgroundColor(nColorModeBk);
                mRelativeHelp.setBackgroundColor(nColorModeBk);
                mRelativeInquiry.setBackgroundColor(nColorModeBk);
                mRelativeReview.setBackgroundColor(nColorModeBk);
                mRelativeInfo.setBackgroundColor(nColorModeBk);
                mTextSave.setTextColor(nColorModeText);
                mTextLock.setTextColor(nColorModeText);
                mTextHideAds.setTextColor(nColorModeText);
                mTextItemInMenu.setTextColor(nColorModeText);
                mTextHelp.setTextColor(nColorModeText);
                mTextInquiry.setTextColor(nColorModeText);
                mTextReview.setTextColor(nColorModeText);
                mTextInfo.setTextColor(nColorModeText);
                mTextNotice.setTextColor(nColorModeText);
                mTextAddSong.setTextColor(nColorModeText);
                mTextTitle.setTextColor(nColorModeText);
                mTextTitleInMenu.setTextColor(nColorModeText);
                mTextArtist.setTextColor(nColorModeArtist);
                mTextArtistInMenu.setTextColor(nColorModeArtist);
                mTextCurPos.setTextColor(nColorModeTextDarkGray);
                mTextRemain.setTextColor(nColorModeTextDarkGray);
                mViewSep0.setBackgroundColor(nColorModeSep);
                mViewSep1.setBackgroundColor(nColorModeSep);
                mViewSep2.setBackgroundColor(nColorModeSep);
                mViewSep3.setBackgroundColor(nColorModeSep);
                mDividerMenu.setBackgroundColor(nColorModeSep);
                mRelativeRecording.setBackgroundColor(nColorModeBlue);
                mTextRecording.setTextColor(nColorRecording);
                mTextRecordingTime.setTextColor(nColorRecording);
                if (textView != null) {
                    textView.setTextColor(nColorModeBlue);
                    for (Drawable drawable : textView.getCompoundDrawables()) {
                        if (drawable != null)
                            drawable.setColorFilter(new PorterDuffColorFilter(nColorModeBlue, PorterDuff.Mode.SRC_IN));
                    }
                }
            }
        });

        TransitionDrawable tdBtnMenu;
        if (needsToDisplayDeadline(true, false)) {
            tdBtnMenu = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_menu_notice), getResources().getDrawable(R.drawable.ic_bar_button_menu_notice_dark)});
        } else {
            tdBtnMenu = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_menu), getResources().getDrawable(R.drawable.ic_bar_button_menu_dark)});
        }
        TransitionDrawable tdBtnMoreInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_playing_large_more), getResources().getDrawable(R.drawable.ic_playing_large_more_dark)});
        TransitionDrawable tdBtnRewind = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_rewind), getResources().getDrawable(R.drawable.ic_bar_button_rewind_dark)});
        TransitionDrawable tdBtnPlay, tdBtnPlayInPlayingBar;
        if (BASS.BASS_ChannelIsActive(sStream) != BASS.BASS_ACTIVE_PLAYING) {
            tdBtnPlay = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_play), getResources().getDrawable(R.drawable.ic_bar_button_play_dark)});
            if(mSeekCurPos.getVisibility() == View.VISIBLE)
                tdBtnPlayInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_playing_large_play), getResources().getDrawable(R.drawable.ic_playing_large_play_dark)});
            else
                tdBtnPlayInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_play), getResources().getDrawable(R.drawable.ic_bar_button_play_dark)});
        } else {
            tdBtnPlay = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_pause), getResources().getDrawable(R.drawable.ic_bar_button_pause_dark)});
            if(mSeekCurPos.getVisibility() == View.VISIBLE)
                tdBtnPlayInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_playing_large_pause), getResources().getDrawable(R.drawable.ic_playing_large_pause_dark)});
            else
                tdBtnPlayInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_pause), getResources().getDrawable(R.drawable.ic_bar_button_pause_dark)});
        }
        TransitionDrawable tdBtnForward = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_forward), getResources().getDrawable(R.drawable.ic_bar_button_forward_dark)});
        TransitionDrawable tdBtnShuffle, tdBtnShuffleInPlayingBar;
        if (mBtnShuffle.getContentDescription().toString().equals(getString(R.string.shuffleOff))) {
            tdBtnShuffle = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_mode_shuffle), getResources().getDrawable(R.drawable.ic_bar_button_mode_shuffle_dark)});
            tdBtnShuffleInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_playing_large_mode_shuffle), getResources().getDrawable(R.drawable.ic_playing_large_mode_shuffle_dark)});
        } else if (mBtnShuffle.getContentDescription().toString().equals(getString(R.string.shuffleOn))) {
            tdBtnShuffle = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_mode_shuffle_on), getResources().getDrawable(R.drawable.ic_bar_button_mode_shuffle_on_dark)});
            tdBtnShuffleInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_playing_large_mode_shuffle_on), getResources().getDrawable(R.drawable.ic_playing_large_mode_shuffle_on_dark)});
        } else {
            tdBtnShuffle = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_mode_single_on), getResources().getDrawable(R.drawable.ic_bar_button_mode_single_on_dark)});
            tdBtnShuffleInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_playing_large_mode_single_on), getResources().getDrawable(R.drawable.ic_playing_large_mode_single_on_dark)});
        }
        TransitionDrawable tdBtnRepeat, tdBtnRepeatInPlayingBar;
        if (mBtnRepeat.getContentDescription().toString().equals(getString(R.string.repeatOff))) {
            tdBtnRepeat = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_mode_repeat), getResources().getDrawable(R.drawable.ic_bar_button_mode_repeat_dark)});
            tdBtnRepeatInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_playing_large_mode_repeat_all), getResources().getDrawable(R.drawable.ic_playing_large_mode_repeat_all_dark)});
        } else if (mBtnRepeat.getContentDescription().toString().equals(getString(R.string.repeatAllOn))) {
            tdBtnRepeat = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_mode_repeat_all_on), getResources().getDrawable(R.drawable.ic_bar_button_mode_repeat_all_on_dark)});
            tdBtnRepeatInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_playing_large_mode_repeat_all_on), getResources().getDrawable(R.drawable.ic_playing_large_mode_repeat_all_on_dark)});
        } else {
            tdBtnRepeat = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_mode_repeat_single_on), getResources().getDrawable(R.drawable.ic_bar_button_mode_repeat_single_on_dark)});
            tdBtnRepeatInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_playing_large_mode_repeat_one_on), getResources().getDrawable(R.drawable.ic_playing_large_mode_repeat_one_on_dark)});
        }
        TransitionDrawable tdBtnRecord = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_rec), getResources().getDrawable(R.drawable.ic_bar_button_rec_dark)});
        TransitionDrawable tdBtnSetting = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_leftmenu_settings), getResources().getDrawable(R.drawable.ic_leftmenu_settings_dark)});
        TransitionDrawable tdBtnDarkMode = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_leftmenu_dark), getResources().getDrawable(R.drawable.ic_leftmenu_dark_dark)});
        TransitionDrawable tdBtnRewindInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_playing_large_rewind), getResources().getDrawable(R.drawable.ic_playing_large_rewind_dark)});
        TransitionDrawable tdBtnForwardInPlayingBar;
        if(mSeekCurPos.getVisibility() == View.VISIBLE)
            tdBtnForwardInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_playing_large_forward), getResources().getDrawable(R.drawable.ic_playing_large_forward_dark)});
        else
            tdBtnForwardInPlayingBar = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_bar_button_forward), getResources().getDrawable(R.drawable.ic_bar_button_forward_dark)});
        TransitionDrawable tdBtnImgViewArtwork = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_playing_large_artwork), getResources().getDrawable(R.drawable.ic_playing_large_artwork_dark)});
        TransitionDrawable tdImgViewDown = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_playing_large_down), getResources().getDrawable(R.drawable.ic_playing_large_down_dark)});
        TransitionDrawable tdImgViewRecording = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_rec_now), getResources().getDrawable(R.drawable.ic_rec_now_dark)});
        TransitionDrawable tdBtnStopRecording = new TransitionDrawable(new Drawable[]{getResources().getDrawable(R.drawable.ic_rec_stop), getResources().getDrawable(R.drawable.ic_rec_stop_dark)});

        mBtnMenu.setImageDrawable(tdBtnMenu);
        mBtnMoreInPlayingBar.setImageDrawable(tdBtnMoreInPlayingBar);
        mBtnRewind.setImageDrawable(tdBtnRewind);
        mBtnPlay.setImageDrawable(tdBtnPlay);
        mBtnForward.setImageDrawable(tdBtnForward);
        mBtnShuffle.setImageDrawable(tdBtnShuffle);
        mBtnShuffleInPlayingBar.setImageDrawable(tdBtnShuffleInPlayingBar);
        mBtnRepeat.setImageDrawable(tdBtnRepeat);
        mBtnRepeatInPlayingBar.setImageDrawable(tdBtnRepeatInPlayingBar);
        mBtnRecord.setImageDrawable(tdBtnRecord);
        mBtnSetting.setImageDrawable(tdBtnSetting);
        mBtnDarkMode.setImageDrawable(tdBtnDarkMode);
        mBtnRewindInPlayingBar.setImageDrawable(tdBtnRewindInPlayingBar);
        mBtnPlayInPlayingBar.setImageDrawable(tdBtnPlayInPlayingBar);
        mBtnForwardInPlayingBar.setImageDrawable(tdBtnForwardInPlayingBar);
        mBtnArtworkInPlayingBar.setImageDrawable(tdBtnImgViewArtwork);
        mImgViewDown.setImageDrawable(tdImgViewDown);
        mImgViewRecording.setImageDrawable(tdImgViewRecording);
        mBtnStopRecording.setImageDrawable(tdBtnStopRecording);

        playlistFragment.setDarkMode(mTabLayout.getSelectedTabPosition() == 0);
        loopFragment.setDarkMode(mTabLayout.getSelectedTabPosition() == 1);
        controlFragment.setDarkMode(mTabLayout.getSelectedTabPosition() == 2);
        equalizerFragment.setDarkMode(mTabLayout.getSelectedTabPosition() == 3);
        effectFragment.setDarkMode(mTabLayout.getSelectedTabPosition() == 4);

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mBtnShuffle.getContentDescription().toString().equals(getString(R.string.shuffleOff))) {
                    mBtnShuffle.setImageDrawable(getResources().getDrawable(R.drawable.ic_bar_button_mode_shuffle_dark));
                    mBtnShuffleInPlayingBar.setImageDrawable(getResources().getDrawable(R.drawable.ic_playing_large_mode_shuffle_dark));
                } else if (mBtnShuffle.getContentDescription().toString().equals(getString(R.string.shuffleOn))) {
                    mBtnShuffle.setImageDrawable(getResources().getDrawable(R.drawable.ic_bar_button_mode_shuffle_on_dark));
                    mBtnShuffleInPlayingBar.setImageDrawable(getResources().getDrawable(R.drawable.ic_playing_large_mode_shuffle_on_dark));
                } else {
                    mBtnShuffle.setImageDrawable(getResources().getDrawable(R.drawable.ic_bar_button_mode_single_on_dark));
                    mBtnShuffleInPlayingBar.setImageDrawable(getResources().getDrawable(R.drawable.ic_playing_large_mode_single_on_dark));
                }
                if (mBtnRepeat.getContentDescription().toString().equals(getString(R.string.repeatOff))) {
                    mBtnRepeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_bar_button_mode_repeat_dark));
                    mBtnRepeatInPlayingBar.setImageDrawable(getResources().getDrawable(R.drawable.ic_playing_large_mode_repeat_all_dark));
                } else if (mBtnRepeat.getContentDescription().toString().equals(getString(R.string.repeatAllOn))) {
                    mBtnRepeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_bar_button_mode_repeat_all_on_dark));
                    mBtnRepeatInPlayingBar.setImageDrawable(getResources().getDrawable(R.drawable.ic_playing_large_mode_repeat_all_on_dark));
                } else {
                    mBtnRepeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_bar_button_mode_repeat_single_on_dark));
                    mBtnRepeatInPlayingBar.setImageDrawable(getResources().getDrawable(R.drawable.ic_playing_large_mode_repeat_one_on_dark));
                }
            }
        });

        int duration = animated ? 300 : 0;
        anim.setDuration(duration).start();
        tdBtnMenu.startTransition(duration);
        tdBtnMoreInPlayingBar.startTransition(duration);
        tdBtnRewind.startTransition(duration);
        tdBtnPlay.startTransition(duration);
        tdBtnForward.startTransition(duration);
        tdBtnShuffle.startTransition(duration);
        tdBtnShuffleInPlayingBar.startTransition(duration);
        tdBtnRepeat.startTransition(duration);
        tdBtnRepeatInPlayingBar.startTransition(duration);
        tdBtnRecord.startTransition(duration);
        tdBtnSetting.startTransition(duration);
        tdBtnDarkMode.startTransition(duration);
        tdBtnPlayInPlayingBar.startTransition(duration);
        tdBtnRewindInPlayingBar.startTransition(duration);
        tdBtnForwardInPlayingBar.startTransition(duration);
        tdBtnImgViewArtwork.startTransition(duration);
        tdImgViewDown.startTransition(duration);
        tdImgViewRecording.startTransition(duration);
        tdBtnStopRecording.startTransition(duration);

        if(mSeekCurPos.getVisibility() == View.VISIBLE)
            mRelativePlayingWithShadow.setBackgroundResource(R.drawable.playingview_dark);
        else mRelativePlayingWithShadow.setBackgroundResource(R.drawable.topshadow_dark);
        mImgViewArtworkInMenu.setBackgroundResource(R.drawable.frameborder_dark);
        mBtnArtworkInPlayingBar.setBackgroundResource(R.drawable.frameborder_dark);
        mTextPlaying.setTextColor(getResources().getColor(R.color.darkModePlaying));
        mSeekCurPos.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        mSeekCurPos.setThumb(getResources().getDrawable(R.drawable.thumbplaying_dark));
    }

    private void updateMenuButton() {
        if (needsToDisplayDeadline(true, false)) {
            mBtnMenu.setImageResource(mDarkMode ? R.drawable.ic_bar_button_menu_notice_dark : R.drawable.ic_bar_button_menu_notice);
        } else {
            mBtnMenu.setImageResource(mDarkMode ? R.drawable.ic_bar_button_menu_dark : R.drawable.ic_bar_button_menu);
        }
    }

    private boolean needsToDisplayDeadline(boolean fromNotice, boolean ignoreDisplayed) {
        SharedPreferences preferences = getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        boolean b2025AnniversaryDisplayed = preferences.getBoolean(fromNotice ? "b2025AnniversaryDisplayedFromNotice" : "b2025AnniversaryDisplayed", false);
        final Date currentDate = new Date();
        DateFormat formatter = new SimpleDateFormat("yyyy/M/d H:m:s", Locale.getDefault());
        Date dateTo;
        try {
            dateTo = formatter.parse("2025/1/15 23:59:59");
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        if (ignoreDisplayed) {
            if (currentDate.compareTo(dateTo) <= 0 && Locale.getDefault().equals(Locale.JAPAN)) {
                return true;
            }
        } else {
            if (!b2025AnniversaryDisplayed && currentDate.compareTo(dateTo) <= 0 && Locale.getDefault().equals(Locale.JAPAN)) {
                return true;
            }
        }
        return false;
    }
}
