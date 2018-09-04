package com.webcommander.calculator.packing.algorithm

import com.webcommander.calculator.packing.model.AusPostPackingItem
import com.webcommander.models.CartItem
import com.webcommander.models.ProductData

class CombinedPacking {
    /**
     * Create multiple packets with cart items if max dimension/weight exceeds of AusPost
     * AusPost applies this method only for domestic postage
     * @param cartItems
     * @return
     */
    public static List getDimensionMapList(List<CartItem> cartItems) {
        List<Map> dimensions = []
        boolean isDomestic = true
        for(CartItem cartItem : cartItems) {
            ProductData productData = cartItem.object.product
            AusPostPackingItem packingItem = new AusPostPackingItem(productData.weight, productData.length,  productData.width, productData.height)
            Map lwhw = [length: 0.0, width: 0.0, height: 0.0, weight: 0.0]
            if(packingItem.isShippable(isDomestic)) {
                Double maxLength = 0.0, maxHeight = 0.0
                for(int i = 0; i < cartItem.quantity; i++) {
                    Double wg = packingItem.getWeight()
                    Double w = packingItem.width
                    Double l = maxLength > packingItem.length ? maxLength : packingItem.length
                    Double h = maxHeight > packingItem.height ? maxHeight : packingItem.height
                    if(new AusPostPackingItem(lwhw.weight + wg, l, lwhw.width + w, h).isShippable(isDomestic)) {
                        lwhw.weight += wg
                        lwhw.width += w
                        lwhw.length = l
                        lwhw.height = h
                    } else {
                        dimensions.add(lwhw)
                        lwhw = [length: 0.0, width: 0.0, height: 0.0, weight: 0.0]
                    }
                }
                dimensions.add(lwhw)
            }
        }
        return dimensions
    }

    /**
     * Create multiple packets with cart items if max weight exceeds of AusPost
     * AusPost applies this method only for international postage
     * @param cartItems
     * @return
     */
    public static List getWeightList(List<CartItem> cartItems) {
        List<Double> weights = []
        for(CartItem cartItem : cartItems) {
            ProductData productData = cartItem.object.product
            AusPostPackingItem packingItem = new AusPostPackingItem(productData.weight, productData.length,  productData.width, productData.height)
            Double weight = 0.0
            if(packingItem.isShippable()) {
                for(int i = 0; i < cartItem.quantity; i++) {
                    Double wg = packingItem.getWeight()
                    if(new AusPostPackingItem(weight + wg).isShippable()) {
                        weight += wg
                    } else {
                        weights.add(weight)
                        weight = 0.0
                    }
                }
                weights.add(weight)
            }
        }
        return weights
    }
}
