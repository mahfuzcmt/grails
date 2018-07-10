
%{--<div class="form-section">
    <div class="form-section-info">
        <h3><g:message code="custom.properties"/></h3>
        <div class="info-content"></div>
    </div>
    <div class="form-section-container">
        <g:include controller="compareProductAdmin" action="loadCustomProperties" params="${[productId: productId]}" />
    </div>
</div>--}%

%{--<div class="section-separator"></div>
<div class="form-section">
    <div class="form-section-info">
        <h3><g:message code="custom.fields.order"/></h3>
        <div class="info-content"></div>
    </div>
    <div class="form-section-container">
        <g:include controller="productCustomField" action="productEditorTabView" params="${[id: productId]}" />
    </div>
</div>--}%

%{--<div class="section-separator"></div>--}%
<form class="create-edit-form" action="${app.relativeBaseUrl()}filterGroup/mapProductFilterGroup">
<input type="hidden" name="productId" value="${productId}">
<div class="form-section">
    <div class="form-section-info">
        <h3><g:message code="filter.group"/></h3>
        <div class="info-content"></div>
    </div>
    <div class="form-section-container">

        <g:each in="${filterGroups}" var="filterGroup">
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label>${filterGroup.name.encodeAsBMHTML()}</label>
                    <g:select name="filter-group-item" class="medium" from="${filterGroup.items}" optionValue="heading" optionKey="id" value="${filterGroupSelectedValues[filterGroup.id]}" noSelection="['': g.message(code: 'none')]" />
                </div>
                <div class="form-row">
                    <span class="action-navigator collapsed filter-group-items" entity-id="${filterGroup.id}" entity-name="${filterGroup.name}" title="<g:message code="add.item"/>" ></span>
                </div>
            </div>
        </g:each>

        <div class="form-row">
            <button type="submit" class="submit-button"><g:message code="update"/></button>
        </div>

    </div>
</div>
</form>

<script>

    $( ".filter-group-items" ).on( "click", function() {

        var itemClicked = $(this);
        var id = itemClicked.attr("entity-id");
        var name = itemClicked.attr("entity-name");

        var tab = app.Tab.getTab("tab-edit-filterGroup-" + id);
        if (!tab) {
            tab = new app.tabs.filterGroupItem({
                filterGroup: {id: id, name: name},
                id: "tab-edit-filterGroup-" + id
            });
            tab.render();
        }
        tab.setActive();
    });

</script>
