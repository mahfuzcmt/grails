(function () {
    var _s = app.tabs.setting.prototype;
    var initProductSettings = _s.initProductSettings;
    _s.initProductSettings = function (data) {
        if (initProductSettings) {
            initProductSettings.call(this, arguments)
        }

        var rowHtml = '<tr information-id="#ID#"><td class="editable title">#TITLE#</td><td><span class="tool-icon remove"></span></td></tr>';
        var firstRow = data.panel.find(".custom-information-section tr.first-row");
        var informationTable = data.panel.find("table.custom-information-group");
        var fieldTitle = ".custom-information-title";
        var saveButton = ".custom-information-section .tool-icon.add.add-row";

        data.panel.find(saveButton).on('click', function () {
            var titleInput = data.panel.find(fieldTitle);
            var typeText = "content";
            if( !titleInput.val() ) {
                errorHighlight(titleInput);
                bm.notify($.i18n.prop("value.must.not.be.empty"), "error");
                return false
            }
            addProductSetting(null, titleInput.val(), typeText, null, null, function() {
                titleInput.val("")
            });
        });

        function addProductSetting(id, fieldTitle, fieldType, modifiedDOM, oldTitle, success) {
            bm.ajax({
                url: app.baseUrl + "productCustomInformation/saveCustomFields",
                type: "post",
                data: {id: id, title: fieldTitle, type: fieldType},
                success: function (resp) {
                    afterInserting(resp.id, resp.title);
                    if(success) success.call()
                },
                error: function () {
                    if(modifiedDOM) {
                        modifiedDOM.find(".value").text(oldTitle);
                    }
                }
            })
        }

        function afterInserting(id, title) {
            var row = informationTable.find("tr[information-id=" + id + "]");
            if (row.length) {
                updateRow(row, title);
            } else {
                addRow(id, title);
            }
        }

        function addRow(id, title) {
            var row = $(rowHtml.replace("#ID#", id).replace("#TITLE#", title));
            informationTable.find("tbody").append(row);
            attachEvent(row);
        }

        function updateRow(row, title) {
            row.find("td.title").val(title);
        }

        function removeRow(row) {
            var informationID = row.attr("information-id");
            bm.ajax({
                url: app.baseUrl + "productCustomInformation/removeCustomField",
                data: {informationID: informationID},
                success: function(resp) {
                    row.remove();
                },
                error: function () {
                }
            })
        }

        function afterModification(row, modifiedDOM, oldVal) {
            var id = row.attr("information-id");
            var title = row.find("td.title span.value").text();
            addProductSetting(id, title, "content", modifiedDOM, oldVal);
        }

        attachEvent(data.panel);
        function attachEvent(content) {
            bm.makeTableCellEditable(content.find("td.editable"), function(td, newVal, oldVal) {
                var parent = td.parent("tr");
                if(!newVal) {
                    errorHighlight(td.find("input"));
                    bm.notify($.i18n.prop("value.must.not.be.empty"), "error");
                    return false
                }
                var title = parent.find("td.title span.value").text();
                afterModification(parent, td, oldVal);
            });
            content.find("td span.remove").on("click", function() {
                var data = $(this).parents("tr");
                bm.confirm($.i18n.prop("confirm.delete.custom.information.popup"), function () {
                    removeRow(data);
                }, function () {})
            })
        }

        function errorHighlight(item) {
            item.addClass("error-highlight");
            setTimeout(function() {
                item.removeClass("error-highlight")
            }, 1000);
        }
    };

})();