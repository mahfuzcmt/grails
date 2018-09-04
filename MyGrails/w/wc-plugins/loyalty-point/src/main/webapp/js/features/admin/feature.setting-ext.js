(function () {
    var tableAppend = function (resp, previewPanel, oldRuleId) {
        if(oldRuleId){
            bm.ajax({
                url: app.baseUrl + "loyaltyPointAdmin/removeRule",
                data: {ruleId: oldRuleId},
                show_response_status: false,
                success: function() {
                    previewPanel.find('[rule-id='+ oldRuleId +']').remove();
                }
            });
        }
        var ruleDetail = resp.rule;
        var adjustmentSign = "";
        if(ruleDetail.ruleType.equals("point.increase")) adjustmentSign = '+';
        else if(ruleDetail.ruleType.equals("increase.times")) adjustmentSign = 'x';
        var tr = $("<tr>", {
            class: "rule-data",
            'rule-id': ruleDetail.id
        });
        tr.append($("<td>", {text: ruleDetail.name}));
        tr.append($("<td>", {text: adjustmentSign + ruleDetail.point}));
        var actionRow = $('<td class="actions-column">');
        actionRow.append($('<span class="tool-icon choose-customer show-customer-group">'));
        actionRow.append($('<span class="tool-icon edit">'));
        actionRow.append($('<span class="tool-icon remove">'));
        tr.append(actionRow);
        if(previewPanel.find("table").length > 0)   previewPanel.find("table").append(tr);
        else {
            var table = $("<table>"), header = $("<tr>");
            header.append($("<th>", {text: $.i18n.prop("name")}));
            header.append($("<th>", {text: $.i18n.prop("adjustment")}));
            header.append($("<th>", {text: $.i18n.prop("action")}));
            table.append(header);
            table.append(tr);
            previewPanel.append(table);
        }
    };

    var bindActionEvents = function (ruleTable) {
        ruleTable.on("click", ".remove", function () {
            var $this = $(this);
            var rowElement = $this.closest(".rule-data");
            var ruleId = rowElement.attr("rule-id");
            bm.ajax({
                url: app.baseUrl + "loyaltyPointAdmin/removeRule",
                data: {ruleId: ruleId},
                success: function(resp) {

                }
            });
            var table = rowElement.closest("table");
            rowElement.remove();
            if(table.find(".rule-data").length <= 0)    table.remove();
        });
        ruleTable.on("click", ".edit", function () {
            var $this = $(this);
            var rowElement = $this.closest(".rule-data");
            var previewPanel = rowElement.closest(".preview-table");
            var ruleId = rowElement.attr("rule-id");
            bm.customerAndGroupSelectionPopup(rowElement.parents("form"), {
                preview_panel: previewPanel,
                previewer: function () {},
                title: $.i18n.prop("edit.rule"),
                data: {rule_id: ruleId},
                url: app.baseUrl + "loyaltyPointAdmin/editRule",
                beforeSubmit: function () {},
                success: function (resp) {
                    tableAppend(resp, previewPanel, ruleId);
                }
            });
        });
        ruleTable.on("click", ".show-customer-group", function () {
            var $this = $(this);
            var url = app.baseUrl + "loyaltyPointAdmin/showSelectedCustomersAndGroups";
            var rowElement = $this.closest(".rule-data");
            var ruleId = rowElement.attr("rule-id");
            bm.editPopup(url, $.i18n.prop("view.customer.and.group"), '', {rule_id: ruleId}, {
                width: 850,
                height: 730,
                events: {
                    content_loaded: function () {

                    }
                },
                beforeSubmit: function() {},
                success: function() {}
            })
        });
    };

    var initFunc = {
        ruleCreation:  function (selector, previewPanel, previewer) {
            selector.on("click", function () {
                var $this = $(this);
                var customerField = $this.attr("customer"), groupField = $this.attr("customer-group");
                bm.customerAndGroupSelectionPopup(selector.parents("form"), {
                    customerField: customerField,
                    groupField: groupField,
                    height: 'auto',
                    preview_panel: previewPanel,
                    previewer: previewer,
                    title: $.i18n.prop("create.rule"),
                    url: app.baseUrl + "loyaltyPointAdmin/addRule",
                    beforeSubmit: function () {},
                    success: function (resp) {
                        tableAppend(resp, previewPanel);
                    }
                });
            });
        }
    };

    var _s = app.tabs.setting.prototype;
    var initLoyaltyPointSettings = _s.initLoyaltyPointSettings;

    _s.initLoyaltyPointSettings = function (data) {
        if (initLoyaltyPointSettings) {
            initLoyaltyPointSettings.call(this, arguments)
        }
        var ruleAddButton = ".loyalty-point-rules .add-rule";
        var preview = data.panel.find(".loyalty-point-rules .preview-table");
        initFunc.ruleCreation(data.panel.find(ruleAddButton), preview, function () {});
        bindActionEvents(preview);
    };
})();