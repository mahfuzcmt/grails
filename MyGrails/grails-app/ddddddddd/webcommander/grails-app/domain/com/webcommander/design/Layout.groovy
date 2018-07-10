package com.webcommander.design

import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.widget.Widget

class Layout {

    Long id
    String name
    String body = "<div class='widget-container'><wi:widget type=\"page\"></wi:widget></div>"
    String js
    String css = ".body > .body-section > .widget-container {width: 1000px;} .body > .widget-container {width: 1000px;} .header > .widget-container {width: 1000px; height: 200px;} .footer > .widget-container {width: 1000px; height: 200px;}"

    Boolean isDisposable = false

    Date created
    Date updated

    Collection<Widget> headerWidgets = []
    Collection<Widget> footerWidgets = []
    Collection<DockSection> dockableSections = []

    static hasMany = [headerWidgets: Widget, footerWidgets: Widget, dockableSections: DockSection]

    static constraints = {
        name unique: true, maxSize: 100
        js nullable: true
        css nullable: true
        headerWidgets nullable: true
        footerWidgets nullable: true
    }

    static mapping = {
        name length: 100
        css type: "text"
        js length: 10000
        body type: "text"
        headerWidgets cache: true
        footerWidgets cache: true
        dockableSections cache: true
    }

    static transients = ['attachedPageForLayout']

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
        if (!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }

    boolean equals(Object obj) {
        if (!obj instanceof Layout) {
            return false;
        }
        if (this.id && obj.id) {
            return id == obj.id
        }
        return super.equals(obj)
    }

    @Override
    int hashCode() {
        if (this.id) {
            return ("layout: " + this.id).hashCode();
        }
        return super.hashCode()
    }

    public static void initialize() {
        def insertSqls = [
            ["Webcommander"]
        ]
        if (Layout.count() == 0) {
            insertSqls.each {
                new Layout(name: it[0]).save()
            }
            AppEventManager.fire("layout-bootstrap-init")
        }
    }

    Map attachedPageForLayout(Integer max=0) {
        Map attached = [pages:[], autoPages: []]
        def page = HookManager.hook("layout-delete-veto-list", [:], id);
        if(page.pages) {
            page.pages.eachWithIndex { name, i ->
                if(i < max) {
                    attached.pages.add(name)
                }
            }
        }
        int pagesCount = attached.pages.size()
        if(page['auto.pages'] && pagesCount < max) {
            page['auto.pages'].eachWithIndex { name, i ->
                if(i < max - pagesCount) {
                    attached.autoPages.add(name)
                }
            }
        }
        return attached
    }
}
