package com.webcommander.models

import com.webcommander.webcommerce.ShippingProfile

/**
 * Created by zobair on 25/01/14.
 */
trait ShippableCartObject extends CartObject {
    Double weight;
    Double length;
    Double width;
    Double height;

    Boolean isShippable

    public abstract ShippingProfile resolveShippingProfile();
}
