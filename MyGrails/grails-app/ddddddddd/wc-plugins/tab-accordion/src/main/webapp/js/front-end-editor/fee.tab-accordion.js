app.FrontEndEditor.WIDGETS.tabAccordion = {
    label: "Tab Accordion", inlineEdit: false, type: app.FrontEndEditor.WIDGET_TYPE.WIDGET, popupTitle: 'widget.tabAccordion'
};

app.FrontEndEditor.widget.tabAccordion = function () {
    app.FrontEndEditor.widget.tabAccordion._super.constructor.apply(this, arguments);
};

var _acc = app.FrontEndEditor.widget.tabAccordion.inherit(app.FrontEndEditor.widgetBase);


var setEditable = function (element) {
    function setText(currElement) {
        if (element.valid()) {
            element.find("span.fee-text").html(currElement.val()).show();
            element.find("span.fee-edit-icon").removeClass('fee-hidden');
            currElement.addClass('fee-hidden');
        }
    }

    element.find("span.fee-text").hide();
    element.find('.fee-hidden').removeClass('fee-hidden');
    element.find('input').on("focusout", function () {
        setText($(this));
    }).on("keypress", function (e) {
        if (e.keyCode == 13) {
            e.preventDefault();
            setText($(this));
        }
    });
};

_acc.init = function () {
    var _self = this;
    var panel = this.configPanel;
    panel.find("form").updateUi();
    this.tabAddTable = panel.find('.fee-tab-add-table');
    this.addItem();

    this.tabAddTable.delegate('.fee-edit-icon', 'click', function () {
        var currEl = $(this);
        var closestEl = currEl.closest(".fee-editable");
        setEditable(closestEl);
        currEl.addClass('fee-hidden');
    });
    this.tabAddTable.delegate('.fee-remove', 'click', function () {
        $(this).closest("tr").remove();
    });
};

_acc.addItem = function () {
    var _self = this;
    var tabNewEntryTable = this.configPanel.find('.fee-add-new-entry');
    var cloneRow = tabNewEntryTable.find("tr:first").clone();
    cloneRow.updateUi();
    cloneRow.find('.fee-add-content-btn').on('click', function () {
        if (cloneRow.valid()) {
            var firstTd = cloneRow.find("td:first");
            var secondTd = cloneRow.find("td:nth-child(2)");
            var contentNameInput = firstTd.find("input");
            var contentIdInput = secondTd.find("select");
            contentNameInput.attr("name", "contentName");
            firstTd.prepend("<span class='fee-text'>" + contentNameInput.val() + "</span>");
            contentIdInput.attr("name", "contentId");
            $(this).hide();
            cloneRow.find('.fee-hidden').removeClass('fee-hidden');
            contentNameInput.addClass('fee-hidden');
            _self.addItem();
        }
    });
    this.tabAddTable.append(cloneRow);
};

_acc.beforeSave = function (ajaxSettings) {
    var titleValue = this.configPanel.find('input[name="title"]').val();
    var typeValue = this.configPanel.find('input[name="type"]').val();
    var axisValue = this.configPanel.find('input[name="axis"]').val();

    ajaxSettings.data = {
        loadJs: true,
        params: JSON.stringify({title: titleValue, type: typeValue, axis: axisValue})
    };
    return true;
};


