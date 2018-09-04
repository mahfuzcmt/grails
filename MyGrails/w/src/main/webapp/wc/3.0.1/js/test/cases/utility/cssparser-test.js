$(function() {
    jasmine.Sandbox.addSpec("CSS Parser", function() {
        jasmine.it("simple basic rule", function() {
            var parser = new CssParser("div.divti a, div a {display: table; display: none;}");
            var map = parser.parse()
            jasmine.expect(map.rulesets.length).toBe(1)
            var rule = map.rulesets[0];
            jasmine.expect(rule.attributes.length).toBe(2)
            jasmine.expect(rule.attributes[0].key).toBe("display")
            jasmine.expect(rule.attributes[1].value).toBe("none")
            jasmine.expect(rule.selectors.join(",")).toBe("div.divti a,div a")
        })
        jasmine.it("attribute value", function() {
            var parser = new CssParser(".ie9 .left-panel> .header,.ie9 .right-panel> .header,.ie9 .table-view > .header,.ie9 .menu-item:hover {" +
                " background : -ms-linear-gradient(top, #FFFFFF 0%, #E9E9E9 44%, #E9E9E9 100%); /* IE10+ */ background : linear-gradient(to bottom, #FFFFFF 0%, #E9E9E9 44%, #E9E9E9 100%); /* W3C */" +
                " border-radius : 0 0 5px 5px;" +
                " background-image : url('../../images/admin/view-popup-close.png');" +
                " font-family    : 'armataregular';}");
            var map = parser.parse()
            jasmine.expect(map.rulesets.length).toBe(1)
            var rule = map.rulesets[0];
            jasmine.expect(rule.attributes.length).toBe(5)
            jasmine.expect(rule.attributes[0].key).toBe("background")
            jasmine.expect(rule.attributes[1].value).toBe("linear-gradient(to bottom, #FFFFFF 0%, #E9E9E9 44%, #E9E9E9 100%)")
            jasmine.expect(rule.attributes[2].value).toBe("0 0 5px 5px")
            jasmine.expect(rule.attributes[3].value).toBe("url('../../images/admin/view-popup-close.png')")
            jasmine.expect(rule.attributes[4].value).toBe("'armataregular'")
            jasmine.expect(rule.selectors.join(",")).toBe(".ie9 .left-panel> .header,.ie9 .right-panel> .header,.ie9 .table-view > .header,.ie9 .menu-item:hover")
        })
        jasmine.it("simple media query", function() {
            var parser = new CssParser("@media all and (min-width: 700px) {.facet_sidebar { display: none; }}");
            var map = parser.parse()
            var rule = map.rulesets[0];
            var child = rule.rulesets[0];
            var attr = child.attributes[0];
            jasmine.expect(rule.type).toBe("media")
            jasmine.expect(rule.definition).toBe("all and (min-width: 700px)")
            jasmine.expect(child.selectors.join(",")).toBe(".facet_sidebar")
            jasmine.expect(attr.key).toBe("display");
            jasmine.expect(attr.value).toBe("none");

        })
        jasmine.it("multiple media query", function() {
            var parser = new CssParser(".facet_sidebar { display: none; }@media all and (min-width: 700px) {.facet_sidebar { display: none; }}@media (max-width: 600px) {.facet_sidebar #a, :before { display: none; }}.facet_sidebar a { display: none; }")
            var map = parser.parse()
            var rule = map.rulesets[2];
            var child = rule.rulesets[0];
            var attr = child.attributes[0];
            jasmine.expect(rule.type).toBe("media")
            jasmine.expect(rule.definition.trim()).toBe("(max-width: 600px)")
            jasmine.expect(child.selectors.join(",")).toBe(".facet_sidebar #a,:before")
            jasmine.expect(attr.key).toBe("display");
            jasmine.expect(attr.value).toBe("none");
        })
        jasmine.it("basic keyframe", function() {
            var parser = new CssParser("@keyframes mymove{from {top:0px;}to {top:200px;}}");
            var map = parser.parse()
            var rule1 = map.rulesets[0];
            var keyframes = rule1.frames;
            jasmine.expect(rule1.id).toBe("mymove")
            jasmine.expect(keyframes[0].offset).toBe("from")
            jasmine.expect(keyframes[0].declarations[0].key).toBe("top")
            jasmine.expect(keyframes[0].declarations[0].value).toBe("0px")
        })
        jasmine.it("basic import", function() {
            var parser = new CssParser("@import url('fineprint.css') print;" +
                "@import url('bluish.css') projection, tv;" +
                "@import 'custom.css';" +
                "@import 'common.css' screen, projection;" +
                "@import url('landscape.css') screen and (orientation:landscape); div {color : red;}");
            var map = parser.parse()
             var imports = map.imports;
            jasmine.expect(imports.length).toBe(5)
            jasmine.expect(imports[0]).toBe("url('fineprint.css') print")
        })
    });
})