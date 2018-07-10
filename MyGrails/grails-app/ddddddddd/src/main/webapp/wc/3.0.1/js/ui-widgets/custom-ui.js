(function() {
    $.prototype.updateUi = function() {
        //if(app.is_front_end) { return this }
        this.find("select:not([multiple], .raw), div.wcui-select.chosen").chosen();
        var multipleSelect = this.find("select[multiple]:not(.raw)");
        multipleSelect.not(".special-select-chosen").chosen();
        var specialSelect = multipleSelect.filter(".special-select-chosen");
        specialSelect.each(function() {
            var chosen = $(this);
            chosen.multispecialchosen(chosen.config("chosen"))
        });
        this.find("div.multitxtchosen").multitxtchosen({type: "multi"});
        this.find("input.multitxtchosen").multitxtchosen({type: "csv"});
        this.find("input.spinner").each(function() {
            var _this = $(this);
            var config = {
                min : _this.attr("min"),
                max : _this.attr("max")
            };
            _this.trigger("onStepperBind", [config]);
            _this.stepper(config);
        });
        this.find("paginator").paginator({
            next: $.i18n.prop("next"),
            prev: $.i18n.prop("prev"),
            first: "<<",
            last: ">>"
        });
        this.find(".bmui-tab:not(.alone)").tabify({
            loader_template: "<span class='vertical-aligner'></span><span class='loader'></span>"
        });
        this.find("textarea.wceditor").each(function() {
            var editor = $(this);
            var config = {
                callbacks: {
                    focus: function() {
                        var validator = editor.data("validator-filed-inst");
                        if(validator) validator.clear()
                    },
                    blur: function() {
                        var validator = editor.data("validator-filed-inst");
                        if(validator) validator.validate()
                    },
                    change: function() {
                        editor.trigger("change")
                    }
                },
                imageUpload: app.baseUrl + 'app/uploadWceditorImage?type=redactor',
                imageManagerJson: app.baseUrl + 'app/wceditorImages',
                fileUpload: app.baseUrl + 'app/uploadWceditorFile?type=redactor',
                fileManagerJson: app.baseUrl + 'app/wceditorFiles',
                plugins: ['fontcolor', 'fontsize', 'alignment', 'inlinestyle', 'properties', 'table', 'fullscreen', 'video', 'imagemanager', 'definedlinks', 'clips', 'limiter', 'textdirection', 'filemanager']
            };
            if(editor.attr('toolbar-type') == 'super_simple') {
                config.plugins = ['inlinestyle', 'definedlinks', 'limiter'];
                config.buttons = ['format', "inlinestyle", 'bold', 'italic', 'link']
            }
            else if(editor.attr('toolbar-type') == 'simple') {
                config.plugins = ['fontcolor', 'fontsize', 'alignment', 'table', 'video', 'imagemanager', 'definedlinks', 'limiter', 'filemanager', 'properties', 'fullscreen', 'textdirection', 'inlinestyle'];
                config.buttons = ['html', 'format', 'bold', 'italic', 'deleted', 'lists', 'image', 'file', 'link']
            }
            editor.redactor(config)
        });
        this.find(".accordion-panel").accordion();
        this.find(".datefield-between").each(function() {
            var group = $(this);
            group.find('.datefield-from').date({
                direction: undefined,
                show_select_today: false,
                lang_clear_date: $.i18n.prop("clear"),
                pair: group.find(".datefield-to").date({
                    direction: true,
                    show_select_today: false,
                    lang_clear_date: $.i18n.prop("clear")
                })
            });
        });
        this.find(".timefield-between").each(function() {
            var group = $(this);
            group.find('.timefield-from').date({
                direction: undefined,
                show_select_today: false,
                lang_clear_date: $.i18n.prop("clear"),
                pair: group.find(".timefield-to").date({
                    direction: true,
                    show_select_today: false,
                    lang_clear_date: $.i18n.prop("clear"),
                    time: "HH:mm:ss"
                }),
                time: "HH:mm:ss"
            });
        });
        this.find(".datefield").each(function() {
            var field = $(this);
            field.date({
                direction: field.attr("no-previous") ? true : (field.attr("no-next") ? false : undefined),
                show_select_today: false,
                lang_clear_date: $.i18n.prop("clear")
            })
        });
        this.find(".timefield").each(function() {
            var field = $(this);
            field.date({
                direction: field.attr("no-previous") ? true : (field.attr("no-next") ? false : undefined),
                show_select_today: false,
                lang_clear_date: $.i18n.prop("clear"),
                time: "HH:mm:ss"
            })
        });
        this.find("input[type='file']").file();
        this.find("input:checkbox").checkbox();
        this.find("input:radio").radio();
        if(!app.is_front_end) {
            this.find('[title][title!=""]').tooltipster({
                contentAsHTML: true
            });
        }
        bm.cBaseDoc(this.find(".context-documentation"));
        this.find(".chart-block").chart();
        this.find("textarea, .redactor-layer").on("mousewheel", function(event, delta) {
            var $this = $(this);
            var scrollTop = $this.scrollTop();
            var scrollHeight = this.scrollHeight;
            var innerHeight = $this.innerHeight();
            $this.scrollTop(scrollTop - delta * 40);
            scrollTop = $this.scrollTop();
            if(scrollTop != 0 && (scrollTop + innerHeight) < scrollHeight) {
                event.originalEvent.ignoreSroll = true
            }
        });
        return this;
    };

    ////////////// checkbox //////////////////////

    var _checkbox_classes = "checked partial unchecked";

    var _checkbox_pubs = {
        state: function(state) {
            if(state) {
                switch(state) {
                    case "checked":
                        if(this.state != 1) {
                            _checkbox_privs.state.call(this, 1);
                        }
                        break;
                    case "unchecked":
                        if(this.state != 4) {
                            _checkbox_privs.state.call(this, 4);
                        }
                        break;
                    case "partial":
                        if(this.state == 1 || this.state == 4) {
                            this.state == 1 ? _checkbox_privs.state.call(this, 2) : (this.state == 4 ? _checkbox_privs.state.call(this, 3) : "")
                        }
                        break;
                }
                return;
            }
            return this.state == 1 ? "checked" : (this.state == 4 ? "unchecked" : "partial")
        },
        enable: function(isEnabled) {
            if(isEnabled) {
                this.el.removeAttr("disabled", "disabled");
                this.removeClass("disabled")
            } else {
                this.addClass("disabled");
                this.el.attr("disabled", "disabled")
            }
        }
    };

    var _checkbox_privs = {
        state: function(state) {
            this.removeClass(_checkbox_classes);
            switch(state) {
                case 1:
                    this.el[0].checked = true;
                    this.addClass("checked");
                    break;
                case 2:
                case 3:
                    this.addClass("partial");
                    break;
                case 4:
                    this.el[0].checked = false;
                    this.addClass("unchecked");
                    break;
            }
            this.state = state;
        }
    };

    var _checkbox_prop = $.prototype.prop;

    $.prototype.prop = function(propName, value) {
        if(propName == "checked" && value != undefined) {
            return this.each(function() {
                var check = $(this);
                var instance = check.data("wcui-checkbox");
                if(instance) {
                    _checkbox_pubs.state.call(instance, value ? "checked" : (value === false ? "unchecked" : undefined))
                } else {
                    _checkbox_prop.apply(check, [propName, value]);
                }
            })
        } else {
            return _checkbox_prop.apply(this, arguments);
        }
    };

    $.prototype.checkbox = function(func) {
        if(!this.length || $(this).hasClass('skip-wcui')) {
            return this;
        }
        if(func) {
            var toReturn;
            var params = Array.prototype.splice.call(arguments, 1);
            this.each(function() {
                var instance = $(this).data("wcui-checkbox");
                if(!instance) {
                    throw $.error(func + " is being called before initialization");
                }
                toReturn = _checkbox_pubs[func].apply(instance, params);
                return toReturn == undefined;
            });
            return toReturn == undefined ? this : toReturn;
        }
        return this.each(function() {
            var original = $(this);
            if(original.css("display") == "none") {
                return;
            }
            var instance = original.data("wcui-checkbox");
            if(instance) {
                return;
            }
            var type = "";
            if(original.is(".single")) {
                type = "single";
            } else if(original.is(".multiple")) {
                type = "multiple";
            }
            var fancy = $("<span class='wcui-checkbox " + type + "'></span>");
            instance = $.extend({}, {el: original}, fancy);
            original.data("wcui-checkbox", instance);
            original.after(fancy).hide();
            if(original.is("[disabled]")) {
                fancy.addClass("disabled");
            }
            if(original.is("[title]")) {
                fancy.attr("title", original.attr("title"));
            }
            if(this.checked) {
                _checkbox_privs.state.call(instance, 1)
            } else {
                _checkbox_privs.state.call(instance, 4)
            }
            fancy.bind("click", function() {
                if(original.is("[disabled]")) {
                    return;
                }
                switch(instance.state) {
                    case 1:
                        _checkbox_privs.state.call(instance, 4);
                        break;
                    case 2:
                        _checkbox_privs.state.call(instance, 4);
                        break;
                    case 3:
                        _checkbox_privs.state.call(instance, 1);
                        break;
                    case 4:
                        _checkbox_privs.state.call(instance, 1);
                        break;
                }
                original.triggerWithPropagation("change");
            });
        })
    };

    ////////////// radio //////////////////////

    var _radio_classes = "checked unchecked";

    var _radio_privs = {
        state: function(state) {
            this.removeClass(_radio_classes);
            switch(state) {
                case 1:
                    this.el[0].checked = true;
                    this.addClass("checked");
                    break;
                case 2:
                    this.addClass("unchecked");
                    break;
            }
            this.state = state;
        },
        clear_others: function() {
            var group = this.group;
            if(group) {
                $.each(group, function() {
                    if(this.el[0].checked) {
                        _radio_privs.state.call(this.el.data("wcui-radio"), 2);
                        return false;
                    }
                })
            }
        }
    };

    var _radio_pubs = {
        check: function() {
            if(this.state != 1) {
                _radio_privs.clear_others.call(this);
                _radio_privs.state.call(this, 1);
            }
        },
        val: function(value) {
            var group = this.group;
            if(typeof value != "undefined") {
                var to_check;
                $.each(group || [this], function() {
                    if(this.el.val() == value) {
                        to_check = this;
                        return false;
                    }
                });
                if(!to_check) {
                    return false;
                }
                _radio_pubs.check.call(to_check);
                return true;
            } else {
                var val;
                $.each(group || [this], function() {
                    if(this.el[0].checked) {
                        val = this.el.val();
                        return false;
                    }
                });
                return val;
            }
        },
        state: _radio_privs.state
    };

    $.prototype.radio = function(func) {
        if(!this.length) {
            return this;
        }
        if(func) {
            var toReturn;
            var params = Array.prototype.splice.call(arguments, 1);
            this.each(function() {
                var instance = $(this).data("wcui-radio");
                if(!instance) {
                    throw $.error(func + " is being called before initialization");
                }
                toReturn = _radio_pubs[func].apply(instance, params);
                return toReturn == undefined;
            });
            return toReturn == undefined ? this : toReturn;
        }
        var groups = {};
        return this.each(function() {
            var original = $(this);
            var instance = original.data("wcui-radio");
            if(instance) {
                return;
            }
            var fancy = $("<span class='wcui-radio'></span>");
            if(original.css("display") == "none") {
                fancy.hide();
            }
            var name = original.attr("name");
            instance = $.extend({}, {el: original}, fancy);
            if(name) {
                var group = groups[name];
                if(!group) {
                    group = [];
                    groups[name] = group;
                }
                instance.group = group;
                group.push(instance);
            }
            original.data("wcui-radio", instance);
            original.after(fancy).hide();
            if(original.is("[disabled]")) {
                fancy.addClass("disabled");
            }
            if(this.checked) {
                _radio_privs.state.call(instance, 1)
            } else {
                _radio_privs.state.call(instance, 2)
            }
            fancy.bind("click", function() {
                if(instance.state == 1 || original.is("[disabled]")) {
                    return;
                }
                _radio_pubs.check.call(instance);
                original.triggerWithPropagation("change");
            });
        })
    };

    ////////////// Custom File Chooser ///////////

    var formFieldDataKey = "form-extra-data";

    function FileDropHandler(input) {
        var _self = this;
        this.uuid = bm.getUUID();
        this.input = input;
        this.clazz = input.attr("class");
        this.auto = input.attr("submit") == "auto";
        if(this.auto) {
            this.submit_url = this.input.attr("ajax-url");
            this.submit_data = this.input.attr("submit-data");
        }
        this.form = this.auto ? null : this.input.closest("form");
        if(this.form && !this.form.length) {
            this.form = null;
        }
        this.input.addClass("masked-file-input");
        this.wrapper = $("<div class='dropzone-wrapper'></div>");
        if(this.clazz) {
            this.wrapper.addClass(this.clazz)
        }
        this.input.after(this.wrapper);
        if(window.FormData) {
            this.dropZone = $("<div class='dropzone'><span class='dropzone-text'></span></div>");
            this.dropZone.find(".dropzone-text").text(this.dropText);
            this.wrapper.append(this.dropZone)
        } else {
            this.dropZone = $("<button type='button' class='fake-file-chooser'></button>").text(this.chooserText);
            this.dropZone.wrap("<span class='input-wrapper-false-mask' style='display: inline-block;'></span>");
            this.wrapper.append(this.dropZone);
            this.dropZone.after(input);
            var width = input.width();
            this.dropZone.on("mousemove", function(e) {
                var x = e.pageX;
                var y = e.pageY;
                var newPosition = {
                    left: x - width + 5,
                    top: y - 5
                };
                input.offset(newPosition);
            });
            input.parent().width(this.dropZone.outerWidth()).height(this.dropZone.outerHeight());
            this.dropZone.off("click")
        }
        var clazz = this.input.attr("clazz");
        if(clazz) {
            this.dropZone.addClass(clazz);
        }
        this.dropZone.click(function() {
            if(!_self.isDisabled) {
                input.trigger("click");
            }
        });
        var size_limit = input.attr("size-limit");
        if(size_limit) {
            this.size_limit = +size_limit;
        }
        if(input.is("[validation]") && !input.is("[error-on]")) {
            var uuid = bm.getUUID();
            this.wrapper.attr("id", uuid);
            input.attr("error-on", uuid)
        }
        this.initializeDrop();
    }

    FileDropHandler.prototype.preAdd = function() {};

    FileDropHandler.prototype.onAdd = function() {};

    FileDropHandler.prototype.onVerifyError = function() {};

    FileDropHandler.prototype.resetFile = function() {};

    FileDropHandler.prototype.initializeDrop = function() {
        var processed_timestamp;
        var _self = this;
        this.dropZone.fileupload({
            dropZone: this.dropZone,
            fileInput: this.input,
            pasteZone: null,
            replaceFileInput: false,
            dataType: "json",
            autoUpload: false,
            add: function(e, data) {
                if(e.delegatedEvent.timeStamp == processed_timestamp) {
                    return;
                }
                _self.preAdd(data);
                var files = data.originalFiles;
                var least_added = false;
                $.each(files, function() {
                    var added = _self.verify(this);
                    if(added) {
                        least_added = true;
                        _self.onAdd(e, this);
                        _self.input.trigger("file-add", [this]);
                        if(_self.auto) {
                            _self.isDisabled = true;
                            $(_self.dropZone).fileupload("send", {files: [this],
                                formData: { id: _self.submit_data},
                                url: _self.submit_url,
                                success : function(e) {
                                    _self.isDisabled = false;
                                    if(e.resetUrl) {
                                        _self.resetUrl = e.resetUrl;
                                    }
                                    _self.input.trigger("file-submit", [e]);
                                },
                                error : function() {
                                    _self.isDisabled = false;
                                    _self.reset = false;
                                    _self.resetFile();
                                }
                            });
                            _self.remove = _self.input.attr("remove-support") == "true";
                        }
                    }
                });
                if(e.delegatedEvent.type == "drop" && least_added) {
                    _self.input.trigger("change", data);
                }
                processed_timestamp = e.delegatedEvent.timeStamp;
            },
            dragenter: function() {
                _self.dropZone.addClass('drag-over');
            },
            dragleave: function() {
                _self.dropZone.removeClass('drag-over');
            },
            drop: function() {
                _self.dropZone.removeClass('drag-over');
            }
        })
    };

    FileDropHandler.prototype.verify = function(file) {
        var errorMessage = null;
        if(window.File) {
            var fileType;
            if(this.size_limit) {
                if(file.size > this.size_limit) {
                    errorMessage = $.i18n.prop("size.limit.exceeded", [this.size_limit.toByteNotation()])
                }
            }
            if(file.type) {
                fileType = file.type
            } else {
                fileType = file.name.substring(file.name.lastIndexOf(".") + 1, file.name.length);
            }
            if(!errorMessage && this.fileType) {
                if(!new RegExp(this.fileType).test(fileType)) {
                    errorMessage = $.i18n.prop("file.type.unsupported");
                }
            }
            if(errorMessage) {
                if(this.onVerifyError(errorMessage, file) === false) {
                    return false;
                }
            }
        }
        if(errorMessage) {
            return false;
        }
        return true;
    };

    FileDropHandler.prototype.cacheFile = function(file) {
        var fileData = {name: this.input.attr("name"), value: file};
        fileData.value.formData = fileData;
        if(this.form) {
            var cache = this.caches;
            if(!cache) {
                cache = this.caches = [];
            }
            cache.push(fileData);
            var formCacheData = this.form.data(formFieldDataKey);
            if(!formCacheData) {
                this.form.data(formFieldDataKey, formCacheData = []);
            }
            formCacheData.push(fileData);
        }
    };

    FileDropHandler.prototype.updateCache = function(file) {
        if(this.caches) {
            this.removeCache();
        }
        this.cacheFile(file);
    };

    FileDropHandler.prototype.removeCache = function(fileData) {
        if(this.form) {
            var formCacheData = this.form.data(formFieldDataKey);
            if(!formCacheData) {
                return;
            }
            if(fileData) {
                formCacheData.remove(fileData);
                this.caches.remove(fileData);
            } else {
                this.caches.every(function() {
                    formCacheData.remove(this);
                });
                this.caches = [];
            }
        }
    };

    function ImageHandler() {}

    ImageHandler.prototype.preview = function(file, img) {
        if(window.FileReader) {
            var reader = new FileReader();
            reader.onload = function(e) {
                img.attr('src', e.target.result);
            };
            reader.readAsDataURL(file);
        }
    };

    function SingleFileDropHandler(input) {
        var _self = this;
        FileDropHandler.apply(this, arguments);
        this.viewer = this.input.attr("previewer");
        if(this.viewer) {
            this.remove = this.input.attr("remove-support") == "true";
            var reset = this.input.attr("reset-support");
            if(reset) {
                this.reset = reset == "true";
            } else {
                this.reset = this.remove;
            }
            this.viewer = this.input.parent().find("#"+ this.viewer).length ?  this.input.parent().find("#"+ this.viewer) : $("#" + this.viewer);
            this.wrapper.addClass("single-file-with-preview")
        } else {
            this.wrapper.addClass("single-file-without-preview");
            if(this.input.attr("text-helper") !== "no") {
                this.fakeText = $("<input type='text' class='fake-chooser-text' readonly>");
                this.wrapper.append(this.fakeText);
            }
        }
        if(this.viewer) {
            this.resetUrl = this.viewer ? this.viewer.attr("src") : null;
            this.initializeResets();
        }
        if(this.auto) {
            if(input.attr("enable-remove-after-submit") != "false") {
                input.one("file-submit", function(ev, resp) {
                    if(resp.resetUrl) {
                        _self.enableRemove();
                    }
                })
            }
            if(input.attr("reset-after-submit") == "true") {
                input.on("file-submit", function() {
                    _self.input.reset();
                })
            }
        }
    }
    SingleFileDropHandler.inherit(FileDropHandler);

    SingleFileDropHandler.prototype.initializeResets = function() {
        var _self = this;
        this.removeButton = $("<span class='tool-icon remove image-remove'></span>");
        if(!this.remove) {
            this.removeButton.hide();
        }
        this.viewer.after(this.removeButton);
        this.removeCheck = $("<input type='checkbox' class='single' value='yes'>");
        this.removeCheck.attr("name", this.input.attr("remove-option-name"));
        this.wrapper.prepend(this.removeCheck.hide());
        this.removeButton.on("click", function() {
            _self.removeFile();
        });
        if(!this.reset) {
            return;
        }
        var _self = this;
        this.resetUrl = this.viewer ? this.viewer.attr("src") : null;
        this.resetButton = $("<span class='tool-icon reset image-reset'></span>").hide();
        if(this.remove) {
            this.removeButton.after(this.resetButton)
        } else {
            this.viewer.after(this.resetButton)
        }
        this.resetButton.on("click", function() {
            _self.resetFile();
        })
    };

    SingleFileDropHandler.prototype.clearFile = function(inputReset) {
        if(this.wrapper.hasClass("file-added")) {
            if(inputReset !== false) {
                this.input.reset();
            }
            if(this.fakeText) {
                this.fakeText.val("");
            }
            this.removeCache();
            this.wrapper.removeClass("file-added");
        }
    };

    SingleFileDropHandler.prototype.enableReset = function() {
        if(this.reset) {
            this.resetButton.show();
        }
    };

    SingleFileDropHandler.prototype.enableRemove = function() {
        if(this.remove) {
            this.removeButton.show();
        }
    };

    SingleFileDropHandler.prototype.removeFile = function() {
        var _self = this;
        this.viewer.hide();
        this.removeButton.hide();
        this.removeCheck[0].checked = true;
        this.clearFile();
        this.input.trigger("file-remove");
        this.enableReset();
        if(this.auto) {
            this.disabled = true;
             bm.ajax({
                 url: app.baseUrl + this.submit_url,
                 data: { id: this.submit_data,
                     "remove-image" : "true"},
                 success: function () {
                    _self.resetUrl = ""
                 },
                 error : function () {
                    _self.resetFile();
                 },
                complete : function () {
                    _self.disabled = false
                 }
             })
        }
    };

    SingleFileDropHandler.prototype.preAdd = function() {
        FileDropHandler.prototype.preAdd.apply(this, arguments);
        this.clearFile(false);
    };

    SingleFileDropHandler.prototype.onAdd = function(e, file) {
        FileDropHandler.prototype.onAdd.apply(this, arguments);
        this.wrapper.addClass("file-added");
        if(this.fakeText) {
            this.fakeText.val(file.name);
        }
        if(e.delegatedEvent.type == "drop") {
            this.updateCache(file);
            this.input.reset();
        }
        this.enableRemove()
    };

    SingleFileDropHandler.prototype.onVerifyError = function(message) {
        FileDropHandler.prototype.onVerifyError.apply(this, arguments);
        bm.notify(message, "alert");
        this.input.reset();
        this.input.trigger("file-reset");
        return false;
    };

    SingleFileDropHandler.prototype.resetFile = function() {
        this.removeCheck[0].checked = false;
        this.enableRemove();
        this.clearFile();
        this.input.trigger("file-reset");
        if(this.reset) {
            this.resetButton.hide();
        }
    };

    function SingleVideoDropHandler(input) {
        this.fileType = "video";
        this.dropText = input.attr("drop-text") || $.i18n.prop("drop.video.or.click");
        this.chooserText = input.attr("chooser-text") || $.i18n.prop("choose.video");
        SingleFileDropHandler.apply(this, arguments)
    }
    SingleVideoDropHandler.inherit(SingleFileDropHandler);

    function SingleImageDropHandler(input) {
        this.fileType = "image";
        this.dropText = input.attr("drop-text") || $.i18n.prop("drop.image.or.click");
        this.chooserText = input.attr("chooser-text") || $.i18n.prop("choose.image");
        SingleFileDropHandler.apply(this, arguments)
    }
    SingleImageDropHandler.inherit(SingleFileDropHandler, ImageHandler);

    SingleImageDropHandler.prototype.onVerifyError = function(message) {
        SingleFileDropHandler.prototype.onVerifyError.apply(this, arguments);
        if(this.reset) {
            this.resetFile()
        }
        return false;
    };

    SingleImageDropHandler.prototype.resetFile = function() {
        if(this.resetUrl) {
            this.viewer.show();
            this.viewer[0].src = this.resetUrl
        } else {
            this.viewer.hide();
        }
        SingleFileDropHandler.prototype.resetFile.apply(this, arguments);
    };

    SingleImageDropHandler.prototype.onAdd = function(e, file) {
        SingleFileDropHandler.prototype.onAdd.apply(this, arguments);
        this.viewer.show();
        this.preview(file, this.viewer);
        this.enableReset()
    };

    function SingleRawDropHandler(input) {
        this.dropText = input.attr("drop-text") || $.i18n.prop("drop.file.or.click");
        this.chooserText = input.attr("chooser-text") || $.i18n.prop("choose.file");
        this.fileType = input.attr("file-type");
        SingleFileDropHandler.apply(this, arguments)
    }
    SingleRawDropHandler.inherit(SingleFileDropHandler);

    SingleRawDropHandler.prototype.onAdd = function(e, file) {
        SingleFileDropHandler.prototype.onAdd.apply(this, arguments);
        if(this.viewer) {
            this.viewer.show();
            var existing = this.viewer.attr("class");
            var rclass;
            existing.split(" ").every(function() {
                if(this.startsWith("type-")) {
                    rclass = this;
                    return false;
                }
            });
            this.viewer.removeClass("" + rclass).addClass("type-" + bm.fileExtension(file.name));
            this.enableReset()
        }
    };

    SingleRawDropHandler.prototype.resetFile = function() {
        if(this.resetUrl) {
            this.viewer.show();
            var existing = this.viewer.attr("class");
            var rclass;
            existing.split(" ").every(function() {
                if(this.startsWith("type-")) {
                    rclass = this;
                    return false;
                }
            });
            this.viewer.removeClass("" + rclass).addClass("type-" + bm.fileExtension(this.resetUrl));
            this.viewer.attr("src", this.resetUrl)
        } else {
            if(this.viewer) {
                this.viewer.hide();
            }
        }
        SingleFileDropHandler.prototype.resetFile.apply(this, arguments);
    };

    function MultipleFileDropHandler() {
        FileDropHandler.apply(this, arguments);
        this.queue = this.form.find("#" + this.input.attr("queue"));
        this.wrapper.addClass("multiple-file")
    }
    MultipleFileDropHandler.inherit(FileDropHandler);

    MultipleFileDropHandler.prototype.onVerifyError = function(message, file) {
        FileDropHandler.prototype.onVerifyError.apply(this, arguments);
        var queueBlock = $("<div class='file-selection-queue error-queue'><span class='file-name'></span><span class='tool-icon remove file-remove'></span></div>");
        queueBlock.find(".file-name").text(message + " : (" + file.name + ")");
        this.initializeQueue(queueBlock, file)
    };

    MultipleFileDropHandler.prototype.preAdd = function() {
        FileDropHandler.prototype.preAdd.apply(this, arguments);
        this.input.reset();
    };

    MultipleFileDropHandler.prototype.onAdd = function(e, file) {
        FileDropHandler.prototype.onAdd.apply(this, arguments);
        var queueBlock = $("<div class='file-selection-queue'><span class='file-name'></span><span class='tool-icon remove file-remove'></span></div>");
        queueBlock.find(".file-name").text(file.name + "  (" + file.size.toByteNotation() + ")");
        this.cacheFile(file);
        this.initializeQueue(queueBlock, file);
        return queueBlock;
    };

    MultipleFileDropHandler.prototype.initializeQueue = function(queue, file) {
        var _self = this;
        queue.data("file", file);
        this.queue.append(queue);
        queue.find(".file-remove").click(function() {
            queue.remove();
            if(file.formData) {
                _self.removeCache(file.formData);
            }
        })
    };

    function MultipleVideoDropHandler(input) {
        this.fileType = "video";
        this.dropText = input.attr("drop-text") || $.i18n.prop("drop.videos.or.click");
        MultipleFileDropHandler.apply(this, arguments)
    }
    MultipleVideoDropHandler.inherit(MultipleFileDropHandler);

    function MultipleImageDropHandler(input) {
        this.fileType = "image";
        this.dropText = input.attr("drop-text") || $.i18n.prop("drop.images.or.click");
        this.chooserText = input.attr("chooser-text") || $.i18n.prop("choose.image");
        MultipleFileDropHandler.apply(this, arguments)
    }
    MultipleImageDropHandler.inherit(MultipleFileDropHandler, ImageHandler);

    MultipleImageDropHandler.prototype.onAdd = function(ev, file) {
        var queue = MultipleFileDropHandler.prototype.onAdd.apply(this, arguments);
        var img = $("<img class='queue-image-preview'>");
        queue.prepend(img);
        this.preview(file, img);
    };

    function MultipleRawDropHandler(input) {
        this.dropText = input.attr("drop-text") || $.i18n.prop("drop.files.or.click");
        this.fileType = input.attr("file-type");
        MultipleFileDropHandler.apply(this, arguments)
    }
    MultipleRawDropHandler.inherit(MultipleFileDropHandler);

    $.prototype.file = function() {
        if(!this.length) {
            return this;
        }
        this.each(function() {
            var input = $(this);
            var type = input.attr("file-type");
            var multiple = input.attr("multiple");
            if(type == "image") {
                if(multiple) {
                    new MultipleImageDropHandler(input)
                } else {
                    new SingleImageDropHandler(input)
                }
            } else if(type == "video") {
                if(multiple) {
                    new MultipleVideoDropHandler(input)
                } else {
                    new SingleVideoDropHandler(input)
                }
            } else {
                if(multiple) {
                    new MultipleRawDropHandler(input)
                } else {
                    new SingleRawDropHandler(input)
                }
            }
        })
    };

    ////////////////// Adding chart in jquery prototype /////////////////

    var chart_line_colors = ["151,205,187", "151,187,205", "205,151,187"];

    $.prototype.chart = function(func, param) {
        if(!this.length) {
            return;
        }
        this.each(function() {
            var tag = $(this);
            if(tag.is(".no-record-chart")) {
                return;
            }
            if(func) {
                var obj = tag.data("wcui-chart");
                tag.html("<canvas></canvas>");
                var canvas = tag.children()[0].getContext("2d");
                var chartType = param.capitalize();
                var chart = new Chart(canvas)[chartType](obj.data, obj.options);
                if(chartType == "Pie" || chartType == "Doughnut") {
                    var legend = chart.generateLegend();
                    tag.append(legend)
                }
            } else {
                var configuration = tag.children();
                tag.html("<canvas></canvas>");
                var canvas = tag.children()[0].getContext("2d");
                var data = $.extend([], {
                    labels: [],
                    datasets: []
                });
                var labels = configuration.filter("label").each(function () {
                    data.labels.push($(this).text())
                });
                var labelCount = labels.length;
                var datasets = configuration.filter("div.dataset");
                var setCount = datasets.length;
                var datas = configuration.filter("div.data");
                datasets.each(function (i) {
                    var color = chart_line_colors[i];
                    var setData = [];
                    data.datasets.push({
                        label: $(this).text(),
                        fillColor: "rgba(" + color + ",0.2)",
                        strokeColor: "rgba(" + color + ",1)",
                        pointColor: "rgba(" + color + ",1)",
                        pointStrokeColor: "#fff",
                        pointHighlightFill: "#fff",
                        pointHighlightStroke: "rgba(" + color + ",1)",
                        data: setData
                    });
                    for (var c = 0; c < labelCount; c++) {
                        setData[c] = +datas.eq(c * setCount + i).text()
                    }
                    if(setCount == 1) { // pie and doughnut chart can be rendered
                        for (var c = 0; c < labelCount; c++) {
                            var uniColorR = Math.floor(Math.random()*210 + 20);
                            var uniColorG = Math.floor(Math.random()*210 + 20);
                            var uniColorB = Math.floor(Math.random()*210 + 20);
                            var uniColorRH = uniColorR + 10;
                            var uniColorGH = uniColorG + 10;
                            var uniColorBH = uniColorB + 10;
                            data.push({
                                value: setData[c],
                                color: "rgba(" + uniColorR + "," + uniColorG + "," + uniColorB + ",1)",
                                highlight: "rgba(" + uniColorRH + "," + uniColorGH + "," + uniColorBH + ",1)",
                                label: data.labels[c]
                            })
                        }
                    }
                });
                var options = {
                    responsive: true,
                    multiTooltipTemplate: "<%if (datasetLabel){%><%=datasetLabel%>: <%}%> <%= value %>",
                    maintainAspectRatio: false
                };
                var chartType = tag.attr("chart-type").capitalize();
                var chart = new Chart(canvas)[chartType](data, options);
                tag.data("wcui-chart", {
                    data: data,
                    options: options
                });
                if(chartType == "Pie" || chartType == "Doughnut") {
                    var legend = chart.generateLegend();
                    tag.append(legend)
                }
            }
        })
    };


    /********Scroll to**********/
    $.prototype.scrollTo = function( target, options, callback ) {
        if(!target) {
            return;
        }
        if(typeof options == 'function' && arguments.length == 2){ callback = options; options = target; }
        var settings = $.extend({
            scrollTarget  : target,
            offsetTop     : 50,
            duration      : 500,
            easing        : 'swing'
        }, options);
        var scrollTarget = (typeof settings.scrollTarget == "number") ? settings.scrollTarget : settings.scrollTarget;
        return this.each(function(){
            var scrollPane = $(this);
            var scrollY = (typeof scrollTarget == "number") ? scrollTarget : scrollTarget.offset().top + scrollPane.scrollTop() - parseInt(settings.offsetTop);
            scrollPane.animate({scrollTop : scrollY }, parseInt(settings.duration), settings.easing, function(){
                if (typeof callback == 'function') { callback.call(this); }
            });
        });
    }

    $.prototype.refreshTooltip = function(title) {
        if(this.is(".tooltipstered")) {
            this.tooltipster("destroy").attr("title", $.i18n.prop(title)).tooltipster()
        }
        return this;
    }
})();
