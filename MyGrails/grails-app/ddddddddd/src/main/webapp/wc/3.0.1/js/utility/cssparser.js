function CssParser (css) {
    this.reader = css ? css.reader(true) : undefined;
    this.rulesets = [];
    this.imports = [];
}

var _c = CssParser.prototype;

(function() {
    function skipUptoNonSpace() {
        var c;
        var terminator = [' ', '\r', '\n', '\t'];
        while(terminator.contains(c = this.reader.read())){}
        return c;
    }
    
    function skipUptoSpace() {
        var c;
        do{
            c = this.reader.read();
        }while(c != " ")
    }
    
    function skipComment() {
        var c = this.reader.read();
        if(c != '*') {
            return;
        }
        do {
            while((c = this.reader.read()) != '*'){}
            c = this.reader.read();
        } while(c != '/');
    }
    
    function readUpto(terminator, writer) {
        var c;
        while(!terminator.contains(c = this.reader.read())) {
            writer.plus(c);
        }
        return c;
    }
    
    function readRuleset(current) {
        var builder = new StringWriter();
        var selectors = [];
        builder.plus(current);
        var terminate;
        do {
            do {
                terminate = readUpto.call(this, ['/', ',', '{'], builder);
                if(terminate == '/') {
                    skipComment.call(this);
                } else {
                    selectors.push(builder.toString().trim());
                    builder = new StringWriter();
                }
            } while(terminate == '/');
        } while (terminate != '{');
        var attributes = [];
        do {
            terminate = readUpto.call(this, [':', '}'], builder);
            if(terminate == ':' ) {
                var key = builder.toString().trim();
                builder = new StringWriter();
                while(true) {
                    terminate = readUpto.call(this, ['(', ';'], builder);
                    if(terminate == '(') {
                        builder.plus(terminate);
                        readUpto.call(this, [')'], builder);
                        builder.plus(')');
                    } else {
                        break;
                    }
                }
                var value = builder.toString().trim();
                var map = {};
                map.key = key;
                map.value = value;
                attributes.push(map);
                builder = new StringWriter();
                while(true) {
                    terminate = skipUptoNonSpace.call(this);
                    if(terminate == '/') {
                        skipComment.call(this);
                    } else {
                        builder.plus(terminate);
                        break;
                    }
                }
            } else {
                builder = new StringWriter();
            }
        } while (terminate != '}');
        return new CssRule(selectors, attributes);
    }
    
    function readMedia() {
        var builder = new StringWriter();
        skipUptoSpace.call(this);
        readUpto.call(this, ['{'], builder);
        var definition = builder.toString().trim();
        var rulesets = [];
        var terminate;
        do{
            terminate = skipUptoNonSpace.call(this);
            if(terminate == '/') {
                skipComment.call(this)
            }
            else if (terminate == "}") {
                break;
            }
            else if (terminate == "@") {
                rulesets.push(readKeyframe.call(this, terminate))
            }
            else {
                rulesets.push(readRuleset.call(this, terminate));
            }
        } while (true)
    
        return new CssMedia(definition, rulesets);
    }
    
    function readImport() {
        var builder = new StringWriter();
        skipUptoSpace.call(this);
        readUpto.call(this, [';'], builder);
        return builder.toString().trim();
    }
    
    function readFrames(c) {
        var frame = {};
        var builder = new StringWriter();
        builder.plus(c);
        var terminate;
        readUpto.call(this, ['{'], builder);
        frame.offset = builder.toString().trim();
        var declarations = [];
        builder = new StringWriter();
        do {
            terminate = readUpto.call(this, [':', '}'], builder);
            if(terminate == ':' ) {
                var key = builder.toString().trim();
                builder = new StringWriter();
                terminate = readUpto.call(this, [';'], builder);
                var value = builder.toString().trim();
                var map = {};
                map.key = key;
                map.value = value;
                declarations.push(map);
                builder = new StringWriter();
                while(true) {
                    terminate = skipUptoNonSpace.call(this);
                    if(terminate == '/') {
                        skipComment.call(this);
                    } else {
                        builder.plus(terminate);
                        break;
                    }
                }
            } else {
                builder = new StringWriter();
            }
        } while (terminate != '}');
        frame.declarations = declarations;
        return frame;
    
    }
    
    function readKeyframe() {
        var frames = [];
        var builder = new StringWriter();
        skipUptoSpace.call(this);
        readUpto.call(this, ['{'], builder);
        var id = builder.toString().trim();
        var terminate
        do{
            terminate = skipUptoNonSpace.call(this);
            if(terminate == '/') {
                skipComment.call(this)
            } else if (terminate == "}") {
                break;
            } else {
                frames.push(readFrames.call(this, terminate));
            }
        } while (true)
        return new CssKeyframe(id, frames);
    }
    
    _c.parse = function() {
        if(!this.reader) {
            return this;
        }
        try {
            while(true) {
                var c = skipUptoNonSpace.call(this);
                if(c == -1) {
                    break;
                }
                switch(c.charCodeAt(0)) {
                    case 47: //'/':
                        skipComment.call(this);
                        break;
                    case 64: //'@':
                        c = this.reader.read();
                        if(c == "m") {
                            this.rulesets.push(readMedia.call(this));
                        } else if(c == "i") {
                            this.imports.push(readImport.call(this))
                        } else if(c == "k") {
                            this.rulesets.push(readKeyframe.call(this));
                        } else {
                            this.rulesets.push(readRuleset.call(this, "@" + c));
                        }
                        break;
                    case 46: //'.':
                    case 35: //'#':
                    case 91: //'[':
                    case 58: //':':
                    case 97: //'a':
                    case 98: //'b':
                    case 99: //'c':
                    case 100: //'d':
                    case 101: //'e':
                    case 102: //'f':
                    case 103: //'g':
                    case 104: //'h':
                    case 105: //'i':
                    case 106: //'j':
                    case 107: //'k':
                    case 108: //'l':
                    case 109: //'m':
                    case 110: //'n':
                    case 111: //'o':
                    case 112: //'p':
                    case 113: //'q':
                    case 114: //'r':
                    case 115: //'s':
                    case 116: //'t':
                    case 117: //'u':
                    case 118: //'v':
                    case 119: //'w':
                    case 120: //'x':
                    case 121: //'y':
                    case 122: //'z':
                    case 65: //'a':
                    case 66: //'b':
                    case 67: //'c':
                    case 68: //'d':
                    case 69: //'e':
                    case 70: //'f':
                    case 71: //'g':
                    case 72: //'h':
                    case 73: //'i':
                    case 74: //'j':
                    case 75: //'k':
                    case 76: //'l':
                    case 77: //'m':
                    case 78: //'n':
                    case 79: //'o':
                    case 80: //'p':
                    case 81: //'q':
                    case 82: //'r':
                    case 83: //'s':
                    case 84: //'t':
                    case 85: //'u':
                    case 86: //'v':
                    case 87: //'w':
                    case 88: //'x':
                    case 89: //'y':
                    case 90: //'z':
                        this.rulesets.push(readRuleset.call(this, c));
                        break;
                }
            }
        } catch(eof) {
        }
        return this;
    }
})()

