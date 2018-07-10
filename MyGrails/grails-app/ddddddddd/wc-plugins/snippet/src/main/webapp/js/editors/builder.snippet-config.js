(function() {
    var DATA_STYLE_PROPERTIES = {
        text: ["color", "font-size",  "margin-bottom", "margin-top", "text-align"],
        html: ["color", "font-size",  "margin-bottom", "margin-top", "text-align"],
        image: ["margin-top", "margin-bottom", "margin-left", "margin-right", "img-src", "link", "target"],
        container: ["background-image", "background-color", "background-repeat", "background-size", "background-attachment", "padding-top", "padding-bottom", "border-style", "border-color", "border-radius", "border-width"],
        button: ["color", "font-size", "background-color", "border-style", "border-color", "border-radius", "border-width", "width", "height"],
        icon: ["color", "font-size", "icon", "link", "target"],
        link: ["color", "font-size", "background-color", "border-style", "border-color", "border-radius", "border-width", "margin-top", "margin-bottom", "margin-left", "margin-right", "padding-top", "padding-bottom", "padding-left", "padding-right", "link", "target"]

    };
    var PROPERTIES_RENDER_CONFIG = {
        "text-align": {
            type: "select",
            options: { "left": "left", "right": "right", "center": "center", "justify": "justify", "justify-all": "justify.all", "start": "start", "end": "end", "match-parent": "match.parent"}
        },
        "background-repeat": {
            type: "select",
            options: { "repeat-x": "repeat-x", "repeat-y": "repeat-y", "repeat": "repeat", "space": "space", "round": "round", "no-repeat": "no-repeat"}
        },
        "border-style": {
            type: "select",
            options: {"none": "none", "dotted": "dotted", "dashed": "dashed", "solid": "solid", "double": "double", "groove": "groove", "ridge": "ridge", "inset": "inset", "outset": "outset", "initial": "initial", "inherit": "inherit"}
        },
        "icon": {},
        "target": {
            type: "select",
            func: "attr",
            options: { '_self': '_self', '_blank': '_blank', '_parent': '_parent', '_top': '_top'}
        }
    };
    app.snippet_config_builder = function(editor) {
        this.editor = editor
        PROPERTIES_RENDER_CONFIG.icon.options = JSON.parse(editor.body.find(".icon-cache").text())
    };
    var _b = app.snippet_config_builder.prototype;

    _b.buildConfig = function(type, node) {
        var _self = this, configHtml = '<div class="side-bar-config ' + type + '">', tagName = node.prop("tagName"), properties = []
        properties.pushAll(DATA_STYLE_PROPERTIES[type])
        if($.inArray(tagName, ['H1', 'H2', 'H3', 'H4', 'H5', 'H6']) != -1) {
            properties.pushAll(["link", "target"])
        }
        if(node.attr("data-cloneable") == "true") {
            configHtml += '<div class="config-group clone-remove-button-holder">' +
                    '<button class="clone">' + $.i18n.prop('clone') + '</button><button class="move move-prev" direction="prev">' + $.i18n.prop('move.prev') + '</button>' +
                    '<button class="move move-next" direction="next">' + $.i18n.prop('move.next') + '</button><button class="remove">' + $.i18n.prop('remove') + '</button>' +
                '</div>'
        }
        properties.every(function(index, prop) {
           configHtml += '<div class="config-group '+ prop + '">'
            configHtml += '<label>' + $.i18n.prop(prop) + '</label>'
            var builderFunction = "buildConfigFor" + prop.camelCase()
            if(_self[builderFunction]) {
                configHtml += _self[builderFunction](node, prop);
            } else {
                configHtml += _self.buildConfigProperty(prop, node)
            }
            configHtml += '</div>'
        });
        configHtml += '</div>';
        configHtml = $(configHtml)
        _self.attachEvents(configHtml)
        return configHtml
    };

    _b.buildConfigProperty = function(prop, node) {
        var html = "", config = PROPERTIES_RENDER_CONFIG[prop], func = config && config.func ? config.func : "css",  value = node[func](prop);
        if(config && config.type == "select") {
            html += '<select class="config" name="' + prop + '">'
            $.each(config.options, function(key, val) {
                html += '<option value="' + key + '" ' + (val == value ? "selected" : "") + '>' + $.i18n.prop(val) + '</option>'
            });
            html += '</select>'
        } else {
            html +='<input type="text" name="' + prop + '" class="config" value="' + value + '"/>'
        }
        return html
    };

    _b.buildConfigForImgSrc = function(node, prop) {
        return '<input type="text" name="' + prop + '" class="config" value="' + node.attr("src")  + '"/><span class="tool-icon upload asset-library-upload-btn"><span/>'
    };

    _b.buildConfigForBackgroundImage = function(node, prop) {
        return '<input type="text" name="' + prop + '" class="config" value="' + node.css("background-image")  + '"/><span class="tool-icon upload asset-library-upload-btn"><span/>'
    };

    _b.buildConfigForLink = function(node) {
        var tagName = node.prop("tagName");
        if(tagName != "A") {
            node  = node.parent("a[wrapper-link]")
        }
        return '<input type="text" name="link" class="config" value="' + (node.length ? node.attr("href")  : "") + '"/>'
    };

    _b.buildConfigForTarget = function(node) {
        var tagName = node.prop("tagName");
        if(tagName != "A") {
            node  = node.parent("a[wrapper-link]")
        }
        return this.buildConfigProperty("target", node)
    };

    _b.buildConfigForIcon = function(node) {
        var value = node.attr("icon-class"), html = '<div class="chosen wcui-select config" name="icon">'
        $.each(PROPERTIES_RENDER_CONFIG.icon.options, function(k, v) {
            html += '<div value="' + k + '" class="options ' + (k == value ? 'selected' : '')  + '"> <img src="' + app.systemResourceUrl + "plugins/snippet/images/icons/"+ v + '"/></div>'
        });
        html += '</div>'
        return html
    };

    _b.attachEvents = function(config) {
        config.updateUi();
        config.find(".config-group.color input, .config-group.background-color input, .config-group.border-color input").spectrum({
            showInput: true,
            preferredFormat: "rgb"
        });
        config.find(".asset-library-upload-btn").on("click", function() {
            var srcInput = $(this).siblings("input")
            bm.uploadImageToAssetLibrary({
                success: function(resp) {
                    if(srcInput.attr('name') == 'background-image') {
                        resp.url = "url('" + resp.url + "')"
                    }
                    srcInput.val(resp.url).trigger("change")
                }
            })
        });
    };

    _b.onConfigChange = function(configInput, elm) {
        var configName = configInput.attr('name'), config = PROPERTIES_RENDER_CONFIG[configName], func = config && config.func ? config.func : "css";
        var changeFunction = "on" + configName.camelCase() + "ConfigChange";
        if(this[changeFunction]) {
            this[changeFunction](configInput, elm);
        } else {
            elm[func](configName, configInput.val())
        }
    };

    _b.onImgSrcConfigChange = function(configInput, elm) {
        elm.attr("src", configInput.val())
    };

    _b.onLinkConfigChange = function(configInput, elm) {
        var tagName = elm.prop("tagName");
        if(tagName != "A") {
            if(elm.parent("a[wrapper-link]").length == 0) {
                elm.wrap('<a href="#" wrapper-link></a>')
            }
            elm = elm.parent("a[wrapper-link]");
        }
        elm.attr("href", configInput.val())
    };

    _b.onTargetConfigChange = function(configInput, elm) {
        var tagName = elm.prop("tagName");
        if(tagName != "A") {
            if(elm.parent("a[wrapper-link]").length == 0) {
                elm.wrap('<a href="#" wrapper-link></a>')
            }
            elm = elm.parent("a[wrapper-link]");
        }
        elm.attr("target", configInput.val())
    };

    _b.onIconConfigChange = function(configInput, elm) {
        elm.removeClass(elm.attr("icon-class")).addClass(configInput.val()).attr("icon-class", configInput.val())
    };
})();