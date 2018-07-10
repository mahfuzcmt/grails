<g:applyLayout name="_widget">
    <g:if test="${request.page}">
        <%
            if (!request.tab_accordion_script_loaded) {
                request.js_cache.push("plugins/tab-accordion/js/shared/tab-accordion.js")
                request.tab_accordion_script_loaded = true;
            }
        %>
    </g:if>
    <g:if test="${params?.loadJs}">
        <app:javascript src="plugins/tab-accordion/js/shared/tab-accordion.js"/>
    </g:if>
    <g:if test="${config.type == 'accordion'}">
        <g:include view="plugins/tab_accordion/widget/accordion.gsp" model="${pageScope.variables}"/>
    </g:if>
    <g:else>
        <g:include view="plugins/tab_accordion/widget/tab.gsp" model="${pageScope.variables}"/>
    </g:else>
</g:applyLayout>