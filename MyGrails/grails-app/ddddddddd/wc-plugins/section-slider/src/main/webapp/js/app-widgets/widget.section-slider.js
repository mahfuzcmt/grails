app.widget.sectionSlider = function(config) {
    app.widget.sectionSlider._super.constructor.apply(this, arguments);
}

var _w = app.widget.sectionSlider.inherit(app.widget.base);

app.widget.sectionSlider.initShortConfig = function(configDom) {
    var sectionSelectorChosen = configDom.find(".visible-section-selector select").on("chosen:re_ordered", function() {
        $(this).trigger("change")
    }).obj(Chosen)
    sectionSelectorChosen.before_choice_close = function(li) {
        if(li.siblings(".search-choice").length == 0) {
            return false
        }
    }
    sectionSelectorChosen.make_selection_sortable()
}

_w.updateCacheForShortConfig = function(cache, serialized, a, b, configPanel) {
    cache = JSON.parse(cache)
    var params
    if(cache.params) {
        params = JSON.parse(cache.params)
        $.extend(params, serialized)
    } else {
        params = serialized
    }
    cache.widgetContent = []
    if(params.slidable_section_list) {
        if(!$.isArray(params.slidable_section_list)) {
            params.slidable_section_list = [params.slidable_section_list]
        } else {
            params.slidable_section_list = configPanel.find("select[name='slidable_section_list']").obj(Chosen).get_ordered_selection()
        }
        params.slidable_section_list.every(function(i, v) {
            cache.widgetContent[i] = {widget: {sid: cache.sid}, type: "embeddedPage", contentId: +v}
        })
    }
    delete params.slidable_section_list
    cache.params = JSON.stringify(params)
    return JSON.stringify(cache)
}