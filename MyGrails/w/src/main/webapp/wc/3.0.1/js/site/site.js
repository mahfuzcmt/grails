$.extend(app, {
    global_event: $({}),
    widget: {}
});

var site = {
    width : $(window).width(),
    height : $(window).height(),
    registered_hooks: {},
    hook: {
        register: function(name, func) {
            var callbacks = site.registered_hooks[name];
            if(!callbacks) {
                callbacks = site.registered_hooks[name] = []
            }
            callbacks.push(func)
        },
        fire: function(name, response, params) {
            var callbacks = site.registered_hooks[name];
            if(callbacks) {
                callbacks.every(function() {
                    response = this.apply(null, [response].concat(params))
                })
            }
        }
    },
    fncts: {}
};
var responsiveMenuManager = {
    closeFunction: null,
    open: function(opener, closer) {
        if(this.closeFunction) {
            this.closeFunction()
        }
        opener();
        this.closeFunction = closer;
    },
    close: function() {
        if(this.closeFunction) {
            this.closeFunction()
        }
        this.closeFunction = null
    }
};
var renderSitePopup, renderGlobalSitePopup, renderAddToCartPopup, renderInfoChooserPopup;

function collectCartData(container, data, forPriceOnly) {
    var included;
    var combination;
    if(container.find(".included-products-container[combination-flexible]").length) {
        included = {};
        container.find(".included-quantity-selector").each(function() {
            var spin = $(this);
            var id = spin.attr("pId");
            included[id] = spin.val()
        });
        included = included ? JSON.stringify(included) : null
    }
    data.included = included;
    if(container.find(".param-dump").length) {
        $.extend(data, container.find(".param-dump").serializeObject())
    }
    site.hook.fire("prepareAddCartData", data, [container, data.productId, data.quantity, forPriceOnly])
}

function addProductInCart() {
    var _this = $(this);
    var carts = _this.config("cart");
    var container = _this.closest(".page-content, .popup, .product-block, .add-cart-info-block");
    var quantity = carts["quantity"];
    var productId = _this.attr("product-id");
    var storeId = _this.attr("storeId");
    var data = {productId: productId, quantity: quantity};
    storeId ? data.storeId = storeId : ""
    collectCartData(container, data, false);
    bm.ajax({
        url: app.baseUrl + "cart/add",
        data: data,
        type: 'post',
        success: function(resp) {
            var content = $(resp.html);
            if(resp.status == 'added') {
                renderAddToCartPopup(content);
            } else if(resp.status == 'redirect-to') {
                window.location.href = app.baseUrl + "product/" + resp.url
            } else if(resp.status == 'incomplete-info') {
                renderInfoChooserPopup(productId, content);
            }
        }
    });
}

function renderMessage(panel, message, type, time, insertMethod) {
    panel = panel ? panel : $('[section=body]')
    var msgSpan = $('<div class="message-block ' + type + '">' + message + '</div>');
    insertMethod = insertMethod ? insertMethod : "prepend";
    panel[insertMethod](msgSpan);
    msgSpan.scrollHere();
    setTimeout(function(){
        msgSpan.remove();
    }, time || 5000);
}

///////////////// CAPTCHA ////////////

app.captchaUtil = (function() {
    var recaptchaWidgets = [];
    return {
        reloadCaptcha: function(captchaType) {
            if(captchaType == 'simple_captcha') {
                var src = $(".simple-captcha img")[0].src;
                var path = bm.path(src);
                path.query._ = new Date().getTime();
                src = path.full();
                $(".simple-captcha img").attr("src", src);
                $(".simple_captcha_input_field > input[name=captcha]").val("");
            } else if(captchaType == 're_captcha') {
                recaptchaWidgets.forEach(function (value) {
                    if( value != undefined )
                        grecaptcha.reset(value);
                });
            }
        },
        bindReCaptcha: function (container, ifNotReady) {
            bm.onReady(window, "grecaptcha", {
                not: ifNotReady,
                ready: function () {
                    grecaptcha.ready(function () {
                        var captcha = grecaptcha.render(container.attr('id'), container.config("config"));
                        recaptchaWidgets.push(captcha)
                    });
                }
            });
        }
    }
})();

