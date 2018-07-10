app.FrontEndEditor.prototype.imageEditor = function () {
    var _self = this;
    var highlightedElement;
    var currentElement;
    var popupEl;
    var popupInstance;
    var previewElement;
    var csdkImageEditor;
    var currentImage; // assigned when the Edit button is clicked

    var defaults = {
        popupClass: 'fee-image-popup confirm-popup',
        animationClass: 'animated zoomIn',
        closingAnimationClass: 'animated fadeOut',
        template: "<form class=\"edit-popup-form image-upload\" method=\"post\" action=\"/app/uploadWceditorImage\" enctype=\"multipart/form-data\">\n" +
        "       <div class=\"header-line\"><span class=\"title\">Upload Image</span><span class=\"icon close\"></span></div>" +
        "    <div class=\"body\">\n" +
        "        <div class=\"form-image-block\">\n" +
        "            <input type=\"file\" name=\"file\" file-type=\"image\" remove-option-name=\"remove-image\" size-limit=\"2097152\" previewer=\"upload-image-preview\" validation=\"drop-file-required\">\n" +
        "        </div>\n" +
        "            <div class=\"resp-message error\"></div>\n" +
        "            <div class=\"preview-image\">\n" +
        "                <div class=\"image-wrapper\">\n" +
        "                    <img id=\"upload-image-preview\" src=''>\n" +
        "                </div>\n" +
        "                <div class=\"image-description-wrapper\">\n" +
        "                    <p class=\"image-name\"></p>\n" +
        "                    <span class=\"image-size\"></span>\n" +
        "                </div>\n" +
        "                <div class=\"image-edit-button\">\n" +
        "        <button type=\"button\" class=\"edit-button fee-btn\">Edit</button>\n" +
        "                </div>\n" +
        "            </div>\n" +
        "    </div>\n" +
        "    <div class=\"button-line\">\n" +
        "        <button type=\"button\" class=\"cancel-button\">Cancel</button>\n" +
        "        <button type=\"submit\" class=\"submit-button edit-popup-form-submit\">Upload</button>\n" +
        "    </div>\n" +
        "</form>",
        csdkAPIKey: "ivi35oh8nmumnuhp"
    };

    var postInit = {
        initVars: function () {

        },
        bind: function () {
            var popupConfig = {
                clazz: defaults.popupClass,
                animation_clazz: defaults.animationClass,
                closing_animation_clazz: defaults.closingAnimationClass,
                is_fixed: true,
                draggable: true,
                drag_handle: '*',
                drag_cancel: '.content',
                is_always_up: true,
                //width: '900px',
                show_title: false,
                show_close: false,
                maximizable: false,
                minimizable: false,
                content: defaults.template,
                events: {
                    content_loaded: function (contentEl) {
                        popupEl = contentEl.el;
                        previewElement = popupEl.find(".preview-image");
                        previewElement.find(".image-edit-button").remove();
                        var imgSrc = currentElement.attr('src');

                        previewElement.find("img").attr('src', imgSrc);
                        previewElement.find(".image-name").text(_self.common.getFileName(imgSrc, true));

                        var obj = new XMLHttpRequest();
                        obj.open('HEAD', imgSrc, true);
                        obj.onreadystatechange = function(){
                            if ( obj.readyState == 4 ) {
                                if ( obj.status == 200 ) {
                                    //alert('Size in bytes: ' + obj.getResponseHeader('Content-Length'));
                                    var imageSize = obj.getResponseHeader('Content-Length');
                                    imageSize = (imageSize/1000) +" KB";
                                    previewElement.find(".image-size").text(imageSize);
                                }
                            }
                        };
                        obj.send(null);
                    }
                }
            };

            popupInstance = new POPUP(popupConfig);

            popupEl.find(".edit-button").on("click", function () {
                currentImage = previewElement.find("img")[0];
                csdkImageEditor.launch({
                    image: currentImage.id,
                    url: currentImage.src
                });

            });

            popupEl.find(".cancel-button").on("click", function () {
                popupInstance.close();
            });

            popupEl.find("input[type='file']").on("change", function () {
                if (this.files && this.files[0]) {
                    var file = this.files[0];
                    var FR = new FileReader();
                    FR.onload = function () {
                        var imgSrc = FR.result;
                        previewElement.find("img").attr('src', imgSrc);
                        previewElement.find(".image-name").text(file.name);
                        var imageSize = ((file.size)/1024) +" KB";
                        previewElement.find(".image-size").text(imageSize);
                    };
                    FR.readAsDataURL(this.files[0]);
                }
            });

            var currentWidget = currentElement.closest(".widget");
            var widgetType = currentWidget.attr("widget-type");
            if(widgetType === 'storeLogo'){
                popupEl.find('form').attr("action", app.baseUrl + "frontEndEditor/editStoreLogo");
                popupEl.find("form input[type='file']").attr("size-limit", "51200");
            }

            popupEl.find(".resp-message").html("");
            popupEl.find('form').form({
                ajax: true,
                preSubmit: function (ajaxSettings) {
                    $.extend(ajaxSettings, {}, null, {
                        error: function (jqXHR, textStatus, errorThrown ) {
                            popupEl.find(".resp-message").html(errorThrown.message);
                        },
                        success: function (resp) {
                            popupEl.find(".resp-message").html("");
                            if(resp.url) {
                                currentElement.attr('src', resp.url);
                            }
                            popupInstance.close();
                        }
                    });
                }
            });
        }
    }
    return {
        init: function (_currentElement, _highlightedElement) {
            currentElement = _currentElement;
            highlightedElement = _highlightedElement;
            postInit.initVars();
            postInit.bind();
            return this;
        }
    };
};
