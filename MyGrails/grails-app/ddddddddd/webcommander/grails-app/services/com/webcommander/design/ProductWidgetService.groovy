package com.webcommander.design

import com.webcommander.AutoPageContent
import com.webcommander.RenderService
import com.webcommander.constants.DomainConstants
import com.webcommander.models.ProductData
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.CombinedProduct
import com.webcommander.webcommerce.ProductService
import grails.gorm.transactions.Transactional

class ProductWidgetService {

    RenderService renderService
    ProductService productService

    def renderProductNameWidget(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/nameWidget", [:], writer)
    }

    def renderProductNameWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/editor/nameWidget", [:], writer)
    }

    def renderProductSummaryWidget(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/summaryWidget", [:], writer)
    }

    def renderProductSummaryWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/editor/summaryWidget", [:], writer)
    }

    def renderProductSkuWidget(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/skuWidget", [:], writer)
    }

    def renderProductSkuWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/editor/skuWidget", [:], writer)
    }

    def renderProductCategoryWidget(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/categoryWidget", [:], writer)
    }

    def renderProductCategoryWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/editor/categoryWidget", [:], writer)
    }

    def renderProductManufacturerWidget(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/manufacturerWidget", [:], writer)
    }

    def renderProductManufacturerWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/editor/manufacturerWidget", [:], writer)
    }

    def renderProductBrandWidget(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/brandWidget", [:], writer)
    }

    def renderProductBrandWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/editor/brandWidget", [:], writer)
    }

    def renderProductDownloadableSpecWidget(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/productDownloadableSpecWidget", [:], writer)
    }

    def renderProductDownloadableSpecWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/editor/productDownloadableSpecWidget", [:], writer)
    }

    def renderProductModelWidget(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/modelWidget", [:], writer)
    }

    def renderProductModelWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/editor/modelWidget", [:], writer)
    }

    def renderProductImageWidget(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/imageWidget", [:], writer)
    }

    def renderProductImageWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/editor/imageWidget", [:], writer)
    }

    def renderStockMarkWidget(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/stockMarkWidget", [productData: attrs.productData], writer)
    }

    def renderStockMarkWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/editor/stockMarkWidget", [:], writer)
    }

    def renderPriceWidget(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/priceWidget", [productData: attrs.productData], writer)
    }

    def renderPriceWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/editor/priceWidget", [:], writer)
    }

    def renderCombinedProductWidget(Map attrs, Writer writer) {
        List<CombinedProduct> includedProducts;
        if(attrs.product.isCombined) {
            includedProducts = productService.getIncludedProducts([id: attrs.product.id])
        }
        renderService.renderView("/productWidget/combinedProductWidget", [includedProducts: includedProducts], writer)
    }

    def renderCombinedProductWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/editor/combinedProductWidget", [:], writer)
    }

    def renderAddCartWidget(Map attrs, Writer writer) {
        Boolean  isPurchaseRestricted = attrs.productData.isPriceOrPurchaseRestricted(AppUtil.loggedCustomer,  AppUtil.loggedCustomerGroupIds)
        renderService.renderView("/productWidget/cartWidget", [clazz: isPurchaseRestricted ? "purchase-restricted" : "", isPurchaseRestricted: isPurchaseRestricted], writer)
    }

    def renderAddCartWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/editor/cartWidget", [:], writer)
    }

    def renderLikeusWidget(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/likeWidget", [:], writer)
    }

    def renderLikeusWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/editor/likeWidget", [:], writer)
    }

    def renderInformationWidget(Map attrs, Writer writer) {
        def productSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT);
        renderService.renderView("/productWidget/informationWidget", [isDescriptionEnabled:productSettings.show_description], writer)
    }

    def renderInformationWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/editor/informationWidget", [:], writer)
    }

    def renderConditionWidget(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/conditionWidget", [:], writer)
    }

    def renderConditionWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/editor/conditionWidget", [:], writer)
    }

    def renderPropertiesWidget(Map attrs, Writer writer) {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_PROPERTIES)
        def generalSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL)
        renderService.renderView("/productWidget/propertiesWidget", [config: config, unitLength: generalSettings.unit_length, unitWeight: generalSettings.unit_weight], writer)
    }

    def renderPropertiesWidgetForEditor(Map attrs, Writer writer) {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_PROPERTIES)
        def generalSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL)
        renderService.renderView("/productWidget/editor/propertiesWidget", [config: config, unitLength: generalSettings.unit_length, unitWeight: generalSettings.unit_weight], writer)
    }

    def renderRelatedWidget(Map attrs, Writer writer) {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.RELATED_PRODUCT);
        List<ProductData> relatedProducts = productService.getProductData(attrs.product.relatedProducts ? attrs.product.relatedProducts.id : [], [max: config['item-per-page']])
        config["show-pagination"] = "none";
        config["item-per-page-selection"] = "false"
        config["sortable"] = "false";
        config["product_listing_id"] = "related-product-listing"
        renderService.renderView("/productWidget/relatedWidget", [relatedProducts: relatedProducts, config: config], writer)
    }

    def renderRelatedWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/productWidget/editor/relatedWidget", [:], writer)
    }

    def renderSocialMediaShareWidget(Map attr, Writer writer) {
        renderService.renderView("/productWidget/socialMediaShare", [:], writer)
    }

    def renderSocialMediaShareWidgetForEditor(Map attr, Writer writer) {
        renderService.renderView("/productWidget/editor/socialMediaShare", [:], writer)
    }

    @Transactional
    Boolean saveContent(Map params) {
        Long id = params.long("containerId");
        AutoPageContent pageContent = AutoPageContent.createCriteria().get {
            eq("belong.id", id)
        };
        pageContent.body = params.bodyContent;
        pageContent.css = params.containerCss;
        pageContent.merge();
        return !pageContent.hasErrors()
    }
}