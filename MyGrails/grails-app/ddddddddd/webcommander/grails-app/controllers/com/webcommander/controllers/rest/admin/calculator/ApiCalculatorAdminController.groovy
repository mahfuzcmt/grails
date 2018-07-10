package com.webcommander.controllers.rest.admin.calculator

import com.webcommander.admin.Customer
import com.webcommander.manager.CartManager
import com.webcommander.models.Cart
import com.webcommander.rest.throwable.ApiException
import com.webcommander.util.RestProcessor
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService

class ApiCalculatorAdminController extends RestProcessor{
    ProductService productService

    def calculateTaxAndDiscount() {
        List<Map> items = [];
        Customer customer = Customer.get(params.customerId)
        if(!params.items instanceof List) {
            throw new ApiException("invalid.arguments")
        }
        params.items.each {
            Map item = new HashMap(it)
            Product product = Product.get(it.id)
            item.price = productService.getProductData(product).effectivePrice
            items.add(item)
        }
        Cart cart = CartManager.createCartForAdminOrder(items, [
            shippingAddress: customer.activeShippingAddress,
            billingAddress: customer.activeBillingAddress
        ]);
        Map response = [:]
        cart.cartItemList.each {
            response[it.object.id + ""] = [
                id: it.object.id,
                quantity: it.quantity,
                unitTax: it.unitTax,
                tax: it.tax,
                discount: it.discount
            ]
        }
        rest(response)
    }
}
