<%@ page import="com.webcommander.plugin.variation.VariationOption; com.webcommander.plugin.variation.VariationType; com.webcommander.plugin.variation.ProductVariation; com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<g:set var="config" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT)}"/>
<g:set var="variations" value="${ProductVariation.findAllByProduct(product)}"/>
<g:if test="${variations}">
    <div class="variation-container" data-selected="${productData.attrs['selectedVariation']}">
        <input type="hidden" class="variation-model" value="${variations[0].details.model}">
        <g:set var="options" value="${variations.options.flatten().unique()}"/>
        <g:set var="types" value="${options.type.unique()}"/>
        <g:set var="typeOptionList" value="${types.collect {type -> [type: type, options: options.findAll {it.type == type}.sort { it.idx }]}}"/>
        <g:set var="variation" value="${ProductVariation.get(productData.attrs['selectedVariation']) ?: new ProductVariation()}"/>
        <input type="hidden" name="productId" value="${product.id}">
        <g:if test="${(config.enable_matrix_view?.toBoolean() && types.size() == 2) && !chosenOnly}">
            <variation:matrixView product="${product}" typeOption="${typeOptionList}" selected="${variation.id}"/>
        </g:if>
        <g:elseif test="${config.enable_flate_chooser?.toBoolean() && !chosenOnly}">
            <variation:flateView product="${product}" typeOption="${typeOptionList}" selected="${variation}"/>
        </g:elseif>
        <g:else>
            <div class="variation-dropdown-container">
                <g:each in="${typeOptionList}" var="typeOption" status="i">
                    <g:set var="type" value="${typeOption.type as VariationType}"/>
                    <g:set var="options" value="${typeOption.options as List<VariationOption>}"/>
                    <g:set var="combination" value="${variation.options ? variation.options.id.flatten() : []}"/>
                    <div class="chosen-wrapper variation-type ${type.standard} select" type-name="type${i}" type-id="${type.id}" type-type="select">
                        <label class="type-label">${(type.name ?: type.name).encodeAsBMHTML()}: </label>
                        <div class="product-variation-select wcui-select" name="config.options">
                            <g:each in="${options}" var="option">
                                <g:set var="selected" value="${combination.contains(option.id)}"/>
                                <span class="options ${selected ? "selected" : ""}" value="${option.id}">
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
        </g:else>
    </div>
</g:if>