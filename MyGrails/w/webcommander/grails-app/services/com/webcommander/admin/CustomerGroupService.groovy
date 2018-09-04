package com.webcommander.admin

import com.webcommander.annotations.Initializable
import com.webcommander.common.CommonService
import com.webcommander.events.AppEventManager
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.TrashUtil
import grails.gorm.transactions.Transactional
import org.hibernate.SessionFactory

@Initializable
class CustomerGroupService {
    CommonService commonService
    SessionFactory sessionFactory


    static void initialize() {
        AppEventManager.on("before-customer-delete", { id ->
            Customer customer = Customer.proxy(id)
            CustomerGroup.createCriteria().list {
                customers {
                    eq("id", id)
                }
            }.each {
                it.customers.remove(customer)
                it.merge()
            }
        })
    }

    private Closure getCriteriaClosure(Map params) {
        def session = AppUtil.session;
        Closure closure = {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.status) {
                eq("status", params.status)
            }
            if (params.createdFrom) {
                Date date = params.createdFrom.dayStart.gmt(session.timezone);
                ge("created", date);
            }
            if (params.createdTo) {
                Date date = params.createdTo.dayEnd.gmt(session.timezone);
                le("created", date);
            }
            if (params.updatedFrom) {
                Date date = params.updatedFrom.dayStart.gmt(session.timezone);
                ge("updated", date);
            }
            if (params.updatedTo) {
                Date date = params.updatedTo.dayEnd.gmt(session.timezone);
                le("updated", date);
            }
            if(params.ids) {
                inList("id", params.list("ids").collect {it.toLong()})
            }
        }
        return closure;
    }

    List<CustomerGroup> getCustomerGroups (List<Long> ids) {
        return ids.size() ? CustomerGroup.createCriteria().list{ inList("id", ids) } : []
    }

    List<CustomerGroup> getCustomerGroups (Map params) {
        Closure closure = getCriteriaClosure(params);
        def listMap = [max: params.max, offset: params.offset];
        return CustomerGroup.createCriteria().list(listMap) {
            and closure;
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }

    Integer getCustomerGroupsCount (Map params) {
        Closure closure = getCriteriaClosure(params);
        return CustomerGroup.createCriteria().get {
            and closure;
            projections {
                rowCount();
            }
        }
    }

    @Transactional
    Boolean save (Map params) {
        Long id = params.id.toLong(0)
        List customers = params.list("customer").collect{Customer.proxy(it.toLong())};
        checkCustomerGroupNameForConflict(params.name, id);
        CustomerGroup customerGroup = id ? CustomerGroup.get(id) : new CustomerGroup();
        customerGroup.name = params.name;
        customerGroup.description = params.description;
        customerGroup.status = params.status;
        customerGroup.defaultTaxCode = params.defaultTaxCode
        customerGroup.customers = customers;
        customerGroup.save();
        sessionFactory.cache.evictCollectionRegions()
        sessionFactory.cache.evictQueryRegions()
        return !customerGroup.hasErrors();
    }

    @Transactional
    def delete(Long id, String at2_reply, String at1_reply) {
        TrashUtil.preProcessFinalDelete("customer-group", id, at2_reply != null, at1_reply != null)
        AppEventManager.fire("before-customer-group-delete", [id])
        CustomerGroup group = CustomerGroup.get(id)
        group.delete()
        AppEventManager.fire("customer-group-delete", [id])
        return !group.hasErrors()
    }

    def deleteSelected(List<String> ids) {
        Integer count = 0;
        ids.each {
            try {
                delete(it, "Yes", "exclude");
                count++;
            } catch (Throwable throwable) {

            }
        }
        return count;
    }

    void checkCustomerGroupNameForConflict(String name, Long id = 0) {
        Integer count = CustomerGroup.createCriteria().count {
            if (id) {
                ne("id", id)
            }
            eq("name", name)
        }
        if (count > 0) {
            throw new ApplicationRuntimeException("customer.group.name.exists.pick.different");
        }
    }

    @Transactional
    def changeStatus(List<Long> ids, String status) {
        Integer count = 0;
        ids.each {
            CustomerGroup customerGroup= CustomerGroup.get(it)
            customerGroup.status = status
            customerGroup.save()
            count++;
        }
        return count;
    }

    @Transactional
    def assignCustomer(params) {
        List<Long> ids = params.list("id")*.toLong();
        List customers = params.list("customer").collect{Customer.proxy(it.toLong())};
        Integer count = 0;
        ids.each {
            CustomerGroup customerGroup = CustomerGroup.get(it)
            customerGroup.customers = customers;
            customerGroup.save();
            count++;
        }
        return count;
    }
}
