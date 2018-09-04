package com.webcommander.acl

import com.webcommander.events.AppEventManager

class Permission {
    Long id

    String name
    String label
    String type //type string should not contain '.'

    Boolean applicableOnEntity = true

    static List getTypes() {
        Permission.withCriteria {
            projections {
                distinct "type"
            }
        }.sort()
    }

    static constraints = {
        name unique: "type"
    }

    static initialize() {
        if(!Permission.count()) {
            def insertPermissions = [
                order: [
                    ["view.list", false], ["create", false], ["view", true], ["manage.shipment", true], ["manage.payment", true], ["manage.order", true], ["edit.permission", true]
                ],
                customer: [
                    ["adjust.store.credit", true], ["view.store.credit.history", true], ["view.list", false], ["create", false], ["remove", true], ["edit.properties", true], ["edit.permission", false]
                ],
                navigation: [
                    ["edit", true], ["edit.items", true], ["view.list", false], ["remove", true], ["create", false], ["edit.permission", false]
                ],
                section: [
                    ["edit", true], ["remove", true], ["create", false], ["view.list", false], ["edit.permission", false]
                ],
                article: [
                    ["edit", true], ["remove", true], ["create", false], ["view.list", false], ["edit.permission", false]
                ],
                page: [
                    ["edit.properties", true], ["edit.content", true], ["create", false], ["view.list", false], ["remove", true], ["edit.permission", false]
                ],
                asset_library: [
                    ["create.directory", false], ["upload.file", false], ["remove.file", false], ["remove.directory", false], ["rename.file.directory", false], ["edit.permission", false]
                ],
                operator: [
                    ["view.list", false], ["create", false], ["edit", true], ["remove", true], ["assign.roles", true], ["edit.permission", false]
                ],
                product: [
                    ["edit.properties", true], ["edit.price.stock", true], ["create", false], ["view.list", false], ["remove", true], ["edit.permission", false], ["import.excel", false], ["export.excel", false]
                ],
                category: [
                    ["edit", true], ["create", false], ["view.list", false], ["remove", true], ["edit.permission", false], ["import.excel", false], ["export.excel", false]
                ],
                role: [
                    ["view.list", true], ["edit.permission", false]
                ],
                administration: [
                    ["edit.permission", false]
                ],
                trash: [
                    ["view.list", false], ["restore", false], ["remove", false], ["edit.permission", false]
                ],
                currency: [
                    ["view.list", false]
                ],
                payment_gateway: [
                    ["view.list", false]
                ],
                shipping: [
                    ["view.list", false]
                ],
                tax: [
                    ["view.list", false]
                ],
                newsletter: [
                    ["view.list", false]
                ],
                zone: [
                    ["view.list", false]
                ],
                my_account: [
                    ["view.list", false], ["purchase.package", false], ["edit.info", false]
                ],
                plugin: [
                    ["view.list", false], ["install", false], ["uninstall", false], ["restart.server", false]
                ],
                document: [
                    ["view.list", false], ["create", false], ["edit", true]
                ]
            ]
            insertPermissions.each { type, names ->
                names.each { entry ->
                    new Permission(name: entry[0], label: entry[0], applicableOnEntity: entry[1], type: type).save()
                }
            }
            AppEventManager.fire("permission-bootstrap-init")
        }
    }
    @Override
    boolean equals(Object obj) {
        return obj instanceof Permission ? obj.id == id : false
    }

    @Override
    int hashCode() {
        return id ? ("Permission # " + id).hashCode() : super.hashCode()
    }
}
