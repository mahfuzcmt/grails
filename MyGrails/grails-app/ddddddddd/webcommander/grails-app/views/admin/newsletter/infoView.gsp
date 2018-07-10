<div class="info-row">
    <label><g:message code="subject"/></label>
    <span class="value">${newsletter.subject.encodeAsBMHTML()}</span>
</div>

<div class="info-row">
    <label><g:message code="from"/></label>
    <span class="value">${newsletter.sender}</span>
</div>
<g:if test="${newsletter.body}">
    <div class="info-row">
        <label><g:message code="email.body"/></label>
    </div>

    <div class="view-content-block" style="height:250px; overflow: auto;">
        <span>${newsletter.body}</span>
    </div>
</g:if>
<g:if test="${newsletter.newsletterReceivers.size()}">
    <div class="info-row">
        <label><g:message code="receivers"/></label>
    </div>

    <div class="view-content-block receiver">
        <g:if test="${subscriber}">
            <h5><g:message code="subscribers"/></h5>
        </g:if>

        <g:if test="${customers}">
            <h5 class="group-label"><g:message code="customers.label"/></h5>

            <div class="view-content-block" style="max-height: 100px; overflow: auto;">
                <g:each in="${customers}" var="customer">
                    <div class="view-list-row">
                        ${customer.encodeAsBMHTML()}
                    </div>
                </g:each>
            </div>
        </g:if>
        <g:if test="${groups}">
            <h5 class="group-label"><g:message code="customer.groups.label"/></h5>

            <div class="view-content-block" style="max-height: 100px; overflow: auto;">
                <g:each in="${groups}" var="group">
                    <div class="view-list-row">
                        ${group.encodeAsBMHTML()}
                    </div>
                </g:each>
            </div>
        </g:if>
        <g:if test="${email}">
            <h5 class="group-label"><g:message code="emails"/></h5>

            <div class="view-content-block" style="max-height: 100px; overflow-y: auto;">
                <g:each in="${email}" var="em">
                    <div class="view-list-row">
                        ${em.encodeAsBMHTML()}
                    </div>
                </g:each>
            </div>
        </g:if>

    </div>
</g:if>