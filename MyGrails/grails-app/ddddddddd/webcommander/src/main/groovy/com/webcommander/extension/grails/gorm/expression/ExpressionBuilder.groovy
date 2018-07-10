package com.webcommander.extension.grails.gorm.expression

import org.grails.datastore.mapping.query.api.QueryableCriteria

/**
 * Created by zobair on 07/12/2014.*/
class ExpressionBuilder {
    Expression expression

    void property(String value) {
        expression = new PropertyExpression(value)
    }

    void "case"(Closure value) {
        expression = new CaseExpression(value)
    }

    void sub(QueryableCriteria criteria) {
        expression = new SubExpression(criteria)
    }
}