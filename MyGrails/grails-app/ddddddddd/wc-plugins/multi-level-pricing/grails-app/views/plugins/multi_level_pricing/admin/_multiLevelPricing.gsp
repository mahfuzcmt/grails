<%@ page import="com.webcommander.admin.CustomerGroup" %>
<div class="form-row">
    <input type="checkbox" class="single" name="isMultiLevelPriceActive" value="true" uncheck-value="false" toggle-target="mlp-fields" ${multiLevelPrice && multiLevelPrice.isActive ? "checked" : ""}>
    <span><g:message code="multi.level.pricing"/></span>
</div>

<div class="multi-level-prices mlp-fields">
    <g:each in="${multiLevelPrice?.prices}" var="productPrice" status="i">
        <div class="multi-level-price row double-input-row" data-row_id="${i}">
            <div class="form-row">
                <ui:domainSelect domain="${CustomerGroup}" class="medium" name="mlp.${i}.customerGroup" custom-attrs="${[multiple: 'true', "data-placeholder": g.message(code: "select.customer.groups")]}"  text="name" values="${productPrice.customerGroups.id}" validation="required"/>
            </div>
            <div class="form-row">
                <input type="text" name="mlp.${i}.price" value="${productPrice.price}"  placeholder="${g.message(code: "price")}" restrict="decimal" validation="required price gt[0]" >
            </div>
            <span class="tool-icon remove"></span>
        </div>
    </g:each>
</div>
<div class="form-row mlp-fields">
    <span class="add-price link-btn">${g.message(code: "add.a.price")}</span>
</div>
<div class="multi-level-price row template hidden double-input-row mlp-fields">
    <div class="form-row">
        <ui:domainSelect domain="${CustomerGroup}" class="medium raw" name="customerGroup" custom-attrs="${[multiple: 'true', "data-placeholder": g.message(code: "select.customer.groups")]}"  text="name" validation="required"/>
    </div>
    <div class="form-row">
        <input type="text" name="price" value="" placeholder="${g.message(code: "price")}" restrict="decimal" validation="required price gt[0]" >
    </div>
    <span class="tool-icon remove"></span>
</div>

