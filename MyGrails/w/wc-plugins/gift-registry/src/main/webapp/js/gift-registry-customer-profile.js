/**
 * Created by sajedur on 13/08/2014.
 */
$(function() {
    app.config.giftRegistryStatusMgsTime = 6000;
    var customerProfileDom = $("#customer-profile-tabs");
    var PANEL;
    customerProfileDom.on("tab:load", function(ev, data) {
        tabSelect = document.location.search.substring(document.location.search.indexOf("&")+1,document.location.search.lenght);
        if(tabSelect  == "gift_registry=true"){
            $("#my-lists").tabify("activate", "gift_registry");
        }
        switch(data.index) {
            case "my-list":
                manageGiftRegistry(data.panel.find(".gift_registry"));
                break;
        }

        $(this).find(".view-item.gift-registry").on("click", function() {
            customerProfileDom.find("div[data-tabify-tab-id=gift-registry]").click()
        })
    });
    function renderMessage(mgs, type) {
        var messageDom = $('<div class="message-block '+ type +'">' + mgs + '</div>');
        PANEL.before(messageDom);
        messageDom.scrollHere();
        setTimeout(function(){
            messageDom.remove()
        }, app.config.giftRegistryStatusMgsTime);
    }

    function reload() {
        PANEL.loader()
        bm.ajax({
            url: app.baseUrl + "giftRegistry/loadGiftRegistry",
            dataType: "html",
            success: function(html) {
                PANEL.html(html);
                bindBaseEvents(PANEL)
            },
            error: function(xhr, status, resp) {
                PANEL.html($.i18n.prop("error"));
            },
            response: function() {
                PANEL.loader(false)
            }
        });
    }

    function bindBaseEvents(panel){
        panel.find(".action-icon.details").on("click", function(){

        });
        panel.find(".action-icon.edit, .button.create-gift-registry").on("click", function() {
            var id =  $(this).attr("giftRegistry-id");
            loadGiftRegistryEdit(panel, id)
        });
        panel.find(".action-icon.share").on("click", function(){
            var id =  $(this).attr("giftRegistry-id");
            loadGiftRegistryShare(panel, id)
        });
        panel.find(".action-icon.remove").on("click", function(){
            var id =  $(this).attr("giftRegistry-id");
            removeGiftRegistry(id)
        });
        panel.find(".action-icon.view-products").on("click", function(){
            var id =  $(this).attr("giftRegistry-id");
            loadGiftRegistryItems(panel, id)
        });
        panel.find(".action-icon.status").on("click", function(){
            var id =  $(this).attr("giftRegistry-id");
            loadGiftRegistryStatus(panel, id)
        });

    }

    function loadGiftRegistryEdit(panel, id){
        function bindEvents(dom) {
            bm.initCountryChangeHandler($(".country-selector-row select"))
            bm.initCityValidator($('[name="postCode"]'), "countryId")
            dom.closest(".gift-registry-create-edit-form").form({
                ajax: true,
                preSubmit: function(ajaxSettings) {
                    bm.mask(panel);
                    panel.loader();
                    $.extend(ajaxSettings, {
                        success: function (resp) {
                            var referer = bm.path(location.href).query["referer"];
                            if(referer) {
                                window.location =  decodeURIComponent(referer);
                            } else {
                                renderMessage(resp.message, resp.status);
                                reload();
                            }
                            panel.removeClass("updating");
                            panel.loader(false)
                        },
                        error: function (a, b, resp) {
                            renderMessage(resp.message, resp.status);
                            panel.removeClass("updating");
                            panel.loader(false)
                        }
                    })
                }
            })
            dom.find(".time-field").date({
                direction: true,
                show_select_today: false,
                lang_clear_date: $.i18n.prop("clear"),
                time: "HH:mm:ss"
            })
            dom.find(".cancel-button").on("click", function(){
                reload();
            })
        }

        bm.mask(panel);
        panel.addClass("updating").loader();
        bm.ajax({
            url: app.baseUrl + "giftRegistry/edit",
            data: {id: id},
            dataType: "html",
            success: function(resp) {
                resp = $(resp);
                panel.html(resp);
                panel.removeClass("updating");
                panel.loader(false)
                bindEvents(resp)
            }
        })
    };

    function loadGiftRegistryShare(panel, id) {
        function splitEmails(nameField, emailField) {
            var name = nameField.is(":disabled") ? "" : nameField.val().trim();
            var email = emailField.val();
            var nameList = [];
            var emailList = [];
            if (name) {
                email = email.trim()
                nameList.push(name)
                emailList.push(email);
            } else {
                $.each(email.split(/[,;]/), function () {
                    var _email = this.trim();
                    if (_email == "") {
                        return;
                    }
                    var name = "";
                    var email = _email;
                    var gInd = _email.indexOf("<");
                    if (gInd > -1) {
                        name = _email.substring(0, gInd).trim();
                        _email = _email.substring(gInd + 1)
                        gInd = _email.indexOf(">");
                        _email = _email.substring(0, gInd).trim();
                    }
                    nameList.push(name);
                    emailList.push(_email);
                });
            }
            return {names: nameList, emails: emailList};
        }
        function bindEvents(dom) {
            var emailField = dom.find("[name=email]");
            var nameField = dom.find("[name=name]");
            dom.closest(".gift-registry-share-form").form({
                ajax: true,
                preSubmit: function(ajaxSettings) {
                    bm.mask(panel);
                    panel.loader();
                    if(!ajaxSettings.data) {
                        ajaxSettings.data = {}
                    }
                    var _data = splitEmails( nameField, emailField);
                    $.extend(ajaxSettings.data, _data);
                    $.extend(ajaxSettings, {
                        success: function (resp) {
                            renderMessage(resp.message, resp.status);
                            loadGiftRegistryShare(panel, id)
                        },
                        error: function (a, b, resp) {
                            renderMessage(resp.message, resp.status);
                            panel.removeClass("updating");
                            panel.loader(false)
                        }
                    })
                }
            })
            dom.find(".cancel-button").on("click", function(){
                reload();
            })
            emailField.blur(function () {
                var email = this.value;
                if (email.indexOf(",") != -1 || email.indexOf(";") != -1 || email.indexOf("<") != -1) {
                    nameField.attr("disabled", "disabled").val($.i18n.prop("single.email.without.name"));
                } else {
                    if (nameField.is(":disabled")) {
                        nameField.val("").removeAttr("disabled");
                    }
                }
            });

        }
        bm.ajax({
            url: app.baseUrl + "giftRegistry/editShare",
            data: {id: id},
            success: function(resp) {
                resp = $(resp.html);
                panel.html(resp);
                bindEvents(resp)
            },
            error: function(xhr, status, resp) {
                renderMessage(resp.message, resp.status)
            }
        })
    }

    function removeGiftRegistry(id) {
        bm.confirm($.i18n.prop("gift.registry.remove.confirm"), function() {
            bm.ajax({
                url: app.baseUrl + "giftRegistry/remove",
                data: {id: id},
                success: function(resp) {
                    renderMessage(resp.message, resp.status);
                    reload();
                },
                error: function(xhr, status, resp){
                    renderMessage(resp.message, resp.status);
                }
            })
        }, function(){})
    }

    function loadGiftRegistryItems(panel, id) {
        var bindEvents = function(dom) {
            dom.find(".action-icon.remove").on("click", function() {
                var icon = $(this);
                var itemId = icon.attr("item-id");
                bm.confirm($.i18n.prop("gift.registry.item.remove.confirm"), function(){
                    bm.ajax({
                        url: app.baseUrl + "giftRegistry/removeItem",
                        data: {id: itemId, giftRegistryId: id},
                        success: function(resp) {
                            renderMessage(resp.message, resp.status);
                            icon.closest("tr").remove();
                            panel.removeClass("updating");
                            panel.loader(false)

                        },
                        error: function(xhr, status, resp) {
                            panel.removeClass("updating");
                            panel.loader(false)
                            renderMessage(resp.message, resp.status);
                        }
                    });
                }, function(){});
            });
            dom.find(".cancel-button").on("click", function() {
                reload();
                panel.removeClass("updating");
                panel.loader(false)
            })
            panel.removeClass("updating");
            panel.loader(false)
        };
        panel.addClass("updating").loader();
        bm.ajax({
            url: app.baseUrl + "giftRegistry/viewItems",
            data: {id: id},
            dataType: "html",
            success: function(resp) {
                resp = $(resp);
                panel.html(resp);
                bindEvents(resp)
            }
        })
    }

    function loadGiftRegistryStatus(panel, id) {
        var bindEvents = function(dom) {
            dom.find(".cancel-button").on("click", function() {
                reload();
                panel.removeClass("updating");
                panel.loader(false)
            })
        };
        panel.addClass("updating").loader();
        bm.ajax({
            url: app.baseUrl + "giftRegistry/status",
            data: {id: id},
            dataType: "html",
            success: function(resp) {
                resp = $(resp);
                panel.html(resp);
                panel.removeClass("updating");
                panel.loader(false)
                bindEvents(resp)
            }
        })
    }

    function manageGiftRegistry(panel) {
        PANEL = panel;
        bindBaseEvents(panel);
    }
})