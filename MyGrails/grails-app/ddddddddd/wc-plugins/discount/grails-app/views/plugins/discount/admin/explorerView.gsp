<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil; com.webcommander.plugin.discount.NameConstants" %>

<div class="right-panel grid-view">
    <div class="body">
        <g:if test="${discountItem == null}">
            <div class="empty-item-content">
                <p><g:message code="click.new.btn.add.discount" encodeAs="raw" args="${['<span class="highlight create-discount"> + New</span>']}"/></p>
            </div>
        </g:if>
        <g:else>
            <div class="discount-preview">
                <div class="title-bar">
                    <span class="header-title"><span class="title">${discountItem.name}</span></span>
                </div>
                <div class="info-row">
                    <label><g:message code="status"/></label>
                    <span class="value"><g:message code="${discountItem.isActive ? "active" : "inactive"}"/></span>
                </div>
                <g:if test="${discountItem.startFrom || discountItem.startTo}">
                    <div class="duration-row">
                        <label><g:message code="duration"/></label>
                        <g:if test="${discountItem.startFrom}">
                            <span class="value">${discountItem.startFrom.toAdminFormat(true, false, session.timezone)}</span>
                        </g:if>
                        <g:if test="${discountItem.isSpecifyEndDate && discountItem.startTo}">
                            <span class="value">${discountItem.startTo.toAdminFormat(true, false, session.timezone)}</span>
                        </g:if>
                    </div>
                </g:if>

                <g:if test="${discountItem.isMaximumUseTotal}">
                    <div class="info-row">
                        <label><g:message code="maximum.use.total"/></label>
                        <span class="value">
                            ${discountItem.maximumUseCount}
                        </span>
                    </div>
                </g:if>

                <g:if test="${discountItem.isMaximumUseCustomer}">
                    <div class="info-row">
                        <label><g:message code="maximum.use.customer"/></label>
                        <span class="value">
                            ${discountItem.maximumUseCustomerCount}
                        </span>
                    </div>
                </g:if>

                <g:if test="${discountItem.isMaximumDiscountAllowed}">
                    <div class="info-row">
                        <label><g:message code="maximum.allowed.amount"/></label>
                        <span class="value">
                            ${AppUtil.baseCurrency.symbol}${discountItem.maximumDiscountAllowedAmount?.toConfigPrice()}
                        </span>
                    </div>
                </g:if>

                <g:if test="${discountItem.isApplyCouponCode}">
                    <div class="info-row">
                        <label><g:message code="apply.coupon.code"/></label>
                        <span class="value">
                            <g:message code="yes"/>
                        </span>
                    </div>
                </g:if>

                <g:if test="${discountItem.isCreateUniqueCouponEachCustomer}">
                    <div class="info-row">
                        <label><g:message code="apply.coupon.code.unique.customer"/></label>
                        <span class="value">
                            <g:message code="yes"/>
                        </span>
                    </div>
                </g:if>

                <g:if test="${discountItem.isDisplayTextCoupon}">
                    <div class="info-row">
                        <label><g:message code="apply.coupon.code.additional.info"/></label>
                        <span class="value">
                            ${discountItem.displayTextCoupon.encodeAsBMHTML()}
                        </span>
                    </div>
                </g:if>

                <div class="info-row">
                    <label><g:message code="target.customers"/></label>
                    <span class="value">
                        <div class="select-customer-preview-table">
                            <g:if test="${discountItem?.assoc?.isAppliedAllCustomer}">
                                <span style="width: 150px"><g:message code="all.customers" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce" as Boolean) == 'true')?"Customers":"Members"]}"/></span>
                            </g:if>
                            <g:if test="${discountItem?.assoc?.customers || discountItem?.assoc?.customerGroups}">
                                <discount:selectionNamePreviewer data="${[customer: discountItem.assoc?.customers]}"/>
                                <discount:selectionNamePreviewer data="${[customerGroup: discountItem.assoc?.customerGroups]}"/>
                            </g:if>
                            <g:else>
                                <span class="discount-dummy" style="width: 150px"></span>
                            </g:else>
                        </div>
                    </span>
                </div>
                <div class="info-row">
                    <label><g:message code="selected.products"/></label>
                    <span class="value">
                        <div class="select-product-preview-table">
                            <g:if test="${discountItem?.assoc?.isAppliedAllProduct}">
                                <span style="width: 150px"><g:message code="all.products"/></span>
                            </g:if>
                            <g:elseif test="${discountItem?.assoc?.products || discountItem?.assoc?.categories}">
                                <discount:selectionNamePreviewer data="${[product: discountItem.assoc?.products]}"/>
                                <discount:selectionNamePreviewer data="${[category: discountItem.assoc?.categories]}"/>
                            </g:elseif>
                            <g:else>
                                <span class="discount-dummy" style="width: 150px"></span>
                            </g:else>
                        </div>
                    </span>
                </div>

                <div class="discount-details-wrap">
                    <g:render template="/plugins/discount/admin/preview/details/${discountItem.discountDetailsType}" model="${[discountDetails: discountItem.discountDetails]}"/>
                </div>
            </div>
        </g:else>
    </div>
</div>