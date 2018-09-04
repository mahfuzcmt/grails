package com.webcommander.plugin.event_management.webmarketing

import com.webcommander.admin.RoleService
import com.webcommander.annotations.Initializable
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.plugin.event_management.Venue
import com.webcommander.plugin.event_management.VenueLocation
import com.webcommander.plugin.event_management.VenueLocationInvitation
import com.webcommander.plugin.event_management.VenueLocationSection
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import com.webcommander.models.RestrictionPolicy
import com.webcommander.util.TrashUtil
import grails.web.servlet.mvc.GrailsParameterMap
import org.hibernate.SessionFactory

@Initializable
class VenueService {

    SessionFactory sessionFactory
    RoleService roleService

    static void initialize() {
        HookManager.register("venue-delete-veto") { response, id ->
            List locations = VenueLocation.createCriteria().list {
                eq("venue.id", id)
            }.id
            if(locations.size()) {
                locations.each { locationId ->
                    Map vetos = HookManager.hook("venue-location-delete-veto", [:], id)
                    response.putAll(vetos)
                }
            }
            return response
        }

        AppEventManager.on("before-venue-delete", { id ->
            VenueLocation.createCriteria().list {
                eq("venue.id", id)
            }.each {
                TrashUtil.preProcessFinalDelete("venue-location", id, true, true)
                AppEventManager.fire("before-venue-location-delete", [it.id, "include"])
                it.delete()
                AppEventManager.fire("venue-location-delete", [it.id])
            }
        })

        HookManager.register("venue-location-delete-veto") { response, id ->
            int invitationCount = VenueLocationInvitation.createCriteria().count(){
                eq("location.id", id)
                ne("status", "rejected")
            }
            if(invitationCount) {
                response.invitations = invitationCount
            }
            return response
        }
        HookManager.register("venueLocation-delete-veto-list") { response, id ->
            List<VenueLocationInvitation> invitationList = VenueLocation.get(id).venueLocationInvitation
            if(invitationList) {
                int approved = 0;
                int pending = 0;
                int rejected = 0;
                for(int i = 0; i < invitationList.size(); i++) {
                    if(invitationList[i].status == "approved") {
                        approved += 1;
                    }
                    else if(invitationList[i].status == "rejected") {
                        rejected += 1;
                    }
                    else {
                        pending += 1;
                    }
                }
                response."invitations" = ["Total ${invitationList.size() > 1?'Invitations:':'Invitation:'} ${invitationList.size()}\n Approved: ${approved}\n Rejected: ${rejected}\n Pending: ${pending}"]
            }
            return response
        }
        AppEventManager.on("before-venue-location-delete", {id ->
            VenueLocationInvitation.createCriteria().list{
                eq("location.id", id)
                eq("status", "rejected")
            }.each {
                AppEventManager.fire("before-venue-location-invitation-delete", [it.id])
                it.delete()
                AppEventManager.fire("venue-location-invitation-delete", [it.id])
            }
        })
        AppEventManager.on("before-event-delete", {id ->
            VenueLocationInvitation.createCriteria().list {
                eq("event.id", id)
            }.each {
                AppEventManager.fire("before-venue-location-invitation-delete", [it.id])
                it.delete()
                AppEventManager.fire("venue-location-invitation-delete", [it.id])
            }
        })
        AppEventManager.on("before-event-session-delete", {id ->
            VenueLocationInvitation.createCriteria().list {
                eq("eventSession.id", id)
            }.each {
                AppEventManager.fire("before-venue-location-invitation-delete", [it.id])
                it.delete()
                AppEventManager.fire("venue-location-invitation-delete", [it.id])
            }
        })

        HookManager.register("venue-location-delete-veto") { response, id ->
            List sectionIds = VenueLocationSection.createCriteria().list{
                eq("venueLocation.id", id)
            }.id
            int sectionCount = sectionIds.size()
            if(sectionCount) {
                sectionIds.each {sectionId->
                    try {
                        TrashUtil.preProcessFinalDelete("venue-location-section", sectionId, true, true)
                    } catch (AttachmentExistanceException att) {
                        if(response.has) {
                            att.attachmentInfo["at3"].has.tickets.count += response.has.tickets.count
                        }
                        response.putAll(att.attachmentInfo["at3"])
                    }
                }
                response.sections = sectionCount
            }
            return response
        }
        HookManager.register("venueLocation-delete-veto-list") { response, id ->
            VenueLocation location = VenueLocation.get(id)
            if(location.sections) {
                response."sections" = location.sections.collect {it.name.encodeAsBMHTML()}
            }
            return response
        }

        AppEventManager.on("before-venue-location-delete", { id ->
            VenueLocationSection.createCriteria().list {
                eq("venueLocation.id", id)
            }.each {
                AppEventManager.fire("before-venue-location-section-delete", [it.id])
                it.delete()
                AppEventManager.fire("venue-location-section-delete", [it.id])
            }
        })
    }

    private def getCriteriaClosure(Map params) {
        def session = AppUtil.session
        Long admin = session.admin
        return {
            if (params.name) {
                ilike("name", "%${params.name.trim().encodeAsLikeText()}%")
            }
        }
    }

    private Closure getLocationCriteriaClosure(Map params) {
        sessionFactory.cache.evictCollectionRegions()
        def session = AppUtil.session
        Long admin = session.admin
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%");
            }
            if (params.name) {
                ilike("name", "%${params.name.trim().encodeAsLikeText()}%");
            }
            if(params.venueId) {
                eq("venue.id", params.venueId)
            }
            if(params.ids) {
                inList("id", params.list("ids").collect {it.toLong()})
            }
            if(!roleService.isPermitted(admin, new RestrictionPolicy(type: "event_location", permission: "view.others"), params)) {
                eq("organiser.id", admin)
            }
            if(params.hasEvent || params.hasEventSession) {
                or {
                    sqlRestriction("this_.id in (select l.id from venue_location l inner join event e where l.id = e.venue_location_id)")
                    sqlRestriction("this_.id in (select l.id from venue_location l inner join event_session s where l.id = s.venue_location_id)")
                }
            }
            if(params.locationIds != null) {
                if(params.locationIds.size() == 0) {
                    inList("id", [0L])
                } else {
                    inList("id", params.locationIds)
                }
            }
        }
    }

    public def getVenues(GrailsParameterMap params) {
        Map listMap = [max: params.max, offset: params.offset]
        return Venue.createCriteria().list(listMap) {
            and getCriteriaClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }

    public Integer getVenuesCount(GrailsParameterMap params) {
        return Venue.createCriteria().count {
            and getCriteriaClosure(params)
        }
    }

    public Long getVenueLocationsCount(Map params) {
        return VenueLocation.createCriteria().count {
            and getLocationCriteriaClosure(params)
        }
    }

    public Collection<VenueLocation> getVenueLocations(Map params) {
        Map listMap = [max: params.max, offset: params.offset]
        return VenueLocation.createCriteria().list(listMap) {
            and getLocationCriteriaClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }

    public Collection<VenueLocation> getVenueLocationsForWidgetContent(List<Long> contentIds) {
        Collection<VenueLocation> venueLocations = VenueLocation.createCriteria().list {
            and getLocationCriteriaClosure([locationIds: contentIds])
        }
        return venueLocations
    }

}
