$(function() {
    app.config.wishListStatusMgsTime = 6000;
    var customerProfileDom = $("#customer-profile-tabs");
    var PANEL;
    customerProfileDom.on("tab:load", function(ev, data) {
        var flag = true;
        tabSelect = document.location.search.substring(1,document.location.search.lenght)
        if(tabSelect  == "wish_list"){
            $("#my-lists").tabify("activate", tabSelect);
        }
        switch(data.index) {
            case "overview":
                bm.onReady(window, "attachWishListGlobalEvents", {
                    ready: function () {
                        if(flag) window.attachWishListGlobalEvents(data.panel)
                    },
                    not: function () {
                        flag = false;
                        $("head").append("<script src='" + app.systemResourceUrl + "plugins/wish-list/js/shared/wish-list.js'></script>");
                    }
                });
                break;
            case "my-list":
                manageWishList(data.panel);
                break;
        }
    });
    function renderMessage(mgs, type) {
        var messageDom = $('<div class="message-block '+ type +'">' + mgs + '</div>');
        PANEL.before(messageDom);
        messageDom.scrollHere();
        setTimeout(function(){
            messageDom.remove()
        }, app.config.wishListStatusMgsTime);
    }

    function reload() {
        PANEL.loader()
        bm.ajax({
            url: app.baseUrl + "wishlist/loadWishList",
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
        panel.find(".action-icon.edit, .button.create-wish-list").on("click", function() {
            var id =  $(this).attr("wishList-id");
            loadWishListEdit(panel, id)
        });
        panel.find(".action-icon.share").on("click", function(){
            var id =  $(this).attr("wishList-id");
            loadWishListShare(panel, id)
        });
        panel.find(".action-icon.remove").on("click", function(){
            var id =  $(this).attr("wishList-id");
            removeWishList(id)
        });

    }

    function loadWishListEdit(panel, id){
        function bindEvents(dom) {
            dom.closest(".wish-list-edit-form").form({
                ajax: true,
                preSubmit: function(ajaxSettings) {
                    panel.loader();
                    $.extend(ajaxSettings, {
                        success: function (resp) {
                            renderMessage(resp.message, resp.status);
                            reload();
                            panel.loader(false)
                        },
                        error: function (a, b, resp) {
                            renderMessage(resp.message, resp.status);
                            panel.loader(false)
                        }
                    })
                }
            });
            dom.find(".cancel-button").on("click", function(){
                reload();
            });
            dom.find(".action-icon.remove").on("click", function() {
                var icon = $(this);
                var itemId = icon.attr("item-id");
                bm.confirm($.i18n.prop("wish.list.item.remove.confirm"), function(){
                    bm.ajax({
                        url: app.baseUrl + "wishlist/deleteItem",
                        data: {id: itemId, wishListId: id},
                        success: function(resp) {
                            renderMessage(resp.message, resp.status);
                            icon.closest("tr").remove();
                        },
                        error: function(xhr, status, resp) {
                            renderMessage(resp.message, resp.status);
                        }
                    });
                }, function(){});
            })
        }
        panel.loader();
        bm.ajax({
            url: app.baseUrl + "wishlist/edit",
            data: {id: id},
            dataType: "html",
            response: function() {
                panel.loader(false)
            },
            success: function(resp) {
                resp = $(resp);
                panel.html(resp);
                bindEvents(resp)
            }
        })
    }
    function loadWishListShare(panel, id) {
        function splitEmails(nameField, emailField) {
            var name = nameField.is(":disabled") ? "" : nameField.val().trim();
            var email = emailField.val();
            var nameList = [];
            var emailList = [];
            if (name) {
                email = email.trim();
                nameList.push(name);
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
                        _email = _email.substring(gInd + 1);
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
            dom.closest(".wish-list-share-form").form({
                ajax: true,
                preSubmit: function(ajaxSettings) {
                    panel.loader();
                    if(!ajaxSettings.data) {
                        ajaxSettings.data = {}
                    }
                    var _data = splitEmails( nameField, emailField);
                    $.extend(ajaxSettings.data, _data);
                    $.extend(ajaxSettings, {
                        success: function (resp) {
                            renderMessage(resp.message, resp.status);
                            loadWishListShare(panel, id)
                        },
                        error: function (a, b, resp) {
                            renderMessage(resp.message, resp.status);
                            panel.removeClass("updating");
                            panel.loader(false)
                        }
                    })
                }
            });
            dom.find(".cancel-button").on("click", function(){
                reload();
            });
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
            url: app.baseUrl + "wishlist/editShare",
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

    function removeWishList(id) {
        bm.confirm($.i18n.prop("wish.list.remove.confirm"), function() {
            bm.ajax({
                url: app.baseUrl + "wishlist/remove",
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

    function manageWishList(panel) {
        PANEL = panel.find(".wish_list");
        bindBaseEvents(PANEL);
    }
    if(location.hash == "#wish-list") {
        $("#customer-profile-tabs").tabify("activate", "wish-list")
    }
    site.hook.register("wish-list-popup-content", function(content) {
        content.find(".view-wish-list").on("click", function(evt) {
            evt.preventDefault();
            $("#customer-profile-tabs").tabify("activate", "wish-list");
            site.global_single_popup.close();
        })
    })
    app.global_event.on("wish-list-create", reload)
});