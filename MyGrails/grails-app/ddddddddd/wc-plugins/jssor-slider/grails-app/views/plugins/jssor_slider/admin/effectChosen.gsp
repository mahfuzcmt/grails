<%@ page import="com.webcommander.plugin.jssor_slider.constant.DomainConstant" %>
<g:set var="animationMap" value="${caption ? DomainConstant.CAPTION_TRANSITIONS : DomainConstant.SLIDE_TRANSITIONS }"/>
<select name="${name}" class="with-opt-group ${clazz}">
    <g:if test="${caption}">
        <option value=""><g:message code="none"/></option>
    </g:if>
    <g:each in="${animationMap}" var="animation">
        <optgroup label="${animation.key}">
            <g:each in="${animation.value}" var="effect">
                <option value="${effect.value}" ${value == effect.value ? 'selected' : ''}>${effect.key}</option>
            </g:each>
        </optgroup>
    </g:each>
</select>