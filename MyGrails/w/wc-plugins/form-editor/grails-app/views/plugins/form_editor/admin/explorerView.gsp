<div class="right-panel grid-view">
    <div class="body">
        <g:if test="${form == null}">
            <div class="empty-form-content">
                <p><g:message code="please.select.form.from.left.view"/> </p>
                <p><g:message code="you.can.create.form.by.new.btn" encodeAs="raw" args="${['<span class="highlight create-from"> + New</span>']}"/></p>
                <p><g:message code="please.use.search.to.search" encodeAs="raw" args="${['<span class="search-icon ">  &#8981; </span>']}"/></p>
            </div>
        </g:if>
        <g:else>
            <iframe class="form-preview" src="<app:baseUrl />formAdmin/preview?id=${form.id}"></iframe>
            <div class="div-mask"></div>
        </g:else>

    </div>
</div>