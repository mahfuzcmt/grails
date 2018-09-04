<div class="content">
    <div class="top">
        <span class="title"><g:message code="website.details"/></span>
    </div>
    <div class="body">
        <g:each in="${aliases}" var="alias">
            <div class="alias">
                <label><g:message code="alias"/>:</label>
                <span>${alias.alias}</span>
            </div>
        </g:each>
    </div>
</div>