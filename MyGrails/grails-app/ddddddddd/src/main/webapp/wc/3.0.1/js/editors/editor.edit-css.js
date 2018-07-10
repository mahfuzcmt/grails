app.tabs.cssEditor = function() {
    app.tabs.cssEditor._super.constructor.apply(this, arguments);
    this.constructor_args = arguments;
    this.text = $.i18n.prop("edit.css");
    this.tip = $.i18n.prop(this.file.name);
    this.ui_class = "edit-css edit-tab";
    this.ui_body_class = "simple-tab";
    this.strict_layout = false;
    this.name = this.file.name;
}

var _e = app.tabs.cssEditor.inherit(app.Tab);

_e.cssContent = "";

_e.onSwitchMenuClick = function(type) {
    var _self = this;
    if(type == "advanceEditor") {
        _self.cssContent = _self.parser.toString(1)
    } else {
        _self.cssContent = _self.cmEditor.getValue()
        $(window).unbind("resize." + _self.id);
    }
    _self.constructor_args[0].cssContent = _self.cssContent
    app.Tab.changeView(_self, "cssEditor", type);
}

_e.setDirty = function () {
    this.body.find(".toolbar-item.save").removeClass("disabled");
    app.tabs.cssEditor._super.setDirty.call(this);
}

_e.clearDirty = function () {
    this.body.find(".toolbar-item.save").addClass("disabled");
    app.tabs.cssEditor._super.clearDirty.call(this);
}

_e.save = function (callback) {
    var _self = this;
    if(_self.body.find(".toolbar-item.save.disabled").length) {
        return;
    }
    var css;
    if(_self.cmEditor) {
        css = _self.cmEditor.getValue();
    } else {
        css = _self.parser.toString(1)
    }
    bm.ajax({
        url: app.baseUrl + _self.file.path,
        method: 'put',
        data: css,
        success: function() {
            _self.clearDirty();
            if(callback) {
                callback();
            }
        }
    })
}

app.tabs.cssEditor.simpleEditor = function (args) {
    this.file = args.file;
    this.id = args.id;
    app.tabs.cssEditor.simpleEditor._super.constructor.apply(this, arguments);
}

var _s = app.tabs.cssEditor.simpleEditor.inherit(app.tabs.cssEditor);

(function () {
    function attachEvents () {
        var _self = this;
        this.body.find(".toolbar-item.save").click(function () {
            _self.save();
        });
    }

    function initialize(css) {
        this.parser = new CssParser(css);
        this.parser.parse();
        this.simpleCss = new app.tabs.simpleCss(this, this.parser);
        this.simpleCss.init();
    }

    _s.init = function () {
        app.tabs.cssEditor.simpleEditor._super.init.call(this);
        var _self = this;
        if(this.cssContent) {
            initialize.call(this, this.cssContent);
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

_s.ajax_url = app.baseUrl + "assetLibrary/loadCssEditor";

_s.switch_menu_entries = [
    {
        text: $.i18n.prop("switch.advance.mode"),
        ui_class: "view-switch editor-advance-mode",
        action: "advanceEditor"
    }
];

app.tabs.cssEditor.advanceEditor = function (args) {
    this.file = args.file;
    this.id = args.id;
    app.tabs.cssEditor.advanceEditor._super.constructor.apply(this, arguments);
}

var _a = app.tabs.cssEditor.advanceEditor.inherit(app.tabs.cssEditor);

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
        $(_self.body.find(".cssEditorBody .CodeMirror")[0]).bind("keydown", function(event) {
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

    _a.init = function () {
        var _self = this;
        app.tabs.cssEditor.advanceEditor._super.init.call(this);
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
                _self.loadEditor(response);
                attachEvents.call(_self);
            }
        });
    }
})()


_a.ajax_url = app.baseUrl + "assetLibrary/loadCodeEditor";

_a.switch_menu_entries = [
    {
        text: $.i18n.prop("switch.simple.mode"),
        ui_class: "view-switch editor-simple-mode",
        action: "simpleEditor"
    }
];

_a.loadEditor = function(css) {
    var _self = this;
    var textArea = _self.body.find(".cssEditorBody .code-area");
    textArea.val(css);
    this.cmEditor = textArea.cssCodeEditor();
    _self.resizeEditor();
    $(window).bind("resize." + _self.id, function() {
        _self.resizeEditor();
    });
    _self.on("close", function() {
        $(window).unbind("resize." + _self.id);
    });
}

_a.resizeEditor = function() {
    this.cmEditor.setSize(null, this.body.height() - this.body.find(".header").height());
    this.cmEditor.refresh();
}

_a.bindEditorEvent = function() {
    var _self = this;
    var cm = _self.cmEditor;
    if(!cm) {
        return;
    }
    cm.on("change", function(CodeMirror) {
        var hSize = CodeMirror.historySize();
        app.tabs.cssEditor.cssContent = CodeMirror.getValue();
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

_a.jumpToLine = function(line) {
    var mirror = this.cmEditor;
    if (line && !isNaN(Number(line))) {
        var line = Number(line) - 1
        mirror.setCursor(line,0);
        mirror.setSelection({line:line,ch:0},{line:line+1,ch:0});
        mirror.focus();
    }
}

_a.initGoToPopup = function () {
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

_a.reFormatSelection = function() {
    var mirror = this.cmEditor;
    var start = mirror.getCursor(true)["line"];
    var end = mirror.getCursor(false)["line"];
    for(var line = start; line <= end; line++) {
        mirror.indentLine(line);
    }
}

_a.searchText = function (findString) {
    var mirror = this.cmEditor;
    CodeMirror.commands.clearSearch(mirror)
    CodeMirror.commands.findAll(mirror, findString, false)
}