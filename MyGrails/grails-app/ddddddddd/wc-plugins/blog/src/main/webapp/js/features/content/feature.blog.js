app.tabs.blog = function() {
    this.constructor_args = arguments;
    this.text = $.i18n.prop("blogs");
    this.tip = $.i18n.prop("manage.blogs");
    this.ui_class = "blog";
    app.tabs.blog._super.constructor.apply(this, arguments);
}

var _b = app.tabs.blog.inherit(app.SingleTableTab);

_b.onSwitchMenuClick = function(type) {
    app.Tab.changeView(this, "blog", type, "SingleTableTab")
}

app.tabs.blog.post = function() {
    app.tabs.blog.post._super.constructor.apply(this, arguments);
}

var _po = app.tabs.blog.post.inherit(app.tabs.blog);

(function () {
    function attachEvent() {
        var _self = this
        this.on_global("blogpost-restore", function() {
            _self.reload();
        });
        this.body.find(".toolbar .create").on("click", function() {
            _self.createPost();
        });
    }
    _po.init = function(){
        app.tabs.blog.post._super.init.call(this);
        attachEvent.call(this);
    }

    app.global_event.on('after-ribbon-render', function(e, ribbonBar) {
        if(app.isPermitted("blog.view.list", {})) {
            ribbonBar.enable("blog");
        } else {
            ribbonBar.disable("blog");
        }
    });
})()

_po.ajax_url = app.baseUrl + "blogAdmin/loadPostAppView";
_po.advanceSearchUrl = app.baseUrl + "blogAdmin/advanceFilterPost";

_po.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit"
    },
    {
        text: $.i18n.prop("view.in.site"),
        ui_class: "preview",
        action:"view-in-website"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "remove"
    }
]

_po.switch_menu_entries = [
    {
        text: $.i18n.prop("blog.category.list"),
        ui_class: "view-switch blog-category list-view",
        action: "category"
    },
    {
        text: $.i18n.prop("blog.comment.list"),
        ui_class: "view-switch blog-comment list-view",
        action: "comment"
    }
];

_po.onMenuOpen = function(navigator) {
    var menu = this.tabulator.menu;
    var itemList = [
        {
            key: "blog.edit",
            class: "edit"
        },
        {
            key: "blog.remove",
            class: "remove"
        }
    ];
    app.checkPermission(menu, itemList);
}


_po.sortable = {
    list: {
        "1": "visibility",
        "2": "name",
        "4": "date"
    },
    sorted: "2",
    dir: "up"
};

_po.onActionClick = function (action, data) {
    switch (action) {
        case "edit":
            this.editPost(data.id, data.name);
            break;
        case "remove":
            this.deletePost(data.id, data.name);
            break;
        case "view-in-website":
            var url = app.siteBaseUrl + "blog/" + data.url + "?adminView=true"
            window.open(url,'_blank');
            break;
    }
};

_po.onSelectedActionClick = function(action, selecteds){
    switch (action){
        case "status":
            this.statusSelectedPosts(selecteds);
            break;
        case "visibility":
            //todo: need to implement
            this.visibilitySelectedPosts(selecteds);
            break;
        case "remove":
            this.deleteSelectedPosts(selecteds.collect("id"));
            break;
    }
};

_po.createPost = function (id, name) {
    var _self = this;
    var title = $.i18n.prop("create.post");
    if(id){
        title = $.i18n.prop("edit.post");
    }
    this.renderCreatePanel(app.baseUrl + "blogAdmin/createPost", title, name, {id: id}, {
        width:710,
        success: function () {
            _self.reload();
        },
        content_loaded: function(popup){
            var _form = this
            _form.find('.create-category.link').click(function() {
                _form.find('.close').trigger('click');
                _self.body.find(".view-switch").trigger('click');
            });
            _form.find(".tool-icon.choose-customer").click(function(){
                var restrictionPanel = _form.find(".restricted-visibility-row-restricted")
                restrictionPanel.find("input[value='selected']").radio("check");
                bm.customerAndGroupSelectionPopup(_form.find("form"), {})
            });
            bm.metaTagEditor(_form.find("#bmui-tab-metatag"));
        }
    });
}

