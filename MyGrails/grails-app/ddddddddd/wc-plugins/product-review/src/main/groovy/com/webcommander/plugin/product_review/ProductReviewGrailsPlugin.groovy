package com.webcommander.plugin.product_review

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class ProductReviewGrailsPlugin extends WebCommanderPluginBase {

    def title = "Product Review"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Displays product review and Rating in webcommander page'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/product-review";
    {
        _plugin = new PluginMeta(identifier: "product-review", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks=[productInfoTabBody:[taglib:"review",callable:"tabBody"],productInfoTabHeader:[taglib:"review",callable:"tabHeader"],configProductInCategoryPage:[taglib:"review",callable:"configProductInCategoryPage"],adminJss:[taglib:"review",callable:"adminJSs"],productSortingTypes:[taglib:"review",callable:"appendSortTypes"],productWidgetConfig:[taglib:"review",callable:"productWidgetConfig"],productWidgetConfigurationPanel:[taglib:"review",callable:"ratingConfig"],imageBlockInProductImageView:[taglib:"review",callable:"renderRatingInImageView"],productListPriceCol:[taglib:"review",callable:"renderRatingInListView"],compareProductDetailsTable:[taglib:"review",callable:"reviewAtCompareDetails"],compareProductConfigFieldsEnd:[taglib:"review",callable:"ratingConfig"],showRatingInWidget:[taglib:"review",callable:"showRatingInWidget"],getProductBetweenRating:[bean:"productReviewService",callable:"getProductIds"]]
    }


}