$(function() {
    $(".valid-verify-form").form({
        text_change_on_submit: false
    });
    renderSitePopup = function(config, popup) {
        var content = config.el || $();
        config = $.extend({
            is_fixed: true,
            is_always_up: true,
        }, config);
        if(config.animation_clazz == undefined) {
            config.animation_clazz = app.config.site_popup_animation_clazz
        }
        content.find(".close-popup, .continue-shopping-btn, .close-btn").click(function () {
           popup.close();
        });
        if(!popup|| popup.is_closed) {
            popup = new POPUP(config)
        } else {
            popup.setContent(content.children())
        }
        return popup
    };

    renderGlobalSitePopup = function(content, config)  {
        config = $.extend({
            auto_close: 40000,
            el: content
        }, config);
        site.global_single_popup = renderSitePopup(config, site.global_single_popup)
        return site.global_single_popup
    };

    renderInfoChooserPopup = function(productId, content) {
        app.global_event.trigger("initialize-info-chose-popup", [productId, content]);
        renderGlobalSitePopup(content, {clazz: "add-cart-information-popup"});
        var combinationContainer = content.find(".included-products-container");
        if(combinationContainer.length) {
            initializeCombination(productId, combinationContainer)
        }
        var storeSelection = content.find(".store-selection")
        if(storeSelection.length) {
            content.find(".add-to-cart-button").attr("storeId", storeSelection.find(':selected').val())
            storeSelection.on("change", function () {
                content.find(".add-to-cart-button").attr("storeId", $(this).find(':selected').val())
            })
        }

        content.find(".add-to-cart-button").click(function() {
            if($(this).is(".disabled")) {
                return;
            }
            addProductInCart.call(this)
        })
    };

    renderAddToCartPopup = function(content) {
        var popup = renderGlobalSitePopup(content, {clazz: "add-to-cart-popup", animation_clazz: app.config.add_to_cart_popup_animation_clazz, auto_close: false});
        content = popup.el
        bindProductQuantitySpinner(content);
        var messageBlocks = content.find(".message-block");
        if(messageBlocks.length) {
            setTimeout(function() {
                messageBlocks.remove()
            }, 5000)
        }
        content.find(".product-quantity-selector").ichange(600, function() {
            if(!this.value) { return }
            var itemId = content.find("[name=itemId]").val();
            bm.ajax({
                url: app.baseUrl + "cart/updateQuantity",
                data: {quantity: this.value, cartItemId: itemId},
                success: function(resp) {
                    if(resp.message) renderMessage(content.find(".body"), resp.message, resp.status)
                    $.each(resp.cartData, function(key, value) {
                        content.find("." + key.minusCase()).text(value)
                    });
                    $.each(resp.cartItemsData[+itemId], function(key, value) {
                        content.find("." + key.minusCase()).text(value)
                    });
                    app.global_event.trigger("update-cart");
                },
                error: function(status, xht, resp) {
                    renderMessage(content.find(".body"), resp.message, resp.status)
                }
            });
        }).numeric();
        app.global_event.trigger("update-cart");
    };

    bindAddToCartClickEvent = function(container) {
        var cartButton = $(".add-to-cart-button");
        if(container != null) {
            cartButton = container.find(".add-to-cart-button")
        }
        cartButton.click(function () {
            if($(this).is(".disabled")) {
                return;
            }
            addProductInCart.call(this)
        });
    };

    bindAddToCartClickEvent();

    bindProductQuantitySpinner = function(container) {
        var spinObj = $(".product-quantity-selector.text-type");
        if(container != null) {
            spinObj = container.find(".product-quantity-selector.text-type")
        }
        spinObj.each(function() {
            var spin = $(this);
            var data = spin.config("spin");
            data.et_data = {
                up: {clazz: "et_cartp_increase_quantity", attr: {'et-category': 'button'}},
                down: {clazz: "et_cartp_decrease_quantity", attr: {'et-category': 'button'}}
            };
            spin.stepper(data)
        })
    };

    bindProductQuantitySpinner();

    bm.initCountryChangeHandler($(".country-selector-row select"));

    $(".widget-currency select").on("change", function(){
        var _self = $(this);
        bm.ajax({
            url: app.baseUrl + "app/changeCurrency",
            data: {currencyId: _self.val()},
            dataType: "json",
            success: function(resp){
               location.reload()
            }
        })
    });

    $(".widget-newsletter > .newsletter.inplace, .newsletter.subscribe").form("prop", "ajax", {
        url: app.baseUrl + "shop/newsletterSubscription",
        success: function(resp, b, c, form) {
            form.find("input[type=text]").val("");
            setMessage(resp.message, true, form);
        }, error: function(a, b, resp, form) {
            setMessage(resp.message, false, form);
        }
    });

    $(".newsletter.unsubscribe").form("prop", "ajax", {
        url: app.baseUrl + "shop/newsletterUnsubscription",
        success: function(resp, b, c, form) {
            var sid = form.find("input[name=sid]").val();
            var page = form.parent();
            var messsage = $(resp.html);
            messsage.find(".link.resubscribe").on("click", function() {
                bm.ajax({
                    url: app.baseUrl + "shop/newsletterResubscription",
                    data: {sid: sid, immediate: true},
                    success: function(resp) {
                        setMessage(resp.message, true, messsage);
                    },
                    error: function(a, b, resp) {
                        setMessage(resp.message, false, messsage);
                    }
                })
            });
            form.replaceWith(messsage);
        }, error: function(a, b, resp, form) {
            setMessage(resp.message, false, form);
        }
    });

    ///////////Order Comment/////////////////

    $(".gust-oder-comment-page .oder-comment-form").form({
        ajax: {
            success: function(resp, b, c, form) {
                var commentArea = $(".gust-oder-comment-page .comment-area");
                var message = $("<div class='comment-row customer'><span class='name'>" +
                    $.i18n.prop(resp.customerName ? resp.customerName : 'customer') + " </span>" +
                    "<span class='date-time-row'><span class='date-time'>" + resp.date + "</span>" +
                    "<span class='show-comment'>" + resp.message + "</span></span></div>");
                commentArea.append(message);
                form.find("textarea").val("");
                commentArea.animate({scrollTop : commentArea[0].scrollHeight}, "slow");
            },
            error: function(xhr, status, resp, form) {
                var commentArea = $(".gust-oder-comment-page .comment-area");
                setMessage(resp.message, false, commentArea);
            }
        }
    });

    function setMessage(message, success, container) {
        var messageBlock = container.find(".message-block");
        if(!messageBlock.length) {
            messageBlock = $("<div class='message-block'></div>");
            container.prepend(messageBlock)
        }
        if(success) {
            messageBlock.removeClass("error-message").addClass("info-message").html(message)
        } else {
            messageBlock.removeClass("info-message").addClass("error-message").html(message)
        }
        messageBlock.scrollHere();
        setTimeout(function() {
            messageBlock.remove()
        }, app.config.newsletter_subscription_message_display_time)
    }

    $(".widget-newsletter > .submit-button.page-button").on("click", function() {
        location.href = app.baseUrl + "subscription"
    });

    initializeProductWidget();
    initCartWidget();
    initializePagination();

    var serverMessages = $(".server-message");
    if(serverMessages.length) {
        serverMessages.scrollHere();
        setTimeout(function() {
            serverMessages.remove()
        }, app.config.server_message_display_time)
    }

    //commons ui change
    $(".bmui-tab").tabify();
    $(".product-block .product-quantity-selector.text-type").on("change spinchange", function () {
        var priceBlock = $(this).parents(".price-n-cart");
        var cartButton = priceBlock.find(".add-to-cart-button");
        cartButton.config("cart", {quantity: this.value})
    });

    $(this).find(".date-picker").each(function () {
        var field = $(this);
        var startDate = field.attr('date-range-start') ? field.attr('date-range-start') : false;
        var endDate = field.attr('date-range-end') ? field.attr('date-range-end') : false;
        var direction = ( startDate || endDate ) ? [startDate, endDate] : (field.attr("no-previous") ? true : (field.attr("no-next") ? false : undefined));
        field.date({
            direction: direction,
            show_select_today: false,
            lang_clear_date: $.i18n.prop("clear")
        })
    });

    $(this).find(".date-time-picker").each(function () {
        var field = $(this);
        var startDate = field.attr('date-range-start') ? field.attr('date-range-start') : false;
        var endDate = field.attr('date-range-end') ? field.attr('date-range-end') : false;
        var direction = ( startDate || endDate ) ? [startDate, endDate] : (field.attr("no-previous") ? true : (field.attr("no-next") ? false : undefined));
        var timeFormat = field.attr('time-format') == '12' ? "hh:mm:tt" : "HH:mm";
        field.date({
            direction: direction,
            show_select_today: false,
            lang_clear_date: $.i18n.prop("clear"),
            time: timeFormat
        })
    });

    $('.recaptcha-container').each(function () {
        app.captchaUtil.bindReCaptcha($(this))
    })
});


