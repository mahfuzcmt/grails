package com.webcommander.admin

class CustomerGroup {
    Long id;
    String name;
    String description;
    String status = 'A';
    Date created
    Date updated
    String defaultTaxCode

    Collection<Customer> customers = []

    static hasMany = [customers: Customer]

    static mapping = {
        customers cascade: false
        description type: "text"
    }

    static constraints = {
        name(blank: false, size: 4..50, unique: true)
        description(nullable: true)
        status(maxSize: 1)
        defaultTaxCode(nullable: true)
    }

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

    public static void initialize() {
        def insertSql = [
            ['WHOLESALE', 'Whole sale'],
            ['RETAIL', 'Retail'],
            ['PARTNER', 'Business partner or reseller'],
            ['MEMBER', 'Member group']
        ]
        if (CustomerGroup.count() < 1 && insertSql) {
            insertSql.each {
                new CustomerGroup(name: it[0], description: it[1]).save();
            }
        }
    }

}
