<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.StringUtil; com.webcommander.webcommerce.Category" %>
<div class="filter-block">
    <input type="text" class="search-text" placeholder="<g:message code="search"/>">
    <ui:hierarchicalSelect class="medium category-selector" domain="${Category}" prepend="${['all': g.message(code: "all.categories")]}" />
    <select id="product-sorting" class="product-sorting medium">
        <option value=""><g:message code="sort.by"/></option>
        <option value="ALPHA_ASC"><g:message code="alphabetic.a.z"/></option>
        <option value="ALPHA_DESC"><g:message code="alphabetic.z.a"/></option>
        <option value="SKU_ASC"><g:message code="sku"/></option>
        <option value="PRICE_DESC"><g:message code="price.high.low"/></option>
        <option value="PRICE_ASC"><g:message code="price.low.high"/></option>
        <option value="CREATED_ASC"><g:message code="creation.date.oldest.first"/></option>
        <option value="CREATED_DESC"><g:message code="creation.date.latest.first"/></option>
    </select>
    <select id="special-product-filtering" class="special-product-filtering medium">
        <option value=""><g:message code="filter.special.products"/></option>
        <option value="ON_SALE"><g:message code="sale.products"/></option>
        <option value="CALL_FOR_PRICE"><g:message code="call.for.price.product"/></option>
        <option value="TOP_SELLING"><g:message code="top.selling.products"/></option>
    </select>
    <div class="price-range">
        <g:set var="ref1" value="${StringUtil.uuid}"/>
        <g:set var="ref2" value="${StringUtil.uuid}"/>
        <span><g:message code="price.range"/></span>&nbsp;
        <span><g:message code="from"/></span>&nbsp;<input class="smaller" type="text" maxlength="9" validation="number maxlength[9]" restrict="numeric" name="form-amount" id="${ref2}" />&nbsp;
        <span><g:message code="to"/></span>&nbsp;<input class="smaller" type="text" maxlength="9" validation="maxlength[9] number compare[${ref2}, number, gte]" restrict="numeric" name="to-amount" id="${ref1}" /> &nbsp;
        <button type="button" class="small submit-button filter-block-submit"><g:message code="search"/></button>
    </div>
</div>
<div class="left-right-selector-panel">
    <div class="multi-column two-column">
        <div class="columns first-column">
            <div class="column-content selection-panel table-view">
                <g:include controller="productAdmin" action="loadProductsForSelection" params="${[notCombined: true, fieldName: fieldName, isDownloadable: product.productType == DomainConstants.PRODUCT_TYPE.DOWNLOADABLE ? "true" : "false"]}"/>
            </div>
        </div><div class="columns last-column">
            <div class="column-content selected-panel table-view">
                <div class="body">
                    <table>
                        <colgroup>
                            <col style="width: 80%">
                            <col style="width: 20%">
                        </colgroup>
                        <tr>
                            <th><g:message code="product.name"/></th>
                            <th class="actions-column">
                                <span class="tool-icon remove-all"></span>
                            </th>
                        </tr>
                        <g:each in="${products}" var="cProduct">
                            <tr>
                                <td>
                                    <div class="form-row">
                                        <label><g:message code="label"/> </label>
                                        <input type="text" class="medium" value="${cProduct.label}" name="label">
                                    </div>
                                    <g:if test="${product.productType != DomainConstants.PRODUCT_TYPE.DOWNLOADABLE}">
                                        <div class="form-row">
                                            <label><g:message code="quantity"/> </label>
                                            <input type="text" class="tiny spinner" value="${cProduct.quantity}" name="quantity" min="1" rectrict="numeric" validation="number gt[0]">
                                        </div>
                                    </g:if>
                                    <g:if test="${!isFixed}">
                                        <div class="form-row">
                                            <label><g:message code="price"/> </label>
                                            <input type="text" class="medium" value="${cProduct.price}" restrict="decimal" name="price" min="1" rectrict="decimal" validation="number gt[0]">
                                        </div>
                                    </g:if>
                                </td>
                                <td class="actions-column" item="${cProduct.includedProduct.id}" type="${fieldName}">
                                    <g:if test="${!preventSort}">
                                        <span class="tool-icon move-controls">
                                            <span class="move-up"></span>
                                            <span class="move-down"></span>
                                        </span>
                                    </g:if>
                                    <span class="action-navigator collapsed"></span>
                                    <input type="hidden" name="${fieldName}" value="${cProduct.includedProduct.id}">
                                </td>
                            </tr>
                        </g:each>
                    </table>
                </div>
                <div class="footer">
                    <paginator total="${products.size()}" offset="0" max="${10}"></paginator>
                </div>
                <div class="hidden">
                    <div class="action-column-dice-content">
                        <table>
                            <tr>
                                <td></td>
                                <td class="actions-column">
                                    <g:if test="${!preventSort}">
                                        <span class="tool-icon move-controls">
                                            <span class="move-up"></span>
                                            <span class="move-down"></span>
                                        </span>
                                    </g:if>
                                    <span class="action-navigator collapsed"></span>
                                    <input type="hidden">
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>