_po.editPost = _po.createPost

_po.viewPost = function (id, name) {
    bm.viewPopup(app.baseUrl + "blogAdmin/viewPost", {id: id}, { width: 1000 });
}

_po.deletePost = function (id, name) {
    var _self = this;
    bm.remove("blogPost", "BlogPost", $.i18n.prop("confirm.delete.blog.post", [name]), app.baseUrl + "blogAdmin/deletePost", id, {
        success: function () {
            _self.reload();
        }
    });
}

_po.deleteSelectedPosts = function (ids) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.posts"), function () {
        bm.ajax({
            url: app.baseUrl + "blogAdmin/deleteSelectedPosts",
            data: {ids: ids},
            success: function () {
                _self.reload();
                app.global_event.trigger("send-trash", ["blogPost", ids]);
                _self.body.find(".action-header").hide();
            }
        })
    }, function () {
    });
}

_po.statusSelectedPosts = function(selecteds) {
    var _self = this;
    bm.editPopup(app.baseUrl + 'blogAdmin/loadPostStatusOption', $.i18n.prop('status'), null , {},  {
        width: 600,
        events: {
            content_loaded: function() {
                var $this = $(this);
                var ids = selecteds.collect("id");
                $.each(ids, function (index, value) {
                    $this.find(".edit-popup-form").append('<input type="hidden" name="id" value="' + value + '">');
                });
            }
        },
        success: function () {
            _self.reload();
        }
    });
};

app.ribbons.web_content.push(app.tabs.blog.ribbon_data = {
    text: $.i18n.prop("blog"),
    ui_class: "blog",
    processor: app.tabs.blog.post,
    views: [
        {ui_class: "post", text: $.i18n.prop('blog.post')},
        {ui_class: "category", text: $.i18n.prop('blog.category')},
        {ui_class: "comment", text: $.i18n.prop('blog.comment')}
    ],
    license: "allow_blog_feature"
});

app.tabs.blog.category = function() {
    app.tabs.blog.category._super.constructor.apply(this, arguments);
}

var _ca = app.tabs.blog.category.inherit(app.tabs.blog);

(function () {
    _ca.init = function(){
        app.tabs.blog.category._super.init.call(this);
        var _self = this;
        this.body.find(".toolbar .create").on("click", function() {
            _self.createCategory();
        });
        this.on_global(["blogCategory-create", "blogCategory-update"], function() {
            _self.reload()
        })
    }
})();

_ca.ajax_url =  app.baseUrl + "blogAdmin/loadCategoryAppView";
_ca.advanceSearchUrl = app.baseUrl + "blogAdmin/advanceFilterCategory";

_ca.menu_entries = [
    {
        text: $.i18n.prop("edit"),
        ui_class: "edit"
    },
    {
        text: $.i18n.prop("view.in.site"),
        ui_class: "preview",
        action:"view-in-website"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "remove"
    }
]

_ca.switch_menu_entries = [
    {
        text: $.i18n.prop("blog.post.list"),
        ui_class: "view-switch blog-post list-view",
        action: "post"
    },
    {
        text: $.i18n.prop("blog.comment.list"),
        ui_class: "view-switch blog-comment list-view",
        action: "comment"
    }
];

_ca.sortable = {
    list: {
        "1": "name",
        "3": "created",
        "4": "updated"
    },
    sorted: "1",
    dir: "up"
}

_ca.onMenuOpen = _po.onMenuOpen

