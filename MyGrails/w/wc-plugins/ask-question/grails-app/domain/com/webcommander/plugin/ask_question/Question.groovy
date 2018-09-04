package com.webcommander.plugin.ask_question

import com.webcommander.webcommerce.Product

class Question {
    Long id
    String question
    String name
    String email
    String answer
    boolean status = false

    Date created
    Date updated

    static belongsTo = [product: Product]

    static constraints = {
        name(maxSize: 200)
        answer(nullable: true)
        question(blank: false)
        email(blank: false, email: true)
    }

    static mapping = {
        answer type: "text"
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
}
