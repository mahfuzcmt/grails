<%@ page import="com.webcommander.constants.NamedConstants;" %>
<g:applyLayout name="_editWidget">
    <span class="configure-btn" title="<g:message code="configuration"/> "><i></i></span>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="category.selection.info"/></h3>
            <div class="info-content"><g:message code="section.text.category.selection"/></div>
        </div>
        <div class="form-section-container">
            <div class="widget-config-panel">
                <div>
                    <div class="form-row">
                        <label><g:message code="title"/></label>
                        <input type="text" class="medium" name="title" value="${widget.title}">
                    </div>
                    <div class="form-row pagination-row">
                        <label><g:message code="show.pagination"/></label>
                        <ui:namedSelect class="medium show-pagination" toggle-target="pagination" name="show-pagination" key="${NamedConstants.PAGINATION_MESSAGE}"
                                        value="${config["show-pagination"]}"/>
                    </div>
                    <div class="form-row item-per-page pagination-top pagination-bottom pagination-top_and_bottom">
                        <label><g:message code="number.of.categories"/></label>
                        <input type="text" class="medium" name="item_per_page" value="${config["item_per_page"]}"/>
                    </div>
                    <div class="form-row item-par-page-selection pagination-top pagination-bottom pagination-top_and_bottom">
                        <input type="checkbox" class="single" name="item-per-page-selection" value="true" uncheck-value="false" ${config["item-per-page-selection"] == "true" ? "checked" : ""}>
                        <span><g:message code="item.per.page.selection"/></span>
                    </div>
                    <div class="form-row">
                        <input type="checkbox" class="single" name="description" value="true" uncheck-value="false" ${config["description"] == "true" ? "checked" : ""}>
                        <span><g:message code="short.description"/></span>
                    </div>
                </div>
            </div>
            <g:include view="/admin/item/category/selection.gsp" model="${[categories: categories, fieldName: "category"]}"/>
            <div class="form-row">
                <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="update"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</g:applyLayout>