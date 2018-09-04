<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.util.StringUtil; com.webcommander.webcommerce.Category" %>
<div class="fee-widget-config-panel">
    <g:form controller="frontEndEditor" action="saveWidget" class="config-form">
        <input type="hidden" name="widgetType" value="${widget.widgetType}">
        <div class="fee-config-body fee-noPadding">
            <div class="fee-page-panel">
                <div class="fee-first-page show">
                    <div class="fee-header-top fee-search-panel">
                        <div class="fee-row fee-align-center fee-form-row">
                            <div class="fee-full-width">
                                <input type="text" class="search-text fee-medium-input" name="name" value="${params?.name}" placeholder="<g:message code="search"/>">
                            </div>
                            <div class="fee-full-width">
                                <ui:hierarchicalSelect class="medium category-selector fee-medium-input always-bottom" name="parent" value="${params?.parent && !['all', 'root'].contains(params?.parent) ? params?.parent?.toLong() : params?.parent}" domain="${Category}" prepend="${['all': g.message(code: "all.categories"), "root": g.message(code: "root")]}"/>
                            </div>
                            <div class="fee-full-width">
                                <select id="product-sorting" name="sortBy" class="product-sorting fee-medium-input always-bottom">
                                    <option value=""><g:message code="sort.by"/></option>
                                    <option value="ALPHA_ASC" ${params.sortBy == 'ALPHA_ASC' ? ' selected="selected"' : ''}><g:message code="alphabetic.a.z"/></option>
                                    <option value="ALPHA_DESC" ${params.sortBy == 'ALPHA_DESC' ? ' selected="selected"' : ''}><g:message code="alphabetic.z.a"/></option>
                                    <option value="SKU_ASC" ${params.sortBy == 'SKU_ASC' ? ' selected="selected"' : ''}><g:message code="sku"/></option>
                                    <option value="PRICE_DESC" ${params.sortBy == 'PRICE_DESC' ? ' selected="selected"' : ''}><g:message code="price.high.low"/></option>
                                    <option value="PRICE_ASC" ${params.sortBy == 'PRICE_ASC' ? ' selected="selected"' : ''}><g:message code="price.low.high"/></option>
                                    <option value="CREATED_ASC" ${params.sortBy == 'CREATED_ASC' ? ' selected="selected"' : ''}><g:message code="creation.date.oldest.first"/></option>
                                    <option value="CREATED_DESC" ${params.sortBy == 'CREATED_DESC' ? ' selected="selected"' : ''}><g:message code="creation.date.latest.first"/></option>
                                </select>
                            </div>
                            <div class="fee-full-width">
                                <ui:perPageCountSelector name="max" value="${params.max}" class="fee-small-input always-bottom"/>
                            </div>
                            <div class="fee-full-width">
                                <select id="special-product-filtering" name="specialProductsFilter" class="special-product-filtering fee-medium-input always-bottom">
                                    <option value=""><g:message code="all.products"/></option>
                                    <option value="ON_SALE" ${params.specialProductsFilter == 'ON_SALE' ? ' selected="selected"' : ''}><g:message code="sale.products"/></option>
                                    <option value="FEATURED" ${params.specialProductsFilter == 'FEATURED' ? ' selected="selected"' : ''}><g:message code="featured.products"/></option>
                                    <option value="CALL_FOR_PRICE" ${params.specialProductsFilter == 'CALL_FOR_PRICE' ? ' selected="selected"' : ''}><g:message code="call.for.price.product"/></option>
                                    <option value="TOP_SELLING" ${params.specialProductsFilter == 'TOP_SELLING' ? ' selected="selected"' : ''}><g:message code="top.selling.products"/></option>
                                </select>
                            </div>
                            <div class="fee-full-width">
                                <button class="fee-blue" type="button"><g:message code="search"/></button>
                            </div>
                        </div>
                    </div>
                    <div class="fee-row fee-padding-30">
                        <div class="fee-col fee-col-48 fee-left-panel">
                            <g:include view="frontEndEditor/loadLeftProductSection.gsp" model="${[allProducts: allProducts, count: count]}" params="${[offset: params.offset, max: params.max]}"/>
                        </div>
                        <div class="fee-col fee-col-4"></div>
                        <div class="fee-col fee-col-48 fee-right-panel">
                            <div class="fee-body table-view">
                                <table class="fee-table">
                                    <colgroup>
                                        <col style="width: 70%">
                                        <col style="width: 30%">
                                    </colgroup>
                                    <tr class="fee-ignore">
                                        <th class="sortable sort-down" sort-dir="up"><g:message code="product.name"/></th>
                                        <th class="actions-column">
                                            <span class="tool-icon remove-all"></span>
                                        </th>
                                    </tr>
                                    <g:each in="${products}" var="product">
                                        <tr class="item">
                                            <td>${product.name.encodeAsBMHTML()}</td>
                                            <td class="actions-column" item="${product.id}" type="${fieldName}">
                                                <g:if test="${!preventSort}">
                                                    <span class="tool-icon move-controls">
                                                        <span class="move-up"></span>
                                                        <span class="move-down"></span>
                                                    </span>
                                                </g:if>
                                                <span class="tool-icon remove"></span>
                                                <input type="hidden" name="product" value="${product.id}">
                                            </td>
                                        </tr>
                                    </g:each>
                                </table>
                            </div>
                            <div class="fee-footer">
                                <right-paginator total="${products.size()}" offset="0" max="${10}"></right-paginator>
                            </div>
                            <div class="hidden">
                                <div class="action-column-dice-content">
                                    <table>
                                        <tr class="empty">
                                            <td></td>
                                            <td class="actions-column">
                                                <g:if test="${!preventSort}">
                                                    <span class="tool-icon move-controls">
                                                        <span class="move-up"></span>
                                                        <span class="move-down"></span>
                                                    </span>
                                                </g:if>
                                                <span class="tool-icon remove"></span>
                                                <input type="hidden">
                                            </td>
                                        </tr>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="fee-last-page hide">
                    <div class="form-content fee-padding-30 product-configuration">
                        <div class="fee-row">
                            <div class="fee-col fee-col-50 fee-form-row fee-padding-5">
                                <label for="widgetTitle"><g:message code="widget.title"/></label>
                                <input type="text" name="title" id="widgetTitle" value="${widget.title}">
                            </div>
                            <div class="fee-col fee-col-50 fee-form-row fee-padding-5">
                                <label for="widgetDisplayType"><g:message code="display.type"/></label>
                                <ui:namedSelect class="display-type always-bottom" toggle-target="pagination-props" id="widgetDisplayType" name="display-type" key="${NamedConstants.PRODUCT_WIDGET_VIEW_MESSAGE}" value="${config["display-type"]}"/>
                            </div>
                        </div>
                        <div class="fee-row">
                            <div class="fee-col fee-col-50 fee-form-row fee-padding-5">
                                <label for="widgetShowPagination"><g:message code="show.pagination"/></label>
                                <ui:namedSelect class="show-pagination always-bottom" toggle-target="item-per-page" name="show-pagination" id="widgetShowPagination" key="${NamedConstants.PAGINATION_MESSAGE}" value="${config["show-pagination"]}"/>
                            </div>
                            <div class="fee-col fee-col-50 fee-form-row fee-padding-5">
                                <label for="widgetItemPerPage"><g:message code="number.of.products"/></label>
                                <input type="text" class="medium" name="item_per_page" id="widgetItemPerPage" value="${config["item_per_page"]}"/>
                            </div>
                        </div>
                        <div class="fee-row">
                            <div class="fee-col fee-col-50 fee-padding-5">
                                <div class="fee-form-row">
                                    <input class="single" type="checkbox" name="price" id="widgetPrice" value="true" uncheck-value="false" ${config.price == "true" ? "checked" : ""}>
                                    <span><g:message code="price"/></span>
                                </div>
                                <div class="fee-form-row">
                                    <input type="checkbox" class="single" name="add_to_cart" value="true" uncheck-value="false" ${config["add_to_cart"] == "true" ? "checked" : ""}>
                                    <span><g:message code="add.to.cart"/></span>
                                </div>
                                <div class="fee-form-row">
                                    <input type="checkbox" class="single" name="sortable" value="true" uncheck-value="false" ${config["sortable"] == "true" ? "checked" : ""}>
                                    <span><g:message code="sortable"/></span>
                                </div>
                            </div>
                            <div class="fee-col fee-col-50 fee-padding-5">
                                <div class="fee-form-row">
                                    <input class="single" type="checkbox" name="description" value="true" uncheck-value="false" ${config["description"] == "true" ? "checked" : ""}>
                                    <span><g:message code="short.description"/></span>
                                </div>
                                <div class="fee-form-row">
                                    <input type="checkbox" class="single" name="item-per-page-selection" value="true" uncheck-value="false" ${config["item-per-page-selection"] == "true" ? "checked" : ""}>
                                    <span><g:message code="item.per.page.selection"/></span>
                                </div>
                                <div class="fee-form-row">
                                    <input type="checkbox" class="single" name="show_on_hover" value="true" uncheck-value="false" ${config["show_on_hover"] == "true" ? "checked" : ""}>
                                    <span><g:message code="show.on.hover"/></span>
                                </div>
                            </div>
                        </div>
                        <div class="fee-row">
                            <div class="fee-col fee-col-50 fee-form-row fee-padding-5">
                                <label for="widgetLabelCallPrice"><g:message code="label.for.call.for.price"/></label>
                                <input type="text" class="medium" name="label_for_call_for_price" id="widgetLabelCallPrice" value="${config.label_for_call_for_price ?: "s:call.for.price"}" validation="required">
                            </div>
                            <div class="fee-col fee-col-50 fee-form-row fee-padding-5">
                                <label for="widgetLabelPrice"><g:message code="label.for.price"/></label>
                                <input type="text" class="medium" name="label_for_price" id="widgetLabelPrice" value="${config.label_for_price ?: "s:call.for.price"}" validation="required">
                            </div>
                        </div>
                        <div class="fee-plugin-form">
                            <plugin:hookTag hookPoint="productWidgetConfig" attrs="[configs: config]"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="fee-button-wrapper fee-config-footer">
            <button type="button" class="fee-page-button fee-common fee-previous" style="display: none;"><g:message code="previous"/></button>
            <button class="fee-save" type="submit"><g:message code="save"/></button>
            <button class="fee-cancel fee-common" type="button"><g:message code="cancel"/></button>
            <button type="button" class="fee-page-button fee-common fee-next"><g:message code="next"/></button>
        </div>
    </g:form>
</div>