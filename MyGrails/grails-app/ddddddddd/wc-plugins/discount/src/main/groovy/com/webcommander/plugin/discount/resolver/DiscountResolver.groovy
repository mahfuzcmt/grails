package com.webcommander.plugin.discount.resolver

import com.webcommander.plugin.discount.models.DiscountData
import com.webcommander.plugin.discount.webcommerce.CustomDiscount

/**
 * Created by sharif ul islam on 12/03/2018.
 */
class DiscountResolver extends Resolver {

    @Override
    Object doResolve(Map context) {

        Resolver discountProfileResolver = DiscountResolverFactory.getDiscountProfileResolver();
        Resolver discountValueResolver = DiscountResolverFactory.getDiscountValueResolver();

        Map response = [:]
        List<DiscountData> discountDataList = []

        Map profileResolverContext = [:]
        profileResolverContext.productId = context.productId
        profileResolverContext.productData = context.productData
        profileResolverContext.cart = context.cart
        Map profileResolverResponse = discountProfileResolver.resolve(profileResolverContext)

        if (profileResolverResponse.resolvedProfiles) {
            for (CustomDiscount discount : profileResolverResponse.resolvedProfiles) {
                DiscountData discountData = new DiscountData()
                discountData.productId = context.productId
                discountData.discountId = discount.id
                discountData.productData = context.productData
                discountData.discount = discount

                Map valueResolverContext = [:]
                valueResolverContext.discount = discount
                valueResolverContext.productId = context.productId
                valueResolverContext.productData = context.productData
                valueResolverContext.discountData = discountData
                valueResolverContext.cart = context.cart
                Map valueResolverResponse = discountValueResolver.resolve(valueResolverContext)

                discountData = valueResolverResponse.discountData

                discountDataList.add(discountData)
            }
        }

        response.discountDataList = discountDataList

        return response
    }

}
