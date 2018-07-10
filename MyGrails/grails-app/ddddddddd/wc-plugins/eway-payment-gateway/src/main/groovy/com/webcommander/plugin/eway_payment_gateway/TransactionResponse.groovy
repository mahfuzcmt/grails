package com.webcommander.plugin.eway_payment_gateway;

public class TransactionResponse {
    String authCode;
    String responseCode;
    double returnAmount;
    int trxnNumber;
    boolean trxnStatus;
    String trxnResponseMessage;
    String merchantOption1;
    String merchantOption2;
    String merchantOption3;
    String merchantReference;
    String merchantInvoice;
    String errorMessage;
}
