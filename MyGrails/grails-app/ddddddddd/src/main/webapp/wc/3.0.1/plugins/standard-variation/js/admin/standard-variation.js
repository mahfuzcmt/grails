var StandardVariation = (function (panel) {

    var pId = panel.find(".product-id").val();
    var productImageMap = {}
    loadImageMap();
    app.global_event.on("product-image-video-update", function(evt, id) {
        loadImageMap();
    })
    function loadImageMap() {
        bm.ajax({
            url: app.baseUrl + 'standardVariation/loadAvailableImageAsJSON',
            data: {id: pId},
            success: function(resp) {
                productImageMap = {}
                $.each(resp, function(id, url) {
                    productImageMap[id] = url;
                });
            }
        })
    }

    return {
        edit : function(id, combination, afterLoad, submitSuccess) {
            bm.ajax({
                url: app.baseUrl + "standardVariation/loadCombinationSetting",
                data: {id: id},
                dataType: "html",
                success: function(resp) {
                    resp = $(resp);
                    var configPanel = panel.find(".standard-variation-panel");
                    if(configPanel.length) {
                        configPanel.replaceWith(resp);
                    } else {
                        panel.find(".variation-combination").after(resp);
                    }
                    configPanel = resp;
                    configPanel.form({
                        ajax: true,
                        preSubmit: function(ajaxSettings) {
                            $.extend(ajaxSettings , {
                                success: function(resp) {
                                    if(submitSuccess) {
                                        submitSuccess();
                                    }
                                }
                            })
                        }
                    });
                    configPanel.updateUi();
                    var amountInput = configPanel.find("#amount")
                    configPanel.find("select#price-adjustment").on("change", function(evt, oldValue, newValue) {
                        var basePrice = amountInput.attr("base-price");
                        if(newValue == 'b') {
                            amountInput.val(basePrice);
                            amountInput.prop("disabled", true);
                        } else {
                            amountInput.prop("disabled", false);
                        }
                        var validation = amountInput.attr("validation");
                        if(newValue == 'r') {
                            amountInput.attr("validation", validation + " lte["+basePrice+"]")
                        } else {
                            var tempVal = [];
                            validation.split(" ").every(function(k, v) {
                                if(!v.startsWith("lte[")) {
                                    tempVal.push(v);
                                }
                            });
                            amountInput.attr("validation", tempVal.join(" "));
                        }
                        configPanel.updateValidator();
                    })
                    configPanel.find('.image-reference').click(function() {
                        bm.editPopup(app.baseUrl + 'standardVariation/loadAvailableImage', $.i18n.prop('available.image'), '', {id: pId}, {
                            width: 736,
                            events: {
                                content_loaded: function(popup) {
                                    this.find('.image-selector .image-holder').click(function() {
                                        var imageId = $(this).attr('image-id');
                                        configPanel.find(".image-id").val(imageId);
                                        if(+imageId) {
                                            configPanel.find('.image-reference').html('<img src="' + productImageMap[imageId] + '">');
                                        } else {
                                            configPanel.find('.image-reference').html('<span class="tool-icon add"></span><span class="add-image">' + $.i18n.prop('add.image') + '</span>');
                                        }
                                        popup.close();
                                    });
                                }
                            }
                        });
                    });
                    if(afterLoad) {
                        afterLoad();
                    }
                }
            })
        }
    }

})