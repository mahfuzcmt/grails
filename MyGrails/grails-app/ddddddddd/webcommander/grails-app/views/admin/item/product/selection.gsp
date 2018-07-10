<%@ page import="com.webcommander.util.StringUtil; com.webcommander.webcommerce.Category" %>
<div class="filter-block" validation-attr="filtervldt">
    <input type="text" class="search-text" placeholder="<g:message code="search"/>">
    <ui:hierarchicalSelect class="medium category-selector" domain="${Category}" prepend="${['all': g.message(code: "all.categories"), "root": g.message(code: "root")]}" />
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
    <ui:perPageCountSelector/>
    <select id="special-product-filtering" class="special-product-filtering medium">
        <option value=""><g:message code="all.products"/></option>
        <option value="ON_SALE"><g:message code="sale.products"/></option>
        <option value="FEATURED"><g:message code="featured.products"/></option>
        <option value="CALL_FOR_PRICE"><g:message code="call.for.price.product"/></option>
        <option value="TOP_SELLING"><g:message code="top.selling.products"/></option>
    </select>
    <div class="price-range">
        <g:set var="ref1" value="${StringUtil.uuid}"/>
        <g:set var="ref2" value="${StringUtil.uuid}"/>
        <span><g:message code="price.range"/></span>&nbsp;
        <span><g:message code="from"/></span>&nbsp;<input class="smaller" type="text" maxlength="9" filtervldt="number maxlength[9]" restrict="decimal" name="form-amount" id="${ref2}" />&nbsp;
        <span><g:message code="to"/></span>&nbsp;<input class="smaller" type="text" maxlength="9" filtervldt="maxlength[9] number compare[${ref2}, number, gte]" restrict="decimal" depends="#${ref2}" name="to-amount" id="${ref1}" /> &nbsp;
        <button class="small submit-button filter-block-submit" type="button"><g:message code="search"/></button>
    </div>
</div>
<div class="left-right-selector-panel">
    <div class="multi-column two-column">
        <div class="columns first-column">
            <div class="column-content selection-panel table-view">
                <g:include controller="productAdmin" action="loadProductsForSelection"/>
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
                        <g:each in="${products}" var="product">
                            <tr>
                                <td>
                                    <plugin:hookTag hookPoint="productSelectionColumn" attrs="${[productId: product.id]}">
                                        ${product.name.encodeAsBMHTML()}
                                    </plugin:hookTag>
                                </td>
                                <td class="actions-column" item="${product.id}" type="${fieldName}">
                                    <g:if test="${!preventSort}">
                                        <span class="tool-icon move-controls">
                                            <span class="move-up"></span>
                                            <span class="move-down"></span>
                                        </span>
                                    </g:if>
                                    <span class="action-navigator collapsed"></span>
                                    <input type="hidden" name="${fieldName}" value="${product.id}">
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