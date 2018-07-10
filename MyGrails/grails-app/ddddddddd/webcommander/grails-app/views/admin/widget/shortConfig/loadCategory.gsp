<%@ page import="com.webcommander.constants.NamedConstants" %>
<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group pagination-row">
        <div class="sidebar-group-label"><g:message code="show.pagination"/></div>
        <ui:namedSelect class="sidebar-input show-pagination" toggle-target="pagination" name="show-pagination" key="${NamedConstants.PAGINATION_MESSAGE}" value="${config["show-pagination"]}"/>
    </div>
    <div class="sidebar-group item-per-page pagination-top pagination-bottom pagination-top_and_bottom">
        <div class="sidebar-group-label"><g:message code="number.of.categories"/></div>
        <input type="text" class="sidebar-input" name="item_per_page" value="${config["item_per_page"]}" validation="skip@if{self::hidden} digits gt[0]"/>
    </div>
    <div class="sidebar-group item-par-page-selection pagination-top pagination-bottom pagination-top_and_bottom">
        <input class="single" type="checkbox" name="item-per-page-selection" value="true" uncheck-value="false" ${config["item-per-page-selection"] == "true" ? "checked" : ""}>
        <label><g:message code="item.per.page.selection"/></label>
    </div>
    <div class="sidebar-group">
        <input class="single" type="checkbox" name="description" value="true" uncheck-value="false" ${config["description"] == "true" ? "checked" : ""}>
        <label><g:message code="short.description"/></label>
    </div>
</g:applyLayout>