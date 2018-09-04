
app.tabs.getPrice = function () {
    this.text = $.i18n.prop("get.price");
    this.tip = $.i18n.prop("manage.get.prices");
    this.ui_class = "get-price";
    this.ui_body_class = "simple-tab";
    this.ajax_url = app.baseUrl + "getPriceAdmin/loadAppView";
    app.tabs.getPrice._super.constructor.apply(this, arguments);
};

app.ribbons.web_marketing.push({
    text: $.i18n.prop("get.price"),
    processor: app.tabs.getPrice,
    ui_class: "get-price",
    license: "allow_getprice_feature",
    ecommerce: true
});

var _gp = app.tabs.getPrice.inherit(app.Tab);


(function(){
    _gp.init = function() {
        var _self = this;
        var panel = _self.body;
        _self.localCategoryTree = panel.find(".local-category-tree");
        _self.getPriceCategoryTree = panel.find(".get-price-category-tree");
        var body = _self.body.find(".body")
        _gp.fetchLocalCategory.call(_self)
        _gp.fetchGetPriceCategory.call(_self)
        var typeSelector = panel.find(".category-type-selector")
        typeSelector.on("change", function() {
            _gp.fetchLocalCategory.call(_self, $(this).val());
        });
        panel.find(".search-form").form({
            preSubmit: function() {
                var form = $(this);
                var text = form.find(".search-text").val();
                _self.currentSearchPointer = 0;
                _self.seachText = text;
                if(text) {
                    _self.searchGetPriceTree(_self.getPriceTreeInst.getRoot(), 1);
                }
                return false;
            }
        });
        panel.find(".search-indicator .next").on("click", function() {
            var toPoint = _self.currentSearchPointer + 1;
            _self.currentSearchPointer = 0;
            if(_self.seachText) {
                _self.searchGetPriceTree(_self.getPriceTreeInst.getRoot(), toPoint);
            }
        });
        panel.find(".search-indicator .previous").on("click", function() {
            if(_self.seachText && _self.currentSearchPointer > 1) {
                var toPoint = _self.currentSearchPointer - 1;
                _self.currentSearchPointer = 0;
                _self.searchGetPriceTree(_self.getPriceTreeInst.getRoot(), toPoint);
            }
        });
        app.tabs.getPrice._super.init.apply(this, arguments);
    }
})();

_gp.currentSearchPointer = 0;
_gp.seachText = "";

_gp.searchGetPriceTree = function(node, pointer) {
    var _self = this;
    if(pointer == _self.currentSearchPointer) {
        return false;
    }
    if(node.data.name && node.data.name.search(new RegExp(this.seachText, "i")) > -1) {
        this.currentSearchPointer ++;
        if(this.currentSearchPointer == pointer) {
            node.mark();
            _self.scroll(_self.getPriceCategoryTree, $(node.span));
            return false;
        }
    }
    if(node.childList instanceof Array) {
        node.childList.every(function() {
            return _self.searchGetPriceTree(this, pointer);
        })
    }

};

_gp.action_menu_entries = [
    {
        text: $.i18n.prop("download.product.feed"),
        ui_class: "download-product-feed",
        action: "download-product-feed"
    },
    {
        text: $.i18n.prop("download.category.feed"),
        ui_class: "download-category-feed",
        action: "download-category-feed"
    },
    {
        text: $.i18n.prop("view.product.feed"),
        ui_class: "view",
        action: "view-product-feed"
    },
    {
        text: $.i18n.prop("view.category.feed"),
        ui_class: "view",
        action: "view-category-feed"
    },
    {
        text: $.i18n.prop("update.getprice.category"),
        ui_class: "update-get-price-category",
        action: "update-get-price-category"
    }
];

_gp.onActionMenuClick = function(action) {
    switch (action) {
        case "download-product-feed":
            window.open(app.baseUrl + "getPriceAdmin/downloadFeed?product=true", "_blank");
            break;
        case "download-category-feed":
            window.open(app.baseUrl + "getPriceAdmin/downloadFeed?category=true", "_blank");
            break;
        case "view-product-feed":
            window.open(app.siteBaseUrl + "getPrice/product", "_blank");
            break;
        case "view-category-feed":
            window.open(app.siteBaseUrl + "getPrice/category", "_blank");
            break;
        case "update-get-price-category":
            this.updatePriceAndCategory();
            break;
    }
}

_gp.updatePriceAndCategory = function() {
    var _self = this;
    bm.ajax({
        url: app.baseUrl + "getPriceAdmin/updateCategory",
        success: function(resp) {
            var categoryDom = $(resp.htmlCategory);
            var csvString = "";
            function put(string) {
                csvString += string;
            }
            function getCategoryCSV(categoryDom, parent) {
                categoryDom.each(function() {
                    var _this = $(this);
                    var children = _this.children("a").text();
                    if(children == "See All") {
                        return;
                    }
                    var _parent = parent + "\,\ " + children;
                    put(_parent + "\n");
                    getCategoryCSV(_this.children(".dl-submenu").children("li"), _parent);
                })
            }
            var categoryCSV = categoryDom.children("li").each(function() {
                var $this = $(this);
                var parent = $this.children("a").text();
                put(parent + "\n");
                getCategoryCSV($this.children(".dl-submenu").children("li"), parent);
            })
            bm.ajax({
                url: app.baseUrl + "getPriceAdmin/updateCategory",
                data: {categoryJSON: csvString},
                success: function(resp) {
                    _gp.fetchGetPriceCategory.call(_self);
                }
            })
        }
    })
}

