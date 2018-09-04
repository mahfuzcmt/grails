package com.webcommander.hibernate

import com.webcommander.extension.grails.gorm.expression.SubExpression
import grails.gorm.DetachedCriteria

class OrderSubqueryExpression extends ExpressionOrder {
    OrderSubqueryExpression(DetachedCriteria sub, boolean ascending = true) {
        super(ascending)
        expression = new SubExpression(sub)
    }
}