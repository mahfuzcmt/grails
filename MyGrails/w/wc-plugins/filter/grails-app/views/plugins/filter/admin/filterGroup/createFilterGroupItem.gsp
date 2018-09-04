<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<g:set var="itemTypes" value="${DomainConstants.NAVIGATION_ITEM_TYPE}"></g:set>
<form class="filter-group-item-form fade-in-down" action="${app.relativeBaseUrl()}filterGroup/saveFilterGroupItems" method="post">

    <span class="title"><g:message code="${params.title}"/></span>

    <div class="mandatory form-row">
        <label><g:message code="title"/></label>
        <input type="text" class="medium" name="title" validation="required maxlength[100]" value="${filterGroupItem.title}" maxlength="100">
    </div>
    <div class="form-row mandatory">
        <label><g:message code="heading"/></label>
        <input type="text" class="medium" name="heading" validation="required maxlength[200]" value="${filterGroupItem.heading}" maxlength="200">
    </div>
    <div class="form-row">
        <label><g:message code="url"/><span class="suggestion">e. g. http://www.abc.com</span></label>
        <input type="text" class="medium" name="itemUrl" validation="url maxlength[200]" value="${filterGroupItem.itemUrl}" maxlength="200">
    </div>
    <div class="form-row">
        <label><g:message code="short.description"/></label>
        %{--<input type="text" class="medium" name="shortDescription" value="${filterGroupItem.shortDescription}">--}%
        <textarea class="small" name="shortDescription">${filterGroupItem.shortDescription}</textarea>
    </div>
    <div class="form-row">
        <label><g:message code="detail.description"/></label>
        %{--<input type="text" class="medium" name="detailDescription" value="${filterGroupItem.detailDescription}">--}%
        <textarea class="small" name="detailDescription">${filterGroupItem.detailDescription}</textarea>
    </div>
    <div class="form-row thicker-row">
        <label><g:message code="item.image"/></label>
        <div class="form-image-block">
            <input type="file" name="item-image" file-type="image" size-limit="2097152" submit-data="${filterGroupItem.id}"
                ${filterGroupItem.image ? 'remove-support="true"' :
                        'reset-support="' + 'true"'} previewer="filter-group-logo-preview" remove-option-name="remove-image">
            <div class="preview-image">
                <g:set var="logoPath" value="${appResource.getFilterGroupItemImageURL(filterGroupItem: filterGroupItem)}"/>
                <img id="filter-group-logo-preview" src="${logoPath}" alt="${filterGroupItem.imageAlt}">
            </div>
        </div>
    </div>

    <div class="form-row">
        <label><g:message code="image.alt"/></label>
        <input type="text" class="medium" name="imageAlt" validation="maxlength[100]" value="${filterGroupItem.imageAlt}" maxlength="100">
    </div>

    <div class="form-row">
        <button type="submit" class="submit-button"><g:message code="${filterGroupItem.title ? "update" : "save"}"/></button>
        <g:if test="${filterGroupItem.title}"><button type="button" class="cancel-button"><g:message code="cancel"/></button></g:if>
    </div>

</form>
