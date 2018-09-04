/**
 * Created by sajed on 5/29/2014.
 */
app.tabs.myShopping = function () {
    this.text = $.i18n.prop("my.shopping");
    this.tip = $.i18n.prop("manage.my.shopping");
    this.ui_class = "my-shopping";
    this.ui_body_class = "simple-tab";
    this.ajax_url = app.baseUrl + "myShoppingAdmin/loadAppView";
    app.tabs.myShopping._super.constructor.apply(this, arguments);
};

app.ribbons.web_marketing.push({
    text: $.i18n.prop("my.shopping"),
    processor: app.tabs.myShopping,
    ui_class: "my-shopping",
    license: "allow_myshopping_feature",
    ecommerce: true
});

var _ms = app.tabs.myShopping.inherit(app.Tab);

(function(){
    var _super = app.tabs.myShopping._super;
    _ms.init = function() {
        var _self = this;
        var panel = _self.body;
        _self.localCategoryTree = panel.find(".local-category-tree");
        _self.myShoppingCategoryTree = panel.find(".my-shopping-category-tree");
        var body = _self.body.find(".body")
        _self.fetchLocalCategory(_self)
        _self.fetchMyShoppingCategory(_self)
        var typeSelector = panel.find(".category-type-selector")
        typeSelector.on("change", function() {
            _self.fetchLocalCategory($(this).val());
        });
        _super.init.apply(this, arguments)

        panel.find(".search-form").form({
            preSubmit: function() {
                var form = $(this);
                var text = form.find(".search-text").val();
                _self.currentSearchPointer = 0;
                _self.seachText = text;
                if(text) {
                    _self.searchMyShoppingTree(_self.myShoppingTreeInst.getRoot(), 1);
                }
                return false;
            }
        });
        panel.find(".search-indicator .next").on("click", function() {
            var toPoint = _self.currentSearchPointer + 1;
            _self.currentSearchPointer = 0;
            if(_self.seachText) {
                _self.searchMyShoppingTree(_self.myShoppingTreeInst.getRoot(), toPoint);
            }
        });
        panel.find(".search-indicator .previous").on("click", function() {
            if(_self.seachText && _self.currentSearchPointer > 1) {
                var toPoint = _self.currentSearchPointer - 1;
                _self.currentSearchPointer = 0;
                _self.searchMyShoppingTree(_self.myShoppingTreeInst.getRoot(), toPoint);
            }
        });
    }
})();

_ms.action_menu_entries = [
    {
        text: $.i18n.prop("download.feed"),
        ui_class: "download download-feed",
        action: "download-feed"
    },
    {
        text: $.i18n.prop("view.feed"),
        ui_class: "view",
        action: "view-feed"
    },
    {
        text: $.i18n.prop("my.shopping.config"),
        ui_class: "my-shopping-config config",
        action: "my-shopping-config"
    }
];

_ms.onActionMenuClick = function(action) {
    switch (action) {
        case "download-feed":
            window.open(app.baseUrl + "myShoppingAdmin/download");
            break;
        case "view-feed":
            window.open(app.siteBaseUrl + "myShopping/feed");
            break;
        case "my-shopping-config":
            ComponentManager.openTab(ComponentManager.getRibbonData(app.ribbons.administration, "settings"), {active: "myShopping"});
            break;
    }
}

