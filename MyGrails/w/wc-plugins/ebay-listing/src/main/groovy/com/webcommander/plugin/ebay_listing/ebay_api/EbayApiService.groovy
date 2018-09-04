package com.webcommander.plugin.ebay_listing.ebay_api

import com.webcommander.ApplicationTagLib
import com.webcommander.config.StoreDetail
import com.webcommander.plugin.ebay_listing.admin.webmarketing.EbayListingProfile
import com.webcommander.plugin.ebay_listing.admin.webmarketing.EbayMetaValue
import com.webcommander.plugin.ebay_listing.admin.webmarketing.EbayPaymentMethod
import com.webcommander.plugin.ebay_listing.admin.webmarketing.EbayPostage
import com.webcommander.plugin.ebay_listing.admin.webmarketing.EbayPricingProfile
import com.webcommander.plugin.ebay_listing.constants.DomainConstants
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Address
import com.webcommander.webcommerce.Product
import com.ebay.sdk.ApiContext
import com.ebay.sdk.ApiCredential
import com.ebay.sdk.ApiException
import com.ebay.sdk.call.AddItemCall
import com.ebay.sdk.call.GetCategoriesCall
import com.ebay.soap.eBLBaseComponents.AmountType
import com.ebay.soap.eBLBaseComponents.BuyerPaymentMethodCodeType
import com.ebay.soap.eBLBaseComponents.CategoryType
import com.ebay.soap.eBLBaseComponents.CountryCodeType
import com.ebay.soap.eBLBaseComponents.CurrencyCodeType
import com.ebay.soap.eBLBaseComponents.DetailLevelCodeType
import com.ebay.soap.eBLBaseComponents.GalleryTypeCodeType
import com.ebay.soap.eBLBaseComponents.ItemType
import com.ebay.soap.eBLBaseComponents.ListingTypeCodeType
import com.ebay.soap.eBLBaseComponents.PictureDetailsType
import com.ebay.soap.eBLBaseComponents.ProductListingDetailsType
import com.ebay.soap.eBLBaseComponents.ReturnPolicyType
import com.ebay.soap.eBLBaseComponents.ShippingDetailsType
import com.ebay.soap.eBLBaseComponents.ShippingServiceCodeType
import com.ebay.soap.eBLBaseComponents.ShippingServiceOptionsType
import com.ebay.soap.eBLBaseComponents.ShippingTypeCodeType
import com.ebay.soap.eBLBaseComponents.SiteCodeType
import grails.util.Holders

import javax.xml.bind.annotation.XmlEnumValue
import java.lang.annotation.Annotation
import java.lang.reflect.Field

class EbayApiService {

    public static final String EBAY_SANDBOX_API_URL = "https://api.sandbox.ebay.com/wsapi"
    public static final String EBAY_SANDBOX_EPS_URL = "https://api.sandbox.ebay.com/api.dll"
    public static final String EBAY_PRODUCTION_API_URL = "https://api.ebay.com/wsapi"
    public static final String EBAY_PRODUCTION_EPS_URL = "https://api.ebay.com/ws/api.dll"

