app.tabs.scriptEditor = function () {
    app.tabs.scriptEditor._super.constructor.apply(this, arguments);
    this.constructor_args = arguments;
    var mode = this.file.mode;
    this.text = $.i18n.prop("edit." + mode);
    this.tip = $.i18n.prop(this.file.name);
    this.ui_class = "edit-" + mode + " edit-tab";
    this.ui_body_class = "simple-tab";
    this.name = this.file.name;
    this.mode = mode;
    this.ajax_url = app.baseUrl + "assetLibrary/loadScriptEditor?mode=" + mode;
}

var _se = app.tabs.scriptEditor.inherit(app.Tab);

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
        $(_self.body.find(".scriptEditorBody .CodeMirror")[0]).bind("keydown", function(event) {
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
        var _self = this;
        app.tabs.scriptEditor._super.init.call(_self);
        bm.ajax({
            type: 'get',
            url: app.baseUrl + (_self.file.loadUrl ? _self.file.loadUrl : "remoteRepository/download"),
            data: {path : _self.file.path},
            dataType: 'text',
            show_response_status: false,
            success: function(response) {
                _self.loadEditor(response);
                attachEvents.call(_self);
            }
        });
    }
})();

_se.onSwitchMenuClick = function(type) {
    $(window).unbind("resize." + this.name);
    this.switchHandler(type);
}

_se.loadEditor = function(content) {
    var _self = this;
    var textArea = _self.body.find(".scriptEditorBody .code-area");
    textArea.val(content);
    _self.cmEditor = CodeMirror.fromTextArea(textArea[0], {
        lineNumbers: true,
        extraKeys: {"Ctrl-Space": "autocomplete"},
        mode: _self.mode,
        theme: "eclipse",
        highlightSelectionMatches: {showToken: /\w/},
        showCursorWhenSelecting: true,
        matchBrackets: true,
        autoCloseBrackets: true
    })
    _self.resizeEditor();
    $(window).bind("resize." + _self.name, function() {
        _self.resizeEditor();
    });
    _self.on("close", function() {
        $(window).unbind("resize." + _self.name);
    });
}

_se.resizeEditor = function() {
    this.cmEditor.setSize(null, this.body.height() - this.body.find(".header").height());
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
    var input = popupDom.find("input[name=lineNo]");
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
    CodeMirror.commands.clearSearch(mirror)
    CodeMirror.commands.findAll(mirror, findString, false)
}

_se.setDirty = function () {
    this.body.find(".toolbar-item.save").removeClass("disabled");
    app.tabs.scriptEditor._super.setDirty.call(this);
}

_se.clearDirty = function () {
    this.body.find(".toolbar-item.save").addClass("disabled");
    app.tabs.scriptEditor._super.clearDirty.call(this);
}

_se.save = function (callback) {
    var _self = this;
    if(_self.body.find(".toolbar-item.save.disabled").length) {
        return;
    }
    bm.ajax({
        url: app.baseUrl + (_self.file.saveUrl ? _self.file.saveUrl : _self.file.path),
        method: _self.file.method ? _self.file.method : 'put',
        data: _self.file.saveUrl ? {data: _self.cmEditor.getValue()} : _self.cmEditor.getValue(),
        success: function() {
            _self.clearDirty();
            if(callback) {
                callback();
            }
        }
    })
}