$(function() {
    var page = $(".page-content");
    var widgets = page.find(".widget-variation .variation-container");
    var productListWidgets = page.find(".widget-variation-product-list .variation-container");

    var loader = "<div><span class='vertical-aligner'></span><img src='" + app.baseUrl + "plugins/variation/images/site/loading.gif'></div>";
    if(widgets.length) {
        widgets.each(function() {
            var widget = this.jqObject;
            var pId = widget.find("input[name=productId]").val();
            widget.data("loader", loader);
            initializeVariation(pId, page, widget);
        })
    }

    if(productListWidgets.length) {
        productListWidgets.each(function() {
            var widget = this.jqObject;
            var pId = widget.find("input[name=productId]").val();
            widget.data("loader", loader);
            initializeVariationInProductList(pId, widget);
        })
    }

    function initializeVariation(productId, content, widget) {
        var type = widget.find(".variation-model");
        if(type.length) {
            var select = widget.find("div.product-variation-select");
            if(select.length) {
                select.chosen({
                    disable_search_threshold: 10,
                    disable_search: true
                });
            }
            var func = "initialise" + type.val().capitalize() + "VariationWidget";
            bm.onReady(window, func, function() {
                window[func](productId, content, widget);
            })
        }
    }

     function initializeVariationInProductList(productId, widget) {
            var type = widget.find(".variation-model");
            if(type.length) {
                var func = "initVariationSelectionInProductList";
                bm.onReady(window, func, function() {
                    window[func](productId, widget);
                })
            }
        }

    app.global_event.on("initialize-info-chose-popup", function(evt, productId, content) {
        initVariationSelection(productId, content);
    })

    site.hook.register("prepareAddCartData", function(data, container, productId, quantity, priceOnly) {
        if(!priceOnly) {
            var combinationForm = container.find(".variation-container");
            if(combinationForm.length) {
                var matrix = combinationForm.find(".combination-matrix");
                var thumb = combinationForm.find(".variation-thumb");
                if(matrix.length) {
                    var cell = matrix.find(".selected");
                    data['config.variation'] = cell.attr("v-id");
                } else if(thumb.length) {
                    var options = [];
                    thumb.find(".option-cell.selected").each(function() {
                        options.push($(this).attr("option-id"));
                    });
                    $.extend(data, {'config.options': options});
                } else {
                    var options = combinationForm.serializeObject();
                    $.extend(data, options);
                }
            }
        }
        return data;
    });

    app.global_event.on("after-product-info-view-initialize", function(evt, productId, content, type, callback) {
        var widget = content.find(".variation-container");
        var modeType = widget.find(".variation-model");
        var description = true;
        if(modeType.length) {
            var matrix = widget.find(".matrix");
            matrix.find(".cell.available").on("click", function() {
                var cell = $(this);
                if(!cell.is(".selected")) {
                    var data = {productId: productId, 'config.variation': cell.attr("v-id"), enableDescription: description};
                    updateCombination(data);
                }
            })
            var select = widget.find("div.product-variation-select");
            if(select.length) {
                select.chosen({
                    disable_search_threshold: 10,
                    disable_search: true
                });
                select = content.find("select.product-variation-select");
                select.on("change", function(ev) {
                    var data = widget.serializeObject();
                    data.enableDescription = description;
                    updateCombination(data);
                })

            }
            var thumbView = widget.find(".variation-thumb");
            thumbView.find(".option-cell").on("click", function() {
                var $this = $(this);
                var ids = [$this.attr("option-id")];
                var types = thumbView.find(".variation-type").not($this.parents(".variation-type"));
                types.each(function() {
                    ids.push($(this).find(".option-cell.selected").attr("option-id"));
                });
                var data = {productId: productId, 'config.options': ids, enableDescription: description};
                updateCombination(data);
            })
        }

        function updateCombination(data) {
            bm.mask(content, loader);
            var shortViewContainer = content.find(".product-info-view-container");
            data.viewType = type;
            bm.ajax({
                url: app.baseUrl + "variation/loadVariationForProductShortView",
                data: data,
                dataType: "html",
                response: function() {
                    bm.unmask(content);
                },
                success: function(resp) {
                    shortViewContainer.replaceWith(resp);
                    if(type == "details") {
                        content.find(".widget-information, .widget-related, .widget-likeus, .widget-shipmentCalculator").remove()
                    }
                    initializeProductInfoView(productId, content, type, callback);
                }
            })
        }
    });

});