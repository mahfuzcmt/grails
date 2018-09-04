<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<g:set var="defaultStateId" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, 'default_state')}"/>
<g:if test="${states?.size()}"><div class="form-row state-selector-row chosen-wrapper">
        <label><g:message code="${params.stateLabel ?: "state"}"/><span class="suggestion"><g:message code="create.state.suggestion"/></span></label>
        <g:if test="${params.isMultiple}">
            <g:select id="state" multiple="true" class="${params.inputClass ?: 'large'}" name="${params.stateName ?: 'state.id'}" et-category="dropdown"
                      from="${states}" data-placeholder="${g.message(code: 'select.state')}" optionKey="id" optionValue="name" value="${stateId ?: defaultStateId}"/>
        </g:if>
        <g:else>
            <g:select id="state" class="${params.inputClass ?: 'large'}" et-category="dropdown" name="${params.stateName ?: 'state.id'}" from="${states}" optionKey="id" optionValue="name"
                      value="${params.noSelection ? 0 : (stateId ?: defaultStateId)}" data-placeholder="${g.message(code: 'select.state')}" noSelection="${params.noSelection ? ["": params.noSelection] : null}"/>
        </g:else>
    </div>
</g:if>