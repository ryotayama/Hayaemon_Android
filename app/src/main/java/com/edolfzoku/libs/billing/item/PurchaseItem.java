package com.edolfzoku.libs.billing.item;

import android.util.Log;
import androidx.annotation.NonNull;
import com.android.billingclient.api.*;

import com.edolfzoku.libs.billing.result.IBillingResult;

/**
 * 課金アイテムクラス
 */
public class PurchaseItem implements AcknowledgePurchaseResponseListener {

    // アイテム名
    protected final String itemName;

    // サーバーとやり取り用のアイテム情報
    protected SkuDetails skuDetails;

    // 購入済みアイテムか
    protected boolean isPurchased;

    // 購入結果の格納先
    protected IBillingResult postBillingResult;

    /**
     * コンストラクタ
     *
     * @param postBillingResult 課金結果の格納先
     * @param itemName          アイテム名
     */
    public PurchaseItem(@NonNull IBillingResult postBillingResult, @NonNull String itemName) {
        this.itemName = itemName;
        this.postBillingResult = postBillingResult;
        this.isPurchased = false;
    }

    /**
     * 購入通信のリザルト
     *
     * @param billingResult 課金結果
     */
    @Override
    public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            Log.d("billing", "購入完了 " + itemName);
            isPurchased = true;
        }
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Log.d("billing", "購入済み " + itemName);
            isPurchased = true;
        }

        // 課金結果を通知
        this.postBillingResult.resultNotify(billingResult);
    }

    /**
     * アイテム名を取得
     *
     * @return アイテム名
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * サーバーから取得したアイテム情報を取得
     *
     * @return アイテム情報
     */
    public SkuDetails getSkuDetails() {
        return this.skuDetails;
    }

    /**
     * アイテム情報を設定
     *
     * @param skuDetails サーバーから取得したアイテム情報
     */
    public void setSkuDetails(@NonNull SkuDetails skuDetails) {
        this.skuDetails = skuDetails;
    }

    /**
     * アイテムが購入されているか
     *
     * @return 購入されているか
     */
    public boolean IsPurchased() {
        return isPurchased;
    }

    /**
     * 購入処理のハンドリング
     *
     * @param billingClient 課金クライアント
     * @param purchase 購入結果
     */
    public void handlePurchase(@NonNull BillingClient billingClient, @NonNull Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                // 購入承認前であるアイテム
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, this);
            }
            else {
                isPurchased = true;
            }
        }
    }

    /**
     * 課金済み状態のON/OFFを切り替える
     * デバッグ専用
     * ※正常遷移では無いので使用は自己責任
     *
     * @param isPurchased 課金済みにするか
     */
    public void debugSetIsPurchased(boolean isPurchased) {
        this.isPurchased = isPurchased;
    }
}
