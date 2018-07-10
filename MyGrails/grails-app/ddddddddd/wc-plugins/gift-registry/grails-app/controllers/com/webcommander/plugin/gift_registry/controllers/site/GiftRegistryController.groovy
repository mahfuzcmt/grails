package com.webcommander.plugin.gift_registry.controllers.site

import com.webcommander.admin.AdministrationService
import com.webcommander.admin.ConfigService
import com.webcommander.admin.Country
import com.webcommander.admin.Customer
import com.webcommander.authentication.annotations.AutoGeneratedPage
import com.webcommander.authentication.annotations.RequiresCustomer
import com.webcommander.constants.DomainConstants
import com.webcommander.models.ProductData
import com.webcommander.parser.EmailTemplateParser
import com.webcommander.plugin.gift_registry.GiftRegistry
import com.webcommander.plugin.gift_registry.GiftRegistryItem
import com.webcommander.plugin.gift_registry.GiftRegistryService
import com.webcommander.plugin.gift_registry.models.GiftRegistryProductData
import com.webcommander.throwables.CartManagerException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Address
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import grails.converters.JSON
import grails.web.databinding.DataBindingUtils

class GiftRegistryController {
    ConfigService configService
    AdministrationService administrationService
    GiftRegistryService giftRegistryService
    ProductService productService

    def add() {
        if (!session.customer) {
            render([status: "error", url: "customer/login"] as JSON);
            return;
        }
        Customer customer = Customer.get(session.customer);
        Integer count = GiftRegistry.countByCustomer(customer);
        Boolean success;
        String error;
        List errorArgs;
        GiftRegistryItem giftItem;
        Long productId = params.long("productId");
        Integer quantity = params.int("quantity")
        Product product = Product.get(productId)
        ProductData productData = productService.getProductData(product, params.config)
        boolean  requiresGiftRegistry = false
        if(!params.giftRegistry) {
            requiresGiftRegistry = true;
        }
        String popupTitle
        if (count == 0 || requiresGiftRegistry) {
             if (count == 0){
                 popupTitle = "create.gift.registry"
             } else {
                 popupTitle = "select.gift.registry"
             }
            Map model = [popupTitle: popupTitle, product: product, productData: productData, quantity: quantity, requiresGiftRegistry: requiresGiftRegistry, customer: customer, count: count]
            params.remove("controller")
            params.remove("action")

            def html = g.include(view:  "/plugins/gift_registry/site/popup.gsp", model: model).toString();
            render([status: "success", html: html] as JSON)

        }
        if(!params.giftRegistry) {
            return;
        }
        Closure repeatAdd
        String warning
        Integer added
        repeatAdd = { _quantity ->
            try {
                giftItem = giftRegistryService.addToGiftRegistry(product, productData, _quantity, params);
                success = true;
                added = _quantity
            } catch(CartManagerException ex) {
                error = ex.message;
                if(error == "ADD_AVAILABLE") {
                    Integer addedQuantity = giftRegistryService.getAddedQuantity(productData, params)
                    String message = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "add_available_message");
                    error = null
                    Integer remainingToAdd = ex.messageArgs[0] - addedQuantity
                    Map replacerMap = [
                            requested_quantity: quantity,
                            available_quantity: remainingToAdd
                    ]
                    message = site.message(code: message)
                    warning = EmailTemplateParser.parse(message, replacerMap)
                    if(remainingToAdd == 0) {
                        giftItem = new GiftRegistryItem()
                        success = true;
                        added = 0
                    } else {
                        repeatAdd remainingToAdd
                    }
                } else {
                    success = false;
                    giftItem = new GiftRegistryItem()
                    errorArgs = ex.messageArgs
                }
            }
        }
        repeatAdd quantity
        GiftRegistry giftRegistry = GiftRegistry.get(params.giftRegistry);
        if(error) {
            error = site.message(code: error)
            if(errorArgs) {
                Map replacerMap = [
                    requested_quantity: errorArgs[0],
                    multiple_of_quantity: errorArgs[0],
                    maximum_quantity: errorArgs[0],
                    minimum_quantity: errorArgs[0]
                ]
                error = EmailTemplateParser.parse(error, replacerMap)
            }
        }

