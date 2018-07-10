<form class="edit-popup-form create-edit-form" action="${app.relativeBaseUrl()}filterAdmin/assignFilter" method="post">
    <div class="form-section">
        <input type="hidden" name="profile_id" value="${profile?.id}">
        <div class="filter-tile-view">
            <g:each in="${filters}" var="filter">
                <g:if test="${profile?.filters?.contains(filter)}">
                    <div class="filter-tile selected" entity-id="${filter.id}">
                        ${filter.name}
                        <input type="hidden" name="selected_filter" value="${filter.id}"/>
                    </div>
                </g:if>
                <g:else>
                    <div class="filter-tile" entity-id="${filter.id}">
                        ${filter.name}
                    </div>
                </g:else>
            </g:each>
        </div>
        <div class="filter-tile-view">
            <g:each in="${filterGroups}" var="filterGroup">
                <g:if test="${profile?.filterGroups?.contains(filterGroup)}">
                    <div class="filter-group-tile selected" entity-id="${filterGroup.id}">
                        ${filterGroup.name}
                        <input type="hidden" name="selected_filter_group" value="${filterGroup.id}"/>
                    </div>
                </g:if>
                <g:else>
                    <div class="filter-group-tile" entity-id="${filterGroup.id}">
                        ${filterGroup.name}
                    </div>
                </g:else>
            </g:each>
        </div>
        <div class="button-line">
            <button type="submit" class="submit-button"><g:message code="${"add"}"/></button>
            <button type="button" class="cancel-button"><g:message code="cancel"/></button>
        </div>
    </div>
</form>