/////////////////Pagination //////////////

function initializePagination() {
    var paginator = $("paginator");
    if(paginator.length == 0) {
        return
    }
    paginator.each(function() {
        $(this).paginator({
            showPages: window.innerWidth > 800 ? app.config.number_block_in_paginator_count : app.config.number_block_in_paginator_count_800
        })
    });
    $(".pagination").each(function() {
        var _this = $(this);
        var paginatorObj = _this.obj();
        paginatorObj.onPageClick = function() {
            window.location.href = getUrl($(this))
        }
    });
    var parPageCount = $(".per-page-count");
    parPageCount.change(function() {
        var paginator = $(this).siblings(".pagination");
        paginator.obj().setItemsPerPage(+this.value);
        window.location.href = getUrl(paginator);
    });
    function getUrl(pagination) {
        var paginatorObj =  pagination.data("wcuiPagination");
        var urlprefix = pagination.data("urlprefix") + "-";
        var url = bm.path(document.location.href);
        url.query[urlprefix + "offset"] = ((paginatorObj.currentPage - 1) * paginatorObj.itemsPerPage);
        url.query[urlprefix + "max"] = (paginatorObj.all ? "-1" : paginatorObj.itemsPerPage);
        return url.full();
    }
}

///////////////// ProductWidget ////////////

