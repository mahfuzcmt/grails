package com.webcommander.models

/**
 * Created by zobair on 21/07/2014.
 */
class RestrictionPolicy {
    String type
    String permission
    Closure condition
    String entityParam
    Class domain;
    String ownerField;
}
