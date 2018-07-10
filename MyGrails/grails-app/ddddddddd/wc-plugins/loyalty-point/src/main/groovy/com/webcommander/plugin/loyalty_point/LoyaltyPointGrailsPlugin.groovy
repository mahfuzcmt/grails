package com.webcommander.plugin.loyalty_point

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class LoyaltyPointGrailsPlugin extends WebCommanderPluginBase {

    def title = "Loyalty Point"
    def author = "Sanjoy Kumar Mitra"
    def authorEmail = "sanjoy@bitmascot.com"
    def description = '''LoyaltyPoint Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/loyalty-point";
    {
        _plugin = new PluginMeta(identifier: "loyalty-point", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks = [
            productAdvancedEditTab            : [taglib: "loyaltyPoint", callable: "productEditField"],
            categoryAdvancedEditTab           : [taglib: "loyaltyPoint", callable: "categoryEditField"],
            saveCategoryAdvancedData          : [bean: "loyaltyPointService", callable: "saveLoyaltyPoint"],
            saveProductAdvancedData           : [bean: "loyaltyPointService", callable: "saveLoyaltyPoint"],
            imageBlockInProductImageView      : [taglib: "loyaltyPoint", callable: "productLoyaltyPointImageView"],
            customerProfilePluginsJS          : [taglib: "loyaltyPoint", callable: "customerProfilePluginsJS"],
            customerProfileLoyaltyPoints      : [taglib: "loyaltyPoint", callable: "customerProfileLoyaltyPoints"],
            customerProfilerReferral          : [taglib: "loyaltyPoint", callable: "customerProfilerReferral"],
            customerProfileTabBody            : [taglib: "loyaltyPoint", callable: "customerProfileTabBody"],
            cartDetailsAfterTable             : [taglib: "loyaltyPoint", callable: "beforePaymentMessage"],
            checkoutConfirmStep               : [taglib: "loyaltyPoint", callable: "beforePaymentMessage"],
            confirmOrderEnd                   : [taglib: "loyaltyPoint", callable: "beforePaymentMessage"],
            paymentSuccessAfterTable          : [taglib: "loyaltyPoint", callable: "paymentSuccessAfterTable"],
            advanceProductBulkColgroup        : [taglib: "loyaltyPoint", callable: "bulkEditAdvancedColGroup"],
            advanceProductBulkHeaderColumn    : [taglib: "loyaltyPoint", callable: "bulkEditAdvancedHeader"],
            advanceProductBulkChangeAllColumn : [taglib: "loyaltyPoint", callable: "bulkEditAdvancedChangeAll"],
            advanceProductBulkDataColumn      : [taglib: "loyaltyPoint", callable: "bulkEditAdvancedDataColumn"],
            advanceCategoryBulkColgroup       : [taglib: "loyaltyPoint", callable: "bulkEditAdvancedColGroup"],
            advanceCategoryBulkHeaderColumn   : [taglib: "loyaltyPoint", callable: "bulkEditAdvancedHeader"],
            advanceCategoryBulkChangeAllColumn: [taglib: "loyaltyPoint", callable: "bulkEditAdvancedChangeAll"],
            advanceCategoryBulkDataColumn     : [taglib: "loyaltyPoint", callable: "bulkEditAdvancedCategoryDataColumn"],
            enterpriseProductAdvancedEditTab  : [taglib: "loyaltyPoint", callable: "variationProductEditField"],
            myAccountPageSettings             : [taglib: "loyaltyPoint", callable: "myAccountPageSetting"],
            customerRegistrationForm          : [taglib: "loyaltyPoint", callable: "customerRegistrationReferralField"],
            checkoutPaymentOption             : [taglib: "loyaltyPoint", callable: "checkoutPaymentOption"],
            customerAndGroupListForSpecialRule: [bean: "loyaltyPointService", callable: "customerAndGroupListForSpecialRule"]
        ]
    }


}
