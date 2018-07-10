package com.webcommander.plugin.sendle.controllers.admin

import com.webcommander.config.StoreDetail
import com.webcommander.constants.DomainConstants
import com.webcommander.models.AddressData
import com.webcommander.plugin.sendle.communicator.SendleCommunicator
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product

class SendleController {

    def loadConfig() {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SENDLE);
        render(view: "/plugins/sendle/admin/appConfig", model: [config: config])
    }

    def pingServer(){
        render SendleCommunicator.ping()
    }

    def getCost(){
        AddressData senderAddress = new AddressData(StoreDetail.first().address)
        AddressData receiverAddress = new AddressData(StoreDetail.first().address)
        Double weight = 1.0
        Double volume = 0.1
        Integer noOfItem = 1
        render SendleCommunicator.calculateShippingCost(senderAddress, receiverAddress, weight, volume, noOfItem)
    }

    def createOrder(){
        AddressData receiver = new AddressData(StoreDetail.first().address)
        render SendleCommunicator.placeOrder(receiver, Product.first(), "2018-06-29", "LALALA", "KAKAKA")
    }

}
