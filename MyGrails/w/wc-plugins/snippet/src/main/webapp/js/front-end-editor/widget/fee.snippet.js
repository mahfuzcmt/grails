app.FrontEndEditor.widget.snippet = function () {
    app.FrontEndEditor.widget.snippet._super.constructor.apply(this, arguments);
};

var _s = app.FrontEndEditor.widget.snippet.inherit(app.FrontEndEditor.widgetBase);

_s.init = function () {
    var _self = this;
    _self.isNew = _self.widgetUUID === undefined;
    var panel = this.configPanel, snippetSelector = panel.find(".snippetId").data("snippet-id"), selectedSnippet = panel.find(".selected-snippet");
    panel.updateUi();
};

_s.getContentValue = function () {
    var widgetValue =  this.configPanel.clone();
    widgetValue.removeClass('fee-widget-row fee-widget-column fee-widget-row-active fee-widget-column-active fee-active-state fee-widget-selected-container');
    widgetValue.find(".snippetId").remove();
    widgetValue.find(".fee-overlay").remove();
    widgetValue.find(".fee-widget-menu").remove();
    widgetValue.filter(".fee-overlay,.fee-border-overlay,.fee-widget-chooser,.fee-widget-command,.fee-after,.fee-before,.bmui-resize-handle,.fee-resize-info,.bmui-sortable-placeholder,.fee-add-content,.fee-menu-bar,.fee-floating-editor-menu").replaceWith("");

    return widgetValue.html();
}

_s.getEditApiUrl = function ()
{
    return "widget/saveSnippetWidgetAndContent";
}

_s.getContentIdKey = function ()
{
    return "id";
}

_s.getContentValueKey = function ()
{
    return "content";
}

_s.addAdditionalData = function (data) {
    var _self = this;
    data["name"] = _self.widgetUUID;
    // data["templateUUID"] = _self.widgetUUID;
}

_s.getContentId = function () {
    return  this.configPanel.find(".snippetId").data("snippet-id");
}

_s.beforeSave = function () {
    if (this.configPanel.find(".snippetId").data("snippet-id") || this.configPanel.find('[name=template]').val()) {
        return true
    }
    bm.notify($.i18n.prop("select.snippet.or.template"), "alert");
    return false
};