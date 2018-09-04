package com.webcommander.plugin.filter

import com.webcommander.events.AppEventManager
import com.webcommander.webcommerce.Category

class FilterProfile {

    Long id
    String name
    String description
    boolean isDefault = false

    Collection<Filter> filters = []
    Collection<FilterGroup> filterGroups = []
    Collection<Category> categories = []

    static hasMany = [
        filters: Filter,
        categories: Category,
        filterGroups: FilterGroup,
    ]
    static constraints = {
        name(unique: true, size: 2..100)
        description(nullable: true, maxSize: 500)
        categories(nullable: true)
        filters(nullable: true)
        filterGroups(nullable: true)
    }

    static mapping = {
        filters cache: true
    }

    static transients = ['getFilters']

    @Override
    int hashCode() {
        if (id) {
            return ("filterProfile: " + id).hashCode();
        }
        return super.hashCode();
    }

    public static void initialize() {
        AppEventManager.on("before-category-delete", { id->
            Category category = Category.get(id);
            FilterProfile.createCriteria().list {
                categories {
                    eq("id", id)
                }
            }.each { FilterProfile profile ->
                profile.removeFromCategories(category)
                profile.save()
            }
        })
    }

}