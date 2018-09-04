app.tabs.xmlEditor = function () {
    app.tabs.xmlEditor._super.constructor.apply(this, arguments);
    this.constructor_args = arguments;
    this.text = $.i18n.prop(this.file.name);
    this.tip = $.i18n.prop(this.text);
    this.ui_class = "edit-xml edit-tab";
    this.ui_body_class = "simple-tab";
    this.strict_layout = false;
    this.name = this.file.name;
    this.readOnly = this.file.readOnly ? this.file.readOnly : false;
}

var _xe = app.tabs.xmlEditor.inherit(app.Tab);

(function () {
    function attachEvents() {
        var _self = this;
    }

    _xe.init = function () {
        app.tabs.xmlEditor._super.init.call(this);
        attachEvents.call(this);
    }
})();

_xe.xmlContent = "";

_xe.onSwitchMenuClick = function(type) {
    var _self = this;
    if(type == "simpleEditor") {
        try {
            var tempString = _self.xmlEditor.getXmlAsString();
            if(tempString.indexOf("?>") > 0) {
                tempString = tempString.substr(0, tempString.indexOf("?>")+2) + "\n" + tempString.substr(tempString.indexOf("?>") + 2, tempString.length);
            }
            _self.xmlContent =  tempString;
        } catch(error) {
            _self.xmlContent = _self.xml;
            bm.notify($.i18n.prop("invalid.xml.document"), "error");
        }
    } else {
        var xml = _self.cmEditor.getValue();
        _self.xmlContent = xml ? xml : '<?xml version="1.0" encoding="UTF-8"?><root></root>';
        $(window).unbind("resize." + this.id);
    }
    _self.constructor_args[0].xmlContent = _self.xmlContent
    app.Tab.changeView(_self, "xmlEditor", type);
}

_xe.setDirty = function () {
    this.body.find(".toolbar-item.save").removeClass("disabled");
    app.tabs.xmlEditor._super.setDirty.call(this);
}

_xe.clearDirty = function () {
    this.body.find(".toolbar-item.save").addClass("disabled");
    app.tabs.xmlEditor._super.clearDirty.call(this);
}

_xe.save = function (callback) {
    var _self = this;
    if(_self.body.find(".toolbar-item.save.disabled").length) {
        return;
    }
    var xml;
    if(_self.cmEditor) {
        xml = _self.cmEditor.getValue();
    } else {
        xml = _self.xmlEditor.getXmlAsString();
    }
    bm.ajax({
        url: app.baseUrl + _self.file.path,
        method: 'put',
        data: xml,
        success: function() {
            _self.clearDirty();
            if(callback) {
                callback();
            }
        }
    })
}

app.tabs.xmlEditor.advanceEditor = function (args) {
    this.file = args.file;
    this.id = args.id;
    app.tabs.xmlEditor.advanceEditor._super.constructor.apply(this, arguments);
}

var _ae = app.tabs.xmlEditor.advanceEditor.inherit(app.tabs.xmlEditor);

(function () {
    function attachEvents () {
        var _self = this;
        this.body.find(".toolbar-item.save").click(function () {
            _self.save();
        });
    }

    function initialize(xmlContent) {
        var _self = this;
        _self.xml = xmlContent ? xmlContent : '<?xml version="1.0" encoding="UTF-8"?><root></root>';
        _self.initAdvanceEditor();
    }

    _ae.init = function () {
        app.tabs.xmlEditor.advanceEditor._super.init.call(this);
        var _self = this;
        if(this.xmlContent) {
            initialize.call(this, this.xmlContent);
        } else {
            bm.ajax({
                type: 'get',
                controller: "remoteRepository",
                action: "download",
                data: {
                    path : _self.file.path
                },
                dataType: 'text',
                show_response_status: false,
                success: function(response) {
                    initialize.call(_self, response);
                }
            });
        }
        attachEvents.call(this);
    }
})();

_ae.ajax_url = app.baseUrl + "assetLibrary/loadAdvanceXmlEditor";

_ae.switch_menu_entries = [
    {
        text: $.i18n.prop("switch.simple.mode"),
        ui_class: "view-switch editor-simple-mode",
        action: "simpleEditor"
    }
];

_ae.initAdvanceEditor = function() {
    var _self = this;
    _self.xmlEditor = new xmlEditor();
    var editorBlock = _self.body.find(".advanceEditorBody #xml")
    _self.xmlEditor.loadXmlFromString(_self.xml, editorBlock, function() {
        editorBlock.show();
        editorBlock.find("#actionButtons").show();
        _self.xmlEditor.renderTree();
        _self.xmlEditor.on("contentChanged", function() {
            _self.setDirty();
        })
    });
}

app.tabs.xmlEditor.simpleEditor = function () {
    app.tabs.xmlEditor.simpleEditor._super.constructor.apply(this, arguments);
}

var _se = app.tabs.xmlEditor.simpleEditor.inherit(app.tabs.xmlEditor);

