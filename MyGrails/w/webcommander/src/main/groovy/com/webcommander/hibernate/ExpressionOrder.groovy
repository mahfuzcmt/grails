package com.webcommander.hibernate

import com.webcommander.extension.grails.gorm.expression.BlankHibernateCriterionToAttachValue
import com.webcommander.extension.grails.gorm.expression.Expression
import com.webcommander.extension.grails.gorm.expression.ExpressionHibernateCriterion
import org.hibernate.Criteria
import org.hibernate.criterion.CriteriaQuery
import org.hibernate.criterion.Order
import org.hibernate.engine.spi.TypedValue
import org.hibernate.internal.CriteriaImpl

/**
 * Created by zobair on 08/12/2014.
 */
class ExpressionOrder extends Order {

    protected Expression expression

    ExpressionOrder(boolean ascending) {
        super(null, ascending)
    }

    @Override
    String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) {
        String sql = expression.toSqlString(criteria, criteriaQuery) + " " + (ascending ? "asc" : "desc")
        List<TypedValue> values = expression.getTypedValues(criteria, criteriaQuery)

        if(values.size()) {
            List<CriteriaImpl.CriterionEntry> parentEntries = criteria.criterionEntries
            if(parentEntries.size() > 0) {
                CriteriaImpl.CriterionEntry lastEntry = parentEntries.last()
                parentEntries[parentEntries.size() - 1] = new CriteriaImpl.CriterionEntry(new ExpressionHibernateCriterion(lastEntry.criterion, values), lastEntry.criteria)
            } else {
                parentEntries.add(new CriteriaImpl.CriterionEntry(new BlankHibernateCriterionToAttachValue(values), criteria))
            }
        }

        return sql
    }
}