function initializeProductWidget() {
    var sortOrder = $(".product-sorting");
    sortOrder.change(function() {
        var $this= $(this), urlPrefix = $this.attr("urlprefix");
        var url = bm.path(document.location.href);
        url.query[urlPrefix + "-sort"] = $this.val();
        window.location.href = url.full();
    });

    $(".scrollable-view").each(function() {
        var _this = $(this);
        var head = _this.find(">.header");
        _this.find(".content").scrollbar({
            show_vertical: false,
            show_horizontal: true,
            use_bar: false,
            visible_on: "always",
            horizontal: {
                step: "auto",
                handle: {
                    left: head.find(".scroll-left"),
                    right: head.find(".scroll-right")
                }
            }
        })
    });
}

///////////////// Cart Widget ////////////////

function initCartWidget() {
    function initQuickCart (cart) {
        cart.find(".cart-wrapper.quick-cart").click(function() {
            var _this = $(this);
            var quickCartContent = _this.siblings(".quick-cart-content");
            if(quickCartContent.hasClass("show")) {
                return;
            }
            if(quickCartContent.is(":hidden")) {
                quickCartContent.stop(true).slideDown();
                quickCartContent.focus().one("blur", function() {
                    setTimeout(function() {
                        quickCartContent.stop(true).slideUp();
                    }, 200)
                })
            } else {
                quickCartContent.stop(true).slideUp();
            }
        });
        cart.find(".cart-menu-button").on("click", function() {
            var $this = $(this), content = $this.siblings(".content"), quickCart = content.find('.quick-cart-content');
            if(content.hasClass("show")) {
                responsiveMenuManager.close()
            } else {
                responsiveMenuManager.open(function() {
                    $this.addClass("close");
                    content.addClass("show");
                    quickCart.css({display: ""}).addClass("show")
                }, function() {
                    $this.removeClass("close");
                    content.removeClass("show");
                    quickCart.css({display: "none"}).removeClass("show")
                })
            }
        });
    }
    $(".widget-cart").each(function(){
        var _this = $(this);
        initQuickCart(_this);
        var id = _this.attr("widget-id");
        var domId = _this.attr("id");
        app.global_event.bind("update-cart billing-address-change", function(){
            bm.ajax({
                data: {id: id},
                dataType: 'html',
                url : app.baseUrl + "cart/widget",
                success: function (resp){
                    _this.replaceWith(resp);
                    _this = $("#" + domId);
                    initQuickCart(_this);
                }
            })
        })
    })
}
/* +++++++++++++++++++++++ City Selector +++++++++++++++++++++++++++++ */

$(function() {
    bm.initCityValidator($('[name="postCode"]'))
});

/*** Combination ************************/

