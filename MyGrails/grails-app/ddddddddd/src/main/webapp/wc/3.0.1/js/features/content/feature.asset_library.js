app.tabs.assetLibrary = function () {
    this.constructor_args = arguments
    this.text = $.i18n.prop("asset.library")
    this.tip = $.i18n.prop("manage.asset.library")
    this.ui_class = "asset-library"
    this.ui_body_class = "simple-tab"
    app.tabs.assetLibrary._super.constructor.apply(this, arguments)
}

app.tabs.assetLibrary.inherit(app.ExplorerPanelTab)

var _a = app.tabs.assetLibrary.prototype
_a.templatePath = app.fullURL + "template"
_a.ajax_url = app.baseUrl + "assetLibrary/loadExplorerView"
_a.explorer_url = app.baseUrl + "assetLibrary/explorerPanel"
_a.parent_key = "path"

app.ribbons.web_content.push(app.tabs.assetLibrary.ribbon_data = {
    text: $.i18n.prop("asset.library"),
    processor: app.tabs.assetLibrary,
    ui_class: "asset-library"
});

(function () {
    function attachEvents() {
        var _self = this
        _self.body.find("select.select-repository").change(function () {
            var repository = this.value
            _self.root_node_name = $.i18n.prop(repository == "pub" ? "public" : "template")
            _self.reload()
        })
        _self.body.find(".header .directory-info").on("click", function () {
            var $this = $(this)
            if (!$this.find(".address-edit").length) {
                _self.makeDirInfoEditable($this)
            }
        })
        _self.body.find(".right-panel .body").bind('dragover', function (e) {
            var tag = e.target
            if ($(tag.parentNode).hasClass("folder")) {
                $(tag.parentNode).addClass("dragging-over")
            } else {
                $(this).addClass("dragging-over")
            }
            return false
        })

        _self.body.find(".right-panel .body").bind('dragleave', function (e) {
            var tag = e.target
            if ($(tag.parentNode).hasClass("folder")) {
                $(tag.parentNode).removeClass("dragging-over")
            } else {
                $(this).removeClass("dragging-over")
            }
        })

        _self.body.find(".status-container .tool-icon.cancel-all").click(function () {
            _self.body.find("tr.status-row").each(function () {
                var tr = $(this)
                if (tr.find(".diplomatic").length) {
                    _self.cancelIndex.push(parseInt(tr.find("input[name=index]").val()))
                    tr.find(".status").attr("class", "status negative")
                }
            })
        })

        _self.body.find(".status-container .tool-icon.clear-list").click(function () {
            _self.body.find("tr.status-row").each(function () {
                var tr = $(this)
                if (tr.find(".positive").length) {
                    tr.remove()
                }
            })
        })

        _self.body.find(".right-panel .body").bind('drop', function (e, ui) {
            e.preventDefault()
            if (e.originalEvent.dataTransfer) {
                var tag = e.target
                var path = _self.currentPath
                if ($(tag.parentNode).hasClass("folder")) {
                    path = _self.currentPath + "/" + $(tag.parentNode).attr("content-name")
                }
                var files = e.originalEvent.dataTransfer.files
                _self.uploadFiles(files, path)
                return false
            }

        })
    }

    _a.init = function () {
        var _self = this
        app.tabs.assetLibrary._super.init.call(this)
        attachEvents.call(this)
        this.on_global("active-template-change", function (evnt, name) {
            if (_self.root_node_name == 'template') {
                _self.reload()
            }
        })
        _self.body.find(".upload").on("click", function () {
            _self.showUploadPopup()
        })
        this.initializeFileTransfer()
    }
})()

