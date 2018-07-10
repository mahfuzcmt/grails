package com.webcommander.plugin.wish_list.controllers.rest

import com.webcommander.admin.Customer
import com.webcommander.authentication.annotations.RequiresCustomer
import com.webcommander.plugin.wish_list.WishList
import com.webcommander.plugin.wish_list.WishListItem
import com.webcommander.plugin.wish_list.WishListService
import com.webcommander.rest.throwable.ApiException
import com.webcommander.util.AppUtil
import com.webcommander.util.RestProcessor
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import grails.converters.JSON

class ApiWishlistController extends RestProcessor{
    ProductService productService
    WishListService wishListService

    @RequiresCustomer
    def list() {
        Integer max = params.max ?: -1
        Integer offset = params.offset ?: 0
        List<WishList> wishLists = WishList.createCriteria().list([max: max, offset: offset]) {
            eq("customer.id", AppUtil.loggedCustomer)
        };
        rest([wishLists: wishLists], [marshallerExclude: ["wishListItems", "emails"]])
    }

    @RequiresCustomer
    def products() {
        Customer customer = Customer.get(AppUtil.loggedCustomer)
        WishList wishList = WishList.findByCustomerAndId(customer, params.long("id"))
        if (!wishList) {
            throw new ApiException("wish.list.not.found", 404)
        }
        def productIds = wishList.wishListItems.collect { it.productId };
        List<Product> products = productService.filterAvailableProducts(productIds, [rawProduct: true])
        rest([products: products], [marshallerExclude: ["relatedProducts", "availableToCustomerGroups", "availableToCustomers", "inventoryAdjustments", "createdBy", "videos"]])
    }


    @RequiresCustomer
    def create() {
       WishList result = wishListService.save(params, Customer.get(AppUtil.loggedCustomer))
       if(result) {
           rest([status: "success", id: result.id])
       } else {
           throw new ApiException("wish.list.${params.id ? "update" : "save"}.failure")
       }
    }

    @RequiresCustomer
    def delete() {
        if(wishListService.remove(params.long("id"))) {
            render([status: "success", message: g.message(code: "wish.list.delete.success")] as JSON)
        } else {
            render([status: "success", message: g.message(code: "wish.list.delete.failure")] as JSON)
        }
    }

    @RequiresCustomer
    def addProduct() {
        Long productId = params.long("productId")
        Product product = Product.get(productId)
        WishList wishList = WishList.findByCustomerAndId(Customer.get(AppUtil.loggedCustomer), params.long("wishListId"))
        wishListService.addToWishList(wishList, product)
        rest(status: "success")
    }

    @RequiresCustomer
    def removeProduct() {
        Long productId = params.long("productId")
        Product product = Product.get(productId)
        WishList wishList = WishList.findByCustomerAndId(Customer.get(AppUtil.loggedCustomer), params.long("wishListId"))
        WishListItem item = WishListItem.findByWishListAndProduct(wishList, product)
        wishListService.removeItem(item)
        rest(status: "success")
    }

    @RequiresCustomer
    def share() {
        WishList wishList = WishList.findByCustomerAndId(Customer.get(AppUtil.loggedCustomer), params.long("id"));
        List<Map> contacts = params.contacts
        Boolean result = wishListService.share(wishList, contacts.name, contacts.email, params.comment)
        if (result) {
            rest([status: "success"])
        } else {
            throw new ApiException("wish.list.share.failed")
        }
    }
}
