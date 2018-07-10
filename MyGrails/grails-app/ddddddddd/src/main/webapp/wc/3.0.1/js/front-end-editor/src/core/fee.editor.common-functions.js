app.FrontEndEditor.prototype.commonFunctions = (function () {
    var _self = this;

    this.getFileName = function (filePath, isExtension) {
        return filePath.filename(isExtension);
    };

    this.isInLayout = function (element) {
        var _header = _self.pageBody.find(".header")[0];
        var _footer = _self.pageBody.find(".footer")[0];
        if(_header && _footer && element) {
            if ($.contains(_header, element) || $.contains(_footer, element)) {
                return true;
            }
            return false;
        }
    };

    this.isAnyPopupOpen = function () {
        if(_self.pageBody.find(".fee-popup, .confirm-popup").length > 0){
            return true;
        }
        return false;
    };

    String.prototype.filename = function(extension){
        var s= this.replace(/\\/g, '/');
        s= s.substring(s.lastIndexOf('/')+ 1);
        return extension? s.replace(/[?#].+$/, ''): s.split('.')[0];
    };

    return this;
});