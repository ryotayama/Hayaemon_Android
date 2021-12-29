package com.edolfzoku.libs.billing.result;

import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;

public class PurchaseResult implements IBillingResult {

    /**
     * 結果を通知
     *
     * @param billingResult 課金結果
     */
    @Override
    public void resultNotify(@NonNull BillingResult billingResult) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            Log.d("billing", "購入成功");

            // アイテム情報の反映処理を書く
            // 購入完了ダイアログを出す
        }
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d("billing", "ユーザーによってキャンセル");

            // 購入キャンセルダイアログを出す
        }
        else {
            Log.d("billing", "上記以外のエラー");

            // エラーダイアログを出す
        }
    }
}