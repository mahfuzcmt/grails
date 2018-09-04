$(function () {
    bm.onReady(app.tabs, "setting", function () {
        app.tabs.setting.prototype.initLive_chatSettings = function (_data) {
            var _self = this;
            var _liveChatProto = app.tabs.liveChat.prototype;
            var form = _data.panel.find("form");
            var table = form.find(".live-chat-department-settings");


            var tabulator = bm.table(table, {
                url: app.baseUrl + "liveChatAdmin/loadChatDepartments",
                sortable: {"0": "name", "1": "description"},
                sorted: "0",
                sortedDir: "up",
                menu_entries: [
                    {
                        text: $.i18n.prop("view"),
                        ui_class: "view",
                        action: "view"
                    },
                    {
                        text: $.i18n.prop("edit"),
                        ui_class: "edit",
                        action: "edit"
                    },
                    {
                        text: $.i18n.prop("remove"),
                        ui_class: "remove",
                        action: "remove"
                    }
                ]
            });

            tabulator.onActionClick = function (action, data) {
                switch (action) {
                    case "view":
                        viewDepartment(data.id);
                        break;
                    case "edit":
                       editDepartment(data.id);
                        break;
                    case "remove":
                        removeDepartment(tabulator, data)
                        break;
                }
            }

            function editDepartment(id) {
                bm.editPopup(app.baseUrl + "liveChatAdmin/getChatDepartmentEditPopup", $.i18n.prop("edit.department"), null, {id: id}, {
                    success: function () {
                        tabulator.reload();
                    }
                })
            };

            function viewDepartment(id) {
                bm.editPopup(app.baseUrl + "liveChatAdmin/viewChatDepartmentDetail", $.i18n.prop("department.detail"), null, {id: id}, {
                 })
            };

            table.on("click", ".add-department", function () {
                bm.editPopup(app.baseUrl + "liveChatAdmin/getChatDepartmentCreatePopup", $.i18n.prop("add.new.department"), null, null, {
                    success: function () {
                        tabulator.reload();
                    }
                })
            });
        }
    })

    function removeDepartment(tabulator, data) {
        bm.confirm($.i18n.prop("confirm.remove", ["department", data.name]), function() {
            bm.ajax({
                url: app.baseUrl + "liveChatAdmin/removeChatDepartment",
                data: data,
                success: function () {
                    tabulator.reload();
                }
            });
        }, function () {
        });
    }
})