_a.showUploadPopup = function (node) {
    var _self = this
    var content = "<form class='edit-popup-form asset-upload-popup' enctype='multipart/form-data'>" +
        "<div class='form-row mandatory'>" +
        "<label>" + $.i18n.prop("select.files") + "</label>" +
        "<input type='file' name='files' multiple='true' validation='drop-file-required' queue='asset-lib-file-upload-queue'>" +
        "<div id='asset-lib-file-upload-queue'></div>" +
        "</div>" +
        "</div>" +
        "<div class='form-row button-line'>" +
        "<button type='submit' class='submit-button'>" + $.i18n.prop("upload") + "</button>" +
        "<button type='button' class='cancel-button'>" + $.i18n.prop("cancel") + "</button>" +
        "</form>"
    _self.renderCreatePanel(undefined, $.i18n.prop("upload.file"), "", {}, {
        content: content,
        toolbar_btn_text: $.i18n.prop("upload"),
        beforeSubmit: function (_form, ajaxSettings, popup) {
            var files = _form.data("form-extra-data").collect("value")
            popup.close()
            _self.uploadFiles(files)
            return false
        }
    })
}

_a.uploadFiles = function (files, path) {
    var _self = this, file_length = files.length, index = 0, applyForAllFiles = false,
        decision = true, container = _self.body.find(".right-panel")
    path = path ? path : _self.currentPath
    _self.populateStatus(files, path)
    var cancelIds = _self.cancelIndex

    function uploadNextFile() {
        if (index < file_length) {
            var file = files[index++]
            if (cancelIds.length && cancelIds.contains(index - 1)) {
                uploadNextFile()
                return
            }
            if (!applyForAllFiles) {
                var message = $("<div class='message'>" + $.i18n.prop("the.file.already.exists.want.overwrite", [file.name]) + "</div>")
                
                bm.ajax({
                    url: app.baseUrl + "remoteRepository/isFileExists",
                    data: {fileName: file.name, filePath: path},
                    success: function (resp) {
                        if (resp.isExists) {
                            bm.confirm(message, [{
                                clazz: "yes-for-all",
                                text: "yes.for.all",
                                handler: function () {
                                    applyForAllFiles = true
                                    decision = true
                                    proceedNext()
                                }
                            },
                                {
                                    clazz: "yes",
                                    text: "yes",
                                    handler: function () {
                                        decision = true
                                        proceedNext()
                                    }
                                },
                                {
                                    clazz: "no-for-all",
                                    text: "no.for.all",
                                    handler: function () {
                                        applyForAllFiles = true
                                        decision = false
                                        proceedNext()
                                    }
                                },
                                {
                                    clazz: "no",
                                    text: "no",
                                    handler: function () {
                                        decision = false
                                        proceedNext()
                                    }
                                }])
                        } else {
                            proceedNext()
                        }

                    }
                })
            } else {
                proceedNext()
            }

            function proceedNext() {
                if (file.size > 1024 * 1024 * 200) {
                    _self.progressUpload(index - 1, null, false, $.i18n.prop("size.limit.exceeded", ["200MB"]))
                    uploadNextFile()
                } else if (decision) {
                    var reader = new FileReader()
                    reader.onload = function (element) {
                        uploadFile(element, file)
                    }
                    reader.readAsDataURL(file)
                } else {
                    _self.progressUpload(index - 1, null, false)
                    uploadNextFile()
                }
            }
        } else {
            container.loader(false)
            setTimeout(function () {
                _self.reload()
            }, 500)
        }

    }

    uploadNextFile()

    function uploadFile(element, file) {
        var result = element.target.result
        var indx = result.indexOf('base64') + 7
        var data = result.slice(indx, result.length)
        var filePath = path + "/" + file.name + "?fileSize=" + file.size

        bm.ajax({
            url: app.baseUrl + filePath,
            type: 'POST',
            method: "put",
            xhr: function () {
                var myXhr = $.ajaxSettings.xhr()
                if (myXhr.upload) {
                    myXhr.upload.addEventListener('progress', function (e) {
                        if (e.lengthComputable) {
                            var percentComplete = Math.round((e.loaded / e.total) * 100)
                            if (_self.cancelIndex.length && _self.cancelIndex.contains(index - 1)) {
                                myXhr.abort()
                            } else {
                                _self.progressUpload(index - 1, percentComplete)
                            }
                        }
                    }, false)
                }
                myXhr.onabort = function (ev) {
                    uploadNextFile()
                }
                return myXhr
            },
            beforeSend: function (xhr) {
                _self.beforeUpload(index - 1)
                xhr.setRequestHeader("base64", true)
            },
            success: function () {
                _self.progressUpload(index - 1, null, true)
                uploadNextFile()
            },
            error: function () {

            },
            data: data,
            //Options to tell jQuery not to process data or worry about content-type.
            cache: false,
            timeout: 7200000,
            contentType: false,
            processData: false
        })
    }
}

