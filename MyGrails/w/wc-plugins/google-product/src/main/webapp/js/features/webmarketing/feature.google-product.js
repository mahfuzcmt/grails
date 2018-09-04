/**
 * Created by sajed on 5/29/2014.
 */
app.tabs.googleProduct = function () {
    this.text = $.i18n.prop("google.product");
    this.tip = $.i18n.prop("manage.google.products");
    this.ui_class = "google-product";
    this.ui_body_class = "simple-tab";
    this.ajax_url = app.baseUrl + "googleProductAdmin/loadAppView";
    app.tabs.googleProduct._super.constructor.apply(this, arguments);
};

app.ribbons.web_marketing.push({
    text: $.i18n.prop("google.product"),
    processor: app.tabs.googleProduct,
    ui_class: "google-product",
    license: "allow_google_product_feature",
    ecommerce: true
});

var _g = app.tabs.googleProduct.inherit(app.Tab);
_g.init = function() {
    var _self = this;
    var panel = _self.body;
    _self.localCategoryTree = panel.find(".local-category-tree");
    _self.googleCategoryTree = panel.find(".google-category-tree");
    var body = _self.body.find(".body")
    _self.fetchLocalCategory()
    _self.fetchGoogleCategory()
    var typeSelector = panel.find(".category-type-selector")
    typeSelector.on("change", function() {
        _self.fetchLocalCategory($(this).val());
    });
    app.tabs.googleProduct._super.init.apply(this, arguments)
    panel.find(".search-form").form({
        preSubmit: function() {
            var form = $(this);
            var text = form.find(".search-text").val();
            _self.currentSearchPointer = 0;
            _self.seachText = text;
            if(text) {
                _self.searchGoogleTree(_self.googleTreeInst.getRoot(), 1);
            }
            return false;
        }
    });
    panel.find(".search-indicator .next").on("click", function() {
        var toPoint = _self.currentSearchPointer + 1;
        _self.currentSearchPointer = 0;
        if(_self.seachText) {
            _self.searchGoogleTree(_self.googleTreeInst.getRoot(), toPoint);
        }
    });
    panel.find(".search-indicator .previous").on("click", function() {
        if(_self.seachText && _self.currentSearchPointer > 1) {
            var toPoint = _self.currentSearchPointer - 1;
            _self.currentSearchPointer = 0;
            _self.searchGoogleTree(_self.googleTreeInst.getRoot(), toPoint);
        }
    });
}

_g.action_menu_entries = [
    {
        text: $.i18n.prop("download.feed"),
        ui_class: "download-feed",
        action: "download-feed"
    },
    {
        text: $.i18n.prop("view.feed"),
        ui_class: "view",
        action: "view-feed"
    },
    {
        text: $.i18n.prop("update.google.category"),
        ui_class: "update-google-category",
        action: "update-google-category"
    },
    {
        text: $.i18n.prop("google.product.config"),
        ui_class: "google-product-config config",
        action: "google-product-config"
    }
];

_g.onActionMenuClick = function(action) {
    switch (action) {
        case "download-feed":
            window.open(app.baseUrl + "googleProductAdmin/download");
            break;
        case "view-feed":
            window.open(app.siteBaseUrl + "googleProduct/feed");
            break;
        case "update-google-category":
            this.updateGoogleCategory();
            break;
        case "google-product-config":
            ComponentManager.openTab(ComponentManager.getRibbonData(app.ribbons.administration, "settings"), {active: "googleProduct"});
            break;
    }
}

_g.currentSearchPointer = 0;
_g.seachText = "";

_g.updateGoogleCategory = function() {
    var _self = this;
    _self.googleCategoryTree.loader();
    bm.ajax({
        url: app.baseUrl + "googleProductAdmin/updateCategory",
        response: function() {
            _self.googleCategoryTree.loader(false);
        },
        success: function() {
            _self.fetchGoogleCategory();
        }
    })
}

