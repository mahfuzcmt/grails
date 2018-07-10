package com.webcommander.plugin.wish_list

import com.webcommander.ApplicationTagLib
import com.webcommander.admin.Customer
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommanderMailService
import com.webcommander.common.Email
import com.webcommander.events.AppEventManager
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import org.grails.plugins.web.taglib.RenderTagLib
import grails.gorm.transactions.Transactional
import grails.util.TypeConvertingMap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@Initializable
@Transactional
class WishListService {
    ProductService productService
    CommanderMailService commanderMailService
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.RenderTagLib")
    RenderTagLib g

    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    ApplicationTagLib app

    static void initialize() {
        AppEventManager.on("before-customer-delete", { id ->
            Customer customerObj = Customer.get(id)
            List list = WishList.findAllByCustomer(customerObj)
            if(list) {
                list*.delete()
            }
        })

        AppEventManager.on("product-put-in-trash before-product-delete", { id->
            Product product = Product.get(id)
            List items = WishListItem.findAllByProduct(product)
            items*.delete()
        })
    }

    WishListItem addToWishList(WishList wishList, Product product) {
        WishListItem wishListItem = wishList.wishListItems.find {
            it.product.id == product.id
        };
        if(!wishListItem) {
            wishListItem = new WishListItem();
            wishListItem.product = product;
            wishList.addToWishListItems(wishListItem);
        } else {
            throw new ApplicationRuntimeException(g.message(code: "wish.list.already.added", args: [product.name]))
        }
        return wishListItem
    }

    WishList save(TypeConvertingMap params, Customer customer) {
        WishList list
        if(params.id){
            list = WishList.findByIdAndCustomer(params.long("id"), customer)
        } else {
            list = new WishList();
        }
        list.name = params.name;
        list.customer = customer;
        list.save()
        return list.hasErrors() ? null : list
    }

    Boolean remove(Long id) {
        WishList wishList = WishList.findByCustomerAndId(Customer.get(AppUtil.loggedCustomer), id)
        wishList.emails*.delete();
        wishList.delete()
        return true
    }

    Boolean share(WishList wishList, List names, List emails, String comment) {
        emails.eachWithIndex {def it, def i ->
            Email email = new Email(name: names[i], email: it)
            email.save();
            wishList.addToEmails(email);
        }
        sendShareMail(wishList, emails.join(","), names.findAll { return  it }.join(", ") , comment);
        wishList.save();
        return !wishList.hasErrors()
    }

    Boolean share(TypeConvertingMap params) {
        return share(WishList.get(params.id), params.list("names[]"), params.list("emails[]"), params.comment)
    }

    void sendShareMail(WishList wishList, String recipient, String to, String comment) {
        def productIds = wishList.wishListItems.collect { it.productId };
        def productList = productService.getProductData(productIds, [:])
        def content =  g.include(view: "/plugins/wish_list/site/shareMail.gsp", model: [productList: productList.subList(0, productList.size() >= 3 ? 3 : productList.size())]).toString();
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("wish-list-share")
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "wish_list_name":
                    refinedMacros[it.key] = wishList.name
                    break;
                case "wish_list_link":
                    refinedMacros[it.key] = app.baseUrl() + "wishlist/products/" + wishList.id;
                    break;
                case "content":
                    refinedMacros[it.key] = content;
                    break;
                case "customer_first_name":
                    refinedMacros[it.key] = wishList.customer.firstName;
                    break;
                case "customer_last_name":
                    refinedMacros[it.key] = wishList.customer.lastName;
                    break;
                case "to_name":
                    refinedMacros[it.key] = to;
                    break;
                case "comment":
                    refinedMacros[it.key] = comment.encodeAsBMHTML();
                    break;
            }
        }
        commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, recipient)
    }

    Boolean removeItem(Map params) {
        Long id = params.long("id");
        WishListItem item = WishListItem.get(id)
        return removeItem(item)
    }

    Boolean removeItem(WishListItem item) {
        if(!item) {
            throw new ApplicationRuntimeException("item.not.found")
        }
        item.delete();
        return true;
    }
}
