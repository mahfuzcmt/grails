package com.webcommander.models

import com.webcommander.AppResourceTagLib
import com.webcommander.beans.SiteMessageSource
import com.webcommander.calculator.TaxCalculator
import com.webcommander.common.Resource
import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import com.webcommander.util.NumberUtil
import com.webcommander.util.SortAndSearchUtil
import com.webcommander.util.TemplateMatcher
import com.webcommander.webcommerce.Currency
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import com.webcommander.webcommerce.ShippingProfile
import com.webcommander.webcommerce.TaxCode
import com.webcommander.webcommerce.TaxProfile
import grails.util.Holders
import org.springframework.beans.BeanUtils

class ProductData {

    private static ProductService _productService
    private static SiteMessageSource _siteMessageSource
    private static AppResourceTagLib _appResource

    private static AppResourceTagLib getAppResource() {
        return _appResource ?: (_appResource = Holders.grailsApplication.mainContext.getBean(AppResourceTagLib))
    }

    private ProductService getProductService() {
        return _productService ?: (_productService = Holders.applicationContext.getBean("productService"))
    }

    private static getSiteMessageSource() {
        return _siteMessageSource?: (_siteMessageSource = Holders.applicationContext.getBean(SiteMessageSource))
    }

    Long id
    Long parentId
    Long taxCodeId

    String sku
    String name
    String taxMessage;
    String title
    String heading
    String image;
    String altText;
    String url;
    String model
    String summary
    String description
    String spec
    String specUrl
    String specUrlInfix
    String productFile
    String productType
    String productCondition
    String calculatedRestrictPriceFor
    String calculatedRestrictPurchaseFor

    Integer availableStock;
    Integer lowStockLevel = 0
    Integer minOrderQuantity = 1;
    Integer maxOrderQuantity;
    Integer multipleOfOrderQuantity
    Integer supportedMinOrderQuantity
    Integer supportedMaxOrderQuantity
    Integer decimalPoints

    Double basePrice;
    Double costPrice;
    Double salePrice;
    Double expectToPayPrice
    Double expectToPayPriceWithTax
    Double effectivePrice
    Double priceToDisplay
    Double previousPriceToDisplay
    Double tax = 0.0;
    TaxCode exitTaxCode
    Double weight
    Double height
    Double length
    Double width
    Double actualBasePrice
    Double actualPriceToDisplay

    Boolean isOnSale;
    Boolean isNew;
    Boolean isFeatured;
    Boolean isActive;
    Boolean isInTrash;
    Boolean isExpectToPay;
    Boolean isAvailable = true;
    Boolean isCallForPriceEnabled;
    Boolean isInventoryEnabled;
    Boolean isMultipleOrderQuantity;
    Boolean isVirtual
    Boolean isCombined
    Boolean isCombinationPriceFixed
    Boolean isCombinationQuantityFlexible
    Boolean isTaxCodeFound
    Boolean isStoreActualInfo = true

    List images = []
    List videos = []
    List metaTags = []

    List<Long> calculatedRestrictPriceExceptCustomers = []
    List<Long> calculatedRestrictPriceExceptCustomerGroups = []
    List<Long> calculatedRestrictPurchaseExceptCustomers = []
    List<Long> calculatedRestrictPurchaseExceptCustomerGroups = []

    Map attrs = [:]

    ProductData(Product product, Map config = null) {
        BeanUtils.copyProperties(product, this, "class", "metaClass", "images", "videos", "metaTags", "calculatedRestrictPriceExceptCustomers", "calculatedRestrictPriceExceptCustomerGroups", "calculatedRestrictPurchaseExceptCustomers", "calculatedRestrictPurchaseExceptCustomerGroups");
        addImage(product.images)
        addVideos(product.videos)
        if(product.spec) {
            spec = product.spec.name
            specUrl = product.spec.baseUrl
            specUrlInfix = appResource.getProductSpecInfix(product.spec.getTenantId(),product.id)
        }
        if(product.productFile) {
            productFile = product.productFile.name
        }
        if(product.metaTags) {
            metaTags = []
            product.metaTags.each {
                metaTags.push([name: it.name, value: it.value])
            }
        }

        Currency currency
        try {
            currency = AppUtil.siteCurrency
        } catch (Exception ex) {
            currency = AppUtil.baseCurrency
        }
        decimalPoints = currency.decimalPoints

        setCalculatedFiled(product)
        this.parentId = product.parentId

    }

    private adjustBasePrice() {

        this.basePrice = getCalculatedActualBasePrice()
        this.salePrice = getCalculatedActualSalePrice()

    }

    Double getCalculatedActualBasePrice() {

        Map taxConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX)

