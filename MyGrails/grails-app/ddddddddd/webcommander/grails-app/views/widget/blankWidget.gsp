<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil" %>
<g:applyLayout name="_widget">
    <g:if test="${AppUtil.request.editMode}">
        <g:message code="${widget.widgetType}"/> <g:message code="widget"/>
    </g:if>
</g:applyLayout>