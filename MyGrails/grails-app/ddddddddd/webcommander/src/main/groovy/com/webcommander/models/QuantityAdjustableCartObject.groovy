package com.webcommander.models

/**
 * Created by zobair on 24/04/2014.*/
trait QuantityAdjustableCartObject extends CartObject {
    Integer multipleOfOrderQuantity
    Integer supportedMinOrderQuantity
    Integer supportedMaxOrderQuantity

    Boolean isMultipleOrderQuantity;
}
