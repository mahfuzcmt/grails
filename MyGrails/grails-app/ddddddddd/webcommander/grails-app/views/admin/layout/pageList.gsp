<g:if test="${pageList["customPage"].size() > 0}">
    <h4 class="group-label"><g:message code="custom.pages"/></h4>
    <div class="view-content-block">
        <g:each in="${pageList["customPage"]}" var="page">
            <div class="view-list-row">
                ${page.name.encodeAsBMHTML()} ${page.isInTrash ? "(" + g.message(code: "in.trash") + ")" : ""}
            </div>
        </g:each>
    </div>
</g:if>
<g:if test="${pageList["systemPage"].size() > 0}">
    <h4 class="group-label"><g:message code="system.pages"/></h4>
    <div class="view-content-block">
        <g:each in="${pageList["systemPage"]}" var="page">
            <div class="view-list-row"><g:message code="${page}"/></div>
        </g:each>
    </div>
</g:if>
<g:if test="${pageList["customPage"].size() == 0 && pageList["systemPage"].size() == 0}">
    <div class="view-content-block">
        <g:message code="no.attached.page"/>
    </div>
</g:if>