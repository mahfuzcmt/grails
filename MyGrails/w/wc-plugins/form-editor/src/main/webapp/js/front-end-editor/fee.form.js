app.FrontEndEditor.WIDGETS.form = {
    label: "Form", inlineEdit: false, type: app.FrontEndEditor.WIDGET_TYPE.WIDGET, popupTitle: 'widget.form'
};

app.FrontEndEditor.widget.form = function () {
    app.FrontEndEditor.widget.form._super.constructor.apply(this, arguments);
};

var _form = app.FrontEndEditor.widget.form.inherit(app.FrontEndEditor.widgetBase);


_form.init = function () {
    var _self = this;
    var panel = this.configPanel;
    panel.updateUi();
};

_form.beforeSave = function (ajaxSettings) {
    var titleValue = this.configPanel.find('input[name="title"]').val();
    ajaxSettings.data = {
        params: JSON.stringify({title: titleValue})
    };
    return true;
};


