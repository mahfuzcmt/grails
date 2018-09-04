/**
 * Created by sajedur on 29/10/2014.
 */
 bm.onReady(app.tabs, "liveChat", function() {
    app.tabs.liveChat.tabInitFunctions.tag = function(panel) {
        var editor = new app.tabs.liveChat.tag(panel, this)
        editor.init()
    }
})

app.tabs.liveChat.tag = function(panel, appTab) {
    this.tool = panel.tool
    this.body = panel
    this.appTab = appTab
    this.ajax_url = app.baseUrl + "liveChatAdmin/loadTag"
    app.tabs.liveChat.tag._super.constructor.call(this, arguments)
};

(function() {
    var _tt = app.tabs.liveChat.tag.inherit(app.SingleTableView)
    var _super = app.tabs.liveChat.tag._super

    _tt.init = function() {
        _super.init.apply(this, arguments)
        var _self = this;

        this.tool.find(".reload").click(function() {
            _self.reload()
        })
        _self.attachEvents();
    }


    _tt.sortable = {
        list: {
            "0": "name"
        },
        sorted: "0",
        dir: "up"
    }

    _tt.attachEvents = function() {
        var _self = this;
        var lastRow = this.body.find("tr.last-row");
        lastRow.find(".add-tag").on("click", function() {
            var isValid = lastRow.valid({
                show_error: false
            });
            if(!isValid) {
                return;
            }
            var name =  lastRow.find("[name=name]").val();
            if(!name) {
                _self.errorHighlight(lastRow.find("[name=name]"))
                return;
            }
            _self.save(null, name)
        });

        lastRow.on("invalid", function(evnent, obj) {
            bm.notify($.i18n.prop(obj.msg_template, obj.msg_params), "error");
            _self.errorHighlight(obj.validator.elm)
        });

        lastRow.on("keypress", function(e) {
            if (e.which == 13){
                lastRow.find(".add-tag").trigger("click")
            }
        });

        this.body.find("span.tool-icon.remove").on("click", function(){
            _self.remove($(this).attr("entity-id"));
        });
    }

    _tt.afterTableReload = function() {
        this.attachEvents();
    }

    _tt.errorHighlight = function (item) {
        item.addClass("error-highlight");
        setTimeout(function() {
            item.removeClass("error-highlight")
        }, 1000);
    }

    _tt.save = function(id, name, errorCallBack) {
       var _self = this;
        bm.ajax({
            url: app.baseUrl + "liveChatAdmin/saveTag",
            data: {id: id, name: name},
            success: function(resp) {
                app.global_event.trigger("chat-tag-saved", [resp.id, name])
                _self.reload();
            },
            error: function() {
                if($.isFunction(errorCallBack)) {
                    errorCallBack();
                }
            }
        })
    };

    _tt.remove = function(id) {
       var _self = this;
        bm.ajax({
            url: app.baseUrl + "liveChatAdmin/removeTag",
            data: {id: id},
            success: function() {
                app.global_event.trigger("chat-tag-deleted", [id]);
                _self.reload();
            }
       })
    };

    _tt.afterCellEdit = function(td, value, oldValue) {
        var tr = td.parent();
        var id = tr.attr("entity-id");
        if(!value || value.length > 100 ) {
            if(value.length > 100 ) {
                bm.notify($.i18n.prop("enter.no.more.characters", [100]), "error")
            }
            this.errorHighlight(td.find("input"));
            return false
        }
        this.save(id,  value, function() {
            td.find(".value").text(oldValue);
        });
    };

})()