        def html = g.include(view:  "/plugins/gift_registry/site/addToGiftRegistry.gsp", model: [
                success: success, giftItem: giftItem, productData: productData,
                quantity: success ? added : quantity, warningMessage: warning,
                errorMessage: error, giftRegistry: giftRegistry,
                totalItem: giftRegistry?.giftItems?.size()
        ]).toString();
        render([status: "success", html: html] as JSON)
    }

    @RequiresCustomer
    def loadGiftRegistry() {
        Customer customer = Customer.get(session.customer)
        List<GiftRegistry> giftRegistries = GiftRegistry.findAllByCustomer(customer)
        render(template: "/plugins/gift_registry/site/loadGiftRegistry", model: [giftRegistries: giftRegistries])
    }

    @RequiresCustomer
    def edit() {
        GiftRegistry giftRegistry = params.id ? GiftRegistry.get(params.id) : new GiftRegistry();
        Map fieldsConfigs = (Map)AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES["SHIPPING_ADDRESS_FIELD"])
        Country country = giftRegistry.address?.country ?: Country.get(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, 'default_country').toLong(0))
        def states = administrationService.getStatesForCountry(country.id)
        List<String> sortedFields = configService.getSortedFields(fieldsConfigs)
        List<String> activeFields = configService.getActiveFields(sortedFields, fieldsConfigs)
        render(view: "/plugins/gift_registry/site/infoEdit", model: [giftRegistry: giftRegistry,
                                                   fields: activeFields, fieldsConfigs: fieldsConfigs, address: giftRegistry.address ?: new Address(), country: country, states: states]);
    }

    @RequiresCustomer
    def save() {
        Boolean result  = giftRegistryService.save(params);
        if (result) {
            render([status: "success", message: g.message(code: "gift.registry.${params.id ? "update" : "save"}.success")] as JSON)
        } else {
            render([status: "success", message: g.message(code: "gift.registry.${params.id ? "update" : "save"}.failure")] as JSON)
        }
    }

    @RequiresCustomer
    def remove() {
        Boolean result  = giftRegistryService.remove(params.long("id"));
        if (result) {
            render([status: "success", message: g.message(code: "gift.registry.delete.success")] as JSON)
        } else {
            render([status: "success", message: g.message(code: "gift.registry.delete.failure")] as JSON)
        }
    }

    @RequiresCustomer
    def viewItems() {
        GiftRegistry registry = GiftRegistry.get(params.id);
        render(view:  "/plugins/gift_registry/site/viewItems", model: [registry: registry]);
    }

    @RequiresCustomer
    def status() {
        GiftRegistry registry = GiftRegistry.get(params.id);
        render(view:  "/plugins/gift_registry/site/status", model: [registry: registry]);
    }

    @RequiresCustomer
    def removeItem() {
        Boolean result  = giftRegistryService.removeItem(params.long("id"));
        if (result) {
            render([status: "success", message: g.message(code: "gift.registry.item.delete.success")] as JSON)
        } else {
            render([status: "success", message: g.message(code: "gift.registry.item.delete.failure")] as JSON)
        }
    }

    @RequiresCustomer
    def editShare() {
        GiftRegistry registry = GiftRegistry.get(params.id);
        if (registry.giftItems.size() == 0) {
            render([status: "error", message: g.message(code: "no.product.added.to.gift.registry")] as JSON)
            return;
        }
        def html = g.include(view: "/plugins/gift_registry/site/editShare.gsp", model: [registry: registry, id: params.id]);
        render([status: "success", html: html.toString()] as JSON)
    }

    @RequiresCustomer
    def share() {
        if (giftRegistryService.share(params)) {
            render([status: "success", message: g.message(code: "gift.registry.share.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "gift.registry.share.failure")] as JSON)
        }
    }

    @AutoGeneratedPage("gift.registry")
    def details() {
        request.gift_registry_page = true
        GiftRegistry registry = GiftRegistry.get(params.id);
        if(!registry){
            response.setStatus(404)
            forward(controller: "exception", action: "handle404")
            return
        }
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GIFT_REGISTRY)
        def productIds = registry.giftItems.collect { it.product.id };
        Map filterMap = [:];
        config["product-sorting"] = params["prwd-sort"]
        config["product_listing_id"] = "gift-registry-product-listing"
        filterMap["product-sorting"] = params["prwd-sort"]
        int max = 10;
        int offset = filterMap['offset'] = params.int("prwd-offset") ?: 0;
        if(config["show-pagination"] == "none" || config["display-type"] == "scrollable") {
            filterMap['max'] = productIds.size()
        } else {
            filterMap['max'] = max = params.int("prwd-max") ?: (config["item-per-page"].toInteger(null) ?: 10)
        }
        Integer totalCount = productService.filterOutAvailableProductCount(productIds, filterMap)
        productIds = productService.filterAvailableProducts(productIds, filterMap)
        List<GiftRegistryItem> filteredGifts = registry.giftItems.findAll {
            return productIds.contains(it.product.id)
        }
        filteredGifts.sort {
            productIds.indexOf(it.product.id)
        }
        List productList = [];
        Integer i = 0;
        filteredGifts.each {
            Map conf = null
            if(it.variation) {
                conf = [variation: it.variation]
            }
            ProductData data = productService.getProductData(it.product, conf)
            if(data) {
                Map product = (Map) data.properties
                GiftRegistryProductData giftRegistryProductData = new GiftRegistryProductData(productService.getProduct(data.id), it)
                DataBindingUtils.bindObjectToInstance(giftRegistryProductData, data)
                giftRegistryProductData.name = product.name + (it.variations ? " ( ${it.variations.join(",")} )" : "")
                giftRegistryProductData.supportedMaxOrderQuantity = (data.supportedMaxOrderQuantity && data.supportedMaxOrderQuantity < it.remain) ? data.supportedMaxOrderQuantity : it.remain;
                productList.add(giftRegistryProductData)
            }
            if (++i == max) return false;
        }
        render(
                view: "/site/siteAutoPage",
                model: [
                        name: DomainConstants.AUTO_GENERATED_PAGES.GIFT_REGISTRY_DETAILS,
                        productList: productList, config: config, max: max, offset: offset, totalCount: totalCount,
                        registry: registry, giftItems: registry.giftItems,
                        macros: [GIFT_REGISTRY_NAME: registry.name],
                        view: "/plugins/gift_registry/site/giftRegistryPage.gsp"
                ]
        )
    }
}