_g.fetchLocalCategory = function(type){
    var _self = this;
    if (_self.localTreeInitialized) {
        _self.localCategoryTree.tree("destroy");
    }
    if(_self.googleTreeInst) {
        _self.googleTreeInst.removeHilightedNodes();
        var  activeNode = _self.googleTreeInst.getActiveNode();
        if(activeNode) {
            activeNode.deactivate();
        }
    }
    _self.localCategoryTree.addClass("updating").loader()
    bm.ajax({
        url: app.baseUrl + "googleProductAdmin/categoryTree",
        data: {type: type},
        success: function(children) {
            buildTree(children)
        },
        error: function() {
            buildTree([])
        }
    }).always(function() {
        _self.localCategoryTree.loader(false)
    })
    function buildTree(children) {
        _self.localTreeInitialized = true;
        _self.localCategoryTree.tree({
            type_prop: "type",
            auto_load_lazy_nodes: true,
            load_url: app.baseUrl + "googleProductAdmin/categoryTree",
            children : children,
            onClick: function(node, event) {
                if(event.ctrlKey) {
                    var googleTreeActiveNode = _self.googleTreeInst.activeNode
                    if(googleTreeActiveNode) {
                        googleTreeActiveNode.deactivate()
                    }
                    _self.localTreeInst.removeHilightedNodes();
                }
            },
            onActivate: function(node) {
                var googleActiveNode = _self.googleTreeInst.activeNode
                if(googleActiveNode && node.isHighlighted) {
                    _self.removeMapping(node, googleActiveNode, true);
                } else if(googleActiveNode) {
                    _self.mapCategory(node, googleActiveNode, true)
                } else {
                    _self.highlightGoogleTree(node);
                }

            },
            onContextMenu: function (node, evt) {

            },
            onExpand: function(flag, node) {
                if(!flag) {
                    var googleActiveNode = _self.googleTreeInst.activeNode;
                    var activeNode = _self.localTreeInst.activeNode
                    if(!googleActiveNode && !activeNode) {
                        _self.googleTreeInst.removeHilightedNodes();
                    }
                }
            },
            onRender: function(node, span) {
                if(node.data.mapping) {
                    $(span).addClass("mapped")
                }
            }
        })
        _self.localTreeInst = _self.localCategoryTree.tree("inst")
        _self.localCategoryTree.scrollbar();
    }
}

_g.highlightGoogleTree = function(localNode) {
    var _self = this
    var mapping = localNode.data.mapping
    _self.googleTreeInst.removeHilightedNodes();
    if(mapping) {
        var node = _self.mappingToNode(mapping);
        node.makeVisible();
        node.highlight();
        _self.scroll(_self.googleCategoryTree, $(node.span))
    }
}

_g.fetchGoogleCategory = function(){
    var _self = this;
    if (_self.googleTreeInitialized) {
        _self.googleCategoryTree.tree("destroy");
    }

    _self.googleCategoryTree.loader()
    bm.ajax({
        url: app.baseUrl + "googleProductAdmin/googleCategoryTree",
        response: function() {
            _self.googleCategoryTree.loader(false);
        },
        success: function(children) {
            buildTree(children)
        },
        error: function() {
            buildTree([])
        }
    })
    function buildTree(children) {
        _self.googleTreeInitialized = true
        _self.googleCategoryTree.tree({
            type_prop: "type",
            auto_load_lazy_nodes: false,
            children: children,
            onClick: function(node, event) {
                if(event.ctrlKey) {
                    var localTreeActiveNode = _self.localTreeInst.activeNode
                    if(localTreeActiveNode) {
                        localTreeActiveNode.deactivate()
                    }
                    _self.googleTreeInst.removeHilightedNodes();
                }
            },
            onActivate: function(node) {
                var localTreeActiveNode = _self.localTreeInst.activeNode
                _self.localTreeInst.removeHilightedNodes();
                if(localTreeActiveNode && node.isHighlighted) {
                    _self.removeMapping(localTreeActiveNode, node, false);
                } else if(localTreeActiveNode) {
                    _self.mapCategory(localTreeActiveNode, node, false)
                } else {
                    var mapping = _self.nodeToMapping(node);
                    _self.highlightLocalTree(_self.localTreeInst.getRoot(), mapping)
                }
            },
            onContextMenu: function (node, evt) {
            },
            onExpand: function(flag, node) {
                if(!flag) {
                    var localActiveNode = _self.localTreeInst.activeNode;
                    var activeNode = _self.googleTreeInst.activeNode
                    if(!localActiveNode && !activeNode) {
                        _self.localTreeInst.removeHilightedNodes();
                    }
                }
            }
        })
        _self.googleTreeInst = _self.googleCategoryTree.tree("inst");
        _self.googleCategoryTree.scrollbar();
    }
}