_a.beforeUpload = function (index) {
    this.pbar = new ProgressBar(this.statusContainer.find(".file" + index + " .td-progress-bar"), true)
    this.pbar.render()
}

_a.populateStatus = function (files, path) {
    var _self = this
    _self.cancelIndex = []
    if (!_self.statusContainer) {
        _self.statusContainer = _self.body.find(".status-container")
    }
    _self.statusContainer.find(".status-row").remove()
    $.each(files, function (i, file) {
        var fileSize = file.size
        var suffix = "MB"
        if (fileSize < 1024) {
            suffix = "Byte"
        } else if (fileSize < (1024 * 1024)) {
            fileSize = fileSize / 1024
            suffix = "KB"
        } else {
            fileSize = fileSize / (1024 * 1024)
        }
        var statusRow = $("<tr class='status-row " + (i % 2 == 0 ? "even" : "odd") + " file" + i + "'><td><input type='hidden' name='index' value='" + i + "'>" + file.name + "</td>" +
            "<td>" + path + "</td><td class='td-progress-bar'></td><td class='status-column'><span class='status diplomatic' title=" + $.i18n.prop('pending') + "></span>" +
            "</td><td class='size'>" + fileSize.toFixed(2) + " " + suffix + "</td><td class='actions-column'><span class='tool-icon remove' title=" + $.i18n.prop('stop') + "></span></td></tr>")
        _self.statusContainer.append(statusRow)
        statusRow.updateUi()
    })
    _self.statusContainer.find(".status-row .tool-icon.remove").click(function () {
        var $this = $(this)
        var tr = $this.parents("tr.status-row")
        var status = tr.find(".status-column .status")
        if (status.is(".diplomatic")) {
            _self.cancelIndex.push(parseInt(tr.find("input[name=index]").val()))
            tr.find(".status").attr("class", "status negative").tooltipster('content', $.i18n.prop("status.failed"))
            $this.tooltipster('content', $.i18n.prop("remove"))
        } else if (status.is(".negative")) {
            tr.remove()
        }
    })
}

_a.cancelIndex = []

_a.progressUpload = function (index, percent, done, statusMgs) {
    var _self = this
    var tr = _self.statusContainer.find(".file" + index)
    var status = tr.find(".status")
    if (done) {
        this.pbar.complete()
        status.attr("class", "status positive").tooltipster('content', statusMgs ? statusMgs : $.i18n.prop('status.successful'))
    } else if (done == false) {
        status.attr("class", "status negative").tooltipster('content', statusMgs ? statusMgs : $.i18n.prop('status.failed'))
    } else {
        this.pbar.advance(percent)
    }
}

