package com.webcommander.plugin.product_custom_information

import com.webcommander.common.CommonService
import com.webcommander.throwables.ApplicationRuntimeException
import grails.gorm.transactions.Transactional

class ProductCustomInformationService {
    CommonService commonService

    List<ProductCustomField> getProductCustomFields() {
        return ProductCustomField.list();
    }

    @Transactional
    Map save(Map params) {
        ProductCustomField customField;
        if (params.id) {
            customField = ProductCustomField.get(params.id as Long);
        } else {
            customField = new ProductCustomField();
        }
        customField.title = params.title;
        if (!commonService.isUnique(customField, "title")) {
            throw new ApplicationRuntimeException("custom.information.exist.error", "alert")
        }
        customField.type = params.type;
        customField.save();
        if(customField.hasErrors()) {
            return null
        }
        return [
            id   : customField.id,
            title: customField.title.encodeAsBMHTML(),
            type : customField.type
        ];
    }

    @Transactional
    boolean delete(Long id) {
        try {
            ProductCustomField productCustomField = ProductCustomField.get(id);
            List<ProductCustomFieldValue> productCustomFieldValueList = ProductCustomFieldValue.findAllByField(productCustomField);
            if (productCustomFieldValueList != null && productCustomFieldValueList.size() > 0) {
                productCustomFieldValueList.each {
                    it.delete();
                }
            }
            productCustomField.delete();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Transactional
    Map saveCustomInformationValue(Map response, Map parameters) {
        String target = parameters.target ?: "product"
        parameters.customInformation.each { key, value ->
            Long fieldId = key as Long, entityId = (target == "variation" ? parameters.variationId : parameters.id) as Long
            ProductCustomField productCustomField = ProductCustomField.proxy(fieldId);
            ProductCustomFieldValue fieldValue = ProductCustomFieldValue.findByFieldAndEntityTypeAndEntityId(productCustomField, target, entityId);
            if (!fieldValue) {
                fieldValue = new ProductCustomFieldValue();
                fieldValue.value = value;
                fieldValue.field = productCustomField;
                fieldValue.entityType = target
                fieldValue.entityId = entityId
            } else {
                fieldValue.value = value;
            }
            fieldValue.save();
        }
        if(target == "variation") {
            removeNonExistVariationCustomInformation(parameters)
        }
        return response
    }

    private void removeNonExistVariationCustomInformation(Map params) {
        List<ProductCustomFieldValue> fieldValues = getProductCustomFieldValueList("variation", params.variationId as Long);
        if(params.customInformation == null) {
            fieldValues*.delete()
        } else {
            for(ProductCustomFieldValue value : fieldValues) {
                if(!params.customInformation.containsKey(value.fieldId.toString())) {
                    value.delete()
                }
            }
        }
    }


    List<ProductCustomFieldValue> getProductCustomFieldValueList() {
        return ProductCustomFieldValue.list();
    }

    List<ProductCustomFieldValue> getProductCustomFieldValueList(String entityType) {
        return ProductCustomFieldValue.findAllByEntityType(entityType);
    }

    List<ProductCustomFieldValue> getProductCustomFieldValueList(String entityType, Long entityId) {
        return ProductCustomFieldValue.findAllByEntityTypeAndEntityId(entityType, entityId);
    }
}
