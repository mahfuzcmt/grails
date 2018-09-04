jasmine.Sandbox = {
    specs: {},
    addSpec: function(name, specFunc) {
        this.specs[name] = specFunc;
    },
    runSpecs: function(name) {
        $("#TrivialReporter").remove();
        var env = new jasmine.Env().setAsCurrent();
        if(name == "*") {
            $.each(this.specs, function(key) {
                jasmine.describe(key, this);
            });
        } else if($.isArray(name)) {
            $.each(name, function() {
                jasmine.describe(this, jasmine.Sandbox.specs[this]);
            });
        } else {
            jasmine.describe(name, jasmine.Sandbox.specs[name]);
        }
        env.addReporter(new jasmine.TrivialReporter({
            location: {search: ""},
            body: $("#JasmineReportContainer")[0]
        }));
        env.execute();
    },
    initUi: function() {
        var sandbox = $("#jasmine-ui-sandbox");
        if(sandbox.length) {
            sandbox.empty()
        } else {
            sandbox = $("<div id='jasmine-ui-sandbox'></div>").css({
                position: "absolute",
                left: -10000,
                top: -10000
            }).appendTo(document.body)
        }
        return sandbox
    },
    ui: function() {
        var sandbox = $("#jasmine-ui-sandbox");
        if(!sandbox.length) {
            sandbox = jasmine.Sandbox.initUi()
        }
        return sandbox
    },
    removeUi: function() {
        $("#jasmine-ui-sandbox").remove()
    }
}

$(function() {
    var containerDom = '<div id="JasmineReportContainer">  \
                        <div class="spec-selector-row">  \
                            <label>Choose A Spec</label>  \
                            <select>\
                                <option value="*">All</option>\
                            </select>              \
                        </div>\
                        <div class="run-button-row">\
                            <input type="button" value="Run">\
                        </div>\
                    </div>';
    var container;
    function initContainer() {
        container = $(containerDom);
        $(document).one("keyup.shift_t", function() {
            $(document.body).append(container);
            var specSelect = container.find("select");
            $.each(jasmine.Sandbox.specs, function(name) {
                var option = $("<option></option>");
                option.text(name);
                specSelect.append(option);
            });
            container.find("input").click(function() {
                jasmine.Sandbox.runSpecs(specSelect.val());
            });
            $(document).one("keyup.shift_t", function() {
                container.remove();
                initContainer();
                return false;
            });
            return false;
        });
    }
    initContainer();
});