_a.menu_entries = {
    file: [
        {
            text: $.i18n.prop("rename"),
            ui_class: "rename",
            action: "rename"
        },
        {
            text: $.i18n.prop("remove"),
            ui_class: "delete",
            action: "delete"
        },
        {
            text: $.i18n.prop("open.in.css.editor"),
            ui_class: "edit-css",
            action: "edit-css"
        },
        {
            text: $.i18n.prop("open.in.js.editor"),
            ui_class: "edit-js open-in-editor",
            action: "edit-js"
        },
        {
            text: $.i18n.prop("open.in.xml.editor"),
            ui_class: "edit-xml open-in-editor",
            action: "edit-xml"
        },
        {
            text: $.i18n.prop("revisions"),
            ui_class: "revision",
            action: "revision"
        },
        {
            text: $.i18n.prop("move"),
            ui_class: "move",
            action: "move"
        },
        {
            text: $.i18n.prop("download"),
            ui_class: "download",
            action: "download"
        },
        {
            text: $.i18n.prop("extract"),
            ui_class: "extract",
            action: "extract"
        },
        {
            text: $.i18n.prop("get.link"),
            ui_class: "asset-library-get-link",
            action: "asset-library-get-link"
        }
    ],
    folder: [
        {
            text: $.i18n.prop("new.directory"),
            ui_class: "new-directory",
            action: "new-directory"
        },
        {
            text: $.i18n.prop("rename"),
            ui_class: "rename",
            action: "rename"
        },
        {
            text: $.i18n.prop("move"),
            ui_class: "move",
            action: "move"
        },

        {
            text: $.i18n.prop("download"),
            ui_class: "download",
            action: "download"
        },
        {
            text: $.i18n.prop("remove"),
            ui_class: "delete",
            action: "delete"
        }
    ],
    "grand-root": [
        {
            text: $.i18n.prop("new.directory"),
            ui_class: "new-directory",
            action: "new-directory"
        },
        {
            text: $.i18n.prop("refresh"),
            ui_class: "refresh",
            action: "refresh"
        }
    ]
}

_a.convertToActualPath = function (path) {
    let firstSepIndex = path.indexOf("/")
    let repo = path.substring(0, firstSepIndex + 1)
    return `${repo}${app.tenant}${path.substring(firstSepIndex)}`
}

_a.onMenuOpen = function (type, data) {
    var _self = this
    var menu = this.explorer.menu[type]
    var item = []
    if (type == "folder") {
        item = [
            {
                key: "asset_library.create.directory",
                class: "new-directory"
            },
            {
                key: "asset_library.rename.file.directory",
                class: "rename"
            },
            {
                key: "asset_library.remove.directory",
                class: "delete"
            }
        ]

        menu.hide("download")
        menu.hide("asset-library-get-link")
    } else if (type == "file") {
        item = [
            {
                key: "asset_library.rename.file.directory",
                class: "rename"
            },
            {
                key: "asset_library.remove.file",
                class: "delete"
            }
        ]

        if (data.clazz == "css") {
            menu.show("edit-css")
        } else {
            menu.hide("edit-css")
        }
        if (data.clazz == "js") {
            menu.show("edit-js")
        } else {
            menu.hide("edit-js")
        }
        if (data.clazz == "xml") {
            menu.show("edit-xml")
        } else {
            menu.hide("edit-xml")
        }
        if (data.clazz == "zip") {
            menu.show("extract")
        } else {
            menu.hide("extract")
        }
        menu.show("download")
        menu.show("asset-library-get-link")
    }
    app.checkPermission(menu, item)
}

_a.onActionClick = function (type, action, nodeData, node) {
    var _self = this
    switch (action) {
        case "new-directory":
            _self.createNewDirectory(nodeData)
            break
        case "delete":
            var message = $.i18n.prop("confirm.remove", [type, nodeData.name])
            bm.confirm(message, function () {
                _self.deleteDirectory(nodeData)
            }, function () {
            })
            break
        case "rename":
            _self.renameFile(nodeData)
            break
        case "edit-css":
            _self.openCssEditor(nodeData)
            break
        case "edit-js":
            _self.openJsEditor(nodeData)
            break
        case "edit-xml":
            _self.openXmlEditor(nodeData)
            break
        case "refresh":
            _self.reloadRemoteTree()
            break
        case "download":
            _self.download(node)
            break
        case "revision":
            _self.showRevision(nodeData)
            break
        case "asset-library-get-link":
            var copyStatus = bm.copy(`${app.siteBaseUrl}${_self.convertToActualPath(nodeData.path)}`)
            copyStatus ? bm.notify($.i18n.prop("get.link.success"), "success") : bm.notify($.i18n.prop("get.link.copy.error"), "error")
            break
        case "move":
            _self.move(node, nodeData)
            break
        case "extract":
            _self.extract(nodeData)

    }
}

