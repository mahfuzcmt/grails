app.widget.image = function(config) {
    app.widget.image._super.constructor.apply(this, arguments);
}

var _p = app.widget.image.inherit(app.widget.base);

_p.init = function() {
    var _self = this;
    app.widget.image._super.init.call(this);
    var container = _self.content

    function updateImage(type) {
        var src = container.find("[name='" + type + "_url']").val();
        var img = container.find("#widget-image-preview");
        if(type == "local") {
            var input = container.find("[name='localImage']")
            var file
            if(input.val()) {
                if(window.FileReader) {
                    file = input[0].files[0]
                } else {
                    file = container.data("form-extra-data").find("this.name=='localImage'").value
                }
            }
            if(file) {
                if(window.FileReader) {
                    var reader = new FileReader()
                    reader.onload = function(e) {
                        img.attr('src', e.target.result);
                    }
                    reader.readAsDataURL(file);
                }
            }
        } else {
            img.attr("src", src).show();
        }
    }

    container.find("[name='localImage']").on("change", function() {
        var radio = container.find(".local-image-upload")
        if(!radio.is(":checked")) {
            radio.radio("check").trigger("change", [false]);
        }
        updateImage("local")
    })/*.on("file-submit", function(event, resp) {
        container.find("[name='local_url']").val(resp.url).addClass("has_url");
    })*/

    container.find("[name='direct_url']").ichange(function() {
        updateImage("direct")
        var radio = container.find(".direct-upload")
        if(!radio.is(":checked")) {
            radio.radio("check").trigger("change", [false]);
        }
    })

    container.find(".select-from-asset-library").on("click", function() {
        function handler(imgUrl) {
            container.find("input[name='asset_library_url']").val(imgUrl)
            updateImage("asset_library")
        }
        var radio = container.find(".asset-library-upload")
        if(!radio.is(":checked")) {
            radio.radio("check").trigger("change", [false]);
        }
        bm.selectFromAssetLibrary($.i18n.prop("select.your.image"), handler)
    })

    container.find(".asset-library-upload, .local-image-upload, .direct-upload").change(function(ev, changeImage) {
        if(changeImage !== false) {
            updateImage($(this).val())
        }
    })
}