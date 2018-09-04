//TODO: Need improvement
var _p = app.customerProfile = {
    _showStatus: function (dom, status, message) {
        var statusDom = dom.find(".message-block");
        if (!statusDom.length) {
            statusDom = dom.prepend('<span class="message-block"></span>').find(".message-block");
        }
        statusDom.removeClass('success').removeClass('error').removeClass('info').addClass(status);
        statusDom.html(message);
        setTimeout(function () {
            statusDom.remove();
        }, app.config.customer_profile_message_display_time)
    },
    _showStatusAfterTitle: function (dom, status, message) {
        if (message && message.length) {
            var statusDom = dom.find(".message-block");
            if (!statusDom.length) {
                dom.find('.title').after('<span class="message-block"></span>');
                statusDom = dom.find(".message-block")
            }
            statusDom.removeClass('success').removeClass('error').removeClass('info').addClass(status);
            statusDom.html(message);
            setTimeout(function () {
                statusDom.remove();
            }, app.config.customer_profile_message_display_time);
        }
    }
};

_p.initOverview = function (data) {
    bindAddToCartClickEvent(data.panel);
    initializeProductWidget();
}

_p.initMyOrders = function (data) {
    this.initPendingOrder(data);
    this.initCompletedOrder(data);
}

_p.initMyCarts = function (data) {

}
_p.initMyEntitlements = function (data) {
    var panel = data.panel
    panel.find(".store-credit-request-form").form({
        ajax: {
            response: function (status, res) {
                var message = res.message;
                panel.find('.msg').val('');
                if (message && message.length) {
                    var statusDom = panel.find(".store-credit-info");
                    var msgDom = $('<span class="message-block">' + message + '</span>');
                    msgDom.addClass(status);
                    statusDom.after(msgDom);
                    msgDom.scrollHere();
                    setTimeout(function () {
                        msgDom.remove();
                    }, app.config.customer_profile_message_display_time);
                }
            }
        }
    });

}
_p.initMyWallet = function (data) {
    var panel = data.panel
    function bindLinkCard() {
        panel.find(".link-card-btn").click(function () {
            var popupBody
            bm.renderSitePopup(app.baseUrl + "customer/addCreditCard", $.i18n.prop("link.a.card"), "", {}, {
                events: {
                    content_loaded: function (popup) {
                        popupBody = popup.el
                    }
                },
                beforeSubmit: function () {},
                success: function (_resp) {
                    reload(_resp);
                },
                error: function (xhr, status, resp) {
                    _p._showStatus(popupBody.find("form"), "error", resp.message)
                }
            })
        });

        var content = bm.floatingPopup(panel.find(".credit-debit-card .floating-popup"));
        content.find(".action-item").click(function () {
            var data = this.jqObject.parents(".popup-body").data();
            bm.confirm($.i18n.prop("confirm.remove", [$.i18n.prop("card"), data.cardNumber]), function () {
                bm.ajax({
                    url: app.baseUrl + "customer/removeCreditCard",
                    data: data,
                    dataType: "json",
                    success: function (resp) {
                        reload(resp);
                    },
                    error: function (xhr, status, resp) {
                        _self._showStatus(panel.find(".accordion-item.wallet"), resp.status, resp.message)
                    }
                });
            }, function() {});
        });

        function reload(_resp) {
            bm.ajax({
                url: app.baseUrl + "customer/loadStoreWallet",
                dataType: "html",
                success: function (resp) {
                    panel.find(".accordion-item.wallet").html(resp);
                    if(_resp) {
                        _self._showStatus(panel.find(".accordion-item.wallet"), "success", _resp.message)
                    }
                    bindLinkCard();
                }
            })
        }

    }

    bindLinkCard();
}
_p.initManageAccount = function (data) {
    var panel = data.panel, _self = this;
    panel.find(".label-bar").unbind();
    panel.find(".accordion-panel").accordion({all_close: true});

    var accountDetails = panel.find(".account_information");
    var billingAddress = panel.find(".billing_address");
    var shippingAddress = panel.find(".shipping_address");

    accountDetails.find(".account-details-edit-link").on("click", function () {
        loadAccountDetailsForm(accountDetails)
    });

    accountDetails.find(".password-edit-link").on("click", function () {
        loadEditPasswordForm(accountDetails)
    });

    billingAddress.find(".create-new").on("click", function () {
        loadBillingAddressForm(billingAddress)
    });

    shippingAddress.find(".create-new").on("click", function () {
        loadShippingAddressForm(shippingAddress)
    });

    var loadAccountDetails = function (dom, status, message) {
        bm.mask(dom, '<div></div>');
        bm.ajax({
            url: app.baseUrl + "customer/loadAccountDetails",
            dataType: "html",
            success: function (details) {
                dom.html(details);
                dom.find(".account-details-edit-link").on("click", function () {
                    loadAccountDetailsForm(dom)
                });
                dom.find(".password-edit-link").on("click", function () {
                    loadEditPasswordForm(accountDetails)
                });
                _self._showStatus(accountDetails, status, message)
            }
        });
    };

    var loadAccountDetailsForm = function (dom) {
        bm.mask(dom, '<div></div>');
        bm.ajax({
            url: app.baseUrl + "customer/editAccountDetails",
            dataType: "html",
            success: function (resp) {
                dom.html(resp);
                bm.initCountryChangeHandler(dom.find(".country-selector-row select"));
                bm.initCityValidator(dom.find('[name="postCode"]'), "countryId");
                dom.find(".account-details-form .cancel-button").on("click", function () {
                    loadAccountDetails(dom)
                });
                dom.find(".account-details-form").form({
                    ajax: {
                        dataType: "html",
                        success: function (res) {
                            dom.html(res);
                            _self._showStatus(dom, "success", $.i18n.prop("account.update.successful"));
                            dom.find(".account-details-edit-link").on("click", function () {
                                loadAccountDetailsForm(dom,res.status, res.message)
                            });
                            dom.find(".password-edit-link").on("click", function () {
                                loadEditPasswordForm(accountDetails)
                            });
                            var messageDom = dom.find(".account-details-wrap .message-block");
                            setTimeout(function () {
                                messageDom.remove();
                            }, app.config.customer_profile_message_display_time);
                        },
                        error: function (xhr, status, res) {
                            dom.html(res);
                            dom.find(".account-details-edit-link").on("click", function () {
                                loadAccountDetailsForm(dom)
                            });
                        }
                    }
                });
            }
        });
    };

    var loadEditPasswordForm = function (dom, status, message) {
        bm.mask(dom, '<div></div>');
        bm.ajax({
            url: app.baseUrl + "customer/editPassword",
            dataType: "html",
            success: function (resp) {
                dom.html(resp);
                 _self._showStatus(dom, status, message);
                dom.find(".cancel-button").on("click", function () {
                    loadAccountDetails(dom)
                });
                dom.find(".edit-password-form").form({
                    ajax: {
                        dataType: "json",
                        success: function (res) {
                            loadAccountDetails(dom, res.status, res.message)
                        },
                        error: function (xhr, status, res) {
                            loadEditPasswordForm(dom, res.status, res.message)
                        }
                    }
                });
            }
        })
    };

    var bindBillingAddressEditLink = function (dom) {
        var addressList = dom.find(".edit");
        $.each(addressList, function (idx, address) {
            $(address).on("click", function () {
                loadBillingAddressForm(dom, $(address).attr("address-id"));
            });
        });
    }

    var bindBillingAddressDeleteLink = function (dom) {
        var addressList = dom.find(".delete");
        $.each(addressList, function (idx, address) {
            $(address).on("click", function () {
                if ($(address).hasClass("active-address")) {
                    bm.alert($.i18n.prop("active.address.cannot.delete"), "", function () {
                    });
                } else {
                    confirmBillingAddressDelete($(address).attr("address-id"));
                }
            });
        });
    };

    var bindBillingAddressActiveLink = function (dom) {
        var addressList = dom.find(".active");
        $.each(addressList, function (idx, address) {
            $(address).on("click", function () {
                makeActiveBillingAddress($(address).attr("address-id"));
            });
        });
    };

    bindBillingAddressEditLink(billingAddress);
    bindBillingAddressDeleteLink(billingAddress);
    bindBillingAddressActiveLink(billingAddress);

    var loadBillingAddressList = function (dom, status, message) {
        bm.mask(dom, '<div></div>');
        bm.ajax({
            url: app.baseUrl + "customer/loadAddressList",
            data: {addressType: "billing"},
            dataType: "html",
            success: function (resp) {
                dom.html(resp);
                bindBillingAddressEditLink(billingAddress);
                bindBillingAddressDeleteLink(billingAddress);
                bindBillingAddressActiveLink(billingAddress);
                dom.find(".create-new").on("click", function () {
                    loadBillingAddressForm(dom)
                });
                _self._showStatus(billingAddress, status, message)
            }
        });
    };

    var loadBillingAddressForm = function (dom, addressId, status, message) {
        bm.mask(dom, '<div></div>');
        bm.ajax({
            url: app.baseUrl + "customer/editAddress",
            data: {addressType: "billing", addressId: addressId},
            dataType: "html",
            success: function (resp) {
                dom.html(resp);
                _self._showStatus(dom, status, message);
                var form = dom.find(".edit-address-form");
                form.find(".cancel-button").click(function () {
                    loadBillingAddressList(dom)
                });
                form.form({
                    ajax: {
                        success: function (res) {
                            loadBillingAddressList(dom, res.status, res.message);
                        },
                        error: function (xhr, status, res) {
                            loadBillingAddressForm(dom, addressId, res.status, res.message)
                        }
                    }
                });
                bm.initCountryChangeHandler(form.find(".country-selector-row select"));
                bm.initCityValidator(form.find('[name="postCode"]'), "countryId");
            }
        });
    };

    var confirmBillingAddressDelete = function (id) {
        bm.confirm($.i18n.prop("confirm.delete.address"), function () {
            bm.ajax({
                url: app.baseUrl + "customer/deleteAddress",
                data: {id: id, addressType: "billing"},
                dataType: "json",
                success: function (resp) {
                    _self._showStatus(billingAddress, resp);
                    loadBillingAddressList(billingAddress, resp.status, resp.message);
                },
                error: function (xhr, status, resp) {
                    loadBillingAddressList(billingAddress, resp.status, resp.message)
                }
            });
        }, function () {

        });
    };

    var makeActiveBillingAddress = function (id) {
        bm.ajax({
            url: app.baseUrl + "customer/changeActiveAddress",
            data: {id: id, addressType: "billing"},
            dataType: "json",
            success: function (resp) {
                loadBillingAddressList(billingAddress)
            },
            error: function (xhr, status, resp) {
                loadBillingAddressList(billingAddress)
            }
        });
    };

    /* +++ Shipping +++ */
    var bindShippingAddressEditLink = function (dom) {
        var addressList = dom.find(".edit");
        $.each(addressList, function (idx, address) {
            $(address).on("click", function () {
                loadShippingAddressForm(dom, $(address).attr("address-id"));
            });
        });
    };

    var bindShippingAddressDeleteLink = function (dom) {
        var addressList = dom.find(".delete");
        $.each(addressList, function (idx, address) {
            $(address).on("click", function () {
                if ($(address).hasClass("active-address")) {
                    bm.alert($.i18n.prop("active.address.cannot.delete"), "", function () {
                    });
                } else {
                    confirmShippingAddressDelete($(address).attr("address-id"));
                }
            });
        });
    };

    var bindShippingAddressActiveLink = function (dom) {
        var addressList = dom.find(".active");
        $.each(addressList, function (idx, address) {
            $(address).on("click", function () {
                makeActiveShippingAddress($(address).attr("address-id"));
            });
        });
    };

    bindShippingAddressEditLink(shippingAddress);
    bindShippingAddressDeleteLink(shippingAddress);
    bindShippingAddressActiveLink(shippingAddress);

    var loadShippingAddressList = function (dom, status, message) {
        bm.mask(dom, '<div></div>');
        bm.ajax({
            url: app.baseUrl + "customer/loadAddressList",
            data: {addressType: "shipping"},
            dataType: "html",
            success: function (resp) {
                dom.html(resp);
                bindShippingAddressEditLink(shippingAddress);
                bindShippingAddressDeleteLink(shippingAddress);
                bindShippingAddressActiveLink(shippingAddress);
                dom.find(".create-new").on("click", function () {
                    loadShippingAddressForm(dom)
                });
                _self._showStatus(shippingAddress, status, message)
            }
        });
    };

    var loadShippingAddressForm = function (dom, addressId, status, message) {
        dom.loader()
        bm.ajax({
            url: app.baseUrl + "customer/editAddress",
            data: {addressType: "shipping", addressId: addressId},
            dataType: "html",
            success: function (resp) {
                dom.loader(false)
                dom.html(resp);
                _self._showStatus(dom, status, message);
                var form = dom.find(".edit-address-form");
                form.find(".cancel-button").click(function () {
                    loadShippingAddressList(dom)
                });
                form.form({
                    ajax: {
                        success: function (res) {
                            loadShippingAddressList(dom, res.status, res.message)
                        },
                        error: function (xhr, status, res) {
                            loadShippingAddressForm(dom, res.status, res.message)
                        }
                    }
                });
                bm.initCountryChangeHandler(form.find(".country-selector-row select"));
                bm.initCityValidator(form.find('[name="postCode"]'), "countryId");
            }
        });
    };

    var confirmShippingAddressDelete = function (id) {
        bm.confirm($.i18n.prop("confirm.delete.address"), function () {
            bm.ajax({
                url: app.baseUrl + "customer/deleteAddress",
                data: {id: id, addressType: "shipping"},
                dataType: "json",
                success: function (resp) {
                    loadShippingAddressList(shippingAddress, resp.status, resp.message)
                },
                error: function (xhr, status, resp) {
                    loadShippingAddressList(shippingAddress, resp.status, resp.message)
                }
            });
        }, function () {

        });
    };

    var makeActiveShippingAddress = function (id) {
        bm.ajax({
            url: app.baseUrl + "customer/changeActiveAddress",
            data: {id: id, addressType: "shipping"},
            dataType: "json",
            success: function (resp) {
                loadShippingAddressList(shippingAddress)
            },
            error: function (xhr, status, resp) {
                loadShippingAddressList(shippingAddress)
            }
        });
    };

    panel.find(".store-credit-request-form").form({
        ajax: {
            response: function (status, res) {
                var message = res.message;
                panel.find('.msg').val('');
                if (message && message.length) {
                    var statusDom = panel.find(".store-credit-info");
                    var msgDom = $('<span class="message-block">' + message + '</span>');
                    msgDom.addClass(status);
                    statusDom.after(msgDom);
                    msgDom.scrollHere();
                    setTimeout(function () {
                        msgDom.remove();
                    }, app.config.customer_profile_message_display_time);
                }
            }
        }
    });

    function bindLinkCard() {
        panel.find(".link-card-btn").click(function () {
            var popupBody
            bm.renderSitePopup(app.baseUrl + "customer/addCreditCard", $.i18n.prop("link.a.card"), "", {}, {
                events: {
                    content_loaded: function (popup) {
                        popupBody = popup.el
                    }
                },
                beforeSubmit: function () {},
                success: function (_resp) {
                    reload(_resp);
                },
                error: function (xhr, status, resp) {
                    _self._showStatus(popupBody.find("form"), "error", resp.message)
                }
            })
        });

        var content = bm.floatingPopup(panel.find(".credit-debit-card .floating-popup"));
        content.find(".action-item").click(function () {
            var data = this.jqObject.parents(".popup-body").data();
            bm.confirm($.i18n.prop("confirm.remove", [$.i18n.prop("card"), data.cardNumber]), function () {
                bm.ajax({
                    url: app.baseUrl + "customer/removeCreditCard",
                    data: data,
                    dataType: "json",
                    success: function (resp) {
                        reload(resp);
                    },
                    error: function (xhr, status, resp) {
                        _self._showStatus(panel.find(".accordion-item.wallet"), resp.status, resp.message)
                    }
                });
            }, function() {});
        });

        function reload(_resp) {
            bm.ajax({
                url: app.baseUrl + "customer/loadStoreWallet",
                dataType: "html",
                success: function (resp) {
                    panel.find(".accordion-item.wallet").html(resp);
                    if(_resp) {
                        _self._showStatus(panel.find(".accordion-item.wallet"), "success", _resp.message)
                    }
                    bindLinkCard();
                }
            })
        }

    }

    bindLinkCard();

};

