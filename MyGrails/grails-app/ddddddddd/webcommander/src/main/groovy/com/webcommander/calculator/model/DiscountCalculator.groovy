package com.webcommander.calculator.model

import com.webcommander.models.Cart

interface DiscountCalculator {
    void calculate(Cart cart)
}