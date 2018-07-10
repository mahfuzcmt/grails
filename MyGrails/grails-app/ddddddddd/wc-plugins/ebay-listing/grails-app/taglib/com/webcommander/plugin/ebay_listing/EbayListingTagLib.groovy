package com.webcommander.plugin.ebay_listing

import com.webcommander.plugin.ebay_listing.admin.webmarketing.EbayListingProfile
import com.webcommander.plugin.ebay_listing.model.EbayCategoryData
import com.webcommander.webcommerce.Product

class EbayListingTagLib {
    static namespace = "ebayListing"

    EbayListingService ebayListingService

    def adminJss = { attrs, body ->
        out << body()
        out << app.javascript(src: 'plugins/ebay-listing/js/admin/editor.ebay-listing.js')
    }

    def renderCategoryBreadcrumb = { attrs, body ->
        out << body()
        EbayListingProfile profile = attrs.profile
        Integer catId = attrs.isPrimaryCategory ? profile.primaryCategory : profile.secondaryCategory
        if(catId) {
            List<EbayCategoryData> categories = ebayListingService.getCategoryBreadcrumb(profile, attrs.isPrimaryCategory)
            String breadcrumb = ""
            for(EbayCategoryData category : categories) {
                breadcrumb = "<span class='node'><span class='right-arrow'> &raquo; </span><span class='title'>${category.name}</span></span>" + breadcrumb
            }
            out << breadcrumb
        } else {
            out << "<span class='tool-icon config'></span>"
        }
    }

    def addToProductEditor = { attr, body ->
        out << body()
        Product product = Product.get(pageScope.productId)
        out << '<div class="bmui-tab-header" data-tabify-tab-id="ebayProfile" data-tabify-url="' + app.relativeBaseUrl() + 'ebayListingAdmin/loadEbayProfile?productId=' + product.id +'">'
        out << '<span class="title">' + g.message(code: "ebay") + '</span>'
        out << '</div>'
    }

    def productEditorTabBody = { attrs, body ->
        out << body()
        out << '<div id="bmui-tab-ebayProfile"></div>'
    }

    def categoryEditorTabHeader = { attr, body ->
        out << body()
        out << '<div class="bmui-tab-header" data-tabify-tab-id="ebayProfile" data-tabify-url="' + app.relativeBaseUrl() + 'ebayListingAdmin/loadEbayProfileForCategory?categoryId=' + pageScope.categoryId +'">'
        out << '<span class="title">' + g.message(code: "ebay") + '</span>'
        out << '</div>'
    }

    def categoryEditorTabBody = { attrs, body ->
        out << body()
        out << '<div id="bmui-tab-ebayProfile"></div>'
    }
}
