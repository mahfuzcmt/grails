
$(function() {
    window.compare_product_global_js_loaded = true;
    var addedButton = '<span class="remove-from-compare-button button" product-id="#PID#" title="'+$.i18n.prop("remove.from.compare")+'">'+ $.i18n.prop("remove.from.compare") + '</span>';
    var compareButton = '<span class="add-to-compare-button button" product-id="#PID#" title="'+$.i18n.prop("add.to.compare")+'">'+ $.i18n.prop("add.to.compare") + '</span>';
    function renderPopup (content) {
       renderGlobalSitePopup(content, {
           clazz: "add-to-compare-popup",
           auto_close: app.config.compare_product_popup_animation_clazz,
           width: 500
       })
    }

    function changeButton(id, onAction) {
        var button, selector, flag;
        if(onAction == 'add') {
            button = addedButton;
            selector = ".add-to-compare-button";
            flag = false
        } else {
            button = compareButton;
            selector = ".remove-from-compare-button";
            flag = true
        }
        button = button.replace("#PID#", id);
        selector = selector + "[product-id="+ id +"]";
        button = $(button);
        $(selector).each(function(){
            var clone = button.clone();
            $(this).replaceWith(clone);
            if(flag == true) {
                bindButtonEvent.call(clone);
            } else {
                bindRemoveButtonEvent.call(clone)
            }
        })

    }

   function bindButtonEvent() {
       this.on("click", function() {
           var productId = $(this).attr("product-id");
           bm.ajax({
               url: app.baseUrl + "compareProduct/addToCompare",
               data: {productId: productId},
               success: function(resp) {
                   var html = $(resp.html)
                   if(!$(".widget-compareProduct").length) {
                       renderPopup(html);
                   }
                   changeButton(productId, "add");
                   app.global_event.trigger("product-compare-update");
               },
               error: function(a, b, resp) {
                   if(resp.html) {
                       renderPopup($(resp.html))
                   }
               }
           })
       })
    }

    function bindRemoveButtonEvent() {
        var _this = this;
        var productId = _this.attr("product-id");
        _this.on("click", function() {
            bm.ajax({
                url: app.baseUrl + "compareProduct/removeFromCompare",
                data: {productId: productId},
                success: function() {
                    changeButton(productId, "remove");
                    app.global_event.trigger("product-compare-update");
                }
            })
        })
    }

    var bindAddToCompareClickEvent = function(prdBlock) {
        prdBlock.find(".add-to-compare-button").each(function() {
            var _this = $(this);
            bindButtonEvent.call(_this);
        })
    }

    bindAddToCompareClickEvent($(this));

    var bindRemoveFromCompareClickEvent = function(prdBlock) {
        prdBlock.find(".remove-from-compare-button").each(function() {
            var _this = $(this);
            bindRemoveButtonEvent.call(_this);
        })
    }
    bindRemoveFromCompareClickEvent($(this));

    app.global_event.on("new-product-block-added", function(ev, prdBlock) {
        bindAddToCompareClickEvent(prdBlock)
        bindRemoveFromCompareClickEvent(prdBlock)
    })

    /*compare widget*/
    function initWidget() {
        function bindEvents (widgetContent) {
            widgetContent.find(".close-icon").on("click", function() {
                var $this = $(this);
                var productId = $this.attr("product-id")
                bm.ajax({
                    url: app.baseUrl + "compareProduct/removeFromCompare",
                    data: { productId: productId},
                    success: function() {
                        app.global_event.trigger("product-compare-update");
                        changeButton(productId, "remove")
                    }
                })
            });

            widgetContent.find(".clear-all-btn.button").on("click", function() {
                bm.ajax({
                    url: app.baseUrl + "compareProduct/removeCompare",
                    success: function() {
                        app.global_event.trigger("product-compare-update");
                        $(".remove-from-compare-button").each(function() {
                            var $this = $(this);
                            changeButton($this.attr("product-id"), "remove");
                        })
                    }
                })
            });
        }
        $(".widget.widget-compareProduct").each(function(){
            var _this = $(this);
            bindEvents(_this);
            var id = _this.attr("widget-id");
            var domId = _this.attr("id")
            app.global_event.bind("product-compare-update", function(){
                bm.ajax({
                    data: {id: id},
                    dataType: 'html',
                    url : app.baseUrl + "compareProduct/widget",
                    success: function (resp){
                        _this.replaceWith(resp);
                        _this = $("#" + domId);
                        bindEvents(_this);
                    }
                })
            })
        })
    }
    initWidget();
    window.bindAddToCompareClickEvent = bindAddToCompareClickEvent;
    window.bindRemoveFromCompareClickEvent = bindRemoveFromCompareClickEvent;
})