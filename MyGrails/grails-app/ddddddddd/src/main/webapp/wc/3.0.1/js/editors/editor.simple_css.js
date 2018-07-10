app.tabs.simpleCss = function (tab, parser) {
    this.tab = tab;
    this.parser = parser;
}

var _s = app.tabs.simpleCss.prototype;

var mask = $("<div class='layout-mask'></div>");

(function () {
    function attachEvents() {
        var _this = this;
        this.rulePanel.find(".button-add-new-rule").click(function () {
            var inputElm = _this.rulePanel.find(".input-add-new-rule");
            var value = inputElm.val();
            inputElm.val("")
            if (value != "") {
                var rule;
                var selectors = value.split(",");
                if (_this.selectedMedia == undefined) {
                    rule = _this.parser.addRule(selectors, []);
                }
                else {
                    rule = _this.selectedMedia.addRule(selectors, [])
                }
                _this.ruleList.find(".selected").removeClass("selected");
                var node = _this.buildRuleItem(rule);
                node.addClass("selected");
                _this.ruleList.append(node)
                _this.selectedRule = rule;
                _this.buildAttributes([]);
                _this.tab.setDirty();
            }
        })
        this.mediaPanel.find(".button-add-new-media").click(function () {
            var maxWidthElm = _this.mediaPanel.find(".add-new-media [name='max-width']");
            var minWidthElm = _this.mediaPanel.find(".add-new-media [name='min-width']");
            var maxWidth = maxWidthElm.val();
            var minWidth = minWidthElm.val();
            maxWidthElm.val("");
            minWidthElm.val("");
            if (!maxWidth && !minWidth) {
                return false;
            }
            var definition = _this.buildMediaDefinition(maxWidth, minWidth)
            var media = _this.parser.addMedia(definition, [])
            var index = _this.mediaList.find(".media").length
            var node = _this.buildMediaItem(media, index);
            _this.mediaList.append(node);
            node.updateUi();
            _this.selectedMedia = media;
            _this.mediaList.find(".selected").removeClass("selected");
            node.addClass("selected");
            _this.buildRules([]);
            _this.tab.setDirty();
        })
        this.tab.body.find(".button-add-new-attribute").click(function () {
            if (_this.selectedRule == undefined) {
                bm.notify($.i18n.prop("no.rule.selected"), "error")
                return;
            }
            var value = _this.tab.body.find(".input-add-new-attribute").val();
            if (value == "") {
                return;
            }
            _this.tab.body.find(".input-add-new-attribute").val("");
            var attr = _this.getAttributeObject(value);
            if (Object.keys(attr).length == 0) {
                bm.notify($.i18n.prop("attribute.entry.not.in.format"), "error")
                return;
            }
            _this.selectedRule.addAttribute(attr.key, attr.value);
            _this.buildAttributes(_this.selectedRule.attributes);
            _this.tab.setDirty();
        })
        this.tab.body.find(".attribute-panel").on("tab:activate", function (event, ui) {
            if (_this.selectedRule == undefined) {
                return;
            }
            if (ui.newIndex == "text") {
                _this.attrTextArea.val("");
                _this.setAttributeText(_this.selectedRule.attributes);
            }
            else {
                var value = _this.attrTextArea.val();
                var attrs = _this.getAttributeObjects(value);
                _this.selectedRule.attributes = attrs;
                _this.buildAttributes(attrs);
            }
        })
        this.tab.body.find('.input-add-new-rule, .input-add-new-attribute, [name=min-width], [name=max-width]').on("keydown", function (e) {
            if (e.keyCode == 13) {
                $(this).siblings(".add-new-entry").trigger("click");
                return false
            }
        })
        this.attrTextArea.on("change", function () {
            var value = _this.attrTextArea.val();
            var attrs = _this.getAttributeObjects(value);
            _this.selectedRule.attributes = attrs;
            _this.tab.setDirty()
        })
    }

    _s.init = function () {
        var _this = this;
        this.mediaPanel = this.tab.body.find(".media-panel")
        this.rulePanel = this.tab.body.find(".rule-panel")
        this.mediaList = this.tab.body.find(".medias").sortable({
            containment: "parent",
            tolerance: "intersect",
            items: "div:not(.global)",
            change: function (event, ui) {
                _this.move(_this.parser.rulesets, ui, "media")
            }
        });
        this.ruleList = this.tab.body.find(".rules").sortable({
            containment: "parent",
            tolerance: "intersect",
            stop: function (event, ui) {
                var rules;
                if (_this.selectedMedia == undefined) {
                    rules = _this.parser.rulesets;
                } else {
                    rules = _this.selectedMedia.rulesets;
                }
                ;
                _this.move(rules, ui, "rule")
            }
        });
        this.attrList = this.tab.body.find(".attribute-list");
        this.attrList.sortable({
            containment: "parent",
            tolerance: "intersect",
            stop: function (event, ui) {
                var attributes = _this.selectedRule.attributes;
                _this.move(attributes, ui, "attr")
            }
        }).scrollbar()
        this.attrTextArea = this.tab.body.find(".attr-panel-text .attr-text")
        this.mediaPanel.find("input[name='max-width'], input[name='min-width']").decimal()
        this.buildMedias();
        attachEvents.call(this);
        this.tab.body.find(".css-entry-list").scrollbar();
    }
})();