app.tabs.assetLibrary.hasChild = function (fileName) {
    var jsonChildren = document.getElementsByName("localfileaccess")[0].getFileList(fileName)
    var children = eval("(" + jsonChildren + ")")
    if (children.folders.length || children.files.length) {
        return true
    }
    return false
}

app.tabs.assetLibrary.convertFiles = function (children, parent) {
    var newChildern = []
    children.folders.every(function () {
        var child = {}
        child.path = this.path ? this.path : this.name
        child.name = this.name
        child.parent = parent
        child.hasChild = app.tabs.assetLibrary.hasChild(child.path)
        child.type = "folder"
        child.isLocal = true
        if (this.rootFolder) {
            child.rootFolder = true
        }
        newChildern.push(child)
    })
    children.files.every(function () {
        var child = {}
        child.path = this.path
        child.name = this.name
        child.parent = parent
        child.hasChild = false
        child.type = "file"
        child.size = this.size
        child.clazz = this.name.substring(this.name.lastIndexOf(".") + 1)
        child.isLocal = true
        newChildern.push(child)
    })
    return newChildern
}

_a.nodeFinder = function (node, path, expectedPath, index, separator) {
    var _self = this
    var foundNode = _self.childNodeFinder(node, expectedPath)
    index++
    if (index == 1) {
        expectedPath = expectedPath.split("\\")[0]
    }
    if (foundNode) {
        foundNode.activate(false)
        if (index < path.length) {
            if (foundNode.data.hasChild && !foundNode.childList) {
                $(foundNode).bind("afterPopulate.click", foundNode, function () {
                    $(foundNode).unbind("afterPopulate.click", arguments.callee)
                    _self.nodeFinder(foundNode, path, expectedPath + separator + path[index], index, separator)
                })
                foundNode.data.isLazy = true
                foundNode.reloadChildren()
            } else {
                _self.nodeFinder(foundNode, path, expectedPath + separator + path[index], index, separator)
            }
        }
    }
}

_a.childNodeFinder = function (node, expectedPath) {
    var foundNode = null
    $.each(node.childList, function () {
        if (expectedPath == this.data.path) {
            foundNode = this
            return false
        }
    })
    return foundNode
}

_a.moveNodesInSameTree = function (node, targetPath, newName) {
    var _self = this
    var newPath = targetPath + "/" + newName
    WebDAVClient.doPropFind(newPath, this, {
        onPopulate: function () {
            bm.notify($.i18n.prop("already.exists", [newPath]), "error")
        },
        onError: function () {
            WebDAVClient.doMove(node.path, newPath, {
                success: function () {
                    _self.reloadRemoteTree(newPath)
                }, error: function () {

                }
            }, node.parent)

        }
    })
}

_a.openCssEditor = function (node) {
    var nodeHash = bm.hash(node.path + "#" + node.name)
    var tab = app.Tab.getTab("tab-edit-css-" + nodeHash)
    if (!tab) {
        tab = new app.tabs.cssEditor.advanceEditor({
            file: {
                path: node.path,
                name: node.name
            },
            id: "tab-edit-css-" + nodeHash
        })
        tab.render()
    }
    tab.setActive()
}

_a.openJsEditor = function (node) {
    var nodeHash = bm.hash(node.path + "#" + node.name)
    var tab = app.Tab.getTab("tab-edit-script-" + nodeHash)
    if (!tab) {
        tab = new app.tabs.scriptEditor({
            file: {
                path: node.path,
                name: node.name,
                mode: "javascript"
            },
            id: "tab-edit-script-" + nodeHash
        })
        tab.render()
    }
    tab.setActive()
}

_a.openXmlEditor = function (node) {
    var nodeHash = bm.hash(node.path + "#" + node.name)
    var tab = app.Tab.getTab("tab-edit-xml-" + nodeHash)
    if (!tab) {
        tab = new app.tabs.xmlEditor.advanceEditor({
            file: {
                path: node.path,
                name: node.name
            },
            id: "tab-edit-xml-" + nodeHash
        })
        tab.render()
    }
    tab.setActive()
}

