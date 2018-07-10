<%@ page import="com.webcommander.util.AppUtil; com.webcommander.conversion.MassConversions; com.webcommander.constants.*" %>
<div class="right-panel grid-view">
    <g:if test="${profile?.id}">
        <div class="panel-header header-left">
            <span class="create-filter btn-secondary-cta">+ <g:message code="add.filter"/></span>
        </div>
        <div class="body" profile-id="${profile.id}">
            <g:each in="${profile?.filters}" var="filter">
                <div class="assigned-filter" filter-id="${filter.id}">
                    <span>${filter.name}</span> <span class="remove" title="<g:message code="delete"/>" ></span>
                </div>
            </g:each>
            <g:each in="${profile?.filterGroups}" var="filterGroup">
                <g:if test="${filterGroup.isActive}">
                    <div class="assigned-filter-group" filter-group-id="${filterGroup.id}">
                        <span>${filterGroup.name}</span> <span class="remove" title="<g:message code="delete"/>" ></span>
                    </div>
                </g:if>
            </g:each>
        </div>
    </g:if>
    <g:else>
        <div class="empty-container empty-profile">
            <h3><g:message code="please.select.profile"/> </h3>
            <div class="create-profile btn btn-cta">
                <i></i><g:message code="add.new.filter"/>
            </div>
        </div>
    </g:else>
</div>