function updateProductPrice(block, data) {
    var container = block.closest(".page-content, .popup, .product-block, .add-cart-info-block");
    if(container.is(".page-content")) {
        container = container.find(".product-widget.widget-price, .product-widget.widget-variation, .product-widget.widget-addCart")
    }
    collectCartData(container, data, true);
    bm.ajax({
        controller: "productPage",
        action: "priceAndUnavailableMsg",
        data: data,
        success: function(resp) {
            container.find(".current-price .price-amount").text(resp.price.toFixed(2));
            if(resp.expect) {
                container.find(".previous-price .price-amount").text(resp.expect.toFixed(2));
            } else if(resp.sale) {
                container.find(".previous-price .price-amount").text(resp.sale.toFixed(2));
            }
            var total = container.find(".current-price .total-amount");
            if(total.length) {
                var quantity = data.quantity || container.find(".add-to-cart-button").config("cart", "quantity");
                total.text((resp.price * quantity).toFixed(2))
            }
            container.closest('.page-content').find('.widget-variation .product-status').remove();
            container.closest('.page-content').find('.widget-stockMark').replaceWith(resp.stockWidget).show()
        },
        error: function(a, b, resp) {
            container.closest('.page-content').find('.widget-stockMark').hide();
            displayUnavailableStatus(container.closest('.page-content').find('.widget-variation'), resp.message)
        }
    })
}

function displayUnavailableStatus(container, message) {
    container.find(".product-status").remove();
    container.prepend('<span class="product-status message-block error"></span>');
    container.find(".product-status").text(message);
}

function initializeCombination(productId, container) {
    if(container.is(".initialized")) {
        return
    }
    container.change(function(evt) {
        var data = {productId: productId};
        var originalEvent = evt.originalEvent || evt;
        var input = $(originalEvent.target || originalEvent.srcElement);
        var page = container.parents(".page-content, .add-cart-information-popup");
        var buttons = page.find(".widget-addCart .button, .add-to-cart-button");
        var infoRow = input.parents(".info-row");
        if(+input.val() < 1) {
            buttons.addClass("disabled");
            renderMessage(infoRow, $.i18n.prop("minimum.required.quantity", [1]), "error", "5000", "after");
            return;
        } else {
            buttons.removeClass("disabled");
            infoRow.siblings(".message-block.error").remove();
        }
        updateProductPrice(container, data)
    });

    container.find(".included-quantity-selector").each(function() {
        var spin = $(this);
        var data = spin.config("spin");
        data.et_data = {
            up: {clazz: "et_pdp_increase_quantity", attr: {'et-category': 'button'}},
            down: {clazz: "et_pdp_decrease_quantity", attr: {'et-category': 'button'}}
        };
        spin.stepper(data)
    });
    container.addClass("initialized")
}

$.extend($.form_care_functions, {
    simple_captcha_reload: function() {
        var _reloadIcon = this.find(".simple-captcha-reload.icon");
        _reloadIcon.click( function() {
            app.captchaUtil.reloadCaptcha('simple_captcha')
        });
    }
});

//////////////////Product Short info View////////////////////

function initializeProductInfoView(productId, content, type, callback) {
    bindProductQuantitySpinner(content);
    content.find(".product-quantity-selector.text-type").on("change spinchange", function () {
        var cartButton = content.find(".widget-addCart .add-to-cart-button");
        cartButton.config("cart", {quantity: this.value});
    });
    var combinationObj = $(".included-products-container");
    if (combinationObj.length) {
        initializeCombination(productId, combinationObj);
    }
    bindAddToCartClickEvent(content);
    if(type == "details") {
        bm.onReady(app, "productWidgets", {
            ready: function() {
                app.productWidgets.init(content, productId)
            },
            not: bm.addScript.bind(undefined, "js/site/product.js", true)
        });
    }
    app.global_event.trigger("after-product-info-view-initialize", [productId, content, type, callback]);
    if(callback) {
        callback()
    }
}

///////////Navigation Widget////////

