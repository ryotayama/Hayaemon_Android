package com.edolfzoku.libs.billing.result;

import androidx.annotation.NonNull;
import com.android.billingclient.api.BillingResult;

/**
 * 課金リザルト受け取るインターフェイス
 */
public interface IBillingResult {

    /**
     * 結果を通知
     *
     * @param billingResult 課金結果
     */
    void resultNotify(@NonNull BillingResult billingResult);
}
