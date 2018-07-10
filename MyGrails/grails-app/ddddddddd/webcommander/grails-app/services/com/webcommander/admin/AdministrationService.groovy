package com.webcommander.admin

import com.webcommander.Page
import com.webcommander.annotations.Initializable
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.util.AppUtil
import grails.gorm.transactions.Transactional
import org.hibernate.sql.JoinType

@Initializable
@Transactional
class AdministrationService {

    static void initialize() {
        HookManager.register "page-put-trash-veto-count", { response, id ->
            def termsType = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE, "terms_and_condition_type");
            if(termsType == 'page') {
                def termsRef = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE, "terms_and_condition_ref");
                if(id == termsRef.toLong()) {
                    String type = 'terms.condition.page'
                    if(response.as) {
                        response.as << type
                    } else {
                        response.as = [type]
                    }
                }
            }

            if (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "landing_page") == Page.createCriteria().get { projections {property "url"}; eq "id", id}) {
                String entity = "landing.page"
                if(response.as) {
                    response.as << entity
                } else {
                    response.as = [entity]
                }
            }
            return response
        }

        HookManager.register("layout-delete-veto") { response, id ->
            if (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "default_layout").toLong(0) == id ) {
                String entity = "default.layout"
                if(response.as) {
                    response.as << entity
                } else {
                    response.as = [entity]
                }
            }
            return response
        }

        HookManager.register "page-put-trash-at2-count", { response, id ->
            def checkPages = [page404: "404.page", page403: "403.page"]
            String url = Page.createCriteria().get { projections {property "url"}; eq "id", id}
            checkPages.each {
                if (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, it.key) == url) {
                    if(response.as) {
                        response.as << it.value
                    } else {
                        response.as = [it.value]
                    }
                }
            }
            return response
        }

        AppEventManager.on("plugin-installed", { identifier ->
            AppUtil.clearConfig();
        })

        AppEventManager.on("before-page-put-in-trash", { id ->
            def checkPages = ["page404", "page403"]
            String url = Page.createCriteria().get { projections {property "url"}; eq "id", id}
            checkPages.each {
                if (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, it) == url) {
                    SiteConfig config = SiteConfig.findByTypeAndConfigKey(DomainConstants.SITE_CONFIG_TYPES.GENERAL, it);
                    config.value = INITIAL_DATA[DomainConstants.SITE_CONFIG_TYPES.GENERAL][it]
                    config.merge()
                    AppUtil.clearConfig()
                }
            }
            def eCommerceConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE)
            if(eCommerceConfig.continue_shopping_target == "specified" && eCommerceConfig.target_page == url) {
                SiteConfig config = SiteConfig.findByTypeAndConfigKey(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "continue_shopping_target")
                config.value = "previous"
                config.merge()
                AppUtil.clearConfig()
            }
        })
    }

    public List<State> getStatesForCountry(long id) {
        return State.createCriteria().list {
            eq("country.id", id)
            eq("isActive", true)
        };
    }

    public boolean isCityExistsForCountry(long id){
        City.createCriteria().get {
            projections {
                rowCount()
            }
            state {
                eq("country.id", id)
            }
        } > 0;
    }

    List<City> getCities(Map params) {
        return City.createCriteria().list {
            if(params.countryId) {
                createAlias("state", "s", JoinType.LEFT_OUTER_JOIN)
                eq("s.country.id", params.countryId.toLong())
            }
            if(params.postCode) {
                eq("postCode", params.postCode)
            }
            if(params.stateId) {
                eq("state.id", params.stateId.toLong(0))
            }
        };
    }

    public def getMatchedCities(Long stateId, String postCode){
        return City.createCriteria().list {
            eq("postCode", postCode)
            eq("state.id", stateId)
        };
    }

    public def getAllCountry(){
        return Country.createCriteria().list {
            eq("isActive", true)
        }
    }
}