(function () {
    function attachEvents() {
        var _self = this;
        var mirror = _self.cmEditor;
        _self.bindEditorEvent();
        _self.body.find(".toolbar form").form({
            preSubmit: function() {
                var input = $(this).find("input:text");
                _self.searchText(input.val());
                return false;
            }
        });
        _self.body.find(".toolbar-item.next").click(function () {
            CodeMirror.commands.findNext(mirror)
            return false;
        });
        _self.body.find(".toolbar-item.previous").click(function () {
            CodeMirror.commands.findPrev(mirror)
            return false;
        });
        _self.body.find(".toolbar-item.save").click(function () {
            _self.save();
        });
        $(_self.body.find(".simpleEditorBody .CodeMirror")[0]).bind("keydown", function(event) {
            if (event.ctrlKey || event.metaKey) {
                switch (String.fromCharCode(event.which).toLowerCase()) {
                    case 's':
                        event.preventDefault();
                        _self.save();
                        break;
                }
            }
            if(event.ctrlKey && event.altKey) {
                switch (String.fromCharCode(event.which).toLowerCase()) {
                    case 'l':
                        event.preventDefault();
                        _self.reFormatSelection();
                        break;
                    case 'g':
                        event.preventDefault();
                        _self.initGoToPopup();
                        break;
                }
            }
        })
        _self.body.find(".toolbar-item.undo").click(function () {
            if($(this).hasClass("disabled")) {
                return this;
            }
            _self.cmEditor.undo();
        });
        _self.body.find(".toolbar-item.redo").click(function () {
            if($(this).hasClass("disabled")) {
                return this;
            }
            _self.cmEditor.redo();
        });
    }

    _se.init = function () {
        app.tabs.xmlEditor.simpleEditor._super.init.call(this);
        this.loadEditor();
        attachEvents.call(this);
    }
})()


_se.ajax_url = app.baseUrl + "assetLibrary/loadSimpleXmlEditor";

_se.switch_menu_entries = [
    {
        text: $.i18n.prop("switch.advance.mode"),
        ui_class: "view-switch editor-advance-mode",
        action: "advanceEditor"
    }
];

_se.loadEditor = function() {
    var _self = this;
    var xml = this.xmlContent
    var textArea = _self.body.find(".simpleEditorBody .code-area");
    textArea.val(xml);
    this.cmEditor = CodeMirror.fromTextArea(textArea[0], {
        lineNumbers: true,
        extraKeys: {"Ctrl-Space": "autocomplete"},
        mode: "xml",
        theme: "eclipse",
        highlightSelectionMatches: {showToken: /\w/},
        showCursorWhenSelecting: true,
        matchBrackets: true,
        autoCloseBrackets: true,
        readOnly: _self.readOnly
    })
    _self.resizeEditor();
    $(window).bind("resize." + _self.id, function() {
        _self.resizeEditor();
    });
    _self.on("close", function() {
        $(window).unbind("resize." + _self.id);
    });
}

_se.resizeEditor = function() {
    this.cmEditor.setSize(null, this.body.height() - this.body.find(".header").height() - 25);
    this.cmEditor.refresh();
}

_se.bindEditorEvent = function() {
    var _self = this;
    var cm = _self.cmEditor;
    if(!cm) {
        return;
    }
    cm.on("change", function(CodeMirror) {
        var hSize = CodeMirror.historySize();
        app.tabs.xmlEditor.xmlContent = CodeMirror.getValue();
        if (hSize['undo'] > 0) {
            _self.setDirty();
            _self.body.find(".toolbar-item.undo").removeClass("disabled");
        } else {
            _self.clearDirty();
            _self.body.find(".toolbar-item.undo").addClass("disabled");
        }
        if (hSize['redo'] > 0) {
            _self.body.find(".toolbar-item.redo").removeClass("disabled");
        } else {
            _self.body.find(".toolbar-item.redo").addClass("disabled");
        }
    })
}

_se.jumpToLine = function(line) {
    var mirror = this.cmEditor;
    if (line && !isNaN(Number(line))) {
        var line = Number(line) - 1
        mirror.setCursor(line,0);
        mirror.setSelection({line:line,ch:0},{line:line+1,ch:0});
        mirror.focus();
    }
}

_se.initGoToPopup = function () {
    var _self = this;
    var popupDom = $("<form class='edit-popup-form'><div class='form-row mandatory'>" +
        "<label>" + $.i18n.prop("line.no") + "</label>" +
        "<input type='text' name='lineNo' value='0' validation='digits'>" +
        "</div>" +
        "<div class='button-line'>" +
        "<button type='button' class='cancel-button'> " + $.i18n.prop("cancel") + "</button> &nbsp; &nbsp;" +
        "<button type='submit' class='submit-button'>" + $.i18n.prop("enter") + "</button>" +
        "</div></form>");
    var input = popupDom.find("input:text");
    bm.editPopup(undefined, $.i18n.prop("go.to.line"), undefined, undefined, {
        content: popupDom,
        events: {
            content_loaded: function() {
                var popDom = this;
                input.focus();
                popDom.find(".cancel-button").click(function () {
                    popDom.close();
                });
            }
        },
        beforeSubmit: function(form, data, popup) {
            popup.close();
            _self.jumpToLine(input.val());
            return false;
        }
    })
}

_se.reFormatSelection = function() {
    var mirror = this.cmEditor;
    var start = mirror.getCursor(true)["line"];
    var end = mirror.getCursor(false)["line"];
    for(var line = start; line <= end; line++) {
        mirror.indentLine(line);
    }
}

_se.searchText = function (findString) {
    var mirror = this.cmEditor;
    CodeMirror.commands.clearSearch(mirror);
    CodeMirror.commands.findAll(mirror, findString, false);
}