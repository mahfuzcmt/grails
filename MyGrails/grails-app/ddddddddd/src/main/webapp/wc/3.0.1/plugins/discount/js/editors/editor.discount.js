(function() {
    var twoSideSelectorPreviewer = {
        name: function (data, selectionPanel, previewPanel) {
            var names = [];

            $.each(data, function (key, value) {
                value.forEach(function (item) {
                    names.push(item.name)
                });
            });

            if (names.length > 0) {
                previewPanel.empty();
                $.each(data, function (key, value) {
                    value.forEach(function (item) {
                        previewPanel.append($("<input>", {
                            name: key,
                            type: "hidden",
                            value: item.value
                        }));
                    });
                });

                previewPanel.append($("<span>", {
                    text: names.join(",")
                }))
            }

        },
        table: function (data, selectionPanel, previewPanel, configs) {
            var table = $("<table>"),
                header = $("<tr>");
            configs = configs || {};
            var maxLength = 0;
            $.each(data, function (key, value) {
                var keys = key.split(".");
                key = keys[keys.length - 1];
                header.append($("<th>", {text: $.i18n.prop(key.dotCase())}));
                maxLength = Math.max(maxLength, value.length);
            });
            if (configs.remove_btn) {
                header.append($("<th>"));
            }
            table.append(header);

            for (var i = 0; i < maxLength; i++) {
                var row = $("<tr>");
                $.each(data, function (key, value) {
                    var td = $("<td>", {text: value[i] ? value[i].name : ""});
                    value[i] && td.append($("<input>", {
                        type: "hidden",
                        name: key,
                        value: value[i].value
                    }));
                    row.append(td);
                });
                if (configs.remove_btn) {
                    row.append($("<td>").append($("<span>", {
                        "class": "tool-icon remove"
                    })))
                }
                row.find(".remove").on("click", function() {
                    row.remove()
                });
                table.append(row)
            }
            previewPanel.html(table);
            bm.pageableTable(table, {});
        }
    };

    var initFuncs = {
        customerAndGroupSelection:  function (selector, previewPanel, previewer) {
            selector.on("click", function () {
                var $this = $(this);
                var customerField = $this.attr("customer-field"), groupField = $this.attr("group-field");
                bm.customerAndGroupSelectionPopup(selector.parents("form"), {
                    customerField: customerField,
                    groupField: groupField,
                    preview_panel: previewPanel,
                    previewer: previewer,
                })

            });
        },
        productSelection: function (selector, fieldName, title, previewPanel, previewer) {
            selector.on("click", function () {
                var $this = $(this);
                bm.productSelectionPopup(selector.parents("form"), {
                    fieldName: fieldName,
                    previewer: previewer,
                    preview_holder: previewPanel,
                    title: title,
                    previewConfig: {
                        remove_btn: false
                    }
                });
            });
        },
        productAndCategorySelection: function (selector, previewPanel, previewer) {
            selector.on("click", function () {
                var $this = $(this);
                var productField = $this.attr("product-field"), categoryField = $this.attr("category-field");
                bm.productAndCategorySelectionPopup(selector.parents("form"), {
                    productField: productField,
                    categoryField: categoryField,
                    preview_panel: previewPanel,
                    previewer: previewer,
                })
            });
        },
        tierTable: function (tierTable, option) {
            var rowTemplate = tierTable.find(".template").detach().removeClass("template hidden"),
                lastRow = tierTable.find(".last-row"), nextRowId = tierTable.find(".tier-details-row").length;
            rowTemplate.find(":input").each(function () {
                var $this = $(this);
                $.each($this.data(), function (key, value) {
                    $this.removeAttr("data-" + key);
                    $this.attr(key, value)
                });
            });
            function attachRowEvent(row) {
                row.find(".remove").on("click", function () {
                    row.remove()
                })
            }

            tierTable.find("tr:not(.last-row)").each(function () {
                attachRowEvent($(this))
            });
            lastRow.find(".add-row").on("click", function () {
                var row = rowTemplate.clone(), namePrefix = option.namePrefix + nextRowId++ + ".";
                row.find(":input").each(function () {
                    var $this = $(this);
                    $this.attr("name", namePrefix + $this.attr("name"))
                });
                row.updateUi();
                lastRow.before(row);
                attachRowEvent(row);
                option.validator.reInit();
                tierTable.find(":text[restrict]").each(function() {
                    var input = $(this);
                    input[input.attr("restrict")]();
                });
            })
        },
        allCustomerSelection:  function (selector, previewPanel, previewer) {
            selector.on("click", function () {
                var $this = $(this);
                $this.addClass("selected");
                $(".discount-choose-customer").removeClass("selected");

                var data = {};
                data["isAppliedAllCustomer"] = [];
                data["isAppliedAllCustomer"].push({
                    name: $this.text().trim(),
                    value: true
                })

                previewer(data, selector.parents("form"), previewPanel);

            });
        },
        allProductSelection:  function (selector, previewPanel, previewer) {
            selector.on("click", function () {
                var $this = $(this);
                $this.addClass("selected");
                $(".discount-choose-product").removeClass("selected");

                var data = {};
                data["isAppliedAllProduct"] = [];
                data["isAppliedAllProduct"].push({
                    name: $this.text().trim(),
                    value: true
                })

                previewer(data, selector.parents("form"), previewPanel);

            });
        },
        viewCouponCodes:  function (selector) {
            var _self = this;
            selector.on("click", function () {
                var $this = $(this);

                var title = "coupon.codes";
                var couponId = $this.attr("data-coupon-id");

                if (couponId) {

                    var url = app.baseUrl + "coupon/loadCouponCodes";
                    bm.editPopup(url, $.i18n.prop(title), null, {couponId: couponId}, {
                        width: 850,
                        success: function () {
                        },
                        events: {
                            content_loaded: function () {
                                var content = $(this);
                                var bindPaginator = function () {
                                    var paginator = content.find(".pagination").obj();
                                    if (paginator) {
                                        paginator.onPageClick = function (page) {
                                            bm.ajax({
                                                url: url,
                                                data: {
                                                    couponId: couponId,
                                                    offset: (page-1) * 10
                                                },
                                                dataType: "html",
                                                success: function (resp) {
                                                    $(content.find(".table-view")).html($(resp)).updateUi();
                                                    bindPaginator()
                                                }
                                            })
                                        }
                                    }

                                    var searchBox = content.find(".search-text");
                                    content.find(".icon-search").on("click", function() {
                                        bm.ajax({
                                            url: url,
                                            data: {
                                                couponId: couponId,
                                                searchText: searchBox.val()
                                            },
                                            dataType: "html",
                                            success: function (resp) {
                                                $(content.find(".table-view")).html($(resp)).updateUi();
                                                bindPaginator()
                                            }
                                        })
                                    })
                                };
                                bindPaginator()

                            }
                        }
                    });

                } else {
                    bm.notify($.i18n.prop("coupon.codes.not.generate"), "alert");
                }

            });
        },
        exportCouponCodes:  function (selector) {
            selector.on("click", function () {
                var $this = $(this);

                var title = "coupon.codes";
                var couponId = $this.attr("data-coupon-id");

                if (couponId) {

                    window.open(app.baseUrl + "coupon/exportCoupon?couponId="+couponId)

                } else {
                    bm.notify($.i18n.prop("coupon.codes.not.generate"), "alert");
                }

            });
        },
    };

    app.DiscountEditor = function (body) {

        var _self = this, type = body.find("[name=type]").val();
        _self.type = type;
        _self.id = body.find("[name=id]").val()
        _self.body = body;
        body.find(".body").scrollbar("destroy");
        var form = _self.form = body.find("form");
        _self.validator = form.data("validatorInst");

        _self.rightPanel = body.find(".right-panel").scrollbar();
        body.find(".left-bar").scrollbar()

        var customerPreviewPanel = $(".select-customer-preview-table");
        var customerSelector = body.find(".discount-choose-customer");
        initFuncs.customerAndGroupSelection(customerSelector, customerPreviewPanel, twoSideSelectorPreviewer.name);

        var productPreviewPanel = $('.select-product-preview-table');
        var productSelector = body.find(".discount-choose-product");
        initFuncs.productAndCategorySelection(productSelector, productPreviewPanel, twoSideSelectorPreviewer.name);

        var allCustomerSelector = body.find(".discount-all-customer");
        var allProductSelector = body.find(".discount-all-product");
        initFuncs.allCustomerSelection(allCustomerSelector, customerPreviewPanel, twoSideSelectorPreviewer.name);
        initFuncs.allProductSelection(allProductSelector, productPreviewPanel, twoSideSelectorPreviewer.name);

        var excludeProductPreviewPanel = $('.exclude-product-preview-table');
        var excludeProductSelector = body.find(".choose-exclude-product");
        initFuncs.productSelection(excludeProductSelector, "excludeProducts", $.i18n.prop("exclude.product.on.sale"), excludeProductPreviewPanel, twoSideSelectorPreviewer.table);

        var viewCouponCodeSelector = body.find(".view-coupon-code");
        initFuncs.viewCouponCodes(viewCouponCodeSelector);

        var exportCouponCodeSelector = body.find(".export-coupon-code");
        initFuncs.exportCouponCodes(exportCouponCodeSelector);

        form.on("change", function () {
            if (customerPreviewPanel.find("input[name=isAppliedAllCustomer]").length) {
                allCustomerSelector.addClass("selected");
                customerSelector.removeClass("selected");
            } else if (customerPreviewPanel.find("input[name]").length) {
                allCustomerSelector.removeClass("selected");
                customerSelector.addClass("selected");
            }

            if (productPreviewPanel.find("input[name=isAppliedAllProduct]").length) {
                allProductSelector.addClass("selected");
                productSelector.removeClass("selected");
            } else if (productPreviewPanel.find("input[name]").length) {
                allProductSelector.removeClass("selected");
                productSelector.addClass("selected");
            }
        });

        _self.loadDiscountDetails()

        body.find("input[name=isCouponCodeAutoGenerate]").on("change", function() {
            var defaultCouponCode = body.find("input[name=defaultCouponCode]")
            var generatedCouponCode = body.find("input[name=generatedCouponCode]")

            if($(this).is(":checked")) {
                defaultCouponCode.val( generatedCouponCode.val() )
                defaultCouponCode.attr('readonly', true);
            } else {
                defaultCouponCode.attr('readonly', false);
            }
        })

        body.find("input[name=isCreateUniqueCouponEachCustomer]").on("change", function() {
            var defaultCouponCodeSection = body.find(".default-coupon-code-section")
            if($(this).is(":checked")) {
                defaultCouponCodeSection.hide()
            } else {
                defaultCouponCodeSection.show()
            }
        })

    };

    app.CouponCodeHistory = function (body) {

        var _self = this, type = body.find("[name=type]").val();
        _self.type = type;
        _self.id = body.find("[name=id]").val()
        _self.body = body;

        body.find(".body").scrollbar("destroy");

        /*
        var form = _self.form = body.find("form");
        _self.validator = form.data("validatorInst");

        _self.rightPanel = body.find(".right-panel").scrollbar();
        body.find(".left-bar").scrollbar()*/


    };

    var _d = app.DiscountEditor.prototype;

    _d.loadDiscountDetails = function () {
        var _self = this;
        _self.rightPanel.loader();
        bm.ajax({
            url: app.baseUrl + "discount/loadDiscountDetails",
            data: {
                discountId: _self.id,
                type: _self.type,
                conditionType: _self.conditionType
            },
            dataType: "html",
            success: function (resp) {
                resp = $(resp);
                resp.updateUi();
                _self.body.find(".discount-details-wrap").html(resp);
                _self.validator.reInit();
                _self.body.find(":text[restrict]").each(function() {
                    var input = $(this);
                    input[input.attr("restrict")]();
                });
                _self.initDiscountDetails();
                bm.autoToggle(resp.parent());
                _self.rightPanel.loader(false);

                _self.body.find(".bmui-tab-header.discount-detail-tab").on("click", function() {
                    var discountDetailsType = $(this).attr("data-tabify-tab-id")
                    $("#discountDetailsType").val(discountDetailsType);
                })

                $("#discount-detail-tabs").tabify("activate", $("#discountDetailsType").val())

            }
        })

    };

    // discount details

    _d.initDiscountDetails = function () {
        var _self = this;
        _self.body.find(".discount-details").each(function () {
            var $this = $(this), type = $this.data("type");
            if (type) {
                _self["init" + type.capitalize() + "DiscountDetails"]($this)
            }
        });
    };

    _d.initAmountDiscountDetails = function (body) {
        var _self = this, tierTable = body.find("table");
        initFuncs.tierTable(tierTable, {
            validator: _self.validator,
            namePrefix: "amountDetails.amountTier."
        });
    };

    _d.initShippingDiscountDetails = function (body) {
        var _self = this, tierTable = body.find("table");
        initFuncs.tierTable(tierTable, {
            validator: _self.validator,
            namePrefix: "shippingDetails.amountTier."
        });
    };

    _d.initProductDiscountDetails = function (body) {
        var _self = this, tierTable = body.find("table");
        initFuncs.tierTable(tierTable, {
            validator: _self.validator,
            namePrefix: "productDetails.quantityTier."
        });
        body.find(".choose-product").on("click", function () {
            var $this = $(this), previewer = $this.siblings(".preview-table");
            bm.productSelectionPopup(previewer, {
                fieldName: "discountProducts",
                previewer: twoSideSelectorPreviewer.table,
                previewConfig: {
                    remove_btn: false
                }
            });
        });
    };

})();