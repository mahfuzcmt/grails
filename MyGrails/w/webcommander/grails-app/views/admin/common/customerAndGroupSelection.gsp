<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<form class="edit-popup-form">
    <g:set var="customerArg" value="${(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"}"/>
    <div class="header filter-block">
        <g:select name="selection-type" class="medium" from="[g.message(code: customerArg), g.message(code: 'customer.groups', args: customerArg)]" keys="['customer', 'group']"/>
        <div class="toolbar toolbar-right">
            <div class="search-form tool-group">
                <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="button" class="icon-search search-button submit-button search-form-submit"></button>
            </div>
        </div>
    </div>
    <div class="left-right-selector-panel">
        <div class="multi-column two-column">
            <div class="columns first-column">
                <div class="column-content selection-panel table-view">
                    <g:include controller="customerAdmin" action="loadCustomerForMultiSelect"/>
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
                                <th><g:message code="name"/></th>
                                <th class="actions-column">
                                    <span class="tool-icon remove-all"></span>
                                </th>
                            </tr>
                            <g:each in="${customers}" var="customer">
                                <tr>
                                    <td>${customer.firstName.encodeAsBMHTML() + (!customer.isCompany ? (" " + customer.lastName?.encodeAsBMHTML()) : "") }</td>
                                    <td class="actions-column" type="customer" item="${customer.id}">
                                        <span class="tool-icon remove-item remove"></span>
                                        <input type="hidden" name="${params.customerField ?: "customer"}" value="${customer.id}">
                                    </td>
                                </tr>
                            </g:each>
                            <g:each in="${customerGroups}" var="customerGroup">
                                <tr>
                                    <td>${customerGroup.name.encodeAsBMHTML()}</td>
                                    <td class="actions-column" type="customer-group" item="${customerGroup.id}">
                                        <span class="tool-icon remove-item remove"></span>
                                        <input type="hidden" name="${params.groupField ?: "customerGroup"}" value="${customerGroup.id}">
                                    </td>
                                </tr>
                            </g:each>
                        </table>
                    </div>
                    <div class="footer">
                        <paginator total="${(customers?.size() ?: 0) + (customerGroups?.size() ?: 0)}" offset="0" max="${10}"></paginator>
                    </div>
                    <div class="hidden">
                        <div class="action-column-dice-content">
                            <table>
                                <tr>
                                    <td></td>
                                    <td class="actions-column">
                                        <span class="tool-icon remove-item remove"></span>
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
    <div class="button-line">
        <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="done"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>