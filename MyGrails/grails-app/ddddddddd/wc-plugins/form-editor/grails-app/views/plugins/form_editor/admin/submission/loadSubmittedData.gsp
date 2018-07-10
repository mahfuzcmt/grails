<div class="header">
    <span class="item-group entity-count title">
        <g:message code="submitted.data"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <span class="toolbar-item add-filter" title="<g:message code="advance.search"/>"><i></i></span>
            <span class="toolbar-item remove-filter disabled" title="<g:message code="remove.search"/>"><i></i></span>
        </div>
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[g.message(code: "with.selected"), g.message(code: "remove")]}" keys="['', 'delete']"/>
        </div>
        <div class="tool-group">
            <a href="${app.relativeBaseUrl()}formAdmin/exportSubmission?id=${params.id}" title="<g:message code="download"/>">
                <span class="tool-group toolbar-btn export"><g:message code="export"/></span>
            </a>
        </div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col style="width: 3%">
            <col style="width: 53%">
            <col style="width: 30%">
            <col style="width: 17%">
        </colgroup>
        <tr>
            <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            <th><g:message code="submission.date"/></th>
            <th><g:message code="submitted.ip"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>

        <g:each in="${formSubmission}" var="formData">
            <tr>
                <td class="select-column"><input type="checkbox" class="multiple" entity-id="${formData.id}" name="${formData.submitted.encodeAsBMHTML()}"></td>
                <td>${formData.submitted.toAdminFormat(true, false, session.timezone)}</td>
                <td>${formData.ip.encodeAsBMHTML()}</td>
                <td class="actions-column">
                    <span class="action-navigator collapsed" entity-id="${formData.id}" entity-name="${formData.submitted.encodeAsBMHTML()}"></span>
                </td>
            </tr>
        </g:each>

    </table>
</div>

<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>