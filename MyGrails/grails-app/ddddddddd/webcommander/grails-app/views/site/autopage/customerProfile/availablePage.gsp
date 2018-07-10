<h1><g:message code="assigned.pages"/></h1>
<g:if test="${pages}">
    <p><g:message code="there.some.extra.pages.for.you"/> :</p>
    <ul class="available-pages">
        <g:each in="${pages}" var="page">
            <li><a href="<app:relativeBaseUrl/>${page.url}" target="_blank">${page.name.encodeAsBMHTML()}</a></li>
        </g:each>
    </ul>
</g:if>
<g:else>
    <p class="no-extra-page-mgs"><g:message code="there.no.extra.pages.for.you"/></p>
</g:else>