_s.buildGlobal = function () {
    var _this = this;
    var node = $('<div class="media css-entry even global"><span class="label">&lt;' + $.i18n.prop("no.selector") + '&gt;</span>');
    node.bind("click", function (ev) {
        _this.getRulesForMedia(undefined, _this.getRuleForGlobal())
        node.addClass("selected");
    });
    this.selectedMedia = undefined;
    this.mediaList.append(node);
    node.addClass("selected")
    this.buildRules(_this.getRuleForGlobal())
}

_s.buildMedias = function () {
    this.mediaList.empty().scrollbar();
    this.buildGlobal();
    var rulesets = this.parser.rulesets;
    var index = 1;
    for (var i = 0; i < rulesets.length; i++) {
        if (rulesets[i].type == "media") {
            var node = this.buildMediaItem(rulesets[i], index);
            this.mediaList.append(node);
            index++;
        }
    }
}

_s.buildMediaItem = function (media) {
    var _this = this;
    var nodeHtml = '<div class="media css-entry"><span class="label">' + media.definition + "</span>" +
        "<span class='editor-buttons'>";
    if (media.definition.match(/(max|min)-width/)) {
        nodeHtml += "<span class='edit-media-item tool-icon edit' title='" + $.i18n.prop("edit") + "'></span>";
        nodeHtml += "<span style='display: none;' class='update-media-item tool-icon update' title='" + $.i18n.prop("update") + "'></span>";
    }
    nodeHtml += "<span class='remove-media-item tool-icon remove' title='" + $.i18n.prop("remove") + "'></span></span></div>"
    var node = $(nodeHtml);
    node.data("media", media)
    node.bind("click", function (ev) {
        var tag = $(ev.target);
        if (tag.is(".remove-media-item")) {
            _this.removeMedia(node, media);
        } else if (tag.is(".edit-media-item")) {
            _this.editMedia(node, media, tag);
        } else if (tag.is(".update-media-item")) {
            _this.updateMedia(node, media, tag);
        } else {
            _this.getRulesForMedia(media, media.rulesets)
            node.addClass("selected");
        }
    });
    return node
}

_s.buildRuleItem = function (rule) {
    var _this = this;
    var node = $('<div class="rule css-entry"><span class="label">' + rule.selectors.join(",") +
        "</span>" +
        "<span class='editor-buttons'><span class='add-rule-item tool-icon edit' title='" + $.i18n.prop("edit") + "'></span>" +
        "<span class='remove-rule-item tool-icon remove' title='" + $.i18n.prop("remove") + "'></span></span></div>");

    var ruleItem = node.find(".label");
    node.data("rule", rule)
    node.bind("click", function (ev) {
        var tag = $(ev.target);
        if (tag.is(".add-rule-item")) {
            _this.editRule(ruleItem, tag, rule);
        } else if (tag.is(".remove-rule-item")) {
            _this.removeRule(ruleItem, rule);
        } else {
            _this.getAttributesForRule(node, rule)
        }
    });
    node.updateUi();
    return node;
}

_s.buildRules = function (rules) {
    this.ruleList.empty().scrollbar();
    this.attrList.empty().scrollbar();
    this.attrTextArea.val("")
    this.selectedRule = undefined
    for (var i = 0; i < rules.length; i++) {
        if (rules[i].type == "rule") {
            var node = this.buildRuleItem(rules[i], i);
            if (i == 0) {
                this.selectedRule = rules[i];
                node.addClass("selected");
                this.buildAttributes(rules[i].attributes);
            }
            this.ruleList.append(node);
        }
    }
}

