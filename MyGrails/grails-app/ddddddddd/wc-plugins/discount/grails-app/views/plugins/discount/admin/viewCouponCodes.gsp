<div class="table-view">
    <div class="header">
        <div class="toolbar toolbar-right">
            <form class="search-form tool-group">
                <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="button" class="icon-search"></button>
            </form>
        </div>
    </div>
    <table class="content">
        <colgroup>
            <col style="width: 70%">
            <col style="width: 30%">
        </colgroup>
        <tr>
            <th><g:message code="name"/></th>
            <th><g:message code="coupon.code"/></th>
        </tr>
        <g:if test="${codeList}">
            <g:each in="${codeList}" var="code">
                <tr>
                    <td>${code?.customer?.firstName.encodeAsBMHTML() + (!code?.customer?.isCompany ? (" " + code?.customer?.lastName?.encodeAsBMHTML()) : "") }</td>
                    <td>${code.code}</td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="2"><g:message code="no.coupon.codes"/></td>
            </tr>
        </g:else>
    </table>
    <div class="footer">
        <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
    </div>
</div>