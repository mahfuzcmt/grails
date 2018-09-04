<%@ page import="com.webcommander.plugin.discount.NameConstants;" %>
<div class="discount-details-wrap">

    <input type="hidden" name="discountDetailsType" id="discountDetailsType" value="${discount.discountDetailsType ?: 'amount'}">

    <div id="discount-detail-tabs" class="bmui-tab wc-tab">
        <div class="bmui-tab-header-container top-side-header wc-tab-header-container">
            <g:each in="${supportedDetails}" var="details">
                <div class="bmui-tab-header discount-detail-tab wc-tab-box " data-tabify-tab-id="${details}">
                    <span class="discount-icon wc-tab-icon discount-icon-${details}"></span>
                    <span class="title"><g:message code="${NameConstants.DISCOUNT_DETAILS_TYPE[details]}"/></span>
                </div>
            </g:each>
        </div>
        <div class="bmui-tab-body-container wc-tab-body-container">
            <g:each in="${supportedDetails}" var="details">
                <g:set var="discountDetails" value="${discount.initiateDiscountDetails(details)}"/>

                <div id="bmui-tab-${details}" class="discount-details" data-type="${details}">
                    <g:include view="/plugins/discount/admin/editor/details/_${details}.gsp" model="[discountDetails: discountDetails]"/>
                </div>

            </g:each>
        </div>
    </div>

</div>