_gp.fetchLocalCategory = function(type) {
    var _self = this;
    if (_self.localTreeInitialized) {
        _self.localCategoryTree.tree("destroy");
    }
    if(_self.getPriceTreeInst) {
        _self.getPriceTreeInst.removeHilightedNodes();
        var  activeNode = _self.getPriceTreeInst.getActiveNode();
        if(activeNode) {
            activeNode.deactivate();
        }
    }
    _self.localCategoryTree.loader();
    bm.ajax({
        url: app.baseUrl + "getPriceAdmin/categoryTree",
        data: {type: type},
        response:function () {
            _self.localCategoryTree.loader(false);
        },
        success: function(children) {
            buildTree(children)
        },
        error: function() {
            buildTree([])
        }
    })
    function buildTree(children) {
        _self.localTreeInitialized = true;
        _self.localCategoryTree.tree({
            type_prop: "type",
            auto_load_lazy_nodes: true,
            load_url: app.baseUrl + "getPriceAdmin/categoryTree",
            children : children,
            onClick: function(node, event) {
                if(event.ctrlKey) {
                    var getPriceTreeActiveNode = _self.getPriceTreeInst.activeNode
                    if(getPriceTreeActiveNode) {
                        getPriceTreeActiveNode.deactivate()
                    }
                    _self.localTreeInst.removeHilightedNodes();
                }
            },
            onActivate: function(node) {
                var getPriceActiveNode = _self.getPriceTreeInst.activeNode
                if(getPriceActiveNode && node.isHighlighted) {
                    _self.removeMapping(node, getPriceActiveNode, true);
                } else if(getPriceActiveNode) {
                    _self.mapCategory(node, getPriceActiveNode, true)
                } else {
                    _self.highlightGetPriceTree(node);
                }

            },
            onContextMenu: function (node, evt) {

            },
            onExpand: function(flag, node) {
                if(!flag) {
                    var getPriceActiveNode = _self.getPriceTreeInst.activeNode;
                    var activeNode = _self.localTreeInst.activeNode
                    if(!getPriceActiveNode && !activeNode) {
                        _self.getPriceTreeInst.removeHilightedNodes();
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

_gp.highlightGetPriceTree = function(localNode) {
    var _self = this
    var mapping = localNode.data.mapping
    _self.getPriceTreeInst.removeHilightedNodes();
    if(mapping) {
        var node = _self.mappingToNode(mapping);
        node.makeVisible();
        node.highlight();
        _self.scroll(_self.getPriceCategoryTree, $(node.span))
    }
}


_gp.fetchGetPriceCategory = function(){
    var _self = this;
    if (_self.getPriceTreeInitialized) {
        _self.getPriceCategoryTree.tree("destroy");
    }

    _self.getPriceCategoryTree.loader()
    bm.ajax({
        url: app.baseUrl + "getPriceAdmin/getPriceCategoryTree",
        response: function () {
            _self.getPriceCategoryTree.loader(false)
        },
        success: function(children) {
            buildTree(children)
        },
        error: function() {
            buildTree([])
        }
    })
    function buildTree(children) {
        _self.getPriceTreeInitialized = true
        _self.getPriceCategoryTree.tree({
            type_prop: "type",
            auto_load_lazy_nodes: false,
            children: children,
            onClick: function(node, event) {
                if(event.ctrlKey) {
                    var localTreeActiveNode = _self.localTreeInst.activeNode
                    if(localTreeActiveNode) {
                        localTreeActiveNode.deactivate()
                    }
                    _self.getPriceTreeInst.removeHilightedNodes();
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
                    var activeNode = _self.getPriceTreeInst.activeNode
                    if(!localActiveNode && !activeNode) {
                        _self.localTreeInst.removeHilightedNodes();
                    }
                }
            }
        })
        _self.getPriceTreeInst = _self.getPriceCategoryTree.tree("inst");
        _self.getPriceCategoryTree.scrollbar();
    }
}

_gp.highlightLocalTree = function(node, mapping) {
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

_gp.nodeToMapping = function(node) {
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

_gp.mappingToNode = function(path){
    var _self = this;
    var root = _self.getPriceTreeInst.getRoot()
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

_gp.mapCategory = function(localNode, getPriceNode, localTree) {
    var _self = this
    var categoryId = localNode.key;
    var getPriceCategory = _self.nodeToMapping(getPriceNode);
    bm.ajax({
        url: app.baseUrl + "getPriceAdmin/mapCategory",
        data: {categoryId: categoryId, getPriceCategory: getPriceCategory},
        success: function(resp) {
            localNode.data.mapping = getPriceCategory;
            $(localNode.span).addClass("mapped")
            if(!localTree) {
                _self.getPriceTreeInst.removeHilightedNodes();
                getPriceNode.highlight();
            } else {
                localNode.highlight();
            }
        }
    })
    if(!localTree) {
        getPriceNode.deactivate()
    } else {
        localNode.deactivate();
    }
};

_gp.removeMapping = function(localNode, getPriceNode, localTree) {
    var _self = this
    var categoryId = localNode.key;
    bm.ajax({
        url: app.baseUrl + "getPriceAdmin/removeMapping",
        data: {categoryId: categoryId},
        success: function(resp) {
            localNode.data.mapping = null;
            $(localNode.span).removeClass("mapped")
            if(!localTree) {
                _self.getPriceTreeInst.removeHilightedNodes();
                getPriceNode.highlight(false);
            } else {
                localNode.highlight(false);
            }
        }
    })
    if(!localTree) {
        getPriceNode.deactivate()
    } else {
        localNode.deactivate();
    }
};

_gp.scroll = function(scrollElement, to) {
   var to_scroll = to[0].offsetTop - scrollElement[0].offsetTop
   scrollElement.animate({
       scrollTop: to_scroll
   }, 200);
}

_gp.reload = function() {
    var typeSelector = this.body.find(".category-type-selector")
    this.fetchLocalCategory(typeSelector.val());
}