$(function() {
    var pageBody = $("#webcommander-page");
    function drawerOpen(content) {
        content.addClass("responsive-navigation");
        if(pageBody.hasClass("with-drawer")) {
            pageBody.find("left-container").html(content)
        } else {
            pageBody.children().wrapAll('<div class="page-container"></div>');
            content = $('<div class="left-container"></div>').html(content);
            pageBody.prepend(content);
            setTimeout(function() {
                pageBody.find(".left-container, .page-container").addClass("open");
            }, 100);
            pageBody.addClass("with-drawer")
        }
    }

    function drawerClose(callback) {
        pageBody.find(".left-container, .page-container").removeClass("open");
        setTimeout(function() {
            pageBody.find(".left-container").remove();
            pageBody.find(".page-container").children().unwrap();
            pageBody.removeClass("with-drawer");
            if(callback) {
                callback();
            }
        }, 200)
    }

    $(".widget-navigation").each(function() {
        var widget = $(this), navigation = widget.find(".nav-wrapper");
        navigation.find(".child-opener").on("click", function() {
            var $this = $(this), child = $this.siblings(".navigation-item-child-container");
            $this.toggleClass("close");
            child.toggleClass("open")
        });
        widget.find(".menu-title").on("click", function() {
            var menuBtn = $(this).find(".menu-button"), transitionType = menuBtn.attr("transition-type");
                    if(transitionType == "drawer") {
                        if(menuBtn.hasClass("close")) {
                    responsiveMenuManager.close()
                } else {
                    responsiveMenuManager.open(function() {
                        menuBtn.addClass("close");
                        drawerOpen(widget.clone(true, true).removeAttr("id"))
                    },function() {
                        menuBtn.removeClass("close");
                        drawerClose();
                    })
                }
            } else {
                if(navigation.hasClass("show")) {
                    responsiveMenuManager.close();
                } else {
                    responsiveMenuManager.open(function() {
                        menuBtn.addClass("close");
                        navigation.addClass("show responsive-navigation")
                    }, function(){
                        menuBtn.removeClass("close");
                        navigation.removeClass("show responsive-navigation");
                    })
                }
            }
        });
    })

});

///////////Search Widget////////
$(function () {
    $(".widget-search .search-menu-button").on("click", function () {
        var $this = $(this), searchForm = $this.siblings(".search-form");
        if (searchForm.hasClass("show")) {
            responsiveMenuManager.close()
        } else {
            responsiveMenuManager.open(function () {
                $this.addClass("close");
                searchForm.addClass("show");
            }, function () {
                $this.removeClass("close");
                searchForm.removeClass("show")
            })
        }
    });

    var searchSuggestionOnselect = function (element, suggestion) {
        this.closest("form").jqObject.submit()
    };

    var searchField = $("#elastic-search-text");

    function bindAutocomplete(searchInput) {
        searchField.autocomplete({
            serviceUrl: app.baseUrl + "lookup/autoComplete",
            type: 'GET',
            showNoSuggestionNotice: true,
            noSuggestionNotice: 'No result Found',
            autoSelectFirst: true,
            preventBadQueries: false,
            groupBy: "category",
            onSelect: searchSuggestionOnselect,
            params: {name: searchInput.val()}
        });
    }

    bindAutocomplete($("#elastic-search-text"))

});



$(window).resize(function() {
    if($(window).width() != site.width && $(window).height() != site.height){
        responsiveMenuManager.close()
        site.width = $(window).width()
        site.height = $(window).height()
    }
});

function shareOnSocialMedea(type, sharing_url, sharing_name, sharing_img) {
    switch(type) {
        case 'twitter':
            window.open('https://twitter.com/intent/tweet?text=' + sharing_name + ' ' + encodeURIComponent(sharing_url), 'sharertwt', 'toolbar=0,status=0,width=640,height=445');
            break;
        case 'facebook':
            window.open('http://www.facebook.com/sharer.php?u=' + sharing_url, 'sharer', 'toolbar=0,status=0,width=660,height=445');
            break;
        case 'linkedin':
            window.open('https://www.linkedin.com/shareArticle?mini=true&url=' + sharing_url + "title=" + sharing_name, 'sharer', 'toolbar=0,status=0,width=660,height=445');
            break;
        case 'google-plus':
            window.open('https://plus.google.com/share?url=' + sharing_url, 'sharer', 'toolbar=0,status=0,width=660,height=445');
            break;
        case 'pinterest':
            var img_url = sharing_img;
            window.open('http://www.pinterest.com/pin/create/button/?media=' + img_url + '&url=' + sharing_url, 'sharerpinterest', 'toolbar=0,status=0,width=660,height=445');
            break;
    }
}

