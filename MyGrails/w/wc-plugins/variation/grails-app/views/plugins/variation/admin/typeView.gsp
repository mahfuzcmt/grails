<%@ page import="grails.converters.JSON; com.webcommander.plugin.variation.constant.DomainConstants" %>
<div class="toolbar-share">
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <label><g:message code="type"/>:</label>
            <g:select name="typeSelector" from="${[g.message(code: "none"), g.message(code: "disposable")]}" keys="${["all", "disposable"]}" class="small"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="name"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col class="name-column"/>
            <col class="standard-column"/>
            <col class="actions-column"/>
        </colgroup>
        <tr>
            <th><g:message code="name"/></th>
            <th><g:message code="standard"/> </th>
            <th class="actions-column"><g:message code="action"/> </th>
        </tr>
        <g:each in="${types}" var="type">
            <tr entity-id="${type.id}">
                <td class="editable name">${type.name.encodeAsBMHTML()}</td>
                <td class="standard" option-count="${type.options.size()}">
                    <span class="text"><g:message code="${type.standard}"/></span>
                </td>
                <td class="actions-column">
                    <span class="tool-icon remove" entity-id="${type.id}" entity-name="${type.name.encodeAsBMHTML()}" title="<g:message code="remove"/>"></span>
                </td>
            </tr>
        </g:each>
        <tr class="last-row">
            <td><input name="name" type="text" class="td-full-width" placeholder="<g:message code="name"/>" validation="required maxlength[100]" maxlength="100"></td>
            <td class="chosen-wrapper">
                <g:set var="vTypes" value="${DomainConstants.VARIATION_REPRESENTATION.values()}"/>
                <g:select class="standards td-full-width" name="standard" from="${vTypes.collect {g.message(code: it)}}" keys="${vTypes}"/>
            </td>
            <td class="actions-column"><span class="tool-icon add add-row"></span></td>
        </tr>
    </table>
</div>

<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>