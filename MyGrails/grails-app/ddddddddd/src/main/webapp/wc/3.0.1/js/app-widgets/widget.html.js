app.widget.html = function(config) {
    app.widget.html._super.constructor.apply(this, arguments);
};

app.widget.html.config_width = 662;

var _c = app.widget.html.inherit(app.widget.base);

_c.init = function() {
    app.widget.html._super.init.call(this);
    var dom = this.content;
    dom.find("input:file").on("file-add", function(event, file) {
        if(FileReader) {
            var reader = new FileReader()
            reader.onload = function(evt) {
                dom.find("textarea").val(evt.target.result);
            }
            reader.readAsText(file);
        } else {
            //TODO: should do something for ie
        }
    });
}