_a.showRevision = function (data) {
    bm.editPopup(app.baseUrl + "remoteRepository/showRevision", $.i18n.prop("revisions"), data.name, {
        path: data.path,
        name: data.name
    }, {
        width: 850,
        events: {
            content_loaded: function (popup) {
                var originalFilePath = this.find("input[name='originalFilePath']").val()
                this.find(".tool-icon.replace").click(function () {
                    var filePath = $(this).data("path")
                    bm.ajax({
                        url: app.baseUrl + "remoteRepository/replaceWithRevision",
                        data: {
                            filePath: filePath,
                            originalFilePath: originalFilePath
                        },
                        success: function () {
                            popup.close()
                        }
                    })
                })
                this.find(".tool-icon.clear-all").click(function () {
                    bm.ajax({
                        url: app.baseUrl + "remoteRepository/clearAllRevisions",
                        data: {
                            filePath: originalFilePath,
                            name: data.name
                        },
                        success: function () {
                            popup.close()
                        }
                    })
                })
            }
        }
    })
}

_a.createNewDirectory = function (node) {
    var _self = this
    var path = node.path
    if (!node.path) {
        path = _self.body.find("select.select-repository").val()
    }
    var content = "<form class='edit-popup-form create-new-directory'><div class='form-row mandatory'><label>" + $.i18n.prop("directory.name") + "</label><input type='text' class='directory-name' maxlength='255' validation='required maxlength[255] url_folder'></div></div>" +
        "<div class='button-line'><button type='submit' class='submit-button'>" + $.i18n.prop("create") + "</button>&nbsp &nbsp<button type='button' class='cancel-button'>" + $.i18n.prop("cancel") + "</button></form>"
    bm.editPopup("", $.i18n.prop("create.new.directory"), "", {}, {
        content: content,
        beforeSubmit: function (form, ajaxSettings, popup) {
            var val = form.find("input.directory-name").val()
            if (val != "") {
                WebDAVClient.doPropFind(path + "/" + val, this, {
                    onPopulate: function () {
                        bm.notify($.i18n.prop("directory.with.same.name.already.exists"), "error")
                    }, onError: function () {
                        var newPath = path + "/" + val
                        WebDAVClient.doCreateDirectory(newPath, this, {
                            success: function (response) {
                                _self.reloadRemoteTree(newPath)
                            }, error: function (status, message) {
                                bm.notify($.i18n.prop("directory.creation.not.allowed"), "error")
                            }
                        })
                    }
                })
            }
            popup.close()
            return false
        }
    })
}

_a.renameFile = function (node) {
    var _self = this
    var content = "<form class='edit-popup-form create-new-directory'>" +
        "<div class='form-row mandatory'>" +
        "<label>" + $.i18n.prop("new.name") + "</label>" +
        "<input type='text' class='directory-name' maxlength='255' validation='required maxlength[255]' value='" + node.name + "'>" +
        "</div>" +
        "</div>" +
        "<div class='button-line'>" +
        "<button type='submit' class='submit-button'>" + $.i18n.prop("rename") + "</button>" +
        "<button type='button' class='cancel-button'>" + $.i18n.prop("cancel") + "</button>" +
        "</form>"
    bm.editPopup("", $.i18n.prop("rename"), "", {}, {
        content: content,
        beforeSubmit: function (_form, ajaxSettings, popup) {
            var val = _form.find("input.directory-name").val()
            if (val != "" && val != node.name) {
                _self.moveNodesInSameTree(node, node.parent, val)
            }
            popup.close()
            return false
        }
    })
}