_s.getRuleForGlobal = function () {
    var globalRules = [];
    for (var i = 0; i < this.parser.rulesets.length; i++) {
        if (this.parser.rulesets[i].type == "rule") {
            globalRules.push(this.parser.rulesets[i]);
        }
    }
    return globalRules;
}

_s.buildAttributeItem = function (attr, index) {
    var _this = this;
    var node = $('<div class="attr css-entry"><span class="label"><span class="attr-key">' + attr.key + '</span>' + '<span class="colon"> : </span>' +
        '<span class="attr-value">' + attr.value + '</span></span>' +
        "<span class='editor-buttons'><span class='edit-attr-item tool-icon edit' title='" + $.i18n.prop("edit") + "'></span>" +
        "<span class='remove-attr-item tool-icon remove' title='" + $.i18n.prop("remove") + "'></span></span></div>");
    node.data("attr", attr);
    node.bind("click", function (ev) {
        var tag = $(ev.target);
        if (tag.is(".edit-attr-item")) {
            _this.editAttribute(node, tag)
        } else if (tag.is(".remove-attr-item")) {
            _this.removeAttribute(node, attr)
        }
    });
    return node;
}

_s.buildAttributes = function (attrs) {
    this.attrTextArea.val("");
    this.attrList.empty().scrollbar();
    this.setAttributeText(attrs);
    var i = 0;
    while (i < attrs.length) {
        var node = this.buildAttributeItem(attrs[i], i)
        i++;
        this.attrList.append(node);
    }
}

_s.getRulesForMedia = function (media, rules) {
    if (this.selectedMedia == media) {
        return;
    }
    this.mediaPanel.find(".media.selected").removeClass("selected");
    this.selectedMedia = media;
    this.buildRules(rules);
}

_s.getAttributesForRule = function (node, rule) {
    this.attrTextArea.trigger("change");
    if (node.hasClass("selected")) {
        return;
    }
    this.ruleList.find(".selected").removeClass("selected");
    this.selectedRule = rule;
    node.addClass("selected");
    this.buildAttributes(rule.attributes);
}

_s.editAttribute = function (attrItem, elm) {
    var attr_key = attrItem.find(".attr-key");
    var attr_value = attrItem.find(".attr-value");
    var attr = attrItem.data("attr")
    if (elm.hasClass("edit")) {
        elm.removeClass("edit").addClass("update")
        attrItem.find(".label, .remove-attr-item").hide();
        attrItem.prepend("<input type='text' class='input-add-attr-item'" + "' value='" + attr.key + " : " +
            attr.value + "'>")
    } else {
        elm.removeClass("update").addClass("edit")
        var value = attrItem.find(".input-add-attr-item").val();
        attrItem.find(".input-add-attr-item").remove();
        attrItem.find(".label, .remove-attr-item").show()
        var newAttr = this.getAttributeObject(value)
        attr.key = newAttr.key;
        attr.value = newAttr.value;
        attr_key.html(attr.key);
        attr_value.html(attr.value);
        this.tab.setDirty();
    }
}

_s.removeAttribute = function (attrItem, attr) {
    var attributes = this.selectedRule.attributes;
    var idx = attributes.indexOf(attr);
    attributes.splice(idx, 1);
    attrItem.remove();
    this.tab.setDirty()
}

_s.setAttributeText = function (attrs) {
    var line;
    var i = 0;
    while (i < attrs.length) {
        var attrText = attrs[i].key + " : " + attrs[i].value + ";";
        if (this.attrTextArea.val() == "") {
            line = attrText;
        }
        else {
            line = "\n" + attrText;
        }
        this.attrTextArea.val(function (index, value) {
            return value + line;
        });
        i++;
    }
}

_s.getAttributeObjects = function (value) {
    var attrs = [];
    value.replace(/([^:]+)?:([^;]+);/g, function ($1, $2, $3) {
        attrs.push({
            key: $2.trim(),
            value: $3.trim()
        })
    });
    return attrs;
}

_s.getAttributeObject = function (value) {
    var attr = {}
    value.replace(/([^:]+)?:([^;]+)/, function ($1, $2, $3) {
        attr.key = $2.trim();
        attr.value = $3.trim();
    });
    return attr
}