    private static Map<String, String> ebay_sites = new HashMap<String, String>()
    private static ApplicationTagLib _app;
    private static ApplicationTagLib getApp() {
        return _app ?: (_app = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib))
    }
    public static ApiContext getApiContext(EbayListingProfile profile) {
        ApiContext apiContext = new ApiContext()
        apiContext.setSite(SiteCodeType.fromValue(profile.setting.ebaySite))
        ApiCredential cred = apiContext.getApiCredential()
        cred.seteBayToken(profile.setting.userToken)
        if (profile.setting.mode == "sandbox"){
            apiContext.setApiServerUrl(EBAY_SANDBOX_API_URL)
            apiContext.setEpsServerUrl(EBAY_SANDBOX_EPS_URL)
        } else {
            apiContext.setApiServerUrl(EBAY_PRODUCTION_API_URL)
            apiContext.setEpsServerUrl(EBAY_PRODUCTION_EPS_URL)
        }
        return apiContext
    }

    public static ApiContext getApiContext(Map config) {
        ApiContext apiContext = new ApiContext()
        apiContext.setSite(SiteCodeType.fromValue(config.ebay_site))
        ApiCredential cred = apiContext.getApiCredential()
        cred.seteBayToken(config.user_token)
        if (config.mode == "sandbox"){
            apiContext.setApiServerUrl(EBAY_SANDBOX_API_URL)
            apiContext.setEpsServerUrl(EBAY_SANDBOX_EPS_URL)
        } else {
            apiContext.setApiServerUrl(EBAY_PRODUCTION_API_URL)
            apiContext.setEpsServerUrl(EBAY_PRODUCTION_EPS_URL)
        }
        return apiContext
    }

    public static Map getEbaySites() {
        if(!ebay_sites) {
            Field[] fields = SiteCodeType.getDeclaredFields()
            ebay_sites["US"] = "US"
            for(Field field : fields) {
                Annotation annotation = field.getAnnotation(XmlEnumValue)
                if(annotation) {
                    ebay_sites[annotation.value()] = annotation.value()
                }
            }
        }
        return ebay_sites
    }

    public static CategoryType[] loadEbayCategories(EbayListingProfile profile) {
        try {
            GetCategoriesCall categoriesCall = new GetCategoriesCall(getApiContext(profile))
            categoriesCall.setCategorySiteID(SiteCodeType.fromValue(profile.setting.ebaySite))
            categoriesCall.addDetailLevel(DetailLevelCodeType.RETURN_ALL)
            return categoriesCall.getCategories()
        } catch (Exception e) {
            return new CategoryType[0]
        }
    }

    public static getPrice(Product product, EbayPricingProfile pricingProfile) {
        Double price;
        if(pricingProfile.type == DomainConstants.PRICING_PROFILE_TYPE.PRODUCT_DEFAULT_PRICE) {
            price = product.isOnSale ? product.salePrice : product.basePrice;
        } else if(pricingProfile.type == DomainConstants.PRICING_PROFILE_TYPE.NEW_PRICE) {
            price = pricingProfile.newAmount
        } else if(pricingProfile.additionalType == '$') {
            price = (product.isOnSale ? product.salePrice : product.basePrice) + pricingProfile.additionalAmount;
        } else {
            price = product.isOnSale ? product.salePrice : product.basePrice;
            price = price + (pricingProfile.additionalAmount * price /100)
        }
        return price;
    }

    public static ShippingDetailsType getShippingDetailsType(EbayPostage ebayPostage) {
        ShippingDetailsType shippingDetailsType = new ShippingDetailsType();
        shippingDetailsType.setShippingType(ShippingTypeCodeType.FREIGHT_FLAT);
        ShippingServiceOptionsType shippingOptions = new ShippingServiceOptionsType();
        shippingOptions.setShippingService(
                ShippingServiceCodeType.SHIPPING_METHOD_STANDARD.value());
        AmountType amount = new AmountType();
        amount.setValue(ebayPostage.handlingCost);
        shippingOptions.setShippingServiceAdditionalCost(amount);
        amount = new AmountType();
        amount.setValue(ebayPostage.shippingCost);
        shippingOptions.setShippingServiceCost(amount);
        shippingOptions.setShippingServicePriority(new Integer(1));
        amount = new AmountType();
        amount.setValue(0.0);
        shippingOptions.setShippingInsuranceCost(amount);
        shippingDetailsType.setShippingServiceOptions([shippingOptions] as ShippingServiceOptionsType[]);
        return shippingDetailsType
    }

    public static ItemType buildItem(EbayListingProfile profile, Product product) {
        ItemType item = new ItemType();
        item.setTitle(product.name);
        item.setDescription(product.description ?: "No Description");
        item.setSKU(product.sku)
        String currencyCode = AppUtil.baseCurrency.code;
        item.setCurrency(CurrencyCodeType.fromValue(currencyCode));
        if(profile.pricing.type == DomainConstants.PRICING_TYPE.FIXED) {
            if(profile.pricing.sellToQuantityType == DomainConstants.SELL_TO_QUANTITY_TYPE.AVAILABLE_STOCK) {
                if(product.isInventoryEnabled && product.availableStock > 0) {
                    item.setQuantity(product.availableStock)
                } else if(product.isInventoryEnabled){
                    item.setQuantity(1);
                }
            } else if(profile.pricing.sellToQuantityType == DomainConstants.SELL_TO_QUANTITY_TYPE.JUST_ONE_ITEM) {
                item.setQuantity(1);
            } else {
                item.setQuantity(profile.pricing.quantity)
            }
            item.setListingType(ListingTypeCodeType.FIXED_PRICE_ITEM);
            Double price = getPrice(product, profile.pricing.buyNowPrice);
            AmountType amountType = new AmountType();
            amountType.setValue(price)
            item.setStartPrice(amountType);
        } else {
            item.setQuantity(1)
            item.setListingType(ListingTypeCodeType.CHINESE)
            Double startPrice = getPrice(product, profile.pricing.startingPrice);
            AmountType startAmount = new AmountType();
            startAmount.setValue(startPrice);
            item.setStartPrice(startAmount);
            Double buyItNowPrice = getPrice(product, profile.pricing.buyNowPrice);
            AmountType buyItAmount = new AmountType();
            buyItAmount.setValue(buyItNowPrice);
            item.setBuyItNowPrice(buyItAmount);
        }
        if(product.globalTradeItemNumber) {
            ProductListingDetailsType productListingDetailsType = new ProductListingDetailsType();
            productListingDetailsType.setGTIN(product.globalTradeItemNumber);
            item.setProductListingDetails(productListingDetailsType)
        }
        String duration = "Days_" + profile.pricing.duration;
        item.setListingDuration(duration);
        StoreDetail storeDetail = StoreDetail.first();
        if(!storeDetail) {
            throw new ApplicationRuntimeException("store.details.not.found")
        }
        Address address = storeDetail.address
        String location = "";
        if(address.postCode) {
            location = location + "Zip Code: ${address.postCode}\n"
        }
        if(address.state) {
            location = location + "State: ${address.state.name}\n"
        }
        item.setLocation(location)
        item.setCountry(CountryCodeType.fromValue(storeDetail.address.country.code));
        item.setSite(SiteCodeType.fromValue(AppUtil.getConfig("ebay_listing", "ebay_site")))
        if(profile.primaryCategory) {
            CategoryType categoryType = new CategoryType();
            categoryType.setCategoryID(profile.primaryCategory.toString());
            item.setPrimaryCategory(categoryType)
        }
        if(profile.secondaryCategory) {
            CategoryType categoryType = new CategoryType();
            categoryType.setCategoryID(profile.secondaryCategory.toString());
            item.setSecondaryCategory(categoryType)
        }
        BuyerPaymentMethodCodeType[] buyerPaymentMethods = new BuyerPaymentMethodCodeType[profile.availablePaymentMethods.size() + 1];
        buyerPaymentMethods[0] = BuyerPaymentMethodCodeType.fromValue(profile.safePaymentMethod.name);
        if(profile.safePaymentMethod.name == "PayPal") {
            EbayMetaValue ebayMetaValue = profile.safePaymentMethod.metaValues.find {it.name == "email"}
            if(!ebayMetaValue) {
                throw new ApplicationRuntimeException("paypal.email.not.found");
            }
            item.setPayPalEmailAddress(ebayMetaValue.value)
        }
        profile.availablePaymentMethods.eachWithIndex { EbayPaymentMethod entry, int i ->
            buyerPaymentMethods[i + 1] = BuyerPaymentMethodCodeType.fromValue(entry.name);
        }
        item.setPaymentMethods(buyerPaymentMethods);
        item.setConditionID(profile.itemCondition);
        if(profile.pricing.isPrivateListing) {
            item.setPrivateListing(true)
        }
        item.setDispatchTimeMax(Integer.valueOf(1));
        item.setShippingDetails(getShippingDetailsType(profile.postage));
        if(profile.returnPolicy.acceptReturn) {
            ReturnPolicyType returnPolicyType = new ReturnPolicyType();
            returnPolicyType.setReturnsAcceptedOption("ReturnsAccepted");
            returnPolicyType.setReturnsWithinOption(profile.returnPolicy.returnWithin)
            returnPolicyType.setRefundOption(profile.returnPolicy.refundType)
            returnPolicyType.setShippingCostPaidByOption(profile.returnPolicy.returnShippingPaidBy)
            item.setReturnPolicy(returnPolicyType)
        } else {
            ReturnPolicyType returnPolicyType = new ReturnPolicyType();
            returnPolicyType.setReturnsAcceptedOption("ReturnsNotAccepted");
            item.setReturnPolicy(returnPolicyType)
        }

        if(profile.useProductImage && product.images.size()) {
            PictureDetailsType pictureDetailsType = new PictureDetailsType();
            pictureDetailsType.setGalleryType(GalleryTypeCodeType.GALLERY);
            Boolean isFirst = true
            product.images.each {
                String image = app.baseUrl() + "resources/product/product-" + product.id + "/" + it.name;
                if(isFirst) {
                    pictureDetailsType.setGalleryURL(image)
                } else {
                    pictureDetailsType.setPictureURL(image)
                    isFirst = true
                }
            }
            item.setPictureDetails(pictureDetailsType)
        }
        return item
    }

    public static Map addItem(EbayListingProfile profile, Product product) {
        ApiContext context = getApiContext(profile);
        AddItemCall call = new AddItemCall(context);
        ItemType item = buildItem(profile, product);
        call.setItem(item)
        Map response = [:]
        try {
            call.addItem()
            response["status"] = "success";
            response["itemId"] = item.getItemID();
        } catch (ApiException ex) {
            response["status"] = "error";
            response["message"] = ex.getMessage()
        } catch(Exception ex) {
            response["status"] = "error";
        }
        return response
    }
}