_p.initPendingOrder = function (data) {
    var panel = data.panel, _self = this;
    var pendingOrders = panel.find(".pending_order");
    var attachOrderList = function () {
        var content = bm.floatingPopup(panel.find(".floating-popup"));
        content.find(".action-item").click(function () {
            var data = this.jqObject.parents(".popup-body").data();
            switch (this.jqObject.data("action")) {
                case "details-pending":
                    orderDetails(data.id,pendingOrders);
                    break;
                case "order-comment-pending":
                    _self.orderComments(data.id);
                    break;
                case "payment-pending":
                    payOrder(data.id);
                    break;
                case "cancel-pending":
                    cancelOrder(data.id);
                    break;
            }
        });
    };
    attachOrderList();
    var orderDetails = function (orderId,dom) {
        dom.loader()
        bm.ajax({
            url: app.baseUrl + "customer/loadPendingOrderDetails",
            data: {orderId: orderId},
            dataType: "html",
            success: function (resp) {
                bindProductShare(dom);
                dom.loader(false)
                dom.html(resp);
                dom.find(".back-button").click(function () {
                    loadPendingOrder(dom);
                });
                dom.find(".order-comment").click(function () {
                    _self.orderComments(this.jqObject.data("id"));
                })
            },
            error: function () {
                dom.loader(false)
                dom.html($.i18n.prop("error"))
            }
        });
    };

    var cancelOrder = function(orderId) {
        bm.confirm($.i18n.prop("confirm.cancel.order"), function() {
            bm.ajax({
                url: app.baseUrl + "customer/cancelOrder",
                data: {orderId: orderId},
                success: function(resp) {
                    _self._showStatus(panel.find(".bmui-tab-body-container"), "success", resp.message)
                    loadPendingOrder(panel.find("#bmui-tab-pending_order"))
                }
            })
        }, function() {})
    };

    function payOrder(orderId) {
        bm.ajax({
            url: app.baseUrl + "customer/payOrder",
            data: {orderId: orderId},
            success: function(resp) {
                if(resp.content) {
                    renderGlobalSitePopup($(resp.content), {clazz:  "exception-popup"})
                } else {
                    location.href = app.baseUrl + "cart/details"
                }
            },
            error: function(xhr, status, resp) {
                _self._showStatus(panel, "error", resp.message)
            }
        });
    }
    function loadPendingOrder(dom) {
        dom.loader()
        bm.ajax({
            url: app.baseUrl + "customer/loadPendingOrder",
            dataType: "html",
            success: function (resp) {
                dom.loader(false)
                dom.html(resp);
                attachOrderList();
            },
            error: function () {
                dom.loader(false)
                dom.html($.i18n.prop("error"));
            }
        });
    }
};

