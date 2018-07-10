package com.webcommander.admin

import com.webcommander.events.AppEventManager
import com.webcommander.util.AppUtil
import grails.converters.JSON
import grails.util.Holders

class Zone {

    Long id
    String name
    Boolean isSystemGenerated = false

    Date created
    Date updated

    Boolean isDefault = false

    Collection<Country> countries = []
    Collection<State> states = []
    Collection<String> postCodes = []

    static hasMany = [countries: Country, states: State, postCodes: String]

    static constraints = {
        name(maxSize: 100, unique: true);
    }

    static mapping = {
        postCodes joinTable:[name: "zone_post_codes", key: "zone_id", column: "post_codes_string", type: "varchar(5)"]
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

    @Override
    int hashCode() {
        if (id) {
            return ("Zone: " + id).hashCode()
        }
        return super.hashCode();
    }

    @Override
    boolean equals(Object o) {
        if (!o instanceof Zone) {
            return false
        }
        if (id && o.id) {
            return id == o.id
        }
        return super.equals(o);
    }

    static void initialize() {
        def _init = {
            if(Zone.countByIsDefault(true) == 0) {
                ZoneService zoneService  = AppUtil.getBean(ZoneService)
                String fileLocation = Holders.servletContext.getRealPath("WEB-INF/dbEntries/zone.json")
                List zones = JSON.parse new File(fileLocation).text
                for (Map data : zones) {
                    Zone _zone = Zone.findByName(data.name)
                    if(_zone) {
                        zoneService.renameZone(_zone)
                        _zone.save(flush: true)
                    }
                    Zone zone = new Zone(name: data.name, isSystemGenerated: true, isDefault: true)

                    if (data.isDefault) {
                        zone.isDefault = data.isDefault
                    }

                    data.countries.each {
                        Country country = Country.findByCode(it)
                        if(country) {
                            zone.addToCountries(country)
                        }
                    }
                    data.states.each {
                        State state = State.findByCode(it)
                        if(state) {
                            zone.addToStates(state)
                        }
                    }
                    if(data.states.size() == zone.states.size() && data.countries.size() == zone.countries.size()) {
                        zone.save()
                    }
                }
                AppEventManager.fire("zone-bootstrap-init")
            }
        }
        if(State.count()) {
            _init()
        } else {
            AppEventManager.one("state-bootstrap-init", "bootstrap-init", _init)
        }
    }

}
