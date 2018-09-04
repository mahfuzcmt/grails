package com.webcommander.hibernate

import com.webcommander.extension.grails.gorm.expression.CaseExpression

class CaseExpressionOrder extends ExpressionOrder {
    CaseExpressionOrder(Closure expressionClosure, boolean ascending = true) {
        super(ascending)
        expression = new CaseExpression(expressionClosure)
    }
}