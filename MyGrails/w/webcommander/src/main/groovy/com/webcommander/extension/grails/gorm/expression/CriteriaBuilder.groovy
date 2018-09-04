package com.webcommander.extension.grails.gorm.expression

import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.query.Query
import org.grails.datastore.mapping.query.api.QueryableCriteria
import org.grails.orm.hibernate.query.AbstractHibernateQuery
import org.grails.orm.hibernate.query.HibernateCriterionAdapter
import org.grails.orm.hibernate.query.HibernateProjectionAdapter
import org.grails.orm.hibernate.query.HibernateQuery
import org.hibernate.criterion.Criterion
import org.hibernate.criterion.Projection
import org.hibernate.criterion.ProjectionList
import org.hibernate.criterion.Projections

/**
 * Created by zobair on 04/12/2014.
 */
class CriteriaBuilder {
    static org.hibernate.criterion.DetachedCriteria getHibernateDetachedCriteria(QueryableCriteria<?> queryableCriteria) {
        String alias = queryableCriteria.getAlias()
        PersistentEntity entity = queryableCriteria.getPersistentEntity()
        Class targetClass = entity.getJavaClass()
        org.hibernate.criterion.DetachedCriteria detachedCriteria
        if(alias != null) {
            detachedCriteria = org.hibernate.criterion.DetachedCriteria.forClass(targetClass, alias)
        } else {
            detachedCriteria = org.hibernate.criterion.DetachedCriteria.forClass(targetClass)
        }
        HibernateQuery query = new HibernateQuery(detachedCriteria, entity)
        populateHibernateDetachedCriteria(query, detachedCriteria, queryableCriteria)
        return detachedCriteria
    }

    private static void populateHibernateDetachedCriteria(AbstractHibernateQuery hibernateQuery, org.hibernate.criterion.DetachedCriteria detachedCriteria, QueryableCriteria<?> queryableCriteria) {
        List<Query.Criterion> criteriaList = queryableCriteria.getCriteria()
        for (Query.Criterion criterion : criteriaList) {
            Criterion hibernateCriterion = new HibernateCriterionAdapter().toHibernateCriterion(hibernateQuery, criterion, null)
            if (hibernateCriterion != null) {
                detachedCriteria.add(hibernateCriterion)
            }
        }

        List<Query.Projection> projections = queryableCriteria.getProjections()
        ProjectionList projectionList = Projections.projectionList()
        for (Query.Projection projection : projections) {
            Projection hibernateProjection = new HibernateProjectionAdapter(projection).toHibernateProjection()
            if (hibernateProjection != null) {
                projectionList.add(hibernateProjection)
            }
        }
        detachedCriteria.setProjection(projectionList)
    }
}