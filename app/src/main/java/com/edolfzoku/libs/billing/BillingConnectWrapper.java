package com.edolfzoku.libs.billing;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.android.billingclient.api.*;
import com.edolfzoku.libs.billing.connector.*;
import com.edolfzoku.libs.billing.item.*;
import com.edolfzoku.libs.billing.result.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 課金システムへの接続ラッパークラス
 */
public class BillingConnectWrapper implements PurchasesUpdatedListener {

    // 実行中のアクティビティ
    protected final Activity currentActivity;

    // 課金クライアント
    protected final BillingClient billingClient;

    // 購入アイテムリスト
    protected final List<PurchaseItem> purchaseItemList;

    /**
     * コンストラクタ
     *
     * @param activity         アクティビティ
     * @param purchaseItemList 課金アイテムリスト
     */
    public BillingConnectWrapper(@NonNull Activity activity, @NonNull List<PurchaseItem> purchaseItemList) {
        this.currentActivity = activity;
        this.purchaseItemList = purchaseItemList;

        // 課金クライアントの準備
        this.billingClient = BillingClient.newBuilder(this.currentActivity)
                .setListener(this)
                .enablePendingPurchases()
                .build();
    }

    /**
     * 購入情報の更新
     *
     * @param billingResult 課金結果
     * @param purchaseList  購入リスト
     */
    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchaseList) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            if (purchaseList == null) {
                // 結果が空
                return;
            }

            for (Purchase purchase : purchaseList) {
                for (PurchaseItem item : purchaseItemList) {
                    if (item.getItemName().equals(purchase.getSku())) {
                        item.handlePurchase(billingClient, purchase);
                    }
                }
            }
        }
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // キャンセル
            Log.d("billing", "購入がキャンセルされました");
        }
    }

    /**
     * 課金サーバーへ接続
     *
     * @param postBillingResult 課金結果の格納先
     */
    public void bind(@Nullable final IBillingResult postBillingResult) {
        BaseConnector connector = new BaseConnector() {
            @Override
            public void run() {
                Log.i("billing", "GooglePlayへ接続");

                // アイテムの情報を取得
                restorePurchaseItem(postBillingResult);
            }
        };
        connector.connect(null, billingClient);
    }

    /**
     * 課金サーバーと切断
     */
    public void unbind() {
        // GooglePlayから切断
        billingClient.endConnection();
    }

    /**
     * 購入アイテムのリストア
     *
     * @param postBillingResult 課金結果の格納先
     */
    public void restorePurchaseItem(@Nullable final IBillingResult postBillingResult) {
        BaseConnector connector = new GetSkuDetailsConnector(this.purchaseItemList);
        connector.connect(postBillingResult, billingClient);
    }

    /**
     * 課金フローを開始
     *
     * @param postBillingResult 課金結果の格納先
     * @param skuDetails        サーバーへ問い合わせるアイテム情報
     */
    public void startBillingFlow(@Nullable final IBillingResult postBillingResult, @NonNull SkuDetails skuDetails) {
        BaseConnector connector = new StartConnector(currentActivity, skuDetails);
        connector.connect(postBillingResult, billingClient);
    }

    /**
     * 通信エラーなどで途中で止まっている購入情報を反映
     * onCreateやonResumeでコール
     *
     * @param postBillingResult 課金結果の格納先
     */
    public void queryPurchases(@Nullable final IBillingResult postBillingResult) {
        BaseConnector connector = new QueryPurchasesConnector(this.purchaseItemList);
        connector.connect(postBillingResult, billingClient);
    }

    /**
     * 自作のコネクターを使用して課金システムにアクセス
     *
     * @param postBillingResult 課金結果の格納先
     * @param connector         自作コネクター
     */
    public void CustomConnection(@Nullable final IBillingResult postBillingResult, @NonNull BaseConnector connector) {
        connector.connect(postBillingResult, billingClient);
    }
}

///
/// 以下、システムとして最低限必要なコネクタークラス
///

/**
 * 課金状況を復元
 */
final class QueryPurchasesConnector extends BaseConnector {
    private final List<PurchaseItem> purchaseItemList;

    /**
     * コンストラクタ
     *
     * @param purchaseItemList 購入アイテムリスト
     */
    public QueryPurchasesConnector(@NonNull List<PurchaseItem> purchaseItemList) {
        super();

        this.purchaseItemList = purchaseItemList;
    }

    /**
     * 実行
     */
    @Override
    public void run() {
        int responseCode = BillingClient.BillingResponseCode.ERROR;
        String debugMessage = "ORG:QueryPurchasesConnector Error.";
        Purchase.PurchasesResult purchasesResult;
        purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
        if (purchasesResult.getPurchasesList() != null) {
            for (Purchase purchase : purchasesResult.getPurchasesList()) {
                for (PurchaseItem item : purchaseItemList) {
                    if (item.getItemName().equals(purchase.getSku())) {
                        // 課金状況を復帰
                        item.handlePurchase(billingClient, purchase);
                    }
                }
            }
            responseCode = BillingClient.BillingResponseCode.OK;
            debugMessage = "ORG:QueryPurchasesConnector Completed.";
        }

        // それっぽいリザルトを独自で返す
        setResultNotify(generalBillingResult(responseCode, debugMessage));
    }
}

/**
 * 課金履歴を取得
 */
final class GetSkuDetailsConnector extends BaseConnector {
    final private List<PurchaseItem> purchaseItemList;
    final private List<String> skuList;

    /**
     * コンストラクタ
     *
     * @param purchaseItemList 購入アイテムリスト
     */
    public GetSkuDetailsConnector(@NonNull List<PurchaseItem> purchaseItemList) {
        super();

        this.purchaseItemList = purchaseItemList;
        this.skuList = new ArrayList<>();
        for (PurchaseItem item : purchaseItemList) {
            this.skuList.add(item.getItemName());
        }
    }

    /**
     * 実行
     */
    @Override
    public void run() {
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(@NonNull BillingResult result, @Nullable List<SkuDetails> list) {
                // 購入可能なアイテムのSKU情報の取得に成功
                if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                    Log.e("billing", "GetSkuDetailsConnector::BillingResponseCode.OKではありません");
                    return;
                } else if (list == null) {
                    Log.e("billing", "GetSkuDetailsConnector::SkuDetailsリストがNULLです");
                    return;
                } else {
                    for (SkuDetails sku : Objects.requireNonNull(list)) {
                        for (PurchaseItem item : purchaseItemList) {
                            if (sku.getSku().equals(item.getItemName())) {
                                item.setSkuDetails(sku);
                            }
                        }
                    }
                }
                setResultNotify(result);
            }
        });
    }
}

/**
 * 課金開始
 */
final class StartConnector extends BaseConnector {
    final private Activity activity;
    final private SkuDetails skuDetails;

    /**
     * コンストラクタ
     *
     * @param activity   アクティビティ
     * @param skuDetails サーバーから取得するアイテム情報
     */
    public StartConnector(@NonNull Activity activity, @NonNull SkuDetails skuDetails) {
        super();

        this.activity = activity;
        this.skuDetails = skuDetails;
    }

    /**
     * 実行
     */
    @Override
    public void run() {
        BillingFlowParams billingFlowParams = BillingFlowParams
                .newBuilder()
                .setSkuDetails(skuDetails)
                .build();

        setResultNotify(billingClient.launchBillingFlow(activity, billingFlowParams));
    }
}

