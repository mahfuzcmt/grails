var browser = {
    init: function () {
        this.id = this.searchString(this.dataBrowser) || "An unknown browser";
        this[this.id] = true;
        this.version = this.searchVersion(navigator.userAgent)
        || this.searchVersion(navigator.appVersion)
        || "an unknown version";
        this.OS = this.searchString(this.dataOS) || "an unknown OS";
        this.lang = navigator.userLanguage || navigator.language || navigator.language || "en";
    },
    searchString: function (data) {
        for (var i = 0; i < data.length; i++) {
            var dataString = data[i].string;
            var dataProp = data[i].prop;
            this.versionSearchString = data[i].versionSearch || data[i].identity;
            if (dataString) {
                if (dataString.indexOf(data[i].subString) != -1)
                    return data[i].identity;
            } else if (dataProp)
                return data[i].identity;
        }
    },
    searchVersion: function (dataString) {
        var index = dataString.indexOf(this.versionSearchString);
        if (index == -1) return;
        return parseFloat(dataString.substring(index + this.versionSearchString.length + 1));
    },
    dataBrowser: [
        {
            string: navigator.userAgent,
            subString: "Edge",
            identity: "ie",
            versionSearch: "Edge"
        },
        {
            string: navigator.userAgent,
            prop: window.opera,
            subString: "OPR",
            identity: "op",
            versionSearch: "OPR"
        },
        {
            string: navigator.userAgent,
            subString: "Chrome",
            versionSearch: "Chrome",
            identity: "gc"
        },
        {
            string: navigator.userAgent,
            subString: "OmniWeb",
            versionSearch: "OmniWeb/",
            identity: "ow"
        },
        {
            string: navigator.vendor,
            subString: "Apple",
            identity: "sf",
            versionSearch: "Version"
        },
        {
            string: navigator.vendor,
            subString: "iCab",
            versionSearch: "iCab",
            identity: "ic"
        },
        {
            string: navigator.vendor,
            subString: "KDE",
            versionSearch: "Konqueror",
            identity: "kq"
        },
        {
            string: navigator.userAgent,
            subString: "Firefox",
            versionSearch: "Firefox",
            identity: "ff"
        },
        {
            string: navigator.vendor,
            subString: "Camino",
            versionSearch: "Camino",
            identity: "cn"
        },
        {		// for newer Netscapes (6+)
            string: navigator.userAgent,
            subString: "Netscape",
            versionSearch: "Netscape",
            identity: "ns"
        },
        {
            string: navigator.userAgent,
            subString: "MSIE",
            identity: "ie",
            versionSearch: "MSIE"
        },
        {
            string: navigator.userAgent,
            subString: ".NET",
            identity: "ie",
            versionSearch: "rv"
        },
        {
            string: navigator.userAgent,
            subString: "Gecko",
            identity: "mz",
            versionSearch: "rv"
        },
        { 		// for older Netscapes (4-)
            string: navigator.userAgent,
            subString: "Mozilla",
            identity: "mz",
            versionSearch: "Mozilla"
        }
    ],
    dataOS: [
        {
            string: navigator.platform,
            subString: "Win",
            identity: "Windows"
        },
        {
            string: navigator.platform,
            subString: "Mac",
            identity: "Mac"
        },
        {
            string: navigator.userAgent,
            subString: "iPhone",
            identity: "iPhone/iPod"
        },
        {
            string: navigator.platform,
            subString: "Linux",
            identity: "Linux"
        }
    ]
};
browser.init();
$(function () {
    $(document.body).addClass(browser.id);
    $(document.body).addClass(browser.id + browser.version);
    browser.key = {
        isDigit: function(code) {
            return code >= 48 && code <= 57
        },
        BACKSPACE: 8,
        TAB: 9,
        DELETE: 46,
        isArrows: function(key) {
            return (key >= 37 && key <= 40) || (key >= 96 && key <= 105)
        },
        is: function(char, key) {
            return char.charCodeAt(0) == key
        },
        NUM_MINUS: 109,
        MINUS: browser.ff ? 173 : 189,
        NUM_POINT: 110,
        POINT: 190,
        isReturn: function(code) {
            return code == 10 || code == 13
        }
    }
    browser.event_capture = $("<div></div>")[0].setCapture
});