_ca.onActionClick = function (action, data) {
    switch (action) {
        case "edit":
            this.editCategory(data.id, data.name);
            break;
        case "remove":
            this.deleteCategory(data.id, data.name);
            break;
        case "view-in-website":
            var url = app.siteBaseUrl + "blog-category/" + data.url + "?adminView=true"
            window.open(url,'_blank');
            break;
    }
};

_ca.onSelectedActionClick = function(action, selecteds){
    switch (action){
        case "remove":
            this.deleteSelectedCategories(selecteds.collect("id"));
            break;
    }
};

_ca.createCategory = function (id, name) {
    var _self = this;
    var title = $.i18n.prop("create.category");
    if(id){
        title = $.i18n.prop("edit.category");
    }
    this.renderCreatePanel(app.baseUrl + "blogAdmin/createCategory", title, name, {id: id}, {
        width: 920,
        success: function () {
            if(id) {
                app.global_event.trigger("blogCategory-update", [id])
            } else {
                app.global_event.trigger("blogCategory-create")
            }
        }
    });
}

app.navigation_item_ref_create_func.blogCategory = _ca.editCategory = _ca.createCategory;

_ca.viewCategory = function (id, name) {
    bm.viewPopup(app.baseUrl + "blogAdmin/viewCategory", {id: id}, { width: 900 });
}

_ca.deleteCategory = function (id, name) {
    var _self = this;
    bm.remove("blog-category", $.i18n.prop("blog.category"), $.i18n.prop("confirm.delete.blog.category", [name]),  app.baseUrl + "blogAdmin/deleteCategory", id, {
        is_final: true,
        success: function() {
            _self.reload();
        }
    });
};

_ca.deleteSelectedCategories = function (ids) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.categories"), function () {
        bm.ajax({
            url: app.baseUrl + "blogAdmin/deleteSelectedCategories",
            data: {ids: ids},
            success: function () {
                _self.reload();
                _self.body.find(".action-header").hide();
            }
        })
    }, function () {
    });
}

app.tabs.blog.comment = function() {
    app.tabs.blog.comment._super.constructor.apply(this, arguments);
}

var _co = app.tabs.blog.comment.inherit(app.tabs.blog);

(function () {
    function attachEvent() {

    }
    _co.init = function(){
        app.tabs.blog.comment._super.init.call(this);
        attachEvent.call(this);
    }
})()

_co.menu_entries = [
    {
        text: $.i18n.prop("approve"),
        ui_class: "approve"
    },
    {
        text: $.i18n.prop("reject"),
        ui_class: "reject"
    },
    {
        text: $.i18n.prop("mark.as.spam"),
        ui_class: "mark-as-spam"
    },
    {
        text: $.i18n.prop("unmark.as.spam"),
        ui_class: "unmark-as-spam"
    },
    {
        text: $.i18n.prop("view"),
        ui_class: "view"
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "remove"
    }
]

_co.switch_menu_entries = [
    {
        text: $.i18n.prop("blog.post.list"),
        ui_class: "view-switch blog-post list-view",
        action: "post"
    },
    {
        text: $.i18n.prop("blog.category.list"),
        ui_class: "view-switch blog-category list-view",
        action: "category"
    }
];

_co.sortable = {
    list: {
        "1": "name",
        "2": "email",
        "5": "status",
        "7": "created"
    },
    sorted: "1",
    dir: "up"
}

_co.onMenuOpen = function(navigator) {
    var menu = this.tabulator.menu;
    if(navigator.is(".approved")) {
        menu.find(".menu-item.approve").hide()
        menu.find(".menu-item.reject").show()
    } else if(navigator.is(".rejected")){
        menu.find(".menu-item.approve").show()
        menu.find(".menu-item.reject").hide()
    } else {
        menu.find(".menu-item.approve").show()
        menu.find(".menu-item.reject").show()
    }
    if(navigator.is(".spam")) {
        menu.find(".menu-item.mark-as-spam").hide()
        menu.find(".menu-item.unmark-as-spam").show()
    }else{
        menu.find(".menu-item.mark-as-spam").show()
        menu.find(".menu-item.unmark-as-spam").hide()
    }
    var itemList = [
        {
            key: "blog.remove",
            class: "remove"
        }
    ];
    app.checkPermission(menu, itemList);
}

