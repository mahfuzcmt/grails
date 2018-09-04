<%@ page import="com.webcommander.util.StringUtil; grails.converters.JSON; com.webcommander.plugin.variation.VariationType; com.webcommander.plugin.variation.constant.DomainConstants" %>
<div class="toolbar-share">
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <label><g:message code="type"/>:</label>
            <g:select name="typeSelector" from="${[g.message(code: "none"), g.message(code: "disposable")]}" keys="${["all", "disposable"]}" class="small"/>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col class="name-column"/>
            <col class="label-column"/>
            <col class="option-column"/>
            <col class="actions-column"/>
        </colgroup>
        <tr>
            <th><g:message code="type.name"/></th>
            <th><g:message code="label"/></th>
            <th><g:message code="value"/></th>
            <th><g:message code="order"/></th>
            <th class="actions-column"><g:message code="action"/> </th>
        </tr>
        <g:each in="${options}" var="option">
            <tr entity-id="${option.id}">
                <td class="type-name" type="${option.type.id}">${option.type.name.encodeAsBMHTML()}</td>
                <td class="label editable" name="label" validation="required">${option.label.encodeAsBMHTML()}</td>
                <g:if test="${option.type.standard == DomainConstants.VARIATION_REPRESENTATION.TEXT}">
                    <td class="editable" name="value" value-attr="${option.value.encodeAsBMHTML()}">${option.value.encodeAsBMHTML()}</td>
                </g:if>
                <g:elseif test="${option.type.standard == DomainConstants.VARIATION_REPRESENTATION.COLOR}">
                    <td value-attr="${option.value.encodeAsBMHTML()}">
                        <input type='text' name='value' class='color-picker' value="${option.value}"/>
                    </td>
                </g:elseif>
                <g:else>
                    <td class="option-image" value-attr="${option.value.encodeAsBMHTML()}">
                        <g:set var="previewId" value="${StringUtil.uuid}"/>
                        <span class="file-wrapper">
                            <input type="file" name="representingImage" clazz="small" class="image-chooser" file-type="image" size-limit="${5*1024}" previewer="preview-${previewId}">
                            <span class="image-preview"><img id="preview-${previewId}" src="${appResource.getVariationImageUrl(image: option, sizeOrPrefix: "16")}"></span>
                        </span>
                    </td>
                </g:else>
                <td class="label-order editable" type="text"  value-attr="${option.idx}" name="order" restrict="numeric" validation="required number">${option.idx}</td>
                <td class="actions-column">
                    <span class="tool-icon remove" entity-id="${option.id}" entity-name="${(option.type.name + " : " + option.value).encodeAsBMHTML()}" title="<g:message code="remove"/>"></span>
                </td>
            </tr>
        </g:each>
        <tr class="represent-type last-row">
            <td class="chosen-wrapper">
                <g:select class="td-full-width" id="variation-type-selector" validation="required" name="type" from="${types}" optionKey="id" optionValue="name"
                          noSelection="${["": g.message(code: 'choose.type')]}" types='${VariationType.all?.collectEntries {[(it.id): it.standard]} as JSON}'/>
            </td>
            <td class="represent-label">
                <input class="td-full-width" type="text" name="label" validation="required maxlength[100]" maxlength="100" placeholder="<g:message code="enter.label"/>">
            </td>
            <td class="represent-value"></td>
            <td class="represent-order">
                <input class="td-full-width" type="text" name="order" validation="required number" placeholder="<g:message code="enter.order"/>">
            </td>
            <td class="actions-column"><span class="tool-icon add add-row"></span></td>
        </tr>
    </table>
</div>

<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>