package com.webcommander.plugin.filter.resolver

import com.webcommander.plugin.filter.FilterProfile
import com.webcommander.webcommerce.Category

/**
 * Created by sharif ul islam on 25/02/2018.
 */
class FilterProfileResolver extends Resolver {

    @Override
    Object doResolve(Map context) {

        Long categoryId = context.categoryId ?: null
        if (categoryId) {
            Category category = Category.load(categoryId)
            FilterProfile profile = FilterProfile.createCriteria().get {
                categories {
                    eq("id", category?.id)
                }
            }

            if (profile) {
                return profile
            }

            if (!profile && category.parentId) {
                context.categoryId = category.parentId
                return doResolve(context)
            }
        }

        // if not found any filter profile then try to find out default profile
        List<FilterProfile> filterProfiles = FilterProfile.createCriteria().list() {
            eq('isDefault', true)
        }
        if (filterProfiles && filterProfiles.size() > 0) {
            return filterProfiles.get(0)
        }

        return null
    }

}
