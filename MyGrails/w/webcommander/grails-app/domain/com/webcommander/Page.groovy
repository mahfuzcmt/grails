package com.webcommander

import com.webcommander.admin.Operator
import com.webcommander.common.MetaTag
import com.webcommander.config.StoreDetail
import com.webcommander.constants.DomainConstants
import com.webcommander.design.DockSection
import com.webcommander.design.Layout
import com.webcommander.admin.Customer
import com.webcommander.admin.CustomerGroup
import com.webcommander.events.AppEventManager
import com.webcommander.widget.Widget

class Page {

    Long id
    String name
    String title
    String url
    String visibility = DomainConstants.PAGE_VISIBILITY.OPEN;
    String visibleTo
    String body
    String js
    String css

    Boolean isActive = true
    Boolean isInTrash = false
    Boolean isDisposable = false
    Boolean disableGooglePageTracking = false

    Date created
    Date updated

    Operator createdBy
    Layout layout
    StoreDetail store

    Collection<MetaTag> metaTags = []
    Collection<Customer> customers = []
    Collection<CustomerGroup> customerGroups = []
    Collection<Widget> headerWidgets = []
    Collection<Widget> footerWidgets = []
    Collection<DockSection> dockableSections = []

    static hasMany = [metaTags: MetaTag, headerWidgets: Widget, footerWidgets: Widget, dockableSections: DockSection, customers: Customer, customerGroups: CustomerGroup]

    static constraints = {
        name(unique: true, size: 2..100)
        title(size: 2..255)
        url(unique: true, size: 2..50)
        createdBy(nullable: true)
        visibleTo(nullable: true)
        body(nullable: true)
        layout(nullable: true)
        store(nullable: true)
        css(nullable: true)
        js(nullable: true)
    }

    static mapping = {
        css type: "text"
        body type: "text"
        name length: 100
        js length: 10000
        url length: 50
        metaTags cache: true
        customers cache: true
        customerGroups cache: true
        headerWidgets cache: true
        footerWidgets cache: true
        dockableSections cache: true
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
        if (!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    @Override
    int hashCode() {
        if (this.id) {
            return ("Page: " + this.id).hashCode();
        }
        if (this.url) {
            return ("Page: " + this.url).hashCode();
        }
        return super.hashCode()
    }

    @Override
    boolean equals(Object obj) {
        if (!obj instanceof Page) {
            return false;
        }
        if (this.id && obj.id) {
            return id == obj.id
        }
        if (this.url && obj.url) {
            return url == obj.url
        }
        return super.equals(obj)
    }

    public static void initialize() {
        def _init = {
            def insertSqls = [["Home", "Home", "home"]]
            if(Page.count() == 0) {
                insertSqls.each {
                    new Page(name: it[0], title: it[1], url: it[2], layout: Layout.first()).save()
                }
            }
        }
        if(Layout.count()) {
            _init()
        } else {
            AppEventManager.one("layout-bootstrap-init", "bootstrap-init", _init)
        }
    }
}
