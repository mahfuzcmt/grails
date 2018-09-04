(function() {
    $.hotkeys = {
        version: "0.8+",
        specialKeys: {
            8: "backspace", 9: "tab", 13: "return", 16: "shift", 17: "ctrl", 18: "alt", 19: "pause",
            20: "capslock", 27: "esc", 32: "space", 33: "pageup", 34: "pagedown", 35: "end", 36: "home",
            37: "left", 38: "up", 39: "right", 40: "down", 45: "insert", 46: "del",
            96: "0", 97: "1", 98: "2", 99: "3", 100: "4", 101: "5", 102: "6", 103: "7",
            104: "8", 105: "9", 106: "*", 107: "+", 109: "-", 110: ".", 111 : "/",
            112: "f1", 113: "f2", 114: "f3", 115: "f4", 116: "f5", 117: "f6", 118: "f7", 119: "f8",
            120: "f9", 121: "f10", 122: "f11", 123: "f12", 144: "numlock", 145: "scroll", 188: ",", 190: ".",
            191: "/", 224: "meta"
        },
        shiftNums: {
            "`": "~", "1": "!", "2": "@", "3": "#", "4": "$", "5": "%", "6": "^", "7": "&",
            "8": "*", "9": "(", "0": ")", "-": "_", "=": "+", ";": ": ", "'": "\"", ",": "<",
            ".": ">", "/": "?", "\\": "|"
        }
    };

    function keyHandler( handleObj ) {
        var origHandler = handleObj.handler, all_namespaces = (handleObj.namespace || "").toLowerCase().split(".");
        var keys = [];
        var namespace
        $.each(all_namespaces, function() {
            var isHot = (this != "" && this != "autocomplete" && (/^(key|alt|ctrl|meta|shift)_/.test(this)))
            if(isHot) {
                keys.push(/^key_/.test(this) ? this.substring(4) : this)
            } else {
                if(namespace) {
                    namespace += "." + this
                } else {
                    namespace = this
                }
            }
        })
        if(keys.length == 0) {
            return;
        }
        handleObj.namespace = namespace
        handleObj.handler = function( event ) {
            if ( this !== event.target && (/textarea|select/i.test( event.target.nodeName ) ||
                event.target.type === "text" || $(event.target).prop('contenteditable') == 'true' )) {
                return;
            }
            var special = $.hotkeys.specialKeys[ event.which || event.keyCode ], character = String.fromCharCode( event.which ).toLowerCase(), modif = "", possible = {};
            if ( event.altKey && special !== "alt" ) {
                modif += "alt_";
            }
            if ( event.ctrlKey && special !== "ctrl" ) {
                modif += "ctrl_";
            }
            if ( event.metaKey && !event.ctrlKey && special !== "meta" ) {
                modif += "meta_";
            }
            if ( event.shiftKey && special !== "shift" ) {
                modif += "shift_";
            }
            if ( special ) {
                possible[ modif + special ] = true;
            } else {
                possible[ modif + character ] = true;
                possible[ modif + $.hotkeys.shiftNums[ character ] ] = true;
                if ( modif === "shift_" ) {
                    possible[ $.hotkeys.shiftNums[ character ] ] = true;
                }
            }
            for ( var i = 0, l = keys.length; i < l; i++ ) {
                if ( possible[ keys[i] ] ) {
                    return origHandler.apply( this, arguments );
                }
            }
        };
    }
    $.each([ "keydown", "keyup", "keypress" ], function() {
        $.event.special[ this ] = { add: keyHandler };
    });
})();