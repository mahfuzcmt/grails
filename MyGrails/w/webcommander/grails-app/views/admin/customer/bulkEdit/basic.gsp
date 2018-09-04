<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil; grails.converters.JSON; com.webcommander.webcommerce.Category" %>
<div class="toolbar-share">
    <span class="item-group entity-count title toolbar-left">
        <g:message code="${(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"customers":"members"}"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="customer-bulk-edit-tab basic-table table-view">
    <div class="bulk-editor-form" action="${app.relativeBaseUrl()}customerAdmin/saveBasicBulkProperties">
        <div class="body">
            <table class="content">
                <colgroup>
                    <col class="name-column">
                    <col class="email-column">
                    <col class="store-credit-column">
                    <col class="status-column">
                </colgroup>
                <tr>
                    <th><g:message code="name"/></th>
                    <th><g:message code="email"/></th>
                    <th><g:message code="store.credit"/></th>
                    <th class="status-column"><g:message code="status"/></th>
                </tr>
                <tr class="data-row">
                    <td></td>
                    <td></td>
                    <td class="change-all store-credit">
                        <span class="fake-link">
                            <g:message code="change.all"/>
                        </span>
                        <span class="store-credit-block" style="display: none;">
                            <span class="icon icon-note note">note</span>
                            <g:select class="action" name="add" from="${[ g.message(code: "increase"), g.message(code: "decrease")]}" keys="['true', 'false']"/>
                            <input type="text" name="deltaAmount" class=" medium" restrict="decimal" validation="required number min[0.01] max[99999999] maxprecision[9,2]">
                            <button type="button" class="apply-button"><g:message code="apply"/></button>
                            <span class="reset-store-credit reset"><g:message code="reset"/></span>
                        </span>
                    </td>
                    <td class="togglable custom-toggle customer-status">
                        <span class="fake-link">
                            <g:message code="change.all"/>
                        </span>
                        <span class="toggle-block" style="display: none;">
                            <input type='checkbox' class='single' value='A' uncheck-value='I'/>
                            <span class="reset-status reset"><g:message code="reset"/></span>
                        </span>
                    </td>
                </tr>
                <g:each in="${customers}" var="customer" status="i">
                    <tr class="data-row">
                        <g:set var="id" value="${customer.id}"/>
                        <input type="hidden" name="id" value="${id}">
                        <td class="name">
                            <span class="value">${customer.name.encodeAsBMHTML()}</span>
                        </td>
                        <td class="email">
                            <span class="value">${customer.userName.encodeAsBMHTML()}</span>
                        </td>

                        <td class="store-credit">
                            <span class="value">${customer.storeCredit.toAdminPrice()}</span>
                        </td>

                        <td class="togglable customer-status">
                            <input type="checkbox" class="single" name="${id}.status" value="A" uncheck-value="I" ${customer.status == "A" ? "checked" : ""} disabled>
                        </td>
                    </tr>
                </g:each>
            </table>
        </div>
        <div class="form-row">
            <label>&nbsp;</label>
            <button type="button" class="submit-button"><g:message code="update"/></button>
        </div>
    </div>
</div>