_p.initCompletedOrder = function (data) {
    var _self = this;
    var panel = data.panel;
    var completedOrders = panel.find(".completed_order")
    var bindOrderList = function () {
        var content = bm.floatingPopup(panel.find(".floating-popup"));
        content.find(".action-item").click(function () {
            var data = this.jqObject.parents(".popup-body").data();
            switch (this.jqObject.data("action")) {
                case "details-completed":
                    orderDetails(data.id,completedOrders);
                    break;
                case "reorder-completed":
                    addAllToCart(data);
                    break;
                case "order-comment-completed":
                    _self.orderComments(data.id);
                    break;
            }
        });
    };

    bindOrderList();

    var orderDetails = function (orderId,dom) {
        dom.loader()
        bm.ajax({
            url: app.baseUrl + "customer/loadCompletedOrderDetails",
            data: {orderId: orderId},
            dataType: "html",
            success: function (resp) {
                bindProductShare(panel);
                dom.loader(false)
                dom.html(resp);
                dom.find(".back-button").click(function () {
                    loadCompleteOrder(dom);
                });
                dom.find(".add-all-to-cart").click(function () {
                    addAllToCart(this.jqObject.data());
                });
                dom.find(".action-icon.add-to-cart").click(function () {
                    addAllToCart(this.jqObject.data());
                })
                dom.find(".order-comment").click(function () {
                    _self.orderComments(this.jqObject.data("id"));
                })
            },
            error: function (xhr, status, resp) {
                dom.loader(false)
                dom.html($.i18n.prop("error"))
            }
        });
    };

    function loadCompleteOrder(dom) {
        dom.loader()
        bm.ajax({
            url: app.baseUrl + "customer/loadCompletedOrder",
            dataType: "html",
            success: function (resp) {
                dom.loader(false)
                dom.html(resp);
                bindOrderList();
            },
            error: function () {
                dom.loader(false)
                dom.html($.i18n.prop("error"));
            }
        });
    }

    function addAllToCart(data) {
        bm.confirm($.i18n.prop("confirm.add.to.cart"), function() {
            bm.ajax({
                url: app.baseUrl + "customer/addAllToCart",
                data: $.extend({}, data),
                success: function(resp) {
                    if(resp.ex) {
                        renderGlobalSitePopup($(resp.ex), {clazz:  "exception-popup"})
                    } else if(data.itemId) {
                        app.global_event.trigger("update-cart");
                        location.href = app.baseUrl + "cart/details"
                    } else {
                        location.href = app.baseUrl + "cart/details"
                    }
                },
                error: function(xhr, status, resp) {
                    _self._showStatus(panel, "error", resp.message)
                }
            });
        }, function(){})
     }

};



