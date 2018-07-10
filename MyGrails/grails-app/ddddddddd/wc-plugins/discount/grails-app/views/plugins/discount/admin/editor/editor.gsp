<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.plugin.discount.Constants; com.webcommander.plugin.discount.NameConstants" %>
<form class="discount-create-edit-panel create-edit-panel" action="<app:baseUrl/>discount/save" method="post">

    <input type="hidden" name="id" value="${discount.id}">
    <input type="hidden" name="type" value="${discount.type}">
    <input type="hidden" name="conditionType" value="cc">

    <div class="left-bar">
        <g:render template="/plugins/discount/admin/editor/leftbar" model="${[discount: discount, isProfileUsed: isProfileUsed]}"/>
    </div>

    <div class="right-panel">

        <div class="wc-row">
            <div class="col-8">
                <div class="disocunt-wizard">
                    <div class="disocunt-timeline-item">
                        <div class="disocunt-timeline-icon">
                            1
                        </div>
                        <div class="disocunt-timeline-content">
                            <h2><g:message code="select.your.customer"/></h2>
                            <div class="wc-row">
                                <div class="col-6">
                                    <div class="discount-select-box discount-choose-customer ${discount.isCertainCustomerSelected() ? 'selected' : ''}">
                                        <span class="discount-select-icon discount-icon discount-icon-certain-user"></span> <g:message code="certain.customers.group"/>
                                    </div>
                                </div>
                                <div class="col-6">
                                    <div class="discount-select-box discount-all-customer ${discount?.assoc?.isAppliedAllCustomer ? 'selected' : ''}">
                                        <span class="discount-select-icon discount-icon discount-icon-all-user"></span> <g:message code="all.customers" args="${[(AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customers":"Members"]}"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="disocunt-timeline-item">
                        <div class="disocunt-timeline-icon ">
                            2
                        </div>
                        <div class="disocunt-timeline-content">
                            <h2>Select your product</h2>
                            <div class="wc-row">
                                <div class="col-6">
                                    <div class="discount-select-box discount-choose-product ${discount.isCertainProductSelected() ? 'selected' : ''} ">
                                        <span class="discount-select-icon discount-icon discount-icon-certain-product"></span> <g:message code="certain.products.category"/>
                                    </div>
                                </div>
                                <div class="col-6">
                                    <div class="discount-select-box discount-all-product ${discount?.assoc?.isAppliedAllProduct ? 'selected' : ''} ">
                                        <span class="discount-select-icon discount-icon discount-icon-all-product"></span> <g:message code="all.products"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="disocunt-timeline-item">
                        <div class="disocunt-timeline-icon">
                            3
                        </div>
                        <div class="disocunt-timeline-content">
                            <h2></span> <g:message code="select.discount.details"/></h2>

                            <div class="discount-details-wrap"></div>

                        </div>
                    </div>
                </div>
            </div>
            <div class="col-4">
                <div class="discount-viewing-wrapper">
                    <h3 class="discount-title"><i><g:message code="preview.summery"/></i></h3>
                    <div class="discount-viewing">

                        <h4><g:message code="target.customers"/></h4>
                        <div class="select-customer-preview-table">
                            <g:if test="${discount?.assoc?.isAppliedAllCustomer}">
                                <input name="isAppliedAllCustomer" value="true" type="hidden">
                                <span style="width: 150px"><g:message code="all.customers" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customers":"Members"]}"/></span>
                            </g:if>
                            <g:elseif test="${discount?.assoc?.customers || discount?.assoc?.customerGroups}">
                                <discount:selectionNamePreviewer data="${[customer: discount.assoc?.customers]}"/>
                                <discount:selectionNamePreviewer data="${[customerGroup: discount.assoc?.customerGroups]}"/>
                            </g:elseif>
                            <g:else>
                                <span class="discount-dummy" style="width: 150px"></span>
                            </g:else>
                        </div>
                        <hr>

                        <h4><g:message code="selected.products"/></h4>
                        <div class="select-product-preview-table">
                            <g:if test="${discount?.assoc?.isAppliedAllProduct}">
                                <input name="isAppliedAllProduct" value="true" type="hidden">
                                <span style="width: 150px"><g:message code="all.products"/></span>
                            </g:if>
                            <g:elseif test="${discount?.assoc?.products || discount?.assoc?.categories}">
                                <discount:selectionNamePreviewer data="${[product: discount.assoc?.products]}"/>
                                <discount:selectionNamePreviewer data="${[category: discount.assoc?.categories]}"/>
                            </g:elseif>
                            <g:else>
                                <span class="discount-dummy" style="width: 150px"></span>
                            </g:else>
                        </div>
                        <hr>

                        <h4><g:message code="discount.on"/></h4>
                        <div class="select-discount-preview-table">
                            <g:if test="${discount?.detailsId}">
                                <g:render template="/plugins/discount/admin/preview/details/${discount.discountDetailsType}" model="${[discountDetails: discount.discountDetails]}"/>
                            </g:if>
                            <g:else>
                                <span class="discount-dummy" style="width: 150px"></span>
                            </g:else>
                        </div>

                    </div>
                </div>
            </div>
        </div>

    </div>

</form>