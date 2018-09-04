<%@ page import="com.webcommander.constants.*" %>
<div class="info-row" xmlns="http://www.w3.org/1999/html">
    <label><g:message code="name"/></label>
    <span class="value">${zone.name.encodeAsBMHTML()}</span>
</div>

<div class="info-row">
    <label><g:message code="country"/></label>
    <g:set var="count" value="${zone.countries.size() - 1}"/>
    <span class="value">
        <g:each in="${zone.countries}" var="country" status="i">
            ${country.name.encodeAsBMHTML()} ${i == count ? "" : ","}
        </g:each>
    </span>
</div>
<g:if test="${zone.states.size() > 0}">
    <div class="info-row">
        <g:set var="count" value="${zone.states.size() - 1}"/>
        <label><g:message code="state"/></label>
        <span class="value">
            <g:each in="${zone.states}" var="state" status="i">
                ${state.name.encodeAsBMHTML()} ${i == count ? "" : ","}
            </g:each>
        </span>
    </div>
</g:if>
<g:if test="${zone.postCodes.size() > 0}">
    <div class="info-row">
        <g:set var="count" value="${zone.postCodes.size() - 1}"/>
        <label><g:message code="post.code"/></label>
        <span class="value">
            <g:each in="${zone.postCodes}" var="postcode" status="i">
                ${postcode.encodeAsBMHTML()} ${i == count ? "" : ","}
            </g:each>
        </span>
    </div>
</g:if>
