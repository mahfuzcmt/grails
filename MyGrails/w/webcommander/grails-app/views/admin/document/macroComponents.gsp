<%@ page import="com.webcommander.constants.NamedConstants" %>
<g:set var="macros" value="${NamedConstants.DOCUMENT_MACROS[type]}"/>
<%
    int length = macros.size()
    for (int i = 0; i < length; i) {
        String macro1 = macros[i]
        String macro2 = macros[i+1]
%>
<div class="component-pair">
    <span class="component-item" data-type="macro" data-macro="${macro1}">
        <span class="title"><g:message code="${macro1.replaceAll("_", ".")}"/></span>
    </span>
    <%i++%>
    <g:if test="${macro2}">
        <span class="component-item" data-type="macro" data-macro="${macro2}">
            <span class="title"><g:message code="${macro2.replaceAll("_", ".")}"/></span>
        </span>
        <%i++%>
    </g:if>
</div>
<%}%>