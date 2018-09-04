var getWcuiTreePersistData = null;
var DTNodeStatus_Error = -1;
var DTNodeStatus_Loading = 1;
var DTNodeStatus_Ok = 0;
$.wcui_tree_parallel_max_load = 10;
(function ($) {
    var Class = {
        create: function () {
            return function () {
                this.initialize.apply(this, arguments);
            };
        }
    };

    function getDtNodeFromElement(el) {
        return TreeWidget.getNode(el);
    }

    function versionCompare(v1, v2) {
        var v1parts = ("" + v1)
                .split("."),
            v2parts = ("" + v2)
                .split("."),
            minLength = Math.min(v1parts.length, v2parts.length),
            p1, p2, i;
        for (i = 0; i < minLength; i++) {
            p1 = parseInt(v1parts[i], 10);
            p2 = parseInt(v2parts[i], 10);
            if (isNaN(p1)) {
                p1 = v1parts[i];
            }
            if (isNaN(p2)) {
                p2 = v2parts[i];
            }
            if (p1 == p2) {
                continue;
            } else if (p1 > p2) {
                return 1;
            } else if (p1 < p2) {
                return -1;
            }
            return NaN;
        }
        if (v1parts.length === v2parts.length) {
            return 0;
        }
        return(v1parts.length < v2parts.length) ? -1 : 1;
    }

    var WcuiTreeNode = Class.create();
    WcuiTreeNode.prototype = {
        initialize: function (parent, tree, data) {
            this.parent = parent;
            this.tree = tree;
            if (typeof data === "string") {
                data = {
                    title: data
                };
            }
            tree._nodeCount++
            this.data = $.extend({}, TreeWidget.nodedatadefaults, data);
            this.key = "" + (data[tree.options.key_prop] == undefined ? tree._nodeCount : data[tree.options.key_prop])
            this.el = null;
            this.span = null;
            this.node = null;
            this.childList = null;
            this._isLoading = false;
            this.hasSubSel = false;
            this.bExpanded = false;
            this.bSelected = false;
            this.isHighlighted = false
        },
        toString: function () {
            return "WcuiTreeNode<" + this.key + ">: '" + this.data[this.options.title_prop] + "'";
        },
        toDict: function (recursive, callback) {
            var dict = $.extend({}, this.data);
            dict.activate = (this.tree.activeNode === this);
            dict.focus = (this.tree.focusNode === this);
            dict.expand = this.bExpanded;
            dict.select = this.bSelected;
            if (callback) {
                callback(dict);
            }
            if (recursive && this.childList) {
                dict.children = [];
                for (var i = 0, l = this.childList.length; i < l; i++) {
                    dict.children.push(this.childList[i].toDict(true, callback));
                }
            } else {
                delete dict.children;
            }
            return dict;
        },
        redraw: function () {
            if (this.is_rendered) {
                this.render(false);
            }
        },
        fromDict: function (dict) {
            var children = dict.children;
            if (children === undefined) {
                this.data = $.extend(this.data, dict);
                this.render();
                return;
            }
            dict = $.extend({}, dict);
            dict.children = undefined;
            this.data = $.extend(this.data, dict);
            this.removeChildren();
            this.addChild(children);
        },
        _getInnerHtml: function () {
            var tree = this.tree,
                opts = tree.options,
                cache = tree.cache,
                level = this.getLevel(),
                data = this.data,
                res = "",
                imageSrc;
            if (level < opts.minExpandLevel) {
                if (level > 1) {
                    res += cache.tagConnector;
                }
            } else if (this.hasChildren() !== false) {
                res += cache.tagExpander;
            } else {
                res += cache.tagConnector;
            }
            if (opts.checkbox && data.hideCheckbox !== true && !data.isStatusNode) {
                res += cache.tagCheckbox;
            }
            if (data.icon) {
                if (data.icon.charAt(0) === "/") {
                    imageSrc = data.icon;
                } else {
                    imageSrc = opts.imagePath + data.icon;
                }
                res += "<img src='" + imageSrc + "' alt='' />";
            } else if (data.icon === false) {
            } else if (data.iconClass) {
                res += "<span class='" + " " + data.iconClass + "'></span>";
            } else {
                res += cache.tagNodeIcon;
            }
            var nodeTitle = "";
            if (opts.onCustomRender) {
                nodeTitle = opts.onCustomRender.call(tree, this) || "";
            }
            if (!nodeTitle) {
                var tooltip = data.tooltip ? ' title="' + data.tooltip.replace(/\"/g, '&quot;') + '"' : '',
                    href = data.href || "#";
                if (opts.noLink || data.noLink) {
                    nodeTitle = '<span style="display:inline-block;" class="' + opts.classNames.title + '"' +
                        tooltip + '>' + data[opts.title_prop] + '</span>';
                } else {
                    nodeTitle = '<a href="' + href + '" class="' + opts.classNames.title + '"' + tooltip + '>' +
                        data[opts.title_prop] + '</a>';
                }
            }
            res += nodeTitle;
            return res;
        },
        _fixOrder: function () {
            var cl = this.childList;
            if (!cl || !this.node) {
                return;
            }
            var childLI = this.node.firstChild;
            for (var i = 0, l = cl.length - 1; i < l; i++) {
                var childNode1 = cl[i];
                var childNode2 = childLI.dtnode;
                if (childNode1 !== childNode2) {
                    this.node.insertBefore(childNode1.el, childNode2.el);
                } else {
                    childLI = childLI.nextSibling;
                }
            }
        },
        _createEl: function () {
            var opts = this.tree.options;
            var data = this.data;
            var isLastSib = this.isLastSibling();
            var cn = opts.classNames;
            var tree = this.tree;
            if (!this.el) {
                this.el = document.createElement("div");
                this.el.className = cn.nodeContainer + (isLastSib ? " " + cn.lastsib : "");
                this.el.dtnode = this;
                if (this.key) {
                    this.el.id = opts.idPrefix + this.tree.uuid + "-" + this.key;
                }
                this.span = document.createElement("span");
                this.span.className = cn.title;
                this.el.appendChild(this.span);
            }
            this.span.innerHTML = this._getInnerHtml();

            var cnList = [];
            cnList.push(cn.node);
            if (data.isFolder) {
                cnList.push(cn.folder);
            }
            if (opts.type_prop && data[opts.type_prop]) {
                cnList.push(data[opts.type_prop]);
            } else if (opts.node_type) {
                cnList.push(opts.node_type);
            }
            if (this.bExpanded) {
                cnList.push(cn.expanded);
            }
            if (this.hasChildren() !== false) {
                cnList.push(cn.hasChildren);
                if (this.childList === null) {
                    data.isLazy = true;
                }
            }
            if (data.isLazy && this.childList === null) {
                cnList.push(cn.lazy);
            }
            if (isLastSib) {
                cnList.push(cn.lastsib);
            }
            if (this.bSelected) {
                cnList.push(cn.selected);
            }
            if (this.hasSubSel) {
                cnList.push(cn.partsel);
            }
            if (tree.activeNode === this) {
                cnList.push(cn.active);
            }
            if (data.clazz) {
                cnList.push(data.clazz);
            }
            if(this.isHighlighted) {
                cnList.push(cn.highlighted)
            }
            cnList.push(cn.combinedExpanderPrefix + (this.bExpanded ? "e" : "c") + (data.isLazy && this.childList === null ? "d" : "") + (isLastSib ? "l" : ""));
            cnList.push(cn.combinedIconPrefix + (this.bExpanded ? "e" : "c") + (data.isFolder ? "f" : ""));
            this.span.className = cnList.join(" ");
            if (!this.is_rendered && opts.onCreate) {
                opts.onCreate.call(tree, this, this.span);
            }
            if (opts.onRender) {
                opts.onRender.call(tree, this, this.span);
            }
        },
        render: function (useEffects) {
            var tree = this.tree,
                parent = this.parent,
                opts = tree.options,
                cn = opts.classNames
            if (!parent && !this.node) {
                this.node = document.createElement("div");
                this.node.className = "wcui-tree";
                if (opts.tree_root_data) {
                    this._createEl();
                    var _node = this.node;
                    this.node = document.createElement("div");
                    $(_node).append(this.el)
                    $(this.el).append(this.node);
                } else {
                    this.el = this.key = this.span = null;
                }
                if (opts.minExpandLevel > 1) {
                    this.node.className += " " + cn.container + " " + cn.noConnector;
                } else {
                    this.node.className += " " + cn.container;
                }
            } else if (parent) {
                this._createEl();
                if (!this.is_rendered) {
                    if (!parent.node) {
                        parent._createNode()
                    }
                    parent.node.appendChild(this.el);
                }
            }
            if (this.bExpanded && this.childList) {
                for (var i = 0, l = this.childList.length; i < l; i++) {
                    this.childList[i].render(false);
                }
                this._fixOrder();
            }
            if (this.node) {
                var isHidden = (this.node.style.display === "none");
                var isExpanded = !!this.bExpanded;
                if (useEffects && opts.fx && (isHidden === isExpanded)) {
                    var duration = opts.fx.duration || 200;
                    $(this.node)
                        .animate(opts.fx, duration);
                } else {
                    this.node.style.display = (this.bExpanded || !parent) ? "" : "none";
                }
            }
            this.is_rendered = true;
        },
        getKeyPath: function (excludeSelf) {
            var path = [];
            this.visitParents(function (node) {
                if (node.parent) {
                    path.unshift(node.key);
                }
            }, !excludeSelf);
            return "/" + path.join(this.tree.options.keyPathSeparator);
        },
        getParent: function () {
            return this.parent;
        },
        getChildren: function () {
            if (this.hasChildren() === undefined) {
                return undefined;
            }
            return this.childList;
        },
        hasChildren: function () {
            if (this.data.isLazy) {
                if (this.childList === null || this.childList === undefined) {
                    return undefined;
                } else if (this.childList.length === 0) {
                    return false;
                } else if (this.childList.length === 1 && this.childList[0].isStatusNode()) {
                    return undefined;
                }
            }
            if (!this.childList) {
                return !!this.data[this.tree.options.has_child_prop]
            }
            return true;
        },
        isFirstSibling: function () {
            var p = this.parent;
            return !p || p.childList[0] === this;
        },
        isLastSibling: function () {
            var p = this.parent;
            return !p || p.childList[p.childList.length - 1] === this;
        },
        isLoading: function () {
            return !!this._isLoading;
        },
        getPrevSibling: function () {
            if (!this.parent) {
                return null;
            }
            var ac = this.parent.childList;
            for (var i = 1, l = ac.length; i < l; i++) {
                if (ac[i] === this) {
                    return ac[i - 1];
                }
            }
            return null;
        },
        getNextSibling: function () {
            if (!this.parent) {
                return null;
            }
            var ac = this.parent.childList;
            for (var i = 0, l = ac.length - 1; i < l; i++) {
                if (ac[i] === this) {
                    return ac[i + 1];
                }
            }
            return null;
        },
        isStatusNode: function () {
            return(this.data.isStatusNode === true);
        },
        isChildOf: function (otherNode) {
            return(this.parent && this.parent === otherNode);
        },
        isDescendantOf: function (otherNode) {
            if (!otherNode) {
                return false;
            }
            var p = this.parent;
            while (p) {
                if (p === otherNode) {
                    return true;
                }
                p = p.parent;
            }
            return false;
        },
        countChildren: function () {
            var cl = this.childList;
            if (!cl) {
                return 0;
            }
            var n = cl.length;
            for (var i = 0, l = n; i < l; i++) {
                var child = cl[i];
                n += child.countChildren();
            }
            return n;
        },
        sortChildren: function (cmp, deep) {
            var cl = this.childList;
            if (!cl) {
                return;
            }
            cmp = cmp || function (a, b) {
                var x = a.data[opts.title_prop].toLowerCase(),
                    y = b.data[opts.title_prop].toLowerCase();
                return x === y ? 0 : x > y ? 1 : -1;
            };
            cl.sort(cmp);
            if (deep) {
                for (var i = 0, l = cl.length; i < l; i++) {
                    if (cl[i].childList) {
                        cl[i].sortChildren(cmp, "$norender$");
                    }
                }
            }
            if (deep !== "$norender$") {
                this.render();
            }
        },
        _setStatusNode: function (data) {
            var firstChild = (this.childList ? this.childList[0] : null);
            if (!data) {
                if (firstChild && firstChild.isStatusNode()) {
                    try {
                        if (this.node) {
                            this.node.removeChild(firstChild.el);
                            firstChild.el = null;
                        }
                    } catch (e) {
                    }
                    if (this.childList.length === 1) {
                        this.childList = [];
                    } else {
                        this.childList.shift();
                    }
                }
            } else if (firstChild) {
                data.isStatusNode = true;
                data[this.tree.options.key_prop] = "_statusNode";
                firstChild.data = data;
                firstChild.render();
            } else {
                data.isStatusNode = true;
                data[this.tree.options.key_prop] = "_statusNode";
                this.addChild(data);
            }
        },
        setLazyNodeStatus: function (lts, opts) {
            var tooltip = (opts && opts.tooltip) ? opts.tooltip : null,
                info = (opts && opts.info) ? " (" + opts.info + ")" : "";
            switch (lts) {
                case DTNodeStatus_Ok:
                    this._setStatusNode(null);
                    $(this.span)
                        .removeClass(this.tree.options.classNames.nodeLoading);
                    this._isLoading = false;
                    if (this.tree.options.autoFocus) {
                        if (this === this.tree.tnRoot && this.childList && this.childList.length > 0) {
                            this.childList[0].focus();
                        } else {
                            this.focus();
                        }
                    }
                    break;
                case DTNodeStatus_Loading:
                    this._isLoading = true;
                    $(this.span)
                        .addClass(this.tree.options.classNames.nodeLoading);
                    if (!this.parent) {
                        this._setStatusNode({
                            title: this.tree.options.strings.loading + info,
                            tooltip: tooltip,
                            clazz: this.tree.options.classNames.nodeWait
                        });
                    }
                    break;
                case DTNodeStatus_Error:
                    this._isLoading = false;
                    this._setStatusNode({
                        title: this.tree.options.strings.loadError + info,
                        tooltip: tooltip,
                        clazz: this.tree.options.classNames.nodeError
                    });
                    break;
                default:
                    throw "Bad LazyNodeStatus: '" + lts + "'.";
            }
        },
        _parentList: function (includeRoot, includeSelf) {
            var l = [];
            var dtn = includeSelf ? this : this.parent;
            while (dtn) {
                if (includeRoot || dtn.parent) {
                    l.unshift(dtn);
                }
                dtn = dtn.parent;
            }
            return l;
        },
        getLevel: function () {
            var level = 0;
            var dtn = this.parent;
            while (dtn) {
                level++;
                dtn = dtn.parent;
            }
            return level;
        },
        _getTypeForOuterNodeEvent: function (event) {
            var cns = this.tree.options.classNames;
            var target = event.target;
            if (target.className.indexOf(cns.node) < 0) {
                return null;
            }
            var eventX = event.pageX - target.offsetLeft;
            var eventY = event.pageY - target.offsetTop;
            for (var i = 0, l = target.childNodes.length; i < l; i++) {
                var cn = target.childNodes[i];
                var x = cn.offsetLeft - target.offsetLeft;
                var y = cn.offsetTop - target.offsetTop;
                var nx = cn.clientWidth,
                    ny = cn.clientHeight;
                if (eventX >= x && eventX <= (x + nx) && eventY >= y && eventY <= (y + ny)) {
                    if (cn.className == cns.title) {
                        return "title";
                    } else if (cn.className == cns.expander) {
                        return "expander";
                    } else if (cn.className == cns.checkbox) {
                        return "checkbox";
                    } else if (cn.className == cns.nodeIcon) {
                        return "icon";
                    }
                }
            }
            return "prefix";
        },
        getEventTargetType: function (event) {
            var tcn = event && event.target ? event.target.className : "",
                cns = this.tree.options.classNames;
            if (tcn === cns.title) {
                return "title";
            } else if (tcn === cns.expander) {
                return "expander";
            } else if (tcn === cns.checkbox) {
                return "checkbox";
            } else if (tcn === cns.nodeIcon) {
                return "icon";
            } else if (tcn === cns.empty || tcn === cns.vline || tcn === cns.connector) {
                return "prefix";
            } else if (tcn.indexOf(cns.node) >= 0) {
                return this._getTypeForOuterNodeEvent(event);
            }
            return null;
        },
        isVisible: function () {
            var parents = this._parentList(true, false);
            for (var i = 0, l = parents.length; i < l; i++) {
                if (!parents[i].bExpanded) {
                    return false;
                }
            }
            return true;
        },
        makeVisible: function () {
            var parents = this._parentList(true, false);
            for (var i = 0, l = parents.length; i < l; i++) {
                parents[i]._expand(true);
            }
        },
        focus: function () {
            this.makeVisible();
            try {
                $(this.span)
                    .find(">a")
                    .focus();
            } catch (e) {
            }
        },
        isFocused: function () {
            return(this.tree.tnFocused === this);
        },
        _activate: function (flag, fireEvents) {
            var opts = this.tree.options;
            if (this.data.isStatusNode) {
                return;
            }
            if (fireEvents && opts.onQueryActivate && opts.onQueryActivate.call(this.tree, flag, this) === false) {
                return;
            }
            if (flag) {
                if (this.tree.activeNode) {
                    if (this.tree.activeNode === this) {
                        return;
                    }
                    this.tree.activeNode.deactivate();
                }
                if (opts.activeVisible) {
                    this.makeVisible();
                }
                this.tree.activeNode = this;
                if (opts.persist) {
                    $.cookie(opts.cookieId + "-active", this.key, opts.cookie);
                }
                this.tree.persistence.activeKey = this.key;
                $(this.span)
                    .addClass(opts.classNames.active);
                if (fireEvents && opts.onActivate) {
                    opts.onActivate.call(this.tree, this);
                }
            } else {
                if (this.tree.activeNode === this) {
                    if (opts.onQueryActivate && opts.onQueryActivate.call(this.tree, false, this) === false) {
                        return;
                    }
                    $(this.span)
                        .removeClass(opts.classNames.active);
                    if (opts.persist) {
                        $.cookie(opts.cookieId + "-active", "", opts.cookie);
                    }
                    this.tree.persistence.activeKey = null;
                    this.tree.activeNode = null;
                    if (fireEvents && opts.onDeactivate) {
                        opts.onDeactivate.call(this.tree, this);
                    }
                }
            }
        },
        mark: function (flag) {
            flag = (flag !== false);
            var opts = this.tree.options;
            if(flag) {
                this.makeVisible()
                var span = $(this.span);
                if(span.hasClass(opts.classNames.marked)) {
                    return;
                }
                if(this.tree.markedNode) {
                    this.tree.markedNode.mark(false)
                }
                span.addClass(opts.classNames.marked)
                this.isMarkedNode = true;
                this.tree.markedNode = this
            } else {
                var span = $(this.span);
                this.isMarkedNode = false;
                span.removeClass(opts.classNames.marked)
                this.tree.markedNode = null;
            }
        },
        _createNode: function () {
            var tree = this.tree,
                opts = tree.options,
                cn = opts.classNames
            this.node = document.createElement("div");
            this.node.style.display = "none";
            this.el.appendChild(this.node);
            if (opts.minExpandLevel > this.getLevel()) {
                this.node.className = cn.container + " " + cn.noConnector;
            } else {
                this.node.className = cn.container;
            }
        },
        activate: function (isSilent) {
            this._activate(true, !isSilent);
        },
        _highlight: function(flag) {
            var opts = this.tree.options;
            var span = $(this.span);
            if(flag) {
                if(span.hasClass(opts.classNames.highlighted)) {
                    return;
                }
                span.addClass(opts.classNames.highlighted)
                this.isHighlighted = true;
                this.tree.hilightedNodes.push(this)
            } else {
                this.isHighlighted = false;
                span.removeClass(opts.classNames.highlighted)
                this.tree.hilightedNodes.remove(this)
            }
        },
        highlight: function(flag) {
            if(flag === false) {
                this._highlight(false);
            } else {
                this._highlight(true);
            }
        },
        deactivate: function (isSilent) {
            this._activate(false, !isSilent);
        },
        isActive: function () {
            return(this.tree.activeNode === this);
        },
        _userActivate: function () {
            var activate = true;
            var expand = false;
            if (this.data.isFolder) {
                switch (this.tree.options.clickFolderMode) {
                    case 2:
                        activate = false;
                        expand = true;
                        break;
                    case 3:
                        activate = expand = true;
                        break;
                }
            }
            if (this.parent === null) {
                expand = false;
            }
            if (expand) {
                this.toggleExpand();
                this.focus();
            }
            if (activate) {
                this.activate();
            }
        },
        _setSubSel: function (hasSubSel) {
            if (hasSubSel) {
                this.hasSubSel = true;
                $(this.span)
                    .addClass(this.tree.options.classNames.partsel);
            } else {
                this.hasSubSel = false;
                $(this.span)
                    .removeClass(this.tree.options.classNames.partsel);
            }
        },
        _updatePartSelectionState: function () {
            var sel;
            if (!this.hasChildren()) {
                sel = (this.bSelected && !this.data.unselectable && !this.data.isStatusNode);
                this._setSubSel(false);
                return sel;
            }
            var i, l,
                cl = this.childList,
                allSelected = true,
                allDeselected = true;
            for (i = 0, l = cl.length; i < l; i++) {
                var n = cl[i],
                    s = n._updatePartSelectionState();
                if (s !== false) {
                    allDeselected = false;
                }
                if (s !== true) {
                    allSelected = false;
                }
            }
            if (allSelected) {
                sel = true;
            } else if (allDeselected) {
                sel = false;
            } else {
                sel = undefined;
            }
            this._setSubSel(sel === undefined);
            this.bSelected = (sel === true);
            return sel;
        },
        _fixSelectionState: function () {
            var p, i, l;
            if (this.bSelected) {
                this.visit(function (node) {
                    node.parent._setSubSel(true);
                    if (!node.data.unselectable) {
                        node._select(true, false, false);
                    }
                });
                p = this.parent;
                while (p) {
                    p._setSubSel(true);
                    var allChildsSelected = true;
                    for (i = 0, l = p.childList.length; i < l; i++) {
                        var n = p.childList[i];
                        if (!n.bSelected && !n.data.isStatusNode && !n.data.unselectable) {
                            allChildsSelected = false;
                            break;
                        }
                    }
                    if (allChildsSelected) {
                        p._select(true, false, false);
                    }
                    p = p.parent;
                }
            } else {
                this._setSubSel(false);
                this.visit(function (node) {
                    node._setSubSel(false);
                    node._select(false, false, false);
                });
                p = this.parent;
                while (p) {
                    p._select(false, false, false);
                    var isPartSel = false;
                    for (i = 0, l = p.childList.length; i < l; i++) {
                        if (p.childList[i].bSelected || p.childList[i].hasSubSel) {
                            isPartSel = true;
                            break;
                        }
                    }
                    p._setSubSel(isPartSel);
                    p = p.parent;
                }
            }
        },
        _select: function (sel, fireEvents, deep) {
            var opts = this.tree.options;
            if (this.data.isStatusNode) {
                return;
            }
            if (this.bSelected === sel) {
                return;
            }
            if (fireEvents && opts.onQuerySelect && opts.onQuerySelect.call(this.tree, sel, this) === false) {
                return;
            }
            if (opts.selectMode == 1 && sel) {
                this.tree.visit(function (node) {
                    if (node.bSelected) {
                        node._select(false, false, false);
                        return false;
                    }
                });
            }
            this.bSelected = sel;
            if (sel) {
                if (opts.persist) {
                    this.tree.persistence.addSelect(this.key);
                }
                $(this.span)
                    .addClass(opts.classNames.selected);
                if (deep && opts.selectMode === 3) {
                    this._fixSelectionState();
                }
                if (fireEvents && opts.onSelect) {
                    opts.onSelect.call(this.tree, true, this);
                }
            } else {
                if (opts.persist) {
                    this.tree.persistence.clearSelect(this.key);
                }
                $(this.span)
                    .removeClass(opts.classNames.selected);
                if (deep && opts.selectMode === 3) {
                    this._fixSelectionState();
                }
                if (fireEvents && opts.onSelect) {
                    opts.onSelect.call(this.tree, false, this);
                }
            }
        },
        select: function (sel) {
            if (this.data.unselectable) {
                return this.bSelected;
            }
            return this._select(sel !== false, true, true);
        },
        toggleSelect: function () {
            return this.select(!this.bSelected);
        },
        isSelected: function () {
            return this.bSelected;
        },
        isLazy: function () {
            return !!this.data.isLazy;
        },
        _loadContent: function (renderChilds, callbacks, topInQueue) {
            callbacks = callbacks || {};
            try {
                var opts = this.tree.options;
                this.setLazyNodeStatus(DTNodeStatus_Loading);
                if (opts.onLazyRead) {
                    if (true === opts.onLazyRead.call(this.tree, this, callbacks)) {
                        this.setLazyNodeStatus(DTNodeStatus_Ok);
                        if (callbacks.done) {
                            callbacks.done()
                        }
                    }
                } else {
                    if (this.data.load_url || opts.load_url) {
                        this.appendAjax({
                            renderChilds: renderChilds,
                            url: this.data.load_url || opts.load_url,
                            data: {
                                key: this.key
                            },
                            success: callbacks.done,
                            error: callbacks.fail
                        }, topInQueue)
                    }
                }
            } catch (e) {
                this.setLazyNodeStatus(DTNodeStatus_Error, {
                    tooltip: "" + e
                });
                if (callbacks.fail) {
                    callbacks.fail()
                }
            }
        },
        _expand: function (bExpand, forceSync) {
            if (this.bExpanded === bExpand) {
                return;
            }
            var opts = this.tree.options;
            if (!bExpand && this.getLevel() < opts.minExpandLevel) {
                return;
            }
            if (opts.onQueryExpand && opts.onQueryExpand.call(this.tree, bExpand, this) === false) {
                return;
            }
            this.bExpanded = bExpand;
            if (opts.persist) {
                if (bExpand) {
                    this.tree.persistence.addExpand(this.key);
                } else {
                    this.tree.persistence.clearExpand(this.key);
                }
            }
            var allowEffects = !(this.data.isLazy && this.childList === null) && !this._isLoading && !forceSync;
            this.render(allowEffects);
            if (this.bExpanded && this.parent && opts.autoCollapse) {
                var parents = this._parentList(false, true);
                for (var i = 0, l = parents.length; i < l; i++) {
                    parents[i].collapseSiblings();
                }
            }
            if (opts.activeVisible && this.tree.activeNode && !this.tree.activeNode.isVisible()) {
                this.tree.activeNode.deactivate();
            }
            if (bExpand && this.data.isLazy && this.childList === null && !this._isLoading) {
                this._loadContent(undefined, undefined, true);
                return;
            }
            if (opts.onExpand) {
                opts.onExpand.call(this.tree, bExpand, this);
            }
        },
        isExpanded: function () {
            return this.bExpanded;
        },
        expand: function (flag) {
            flag = (flag !== false);
            if (!this.childList && !this.data.isLazy && flag) {
                return;
            } else if (this.parent === null && !flag) {
                return;
            }
            this._expand(flag);
        },
        scheduleAction: function (mode, ms) {
            if (this.tree.timer) {
                clearTimeout(this.tree.timer);
            }
            var self = this;
            switch (mode) {
                case "cancel":
                    break;
                case "expand":
                    this.tree.timer = setTimeout(function () {
                        self.expand(true);
                    }, ms);
                    break;
                case "activate":
                    this.tree.timer = setTimeout(function () {
                        self.activate();
                    }, ms);
                    break;
                default:
                    throw "Invalid mode " + mode;
            }
        },
        toggleExpand: function () {
            this.expand(!this.bExpanded);
        },
        collapseSiblings: function () {
            if (this.parent === null) {
                return;
            }
            var ac = this.parent.childList;
            for (var i = 0, l = ac.length; i < l; i++) {
                if (ac[i] !== this && ac[i].bExpanded) {
                    ac[i]._expand(false);
                }
            }
        },
        _onClick: function (event) {
            var targetType = this.getEventTargetType(event);
            if (targetType === "expander") {
                this.toggleExpand();
                this.focus();
            } else if (targetType === "checkbox") {
                this.toggleSelect();
                this.focus();
            } else {
                this._userActivate();
                var aTag = this.span.getElementsByTagName("a");
                if (aTag[0]) {
                    if (!(browser.ie && browser.version < 9)) {
                        aTag[0].focus();
                    }
                } else {
                    return true;
                }
            }
        },
        _onKeydown: function (event) {
            var handled = true,
                sib;
            switch (event.which) {
                case 107:
                case 187:
                    if (!this.bExpanded) {
                        this.toggleExpand();
                    }
                    break;
                case 109:
                case 189:
                    if (this.bExpanded) {
                        this.toggleExpand();
                    }
                    break;
                case 32:
                    this._userActivate();
                    break;
                case 8:
                    if (this.parent) {
                        this.parent.focus();
                    }
                    break;
                case 37:
                    if (this.bExpanded) {
                        this.toggleExpand();
                        this.focus();
                    } else if (this.parent && this.parent.parent) {
                        this.parent.focus();
                    }
                    break;
                case 39:
                    if (!this.bExpanded && (this.childList || this.data.isLazy)) {
                        this.toggleExpand();
                        this.focus();
                    } else if (this.childList) {
                        this.childList[0].focus();
                    }
                    break;
                case 38:
                    sib = this.getPrevSibling();
                    while (sib && sib.bExpanded && sib.childList) {
                        sib = sib.childList[sib.childList.length - 1];
                    }
                    if (!sib && this.parent && this.parent.parent) {
                        sib = this.parent;
                    }
                    if (sib) {
                        sib.focus();
                    }
                    break;
                case 40:
                    if (this.bExpanded && this.childList) {
                        sib = this.childList[0];
                    } else {
                        var parents = this._parentList(false, true);
                        for (var i = parents.length - 1; i >= 0; i--) {
                            sib = parents[i].getNextSibling();
                            if (sib) {
                                break;
                            }
                        }
                    }
                    if (sib) {
                        sib.focus();
                    }
                    break;
                default:
                    handled = false;
            }
            if (handled) {
                event.preventDefault();
            }
        },
        _onFocus: function (event) {
            var opts = this.tree.options;
            if (event.type == "blur" || event.type == "focusout") {
                if (opts.onBlur) {
                    opts.onBlur.call(this.tree, this);
                }
                if (this.tree.tnFocused) {
                    $(this.tree.tnFocused.span)
                        .removeClass(opts.classNames.focused);
                }
                this.tree.tnFocused = null;
                if (opts.persist) {
                    $.cookie(opts.cookieId + "-focus", "", opts.cookie);
                }
            } else if (event.type == "focus" || event.type == "focusin") {
                if (this.tree.tnFocused && this.tree.tnFocused !== this) {
                    $(this.tree.tnFocused.span)
                        .removeClass(opts.classNames.focused);
                }
                this.tree.tnFocused = this;
                if (opts.onFocus) {
                    opts.onFocus.call(this.tree, this);
                }
                $(this.tree.tnFocused.span)
                    .addClass(opts.classNames.focused);
                if (opts.persist) {
                    $.cookie(opts.cookieId + "-focus", this.key, opts.cookie);
                }
            }
        },
        visit: function (fn, includeSelf) {
            var res = true;
            if (includeSelf === true) {
                res = fn(this);
                if (res === false || res == "skip") {
                    return res;
                }
            }
            if (this.childList) {
                for (var i = 0, l = this.childList.length; i < l; i++) {
                    res = this.childList[i].visit(fn, true);
                    if (res === false) {
                        break;
                    }
                }
            }
            return res;
        },
        visitParents: function (fn, includeSelf) {
            if (includeSelf && fn(this) === false) {
                return false;
            }
            var p = this.parent;
            while (p) {
                if (fn(p) === false) {
                    return false;
                }
                p = p.parent;
            }
            return true;
        },
        remove: function () {
            if (this === this.tree.root) {
                throw "Cannot remove system root";
            }
            return this.parent.removeChild(this);
        },
        removeChild: function (tn) {
            var ac = this.childList;
            if (ac.length == 1) {
                if (tn !== ac[0]) {
                    throw "removeChild: invalid child";
                }
                return this.removeChildren();
            }
            if (tn === this.tree.activeNode) {
                tn.deactivate();
            }
            if (this.tree.options.persist) {
                if (tn.bSelected) {
                    this.tree.persistence.clearSelect(tn.key);
                }
                if (tn.bExpanded) {
                    this.tree.persistence.clearExpand(tn.key);
                }
            }
            tn.removeChildren(true);
            if (this.node) {
                this.node.removeChild(tn.el);
            }
            for (var i = 0, l = ac.length; i < l; i++) {
                if (ac[i] === tn) {
                    this.childList.splice(i, 1);
                    break;
                }
            }
        },
        removeChildren: function (isRecursiveCall, retainPersistence) {
            var tree = this.tree;
            var ac = this.childList;
            if (ac) {
                for (var i = 0, l = ac.length; i < l; i++) {
                    var tn = ac[i];
                    if (tn === tree.activeNode && !retainPersistence) {
                        tn.deactivate();
                    }
                    if (this.tree.options.persist && !retainPersistence) {
                        if (tn.bSelected) {
                            this.tree.persistence.clearSelect(tn.key);
                        }
                        if (tn.bExpanded) {
                            this.tree.persistence.clearExpand(tn.key);
                        }
                    }
                    tn.removeChildren(true, retainPersistence);
                    if (this.node) {
                        $("div." + this.tree.options.classNames.nodeContainer, $(this.node))
                            .remove();
                    }
                }
                this.childList = null;
            }
            if (!isRecursiveCall) {
                this._isLoading = false;
                this.render();
            }
        },
        setTitle: function (title) {
            this.fromDict({
                title: title
            });
        },
        reloadChildren: function (callback) {
            if (this.parent === null) {
                throw "Use tree.reload() instead";
            } else if (!this.data.isLazy) {
                throw "node.reloadChildren() requires lazy nodes.";
            }
            if (callback) {
                var self = this;
                var eventType = "nodeLoaded.tree." + this.tree.$tree.attr("id") + "." + this.key;
                this.tree.$tree.bind(eventType, function (e, node, isOk) {
                    self.tree.$tree.unbind(eventType);
                    if (node !== self) {
                        throw "got invalid load event";
                    }
                    callback.call(self.tree, node, isOk);
                });
            }
            this.removeChildren();
            this._loadContent();
        },
        _loadKeyPath: function (keyPath, callback) {
            var tree = this.tree;
            if (keyPath === "") {
                throw "Key path must not be empty";
            }
            var segList = keyPath.split(tree.options.keyPathSeparator);
            if (segList[0] === "") {
                throw "Key path must be relative (don't start with '/')";
            }
            var seg = segList.shift();
            if (this.childList) {
                for (var i = 0, l = this.childList.length; i < l; i++) {
                    var child = this.childList[i];
                    if (child.key === seg) {
                        if (segList.length === 0) {
                            callback.call(tree, child, "ok");
                        } else if (child.data.isLazy && (child.childList === null || child.childList === undefined)) {
                            var self = this;
                            child.reloadChildren(function (node, isOk) {
                                if (isOk) {
                                    callback.call(tree, child, "loaded");
                                    node._loadKeyPath(segList.join(tree.options.keyPathSeparator), callback);
                                } else {
                                    callback.call(tree, child, "error");
                                }
                            });
                        } else {
                            callback.call(tree, child, "loaded");
                            child._loadKeyPath(segList.join(tree.options.keyPathSeparator), callback);
                        }
                        return;
                    }
                }
            }
            callback.call(tree, undefined, "notfound", seg, segList.length === 0);
            return;
        },
        resetLazy: function () {
            if (this.parent === null) {
                throw "Use tree.reload() instead";
            } else if (!this.data.isLazy) {
                throw "node.resetLazy() requires lazy nodes.";
            }
            this.expand(false);
            this.removeChildren();
        },
        _addChildNode: function (dtnode, beforeNode) {
            var tree = this.tree,
                opts = tree.options,
                pers = tree.persistence;
            dtnode.parent = this;
            if (this.childList === null) {
                this.childList = [];
            } else if (!beforeNode) {
                if (this.childList.length > 0) {
                    $(this.childList[this.childList.length - 1].span)
                        .removeClass(opts.classNames.lastsib);
                }
            }
            if (beforeNode) {
                var iBefore = $.inArray(beforeNode, this.childList);
                if (iBefore < 0) {
                    throw "<beforeNode> must be a child of <this>";
                }
                this.childList.splice(iBefore, 0, dtnode);
            } else {
                this.childList.push(dtnode);
            }
            var isInitializing = tree.isInitializing();
            if (opts.persist && pers.cookiesFound && isInitializing) {
                if (pers.activeKey === dtnode.key) {
                    tree.activeNode = dtnode;
                }
                if (pers.focusedKey === dtnode.key) {
                    tree.focusNode = dtnode;
                }
                dtnode.bExpanded = ($.inArray(dtnode.key, pers.expandedKeyList) >= 0);
                dtnode.bSelected = ($.inArray(dtnode.key, pers.selectedKeyList) >= 0);
            } else {
                if (dtnode.data.activate) {
                    tree.activeNode = dtnode;
                    if (opts.persist) {
                        pers.activeKey = dtnode.key;
                    }
                }
                if (dtnode.data.focus) {
                    tree.focusNode = dtnode;
                    if (opts.persist) {
                        pers.focusedKey = dtnode.key;
                    }
                }
                dtnode.bExpanded = (dtnode.data.expand === true);
                if (dtnode.bExpanded && opts.persist) {
                    pers.addExpand(dtnode.key);
                }
                dtnode.bSelected = (dtnode.data.select === true);
                if (dtnode.bSelected && opts.persist) {
                    pers.addSelect(dtnode.key);
                }
            }
            if (opts.minExpandLevel >= dtnode.getLevel()) {
                this.bExpanded = true;
            }
            if (dtnode.bSelected && opts.selectMode == 3) {
                var p = this;
                while (p) {
                    if (!p.hasSubSel) {
                        p._setSubSel(true);
                    }
                    p = p.parent;
                }
            }
            if (tree.bEnableUpdate) {
                this.render();
            }
            return dtnode;
        },
        addChild: function (obj, beforeNode, renderChilds) {
            if (renderChilds === undefined) {
                renderChilds = true;
            }
            if (typeof (obj) == "string") {
                throw "Invalid data type for " + obj;
            } else if (!obj || obj.length === 0) {
                return;
            } else if (obj instanceof WcuiTreeNode) {
                return [this._addChildNode(obj, beforeNode)];
            }
            if (!obj.length) {
                obj = [obj];
            }
            var prevFlag = this.tree.enableUpdate(false);
            var tns = [];
            for (var i = 0, l = obj.length; i < l; i++) {
                var data = obj[i];
                var dtnode = this._addChildNode(new WcuiTreeNode(this, this.tree, data), beforeNode);
                tns.push(dtnode);
                if (data.children) {
                    dtnode.addChild(data.children);
                }
            }
            if (renderChilds) {
                this.tree.enableUpdate(prevFlag);
            } else {
                this.tree.bEnableUpdate = prevFlag;
            }
            return tns;
        },
        appendAjax: function (ajaxOptions, queuePreferred) {
            function mixinCallbacks() {
                var options = this._ajax_options;
                var _success = options.success;
                var _error = options.error;
                options.success = _success ? function () {
                    _success.apply(this, arguments);
                    ajaxOptions.success.apply(this, arguments);
                } : ajaxOptions.success
                options.error = _error ? function () {
                    _error.apply(this, arguments);
                    ajaxOptions.error.apply(this, arguments);
                } : ajaxOptions.error
                if (queuePreferred) {
                    var key = this.key;
                    var index = this.tree.wcui_tree_queue_manager.index(function() {
                        return key == this.key
                    })
                    if(index) {
                        this.tree.wcui_tree_queue_manager.top(index)
                    }
                }
            }

            if (this._ajax_options) {
                mixinCallbacks.call(this)
                return;
            }
            this._ajax_options = ajaxOptions;
            var queueEntry = {key: this.key, _this: this, caller: queuedAppend};
            this.tree.wcui_tree_queue_manager.push(queueEntry, queuePreferred);
            if (!this.tree.wcui_tree_queue_manager.hasFreeSlot()) {
                return;
            }
            queueEntry = this.tree.wcui_tree_queue_manager.pop()

            function queuedAppend() {
                var self = this;
                this.setLazyNodeStatus(DTNodeStatus_Loading);
                var eventType = "nodeLoaded.tree." + this.tree.$tree.attr("id") + "." + this.key;
                var options = $.extend({}, this.tree.options.ajaxDefaults, this._ajax_options, {
                    response: function() {
                        delete self._ajax_options
                    },
                    success: function (data, textStatus) {
                        self.tree.wcui_tree_queue_manager.dec();
                        var prevPhase = self.tree.phase;
                        self.tree.phase = "init";
                        if (options.postProcess) {
                            data = options.postProcess.call(this, data, this.dataType);
                        } else if (data && data.hasOwnProperty("d")) {
                            data = (typeof data.d) == "string" ? $.parseJSON(data.d) : data.d;
                        }
                        if (!$.isArray(data) || data.length !== 0) {
                            var childs = self.addChild(data, null, ajaxOptions.renderChilds);
                            if (self.tree.options.auto_load_lazy_nodes) {
                                $.each(childs, function () {
                                    if (this.needLazyCall()) {
                                        this._loadContent(false);
                                    }
                                })
                            }
                        }
                        self.redraw()
                        self.tree.phase = "postInit";
                        self.tree.$tree.trigger(eventType, [self, true]);
                        self.tree.phase = prevPhase;
                        if (ajaxOptions.renderChilds !== false) {
                            self.setLazyNodeStatus(DTNodeStatus_Ok);
                        }
                        // cannot be kept in a variable, because it may be changed through mixin after callback
                        if (ajaxOptions.success) {
                            ajaxOptions.success.call(options, self, data, textStatus);
                        }
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        self.tree.wcui_tree_queue_manager.dec();
                        self.tree.$tree.trigger(eventType, [self, false]);
                        self.setLazyNodeStatus(DTNodeStatus_Error, {
                            info: textStatus,
                            tooltip: "" + errorThrown
                        });
                        // cannot be kept in a variable, because it may be changed through mixin after callback
                        if (ajaxOptions.error) {
                            ajaxOptions.error.call(options, self, jqXHR, textStatus, errorThrown);
                        }
                    },
                    complete: function () {
                        self.tree._xhrs.splice(self.tree._xhrs.indexOf(self._xhr), 1);
                        if (self.tree._xhrs.length == 0) {
                            delete self.tree._xhrs;
                        }
                    }
                });
                if (!this.tree._xhrs) {
                    this.tree._xhrs = [];
                }
                this._xhr = this.tree.options.ajax_caller(options);
                this.tree._xhrs.push(this._xhr);
            }

            this.tree.wcui_tree_queue_manager.inc()
            queuedAppend.call(queueEntry._this)
        },
        move: function (targetNode, mode) {
            var pos;
            if (this === targetNode) {
                return;
            }
            if (!this.parent) {
                throw "Cannot move system root";
            }
            if (mode === undefined || mode == "over") {
                mode = "child";
            }
            var prevParent = this.parent;
            var targetParent = (mode === "child") ? targetNode : targetNode.parent;
            if (targetParent.isDescendantOf(this)) {
                throw "Cannot move a node to it's own descendant";
            }
            if (this.parent.childList.length == 1) {
                this.parent.childList = this.parent.data.isLazy ? [] : null;
                this.parent.bExpanded = false;
            } else {
                pos = $.inArray(this, this.parent.childList);
                if (pos < 0) {
                    throw "Internal error";
                }
                this.parent.childList.splice(pos, 1);
            }
            if (this.parent.node) {
                this.parent.node.removeChild(this.el);
            }
            this.parent = targetParent;
            if (targetParent.hasChildren()) {
                switch (mode) {
                    case "child":
                        targetParent.childList.push(this);
                        break;
                    case "before":
                        pos = $.inArray(targetNode, targetParent.childList);
                        if (pos < 0) {
                            throw "Internal error";
                        }
                        targetParent.childList.splice(pos, 0, this);
                        break;
                    case "after":
                        pos = $.inArray(targetNode, targetParent.childList);
                        if (pos < 0) {
                            throw "Internal error";
                        }
                        targetParent.childList.splice(pos + 1, 0, this);
                        break;
                    default:
                        throw "Invalid mode " + mode;
                }
            } else {
                targetParent.childList = [this];
            }
            if (!targetParent.node) {
                targetParent._createNode();
            }
            if (this.el) {
                targetParent.node.appendChild(this.el);
            }
            if (this.tree !== targetNode.tree) {
                this.visit(function (node) {
                    node.tree = targetNode.tree;
                }, null, true);
                throw "Not yet implemented.";
            }
            if (!prevParent.isDescendantOf(targetParent)) {
                prevParent.render();
            }
            if (!targetParent.isDescendantOf(prevParent)) {
                targetParent.render();
            }
        },
        needLazyCall: function () {
            return (this.data.isLazy || this.hasChildren() !== false) && this.childList === null;
        }
    };
    var WcuiTreeStatus = Class.create();
    WcuiTreeStatus._getTreePersistData = function (cookieId, cookieOpts) {
        var ts = new WcuiTreeStatus(cookieId, cookieOpts);
        ts.read();
        return ts.toDict();
    };
    getWcuiTreePersistData = WcuiTreeStatus._getTreePersistData;
    WcuiTreeStatus.prototype = {
        initialize: function (cookieId, cookieOpts) {
            if (cookieId === undefined) {
                cookieId = TreeWidget.prototype.options.cookieId;
            }
            cookieOpts = $.extend({}, TreeWidget.prototype.options.cookie, cookieOpts);
            this.cookieId = cookieId;
            this.cookieOpts = cookieOpts;
            this.cookiesFound = undefined;
            this.activeKey = null;
            this.focusedKey = null;
            this.expandedKeyList = null;
            this.selectedKeyList = null;
        },
        read: function () {
            this.cookiesFound = false;
            var cookie = $.cookie(this.cookieId + "-active");
            this.activeKey = (cookie === null) ? "" : cookie;
            if (cookie !== null) {
                this.cookiesFound = true;
            }
            cookie = $.cookie(this.cookieId + "-focus");
            this.focusedKey = (cookie === null) ? "" : cookie;
            if (cookie !== null) {
                this.cookiesFound = true;
            }
            cookie = $.cookie(this.cookieId + "-expand");
            this.expandedKeyList = (cookie === null) ? [] : cookie.split(",");
            if (cookie !== null) {
                this.cookiesFound = true;
            }
            cookie = $.cookie(this.cookieId + "-select");
            this.selectedKeyList = (cookie === null) ? [] : cookie.split(",");
            if (cookie !== null) {
                this.cookiesFound = true;
            }
        },
        write: function () {
            $.cookie(this.cookieId + "-active", (this.activeKey === null) ? "" : this.activeKey, this.cookieOpts);
            $.cookie(this.cookieId + "-focus", (this.focusedKey === null) ? "" : this.focusedKey, this.cookieOpts);
            $.cookie(this.cookieId + "-expand", (this.expandedKeyList === null) ? "" : this.expandedKeyList.join(
                ","), this.cookieOpts);
            $.cookie(this.cookieId + "-select", (this.selectedKeyList === null) ? "" : this.selectedKeyList.join(
                ","), this.cookieOpts);
        },
        addExpand: function (key) {
            if ($.inArray(key, this.expandedKeyList) < 0) {
                this.expandedKeyList.push(key);
                $.cookie(this.cookieId + "-expand", this.expandedKeyList.join(","), this.cookieOpts);
            }
        },
        clearExpand: function (key) {
            var idx = $.inArray(key, this.expandedKeyList);
            if (idx >= 0) {
                this.expandedKeyList.splice(idx, 1);
                $.cookie(this.cookieId + "-expand", this.expandedKeyList.join(","), this.cookieOpts);
            }
        },
        addSelect: function (key) {
            if ($.inArray(key, this.selectedKeyList) < 0) {
                this.selectedKeyList.push(key);
                $.cookie(this.cookieId + "-select", this.selectedKeyList.join(","), this.cookieOpts);
            }
        },
        clearSelect: function (key) {
            var idx = $.inArray(key, this.selectedKeyList);
            if (idx >= 0) {
                this.selectedKeyList.splice(idx, 1);
                $.cookie(this.cookieId + "-select", this.selectedKeyList.join(","), this.cookieOpts);
            }
        },
        isReloading: function () {
            return this.cookiesFound === true;
        },
        toDict: function () {
            return {
                cookiesFound: this.cookiesFound,
                activeKey: this.activeKey,
                focusedKey: this.activeKey,
                expandedKeyList: this.expandedKeyList,
                selectedKeyList: this.selectedKeyList
            };
        }
    };
    var WcuiTree = Class.create();
    WcuiTree.version = "$Version: 1.2.4$";
    WcuiTree.prototype = {
        initialize: function ($widget) {
            var self = this;
            this.phase = "init";
            this.$widget = $widget;
            this.options = $widget.options;
            this.$tree = $widget.element;
            this.timer = null;
            this.divTree = this.$tree.get(0);
            this.wcui_tree_queue_manager = QueueManager($.wcui_tree_parallel_max_load, function (queueEntry) {
                self.wcui_tree_queue_manager.inc()
                queueEntry.caller.call(queueEntry._this)
            });
            _initDragAndDrop(this);
        },
        _load: function (callback) {
            var opts = this.options;
            this.bEnableUpdate = true;
            this._nodeCount = 1;
            this.activeNode = null;
            this.hilightedNodes = []
            this.focusNode = null;
            if (opts.minExpandLevel < 1) {
                opts.minExpandLevel = 1;
            }
            if (opts.classNames !== TreeWidget.prototype.options.classNames) {
                opts.classNames = $.extend({}, TreeWidget.prototype.prototype.options.classNames, opts.classNames);
            }
            if (opts.ajaxDefaults !== TreeWidget.prototype.options.ajaxDefaults) {
                opts.ajaxDefaults = $.extend({}, TreeWidget.prototype.options.ajaxDefaults, opts.ajaxDefaults);
            }
            if (opts.dnd !== TreeWidget.prototype.options.dnd) {
                opts.dnd = $.extend({}, TreeWidget.prototype.options.dnd, opts.dnd);
            }
            if (!opts.imagePath) {
                $("script")
                    .each(function () {
                        var _rexDtLibName = /.*tree[^\/]*\.js$/i;
                        if (this.src.search(_rexDtLibName) >= 0) {
                            if (this.src.indexOf("/") >= 0) {
                                opts.imagePath = this.src.slice(0, this.src.lastIndexOf("/")) + "/skin/";
                            } else {
                                opts.imagePath = "skin/";
                            }
                            return false;
                        }
                    });
            }
            this.persistence = new WcuiTreeStatus(opts.cookieId, opts.cookie);
            if (opts.persist) {
                if (!$.cookie) {
                    $.error("Please include jquery.cookie.js to use persistence.");
                }
                this.persistence.read();
            }
            this.cache = {
                tagEmpty: "<span class='" + opts.classNames.empty + "'></span>",
                tagVline: "<span class='" + opts.classNames.vline + "'></span>",
                tagExpander: "<span class='" + opts.classNames.expander + "'></span>",
                tagConnector: "<span class='" + opts.classNames.connector + "'></span>",
                tagNodeIcon: "<span class='" + opts.classNames.nodeIcon + "'></span>",
                tagCheckbox: "<span class='" + opts.classNames.checkbox + "'></span>"
            };
            if (opts.children || (opts.initAjax && opts.initAjax.url) || opts.initId) {
                $(this.divTree)
                    .empty();
            }
            var $ulInitialize = this.$tree.find(">div." + opts.classNames.container + ":first")
                .hide();
            this.tnRoot = new WcuiTreeNode(null, this, opts.tree_root_data || {});
            this.tnRoot.bExpanded = true;
            this.tnRoot.render();
            this.divTree.appendChild(opts.tree_root_data ? $(this.tnRoot.node).closest(".wcui-tree")[0] : this.tnRoot.node);
            var root = this.tnRoot,
                isReloading = (opts.persist && this.persistence.isReloading()),
                isLazy = false,
                prevFlag = this.enableUpdate(false);
            if (opts.children) {
                root.addChild(opts.children);
            } else if (opts.initAjax && opts.initAjax.url) {
                isLazy = true;
                root.data.isLazy = true;
                this._reloadAjax(callback);
            } else if (opts.initId) {
                this._createFromTag(root, $("#" + opts.initId));
            } else {
                this._createFromTag(root, $ulInitialize);
                $ulInitialize.remove();
            }
            this._checkConsistency();
            if (!isLazy && opts.selectMode == 3) {
                root._updatePartSelectionState();
            }
            this.enableUpdate(prevFlag);
            this.$widget.bind();
            this.phase = "postInit";
            if (opts.persist) {
                this.persistence.write();
            }
            if (this.focusNode && this.focusNode.isVisible()) {
                this.focusNode.focus();
            }
            if (opts.auto_load_lazy_nodes) {
                $.each(this.tnRoot.childList || [], function () {
                    if (this.needLazyCall()) {
                        this._loadContent(false);
                    }
                });
            }
            if (!isLazy) {
                if (opts.onPostInit) {
                    opts.onPostInit.call(this, isReloading, false);
                }
                if (callback) {
                    callback.call(this, "ok");
                }
            }
            this.phase = "idle";
        },
        _reloadAjax: function (callback) {
            var opts = this.options;
            if (!opts.initAjax || !opts.initAjax.url) {
                throw "tree.reload() requires 'initAjax' mode.";
            }
            var pers = this.persistence;
            var ajaxOpts = $.extend({}, opts.initAjax);
            if (ajaxOpts.addActiveKey) {
                ajaxOpts.data.activeKey = pers.activeKey;
            }
            if (ajaxOpts.addFocusedKey) {
                ajaxOpts.data.focusedKey = pers.focusedKey;
            }
            if (ajaxOpts.addExpandedKeyList) {
                ajaxOpts.data.expandedKeyList = pers.expandedKeyList.join(",");
            }
            if (ajaxOpts.addSelectedKeyList) {
                ajaxOpts.data.selectedKeyList = pers.selectedKeyList.join(",");
            }
            var isReloading = pers.isReloading();
            ajaxOpts.success = function (dtnode, data, textStatus) {
                if (opts.selectMode == 3) {
                    dtnode.tree.tnRoot._updatePartSelectionState();
                }
                if (opts.onPostInit) {
                    opts.onPostInit.call(dtnode.tree, isReloading, false);
                }
                if (callback) {
                    callback.call(dtnode.tree, "ok");
                }
            };
            ajaxOpts.error = function (dtnode, XMLHttpRequest, textStatus, errorThrown) {
                if (opts.onPostInit) {
                    opts.onPostInit.call(dtnode.tree, isReloading, true, XMLHttpRequest, textStatus, errorThrown);
                }
                if (callback) {
                    callback.call(dtnode.tree, "error", XMLHttpRequest, textStatus, errorThrown);
                }
            };
            this.tnRoot.appendAjax(ajaxOpts);
        },
        toString: function () {
            return "WcuiTree '" + this.$tree.attr("id") + "'";
        },
        toDict: function () {
            return this.tnRoot.toDict(true);
        },
        serializeArray: function (stopOnParents) {
            var nodeList = this.getSelectedNodes(stopOnParents),
                name = this.$tree.attr("name") || this.$tree.attr("id"),
                arr = [];
            for (var i = 0, l = nodeList.length; i < l; i++) {
                arr.push({
                    name: name,
                    value: nodeList[i].key
                });
            }
            return arr;
        },
        getPersistData: function () {
            return this.persistence.toDict();
        },
        isInitializing: function () {
            return(this.phase == "init" || this.phase == "postInit");
        },
        isReloading: function () {
            return(this.phase == "init" || this.phase == "postInit") && this.options.persist && this.persistence.cookiesFound;
        },
        isUserEvent: function () {
            return(this.phase == "userEvent");
        },
        redraw: function () {
            this.tnRoot.redraw();
        },
        reload: function (callback) {
            this._load(callback);
        },
        getRoot: function () {
            return this.tnRoot;
        },
        enable: function () {
            this.$widget.enable();
        },
        disable: function () {
            this.$widget.disable();
        },
        getNodeByKey: function (key) {
            var modifiedKey = key.startsWith(this.options.idPrefix) ? key : (this.options.idPrefix + this.uuid + "-" + key)
            var el = document.getElementById(modifiedKey);
            if (el) {
                return el.dtnode ? el.dtnode : null;
            }
            var match = null;
            this.visit(function (node) {
                if (node.key === key) {
                    match = node;
                    return false;
                }
            }, true);
            return match;
        },
        getActiveNode: function () {
            return this.activeNode;
        },
        reactivate: function (setFocus) {
            var node = this.activeNode;
            if (node) {
                this.activeNode = null;
                node.activate();
                if (setFocus) {
                    node.focus();
                }
            }
        },
        getSelectedNodes: function (stopOnParents) {
            var nodeList = [];
            this.tnRoot.visit(function (node) {
                if (node.bSelected) {
                    nodeList.push(node);
                    if (stopOnParents === true) {
                        return "skip";
                    }
                }
            });
            return nodeList;
        },
        removeHilightedNodes: function() {
            var temp = this.hilightedNodes.slice(0)
            temp.every(function(node){
                this.highlight(false);
            })
        },
        activateKey: function (key, isSilent) {
            var dtnode = (key === null) ? null : this.getNodeByKey(key);
            if (!dtnode) {
                if (this.activeNode) {
                    this.activeNode.deactivate(isSilent);
                }
                this.activeNode = null;
                return null;
            }
            dtnode.focus();
            dtnode.activate(isSilent);
            return dtnode;
        },
        loadKeyPath: function (keyPath, callback) {
            var segList = keyPath.split(this.options.keyPathSeparator);
            if (segList[0] === "") {
                segList.shift();
            }
            if (segList[0] == this.tnRoot.key) {
                segList.shift();
            }
            keyPath = segList.join(this.options.keyPathSeparator);
            return this.tnRoot._loadKeyPath(keyPath, callback);
        },
        selectKey: function (key, select) {
            var dtnode = this.getNodeByKey(key);
            if (!dtnode) {
                return null;
            }
            dtnode.select(select);
            return dtnode;
        },
        enableUpdate: function (bEnable, redrawNode) {
            if (this.bEnableUpdate == bEnable) {
                return bEnable;
            }
            this.bEnableUpdate = bEnable;
            if (bEnable) {
                (redrawNode || this.tnRoot)
                    .redraw();
            }
            return !bEnable;
        },
        count: function () {
            return this.tnRoot.countChildren();
        },
        visit: function (fn, includeRoot) {
            return this.tnRoot.visit(fn, includeRoot);
        },
        _createFromTag: function (parentTreeNode, $ulParent) {
            var self = this;
            $ulParent.find(">div." + this.options.classNames.nodeContainer)
                .each(function () {
                    var $li = $(this),
                        $liSpan = $li.find(">span:first"),
                        $liA = $li.find(">a:first"),
                        title,
                        href = null,
                        target = null,
                        tooltip;
                    if ($liSpan.length) {
                        title = $liSpan.html();
                    } else if ($liA.length) {
                        title = $liA.html();
                        href = $liA.attr("href");
                        target = $liA.attr("target");
                        tooltip = $liA.attr("title");
                    } else {
                        title = $li.html();
                        var iPos = title.search(/<div/i);
                        if (iPos >= 0) {
                            title = $.trim(title.substring(0, iPos));
                        } else {
                            title = $.trim(title);
                        }
                    }
                    var data = {
                        title: title,
                        tooltip: tooltip,
                        isFolder: $li.hasClass("folder"),
                        isLazy: $li.hasClass("lazy"),
                        expand: $li.hasClass("expanded"),
                        select: $li.hasClass("selected"),
                        activate: $li.hasClass("active"),
                        focus: $li.hasClass("focused"),
                        noLink: $li.hasClass("noLink")
                    };
                    if (href) {
                        data.href = href;
                        data.target = target;
                    }
                    if ($li.attr("title")) {
                        data.tooltip = $li.attr("title");
                    }
                    if ($li.attr("id")) {
                        data[self.tree.options.key_prop] = "" + $li.attr("id");
                    }
                    if ($li.attr("data")) {
                        var dataAttr = $.trim($li.attr("data"));
                        if (dataAttr) {
                            if (dataAttr.charAt(0) != "{") {
                                dataAttr = "{" + dataAttr + "}";
                            }
                            try {
                                $.extend(data, eval("(" + dataAttr + ")"));
                            } catch (e) {
                                throw("Error parsing node data: " + e + "\ndata:\n'" + dataAttr + "'");
                            }
                        }
                    }
                    var childNodes = parentTreeNode.addChild(data);
                    var $ul = $li.find(">div." + opts.classNames.container + ":first");
                    if ($ul.length) {
                        self._createFromTag(childNodes.length ? childNodes[0] : null, $ul);
                    }
                });
        },
        _checkConsistency: function () {
        },
        _setDndStatus: function (sourceNode, targetNode, helper, hitMode, accept) {
            var $source = sourceNode ? $(sourceNode.span) : null,
                $target = $(targetNode.span);
            if (!this.$dndMarker) {
                this.$dndMarker = $("<div id='tree-drop-marker'></div>")
                    .hide()
                    .css({
                        "z-index": 1000
                    })
                    .prependTo($(this.divTree)
                        .parent());
            }
            if (hitMode === "after" || hitMode === "before" || hitMode === "over") {
                var markerOffset = "0 0";
                switch (hitMode) {
                    case "before":
                        this.$dndMarker.removeClass("tree-drop-after tree-drop-over");
                        this.$dndMarker.addClass("tree-drop-before");
                        markerOffset = "0 -8";
                        break;
                    case "after":
                        this.$dndMarker.removeClass("tree-drop-before tree-drop-over");
                        this.$dndMarker.addClass("tree-drop-after");
                        markerOffset = "0 8";
                        break;
                    default:
                        this.$dndMarker.removeClass("tree-drop-after tree-drop-before");
                        this.$dndMarker.addClass("tree-drop-over");
                        $target.addClass("tree-drop-target");
                        markerOffset = "8 0";
                }
                this.$dndMarker
                    .show()
                    .position({
                        my: "left top",
                        at: "left top",
                        of: $target,
                        offset: markerOffset
                    });
            } else {
                $target.removeClass("tree-drop-target");
                this.$dndMarker.hide();
            }
            if (hitMode === "after") {
                $target.addClass("tree-drop-after");
            } else {
                $target.removeClass("tree-drop-after");
            }
            if (hitMode === "before") {
                $target.addClass("tree-drop-before");
            } else {
                $target.removeClass("tree-drop-before");
            }
            if (accept === true) {
                if ($source) {
                    $source.addClass("tree-drop-accept");
                }
                $target.addClass("tree-drop-accept");
                helper.addClass("tree-drop-accept");
            } else {
                if ($source) {
                    $source.removeClass("tree-drop-accept");
                }
                $target.removeClass("tree-drop-accept");
                helper.removeClass("tree-drop-accept");
            }
            if (accept === false) {
                if ($source) {
                    $source.addClass("tree-drop-reject");
                }
                $target.addClass("tree-drop-reject");
                helper.addClass("tree-drop-reject");
            } else {
                if ($source) {
                    $source.removeClass("tree-drop-reject");
                }
                $target.removeClass("tree-drop-reject");
                helper.removeClass("tree-drop-reject");
            }
        },
        _onDragEvent: function (eventName, node, otherNode, event, ui, draggable) {
            var opts = this.options,
                dnd = this.options.dnd,
                res = null,
                nodeTag = $(node.span),
                hitMode,
                enterResponse;
            switch (eventName) {
                case "helper":
                    var $helper = $("<div class='tree-drag-helper'><span class='tree-drag-helper-img' /></div>")
                        .append($(event.target)
                            .closest("." + opts.classNames.title)
                            .clone());
                    $helper.data("dtSourceNode", node);
                    res = $helper;
                    break;
                case "start":
                    res = true;
                    if (node.isStatusNode()) {
                        res = false;
                    } else if (dnd.onDragStart) {
                        res = dnd.onDragStart(node);
                    }
                    if (res === false) {
                        ui.helper.trigger("mouseup");
                        ui.helper.hide();
                    } else {
                        nodeTag.addClass("tree-drag-source");
                    }
                    break;
                case "enter":
                    res = true;
                    if (dnd.onDragEnter) {
                        res = dnd.onDragEnter(node, otherNode);
                        res = {
                            over: ((res === true) || (res === "over") || $.inArray("over", res) >= 0),
                            before: ((res === true) || (res === "before") || $.inArray("before", res) >= 0),
                            after: ((res === true) || (res === "after") || $.inArray("after", res) >= 0)
                        };
                    } else {
                        res = {over: true, before: true, after: true}
                    }
                    ui.helper.data("enterResponse", res);
                    break;
                case "over":
                    enterResponse = ui.helper.data("enterResponse");
                    hitMode = null;
                    if (enterResponse === false) {
                    } else if (typeof enterResponse === "string") {
                        hitMode = enterResponse;
                    } else {
                        var nodeOfs = nodeTag.offset();
                        var relPos = {
                            x: event.pageX - nodeOfs.left,
                            y: event.pageY - nodeOfs.top
                        };
                        var relPos2 = {
                            x: relPos.x / nodeTag.width(),
                            y: relPos.y / nodeTag.height()
                        };
                        if (enterResponse.after && relPos2.y > 0.75) {
                            hitMode = "after";
                        } else if (!enterResponse.over && enterResponse.after && relPos2.y > 0.5) {
                            hitMode = "after";
                        } else if (enterResponse.before && relPos2.y <= 0.25) {
                            hitMode = "before";
                        } else if (!enterResponse.over && enterResponse.before && relPos2.y <= 0.5) {
                            hitMode = "before";
                        } else if (enterResponse.over) {
                            hitMode = "over";
                        }
                        if (dnd.preventVoidMoves) {
                            if (node === otherNode) {
                                hitMode = null;
                            } else if (hitMode === "before" && otherNode && node === otherNode.getNextSibling()) {
                                hitMode = null;
                            } else if (hitMode === "after" && otherNode && node === otherNode.getPrevSibling()) {
                                hitMode = null;
                            } else if (hitMode === "over" && otherNode && otherNode.parent === node && otherNode.isLastSibling()) {
                                hitMode = null;
                            }
                        }
                        ui.helper.data("hitMode", hitMode);
                    }
                    if (hitMode === "over" && dnd.autoExpandMS && node.hasChildren() !== false && !node.bExpanded) {
                        node.scheduleAction("expand", dnd.autoExpandMS);
                    }
                    if (hitMode) {
                        if (dnd.onDragOver) {
                            res = dnd.onDragOver(node, otherNode, hitMode);
                            if (res === "over" || res === "before" || res === "after") {
                                hitMode = res;
                            }
                        } else {
                            res = (node == otherNode || node.isDescendantOf(otherNode)) ? false : hitMode;
                        }
                    }
                    this._setDndStatus(otherNode, node, ui.helper, hitMode, res !== false && hitMode !== null);
                    break;
                case "drop":
                    var isForbidden = ui.helper.hasClass("tree-drop-reject");
                    hitMode = ui.helper.data("hitMode");
                    if (hitMode && dnd.onDrop && !isForbidden) {
                        dnd.onDrop(node, otherNode, hitMode, ui, draggable);
                    }
                    break;
                case "leave":
                    node.scheduleAction("cancel");
                    ui.helper.data("enterResponse", null);
                    ui.helper.data("hitMode", null);
                    this._setDndStatus(otherNode, node, ui.helper, "out", undefined);
                    if (dnd.onDragLeave) {
                        dnd.onDragLeave(node, otherNode);
                    }
                    break;
                case "stop":
                    nodeTag.removeClass("tree-drag-source");
                    if (dnd.onDragStop) {
                        dnd.onDragStop(node);
                    }
                    break;
                default:
                    throw "Unsupported drag event: " + eventName;
            }
            return res;
        },
        cancelDrag: function () {
            var dd = $.ui.ddmanager.current;
            if (dd) {
                dd.cancel();
            }
        },
        getCanonicalActive: function () {
            var node = this.getActiveNode();
            var nodes = []
            if (node) {
                nodes.push(node.key)
                while (node.parent) {
                    node = node.parent;
                    nodes.push(node.key)
                }
            }
            return nodes;
        },
        setCanonicalActive: function (canonicalActive, callbacks, isSilent) {
            if (arguments.length == 2 && typeof callbacks == "boolean") {
                isSilent = callbacks;
                callbacks = {};
            }
            callbacks = callbacks || {}
            var length = canonicalActive.length;
            if (!canonicalActive && !length) {
                if (callbacks.fail) {
                    callbacks.fail();
                }
                return;
            }
            var node = {childList: [this.tnRoot]};
            var foundNode;
            var t = length - 1;
            var nodeFinder = function () {
                var expectedKey = canonicalActive[t];
                foundNode = null;
                $.each(node.childList || [], function () {
                    if (expectedKey == this.key) {
                        foundNode = this;
                        return false;
                    }
                })
                if (!foundNode) {
                    if (callbacks.fail) {
                        callbacks.fail();
                    }
                    return;
                }
                node = foundNode;
                t--;
                if (t == -1) {
                    node.activate(isSilent);
                    if (callbacks.done) {
                        callbacks.done();
                    }
                    return;
                }
                if (node.needLazyCall()) {
                    node._loadContent(false, {
                        done: nodeFinder,
                        fail: callbacks.fail
                    }, true)
                } else {
                    nodeFinder();
                }
            }
            nodeFinder();
        }
    };
    var widget_uuid = 100;
    var TreeWidget = function() {
        this.widgetName = "wcTree";
        this.widgetFullName = "wcuiTreeWidget";
    }
    TreeWidget.inherit(WCUIWidget);
    TreeWidget.prototype._create = function (options, element) {
        this.eventNamespace = "." + this.widgetName + this.uuid;
        var opts = this.options;
        if (opts.debugLevel >= 1) {
        }
        this.options.event += ".tree";
        this.tree = new WcuiTree(this);
        this.tree.uuid = this.uuid
        this.tree._load();
    };

    TreeWidget.prototype.bind = function () {
        var namespace = "tree_" + this.uuid;
        this.element.off("." + namespace);
        var eventNames = "click." + namespace + " dblclick." + namespace;
        if (this.options.keyboard) {
            eventNames += " keypress." + this.uuid + " keydown." + this.uuid;
        }
        var tree = this.tree;
        var o = tree.options;
        this.element.on(eventNames, function (event) {
            var dtnode = TreeWidget.getNode(event.target);
            if (!dtnode) {
                return true;
            }
            var prevPhase = tree.phase;
            tree.phase = "userEvent";
            try {
                switch (event.type) {
                    case "click":
                        return (o.onClick && o.onClick.call(tree, dtnode, event) === false) ? false : dtnode._onClick(event);
                    case "dblclick":
                        if (o.onDblClick && o.onDblClick.call(tree, dtnode, event) === false) {
                            return false
                        }
                    case "keydown":
                        return (o.onKeydown && o.onKeydown.call(tree, dtnode, event) === false) ? false : dtnode._onKeydown(event);
                    case "keypress":
                        if (o.onKeypress && o.onKeypress.call(tree, dtnode, event) === false) {
                            return false;
                        }
                }
            } finally {
                tree.phase = prevPhase;
            }
        });

        function __focusHandler(event) {
            event = $.event.fix(event || window.event);
            var dtnode = TreeWidget.getNode(event.target);
            return dtnode ? dtnode._onFocus(event) : false;
        }

        this.element.on("focus." + namespace + " blur." + namespace, __focusHandler)
        this.element.on("contextmenu." + namespace, "." + this.tree.options.classNames.title, function (ev) {
            var dtnode = TreeWidget.getNode(ev.target);
            if (o.onContextMenu) {
                o.onContextMenu.call(dtnode.tree, dtnode, ev)
                return false;
            }
        })
        if(o.afterLoad) {
            o.afterLoad(this.element.find(".tree-container"), TreeWidget);
        }
    }

    TreeWidget.prototype.unbind = function () {
        var namespace = "tree_" + this.uuid;
        this.element.unbind("." + namespace);
    }

    TreeWidget.prototype.enable = function () {
        this.bind();
        WCUIWidget.enable.apply(this, arguments);
    }

    TreeWidget.prototype.disable = function () {
        this.element.off("." + "tree_" + this.uuid);
        WCUIWidget.disable.apply(this, arguments);
    }

    TreeWidget.prototype.inst = function () {
        return this.tree
    };

    TreeWidget.prototype._destroy = function () {
        this.tree.wcui_tree_queue_manager.clear();
        this.unbind()
        if (this.tree._xhrs) {
            //To prevent modification in array which is being iterated
            var xhrs_to_abort = [];
            $.each(this.tree._xhrs, function () {
                xhrs_to_abort.push(this);
            });
            $.each(xhrs_to_abort, function () {
                this.abort();
            });
            delete this.tree._xhrs;
        }
    }


    $.fn.tree = function(options) {
        var args = arguments;
        var returnValue;
        $.each(this, function() {
            var $this = $(this);
            var treeWidget = $this.data("wcuiTreeWidget");
            if(typeof options == "string" && treeWidget) {
                returnValue = treeWidget[options].apply(treeWidget, Array.prototype.splice.call(args, 1))
            } else if (!treeWidget) {
                treeWidget = new TreeWidget();
                treeWidget._createWidget(options, $this)
                $this.data("wcuiTreeWidget", treeWidget);
            }
        })
        if(returnValue) {
            return returnValue
        }
        return this;
    }
    //if (versionCompare($.ui.version, "1.8") < 0) {
    //    $.wcui.tree.getter = "getTree getRoot getActiveNode getSelectedNodes";
    //}
    TreeWidget.prototype.version = "$Version: 1.2.4$";
    TreeWidget.getNode = function (el) {
        if (el instanceof WcuiTreeNode) {
            return el;
        }
        if (el.selector !== undefined) {
            el = el[0];
        }
        while (el) {
            if (el.dtnode) {
                return el.dtnode;
            }
            el = el.parentNode;
        }
        return null;
    };
    TreeWidget.getPersistData = WcuiTreeStatus._getTreePersistData;
    TreeWidget.prototype.options = {
        title: "WcuiTree",
        minExpandLevel: 1,
        imagePath: null,
        children: null,
        initId: null,
        initAjax: null,
        autoFocus: true,
        keyboard: true,
        persist: false,
        autoCollapse: false,
        clickFolderMode: 3,
        activeVisible: true,
        checkbox: false,
        selectMode: 1,
        fx: null,
        noLink: true,
        onClick: null,
        onDblClick: null,
        onKeydown: null,
        onKeypress: null,
        onFocus: null,
        onBlur: null,
        onQueryActivate: null,
        onQuerySelect: null,
        onQueryExpand: null,
        onPostInit: null,
        onActivate: null,
        onDeactivate: null,
        onSelect: null,
        onExpand: null,
        onLazyRead: null,
        onCustomRender: null,
        onCreate: null,
        onRender: null,
        postProcess: null,
        dnd: {
            onDragStart: null,
            onDragStop: null,
            autoExpandMS: 1000,
            preventVoidMoves: true,
            onDragEnter: null,
            onDragOver: null,
            onDrop: null,
            onDragLeave: null
        },
        ajaxDefaults: {
            cache: false,
            timeout: 0,
            dataType: "json"
        },
        strings: {
            loading: "Loading&#8230;",
            loadError: "Load error!"
        },
        idPrefix: "tree-id-",
        keyPathSeparator: "/",
        cookieId: "tree",
        cookie: {
            expires: null
        },
        classNames: {
            container: "tree-container",
            nodeContainer: "tree-node-container",
            node: "tree-node",
            folder: "tree-folder",
            empty: "tree-empty",
            vline: "tree-vline",
            expander: "tree-expander",
            connector: "tree-connector",
            checkbox: "tree-checkbox",
            nodeIcon: "tree-icon",
            title: "tree-title",
            noConnector: "tree-no-connector",
            nodeError: "tree-statusnode-error",
            nodeWait: "tree-statusnode-wait",
            hidden: "tree-hidden",
            combinedExpanderPrefix: "tree-exp-",
            combinedIconPrefix: "tree-ico-",
            nodeLoading: "tree-loading",
            hasChildren: "tree-has-children",
            active: "tree-active",
            selected: "tree-selected",
            expanded: "tree-expanded",
            lazy: "tree-lazy",
            focused: "tree-focused",
            partsel: "tree-partsel",
            lastsib: "tree-lastsib",
            highlighted: "highlighted",
            marked: "marked"
        },
        debugLevel: 1,
        title_prop: "name",
        has_child_prop: "hasChild",
        type_prop: "type",
        node_type: undefined,
        load_url: undefined,
        key_prop: "id",
        auto_load_lazy_nodes: false,
        ajax_caller: bm.ajax
    };

    TreeWidget.nodedatadefaults = {
        title: null,
        key: null,
        isFolder: false,
        isLazy: false,
        tooltip: null,
        href: null,
        icon: null,
        clazz: null,
        noLink: true,
        activate: false,
        focus: false,
        expand: false,
        select: false,
        hideCheckbox: false,
        unselectable: false,
        children: null
    };
    function _initDragAndDrop(tree) {
        //drag n drop not supported in this modified version
        return;
        var dnd = tree.options.dnd || null;
        if (dnd && (dnd.onDragStart || dnd.onDrop)) {
            _registerDnd();
        }
        if (dnd && (dnd.onDragStart || dnd.onDrop)) {
            tree.$tree.draggable({
                addClasses: false,
                appendTo: "body",
                containment: false,
                delay: 0,
                distance: 4,
                revert: false,
                scroll: true,
                scrollSpeed: 7,
                scrollSensitivity: 10,
                connectToWcuiTree: true,
                helper: function (event) {
                    var sourceNode = TreeWidget.getNode(event.target);
                    if (!sourceNode) {
                        return "<div></div>";
                    }
                    return sourceNode.tree._onDragEvent("helper", sourceNode, null, event, null, null);
                },
                start: function (event, ui) {
                    var sourceNode = ui.helper.data("dtSourceNode");
                    return !!sourceNode;
                },
                _last: null
            });
        }
        if (dnd && dnd.onDrop) {
            //drag n drop not supported in this modified version
            /*tree.$tree.droppable({
                addClasses: false,
                tolerance: "intersect",
                greedy: false,
                _last: null
            });*/
        }
    }

    var didRegisterDnd = false;
    //drag n drop not supported in this modified version
    /*var _registerDnd = function () {
        if (didRegisterDnd) {
            return;
        }
        $.ui.plugin.add("draggable", "connectToWcuiTree", {
            start: function (event, ui) {
                var draggable = $(this)
                        .data("ui-draggable") || $(this)
                        .data("draggable"),
                    sourceNode = ui.helper.data("dtSourceNode") || null;
                if (sourceNode) {
                    draggable.offset.click.top = -2;
                    draggable.offset.click.left = +16;
                    return sourceNode.tree._onDragEvent("start", sourceNode, null, event, ui, draggable);
                }
            },
            drag: function (event, ui) {
                var draggable = $(this)
                        .data("ui-draggable") || $(this)
                        .data("draggable"),
                    sourceNode = ui.helper.data("dtSourceNode") || null,
                    prevTargetNode = ui.helper.data("dtTargetNode") || null,
                    targetNode = TreeWidget.getNode(event.target);
                if (event.target && !targetNode) {
                    var isHelper = $(event.target)
                        .closest("div.tree-drag-helper, #tree-drop-marker")
                        .length > 0;
                    if (isHelper) {
                        return;
                    }
                }
                ui.helper.data("dtTargetNode", targetNode);
                if (prevTargetNode && prevTargetNode !== targetNode) {
                    prevTargetNode.tree._onDragEvent("leave", prevTargetNode, sourceNode, event, ui, draggable);
                }
                if (targetNode) {
                    if (!targetNode.tree.options.dnd.onDrop) {
                    } else if (targetNode === prevTargetNode) {
                        targetNode.tree._onDragEvent("over", targetNode, sourceNode, event, ui, draggable);
                    } else {
                        targetNode.tree._onDragEvent("enter", targetNode, sourceNode, event, ui, draggable);
                    }
                }
            },
            stop: function (event, ui) {
                var draggable = $(this)
                        .data("ui-draggable") || $(this)
                        .data("draggable"),
                    sourceNode = ui.helper.data("dtSourceNode") || null,
                    targetNode = ui.helper.data("dtTargetNode") || null,
                    eventType = event.type,
                    dropped = (eventType == "mouseup" && event.which == 1);
                if (targetNode) {
                    if (dropped) {
                        targetNode.tree._onDragEvent("drop", targetNode, sourceNode, event, ui, draggable);
                    }
                    targetNode.tree._onDragEvent("leave", targetNode, sourceNode, event, ui, draggable);
                }
                if (sourceNode) {
                    sourceNode.tree._onDragEvent("stop", sourceNode, null, event, ui, draggable);
                }
            }
        });
        didRegisterDnd = true;
    };*/
}(jQuery));