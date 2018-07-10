package com.webcommander.plugin.simplified_event_management

import com.webcommander.models.blueprints.AbstractStaticResource

class SimplifiedEventImage extends AbstractStaticResource {
    Long id
    String name
    SimplifiedEvent event
    String baseUrl

    Integer idx

    static belongsTo = [SimplifiedEvent]

    static transients = ['resourceName', 'relativeUrl']

    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof SimplifiedEvent)) {
            return false;
        }
        if (this.id) {
            return id == obj.id
        }
        return super.equals(obj)
    }

    @Override
    String getBaseUrl() {
        return this.baseUrl
    }

    @Override
    String getResourceName() {
        return this.name
    }

    @Override
    void setResourceName(String resourceName) {
        this.name = resourceName
    }

    @Override
    String getRelativeUrl() {
        return "simplified-event/event-${event?.id}/images/"
    }

    @Override
    List<String> getPrefixes() {
        return ["100", "300"]
    }
}
