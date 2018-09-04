<g:each in="${templates}" var="template">
    <div class="template ${template.category}" uuid="${template.uuid}" repository-type="${template.repositoryType}" category="${template.category}">
        <img src="${template.thumb}">
        <div class="btn-wrap">
            <button class="btn use"><g:message code="use"/></button>
            <button class="btn show-more"><g:message code="show.more.like.this"/></button>
        </div>
    </div>
</g:each>
