package com.webcommander.plugin.event_management

class VenueLocationImage {

    Long id
    String name

    static belongsTo = [venueLocation: VenueLocation]

    static mapping = {
        autoImport false
    }

    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof Event)) {
            return false;
        }
        if (this.id) {
            return id == obj.id
        }
        return super.equals(obj)
    }
}
