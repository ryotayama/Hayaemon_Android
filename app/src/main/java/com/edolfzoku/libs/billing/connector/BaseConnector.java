package com.edolfzoku.libs.billing.connector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.edolfzoku.libs.billing.result.*;

/**
 * 課金に関する通信クラス
 */
public abstract class BaseConnector implements Runnable {
    protected IBillingResult postResult;
    protected BillingClient billingClient;

    /**
     * コンストラクタ
     */
    public BaseConnector() {
        this.postResult = null;
    }

    /**
     * サーバーへ接続
     *
     * @param postBillingResult 課金結果の格納先
     * @param billingClient     課金クライアント
     */
    public void connect(@Nullable IBillingResult postBillingResult, @NonNull BillingClient billingClient) {
        this.billingClient = billingClient;
        if (postBillingResult != null) {
            this.postResult = postBillingResult;
        }

        if (this.billingClient.isReady()) {
            run();
            return;
        }

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                run();
            }

            @Override
            public void onBillingServiceDisconnected() {
            }
        });
    }

    /**
     * 課金結果を格納
     *
     * @param billingResult 課金結果
     */
    public void setResultNotify(BillingResult billingResult) {
        if (this.postResult != null) {
            this.postResult.resultNotify(billingResult);
        }
    }

    /**
     * 課金結果を生成
     *
     * @param responseCode レスポンスコード
     * @param debugMessage デバッグメッセージ
     * @return 生成した課金結果
     */
    public static BillingResult generalBillingResult(int responseCode, String debugMessage) {
        return BillingResult.newBuilder()
                .setResponseCode(responseCode)
                .setDebugMessage(debugMessage)
                .build();
    }
}