_a.move = function (node, nodeData) {
    var _self = this
    var repository = _self.body.find("select.select-repository")
    var content = "<form class='edit-popup-form create-new-directory'>" +
        "<div class='form-row mandatory'>" +
        "<label>" + $.i18n.prop("destination") + "</label>" +
        "<input type='text' name='destination' class='destination' validation='required'>" +
        "</div>" +
        "</div>" +
        "<div class='button-line'>" +
        "<button type='submit' class='submit-button'>" + $.i18n.prop("move") + "</button>" +
        "<button type='button' class='cancel-button'>" + $.i18n.prop("cancel") + "</button>" +
        "</form>"
    bm.editPopup("", $.i18n.prop("move"), "", {}, {
        content: content,
        beforeSubmit: function (_form, ajaxSettings, popup) {
            var val = _form.find("input.destination").val().trim()
            var destination = repository.val() + (val.startsWith("/") ? "" : "/") + (val == "/" ? "" : val) + "/" + nodeData.name
            _self.moveFile(nodeData.path, destination, function () {
                popup.close()
                _self.reload()
            }, function () {
            })
            return false
        }
    })
}

_a.extract = function (nodeData) {
    var _self = this, waitPopup = bm.waitPopup()
    bm.ajax({
        url: app.baseUrl + "remoteRepository/extract",
        data: {filePath: nodeData.path},
        success: function () {
            _self.reload()
        },
        response: function () {
            waitPopup.close()
        },
        timeout: 7200000
    })
}

_a.deleteDirectory = function (node) {
    var _self = this
    WebDAVClient.doDeletePath(node.path, {
        success: function () {
            _self.reloadRemoteTree(node.path)
        }, error: function (message) {
            bm.notify($.i18n.prop("directory.creation.not.allowed", [node.type]), "error")
        }
    })
}

_a.download = function (node) {
    bm.download(app.baseUrl + "remoteRepository/download?path=" + node.parent(".grid-item").config("content", "path"))
}

_a.onLazyRead = function (node, callbacks) {
    var path
    var _self = this
    callbacks = callbacks || {}
    if ($.isFunction(node)) {
        path = this.body.find("select.select-repository").val()
    } else {
        path = node.data.path
    }
    WebDAVClient.doPropFind(path, this, {
        onPopulate: function (childList) {
            var children = childList.folders
            children.every(function () {
                this.hasChild = this.hasChildFolder
            })
            if ($.isFunction(node)) {
                node.call(_self, children)
            } else {
                node.addChild(children)
                $(node).trigger("afterPopulate")
            }
            if (callbacks.done) {
                callbacks.done()
            }
        }, onError: function () {
            if ($.isFunction(node)) {
                node.call(_self, [])
            }
            if (callbacks.fail) {
                callbacks.fail()
            }
        }
    })
}

_a.root_node_name = $.i18n.prop("public")
_a.beforeBuildTree = function (options) {
    options.tree_root_data.name = this.root_node_name
}
_a.tree_node_type = "folder"

_a.tree_options = {
    onLazyRead: _a.onLazyRead,
    auto_load_lazy_nodes: false,
    grand_root_context: true
}

_a.moveFile = function (from, destination, sucess, error) {
    bm.ajax({
        type: "MOVE",
        url: app.baseUrl + from,
        beforeSend: function (xhr) {
            xhr.setRequestHeader("Destination", destination)
        },
        success: sucess,
        error: error
    })
}

_a.initializeFileTransfer = function () {
    var _self = this
    var treeBody = _self.body.find(".left-panel .body")
    var rightPanel = _self.body.find(".right-panel .body")
    if (!this.currentPath) {
        this.currentPath = _self.body.find("select.select-repository").val()
    }

    function selectFile(file) {
        rightPanel.find(".file.selected, .folder.selected").removeClass("selected")
        file.addClass("selected")
    }

    rightPanel.find(".folder").dblclick(function () {
        var node = treeBody.tree("inst").getActiveNode()
        var expectedPath = _self.currentPath + "/" + $(this).data("name")

        function nodeFinder() {
            var foundNode = null
            $.each(node.childList, function () {
                if (expectedPath == this.data.path) {
                    foundNode = this
                    return false
                }
            })
            if (foundNode) {
                foundNode.activate(false)
                return
            }
        }

        if (node.data.isLazy && !node.childList) {
            $(node).bind("afterPopulate.click", function () {
                $(node).unbind("afterPopulate.click", arguments.callee)
                nodeFinder()
            })
            node.reloadChildren()
        } else {
            nodeFinder()
        }
    })

    rightPanel.find(".file, .folder").draggable({
        containment: _self.body,
        helper: "clone",
        drop: {
            element: rightPanel.find(".folder"),
            intersect: 25
        },
        append_on_drop: false,
        start: function () {
            selectFile($(this))
        }
    }).click(function () {
        selectFile($(this))
    })

    rightPanel.find(".file, .folder").on("dragdrop", function (event, evData) {
        var drag = evData.drag
        var drop = evData.drop
        if (drop.is(".folder")) {
            var from = drag.attr("content-path")
            var destination = _self.currentPath + "/" + drop.attr("content-name") + "/" + drag.attr("content-name")
            _self.gridPanel.addClass("updating")
            _self.moveFile(from, destination, function () {
                _self.reload(true)
            }, function () {
                _self.gridPanel.removeClass("updating")
            })
        }
    })
}

