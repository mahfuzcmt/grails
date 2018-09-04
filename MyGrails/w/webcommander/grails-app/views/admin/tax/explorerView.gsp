<%@ page import="com.webcommander.util.AppUtil; com.webcommander.conversion.MassConversions; com.webcommander.constants.*" %>
<div class="right-panel grid-view">
    <div class="body">
        <g:if test="${rule}">
            <div class="shipping-rule-container">
                <g:include view="admin/tax/new/rule/editor.gsp" model="[rule: rule]"/>
            </div>
        </g:if>
        <g:else>
            <div class="empty-item-content">
                <p><g:message code="click.new.btn.add.tax" encodeAs="raw" args="${['<span class="highlight create-profile"> + New</span>']}"/></p>
            </div>
        </g:else>
    </div>
</div>