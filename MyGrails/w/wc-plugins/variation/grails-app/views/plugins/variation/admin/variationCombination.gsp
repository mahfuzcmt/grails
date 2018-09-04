<%@ page import="com.webcommander.plugin.variation.VariationOption; com.webcommander.plugin.variation.ProductVariation; com.webcommander.plugin.variation.VariationType" %>
<g:set var="select" value="${[]}"/>
<div class="variation-combination">
    <div class="combination-dropdown">
        <g:each in="${config.findAll {it.key.startsWith("combobox")}}" var="typeBox" status="i">
            <g:set var="typeOption" value="${typeOptionsMap.find {it.type.id == typeBox.value}}"/>
            <g:set var="value" value="${typeOption['c-value' + (i + 1)]}"/>
            <% select.push(value ?: typeOption['selected'][0].id)%>
            <div class="form-row chosen-wrapper select-option">
                <label>${typeOption.type.name.encodeAsBMHTML()}</label>
                <g:set var="type" value="${typeOption.type as VariationType}"/>
                <g:set var="options" value="${typeOption['selected'] as List<VariationOption>}"/>
                <div class="product-variation-select wcui-select" name="${typeBox.key}">
                    <g:each in="${options}" var="option" status="j">
                        ${option.id +" " + value}
                        <span class="options ${((option.id == value) || (j == 0 && !value)) ? "selected" : ""}" value="${option.id}">
                            <g:if test="${type.standard == 'color'}">
                                <span class="variation-value color" style="background-color:${option.value};"></span>
                            </g:if>
                            <g:elseif test="${type.standard == 'image'}">
                                <span class="variation-value">
                                    <img src="${appResource.getVariationImageUrl(image: option, sizeOrPrefix: "16")}">
                                </span>
                            </g:elseif>
                            <g:else>
                                <span class="variation-value">${option.value.encodeAsBMHTML()}</span>
                            </g:else>
                        </span>
                    </g:each>
                </div>

            </div>
        </g:each>
    </div>
    <div class="matrix-container">
        <g:set var="xAxis" value="${typeOptionsMap.find {it.type.id == config['xAxis']}}"/>
        <g:set var="yAxis" value="${typeOptionsMap.find {it.type.id == config['yAxis']}}"/>
        <span class="x-title">${xAxis.type.name.encodeAsBMHTML()}</span>
        <g:if test="${yAxis}">
            <span class="y-title">${yAxis.type.name.encodeAsBMHTML()}</span>
        </g:if>
        <div class="matrix-table">
            <g:each in="${xAxis['selected']}" var="x" status="i">
                <div class="matrix-row">
                    <g:if test="${yAxis}">
                        <g:each in="${yAxis['selected']}" var="y" status="j">
                            <g:set var="pVariation" value="${ProductVariation.lookUpVariation(product.id, [select, x.id, y.id].flatten())}"/>
                            <g:set var="data" value="${pVariation.findData()}"/>
                            <div class="matrix-cell" v-id="${pVariation?.id}">
                                <span class="representation">
                                    [<g:include view="plugins/variation/admin/options.gsp" model="[option: x]"/>, <g:include view="plugins/variation/admin/options.gsp" model="[option: y]"/>]
                                </span>
                                <span class="actions">
                                    <span class="${pVariation?.active ? 'action-navigator collapsed variation-config' : 'tool-icon variation-active'}" entity-id="${pVariation?.id}"
                                          entity-active="${pVariation.active}" entity-name="${product.name.encodeAsBMHTML()}" entity-base="${pVariation.isBase}" entity-combination="${x.value + ", " + y.value}"
                                          entity-url="${data?.url}" title="<g:message code="${pVariation?.active ? 'configuration' : 'activate'}"/>"></span>
                                    <g:if test="${pVariation.isBase}">
                                        <span class="tool-icon base-combination" title="<g:message code="default.variation"/>"></span>
                                    </g:if>
                                </span>
                            </div>
                        </g:each>
                    </g:if>
                    <g:else>
                        <g:set var="pVariation" value="${ProductVariation.lookUpVariation(product.id, [x.id])}"/>
                        <g:set var="data" value="${pVariation.findData()}"/>
                        <div class="matrix-cell" v-id="${pVariation?.id}">
                            <span class="representation">
                                <g:include view="plugins/variation/admin/options.gsp" model="[option: x]"/>
                            </span>
                            <span class="actions">
                                <span class="${pVariation?.active ? 'action-navigator collapsed variation-config' : 'tool-icon variation-active'}" entity-id="${pVariation?.id}"
                                      entity-combination="${x.value}" entity-active="${pVariation.active}" entity-url="${data?.url}" entity-base="${pVariation.isBase}" entity-name="${product.name.encodeAsBMHTML()}"
                                      title="<g:message code="${pVariation?.active ? 'configuration' : 'activate'}"/>"></span>
                                <g:if test="${pVariation.isBase}">
                                    <span class="tool-icon base-combination" title="<g:message code="default.variation"/>"></span>
                                </g:if>
                            </span>
                        </div>
                    </g:else>
                </div>
            </g:each>
        </div>
    </div>
</div>