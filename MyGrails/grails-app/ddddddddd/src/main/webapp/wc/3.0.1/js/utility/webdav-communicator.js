/** does all the methods of webdav protocol **/
(function() {
    var Constants = {
        propfind_request_data : "<?xml version=\"1.0\"?><propfind xmlns=\"bm:\"><prop>" +
            "<getcontenttype/><getchildcount/><getfoldercount/><getcontentlength/><resourcetype/><displayname/><getlastmodified/><supportedlock/>" +
            "<lockdiscovery/></prop></propfind>",
        NamespaceUri : "bm:",
        CreationDate : "creationdate",
        DisplayName : "displayname",
        GetContentLength : "getcontentlength",
        GetContentType : "getcontenttype",
        GetETag : "getetag",
        GetLastModified : "getlastmodified",
        GetChildCount : "getchildcount",
        GetFolderCount : "getfoldercount",
        ResourceType : "resourcetype",
        SupportedLock : "supportedlock",
        LockDiscovery : "lockdiscovery",
        GetContentLanguage : "getcontentlanguage",
        Source : "source",
        Src : "src",
        Dst : "dst",
        Link : "link",
        Slash : "/",
        DepndencyFailedCode : 424,
        LockedCode : 423,
        OpaqueLockToken : "opaquelocktoken:"
    };

    window.WebDAVClient = {
        doPropFind : function(path, handlerContext, handler) {
            var callState = { handler: handler, handlerContext: handlerContext};
            bm.ajax({
                type: "PROPFIND",
                url: app.baseUrl + bm.encodePath(path),
                data: Constants.propfind_request_data,
                dataType: "xml",
                beforeSend: function(xhr) {
                    xhr.setRequestHeader("Depth", "1");
                },
                success: function(response) {
                    var xmlData = $(response);
                    var files = [];
                    var folders = [];
                    var parentUrl = path;
                    if(/\/$/.test(parentUrl)) {
                        parentUrl = parentUrl.substring(0, parentUrl.length - 1);
                    }
                    xmlData.find("D\\:response, response").each(function(index, node){
                        if(index != 0) {
                            var entity = $(node).find("D\\:resourcetype, resourcetype");
                            var name = $(node).find("D\\:displayname, displayname");
                            if(entity.children().length == 0) {
                                var size = $(node).find("D\\:getcontentlength, getcontentlength");
                                var extension = name.text().substring(name.text().lastIndexOf(".") + 1)
                                files.push({name: name.text(), size: parseInt(size.text(), 10), path: parentUrl + "/" + name.text(), parent: parentUrl, type: "file", clazz: extension});
                            } else {
                                var childCount =  $(node).find("D\\:getchildcount, getchildcount");
                                var childFolderCount =  $(node).find("D\\:getfoldercount, getfoldercount");
                                folders.push({name: name.text(), path: parentUrl + "/" + name.text(), parent: parentUrl, type: "folder", hasChildFolder: parseInt(childFolderCount.text()) > 0, hasChild: parseInt(childCount.text()) > 0});
                            }
                        }
                    });
                    callState.handler.onPopulate.call(callState.handlerContext, {files: files, folders: folders});
                },
                error: function(status, message) {
                    callState.handler.onError.call(callState.handlerContext, message);
                }
            });
        },

        doDeletePath : function(path, handler, handlerContext){
            var callState = { handler: handler, handlerContext: handlerContext};
            bm.ajax({
                type: "DELETE",
                url: app.baseUrl + bm.encodePath(path),
                success: function() {
                    callState.handler.success.call(callState.handlerContext);
                },
                error: function(status, message) {
                    callState.handler.error.call(callState.handlerContext, message);
                }
            });
        },
        doCreateDirectory : function(path, handlerContext, handler) {
            var callState = { handler: handler, handlerContext: handlerContext};
            bm.ajax({
                type: "MKCOL",
                url: app.baseUrl + bm.encodePath(path),
                success: function() {
                    callState.handler.success.call(callState.handlerContext);
                },
                error: function(status, message) {
                    callState.handler.error.call(callState.handlerContext, status, message);
                }
            });
        },
        doMove : function(sourcePath, targetPath, handler, handlerContext){
            var callState = { handler: handler, handlerContext: handlerContext};
            bm.ajax({
                type: "MOVE",
                url: app.baseUrl + bm.encodePath(sourcePath),
                beforeSend: function(xhr) {
                    xhr.setRequestHeader("Destination", targetPath);
                },
                success: function() {
                    callState.handler.success.call(callState.handlerContext);
                },
                error: function(status, message) {
                    callState.handler.error.call(callState.handlerContext, message);
                }
            });
        }
    }
})();