        Boolean priceWithTax = taxConfig.is_price_with_tax.toBoolean()
        String basePriceRounding = taxConfig.base_price_rounding
        Double actualBasePrice = this.basePrice
        if (priceWithTax && taxConfig.default_tax_code) {

            TaxCode defaultTaxCode = TaxCode.findByName(taxConfig.default_tax_code.toString())

            actualBasePrice -= defaultTaxCode.rate * actualBasePrice / (100 + defaultTaxCode.rate)
            if (basePriceRounding) {
                actualBasePrice = NumberUtil.getFormatedDouble(actualBasePrice, basePriceRounding, decimalPoints)
            }

        }
        return actualBasePrice
    }

    Double getCalculatedActualSalePrice() {

        Map taxConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX)

        Boolean priceWithTax = taxConfig.is_price_with_tax.toBoolean()
        String basePriceRounding = taxConfig.base_price_rounding
        Double actualSalePrice = this.salePrice
        if (priceWithTax && taxConfig.default_tax_code && isOnSale) {

            TaxCode defaultTaxCode = TaxCode.findByName(taxConfig.default_tax_code.toString())

            actualSalePrice -= defaultTaxCode.rate * actualSalePrice / (100 + defaultTaxCode.rate)
            if (basePriceRounding) {
                actualSalePrice = NumberUtil.getFormatedDouble(actualSalePrice, basePriceRounding, decimalPoints)
            }
        }
        return actualSalePrice
    }

    private adjustTaxValues() {
        Map taxConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX);
        Map taxMap = TaxCalculator.getTaxDetailForProduct(this);
        isTaxCodeFound = taxMap.isTaxCodeFound
        taxCodeId = taxMap.taxCodeId
        if(isOnSale) {
            effectivePrice = salePrice
            tax = taxMap.saleAmount
        } else {
            effectivePrice = basePrice
            tax = taxMap.baseAmount
        }
        taxMessage = null
        if(taxMap.code) {
            String message = taxConfig.tax_message;
            exitTaxCode = taxMap.taxCode
            if(taxConfig.show_price_with_tax.toBoolean(false) && message) {
                TemplateMatcher engine = new TemplateMatcher("%", "%")
                taxMessage = engine.replace(siteMessageSource.convert(message), [code: taxMap.code, rate: taxMap.rate + " %", amount: tax.toPrice()]);
            }
        } else {
            taxMessage = "no.product.taxcode"
        }

        priceToDisplay = taxConfig.show_price_with_tax.toBoolean() ? effectivePrice + tax : effectivePrice;
        Double expectedBase = isExpectToPay ? expectToPayPrice : basePrice
        Double expectedTax = isExpectToPay ? taxMap.expectAmount : taxMap.baseAmount
        if(isExpectToPay || isOnSale) {
            previousPriceToDisplay = taxConfig.show_price_with_tax.toBoolean() ? expectedBase + expectedTax : expectedBase;
        } else {
            previousPriceToDisplay = null
        }

        expectToPayPriceWithTax = expectedBase + expectedTax

        if (isStoreActualInfo) {
            actualBasePrice = effectivePrice
            //actualPriceToDisplay = priceToDisplay
            actualPriceToDisplay = effectivePrice + tax
        }
    }

    ProductData updatePrice() {
        adjustTaxValues()
        return this
    }

    ProductData calculatePrice() {
        adjustBasePrice()
        updatePrice()
        return this
    }

    public void addImage(List imageList) {
        if(imageList.size() > 0) {
            image = imageList[0].name;
            altText = imageList[0].altText;
            List tmpImages = []
            imageList.eachWithIndex { image, idx ->
                tmpImages.add([
                    idx: image.idx,
                    name: image.name,
                    altText: image.altText,
                    baseUrl: image.getBaseUrl(),
                    urlInfix: image.findUrlInfix()
                ])
            }
            images = tmpImages
        }
    }

    public void addVideos(List videoList) {
        if(videoList.size() > 0) {
            List tmpVideos = []
            videoList.eachWithIndex { video, idx ->
                tmpVideos.add([idx: video.idx, name: video.name, title: video.title, description: description, urlInfix: video.findUrlInfix()])
            }
            videos = tmpVideos
        }
    }

    void setCalculatedFiled(Product product) {
        if(product.isCombined) {
            Double price = productService.getCombinationPrice(product)
            if(price) {
                isOnSale = false
                basePrice = price
                isExpectToPay = false
            }
        }
        supportedMinOrderQuantity =  isMultipleOrderQuantity ? multipleOfOrderQuantity : (minOrderQuantity ?: 1);
        supportedMaxOrderQuantity = maxOrderQuantity
        boolean considerStock = isInventoryEnabled && AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "order_quantity_over_stock") != DomainConstants.OUT_OF_STOCK_MESSAGE_TYPE.SELL_AWAY
        if(considerStock && (!supportedMaxOrderQuantity || supportedMaxOrderQuantity > availableStock)) {
            supportedMaxOrderQuantity = availableStock
        }
        if(isMultipleOrderQuantity) {
            int counter = 2;
            while(supportedMinOrderQuantity < minOrderQuantity && (!maxOrderQuantity || supportedMinOrderQuantity < maxOrderQuantity)) {
                supportedMinOrderQuantity = counter++ * multipleOfOrderQuantity
            }
            if(maxOrderQuantity) {
                Integer tentativeMax = supportedMinOrderQuantity
                while(tentativeMax <= maxOrderQuantity) {
                    supportedMaxOrderQuantity = tentativeMax;
                    tentativeMax = counter++ * multipleOfOrderQuantity
                }
            }
        }

        if(product.calculatedRestrictPriceFor == DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.EXCEPT_SELECTED) {
            this.calculatedRestrictPriceExceptCustomers = product.calculatedRestrictPriceExceptCustomers.id
            this.calculatedRestrictPriceExceptCustomerGroups = product.calculatedRestrictPriceExceptCustomerGroups.id
        }
        if(product.calculatedRestrictPurchaseFor == DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.EXCEPT_SELECTED) {
            this.calculatedRestrictPurchaseExceptCustomers = product.calculatedRestrictPurchaseExceptCustomers.id
            this.calculatedRestrictPurchaseExceptCustomerGroups = product.calculatedRestrictPurchaseExceptCustomerGroups.id
        }

    }

    String getLink() {
        return "product/" + url
    }

    String getImageLink(String imageSize = "", Integer index = null) {
        String originalSize = imageSize
        imageSize = imageSize ? (imageSize + "-") : ""
        return image ? (images[0].baseUrl + images[0].urlInfix + imageSize + (index !=null ? images[index]?.name : image)) : appResource.getProductDefaultImageWithPrefix(originalSize)
    }

    ShippingProfile resolveShippingProfile() {
        Product product = Product.get(id)
        def resolver = product;
        ShippingProfile profile;
        while(!profile && resolver) {
            profile = resolver.shippingProfile
            resolver = resolver.parent
        }
        if(!profile) {
            return ShippingProfile.get(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "default_shipping_profile").toLong(0))
        }
        return profile;
    }

    TaxProfile resolveTaxProfile() {
        Product product = Product.get(id)
        def profileResolver = product
        TaxProfile profile

        String configurationType = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "configuration_type");
        if (configurationType && configurationType.equals(DomainConstants.TAX_CONFIGURATION_TYPE.MANUAL)) {
            while(!profile && profileResolver) {
                profile = profileResolver.taxProfile;
                profileResolver = profileResolver.parent;
            }
        }

        if(!profile) {
            profile = TaxProfile.get(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "default_tax_profile").toLong(0));
        }
        return profile;
    }

    Boolean isPriceRestricted(Long customerId, List<Long> customerGroupIds) {
        if(this.calculatedRestrictPriceFor == DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.NONE || (this.calculatedRestrictPriceFor == DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.EXCEPT_CUSTOMER && customerId)) {
            return false
        }
        if(this.calculatedRestrictPriceFor == DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.EVERYONE || this.calculatedRestrictPriceFor != DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.EXCEPT_SELECTED || !customerId) {
            return true
        }
        if(customerGroupIds) {
            int len = customerGroupIds.size()
            for (int i = 0; i < len; i ++) {
                if (SortAndSearchUtil.binarySearch(this.calculatedRestrictPriceExceptCustomerGroups, customerGroupIds[i]) != -1) {
                    return false
                }
            }
        }
        if (SortAndSearchUtil.binarySearch(this.calculatedRestrictPriceExceptCustomers, customerId) != -1) {
            return false
        }
        return true
    }

    Boolean isPurchaseRestricted(Long customerId, List<Long> customerGroupIds) {
        if(this.calculatedRestrictPurchaseFor == DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.NONE || (this.calculatedRestrictPurchaseFor == DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.EXCEPT_CUSTOMER && customerId)) {
            return false
        }
        if(this.calculatedRestrictPurchaseFor == DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.EVERYONE || this.calculatedRestrictPurchaseFor != DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.EXCEPT_SELECTED || !customerId) {
            return true
        }
        if(customerGroupIds) {
            int len = customerGroupIds.size()
            for (int i = 0; i < len; i ++) {
                if (SortAndSearchUtil.binarySearch(this.calculatedRestrictPurchaseExceptCustomerGroups, customerGroupIds[i]) != -1) {
                    return false
                }
            }
        }
        if (SortAndSearchUtil.binarySearch(this.calculatedRestrictPurchaseExceptCustomers, customerId) != -1) {
            return false
        }
        return true
    }

    Boolean isPriceOrPurchaseRestricted(Long customerId, List<Long> customerGroupIds) {
        return isPriceRestricted(customerId, customerGroupIds) || isPurchaseRestricted(customerId, customerGroupIds)
    }

    void populateSpecInfo(Resource specInfo, Long id) {
        if (specInfo) {
            spec = specInfo.name
            specUrl = specInfo.baseUrl
            specUrlInfix = appResource.getProductSpecInfix(specInfo.getTenantId(), id)
        }
    }
}
