package com.webcommander.plugin.variation

import com.webcommander.constants.DomainConstants
import com.webcommander.models.ProductData
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product

class VariationTagLib {
    VariationService variationService
    static namespace = "variation"

    def allowed = { attrs, body ->
        if(variationService.allowed()) {
            out << body()
        }
    }

    def allowedStandard = { attrs, body ->
        if(allowed() && variationService.allowedStandard()) {
            out << body()
        }
    }

    def allowedEnterprise = { attrs, body ->
        if(allowed() && variationService.allowedEnterprise()) {
            out << body()
        }
    }

    def adminJSs = { attrs, body ->
        out << body()
        if(variationService.allowed()) {
            out << app.javascript(src: 'plugins/variation/js/shared/variation-selection.js')
        }
    }

    def siteJSs = { attrs, body ->
        out << body()
        if(variationService.allowed()) {
            out << app.javascript(src: 'plugins/variation/js/shared/variation-selection.js')
        }
    }

    def productEditorTabHeader = { Map attrs, body ->
        out << body()
        Product product = attrs.product
        if(variationService.allowed() && com.webcommander.plugin.variation.constant.DomainConstants.VARIATION_MODELS && !product.isCombined) {
            out << '<div class="bmui-tab-header" data-tabify-tab-id="variation" data-tabify-url="' + app.relativeBaseUrl() + 'variationAdmin/productEditorTabView?id=' + pageScope.productId + '">'
            out << '<span class="title">' + g.message(code: "variation") + '</span>'
            out << '</div>'
        }

    }

    def productPageConfig = { attrs, body ->
        out << body()
        if(variationService.allowed()) {
            Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT)
            out << "<div class='double-input-row'>" +
                    "<div class='form-row'>" +
                    "<input type='checkbox' class='single toggle-reverse' name='product.enable_matrix_view' value='true' uncheck-value='false' toggle-target='matrix-view' " +
                    "${config.enable_matrix_view == 'true' ? 'checked' : '' }>" +
                    "<span>${g.message(code: "enable.matrix.view")}</span>" +
                    "<span class='suggestion'>${g.message(code: "applicable.for.two.variation.types")}</span>" +
                    "</div><div class='form-row matrix-view'>" +
                    "<input type='checkbox' do-reverse-toggle class='single' name='product.enable_flate_chooser' value='true' uncheck-value='false' ${config.enable_flate_chooser == 'true' ? 'checked' : '' }>" +
                    "<span>${g.message(code: "enable.flat.chooser")}</span>" +
                    "</div>" +
                    "</div>"
        }
    }

    def variationWidget = { attrs, body ->
        out << body()
        if(variationService.allowed()) {
            out << g.include(view: "plugins/variation/productWidget/variationWidget.gsp")
        }
    }

    def matrixView  = { attrs, body ->
        Product product = attrs.product
        List typeOption = attrs.typeOption
        Long selected = attrs.selected
        List<VariationOption> options1 = typeOption[0].options;
        List<VariationOption> options2 = typeOption[1].options;
        int row = options1.size();
        int col = options2.size();
        out << "<table class='matrix combination-matrix'>"
        for (int i = 0; i <= row; i++ ) {
            out << '<tr class="matrix-row">'
            for (int j = 0; j <= col; j++) {
                if (i==0 && j == 0) {
                    out << '<th class="cell label"></th>'
                } else if(i == 0) {
                    def op = options2[j - 1]
                    out << optionCell(op)
                } else if (j == 0) {
                    def op = options1[i - 1]
                    out << optionCell(op)
                } else {
                    List combination = [options1[i-1].id, options2[j-1].id]
                    def variation = variationService.getVariationByOptions(product, combination)
                    if (variation) {
                        Long id = variation.id
                        out << "<td class='cell${variation.active ? ' available' : ''}${id == selected ? ' selected' : ''}' v-id='${id}'></td>"
                    } else {
                        out << "<td class='cell'></td>"
                    }
                }
            }
            out << '</tr>'
        }
        out << "</table>"
    }

    def flateView = { attrs, body ->
        Product product = attrs.product
        List typeOptions = attrs.typeOption
        ProductVariation variation = attrs.selected
        out << "<div class='variation-thumb'>"
        typeOptions.eachWithIndex { typeOption, i ->
            VariationType type = typeOption.type
            List<VariationOption> options = typeOption.options
            List combination = variation.options? variation.options.id.flatten() : []
            out << "<div class='variation-type ${type.standard}' type-name='type-${i}' type-id='${type.id}' type-type='flate'>" +
                    "<label class='type-label'>${(type.name ?: type.name).encodeAsBMHTML()}:</label>" +
                    "<ul class='options'>"
            options.each { option ->
                def selected = combination.contains(option.id)
                out << "<li class='option-cell ${selected ? "selected" : ""}' option-id='${option.id}'>"
                if(type.standard == 'color') {
                    out << "<span class='variation-value color' style='background-color:${option.value};' title='${option.label?.encodeAsBMHTML()}'></span>"
                } else if(type.standard == 'image') {
                    out << "<img class='variation-value image' src='${app.customResourceBaseUrl()}resources/variation/option/option-${option.id}/16-${option.value}' title='${option.label?.encodeAsBMHTML()}'>"
                } else {
                    out << "<span class='variation-value text'>${option.label ? option.label.encodeAsBMHTML() : option.value.encodeAsBMHTML()}</span>"
                }
                out << "</li>"
            }
            out << "</ul>" +
                    "</div>"
        }
        out << "</div>"
    }

    private def optionCell(VariationOption option) {
        VariationType type = option.type
        String cell = ""
        if(type.standard == "color") {
            cell = "<th class='cell label'><span class='color' style='background-color: ${option.value}'></span></th>"
        } else if (type.standard == "image") {
            cell = "<th class='cell label'><span class='image'><img src='${app.customResourceBaseUrl()}resources/variation/option/option-${option.id}/16-${option.value}'></span></th>"
        } else {
            cell = "<th class='cell label'><span class='text'>${option.value.encodeAsBMHTML()}</span></th>"
        }
    }

    def variationPopup = { attrs, body ->
        out << body()
        Map model = pageScope.variables
        ProductData data = model.productData
        model.chosenOnly = true

        if(data.attrs['selectedVariation']) {
            out << "<h4 class='title'>${g.message(code: "variation.combination")}</h4>"
            out << g.include(view: "plugins/variation/site/variationSelection.gsp", model: model)
        }
    }

    def googleProductConfig = { attrs, body ->
        out << body()
        if(variationService.allowed()) {
            List variations = VariationType.createCriteria().list {
                projections {
                    distinct("name")
                }
                eq("isDisposable", false)
            }
            if(variations) {
                Map model = pageScope.variables
                model.variations = variations
                out << g.include(view: "plugins/variation/admin/googleProductConfig.gsp", model: model)
            }
        }
    }
}