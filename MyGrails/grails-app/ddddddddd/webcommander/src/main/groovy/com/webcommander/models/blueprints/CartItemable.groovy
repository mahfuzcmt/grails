package com.webcommander.models.blueprints

import grails.util.TypeConvertingMap

/**
 * Created by sajedur on 6/30/2015.
 */
interface CartItemable {
    String getItemName();
    String getItemType();
    Integer getQuantity();
    Long getItemId();
    TypeConvertingMap paramsObj();
}
