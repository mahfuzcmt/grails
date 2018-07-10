<%@ page import="com.webcommander.webcommerce.Product; com.webcommander.constants.DomainConstants; com.webcommander.conversion.*" %>

<div class="table-view custom-properties-container">
    <div class="body">
        <table class="custom-properties">
            <colgroup>
                <col style="width: 25%">
                <col style="width: 65%">
                <col style="width: 10%">
            </colgroup>
            <thead>
            <tr>
                <th><g:message code="label"/></th>
                <th><g:message code="description"/></th>
                <th><g:message code="actions"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${customProperties}" var="cp">
                <tr entity-id="${cp.id}">
                    <td class="editable key" style="word-wrap: break-word">${cp.label.encodeAsBMHTML()}</td>
                    <td class="editable value" style="word-wrap: break-word">${cp.description.encodeAsBMHTML()}</td>
                    <td>
                        <span class="tool-icon move-controls">
                            <span class="move-up"></span>
                            <span class="move-down"></span>
                        </span>
                        &nbsp;&nbsp;
                        <span class="tool-icon remove" entity-id="${cp.id}"></span>
                    </td>
                </tr>
            </g:each>
            <tr class="last-row">
                <td><input type="text" validation="required maxlength[100]" name="key" class="td-full-width key" placeholder="<g:message code="label"/>"></td>
                <td><input type="text" validation="required maxlength[1000]" name="value" class="td-full-width"  placeholder="<g:message code="description"/>"></td>
                <td><span class="tool-icon add add-row"></span></td>
            </tr>
            </tbody>
        </table>
    </div>

    <div class="footer" hidden="true">
        <ui:perPageCountSelector/>
        <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
    </div>
</div>
