app.tabs.editEnterpriseVariation = function() {
    app.tabs.editEnterpriseVariation._super.constructor.apply(this, arguments);
    $.extend(this, {
        text: $.i18n.prop("edit.enterprise.product"),
        name: this.variation.combination,
        tip: this.variation.combination,
        ui_class: "edit-tab edit edit-variation-product",
        ajax_url: app.baseUrl + "enterpriseVariation/loadProductEditor?vId=" + this.variation.id
    });
}

app.tabs.editEnterpriseVariation.inherit(app.MultiTab);

var _ev = app.tabs.editEnterpriseVariation.prototype;

(function(){

    app.global_event.on("variation-menu-config", function(evt, config) {
        config.push({
            text: $.i18n.prop("view.in.site"),
            ui_class: "preview view",
            action:"view-in-website"
        });
    })
    app.global_event.on("variation-menu-click", function(evt, action, data) {
        if(action == "view-in-website") {
            var url = app.siteBaseUrl + "product/" + data.url +"?adminView=true"
            window.open(url,'_blank');
        }
    })
    function attachEvents() {
        this.attachEditor();
    }

    _ev.init = function() {
        app.tabs.editEnterpriseVariation._super.init.call(this);
        attachEvents.call(this);
    }

    app.tabs.editEnterpriseVariation.tabInitFunctions = {
        basic: function(panel){
            var _form = panel.find("form");
            panel.find(".description").on("variation-active-change", function(evt, checked) {
                var container = $(this);
                if(checked) {
                    container.removeClass("disabled");
                } else {
                    container.addClass("disabled");
                }
            })
        },
        priceStock: function(panel) {
            panel.find(".view-inventory-history").on("click", function() {
                var id = $(this).attr("variation-id");
                var url = app.baseUrl + "enterpriseVariation/inventoryHistory";
                var title = $.i18n.prop("inventory.history");
                bm.editPopup(url, title, "", {id: id}, {
                    width: 850,
                    success: function() {},
                    events: {
                        content_loaded: function() {
                            var content = $(this);
                            var bindPaginator = function () {
                                var paginator = content.find(".pagination").obj()
                                if(paginator) {
                                    paginator.onPageClick = function(page){
                                        bm.ajax({
                                            url: url,
                                            data: {
                                                id: id,
                                                offset: (page - 1) * 5
                                            },
                                            dataType: "html",
                                            success: function(resp) {
                                                content.find(".content").html($(resp)).updateUi()
                                                bindPaginator();
                                            }
                                        })
                                    }
                                }
                            }
                            bindPaginator();
                        }
                    }
                });
            });
        },
        imageVideo: function(panels) {
            panels.find(".submit-button").on("variation-active-change", function(evt, checked) {
                var container = $(this).parents(".form-section-container").find(".overlay-panel");
                if(checked) {
                    container.removeClass("disabled");
                } else {
                    container.addClass("disabled");
                }
            })
            panels.find("form.edit-popup-form").each(function() {
                var panel = $(this);
                if(panel.is(".image-form")) {
                    var imageData = panel.find(".image-thumb");
                    var imageQueue
                    panel.find(".submit-button").on("variation-active-change", function(evt, checked) {
                        if(checked) {
                            panel.find(".multiple-image-queue").append(imageQueue);
                            imageData.detach();
                        } else {
                            imageQueue = panel.find(".file-selection-queue");
                            imageQueue.detach();
                            panel.find(".product-image-wrapper").append(imageData);
                            if(panel.is(".enable")) {
                                panel.find("input[name=imageId]").each(function() {
                                    $("<input type='hidden' name='remove-images' value='" + $(this).val() + "'>").appendTo(panel)
                                })
                                panel.find("button.submit-button").removeAttr("disabled").trigger("click");
                            }
                        }
                    })
                    app.editProduct.tabInitFunctions.image(panel, panels, "enterpriseVariation/editImage");
                } else if(panel.is(".video-form")) {
                    var videoData = panel.find(".image-thumb");
                    var videoQueue
                    panel.find(".submit-button").on("variation-active-change", function(evt, checked) {
                        if(checked) {
                            panel.find(".multiple-video-queue").append(videoQueue);
                            videoData.detach();
                        } else {
                            videoQueue = panel.find(".file-selection-queue");
                            videoQueue.detach();
                            panel.find(".product-video-wrapper").append(videoData);
                            if(panel.is(".enable")) {
                                panel.find("input[name=videoId]").each(function() {
                                    $("<input type='hidden' name='remove-videos' value='" + $(this).val() + "'>").appendTo(panel)
                                })
                                panel.find("button.submit-button").removeAttr("disabled").trigger("click");
                            }
                        }
                    })
                    app.editProduct.tabInitFunctions.video(panel);
                } else if(panel.is(".downloadable-spec-form")) {
                    var specBlock = panel.find(".spec-file-block");
                    var spec = specBlock.children();
                    panel.find(".submit-button").on("variation-active-change", function(evt, checked) {
                        if(checked) {
                            spec.detach();
                        } else {
                            specBlock.append(spec);
                            if(panel.is(".enable")) {
                                $("<input type='hidden' name='remove_spec' value='true'>").appendTo(panel);
                                panel.find("button.submit-button").removeAttr("disabled").trigger("click");
                            }
                        }
                    })
                    app.editProduct.tabInitFunctions.downloadableSpec(panel, panels);
                }
            })
        },
        advanced: function(panel) {
            panel.find(".variation-metatag").on("variation-active-change", function(evt, checked) {
                var container = $(this).find(".overlay-panel");
                if(checked) {
                    container.removeClass("disabled");
                } else {
                    container.addClass("disabled");
                }
            })
            bm.metaTagEditor(panel);
        },
        productFile: function(panel) {
            app.global_event.on("product-update", function() {
                panel.reload()
            });
            var productFileBlock = panel.find(".product-file-block");
            var productFile = productFileBlock.children();
            function attachRemoveEvent(block) {
                block.find(".tool-icon.remove").click(function() {
                    $("<input type='hidden' name='fileRemoved' value='true'>").appendTo(panel.find("form"))
                    block.children().remove()
                    panel.setDirty()
                })
            }
            panel.find(".variation-product-file").on("variation-active-change", function(evt, checked) {
                var container = $(this).find(".overlay-panel");
                if(checked) {
                    productFile.detach();
                    container.removeClass("disabled");
                } else {
                    productFileBlock.append(productFile);
                    $("<input type='hidden' name='fileRemoved' value='true'>").appendTo(panel.find("form"))
                    panel.find("button.submit-button").removeAttr("disabled").trigger("click");
                    container.addClass("disabled");
                }
            });
            attachRemoveEvent(productFileBlock);
        }
    }

    _ev.onContentLoad = function(data) {
        var _self = this;
        data.panel.find("form").form({
            ajax: {
                success: function() {
                    app.global_event.trigger("enterprise-product-update", [_self.variation.pId]);
                    data.panel.reload();
                    data.panel.clearDirty();
                }
            }
        });
        if(typeof app.tabs.editEnterpriseVariation.tabInitFunctions[data.index] == "function"){
            app.tabs.editEnterpriseVariation.tabInitFunctions[data.index].call(this, data.panel);
        }
    }
})();

_ev.attachEditor = function() {
    var _self = this;
    _self.body.on("change", "input.active-check", function() {
        var $this = $(this);
        var panel = $this.parents(".bmui-tab-panel");
        var fields = $this.prevAll("input, select, textarea, .image-reference, .rteditor-wrap");
        if(fields.is(".rteditor-wrap")) {
            fields = fields.find("textarea").first();
        }
        var depends = panel.find("."+$this.attr("disable-also"));
        fields.add(depends).each(function() {
            var field = $(this)
            var checked = $this.is(":checked");
            if(checked) {
                field.removeAttr("disabled");
            } else {
                field.attr("disabled", "disabled");
            }
            if(field.is(".overlay-panel.auto-change")) {
                field.toggleClass("disabled")
            }
            if(field.is("select")) {
                field.chosen("update");
            }
            field.trigger("variation-active-change", [checked])
        })
    })
}