_c.getRule = function(selectors) {
    if(arguments.length == 2) {
        var media = this.getMedia(selectors);
        if(media) {
            selectors = arguments[1]
            return media.getRule(selectors);
        }
        return null;
    }
    if(typeof selectors == "string") {
        selectors = [selectors];
    }
    var rule;
    $.each(this.rulesets, function() {
        if(this.type == "rule" && this.selectors.length == selectors.length) {
            var match = true;
            $.each(this.selectors, function() {
                if(!selectors.collect(function() {return "" + this}).contains("" + this)) {
                    match = false;
                    return false;
                }
            })
            if(match) {
                rule = this;
                return false;
            }
        }
    })
    return rule;
}

_c.getMedias = function() {
    var media = [];
    $.each(this.rulesets, function() {
        if (this.type == "media") {
            media.push(this);
        }
    })
    return media;
}

_c.getMedia = function(query) {
    var media;
    $.each(this.rulesets, function() {
        if (this.type == "media" && this.definition == query) {
            media = this;
            return false;
        }
    })
    return media;
}

_c.setAttribute = function(selector, attrName, value) {
    var rule = this.getRule(selector);
    if(rule) {
        rule.setAttribute(attrName, value);
    } else {
        var attribute = {};
        attribute[attrName] = value;
        this.addRule(selector, attribute)
    }
}

_c.getAttribute = function(selector, attrName) {
    var rule = this.getRule(selector);
    if(rule) {
        return rule.getAttribute(attrName);
    }
    return null;
}

_c.removeAttribute = function(selector, attrName) {
    var rule = this.getRule(selector);
    if(rule) {
        rule.removeAttribute(attrName);
    }
}

_c.toString = function(prettyPrint) {
    if(arguments.length == 0) {
        prettyPrint = false;
    }
    var builder = new StringWriter();
    var imports = this.imports;
    for (var i = 0;i < imports.length; i++) {
        builder.plus("@import " + imports[i] + ";" + (prettyPrint ? "\n" : ""))
    }
    var rulesets = this.rulesets;
    for (var i = 0; i < rulesets.length; i++) {
        builder.plus(rulesets[i].toString(prettyPrint, 0))
    }
    return builder.toString();
}

