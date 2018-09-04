bm.onReady(app, "DiscountEditor", function() {
    var _d = app.DiscountEditor.prototype;
    var leftTable, rightTable;
    var selectedVariation = {};
    var selector;

    _d.initProductDiscountDetails = function(body) {
        loadSelectedVariations();
        body.find(".choose-product").on("click", () => {
            [0, 1, 2, 3, 4, 5].iterate((iterator) => {
                var popupForm = $("body > .popup .product-selection-panel");
                leftTable = popupForm.find(".first-column");
                rightTable = popupForm.find(".last-column");
                if(!popupForm.length) {
                    setTimeout(iterator.next, 1000);
                } else {
                    selector = (popupForm.closest(".popup")).data("selector");
                    this.checkVariationSelection();
                    this.bindSelectorEvents();
                    this.bindVariationEvents(popupForm);
                    var oldPreSubmit = popupForm.form("prop", "preSubmit");
                    var oldSelections;

                    popupForm.form("prop", "preSubmit", oldPreSubmit.intercept(() => {
                        oldSelections = rightTable.find("tr.discountProducts");
                        $.each(oldSelections, function (key, value) {
                            $(value).find(".show-variation").remove();
                        });
                    }).blend(() => {
                        $.each(oldSelections, function (key, value) {
                            $(value);
                        });
                        return false
                    }))
                }
            });
        });
    }.blend(_d.initProductDiscountDetails);

    _d.checkVariationSelection = function () {
        var variationProduct = leftTable.find(".show-variation").closest("tr");
        var productWithVariation = variationProduct.find(".actions-column :checkbox").filter(":checked").closest("tr");
        $.each(productWithVariation, function (key, value) {
            var productId = Number($(value).find(".show-variation").attr("product-id"));
            bm.ajax({
                url: app.baseUrl + "variationDiscountAdmin/getProductVariationList",
                data: {productId: productId},
                success: function(resp) {
                    var variationList = JSON.parse(resp.variations);
                    if(selectedVariation[productId].length == variationList.length) {
                        $(value).find("input.multiple").prop("checked", true);
                    } else if(selectedVariation[productId].length < variationList.length && selectedVariation[productId].length > 0) {
                        $(value).find("input.multiple").checkbox("state", "partial");
                    }
                }
            });
        });
    };

    _d.bindSelectorEvents = function () {
        var _this = this;
        var _superBeforeLoad = selector.beforeLoadTableContent;
        selector.beforeLoadTableContent = function (params) {
            _superBeforeLoad(params);
        };

        selector.onNewSelection = function (row) {
            var td = row.find("td").first();
            var productId = Number(row.find(".actions-column").attr("item"));
            var selectedElement = leftTable.find('[discountproducts='+ productId +']').parent();
            var productName = selectedElement.find('td .product-name').text().trim();
            var productNameDom = '<span class="product-name">' + productName + '</span>';
            var variationDom = '<span class="show-variation" product-id="' + productId + '">' + $.i18n.prop("variation") + '</span>';

            td.text("");
            td.append(productNameDom);
            if((selectedElement.find("td").first()).find(".show-variation").length)  td.append(variationDom);
            row.updateUi();
        };

        selector.resetRightPanel = function (resp, rightPanelSelectedTd, callback) {
            var respTds = resp.find("td.actions-column");
            rightPanelSelectedTd.each(function() {
                var td = $(this);
                var respTd = respTds.find("input[value="+td.attr("item")+"]").parent();
                if(!respTd.length) {
                    td.parent().remove();
                }
            });
            if(callback) {
                callback();
            }
            _this.checkVariationSelection();
        };
    };

    _d.bindVariationEvents = function(popupForm) {
        popupForm.on("click", ".show-variation", function () {
            var _this = $(this);
            var productId = Number(_this.attr("product-id"));
            var productElement = _this.closest("tr");
            var data = {
                "productId": productId,
                "selectedVariations": selectedVariation[productId]
            };

            bm.floatingPanel(_this, app.baseUrl + "variationDiscountAdmin/showProductVariation", data, {
                width: 350,
                height: null,
                clazz: "list-variation-popup discount",
                position_collison: "none",
                events: {
                    content_loaded: function (popup) {
                        var element = popup.el;
                        element.updateUi();
                        element.on("change", ".variation-selection input:checkbox", function (ev) {
                            var productOnLeft = leftTable.find("[product-id=" + productId + "]").closest("tr");
                            var variationId = Number($(this).parent().attr('variation-item'));
                            if ($(ev.target).prop("checked")) {
                                addToCache(productId, variationId);
                                productOnLeft.find(".actions-column .unchecked").trigger("click");
                                updateHeaderCheckStatus(productOnLeft, element);
                            } else {
                                removeFromCache(productId, variationId);
                                if(!element.find(":checkbox").filter(":checked").length){
                                    productOnLeft.find(".actions-column .wcui-checkbox").trigger("click");
                                }
                                updateHeaderCheckStatus(productOnLeft, element);
                            }
                        });
                    }
                }
            });
        });

        $(".header .toolbar-btn.save").on("click", function () {
            bm.ajax({
                url: app.baseUrl + "variationDiscountAdmin/cacheVariationData",
                data: {selectedVariation: JSON.stringify(selectedVariation)},
                show_response_status: false
            });
        });

        // todo: fix change on product selection
        popupForm.on("change", "td.actions-column input:checkbox", function (element) {
            var productId = Number($(this).parent().attr("discountproducts"));
            if($(element.target).prop("checked")) {
                // bm.ajax({
                //     url: app.baseUrl + "variationDiscountAdmin/getProductVariationList",
                //     data: {productId: productId},
                //     success: function(resp) {
                //         $.each(JSON.parse(resp.variations), function (key, value) {
                //             addToCache(productId, value);
                //         });
                //     }
                // });
            } else {
                selectedVariation[productId] = [];
            }
        });

        popupForm.on('click', '.actions-column .remove-all', function (element) {
            selectedVariation = {};
        });
    };

    function loadSelectedVariations() {
        bm.ajax({
            url: app.baseUrl + "variationDiscountAdmin/getAllSelectedVariations",
            data: {},
            show_response_status: false,
            success: function (data) {
                selectedVariation = data.selectedVariations;
            }
        });
    }

    function addToCache(productId, variationId) {
        if(selectedVariation[productId] === undefined) {
            selectedVariation[productId] = [];
        }
        if(!selectedVariation[productId].contains(variationId)) {
            selectedVariation[productId].push(variationId);
        }
    }

    function removeFromCache(productId, variationId) {
        if(selectedVariation[productId].contains(variationId)) {
            var index = selectedVariation[productId].indexOf(variationId);
            if(index > -1)  selectedVariation[productId].splice(index, 1);
        }
    }

    function updateHeaderCheckStatus(element, popup) {
        var checkBoxes = popup.find(":checkbox");
        if (checkBoxes.filter(":checked").length == 0) {
            element.find("input.multiple").prop("checked", false)
        } else if (checkBoxes.length == checkBoxes.filter(":checked").length) {
            element.find("input.multiple").prop("checked", true)
        } else {
            element.find("input.multiple").checkbox("state", "partial")
        }
        if (element.find("input.multiple").closest("th").length) {
            element.scrollbar("content", element.find(".check-all-variation").closest("th"))
        }
    }
});