_ms.fetchLocalCategory = function(type){
    var _self = this;
    if (_self.localTreeInitialized) {
        _self.localCategoryTree.tree("destroy");
    }
    if(_self.myShoppingTreeInst) {
        _self.myShoppingTreeInst.removeHilightedNodes();
        var  activeNode = _self.myShoppingTreeInst.getActiveNode();
        if(activeNode) {
            activeNode.deactivate();
        }
    }
    _self.localCategoryTree.addClass("updating").loader()
    bm.ajax({
        url: app.baseUrl + "myShoppingAdmin/categoryTree",
        data: {type: type},
        response: function() {
            _self.localCategoryTree.loader(false)
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
            load_url: app.baseUrl + "myShoppingAdmin/categoryTree",
            children : children,
            onClick: function(node, event) {
                if(event.ctrlKey) {
                    var myShoppingTreeActiveNode = _self.myShoppingTreeInst.activeNode
                    if(myShoppingTreeActiveNode) {
                        myShoppingTreeActiveNode.deactivate()
                    }
                    _self.localTreeInst.removeHilightedNodes();
                }
            },
            onActivate: function(node) {
                var myShoppingActiveNode = _self.myShoppingTreeInst.activeNode
                if(myShoppingActiveNode && node.isHighlighted) {
                    _self.removeMapping(node, myShoppingActiveNode, true);
                } else if(myShoppingActiveNode) {
                    _self.mapCategory(node, myShoppingActiveNode, true)
                } else {
                    _self.highlightMyShoppingTree(node);
                }

            },
            onContextMenu: function (node, evt) {

            },
            onExpand: function(flag, node) {
                if(!flag) {
                    var myShoppingActiveNode = _self.myShoppingTreeInst.activeNode;
                    var activeNode = _self.localTreeInst.activeNode
                    if(!myShoppingActiveNode && !activeNode) {
                        _self.myShoppingTreeInst.removeHilightedNodes();
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

_ms.highlightMyShoppingTree = function(localNode) {
    var _self = this
    var mapping = localNode.data.mapping
    _self.myShoppingTreeInst.removeHilightedNodes();
    if(mapping) {
        var node = _self.mappingToNode(mapping);
        node.makeVisible();
        node.highlight();
        _self.scroll(_self.myShoppingCategoryTree, $(node.span))
    }
}


_ms.fetchMyShoppingCategory = function(){
    var _self = this;
    if (_self.myShoppingTreeInitialized) {
        _self.myShoppingCategoryTree.tree("destroy");
    }

    _self.myShoppingCategoryTree.addClass("updating").loader()
    bm.ajax({
        url: app.baseUrl + "myShoppingAdmin/myShoppingCategoryTree",
        success: function(children) {
            buildTree(children)
        },
        error: function() {
            buildTree([])
        }
    })
    function buildTree(children) {
        _self.myShoppingTreeInitialized = true
        _self.myShoppingCategoryTree.tree({
            type_prop: "type",
            auto_load_lazy_nodes: false,
            children: children,
            onClick: function(node, event) {
                if(event.ctrlKey) {
                    var localTreeActiveNode = _self.localTreeInst.activeNode
                    if(localTreeActiveNode) {
                        localTreeActiveNode.deactivate()
                    }
                    _self.myShoppingTreeInst.removeHilightedNodes();
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
                    var activeNode = _self.myShoppingTreeInst.activeNode
                    if(!localActiveNode && !activeNode) {
                        _self.localTreeInst.removeHilightedNodes();
                    }
                }
            }
        })
        _self.myShoppingTreeInst = _self.myShoppingCategoryTree.tree("inst");
        _self.myShoppingCategoryTree.scrollbar();
    }
}

_ms.highlightLocalTree = function(node, mapping) {
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

_ms.nodeToMapping = function(node) {
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

_ms.mappingToNode = function(path){
    var _self = this;
    var root = _self.myShoppingTreeInst.getRoot()
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

_ms.mapCategory = function(localNode, myShoppingNode, localTree) {
    var _self = this
    var categoryId = localNode.key;
    var mapping = _self.nodeToMapping(myShoppingNode);
    bm.ajax({
        url: app.baseUrl + "myShoppingAdmin/mapCategory",
        data: {categoryId: categoryId, path: mapping, category: myShoppingNode.data.name},
        success: function(resp) {
            localNode.data.mapping = mapping;
            $(localNode.span).addClass("mapped")
            if(!localTree) {
                _self.myShoppingTreeInst.removeHilightedNodes();
                myShoppingNode.highlight();
            } else {
                localNode.highlight();
            }
        }
    })
    if(!localTree) {
        myShoppingNode.deactivate()
    } else {
        localNode.deactivate();
    }
};

_ms.removeMapping = function(localNode, myShoppingNode, localTree) {
    var _self = this
    var categoryId = localNode.key;
    bm.ajax({
        url: app.baseUrl + "myShoppingAdmin/removeMapping",
        data: {categoryId: categoryId},
        success: function(resp) {
            localNode.data.mapping = null;
            $(localNode.span).removeClass("mapped")
            if(!localTree) {
                _self.myShoppingTreeInst.removeHilightedNodes();
                myShoppingNode.highlight(false);
            } else {
                localNode.highlight(false);
            }
        }
    })
    if(!localTree) {
        myShoppingNode.deactivate()
    } else {
        localNode.deactivate();
    }
};

_ms.searchMyShoppingTree = function(node, pointer) {
    var _self = this;
    if(pointer == _self.currentSearchPointer) {
        return false;
    }
    if(node.data.name && node.data.name.search(new RegExp(this.seachText, "i")) > -1) {
        this.currentSearchPointer ++;
        if(this.currentSearchPointer == pointer) {
            node.mark();
            _self.scroll(_self.myShoppingCategoryTree, $(node.span));
            return false;
        }
    }
    if(node.childList instanceof Array) {
        node.childList.every(function() {
            return _self.searchMyShoppingTree(this, pointer);
        })
    }
};

_ms.scroll = function(scrollElement, to) {
   var to_scroll = to[0].offsetTop - scrollElement[0].offsetTop
   scrollElement.animate({
       scrollTop: to_scroll
   }, 200);
}

_ms.reload = function() {
    var typeSelector = this.body.find(".category-type-selector");
    this.fetchLocalCategory(typeSelector.val());
};