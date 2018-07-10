package com.webcommander.plugin.discount

import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.discount.models.DiscountData
import com.webcommander.plugin.discount.webcommerce.details.ShippingDiscountDetails
import com.webcommander.util.AppUtil
import com.webcommander.util.TemplateMatcher

class DiscountTagLib {
    static namespace = "discount"

    def selectionNamePreviewer = { attrs, body ->
        Map<String, Collection> data = attrs.data
        String prefix = attrs.prefix ?: ""
        out << "<span class='preview'>"
        Iterator it = data.entrySet().iterator()
        Boolean flag = false
        while (it.hasNext()) {
            Map.Entry entry = it.next()
            for (def entity : entry.value) {
                if(flag) {
                    out << ","
                } else {
                    flag = true
                }
                out << "<input type='hidden' name='${prefix}${entry.key}' value='${entity.id}'><span>${entity.name}</span>"
            }
        }
        out << '</span>'
    }

    def selectionTablePreview = { attrs, body ->
        Map<String, Collection> data = attrs.data
        out << '<table><tr>'
        Integer maxLen = 0;
        Iterator iterator = data.entrySet().iterator()
        while (iterator.hasNext()) {
            Map.Entry entry = iterator.next()
            maxLen = Math.max(maxLen, entry.value.size())
            String[] strings = entry.key.split("\\.")
            out << "<th>${g.message(code: (strings[strings.length - 1]).dotCase())}</th>"
        }
        if(attrs.removeBtn) {
            out << "<th></th>"
        }
        out << "</tr>"
        for (int i = 0; i < maxLen; i++) {
            iterator = data.entrySet().iterator()
            out << "<tr>"
            while (iterator.hasNext()) {
                Map.Entry entry = iterator.next()
                def entity = i >= entry.value.size() ? null : entry.value[i]
                if(entity) {
                    out << "<td>${entity.name}<input type='hidden' name='${entry.key}' value='${entity.id}' /></td>"
                } else {
                    out << '<td></td>'
                }
            }
            if(attrs.removeBtn) {
                out << "<td><span class='tool-icon remove'></span></td>"
            }
            out << "</tr>"
        }
        out << '</table>'
    }

    def couponFormForCheckPage = {attr, body ->
        out << body()
        out << g.render(template: "/plugins/discount/checkout/paymentOption")
    }

    def couponForm = {attr, body ->
        out << body()
        String showCouponInCartPage = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.DISCOUNT, "show_coupon_in_cart_page")
        if(!showCouponInCartPage || showCouponInCartPage == "false") {
            return;
        }
        out << "<form action='" + app.relativeBaseUrl() + "cart/details' method='post' class='discount-coupon-code-form valid-verify-form'>"
        if(request.discountCouponStatusMsg) {
            out << "<span class='message-block $request.discountCouponStatusType'>"
            out << request.discountCouponStatusMsg
            out << "</span>"
        }
        out << "<div class='form-row mandatory'>"
        out << "<label>" + g.message(code: "discount.coupon") + ":</label>"
        out << "<input type='text' name='couponCode' validation='required' autocomplete='off' />"
        out << "</div>"
        out << "<div class='form-row btn-row'>"
        out << "<label></label>"
        out << "<button type='submit' class='submit-button apply-discount-coupon'>" + g.message(code: "apply") + "</button>"
        out << "</div>"
        out << "</form>"
    }

    def discountMessage = { attrs, body ->
        DiscountData data = attrs.data
        String message = "<span class='discount-message'>"
        if (data) {
            TemplateMatcher engine = new TemplateMatcher("%", "%")
            if (data.isFreeShipping) {

                if (data.discount.isDisplayTextCart) {
                    message = data.discount.displayTextCart
                } else {
                    message = "You qualify for Free shipping"
                }

            } else if (data.discount.discountDetailsType.equals(Constants.DETAILS_TYPE.SHIPPING)
                    && data.discount.discountDetails.type.equals(Constants.SHIPPING_DETAILS_TYPE.SHIPPING_CAP)) {
                ShippingDiscountDetails details = data.discount.discountDetails

                if (data.discount.isDisplayTextCart) {
                    message = data.discount.displayTextCart
                    message = engine.replace(message, [amt: AppUtil.currencySymbol + details.capAmount])
                } else {
                    message = "You qualify for capped shipping of " + AppUtil.currencySymbol + details.capAmount
                }

            } else if (data.discount.discountDetailsType.equals(Constants.DETAILS_TYPE.SHIPPING)
                    && data.discount.discountDetails.type.equals(Constants.SHIPPING_DETAILS_TYPE.DISCOUNT_AMOUNT)) {

                if (data.discount.isDisplayTextCart) {
                    message = data.discount.displayTextCart
                    message = engine.replace(message, [amt: AppUtil.currencySymbol + data.resolvedAmount])
                } else {
                    message = "You qualify for "+ AppUtil.currencySymbol + data.resolvedAmount +" discount on shipping"
                }

            }

            if (attrs.isShowDiscountedMessage) {
                String discountedMessage = data.discountedMessage
                message = discountedMessage ? discountedMessage : message
            }
        }
        message += "</span>"
        out << message
    }

}
