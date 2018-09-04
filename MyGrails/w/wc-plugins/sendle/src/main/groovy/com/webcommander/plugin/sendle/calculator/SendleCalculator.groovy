package com.webcommander.plugin.sendle.calculator

import com.webcommander.config.StoreDetail
import com.webcommander.models.AddressData
import com.webcommander.plugin.sendle.communicator.SendleCommunicator
import com.webcommander.webcommerce.ShippingCondition

class SendleCalculator {

    public static Double calculateCost(AddressData receiverLocation, ShippingCondition condition, Integer noOfItem, Double width, Double height, length, Double weight){
        AddressData senderLocation = new AddressData(StoreDetail.first().address)
        Double volume = width * height * length
        return  calculateCostInner(senderLocation, receiverLocation, condition, noOfItem, weight, volume)
    }

    public static Double calculateCostInner(AddressData senderLocation, AddressData receiverLocation, ShippingCondition condition, Integer noOfItem, Double weight, Double volume) {
        def shippingCost = SendleCommunicator.calculateShippingCost(senderLocation, receiverLocation, weight, volume, noOfItem)
        return shippingCost
    }
}