_a.beforeReloadRequest = function (config) {
    app.tabs.assetLibrary._super.beforeReloadRequest.call(this, config)
    var repository = this.body.find("select.select-repository").val()
    $.extend(config, {repository: repository})
}

_a.afterTableReload = function () {
    var _self = this
    var node = _self.body.find(".left-panel .body").tree("inst").getActiveNode()
    _self.currentPath = _self.body.find("select.select-repository").val()
    if (node && node.data.path) {
        _self.currentPath = node.data.path
    }
    _self.attachDirectoryInfo()
    _self.initializeFileTransfer()
}

_a.attachDirectoryInfo = function () {
    var _self = this
    var currentPath = _self.currentPath
    var pathList = currentPath.split("/")
    var infoBody = _self.body.find(".header .directory-info")
    infoBody.html("<span class='folder-icon'></span>")
    var length = pathList.length
    $.each(pathList, function (i, value) {
        if (i == 0) {
            infoBody.append("<span class='dir-name'>" + _self.root_node_name + "</span>")
        } else {
            infoBody.append("<span class='dir-name'>" + value + "</span>")
        }
        if (i != (length - 1)) {
            infoBody.append("<span class='right-arrow'></span>")
        }
    })
}

_a.makeDirInfoEditable = function (dirInfo) {
    var _self = this
    var cPath = _self.currentPath
    dirInfo.children(":not(.folder-icon)").remove()
    var editField = $("<input type='text' class='address-edit' value=''>")
    dirInfo.append(editField)
    cPath = (cPath == "pub") ? "" : cPath.substring(cPath.indexOf("/") + 1, cPath.length)
    editField.focus().val(cPath)

    function updateAddress() {
        var cacheAddress = _self.currentPath
        _self.remoteNodeFinder(editField.val())
        if (!dirInfo.find(".dir-name").length) {
            _self.attachDirectoryInfo()
        }
    }

    editField.on("focusout", function () {
        updateAddress()
    }).on("keypress", function (e) {
        if (e.keyCode == 13) {
            e.preventDefault()
            updateAddress()
        }
    })
}

_a.remoteNodeFinder = function (path) {
    var _self = this
    var tree = _self.body.find(".left-panel .body").tree("inst")
    var node = tree.getRoot()
    path = "pub/" + path
    var s = path.charAt(path.length - 1)
    if (s == "/" || s == "\\") {
        path = path.substring(0, path.length - 1)
    }
    if (tree.activeNode && tree.activeNode.data.path == path) {
        return
    }
    path = path.split("/")
    var i = 1
    if (path[0] == "") {
        node.activate(false)
    }
    var expectedPath = path[0] + "/" + path[1]
    var separator = "/"
    _self.nodeFinder(node, path, expectedPath, i, separator)
}

_a.onMoveTreeNode = function (node, sourceNode, hitMode) {
    if (hitMode == "over") {
        this.moveNodesInSameTree(sourceNode.data, node.data.path, sourceNode.data.name)
        this.reload()
    }
}

_a.reloadRemoteTree = function () {
    this.reload()
}