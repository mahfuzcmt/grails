<g:if test="${!noTitle}">
    <div class="sidebar-group title-input">
        <span class="sidebar-group-label"><g:message code="title"/></span>
        <div class="sidebar-group-body">
            <input class="sidebar-input" type="text" maxlength="250" validation="maxlength[250]" name="title" value="${widget.title}">
        </div>
    </div>
</g:if>
<div class="sidebar-group clazz-input">
    <span class="sidebar-group-label"><g:message code="class.name"/></span>
    <div class="sidebar-group-body">
        <input class="sidebar-input" type="text" maxlength="250" validation="maxlength[250]" name="clazz" value="${widget.clazz}">
    </div>
</div>
<div class="widget-specific-config">
    <g:layoutBody/>
</div>
<g:if test="${!noAdvance}">
    <div class="advance-config-btn"><input type="button" value="${advanceText}"></div>
</g:if>
<g:if test="${!params.cache}">
    <textarea class="widget-cache" style="display: none">${widget.serialize().encodeAsHTML()}</textarea>
</g:if>