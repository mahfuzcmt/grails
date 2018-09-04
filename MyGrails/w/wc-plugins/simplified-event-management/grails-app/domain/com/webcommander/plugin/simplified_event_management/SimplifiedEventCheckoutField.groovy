package com.webcommander.plugin.simplified_event_management

class SimplifiedEventCheckoutField {

    Long id

    String type //single select (radio), single select (drop down), multi select (checkbox), multi select (list box), text, long text
    String label
    String name
    String title
    String clazz
    String value
    String placeholder
    String validation

    Collection<String> options = []
    SimplifiedEvent event

    static hasMany = [options: String]
    static belongsTo = [event: SimplifiedEvent]

    static constraints = {
        validation nullable: true
        placeholder nullable: true
        value nullable: true
        clazz nullable: true
        title nullable: true
    }

    static mapping = {
        options joinTable:[name: "simplified_event_checkout_field_options", key: "field_id", column: "option_text", type: "varchar(255)"]
        type length: 50
        validation length: 100
        placeholder length: 100
        value length: 500
        clazz length: 100
        title length: 100
        label length: 100
    }
}