function tellAFriendAboutProduct(productId) {
    bm.ajax({
        url: app.baseUrl + "shop/tellFriend",
        dataType: 'html',
        type: 'post',
        data: {productId: productId},
        success: function(resp) {
            var content = $(resp);
            content.find(".tell-friend-popup").form({
                ajax: true,
                preSubmit: function(ajaxSettings) {
                    $.extend(ajaxSettings, {
                        success: function(resp){
                            var successPopupContent = $(resp.html);
                            renderGlobalSitePopup(successPopupContent, {clazz: "tell-friend-success"})
                        },
                        error: function(a, b, resp) {
                            renderMessage(content.find(".body .message-container"), resp.message, "error")
                        }
                    })
                }
            });
            renderGlobalSitePopup(content, {clazz: "tell_friend_popup"});
        }
    })
}

var ProductViewSwitcher = (function() {
    function ProductViewSwitcher(listingBlock) {
        var _self = this, switchers = listingBlock.find(".view-switchers > button");
        this.listingBlock = listingBlock
        this.id = listingBlock.attr("id")
        this.displayType = listingBlock.attr("display-type")
        this.showOnHover = listingBlock.attr("show-on-hover")
        switchers.on("click", function() {
            var $this = $(this);
            if($this.is(".active")) {
                return
            }
            _self.switchDisplayType($this.attr("value"))
        });
        var selectedDisplayType = localStorage.getItem(this.id)
        if(selectedDisplayType && selectedDisplayType != this.displayType && switchers.length) {
            _self.switchDisplayType(selectedDisplayType)
        }
    }

    var _p = ProductViewSwitcher.prototype;

    _p._switchToList= function() {
        if(this.showOnHover == "true") {
            this.listingBlock.find(".product-block").each(function() {
                var $this = $(this), addBtn = $this.find(".btn-add")
                $this.find(".price-waper").after(addBtn)
            })
        }
    };

    _p._switchToImage = function() {
        if(this.showOnHover == "true") {
            this.listingBlock.find(".product-block").each(function() {
                var $this = $(this), addBtn = $this.find(".btn-add")
                $this.find(".btn-add-placeholder").after(addBtn)
            })
        }
    };

    _p.switchDisplayType = function(displayType) {
        this.listingBlock.removeClass(this.displayType + "-view").addClass(displayType+"-view")
        localStorage.setItem(this.id, displayType)
        this["_switchTo" + displayType.capitalize()]()
        this.listingBlock.find(".view-switchers > button.active").removeClass("active")
        this.listingBlock.find(".view-switchers > button[value=" + displayType + "]").addClass("active")
        this.displayType = displayType;
    };
    return ProductViewSwitcher
}());

$(function() {
    $('.product-view').each(function() {
        new ProductViewSwitcher($(this))
    })
});

window.fbAsyncInit = function() {
    FB.init({
        appId      : app.fb_app_id,
        cookie     : true,
        xfbml      : true,
        version    : 'v2.8'
    });
};

function googleAyncInit() {
    if(app.google_client_id) {
        bm.onReady(window, "gapi", function() {
            gapi.load('auth2', function () {
                window.googleAuth2 = gapi.auth2.init({
                    client_id: app.google_client_id,
                    cookiepolicy: 'single_host_origin',
                });
            });
        })
    }
}


function fbLogin(callback){
    FB.login(function(response) {
        if (response.authResponse) {
            console.log('Welcome!  Fetching your information.... ');
            var access_token = response.authResponse.accessToken;
            callback(access_token)
        } else {
            console.log('User cancelled login or did not fully authorize.');
        }
    },{
        scope: 'email,user_location',
        return_scopes: true
    });
}

function googleLogin(callback) {
    googleAuth2.signIn().then(function(googleUser) {
      callback(googleUser.getAuthResponse().id_token)
    });
}

function loginBySocialToken(data) {
    bm.ajax({
        url: app.baseUrl + "customer/doSocialLogin",
        data: data,
        success: function(resp){
            location.href = resp.redirectUrl
        },
        error: function(status, xhr, resp) {
            renderMessage(null, resp.message, "error")
        }
    })
}
$(function() {
   $('.login-with-fb-btn').on("click", function() {
       var $this = $(this), form = $this.parents("form")
       fbLogin(function(access_token) {
            loginBySocialToken({
                token: access_token,
                media: "fb",
                referer: form.find('[name=referer]').val()
            })
       })
   });
   $('.login-with-google-btn').on("click", function() {
       var $this = $(this), form = $this.parents("form")
       googleLogin(function(access_token) {
            loginBySocialToken({
                token: access_token,
                media: "google",
                referer: form.find('[name=referer]').val()
            })
       })
   })
});