_g.highlightLocalTree = function(node, mapping) {
    var _self = this
    if(node.data.mapping == mapping) {
        node.highlight();
        node.makeVisible();
    }
    if(node.childList instanceof Array) {
        node.childList.every(function() {
            _self.highlightLocalTree(this, mapping);
        })
    }

}

_g.nodeToMapping = function(node) {
    var path = node.data.name;
    while(node.parent) {
        node = node.parent
        if(!node.parent) {
            break;
        }
        path = node.data.name + ">" + path;
    }
    return path;
}

_g.mappingToNode = function(path){
    var _self = this;
    var root = _self.googleTreeInst.getRoot()
    path = path.split(">");
    path.every(function() {
        var subPath = this;
        root.childList.every(function() {
            var node = this;
            if(node.data.name == subPath) {
                root = node;
                return
            }
        })
    })
    return root;
};

_g.mapCategory = function(localNode, googleNode, localTree) {
    var _self = this
    var categoryId = localNode.key;
    var googleCategory = _self.nodeToMapping(googleNode);
    bm.ajax({
        url: app.baseUrl + "googleProductAdmin/mapCategory",
        data: {categoryId: categoryId, googleCategory: googleCategory},
        success: function(resp) {
            localNode.data.mapping = googleCategory;
            $(localNode.span).addClass("mapped")
            if(!localTree) {
                _self.googleTreeInst.removeHilightedNodes();
                googleNode.highlight();
            } else {
                localNode.highlight();
            }
        }
    })
    if(!localTree) {
        googleNode.deactivate()
    } else {
        localNode.deactivate();
    }
};

_g.removeMapping = function(localNode, googleNode, localTree) {
    var _self = this
    var categoryId = localNode.key;
    bm.ajax({
        url: app.baseUrl + "googleProductAdmin/removeMapping",
        data: {categoryId: categoryId},
        success: function(resp) {
            localNode.data.mapping = null;
            $(localNode.span).removeClass("mapped")
            if(!localTree) {
                _self.googleTreeInst.removeHilightedNodes();
                googleNode.highlight(false);
            } else {
                localNode.highlight(false);
            }
        }
    })
    if(!localTree) {
        googleNode.deactivate()
    } else {
        localNode.deactivate();
    }
};

_g.searchGoogleTree = function(node, pointer) {
    var _self = this;
    if(pointer == _self.currentSearchPointer) {
        return false;
    }
    if(node.data.name && node.data.name.search(new RegExp(this.seachText, "i")) > -1) {
        this.currentSearchPointer ++;
        if(this.currentSearchPointer == pointer) {
            node.mark();
            _self.scroll(_self.googleCategoryTree, $(node.span))
            return false;
        }
    }
    if(node.childList instanceof Array) {
        node.childList.every(function() {
            return _self.searchGoogleTree(this, pointer);
        })
    }

};

_g.scroll = function(scrollElement, to) {
   var to_scroll = to[0].offsetTop - scrollElement[0].offsetTop
   scrollElement.animate({
       scrollTop: to_scroll
   }, 200);
}

_g.reload = function() {
    var typeSelector = this.body.find(".category-type-selector")
    this.fetchLocalCategory(typeSelector.val());

};