_c.removeMedia = function (media) {
    if(!media) {
        return;
    }
    if(media instanceof CssMedia) {
        for( var i = 0; i < this.rulesets.length; i++) {
            if(this.rulesets[i] == media) {
                this.rulesets.splice(i, 1);
                break;
            }
        }
    } else {
        this.removeMedia(this.getMedia(media))
    }
}

_c.removeMediaRules = function(selector) {
    if(!selector) {
        return;
    }
    var medias = this.getMedias()
    for(var i = 0; i < medias.length; i++) {
        var rules = medias[i].rulesets;
        for(var j = 0; j < rules.length; j++) {
            var selectors = rules[j].selectors;
            for(var k = 0; k < selectors.length; k++) {
                var _selector = selectors[k];
                if(_selector.endsWith(selector)) {
                    selectors.splice(k, 1);
                    if(selectors.length) {
                        k--;
                    }
                }
            }
            if(!selectors.length) {
                medias[i].removeRule(rules[j]);
                if(rules.length) {
                    j--;
                }
            }
        }
        if(!rules.length) {
            this.removeMedia(medias[i]);
        }
    }
}

_c.addMedia = function (definition, rulesets) {
    var media = new CssMedia(definition, rulesets);
    this.rulesets.push(media);
    return media;
}

_c.addRule = function (selectors, attributes) {
    var rule = selectors instanceof CssRule ? selectors : new CssRule(selectors, attributes);
    this.rulesets.push(rule);
    return rule;
}

_c.removeRule = function (index) {
    var rule
    if (typeof(index) == "number") {
        rule = this.rulesets.splice(index, 1);
    } else if(index instanceof CssRule) {
        for( var i = 0; i < this.rulesets.length; i++) {
            if(this.rulesets[i] == index) {
                rule = this.rulesets.splice(i, 1);
                break;
            }
        }
    } else {
        if(typeof index == "string") {
            index = [index]
        }
        for( var i = 0; i < this.rulesets.length; i++) {
            if(bm.equal(this.rulesets[i].selectors, index)) {
                rule = this.rulesets.splice(i, 1);
                break;
            }
        }
    }
    if(rule && rule.length) {
        return rule[0]
    }
}

var CssMedia = function(definition, rulesets) {
    this.definition = definition;
    this.rulesets = rulesets || [];
    this.type = "media";
}

var _m = CssMedia.prototype;

_m.getRule = function (selectors) {
    return CssParser.prototype.getRule.call(this, selectors)
}

_m.removeRule= function (index) {
    CssParser.prototype.removeRule.call(this, index)
}

_m.setAttribute = function(selector, attrName, value) {
    CssParser.prototype.setAttribute.call(this, selector, attrName, value)
}

_m.getAttribute = function(selector, attrName) {
    return CssParser.prototype.getAttribute.call(this, selector, attrName);
}

_m.removeAttribute = function(selector, attrName) {
    CssParser.prototype.removeAttribute.call(this, selector, attrName)
}

_m.addRule = function (selectors, attributes) {
    return CssParser.prototype.addRule.call(this, selectors, attributes)
}

_m.copy = function() {
    var definition = this.definition
    var rulesets = this.rulesets.collect(function() {
        return this.copy()
    })
    return new CssMedia(definition, rulesets)
}

_m.merge = function(source) {
    var _self = this
    source.rulesets.every(function() {
        var rule = _self.getRule(this.selectors)
        if(!rule) {
            rule = _self.addRule(this.selectors, [])
        }
        var attributes = {}
        this.attributes.every(function() {
            attributes[this.key] = this.value
        })
        rule.setAttribute(attributes)
    })
}

_m.toString = function(prettyPrint, depth) {
    if(arguments.length == 0) {
        prettyPrint = false;
        depth = 0;
    }
    var space = ""
    for(var i=0; i < depth; i++) {
        space += "\t";
    }
    var builder = new StringWriter();
    builder.plus(space + "@media " + this.definition + (prettyPrint ? " {\n" : "{"))
    for (var i = 0; i < this.rulesets.length; i++) {
        var rule = this.rulesets[i];
        builder.plus(rule.toString(prettyPrint, depth + 1))
    }
    builder.plus(prettyPrint ? (space + "}\n") : "}");
    return builder.toString();
}

var CssRule = function(selectors, attributes) {
    if(!$.isArray(selectors)) {
        selectors = [selectors];
    }
    this.selectors = selectors.collect(function() {return "" + this}); //ensuring primitive insertion
    if(attributes && !$.isArray(attributes)) {
        var _attrs = [];
        $.each(attributes, function(key) {
            _attrs.push({key: key, value: this})
        })
        attributes = _attrs;
    }
    this.attributes = attributes || [];
    this.type = "rule"
}

