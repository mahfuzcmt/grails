package com.webcommander.admin

import com.webcommander.common.CommonService
import com.webcommander.events.AppEventManager
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.TrashUtil
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

@Transactional
class ZoneService {
    CommonService commonService

    private Closure getFilterClosure(Map params) {
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%");
            }
            if(params.country) {
                countries {
                    like("name", "%${params.country.trim().encodeAsLikeText()}%")
                }
            }
            if(params.state) {
                states {
                    or {
                        like("name", "%${params.state.trim().encodeAsLikeText()}%")
                    }
                }
            }
            if(params.excludeIds) {
                not {
                    inList ("id", params.excludeIds)
                }
            }
            if (params.ids) {
                inList("id", params.list("ids").collect { it.toLong() })
            }
            if(params["post-code"]) {
                //TODO change the way of search
                sqlRestriction "exists (select * from zone_post_codes where post_codes_string = '${params["post-code"]}' and this_.id = zone_id)"
            }

            if (params.isDefault != null) {
                or {
                    eq("isDefault", params.isDefault)
                    eq("name", "REST_OF_THE_WORLD")
                }
            }
        }
    }

    Integer getZoneCount(GrailsParameterMap params) {
        return Zone.createCriteria().count(getFilterClosure(params))
    }

    List<Zone> getZones(Map params) {
        def listMap = [max: params.max, offset: params.offset];
        return Zone.createCriteria().list(listMap) {
            and getFilterClosure(params)
            order params.sort ?: "name", params.dir ?: "asc"
        }
    }

    def saveZone(def zoneParam, Long id) {
        Zone zone
        if(id) {
            zone = Zone.get(id);
            zone.countries.clear();
            zone.states.clear();
            zone.postCodes.clear();
        } else {
            zone = new Zone();
        }
        zone.name = zoneParam.name;
        zoneParam.list("country.id").each {
            zone.countries.add(Country.get(it.toLong()))
        }
        zoneParam.list("state.id").each {
            zone.states.add(State.get(it.toLong()));
        }
        if(zone.states.size() < 2 && zone.countries.size() < 2) {
            zoneParam.list("postcode").each {
                zone.postCodes.add(it);
            }
        }
        zone.save()
        if(zone.hasErrors()) {
            throw new ApplicationRuntimeException("zone.save.failure");
        }
        if(id) {
            AppEventManager.fire("zone-update", [id])
        }
        return zone;
    }

    void renameZone(Zone zone) {
        zone.name = commonService.getCopyNameForDomain(zone)
        zone.save()
    }

    Boolean delete(Long id, String at1, String at2) {
        TrashUtil.preProcessFinalDelete("zone", id, at2 != null, at1 != null);
        AppEventManager.fire("before-zone-delete", [id])
        Zone zone = Zone.get(id);
        zone.delete();
        AppEventManager.fire("zone-delete", [id])
        return !zone.hasErrors();
    }

    Boolean deleteSelected(List<Long> ids) {
        List<Zone> zones = []
        try {
            ids.each {
                Zone zone = Zone.get(it)
                if(zone.isSystemGenerated)  throw new Exception("System generated zone")
                zones.add(zone)
            }
            zones.each {
                it.delete()
            }
            return true
        } catch (Exception ex) {
            return false
        }
    }
}
