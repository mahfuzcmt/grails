package com.webcommander.plugin.order_custom_fields

import grails.transaction.NotTransactional
import grails.gorm.transactions.Transactional
import grails.web.databinding.DataBindingUtils

@Transactional
class OrderCustomFieldsService {

    boolean saveFields(Map params) {
        OrderCheckoutFields field;
        if(params.id) {
            field = OrderCheckoutFields.get(params.id)
            field.options.clear()
            field.setValidation(null)
        } else {
            field = new OrderCheckoutFields()
        }
        DataBindingUtils.bindObjectToInstance(field, params, null, ["id", "options"], null)
        params.list("options").each { option ->
            if(option) { //preventing addition of empty string
                field.options.add(option)
            }
        }
        field.save()
        return !field.hasErrors()
    }

    private Closure getCriteriaClosure(Map params) {
        return {
            if (params.searchText) {
                ilike("label", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
        }
    }

    Integer getOrderCheckoutFieldsCount(Map params) {
        return OrderCheckoutFields.createCriteria().count {
            and getCriteriaClosure(params)
        }
    }

    List<OrderCheckoutFields> getOrderCheckoutFields(Map params) {
        def listMap = [max: params.max, offset: params.offset];
        return OrderCheckoutFields.createCriteria().list(listMap) {
            and getCriteriaClosure(params)
            order(params.sort ?: "label", params.dir ?: "asc")
        }
    }

    boolean saveFieldsGroupTitle(Map params) {
        OrderCheckoutFieldsTitle title = OrderCheckoutFieldsTitle.first();
        if(title) {
            title.title = params.title
        } else {
            title = new OrderCheckoutFieldsTitle(title: params.title)
        }
        title.save()
        return !title.hasErrors()
    }

    boolean deleteFields(Long id) {
        OrderCheckoutFields field = OrderCheckoutFields.get(id)
        field.delete()
        return true
    }

    @NotTransactional
    boolean deleteSelectedFields(List<Long> ids) {
        ids.each {
            deleteFields(it)
        }
        return true
    }

}
