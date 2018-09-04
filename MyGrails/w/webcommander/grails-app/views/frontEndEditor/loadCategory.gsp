<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.util.StringUtil; com.webcommander.webcommerce.Category" %>
<div class="fee-widget-config-panel">
    <g:form controller="frontEndEditor" action="saveWidget" class="config-form" onsubmit="return false;">
        <input type="hidden" name="widgetType" value="${widget.widgetType}">
        <div class="fee-config-body fee-noPadding">
            <div class="fee-page-panel">
                <div class="fee-first-page show">
                    <div class="fee-header-top fee-search-panel">
                        <div class="fee-row fee-align-center fee-form-row">
                            <div class="fee-full-width">
                                <input type="text" class="search-text" name="name" value="${params?.name}" placeholder="<g:message code="search"/>">
                            </div>
                            <div class="fee-full-width">
                                <select id="category-sorting" name="sortBy" class="category-sorting always-bottom">
                                    <option value=""><g:message code="sort.by"/></option>
                                    <option value="ALPHA_ASC" ${params.sortBy == 'ALPHA_ASC' ? ' selected="selected"' : ''}><g:message code="alphabetic.a.z"/></option>
                                    <option value="ALPHA_DESC" ${params.sortBy == 'ALPHA_DESC' ? ' selected="selected"' : ''}><g:message code="alphabetic.z.a"/></option>
                                    <option value="SKU_ASC" ${params.sortBy == 'SKU_ASC' ? ' selected="selected"' : ''}><g:message code="sku"/></option>
                                    <option value="CREATED_ASC" ${params.sortBy == 'CREATED_ASC' ? ' selected="selected"' : ''}><g:message code="creation.date.oldest.first"/></option>
                                    <option value="CREATED_DESC" ${params.sortBy == 'CREATED_DESC' ? ' selected="selected"' : ''}><g:message code="creation.date.latest.first"/></option>
                                </select>
                            </div>
                            <div class="fee-full-width">
                                <ui:perPageCountSelector name="max" value="${params.max}" class="fee-small-input always-bottom"/>
                            </div>
                            <div class="fee-full-width">
                                <button class="submit-button" type="button"><g:message code="search"/></button>
                            </div>
                        </div>
                    </div>
                    <div class="fee-row fee-padding-30">
                        <div class="fee-col fee-col-48 fee-left-panel">
                            <g:include view="frontEndEditor/loadLeftCategorySection.gsp" model="${[allCategories: allCategories, count: count]}" params="${[offset: params.offset, max: params.max]}"/>
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
                                        <th class="sortable sort-down" sort-dir="up"><g:message code="category.name"/></th>
                                        <th class="actions-column">
                                            <span class="tool-icon remove-all"></span>
                                        </th>
                                    </tr>
                                    <g:each in="${categories}" var="category">
                                        <tr class="item">
                                            <td>${category.name.encodeAsBMHTML()}</td>
                                            <td class="actions-column" item="${category.id}" type="${fieldName}">
                                                <g:if test="${!preventSort}">
                                                    <span class="tool-icon move-controls">
                                                        <span class="move-up"></span>
                                                        <span class="move-down"></span>
                                                    </span>
                                                </g:if>
                                                <span class="tool-icon remove"></span>
                                                <input type="hidden" name="category" value="${category.id}">
                                            </td>
                                        </tr>
                                    </g:each>
                                </table>
                            </div>
                            <div class="fee-footer">
                                <right-paginator total="${categories?.size()}" offset="0" max="${10}"></right-paginator>
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
                    <div class="form-content fee-padding-30 category-configuration">
                        <div class="fee-row">
                            <div class="fee-col fee-col-50 fee-form-row fee-padding-5">
                                <label for="widgetTitle"><g:message code="widget.title"/></label>
                                <input type="text" name="title" id="widgetTitle" value="${widget.title}">
                            </div>
                            <div class="fee-col fee-col-50 fee-form-row fee-padding-5">
                                <label for="widgetShowPagination"><g:message code="show.pagination"/></label>
                                <ui:namedSelect class="always-bottom show-pagination" toggle-target="item-per-page" name="show-pagination" id="widgetShowPagination" key="${NamedConstants.PAGINATION_MESSAGE}" value="${config["show-pagination"]}"/>
                            </div>
                        </div>
                        <div class="fee-row">
                            <div class="fee-col fee-col-50 fee-padding-5">
                                <div class="fee-col fee-col-50 fee-form-row fee-padding-5">
                                    <label for="widgetItemPerPage"><g:message code="number.of.categories"/></label>
                                    <input type="text" class="medium" name="item_per_page" id="widgetItemPerPage" value="${config["item_per_page"] ?: 10}"/>
                                </div>
                            </div>
                            <div class="fee-col fee-col-50 fee-padding-5">
                                <div class="fee-form-row">
                                    <label for="widgetItemPerPage">&nbsp;</label>
                                    <input class="single" type="checkbox" name="description" value="true" uncheck-value="false" ${config["description"] == "true" ? "checked" : ""}>
                                    <span><g:message code="short.description"/></span>
                                </div>
                            </div>
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