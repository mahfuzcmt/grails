package com.webcommander.plugin.reorder

import com.webcommander.models.ProductData
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService

class ReorderTagLib {
    ProductService productService
    static namespace = "reorder"

    def addedProduct = { attr, body ->
        out << body()
        Map params = attr.params;
        Order order = Order.get(params.orderId)
        Integer notAvailableCount = 0
        if(order && order.customer) {
            List quantities = []
            List names = []
            List<ProductData> products = []
            order.items.each {
                if(it.productType == "product") {
                    ProductData data = productService.getProductData(Product.get(it.productId), [orderItemId: it.id])
                    Integer availableQuantity;
                    if(data && data.isAvailable && (availableQuantity = data.isInventoryEnabled && (it.quantity > data.availableStock) ? data.availableStock : it.quantity)) {
                        quantities.push(availableQuantity)
                        names.push(data.name + (it.variations ? "(${it.variations.join(", ").encodeAsBMHTML()})" : ""))
                        products.push(data)
                    } else {
                        notAvailableCount++
                    }
                }
            }
            out << g.include(view: "/plugins/reorder/admin/addedProduct.gsp", model: [products: products, quantities: quantities, names: names, notAvailableCount: notAvailableCount])
        }
    }
}