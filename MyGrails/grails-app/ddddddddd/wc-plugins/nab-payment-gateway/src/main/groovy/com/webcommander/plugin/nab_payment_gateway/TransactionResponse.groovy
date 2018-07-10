package com.webcommander.plugin.nab_payment_gateway;

public class TransactionResponse {
    String messageID;
    String merchantID;
    String statusCode;
    String statusDescription;
    double amount;
    int purchaseOrderNo;
    boolean approved;
    String responseCode;
    String responseText;
    String txnID;
}
