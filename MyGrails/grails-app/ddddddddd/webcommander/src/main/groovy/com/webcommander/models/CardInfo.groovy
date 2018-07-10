package com.webcommander.models

class CardInfo {
    String holderName
    String cardNumber
    String expiryMonth
    String expiryYear
    String cvv

    public CardInfo(String holderName, String cardNumber, String cvv, String expiryMonth, String expiryYear) {
        this.holderName = holderName
        this.cardNumber = cardNumber
        this.expiryMonth = expiryMonth
        this.expiryYear = expiryYear
        this.cvv = cvv
    }

    public CardInfo(String cardNumber, String cvv, String expiryMonth, String expiryYear) {
      this("", cardNumber, cvv, expiryMonth, expiryYear)
    }
}
