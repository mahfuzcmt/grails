package com.webcommander.calculator.model

interface Discount {
    Double getAmount();
    void setAmount(Double amount);
    void onClear();
    void onAdd()
}