_s.moveAttr = function (attr, nextAttr) {
    var attributes = this.selectedRule.attributes;
    var attrIdx = attributes.indexOf(attr);
    var nextAttrIdx = attributes.length - 1;
    if (nextAttr) {
        nextAttrIdx = attributes.indexOf(nextAttr) - 1;
    }
    attributes.splice(attrIdx, 1);
    attributes.splice(nextAttrIdx, 0, attr);
}

_s.editRule = function (ruleItem, elm, rule) {
    var parentElm = ruleItem.parent();
    var removeButton = parentElm.find(".remove-rule-item");
    if (elm.hasClass("edit")) {
        elm.removeClass("edit").addClass("update")
        var value = rule.selectors.join(",");
        ruleItem.hide();
        removeButton.hide();
        parentElm.prepend("<input type='text' class='input-add-rule-item'" + "' value='" + value + "'>")
    }
    else {
        elm.removeClass("update").addClass("edit")
        var value = parentElm.find(".input-add-rule-item").val();
        rule.selectors = value.split(",")
        parentElm.find(".input-add-rule-item").remove();
        ruleItem.show()
            .html(value.toString());
        removeButton.show()
        this.tab.setDirty();
    }
}

_s.removeRule = function (ruleItem, rule) {
    var rules;
    if (this.selectedMedia == undefined) {
        this.parser.removeRule(rule);
        rules = this.parser.rulesets;
    } else {
        this.selectedMedia.removeRule(rule);
        rules = this.selectedMedia.rulesets;
    }
    ruleItem.parent().remove();
    if (this.selectedRule == rule) {
        this.buildRules(rules);
    }
    this.tab.setDirty()
}

_s.move = function (list, ui, type) {
    var item = ui.item
    var relativeItem = item[0].nextSibling;
    var currentIdx = list.length - 1;
    if (relativeItem) {
        currentIdx = list.indexOf($(relativeItem).data(type));
    }
    if (ui.offset.top > ui.originalPosition.top && relativeItem) {     ////down
        currentIdx = currentIdx - 1;
    }
    var itemData = $(item).data(type);
    var itemIdx = list.indexOf(itemData);
    list.splice(itemIdx, 1)
    list.splice(currentIdx, 0, itemData)
}

_s.removeMedia = function (node, media) {
    node.remove();
    this.parser.removeMedia(media);
    if (node.hasClass("selected")) {
        this.ruleList.empty().scrollbar();
        this.attrList.empty().scrollbar();
        this.buildMedias();
    }
    this.tab.setDirty()
}

_s.editMedia = function (node, media, tag) {
    var elems = node.find(".label, .remove-media-item, .edit-media-item");
    var updateBtn = node.find(".update-media-item");
    elems.hide();
    updateBtn.show();
    node.append("<span class='edit-media-inputs'><label>" + $.i18n.prop("min.width") + " </label><input type='text' name='min-width'>&nbsp;&nbsp;&nbsp;&nbsp;<label>" +
        $.i18n.prop("max.width") + " </label><input type='text' name='max-width'></span>");
    node.find("input[name='max-width'], input[name='min-width']").decimal()

}

_s.updateMedia = function (node, media, tag) {
    var elems = node.find(".label, .remove-media-item, .edit-media-item");
    var updateBtn = node.find(".update-media-item");
    elems.show();
    updateBtn.hide();
    var maxWidthElm = node.find("input[name='max-width']");
    var minWidthElm = node.find("input[name='min-width']");
    var maxWidth = maxWidthElm.val();
    var minWidth = minWidthElm.val();
    maxWidthElm.val("");
    minWidthElm.val("");
    node.find(".edit-media-inputs").remove();
    if (!maxWidth && !minWidth) {
        return false;
    }
    var definition = this.buildMediaDefinition(maxWidth, minWidth);
    media.definition = definition;
    node.find(".label").html(definition);
    this.tab.setDirty();
}

_s.buildMediaDefinition = function (maxWidth, minWidth) {
    var builder = new StringWriter();
    if (minWidth) {
        builder.plus("(min-width: " + minWidth + "px)")
        if (maxWidth) {
            builder.plus(" and (max-width: " + maxWidth + "px)")
        }
    }
    else {
        builder.plus("(max-width: " + maxWidth + "px)");
    }
    return builder.toString();
}