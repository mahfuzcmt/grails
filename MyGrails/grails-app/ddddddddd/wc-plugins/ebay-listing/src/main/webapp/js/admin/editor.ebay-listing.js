app.tabs.editEbayListingProfile = function() {
    app.tabs.editEbayListingProfile._super.constructor.apply(this, arguments);
    $.extend(this, {
        text: $.i18n.prop("edit.ebay.profile"),
        name: this.profile.name,
        tip: this.profile.name,
        ui_class: "edit-ebay-profile edit",
        ajax_url: app.baseUrl + "ebayListingAdmin/loadProfileEditor?profileId=" + this.profile.id
    });
};

app.tabs.editEbayListingProfile.inherit(app.MultiTab);

var _elp = app.tabs.editEbayListingProfile.prototype;

(function() {
    function attachEvents() {

    }

    _elp.init = function() {
        app.tabs.editEbayListingProfile._super.init.call(this);
        attachEvents.call(this);
    };

    app.tabs.editEbayListingProfile.tabInitFunctions = {
        basic: function(panel) {
            var _self = this;
            var content = $('<form class="edit-popup-form" method="post">' +
                '<div class="category-tree-container"><span>loading...</span></div>' +
                '<div class="button-line">' +
                '<button type="submit" class="submit-button">' + $.i18n.prop('ok') + '</button>' +
                '<button type="button" class="cancel-button">' + $.i18n.prop('cancel') + '</button>' +
                '</div>' +
                '</form>');
            panel.find('.category-breadcrumb').click(function() {
                var parent = $(this).closest('.category-selector');
                var catId = parent.find('.selected-category').val();
                var treeContainer = content.find('.category-tree-container');
                treeContainer.css({height: 400});
                bm.editPopup(undefined, $.i18n.prop('browse.category'), undefined, undefined, {
                    width: 800,
                    content: content,
                    events: {
                        content_loaded: function() {
                            var popup = $(this);
                            bm.ajax({
                                url: app.baseUrl + 'ebayListingAdmin/loadCategoryTree',
                                data: {id: _self.profile.id},
                                success: function(resp) {
                                    treeContainer.tree({
                                        type_prop: "type",
                                        auto_load_lazy_nodes: false,
                                        children: resp.children,
                                        onActivate: function(node) {
                                            popup.data({selectedNode: node})
                                        }
                                    });
                                    var tree = treeContainer.tree('inst');
                                    treeContainer.scrollbar();
                                    var nodeToActivate = tree.getNodeByKey(catId);
                                    if(nodeToActivate) {
                                        nodeToActivate.activate();
                                    }
                                    treeContainer.scrollbar("update");
                                },
                                error: function() {
                                    bm.notify($.i18n.prop('ebay.category.load.failed'), 'error')
                                }
                            });
                        }
                    },
                    beforeSubmit: function(form, settings, popup) {
                        function buildCategoryBreadcrumb(node) {
                            if(node.parent) {
                                return buildCategoryBreadcrumb(node.parent) +
                                    '<span class="node"><span class="right-arrow"> &raquo; </span><span class="title">' + node.data.name + '</span></span>';
                            } else {
                                return '';
                            }
                        }
                        var selectedNode = $(this).data('selectedNode');
                        parent.find('.selected-category').val(selectedNode.key);
                        parent.find('.category-breadcrumb').html(buildCategoryBreadcrumb(selectedNode));
                        popup.close();
                        return false;
                    },
                    success: function(resp) {
                        app.global_event.trigger('ebay-listing-profile-create');
                    }
                });
            });
        },
        pricing: function(panel) {

        },
        paymentMethod: function(panel) {

        },
        postage: function(panel) {

        },
        returnPolicy: function(panel) {

        },
        schedule: function(panel) {

        },
        setting: function(panel) {

        }
    };

    _elp.onContentLoad = function(data) {
        var _self = this;
        data.panel.find("form").form({
            ajax: {
                success: function() {
                    app.global_event.trigger("ebay-listing-profile-update", [_self.profile.id]);
                    data.panel.clearDirty();
                }
            }
        });
        if(typeof app.tabs.editEbayListingProfile.tabInitFunctions[data.index] == "function"){
            app.tabs.editEbayListingProfile.tabInitFunctions[data.index].call(this, data.panel);
        }
    };
})();

app.editProduct.tabInitFunctions.ebayProfile = function(panel) {
    var listOnEbayBtn = panel.find(".list-on-ebay");
    listOnEbayBtn.on("click", function() {
        if(listOnEbayBtn.is("[disabled]")) {
            return;
        } else {
            listOnEbayBtn.attr("disabled", "disabled")
        }
        var btnText = listOnEbayBtn.text();
        listOnEbayBtn.text("Listing...")
        var productId = panel.find("[name=productId]").val();
        var profileId = panel.find("[name=profile]").val();
        if(!profileId) {
            bm.notify($.i18n.prop("please.select.profile"), "alert")
        }
        bm.ajax({
            url: app.baseUrl + "ebayListingAdmin/listProductOnEbay",
            data: {
                profileId: profileId,
                productId: productId
            },
            success: function(resp) {},
            complete: function() {
                listOnEbayBtn.removeAttr("disabled");
                listOnEbayBtn.text(btnText);
            }

        })
    })
}

app.editCategory.tabInitFuncs.ebayProfile = function(panel) {
    var listOnEbayBtn = panel.find(".list-on-ebay");
    listOnEbayBtn.on("click", function() {
        if(listOnEbayBtn.is("[disabled]")) {
            return;
        } else {
            listOnEbayBtn.attr("disabled", "disabled")
        }
        var btnText = listOnEbayBtn.text();
        listOnEbayBtn.text("Listing...")
        var categoryId = panel.find("[name=categoryId]").val();
        var profileId = panel.find("[name=profile]").val();
        if(!profileId) {
            bm.notify($.i18n.prop("please.select.profile"), "alert")
        }
        bm.ajax({
            url: app.baseUrl + "ebayListingAdmin/listCategoryOnEbay",
            data: {
                profileId: profileId,
                categoryId: categoryId
            },
            success: function(resp) {},
            complete: function() {
                listOnEbayBtn.removeAttr("disabled");
                listOnEbayBtn.text(btnText);
            }
        })
    })
}
