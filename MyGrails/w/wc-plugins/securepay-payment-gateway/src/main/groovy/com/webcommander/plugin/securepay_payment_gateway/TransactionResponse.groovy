package com.webcommander.plugin.securepay_payment_gateway;

public class TransactionResponse {
    String messageID;
    String merchantID;
    String statusCode;
    String statusDescription;
    String txnType;
    double amount;
    int purchaseOrderNo;
    boolean approved;
    String responseCode;
    String responseText;
    String txnID;
}
