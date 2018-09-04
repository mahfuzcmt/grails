package com.webcommander.plugin.product_custom_information

import com.webcommander.models.ProductData

class ProductCustomInfoTagLib {
    ProductCustomInformationService productCustomInformationService;
    static namespace = "productCustomInfo"
    private String entityType;
    private Long entityId;

    def productPageSettings = { Map attrs, body ->
        out << body()
        out << g.include(view: "/plugins/product_custom_information/productPageSettings.gsp", model: [informations: productCustomInformationService.getProductCustomFields()])
    }

    def productAdvancedInformation = { Map attrs, body ->
        out << body()
        if ((attrs.entityType).equals("product")) {
            this.entityId = pageScope.product.id as Long;
            out << g.include(view: "/plugins/product_custom_information/productAdvancedInformation.gsp", model: [informations: productCustomInformationService.getProductCustomFields(), values: productCustomInformationService.getProductCustomFieldValueList(attrs.entityType as String, this.entityId), entityType: attrs.entityType]);
        } else if ((attrs.entityType).equals("variation")) {
            this.entityId = pageScope.variationId as Long;

            List<ProductCustomField> productCustomFieldList = productCustomInformationService.getProductCustomFields();
            List<ProductCustomFieldValue> productCustomFieldValueList = productCustomInformationService.getProductCustomFieldValueList(attrs.entityType as String, this.entityId);
            List<ProductCustomFieldValue> newProductCustomFieldValueList = new ArrayList<>();

            for (int i = 0; i < productCustomFieldList.size(); i++) {
                boolean flag = false;
                for (int j = 0; j < productCustomFieldValueList.size(); j++) {
                    if (productCustomFieldList[i].id == productCustomFieldValueList[j].fieldId) {
                        newProductCustomFieldValueList.add(productCustomFieldValueList[j]);
                        flag = true;
                    }
                }
                if (!flag) {
                    newProductCustomFieldValueList.add(ProductCustomFieldValue.findByFieldAndEntityTypeAndEntityId(productCustomFieldList[i], "product", pageScope.product.id as Long));
                }
            }
            out << g.include(view: "/plugins/product_custom_information/productAdvancedInformation.gsp", model: [informations: productCustomFieldList, values: newProductCustomFieldValueList, entityType: attrs.entityType]);
        }
    }

    def productTabInformationHeader = { Map attrs, body ->
        out << body()
        List<ProductCustomField> fields = productCustomInformationService.getProductCustomFields()
        pageScope.productInformationFields = fields
        for (ProductCustomField field : fields) {
            out << "<div class='bmui-tab-header' data-tabify-tab-id='custom-information-${field.id}'>"
            out << '<span class="title">'
            out << site.message(code: field.title)
            out << '</span>'
            out << '</div>'
        }
    }

    def productTabInformationBody = { Map attrs, body ->
        out << body()
        ProductData productData = pageScope.productData
        String entityType = "product"
        Long entityId = productData.id
        List<ProductCustomField> fields = pageScope.productInformationFields;
        Map<Long, ProductCustomFieldValue> fieldValueMapping = [:]
        if(productData.attrs.selectedVariation && productData.variationModel == "enterprise") {
            entityType = "variation"
            entityId = productData.attrs.selectedVariation
        }
        List<ProductCustomFieldValue> fieldValues = productCustomInformationService.getProductCustomFieldValueList(entityType, entityId)
        for (ProductCustomFieldValue value : fieldValues) {
            fieldValueMapping[value.field.id] = value
        }
        if(entityType == "variation" && fieldValues.size() != fields.size()) {
            fieldValues = productCustomInformationService.getProductCustomFieldValueList("product", productData.id)
            for (ProductCustomFieldValue value : fieldValues) {
                fieldValueMapping[value.field.id] = fieldValueMapping[value.field.id] ?: value
            }
        }
        for (ProductCustomField field : fields) {
            ProductCustomFieldValue value = fieldValueMapping[field.id]
            out << "<div id='bmui-tab-custom-information-${field.id}' class='bmui-tab-panel' role='tabpanel' aria-expanded='true' aria-hidden='false'>"
            out << value?.value
            out << '</div>'
        }
    }
}