var _r = CssRule.prototype;

_r.addAttribute = function(attrName, value) {
    var _rule = this;
    if(arguments.length == 1) {
        $.each(attrName, function(key, value) {
            _rule.addAttribute(key, value)
        })
    } else {
        var map = {};
        map.key = attrName;
        map.value = value;
        this.attributes.push(map)
    }
}

_r.setAttribute = function(attrName, value) {
    var _rule = this;
    if(arguments.length == 1) {
        $.each(attrName, function(key, value) {
            _rule.setAttribute(key, value)
        })
    } else {
        var attr;
        $.each(this.attributes, function() {
            if(this.key == attrName) {
                attr = this;
                return false;
            }
        })
        if(attr) {
            attr.value = value;
        } else {
            var map = {};
            map.key = attrName;
            map.value = value;
            this.attributes.push(map)
        }
    }
}

_r.getAttribute = function(attrName) {
    var attr;
    $.each(this.attributes, function() {
        if(this.key == attrName) {
            attr = this;
            return false;
        }
    })
    if(attr) {
        return attr.value;
    } else {
        return null;
    }
}

_r.getAttributes = function(attrName) {
    var values = [];
    $.each(this.attributes, function() {
        if(this.key == attrName) {
            values.push(this.value);
        }
    })
    return values;
}

_r.removeAttribute = function(attrName) {
    var _rule = this;
    if($.isArray(attrName)) {
        $.each(attrName, function() {
            _rule.removeAttribute(this);
        })
    } else if(typeof attrName == "string" || attrName instanceof String) {
        var _self = this;
        var attrIndices = [];
        $.each(this.attributes, function(ind) {
            if(this.key == attrName) {
                attrIndices.push(ind);
            }
        })
        var adjust = 0;
        $.each(attrIndices, function() {
            _self.attributes.splice(this + adjust--, 1);
        })
    } else if(typeof attrName == "number") {
        this.attributes.splice(attrName, 1);
    }
    return this;
}

_r.copy = function() {
    var selectors = this.selectors.collect(function() {
        return this;
    })
    var attributes = this.attributes.collect(function() {
        return {key: this.key, value: this.value};
    })
    return new CssRule(selectors, attributes)
}

_r.toString = function(prettyPrint, depth) {
    if(arguments.length == 0) {
        prettyPrint = false;
        depth = 0;
    }
    var space = ""
    for(var i=0; i<depth; i++){
        space += "\t";}
    var attrSpace = space + "\t"
    var builder = new StringWriter();
    if(prettyPrint) {
        for (var i =0; i < this.selectors.length; i++) {
            var selector = this.selectors[i]
            builder.plus(space + selector)
            if(i < (this.selectors.length - 1)){
                builder.plus(",\n")
            }
        }
        builder.plus(" " +"{\n");
    } else {
        builder.plus(space + this.selectors.join(prettyPrint ? ", " : ","))
        builder.plus("{");
    }
    var attributes = this.attributes;
    for(var j = 0; j < attributes.length ; j++) {
        builder.plus((prettyPrint ? attrSpace : "") + attributes[j].key + (prettyPrint ? " : " : ":") + attributes[j].value + ";");
        builder.plus(prettyPrint ? "\n"  : "")
    }
    builder.plus(prettyPrint ? (space + "}\n") : "}");
    return builder.toString();
}

var CssKeyframe = function(id, frames) {
    this.id = id;
    this.frames = frames;
    this.type = "keyframe";
}

var _k = CssKeyframe.prototype;

_k.toString = function (prettyPrint, depth) {
    if(arguments.length == 0) {
        prettyPrint = false;
        depth = 0;
    }
    var space = ""
    for(var i=0; i<depth; i++){
        space += "\t";}
    var attrSpace = space + "\t"
    var builder = new StringWriter();
    builder.plus(space + "@keyframe " + this.id + (prettyPrint ? " {\n" : "{"))
    for (var i = 0; i < this.frames.length; i++) {
        var frame = this.frames[i];
        builder.plus(prettyPrint ? attrSpace : "")
        builder.plus(frame.offset)
        builder.plus(prettyPrint ? " { " : "{")
        for (var j = 0; j < frame.declarations.length; j++) {
            var declaration = frame.declarations[j];
            builder.plus(declaration.key + (prettyPrint ? ": " : ":") + declaration.value + ";")
        }
        builder.plus(prettyPrint ? " }\n" : "}")
    }
    builder.plus(prettyPrint ? (space + "}\n") : "}");
    return builder.toString();
}