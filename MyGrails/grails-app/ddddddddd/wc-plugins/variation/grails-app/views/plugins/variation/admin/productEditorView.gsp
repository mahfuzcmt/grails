<%@ page import="com.webcommander.manager.LicenseManager; com.webcommander.plugin.variation.ProductVariation; com.webcommander.plugin.variation.constant.DomainConstants" %>
<div class="form-section">
    <div class="form-section-info">
        <h3><g:message code="variation.information"/></h3>
        <div class="info-content"><g:message code="section.text.product.variation.edit"/></div>
    </div>
    <div class="form-section-container">
        <div class="bmui-tab">
            <div class="bmui-tab-header-container top-side-header">
                <div class="bmui-tab-header" data-tabify-tab-id="assign"}>
                    <span class="title"><g:message code="assign.variation"/></span>
                </div>
                <g:if test="${model}">
                    <div class="bmui-tab-header" data-tabify-tab-id="combination" ${hasVariation ? "active" : "disabled"}>
                        <span class="title"><g:message code="variation.combination"/></span>
                    </div>
                </g:if>
            </div>
            <div class="bmui-tab-body-container">
                <div id="bmui-tab-assign">
                    <form action="${app.relativeBaseUrl()}variationAdmin/saveVariation" method="post" class="edit-popup-form create-edit-form">
                        <input type="hidden" name="pId" value="${product.id}"/>
                        <input type="hidden" name="id" class="variation-id" value="${variation.id}"/>
                        <div class="form-row">
                            <g:if test="${DomainConstants.VARIATION_MODELS.size()}">
                                <label><g:message code="variation.model"/></label>
                                <g:each in="${DomainConstants.VARIATION_MODELS}" var="storedModel">
                                    <g:set var="variationModel" value="${storedModel.value}"/>
                                    <input type="radio" name="model" value="${storedModel.key}" ${model == storedModel.key ? "checked" : ""} ${(model && model != storedModel.key) || !LicenseManager.isAllowed(variationModel.license) ? "disabled" : ""}>
                                    <span class="value"><g:message code="${variationModel.label}"/></span>
                                </g:each>
                            </g:if>
                            <g:else>
                                <label><g:message code="variation.model.not.found" /></label>
                            </g:else>
                        </div>
                        <div class="form-row mandatory mandatory-chosen-wrapper">
                            <label><g:message code="variation.type"/><span class="suggestion"> <g:message code="select.variation.type"/></span></label>
                            <g:select name="variationType" from="${types}" optionKey="id" optionValue="name" multiple="multiple" data-placeholder="${g.message(code: 'assign.a.type')}"
                                      value="${selectedTypes}" validation="required" noSelection="['add-variation-type':'+ Add Variation Type']"/>

                            <table class="content variation-type-add-section hidden">
                                <tr class="last-row">
                                    <td><input name="name" type="text" class="td-full-width variation-type-input small" placeholder="<g:message code="name"/>" maxlength="100"></td>
                                    <td class="chosen-wrapper">
                                        <g:set var="vTypes" value="${DomainConstants.VARIATION_REPRESENTATION.values()}"/>
                                        <g:select class="standards td-full-width small" name="standard" from="${vTypes.collect {g.message(code: it)}}" keys="${vTypes}"/>
                                    </td>
                                    <td class="actions-column">
                                        <span class="tool-icon add add-row validation-type-add"></span>
                                        <span class="cancel remove-variation-type-row"> Cancel </span>
                                    </td>
                                </tr>
                            </table>
                        </div>
                        <div class="option-chooser-wrapper">
                            <g:each in="${typeOptionsMaps}" var="mapEntry">
                                <g:include view="/plugins/variation/admin/chooseOptionsForType.gsp" model="${[type: mapEntry.type, allOptions: mapEntry.all, selectedOptions: mapEntry.selected]}"/>
                            </g:each>
                        </div>
                        <g:if test="${!variation.id && DomainConstants.VARIATION_MODELS.size()}">
                            <div class="form-row">
                                <button type="submit" class="edit-popup-form-submit submit-button"><g:message code="generate"/></button>
                            </div>
                        </g:if>
                    </form>
                </div>
                <g:if test="${model}">
                    <div id="bmui-tab-combination">
                        <input type="hidden" class="product-id" name="pId" value="${product.id}"/>
                        <div class="variation-select btn dropdown" model="${model}">
                            <span class="title"><g:message code="select.variation.view"/></span>
                        </div>
                        <g:include controller="variationAdmin" action="loadCombination" params="[pId: product.id]"/>
                    </div>
                </g:if>
                <input type="hidden" class="image-option-add-mode" value="false">
            </div>
        </div>
    </div>
</div>