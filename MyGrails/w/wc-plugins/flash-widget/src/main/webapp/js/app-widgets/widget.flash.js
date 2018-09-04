app.widget.flash = function (config) {
    app.widget.flash._super.constructor.apply(this, arguments);
}

var _fw = app.widget.flash.inherit(app.widget.base)
app.widget.flash.config_width = 800;

_fw.init = function () {
    var _self = this;
    app.widget.flash._super.init.call(this);
    var container = _self.content

    var parameterTemplate = '<tr><td><input class="td-full-width" type="text" name="paramName"></td>' +
        '<td><input class="td-full-width" type="text" name="paramValue"></td><td><span class="tool-icon remove"></span></td></tr>'
    var PARAM_TEMPLATE = '<param name="$PNAME$" value="$PVALUE$">';
    var FLASH_TEMPLATE = '<object classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" width="$WIDTH$" height="$HEIGHT$" align="middle">' +
        '<param name="movie" value="$SWFURL$"/> $PARAMS$ ' +
        '<object type="application/x-shockwave-flash" data="$SWFURL$" width="$WIDTH$" height="$HEIGHT$" $ATTRIBUTES$>' +
            '<param name="movie" value="$SWFURL$"/>' +
            '<a href="http://www.adobe.com/go/getflash">' +
                '<img src="http://www.adobe.com/images/shared/download_buttons/get_flash_player.gif" alt="Get Adobe Flash player"/>' +
            '</a>' +
        '</object></object>';

    container.find("[name='local']").change(function() {
        container.find(".local-flash-upload").radio("check").trigger("change");
    })
    container.find("[name='local']").on("file-submit", function(event, resp) {
        container.find("[name='local_url']").val(resp.url).addClass("has_url")
        container.find("[name='local_file_path']").val(resp.path)
        container.find("[name='local_file_name']").val(resp.name)
    })
    container.find("[name='direct_url']").ichange(function() {
        container.find(".direct-upload").radio("check").trigger("change");
    })

    container.find(".select-from-asset-library").on("click", function() {
        function handler(fileUrl) {
            container.find("input[name='asset_library_url']").val(fileUrl).trigger("validate")
        }
        container.find(".asset-library-upload").radio("check").trigger("change")
        bm.selectFromAssetLibrary($.i18n.prop("select.your.flash.file"), handler, ["swf"], {}, true)
    })

    container.find(".add-parameter").on("click", function() {
        var temp = $(parameterTemplate);
        container.find(".parameters tbody").append(temp);
        temp.find(".tool-icon.remove").on("click", function() {
            temp.remove();
        })
    })

    container.find(".parameters .tool-icon.remove-all").on("click", function() {
        container.find(".parameters tbody tr:not(:first)").remove();
    })

    container.find(".tool-icon.remove").on("click", function() {
        $(this).closest("tr").remove();
    })
    function generateHTMLCode() {
        var selectedUrl = container.find("[name='" + container.find("[name='upload_type']:checked").val() + "_url']").val();
        if(!selectedUrl) {
            return "";
        }
        var code = FLASH_TEMPLATE.replace(/\$SWFURL\$/g, selectedUrl);
        var params = "";
        container.find(".parameters tr:not(':first')").each(function() {
            var nv = $(this).find("input");
            if($.trim(nv[0].value)) {
                var param = PARAM_TEMPLATE.replace("$PNAME$", $.trim(nv[0].value)).replace("$PVALUE$", $.trim(nv[1].value));
                params += " " + param;
            }
        });
        code = code.replace("$PARAMS$", params);
        code = code.replace("$PARAMS$", "");
        var w = container.find("[name='width']").val();
        code = code.replace(/\$WIDTH\$/g, w ? w : 400);
        var h = container.find("[name='height']").val();
        code = code.replace(/\$HEIGHT\$/g, h ? h : 300);
        return code.replace("$ATTRIBUTES$", container.find("[name='attribute']").val());
    }

    function preview() {
        var code = generateHTMLCode();
        if(!code) return;
        container.find(".flash-widget-preview").html(code);
    }

    container.find(".preview-button").on("click", function() {
        preview();
    })
    preview();
}
