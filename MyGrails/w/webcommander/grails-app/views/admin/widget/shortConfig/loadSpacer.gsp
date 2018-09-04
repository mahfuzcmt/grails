<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group type-text">
        <div class="sidebar-group-label"><g:message code="height"/><span class="suggestion"> eg. 20,30,40</span></div>
        <div class="sidebar-group-body">
            <input type="text" class="sidebar-input" name="height" value="${config.height}" validation="required number gt[0]" restrict="numeric">
        </div>
        <div class="sidebar-group-label"><g:message code="height.in.tab"/></div>
        <div class="sidebar-group-body">
            <input type="text" class="sidebar-input" name="height_in_tab" value="${config.height_in_tab}" validation="required number gt[0]" restrict="numeric">
        </div>
        <div class="sidebar-group-label"><g:message code="height.in.mobile"/></div>
        <div class="sidebar-group-body">
            <input type="text" class="sidebar-input" name="height_in_mobile" value="${config.height_in_mobile}" validation="required number gt[0]" restrict="numeric">
        </div>
    </div>
</g:applyLayout>
