package com.webcommander.plugin.discount.validator

import com.webcommander.admin.Customer
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.ResponseCodes
import com.webcommander.models.Cart
import com.webcommander.plugin.discount.Constants
import com.webcommander.plugin.discount.DiscountService
import com.webcommander.plugin.discount.util.DiscountDataUtil
import com.webcommander.plugin.discount.webcommerce.CustomDiscount
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product
import grails.util.Holders

/**
 * Created by sharif ul islam on 14/03/2018.
 */
class DiscountProfileValidator implements Validator {

    private static DiscountService _discountService

    private DiscountService getDiscountService() {
        return _discountService ?: (_discountService = Holders.applicationContext.getBean("discountService"))
    }

    boolean validate;

    @Override
    Map<String, Object> validate(Map<String, Object> context) {

        Map response = [:]
        Map validationMessage = [:]

        try {

            Cart cart = context.cart

            CustomDiscount discount = context.get("discount")
            Long productId = context.get("productId")
            Product selectedProduct
            List<Product> products = DiscountDataUtil.getAllProducts(discount)
            List<Customer> customers = DiscountDataUtil.getAllCustomers(discount)

            setValidate(true)
            String message = null

            if (productId) {
                selectedProduct = Product.get(productId)
            }

            if (!discount.assoc.isAppliedAllProduct && productId && ( !discount.discountDetailsType.equals(Constants.DETAILS_TYPE.PRODUCT) )) {
                def includeProduct = products.find { it.id == productId }
                if (!includeProduct) {
                    setValidate(false)
                    message = "E001"
                    validationMessage.put("productId", message)
                }

            }

            if (!discount.assoc.isAppliedAllProduct
                    && (discount.discountDetailsType.equals(Constants.DETAILS_TYPE.AMOUNT)
                        || discount.discountDetailsType.equals(Constants.DETAILS_TYPE.SHIPPING)
                    || discount.discountDetailsType.equals(Constants.DETAILS_TYPE.PRODUCT)
                        )
                    && cart) {
                boolean found = false
                cart.cartItemList.each { item ->
                    Product prod = products.find() { it.id == item.object.product.id }
                    if (prod) {
                        found = true
                        return true
                    }
                }
                if (!found) {
                    setValidate(false)
                    message = "E001"
                    validationMessage.put("productId", message)
                }
            }

            Date currentDate = new Date().gmt()
            if(!discount.isActive) {
                setValidate(false)
                message = "E200"
                validationMessage.put("isActive", message)
            }

            if(discount.startFrom && currentDate < discount.startFrom) {
                setValidate(false)
                message = "E201"
                validationMessage.put("startFrom", message)
            }

            if(discount.isSpecifyEndDate && (discount.startTo && currentDate > discount.startTo)) {
                setValidate(false)
                message = "E202"
                validationMessage.put("startTo", message)
            }

            if (discount.isMaximumUseTotal && (discountService.getDiscountUsageCount([discountId: discount.id]) >= discount.maximumUseCount)) {
                setValidate(false)
                message = "E203"
                validationMessage.put("maximumUseCount", message)
            }

            if (AppUtil.loggedCustomer && !discount.assoc.isAppliedAllCustomer) {
                def customer = customers.find { it.id == AppUtil.loggedCustomer }
                if (!customer) {
                    setValidate(false)
                    message = "E204"
                    validationMessage.put("customer", message)
                } else if (discount.isMaximumUseCustomer && (discountService.getDiscountUsageCount([discountId: discount.id, customerId: AppUtil.loggedCustomer]) >= discount.maximumUseCustomerCount)) {
                    setValidate(false)
                    message = "E205"
                    validationMessage.put("maximumUseCustomerCount", message)
                }
            }

            if (discount.isExcludeProductsOnSale) {
                boolean found = false
                cart.cartItemList.each { item ->
                    Product prod = products.find() { it.id == item.object.product.id }
                    if (prod && !prod.isOnSale) {
                        found = true
                        return true
                    }
                }
                if (!found) {
                    setValidate(false)
                    message = "E206"
                    validationMessage.put("excludeProducts", message)
                }
            }

            if (!AppUtil.loggedCustomer && !discount.assoc.isAppliedAllCustomer) {
                setValidate(false)
                message = "E207"
                validationMessage.put("customer", message)
            }

            if (!isValidate()) {
                response.put(DomainConstants.RESPONSE_CODE, ResponseCodes.BAD_REQUEST)
                response.put(DomainConstants.RESPONSE_MESSAGE, "Discount Profile Validation Failed...!")
            } else {
                response.put(DomainConstants.RESPONSE_CODE, ResponseCodes.SUCCESS_CODE)
            }

        } catch (Exception e) {
            e.printStackTrace()

            response.put(DomainConstants.RESPONSE_CODE, ResponseCodes.INTERNAL_SERVER_ERROR_CODE)
            response.put(DomainConstants.RESPONSE_MESSAGE, "Discount Profile Validation Failed...!")

            return response
        }

        response.put("validationMessage", validationMessage)

        return response

    }

}
