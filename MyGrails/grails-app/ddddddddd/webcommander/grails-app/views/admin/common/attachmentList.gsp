<g:each in="${listMap}" var="entity">
    <h4 class="group-label"><g:message code="${entity.key}"/></h4>
    <div class="view-content-block">
        <g:each in="${entity.value}" var="entry">
            <div class="view-list-row">
                ${entry.encodeAsBMHTML()}
            </div>
        </g:each>
    </div>
</g:each>
