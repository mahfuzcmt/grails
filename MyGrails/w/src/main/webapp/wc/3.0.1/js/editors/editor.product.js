app.editProduct = function(appTab, product) {
    this.itemTab = appTab;
    this.product = product;
    app.editProduct._super.constructor.call(this, arguments);
};

(function(){
    var _ep = app.editProduct.inherit(app.MultiTab);
    var _super = app.editProduct._super;
    _ep.changeHeader = false;

    function attachEvents() {
        var _self = this;
        this.on_global(["tax-profile-create", "tax-profile-update", "tax-profile-delete"], function(){
            if(_self.tabs["price-quantity"]) {
                bm.updateDomainSelector(_self.tabs["price-quantity"].find("select[name='taxProfile']"), "webcommerce.TaxProfile");
            }
        });

        this.on_global(["category-create","category-update", "category-delete"], function(){
            bm.updateCategorySelector(_self.body.find(".category-selector"), "webcommerce.Category");
        });
    }

    _ep.init = function() {
        var _self = this;
        _self.itemTab.renderCreatePanel(app.baseUrl + "productAdmin/loadProductEditor", $.i18n.prop("edit.product"), _self.product.name, {id: _self.product.id}, {
            createPanelTemplate: $('<div class="embedded-edit-form-panel create-panel fade-in-up"><div class="header"><span class="header-title"></span><span class="toolbar toolbar-right"><span class="tool-group toolbar-btn save save-all">' + $.i18n.prop("save.all")+ '</span><span class="tool-group toolbar-btn cancel">' + $.i18n.prop("cancel") + '</span></span></div><div class="body"></div></div>'),
            ajax: {
                show_success_status: false
            },
            submit_n_cancle: false,
            scrollable: false,
            content_loaded: function (template) {
                _self.body = this;
                _self.header = this.find(".header");
                _super.init.apply(_self, arguments);
                attachEvents.call(_self);
            }
        });
    }

    function bindCustomerSelection(form, selector, customerField, groupField) {
        form.find(selector).on("click", function(){
            bm.customerAndGroupSelectionPopup(form, {
                customerField: customerField,
                groupField: groupField
            })
        })
    }

    app.editProduct.tabInitFunctions = {
        basic: function(panel){
            var _form = panel.find("form");
            bindCustomerSelection(_form, ".select-customer", "customer", "customerGroup")
            var newId = bm.getUUID();
            panel.find("#prodAvailFrom").attr("id", newId);
            panel.find("#prodAvailTo").attr("validation", "skip@if{self::hidden} either_required[" + newId + "," + $.i18n.prop("from") + " ," + $.i18n.prop("to") + "]");
        },
        advanced: function(panel) {
            bm.metaTagEditor(panel)
        },
        combined: function(panel){
            var select = panel.find("select.product-combined-select");
            var sortPanel = panel.find(".sort-panel");
            select.on("change",function(){
                sortPanel.empty();
                var vals = select.val()
                $.each(vals, function(idx, key){
                    var name = select.find("[value='" + key + "']").text(),
                        sortElm = $("<div class='form-row'><label>" + name + "</label><input type='hidden' name='products' value='" + key + "'><input type='text' name='alias'></div> ");
                    sortPanel.append(sortElm);
                })
            })
            sortPanel.sortable();
        },
        "price-quantity": function(panel) {
            var _form = panel.find("form");
            bindCustomerSelection(_form, ".restrict-price-except-select-customer", "restrictPriceExceptCustomer", "restrictPriceExceptCustomerGroup")
            bindCustomerSelection(_form, ".restrict-purchase-except-select-customer", "restrictPurchaseExceptCustomer", "restrictPurchaseExceptCustomerGroup")
            panel.find(".view-inventory-history").on("click", function() {
                var id = $(this).attr("product-id");
                var url = app.baseUrl + "productAdmin/inventoryHistory";
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
                                                bindPaginator()
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
        "image-video": function(panel, panel_obj) {
            var _self = this
            panel.save = function(onFinish) {
                panel.reload = function() {}
                var forms = panel.find("form.edit-popup-form")
                var length = forms.length
                bm.iterate(forms, function(handle, i) {
                    var $this = $(this);
                    function success() {
                        if(i + 1 == length) {
                            panel_obj.clearDirty()
                            onFinish()
                        } else {
                            handle.next()
                        }
                    }
                    if($this.is(".submitting")) {
                        success()
                        return;
                    }
                    $this.form("submit", {
                        ajax: {
                            show_success_status: false
                        },
                        success: success,
                        error: function() {
                            _self.setActiveTab("image-video")
                        },
                        invalid: function() {
                            _self.setActiveTab("image-video")
                            panel.find("form").valid("position")
                        }
                    })
                })
            }
            panel.find("form.edit-popup-form").each(function() {
                var form = $(this);
                if(form.is(".image-form")) {
                    app.editProduct.tabInitFunctions.image.call(this, form, panel_obj);
                } else if(form.is(".video-form")) {
                    app.editProduct.tabInitFunctions.video.call(this, form);
                } else if(form.is(".downloadable-spec-form")) {
                    app.editProduct.tabInitFunctions.downloadableSpec.call(this, form, panel);
                }
            })
        },
        image: function(panel, panel_obj, popupUrl) {
            var imgMenu = [
                {
                    text: $.i18n.prop("alt.text"),
                    ui_class: "edit",
                    action: "altText"
                },
                {
                    text: $.i18n.prop("remove"),
                    ui_class: "delete",
                    action: "remove"
                }
            ];
            var imageList = panel.find(".product-image-container");
            function removeImage(entity) {
                var imageId = entity.attr("data-id");
                $("<input type='hidden' name='remove-images' value='" + imageId + "'>").appendTo(entity.closest("form"))
                entity.trigger("change").remove();
                imageList.scrollbar("update")
            }
            function editAltTag(entity) {
                var data = {}
                data.cache = entity.attr("update_cache");
                data.id = entity.attr("data-id");
                $.each($(".product-image-wrapper").find("input[name='altText']"),function() {
                    if($(this).attr("img-id") == data.id) {
                        data.altText = $(this).val()
                    }
                })
                bm.editPopup(app.baseUrl + (popupUrl ? popupUrl : "productAdmin/editImage"), $.i18n.prop("edit.alternative.text"), "", data, {
                    width : 450,
                    beforeSubmit: function(form, extraData, popup){
                        var properties = form.serializeArray();
                        var cache = {}
                        $.each(properties, function(ind, val) {
                            cache[val.name] = val.value;
                        });
                        $.each($(".product-image-wrapper").find("input[name='altText']"),function() {
                            if($(this).attr("img-id") == data.id) {
                                $(this).remove()
                            }
                        })
                        $.each($(".product-image-wrapper").find("input[name='altTextId']"), function() {
                            if(this.value.equals(data.id)){
                                $(this).remove()
                            }
                        })
                        var prop = $('<input type="hidden" img-id=' + cache.id + ' name="altText" value="' + cache.altText + '">' +
                        '<input type="hidden" name="altTextId" value="' + cache.id + '">')
                        $(".product-image-wrapper").append(prop)
                        popup.close();
                        panel_obj.setDirty();
                        return false;
                    }
                })
            }
            function onActionClick(action, entity) {
                entity = entity.parent();
                switch (action){
                    case "remove":
                        removeImage(entity);
                        break;
                    case "altText":
                        editAltTag(entity);
                        break;
                }
            }
            bm.menu(imgMenu, panel, ".float-menu-navigator", onActionClick, "click", ["left top+1", "right+20 top+5"]);
            imageList.find(".remove").on("click", function() {
                var entity = $(this).closest(".image-thumb")
                removeImage(entity);
            });
            imageList = imageList.find(".one-line-scroll-content").scrollbar({
                show_vertical: false,
                show_horizontal: true,
                use_bar: false,
                visible_on: "auto",
                horizontal: {
                    handle: {
                        left: imageList.find(".left-scroller"),
                        right: imageList.find(".right-scroller")
                    }
                }
            });
            imageList.hover(function() {
                imageList.scrollbar("update")
            })
            $(window).on("resize." + this.id, function() {
                imageList.scrollbar("update")
            })
            imageList.sortable({
                axis: "x",
                placeholder: true,
                handle: ".image-thumb",
                stop: function() {
                    imageList.trigger("change")
                },
                sort: function() {
                    panel_obj.setDirty()
                }
            })
        },
        video: function(panel) {
            var videoMenu = [
                {
                    text: $.i18n.prop("remove"),
                    ui_class: "delete",
                    action: "remove"
                }
            ];
            var videoList = panel.find(".product-video-container");
            function removeVideo(entity) {
                var videoId = entity.attr("data-id");
                $("<input type='hidden' name='remove-videos' value='" + videoId + "'>").appendTo(entity.closest("form")) ;
                entity.trigger("change").remove();
                videoList.scrollbar("update")
            }

            videoList = videoList.find(".one-line-scroll-content").scrollbar({
                show_vertical: false,
                show_horizontal: true,
                use_bar: false,
                visible_on: "auto",
                horizontal: {
                    handle: {
                        left: videoList.find(".left-scroller"),
                        right: videoList.find(".right-scroller")
                    }
                }
            });
            $(window).on("resize." + this.id, function() {
                videoList.scrollbar("update")
            })
            videoList.find(".remove").on("click", function(){
                var entity = $(this).closest(".image-thumb");
                removeVideo(entity);
            })
        },
        downloadableSpec: function(panel, panel_obj) {
            var fileBlock = panel.find(".spec-file-block");
            panel.find("input[type=file]").on("file-add", function(event, file) {
                fileBlock = panel.find(".spec-file-block")
                var fileName = file.name
                var itemTemplate = '<div class="spec-file-block"><span class="file #EXTENTION#"><span class="tree-icon"></span></span>' +
                    '<span class="name">#FILENAME#</span><span class="tool-icon remove" file-name="#FILENAME#"></span>' +
                    '</div>';
                if(fileName.length) {
                    var fileName = fileName.split(".")
                    itemTemplate = itemTemplate.replaceAll("#EXTENTION#", fileName[fileName.length-1])
                    fileBlock.replaceWith(itemTemplate.replaceAll("#FILENAME#", fileName.join(".")))
                    panel.find("input[name=remove_spec]").remove()
                    attachRemoveEvent(panel.find(".spec-file-block"))
                }
            });
            attachRemoveEvent(fileBlock)
            function attachRemoveEvent(fileBlock) {
                fileBlock.find(".tool-icon.remove").click(function() {
                    $("<input type='hidden' name='remove_spec' value='true'>").appendTo(panel)
                    fileBlock.children().remove();
                    fileBlock.css('display', 'none');
                    panel_obj.setDirty();
                })
            }
        },
        relatedProducts: function(panel) {
            bm.initProductSelection(panel, "related")
        },
        includedProducts: function(panel) {
            var selector = bm.initProductSelection(panel, "included");
            var isFixed = panel.find("input[name=isFixed]").val();
            var isDownloadable = panel.find("input[name=isDownloadable]").val();
            var _superBeforeLoad = selector.beforeLoadTableContent;
            selector.beforeLoadTableContent = function(params) {
                _superBeforeLoad(params);
                params.notCombined = true;
                params.isDownloadable = isDownloadable
            };
            selector.onNewSelection = function(row) {
                var td = row.find("td").first();
                var labelDom = '<div class="form-row"><label>'+ $.i18n.prop("label") +'</label><input type="text" class="medium" name="label" value="'+ td.text()+'"></div>'
                var quantityDom = '<div class="form-row"><label>'+ $.i18n.prop("quantity")+'</label> <input type="text" name="quantity" class="tiny spinner" min="1" value="1" validation="number" restric="numeric"></div>'
                var priceDom =  '<div class="form-row"><label>'+ $.i18n.prop("price")+'</label> <input type="text" validation="number" name="price" restrict="decimal"></div>'
                td.text("")
                td.append(labelDom)
                if(isDownloadable != "true") {
                    td.append(quantityDom);
                }
                if(isFixed != "true") {
                   priceDom = $(priceDom)
                   row.find("td").first().append(priceDom);
                   row.find("input[name=price]").decimal();
                   row.find("input[name=quantity]").numeric();
                }
                row.updateUi()
            }
            selector.resetRightPanel = function(resp, rightPanelSelectedTd, callback) {
                var respTds = resp.find("td.actions-column");
                rightPanelSelectedTd.each(function() {
                    var td = $(this);
                    var respTd = respTds.find("input[value="+td.attr("item")+"]").parent();
                    if(!respTd.length) {
                        td.parent().remove();
                    }
                })
                if(callback) {
                    callback();
                }
            }
        },
        productFile: function(panel) {
            app.global_event.on("product-update", function() {
                panel.reload()
            });
            var fileBlock = panel.find(".product-file-block");
            var itemTemplate = '<div class="product-file-block"><span class="file #EXTENTION#"><span class="tree-icon"></span></span>' +
                '<span class="name">#FILENAME#</span><span class="tool-icon remove" file-name="#FILENAME#"></span></div>';

            function attachRemoveEvent(block) {
                block.find(".tool-icon.remove").click(function() {
                    $("<input type='hidden' name='fileRemoved' value='true'>").appendTo(panel.find("form"))
                    block.children().remove()
                    panel.setDirty()
                })
            }
            panel.find("input[type=file]").on("change", function() {
                var fileName = $(this).val().split('/').pop().split('\\').pop()
                if(fileName.length) {
                    var fileNameSplitted = fileName.split(".")
                    var extension = fileNameSplitted[fileNameSplitted.length-1]
                    var newFileBlock = itemTemplate.replaceAll("#EXTENTION#", extension).replaceAll("#FILENAME#", fileName)
                    newFileBlock = $(newFileBlock)
                    attachRemoveEvent(newFileBlock)
                    fileBlock.replaceWith(newFileBlock)
                    fileBlock = newFileBlock
                    panel.find("input[name=fileRemoved]").remove()
                }
            });
            attachRemoveEvent(fileBlock)
        },
        multiStore: function(panel) {
            var _self = this
            var _formMultiStore = panel.find(".product-properties-and-store")
            var bindInlineEditable = function(editable) {
                editable.each(function () {
                    $(this).editable().on("inlinechange", function () {
                        var span = this.jqObject;
                        span.next(".column-name").val(span.text()).trigger("change");
                    });
                })
            }

            bindInlineEditable(panel.find(".inline-editable"))

            /*_formMultiStore.form("prop", "preSubmit", function() {
                var inputs = _formMultiStore.find("input")
                inputs.each(function () {
                    var _input = $(this)
                })
            });*/

        }
    }

    _ep.onContentLoad = function(data, panel_obj) {
        var _self = this;
        data.panel.find("form").form({
            ajax: {
                success: function() {
                    app.global_event.trigger("product-update", [_self.product.id]);
                    if (data.index == "price-quantity" || data.index == "image-video") {
                        if(data.index == "image-video") {
                            app.global_event.trigger("product-image-video-update", [_self.product.id]);
                        }
                        data.panel.reload();
                    }
                    data.panel.clearDirty();
                }
            }
        });
        if(typeof app.editProduct.tabInitFunctions[data.index] == "function") {
            app.editProduct.tabInitFunctions[data.index].call(this, data.panel, panel_obj);
            if (data.index == "price-quantity") {
                this.on_global("variation-status-update", function() {
                    data.panel.reload();
                })
            }
        }
    }
})();