var generateSocialMediaShareKey = function (productId, customerId, medium) {
    bm.ajax({
        url: app.baseUrl + "customer/generateSocialMediaShareKey",
        show_response_status: false,
        data: {
            productId: productId,
            customerId: customerId,
            medium: medium
        },
        success: function(resp) {
            var url = bm.htmlEncode(bm.getAbsoluteURL(resp.url));
            if(medium.equals("fb")) {
                shareOnSocialMedea("facebook", url);
            } else if(medium.equals("tw")) {
                shareOnSocialMedea("twitter", url, "");
            } else if(medium.equals("gp")) {
                shareOnSocialMedea("google-plus", url);
            } else if(medium.equals("ln")) {
                shareOnSocialMedea("linkedin", url, "");
            }
        }
    });
};

var bindProductShare = function (panel) {
    panel.on("click", ".product-share", function () {
        var _this = $(this);
        var productId = _this.attr("data-product-id").trim();
        var userId = _this.closest("#order-details").attr("customer-id");
        bm.floatingPanel(_this, app.baseUrl + "customer/showShareOption", {}, {
            height: null,
            masking: true,
            clazz: "product-share-option",
            position_collison: "none",
            events: {
                content_loaded: function (popup) {
                    var element = popup.el;
                    element.on('click', '.facebook-share', function () {
                        generateSocialMediaShareKey(productId, userId, "fb");
                    });
                    element.on('click', '.twitter-share', function () {
                        generateSocialMediaShareKey(productId, userId, "tw");
                    });
                    element.on('click', '.googleplus-share', function () {
                        generateSocialMediaShareKey(productId, userId, "gp");
                    });
                    element.on('click', '.linkedin-share', function () {
                        generateSocialMediaShareKey(productId, userId, "ln");
                    });
                }
            }
        });
    });
};

