package com.webcommander.models

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.throwables.CartManagerException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.CombinedProduct
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import com.webcommander.webcommerce.TaxProfile
import grails.converters.JSON
import grails.util.Holders
import grails.util.TypeConvertingMap
import org.springframework.beans.BeanUtils

/**
 * Created by sajedur on 10-12-2014.
 */
trait ProductInCartBase extends QuantityAdjustableCartObject  {

    String type = NamedConstants.CART_OBJECT_TYPES.PRODUCT
    ProductData product
    Map includedProductQuantityMap
    TypeConvertingMap requestedParams

    private static ProductService _productService;
    private static ProductService getProductService() {
        if(_productService) {
            return _productService
        } else {
            return _productService = Holders.applicationContext.getBean("productService")
        }
    }


    String getLink() {
        return product.link
    }

    String getImageLink(String imageSize) {
        return product.getImageLink(imageSize)
    }

    TaxProfile resolveTaxProfile() {
        return product.resolveTaxProfile();
    }

    /**
     * For cart quantity check
     * */
    public void validate(Integer quantity) {
        def type = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "order_quantity_over_stock");
        Closure quantityCheck = { _product, _quantity ->
            if(_product.isInventoryEnabled && type != DomainConstants.OUT_OF_STOCK_MESSAGE_TYPE.SELL_AWAY) {
                if(_quantity > _product.availableStock && type == DomainConstants.OUT_OF_STOCK_MESSAGE_TYPE.DO_NOT_SELL) {
                    String message = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "do_not_sell_message");
                    throw new CartManagerException(this, message, [quantity]);
                } else if (_quantity > _product.availableStock && _product.availableStock >= _product.supportedMinOrderQuantity && type == DomainConstants.OUT_OF_STOCK_MESSAGE_TYPE.ADD_AVAILABLE ) {
                    throw new CartManagerException(this, "ADD_AVAILABLE", [available(quantity)]);
                } else if(_product.availableStock < _product.supportedMinOrderQuantity  && type == DomainConstants.OUT_OF_STOCK_MESSAGE_TYPE.ADD_AVAILABLE) {
                    throw new CartManagerException(this, "s:requested.quantity.not.available", [quantity]);
                }
            }
            if(product.id == _product.id) {
                if(_product.supportedMaxOrderQuantity && _quantity > _product.supportedMaxOrderQuantity) {
                    throw new CartManagerException(this, AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CART_PAGE, "max_quantity_message"), [available(quantity)]);
                }
                if(_product.supportedMinOrderQuantity && _quantity < _product.supportedMinOrderQuantity) {
                    throw new CartManagerException(this, AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CART_PAGE, "min_quantity_message"), [_product.supportedMinOrderQuantity]);
                }
                if(_product.isMultipleOrderQuantity) {
                    if(_quantity % _product.multipleOfOrderQuantity != 0) {
                        throw new CartManagerException(this, AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CART_PAGE, "multiple_quantity_message"), [_product.multipleOfOrderQuantity]);
                    }
                }
            }
        }
        if(product.isPriceOrPurchaseRestricted(AppUtil.loggedCustomer, AppUtil.loggedCustomerGroupIds) || product.isCallForPriceEnabled || !product.isActive || product.isInTrash) {
            throw new CartManagerException(this, "s:you.can.not.buy.this.product");
        }
        quantityCheck product, quantity
        if(product.isCombined && !product.isCombinationPriceFixed && product.isCombinationQuantityFlexible) {
            Long bpId = id;
            List<CombinedProduct> combinedProducts = CombinedProduct.where {
                baseProduct.id == bpId
            }.list()
            combinedProducts.eachWithIndex {CombinedProduct it, int i ->
                if(includedProductQuantityMap != null) {
                    int _quantity = (includedProductQuantityMap["" + it.id] ?: ("" + it.quantity)).toInteger();
                    quantityCheck it.includedProduct, _quantity * quantity
                } else {
                    quantityCheck it.includedProduct, it.quantity
                }
            }
        }
    }

    /**
     * For cart quantity check
     * */
    public Integer available(Integer quantity) {
        Closure availableCheck = { _product, _quantity ->
            if(_product.isInventoryEnabled) {
                if(_quantity > _product.availableStock) {
                    _quantity = _product.availableStock
                }
            }
            if(product.id == _product.id) {
                if(_product.supportedMaxOrderQuantity && _quantity > _product.supportedMaxOrderQuantity) {
                    _quantity = _product.supportedMaxOrderQuantity;
                }
                if(_product.supportedMinOrderQuantity && _quantity < _product.supportedMinOrderQuantity) {
                    return 0;
                }
                if(_product.isMultipleOrderQuantity) {
                    Integer diff = _quantity % _product.multipleOfOrderQuantity;
                    if(_quantity - diff >= _product.minOrderQuantity) {
                        return _quantity - diff
                    } else {
                        return 0;
                    }
                }
            }
            return _quantity
        }
        quantity = availableCheck product, quantity
        if(quantity > 0 && product.isCombined) {
            List<CombinedProduct> combinedProducts = CombinedProduct.where {
                baseProduct.id == id
            }.list()
            for(def combined : combinedProducts) {
                int _quantity = includedProductQuantityMap?."$combined.id" ?: ("" + combined.quantity).toInteger()
                Integer avail = availableCheck combined.includedProduct, _quantity * quantity
                if(avail == 0) {
                    return 0;
                }
                if(avail != _quantity * quantity) {
                    quantity = Math.floor(avail/(double)_quantity)
                }
                if(quantity == 0) {
                    return 0;
                }
            }
        }
        return quantity
    }

    void updateProps() {
        try {
            BeanUtils.copyProperties(product, this, "class", "metaClass", "images")
            Product _product = Product.proxy(id)
            if(_product.isCombined && requestedParams.included) {
                effectivePrice = productService.getCombinationPrice(_product, JSON.parse(requestedParams.included))
                if(effectivePrice != null && effectivePrice != product.basePrice) {
                    if(product.effectivePrice != product.basePrice) {
                        Double rate = product.effectivePrice / product.basePrice
                        effectivePrice = rate * product.effectivePrice
                    }
                } else {
                    effectivePrice = product.effectivePrice
                }
            }
        } catch(ApplicationRuntimeException app) {
            if(app.message == "no.variation.for.combination") {
                throw new CartManagerException(this, "no.longer.exists")
            }
        }
    }

    void refresh() {
        Product product = Product.get(id);
        this.product = productService.getProductData(product, requestedParams.config)
        updateProps()
    }
}
