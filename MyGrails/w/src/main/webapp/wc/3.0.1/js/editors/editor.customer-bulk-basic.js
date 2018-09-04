bm.onReady(app.tabs, "customerBulkEditor", function () {
    var _panel
    var appTab

    app.tabs.customerBulkEditor.Basic = function (_this, parentTab) {
        _panel = _this
        appTab = parentTab
    }

    var _eb = app.tabs.customerBulkEditor.Basic.prototype;

    _eb.reload = function () {
        this.tabulator.reload();
    }

    _eb.afterTableReload = function () {
        _panel.clearDirty();
        appTab.body.find(".bmui-tab-body-container").find(".bulkedit-parent-td-proxy").remove();
        var basic = appTab.tab_objs.basic
    }

    _eb.init = function () {
        var _self = this;
        app.global_event.on("customer-bulk-updated-basic", function () {
            _self.reload();
        });
        this.tabulator = bm.table(_panel.find(".customer-bulk-edit-tab.basic-table"), {
            url: app.baseUrl + "customerAdmin/loadCustomerBulkProperties",
            beforeReloadRequest: function () {
                _self.beforeReloadRequest(this.param)
            },
            afterLoad: function () {
                _panel.tool.filter(".action-header:visible").hide();
                if (_self.afterTableReload) {
                    _self.afterTableReload()
                }
            },
            onload: function() {
            },
        });
        _self.beforeReloadRequest = function (param) {
            $.extend(param, {property: "basic", ids: this.ids});
        };

        this.table = _panel.find(".body table");

        toggleStatus.call(this);
        changeStoreCredit.call(this)
    }

    function toggleStatus() {
        var _this = this;
        var togglableTd = _this.table.find("td.togglable.custom-toggle");
        var changeAllButton = togglableTd.find(".fake-link");
        var toggleBlock = togglableTd.find(".toggle-block");
        var toggleButton = toggleBlock.find("input[type='checkbox']");
        var resetButton = toggleBlock.find(".reset-status");

        changeAllButton.on("click", function () {
            $(this).hide();
            toggleBlock.show();
        });

        toggleButton.on("change", function () {
            var _self = $(this);
            var tds = _this.table.find("td.togglable.customer-status");

            var hiddenStatus = $("<input type='hidden' name='status' class='hidden-status'/>");
            var statusField = toggleBlock.find("input[name=status]");
            if(statusField.length) {
                if (_self.is(":checked")) {
                    statusField.val("A");
                } else {
                    statusField.val("I");
                }
            } else {
                if (_self.is(":checked")) {
                    hiddenStatus.val("A");
                } else {
                    hiddenStatus.val("I");
                }
                toggleBlock.append(hiddenStatus);
            }


            tds.each(function () {
                var td = $(this);
                if (_self.is(":checked")) {
                    td.find("input[type='checkbox']").prop("checked", true);
                } else {
                    td.find("input[type='checkbox']").prop("checked", false);
                }
            })
        });

        resetButton.on("click", function () {
            var reloadBtn = appTab.body.find(".toolbar-item.reload");
            reloadBtn.trigger('click');
        });
    }

    function changeStoreCredit() {
        var _this = this;
        var changeableStoreCreditTd = _this.table.find("td.change-all.store-credit");
        var changeAllButton = changeableStoreCreditTd.find(".fake-link");
        var storeCreditBlock = changeableStoreCreditTd.find(".store-credit-block");
        var inputField = storeCreditBlock.find("input[name=deltaAmount]");
        var applyButton = storeCreditBlock.find(".apply-button");
        var resetButton = storeCreditBlock.find(".reset-store-credit");
        var note = storeCreditBlock.find(".note");
        var restrict = inputField.attr("restrict");

        if (restrict) {
            inputField[restrict]();
        }
        changeAllButton.on("click", function () {
            $(this).hide();
            storeCreditBlock.show();
        });

        inputField.on("focusout", function () {
            if(valid(inputField) == false) {
                return false
            }
        });

        function appendHiddenNote(note) {
            var hiddenNote = $("<input type='hidden' name='noteText' value=''>");
            hiddenNote.val(note);
            var noteField = storeCreditBlock.find("input[name=noteText]");
            if(noteField.length) {
                noteField.val(note)
            } else {
                storeCreditBlock.append(hiddenNote);
            }
        }

        note.on("click", function () {
            var noteText = storeCreditBlock.find("input[name=noteText]").val()
            var popupDom = $("<form class='edit-popup-form'><div class='form-row'>" +
                "<label>" + $.i18n.prop("note") + "</label>" +
                "<textarea name='noteText' validation='maxlength[500]'></textarea>" +
                "</div>" +
                "<div class='button-line'>" +
                "<button type='submit' class='submit-button'>" + $.i18n.prop("done") + "</button>" +
                "<button type='button' class='cancel-button'> " + $.i18n.prop("cancel") + "</button> &nbsp; &nbsp;" +
                "</div></form>");
            var input = popupDom.find("textarea[name=noteText]");
            noteText ? input.val(noteText) : "";
            bm.editPopup(undefined, $.i18n.prop("add.store.credit.note"), undefined, undefined, {
                content: popupDom,
                events: {
                    content_loaded: function() {
                        var popDom = this;
                        input.focus();
                    }
                },
                beforeSubmit: function(form, data, popup) {
                    popup.close();
                    appendHiddenNote(input.val());
                    return false;
                }
            })
        });

        applyButton.on("click", function () {
            var _self = $(this);
            var _storeCreditBlock = $(this).parent();

            var action = _storeCreditBlock.find(".action").val()
            var inputValue = _storeCreditBlock.find("input[name=deltaAmount]").val()

            if(valid(inputField) == false) {
                return false
            }
            var tds = _this.table.find("td.store-credit").not(".change-all");
            tds.each(function () {
                var td = $(this);
                var tdVal = td.find(".value");
                var oldValue = tdVal.text().replace(",","");
                var newValue = 0;
                if(action == "true") {
                    newValue = parseFloat(oldValue) + parseFloat(inputValue);
                } else {
                    newValue = parseFloat(oldValue) - parseFloat(inputValue);
                    newValue < 0 ? (newValue = 0) : "";
                }
                tdVal.text(newValue);
            })
            _storeCreditBlock.hide();
            changeAllButton.show();
        });

        resetButton.on("click", function () {
            var reloadBtn = appTab.body.find(".toolbar-item.reload");
            reloadBtn.trigger('click');
        });

        function valid(input) {
            var errorObj = ValidationField.validateAs(input, input.attr("validation"));
            if (errorObj) {
                bm.notify($.i18n.prop(errorObj.msg_template, errorObj.msg_params), "alert");
                errorHighlight(input);
                return false;
            }
            return true;
        }

        function errorHighlight(input) {
            applyButton.attr("disabled", true);
            input.addClass("error-highlight");
            setTimeout(function () {
                input.removeClass("error-highlight");
                applyButton.removeAttr("disabled");
            }, 1000);
        }
    }

    app.tabs.customerBulkEditor.errorHighlight = function (item) {
        var tabContainer = appTab.activePanel();
        var updateBtn = tabContainer.find(".submit-button");
        updateBtn.attr("disabled", true);
        item.addClass("error-highlight");
        setTimeout(function () {
            item.removeClass("error-highlight");
            updateBtn.removeAttr("disabled");
        }, 1000);
    }

});