_p.orderComments = function (orderId) {
    var _self = this;
    var commentArea
    bm.renderSitePopup(app.baseUrl + "customer/loadOrderComments", $.i18n.prop("message"), "", {orderId: orderId}, {
        auto_close_on_success: false,
        events: {
            content_loaded: function (popup) {
                commentArea = this.find(".comment-area");
                commentArea.scrollTop(commentArea[0].scrollHeight);
            }
        },
        success: function (resp, status, ajaxObj, form) {
            var message = $("<div class='comment-row customer'><span class='name'>" +
                $.i18n.prop(resp.customerName ? resp.customerName : 'customer') + " </span>" +
                "<span class='date-time-row'><span class='date-time'>" + resp.date + "</span>" +
                "<span class='show-comment'>" + resp.message + "</span></span></div>");
            commentArea.append(message);
            form.find("textarea").val("");
            commentArea.animate({scrollTop: commentArea[0].scrollHeight}, "slow");
        },
        error: function (a, b, resp) {
            _self._showStatus(commentArea, "error", resp.message);
        }
    });
};

$(function () {
    var customerProfileTab = app.customerProfile.body = $("#customer-profile-tabs");
    customerProfileTab.tabify("option", "load", function (data) {
        if (app.customerProfile["init" + data.index.camelCase()]) {
            app.customerProfile["init" + data.index.camelCase()](data)
        }
    });
    app.global_event.trigger('customer-profile-tabs-initialize', [customerProfileTab]);
    customerProfileTab.tabify("option", "loader_template",
        "<div class='loader-mask div-mask customer-profile-loading'><span class='vertical-aligner'></span><span class='loading-text'>" +
        $.i18n.prop("loading") + " ...</span></div>");
});