_co.onActionClick = function (action, data) {
    var _self = this
    switch (action) {
        case "remove":
            this.deleteComment(data.id, data.name);
            break;
        case "approve":
            bm.confirm( $.i18n.prop("confirm.approve.blog.comment"), function () {
                _self.editComment({id: data.id, status: "approved"})
            }, function () {
            });
            break;
        case "reject":
            bm.confirm( $.i18n.prop("confirm.reject.blog.comment"), function () {
                _self.editComment({id: data.id, status: "rejected"})
            }, function () {
            });
            break;
        case "mark-as-spam":
            this.editComment({id: data.id, isSpam: true})
            break;
        case "view":
            this.viewComment(data.id)
            break;
        case "unmark-as-spam":
            this.editComment({id: data.id, isSpam: false})
            break
    }
};

_co.ajax_url = app.baseUrl + "blogAdmin/loadCommentAppView";
_co.advanceSearchUrl = app.baseUrl + "blogAdmin/advanceFilterComment";

_co.viewComment = function (id) {
    bm.viewPopup(app.baseUrl + "blogAdmin/viewComment", {id: id}, { width: 500 });
}

_co.deleteComment = function (id, name) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.blog.comment", [name]), function () {
        bm.ajax({
            url: app.baseUrl + "blogAdmin/deleteComment",
            data: {id: id},
            success: function (resp) {
                _self.reload();
            }
        });
    }, function () {
    });
}

_co.editComment = function(data) {
    var _self = this;
    bm.ajax({
        url: app.baseUrl + "blogAdmin/editComment",
        data: data,
        success: function () {
            _self.reload();
        }
    });
}

_co.onSelectedActionClick = function(action, selecteds){
    switch (action){
        case "spam":
            this.spamSelectedComments(selecteds);
            break;
        case "status":
            this.statusSelectedComments(selecteds);
            break;
        case "remove":
            this.deleteSelectedComments(selecteds.collect("id"));
            break;
    }
};

_co.deleteSelectedComments = function (ids) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.delete.selected.comments"), function () {
        bm.ajax({
            url: app.baseUrl + "blogAdmin/deleteSelectedComments",
            data: {ids: ids},
            success: function () {
                _self.reload();
                _self.body.find(".action-header").hide();
            }
        })
    }, function () {
    });
}

_co.spamSelectedComments = function(selecteds) {
    var _self = this;
    bm.editPopup(app.baseUrl + 'blogAdmin/loadSpamOption', $.i18n.prop('spam'), null , {},  {
        width: 600,
        events: {
            content_loaded: function() {
                var $this = $(this);
                var ids = selecteds.collect("id");
                $.each(ids, function (index, value) {
                    $this.find(".edit-popup-form").append('<input type="hidden" name="id" value="' + value + '">');
                });
            }
        },
        success: function () {
            _self.reload();
        }
    });
};

_co.statusSelectedComments = function(selecteds) {
    var _self = this;
    bm.editPopup(app.baseUrl + 'blogAdmin/loadCommentStatusOption', $.i18n.prop('status'), null , {},  {
        width: 600,
        events: {
            content_loaded: function() {
                var $this = $(this);
                var ids = selecteds.collect("id");
                $.each(ids, function (index, value) {
                    $this.find(".edit-popup-form").append('<input type="hidden" name="id" value="' + value + '">');
                });
            }
        },
        success: function () {
            _self.reload();
        }
    });
};

var _cu = app.tabs.blog.prototype;
_cu.advanceSearchUrl = app.baseUrl + "blog/advanceFilter";
_cu.advanceSearchTitle = $.i18n.prop("blog");
