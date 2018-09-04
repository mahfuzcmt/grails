package com.webcommander.plugin.afterpay_payment_gateway

import com.webcommander.beans.SiteMessageSource
import com.webcommander.util.AppUtil

class AfterpayPaymentGatewayTagLib {
    static namespace = "afterpayPaymentGateway";
    SiteMessageSource siteMessageSource;
    AfterpayService afterpayService;

    def productPriceWidget = { Map attrs, body ->
        out << body();
        if (afterpayService.isInstallmentShowActive()) {
            app.enqueueSiteJs(src: "plugins/afterpay-payment-gateway/js/site-js/product-ext.js", scriptId: "afterpay-product-ext");
            Double productPrice = pageScope.productData.priceToDisplay;
            String installmentAmount = (productPrice / 4).toCurrency().toPrice();
            String imageName = afterpayService.getValue("afterpayImage");
            String filePath = "resources/afterpay-payment-gateway/images/" + imageName;
            String message = siteMessageSource.convert("s:price.installment.amount", null, [installment_amount: AppUtil.siteCurrency.symbol + installmentAmount]);
            String html = '<div class="afterpay-installment"><span class="afterpay-message">' + message + '</span><span class="afterpay-icon"></span><span image-path="' + filePath + '" class="learnmore"> Learn more </span></div>';
            out << html;
        }
    }
}
